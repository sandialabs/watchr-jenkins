/*******************************************************************************
* Watchr
* ------
* Copyright 2021 National Technology & Engineering Solutions of Sandia, LLC (NTESS).
* Under the terms of Contract DE-NA0003525 with NTESS, the U.S. Government retains
* certain rights in this software.
******************************************************************************/
package gov.sandia.watchr.graph;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import gov.sandia.watchr.config.GraphDisplayConfig.GraphDisplaySort;
import gov.sandia.watchr.model.JenkinsConfigContext;
import gov.sandia.watchr.util.CommonConstants;
import gov.sandia.watchr.util.StringUtil;

public class JenkinsHtmlFragmentGenerator {

    ////////////
    // PUBLIC //
    ////////////

    public String buildMenuBar(JenkinsConfigContext context, int numberOfGraphs) throws UnsupportedEncodingException {       
        StringBuilder sb = new StringBuilder();
        String tableContents = populateMenuBarTableContents(context);
        sb.append(buildMenuBarTable(tableContents));
        sb.append(HtmlConstants.BR);

        int currentPage = context.getGraphDisplayConfig().getPage();
        int numberOfPages = (int) Math.ceil((double)numberOfGraphs / (double)context.getGraphDisplayConfig().getGraphsPerPage());
        String graphSelectedPath = context.getGraphDisplayConfig().getLastPlotDbLocation();

        sb.append(buildPagingLinks(currentPage, numberOfPages, graphSelectedPath));
        return sb.toString();
    }

    /**
     * Display text to alert the user to the fact that no graphable data could be found.
     * @return The HTML.
     */
    public String noGraphs() {
        return HtmlUtil.createDiv("No graphs were found at this location for the given category.", "", "error", "", "margin:15px;");
    }

    ///////////////
    // PROTECTED //
    ///////////////

    protected String buildOptionCombo(
            String comboName, String comboDisplay, String[] options, String selectedItem, int width) {
       
        StringBuilder comboMainContentsSb = new StringBuilder();
        List<String> htmlOptions = new ArrayList<>();
        for(String option : options) {
            String optionValue = option.toLowerCase().replace(" ", "");
            htmlOptions.add(
                HtmlUtil.createSelectionOption(option, optionValue, selectedItem.equalsIgnoreCase(option))
            );
        }
        StringBuilder selectStyleSb = new StringBuilder("width: ").append(width).append("px;");
        comboMainContentsSb.append(HtmlUtil.createSelection(htmlOptions, "setting-input dropdownList", comboName, selectStyleSb.toString()));

        List<String> tds1 = new ArrayList<>();
        tds1.add(HtmlUtil.createTableCell(comboDisplay, "setting-name", ""));
        tds1.add(HtmlUtil.createTableCell(comboMainContentsSb.toString(), "setting-main", ""));

        return HtmlUtil.createTableRow(tds1, "");
    }

    protected String buildTextField(String textName, String textDisplay, String defaultValue, int width) {
        List<String> tds = new ArrayList<>();
        tds.add(HtmlUtil.createTableCell(textDisplay, "setting-name", "vertical-align:middle;"));

        StringBuilder td3StyleSb = new StringBuilder("width: ").append(width).append("px;");
        tds.add(HtmlUtil.createTableCell(
            HtmlUtil.createInput("", "", "setting-input", textName, "text", defaultValue, td3StyleSb.toString()),
            "", ""
        ));

        return HtmlUtil.createTableRow(tds, "");
    }

    protected String buildCheckboxField(String checkboxName, String checkboxDisplay, boolean checked) {
        List<String> tds = new ArrayList<>();

        StringBuilder inputSb = new StringBuilder();
        inputSb.append(HtmlUtil.createCheckboxInput("", "", "setting-input", checkboxName, "checkbox", "true", "width: 10px", checked));

        tds.add(HtmlUtil.createTableCell(checkboxDisplay, "setting-name", "vertical-align:middle;"));
        tds.add(HtmlUtil.createTableCell(inputSb.toString(), "setting-main", ""));

        StringBuilder sb = new StringBuilder();
        sb.append(HtmlUtil.createTableRow(tds, ""));
        return sb.toString();
    }

    ///////////////
    // PROTECTED //
    ///////////////

