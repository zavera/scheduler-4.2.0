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
package edu.harvard.catalyst.scheduler.util;

import static edu.harvard.catalyst.scheduler.util.SubjectDataEncryptor.decrypt;
import static edu.harvard.catalyst.scheduler.util.SubjectDataEncryptor.encrypt;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.security.Key;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import edu.harvard.catalyst.scheduler.AbstractSpringWiredJunit4Test;
import edu.harvard.catalyst.scheduler.dto.SubjectsDTO;
import edu.harvard.catalyst.scheduler.dto.response.MrnInfoDTO;
import edu.harvard.catalyst.scheduler.entity.*;
import edu.harvard.catalyst.scheduler.persistence.AppointmentDAO;
import edu.harvard.catalyst.scheduler.persistence.StudyDAO;
import edu.harvard.catalyst.scheduler.persistence.SubjectDAO;
import edu.harvard.catalyst.scheduler.service.AuditService;
import edu.harvard.catalyst.scheduler.service.SubjectServiceClassic;
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

public final class SubjectDataEncryptorTest extends AbstractSpringWiredJunit4Test {
    @Autowired @Qualifier("encryptionKey")
    private Key encryptionKey;


    private SubjectDAO mockSubjectDAO;
    private AuditService mockAuditService;
    private StudyDAO mockStudyDAO;
    private AppointmentDAO mockAppointmentDAO;

    private User mockUser;

    @Before
    public void before() {

        SubjectDataEncryptor.setEncryptionKey(encryptionKey);

        mockUser = mock(User.class);
        mockSubjectDAO  = mock(SubjectDAO.class);
        mockAppointmentDAO = mock(AppointmentDAO.class);
        mockAuditService = mock(AuditService.class);
        mockStudyDAO = mock(StudyDAO.class);

        Institution institution = mock(Institution.class);
        when(institution.getName()).thenReturn("name");
        when(mockUser.getInstitution()).thenReturn(institution);
    }
    
    @Test
    public void testRoundTrip() {
        final String input = "John Subject, Esq.";
        
        final String roundTripped = decrypt(encrypt(input));
        
        assertEquals(input, roundTripped);
    }

    @Test
    public void testNull(){
        String empty =  SubjectDataEncryptor.encrypt(null);
        assertEquals("", empty);

        Subject nullSubject = SubjectDataEncryptor.encryptSubjectInPlace(null);
        assertNull(nullSubject);

        SubjectMrn nullMrn = SubjectDataEncryptor.decryptSubjectMrnAndSubject(null);
        assertNull(nullMrn);
    }

    @Test
    public void testSubjectMrnEncryptionAndDecryption() throws Exception {

        SubjectsDTO subjectsDTO = ServiceTestsDTOUtility.createSubject(true);

        subjectsDTO.setPartnersUid("abc778");
        subjectsDTO.setMrnInfo(new MrnInfoDTO("199", "site1", "A"));
        subjectsDTO.setMiddleName("squeakers");
        subjectsDTO.setStreetAddress2("sub-basement #3");
        subjectsDTO.setSecondaryContactNumber("6786786788");;

        ServiceTestsDTOUtility.setUpSubjectDao(6, mockSubjectDAO, true);

        setupSaveSubjectMrn();

        SubjectServiceClassic subjectServiceClassic =
                new SubjectServiceClassic(mockAuditService, mockSubjectDAO, mockStudyDAO, null, mockAppointmentDAO, encryptionKey);

        Subject subject = subjectServiceClassic.createSubject(subjectsDTO, mockUser, "ipAddress goes here");


        //make sure all the items in the subject that *should* be encrypted are:

        notNullAndNotEquals(subjectsDTO.getFirstName(), subject.getFirstName());
        notNullAndNotEquals(subjectsDTO.getLastName(), subject.getLastName());
        notNullAndNotEquals("Joy Hall", subject.getFullName());
        notNullAndNotEquals(subjectsDTO.getZip(), subject.getZip());
        notNullAndNotEquals(subjectsDTO.getCity(), subject.getCity());
        notNullAndNotEquals(subjectsDTO.getPrimaryContactNumber(), subject.getPrimaryContactNumber());
        notNullAndNotEquals(subjectsDTO.getStreetAddress1(), subject.getStreetAddress1());

        //extra:
        notNullAndNotEquals(subjectsDTO.getMiddleName(), subject.getMiddleName());
        notNullAndNotEquals(subjectsDTO.getSecondaryContactNumber(), subject.getSecondaryContactNumber());
        notNullAndNotEquals(subjectsDTO.getPartnersUid(), subject.getPuid());
        notNullAndNotEquals(subjectsDTO.getStreetAddress2(), subject.getStreetAddress2());

        //now test the encrypted values themselves,  note:  if the key changes, then these assertions wil fail:
        testSubjectEncryptionValues(subject, true);

        //now test decryption:

        Set<SubjectMrn> subjectMrnSet = subject.getSubjectMrnSet();
        assertEquals(1, subjectMrnSet.size());

        Iterator<SubjectMrn> iterator = subjectMrnSet.iterator();

        if (!iterator.hasNext()) {
            throw new RuntimeException("Collection is empty");
        }
        SubjectMrn subjectMrn = iterator.next();

        SubjectMrn decryptedSubjectMrn = SubjectDataEncryptor.decryptSubjectMrnAndSubject(subjectMrn);

        checkDecryption(decryptedSubjectMrn);
        assertEquals("199", decryptedSubjectMrn.getMrn());

        //make sure original subject not decrypted, just the copy!
        testSubjectEncryptionValues(subject);

    }


