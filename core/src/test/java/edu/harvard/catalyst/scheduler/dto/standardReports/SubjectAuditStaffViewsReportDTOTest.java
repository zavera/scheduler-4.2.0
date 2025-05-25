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
import edu.harvard.catalyst.scheduler.dto.SubjectAuditStaffViewsReportDTO;
import edu.harvard.catalyst.scheduler.entity.Department;
import edu.harvard.catalyst.scheduler.entity.Institution;
import edu.harvard.catalyst.scheduler.entity.Role;
import edu.harvard.catalyst.scheduler.entity.User;
import org.junit.Test;

import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * @author carl
 *
 */
public class SubjectAuditStaffViewsReportDTOTest {

    @Test
    public void toCsvHeadersTest() {
        SubjectAuditStaffViewsReportDTO dto1 = new SubjectAuditStaffViewsReportDTO();

        String result = dto1.toCsvHeaders();

        assertEquals("Subject Name,User,Title,Department,Institution," +
                "Action Performed," +
                "Date,IP Address", result);
    }

    @Test
    public void toCsvRowsTest() {

        SubjectAuditStaffViewsReportDTO dto1 = new SubjectAuditStaffViewsReportDTO();
        SubjectAuditStaffViewsReportDTO dto2 = new SubjectAuditStaffViewsReportDTO();
        List<?> dtoList = Lists.newArrayList(dto1, dto2);

        User user1 = new User();
        Role role1 = new Role();
        Department dept1 = new Department();
        Institution inst1 = new Institution();
        role1.setName("role1");
        dept1.setName("dept1");
        inst1.setName("inst1");
        user1.setFirstName("uf1");
        user1.setMiddleName("um1");
        user1.setLastName("ul1");
        user1.setRole(role1);
        user1.setDepartment(dept1);
        user1.setInstitution(inst1);
        dto1.setUserId(user1);

        Date date1 = new Date(100120000);

        dto1.setSubjectId(1);
        dto1.setSubjectName("subj1");
        dto1.setActionPerformed("act1");
        dto1.setDate(date1);
        dto1.setIpAddress("ip1");

        User user2 = new User();
        Role role2 = new Role();
        Department dept2 = new Department();
        Institution inst2 = new Institution();
        role2.setName("role2");
        dept2.setName("dept2");
        inst2.setName("inst2");
        user2.setFirstName("uf2");
        user2.setMiddleName("um2");
        user2.setLastName("ul2");
        user2.setRole(role2);
        user2.setDepartment(dept2);
        user2.setInstitution(inst2);
        dto2.setUserId(user2);

        Date date2 = new Date(200220000);

        dto2.setSubjectId(2);
        dto2.setSubjectName("subj2");
        dto2.setActionPerformed("act2");
        dto2.setDate(date2);
        dto2.setIpAddress("ip2");

        List<String> threeRows = dto1.toCsvRows(dtoList);

        String expected1 = "\"subj1\",\"uf1 um1 ul1\",\"role1\",\"dept1\",\"inst1\",\"act1\",\"01/01/1970 22:48\",\"ip1\"\n";

        String expected2 = "\"subj2\",\"uf2 um2 ul2\",\"role2\",\"dept2\",\"inst2\",\"act2\",\"01/03/1970 02:37\",\"ip2\"\n";

        assertEquals(4, threeRows.size());
        
        assertEquals(expected1, threeRows.get(1));
        assertEquals(expected2, threeRows.get(3));
    }
}
