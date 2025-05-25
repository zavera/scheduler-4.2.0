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

import edu.harvard.catalyst.scheduler.AbstractSpringWiredHibernateTest;
import edu.harvard.catalyst.scheduler.dto.response.GetStudiesResponse;
import edu.harvard.catalyst.scheduler.dto.response.GetStudyVisitsResponse;
import edu.harvard.catalyst.scheduler.dto.response.VisitTemplatesResponse;
import edu.harvard.catalyst.scheduler.entity.*;
import edu.harvard.catalyst.scheduler.util.dbpopulator.DbPopulator;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Created by xavier on 10/4/17.
 */
public class StudyDAOTransactionalTest extends AbstractSpringWiredHibernateTest {

    @Autowired
    StudyDAO dao;

    DbPopulator dataPopulator;

    @Before
    public void before() {

        this.dataPopulator = new DbPopulator(dao);

    }

    @Test
    public void testGetStudyVisits() {

        // this code only tests the fact that visit templates with no assigned resources
        // are not returned

        Study study = this.dataPopulator.studyPopulator.populateOne(1);
        int studyId = study.getId();

        VisitType visitType = this.dataPopulator.visitTypePopulator.populateOne(1);
        VisitTemplate visitTemplate1 = this.dataPopulator.visitTemplatePopulator.populateOne(
                1,
                study,
                visitType,
                true,
                false,
                false
        );
        VisitTemplate visitTemplate2 = this.dataPopulator.visitTemplatePopulator.populateOne(
                2,
                study,
                visitType,
                true,
                false,
                false
        );

        Resource resource1 = this.dataPopulator.resourcePopulator.populateOne(1);
        Resource resource2 = this.dataPopulator.resourcePopulator.populateOne(2);

        TemplateResource templateResource1 = this.dataPopulator.templateResourcePopulator.populateOne(
                null,
                visitTemplate1,
                resource1,
                10,
                20,
                null,
                null,
                false,
                false,
                false
        );

        // verify that visit templates without resources are not returned

        GetStudyVisitsResponse response = dao.getStudyVisits(
                "",
                "v.name",
                "ASC",
                1,
                1,
                studyId,
                false
        );

        assertEquals(1, response.visitTemplate1s.size());
        assertEquals(visitTemplate1.getId(), response.visitTemplate1s.get(0).id);

        TemplateResource templateResource2 = this.dataPopulator.templateResourcePopulator.populateOne(
                null,
                visitTemplate2,
                resource2,
                10,
                20,
                null,
                null,
                false,
                false,
                false
        );

        response = dao.getStudyVisits(
                "",
                "v.name",
                "ASC",
                1,
                10,
                studyId,
                false
        );

        // the returned visit templates should be ordered alphabetically by name
        // the data dbpopulator generates names such as "visit template 1", "visit template 2", etc
        assertEquals(2, response.visitTemplate1s.size());
        assertEquals(visitTemplate1.getId(), response.visitTemplate1s.get(0).id);
        assertEquals(visitTemplate2.getId(), response.visitTemplate1s.get(1).id);

    }

    @Test
    public void testGetRoles() {

        this.dataPopulator.rolePopulator.populateMany(1, 3);

        List<Role> roles = dao.getRoles();

        assertEquals(3, roles.size());
        assertEquals("role 1", roles.get(0).getName());
        assertEquals("role 2", roles.get(1).getName());
        assertEquals("role 3", roles.get(2).getName());

    }

    @Test
    public void testGetInstitutionRoles() {

        this.dataPopulator.institutionRolePopulator.populateMany(1, 3);

        List<InstitutionRole> institutionRoles = dao.getInstitutionRoles();

        assertEquals(3, institutionRoles.size());
        assertEquals("institution role 1", institutionRoles.get(0).getName());
        assertEquals("institution role 2", institutionRoles.get(1).getName());
        assertEquals("institution role 3", institutionRoles.get(2).getName());

    }

    @Test
    public void testGetStudyStatuses() {

        this.dataPopulator.studyStatusPopulator.populateMany(1, 3);

        List<StudyStatus> studyStatuses = dao.getStudyStatuses();

        assertEquals(3, studyStatuses.size());
        assertEquals("study status 1", studyStatuses.get(0).getName());
        assertEquals("study status 2", studyStatuses.get(1).getName());
        assertEquals("study status 3", studyStatuses.get(2).getName());

    }

