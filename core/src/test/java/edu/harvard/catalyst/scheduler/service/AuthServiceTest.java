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

import com.google.gson.Gson;
import edu.harvard.catalyst.scheduler.core.Statics;
import edu.harvard.catalyst.scheduler.dto.*;
import edu.harvard.catalyst.scheduler.dto.response.GetUsersResponse;
import edu.harvard.catalyst.scheduler.dto.response.UserDataResponse;
import edu.harvard.catalyst.scheduler.dto.response.UserDetailResponse;
import edu.harvard.catalyst.scheduler.entity.*;
import edu.harvard.catalyst.scheduler.persistence.AuthDAO;
import edu.harvard.catalyst.scheduler.persistence.ResourceDAO;
import edu.harvard.catalyst.scheduler.persistence.StudyDAO;
import edu.harvard.catalyst.scheduler.security.SchedulerUserDetails;
import edu.harvard.catalyst.scheduler.util.*;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

/**
 * @author Anusha Chitla
 */

@RunWith(MockitoJUnitRunner.class)
public class AuthServiceTest {

    private AuthService authService;
    @Mock
    private StudyDAO mockStudyDAO;
    @Mock
    private AuthDAO mockAuthDAO;
    @Mock
    private MailHandler mockMailHandler;
    @Mock
    private ResourceDAO mockResourceDAO;
    @Mock
    private AuditService mockAuditService;
    @Mock
    private GetUsersDTO mockGetUsersDTO;
    @Mock
    private LoginDTO mockLoginDTO;
    @Mock
    private User user;

    private final String username = "username";
    private final String password = "password";
    private final String session = "session";
    private final String ipAddress = "ipAddress";

    @Before
    public void setUp() {
        authService = new AuthService(mockAuthDAO, mockStudyDAO, mockResourceDAO, mockAuditService, mockMailHandler);
    }

    @Test(expected = BadCredentialsException.class)
    public void testAuthenticateUserBadCredentials() throws Exception {
        when(mockAuthDAO.authenticate(username, password, session)).thenReturn(null);
        try {
            authService.authenticateUser(username, password, session, ipAddress);
        } finally {
            verify(mockAuthDAO).authenticate(username, password, session);
        }
    }

    @Test
    public void authenticateUserPositiveTest() throws Exception {
        final User user = ServiceTestsDAOUtility.createMockUser();
        final UserSession userSession = ServiceTestsDAOUtility.createUserSession(user);
        when(mockAuthDAO.authenticate(username, password, session)).thenReturn(user);
        when(mockAuthDAO.createUserSession(session, ipAddress, user)).thenReturn(userSession);

        final SchedulerUserDetails actualDetails = authService.authenticateUser(username, password, session, ipAddress);

        assertNotNull(actualDetails);
        assertEquals(User.STATUS_AUTHORIZED, (int) actualDetails.getUser().getAuthStatus());
        assertEquals(userSession, actualDetails.getUserSession());
        assertEquals(user, actualDetails.getUser());
        verify(mockAuthDAO).authenticate(username, password, session);
        verify(mockAuthDAO).createUserSession(session, ipAddress, user);
    }

    @Test(expected = DisabledException.class)
    public void authenticateUserDisabledTest() throws Exception {
        final User user = ServiceTestsDAOUtility.createMockUser();
        user.setActive(false);
        when(mockAuthDAO.authenticate(username, password, session)).thenReturn(user);
        try {
            authService.authenticateUser(username, password, session, ipAddress);
        } finally {
            verify(mockAuthDAO).authenticate(username, password, session);
        }
    }

    @Test
    public void doPasswordResetTest() throws Exception {
        final User userMock = ServiceTestsDAOUtility.createMockUser();
        when(mockAuthDAO.findUserByEmail("Jtc7101%")).thenReturn(userMock);
        assertNotNull(userMock);
        final String newPassword = "BIBnjs2.2";
        userMock.setPassword(newPassword);
        final PasswordResetDTO passwordResetDTO = new PasswordResetDTO();
        passwordResetDTO.setEmail("Jtc7101%");
        assertNotNull(passwordResetDTO);

        final Boolean bool = authService.doPasswordReset(passwordResetDTO);
        assertEquals(true, bool);
        mockMailHandler.sendMandatoryEmails(ServiceTestsDAOUtility.messageMock());

        String badUserEmail = "badUserEmail@nothere.com";
        when(mockAuthDAO.findUserByEmail(badUserEmail)).thenReturn(null);

        PasswordResetDTO passwordResetDTO1 = mock(PasswordResetDTO.class);
        when(passwordResetDTO1.getEmail()).thenReturn(badUserEmail);

        boolean result = authService.doPasswordReset(passwordResetDTO1);
        assertFalse(result);
    }

    @Test
    public void generatePasswordTest() {
        final String alphabet = "$%!@#^&?";
        final int n = alphabet.length();
        final Random r = new Random();
        assertNotNull(n);
        assertNotNull(r);
        final String pswd = authService.generatePassword();
        assertNotNull(pswd);
    }

