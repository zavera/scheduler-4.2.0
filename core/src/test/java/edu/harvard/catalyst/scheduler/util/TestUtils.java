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

import com.google.gson.*;
import org.apache.log4j.Appender;
import org.apache.log4j.spi.LoggingEvent;
import org.junit.Assert;
import org.mockito.ArgumentCaptor;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.verify;

public class TestUtils {

    public static String OutpatientCRC = "Outpatient CRC";
    public static String OutpatientNonCRC = "Outpatient Non CRC";
    public static String OutpatientOffInstitution = "Outpatient Off Institution";
    public static String InpatientCRC = "Inpatient CRC";
    public static String InpatientNonCRC = "Inpatient Non CRC";
    public static String InpatientOffInstitution = "Inpatient Off Institution";

    public static <T> void assertNonNullAndEqual(T expected, T actual) {
        Assert.assertNotNull(expected);
        Assert.assertEquals("Wanted <" + expected + ">, not <" + actual + ">",
                expected, actual);
    }

    ///// Collection
    public static <T> boolean isNonNullNonEmpty(Collection<T> collection) {
        return collection != null && !collection.isEmpty();
    }
    
    public static <T> void assertNonNullNonEmpty(Collection<T> collection) {
        Assert.assertTrue("Wanted non-null, non-empty", isNonNullNonEmpty(collection));
    }
    
    public static <T> void assertNull(Collection<T> collection) {
        // whoa, looks like assertTrue evaluates the first arg even if the second one is true
        Assert.assertTrue("Wanted null, not size: " + (collection != null ? collection.size() : -1),
                collection == null);
    }

    public static <T> void assertNonNullAndHasThisMany(Collection<T> collection, int howMany) {
        Assert.assertTrue("Wanted non-null, size: " + howMany +
                    ", not " + (collection == null ? "null" : collection.size()),
                collection != null && collection.size() == howMany);
    }

    
    ///// Map
    public static <T, S> boolean isNonNullNonEmpty(Map<T, S> map) {
        return map != null && !map.isEmpty();
    }
    
    public static <T, S> void assertNonNullNonEmpty(Map<T, S> map) {
        Assert.assertTrue("Wanted non-null, non-empty", isNonNullNonEmpty(map));
    }
    
    public static <T> void assertNull(Map map) {
        Assert.assertTrue("Wanted null, not size: " + (map != null ? map.size() : -1),
                map == null);
    }
      
    public static <T> void assertNonNullAndHasThisMany(Map map, int howMany) {
        Assert.assertTrue("Wanted non-null, size: " + howMany +
                    ", not " + (map == null ? "null" : map.size()),
                    map != null && map.size() == howMany);
    }

    public static String spacesToSpace(String input) {
        String result = input.replaceAll("\\s+", " ");
        return result;
    }


    /**
     * Json comparison assertion. The inputs can Strings, JsonElement's, or POJO's.
     *
     * TODO-XH: has not been fully tested. In particular, need to verify that two equivalent
     * JSON strings in different orders will be seen as equal.
     *
     * Note: instead of creating a Gson object here, we pass it as a method argument. This is aimed
     * at avoiding differences in (de)serialization between the Gson object used in the tests that call
     * the following methods, and the Gson object used by the code under test.
     *
     * @param object1
     * @param object2
     */
    public static void assertEqualJson(Gson gson, String message, Object object1, Object object2) {

        if (message == null) message = "JSON objects/strings should be identical";

        JsonElement element1 = universalConvertToJsonElement(gson, object1);
        JsonElement element2 = universalConvertToJsonElement(gson, object2);

        assertEquals(message, element1, element2);
    }

    /**
     * Same as above but with simplified signature.
     */
    public static void assertEqualJson(Gson gson, Object object1, Object object2) {

        assertEqualJson(gson, null, object1, object2);
    }


    /**
     * Utility method to get various objects or stroings in the right shape to compare them
     * together as Json data
     *
     * @param object
     * @return
     */
    static public JsonElement universalConvertToJsonElement(Gson gson, Object object) {

        JsonParser parser = new JsonParser();
        JsonElement element;

        if (object.getClass() == String.class) {
            element = parser.parse((String) object);
        }
        else if (object.getClass() == JsonElement.class) {
            element = (JsonElement) object;
        }
        else {
            element = gson.toJsonTree(object);
        }

        return element;

    }
    public static void verifyLog(String phrase, int times, Appender mockAppender, ArgumentCaptor<LoggingEvent> captorLoggingEvent) {
        List<LoggingEvent> loggingEvents = getLogEvents(times, mockAppender, captorLoggingEvent);
        verifyEvents(phrase, times, loggingEvents);
    }

    public static List<LoggingEvent> getLogEvents(int times, Appender mockAppender, ArgumentCaptor<LoggingEvent> captorLoggingEvent) {
        verify(mockAppender, atLeast(times)).doAppend(captorLoggingEvent.capture());
        List<LoggingEvent> loggingEvents = captorLoggingEvent.getAllValues();

        return loggingEvents;
    }

    public static void verifyEvents(String phrase, int times, List<LoggingEvent> loggingEvents) {

        List<String> logMessages = loggingEvents.stream()
                .map(lm -> (String) lm.getMessage())
                .filter(s -> s.contains(phrase))
                .collect(Collectors.toList());

        assertTrue(logMessages.size() == times);
    }

}