    @Test
    public void testGetOverrideReasons() {

        this.dataPopulator.appointmentOverrideReasonPopulator.populateMany(1, 3);

        List<AppointmentOverrideReason> studyStatuses = dao.getOverrideReasons();

        assertEquals(3, studyStatuses.size());
        assertEquals("appointment override reason 1", studyStatuses.get(0).getName());
        assertEquals("appointment override reason 2", studyStatuses.get(1).getName());
        assertEquals("appointment override reason 3", studyStatuses.get(2).getName());

    }

    @Test
    public void testFindAppointmentStatusReasonByStatus() {

        List<AppointmentStatus> appointmentStatuses =
                this.dataPopulator.appointmentStatusPopulator.populateMany(1, 2);

        this.dataPopulator.appointmentStatusReasonPopulator.populateMany(1, 2, appointmentStatuses.get(0));
        this.dataPopulator.appointmentStatusReasonPopulator.populateMany(3, 3, appointmentStatuses.get(1));

        List<AppointmentStatusReason> retrievedAppointmentStatusReasons =
                dao.findAppointmentStatusReasonByStatus(appointmentStatuses.get(1));

        assertEquals(3, retrievedAppointmentStatusReasons.size());
        assertEquals("appointment status reason 3", retrievedAppointmentStatusReasons.get(0).getName());
        assertEquals("appointment status reason 4", retrievedAppointmentStatusReasons.get(1).getName());
        assertEquals("appointment status reason 5", retrievedAppointmentStatusReasons.get(2).getName());
        assertEquals(appointmentStatuses.get(1), retrievedAppointmentStatusReasons.get(0).getAppointmentStatus());
        assertEquals(appointmentStatuses.get(1), retrievedAppointmentStatusReasons.get(1).getAppointmentStatus());
        assertEquals(appointmentStatuses.get(1), retrievedAppointmentStatusReasons.get(2).getAppointmentStatus());

    }

    @Test
    public void testGetAppointmentStatuses() {

        this.dataPopulator.appointmentStatusPopulator.populateMany(1, 3);

        List<AppointmentStatus> appointmentStatuses = dao.getAppointmentStatuses();

        assertEquals(3, appointmentStatuses.size());
        assertEquals("appointment status 1", appointmentStatuses.get(0).getName());
        assertEquals("appointment status 2", appointmentStatuses.get(1).getName());
        assertEquals("appointment status 3", appointmentStatuses.get(2).getName());

    }

    @Test
    public void testGetVisitCancelStatuses() {

        this.dataPopulator.cancellationStatusPopulator.populateMany(1, 3);

        List<CancellationStatus> cancellationStatuses = dao.getVisitCancelStatuses();

        assertEquals(3, cancellationStatuses.size());
        assertEquals("cancellation status 1", cancellationStatuses.get(0).getName());
        assertEquals("cancellation status 2", cancellationStatuses.get(1).getName());
        assertEquals("cancellation status 3", cancellationStatuses.get(2).getName());

    }

    @Test
    public void testGetIRBInstitutions() {

        this.dataPopulator.irbInstitutionPopulator.populateMany(1, 3);

        List<IRBInstitution> irbInstitutions = dao.getIRBInstitutions();

        assertEquals(3, irbInstitutions.size());
        assertEquals("IRB Institution 1", irbInstitutions.get(0).getName());
        assertEquals("IRB Institution 2", irbInstitutions.get(1).getName());
        assertEquals("IRB Institution 3", irbInstitutions.get(2).getName());

    }

    @Test
    public void testGetStudies() {

        this.dataPopulator.studyPopulator.populateMany(1, 3);

        List<Study> studies = dao.getStudies();

        assertEquals(3, studies.size());
        assertEquals("study 1", studies.get(0).getName());
        assertEquals("study 2", studies.get(1).getName());
        assertEquals("study 3", studies.get(2).getName());

    }

