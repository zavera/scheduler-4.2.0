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

import edu.harvard.catalyst.scheduler.dto.response.MrnInfoDTO;
import edu.harvard.catalyst.scheduler.entity.Institution;
import edu.harvard.catalyst.scheduler.entity.User;
import edu.harvard.catalyst.scheduler.security.SchedulerSession;
import edu.harvard.catalyst.scheduler.security.SchedulerUserDetails;
import edu.harvard.catalyst.scheduler.service.StandaloneSubjectService;
import edu.harvard.catalyst.scheduler.service.EpicSubjectService;
import edu.harvard.catalyst.scheduler.service.SubjectService;
import edu.harvard.catalyst.scheduler.service.SubjectServiceClassic;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import javax.servlet.http.HttpServletRequest;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.*;

/**
 *
 * @author Ankit Panchamia
 * @date 03/24/2014
 *
 */

@RunWith(MockitoJUnitRunner.class)
public class ExternalSubjectResourceTest {

    private final SubjectServiceClassic subjectServiceClassic = mock(SubjectServiceClassic.class);
    private final SubjectService genericSubjectService = mock(SubjectService.class);
    private final SubjectResource genericSubjectResource = new SubjectResource(subjectServiceClassic, genericSubjectService, null);

    private final SubjectService defaultSubjectService = mock(StandaloneSubjectService.class);
    private final SubjectResource defaultSubjectResource = new SubjectResource(subjectServiceClassic, defaultSubjectService,  "default");

    private final SubjectService epicSubjectService = mock(EpicSubjectService.class);
    private final SubjectResource epicSubjectResource = new SubjectResource(subjectServiceClassic, epicSubjectService, "epic");

    private final SchedulerSession session = mock(SchedulerSession.class);
    private final HttpServletRequest request = mock(HttpServletRequest.class);
    private final SchedulerUserDetails schedulerUserDetails = mock(SchedulerUserDetails.class);
    private final User user = mock(User.class);
    private final Institution institution = mock(Institution.class);
    private String institutionName = "institution";

    private final String mrn = "mrn";
    private final String host = "host";

    public enum MrnStatus {
        ACTIVE("A"),
        MERGED("M");
        private String code;
        MrnStatus(String code)
        {
            this.code = code;
        }

        public String getCode()
        {
            return code;
        }
    };

    @Before
    public void setup() {
        when(session.getUserDetails()).thenReturn(schedulerUserDetails);
        when(request.getRemoteHost()).thenReturn(host);
        when(schedulerUserDetails.getUser()).thenReturn(user);
        when(user.getInstitution()).thenReturn(institution);
        when(institution.getName()).thenReturn(institutionName);
    }

    @Test
    // TODO-XH : Just a very simplistic place-holder stub for now. It just checks that things are wired, correctly or not.
    // I need to learn how to write tests like the others in this class
    //
    // What about using scheduler/it/src/test/java/edu/harvard/catalyst/scheduler/persistence/AbstractDbIntegrationTest.java for integration testing?
    //
    public void testGetSearchSubjectsNoGenderFilter() throws Exception {
        final String bogusFirstName = "asdf";
        final String bogusLastName = "first";

        final String bogusMrn = "1234";
        final String bogusSearchData = "{\"lastName\":\"" + bogusLastName
                + "\",\"firstName\":\"" + bogusFirstName +  "\",\"mrn\":\"" + bogusMrn + "\",\"birthday\":\"\",\"genderList\":[]}";

        genericSubjectResource.setSession(session);
        genericSubjectResource.setRequest(request);
        genericSubjectResource.getSearchSubjects(bogusSearchData);
        verify(genericSubjectService, times(1)).getSearchedSubjects(bogusLastName, bogusFirstName, bogusMrn, "", new ArrayList<String>());

        defaultSubjectResource.setSession(session);
        defaultSubjectResource.setRequest(request);
        defaultSubjectResource.getSearchSubjects(bogusSearchData);
        verify(defaultSubjectService, times(1)).getSearchedSubjects(bogusLastName, bogusFirstName, bogusMrn, "", new ArrayList<String>());

        epicSubjectResource.setSession(session);
        epicSubjectResource.setRequest(request);
        epicSubjectResource.getSearchSubjects(bogusSearchData);
        verify(epicSubjectService, times(1)).getSearchedSubjects(bogusLastName, bogusFirstName, bogusMrn, "", new ArrayList<String>());
    }

