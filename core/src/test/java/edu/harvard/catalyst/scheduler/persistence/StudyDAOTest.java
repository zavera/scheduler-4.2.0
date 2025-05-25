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
import edu.harvard.catalyst.scheduler.dto.response.UserDataResponse;
import edu.harvard.catalyst.scheduler.dto.response.VisitApprovalModelResponseDTO;
import edu.harvard.catalyst.scheduler.entity.*;
import edu.harvard.catalyst.scheduler.util.SubjectDataEncryptor;
import edu.harvard.catalyst.scheduler.util.TestUtils;
import junit.framework.TestCase;
import org.hibernate.query.Query;
import org.hibernate.query.NativeQuery;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static edu.harvard.catalyst.scheduler.persistence.TestRoles.StudyCoordinatorResearchAsst;
import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

/**
 * @author Bill Simons
 * @date 1/7/14
 * @link http://cbmi.med.harvard.edu
 * @link http://chip.org
 */
public class StudyDAOTest extends AbstractSpringWiredHibernateTest {

    @Autowired
    private StudyDAO dao;

    private static final Institution inst1 = new Institution("foo", "Foo University");
    private static final Institution inst2 = new Institution("bar", "Bar University");
    private static final Institution inst3 = new Institution("baz", "Baz University");
    private static final String localId1 = "local1";
    private static final String localId2 = "local2";
    private static final String localId3 = "local3";
    private static final String localId4 = "local4";
    private static final User user = makeTestUser(1, "ecommonsId1", "userLastName1");
    static final User investigator1 = makeTestUser(2, "investigator1", "investigator1LastName");
    static final User investigator2 = makeTestUser(3, "investigator2", "investigator2LastName");
    private static final Study study1 = makeTestStudy(1, "study1", TestStudyStatuses.OpenStudyStatus, localId1, investigator1);
    private static final Study study2 = makeTestStudy(2, "study2", TestStudyStatuses.ClosedStudyStatus, localId2, investigator2);
    private static final Study study3 = makeTestStudy(3, "study3", TestStudyStatuses.OpenStudyStatus, localId3, investigator2);
    private static final Study study4 = makeTestStudy(4, "study4", TestStudyStatuses.OpenStudyStatus, localId4, investigator2);
    private static final StudyUser su1 = new StudyUser(study1, user, true);
    private static final StudyUser su1a = new StudyUser(study1, user, true);
    private static final StudyUser su2 = new StudyUser(study2, user, true);
    private static final StudyUser su4 = new StudyUser(study4, user, true);

    @Before
    @Transactional
    public void populateDbWithTestData() {
        this.executeSqlScript("classpath:test-study-statuses.sql", false);
        this.executeSqlScript("classpath:test-institution-roles.sql", false);
        this.executeSqlScript("classpath:test-roles.sql", false);

        saveDummyData();
    }

    private void saveDummyData() {
        save(inst1, inst2, inst3);
        save(investigator1, investigator2);
        save(study1, study2, study3, study4);
        save(user);
        save(su1, su1a, su2, su4);
    }

    @Test
    public void testFindStudyListByPerson() throws Exception {
        final List<Study> actual = dao.findStudyListByPerson(user);

        // we only want the study to show up once
        assertEquals(2, actual.size());

        assertTrue(actual.contains(study1));
        assertTrue(actual.contains(study4));
        assertFalse(actual.contains(study2));
        assertFalse(actual.contains(study3));
    }

    @Test
    public void testFindStudyListByPersonAndLocalID() throws Exception {
        {
            final List<Study> actual = dao.findStudyListByPersonAndLocalID(user, localId1);
            assertEquals(1, actual.size());
            assertTrue(actual.contains(study1));
        }

        {
            final List<Study> actual = dao.findStudyListByPersonAndLocalID(user, localId2);
            assertEquals(1, actual.size());
            assertTrue(actual.contains(study2));
        }

        {
            final List<Study> actual = dao.findStudyListByPersonAndLocalID(user, localId3);
            assertTrue(actual.isEmpty()); // user is not in study3
        }

        {
            // find all studies starting with "local" prefix
            final List<Study> actual = dao.findStudyListByPersonAndLocalID(user, "local");
            assertEquals(3, actual.size());
            assertTrue(actual.contains(study1));
            assertTrue(actual.contains(study2));
            assertTrue(actual.contains(study4));
        }
    }

    @Test
    public void testFindStudyStaffFilterStudyListByPI() throws Exception {
        {
            final List<Study> actual = dao.findStudyStaffFilterStudyListByPI(user, investigator1.getLastName());
            assertEquals(1, actual.size());
            assertTrue(actual.contains(study1));
        }

        {
            final List<Study> actual = dao.findStudyStaffFilterStudyListByPI(user, investigator2.getLastName());
            assertEquals(2, actual.size());
            assertTrue(actual.contains(study2));
            assertTrue(actual.contains(study4));
        }
    }

    @Test
    public void testGetStudyStaffFilterStudiesByLocalId() throws Exception {
        {
            final List<Study> actual = dao.getStudyStaffFilterStudiesByLocalId(user, localId1);
            assertEquals(1, actual.size());
            assertTrue(actual.contains(study1));
        }

        {
            final List<Study> actual = dao.getStudyStaffFilterStudiesByLocalId(user, localId2);
            assertEquals(1, actual.size());
            assertTrue(actual.contains(study2));
        }

        {
            final List<Study> actual = dao.getStudyStaffFilterStudiesByLocalId(user, localId3);
            assertTrue(actual.isEmpty()); // user is not in study3
        }

        {
            // find all studies starting with "local" prefix - should be none
            // because "local" is not a full id
            final List<Study> actual = dao.getStudyStaffFilterStudiesByLocalId(user, "local");
            assertEquals(0, actual.size());
        }
    }

    private static User makeTestUser(final int userId, final String ecommonsId, final String lastName) {
        final User user = new User();
        user.setId(userId);
        user.setEcommonsId(ecommonsId);
        user.setFirstName("Test");
        user.setLastName(lastName);
        user.setPassword("password");
        user.setSalt("salt");
        user.setInstitution(inst1);
        user.setInstitutionRole(TestInstitutionRoles.SuperAdmin);
        user.setRole(StudyCoordinatorResearchAsst);
        return user;
    }

