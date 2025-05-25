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
package edu.harvard.catalyst.scheduler.batchSubjects;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import edu.harvard.catalyst.scheduler.dto.ExternalSubjectQueryBuilder;
import edu.harvard.catalyst.scheduler.dto.Epic.EmpiSubjectDto;
import edu.harvard.catalyst.scheduler.entity.*;
import edu.harvard.catalyst.scheduler.entity.Ethnicity;
import edu.harvard.catalyst.scheduler.persistence.EpicSubjectDAO;
import edu.harvard.catalyst.scheduler.persistence.SubjectDAO;
import edu.harvard.catalyst.scheduler.service.EpicSubjectService;
import edu.harvard.catalyst.scheduler.util.DateUtility;
import edu.harvard.catalyst.scheduler.util.FactoryMockKey;
import edu.harvard.catalyst.scheduler.util.SubjectDataEncryptor;
import org.apache.log4j.Appender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.spi.LoggingEvent;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.security.Key;
import java.util.*;
import java.util.stream.Collectors;

import static edu.harvard.catalyst.scheduler.dto.Epic.EmpiSubjectDto.*;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;
import static org.springframework.test.util.AssertionErrors.assertEquals;

@RunWith(MockitoJUnitRunner.class)
public class EpicBatchTest {

    static final int NUM_SUBJECTS = 1; // use only values from 1-9
    static final String site = "MGH";

    List<SubjectMrn> subjectMrnList;
    EpicSubjectDAO epicSubjectDAO = mock(EpicSubjectDAO.class);
    EpicSubjectService epicSubjectService;

    EmpiSubjectDto empiSubjectDto;

    Key key;

    SubjectMrn subjectMrn0;
    Subject subject0;
    Patient patient0;
    BatchSubjects batchSubjects;
    Subject staleSubject;

    @Mock
    private Appender mockAppender;
    @Captor
    private ArgumentCaptor<LoggingEvent> captorLoggingEvent;



    @Test
    public void testNoChanges() throws Exception {

        batchSubjects.run();

        myVerifyNoChanges();
    }

    /***  from spec's
     Partner ID,
     First Name,
     Middle Name,
     Last Name,
     DOB,

     Street Address 1,
     Street Address 2,
     City,
     Zip,
     Primary Phone,
     Secondary Phone,

     Country,
     State,
     Gender,
     Race,
     Ethnicity

     MRN-Status,
     ****/

    private int UNKNOWN_OR_NOT_REPORTED_ETHNICITY = 3;

    @Test
    public void testNullEthnicity1() throws Exception {

        Ethnicity ethnicity = new Ethnicity();
        ethnicity.setName("Unknown");
        ethnicity.setId(3);

        patient0.setEthnicity(null);

        when(epicSubjectDAO.lookupEmpiEthnicityString(null)).thenReturn(ethnicity);

        batchSubjects.run();

        myVerifyWithChangesText("ethnicity:'CHINESE'--->'Unknown'");
    }

    @Test
    public void testNullEthnicity2() throws Exception {

        patient0.getEthnicity().setEthnic1(null);

        batchSubjects.run();

        myVerifyWithChangesText("ethnicity:'CHINESE'--->'null'");
    }

    @Test
    public void testNullCountry() throws Exception {

        patient0.getAddress().setCountry(null);

        batchSubjects.run();

        myVerifyWithChangesText("country:'Country0'--->'null'");
    }

    @Test
    public void testNullState() throws Exception {

        patient0.getAddress().setState(null);

        batchSubjects.run();

        myVerifyWithChangesText("state:'State0'--->'null'");
    }

    @Test
    public void testNullRace1() throws Exception {

        patient0.setOtherPid(null);

        batchSubjects.run();

        myVerifyWithChangesText("race:'R0'--->'null'");
    }

    @Test
    public void testNullRace2() throws Exception {

        patient0.getOtherPid().setRace(null);

        batchSubjects.run();

        myVerifyWithChangesText("race:'R0'--->'null'");
    }

    @Test
    public void testPuidChanges() throws Exception {

        subject0.setPuid(SubjectDataEncryptor.encrypt("Foo"));

        batchSubjects.run();

        myVerifyWithChangesText("puid:'Foo'--->'0'");
    }

