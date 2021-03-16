/*******************************************************************************
* Watchr
* ------
* Copyright 2021 National Technology & Engineering Solutions of Sandia, LLC (NTESS).
* Under the terms of Contract DE-NA0003525 with NTESS, the U.S. Government retains
* certain rights in this software.
******************************************************************************/
package gov.sandia.sems.jenkinsutilities.htmlgenerator;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;
import java.util.Map.Entry;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import org.apache.commons.lang3.StringUtils;

public class HtmlFragmentGenerator {

    ////////////
    // FIELDS //
    ////////////

    private static final String NEW_TABLE_ROW = "<tr>\n";

    /////////////////
    // CONSTRUCTOR //
    /////////////////

    private HtmlFragmentGenerator() {}

    //////////////////////
    // MAIN REPORT PAGE //
    //////////////////////

    public static String buildOptionCombo(
            String comboName, String comboDisplay, String[] options, String selectedItem, int width) {
        StringBuilder sb = new StringBuilder();
        sb.append("<tr>");
        sb.append("<td class=\"setting-leftspace\">&nbsp;</td>\n");
        sb.append("<td class=\"setting-name\">").append(comboDisplay).append("</td>\n");
        sb.append("<td class=\"setting-main\">");
        sb.append("<select class=\"setting-input dropdownList\" name=\"").append(comboName).append("\" ");
        sb.append("style=\"width: ").append(width).append("px;\"");
        sb.append(">");

        for(String option : options) {
            sb.append("\n\t<option value='").append(option.replace(" ", "")).append("' ");
            if(!StringUtils.isBlank(selectedItem)) {
                sb.append((selectedItem.equalsIgnoreCase(option) ? "selected='selected' " : " "));
            }
            sb.append(">");
            sb.append(option);
            sb.append("</option>");
        }
        sb.append("\n</select>");

        sb.append("</td>");
        sb.append(buildHelpLinkButton(comboName, comboDisplay));
        sb.append("</tr>");
        sb.append("<tr class=\"validation-error-area\"><td colspan=\"2\"></td><td></td><td></td></tr>");
        sb.append(buildHelpTextDock("Loading..."));
        return sb.toString();
    }

    public static String buildTextField(String textName, String textDisplay, String defaultValue, int width) {
        StringBuilder sb = new StringBuilder();
        sb.append("<tr>");
        sb.append("<td class=\"setting-leftspace\">&nbsp;</td>\n");
        sb.append("<td class=\"setting-name\" style=\"vertical-align:middle;\">").append(textDisplay).append("</td>\n");
        sb.append("<td class=\"setting-main\">");
        sb.append("<input class=\"setting-input\" name=\"").append(textName).append("\" ");
        sb.append("value=\"").append(defaultValue).append("\"");
        sb.append("type=\"text\" style=\"width: ").append(width).append("px;\"");
        sb.append(">");
        sb.append("</td>");
        sb.append(buildHelpLinkButton(textName, textDisplay));
        sb.append("</tr>");
        sb.append(buildHelpTextDock("Loading..."));
        return sb.toString();
    }

    public static String buildCheckboxField(String checkboxName, String checkboxDisplay, boolean checked) {
        StringBuilder sb = new StringBuilder();
        sb.append("<tr>");
        sb.append("<td class=\"setting-leftspace\">&nbsp;</td>\n");
        sb.append("<td class=\"setting-name\" style=\"vertical-align:middle;\">").append(checkboxDisplay)
                .append("</td>\n");
        sb.append("<td class=\"setting-main\">");
        sb.append("<input class=\"setting-input\" name=\"").append(checkboxName).append("\" ");
        sb.append("value=\"true\" ");
        sb.append("type=\"checkbox\" style=\"width: 10px;\" ");
        sb.append(checked ? "checked" : "");
        sb.append(">");
        sb.append("</td>");
        sb.append(buildHelpLinkButton(checkboxName, checkboxDisplay));
        sb.append("</tr>");
        sb.append(buildHelpTextDock("Loading..."));
        return sb.toString();
    }

