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
public class SubjectDataDeleterTest {

    @Test
    public void testDeleteSubject() {

        int subjectId = 1;
        String subjectMrn = "0001";

        SubjectDAO subjectDAO = mock(SubjectDAO.class);
        StandaloneSubjectService subjectService = new StandaloneSubjectService(subjectDAO);
        StandaloneSubjectService subjectServiceSpy = spy(subjectService);

        SubjectDataDeleter subjectDataDeleter = new SubjectDataDeleter(null, subjectDAO, subjectServiceSpy);
        subjectDataDeleter.decrypt = false;

        Subject subject1 = new Subject();
        subject1.setId(subjectId);

        SubjectMrn mrn1 = new SubjectMrn();
        mrn1.setMrn(subjectMrn);
        subject1.getSubjectMrnSet().add(mrn1);

        when(subjectDAO.findById(Subject.class, subjectId)).thenReturn(subject1);

        subjectDataDeleter.deleteSubject(subjectId, 1);

        // verify method calls
        verify(subjectDAO, times(1)).updateEntity(subject1);

        String changeDetails = "Subject ID: " + subjectId + " (MRN: " + subjectMrn + ") archived (DELETED)";
        verify(subjectServiceSpy, times(1)).markArchivalStatus(subject1,
                                                          SubjectDataDeleter.DELETED_BY_SUBJECT_DATA_CLEAN_UP_PROGRAM_SUBJECT_DATA_DELETER,
                                                          "Subject ID: " + subjectId + " (MRN: " + subjectMrn + ") archived (DELETED)",
                                                            ArchivalStatus.DELETED);
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
        assertEquals(SubjectDataDeleter.DELETED_BY_SUBJECT_DATA_CLEAN_UP_PROGRAM_SUBJECT_DATA_DELETER, activityLogEntry.getActionPerformed());
        assertEquals(subject1, activityLogEntry.getAffectedSubject());
        assertEquals(changeDetails, activityLogEntry.getChangesDetail());

        // verify final object attributes

        assertEquals(ArchivalStatus.DELETED, subject1.getArchivalStatus());

    }

}
