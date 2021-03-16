/*******************************************************************************
* Watchr
* ------
* Copyright 2021 National Technology & Engineering Solutions of Sandia, LLC (NTESS).
* Under the terms of Contract DE-NA0003525 with NTESS, the U.S. Government retains
* certain rights in this software.
******************************************************************************/
package gov.sandia.sems.jenkins.semsjppplugin;

public enum PerformanceReportFormat {
    XML("XML"),
    JSON_STANDARD("JSON (Standard)"),
    JSON_PYOMO("JSON (Pyomo)"),
    JSON_TRILINOS("JSON (Trilinos)");

    private final String label;

    private PerformanceReportFormat(String label) {
        this.label = label;
    }

    public String get() {
        return label;
    }

    public static PerformanceReportFormat getEnumFromLabel(String label) {
        if(label.equals("XML")) { return XML; }
        else if(label.equals("JSON (Standard)")) { return JSON_STANDARD; }
        else if(label.equals("JSON (Pyomo)")) { return JSON_PYOMO; }
        else if(label.equals("JSON (Trilinos)")) { return JSON_TRILINOS; }
        else { return null; }
    }
}