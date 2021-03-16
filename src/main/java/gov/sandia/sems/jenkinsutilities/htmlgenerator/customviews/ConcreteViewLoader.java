/*******************************************************************************
* Watchr
* ------
* Copyright 2021 National Technology & Engineering Solutions of Sandia, LLC (NTESS).
* Under the terms of Contract DE-NA0003525 with NTESS, the U.S. Government retains
* certain rights in this software.
******************************************************************************/
package gov.sandia.sems.jenkinsutilities.htmlgenerator.customviews;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;

import org.apache.commons.lang3.StringUtils;

import gov.sandia.sems.jenkinsutilities.db.IDatabaseAccessor;
import gov.sandia.sems.jenkinsutilities.db.IDatabasePartType;
import gov.sandia.sems.jenkinsutilities.treeaccessor.FilterData;
import gov.sandia.sems.jenkinsutilities.treeaccessor.IFilterAccessor;
import gov.sandia.sems.jenkinsutilities.treeaccessor.ITreeAccessor;
import gov.sandia.sems.jenkinsutilities.treeaccessor.NodeData;
import gov.sandia.sems.jenkinsutilities.treeaccessor.OneLevelTree;
import gov.sandia.sems.jenkinsutilities.utilities.DateUtil;
import gov.sandia.sems.jenkinsutilities.views.ConcreteView;
import gov.sandia.sems.jenkinsutilities.views.View;
import gov.sandia.sems.jenkinsutilities.views.ViewDataset;

/**
 * This class is responsible for converting lightweight View objects to heavyweight
 * ConcreteView objects - that is, it loads all of a view's associated data from the
 * database using an ITreeAccessor.
 * 
 * @author Elliott Ridgway
 */
public class ConcreteViewLoader {

    ////////////
    // FIELDS //
    ////////////

    private final IDatabaseAccessor db;

    private int timeScale;
    private List<View> views;
    private List<ConcreteView> concreteViews;

    private String measurable;
    private String oldestDate;
    private String newestDate;

    /////////////////
    // CONSTRUCTOR //
    /////////////////

    /**
     * The constructor.
     * @param db Every ConcreteViewLoader must have access to the database.
     */
    public ConcreteViewLoader(IDatabaseAccessor db) {
        this.db = db;
        this.views = new ArrayList<>();
        this.concreteViews = new ArrayList<>();
    }

    /////////////
    // GETTERS //
    /////////////

    public int getTimeScale() {
        return timeScale;
    }

    public String getMeasurable() {
        return measurable;
    }

    public String getOldestDate() {
        return oldestDate;
    }

    public String getNewestDate() {
        return newestDate;
    }

    public List<View> getViews() {
        return Collections.unmodifiableList(views);
    }

    public List<ConcreteView> getConcreteViews() {
        if(concreteViews.isEmpty()) {
            return loadViews();
        }
        return concreteViews;
    }

    /////////////
    // SETTERS //
    /////////////

    public ConcreteViewLoader setMeasurable(String measurable, String defaultMeasurable) {
        if(StringUtils.isBlank(measurable)) {
            this.measurable = defaultMeasurable;
        } else {
            this.measurable = measurable;
        }
        return this;
    }

    public ConcreteViewLoader setTimescale(int timeScale) {
        this.timeScale = timeScale;
        return this;
    }

    /**
     * Sets the cache of {@link View}s for the ConcreteViewLoader and sorts
     * them as a matter of course.
     * 
     * @param views The views to set on the ConcreteViewLoader.
     * @return The ConcreteViewLoader object.
     */
    public ConcreteViewLoader setViews(Collection<View> views) {
        this.views.clear();
        this.views.addAll(views);
        Collections.sort(this.views);
        return this;
    }

    //////////
    // LOAD //
    //////////

    /**
     * Loads the view data.  Essentially, this class takes the
     * cached {@link View} objects and instantiates a
     * corresponding {@link ConcreteView} class for each View.
     * @return The List of ConcreteViews.
     */
    private List<ConcreteView> loadViews() {
        ITreeAccessor treeAccessor = (ITreeAccessor) db.getAndOpenDatabasePart(IDatabasePartType.TREE);
        List<ConcreteView> loadedConcreteViews = new ArrayList<>();
        for(View view : views) {
            Map<String, OneLevelTree> innerMap = new HashMap<>();
            for(ViewDataset viewDataset : view.getViewDatasets()) {
                String treePath = viewDataset.getPath();
                OneLevelTree tree = treeAccessor.getNodeAt(treePath);
                innerMap.put(treePath, tree);
            }
            loadedConcreteViews.add(new ConcreteView(view.getName(), innerMap));
        }
        return loadedConcreteViews;
    }

