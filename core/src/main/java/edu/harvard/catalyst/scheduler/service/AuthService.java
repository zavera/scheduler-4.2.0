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

import com.Ostermiller.util.RandPass;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
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
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static edu.harvard.catalyst.hccrc.core.util.ListUtils.flatten;
import static edu.harvard.catalyst.scheduler.util.MiscUtil.isNonNullNonEmpty;

@Component
public class AuthService implements ServiceHelpers {

    private final MailHandler mailHandler;
    private final AuthDAO authDAO;
    private final StudyDAO studyDAO;
    private final ResourceDAO resourceDAO;
    private final AuditService auditService;

    private static final Logger LOGGER = Logger.getLogger(AuthService.class);
    private static final int MIN_PASSWORD_LENGTH = 8;

    static final String NEW_USER_MESSAGE_START =
            "\n<html><head><title>%1s</title><style></style>\n</head>\n<body><p>Welcome to the CRC Scheduler.</p>\n<p>" +
                    "<strong>%2s %3s,<strong></p>\n";

    static final String NEW_USER_MESSAGE_OPTIONAL_MIDDLE =
                    "<p>You will have access to the system once approved by the Super Admin. Please wait for the final confirmation.</p>\n ";

    static final String NEW_USER_MESSAGE_END =
                    "<p> You can go to %4s and click on the \"forgot password\" link to create a new random password.</p>\n " +
                    "<p>This will be emailed to you and then you may change your password once you have logged in.</p>\n</body>\n</html>\n";


    static final String MAIL_ADMIN_MESSAGE =
            "<html><head><title></title><style></style></head><body><p><strong>%1s %2s has registered.<strong></p>\n " +
                    "<p> You can go to %3s and activate the user.</p>\n<p>The user will be notified of their access " +
                    "to the system once the account is activated.</p>\n</body>\n</html>\n";

    @Autowired
    public AuthService(final AuthDAO authDAO, final StudyDAO studyDAO, final ResourceDAO resourceDAO, final AuditService auditService, final MailHandler mailHandler) {
        this.authDAO = authDAO;
        this.studyDAO = studyDAO;
        this.resourceDAO = resourceDAO;
        this.auditService = auditService;
        this.mailHandler = mailHandler;
    }

    // Don't use - Needed for spring security cglib proxying
    AuthService() {
        this(null, null, null, null, null);
    }

    SchedulerUserDetails authenticateUser(final String username, final String password, final String httpSession, final String ipAddress) {
        final User user = authDAO.authenticate(username, password, httpSession);

        if (user == null) {
            // spring framework requires this exception
            throw new BadCredentialsException("Invalid Username or Password");
        }

        if (!user.getActive()) {
            throw new DisabledException("User inactive");
        } else {
            user.setAuthStatus(User.STATUS_AUTHORIZED);
            final UserSession userSession = authDAO.createUserSession(httpSession, ipAddress, user);
            return new SchedulerUserDetails(user, userSession);
        }
    }

    String generatePassword() {
        final String alphabet = "$%!@#^&?";
        final int n = alphabet.length();
        final Random r = new Random();
        return new RandPass().getPass(8) + alphabet.charAt(r.nextInt(n));
    }

    // @Transactional
    public boolean doPasswordReset(final PasswordResetDTO passwordResetDTO) {
        final User user = authDAO.findUserByEmail(passwordResetDTO.getEmail());

        if (user != null) {
            final String newPassword = generatePassword();
            final String newSalt = UUID.randomUUID().toString();
            final String encodedPassword = OneWayPasswordEncoder.getInstance().encode(newPassword, newSalt);
            user.setPassword(encodedPassword);
            user.setSalt(newSalt);
            authDAO.updateEntity(user);

            final StringBuilder contentSb = new StringBuilder();
            final String subject = "Password Reset for " + user.getFirstName() + " " + user.getLastName();
            contentSb.append("\n<html><head><title>").append(subject).append("</title><style></style>\n</head>\n<body>");
            contentSb.append("<p><strong>").append(user.getFirstName()).append(user.getLastName()).append("<strong></p>\n");
            contentSb.append("<p>Your password has been reset to: ").append(newPassword).append("</p>\n");
            contentSb.append("</body>\n").append("</html>\n");

            contentSb.append(subject).append("\n").append(user.getFirstName()).append(" ").append(user.getLastName()).append("\nYour password has been reset to:").append(newPassword);

            mailHandler.sendMandatoryEmails(new MailMessageBuilder().to(user.getEmail()).subject(subject).text(contentSb.toString()).build());
        }

        return user != null;
    }

