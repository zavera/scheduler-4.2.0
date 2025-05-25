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

import java.util.Date;
import java.util.List;

import edu.harvard.catalyst.scheduler.dto.response.MrnInfoDTO;
import edu.harvard.catalyst.scheduler.entity.Subject;


public class SubjectsDTO extends BooleanResultDTO {

    private int id;
    private String partnersUid;
    private Date birthdate;
    private String birthday;
    private String mrn;
    private String firstName;
    private String middleName;
    private String lastName;
    private String fullName;
    private String nameSuffix;
    private int ethnicity;
    private String ethnicityName;
    private int race;
    private int gender;
    private String genderEmpi;
    private String streetAddress1;
    private String streetAddress2;
    private int country;
    private String city;
    private int state;
    private String zip;
    private String comment;
    private String primaryContactNumber;
    private String secondaryContactNumber;
    private Date createdDate;
    private boolean secure;
    private boolean active;
    private boolean mrnAlreadyExistsError;
    private boolean similarMrnAlreadyExistsError;
    private MrnInfoDTO similarMrnInfo;
    private int userId;
    private int studyId;
    private int subjectToDeactivateId;
    private int subjectToActivateId;
    private int subjectToEditId;
    private List<MrnInfoDTO> mrnInfoDTOList;
    private boolean mergeSimilarMrn;

    String nullifyIfBlank(String input) {
        String result = input;

        if (input != null && input.isEmpty()) {
            result = null;
        }

        return result;
    }
    public void nullifyBlanks() {
        middleName              = nullifyIfBlank(middleName            );
        fullName                = nullifyIfBlank(fullName              );
        nameSuffix              = nullifyIfBlank(nameSuffix            );
        ethnicityName           = nullifyIfBlank(ethnicityName         );
        streetAddress1          = nullifyIfBlank(streetAddress1        );
        streetAddress2          = nullifyIfBlank(streetAddress2        );
        city                    = nullifyIfBlank(city                  );
        zip                     = nullifyIfBlank(zip                   );
        comment                 = nullifyIfBlank(comment               );
        primaryContactNumber    = nullifyIfBlank(primaryContactNumber  );
        secondaryContactNumber  = nullifyIfBlank(secondaryContactNumber);

    }

    public MrnInfoDTO getMrnInfo() {
        return mrnInfo;
    }

    public void setMrnInfo(MrnInfoDTO mrnInfoDTO) {
        this.mrnInfo = mrnInfoDTO;
    }

    private MrnInfoDTO mrnInfo;

    public List<String> getGenderList() {
        return genderList;
    }

    public void setGenderList(List<String> genderList) {
        this.genderList = genderList;
    }

    private List<String> genderList;
    
    private List<Subject> searchSubjects;
    
    public SubjectsDTO() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Date getBirthdate() {
        return birthdate;
    }

    public void setBirthdate(Date birthdate) {
        this.birthdate = birthdate;
    }

