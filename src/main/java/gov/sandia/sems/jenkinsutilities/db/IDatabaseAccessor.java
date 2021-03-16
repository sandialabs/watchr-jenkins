/*******************************************************************************
* Watchr
* ------
* Copyright 2021 National Technology & Engineering Solutions of Sandia, LLC (NTESS).
* Under the terms of Contract DE-NA0003525 with NTESS, the U.S. Government retains
* certain rights in this software.
******************************************************************************/
package gov.sandia.sems.jenkinsutilities.db;

/**
 * A lightweight interface that serves up access to specific parts of
 * the Watchr database.  The specific database implementation
 * should be completely invisible to those who call methods from this
 * class.
 * 
 * @author Elliott Ridgway
 */
public interface IDatabaseAccessor {

    /**
     * A Watchr database is split into conceptual "parts" that can be
     * handled one at a time.  See {@link IDatabasePartType} for a full
     * list of defined database parts.
     * 
     * @param type The type of database part.
     * @return The database part.
     */
    public IDatabasePart getDatabasePart(IDatabasePartType type);

    /**
     * A Watchr database is split into conceptual "parts" that can be
     * handled one at a time.  See {@link IDatabasePartType} for a full
     * list of defined database parts.<br><br>
     * This method variation opens the database part after retrieving it,
     * by invoking the IDatabasePart object's open() method.
     * 
     * @param type The type of database part.
     * @return The opened database part.
     */
    public IDatabasePart getAndOpenDatabasePart(IDatabasePartType type);

    /**
     * Sets a specific database part.  A Watchr database is split into
     * conceptual "parts" that can be handled one at a time.  See
     * {@link IDatabasePartType} for a full list of defined database parts.
     * @param type The type of database part.
     * @param part The part to set.
     */
    public void setDatabasePart(IDatabasePartType type, IDatabasePart part);
}