    // @Transactional
    public UserDTO doRegisterUser(final UserDTO dto, String remoteHost) {
        final User user = new User();

        if (!testPassword(dto.getPassword())) {
            dto.setResult(false);
            dto.setErrorMsg(Statics.INSUFFICIENT_PASSWORD);
            return dto;
        }

        setupUser(dto, user, InstitutionRoleType.ROLE_STUDY_STAFF);

        if (!dto.isResult()){
            return dto;
        }

        auditService.logUserActivity(remoteHost, user, user, Statics.AUDIT_USER_CREATE, null, null);

        return dto;
    }


    public UserDTO sendRegistrationEmail(UserDTO dto, String contextPath, String serverName, int serverPort) {

        final User user = authDAO.findById(User.class, dto.getId());

        final String url = makeUrl(contextPath, serverName, serverPort);
        final List<User> superAdmins = authDAO.findSuperAdminByInstitutionRole();

        final String superAdminSubject = "New User Registered: " + user.getFirstName() + " " + user.getLastName();
        final String subject = makeEmailUserSubject(user);

        superAdmins.stream().map(toMessage(superAdminSubject,  makeNewUserAdminMessage(user, url))).forEach(mailHandler::sendMandatoryEmails);

        final SimpleMailMessage message = toMessage(subject, makeNewUserMessage(subject, user, url, true)).apply(user);

        mailHandler.sendMandatoryEmails(message);

        return dto;
    }




    String makeUrl(String contextPath, String serverName, int serverPort) {
        return "https://" + serverName + ":" + serverPort + contextPath;
    }

    String makeEmailUserSubject(User user) {
        return "Welcome " + user.getFirstName() + " " + user.getLastName();
    }

    private void setupUser(UserDTO dto, User user, InstitutionRoleType institutionRoleType) {
        if (isBadEmailOrEcommonsId(dto)) {
            return;
        }

        final String salt = UUID.randomUUID().toString();
        user.setSalt(salt);

        final String encodedPassword = OneWayPasswordEncoder.getInstance().encode(dto.getPassword(), salt);
        user.setPassword(encodedPassword);

        final InstitutionRoleType roleType =  null != institutionRoleType ? institutionRoleType: dto.getInstitutionRoleType();

        setSomeUserProperties(dto, user, roleType);

        authDAO.createEntity(user);

        if (user.getId() != null) {
            dto.setId(user.getId());
        }
        dto.setResult(true);
    }

    String makeNewUserMessage(String subject, User user, String url,
                              boolean addSuperUserSentence){
        StringBuilder sb = new StringBuilder();
        sb.append(NEW_USER_MESSAGE_START);

        if (addSuperUserSentence){
            sb.append(NEW_USER_MESSAGE_OPTIONAL_MIDDLE);
        }

        sb.append(NEW_USER_MESSAGE_END);

        return String.format(sb.toString(), subject, user.getFirstName(), user.getLastName(), url);
    }

    String makeNewUserAdminMessage(User user, String url){
        return String.format(MAIL_ADMIN_MESSAGE, user.getFirstName(), user.getLastName(), url);
    }

    private Function<User, SimpleMailMessage> toMessage(final String subject, final String body) {
        return u -> new MailMessageBuilder().to(u.getEmail()).subject(subject).text(body).build();
    }

    // @Transactional
    public GetUsersResponse getUsers(final SearchDTO searchDTO, final String sortBy, final String orderBy, final int page, final int maxResults, final String ipAddress, final User user) {

        auditService.logViewActivity(ipAddress, user, "All Users Viewed.");

        if (searchDTO != null) {
            List<SearchDTO.SearchItem> searchItems = searchDTO.getSearchItems();
            if (searchItems != null && searchItems.size() > 0) {
                searchItems = searchItems.stream().map(si -> {
                    if (si.getKey().equals("u.primaryPhone")) {
                        MiscUtil.preparePhoneNumberSearchItems(si);
                    }
                    return si;

                }).collect(Collectors.toList());
                searchDTO.setSearchItems(searchItems);
            }
        }

        final GetUsersResponse users = authDAO.getUsers(searchDTO, sortBy, orderBy, page, maxResults);
        return users;
    }

