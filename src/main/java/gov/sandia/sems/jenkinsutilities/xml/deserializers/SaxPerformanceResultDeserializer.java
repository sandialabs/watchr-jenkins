/*******************************************************************************
* Watchr
* ------
* Copyright 2021 National Technology & Engineering Solutions of Sandia, LLC (NTESS).
* Under the terms of Contract DE-NA0003525 with NTESS, the U.S. Government retains
* certain rights in this software.
******************************************************************************/
package gov.sandia.sems.jenkinsutilities.xml.deserializers;

import gov.sandia.sems.jenkins.semsjppplugin.Keywords;
import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.SchemaFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

import gov.sandia.sems.jenkinsutilities.xml.MetaData;
import gov.sandia.sems.jenkinsutilities.xml.Metric;
import gov.sandia.sems.jenkinsutilities.xml.PerformanceReport;
import gov.sandia.sems.jenkinsutilities.xml.TimingBlock;
import gov.sandia.sems.jenkinsutilities.xml.XmlDataElement;
import java.util.HashSet;
import java.util.Set;
import java.util.Stack;
import org.apache.commons.lang3.math.NumberUtils;

/**
 * 
 * @author Elliott Ridgway, Lawrence Allen
 */
public class SaxPerformanceResultDeserializer implements PerformanceResultDeserializer {
    
    @Override
    public PerformanceReport deserialize(InputStream stream) throws SAXException, ParserConfigurationException, IOException {
        SAXParserFactory parserFactory = SAXParserFactory.newInstance();
        SchemaFactory schemaFactory = SchemaFactory.newInstance("http://www.w3.org/2001/XMLSchema");
        parserFactory.setSchema(
            schemaFactory.newSchema(
                new Source[] {
                    new StreamSource(SaxPerformanceResultDeserializer.class.getResourceAsStream("perf-report.xsd"))
                }
            )
        );
        SAXParser parser;
        parser = parserFactory.newSAXParser();
        TimingDataHandler handler = new TimingDataHandler();
        parser.parse(stream, handler);
        return handler.getReport();
    }
}

class TimingDataHandler extends DefaultHandler {
    
    ////////////
    // FIELDS //
    ////////////
    
    private PerformanceReport report;
    private final Set<String> discoveredMeasurables = new HashSet<>();    
    private final Stack<XmlDataElement> parents = new Stack<>();

    /////////////
    // GETTERS //
    /////////////

    public PerformanceReport getReport() {
        return report;
    }
    
    public Set<String> getDiscoveredMeasurables() {
        return discoveredMeasurables;
    }
    
    //////////////
    // OVERRIDE //
    //////////////
    
    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        if(qName.equals(Keywords.CATEGORY_PERFORMANCE_REPORT.get())) {
            report = constructPerformanceReport(attributes);
            parents.push(report);
        } else if(qName.equals(Keywords.CATEGORY_TIMING.get())) {
            TimingBlock timing = constructTimingBlock(attributes);
            parents.push(timing);
        } else if(qName.equals(Keywords.CATEGORY_METRIC.get())) {
            Metric metric = constructMetric(attributes);
            parents.push(metric);
        } else if(qName.equals(Keywords.CATEGORY_METADATA.get())) {
            MetaData metadata = new MetaData();
            metadata.setName(getValueFromAttributes(attributes, Keywords.METADATA_KEY.get()));
            metadata.setValue(getValueFromAttributes(attributes, Keywords.METADATA_VALUE.get()));
            parents.push(metadata);
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        if(!parents.isEmpty()) {
            XmlDataElement endElement = parents.pop();
            if(!parents.isEmpty()) {
                parents.peek().getChildren().add(endElement);
            }
        }
    }
    
    /////////////
    // UTILITY //
    /////////////
        
    private String getValueFromAttributes(Attributes attributes, String key) {
        String value = attributes.getValue(key);
        if(value == null || value.equals("null")) {
            return null;
        }
        return value;
    }
    
    private boolean valueExists(Attributes attributes, String key) {
        return attributes.getValue(key) != null;
    }
    
