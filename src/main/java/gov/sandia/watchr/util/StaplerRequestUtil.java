/*******************************************************************************
* Watchr
* ------
* Copyright 2021 National Technology & Engineering Solutions of Sandia, LLC (NTESS).
* Under the terms of Contract DE-NA0003525 with NTESS, the U.S. Government retains
* certain rights in this software.
******************************************************************************/
package gov.sandia.watchr.util;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.kohsuke.stapler.Stapler;
import org.kohsuke.stapler.StaplerRequest;

import gov.sandia.watchr.config.GraphDisplayConfig;
import gov.sandia.watchr.config.GraphDisplayConfig.GraphDisplaySort;
import gov.sandia.watchr.model.JenkinsConfigContext;

/**
 *
 * @author Elliott Ridgway
 */
public class StaplerRequestUtil {

    private StaplerRequestUtil() {}

    public static void updateGraphDisplayConfigFromParameterList(
            GraphDisplayConfig graphDisplayConfig) throws UnsupportedEncodingException {
                
        String path_ = UrlUtil.getProcessedURL(parseStringParameter(JenkinsConfigContext.PARAM_PATH, ""));
        String category_ = parseStringParameter(JenkinsConfigContext.PARAM_CATEGORY, graphDisplayConfig.getDisplayCategory());
        int timeScale_ = parseIntParameter(JenkinsConfigContext.PARAM_TIME_SCALE, graphDisplayConfig.getDisplayRange());
        int graphWidth_ = parseIntParameter(JenkinsConfigContext.PARAM_GRAPH_WIDTH, graphDisplayConfig.getGraphWidth());
        int graphHeight_ = parseIntParameter(JenkinsConfigContext.PARAM_GRAPH_HEIGHT, graphDisplayConfig.getGraphHeight());
        int graphsPerRow_ = parseIntParameter(JenkinsConfigContext.PARAM_GRAPHS_PER_ROW, graphDisplayConfig.getGraphsPerRow());
        int roundTo_ = parseIntParameter(JenkinsConfigContext.PARAM_ROUND_TO, graphDisplayConfig.getDisplayedDecimalPlaces());
        int page_ = parseIntParameter(JenkinsConfigContext.PARAM_PAGE, graphDisplayConfig.getPage());

        boolean configSortAsecnding = graphDisplayConfig.getSort() == GraphDisplaySort.ASCENDING;
        boolean sortAscending_ = parseBooleanParameter(JenkinsConfigContext.PARAM_SORT_ASCENDING, configSortAsecnding);
        
        if(parameterValueExists(JenkinsConfigContext.PARAM_PATH) && !graphDisplayConfig.getNextPlotDbLocation().equals(path_)) {
            graphDisplayConfig.setNextPlotDbLocation(path_);
        }

        if(parameterValueExists(JenkinsConfigContext.PARAM_CATEGORY) && !graphDisplayConfig.getDisplayCategory().equals(category_)) {
            graphDisplayConfig.setDisplayCategory(category_);

            // Reset to page 1 if the category is changed.
            page_ = 1;
            graphDisplayConfig.setPage(page_);
        }

        if(parameterValueExists(JenkinsConfigContext.PARAM_TIME_SCALE) && graphDisplayConfig.getDisplayRange() != timeScale_) {
            graphDisplayConfig.setDisplayRange(timeScale_);
        }

        if(parameterValueExists(JenkinsConfigContext.PARAM_GRAPH_WIDTH) && graphDisplayConfig.getGraphWidth() != graphWidth_) {
            graphDisplayConfig.setGraphWidth(graphWidth_);
        }

        if(parameterValueExists(JenkinsConfigContext.PARAM_GRAPH_HEIGHT) && graphDisplayConfig.getGraphHeight() != graphHeight_) {
            graphDisplayConfig.setGraphHeight(graphHeight_);
        }

        if(parameterValueExists(JenkinsConfigContext.PARAM_GRAPHS_PER_ROW) && graphDisplayConfig.getGraphsPerRow() != graphsPerRow_) {
            graphDisplayConfig.setGraphsPerRow(graphsPerRow_);
        }

        if(parameterValueExists(JenkinsConfigContext.PARAM_ROUND_TO) && graphDisplayConfig.getDisplayedDecimalPlaces() != roundTo_) {
            graphDisplayConfig.setDisplayedDecimalPlaces(roundTo_);
        }

        if(parameterValueExists(JenkinsConfigContext.PARAM_PAGE) && graphDisplayConfig.getPage() != page_) {
            graphDisplayConfig.setPage(page_);
        }

        // Note:  We can use "category" as an indicator that a full parameter list was sent,
        // and therefore we should consider changes to the following boolean settings.  It's a
        // bit of a cheat to help us get around the fact that we can't distinguish between
        // a boolean parameter being "false" vs. simply being absent from the parameter list.
        if(StaplerRequestUtil.parameterValueExists(JenkinsConfigContext.PARAM_CATEGORY) && sortAscending_ != configSortAsecnding) {
            if(sortAscending_) {
                graphDisplayConfig.setSort(GraphDisplaySort.ASCENDING);
            } else {
                graphDisplayConfig.setSort(GraphDisplaySort.DESCENDING);
            }
        }
    }

