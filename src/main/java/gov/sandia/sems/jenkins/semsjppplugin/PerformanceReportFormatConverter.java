/*******************************************************************************
* Watchr
* ------
* Copyright 2021 National Technology & Engineering Solutions of Sandia, LLC (NTESS).
* Under the terms of Contract DE-NA0003525 with NTESS, the U.S. Government retains
* certain rights in this software.
******************************************************************************/
package gov.sandia.sems.jenkins.semsjppplugin;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import com.google.gson.JsonObject;

import org.apache.commons.io.FilenameUtils;
import org.xml.sax.SAXException;

import gov.sandia.sems.jenkinsutilities.json.PerformanceReportJsonReader;
import gov.sandia.sems.jenkinsutilities.json.PyomoPerformanceReportAdapter;
import gov.sandia.sems.jenkinsutilities.json.TrilinosPerformanceReportAdapter;
import gov.sandia.sems.jenkinsutilities.xml.PerformanceReport;
import gov.sandia.sems.jenkinsutilities.xml.deserializers.SaxPerformanceResultDeserializer;
import hudson.FilePath;

public class PerformanceReportFormatConverter {
    public boolean canParseFile(String performanceReportFile, String performanceReportFormat) {
        final String extension = FilenameUtils.getExtension(performanceReportFile);

        if(performanceReportFormat.equals(PerformanceReportFormat.XML.toString())) {
            return extension.equalsIgnoreCase("xml");
        } else if(performanceReportFormat.equals(PerformanceReportFormat.JSON_STANDARD.toString())) {
            return extension.equalsIgnoreCase("json");
        } else if(performanceReportFormat.equals(PerformanceReportFormat.JSON_TRILINOS.toString())) {
            return extension.equalsIgnoreCase("json");
        } else if(performanceReportFormat.equals(PerformanceReportFormat.JSON_PYOMO.toString())) {
            return extension.equalsIgnoreCase("json");
        }
        return false;
    }

    public List<PerformanceReport> parsePerformanceReport(
            FilePath childFilePath, String performanceReportFormat)
            throws InterruptedException, IOException, SAXException, ParserConfigurationException {
        List<PerformanceReport> parsedReports = new ArrayList<>();

        if(performanceReportFormat.equals(PerformanceReportFormat.XML.toString())) {    
            PerformanceReport performanceReport = parseXmlPerformanceReport(childFilePath);
            if(performanceReport != null) {
                parsedReports.add(performanceReport);
            }
        } else if(performanceReportFormat.equals(PerformanceReportFormat.JSON_STANDARD.toString())) {
            PerformanceReport performanceReport = parseJsonStandardPerformanceReport(childFilePath);
            if(performanceReport != null) {
                parsedReports.add(performanceReport);
            }
        } else if(performanceReportFormat.equals(PerformanceReportFormat.JSON_TRILINOS.toString())) {
            List<PerformanceReport> performanceReports = parseJsonTrilinosPerformanceReports(childFilePath);
            parsedReports.addAll(performanceReports);
        } else if(performanceReportFormat.equals(PerformanceReportFormat.JSON_PYOMO.toString())) {
            List<PerformanceReport> performanceReports = parseJsonPyomoPerformanceReports(childFilePath);
            parsedReports.addAll(performanceReports);
        }

        return parsedReports;
    }

    private PerformanceReport parseXmlPerformanceReport(FilePath childFilePath)
            throws InterruptedException, IOException, SAXException, ParserConfigurationException {
        try(InputStream is = childFilePath.read()) {
            SaxPerformanceResultDeserializer deserializer = new SaxPerformanceResultDeserializer();
            return deserializer.deserialize(is);
        }
    }

    private PerformanceReport parseJsonStandardPerformanceReport(FilePath childFilePath)
            throws IOException, InterruptedException {
        PerformanceReportJsonReader jsonReader = new PerformanceReportJsonReader();
        return jsonReader.deserialize(childFilePath.read());
    }

    private List<PerformanceReport> parseJsonTrilinosPerformanceReports(FilePath childFilePath)
            throws IOException, InterruptedException {
        TrilinosPerformanceReportAdapter adapter = new TrilinosPerformanceReportAdapter();
        List<JsonObject> convertedJsonObjects = adapter.convertMultipleTimestamps(childFilePath.read());
        List<PerformanceReport> performanceReports = new ArrayList<>();

        PerformanceReportJsonReader jsonReader = new PerformanceReportJsonReader();
        for(JsonObject convertedJsonObject : convertedJsonObjects) {
            PerformanceReport performanceReport = jsonReader.deserialize(convertedJsonObject.toString());
            performanceReports.add(performanceReport);
        }
        return performanceReports;
    }

    private List<PerformanceReport> parseJsonPyomoPerformanceReports(FilePath childFilePath)
            throws IOException, InterruptedException {
        PyomoPerformanceReportAdapter adapter = new PyomoPerformanceReportAdapter();
        JsonObject convertedJsonObject = adapter.convert(childFilePath.getName(), childFilePath.read());
        List<PerformanceReport> performanceReports = new ArrayList<>();
        PerformanceReportJsonReader jsonReader = new PerformanceReportJsonReader();
        PerformanceReport performanceReport = jsonReader.deserialize(convertedJsonObject.toString());
        performanceReports.add(performanceReport);
        return performanceReports;
    }
}
