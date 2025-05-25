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
package edu.harvard.catalyst.scheduler.service;

import edu.harvard.catalyst.scheduler.core.Statics;
import edu.harvard.catalyst.scheduler.dto.response.MrnInfoDTO;
import edu.harvard.catalyst.scheduler.entity.*;
import edu.harvard.catalyst.scheduler.util.TestUtils;
import edu.harvard.catalyst.scheduler.dto.SubjectsDTO;
import edu.harvard.catalyst.scheduler.persistence.AppointmentDAO;
import edu.harvard.catalyst.scheduler.persistence.StudyDAO;
import edu.harvard.catalyst.scheduler.persistence.SubjectDAO;
import edu.harvard.catalyst.scheduler.util.ServiceTestsDTOUtility;
import org.hibernate.exception.ConstraintViolationException;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import static org.mockito.Mockito.doThrow;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import java.security.Key;
import java.util.*;

import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.spy;

import edu.harvard.catalyst.scheduler.util.FactoryMockKey;
import edu.harvard.catalyst.scheduler.util.SubjectDataEncryptor;
import edu.harvard.catalyst.scheduler.core.SchedulerRuntimeException;

import edu.harvard.catalyst.scheduler.util.BooleanSwitch;

/**
 * @author Bill Simons
 * @date 7/10/13
 * @link http://cbmi.med.harvard.edu
 * @link http://chip.org
 */
@RunWith(MockitoJUnitRunner.class)
public class SubjectServiceTest {

    @Mock private SubjectDAO mockSubjectDAO;
    @Mock private AuditService mockAuditService;
    @Mock private StudyDAO mockStudyDAO;
    @Mock private AppointmentDAO mockAppointmentDAO;

    @Mock private User mockUser;

    private String host = "host";

    private static Gender female;

    @BeforeClass
    public static void setUpGender() {
        female = new Gender();
        female.setName("Female");
        female.setCode("F");
        female.setId(2);
    }

    @Before
    public void before(){
        FactoryMockKey keyFactory = new FactoryMockKey();
        Key encryptionKey = keyFactory.createKey();
        SubjectDataEncryptor.setEncryptionKey(encryptionKey);

 }

    @Test
    public void testSetSomeEntityValuesViaDto() {
        doDTOTest(female, false, true);
        doDTOTest(female, false, false);

        Gender otherGender = new Gender();
        otherGender.setName("Other");
        otherGender.setId(22);

        doDTOTest(otherGender, true, true);
    }

