/*******************************************************************************
* Watchr
* ------
* Copyright 2021 National Technology & Engineering Solutions of Sandia, LLC (NTESS).
* Under the terms of Contract DE-NA0003525 with NTESS, the U.S. Government retains
* certain rights in this software.
******************************************************************************/
package gov.sandia.sems.jenkinsutilities.treeaccessor;

import gov.sandia.sems.jenkinsutilities.utilities.XStreamSingleton;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.Map.Entry;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.annotations.XStreamOmitField;
import gov.sandia.sems.jenkins.semsjppplugin.CommonConstants;
import gov.sandia.sems.jenkins.semsjppplugin.Keywords;
import gov.sandia.sems.jenkins.semsjppplugin.PerformanceResultAction;
import gov.sandia.sems.jenkinsutilities.db.IDatabaseAccessor;
import gov.sandia.sems.jenkinsutilities.db.IDatabasePartType;
import gov.sandia.sems.jenkinsutilities.utilities.DateUtil;
import gov.sandia.sems.jenkinsutilities.utilities.FileUtil;
import gov.sandia.sems.jenkinsutilities.utilities.LogUtil;
import gov.sandia.sems.jenkinsutilities.utilities.OsUtil;
import gov.sandia.sems.jenkinsutilities.utilities.PathUtil;
import gov.sandia.sems.jenkinsutilities.utilities.StatUtil;
import gov.sandia.sems.jenkinsutilities.utilities.XStreamUtil;
import gov.sandia.sems.jenkinsutilities.xml.MetaData;

import gov.sandia.sems.jenkinsutilities.xml.PerformanceReport;
import gov.sandia.sems.jenkinsutilities.xml.XmlDataElement;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * The DiskTreeAccessor is the class that allows for access to the tree of
 * folders and files representing the performance report data.
 * 
 * @author Elliott Ridgway, Lawrence Allen
 */
public class DiskTreeAccessor implements ITreeAccessor, Serializable {

    ////////////
    // FIELDS //
    ////////////

    public static final long serialVersionUID = "DiskTreeAccessor".hashCode();

    @XStreamOmitField
    private final transient IDatabaseAccessor parent;
    @XStreamOmitField
    private final transient File logFile;
    @XStreamOmitField
    private final transient PerformanceResultAction performanceResultAction;
    @XStreamOmitField
    private static final String RECORD_XML_FILE = "record.xml"; // Name for the files that contain serialized
                                                                // OneLevelTree objects.
    @XStreamOmitField
    private static final String STATE_XML_FILE = "tree_state.xml"; // Name for a file containing the serialized version
                                                                   // of the DiskTreeAccessor object.
    @XStreamOmitField
    private boolean open = false;
    @XStreamOmitField
    private transient Map<String, OneLevelTree> cache = null;

    private final String treeDirAbsPath;
    private String stateXmlFileAbsPath = null;

    /* package */ String minDate = CommonConstants.MAX_DATE;
    /* package */ String maxDate = CommonConstants.MIN_DATE;

    @XStreamOmitField
    private final transient Set<OneLevelTree> alteredNodes = new HashSet<>();

    private Map<Integer, Map<String, List<String>>> buildToHashes = new HashMap<>();

    /////////////////
    // CONSTRUCTOR //
    /////////////////

    public DiskTreeAccessor(IDatabaseAccessor parent, String treeDirAbsPath, PerformanceResultAction action, File logFile) {
        this.parent = parent;
        this.treeDirAbsPath = treeDirAbsPath;
        this.performanceResultAction = action;
        this.logFile = logFile;

        this.cache = new HashMap<>();
    }

    ////////////////
    // PUBLIC API //
    ////////////////

    @Override
    public IDatabaseAccessor getParentDatabase() {
        return parent;
    }

    @Override
    public Map<Integer, Map<String, List<String>>> getBuildToHashesMap() {
        if (buildToHashes == null) {
            buildToHashes = new HashMap<>();
        }
        return buildToHashes;
    }

    @Override
    public String getRootPath() {
        return treeDirAbsPath;
    }

    @Override
    public boolean open() {
        if (!isOpen()) {
            try {
                File rootPathFile = new File(treeDirAbsPath);
                if (!rootPathFile.exists()) {
                    rootPathFile.mkdirs();
                }
                loadStateFile();
                open = true;
                return true;
            } catch (IOException e) {
                LogUtil.writeErrorToLog(logFile, e);
            }
        }
        return false;
    }

