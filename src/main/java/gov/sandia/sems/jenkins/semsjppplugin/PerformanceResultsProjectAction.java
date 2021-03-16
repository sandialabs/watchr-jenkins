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
import gov.sandia.sems.jenkinsutilities.htmlgenerator.ConfigContext;
import gov.sandia.sems.jenkinsutilities.htmlgenerator.HtmlFragmentGenerator;
import gov.sandia.sems.jenkinsutilities.htmlgenerator.HtmlGenerator;
import gov.sandia.sems.jenkinsutilities.htmlgenerator.MainReportMode;
import gov.sandia.sems.jenkinsutilities.treeaccessor.ITreeAccessor;
import gov.sandia.sems.jenkinsutilities.utilities.LogUtil;
import hudson.model.Job;
import hudson.model.ProminentProjectAction;
import hudson.model.Run;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.JsonObject;


/**
 * This action is responsible for launching the page that displays the data
 * graphs for Watchr.  This action is docked on the leftmost
 * control panel of any job that has Watchr enabled.
 * 
 * @author Elliott Ridgway, Lawrence Allen
 */
public final class PerformanceResultsProjectAction implements ProminentProjectAction{

    ////////////
    // FIELDS //
    ////////////

    private static final Map<Job<?,?>, ConfigContext> contextMap;
    private final Job<?,?> job;
    private File logFile;
    
    private HtmlGenerator htmlGenerator;
    
    /////////////////
    // STATIC INIT //
    /////////////////

    static {
        contextMap = new HashMap<>();
    }
    
    /////////////////
    // CONSTRUCTOR //
    /////////////////

    public PerformanceResultsProjectAction(Job<?, ?> job) {
        this.job = job;
        createHtmlGenerator();
    }

    //////////////
    // OVERRIDE //
    //////////////

    @Override
    public String getIconFileName() {
        return "/plugin/sems-jpp-plugin/watchr48x48.png";
    }
    
    @Override
    public String getDisplayName() {
        return "Performance Reports";
    }
    
    @Override
    public String getUrlName() {
        return "performanceReports";
    }
    
    ///////////
    // JELLY //
    ///////////

    // Note:  The order of methods listed in this section reflects the order in which
    // they will be called from the index.jelly file.
    
    /**
     * Called from the PerformanceResultsProjectAction index.jelly to initialize
     * the HtmlGenerator object.
     */
    public void createHtmlGenerator() {
        Run<?, ?> lastBuild = job.getLastBuild();
        this.logFile = lastBuild.getLogFile();

        // Currently, Watchr only supports file/folder-based database implementations.
        IDatabaseAccessor db = new DiskDatabaseAccessor(job);

        PerformanceResultAction currentPerformanceResultAction = lastBuild.getAction(PerformanceResultAction.class);

        String[] measurables = currentPerformanceResultAction.getUserDefinedMeasurables();
        String defaultMeasurable = "";
        if(measurables.length == 0) {
            LogUtil.writeToLog(this.logFile, "Measurables length was 0");
        } else {
            defaultMeasurable = measurables[0];
        }

        boolean avgFailIfGreater    = currentPerformanceResultAction.getAvgFailIfGreater();
        boolean stdDevFailIfGreater = currentPerformanceResultAction.getStdDevFailIfGreater();

        ConfigContext context = getConfigContextOrDefault(job, defaultMeasurable);
        try {
            context.updateHtmlGeneratorSettingsFromParameterList();
        } catch(UnsupportedEncodingException e) {
            LogUtil.writeErrorToLog(logFile, e);
        }
        contextMap.put(job, context);

        context.updateMainReportSettingsFromNewStaplerRequest();

        ITreeAccessor treeAccess = (ITreeAccessor) db.getDatabasePart(IDatabasePartType.TREE);
        String levelSeparator = treeAccess.getLevelSeparator();

        this.htmlGenerator = new HtmlGenerator(
            context, db, measurables, avgFailIfGreater, stdDevFailIfGreater, levelSeparator, logFile
        );
    }
        
    /**
     * Called from the PerformanceResultsProjectAction index.jelly to gather
     * all of the data necessary for displaying graphs.
     * @return A String representation of a JSON that stores all the graph data.
     */
    public String getDataFromPath() {
        if(htmlGenerator != null) {
            return htmlGenerator.getDataFromPath();
        }
        return "{}";
    }
    
    /**
     * Called from the PerformanceResultsProjectAction index.jelly to get
     * a reference to the current job "owner".
     * @return The current job.
     */
    public Run<?, ?> getOwner() {
        return job.getLastBuild();
    }
    
    /**
     * Called from the PerformanceResultsProjectAction index.jelly to generate
     * all of the HTML for the graph header menu.
     * @return A String representing the HTML for the graph header menu.
     */
    public String getMenuHTML() {
        ConfigContext configContext = getConfigContext(job);
        if(htmlGenerator != null && configContext.getMainReportMode() == MainReportMode.NONE) {
            return htmlGenerator.getMenuBar();
        }
        return "";
    }
    
    /**
     * Called from the PerformanceResultsProjectAction index.jelly to generate
     * all of the HTML for the graphs.
     * @return A String representing the HTML for the graphs.
     */
    public String getGraphHTML() {
        ConfigContext configContext = getConfigContext(job);
        if(htmlGenerator != null && configContext.getMainReportMode() == MainReportMode.NONE) {
            return htmlGenerator.getGraphHTML();
        } else if(configContext.getMainReportMode() == MainReportMode.VIEW_FILTER) {
            try {
                JsonObject dataBundleJson = configContext.loadDataForFilterPage();
                return HtmlFragmentGenerator.buildGraphFilterEditor(dataBundleJson);
            } catch(Exception e) {
                LogUtil.writeErrorToLog(logFile, e);
            }
        }
        return "";
    }

    /////////////
    // GETTERS //
    /////////////

    /**
     * Retrieve the {@link ConfigContext} associated with this action's {@link Job}.
     * @param job The Job of this action.
     * @return The associated ConfigContext, or null if none has been defined.
     */
    public static ConfigContext getConfigContext(Job<?,?> job) {
        return contextMap.get(job);
    }

    /**
     * Retrieve the {@link ConfigContext} associated with this action's {@link Job}.
     * If none is in the map, then insert a default.
     * @param job The Job of this action.
     * @param defaultMeasurable Required because every ConfigContext must have a beginning
     * defaultMeasurable String to initially display.
     * @return The associated ConfigContext.
     */
    public static ConfigContext getConfigContextOrDefault(Job<?,?> job, String defaultMeasurable) {
        // Currently, Watchr only supports file/folder-based database implementations.
        IDatabaseAccessor db = new DiskDatabaseAccessor(job);
        return contextMap.getOrDefault(job, new ConfigContext(db, defaultMeasurable));
    }   
}