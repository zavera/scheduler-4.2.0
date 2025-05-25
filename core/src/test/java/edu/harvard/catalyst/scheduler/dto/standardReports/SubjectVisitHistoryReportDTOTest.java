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
package edu.harvard.catalyst.scheduler.dto.standardReports;

import com.google.common.collect.Lists;
import edu.harvard.catalyst.scheduler.dto.SubjectVisitHistoryReportDTO;
import org.junit.Test;

import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * @author carl
 *
 */
public class SubjectVisitHistoryReportDTOTest {
    @Test
    public void toCsvHeadersTest() {
        SubjectVisitHistoryReportDTO dto1 = new SubjectVisitHistoryReportDTO();

        String result = dto1.toCsvHeaders();

        assertEquals("Subject Name,MRN,Visit Name,Scheduled Visit Start Time," +
                        "Scheduled Visit End Time,Appointment Status,Local ID, " +
                        "Cancel Date, Cancel Status Reason, Check In Date, " +
                        "Check Out Date",
                result);
    }

    @Test
    public void toCsvRowsTest() {

        SubjectVisitHistoryReportDTO dto1 = new SubjectVisitHistoryReportDTO();
        SubjectVisitHistoryReportDTO dto2 = new SubjectVisitHistoryReportDTO();

        dto1.setSubjectFirstName("f1");
        dto1.setSubjectMiddleName("m1");
        dto1.setSubjectLastName("l1");
        dto1.setMrn("mrn1");
        dto1.setLocalId("lid1");
        dto1.setVisitName("v1");
        dto1.setScheduledStartTime(new Date(100060000));
        dto1.setScheduledEndTime(new Date(100120000));
        dto1.setCancelDate(new Date(100180000));
        dto1.setCheckInDate(new Date(100240000));
        dto1.setCheckOutDate(new Date(100300000));
        dto1.setAppointmentStatus("as1");
        dto1.setCancelStatusReason("cs1");

        dto2.setSubjectFirstName("f2");
        dto2.setSubjectMiddleName("m2");
        dto2.setSubjectLastName("l2");
        dto2.setMrn("mrn2");
        dto2.setLocalId("lid2");
        dto2.setVisitName("v2");
        dto2.setScheduledStartTime(new Date(200060000));
        dto2.setScheduledEndTime(new Date(200220000));
        dto2.setCancelDate(new Date(200280000));
        dto2.setCheckInDate(new Date(200240000));
        dto2.setCheckOutDate(new Date(200300000));
        dto2.setAppointmentStatus("as2");
        dto2.setCancelStatusReason("cs2");

        List<?> dtoList = Lists.newArrayList(dto1, dto2);

        List<String> threeRows = dto1.toCsvRows(dtoList);

        String expected1 = "\"f1 m1 l1\",\"mrn1\",\"v1\",\"01/01/1970 22:47\",\"01/01/1970 22:48\",\"as1\",\"lid1\",\"01/01/1970 22:49\",\"cs1\",\"01/01/1970 22:50\",\"01/01/1970 22:51\"\n";

        String expected2 = "\"f2 m2 l2\",\"mrn2\",\"v2\",\"01/03/1970 02:34\",\"01/03/1970 02:37\",\"as2\",\"lid2\",\"01/03/1970 02:38\",\"cs2\",\"01/03/1970 02:37\",\"01/03/1970 02:38\"\n";

        assertEquals(4, threeRows.size());

        assertEquals(expected1, threeRows.get(1));
        assertEquals(expected2, threeRows.get(3));
    }
}
