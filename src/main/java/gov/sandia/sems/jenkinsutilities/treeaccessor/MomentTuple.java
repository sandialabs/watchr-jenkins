/*******************************************************************************
* Watchr
* ------
* Copyright 2021 National Technology & Engineering Solutions of Sandia, LLC (NTESS).
* Under the terms of Contract DE-NA0003525 with NTESS, the U.S. Government retains
* certain rights in this software.
******************************************************************************/
package gov.sandia.sems.jenkinsutilities.treeaccessor;

import com.google.gson.JsonObject;

import gov.sandia.sems.jenkinsutilities.utilities.NumUtil;

/**
 * A MomentTuple captures data at a single moment in time.
 * 
 * @author Elliott Ridgway
 */
public class MomentTuple {

    ////////////
    // FIELDS //
    ////////////
    
    private final String type;
    private final double value;
    private double average;
    private double std;
    
    /////////////////
    // CONSTRUCTOR //
    /////////////////
    
    public MomentTuple(String type, double value) {
        this(type, value, Double.NaN, Double.NaN);
    }
        
    public MomentTuple(String type, double value, double average, double std) {
        this.type = type;
        this.value = value;
        this.average = average;
        this.std = std;
    }
    

    /////////////
    // GETTERS //
    /////////////
    
    public String getType() {
        return type;
    }

    public double getValue() {
        return value;
    }

    public double getAverage() {
        return average;
    }

    public double getStd() {
        return std;
    }

    ////////////////////////
    // GETTERS (COMPUTED) //
    ////////////////////////
    
    public boolean hasValues() {
        return value > 0.0;
    }

    /////////////
    // SETTERS //
    /////////////

    public void setAverage(double average) {
        this.average = average;
    }

    public void setStd(double std) {
        this.std = std;
    }

    /////////////
    // UTILITY //
    /////////////

    public boolean isEmpty() {
        boolean allZeroes = true;
        allZeroes = allZeroes && (getValue() == 0   || getValue() == Double.NaN);
        allZeroes = allZeroes && (getAverage() == 0 || getAverage() == Double.NaN);
        allZeroes = allZeroes && (getStd() == 0     || getStd() == Double.NaN);
        return allZeroes;
    }
    
    //////////
    // JSON //
    //////////
    
    /**
     * Returns the data from this MomentTuple object in a JSON structure.
     * 
     * @param includeAvg If true, include this MomentTuple's average value.
     * @param includeStdDev If true, include this MomentTuple's standard deviation value.
     * @param stdDevOffset If true, offset the standard deviation line from the average line.
     * @param roundTo Number of decimal places to round the returned values to.
     * @return A JSON structure representing the data from this MomentTuple.
     */
    public JsonObject toJson(boolean includeAvg, boolean includeStdDev, boolean stdDevOffset, int roundTo) {       
        JsonObject json = new JsonObject();
        json.addProperty("type", type);
        json.addProperty("value", NumUtil.truncateTo(value, roundTo));
        
        if(includeAvg) {
            json.addProperty("avg", NumUtil.truncateTo(average, roundTo));
        }
        if(includeStdDev) {
            double stdDevValue = NumUtil.truncateTo(std, roundTo);
            if(stdDevOffset) {
                // The shape of the standard deviation line is correct when each data
                // point is offset individually from each corresponding average value.
                // For instance, the standard deviation for November 14th should be
                // offset from the average for November 14th.                
                json.addProperty("std", average + stdDevValue);
            } else {
                json.addProperty("std", stdDevValue);
            }
        }
        
        return json;
    }
    
    //////////////
    // OVERRIDE //
    //////////////
    
    @Override
    public String toString() {
        return toJson(true, true, false, 3).toString();
    }
}