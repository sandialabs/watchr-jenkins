/*******************************************************************************
* Watchr
* ------
* Copyright 2021 National Technology & Engineering Solutions of Sandia, LLC (NTESS).
* Under the terms of Contract DE-NA0003525 with NTESS, the U.S. Government retains
* certain rights in this software.
******************************************************************************/
package gov.sandia.sems.jenkinsutilities.htmlexporter;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;

import javax.xml.parsers.ParserConfigurationException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.xml.sax.SAXException;

import gov.sandia.sems.jenkinsutilities.PerformanceTreeTestUtils;
import gov.sandia.sems.jenkinsutilities.TestFileUtils;
import gov.sandia.sems.jenkinsutilities.db.IDatabaseAccessor;
import gov.sandia.sems.jenkinsutilities.db.IDatabasePartType;
import gov.sandia.sems.jenkinsutilities.htmlgenerator.ConfigContext;
import gov.sandia.sems.jenkinsutilities.htmlgenerator.HtmlGenerator;
import gov.sandia.sems.jenkinsutilities.treeaccessor.DiskTreeAccessor;
import gov.sandia.sems.jenkinsutilities.utilities.LogUtil;

public class HtmlExporterTest {

    private File logFile;
    private IDatabaseAccessor db;
    private ConfigContext context;
    private HtmlGenerator htmlGenerator;

    @Before
    public void setup() {
        logFile = TestFileUtils.initializeTestLogFile(HtmlExporterTest.class);

        try {
            db = PerformanceTreeTestUtils.setupTestDatabase(logFile);
            this.context = new ConfigContext(db, "");

            DiskTreeAccessor treeAccess = (DiskTreeAccessor) db.getDatabasePart(IDatabasePartType.TREE);
            
            ClassLoader classLoader = getClass().getClassLoader();
            URL reportsDir1Url = classLoader.getResource("xml_reports_dataset_1");
            File reportsDir1 = new File(reportsDir1Url.toURI());
            PerformanceTreeTestUtils.loadDatasetIntoSandboxTree(treeAccess, reportsDir1);
        } catch(IOException | URISyntaxException | SAXException | ParserConfigurationException e) {
            Assert.fail(e.getMessage());
        }
        context.setGraphSelectedPath("");
        context.setGraphSelectedMeasurable("cpu-time-max");
        context.setGraphSelectedTimeScale(30);
        context.setShowDescendants(true);
        context.setShowAvgLine(true);
        context.setShowStdDevLine(true);

        String[] dataModes_ = new String[0];
        boolean avgFailIfGreater_     = true;
        boolean stdDevFailIfGreater_  = false;
        htmlGenerator = new HtmlGenerator(context, db, dataModes_, avgFailIfGreater_, stdDevFailIfGreater_, "/", logFile);
    }
    
    @Test
    public void testExport_HappyPath() {
        try {
            HtmlExporter exporter = new HtmlExporter();
            File destFile = new File(Files.createTempDirectory(null).toString());
            exporter.export(destFile, htmlGenerator);
            assertEquals(2, destFile.listFiles().length);
            assertEquals("export1.html", destFile.listFiles()[0].getName());
            assertEquals("js", destFile.listFiles()[1].getName());

            System.out.println(destFile.toString());
        } catch(IOException e) {
            LogUtil.writeErrorToLog(logFile, e);
        }
    }
}
