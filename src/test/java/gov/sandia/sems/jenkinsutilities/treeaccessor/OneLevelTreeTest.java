/*******************************************************************************
* Watchr
* ------
* Copyright 2021 National Technology & Engineering Solutions of Sandia, LLC (NTESS).
* Under the terms of Contract DE-NA0003525 with NTESS, the U.S. Government retains
* certain rights in this software.
******************************************************************************/
package gov.sandia.sems.jenkinsutilities.treeaccessor;

import gov.sandia.sems.jenkins.semsjppplugin.Keywords;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.junit.Assert.assertFalse;

/**
 *
 * @author Elliott Ridgway
 */
public class OneLevelTreeTest {
    
    @Test
    public void testHasCategory() {
        String type1 = "value";
        String type2 = "cpu";
        String type3 = "wall";
        
        String units = "seconds";
        Map<String, String> metadata = new HashMap<>();
        
        Set<MomentTuple> momentTuples1 = new HashSet<>();
        Set<MomentTuple> momentTuples2 = new HashSet<>();
        Set<MomentTuple> momentTuples3 = new HashSet<>();
        
        momentTuples1.add(new MomentTuple(type1, 3.0, 2.0, 1.0));
        momentTuples2.add(new MomentTuple(type1, 4.0, 2.0, 1.0));
        momentTuples3.add(new MomentTuple(type1, 5.0, 2.0, 1.0));
        
        momentTuples1.add(new MomentTuple(type2, 3.0, 2.0, 1.0));
        momentTuples2.add(new MomentTuple(type2, 2.0, 2.0, 1.0));
        momentTuples3.add(new MomentTuple(type2, 0.0, 2.0, 1.0));
        
        momentTuples1.add(new MomentTuple(type3, 3.0, 2.0, 1.0));
        momentTuples2.add(new MomentTuple(type3, 1.5, 2.0, 1.0));
        momentTuples3.add(new MomentTuple(type3, 1.5, 2.0, 1.0));
        
        NodeData nodeData1 = new NodeData(units, momentTuples1, metadata);
        NodeData nodeData2 = new NodeData(units, momentTuples2, metadata);
        NodeData nodeData3 = new NodeData(units, momentTuples3, metadata);
        
        TreeMap<String, NodeData> dateMap = new TreeMap<>();
        dateMap.put("2018-12-17", nodeData1);
        dateMap.put("2018-12-18", nodeData2);
        dateMap.put("2018-12-19", nodeData3);
        
        OneLevelTree tree1 = new OneLevelTree("A", "/", Keywords.CATEGORY_TIMING.get());
        tree1.getNodes().putAll(dateMap);
        OneLevelTree tree2 = new OneLevelTree("B", "/", Keywords.CATEGORY_METRIC.get());
        OneLevelTree tree3 = new OneLevelTree("C", "/", Keywords.CATEGORY_METRIC.get());
        
        List<OneLevelTree> trees = new ArrayList<>();
        trees.add(tree1);
        trees.add(tree2);
        trees.add(tree3);
        
        Assert.assertTrue(OneLevelTree.hasCategory(trees, Keywords.CATEGORY_TIMING.get()));
        Assert.assertTrue(OneLevelTree.hasCategory(trees, Keywords.CATEGORY_METRIC.get()));
        Assert.assertFalse(OneLevelTree.hasCategory(trees, Keywords.CATEGORY_METADATA.get()));
    }
    
