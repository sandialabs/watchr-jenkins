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
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

/**
 * Adapts Trilinos-style JSON performance reports into Watchr-style JSON
 * performance reports.
 * 
 * Trilinos-style JSON performance reports cannot be mapped one-to-one with
 * Watchr-style JSON performance reports.  Shortcomings to be aware of:
 * 1) The Trilinos "time stamp format" field is discared.
 * 2) The Trilinos "duration" field located at the root of each metrics block
 *    is discarded.  This is due to the fact that it is currently ambiguous
 *    whether the "duration" field should be pushed downwards into each
 *    metric object as a piece of metadata, or pushed upwards to the parent
 *    object's metadata.  Any other non-group JSON element at this level will
 *    be similarly discarded.
 * 
 * @author Elliott Ridgway
 */
public class TrilinosPerformanceReportAdapter {

    ////////////
    // FIELDS //
    ////////////

    // Keys shared by Watchr format and Trilinos format
    private static final String JSON_KEY_NAME = "name";
    private static final String JSON_KEY_METRICS  = "metrics";
    private static final String JSON_KEY_METADATA = "metadata";
    
    // Unique to Watchr format
    private static final String JSON_KEY_DATE = "date";
    private static final String JSON_KEY_PERFORMANCE_REPORT = "performanceReport";
    private static final String JSON_KEY_TIMINGS = "timings";
    private static final String JSON_KEY_UNITS = "units";

    // Unique to Trilinos format
    private static final String JSON_KEY_TIME_STAMP        = "time stamp";
    private static final String JSON_KEY_TIME_STAMP_FORMAT = "time stamp format";

    private int unnamedChildrenCount = 0;

    ////////////
    // PUBLIC //
    ////////////

    /**
     * Convert a Trilinos JSON {@link File} into a {@link List} of {@link JsonObject}s
     * that are in the standard performance report format.  A Trilinos JSON file can
     * contain many top-level JsonObjects that represent unique pairings of
     * timings and metrics - hence, this method returns one JsonObject per timestamp.
     * @param jsonFile The JSON File to read.
     * @return The List of identified JsonObjects that contain standard format for
     * the performance report data.  Each JsonObject represents a unique
     * timestamp.
     * @throws IOException Thrown if there was an error reading the file.
     */
    public List<JsonObject> convertMultipleTimestamps(File jsonFile) throws IOException {
        return convertMultipleTimestamps(FileUtils.readFileToString(jsonFile));
    }

    /**
     * Convert a Trilinos JSON file {@link InputStream} into a {@link List} of {@link JsonObject}s
     * that are in the standard performance report format.  A Trilinos JSON file can
     * contain many top-level JsonObjects that represent unique pairings of
     * timings and metrics - hence, this method returns one JsonObject per timestamp.
     * @param is The InputStream to read.
     * @return The List of identified JsonObjects that contain standard format for
     * the performance report data.  Each JsonObject represents a unique
     * timestamp.
     * @throws IOException Thrown if there was an error reading the file.
     */
    public List<JsonObject> convertMultipleTimestamps(InputStream is) throws IOException {
        StringWriter writer = new StringWriter();
        IOUtils.copy(is, writer, StandardCharsets.UTF_8); // UTF-8 is a safe assumption
        return convertMultipleTimestamps(writer.toString());
    }