    @Test
    public void testSubjectMrnEncryptionAndDecryptionWithOnlyRequiredFields() throws Exception {

        SubjectsDTO subjectsDTO = ServiceTestsDTOUtility.createSubject(true);

        subjectsDTO.setMrnInfo(new MrnInfoDTO("199", "site1", "A"));

        ServiceTestsDTOUtility.setUpSubjectDao(6, mockSubjectDAO, true);

        setupSaveSubjectMrn();

        SubjectServiceClassic subjectServiceClassic =
                new SubjectServiceClassic(mockAuditService, mockSubjectDAO, mockStudyDAO, null, mockAppointmentDAO, encryptionKey);

        Subject subject = subjectServiceClassic.createSubject(subjectsDTO, mockUser, "ipAddress goes here");


        //make sure all the items in the subject that *should* be encrypted are:

        notNullAndNotEquals(subjectsDTO.getFirstName(), subject.getFirstName());
        notNullAndNotEquals(subjectsDTO.getLastName(), subject.getLastName());
        notNullAndNotEquals("Joy Hall", subject.getFullName());
        notNullAndNotEquals(subjectsDTO.getZip(), subject.getZip());
        notNullAndNotEquals(subjectsDTO.getCity(), subject.getCity());
        notNullAndNotEquals(subjectsDTO.getPrimaryContactNumber(), subject.getPrimaryContactNumber());
        notNullAndNotEquals(subjectsDTO.getStreetAddress1(), subject.getStreetAddress1());

        //extra:
        assertNull(subject.getMiddleName());
        assertNull(subject.getSecondaryContactNumber());
        assertNull(subject.getPuid());
        assertNull(subject.getStreetAddress2());

        //now test the encrypted values themselves,  note:  if the key changes, then these assertions wil fail:
        testSubjectEncryptionValues(subject);

        //now test decryption:

        Set<SubjectMrn> subjectMrnSet = subject.getSubjectMrnSet();
        assertEquals(1, subjectMrnSet.size());

        Iterator<SubjectMrn> iterator = subjectMrnSet.iterator();

        if (!iterator.hasNext()) {
            throw new RuntimeException("Collection is empty");
        }
        SubjectMrn subjectMrn = iterator.next();

        SubjectMrn decryptedSubjectMrn = SubjectDataEncryptor.decryptSubjectMrnAndSubject(subjectMrn);

        checkDecryption(decryptedSubjectMrn);
        assertEquals("199", decryptedSubjectMrn.getMrn());

        //make sure original subject not decrypted, just the copy!
        testSubjectEncryptionValues(subject);

    }
    private void checkDecryption(SubjectMrn decryptedSubjectMrn) {
        checkDecryption(decryptedSubjectMrn, false);
    }

    private void checkDecryption(SubjectMrn decryptedSubjectMrn, boolean includeOptionalFields) {
        Subject decryptedSubject = decryptedSubjectMrn.getSubject();

        assertEquals("JOY", decryptedSubject.getFirstName());
        assertEquals("HALL", decryptedSubject.getLastName());
        assertEquals("JOY HALL", decryptedSubject.getFullName());
        assertEquals("02139", decryptedSubject.getZip());
        assertEquals("CAMBRIDGE", decryptedSubject.getCity());
        assertEquals("1231231233", decryptedSubject.getPrimaryContactNumber());
        assertEquals("120 LIBERTY PLAZA", decryptedSubject.getStreetAddress1());

        if (includeOptionalFields) {
            assertEquals("ABC778", decryptedSubject.getPuid());
            assertEquals("SQUEAKERS",decryptedSubject.getMiddleName());
            assertEquals("sub-basement #3",  decryptedSubject.getStreetAddress2());
            assertEquals("6786786788", decryptedSubject.getSecondaryContactNumber());
        }
    }


