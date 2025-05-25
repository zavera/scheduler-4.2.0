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

import com.google.common.collect.Lists;
import edu.harvard.catalyst.scheduler.AbstractSpringWiredHibernateTest;
import edu.harvard.catalyst.scheduler.dto.SearchDTO;
import edu.harvard.catalyst.scheduler.dto.response.GetUsersResponse;
import edu.harvard.catalyst.scheduler.entity.*;
import edu.harvard.catalyst.scheduler.util.MiscUtil;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;

/**
 * Created by xavier on 5/2/7.
 */

public class AuthDAOTest extends AbstractSpringWiredHibernateTest {

    @Autowired
    private AuthDAO authDAO;

    private Division setUpDivision(String suffix) {
        Division division = new Division();
        division.setName("institution name " + suffix);
        save(division);
        return division;
    }

    private Department setUpDepartment(String suffix) {
        Department department = new Department();
        department.setName("department name " + suffix);
        save(department);
        return department;
    }

    private Credential setUpCredential(String suffix) {
        Credential credential = new Credential();
        credential.setName("credential name " + suffix);
        save(credential);
        return credential;
    }

    private FacultyRank setUpFacultyRank(String suffix) {
        FacultyRank facultyRank = new FacultyRank();
        facultyRank.setName("faculty rank " + suffix);
        save(facultyRank);
        return facultyRank;
    }

    private Institution setUpInstitution(String suffix) {
        Institution institution = new Institution();
        institution.setName("institution name " + suffix);
        institution.setLongName("institution long name " + suffix);
        save(institution);
        return institution;
    }

    private InstitutionRole setUpInstitutionRole(String suffix) {
        InstitutionRole institutionRole = new InstitutionRole();
        institutionRole.setName("institution name " + suffix);
        institutionRole.setType(InstitutionRoleType.ROLE_SCHEDULER);
        institutionRole.setDescription("institution role description " + suffix);
        save(institutionRole);
        return institutionRole;
    }

    private Role setUpRole(String suffix) {
        Role role = new Role();
        role.setName("role name " + suffix);
        role.setType(RoleType.ROLE_SCHEDULER);
        save(role);
        return role;
    }
    
    private User setUpUser(
            String mainSuffix,
            String lastName,
            String firstName,
            Division division,
            Department department,
            Credential credential,
            FacultyRank facultyRank,
            Institution institution,
            InstitutionRole institutionRole,
            Role role
    ) {
        User user = new User();
        user.setEcommonsId("aaa " + mainSuffix);
        user.setPassword("password " + mainSuffix);
        user.setFirstName(firstName);
        user.setMiddleName("middle name " + mainSuffix);
        user.setLastName(lastName);
        user.setPrimaryPhone("primary phone " + mainSuffix);
        user.setSecondaryPhone("secondary phone " + mainSuffix);
        user.setEmail("email " + mainSuffix);
        user.setFax("fax " + mainSuffix);
        user.setPager("pager " + mainSuffix);
        user.setDivision(division);
        user.setDepartment(department);
        user.setCredential(credential);
        user.setFacultyRank(facultyRank);
        user.setInstitution(institution);
        user.setInstitutionRole(institutionRole);
        user.setRole(role);
        user.setActive(true);
        user.setSalt("salt " + mainSuffix);
        user.setLastLoginTime(new Date());
        user.setAuthStatus(1);
        user.setPreviousLoginTime("");
        user.setSessionId("session id " + mainSuffix);
        user.setBookedVisits(null);
        save(user);
        return user;
    }


