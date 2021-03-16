/*******************************************************************************
* Watchr
* ------
* Copyright 2021 National Technology & Engineering Solutions of Sandia, LLC (NTESS).
* Under the terms of Contract DE-NA0003525 with NTESS, the U.S. Government retains
* certain rights in this software.
******************************************************************************/
package gov.sandia.sems.jenkinsutilities.utilities;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Elliott Ridgway
 */
public class DateUtilTest {

    private final List<String> dates = new ArrayList<>();
    
    @Before
    public void setup() {
        dates.add("2018-10-11T00:00:00");
        dates.add("2018-10-12T00:00:00");
        dates.add("2018-10-13T00:00:00");
        dates.add("2018-10-14T00:00:00");
        dates.add("2018-10-15T00:00:00");
        dates.add("2018-10-16T00:00:00");
        dates.add("2018-10-17T00:00:00");
        dates.add("2018-10-18T00:00:00");
        dates.add("2018-10-19T00:00:00");
    }

    @After
    public void teardown() {
        dates.clear();
    }

    @Test
    public void testGetPastDateFromNumberOfPoints() {
        int pointsInPast = 7;
        String resultDate = DateUtil.getPastDateFromNumberOfPoints(dates, pointsInPast);
        Assert.assertEquals("2018-10-12T00:00:00", resultDate);
    }

    @Test
    public void testGetPastDateFromNumberOfPoints_MultipleMeasurementsOnSameDay() {
        dates.add("2018-10-19T01:00:00");
        dates.add("2018-10-19T02:00:00");
        dates.add("2018-10-19T03:00:00");
        dates.add("2018-10-19T04:00:00");
        dates.add("2018-10-19T05:00:00");
        dates.add("2018-10-19T06:00:00");
        dates.add("2018-10-19T07:00:00");

        int pointsInPast = 7;
        String resultDate = DateUtil.getPastDateFromNumberOfPoints(dates, pointsInPast);
        Assert.assertEquals("2018-10-19T00:00:00", resultDate);
    }
    
    @Test
    public void testGetPastDate() {
        String startDate = "2018-10-19T00:00:00";
        int daysInPast = 7;
        
        try {
            String resultDate = DateUtil.getPastDate(dates, startDate, daysInPast);
            Assert.assertEquals("2018-10-12T00:00:00", resultDate);
        } catch(ParseException e) {
            Assert.fail(e.getMessage());
        }
    }
    
    @Test
    public void testIsWithinRange() {
        String date = "2018-10-15T00:00:00";
        try {
            Assert.assertTrue(DateUtil.isWithinRange(date, "2018-10-11T00:00:00", "2018-10-19T00:00:00"));
            Assert.assertTrue(DateUtil.isWithinRange(date, "2018-10-15T00:00:00", "2018-10-19T00:00:00"));
            Assert.assertFalse(DateUtil.isWithinRange(date, "2018-10-16T00:00:00", "2018-10-19T00:00:00"));
        } catch(ParseException e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void testIdentifyDateFormat_TFormatMatch() {
        String date = "2018-10-15T02:03:04";
        Assert.assertEquals(DateUtil.DATE_FORMAT_1, DateUtil.identifyDateFormat(date));
    }

    @Test
    public void testIdentifyDateFormat_SpaceFormatMatch() {
        String date = "2018-10-15 02:03:04";
        Assert.assertEquals(DateUtil.DATE_FORMAT_2, DateUtil.identifyDateFormat(date));
    }

    @Test
    public void testIdentifyDateFormat_BadMatch() {
        String date = "2018-10-15 AB:CD:EF";
        Assert.assertNull(DateUtil.identifyDateFormat(date));
    }
}
