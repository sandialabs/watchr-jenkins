/*******************************************************************************
* Watchr
* ------
* Copyright 2021 National Technology & Engineering Solutions of Sandia, LLC (NTESS).
* Under the terms of Contract DE-NA0003525 with NTESS, the U.S. Government retains
* certain rights in this software.
******************************************************************************/
package gov.sandia.sems.jenkinsutilities.htmlgenerator.customviews;

import gov.sandia.sems.jenkinsutilities.htmlgenerator.HtmlConstants;

public enum CustomViewMode {
    NONE,
    ADD,
    EDIT,
    DELETE_ASK,
    DELETE,
    UPDATE_DISPLAY;

    public static String getStringVersion(CustomViewMode customViewMode) {
        if(customViewMode == ADD) {
            return HtmlConstants.CUSTOM_VIEW_MODE_ADD;
        } else if(customViewMode == EDIT) {
            return HtmlConstants.CUSTOM_VIEW_MODE_EDIT;
        } else if(customViewMode == DELETE_ASK) {
            return HtmlConstants.CUSTOM_VIEW_MODE_DELETE_ASK;
        } else if(customViewMode == DELETE) {
            return HtmlConstants.CUSTOM_VIEW_MODE_DELETE;
        } else if(customViewMode == UPDATE_DISPLAY) {
            return HtmlConstants.CUSTOM_VIEW_MODE_UPDATE_DISPLAY;
        }
        return ""; // NONE
    }

    public static CustomViewMode getEnumVersion(String customViewModeStr) {
        if(customViewModeStr.equals(HtmlConstants.CUSTOM_VIEW_MODE_ADD)) {
            return ADD;
        } else if(customViewModeStr.equals(HtmlConstants.CUSTOM_VIEW_MODE_EDIT)) {
            return EDIT;
        } else if(customViewModeStr.equals(HtmlConstants.CUSTOM_VIEW_MODE_DELETE_ASK)) {
            return DELETE_ASK;
        } else if(customViewModeStr.equals(HtmlConstants.CUSTOM_VIEW_MODE_DELETE)) {
            return DELETE;
        } else if(customViewModeStr.equals(HtmlConstants.CUSTOM_VIEW_MODE_UPDATE_DISPLAY)) {
            return UPDATE_DISPLAY;
        }
        return NONE;
    }
}