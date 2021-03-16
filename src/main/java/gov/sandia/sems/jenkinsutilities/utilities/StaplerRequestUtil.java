/*******************************************************************************
* Watchr
* ------
* Copyright 2021 National Technology & Engineering Solutions of Sandia, LLC (NTESS).
* Under the terms of Contract DE-NA0003525 with NTESS, the U.S. Government retains
* certain rights in this software.
******************************************************************************/
package gov.sandia.sems.jenkinsutilities.utilities;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.kohsuke.stapler.Stapler;
import org.kohsuke.stapler.StaplerRequest;

/**
 *
 * @author Elliott Ridgway
 */
public class StaplerRequestUtil {
    
    public static String echoCurrentStaplerRequest(File logFile) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append(Stapler.getCurrentRequest().getParameterMap().toString());
            sb.append("\r\nParameter Names:");
            List<String> parameterNames = Collections.list(Stapler.getCurrentRequest().getParameterNames());
            for(String parameterName : parameterNames) {
                sb.append(parameterName).append(", ");
            }
            LogUtil.writeToLog(logFile, sb.toString());
            return sb.toString();
        } catch(Exception e) {
            LogUtil.writeErrorToLog(logFile, e);
        }
        return "";
    }
    
    public static boolean isRequestParameterMapEmpty() {
        StaplerRequest request = Stapler.getCurrentRequest();
        return request.getParameterMap().isEmpty();
    }
    
    public static String parseStringParameter(String paramName, String dflt) {       
        StaplerRequest request = Stapler.getCurrentRequest();
        if(request != null) {
            String values[] = request.getParameterValues(paramName);
            if(values != null && values.length > 0) {
                String value = request.getParameterValues(paramName)[0];
                return !StringUtils.isBlank(value) ? value : dflt;
            }
        }
        return dflt;
    }
        
    public static int parseIntParameter(String paramName, int dflt) throws NumberFormatException {       
        StaplerRequest request = Stapler.getCurrentRequest();
        if(request != null) {
            String values[] = request.getParameterValues(paramName);
            if(values != null && values.length > 0) {
                String value = request.getParameterValues(paramName)[0];
                if(!StringUtils.isBlank(value)) {
                    return Integer.parseInt(value);
                }
            }
        }
        return dflt;
    }
    
    public static Boolean parseBooleanParameter(String paramName, boolean dflt) {
        StaplerRequest request = Stapler.getCurrentRequest();
        if(request != null && !request.getParameterMap().isEmpty()) {
            String[] values = request.getParameterValues(paramName);
            if(values != null && values.length > 0) {
                String value = values[0];
                return !StringUtils.isBlank(value) ? (value.equals("on") || value.equals("true")) : dflt;
            }
        }
        return false;
    }
    
    public static boolean parameterNameExists(String paramName) {
        StaplerRequest request = Stapler.getCurrentRequest();
        if(request != null && !request.getParameterMap().isEmpty()) {
            List<String> parameterNames = Collections.list(request.getParameterNames());
            return parameterNames.contains(paramName);
        }
        return false;
    }
    
    public static boolean parameterValueExists(String paramName) {
        StaplerRequest request = Stapler.getCurrentRequest();
        if(request != null && !request.getParameterMap().isEmpty()) {
            String[] values = request.getParameterValues(paramName);
            return (values != null && values.length > 0);
        }
        return false;
    }

    public static List<String> findStringParameter(String regex) {
        StaplerRequest request = Stapler.getCurrentRequest();
        List<String> matches = new ArrayList<>();
        if(request != null) {
            Enumeration<String> paramNames = request.getParameterNames();
            while(paramNames.hasMoreElements()) {
                String nextParameterName = paramNames.nextElement();
                if(nextParameterName.matches(regex)) {
                    matches.add(nextParameterName);
                }
            }
        }
        return matches;
    }
}