    @Test
    public void testDataIsFailure() {
        String type1 = "value";
        String type2 = "cpu";
        String type3 = "wall";
        String type4 = "memory";
        
        String units = "seconds";
        Map<String, String> metadata = new HashMap<>();
        
        Set<MomentTuple> momentTuples1 = new HashSet<>();
        Set<MomentTuple> momentTuples2 = new HashSet<>();
        Set<MomentTuple> momentTuples3 = new HashSet<>();
        
        // Value stays above avg and std
        momentTuples1.add(new MomentTuple(type1, 3.0, 2.0, 1.0));
        momentTuples2.add(new MomentTuple(type1, 4.0, 2.0, 1.0));
        momentTuples3.add(new MomentTuple(type1, 5.0, 2.0, 1.0));
        
        // Value falls below avg and std
        momentTuples1.add(new MomentTuple(type2, 3.0, 2.0, 1.0));
        momentTuples2.add(new MomentTuple(type2, 2.0, 2.0, 1.0));
        momentTuples3.add(new MomentTuple(type2, 0.0, 2.0, 1.0));
        
        // Value falls below avg, but not std
        momentTuples1.add(new MomentTuple(type3, 3.0, 2.0, 1.0));
        momentTuples2.add(new MomentTuple(type3, 1.5, 2.0, 1.0));
        momentTuples3.add(new MomentTuple(type3, 1.5, 2.0, 1.0));

        // Value falls below avg+std, but not avg
        momentTuples1.add(new MomentTuple(type4, 5.0, 2.0, 1.0));
        momentTuples2.add(new MomentTuple(type4, 4.0, 2.0, 1.0));
        momentTuples3.add(new MomentTuple(type4, 2.5, 2.0, 1.0));
        
        NodeData nodeData1 = new NodeData(units, momentTuples1, metadata);
        NodeData nodeData2 = new NodeData(units, momentTuples2, metadata);
        NodeData nodeData3 = new NodeData(units, momentTuples3, metadata);
        
        TreeMap<String, NodeData> dateMap = new TreeMap<>();
        dateMap.put("2018-12-17", nodeData1);
        dateMap.put("2018-12-18", nodeData2);
        dateMap.put("2018-12-19", nodeData3);
        
        boolean avgFailIfGreater = true;
        boolean stdDevFailIfGreater = false;

        OneLevelTree tree = new OneLevelTree("", "", "");
        tree.getNodes().putAll(dateMap);
        
        assertTrue(tree.dataIsFailure(type1, avgFailIfGreater, stdDevFailIfGreater));
        assertFalse(tree.dataIsFailure(type2, avgFailIfGreater, stdDevFailIfGreater));
        assertFalse(tree.dataIsFailure(type3, avgFailIfGreater, stdDevFailIfGreater));
        assertTrue(tree.dataIsFailure(type4, avgFailIfGreater, stdDevFailIfGreater));
        
        avgFailIfGreater = false;
        stdDevFailIfGreater = false;
        
        assertFalse(tree.dataIsFailure(type1, avgFailIfGreater, stdDevFailIfGreater));
        assertFalse(tree.dataIsFailure(type2, avgFailIfGreater, stdDevFailIfGreater));
        assertFalse(tree.dataIsFailure(type3, avgFailIfGreater, stdDevFailIfGreater));
        assertFalse(tree.dataIsFailure(type4, avgFailIfGreater, stdDevFailIfGreater));
        
        avgFailIfGreater = true;
        stdDevFailIfGreater = true;
        
        assertTrue(tree.dataIsFailure(type1, avgFailIfGreater, stdDevFailIfGreater));
        assertFalse(tree.dataIsFailure(type2, avgFailIfGreater, stdDevFailIfGreater));
        assertFalse(tree.dataIsFailure(type3, avgFailIfGreater, stdDevFailIfGreater));
        assertTrue(tree.dataIsFailure(type4, avgFailIfGreater, stdDevFailIfGreater));

        avgFailIfGreater = false;
        stdDevFailIfGreater = true;
        
        assertTrue(tree.dataIsFailure(type1, avgFailIfGreater, stdDevFailIfGreater));
        assertFalse(tree.dataIsFailure(type2, avgFailIfGreater, stdDevFailIfGreater));
        assertFalse(tree.dataIsFailure(type3, avgFailIfGreater, stdDevFailIfGreater));
        assertFalse(tree.dataIsFailure(type4, avgFailIfGreater, stdDevFailIfGreater));
    }

    @Test
    public void testIsEmpty_TrueBecauseActuallyEmpty() {
        OneLevelTree emptyTree = new OneLevelTree("name", "path", "category");
        assertTrue(emptyTree.isEmpty());
    }

