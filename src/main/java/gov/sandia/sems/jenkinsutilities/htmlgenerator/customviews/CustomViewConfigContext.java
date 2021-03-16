/*******************************************************************************
* Watchr
* ------
* Copyright 2021 National Technology & Engineering Solutions of Sandia, LLC (NTESS).
* Under the terms of Contract DE-NA0003525 with NTESS, the U.S. Government retains
* certain rights in this software.
******************************************************************************/
package gov.sandia.sems.jenkinsutilities.htmlgenerator.customviews;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import org.apache.commons.lang3.StringUtils;

import gov.sandia.sems.jenkins.semsjppplugin.CommonConstants;
import gov.sandia.sems.jenkinsutilities.db.IDatabaseAccessor;
import gov.sandia.sems.jenkinsutilities.db.IDatabasePartType;
import gov.sandia.sems.jenkinsutilities.htmlgenerator.AbstractConfigContext;
import gov.sandia.sems.jenkinsutilities.htmlgenerator.HtmlConstants;
import gov.sandia.sems.jenkinsutilities.treeaccessor.ITreeAccessor;
import gov.sandia.sems.jenkinsutilities.treeaccessor.OneLevelTree;
import gov.sandia.sems.jenkinsutilities.utilities.StaplerRequestUtil;
import gov.sandia.sems.jenkinsutilities.utilities.StringUtil;
import gov.sandia.sems.jenkinsutilities.views.IViewAccessor;
import gov.sandia.sems.jenkinsutilities.views.View;
import gov.sandia.sems.jenkinsutilities.views.ViewDataset;

/**
 * A state model for the Performance Report Custom Views page.
 * The page can be displayed in one of several {@link CustomViewMode}s, and
 * those modes may have associated {@link CustomViewBuilderAction}s if the
 * user is adding/editing a custom view.  Any action the user takes on this
 * page that results in a state change is persisted to this class.
 * 
 * @author Elliott Ridgway
 */
public class CustomViewConfigContext extends AbstractConfigContext {

    ////////////
    // FIELDS //
    ////////////

    // Database access
    private final IDatabaseAccessor db;

    // State indicators
    private CustomViewMode customViewMode = CustomViewMode.NONE;
    private CustomViewBuilderAction viewBuilderAction = CustomViewBuilderAction.NONE;
    private CustomViewSearchType searchType = CustomViewSearchType.SEARCH_BY_NAME;
    private String rejectSaveReason = "";

    // "New" view properties (add/edit mode)
    private String newCustomViewName       = HtmlConstants.PARAM_DFLT_CUSTOM_VIEW_NAME;
    private String newCustomViewSearchTerm = HtmlConstants.PARAM_DFLT_CUSTOM_VIEW_SEARCH_TERM;
    private Set<String> newCustomViewSelectedDatasets = new HashSet<>();

    // Selected view (edit/delete mode)
    private String currentCustomViewName = "";
    private View currentView = null;
    
    // Temporary sets when the user selects/removes data
    private Set<String> datasetsSelectedOnLastAction = new HashSet<>(); // Datasets checked for moving from available to selected.
    private Set<String> datasetsRemovedOnLastAction = new HashSet<>();  // Datasets checked for being removed from selected.

    // Search result cache
    private List<OneLevelTree> viewEditorSearchResults = new ArrayList<>();
    private String lastCustomViewSearchTerm = "";
    private CustomViewSearchType lastSearchType = CustomViewSearchType.SEARCH_BY_NAME;

    /////////////////
    // CONSTRUCTOR //
    /////////////////

    /**
     * Constructor.  All CustomViewConfigContexts must have access to the two databases.
     * 
     * @param db The accessor for the main database.
     * @param defaultMeasurable The starting measurable for this context.
     */
    public CustomViewConfigContext(IDatabaseAccessor db, String defaultMeasurable) {
        super(defaultMeasurable);
        this.db = db;
    }

    /////////////
    // GETTERS //
    /////////////

    public CustomViewMode getCustomViewMode() {
        return customViewMode;
    }

    public CustomViewBuilderAction getViewBuilderAction() {
        return viewBuilderAction;
    }

    public CustomViewSearchType getCustomViewSearchType() {
        return searchType;
    }

    public String getNewCustomViewName() {
        return newCustomViewName;
    }

