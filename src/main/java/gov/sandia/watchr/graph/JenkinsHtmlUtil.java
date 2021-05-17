/*******************************************************************************
* Watchr
* ------
* Copyright 2021 National Technology & Engineering Solutions of Sandia, LLC (NTESS).
* Under the terms of Contract DE-NA0003525 with NTESS, the U.S. Government retains
* certain rights in this software.
******************************************************************************/
package gov.sandia.watchr.graph;

import org.apache.commons.lang3.StringUtils;
import org.kohsuke.stapler.Stapler;

import hudson.Functions;

public class JenkinsHtmlUtil {

    private JenkinsHtmlUtil() {}

    public static String createStrong(String strongContents) {
        StringBuilder sb = new StringBuilder();
        sb.append("<strong>");
        sb.append(strongContents);
        sb.append("</strong>");
        return sb.toString();
    }

    public static String createButton(String buttonContents, String type, String buttonClass) {
        StringBuilder sb = new StringBuilder();
        sb.append("<button");
        if(StringUtils.isNotBlank(type)) sb.append(" type='").append(type).append("'");
        if(StringUtils.isNotBlank(buttonClass))  sb.append(" class='").append(buttonClass).append("'");
        sb.append(">");
        sb.append(buttonContents);
        sb.append("</button>");
        return sb.toString();
    }

    public static final String appendCrumbInformation() {
        StringBuilder sb = new StringBuilder();
        String crumb = Functions.getCrumb(Stapler.getCurrentRequest());
        String crumbRequestField = Functions.getCrumbRequestField();

        sb.append(HtmlUtil.createInput("", "Jenkins-Crumb", "", "Jenkins-Crumb", "hidden", crumb));
        sb.append(HtmlUtil.createInput("", "crumbRequestField", "", "crumbRequestField", "hidden", crumbRequestField));
        return sb.toString();
    }     
}