    private PerformanceReport constructPerformanceReport(Attributes attributes) throws SAXParseException {
        String name = "";
        String date = "";
        String units = "";
        if(valueExists(attributes, Keywords.NAME.get())) {
            name = getValueFromAttributes(attributes, Keywords.NAME.get());
        }
        if(valueExists(attributes, Keywords.DATE.get())) {
            date = getValueFromAttributes(attributes, Keywords.DATE.get());
        }
        if(valueExists(attributes, Keywords.UNITS.get())) {
            units = getValueFromAttributes(attributes, Keywords.UNITS.get());
        } else if(valueExists(attributes, Keywords.TIME_UNITS.get())) {
            units = getValueFromAttributes(attributes, Keywords.TIME_UNITS.get());
        }
        
        PerformanceReport thisReport = new PerformanceReport(name, date, units);
        return thisReport;
    }
    
    private TimingBlock constructTimingBlock(Attributes attributes) throws SAXException {
        String name = "";
        String date = "";
        String units = "";
        if(valueExists(attributes, Keywords.NAME.get())) {
            name = getValueFromAttributes(attributes, Keywords.NAME.get());
        }
        
        if(valueExists(attributes, Keywords.DATE.get())) {
            date = getValueFromAttributes(attributes, Keywords.DATE.get());
        } else if(valueExists(attributes, Keywords.END_TIME.get())) {
            date = getValueFromAttributes(attributes, Keywords.END_TIME.get());
        }
        
        if(valueExists(attributes, Keywords.UNITS.get())) {
            units = getValueFromAttributes(attributes, Keywords.UNITS.get());
        } else if(valueExists(attributes, Keywords.TIME_UNITS.get())) {
            units = getValueFromAttributes(attributes, Keywords.TIME_UNITS.get());
        }
        
        TimingBlock block = new TimingBlock(name, date, units);
        
        for(int i = 0; i < attributes.getLength(); i++) {
            String attrName = attributes.getQName(i);
            if(!attrName.equals(Keywords.NAME.get()) &&
               !attrName.equals(Keywords.DATE.get()) &&
               !attrName.equals(Keywords.END_TIME.get()) &&
               !attrName.equals(Keywords.UNITS.get()) &&
               !attrName.equals(Keywords.TIME_UNITS.get())) {
                String attrValue = attributes.getValue(attrName);
                
                if(NumberUtils.isCreatable(attrValue)) {
                    block.getAttributes().put(attrName, NumberUtils.createDouble(attrValue));
                } else if(attrValue.toUpperCase().equals("TRUE") || attrValue.toUpperCase().equals("FALSE")) {
                    block.getAttributes().put(attrName, Boolean.parseBoolean(attrValue) ? 1.0 : 0.0);
                } else {
                    throw new SAXException("Cannot store value " + attrValue);
                }
            }
        }
        return block;
    }

    private Metric constructMetric(Attributes attributes) throws SAXException {
        String name = "";
        String date = "";
        String units = "";
        if(valueExists(attributes, Keywords.NAME.get())) {
            name = getValueFromAttributes(attributes, Keywords.NAME.get());
        }
        if(valueExists(attributes, Keywords.DATE.get())) {
            date = getValueFromAttributes(attributes, Keywords.DATE.get());
        }
        if(valueExists(attributes, Keywords.UNITS.get())) {
            units = getValueFromAttributes(attributes, Keywords.UNITS.get());
        } else if(valueExists(attributes, Keywords.TIME_UNITS.get())) {
            units = getValueFromAttributes(attributes, Keywords.TIME_UNITS.get());
        }
        
        Metric metric = new Metric(name, date, units);
        
        for(int i = 0; i < attributes.getLength(); i++) {
            String attrName = attributes.getQName(i);
            if(!attrName.equals(Keywords.NAME.get()) &&
               !attrName.equals(Keywords.DATE.get()) &&
               !attrName.equals(Keywords.END_TIME.get()) &&
               !attrName.equals(Keywords.UNITS.get()) &&
               !attrName.equals(Keywords.TIME_UNITS.get())) {
                String attrValue = attributes.getValue(attrName);
                
                if(NumberUtils.isCreatable(attrValue)) {
                    metric.getAttributes().put(attrName, NumberUtils.createDouble(attrValue));
                } else if(attrValue.toUpperCase().equals("TRUE") || attrValue.toUpperCase().equals("FALSE")) {
                    metric.getAttributes().put(attrName, Boolean.parseBoolean(attrValue) ? 1.0 : 0.0);
                } else {
                    throw new SAXException("Cannot store value " + attrValue);
                }
            }
        }
        return metric;
    }
}