    public String getNewCustomViewSearchTerm() {
        return newCustomViewSearchTerm;
    }

    public Set<String> getNewCustomViewSelectedDatasets() {
        return newCustomViewSelectedDatasets;
    }

    public List<String> getDatasetsSelectedOnLastAction() {
        return new ArrayList<>(datasetsSelectedOnLastAction);
    }

    public List<String> getDatasetsRemovedOnLastAction() {
        return new ArrayList<>(datasetsRemovedOnLastAction);
    }

    public String getCurrentCustomViewName() {
        return currentCustomViewName;
    }

    public View getCurrentView() {
        return currentView;
    }

    public String getRejectSaveReason() {
        return rejectSaveReason;
    }

    public String getViewEditorLastSearchTerm() {
        return lastCustomViewSearchTerm;
    }

    public CustomViewSearchType getViewEditorLastSearchType() {
        return lastSearchType;
    }

    public List<OneLevelTree> getViewEditorSearchResults() {
        return viewEditorSearchResults;
    }

    /////////////
    // SETTERS //
    /////////////

    public void setCustomViewMode(CustomViewMode customViewMode) {
        this.customViewMode = customViewMode;
    }

    public void setViewBuilderAction(CustomViewBuilderAction viewBuilderAction) {
        this.viewBuilderAction = viewBuilderAction;
    }

    public void setSearchType(CustomViewSearchType searchType) {
        this.searchType = searchType;
    }

    public void setNewCustomViewName(String newCustomViewName_) {
        newCustomViewName = newCustomViewName_;
    }

    public void setNewCustomViewSearchTerm(String newCustomViewSearchTerm_) {
        newCustomViewSearchTerm = newCustomViewSearchTerm_;
    }

    public void setNewCustomViewSelectedDatasets(List<String> selectedDatasets) {
        this.newCustomViewSelectedDatasets.clear();
        this.newCustomViewSelectedDatasets.addAll(selectedDatasets);
    }

    public void setDatasetsSelectedOnLastAction(List<String> datasetsToSelect) {
        datasetsSelectedOnLastAction.clear();
        datasetsSelectedOnLastAction.addAll(datasetsToSelect);
    }

    public void setDatasetsRemovedOnLastAction(List<String> datasetsToRemove) {
        datasetsRemovedOnLastAction.clear();
        datasetsRemovedOnLastAction.addAll(datasetsToRemove);
    }

    public void setCurrentCustomViewName(String currentCustomViewName_) {
        currentCustomViewName = currentCustomViewName_;
    }

    public void setCurrentView(View currentView_) {
        currentView = currentView_;
    }

    public void clearAll() {
        setCustomViewMode(CustomViewMode.NONE);
        setViewBuilderAction(CustomViewBuilderAction.NONE);
        setSearchType(CustomViewSearchType.SEARCH_BY_NAME);

        setNewCustomViewName("");
        setNewCustomViewSearchTerm("");
        newCustomViewSelectedDatasets.clear();

        viewEditorSearchResults.clear();
        lastCustomViewSearchTerm = "";
        lastSearchType = CustomViewSearchType.SEARCH_BY_NAME;

        setCurrentCustomViewName("");
        setCurrentView(null);
    }

    public void setRejectSaveReason(String rejectSaveReason) {
        this.rejectSaveReason = rejectSaveReason;
    }

    public void setViewEditorLastSearchTerm(String lastCustomViewSearchTerm) {
        this.lastCustomViewSearchTerm = lastCustomViewSearchTerm;
    }

    public void setViewEditorLastSearchType(CustomViewSearchType lastCustomViewSearchType) {
        this.lastSearchType = lastCustomViewSearchType;
    }

    public void setViewEditorSearchResults(List<OneLevelTree> searchResults) {
        viewEditorSearchResults.clear();
        viewEditorSearchResults.addAll(searchResults);
    }

