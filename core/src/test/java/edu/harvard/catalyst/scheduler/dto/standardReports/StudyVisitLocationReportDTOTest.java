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
import edu.harvard.catalyst.scheduler.dto.StudyVisitLocationReportDTO;
import org.junit.Test;

import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * @author carl
 *
 */
public class StudyVisitLocationReportDTOTest {

    @Test
    public void toCsvHeadersTest() {
        StudyVisitLocationReportDTO dto1 = new StudyVisitLocationReportDTO();

        String result = dto1.toCsvHeaders();

        assertEquals("Location Name,Visit Name,Visit Type,Check In Time," +
                "Check Out Time,Visit Duration,Subject Name,MRN,Gender," +
                "Study Name,Catalyst ID,Local Id,IRB #", result);
    }

    @Test
    public void toCsvRowsTest() {

        StudyVisitLocationReportDTO dto1 = new StudyVisitLocationReportDTO();
        StudyVisitLocationReportDTO dto2 = new StudyVisitLocationReportDTO();
        List<?> dtoList = Lists.newArrayList(dto1, dto2);

        Date in = new Date(100180000);
        Date out = new Date(100240000);

        dto1.setSublocationName("sub1");
        dto1.setVisitTypeName("vtn1");
        dto1.setVisitType("vt1");
        dto1.setCheckInTime(in);
        dto1.setCheckOutTime(out);
        dto1.setSubjectFirstName("f1");
        dto1.setSubjectMiddleName("m1");
        dto1.setSubjectLastName("l1");
        dto1.setMrn("mrn1");
        dto1.setGenderName("g1");
        dto1.setStudyName("s1");
        dto1.setCatalystId("c1");
        dto1.setLocalId("l1");
        dto1.setIrb("i1");

        dto2.setSublocationName("sub2");
        dto2.setVisitTypeName("vtn2");
        dto2.setVisitType("vt2");
        dto2.setCheckInTime(in);
        dto2.setCheckOutTime(out);
        dto2.setSubjectFirstName("f2");
        dto2.setSubjectMiddleName("m2");
        dto2.setSubjectLastName("l2");
        dto2.setMrn("mrn2");
        dto2.setGenderName("g2");
        dto2.setStudyName("s2");
        dto2.setCatalystId("c2");
        dto2.setLocalId("l2");
        dto2.setIrb("i2");

        List<String> threeRows = dto1.toCsvRows(dtoList);

        String expected1 = "\"sub1\",\"vtn1\",\"vt1\",\"01/01/1970 22:49\",\"01/01/1970 22:50\",\"1 Min\",\"f1 m1 l1\",\"mrn1\",\"g1\",\"s1\",\"c1\",\"l1\",\"i1\"\n";

        String expected2 = "\"sub2\",\"vtn2\",\"vt2\",\"01/01/1970 22:49\",\"01/01/1970 22:50\",\"1 Min\",\"f2 m2 l2\",\"mrn2\",\"g2\",\"s2\",\"c2\",\"l2\",\"i2\"\n";

        assertEquals(3, threeRows.size());
        
        assertEquals(expected1, threeRows.get(1));
        assertEquals(expected2, threeRows.get(2));
    }
}
