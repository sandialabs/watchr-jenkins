/*******************************************************************************
* Watchr
* ------
* Copyright 2021 National Technology & Engineering Solutions of Sandia, LLC (NTESS).
* Under the terms of Contract DE-NA0003525 with NTESS, the U.S. Government retains
* certain rights in this software.
******************************************************************************/
package gov.sandia.sems.jenkinsutilities.htmlgenerator;

public enum MainReportAction {
    NONE,
    SAVE_FILTER,
    SAVE_AND_CLOSE_FILTER;

    public static String getStringVersion(MainReportAction action) {
        if(action == SAVE_FILTER) {
            return HtmlConstants.PARAM_REPORT_MODE_FILTER_SAVE;
        } else if(action == SAVE_AND_CLOSE_FILTER) {
            return HtmlConstants.PARAM_REPORT_MODE_FILTER_SAVE_AND_CLOSE;
        }
        return ""; // NONE
    }

    public static MainReportAction getEnumVersion(String actionStr) {
       if(actionStr.equals(HtmlConstants.PARAM_REPORT_MODE_FILTER_SAVE)) {
            return SAVE_FILTER;
        } else if(actionStr.equals(HtmlConstants.PARAM_REPORT_MODE_FILTER_SAVE_AND_CLOSE)) {
            return SAVE_AND_CLOSE_FILTER;
        }
        return NONE;
    }
}