    //////////////
    // OVERRIDE //
    //////////////

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Context {\n");
        sb.append("\tcustomViewMode: ").append(customViewMode).append(",\n");
        sb.append("\tcustomViewBuilderAction: ").append(viewBuilderAction).append(",\n");
        sb.append("\tsearchType: ").append(searchType).append(",\n");
        sb.append("\trejectSaveReason: ").append(rejectSaveReason).append(",\n");
        sb.append("\tnewCustomViewName: ").append(newCustomViewName).append(",\n");
        sb.append("\tnewCustomViewSearchTerm: ").append(newCustomViewSearchTerm).append(",\n");
        sb.append("\tcurrentCustomViewName: ").append(currentCustomViewName).append(",\n");
        sb.append("\tcustomViewSelectedDatasets: ").append(newCustomViewSelectedDatasets).append(",\n");
        if(currentView != null) {
            sb.append("\tcurrentView: ").append(currentView.toString()).append("\n");
        } else {
            sb.append("\tcurrentView: none\n");
        }
        sb.append("}\n");
        return sb.toString();
    }

    /////////////////////
    // STAPLER LOADING //
    /////////////////////

    /**
     * Update the custom view mode based on any change sent via the Stapler request.
     */
    public void updateCustomViewModeFromStaplerRequest() {
        String oldCustomViewModeStr = CustomViewMode.getStringVersion(customViewMode);
        String newCustomViewModeStr =
            StaplerRequestUtil.parseStringParameter(HtmlConstants.PARAM_CUSTOM_VIEW_MODE,
            oldCustomViewModeStr);
        if(StaplerRequestUtil.parameterValueExists(HtmlConstants.PARAM_CUSTOM_VIEW_MODE) &&
                !oldCustomViewModeStr.equals(newCustomViewModeStr)) {
            setCustomViewMode(CustomViewMode.getEnumVersion(newCustomViewModeStr));
        } else if(!StaplerRequestUtil.parameterValueExists(HtmlConstants.PARAM_CUSTOM_VIEW_MODE)) {
            setCustomViewMode(CustomViewMode.NONE);
        }
    }

    /**
     * Update the custom view builder action based on any change sent via the Stapler request.
     */
    public void updateBuilderActionFromStaplerRequest() {
        String oldViewBuilderActionStr = CustomViewBuilderAction.getStringVersion(viewBuilderAction);
        
        String newViewBuilderActionStr =
            StaplerRequestUtil.parseStringParameter(HtmlConstants.CUSTOM_VIEW_BUILDER_ACTION_SEARCH, "");
        if(StringUtils.isBlank(newViewBuilderActionStr)) {
            newViewBuilderActionStr =
                StaplerRequestUtil.parseStringParameter(HtmlConstants.CUSTOM_VIEW_BUILDER_ACTION_MOVE_AVAILABLE, "");
        }
        if(StringUtils.isBlank(newViewBuilderActionStr)) {
            newViewBuilderActionStr =
                StaplerRequestUtil.parseStringParameter(HtmlConstants.CUSTOM_VIEW_BUILDER_ACTION_REMOVE_SELECTED, "");
        }
        if(StringUtils.isBlank(newViewBuilderActionStr)) {
            newViewBuilderActionStr =
                StaplerRequestUtil.parseStringParameter(HtmlConstants.CUSTOM_VIEW_BUILDER_ACTION_SAVE, "");
        }

        setViewBuilderAction(CustomViewBuilderAction.NONE);
        if(StringUtils.isNotBlank(newViewBuilderActionStr) && !oldViewBuilderActionStr.equals(newViewBuilderActionStr)) {
            if(newViewBuilderActionStr.equals(HtmlConstants.CUSTOM_VIEW_BUILDER_ACTION_DISPLAY_SEARCH)) {
                setViewBuilderAction(CustomViewBuilderAction.SEARCH);
            } else if(newViewBuilderActionStr.equals(HtmlConstants.CUSTOM_VIEW_BUILDER_ACTION_DISPLAY_MOVE_AVAILABLE)) {
                setViewBuilderAction(CustomViewBuilderAction.MOVE_AVAILABLE);
            } else if(newViewBuilderActionStr.equals(HtmlConstants.CUSTOM_VIEW_BUILDER_ACTION_DISPLAY_REMOVE_SELECTED)) {
                setViewBuilderAction(CustomViewBuilderAction.REMOVE_SELECTED);
            } else if(newViewBuilderActionStr.equals(HtmlConstants.CUSTOM_VIEW_BUILDER_ACTION_DISPLAY_SAVE)) {
                setViewBuilderAction(CustomViewBuilderAction.SAVE);
            }
        }
    }

