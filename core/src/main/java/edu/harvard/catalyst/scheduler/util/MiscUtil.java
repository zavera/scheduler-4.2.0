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


import com.google.common.base.Strings;
import edu.harvard.catalyst.scheduler.dto.SearchDTO;
import edu.harvard.catalyst.scheduler.dto.response.CalendarVisitsResponse;
import edu.harvard.catalyst.scheduler.entity.InstitutionRoleType;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDate;
import java.util.*;
import java.util.function.Function;

import static edu.harvard.catalyst.scheduler.util.DateUtility.*;
import static edu.harvard.catalyst.scheduler.util.DateUtility.MILLISECS_PER_MIN;

/**
 * Created with IntelliJ IDEA.
 * User: carl
 * Date: 6/16/16
 * Time: 9:01 AM
 */
public class MiscUtil {
    static public final int HASH_MODULUS = 200;

    public static int negativeIsInfinity(int candidate) {
        return (candidate < 0 ? Integer.MAX_VALUE : candidate);
    }

    public static boolean differentObjects(Object one, Object two) {
        boolean result = true;

        if (one == null && two == null) {
            result = false;
        } else if (one != null && one.equals(two)) {
            result = false;
        } else if (two != null && two.equals(one)) {
            result = false;
        }
        return result;
    }

    public static boolean differentStringsIgnoreCase(String one, String two) {
        if (one != null) {
            one = one.toUpperCase();
        }
        if (two != null) {
            two = two.toUpperCase();
        }
        return differentObjects(one, two);
    }

    public static final <T, F extends Comparable<F>> Comparator<T> compareVia(
            final Function<T, F> fieldGetter) {

        return (o1, o2) -> {
            final F field1 = fieldGetter.apply(o1);
            final F field2 = fieldGetter.apply(o2);

            if (field1 == null && field2 == null)
                return 0;
            if (field1 == null)
                return -1;
            if (field2 == null)
                return 1;

            return field1.compareTo(field2);
        };
    }

    public static final <T> Comparator<T> invert(final Comparator<T> c) {
        return (o1, o2) -> c.compare(o2, o1);
    }

    public static String throwableInfoForLog(Throwable t) {
        String result = " -- " + t.getClass().getSimpleName() + " -- " + t.getMessage();
        return result;
    }