    @Test
    public void testFirstNameChanges() throws Exception {

        subject0.setFirstName(SubjectDataEncryptor.encrypt("Foo"));

        batchSubjects.run();

        myVerifyWithChangesText("firstName:'Foo'--->'First0'");
    }

    @Test
    public void testMiddleNameChanges() throws Exception {

        subject0.setMiddleName(SubjectDataEncryptor.encrypt("Foo"));

        batchSubjects.run();

        myVerifyWithChangesText("middleName:'Foo'--->'MI0'");
    }

    @Test
    public void testLastNameChanges() throws Exception {

        subject0.setLastName(SubjectDataEncryptor.encrypt("Foo"));

        batchSubjects.run();

        myVerifyWithChangesText("lastName:'Foo'--->'Last0'");
    }

    @Test
    public void testDobChanges() throws Exception {

        Date bogus = DateUtility.parse(
                DateUtility.monthDayYear(),
                "02/1/1948");
        subject0.setBirthdate(bogus);

        batchSubjects.run();

        myVerifyWithChangesText("birthdate:'1948-02-01'--->'1928-01-10'");
    }
    @Test
    public void testAddress1Changes() throws Exception {

        subject0.setStreetAddress1(SubjectDataEncryptor.encrypt("Foo"));

        batchSubjects.run();

        myVerifyWithChangesText("streetAddress1:'Foo'--->'Address10'");
    }

    @Test
    public void testAddress2Changes() throws Exception {

        subject0.setStreetAddress2(SubjectDataEncryptor.encrypt("Foo"));

        batchSubjects.run();

        myVerifyWithChangesText("streetAddress2:'Foo'--->'Address20'");
    }

    @Test
    public void testCityChanges() throws Exception {

        subject0.setCity(SubjectDataEncryptor.encrypt("Foo"));

        batchSubjects.run();

        myVerifyWithChangesText("city:'Foo'--->'City0'");
    }

    @Test
    public void testZipChanges() throws Exception {

        subject0.setZip(SubjectDataEncryptor.encrypt("Foo"));

        batchSubjects.run();

        myVerifyWithChangesText("zip:'Foo'--->'00000'");
    }

    @Test
    public void testPrimaryChanges() throws Exception {

        subject0.setPrimaryContactNumber(SubjectDataEncryptor.encrypt("Foo"));

        batchSubjects.run();

        myVerifyWithChangesText("primaryContactNumber:'Foo'--->'(123) 456-7890'");
    }

    @Test
    public void testSecondaryChanges() throws Exception {

        subject0.setSecondaryContactNumber(SubjectDataEncryptor.encrypt("Foo"));

        batchSubjects.run();

        myVerifyWithChangesText("secondaryContactNumber:'Foo'--->'(123) 456-7890'");
    }

    @Test
    public void testGenderChanges() throws Exception {

        patient0.setGender("Foo");

        batchSubjects.run();

        myVerifyWithChangesText("genderEmpi:'M'--->'Foo'");
    }

    @Test
    public void testRaceChanges() throws Exception {

        Race race2 = new Race();
        race2.setName("Foo");
        race2.setId(2);

        subject0.setRace(race2);

        when(batchSubjects.findEntityByFieldString("Race", "name", "Foo")).thenReturn(race2);

        batchSubjects.run();

        myVerifyWithChangesText("race:'Foo'--->'R0'");
    }

    @Test
    public void testEthnicityChanges() throws Exception {

        Ethnicity ethnicity2 = new Ethnicity();
        ethnicity2.setName("Foo");
        ethnicity2.setId(2);

        Ethnicity ethnicityChinese = new Ethnicity();
        ethnicityChinese.setName("CHINESE");
        ethnicityChinese.setId(3);

        subject0.setEthnicity(ethnicity2);

        when(batchSubjects.findEntityByFieldString("Ethnicity", "name", "Foo")).thenReturn(ethnicity2);
        when(epicSubjectDAO.lookupEmpiEthnicityString("CHINESE")).thenReturn(ethnicityChinese);

        batchSubjects.run();

        myVerifyWithChangesText("ethnicity:'Foo'--->'CHINESE'");
    }

    @Test
    public void testCountryChanges() throws Exception {

        Country country2 = new Country();
        country2.setName("Foo");
        country2.setId(2);

        subject0.setCountry(country2);

        when(batchSubjects.findEntityByFieldString("Country", "name", "Foo")).thenReturn(country2);

        batchSubjects.run();

        myVerifyWithChangesText("country:'Foo'--->'Country0'");
    }

