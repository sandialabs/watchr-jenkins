/*******************************************************************************
* Watchr
* ------
* Copyright 2021 National Technology & Engineering Solutions of Sandia, LLC (NTESS).
* Under the terms of Contract DE-NA0003525 with NTESS, the U.S. Government retains
* certain rights in this software.
******************************************************************************/
package gov.sandia.watchr.model;

import java.util.Set;

import gov.sandia.watchr.WatchrCoreApp;
import gov.sandia.watchr.WatchrJenkinsApp;
import gov.sandia.watchr.config.GraphDisplayConfig;
import gov.sandia.watchr.config.GraphDisplayConfig.GraphDisplaySort;
import gov.sandia.watchr.log.ILogger;
import gov.sandia.watchr.util.CommonConstants;
import hudson.model.Job;

public class JenkinsConfigContext {

    ////////////
    // FIELDS //
    ////////////
    
    public static final String PARAM_PATH                = "path";
    public static final String PARAM_PAGE                = "page";
    public static final String PARAM_CATEGORY            = "category";
    public static final String PARAM_TIME_SCALE          = "timeScale";
    public static final String PARAM_GRAPH_WIDTH         = "graphWidth";
    public static final String PARAM_GRAPH_HEIGHT        = "graphHeight";
    public static final String PARAM_GRAPHS_PER_ROW      = "graphsPerRow";
    public static final String PARAM_ROUND_TO            = "roundTo";
    public static final String PARAM_SORT_ASCENDING      = "sortAscending";
    public static final String PARAM_DELETE_NAME         = "deleteName";
    public static final String PARAM_DELETE_CATEGORY     = "deleteCategory";

    public static final String  PARAM_DFLT_PATH           = CommonConstants.ROOT_PATH_ALIAS;
    public static final int     PARAM_DFLT_PAGE           = 1;    
    public static final String  PARAM_DFLT_CATEGORY       = "";
    public static final int     PARAM_DFLT_TIME_SCALE     = 30;
    public static final int     PARAM_DFLT_GRAPH_WIDTH    = 500; 
    public static final int     PARAM_DFLT_GRAPH_HEIGHT   = 500;
    public static final int     PARAM_DFLT_GRAPHS_PER_ROW  = 3;
    public static final int     PARAM_DFLT_GRAPHS_PER_PAGE = 15;
    public static final int     PARAM_DFLT_ROUND_TO       = 3;
    public static final boolean PARAM_DFLT_DELETE         = false;
    public static final GraphDisplaySort PARAM_DFLT_SORT_ASCENDING = GraphDisplaySort.ASCENDING;

    private GraphDisplayConfig graphDisplayConfig;

    private final Job<?,?> job;

    public JenkinsConfigContext(Job<?,?> job) {
        this.job = job;
        WatchrJenkinsApp.loadDatabase(job);

        WatchrCoreApp app = WatchrJenkinsApp.getAppForJob(job);
        graphDisplayConfig = app.getDatabaseGraphDisplayConfiguration(getDatabaseName());
        if(graphDisplayConfig == null) {
            graphDisplayConfig = new GraphDisplayConfig("", app.getLogger());
            graphDisplayConfig.setNextPlotDbLocation(PARAM_DFLT_PATH);
            graphDisplayConfig.setLastPlotDbLocation(PARAM_DFLT_PATH);
            graphDisplayConfig.setPage(PARAM_DFLT_PAGE);
            graphDisplayConfig.setGraphsPerRow(PARAM_DFLT_GRAPHS_PER_ROW);
            graphDisplayConfig.setGraphWidth(PARAM_DFLT_GRAPH_WIDTH);
            graphDisplayConfig.setGraphHeight(PARAM_DFLT_GRAPH_HEIGHT);
            graphDisplayConfig.setGraphsPerPage(PARAM_DFLT_GRAPHS_PER_PAGE);
            graphDisplayConfig.setDisplayRange(PARAM_DFLT_TIME_SCALE);
            graphDisplayConfig.setDisplayedDecimalPlaces(PARAM_DFLT_ROUND_TO);
            graphDisplayConfig.setDisplayCategory(PARAM_DFLT_CATEGORY);
            graphDisplayConfig.setSort(PARAM_DFLT_SORT_ASCENDING);
        }
    }

    /////////////
    // GETTERS //
    /////////////

    public Job<?,?> getJob() {
        return job;
    }

    public String getDatabaseName() {
        return job.getName();
    }

    public GraphDisplayConfig getGraphDisplayConfig() {
        return graphDisplayConfig;
    }
    
    public Set<String> getCategories() {
        WatchrJenkinsApp.loadDatabase(job);
        WatchrCoreApp app = WatchrJenkinsApp.getAppForJob(job);
        Set<String> categories = app.getDatabaseCategories(getDatabaseName());

        ILogger logger = app.getLogger();
        logger.logInfo(categories.toString());
        return categories;
    }

    public int getNumberOfPlots() {
        WatchrJenkinsApp.loadDatabase(job);
        WatchrCoreApp app = WatchrJenkinsApp.getAppForJob(job);
        int plotNumber = app.getPlotsSize(getDatabaseName());

        ILogger logger = app.getLogger();
        logger.logInfo("Number of plots: " + plotNumber);
        return plotNumber;
    }

    public int getNumberOfFailedPlots() {
        WatchrJenkinsApp.loadDatabase(job);
        WatchrCoreApp app = WatchrJenkinsApp.getAppForJob(job);
        int plotNumber = app.getFailedPlotsSize(getDatabaseName());

        ILogger logger = app.getLogger();
        logger.logInfo("Number of failed plots: " + plotNumber);
        return plotNumber;
    }
}
