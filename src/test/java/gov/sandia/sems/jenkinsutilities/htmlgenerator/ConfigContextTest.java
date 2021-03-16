/*******************************************************************************
* Watchr
* ------
* Copyright 2021 National Technology & Engineering Solutions of Sandia, LLC (NTESS).
* Under the terms of Contract DE-NA0003525 with NTESS, the U.S. Government retains
* certain rights in this software.
******************************************************************************/
package gov.sandia.sems.jenkinsutilities.htmlgenerator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.ParseException;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import org.junit.Before;
import org.junit.Test;
import org.xml.sax.SAXException;

import gov.sandia.sems.jenkinsutilities.PerformanceTreeTestUtils;
import gov.sandia.sems.jenkinsutilities.db.IDatabaseAccessor;
import gov.sandia.sems.jenkinsutilities.db.IDatabasePartType;
import gov.sandia.sems.jenkinsutilities.treeaccessor.DiskTreeAccessor;
import gov.sandia.sems.jenkinsutilities.treeaccessor.FilterData;
import gov.sandia.sems.jenkinsutilities.treeaccessor.IFilterAccessor;
import gov.sandia.sems.jenkinsutilities.treeaccessor.ITreeAccessor;
import gov.sandia.sems.jenkinsutilities.utilities.LogUtil;

public class ConfigContextTest {

    ////////////
    // FIELDS //
    ////////////

    private static final String LOG_FILE = "logFile.txt";
    private IDatabaseAccessor db;
    private File logFile;

    ////////////
    // BEFORE //
    ////////////
    
    @Before
    public void setup() {
        ClassLoader classLoader = getClass().getClassLoader();

        try {
            URL logFileURL = classLoader.getResource("logFile.txt");
            logFile = new File(logFileURL.toURI());
            if(!logFile.exists()) {
                logFile.createNewFile();
            }
            db = PerformanceTreeTestUtils.setupTestDatabase(logFile);
        } catch(IOException | URISyntaxException e) {
            fail(e.getMessage());
        }
    }    

    ///////////
    // TESTS //
    ///////////

    @Test
    public void testGettersAndSetters() {
        ConfigContext context = new ConfigContext(db, "");
        context.setMainReportMode(MainReportMode.VIEW_FILTER);
        context.setMainReportAction(MainReportAction.SAVE_FILTER);
        context.setSelectedGraphName("graphName");
        context.setSelectedGraphPath("graphPath");

        assertEquals(MainReportMode.VIEW_FILTER, context.getMainReportMode());
        assertEquals(MainReportAction.SAVE_FILTER, context.getMainReportAction());
        assertEquals("graphName", context.getSelectedGraphName());
        assertEquals("graphPath", context.getSelectedGraphPath());
    }

    @Test
    public void testSetSelectedGraphPath_Empty() {
        ConfigContext context = new ConfigContext(db, "");
        context.setSelectedGraphPath("");

        ITreeAccessor tree = (ITreeAccessor) db.getAndOpenDatabasePart(IDatabasePartType.TREE);
        assertEquals(tree.getRootPath(), context.getSelectedGraphPath());
    }

    @Test
    public void testLoadDataForFilterPage_NoFilterDates() {
        ClassLoader classLoader = getClass().getClassLoader();
        try {
            URL logFileURL = classLoader.getResource(LOG_FILE);
            logFile = new File(logFileURL.toURI());
            if(!logFile.exists()) {
                logFile.createNewFile();
            }
            
            DiskTreeAccessor treeAccess = (DiskTreeAccessor) db.getDatabasePart(IDatabasePartType.TREE);
            URL reportsDir1Url = classLoader.getResource("xml_reports_dataset_1");
            File reportsDir1 = new File(reportsDir1Url.toURI());
            PerformanceTreeTestUtils.loadDatasetIntoSandboxTree(treeAccess, reportsDir1);

            ConfigContext context = new ConfigContext(db, "cpu-time-max");
            context.setSelectedGraphName("name");
            context.setSelectedGraphPath("Label_1793032202/Label_2603186");         

            JsonObject filterPageData = context.loadDataForFilterPage();
            assertEquals("name", filterPageData.get(ConfigContext.JSON_PROPERTY_GRAPHNAME).getAsString());
            assertTrue(filterPageData.get(ConfigContext.JSON_PROPERTY_GRAPHPATH).getAsString().contains("Label_1793032202"));

            JsonArray datesArray = filterPageData.get(ConfigContext.JSON_PROPERTY_DATES).getAsJsonArray();
            assertEquals(2, datesArray.size());
        } catch(URISyntaxException | IOException | SAXException | ParserConfigurationException | ParseException e) {
            LogUtil.writeErrorToLog(logFile, e);
            fail(e.getMessage());
        }
    }

    @Test
    public void testLoadDataForFilterPage_WithFilterDates() {
        ClassLoader classLoader = getClass().getClassLoader();
        try {
            URL logFileURL = classLoader.getResource(LOG_FILE);
            logFile = new File(logFileURL.toURI());
            if(!logFile.exists()) {
                logFile.createNewFile();
            }
            
            DiskTreeAccessor treeAccess = (DiskTreeAccessor) db.getDatabasePart(IDatabasePartType.TREE);
            URL reportsDir1Url = classLoader.getResource("xml_reports_dataset_1");
            File reportsDir1 = new File(reportsDir1Url.toURI());
            PerformanceTreeTestUtils.loadDatasetIntoSandboxTree(treeAccess, reportsDir1);

            ConfigContext context = new ConfigContext(db, "cpu-time-max");
            context.setSelectedGraphName("name");
            context.setSelectedGraphPath("Label_1793032202/Label_2603186");

            IFilterAccessor filterAccess = (IFilterAccessor) db.getAndOpenDatabasePart(IDatabasePartType.FILTERS);
            Set<FilterData> filterDataSet = filterAccess.getFilterData();
            FilterData filterData = new FilterData("Label_1793032202/Label_2603186");
            filterData.getFilteredDates().add("2018-01-08T21:59:59");
            filterDataSet.add(filterData);
            filterAccess.close();

            JsonObject filterPageData = context.loadDataForFilterPage();
            assertEquals("name", filterPageData.get(ConfigContext.JSON_PROPERTY_GRAPHNAME).getAsString());
            assertTrue(filterPageData.get(ConfigContext.JSON_PROPERTY_GRAPHPATH).getAsString().contains("Label_1793032202"));

            JsonArray datesArray = filterPageData.get(ConfigContext.JSON_PROPERTY_DATES).getAsJsonArray();
            assertEquals(2, datesArray.size());

            JsonArray filterDatesArray = filterPageData.get(ConfigContext.JSON_PROPERTY_FILTERDATES).getAsJsonArray();
            assertEquals(1, filterDatesArray.size());
            assertEquals("2018-01-08T21:59:59", filterDatesArray.get(0).getAsString());
        } catch(URISyntaxException | IOException | SAXException | ParserConfigurationException | ParseException e) {
            LogUtil.writeErrorToLog(logFile, e);
            fail(e.getMessage());
        }
    }
}