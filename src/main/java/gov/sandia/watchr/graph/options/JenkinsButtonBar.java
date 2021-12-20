/*******************************************************************************
* Watchr
* ------
* Copyright 2021 National Technology & Engineering Solutions of Sandia, LLC (NTESS).
* Under the terms of Contract DE-NA0003525 with NTESS, the U.S. Government retains
* certain rights in this software.
******************************************************************************/
package gov.sandia.watchr.graph.options;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import gov.sandia.watchr.db.IDatabase;
import gov.sandia.watchr.graph.HtmlUtil;
import gov.sandia.watchr.graph.chartreuse.model.PlotWindowModel;
import gov.sandia.watchr.graph.library.IHtmlButtonRenderer;
import gov.sandia.watchr.log.ILogger;
import gov.sandia.watchr.model.JenkinsConfigContext;
import gov.sandia.watchr.util.FileUtil;

public class JenkinsButtonBar extends AbstractButtonBar {

    ////////////
    // FIELDS //
    ////////////

    public static final String DIVE_IMG_PATH   = "/plugin/watchr-jenkins/dive.png";
    public static final String DELETE_IMG_PATH = "/plugin/watchr-jenkins/delete.png";

    private final ILogger logger;

    /////////////////
    // CONSTRUCTOR //
    /////////////////

    public JenkinsButtonBar(IHtmlButtonRenderer buttonRenderer, ILogger logger) {
        super(buttonRenderer);
        this.logger = logger;
    }

    //////////////
    // OVERRIDE //
    //////////////

    @Override
    public String getHtmlForButton(PlotWindowModel plot, ButtonType buttonType) {
        if(buttonType == ButtonType.GO_TO_CHILD_GRAPH) {
            return getChildNavigationButton(plot);
        } else if(buttonType == ButtonType.DELETE) {
            return getDeleteButton(plot);
        }
        return "";
    }

    /////////////
    // PRIVATE //
    /////////////

    private String getChildNavigationButton(PlotWindowModel plot) {
        IDatabase db = getButtonRenderer().getDatabase();
        Map<String,String> params = new HashMap<>();
        try {
            params = getParameterMapForChildButton(db, plot);
        } catch(UnsupportedEncodingException e) {
            logger.logError("An error occurred parsing parameters for the plot " + plot.getName(), e);
        }

        if(!params.isEmpty()) {
            String img = HtmlUtil.createImage(DIVE_IMG_PATH, 32, 32);
            String link = HtmlUtil.createLink(HtmlUtil.createParameterList(params), img);
            return HtmlUtil.createButton("submit", link);
        } else {
            return "";
        }
    }

    private String getDeleteButton(PlotWindowModel plot) {
        StringBuilder sb = new StringBuilder();

        String cleanPlotName = "";
        try {
            cleanPlotName = FileUtil.removeIllegalCharactersFromFilename(escapePlotName(plot));
        } catch(UnsupportedEncodingException e) {
            logger.logError("An error occurred removing illegal characters from the name " + plot.getName(), e);
        }
        cleanPlotName = cleanPlotName.replace(".", "_");
        cleanPlotName = cleanPlotName.replace("-", "_");
        String functionName = "delete_" + cleanPlotName + "()";

        Map<String,String> params = new HashMap<>();
        params.put(JenkinsConfigContext.PARAM_DELETE_NAME, plot.getName());
        params.put(JenkinsConfigContext.PARAM_DELETE_CATEGORY, plot.getCategory());
        sb.append(HtmlUtil.createButton("submit", functionName, HtmlUtil.createImage(DELETE_IMG_PATH, 32, 32)));

        sb.append("<script type=\"text/javascript\">");
        sb.append("function ").append(functionName).append(" {");
        sb.append("    var optsel = confirm(\"Are you sure you want to delete this plot and all its children?\");");
        sb.append("    if (optsel == true) {");
        sb.append("        window.location.href = '").append(HtmlUtil.createParameterList(params)).append("';");
        sb.append("    }");
        sb.append("}");
        sb.append("</script>");

        return sb.toString();
    }
    
}
