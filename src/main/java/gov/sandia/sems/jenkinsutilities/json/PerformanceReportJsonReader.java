/*******************************************************************************
* Watchr
* ------
* Copyright 2021 National Technology & Engineering Solutions of Sandia, LLC (NTESS).
* Under the terms of Contract DE-NA0003525 with NTESS, the U.S. Government retains
* certain rights in this software.
******************************************************************************/
package gov.sandia.sems.jenkinsutilities.json;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.math.NumberUtils;

import gov.sandia.sems.jenkins.semsjppplugin.Keywords;
import gov.sandia.sems.jenkinsutilities.xml.MetaData;
import gov.sandia.sems.jenkinsutilities.xml.Metric;
import gov.sandia.sems.jenkinsutilities.xml.PerformanceReport;
import gov.sandia.sems.jenkinsutilities.xml.TimingBlock;
import gov.sandia.sems.jenkinsutilities.xml.XmlDataElement;

/**
 * This class is responsible for converting JSON-formatted performance report
 * files into {@link PerformanceReport} objects.<br><br>The JSON files deserialized
 * by this class must follow the "standard" schema for this project.  Other
 * JSON formats (such as Trilinos-formatted reports) must first be run through
 * an adapter class before being handed to this class (see
 * {@link TrilinosPerformanceReportAdapter}) as an example.
 * 
 * @author Elliott Ridgway
 */
public class PerformanceReportJsonReader {

    ////////////
    // FIELDS //
    ////////////

    private static final String JSON_KEY_DATE     = "date";
    private static final String JSON_KEY_METADATA = "metadata";
    private static final String JSON_KEY_NAME     = "name";
    private static final String JSON_KEY_METRICS  = "metrics";
    private static final String JSON_KEY_TIMINGS  = "timings";
    private static final String JSON_KEY_UNITS    = "units";
    private static final String JSON_KEY_PERFORMANCE_REPORT = "performanceReport";

    ////////////
    // PUBLIC //
    ////////////

    /**
     * Convert a JSON {@link File} to a {@link PerformanceReport} object.
     * @param jsonFile The JSON file.  Must follow "standard" schema for
     * this plugin.
     * @return The PerformanceReport containing all the data from the JSON.
     * @throws IOException Thrown if there was an error reading the file.
     */
    public PerformanceReport deserialize(File jsonFile) throws IOException {
        return deserialize(FileUtils.readFileToString(jsonFile));
    }

    /**
     * Convert an {@link InputStream} from a JSON file to a {@link PerformanceReport} object.
     * @param is The input stream of the JSON file.  The JSON file data must
     * follow "standard" schema for this plugin.
     * @return The PerformanceReport containing all the data from the JSON.
     * @throws IOException Thrown if there was an error reading the file.
     */
    public PerformanceReport deserialize(InputStream is) throws IOException {
        StringWriter writer = new StringWriter();
        IOUtils.copy(is, writer, StandardCharsets.UTF_8); // UTF-8 is a safe assumption
        return deserialize(writer.toString());
    }

    /**
     * Convert a {@link String} of JSON file contents to a {@link PerformanceReport} object.
     * @param jsonFileContents The JSON file contents.  The JSON file data must
     * follow "standard" schema for this plugin.
     * @return The PerformanceReport containing all the data from the JSON.
     */
    public PerformanceReport deserialize(String jsonFileContents) {
        JsonObject jsonObject = new JsonParser().parse(jsonFileContents).getAsJsonObject();

        Set<Map.Entry<String, JsonElement>> entrySet = jsonObject.entrySet();
        Iterator<Map.Entry<String, JsonElement>> entrySetIterator = entrySet.iterator();
        while(entrySetIterator.hasNext()) {
            Map.Entry<String, JsonElement> entry = entrySetIterator.next();
            if(entry.getKey().equals(JSON_KEY_PERFORMANCE_REPORT) &&
               entry.getValue() instanceof JsonObject) {
                return getPerformanceReport((JsonObject) entry.getValue());
            }
        }

        return null;
    }

    /////////////
    // PRIVATE //
    /////////////