    @Test
    public void testIsEmpty_TrueBecauseInternalMapsAreEmpty() {
        OneLevelTree tree = new OneLevelTree("name", "path", "category");
        Map<String, NodeData> internalMap = new HashMap<>();
        tree.getNodes().putAll(internalMap);
        assertTrue(tree.isEmpty());
    }

    @Test
    public void testIsEmpty_TrueBecauseAllZeroes() {
        String type1 = "value";
        String type2 = "cpu";
        String type3 = "wall";
        
        String units = "seconds";
        Map<String, String> metadata = new HashMap<>();
        
        Set<MomentTuple> momentTuples1 = new HashSet<>();
        Set<MomentTuple> momentTuples2 = new HashSet<>();
        Set<MomentTuple> momentTuples3 = new HashSet<>();
        
        momentTuples1.add(new MomentTuple(type1, 0.0, 0.0, 0.0));
        momentTuples2.add(new MomentTuple(type1, 0.0, 0.0, 0.0));
        momentTuples3.add(new MomentTuple(type1, 0.0, 0.0, 0.0));
        
        momentTuples1.add(new MomentTuple(type2, 0.0, 0.0, 0.0));
        momentTuples2.add(new MomentTuple(type2, 0.0, 0.0, 0.0));
        momentTuples3.add(new MomentTuple(type2, 0.0, 0.0, 0.0));
        
        momentTuples1.add(new MomentTuple(type3, 0.0, 0.0, 0.0));
        momentTuples2.add(new MomentTuple(type3, 0.0, 0.0, 0.0));
        momentTuples3.add(new MomentTuple(type3, 0.0, 0.0, 0.0));
        
        NodeData nodeData1 = new NodeData(units, momentTuples1, metadata);
        NodeData nodeData2 = new NodeData(units, momentTuples2, metadata);
        NodeData nodeData3 = new NodeData(units, momentTuples3, metadata);
        
        TreeMap<String, NodeData> dateMap = new TreeMap<>();
        dateMap.put("2018-12-17", nodeData1);
        dateMap.put("2018-12-18", nodeData2);
        dateMap.put("2018-12-19", nodeData3);

        OneLevelTree tree = new OneLevelTree("name", "path", "category");
        tree.getNodes().putAll(dateMap);
        assertTrue(tree.isEmpty());
    }

    @Test
    public void testIsEmpty_FalseBecauseDataExists() {
        String type1 = "value";
        String type2 = "cpu";
        String type3 = "wall";
        String type4 = "memory";
        
        String units = "seconds";
        Map<String, String> metadata = new HashMap<>();
        
        Set<MomentTuple> momentTuples1 = new HashSet<>();
        Set<MomentTuple> momentTuples2 = new HashSet<>();
        Set<MomentTuple> momentTuples3 = new HashSet<>();
        
        // Value stays above avg and std
        momentTuples1.add(new MomentTuple(type1, 3.0, 2.0, 1.0));
        momentTuples2.add(new MomentTuple(type1, 4.0, 2.0, 1.0));
        momentTuples3.add(new MomentTuple(type1, 5.0, 2.0, 1.0));
        
        // Value falls below avg and std
        momentTuples1.add(new MomentTuple(type2, 3.0, 2.0, 1.0));
        momentTuples2.add(new MomentTuple(type2, 2.0, 2.0, 1.0));
        momentTuples3.add(new MomentTuple(type2, 0.0, 2.0, 1.0));
        
        // Value falls below avg, but not std
        momentTuples1.add(new MomentTuple(type3, 3.0, 2.0, 1.0));
        momentTuples2.add(new MomentTuple(type3, 1.5, 2.0, 1.0));
        momentTuples3.add(new MomentTuple(type3, 1.5, 2.0, 1.0));

        // Value falls below avg+std, but not avg
        momentTuples1.add(new MomentTuple(type4, 5.0, 2.0, 1.0));
        momentTuples2.add(new MomentTuple(type4, 4.0, 2.0, 1.0));
        momentTuples3.add(new MomentTuple(type4, 2.5, 2.0, 1.0));
        
        NodeData nodeData1 = new NodeData(units, momentTuples1, metadata);
        NodeData nodeData2 = new NodeData(units, momentTuples2, metadata);
        NodeData nodeData3 = new NodeData(units, momentTuples3, metadata);
        
        TreeMap<String, NodeData> dateMap = new TreeMap<>();
        dateMap.put("2018-12-17", nodeData1);
        dateMap.put("2018-12-18", nodeData2);
        dateMap.put("2018-12-19", nodeData3);

        OneLevelTree tree = new OneLevelTree("name", "path", "category");
        tree.getNodes().putAll(dateMap);
        assertFalse(tree.isEmpty());
    }

