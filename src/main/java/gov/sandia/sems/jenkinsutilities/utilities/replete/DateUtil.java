/*******************************************************************************
* Watchr
* ------
* Copyright 2021 National Technology & Engineering Solutions of Sandia, LLC (NTESS).
* Under the terms of Contract DE-NA0003525 with NTESS, the U.S. Government retains
* certain rights in this software.
******************************************************************************/
package gov.sandia.sems.jenkinsutilities.utilities.replete;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DateUtil {
    private static SimpleDateFormat shortFormat = new SimpleDateFormat("yyyy-MM-dd");
    public static String toShortString(long millis) {
        return toShortString(new Date(millis));
    }
    public static String toShortString(Date d) {
        return shortFormat.format(d);
    }

    private static SimpleDateFormat longFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss a");
    public static String toLongString(long millis) {
        return toLongString(new Date(millis));
    }
    public static String toLongString(Date d) {
        return longFormat.format(d);
    }

    private static SimpleDateFormat longCompactFormat = new SimpleDateFormat("yyyyMMddHHmmss");
    public static String toLongCompactString(long millis) {
        return toLongCompactString(new Date(millis));
    }
    public static String toLongCompactString(Date d) {
        return longCompactFormat.format(d);
    }

    public static String toElapsedString(long origMillis) {
        return toElapsedString(origMillis, ElapsedVerbosity.SHORT);
    }
    public static String toElapsedString(long origMillis, ElapsedVerbosity labelVerbosity) {
        return toElapsedString(origMillis, labelVerbosity, false);
    }
    public static String toElapsedString(long origMillis, ElapsedVerbosity labelVerbosity, boolean spacesAfterNums) {
        return toElapsedString(origMillis, labelVerbosity, spacesAfterNums, false);
    }
    public static String toElapsedString(long origMillis, ElapsedVerbosity labelVerbosity, boolean spacesAfterNums, boolean expandMS) {

        boolean neg = origMillis < 0;
        origMillis = Math.abs(origMillis);

        long millis = origMillis;
        long msPerSec = 1000;
        long msPerMin = msPerSec * 60;
        long msPerHour = msPerMin * 60;
        long msPerDay = msPerHour * 24;
        long msPerYear = msPerDay * 365;  // Technically approximation

        long years = millis / msPerYear;  millis %= msPerYear;
        long days = millis / msPerDay;    millis %= msPerDay;
        long hours = millis / msPerHour;  millis %= msPerHour;
        long mins = millis / msPerMin;    millis %= msPerMin;
        long secs = millis / msPerSec;    millis %= msPerSec;

        String[][] labels = new String[][] {
            {"y",     "d",    "h",     "m",       "s",       "ms"},            // labelVerbosity = 0
            {"yr",    "dy",   "hr",    "min",     "sec",     "msec"},          // labelVerbosity = 1
            {"years", "days", "hours", "minutes", "seconds", "milliseconds"}   // labelVerbosity = 2
        };
        int level = labelVerbosity.ordinal();

        String sp = (!spacesAfterNums && level != 2) ? "" : " ";

        String ret = "";
        if(years != 0) {
            String lbl = labels[level][0];
            if(years == 1 && lbl.endsWith("s")) {
                lbl = lbl.substring(0, lbl.length() - 1);
            }
            ret += years + sp + lbl + " ";
        }
        if(days != 0) {
            String lbl = labels[level][1];
            if(days == 1 && lbl.endsWith("s")) {
                lbl = lbl.substring(0, lbl.length() - 1);
            }
            ret += days + sp + lbl + " ";
        }
        if(hours != 0) {
            String lbl = labels[level][2];
            if(hours == 1 && lbl.endsWith("s")) {
                lbl = lbl.substring(0, lbl.length() - 1);
            }
            ret += hours + sp + lbl + " ";
        }
        if(mins != 0) {
            String lbl = labels[level][3];
            if(mins == 1 && lbl.endsWith("s")) {
                lbl = lbl.substring(0, lbl.length() - 1);
            }
            ret += mins + sp + lbl + " ";
        }
        if(secs != 0 || origMillis == 0) {
            String lbl = labels[level][4];
            if(secs == 1 && lbl.length() > 1 && lbl.endsWith("s")) {
                lbl = lbl.substring(0, lbl.length() - 1);
            }
            ret += secs + sp + lbl + " ";
        }

        // If we want to show the millis like every other time unit...
        if(expandMS && millis != 0) {
            String lbl = labels[level][5];
            if(millis == 1 && level == 2 && lbl.length() > 1 && lbl.endsWith("s")) {
                lbl = lbl.substring(0, lbl.length() - 1);
            }
            ret += millis + sp + lbl;

        // else if we don't want to expand the millis, and the original
        // duration was less than 1 second...
        } else if(millis != 0 && origMillis < 1000) {
            String lbl = labels[level][4];
            if(lbl.length() > 1 && lbl.endsWith("s")) {
                lbl = lbl.substring(0, lbl.length() - 1);
            }
            ret += "<1" + sp + lbl;
        }

        ret = ret.trim();

        if(neg) {
            if(ret.startsWith("<1")) {
                ret = ">-1" + ret.substring(2);
            } else {
                ret = "-" + ret;
            }
        }

        return ret;
    }


    public static String ago(Date date) {
        return ago(date.getTime());
    }
    public static String ago(Long time) {
        return ago(System.currentTimeMillis(), time);
    }
    public static String ago(long now, Date date) {
        return ago(now, date.getTime());
    }
    public static String ago(long now, Long time) {
        String ago = "";
        if(time != null) {
            long elapsedSince = now - time;
            if(elapsedSince > 0) {
                ago = " (" + DateUtil.toElapsedString(elapsedSince,
                    ElapsedVerbosity.MED, true) + " ago)";
            }
        }
        return ago;
    }


    //////////
    // TEST //
    //////////

    public static void main(String[] args) {
        long[] times = {-3500, -500, 0, 1, 10, 1000, 100000, 1458796, 10000000};
        for(boolean expMs : new boolean[] {true, false}) {
            System.out.println("ExpandMs = " + expMs);
            for(boolean spaces : new boolean[] {true, false}) {
                System.out.println("    Spaces = " + spaces);
                for(ElapsedVerbosity level : ElapsedVerbosity.values()) {
                    System.out.println("        Level = " + level);
                    for(long time : times) {
                        p(time, level, spaces, expMs);
                    }
                }
            }
        }
    }
    private static void p(long time, ElapsedVerbosity level, boolean spaces, boolean expandMs) {
        System.out.println("            " + time + " = [" + toElapsedString(time, level, spaces, expandMs) + "]");
    }
}
