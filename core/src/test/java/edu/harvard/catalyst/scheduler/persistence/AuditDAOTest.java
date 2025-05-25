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

import edu.harvard.catalyst.scheduler.core.BookedVisitActivityLogStatics;
import edu.harvard.catalyst.scheduler.entity.*;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Created by xavier on 3/20/17.
 */
public class AuditDAOTest extends AppointmentTestDataProvider {

    @Autowired
    private AuditDAO auditDAO;

    @Test
    public void testGetActivityLogForBookedVisit() {

        setupAppointmentRelatedData();

        List<BookedVisitActivityLogStatics> actions = Arrays.asList(BookedVisitActivityLogStatics.values());
        List<BookedVisitActivityLogStatics> expectedActions = Arrays.asList(
                new BookedVisitActivityLogStatics[]{
                BookedVisitActivityLogStatics.BATCH_ENTRY,
                BookedVisitActivityLogStatics.CANCELLED,
                BookedVisitActivityLogStatics.CHECKED_IN,
                BookedVisitActivityLogStatics.CHECKED_OUT,
                BookedVisitActivityLogStatics.OVERBOOKED,
                BookedVisitActivityLogStatics.RESCHEDULED,
                BookedVisitActivityLogStatics.SCHEDULED
        });

        List<Date> expectedDates = new ArrayList<>();

        Date date = new Date();
        List<BookedVisitActivityLog> bookedVisitActivityLogList = new ArrayList<>();

        for (BookedVisitActivityLogStatics action : actions) {

            BookedVisitActivityLog bookedVisitActivityLog = new BookedVisitActivityLog();
            bookedVisitActivityLog.setActionPerformed(action.getLogString());
            bookedVisitActivityLog.setAppointmentOverrideReason(null);
            bookedVisitActivityLog.setBookedVisit(bookedVisit);
            date = new Date(date.getTime() + 1000000);
            if (expectedActions.contains(action)) {
                expectedDates.add(date);
            }
            bookedVisitActivityLog.setDate(date);
            bookedVisitActivityLog.setIpAddress("123.456.789.000");
            bookedVisitActivityLog.setPerformingUser(adminUser);
            this.save(bookedVisitActivityLog);
            bookedVisitActivityLogList.add(bookedVisitActivityLog);

        }

        // this one should not be returned because it has to do with a different visit
        BookedVisitActivityLog bookedVisitActivityLogOtherVisit = new BookedVisitActivityLog();
        bookedVisitActivityLogOtherVisit.setActionPerformed(BookedVisitActivityLogStatics.RESCHEDULED.getLogString());
        bookedVisitActivityLogOtherVisit.setAppointmentOverrideReason(null);
        bookedVisitActivityLogOtherVisit.setBookedVisit(bookedVisit2);
        date = new Date(date.getTime() + 1000000);
        bookedVisitActivityLogOtherVisit.setDate(date);
        bookedVisitActivityLogOtherVisit.setIpAddress("000.000.000.000");
        bookedVisitActivityLogOtherVisit.setPerformingUser(null);
        this.save(bookedVisitActivityLogOtherVisit);

        // this one should not be returned because it has an action not in the enum
        BookedVisitActivityLog bookedVisitActivityLogOtherAction = new BookedVisitActivityLog();
        bookedVisitActivityLogOtherAction.setActionPerformed("some other action");
        bookedVisitActivityLogOtherAction.setAppointmentOverrideReason(null);
        bookedVisitActivityLogOtherAction.setBookedVisit(bookedVisit2);
        date = new Date(date.getTime() + 1000000);
        bookedVisitActivityLogOtherAction.setDate(date);
        bookedVisitActivityLogOtherAction.setIpAddress("000.000.000.000");
        bookedVisitActivityLogOtherAction.setPerformingUser(null);
        this.save(bookedVisitActivityLogOtherAction);

        List<BookedVisitActivityLog> returnedBookedActivityList = auditDAO.getActivityLogForBookedVisit(bookedVisit);

        assertEquals(expectedActions.size(), returnedBookedActivityList.size());

        // Note: records are returned in reverse chronological order
        int numberOfReturnedBookedActivityList = returnedBookedActivityList.size();
        for ( int i = 0 ; i < numberOfReturnedBookedActivityList - 1; i++ ) {
            BookedVisitActivityLog returnedLogItem = returnedBookedActivityList.get(numberOfReturnedBookedActivityList-i-1);
            String action = returnedLogItem.getActionPerformed();
            String expectedAction = expectedActions.get(i).getLogString();
            Date dateReturned = returnedLogItem.getDate();
            Date expectedDate = expectedDates.get(i);
            assertEquals(expectedAction, action);
            assertEquals(expectedDate, dateReturned);
        }

    }

}
