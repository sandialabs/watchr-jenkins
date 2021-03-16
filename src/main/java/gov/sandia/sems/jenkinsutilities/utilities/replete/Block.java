/*******************************************************************************
* Watchr
* ------
* Copyright 2021 National Technology & Engineering Solutions of Sandia, LLC (NTESS).
* Under the terms of Contract DE-NA0003525 with NTESS, the U.S. Government retains
* certain rights in this software.
******************************************************************************/
package gov.sandia.sems.jenkinsutilities.utilities.replete;

import java.util.LinkedHashMap;
import java.util.Map;

public class Block {

    ////////////
    // FIELDS //
    ////////////

    public String name;
    public long start;
    public long prevStart;
    public long prevEnd;
    public long minElapsed = Long.MAX_VALUE;
    public long maxElapsed = Long.MIN_VALUE;
    public long prevElapsed;
    public long totalElapsed;
    public int iterations;
    public Map<String, Block> children = new LinkedHashMap<>();
    public long estimatedIterations;
    public long lastPrintTime = -1;
    public boolean isStep;


    /////////////////
    // CONSTRUCTOR //
    /////////////////

    public Block(String name, long start, long estIter, boolean isStep) {
        this.name = name;
        this.start = start;
        iterations = 1;
        estimatedIterations = estIter;
        this.isStep = isStep;
    }


    //////////////////////////
    // ACCESSORS / MUTATORS //
    //////////////////////////

    public boolean isStep() {
        return isStep;
    }

    public void registerElapsed(long newEnd, long newElapsed) {
        prevEnd = newEnd;
        prevElapsed = newElapsed;
        totalElapsed += newElapsed;
        if(newElapsed < minElapsed) {
            minElapsed = newElapsed;
        }
        if(newElapsed > maxElapsed) {
            maxElapsed = newElapsed;
        }
    }


    ////////////
    // RENDER //
    ////////////

    // These methods return just the most important summary
    // information of total time spent in the block, how many
    // iterations the block has had, and average time per iteration
    // spent in the block.

    public String toString(boolean prettyTime, long baseTime) {   // Base time not used yet
        double avg = (double) totalElapsed / iterations;
        avg = Math.round(avg * 10) / 10.0;
        String e = prettyTime ? DateUtil.toElapsedString(totalElapsed) : totalElapsed + " ms";
        String a = prettyTime ? DateUtil.toElapsedString((long) avg) : avg + " ms";
        return
            name +
            ": T(" + e +
            ") I(" + iterations +
            ") A(" + a + ")";
    }

    public String[] toStringFields(boolean prettyTime, long baseTime, long now) {
        double avg = (double) totalElapsed / iterations;
        avg = Math.round(avg * 10) / 10.0;
        String e = prettyTime ? DateUtil.toElapsedString(totalElapsed) : totalElapsed + " ms";
        String a = prettyTime ? DateUtil.toElapsedString((long) avg) : avg + " ms";
        String estIter = "";
        String estRem = "";
        String estTime = "";

        if(estimatedIterations >= 0) {
            long estimatedRemainingIterations = estimatedIterations - iterations;

            double msPerIter = (double) totalElapsed / iterations;
            double remaining = (estimatedIterations - iterations) * msPerIter;
            long estimatedRemainingDuration = (long) remaining;

            long estimatedEndTime = now + estimatedRemainingDuration;

            estIter = "" + estimatedRemainingIterations;

            estRem =
                prettyTime ?
                    DateUtil.toElapsedString(estimatedRemainingDuration) :
                        estimatedRemainingDuration + " ms";
            estTime =
                prettyTime ?
                    DateUtil.toLongString(estimatedEndTime) :
                        (estimatedEndTime - baseTime) + " ms";
        }

        return new String[] {
            name,
            "" + e,
            "" + iterations,
            "" + a,
            estIter,
            estRem,
            estTime
        };
    }
}