    private void doDTOTest(Gender gender, boolean expectException, boolean setState) {
        SubjectServiceClassic subjectServiceClassic = new SubjectServiceClassic(mockAuditService, mockSubjectDAO, mockStudyDAO, null, mockAppointmentDAO, null);
        Subject subject = new Subject();

        int newInt = 0;
        SubjectsDTO subjectDTO = new SubjectsDTO();

        Date birthdate = new Date();
        String city = "theCity";

        int ethnicityInt = ++newInt;
        Ethnicity ethnicity = new Ethnicity();
        ethnicity.setId(11); // id determines its hashCode, useful if compare to 'others'

        String firstName = "first";
        String lastName = "last";
        String middleName = "middle";
        String fullName = firstName + " " + lastName;

        String contactNumber = Integer.toString(++newInt);

        int raceInt = ++newInt;
        Race race = new Race();
        race.setId(newInt); // id determines its hashCode, useful if compare to 'others'

        Boolean secure = Boolean.FALSE;

        String secondaryContactNumber = Integer.toString(++newInt);
        String addr1 = "addr1";
        String addr2 = "addr2";

        int stateInt = ++newInt;
        State state = new State();
        state.setId(newInt); // as above


        int countryInt = ++newInt;
        Country country = new Country();
        country.setId(newInt); // as above

        String zip = "zip123";
        String comment = "my comment";

        when(mockSubjectDAO.findByEthnicityId(ethnicityInt)).thenReturn(ethnicity);
        when(mockSubjectDAO.findByGenderId(anyInt())).thenReturn(gender);
        when(mockSubjectDAO.findByRaceId(raceInt)).thenReturn(race);
        when(mockSubjectDAO.findByStateId(stateInt)).thenReturn(state);
        when(mockSubjectDAO.findCountryById(countryInt)).thenReturn(country);

        subjectDTO.setBirthdate(birthdate);
        subjectDTO.setCity(city);
        subjectDTO.setEthnicity(ethnicityInt);
        subjectDTO.setFirstName(firstName);
        subjectDTO.setLastName(lastName);
        subjectDTO.setGender(2);
        subjectDTO.setMiddleName(middleName);
        subjectDTO.setPrimaryContactNumber(contactNumber);
        subjectDTO.setRace(raceInt);
        subjectDTO.setSecondaryContactNumber(secondaryContactNumber);
        subjectDTO.setStreetAddress1(addr1);
        subjectDTO.setStreetAddress2(addr2);

        if (setState) {
            subjectDTO.setState(stateInt);
        }

        subjectDTO.setCountry(countryInt);
        subjectDTO.setZip(zip);
        subjectDTO.setComment(comment);

        try {
            subjectServiceClassic.setSomeEntityValuesViaDto(subject, subjectDTO);

            if (expectException) {
                fail("should have thrown an exception");
            }
        } catch  (SchedulerRuntimeException e) {
            if (!expectException) {
                fail("threw an unexpected exception: " + e);
            } else {
                assertEquals("Cannot set subject.genderType because of null gender code on gender entity", e.getMessage());
                return;
            }
        }

        TestUtils.assertNonNullAndEqual(subject.getBirthdate(), birthdate);
        TestUtils.assertNonNullAndEqual(subject.getCity(), city);
        TestUtils.assertNonNullAndEqual(subject.getEthnicity(), ethnicity);
        TestUtils.assertNonNullAndEqual(subject.getFirstName(), firstName);
        TestUtils.assertNonNullAndEqual(subject.getLastName(), lastName);
        TestUtils.assertNonNullAndEqual(subject.getGender(), gender);
        TestUtils.assertNonNullAndEqual(subject.getFullName(), fullName);
        TestUtils.assertNonNullAndEqual(subject.getMiddleName(), middleName);
        TestUtils.assertNonNullAndEqual(subject.getPrimaryContactNumber(), contactNumber);
        TestUtils.assertNonNullAndEqual(subject.getRace(), race);
        TestUtils.assertNonNullAndEqual(subject.getSecure(), secure);
        TestUtils.assertNonNullAndEqual(subject.getSecondaryContactNumber(), secondaryContactNumber);
        TestUtils.assertNonNullAndEqual(subject.getStreetAddress1(), addr1);
        TestUtils.assertNonNullAndEqual(subject.getStreetAddress2(), addr2);
        if (setState) {
            TestUtils.assertNonNullAndEqual(subject.getState(), state);
        } else {
            assertNull(subject.getState());
        }
        TestUtils.assertNonNullAndEqual(subject.getCountry(), country);
        TestUtils.assertNonNullAndEqual(subject.getZip(), zip);
        TestUtils.assertNonNullAndEqual(subject.getComment(), comment);
    }

