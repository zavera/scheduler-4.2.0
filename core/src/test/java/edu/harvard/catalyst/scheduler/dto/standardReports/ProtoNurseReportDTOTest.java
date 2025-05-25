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
import edu.harvard.catalyst.scheduler.dto.ProtoNurseReportDTO;
import org.junit.Test;

import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @author clint
 * @date Aug 1, 2013
 *
 */
public final class ProtoNurseReportDTOTest {
    private ProtoNurseReportDTO dto1 = new ProtoNurseReportDTO();
    private ProtoNurseReportDTO dto2 = new ProtoNurseReportDTO();
    private String header = "Full Name,Study Name,Catalyst ID,Local Id,Study Status,IRB #,IRB Expiration";

    @Test
    public void toCsvHeadersTest() {
        String result = dto1.toCsvHeaders();

        assertEquals(header, result);
    }

    @Test
    public void toCsvRowsTest() {

        dto1.setNurseId(1);
        dto1.setFirstName("f1");
        dto1.setMiddleName("m1");
        dto1.setLastName("l1");
        dto1.setName("n1");
        dto1.setSpid("s1");
        dto1.setCatalystId("c1");
        dto1.setLocalId("l1");
        dto1.setStudyStatusName("ss1");
        dto1.setIrb("i1");
        dto1.setIrbExpiration(new Date(100060000));

        dto2.setNurseId(2);
        dto2.setFirstName("f2");
        dto2.setMiddleName("m2");
        dto2.setLastName("l2");
        dto2.setName("n2");
        dto2.setSpid("s2");
        dto2.setCatalystId("c2");
        dto2.setLocalId("l2");
        dto2.setStudyStatusName("ss2");
        dto2.setIrb("i2");
        dto2.setIrbExpiration(new Date(200060000));

        List<?> dtoList = Lists.newArrayList(dto1, dto2);

        List<String> threeRows = dto1.toCsvRows(dtoList);

        String expected1 = "\"f1 m1 l1\",\"n1\",\"c1\",\"l1\",\"ss1\",\"i1\",\"01/01/1970\"\n";

        String expected2 = "\"f2 m2 l2\",\"n2\",\"c2\",\"l2\",\"ss2\",\"i2\",\"01/03/1970\"\n";

        assertEquals(5, threeRows.size());

        assertEquals(header + "\n", threeRows.get(0));
        assertEquals(header + "\n", threeRows.get(3));

        assertEquals(expected1, threeRows.get(1));
        assertEquals(expected2, threeRows.get(4));
    }


    @Test
    public void testFromArray() {
        final Integer nurseId = 99;
        final String firstName = "jane";
        final String middleName = "q";
        final String lastName = "public";
        final String name = "jane q public";
        final String spid = "asdadasdf";
        final String catalystId = "kalsjdhkgds";
        final String localId = "aklsdjlkajsd";
        final String studyStatusName = "aksldjlasjd";
        final String irb = "sakjfdhlksdgh";
        final Date irbExpiration = new Date();
        
        final ProtoNurseReportDTO dto = ProtoNurseReportDTO.fromArray(new Object[] { nurseId, firstName, middleName, lastName, name, spid, catalystId, localId, studyStatusName, irb, irbExpiration});
        
        assertNotNull(dto);
        
        assertEquals(nurseId, dto.getNurseId());
        assertEquals(firstName, dto.getFirstName());
        assertEquals(middleName, dto.getMiddleName());
        assertEquals(lastName, dto.getLastName());
        assertEquals(name, dto.getName());
        assertEquals(spid, dto.getSpid());
        assertEquals(catalystId, dto.getCatalystId());
        assertEquals(localId, dto.getLocalId());
        assertEquals(studyStatusName, dto.getStudyStatusName());
        assertEquals(irb, dto.getIrb());
        assertEquals(irbExpiration, dto.getIrbExpiration());
    }
}
