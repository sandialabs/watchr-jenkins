/*******************************************************************************
* Watchr
* ------
* Copyright 2021 National Technology & Engineering Solutions of Sandia, LLC (NTESS).
* Under the terms of Contract DE-NA0003525 with NTESS, the U.S. Government retains
* certain rights in this software.
******************************************************************************/
package gov.sandia.sems.jenkinsutilities.htmlexporter;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.apache.commons.io.FileUtils;

import gov.sandia.sems.jenkinsutilities.htmlgenerator.HtmlGenerator;

public class HtmlExporter {
 
    private static final String DATA_FROM_PATH = "$$$DATA_FROM_PATH";
    private static final String GRAPH_HTML = "$$$GRAPH_HTML";

    public void export(File destFolder, HtmlGenerator generator) throws IOException {
        copyPrerequisitePlottingLibraries(destFolder);
        String htmlTemplate = getHtmlTemplate();
        String htmlGenerated = generateHtml(htmlTemplate, generator);
        writeHtmlToFolder(destFolder, "export1.html", htmlGenerated);
    }

    private void copyPrerequisitePlottingLibraries(File destFolder) throws IOException {
        File destJsFolder = new File(destFolder, "js");
        URL jsFolderUrl = getClass().getResource("/js");
        File jsFolder = new File(jsFolderUrl.getFile());
        FileUtils.copyDirectory(jsFolder, destJsFolder);
    }

    private String getHtmlTemplate() throws IOException {
        URL templateUrl = getClass().getResource("/export_template.html");
        File templateFile = new File(templateUrl.getFile());
        return FileUtils.readFileToString(templateFile);
    }

    private String generateHtml(String template, HtmlGenerator generator) {
        String finalHtml = template.replace(DATA_FROM_PATH, generator.getDataFromPath());
        finalHtml = finalHtml.replace(GRAPH_HTML, generator.getGraphHTML());
        return finalHtml;
    }

    private void writeHtmlToFolder(File destFolder, String fileName, String fileContents) throws IOException {
        File destFile = new File(destFolder, fileName);
        FileUtils.writeStringToFile(destFile, fileContents);
    }
}