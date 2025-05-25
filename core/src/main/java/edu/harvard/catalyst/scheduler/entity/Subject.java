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

import edu.harvard.catalyst.scheduler.util.SubjectDataEncryptor;
import edu.harvard.catalyst.scheduler.web.BaseResource;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Entity
@Table(name = "subject")
public class Subject extends BaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;
    private String firstName;
    private String middleName;
    private String lastName;
    private String fullName;
    private Ethnicity ethnicity;
    private Date birthdate;
    private Race race;
    private Gender gender;
    private String genderEmpi;
    // This is a new attribute which will replace 'gender' over time
    private GenderType genderType;
    private String streetAddress1;
    private String streetAddress2;
    private String city;
    private State state;
    private String zip;
    private String comment;
    private Country country;
    private String primaryContactNumber;
    private String secondaryContactNumber;
    private Date createdDate;
    private boolean secure;
    private boolean active;
    private Date latestSsotRefresh;
    private String puid;

    // NOTE: we don't have a 'primary_subject' linking back to the subject we will be merging into
    // This information will be stored in the activity_log message details
    private ArchivalStatus archivalStatus;

    @BaseResource.Exclude
    private Set<SubjectMrn> subjectMrnSet  = new HashSet<>();

    public static Subject defensiveCopy(final Subject original) {
        final Subject copy = new Subject();

        copy.id = original.id;
        copy.firstName = original.firstName;
        copy.middleName = original.middleName;
        copy.lastName = original.lastName;
        copy.fullName = original.fullName;
        copy.ethnicity = original.ethnicity;
        copy.birthdate = original.birthdate;
        copy.race = original.race;
        copy.gender = original.gender;
        copy.genderEmpi = original.genderEmpi;
        copy.genderType = original.genderType;
        copy.streetAddress1 = original.streetAddress1;
        copy.streetAddress2 = original.streetAddress2;
        copy.city = original.city;
        copy.state = original.state;
        copy.zip = original.zip;
        copy.comment = original.comment;
        copy.country = original.country;
        copy.primaryContactNumber = original.primaryContactNumber;
        copy.secondaryContactNumber = original.secondaryContactNumber;
        copy.createdDate = original.createdDate;
        copy.secure = original.secure;
        copy.active = original.active;

        copy.latestSsotRefresh = original.latestSsotRefresh;

        copy.archivalStatus = original.archivalStatus;
        copy.subjectMrnSet = new HashSet<>(original.getSubjectMrnSet());

        copy.puid = original.puid;
        return copy;
    }

    public Subject() {
        super(null);
    }

    public Subject(String dummyValue) {
        super(0);

        this.race = new Race();
        this.gender = new Gender();
        this.genderType = GenderType.UNREPORTED;
        this.genderEmpi = dummyValue;
        this.ethnicity = new Ethnicity();
        this.state = new State();
        this.country = new Country();

        this.secure = false;
        this.active = false;

        this.birthdate = new Date();
        this.latestSsotRefresh = new Date();
        this.createdDate = new Date();

        this.firstName = dummyValue;
        this.middleName = dummyValue;
        this.lastName = dummyValue;
        this.fullName = dummyValue;
        this.streetAddress1 = dummyValue;
        this.streetAddress2 = dummyValue;
        this.city = dummyValue;
        this.zip = dummyValue;
        this.comment = dummyValue;
        this.primaryContactNumber = dummyValue;
        this.secondaryContactNumber = dummyValue;

        this.archivalStatus = null;
    }

    @Column(name = "first_name")
    @Basic(optional = false)
    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(final String firstName) {
        this.firstName = firstName;
    }

    @Column(name = "middle_name")
    public String getMiddleName() {
        return middleName;
    }

    public void setMiddleName(final String middleName) {
        this.middleName = middleName;
    }

    @Column(name = "last_name")
    @Basic(optional = false)
    public String getLastName() {
        return lastName;
    }

    public void setLastName(final String lastName) {
        this.lastName = lastName;
    }

    @Column(name = "full_name")
    public String getFullName() {
        return fullName;
    }

    public void setFullName(final String fullName) {
        this.fullName = fullName;
    }


    @Column(name = "birthdate")
    @Basic(optional = false)
    public Date getBirthdate() {
        return birthdate;
    }

    public void setBirthdate(final Date birthdate) {
        this.birthdate = birthdate;
    }

    @JoinColumn(name = "ethnicity", referencedColumnName = "id")
    @ManyToOne(optional = true)
    public Ethnicity getEthnicity() {
        return ethnicity;
    }

    public void setEthnicity(final Ethnicity ethnicity) {
        this.ethnicity = ethnicity;
    }

    @JoinColumn(name = "gender", referencedColumnName = "id")
    @ManyToOne(optional = false)
    @Basic(optional = false)
    public Gender getGender() {
        return gender;
    }

    public void setGender(final Gender gender) {
        this.gender = gender;
    }

    @Column(name = "gender_enum")
    @Basic(optional = false)
    @Enumerated(EnumType.STRING)
    public GenderType getGenderType() {
        return genderType;
    }

    public void setGenderType(final GenderType genderType) {
        this.genderType = genderType;
    }

    @Column(name = "gender_empi")
    public String getGenderEmpi() { return genderEmpi; }
    public void setGenderEmpi(final String genderEmpi) { this.genderEmpi = genderEmpi;}

    @JoinColumn(name = "race", referencedColumnName = "id")
    @ManyToOne(optional = true)
    public Race getRace() {
        return race;
    }

    public void setRace(final Race race) {
        this.race = race;
    }

    @Column(name = "primary_contact_number")
    @Basic(optional = true)
    public String getPrimaryContactNumber() {
        return primaryContactNumber;
    }

    public void setPrimaryContactNumber(final String primaryContactNumber) {
        this.primaryContactNumber = primaryContactNumber;
    }

    @Column(name = "secondary_contact_number")
    public String getSecondaryContactNumber() {
        return secondaryContactNumber;
    }

    public void setSecondaryContactNumber(final String secondaryContactNumber) {
        this.secondaryContactNumber = secondaryContactNumber;
    }

    @Column(name = "street_address1")
    public String getStreetAddress1() {
        return streetAddress1;
    }

    public void setStreetAddress1(final String streetAddress1) {
        this.streetAddress1 = streetAddress1;
    }

    @Column(name = "street_address2")
    public String getStreetAddress2() {
        return streetAddress2;
    }

    public void setStreetAddress2(final String streetAddress2) {
        this.streetAddress2 = streetAddress2;
    }

    @Column(name = "city")
    public String getCity() {
        return city;
    }

    public void setCity(final String city) {
        this.city = city;
    }

    @JoinColumn(name = "state", referencedColumnName = "id")
    @ManyToOne(optional = true)
    public State getState() {
        return state;
    }

    public void setState(final State state) {
        this.state = state;
    }

    @Column(name = "zip")
    public String getZip() {
        return zip;
    }

    public void setZip(final String zip) {
        this.zip = zip;
    }

    @JoinColumn(name = "country", referencedColumnName = "id")
    @ManyToOne(optional = true)
    public Country getCountry() {
        return country;
    }

    public void setCountry(final Country country) {
        this.country = country;
    }

    @Column(name = "created_date")
    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(final Date createdDate) {
        this.createdDate = createdDate;
    }

    @Column(name = "secure")
    public boolean getSecure() {
        return secure;
    }

    public void setSecure(final boolean secure) {
        this.secure = secure;
    }

    @Column(name = "active")
    public boolean getActive() {
        return active;
    }

    public void setActive(final boolean active) {
        this.active = active;
    }

    @Column(name = "comment")
    public String getComment() {
        return comment;
    }

    public void setComment(final String comment) {
        this.comment = comment;
    }

    @Column(name = "latest_ssot_refresh")
    public Date getLatestSsotRefresh() {
        return latestSsotRefresh;
    }

    public void setLatestSsotRefresh(Date latestSsotRefresh) {
        this.latestSsotRefresh = latestSsotRefresh;
    }

    @Column(name = "archival_status")
    @Basic(optional = true)
    @Enumerated(EnumType.STRING)
    public ArchivalStatus getArchivalStatus() {
        return archivalStatus;
    }

    public void setArchivalStatus(ArchivalStatus archivalStatus) {
        this.archivalStatus = archivalStatus;
    }

    @Column(name = "puid")
    public String getPuid() {
        return puid;
    }

    public void setPuid(String puid) {
        this.puid = puid;
    }

    @OneToMany(fetch = FetchType.EAGER, mappedBy = "subject")
    public Set<SubjectMrn> getSubjectMrnSet() {
        return subjectMrnSet;
    }

    public void setSubjectMrnSet(Set<SubjectMrn> subjectMrnSet) {
        this.subjectMrnSet = subjectMrnSet;
    }

    @Transient
    // there could be several sm's that match site, each with distinct mrn-code
    public Optional<SubjectMrn> getFirstSubjectMrn(String site) {
        Optional<SubjectMrn> result = Optional.ofNullable(null);

        if (getSubjectMrnSet() != null) {
            result = getSubjectMrnSet().stream()
                // if site is null just pick the first one (in 'steady state', i.e. after initial
                // migration, there should be no null sites)
                .filter(sm -> sm.getSite() == null || sm.getSite().equalsIgnoreCase(site))
                .findFirst();
        }

        return result;
    }

    @Transient
    public Set<SubjectMrn> getDecryptedSubjectMrnSet() {
        return getSubjectMrnSet().stream()
                .map(sm -> new SubjectMrn(
                        sm.getSubject(),
                        SubjectDataEncryptor.decrypt(sm.getMrn()),
                        sm.getSite(),
                        sm.getStatus(),
                        sm.getId()
                ))
                .collect(Collectors.toSet());
    }

    @Override
    public String toString() {
        return "Subject [id=" + id + ", getId()=" + getId() + "]";
    }


    public void setDerivedFullName() {
        setFullName(firstName + " " + lastName);
    }

    public boolean canShareResource() {

        return this.getGenderType().canShareResource();

    }

}
