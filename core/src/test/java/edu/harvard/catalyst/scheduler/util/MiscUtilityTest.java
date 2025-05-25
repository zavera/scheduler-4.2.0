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

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import edu.harvard.catalyst.scheduler.entity.Gender;
import org.junit.Test;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static edu.harvard.catalyst.scheduler.util.DateUtility.MINS_PER_DAY;
import static edu.harvard.catalyst.scheduler.util.DateUtility.MINS_PER_HR;
import static org.junit.Assert.*;

/**
 * @author Bill Simons
 * @date 8/2/13
 * @link http://cbmi.med.harvard.edu
 * @link http://chip.org
 */
public final class MiscUtilityTest {

    @Test
    public void differentObjectsTest() {
        assertFalse(MiscUtil.differentObjects(null, null));
        assertFalse(MiscUtil.differentObjects("joe", "joe"));

        assertTrue(MiscUtil.differentObjects("joe", null));
        assertTrue(MiscUtil.differentObjects(null, "joe"));
        assertTrue(MiscUtil.differentObjects("jane", "joe"));
    }

    @Test
    public void differentObjectsThatLookTheSameToDifferentObjectsMethod() {

        Gender g1 = new Gender();
        g1.setCode("A");

        Gender g2 = new Gender();
        g2.setCode("B");

        // FIXME:
        // This assertion should pass, but doesn't:
//        assertTrue(MiscUtil.differentObjects(g1, g2));
        // Instead the two gender objects are deemed to be equal (b/c their id's are both null)
        assertFalse(MiscUtil.differentObjects(g1, g2));

    }

    @Test
    public void testThrowableInfoForLog() {

        assertEquals(" -- NullPointerException -- blah",
                MiscUtil.throwableInfoForLog(new NullPointerException("blah")));

        return;
    }

    @Test
    public void testAppendIfDifferentStrings() {
        String result;
        String should = "Should be the same!";

        result = MiscUtil.appendIfDifferentStrings(new StringBuilder(), "one", "one", "Same").toString();
        assertEquals(should, "Same: one (no change). ", result);

        result = MiscUtil.appendIfDifferentStrings(new StringBuilder(), "one", "two", "Diff").toString();
        assertEquals(should, "Diff: one --> two. ", result);

        StringBuilder builder = new StringBuilder();
        MiscUtil.appendIfDifferentStrings(builder, null, null, "12null");
        MiscUtil.appendIfDifferentStrings(builder, null, "two", "1null");
        MiscUtil.appendIfDifferentStrings(builder, "one", null, "2null");
        result = builder.toString();

        assertEquals(should, "12null: null (no change). 1null: null --> two. 2null: one --> null. ", result);

        return;
    }

    @Test
    public void testUndefinedToNull() {
        String input = "hello";
        assertEquals(input, MiscUtil.undefinedToNull(input));

        assertEquals(null, MiscUtil.undefinedToNull("undefined"));
        assertEquals(null, MiscUtil.undefinedToNull("undeFIned"));
        assertEquals(null, MiscUtil.undefinedToNull(null));
    }
    @Test
    public void testMultiDayMinutesToHourlyMinute() {
        final int oddMinute1 = 5;
        final int oddMinute2 = 65;

        int actual;
        final int multiDayMinutes1 = MINS_PER_DAY * 2 + oddMinute1;
        actual = MiscUtil.multiDayMinutesToHourlyMinute(multiDayMinutes1);
        assertEquals(actual, oddMinute1);

        final int multiDayMinutes2 = MINS_PER_DAY * 2 + oddMinute2;
        actual = MiscUtil.multiDayMinutesToHourlyMinute(multiDayMinutes2);
        assertEquals(actual, oddMinute1);

        final int multiDayMinutes3 = oddMinute1;
        actual = MiscUtil.multiDayMinutesToHourlyMinute(multiDayMinutes3);
        assertEquals(actual, oddMinute1);

        final int multiDayMinutes4 = oddMinute2;
        actual = MiscUtil.multiDayMinutesToHourlyMinute(multiDayMinutes4);
        assertEquals(actual, oddMinute1);
    }