    //    @Test
    //    public void getUsersTest() throws Exception {
    //        List<User> userDTO = new ArrayList<User>();
    //        User dto = new User();
    //        dto.setEcommonsId("ss123");
    //        dto.setPrimaryPhone("9724561289");
    //        User dto2 = new User();
    //        dto2.setEcommonsId("tv123");
    //        dto2.setPrimaryPhone("2173091108");
    //        userDTO.add(dto);
    //        userDTO.add(dto2);
    //        User user = new User();
    //        user.setEcommonsId("gh123");
    //        mockGetUsersDTO.setUsers(userDTO);
    //        String strippedString = dto.getPrimaryPhone().replaceAll("[^a-zA-Z0-9]+", "");
    //        dto.setPrimaryPhone(strippedString);
    //        String strippedString1 = dto2.getPrimaryPhone().replaceAll("[^a-zA-Z0-9]+", "");
    //        dto2.setPrimaryPhone(strippedString1);
    //        when(mockAuthDAO.getUsers(mockGetUsersDTO, 3, 4)).thenReturn(userDTO);
    //        assertNotNull(userDTO.size());
    //        assertNotNull(userDTO);
    //        List<User> displayedUsers = authService.getUsers(mockGetUsersDTO, null, user, 3, 4);
    //        assertNotNull(displayedUsers);
    //
    //    }
    //
    //    @Test
    //    public void getStudyMembersTest() throws Exception {
    //        List<User> userDTO = new ArrayList<User>();
    //        User dto = new User();
    //        dto.setEcommonsId("ft123");
    //        dto.setPrimaryPhone("9724561289");
    //        User dto2 = new User();
    //        dto2.setEcommonsId("jg123");
    //        dto2.setPrimaryPhone("2173091108");
    //        userDTO.add(dto);
    //        userDTO.add(dto2);
    //        User user = new User();
    //        user.setEcommonsId("bd123");
    //        mockGetUsersDTO.setUsers(userDTO);
    //        when(mockAuthDAO.getUsers(mockGetUsersDTO, 3, 4)).thenReturn(userDTO);
    //        String strippedString = dto.getPrimaryPhone().replaceAll("[^a-zA-Z0-9]+", "");
    //        dto.setPrimaryPhone(strippedString);
    //        String strippedString1 = dto2.getPrimaryPhone().replaceAll("[^a-zA-Z0-9]+", "");
    //        dto2.setPrimaryPhone(strippedString1);
    //        assertNotNull(userDTO.size());
    //        assertNotNull(userDTO);
    //        List<User> displayedUsers = authService.getStudyMembers(mockGetUsersDTO);
    //        assertNotNull(displayedUsers);
    //    }

    @Test
    public void CheckEmailPositiveTest() throws Exception {
        mockLoginDTO.setUser(ServiceTestsDTOUtility.loginCredentials());
        when(mockAuthDAO.checkEmail(mockLoginDTO.getEmail())).thenReturn(true);
        final BooleanResultDTO booleanVal = authService.checkEmail(mockLoginDTO);
        assertEquals(true, booleanVal.isResult());
    }

    @Test
    public void CheckEmailNegativeTest() throws Exception {
        mockLoginDTO.setUser(ServiceTestsDTOUtility.loginCredentials());
        when(mockAuthDAO.checkEmail(mockLoginDTO.getEmail())).thenReturn(false);
        final BooleanResultDTO booleanVal = authService.checkEmail(mockLoginDTO);
        assertEquals(false, booleanVal.isResult());
    }
    @Test
    public void getRolesPositiveTest() throws Exception {
        final List<Role> roles = new ArrayList<Role>();
        final Role r1 = new Role();
        final Role r2 = new Role();
        final Role r3 = new Role();
        r1.setId(1);
        r2.setId(3);
        r3.setId(12);
        roles.add(r1);
        roles.add(r2);
        roles.add(r3);

        when(mockAuthDAO.getRolesExceptFinalApprover()).thenReturn(roles);
        final int expectedId1 = r1.getId();
        final int expectedId2 = r2.getId();
        final List<Role> role = authService.getRoles();
        final int actualId1 = role.get(0).getId();
        final int actualId2 = role.get(1).getId();
        assertEquals(expectedId1, actualId1);
        assertEquals(expectedId2, actualId2);
    }

    @Test
    public void getRolesNegativeTest() throws Exception {
        final List<Role> roles = new ArrayList<Role>();
        final Role r1 = new Role();
        final Role r2 = new Role();
        final Role r3 = new Role();
        r1.setId(1);
        r2.setId(3);
        r3.setId(12);
        roles.add(r1);
        roles.add(r2);
        roles.add(r3);

        when(mockAuthDAO.getRolesExceptFinalApprover()).thenReturn(roles);
        final int expectedId1 = r1.getId();
        final int expectedId2 = r2.getId();
        final List<Role> role = authService.getRoles();
        final int actualId1 = role.get(0).getId();
        final int actualId2 = role.get(1).getId();
        assertEquals(expectedId1, actualId1);
        assertEquals(expectedId2, actualId2);
    }