    @Test
    // TODO-XH : Just a very simplistic place-holder stub for now. It just checks that things are wired, correctly or not.
    // I need to learn how to write tests like the others in this class
    //
    // What about using scheduler/it/src/test/java/edu/harvard/catalyst/scheduler/persistence/AbstractDbIntegrationTest.java for integration testing?
    //
    public void testGetSearchSubjectsHasGenderFilter() throws Exception {
        final String bogusLastName = "asdf";
        final String bogusFirstName = "first";

        final String bogusMrn = "1234";
        final String bogusSearchData = "{\"lastName\":\"" + bogusLastName
        + "\",\"firstName\":\"" + bogusFirstName + "\",\"mrn\":\"" + bogusMrn + "\",\"birthday\":\"\",\"genderList\":[1,2,3]}";

        List<String> genderList = new ArrayList<String>();
        genderList.add("1");
        genderList.add("2");
        genderList.add("3");

        genericSubjectResource.setSession(session);
        genericSubjectResource.setRequest(request);
        genericSubjectResource.getSearchSubjects(bogusSearchData);
        verify(genericSubjectService, times(1)).getSearchedSubjects(bogusLastName, bogusFirstName, bogusMrn, "", genderList);

        defaultSubjectResource.setSession(session);
        defaultSubjectResource.setRequest(request);
        defaultSubjectResource.getSearchSubjects(bogusSearchData);
        verify(defaultSubjectService, times(1)).getSearchedSubjects(bogusLastName, bogusFirstName, bogusMrn, "", genderList);

        epicSubjectResource.setSession(session);
        epicSubjectResource.setRequest(request);
        epicSubjectResource.getSearchSubjects(bogusSearchData);
        verify(epicSubjectService, times(1)).getSearchedSubjects(bogusLastName, bogusFirstName , bogusMrn, "", genderList);
    }

    @Test
    public void testGetSubjectData() throws Exception {
        final int bogusId = 1234;
        final String bogusSearchData = "{\"id\":" +bogusId+ "}";

        genericSubjectResource.setSession(session);
        genericSubjectResource.setRequest(request);
        genericSubjectResource.getSubjectData(null, bogusSearchData);
        verify(genericSubjectService, times(1)).getSubjectData(null, bogusId, host, user);

        defaultSubjectResource.setSession(session);
        defaultSubjectResource.setRequest(request);
        defaultSubjectResource.getSubjectData(null, bogusSearchData);
        verify(defaultSubjectService, times(1)).getSubjectData(null, bogusId, host, user);

        epicSubjectResource.setSession(session);
        epicSubjectResource.setRequest(request);
        epicSubjectResource.getSubjectData(null, bogusSearchData);
        verify(epicSubjectService, times(1)).getSubjectData(null, bogusId, host, user);
    }

    @Test
    public void testGetExternalSubjectDataUsingMrnInfo() throws Exception {
        final String mrn = "1000";
        final String site = "inst";
        final String status = MrnStatus.ACTIVE.getCode();
        final String mrnSearchData = "{\"mrnInfo\":{\"value\":\"" + mrn + "\"" +
                ", \"institution\":\"" + site + "\"" +
                ", \"status\":\"" + status + "\"}}";

        MrnInfoDTO mrnInfoDTO = new MrnInfoDTO(mrn, site, status);

        epicSubjectResource.setSession(session);
        epicSubjectResource.setRequest(request);
        epicSubjectResource.getExternalSubjectDataUsingMrnInfo(mrnSearchData);
        verify(epicSubjectService, times(1)).getExternalSubjectDataUsingMrn(mrnInfoDTO);
    }

    @Test
    public void testFindInternalSubjectByMrn() throws Exception {
        final String mrn = "1000";
        final String site = "inst";
        final String status = MrnStatus.ACTIVE.getCode();
        final String mrnSearchData = "{\"mrnInfoList\":[{\"value\":\"" + mrn + "\"" +
                ", \"institution\":\"" + site + "\"" +
                ", \"status\":\"" + status + "\"}]}";

        List<MrnInfoDTO> mrnInfoDTOList = new ArrayList<>();
        MrnInfoDTO mrnInfoDTO = new MrnInfoDTO(mrn, site, status);
        mrnInfoDTOList.add(mrnInfoDTO);

        defaultSubjectResource.setSession(session);
        defaultSubjectResource.setRequest(request);
        defaultSubjectResource.findInternalSubjectByMrn(mrnSearchData);
        verify(defaultSubjectService, times(1)).findInternalSubjectByMrn(mrnInfoDTOList);

        epicSubjectResource.setSession(session);
        epicSubjectResource.setRequest(request);
        epicSubjectResource.findInternalSubjectByMrn(mrnSearchData);
        verify(epicSubjectService, times(1)).findInternalSubjectByMrn(mrnInfoDTOList);
    }
}