    @Test
    public void testStateChanges() throws Exception {

        State state2 = new State();
        state2.setName("Foo");
        state2.setId(2);

        subject0.setState(state2);

        when(batchSubjects.findEntityByFieldString("State", "name", "Foo")).thenReturn(state2);

        batchSubjects.run();

        myVerifyWithChangesText("state:'Foo'--->'State0'");
    }

    @Test
    public void testMrnStatusChanges() throws Exception {

        SubjectMrn subjectMrn = subject0.getFirstSubjectMrn(site).get();
        subjectMrn.setStatus("S2");

        batchSubjects.run();

        myVerifyWithChangesText("mrn status:'S2'--->'S0'");
    }

    @Test
    public void testCoupleOfChanges() throws Exception {

        subject0.setSecondaryContactNumber(SubjectDataEncryptor.encrypt("Foo"));

        SubjectMrn subjectMrn = subject0.getFirstSubjectMrn(site).get();
        subjectMrn.setStatus("S2");

        batchSubjects.run();

        myVerifyWithChangesText("secondaryContactNumber:'Foo'--->'(123) 456-7890'\n" +
                "mrn status:'S2'--->'S0'");
    }

    @Before
    public void setup() {
        Logger root = Logger.getRootLogger();
        root.addAppender(mockAppender);
        root.setLevel(Level.INFO);

        FactoryMockKey keyFactory = new FactoryMockKey();
        key = keyFactory.createKey();
        SubjectDataEncryptor.setEncryptionKey(key);

        SubjectDAO subjectDAO = mock(SubjectDAO.class);

        epicSubjectDAO = mock(EpicSubjectDAO.class);
        epicSubjectService = new EpicSubjectService(epicSubjectDAO, null);
        batchSubjects = spy(new BatchSubjects(subjectDAO, key, epicSubjectService, epicSubjectDAO));

        subjectMrnList = new ArrayList<>();


        SubjectMrn subjectMrn = setupSubject(0);
        subjectMrnList.add(subjectMrn);

        Patient patient = setupPatient(0);
        patient0 = patient;

        empiSubjectDto = mock(EmpiSubjectDto.class);
        List<Patient> patientList = Lists.newArrayList(patient);
        Patients patients = new Patients(patientList);
        when(empiSubjectDto.getPatients()).thenReturn(patients);

        when(epicSubjectDAO.getSearchedSubjectsAsEmpiSubjectDto(any(ExternalSubjectQueryBuilder.class), eq(true))).thenReturn(empiSubjectDto);

        subjectMrn0 = subjectMrnList.get(0);

        when(subjectDAO.findNumberOfSubjectMrns()).thenReturn(NUM_SUBJECTS);
        when(subjectDAO.findAllSubjectMrns()).thenReturn(subjectMrnList);

        staleSubject = new Subject();
        staleSubject.setId(2);
        State oldState = new State();
        oldState.setName("oldState");
        staleSubject.setState(oldState);

        Country oldCountry = new Country();
        oldCountry.setName("oldCountry");
        staleSubject.setCountry(oldCountry);

        Ethnicity oldEthnicity = new Ethnicity();
        oldEthnicity.setName("oldEthnicity");
        staleSubject.setEthnicity(oldEthnicity);

        Race race = new Race();
        race.setName("oldRace");
        staleSubject.setRace(race);
    }

