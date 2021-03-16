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
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.kohsuke.stapler.Stapler;

import gov.sandia.sems.jenkins.semsjppplugin.CommonConstants;
import gov.sandia.sems.jenkins.semsjppplugin.Icons;
import gov.sandia.sems.jenkinsutilities.utilities.LogUtil;
import gov.sandia.sems.jenkinsutilities.utilities.StringUtil;
import hudson.Functions;

/**
 * This class is responsible for generating segments of HTML for displaying
 * graph data. This abstract class is extended by multiple concrete HTML
 * generator classes.
 * 
 * @author Elliott Ridgway
 */
public abstract class AbstractHtmlGenerator {

    ////////////
    // FIELDS //
    ////////////

    protected final AbstractConfigContext context;
    protected final String[] measurables;
    protected final String displayedLevelSeparator;
    protected final File logFile;

    /////////////////
    // CONSTRUCTOR //
    /////////////////

    protected AbstractHtmlGenerator(AbstractConfigContext context, String[] measurables, String displayedLevelSeparator, File logFile) {
        this.context = context;
        this.measurables = measurables;
        this.displayedLevelSeparator = displayedLevelSeparator;
        this.logFile = logFile;
    }

    
    ////////////////////////////////
    // HTML GENERATION - MENU BAR //
    ////////////////////////////////

    public String getMenuBar() {
        StringBuilder sb = new StringBuilder();
        sb.append("<div class=\"pane-frame\">\n");
        sb.append("<table id=\"graphSettings\" class=\"pane\" style=\"margin-top:0; border-top:none\">\n");
        sb.append("<tr style=\"border-top:none; white-space: normal\">\n");
        sb.append("<td>\n");
        sb.append("<h2 style=\"padding-top:20px;padding-left:20px;\">Performance Report Display Configuration</h2>");

        String[] pathComponents = StringUtil.splitFilePath(context.getGraphSelectedPath());
        if (pathComponents.length > 0) {
            try {
                sb.append(buildVisualDirectoryPath(pathComponents));
            } catch (UnsupportedEncodingException e) {
                LogUtil.writeErrorToLog(logFile, e);
            }

            try {
                sb.append(buildUpOneLevelLink());
            } catch (UnsupportedEncodingException e) {
                LogUtil.writeErrorToLog(logFile, e);
            }
            sb.append("<br>");
        }

        sb.append("<form method=\"post\" autocomplete=\"off\" name=\"").append(HtmlConstants.GRAPH_OPTIONS_FORM)
                .append("\" action=\"\">\n");
        sb.append("<table width=\"100%\">");

        // This code ensures that the page's current path location will be submitted
        // alongside regular form submissions
        // that come from changing the page's dropdown boxes.
        sb.append("<input type=\"hidden\"");
        sb.append(" id=\"").append(HtmlConstants.PARAM_PATH).append("\"");
        sb.append(" name=\"").append(HtmlConstants.PARAM_PATH).append("\"");
        sb.append(" value=\"").append(context.getGraphSelectedPath()).append("\"");
        sb.append("/>");

        sb.append(appendCrumbInformation());
        
        sb.append(HtmlFragmentGenerator.buildOptionCombo(HtmlConstants.PARAM_MEASURABLE, "Display Measurable:  ", measurables,
                context.getGraphSelectedMeasurable(), 200));
        sb.append(HtmlFragmentGenerator.buildTextField(HtmlConstants.PARAM_TIME_SCALE, "Range to Display: ",
                Integer.toString(context.getGraphSelectedTimeScale()), 200));
        sb.append(HtmlFragmentGenerator.buildTextField(HtmlConstants.PARAM_GRAPH_WIDTH, "Graph Width: ",
                Integer.toString(context.getGraphWidth()), 200));
        sb.append(HtmlFragmentGenerator.buildTextField(HtmlConstants.PARAM_GRAPH_HEIGHT, "Graph Height: ",
                Integer.toString(context.getGraphHeight()), 200));
        sb.append(HtmlFragmentGenerator.buildTextField(HtmlConstants.PARAM_GRAPHS_PER_ROW, "Graphs Per Row: ",
                Integer.toString(context.getGraphsPerRow()), 200));
        sb.append(HtmlFragmentGenerator.buildTextField(HtmlConstants.PARAM_ROUND_TO, "Number of Displayed Decimal Places: ",
                Integer.toString(context.getRoundTo()), 200));                
        sb.append(HtmlFragmentGenerator.buildCheckboxField(HtmlConstants.PARAM_SHOW_DESCENDANTS, "Show graph descendants",
                context.getShowDescendants()));
        sb.append(HtmlConstants.SPACING);
        sb.append(HtmlFragmentGenerator.buildCheckboxField(HtmlConstants.PARAM_SHOW_AVG_LINE, "Show average line",
                context.getShowAvgLine()));
        sb.append(HtmlConstants.SPACING);
        sb.append(HtmlFragmentGenerator.buildCheckboxField(HtmlConstants.PARAM_SHOW_STD_DEV_LINE, "Show standard deviation line",
                context.getShowStdDevLine()));
        sb.append(HtmlConstants.SPACING);

        sb.append("<tr>");
        sb.append("<td colspan=\"4\">");
        sb.append("<input name=\"Submit\" type=\"submit\" value=\"Submit\" class=\"submit-button primary\" />");
        sb.append("</td>");
        sb.append("</tr>");

        sb.append("</table>");
        sb.append("</form>");
        sb.append("</td>");
        sb.append("</tr>");
        sb.append("</table>");
        sb.append("</div>");

        return sb.toString();
    }

