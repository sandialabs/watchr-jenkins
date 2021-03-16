/*******************************************************************************
* Watchr
* ------
* Copyright 2021 National Technology & Engineering Solutions of Sandia, LLC (NTESS).
* Under the terms of Contract DE-NA0003525 with NTESS, the U.S. Government retains
* certain rights in this software.
******************************************************************************/
package gov.sandia.sems.jenkinsutilities;

import static org.junit.Assert.fail;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.io.FileUtils;
import org.mockito.Mockito;
import org.xml.sax.SAXException;

import gov.sandia.sems.jenkins.semsjppplugin.PerformanceResultAction;
import gov.sandia.sems.jenkinsutilities.db.IDatabaseAccessor;
import gov.sandia.sems.jenkinsutilities.db.IDatabasePart;
import gov.sandia.sems.jenkinsutilities.db.IDatabasePartType;
import gov.sandia.sems.jenkinsutilities.treeaccessor.DiskFilterAccessor;
import gov.sandia.sems.jenkinsutilities.treeaccessor.DiskTreeAccessor;
import gov.sandia.sems.jenkinsutilities.treeaccessor.IFilterAccessor;
import gov.sandia.sems.jenkinsutilities.treeaccessor.ITreeAccessor;
import gov.sandia.sems.jenkinsutilities.views.DiskViewAccessor;
import gov.sandia.sems.jenkinsutilities.views.IViewAccessor;
import gov.sandia.sems.jenkinsutilities.xml.PerformanceReport;
import gov.sandia.sems.jenkinsutilities.xml.deserializers.SaxPerformanceResultDeserializer;

/**
 * Utility methods for building an {@link ITreeAccessor} and loading it with data
 * to use for unit tests.
 *
 * @author Elliott Ridgway
 */
public class PerformanceTreeTestUtils {

    ////////////
    // FIELDS //
    ////////////

    // The names of folders provided in src/test/resources
    public static final String EXPECTED_TREE_DIR = "performance_history_tree";
    public static final String SANDBOX_DIR = "sandbox";
    public static final String VIEW_SANDBOX_DIR = "view_sandbox";

    private static File sandboxDir;

    /////////////
    // UTILITY //
    /////////////

    public static IDatabaseAccessor setupTestDatabase(File logFile) throws IOException, URISyntaxException {
        ClassLoader classLoader = PerformanceTreeTestUtils.class.getClassLoader();
        PerformanceResultAction action = Mockito.mock(PerformanceResultAction.class);
        when(action.shouldUseHiddenValuesForDerivedLines()).thenReturn(false);

        File treeAccessSandboxFile = null;
        File viewAccessSandboxFile = null;
        File filterAccessSandboxFile = null;

        URL sandboxURL = classLoader.getResource(SANDBOX_DIR);
        if(sandboxURL != null) {
            sandboxDir = new File(sandboxURL.toURI());
            if(sandboxDir.exists() && sandboxDir.isDirectory()) {                        
                // Clean up any lingering data in the sandbox directory.
                for(File child : sandboxDir.listFiles()) {
                    if(child.exists() && child.isDirectory()) {
                        FileUtils.deleteDirectory(child);
                    }
                }

                treeAccessSandboxFile = new File(sandboxDir, "performance_history_tree");
                viewAccessSandboxFile = new File(sandboxDir, "performance_views");
                filterAccessSandboxFile = new File(sandboxDir, "performance_history_filters");

                treeAccessSandboxFile.mkdir();
                viewAccessSandboxFile.mkdir();
                filterAccessSandboxFile.mkdir();

                IDatabaseAccessor db = new IDatabaseAccessor(){
                    private ITreeAccessor treeAccess_;
                    private IViewAccessor viewAccess_;
                    private IFilterAccessor filterAccess_;

                    @Override
                    public IDatabasePart getDatabasePart(IDatabasePartType type) {
                        if(type == IDatabasePartType.TREE) {
                            return treeAccess_;
                        }
                        if(type == IDatabasePartType.VIEWS) {
                            return viewAccess_;
                        }
                        if(type == IDatabasePartType.FILTERS) {
                            return filterAccess_;
                        }
                        return null;
                    }

                    @Override
                    public IDatabasePart getAndOpenDatabasePart(IDatabasePartType type) {
                        if(type == IDatabasePartType.TREE) {
                            treeAccess_.open();
                            return treeAccess_;
                        }
                        if(type == IDatabasePartType.VIEWS) {
                            viewAccess_.open();
                            return viewAccess_;
                        }
                        if(type == IDatabasePartType.FILTERS) {
                            filterAccess_.open();
                            return filterAccess_;
                        }
                        return null;
                    }

                    @Override
                    public void setDatabasePart(IDatabasePartType type, IDatabasePart part) {
                        if(type == IDatabasePartType.TREE) {
                            treeAccess_ = (ITreeAccessor) part;
                        } else if(type == IDatabasePartType.VIEWS) {
                            viewAccess_ = (IViewAccessor) part;
                        } else if(type == IDatabasePartType.FILTERS) {
                            filterAccess_ = (IFilterAccessor) part;
                        }
                    }
                };

                db.setDatabasePart(IDatabasePartType.TREE, new DiskTreeAccessor(db, treeAccessSandboxFile.getAbsolutePath(), action, logFile));
                db.setDatabasePart(IDatabasePartType.VIEWS, new DiskViewAccessor(db, viewAccessSandboxFile.getAbsolutePath(), logFile));
                db.setDatabasePart(IDatabasePartType.FILTERS, new DiskFilterAccessor(db, filterAccessSandboxFile.getAbsolutePath(), logFile));
                return db;
            }
        } else {
            fail("Could not find sandbox dir.  URL: " + sandboxURL);
        }

        return null;
    }