    /**
     * Convert a Trilinos JSON file (represented as a {@link String}) into a {@link List}
     * of {@link JsonObject}s that are in the standard performance report format.  
     * A Trilinos JSON file can contain many top-level JsonObjects that represent unique 
     * pairings of timings and metrics - hence, this method returns one JsonObject per timestamp.
     * @param jsonFileContents The file contents String.
     * @return The List of identified JsonObjects that contain standard format for
     * the performance report data.  Each JsonObject represents a unique
     * timestamp.
     */
    public List<JsonObject> convertMultipleTimestamps(String jsonFileContents) {
        JsonElement parsedJsonFile = new JsonParser().parse(jsonFileContents);
        JsonArray convertedJsonArray = new JsonArray();
        List<JsonObject> convertedJsonPerformanceReportList = new ArrayList<>();

        if(parsedJsonFile.isJsonArray()) {
            JsonArray originalJsonArray = parsedJsonFile.getAsJsonArray();
            convertedJsonArray = createMultipleChildJsonObjectsFromTargetJsonArray(originalJsonArray);
        } else if(parsedJsonFile.isJsonObject()) {
            JsonObject convertedJsonObject = convert(jsonFileContents);
            convertedJsonArray = new JsonArray();
            convertedJsonArray.add(convertedJsonObject);
        }

        // In order for Watchr to properly ingest this data, everything must be
        // grouped under matching dates, so we use a map of String to JsonArray,
        // where the String is a date.
        Map<String, JsonArray> convertedJsonMap = mapByDate(convertedJsonArray);

        for(String date : convertedJsonMap.keySet()) {
            JsonObject reportJson = new JsonObject();

            JsonObject performanceReport = new JsonObject();
            performanceReport.addProperty(JSON_KEY_NAME, "report_" + date);
            performanceReport.addProperty(JSON_KEY_DATE, date);
            performanceReport.addProperty(JSON_KEY_UNITS, "seconds");

            performanceReport.add(JSON_KEY_TIMINGS, convertedJsonMap.get(date));
            reportJson.add(JSON_KEY_PERFORMANCE_REPORT, performanceReport);
            convertedJsonPerformanceReportList.add(reportJson);
        }

        return convertedJsonPerformanceReportList;
    }

    /**
     * Convert a Trilinos JSON {@link File} into a {@link JsonObject} that is
     * in the standard performance report format.  Use this method if the
     * Trilinos-style JSON only contains one timestamp.  Otherwise, call
     * {@link TrilinosPerformanceReportAdapter#convertMultipleTimestamps(File)}.
     * @param jsonFile The JSON File to read.
     * @return A JsonObject that contains the standard format for the performance
     * report data.
     * @throws IOException Thrown if there was an error reading the file.
     */
    public JsonObject convert(File jsonFile) throws IOException {
        return convert(FileUtils.readFileToString(jsonFile));
    }

    /**
     * Convert an {@link InputStream} of a Trilinos JSON file into a
     * {@link JsonObject} that is in the standard performance report format.
     * Use this method if the Trilinos-style JSON only contains one timestamp.
     * Otherwise, call {@link TrilinosPerformanceReportAdapter#convertMultipleTimestamps(InputStream)}.
     * @param is The input stream of the JSON file.
     * @return A JsonObject that contains the standard format for the performance
     * report data.
     * @throws IOException Thrown if there was an error reading the file.
     */
    public JsonObject convert(InputStream is) throws IOException {
        StringWriter writer = new StringWriter();
        IOUtils.copy(is, writer, StandardCharsets.UTF_8); // UTF-8 is a safe assumption
        return convert(writer.toString());
    }

    /**
     * Convert a {@link String} representation of a Trilinos JSON file into a
     * {@link JsonObject} that is in the standard performance report format.
     * Use this method if the Trilinos-style JSON only contains one timestamp.
     * Otherwise, call {@link TrilinosPerformanceReportAdapter#convertMultipleTimestamps(String)}.
     * @param jsonFileContents The JSON file contents as a String.
     * @return A JsonObject that contains the standard format for the performance
     * report data.
     */
    public JsonObject convert(String jsonFileContents) {
        JsonObject originalJsonObject = new JsonParser().parse(jsonFileContents).getAsJsonObject();
        JsonObject convertedJsonObject = new JsonObject();
        JsonObject performanceReport = createChildJsonObjectFromTargetJsonObject(originalJsonObject);
        convertedJsonObject.add(JSON_KEY_PERFORMANCE_REPORT, performanceReport);
        return convertedJsonObject;
    }
    
    /////////////
    // PRIVATE //
    /////////////