    protected String populateMenuBarTableContents(JenkinsConfigContext context) throws UnsupportedEncodingException {
        StringBuilder tdContents = new StringBuilder();
        tdContents.append(HtmlUtil.createH2("Performance Report Display Configuration", "padding-top:20px;padding-left:20px;"));
        String[] pathComponents = StringUtil.splitFilePath(context.getGraphDisplayConfig().getLastPlotDbLocation());
        if(pathComponents.length > 1 || (pathComponents.length == 1 && !pathComponents[0].equals(CommonConstants.ROOT_PATH_ALIAS))) {
            tdContents.append(buildVisualDirectoryPath(pathComponents, context.getGraphDisplayConfig().getPage(), "/"));
            tdContents.append(HtmlConstants.BR);
        }
        tdContents.append(buildOptionsForm(context));
        return tdContents.toString();
    }

    protected String buildMenuBarTable(String tableContents) {
        List<String> tds = new ArrayList<>();
        String td = HtmlUtil.createTableCell(tableContents, "", "");
        tds.add(td);

        List<String> trs = new ArrayList<>();
        String tr = HtmlUtil.createTableRow(tds, "border-top:none; white-space: normal");
        trs.add(tr);

        String table = HtmlUtil.createTable(trs, "graphSettings", "pane", "margin-top:0; border-top:none");

        StringBuilder sb = new StringBuilder();
        sb.append(HtmlUtil.createDiv(table, "", "pane-frame", "", ""));
        return sb.toString();
    }

    protected String buildOptionsForm(JenkinsConfigContext context) {
        List<String> trs = new ArrayList<>();
        trs.add(buildCategoryOption(context.getCategories(), context.getGraphDisplayConfig().getDisplayCategory()));
        trs.add(buildRangeToDisplayOption(context.getGraphDisplayConfig().getDisplayRange()));
        trs.add(buildGraphWidthOption(context.getGraphDisplayConfig().getGraphWidth()));
        trs.add(buildGraphHeightOption(context.getGraphDisplayConfig().getGraphHeight()));
        trs.add(buildGraphsPerRowOption(context.getGraphDisplayConfig().getGraphsPerRow()));
        trs.add(buildRoundToOption(context.getGraphDisplayConfig().getDisplayedDecimalPlaces()));
        trs.add(buildSortOption(context.getGraphDisplayConfig().getSort() == GraphDisplaySort.ASCENDING));

        String submitCellContents = HtmlUtil.createInput("", "", "submit-button primary", "Submit", "submit", "submit");
        List<String> submitRowContents = new ArrayList<>();
        submitRowContents.add(HtmlUtil.createTableCell(submitCellContents, "", ""));
        trs.add(HtmlUtil.createTableRow(submitRowContents, ""));

        StringBuilder formContentsSb = new StringBuilder();

        // This line ensures that the page's current path location will be submitted
        // alongside regular form submissions that come from changing the page's dropdown boxes.
        formContentsSb.append(
            HtmlUtil.createInput(
                "", JenkinsConfigContext.PARAM_PATH,
                "", JenkinsConfigContext.PARAM_PATH,
                "hidden", context.getGraphDisplayConfig().getLastPlotDbLocation())
            );
        formContentsSb.append(JenkinsHtmlUtil.appendCrumbInformation());

        Map<String, String> tableProps = new HashMap<>();
        tableProps.put("width", "100%");
        formContentsSb.append(HtmlUtil.createTable(trs, "", "", "", tableProps));

        StringBuilder sb = new StringBuilder();
        sb.append(HtmlUtil.createForm(formContentsSb.toString(), "frmOptions", "post", "", "off"));
        return sb.toString();
    }

    protected String buildCategoryOption(Set<String> categories, String selectedCategory) {
        return buildOptionCombo(
            JenkinsConfigContext.PARAM_CATEGORY,
            "Category:  ",
            categories.toArray(new String[categories.size()]), selectedCategory,
            200
        );
    }

    protected String buildRangeToDisplayOption(int timeScale) {
        return buildTextField(
            JenkinsConfigContext.PARAM_TIME_SCALE,
            "Range to Display: ",
            Integer.toString(timeScale),
            200
        );
    }

    protected String buildGraphWidthOption(int graphWidth) {
        return buildTextField(
            JenkinsConfigContext.PARAM_GRAPH_WIDTH,
            "Graph Width: ",
            Integer.toString(graphWidth),
            200
        );
    }