    BooleanResultDTO checkEcommonsId(final LoginDTO loginDTO) {
        final BooleanResultDTO booleanResultDTO = new BooleanResultDTO();
        booleanResultDTO.setResult(authDAO.checkEcommonsId(loginDTO.getEcommonsId()));
        return booleanResultDTO;
    }

    BooleanResultDTO checkEmail(final LoginDTO loginDTO) {
        final BooleanResultDTO booleanResultDTO = new BooleanResultDTO();
        booleanResultDTO.setResult(authDAO.checkEmail(loginDTO.getEmail()));
        return booleanResultDTO;
    }

    // @Transactional
    public UserDTO createUser(final UserDTO dto, final User creatingUser, final String contextPath,
                              final String remoteHost, final String serverName, final int serverPort) {
        final User user = new User();

        if (dto.isGenerateNewPassword()) {
            dto.setPassword(generatePassword());
        } else if (!testPassword(dto.getPassword())) {
            dto.setResult(false);
            dto.setErrorMsg(Statics.INSUFFICIENT_PASSWORD);
            return dto;
        }

        setupUser(dto, user, null);

        if (!dto.isResult()){
            return dto;
        }

        final String url = makeUrl(contextPath, serverName, serverPort);
        final String subject = makeEmailUserSubject(user);

        String emailContent = makeNewUserMessage(subject, user, url, false);

        mailHandler.sendOptionalEmails(
                new MailMessageBuilder().to(user.getEmail()).subject(subject).text(emailContent).build());

        auditService.logUserActivity(remoteHost, user, creatingUser, Statics.AUDIT_USER_CREATE, null, null);

        return dto;
    }

    // @Transactional
    public UserDTO updatePassword(final UserDTO dto, final User usr) {
        final User user = authDAO.findUserById(usr.getId());

        if (!testPassword(dto.getPassword())) {
            dto.setErrorMsg(Statics.INSUFFICIENT_PASSWORD);
            dto.setResult(false);
            return dto;
        }

        final String encodedPassword = encodePassword(dto, user);
        dto.setPassword(encodedPassword);
        user.setPassword(dto.getPassword());
        authDAO.updateEntity(user);
        dto.setResult(true);

        return dto;
    }

    boolean isValidUser(User user, UserDTO dto)
    {
        boolean result = true;
        if (!compareEcommonsIds(dto, user) && !checkEcommonsId(dto)) {
            dto.setResult(false);
            dto.setErrorMsg(Statics.ECOMMONS_ID_EXISTS);
            result = false;
        }
        else if (!compareEmails(dto, user) && !checkEmail(dto)) {
            dto.setResult(false);
            dto.setErrorMsg(Statics.EMAIL_EXISTS);
            result = false;
        }

        return result;
    }

    boolean processPassword(User user, UserDTO dto)
    {
        // only process password if a new one is submitted
        if (hasPassword(dto)) {
            if (!passwordsMatch(dto, user)) {
                if (!testPassword(dto.getPassword())) {
                    dto.setErrorMsg(Statics.INSUFFICIENT_PASSWORD);
                    dto.setResult(false);
                    return false;
                }

                dto.setPassword(encodePassword(dto, user));
            }

            user.setPassword(dto.getPassword());
        }

        return true;
    }

    // @Transactional
    public UserDTO updateUser(final UserDTO dto, final User creatingUser, final String ipAddress) {
        final User user = authDAO.findUserById(dto.getId());
        final InstitutionRoleType institutionRoleType = dto.getInstitutionRoleType();

        if(!isValidUser(user, dto))
        {
            return dto;
        }

        if(!processPassword(user, dto))
        {
            return dto;
        }

        logUpdateUserData(dto, creatingUser, ipAddress, user, institutionRoleType);

        setSomeUserProperties(dto, user, institutionRoleType);

        authDAO.updateEntity(user);

        dto.setResult(true);

        return dto;
    }

    private boolean checkEmail(final UserDTO dto) {
        return authDAO.checkEmail(dto.getEmail());
    }

    private boolean compareEmails(final UserDTO dto, final User user) {
        return dto.getEmail().equals(user.getEmail());
    }

    private String encodePassword(final UserDTO dto, final User user) {
        return OneWayPasswordEncoder.getInstance().encode(dto.getPassword(), user.getSalt());
    }

    private boolean checkEcommonsId(final UserDTO dto) {
        return authDAO.checkEcommonsId(dto.getEcommonsId());
    }

