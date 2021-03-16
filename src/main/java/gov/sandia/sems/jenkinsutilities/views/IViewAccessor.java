/*******************************************************************************
* Watchr
* ------
* Copyright 2021 National Technology & Engineering Solutions of Sandia, LLC (NTESS).
* Under the terms of Contract DE-NA0003525 with NTESS, the U.S. Government retains
* certain rights in this software.
******************************************************************************/
package gov.sandia.sems.jenkinsutilities.views;

import java.util.Collection;
import java.util.UUID;

import gov.sandia.sems.jenkinsutilities.db.IDatabasePart;
import gov.sandia.sems.jenkinsutilities.treeaccessor.ITreeAccessor;

/**
 * Interface for accessing the user's collection of {@link View}s.
 * This interface abstracts away the specific data storage mechanism
 * for loading and saving View objects.<br><br>
 * 
 * A notable difference between IViewAccessor and {@link ITreeAccessor}
 * is that there is no implied information hierarchy in IViewAccessor;
 * that is, all user-defined Views can be stored in a flat structure.
 * This interface has no support for parent-child relationships between
 * Views.  Additionally, name uniqueness between Views must be enforced
 * by implementations of this interface, since Views can be looked up by
 * name alone.
 * 
 * @author Elliott Ridgway
 */
public interface IViewAccessor extends IDatabasePart {

    /**
     * @return The entire {@link Collection} of {@link View}s.
     */
    public Collection<View> getAllViews();

    /**
     * @param uuid The UUID of the {@link View} to retrieve.
     * @return The retrieved View, or null if no View exists
     * with the given UUID.
     */
    public View getView(UUID uuid);

    /**
     * @param name The View name to search on.
     * @return The first View with a matching name, or null if
     * none exists.
     */
    public View findView(String name);

    /**
     * Add a new {@link View} to the IViewAccessor implementation.
     * @param viewToAdd The View to add.
     */
    public void addView(View viewToAdd);

    /**
     * Updates an existing {@link View}.  Because Views have
     * immutable properties, it is expected that a delete-then-add
     * strategy should take place (that is, the original View object
     * will be deleted, and a new View object, representing the modified
     * version of the original View, will be added in its place.)
     * @param originalViewUUID The UUID of the original View to discard.
     * @param viewToAdd The new View (with a new UUID) that will take its
     * place.
     */
    public void replaceView(UUID originalViewUUID, View viewToAdd);

    /**
     * Deletes an existing {@link View}.
     * @param viewToDelete The view to delete.
     * @return Whether the delete operation was successful.
     */
    public boolean deleteView(View viewToDelete);
}