    @Test
    public void testGetUsers() {

        Division division1 = setUpDivision("1");
        Department department1 = setUpDepartment("1");
        Credential credential1 = setUpCredential("1");
        FacultyRank facultyRank1 = setUpFacultyRank("1");
        Institution institution1 = setUpInstitution("1");
        InstitutionRole institutionRole1 = setUpInstitutionRole("1");
        Role role1 = setUpRole("1");

        User user1 = setUpUser(
                "1",
                "last name 1",
                "first name 2",
                division1,
                department1,
                credential1,
                facultyRank1,
                institution1,
                institutionRole1,
                role1
        );

        User user2 = setUpUser(
                "2",
                "last name 2",
                "first name 3",
                division1,
                department1,
                credential1,
                facultyRank1,
                institution1,
                institutionRole1,
                role1
        );

        User user3 = setUpUser(
                "3",
                "last name 3",
                "first name 1",
                division1,
                department1,
                credential1,
                facultyRank1,
                institution1,
                institutionRole1,
                role1
        );

        // Test the page size

        List<SearchDTO.SearchItem> searchItems = new ArrayList<>();
        SearchDTO searchDTO = new SearchDTO();
        searchDTO.setSearchItems(searchItems);

        GetUsersResponse response = authDAO.getUsers(searchDTO, "u.lastName", "ASC", 1, 50);
        assertEquals(3, response.getTotal());
        assertEquals(3, response.getUsers().size());

        response = authDAO.getUsers(searchDTO, "u.lastName", "ASC", 1, 2);
        assertEquals(3, response.getTotal());
        assertEquals(2, response.getUsers().size());

        response = authDAO.getUsers(searchDTO, "u.lastName", "ASC", 2, 2);
        assertEquals(3, response.getTotal());
        assertEquals(1, response.getUsers().size());

        // test the ordering options

        response = authDAO.getUsers(searchDTO, "u.lastName", "ASC", 1, 50);
        List<GetUsersResponse.User1> returnedUsers = response.getUsers();
        assertEquals(3, returnedUsers.size());
        assertEquals(user1.getLastName(), returnedUsers.get(0).getLastName());
        assertEquals(user2.getLastName(), returnedUsers.get(1).getLastName());
        assertEquals(user3.getLastName(), returnedUsers.get(2).getLastName());

        response = authDAO.getUsers(searchDTO, "u.lastName", "DESC", 1, 50);
        returnedUsers = response.getUsers();
        assertEquals(3, returnedUsers.size());
        assertEquals(user3.getLastName(), returnedUsers.get(0).getLastName());
        assertEquals(user2.getLastName(), returnedUsers.get(1).getLastName());
        assertEquals(user1.getLastName(), returnedUsers.get(2).getLastName());

        response = authDAO.getUsers(searchDTO, "u.firstName", "ASC", 1, 50);
        returnedUsers = response.getUsers();
        assertEquals(3, returnedUsers.size());
        assertEquals(user3.getFirstName(), returnedUsers.get(0).getFirstName());
        assertEquals(user1.getFirstName(), returnedUsers.get(1).getFirstName());
        assertEquals(user2.getFirstName(), returnedUsers.get(2).getFirstName());

        response = authDAO.getUsers(searchDTO, "u.firstName", "DESC", 1, 50);
        returnedUsers = response.getUsers();
        assertEquals(3, returnedUsers.size());
        assertEquals(user2.getFirstName(), returnedUsers.get(0).getFirstName());
        assertEquals(user1.getFirstName(), returnedUsers.get(1).getFirstName());
        assertEquals(user3.getFirstName(), returnedUsers.get(2).getFirstName());

        // test the filtering options

        user1.setLastName("last name 2");
        save(user1);

        SearchDTO.SearchItem filterByLastName = new SearchDTO.SearchItem();
        filterByLastName.setKey("u.lastName");
        filterByLastName.setValue("2");
        filterByLastName.setSearchType(SearchDTO.SearchType.ANY);
        searchItems.add(filterByLastName);
        response = authDAO.getUsers(searchDTO, "u.firstName", "ASC", 1, 50);
        returnedUsers = response.getUsers();
        assertEquals(2, returnedUsers.size());
        assertEquals(user2.getLastName(), returnedUsers.get(0).getLastName());
        assertEquals(user1.getLastName(), returnedUsers.get(1).getLastName());

        SearchDTO.SearchItem filterByFirstName = new SearchDTO.SearchItem();
        filterByFirstName.setKey("u.firstName");
        filterByFirstName.setValue("2");
        filterByFirstName.setSearchType(SearchDTO.SearchType.ANY);
        searchItems.add(filterByFirstName);
        response = authDAO.getUsers(searchDTO, "u.ecommonsId", "ASC", 1, 50);
        returnedUsers = response.getUsers();
        assertEquals(1, returnedUsers.size());
        assertEquals(user1.getLastName(), returnedUsers.get(0).getLastName());

    }

