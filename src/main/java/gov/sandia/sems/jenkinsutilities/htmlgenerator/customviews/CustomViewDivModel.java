/*******************************************************************************
* Watchr
* ------
* Copyright 2021 National Technology & Engineering Solutions of Sandia, LLC (NTESS).
* Under the terms of Contract DE-NA0003525 with NTESS, the U.S. Government retains
* certain rights in this software.
******************************************************************************/
package gov.sandia.sems.jenkinsutilities.htmlgenerator.customviews;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import gov.sandia.sems.jenkins.semsjppplugin.Icons;
import gov.sandia.sems.jenkinsutilities.htmlgenerator.DivModel;
import gov.sandia.sems.jenkinsutilities.htmlgenerator.HtmlConstants;

/**
 * A tuple of data used for generation of graph divs.
 * 
 * @author Elliott Ridgway, Lawrence Allen
 */
public class CustomViewDivModel extends DivModel {
        
    ////////////
    // FIELDS //
    ////////////

    private final String name;

    //////////////////
    // CONSTRUCTORS //
    //////////////////

    public CustomViewDivModel(String name) {
        this.name = name;
    }

    //////////////
    // OVERRIDE //
    //////////////

    @Override
    public String toDiv(int graphWidth, int graphHeight) {
        StringBuilder sb = new StringBuilder();
        sb.append("<td style=\"width: 260px; height: 500px;\">");
        sb.append("<div id=\"flotr-chart-outer-").append(name).append("\" align=\"right\" style=\"padding: 20px; width:");
        sb.append(addDivWidthSpacing(graphWidth)).append("px; height:");
        sb.append(addDivHeightSpacing(graphHeight)).append("px; border: thin solid black;  background-color: #F0FFFF\">\n");
        sb.append("<div class=\"test-trend-caption\">");
        sb.append(name).append("</div>");
        sb.append("<div id=\"flotr-chart-").append(name);
        sb.append("\" style=\"width:").append(graphWidth);
        sb.append("px;height:").append(graphHeight).append("px;");
        sb.append(HtmlConstants.IMG_STYLE).append("\">\n");
        sb.append("<img src='").append(Icons.NO_DATA_IMG_PATH).append("'/>");
        sb.append("</div>");
        sb.append("<div class=\"test-trend-caption\" >");
        sb.append(HtmlConstants.STATS_DIV_STYLE).append(name).append("_MinMax\"> </div>\n");
        sb.append("<div style='height:1px;font-size:1px;'>&nbsp;</div>");

        sb.append(createDivOptionsHtml());
        sb.append("</div>\n");
        sb.append("</td>");
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
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("<div align='center' style='float:left;'>");
            sb.append("<a href='");
            sb.append("?").append(HtmlConstants.PARAM_CUSTOM_VIEW_MODE);
            sb.append("=").append(HtmlConstants.CUSTOM_VIEW_MODE_EDIT);
            sb.append("&").append(HtmlConstants.PARAM_CURRENT_CUSTOM_VIEW);
            sb.append("=").append(URLEncoder.encode(name, StandardCharsets.UTF_8.name()));
            sb.append("'>");
            sb.append("<img width='32' height='32' src='").append(Icons.EDIT_IMG_PATH).append("'>");
            sb.append("</a>");
            sb.append("<a href='");
            sb.append("?").append(HtmlConstants.PARAM_CUSTOM_VIEW_MODE);
            sb.append("=").append(HtmlConstants.CUSTOM_VIEW_MODE_DELETE_ASK);
            sb.append("&").append(HtmlConstants.PARAM_CURRENT_CUSTOM_VIEW);
            sb.append("=").append(URLEncoder.encode(name, StandardCharsets.UTF_8.name()));
            sb.append("'>");
            sb.append("<img width='32' height='32' src='").append(Icons.DELETE_IMG_PATH).append("'>");
            sb.append("</a>");
            sb.append("</div><br>");
            return sb.toString();
        } catch(UnsupportedEncodingException e) {
            // Do nothing
        }
        return new String();
    }
}