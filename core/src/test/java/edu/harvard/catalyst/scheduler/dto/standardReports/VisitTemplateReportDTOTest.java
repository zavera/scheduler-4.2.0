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
import edu.harvard.catalyst.scheduler.dto.VisitTemplateReportDTO;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * @author carl
 *
 */
public class VisitTemplateReportDTOTest {

    @Test
    public void toCsvHeadersTest() {
        VisitTemplateReportDTO dto1 = new VisitTemplateReportDTO();

        String result = dto1.toCsvHeaders();

        assertEquals("Study Id,Study Name,Study Status," +
                "Visit Name,Visit Type,Sublocation,Billable Template?,Research Pharmacy?," +
                "Institution: Non CRC,Non Institution: Non CRC," +
                "Relative Time?,Nursing,Nutrition,Processing," +
                "Setup,Comment", result);
    }

    @Test
    public void toCsvRowsTest() {

        VisitTemplateReportDTO dto1 = new VisitTemplateReportDTO();
        VisitTemplateReportDTO dto2 = new VisitTemplateReportDTO();
        List<?> dtoList = Lists.newArrayList(dto1, dto2);

        dto1.setStudyId("sid1");
        dto1.setStudyName("s1");
        dto1.setStudyStatus("status1");
        dto1.setName("name1");
        dto1.setVisitType("vtype1");
        dto1.setSublocation("sub1");
        dto2.setBillable(true);
        dto1.setResearchPharmacy(true);
        dto1.setInstitutionNonCRC(true);
        dto1.setNonInstitutionNonCRC(true);
        dto1.setRelativeTime(true);
        dto1.setNursing("nurse1");
        dto1.setNutrition("nutr1");
        dto1.setProcessing("p1");
        dto1.setSetup("setup1");
        dto1.setComment("c1");

        dto2.setStudyId("sid2");
        dto2.setStudyName("s2");
        dto2.setStudyStatus("status2");
        dto2.setName("name2");
        dto2.setVisitType("vtype2");
        dto2.setSublocation("sub2");
        dto2.setBillable(true);
        dto2.setResearchPharmacy(true);
        dto2.setInstitutionNonCRC(true);
        dto2.setNonInstitutionNonCRC(true);
        dto2.setRelativeTime(true);
        dto2.setNursing("nurse2");
        dto2.setNutrition("nutr2");
        dto2.setProcessing("p2");
        dto2.setSetup("setup2");
        dto2.setComment("c2");

        List<String> threeRows = dto1.toCsvRows(dtoList);

        String expected1 = "\"sid1\",\"s1\",\"status1\",\"name1\",\"vtype1\",\"sub1\",\"\",\"Yes\",\"Yes\",\"Yes\",\"Yes\",\"nurse1\",\"nutr1\",\"p1\",\"setup1\",\"c1\"\n";

        String expected2 = "\"sid2\",\"s2\",\"status2\",\"name2\",\"vtype2\",\"sub2\",\"Yes\",\"Yes\",\"Yes\",\"Yes\",\"Yes\",\"nurse2\",\"nutr2\",\"p2\",\"setup2\",\"c2\"\n";

        assertEquals(3, threeRows.size());
        
        assertEquals(expected1, threeRows.get(1));
        assertEquals(expected2, threeRows.get(2));
    }
}
