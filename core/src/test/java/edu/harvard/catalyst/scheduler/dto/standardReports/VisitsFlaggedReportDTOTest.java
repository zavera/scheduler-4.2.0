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
import edu.harvard.catalyst.scheduler.dto.VisitsFlaggedEditReportDTO;
import org.junit.Test;

import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * @author carl
 *
 */
public class VisitsFlaggedReportDTOTest {

    @Test
    public void toCsvHeadersTest() {
        VisitsFlaggedEditReportDTO dto1 = new VisitsFlaggedEditReportDTO();

        String result = dto1.toCsvHeaders();

        assertEquals("Study Name,Local ID,PI Name,Subject Name,DOB,MRN," +
                "Visit Name, Visit Type, Check In, Check Out, " +
                "Resource Duration Variation?,Activity Variation?", result);
    }

    @Test
    public void toCsvRowsTest() {

        VisitsFlaggedEditReportDTO dto1 = new VisitsFlaggedEditReportDTO();
        VisitsFlaggedEditReportDTO dto2 = new VisitsFlaggedEditReportDTO();
        List<?> dtoList = Lists.newArrayList(dto1, dto2);

        Date start = new Date(100060000);
        Date end = new Date(100120000);

        dto1.setStudyName("s1");
        dto1.setLocalId("l1");
        dto1.setPiName("p1");
        dto1.setSubjectFirstName("f1");
        dto1.setSubjectMiddleName("m1");
        dto1.setSubjectLastName("l1");
        dto1.setBirthdate("b1");
        dto1.setMrn("mrn1");
        dto1.setVisitName("v1");
        dto1.setVisitType("vt1");
        dto1.setCheckInTime(start);
        dto1.setCheckOutTime(end);
        dto1.setVaryDuration(true);
        dto1.setOmmittedActivities(true);

        dto2.setStudyName("s2");
        dto2.setLocalId("l2");
        dto2.setPiName("p2");
        dto2.setSubjectFirstName("f2");
        dto2.setSubjectMiddleName("m2");
        dto2.setSubjectLastName("l2");
        dto2.setBirthdate("b2");
        dto2.setMrn("mrn2");
        dto2.setVisitName("v2");
        dto2.setVisitType("vt2");
        dto2.setCheckInTime(start);
        dto2.setCheckOutTime(end);
        dto2.setVaryDuration(true);
        dto2.setOmmittedActivities(true);

        List<String> threeRows = dto1.toCsvRows(dtoList);

        String expected1 = "\"s1\",\"l1\",\"p1\",\"f1 m1 l1\",\"b1\",\"mrn1\",\"v1\",\"vt1\",01/01/1970 22:47,01/01/1970 22:48,Yes,Yes\n";

        String expected2 = "\"s2\",\"l2\",\"p2\",\"f2 m2 l2\",\"b2\",\"mrn2\",\"v2\",\"vt2\",01/01/1970 22:47,01/01/1970 22:48,Yes,Yes\n";

        assertEquals(3, threeRows.size());
        
        assertEquals(expected1, threeRows.get(1));
        assertEquals(expected2, threeRows.get(2));
    }
}
