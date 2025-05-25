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
package edu.harvard.catalyst.scheduler.persistence;

import java.security.Key;
import java.util.Date;
import java.util.Optional;
import java.util.function.Predicate;

import static edu.harvard.catalyst.scheduler.persistence.SubjectDAO.normalizeFilterString;
import static edu.harvard.catalyst.scheduler.persistence.SubjectDAO.subjectsPredicate;
import static org.junit.Assert.*;

import com.google.common.collect.Sets;
import edu.harvard.catalyst.scheduler.AbstractSpringWiredHibernateTest;
import edu.harvard.catalyst.scheduler.dto.response.Address;
import edu.harvard.catalyst.scheduler.dto.response.SubjectsResponseDTO;
import edu.harvard.catalyst.scheduler.entity.*;
import edu.harvard.catalyst.scheduler.persistence.SubjectDAO.Row;
import edu.harvard.catalyst.scheduler.util.DateUtility;
import edu.harvard.catalyst.scheduler.util.FactoryMockKey;
import edu.harvard.catalyst.scheduler.util.SubjectDataEncryptor;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;


public final class SubjectDAOTest extends AbstractSpringWiredHibernateTest
{
    private Key encryptionKey;

    @Autowired
    private SubjectDAO subjectDAO;

    @Before
    public void before() {
        FactoryMockKey keyFactory = new FactoryMockKey();
        encryptionKey = keyFactory.createKey();
        SubjectDataEncryptor.setEncryptionKey(encryptionKey);
    }

    @Test
    public void testSubjectsPredicate() {
        assertTrue(subjectsPredicate(Optional.empty()).test(null));
        
        final Predicate<Row> subjectsPredicate = subjectsPredicate(Optional.of("FOO"));
        
        assertFalse(subjectsPredicate.test(row("", "")));
        assertFalse(subjectsPredicate.test(row("foo", "")));
        assertFalse(subjectsPredicate.test(row("", "foo")));
        
        assertTrue(subjectsPredicate.test(row("FOO", "")));
        assertTrue(subjectsPredicate.test(row("", "FOO")));
        assertTrue(subjectsPredicate.test(row("FOO", "FOO")));
        
    }
    
    private static final Row row(final String mrn, final String lastName) {
        return new Row.RowBuilder()
                .id(42)
                .lastName(lastName)
                .firstName("")
                .mrn(mrn)
                .dob(new Date())
                .gender("")
                .address(new Address())
                .primaryContact("")
                .status(true).createRow();
    }
    
    @Test
    public void testNormalizeFilterString() {
        assertEquals(Optional.empty(), normalizeFilterString(null));
        assertEquals(Optional.empty(), normalizeFilterString(""));
        assertEquals(Optional.empty(), normalizeFilterString(" "));
        assertEquals(Optional.empty(), normalizeFilterString("    "));
        
        assertEquals(Optional.of("FOO"), normalizeFilterString("FOO"));
        assertEquals(Optional.of("FOO"), normalizeFilterString("foo"));
        assertEquals(Optional.of("FOO"), normalizeFilterString("fOo"));
        assertEquals(Optional.of("FOO"), normalizeFilterString(" foo"));
        assertEquals(Optional.of("FOO"), normalizeFilterString("FOo "));
        assertEquals(Optional.of("FOO"), normalizeFilterString("  foo "));
    }

    @Test
    public void testEncryptIgnoringLiteralNulls() {
        assertEquals(null, SubjectDAO.encryptIgnoringLiteralNullsNullsOrBlanks("null"));
        assertEquals(null, SubjectDAO.encryptIgnoringLiteralNullsNullsOrBlanks(null));
    }

    @Test
    public void testIsLiteralNullOrNull() {
        assertFalse(SubjectDAO.isLiteralNullNullOrBlank("joey"));
        assertTrue(SubjectDAO.isLiteralNullNullOrBlank(null));
        assertTrue(SubjectDAO.isLiteralNullNullOrBlank("null"));
        assertTrue(SubjectDAO.isLiteralNullNullOrBlank("nULl"));
    }

    Subject setupForMrnTests() {

        Subject subject = new Subject();
        SubjectMrn subjectMrn = new SubjectMrn();
        subjectMrn.setSubject(subject);
        subjectMrn.setMrn("myMrn");

        subject.setSubjectMrnSet(Sets.newHashSet(subjectMrn));

        return subject;
    }
    @Test
    public void testMrnIs() {

        Subject subject = setupForMrnTests();

        assertTrue(SubjectDAO.mrnMatchesIgnoreCaseExactly(subject, "myMrn"));
        assertFalse(SubjectDAO.mrnMatchesIgnoreCaseExactly(subject, "notMine"));
    }

