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
package edu.harvard.catalyst.scheduler.dto.response;

import edu.harvard.catalyst.scheduler.core.SchedulerRuntimeException;
import edu.harvard.catalyst.scheduler.entity.InstitutionRoleType;
import edu.harvard.catalyst.scheduler.entity.User;


/**
 * User: ankit
 * Date: 4/23/14
 * Time: 2:13 PM
 */
public class UserDetailResponse {

    private Integer id;
    private String lastName;
    private String firstName;
    private String ecommonsId;
    private String middleName;
    private String primaryPhone;
    private String secondaryPhone;
    private String email;
    private String notificationEmail;
    private String fax;
    private String pager;
    private String division;
    private Integer divisionId;
    private String department;
    private Integer departmentId;
    private String institution;
    private Integer institutionId;
    private String institutionRole;
    private Integer institutionRoleId;
    private InstitutionRoleType institutionRoleType;
    private String role;
    private Integer roleId;
    private String credential;
    private Integer credentialId;
    private String facultyRank;
    private Integer facultyRankId;


    public UserDetailResponse(User user) {
        if (user == null) {
            SchedulerRuntimeException.logAndThrow("user parameter should be non-null");
        }

        this.id = user.getId();
        this.lastName = user.getLastName();
        this.firstName = user.getFirstName();
        this.ecommonsId = user.getEcommonsId();
        this.middleName = user.getMiddleName();
        this.primaryPhone = user.getPrimaryPhone();
        this.secondaryPhone = user.getSecondaryPhone();
        this.email = user.getEmail();
        this.notificationEmail = user.getNotificationEmail();
        this.fax = user.getFax();
        this.pager = user.getPager();
        if (user.getDivision() != null) {
            this.division = user.getDivision().getName();
            this.divisionId = user.getDivision().getId();
        }
        if (user.getDepartment() != null) {
            this.department = user.getDepartment().getName();
            this.departmentId = user.getDepartment().getId();
        }
        if (user.getInstitution() != null) {
            this.institution = user.getInstitution().getName();
            this.institutionId = user.getInstitution().getId();
        }
        if (user.getInstitutionRole() != null) {
            this.institutionRole = user.getInstitutionRole().getName();
            this.institutionRoleId = user.getInstitutionRole().getId();
            this.institutionRoleType = user.getInstitutionRole().getType();
        }
        if (user.getRole() != null) {
            this.role = user.getRole().getName();
            this.roleId = user.getRole().getId();
        }
        if (user.getCredential() != null) {
            this.credential = user.getCredential().getName();
            this.credentialId = user.getCredential().getId();
        }
        if (user.getFacultyRank() != null) {
            this.facultyRank = user.getFacultyRank().getName();
            this.facultyRankId = user.getFacultyRank().getId();
        }
    }      
}
