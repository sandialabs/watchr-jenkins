/*******************************************************************************
* Watchr
* ------
* Copyright 2021 National Technology & Engineering Solutions of Sandia, LLC (NTESS).
* Under the terms of Contract DE-NA0003525 with NTESS, the U.S. Government retains
* certain rights in this software.
******************************************************************************/
package gov.sandia.sems.jenkinsutilities.htmlgenerator;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import org.apache.commons.lang3.StringUtils;

import gov.sandia.sems.jenkins.semsjppplugin.CommonConstants;
import gov.sandia.sems.jenkins.semsjppplugin.Keywords;
import gov.sandia.sems.jenkinsutilities.db.IDatabaseAccessor;
import gov.sandia.sems.jenkinsutilities.db.IDatabasePartType;
import gov.sandia.sems.jenkinsutilities.treeaccessor.FilterData;
import gov.sandia.sems.jenkinsutilities.treeaccessor.IFilterAccessor;
import gov.sandia.sems.jenkinsutilities.treeaccessor.ITreeAccessor;
import gov.sandia.sems.jenkinsutilities.treeaccessor.NodeData;
import gov.sandia.sems.jenkinsutilities.treeaccessor.OneLevelTree;
import gov.sandia.sems.jenkinsutilities.utilities.LogUtil;

/**
 * This class is responsible for generating segments of HTML for displaying
 * graph data.
 * 
 * @author Elliott Ridgway, Lawrence Allen
 */
public class HtmlGenerator extends AbstractHtmlGenerator {

    ////////////
    // FIELDS //
    ////////////

    private static final String DATES = "dates"; //$NON-NLS-1$
    private static final String FAIL = "fail"; //$NON-NLS-1$
    private static final String GRAPH_DATA = "graphData"; //$NON-NLS-1$
    private static final String METADATA = "metadata"; //$NON-NLS-1$
    private static final String OPTIONS = "options"; //$NON-NLS-1$
    private static final String UNITS = "units"; //$NON-NLS-1$

    private final IDatabaseAccessor db;
    private boolean avgFailIfGreater;
    private boolean stdDevFailIfGreater;

    /////////////////
    // CONSTRUCTOR //
    /////////////////

    public HtmlGenerator(
            ConfigContext context, IDatabaseAccessor db,
            String[] measurables, boolean avgFailIfGreater,
            boolean stdDevFailIfGreater,
            String levelSeparator, File logFile) {
        super(context, measurables, levelSeparator, logFile);

        this.db = db;
        this.avgFailIfGreater = avgFailIfGreater;
        this.stdDevFailIfGreater = stdDevFailIfGreater;
    }

    //////////////
    // OVERRIDE //
    //////////////

    @Override
    public String getDataFromPath() {
        String path = context.getGraphSelectedPath();
        String measurable = context.getGraphSelectedMeasurable();
        if (StringUtils.isBlank(measurable) && measurables.length > 0) {
            measurable = measurables[0];
        }
        int timeScale = context.getGraphSelectedTimeScale();
        boolean showDescendantGraphs = context.getShowDescendants();
        boolean enableAvg = context.getShowAvgLine();
        boolean enableStdDev = context.getShowStdDevLine();
        int roundTo = context.getRoundTo();

        JsonObject dataJson = new JsonObject();
        JsonObject graphDataJson = new JsonObject();
    
        ITreeAccessor treeAccess = (ITreeAccessor) db.getAndOpenDatabasePart(IDatabasePartType.TREE);
        List<OneLevelTree> trees = treeAccess.getChildrenAt(path, showDescendantGraphs, measurable);
                        
        String[] segments  = path.split(Pattern.quote(treeAccess.getLevelSeparator()));
        String topLevelDir = segments[segments.length-1];

        Map<String, OneLevelTree> fullGraphPathNameMap = retrieveChildData(topLevelDir, trees);
        if(!fullGraphPathNameMap.isEmpty()) {
            List<String> fullGraphPathNameMapKeyList = new ArrayList<>();
            fullGraphPathNameMapKeyList.addAll(fullGraphPathNameMap.keySet());
            Collections.sort(fullGraphPathNameMapKeyList, (String o1, String o2) ->
                o1.toUpperCase().compareTo(o2.toUpperCase())
            );
            
            for(String fullGraphPathName : fullGraphPathNameMapKeyList) {
                JsonObject graphJson   = new JsonObject();
                
                JsonArray  datesJson   = new JsonArray();
                JsonObject optionsJson = new JsonObject();
                
                OneLevelTree tree = fullGraphPathNameMap.get(fullGraphPathName);

                List<String> filterDates = new ArrayList<>();
                IFilterAccessor filterAccess = (IFilterAccessor) db.getAndOpenDatabasePart(IDatabasePartType.FILTERS);
                FilterData filterData = filterAccess.getFilterDataByPath(tree.getPath(), false);
                if(filterData != null) {
                    filterDates.addAll(filterData.getFilteredDates());
                }

                try {
                    datesJson = tree.getDatesInRange(measurable, timeScale, enableAvg, enableStdDev, roundTo, filterDates);
                    
                    if(datesJson.size() > 0) {
                        datesJson = applyGlobalMetadataToJson(datesJson);
                        optionsJson = applyOptionsToJson(optionsJson, tree, measurable, filterDates);
                    }
                } catch(ParseException e) {
                    LogUtil.writeErrorToLog(logFile, e);
                }
                graphJson.add(DATES, datesJson);
                graphJson.add(OPTIONS, optionsJson);
                
                graphDataJson.add(fullGraphPathName, graphJson);
            }
        }
        
        dataJson.add(GRAPH_DATA, graphDataJson);
        
        return dataJson.toString();
    }  

