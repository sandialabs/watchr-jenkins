/*******************************************************************************
* Watchr
* ------
* Copyright 2021 National Technology & Engineering Solutions of Sandia, LLC (NTESS).
* Under the terms of Contract DE-NA0003525 with NTESS, the U.S. Government retains
* certain rights in this software.
******************************************************************************/
package gov.sandia.sems.jenkinsutilities.treeaccessor;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.xml.sax.SAXException;

import gov.sandia.sems.jenkins.semsjppplugin.PerformanceResultAction;
import gov.sandia.sems.jenkinsutilities.PerformanceTreeTestUtils;
import gov.sandia.sems.jenkinsutilities.db.IDatabaseAccessor;
import gov.sandia.sems.jenkinsutilities.utilities.LogUtil;
import gov.sandia.sems.jenkinsutilities.xml.PerformanceReport;
import gov.sandia.sems.jenkinsutilities.xml.deserializers.SaxPerformanceResultDeserializer;

/**
 * Start with no data in the DiskTreeAccessor at the beginning of each unit test.
 * 
 * @author Elliott Ridgway
 */
public class DiskTreeAccessorStartBlankTest {
    
    ////////////
    // FIELDS //
    ////////////

    private static final String LOG_FILE = "logFile.txt";
    private static final String SANDBOX = "sandbox";
    private static final String XML_REPORT_DATASET_3 = "xml_reports_dataset_3";

    private DiskTreeAccessor treeAccess;
    private PerformanceResultAction action;

    private File logFile;
    private File sandboxDir;
    
    ////////////////////////
    // SETUP AND TEARDOWN //
    ////////////////////////

    @Before
    public void setup() {
        action = Mockito.mock(PerformanceResultAction.class);
        ClassLoader classLoader = getClass().getClassLoader();

        try {
            URL logFileURL = classLoader.getResource(LOG_FILE);
            logFile = new File(logFileURL.toURI());
            if(!logFile.exists()) {
                logFile.createNewFile();
            }

            URL sandboxURL = classLoader.getResource(SANDBOX);
            if(sandboxURL != null) {
                sandboxDir = new File(sandboxURL.toURI());
                if(sandboxDir.exists() && sandboxDir.isDirectory()) {                    
                    // Set up the tree.
                    IDatabaseAccessor db = PerformanceTreeTestUtils.setupTestDatabase(logFile);
                    treeAccess = new DiskTreeAccessor(db, sandboxDir.getAbsolutePath(), action, logFile);
                }
            } else {
                Assert.fail("Could not find sandbox dir.  URL: " + sandboxURL);
            }
        } catch(IOException | URISyntaxException e) {
            Assert.fail(e.getMessage());
        }
    }

    @After
    public void teardown() {
        ClassLoader classLoader = getClass().getClassLoader();
        try {
            URL sandboxURL = classLoader.getResource(SANDBOX);
            if(sandboxURL != null) {                
                sandboxDir = new File(sandboxURL.toURI());
                if(sandboxDir.exists() && sandboxDir.isDirectory()) {
                    for(File child : sandboxDir.listFiles()) {
                        if(child.isDirectory()) {
                            FileUtils.deleteDirectory(child);
                        } else {
                            child.delete();
                        }
                    }
                }
            }
        } catch(URISyntaxException | IOException e) {
            LogUtil.writeErrorToLog(logFile, e);
            Assert.fail(e.getMessage());
        }
    }

    ///////////////////////
    // INTEGRATION TESTS //
    ///////////////////////

    @Test
    public void testRootNodeFileExists() {
        LogUtil.writeToLog(logFile, "***DiskTreeAccessorStartBlankTest:testRootNodeFileExists");

        ClassLoader classLoader = getClass().getClassLoader();

        // First, load some reports into the tree.
        boolean openStatus = treeAccess.open();
        assertTrue(openStatus);
        try {
            URL reportsDir1Url = classLoader.getResource(XML_REPORT_DATASET_3);
            File reportsDir1 = new File(reportsDir1Url.toURI());

            for(File reportFile : reportsDir1.listFiles()) {
                try(InputStream is = FileUtils.openInputStream(reportFile)) {
                    SaxPerformanceResultDeserializer deserializer = new SaxPerformanceResultDeserializer();
                    PerformanceReport performanceReport = deserializer.deserialize(is);
                    treeAccess.addReport(performanceReport);
                }
            }
        } catch(IOException | ParserConfigurationException | SAXException | URISyntaxException e) {
            Assert.fail(e.getMessage());
        } finally {
            // Close the tree, which triggers calculation of average and standard deviation lines.
            treeAccess.close();
        }

        OneLevelTree rootTree = treeAccess.getNodeAt("");
        assertNotNull(rootTree);
        assertFalse(rootTree.getNodes().isEmpty());
    }
}