    boolean compareEcommonsIds(final UserDTO dto, final User user) {
        return dto.getEcommonsId().equals(user.getEcommonsId());
    }

    boolean passwordsMatch(final UserDTO dto, final User user) {
        return user.getPassword().equals(dto.getPassword());
    }

    static boolean hasPassword(final UserDTO dto) {
        final String password = dto.getPassword();
        
        return isNonNullNonEmpty(password);
    }

    private void logUpdateUserData(final UserDTO dto, final User creatingUser, final String ipAddress, final User user, final InstitutionRoleType institutionRoleType) {

        final String institutionRole = studyDAO.findInstitutionRoleByType(institutionRoleType).getName();

        final String role = lookupFieldById(dto.getRoleId(), studyDAO::findRoleById);

        final String institution = lookupFieldById(dto.getInstitutionId(), resourceDAO::findInstitutionById);

        @SuppressWarnings("unchecked")
        final List<Optional<String>> previousDataComponents = Lists.newArrayList(
                makeFieldString("First Name", dto.getFirstName(), user.getFirstName()),
                makeFieldString("Middle Name", dto.getMiddleName(), user.getMiddleName()),
                makeFieldString("Last Name", dto.getLastName(), user.getLastName()),
                makeFieldString("Ecommons Id", dto.getEcommonsId(), user.getEcommonsId()),
                makeFieldString("Primary Phone", dto.getPrimaryPhone(), user.getPrimaryPhone()),

                makeFieldString("Institution Role", institutionRole, user.getInstitutionRole(), institutionRoleType, InstitutionRole::getType),
                makeFieldString("Role", role, user.getRole(), dto.getRoleId(), Role::getType),
                makeFieldString("Institution", institution, user.getInstitution(), dto.getInstitutionId(), Institution::getId),

                makeFieldString("Notification Email", dto.getNotificationEmail(), user.getNotificationEmail()));

        @SuppressWarnings("unchecked")
        final List<Optional<String>> otherPreviousDataComponents = Lists.newArrayList(
                makeFieldString("Secondary Phone", dto.getSecondaryPhone(), user.getSecondaryPhone()),
                makeFieldString("Email", dto.getEmail(), user.getEmail()),
                makeFieldString("Fax", dto.getFax(), user.getFax()),
                makeFieldString("Pager", dto.getPager(), user.getPager()),

                makeFieldString("Division", dto.getDivision(), user.getDivision()),
                makeFieldString("Department", dto.getDepartment(), user.getDepartment()),
                makeFieldString("Credential", dto.getCredential(), user.getCredential()),
                makeFieldString("Faculty Rank", dto.getFacultyRank(), user.getFacultyRank()));

        final Joiner joiner = Joiner.on("");

        final String previousData = joiner.join(flatten(previousDataComponents));

        final String otherPreviousData = joiner.join(flatten(otherPreviousDataComponents));

        auditService.logUserActivity(ipAddress, user, creatingUser, Statics.AUDIT_USER_UPDATE, previousData, otherPreviousData);
    }

    UserDTO logViewUser(final UserDTO dto, final User creatingUser, final String ipAddress) {
        final User user = authDAO.findUserById(dto.getId());
        auditService.logUserActivity(ipAddress, user, creatingUser, Statics.AUDIT_USER_VIEW, null, null);
        return dto;
    }

    boolean testPassword(final String password) {

        if (password.length() < MIN_PASSWORD_LENGTH) {
            LOGGER.info("Submitted passwords is not at least eight characters long");
            return false;
        }
        final Pattern lowerCasePattern = Pattern.compile("[a-z]+");
        final Matcher lowerCaseMatcher = lowerCasePattern.matcher(password);

        final Pattern upperCasePattern = Pattern.compile("[A-Z]+");
        final Matcher upperCaseMatcher = upperCasePattern.matcher(password);

        if (!lowerCaseMatcher.find() || !upperCaseMatcher.find()) {
            LOGGER.info("Sumitted passwords does not include at least one uppercase and one lowercase letter");
            return false;
        }

        final Pattern numericPattern = Pattern.compile("\\d+");
        final Matcher numericMatcher = numericPattern.matcher(password);

        final Pattern punctuationPattern = Pattern.compile("\\p{Punct}+");
        final Matcher punctuationMatcher = punctuationPattern.matcher(password);

        if (!numericMatcher.find() || !punctuationMatcher.find()) {
            LOGGER.info("Submitted passwords does not include at least one numeric character and one punctuation character");
            return false;
        }
        return true;
    }