    @Override
    public String getGraphHTML() {
        StringBuilder sb = new StringBuilder();

        String path = context.getGraphSelectedPath();
        String measurable = context.getGraphSelectedMeasurable();
        if (StringUtils.isBlank(measurable) && measurables.length > 0) {
            measurable = measurables[0];
        }
        boolean showDescendantGraphs = context.getShowDescendants();

        ITreeAccessor treeAccess = (ITreeAccessor) db.getAndOpenDatabasePart(IDatabasePartType.TREE);
        OneLevelTree currentTree = treeAccess.getNodeAt(path);
        List<OneLevelTree> childTrees = treeAccess.getChildrenAt(path, showDescendantGraphs, measurable);

        if (currentTree != null) {
            TreeMap<String, IndividualDivTuple> divTuples = createDivTuples(currentTree.getName(), childTrees);
            
            boolean hasTimingData = OneLevelTree.hasCategory(childTrees, Keywords.CATEGORY_TIMING.get());
            boolean hasMetricData = OneLevelTree.hasCategory(childTrees, Keywords.CATEGORY_METRIC.get());

            if (hasTimingData || hasMetricData) {
                try {
                    sb.append(HtmlFragmentGenerator.buildGraphs(context, divTuples));
                } catch(UnsupportedEncodingException e) {
                    LogUtil.writeErrorToLog(logFile, e);
                }
            } else {
                sb.append(HtmlFragmentGenerator.noGraphs());
            }
        }
        return sb.toString();
    }

    /////////////
    // PRIVATE //
    /////////////

    private TreeMap<String, IndividualDivTuple> createDivTuples(String topLevelName, List<OneLevelTree> trees) {
        TreeMap<String, IndividualDivTuple> divTuplesMap = new TreeMap<>();
        ITreeAccessor treeAccess = (ITreeAccessor) db.getAndOpenDatabasePart(IDatabasePartType.TREE);

        for (OneLevelTree tree : trees) {
            List<OneLevelTree> childTrees = treeAccess.getChildrenAt(tree.getPath(), false, "");
            boolean hasChildren = !childTrees.isEmpty();
            boolean showDescendantGraphs = context.getShowDescendants();

            StringBuilder finalGraphName = new StringBuilder();
            if (showDescendantGraphs) {
                // This child is potentially N levels below the page being displayed, so we
                // should show the full path it takes to get to this child.
                Deque<String> additionalParentDirectories = new ArrayDeque<>();
                OneLevelTree currentTree = tree;
                String currentLevelName = currentTree.getName();
                while (!currentLevelName.equals(topLevelName)
                        && !currentLevelName.equals(CommonConstants.ROOT_PATH_ALIAS)) {
                    additionalParentDirectories.push(currentLevelName);
                    currentTree = treeAccess.getParentAt(currentTree.getPath());
                    currentLevelName = currentTree.getName();
                }

                while (!additionalParentDirectories.isEmpty()) {
                    finalGraphName.append(additionalParentDirectories.pop());
                    if (!additionalParentDirectories.isEmpty()) {
                        finalGraphName.append(treeAccess.getLevelSeparator());
                    }
                }
            } else {
                finalGraphName.append(tree.getName());
            }

            IndividualDivTuple divTuple =
                new IndividualDivTuple(
                    finalGraphName.toString(), context.getGraphSelectedPath(), tree.getCategory(),
                    treeAccess.getLevelSeparator(), hasChildren, true
                );
            divTuplesMap.put(finalGraphName.toString(), divTuple);
        }
        return divTuplesMap;
    }
    
