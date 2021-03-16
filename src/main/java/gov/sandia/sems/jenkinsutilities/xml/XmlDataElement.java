/*******************************************************************************
* Watchr
* ------
* Copyright 2021 National Technology & Engineering Solutions of Sandia, LLC (NTESS).
* Under the terms of Contract DE-NA0003525 with NTESS, the U.S. Government retains
* certain rights in this software.
******************************************************************************/
package gov.sandia.sems.jenkinsutilities.xml;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * An abstract block of XML data found in a performance report XML.
 * 
 * @author Elliott Ridgway
 */
public abstract class XmlDataElement {
    
    ////////////
    // FIELDS //
    ////////////

    private final String category;    
    private final String name;
    private final String date;
    private final String units;

    private final Map<String, Double> attributes;
    
    private final List<XmlDataElement> children;
    
    /////////////////
    // CONSTRUCTOR //
    /////////////////
    
    public XmlDataElement(String category, String name, String date, String units) {
        this.category = category;
        this.name     = name;
        this.date     = date;
        this.units    = units;
        
        this.attributes = new HashMap<>();
        this.children = new ArrayList<>();
    }
    
    /////////////
    // GETTERS //
    /////////////
    
    public String getCategory() {
        return category;
    }
    
    public String getName() {
        return name;
    }
    
    public String getDate() {
        return date;
    }
    
    public String getUnits() {
        return units;
    }
    
    public Map<String, Double> getAttributes() {
        return attributes;
    }
    
    public List<XmlDataElement> getChildren() {
        return children;
    }
    
    public List<? extends XmlDataElement> getChildren(Class<?> clazz) {
        List<XmlDataElement> filteredChildren = new ArrayList<>();
        for(XmlDataElement child : children) {
            if(child.getClass() == clazz) {
                filteredChildren.add(child);
            }
        }
        return filteredChildren;
    }
    
    //////////////
    // OVERRIDE //
    //////////////
    
    @Override
    public boolean equals(Object other) {
        if(other instanceof XmlDataElement) {
            XmlDataElement otherElement = (XmlDataElement) other;
            
            boolean equals = true;
            equals = equals && otherElement.getCategory().equals(category);
            equals = equals && otherElement.getName().equals(name);
            equals = equals && otherElement.getDate().equals(date);
            equals = equals && otherElement.getUnits().equals(units);
            equals = equals && otherElement.getAttributes().equals(attributes);
            return equals;
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 97 * hash + Objects.hashCode(this.category);
        hash = 97 * hash + Objects.hashCode(this.name);
        hash = 97 * hash + Objects.hashCode(this.date);
        hash = 97 * hash + Objects.hashCode(this.units);
        hash = 97 * hash + Objects.hashCode(this.attributes);
        hash = 97 * hash + Objects.hashCode(this.children);
        return hash;
    }
}
