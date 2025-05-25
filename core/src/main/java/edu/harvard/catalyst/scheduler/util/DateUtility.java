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

import edu.harvard.catalyst.scheduler.core.SchedulerRuntimeException;
import edu.harvard.catalyst.scheduler.core.Statics;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.*;
import java.util.*;

import static edu.harvard.catalyst.scheduler.util.MiscUtil.isNonNullNonEmpty;
import static java.time.temporal.ChronoUnit.DAYS;


public final class DateUtility {
    public static final int MINS_PER_HR = 60;
    public static final int MINS_PER_DAY = 1440;
    public static final int MINUTES_PER_PERIOD = 15;
    public static final int PERIODS_PER_DAY = MINS_PER_DAY / MINUTES_PER_PERIOD;
    public static final long MILLISECS_PER_MIN = 60000L;
    private static final int TWO_DIGIT_TIME = 10;
    public static Calendar TEMPLATE_RESOURCE_CALENDAR_ORIGIN;
    public static LocalDate TEMPLATE_RESOURCE_LOCAL_DATE_ORIGIN;
    public static LocalDateTime TEMPLATE_RESOURCE_LOCAL_DATE_TIME_ORIGIN;
    public static Date TEMPLATE_RESOURCE_DATE_ORIGIN;

    static {
        TEMPLATE_RESOURCE_CALENDAR_ORIGIN = Calendar.getInstance();
        TEMPLATE_RESOURCE_CALENDAR_ORIGIN.set(Calendar.YEAR, 2012);
        TEMPLATE_RESOURCE_CALENDAR_ORIGIN.set(Calendar.MONTH, 1);
        TEMPLATE_RESOURCE_CALENDAR_ORIGIN.set(Calendar.DAY_OF_MONTH, 1);
        TEMPLATE_RESOURCE_CALENDAR_ORIGIN.set(Calendar.HOUR_OF_DAY, 0);
        TEMPLATE_RESOURCE_CALENDAR_ORIGIN.set(Calendar.MINUTE, 0);
        TEMPLATE_RESOURCE_CALENDAR_ORIGIN.set(Calendar.SECOND, 0);
        TEMPLATE_RESOURCE_CALENDAR_ORIGIN.set(Calendar.MILLISECOND, 0);

        TEMPLATE_RESOURCE_LOCAL_DATE_ORIGIN = DateUtility.instantToLocalDate(TEMPLATE_RESOURCE_CALENDAR_ORIGIN.toInstant());
        TEMPLATE_RESOURCE_DATE_ORIGIN = TEMPLATE_RESOURCE_CALENDAR_ORIGIN.getTime();
        TEMPLATE_RESOURCE_LOCAL_DATE_TIME_ORIGIN = TEMPLATE_RESOURCE_LOCAL_DATE_ORIGIN.atStartOfDay();
    }

    private final DateFormat monthDayYear = new SimpleDateFormat("MM/dd/yyyy");
    private final DateFormat dateHrMinSec = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private final DateFormat ie8 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
    private final DateFormat gantt = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
    private final DateFormat otherBrowser = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
    private final DateFormat subjectDate = new SimpleDateFormat("EEE MMM dd yyyy HH:mm:ss");
    private final DateFormat fullCalendar = new SimpleDateFormat("EEE MMM dd yyyy HH:mm:ss Z (zzzz)");
    private final DateFormat yearMonthDay = new SimpleDateFormat("yyyy-MM-dd");
    private final DateFormat dateHrMin = new SimpleDateFormat("yyyy-MM-dd HH:mm");
    private final DateFormat dayMonthYear = new SimpleDateFormat("dd-MM-yyyy");
    private final DateFormat dateTime = new SimpleDateFormat("MM/dd/yyyy hh:mm aa");
    private final DateFormat date24HTime = new SimpleDateFormat("MM/dd/yyyy HH:mm");
    private final DateFormat hrMin = new SimpleDateFormat("HH:mm");
    private final DateFormat clockHrMin = new SimpleDateFormat("hh:mm aa");
    private final DateFormat hr = new SimpleDateFormat("HH");
    private final DateFormat min = new SimpleDateFormat("mm");
    private final DateFormat dayMonthDayYear = new SimpleDateFormat("EEE MMM dd yyyy HH:mm:ss");


    public static final DateFormat monthDayYear() {
        return getDateUtility().monthDayYear;
    }

