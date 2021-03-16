/*******************************************************************************
* Watchr
* ------
* Copyright 2021 National Technology & Engineering Solutions of Sandia, LLC (NTESS).
* Under the terms of Contract DE-NA0003525 with NTESS, the U.S. Government retains
* certain rights in this software.
******************************************************************************/
package gov.sandia.sems.jenkinsutilities.treeaccessor;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

/**
 * The NodeData class represents a single node in the tree of information.
 * The class is responsible for maintaining temporal data of many possible types
 * (achieved with the momentTuples Set) and a Map of metadata information for
 * this node.
 * 
 * @author Elliott Ridgway, Lawrence Allen
 */
public class NodeData {

    ////////////
    // FIELDS //
    ////////////

    private final Set<MomentTuple> momentTuples;
    private final Map<String, String> metadata;
    private final String units;

    private OneLevelTree parentTree;
    
    /////////////////
    // CONSTRUCTOR //
    /////////////////

    public NodeData(String units, Set<MomentTuple> momentTuples, Map<String, String> metadata) {
        this.units = units;
        this.momentTuples = new LinkedHashSet<>();
        this.momentTuples.addAll(momentTuples);
        
        this.metadata = new HashMap<>();
        this.metadata.putAll(metadata);
    }

    /////////////
    // GETTERS //
    /////////////
    
    public String getUnits() {
        return units;
    }
    
    public Set<MomentTuple> getMomentTuples() {
        return Collections.unmodifiableSet(momentTuples);
    }
    
    public Map<String, String> getMetadata() {
        return Collections.unmodifiableMap(metadata);
    }

    public OneLevelTree getParentTree() {
        return parentTree;
    }
    
    ////////////////////////
    // GETTERS (COMPUTED) //
    ////////////////////////
    
    public MomentTuple getMomentTuple(String dataType) {
        for(MomentTuple momentTuple : momentTuples) {
            if(momentTuple.getType().equals(dataType)) {
                return momentTuple;
            }
        }
        return null;
    }

    public MomentTuple getMomentTupleByMeasurable(String measurable) {
        for(MomentTuple momentTuple : momentTuples) {
            if(momentTuple.getType().equals(measurable)) {
                return momentTuple;
            }
        }
        return null;
    }

    /////////////
    // SETTERS //
    /////////////

    public void setParentTree(OneLevelTree parentTree) {
        this.parentTree = parentTree;
    }
    
    //////////
    // JSON //
    //////////
    
    /**
     * 
     * Converts a single MomentTuple measurable from a NodeData object into a JSON
     * structure.
     * 
     * @param date The string date of the MomentTuple to use.
     * @param measurable The measurable within a given MomentTuple to use.
     * @param includeAvg If true, return the average value in the JSON structure.
     * @param includeStdDev If true, return the average value in the JSON structure.
     * @param stdDevOffset If true, offset the standard deviation line from the average line.
     * @param roundTo The number of decimal places to round values to.
     * 
     * @return A JsonObject containing all the requested data from this NodeData object.
     */
    public JsonObject toJson(
            String date, String measurable, boolean includeAvg, boolean includeStdDev, boolean stdDevOffset, int roundTo) {
        JsonObject json = new JsonObject();
        JsonObject dataJson = new JsonObject();
        
        // Add moment tuple
        MomentTuple momentTuple = getMomentTupleByMeasurable(measurable);
        if(momentTuple != null) {
            dataJson.add("momentTuple", momentTuple.toJson(includeAvg, includeStdDev, stdDevOffset, roundTo));
        }

        // Add associated metadata
        JsonObject metadataJson = new JsonObject();
        for(Entry<String,String> entry : metadata.entrySet()) {
            metadataJson.addProperty(entry.getKey(), entry.getValue());
        }
        dataJson.add("metadata", metadataJson);

        json.add(date, dataJson);
        return json;
    }

    ////////////////
    // OVERRIDDEN //
    ////////////////

    @Override
    public String toString() {
        JsonObject json = new JsonObject();
        json.addProperty("units", units);
        
        JsonArray momentTuplesJson = new JsonArray();
        for(MomentTuple momentTuple : momentTuples) {
            momentTuplesJson.add(momentTuple.toJson(true, true, false, 3));
        }
        json.add("momentTuples", momentTuplesJson);
        
        JsonObject metadataJson = new JsonObject();
        for(Entry<String,String> entry : metadata.entrySet()) {
            metadataJson.addProperty(entry.getKey(), entry.getValue());
        }
        json.add("metadata", metadataJson);
        
        return json.toString();
    }
}