    private static Study makeTestStudy(final int studyId, final String studyName, final StudyStatus studyStatus, final String localId, final User investigator) {
        final Study study = new Study();
        study.setId(studyId);
        study.setName(studyName);
        study.setStudyStatus(studyStatus);
        study.setLocalId(localId);
        study.setInvestigator(investigator);
        return study;
    }

    @Test
    public void testAddFilterAndOrder() {
        filterHelper("blah", "query and (CONCAT(lower(s.localId), ' - ', " +
                "lower(s.name)) LIKE '%blah%')  order by sort asc");

        filterHelper("", "query order by sort asc");
        filterHelper(null, "query order by sort asc");
    }

    private void filterHelper(final String filter, final String expectedResult) {
        final String result = dao.addFilterAndOrder(filter, "sort", "asc", "query");
        TestCase.assertEquals(expectedResult, result);
    }

    @Test
    public void testPossiblyFlippedOrderByForApproved() {
        String result = dao.flipOrderByForVisitDotApproved("foo", "bar");
        assertEquals("bar", result);

        result = dao.flipOrderByForVisitDotApproved("v.approved", "ASC");
        assertEquals("DESC", result);

        result = dao.flipOrderByForVisitDotApproved("v.approved", "DESC");
        assertEquals("ASC", result);

        result = dao.flipOrderByForVisitDotApproved("v.approved", "foo");
        assertEquals("DESC", result);
    }

    @Test
    public void testGetStudyMembers() {

        String lastName1 = "last name 1";
        String lastName2 = "last name 2";
        String lastName3 = "last name 3";
        String lastName4 = "last name 4";

        String firstName1 = "first name 4 d";
        String firstName2 = "first name 3 c";
        String firstName3 = "first name 2 d";
        String firstName4 = "first name 1 d";

        String eCommonId1 = "e-commons-id 1 a";
        String eCommonId2 = "e-commons-id 2 a";
        String eCommonId3 = "e-commons-id 3 b";
        String eCommonId4 = "e-commons-id 4 b";

        String institutionName = "institution name";
        String institutionLongName = "institution long name";

        String everybodysPassword = "123";
        String everybodysSalt = "salt";

        String institutionRoleName = "institution role name";
        String institutionRoleDescription = "institution role description";

        String roleName = "roleName";
        RoleType roleType = RoleType.ROLE_SCHEDULER;

        // method call parameters and returned value variable

        String sortBy;
        String orderBy;
        int page;
        int maxResults;
        SearchDTO searchDto;

        List<UserDataResponse> responseList;

        // CASE 1 : no study members

        Study study1 = new Study();
        Study study2 = new Study();
        save(study1);
        save(study2);

        sortBy = "su.user.lastName";
        orderBy = "ASC";
        page = 1;
        maxResults = 2;
        searchDto = null;

        responseList = dao.getStudyMembers(study1, sortBy, orderBy, page, maxResults, searchDto);

        assertEquals(0, responseList.size());

        // CASE 2 : 3 members for the study, return 100 records

        Institution institution = new Institution(institutionName, institutionLongName);
        save(institution);

        InstitutionRole institutionRole = new InstitutionRole();
        institutionRole.setName(institutionRoleName);
        institutionRole.setType(InstitutionRoleType.ROLE_SCHEDULER);
        institutionRole.setDescription(institutionRoleDescription);
        save(institutionRole);

        Role role = new Role();
        role.setName(roleName);
        role.setType(roleType);
        save(role);

        User user1 = new User();
        user1.setEcommonsId(eCommonId1);
        user1.setLastName(lastName1);
        user1.setFirstName(firstName1);
        user1.setInstitution(institution);
        user1.setPassword(everybodysPassword);
        user1.setInstitutionRole(institutionRole);
        user1.setSalt(everybodysSalt);
        user1.setRole(role);
        save(user1);

        User user2 = new User();
        user2.setEcommonsId(eCommonId2);
        user2.setLastName(lastName2);
        user2.setFirstName(firstName2);
        user2.setInstitution(institution);
        user2.setPassword(everybodysPassword);
        user2.setInstitutionRole(institutionRole);
        user2.setSalt(everybodysSalt);
        user2.setRole(role);
        save(user2);

        User user3 = new User();
        user3.setEcommonsId(eCommonId3);
        user3.setLastName(lastName3);
        user3.setFirstName(firstName3);
        user3.setInstitution(institution);
        user3.setPassword(everybodysPassword);
        user3.setInstitutionRole(institutionRole);
        user3.setSalt(everybodysSalt);
        user3.setRole(role);
        save(user3);

        User user4 = new User();
        user4.setEcommonsId(eCommonId4);
        user4.setLastName(lastName4);
        user4.setFirstName(firstName4);
        user4.setInstitution(institution);
        user4.setPassword(everybodysPassword);
        user4.setInstitutionRole(institutionRole);
        user4.setSalt(everybodysSalt);
        user4.setRole(role);
        save(user4);

        StudyUser studyUser1 = new StudyUser(study1, user1, false);
        save(studyUser1);

        StudyUser studyUser2 = new StudyUser(study1, user2, true);
        save(studyUser2);

        // NOTE: this user on on study 2, not 1
        StudyUser studyUser3 = new StudyUser(study2, user3, true);
        save(studyUser3);

        StudyUser studyUser4 = new StudyUser(study1, user4, true);
        save(studyUser4);

        sortBy = "su.user.lastName";
        orderBy = "ASC";
        page = 1;
        maxResults = 100;
        searchDto = null;

        responseList = dao.getStudyMembers(study1, sortBy, orderBy, page, maxResults, searchDto);
        UserDataResponse response1 = responseList.get(0);
        UserDataResponse response2 = responseList.get(1);
        UserDataResponse response3 = responseList.get(2);

        assertEquals(3, responseList.size());
        assertEquals(lastName1, response1.getLastName());
        assertEquals(lastName2, response2.getLastName());
        assertEquals(lastName4, response3.getLastName());

        assertEquals(3, response1.getTotalCount());
        assertEquals(3, response2.getTotalCount());
        assertEquals(3, response3.getTotalCount());

        // CASE 3 : 3 members for the study, limit to 2 records

        sortBy = "su.user.lastName";
        orderBy = "ASC";
        page = 1;
        maxResults = 2;
        searchDto = null;

        responseList = dao.getStudyMembers(study1, sortBy, orderBy, page, maxResults, searchDto);
        response1 = responseList.get(0);
        response2 = responseList.get(1);

        assertEquals(2, responseList.size());
        assertEquals(lastName1, response1.getLastName());
        assertEquals(lastName2, response2.getLastName());

        assertEquals(3, response1.getTotalCount());
        assertEquals(3, response2.getTotalCount());
        assertEquals(3, response3.getTotalCount());

        // CASE 4 : 3 members for the study, return 2 records in reverse last name order

        sortBy = "su.user.lastName";
        orderBy = "DESC";
        page = 1;
        maxResults = 2;
        searchDto = null;

        responseList = dao.getStudyMembers(study1, sortBy, orderBy, page, maxResults, searchDto);
        response1 = responseList.get(0);
        response2 = responseList.get(1);

        assertEquals(2, responseList.size());
        assertEquals(lastName4, response1.getLastName());
        assertEquals(lastName2, response2.getLastName());

        assertEquals(3, response1.getTotalCount());
        assertEquals(3, response2.getTotalCount());

        // CASE 5 : 3 members for the study, return 2 records in reverse first name order

        sortBy = "su.user.firstName";
        orderBy = "DESC";
        page = 1;
        maxResults = 2;
        searchDto = null;

        responseList = dao.getStudyMembers(study1, sortBy, orderBy, page, maxResults, searchDto);
        response1 = responseList.get(0);
        response2 = responseList.get(1);

        assertEquals(2, responseList.size());
        assertEquals(firstName1, response1.getFirstName());
        assertEquals(firstName2, response2.getFirstName());

        assertEquals(3, response1.getTotalCount());
        assertEquals(3, response2.getTotalCount());

        // CASE 6 : 3 members for the study, return 2 records in first name order, filter by last name containing '4'

        sortBy = "su.user.firstName";
        orderBy = "DESC";
        page = 1;
        maxResults = 2;
        SearchDTO.SearchItem searchItem1 = new SearchDTO.SearchItem();
        searchItem1.setKey("su.user.lastName");
        searchItem1.setValue("4");
        List<SearchDTO.SearchItem> searchItems = new ArrayList<>();
        searchItems.add(searchItem1);
        searchDto = new SearchDTO();
        searchDto.setSearchItems(searchItems);

        responseList = dao.getStudyMembers(study1, sortBy, orderBy, page, maxResults, searchDto);
        response1 = responseList.get(0);

        assertEquals(1, responseList.size());
        assertEquals(firstName4, response1.getFirstName());

        assertEquals(1, response1.getTotalCount());

        // CASE 7 : 3 members for the study, return 2 records in first name order, filter by last name containing '4'

        sortBy = "su.user.firstName";
        orderBy = "DESC";
        page = 1;
        maxResults = 100;
        SearchDTO.SearchItem searchItem2 = new SearchDTO.SearchItem();
        searchItem2.setKey("su.user.ecommonsId");
        searchItem2.setValue("a");
        SearchDTO.SearchItem searchItem3 = new SearchDTO.SearchItem();
        searchItem3.setKey("su.user.firstName");
        searchItem3.setValue("c");
        searchItems = new ArrayList<>();
        searchItems.add(searchItem2);
        searchItems.add(searchItem3);
        searchDto = new SearchDTO();
        searchDto.setSearchItems(searchItems);

        responseList = dao.getStudyMembers(study1, sortBy, orderBy, page, maxResults, searchDto);
        response1 = responseList.get(0);

        assertEquals(1, responseList.size());
        assertEquals(firstName2, response1.getFirstName());

        assertEquals(1, response1.getTotalCount());

    }