    @Test
    public void test_createSubject() throws Exception {
        Institution institution = mock(Institution.class);
        when(mockUser.getInstitution()).thenReturn(institution);
        when(institution.getName()).thenReturn("name");

        ServiceTestsDTOUtility.setUpSubjectDao(5, mockSubjectDAO, false);

        SubjectsDTO subjectsDTO = ServiceTestsDTOUtility.createSubject(true);
        subjectsDTO.setStudyId(4);

        Study mockStudy = mock(Study.class);
        when(mockStudy.getId()).thenReturn(4);
        when(mockStudyDAO.findStudyById(4)).thenReturn(mockStudy);

        MrnInfoDTO mrnInfoDTO = new MrnInfoDTO("199", "site1", "A");
        subjectsDTO.setMrnInfo(mrnInfoDTO);


        doAnswer(new Answer() {
            public Object answer(InvocationOnMock invocation) {
                Object[] args = invocation.getArguments();
                // args[0]: the first arg, that I want to modify
                StudySubject studySubject = (StudySubject)args[0];
                assertEquals("199", studySubject.getSubjectMrn().getMrn());
                assertEquals(4, studySubject.getStudy().getId().intValue());

                //return null since it's a void method:
                return null;
            }}).when(mockStudyDAO).createEntity(any(StudySubject.class));

        StudyService mockStudyService = mock(StudyService.class);
        SubjectServiceClassic service = new SubjectServiceClassic(mockAuditService, mockSubjectDAO, mockStudyDAO, mockStudyService, mockAppointmentDAO, null);
        service.createSubjectForDto(subjectsDTO, mockUser, host);

        assertTrue(subjectsDTO.isResult());
        assertEquals(5, subjectsDTO.getId());

        //throw the exception now; test that too
        doThrow(new ConstraintViolationException("message", null, "randomName")).when(mockStudyDAO).createEntity(any());
        final BooleanSwitch constraintViolationHappened =  new BooleanSwitch();

        SubjectServiceClassic sscSpy = spy(service);

        doAnswer(new Answer() {
            public Object answer(InvocationOnMock invocation) {
                Object[] args = invocation.getArguments();
                // args[0]: the first arg, that I want to modify
                String message = (String)args[0];
                assertEquals("updateSubject() constraint violation", message);
                constraintViolationHappened.setState(true);
                //return null since it's a void method:
                return null;
            }}).when(sscSpy).logDontThrow(any(String.class), any(Exception.class));

        sscSpy.createSubject(subjectsDTO, mockUser, "89");
        assertTrue(constraintViolationHappened.isState());
    }

    @Test
    public void test_createSubjectForDTOError() throws Exception {
        Institution institution = mock(Institution.class);
        when(mockUser.getInstitution()).thenReturn(institution);
        when(institution.getName()).thenReturn("name");

        ServiceTestsDTOUtility.setUpSubjectDao(5, mockSubjectDAO, false);

        SubjectServiceClassic service = new SubjectServiceClassic(mockAuditService, mockSubjectDAO, mockStudyDAO, null, mockAppointmentDAO, null);

        SubjectsDTO subjectsDTO = ServiceTestsDTOUtility.createSubject(false);

        MrnInfoDTO mrnInfoDTO = new MrnInfoDTO("199", "site1", "A");
        subjectsDTO.setMrnInfo(mrnInfoDTO);

        when(mockSubjectDAO.mrnInfoExists(mrnInfoDTO)).thenReturn(true);

        service.createSubjectForDto(subjectsDTO, mockUser, host);

        assertFalse(subjectsDTO.isResult());
        assertTrue(subjectsDTO.isMrnAlreadyExistsError());

        assertEquals(0, subjectsDTO.getId());
    }

    @Test
    public void test_createSubjectWithInvalidMrn() throws Exception {
        Institution institution = mock(Institution.class);
        when(mockUser.getInstitution()).thenReturn(institution);
        when(institution.getName()).thenReturn("name");

        ServiceTestsDTOUtility.setUpSubjectDao(5, mockSubjectDAO, false);

        SubjectServiceClassic service = new SubjectServiceClassic(mockAuditService, mockSubjectDAO, mockStudyDAO, null, mockAppointmentDAO, null);

        SubjectsDTO subjectsDTO = ServiceTestsDTOUtility.createSubject(true);

        MrnInfoDTO mrnInfoDTO = new MrnInfoDTO("199", "site1", "A");
        subjectsDTO.setMrnInfo(mrnInfoDTO);

        when(mockSubjectDAO.mrnInfoExists(mrnInfoDTO)).thenReturn(true);

        Subject result = service.createSubject(subjectsDTO, mockUser, "18.209.0.57");
        assertNull(result);

        assertFalse(subjectsDTO.isResult());
        assertTrue(subjectsDTO.isMrnAlreadyExistsError());
    }