    public static boolean teardownTestDatabase() throws IOException, URISyntaxException {
        ClassLoader classLoader = PerformanceTreeTestUtils.class.getClassLoader();

        URL sandboxURL = classLoader.getResource(SANDBOX_DIR);
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
                return true;
            }
        }
        return false;
    }

    /**
     * 
     * @param destinationTree
     * @param expectedDir
     * @param logFile
     * @return
     * @throws IOException
     * @throws URISyntaxException
     */
    public static boolean loadExistingDiskTreeIntoCurrentTree(DiskTreeAccessor destinationTree, File expectedDir, File logFile)
            throws IOException, URISyntaxException {

        File treeDir = new File(destinationTree.getRootPath());

        if(expectedDir.exists() && expectedDir.isDirectory()) {
            FileUtils.copyDirectory(expectedDir, treeDir);
            return true;
        } else {
            fail("Could not find directory to copy: " + expectedDir.getAbsolutePath());
        }
        return false;
    }

    /**
     * Load performance reports from the {@code temporalReportsDir} folder into the provided
     * {@link ITreeAccessor} object.
     * @param treeAccess The ITreeAccessor object to load with new performance report data.
     * @param temporalReportsDir The directory containing the performance reports.
     * @throws IOException Thrown if the performance report files could not be read.
     * @throws SAXException Thrown if the performance report files could not be deserialized.
     * @throws ParserConfigurationException Thrown if the performance report files could not be parsed.
     */
    public static void loadDatasetIntoSandboxTree(ITreeAccessor destinationTree, File temporalReportsDir)
            throws IOException, SAXException, ParserConfigurationException {

        boolean openStatus = destinationTree.open();
        if(openStatus) {
            for(File temporalReport : temporalReportsDir.listFiles()) {
                try(InputStream is = FileUtils.openInputStream(temporalReport)) {
                    SaxPerformanceResultDeserializer deserializer = new SaxPerformanceResultDeserializer();
                    PerformanceReport performanceReport = deserializer.deserialize(is);
                    destinationTree.addReport(performanceReport);
                }
            }
            boolean closeStatus = destinationTree.close();
            if(!closeStatus) {
                fail("Failed to close tree.");
            }
        } else {
            fail("Failed to open tree.");
        }
    }
}