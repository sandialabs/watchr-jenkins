/*******************************************************************************
* Watchr
* ------
* Copyright 2021 National Technology & Engineering Solutions of Sandia, LLC (NTESS).
* Under the terms of Contract DE-NA0003525 with NTESS, the U.S. Government retains
* certain rights in this software.
******************************************************************************/
package gov.sandia.sems.jenkinsutilities.views;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import gov.sandia.sems.jenkinsutilities.treeaccessor.OneLevelTree;

/**
 * A ConcreteView differs from a regular {@link View} in that it contains
 * fully-retrieved data structures for the datasets that make up the View.
 * 
 * @author Elliott Ridgway
 */
public class ConcreteView {

    ////////////
    // FIELDS //
    ////////////

    public final String name;
    public final Map<String, OneLevelTree> datasetMap;

    /////////////////
    // CONSTRUCTOR //
    /////////////////

    public ConcreteView(String name, Map<String, OneLevelTree> datasetMap) {
        this.name = name;
        this.datasetMap = new HashMap<>();
        this.datasetMap.putAll(datasetMap);
    }

    /////////////
    // GETTERS //
    /////////////

    public String getName() {
        return name;
    }

    public Map<String, OneLevelTree> getDatasetMap() {
        return Collections.unmodifiableMap(datasetMap);
    }
}