    @Test
    public void testGetVisitApprovals() {

        Date masterDate = new Date();

        String lastName1 = "last name 1";
        String lastName2 = "last name 2";
        String lastName3 = "last name 3";
        String lastName4 = "last name 4";
        String lastName5 = "last name 5";
        String lastName6 = "last name 6";

        String firstName1 = "first name 4 d";
        String firstName2 = "first name 3 c";
        String firstName3 = "first name 2 d";
        String firstName4 = "first name 2 d";
        String firstName5 = "first name 5 d";
        String firstName6 = "first name 6 d";

        String eCommonId1 = "e-commons-id 1 a";
        String eCommonId2 = "e-commons-id 2 a";
        String eCommonId3 = "e-commons-id 3 b";
        String eCommonId4 = "e-commons-id 4 b";
        String eCommonId5 = "e-commons-id 5 b";
        String eCommonId6 = "e-commons-id 6 b";

        String institutionName = "institution name";
        String institutionLongName = "institution long name";

        String everybodysPassword = "123";
        String everybodysSalt = "salt";

        String institutionRoleName1 = "institution role name 1";
        String institutionRoleDescription1 = "institution role description 1";

        String institutionRoleName2 = "institution role name 2";
        String institutionRoleDescription2 = "institution role description 2";

        String roleName1 = "roleName 1";
        RoleType roleType1 = RoleType.ROLE_SCHEDULER;

        String roleName2 = "roleName 2";
        RoleType roleType2 = RoleType.ROLE_PHYSICIAN;

        String visitTemplateName1 = "visit template 1";
        String visitTemplateShortName1 = "visit template short name 1";
        Boolean visitTemplateApproved1 = false;
        Boolean visitTemplateRelativeTime1 = false;
        String visitTemplateNursing1 = null;
        String visitTemplateNutrition1 = null;
        String visitTemplateProcessing1 = null;
        String visitTemplateSetup1 = null;
        Date visitTemplateCreatedDate1 = new Date();
        Boolean visitTemplateInstitutionNonCRC1 = false;
        Boolean visitTemplateNonInstitutionNonCRC1 = false;
        Boolean visitTemplateResearchPharmacy1 = false;
        Boolean visitTemplateActive1 = false;
        Date visitTemplateLastUpdateTime1 = new Date(masterDate.getTime() + 1000 * 60);
        Integer visitTemplateDuration1 = 100;

        String visitTemplateName2 = "visit template 2";
        String visitTemplateShortName2 = "visit template short name 2";
        Boolean visitTemplateApproved2 = true;
        Boolean visitTemplateRelativeTime2 = false;
        String visitTemplateNursing2 = null;
        String visitTemplateNutrition2 = null;
        String visitTemplateProcessing2 = null;
        String visitTemplateSetup2 = null;
        Date visitTemplateCreatedDate2 = new Date();
        Boolean visitTemplateInstitutionNonCRC2 = false;
        Boolean visitTemplateNonInstitutionNonCRC2 = false;
        Boolean visitTemplateResearchPharmacy2 = false;
        Boolean visitTemplateActive2 = false;
        Date visitTemplateLastUpdateTime2 = new Date(masterDate.getTime() + 2000 * 60);
        Integer visitTemplateDuration2 = 100;

        String templateApprovalHistoryComment1 = "template approval history comment 1";
        String templateApprovalHistoryComment2 = "template approval history comment 2";
        String templateApprovalHistoryComment3 = "template approval history comment 3";
        String templateApprovalHistoryComment4 = "template approval history comment 4";
        String templateApprovalHistoryComment5 = "template approval history comment 5";
        String templateApprovalHistoryComment6 = "template approval history comment 6";

        Date statusChangeTime1 = new Date(masterDate.getTime() + 1 * 1000 * 60);
        Date statusChangeTime2 = new Date(masterDate.getTime() + 2 * 1000 * 60);
        Date statusChangeTime3 = new Date(masterDate.getTime() + 3 * 1000 * 60);
        Date statusChangeTime4 = new Date(masterDate.getTime() + 4 * 1000 * 60);
        Date statusChangeTime5 = new Date(masterDate.getTime() + 5 * 1000 * 60);
        Date statusChangeTime6 = new Date(masterDate.getTime() + 6 * 1000 * 60);

        boolean templateApprovalHistoryApproved1 = true;
        boolean templateApprovalHistoryApproved2 = false;
        boolean templateApprovalHistoryApproved3 = true;
        boolean templateApprovalHistoryApproved4 = false;
        boolean templateApprovalHistoryApproved5 = true;
        boolean templateApprovalHistoryApproved6 = false;

        // entities

        Institution institution = new Institution(institutionName, institutionLongName);
        save(institution);

        InstitutionRole institutionRole1 = new InstitutionRole();
        institutionRole1.setName(institutionRoleName1);
        institutionRole1.setType(InstitutionRoleType.ROLE_SCHEDULER);
        institutionRole1.setDescription(institutionRoleDescription1);
        save(institutionRole1);

        InstitutionRole institutionRole2 = new InstitutionRole();
        institutionRole2.setName(institutionRoleName2);
        institutionRole2.setType(InstitutionRoleType.ROLE_GENERAL_VIEW);
        institutionRole2.setDescription(institutionRoleDescription2);
        save(institutionRole2);

        Role role1 = new Role();
        role1.setName(roleName1);
        role1.setType(roleType1);
        save(role1);

        Role role2 = new Role();
        role2.setName(roleName2);
        role2.setType(roleType2);
        save(role2);


        User user1 = new User();
        user1.setEcommonsId(eCommonId1);
        user1.setLastName(lastName1);
        user1.setFirstName(firstName1);
        user1.setInstitution(institution);
        user1.setPassword(everybodysPassword);
        user1.setInstitutionRole(institutionRole1);
        user1.setSalt(everybodysSalt);
        user1.setRole(role1);
        save(user1);

        User user2 = new User();
        user2.setEcommonsId(eCommonId2);
        user2.setLastName(lastName2);
        user2.setFirstName(firstName2);
        user2.setInstitution(institution);
        user2.setPassword(everybodysPassword);
        user2.setInstitutionRole(institutionRole1);
        user2.setSalt(everybodysSalt);
        user2.setRole(role1);
        save(user2);

        User user3 = new User();
        user3.setEcommonsId(eCommonId3);
        user3.setLastName(lastName3);
        user3.setFirstName(firstName3);
        user3.setInstitution(institution);
        user3.setPassword(everybodysPassword);
        user3.setInstitutionRole(institutionRole1);
        user3.setSalt(everybodysSalt);
        user3.setRole(role1);
        save(user3);

        User user4 = new User();
        user4.setEcommonsId(eCommonId4);
        user4.setLastName(lastName4);
        user4.setFirstName(firstName4);
        user4.setInstitution(institution);
        user4.setPassword(everybodysPassword);
        user4.setInstitutionRole(institutionRole1);
        user4.setSalt(everybodysSalt);
        user4.setRole(role1);
        save(user4);

        User user5 = new User();
        user5.setEcommonsId(eCommonId5);
        user5.setLastName(lastName5);
        user5.setFirstName(firstName5);
        user5.setInstitution(institution);
        user5.setPassword(everybodysPassword);
        user5.setInstitutionRole(institutionRole2);
        user5.setSalt(everybodysSalt);
        user5.setRole(role2);
        save(user5);

        User user6 = new User();
        user6.setEcommonsId(eCommonId6);
        user6.setLastName(lastName6);
        user6.setFirstName(firstName6);
        user6.setInstitution(institution);
        user6.setPassword(everybodysPassword);
        user6.setInstitutionRole(institutionRole2);
        user6.setSalt(everybodysSalt);
        user6.setRole(role2);
        save(user6);

        VisitType visitTemplateVisitType1 = new VisitType();
        visitTemplateVisitType1.setName(TestUtils.InpatientCRC);
        VisitType visitTemplateVisitType2 = new VisitType();
        visitTemplateVisitType2.setName(TestUtils.InpatientCRC);
        save(visitTemplateVisitType1);
        save(visitTemplateVisitType2);

        VisitTemplate visitTemplate1 = new VisitTemplate(
                null,
                visitTemplateName1,
                visitTemplateShortName1,
                visitTemplateApproved1,
                visitTemplateRelativeTime1,
                visitTemplateNursing1,
                visitTemplateNutrition1,
                visitTemplateProcessing1,
                visitTemplateSetup1,
                null,
                visitTemplateCreatedDate1,
                visitTemplateVisitType1,
                null,
                visitTemplateInstitutionNonCRC1,
                visitTemplateNonInstitutionNonCRC1,
                visitTemplateResearchPharmacy1,
                visitTemplateActive1,
                visitTemplateLastUpdateTime1,
                visitTemplateDuration1,
                true
        );
        save(visitTemplate1);

        VisitTemplate visitTemplate2 = new VisitTemplate(
                null,
                visitTemplateName2,
                visitTemplateShortName2,
                visitTemplateApproved2,
                visitTemplateRelativeTime2,
                visitTemplateNursing2,
                visitTemplateNutrition2,
                visitTemplateProcessing2,
                visitTemplateSetup2,
                null,
                visitTemplateCreatedDate2,
                visitTemplateVisitType2,
                null,
                visitTemplateInstitutionNonCRC2,
                visitTemplateNonInstitutionNonCRC2,
                visitTemplateResearchPharmacy2,
                visitTemplateActive2,
                visitTemplateLastUpdateTime2,
                visitTemplateDuration2,
                true
        );
        save(visitTemplate2);

        TemplateApprovalHistory templateApprovalHistory1 = new TemplateApprovalHistory(
                user1,
                user1.getLastName(),
                user1.getFirstName(),
                user1.getRole().getName(),
                user1.getInstitutionRole().getName(),
                templateApprovalHistoryApproved1,
                templateApprovalHistoryComment1,
                statusChangeTime1,
                visitTemplate1,
                visitTemplate1.getName(),
                new Date()
        );
        save(templateApprovalHistory1);

        TemplateApprovalHistory templateApprovalHistory2 = new TemplateApprovalHistory(
                user2,
                user2.getLastName(),
                user2.getFirstName(),
                user2.getRole().getName(),
                user2.getInstitutionRole().getName(),
                templateApprovalHistoryApproved2,
                templateApprovalHistoryComment2,
                statusChangeTime2,
                visitTemplate1,
                visitTemplate1.getName(),
                new Date()
        );
        save(templateApprovalHistory2);

        TemplateApprovalHistory templateApprovalHistory3 = new TemplateApprovalHistory(
                user3,
                user3.getLastName(),
                user3.getFirstName(),
                user3.getRole().getName(),
                user3.getInstitutionRole().getName(),
                templateApprovalHistoryApproved3,
                templateApprovalHistoryComment3,
                statusChangeTime3,
                visitTemplate2,
                visitTemplate2.getName(),
                new Date()
        );
        save(templateApprovalHistory3);

        TemplateApprovalHistory templateApprovalHistory4 = new TemplateApprovalHistory(
                user4,
                user4.getLastName(),
                user4.getFirstName(),
                user4.getRole().getName(),
                user4.getInstitutionRole().getName(),
                templateApprovalHistoryApproved4,
                templateApprovalHistoryComment4,
                statusChangeTime4,
                visitTemplate2,
                visitTemplate2.getName(),
                new Date()
        );
        save(templateApprovalHistory4);

        TemplateApprovalHistory templateApprovalHistory5 = new TemplateApprovalHistory(
                user5,
                user5.getLastName(),
                user5.getFirstName(),
                user5.getRole().getName(),
                user5.getInstitutionRole().getName(),
                templateApprovalHistoryApproved5,
                templateApprovalHistoryComment5,
                statusChangeTime5,
                visitTemplate2,
                visitTemplate2.getName(),
                new Date()
        );
        save(templateApprovalHistory5);

        TemplateApprovalHistory templateApprovalHistory6 = new TemplateApprovalHistory(
                user6,
                user6.getLastName(),
                user6.getFirstName(),
                user6.getRole().getName(),
                user6.getInstitutionRole().getName(),
                templateApprovalHistoryApproved6,
                templateApprovalHistoryComment6,
                statusChangeTime6,
                visitTemplate2,
                visitTemplate2.getName(),
                new Date()
        );
        save(templateApprovalHistory6);

        // method call parameters and returned value variable

        String sortBy;
        String orderBy;
        int page;
        int maxResults;

        List<VisitApprovalModelResponseDTO.VisitApproval> visitApprovalList;
        VisitApprovalModelResponseDTO.VisitApproval approval1;
        VisitApprovalModelResponseDTO.VisitApproval approval2;
        VisitApprovalModelResponseDTO.VisitApproval approval3;
        VisitApprovalModelResponseDTO.VisitApproval approval4;

        // CASE 1 : list every approval for visit template 2, in date order

        sortBy = "tah.statusChangeTime";
        orderBy = "ASC";
        page = 1;
        maxResults = 20;

        visitApprovalList = dao.getVisitApprovals(
                visitTemplate2.getId(), sortBy, orderBy, page, maxResults
        );

        assertEquals(4, visitApprovalList.size());

        approval1 = visitApprovalList.get(0);
        assertEquals(4, approval1.getTotalCount().longValue());
        assertEquals(user3.getLastName(), approval1.getUserLastName());
        assertEquals(user3.getFirstName(), approval1.getUserFirstName());
        assertEquals(templateApprovalHistory3.getStatusChangeTime(), approval1.getStatusChangeTime());
        assertEquals(templateApprovalHistory3.getComment(), approval1.getComment());
        assertEquals(templateApprovalHistory3.getVisitTemplate().getCreatedDate(), approval1.getVisitCreatedDate());
        assertEquals(user3.getRole().getName(), approval1.getUserRole());
        assertEquals(user3.getInstitutionRole().getName(), approval1.getUserTitle());
        assertEquals("Approved", approval1.getApproved()); //visit template approved
        assertEquals("Approved", approval1.getFinalApproval()); // history: changed to approved

        approval2 = visitApprovalList.get(1);
        assertEquals(4, approval2.getTotalCount().longValue());
        assertEquals(user4.getLastName(), approval2.getUserLastName());
        assertEquals(user4.getFirstName(), approval2.getUserFirstName());
        assertEquals(templateApprovalHistory4.getStatusChangeTime(), approval2.getStatusChangeTime());
        assertEquals(templateApprovalHistory4.getComment(), approval2.getComment());
        assertEquals(templateApprovalHistory4.getVisitTemplate().getCreatedDate(), approval2.getVisitCreatedDate());
        assertEquals(user4.getRole().getName(), approval2.getUserRole());
        assertEquals(user4.getInstitutionRole().getName(), approval2.getUserTitle());
        assertEquals("Not Approved", approval2.getApproved()); //visit template approved
        assertEquals("Approved", approval2.getFinalApproval()); // history: changed to approved

        approval3 = visitApprovalList.get(2);
        assertEquals(4, approval3.getTotalCount().longValue());
        assertEquals(user5.getLastName(), approval3.getUserLastName());
        assertEquals(user5.getFirstName(), approval3.getUserFirstName());
        assertEquals(templateApprovalHistory5.getStatusChangeTime(), approval3.getStatusChangeTime());
        assertEquals(templateApprovalHistory5.getComment(), approval3.getComment());
        assertEquals(templateApprovalHistory5.getVisitTemplate().getCreatedDate(), approval3.getVisitCreatedDate());
        assertEquals(user5.getRole().getName(), approval3.getUserRole());
        assertEquals(user5.getInstitutionRole().getName(), approval3.getUserTitle());
        assertEquals("Approved", approval3.getApproved()); //visit template approved
        assertEquals("Approved", approval3.getFinalApproval()); // history: changed to approved

        approval4 = visitApprovalList.get(3);
        assertEquals(4, approval4.getTotalCount().longValue());
        assertEquals(user6.getLastName(), approval4.getUserLastName());
        assertEquals(user6.getFirstName(), approval4.getUserFirstName());
        assertEquals(templateApprovalHistory6.getStatusChangeTime(), approval4.getStatusChangeTime());
        assertEquals(templateApprovalHistory6.getComment(), approval4.getComment());
        assertEquals(templateApprovalHistory6.getVisitTemplate().getCreatedDate(), approval4.getVisitCreatedDate());
        assertEquals(user6.getRole().getName(), approval4.getUserRole());
        assertEquals(user6.getInstitutionRole().getName(), approval4.getUserTitle());
        assertEquals("Not Approved", approval4.getApproved()); //visit template approved
        assertEquals("Approved", approval4.getFinalApproval()); // history: changed to approved

        // CASE 2 : list every approval for visit template 2, in date order

        sortBy = "u.lastName";
        orderBy = "DESC";
        page = 1;
        maxResults = 20;

        visitApprovalList = dao.getVisitApprovals(
                visitTemplate2.getId(), sortBy, orderBy, page, maxResults
        );

        assertEquals(4, visitApprovalList.size());

        approval1 = visitApprovalList.get(0);
        assertEquals(user6.getLastName(), approval1.getUserLastName());

        approval2 = visitApprovalList.get(1);
        assertEquals(user5.getLastName(), approval2.getUserLastName());

        approval3 = visitApprovalList.get(2);
        assertEquals(user4.getLastName(), approval3.getUserLastName());

        approval4 = visitApprovalList.get(3);
        assertEquals(user3.getLastName(), approval4.getUserLastName());

        // CASE 3 : list every approval for visit template 2, in ascending order of user title, 2 per page, 2nd page

        sortBy = "u.institutionRole";
        orderBy = "ASC";
        page = 2;
        maxResults = 2;

        visitApprovalList = dao.getVisitApprovals(
                visitTemplate2.getId(), sortBy, orderBy, page, maxResults
        );

        assertEquals(2, visitApprovalList.size());

        approval3 = visitApprovalList.get(0);
        assertEquals(4, approval3.getTotalCount().longValue());
        assertEquals(institutionRoleName2, approval3.getUserTitle());

        approval4 = visitApprovalList.get(1);
        assertEquals(4, approval4.getTotalCount().longValue());
        assertEquals(institutionRoleName2, approval4.getUserTitle());

    }