    /**
     * Update the custom view search type based on any change sent via the Stapler request.
     */
    public void updateCustomViewSearchType() {
        String oldSearchTypeStr = CustomViewSearchType.getStringVersion(searchType);
        
        String newSearchTypeStr =
            StaplerRequestUtil.parseStringParameter(HtmlConstants.PARAM_CUSTOM_VIEW_SEARCH_STYLE, "");
        if(StringUtils.isBlank(newSearchTypeStr)) {
            newSearchTypeStr =
                StaplerRequestUtil.parseStringParameter(HtmlConstants.PARAM_CUSTOM_VIEW_SEARCH_STYLE, "");
        }

        setSearchType(CustomViewSearchType.SEARCH_BY_NAME); // By default, search by name
        if(StringUtils.isNotBlank(newSearchTypeStr) && !oldSearchTypeStr.equals(newSearchTypeStr)) {
            if(newSearchTypeStr.equals(HtmlConstants.CUSTOM_VIEW_SEARCH_BY_NAME_VALUE)) {
                setSearchType(CustomViewSearchType.SEARCH_BY_NAME);
            } else if(newSearchTypeStr.equals(HtmlConstants.CUSTOM_VIEW_SEARCH_BY_PATH_VALUE)) {
                setSearchType(CustomViewSearchType.SEARCH_BY_PATH);
            }
        }
    }

    /**
     * Update the new custom view name field, based on any change sent via the Stapler request.
     */
    public void updateCustomViewNameFromStaplerRequest() {
        String customViewName_ =
            StaplerRequestUtil.parseStringParameter(HtmlConstants.PARAM_CUSTOM_VIEW_FIELD_NAME, getNewCustomViewName());
        if(StaplerRequestUtil.parameterValueExists(HtmlConstants.PARAM_CUSTOM_VIEW_FIELD_NAME) &&
                !getNewCustomViewName().equals(customViewName_)) {
            setNewCustomViewName(customViewName_);
        }
    }

    /**
     * Update the new custom view search term field, based on any change sent via the Stapler request.
     */
    public void updateCustomViewSearchTermFromStaplerRequest() {
        String customViewSearchTerm_ =
            StaplerRequestUtil.parseStringParameter(HtmlConstants.PARAM_CUSTOM_VIEW_FIELD_SEARCH_TERM, getNewCustomViewSearchTerm());
        if(StaplerRequestUtil.parameterValueExists(HtmlConstants.PARAM_CUSTOM_VIEW_FIELD_SEARCH_TERM) &&
                !getNewCustomViewSearchTerm().equals(customViewSearchTerm_)) {
            setNewCustomViewSearchTerm(customViewSearchTerm_);
        }   
    }

    /**
     * Update the search results, based on a new search term sent via the Stapler request.  If the search term
     * is the same as the last one, the search operation is rejected, in order to improve performance.
     */
    public void updateSearchResultsFromStaplerRequest() {
        if(StringUtils.isNotBlank(newCustomViewSearchTerm) &&
                (!newCustomViewSearchTerm.equals(lastCustomViewSearchTerm)) || searchType != lastSearchType) {
            String augmentedSearchTerm = "";
            if(searchType == CustomViewSearchType.SEARCH_BY_NAME) {
                // This is a bit of an assumption on the part of the CustomViewConfigContext class.
                // Specific implementations of ITreeAccessor may in fact use different path-separation
                // characters (apart from \ and /), in which case the following negative lookahead
                // regex will not force the tree accessor implementation to perform a name-only search.
                augmentedSearchTerm = "(?!\\/\\\\)*(" + newCustomViewSearchTerm + ")(?!\\/\\\\)*";
            } else if(searchType == CustomViewSearchType.SEARCH_BY_PATH) {
                augmentedSearchTerm = ".*" + newCustomViewSearchTerm + ".*";
            }

            ITreeAccessor treeAccessor = (ITreeAccessor) db.getAndOpenDatabasePart(IDatabasePartType.TREE);
            List<OneLevelTree> searchResults = treeAccessor.searchForChildren("", augmentedSearchTerm, true, false);
            setViewEditorSearchResults(searchResults);
            setViewEditorLastSearchTerm(newCustomViewSearchTerm);
            setViewEditorLastSearchType(searchType);
        } else if(StringUtils.isBlank(newCustomViewSearchTerm)) {
            viewEditorSearchResults.clear();
        }
    }

