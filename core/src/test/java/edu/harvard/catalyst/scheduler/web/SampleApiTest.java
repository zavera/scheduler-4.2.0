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
/*

1. set up db data
2. set up JSON input
3. call web resource with JSON input
4. check web resource return value
5. check db side-effects
6. clean up db (or roll-back tx)
 */

package edu.harvard.catalyst.scheduler.web;

import edu.harvard.catalyst.scheduler.AbstractDbIntegrationTest;
import edu.harvard.catalyst.scheduler.dto.SubjectsDTO;
import edu.harvard.catalyst.scheduler.entity.*;
import edu.harvard.catalyst.scheduler.security.SchedulerSession;
import edu.harvard.catalyst.scheduler.security.SchedulerUserDetails;
import edu.harvard.catalyst.scheduler.util.SubjectDataEncryptor;
import org.hibernate.query.Query;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import javax.servlet.http.HttpServletRequest;
import java.security.Key;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.hibernate.Session;

/**
 * Created by xavier on 5/9/16.
 */
public class SampleApiTest extends AbstractDbIntegrationTest {

    @Autowired @Qualifier("encryptionKey")
    Key encryptionKey;

    private SchedulerSession session;
    private HttpServletRequest request;
    private SchedulerUserDetails schedulerUserDetails;
    private User mockUserInSession;
    private String mockHostnameInRequest;

    @Autowired
    public SubjectResource subjectResource;
    Session hibernateSession;

    int ethnicityId;
    int genderId;
    int raceId;
    int countryId;
    int stateId;
    User user;


    @Before
    public void setUp() {

        SubjectDataEncryptor.setEncryptionKey(encryptionKey);

        // do this at the top, because it is later needed for creating persistent entities in the same method
        hibernateSession = session();

        session = mock(SchedulerSession.class);
        request = mock(HttpServletRequest.class);
        schedulerUserDetails = mock(SchedulerUserDetails.class);
        mockHostnameInRequest = "mockHostnameInRequest";

        subjectResource.setSession(session);
        subjectResource.setRequest(request);

        when(session.getUserDetails()).thenReturn(schedulerUserDetails);
        user = createAndPersistOneUSer();
        mockUserInSession = user;
        when(schedulerUserDetails.getUser()).thenReturn(mockUserInSession);
        when(request.getRemoteHost()).thenReturn(mockHostnameInRequest);

        // populateMany the database with entities that we can later refer to by ID
        countryId = this.createAndPersistOneCountry().getId();
        ethnicityId = this.createAndPersistOneEthnicityEntity().getId();
        genderId = this.createAndPersistOneGenderEntity().getId();
        raceId = this.createAndPersistOneRace().getId();
        stateId = this.createAndPersistOneState().getId();

    }

    User createAndPersistOneUSer() {

        User user = new User();
        user.setPreviousLoginTime(null);
        user.setFirstName("a");
        user.setLastName("b");
        user.setPassword("c%");
        user.setEmail("d");
        user.setActive(true);
        user.setSalt("e");
        user.setEcommonsId("f");

        Role role = new Role(0, "a", RoleType.ROLE_ADMINISTRATIVE_DIRECTOR);
        hibernateSession.save(role);
        user.setRole(role);

        InstitutionRole institutionRole = new InstitutionRole();
        institutionRole.setType(InstitutionRoleType.ROLE_SUPER_ADMIN);
        institutionRole.setDescription("a");
        institutionRole.setName("b");
        hibernateSession.save(institutionRole);
        user.setInstitutionRole(institutionRole);

        Institution institution = new Institution("institution name", "institution long name");
        hibernateSession.save(institution);
        user.setInstitution(institution);

        hibernateSession.save(user);

        return user;

    }

    Ethnicity createAndPersistOneEthnicityEntity() {

        Ethnicity ethnicity1 = new Ethnicity();
        ethnicity1.setName("test etqhnicity");
        hibernateSession.save(ethnicity1);
        ethnicityId = ethnicity1.getId();

        return ethnicity1;

    }

    Gender createAndPersistOneGenderEntity() {

        Gender gender1 = new Gender();
        gender1.setName("test gender");
        gender1.setCode("M");
        hibernateSession.save(gender1);

        return gender1;

    }

    Race createAndPersistOneRace() {

        Race race1 = new Race();
        race1.setName("test race");
        hibernateSession.save(race1);

        return race1;

    }

