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
import edu.harvard.catalyst.scheduler.dto.StudySubjectVisitReportDTO;
import org.junit.Test;

import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * @author carl
 *
 */
public class StudySubjectVisitReportDTOTest {
    @Test
    public void toCsvHeadersTest() {
        StudySubjectVisitReportDTO dto1 = new StudySubjectVisitReportDTO();

        String result = dto1.toCsvHeaders();

        assertEquals("Subject Name,MRN,DOB,Visit Name," +
                        "Scheduled Visit Start Time," +
                        "Scheduled Visit End Time,Appointment Status," +
                        "Local ID",
                result);
    }

    @Test
    public void toCsvRowsTest() {

        StudySubjectVisitReportDTO dto1 = new StudySubjectVisitReportDTO();
        StudySubjectVisitReportDTO dto2 = new StudySubjectVisitReportDTO();

        dto1.setSubjectFirstName("f1");
        dto1.setSubjectMiddleName("m1");
        dto1.setSubjectLastName("l1");
        dto1.setMrn("mrn1");
        dto1.setMrn("dob1");
        dto1.setLocalId("lid1");
        dto1.setVisitName("v1");
        dto1.setScheduledStartTime(new Date(100060000));
        dto1.setScheduledEndTime(new Date(100120000));
        dto1.setAppointmentStatus("as1");

        dto2.setSubjectFirstName("f2");
        dto2.setSubjectMiddleName("m2");
        dto2.setSubjectLastName("l2");
        dto2.setMrn("mrn2");
        dto2.setMrn("dob2");
        dto2.setLocalId("lid2");
        dto2.setVisitName("v2");
        dto2.setScheduledStartTime(new Date(200060000));
        dto2.setScheduledEndTime(new Date(200220000));
        dto2.setAppointmentStatus("as2");

        List<?> dtoList = Lists.newArrayList(dto1, dto2);

        List<String> threeRows = dto1.toCsvRows(dtoList);

        String expected1 = "\"f1 m1 l1\",\"dob1\",\"\",\"v1\",\"01/01/1970 22:47\",\"01/01/1970 22:48\",\"as1\",\"lid1\"\n";

        String expected2 = "\"f2 m2 l2\",\"dob2\",\"\",\"v2\",\"01/03/1970 02:34\",\"01/03/1970 02:37\",\"as2\",\"lid2\"\n";

        assertEquals(4, threeRows.size());

        assertEquals(expected1, threeRows.get(1));
        assertEquals(expected2, threeRows.get(3));
    }
}