    private JsonArray createMultipleChildJsonObjectsFromTargetJsonObject(JsonObject originalJsonObject) {
        JsonArray jsonArray = new JsonArray();
        Iterator<Map.Entry<String, JsonElement>> iterator = originalJsonObject.entrySet().iterator();
        while(iterator.hasNext()) {
            Map.Entry<String, JsonElement> mapEntry = iterator.next();
            JsonElement element = mapEntry.getValue();
            if(element instanceof JsonObject) {
                JsonObject convertedValue = createChildJsonObjectFromTargetJsonObject((JsonObject) element);
                convertedValue.addProperty(JSON_KEY_NAME, mapEntry.getKey());
                jsonArray.add(convertedValue);
            }
        }
        return jsonArray;
    }


    private JsonArray createMultipleChildJsonObjectsFromTargetJsonArray(JsonArray originalJsonArray) {
        JsonArray jsonArray = new JsonArray();

        Iterator<JsonElement> iterator = originalJsonArray.iterator();
        while(iterator.hasNext()) {
            JsonElement element = iterator.next();
            if(element instanceof JsonObject) {
                jsonArray.add(createChildJsonObjectFromTargetJsonObject((JsonObject) element));
            }
        }

        return jsonArray;
    }

    private JsonObject createChildJsonObjectFromTargetJsonObject(JsonObject originalJsonObject) {
        JsonObject newJsonObject = new JsonObject();
        JsonArray metricsArray = new JsonArray();
        boolean isNamed = false;

        Iterator<Map.Entry<String, JsonElement>> iterator = originalJsonObject.entrySet().iterator();
        while(iterator.hasNext()) {
            Map.Entry<String, JsonElement> nextElement = iterator.next();
            if(nextElement.getKey().equals(JSON_KEY_TIME_STAMP)){
                final String key   = nextElement.getKey();
                final String value = nextElement.getValue().getAsString();
                Pair<String, String> datePair = convertTimestampField(new ImmutablePair<String, String>(key, value));
                newJsonObject.addProperty(datePair.getLeft(), datePair.getRight());
            } else if(nextElement.getKey().equals(JSON_KEY_TIME_STAMP_FORMAT)) {
                // We have no use for the "time stamp format" field.
                continue;
            } else if(nextElement.getKey().equals(JSON_KEY_METADATA)) {
                JsonObject flatMetadata = flattenMetadata((JsonObject) nextElement.getValue());
                // The Trilinos format places the top-level group's name in the metadata group.
                String name = findNamePropertyInJsonObject(flatMetadata);
                if(StringUtils.isNotBlank(name)) {
                    newJsonObject.addProperty(JSON_KEY_NAME, name);
                    isNamed = true;
                } 
                newJsonObject.add(JSON_KEY_METADATA, flatMetadata);
            } else if(nextElement.getKey().equals(JSON_KEY_METRICS) && nextElement.getValue() instanceof JsonObject) {
                metricsArray = createMultipleChildJsonObjectsFromTargetJsonObject((JsonObject) nextElement.getValue());
            } else {
                // We've hit a non-standard structure, so try to figure out what it is and where to put it.
                JsonObject unknownObject = new JsonObject();
                unknownObject.add(nextElement.getKey(), nextElement.getValue());
                if(nextElement.getValue().isJsonPrimitive()) {
                    newJsonObject.addProperty(nextElement.getKey(), nextElement.getValue().getAsString());
                } else if(isSingleValueJsonObject(unknownObject)) {
                    Pair<String, String> singleValuePair = convertSingleValueJsonObject(unknownObject);
                    newJsonObject.addProperty(singleValuePair.getLeft(), singleValuePair.getRight());
                } else if(isMultipleValueJsonObject(unknownObject)) {
                    JsonObject newChildJsonObject = convertMultipleValueJsonObject(unknownObject);
                    metricsArray.add(newChildJsonObject);
                } else if(isHierarchicalValueJsonObject(unknownObject)) {
                    JsonObject newChildJsonObject = convertHierarchicalValueJsonObject(unknownObject);
                    metricsArray.add(newChildJsonObject);
                }
            }
        }
        if(metricsArray.size() > 0) {
            newJsonObject.add(JSON_KEY_METRICS, metricsArray);
        }

        if(!isNamed) {
            unnamedChildrenCount++;
            newJsonObject.addProperty(JSON_KEY_NAME, "group_" + unnamedChildrenCount);
        }

        return newJsonObject;
    }