    @Test
    public void testGetNonClosedStudies() {

        StudyStatus openStatus = this.dataPopulator.studyStatusPopulator.populateOne(1);
        StudyStatus closedStatus = this.dataPopulator.studyStatusPopulator.populateOne(3);
        closedStatus.setIsClosed(true);
        save(closedStatus);

        Study openStudy1 = this.dataPopulator.studyPopulator.populateOne(1);
        openStudy1.setStudyStatus(openStatus);
        save(openStudy1);

        Study closedStudy = this.dataPopulator.studyPopulator.populateOne(2);
        closedStudy.setStudyStatus(closedStatus);
        save(closedStudy);

        Study openStudy2 = this.dataPopulator.studyPopulator.populateOne(3);
        openStudy2.setStudyStatus(openStatus);
        save(closedStudy);

        GetStudiesResponse studiesDTO = dao.getNonClosedStudies();

        List<GetStudiesResponse.Study1> retreivedStudies = studiesDTO.getStudies();

        assertEquals(2, retreivedStudies.size());
        assertEquals("study 1", retreivedStudies.get(0).name);
        assertEquals("study 3", retreivedStudies.get(1).name);

    }


    @Test
    public void testIsStudyByPersonAndStudy() {

        User user = this.dataPopulator.userPopulator.populateOneWithDependencies(1);

        StudyStatus studyStatus = this.dataPopulator.studyStatusPopulator.populateOne(1);
        studyStatus.setIsClosed(false);
        save(studyStatus);

        Study study = this.dataPopulator.studyPopulator.populateOne(1);
        study.setStudyStatus(studyStatus);

        // CASE 1: user is not associated to the study

        boolean userFound = dao.isStudyByPersonAndStudy(user, study);

        assertFalse(userFound);

        // CASE 2: put the user on the study

        StudyUser studyUser = this.dataPopulator.studyUserPopulator.populateOne(study, user);
        studyUser.setActive(true);
        save(studyUser);

        userFound = dao.isStudyByPersonAndStudy(user, study);

        assertTrue(userFound);

        // CASE 3: user is on the study, but is inactive

        studyUser.setActive(false);

        userFound = dao.isStudyByPersonAndStudy(user, study);

        assertFalse(userFound);

    }

    @Test
    public void testIfBookedVisitsHaveVisit() {

        VisitTemplate visitTemplate = this.dataPopulator.visitTemplatePopulator.populateOneWithDependencies(
                1,
                true,
                true,
                false
        );

        // CASE 1: no booked visit

        boolean result = dao.ifBookedVisitsHaveVisit(visitTemplate);

        assertFalse(result);

        // CASE 2: create a booked visit

        BookedVisit bookedVisit = this.dataPopulator.bookedVisitPopulator.populateOneWithDependencies(visitTemplate);

        result = dao.ifBookedVisitsHaveVisit(visitTemplate);

        assertTrue(result);

    }

    @Test
    public void testFindStudyByName() {

        String studyName = "study 1";

        List<Study> studies = dataPopulator.studyPopulator.populateMany(1, 4);
        studies.get(3).setLocalId("another 3");

        List<Study> foundStudies = dao.findStudyByName("3");

        assertEquals(2, foundStudies.size());
        assertEquals("local id 3", foundStudies.get(0).getLocalId());
        assertEquals("another 3", foundStudies.get(1).getLocalId());

    }