    private PerformanceReport getPerformanceReport(JsonObject jsonObject) {
        XmlDataElement performanceReport = getChildElement(Keywords.CATEGORY_PERFORMANCE_REPORT, jsonObject);
        if(performanceReport instanceof PerformanceReport) {
            return (PerformanceReport) performanceReport;
        }
        return null;
    }

    private XmlDataElement getChildElement(Keywords type, JsonObject jsonObject) {
        String name = "";
        String date = "";
        String units = "";
        List<XmlDataElement> timings = new ArrayList<>();
        List<XmlDataElement> metrics = new ArrayList<>();
        List<XmlDataElement> metadata = new ArrayList<>();
        Map<String, Double> attributes = new HashMap<>();

        Set<Map.Entry<String, JsonElement>> entrySet = jsonObject.entrySet();
        Iterator<Map.Entry<String, JsonElement>> entrySetIterator = entrySet.iterator();
        while(entrySetIterator.hasNext()) {
            Map.Entry<String, JsonElement> entry = entrySetIterator.next();
            if(entry.getKey().equals(JSON_KEY_NAME) ) {
                name = entry.getValue().getAsString();
            } else if(entry.getKey().equals(JSON_KEY_DATE) ) {
                date = entry.getValue().getAsString();
            } else if(entry.getKey().equals(JSON_KEY_UNITS) ) {
                units = entry.getValue().getAsString();
            } else if(entry.getKey().equals(JSON_KEY_TIMINGS) && entry.getValue() instanceof JsonArray ) {
                timings.addAll(getChildArray(Keywords.CATEGORY_TIMING, (JsonArray) entry.getValue()));
            } else if(entry.getKey().equals(JSON_KEY_METRICS) && entry.getValue() instanceof JsonArray ) {
                metrics.addAll(getChildArray(Keywords.CATEGORY_METRIC, (JsonArray) entry.getValue()));
            } else if(entry.getKey().equals(JSON_KEY_METADATA) && entry.getValue() instanceof JsonObject ) {
                metadata.addAll(getChildElementsAsMetaData((JsonObject) entry.getValue()));
            } else {
                String key = entry.getKey();
                String value = entry.getValue().getAsString();
                if(NumberUtils.isCreatable(value)){
                    attributes.put(key, Double.parseDouble(value));
                }
            }
        }

        XmlDataElement elementToReturn = null;
        if(type == Keywords.CATEGORY_PERFORMANCE_REPORT) {
            elementToReturn = new PerformanceReport(name, date, units);
        } else if(type == Keywords.CATEGORY_TIMING) {
            elementToReturn = new TimingBlock(name, date, units);
        } else if(type == Keywords.CATEGORY_METRIC) {
            elementToReturn = new Metric(name, date, units);
        } else if(type == Keywords.CATEGORY_METADATA) {
            elementToReturn = new MetaData();
        }

        elementToReturn.getChildren().addAll(timings);
        elementToReturn.getChildren().addAll(metrics);
        elementToReturn.getChildren().addAll(metadata);
        elementToReturn.getAttributes().putAll(attributes);

        return elementToReturn;
    }    

    private List<XmlDataElement> getChildArray(Keywords type, JsonArray jsonArray) {
        List<XmlDataElement> elements = new ArrayList<>();
        Iterator<JsonElement> jsonElementIterator = jsonArray.iterator();
        while(jsonElementIterator.hasNext()) {
            JsonElement nextElement = jsonElementIterator.next();
            if(nextElement instanceof JsonObject) {
                JsonObject jsonObject = (JsonObject) nextElement;
                elements.add(getChildElement(type, jsonObject));
            }
        }
        return elements;
    }

    private List<MetaData> getChildElementsAsMetaData(JsonObject parentElement) {
        List<MetaData> metadata = new ArrayList<>();
        Iterator<Map.Entry<String,JsonElement>> jsonElementIterator = parentElement.entrySet().iterator();
        while(jsonElementIterator.hasNext()) {
            Map.Entry<String,JsonElement> nextMetadata = jsonElementIterator.next();
            if(nextMetadata.getValue().isJsonPrimitive()) {
                MetaData newMetaData = new MetaData();
                newMetaData.setName(nextMetadata.getKey());
                newMetaData.setValue(nextMetadata.getValue().getAsString());
                metadata.add(newMetaData);
            }
        }
        return metadata;
    }
}