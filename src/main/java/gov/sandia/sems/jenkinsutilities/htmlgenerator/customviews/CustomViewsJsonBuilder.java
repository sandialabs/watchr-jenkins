/*******************************************************************************
* Watchr
* ------
* Copyright 2021 National Technology & Engineering Solutions of Sandia, LLC (NTESS).
* Under the terms of Contract DE-NA0003525 with NTESS, the U.S. Government retains
* certain rights in this software.
******************************************************************************/
package gov.sandia.sems.jenkinsutilities.htmlgenerator.customviews;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import gov.sandia.sems.jenkinsutilities.treeaccessor.MomentTuple;
import gov.sandia.sems.jenkinsutilities.treeaccessor.NodeData;
import gov.sandia.sems.jenkinsutilities.treeaccessor.OneLevelTree;
import gov.sandia.sems.jenkinsutilities.views.ConcreteView;
import gov.sandia.sems.jenkinsutilities.views.View;
import gov.sandia.sems.jenkinsutilities.views.ViewDataset;
import gov.sandia.sems.jenkinsutilities.views.ViewDataset.DatasetType;

/**
 * This class is responsible for creating JSON objects representing data on custom views,
 * to then be passed to an HTML fragment generator class.
 * 
 * @author Elliott Ridgway
 */
public class CustomViewsJsonBuilder {

    ////////////
    // FIELDS //
    ////////////
    
    private static final String JSON_STRING_DATE = "date";
    private static final String JSON_STRING_LEGEND_BUFFER = "legendBuffer";
    private static final String JSON_STRING_MAX = "max";
    private static final String JSON_STRING_MIN = "min";
    private static final String JSON_STRING_POINTS = "points";

    ////////////
    // PUBLIC //
    ////////////

    /**
     * Constructs a {@link JsonArray} of dates for the given {@link View}.
     * For each date, the JSON structure contains a list of points.  Each
     * point corresponds to a value contained at that date for a given
     * dataset name.  If no point value is located on that date, the point
     * value is simply a blank String.
     * 
     * @param concreteViewLoader The ConcreteViewLoader to load ConcreteView objects.
     * @param view The View to inspect.
     * @return The JsonArray of dates.
     * @throws ParseException Thrown if the date parse operation fails.
     */
    public JsonArray constructDateJsonForView(
            ConcreteViewLoader concreteViewLoader, View view) throws ParseException {

        JsonArray datesJson = new JsonArray();
        List<ConcreteView> concreteViews = concreteViewLoader.getConcreteViews();
        ConcreteView concreteView = concreteViewLoader.findConcreteViewByName(concreteViews, view.getName());

        if(concreteView != null) {
            Map<String, OneLevelTree> trees = concreteView.getDatasetMap();
            for(String date : concreteViewLoader.getPossibleDates(concreteView)) {
                JsonObject dateGroupJson = new JsonObject();
                dateGroupJson.addProperty(JSON_STRING_DATE, date);
                JsonObject pointsJson = new JsonObject();

                List<ViewDataset> sortedViewDatasets = new ArrayList<>(view.getViewDatasets());
                Collections.sort(sortedViewDatasets);
                for(ViewDataset viewDataset : sortedViewDatasets) {
                    OneLevelTree tree = trees.get(viewDataset.getPath());
                    String treeGraphName = constructViewDatasetFullName(viewDataset);
                    Map<String, NodeData> nodeDateMap = tree.getNodes();
                    if(nodeDateMap.get(date) != null) {
                        NodeData nodeData = nodeDateMap.get(date);
                        MomentTuple momentTuple = nodeData.getMomentTuple(concreteViewLoader.getMeasurable());
                        pointsJson.addProperty(treeGraphName,
                            getMomentValueForViewDataset(momentTuple, viewDataset.getType())
                        );
                    } else {
                        pointsJson.addProperty(treeGraphName, new String());
                    }
                }
                dateGroupJson.add(JSON_STRING_POINTS, pointsJson);
                datesJson.add(dateGroupJson);
            }
        }
        return datesJson;
    }

