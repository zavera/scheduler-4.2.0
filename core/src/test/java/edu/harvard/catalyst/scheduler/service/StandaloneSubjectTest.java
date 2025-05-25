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

import edu.harvard.catalyst.scheduler.core.SchedulerRuntimeException;
import edu.harvard.catalyst.scheduler.dto.response.MrnInfoDTO;
import edu.harvard.catalyst.scheduler.dto.response.SubjectDetailResponse;
import edu.harvard.catalyst.scheduler.entity.ActivityLog;
import edu.harvard.catalyst.scheduler.entity.ArchivalStatus;
import edu.harvard.catalyst.scheduler.entity.Institution;
import edu.harvard.catalyst.scheduler.entity.Subject;
import edu.harvard.catalyst.scheduler.entity.SubjectMrn;
import edu.harvard.catalyst.scheduler.entity.User;
import edu.harvard.catalyst.scheduler.persistence.SubjectDAO;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.List;

@RunWith(MockitoJUnitRunner.class)
public final class StandaloneSubjectTest {
    @Mock private User mockUser;
    @Mock private Institution mockInstitution;
    @Mock private SubjectDAO mockSubjectDAO ;
    @Mock private SubjectMrn mockSubjectMrn;
    private StandaloneSubjectService standaloneSubjectService;

    @Before
    public void setUp() {
        standaloneSubjectService = new StandaloneSubjectService(mockSubjectDAO);
    }

    @Test
    public void testGetSubjectData() {
        Mockito.when(mockUser.getInstitution()).thenReturn(mockInstitution);
        Mockito.when(mockInstitution.getName()).thenReturn("name");

        standaloneSubjectService.getSubjectData(null, 1, "host", mockUser);
        Mockito.verify(mockSubjectDAO).getSubjectDataById(1);
    }

    @Test
    public void testGetSearchedSubjectsWithOneGenderFilter() {
        List<String> genderIdList = new ArrayList<>();
        genderIdList.add("1");
        standaloneSubjectService.getSearchedSubjects("ln", "fn", "mrn", "bd", genderIdList);
        Mockito.verify(mockSubjectDAO).getSearchSubjects("ln", "fn", "mrn", "bd", genderIdList);
    }

    @Test
    public void testGetSearchedSubjectsWithMultipleGenderFilter() {
        List<String> genderIdList = new ArrayList<>();
        genderIdList.add("1");
        genderIdList.add("2");
        genderIdList.add("4");

        standaloneSubjectService.getSearchedSubjects("ln", "fn", "mrn", "bd", genderIdList);
        Mockito.verify(mockSubjectDAO).getSearchSubjects("ln", "fn", "mrn", "bd", genderIdList);
    }

    @Test (expected = SchedulerRuntimeException.class)
    public void testRefreshExternalSubject() {
        standaloneSubjectService.refreshExternalSubject(mockSubjectMrn, false);
    }

    @Test
    public void testGetSearchedSubjectsWithNoGenderFilter() {
        List<String> genderIdList = new ArrayList<>();
        standaloneSubjectService.getSearchedSubjects("ln", "fn", "mrn", "bd", genderIdList);
        Mockito.verify(mockSubjectDAO, Mockito.times(1)).getSearchSubjects("ln", "fn", "mrn", "bd", genderIdList);
    }

    @Test
    public void testMarkArchivalStatus() {
        final Subject subject = new Subject();
        final String actionPerformed = "action!";
        final String changeDetail = "change!";
        final ArchivalStatus archivalStatus = ArchivalStatus.MERGED;
        ArgumentCaptor<Subject> argumentSubject = ArgumentCaptor.forClass(Subject.class);
        ArgumentCaptor<ActivityLog> argumentAL = ArgumentCaptor.forClass(ActivityLog.class);

        standaloneSubjectService.markArchivalStatus(subject, actionPerformed, changeDetail, archivalStatus);

        Mockito.verify(mockSubjectDAO).createEntity(argumentAL.capture());
        Mockito.verify(mockSubjectDAO).updateEntity(argumentSubject.capture());

        Assert.assertNotNull(argumentSubject.getValue());
        Assert.assertTrue(argumentSubject.getValue() == subject);
        Assert.assertEquals(argumentSubject.getValue().getArchivalStatus(), archivalStatus);
        Assert.assertNotNull(argumentAL.getValue());
        Assert.assertEquals(argumentAL.getValue().getChangesDetail(), changeDetail);
        Assert.assertEquals(argumentAL.getValue().getActionPerformed(), actionPerformed);
        Assert.assertTrue(argumentAL.getValue().getAffectedSubject() == subject);
        Assert.assertEquals(argumentAL.getValue().getAffectedSubject().getArchivalStatus(), archivalStatus);
    }

    @Test
    public void testFindInternalSubjectByMrn() {
        final List<MrnInfoDTO> list = new ArrayList<>();
        final SubjectDetailResponse response = new SubjectDetailResponse();
        Mockito.when(mockSubjectDAO.findInternalSubjectByMrn(list)).thenReturn(response);

        final SubjectDetailResponse result = this.mockSubjectDAO.findInternalSubjectByMrn(list);

        Assert.assertNotNull(result);
        Assert.assertTrue(response == result);
        Mockito.verify(mockSubjectDAO).findInternalSubjectByMrn(list);
    }

    @Test
    public void testGetExternalSubjectDataUsingMrn() {
        final SubjectDetailResponse result = this.standaloneSubjectService.getExternalSubjectDataUsingMrn(null);

        Assert.assertNotNull(result);
        Assert.assertNull(result.getComment());
        Assert.assertNull(result.getId());
        Assert.assertNull(result.getEmpiFailoverMessage());
        Assert.assertNull(result.getEthnicityName());
        Assert.assertNull(result.getFirstName());
        Assert.assertNull(result.getGenderName());
        Assert.assertNull(result.getLastName());
        Assert.assertNull(result.getPrimaryContactNumber());
        Assert.assertNull(result.getSecondaryContactNumber());
        Assert.assertTrue(result.getStateName().isEmpty());
        Assert.assertTrue(result.getMrnInfoList().isEmpty());
    }
}
