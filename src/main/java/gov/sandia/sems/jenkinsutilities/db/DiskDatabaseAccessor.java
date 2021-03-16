/*******************************************************************************
* Watchr
* ------
* Copyright 2021 National Technology & Engineering Solutions of Sandia, LLC (NTESS).
* Under the terms of Contract DE-NA0003525 with NTESS, the U.S. Government retains
* certain rights in this software.
******************************************************************************/
package gov.sandia.sems.jenkinsutilities.db;

import java.io.File;

import gov.sandia.sems.jenkins.semsjppplugin.PerformanceResultAction;
import gov.sandia.sems.jenkinsutilities.treeaccessor.DiskFilterAccessor;
import gov.sandia.sems.jenkinsutilities.treeaccessor.DiskTreeAccessor;
import gov.sandia.sems.jenkinsutilities.treeaccessor.IFilterAccessor;
import gov.sandia.sems.jenkinsutilities.treeaccessor.ITreeAccessor;
import gov.sandia.sems.jenkinsutilities.views.DiskViewAccessor;
import gov.sandia.sems.jenkinsutilities.views.IViewAccessor;
import hudson.model.Job;
import hudson.model.Run;

/**
 * The disk-based implementation of {@link IDatabaseAccessor}.  Rather than
 * using a traditional database format such as Mongo or HDF5, this implementation
 * relies on folders and files stored directly on the hard drive to mimic a
 * hierarchical database.
 * 
 * @author Elliott Ridgway
 */
public class DiskDatabaseAccessor implements IDatabaseAccessor {

    ////////////
    // FIELDS //
    ////////////

    private static final String DISK_TREE_FOLDER_NAME = "performance_history_tree";
    private static final String DISK_FILTER_FOLDER_NAME = "performance_history_filters";
    private static final String DISK_VIEW_FOLDER_NAME = "performance_views";

    private DiskTreeAccessor diskTreeAccessor = null;
    private DiskViewAccessor diskViewAccessor = null;
    private DiskFilterAccessor diskFilterAccessor = null;

    private final Job<?,?> job;
    private final File logFile;

    /////////////////
    // CONSTRUCTOR //
    /////////////////

    /**
     * The constructor.
     * 
     * @param job A Jenkins Job object must be passed for the
     * DiskDatabaseAccessor to orient itself on the system
     * and begin writing data to the correct location.
     */
    public DiskDatabaseAccessor(Job<?,?> job) {
        this.job = job;
        
        Run<?,?> lastBuild = job.getLastBuild();
        logFile = lastBuild.getLogFile();
    }

    //////////////
    // OVERRIDE //
    //////////////

    @Override
    public IDatabasePart getDatabasePart(IDatabasePartType type) {
        if(type == IDatabasePartType.TREE) {
            return getTreeAccessor(false);
        } else if(type == IDatabasePartType.VIEWS) {
            return getViewAccessor(false);
        } else if(type == IDatabasePartType.FILTERS) {
            return getFilterAccessor(false);
        }
        return null;
    }

    @Override
    public IDatabasePart getAndOpenDatabasePart(IDatabasePartType type) {
        if(type == IDatabasePartType.TREE) {
            return getTreeAccessor(true);
        } else if(type == IDatabasePartType.VIEWS) {
            return getViewAccessor(true);
        } else if(type == IDatabasePartType.FILTERS) {
            return getFilterAccessor(true);
        }
        return null;
    }

    @Override
    public void setDatabasePart(IDatabasePartType type, IDatabasePart part) {
        if(type == IDatabasePartType.TREE && part instanceof DiskTreeAccessor) {
            diskTreeAccessor = (DiskTreeAccessor) part;
        } else if(type == IDatabasePartType.VIEWS && part instanceof DiskViewAccessor) {
            diskViewAccessor = (DiskViewAccessor) part;
        } else if(type == IDatabasePartType.FILTERS && part instanceof DiskFilterAccessor) {
            diskFilterAccessor = (DiskFilterAccessor) part;
        }
    }

    /////////////
    // PRIVATE //
    /////////////

    private ITreeAccessor getTreeAccessor(boolean open) {
        if(job != null) {
            Run<?,?> lastBuild = job.getLastBuild();
            PerformanceResultAction action = lastBuild.getAction(PerformanceResultAction.class);

            File rootDir = lastBuild.getParent().getRootDir();
            String treeAccessorPath = new File(rootDir, DISK_TREE_FOLDER_NAME).toPath().toString();
            File treeAccessorDir = new File(treeAccessorPath);
            
            boolean treeDirExists = treeAccessorDir.exists() && treeAccessorDir.isDirectory();
            if(!treeDirExists) {
                treeAccessorDir.mkdirs();
            }

            if(diskTreeAccessor == null) {
                diskTreeAccessor = new DiskTreeAccessor(this, treeAccessorPath, action, logFile);
            }
            if(open && !diskTreeAccessor.isOpen()) {
                diskTreeAccessor.open();
            }
            return diskTreeAccessor;
        }
        return null;
    }

    private IFilterAccessor getFilterAccessor(boolean open) {
        if(job != null) {
            Run<?,?> lastBuild = job.getLastBuild();

            File rootDir = lastBuild.getParent().getRootDir();
            String filterAccessorPath = new File(rootDir, DISK_FILTER_FOLDER_NAME).toPath().toString();
            File filterAccessorDir = new File(filterAccessorPath);
            
            boolean treeDirExists = filterAccessorDir.exists() && filterAccessorDir.isDirectory();
            if(!treeDirExists) {
                filterAccessorDir.mkdirs();
            }

            if(diskFilterAccessor == null) {
                diskFilterAccessor = new DiskFilterAccessor(this, filterAccessorPath, logFile);
            }
            if(open && !diskFilterAccessor.isOpen()) {
                diskFilterAccessor.open();
            }
            return diskFilterAccessor;
        }
        return null;
    }

    private IViewAccessor getViewAccessor(boolean open) {
        if(job != null) {
            Run<?,?> lastBuild = job.getLastBuild();

            File rootDir = lastBuild.getParent().getRootDir();
            String viewAccessorPath = new File(rootDir, DISK_VIEW_FOLDER_NAME).toPath().toString();
            File viewAccessorDir = new File(viewAccessorPath);
            
            boolean treeDirExists = viewAccessorDir.exists() && viewAccessorDir.isDirectory();
            if(!treeDirExists) {
                viewAccessorDir.mkdirs();
            }

            if(diskViewAccessor == null) {
                diskViewAccessor = new DiskViewAccessor(this, viewAccessorPath, logFile);
            }
            if(open && !diskViewAccessor.isOpen()) {
                diskViewAccessor.open();
            }
            return diskViewAccessor;
        }
        return null;
    }
}