/**
 * Copyright (c) 2015-2016, President and Fellows of Harvard College
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *
 * 3. The name of the author may not be used to endorse or promote products
 * derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR "AS IS" AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO
 * EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED
 * TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package edu.harvard.catalyst.scheduler.util;

import org.junit.Assert;
import org.junit.Test;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Calendar;
import java.util.Date;

import static junit.framework.TestCase.assertNull;
import static org.junit.Assert.*;

/**
 * @author Bill Simons
 * @date 8/2/13
 * @link http://cbmi.med.harvard.edu
 * @link http://chip.org
 */
public final class DateUtilityTest {

    private static final SimpleDateFormat TEST_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @Test
    public void testParse() throws ParseException {
        Assert.assertEquals("MONTH_DAY_YEAR format not parsed as expected", TEST_FORMAT.parse("2013-12-17 00:00:00"), DateUtility.parse(DateUtility.monthDayYear(), "12/17/2013"));
        Assert.assertEquals("DATE_HR_MIN_SEC format not parsed as expected", TEST_FORMAT.parse("2013-12-17 11:57:45"), DateUtility.parse(DateUtility.dateHourMinSec(), "2013-12-17 11:57:45"));
        Assert.assertEquals("IE8 format not parsed as expected", TEST_FORMAT.parse("2013-12-17 11:57:45"), DateUtility.parse(DateUtility.ie8(), "2013-12-17T11:57:45Z"));
        Assert.assertEquals("OTHER_BROWSER format not parsed as expected", TEST_FORMAT.parse("2013-12-17 11:57:45"), DateUtility.parse(DateUtility.otherBrowser(), "2013-12-17T11:57:45.000Z"));
        Assert.assertEquals("GANTT format not parsed as expected", TEST_FORMAT.parse("2013-12-17 11:57:45"), DateUtility.parse(DateUtility.gantt(), "2013-12-17T11:57:45"));
        //TODO - Need assert for FULL_CALENDER AND IE_FULL_CALENDAR
        Assert.assertEquals("YEAR_MONTH_DAY format not parsed as expected", TEST_FORMAT.parse("2013-12-17 00:00:00"), DateUtility.parse(DateUtility.yearMonthDay(), "2013-12-17"));
        Assert.assertEquals("DATE_HR_MIN format not parsed as expected", TEST_FORMAT.parse("2013-12-17 11:57:00"), DateUtility.parse(DateUtility.dateHourMin(), "2013-12-17 11:57"));
        Assert.assertEquals("DAY_MONTH_YEAR format not parsed as expected", TEST_FORMAT.parse("2013-12-17 00:00:00"), DateUtility.parse(DateUtility.dateMonthYear(), "17-12-2013"));
    }

    @Test
    public void testFormat() throws ParseException {
        Assert.assertEquals("07:11", DateUtility.format(DateUtility.hourMin(), TEST_FORMAT.parse("2014-01-17 07:11:12")));
        Assert.assertEquals("07", DateUtility.format(DateUtility.hour(), TEST_FORMAT.parse("2014-01-17 07:11:12")));
        Assert.assertEquals("11", DateUtility.format(DateUtility.min(), TEST_FORMAT.parse("2014-01-17 07:11:12")));
    }

    @Test
    public void testTimezoneAgnostic() throws ParseException {
        Assert.assertEquals("Timezones mattered to parser, everything should be default",
                TEST_FORMAT.parse("2013-12-17 00:00:00"),
                DateUtility.parse(DateUtility.subjectDate(), "Tue Dec 17 2013 00:00:00 obqiohjioqerbioebnvnxx343ttvqevrbbij"));
        Assert.assertEquals("Timezones mattered to parser, everything should be default",
                DateUtility.parse(DateUtility.subjectDate(), "Wed Jan 01 1950 00:00:00 EDT-0000 (Eastern Daylight Time)"),
                DateUtility.parse(DateUtility.subjectDate(), "Wed Jan 01 1950 00:00:00 PDT-0000 (Pacific Daylight Time)"));
        Assert.assertEquals("Checking Wartime ",
                TEST_FORMAT.parse("1944-10-02 10:00:00"),
                DateUtility.parse(DateUtility.subjectDate(), "Mon Oct 02 1944 10:00:00 UTC-0400 (EWT)"));
    }

    @Test
    public void testStartOfDay() throws ParseException {
        final Date actual = DateUtility.startOfDay(calendarOf("2011-07-02 13:29:42"));
        assertEquals("Expect beginning of day", TEST_FORMAT.parse("2011-07-02 00:00:00"), actual);
    }

    private Calendar calendarOf(final String dateString) throws ParseException {
        final Date date = TEST_FORMAT.parse(dateString);
        final Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        return cal;
    }

