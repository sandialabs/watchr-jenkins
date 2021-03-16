/*******************************************************************************
* Watchr
* ------
* Copyright 2021 National Technology & Engineering Solutions of Sandia, LLC (NTESS).
* Under the terms of Contract DE-NA0003525 with NTESS, the U.S. Government retains
* certain rights in this software.
******************************************************************************/
package gov.sandia.sems.jenkinsutilities.htmlgenerator.customviews;

import gov.sandia.sems.jenkins.semsjppplugin.Icons;
import gov.sandia.sems.jenkinsutilities.htmlgenerator.DivModel;
import gov.sandia.sems.jenkinsutilities.htmlgenerator.HtmlConstants;

/**
 * A model of data used for the generation of a div containing an add button.
 * This is used for adding new custom views.
 * 
 * @author Elliott Ridgway
 */
public class AddButtonDivModel extends DivModel {

    //////////////
    // OVERRIDE //
    //////////////

    @Override
    public String toDiv(int graphWidth, int graphHeight) {
        final String CUSTOM_VIEW_ADD_OUTER_LABEL = "custom-view-add-outer";
        final String CUSTOM_VIEW_ADD_LABEL = "custom-view-add";
        final String CUSTOM_VIEW_ADD_PRETTY_LABEL = "Add New Custom View";

        StringBuilder sb = new StringBuilder();
        sb.append("<td style=\"width: 260px; height: 500px;\">");
        sb.append("<div id=\"").append(CUSTOM_VIEW_ADD_OUTER_LABEL).append("\" align=\"right\" style=\"padding: 20px; width:");
        sb.append(addDivWidthSpacing(graphWidth));
        sb.append("px; height:");
        sb.append(addDivHeightSpacing(graphHeight));
        sb.append("px; border: thin solid black;  background-color: #F0FFFF\">\n");
        sb.append("<div class=\"test-trend-caption\">");
        sb.append(CUSTOM_VIEW_ADD_PRETTY_LABEL).append("</div>");
        sb.append("<div id=\"").append(CUSTOM_VIEW_ADD_LABEL).append("\" style=\"width:").append(graphWidth);
        sb.append("px;height:").append(graphHeight);
        sb.append("px;");
        sb.append(HtmlConstants.IMG_STYLE).append("\">\n");
        sb.append("<a href='");
        sb.append("?").append(HtmlConstants.PARAM_CUSTOM_VIEW_MODE);
        sb.append("=").append(HtmlConstants.CUSTOM_VIEW_MODE_ADD);
        sb.append("'><img src='").append(Icons.ADD_BUTTON).append("'/></a>");
        sb.append("</div>\n");
        sb.append("</div>\n");
        sb.append("</div>\n");
        sb.append("</td>\n");
        return sb.toString();
    }
    
    @Override
    protected int addDivWidthSpacing(int initialValue) {
        return initialValue + 100;
    }
    
    @Override
    protected int addDivHeightSpacing(int initialValue) {
        return initialValue + 200;
    }

    @Override
    protected String createDivOptionsHtml() {
        return new String();
    }
}