    /**
     * Find the oldest and newest possible dates for a given {@link View} object.
     * This method takes into account the datasets contained on the View (by
     * instantiating a {@link ConcreteView} object and inspecting the {@link OneLevelTree}s)
     * and also the ConcreteViewLoader's configuration for chosen measurable and time scale.
     * 
     * At the end of this method, the date range bounded by the oldest and newest dates
     * is cached in the ConcreteViewLoader.
     * 
     * @param view The View to retrieve oldest and newest dates for.
     * @throws ParseException Thrown if the date parsing operation fails.
     */
    public void cacheOldestAndNewestDatesForView(View view) throws ParseException {
        IFilterAccessor filterAccessor = (IFilterAccessor) db.getAndOpenDatabasePart(IDatabasePartType.FILTERS);

        String oldestDateAcrossDatasets = "";
        String newestDateAcrossDatasets = "";
        List<ConcreteView> loadedConcreteViews = getConcreteViews();
        ConcreteView concreteView = findConcreteViewByName(loadedConcreteViews, view.getName());

        if(concreteView != null) {
            Map<String, OneLevelTree> trees = concreteView.getDatasetMap();
            for(ViewDataset viewDataset : view.getViewDatasets()) {
                OneLevelTree tree = trees.get(viewDataset.getPath());
                if(tree != null) {
                    SortedMap<String, NodeData> sortedDateTreeMap = tree.getNodes();
                    if(!tree.isEmptyDataSet(measurable)) {
                        List<String> sortedDateList = new ArrayList<>();
                        sortedDateList.addAll(sortedDateTreeMap.keySet());
                        
                        if(filterAccessor != null) {
                            FilterData filterData = filterAccessor.getFilterDataByPath(tree.getPath(), false);
                            if(filterData != null) {
                                sortedDateList.removeAll(filterData.getFilteredDates());
                            }
                        }

                        String newestDateCandidate = sortedDateTreeMap.lastKey();
                        String oldestDateCandidate = DateUtil.getPastDateFromNumberOfPoints(sortedDateList, timeScale);
                        boolean isNewestInRange =
                            DateUtil.isWithinRange(newestDateCandidate, oldestDateAcrossDatasets, newestDateAcrossDatasets);
                        boolean isOldestInRange =
                            DateUtil.isWithinRange(oldestDateCandidate, oldestDateAcrossDatasets, newestDateAcrossDatasets);
                        if(StringUtils.isBlank(newestDateAcrossDatasets) || !isNewestInRange) {
                            newestDateAcrossDatasets = newestDateCandidate;
                        }
                        if(StringUtils.isBlank(oldestDateAcrossDatasets) || !isOldestInRange) {
                            oldestDateAcrossDatasets = oldestDateCandidate;
                        }
                    }
                }
            }
        }

        this.oldestDate = oldestDateAcrossDatasets;
        this.newestDate = newestDateAcrossDatasets;
    }

    /////////////
    // PACKAGE //
    /////////////

    /**
     * Get all possible date {@link String}s representable on a {@link ConcreteView}.
     * 
     * @param concreteView The ConcreteView to inspect.
     * @return The List of all possible dates.  Though stored in a regular List, 1) no duplicate
     * dates will be present, and 2) the dates will be sorted from oldest to newest.
     * @throws ParseException Thrown if a date parse exception occurs.
     */
    /*package*/ List<String> getPossibleDates(ConcreteView concreteView) throws ParseException {
        IFilterAccessor filterAccessor = (IFilterAccessor) db.getAndOpenDatabasePart(IDatabasePartType.FILTERS);
        
        Set<String> possibleDates = new HashSet<>();
        for(String key : concreteView.getDatasetMap().keySet()) {
            OneLevelTree tree = concreteView.getDatasetMap().get(key);
            if(tree != null && !tree.isEmptyDataSet(measurable)) {
                SortedMap<String, NodeData> dateTreeMap = tree.getNodes();
                List<String> dateList = new ArrayList<>(dateTreeMap.keySet());

                if(filterAccessor != null) {
                    FilterData filterData = filterAccessor.getFilterDataByPath(tree.getPath(), false);
                    if(filterData != null) {
                        dateList.removeAll(filterData.getFilteredDates());
                    }
                }

                for(String date : dateList) {
                    String localDate = date.substring(0, 19);
                    if(DateUtil.isWithinRange(localDate, getOldestDate(), getNewestDate())) {
                        possibleDates.add(localDate);
                    }
                }
            }
        }

        List<String> sortedDates = new ArrayList<>(possibleDates);
        Collections.sort(sortedDates);
        return sortedDates;
    }

    /**
     * Given a List of {@link ConcreteView}s, find the first ConcreteView object with a matching
     * name.
     * @param concreteViews The List of ConcreteViews to search.
     * @param name The name to match on.
     * @return The ConcreteView if found, or null otherwise.
     */
    /*package*/ ConcreteView findConcreteViewByName(List<ConcreteView> concreteViews, String name) {
        for(ConcreteView concreteView : concreteViews) {
            if(concreteView.getName().equals(name)) {
                return concreteView;
            }
        }
        return null;
    }
}