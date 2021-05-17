package gov.sandia.watchr.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import org.junit.Test;

import gov.sandia.watchr.config.GraphDisplayConfig;
import gov.sandia.watchr.util.CommonConstants;

public class JenkinsConfigContextTest {
    
    private final String TEST_DATABASE = "MyDatabase";

    @Test
    public void testConstructor_Default() {
        try {
            File emptyDir = Files.createTempDirectory(null).toFile();

            JenkinsConfigContext context = new JenkinsConfigContext(TEST_DATABASE, emptyDir);
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