    @Override
    public void addReport(PerformanceReport report) {
        try {
            // If the report is before our minDate or after our maxDate, we update
            // accordingly
            String reportDate = report.getDate();
            if (reportDate.compareTo(minDate) <= 0) {
                minDate = reportDate;
            }
            if (reportDate.compareTo(maxDate) >= 0) {
                maxDate = reportDate;
            }

            for (XmlDataElement element : report.getChildren()) {
                translateElementsToNodes(element, reportDate, treeDirAbsPath);
            }
            NodeData data = translateElementToNode(report);

            String rootName =
                !StringUtils.isBlank(report.getName()) ?
                CommonConstants.ROOT_PATH_ALIAS :
                report.getName();
            addTree(reportDate, treeDirAbsPath, rootName, Keywords.CATEGORY_PERFORMANCE_REPORT.get(), data);
        } catch (Exception e) {
            LogUtil.writeErrorToLog(logFile, e);
        }
    }

    @Override
    public OneLevelTree getNodeAt(String relativePath) {
        String fullPath = constructAbsolutePath(relativePath);

        OneLevelTree tree = null;
        if (cache.containsKey(fullPath)) {
            tree = cache.get(fullPath);
        } else {
            File treeXmlFile = new File(fullPath, RECORD_XML_FILE);
            if (treeXmlFile.exists()) {
                try (InputStream is = FileUtils.openInputStream(treeXmlFile)) {
                    XStream xstream = XStreamSingleton.getInstance();
                    Object obj = xstream.fromXML(is);
                    if (obj instanceof OneLevelTree) {
                        tree = (OneLevelTree) obj;
                    }
                } catch (Exception e) {
                    LogUtil.writeToLog(logFile, "Error reading file at " + treeXmlFile.getAbsolutePath());
                    LogUtil.writeErrorToLog(logFile, e);
                }

                cache.put(fullPath, tree);
            }
        }
        return tree;
    }

    @Override
    public OneLevelTree getRootNode() {
        return getNodeAt("");
    }

    @Override
    public OneLevelTree getParentAt(String relativePath) {
        String fullPath = constructAbsolutePath(relativePath);
        File currentFile = new File(fullPath);
        File parentFile = currentFile.getParentFile();
        String parentFilePath = parentFile.getAbsolutePath();

        OneLevelTree tree = null;
        if (cache.containsKey(parentFilePath)) {
            tree = cache.get(parentFilePath);
        } else {
            File treeXmlFile = new File(parentFilePath, RECORD_XML_FILE);
            if (treeXmlFile.exists()) {
                try (InputStream is = FileUtils.openInputStream(treeXmlFile)) {
                    XStream xstream = XStreamSingleton.getInstance();
                    Object obj = xstream.fromXML(is);
                    if (obj instanceof OneLevelTree) {
                        tree = (OneLevelTree) obj;
                    }
                } catch (IOException e) {
                    LogUtil.writeErrorToLog(logFile, e);
                }

                cache.put(parentFilePath, tree);
            }
        }
        return tree;
    }

    @Override
    public List<OneLevelTree> getChildrenAt(String relativePath, boolean preferDescendants, String measurable) {
        List<OneLevelTree> recordTrees = new ArrayList<>();

        List<String> childNames = new ArrayList<>();
        OneLevelTree parentTree = getNodeAt(relativePath);
        if (parentTree != null) {
            childNames.addAll(parentTree.getNodes().keySet());
        }

        if (!childNames.isEmpty()) {
            String fullPath = constructAbsolutePath(relativePath);
            File parentDir = new File(fullPath);
            for (String childDir : parentDir.list()) {
                File childDirFile = new File(fullPath, childDir);
                OneLevelTree childTree = loadChildTreeFromRecordFile(fullPath, childDir);
                if (childTree != null) {
                    // Check if tree has any data to display.
                    boolean hasData = !childTree.isEmptyDataSet(measurable) || StringUtils.isBlank(measurable);
                    if (hasData || !preferDescendants) {
                        recordTrees.add(childTree);
                    } else { // preferDescendants
                        boolean hasChildren = FileUtil.hasChildDirectories(childDirFile);
                        if (hasChildren) {
                            recordTrees.addAll(getChildrenAt(
                                    relativePath + getLevelSeparator() + childDirFile.getName(), true, measurable));
                        }
                    }
                }
            }
        }

        return recordTrees;
    }