    State createAndPersistOneState() {

        State state1 = new State();
        state1.setName("test state");
        hibernateSession.save(state1);

        return state1;

    }

    Country createAndPersistOneCountry() {

        Country country1 = new Country();
        country1.setName("test race");
        hibernateSession.save(country1);

        return country1;

    }

    public SubjectsDTO createSampeSubjectsDto(int flavor) {

        SubjectsDTO dto = new SubjectsDTO();
        dto.setId(0); // not saved
        dto.setMrn(flavor + "a");
        dto.setFirstName(flavor + "b");
        dto.setMiddleName(flavor + "c");
        dto.setLastName("d");
        dto.setFullName("e");
        dto.setEthnicity(ethnicityId);
        dto.setRace(raceId);
        dto.setGender(genderId);
        dto.setStreetAddress1("f");
        dto.setStreetAddress2("g");
        dto.setCountry(countryId);
        dto.setCity("h");
        dto.setState(stateId);
        dto.setZip("i");
        dto.setPrimaryContactNumber("j");
        dto.setSecondaryContactNumber("k");
        dto.setCreatedDate(new Date());
        dto.setSecure(true);
        dto.setActive(true);
        dto.setUserId(user.getId());
        dto.setSubjectToDeactivateId(0);
        dto.setSubjectToActivateId(0);
        dto.setSubjectToEditId(0);
        dto.setSearchSubjects(new ArrayList<Subject>());
        // follows SimpleDateFormat("EEE MMM dd yyyy HH:mm:ss zZ (zzzz)");
        dto.setBirthday("Wed Jan 01 1950 00:00:00 PDT-0000 (Pacific Daylight Time)");
        dto.setComment("l");
        dto.setStudyId(0);

        return dto;

    }


    // Keep at least one test not commented-out so that the Maven build doesn't bomb because this test class has no tests
    /**
     * The objective of this test is to make sure that the no-args constructor needed by cglib proxying is implemented.
     */
    @Test
    public void testNoArgumentResourceConstructorExists() {

        SubjectResource subjectResource = new SubjectResource();

    }
    @Test
    public void testQuery() {

        Query query = hibernateSession.createQuery("select vt from VisitTemplate vt");
        List<VisitTemplate> visitTemplateList = query.list();
        return;
    }


//    @Test
//    public void testCreateSubject() {
//
//        // create the DTO for the subject we want to create
//        SubjectsDTO incomingDto = createSampeSubjectsDto(1);
//
//        // verify that the database does not contain any subject that would match the one we will create
////        incomingDto.setBirthday("1951/02/03");
////        incomingDto.setBirt
//        // EEE MMM dd yyyy HH:mm:ss zZ (zzzz)
//
//
//        incomingDto.setGender(0); // some issue with query... let gender be null so that it will bypass the issue
//        incomingDto.setBirthday("1951/02/03"); // date format issuee
//        String incomingString = subjectResource.gson.toJson(incomingDto);
//        String matchingSubjectsInDatabaseString = subjectResource.getSearchSubjects(incomingString);
//        SubjectsResponseDTO matchingSubjectsInDatabaseDto = subjectResource.gson.fromJson(matchingSubjectsInDatabaseString, SubjectsResponseDTO.class);
//        assertEquals("Matching subjects list should be empty", 0, matchingSubjectsInDatabaseDto.getSubjectList().size());
//
//        // METHOD UNDER TEST
//        // TODO-XH : is the header String passed here representative of real life scenarios?
//        incomingDto.setBirthday("Wed Jan 01 1950 00:00:00 PDT-0000 (Pacific Daylight Time)");
//        incomingDto.setGender(0); // make hibernate happy. We get an exception if the DTO's geneder ID does not point to an exeisting gender entity.
//        incomingDto.setGender(genderId); // make hibernate happy. We get an exception if the DTO's geneder ID does not point to an exeisting gender entity.
//        incomingString = subjectResource.gson.toJson(incomingDto);
//
//        // TODO-XH : refactor header "magic string" to a constant
//        String responseString = subjectResource.createSubject(incomingString, "2016/05/09 2:59:00");
//
//        SubjectsDTO responseDto = subjectResource.gson.fromJson(responseString, SubjectsDTO.class);
//
//
//        assertEquals("incoming result flag should be false", false, incomingDto.isResult());
//
//        // sanitize the attributes of the incoming and response DTOs that will not match
//        // 1. MOOT
//        // 2. Also set the result flag to be the same, so the comparison on the other attributes can succeed
//        incomingDto.setResult(true);
//        // 3. Also deal with the birthDATE...
//        assertEquals("birthdate of output should equal the birthday that was provided as input", incomingDto.getBirthday(), responseDto.getBirthday());
//        // 4 ... and the birthDAY
//        DateFormat birthdayFormat = new SimpleDateFormat("EEE MMM dd yyyy HH:mm:ss zZ (zzzz)");
//        assertEquals("", responseDto.getBirthdate(), DateUtility.parse(birthdayFormat, responseDto.getBirthday()));
//        // 5. get rid of the birthdate because it is null in the incoming DTO
//        responseDto.setBirthdate(null);
//
//        // do the comparison
//        TestUtils.assertEqualJson(subjectResource.gson, incomingDto, responseDto);
//
//        // now check the side effects in the database
//        // get the subject by id (the id is expected to be in the responseString returned by the call to create the subject
//        incomingDto.setBirthday("1951/02/03"); // date format issue
//        incomingDto.setGender(0); // some issue with query... let gender be null so that it will bypass the issue
//        incomingString = subjectResource.gson.toJson(incomingDto);
//        matchingSubjectsInDatabaseString = subjectResource.getSearchSubjects(incomingString);
//        matchingSubjectsInDatabaseDto = subjectResource.gson.fromJson(matchingSubjectsInDatabaseString, SubjectsResponseDTO.class);
//        List<SubjectsResponseDTO> matchingList = matchingSubjectsInDatabaseDto.getSubjectList();
//        assertEquals("Matching subjects list should have exactly one entry", 1, matchingList.size());
//        SubjectsResponseDTO newSubject = matchingList.get(0);
//
////        TestUtils.assertEqualJson(subjectResource.gson, incomingDto, subjectFromDatabaseString);
////
////        String subjectFromDatabaseString = subjectResource.getSubjectData(responseString);
//
//
//    }


