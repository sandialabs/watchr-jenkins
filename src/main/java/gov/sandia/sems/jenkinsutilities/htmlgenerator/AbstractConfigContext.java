/*******************************************************************************
* Watchr
* ------
* Copyright 2021 National Technology & Engineering Solutions of Sandia, LLC (NTESS).
* Under the terms of Contract DE-NA0003525 with NTESS, the U.S. Government retains
* certain rights in this software.
******************************************************************************/
package gov.sandia.sems.jenkinsutilities.htmlgenerator;

import java.io.UnsupportedEncodingException;

import gov.sandia.sems.jenkins.semsjppplugin.CommonConstants;
import gov.sandia.sems.jenkinsutilities.utilities.StaplerRequestUtil;
import gov.sandia.sems.jenkinsutilities.utilities.UrlUtil;

/**
 * A state model for the Performance Report Views page.
 * 
 * @author Elliott Ridgway
 */
public class AbstractConfigContext {

    ////////////
    // FIELDS //
    ////////////

    protected String  graphSelectedPath       = ""; //$NON-NLS-1$
    protected String  graphSelectedMeasurable = HtmlConstants.PARAM_DFLT_MEASURABLE;
    protected int     graphSelectedTimeScale  = HtmlConstants.PARAM_DFLT_TIME_SCALE;
    protected int     graphWidth              = HtmlConstants.PARAM_DFLT_GRAPH_WIDTH;
    protected int     graphHeight             = HtmlConstants.PARAM_DFLT_GRAPH_HEIGHT;
    protected int     graphsPerRow            = HtmlConstants.PARAM_DFLT_GRAPHS_PER_ROW;
    protected int     roundTo                 = HtmlConstants.PARAM_DFLT_ROUND_TO;
    private boolean   showDescendants         = HtmlConstants.PARAM_DFLT_SHOW_DESCENDANTS;
    private boolean   showAvgLine             = HtmlConstants.PARAM_DFLT_SHOW_AVG_LINE;
    private boolean   showStdDevLine          = HtmlConstants.PARAM_DFLT_SHOW_STD_DEV_LINE;
    private int       page                    = HtmlConstants.PARAM_DFLT_PAGE;

    /////////////////
    // CONSTRUCTOR //
    /////////////////

    protected AbstractConfigContext(String defaultMeasurable) {
        graphSelectedMeasurable = defaultMeasurable;
    }

    /////////////
    // GETTERS //
    /////////////

    public String getGraphSelectedPath() {
        return graphSelectedPath;
    }
    
    public String getGraphSelectedMeasurable() {
        return graphSelectedMeasurable;
    }
    
    public int getGraphSelectedTimeScale() {
        return graphSelectedTimeScale;
    }
    
    public int getGraphWidth() {
        return graphWidth;
    }
    
    public int getGraphHeight() {
        return graphHeight;
    }
    
    public int getGraphsPerRow() {
        return graphsPerRow;
    }
    
    public boolean getShowDescendants() {
        return showDescendants;
    }
    
    public boolean getShowAvgLine() {
        return showAvgLine;
    }
    
    public boolean getShowStdDevLine() {
        return showStdDevLine;
    }

    public int getRoundTo() {
        return roundTo;
    }

    public int getPage() {
        return page;
    }

    /////////////
    // SETTERS //
    /////////////
    
    public void setGraphSelectedPath(String graphSelectedPath) {
        this.graphSelectedPath = graphSelectedPath;
    }
    
    public void setGraphSelectedMeasurable(String graphSelectedMeasurable) {
        this.graphSelectedMeasurable = graphSelectedMeasurable;
    }
    
    public void setGraphSelectedTimeScale(int graphSelectedTimeScale) {
        this.graphSelectedTimeScale = graphSelectedTimeScale;
    }
    
    public void setGraphWidth(int graphWidth) {
        this.graphWidth = graphWidth;
    }
    
    public void setGraphHeight(int graphHeight) {
        this.graphHeight = graphHeight;
    }
    
    public void setGraphsPerRow(int graphsPerRow) {
        this.graphsPerRow = graphsPerRow;
    }
    
    public void setShowDescendants(boolean showDescendants) {
        this.showDescendants = showDescendants;
    }
    
    public void setShowAvgLine(boolean showAvgLine) {
        this.showAvgLine = showAvgLine;
    }
    
    public void setShowStdDevLine(boolean showStdDevLine) {
        this.showStdDevLine = showStdDevLine;
    }

    public void setRoundTo(int roundTo) {
        this.roundTo = roundTo;
    }

    public void setPage(int page) {
        this.page = page;
    }

    /////////////
    // UTILITY //
    /////////////
    
