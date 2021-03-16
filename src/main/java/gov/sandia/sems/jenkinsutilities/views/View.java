/*******************************************************************************
* Watchr
* ------
* Copyright 2021 National Technology & Engineering Solutions of Sandia, LLC (NTESS).
* Under the terms of Contract DE-NA0003525 with NTESS, the U.S. Government retains
* certain rights in this software.
******************************************************************************/
package gov.sandia.sems.jenkinsutilities.views;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * This class represents a custom, user-defined grouping
 * of graph information.
 * 
 * @author Elliott Ridgway
 */
public class View implements Comparable<View> {

    ////////////
    // FIELDS //
    ////////////

    private final UUID uuid;
    private final String name;
    private final List<ViewDataset> viewDatasets;

    /////////////////
    // CONSTRUCTOR //
    /////////////////

    public View(String name, List<ViewDataset> viewDatasets) {
        this.uuid = UUID.randomUUID();
        this.name = name;
        this.viewDatasets = new ArrayList<>();
        this.viewDatasets.addAll(viewDatasets);
    }

    /////////////
    // GETTERS //
    /////////////

    public UUID getUUID() {
        return uuid;
    }

    public String getName() {
        return name;
    }

    public List<ViewDataset> getViewDatasets() {
        return Collections.unmodifiableList(viewDatasets);
    }

    //////////////
    // OVERRIDE //
    //////////////

    @Override
    public boolean equals(Object other) {
        if(other instanceof View) {
            View otherView = (View) other;
            boolean equals = true;
            equals = equals && otherView.getUUID().equals(getUUID());
            return equals;
        }
        return false;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("View {").append("\n");
        sb.append("UUID: ").append(uuid).append(",\n");
        sb.append("name: ").append(name).append(",\n");
        sb.append("viewDatasets: ").append(viewDatasets).append("\n");
        sb.append("}\n");
        return sb.toString();
    }

    @Override
    public int compareTo(View other) {
        return getName().compareTo(other.getName());
    }

    /////////////
    // UTILITY //
    /////////////

    public boolean effectiveEquals(Object other) {
        if(other instanceof View) {
            View otherView = (View) other;
            boolean equals = true;
            equals = equals && otherView.getName().equals(getName());
            equals = equals && otherView.getViewDatasets().equals(getViewDatasets());
            return equals;
        }
        return false;
    }
}