    SubjectMrn setupSubject(int i) {
        Subject subject = new Subject();
        subject.setId(1);

        String schedulerMrn = SubjectDataEncryptor.encrypt("000" + i);

        Set<SubjectMrn> subjectMrnSet = new HashSet<>();
        SubjectMrn subjectMrn = new SubjectMrn(subject, schedulerMrn, site, "S" + i, null);
        subjectMrnSet.add(subjectMrn);
        subject.setSubjectMrnSet(subjectMrnSet);

        subject.setFirstName("First" + i);
        subject.setLastName("Last" + i);
        subject.setMiddleName("MI" + i);

        subject.setPuid(String.valueOf(i));

        java.util.Date dobDate = DateUtility.parse(
                DateUtility.monthDayYear(),
                "01/1" + i + "/1928");
        subject.setBirthdate(dobDate);

        subject.setStreetAddress1("Address1" + i);
        subject.setStreetAddress2("Address2" + i);
        subject.setCity("City" + i);

        State state = new State();
        state.setName("State" + i);
        state.setId(1);
        subject.setState(state);
        when(batchSubjects.findEntityByFieldString("State", "name", "State" + i)).thenReturn(state);


        subject.setZip("0000" + i);

        Country country = new Country();
        country.setName("Country" + i);
        country.setId(1);
        subject.setCountry(country);
        when(batchSubjects.findEntityByFieldString("Country", "name", "Country" + i)).thenReturn(country);


        subject.setPrimaryContactNumber("(123) 456-789" + i);
        subject.setSecondaryContactNumber("(123) 456-789" + i);

        subject.setGenderEmpi("M");

        Race race = new Race();
        race.setName("R" + i);
        race.setId(1);
        subject.setRace(race);
        when(batchSubjects.findEntityByFieldString("Race", "name", "R" + i)).thenReturn(race);

        Ethnicity ethnicity = new Ethnicity();
        ethnicity.setName("CHINESE");
        ethnicity.setId(1);
        subject.setEthnicity(ethnicity);

        Map<String,Ethnicity> map = Maps.newHashMap();
        map.put("CHINESE", ethnicity);
        when(epicSubjectDAO.loadEmpiToSchedulerEthnicityMap()).thenReturn(map);

        SubjectDataEncryptor.encryptSubjectInPlace(subject);

        subject0 = subject;

        return subjectMrn;
    }

    Patient setupPatient(int i) {
        Patient patient = new Patient();

        Mrn patientMrn = new Mrn(site, "000" + i, "S" + i);
        Mrns mrns = new Mrns();
        mrns.setMrnList(Lists.newArrayList(patientMrn));
        patient.setMrns(mrns);

        Name name = new Name("Last" + i,
                            "First" + i,
                            "MI" + i);
        name.setSuffix("Suffix" + i);
        patient.setName(name);

        patient.setUid(i);
        patient.setDobString("01/1" + i + "/1928");
        patient.setGender("M");

        Address address = new Address(
                "Address1" + i,
                "Address2" + i,
                "City" + i,
                "State" + i,
                "0000" + i,
                "Country" + i
                );
        patient.setAddress(address);

        Phone home = new Phone("Home", "(123) 456-789" + i);
        Phone work = new Phone("Work", "(123) 456-789" + i);
        Phones phones = new Phones(Lists.newArrayList(home, work));

        patient.setPhones(phones);

        OtherPid otherPid = new OtherPid();
        otherPid.setRace("R" + i);
        patient.setOtherPid(otherPid);

        edu.harvard.catalyst.scheduler.dto.Epic.EmpiSubjectDto.Ethnicity ethnicity =
                new edu.harvard.catalyst.scheduler.dto.Epic.EmpiSubjectDto.Ethnicity();
        ethnicity.setEthnic1("CHINESE");
        patient.setEthnicity(ethnicity);

        return patient;
    }

    void myVerifyNoChanges() {
        verify(mockAppender, atLeast(1)).doAppend(captorLoggingEvent.capture());
        List<LoggingEvent> loggingEvents = captorLoggingEvent.getAllValues();

        List<String> changeMessages = loggingEvents.stream()
                .map(lm -> (String) lm.getMessage())
                .filter(s -> s.contains("No Changes for Subject: 1"))
                .collect(Collectors.toList());

        assertTrue(changeMessages.size() == 1);
    }

    void myVerifyWithChangesText(String changesText) {
        verify(mockAppender, atLeast(1)).doAppend(captorLoggingEvent.capture());
        List<LoggingEvent> loggingEvents = captorLoggingEvent.getAllValues();

        List<String> changeMessages = loggingEvents.stream()
                .map(lm -> (String) lm.getMessage())
                .filter(s -> s.contains("There are changes for Subject: 1"))
                .collect(Collectors.toList());

        assertTrue(changeMessages.size() == 1);

        String expectedChanges = "Subject 1. Mrn 0000-MGH-S0: " + changesText + "\n";
        String encryptedChanges = SubjectDataEncryptor.encrypt(expectedChanges);
        verify(epicSubjectDAO, times(1)).logNightlyBatchDeltas(encryptedChanges);

        return;
    }
    //stateEntity = (State)findEntityByFieldString("State", "name", stateString);