    @Test
    public void testDecryptionWithinStudySubject() throws Exception {
        StudySubject studySubject = new StudySubject();

        SubjectsDTO subjectsDTO = ServiceTestsDTOUtility.createSubject(true);
        subjectsDTO.setMrnInfo(new MrnInfoDTO("199", "site1", "A"));

        ServiceTestsDTOUtility.setUpSubjectDao(6, mockSubjectDAO, true);

        final Container subjectMrnContainer = new Container();

        doAnswer(new Answer() {
            public Object answer(InvocationOnMock invocation) {
                Object[] args = invocation.getArguments();

                // args[0]: the first arg, that I want to modify
                SubjectMrn subjectMrn = (SubjectMrn)args[0];
                subjectMrn.setId(11);

                subjectMrn.setMrn(SubjectDataEncryptor.encrypt(subjectMrn.getMrn()));

                subjectMrnContainer.setContents(subjectMrn);

                //return null since it's a void method:
                return null;
            }}).when(mockSubjectDAO).saveSubjectMrn(any(SubjectMrn.class));

        SubjectServiceClassic subjectServiceClassic =
                new SubjectServiceClassic(mockAuditService, mockSubjectDAO, mockStudyDAO, null, mockAppointmentDAO, encryptionKey);

        subjectServiceClassic.createSubject(subjectsDTO, mockUser, "ipAddress goes here");

        studySubject.setSubjectMrn(subjectMrnContainer.getContents());
        Study mockStudy = mock(Study.class);
        when(mockStudy.getId()).thenReturn(1);
        studySubject.setStudy(mockStudy);

        SubjectDataEncryptor.decryptSubjectWithinStudySubject(studySubject);

        SubjectMrn mrnWithDecSubject = studySubject.getSubjectMrn();
        checkDecryption(mrnWithDecSubject);
        assertEquals("WhkHYS+keLxwAwnqFeX19A==", mrnWithDecSubject.getMrn());
    }

    @Test
    public void testDecryptSubjectLastName() throws Exception {
        final SubjectsDTO subjectsDTO = ServiceTestsDTOUtility.createSubject(true);

        Subject subject = createSubject(subjectsDTO);
        Subject decryptedLastNameSubject = SubjectDataEncryptor.decryptSubjectLastName(subject);
        assertEquals(subjectsDTO.getLastName().toUpperCase(), decryptedLastNameSubject.getLastName());
        assertNotEquals(subjectsDTO.getFirstName().toUpperCase(), decryptedLastNameSubject.getFirstName());
        assertNotEquals(subjectsDTO.getMiddleName().toUpperCase(), decryptedLastNameSubject.getMiddleName());
        assertNotEquals(subjectsDTO.getPartnersUid().toUpperCase(), decryptedLastNameSubject.getPuid());
        assertNotEquals(subjectsDTO.getCity().toUpperCase(), decryptedLastNameSubject.getCity());

        MrnInfoDTO mrnInfoDTO = subjectsDTO.getMrnInfo();
        Set<SubjectMrn> decryptedSubjectMrnSet = decryptedLastNameSubject.getSubjectMrnSet();
        for(SubjectMrn decryptedSubjectMrn: decryptedSubjectMrnSet) {
            assertNotEquals(mrnInfoDTO.getValue(), decryptedSubjectMrn.getMrn());
        }
    }

    @Test
    public void testDecryptSubjectMrnWithinSubject() throws Exception {
        final SubjectsDTO subjectsDTO = ServiceTestsDTOUtility.createSubject(true);

        Subject subject = createSubject(subjectsDTO);
        Subject decryptedLastNameSubject = SubjectDataEncryptor.decryptSubjectMrnWithinSubject(subject);
        assertNotEquals(subjectsDTO.getLastName().toUpperCase(), decryptedLastNameSubject.getLastName());
        assertNotEquals(subjectsDTO.getFirstName().toUpperCase(), decryptedLastNameSubject.getFirstName());
        assertNotEquals(subjectsDTO.getMiddleName().toUpperCase(), decryptedLastNameSubject.getMiddleName());
        assertNotEquals(subjectsDTO.getPartnersUid().toUpperCase(), decryptedLastNameSubject.getPuid());
        assertNotEquals(subjectsDTO.getCity().toUpperCase(), decryptedLastNameSubject.getCity());

        MrnInfoDTO mrnInfoDTO = subjectsDTO.getMrnInfo();
        Set<SubjectMrn> decryptedSubjectMrnSet = decryptedLastNameSubject.getSubjectMrnSet();
        for(SubjectMrn decryptedSubjectMrn: decryptedSubjectMrnSet) {
            assertEquals(mrnInfoDTO.getValue(), decryptedSubjectMrn.getMrn());
        }
    }