    /**
     * The DiskTreeAccessor implementation of this method respects all Java regular
     * expression syntax. In addition, the entire path (i.e. path leading up to this
     * OneLevelTree, and the OneLevelTree name itself) is used for the match.
     */
    @Override
    public List<OneLevelTree> searchForChildren(String startPath, String searchTerm, boolean searchAllDescendants,
            boolean returnEmptyResults) {

        List<OneLevelTree> foundTrees = new ArrayList<>();

        List<OneLevelTree> children = getChildrenAt(startPath, false, "");
        for (OneLevelTree child : children) {
            String childFullPath = child.getPath() + getLevelSeparator() + child.getName();
            boolean textMatch = childFullPath.matches(searchTerm) || child.getName().matches(searchTerm);
            boolean emptyChild = child.isEmpty();
            if (textMatch && (!emptyChild || returnEmptyResults)) {
                foundTrees.add(child);
            }
            if (searchAllDescendants) {
                foundTrees.addAll(searchForChildren(child.getPath(), searchTerm, true, returnEmptyResults));
            }
        }
        return foundTrees;
    }

    @Override
    public String getLevelSeparator() {
        return File.separator;
    }

    @Override
    public boolean close() {
        if (!isOpen()) {
            return false;
        }

        if (performanceResultAction.shouldRecalculateAllDerivedLines()) {
            Set<OneLevelTree> allNodes = getAllChildren();

            try {
                setMinAndMaxDatesToMinAndMaxFromNodes(allNodes);
            } catch (ParseException e) {
                LogUtil.writeErrorToLog(logFile, e);
            }
            LogUtil.writeToLog(logFile, "Recalculating all derived lines...");
            recalculateValues(allNodes);
        } else if (!alteredNodes.isEmpty()) {
            // If whatever we did changed a node, we recalculate it.
            LogUtil.writeToLog(logFile, "Recalculating derived lines for altered graphs...");
            recalculateValues(alteredNodes);
        }

        // Reset minDate and maxDate to their defaults for the next
        // session of writing data to the tree.
        minDate = CommonConstants.MAX_DATE;
        maxDate = CommonConstants.MIN_DATE;
        alteredNodes.clear();

        // Empty the cache upon closing the tree.
        cache.clear();

        try {
            FileUtil.serializeObjToFile(stateXmlFileAbsPath, this);
        } catch (IOException e) {
            LogUtil.writeErrorToLog(logFile, e);
        }

        open = false;

        return true;
    }

    @Override
    public boolean isOpen() {
        return open;
    }

    //////////////////////////////
    // PRIVATE / PACKAGE ACCESS //
    //////////////////////////////

    /* package */ void loadStateFile() throws IOException {
        File stateXmlFile = new File(treeDirAbsPath, STATE_XML_FILE);

        if (!stateXmlFile.exists()) {
            boolean success = stateXmlFile.createNewFile();
            if(success) {
                FileUtil.setFilePermissions(stateXmlFile, true, true, true);
            }
        } else {
            XStream xstream = XStreamSingleton.getInstance();
            try (InputStream is = FileUtils.openInputStream(stateXmlFile)) {
                Object deSzObj = xstream.fromXML(is);
                if (deSzObj instanceof DiskTreeAccessor) {
                    DiskTreeAccessor loadedTreeAccessor = (DiskTreeAccessor) deSzObj;

                    if (loadedTreeAccessor.getBuildToHashesMap() != null) {
                        buildToHashes = new HashMap<>(loadedTreeAccessor.getBuildToHashesMap());
                    }
                }
            }
        }
        stateXmlFileAbsPath = stateXmlFile.toPath().toString();
    }

    /* package */ String constructAbsolutePath(String relativePath) {
        String fullPath = treeDirAbsPath;
        if (!relativePath.equals(CommonConstants.ROOT_PATH_ALIAS)) {
            if (!fullPath.endsWith(File.separator)) {
                fullPath = fullPath + File.separator;
            }
            fullPath = fullPath + relativePath;
        }
        return fullPath;
    }