    /**
     * This test only verifies the correctness of the generated HQL string
     */
    @Test
    public void testGetStudyVisitsByStatusHqlQueryString() {

        int study = 1;
        boolean active = true;
        String sortBy = "v.name";
        String orderBy = "DESC";
        int page = 2;
        int maxResults = 3;

        // filtered columns: v.name, v.visitType, v.sublocation.name
        SearchDTO searchDTO = new SearchDTO();
        searchDTO.setSearchItems(
                Lists.newArrayList(
                        new SearchDTO.SearchItem(
                                SearchDTO.SearchType.ANY,
                                "v.visitType",
                                TestUtils.InpatientCRC.toString() + " ",
                                null
                        ),
                        new SearchDTO.SearchItem(
                                SearchDTO.SearchType.ANY,
                                "v.sublocation.name",
                                "partial location name",
                                null
                        )
                )
        );

        searchDTO.mapSearchItemKeyAndValue( "v.visitType", "v.visitType", SearchDTO.visitTypeValueMapper);

        // IMPORTANT: using the StudyDAO wired-in by spring does not work
        // Instead create a mock, and stub every method that gets called by the method
        // under test
        StudyDAO studyDao = new StudyDAO();
        StudyDAO studyDaoSpy = Mockito.spy(studyDao);

        org.hibernate.Query mockQuery = mock(Query.class);

        doReturn(session()).when(studyDaoSpy).session();
        doReturn(mockQuery).when(studyDaoSpy).newQuery(any());
        doReturn(mockQuery).when(mockQuery).setParameter(anyString(), any());
        doReturn(mockQuery).when(mockQuery).setFirstResult(anyInt());
        doReturn(mockQuery).when(mockQuery).setMaxResults(anyInt());

        studyDaoSpy.getStudyVisitsByStatus(study, active, sortBy, orderBy, page, maxResults, searchDTO);

        String expectedHql = "SELECT v from VisitTemplate v, Study s " +
                "WHERE v.study = s.id and s.id =:study and v.active = :active  " +
                "AND v.visitType LIKE :v_visitType " +
                "AND v.sublocation.name LIKE :v_sublocation_name " +
                "ORDER BY v.name DESC";

        Mockito.verify(studyDaoSpy, times(1)).newQuery(expectedHql);

        List<SearchDTO.SearchItem> searchItems = searchDTO.getSearchItems();

        assertEquals("v_visitType", searchItems.get(0).getQueryParamName());
        assertEquals("v_sublocation_name", searchItems.get(1).getQueryParamName());

        assertEquals("%InpatientCRC%", searchItems.get(0).getQueryParamValue());
        assertEquals("%partial location name%", searchItems.get(1).getQueryParamValue());

    }

