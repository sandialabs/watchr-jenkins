/*******************************************************************************
* Watchr
* ------
* Copyright 2021 National Technology & Engineering Solutions of Sandia, LLC (NTESS).
* Under the terms of Contract DE-NA0003525 with NTESS, the U.S. Government retains
* certain rights in this software.
******************************************************************************/
package gov.sandia.sems.jenkins.semsjppplugin;

import gov.sandia.sems.jenkinsutilities.db.DiskDatabaseAccessor;
import gov.sandia.sems.jenkinsutilities.db.IDatabaseAccessor;
import gov.sandia.sems.jenkinsutilities.db.IDatabasePartType;
import gov.sandia.sems.jenkinsutilities.htmlgenerator.customviews.CustomViewBuilderAction;
import gov.sandia.sems.jenkinsutilities.htmlgenerator.customviews.CustomViewConfigContext;
import gov.sandia.sems.jenkinsutilities.htmlgenerator.customviews.CustomViewMode;
import gov.sandia.sems.jenkinsutilities.htmlgenerator.customviews.CustomViewsHtmlFragmentGenerator;
import gov.sandia.sems.jenkinsutilities.htmlgenerator.customviews.CustomViewsHtmlGenerator;
import gov.sandia.sems.jenkinsutilities.utilities.LogUtil;
import gov.sandia.sems.jenkinsutilities.views.IViewAccessor;
import gov.sandia.sems.jenkinsutilities.views.View;
import hudson.model.Job;
import hudson.model.ProminentProjectAction;
import hudson.model.Run;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.JsonObject;

import org.apache.commons.lang3.StringUtils;

/**
 * The Jenkins job action responsible for displaying, adding, editing, and deleting
 * custom performance report views.
 * 
 * @author Elliott Ridgway
 */
public final class PerformanceResultViewsProjectAction implements ProminentProjectAction{

    ////////////
    // FIELDS //
    ////////////

    private static final Map<Job<?,?>, CustomViewConfigContext> contextMap;
    private final Job<?,?> job;
    private CustomViewsHtmlGenerator customViewsHtmlGenerator;
    private String defaultMeasurable;

    private IDatabaseAccessor db;
    private File logFile;

    /////////////////
    // STATIC INIT //
    /////////////////

    static {
        contextMap = new HashMap<>();
    }
    
    /////////////////
    // CONSTRUCTOR //
    /////////////////

    public PerformanceResultViewsProjectAction(Job<?, ?> job) {
        this.job = job;
        // Currently, Watchr only supports file/folder-based database implementations.
        db = new DiskDatabaseAccessor(job);
    }

    //////////////
    // OVERRIDE //
    //////////////

    @Override
    public String getIconFileName() {
        return "/plugin/sems-jpp-plugin/watchr_views_48x48.png";
    }
    
    @Override
    public String getDisplayName() {
        return "Performance Report Custom Views";
    }
    
    @Override
    public String getUrlName() {
        return "performanceReportViews";
    }
    
    ///////////
    // JELLY //
    ///////////

    // Note:  The order of methods listed in this section reflects the order in which
    // they will be called from the index.jelly file.
    
    /**
     * Called from the PerformanceResultViewsProjectAction index.jelly to initialize
     * the CustomViewsHtmlGenerator object.
     */
    public void createHtmlGenerator() {
        Run<?, ?> lastBuild = job.getLastBuild();
        this.logFile = lastBuild.getLogFile();

        PerformanceResultAction currentPerformanceResultAction = lastBuild.getAction(PerformanceResultAction.class);
        String[] measurables = currentPerformanceResultAction.getUserDefinedMeasurables();
        defaultMeasurable = new String();
        if(measurables.length == 0) {
            LogUtil.writeToLog(this.logFile, "Measurables length was 0");
        } else {
            defaultMeasurable = measurables[0];
        }
        
        CustomViewConfigContext context = getConfigContextOrDefault(job, db, defaultMeasurable);
        this.customViewsHtmlGenerator = new CustomViewsHtmlGenerator(context, job, db, measurables, logFile);

        updateCustomViewSettingsFromNewStaplerRequest(db);        
    }

     /**
     * Called from the PerformanceResultViewsProjectAction index.jelly to get
     * the graph data.
     * @return A String representing the JSON making up the graph data.
     */
    public String getDataFromPath() {
        String jsonAsString = "{}";
        if(customViewsHtmlGenerator != null) {
            jsonAsString = customViewsHtmlGenerator.getDataFromPath();
            if(StringUtils.isBlank(jsonAsString)) {
                jsonAsString = "{}";
            }
        }
        return jsonAsString;
    }    
    
    /**
     * Called from the PerformanceResultViewsProjectAction index.jelly to generate
     * all of the HTML for the graph header menu.
     * @return A String representing the HTML for the graph header menu.
     */
    public String getMenuHTML() {
        CustomViewConfigContext configContext = getConfigContext(job);
        CustomViewMode customViewMode = configContext.getCustomViewMode();
        if(configContext.shouldLoadViewBuilder() || customViewMode == CustomViewMode.DELETE_ASK) {
            return new String(); // Do not create a menu header area if we're in a special mode.
        }

        if(customViewsHtmlGenerator != null) {
            return customViewsHtmlGenerator.getMenuBar();
        }
        return new String();
    }
    