    @Test
    public void PasswordLengthTrueTest() {
        final Boolean boolValue = authService.testPassword("Njss101$");
        assertEquals(true, boolValue);
    }

    @Test
    public void PasswordLengthFalseTest() {
        final Boolean boolValue = authService.testPassword("Ns101$");
        assertEquals(false, boolValue);
    }

    @Test
    public void PasswordLowercaseTrueTest() {
        final Boolean boolValue = authService.testPassword("NjS2101$");
        assertEquals(true, boolValue);

    }

    @Test
    public void PasswordLowerCaseFalseTest() {
        final Boolean boolValue = authService.testPassword("NJSR2101$");
        assertEquals(false, boolValue);
    }

    @Test
    public void PasswordNumericTrueTest() {
        final Boolean boolValue = authService.testPassword("NjS2101$");
        assertEquals(true, boolValue);

    }

    @Test
    public void PasswordNumericFalseTest() {
        final Boolean boolValue = authService.testPassword("NjSRabcd$");
        assertEquals(false, boolValue);
    }

    @Test
    public void PasswordPunctuationTrueTest() {
        final Boolean boolValue = authService.testPassword("NjS2101$");
        assertEquals(true, boolValue);

    }

    @Test
    public void PasswordPunctuationFalseTest() {
        final Boolean boolValue = authService.testPassword("NjSRabcdy5");
        assertEquals(false, boolValue);
    }

    @Test
    public void PasswordUpperCaseFalseTest() {
        final Boolean boolValue = authService.testPassword("onlylowercase5");
        assertEquals(false, boolValue);
    }

    @Test
    public void updatePasswordNegativeTest() throws Exception {
        final User user = ServiceTestsDAOUtility.sampleUser();
        final UserDTO dto = ServiceTestsDTOUtility.updatePassword();
        when(mockAuthDAO.findUserById(user.getId())).thenReturn(ServiceTestsDAOUtility.sampleUser());
        dto.setPassword("Njs2101ty");
        authService.updatePassword(dto, user);
        final String errorMsg = dto.getErrorMsg();
        assertEquals(Statics.INSUFFICIENT_PASSWORD, errorMsg);

    }

    @Test
    public void updatePasswordPositiveTest() throws Exception {
        final User user = ServiceTestsDAOUtility.createMockUser();
        final UserDTO dto = ServiceTestsDTOUtility.updatePassword();
        when(mockAuthDAO.findUserById(user.getId())).thenReturn(ServiceTestsDAOUtility.createMockUser());
        dto.setPassword("BIBnjs2.2");
        authService.updatePassword(dto, user);
        assertEquals(true, dto.isResult());
    }

    @Test
    public void CheckEcommonsIdPositiveTest() throws Exception {
        mockLoginDTO.setUser(ServiceTestsDTOUtility.loginCredentials());
        when(mockAuthDAO.checkEcommonsId(mockLoginDTO.getEcommonsId())).thenReturn(true);
        final BooleanResultDTO booleanVal = authService.checkEcommonsId(mockLoginDTO);
        assertEquals(true, booleanVal.isResult());
    }

    @Test
    public void CheckEcommonsIdNegativeTest() throws Exception {
        mockLoginDTO.setUser(ServiceTestsDTOUtility.loginCredentials());
        when(mockAuthDAO.checkEcommonsId(mockLoginDTO.getEcommonsId())).thenReturn(false);
        final BooleanResultDTO booleanVal = authService.checkEcommonsId(mockLoginDTO);
        assertEquals(false, booleanVal.isResult());

    }

    //    @Test
    //    public void deactivateUserTest() throws Exception {
    //        User users = ServiceTestsDAOUtility.sampleUser();
    //        UserDTO dto = ServiceTestsDTOUtility.toDeactivate();
    //        when(mockAuthDAO.findUserById(dto.getUserToDeactivateId())).thenReturn(users);
    //        authService.deactivateUser(dto, ServiceTestsDAOUtility.createMockUser(), "1.0.0");
    //        assertEquals(false, users.getActive());
    //    }
    //
    //    @Test
    //    public void activateUserTest() throws Exception {
    //        User inactiveUser = new User();
    //        inactiveUser.setFirstName("carol");
    //        inactiveUser.setLastName("turner");
    //        inactiveUser.setEcommonsId("ct968");
    //        inactiveUser.setPassword("GTui892%%");
    //        inactiveUser.setEmail("ct8923@gmail.com");
    //        inactiveUser.setId(5);
    //        inactiveUser.setActive(false);
    //
    //        UserDTO dto = ServiceTestsDTOUtility.toActivate();
    //        when(mockAuthDAO.findUserById(dto.getUserToActivateId())).thenReturn(inactiveUser);
    //        authService.activateUser(dto, ServiceTestsDAOUtility.createMockUser(), "contextPath", "1.0.0.0", "server", 8080);
    //        assertEquals(true, inactiveUser.getActive());
    //
    //    }

