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
package edu.harvard.catalyst.scheduler.entity;

import edu.harvard.catalyst.scheduler.core.BookedVisitActivityLogStatics;
import edu.harvard.catalyst.scheduler.core.SchedulerRuntimeException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.junit.Assert.assertEquals;
import static org.springframework.test.util.AssertionErrors.fail;

/**
 * Created by xavier on 3/27/17.
 */
public class BookedVisitActivityLogStaticsTest {

    @Test
    public void testValueByLogString() {

        try {
            BookedVisitActivityLogStatics value = BookedVisitActivityLogStatics.valueByLogString(null);
            fail("An exception should have been thrown");
        }
        catch (SchedulerRuntimeException e) { }

        String ERRONEOUS_ACTION_NAME = "some string that is not a value of the enum";
        try {
            BookedVisitActivityLogStatics value = BookedVisitActivityLogStatics.valueByLogString(ERRONEOUS_ACTION_NAME);
            fail("An exception should have been thrown");
        }
        catch (SchedulerRuntimeException e) { }

        try {
            BookedVisitActivityLogStatics action = BookedVisitActivityLogStatics.valueByLogString(
                    BookedVisitActivityLogStatics.SCHEDULED.getLogString()
            );
            assertEquals(BookedVisitActivityLogStatics.SCHEDULED, action);
        }
        catch (SchedulerRuntimeException e) {
            fail("An exception should not have been thrown");
        }

    }

    @Test
    public void testGetActionForViewedResourcesListForAppointment() {
        String status = "some status";
        String logString = BookedVisitActivityLogStatics.getActionForViewedResourcesListForAppointment(status);
        assertEquals("Clicked on the " + status + " appointment and viewed the resources list.", logString);
    }

    @Test
    public void testGetActionForViewedHistoryListForAppointment() {
        String status = "some status";
        String logString = BookedVisitActivityLogStatics.getActionForViewedHistoryForAppointment(status);
        assertEquals("Clicked on the " + status + " appointment and viewed the activity history.", logString);
    }

}