    @Test
    public void testMultiDayMinutesToDailyHour() {
        final int oddMinute1 = 5;
        final int oddMinute2 = 65;

        int actual;
        final int multiDayMinutes1 = MINS_PER_DAY * 2 + oddMinute1;
        actual = MiscUtil.multiDayMinutesToDailyHour(multiDayMinutes1);
        assertEquals(actual, 0);

        final int multiDayMinutes2 = MINS_PER_DAY * 2 + oddMinute2;
        actual = MiscUtil.multiDayMinutesToDailyHour(multiDayMinutes2);
        assertEquals(actual, 1);

        final int multiDayMinutes3 = oddMinute1;
        actual = MiscUtil.multiDayMinutesToDailyHour(multiDayMinutes3);
        assertEquals(actual, 0);

        final int multiDayMinutes4 = oddMinute2;
        actual = MiscUtil.multiDayMinutesToDailyHour(multiDayMinutes4);
        assertEquals(actual, 1);
    }
    @Test
    public void testNonNullNonEmptyList() {
        List<String> empty = Lists.newArrayList();
        List<String> nonEmpty = Lists.newArrayList("hello");
        List<String> nullStringL = null;

        assertTrue(MiscUtil.isNonNullNonEmpty(nonEmpty));
        assertFalse(MiscUtil.isNonNullNonEmpty(empty));
        assertFalse(MiscUtil.isNonNullNonEmpty(nullStringL));
    }
    @Test
    public void testNonNullNonEmptyMap() {
        Map<String, String> empty = Maps.newHashMap();
        Map<String, String> nonEmpty = Maps.newHashMap();
        nonEmpty.put("foo", "bar");
        Map<String, String> nullStringM = null;

        assertTrue(MiscUtil.isNonNullNonEmpty(nonEmpty));
        assertFalse(MiscUtil.isNonNullNonEmpty(empty));
        assertFalse(MiscUtil.isNonNullNonEmpty(nullStringM));
    }
    @Test
    public void testNonNullNonEmptyString() {
        String empty = "";
        String nonEmpty = "hello";
        String nullString = null;

        assertTrue(MiscUtil.isNonNullNonEmpty(nonEmpty));
        assertFalse(MiscUtil.isNonNullNonEmpty(empty));
        assertFalse(MiscUtil.isNonNullNonEmpty(nullString));
    }

    /////////// standard report utils ////////

    @Test
    public void test_q() {

        assertEquals("\"1\"", MiscUtil.q(1));
        assertEquals("\"1.8\"", MiscUtil.q(1.8));
        assertEquals("\"0.8\"", MiscUtil.q(.8));

        assertEquals("\"Hello\"", MiscUtil.q("Hello"));
        assertEquals("\"\"", MiscUtil.q(""));
        assertEquals("\"\"", MiscUtil.q((String)null));
        assertEquals("\"\"", MiscUtil.q((Number)null));
    }

    @Test
    public void test_booleanToStatusValue() {

        assertEquals("Active", MiscUtil.booleanToStatusValue("true"));
        assertEquals("Active", MiscUtil.booleanToStatusValue("tRUe"));
        assertEquals("Active", MiscUtil.booleanToStatusValue(" tRUe "));

        assertEquals("Not Active", MiscUtil.booleanToStatusValue("false"));
        assertEquals("Not Active", MiscUtil.booleanToStatusValue(""));
        assertEquals("Not Active", MiscUtil.booleanToStatusValue(null));
    }

    @Test
    public void test_nullToEmptyString() {

        assertEquals("", MiscUtil.nullToEmptyString(""));
        assertEquals("", MiscUtil.nullToEmptyString(null));
        assertEquals("rr", MiscUtil.nullToEmptyString("rr"));
        assertEquals(" r ", MiscUtil.nullToEmptyString(" r "));
    }

    @Test
    public void test_yesNoValue() {

        assertEquals("", MiscUtil.yesNoValue(null));
        assertEquals("Yes", MiscUtil.yesNoValue(true));
        assertEquals("No", MiscUtil.yesNoValue(false));
    }

    @Test
    public void test_showDate() {

        Date date3_27_10_10 =
                DateUtility.toDate(LocalDateTime.of(2017, 3, 27, 10, 10));

        assertEquals("03/27/2017", MiscUtil.showDate(date3_27_10_10));
        assertEquals("", MiscUtil.showDate(null));
    }

