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

import edu.harvard.catalyst.scheduler.dto.Epic.EmpiSubjectDto;
import edu.harvard.catalyst.scheduler.util.DateUtility;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * User: ankit
 * Date: 3/5/14
 * Time: 8:02 AM
 */
public class SubjectsResponseDTO {
    private Long totalCount;

    private String logMessage;
    private String empiFailoverMessage;

    List<SubjectsResponseDTO> subjectList;

    public SubjectsResponseDTO(List<SubjectsResponseDTO> responseList, Long totalCount) {
        this.totalCount = totalCount;
        this.subjectList = responseList;
    }

    private int id;
    private int partnersUid;
    private String lastName;
    private String firstName;
    private String middleName;
    private String nameSuffix;
    private Date dob;
    private String gender;
    private Address address;
    private String primaryContact;
    private boolean active;

    public SubjectsResponseDTO(int id, String lastName, String firstName, String middleName, Date dob,
                               String gender, Address address, String primaryContact, boolean active) {
        this.id = id;
        this.lastName = lastName;
        this.firstName = firstName;
        this.middleName = middleName;
        this.nameSuffix = nameSuffix;
        this.dob = dob;
        this.gender = gender;
        this.address = address;
        this.primaryContact = primaryContact;
        this.active = active;
    }

    static String inactiveStatus = "inactive";

    public static SubjectsResponseDTO createSubjectsResponseDTO(EmpiSubjectDto empiSubjectDto) {

        List<SubjectsResponseDTO> subjectsResponseDTOList = new ArrayList();

        //Need a check for null patients
        if(empiSubjectDto.getPatients() != null)
        {
            subjectsResponseDTOList =
                    empiSubjectDto.getPatients().getPatientList().stream()
                            .map(p -> new SubjectsResponseDTO(p))
                            .collect(Collectors.toList());
        }
        SubjectsResponseDTO result = new SubjectsResponseDTO(subjectsResponseDTOList, Long.valueOf(subjectsResponseDTOList.size()));

        return result;
    }

    public SubjectsResponseDTO(EmpiSubjectDto.Patient subject) {
        this.id = subject.getUid();
        this.partnersUid = subject.getUid();
        this.lastName = subject.getName().getLast();
        this.firstName = subject.getName().getFirst();
        this.middleName = subject.getName().getMiddleInitial();
        this.nameSuffix = subject.getName().getSuffix();
        this.dob = DateUtility.parse(DateUtility.monthDayYear(), subject.getDobString());
        this.gender = subject.getGender();


        Address address = new Address();
        if(subject.getAddress() != null)
        {
            address = new Address(subject.getAddress().getLine1(), subject.getAddress().getLine2(),
                    subject.getAddress().getCity(), subject.getAddress().getState(), subject.getAddress().getZipAsString(),
                    subject.getAddress().getCountry());
        }

        this.address = address;

        if(subject.getPhones() != null && subject.getPhones().getPhoneList() != null
                && !subject.getPhones().getPhoneList().isEmpty())
        {
            this.primaryContact = subject.getPhones().getPhoneList().get(0).getNumber();
        }
        this.active = subject.getStatus() != inactiveStatus;
    }

    public static final Comparator<SubjectsResponseDTO> SubjectLastNameComparatorAsc = (o1, o2) -> o1.lastName.compareToIgnoreCase(o2.lastName);

    public static final Comparator<SubjectsResponseDTO> SubjectLastNameComparatorDesc = (o1, o2) -> o2.lastName.compareToIgnoreCase(o1.lastName);

    public static final Comparator<SubjectsResponseDTO> SubjectFirstNameComparatorAsc = (o1, o2) -> o1.firstName.compareToIgnoreCase(o2.firstName);

    public static final Comparator<SubjectsResponseDTO> SubjectFirstNameComparatorDesc = (o1, o2) -> o2.firstName.compareToIgnoreCase(o1.firstName);

    public static final Comparator<SubjectsResponseDTO> SubjectContactComparatorAsc = (o1, o2) -> o1.primaryContact.compareToIgnoreCase(o2.primaryContact);

    public static final Comparator<SubjectsResponseDTO> SubjectContactComparatorDesc = (o1, o2) -> o2.primaryContact.compareToIgnoreCase(o1.primaryContact);

    public static final Comparator<SubjectsResponseDTO> SubjectBirthdateComparatorAsc = (o1, o2) -> o1.dob.compareTo(o2.dob);

    public static final Comparator<SubjectsResponseDTO> SubjectBirthdateComparatorDesc = (o1, o2) -> o2.dob.compareTo(o1.dob);

    public static final Comparator<SubjectsResponseDTO> SubjectGenderComparatorAsc = (o1, o2) -> o1.gender.compareToIgnoreCase(o2.gender);

    public static final Comparator<SubjectsResponseDTO> SubjectGenderComparatorDesc = (o1, o2) -> o2.gender.compareToIgnoreCase(o1.gender);

    public static final Comparator<SubjectsResponseDTO> SubjectCityComparatorAsc = (o1, o2) -> o1.address.getCity().compareToIgnoreCase(o2.address.getCity());

    public static final Comparator<SubjectsResponseDTO> SubjectCityComparatorDesc = (o1, o2) -> o2.address.getCity().compareToIgnoreCase(o1.address.getCity());

    public static final Comparator<SubjectsResponseDTO> SubjectStateComparatorAsc = (o1, o2) -> o1.address.getStateName().compareToIgnoreCase(o2.address.getStateName());

    public static final Comparator<SubjectsResponseDTO> SubjectStateComparatorDesc = (o1, o2) -> o2.address.getStateName().compareToIgnoreCase(o1.address.getStateName());

    public Long getTotalCount() {
        return totalCount;
    }

    public List<SubjectsResponseDTO> getSubjectList() {
        return subjectList;
    }

    public int getId() {
        return id;
    }

    public String getLastName() {
        return lastName;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getMiddleName() {
        return middleName;
    }

    public Date getDob() {
        return dob;
    }

    public void setGender(String gender)
    {
        this.gender = gender;
    }

    public String getGender() {
        return gender;
    }

    public Address getAddress() {
        return address;
    }

    public String getPrimaryContact() {
        return primaryContact;
    }

    public boolean getActive() {
        return active;
    }

    public int getPartnersUid() {
        return partnersUid;
    }

    public String getEmpiFailoverMessage() {
        return empiFailoverMessage;
    }

    public void setEmpiFailoverMessage(String empiFailoverMessage) {
        this.empiFailoverMessage = empiFailoverMessage;
    }
}
