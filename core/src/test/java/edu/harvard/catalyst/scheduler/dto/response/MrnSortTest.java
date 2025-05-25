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

import edu.harvard.catalyst.scheduler.AbstractSpringWiredJunit4Test;
import edu.harvard.catalyst.scheduler.entity.StudySubject;
import edu.harvard.catalyst.scheduler.entity.SubjectMrn;
import edu.harvard.catalyst.scheduler.util.SubjectDataEncryptor;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.security.Key;
import java.util.ArrayList;
import java.util.List;

import static edu.harvard.catalyst.scheduler.dto.response.GetStudySubjectsResponse.StudySubjectMRNComparatorAsc;
import static edu.harvard.catalyst.scheduler.dto.response.GetStudySubjectsResponse.StudySubjectMRNComparatorDesc;
import static org.junit.Assert.assertTrue;


public class MrnSortTest extends AbstractSpringWiredJunit4Test {
    @Autowired
    @Qualifier("encryptionKey")
    private Key encryptionKey;

    private final String mrn1Str = "0048";
    private final String mrn2Str = "3643448";
    private final String mrn3Str = "0062";
    private final String mrn4Str = "0021";

    private  StudySubject subj1;
    private  StudySubject subj2;
    private  StudySubject subj3;
    private  StudySubject subj4;

    private  List<StudySubject> studySubjects = new ArrayList<>();

    @Before
    public void before() {
        SubjectDataEncryptor.setEncryptionKey(encryptionKey);

        subj1 = makeStudySubject(mrn1Str);
        subj2 = makeStudySubject(mrn2Str);
        subj3 = makeStudySubject(mrn3Str);
        subj4 = makeStudySubject(mrn4Str);

        studySubjects.add(subj1);
        studySubjects.add(subj2);
        studySubjects.add(subj3);
        studySubjects.add(subj4);
    }

    @Test
    public void testMrnSorting() throws Exception {
    //ascending order is:  mrn4, mrn1, mrn3, mrn2

        studySubjects.sort(StudySubjectMRNComparatorAsc);

        //check sorted correctly:
        assertTrue(studySubjects.get(0).getDecryptedMrn().equals(mrn4Str));
        assertTrue(studySubjects.get(1).getDecryptedMrn().equals(mrn1Str));
        assertTrue(studySubjects.get(2).getDecryptedMrn().equals(mrn3Str));
        assertTrue(studySubjects.get(3).getDecryptedMrn().equals(mrn2Str));

        studySubjects.sort(StudySubjectMRNComparatorDesc);

        //check sorted correctly:
        assertTrue(studySubjects.get(0).getDecryptedMrn().equals(mrn2Str));
        assertTrue(studySubjects.get(1).getDecryptedMrn().equals(mrn3Str));
        assertTrue(studySubjects.get(2).getDecryptedMrn().equals(mrn1Str));
        assertTrue(studySubjects.get(3).getDecryptedMrn().equals(mrn4Str));
    }

    //////////////////////////////////////////
    //////////////////////////////////////////

    private StudySubject makeStudySubject(String mrn) {

        StudySubject result= new StudySubject();
        SubjectMrn mrn1 = new SubjectMrn();

        mrn1.setMrn(SubjectDataEncryptor.encrypt(mrn));
        mrn1.setSite("bwh");
        mrn1.setStatus("A");
        result.setSubjectMrn(mrn1);

        return result;
    }

}
