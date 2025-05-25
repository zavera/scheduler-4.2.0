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
package edu.harvard.catalyst.scheduler.core;

import edu.harvard.catalyst.scheduler.entity.BookedVisitActivityLog;

import java.util.HashMap;
import java.util.Map;

/**
 * This class contains statics as well as a couple of functions for creating
 * activity log actions for booked visits.
 *
 * This class is an ENUM despite the fact that in certain cases the action
 * string is dynamic.
 *
 * The map is useful in only allowing string values that match one of the enum
 * values. This can't be done just via BookedVisitActivityLogStatics.valueOf()
 * because the values are not valid java identifiers (i.e they have spaces and
 * dashes). If we were using simple static strings, we could not verify
 * that values pulled from the database are valid without comparing to
 * each and every one of the static strings.
 */
public enum BookedVisitActivityLogStatics {
    BATCH_ENTRY ("Batch Entry"),
    CANCELLED ("Cancelled"),
    CHECKED_IN ("Checked-In"),
    CHECKED_OUT ("Checked-Out"),
    COMMENTED ("Commented"),
    OVERBOOKED ("Overbooked"),
    RESCHEDULED ("Rescheduled"),
    SCHEDULED ("Scheduled"),
    VIEWED_DETAILS_FROM_APPOINTMENT_LIST ("Viewed Appointment List Details");

    public static final String USER_DATA_NOT_AVAILABLE = "user data not available.";

    private final String logString;

    private static Map<String, BookedVisitActivityLogStatics> map = new HashMap<String, BookedVisitActivityLogStatics>();

    static {
        for (BookedVisitActivityLogStatics logEnum : BookedVisitActivityLogStatics.values()) {
            map.put(logEnum.logString, logEnum);
        }
    }

    public static BookedVisitActivityLogStatics valueByLogString(String logString) {
        BookedVisitActivityLogStatics value = map.get(logString);
        if (value == null) {
            SchedulerRuntimeException.logAndMaybeThrow("Invalid value for BookedVisitActivityLogStatics enum: " + logString, true, null);
        }
        return value;
    }

    BookedVisitActivityLogStatics(String logString) {
        this.logString = logString;
    }

    public String getLogString() {
        return this.logString;
    }

    public static String getActionForViewedResourcesListForAppointment(String status) {
        return "Clicked on the " + status + " appointment and viewed the resources list.";
    }

    public static String getActionForViewedHistoryForAppointment(String status) {
        return "Clicked on the " + status + " appointment and viewed the activity history.";
    }

}