    /**
     * Update the list of selected datasets, based on what is sent via the Stapler request.
     */
    public void updateListOfSelectedDatasetsFromStaplerRequest() {
        List<String> availableDatasetsToSelect =
            StaplerRequestUtil.findStringParameter(HtmlConstants.PARAM_CUSTOM_VIEW_AVAILABLE_PREFIX + ".*");
        List<String> selectedDatasets = new ArrayList<>();
        for(String availableDataset : availableDatasetsToSelect) {
            String datasetName = availableDataset.split(HtmlConstants.PARAM_MUSTACHIO)[1];
            selectedDatasets.add(datasetName);
        }
        
        setDatasetsSelectedOnLastAction(selectedDatasets);
        this.newCustomViewSelectedDatasets.addAll(getDatasetsSelectedOnLastAction());
    }

    /**
     * Update the list of removed datasets, based on what is sent via the Stapler request.
     */
    public void updateListOfRemovedDatasetsFromStaplerRequest() {
        List<String> selectedDatasetsToRemove =
            StaplerRequestUtil.findStringParameter(HtmlConstants.PARAM_CUSTOM_VIEW_SELECTED_PREFIX + ".*");
        List<String> removedDatasets = new ArrayList<>();
        for(String removedDataset : selectedDatasetsToRemove) {
            String datasetName = removedDataset.split(HtmlConstants.PARAM_MUSTACHIO)[1];
            removedDatasets.add(datasetName);
        }

        setDatasetsRemovedOnLastAction(removedDatasets);
        this.newCustomViewSelectedDatasets.removeAll(getDatasetsRemovedOnLastAction());
    }

    /**
     * Update the name for the currently-selected {@link View}, based on what is sent via the Stapler request.
     */
    private void updateCurrentCustomViewNameFromStaplerRequest() {
        String currentCustomViewName_ =
            StaplerRequestUtil.parseStringParameter(HtmlConstants.PARAM_CURRENT_CUSTOM_VIEW, getCurrentCustomViewName());
        boolean parameterExists = StaplerRequestUtil.parameterValueExists(HtmlConstants.PARAM_CURRENT_CUSTOM_VIEW);
        
        if(parameterExists && !getCurrentCustomViewName().equals(currentCustomViewName_)) {
            setCurrentCustomViewName(currentCustomViewName_);
        } else if(!parameterExists) {
            setCurrentCustomViewName("");
        }
    }

    /**
     * Update graph display settings, based on any new configuration sent via the Stapler request.
     */
    public void updateGraphDisplaySettingsFromStaplerRequest() {
        String measurable_ =
            StaplerRequestUtil.parseStringParameter(HtmlConstants.PARAM_MEASURABLE, getGraphSelectedMeasurable());
        int timeScale_ =
            StaplerRequestUtil.parseIntParameter(HtmlConstants.PARAM_TIME_SCALE, getGraphSelectedTimeScale());
        int graphWidth_ =
            StaplerRequestUtil.parseIntParameter(HtmlConstants.PARAM_GRAPH_WIDTH, getGraphWidth());
        int graphHeight_ =
            StaplerRequestUtil.parseIntParameter(HtmlConstants.PARAM_GRAPH_HEIGHT, getGraphHeight());
        int graphsPerRow_ =
            StaplerRequestUtil.parseIntParameter(HtmlConstants.PARAM_GRAPHS_PER_ROW, getGraphsPerRow());
        int roundTo_ =
            StaplerRequestUtil.parseIntParameter(HtmlConstants.PARAM_ROUND_TO, getRoundTo());

        if(StaplerRequestUtil.parameterValueExists(HtmlConstants.PARAM_MEASURABLE) && !getGraphSelectedMeasurable().equals(measurable_)) {
            setGraphSelectedMeasurable(measurable_);
        }

        if(StaplerRequestUtil.parameterValueExists(HtmlConstants.PARAM_TIME_SCALE) && getGraphSelectedTimeScale() != timeScale_) {
            setGraphSelectedTimeScale(timeScale_);
        }

        if(StaplerRequestUtil.parameterValueExists(HtmlConstants.PARAM_GRAPH_WIDTH) && getGraphWidth() != graphWidth_) {
            setGraphWidth(graphWidth_);
        }

        if(StaplerRequestUtil.parameterValueExists(HtmlConstants.PARAM_GRAPH_HEIGHT) && getGraphHeight() != graphHeight_) {
            setGraphHeight(graphHeight_);
        }

        if(StaplerRequestUtil.parameterValueExists(HtmlConstants.PARAM_GRAPHS_PER_ROW) && getGraphsPerRow() != graphsPerRow_) {
            setGraphsPerRow(graphsPerRow_);
        }

        if(StaplerRequestUtil.parameterValueExists(HtmlConstants.PARAM_ROUND_TO) && getRoundTo() != roundTo_) {
            setRoundTo(roundTo_);
        }
    }