    /* package */ OneLevelTree loadChildTreeFromRecordFile(String absolutePathToParentDir,
            String relativeChildDirName) {
        File parentDir = new File(absolutePathToParentDir);
        if (parentDir.exists() && parentDir.isDirectory()) {
            File childDir = new File(parentDir, relativeChildDirName);
            if (childDir.exists() && childDir.isDirectory()) {
                File recordXmlFile = new File(childDir, RECORD_XML_FILE);
                if (recordXmlFile.exists()) {
                    try {
                        return XStreamUtil.deSzOneLevelTree(recordXmlFile);
                    } catch (IOException e) {
                        LogUtil.writeErrorToLog(logFile, e);
                    }
                }
            }
        }

        return null;
    }

    /**
     * Recursively returns all child {@link OneLevelTree} objects, starting from the
     * root of the tree.
     */
    /* package */ Set<OneLevelTree> getAllChildren() {
        return getAllChildren(new HashSet<>(), "", "");
    }

    /**
     * Set this DiskTreeAccessor object's min and max date fields based on the min
     * and max dates available in the provided {@link Set} of {@link OneLevelTree}
     * objects.
     */
    /* package */ void setMinAndMaxDatesToMinAndMaxFromNodes(Set<OneLevelTree> trees) throws ParseException {
        String foundMinDate = minDate;
        String foundMaxDate = maxDate;
        for (OneLevelTree tree : trees) {
            SortedMap<String, NodeData> sortedDateTreeMap = tree.getNodes();
            String oldestDate = sortedDateTreeMap.firstKey();
            String newestDate = sortedDateTreeMap.lastKey();

            if (oldestDate.length() > 19) {
                oldestDate = oldestDate.substring(0, 19);
            }
            if (newestDate.length() > 19) {
                newestDate = newestDate.substring(0, 19);
            }

            if (foundMinDate.equals(CommonConstants.MAX_DATE) || !DateUtil.isWithinRange(oldestDate, foundMinDate, foundMaxDate)) {
                foundMinDate = oldestDate;
            }

            if (foundMaxDate.equals(CommonConstants.MIN_DATE) || !DateUtil.isWithinRange(newestDate, foundMinDate, foundMaxDate)) {
                foundMaxDate = newestDate;
            }
        }

        minDate = foundMinDate;
        maxDate = foundMaxDate;
    }

    /**
     * Recursively retrieves all child {@link OneLevelTree} objects. Returned as a
     * {@link Set}, to eliminate duplicates.
     * 
     * @param treesSoFar   The recursive holder of OneLevelTree objects found so far
     *                     in the traversal.
     * @param relativePath The starting point of where to look for OneLevelTree
     *                     objects.
     * @param measurable   The data measurable to filter on. Leave blank to return
     *                     all trees regardless of data.
     * @return All child OneLevelTree objects.
     */
    private Set<OneLevelTree> getAllChildren(Set<OneLevelTree> treesSoFar, String relativePath, String measurable) {
        relativePath = OsUtil.convertToOsFileSeparators(relativePath);
        OneLevelTree parentTree = getNodeAt(relativePath);
        if (parentTree != null) {
            String parentTreePath = parentTree.getPath();
            parentTreePath = OsUtil.convertToOsFileSeparators(parentTreePath);
            treesSoFar.add(parentTree);

            for (OneLevelTree childTree : getChildrenAt(parentTreePath, false, measurable)) {
                String childPath = childTree.getPath();
                childPath = OsUtil.convertToOsFileSeparators(childPath);
                treesSoFar.addAll(getAllChildren(treesSoFar, childPath, measurable));
            }
        }
        return treesSoFar;
    }

    private OneLevelTree getRecordTree(String absPath) {
        OneLevelTree tree = null;

        // If the path is in the cache, we can just return that.
        if (cache.containsKey(absPath)) {
            tree = cache.get(absPath);
        } else {
            File xmlFile;

            if (absPath.endsWith(RECORD_XML_FILE)) {
                xmlFile = new File(absPath);
            } else {
                xmlFile = new File(absPath, RECORD_XML_FILE);
            }

            File parentDir = xmlFile.getParentFile();
            if (!parentDir.exists()) {
                parentDir.mkdirs();
            }

            if (xmlFile.exists()) {
                XStream xstream = XStreamSingleton.getInstance();
                try (InputStream is = FileUtils.openInputStream(xmlFile)) {
                    Object obj = xstream.fromXML(is);
                    if (obj instanceof OneLevelTree) {
                        tree = (OneLevelTree) obj;
                    }
                } catch (Exception e) {
                    LogUtil.writeToLog(logFile, "Error in parsing " + xmlFile.toPath().toString());
                    LogUtil.writeErrorToLog(logFile, e);
                }

                // Let's get it in the cache
                cache.put(xmlFile.toPath().toString(), tree);
            }
        }

        return tree;
    }

