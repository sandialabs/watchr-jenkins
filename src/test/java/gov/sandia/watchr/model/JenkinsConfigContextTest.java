package gov.sandia.watchr.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Collection;

import org.acegisecurity.AccessDeniedException;
import org.junit.Test;

import gov.sandia.watchr.config.GraphDisplayConfig;
import gov.sandia.watchr.util.CommonConstants;
import hudson.model.FreeStyleProject;
import hudson.model.Item;
import hudson.model.ItemGroup;

public class JenkinsConfigContextTest {
    
    private final String TEST_DATABASE = "MyDatabase";

    @Test
    public void testConstructor_Default() {
        try {
            File emptyDir = Files.createTempDirectory(null).toFile();

            ItemGroup<Item> parent = new ItemGroup<Item>() {
                @Override public File getRootDir() { return emptyDir; }
                @Override public void save() throws IOException { }
                @Override public String getDisplayName() { return null; }
                @Override public String getFullDisplayName() { return null; }
                @Override public String getFullName() { return null; }
                @Override public Item getItem(String arg0) throws AccessDeniedException { return null; }
                @Override public Collection<Item> getItems() { return null; }
                @Override public File getRootDirFor(Item arg0) { return null; }
                @Override public String getUrl() { return null; }
                @Override public String getUrlChildPrefix() { return null; }
                @Override public void onDeleted(Item arg0) throws IOException { }
            };
            FreeStyleProject job = new FreeStyleProject(parent, TEST_DATABASE);
            JenkinsConfigContext context = new JenkinsConfigContext(job);
            assertEquals(TEST_DATABASE, context.getDatabaseName());

            GraphDisplayConfig graphDisplayConfig = context.getGraphDisplayConfig();
            assertEquals(CommonConstants.ROOT_PATH_ALIAS, graphDisplayConfig.getNextPlotDbLocation());
            assertEquals(CommonConstants.ROOT_PATH_ALIAS, graphDisplayConfig.getLastPlotDbLocation());
            assertEquals(1, graphDisplayConfig.getPage());
            assertEquals(JenkinsConfigContext.PARAM_DFLT_GRAPHS_PER_ROW, graphDisplayConfig.getGraphsPerRow());
            assertEquals(JenkinsConfigContext.PARAM_DFLT_GRAPH_WIDTH, graphDisplayConfig.getGraphWidth());
            assertEquals(JenkinsConfigContext.PARAM_DFLT_GRAPH_HEIGHT, graphDisplayConfig.getGraphHeight());
            assertEquals(JenkinsConfigContext.PARAM_DFLT_TIME_SCALE, graphDisplayConfig.getDisplayRange());
            assertEquals(JenkinsConfigContext.PARAM_DFLT_ROUND_TO, graphDisplayConfig.getDisplayedDecimalPlaces());
            assertEquals(JenkinsConfigContext.PARAM_DFLT_CATEGORY, graphDisplayConfig.getDisplayCategory());
        } catch(IOException e) {
            fail(e.getMessage());
        }
    }
}