    public static String buildHelpLinkButton(String name, String display) {
        StringBuilder sb = new StringBuilder();
        
        sb.append("<td class=\"setting-help\">");
        sb.append("<a helpurl=\"/descriptor/gov.sandia.sems.jenkins.semsjppplugin.PerformanceRecorder/help/");
        sb.append(name).append("\" href=\"#\" class=\"help-button\">");
        sb.append("<svg viewBox=\"0 0 24 24\" aria-hidden=\"\" tooltip=\"Help for feature: ");
        sb.append(display).append("\" focusable=\"false\" class=\"svg-icon icon-help \">");
		sb.append("<use href=\"/static/a6762ba2/images/material-icons/svg-sprite-action-symbol.svg#ic_help_24px\">");
		sb.append("</use>");
		sb.append("</svg>");
		sb.append("</a>");
        sb.append("</td>");
        return sb.toString();
    }

    public static String buildHelpTextDock(String helpText) {
        StringBuilder sb = new StringBuilder();
        sb.append("<tr class=\"help-area\">");
	    sb.append("<td></td>");
	    sb.append("<td colspan=\"2\">");
		sb.append("<div class=\"help\" style=\"display: none;\">");
        sb.append(helpText);
        sb.append("</div></td><td></td></tr>");
        return sb.toString();
    }

    public static String buildGraphs(
            AbstractConfigContext context, SortedMap<String, IndividualDivTuple> nodes) throws UnsupportedEncodingException {
        int graphsPerRow = context.getGraphsPerRow();

        // Determine page range to display.
        int page = context.getPage();
        final int pageRangeBegin = page * HtmlConstants.MAX_GRAPHS_PER_PAGE;
        final int pageRangeEnd = (page+1) * HtmlConstants.MAX_GRAPHS_PER_PAGE;
        final int numberOfPages = (int) Math.ceil((nodes.size() * 1.0) / (HtmlConstants.MAX_GRAPHS_PER_PAGE * 1.0));

        StringBuilder sb = new StringBuilder();
        if(page != 0 || nodes.size() > HtmlConstants.MAX_GRAPHS_PER_PAGE) {
            sb.append(HtmlConstants.TITLE_STYLE);
            sb.append("Pages");
            sb.append("</p>");
            sb.append("<p style='margin-left:10px'>");
            for(int i = 0; i < numberOfPages; i++) {
                if(i == page) { sb.append("<strong>"); }
                sb.append(buildPageLink(context.getGraphSelectedPath(), i, Integer.toString(i+1))).append(" ");
                if(i == page) { sb.append("</strong>"); }
            }
            sb.append("</p>");
        }

        sb.append(HtmlConstants.TABLE_STYLE);
        sb.append("<tbody><tr>");
        // Then, we use those tuples we made to actually generate html.
        int index = 0;
        for (Entry<String,IndividualDivTuple> entry : nodes.entrySet()) {
            if(index >= pageRangeBegin && index < pageRangeEnd) {
                IndividualDivTuple node = entry.getValue();
                sb.append(node.toDiv(context.getGraphWidth(), context.getGraphHeight()));
                if ((index + 1) % graphsPerRow == 0) {
                    sb.append("</tr>\n<tr>");
                }
            }
            index++;
        }
        sb.append("</tr></tbody></table>");

        return sb.toString();
    }

    /**
     * Display text to alert the user to the fact that no graphable data could be found.
     * @return The HTML.
     */
    public static String noGraphs() {
        StringBuilder sb = new StringBuilder();
        sb.append("<div class=\"error\" style=\"margin:15px\"'>");
        sb.append("No graphs were found at this location for the given measurable type.");
        sb.append("</div>").append("\n");
        return sb.toString();
    }

    /**
     * Generate the HTML link for navigating to another page of graphs.
     * 
     * @param path         The path that we're currently at.
     * @param newPage      The new page number to navigate to.
     * @param pageLinkText The text to display for the link.
     * @return The HTML.
     * @throws UnsupportedEncodingException Thrown if formatting the parameters for
     *                                      the HTML fails.
     */
    public static String buildPageLink(
            String path, int newPage, String pageLinkText) throws UnsupportedEncodingException {
        StringBuilder sb = new StringBuilder();
        sb.append("<a href='");
        sb.append(AbstractHtmlGenerator.createParameterList(path, newPage)).append("'>");
        sb.append(pageLinkText).append("</a>");
        sb.append("<input type=\"hidden\" name=\"page\" value='").append(newPage);
        sb.append("'/>");

        return sb.toString();
    }