    /**
     * This test only verifies the correctness of the generated SQL string
     */
    @Test
    public void testGetStudyListSqlQueryString() {

        String sortBy = "s.name";
        String orderBy = "DESC";
        int page = 2;
        int maxResults = 3;

        // filtered columns:
        // s.name, pi.last_name, pi.first_name, s.irb, s.local_id
        List<SearchDTO.SearchItem> searchItems = Lists.newArrayList(
                new SearchDTO.SearchItem(
                        SearchDTO.SearchType.ANY,
                        "s.name",
                        "partial study name",
                        null
                ),
                new SearchDTO.SearchItem(
                        SearchDTO.SearchType.ANY,
                        "pi.last_name",
                        "partial PI last name",
                        null
                ),
                new SearchDTO.SearchItem(
                        SearchDTO.SearchType.ANY,
                        "pi.first_name",
                        "partial PI first name",
                        null
                ),
                new SearchDTO.SearchItem(
                        SearchDTO.SearchType.ANY,
                        "s.irb",
                        "partial IRB",
                        null
                ),
                new SearchDTO.SearchItem(
                        SearchDTO.SearchType.ANY,
                        "s.local_id",
                        "partial local ID",
                        null
                )
        );

        SearchDTO searchDTO = new SearchDTO();
        searchDTO.setSearchItems(searchItems);

        // IMPORTANT: using the StudyDAO wired-in by spring does not work
        // Instead create a mock, and stub every method that gets called by the method
        // under test
        StudyDAO studyDao = new StudyDAO();
        StudyDAO studyDaoSpy = Mockito.spy(studyDao);

        NativeQuery mockQuery = mock(NativeQuery.class);

        doReturn(session()).when(studyDaoSpy).session();
        // This one is a SQL query, not HQL
        doReturn(mockQuery).when(studyDaoSpy).newNativeQuery(any());
        doReturn(mockQuery).when(mockQuery).setParameter(anyString(), any());
        doReturn(mockQuery).when(mockQuery).setFirstResult(anyInt());
        doReturn(mockQuery).when(mockQuery).setMaxResults(anyInt());

        User studyUser = new User();
        studyUser.setId(22);

        String fromStudyUserString = "study_user su, ";
        String whereStudyUserString = " s.id = su.study and su.user = :user and su.active=true and ";


        studyDaoSpy.getStudyList(
                sortBy, orderBy, page, maxResults,
                fromStudyUserString, whereStudyUserString,
                studyUser, searchDTO);

        String expectedSql = "select s.id, s.irb, s.local_id, s.name, ss.short_name, pi.first_name, pi.last_name " +
                " from study_status ss, study_user su,  study s left outer join user pi on pi.id = s.principal_investigator " +
                " where  s.id = su.study and su.user = :user " +
                "and su.active=true and  ss.id = s.study_status " +
                "AND s.name LIKE :s_name " +
                "AND pi.last_name LIKE :pi_last_name " +
                "AND pi.first_name LIKE :pi_first_name " +
                "AND s.irb LIKE :s_irb " +
                "AND s.local_id LIKE :s_local_id order by s.name DESC";

        Mockito.verify(studyDaoSpy, times(1)).newNativeQuery(expectedSql);

        assertEquals("s_name", searchItems.get(0).getQueryParamName());
        assertEquals("pi_last_name", searchItems.get(1).getQueryParamName());
        assertEquals("pi_first_name", searchItems.get(2).getQueryParamName());
        assertEquals("s_irb", searchItems.get(3).getQueryParamName());
        assertEquals("s_local_id", searchItems.get(4).getQueryParamName());

        assertEquals("%partial study name%", searchItems.get(0).getQueryParamValue());
        assertEquals("%partial PI last name%", searchItems.get(1).getQueryParamValue());
        assertEquals("%partial PI first name%", searchItems.get(2).getQueryParamValue());
        assertEquals("%partial IRB%", searchItems.get(3).getQueryParamValue());
        assertEquals("%partial local ID%", searchItems.get(4).getQueryParamValue());

    }

