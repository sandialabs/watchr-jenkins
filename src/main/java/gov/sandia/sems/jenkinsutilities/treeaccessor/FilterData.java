/*******************************************************************************
* Watchr
* ------
* Copyright 2021 National Technology & Engineering Solutions of Sandia, LLC (NTESS).
* Under the terms of Contract DE-NA0003525 with NTESS, the U.S. Government retains
* certain rights in this software.
******************************************************************************/
package gov.sandia.sems.jenkinsutilities.treeaccessor;

import java.util.ArrayList;
import java.util.List;

/**
 * Watchr has the ability to apply filters to selected dates that exist in
 * {@link OneLevelTree}-based datasets.  This way, "bad" data can be hidden
 * from view without needing to delete any data.<br><br>
 * 
 * A FilterData object provides a path to a dataset, and a List of String dates
 * located at that dataset that need to be filtered.  This is an extremely
 * lightweight object, and it is up to surrounding classes to decide how to
 * use this information.
 * 
 * @author Elliott Ridgway
 */
public class FilterData {

    ////////////
    // FIELDS //
    ////////////

    private final String path;
    private final List<String> filteredDates;

    /////////////////
    // CONSTRUCTOR //
    /////////////////

    public FilterData(String path) {
        this.path = path;
        this.filteredDates = new ArrayList<>();
    }

    /////////////
    // GETTERS //
    /////////////

    public String getPath() {
        return path;
    }

    public List<String> getFilteredDates() {
        return filteredDates;
    }

    ////////////
    // EQUALS //
    ////////////

    @Override
    public boolean equals(Object other) {
		if(other == null) {
			return false;
		} else if(other == this) {
			return true;
		} else if(getClass() != other.getClass()) {
			return false;
		} else {
			boolean equals = true;
            FilterData otherFilterData = (FilterData) other;
            equals = equals && otherFilterData.getPath().equals(getPath());
			return equals;
		}
    }

    @Override
    public int hashCode() {
        return path.hashCode();
    }
}