    public static final DateFormat dateHourMinSec() {
        return getDateUtility().dateHrMinSec;
    }

    public static final DateFormat ie8() {
        return getDateUtility().ie8;
    }

    public static final DateFormat otherBrowser() {
        return getDateUtility().otherBrowser;
    }

    public static final DateFormat gantt() {
        return getDateUtility().gantt;
    }

    // TODO-XH : should we call this method subjectDateFormat() ?
    public static final DateFormat subjectDate() {
        return getDateUtility().subjectDate;
    }

    public static final DateFormat fullCalendar() {
        return getDateUtility().fullCalendar;
    }

    public static final DateFormat yearMonthDay() {
        return getDateUtility().yearMonthDay;
    }

    public static final DateFormat dateHourMin() {
        return getDateUtility().dateHrMin;
    }

    public static final DateFormat dateMonthYear() {
        return getDateUtility().dayMonthYear;
    }

    public static final DateFormat dateMonthDayYear() {
        return getDateUtility().dayMonthDayYear;
    }

    public static final DateFormat dateTime() {
        return getDateUtility().dateTime;
    }

    public static final DateFormat date24HTime() {
        return getDateUtility().date24HTime;
    }

    public static final DateFormat hourMin() {
        return getDateUtility().hrMin;
    }

    public static final DateFormat hour() {
        return getDateUtility().hr;
    }

    public static final DateFormat min() {
        return getDateUtility().min;
    }

    private static final int MILLISEC_IN_A_DAY = 1000 * 60 * 60 * 24;

    private DateUtility() {
        //prevent instantiation for static helper class
    }

    //NB: Why a ThreadLocal?
    private static final ThreadLocal<DateUtility> dateUtilityThreadLocal = new ThreadLocal<>();

    static DateUtility getDateUtility() {
        DateUtility dateUtility = dateUtilityThreadLocal.get();
        
        if(dateUtility == null) {
            dateUtility = new DateUtility();
            dateUtilityThreadLocal.set(dateUtility);
        }
        
        return dateUtility;
    }

    public static String format(final DateFormat format, final Date date) {
        String value = null;
        if(date != null) {
            value = format.format(date);
        }
        return value;
    }

    public static Date parse(final DateFormat format, final String value) {
        if (value.isEmpty()) {
            return null;
        }
        Date date = null;
        try {
            date = format.parse(value);
        } catch(final java.text.ParseException pe) {
            SchedulerRuntimeException.logAndThrow("Bad format '" + format + "' or value '" + value + "'", pe);
        }
        return date;
    }

    public static Date parseLong(final String value) {
        Date result = null;

        if(isNonNullNonEmpty(value)) {
            final Date date = new Date();
            final Long dateLong = Long.parseLong(value);
            date.setTime(dateLong);
            result = date;
        }

        return result;
    }

    public static Date nextDay(final Date date) {
        final Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        clearTime(cal);
        cal.add(Calendar.DATE, 1);
        return cal.getTime();
    }

    private static void clearTime(final Calendar cal) {
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
    }

    public static Date startOfDay(final Calendar cal) {
        clearTime(cal);
        return cal.getTime();
    }

    /**
     * Calculates the difference in day between date1 and date2 IMP NOTE -
     * Inclusive of date1 and date2
     */
    public static int compareDateDifference(final Date date1, final Date date2) {
        final Calendar cal1 = Calendar.getInstance();
        final Calendar cal2 = Calendar.getInstance();
        cal1.clear();
        cal2.clear();
        cal1.setTime(date1);
        cal2.setTime(date2);
        final long results = (cal2.getTimeInMillis() - cal1.getTimeInMillis()) / MILLISEC_IN_A_DAY;
        final int result = (int) results + 1;
        if(result < 0) {
            return -(result + -1);
        }
        return result;
    }

    public static int day2minusDay1(final Date date1, final Date date2) {
        final Calendar cal1 = Calendar.getInstance();
        final Calendar cal2 = Calendar.getInstance();
        cal1.setTime(date1);
        cal2.setTime(date2);
        clearTime(cal1);
        clearTime(cal2);
        final long deltaMillis = cal2.getTimeInMillis() - cal1.getTimeInMillis();
        return (int) (deltaMillis / MILLISEC_IN_A_DAY);
    }

