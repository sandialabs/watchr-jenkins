/*******************************************************************************
* Watchr
* ------
* Copyright 2021 National Technology & Engineering Solutions of Sandia, LLC (NTESS).
* Under the terms of Contract DE-NA0003525 with NTESS, the U.S. Government retains
* certain rights in this software.
******************************************************************************/
package gov.sandia.sems.jenkinsutilities.utilities;

/**
 * @author Derek Trumbo
 */

public class TicToc {
    private static long start = -1;
    public static long tic() {
        return tic(null);
    }
    public static long tic(String label) {
        if(label != null) {
            System.out.println(label);
        }
        start = System.currentTimeMillis();
        return start;
    }
    public static long toc() {
        return toc(null);
    }
    public static long toc(String label) {
        long now = System.currentTimeMillis();
        if(start == -1) {
            start = now;
        }
        long stop = now - start;
        if(label == null) {
            System.out.println(stop);
        } else {
            System.out.println(label + ": " + stop);
        }
        start = now;
        return stop;
    }
    public static long tocn() {
        long now = System.currentTimeMillis();
        if(start == -1) {
            start = now;
        }
        long stop = now - start;
        start = now;
        return stop;
    }
}