    @Test
    public void logViewUserTest() throws Exception {
        final User user = ServiceTestsDAOUtility.sampleUser();
        final UserDTO dto = ServiceTestsDTOUtility.createUserDTO();
        when(mockAuthDAO.findUserById(5)).thenReturn(user);
        assertNotNull(dto);
        assertNotNull(user);
        authService.logViewUser(dto, user, "1.0.0");
    }

    @Test
    public void createUserTest() throws Exception {
        final UserDTO dto1 = ServiceTestsDTOUtility.createUserDTO();
        dto1.setNotificationEmail("randall@bidmc.org");
        dto1.setInstitutionRoleType(InstitutionRoleType.ROLE_FRONT_DESK);

        InstitutionRole frontDesk = mock(InstitutionRole.class);
        when(mockStudyDAO.findInstitutionRoleByType(InstitutionRoleType.ROLE_FRONT_DESK)).thenReturn(frontDesk);

        String validPassword = authService.generatePassword();

        AuthService spyAuthService = spy(authService);

        when(spyAuthService.generatePassword()).thenReturn(validPassword);

        dto1.setPassword("failme");
        final User user = ServiceTestsDAOUtility.createMockUser();

        doNegativeCreateUserTest(spyAuthService, dto1, Statics.INSUFFICIENT_PASSWORD, user);

        dto1.setPassword("leGalP4$$w0rd");
        doNegativeCreateUserTest(spyAuthService, dto1, "Ecommons ID already in system", user);

        when(mockAuthDAO.checkEcommonsId(anyString())).thenReturn(true);
        doNegativeCreateUserTest(spyAuthService, dto1, "Email already in system", user);

        dto1.setGenerateNewPassword(true);

        when(mockAuthDAO.checkEcommonsId(anyString())).thenReturn(true);
        when(mockAuthDAO.checkEmail(anyString())).thenReturn(true);

        BooleanSwitch savedUser = new BooleanSwitch();

        doAnswer(new Answer() {
            public Object answer(InvocationOnMock invocation) {
                Object[] args = invocation.getArguments();
                // args[0]: the first arg, that I want to modify
                User user = (User) args[0];
                assertNotEquals(validPassword, user.getPassword());
                savedUser.setState(true);
                assertEquals("randall@bidmc.org", user.getNotificationEmail());
                assertEquals(frontDesk, user.getInstitutionRole());

                //return null since it's a void method:
                return null;
            }
        }).when(mockAuthDAO).createEntity(any(User.class));

        UserDTO response = spyAuthService.createUser(dto1, user, "/foo/bar", "18.209.0.57", "ack", 8001);
        assertEquals(validPassword, response.getPassword());
        assertTrue(response.isResult());
        assertTrue(savedUser.isState());
    }


    @Ignore
    public void updateUserTest() throws Exception {
        final UserDTO dto = ServiceTestsDTOUtility.createUserDTO();

    }

    @Test
    public void testSmallStringConstructors() {
        User mockUser = mock(User.class);
        when(mockUser.getFirstName()).thenReturn("Steven");
        when(mockUser.getLastName()).thenReturn("Universe");

        String expected = "Welcome Steven Universe";

        String actual = authService.makeEmailUserSubject(mockUser);
        assertEquals(expected, actual);

        String expectedUrl = "https://foobar.harvard.edu:8001/foo/bar";
        String actualUrl = authService.makeUrl("/foo/bar", "foobar.harvard.edu", 8001);
        assertEquals(expectedUrl, actualUrl);
    }

    @Test
    public void testMessageFormatting() {
        String subject = "re: testing";
        User user = mock(User.class);
        when(user.getFirstName()).thenReturn("George");
        when(user.getLastName()).thenReturn("Washington");

        String url = "https://open.med.harvard.edu";

        String msg = authService.makeNewUserMessage(subject, user, url, true);

        String expected = "\n<html><head><title>re: testing</title><style></style>\n</head>\n<body><p>Welcome to the CRC Scheduler.</p>\n<p><strong>George Washington,<strong></p>\n" +
                "<p>You will have access to the system once approved by the Super Admin. Please wait for the final confirmation.</p>\n " +
                "<p> You can go to https://open.med.harvard.edu and click on the \"forgot password\" link to create a new random password.</p>\n " +
                "<p>This will be emailed to you and then you may change your password once you have logged in.</p>\n</body>\n</html>\n";

        assertEquals(expected, msg);

        String expected2 =
                "<html><head><title></title><style></style></head><body><p><strong>George Washington has registered.<strong></p>\n" +
                        " <p> You can go to https://open.med.harvard.edu and activate the user.</p>\n<p>The user will be notified" +
                        " of their access to the system once the account is activated.</p>\n</body>\n</html>\n";

        String msg2 = authService.makeNewUserAdminMessage(user, url);
        assertEquals(expected2, msg2);


        String expected3 = "\n<html><head><title>re: testing</title><style></style>\n</head>\n<body><p>Welcome to the CRC Scheduler.</p>\n<p><strong>George Washington,<strong></p>\n" +
                "<p> You can go to https://open.med.harvard.edu and click on the \"forgot password\" link to create a new random password.</p>\n " +
                "<p>This will be emailed to you and then you may change your password once you have logged in.</p>\n</body>\n</html>\n";

        String msg3 = authService.makeNewUserMessage(subject, user, url, false);

        assertEquals(expected3, msg3);
    }

