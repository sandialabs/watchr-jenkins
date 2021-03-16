/*******************************************************************************
* Watchr
* ------
* Copyright 2021 National Technology & Engineering Solutions of Sandia, LLC (NTESS).
* Under the terms of Contract DE-NA0003525 with NTESS, the U.S. Government retains
* certain rights in this software.
******************************************************************************/
package gov.sandia.sems.jenkinsutilities.utilities;

public class NumUtil {
    public static boolean isInt(String s) {
        try {
            Integer.parseInt(s);
            return true;
        } catch(Exception e) {
            return false;
        }
    }
    public static boolean isDouble(String s) {
        try {
            Double.parseDouble(s);
            return true;
        } catch(Exception e) {
            return false;
        }
    }
    public static Integer i(String s) {
        try {
            return Integer.parseInt(s);
        } catch(Exception e) {
            return null;
        }
    }
    public static Integer i(String s, int dflt) {
        try {
            return Integer.parseInt(s);
        } catch(Exception e) {
            return dflt;
        }
    }
    public static Long l(String s) {
        try {
            return Long.parseLong(s);
        } catch(Exception e) {
            return null;
        }
    }
    public static Float f(String s) {
        try {
            return Float.parseFloat(s);
        } catch(Exception e) {
            return null;
        }
    }
    public static Double d(String s) {
        try {
            return Double.parseDouble(s);
        } catch(Exception e) {
            return null;
        }
    }
    public static String pct(double dis, double size) {
        return "" + ((int) (1000.0 * dis / size) / 10.0) + "%";
    }
    public static String pctInt(double dis, double size) {
        return "" + ((int) (100.0 * dis / size)) + "%";
    }
    
    public static double truncateTo( double unroundedNumber, int decimalPlaces ) {
        String format = "%." + decimalPlaces +"f";
        String ret = "";
        ret = String.format(format, unroundedNumber);
        return Double.parseDouble(ret);
    }
}