    ////////////////////
    // PACKAGE ACCESS //
    ////////////////////

    /*package*/ Pair<String, String> convertTimestampField(Pair<String, String> input) {
        return new ImmutablePair<String, String>(JSON_KEY_DATE, input.getRight());
    }

    /*package*/ JsonObject flattenMetadata(JsonObject input) {
        JsonObject output = new JsonObject();

        Set<Map.Entry<String, JsonElement>> children = input.entrySet();
        Iterator<Map.Entry<String, JsonElement>> iterator = children.iterator();
        while(iterator.hasNext()) {
            Map.Entry<String, JsonElement> nextChild = iterator.next();
            if(nextChild.getValue().isJsonPrimitive()) {
                output.addProperty(nextChild.getKey(), nextChild.getValue().getAsString());
            } else if(nextChild.getValue() instanceof JsonObject) {
                JsonObject recursiveChildrenJsonObject = flattenMetadata((JsonObject) nextChild.getValue());
                Set<Map.Entry<String, JsonElement>> recursiveChildren = recursiveChildrenJsonObject.entrySet();
                Iterator<Map.Entry<String, JsonElement>> recursiveChildrenIterator = recursiveChildren.iterator();
                while(recursiveChildrenIterator.hasNext()) {
                    Map.Entry<String, JsonElement> nextRecursiveChild = recursiveChildrenIterator.next();
                    output.addProperty(nextRecursiveChild.getKey(), nextRecursiveChild.getValue().getAsString());
                }
            }
        }
        return output;
    }

    /*package*/ boolean isSingleValueJsonObject(JsonObject input) {
        boolean success = true;
        success = success && input.entrySet().size() == 1;

        Map.Entry<String, JsonElement> mainElement = input.entrySet().iterator().next();
        success = success && mainElement.getValue() instanceof JsonObject;
        if(success) {
            JsonObject innerValue = (JsonObject) mainElement.getValue();
            success = success && innerValue.entrySet().size() == 1;
            Map.Entry<String, JsonElement> innerElement = innerValue.entrySet().iterator().next();
            success = success && innerElement.getValue().isJsonPrimitive();
        }

        return success;
    }

    /*package*/ boolean isMultipleValueJsonObject(JsonObject input) {
        boolean success = true;
        success = success && input.entrySet().size() == 1;

        Map.Entry<String, JsonElement> mainElement = input.entrySet().iterator().next();
        success = success && mainElement.getValue() instanceof JsonObject;
        if(success) {
            JsonObject innerValue = (JsonObject) mainElement.getValue();
            success = success && innerValue.entrySet().size() > 1;
            Iterator<Map.Entry<String, JsonElement>> iterator = innerValue.entrySet().iterator();
            while(iterator.hasNext()) {
                Map.Entry<String, JsonElement> innerElement = iterator.next();
                success = success && innerElement.getValue().isJsonPrimitive();
            }
        }

        return success;
    }

    /*package*/ boolean isHierarchicalValueJsonObject(JsonObject input) {
        boolean success = true;
        success = success && input.entrySet().size() == 1;

        Map.Entry<String, JsonElement> mainElement = input.entrySet().iterator().next();
        success = success && mainElement.getValue() instanceof JsonObject;
        if(success) {
            JsonObject innerValue = (JsonObject) mainElement.getValue();
            boolean foundJsonObject = false;
            Iterator<Map.Entry<String, JsonElement>> iterator = innerValue.entrySet().iterator();
            while(iterator.hasNext()) {
                Map.Entry<String, JsonElement> innerElement = iterator.next();
                foundJsonObject = innerElement.getValue() instanceof JsonObject;
                if(foundJsonObject) {
                    break;
                }
            }
            success = success && foundJsonObject;
        }

        return success;
    }