    @Test
    public void doRegisterUserTest() throws Exception {
        final UserDTO dto = ServiceTestsDTOUtility.createUserDTO();

        //make password fail:
        dto.setPassword("failme"); //no uppercase
        doNegativeRegisterUserTest(dto, Statics.INSUFFICIENT_PASSWORD);

        //put password back to be legal:
        dto.setPassword("leGalP4$$w0rd");

        //play with ecommons:
        when(mockAuthDAO.checkEcommonsId(anyString())).thenReturn(false);

        doNegativeRegisterUserTest(dto, "Ecommons ID already in system");

        when(mockAuthDAO.checkEcommonsId(anyString())).thenReturn(true);
        when(mockAuthDAO.checkEmail(anyString())).thenReturn(false);

        doNegativeRegisterUserTest(dto, "Email already in system");

        //clear out the ecommons:
        when(mockAuthDAO.checkEmail(anyString())).thenReturn(true);


        InstitutionRole studyStaff = mock(InstitutionRole.class);
        when(mockStudyDAO.findInstitutionRoleByType(InstitutionRoleType.ROLE_STUDY_STAFF)).thenReturn(studyStaff);

        BooleanSwitch savedUser = new BooleanSwitch();

        doAnswer(new Answer() {
            public Object answer(InvocationOnMock invocation) {
                Object[] args = invocation.getArguments();
                // args[0]: the first arg, that I want to modify
                User user = (User) args[0];
                assertNotEquals("leGalP4$$w0rd", user.getPassword());
                savedUser.setState(true);

                assertEquals(studyStaff, user.getInstitutionRole());

                //return null since it's a void method:
                return null;
            }
        }).when(mockAuthDAO).createEntity(any(User.class));

        //positive:
        UserDTO response = authService.doRegisterUser(dto, "18.209.0.57");
        assertTrue(response.isResult());
        assertTrue(savedUser.isState());
    }


    public void doNegativeRegisterUserTest(UserDTO dto, String errorMsg) {
        UserDTO response = authService.doRegisterUser(dto,  "18.209.0.57");
        assertFalse(response.isResult());
        assertEquals(errorMsg, response.getErrorMsg());
    }

    public void doNegativeCreateUserTest(AuthService aService, UserDTO dto, String errorMsg, User user) {
        UserDTO response = aService.createUser(dto, user, "context", "18.209.0.57", "fdc.mit.edu", 888);
        assertFalse(response.isResult());
        assertEquals(errorMsg, response.getErrorMsg());
    }

    @Test
    public void testIsBadEmailOrEcommonsId() {
        final UserDTO userDTO = mock(UserDTO.class);

        when(userDTO.getEcommonsId()).thenReturn("bad");
        when(mockAuthDAO.checkEcommonsId("bad")).thenReturn(false);

        boolean result = authService.isBadEmailOrEcommonsId(userDTO);
        assertTrue("'bad' should be bad ID!", result);

        when(userDTO.getEcommonsId()).thenReturn("good");
        when(mockAuthDAO.checkEcommonsId("good")).thenReturn(true);
        when(userDTO.getEmail()).thenReturn("badEmail");
        when(mockAuthDAO.checkEmail("badEmail")).thenReturn(false);

        result = authService.isBadEmailOrEcommonsId(userDTO);
        assertTrue("'badEmail' should be bad email!", result);

        when(userDTO.getEmail()).thenReturn("goodEmail");
        when(mockAuthDAO.checkEmail("goodEmail")).thenReturn(true);

        result = authService.isBadEmailOrEcommonsId(userDTO);
        assertTrue("good values should be OK!", !result);
    }

    @Test
    public void testSetSomeUserProperties() {
        final UserDTO userDTO = mock(UserDTO.class);

        final String id = "ID";
        final String firstName = "firstName";

        when(userDTO.getEcommonsId()).thenReturn(id);
        when(userDTO.getFirstName()).thenReturn(firstName);

        final User user = mock(User.class);
        final InstitutionRoleType institutionRoleType = InstitutionRoleType.ROLE_FRONT_DESK;

        authService.setSomeUserProperties(userDTO, user, institutionRoleType);

        verify(user, times(1)).setEcommonsId(id);
        verify(user, times(1)).setFirstName(firstName);
    }

