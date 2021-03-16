/*******************************************************************************
* Watchr
* ------
* Copyright 2021 National Technology & Engineering Solutions of Sandia, LLC (NTESS).
* Under the terms of Contract DE-NA0003525 with NTESS, the U.S. Government retains
* certain rights in this software.
******************************************************************************/
package gov.sandia.sems.jenkinsutilities.htmlgenerator;

import gov.sandia.sems.jenkins.semsjppplugin.Icons;
import gov.sandia.sems.jenkinsutilities.utilities.StringUtil;

import java.io.UnsupportedEncodingException;

/**
 * A tuple of data used for generation of graph divs.
 * 
 * @author Elliott Ridgway, Lawrence Allen
 */
public class IndividualDivTuple extends DivModel {
        
    ////////////
    // FIELDS //
    ////////////

    private final String name;
    private final String path;
    private final String category;
    private final String pathSeparator;
    private final boolean hasChildren;
    private final boolean showFilter;

    //////////////////
    // CONSTRUCTORS //
    //////////////////

    public IndividualDivTuple(
            String name, String path, String category, String pathSeparator, boolean hasChildren, boolean showFilter) {
        this.name = name;
        this.path = path;
        this.category = category;
        this.hasChildren = hasChildren;
        this.pathSeparator = pathSeparator;
        this.showFilter = showFilter;
    }

    /////////////
    // GETTERS //
    /////////////

    public String getCategory() {
        return category;
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
        final String fullPath = path + pathSeparator + name;
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("<div align='center' style='float:left;'>");
            if(hasChildren) {
                String pList = AbstractHtmlGenerator.createParameterList(fullPath, 0);
                sb.append("<a href='").append(pList).append("'>");
                sb.append("<img width='32' height='32' src='").append(Icons.DIVE_IMG_PATH).append("'>");
                sb.append("</a>");
            }

            if(showFilter) {
                sb.append("<a href='");
                sb.append("?").append(HtmlConstants.PARAM_REPORT_MODE).append("=").append(HtmlConstants.PARAM_REPORT_MODE_FILTER_VIEW);
                sb.append("&").append(HtmlConstants.PARAM_REPORT_GRAPH_NAME).append("=").append(StringUtil.encode(name));
                sb.append("&").append(HtmlConstants.PARAM_REPORT_GRAPH_PATH).append("=").append(StringUtil.encode(fullPath));
                sb.append("'>");
                sb.append("<img width='32' height='32' src='").append(Icons.FILTER_IMG_PATH).append("'>");
                sb.append("</a>");
            }
            sb.append("</div><br>");
            return sb.toString();
        } catch(UnsupportedEncodingException e) {
            // Do nothing
        }
        return "";
    }
}
