/*******************************************************************************
* Watchr
* ------
* Copyright 2021 National Technology & Engineering Solutions of Sandia, LLC (NTESS).
* Under the terms of Contract DE-NA0003525 with NTESS, the U.S. Government retains
* certain rights in this software.
******************************************************************************/
package gov.sandia.watchr;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import gov.sandia.watchr.db.impl.FileBasedDatabase;
import gov.sandia.watchr.graph.library.IHtmlGraphRenderer;
import gov.sandia.watchr.graph.library.impl.PlotlyGraphRenderer;
import gov.sandia.watchr.graph.options.ButtonType;
import gov.sandia.watchr.graph.options.JenkinsButtonBar;
import gov.sandia.watchr.model.JenkinsConfigContext;
import gov.sandia.watchr.log.ILogger;
import gov.sandia.watchr.log.WatchrJenkinsLogger;
import hudson.model.Job;
import hudson.model.Run;

public class WatchrJenkinsApp {

    ////////////
    // FIELDS //
    ////////////

    private static final Map<Job<?,?>, JenkinsConfigContext> configContextMap;

    //////////
    // INIT //
    //////////

    static {
        configContextMap = new HashMap<>();
    }

    /////////////////
    // CONSTRUCTOR //
    /////////////////

    private WatchrJenkinsApp() {}

    //////////////
    // DATABASE //
    //////////////

    public static void loadDatabase(String databaseName, File dbRootDir) {
        WatchrCoreApp app = WatchrCoreApp.getInstance();
        app.connectDatabase(databaseName, FileBasedDatabase.class, new Object[]{ dbRootDir });
        setGraphRenderer(databaseName);
    }

    ////////////////////
    // CONFIG CONTEXT //
    ////////////////////

    public static JenkinsConfigContext getConfigContext(Job<?,?> job) {
        return configContextMap.get(job);
    }

    public static JenkinsConfigContext getConfigContextOrDefault(Job<?,?> job) {
        JenkinsConfigContext context = configContextMap.get(job);
        if(context == null) {
            context = new JenkinsConfigContext(job.getName(), job.getRootDir());
        }
        return context;
    }

    public static void putConfigContext(Job<?,?> job, JenkinsConfigContext context) {
        configContextMap.put(job, context);
    }

    //////////////////////
    // GRAPHING LIBRARY //
    //////////////////////

    private static void setGraphRenderer(String databaseName) {
        WatchrCoreApp app = WatchrCoreApp.getInstance();
        IHtmlGraphRenderer renderer = app.getGraphRenderer(PlotlyGraphRenderer.class, databaseName);

        if(renderer.getButtons().isEmpty()) {
            List<ButtonType> availableButtons = new ArrayList<>();
            availableButtons.add(ButtonType.GO_TO_CHILD_GRAPH);
            availableButtons.add(ButtonType.DELETE);
            
            renderer.setButtonBar(new JenkinsButtonBar(renderer));
            renderer.getButtons().addAll(availableButtons);
        }
    }

    /////////////
    // LOGGING //
    /////////////

    public static File getLogForLastBuild(Job<?,?> job) {
        try {
            Run<?,?> lastBuild = job.getLastBuild();
            File buildDir = lastBuild.getRootDir();

            File logFile = new File(buildDir, "log");
            if(!logFile.exists()) {
                boolean success = logFile.createNewFile();
                if(!success) {
                    return null;
                }
            }
            return logFile;
        } catch(IOException e) {
            System.err.println("Could not get handle on log file.");
            e.printStackTrace();
            return null;
        }
    }

    public static File getLogForBuild(Run<?,?> build) {
        try {
            File buildDir = build.getRootDir();

            File logFile = new File(buildDir, "log");
            if(!logFile.exists()) {
                boolean success = logFile.createNewFile();
                if(!success) {
                    return null;
                }
            }
            return logFile;
        } catch(IOException e) {
            System.err.println("Could not get handle on log file.");
            e.printStackTrace();
            return null;
        }
    } 
    
    public static ILogger useAndGetLoggerForJob(Job<?,?> job) {
        WatchrCoreApp.getInstance().setLogger(
            new WatchrJenkinsLogger(WatchrJenkinsApp.getLogForLastBuild(job))
        );
        return WatchrCoreApp.getInstance().getLogger();
    }
}