    @Test
    public void testHasPassword() {
        final UserDTO dto = new UserDTO();

        assertNull(dto.getPassword());

        assertFalse(AuthService.hasPassword(dto));

        dto.setPassword("");

        assertFalse(AuthService.hasPassword(dto));

        dto.setPassword("foo");

        assertTrue(AuthService.hasPassword(dto));

        dto.setPassword("bar");

        assertTrue(AuthService.hasPassword(dto));
    }


    @Test
    public void testServiceHelper(){
        String role = authService.lookupFieldById(0, mockStudyDAO::findRoleById);
        assertNull(role);
    }

    @Test
    public void testComparePasswords() {
        final UserDTO dto = new UserDTO();
        dto.setPassword("ack123");

        final User user = new User();
        user.setPassword("ook123");

        assertFalse(authService.passwordsMatch(dto, user));

        user.setPassword("ack123");

        assertTrue(authService.passwordsMatch(dto, user));

        dto.setPassword(null);

        assertFalse(authService.passwordsMatch(dto, user));
    }


    @Test
    public void testCompareECommonsIds() {
        final UserDTO dto = new UserDTO();
        dto.setEcommonsId("ack123");

        final User user = new User();
        user.setEcommonsId("ook123");

        assertFalse(authService.compareEcommonsIds(dto, user));

        user.setEcommonsId("ack123");

        assertTrue(authService.compareEcommonsIds(dto, user));

        dto.setEcommonsId("randomPssword");
        user.setEcommonsId(null);

        assertFalse(authService.compareEcommonsIds(dto, user));
    }

    @Test
    public void testGetUsers()
    {
        final String filterString = "filter";
        final String sortBy = "sortBy";
        final String orderBy = "asc";
        final int page = 1;
        final int maxResults = 5;

        User user1 = new User();
        user1.setFirstName("user1 first name");
        user1.setLastName("user1 last name");
        user1.setEcommonsId("ecommonsId1");
        user1.setId(2);
        user1.setActive(true);
        user1.setPrimaryPhone("1112223333");

        User user2 = new User();
        user2.setFirstName("user2 first name");
        user2.setLastName("user2 last name");
        user2.setEcommonsId("ecommonsId2");
        user2.setId(78);
        user2.setActive(false);
        user2.setPrimaryPhone("7778889999");

        List<User> userList = new ArrayList<>();
        userList.add(user1);
        userList.add(user2);

        SearchDTO searchDTO = new SearchDTO();
        SearchDTO.SearchItem searchItem1 = new SearchDTO.SearchItem();
        searchItem1.setKey("key 1");
        searchItem1.setValue("value 1");
        searchItem1.setSearchType(SearchDTO.SearchType.ANY);

        SearchDTO.SearchItem searchItem2 = new SearchDTO.SearchItem();
        searchItem2.setKey("key 2");
        searchItem2.setValue("value 2");
        searchItem2.setSearchType(SearchDTO.SearchType.ANY);

        List<SearchDTO.SearchItem> searchItems = new ArrayList<>();
        searchItems.add(searchItem1);
        searchItems.add(searchItem2);
        searchDTO.setSearchItems(searchItems);

        Gson gson = new Gson();
        GetUsersResponse expectedUsersResponse = GetUsersResponse.createGetUsersResponse(userList, (long)2);
        String expectedUsersResponseString = gson.toJson(expectedUsersResponse);

        // CASE 1: 2 search items

        when(mockAuthDAO.getUsers(searchDTO, sortBy, orderBy, page, maxResults)).thenReturn(expectedUsersResponse);

        GetUsersResponse usersResponse = authService.getUsers(searchDTO, sortBy, orderBy, page, maxResults, ipAddress, user);

        String usersResponseString = gson.toJson(usersResponse);
        assertEquals(expectedUsersResponseString, usersResponseString);
        verify(mockAuditService, times(1)).logViewActivity(ipAddress, user, "All Users Viewed.");
        verify(mockAuthDAO, times(1)).getUsers(searchDTO, sortBy, orderBy, page, maxResults);

        // CASE 2: no search DTO

        when(mockAuthDAO.getUsers(null, sortBy, orderBy, page, maxResults)).thenReturn(expectedUsersResponse);

        usersResponse = authService.getUsers(null, sortBy, orderBy, page, maxResults, ipAddress, user);

        usersResponseString = gson.toJson(usersResponse);
        assertEquals(expectedUsersResponseString, usersResponseString);
        // NOTE: logViewActivity is called with exactly the same parameters each time getUsers() is called
        // so the number of times it has been called, according to mockito, gets incremented by 1
        // each time. On the other hand, getSers() itself is called with different parameters
        // each time, so we expect it to have been called just once with the given parameters
        verify(mockAuditService, times(2)).logViewActivity(ipAddress, user, "All Users Viewed.");
        verify(mockAuthDAO, times(1)).getUsers(null, sortBy, orderBy, page, maxResults);

        // CASE 3: search phone number - expect "OR" search item

        searchDTO = new SearchDTO();
        searchItem1 = new SearchDTO.SearchItem();
        searchItem1.setKey("u.primaryPhone");
        searchItem1.setValue("(__1) 2_2-3___");
        searchItem1.setSearchType(SearchDTO.SearchType.ANY);

        searchItems = new ArrayList<>();
        searchItems.add(searchItem1);
        searchDTO.setSearchItems(searchItems);

        when(mockAuthDAO.getUsers(searchDTO, sortBy, orderBy, page, maxResults)).thenReturn(expectedUsersResponse);

        usersResponse = authService.getUsers(searchDTO, sortBy, orderBy, page, maxResults, ipAddress, user);

        usersResponseString = gson.toJson(usersResponse);
        assertEquals(expectedUsersResponseString, usersResponseString);
        // NOTE: logViewActivity is called with exactly the same parameters each time getUsers() is called
        // so the number of times it has been called, according to mockito, gets incremented by 1
        // each time. On the other hand, getUsers() itself is called with different parameters
        // each time, so we expect it to have been called just once with the given parameters
        verify(mockAuditService, times(3)).logViewActivity(ipAddress, user, "All Users Viewed.");
        verify(mockAuthDAO, times(1)).getUsers(searchDTO, sortBy, orderBy, page, maxResults);

        List<SearchDTO.SearchItem> orSearchItems = searchItem1.getOrSearchItems();
        assertEquals(2, orSearchItems.size());
        SearchDTO.SearchItem orSearchItem1 = orSearchItems.get(0);
        SearchDTO.SearchItem orSearchItem2 = orSearchItems.get(1);

        assertEquals(null, searchItem1.getKey());
        assertEquals(null, searchItem1.getValue());

        assertEquals("u.primaryPhone", orSearchItem1.getKey());
        assertEquals("__1-2_2-3___", orSearchItem1.getValue());

        assertEquals("u.primaryPhone", orSearchItem2.getKey());
        assertEquals("(__1) 2_2-3___", orSearchItem2.getValue());

    }

