/*******************************************************************************
* Watchr
* ------
* Copyright 2021 National Technology & Engineering Solutions of Sandia, LLC (NTESS).
* Under the terms of Contract DE-NA0003525 with NTESS, the U.S. Government retains
* certain rights in this software.
******************************************************************************/
package gov.sandia.sems.jenkinsutilities.xml;

import gov.sandia.sems.jenkins.semsjppplugin.Keywords;

/**
 * The PerformanceReport object should be the top-level element in a performance report XML.
 * 
 * @author Elliott Ridgway
 */
public class PerformanceReport extends XmlDataElement implements Comparable<PerformanceReport> {

    public PerformanceReport(String name, String date, String units) {
        super(Keywords.CATEGORY_PERFORMANCE_REPORT.get(), name, date, units);
    }

    @Override
    public int compareTo(PerformanceReport o) {
        return getDate().compareTo(o.getDate());
    }
}
