/*******************************************************************************
* Watchr
* ------
* Copyright 2021 National Technology & Engineering Solutions of Sandia, LLC (NTESS).
* Under the terms of Contract DE-NA0003525 with NTESS, the U.S. Government retains
* certain rights in this software.
******************************************************************************/
package gov.sandia.sems.jenkinsutilities.treeaccessor;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.Map.Entry;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.thoughtworks.xstream.annotations.XStreamOmitField;

import gov.sandia.sems.jenkinsutilities.utilities.DateUtil;

/**
 * A OneLevelTree object contains all data stored at a single node in the
 * overall hierarchical data structure.
 * 
 * @author Elliott Ridgway, Lawrence Allen
 */
public class OneLevelTree {
    
    ////////////
    // FIELDS //
    ////////////
    
    private final String name;
    private final String path;
    private final String category;

    /**
     * The outer map has keys that represent locations below this level of the
     * hierarchical data structure.
     * 
     * The inner map has keys that represent points in time, with values that
     * represent the data collected at that point in time.
     * 
     * This field is provided for legacy support for database systems that were built on Watchr
     * 2.3 and older.  This field should not be used by any methods apart from
     * moveOldDataToNewTreeMap(), which transfers data in this map to the nodes2 field
     * at the first opportunity.
     * 
     * @deprecated since 2.3
     */
    @Deprecated
    @XStreamOmitField
    private final Map<String, Map<String, NodeData>> nodes;

    // The map has keys that represent points in time, with values that
    // represent the data collected at that point in time.
    private TreeMap<String, NodeData> nodes2;
    
    /////////////////
    // CONSTRUCTOR //
    /////////////////

    /**
     * 
     * @param name The name of the tree.
     * @param path The path to this tree. (Should be relative to the root of the tree accessor).
     * @param category The category that the tree belongs to (see {@code Keywords}).
     */
    public OneLevelTree(String name, String path, String category) {
        this.name = name;
        this.path = path;
        this.category = category;
        this.nodes = new HashMap<>();
        this.nodes2 = new TreeMap<>();
    }

    /////////////
    // GETTERS //
    /////////////
    
    public String getName() {
        return name;
    }
        
    public String getPath() {
        return path;
    }
    
    public String getCategory() {
        return category;
    }
    
    public SortedMap<String, NodeData> getNodes() {
        if(nodes2 == null) {
            nodes2 = new TreeMap<>();
        }

        if(nodes2.isEmpty() && !nodes.isEmpty()) {
            moveOldDataToNewTreeMap();
        }
        return nodes2;
    }
    
    /////////////
    // UTILITY //
    /////////////

    private void moveOldDataToNewTreeMap() {
        String mainKey = nodes.keySet().iterator().next();
        nodes2.putAll(nodes.get(mainKey));
    }
    
    /**
     * Test if this tree is empty (empty meaning that all data are zeroes)
     * for a specific type of {@link MomentTuple} data.
     * 
     * @param type The {@link MomentTuple} type to look at.
     * @return Whether the map is empty.
     */
    public final boolean isEmptyDataSet(String type) {
        boolean allZeroes = true;  
        for(String date : getNodes().keySet()) {
            NodeData nodeData = getNodes().get(date);
            if(nodeData != null) {
                MomentTuple momentTuple = nodeData.getMomentTuple(type);
                allZeroes = momentTuple == null || momentTuple.isEmpty();
                if(!allZeroes) {
                    break;
                }
            }
        }
        return allZeroes;
    }

    /**
     * Test if this tree is empty (empty meaning that all data are zeroes)
     * for a specific type of {@link MomentTuple} data.
     * 
     * @param type The {@link MomentTuple} type to look at.
     * @return Whether the map is empty.
     * @throws ParseException Thrown if the date range check fails while parsing the date strings.
     */
    public final boolean isEmptyDataSetInDateRange(String type, String minDate, String maxDate) throws ParseException {
        boolean allZeroes = true;  
        for(String date : getNodes().keySet()) {
            if(DateUtil.isWithinRange(date, minDate, maxDate)) {
                NodeData nodeData = getNodes().get(date);
                if(nodeData != null) {
                    MomentTuple momentTuple = nodeData.getMomentTuple(type);
                    allZeroes = momentTuple == null || momentTuple.isEmpty();
                    if(!allZeroes) {
                        break;
                    }
                }
            }
        }
        return allZeroes;
    }

    /**
     * Tests if this OneLevelTree is empty (empty either meaning that the data structure is
     * literally empty, or all data stored within is all zeroes).
     * 
     * @return Whether the OneLevelTree is empty.
     */
    public boolean isEmpty() {
        if(getNodes().isEmpty()) {
            return true;
        } else {
            boolean allZeroes = true;

            for(String date : getNodes().keySet()) {
                NodeData nodeData = getNodes().get(date);
                if(nodeData != null) {
                    for(MomentTuple momentTuple : nodeData.getMomentTuples()) {
                        allZeroes = momentTuple == null || momentTuple.isEmpty(); 
                        if(!allZeroes) {
                            break;
                        }
                    }
                }
                if(!allZeroes) {
                    break;
                }
            }
            return allZeroes;
        }
    }

    public final boolean dataIsFailure(String type, boolean avgFailIfGreater, boolean stdDevFailIfGreater) {
        return dataIsFailure(type, avgFailIfGreater, stdDevFailIfGreater, new ArrayList<>());
    }
    