    /**
     * Constructs a top-level statistics ("stats") {@link JsonObject} for a given
     * {@link View}.  The "stats" data structure contains rollup information
     * that the display will need quickly.
     * 
     * @param concreteViewLoader The ConcreteViewLoader to load ConcreteView objects.
     * @param view The View to inspect.
     * @param graphHeight The height of the graph in pixels.
     * @return The stats JsonObject.
     * @throws ParseException Thrown if a date parse operation fails.
     */
    public JsonObject constructStatsJsonForView(
        ConcreteViewLoader concreteViewLoader, View view, int graphHeight) throws ParseException {

        JsonObject statsJson = new JsonObject();
        List<ConcreteView> concreteViews = concreteViewLoader.getConcreteViews();
        ConcreteView concreteView = concreteViewLoader.findConcreteViewByName(concreteViews, view.getName());
        String measurable = concreteViewLoader.getMeasurable();

        if(concreteView != null) {
            Map<String, OneLevelTree> trees = concreteView.getDatasetMap();
            double globalMax = Double.MIN_VALUE;
            double globalMin = Double.MAX_VALUE;
            List<String> possibleDates = concreteViewLoader.getPossibleDates(concreteView);
            for(String date : possibleDates) {
                for(ViewDataset viewDataset : view.getViewDatasets()) {
                    OneLevelTree tree = trees.get(viewDataset.getPath());
                    Map<String, NodeData> nodeDateMap = tree.getNodes();
                    if(nodeDateMap.get(date) != null) {
                        NodeData nodeData = nodeDateMap.get(date);
                        MomentTuple momentTuple = nodeData.getMomentTuple(measurable);
                        Double value = getMomentValueForViewDataset(momentTuple, viewDataset.getType());
                        if(value != null && value > globalMax) {
                            globalMax = value;
                        }
                        if(value != null && value < globalMin) {
                            globalMin = value;
                        }
                    }
                }            
            }
            statsJson.addProperty(JSON_STRING_MAX, globalMax);
            statsJson.addProperty(JSON_STRING_MIN, globalMin);

            // Count the number of datasets to calculate the size of the legend.  Note that
            // empty datasets are excluded from the calculation.
            int datasetCount = 0;
            int maxIndex = possibleDates.size() - 1;
            int minIndex = maxIndex - concreteViewLoader.getTimeScale();
            if(minIndex < 0) {
                minIndex = 0;
            }
            String maxDate = possibleDates.get(maxIndex);
            String minDate = possibleDates.get(minIndex);
            for(ViewDataset vd : view.getViewDatasets()) {
                OneLevelTree tree = trees.get(vd.getPath());
                if(!tree.isEmptyDataSetInDateRange(measurable, minDate, maxDate)) {
                    datasetCount ++;
                }
            }
            double legendBuffer = getLegendHeightBuffer(datasetCount, globalMin, globalMax, graphHeight);
            statsJson.addProperty(JSON_STRING_LEGEND_BUFFER, legendBuffer);
        }
        return statsJson;
    }

    ////////////////////
    // PACKAGE ACCESS //
    ////////////////////

    /**
     * Since a {@link ViewDataset} may refer to a dataset's main line,
     * average line, or standard deviation line, we need a method that
     * can organize this information into a human-readable label.
     * 
     * @param viewDataset the ViewDataset to examine.
     * @return A {@link String} stating the ViewDataset's data type.
     */
    /*package*/ String constructViewDatasetFullName(ViewDataset viewDataset) {
        String treePath = viewDataset.getPath();
        ViewDataset.DatasetType type = viewDataset.getType();

        String treeGraphName = treePath;
        if(type == ViewDataset.DatasetType.AVERAGE) {
            treeGraphName += " - Average";
        } else if(type == ViewDataset.DatasetType.STD_DEV) {
            treeGraphName += " - Std. Dev.";
        } else {
            treeGraphName += " - Value";
        }

        return treeGraphName;
    }

    /**
     * Given a {@link NodeData} block, retrieve a value for the measurable/data type combo also provided in
     * the argument list.
     * 
     * @param nodeData The NodeData block to inspect.
     * @param type The type of data to retrieve.
     * @param measurable The measurable to retrieve.
     * @return The Double value, or null if no value could be found given the parameters.
     */
    /*package*/ Double getMomentValueForViewDataset(MomentTuple momentTuple, DatasetType type) {
        if(momentTuple != null) {
            if(type == ViewDataset.DatasetType.AVERAGE) {
                return momentTuple.getAverage();
            } else if(type == ViewDataset.DatasetType.STD_DEV) {
                // Always display standard deviation lines as offsets from the average line.
                // This is a very Sierra-specific use case and may break down if a user ever
                // wants to display a standard deviation line by itself, without the context
                // of the average line... :(
                Double avg = momentTuple.getAverage();
                Double std = momentTuple.getStd();
                return avg + std;
            } else {
                return momentTuple.getValue();
            }
        }
        return null;
    }

    /**
     * Calculate the additional amount of space needed to display a legend without obscuring
     * the contents of the graph.
     * 
     * @param legendCount The number of elements in the legend.
     * @param globalMin The minimum value of the graph.
     * @param globalMax The maximum value of the graph.
     * @param graphHeightPx The height of the graph in pixels.
     * 
     * @return The amount of extra space (in graph unit space, not pixels) to provide for the legend.
     */
    /*package*/ double getLegendHeightBuffer(int legendCount, Double globalMin, Double globalMax, int graphHeightPx) {
        final int singleLegendLineHeightPx = 40;
        final int legendHeightPx = singleLegendLineHeightPx * legendCount;
        final Double range = globalMax - globalMin;
        final Double scaleFactor = range / graphHeightPx;
        return legendHeightPx * scaleFactor;
    }
}