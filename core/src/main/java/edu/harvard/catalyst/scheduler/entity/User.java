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
package edu.harvard.catalyst.scheduler.entity;

import org.springframework.util.StringUtils;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "user")
public class User extends BaseEntity implements Serializable, HasFirstName, HasLastName, HasEmail {

    public static final int STATUS_AUTHORIZED = 1;
    public static final int STATUS_NOT_FOUND = 0;
    public static final int STATUS_INVALID_PASSWORD = -1;
    public static final int STATUS_INACTIVE = -2;

    private static final long serialVersionUID = 8014584895711544530L;
    private String ecommonsId;
    private transient String password;
    private String firstName;
    private String middleName;
    private String lastName;
    private String primaryPhone;
    private String secondaryPhone;
    private String email;
    private String notificationEmail;
    private String fax;
    private String pager;
    private Division division;
    private Department department;
    private Institution institution;
    private InstitutionRole institutionRole;
    private Role role;
    private Credential credential;
    private FacultyRank facultyRank;
    private boolean active;
    private transient String salt;
    private int authStatus;
    private String sessionId;
    private Date lastLoginTime;
    private String previousLoginTime;

    private List<BookedVisit> bookedVisits;

    public User() {
        super(null);
    }

    @Column(name = "ecommons_id")
    @Basic(optional = false)
    public String getEcommonsId() {
        return ecommonsId;
    }

    public void setEcommonsId(String ecommonsId) {
        this.ecommonsId = ecommonsId;
    }

    @Column(name = "password")
    @Basic(optional = false)
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Column(name = "first_name")
    @Basic(optional = false)
    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    @Column(name = "middle_name")
    public String getMiddleName() {
        return middleName;
    }

    public void setMiddleName(String middleName) {
        this.middleName = middleName;
    }

    @Column(name = "last_name")
    @Basic(optional = false)
    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    @Column(name = "primary_phone")
    public String getPrimaryPhone() {
        return primaryPhone;
    }

    public void setPrimaryPhone(String primaryPhone) {
        this.primaryPhone = primaryPhone;
    }

    @Column(name = "secondary_phone")
    public String getSecondaryPhone() {
        return secondaryPhone;
    }

    public void setSecondaryPhone(String secondaryPhone) {
        this.secondaryPhone = secondaryPhone;
    }

    @Column(name = "email")
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Column(name = "fax")
    public String getFax() {
        return fax;
    }

    public void setFax(String fax) {
        this.fax = fax;
    }

    @Column(name = "pager")
    public String getPager() {
        return pager;
    }

    public void setPager(String pager) {
        this.pager = pager;
    }

    @JoinColumn(name = "division", referencedColumnName = "id")
    @ManyToOne(optional = true)
    public Division getDivision() {
        return division;
    }

    public void setDivision(Division division) {
        this.division = division;
    }

    @JoinColumn(name = "department", referencedColumnName = "id")
    @ManyToOne(optional = true)
    public Department getDepartment() {
        return department;
    }

    public void setDepartment(Department department) {
        this.department = department;
    }

    @JoinColumn(name = "credential", referencedColumnName = "id")
    @ManyToOne(optional = true)
    public Credential getCredential() {
        return credential;
    }

    public void setCredential(Credential credential) {
        this.credential = credential;
    }

    @JoinColumn(name = "faculty_rank", referencedColumnName = "id")
    @ManyToOne(optional = true)
    public FacultyRank getFacultyRank() {
        return facultyRank;
    }

    public void setFacultyRank(FacultyRank facultyRank) {
        this.facultyRank = facultyRank;
    }

    @JoinColumn(name = "institution", referencedColumnName = "id")
    @ManyToOne(optional = false)
    public Institution getInstitution() {
        return institution;
    }

    public void setInstitution(Institution institution) {
        this.institution = institution;
    }

    @JoinColumn(name = "institution_role", referencedColumnName = "id")
    @ManyToOne(optional = false)
    public InstitutionRole getInstitutionRole() {
        return institutionRole;
    }

    public void setInstitutionRole(InstitutionRole institutionRole) {
        this.institutionRole = institutionRole;
    }

    @JoinColumn(name = "role", referencedColumnName = "id")
    @ManyToOne(optional = false)
    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    @Column(name = "active")
    public boolean getActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    @Column(name = "salt")
    @Basic(optional = false)
    public String getSalt() {
        return salt;
    }

    public void setSalt(String salt) {
        this.salt = salt;
    }

    @Column(name = "last_login_time")
    public Date getLastLoginTime() {
        return lastLoginTime;
    }

    public void setLastLoginTime(Date lastLoginTime) {
        this.lastLoginTime = lastLoginTime;
    }

    @Transient
    public Integer getAuthStatus() {
        return authStatus;
    }

    public void setAuthStatus(Integer authStatus) {
        this.authStatus = authStatus;
    }

    @Transient
    public String getPreviousLoginTime() {
        return previousLoginTime;
    }

    public void setPreviousLoginTime(String previousLoginTime) {
        this.previousLoginTime = previousLoginTime;
    }

    @Transient
    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    @Transient
    public List<BookedVisit> getBookedVisits() {
        return bookedVisits;
    }

    public void setBookedVisits(List<BookedVisit> bookedVisits) {
        this.bookedVisits = bookedVisits;
    }

    @Override
    public String toString() {
        return "User [id=" + id + ", getId()=" + getId() + "]";
    }

    @Transient 
    public String getPreferredNotificationEmail(){
        if(!StringUtils.hasText(this.getNotificationEmail())){
            return this.email;
        } 
        return notificationEmail;    	
    }

    @Column(name = "notification_email")
    public String getNotificationEmail() {
        return notificationEmail;
    }

    public void setNotificationEmail(String notificationEmail) {
        this.notificationEmail = notificationEmail;
    }

    @Transient
    public boolean isStudyStaff() {
        return InstitutionRole.isStudyStaff(getInstitutionRole());
    }
}