    public void updateHtmlGeneratorSettingsFromParameterList() throws UnsupportedEncodingException {
        String path_ =
            UrlUtil.getProcessedURL(StaplerRequestUtil.parseStringParameter(HtmlConstants.PARAM_PATH, ""));
        String measurable_ =
            StaplerRequestUtil.parseStringParameter(HtmlConstants.PARAM_MEASURABLE, getGraphSelectedMeasurable());
        int timeScale_ =
            StaplerRequestUtil.parseIntParameter(HtmlConstants.PARAM_TIME_SCALE, getGraphSelectedTimeScale());
        int graphWidth_ =
            StaplerRequestUtil.parseIntParameter(HtmlConstants.PARAM_GRAPH_WIDTH, getGraphWidth());
        int graphHeight_ =
            StaplerRequestUtil.parseIntParameter(HtmlConstants.PARAM_GRAPH_HEIGHT, getGraphHeight());
        int graphsPerRow_ =
            StaplerRequestUtil.parseIntParameter(HtmlConstants.PARAM_GRAPHS_PER_ROW, getGraphsPerRow());
        int roundTo_ =
            StaplerRequestUtil.parseIntParameter(HtmlConstants.PARAM_ROUND_TO, getRoundTo());
        boolean showDescendants_ =
            StaplerRequestUtil.parseBooleanParameter(HtmlConstants.PARAM_SHOW_DESCENDANTS, getShowDescendants());
        boolean showAvgLine_ =    
            StaplerRequestUtil.parseBooleanParameter(HtmlConstants.PARAM_SHOW_AVG_LINE, getShowAvgLine());
        boolean showStdDevLine_ =
            StaplerRequestUtil.parseBooleanParameter(HtmlConstants.PARAM_SHOW_STD_DEV_LINE, getShowStdDevLine());
        int page_ =
            StaplerRequestUtil.parseIntParameter(HtmlConstants.PARAM_PAGE, getPage());
        
        if(StaplerRequestUtil.parameterValueExists(HtmlConstants.PARAM_PATH) && !getGraphSelectedPath().equals(path_)) {
            if(path_.equals(CommonConstants.ROOT_PATH_ALIAS)) {
                setGraphSelectedPath("");
            } else {
                setGraphSelectedPath(path_);
            }
        }

        if(StaplerRequestUtil.parameterValueExists(HtmlConstants.PARAM_MEASURABLE) && !getGraphSelectedMeasurable().equals(measurable_)) {
            setGraphSelectedMeasurable(measurable_);
        }

        if(StaplerRequestUtil.parameterValueExists(HtmlConstants.PARAM_TIME_SCALE) && getGraphSelectedTimeScale() != timeScale_) {
            setGraphSelectedTimeScale(timeScale_);
        }

        if(StaplerRequestUtil.parameterValueExists(HtmlConstants.PARAM_GRAPH_WIDTH) && getGraphWidth() != graphWidth_) {
            setGraphWidth(graphWidth_);
        }

        if(StaplerRequestUtil.parameterValueExists(HtmlConstants.PARAM_GRAPH_HEIGHT) && getGraphHeight() != graphHeight_) {
            setGraphHeight(graphHeight_);
        }

        if(StaplerRequestUtil.parameterValueExists(HtmlConstants.PARAM_GRAPHS_PER_ROW) && getGraphsPerRow() != graphsPerRow_) {
            setGraphsPerRow(graphsPerRow_);
        }

        if(StaplerRequestUtil.parameterValueExists(HtmlConstants.PARAM_ROUND_TO) && getRoundTo() != roundTo_) {
            setRoundTo(roundTo_);
        }

        if(StaplerRequestUtil.parameterValueExists(HtmlConstants.PARAM_PAGE) && getPage() != page_) {
            setPage(page_);
        }

        // Note:  We can use "measurable" as an indicator that a full parameter list was sent,
        // and therefore we should consider changes to the following boolean settings.  It's a
        // bit of a cheat to help us get around the fact that we can't distinguish between
        // a boolean parameter being "false" vs. simply being absent from the parameter list.
        if(StaplerRequestUtil.parameterValueExists(HtmlConstants.PARAM_MEASURABLE) && getShowDescendants() != showDescendants_) {
            setShowDescendants(showDescendants_);
        }

        if(StaplerRequestUtil.parameterValueExists(HtmlConstants.PARAM_MEASURABLE) && getShowAvgLine() != showAvgLine_) {
            setShowAvgLine(showAvgLine_);
        }

        if(StaplerRequestUtil.parameterValueExists(HtmlConstants.PARAM_MEASURABLE) && getShowStdDevLine() != showStdDevLine_) {
            setShowStdDevLine(showStdDevLine_);
        }
    }    
}