    @Test
    public void testDataIsFailure_WithDataFilter() {
        String type1 = "value";
        String type2 = "cpu";
        String type3 = "wall";
        String type4 = "memory";
        
        String units = "seconds";
        Map<String, String> metadata = new HashMap<>();
        
        Set<MomentTuple> momentTuples1 = new HashSet<>();
        Set<MomentTuple> momentTuples2 = new HashSet<>();
        Set<MomentTuple> momentTuples3 = new HashSet<>();
        
        // Value stays above avg and std
        momentTuples1.add(new MomentTuple(type1, 3.0, 2.0, 1.0));
        momentTuples2.add(new MomentTuple(type1, 4.0, 2.0, 1.0));
        momentTuples3.add(new MomentTuple(type1, 5.0, 2.0, 1.0));
        
        // Value falls below avg and std
        momentTuples1.add(new MomentTuple(type2, 3.0, 2.0, 1.0));
        momentTuples2.add(new MomentTuple(type2, 4.0, 2.0, 1.0));
        momentTuples3.add(new MomentTuple(type2, 0.0, 2.0, 1.0));
        
        // Value falls below avg, but not std
        momentTuples1.add(new MomentTuple(type3, 3.0, 2.0, 1.0));
        momentTuples2.add(new MomentTuple(type3, 4.0, 2.0, 1.0));
        momentTuples3.add(new MomentTuple(type3, 1.5, 2.0, 1.0));

        // Value falls below avg+std, but not avg
        momentTuples1.add(new MomentTuple(type4, 5.0, 2.0, 1.0));
        momentTuples2.add(new MomentTuple(type4, 4.0, 2.0, 1.0));
        momentTuples3.add(new MomentTuple(type4, 2.5, 2.0, 1.0));
        
        NodeData nodeData1 = new NodeData(units, momentTuples1, metadata);
        NodeData nodeData2 = new NodeData(units, momentTuples2, metadata);
        NodeData nodeData3 = new NodeData(units, momentTuples3, metadata);
        
        TreeMap<String, NodeData> dateMap = new TreeMap<>();
        dateMap.put("2018-12-17", nodeData1);
        dateMap.put("2018-12-18", nodeData2);
        dateMap.put("2018-12-19", nodeData3);

        List<String> datesToIgnore = new ArrayList<>();
        datesToIgnore.add("2018-12-19");
        
        boolean avgFailIfGreater = true;
        boolean stdDevFailIfGreater = false;

        OneLevelTree tree = new OneLevelTree("", "", "");
        tree.getNodes().putAll(dateMap);
        
        assertTrue(tree.dataIsFailure(type1, avgFailIfGreater, stdDevFailIfGreater, datesToIgnore));
        assertTrue(tree.dataIsFailure(type2, avgFailIfGreater, stdDevFailIfGreater, datesToIgnore));
        assertTrue(tree.dataIsFailure(type3, avgFailIfGreater, stdDevFailIfGreater, datesToIgnore));
        assertTrue(tree.dataIsFailure(type4, avgFailIfGreater, stdDevFailIfGreater, datesToIgnore));
        
        avgFailIfGreater = false;
        stdDevFailIfGreater = false;
        
        assertFalse(tree.dataIsFailure(type1, avgFailIfGreater, stdDevFailIfGreater, datesToIgnore));
        assertFalse(tree.dataIsFailure(type2, avgFailIfGreater, stdDevFailIfGreater, datesToIgnore));
        assertFalse(tree.dataIsFailure(type3, avgFailIfGreater, stdDevFailIfGreater, datesToIgnore));
        assertFalse(tree.dataIsFailure(type4, avgFailIfGreater, stdDevFailIfGreater, datesToIgnore));
        
        avgFailIfGreater = true;
        stdDevFailIfGreater = true;
        
        assertTrue(tree.dataIsFailure(type1, avgFailIfGreater, stdDevFailIfGreater, datesToIgnore));
        assertTrue(tree.dataIsFailure(type2, avgFailIfGreater, stdDevFailIfGreater, datesToIgnore));
        assertTrue(tree.dataIsFailure(type3, avgFailIfGreater, stdDevFailIfGreater, datesToIgnore));
        assertTrue(tree.dataIsFailure(type4, avgFailIfGreater, stdDevFailIfGreater, datesToIgnore));

        avgFailIfGreater = false;
        stdDevFailIfGreater = true;
        
        assertTrue(tree.dataIsFailure(type1, avgFailIfGreater, stdDevFailIfGreater, datesToIgnore));
        assertTrue(tree.dataIsFailure(type2, avgFailIfGreater, stdDevFailIfGreater, datesToIgnore));
        assertTrue(tree.dataIsFailure(type3, avgFailIfGreater, stdDevFailIfGreater, datesToIgnore));
        assertTrue(tree.dataIsFailure(type4, avgFailIfGreater, stdDevFailIfGreater, datesToIgnore));
    }