    public static String throwableToStackTraceString(Throwable t) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        t.printStackTrace(pw);
        return sw.toString(); // stack trace as a string
    }

    public static String applyCodeToXX(String input, String code) {
        String result = input;
        if (code != null) {
            result = result.replaceAll("XX", code);
        }
        return result;
    }

    public static boolean isNonNullNonEmpty(final Collection<?> collection) {
        return collection != null && !collection.isEmpty();
    }

    public static boolean isNonNullNonEmpty(final Map<?, ?> map) {
        return map != null && !map.isEmpty();
    }

    public static boolean isNonNullNonEmpty(final String string) {
        return !Strings.isNullOrEmpty(string);
    }

    public static boolean isNullOrEmpty(final List<?> list) {
        return !isNonNullNonEmpty(list);
    }

    public static boolean isNullOrEmpty(final String string) {
        return Strings.isNullOrEmpty(string);
    }

    public static StringBuilder appendIfDifferentStrings(StringBuilder builder, String oldOne, String newOne, String label) {
        builder.append(label).append(": ");

        if (MiscUtil.differentStringsIgnoreCase(oldOne, newOne)) {
            builder.append(oldOne).append(" --> ").append(newOne);
        } else {
            builder.append(oldOne).append(" (no change)");
        }
        builder.append(". ");

        return builder;
    }

    public static String undefinedToNull(final String input) {
        String result = ("undefined".equalsIgnoreCase(input))
                ? null
                : input;

        return result;
    }

    public static String dbFormatPhoneNumber(String input, boolean keepUnderscores, boolean includeParentheses) {

        String result = null;
        if (input != null) {
            String pattern1;
            String pattern2;

            if (keepUnderscores) {
                pattern1 = "[^\\d^_]";
                pattern2 = "([\\d_]{3})([\\d_]{3})([\\d_]{4})";

            } else {
                pattern1 = "[^\\d]";
                pattern2 = "(\\d{3})(\\d{3})(\\d{4})";
            }

            String format;
            if (includeParentheses) {
                format = "($1) $2-$3";
            } else {
                format = "$1-$2-$3";
            }

            result = input.replaceAll(pattern1, ""); // leave only digits and underscore characters
            result = result.replaceAll(pattern2, format);

        }

        return result;

    }


    /**
     * IMPORTANT: this method has side-effects on its SearchItem parameter
     *
     * @param searchItem
     */
    public static SearchDTO.SearchItem preparePhoneNumberSearchItems(SearchDTO.SearchItem searchItem) {

        SearchDTO.SearchType searchType = searchItem.getSearchType() != null ? searchItem.getSearchType() : SearchDTO.SearchType.ANY;
        // underscores are used in LIKE where clauses to represent a single occurrence of any character
        // therefore we don't want them for an exact match
        boolean keepUnderscores = searchType == SearchDTO.SearchType.ANY;
        String key = searchItem.getKey();
        String originalValue = searchItem.getValue();

        String valueWithParentheses = MiscUtil.dbFormatPhoneNumber(originalValue, keepUnderscores, true);
        String valueWithoutParentheses = MiscUtil.dbFormatPhoneNumber(originalValue, keepUnderscores, false);

        // first search item with value without parentheses
        SearchDTO.SearchItem orSearchItem1 = new SearchDTO.SearchItem();
        orSearchItem1.setKey(key);
        orSearchItem1.setValue(valueWithoutParentheses);
        orSearchItem1.setSearchType(searchType);

        // second SearchItem for the value with parentheses
        SearchDTO.SearchItem orSearchItem2 = new SearchDTO.SearchItem();
        orSearchItem2.setKey(key);
        orSearchItem2.setValue(valueWithParentheses);
        orSearchItem2.setSearchType(searchType);

        // clean up original search item
        List<SearchDTO.SearchItem> orSearchItemList = new ArrayList<>();
        orSearchItemList.add(orSearchItem1);
        orSearchItemList.add(orSearchItem2);
        searchItem.setOrSearchItems(orSearchItemList);
        searchItem.setKey(null);
        searchItem.setValue(null);

        // fluent interface useful for testing: return input object
        return searchItem;

    }

    public static final <T> Optional<T> nullToEmpty(final Optional<T> o) {
        return (o != null) ? o : Optional.empty();
    }

    public static int moduloMinsPerHour(final int totalMinutes) {
        return multiDayMinutesToHourlyMinute(totalMinutes);
    }

    public static int divideByMinsPerHour(final int totalMinutes) {
        return totalMinutes / MINS_PER_HR;
    }

    public static int divideByMinsPerDay(final int totalMinutes) {
        return totalMinutes / MINS_PER_DAY;
    }

    public static int multiDayMinutesToHourlyMinute(final int slotOffsetEndMinute) {
        return slotOffsetEndMinute % MINS_PER_HR;
    }

    public static int multiDayMinutesToDailyHour(final int slotOffsetStartMinute) {
        return slotOffsetStartMinute % MINS_PER_DAY / MINS_PER_HR;
    }

    //////////////////////////////// below are useful for standard report

    public static String localDate2LaterThanDate1YesNo(Date date1, Date date2) {
        LocalDate ldate1 = dateToLocalDate(date1);
        LocalDate ldate2 = dateToLocalDate(date2);

        String result = ldate2.isAfter(ldate1) ? "YES" : "NO";
        return result;
    }

    public static String endMinusStartInMinutes(Date start, Date end) {
        if (start == null || end == null) {
            return "";
        }

        long millisDiff = end.getTime() - start.getTime();

        long minutesDiff = millisDiff / MILLISECS_PER_MIN;

        return String.valueOf(minutesDiff);
    }

    public static String formatEndMinusStart(Date start, Date end) {
        if (start == null || end == null) {
            return "";
        }

        long millisDiff = end.getTime() - start.getTime();

        if (millisDiff < 0) { // rainy edge case
            return "";
        }

        long minutesDiff = millisDiff / MILLISECS_PER_MIN;
        long daysDiff = minutesDiff / MINS_PER_DAY;

        int remainingMinsInDay = (int) (minutesDiff % MINS_PER_DAY);
        int remainingHoursInDay = remainingMinsInDay / MINS_PER_HR;
        int remainingMinsInHour = remainingMinsInDay % MINS_PER_HR;

        String result = formatDaysHrsMins(
                daysDiff, remainingHoursInDay, remainingMinsInHour);

        return result;
    }

    public static String formatDaysHrsMins(long days, int hours, int minutes) {
        String result = formatNoneOneOrMore(days, "Day") +
                formatNoneOneOrMore(hours, "Hr") +
                formatNoneOneOrMore(minutes, "Min");

        result = result.trim();

        if (result.isEmpty()) {
            result = "0 Mins";
        }
        return result;
    }

    public static String formatMinsIntoDaysHrsMins(Integer timeInMinutes) {
        if (timeInMinutes != null) {
            int timeDiffInDays = timeInMinutes / MINS_PER_DAY;
            int remainingMinutesInDay = timeInMinutes % MINS_PER_DAY;
            int remainingHoursInDay = remainingMinutesInDay / 60;
            int remainingMinutesInHour = remainingMinutesInDay % 60;
            return formatDaysHrsMins(
                    timeDiffInDays, remainingHoursInDay, remainingMinutesInHour);
        } else {
            return "";
        }
    }

    static String formatNoneOneOrMore(long value, String singular) {
        String result;

        if (value < 0L) { // millis overflow
            result = "Many" + " " + singular + "s ";
        } else if (value == 0L) {
            result = "";
        } else if (value == 1L) {
            result = String.valueOf(value) + " " + singular + " ";
        } else {
            result = String.valueOf(value) + " " + singular + "s ";
        }

        return result;
    }

    public static String showDateTime(Date raw) {
        String result = DateUtility.format(
                DateUtility.date24HTime(), raw);
        return nullToEmptyString(result);
    }

    public static String showDate(Date raw) {
        String result = DateUtility.format(
                DateUtility.monthDayYear(), raw);
        return nullToEmptyString(result);
    }

    public static String yesNoValue(Boolean value) {
        String result;

        if (value == null) {
            result = "";
        } else if (value) {
            result = "Yes";
        } else {
            result = "No";
        }
        return result;
    }

    public static String nullToEmptyString(String raw) {

        return raw != null ? raw : "";
    }

    public static String booleanToStatusValue(String raw) {
        raw = nullToEmptyString(raw);

        if (raw.trim().equalsIgnoreCase("true")) {
            return "Active";
        } else {
            return "Not Active";
        }
    }

    public static String fullName(String first, String middle, String last) {
        String result =
                nullToEmptyString(first) + " " +
                        nullToEmptyString(middle) + " " +
                        nullToEmptyString(last);
        result = result.replaceAll("\\s+", " ");
        return result.trim();
    }

    public static String q(String raw) {
        raw = nullToEmptyString(raw);
        raw = raw.replaceAll(",", " ");
        raw = raw.replaceAll("\"", "");

        return "\"" + raw + "\"";
    }

    public static String q(Number raw) {
        String result = "";

        if (raw != null) {
            result = String.valueOf(raw);
        }

        return "\"" + result + "\"";
    }

    // Check if an appointment can be scheduled based on the schedulingRestriction property and the given start date
    public static boolean canScheduleAppointment(long epochStartDate, InstitutionRoleType role) {
        if (role == InstitutionRoleType.ROLE_GENERAL_VIEW || role == InstitutionRoleType.ROLE_FRONT_DESK ||
                role == InstitutionRoleType.ROLE_STUDY_STAFF) {
            int restriction = SchedulingRestriction.getInstance().getSchedulingRestriction();
            if (restriction == 0) return true;
            Date startDate = new Date(epochStartDate);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(new Date());
            calendar.add(Calendar.DATE, restriction-1);
            return calendar.getTime().compareTo(startDate) < 0;
        }
        return true;
    }

    public static String formatSchedulingRestrictionErrorMessage() {
        Integer restriction = SchedulingRestriction.getInstance().getSchedulingRestriction();
        String numberOfDays;
        if (restriction == 1) {
            numberOfDays = "1 day";
        }
        else {
            numberOfDays = restriction.toString() + " days";
        }
        return "You are not able to schedule appointments within " + numberOfDays + " of the start time. Please " +
                "contact your CRC administrator for booking assistance.";
    }
}