    @Test
    public void testfindStudyDataById() {

        List<Study> studies = dataPopulator.studyPopulator.populateMany(1, 2);

        VisitType visitType = dataPopulator.visitTypePopulator.populateOne(1);

        VisitTemplate visitTemplate1 = this.dataPopulator.visitTemplatePopulator.populateOne(
                1,
                studies.get(0),
                visitType,
                true,
                false,
                false
        );
        VisitTemplate visitTemplate2 = this.dataPopulator.visitTemplatePopulator.populateOne(
                2,
                studies.get(0),
                visitType,
                true,
                false,
                false
        );
        VisitTemplate visitTemplate3 = this.dataPopulator.visitTemplatePopulator.populateOne(
                3,
                studies.get(1),
                visitType,
                true,
                false,
                false
        );

        AppointmentStatus appointmentStatus = dataPopulator.appointmentStatusPopulator.populateOne(1);

        Date startTime1 = new Date();
        Date endTime1 = new Date(startTime1.getTime() + 1000);
        Date startTime2 = new Date(startTime1.getTime() + 3000);
        Date endTime2 = new Date(startTime1.getTime() + 2000);
        Date startTime3 = new Date(startTime1.getTime() + 4000);
        Date endTime3 = new Date(startTime1.getTime() + 5000);

        // CASE 1: no booked visit

        Study retrievedStudy = dao.findStudyDataById(studies.get(0).getId());

        assertEquals("study 1", retrievedStudy.getName());
        assertNull(retrievedStudy.getFirstVisitDate());
        assertNull(retrievedStudy.getLastScheduledVisitDate());

        // CASE 2: create a booked visit

        BookedVisit bookedVisit1 = this.dataPopulator.bookedVisitPopulator.populateOne(
                studies.get(0),
                visitType,
                visitTemplate1,
                appointmentStatus,
                startTime1,
                endTime1,
                null,
                null,
                null,
                null,
                null,
                null
        );
        BookedVisit bookedVisit2 = this.dataPopulator.bookedVisitPopulator.populateOne(
                studies.get(0),
                visitType,
                visitTemplate1,
                appointmentStatus,
                startTime2,
                endTime2,
                null,
                null,
                null,
                null,
                null,
                null
        );
        BookedVisit bookedVisit3 = this.dataPopulator.bookedVisitPopulator.populateOne(
                studies.get(1),
                visitType,
                visitTemplate2,
                appointmentStatus,
                startTime3,
                endTime3,
                null,
                null,
                null,
                null,
                null,
                null
        );


        retrievedStudy = dao.findStudyDataById(studies.get(0).getId());

        assertEquals("study 1", retrievedStudy.getName());
        assertEquals(startTime1.getTime(), retrievedStudy.getFirstVisitDate().getTime());
        assertEquals(startTime2.getTime(), retrievedStudy.getLastScheduledVisitDate().getTime());

    }

    @Test
    public void testFindByStudyMemberId() {

        User user1 = dataPopulator.userPopulator.populateOneWithDependencies(1);
        User user2 = dataPopulator.userPopulator.populateOneWithDependencies(2);

        User retrievedUser = dao.findByStudyMemberId(user1.getId());

        assertEquals(user1, retrievedUser);

    }

    @Test
    public void testFindStudyUserByStudyAndUser() {

        User user1 = dataPopulator.userPopulator.populateOneWithDependencies(1);
        User user2 = dataPopulator.userPopulator.populateOneWithDependencies(2);

        Study study = dataPopulator.studyPopulator.populateOne(1);

        StudyUser studyUser1 = dataPopulator.studyUserPopulator.populateOne(study, user1);
        StudyUser studyUser2 = dataPopulator.studyUserPopulator.populateOne(study, user2);

        StudyUser retrievedStudyUser = dao.findStudyUserByStudyAndUser(study, user1);

        assertEquals(studyUser1, retrievedStudyUser);

    }

    @Test
    public void testFindGenderById() {

        Gender gender1 = dataPopulator.genderPopulator.populateOne(1);
        Gender gender2 = dataPopulator.genderPopulator.populateOne(2);

        Gender retrievedGender = dao.findGenderById(gender2.getId());

        assertEquals(gender2, retrievedGender);

    }

    @Test
    public void testFindStudySubjectById() {

        Study study1 = dataPopulator.studyPopulator.populateOne(1);

        Gender gender1 = dataPopulator.genderPopulator.populateOne(1);

        Subject subject1 = dataPopulator.subjectPopulator.populateOne(
                1,
                gender1,
                GenderType.F
        );

        SubjectMrn subjectMrn1a = dataPopulator.subjectMrnPopulator.populateOne(
                subject1,
                "MRN1a"
        );
        SubjectMrn subjectMrn1b = dataPopulator.subjectMrnPopulator.populateOne(
                subject1,
                "MRN1b"
        );

        StudySubject studySubject1a = dataPopulator.studySubjectPopulator.populateOne(
                study1,
                subjectMrn1a
        );
        StudySubject studySubject1b = dataPopulator.studySubjectPopulator.populateOne(
                study1,
                subjectMrn1b
        );

        StudySubject retrievedStudySubject = dao.findStudySubjectById(studySubject1a.getId());

        assertEquals(studySubject1a, retrievedStudySubject);

    }

