/*******************************************************************************
* Watchr
* ------
* Copyright 2021 National Technology & Engineering Solutions of Sandia, LLC (NTESS).
* Under the terms of Contract DE-NA0003525 with NTESS, the U.S. Government retains
* certain rights in this software.
******************************************************************************/
package gov.sandia.watchr.actions;

import hudson.model.Job;
import hudson.model.ProminentProjectAction;
import hudson.model.Run;

import java.io.UnsupportedEncodingException;

import org.apache.commons.lang3.StringUtils;

import gov.sandia.watchr.WatchrCoreApp;
import gov.sandia.watchr.WatchrJenkinsApp;
import gov.sandia.watchr.config.GraphDisplayConfig;
import gov.sandia.watchr.config.GraphDisplayConfig.LeafNodeStrategy;
import gov.sandia.watchr.graph.JenkinsHtmlFragmentGenerator;
import gov.sandia.watchr.graph.library.GraphOperationMetadata;
import gov.sandia.watchr.graph.library.GraphOperationResult;
import gov.sandia.watchr.model.JenkinsConfigContext;
import gov.sandia.watchr.util.StaplerRequestUtil;
import gov.sandia.watchr.log.ILogger;

/**
 * This action is responsible for launching the page that displays the data
 * graphs for Watchr.  This action is docked on the leftmost
 * control panel of any job that has Watchr enabled.
 * 
 * @author Elliott Ridgway, Lawrence Allen
 */
public final class PerformanceResultsProjectAction implements ProminentProjectAction {

    ////////////
    // FIELDS //
    ////////////

    private final Job<?,?> job;
    
    /////////////////
    // CONSTRUCTOR //
    /////////////////

    public PerformanceResultsProjectAction(Job<?, ?> job) {
        this.job = job;
        WatchrJenkinsApp.useAndGetLoggerForJob(job);
    }

    //////////////
    // OVERRIDE //
    //////////////

    @Override
    public String getIconFileName() {
        return "/plugin/watchr-jenkins/watchr48x48.png";
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
        updateGraphConfigurationOnPageLoad();

        ILogger logger = WatchrJenkinsApp.useAndGetLoggerForJob(job);
        logger.logInfo(StaplerRequestUtil.echoCurrentStaplerRequest());

        deleteAnyPlotsOnPageLoad();
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
     * all of the HTML for Watchr graphs.
     * @return A String representing the HTML for the graph header menu.
     */
    public String getHTML() {
        
        WatchrCoreApp coreApp = WatchrJenkinsApp.getAppForJob(job);
        StringBuilder htmlSb = new StringBuilder();
        JenkinsHtmlFragmentGenerator fragmentGenerator = new JenkinsHtmlFragmentGenerator();

        try {
            JenkinsConfigContext configContext = WatchrJenkinsApp.getConfigContextOrDefault(job);
            GraphDisplayConfig plotConfiguration = configContext.getGraphDisplayConfig();      
            plotConfiguration.setLeafNodeStrategy(LeafNodeStrategy.TRAVEL_UP_TO_PARENT);
            String dbName = configContext.getDatabaseName();

            coreApp.getLogger().logInfo(plotConfiguration.toString());

            GraphOperationResult graphResult = coreApp.getGraphHtml(dbName, plotConfiguration, false);

            if(graphResult.getMetadata().keySet().contains(GraphOperationMetadata.PLOT_DB_LOCATION.get())) {
                plotConfiguration.setLastPlotDbLocation(graphResult.getMetadata().get(GraphOperationMetadata.PLOT_DB_LOCATION.get()));
            }

            int numberOfGraphs = 15; // Arbitrary temp value.
            if(graphResult.getMetadata().keySet().contains(GraphOperationMetadata.NUMBER_OF_GRAPHS.get())) {
                numberOfGraphs = Integer.parseInt(graphResult.getMetadata().get(GraphOperationMetadata.NUMBER_OF_GRAPHS.get()));
            }

            htmlSb.append(fragmentGenerator.buildMenuBar(configContext, numberOfGraphs));
            htmlSb.append(graphResult.getHtml());
        } catch(Exception e) {
            coreApp.getLogger().logError("getHTML Error:", e);
        }

        return htmlSb.toString();
    }

    /////////////
    // PRIVATE //
    /////////////

    private void updateGraphConfigurationOnPageLoad() {
        ILogger logger = WatchrJenkinsApp.useAndGetLoggerForJob(job);
        try {
            JenkinsConfigContext context = WatchrJenkinsApp.getConfigContextOrDefault(job);
            StaplerRequestUtil.updateGraphDisplayConfigFromParameterList(context.getGraphDisplayConfig());
            WatchrJenkinsApp.addConfigContext(context);
        } catch(UnsupportedEncodingException e) {
            logger.logError("Error occurred updating HTML generator settings from the sent parameter list.", e);
        }
    }

    private void deleteAnyPlotsOnPageLoad() {
        WatchrCoreApp coreApp = WatchrJenkinsApp.getAppForJob(job);
        ILogger logger = WatchrJenkinsApp.useAndGetLoggerForJob(job);
        
        try {
            String deletedPlotName     = StaplerRequestUtil.getDeletedPlotNameFromParameterList();
            String deletedPlotCategory = StaplerRequestUtil.getDeletedPlotCategoryFromParameterList();
            
            if(StringUtils.isNotBlank(deletedPlotName)) {
                String logInfoMessage = 
                    "Attempting to delete " + deletedPlotName + " from category " + deletedPlotCategory + " (watchr-jenkins)";
                logger.logInfo(logInfoMessage);
                JenkinsConfigContext context = WatchrJenkinsApp.getConfigContextOrDefault(job);
                coreApp.deletePlotFromDatabase(
                    context.getDatabaseName(), deletedPlotName, deletedPlotCategory
                );
                coreApp.saveDatabase(context.getDatabaseName());
            } else {
                logger.logInfo("Nothing to delete (watchr-jenkins)");
            }
        } catch(UnsupportedEncodingException e) {
            logger.logError("Error occurred deleting plot.", e);
        }
    }
}