    ///////////////////
    // STAPLER CHECK //
    ///////////////////

    /**
     * @return True if any data has been sent via the Stapler request that would necessitate
     * an update to this object's fields for the new/edited custom view.
     */
    public boolean isAnyDataPresentInStaplerRequest() {
        boolean isNamePresent = StaplerRequestUtil.parameterNameExists(HtmlConstants.PARAM_CUSTOM_VIEW_FIELD_NAME);
        boolean isSearchTermPresent = StaplerRequestUtil.parameterNameExists(HtmlConstants.PARAM_CUSTOM_VIEW_FIELD_SEARCH_TERM);
        List<String> availableDatasetsToSelect =
            StaplerRequestUtil.findStringParameter(HtmlConstants.PARAM_CUSTOM_VIEW_AVAILABLE_PREFIX + ".*");
        List<String> selectedDatasetsToRemove =
            StaplerRequestUtil.findStringParameter(HtmlConstants.PARAM_CUSTOM_VIEW_SELECTED_PREFIX + ".*");
        
        return isNamePresent || isSearchTermPresent || !availableDatasetsToSelect.isEmpty() || !selectedDatasetsToRemove.isEmpty();
    }

    /**
     * @return True if we have an already-created "current" view in the Stapler request.  This is
     * true only for EDIT and DELETE_ASK modes.
     */
    public boolean isCurrentViewInStaplerRequest() {
        if(customViewMode == CustomViewMode.EDIT || customViewMode == CustomViewMode.DELETE_ASK) {
            return StaplerRequestUtil.parameterValueExists(HtmlConstants.PARAM_CURRENT_CUSTOM_VIEW);
        }
        return false;
    }

    /////////////
    // UTILITY //
    /////////////

    /**
     * @return True if this CustomViewConfigContext is in a state where the view builder controls
     * should be added to the page.
     */
    public boolean shouldLoadViewBuilder() {
        return (customViewMode == CustomViewMode.ADD || customViewMode == CustomViewMode.EDIT) ||
               (viewBuilderAction == CustomViewBuilderAction.SAVE && StringUtils.isNotBlank(rejectSaveReason));
    }

    /**
     * Using the currently loaded {@link View}, this method will take all of its stored
     * {@link ViewDataset}s and produce a List of Strings representing the ViewDatasets.
     * View dataset type is accounted for by appending the type to each String.<br><br>For
     * example, a ViewDataset of type ViewDataset.DatasetType.AVERAGE named "MyViewDataset"
     * will recieve a String of "MyViewDataset - Average".
     */
    private void updateSelectedDatasetsFromEditedView() {
        View editedView = getCurrentView();
        if(editedView != null) {
            List<String> customViewSelectedDatasets = new ArrayList<>();
            for(ViewDataset vd : editedView.getViewDatasets()) {
                String vdFullName = vd.getPath() + CustomViewsHtmlGenerator.DATASET_TYPE_DELIMITER;
                if(vd.getType() == ViewDataset.DatasetType.DATA) {
                    vdFullName = vdFullName + CustomViewsHtmlGenerator.TYPE_SUFFIX_STRING_VALUE;
                } else if(vd.getType() == ViewDataset.DatasetType.AVERAGE) {
                    vdFullName = vdFullName + CustomViewsHtmlGenerator.TYPE_SUFFIX_STRING_AVG;
                } else if(vd.getType() == ViewDataset.DatasetType.STD_DEV) {
                    vdFullName = vdFullName + CustomViewsHtmlGenerator.TYPE_SUFFIX_STRING_STDDEV;
                }
                customViewSelectedDatasets.add(vdFullName);
            }
            setNewCustomViewSelectedDatasets(customViewSelectedDatasets);
        }
    }

