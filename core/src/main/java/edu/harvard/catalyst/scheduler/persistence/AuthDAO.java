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
package edu.harvard.catalyst.scheduler.persistence;

import edu.harvard.catalyst.scheduler.dto.SearchDTO;
import edu.harvard.catalyst.scheduler.dto.response.GetUsersResponse;
import edu.harvard.catalyst.scheduler.dto.response.UserDataResponse;
import edu.harvard.catalyst.scheduler.dto.response.UserDetailResponse;
import edu.harvard.catalyst.scheduler.entity.*;
import edu.harvard.catalyst.scheduler.util.OneWayPasswordEncoder;
import org.apache.log4j.Logger;
import org.hibernate.query.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import static edu.harvard.catalyst.scheduler.util.MiscUtil.isNonNullNonEmpty;
import static edu.harvard.catalyst.scheduler.util.MiscUtil.negativeIsInfinity;

@Repository
@Transactional
public class AuthDAO extends SiteDAO {

    private static final Logger LOG = Logger.getLogger(AuthDAO.class);

    public UserSession createUserSession(final String httpSession, final String ipAddress, final User user) {
        final UserSession userSession = new UserSession();
        userSession.setUser(user);
        userSession.setSessionId(httpSession);
        userSession.setIpAddress(ipAddress);
        userSession.setLastAccessTime(new Date());
        
        this.createEntity(userSession);
        
        return userSession;
    }

    public User authenticate(final String username, final String password, final String httpSession) {
        final User userFromEcommonsId = findUserByEcommonsId(username);
        
        if (userFromEcommonsId == null) {
            return null;
        }
        
        final String encodedPassword = OneWayPasswordEncoder.getInstance().encode(password, userFromEcommonsId.getSalt());

        final CriteriaQueryHelper criteriaHelper = new CriteriaQueryHelper(session(), User.class);
        
        criteriaHelper.whereEquals("ecommonsId", username);
        criteriaHelper.whereEquals("password", encodedPassword);
        
        final User user = (User) criteriaHelper.getQuery().uniqueResult();
        
        if (user == null) {
            return null;
        }
        
        final DateFormat df = new SimpleDateFormat("EEE, MMM d, yyyy h:mm a");
        
        user.setPreviousLoginTime(user.getLastLoginTime() != null ? df.format(user.getLastLoginTime().getTime()) : "");
        user.setLastLoginTime(new Date());
        user.setSessionId(httpSession);
        user.setBookedVisits(null);
        
        this.updateEntity(user);
        
        return user;
    }

    public User findUserById(final int id) {
        return this.findById(User.class, id);
    }

    public UserDetailResponse getUserData(final int id) {
        final User user = this.findById(User.class, id);

        return new UserDetailResponse(user);
    }

    public User findUserByEcommonsId(final String id) {
        final CriteriaQueryHelper criteriaHelper = new CriteriaQueryHelper(session(), User.class);
        criteriaHelper.whereEquals("ecommonsId", id);
        
        return (User) criteriaHelper.getQuery().uniqueResult();
    }

    public User findUserByEmail(final String email) {
        final CriteriaQueryHelper criteriaHelper = new CriteriaQueryHelper(session(), User.class);
        criteriaHelper.whereEquals("email", email);
        
        return (User) criteriaHelper.getQuery().uniqueResult();
    }

    public UserSession findUserSessionBySessionId(final String sessionId) {
        final CriteriaQueryHelper criteriaHelper = new CriteriaQueryHelper(session(), UserSession.class);
        criteriaHelper.whereEquals("sessionId", sessionId);
        
        return (UserSession) criteriaHelper.getQuery().uniqueResult();
    }

    /** is there a user with this ecommons id? */
    public boolean checkEcommonsId(final String ecommonsId) {
        LOG.info("testing ecommonsId: " + ecommonsId);
        
        return checkForUserByField("ecommonsId", ecommonsId);
    }

    public boolean checkEmail(final String email) {
        LOG.info("testing email: " + email);
        
        return checkForUserByField("email", email);
    }

    private boolean checkForUserByField(final String columnName, final String columnValue) {
        final CriteriaQueryHelper criteriaHelper = new CriteriaQueryHelper(session(), User.class);
        criteriaHelper.whereEquals(columnName, columnValue);
        
        final User user = (User) criteriaHelper.getQuery().uniqueResult();
        
        return user == null;
    }

    public List<Credential> getCredentials() {
        return this.findAll(Credential.class);
    }

    public List<Division> getDivisions() {
        return this.findAll(Division.class);
    }

    public List<Department> getDepartments() {
        return this.findAll(Department.class);
    }

    public List<FacultyRank> getFacultyRanks() {
        return this.findAll(FacultyRank.class);
    }

    public List<Role> getRoles() {
        return this.findAll(Role.class);
    }

    public Credential findCredentialById(final int id) {
        return this.findById(Credential.class, id);
    }

    public Department findDepartmentById(final int id) {
        return this.findById(Department.class, id);
    }

