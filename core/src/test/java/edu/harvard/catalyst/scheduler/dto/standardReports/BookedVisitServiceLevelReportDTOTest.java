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
import edu.harvard.catalyst.scheduler.dto.BookedVisitServiceLevelByVisitTypeReportDTO;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * @author carl
 *
 */
public class BookedVisitServiceLevelReportDTOTest {

    @Test
    public void toCsvHeadersTest() {
        BookedVisitServiceLevelByVisitTypeReportDTO dto1 = new BookedVisitServiceLevelByVisitTypeReportDTO();

        String result = dto1.toCsvHeaders();

        assertEquals("Visit Type,Nursing,# of Visits,Nutrition,# of Visits," +
                "Processing,# of Visits,Set Up,# of Visits", result);
    }

    @Test
    public void toCsvRowsTest() {

        BookedVisitServiceLevelByVisitTypeReportDTO dto1 = new BookedVisitServiceLevelByVisitTypeReportDTO();
        BookedVisitServiceLevelByVisitTypeReportDTO dto2 = new BookedVisitServiceLevelByVisitTypeReportDTO();
        List<?> dtoList = Lists.newArrayList(dto1, dto2);

        dto1.setVisitId(1);
        dto1.setVisitName("v1");
        dto1.setVisitType("vt1");
        dto1.setNursing("nurse1");
        dto1.setNursinglevel("nurseLevel1");
        dto1.setNutrition("nutr1");
        dto1.setNutritionlevel("nutrLevel");
        dto1.setProcessing("proc1");
        dto1.setProcessinglevel("procLevel1");
        dto1.setSetup("setup1");
        dto1.setSetuplevel("setupLevel1");

        dto2.setVisitId(2);
        dto2.setVisitName("v2");
        dto2.setVisitType("vt2");
        dto2.setNursing("nurse2");
        dto2.setNursinglevel("nurseLevel2");
        dto2.setNutrition("nutr2");
        dto2.setNutritionlevel("nutrLevel");
        dto2.setProcessing("proc2");
        dto2.setProcessinglevel("procLevel2");
        dto2.setSetup("setup2");
        dto2.setSetuplevel("setupLevel2");

        List<String> threeRows = dto1.toCsvRows(dtoList);

        String expected1 = "\"vt1\",\"nurseLevel1\",\"nurse1\",\"nutrLevel\",\"nutr1\",\"procLevel1\",\"proc1\",\"setupLevel1\",\"setup1\"\n";

        String expected2 = "\"vt2\",\"nurseLevel2\",\"nurse2\",\"nutrLevel\",\"nutr2\",\"procLevel2\",\"proc2\",\"setupLevel2\",\"setup2\"\n";

        assertEquals(3, threeRows.size());
        
        assertEquals(expected1, threeRows.get(1));
        assertEquals(expected2, threeRows.get(2));
    }
}
