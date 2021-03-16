/*******************************************************************************
* Watchr
* ------
* Copyright 2021 National Technology & Engineering Solutions of Sandia, LLC (NTESS).
* Under the terms of Contract DE-NA0003525 with NTESS, the U.S. Government retains
* certain rights in this software.
******************************************************************************/
package gov.sandia.sems.jenkinsutilities.views;

import gov.sandia.sems.jenkinsutilities.treeaccessor.ITreeAccessor;

/**
 * This class represents a single dataset that is from the
 * main {@link ITreeAccessor} database.  Here, it is part of a custom,
 * user-defined {@link View}.
 * 
 * @author Elliott Ridgway
 */
public class ViewDataset implements Comparable<ViewDataset> {

    ////////////
    // FIELDS //
    ////////////

    public enum DatasetType {
        DATA, AVERAGE, STD_DEV;
    }

    public final String path;
    public final DatasetType type;

    /////////////////
    // CONSTRUCTOR //
    /////////////////

    /**
     * @param path The path to the dataset in the associated {@link ITreeAccessor}
     * managed by the parent {@link IViewAccessor}.
     * @param type Indicates whether the target data is regular data, average data,
     * or standard deviation data.
     */
    public ViewDataset(String path, DatasetType type) {
        this.path = path;
        this.type = type;
    }

    /////////////
    // GETTERS //
    /////////////

    public String getPath() {
        return path;
    }

    public DatasetType getType() {
        return type;
    }

    //////////////
    // OVERRIDE //
    //////////////

    @Override
    public boolean equals(Object other) {
        if(other instanceof ViewDataset) {
            ViewDataset otherViewDataset = (ViewDataset) other;
            boolean equals = true;
            equals = equals && otherViewDataset.getPath().equals(getPath());
            equals = equals && otherViewDataset.getType() == getType();
            return equals;
        }
        return false;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("ViewDataset {").append("\n");
        sb.append("path: ").append(path).append(",\n");
        sb.append("type: ").append(type).append(",\n");
        sb.append("}\n");
        return sb.toString();
    }

    @Override
    public int compareTo(ViewDataset otherViewDataset) {
        int compare = getPath().compareTo(otherViewDataset.getPath());
        if(compare == 0) {
            compare = getType().compareTo(otherViewDataset.getType());
        }
        return compare;
    }
}