    @Test
    public void testRefreshExternalSubjectForBatchUpdate() {

        Integer id = 1;
        String site = "MGH";
        String mrn = SubjectDataEncryptor.encrypt("0000");

        Subject subject = new Subject();
        subject.setId(id);

        SubjectMrn subjectMrn = new SubjectMrn();
        subjectMrn.setSubject(subject);
        subjectMrn.setId(id);
        subjectMrn.setStatus("A");
        subjectMrn.setMrn(mrn);
        subjectMrn.setSite(site);

        subject.setSubjectMrnSet(Sets.newHashSet(subjectMrn));

        when(epicSubjectDAO.findById(SubjectMrn.class, id)).thenReturn(subjectMrn);

        doReturn(true).when(batchSubjects).sanityCheckEmpiMatchCount(refEq(empiSubjectDto), eq(site), eq(id));
        doReturn("something changed").when(batchSubjects).imposeUpon(any(Subject.class), any(EmpiSubjectDto.class), eq(site), any(SubjectMrn.class));
        doReturn(mrn).when(batchSubjects).decryptMrn(mrn);

        doNothing().when(epicSubjectDAO).encryptAndSave(refEq(subject));

        assertTrue(batchSubjects.refreshExternalSubject(subjectMrn, true));

        verify(batchSubjects, times(1)).sanityCheckEmpiMatchCount(empiSubjectDto, site, id);
        verify(batchSubjects, times(1)).imposeUpon(any(Subject.class), any(EmpiSubjectDto.class), eq(site), any(SubjectMrn.class));
        verify(epicSubjectDAO, times(1)).encryptAndSave(subject);
    }

    @Test
    public void testImposeUpon() {

        Subject subject = new Subject();
        subject.setPuid("0");
        subject.setLastName("");
        subject.setFirstName("");
        subject.setMiddleName("");
        subject.setStreetAddress1("");
        subject.setStreetAddress2("");
        subject.setCity("");
        subject.setZip("");
        subject.setPrimaryContactNumber(null);
        subject.setSecondaryContactNumber(null);
        subject.setSubjectMrnSet(Sets.newHashSet());
        subject.setGenderEmpi("");

        Set<SubjectMrn> subjectMrnSet = Sets.newHashSet();
        SubjectMrn subjectMrn = new SubjectMrn(subject, subjectMrn0.getMrn(), subjectMrn0.getSite(), subjectMrn0.getStatus());
        subjectMrnSet.add(subjectMrn);
        subject.setSubjectMrnSet(subjectMrnSet);

        Patient patient = new Patient();
        patient.setGender("");
        patient.setName(new Name("", "", ""));
        patient.setAddress(new Address("", "", "", "", "", ""));

        EmpiSubjectDto empiSubjectDto = new EmpiSubjectDto(Lists.newArrayList(patient));

        String changes = batchSubjects.imposeUpon(subject, empiSubjectDto, "mgh", subjectMrn);
        assertTrue(changes.isEmpty());

        Name name = new Name("last", "first", "mi");
        patient.setName(name);

        patient.setGender("Male");

        changes = batchSubjects.imposeUpon(subject, empiSubjectDto, "mgh", subjectMrn);
        assertEquals("Last name not persisted", subject.getLastName(), "last");
        assertEquals("First name not persisted", subject.getFirstName(), "first");
        assertEquals("Middle name not persisted", subject.getMiddleName(), "mi");
        assertEquals("name change",
                "lastName:''--->'last'\n" +
                        "firstName:''--->'first'\n" +
                        "middleName:''--->'mi'\n" +
                        "genderEmpi:''--->'Male'\n",
                changes);

        // TODO: document/explain what is being tested here

        subject.setLastName("oldLast");
        subject.setFirstName("oldFirst");
        subject.setMiddleName("oldMiddle");

        patient.setGender("Female");

        changes = batchSubjects.imposeUpon(subject, empiSubjectDto, "mgh", subjectMrn);
        assertEquals("old name change",
                "lastName:'oldLast'--->'last'\n" +
                        "firstName:'oldFirst'--->'first'\n" +
                        "middleName:'oldMiddle'--->'mi'\n" +
                        "genderEmpi:'Male'--->'Female'\n",
                changes);

        return;
    }

