/*******************************************************************************
* Watchr
* ------
* Copyright 2021 National Technology & Engineering Solutions of Sandia, LLC (NTESS).
* Under the terms of Contract DE-NA0003525 with NTESS, the U.S. Government retains
* certain rights in this software.
******************************************************************************/
package gov.sandia.watchr.buildsteps;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.apache.commons.lang3.StringUtils;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;

import gov.sandia.watchr.WatchrCoreApp;
import gov.sandia.watchr.WatchrJenkinsApp;
import gov.sandia.watchr.actions.PerformanceResultAction;
import gov.sandia.watchr.config.GraphDisplayConfig;
import gov.sandia.watchr.log.ILogger;
import gov.sandia.watchr.log.WatchrJenkinsLogger;
import gov.sandia.watchr.model.JenkinsConfigContext;
import gov.sandia.watchr.parse.WatchrParseException;
import gov.sandia.watchr.util.CommonConstants;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.AbstractProject;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Publisher;
import hudson.tasks.Recorder;
import hudson.util.FormValidation;
import io.jenkins.cli.shaded.org.apache.commons.io.FileUtils;
import hudson.model.Descriptor;
import jenkins.tasks.SimpleBuildStep;

/**
 * The PerformanceRecorder class is the starting point for a new Jenkins build
 * that calls into Watchr as a post-build step. It is primarily responsible for
 * loading the performance reports from the job's workspace into the data tree
 * accessor.
 * 
 * @author Elliott Ridgway, Lawrence Allen
 */
public final class PerformanceRecorder extends Recorder implements SimpleBuildStep {

    ////////////
    // FIELDS //
    ////////////

    public final String watchrConfigJson;
    public final String watchrConfigFilepath;
    public final String performanceReportsLocation;
    public final boolean exportGraphs;

    /////////////////
    // CONSTRUCTOR //
    /////////////////

    // Fields in config.jelly must match the parameter names in the "DataBoundConstructor"
    @DataBoundConstructor
    public PerformanceRecorder(
            String watchrConfigJson, String watchrConfigFilepath,
            String performanceReportsLocation, boolean exportGraphs) {

        this.watchrConfigJson = watchrConfigJson;
        this.watchrConfigFilepath = watchrConfigFilepath;
        this.performanceReportsLocation = performanceReportsLocation;
        this.exportGraphs = exportGraphs;
    }

    //////////////
    // OVERRIDE //
    //////////////

    @Override
    public void perform(
            Run<?, ?> build, FilePath workspace, Launcher launcher, TaskListener listener)
            throws IOException, InterruptedException {

        PerformanceResultAction action = build.getAction(PerformanceResultAction.class);
        if(action == null) {
            build.addAction(new PerformanceResultAction(build));
        } else {
            build.save();
        }

        getAndParsePerformanceReports(build, workspace);
    }

    @Override
    public BuildStepMonitor getRequiredMonitorService() {
        return BuildStepMonitor.NONE;
    }

    /////////////
    // PRIVATE //
    /////////////

    private void getAndParsePerformanceReports(Run<?, ?> build, FilePath workspace) throws InterruptedException {
        WatchrCoreApp app = WatchrCoreApp.getInstance();
        ILogger logger = new WatchrJenkinsLogger(WatchrJenkinsApp.getLogForBuild(build));
        app.setLogger(logger);

        JenkinsConfigContext configContext =
            WatchrJenkinsApp.getConfigContextOrDefault(build.getParent());
        String dbName = configContext.getDatabaseName();

        FilePath perfResultsFilePath = workspace.child(performanceReportsLocation);

        try {
            File perfResultsFile = new File(perfResultsFilePath.toURI());
            File[] childFiles = perfResultsFile.listFiles();
            String configFileContents = getWatchrConfigFileContents(workspace);

            if(childFiles != null && childFiles.length > 0 && StringUtils.isNotBlank(configFileContents)) {
                logger.logInfo("Requesting new plots from " + perfResultsFile.getAbsolutePath() + " for db " + dbName + " (watchr-jenkins)");
                app.processConfigFile(dbName, perfResultsFile, configFileContents);

                if(exportGraphs) {
                    doExportGraphs(build, workspace, dbName);
                }
            } else if(childFiles == null || childFiles.length == 0) {
                logger.logError("No performance reports were located at path " + perfResultsFile.getAbsolutePath());
            } else if(StringUtils.isBlank(configFileContents)) {
                logger.logError("No Watchr configuration specified!");
            }
        } catch (IOException e1) {
            logger.logError("An error occurred reading the file path: ", e1);
        } catch(WatchrParseException e2) {
            logger.logError("An error occurred extracting new plot data: ", e2.getOriginalException());
        } catch (InterruptedException e3) {
            throw e3;
        }
    }

