/*******************************************************************************
* Watchr
* ------
* Copyright 2021 National Technology & Engineering Solutions of Sandia, LLC (NTESS).
* Under the terms of Contract DE-NA0003525 with NTESS, the U.S. Government retains
* certain rights in this software.
******************************************************************************/
package gov.sandia.sems.jenkins.semsjppplugin;

import java.text.SimpleDateFormat;

/**
 * Common constants used throughout Watchr.
 * 
 * @author Elliott Ridgway
 */
public class CommonConstants {
    
    public static final String PROJECT_VERSION = "2.6.0";

    // Alias used to represent the root of the folder tree.
    public static final String ROOT_PATH_ALIAS = "ROOT";
    
    public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
    
    public static final String MAX_DATE = "z"; // "z" is like an upper range for date strings.
    public static final String MIN_DATE = "0"; // "0" is like a lower range for date strings.

    public static final String AVAILABLE = "available";
    public static final String AVAILABLE_DATASETS = "availableDatasets";
    public static final String SELECTED = "selected";
    public static final String SELECTED_DATASETS = "selectedDatasets";
}
