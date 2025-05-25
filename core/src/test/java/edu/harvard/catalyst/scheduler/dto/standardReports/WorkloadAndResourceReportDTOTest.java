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
import edu.harvard.catalyst.scheduler.dto.WorkloadAndResourceResponseDTO;
import edu.harvard.catalyst.scheduler.entity.VisitType;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * @author carl
 *
 */
public class WorkloadAndResourceReportDTOTest {
    private WorkloadAndResourceResponseDTO dto1 = new WorkloadAndResourceResponseDTO();
    private WorkloadAndResourceResponseDTO dto2 = new WorkloadAndResourceResponseDTO();

    @Test
    public void toCsvHeadersTest() {
        String result = dto1.toCsvHeaders();

        assertEquals("Visit Name,Visit Type,Local ID,Nursing,"
                + "Nutrition,Processing,Setup,"
                        + "Resource Name,Resource Duration,"
                        + "Resource Name,Resource Duration,"
                        + "Resource Name,Resource Duration,"

                        + "Resource Name,Resource Duration,"
                        + "Resource Name,Resource Duration,"
                        + "Resource Name,Resource Duration,"

                        + "Resource Name,Resource Duration,"
                        + "Resource Name,Resource Duration,"
                        + "Resource Name,Resource Duration,"
                        + "Resource Name,Resource Duration"
                , result);
    }

    @Test
    public void toCsvRowsTest() {

        VisitType vt1 = new VisitType();
        vt1.setName("vt1");

        dto1.setVisitId(1);
        dto1.setStudyName("s1");
        dto1.setVisitName("v1");
        dto1.setNursing("n1");
        dto1.setNutrition("nu1");
        dto1.setProcessing("p1");
        dto1.setSetup("su1");
        dto1.setVisitType(vt1);
        dto1.setDuration(1);
        dto1.setResourceName("r1");

        VisitType vt2 = new VisitType();
        vt2.setName("vt2");

        dto2.setVisitId(2);
        dto2.setStudyName("s2");
        dto2.setVisitName("v2");
        dto2.setNursing("n2");
        dto2.setNutrition("nu2");
        dto2.setProcessing("p2");
        dto2.setSetup("su2");
        dto2.setVisitType(vt2);
        dto2.setDuration(2);
        dto2.setResourceName("r2");

        List<?> dtoList = Lists.newArrayList(dto1, dto2);

        List<String> threeRows = dto1.toCsvRows(dtoList);

        String expected1 = "\"v1\",\"vt1\",\"s1\",\"n1\",\"nu1\",\"p1\",\"su1\",\"r1\",\"1 Min\"\n";

        String expected2 = "\"v2\",\"vt2\",\"s2\",\"n2\",\"nu2\",\"p2\",\"su2\",\"r2\",\"2 Mins\"\n";

        assertEquals(3, threeRows.size());
        
        assertEquals(expected1, threeRows.get(1));
        assertEquals(expected2, threeRows.get(2));
    }
}