    private String getWatchrConfigFileContents(FilePath workspace) throws IOException, InterruptedException {
        if(StringUtils.isNotBlank(watchrConfigFilepath)) {
            FilePath configFilePath = workspace.child(watchrConfigFilepath);
            File configFile = new File(configFilePath.toURI());
            if(configFile.exists()) {
                return FileUtils.readFileToString(configFile, StandardCharsets.UTF_8);
            } else {
                WatchrCoreApp app = WatchrCoreApp.getInstance();
                app.getLogger().logError("Path to config file was specified (" + watchrConfigFilepath + "), but file does not exist.");
            }
        } else if(StringUtils.isNotBlank(watchrConfigJson)) {
            return watchrConfigJson;
        }
        return "";
    }

    private void doExportGraphs(Run<?, ?> build, FilePath workspace, String databaseName) throws IOException, InterruptedException {
        long timestamp = System.currentTimeMillis();
        String graphExportDestinationName = "watchrGraphExport_" + timestamp;
        FilePath graphExportDestinationFilePath = workspace.child(graphExportDestinationName);
        graphExportDestinationFilePath.mkdirs();
        
        File graphExportDestination = new File(graphExportDestinationFilePath.toURI());

        JenkinsConfigContext configContext =
            WatchrJenkinsApp.getConfigContextOrDefault(build.getParent());
        GraphDisplayConfig exportDisplayConfig = new GraphDisplayConfig(configContext.getGraphDisplayConfig());
        exportDisplayConfig.setLastPlotDbLocation(CommonConstants.ROOT_PATH_ALIAS);
        exportDisplayConfig.setNextPlotDbLocation(CommonConstants.ROOT_PATH_ALIAS);

        WatchrCoreApp app = WatchrCoreApp.getInstance();
        app.getLogger().logInfo("Exporting graphs to " + graphExportDestination.getAbsolutePath() + "...");

        app.exportAllGraphHtml(databaseName, exportDisplayConfig, graphExportDestination);
    }

    /////////////////
    // INNER CLASS //
    /////////////////

    @Extension
    public static class DescriptorImpl extends BuildStepDescriptor<Publisher> {

        public DescriptorImpl() {
            load();
        }

        @Override
        public String getDisplayName() {
            return "Publish Watchr Performance Reports";
        }

        /**
         * Performs on-the-fly validation of the form field 'watchrConfigJson'.
         *
         * @param value This parameter receives the value that the user has typed.
         * @return Indicates the outcome of the validation. This is sent to the browser.
         */
        public FormValidation doCheckWatchrConfigJson(@QueryParameter String value) {
            return FormValidation.ok();
        }

        /**
         * Performs on-the-fly validation of the form field 'performanceReportsLocation'.
         *
         * @param value This parameter receives the value that the user has typed.
         * @return Indicates the outcome of the validation. This is sent to the browser.
         */
        public FormValidation doCheckPerformanceReportsLocation(@QueryParameter String value) {
            return FormValidation.ok();
        }

        @Override
        @SuppressWarnings("rawtypes")
        public boolean isApplicable(Class<? extends AbstractProject> aClass) {
            // Indicates that this builder can be used with all kinds of project types
            return true;
        }

        @Override
        public boolean configure(StaplerRequest req, net.sf.json.JSONObject formData) throws Descriptor.FormException {
            save();
            return super.configure(req,formData);
        }
    }
}