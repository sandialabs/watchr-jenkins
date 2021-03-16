/*******************************************************************************
* Watchr
* ------
* Copyright 2021 National Technology & Engineering Solutions of Sandia, LLC (NTESS).
* Under the terms of Contract DE-NA0003525 with NTESS, the U.S. Government retains
* certain rights in this software.
******************************************************************************/
package gov.sandia.sems.jenkinsutilities.treeaccessor;

import java.util.Set;

import gov.sandia.sems.jenkinsutilities.db.IDatabasePart;

/**
 * Interface to an arbitrary, hierarchical database storage mechanism that can
 * store performance report filtering information.
 * 
 * @author Elliott Ridgway
 */
public interface IFilterAccessor extends IDatabasePart {

    /**
    * @return A Set of {@link FilterData} objects maintained by this ITreeAccessor.
    */
    public Set<FilterData> getFilterData();

    /**
     * @param path The path to search on.
     * @param exactMatch Whether or not to allow partial matches of the FilterData's path field.
     * @return The FilterData object to return, or null if there was no matching path.
     */
    public FilterData getFilterDataByPath(String path, boolean exactMatch);
}