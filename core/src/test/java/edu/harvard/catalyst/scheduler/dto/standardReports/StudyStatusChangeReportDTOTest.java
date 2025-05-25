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
import edu.harvard.catalyst.scheduler.dto.StudyStatusChangeReportDTO;
import org.junit.Test;

import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * @author carl
 *
 */
public class StudyStatusChangeReportDTOTest {

    @Test
    public void toCsvHeadersTest() {
        StudyStatusChangeReportDTO dto1 = new StudyStatusChangeReportDTO();

        String result = dto1.toCsvHeaders();

        assertEquals("Local ID,PI,Study Status, Date of Status Change, User", result);
    }

    @Test
    public void toCsvRowsTest() {

        StudyStatusChangeReportDTO dto1 = new StudyStatusChangeReportDTO();
        StudyStatusChangeReportDTO dto2 = new StudyStatusChangeReportDTO();
        List<?> dtoList = Lists.newArrayList(dto1, dto2);

        Date change = new Date(100180000);

        dto1.setLocalId("l1");
        dto1.setPi("p1");
        dto1.setStudyStatus("st1");
        dto1.setDateStatusChange(change);
        dto1.setUserFirstName("f1");
        dto1.setUserMiddleName("m1");
        dto1.setUserLastName("l1");

        dto2.setLocalId("l2");
        dto2.setPi("p2");
        dto2.setStudyStatus("st2");
        dto2.setDateStatusChange(change);
        dto2.setUserFirstName("f2");
        dto2.setUserMiddleName("m2");
        dto2.setUserLastName("l2");

        List<String> threeRows = dto1.toCsvRows(dtoList);

        String expected1 = "\"l1\",\"p1\",\"st1\",\"01/01/1970 22:49\",\"f1 m1 l1\"\n";

        String expected2 = "\"l2\",\"p2\",\"st2\",\"01/01/1970 22:49\",\"f2 m2 l2\"\n";

        assertEquals(3, threeRows.size());
        
        assertEquals(expected1, threeRows.get(1));
        assertEquals(expected2, threeRows.get(2));
    }
}
