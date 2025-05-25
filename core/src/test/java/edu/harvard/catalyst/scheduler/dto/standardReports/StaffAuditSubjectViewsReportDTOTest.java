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
import edu.harvard.catalyst.scheduler.dto.StaffAuditSubjectViewsReportDTO;
import org.junit.Test;

import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * @author carl
 *
 */
public class StaffAuditSubjectViewsReportDTOTest {

    @Test
    public void toCsvHeadersTest() {
        StaffAuditSubjectViewsReportDTO dto1 = new StaffAuditSubjectViewsReportDTO();

        String result = dto1.toCsvHeaders();

        assertEquals("Subject Name,MRN,Action Performed," +
                "Date,IP Address,User,User ID", result);
    }

    @Test
    public void toCsvRowsTest() {

        StaffAuditSubjectViewsReportDTO dto1 = new StaffAuditSubjectViewsReportDTO();
        StaffAuditSubjectViewsReportDTO dto2 = new StaffAuditSubjectViewsReportDTO();
        List<?> dtoList = Lists.newArrayList(dto1, dto2);

        Date date = new Date(100180000);

        dto1.setSubjectId(1);
        dto1.setSubjectFirstName("sf1");
        dto1.setSubjectMiddleName("sm1");
        dto1.setSubjectLastName("sl1");
        dto1.setMrn("mrn1");
        dto1.setActionPerformed("act1");
        dto1.setDate(date);
        dto1.setIpAddress("ip1");
        dto1.setUserFirstName("uf1");
        dto1.setUserMiddleName("um1");
        dto1.setUserLastName("ul1");
        dto1.setEcommons("ec1");

        dto2.setSubjectId(2);
        dto2.setSubjectFirstName("sf2");
        dto2.setSubjectMiddleName("sm2");
        dto2.setSubjectLastName("sl2");
        dto2.setMrn("mrn2");
        dto2.setActionPerformed("act2");
        dto2.setDate(date);
        dto2.setIpAddress("ip2");
        dto2.setUserFirstName("uf2");
        dto2.setUserMiddleName("um2");
        dto2.setUserLastName("ul2");
        dto2.setEcommons("ec2");

        List<String> threeRows = dto1.toCsvRows(dtoList);

        String expected1 = "\"sf1 sm1 sl1\",\"mrn1\",\"act1\",\"01/01/1970 22:49\",\"ip1\",\"uf1 um1 ul1\",\"ec1\"\n";

        String expected2 = "\"sf2 sm2 sl2\",\"mrn2\",\"act2\",\"01/01/1970 22:49\",\"ip2\",\"uf2 um2 ul2\",\"ec2\"\n";

        assertEquals(5, threeRows.size());
        
        assertEquals(expected1, threeRows.get(1));
        assertEquals(expected2, threeRows.get(4));
    }
}
