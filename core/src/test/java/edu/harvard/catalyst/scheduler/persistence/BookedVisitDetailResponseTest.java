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
package edu.harvard.catalyst.scheduler.persistence;

import edu.harvard.catalyst.scheduler.dto.response.BookedVisitDetailResponse;
import org.junit.Test;

import static edu.harvard.catalyst.scheduler.util.DateUtility.dateTime;
import static edu.harvard.catalyst.scheduler.util.DateUtility.format;
import static org.junit.Assert.assertEquals;

public final class BookedVisitDetailResponseTest extends AppointmentTestDataProvider {

    @Test
    public void testBookedVisitDetailResponse() {

        setupAppointmentRelatedData();

        BookedVisitDetailResponse response = new BookedVisitDetailResponse(bookedVisit);

        Object obj = new Object();

        assertEquals(bookedVisit.getId(), response.getId());
        assertEquals(STUDY_NAME, response.getStudyName());
        assertEquals(VISIT_TEMPLATE_DURATION, response.getVisitDuration());
        assertEquals(scheduledStatus.getIsScheduled(), response.getIsScheduled());
        assertEquals(scheduledStatus.getIsCheckedIn(), response.getIsCheckedIn());
        assertEquals(scheduledStatus.getIsCheckedOut(), response.getIsCheckedOut());
        assertEquals(scheduledStatus.getIsCancelled(), response.getIsCancelled());
        assertEquals(scheduledStatus.getIsHold(), response.getIsHold());
        assertEquals(SCHEDULED_STATUS_NAME, response.getAppointmentStatus());
        assertEquals(format(dateTime(), START_DATE), response.getScheduledStartTime());
        assertEquals(format(dateTime(), END_DATE), response.getScheduledEndTime());
        assertEquals(format(dateTime(), CHECKIN_DATE), response.getCheckInDate());
        assertEquals(format(dateTime(), CHECKOUT_DATE), response.getCheckOutDate());
        assertEquals(format(dateTime(), CANCEL_DATE), response.getCancelDate());
        assertEquals(CHECKOUT_STATUS_REASON_NAME, response.getCheckOutReason());
        assertEquals(CANCEL_STATUS_REASON_NAME, response.getCancelReason());
        assertEquals(LOCAL_ID, response.getLocalId());
        assertEquals((SUBJECT_FIRST_NAME + " " + SUBJECT_LAST_NAME).toUpperCase(), response.getSubjectName());
        assertEquals(CATALYST_STUDY_ID, response.getCatID());
        assertEquals(MRN, response.getSubjectMRN());
        assertEquals(IRB, response.getIrb());
        assertEquals(GENDER_NAME, response.getGenderName());
        assertEquals(VISIT_TEMPLATE_NAME, response.getVisitName());
        assertEquals(SUBJECT_BIRTHDAY, response.getBirthDate());
        assertEquals(BOOKED_VISIT_COMMENT, response.getComment());
        assertEquals(visitType.getName(), response.getVisitTypeName());
    }
}