    @Test
    public void testMrnMatches() {

        Subject subject = setupForMrnTests();

        assertTrue(SubjectDAO.mrnMatchesIgnoreCaseAtLeastPartially(subject, "myMrn"));
        assertFalse(SubjectDAO.mrnMatchesIgnoreCaseAtLeastPartially(subject, "notMine"));

        assertTrue(SubjectDAO.mrnMatchesIgnoreCaseAtLeastPartially(subject, "my"));
        assertTrue(SubjectDAO.mrnMatchesIgnoreCaseAtLeastPartially(subject, "yM"));
    }

    @Test
    public void testFromSubject()
    {
        Subject subject = new Subject();
        subject.setId(2);
        subject.setFirstName("myFirstName");
        subject.setMiddleName("myMiddleName");
        subject.setLastName("myLastName");
        subject.setPrimaryContactNumber("primaryContact");
        Date date = new Date();
        subject.setBirthdate(date);
        subject.setStreetAddress1("addressLine1");
        subject.setStreetAddress2("addressLine2");
        subject.setCity("city");

        State state = new State();
        state.setName("state");
        subject.setState(state);
        subject.setZip("zip");

        Gender gender = new Gender();
        gender.setName("myGender");
        gender.setCode("MG");
        subject.setGender(gender);

        SubjectMrn subjectMrn = new SubjectMrn();
        subjectMrn.setSubject(subject);
        subjectMrn.setMrn("myMrn");
        subjectMrn.setSite("site");

        SubjectDAO.Row row = SubjectDAO.Row.fromSubject(subjectMrn);
        SubjectsResponseDTO subjectsResponseDTO = row.toSubjectsResponse();

        assertEquals("subject row data id", subject.getId().intValue(), subjectsResponseDTO.getId());
        assertEquals("subject row data first name", subject.getFirstName(), subjectsResponseDTO.getFirstName());
        assertEquals("subject row data middle name", subject.getMiddleName(), subjectsResponseDTO.getMiddleName());
        assertEquals("subject row data last name", subject.getLastName(), subjectsResponseDTO.getLastName());
        assertEquals("subject row data gender", subject.getGender().getName(), subjectsResponseDTO.getGender());
        assertEquals("subject row data dob", subject.getBirthdate(), subjectsResponseDTO.getDob());
        assertEquals("subject row data primary contact", subject.getPrimaryContactNumber(),
                subjectsResponseDTO.getPrimaryContact());
        assertEquals("subject row data city", subject.getCity(), subjectsResponseDTO.getAddress().getCity());
        assertEquals("subject row data state", subject.getState().getName().toUpperCase(), subjectsResponseDTO.getAddress().getStateName());
    }

    @Test
    public void testGetSubjectMrnByMrnAndSite(){
        Subject subject = createTestSubject(10);

        String testMrn = "1234";
        String encrypedTestMrn = SubjectDataEncryptor.encrypt(testMrn);
        String testSite = "INST";
        SubjectMrn subjectMrn = new SubjectMrn();
        subjectMrn.setSubject(subject);
        subjectMrn.setMrn(encrypedTestMrn);
        subjectMrn.setSite(testSite);

        save(subjectMrn);

        SubjectMrn foundSubjectMrn = subjectDAO.getSubjectMrnByMrnAndSite(testMrn, testSite);
        assertNotNull(foundSubjectMrn);

        assertEquals(encrypedTestMrn, foundSubjectMrn.getMrn());
        assertEquals(testSite, foundSubjectMrn.getSite());


        String testMrn2 = "5678";
        String encrypedTestMrn2 = SubjectDataEncryptor.encrypt(testMrn2);
        SubjectMrn subjectMrnNoSite = new SubjectMrn();
        subjectMrnNoSite.setMrn(encrypedTestMrn2);
        subjectMrnNoSite.setSubject(subject);
        save(subjectMrnNoSite);

        foundSubjectMrn = subjectDAO.getSubjectMrnByMrnAndSite(testMrn2, testSite);
        assertEquals(encrypedTestMrn2, foundSubjectMrn.getMrn());
        assertNull(foundSubjectMrn.getSite());
    }

    private Subject createTestSubject(int id){
        Subject subject = new Subject();
        subject.setId(id);
        subject.setFirstName("myFirstName");
        subject.setMiddleName("myMiddleName");
        subject.setLastName("myLastName");
        subject.setPrimaryContactNumber("primaryContact");
        Date date = new Date();
        subject.setBirthdate(date);
        subject.setStreetAddress1("addressLine1");
        subject.setStreetAddress2("addressLine2");
        subject.setCity("city");

        Gender gender = new Gender();
        gender.setName("Female");
        gender.setCode("F");
        save(gender);
        GenderType genderType = GenderType.F;
        subject.setGenderType(genderType);
        subject.setGender(gender);

        State state = new State();
        state.setName("state");
        save(state);
        subject.setState(state);
        subject.setZip("zip");

        save(subject);

        return subject;
    }
}