    @Test
    public void testMrnBelongsToAnotherSubject(){
        SubjectsDTO subjectsDTO = new SubjectsDTO();
        SubjectServiceClassic service = new SubjectServiceClassic(mockAuditService, mockSubjectDAO, mockStudyDAO, null, mockAppointmentDAO, null);

        //mrn info is null:
        assertFalse(service.mrnAlreadyExists(subjectsDTO));

        MrnInfoDTO mrnInfoDTO = new MrnInfoDTO("199", "site1", "A");
        subjectsDTO.setMrnInfo(mrnInfoDTO);

        when(mockSubjectDAO.mrnInfoExists(mrnInfoDTO)).thenReturn(false);

        assertFalse(service.mrnAlreadyExists(subjectsDTO));

        //make it true:
        when(mockSubjectDAO.mrnInfoExists(mrnInfoDTO)).thenReturn(true);
        assertTrue(service.mrnAlreadyExists(subjectsDTO));
    }


    @Test
    public void testCreateSubjectMrnIfNeeded() throws Exception {
        Subject testSubject = makeTestSubject();

        SubjectMrn subjectMrn = new SubjectMrn(testSubject, "199", "site1", "A", 67);
        Set<SubjectMrn> mrnSet = new HashSet<>();
        mrnSet.add(subjectMrn);

        testSubject.setSubjectMrnSet(mrnSet);

        SubjectMrn newSubjectMrn = new SubjectMrn(testSubject, "123", "site1", "A", null);

        //should return the newSubjectMrn:
        when(mockSubjectDAO.getSubjectMrnForSubject(anyObject())).thenReturn(subjectMrn);

        SubjectServiceClassic service = new SubjectServiceClassic(mockAuditService, mockSubjectDAO, mockStudyDAO, null, mockAppointmentDAO, null);


        SubjectMrn result = service.createSubjectMrnIfNeeded(true, testSubject, newSubjectMrn);

        assertEquals(67, result.getId().intValue());
        assertEquals("123", result.getMrn());

        SubjectMrn newSubjectMrn2 = new SubjectMrn(testSubject, "1234", "site2", "A", 678);
        result = service.createSubjectMrnIfNeeded(false, testSubject, newSubjectMrn2);
        assertEquals("1234", result.getMrn());
        assertEquals(678, result.getId().intValue());

        //test Exception
        testSubject.setSubjectMrnSet(new HashSet<>());

        try {
            service.createSubjectMrnIfNeeded(true, testSubject, newSubjectMrn);
            fail("exception should have been thrown");
        } catch (SubjectServiceClassic.NoMrnFoundException e) {
            assertEquals("Updating Subject Failed, the Subject's previous MRN was not found",
                    e.getMessage());
        }
    }

    @Test
    public void testAlreadyInStudy() throws Exception {

        Subject mockSubject = mock(Subject.class);
        when(mockSubject.getId()).thenReturn(17);

        Study mockStudy = mock(Study.class);
        when(mockStudy.getId()).thenReturn(23);

        // #1:  empty response

        //a:  user is studyStaff:
        when(mockUser.isStudyStaff()).thenReturn(true);

        List<StudySubject> studySubjectList = initStudySubjectList();

        StudyService mockStudyService = mock(StudyService.class);
        SubjectServiceClassic service = new SubjectServiceClassic(mockAuditService, mockSubjectDAO,
                mockStudyDAO, mockStudyService, mockAppointmentDAO, null);

        boolean result = service.isSubjectAlreadyInThisStudy(mockUser, mockSubject, mockStudy);
        assertFalse(result);

        //b:  user is NOT studyStaff:
        when(mockUser.isStudyStaff()).thenReturn(false);

        result =  service.isSubjectAlreadyInThisStudy(mockUser, mockSubject, mockStudy);
        assertFalse(result);

        // #2 b:  user is NOT studyStaff, user in the study:
        StudySubject mockStudySubject = mock(StudySubject.class);
        when(mockStudySubject.getId()).thenReturn(123);
        when(mockStudySubject.getSubject()).thenReturn(mockSubject);

        when(mockStudySubject.getStudy()).thenReturn(mockStudy);

        studySubjectList.add(mockStudySubject);

        result =  service.isSubjectAlreadyInThisStudy(mockUser, mockSubject, mockStudy);
        assertTrue(result);

        //2a:  user IS study staff
        when(mockUser.isStudyStaff()).thenReturn(true);
        result =  service.isSubjectAlreadyInThisStudy(mockUser, mockSubject, mockStudy);
        assertTrue(result);

        //3a:  user is Study staff, the subject is in *other* studies, just not this one
        studySubjectList.clear();

        StudySubject joe = mock(StudySubject.class);
        when(joe.getId()).thenReturn(223);
        when(joe.getSubject()).thenReturn(mockSubject);

        Study otherMockStudy = mock(Study.class);
        when(otherMockStudy.getId()).thenReturn(34);

        when(joe.getStudy()).thenReturn(otherMockStudy);

        StudySubject mike = mock(StudySubject.class);
        when(mike.getId()).thenReturn(224);
        when(mike.getSubject()).thenReturn(mockSubject);

        Study otherMockStudy2 = mock(Study.class);
        when(otherMockStudy2.getId()).thenReturn(341);
        when(mike.getStudy()).thenReturn(otherMockStudy2);

        studySubjectList.add(joe);
        studySubjectList.add(mike);

        result =  service.isSubjectAlreadyInThisStudy(mockUser, mockSubject, mockStudy);
        assertFalse(result);
    }