    @Test
    public void testFindInstitutionRoleByType() {

        InstitutionRoleType roleType = InstitutionRoleType.ROLE_FRONT_DESK;

        InstitutionRole role1 = dataPopulator.institutionRolePopulator.populateOne(1);
        role1.setType(roleType);
        save(role1);

        InstitutionRole retrievedRole = dao.findInstitutionRoleByType(roleType);

        assertEquals(role1, retrievedRole);

        InstitutionRole role2 = dataPopulator.institutionRolePopulator.populateOne(1);
        role2.setType(roleType);
        save(role2);

        try {
            dao.findInstitutionRoleByType(roleType);
            assertTrue("Should have thrown an exception", false);
        }
        catch (Throwable t) {
            assertEquals("query did not return a unique result: 2", t.getMessage());
        }

    }

    @Test
    public void testGetNotApprovedVisits() {

        User user1 = dataPopulator.userPopulator.populateOneWithDependencies(1);
        User user2 = dataPopulator.userPopulator.populateOneWithDependencies(1);

        // also populates a study, a visit type and a sublocation
        VisitTemplate visitTemplate1 = this.dataPopulator.visitTemplatePopulator.populateOneWithDependencies(
                1,
                false,
                true,
                true
        );
        Study study1 = visitTemplate1.getStudy();
        study1.setInvestigator(user1);
        save(study1);

        VisitTemplate visitTemplate2 = this.dataPopulator.visitTemplatePopulator.populateOneWithDependencies(
                2,
                false,
                true,
                true
        );
        Study study2 = visitTemplate2.getStudy();

        VisitTemplate visitTemplate3 = this.dataPopulator.visitTemplatePopulator.populateOneWithDependencies(
                1,
                true,
                true,
                true
        );

        String sortBy = "visitTemplate.name";
        String orderBy = "ASC";
        int page = 1;
        int maxResults = 100;

        List<VisitTemplatesResponse> responses = dao.getNotApprovedVisits(
                sortBy, orderBy, page, maxResults
        );

        assertEquals(2, responses.size());
        VisitTemplatesResponse response1 = responses.get(0);
        VisitTemplatesResponse response2 = responses.get(1);

        assertEquals(visitTemplate1.getId(), (Integer) response1.id);
        assertEquals(study1.getId(), (Integer) response1.studyId);
        assertEquals(visitTemplate1.getName(), response1.visitName);
        assertEquals(visitTemplate1.getVisitType().getName(), response1.visitTypeName);
        assertEquals(visitTemplate1.getSublocation().getName(), response1.sublocationName);
        assertEquals("Not Approved", response1.finalApprovalStatus);
        assertEquals((Long) 2L, response1.totalCount);
        assertEquals(study1.getLocalId(), response1.localId);
        assertEquals(visitTemplate1.getLastUpdateTime(), response1.lastUpdateTime);
        assertEquals(user1.getLastName() + " " + user1.getFirstName(), response1.piName);

        assertEquals(visitTemplate2.getId(), (Integer) response2.getId());
        assertEquals(study2.getId(), (Integer) response2.studyId);
        assertEquals(visitTemplate2.getName(), response2.visitName);
        assertEquals(visitTemplate2.getVisitType().getName(), response2.visitTypeName);
        assertEquals(visitTemplate2.getSublocation().getName(), response2.sublocationName);
        assertEquals("Not Approved", response2.finalApprovalStatus);
        assertEquals((Long) 2L, response2.totalCount);
        assertEquals(study2.getLocalId(), response2.localId);
        assertEquals(visitTemplate2.getLastUpdateTime(), response2.lastUpdateTime);
        assertEquals(" ", response2.piName);

    }

