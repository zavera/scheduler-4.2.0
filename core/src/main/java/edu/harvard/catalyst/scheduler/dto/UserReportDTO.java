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
package edu.harvard.catalyst.scheduler.dto;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import edu.harvard.catalyst.scheduler.entity.User;

import java.util.List;

import static edu.harvard.catalyst.scheduler.util.MiscUtil.*;

public class UserReportDTO implements CsvAbleDTO {

    private String firstName;
    private String middleName;
    private String lastName;

    private String role;
    private String institutionRole;
    private String department;
    private String division;
    private String institution;
    private String credential;
    private String facultyRank;
    private String primaryPhone;
    private String secondaryPhone;
    private String email;
    private String active;
;
    public UserReportDTO(User u) {
        firstName = u.getFirstName();
        middleName = u.getMiddleName();
        lastName = u.getLastName();
        active = String.valueOf(u.getActive());
        primaryPhone = u.getPrimaryPhone();
        secondaryPhone = u.getSecondaryPhone();
        email = u.getEmail();

        if (u.getRole() != null) {
            role = u.getRole().getName();
        }
        if (u.getInstitutionRole() != null) {
            institutionRole = u.getInstitutionRole().getName();
        }
        if (u.getDepartment() != null) {
            department = u.getDepartment().getName();
        }
        if (u.getDivision() != null) {
            division = u.getDivision().getName();
        }
        if (u.getInstitution() != null) {
            institution = u.getInstitution().getName();
        }
        if (u.getCredential() != null) {
            credential = u.getCredential().getName();
        }
        if (u.getFacultyRank() != null) {
            facultyRank = u.getFacultyRank().getName();
        }
    }

    @Override
    public String toCsvHeaders() {
        
        return "Full Name,User Title,Role,Department," +
                "Division,Institution,Status,Credential," +
                "Faculty Rank,Primary Phone," +
                "Secondary Phone,Email";
    }
    @Override
    public List<String> toCsvRows(List<?> dtoList) {
        List<String> result = Lists.newArrayList();
        result.add(toCsvHeaders() + "\n");

        for (Object object: dtoList) {
            UserReportDTO u = (UserReportDTO) object;
            
            List<String> columns = Lists.newArrayList();

            columns.add(q(fullName(u.firstName, u.middleName, u.lastName)));
            columns.add(q(u.role));
            columns.add(q(u.institutionRole));
            columns.add(q(u.department));
            columns.add(q(u.division));
            columns.add(q(u.institution));
            columns.add(q(booleanToStatusValue(u.active)));
            columns.add(q(u.credential));
            columns.add(q(u.facultyRank));
            columns.add(q(u.primaryPhone));
            columns.add(q(u.secondaryPhone));
            columns.add(q(u.email));
            
            String rows = Joiner.on(",").join(columns);
            result.add(rows + "\n");
        }
        return result;
    }
}