    @Test
    public void testNextDay() throws ParseException {
        final Date actual = DateUtility.nextDay(TEST_FORMAT.parse("2011-07-02 13:29:42"));
        assertEquals("Expect beginning of next day", TEST_FORMAT.parse("2011-07-03 00:00:00"), actual);
    }

    @Test
    public void testNextDayLeapYear() throws ParseException {
        final Date actual = DateUtility.nextDay(TEST_FORMAT.parse("2012-02-28 13:29:42"));
        assertEquals("Expect beginning of day in a leap year", TEST_FORMAT.parse("2012-02-29 00:00:00"), actual);
    }

    @Test
    public void testParseLong() throws ParseException {
        final Date now = new Date();
        final long millis = now.getTime();

        final String millisString = String.valueOf(millis);
        final Date alsoNow = DateUtility.parseLong(millisString);

        TestUtils.assertNonNullAndEqual(now, alsoNow);

        try {
            final Date wtf = DateUtility.parseLong("whoa dude");

            assertTrue("Shouldn't get to this statement", false);
        }
        catch (final NumberFormatException nfe) {
            // happy
        }

        Date nullDate = DateUtility.parseLong(null);
        assertTrue("Should be null", nullDate == null);

        nullDate = DateUtility.parseLong("");
        assertTrue("Should be null", nullDate == null);
    }
    @Test
    public void testDay2minusDay1() throws ParseException {
        final Date aDay = TEST_FORMAT.parse("2012-02-20 13:29:42");
        final Date twoDaysLater = TEST_FORMAT.parse("2012-02-22 13:15:00");
        final Date oneDayEarlier = TEST_FORMAT.parse("2012-02-19 10:15:00");

        assertEquals(0, DateUtility.day2minusDay1(aDay, aDay));

        assertEquals(-2, DateUtility.day2minusDay1(twoDaysLater, aDay));
        assertEquals(2, DateUtility.day2minusDay1(aDay, twoDaysLater));

        assertEquals(1, DateUtility.day2minusDay1(oneDayEarlier, aDay));
        assertEquals(-1, DateUtility.day2minusDay1(aDay, oneDayEarlier));
    }

    @Test
    public void testOldZoneDelta() {
        testZoneHelper(2016, 3, 13, 1, 45, 15);
        testZoneHelper(2016, 3, 13, 1, 45, 30);
        testZoneHelper(2016, 3, 13, 1, 45, 90);

        testZoneHelper(2015, 11, 1, 1, 45, 15);
        testZoneHelper(2015, 11, 1, 1, 45, 30);
        testZoneHelper(2015, 11, 1, 1, 45, 90);

        testZoneHelper(2015, 12, 20, 1, 45, 15);
    }

    private void testZoneHelper(final int year, final int month, final int day,
            final int hour, final int minute,
            final int deltaMinutes) {

        final Calendar calendar1 = Calendar.getInstance();
        calendar1.set(Calendar.YEAR, year);
        calendar1.set(Calendar.MONTH, month - 1);
        calendar1.set(Calendar.DAY_OF_MONTH, day);

        calendar1.set(Calendar.HOUR_OF_DAY, hour);
        calendar1.set(Calendar.MINUTE, minute);

        final Date startDate = calendar1.getTime();
        final Date endDate = DateUtility.oldZoneDeltaDate(startDate, deltaMinutes);

        final long actualDeltaMinutes = (endDate.getTime() - startDate.getTime())/ DateUtility.MILLISECS_PER_MIN;

        //assertEquals(actualDeltaMinutes, deltaMinutes);
    }
    @Test
    public void testDate8Delta() {
        test8helper(2016, 3, 13, 1, 45, 30);
        test8helper(2015, 11, 1, 1, 45, 30);

        test8helper(2015, 12, 20, 1, 45, 15);

        test8helper(2015, 11, 1, 1, 45, 15);
        test8helper(2016, 3, 13, 1, 45, 15);
    }

    void test8helper(final int year, final int month, final int day,
            final int hour, final int minute, final int deltaMinutes) {

        final LocalDateTime start = LocalDateTime.of(year, month, day, hour, minute);

        final LocalDateTime end = DateUtility.oldZoneDeltaDate8(start, deltaMinutes);

        final ZonedDateTime zonedStart = start.atZone(ZoneId.of("America/New_York"));
        final ZonedDateTime zonedEnd = end.atZone(ZoneId.of("America/New_York"));

        final long actualDeltaMinutes =
                (zonedEnd.toEpochSecond() - zonedStart.toEpochSecond()) / 60;

        //assertEquals(deltaMinutes, actualDeltaMinutes);
    }
    
