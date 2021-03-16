/*******************************************************************************
* Watchr
* ------
* Copyright 2021 National Technology & Engineering Solutions of Sandia, LLC (NTESS).
* Under the terms of Contract DE-NA0003525 with NTESS, the U.S. Government retains
* certain rights in this software.
******************************************************************************/
package gov.sandia.sems.jenkinsutilities.htmlgenerator;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import org.apache.commons.lang3.StringUtils;

import gov.sandia.sems.jenkinsutilities.db.IDatabaseAccessor;
import gov.sandia.sems.jenkinsutilities.db.IDatabasePartType;
import gov.sandia.sems.jenkinsutilities.treeaccessor.FilterData;
import gov.sandia.sems.jenkinsutilities.treeaccessor.IFilterAccessor;
import gov.sandia.sems.jenkinsutilities.treeaccessor.ITreeAccessor;
import gov.sandia.sems.jenkinsutilities.treeaccessor.OneLevelTree;
import gov.sandia.sems.jenkinsutilities.utilities.StaplerRequestUtil;

/**
 * A state model for the Performance Report Views page.
 * 
 * @author Elliott Ridgway
 */
public class ConfigContext extends AbstractConfigContext {

    ////////////
    // FIELDS //
    ////////////

    public static final String JSON_PROPERTY_GRAPHNAME = "graphName";
    public static final String JSON_PROPERTY_GRAPHPATH = "graphPath";
    public static final String JSON_PROPERTY_DATES = "dates";
    public static final String JSON_PROPERTY_FILTERDATES = "filterDates";

    private final IDatabaseAccessor db;

    private MainReportMode mainReportMode = MainReportMode.NONE;
    private MainReportAction mainReportAction = MainReportAction.NONE;
    private String filterGraphName = "";
    private String filterGraphPath = "";

    /////////////////
    // CONSTRUCTOR //
    /////////////////

    /**
     * @param db Access to the database.
     * @param defaultMeasurable The default measurable that this context should begin with.
     */
    public ConfigContext(final IDatabaseAccessor db, String defaultMeasurable) {
        super(defaultMeasurable);
        this.db = db;
    }

    /////////////
    // GETTERS //
    /////////////

    public MainReportMode getMainReportMode() {
        return mainReportMode;
    }

    public MainReportAction getMainReportAction() {
        return mainReportAction;
    }

    public String getSelectedGraphName() {
        return filterGraphName;
    }

    public String getSelectedGraphPath() {
        return filterGraphPath;
    }

    /////////////
    // SETTERS //
    /////////////
    
    public void setMainReportMode(MainReportMode mainReportMode) {
        this.mainReportMode = mainReportMode;
    }

    public void setMainReportAction(MainReportAction mainReportAction) {
        this.mainReportAction = mainReportAction;
    }

    public void setSelectedGraphName(String filterGraphName) {
        this.filterGraphName = filterGraphName;
    }

    public void setSelectedGraphPath(String filterGraphPath) {
        if(StringUtils.isBlank(filterGraphPath)) {
            ITreeAccessor treeAccess = (ITreeAccessor) db.getAndOpenDatabasePart(IDatabasePartType.TREE);
            this.filterGraphPath = treeAccess.getRootPath();
        } else {
            this.filterGraphPath = filterGraphPath;
        }
    }

    /////////////
    // UTILITY //
    /////////////