    ///////////////////////
    // GRAPH FILTER PAGE //
    ///////////////////////
    
    /**
     * Build the editor for filtering data off of graphs.
     * @param dataBundle The JsonObject data bundle needed to construct the HTML editor.
     * @return The built HTML as a String.
     */
    public static String buildGraphFilterEditor(JsonObject dataBundle) {
        StringBuilder sb = new StringBuilder();

        JsonElement graphNameJson = dataBundle.get("graphName");
        JsonElement graphPathJson = dataBundle.get("graphPath");
        String graphName = "";
        String graphPath = "";
        String graphPathUpToName = "";
        JsonArray datesJsonArray = null;
        JsonArray filterDatesJsonArray = null;

        if(graphNameJson != null) {
            graphName = graphNameJson.getAsString();
        }
        if(graphPathJson != null) {
            graphPath = graphPathJson.getAsString();
            int endIndex = graphPath.lastIndexOf(File.separator);
            if(endIndex != -1) {
                graphPathUpToName = graphPath.substring(0, endIndex);
            } else {
                graphPathUpToName = graphPath;
            }
        }
        JsonElement datesJson = dataBundle.get("dates");
        
        if(datesJson != null) {
            datesJsonArray = datesJson.getAsJsonArray();
        }
        JsonElement filterDatesJson = dataBundle.get("filterDates");
        
        if(filterDatesJson != null) {
            filterDatesJsonArray = filterDatesJson.getAsJsonArray();
        }

        sb.append("<div class=\"pane-frame\">\n");
        sb.append("<table id=\"graphSettings\" class=\"pane\" style=\"margin-top:0; border-top:none\">\n");
        sb.append("<tr style=\"border-top:none; white-space: normal\">\n");
        sb.append("<td>\n");
        sb.append("<h2 style=\"padding-top:20px;padding-left:20px;\">");
        sb.append("Update Data Filter for " + graphName);
        sb.append("</h2>");

        sb.append("<form method=\"post\" autocomplete=\"off\" name=\"filterEditor\">");
        sb.append("<table width=\"100%\">");

        sb.append(AbstractHtmlGenerator.appendCrumbInformation());

        List<String> displayList = new ArrayList<>();
        if(datesJsonArray != null) {
            for(int i = 0; i < datesJsonArray.size(); i++) {
                displayList.add(datesJsonArray.get(i).getAsString());
            }
        }
        List<String> filterList = new ArrayList<>();
        if(filterDatesJsonArray != null) {
            for(int i = 0; i < filterDatesJsonArray.size(); i++) {
                filterList.add(filterDatesJsonArray.get(i).getAsString());
            }
        }

        IndividualDivTuple graphDiv = new IndividualDivTuple(graphName, graphPathUpToName, "", "/", false, false);

        sb.append(NEW_TABLE_ROW);
        sb.append(buildDatasetListSection(displayList, filterList));
        sb.append(buildFilterGraphPreviewSection(graphDiv));
        sb.append("</tr>\n");
        sb.append("</table>\n");

        sb.append("<div class=\"pane-frame\" style=\"padding: 10px; margin: 5px;\">\n");
        sb.append("Note:  Rolling average and rolling standard deviation lines are calculated at build time.  ");
        sb.append("Filtered data points will not cause changes to these derived lines until the next build.");
        sb.append("</div>\n");

        sb.append("<table width=\"100%\">");
        sb.append("<input name=\"").append(HtmlConstants.PARAM_REPORT_ACTION_SAVE_AND_CLOSE).append("\" ");
        sb.append("type=\"submit\" value=\"").append(HtmlConstants.PARAM_REPORT_ACTION_SAVE_AND_CLOSE_DISPLAY).append("\" ");
        sb.append("class=\"submit-button\" />");
        sb.append("</table>");

        sb.append("</form>\n");
        sb.append("</td>\n");
        sb.append("</tr>\n");
        sb.append("</div>\n");

        return sb.toString();
    }

