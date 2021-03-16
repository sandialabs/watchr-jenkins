/*******************************************************************************
* Watchr
* ------
* Copyright 2021 National Technology & Engineering Solutions of Sandia, LLC (NTESS).
* Under the terms of Contract DE-NA0003525 with NTESS, the U.S. Government retains
* certain rights in this software.
******************************************************************************/
package gov.sandia.sems.jenkinsutilities.htmlgenerator.customviews;

import gov.sandia.sems.jenkinsutilities.htmlgenerator.HtmlConstants;

public enum CustomViewSearchType {
    SEARCH_BY_NAME,
    SEARCH_BY_PATH;

    public static String getStringVersion(CustomViewSearchType searchType) {
        if(searchType == SEARCH_BY_NAME) {
            return HtmlConstants.CUSTOM_VIEW_SEARCH_BY_NAME;
        } else if(searchType == SEARCH_BY_PATH) {
            return HtmlConstants.CUSTOM_VIEW_SEARCH_BY_PATH;
        }
        return null;
    } 

    public static CustomViewSearchType getEnumVersion(String searchTypeStr) {
        if(searchTypeStr.equals(HtmlConstants.CUSTOM_VIEW_SEARCH_BY_NAME)) {
            return SEARCH_BY_NAME;
        } else if(searchTypeStr.equals(HtmlConstants.CUSTOM_VIEW_SEARCH_BY_PATH)) {
            return SEARCH_BY_PATH;
        }
        return null;
    }
}