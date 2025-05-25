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
import edu.harvard.catalyst.scheduler.entity.Subject;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;


/**
 * User: ankit
 * Date: 3/14/14
 * Time: 1:36 PM
 */
public class SubjectDetailResponse {

    private String empiFailoverMessage;

    private Integer id;
    private String partnersUid;
    private String firstName;
    private String lastName;
    private String middleName;
    private Integer genderId;
    private String genderCode;
    private String genderName;
    private Integer raceId;
    private String raceName;
    private Integer ethnicityId;
    private String ethnicityName;
    private Date birthdate;
    private List<MrnInfoDTO> mrnInfoList;
    private boolean active;


    private String comment;
    private Address address;
    private String primaryContactNumber;
    private String secondaryContactNumber;

    public SubjectDetailResponse()
    {
       // use setters right after construction

        this.address = new Address();
        this.mrnInfoList = new ArrayList<>();
    }

    public SubjectDetailResponse(Subject subject) {
        if (subject == null) {
            SchedulerRuntimeException.logAndThrow("Subject parameter should be non-null");
        }

        this.id = subject.getId();
        this.partnersUid = subject.getPuid();
        this.firstName = subject.getFirstName();
        this.lastName = subject.getLastName();
        this.middleName = subject.getMiddleName();

        if(subject.getGender() != null) {
            this.genderId = subject.getGender().getId();
            this.genderName = subject.getGender().getName();
        }

        if(subject.getRace() != null)
        {
            this.raceId = subject.getRace().getId();
            this.raceName = subject.getRace().getName();
        }
        else
        {
            this.raceId = 0;
            this.raceName = "";
        }

        if(subject.getEthnicity() != null)
        {
            this.ethnicityId = subject.getEthnicity().getId();
            this.ethnicityName = subject.getEthnicity().getName();
        }
        else
        {
            this.ethnicityId = 0;
            this.ethnicityName = "";
        }

        this.birthdate = subject.getBirthdate();

        mrnInfoList = subject.getSubjectMrnSet().stream()
                .map(sm -> new MrnInfoDTO(
                        sm.getMrn(),
                        sm.getSite(),
                        sm.getStatus()
                )) .collect(Collectors.toList());

        this.comment = subject.getComment();

        Address address = new Address();
        address.setAddressLine1(subject.getStreetAddress1());
        address.setAddressLine2(subject.getStreetAddress2());
        address.setCity(subject.getCity());

        if (subject.getState() != null) {
            address.setStateId(subject.getState().getId());
            address.setStateName(subject.getState().getName());
        }

        if(subject.getCountry() != null) {
            address.setCountryId(subject.getCountry().getId());
            address.setCountryName(subject.getCountry().getName());
        }
        address.setZipCode(subject.getZip());

        this.address = address;

        this.primaryContactNumber = subject.getPrimaryContactNumber();
        this.secondaryContactNumber = subject.getSecondaryContactNumber();
        this.active = subject.getActive();
    }

    public SubjectDetailResponse(String dummyValue) {

        this.id = 0;
        this.genderId = 0;
        this.raceId = 0;
        this.ethnicityId = 0;

        this.birthdate = new Date();
        this.partnersUid = dummyValue;
        this.firstName = dummyValue;
        this.lastName = dummyValue;
        this.middleName = dummyValue;
        this.genderName = dummyValue;
        this.raceName = dummyValue;
        this.ethnicityName = dummyValue;
        mrnInfoList = new ArrayList<MrnInfoDTO>();
        mrnInfoList.add(new MrnInfoDTO(dummyValue));

        this.comment = dummyValue;

        Address address = new Address();
        this.address = address;
        address.setAddressLine1(dummyValue);
        address.setAddressLine2(dummyValue);
        address.setCity(dummyValue);
        address.setStateId(0);
        address.setStateName(dummyValue);
        address.setCountryName(dummyValue);
        address.setCountryId(0);

        address.setZipCode(dummyValue);
        this.primaryContactNumber = dummyValue;
        this.secondaryContactNumber = dummyValue;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getId()
    {
        return id;
    }

    public void setPartnersUid(String partnersUid) {
        this.partnersUid = partnersUid;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public void setMiddleName(String middleName) {
        this.middleName = middleName;
    }

    public void setGenderId(int genderId) {
        this.genderId = genderId;
    }

    public int getGenderId() {
        return genderId;
    }

    public void setGenderName(String genderName) {
        this.genderName = genderName;
    }

    public String getGenderName(String genderName) {
        return genderName;
    }

    public void setGenderCode(String genderCode) {
        this.genderCode = genderCode;
    }

    public String getGenderCode(String genderCode) {
        return genderCode;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setRaceId(int raceId) {
        this.raceId = raceId;
    }

    public void setRaceName(String raceName) {
        this.raceName = raceName;
    }

    public void setEthnicityId(int ethnicityId) {
        this.ethnicityId = ethnicityId;
    }

    public void setEthnicityName(String ethnicityName) {
        this.ethnicityName = ethnicityName;
    }

    public String getEthnicityName() {
        return ethnicityName;
    }

    public void setBirthdate(Date birthdate) {
        this.birthdate = birthdate;
    }

    public void setMrnInfoList(List<MrnInfoDTO> mrnInfoList) {
        this.mrnInfoList = mrnInfoList;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public boolean isActive() {
        return active;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getComment()
    {
        return comment;
    }
    public void setStreetAddress1(String streetAddress1) {
        this.address.setAddressLine1(streetAddress1);
    }

    public void setStreetAddress2(String streetAddress2) {
        this.address.setAddressLine2(streetAddress2);
    }

    public void setCity(String city) {
        this.address.setCity(city);
    }

    public void setStateId(int stateId) {
        this.address.setStateId(stateId);
    }

    public void setStateName(String stateName) {
        this.address.setStateName(stateName);
    }

    public String getStateName(){ return this.address.getStateName();}

    public void setCountryId(int countryId) {
        this.address.setCountryId(countryId);
    }

    public void setCountryName(String countryName) {
        this.address.setCountryName(countryName);
    }

    public void setZip(String zip) {
        this.address.setZipCode(zip);
    }

    public void setPrimaryContactNumber(String primaryContactNumber) {
        this.primaryContactNumber = primaryContactNumber;
    }

    public void setSecondaryContactNumber(String secondaryContactNumber) {
        this.secondaryContactNumber = secondaryContactNumber;
    }
    public String getPrimaryContactNumber() {
        return primaryContactNumber;
    }

    public String getSecondaryContactNumber() {
        return secondaryContactNumber;
    }

    public List<MrnInfoDTO> getMrnInfoList() {
        return mrnInfoList;
    }

    public String getGenderName()
    {
        return this.genderName;
    }

    public String getEmpiFailoverMessage() {
        return empiFailoverMessage;
    }

    public void setEmpiFailoverMessage(String empiFailoverMessage) {
        this.empiFailoverMessage = empiFailoverMessage;
    }
}
