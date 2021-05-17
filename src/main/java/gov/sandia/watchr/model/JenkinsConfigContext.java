/*******************************************************************************
* Watchr
* ------
* Copyright 2021 National Technology & Engineering Solutions of Sandia, LLC (NTESS).
* Under the terms of Contract DE-NA0003525 with NTESS, the U.S. Government retains
* certain rights in this software.
******************************************************************************/
package gov.sandia.watchr.model;

import java.io.File;
import java.util.Set;

import gov.sandia.watchr.WatchrCoreApp;
import gov.sandia.watchr.WatchrJenkinsApp;
import gov.sandia.watchr.config.GraphDisplayConfig;
import gov.sandia.watchr.config.GraphDisplayConfig.GraphDisplaySort;
import gov.sandia.watchr.util.CommonConstants;

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
    public static final String PARAM_DELETE              = "delete";

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
    private String databaseName;
    private File databaseRootDir;

    public JenkinsConfigContext(String databaseName, File databaseRootDir) {
        this.databaseName = databaseName;
        this.databaseRootDir = databaseRootDir;

        File dbDir = new File(databaseRootDir, "db");
        WatchrJenkinsApp.loadDatabase(databaseName, dbDir);

        graphDisplayConfig = WatchrCoreApp.getInstance().getDatabaseGraphDisplayConfiguration(databaseName);
        if(graphDisplayConfig == null) {
            graphDisplayConfig = new GraphDisplayConfig("");
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

    public String getDatabaseName() {
        return databaseName;
    }

    public GraphDisplayConfig getGraphDisplayConfig() {
        return graphDisplayConfig;
    }
    
    public Set<String> getCategories() {
        File dbDir = new File(databaseRootDir, "db");
        WatchrJenkinsApp.loadDatabase(databaseName, dbDir);
        return WatchrCoreApp.getInstance().getDatabaseCategories(databaseName);
    }

    public int getNumberOfPlots() {
        File dbDir = new File(databaseRootDir, "db");
        WatchrJenkinsApp.loadDatabase(databaseName, dbDir);
        return WatchrCoreApp.getInstance().getPlotsSize(databaseName);
    }

    public int getNumberOfFailedPlots() {
        File dbDir = new File(databaseRootDir, "db");
        WatchrJenkinsApp.loadDatabase(databaseName, dbDir);
        return WatchrCoreApp.getInstance().getFailedPlotsSize(databaseName);
    }
}