    @Test
    public void test_updateSubject() throws Exception {
        setupMockUser();

        SubjectsDTO subjectsDTO = ServiceTestsDTOUtility.createSubject(true);
        subjectsDTO.setActive(true);

        MrnInfoDTO mrnInfoDTO = new MrnInfoDTO("199", "site1", "A");
        subjectsDTO.setMrnInfo(mrnInfoDTO);

        Subject testSubject = makeTestSubject();

        when(mockSubjectDAO.findBySubjectId(anyInt())).thenReturn(testSubject);

        SubjectMrn subjectMrn = new SubjectMrn(testSubject, "199", "site1", "A", 67);

        //first:  test that it belongs to someone else:

        when(mockSubjectDAO.getSubjectMrnForSubject(anyObject())).thenReturn(null);

        when(mockSubjectDAO.mrnInfoExists(mrnInfoDTO)).thenReturn(true);

        StudyService mockStudyService = mock(StudyService.class);
        SubjectServiceClassic service = new SubjectServiceClassic(mockAuditService, mockSubjectDAO, mockStudyDAO, mockStudyService, mockAppointmentDAO, null);

        SubjectsDTO result = service.updateSubject(subjectsDTO, mockUser, "18.209.0.57", true);
        assertTrue(result.isMrnAlreadyExistsError());
        assertFalse(result.isResult());

        //first:  DON"T add user to a study:

        Study study = makeStudy();

        when(mockStudyDAO.findStudyById(anyInt())).thenReturn(study);

        List<StudySubject> studySubjectList = new ArrayList<>();

        StudySubject mockStudySubject = mock(StudySubject.class);
        when(mockStudySubject.getId()).thenReturn(123);

        Study mockStudy = mock(Study.class);
        when(mockStudy.getId()).thenReturn(23);
        when(mockStudySubject.getStudy()).thenReturn(mockStudy);

        studySubjectList.add(mockStudySubject);

        when(mockAppointmentDAO.findStudySubjectBySubject(any())).thenReturn(studySubjectList);
        when(mockAppointmentDAO.findStudySubjectBySubjectAndStudy(any(), any())).thenReturn(studySubjectList);
        when(mockSubjectDAO.getSubjectMrnForSubject(anyObject())).thenReturn(subjectMrn);

        Ethnicity ethnicity = new Ethnicity();
        ethnicity.setId(2);
        ethnicity.setName("Hispanic or Latino");

        Race race = new Race();
        race.setId(1);
        race.setName("White");

        Country country = new Country();
        country.setId(1);
        country.setName("United States");

        when(mockSubjectDAO.findByEthnicityId(2)).thenReturn(ethnicity);
        when(mockSubjectDAO.findByGenderId(2)).thenReturn(female);
        when(mockSubjectDAO.findByRaceId(1)).thenReturn(race);
        when(mockSubjectDAO.findCountryById(1)).thenReturn(country);

        FactoryMockKey keyFactory = new FactoryMockKey();
        Key key = keyFactory.createKey();
        SubjectDataEncryptor.setEncryptionKey(key);

        BooleanSwitch addedStudySubject = new BooleanSwitch();

        doAnswer(new Answer() {
            public Object answer(InvocationOnMock invocation) {
                Object[] args = invocation.getArguments();
                // args[0]: the first arg, that I want to modify
                StudySubject studySubject = (StudySubject)args[0];
                assertEquals("199", studySubject.getSubjectMrn().getMrn());
                assertEquals(1, studySubject.getStudy().getId().intValue());
                addedStudySubject.setState(true);
                //return null since it's a void method:
                return null;
            }}).when(mockStudyDAO).createEntity(any(StudySubject.class));


        SubjectsDTO result1 = service.updateSubject(subjectsDTO, mockUser, "18.209.0.57", true);
        assertTrue(result1.isResult());
        assertFalse(addedStudySubject.isState());

        //also check that fields got updated:
        assertEquals("Joy", result1.getFirstName());
        assertEquals("Cambridge", result1.getCity());
        assertEquals("1231231233", result1.getPrimaryContactNumber());

        //see that this field stayed the same:
        assertEquals("Hall", result1.getLastName());


        //next:  adding user to a study:
        subjectsDTO.setStudyId(1);

        //reset the ones that changed:
        subjectsDTO.setFirstName("Joy");
        subjectsDTO.setCity("Cambridge");
        subjectsDTO.setPrimaryContactNumber("1231231233");

        SubjectsDTO result2 = service.updateSubject(subjectsDTO, mockUser, "18.209.0.57", true);
        assertTrue(result2.isResult());
        assertTrue(addedStudySubject.isState());

    }

