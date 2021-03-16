/*******************************************************************************
* Watchr
* ------
* Copyright 2021 National Technology & Engineering Solutions of Sandia, LLC (NTESS).
* Under the terms of Contract DE-NA0003525 with NTESS, the U.S. Government retains
* certain rights in this software.
******************************************************************************/
package gov.sandia.sems.jenkinsutilities;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

import org.apache.commons.io.FileUtils;
import org.junit.Assert;

public class TestFileUtils {

    private static final String LOG_FILE = "logFile.txt";

    /**
     * Utility method for reading a test file that is local to the src/test/resources
     * directory to a String object.
     * 
     * @param governingClass Used to orient a {@link ClassLoader} for loading from the
     * resources directory.
     * @param relativePath The path to the test resource file, starting from "src/test/resources".
     * @return The contents of the file as a String.
     * @throws IOException Thrown if there was an error finding the file.
     * @throws URISyntaxException Thrown if there was a URL error in the provided file path.
     */
    public static String readTestFileToString(Class<?> governingClass, String relativePath)
            throws IOException, URISyntaxException {

        ClassLoader classLoader = governingClass.getClassLoader();
        URL expectedFileURL = classLoader.getResource(relativePath);
        if(expectedFileURL != null) {                
            File expectedFile = new File(expectedFileURL.toURI());
            if(expectedFile.exists()) {
                return FileUtils.readFileToString(expectedFile);
            } else {
                throw new FileNotFoundException("File does not exist at path " + expectedFile.getPath());
            }
        } else {
            throw new FileNotFoundException("Could not find test file.  URL: " + expectedFileURL);
        }
    }

    /**
     * Utility method for getting a test file that is local to the src/test/resources
     * directory to a String object.
     * 
     * @param governingClass Used to orient a {@link ClassLoader} for loading from the
     * resources directory.
     * @param relativePath The path to the test resource file, starting from "src/test/resources".
     * @return The contents of the file as a String.
     * @throws IOException Thrown if there was an error finding the file.
     * @throws URISyntaxException Thrown if there was a URL error in the provided file path.
     */
    public static File getTestFile(Class<?> governingClass, String relativePath)
            throws IOException, URISyntaxException {

        ClassLoader classLoader = governingClass.getClassLoader();
        URL expectedFileURL = classLoader.getResource(relativePath);
        if(expectedFileURL != null) {                
            File expectedFile = new File(expectedFileURL.toURI());
            if(expectedFile.exists()) {
                return expectedFile;
            } else {
                throw new FileNotFoundException("File does not exist at path " + expectedFile.getPath());
            }
        } else {
            throw new FileNotFoundException("Could not find test file.  URL: " + expectedFileURL);
        }
    }

    public static File initializeTestLogFile(Class<?> governingClass) {
        ClassLoader classLoader = governingClass.getClassLoader();
        try {
            URL logFileURL = classLoader.getResource(LOG_FILE);
            File logFile = new File(logFileURL.toURI());
            if(!logFile.exists()) {
                logFile.createNewFile();
            }
            return logFile;
        } catch(IOException | URISyntaxException e) {
            Assert.fail(e.getMessage());
        }
        return null;
    }
}