    // @Transactional
    public BooleanResultDTO changeUserStatus(final int userId, final User creatingUser, final String contextPath, final String remoteHost, final String serverName, final int serverPort) {
        final String url = makeUrl(contextPath, serverName, serverPort);

        final BooleanResultDTO booleanResultDTO = new BooleanResultDTO();
        final User users = authDAO.findUserById(userId);
        if (users.getActive()) {
            users.setActive(Boolean.FALSE);
            authDAO.updateEntity(users);
            auditService.logUserActivity(remoteHost, users, creatingUser, Statics.AUDIT_USER_DEACTIVATE, null, null);
        } else {
            users.setActive(Boolean.TRUE);
            authDAO.updateEntity(users);
            auditService.logUserActivity(remoteHost, users, creatingUser, Statics.AUDIT_USER_ACTIVATE, null, null);
            sendUserActivationEmail(url, users);
        }
        booleanResultDTO.setResult(true);
        return booleanResultDTO;
    }

    void sendUserActivationEmail(final String url, final User users) {
        final StringBuffer contentSB = new StringBuffer();
        final String subject = "Welcome to the CRC Scheduler,  " + users.getFirstName() + " " + users.getLastName() + " !";
        contentSB.append("\n<html>" + "<head>" + "<title>" + subject + "</title>" + "<style>" + "</style>\n" + "</head>\n" + "<body>");
        contentSB.append("\n" + "<p>Welcome to the CRC Scheduler,</p> " + users.getFirstName() + " " + users.getLastName() + "! \n" + "<p>Your account has been activated and you can now access the system. </p>\n" + "<p> You can go to " + url + " and login as " + users.getEcommonsId() + " with the password you entered in the Registration form. " + " If you ever forget your password, you can click on the \"Forgot My Password\" link and submit your email address. </p>\n" + "<p>A new password will then be emailed to you. Once you are logged in, you can always reset your password on the Management page. </p>\n" + " Thanks!</body>\n" + "</html>\n");
        mailHandler.sendOptionalEmails(new MailMessageBuilder().to(users.getEmail()).subject(subject).text(contentSB.toString()).build());
    }

    // @Transactional
    public List<Role> getRoles() {
        return authDAO.getRolesExceptFinalApprover();
    }

    boolean isBadEmailOrEcommonsId(final UserDTO dto) {
        if (!checkEcommonsId(dto)) {
            dto.setResult(false);
            dto.setErrorMsg("Ecommons ID already in system");
            return true;
        }

        if (!checkEmail(dto)) {
            dto.setResult(false);
            dto.setErrorMsg("Email already in system");
            return true;
        }

        return false;
    }

    void setSomeUserProperties(final UserDTO dto, final User user, final InstitutionRoleType institutionRoleType) {
        user.setEcommonsId(dto.getEcommonsId());
        user.setFirstName(dto.getFirstName());
        user.setMiddleName(dto.getMiddleName());
        user.setLastName(dto.getLastName());
        user.setPrimaryPhone(dto.getPrimaryPhone());
        user.setSecondaryPhone(dto.getSecondaryPhone());
        user.setEmail(dto.getEmail());
        user.setNotificationEmail(dto.getNotificationEmail());
        user.setFax(dto.getFax());
        user.setPager(dto.getPager());
        user.setDivision(authDAO.findDivisionById(dto.getDivision()));
        user.setDepartment(authDAO.findDepartmentById(dto.getDepartment()));
        user.setInstitutionRole(studyDAO.findInstitutionRoleByType(institutionRoleType));
        user.setRole(studyDAO.findRoleById(dto.getRoleId()));
        user.setInstitution(resourceDAO.findInstitutionById(dto.getInstitutionId()));
        user.setCredential(authDAO.findCredentialById(dto.getCredential()));
        user.setFacultyRank(authDAO.findFacultyRankById(dto.getFacultyRank()));
        user.setActive(dto.isActive());
    }

    // used by study_member.html page for listing users to add to a study
    // @Transactional
    public List<UserDataResponse> getStudyMembers(final String filterString, final String sortBy, final String orderBy) {
        return authDAO.getAddStudyMemberList(filterString, sortBy, orderBy);
    }

    // @Transactional
    public UserDetailResponse getUserData(final int userId) {
        return authDAO.getUserData(userId);
    }
}