    @Test
    public void test_updateSubjectAlreadyInStudy() throws Exception {
        setupMockUser();

        SubjectsDTO subjectsDTO = ServiceTestsDTOUtility.createSubject(true);
        subjectsDTO.setActive(true);
        MrnInfoDTO mrnInfoDTO = new MrnInfoDTO("199", "site1", "A");
        subjectsDTO.setMrnInfo(mrnInfoDTO);

        Subject testSubject = makeTestSubject();

        when(mockSubjectDAO.findBySubjectId(anyInt())).thenReturn(testSubject);
        SubjectMrn subjectMrn = new SubjectMrn(testSubject, "199", "site1", "A", 67);

        //next:  adding user to a study:
        subjectsDTO.setStudyId(1);

        Study study = makeStudy();

        when(mockStudyDAO.findStudyById(anyInt())).thenReturn(study);

        //make study subject list
        List<StudySubject> studySubjectList = initStudySubjectList();

        StudySubject mockStudySubject = mock(StudySubject.class);
        when(mockStudySubject.getId()).thenReturn(123);
        when(mockStudySubject.getStudy()).thenReturn(study);

        studySubjectList.add(mockStudySubject);

        when(mockSubjectDAO.getSubjectMrnForSubject(anyObject())).thenReturn(subjectMrn);

        //a:  user is study staff
        when(mockUser.isStudyStaff()).thenReturn(true);

        StudyService mockStudyService = mock(StudyService.class);
        SubjectServiceClassic service = new SubjectServiceClassic(mockAuditService, mockSubjectDAO, mockStudyDAO, mockStudyService, mockAppointmentDAO, null);

        SubjectsDTO result2 = service.updateSubject(subjectsDTO, mockUser, "18.209.0.57", true);
        assertFalse(result2.isResult());
        assertEquals(Statics.SUBJECT_ALREADY_ENROLLED, result2.getErrorMsg());

        //b:  user is NOT study staff
        when(mockUser.isStudyStaff()).thenReturn(false);
        result2 = service.updateSubject(subjectsDTO, mockUser, "18.209.0.57", true);
        assertFalse(result2.isResult());
        assertEquals(Statics.SUBJECT_ALREADY_ENROLLED, result2.getErrorMsg());
    }