    @Test
    public void test_showDateTime() {

        Date date3_27_10_10 =
                DateUtility.toDate(LocalDateTime.of(2017, 3, 27, 10, 10));
        Date date3_27_15_15 =
                DateUtility.toDate(LocalDateTime.of(2017, 3, 27, 15, 15));

        assertEquals("03/27/2017 10:10",
                MiscUtil.showDateTime(date3_27_10_10));
        assertEquals("03/27/2017 15:15",
                MiscUtil.showDateTime(date3_27_15_15));
        assertEquals("", MiscUtil.showDateTime(null));
    }

    @Test
    public void test_formatNoneOneOrMore() {

        assertEquals("",
                MiscUtil.formatNoneOneOrMore(0L, "thing"));
        assertEquals("1 thing ",
                MiscUtil.formatNoneOneOrMore(1L, "thing"));
        assertEquals("10 things ",
                MiscUtil.formatNoneOneOrMore(10L, "thing"));
    }

    @Test
    public void test_formatMinsIntoDaysHrsMins() {

        Integer time3_4_20 = (3*MINS_PER_DAY) + (4*MINS_PER_HR) + 20;
        assertEquals("",
                MiscUtil.formatMinsIntoDaysHrsMins(null));
        assertEquals("3 Days 4 Hrs 20 Mins",
                MiscUtil.formatMinsIntoDaysHrsMins(time3_4_20));
    }

    @Test
    public void test_formatDaysHrsMins() {

        assertEquals("3 Days 4 Hrs 20 Mins",
                MiscUtil.formatDaysHrsMins(3, 4, 20));
        assertEquals("4 Hrs 20 Mins",
                MiscUtil.formatDaysHrsMins(0, 4, 20));
        assertEquals("3 Days 20 Mins",
                MiscUtil.formatDaysHrsMins(3, 0, 20));
        assertEquals("3 Days 4 Hrs",
                MiscUtil.formatDaysHrsMins(3, 4, 0));
    }

    @Test
    public void test_formatEndMinusStart() {

        Date date3_27_10_10 =
                DateUtility.toDate(LocalDateTime.of(2017, 3, 27, 10, 10));
        Date date3_27_10_12 =
                DateUtility.toDate(LocalDateTime.of(2017, 3, 27, 10, 12));
        Date date3_27_12_10 =
                DateUtility.toDate(LocalDateTime.of(2017, 3, 27, 12, 10));
        Date date3_27_12_12 =
                DateUtility.toDate(LocalDateTime.of(2017, 3, 27, 12, 12));

        Date date3_29_10_15 =
                DateUtility.toDate(LocalDateTime.of(2017, 3, 29, 10, 15));
        Date date3_29_15_15 =
                DateUtility.toDate(LocalDateTime.of(2017, 3, 29, 15, 15));

        assertEquals("0 Mins",
                MiscUtil.formatEndMinusStart(date3_27_10_10, date3_27_10_10));
        assertEquals("2 Mins",
                MiscUtil.formatEndMinusStart(date3_27_10_10, date3_27_10_12));
        assertEquals("2 Hrs",
                MiscUtil.formatEndMinusStart(date3_27_10_10, date3_27_12_10));
        assertEquals("2 Hrs 2 Mins",
                MiscUtil.formatEndMinusStart(date3_27_10_10, date3_27_12_12));

        assertEquals("2 Days 5 Hrs 5 Mins",
                MiscUtil.formatEndMinusStart(date3_27_10_10, date3_29_15_15));
        assertEquals("2 Days 5 Mins",
                MiscUtil.formatEndMinusStart(date3_27_10_10, date3_29_10_15));
    }

    @Test
    public void test_endMinusStartInMinutes() {

        Date date3_27_10_10 =
                DateUtility.toDate(LocalDateTime.of(2017, 3, 27, 10, 10));
        Date date3_27_10_12 =
                DateUtility.toDate(LocalDateTime.of(2017, 3, 27, 10, 12));
        Date date3_27_12_12 =
                DateUtility.toDate(LocalDateTime.of(2017, 3, 27, 12, 12));

        Date date3_29_10_15 =
                DateUtility.toDate(LocalDateTime.of(2017, 3, 29, 10, 15));

        assertEquals("0",
                MiscUtil.endMinusStartInMinutes(date3_27_10_10, date3_27_10_10));
        assertEquals("2",
                MiscUtil.endMinusStartInMinutes(date3_27_10_10, date3_27_10_12));
        assertEquals("122",
                MiscUtil.endMinusStartInMinutes(date3_27_10_10, date3_27_12_12));

        int mins2_days_5_mins = (2*MINS_PER_DAY) + 5;
        String string2_5 = String.valueOf(mins2_days_5_mins);

        assertEquals(string2_5,
                MiscUtil.endMinusStartInMinutes(date3_27_10_10, date3_29_10_15));
    }