    protected String buildGraphHeightOption(int graphHeight) {
        return buildTextField(
            JenkinsConfigContext.PARAM_GRAPH_HEIGHT,
            "Graph Height: ",
            Integer.toString(graphHeight),
            200
        );
    }

    protected String buildGraphsPerRowOption(int graphsPerRow) {
        return buildTextField(
            JenkinsConfigContext.PARAM_GRAPHS_PER_ROW,
            "Graphs Per Row: ",
            Integer.toString(graphsPerRow),
            200
        );
    }

    protected String buildRoundToOption(int roundTo) {
        return buildTextField(
            JenkinsConfigContext.PARAM_ROUND_TO,
            "Number of Displayed Decimal Places: ",
            Integer.toString(roundTo),
            200
        );
    }

    protected String buildSortOption(boolean shouldSortAscending) {
        return buildCheckboxField(
            JenkinsConfigContext.PARAM_SORT_ASCENDING,
            "Sort ascending: ",
            shouldSortAscending
        );
    }

    protected String buildVisualDirectoryPath(
            String[] pathComponents, final int currentPage, String displayedLevelSeparator) throws UnsupportedEncodingException {
        final String visualSplit = " / ";

        StringBuilder pSb = new StringBuilder();
        pSb.append(HtmlUtil.createLink(HtmlUtil.createGraphLinkParameterList(CommonConstants.ROOT_PATH_ALIAS, currentPage), "Home"));

        StringBuilder buildPath = new StringBuilder();
        for (int i = 0; i < pathComponents.length; i++) {
            String pathComponent = pathComponents[i];
            if (StringUtils.isBlank(pathComponent)) {
                continue;
            }

            buildPath.append(pathComponent).append(displayedLevelSeparator);

            pSb.append(visualSplit);
            if (i < pathComponents.length - 1) {
                pSb.append(HtmlUtil.createLink(HtmlUtil.createGraphLinkParameterList(StringUtil.encode(buildPath.toString()), currentPage), pathComponent));
            } else {
                // The last part of the displayed path does not have a link.
                pSb.append(pathComponent);
            }
        }

        StringBuilder pStyleSb = new StringBuilder();
        pStyleSb.append("border-bottom: 2px #000000 solid; ");
        pStyleSb.append("margin-bottom: 5px; ");
        pStyleSb.append("margin-left: 10px; ");
        pStyleSb.append("font-size: 16pt; ");
        pStyleSb.append("font-weight: bold");

        StringBuilder sb = new StringBuilder();
        sb.append(HtmlUtil.createP( pSb.toString(), pStyleSb.toString() ));
        sb.append(HtmlConstants.BR);

        return sb.toString();
    }

    protected String buildPagingLinks(int currentPage, int numberOfPages, String graphSelectedPath) throws UnsupportedEncodingException {
        StringBuilder sb = new StringBuilder();
        StringBuilder pStyleSb = new StringBuilder();
        pStyleSb.append("border-bottom: 1px #DDDDDD solid;");
        pStyleSb.append("margin-bottom: 5px;");
        pStyleSb.append("margin-left: 10px;");
        pStyleSb.append("font-size: 14pt;");
        pStyleSb.append("font-weight: bold;");
        pStyleSb.append("font-variant: small-caps");
        sb.append(HtmlUtil.createP("Pages", pStyleSb.toString()));
        
        StringBuilder mainGraphPSb = new StringBuilder();
        for(int i = 1; i <= numberOfPages; i++) {
            if(i == currentPage) {
                mainGraphPSb.append(JenkinsHtmlUtil.createStrong(Integer.toString(i)));
            } else {
                mainGraphPSb.append(buildPageLink(graphSelectedPath, i, Integer.toString(i)));
            }
            if(i < numberOfPages) {
                mainGraphPSb.append(" | ");
            }
        }
        sb.append(HtmlUtil.createP(mainGraphPSb.toString(), "margin-left:10px"));
        return sb.toString();
    }

    protected String buildPageLink(String path, int newPage, String pageLinkText) throws UnsupportedEncodingException {
        StringBuilder sb = new StringBuilder();
        sb.append(HtmlUtil.createLink(HtmlUtil.createGraphLinkParameterList(path, newPage), pageLinkText));
        sb.append(HtmlUtil.createInput("", "", "", "page", "hidden", Integer.toString(newPage)));
        return sb.toString();
    }
}