    @Test
    public void testFindStudyUserRolesByStudy() {

        Study study1 = dataPopulator.studyPopulator.populateOne(1);
        Study study2 = dataPopulator.studyPopulator.populateOne(2);

        User user1a = dataPopulator.userPopulator.populateOneWithDependencies(1);
        User user1b = dataPopulator.userPopulator.populateOneWithDependencies(2);
        User user2 = dataPopulator.userPopulator.populateOneWithDependencies(3);
        User user3 = dataPopulator.userPopulator.populateOneWithDependencies(4);

        StudyUser studyUser1a = dataPopulator.studyUserPopulator.populateOne(
                study1, user1a
        );
        StudyUser studyUser1b = dataPopulator.studyUserPopulator.populateOne(
                study1, user1b
        );
        StudyUser studyUser2 = dataPopulator.studyUserPopulator.populateOne(
                study2, user2
        );

        List<StudyUser> retrievedStudyUsers = dao.findStudyUserRolesByStudy(study1);

        assertEquals(2, retrievedStudyUsers.size());

        assertEquals(user1a, retrievedStudyUsers.get(0).getUser());
        assertEquals(study1, retrievedStudyUsers.get(0).getStudy());

        assertEquals(user1b, retrievedStudyUsers.get(1).getUser());
        assertEquals(study1, retrievedStudyUsers.get(1).getStudy());

    }

    @Test
    public void testFindVisitTemplateCommentsByVisit() {

        VisitTemplate visitTemplate1 = dataPopulator.visitTemplatePopulator.populateOneWithDependencies(
                1,
                false,
                false,
                false
        );

        VisitTemplate visitTemplate2 = dataPopulator.visitTemplatePopulator.populateOneWithDependencies(
                2,
                true,
                true,
                true
        );

        BookedVisit bookedVisit1a = dataPopulator.bookedVisitPopulator.populateOneWithDependencies(
                visitTemplate1
        );
        BookedVisit bookedVisit1b = dataPopulator.bookedVisitPopulator.populateOneWithDependencies(
                visitTemplate1
        );
        BookedVisit bookedVisit2 = dataPopulator.bookedVisitPopulator.populateOneWithDependencies(
                visitTemplate2
        );

        User user1 = dataPopulator.userPopulator.populateOneWithDependencies(1);
        User user2 = dataPopulator.userPopulator.populateOneWithDependencies(2);

        List<Comments> commentsList1a = dataPopulator.commentsPopulator.populateMany(
                1,
                3,
                bookedVisit1a,
                user1
        );
        List<Comments> commentsList1b = dataPopulator.commentsPopulator.populateMany(
                4,
                1,
                bookedVisit1b,
                user2
        );

        List<Comments> retrievedComments = dao.findVisitTemplateCommentsByVisit(visitTemplate1);
        Long numberOfComments = dao.findNumVisitTemplateCommentsByVisit(visitTemplate1.getId());

        assertEquals(4, retrievedComments.size());
        assertEquals((Long) 4L, numberOfComments);

        retrievedComments = dao.findVisitTemplateCommentsByVisit(visitTemplate2);
        numberOfComments = dao.findNumVisitTemplateCommentsByVisit(visitTemplate2.getId());

        assertEquals(0, retrievedComments.size());
        assertEquals((Long) 0L, numberOfComments);

    }

    @Test
    public void testcheckLocalId() {

        List<Study> studies = dataPopulator.studyPopulator.populateMany(
                1,
                5
        );

        boolean localIdIsFree = dao.checkLocalId("local id 5");

        assertFalse(localIdIsFree);

        localIdIsFree = dao.checkLocalId("local id 6");

        assertTrue(localIdIsFree);

    }

    @Test
    public void testFindVisitTemplateByStudy() {

        VisitType visitType = this.dataPopulator.visitTypePopulator.populateOne(1);
        Sublocation sublocation = this.dataPopulator.sublocationPopulator.populateOneWithDependencies(1);
        List<Study> studies = dataPopulator.studyPopulator.populateMany(
                1,
                2
        );

        VisitTemplate visitTemplate1 = dataPopulator.visitTemplatePopulator.populateOne(
                1,
                studies.get(0),
                visitType,
                false,
                false,
                true
        );
        VisitTemplate visitTemplate2 = dataPopulator.visitTemplatePopulator.populateOne(
                2,
                studies.get(0),
                visitType,
                true,
                false,
                true
        );
        VisitTemplate visitTemplate3 = dataPopulator.visitTemplatePopulator.populateOne(
                2,
                studies.get(0),
                visitType,
                false,
                false,
                true
        );
        VisitTemplate visitTemplate4 = dataPopulator.visitTemplatePopulator.populateOne(
                3,
                studies.get(1),
                visitType,
                true,
                false,
                true
        );

        List<VisitTemplate> retrievedTemplates = dao.findVisitTemplateByStudy(studies.get(0));

        assertEquals(2, retrievedTemplates.size());
        assertEquals(visitTemplate1, retrievedTemplates.get(0));
        assertEquals(visitTemplate3, retrievedTemplates.get(1));

    }