    private String buildVisualDirectoryPath(String[] pathComponents) throws UnsupportedEncodingException {
        int currentPage = context.getPage();

        StringBuilder sb = new StringBuilder();
        sb.append("<p style='").append(HtmlConstants.MENU_BAR_TITLE_STYLE).append("'>");
        sb.append("<a href='").append(createParameterList(CommonConstants.ROOT_PATH_ALIAS, currentPage));
        sb.append("'>Home</a>");

        StringBuilder buildPath = new StringBuilder();
        for (int i = 0; i < pathComponents.length; i++) {
            String pathComponent = pathComponents[i];
            if (StringUtils.isBlank(pathComponent)) {
                continue;
            }

            buildPath.append(pathComponent).append(displayedLevelSeparator);

            sb.append(" / ");
            if (i < pathComponents.length - 1) {
                sb.append("<a href='").append(createParameterList(StringUtil.encode(buildPath.toString()), currentPage));
                sb.append("'>");
                sb.append(pathComponent);
                sb.append("</a>");
            } else {
                // The last part of the displayed path does not have a link.
                sb.append(pathComponent);
            }
        }
        sb.append("</p><br>");

        return sb.toString();
    }

    private String buildUpOneLevelLink() throws UnsupportedEncodingException {
        String[] pathComponents = context.getGraphSelectedPath().split(Pattern.quote(displayedLevelSeparator));
        int currentPage = context.getPage();

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < pathComponents.length - 1; i++) {
            sb.append(pathComponents[i]);
            if (i < pathComponents.length - 2) {
                sb.append(displayedLevelSeparator);
            }
        }
        String upOneLevelPath = sb.toString();

        sb = new StringBuilder();
        sb.append("<a style='").append(HtmlConstants.UP_ONE_LEVEL_STYLE).append("' href='");
        sb.append(createParameterList(upOneLevelPath, currentPage)).append("'>");
        sb.append("<img width='32' height='32' src='" + Icons.RETURN_PATH + "'/>Up One Level</a>");
        sb.append("<input type=\"hidden\" name=\"path\" value='");

        if (pathComponents.length == 1) { // i.e. one level down
            sb.append(CommonConstants.ROOT_PATH_ALIAS);
        } else {
            sb.append(StringUtil.encode(upOneLevelPath));
        }
        sb.append("'/>");

        return sb.toString();
    }

    /////////////
    // UTILITY //
    /////////////

    /**
     * Creates the URL parameter list used to display graphs.
     * 
     * @param path The path to use.
     * @param page The page of graphs we're on.
     * @return A URL-style parameter list.
     * @throws UnsupportedEncodingException If there is a bad encoding in the parameter list.
     */
    public static final String createParameterList(String path, int page) throws UnsupportedEncodingException {
        StringBuilder sb = new StringBuilder();
        
        if(StringUtils.isBlank(path)){
            path = CommonConstants.ROOT_PATH_ALIAS;
        }
        
        sb.append("?");
        sb.append(HtmlConstants.PARAM_PATH).append("=").append(StringUtil.encode(path));
        sb.append("&");
        sb.append(HtmlConstants.PARAM_PAGE).append("=").append(page);

        return sb.toString();
    } 

    public static final String appendCrumbInformation() {
        StringBuilder sb = new StringBuilder();
        String crumb = Functions.getCrumb(Stapler.getCurrentRequest());
        String crumbRequestField = Functions.getCrumbRequestField();

        sb.append("<input type=\"hidden\"");
        sb.append(" id=\"Jenkins-Crumb\"");
        sb.append(" name=\"Jenkins-Crumb\"");
        sb.append(" value=\"").append(crumb).append("\"");
        sb.append("/>");

        sb.append("<input type=\"hidden\"");
        sb.append(" id=\"crumbRequestField\"");
        sb.append(" name=\"crumbRequestField\"");
        sb.append(" value=\"").append(crumbRequestField).append("\"");
        sb.append("/>");
        return sb.toString();
    }

    //////////////
    // ABSTRACT //
    //////////////

    /**
     * This method is called from the Javascript to display divs for
     * every graph on the Jenkins page.  It is not responsible for
     * populating those graph divs with data.
     * 
     * @return A String representing the main body of HTML graphs.
     */
    public abstract String getGraphHTML(); 

    /**
     * This method is called from the Javascript that displays the graphs on
     * Jenkins. It is responsible for returning a JSON-structured summary of the
     * graph data.  On the Javascript/HTML side, this JSON structure will
     * populate each graph div with the appropriate data.
     * 
     * @return A JSON structure of graph data, in String form.
     */
    public abstract String getDataFromPath();   
}