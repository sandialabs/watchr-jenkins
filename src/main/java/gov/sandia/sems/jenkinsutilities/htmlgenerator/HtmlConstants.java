/*******************************************************************************
* Watchr
* ------
* Copyright 2021 National Technology & Engineering Solutions of Sandia, LLC (NTESS).
* Under the terms of Contract DE-NA0003525 with NTESS, the U.S. Government retains
* certain rights in this software.
******************************************************************************/
package gov.sandia.sems.jenkinsutilities.htmlgenerator;

import gov.sandia.sems.jenkins.semsjppplugin.CommonConstants;

/**
 * Common constants used in HTML graph generation.
 * 
 * @author Elliott Ridgway
 */
public class HtmlConstants {
    public static final String PARAM_MUSTACHIO         = "~~~~_-_-_~~~~";

    // Graph display configuration options

    public static final String PARAM_PATH                = "path";
    public static final String PARAM_MEASURABLE          = "measurable";
    public static final String PARAM_TIME_SCALE          = "timeScale";
    public static final String PARAM_GRAPH_WIDTH         = "graphWidth";
    public static final String PARAM_GRAPH_HEIGHT        = "graphHeight";
    public static final String PARAM_GRAPHS_PER_ROW      = "graphsPerRow";
    public static final String PARAM_ROUND_TO            = "roundTo";
    public static final String PARAM_SHOW_DESCENDANTS    = "showDescendants";
    public static final String PARAM_SHOW_AVG_LINE       = "showAvgLine";
    public static final String PARAM_SHOW_STD_DEV_LINE   = "showStdDevLine";    
    public static final String PARAM_PAGE                = "page";
    
    public static final String  PARAM_DFLT_PATH        = CommonConstants.ROOT_PATH_ALIAS;
    public static final String  PARAM_DFLT_MEASURABLE  = "";
    public static final int     PARAM_DFLT_TIME_SCALE     = 30;
    public static final int     PARAM_DFLT_GRAPH_WIDTH    = 450; 
    public static final int     PARAM_DFLT_GRAPH_HEIGHT   = 450;
    public static final int     PARAM_DFLT_GRAPHS_PER_ROW = 3;
    public static final int     PARAM_DFLT_ROUND_TO       = 3;
    public static final boolean PARAM_DFLT_SHOW_DESCENDANTS  = true;
    public static final boolean PARAM_DFLT_SHOW_AVG_LINE     = true;
    public static final boolean PARAM_DFLT_SHOW_STD_DEV_LINE = false;
    public static final int     PARAM_DFLT_PAGE              = 0;

    // Main graph options

    public static final String FILTER_FIELD_PATH        = "path";
    public static final String FILTER_FIELD_DATE_PREFIX = "filterDateElement";

    public static final String PARAM_REPORT_MODE                       = "mode";
    public static final String PARAM_REPORT_MODE_FILTER_VIEW           = "filterView";
    public static final String PARAM_REPORT_MODE_FILTER_SAVE           = "filterSave";
    public static final String PARAM_REPORT_MODE_FILTER_SAVE_AND_CLOSE = "filterSaveAndClose";
    public static final String PARAM_REPORT_GRAPH_NAME                 = "graphName";
    public static final String PARAM_REPORT_GRAPH_PATH                 = "graphPath";

    public static final String PARAM_REPORT_ACTION                 = "action";
    public static final String PARAM_REPORT_ACTION_SAVE            = "save";
    public static final String PARAM_REPORT_ACTION_SAVE_AND_CLOSE  = "saveAndClose";
    public static final String PARAM_REPORT_ACTION_SAVE_DISPLAY            = "Save";
    public static final String PARAM_REPORT_ACTION_SAVE_AND_CLOSE_DISPLAY  = "Save and Close";

    // Custom view options

    public static final String PARAM_CUSTOM_VIEW_MODE              = "customViewMode";
    public static final String PARAM_CUSTOM_VIEW_FIELD_NAME        = "customViewFieldName";
    public static final String PARAM_CUSTOM_VIEW_FIELD_SEARCH_TERM = "customViewFieldSearchTerm";
    public static final String PARAM_CUSTOM_VIEW_POST_MODIFY       = "postModify";
    public static final String PARAM_CUSTOM_VIEW_AVAILABLE_PREFIX  = "availableDataset";
    public static final String PARAM_CUSTOM_VIEW_SELECTED_PREFIX   = "selectedDataset";    
    public static final String PARAM_CURRENT_CUSTOM_VIEW           = "currentCustomView";

