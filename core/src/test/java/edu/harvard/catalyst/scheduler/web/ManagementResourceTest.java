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

import edu.harvard.catalyst.scheduler.dto.response.UserDataResponse;
import edu.harvard.catalyst.scheduler.entity.InstitutionRole;
import edu.harvard.catalyst.scheduler.entity.User;
import edu.harvard.catalyst.scheduler.security.SchedulerSession;
import edu.harvard.catalyst.scheduler.security.SchedulerUserDetails;
import edu.harvard.catalyst.scheduler.service.AuthService;
import edu.harvard.catalyst.scheduler.util.ServiceTestsDAOUtility;
import edu.harvard.catalyst.scheduler.util.TestUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import javax.servlet.http.HttpServletRequest;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.*;

/**
 * Created by marc-danie on 6/14/17.
 */
@RunWith(MockitoJUnitRunner.class)
public class ManagementResourceTest {
    ManagementResource managementResource;
    private AuthService mockAuthService = mock(AuthService.class);

    private final HttpServletRequest request = mock(HttpServletRequest.class);
    private final SchedulerSession session = mock(SchedulerSession.class);
    private final SchedulerUserDetails schedulerUserDetails = mock(SchedulerUserDetails.class);
    private final User mockUserInSession = ServiceTestsDAOUtility.createMockUser();
    private final String mockHostnameInRequest = "mockHostnameInRequest";

    @Before
    public void setUp() {

        managementResource = new ManagementResource(mockAuthService);
        managementResource.setSession(session);
        managementResource.setRequest(request);

        when(session.getUserDetails()).thenReturn(schedulerUserDetails);
        when(schedulerUserDetails.getUser()).thenReturn(mockUserInSession);
        when(request.getRemoteHost()).thenReturn(mockHostnameInRequest);
    }

    @Test
    public void testGetStudyMembersStudyScreen(){

        String filterString = "testUser";
        String orderBy = "ASC";
        String sortBy = "u.lastName";

        List<UserDataResponse> userDataResponseList = new ArrayList();

        User user = new User();
        user.setFirstName("Foo");
        user.setLastName("BarLast");
        user.setEcommonsId("fb7824");
        user.setId(423);

        InstitutionRole instRole = new InstitutionRole();
        instRole.setName("Admin");
        user.setInstitutionRole(instRole);
        UserDataResponse userDataResponse  = new UserDataResponse(user, Long.valueOf(1));
        userDataResponseList.add(userDataResponse);

        when(mockAuthService.getStudyMembers(filterString, sortBy, orderBy)).thenReturn(userDataResponseList);

        String actualJsonResult = managementResource.getStudyMembersStudyScreen(filterString, sortBy, orderBy);

        verify(mockAuthService, times(1)).getStudyMembers(
                filterString, sortBy, orderBy);

        TestUtils.assertEqualJson(managementResource.gson, userDataResponseList, actualJsonResult);
    }
}