    /**
     * This test only verifies the correctness of the generated HQL string
     */
    @Test
    public void testGetStudyMembersHqlQueryString() {

        String sortBy = "s.name";
        String orderBy = "DESC";

        // filtered columns:
        //        u.lastName, u.firstName, u.primaryPhone, u.ecommonsId
        List<SearchDTO.SearchItem> searchItems = Lists.newArrayList(
                new SearchDTO.SearchItem(
                        SearchDTO.SearchType.ANY,
                        "u.lastName",
                        "partial user last name",
                        null
                ),
                new SearchDTO.SearchItem(
                        SearchDTO.SearchType.ANY,
                        "u.firstName",
                        "partial user first name",
                        null
                ),
                MiscUtil.preparePhoneNumberSearchItems(new SearchDTO.SearchItem(
                        SearchDTO.SearchType.ANY,
                        "u.primaryPhone",
                        "(_55) 1_3-____",
                        null
                )),
                new SearchDTO.SearchItem(
                        SearchDTO.SearchType.ANY,
                        "u.ecommonsId",
                        "partial e-commons ID",
                        null
                )
        );

        // IMPORTANT: using the AuthDAO wired-in by spring does not work
        // Instead create a mock, and stub every method that gets called by the method
        // under test
        AuthDAO authDao = new AuthDAO();
        AuthDAO authDaoSpy = Mockito.spy(authDao);

        org.hibernate.query.Query mockQuery = mock(org.hibernate.query.Query.class);

        doReturn(session()).when(authDaoSpy).session();
        doReturn(mockQuery).when(authDaoSpy).newQuery(any());
        doReturn(mockQuery).when(mockQuery).setParameter(anyString(), any());
        doReturn(mockQuery).when(mockQuery).setFirstResult(anyInt());
        doReturn(mockQuery).when(mockQuery).setMaxResults(anyInt());
        doReturn(0L).when(mockQuery).uniqueResult();

        SearchDTO searchDTO = new SearchDTO();
        searchDTO.setSearchItems(searchItems);

        authDaoSpy.getUsers(searchDTO, sortBy, orderBy, 1, 10);

        String expectedListHql = "select u from User u " +
                " WHERE u.lastName LIKE :u_lastName " +
                "AND u.firstName LIKE :u_firstName " +
                "AND (u.primaryPhone LIKE :u_primaryPhone OR u.primaryPhone LIKE :u_primaryPhone_) " +
                "AND u.ecommonsId LIKE :u_ecommonsId order by s.name DESC";

        String expectedCountHql = "select count(u) from User u " +
                " WHERE u.lastName LIKE :u_lastName " +
                "AND u.firstName LIKE :u_firstName " +
                "AND (u.primaryPhone LIKE :u_primaryPhone OR u.primaryPhone LIKE :u_primaryPhone_) " +
                "AND u.ecommonsId LIKE :u_ecommonsId";

        Mockito.verify(authDaoSpy, times(1)).newQuery(expectedListHql);
        Mockito.verify(authDaoSpy, times(1)).newQuery(expectedCountHql);

        assertEquals("u_lastName", searchItems.get(0).getQueryParamName());
        assertEquals("u_firstName", searchItems.get(1).getQueryParamName());
        assertEquals(null, searchItems.get(2).getQueryParamName());
        List<SearchDTO.SearchItem> primaryPhoneOrSearchItems = searchItems.get(2).getOrSearchItems();
        assertEquals(2, primaryPhoneOrSearchItems.size());
        assertEquals("u_primaryPhone", primaryPhoneOrSearchItems.get(0).getQueryParamName());
        assertEquals("u_primaryPhone_", primaryPhoneOrSearchItems.get(1).getQueryParamName());

        assertEquals("u_ecommonsId", searchItems.get(3).getQueryParamName());

        assertEquals("%partial user last name%", searchItems.get(0).getQueryParamValue());
        assertEquals("%partial user first name%", searchItems.get(1).getQueryParamValue());
        assertEquals(null, searchItems.get(2).getQueryParamValue());
        assertEquals("%_55-1_3-____%", primaryPhoneOrSearchItems.get(0).getQueryParamValue());
        assertEquals("%(_55) 1_3-____%", primaryPhoneOrSearchItems.get(1).getQueryParamValue());
        assertEquals("%partial e-commons ID%", searchItems.get(3).getQueryParamValue());

    }

}