    private void addTree(
            final String date, final String filePath, final String name,
            final String category, final NodeData data) {
        if (!StringUtils.isBlank(name) && !StringUtils.isBlank(category)) {
            OneLevelTree tree = getRecordTree(filePath);
            if (tree == null) {
                String[] fullPathSegments = treeDirAbsPath.split(Pattern.quote(File.separator));
                String filePathRelativeToTreeAccessor = PathUtil.removeLeadingSegments(filePath, File.separator,
                        fullPathSegments.length);
                tree = new OneLevelTree(name, filePathRelativeToTreeAccessor, category);
            }
            tree.getNodes().put(date, data);
            data.setParentTree(tree);

            cache.put(filePath, tree);
            writeTree(filePath, tree);
            alteredNodes.add(tree);
        }
    }

    private void translateElementsToNodes(XmlDataElement element, String date, String path) {
        String name = element.getName();
        if (name == null) {
            LogUtil.writeToLog(logFile, "An element at date = " + date + " and path = " + path + " has no name.");
        }

        File currentDir = new File(path);
        if (!currentDir.exists()) {
            currentDir.mkdirs();
        }
        File nextLocation = new File(currentDir, name);
        String nextLocationPath = nextLocation.toPath().toString();

        if (!(element.getChildren().isEmpty())) {
            for (XmlDataElement child : element.getChildren()) {
                translateElementsToNodes(child, date, nextLocationPath);
            }
        }
        NodeData data = translateElementToNode(element);
        addTree(date, nextLocationPath, name, element.getCategory(), data);
    }

    @SuppressWarnings("unchecked")
    private NodeData translateElementToNode(XmlDataElement element) {
        Set<MomentTuple> momentTuples = new HashSet<>();
        for (String attributeKey : element.getAttributes().keySet()) {
            MomentTuple momentTuple = new MomentTuple(attributeKey, element.getAttributes().get(attributeKey));
            momentTuples.add(momentTuple);
        }

        List<MetaData> metaData = (List<MetaData>) element.getChildren(MetaData.class);
        Map<String, String> metadataMap = new HashMap<>();
        for (MetaData thisMetaData : metaData) {
            metadataMap.put(thisMetaData.getName(), thisMetaData.getValue());
        }

        String units = element.getUnits();
        if (StringUtils.isBlank(units)) {
            units = performanceResultAction.getDefaultMeasurableUnit();
        }
        return new NodeData(units, momentTuples, metadataMap);
    }

    private void writeTree(String filePath, OneLevelTree tree) {
        if (tree != null && !StringUtils.isBlank(filePath)) {
            File file = null;
            try {
                if (!filePath.endsWith(RECORD_XML_FILE)) {
                    filePath = filePath + File.separator + RECORD_XML_FILE;
                }

                file = new File(filePath);
                if (!file.exists()) {
                    boolean success = file.createNewFile();
                    if(success) {
                        FileUtil.setFilePermissions(file, true, true, true);
                    }
                }
            } catch (IOException e) {
                LogUtil.writeToLog(logFile, "Could not create file " + filePath);
                LogUtil.writeErrorToLog(logFile, e);
            }

            try (OutputStream os = FileUtils.openOutputStream(file, false)) {
                XStream xstream = XStreamSingleton.getInstance();
                xstream.toXML(tree, os);
            } catch (Exception e) {
                LogUtil.writeToLog(logFile, "Could not serialize file " + filePath);
                LogUtil.writeErrorToLog(logFile, e);
            }
        }
    }