    /*package*/ Pair<String, String> convertSingleValueJsonObject(JsonObject input) {
        Map.Entry<String, JsonElement> mainElement = input.entrySet().iterator().next();
        final String key = mainElement.getKey();

        JsonObject innerValue = (JsonObject) mainElement.getValue();
        Map.Entry<String, JsonElement> innerElement = innerValue.entrySet().iterator().next();
        final String value = innerElement.getValue().getAsString();

        return new ImmutablePair<String, String>(key, value);
    }

    /*package*/ JsonObject convertMultipleValueJsonObject(JsonObject input) {
        JsonObject output = new JsonObject();
        Map.Entry<String, JsonElement> mainElement = input.entrySet().iterator().next();
        output.addProperty(JSON_KEY_NAME, mainElement.getKey());

        JsonObject innerValue = (JsonObject) mainElement.getValue();
        Iterator<Map.Entry<String, JsonElement>> iterator = innerValue.entrySet().iterator();
        while(iterator.hasNext()) {
            Map.Entry<String, JsonElement> innerElement = iterator.next();
            output.addProperty(innerElement.getKey(), innerElement.getValue().getAsString());
        }
        return output;
    }

    /*package*/ JsonObject convertHierarchicalValueJsonObject(JsonObject jsonObject) {
        Iterator<Map.Entry<String, JsonElement>> iterator = jsonObject.entrySet().iterator();
        Map.Entry<String, JsonElement> innerElement = iterator.next();
        return convertHierarchicalValueJsonObject(innerElement.getKey(), innerElement.getValue());
    }

    /*package*/ JsonObject convertHierarchicalValueJsonObject(String name, JsonElement value) {
        JsonObject output = new JsonObject();
        JsonArray metricsArray = new JsonArray();
        output.addProperty(JSON_KEY_NAME, name);

        JsonObject jsonObject = (JsonObject) value;
        Iterator<Map.Entry<String, JsonElement>> iterator = jsonObject.entrySet().iterator();
        while(iterator.hasNext()) {
            Map.Entry<String, JsonElement> innerElement = iterator.next();
            if(innerElement.getValue().isJsonPrimitive()) {
                output.addProperty(innerElement.getKey(), innerElement.getValue().getAsString());
            } else if(innerElement.getValue() instanceof JsonObject) {
                final String innerKey = innerElement.getKey();
                final JsonObject innerValue = (JsonObject) innerElement.getValue();
                JsonObject innerObject = convertHierarchicalValueJsonObject(innerKey, innerValue);
                metricsArray.add(innerObject);
            }
        }
        if(metricsArray.size() > 0) {
            output.add(JSON_KEY_METRICS, metricsArray);
        }
        return output;
    }

    /*package*/ String findNamePropertyInJsonObject(JsonObject flatMetadata) {
        String name = "";
        Iterator<Map.Entry<String, JsonElement>> iterator = flatMetadata.entrySet().iterator();
        while(iterator.hasNext()) {
            Map.Entry<String, JsonElement> nextElement = iterator.next();
            if(nextElement.getKey().equals(JSON_KEY_NAME)) {
                JsonElement nextValue = nextElement.getValue();
                if(nextValue.isJsonPrimitive()) {
                    return nextValue.getAsString();
                }
            }
        }
        return name;
    }

    /*package*/ Map<String, JsonArray> mapByDate(JsonArray jsonArray) {
        Map<String, JsonArray> map = new HashMap<>();

        for(int i = 0; i < jsonArray.size(); i++) {
            JsonElement element = jsonArray.get(i);
            if(element instanceof JsonObject) {
                JsonObject jsonObject = (JsonObject) element;
                String date = jsonObject.get(JSON_KEY_DATE).getAsString();
                if(StringUtils.isNotBlank(date)) {
                    JsonArray dateArray = map.getOrDefault(date, new JsonArray());
                    dateArray.add(jsonObject);
                    map.put(date, dateArray);
                }
            }
        }

        return map;
    }
}