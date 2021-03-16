/*******************************************************************************
* Watchr
* ------
* Copyright 2021 National Technology & Engineering Solutions of Sandia, LLC (NTESS).
* Under the terms of Contract DE-NA0003525 with NTESS, the U.S. Government retains
* certain rights in this software.
******************************************************************************/
package gov.sandia.sems.jenkinsutilities.db;

/**
 * A database part is a single section of the Watchr database.
 * @author Elliott Ridgway
 */
public interface IDatabasePart {

   /**
    * Opens this part of the database, for reading/writing data.
    * Opening should be the first step when it comes to handling
    * database data.
    * @return Whether or not the open operation was successful.
    */
    public boolean open();
   
    /**
     * Closes this part of the database, for when we're done
     * reading/writing data.  A caller should not be able to read
     * or write from a closed database part.     * 
     * @return Whether or not the close operation was successful.
     */
    public boolean close();

    /**
     * @return Whether this database part is in an open state.
     */
    public boolean isOpen();

    /**
     * @return The parent IDatabaseAccessor object.
     */
    public IDatabaseAccessor getParentDatabase();
}