    public static final String CUSTOM_VIEW_BUILDER_ACTION_SEARCH                  = "search";
    public static final String CUSTOM_VIEW_BUILDER_ACTION_MOVE_AVAILABLE          = "moveAvailable";
    public static final String CUSTOM_VIEW_BUILDER_ACTION_REMOVE_SELECTED         = "removeSelected";
    public static final String CUSTOM_VIEW_BUILDER_ACTION_SAVE                    = "save";

    public static final String CUSTOM_VIEW_BUILDER_ACTION_DISPLAY_SEARCH          = "Search";
    public static final String CUSTOM_VIEW_BUILDER_ACTION_DISPLAY_MOVE_AVAILABLE  = "Move to Selected";
    public static final String CUSTOM_VIEW_BUILDER_ACTION_DISPLAY_REMOVE_SELECTED = "Remove from Selected";
    public static final String CUSTOM_VIEW_BUILDER_ACTION_DISPLAY_SAVE            = "Save";

    public static final String PARAM_CUSTOM_VIEW_SEARCH_STYLE = "searchStyle";
    public static final String CUSTOM_VIEW_SEARCH_BY_NAME = "searchByName";
    public static final String CUSTOM_VIEW_SEARCH_BY_PATH = "searchByFullPath";
    public static final String CUSTOM_VIEW_SEARCH_BY_NAME_VALUE = "name";
    public static final String CUSTOM_VIEW_SEARCH_BY_PATH_VALUE = "path";
    public static final String CUSTOM_VIEW_SEARCH_BY_NAME_DISPLAY = "Search by name";
    public static final String CUSTOM_VIEW_SEARCH_BY_PATH_DISPLAY = "Search by full path";

    public static final String PARAM_CUSTOM_VIEW_REJECT_REASON       = "rejectReason";
    public static final String CUSTOM_VIEW_REJECT_REASON_EMPTY_NAME  = "emptyViewName";
    public static final String CUSTOM_VIEW_REJECT_REASON_DUP_NAME    = "duplicateViewName";
    public static final String CUSTOM_VIEW_REJECT_REASON_BAD_NAME    = "badViewName";
    public static final String CUSTOM_VIEW_REJECT_REASON_NO_DATASETS = "noDatasets";

    public static final String CUSTOM_VIEW_MODE_ADD            = "add";
    public static final String CUSTOM_VIEW_MODE_EDIT           = "edit";
    public static final String CUSTOM_VIEW_MODE_DELETE_ASK     = "deleteAsk";
    public static final String CUSTOM_VIEW_MODE_DELETE         = "delete";
    public static final String CUSTOM_VIEW_MODE_UPDATE_DISPLAY = "updateDisplay";

    public static final String PARAM_DFLT_CUSTOM_VIEW_MODE  = "";
    public static final String PARAM_DFLT_CUSTOM_VIEW_NAME  = "";
    public static final String PARAM_DFLT_CUSTOM_VIEW_SEARCH_TERM = "";

    // Paging

    public static final int MAX_GRAPHS_PER_PAGE = 15;

    // HTML fragments
    
    public static final String GRAPH_OPTIONS_FORM = "frmOptions";
    public static final String TITLE_STYLE          = "<p style='border-bottom: 1px #DDDDDD solid; margin-bottom: 5px; margin-left: 10px; font-size: 14pt; font-weight: bold; font-variant: small-caps'>";
    public static final String MENU_BAR_TITLE_STYLE = "border-bottom: 2px #000000 solid; margin-bottom: 5px; margin-left: 10px; font-size: 16pt; font-weight: bold";
    public static final String TABLE_STYLE          = "<table style='margin-left: 10px' cellspacing='20' cellpadding='5'>";
    public static final String STATS_DIV_STYLE      = "<div align='left' style=\"font-size:75%; text-align:write; width:400px; height:60px;\" id=\"";
    public static final String IMG_STYLE            = "display:table-cell; vertical-align:middle; text-align:center";
    public static final String SUB_BLOCK_STYLE      = "margin-bottom: 1px; margin-left: 10px; font-size: 10pt; font-weight: 550";
    public static final String UP_ONE_LEVEL_STYLE   = "margin-left: 10px; text-decoration: none; font-variant: small-caps; font-size: 14pt";
    public static final String SPACING              = "&nbsp;&nbsp;&nbsp;";

    private HtmlConstants() {}
}