    /**
     * Once you have properly updated this object's state
     * based on datasets that were moved or removed in the last user
     * operation, this method will clear both the {@code datasetsSelectedOnLastAction}
     * and {@code datasetsRemovedOnLastAction} sets.
     */
    public void clearDatasetsMovedOnLastAction() {
        datasetsSelectedOnLastAction.clear();
        datasetsRemovedOnLastAction.clear();
    }

    /**
     * If any data has been sent via Stapler request that requires us to update this object's
     * fields for the new/current view, then perform the necessary updates.<br><br>However, if no
     * data is present and we are in ADD mode, then this method also performs an update by
     * clearing everything related to the new/current view (because the user is approaching
     * the page in ADD mode with no new data for the view, we can assume they are coming to
     * the page for the first time and we need to clear out any potentially stale data).
     */
    public void updateIfAnyNewDataPresentInStaplerRequest() {
        if(isAnyDataPresentInStaplerRequest() && shouldLoadViewBuilder()) {
            updateCustomViewNameFromStaplerRequest();
            updateCustomViewSearchTermFromStaplerRequest();
            updateCustomViewSearchType();
            updateForViewBuilderAction();
        } else if(!isAnyDataPresentInStaplerRequest() && customViewMode == CustomViewMode.ADD) {
            // Are we approaching the custom view editor page without any data in the parameter list?
            // If so, clear all cached settings that could interfere with a blank Add page.
            setNewCustomViewName("");
            setNewCustomViewSearchTerm("");
            setSearchType(CustomViewSearchType.SEARCH_BY_NAME);
            viewEditorSearchResults.clear();
            newCustomViewSelectedDatasets.clear();
        }
    }

    /**
     * If a current {@link View} is present in the Stapler request, then update all of the
     * CustomViewConfigContext object's fields that come from data already in the
     * current View.
     */
    public void updateIfCurrentViewInStaplerRequest() {
        if(isCurrentViewInStaplerRequest()) {
            updateCurrentCustomView();
            if(customViewMode == CustomViewMode.EDIT) {
                setNewCustomViewName(getCurrentView().getName());
                setNewCustomViewSearchTerm("");
                setSearchType(CustomViewSearchType.SEARCH_BY_NAME);
                updateSelectedDatasetsFromEditedView();
            }
        }
    }

    /**
     * Perform the proper updates based on the current {@link CustomViewBuilderAction}.
     */
    private void updateForViewBuilderAction() {
        if(viewBuilderAction == CustomViewBuilderAction.SEARCH) {
            updateSearchResultsFromStaplerRequest();
        } else if(viewBuilderAction == CustomViewBuilderAction.MOVE_AVAILABLE) {
            updateListOfSelectedDatasetsFromStaplerRequest();
        } else if(viewBuilderAction == CustomViewBuilderAction.REMOVE_SELECTED) {
            updateListOfRemovedDatasetsFromStaplerRequest();
        }
    }

    /**
     * Given a current custom view name from the Stapler request, update
     * this object's cached current {@link View} object by loading the View
     * from the database, using the new name from the Stapler request.
     */
    private void updateCurrentCustomView() {
        updateCurrentCustomViewNameFromStaplerRequest();
        String currentCustomViewName = getCurrentCustomViewName();

        IViewAccessor viewAccessor = (IViewAccessor) db.getAndOpenDatabasePart(IDatabasePartType.VIEWS);
        setCurrentView(viewAccessor.findView(currentCustomViewName));
    }

    /**
     * Determine if there is a reason to reject the save request.
     */
    public void detectRejectReason() {
        String rejectSaveReason_ = "";
        String proposedViewName = getNewCustomViewName();

        IViewAccessor viewAccessor = (IViewAccessor) db.getAndOpenDatabasePart(IDatabasePartType.VIEWS);
        View existingViewWithSameName = viewAccessor.findView(proposedViewName);

        if(StringUtils.isBlank(proposedViewName)) {
            rejectSaveReason_ = HtmlConstants.CUSTOM_VIEW_REJECT_REASON_EMPTY_NAME;
        } else if(existingViewWithSameName != null && !existingViewWithSameName.equals(currentView)) {
            rejectSaveReason_ = HtmlConstants.CUSTOM_VIEW_REJECT_REASON_DUP_NAME;
        } else if(StringUtil.hasIllegalCharacters(proposedViewName)) {
            rejectSaveReason_ = HtmlConstants.CUSTOM_VIEW_REJECT_REASON_BAD_NAME;
        } else if(newCustomViewSelectedDatasets.isEmpty()) {
            rejectSaveReason_ = HtmlConstants.CUSTOM_VIEW_REJECT_REASON_NO_DATASETS;
        }
        setRejectSaveReason(rejectSaveReason_);
    }