    public String getMrn() {
        return mrn;
    }
    public void setMrn(String mrn) {
        this.mrn = mrn;
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
    
    public String getSuffixName() {
        return fullName;
    }
    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getNameSuffix() {
        return nameSuffix;
    }
    public void setNameSuffix(String nameSuffix) {
        this.nameSuffix = nameSuffix;
    }
    
    public int getEthnicity() {
        return ethnicity;
    }
    public void setEthnicity(int ethnicity) {
        this.ethnicity = ethnicity;
    }

    public int getRace() {
        return race;
    }
    public void setRace(int race) {
        this.race = race;
    }

    public int getGender() {
        return gender;
    }
    public void setGender(int gender) {
        this.gender = gender;
    }

    public String getStreetAddress1() {
        return streetAddress1;
    }

    public void setStreetAddress1(String streetAddress1) {
        this.streetAddress1 = streetAddress1;
    }

    public String getStreetAddress2() {
        return streetAddress2;
    }

    public void setStreetAddress2(String streetAddress2) {
        this.streetAddress2 = streetAddress2;
    }

    public int getCountry() {
        return country;
    }

    public void setCountry(int country) {
        this.country = country;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public String getZip() {
        return zip;
    }

    public void setZip(String zip) {
        this.zip = zip;
    }

    public String getPrimaryContactNumber() {
        return primaryContactNumber;
    }

    public void setPrimaryContactNumber(String primaryContactNumber) {
        this.primaryContactNumber = primaryContactNumber;
    }

    public String getSecondaryContactNumber() {
        return secondaryContactNumber;
    }

    public void setSecondaryContactNumber(String secondaryContactNumber) {
        this.secondaryContactNumber = secondaryContactNumber;
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }

    public Boolean getSecure() {
        return secure;
    }

    public void setSecure(Boolean secure) {
        this.secure = secure;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }


    public boolean isMrnAlreadyExistsError() {
        return mrnAlreadyExistsError;
    }

    public void setMrnAlreadyExistsError(boolean mrnAlreadyExistsError) {
        this.mrnAlreadyExistsError = mrnAlreadyExistsError;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getSubjectToDeactivateId() {
        return subjectToDeactivateId;
    }

    public void setSubjectToDeactivateId(int subjectToDeactivateId) {
        this.subjectToDeactivateId = subjectToDeactivateId;
    }

    public void setSecure(boolean secure) {
        this.secure = secure;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public int getSubjectToActivateId() {
        return subjectToActivateId;
    }

    public void setSubjectToActivateId(int subjectToActivateId) {
        this.subjectToActivateId = subjectToActivateId;
    }
    

    public int getSubjectToEditId() {
        return subjectToEditId;
    }

    public void setSubjectToEditId(int subjectToEditId) {
        this.subjectToEditId = subjectToEditId;
    }

    public List<Subject> getSearchSubjects() {
        return searchSubjects;
    }

    public void setSearchSubjects(List<Subject> searchSubjects) {
        this.searchSubjects = searchSubjects;
    }

    public String getBirthday() {
        return birthday;
    }

    public void setBirthday(String birthday) {
        this.birthday = birthday;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public int getStudyId() {
        return studyId;
    }

    public void setStudyId(int studyId) {
        this.studyId = studyId;
    }

    public List<MrnInfoDTO> getMrnInfoDTOList() {
        return mrnInfoDTOList;
    }

    public void setMrnInfoDTOList(List<MrnInfoDTO> mrnInfoDTOList) {
        this.mrnInfoDTOList = mrnInfoDTOList;
    }

    public String getGenderEmpi() {
        return genderEmpi;
    }

    public void setGenderEmpi(String genderEmpi) {
        this.genderEmpi = genderEmpi;
    }

    public String getEthnicityName() {
        return ethnicityName;
    }

    public void setEthnicityName(String ethnicityName) {
        this.ethnicityName = ethnicityName;
    }

    public String getPartnersUid() { return partnersUid; }

    public void setPartnersUid(String partnersUid) { this.partnersUid = partnersUid; }

    public boolean mergeSimilarMrn() {
        return mergeSimilarMrn;
    }

    public void setMergeSimilarMrn(boolean mergeSimilarMrn) {
        this.mergeSimilarMrn = mergeSimilarMrn;
    }

    public boolean isSimilarMrnAlreadyExistsError() {
        return similarMrnAlreadyExistsError;
    }

    public void setSimilarMrnAlreadyExistsError(boolean similarMrnAlreadyExistsError) {
        this.similarMrnAlreadyExistsError = similarMrnAlreadyExistsError;
    }

    public MrnInfoDTO getSimilarMrnInfo() {
        return similarMrnInfo;
    }

    public void setSimilarMrnInfo(MrnInfoDTO similarMrnInfo) {
        this.similarMrnInfo = similarMrnInfo;
    }
}
