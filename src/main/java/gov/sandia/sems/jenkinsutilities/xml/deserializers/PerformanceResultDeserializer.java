/*******************************************************************************
* Watchr
* ------
* Copyright 2021 National Technology & Engineering Solutions of Sandia, LLC (NTESS).
* Under the terms of Contract DE-NA0003525 with NTESS, the U.S. Government retains
* certain rights in this software.
******************************************************************************/
package gov.sandia.sems.jenkinsutilities.xml.deserializers;

import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import gov.sandia.sems.jenkinsutilities.xml.PerformanceReport;


/**
 * 
 * @author Lawrence Allen
 */
public interface PerformanceResultDeserializer {
    public PerformanceReport deserialize(InputStream stream) throws ParseException, SAXException, IOException, ParserConfigurationException;
}