    /*
    @Test
    public void testUpdateSubject() {

        // Create and save a subject, using the resource API
        // Note: this duplicates a lot of the work done in testCreateSubject()
        SubjectsDTO subjectSetupDto = createSampeSubjectsDto(1);
        String subjectSetupIncomingString = subjectResource.gson.toJson(subjectSetupDto);
        String subjectSetupResponseString = subjectResource.createSubject(subjectSetupIncomingString, "2016/05/09 2:59:00");
        SubjectsDTO subjectSetupResponseDto = subjectResource.gson.fromJson(subjectSetupResponseString, SubjectsDTO.class);

        // get the new subject's id
        int subjectId = subjectSetupResponseDto.getId();





        // Create the DTO for updating the subject entity
        SubjectsDTO subjectUpdateDto = createSampeSubjectsDto(2);
        subjectUpdateDto.setId(subjectId);
        String subjectUpdateIncomingString = subjectResource.gson.toJson(subjectUpdateDto);

        // METHOD UNDER TEST
        // is the header String passed here representative of real life scenarios?
        String subjectUpdateResponseString = subjectResource.updateSubject(subjectUpdateIncomingString, "2016/05/09 2:59:00");

        SubjectsDTO responseDto = subjectResource.gson.fromJson(subjectUpdateResponseString, SubjectsDTO.class);

        assertEquals("incoming result flag should be false", false, incomingDto.isResult());

        // sanitize the attributes of the incoming and response DTOs that will not match
        // 2. Also set the result flag to be the same, so the comparison on the other attributes can succeed
        incomingDto.setResult(true);
        // 3. Also deal with the birthDATE...
        assertEquals("birthdate of output should equal the birthday that was provided as input", incomingDto.getBirthday(), responseDto.getBirthday());
        // 4 ... and the birthDAY
        DateFormat birthdayFormat = new SimpleDateFormat("EEE MMM dd yyyy HH:mm:ss zZ (zzzz)");
        assertEquals("", responseDto.getBirthdate(), DateUtility.parse(birthdayFormat, responseDto.getBirthday()));
        // 5. get rid of the birthdate because it is null in the incoming DTO
        responseDto.setBirthdate(null);

        // do the comparison
        TestUtils.assertEqualJson(subjectResource.gson, incomingDto, responseDto);

        // now check the side effects in the datbase

        subjectResource.updateSubject();
    }
    */

}
