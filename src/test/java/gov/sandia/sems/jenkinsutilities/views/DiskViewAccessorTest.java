/*******************************************************************************
* Watchr
* ------
* Copyright 2021 National Technology & Engineering Solutions of Sandia, LLC (NTESS).
* Under the terms of Contract DE-NA0003525 with NTESS, the U.S. Government retains
* certain rights in this software.
******************************************************************************/
package gov.sandia.sems.jenkinsutilities.views;

import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import gov.sandia.sems.jenkinsutilities.PerformanceTreeTestUtils;
import gov.sandia.sems.jenkinsutilities.db.IDatabaseAccessor;
import gov.sandia.sems.jenkinsutilities.db.IDatabasePartType;
import gov.sandia.sems.jenkinsutilities.utilities.LogUtil;
import gov.sandia.sems.jenkinsutilities.utilities.XStreamSingleton;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import com.thoughtworks.xstream.XStream;

public class DiskViewAccessorTest {

    ////////////
    // FIELDS //
    ////////////

    private static final String LOG_FILE = "logFile.txt";

    private IDatabaseAccessor db;
    private DiskViewAccessor viewAccessor;
    private File logFile;

    ////////////
    // BEFORE //
    ////////////

    @Before
    public void setup() {
        ClassLoader classLoader = getClass().getClassLoader();

        try {
            URL logFileURL = classLoader.getResource(LOG_FILE);
            logFile = new File(logFileURL.toURI());
            if(!logFile.exists()) {
                logFile.createNewFile();
            }

            db = PerformanceTreeTestUtils.setupTestDatabase(logFile);
            viewAccessor = (DiskViewAccessor) db.getDatabasePart(IDatabasePartType.VIEWS);
        } catch(IOException | URISyntaxException e) {
            Assert.fail(e.getMessage());
        }
    }

    ////////////////
    // UNIT TESTS //
    ////////////////

    @Test
    public void testConstructor(){
        LogUtil.writeToLog(logFile, "***DiskViewAccessorTest:testConstructor");
        assertNotNull(viewAccessor);
    }

    @Test
    public void testOpen() {
        LogUtil.writeToLog(logFile, "***DiskViewAccessorTest:testOpen");
        boolean openSuccess = viewAccessor.open();
        assertTrue(openSuccess);
    }

    @Test
    public void testClose_Default() {
        LogUtil.writeToLog(logFile, "***DiskViewAccessorTest:testClose_Default");
        boolean openSuccess = viewAccessor.open();
        assertTrue(openSuccess);
        boolean closeSuccess = viewAccessor.close();
        assertTrue(closeSuccess);
    }

    @Test
    public void test_AddAndRetrieveEmptyView() {
        LogUtil.writeToLog(logFile, "***DiskViewAccessorTest:test_AddAndRetrieveEmptyView");

        List<ViewDataset> viewDatasets = new ArrayList<>();
        View newView = new View("MyFirstView", viewDatasets);

        viewAccessor.addView(newView);

        assertEquals(1, viewAccessor.getAllViews().size());
        assertEquals(newView, viewAccessor.getAllViews().iterator().next());
        assertEquals(newView, viewAccessor.getView(newView.getUUID()));
    }

    @Test
    public void test_AddAndRetrieveViewContainingData() {
        LogUtil.writeToLog(logFile, "***DiskViewAccessorTest:test_AddAndRetrieveViewContainingData");

        List<ViewDataset> viewDatasets = new ArrayList<>();
        viewDatasets.add(new ViewDataset("subgraph_1", ViewDataset.DatasetType.DATA));
        View newView = new View("MySecondView", viewDatasets);

        viewAccessor.addView(newView);

        assertEquals(1, viewAccessor.getAllViews().size());
        assertEquals(newView, viewAccessor.getAllViews().iterator().next());
        assertEquals(newView, viewAccessor.getView(newView.getUUID()));
    }

    @Test
    public void test_AddThenUpdateView() {
        LogUtil.writeToLog(logFile, "***DiskViewAccessorTest:test_AddThenUpdateView");

        List<ViewDataset> viewDatasets1 = new ArrayList<>();
        viewDatasets1.add(new ViewDataset("subgraph_1", ViewDataset.DatasetType.DATA));
        View newView1 = new View("MyFirstView", viewDatasets1);

        List<ViewDataset> viewDatasets2 = new ArrayList<>();
        viewDatasets1.add(new ViewDataset("subgraph_1", ViewDataset.DatasetType.DATA));
        View newView2 = new View("MyFirstView", viewDatasets2);

        viewAccessor.addView(newView1);
        assertEquals(1, viewAccessor.getAllViews().size());
        assertEquals(newView1, viewAccessor.getAllViews().iterator().next());
        assertEquals(newView1, viewAccessor.getView(newView1.getUUID()));

        viewAccessor.replaceView(newView1.getUUID(), newView2);
        assertEquals(1, viewAccessor.getAllViews().size());
        assertEquals(newView2, viewAccessor.getAllViews().iterator().next());
        assertEquals(newView2, viewAccessor.getView(newView2.getUUID()));
        assertNotEquals(newView1, newView2);
        assertNotEquals(newView1, viewAccessor.getView(newView1.getUUID()));
    }

    @Test
    public void testDeleteView() {
        LogUtil.writeToLog(logFile, "***DiskViewAccessorTest:testDeleteView");

        List<ViewDataset> viewDatasets1 = new ArrayList<>();
        viewDatasets1.add(new ViewDataset("subgraph_1", ViewDataset.DatasetType.DATA));
        View newView1 = new View("MyFirstView", viewDatasets1);

        viewAccessor.addView(newView1);
        assertEquals(1, viewAccessor.getAllViews().size());
        assertEquals(newView1, viewAccessor.getAllViews().iterator().next());
        assertEquals(newView1, viewAccessor.getView(newView1.getUUID()));

        viewAccessor.deleteView(newView1);
        assertEquals(0, viewAccessor.getAllViews().size());
        assertNull(viewAccessor.getView(newView1.getUUID()));
    }

    //////////////////
    // SYSTEM TESTS //
    //////////////////

    @Test
    public void testClose_WithData() {
        LogUtil.writeToLog(logFile, "***DiskViewAccessorTest:testClose_WithData");
        boolean openSuccess = viewAccessor.open();
        assertTrue(openSuccess);

        List<ViewDataset> viewDatasets = new ArrayList<>();
        viewDatasets.add(new ViewDataset("subgraph_1", ViewDataset.DatasetType.DATA));
        View newView = new View("MyFirstView", viewDatasets);
        viewAccessor.addView(newView);

        boolean closeSuccess = viewAccessor.close();
        assertTrue(closeSuccess);

        // Test to make sure data was saved to view state file.
        File viewStateFile = new File(viewAccessor.getViewDirAbsPath(), DiskViewAccessor.STATE_XML_FILE);
        assertTrue(viewStateFile.exists());
        assertTrue(!viewStateFile.isDirectory());

        XStream xstream = XStreamSingleton.getInstance();
        try(InputStream is = FileUtils.openInputStream(viewStateFile)){
            Object deSzObj = xstream.fromXML(is);
            assertTrue(deSzObj instanceof DiskViewAccessor);
            
            DiskViewAccessor loadedViewAccessor = (DiskViewAccessor) deSzObj;
            assertEquals(1, loadedViewAccessor.getAllViews().size());
            assertNotNull(loadedViewAccessor.getView(newView.getUUID()));
            assertEquals(1, loadedViewAccessor.getView(newView.getUUID()).getViewDatasets().size());
        } catch(Exception e) {
            LogUtil.writeErrorToLog(logFile, e);
        }
    }
}