    private Map<String, OneLevelTree> retrieveChildData(String topLevelDir, List<OneLevelTree> trees) {
        Map<String, OneLevelTree> data = new LinkedHashMap<>();
        boolean showDescendantGraphs = context.getShowDescendants();
        ITreeAccessor treeAccess = (ITreeAccessor) db.getAndOpenDatabasePart(IDatabasePartType.TREE);

        for(OneLevelTree tree : trees) {
            StringBuilder finalGraphName = new StringBuilder();
            if(showDescendantGraphs) {
                // This child is potentially N levels below the page being displayed.
                Deque<String> additionalParentDirectories = new ArrayDeque<>();
                
                OneLevelTree currentTree = tree;
                String currentLevelName = currentTree.getName();                
                
                while(currentTree != null &&
                      !currentLevelName.equals(topLevelDir) &&
                      !currentLevelName.equals(CommonConstants.ROOT_PATH_ALIAS)) {
                          
                    additionalParentDirectories.push(currentLevelName);
                    currentTree = treeAccess.getParentAt(currentTree.getPath());
                    if(currentTree != null) {
                        currentLevelName = currentTree.getName();
                    }
                }
                
                while(!additionalParentDirectories.isEmpty()) {
                    finalGraphName.append(additionalParentDirectories.pop());
                    if(!additionalParentDirectories.isEmpty()) {
                        finalGraphName.append(treeAccess.getLevelSeparator());
                    }
                }
            } else {
                finalGraphName.append(tree.getName());
            }
                        
            data.put(finalGraphName.toString(), tree);
        }
        return data;
    }

    /**
     * If global metadata exists in the root of the data tree at any date, we should
     * display such metadata at every child node that is a matching date, since
     * there's no other way to see global metadata.
     * 
     * @param datesJson The JsonArray to update with global metadata.
     * @return The modified JsonArray.
     */
    private JsonArray applyGlobalMetadataToJson(JsonArray datesJson) {
        ITreeAccessor treeAccessor = (ITreeAccessor) db.getAndOpenDatabasePart(IDatabasePartType.TREE);
        OneLevelTree root = treeAccessor.getRootNode();
        SortedMap<String, NodeData> rootNodes = root.getNodes();

        Iterator<JsonElement> dateIterator = datesJson.iterator();
        while(dateIterator.hasNext()) {
            JsonElement dateJson = dateIterator.next();
            if(dateJson instanceof JsonObject) {
                for(Entry<String, NodeData> entry : rootNodes.entrySet()) {
                    String rootDate = entry.getKey();
                    NodeData rootNode = entry.getValue();

                    String jsonDate = ((JsonObject)dateJson).entrySet().iterator().next().getKey();
                    JsonElement dateContentsJson = ((JsonObject)dateJson).get(jsonDate);
                    JsonObject metadataJson = (JsonObject)((JsonObject)dateContentsJson).get(METADATA);

                    if(rootDate.equals(jsonDate) && !rootNode.getMetadata().isEmpty()) {
                        // Apply global metadata here.
                        for(Entry<String,String> metadataEntry : rootNode.getMetadata().entrySet()) {
                            if(!metadataJson.has(metadataEntry.getKey())) {
                                metadataJson.addProperty(metadataEntry.getKey(), metadataEntry.getValue());
                            }
                        }
                    }
                }
            }
        }
        return datesJson;
    }

    private JsonObject applyOptionsToJson(
            JsonObject optionsJson, OneLevelTree tree, String measurable, List<String> filterDates) {

        optionsJson.addProperty(FAIL, Boolean.toString(
            tree.dataIsFailure(measurable, avgFailIfGreater, stdDevFailIfGreater, filterDates)
        ));
            
        SortedMap<String, NodeData> sortedDateTreeMap = tree.getNodes();
        String latestDate = sortedDateTreeMap.lastKey();
        String units = sortedDateTreeMap.get(latestDate).getUnits();
        optionsJson.addProperty(UNITS, StringUtils.isBlank(units) ? "Unknown unit?" : units);

        return optionsJson;
    }
}