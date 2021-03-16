/*******************************************************************************
* Watchr
* ------
* Copyright 2021 National Technology & Engineering Solutions of Sandia, LLC (NTESS).
* Under the terms of Contract DE-NA0003525 with NTESS, the U.S. Government retains
* certain rights in this software.
******************************************************************************/
package gov.sandia.sems.jenkins.semsjppplugin;

/**
 *
 * Certain words that appear in performance report XML files are considered
 * keywords, not user-defined strings.
 * 
 * @author Elliott Ridgway
 */
public enum Keywords {
    NAME("name"),
    DATE("date"),
    UNITS("units"),
    END_TIME("end-time"), // Not recommended but supported for backwards compatibility.
    TIME_UNITS("time-units"), // Not recommended but supported for backwards compatibility.
            
    METADATA_KEY("key"),
    METADATA_VALUE("value"),
    
    CATEGORY_PERFORMANCE_REPORT("performance-report"),
    CATEGORY_TIMING("timing"),
    CATEGORY_METRIC("metric"),
    CATEGORY_METADATA("metadata");
    
    private final String word;
    
    private Keywords(String word) {
        this.word = word;
    }

    public String get() {
        return word;
    }
}