    @Test
    public void testGetUserData()
    {
        int userId = 7;

        User user1 = new User();
        user1.setId(userId);
        UserDetailResponse expectedUserDetailResponse = new UserDetailResponse(user1);
        when(mockAuthDAO.getUserData(userId)).thenReturn(expectedUserDetailResponse);

        Gson gson = new Gson();
        String expectedUserDetailResponseString = gson.toJson(expectedUserDetailResponse);

        UserDetailResponse userDetailResponse = authService.getUserData(userId);

        String userDetailResponseString = gson.toJson(userDetailResponse);
        assertEquals("get user data", expectedUserDetailResponseString, userDetailResponseString);
    }

    @Test
    public void testUpdateInvalidUser()
    {
        UserDTO userDTO = ServiceTestsDTOUtility.createUserDTO();
        int userId = userDTO.getId();

        String password = "bar";

        String ecommonsId = userDTO.getEcommonsId();
        String newEcommonsId = "diffEcommons";;
        User user = new User();
        user.setEcommonsId(newEcommonsId);
        user.setPassword(userDTO.getPassword());
        user.setPassword(password);
        user.setEmail("diffEmail");

        when(mockAuthDAO.findUserById(userId)).thenReturn(user);

        UserDTO result = authService.updateUser(userDTO, user, ipAddress);

        when(mockAuthDAO.checkEcommonsId(newEcommonsId)).thenReturn(false);

        assertEquals("user ecommons id exists", Statics.ECOMMONS_ID_EXISTS, result.getErrorMsg());

        user.setEcommonsId(ecommonsId);
        when(mockAuthDAO.checkEcommonsId(ecommonsId)).thenReturn(true);
        when(mockAuthDAO.checkEmail(userDTO.getEmail())).thenReturn(false);

        result = authService.updateUser(userDTO, user, ipAddress);
        assertEquals("user email exists", Statics.EMAIL_EXISTS, result.getErrorMsg());


        user.setPassword("diffPassword");
        when(mockAuthDAO.checkEcommonsId(ecommonsId)).thenReturn(true);
        when(mockAuthDAO.checkEmail(userDTO.getEmail())).thenReturn(true);

        result = authService.updateUser(userDTO, user, ipAddress);
        assertEquals("invalid password", Statics.INSUFFICIENT_PASSWORD, result.getErrorMsg());
    }

