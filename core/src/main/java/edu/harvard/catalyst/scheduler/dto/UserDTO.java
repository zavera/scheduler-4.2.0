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

import java.lang.reflect.Type;
import java.util.List;

import edu.harvard.catalyst.scheduler.entity.BookedVisit;
import edu.harvard.catalyst.scheduler.entity.InstitutionRoleType;
import edu.harvard.catalyst.scheduler.entity.User;

import com.google.gson.Gson;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class UserDTO extends BooleanResultDTO implements JsonDeserializer<UserDTO> {
    private int id;
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
    private int division;
    private int department;
    private InstitutionRoleType institutionRoleType;
    private int roleId;
    private int institutionId;
    private int credential;
    private int facultyRank;
    private boolean active;
    private transient String salt;
    private int authStatus;
    private String previousLoginTime;
    private User user;
    private int userToDeactivateId;
    private int userToActivateId;
    private boolean generateNewPassword;
    private List<BookedVisit> bookedVisits;
    

    public UserDTO() {
    }


    public String getEcommonsId() {
        return this.ecommonsId;
    }

    public void setEcommonsId(String ecommonsId) {
        this.ecommonsId = ecommonsId;
    }

    public String getPassword() {
        return this.password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getNotificationEmail() {
		return notificationEmail;
	}


	public void setNotificationEmail(String notificationEmail) {
		this.notificationEmail = notificationEmail;
	}

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getMiddleName() {
        return middleName;
    }

    public void setMiddleName(String middleName) {
        this.middleName = middleName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getPrimaryPhone() {
        return primaryPhone;
    }

    public void setPrimaryPhone(String primaryPhone) {
        this.primaryPhone = primaryPhone;
    }

    public String getSecondaryPhone() {
        return secondaryPhone;
    }

    public void setSecondaryPhone(String secondaryPhone) {
        this.secondaryPhone = secondaryPhone;
    }

    public String getFax() {
        return fax;
    }

    public void setFax(String fax) {
        this.fax = fax;
    }

    public String getPager() {
        return pager;
    }

    public void setPager(String pager) {
        this.pager = pager;
    }

    public int getDivision() {
        return division;
    }

    public void setDivision(int division) {
        this.division = division;
    }

    public int getDepartment() {
        return department;
    }

    public void setDepartment(int department) {
        this.department = department;
    }

    public InstitutionRoleType getInstitutionRoleType() {
        return institutionRoleType;
    }

    public void setInstitutionRoleType(InstitutionRoleType institutionRoleType) {
        this.institutionRoleType = institutionRoleType;
    }

    public int getRoleId() {
        return roleId;
    }

    public void setRoleId(int roleId) {
        this.roleId = roleId;
    }

    public int getInstitutionId() {
        return institutionId;
    }

    public void setInstitutionId(int institutionId) {
        this.institutionId = institutionId;
    }

    public int getCredential() {
        return credential;
    }

    public void setCredential(int credential) {
        this.credential = credential;
    }

    public int getFacultyRank() {
        return facultyRank;
    }

    public void setFacultyRank(int facultyRank) {
        this.facultyRank = facultyRank;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getSalt() {
        return salt;
    }

    public void setSalt(String salt) {
        this.salt = salt;
    }

    public int getAuthStatus() {
        return authStatus;
    }

    public void setAuthStatus(int authStatus) {
        this.authStatus = authStatus;
    }

    public String getPreviousLoginTime() {
        return previousLoginTime;
    }

    public void setPreviousLoginTime(String previousLoginTime) {
        this.previousLoginTime = previousLoginTime;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public int getUserToDeactivateId() {
        return userToDeactivateId;
    }

    public void setUserToDeactivateId(int userToDeactivateId) {
        this.userToDeactivateId = userToDeactivateId;
    }

    public int getUserToActivateId() {
        return userToActivateId;
    }

    public void setUserToActivateId(int userToActivateId) {
        this.userToActivateId = userToActivateId;
    }


    public boolean isGenerateNewPassword() {
        return generateNewPassword;
    }

    public void setGenerateNewPassword(boolean generateNewPassword) {
        this.generateNewPassword = generateNewPassword;
    }


    public List<BookedVisit> getBookedVisits() {
        return bookedVisits;
    }


    public void setBookedVisits(List<BookedVisit> bookedVisits) {
        this.bookedVisits = bookedVisits;
    }


    /* (non-Javadoc)
     * @see com.google.gson.JsonDeserializer#deserialize(com.google.gson.JsonElement, java.lang.reflect.Type, com.google.gson.JsonDeserializationContext)
     */
    public UserDTO deserialize(JsonElement json, Type typeOfT,
            JsonDeserializationContext context) {
        Gson gson = new Gson();
        UserDTO dto = gson.fromJson(json.toString(), UserDTO.class);

        JsonObject obj = (JsonObject) json;
        dto.setPassword(obj.get("password") != null ? obj.get("password").getAsString() : "");
        return dto;
    }

}
