/*******************************************************************************
* Watchr
* ------
* Copyright 2021 National Technology & Engineering Solutions of Sandia, LLC (NTESS).
* Under the terms of Contract DE-NA0003525 with NTESS, the U.S. Government retains
* certain rights in this software.
******************************************************************************/
package gov.sandia.sems.jenkinsutilities.treeaccessor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import com.google.gson.JsonObject;

import org.junit.Test;

public class NodeDataTest {

    @Test
    public void testToJson() {
        String units = "seconds";
        String type1 = "value";

        Set<MomentTuple> momentTuples = new LinkedHashSet<>();
        momentTuples.add(new MomentTuple(type1, 3.0, 2.0, 1.0));
        momentTuples.add(new MomentTuple(type1, 4.0, 2.0, 1.0));
        momentTuples.add(new MomentTuple(type1, 5.0, 2.0, 1.0));

        Map<String, String> metadata = new HashMap<>();
        metadata.put("A", "1");
        metadata.put("B", "2");
        metadata.put("C", "3");

        NodeData nodeData = new NodeData(units, momentTuples, metadata);
        JsonObject jsonObject = nodeData.toJson("2020-09-16", type1, true, true, true, 2);

        assertEquals(1, jsonObject.size());
        assertNotNull(jsonObject.get("2020-09-16"));
        JsonObject childData = (JsonObject) jsonObject.get("2020-09-16");
        assertEquals(2, childData.size());
        assertNotNull(childData.get("momentTuple"));
        assertNotNull(childData.get("metadata"));

        JsonObject momentTupleResult = (JsonObject) childData.get("momentTuple");
        JsonObject metadataResult = (JsonObject) childData.get("metadata");

        System.out.println(momentTupleResult.toString());

        assertEquals(4, momentTupleResult.size());
        assertEquals("value", momentTupleResult.get("type").getAsString());
        assertEquals("3.0", momentTupleResult.get("value").getAsString());
        assertEquals("2.0", momentTupleResult.get("avg").getAsString());
        assertEquals("3.0", momentTupleResult.get("std").getAsString());

        assertEquals(3, metadataResult.size());
        assertEquals("1", metadataResult.get("A").getAsString());
        assertEquals("2", metadataResult.get("B").getAsString());
        assertEquals("3", metadataResult.get("C").getAsString());
    }
}