    /**
     * This test only verifies the correctness of the generated HQL string
     */
    @Test
    public void testGetStudyMembersHqlQueryString() {

        String sortBy = "s.name";
        String orderBy = "DESC";
        int page = 2;
        int maxResults = 3;

        // filtered columns:
        //        su.user.lastName, su.user.firstName, su.user.institutionRole.name
        SearchDTO searchDTO = new SearchDTO();
        searchDTO.setSearchItems(
                Lists.newArrayList(
                        new SearchDTO.SearchItem(
                        SearchDTO.SearchType.ANY,
                        "su.user.lastName",
                        "partial user last name",
                        null
                    ),
                    new SearchDTO.SearchItem(
                            SearchDTO.SearchType.ANY,
                            "su.user.firstName",
                            "partial user first name",
                            null
                    ),
                    new SearchDTO.SearchItem(
                            SearchDTO.SearchType.ANY,
                            "su.user.institutionRole.name",
                            "partial institutionRole name",
                            null
                    )
                )
        );

        // IMPORTANT: using the StudyDAO wired-in by spring does not work
        // Instead create a mock, and stub every method that gets called by the method
        // under test
        StudyDAO studyDao = new StudyDAO();
        StudyDAO studyDaoSpy = Mockito.spy(studyDao);

        org.hibernate.Query mockQuery = mock(Query.class);

        doReturn(session()).when(studyDaoSpy).session();
        doReturn(mockQuery).when(studyDaoSpy).newQuery(any());
        doReturn(mockQuery).when(mockQuery).setParameter(anyString(), any());
        doReturn(mockQuery).when(mockQuery).setFirstResult(anyInt());
        doReturn(mockQuery).when(mockQuery).setMaxResults(anyInt());

        Study study = mock(Study.class);

        studyDaoSpy.getStudyMembers(study, sortBy, orderBy, page, maxResults, searchDTO);

        String expectedHql = "SELECT su from StudyUser su WHERE su.study = :study " +
                " AND su.user.lastName LIKE :su_user_lastName " +
                "AND su.user.firstName LIKE :su_user_firstName " +
                "AND su.user.institutionRole.name LIKE :su_user_institutionRole_name " +
                "order by s.name DESC";

        Mockito.verify(studyDaoSpy, times(1)).newQuery(expectedHql);

        List<SearchDTO.SearchItem> searchItems = searchDTO.getSearchItems();

        assertEquals("su_user_lastName", searchItems.get(0).getQueryParamName());
        assertEquals("su_user_firstName", searchItems.get(1).getQueryParamName());
        assertEquals("su_user_institutionRole_name", searchItems.get(2).getQueryParamName());

        assertEquals("%partial user last name%", searchItems.get(0).getQueryParamValue());
        assertEquals("%partial user first name%", searchItems.get(1).getQueryParamValue());
        assertEquals("%partial institutionRole name%", searchItems.get(2).getQueryParamValue());

    }