    @Test
    public void testFindOpenStudyListByPerson() {

        User user1 = dataPopulator.userPopulator.populateOneWithDependencies(1);
        User user2 = dataPopulator.userPopulator.populateOneWithDependencies(2);
        User user3 = dataPopulator.userPopulator.populateOneWithDependencies(3);
        User user4 = dataPopulator.userPopulator.populateOneWithDependencies(4);

        VisitType visitType = this.dataPopulator.visitTypePopulator.populateOne(1);
        Sublocation sublocation = this.dataPopulator.sublocationPopulator.populateOneWithDependencies(1);
        List<Study> studies = dataPopulator.studyPopulator.populateMany(
                1,
                3
        );
        Study study1 = studies.get(0);
        Study study2 = studies.get(1);
        Study study3 = studies.get(2);

        StudyStatus openStudyStatus = dataPopulator.studyStatusPopulator.populateOne(1);
        openStudyStatus.setIsOpen(true);
        save(openStudyStatus);
        StudyStatus closedStudyStatus = dataPopulator.studyStatusPopulator.populateOne(2);
        closedStudyStatus.setIsClosed(true);
        save(closedStudyStatus);

        study1.setStudyStatus(openStudyStatus);
        study2.setStudyStatus(openStudyStatus);
        study3.setStudyStatus(closedStudyStatus);

        StudyUser studyUser1 = dataPopulator.studyUserPopulator.populateOne(study1, user1);
        StudyUser studyUser2 = dataPopulator.studyUserPopulator.populateOne(study1, user2);
        StudyUser studyUser3 = dataPopulator.studyUserPopulator.populateOne(study2, user1);
        StudyUser studyUser4 = dataPopulator.studyUserPopulator.populateOne(study2, user4);

        VisitTemplate visitTemplate1 = dataPopulator.visitTemplatePopulator.populateOne(
                1,
                study1,
                visitType,
                false,
                false,
                true
        );
        VisitTemplate visitTemplate2 = dataPopulator.visitTemplatePopulator.populateOne(
                2,
                study1,
                visitType,
                true,
                false,
                true
        );
        VisitTemplate visitTemplate3 = dataPopulator.visitTemplatePopulator.populateOne(
                2,
                study2,
                visitType,
                false,
                false,
                true
        );
        VisitTemplate visitTemplate4 = dataPopulator.visitTemplatePopulator.populateOne(
                3,
                study2,
                visitType,
                false,
                false,
                true
        );

        String sortBy = "s.localId";
        String orderBy = "DESC";
        int page = 1;
        int maxResults = 10;

        GetStudiesResponse response = dao.findOpenStudyListByPerson(
            user1,
            null,
            sortBy,
            orderBy,
            page,
            maxResults
        );

        List<GetStudiesResponse.Study1> retrievedStudies = response.getStudies();

        assertEquals(1, retrievedStudies.size());
        assertEquals("study 1", retrievedStudies.get(0).name);

        response = dao.findOpenStudyListByPerson(
                user1,
                "local id 1",
                sortBy,
                orderBy,
                page,
                maxResults
        );

        retrievedStudies = response.getStudies();

        assertEquals(1, retrievedStudies.size());
        assertEquals("study 1", retrievedStudies.get(0).name);

        response = dao.findOpenStudyListByPerson(
                user1,
                "local id 2",
                sortBy,
                orderBy,
                page,
                maxResults
        );

        retrievedStudies = response.getStudies();

        assertEquals(0, retrievedStudies.size());

    }

