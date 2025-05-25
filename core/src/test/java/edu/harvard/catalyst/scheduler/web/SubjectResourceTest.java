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
package edu.harvard.catalyst.scheduler.web;

import edu.harvard.catalyst.scheduler.util.TestUtils;
import edu.harvard.catalyst.scheduler.dto.SubjectsDTO;
import edu.harvard.catalyst.scheduler.entity.Subject;
import edu.harvard.catalyst.scheduler.entity.User;
import edu.harvard.catalyst.scheduler.security.SchedulerSession;
import edu.harvard.catalyst.scheduler.security.SchedulerUserDetails;
import edu.harvard.catalyst.scheduler.service.SubjectService;
import edu.harvard.catalyst.scheduler.service.SubjectServiceClassic;
import edu.harvard.catalyst.scheduler.util.DateUtility;
import edu.harvard.catalyst.scheduler.util.ServiceTestsDAOUtility;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Date;

import static edu.harvard.catalyst.scheduler.util.DateUtility.parse;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.refEq;
import static org.mockito.Mockito.*;

/**
 * Created by xavier on 5/11/16.
 */
@RunWith(MockitoJUnitRunner.class)
public class SubjectResourceTest {

    public static final String SAMPLE_USER_AGENT_REQUEST_HEADER = "Chrome Mozilla etc";
    public static final String SAMPLE_BIRTHDAY = "Wed Jan 01 1950 00:00:00 PDT-0000 (Pacific Daylight Time)";

    SubjectResource subjectResource;

    private SubjectService mockSubjectService = mock(SubjectService.class);
    private SubjectServiceClassic mockSubjectServiceClassic = mock(SubjectServiceClassic.class);
//    private SubjectServiceClassic mockSubjectServiceClassic = mock(SubjectServiceClassic.class, withSettings().invocationListeners(new VerboseMockInvocationLogger()));
//	private StudyService mockStudyService = mock(StudyService.class, withSettings().invocationListeners(new VerboseMockInvocationLogger()));

    private final SchedulerSession session = mock(SchedulerSession.class);
    private final HttpServletRequest request = mock(HttpServletRequest.class);
    private final SchedulerUserDetails schedulerUserDetails = mock(SchedulerUserDetails.class);
    private final User mockUserInSession = ServiceTestsDAOUtility.createMockUser();
    private final String mockHostnameInRequest = "mockHostnameInRequest";
    private final ServletContext mockContext = mock(ServletContext.class);

    @Before
    public void setUp() {

        subjectResource = new SubjectResource(mockSubjectServiceClassic, mockSubjectService, "default");
        subjectResource.setSession(session);
        subjectResource.setRequest(request);

        when(session.getUserDetails()).thenReturn(schedulerUserDetails);
        when(schedulerUserDetails.getUser()).thenReturn(mockUserInSession);
        when(request.getRemoteHost()).thenReturn(mockHostnameInRequest);

    }

    public SubjectsDTO createSampeSubjectsDto(int prefix) {

        SubjectsDTO dto = new SubjectsDTO();
        dto.setId(0); // not saved
        dto.setMrn(prefix + "a");
        dto.setFirstName(prefix + "b");
        dto.setMiddleName(prefix + "c");
        dto.setLastName("d");
        dto.setFullName("e");
        dto.setNameSuffix("jr");
        dto.setEthnicity(1);
        dto.setRace(2);
        dto.setGender(3);
        dto.setStreetAddress1("f");
        dto.setStreetAddress2("g");
        dto.setCountry(4);
        dto.setCity("h");
        dto.setState(5);
        dto.setZip("i");
        dto.setPrimaryContactNumber("j");
        dto.setSecondaryContactNumber("k");
        dto.setCreatedDate(new Date());
        dto.setSecure(true);
        dto.setActive(true);
        dto.setUserId(6);
        dto.setSubjectToDeactivateId(0);
        dto.setSubjectToActivateId(0);
        dto.setSubjectToEditId(0);
        dto.setSearchSubjects(new ArrayList<Subject>());
        // follows SimpleDateFormat("EEE MMM dd yyyy HH:mm:ss zZ (zzzz)");
        dto.setBirthday(SAMPLE_BIRTHDAY);
        dto.setComment("l");
        dto.setStudyId(0);

        return dto;

    }

    /**
     * The objective of this test is to make sure that the no-args constructor needed by cglib proxying is implemented.
     */
    @Test
    public void testNoArgumentResourceConstructorExists() {

        SubjectResource subjectResource = new SubjectResource();

    }

    @Test
    public void testCreateSubject() {

        SubjectsDTO incomingDto = createSampeSubjectsDto(1);
        String incomingString = subjectResource.gson.toJson(incomingDto);

        // the trick here is to use refEq() for matching the DTO argument with the reference DTO.
        // Not that 'ref' in refEq() means _reflection_, not reference.
        // Note: ignore comparing fundingSourceInfoList because refEq does a shallow comparison
        doNothing().when(mockSubjectServiceClassic).createSubjectForDto(refEq(incomingDto, "fundingSourceInfoList"), eq(mockUserInSession), eq(mockHostnameInRequest));

        String resultString = subjectResource.createSubject(incomingString, SAMPLE_USER_AGENT_REQUEST_HEADER);

        // need to doctor the DTO a little bit to reflect what goes on in the resource method
        incomingDto.setBirthdate(parse(DateUtility.subjectDate(), SAMPLE_BIRTHDAY));
        TestUtils.assertEqualJson(subjectResource.gson, incomingDto, resultString);

    }

    @Test
    public void testUpdateSubject() {

        SubjectsDTO incomingDto = createSampeSubjectsDto(1);
        String incomingString = subjectResource.gson.toJson(incomingDto);

        // the trick here is to use refEq() for matching the DTO argument with the reference DTO.
        // Not that 'ref' in refEq() means _reflection_, not reference.
        // Note: ignore comparing fundingSourceInfoList because refEq does a shallow comparison

        // mimmic the transformation that occurs in the resource call
        Date birthdate = subjectResource.getFormattedBirthDate(incomingDto.getBirthday());
        incomingDto.setBirthdate(birthdate);

        when(mockSubjectServiceClassic.updateSubject(refEq(incomingDto, "fundingSourceInfoList"), eq(mockUserInSession), eq(mockHostnameInRequest), eq(true))).thenReturn(incomingDto);

        SubjectsDTO hmm = mockSubjectServiceClassic.updateSubject(incomingDto, mockUserInSession, mockHostnameInRequest, true);

        String resultString = subjectResource.updateSubject(incomingString, SAMPLE_USER_AGENT_REQUEST_HEADER);

        // need to doctor the DTO a little bit to reflect what goes on in the resource method
        incomingDto.setBirthdate(parse(DateUtility.subjectDate(), SAMPLE_BIRTHDAY));
        TestUtils.assertEqualJson(subjectResource.gson, incomingDto, resultString);

    }


}