    /**
     * Whether or not this tree should be displayed as being
     * in a "failure" state (based on previous configuration settings).
     * 
     * @param type The specific type of {@link MomentTuple} to investigate.
     * @param avgFailIfGreater Whether the data should fail if the most recent
     * data has risen above the average.
     * @param stdDevFailIfGreater Whether the data should fail if the most recent
     * data has risen above the offset standard deviation.
     * @param datesToIgnore A List of String dates to ignore for the consideration
     * of whether or not the graph is in a failure state.
     * @return Whether or not the data is in a failure state.
     */
    public final boolean dataIsFailure(
            String type, boolean avgFailIfGreater, boolean stdDevFailIfGreater, List<String> datesToIgnore) {
        boolean fail = false;
        if(!isEmptyDataSet(type)) {
            List<String> orderedDates = new ArrayList<>(getNodes().keySet());
            int index = orderedDates.size() - 1;
            String latestDate = orderedDates.get(index);
            while(datesToIgnore.contains(latestDate) && index >= 0) {
                latestDate = orderedDates.get(index);
                index --;
            }
            if(index < 0) {
                return false; // No dates are visible, so there's nothing to fail, so we have to return false.
            }

            NodeData nodeData = getNodes().get(latestDate);
            if(nodeData != null) {
                MomentTuple momentTuple = nodeData.getMomentTuple(type);
                if(momentTuple != null) {
                    if(avgFailIfGreater) {
                        fail = momentTuple.getValue() > momentTuple.getAverage();
                    }
                    if(stdDevFailIfGreater) {
                        double offsetStdDev = momentTuple.getAverage() + momentTuple.getStd();
                        fail = fail || (momentTuple.getValue() > offsetStdDev);
                    }
                }
            }
            return fail;
        }
        return false;
    }

    /**
     * Collects date information in a given range, and returns it as a {@link JsonArray} object.
     * 
     * @param measurable The measurable for which to look for data.
     * @param timeScale The number of data points in the past, starting from the latest date in the dataset.
     * @param enableAvg If true, include average information in the JsonArray object.
     * @param enableStdDev If true, include standard deviation information in the JsonArray object.
     * @param roundTo The number of digits of precision to round to.
     * @return The JsonArray containing date range information.
     * @throws ParseException Thrown if date parsing fails.
     */
    public JsonArray getDatesInRange(
            String measurable, int timeScale, boolean enableAvg, boolean enableStdDev, int roundTo) throws ParseException {
        return getDatesInRange(measurable, timeScale, enableAvg, enableStdDev, roundTo, new ArrayList<>());
    }

    /**
     * Collects date information in a given range, and returns it as a {@link JsonArray} object.
     * 
     * @param measurable The measurable for which to look for data.
     * @param timeScale The number of data points in the past, starting from the latest date in the dataset.
     * @param enableAvg If true, include average information in the JsonArray object.
     * @param enableStdDev If true, include standard deviation information in the JsonArray object.
     * @param roundTo The number of digits of precision to round to.
     * @param filterList A List of String dates to ignore when returning dates in the range.
     * @return The JsonArray containing date range information.
     * @throws ParseException Thrown if date parsing fails.
     */
    public JsonArray getDatesInRange(
            String measurable, int timeScale, boolean enableAvg, boolean enableStdDev,
            int roundTo, List<String> filterList) throws ParseException {

        JsonArray datesJson = new JsonArray();
        
        if(!isEmptyDataSet(measurable)) {
            SortedMap<String, NodeData> nodeDataMap = getNodes();

            List<String> sortedDateList = new ArrayList<>();
            sortedDateList.addAll(nodeDataMap.keySet());

            String latestDate = nodeDataMap.lastKey();
            String oldestDate = DateUtil.getPastDateFromNumberOfPoints(sortedDateList, timeScale);

            for(Entry<String, NodeData> entry : nodeDataMap.entrySet()) {
                String date = entry.getKey();
                NodeData dateNode = entry.getValue();

                if(!filterList.contains(date)) {
                    String localDate = date.substring(0, 19);
                    if(DateUtil.isWithinRange(localDate, oldestDate, latestDate)){        
                        JsonObject dateJson = dateNode.toJson(date, measurable, enableAvg, enableStdDev, true, roundTo);
                        datesJson.add(dateJson);
                    }
                }
            }
        }
        return datesJson;
    }
    
    /**
     * Tests a List of OneLevelTree objects to see if any of them belong to
     * a specific category.
     * 
     * @param trees The List of OneLevelTree objects.
     * @param category The category to test for.
     * @return Whether any of the OneLevelTree objects belong to a category.
     */
    public static final boolean hasCategory(List<OneLevelTree> trees, String category) {
        for(OneLevelTree tree : trees) {
            if(tree.getCategory().equals(category)) {
                return true;
            }
        }
        return false;
    }

    //////////////
    // OVERRIDE //
    //////////////
    
    @Override
    public String toString() {
        JsonObject json = new JsonObject();
        json.addProperty("path", path);
        json.addProperty("category", category);
        
        JsonObject nodesJson = new JsonObject();
        for(String date : getNodes().keySet()) {
            NodeData nodeData = getNodes().get(date);
            nodesJson.addProperty(date, nodeData.toString());
        }
        json.add("nodes", nodesJson);
        
        return json.toString();
    }
}