/*******************************************************************************
* Watchr
* ------
* Copyright 2021 National Technology & Engineering Solutions of Sandia, LLC (NTESS).
* Under the terms of Contract DE-NA0003525 with NTESS, the U.S. Government retains
* certain rights in this software.
******************************************************************************/
package gov.sandia.watchr.buildsteps;

import java.io.IOException;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;

import gov.sandia.watchr.WatchrCoreApp;
import gov.sandia.watchr.WatchrJenkinsApp;
import gov.sandia.watchr.actions.PerformanceResultAction;
import gov.sandia.watchr.config.GraphDisplayConfig;
import gov.sandia.watchr.config.GraphDisplayConfig.LeafNodeStrategy;
import gov.sandia.watchr.config.file.IFileReader;
import gov.sandia.watchr.impl.WatchrJenkinsFileReader;
import gov.sandia.watchr.impl.WatchrJenkinsLogger;
import gov.sandia.watchr.log.ILogger;
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
    public final boolean exportGraphs;
    
    public final String watchrConfigFilepath;
    public final String performanceReportsLocation;

    /////////////////
    // CONSTRUCTOR //
    /////////////////

    // Fields in config.jelly must match the parameter names in the "DataBoundConstructor"
    @DataBoundConstructor
    public PerformanceRecorder(
            String watchrConfigJson, String watchrConfigFilepath,
            String performanceReportsLocation, boolean exportGraphs) {

        this.watchrConfigJson = watchrConfigJson;
        this.exportGraphs = exportGraphs;
        
        this.performanceReportsLocation = removeLeadingSlash(performanceReportsLocation);
        this.watchrConfigFilepath = removeLeadingSlash(watchrConfigFilepath);
    }

    //////////////
    // OVERRIDE //
    //////////////

    @Override
    public void perform(
            Run<?, ?> build, FilePath workspace, Launcher launcher, TaskListener listener) throws InterruptedException {

        PerformanceResultAction action = build.getAction(PerformanceResultAction.class);
        if(action == null) {
            build.addAction(new PerformanceResultAction(build));
        } else {
            try {
                build.save();
            } catch (IOException e1) {
                ILogger logger = new WatchrJenkinsLogger(WatchrJenkinsApp.getLogForBuild(build));
                logger.logError("An error occurred saving the build: ", e1);
            } 
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
        WatchrCoreApp coreApp = WatchrJenkinsApp.getAppForJob(build.getParent());
        ILogger logger = new WatchrJenkinsLogger(WatchrJenkinsApp.getLogForBuild(build));
        coreApp.setLogger(logger);

        IFileReader fileReader = new WatchrJenkinsFileReader(workspace, logger);
        coreApp.setFileReader(fileReader);

        JenkinsConfigContext configContext =
            WatchrJenkinsApp.getConfigContextOrDefault(build.getParent());
        String dbName = configContext.getDatabaseName();

        FilePath perfResultsFilePath = workspace.child(performanceReportsLocation);
        
        try {
            String filePathString = perfResultsFilePath.toURI().getPath();
            List<String> childFiles = fileReader.getFolderContents(filePathString);
            
            String configFileContents = "";
            if(StringUtils.isNotBlank(watchrConfigJson)) {
                configFileContents = watchrConfigJson;
            } else if(StringUtils.isNotBlank(watchrConfigFilepath)) {
                configFileContents = fileReader.readFromFile(watchrConfigFilepath);
            }

            boolean anyReportsExist = !childFiles.isEmpty();
            if(anyReportsExist && StringUtils.isNotBlank(configFileContents)) {
                logger.logInfo("Requesting new plots from " + filePathString + " for db " + dbName + " (watchr-jenkins)");
                coreApp.addToDatabase(dbName, filePathString, configFileContents);
                logger.logInfo("Saving database... (watchr-jenkins)");
                coreApp.saveDatabase(dbName);

                if(exportGraphs) {
                    doExportGraphs(build, workspace, dbName);
                }
            } else if(!anyReportsExist) {
                logger.logError("No performance reports were located at path " + filePathString);
            } else if(StringUtils.isBlank(configFileContents)) {
                logger.logError("No Watchr configuration specified!");
            }
        } catch (IOException e1) {
            logger.logError("An error occurred reading the file path: ", e1);
        } catch(WatchrParseException e2) {
            logger.logError("An error occurred extracting new plot data: ", e2.getOriginalException());
        } catch (InterruptedException e3) {
            logger.logError("An interruption exception occurred: ", e3);
            throw e3;
        } catch (Exception e4) {
            logger.logError("A generic exception occurred: ", e4);
        }
    }

    private void doExportGraphs(Run<?, ?> build, FilePath workspace, String databaseName) throws InterruptedException {
        WatchrCoreApp coreApp = WatchrJenkinsApp.getAppForJob(build.getParent());
        ILogger logger = new WatchrJenkinsLogger(WatchrJenkinsApp.getLogForBuild(build));
        try {
            long timestamp = System.currentTimeMillis();
            String graphExportDestinationName = "watchrGraphExport_" + timestamp;
            FilePath graphExportDestinationFilePath = workspace.child(graphExportDestinationName);
            graphExportDestinationFilePath.mkdirs();
            
            String graphExportDestinationPath = graphExportDestinationFilePath.toURI().getPath();

            JenkinsConfigContext configContext =
                WatchrJenkinsApp.getConfigContextOrDefault(build.getParent());
            GraphDisplayConfig exportDisplayConfig = new GraphDisplayConfig(configContext.getGraphDisplayConfig());
            exportDisplayConfig.setLastPlotDbLocation(CommonConstants.ROOT_PATH_ALIAS);
            exportDisplayConfig.setNextPlotDbLocation(CommonConstants.ROOT_PATH_ALIAS);
            exportDisplayConfig.setLeafNodeStrategy(LeafNodeStrategy.SHOW_NOTHING);

            coreApp.getLogger().logInfo("Exporting graphs to " + graphExportDestinationPath + "...");
            coreApp.exportAllGraphHtml(databaseName, exportDisplayConfig, graphExportDestinationPath);
        } catch (IOException e1) {
            logger.logError("An error occurred using the graph export directory: ", e1);
        } catch (InterruptedException e3) {
            logger.logError("An interruption exception occurred: ", e3);
            throw e3;
        } catch (Exception e4) {
            logger.logError("A generic exception occurred: ", e4);
        }
    }

    private String removeLeadingSlash(String original) {
        // Filepaths must always be relative to the workspace of the Jenkins job,
        // so leading slashes should be removed.
        if(original.startsWith("/")) {
            return original.substring(1, original.length());
        }
        return original;
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