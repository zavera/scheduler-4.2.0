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
import edu.harvard.catalyst.scheduler.dto.UserReportDTO;
import edu.harvard.catalyst.scheduler.entity.*;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * @author carl
 *
 */
public class UserReportDTOTest {
    private User u1;
    private User u2;
    
    @Before
    public void setup() {
        u1 = new User();

        u1.setFirstName("f1");
        u1.setMiddleName("m1");
        u1.setLastName("l1");
        u1.setActive(true);
        u1.setPrimaryPhone("p1");
        u1.setSecondaryPhone("s1");
        u1.setEmail("e1");

        Role role1 = new Role();
        role1.setName("r1");
        InstitutionRole instRole1 = new InstitutionRole();
        instRole1.setName("ir1");
        Department dept1 = new Department();
        dept1.setName("dept1");
        Division div1 = new Division();
        div1.setName("div1");
        Institution inst1 = new Institution();
        inst1.setName("i1");
        Credential cred1 = new Credential();
        cred1.setName("cred1");
        FacultyRank rank1 = new FacultyRank();
        rank1.setName("rank1");
        
        u1.setRole(role1);
        u1.setInstitutionRole(instRole1);
        u1.setDepartment(dept1);
        u1.setDivision(div1);
        u1.setInstitution(inst1);
        u1.setCredential(cred1);
        u1.setFacultyRank(rank1);
        
        u2 = new User();

        u2.setFirstName("f2");
        u2.setMiddleName("m2");
        u2.setLastName("l2");
        u2.setActive(true);
        u2.setPrimaryPhone("p2");
        u2.setSecondaryPhone("s2");
        u2.setEmail("e2");

        Role role2 = new Role();
        role2.setName("r2");
        InstitutionRole instRole2 = new InstitutionRole();
        instRole2.setName("ir2");
        Department dept2 = new Department();
        dept2.setName("dept2");
        Division div2 = new Division();
        div2.setName("div2");
        Institution inst2 = new Institution();
        inst2.setName("i2");
        Credential cred2 = new Credential();
        cred2.setName("cred2");
        FacultyRank rank2 = new FacultyRank();
        rank2.setName("rank2");

        u2.setRole(role2);
        u2.setInstitutionRole(instRole2);
        u2.setDepartment(dept2);
        u2.setDivision(div2);
        u2.setInstitution(inst2);
        u2.setCredential(cred2);
        u2.setFacultyRank(rank2);
    }

    @Test
    public void toCsvHeadersTest() {
        UserReportDTO dto1 = new UserReportDTO(u1);

        String result = dto1.toCsvHeaders();

        assertEquals("Full Name,User Title,Role,Department," +
                "Division,Institution,Status,Credential," +
                "Faculty Rank,Primary Phone," +
                "Secondary Phone,Email", result);
    }

    @Test
    public void toCsvRowsTest() {

        UserReportDTO dto1 = new UserReportDTO(u1);
        UserReportDTO dto2 = new UserReportDTO(u2);
        List<?> dtoList = Lists.newArrayList(dto1, dto2);

        List<String> threeRows = dto1.toCsvRows(dtoList);

        String expected1 = "\"f1 m1 l1\",\"r1\",\"ir1\",\"dept1\"," +
                "\"div1\",\"i1\",\"Active\",\"cred1\",\"rank1\"," +
                "\"p1\",\"s1\",\"e1\"\n";

        String expected2 = "\"f2 m2 l2\",\"r2\",\"ir2\",\"dept2\"," +
                "\"div2\",\"i2\",\"Active\",\"cred2\",\"rank2\"," +
                "\"p2\",\"s2\",\"e2\"\n";

        assertEquals(3, threeRows.size());
        
        assertEquals(expected1, threeRows.get(1));
        assertEquals(expected2, threeRows.get(2));
    }
}