    @Test
    public void testIsEmptyDataSetInDateRange() {
        try {
            String type_value = "value";
            String type_cpu = "cpu";
            String type_wall = "wall";
            String type_memory = "memory";
            
            String units = "seconds";
            Map<String, String> metadata = new HashMap<>();
            
            Set<MomentTuple> momentTuples1 = new HashSet<>();
            Set<MomentTuple> momentTuples2 = new HashSet<>();
            Set<MomentTuple> momentTuples3 = new HashSet<>();
            
            momentTuples1.add(new MomentTuple(type_value, 3.0, 2.0, 1.0));
            momentTuples2.add(new MomentTuple(type_value, 4.0, 2.0, 1.0));
            momentTuples3.add(new MomentTuple(type_value, 5.0, 2.0, 1.0));
            
            momentTuples1.add(new MomentTuple(type_cpu, 3.0, 2.0, 1.0));
            
            momentTuples1.add(new MomentTuple(type_wall, 3.0, 2.0, 1.0));
            momentTuples2.add(new MomentTuple(type_wall, 1.5, 2.0, 1.0));
            momentTuples3.add(new MomentTuple(type_wall, 1.5, 2.0, 1.0));

            momentTuples1.add(new MomentTuple(type_memory, 5.0, 2.0, 1.0));
            momentTuples2.add(new MomentTuple(type_memory, 4.0, 2.0, 1.0));
            momentTuples3.add(new MomentTuple(type_memory, 2.5, 2.0, 1.0));
            
            NodeData nodeData1 = new NodeData(units, momentTuples1, metadata);
            NodeData nodeData2 = new NodeData(units, momentTuples2, metadata);
            NodeData nodeData3 = new NodeData(units, momentTuples3, metadata);
            
            TreeMap<String, NodeData> dateMap = new TreeMap<>();
            dateMap.put("2018-12-17", nodeData1);
            dateMap.put("2018-12-18", nodeData2);
            dateMap.put("2018-12-19", nodeData3);

            OneLevelTree tree = new OneLevelTree("name", "path", "category");
            tree.getNodes().putAll(dateMap);
            assertTrue(tree.isEmptyDataSetInDateRange("cpu", "2018-12-18", "2018-12-19"));
        } catch(ParseException e) {
            fail(e.getMessage());
        }
    }
}