    /**
     * Called from the PerformanceResultViewsProjectAction index.jelly to generate
     * all of the HTML for the graphs.
     * @return A String representing the HTML for the graphs.
     */
    public String getGraphHTML() {
        CustomViewConfigContext configContext = getConfigContext(job);
        final CustomViewMode customViewMode = configContext.getCustomViewMode();

        if(configContext.shouldLoadViewBuilder()) {
            JsonObject dataBundleJson = configContext.loadDataForViewBuilderPage();
            return CustomViewsHtmlFragmentGenerator.buildCustomViewEditor(dataBundleJson);
        } else if(customViewMode == CustomViewMode.DELETE_ASK) {
            String name = configContext.getCurrentCustomViewName();
            return CustomViewsHtmlFragmentGenerator.buildDeleteAskForm(name);
        } else if(customViewsHtmlGenerator != null) {
            return customViewsHtmlGenerator.getGraphHTML();
        }
        return new String();
    }

    /**
     * Called from the PerformanceResultViewsProjectAction index.jelly to get
     * a reference to the current job "owner".
     * @return The current job.
     */
    public Run<?, ?> getOwner() {
        return job.getLastBuild();
    }

    /////////////
    // GETTERS //
    /////////////

    public static CustomViewConfigContext getConfigContext(Job<?,?> job) {
        return contextMap.get(job);
    }

    public static CustomViewConfigContext getConfigContextOrDefault(
            Job<?,?> job, IDatabaseAccessor db, String defaultMeasurable) {
        return contextMap.getOrDefault(job, new CustomViewConfigContext(db, defaultMeasurable));
    }

    /////////////
    // SETTERS //
    /////////////

    public static void putConfigContext(Job<?,?> job, CustomViewConfigContext context) {
        contextMap.put(job, context);
    }    

    /////////////
    // PRIVATE //
    /////////////

    /**
     * When a Stapler parameter list is populated with data (usually following a page submit/refresh),
     * this method should be called in order to update the context's configuration settings based
     * on new parameter information.
     * 
     * @param db The IDatabaseAccessor for this operation.
     */
    private void updateCustomViewSettingsFromNewStaplerRequest(IDatabaseAccessor db) {
        CustomViewConfigContext context = getConfigContextOrDefault(job, db, defaultMeasurable);

        // LogUtil.writeToLog(logFile, "Stapler echo:");
        // StaplerRequestUtil.echoCurrentStaplerRequest(logFile);
        // LogUtil.writeToLog(logFile, context.toString());
        // LogUtil.writeToLog(logFile, "DiskViewAccessor {{{\n" + viewAccess.getAllViews().toString() + "\n}}}\n");

        context.updateCustomViewModeFromStaplerRequest();
        CustomViewMode customViewMode = context.getCustomViewMode();

        if(customViewMode == CustomViewMode.ADD || customViewMode == CustomViewMode.EDIT) {
            context.updateBuilderActionFromStaplerRequest();
        }

        context.updateIfAnyNewDataPresentInStaplerRequest();
        if(customViewMode == CustomViewMode.UPDATE_DISPLAY) {
            context.updateGraphDisplaySettingsFromStaplerRequest();
        } else {
            context.updateIfCurrentViewInStaplerRequest();
        }

        final CustomViewBuilderAction viewBuilderAction = context.getViewBuilderAction();
        if(viewBuilderAction == CustomViewBuilderAction.SAVE) {
            context.detectRejectReason();
        }       
        final String rejectSaveReason = context.getRejectSaveReason();

        if(customViewMode == CustomViewMode.NONE && viewBuilderAction == CustomViewBuilderAction.NONE) {
            context.clearAll();
        } else if(customViewMode == CustomViewMode.ADD || customViewMode == CustomViewMode.EDIT) {
            if(viewBuilderAction == CustomViewBuilderAction.SAVE && StringUtils.isBlank(rejectSaveReason)) {
                customViewsHtmlGenerator.saveViewFromEditorPageCache();
                context.clearAll();
            }
        } else if(customViewMode == CustomViewMode.DELETE) {
            View viewToDelete = context.getCurrentView();
            if(viewToDelete != null) {
                boolean deleteSuccess = deleteView(viewToDelete);
                if(!deleteSuccess) {
                    String message = "Delete operation for view " + context.getCurrentCustomViewName() + " was not successful";
                    LogUtil.writeToLog(logFile, message);
                }
            }
            context.clearAll();
        }
        putConfigContext(job, context);
    }

    private boolean deleteView(View viewToDelete) {
        IViewAccessor viewAccess = (IViewAccessor) db.getAndOpenDatabasePart(IDatabasePartType.VIEWS);

        boolean success = viewAccess.deleteView(viewToDelete);
        success = success && viewAccess.close();
        success = success && viewAccess.open();
        return success;
    }
}