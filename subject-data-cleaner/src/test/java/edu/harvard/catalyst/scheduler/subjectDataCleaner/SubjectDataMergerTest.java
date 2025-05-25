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

package edu.harvard.catalyst.scheduler.subjectDataCleaner;

import edu.harvard.catalyst.scheduler.entity.*;
import edu.harvard.catalyst.scheduler.persistence.AppointmentDAO;
import edu.harvard.catalyst.scheduler.persistence.StudyDAO;
import edu.harvard.catalyst.scheduler.persistence.SubjectDAO;
import edu.harvard.catalyst.scheduler.service.StandaloneSubjectService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.*;

/**
 * Created by xavier on 6/27/16.
 */
@RunWith(MockitoJUnitRunner.class)
public class SubjectDataMergerTest {

    @Test
    public void testMergeSubject() {

        int primarySubjectId = 11;
        int secondarySubjectId = 22;

        String primaryMrnString = "0001";
        String secondaryMrnString = "0002";

        int studyWithOnlyPrimarySubjectId = 111;
        int studyWithBothSubjectsId = 222;
        int studyWithOnlySecondarySubjectId = 333;

        int studySubject1Id = 1111;
        int studySubject2Id = 2222;
        int studySubject3Id = 3333;
        int studySubject4Id = 4444;

        int bookedVisitStudy2Subject1Id = 22222;
        int bookedVisitStudy2Subject2Id = 33333;
        int bookedVisitStudy3Subject2Id = 44444;

        SubjectDAO subjectDAO = mock(SubjectDAO.class);
        StudyDAO studyDAO = mock(StudyDAO.class);
        AppointmentDAO appointmentDao = mock(AppointmentDAO.class);
        StandaloneSubjectService subjectService = new StandaloneSubjectService(subjectDAO);
        StandaloneSubjectService subjectServiceSpy = spy(subjectService);

        SubjectDataMerger subjectDataMerger = new SubjectDataMerger(null, subjectDAO, studyDAO, appointmentDao, subjectServiceSpy);
        subjectDataMerger.decrypt = false;

        // set up the PRIMARY subject with MRN = 0001
        Subject primarySubject = new Subject();
        primarySubject.setId(primarySubjectId);
        SubjectMrn primarySubjectMrn = new SubjectMrn();
        primarySubjectMrn.setMrn(primaryMrnString);
        primarySubjectMrn.setSubject(primarySubject);
        primarySubject.getSubjectMrnSet().add(primarySubjectMrn);
        List<Subject> primarySubjectAsaList = new ArrayList<>();
        primarySubjectAsaList.add(primarySubject);

        // set up the SECONDARY subject with MRN = 0002
        Subject secondarySubject = new Subject();
        secondarySubject.setId(secondarySubjectId);
        SubjectMrn secondarySubjectMrn = new SubjectMrn();
        secondarySubjectMrn.setMrn(secondaryMrnString);
        secondarySubjectMrn.setSubject(secondarySubject);
        secondarySubject.getSubjectMrnSet().add(secondarySubjectMrn);
        List<Subject> secondarySubjectAsaList = new ArrayList<>();
        secondarySubjectAsaList.add(secondarySubject);

        // set up a study (111), which will contain only the primary subject, 111
        Study studyWithOnlyPrimarySubject = new Study();
        studyWithOnlyPrimarySubject.setId(studyWithOnlyPrimarySubjectId);
        studyWithOnlyPrimarySubject.setName("study with only primary subject");

        // set up a study (222), which will contain both subjects
        Study studyWithBothSubjects = new Study();
        studyWithBothSubjects.setId(studyWithBothSubjectsId);
        studyWithBothSubjects.setName("study with both subjects");

        // set up a study (333), which will contain only the secondary subject, 222
        Study studyWithOnlySecondarySubject = new Study();
        studyWithOnlySecondarySubject.setId(studyWithOnlySecondarySubjectId);
        studyWithOnlySecondarySubject.setName("study with only secondary subject");

        // set up a StudySubject (1111) with only primary subject (111) on study (111)
        StudySubject studySubject1 = new StudySubject();
        studySubject1.setId(studySubject1Id);
        studySubject1.setSubjectMrn(primarySubjectMrn);
        studySubject1.setStudy(studyWithOnlyPrimarySubject);

        // set up a StudySubjects (studyWithBothSubjectsId2, 3333) with both primary and secondary subjects (111, 222) on the same study (222)
        StudySubject studySubject2 = new StudySubject();
        studySubject2.setId(studySubject2Id);
        studySubject2.setSubjectMrn(primarySubjectMrn);
        studySubject2.setStudy(studyWithBothSubjects);
        StudySubject studySubject3 = new StudySubject();
        studySubject3.setId(studySubject3Id);
        studySubject3.setSubjectMrn(secondarySubjectMrn);
        studySubject3.setStudy(studyWithBothSubjects);

        // set up StudySubject (4444) with secondary subject (222) on study (333)
        StudySubject studySubject4 = new StudySubject();
        studySubject4.setId(studySubject4Id);
        studySubject4.setSubjectMrn(secondarySubjectMrn);
        studySubject4.setStudy(studyWithOnlySecondarySubject);

        List<StudySubject> primarySubjectStudies = new ArrayList<>();
        primarySubjectStudies.add(studySubject1);
        primarySubjectStudies.add(studySubject2);

        List<StudySubject> secondarySubjectStudies = new ArrayList<>();
        secondarySubjectStudies.add(studySubject3);
        secondarySubjectStudies.add(studySubject4);

        BookedVisit bookedVisitStudy2Subject1 = new BookedVisit();
        bookedVisitStudy2Subject1.setId(bookedVisitStudy2Subject1Id);
        bookedVisitStudy2Subject1.setName("visit for subject " + primaryMrnString + " on study 2");
        bookedVisitStudy2Subject1.setSubjectMrn(primarySubjectMrn);
        bookedVisitStudy2Subject1.setStudy(studyWithBothSubjects);
        List<BookedVisit> bookedVisitsStudy2Subject1 = new ArrayList<>();
        bookedVisitsStudy2Subject1.add(bookedVisitStudy2Subject1);

        BookedVisit bookedVisitStudy2Subject2 = new BookedVisit();
        bookedVisitStudy2Subject2.setId(bookedVisitStudy2Subject2Id);
        bookedVisitStudy2Subject2.setName("visit for subject " + secondaryMrnString + " on study 2");
        bookedVisitStudy2Subject2.setSubjectMrn(secondarySubjectMrn);
        bookedVisitStudy2Subject2.setStudy(studyWithBothSubjects);
        List<BookedVisit> bookedVisitsStudy2Subject2 = new ArrayList<>();
        bookedVisitsStudy2Subject2.add(bookedVisitStudy2Subject2);

        // check below, do I have the correct study?
        BookedVisit bookedVisitStudy3Subject2 = new BookedVisit();
        bookedVisitStudy3Subject2.setId(bookedVisitStudy3Subject2Id);
        bookedVisitStudy3Subject2.setName("visit for subject " + secondaryMrnString + " on study 3");
        bookedVisitStudy3Subject2.setSubjectMrn(secondarySubjectMrn);
        bookedVisitStudy3Subject2.setStudy(studyWithOnlySecondarySubject);
        List<BookedVisit> bookedVisitsStudy3Subject2 = new ArrayList<>();
        bookedVisitsStudy3Subject2.add(bookedVisitStudy3Subject2);

        when(subjectDAO.findById(primarySubjectId)).thenReturn(primarySubject);
        when(subjectDAO.findById(secondarySubjectId)).thenReturn(secondarySubject);

        when(studyDAO.findStudySubjectBySubjectMrn(primarySubjectMrn)).thenReturn(primarySubjectStudies);
        when(studyDAO.findStudySubjectBySubjectMrn(secondarySubjectMrn)).thenReturn(secondarySubjectStudies);

        when(appointmentDao.getAllBookedVisitByStudyAndSubjectMrn(studyWithBothSubjects, primarySubjectMrn)).thenReturn(bookedVisitsStudy2Subject1);
        when(appointmentDao.getAllBookedVisitByStudyAndSubjectMrn(studyWithBothSubjects, secondarySubjectMrn)).thenReturn(bookedVisitsStudy2Subject2);
        when(appointmentDao.getAllBookedVisitByStudyAndSubjectMrn(studyWithOnlySecondarySubject, secondarySubjectMrn)).thenReturn(bookedVisitsStudy3Subject2);

        // call the method under test here:
        final int rowNumber = 1;

        //
        // METHOD UNDER TEST
        //
        subjectDataMerger.mergeSubject(secondarySubjectId, primarySubjectId, rowNumber);

        // TODO-XH: verify arguments too, not just methods called

        // verify method calls: getting subjects by ID
        verify(subjectDAO, times(1)).findById(primarySubjectId);
        verify(subjectDAO, times(1)).findById(secondarySubjectId);

        // verify method calls: updating bookedVisits
        verify(appointmentDao, times(1)).updateEntity(bookedVisitStudy2Subject2);
        verify(appointmentDao, times(1)).updateEntity(bookedVisitStudy3Subject2);

        // verify method calls: updating StudySubjects
        // CAREFUL! the same methods could be called on any other DAO, because they are implemented by the
        // DAO super class SiteDAO
        verify(studyDAO, times(1)).deleteEntity(studySubject3);
        verify(studyDAO, times(1)).updateEntity(studySubject4);

        // verify method calls: updating secondary subjects (marked as merged)
        verify(subjectDAO, times(1)).updateEntity(secondarySubject);

        // verify method call: markAsMerged() called on secondarySubject
        String changeDetails = "subject with ID " + secondarySubjectId + " and MRN " + secondaryMrnString + " merged into subject with ID " + primarySubjectId +
                               " and MRN " + primaryMrnString + ",BookedVisit#" +
                               bookedVisitStudy2Subject2Id + ".subjectMrn:from#" + secondaryMrnString + ":to#" + primaryMrnString + "," +
                               "StudySubject#" + studySubject3Id + ":DELETED," +
                               "BookedVisit#" + bookedVisitStudy3Subject2Id + ".subjectMrn:from#" + secondaryMrnString + ":to#" +
                                primaryMrnString + ",StudySubject#" +
                               studySubject4Id + ".subject:from#" + secondarySubjectId + ":to#11";
        verify(subjectServiceSpy, times(1)).markArchivalStatus(secondarySubject, SubjectDataMerger.MERGED_BY_SUBJECT_DATA_CLEAN_UP_PROGRAM_SUBJECT_DATA_MERGER, changeDetails, ArchivalStatus.MERGED);

        // can't get this one to work
//        verify(subjectDAO, times(1)).createEntity(activityLogEntry);
        // but this does, and it is a lot easier to write and maintain -- c.f. http://stackoverflow.com/a/7436872/626832
        ArgumentCaptor<ActivityLog> argument = ArgumentCaptor.forClass(ActivityLog.class);
        verify(subjectDAO).createEntity(argument.capture());
        ActivityLog activityLogEntry =  argument.getValue();
        assertNull(activityLogEntry.getPerformingUser());
        assertNull(activityLogEntry.getIpAddress());
        assertNull(activityLogEntry.getAffectedUser());
        assertNull(activityLogEntry.getAffectedResource());
        assertNull(activityLogEntry.getAffectedSublocation());
        assertNull(activityLogEntry.getAffectedStudy());
        assertNull(activityLogEntry.getAffectedVisit());
        assertNull(activityLogEntry.getChangesDetailRequiredField());
        assertNull(activityLogEntry.getAppointmentOverrideReason());
        assertNull(activityLogEntry.getBookedVisit());
        assertNull(activityLogEntry.getId());
        assertEquals(SubjectDataMerger.MERGED_BY_SUBJECT_DATA_CLEAN_UP_PROGRAM_SUBJECT_DATA_MERGER, activityLogEntry.getActionPerformed());
        assertEquals(secondarySubject, activityLogEntry.getAffectedSubject());
        assertEquals("subject with ID " + secondarySubjectId + " and MRN " + secondaryMrnString + " merged into " +
                     "subject with ID " + primarySubjectId + " and MRN " + primaryMrnString + ",BookedVisit#" + bookedVisitStudy2Subject2Id + ".subjectMrn:from#" +
                    secondaryMrnString + ":to#" + primaryMrnString +
                     ",StudySubject#" + studySubject3Id + ":DELETED,BookedVisit#" + bookedVisitStudy3Subject2Id + ".subjectMrn:from#" + secondaryMrnString +
                     ":to#" + primaryMrnString + ",StudySubject#" +
                     studySubject4Id + ".subject:from#" + secondarySubjectId + ":to#" + primarySubjectId,
                     activityLogEntry.getChangesDetail());

        // verify final object attributes

        // secondary StudySubject's which overlap a study in one of the primary StudySubject's get deleted
        verify(studyDAO, times(1)).deleteEntity(studySubject3);

        // The other secondary subjects get reassigned to the primary subject
        assertEquals(studySubject4.getSubjectMrn(), primarySubjectMrn);

        // the primary StudySubject's don't get re-assigned
        assertEquals(studySubject1.getSubjectMrn(), primarySubjectMrn);
        assertEquals(studySubject2.getSubjectMrn(), primarySubjectMrn);

        // subject0002 (secondary subject) gets deleted, and points to primary subject subject0001
        assertEquals("Secondary subject should now be archived as MERGED", ArchivalStatus.MERGED, secondarySubject.getArchivalStatus());

        assertEquals(bookedVisitStudy2Subject1.getStudy(), studyWithBothSubjects);
        assertEquals(bookedVisitStudy2Subject1.getSubjectMrn(), primarySubjectMrn);

        assertEquals(bookedVisitStudy2Subject2.getStudy(), studyWithBothSubjects);
        assertEquals(bookedVisitStudy2Subject2.getSubjectMrn(), primarySubjectMrn);

        assertEquals(bookedVisitStudy3Subject2.getStudy(), studyWithOnlySecondarySubject);
        assertEquals(bookedVisitStudy3Subject2.getSubjectMrn(), primarySubjectMrn);

    }

}