    @Test
    public void testGetOpenStudies() {

        VisitType visitType = this.dataPopulator.visitTypePopulator.populateOne(1);
        Sublocation sublocation = this.dataPopulator.sublocationPopulator.populateOneWithDependencies(1);
        List<Study> studies = dataPopulator.studyPopulator.populateMany(
                1,
                3
        );
        Study study1 = studies.get(0);
        Study study2 = studies.get(1);
        Study study3 = studies.get(2);

        StudyStatus openStudyStatus = dataPopulator.studyStatusPopulator.populateOne(1);
        openStudyStatus.setIsOpen(true);
        save(openStudyStatus);
        StudyStatus closedStudyStatus = dataPopulator.studyStatusPopulator.populateOne(2);
        closedStudyStatus.setIsClosed(true);
        save(closedStudyStatus);

        study1.setStudyStatus(openStudyStatus);
        study2.setStudyStatus(openStudyStatus);
        study3.setStudyStatus(closedStudyStatus);

        VisitTemplate visitTemplate1 = dataPopulator.visitTemplatePopulator.populateOne(
                1,
                study1,
                visitType,
                false,
                false,
                true
        );
        VisitTemplate visitTemplate2 = dataPopulator.visitTemplatePopulator.populateOne(
                2,
                study1,
                visitType,
                true,
                false,
                true
        );
        VisitTemplate visitTemplate3 = dataPopulator.visitTemplatePopulator.populateOne(
                2,
                study2,
                visitType,
                false,
                false,
                true
        );
        VisitTemplate visitTemplate4 = dataPopulator.visitTemplatePopulator.populateOne(
                3,
                study2,
                visitType,
                false,
                false,
                true
        );

        String sortBy = "s.localId";
        String orderBy = "DESC";
        int page = 1;
        int maxResults = 10;

        GetStudiesResponse response = dao.getOpenStudies(
                null,
                sortBy,
                orderBy,
                page,
                maxResults
        );

        List<GetStudiesResponse.Study1> retrievedStudies = response.getStudies();

        assertEquals(1, retrievedStudies.size());
        assertEquals("study 1", retrievedStudies.get(0).name);

        response = dao.getOpenStudies(
                "local id 1",
                sortBy,
                orderBy,
                page,
                maxResults
        );

        retrievedStudies = response.getStudies();

        assertEquals(1, retrievedStudies.size());
        assertEquals("study 1", retrievedStudies.get(0).name);

        response = dao.getOpenStudies(
                "local id 2",
                sortBy,
                orderBy,
                page,
                maxResults
        );

        retrievedStudies = response.getStudies();

        assertEquals(0, retrievedStudies.size());

    }

    @Test
    public void testFindStudySubjectBySubjectMrn() {

        Gender gender = dataPopulator.genderPopulator.populateOne(1);

        Subject subject1 = dataPopulator.subjectPopulator.populateOne(
                1,
                gender,
                GenderType.DECLINED
        );
        Subject subject2 = dataPopulator.subjectPopulator.populateOne(
                2,
                gender,
                GenderType.DECLINED
        );

        SubjectMrn subjectMrn1 = dataPopulator.subjectMrnPopulator.populateOne(
                subject1,
                "mrn 1"
        );
        SubjectMrn subjectMrn2 = dataPopulator.subjectMrnPopulator.populateOne(
                subject1,
                "mrn 2"
        );

        List<Study> studies = dataPopulator.studyPopulator.populateMany(
                1,
                3
        );
        Study study1 = studies.get(0);
        Study study2 = studies.get(1);
        Study study3 = studies.get(2);

        StudySubject studySubject1 = dataPopulator.studySubjectPopulator.populateOne(
                study1,
                subjectMrn1
        );
        StudySubject studySubject2 = dataPopulator.studySubjectPopulator.populateOne(
                study2,
                subjectMrn1
        );
        StudySubject studySubject3 = dataPopulator.studySubjectPopulator.populateOne(
                study3,
                subjectMrn2
        );

        List<StudySubject> studySubjects = dao.findStudySubjectBySubjectMrn(subjectMrn1);

        assertEquals(2, studySubjects.size());

    }

}

