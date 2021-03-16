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
 * A block of metadata from a performance report XML.
 * 
 * @author Elliott Ridgway
 */
public class MetaData extends XmlDataElement {

    private String key;
    private String value;
    
    public MetaData() {
        super(Keywords.CATEGORY_METADATA.get(), "", "", "");
    }

    @Override
    public String getName() {
        return key;
    }
    
    public String getValue() {
        return value;
    }
    
    public void setName(String key) {
        this.key = key;
    }

    public void setValue(String value) {
        this.value = value;
    }   
}
