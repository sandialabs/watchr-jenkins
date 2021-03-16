/*******************************************************************************
* Watchr
* ------
* Copyright 2021 National Technology & Engineering Solutions of Sandia, LLC (NTESS).
* Under the terms of Contract DE-NA0003525 with NTESS, the U.S. Government retains
* certain rights in this software.
******************************************************************************/
package gov.sandia.sems.jenkinsutilities.utilities;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author Elliott Ridgway
 */

public class DateUtil {
    
    public static final DateFormat DATE_FORMAT_1 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
    public static final DateFormat DATE_FORMAT_2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private static final String DATE_FORMAT_REGEX_1 = new String("\\d{4}\\-\\d{2}\\-\\d{2}T\\d{2}:\\d{2}:\\d{2}");
    private static final String DATE_FORMAT_REGEX_2 = new String("\\d{4}\\-\\d{2}\\-\\d{2}(\\s*)\\d{2}:\\d{2}:\\d{2}");
    
    /**
     * Get a past date from a {@link List} of {@link String} dates.
     * 
     * @param sortedDates The List of dates to inspect.  This method expects them to be sorted
     * from oldest to newest.
     * @param dataPointsInPast The number of points in the past, starting from the last date in the list.
     * @return The past date.
     */
    public static String getPastDateFromNumberOfPoints(List<String> sortedDates, int dataPointsInPast) {
        int lastIndex = sortedDates.size() - 1;
        int oldestDateIndex = lastIndex - dataPointsInPast;
        if(oldestDateIndex < 0) {
            oldestDateIndex = 0;
        }
        return sortedDates.get(oldestDateIndex);
    }

    /**
     * Get a past date from a {@link List} of {@link String} dates.
     * 
     * @param dates The List of date Strings.
     * @param startDateStr The String date to start from.
     * @param daysInPast The number of days into the past, starting from {@code startDateStr}.
     * @return The past date.
     * @throws ParseException Thrown if the date format detected in the List of Strings could not be parsed.
     */
    public static String getPastDate(List<String> dates, String startDateStr, int daysInPast) throws ParseException {
        DateFormat dateFormat = identifyDateFormat(startDateStr);
        if(dateFormat != null) {
            Date startDate = dateFormat.parse(startDateStr);
            
            Calendar cal = Calendar.getInstance();
            cal.setTime(startDate);
            cal.add(Calendar.DATE, -daysInPast);
            Date targetDate = cal.getTime();
            
            String returnDate = "";
            long minimumDistance = Long.MAX_VALUE;
            for(String dateStr : dates) {
                Date date = dateFormat.parse(dateStr);            
                long distance = Math.abs(date.getTime() - targetDate.getTime());
                if(distance < minimumDistance) {
                    minimumDistance = distance;
                    returnDate = dateStr;
                }
            }
            
            return returnDate;
        }
        return new String();
    }

    /**
     * Verifies whether a given date {@link String} is within a range.
     * 
     * @param testDateStr The date String to inspect.
     * @param lowerBoundDateStr The lower bound date (strictly exclusive).
     * @param upperBoundDateStr The upper bound date (strictly exclusive).
     * @return Whether {@code testDateStr} is within the proper bounds.
     * @throws ParseException Thrown if the date format detected in the List of Strings could not be parsed.
     */
    public static boolean isWithinRange(String testDateStr, String lowerBoundDateStr, String upperBoundDateStr) throws ParseException {       
        DateFormat dateFormat = identifyDateFormat(testDateStr);
        if(dateFormat != null && StringUtils.isNotBlank(testDateStr) &&
                StringUtils.isNotBlank(lowerBoundDateStr) && StringUtils.isNotBlank(upperBoundDateStr)) {
            Date testDate = dateFormat.parse(testDateStr);
            Date lowerBoundDate = dateFormat.parse(lowerBoundDateStr);
            Date upperBoundDate = dateFormat.parse(upperBoundDateStr);
            
            if(testDate != null && lowerBoundDate != null && upperBoundDate != null) {
                return (testDate.before(upperBoundDate) || testDate.equals(upperBoundDate)) &&
                       (testDate.after(lowerBoundDate) || testDate.equals(lowerBoundDate));
            }

            return true;
        }
        return false;
    }   

    /**
     * Identify a {@link DateFormat} based on this class's list
     * of known date formats.
     * 
     * @param date The date {@link String} to inspect.
     * @return The identified DateFormat object, or null if the String
     * is not identifiable as a DateFormat.
     */
    public static DateFormat identifyDateFormat(String date) {
        if(date.matches(DATE_FORMAT_REGEX_1)) {
            return DATE_FORMAT_1;
        } else if(date.matches(DATE_FORMAT_REGEX_2)) {
            return DATE_FORMAT_2;
        } else {
            return null;
        }
    }

    /**
     * 
     * @param epochTime
     * @return
     */
    public static String epochTimeToTimestamp(long epochTime) {
        Date date = new Date(epochTime);
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        return format.format(date);
    }
}
