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
 * A metric block from a performance report XML.
 * 
 * @author Elliott Ridgway
 */
public class Metric extends XmlDataElement {
    
    public Metric(String name, String date, String units) {
        super(Keywords.CATEGORY_METRIC.get(), name, date, units);
    }
}
