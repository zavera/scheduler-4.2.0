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
import edu.harvard.catalyst.scheduler.dto.VisitDurationByVisitTypeReportDTO;
import org.junit.Test;

import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * @author carl
 *
 */
public class VisitsDurationReportDTOTest {

    @Test
    public void toCsvHeadersTest() {
        VisitDurationByVisitTypeReportDTO dto1 = new VisitDurationByVisitTypeReportDTO();

        String result = dto1.toCsvHeaders();

        assertEquals("Local ID,Booked Visit ID,Scheduled Start Time," +
                "Scheduled End Time,Visit Type,Visit Name," +
                "Check In Date,Check Out Date,Visit Duration", result);
    }

    @Test
    public void toCsvRowsTest() {

        VisitDurationByVisitTypeReportDTO dto1 = new VisitDurationByVisitTypeReportDTO();
        VisitDurationByVisitTypeReportDTO dto2 = new VisitDurationByVisitTypeReportDTO();
        List<?> dtoList = Lists.newArrayList(dto1, dto2);

        Date start = new Date(100060000);
        Date end = new Date(100120000);
        Date in = new Date(100180000);
        Date out = new Date(100240000);

        dto1.setStudyName("s1");
        dto1.setVisitId(1);
        dto1.setScheduledStartTime(end);
        dto1.setScheduledEndTime(start);
        dto1.setVisitType("vt1");
        dto1.setVisitName("v1");
        dto1.setCheckInTime(in);
        dto1.setCheckOutTime(out);

        dto2.setStudyName("s2");
        dto2.setVisitId(2);
        dto2.setScheduledStartTime(end);
        dto2.setScheduledEndTime(start);
        dto2.setVisitType("vt2");
        dto2.setVisitName("v2");
        dto2.setCheckInTime(in);
        dto2.setCheckOutTime(out);

        List<String> threeRows = dto1.toCsvRows(dtoList);

        String expected1 = "\"s1\",\"1\",01/01/1970 22:48,01/01/1970 22:47,\"vt1\",\"v1\",01/01/1970 22:49,01/01/1970 22:50,1 Min\n";

        String expected2 = "\"s2\",\"2\",01/01/1970 22:48,01/01/1970 22:47,\"vt2\",\"v2\",01/01/1970 22:49,01/01/1970 22:50,1 Min\n";

        assertEquals(3, threeRows.size());
        
        assertEquals(expected1, threeRows.get(1));
        assertEquals(expected2, threeRows.get(2));
    }
}
