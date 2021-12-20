/*******************************************************************************
* Watchr
* ------
* Copyright 2021 National Technology & Engineering Solutions of Sandia, LLC (NTESS).
* Under the terms of Contract DE-NA0003525 with NTESS, the U.S. Government retains
* certain rights in this software.
******************************************************************************/
package gov.sandia.watchr.impl;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;

import gov.sandia.watchr.config.WatchrConfigError;
import gov.sandia.watchr.config.WatchrConfigError.ErrorLevel;
import gov.sandia.watchr.log.ILogger;
import gov.sandia.watchr.util.DateUtil;

public class WatchrJenkinsLogger implements ILogger {

    private File file;
    private ErrorLevel loggingLevel = ErrorLevel.INFO;

    public WatchrJenkinsLogger(File file) {
        this.file = file;
    }

    @Override
    public void logError(String err) {
        if(loggingLevel.ordinal() <= ErrorLevel.ERROR.ordinal()) {
            writeToLog(err, ErrorLevel.ERROR);
        }
    }

    @Override
    public void logError(String err, Throwable t) {
        if(loggingLevel.ordinal() <= ErrorLevel.ERROR.ordinal()) {
            writeErrorToLog(err, ErrorLevel.ERROR, t);
        }
    }

    @Override
    public void logInfo(String info) {
        if(loggingLevel.ordinal() <= ErrorLevel.INFO.ordinal()) {
            writeToLog(info, ErrorLevel.INFO);
        }
    }

    @Override
    public void logWarning(String warning) {
        if(loggingLevel.ordinal() <= ErrorLevel.WARNING.ordinal()) {
            writeToLog(warning, ErrorLevel.WARNING);
        }
    }

    @Override
    public void logDebug(String debug) {
        if(loggingLevel.ordinal() <= ErrorLevel.DEBUG.ordinal()) {
            writeToLog(debug, ErrorLevel.DEBUG);
        }
    }
    
    @Override
    public void log(WatchrConfigError errorObj) {
        ErrorLevel level = errorObj.getLevel();
        String time = errorObj.getTime();
        String message = errorObj.getMessage();

        if(level == ErrorLevel.DEBUG) {
            logDebug(time + ": " + message);
        } else if(level == ErrorLevel.INFO) {
            logInfo(time + ": " + message);
        } else if(level == ErrorLevel.WARNING) {
            logWarning(time + ": " + message);
        } else if(level == ErrorLevel.ERROR) {
            logError(time + ": " + message);
        }
    }

    @Override
    public ErrorLevel getLoggingLevel() {
        return loggingLevel;
    }

    @Override
    public void setLoggingLevel(ErrorLevel loggingLevel) {
        this.loggingLevel = loggingLevel;
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

    private String getLogMessageSeverity(ErrorLevel level) {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        sb.append(level.toString());
        sb.append("] ");
        return sb.toString();
    }
 
    private void writeToLog(String message, WatchrConfigError.ErrorLevel errorLevel) {
        try(PrintWriter pw = new PrintWriter(new OutputStreamWriter(new FileOutputStream(file, true), StandardCharsets.UTF_8))) {
            pw.write(getLogMessagePrefix() + getLogMessageSeverity(errorLevel) + message + "\n");
        } catch(IOException e) {
            System.err.println("Could not find log file " + file.getAbsolutePath());
            e.printStackTrace();
        }
    }

    private void writeErrorToLog(String message, WatchrConfigError.ErrorLevel errorLevel, Throwable t) {
        try(PrintWriter pw = new PrintWriter(new OutputStreamWriter(new FileOutputStream(file, true), StandardCharsets.UTF_8))) {
            pw.write(getLogMessagePrefix() + getLogMessageSeverity(errorLevel) + message + "\n");

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
}