    @Test
    public void test_updateSubjectMrnError() throws Exception {
        setupMockUser();

        SubjectsDTO subjectsDTO = ServiceTestsDTOUtility.createSubject(true);
        subjectsDTO.setActive(true);

        MrnInfoDTO mrnInfoDTO = new MrnInfoDTO("199", "site1", "A");
        subjectsDTO.setMrnInfo(mrnInfoDTO);

        Subject testSubject = makeTestSubject();
        testSubject.setSubjectMrnSet(new HashSet<>());

        when(mockSubjectDAO.findBySubjectId(anyInt())).thenReturn(testSubject);

        //next:  adding user to a study:
        subjectsDTO.setStudyId(1);
        Study study = makeStudy();

        when(mockStudyDAO.findStudyById(anyInt())).thenReturn(study);

        when(mockSubjectDAO.getSubjectMrnForSubject(anyObject())).thenReturn(null);
        when(mockSubjectDAO.mrnInfoExists(mrnInfoDTO)).thenReturn(false);

        StudyService mockStudyService = mock(StudyService.class);
        SubjectServiceClassic service = new SubjectServiceClassic(mockAuditService, mockSubjectDAO, mockStudyDAO, mockStudyService, mockAppointmentDAO, null);

        SubjectsDTO result2 = service.updateSubject(subjectsDTO, mockUser, "18.209.0.57", true);
        assertFalse(result2.isResult());
        assertEquals(Statics.MRN_NOT_FOUND_FOR_SUBJECT, result2.getErrorMsg());
    }


    @Test
    public void testFieldStringConstruction(){

        Ethnicity ethnicity = new Ethnicity();
        ethnicity.setId(2);

        String ethnicityStr ="Hispanic or Latino";
        ethnicity.setName(ethnicityStr);

        Subject subject = mock(Subject.class);
        when(subject.getEthnicity()).thenReturn(ethnicity);

        FactoryMockKey keyFactory = new FactoryMockKey();
        Key key = keyFactory.createKey();
        SubjectDataEncryptor.setEncryptionKey(key);

        SubjectServiceClassic service = new SubjectServiceClassic(mockAuditService, mockSubjectDAO, mockStudyDAO, null, mockAppointmentDAO, key);

        //0: no change:
        Optional<String> fieldString = service.makeFieldString("Ethnicity", ethnicityStr.toUpperCase(), subject.getEthnicity(), 2, Ethnicity::getId, SubjectServiceClassic.CRYPT, SubjectServiceClassic.CRYPT_UPPER);
        assertFalse(fieldString.isPresent());

        //1: change
        Optional<String> fieldString2 = service.makeFieldString("Ethnicity", "Caucasian".toUpperCase(), subject.getEthnicity(), 1, Ethnicity::getId, SubjectServiceClassic.CRYPT, SubjectServiceClassic.CRYPT_UPPER);
        String expected = " Ethnicity: Bj86ty+W1y7tFwqqytT6KuFyyKU8FSBYWMpJQkAEQ7o= to G5OaukOEPW7D6CpCk+l5Iw==,";
        assertEquals(expected.trim(), fieldString2.get().trim());

        //2: null to something else
        when(subject.getEthnicity()).thenReturn(null);
        Optional<String> fieldString3 = service.makeFieldString("Ethnicity", "Caucasian".toUpperCase(), subject.getEthnicity(), 1, Ethnicity::getId, SubjectServiceClassic.CRYPT, SubjectServiceClassic.CRYPT_UPPER);
        String expected3 = " Ethnicity: NULL  to G5OaukOEPW7D6CpCk+l5Iw==,";
        assertEquals(expected3.trim(), fieldString3.get().trim());


        //3:  something else to null:
        when(subject.getEthnicity()).thenReturn(ethnicity);
        Optional<String> fieldString4 = service.makeFieldString("Ethnicity", null, subject.getEthnicity(), 0, Ethnicity::getId, SubjectServiceClassic.CRYPT, SubjectServiceClassic.CRYPT_UPPER);
        assertTrue(fieldString4.isPresent());
        String expected4 = " Ethnicity: Bj86ty+W1y7tFwqqytT6KuFyyKU8FSBYWMpJQkAEQ7o= to : NULL ,";
        assertEquals(expected4.trim(), fieldString4.get().trim());

        //fieldString construction without an id:
        Optional<String> fieldString5 = service.makeFieldString("TESTING", "foo", "foo");
        assertFalse(fieldString5.isPresent());

        Optional<String> fieldStrin65 = service.makeFieldString("TESTING", "foo", null);
        String expected6 = "TESTING: NULL  to foo,".trim();
        assertEquals(fieldStrin65.get().trim(), expected6);

        Optional<String> fieldString7 = service.makeFieldString("TESTING", null, "foo");
        assertTrue(fieldString7.isPresent());
        String expected7 = "TESTING: foo to : NULL , ";
        assertEquals(expected7.trim(), fieldString7.get().trim());

        Optional<String> fieldString8 = service.makeFieldString("TESTING", "foo", "bar");
        String expected8 = "TESTING: bar to foo,";
        assertEquals(expected8, fieldString8.get().trim());
    }

