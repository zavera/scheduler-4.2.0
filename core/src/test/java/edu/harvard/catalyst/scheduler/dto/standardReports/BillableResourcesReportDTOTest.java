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
import edu.harvard.catalyst.scheduler.dto.BillableResourcesReportDTO;
import org.junit.Test;

import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * @author carl
 *
 */
public class BillableResourcesReportDTOTest {

    @Test
    public void toCsvHeadersTest() {
        BillableResourcesReportDTO dto1 = new BillableResourcesReportDTO();

        String result = dto1.toCsvHeaders();

        assertEquals("Resource Name,Check In,Check Out," +
                "Visit Duration,Study Subject," +
                "MRN,Industry Initiated,CRC Approved Study,Sublocation," +
                "Study Name,Catalyst ID,Local ID,IRB #,PI,Visit Name,Visit Type," +
                "Resource Start Time,Resource End Time, Booked Resource Activities, " +
                "Booked Resource Activities Comment, " +
                "Booked Resource Activities Quantity, Booked Visit Comment", result);
    }

    @Test
    public void toCsvRowsTest() {

        BillableResourcesReportDTO dto1 = new BillableResourcesReportDTO();
        BillableResourcesReportDTO dto2 = new BillableResourcesReportDTO();
        List<?> dtoList = Lists.newArrayList(dto1, dto2);

        Date checkin = new Date(100060000);
        Date checkout = new Date(100120000);
        Date scheduledIn = new Date(200060000);
        Date scheduledOut = new Date(200120000);
        
        dto1.setResourceName("r1");
        dto1.setCheckInTime(checkin);
        dto1.setCheckOutTime(checkout);
        dto1.setSubjectFirstName("sf1");
        dto1.setSubjectMiddleName("sm1");
        dto1.setSubjectLastName("sl1");
        dto1.setMrn("m1");
        dto1.setIndustryInitiated(true);
        dto1.setCrcFunded(true);
        dto1.setSublocationName("sub1");
        dto1.setStudyName("st1");
        dto1.setCatalystId("c1");
        dto1.setLocalId("l1");
        dto1.setIrb("i1");
        dto1.setPiFirstName("pf1");
        dto1.setPiMiddleName("pm1");
        dto1.setPiLastName("pl1");
        dto1.setVisitName("v1");
        dto1.setVisitTypeName("vt1");
        dto1.setScheduledStartTime(scheduledIn);
        dto1.setScheduledEndTime(scheduledOut);
        dto1.setResourceActivities("act1");
        dto1.setResourceActivitiesComment("acomm1");
        dto1.setResourceActivitiesQuantity("q1");
        dto1.setBookedVisitComment("bvcomm1");

        dto2.setResourceName("r2");
        dto2.setCheckInTime(checkin);
        dto2.setCheckOutTime(checkout);
        dto2.setSubjectFirstName("sf2");
        dto2.setSubjectMiddleName("sm2");
        dto2.setSubjectLastName("sl2");
        dto2.setMrn("m2");
        dto2.setIndustryInitiated(true);
        dto2.setCrcFunded(true);
        dto2.setSublocationName("sub2");
        dto2.setStudyName("st2");
        dto2.setCatalystId("c2");
        dto2.setLocalId("l2");
        dto2.setIrb("i2");
        dto2.setPiFirstName("pf2");
        dto2.setPiMiddleName("pm2");
        dto2.setPiLastName("pl2");
        dto2.setVisitName("v2");
        dto2.setVisitTypeName("vt2");
        dto2.setScheduledStartTime(scheduledIn);
        dto2.setScheduledEndTime(scheduledOut);
        dto2.setResourceActivities("act2");
        dto2.setResourceActivitiesComment("acomm2");
        dto2.setResourceActivitiesQuantity("q2");
        dto2.setBookedVisitComment("bvcomm2");

        List<String> threeRows = dto1.toCsvRows(dtoList);

        String expected1 = "\"r1\",\"01/01/1970 22:47\",\"01/01/1970 22:48\"," +
                "1 Min,\"sf1 sm1 sl1\",\"m1\",\"Yes\",\"Yes\",\"sub1\"," +
                "\"st1\",\"c1\",\"l1\",\"i1\",\"pf1 pm1 pl1\",\"v1\",\"vt1\"," +
                "\"01/03/1970 02:34\",\"01/03/1970 02:35\",\"act1\",\"acomm1\"," +
                "\"q1\",\"bvcomm1\"\n";
        String expected2 = "\"r2\",\"01/01/1970 22:47\",\"01/01/1970 22:48\"," +
                "1 Min,\"sf2 sm2 sl2\",\"m2\",\"Yes\",\"Yes\",\"sub2\"," +
                "\"st2\",\"c2\",\"l2\",\"i2\",\"pf2 pm2 pl2\",\"v2\",\"vt2\"," +
                "\"01/03/1970 02:34\",\"01/03/1970 02:35\",\"act2\",\"acomm2\"," +
                "\"q2\",\"bvcomm2\"\n";

        assertEquals(3, threeRows.size());
        
        assertEquals(expected1, threeRows.get(1));
        assertEquals(expected2, threeRows.get(2));
    }
}