    @Test
    public void testUpdateValidUser()
    {
        UserDTO userDTO = ServiceTestsDTOUtility.createUserDTO();
        int userId = userDTO.getId();

        InstitutionRoleType instRoleType = InstitutionRoleType.ROLE_STUDY_STAFF;
        userDTO.setInstitutionRoleType(instRoleType);

        User user = new User();
        user.setId(userId);
        user.setPassword(userDTO.getPassword());
        user.setFirstName("FirstUser");
        user.setLastName("LastUser");
        user.setPrimaryPhone("1231231234");
        user.setActive(false);

        when(mockAuthDAO.findUserById(userId)).thenReturn(user);
        when(mockAuthDAO.checkEmail(userDTO.getEmail())).thenReturn(true);
        when(mockAuthDAO.checkEcommonsId(userDTO.getEcommonsId())).thenReturn(true);

        Department department = new Department();
        department.setId(userDTO.getDepartment());
        department.setName("newDepartment");
        when(mockAuthDAO.findDepartmentById(userDTO.getDepartment())).thenReturn(department);

        Division division = new Division();
        division.setId(userDTO.getDivision());
        division.setName("newDivision");
        when(mockAuthDAO.findDivisionById(userDTO.getDivision())).thenReturn(division);

        FacultyRank facultyRank = new FacultyRank();
        facultyRank.setId(userDTO.getFacultyRank());
        facultyRank.setName("newFacultyRank");
        when(mockAuthDAO.findFacultyRankById(userDTO.getFacultyRank())).thenReturn(facultyRank);

        Credential credential = new Credential();
        credential.setId(userDTO.getCredential());
        credential.setName("newCredential");
        when(mockAuthDAO.findCredentialById(userDTO.getCredential())).thenReturn(credential);

        InstitutionRole institutionRole = new InstitutionRole();
        institutionRole.setType(instRoleType);
        institutionRole.setId(1);
        institutionRole.setName(instRoleType.name());
        when(mockStudyDAO.findInstitutionRoleByType(instRoleType)).thenReturn(institutionRole);

        Institution institution = new Institution("inst", "institution");
        institution.setId(userDTO.getInstitutionId());
        when(mockResourceDAO.findInstitutionById(userDTO.getInstitutionId())).thenReturn(institution);

        Role role = new Role();
        role.setId(userDTO.getRoleId());
        role.setName("newRole");
        when(mockStudyDAO.findRoleById(userDTO.getRoleId())).thenReturn(role);

        UserDTO result = authService.updateUser(userDTO, user, ipAddress);

        assertEquals("update user first name", userDTO.getFirstName(), user.getFirstName());
        assertEquals("update user middle name", userDTO.getMiddleName(), user.getMiddleName());
        assertEquals("update user last name", userDTO.getLastName(), user.getLastName());
        assertEquals("update user primary phone", userDTO.getPrimaryPhone(), user.getPrimaryPhone());
        assertEquals("update user secondary phone", userDTO.getSecondaryPhone(), user.getSecondaryPhone());
        assertEquals("update user email", userDTO.getEmail(), user.getEmail());
        assertEquals("update user notification email", userDTO.getNotificationEmail(), user.getNotificationEmail());
        assertEquals("update user pager", userDTO.getPager(), user.getPager());
        assertEquals("update user department", userDTO.getDepartment(), user.getDepartment().getId().intValue());
        assertEquals("update user division", userDTO.getDivision(), user.getDivision().getId().intValue());
        assertEquals("update user fax", userDTO.getFax(), user.getFax());
        assertEquals("update user active", userDTO.isActive(), user.getActive());
        assertEquals("update user faculty rank", userDTO.getFacultyRank(), user.getFacultyRank().getId().intValue());
        assertEquals("update user credential", userDTO.getCredential(), user.getCredential().getId().intValue());
        assertEquals("update user institution role", userDTO.getInstitutionRoleType(), user.getInstitutionRole().getType());
        assertEquals("update user role", userDTO.getRoleId(), user.getRole().getId().intValue());

        verify(mockAuditService, times(1)).logUserActivity(anyString(), any(User.class),
                any(User.class), anyString(), anyString(), anyString());
    }

    @Test
    public void testGetStudyMembers() throws Exception {
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

        when(mockAuthDAO.getAddStudyMemberList(filterString, sortBy, orderBy)).thenReturn(userDataResponseList);

        List<UserDataResponse> displayedUsers = authService.getStudyMembers(filterString, sortBy, orderBy);
        TestUtils.assertNonNullNonEmpty(displayedUsers);

        UserDataResponse userDataResponse1 = displayedUsers.get(0);
        assertEquals(userDataResponse1.getLastName(), user.getLastName());
        assertEquals(userDataResponse1.getFirstName(), user.getFirstName());
        assertEquals(userDataResponse1.getTitle(), user.getInstitutionRole().getName());
        assertEquals(userDataResponse1.getUserId(), user.getEcommonsId());
        assertEquals(userDataResponse1.getId(), user.getId());
    }
}
//TODO verify mockAuthDAO.updateEntity and mailHandler.send
//TODO testInactiveUser

