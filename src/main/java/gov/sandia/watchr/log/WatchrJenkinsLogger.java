/*******************************************************************************
* Watchr
* ------
* Copyright 2021 National Technology & Engineering Solutions of Sandia, LLC (NTESS).
* Under the terms of Contract DE-NA0003525 with NTESS, the U.S. Government retains
* certain rights in this software.
******************************************************************************/
package gov.sandia.watchr.log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;

import gov.sandia.watchr.config.WatchrConfigError;
import gov.sandia.watchr.config.WatchrConfigError.ErrorLevel;
import gov.sandia.watchr.util.DateUtil;

public class WatchrJenkinsLogger implements ILogger {

    private File file;

    public WatchrJenkinsLogger(File file) {
        this.file = file;
    }

    @Override
    public void logError(String err) {
        writeToLog(err);
    }

    @Override
    public void logError(String err, Throwable t) {
        writeErrorToLog(err, t);
    }

    @Override
    public void logInfo(String info) {
        writeToLog(info);
    }

    @Override
    public void logWarning(String warning) {
        writeToLog(warning);
    }

    /////////////
    // PRIVATE //
    /////////////

    private String getLogMessagePrefix() {
        StringBuilder sb = new StringBuilder();
        sb.append("Watchr [");
        sb.append(DateUtil.epochTimeToTimestamp(System.currentTimeMillis()));
        sb.append("]: ");
        return sb.toString();
    }
 
    private void writeToLog(String message) {
        try(PrintWriter pw = new PrintWriter(new OutputStreamWriter(new FileOutputStream(file, true), StandardCharsets.UTF_8))) {
            pw.write(getLogMessagePrefix() + message + "\n");
        } catch(IOException e) {
            System.err.println("Could not find log file " + file.getAbsolutePath());
            e.printStackTrace();
        }
    }

    private void writeErrorToLog(String message, Throwable t) {
        try(PrintWriter pw = new PrintWriter(new OutputStreamWriter(new FileOutputStream(file, true), StandardCharsets.UTF_8))) {
            pw.write(getLogMessagePrefix() + message + "\n");

            StringWriter sw = new StringWriter();
            t.printStackTrace(new PrintWriter(sw));
            String exceptionAsString = sw.toString();
            pw.write(getLogMessagePrefix() + exceptionAsString);
            pw.write("\n");
        } catch(IOException e) {
            System.err.println("Could not find log file " + file.getAbsolutePath());
            e.printStackTrace();
        }
    }

    @Override
    public void log(WatchrConfigError errorObj) {
        ErrorLevel level = errorObj.getLevel();
        String time = errorObj.getTime();
        String message = errorObj.getMessage();

        if(level == ErrorLevel.INFO) {
            logInfo(time + ": " + message);
        } else if(level == ErrorLevel.WARNING) {
            logWarning(time + ": " + message);
        } else if(level == ErrorLevel.ERROR) {
            logError(time + ": " + message);
        }
    }
}
