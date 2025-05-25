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

import edu.harvard.catalyst.scheduler.AbstractSpringWiredJunit4Test;
import edu.harvard.catalyst.scheduler.core.SchedulerRuntimeException;
import edu.harvard.catalyst.scheduler.dto.ExternalSubjectQueryBuilder;
import edu.harvard.catalyst.scheduler.dto.response.MrnInfoDTO;
import edu.harvard.catalyst.scheduler.dto.response.SubjectDetailResponse;
import edu.harvard.catalyst.scheduler.dto.response.SubjectsResponseDTO;
import edu.harvard.catalyst.scheduler.entity.*;
import edu.harvard.catalyst.scheduler.persistence.EpicSubjectDAO;
import edu.harvard.catalyst.scheduler.util.MiscUtil;
import edu.harvard.catalyst.scheduler.util.SubjectDataEncryptor;
import org.apache.log4j.Appender;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.spi.LoggingEvent;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.io.IOException;
import java.security.Key;
import java.util.ArrayList;
import java.util.List;

import static edu.harvard.catalyst.scheduler.core.Statics.*;
import static edu.harvard.catalyst.scheduler.util.TestUtils.verifyLog;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public final class EpicSubjectServiceTest extends AbstractSpringWiredJunit4Test {

    private Appender mockAppender;
    private ArgumentCaptor<LoggingEvent> captorLoggingEvent;

    private User user;
    private Institution institution;

    private EpicSubjectDAO epicSubjectDAO;
    private EpicSubjectService epicSubjectService;
    // We will need to mock some methods of epicSubjectService. but not all.
    // so we need to create a spy
    private EpicSubjectService epicSubjectServiceSpy;
    private StandaloneSubjectService standaloneSubjectService;

    @Autowired
    @Qualifier("encryptionKey")
    Key encryptionKey;

    @Before
    public void setup() {
        mockAppender = mock(Appender.class);
        captorLoggingEvent = ArgumentCaptor.forClass(LoggingEvent.class);

        Logger root = Logger.getRootLogger();
        root.addAppender(mockAppender);
        root.setLevel(Level.INFO);

        user = mock(User.class);
        institution = mock(Institution.class);

        standaloneSubjectService = mock(StandaloneSubjectService.class);

        epicSubjectDAO = mock(EpicSubjectDAO.class);
        epicSubjectService = new EpicSubjectService(epicSubjectDAO, standaloneSubjectService);

        epicSubjectServiceSpy = spy(epicSubjectService);

        SubjectDataEncryptor.setEncryptionKey(encryptionKey);

    }

    @After
    public void teardown() {
        LogManager.getRootLogger().removeAppender(mockAppender);
    }

    @Test
    public void testGetSubjectData() {
        when(user.getInstitution()).thenReturn(institution);
        when(institution.getName()).thenReturn("name");

        epicSubjectService.getSubjectData(true, 1, "host", user);
        verify(epicSubjectDAO, times(1)).findSubjectByUid(1);
    }

    @Test
    public void testGetSearchedSubjectsWithGenderFilter() {
        List<String> genderIdList = new ArrayList();
        genderIdList.add("F");
        epicSubjectService.getSearchedSubjects("ln", "fn", "mrn", "bd", genderIdList);
        verify(epicSubjectDAO, times(1)).getSearchedSubjects("ln", "fn", "mrn", "bd", "F");
    }

    @Test
    public void testGetSearchedSubjectsWithNoGenderFilter() {
        List<String> genderIdList = new ArrayList();
        epicSubjectService.getSearchedSubjects("ln", "fn", "mrn", "bd", genderIdList);
        verify(epicSubjectDAO, times(1)).getSearchedSubjects("ln", "fn", "mrn", "bd", null);
    }

    @Test
    public void testGetSearchedSubjectsWithUndisclosedGenderFilter() {
        List<String> genderIdList = new ArrayList();
        genderIdList.add("U");
        epicSubjectService.getSearchedSubjects("ln", "fn", "mrn", "bd", genderIdList);
        verify(epicSubjectDAO, times(1)).getSearchedSubjects("ln", "fn", "mrn", "bd", "U");
    }


    @Test
    public void testGetExternalSubjectDataUsingMrnFailover() {
        SubjectDetailResponse subjectDetailResponse = mock(SubjectDetailResponse.class);

        when(epicSubjectDAO.getSubjectDetailsUsingMrnAndSite(any(), any()))
                .thenThrow(new SchedulerRuntimeException());
        when(standaloneSubjectService.findInternalSubjectByMrn(any()))
                .thenReturn(subjectDetailResponse);

        epicSubjectServiceSpy.getExternalSubjectDataUsingMrn(new MrnInfoDTO("", "", ""));

        verify(subjectDetailResponse, times(1)).setEmpiFailoverMessage(
                MiscUtil.applyCodeToXX(STR_EMPI_FAILS + STR_BUT_SCHED_WORKS, CODE_EMPI_FAILS_BUT_SCHED_WORKS));
    }

    @Test
    public void testGetSearchedSubjects() {
        SubjectsResponseDTO subjectsResponseDTO = mock(SubjectsResponseDTO.class);

        when(epicSubjectDAO.getSearchedSubjects(any(), any(), any(), any(), any()))
                .thenThrow(new SchedulerRuntimeException());
        when(standaloneSubjectService.getSearchedSubjects(any(), any(), any(), any(), any()))
                .thenReturn(subjectsResponseDTO);
        when(subjectsResponseDTO.getTotalCount()).thenReturn(0L);

        epicSubjectServiceSpy.getSearchedSubjects("", "", "", "", null);

        verify(subjectsResponseDTO, times(1)).setEmpiFailoverMessage(
                MiscUtil.applyCodeToXX(STR_EMPI_FAILS + STR_AND_SCHED_FAILS_TOO, CODE_EMPI_FAILS_AND_SCHED_FAILS));


        when(subjectsResponseDTO.getTotalCount()).thenReturn(1L);

        epicSubjectServiceSpy.getSearchedSubjects("", "", "", "", null);

        verify(subjectsResponseDTO, times(1)).setEmpiFailoverMessage(
                MiscUtil.applyCodeToXX(STR_EMPI_FAILS + STR_BUT_SCHED_WORKS, CODE_EMPI_FAILS_BUT_SCHED_WORKS));
    }

    @Test
    public void testGetSubjectDataFailoverPuidOk() {
        SubjectDetailResponse subjectDetailResponse = new SubjectDetailResponse();
        Subject subject = new Subject();
        subject.setId(1);

        when(epicSubjectDAO.findSubjectByUid(any())).thenReturn(subjectDetailResponse);

        epicSubjectServiceSpy.getSubjectData(true, 1, "", null);

        verifyLog(STR_EMPI_FAILS, 0, mockAppender, captorLoggingEvent);
    }

    @Test
    public void testGetSubjectDataFailoverPuidExcNoSched() {
        Subject subject = new Subject();
        subject.setId(1);

        when(epicSubjectDAO.findSubjectByUid(any())).thenThrow(new SchedulerRuntimeException());
        when(epicSubjectDAO.findInternalSubjectByPuid(any())).thenReturn(null);

        epicSubjectServiceSpy.getSubjectData(true, 1, "", null);

        verifyLog("Subject Search, Expand (+) subject info. " +
                MiscUtil.applyCodeToXX(STR_EMPI_FAILS +
                        STR_AND_SCHED_FAILS_TOO, CODE_EMPI_FAILS_AND_SCHED_FAILS),
                1, mockAppender, captorLoggingEvent);
    }

    @Test
    public void testGetExternalSubjectDataUsingMrn()
    {
        MrnInfoDTO mrnInfoDTO = new MrnInfoDTO("1234", "INST", "");
        epicSubjectService.getExternalSubjectDataUsingMrn(mrnInfoDTO);
        verify(epicSubjectDAO, times(1)).getSubjectDetailsUsingMrnAndSite(mrnInfoDTO.getValue(), mrnInfoDTO.getInstitution());

        mrnInfoDTO = null;
        SubjectDetailResponse response = epicSubjectService.getExternalSubjectDataUsingMrn(mrnInfoDTO);
        verify(epicSubjectDAO, times(0)).getSubjectDetailsUsingMrnAndSite(null, null);
        assertNull("Empty subject response", response.getId());
    }

    @Test
    public void testGetExternalSubjectDataUsingMrnBadSslOkSched() throws Exception
    {
        MrnInfoDTO mrnInfoDTO = new MrnInfoDTO("1234", "INST", "");

        SubjectDetailResponse subjectDetailResponse = mock(SubjectDetailResponse.class);
        when(subjectDetailResponse.getId()).thenReturn(1);

        when(epicSubjectDAO.getSubjectDetailsUsingMrnAndSite(mrnInfoDTO.getValue(), mrnInfoDTO.getInstitution()))
                .thenThrow(new SchedulerRuntimeException(new IOException()));
        when(standaloneSubjectService.findInternalSubjectByMrn(any())).thenReturn(subjectDetailResponse);

        epicSubjectService.getExternalSubjectDataUsingMrn(mrnInfoDTO);
        verify(epicSubjectDAO, times(1)).getSubjectDetailsUsingMrnAndSite(mrnInfoDTO.getValue(), mrnInfoDTO.getInstitution());

        String expected = MiscUtil.applyCodeToXX(STR_SSL_FAILS + STR_BUT_SCHED_WORKS, CODE_SSL_FAILS_BUT_SCHED_WORKS);
        verify(subjectDetailResponse, times(1)).setEmpiFailoverMessage(expected);
    }

    @Test
    public void testGetExternalSubjectDataUsingMrnBadEmpiOkSched() throws Exception
    {
        MrnInfoDTO mrnInfoDTO = new MrnInfoDTO("1234", "INST", "");

        SubjectDetailResponse subjectDetailResponse = mock(SubjectDetailResponse.class);
        when(subjectDetailResponse.getId()).thenReturn(1);

        when(epicSubjectDAO.getSubjectDetailsUsingMrnAndSite(mrnInfoDTO.getValue(), mrnInfoDTO.getInstitution()))
                .thenThrow(new SchedulerRuntimeException(new NullPointerException()));
        when(standaloneSubjectService.findInternalSubjectByMrn(any())).thenReturn(subjectDetailResponse);

        epicSubjectService.getExternalSubjectDataUsingMrn(mrnInfoDTO);
        verify(epicSubjectDAO, times(1)).getSubjectDetailsUsingMrnAndSite(mrnInfoDTO.getValue(), mrnInfoDTO.getInstitution());

        String expected = MiscUtil.applyCodeToXX(STR_EMPI_FAILS + STR_BUT_SCHED_WORKS, CODE_EMPI_FAILS_BUT_SCHED_WORKS);
        verify(subjectDetailResponse, times(1)).setEmpiFailoverMessage(expected);
    }

    @Test
    public void testGetExternalSubjectDataUsingMrnBadSslBadSched() throws Exception
    {
        MrnInfoDTO mrnInfoDTO = new MrnInfoDTO("1234", "INST", "");

        SubjectDetailResponse subjectDetailResponse = mock(SubjectDetailResponse.class);
        when(subjectDetailResponse.getId()).thenReturn(null);

        when(epicSubjectDAO.getSubjectDetailsUsingMrnAndSite(mrnInfoDTO.getValue(), mrnInfoDTO.getInstitution()))
                .thenThrow(new SchedulerRuntimeException(new IOException()));
        when(standaloneSubjectService.findInternalSubjectByMrn(any())).thenReturn(subjectDetailResponse);

        epicSubjectService.getExternalSubjectDataUsingMrn(mrnInfoDTO);
        verify(epicSubjectDAO, times(1)).getSubjectDetailsUsingMrnAndSite(mrnInfoDTO.getValue(), mrnInfoDTO.getInstitution());

        String expected = MiscUtil.applyCodeToXX(STR_SSL_FAILS + STR_AND_SCHED_FAILS_TOO, CODE_SSL_FAILS_AND_SCHED_FAILS);
        verify(subjectDetailResponse, times(1)).setEmpiFailoverMessage(expected);
    }

    @Test
    public void testGetExternalSubjectDataUsingMrnBadEmpiBadSched() throws Exception
    {
        MrnInfoDTO mrnInfoDTO = new MrnInfoDTO("1234", "INST", "");

        SubjectDetailResponse subjectDetailResponse = mock(SubjectDetailResponse.class);
        when(subjectDetailResponse.getId()).thenReturn(null);

        when(epicSubjectDAO.getSubjectDetailsUsingMrnAndSite(mrnInfoDTO.getValue(), mrnInfoDTO.getInstitution()))
                .thenThrow(new SchedulerRuntimeException(new NullPointerException()));
        when(standaloneSubjectService.findInternalSubjectByMrn(any())).thenReturn(subjectDetailResponse);

        epicSubjectService.getExternalSubjectDataUsingMrn(mrnInfoDTO);
        verify(epicSubjectDAO, times(1)).getSubjectDetailsUsingMrnAndSite(mrnInfoDTO.getValue(), mrnInfoDTO.getInstitution());

        String expected = MiscUtil.applyCodeToXX(STR_EMPI_FAILS + STR_AND_SCHED_FAILS_TOO, CODE_EMPI_FAILS_AND_SCHED_FAILS);
        verify(subjectDetailResponse, times(1)).setEmpiFailoverMessage(expected);
    }

    @Test
    public void testFindInternalSubjectByMrn()
    {
        List<MrnInfoDTO> mrnInfoDTOList = new ArrayList<>();
        MrnInfoDTO mrnInfoDTO = new MrnInfoDTO("1234", "INST", "");
        mrnInfoDTOList.add(mrnInfoDTO);
        epicSubjectService.findInternalSubjectByMrn(mrnInfoDTOList);
        verify(epicSubjectDAO, times(1)).findInternalSubjectByMrn(mrnInfoDTOList);
    }

    @Test
    public void testGetSubjectDataFailoverPuidExcYesSched() {
        Subject subject = new Subject();
        subject.setId(1);

        when(epicSubjectDAO.findSubjectByUid(any())).thenThrow(new SchedulerRuntimeException());
        when(epicSubjectDAO.findInternalSubjectByPuid(any())).thenReturn(subject);

        epicSubjectServiceSpy.getSubjectData(true, 1, "", null);

        verifyLog("Subject Search, Expand (+) subject info. " +
                        MiscUtil.applyCodeToXX(STR_EMPI_FAILS +
                                STR_BUT_SCHED_WORKS, CODE_EMPI_FAILS_BUT_SCHED_WORKS),
                1, mockAppender, captorLoggingEvent);
    }

    @Test
    public void testGetSubjectDataFailoverNoPuidExc() {
        Subject subject = new Subject();
        subject.setId(1);

        when(epicSubjectDAO.findBySubjectId(1)).thenThrow(new SchedulerRuntimeException());

        epicSubjectServiceSpy.getSubjectData(false, 1, "", null);

        String expected = "Subject Search, Expand (+) subject info. " +
                "At this time subject details cannot be displayed or updated, " +
                "and subject cannot be added to a study. " +
                "Please try again later (error XX). -- SchedulerRuntimeException -- ";
        verifyLog(expected, 1, mockAppender, captorLoggingEvent);
    }

    @Test
    public void testGetSubjectDataFailoverNoPuidOk() {
        Subject subject = new Subject();
        subject.setId(1);

        when(epicSubjectDAO.findBySubjectId(1)).thenReturn(subject);

        epicSubjectServiceSpy.getSubjectData(false, 1, "", null);

        verifyLog("Subject Search, Expand (+) subject info. " +
                        MiscUtil.applyCodeToXX(STR_EMPI_FAILS +
                                STR_BUT_SCHED_WORKS, CODE_EMPI_FAILS_BUT_SCHED_WORKS),
                1, mockAppender, captorLoggingEvent);
    }

    @Test
    public void testLookupEmpiEthnicityString() {
        String ethnicityStr = "ethnicity";
        epicSubjectService.lookupEmpiEthnicityString(ethnicityStr);
        verify(epicSubjectDAO, times(1)).lookupEmpiEthnicityString(ethnicityStr);
    }

    @Test
    public void testGetSubjectsAsEmpiSubjectDto(){
        final String subjectLastName = "Last";
        final String subjectFirstName = "First";
        final String subjectMrn = "1000";
        final String subjectBirthDate = "dob";
        final String subjectGenderCode = "M";
        String institution = "inst";

        ExternalSubjectQueryBuilder externalSubjectQueryBuilder = new ExternalSubjectQueryBuilder()
                .lastName(subjectLastName)
                .firstName(subjectFirstName)
                .birthdate(subjectBirthDate)
                .genderCode(subjectGenderCode)
                .mrn(subjectMrn)
                .mrnSite(institution);
        epicSubjectService.getSubjectsAsEmpiSubjectDto(externalSubjectQueryBuilder, true);
        verify(epicSubjectDAO, times(1)).getSearchedSubjectsAsEmpiSubjectDto(externalSubjectQueryBuilder, true);
    }
}