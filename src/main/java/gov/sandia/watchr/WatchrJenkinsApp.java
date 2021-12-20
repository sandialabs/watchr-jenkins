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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import gov.sandia.watchr.db.impl.FileBasedDatabase;
import gov.sandia.watchr.graph.library.IHtmlButtonRenderer;
import gov.sandia.watchr.graph.library.IHtmlGraphRenderer;
import gov.sandia.watchr.graph.library.impl.PlotlyGraphRenderer;
import gov.sandia.watchr.graph.options.ButtonType;
import gov.sandia.watchr.graph.options.JenkinsButtonBar;
import gov.sandia.watchr.impl.WatchrJenkinsLogger;
import gov.sandia.watchr.model.JenkinsConfigContext;
import gov.sandia.watchr.log.ILogger;
import hudson.model.Job;
import hudson.model.Run;

public class WatchrJenkinsApp {

    ////////////
    // FIELDS //
    ////////////

    private static final Set<JenkinsConfigContext> configContexts;
    private static final Map<Job<?,?>, WatchrCoreApp> coreAppCache;

    //////////
    // INIT //
    //////////

    static {
        configContexts = new HashSet<>();
        coreAppCache = new HashMap<>();
    }

    /////////////////
    // CONSTRUCTOR //
    /////////////////

    private WatchrJenkinsApp() {}

    //////////////
    // DATABASE //
    //////////////

    public static void loadDatabase(Job<?,?> job) {
        String databaseName = job.getName();
        File dbRootDir = job.getRootDir();
        File dbDir = new File(dbRootDir, "db");

        ILogger logger = useAndGetLoggerForJob(job);
        logger.logInfo("Loading database " + databaseName + "... (watchr-jenkins)");
        
        WatchrCoreApp app = getAppForJob(job);
        app.connectDatabase(databaseName, FileBasedDatabase.class, new Object[]{ dbDir });
        setGraphRenderer(job);
    }

    ////////////////////
    // CONFIG CONTEXT //
    ////////////////////

    public static JenkinsConfigContext getConfigContextOrDefault(Job<?,?> job) {
        ILogger logger = useAndGetLoggerForJob(job);
        logger.logInfo("Checking context map for job " + job.getName() + "... (watchr-jenkins)");

        JenkinsConfigContext context = null;
        for(JenkinsConfigContext thisContext : configContexts) {
            if(thisContext.getJob().equals(job)) {
                context = thisContext;
                break;
            }
        }
        if(context == null) {
            context = new JenkinsConfigContext(job);
        }
        return context;
    }

    public static void addConfigContext(JenkinsConfigContext context) {
        configContexts.add(context);
    }

    ////////////////////
    // CORE APP CACHE //
    ////////////////////

    public static WatchrCoreApp getAppForJob(Job<?,?> job) {
        return coreAppCache.computeIfAbsent(job, k -> new WatchrCoreApp());
    }

    //////////////////////
    // GRAPHING LIBRARY //
    //////////////////////

    private static void setGraphRenderer(Job<?,?> job) {
        WatchrCoreApp app = getAppForJob(job);
        
        String databaseName = job.getName();
        IHtmlGraphRenderer graphRenderer = app.getGraphRenderer(PlotlyGraphRenderer.class, databaseName);
        IHtmlButtonRenderer buttonRenderer = graphRenderer.getButtonRenderer();

        if(buttonRenderer.getButtons().isEmpty()) {
            List<ButtonType> availableButtons = new ArrayList<>();
            availableButtons.add(ButtonType.GO_TO_CHILD_GRAPH);
            availableButtons.add(ButtonType.DELETE);
            
            buttonRenderer.setButtonBar(new JenkinsButtonBar(buttonRenderer, app.getLogger()));
            buttonRenderer.getButtons().addAll(availableButtons);
        }
    }

    /////////////
    // LOGGING //
    /////////////

    public static File getLogForLastBuild(Job<?,?> job) {
        try {
            Run<?,?> lastBuild = job.getLastBuild();
            if(lastBuild != null) {
                File buildDir = lastBuild.getRootDir();

                File logFile = new File(buildDir, "log");
                if(!logFile.exists()) {
                    boolean success = logFile.createNewFile();
                    if(!success) {
                        return null;
                    }
                }
                return logFile;
            }
        } catch(IOException e) {
            System.err.println("Could not get handle on log file.");
            e.printStackTrace();
            return null;
        }
        return null;
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
        WatchrCoreApp app = getAppForJob(job);
        File logFile = WatchrJenkinsApp.getLogForLastBuild(job);
        if(logFile != null) {
            ILogger jenkinsLogger = new WatchrJenkinsLogger(logFile);
            app.setLogger(jenkinsLogger);
        }
        return app.getLogger();
    }
}