    public static int deltaMinutesBetweenDate1Date2(final Date date1, final Date date2) {
        final Calendar cal1 = Calendar.getInstance();
        cal1.setTime(date1);
        final int offset = cal1.get(Calendar.DST_OFFSET);
        final Calendar cal2 = Calendar.getInstance();
        cal2.setTime(date2);
        cal2.set(Calendar.DST_OFFSET, offset);
        final long deltaMillis = cal2.getTimeInMillis() - cal1.getTimeInMillis();
        return (int) (deltaMillis / MILLISECS_PER_MIN);
    }

    public static Date adjustDateToHMS(final Date date, int hour, int minute, int second) {
        final TimeZone tz = TimeZone.getDefault();
        final Calendar calendar = Calendar.getInstance(tz);
        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, second);
        return calendar.getTime();
    }
    public static Date adjustDateToEndOfDay(final Date date) {
        return adjustDateToHMS(date, 23, 59, 59);
    }

    public static Date adjustDateToStartOfDay(final Date date) {
        return adjustDateToHMS(date, 0, 0, 0);
    }

    public static int convertMillisecondsToMinutes(final long milliseconds) {
        return Math.round(milliseconds / 1000 / 60);
    }

    public static int convertMillisToMinutes(final long milliseconds) {
        return (int) (milliseconds / 1000 / 60);
    }

    public static int getDayOfTheWeek(final Date startDate) {
        final Calendar cal = Calendar.getInstance();
        cal.clear();
        cal.setTime(startDate);
        return cal.get(Calendar.DAY_OF_WEEK);
    }

    public static List<Date> dateInterval(final Date initial, final Date endDate) {
        final List<Date> dates = new ArrayList<Date>();

        Date startDate = adjustDateToStartOfDay(initial);

        while(isStartDateBeforeEndDate(startDate, endDate)) {
            dates.add(startDate);
            startDate = incrementDay(startDate);
        }
        return dates;
    }

    public static Date incrementDay(final Date date) {
        final Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.DATE, 1);
        return cal.getTime();
    }

    private static boolean isStartDateBeforeEndDate(final Date startDate, final Date endDate) {
        return startDate.getTime() <= endDate.getTime();
    }

    public static String retrieveDayOfWeek(final int day) {
        final String days;
        
        if(day == 1) {
            days = Statics.SUNDAY;
        }
        else if(day == 2) {
            days = Statics.MONDAY;
        }
        else if(day == 3) {
            days = Statics.TUESDAY;
        }
        else if(day == 4) {
            days = Statics.WEDNESDAY;
        }
        else if(day == 5) {
            days = Statics.THURSDAY;
        }
        else if(day == 6) {
            days = Statics.FRIDAY;
        }
        else if(day == 7) {
            days = Statics.SATURDAY;
        } else {
            days = null;
        }
        return days;
    }

    public static Date oldZoneDeltaDate(final Date startDate, final int minutes) {
        return new Date(startDate.getTime() + minutes * MILLISECS_PER_MIN);
    }

    public static LocalDateTime oldZoneDeltaDate8(final LocalDateTime startDate, final int minutes) {
        return startDate.plusMinutes(minutes);
    }
    
    public static LocalDateTime dateToLocalDateTime(final Date date) {
        return LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
    }

    public static LocalDate dateToLocalDate(final Date date) {
        return instantToLocalDate(date.toInstant());
    }

    public static int overnightsBetweenDates(final Date start, final Date end) {
        LocalDate startLDate = dateToLocalDate(start);
        LocalDate endLDate = dateToLocalDate(end);

        int result = (int)DAYS.between(startLDate, endLDate);
        return result;
    }

    public static LocalDate instantToLocalDate(final Instant instant) {
        return instant.atZone(ZoneId.systemDefault()).toLocalDate();
    }

    public static int minutesIntoDay(final int minutes) {
        return minutes % MINS_PER_DAY;
    }

    public static Date minutesFromOriginToDate(long minutes) {
        return toDate(TEMPLATE_RESOURCE_LOCAL_DATE_TIME_ORIGIN.plusMinutes(minutes));
    }

    public static Date originDatePlusMinutes(long minutes) {
        return Date.from(TEMPLATE_RESOURCE_DATE_ORIGIN.toInstant().plusSeconds(minutes * 60));
    }

    public static Date toDate(final LocalDateTime ldt) {
        if (ldt == null) {
            return null;
        }

        return Date.from(ldt.atZone(ZoneOffset.systemDefault()).toInstant());
    }

    public static Date toDate(final LocalDate ld) {
        return Date.from(ld.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    public static boolean earlierAndLaterAreSameLocalDay(int earlierMinutes, int laterMinutes) {

        int earlierDay = earlierMinutes / MINS_PER_DAY;
        int laterDay = laterMinutes / MINS_PER_DAY;

        return earlierDay == laterDay;
    }

    public static boolean minutesIntervalOverlapsDaysOffset(int candidateStart,
                                                            int candidateEnd,
                                                            int offsetInDays) {
        int viewStart = (offsetInDays - 1) * MINS_PER_DAY;
        int viewEnd = viewStart + MINS_PER_DAY;

        boolean result = minutesIntervalsOverlap(candidateStart, candidateEnd, viewStart, viewEnd);

        return result;
    }

    public static boolean minutesIntervalsOverlap(int resourceStart, int resourceEnd,
                                                  int viewStart, int viewEnd) {
        return  (resourceStart < viewEnd && resourceEnd > viewEnd)
                || (resourceStart < viewStart && resourceEnd > viewStart)
                || (resourceStart >= viewStart && resourceEnd <= viewEnd)
                || (resourceEnd == viewStart)
        ;
    }

    public static boolean overlap(Date startTime, Date endTime, Date otherStartTime, Date otherEndTime) {

        if (endTime.before(startTime)) SchedulerRuntimeException.logAndThrow("End time is before start time");
        if (otherEndTime.before(otherStartTime)) SchedulerRuntimeException.logAndThrow("End time is before start time");

        return (
                !startTime.before(otherStartTime) && startTime.before(otherEndTime) ||
                !startTime.after(otherStartTime) && endTime.after(otherStartTime)
        );

    }

    public static int dateToDayNumber(Date date) {
        final LocalDateTime localDateTime = DateUtility.dateToLocalDate(date).atStartOfDay();

        int result = (int) (1 + Duration.between(TEMPLATE_RESOURCE_LOCAL_DATE_TIME_ORIGIN, localDateTime).toDays());
        return result;
    }

    public static int minutesToDayNumber(int minutes) {
        return (1 + minutes / MINS_PER_DAY);
    }

//    public static LocalDate dayNumberToLocalDate(int dayNumber) {
//        return TEMPLATE_RESOURCE_LOCAL_DATE_ORIGIN.plusDays(dayNumber - 1);
//    }

    public static int convertMinutesToStartDayZeroOffset(final int time) {
        return time / MINS_PER_DAY;
    }

    public static int convertTotalMinutesToMinutesInHour(final int time) {
        return time % 60;
    }

    public static int convertMinutesToHourInDay(final int time) {
        return time / 60 % 24;
    }

    public static String padTime(final int timeUnit) {
        return timeUnit < TWO_DIGIT_TIME ? "0" + timeUnit : "" + timeUnit;
    }

    public static int minutesSinceOrigin(Date date) {
        return minutesBetweenDates(TEMPLATE_RESOURCE_DATE_ORIGIN, date);
    }

    public static int minutesBetweenDates(Date earlier, Date later) {

        Instant earlierInstant = Instant.from(Instant.ofEpochMilli(earlier.getTime()));
        Instant laterInstant = Instant.from(Instant.ofEpochMilli(later.getTime()));
        
        long duration = Duration.between(earlierInstant, laterInstant).toMinutes();

        if (duration > (long)Integer.MAX_VALUE) {
            SchedulerRuntimeException.logAndThrow("int has overflowed: too many minutes between: "
                    + earlier + " and " + later);
        }

        return (int) duration;
    }

    public static Timestamp formatFilterByDateTime(final String filterString) {
        final String OLD_FORMAT = "MM/dd/yy HH:mm";
        final String NEW_FORMAT = "yyyy-MM-dd HH:mm:ss.S";

        final SimpleDateFormat sdf = new SimpleDateFormat(OLD_FORMAT);
        Date d;
        try {
            d = sdf.parse(filterString);
        } catch (final ParseException e) {
            return null;
        }
        sdf.applyPattern(NEW_FORMAT);
        final String newDateString = sdf.format(d);

        final Timestamp timestamp = Timestamp.valueOf(newDateString);
        return timestamp;
    }

    public static String dayHrMinSecFormat(final Date date) {
        String result;

        if (date == null) {
            result = null;
        }
        else {
            result = format(dateHourMinSec(), date);
        }

        return result;
    }

    public static Date oneMillisecondBeforeNow() {

        return new Date((new Date()).getTime() - 1);

    }

}