    /**
     * This test only verifies the correctness of the generated HQL string
     */
    @Test
    public void testFindStudySubjectsByStudyClausically() {

        // filtered columns:
        //      mrn, lastName, firstName, birthdate, city, state, primaryContactNumber
        List<SearchDTO.SearchItem> searchItems = Lists.newArrayList(
                new SearchDTO.SearchItem(
                        SearchDTO.SearchType.EXACT,
                        "mrn",
                        "123",
                        null
                ),
                new SearchDTO.SearchItem(
                        SearchDTO.SearchType.EXACT,
                        "lastName",
                        "partial last name",
                        null
                ),
                new SearchDTO.SearchItem(
                        SearchDTO.SearchType.EXACT,
                        "firstName",
                        "partial first name",
                        null
                ),
                new SearchDTO.SearchItem(
                        SearchDTO.SearchType.EXACT,
                        "birthdate",
                        "01/01/1900",
                        null
                ),
                new SearchDTO.SearchItem(
                        SearchDTO.SearchType.ANY,
                        "city",
                        "partial city",
                        null
                ),
                new SearchDTO.SearchItem(
                        SearchDTO.SearchType.ANY,
                        "state",
                        "MA",
                        null
                ),
                new SearchDTO.SearchItem(
                        SearchDTO.SearchType.ANY,
                        "primaryContactNumber",
                        "555",
                        null
                )
        );

        // IMPORTANT: using the StudyDAO wired-in by spring does not work
        // Instead create a mock, and stub every method that gets called by the method
        // under test
        StudyDAO studyDao = new StudyDAO();
        StudyDAO studyDaoSpy = Mockito.spy(studyDao);

        org.hibernate.Query mockQuery = mock(Query.class);

        doReturn(session()).when(studyDaoSpy).session();
        doReturn(mockQuery).when(studyDaoSpy).newQuery(any());
        doReturn(mockQuery).when(mockQuery).setParameter(anyString(), any());
        doReturn(mockQuery).when(mockQuery).setFirstResult(anyInt());
        doReturn(mockQuery).when(mockQuery).setMaxResults(anyInt());

        Study study = mock(Study.class);

        SearchDTO searchDTO = new SearchDTO();
        searchDTO.setSearchItems(searchItems);

        // mimmic what is done in the service layer: map search item keys
        searchDTO.mapSearchItemKeyAndValue("mrn", "sm.mrn", SubjectDataEncryptor.capitalizeAndEncrypt);
        searchDTO.mapSearchItemKeyAndValue("lastName", "su.lastName", SubjectDataEncryptor.capitalizeAndEncrypt);
        searchDTO.mapSearchItemKeyAndValue("firstName", "su.firstName", SubjectDataEncryptor.capitalizeAndEncrypt);
        searchDTO.mapSearchItemKeyAndValue("birthdate", "su.birthdate", null);
        searchDTO.mapSearchItemKeyAndValue("city", "su.city", SubjectDataEncryptor.capitalizeAndEncrypt);
        searchDTO.mapSearchItemKeyAndValue("state", "su.state.name", null);
        searchDTO.mapSearchItemKeyAndValue("primaryContactNumber", "su.primaryContactNumber", SubjectDataEncryptor.capitalizeAndEncrypt);

        studyDaoSpy.findStudySubjectsByStudyClausically(study, true, searchDTO);

        String expectedHql = "SELECT ss FROM StudySubject ss, Subject su, SubjectMrn sm " +
                "where ss.study = :study and sm.id = ss.subjectMrn " +
                "and su.id = sm.subject and su.active = true " +
                " and su.archivalStatus IS NULL " +
                " AND sm.mrn = :sm_mrn " +
                "AND su.lastName = :su_lastName " +
                "AND su.firstName = :su_firstName " +
                "AND su.birthdate = :su_birthdate " +
                "AND su.city LIKE :su_city " +
                "AND su.state.name LIKE :su_state_name " +
                "AND su.primaryContactNumber LIKE :su_primaryContactNumber";

        Mockito.verify(studyDaoSpy, times(1)).newQuery(expectedHql);

        assertEquals("sm_mrn", searchItems.get(0).getQueryParamName());
        assertEquals("su_lastName", searchItems.get(1).getQueryParamName());
        assertEquals("su_firstName", searchItems.get(2).getQueryParamName());
        assertEquals("su_birthdate", searchItems.get(3).getQueryParamName());
        assertEquals("su_city", searchItems.get(4).getQueryParamName());
        assertEquals("su_state_name", searchItems.get(5).getQueryParamName());
        assertEquals("su_primaryContactNumber", searchItems.get(6).getQueryParamName());

        assertEquals("QftkHtTYzIuF4Ed2wrtsGg==", searchItems.get(0).getQueryParamValue());
        assertEquals("czQAu4S3ahgXnNB1KH9+iSxEf16WpNXsU6YWS/pX/2Q=", searchItems.get(1).getQueryParamValue());
        assertEquals("erltshLkVKtEBEKAdaUj6uPnTg79vpBB0RIVr04S1bc=", searchItems.get(2).getQueryParamValue());
        assertEquals("01/01/1900", searchItems.get(3).getQueryParamValue());
        assertEquals("%/MDvvCxmDcKPaeOHljdDtQ==%", searchItems.get(4).getQueryParamValue());
        assertEquals("%MA%", searchItems.get(5).getQueryParamValue());
        assertEquals("%pZ6aitp4TxHY5D+4Uo3u0Q==%", searchItems.get(6).getQueryParamValue());

    }
}
