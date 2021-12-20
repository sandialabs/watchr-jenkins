/*******************************************************************************
* Watchr
* ------
* Copyright 2021 National Technology & Engineering Solutions of Sandia, LLC (NTESS).
* Under the terms of Contract DE-NA0003525 with NTESS, the U.S. Government retains
* certain rights in this software.
******************************************************************************/
package gov.sandia.watchr.impl;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import gov.sandia.watchr.config.file.IFileReader;
import gov.sandia.watchr.log.ILogger;
import gov.sandia.watchr.util.FileUtil;
import hudson.FilePath;

/**
 * Implementation of {@link IFileReader} that uses Jenkins's {@link FilePath}
 * object to perform file operations. There are two details to be aware of:<br>
 * <br>
 * 1) Files may not exist on the same filesystem as Jenkins, so we can never
 * rely on Java File objects at any point.<br>
 * 2) All file paths given to this class must be relative to the root FilePath
 * provided at construction time.
 * 
 * @author Elliott Ridgway
 */
public class WatchrJenkinsFileReader implements IFileReader {

    ////////////
    // FIELDS //
    ////////////

    private final FilePath root;
    private final ILogger logger;

    /////////////////
    // CONSTRUCTOR //
    /////////////////

    public WatchrJenkinsFileReader(FilePath root, ILogger logger) {
        this.root = root;
        this.logger = logger;
    }

    //////////////
    // OVERRIDE //
    //////////////

    @Override
    public String readFromFile(String filePath) {
        try {
            FilePath childFile = root.child(filePath);
            return childFile.readToString();
        } catch(IOException e) {
            logger.logError("An error occurred reading the file at " + filePath, e);
        } catch(InterruptedException e) {
            logger.logError("An error occurred reading the file at " + filePath, e);
            Thread.currentThread().interrupt();
        }
        return "";
    }

    @Override
    public void writeToFile(String filePath, String fileContents) {
        try {
            FilePath childFile = root.child(filePath);
            childFile.write(fileContents, StandardCharsets.UTF_8.name());
        } catch(IOException e) {
            logger.logError("An error occurred reading the file at " + filePath, e);
        } catch(InterruptedException e) {
            logger.logError("An error occurred reading the file at " + filePath, e);
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public List<String> getFolderContents(String filePath) {
        List<String> childFilePaths = new ArrayList<>();
        try {
            String anchorPath = root.toURI().getPath();

            FilePath parentFile = root.child(filePath);
            for(FilePath childFile : parentFile.list()) {
                String childPath = childFile.toURI().getPath();
                String relativePath = FileUtil.createRelativeFilePath(anchorPath, childPath);
                childFilePaths.add(relativePath);
            }
        } catch(IOException e) {
            logger.logError("An error occurred reading the file at " + filePath, e);
        } catch(InterruptedException e) {
            logger.logError("An error occurred reading the file at " + filePath, e);
            Thread.currentThread().interrupt();
        }

        return childFilePaths;
    }

    @Override
    public String getName(String filePath) {
        FilePath childFile = root.child(filePath);
        return childFile.getName();
    }

    @Override
    public boolean exists(String filePath) {
        try {
            FilePath childFile = root.child(filePath);
            return childFile.exists();
        } catch(IOException e) {
            logger.logError("An error occurred reading the file at " + filePath, e);
        } catch(InterruptedException e) {
            logger.logError("An error occurred reading the file at " + filePath, e);
            Thread.currentThread().interrupt();
        }
        return false;
    }

    @Override
    public boolean isDirectory(String filePath) {
        FilePath childFile = root.child(filePath);
        try {
            return childFile.isDirectory();
        } catch(IOException e) {
            logger.logError("An error occurred reading the file at " + filePath, e);
        } catch(InterruptedException e) {
            logger.logError("An error occurred reading the file at " + filePath, e);
            Thread.currentThread().interrupt();
        }
        return false;
    }

    @Override
    public boolean isFile(String filePath) {
        FilePath childFile = root.child(filePath);
        try {
            return !childFile.isDirectory();
        } catch(IOException e) {
            logger.logError("An error occurred reading the file at " + filePath, e);
        } catch(InterruptedException e) {
            logger.logError("An error occurred reading the file at " + filePath, e);
            Thread.currentThread().interrupt();
        }
        return false;
    }    
}