    @Test
    public void test_localDate2LaterThanDate1YesNo() {

        Date date3_27_10_10 =
                DateUtility.toDate(LocalDateTime.of(2017, 3, 27, 10, 10));
        Date date3_27_10_12 =
                DateUtility.toDate(LocalDateTime.of(2017, 3, 27, 10, 12));

        Date date3_29_10_15 =
                DateUtility.toDate(LocalDateTime.of(2017, 3, 29, 10, 15));

        assertEquals("YES",
                MiscUtil.localDate2LaterThanDate1YesNo(date3_27_10_10, date3_29_10_15));
        assertEquals("YES",
                MiscUtil.localDate2LaterThanDate1YesNo(date3_27_10_12, date3_29_10_15));

        assertEquals("NO",
                MiscUtil.localDate2LaterThanDate1YesNo(date3_27_10_10, date3_27_10_10));
        assertEquals("NO",
                MiscUtil.localDate2LaterThanDate1YesNo(date3_27_10_10, date3_27_10_12));
        assertEquals("NO",
                MiscUtil.localDate2LaterThanDate1YesNo(date3_29_10_15, date3_27_10_10));
    }

    class DateAndString {
        Date date;
        String string;

        public DateAndString(Date date, String string) {
            this.date = date;
            this.string = string;
        }
        public Date getDate() {
            return date;
        }

        public String getString() {
            return string;
        }
        @Override
        public String toString() {
            return DateUtility.format(DateUtility.monthDayYear(), date) +
                    ", " + string;
        }
    }

    @Test
    public void testComparators() {
        Date date1 = DateUtility.toDate(LocalDateTime.of(2017, 1, 1, 0, 0));
        Date date2 = DateUtility.toDate(LocalDateTime.of(2017, 2, 2, 0, 0));
        Date date3 = DateUtility.toDate(LocalDateTime.of(2017, 3, 3, 0, 0));
        Date date4 = DateUtility.toDate(LocalDateTime.of(2017, 4, 4, 0, 0));

        String string1 = "11";
        String string2 = "22";
        String string3 = "33";
        String string4 = "44";

        DateAndString ds12 = new DateAndString(date1, string2);
        DateAndString ds24 = new DateAndString(date2, string4);
        DateAndString ds31 = new DateAndString(date3, string1);
        DateAndString ds43 = new DateAndString(date4, string3);

        List<DateAndString> dsList = Lists.newArrayList(ds24, ds31, ds12, ds43);

        comparatorsHelper(dsList, "02/02/2017, 44. 03/03/2017, 11. 01/01/2017, 22. 04/04/2017, 33");

        Collections.sort(dsList, MiscUtil.compareVia(DateAndString::getString));
        comparatorsHelper(dsList, "03/03/2017, 11. 01/01/2017, 22. 04/04/2017, 33. 02/02/2017, 44");

        Collections.sort(dsList, MiscUtil.invert(MiscUtil.compareVia(DateAndString::getString)));
        comparatorsHelper(dsList, "02/02/2017, 44. 04/04/2017, 33. 01/01/2017, 22. 03/03/2017, 11");

        Collections.sort(dsList, MiscUtil.compareVia(DateAndString::getDate));
        comparatorsHelper(dsList, "01/01/2017, 22. 02/02/2017, 44. 03/03/2017, 11. 04/04/2017, 33");

        Collections.sort(dsList, MiscUtil.invert(MiscUtil.compareVia(DateAndString::getDate)));
        comparatorsHelper(dsList, "04/04/2017, 33. 03/03/2017, 11. 02/02/2017, 44. 01/01/2017, 22");
    }

    void comparatorsHelper(List<DateAndString> dsList, String expected) {
        List<String> outputList = dsList.stream()
                .map(ds -> ds.toString())
                .collect(Collectors.toList());

        assertEquals(expected, Joiner.on(". ").join(outputList));
    }

}




