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
import edu.harvard.catalyst.scheduler.dto.AncillaryOnlyByProtocolReportDTO;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * @author carl
 *
 */
public class AncillaryOnlyReportDTOTest {

    @Test
    public void toCsvHeadersTest() {
        AncillaryOnlyByProtocolReportDTO dto1 = new AncillaryOnlyByProtocolReportDTO();

        String result = dto1.toCsvHeaders();

        assertEquals("Protocol,,Ancillary Only Visits\n,A, B, D", result);
    }

    @Test
    public void toCsvRowsTest() {

        AncillaryOnlyByProtocolReportDTO dto1 = new AncillaryOnlyByProtocolReportDTO();
        AncillaryOnlyByProtocolReportDTO dto2 = new AncillaryOnlyByProtocolReportDTO();
        List<?> dtoList = Lists.newArrayList(dto1, dto2);
        
        dto1.setStudyName("s1");
        dto1.setCrcA("a1");
        dto1.setCrcB("b1");
        dto1.setCrcC("c1");

        dto2.setStudyName("s2");
        dto2.setCrcA("a2");
        dto2.setCrcB("b2");
        dto2.setCrcC("c2");

        dto2.setTotalCRCA(10);
        dto2.setTotalCRCB(11);
        dto2.setTotalCRCC(12);

        List<String> threeRows = dto1.toCsvRows(dtoList);

        String expected1 = "\"s1\",\"a1\",\"b1\",\"c1\"\n";
        String expected2 = "\"s2\",\"a2\",\"b2\",\"c2\"\n";
        String expected3 = "\"Total\",\"10\",\"11\",\"12\"\n";

        assertEquals(4, threeRows.size());
        
        assertEquals(expected1, threeRows.get(1));
        assertEquals(expected2, threeRows.get(2));
        assertEquals(expected3, threeRows.get(3));
    }
}