    /*@Test
    public void testMergeSubject() throws Exception{
        MrnInfoDTO mrnInfo = new MrnInfoDTO("1234");
        mrnInfo.setInstitution("inst");
        mrnInfo.setStatus("A");
        SubjectsDTO subjectsDTO = ServiceTestsDTOUtility.createSubject(true);
        subjectsDTO.setMrnInfo(mrnInfo);
        String ipAddress = "18.209.0.57";

        StudyService mockStudyService = mock(StudyService.class);
        SubjectServiceClassic service = new SubjectServiceClassic(mockAuditService, mockSubjectDAO, mockStudyDAO, mockStudyService, mockAppointmentDAO, null);
        SubjectServiceClassic serviceSpy = mock(SubjectServiceClassic.class); spy(service);
        SubjectMrn subjectMrn = new SubjectMrn();
        subjectMrn.setMrn(SubjectDataEncryptor.encrypt("0123"));
        subjectMrn.setSite("inst");
        subjectMrn.setStatus("A");

        when(mockSubjectDAO.getSubjectMrnByMrnAndSite(anyString(), anyString())).thenReturn(subjectMrn);
        when(serviceSpy.mrnHasLeadingZeros(eq(subjectsDTO.getMrnInfo()))).thenReturn(true);

        User adminUser = new User();
        InstitutionRole institutionRole = new InstitutionRole();
        institutionRole.setType(InstitutionRoleType.ROLE_SUPER_ADMIN);
        adminUser.setInstitutionRole(institutionRole);
        SubjectsDTO result = serviceSpy.updateSubject(subjectsDTO, adminUser, ipAddress, true);

        assertTrue(result.isSimilarMrnAlreadyExistsError());
    }*/
    // HELPER METHODS ////////////////////////////

    private Subject makeTestSubject() {
        Subject testSubject = new Subject();
        testSubject.setLastName("Hall");
        testSubject.setFirstName("Joia");
        testSubject.setCity("Somerville");
        testSubject.setStreetAddress1("123 college ave, apt #3b");
        testSubject.setZip("09876");
        testSubject.setActive(true);
        return testSubject;
    }

    private List<StudySubject> initStudySubjectList() {
        List<StudySubject> studySubjectList = new ArrayList<>();
        when(mockAppointmentDAO.findStudySubjectBySubject(any())).thenReturn(studySubjectList);
        when(mockAppointmentDAO.findStudySubjectBySubjectAndStudy(any(), any())).thenReturn(studySubjectList);
        return studySubjectList;
    }

    private Study makeStudy() {
        Study study = new Study();
        study.setName("test study");
        study.setId(1);

        Institution inst = mock(Institution.class);
        when(inst.getName()).thenReturn("site1");
        when(inst.getId()).thenReturn(1);
        study.setInstitution(inst);
        study.setLocalId("TS-1");
        return study;
    }

    private void setupMockUser() {
        Institution institution = mock(Institution.class);
        when(mockUser.getInstitution()).thenReturn(institution);
        when(institution.getName()).thenReturn("site1");
    }
}
