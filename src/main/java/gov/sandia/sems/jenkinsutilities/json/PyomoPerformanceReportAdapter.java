/*******************************************************************************
* Watchr
* ------
* Copyright 2021 National Technology & Engineering Solutions of Sandia, LLC (NTESS).
* Under the terms of Contract DE-NA0003525 with NTESS, the U.S. Government retains
* certain rights in this software.
******************************************************************************/
package gov.sandia.sems.jenkinsutilities.json;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.Map.Entry;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import gov.sandia.sems.jenkinsutilities.utilities.DateUtil;

/**
 * Adapts Pyomo-style JSON performance reports into Watchr-style JSON
 * performance reports.
 * 
 * @author Elliott Ridgway
 */
public class PyomoPerformanceReportAdapter {
    
    private static final String TIME = "time";
    private static final String JSON_KEY_METADATA = "metadata";
    private static final String JSON_KEY_PERFORMANCE_REPORT = "performanceReport";
    private static final String JSON_KEY_TIMINGS = "timings";

    private static final Set<String> metadataBlacklist;

    static {
        metadataBlacklist = new LinkedHashSet<>();
        metadataBlacklist.add("hostname");
        metadataBlacklist.add("pyomo_version");
        metadataBlacklist.add("python_implementation");
        metadataBlacklist.add("branch");
        metadataBlacklist.add("platform");
    }

    public JsonObject convert(String fileName, InputStream is) throws IOException {
        StringWriter writer = new StringWriter();
        IOUtils.copy(is, writer, StandardCharsets.UTF_8); // UTF-8 is a safe assumption
        return convert(fileName, writer.toString());
    }

    public JsonObject convert(String fileName, String fileContents) {
        JsonArray pyomoJsonArray = new JsonParser().parse(fileContents).getAsJsonArray();
        JsonObject convertedJsonObject = new JsonObject();
        String pythonVersion = getPythonVersionFromFilename(fileName);
        
        JsonObject performanceReport = new JsonObject();
        List<JsonObject> timings = getWatchrFormattedTimingBlocks(pythonVersion, pyomoJsonArray);
        JsonObject metadata = getWatchrFormattedMetadataJson(pyomoJsonArray);

        JsonArray timingsArray = new JsonArray();
        for(JsonObject timing : timings) {
            timingsArray.add(timing);
        }

        performanceReport.addProperty("name", fileName);
        performanceReport.addProperty("date", getTimestamp(metadata));
        performanceReport.addProperty("units", "seconds");

        performanceReport.add(JSON_KEY_TIMINGS, timingsArray);
        performanceReport.add(JSON_KEY_METADATA, metadata);
        convertedJsonObject.add(JSON_KEY_PERFORMANCE_REPORT, performanceReport);

        return convertedJsonObject;
    }

    /**
     * Get last segment of filename, which contains the Python version attribute.
     * @param filename The filename.
     * @return The Python version attribute.
     */
    /*package*/ String getPythonVersionFromFilename(String filename) {
        int pythonIndex = filename.indexOf("py");
        String pythonSubstring = filename.substring(pythonIndex, filename.length());
        return FilenameUtils.getBaseName(pythonSubstring);
    }

    /*package*/ JsonObject getPyomoFormattedMetadataJson(JsonArray pyomoJsonArray) {
        JsonElement metadataJson = pyomoJsonArray.get(0);
        if(metadataJson instanceof JsonObject) {
            return (JsonObject) metadataJson;
        }
        return null;
    }

    /*package*/ String getTimestamp(JsonObject metadataJson) {
        long epochTimeMillis = (long) metadataJson.get(TIME).getAsFloat() * 1000;
        return DateUtil.epochTimeToTimestamp(epochTimeMillis);
    }

    /*package*/ JsonObject getWatchrFormattedMetadataJson(JsonArray pyomoJsonArray) {
        JsonObject pyomoMetadataJson = getPyomoFormattedMetadataJson(pyomoJsonArray);
        JsonObject watchrMetadataJson = new JsonObject();
        for(Entry<String, JsonElement> entry : pyomoMetadataJson.entrySet()) {
            String key = entry.getKey();
            String value = "";
            JsonElement elementValue = entry.getValue();
            if(elementValue instanceof JsonArray) {
                JsonArray jsonArray = (JsonArray) elementValue;
                StringBuilder arrayAsString = new StringBuilder();

                for(Iterator<JsonElement> iter = jsonArray.iterator(); iter.hasNext(); ) {
                    JsonElement element = iter.next();
                    arrayAsString.append(element);
                    if(iter.hasNext()) {
                        arrayAsString.append(".");
                    }
                }

                value = arrayAsString.toString();
            } else {
                value = elementValue.getAsString();
            }
            if(StringUtils.isNotBlank(value) && !metadataBlacklist.contains(key)) {
                watchrMetadataJson.addProperty(key, value);
            }
        }
        return watchrMetadataJson;
    }

    /*package*/ List<JsonObject> getWatchrFormattedTimingBlocks(String pythonVersion, JsonArray pyomoJsonArray) {
        List<JsonObject> watchrFormattedTimings = new ArrayList<>();
        for(int i = 1; i < pyomoJsonArray.size(); i++) {
            JsonElement nextElement = pyomoJsonArray.get(i);
            if(nextElement instanceof JsonObject) {
                JsonObject nextObject = (JsonObject) nextElement;
                for(Entry<String, JsonElement> parentTiming : nextObject.entrySet()) {
                    String parentTimingName = parentTiming.getKey();
                    if(parentTimingName.contains(".")) {
                        parentTimingName = parentTimingName.substring(parentTimingName.lastIndexOf(".")+1, parentTimingName.length());
                    }

                    JsonElement parentTimingContents = parentTiming.getValue();

                    JsonArray newTimingChildArray = new JsonArray();
                    if(parentTimingContents instanceof JsonObject) {
                        for(Entry<String, JsonElement> childTiming : ((JsonObject)parentTimingContents).entrySet()) {
                            JsonObject newTimingChildObject = new JsonObject();
                            String childTimingName = childTiming.getKey();
                            if(!childTimingName.equals("timing")) {
                                newTimingChildObject.addProperty("name", childTimingName);
                                newTimingChildObject.addProperty(pythonVersion, childTiming.getValue().getAsString());
                                newTimingChildArray.add(newTimingChildObject);
                            }
                        }
                    }

                    JsonObject newTimingJsonObject = new JsonObject();
                    newTimingJsonObject.addProperty("name", parentTimingName);
                    newTimingJsonObject.add("timings", newTimingChildArray);
                    watchrFormattedTimings.add(newTimingJsonObject);
                }
            }
        }
        return watchrFormattedTimings;
    }
}
