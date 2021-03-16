/*******************************************************************************
* Watchr
* ------
* Copyright 2021 National Technology & Engineering Solutions of Sandia, LLC (NTESS).
* Under the terms of Contract DE-NA0003525 with NTESS, the U.S. Government retains
* certain rights in this software.
******************************************************************************/
package gov.sandia.sems.jenkinsutilities.utilities;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Global logging utility.
 * 
 * @author Elliott Ridgway
 */
public class LogUtil {
    
    private static final SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy-HH:mm:ss.SSS");
    
    public static void printLoggerMessage(PrintStream logger, String message) {
        if(logger != null) {
            logger.println(message);
        }
    }
    
    public static void printErrorMessage(PrintStream logger, Throwable t) {
        if(logger != null) {
            logger.println("*********ERROR:" + t.getMessage());
            
            StringWriter sw = new StringWriter();
            t.printStackTrace(new PrintWriter(sw));
            String exceptionAsString = sw.toString();
            logger.println(exceptionAsString);
        }
    }    
    
    public static void writeToLog(File f, String message) {
        if(f != null && f.exists()) {
            try(FileWriter fw     = new FileWriter(f, true);
                BufferedWriter bw = new BufferedWriter(fw);
                PrintWriter pw    = new PrintWriter(bw)) {

                pw.write("[" + sdf.format(new Date(System.currentTimeMillis())) + "] " + message);
                pw.write("\r\n");
            } catch(IOException e) {
                // Do nothing
            }
        }
    }
    
    public static void writeErrorToLog(File f, Throwable t) {        
        if(f != null && f.exists()) {
            try(FileWriter fw     = new FileWriter(f, true);
                BufferedWriter bw = new BufferedWriter(fw);
                PrintWriter pw    = new PrintWriter(bw)) {

                StringWriter sw = new StringWriter();
                t.printStackTrace(pw);
                String exceptionAsString = sw.toString();
                
                fw.write("[" + sdf.format(new Date(System.currentTimeMillis())) + "]");
                fw.write(exceptionAsString);
                fw.write("\r\n");
            } catch(IOException e) {
                // Do nothing
            }
        }
    }
}
