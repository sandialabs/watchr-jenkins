/*******************************************************************************
* Watchr
* ------
* Copyright 2021 National Technology & Engineering Solutions of Sandia, LLC (NTESS).
* Under the terms of Contract DE-NA0003525 with NTESS, the U.S. Government retains
* certain rights in this software.
******************************************************************************/
package gov.sandia.sems.jenkinsutilities.htmlgenerator;

public enum MainReportMode {
    NONE,
    VIEW_FILTER;

    public static String getStringVersion(MainReportMode mainReportMode) {
        if(mainReportMode == VIEW_FILTER) {
            return HtmlConstants.PARAM_REPORT_MODE_FILTER_VIEW;
        }
        return ""; // NONE
    }

    public static MainReportMode getEnumVersion(String mainReportModeStr) {
        if(mainReportModeStr.equals(HtmlConstants.PARAM_REPORT_MODE_FILTER_VIEW)) {
            return VIEW_FILTER;
        }
        return NONE;
    }
}