    ///////////////
    // DATA LOAD //
    ///////////////

    /**
     * Load the data bundle required for the view builder.  This method also performs
     * some post-load cleanup of this object's state, in order to ensure that there is
     * not stale data in the object for subsequent loads of the view builder.
     * 
     * @return A {@link JsonObject} containing all the data needed by the view builder.
     */
    public JsonObject loadDataForViewBuilderPage() {
        JsonArray availableDatasetsJson = new JsonArray();
        JsonArray selectedDatasetsJson  = new JsonArray();
        JsonObject dataJson = new JsonObject();

        // Load the reject save reason.
        if(StringUtils.isNotBlank(rejectSaveReason)) {
            dataJson.addProperty(HtmlConstants.PARAM_CUSTOM_VIEW_REJECT_REASON, rejectSaveReason);
        }
        // Load the name.
        dataJson.addProperty(HtmlConstants.PARAM_CUSTOM_VIEW_FIELD_NAME, newCustomViewName);

        // Load the search term.
        dataJson.addProperty(HtmlConstants.PARAM_CUSTOM_VIEW_FIELD_SEARCH_TERM, newCustomViewSearchTerm);
        
        // Load the search type.
        if(searchType == CustomViewSearchType.SEARCH_BY_PATH) {
            dataJson.addProperty(HtmlConstants.PARAM_CUSTOM_VIEW_SEARCH_STYLE, HtmlConstants.CUSTOM_VIEW_SEARCH_BY_PATH_VALUE);
        } else {
            dataJson.addProperty(HtmlConstants.PARAM_CUSTOM_VIEW_SEARCH_STYLE, HtmlConstants.CUSTOM_VIEW_SEARCH_BY_NAME_VALUE);
        }

        // Load the custom view mode.
        dataJson.addProperty(HtmlConstants.PARAM_CUSTOM_VIEW_MODE, CustomViewMode.getStringVersion(customViewMode));

        // Add any previously selected datasets to the JSON array.
        for(String selectedDataset : newCustomViewSelectedDatasets) {
            selectedDatasetsJson.add(selectedDataset);
        }

        // Load the search results into the set of available datasets.
        List<OneLevelTree> viewEditorSearchResults = getViewEditorSearchResults();
        for(int i = 0; i < viewEditorSearchResults.size(); i++) {
            String baseString = viewEditorSearchResults.get(i).getPath() + CustomViewsHtmlGenerator.DATASET_TYPE_DELIMITER;
            String valueString  = baseString + CustomViewsHtmlGenerator.TYPE_SUFFIX_STRING_VALUE;
            String avgString    = baseString + CustomViewsHtmlGenerator.TYPE_SUFFIX_STRING_AVG;
            String stdDevString = baseString + CustomViewsHtmlGenerator.TYPE_SUFFIX_STRING_STDDEV;

            if(!newCustomViewSelectedDatasets.contains(valueString)) {
                availableDatasetsJson.add(valueString);
            }
            if(!newCustomViewSelectedDatasets.contains(avgString)) {
                availableDatasetsJson.add(avgString);
            }
            if(!newCustomViewSelectedDatasets.contains(stdDevString)) {
                availableDatasetsJson.add(stdDevString);
            }
        }
        dataJson.add(CommonConstants.AVAILABLE_DATASETS, availableDatasetsJson);

        // Load the selected datasets.
        dataJson.add(CommonConstants.SELECTED_DATASETS, selectedDatasetsJson);

        // Finally, forget about datasets that were selected/removed on the last action,
        // since we have successfully persisted the new state of things.
        clearDatasetsMovedOnLastAction();

        // Clear the view builder action.
        setViewBuilderAction(CustomViewBuilderAction.NONE);

        // Clear the reject reason, since we also don't need it anymore.
        setRejectSaveReason("");
        
        return dataJson;
    }
}