    @Test
    public void testSanityCheckEmpiMatchCount() {
        EmpiSubjectDto empiSubjectDTO = null;
        String institution = null;
        Integer id = null;

        boolean result = batchSubjects.sanityCheckEmpiMatchCount(empiSubjectDTO, institution, id);
        assertFalse("Null empiSubjectDTO", result);

        List<Patient> patientList = new ArrayList<>();
        Patient patient1 = new Patient();
        patient1.setName(new Name("Last1", "First1", "Middle1"));
        patientList.add(patient1);
        empiSubjectDTO = new EmpiSubjectDto(patientList);
        institution = "inst";
        id = 1000;
        result = batchSubjects.sanityCheckEmpiMatchCount(empiSubjectDTO, institution, id);
        assertTrue("One patient in empiSubjectDTO", result);

        Patient patient2 = new Patient();
        patient1.setName(new Name("Last2", "First2", "Middle2"));
        patientList.add(patient2);
        patientList.add(patient2);
        result = batchSubjects.sanityCheckEmpiMatchCount(empiSubjectDTO, institution, id);
        assertFalse("One patient in empiSubjectDTO", result);
    }
    @Test
    public void testRelevantMrn() {
        Patient patient = new Patient();
        Mrns mrns = new Mrns();
        List<Mrn> mrnList = new ArrayList<>();
        String institution1 = "inst1";
        String institution2 = "inst2";
        String status = "A";
        String mrnValue = "mrn";
        Mrn mrn1 = new Mrn(institution1, mrnValue, status);
        Mrn mrn2 = new Mrn(institution2, mrnValue, status);
        mrnList.add(mrn1);
        mrnList.add(mrn2);
        mrns.setMrnList(mrnList);
        patient.setMrns(mrns);

        Mrn resultMrn = batchSubjects.relevantMrn(patient, institution2, mrnValue);
        assertEquals("mrn with matching institution", resultMrn.getSite(), institution2);

        resultMrn = batchSubjects.relevantMrn(patient, "myinst", mrnValue);
        assertNull("no mrn with matching institution:", resultMrn);
    }

    @Test
    public void testDecryptMrn()
    {
        String mrn = "1000";

        String encryptedMrn = SubjectDataEncryptor.encrypt(mrn);
        String expectedResult = SubjectDataEncryptor.decrypt(encryptedMrn);
        String actualResult = batchSubjects.decryptMrn(encryptedMrn);
        org.junit.Assert.assertEquals("mrn decrypted", expectedResult, actualResult);
    }

    @Test
    public void testFindEntityByFieldString()
    {
        String table = "tableName";
        String column = "columnName";
        String columnValue = "columnValue";
        batchSubjects.findEntityByFieldString(table, column, columnValue);
        verify(epicSubjectDAO, times(1)).findEntityByFieldString(table, column, columnValue);
    }

    @Test
    public void testImposeCountry()
    {
        Country countryEntity = new Country();
        String countryString = "incomingCountry";
        countryEntity.setName(countryString);

        Address address = new Address(null,
                null,
                null,
                null,
                null,
                countryEntity.getName());

        Patient patient = new Patient();
        patient.setAddress(address);

        Country mappedCountry = new Country();
        mappedCountry.setName("mappedCountry");
        mappedCountry.setId(2);

        StringBuilder changes = new StringBuilder();
        when(epicSubjectDAO.findEntityByFieldString("Country", "name", countryString)).thenReturn(null);
        batchSubjects.imposeCountry(staleSubject, patient, changes);
        org.junit.Assert.assertEquals("impose country - mapping failed", "oldCountry", staleSubject.getCountry().getName());

        when(epicSubjectDAO.findEntityByFieldString("Country", "name", countryString)).thenReturn(mappedCountry);
        batchSubjects.imposeCountry(staleSubject, patient, changes);
        org.junit.Assert.assertEquals("impose country", mappedCountry.getName(), staleSubject.getCountry().getName());
    }