    /**
     * Build the HTML segment for displaying lists of dates that can be filtered.
     * 
     * @param displayList The full List of String elements to display.
     * @param filterList The subset of filtered elements to display.
     * @return The generated HTML as a String.
     */
    private static String buildDatasetListSection(List<String> displayList, List<String> filterList) {
        String header = "Filtered Dates";
        String tableId = "filterTable";

        StringBuilder sb = new StringBuilder();
        sb.append("<td>");
        sb.append("<div class=\"pane-frame\">\n");
        sb.append("<table id=\"").append(tableId).append("\" class=\"pane\" style=\"margin-top:0; border-top:none\">\n");
        sb.append("<tr style=\"border-top:none; white-space: normal\">\n");
        sb.append("<td>\n");
        sb.append("<h2 style=\"padding-top:20px;padding-left:20px;\">").append(header).append("</h2>\n");
        sb.append("</td></tr>\n");
        
        sb.append("<tr><td style=\"padding-left: 10px;\">");
        sb.append("<div style=\"height:560px;width:400px;border:1px solid #ccc;overflow:auto;\">\n");
        sb.append("<table id=\"").append(tableId).append("_inner\">\n");
        for(String element : displayList) {
            sb.append(buildFilterPageCheckboxField(element, element, filterList.contains(element)));
        }
        sb.append("</table>");
        sb.append("</div>");
        sb.append("</td></tr>");
        
        sb.append("<tr>");
        sb.append("<td>");
        sb.append("<input name=\"").append(HtmlConstants.PARAM_REPORT_ACTION_SAVE).append("\" ");
        sb.append("type=\"submit\" value=\"").append(HtmlConstants.PARAM_REPORT_ACTION_SAVE_DISPLAY).append("\" ");
        sb.append("class=\"submit-button\" />");
        sb.append("</td>");
        sb.append("</tr>");
        sb.append("</table>");
        sb.append("</div>");
        sb.append("</td>");

        return sb.toString();
    }

    /**
     * Builds the individual checkbox control.
     * 
     * @param checkboxName The unique HTML element name for the checkbox.
     * @param checkboxDisplay The display string to show to the right of the checkbox.
     * @param selected Whether the checkbox should be selected.
     * @return The generated HTML as a String.
     */
    private static String buildFilterPageCheckboxField(String checkboxName, String checkboxDisplay, boolean selected) {
        StringBuilder sb = new StringBuilder();
        sb.append(NEW_TABLE_ROW);
        sb.append("<td class=\"setting-leftspace\"></td>\n");
        
        sb.append("<td>");
        sb.append("<input class=\"setting-input\" name=\"");
        sb.append("filterDateElement");
        sb.append(HtmlConstants.PARAM_MUSTACHIO);
        sb.append(checkboxName).append("\" ");
        sb.append("value=\"true\" ");
        sb.append("type=\"checkbox\" style=\"width: 10px;\"");
        sb.append(selected ? " checked " : "");
        sb.append("></td>");

        sb.append("<td class=\"setting-main\" style=\"vertical-align:middle;\">").append(checkboxDisplay).append("</td>\n");

        sb.append("</tr>");
        return sb.toString();
    }

    /**
     * Build the HTML segment for displaying lists of dates that can be filtered.
     * 
     * @param graphDiv The graph div to display.
     * @return The generated HTML as a String.
     */
    private static String buildFilterGraphPreviewSection(IndividualDivTuple graphDiv) {
        String header = "Preview";
        String tableId = "previewTable";

        StringBuilder sb = new StringBuilder();
        sb.append("<td>");
        sb.append("<div class=\"pane-frame\">\n");
        sb.append("<table id=\"").append(tableId).append("\" class=\"pane\" style=\"margin-top:0; border-top:none\">\n");
        sb.append("<tr style=\"border-top:none; white-space: normal\"><td>\n");
        sb.append("<h2 style=\"padding-top:20px;padding-left:20px;\">").append(header).append("</h2>\n");
        sb.append("</td></tr>\n");

        sb.append("<tr>");
        sb.append(graphDiv.toDiv(680, 400));
        sb.append("</tr>");

        sb.append("</table>");
        sb.append("</div>");
        sb.append("</td>");

        return sb.toString();
    }
}