    public static String getDeletedPlotNameFromParameterList() throws UnsupportedEncodingException {
        if(parameterValueExists(JenkinsConfigContext.PARAM_DELETE_NAME)) {
            return UrlUtil.getProcessedURL(parseStringParameter(JenkinsConfigContext.PARAM_DELETE_NAME, null));
        }
        return null;
    }

    public static String getDeletedPlotCategoryFromParameterList() throws UnsupportedEncodingException {
        if(parameterValueExists(JenkinsConfigContext.PARAM_DELETE_CATEGORY)) {
            return UrlUtil.getProcessedURL(parseStringParameter(JenkinsConfigContext.PARAM_DELETE_CATEGORY, null));
        }
        return null;
    }
    
    public static String echoCurrentStaplerRequest() {
        StringBuilder sb = new StringBuilder();
        sb.append(Stapler.getCurrentRequest().getParameterMap().toString());
        sb.append("\r\nParameter Names:");
        List<String> parameterNames = Collections.list(Stapler.getCurrentRequest().getParameterNames());
        for(String parameterName : parameterNames) {
            sb.append(parameterName).append(", ");
        }
        return sb.toString();
    }
    
    public static boolean isRequestParameterMapEmpty() {
        StaplerRequest request = Stapler.getCurrentRequest();
        return request.getParameterMap().isEmpty();
    }
    
    public static String parseStringParameter(String paramName, String dflt) {       
        StaplerRequest request = Stapler.getCurrentRequest();
        if(request != null) {
            String[] values = request.getParameterValues(paramName);
            if(values != null && values.length > 0) {
                String value = request.getParameterValues(paramName)[0];
                return !StringUtils.isBlank(value) ? value : dflt;
            }
        }
        return dflt;
    }
        
    public static int parseIntParameter(String paramName, int dflt) {
        StaplerRequest request = Stapler.getCurrentRequest();
        if(request != null) {
            String[] values = request.getParameterValues(paramName);
            if(values != null && values.length > 0) {
                String value = request.getParameterValues(paramName)[0];
                if(!StringUtils.isBlank(value)) {
                    return Integer.parseInt(value);
                }
            }
        }
        return dflt;
    }
    
    public static Boolean parseBooleanParameter(String paramName, boolean dflt) {
        StaplerRequest request = Stapler.getCurrentRequest();
        if(request != null && !request.getParameterMap().isEmpty()) {
            String[] values = request.getParameterValues(paramName);
            if(values != null && values.length > 0) {
                String value = values[0];
                return !StringUtils.isBlank(value) ? (value.equals("on") || value.equals("true")) : dflt;
            }
        }
        return false;
    }
    
    public static boolean parameterNameExists(String paramName) {
        StaplerRequest request = Stapler.getCurrentRequest();
        if(request != null && !request.getParameterMap().isEmpty()) {
            List<String> parameterNames = Collections.list(request.getParameterNames());
            return parameterNames.contains(paramName);
        }
        return false;
    }
    
    public static boolean parameterValueExists(String paramName) {
        StaplerRequest request = Stapler.getCurrentRequest();
        if(request != null && !request.getParameterMap().isEmpty()) {
            String[] values = request.getParameterValues(paramName);
            return (values != null && values.length > 0);
        }
        return false;
    }

    public static List<String> findStringParameter(String regex) {
        StaplerRequest request = Stapler.getCurrentRequest();
        List<String> matches = new ArrayList<>();
        if(request != null) {
            Enumeration<String> paramNames = request.getParameterNames();
            while(paramNames.hasMoreElements()) {
                String nextParameterName = paramNames.nextElement();
                if(nextParameterName.matches(regex)) {
                    matches.add(nextParameterName);
                }
            }
        }
        return matches;
    }
}