    /**
     * Loads all data needed by the filter page (available to all graphs on the main Performance Reports page).
     * 
     * @return The data needed for the filter page.
     * @throws ParseException This is thrown if we fail during the parsing of the date range information.
     */
    public JsonObject loadDataForFilterPage() throws ParseException {
        ITreeAccessor treeAccess = (ITreeAccessor) db.getAndOpenDatabasePart(IDatabasePartType.TREE);
        IFilterAccessor filterAccess = (IFilterAccessor) db.getAndOpenDatabasePart(IDatabasePartType.FILTERS);

        try {
            JsonObject dataBundle = new JsonObject();
            JsonArray datesJson = new JsonArray();
            JsonArray filterDatesArr = new JsonArray();

            dataBundle.addProperty(JSON_PROPERTY_GRAPHNAME, filterGraphName);
            dataBundle.addProperty(JSON_PROPERTY_GRAPHPATH, filterGraphPath);

            OneLevelTree tree = treeAccess.getNodeAt(filterGraphPath);
            if(tree != null && !tree.isEmptyDataSet(graphSelectedMeasurable)) {
                JsonArray returnedDatesJson = tree.getDatesInRange(graphSelectedMeasurable, graphSelectedTimeScale, false, false, roundTo);
                for(int i = 0; i < returnedDatesJson.size(); i++) {
                    JsonObject dateJson = returnedDatesJson.get(i).getAsJsonObject();
                    String date = dateJson.entrySet().iterator().next().getKey(); // Assuming there's only one date String per JsonObject.
                    datesJson.add(date);
                }
                dataBundle.add(JSON_PROPERTY_DATES, datesJson);
            }

            Set<FilterData> filterDataSet = filterAccess.getFilterData();
            Iterator<FilterData> iter = filterDataSet.iterator();
            while(iter.hasNext()) {
                FilterData filterData = iter.next();
                if(filterData.getPath().equals(filterGraphPath)) {
                    for(String date : filterData.getFilteredDates()) {
                        filterDatesArr.add(date);
                    }
                    dataBundle.add(JSON_PROPERTY_FILTERDATES, filterDatesArr);
                    break;
                }
            }
            return dataBundle;
        } catch(Exception e) {
            throw e;
        }
    }

    /////////////////////
    // STAPLER LOADING //
    /////////////////////

    /**
     * When a Stapler parameter list is populated with data (usually following a page submit/refresh),
     * this method should be called in order to update the context's configuration settings based
     * on new parameter information.
     * 
     */
    public void updateMainReportSettingsFromNewStaplerRequest() {
        updateMainReportModeFromStaplerRequest();
        if(getMainReportMode() != MainReportMode.NONE) {    
            updateFilterSelectedGraphNameFromStaplerRequest();
            updateFilterSelectedGraphPathFromStaplerRequest();
        }

        updateMainReportActionFromStaplerRequest();
        if(getMainReportAction() != MainReportAction.NONE) {
            updateDataFiltersFromNewStaplerRequest();
            if(getMainReportAction() == MainReportAction.SAVE_AND_CLOSE_FILTER) {
                // If "Save and Close" was selected, we need to set the mode back to "None",
                // indicating a return to the main page.
                setMainReportMode(MainReportMode.NONE);
                setMainReportAction(MainReportAction.NONE);
            }
        }

        // Note:  We can use "measurable" as an indicator that a full parameter list was sent,
        // and therefore we know that main report mode/actions should be set back to their
        // default settings.  This is a bit kloogey, but we're limited by how Stapler requests
        // and HTML parameter lists are sent by Jenkins...
        if(StaplerRequestUtil.parameterValueExists(HtmlConstants.PARAM_MEASURABLE)) {
            setMainReportMode(MainReportMode.NONE);
            setMainReportAction(MainReportAction.NONE);
        }
    }

    /**
     * Update the mode based on any change sent via the Stapler request.
     */
    private void updateMainReportModeFromStaplerRequest() {
        String oldReportModeStr = MainReportMode.getStringVersion(mainReportMode);
        String newReportModeStr =
            StaplerRequestUtil.parseStringParameter(HtmlConstants.PARAM_REPORT_MODE, oldReportModeStr);
        if(StaplerRequestUtil.parameterValueExists(HtmlConstants.PARAM_REPORT_MODE) && !oldReportModeStr.equals(newReportModeStr)) {
            setMainReportMode(MainReportMode.getEnumVersion(newReportModeStr));
        } else if(!StaplerRequestUtil.parameterValueExists(HtmlConstants.PARAM_REPORT_MODE)) {
            setMainReportMode(MainReportMode.NONE);
        }
    }

