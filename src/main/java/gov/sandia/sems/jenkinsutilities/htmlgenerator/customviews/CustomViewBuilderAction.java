/*******************************************************************************
* Watchr
* ------
* Copyright 2021 National Technology & Engineering Solutions of Sandia, LLC (NTESS).
* Under the terms of Contract DE-NA0003525 with NTESS, the U.S. Government retains
* certain rights in this software.
******************************************************************************/
package gov.sandia.sems.jenkinsutilities.htmlgenerator.customviews;

import org.apache.commons.lang3.StringUtils;

import gov.sandia.sems.jenkinsutilities.htmlgenerator.HtmlConstants;

public enum CustomViewBuilderAction {
    NONE,
    SEARCH,
    MOVE_AVAILABLE,
    REMOVE_SELECTED,
    SAVE;

    public static String getStringVersion(CustomViewBuilderAction action) {
        if(action == NONE) {
            return "";
        } else if(action == SEARCH) {
            return HtmlConstants.CUSTOM_VIEW_BUILDER_ACTION_SEARCH;
        } else if(action == MOVE_AVAILABLE) {
            return HtmlConstants.CUSTOM_VIEW_BUILDER_ACTION_MOVE_AVAILABLE;
        } else if(action == REMOVE_SELECTED) {
            return HtmlConstants.CUSTOM_VIEW_BUILDER_ACTION_REMOVE_SELECTED;
        } else if(action == SAVE) {
            return HtmlConstants.CUSTOM_VIEW_BUILDER_ACTION_SAVE;
        }
        return null;
    } 

    public static CustomViewBuilderAction getEnumVersion(String customViewActionStr) {
        if(StringUtils.isBlank(customViewActionStr)) {
            return NONE;
        } else if(customViewActionStr.equals(HtmlConstants.CUSTOM_VIEW_BUILDER_ACTION_SEARCH)) {
            return SEARCH;
        } else if(customViewActionStr.equals(HtmlConstants.CUSTOM_VIEW_BUILDER_ACTION_MOVE_AVAILABLE)) {
            return MOVE_AVAILABLE;
        } else if(customViewActionStr.equals(HtmlConstants.CUSTOM_VIEW_BUILDER_ACTION_REMOVE_SELECTED)) {
            return REMOVE_SELECTED;
        } else if(customViewActionStr.equals(HtmlConstants.CUSTOM_VIEW_BUILDER_ACTION_SAVE)) {
            return SAVE;
        }
        return null;
    }
}