    private void recalculateValues(Set<OneLevelTree> treesToRecalculate) {       
        if(minDate.equals(CommonConstants.MAX_DATE) || maxDate.equals(CommonConstants.MIN_DATE)) {
            LogUtil.writeToLog(logFile, "****WARNING - minDate and maxDate were not set appropriately.");
            LogUtil.writeToLog(logFile, "****Please make sure your new performance reports use valid dates.");
            return;
        }

        String fullPath = treeDirAbsPath;
        if(!fullPath.endsWith(File.separator)) {
            fullPath = fullPath + File.separator;
        }

        IDatabaseAccessor db = getParentDatabase();
        IFilterAccessor filterAccessor = (IFilterAccessor) db.getAndOpenDatabasePart(IDatabasePartType.FILTERS);
        
        for(OneLevelTree tree : treesToRecalculate) {
            List<String> filterDates = new ArrayList<>();
            FilterData filterData = filterAccessor.getFilterDataByPath(tree.getPath(), false);
            if(filterData != null && !performanceResultAction.shouldUseHiddenValuesForDerivedLines()) {
                filterDates.addAll(filterData.getFilteredDates());
            }

            TreeMap<String, NodeData> nodes = (TreeMap<String, NodeData>) tree.getNodes();
            for(String date : nodes.descendingKeySet()) {
                try {
                    if(DateUtil.isWithinRange(date, minDate, maxDate)) {
                        NodeData node = nodes.get(date);
                        if(node != null) {
                            int rollingRange = performanceResultAction.getRollingRange();

                            for(MomentTuple momentTuple : node.getMomentTuples()) {
                                List<Double> rollingValues =
                                    gatherTemporalData(nodes, date, momentTuple.getType(), rollingRange, filterDates);
                                momentTuple.setAverage(StatUtil.avg(rollingValues));
                                momentTuple.setStd(StatUtil.stdDev(rollingValues));
                            }
                        }
                    }
                } catch(ParseException e) {
                    LogUtil.writeErrorToLog(logFile, e);
                }
            }

            String relativePath = tree.getPath();
            relativePath = OsUtil.convertToOsFileSeparators(relativePath);
            String treeAbsPath = fullPath + relativePath;
            cache.put(treeAbsPath, tree);
            writeTree(treeAbsPath, tree);
        }
    }    
    
    /**
     * Gathers temporal data of a specific type from block trees.
     * 
     * @param blockTree The block tree to extract temporal data from.
     * @param upperDate The date that defines the upper limit for extracting temporal data.
     * @param momentTupleType The type of temporal data to extract.
     * @param lowerRange The lower range specifies how many past data points to gather.
     * @param filterDates A list of dates to ignore.
     * @return The gathered temporal data.
     */
    private List<Double> gatherTemporalData(
            Map<String, NodeData> blockTree, String upperDate, String momentTupleType,
            int lowerRange, List<String> filterDates) {

        List<Double> allData = new ArrayList<>();
        
        for(Entry<String, NodeData> entry : blockTree.entrySet()) {
            String date = entry.getKey();
            NodeData nodeData = entry.getValue();
            
            if(nodeData != null && upperDate.compareTo(date) >= 0 && !filterDates.contains(date)) {
                MomentTuple moment = nodeData.getMomentTuple(momentTupleType);
                if(moment != null) {
                    allData.add(moment.getValue());
                }
            }
        }
        
        List<Double> recentData = new ArrayList<>();
        if(lowerRange > 0 && allData.size() < lowerRange) {
            recentData.addAll(allData);
        } else {
            for(int i = allData.size() - lowerRange; i < allData.size(); i++) {
                if(i >= 0) {
                    recentData.add(allData.get(i));
                }
            }
            return recentData;
        }
        
        return recentData;
    }

    /**
     * https://stackoverflow.com/questions/45321050/java-string-matching-with-wildcards
     * @param glob The original String glob.
     * @return The glob converted to a Java-style regular expression.
     */
    /*package*/ String convertToRegex(String glob) {
        StringBuilder out = new StringBuilder("^");
        for(int i = 0; i < glob.length(); ++i) {
            final char c = glob.charAt(i);
            switch(c) {
                case ' ': out.append("\\s"); break;
                case '*': out.append(".*"); break;
                case '.': out.append("\\."); break;
                case '^': out.append("\\^"); break;
                case '$': out.append("\\$"); break;
                case '\\': out.append("\\\\"); break;
                default: out.append(c);
            }
        }
        out.append('$');
        return out.toString();
    }

    public PerformanceResultAction getPerformanceResultAction() {
        return performanceResultAction;
    }
}