    @Test
    public void testImposeState()
    {
        State stateEntity = new State();
        String stateString = "incomingState";
        stateEntity.setName(stateString);

        Address address = new Address(null,
                null,
                null,
                stateEntity.getName(),
                null,
                null);

        Patient patient = new Patient();
        patient.setAddress(address);

        State mappedState = new State();
        mappedState.setName("mappedState");
        mappedState.setId(2);
        StringBuilder changes = new StringBuilder();

        when(epicSubjectDAO.findEntityByFieldString("State", "name", stateString)).thenReturn(null);
        batchSubjects.imposeState(staleSubject, patient, changes);
        org.junit.Assert.assertEquals("impose state - mapping failed", "oldState", staleSubject.getState().getName());

        when(epicSubjectDAO.findEntityByFieldString("State", "name", stateString)).thenReturn(mappedState);
        batchSubjects.imposeState(staleSubject, patient, changes);
        org.junit.Assert.assertEquals("impose state", mappedState.getName(), staleSubject.getState().getName());
    }

    @Test
    public void testImposeEthnicity()
    {
        Patient patient = new Patient();
        EmpiSubjectDto.Ethnicity patientEthnicity = new EmpiSubjectDto.Ethnicity();
        String newEthnicityName = "new";
        patientEthnicity.setEthnic1(newEthnicityName);
        patient.setEthnicity(patientEthnicity);
        StringBuilder changes = new StringBuilder();

        when(epicSubjectDAO.lookupEmpiEthnicityString(newEthnicityName)).thenReturn(null);
        batchSubjects.imposeEthnicity(staleSubject, patient, changes);
        org.junit.Assert.assertEquals("impose ethnicity - mapping failed", "oldEthnicity", staleSubject.getEthnicity().getName());

        Ethnicity mappedEthnicity = new Ethnicity();
        mappedEthnicity.setName("mappedEthnicity");
        mappedEthnicity.setId(2);
        when(epicSubjectDAO.lookupEmpiEthnicityString(newEthnicityName)).thenReturn(mappedEthnicity);
        batchSubjects.imposeEthnicity(staleSubject, patient, changes);
        org.junit.Assert.assertEquals("impose ethnicity", mappedEthnicity.getName(), staleSubject.getEthnicity().getName());
    }

    @Test
    public void testImposeRace()
    {
        EmpiSubjectDto.OtherPid patientOtherPid = new EmpiSubjectDto.OtherPid();
        String newRaceName = "newRace";
        patientOtherPid.setRace(newRaceName);
        Patient patient = new Patient();
        patient.setOtherPid(patientOtherPid);
        StringBuilder changes = new StringBuilder();

        when(epicSubjectDAO.findEntityByFieldString("Race", "name", newRaceName)).thenReturn(null);
        batchSubjects.imposeRace(staleSubject, patient, changes);
        org.junit.Assert.assertEquals("impose race - mapping failed", "oldRace", staleSubject.getRace().getName());

        Race mappedRace = new Race();
        mappedRace.setName("mappedRace");
        mappedRace.setId(2);
        when(epicSubjectDAO.findEntityByFieldString("Race", "name", newRaceName)).thenReturn(mappedRace);
        batchSubjects.imposeRace(staleSubject, patient, changes);
        org.junit.Assert.assertEquals("impose race", mappedRace.getName(), staleSubject.getRace().getName());
    }
    @Test
    public void testPhone()
    {
        String correct = "(111) 111-1111";
        assertEquals("", correct, batchSubjects.canonicalPhoneNumber("1111111111"));
        assertEquals("", correct, batchSubjects.canonicalPhoneNumber("111-111-1111"));
        assertEquals("", correct, batchSubjects.canonicalPhoneNumber("11---1111-1111"));
        assertEquals("", correct, correct);
    }
    public void testRelevantMrn2() {
        // from empi
        Patient patient = new Patient();
        Mrns mrns = new Mrns();
        List<Mrn> mrnList = new ArrayList<>();
        String institution = "S0";
        String status = "A";
        String status2 = "M";
        String mrnValue = "1000";
        String mrnValue1 = "1001";
        Mrn mrn1 = new Mrn(institution, mrnValue, status);
        Mrn mrn2 = new Mrn(institution, mrnValue1, status2);
        mrnList.add(mrn1);
        mrnList.add(mrn2);
        mrns.setMrnList(mrnList);
        patient.setMrns(mrns);

        // from scheduler
        SubjectMrn subjectMrn = setupSubject(0);  // mrn : 1000
        subjectMrn.setMrn("1001");
        subjectMrn.setStatus("M");
        subjectMrn.setSite(institution);


        Mrn resultMrn = batchSubjects.relevantMrn(patient, institution, subjectMrn.getMrn());
        assertEquals("", resultMrn.getStatus(), "M");        // status2 should match here
    }

}
