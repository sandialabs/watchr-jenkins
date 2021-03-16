/*******************************************************************************
* Watchr
* ------
* Copyright 2021 National Technology & Engineering Solutions of Sandia, LLC (NTESS).
* Under the terms of Contract DE-NA0003525 with NTESS, the U.S. Government retains
* certain rights in this software.
******************************************************************************/
package gov.sandia.sems.jenkins.semsjppplugin;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;
import org.xml.sax.SAXException;

import gov.sandia.sems.jenkinsutilities.db.DiskDatabaseAccessor;
import gov.sandia.sems.jenkinsutilities.db.IDatabaseAccessor;
import gov.sandia.sems.jenkinsutilities.db.IDatabasePartType;
import gov.sandia.sems.jenkinsutilities.treeaccessor.ITreeAccessor;
import gov.sandia.sems.jenkinsutilities.utilities.LogUtil;
import gov.sandia.sems.jenkinsutilities.utilities.Md5Util;
import gov.sandia.sems.jenkinsutilities.utilities.NumUtil;
import gov.sandia.sems.jenkinsutilities.xml.PerformanceReport;
import hudson.AbortException;
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
import hudson.model.Descriptor;
import hudson.util.FormValidation;
import jenkins.tasks.SimpleBuildStep;

import org.apache.commons.lang.StringUtils;

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

    public final String performanceReportsLocation; // The location in the Jenkins job workspace of the performance
                                                    // reports to read.
    public final String performanceReportFormat;
    public final String defaultMeasurableUnit;
    public final String userDefinedMeasurables;
    public final int rollingRange;
    public final boolean avgFailIfGreater;
    public final boolean stdDevFailIfGreater;
    public final boolean recalculateAllDerivedLines;
    public final boolean useHiddenValuesForDerivedLines;

    /////////////////
    // CONSTRUCTOR //
    /////////////////

    // Fields in config.jelly must match the parameter names in the
    // "DataBoundConstructor"
    @DataBoundConstructor
    public PerformanceRecorder(String performanceReportsLocation, String performanceReportFormat,
            String defaultMeasurableUnit, String userDefinedMeasurables, int rollingRange, boolean avgFailIfGreater,
            boolean stdDevFailIfGreater, boolean recalculateAllDerivedLines, boolean useHiddenValuesForDerivedLines) {
        this.performanceReportsLocation = performanceReportsLocation;
        this.defaultMeasurableUnit = defaultMeasurableUnit;
        this.userDefinedMeasurables = userDefinedMeasurables;
        this.rollingRange = rollingRange;
        this.avgFailIfGreater = avgFailIfGreater;
        this.stdDevFailIfGreater = stdDevFailIfGreater;
        this.recalculateAllDerivedLines = recalculateAllDerivedLines;
        this.useHiddenValuesForDerivedLines = useHiddenValuesForDerivedLines;

        PerformanceReportFormat performanceReportFormatEnum = PerformanceReportFormat.XML; // XML is the default.
        if (StringUtils.isNotBlank(performanceReportFormat)) {
            performanceReportFormatEnum = PerformanceReportFormat.getEnumFromLabel(performanceReportFormat);
        }
        if (performanceReportFormatEnum == null) {
            try {
                performanceReportFormatEnum = PerformanceReportFormat.valueOf(performanceReportFormat);
            } catch (IllegalArgumentException e) {
                // Do nothing.
            }
        }

        this.performanceReportFormat = (performanceReportFormatEnum != null) ? performanceReportFormatEnum.toString()
                : "";
    }

    //////////////
    // OVERRIDE //
    //////////////

    @Override
    public void perform(Run<?, ?> build, FilePath workspace, Launcher launcher, TaskListener listener)
            throws InterruptedException, IOException {

        printBuildStats(build, listener);

        // Some initialization code.
        List<PerformanceReport> reports = new ArrayList<>();
        PerformanceResultAction action = build.getAction(PerformanceResultAction.class);
        if (action == null) {
            action = new PerformanceResultAction(build, reports, userDefinedMeasurables.split("\\s+"),
                    defaultMeasurableUnit, rollingRange, avgFailIfGreater, stdDevFailIfGreater,
                    recalculateAllDerivedLines, useHiddenValuesForDerivedLines);
            build.addAction(action);
        } else {
            build.save();
        }

        // Currently, Watchr only supports file/folder-based database implementations.
        IDatabaseAccessor db = new DiskDatabaseAccessor(build.getParent());
        ITreeAccessor tree = (ITreeAccessor) db.getAndOpenDatabasePart(IDatabasePartType.TREE);

        // Update the build-to-hashes map.
        Map<Integer, Map<String, List<String>>> hashesMap = tree.getBuildToHashesMap();
        List<String> hashes = new ArrayList<>();
        if (hashesMap != null) {
            if (!hashesMap.containsKey(build.getNumber())) {
                hashesMap.put(build.getNumber(), new HashMap<>());
            }
            for (Integer buildId : hashesMap.keySet()) {
                hashes.addAll(hashesMap.get(buildId).keySet());
            }
        } else {
            throw new AbortException("Hash map cannot be null.  Aborting...");
        }

        // Gather performance report files.
        FilePath perfResultsFilePath = workspace.child(performanceReportsLocation);
        reports = parseFiles(perfResultsFilePath, performanceReportFormat, build, hashesMap, hashes, listener);

        // Check for error state.
        if (reports.isEmpty()) {
            LogUtil.printLoggerMessage(listener.getLogger(), "No performance reports were located.");
        } else {
            listener.getLogger().println("Watchr:  Number of New Performance Reports Parsed: " + reports.size());

            // Finally, add all reports to the tree.
            for (PerformanceReport report : reports) {
                if (StringUtils.isNotBlank(report.getName())) {
                    listener.getLogger().println("Adding report " + report.getName());
                }
                tree.addReport(report);
            }
        }
        tree.close();
    }

    @Override
    public BuildStepMonitor getRequiredMonitorService() {
        return BuildStepMonitor.NONE;
    }

    /////////////
    // PRIVATE //
    /////////////

    private void printBuildStats(Run<?, ?> build, TaskListener listener) {
        StringBuilder sb = new StringBuilder();
        sb.append("Watchr ").append(CommonConstants.PROJECT_VERSION);
        sb.append(" Configuration for Build #").append(build.getNumber());
        LogUtil.printLoggerMessage(listener.getLogger(), sb.toString());

        sb = new StringBuilder();
        sb.append(" * Performance Reports Location: ").append(performanceReportsLocation);
        LogUtil.printLoggerMessage(listener.getLogger(), sb.toString());

        sb = new StringBuilder();
        sb.append(" * Performance Report Format: ").append(performanceReportFormat);
        LogUtil.printLoggerMessage(listener.getLogger(), sb.toString());

        sb = new StringBuilder();
        sb.append(" * Default Measurable Unit: ").append(defaultMeasurableUnit);
        LogUtil.printLoggerMessage(listener.getLogger(), sb.toString());

        sb = new StringBuilder();
        sb.append(" * User Defined Measurables: ").append(userDefinedMeasurables);
        LogUtil.printLoggerMessage(listener.getLogger(), sb.toString());

        sb = new StringBuilder();
        sb.append(" * Rolling Range: ").append(Integer.toString(rollingRange));
        LogUtil.printLoggerMessage(listener.getLogger(), sb.toString());

        sb = new StringBuilder();
        sb.append(" * Recalculate All Derived Lines: ").append(Boolean.toString(recalculateAllDerivedLines));
        LogUtil.printLoggerMessage(listener.getLogger(), sb.toString());

        sb = new StringBuilder();
        sb.append(" * Use Filtered Values: ").append(Boolean.toString(useHiddenValuesForDerivedLines));
        LogUtil.printLoggerMessage(listener.getLogger(), sb.toString());

        sb = new StringBuilder();
        sb.append(" * Fail if data is greater than average: ").append(Boolean.toString(avgFailIfGreater));
        LogUtil.printLoggerMessage(listener.getLogger(), sb.toString());

        sb = new StringBuilder();
        sb.append(" * Fail if data is greater than standard deviation: ").append(Boolean.toString(stdDevFailIfGreater));
        LogUtil.printLoggerMessage(listener.getLogger(), sb.toString());
    }

    private List<PerformanceReport> parseFiles(FilePath startFilePath, String performanceReportFormat, Run<?, ?> build,
            Map<Integer, Map<String, List<String>>> hashesMap, List<String> hashes, TaskListener listener)
            throws InterruptedException, IOException {

        List<FilePath> childFilePaths = startFilePath.list();
        List<PerformanceReport> parsedReports = new ArrayList<>();

        FilePath childFilePath = null;
        for (int i = 0; i < childFilePaths.size(); i++) {
            childFilePath = childFilePaths.get(i);
            if (childFilePath.isDirectory()) {
                parsedReports
                        .addAll(parseFiles(childFilePath, performanceReportFormat, build, hashesMap, hashes, listener));
            } else {
                String fileContents = childFilePath.readToString();

                // Check if this file's hash is already in the tree. If not, add it.
                String hash = Md5Util.getMd5(fileContents.getBytes());
                if (!hashes.contains(hash)) {
                    Map<String, List<String>> hashInnerMap = hashesMap.get(build.getNumber());

                    List<String> nameAndDate = new ArrayList<>();
                    nameAndDate.add(childFilePath.getName());
                    String timeStamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date());
                    nameAndDate.add(timeStamp);
                    hashInnerMap.put(hash, nameAndDate);

                    hashesMap.put(build.getNumber(), hashInnerMap);
                } else {
                    Integer foundBuildId = null;
                    for (Integer buildId : hashesMap.keySet()) {
                        if (hashesMap.get(buildId).containsKey(hash)) {
                            foundBuildId = buildId;
                            break;
                        }
                    }
                    if (foundBuildId != null) {
                        List<String> nameAndDate = hashesMap.get(foundBuildId).get(hash);

                        StringBuilder sb = new StringBuilder();
                        sb.append("Skipping " + childFilePath.getName());
                        LogUtil.printLoggerMessage(listener.getLogger(), sb.toString());

                        sb = new StringBuilder();
                        sb.append("(An identical file called ");
                        sb.append("\"").append(nameAndDate.get(0)).append("\"");
                        sb.append(" was seen on ").append(nameAndDate.get(1));
                        sb.append(" during build #").append(foundBuildId).append(")");
                        LogUtil.printLoggerMessage(listener.getLogger(), sb.toString());

                        continue;
                    }
                }

                // Parse the performance report file.
                PerformanceReportFormatConverter converter = new PerformanceReportFormatConverter();
                boolean canParseFile = converter.canParseFile(childFilePath.getName(), performanceReportFormat);
                if (childFilePath.exists() && canParseFile) {
                    try {
                        parsedReports.addAll(converter.parsePerformanceReport(childFilePath, performanceReportFormat));
                    } catch (SAXException | ParserConfigurationException e) {
                        LogUtil.printErrorMessage(listener.getLogger(), e);
                    }
                }
            }
        }
        return parsedReports;
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
            return "Publish Performance Reports";
        }

        /**
         * Performs on-the-fly validation of the form field 'rollingRange'.
         *
         * @param value This parameter receives the value that the user has typed.
         * @return Indicates the outcome of the validation. This is sent to the browser.
         */
        public FormValidation doCheckRollingRange(@QueryParameter String value) {
            boolean proceed = true;
            proceed = proceed && NumUtil.isInt(value);
            proceed = proceed && (Integer.parseInt(value)) > 0;

            if(proceed) return FormValidation.ok();
            else        return FormValidation.error("Rolling range must be a positive integer value");
        }
                
        /**
         * Performs on-the-fly validation of the form field 'userDefinedMeasurables'.
         *
         * @param value This parameter receives the value that the user has typed.
         * @return Indicates the outcome of the validation. This is sent to the browser.
         */
        public FormValidation doCheckUserDefinedMeasurables(@QueryParameter String value) {
            boolean proceed = true;
            proceed = proceed && !StringUtils.isBlank(value);

            if(proceed) return FormValidation.ok();
            else        return FormValidation.error("Field should contain at least one measurable.");
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