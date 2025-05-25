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
import edu.harvard.catalyst.scheduler.dto.SubjectPurgeReportDTO;
import org.junit.Test;

import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * @author carl
 *
 */
public class SubjectPurgeReportDTOTest {

    @Test
    public void toCsvHeadersTest() {
        SubjectPurgeReportDTO dto1 = new SubjectPurgeReportDTO();

        String result = dto1.toCsvHeaders();

        assertEquals("Subject Name,MRN,User,Subject Creation Date",
                result);
    }

    @Test
    public void toCsvRowsTest() {

        SubjectPurgeReportDTO dto1 = new SubjectPurgeReportDTO();
        SubjectPurgeReportDTO dto2 = new SubjectPurgeReportDTO();

        dto1.setFirstName("f1");
        dto1.setMiddleName("m1");
        dto1.setLastName("l1");
        dto1.setMrn("mrn1");
        dto1.setUserFirstName("uf1");
        dto1.setUserMiddleName("um1");
        dto1.setUserLastName("ul1");
        dto1.setCreateTime(new Date(100060000));

        dto2.setFirstName("f2");
        dto2.setMiddleName("m2");
        dto2.setLastName("l2");
        dto2.setMrn("mrn2");
        dto2.setUserFirstName("uf2");
        dto2.setUserMiddleName("um2");
        dto2.setUserLastName("ul2");
        dto2.setCreateTime(new Date(200060000));

        List<?> dtoList = Lists.newArrayList(dto1, dto2);

        List<String> threeRows = dto1.toCsvRows(dtoList);

        String expected1 = "\"f1 m1 l1\",\"mrn1\",\"uf1 um1 ul1\",\"01/01/1970 22:47\"\n";

        String expected2 = "\"f2 m2 l2\",\"mrn2\",\"uf2 um2 ul2\",\"01/03/1970 02:34\"\n";

        assertEquals(3, threeRows.size());
        
        assertEquals(expected1, threeRows.get(1));
        assertEquals(expected2, threeRows.get(2));
    }
}