    private Subject createSubject(SubjectsDTO subjectsDTO)throws Exception {
        final String firstName = "fooFirst";
        final String lastName = "fooLast";
        final String middleName = "squeakers";
        final String partnersUid = "abc778";
        subjectsDTO.setFirstName(firstName);
        subjectsDTO.setLastName(lastName);
        subjectsDTO.setPartnersUid(partnersUid);

        MrnInfoDTO mrnInfoDTO = new MrnInfoDTO("199", "site1", "A");
        subjectsDTO.setMrnInfo(mrnInfoDTO);
        subjectsDTO.setMiddleName(middleName);
        subjectsDTO.setStreetAddress2("sub-basement #3");
        subjectsDTO.setSecondaryContactNumber("6786786788");


        ServiceTestsDTOUtility.setUpSubjectDao(6, mockSubjectDAO, true);

        setupSaveSubjectMrn();

        SubjectServiceClassic subjectServiceClassic =
                new SubjectServiceClassic(mockAuditService, mockSubjectDAO, mockStudyDAO, null, mockAppointmentDAO, encryptionKey);

        Subject subject = subjectServiceClassic.createSubject(subjectsDTO, mockUser, "ipAddress goes here");

        return subject;
    }

    private void setupSaveSubjectMrn() {
        doAnswer(new Answer() {
            public Object answer(InvocationOnMock invocation) {
                Object[] args = invocation.getArguments();

                // args[0]: the first arg, that I want to modify
                SubjectMrn subjectMrn = (SubjectMrn)args[0];
                subjectMrn.setId(11);

                subjectMrn.setMrn(SubjectDataEncryptor.encrypt(subjectMrn.getMrn()));

                //return null since it's a void method:
                return null;
            }}).when(mockSubjectDAO).saveSubjectMrn(any(SubjectMrn.class));
    }

    private void testSubjectEncryptionValues(Subject subject) {
        testSubjectEncryptionValues(subject, false);
    }

    private void testSubjectEncryptionValues(Subject subject, boolean checkOptionalFields) {
        assertEquals("/HJ52HBrhuY2H11rEAaOew==", subject.getFirstName());
        assertEquals("9F/XTydlb0fMTzS1iyT8Cw==", subject.getLastName());
        assertEquals("obpNn594IVMipiMZaEkTrw==",subject.getFullName());
        assertEquals("tNLGvPNoDgh+QSl04Wz7FQ==",subject.getZip());
        assertEquals("suLjgbdreKu/FDLCxCIIYg==",subject.getCity());
        assertEquals("y1L9yOgcvP0SM9y63rX2Lg==",subject.getPrimaryContactNumber());
        assertEquals("M0p0uyhn5CON8rDo2iZEhJz/jl1xpWpQObcrNeUia8E=",subject.getStreetAddress1());

        if (checkOptionalFields) {
            assertEquals("YX6ptKV2Gaf/cU5SQ6D7yg==", subject.getPuid());
            assertEquals("t426XoG31aH9VC1RymxvWg==",  subject.getStreetAddress2());
            assertEquals("qUSPsUm5X9ayi4qQQNTkuQ==" , subject.getSecondaryContactNumber());
            assertEquals("OLay0wvFqgV95OWJ0js5eQ==", subject.getMiddleName());
        }

        Set<SubjectMrn> subjectMrnSet = subject.getSubjectMrnSet();
        List<SubjectMrn> subjectMrnList = new ArrayList(subjectMrnSet);
        SubjectMrn subjectMrn = subjectMrnList.get(0);

        //now test encryption of subjectMrn:
        assertNotEquals(subjectMrn.getMrn(), "199");
        assertEquals("WhkHYS+keLxwAwnqFeX19A==", subjectMrn.getMrn());
    }

    /**allows a final class to be changed within the doAnswer() mocking block */
    class Container {
        private SubjectMrn contents;

        public Container() {
        }

        public SubjectMrn getContents() {
            return contents;
        }

        public void setContents(SubjectMrn contents) {
            this.contents = contents;
        }
    }

    /**
     *  test that the other != original, and thet the 'other' is not null
     *  point is to make sure the value is encrypted (not equal to original)
     *  and also that it is not null
     */

    private void notNullAndNotEquals(String original, String other){
        assertNotNull(other);
        assertNotEquals(original, other);
    }


}
