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
import edu.harvard.catalyst.scheduler.dto.BillingReportDTO;
import org.junit.Test;

import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * @author carl
 *
 */
public class BillingReportDTOTest {

    @Test
    public void toCsvHeadersTest() {
        BillingReportDTO dto1 = new BillingReportDTO();

        String result = dto1.toCsvHeaders();

        assertEquals("Study Name,Catalyst ID,Local ID,IRB #," +
                "Industry Initiated,PI Name,CRC Approved Study," +
                "Resource Start Time,Resource End Time," +
                "Resource Duration,Resource Duration In Mins," +
                "Check In,Check Out,Visit Duration," +
                "Visit Duration Total Mins,Visit Over Midnight?," +
                "Sublocation,Resource Name, Resource Activities, " +
                "Resource Activities Comment, " +
                "Resource Activities Quantity, Billable,Visit Name," +
                "Visit Type,Study Subject, MRN,Birth Date," +
                "Booked Visit Comment", result);
    }

    @Test
    public void toCsvRowsTest() {

        BillingReportDTO dto1 = new BillingReportDTO();
        BillingReportDTO dto2 = new BillingReportDTO();
        List<?> dtoList = Lists.newArrayList(dto1, dto2);
        
        Date chkIn1 = new Date( 120100);
        Date chkOut1 = new Date(240100);
        Date chkIn2 = new Date( 120100);
        Date chkOut2 = new Date(240100);
        
        Date vstIn1 = new Date( 360100);
        Date vstOut1 = new Date(480100);
        Date vstIn2 = new Date( 360100);
        Date vstOut2 = new Date(480100);
        
        Date schIn1 = new Date( 600100);
        Date schOut1 = new Date(720100);
        Date schIn2 = new Date( 600100);
        Date schOut2 = new Date(720100);

        dto1.setStudyId(1);
        dto1.setStudyName("s1");
        dto1.setCatalystId("c1");
        dto1.setLocalId("l1");
        dto1.setIrb("i1");
        dto1.setIndustryInitiated(true);
        dto1.setCrcFunded(true);
        dto1.setCheckInTime(chkIn1);
        dto1.setCheckOutTime(chkOut1);
        dto1.setScheduledStartTime(schIn1);
        dto1.setScheduledEndTime(schOut1);
        dto1.setVisitCheckInTime(vstIn1);
        dto1.setVisitCheckOutTime(vstOut1);
        dto1.setSublocation("sl1");
        dto1.setResourceName("r1");
        dto1.setBillable(true);
        dto1.setVisitName("v1");
        dto1.setVisitType("t1");
        dto1.setSubjectFirstName("sf1");
        dto1.setSubjectMiddleName("sm1");
        dto1.setSubjectLastName("sl1");
        dto1.setMrn("m1");
        dto1.setBirthdate("b1");
        dto1.setInvestigatorFirstName("if1");
        dto1.setInvestigatorMiddleName("im1");
        dto1.setInvestigatorLastName("il1");
        dto1.setBookedVisitComment("vc1");
        dto1.setResourceActivities("a1");
        dto1.setResourceActivitiesComment("ac1");
        dto1.setResourceActivitiesQuantity("q1");

        dto2.setStudyId(2);
        dto2.setStudyName("s2");
        dto2.setCatalystId("c2");
        dto2.setLocalId("l2");
        dto2.setIrb("i2");
        dto2.setIndustryInitiated(false);
        dto2.setCrcFunded(false);
        dto2.setCheckInTime(chkIn2);
        dto2.setCheckOutTime(chkOut2);
        dto2.setScheduledStartTime(schIn2);
        dto2.setScheduledEndTime(schOut2);
        dto2.setVisitCheckInTime(vstIn2);
        dto2.setVisitCheckOutTime(vstOut2);
        dto2.setSublocation("sl2");
        dto2.setResourceName("r2");
        dto2.setBillable(false);
        dto2.setVisitName("v2");
        dto2.setVisitType("t2");
        dto2.setSubjectFirstName("sf2");
        dto2.setSubjectMiddleName("sm2");
        dto2.setSubjectLastName("sl2");
        dto2.setMrn("m2");
        dto2.setBirthdate("b2");
        dto2.setInvestigatorFirstName("if2");
        dto2.setInvestigatorMiddleName("im2");
        dto2.setInvestigatorLastName("il2");
        dto2.setBookedVisitComment("vc2");
        dto2.setResourceActivities("a2");
        dto2.setResourceActivitiesComment("ac2");
        dto2.setResourceActivitiesQuantity("q2");

        List<String> threeRows = dto1.toCsvRows(dtoList);

        String expected1 = "\"s1\",\"c1\",\"l1\",\"i1\",\"Yes\"," +
                "\"if1 im1 il1\",\"Yes\"," +
                "\"12/31/1969 19:02\",\"12/31/1969 19:04\"," +
                "\"2 Mins\",\"2\",\"12/31/1969 19:06\"," +
                "\"12/31/1969 19:08\",\"2 Mins\",\"2\",\"NO\"," +
                "\"sl1\",\"r1\",\"a1\",\"ac1\",\"q1\",\"Yes\"," +
                "\"v1\",\"t1\",\"sf1 sm1 sl1\",\"m1\",\"b1\",\"vc1\"\n";

        String expected2 = "\"s2\",\"c2\",\"l2\",\"i2\",\"No\"," +
                "\"if2 im2 il2\",\"No\"," +
                "\"12/31/1969 19:02\",\"12/31/1969 19:04\"," +
                "\"2 Mins\",\"2\",\"12/31/1969 19:06\"," +
                "\"12/31/1969 19:08\",\"2 Mins\",\"2\",\"NO\"," +
                "\"sl2\",\"r2\",\"a2\",\"ac2\",\"q2\",\"No\"," +
                "\"v2\",\"t2\",\"sf2 sm2 sl2\",\"m2\",\"b2\",\"vc2\"\n";

        assertEquals(3, threeRows.size());

        assertEquals(expected1, threeRows.get(1));
        assertEquals(expected2, threeRows.get(2));
    }

}