    public Division findDivisionById(final int id) {
        return this.findById(Division.class, id);
    }

    public FacultyRank findFacultyRankById(final int id) {
        return this.findById(FacultyRank.class, id);
    }

    public List<FundingSource> getFundingSources() {
        return this.findAll(FundingSource.class);
    }

    public List<CentersAndInstitutions> getCentersAndInstitutions() {
        return this.findAll(CentersAndInstitutions.class);
    }

    @SuppressWarnings("unchecked")
    private List<User> findUsersByInstitutionRole(final InstitutionRoleType type) {
        final String hql = "select u from User u where u.institutionRole = (select r from InstitutionRole r where r.type = :type)";
        final Query query = newQuery(hql).setParameter("type", type);
        return query.list();
    }

    @SuppressWarnings("unchecked")
    public List<Role> getRolesExceptFinalApprover() {
        final String hql = "select r from Role r where r.type != :roleType";
        final Query query = newQuery(hql).setParameter("roleType", RoleType.ROLE_FINAL_APPROVER);
        return query.list();
    }

    public List<User> findSuperAdminByInstitutionRole() {
        return findUsersByInstitutionRole(InstitutionRoleType.ROLE_SUPER_ADMIN);
    }

    @SuppressWarnings("unchecked")
    private List<User> findUserByRole(final RoleType roleType){
        final String hql = "select u from User u where u.role = (select r from Role r where r.type = :roleType)";
        final Query query = newQuery(hql).setParameter("roleType", roleType);
        return query.list();
    }

    public List<User> findFinalApproverByRole() {
        return findUserByRole(RoleType.ROLE_FINAL_APPROVER);
    }

    public List<User> findAdminDirectorUserByRole() {
        return findUserByRole(RoleType.ROLE_ADMINISTRATIVE_DIRECTOR);
    }

    public List<User> findSchedulerUserByInstitutionRole() {
        return findUsersByInstitutionRole(InstitutionRoleType.ROLE_SCHEDULER);
    }

    public List<User> findNurseManagerUserByRole() {
        return findUserByRole(RoleType.ROLE_NURSE_MANAGER_DIRECTOR);
    }

    public List<User> findNutritionManagerUserByRole() {
        return findUserByRole(RoleType.ROLE_NUTRITION_MANAGER);
    }
    public List<User> findCRCAdminByRole(){
        return findUserByRole(RoleType.ROLE_CRC_ADMINISTRATION);
    }

    // used by study_member.html page for listing users to add to a study
    public List<UserDataResponse> getAddStudyMemberList(final String filterString, final String sortBy, final String orderBy) {
        final Query mainQuery = getUserListQueryUsingCustomFilterString(filterString, sortBy, orderBy);

        final Long total = (long) mainQuery.list().size();

        @SuppressWarnings("unchecked")
        final List<User> resultRows = mainQuery.list();

        return UserDataResponse.getUserData(resultRows, total);
    }

    public GetUsersResponse getUsers(final SearchDTO searchDTO,
                                     final String sortBy,
                                     final String orderBy,
                                     final int page,
                                     final int maxResults) {

        final String coreHql = "from User u ";

        String totalSearchClause = "";
        if(searchDTO != null) {
            totalSearchClause = searchDTO.toSearchClause(" AND ", " WHERE ");
        }

        final String listHql = "select u " + coreHql + totalSearchClause + " order by " + sortBy + " " + orderBy;
        final Query listQuery = newQuery(listHql);

        final String countHql = "select count(u) " + coreHql + totalSearchClause;
        final Query countQuery = newQuery(countHql);

        if(searchDTO != null) {
            searchDTO.assignQueryParameterValues(listQuery);
            searchDTO.assignQueryParameterValues(countQuery);
        }

        final long total = (Long) countQuery.uniqueResult();

        setPagination(page, maxResults, listQuery);

        @SuppressWarnings("unchecked")
        final List<User> resultRows = listQuery.list();

        return GetUsersResponse.createGetUsersResponse(resultRows, total);
    }

    void setPagination(final int page, int maxResults, final Query mainQuery) {
        maxResults = negativeIsInfinity(maxResults);

        final int offset = (page - 1) * maxResults;
        mainQuery.setFirstResult(offset);
        mainQuery.setMaxResults(maxResults);
    }

    // used by study_member.html page for listing users to add to a study
    Query getUserListQueryUsingCustomFilterString(final String filterString, final String sortBy, final String orderBy) {
        final String simpleHql = "select u from User u ";

        final boolean filterStringIsPresent = isNonNullNonEmpty(filterString);

        final String whereClause = !filterStringIsPresent ? " " : "where (lower(u.lastName) LIKE :filterString or lower(u.ecommonsId) LIKE :filterString) ";

        final String completeHql = simpleHql + whereClause + " order by " + sortBy + " " + orderBy;
        
        final Query mainQuery = newQuery(completeHql);

        if (filterStringIsPresent) {
            mainQuery.setParameter("filterString", "%" + filterString.toLowerCase() + "%");
        }

        return mainQuery;
    }

}