    @Test
    public void testFromDate() {
        final Date now = new Date();
        
        final LocalDateTime ldtNow = DateUtility.dateToLocalDateTime(now);
        
        //java.util.Date years count from 1900; LocalDateTime years count from 0
        assertEquals(now.getYear() + 1900, ldtNow.getYear());
        //java.util.Date months start at 0; LocalDateTime month values start at 1
        assertEquals(now.getMonth() + 1, ldtNow.getMonthValue());
        assertEquals(now.getDate(), ldtNow.getDayOfMonth());
        assertEquals(now.getHours(), ldtNow.getHour());
        assertEquals(now.getMinutes(), ldtNow.getMinute());
        assertEquals(now.getSeconds(), ldtNow.getSecond());
        assertEquals(now.getTime(), ldtNow.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
    }
    
    @Test
    public void testToDate() {
        final LocalDateTime ldtNow = LocalDateTime.now();
        
        final Date now = DateUtility.toDate(ldtNow);
        
        //java.util.Date years count from 1900; LocalDateTime years count from 0
        assertEquals(now.getYear() + 1900, ldtNow.getYear());
        //java.util.Date months start at 0; LocalDateTime month values start at 1
        assertEquals(now.getMonth() + 1, ldtNow.getMonthValue());
        assertEquals(now.getDate(), ldtNow.getDayOfMonth());
        assertEquals(now.getHours(), ldtNow.getHour());
        assertEquals(now.getMinutes(), ldtNow.getMinute());
        assertEquals(now.getSeconds(), ldtNow.getSecond());
        assertEquals(now.getTime(), ldtNow.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
    }
    
    @Test
    public void testToDateRoundTrip() {
        final LocalDateTime ldtNow = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
        
        final LocalDateTime roundTripped = DateUtility.dateToLocalDateTime(DateUtility.toDate(ldtNow)).truncatedTo(ChronoUnit.SECONDS);
        
        assertEquals(ldtNow, roundTripped);
    }

    @Test
    public void dateToDayNumberTest() {
        Date origin = DateUtility.TEMPLATE_RESOURCE_DATE_ORIGIN;

        Date fifthDay = DateUtility.toDate(DateUtility.TEMPLATE_RESOURCE_LOCAL_DATE_TIME_ORIGIN.plusDays(4).plusHours(3));
        Date thirtyFifthDay = DateUtility.toDate(DateUtility.TEMPLATE_RESOURCE_LOCAL_DATE_TIME_ORIGIN.plusDays(34).plusHours(3));

        dateToDayNumberTestHelper(origin, 1);
        dateToDayNumberTestHelper(fifthDay, 5);
        dateToDayNumberTestHelper(thirtyFifthDay, 35);
    }

    void dateToDayNumberTestHelper(Date candidate, int expectedDayNumber) {
        int dayNumber = DateUtility.dateToDayNumber(candidate);

        assertEquals(expectedDayNumber, dayNumber);
    }
    @Test
    public void testPadTime() throws Exception {
        Assert.assertEquals("00", DateUtility.padTime(0));
        Assert.assertEquals("07", DateUtility.padTime(7));
        Assert.assertEquals("11", DateUtility.padTime(11));
    }
    @Test
    public void convertMinutesToStartDayZeroOffsetTest() throws Exception {
        Assert.assertEquals(0, DateUtility.convertMinutesToStartDayZeroOffset(0));
        Assert.assertEquals(0, DateUtility.convertMinutesToStartDayZeroOffset(40));

        Assert.assertEquals(1, DateUtility.convertMinutesToStartDayZeroOffset(DateUtility.MINS_PER_DAY));
        Assert.assertEquals(1, DateUtility.convertMinutesToStartDayZeroOffset(DateUtility.MINS_PER_DAY + 40));

        Assert.assertEquals(50, DateUtility.convertMinutesToStartDayZeroOffset(50 * DateUtility.MINS_PER_DAY));
        Assert.assertEquals(50, DateUtility.convertMinutesToStartDayZeroOffset(50 * DateUtility.MINS_PER_DAY + 40));
    }
    @Test
    public void convertTotalMinutesToMinutesInHourTest() throws Exception {
        Assert.assertEquals(0, DateUtility.convertTotalMinutesToMinutesInHour(0));
        Assert.assertEquals(35, DateUtility.convertTotalMinutesToMinutesInHour(35));
        Assert.assertEquals(0, DateUtility.convertTotalMinutesToMinutesInHour(60));
        Assert.assertEquals(35, DateUtility.convertTotalMinutesToMinutesInHour(95));
    }
    @Test
    public void convertMinutesToHourInDayTest() throws Exception {
        Assert.assertEquals(0, DateUtility.convertMinutesToHourInDay(0));
        Assert.assertEquals(0, DateUtility.convertMinutesToHourInDay(30));
        Assert.assertEquals(1, DateUtility.convertMinutesToHourInDay(60));
        Assert.assertEquals(1, DateUtility.convertMinutesToHourInDay(90));

        Assert.assertEquals(0, DateUtility.convertMinutesToHourInDay(0 + DateUtility.MINS_PER_DAY));
        Assert.assertEquals(0, DateUtility.convertMinutesToHourInDay(30 + DateUtility.MINS_PER_DAY));
        Assert.assertEquals(1, DateUtility.convertMinutesToHourInDay(60 + DateUtility.MINS_PER_DAY));
        Assert.assertEquals(1, DateUtility.convertMinutesToHourInDay(90 + DateUtility.MINS_PER_DAY));

        Assert.assertEquals(0, DateUtility.convertMinutesToHourInDay(0 + 40 * DateUtility.MINS_PER_DAY));
        Assert.assertEquals(0, DateUtility.convertMinutesToHourInDay(30 + 40 * DateUtility.MINS_PER_DAY));
        Assert.assertEquals(1, DateUtility.convertMinutesToHourInDay(60 + 40 * DateUtility.MINS_PER_DAY));
        Assert.assertEquals(1, DateUtility.convertMinutesToHourInDay(90 + 40 * DateUtility.MINS_PER_DAY));

    }
    @Test
    public void minutesFromOneRelativeDateToOtherTest() throws Exception {
        minutesFromOneRelativeDateToOtherTestHelper("2012-02-20 13:29:42", "2012-02-20 13:29:42", 0);
        minutesFromOneRelativeDateToOtherTestHelper("2012-02-20 13:29:42", "2012-02-20 13:30:41", 0);
        minutesFromOneRelativeDateToOtherTestHelper("2012-02-20 13:29:42", "2012-02-20 13:30:42", 1);
    }

    void minutesFromOneRelativeDateToOtherTestHelper(
            String earlierDateString,
            String laterDateString,
            long expectedResult) throws Exception {

        final Date earlierDate = TEST_FORMAT.parse(earlierDateString);
        final Date laterDate = TEST_FORMAT.parse(laterDateString);

        Assert.assertEquals(expectedResult, DateUtility.minutesBetweenDates(earlierDate, laterDate));
    }

    @Test
    public void testOverlap() {

        long now = new Date().getTime();
        Date time1 = new Date(now);
        Date time2 = new Date(now+1);
        Date time3 = new Date(now+2);
        Date time4 = new Date(now+3);

        assertFalse(DateUtility.overlap(time1, time2, time3, time4));
        assertTrue(DateUtility.overlap(time1, time3, time2, time4));
        assertTrue(DateUtility.overlap(time1, time4, time2, time3));
        assertTrue(DateUtility.overlap(time2, time3, time1, time4));
        assertTrue(DateUtility.overlap(time2, time4, time1, time3));
        assertFalse(DateUtility.overlap(time3, time4, time1, time2));

        // cases where some times are equal
        // touch at mid-point
        assertFalse(DateUtility.overlap(time1, time2, time2, time3));
        assertFalse(DateUtility.overlap(time2, time3, time1, time2));
        // touch at right ends
        assertTrue(DateUtility.overlap(time1, time3, time2, time3));
        assertTrue(DateUtility.overlap(time2, time3, time1, time3));
        // touch at left-ends
        assertTrue(DateUtility.overlap(time1, time2, time1, time3));
        assertTrue(DateUtility.overlap(time1, time3, time1, time2));
        // touch at both ends
        assertTrue(DateUtility.overlap(time1, time2, time1, time2));

    }

    @Test
    public void testDayHrMinSecFormat() {
        assertNull(DateUtility.dayHrMinSecFormat(null));

        Date date = new Date();
        String expected = DateUtility.format(DateUtility.dateHourMinSec(), date);
        String actual = DateUtility.dayHrMinSecFormat(date);

        assertEquals(expected, actual);
    }

    @Test
    public void test_overnightsBetweenDates() {

        Date date327 = DateUtility.toDate(LocalDateTime.of(2017, 3, 27, 10, 10));
        Date date328 = DateUtility.toDate(LocalDateTime.of(2017, 3, 28, 0, 10));
        Date date329 = DateUtility.toDate(LocalDateTime.of(2017, 3, 29, 4, 10));

        assertEquals(0, DateUtility.overnightsBetweenDates(date327, date327));
        assertEquals(1, DateUtility.overnightsBetweenDates(date327, date328));
        assertEquals(2, DateUtility.overnightsBetweenDates(date327, date329));

    }
}