    /**
     * Update the action based on any change sent via the Stapler request.
     */
    private void updateMainReportActionFromStaplerRequest() {
        String oldReportActionStr = MainReportAction.getStringVersion(mainReportAction);
        
        String newReportActionStr =
            StaplerRequestUtil.parseStringParameter(HtmlConstants.PARAM_REPORT_ACTION_SAVE_AND_CLOSE, "");
        if(StringUtils.isBlank(newReportActionStr)) {
            newReportActionStr =
                StaplerRequestUtil.parseStringParameter(HtmlConstants.PARAM_REPORT_ACTION_SAVE, "");
        }

        setMainReportAction(MainReportAction.NONE);
        if(StringUtils.isNotBlank(newReportActionStr) && !oldReportActionStr.equals(newReportActionStr)) {
            if(newReportActionStr.equals(HtmlConstants.PARAM_REPORT_ACTION_SAVE_DISPLAY)) {
                setMainReportAction(MainReportAction.SAVE_FILTER);
            } else if(newReportActionStr.equals(HtmlConstants.PARAM_REPORT_ACTION_SAVE_AND_CLOSE_DISPLAY)) {
                setMainReportAction(MainReportAction.SAVE_AND_CLOSE_FILTER);
            }
        }
    }

    /**
     * Update the selected graph name based on any change sent via the Stapler request.
     */
    private void updateFilterSelectedGraphNameFromStaplerRequest() {
        String oldSelectedGraphName = getSelectedGraphName();
        String newSelectedGraphName =
            StaplerRequestUtil.parseStringParameter(HtmlConstants.PARAM_REPORT_GRAPH_NAME, oldSelectedGraphName);
        if(StaplerRequestUtil.parameterValueExists(HtmlConstants.PARAM_REPORT_GRAPH_NAME) &&
                !oldSelectedGraphName.equals(newSelectedGraphName)) {
            setSelectedGraphName(newSelectedGraphName);
        } else if(!StaplerRequestUtil.parameterValueExists(HtmlConstants.PARAM_REPORT_GRAPH_NAME)) {
            setSelectedGraphName("");
        }
    }

    /**
     * Update the selected graph path based on any change sent via the Stapler request.
     */
    private void updateFilterSelectedGraphPathFromStaplerRequest() {
        String oldSelectedGraphPath = getSelectedGraphPath();
        String newSelectedGraphPath =
            StaplerRequestUtil.parseStringParameter(HtmlConstants.PARAM_REPORT_GRAPH_PATH, oldSelectedGraphPath);
        if(StaplerRequestUtil.parameterValueExists(HtmlConstants.PARAM_REPORT_GRAPH_PATH) &&
                !oldSelectedGraphPath.equals(newSelectedGraphPath)) {
            setSelectedGraphPath(newSelectedGraphPath);
        } else if(!StaplerRequestUtil.parameterValueExists(HtmlConstants.PARAM_REPORT_GRAPH_PATH)) {
            setSelectedGraphPath("");
        }
    }
    
    /**
     * Update data filters.
     */
    private void updateDataFiltersFromNewStaplerRequest() {
        IFilterAccessor filterAccess = (IFilterAccessor) db.getAndOpenDatabasePart(IDatabasePartType.FILTERS);

        String path_ = StaplerRequestUtil.parseStringParameter(HtmlConstants.PARAM_REPORT_GRAPH_PATH, "");
        if(StaplerRequestUtil.parameterValueExists(HtmlConstants.PARAM_REPORT_GRAPH_PATH) && StringUtils.isNotBlank(path_)) {
            List<String> filterDates =
                StaplerRequestUtil.findStringParameter(HtmlConstants.FILTER_FIELD_DATE_PREFIX + ".*");
            List<String> dates = new ArrayList<>();
            for(String filterDateStr : filterDates) {
                String[] filterDateComponents = filterDateStr.split(HtmlConstants.PARAM_MUSTACHIO);
                String date = filterDateComponents[1];
                dates.add(date);
            }
            FilterData newFilterData = new FilterData(path_);
            newFilterData.getFilteredDates().addAll(dates);

            Set<FilterData> filterData = filterAccess.getFilterData();
            if(filterData.contains(newFilterData)) {
                filterData.remove(newFilterData);
            }
            filterData.add(newFilterData);
            filterAccess.close();
        }
    }
}