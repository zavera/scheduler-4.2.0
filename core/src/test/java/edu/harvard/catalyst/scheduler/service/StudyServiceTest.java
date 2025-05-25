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

/**
 * Created with IntelliJ IDEA.
 * User: carl
 * Date: 11/6/13
 * Time: 4:33 PM
 */

import com.google.common.collect.Lists;
import edu.harvard.catalyst.scheduler.core.Statics;
import edu.harvard.catalyst.scheduler.dto.*;
import edu.harvard.catalyst.scheduler.dto.request.BooleanRequest;
import edu.harvard.catalyst.scheduler.dto.request.VisitTemplatesRequest;
import edu.harvard.catalyst.scheduler.dto.response.GetStudiesResponse;
import edu.harvard.catalyst.scheduler.dto.response.GetStudySubjectsResponse;
import edu.harvard.catalyst.scheduler.dto.response.StudyDataResponse;
import edu.harvard.catalyst.scheduler.dto.response.StudyDetailResponse;
import edu.harvard.catalyst.scheduler.dto.response.UserDataResponse;
import edu.harvard.catalyst.scheduler.dto.response.VisitApprovalModelResponseDTO;
import edu.harvard.catalyst.scheduler.entity.*;
import edu.harvard.catalyst.scheduler.persistence.TemplateResourceDAO;
import edu.harvard.catalyst.scheduler.util.TestUtils;
import edu.harvard.catalyst.scheduler.dto.response.FundingSourceInfo;
import edu.harvard.catalyst.scheduler.dto.response.StudyDTO;
import edu.harvard.catalyst.scheduler.dto.response.VisitTemplatesResponse;
import edu.harvard.catalyst.scheduler.persistence.AuthDAO;
import edu.harvard.catalyst.scheduler.persistence.StudyDAO;
import edu.harvard.catalyst.scheduler.persistence.SubjectDAO;
import edu.harvard.catalyst.scheduler.util.MailHandler;
import edu.harvard.catalyst.scheduler.util.ServiceTestsDAOUtility;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static edu.harvard.catalyst.scheduler.core.Statics.NA;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public final class StudyServiceTest {

    private StudyService studyService;

    @Mock private AuditService mockAuditService;
    @Mock private StudyDAO mockStudyDAO;
    @Mock private AuthDAO mockAuthDAO;
    @Mock private MailHandler mockMailHandler;
    @Mock private SubjectDAO mockSubjectDAO;
    @Mock private TemplateResourceDAO mockTemplateResourceDAO;

    @Before
    public void setUp() {
        studyService = new StudyService(
                mockAuditService,
                mockStudyDAO,
                mockAuthDAO,
                mockMailHandler,
                mockSubjectDAO,
                mockTemplateResourceDAO);
    }

    @Test
    public void testGetSubjectStudies() {
        final User user = Mockito.mock(User.class);
        Mockito.when(mockAuthDAO.findUserById(111)).thenReturn(user);
        Mockito.when(user.isStudyStaff()).thenReturn(true);

        final GetStudiesResponse result1 = this.studyService.getSubjectStudies(111);

        Assert.assertNull(result1);
        Mockito.verify(mockAuthDAO).findUserById(111);
        Mockito.verify(mockStudyDAO).findSubjectStudyListByPerson(user);

        Mockito.reset(mockAuthDAO);
        Mockito.reset(user);

        Mockito.when(mockAuthDAO.findUserById(222)).thenReturn(user);
        Mockito.when(user.isStudyStaff()).thenReturn(false);

        final GetStudiesResponse result2 = this.studyService.getSubjectStudies(222);

        Assert.assertNull(result2);
        Mockito.verify(mockAuthDAO).findUserById(222);
        Mockito.verify(mockStudyDAO).getNonClosedStudies();
    }

    @Test
    public void testGetOpenStudies() {
        final String filterString = "filter";
        final String sortBy = "sort";
        final String orderBy = "order";
        final int page = 1;
        final int maxResults = 10;
        final User user = Mockito.mock(User.class);
        Mockito.when(user.isStudyStaff()).thenReturn(true);

        final GetStudiesResponse getStudiesResponse1 = studyService.getOpenStudies(filterString, sortBy, orderBy,
                page, maxResults, user);

        Assert.assertNull(getStudiesResponse1);
        Mockito.verify(mockStudyDAO).findOpenStudyListByPerson(user, filterString, sortBy, orderBy, page, maxResults);
        Mockito.verify(mockStudyDAO, Mockito.never()).getOpenStudies(filterString, sortBy, orderBy, page, maxResults);
        Mockito.reset(mockStudyDAO);

        Mockito.when(user.isStudyStaff()).thenReturn(false);

        final GetStudiesResponse getStudiesResponse2 = studyService.getOpenStudies(filterString, sortBy, orderBy,
                page, maxResults, user);

        Assert.assertNull(getStudiesResponse2);
        Mockito.verify(mockStudyDAO, Mockito.never()).findOpenStudyListByPerson(user, filterString, sortBy, orderBy, page, maxResults);
        Mockito.verify(mockStudyDAO).getOpenStudies(filterString, sortBy, orderBy, page, maxResults);
    }

    @Test
    public void testCreateStudy() {
        final User user = ServiceTestsDAOUtility.createMockUser();
        final StudyDTO studyDTO1 = new StudyDTO();
        studyDTO1.setId(111);
        Mockito.when(mockStudyDAO.checkLocalId(studyDTO1.getLocalId())).thenReturn(false);

        final StudyDTO result1 = studyService.createStudy(studyDTO1, user, "ipAddress");

        Assert.assertNotNull(result1);
        Assert.assertTrue(studyDTO1 == result1);
        Assert.assertEquals(result1.isResult(), false);
        Assert.assertEquals(result1.getErrorMsg(), "Local Id already exists");

        Mockito.reset(mockStudyDAO);

        final StudyDTO studyDTO2 = new StudyDTO();
        studyDTO2.setId(112);
        studyDTO2.setName("This is a name");
        studyDTO2.setIndustryInitiated("Yes");
        final List<FundingSourceInfo> fundList = new ArrayList<>();
        final FundingSourceInfo fundingSourceInfo = new FundingSourceInfo(22, "fund name", "fund comment", 2,
                "fund grant", "fund center", 1.1, 2.2, new Date(), new Date(), 11, "fund center and institute");

        final FundingSource fundingSource = new FundingSource();
        fundingSource.setName(fundingSourceInfo.getName());
        fundList.add(fundingSourceInfo);
        studyDTO2.setFundingSourceInfoList(fundList);
        final StudyStatus studyStatus = new StudyStatus(2, "study name", "sn", false, true, false);

        Mockito.when(mockStudyDAO.checkLocalId(studyDTO2.getLocalId())).thenReturn(true);
        Mockito.when(mockStudyDAO.findFundingSourceById(fundingSourceInfo.getId())).thenReturn(fundingSource);
        Mockito.when(mockStudyDAO.findStudyStatusById(studyDTO2.getStudyStatus())).thenReturn(studyStatus);
        Mockito.doAnswer(new MockStudyAnswer()).when(mockStudyDAO).createEntity(Mockito.any(Study.class));

        final StudyDTO result2 = studyService.createStudy(studyDTO2, user, "ipAddress");

        Mockito.verify(mockStudyDAO, Mockito.times(2)).createEntity(Mockito.anyObject());
        Mockito.verify(mockStudyDAO).checkLocalId(studyDTO2.getLocalId());

        Assert.assertNotNull(result2);
        Assert.assertTrue(result2 == studyDTO2);
        Assert.assertTrue(result2.isResult());
    }

    @Test
    public void testCreateVisit() {
        final VisitDTO visit = new VisitDTO();
        visit.setSublocation(100);
        visit.setVisitType(101);
        visit.setComment("");
        final User user = ServiceTestsDAOUtility.createMockUser();
        final String ipAddress = "Apple is like a ship with a hole in the bottom, leaking water and my job is to " +
                "get the ship pointed in the right direction";

        final VisitType visitType101 = new VisitType();

        Mockito.when(mockStudyDAO.findStudyById(visit.getId())).thenReturn(new Study());
        Mockito.when(mockStudyDAO.findSubLocationById(visit.getSublocation())).thenReturn(new Sublocation("portal",
                new Institution("aperture science", "as")));

        Mockito.when(mockStudyDAO.findVisitTypeById(visit.getVisitType())).thenReturn(visitType101);

        final VisitTemplate result = this.studyService.createVisit(visit, user, ipAddress);

        Assert.assertNotNull(result);
        Mockito.verify(mockStudyDAO).createEntity(Mockito.any(VisitTemplate.class));
        Mockito.verify(mockAuditService).logVisitActivity(Mockito.anyString(), Mockito.any(VisitTemplate.class),
                Mockito.any(User.class), Mockito.anyString(), Mockito.any(), Mockito.any());
    }

    @Test
    public void testCopyVisit() {
        final VisitDTO visitDTO = new VisitDTO();
        visitDTO.setId(404);
        final User user = ServiceTestsDAOUtility.createMockUser();
        final String ipAddress = "There is no reason for any individual to have a computer in his home.";
        final VisitTemplate visitTemplate = new VisitTemplate();

        Mockito.when(mockStudyDAO.findVisitById(visitDTO.getId())).thenReturn(visitTemplate);

        final VisitTemplate result = this.studyService.copyVisit(visitDTO, user, ipAddress);

        Assert.assertNotNull(result);
        Mockito.verify(mockStudyDAO).createEntity(Mockito.any(VisitTemplate.class));
        Mockito.verify(mockAuditService).logVisitActivity(Mockito.anyString(), Mockito.any(VisitTemplate.class),
                Mockito.any(User.class), Mockito.anyString(), Mockito.any(), Mockito.any());

    }

    @Test
    public void testUpdateVisit() {
        final VisitDTO visitDTO = new VisitDTO();

        final VisitType newVisitTypeInpatient = new VisitType();
        newVisitTypeInpatient.setInpatient(true);
        newVisitTypeInpatient.setName(TestUtils.InpatientCRC);

        final VisitType oldVisitTypeOutpatient = new VisitType();
        oldVisitTypeOutpatient.setInpatient(false);
        oldVisitTypeOutpatient.setName(TestUtils.OutpatientNonCRC);

        visitDTO.setSublocation(100);
        visitDTO.setVisitType(2);
        visitDTO.setName("I'll make a GUI interface using visual basic to track the killers IP address");
        final User user = ServiceTestsDAOUtility.createMockUser();
        final String ipAddress = "All unit tests and no play makes jack a dull boy.";
        final VisitTemplate visitTemplate = new VisitTemplate();
        visitTemplate.setName("It's a Unix system! I know this!");
        visitTemplate.setVisitType(newVisitTypeInpatient);

        Mockito.when(mockStudyDAO.findVisitById(visitDTO.getId())).thenReturn(visitTemplate);
        Mockito.when(mockStudyDAO.findVisitTypeById(visitDTO.getVisitType())).thenReturn(oldVisitTypeOutpatient);

        final VisitTemplate result = this.studyService.updateVisit(visitDTO, user, ipAddress);

        Assert.assertNotNull(result);
        Assert.assertTrue(result == visitTemplate);
        Mockito.verify(mockStudyDAO).updateEntity(visitTemplate);
        Mockito.verify(mockAuditService).logVisitActivity(ipAddress, visitTemplate,
                user, Statics.AUDIT_VISIT_UPDATE, " Visit Name: It's a Unix system! I know this! to I'll make a GUI " +
                        "interface using visual basic to track the killers IP address,  Visit Type: Inpatient CRC to " +
                        "Outpatient Non CRC,  Sub-location:  NULL , ", "");
    }

    @Test
    public void testAddVisitCommentIfPresent() {
        StudyService serviceSpy = spy(studyService);

        VisitTemplate mockVisitTemplate = mock(VisitTemplate.class);
        User user = new User();
        String address = "addr";
        String comment = null;

        serviceSpy.addVisitCommentIfPresent(mockVisitTemplate, comment, user, address);
        verify(serviceSpy, times(0)).createVisitComments(comment, user, address, mockVisitTemplate);
        verify(mockVisitTemplate, times(0)).setComment(comment);

        comment = "";
        serviceSpy.addVisitCommentIfPresent(mockVisitTemplate, comment, user, address);
        verify(serviceSpy, times(0)).createVisitComments(comment, user, address, mockVisitTemplate);
        verify(mockVisitTemplate, times(0)).setComment(comment);

        comment = "non-empty";
        serviceSpy.addVisitCommentIfPresent(mockVisitTemplate, comment, user, address);
        verify(serviceSpy, times(1)).createVisitComments(comment, user, address, mockVisitTemplate);
        verify(mockVisitTemplate, times(1)).setComment(comment);
    }

    @Test
    public void testUpdateStudy() {
        final User user = ServiceTestsDAOUtility.createMockUser();
        final StudyDTO studyDTO1 = new StudyDTO();
        studyDTO1.setLocalId("Local Id");
        studyDTO1.setId(233);
        final Study study1 = new Study();
        study1.setLocalId("Local different Id");

        Mockito.when(mockStudyDAO.findStudyById(233)).thenReturn(study1);
        Mockito.when(mockStudyDAO.checkLocalId("Local Id")).thenReturn(false);

        final StudyDTO result1 = studyService.updateStudy(studyDTO1, user, "");

        Assert.assertNotNull(result1);
        Assert.assertTrue(result1 == studyDTO1);
        Assert.assertFalse(studyDTO1.isResult());
        Assert.assertEquals(studyDTO1.getErrorMsg(), "Local Id already exists");

        Mockito.reset(mockStudyDAO);

        final StudyDTO studyDTO2 = new StudyDTO();
        studyDTO2.setLocalId("Local Id");
        studyDTO2.setId(333);
        studyDTO2.setIndustryInitiated("Yes");
        studyDTO2.setCatalystId("catId");
        studyDTO2.setName("study name");
        studyDTO2.setCrcCategory("");
        studyDTO2.setPediatric("");
        studyDTO2.setIrb("");
        final Study study2 = new Study();
        study2.setId(444);
        study2.setLocalId("Local different Ids");
        study2.setCatalystId("catIds");
        study2.setName("study names");
        study2.setCrcCategory("1");
        study2.setPediatric("2");
        study2.setIrb("3");

        final List<FundingSourceInfo> fundList = new ArrayList<>();
        final FundingSourceInfo fundingSourceInfo = new FundingSourceInfo(22, "fund name", "fund comment", 2,
                "fund grant", "fund center", 1.1, 2.2, new Date(), new Date(), 11, "fund center and institute");

        final FundingSource fundingSource = new FundingSource();
        fundingSource.setName(fundingSourceInfo.getName());
        fundList.add(fundingSourceInfo);
        studyDTO2.setFundingSourceInfoList(fundList);

        Mockito.when(mockStudyDAO.findStudyById(333)).thenReturn(study2);
        Mockito.when(mockStudyDAO.findFundingSourceById(fundingSourceInfo.getId())).thenReturn(fundingSource);
        Mockito.when(mockStudyDAO.checkLocalId("Local Id")).thenReturn(true);
        Mockito.doAnswer(new MockStudyAnswer()).when(mockStudyDAO).createEntity(Mockito.any(Study.class));

        final StudyDTO result2 = studyService.updateStudy(studyDTO2, user, "");

        Assert.assertNotNull(result2);
        Assert.assertTrue(result2 == studyDTO2);
        Assert.assertEquals(result2.getLocalId(), "Local Id");
        Assert.assertEquals(result2.getId(), 444);
        Assert.assertEquals(result2.getIndustryInitiated(), "Yes");
        Assert.assertEquals(result2.getCatalystId(), "catId");
        Assert.assertEquals(result2.getName(), "study name");
        Assert.assertEquals(result2.getCrcCategory(), "");
        Assert.assertEquals(result2.getPediatric(), "");
        Assert.assertEquals(result2.getIrb(), "");

        Mockito.verify(mockStudyDAO).findStudyById(333);
        Mockito.verify(mockStudyDAO).createEntity(Mockito.anyObject());
        Mockito.verify(mockStudyDAO).checkLocalId(studyDTO2.getLocalId());
    }

    @Test
    public void testGetStudySubjects() {
        final User user = ServiceTestsDAOUtility.createMockUser();
        user.setInstitutionRole(new InstitutionRole(234, InstitutionRoleType.ROLE_STUDY_STAFF, "", ""));
        final Optional<String> nullableFilterString = Optional.of("j");
        final Integer ofPage = 20;
        final Integer ofMaxResults= 0;
        final Optional<Integer> ofStudyId = Optional.of(333);
        final String ofSortBy = "sortBy";
        final String ofOrderBy = "orderBy";
        final Optional<User> ofUser = Optional.of(user);
        final Study study = new Study();
        final SearchDTO searchDTO = new SearchDTO();
        final List<SearchDTO.SearchItem> searchItems = new ArrayList<>();
        SearchDTO.SearchItem searchItem1 = new SearchDTO.SearchItem();
        searchItem1.setKey("mySearchItem");
        searchItem1.setValue("mySearchValue");
        SearchDTO.SearchItem searchItem2 = new SearchDTO.SearchItem();
        searchItem2.setKey("birthdate");
        searchItem2.setValue("10/10/2001");
        searchItems.add(searchItem1);
        searchItems.add(searchItem2);
        searchDTO.setSearchItems(searchItems);

        Mockito.when(mockStudyDAO.findStudyById(333)).thenReturn(study);
        Mockito.when(mockStudyDAO.isStudyByPersonAndStudy(user, study)).thenReturn(false);

        final GetStudySubjectsResponse result1 = this.studyService.getStudySubjects(nullableFilterString, ofPage,
                ofMaxResults, ofStudyId, ofSortBy, ofOrderBy, true, ofUser, null);

        Assert.assertNull(result1);
        Mockito.verify(mockStudyDAO).isStudyByPersonAndStudy(user, study);

        Mockito.reset(mockStudyDAO);

        final List<StudySubject> list = new ArrayList<>();
        user.setInstitutionRole(new InstitutionRole(234, InstitutionRoleType.ROLE_SUPER_ADMIN, "", ""));

        Mockito.when(mockStudyDAO.findStudyById(333)).thenReturn(study);
        Mockito.when(mockStudyDAO.getRawStudySubjectsByStudy(study, true, searchDTO)).thenReturn(list);

        final GetStudySubjectsResponse result2 = this.studyService.getStudySubjects(nullableFilterString, ofPage,
                ofMaxResults, ofStudyId, ofSortBy, ofOrderBy, true, ofUser, searchDTO);

        Assert.assertNotNull(result2);

        Mockito.verify(mockStudyDAO, Mockito.never()).isStudyByPersonAndStudy(user, study);
        Mockito.verify(mockStudyDAO).getRawStudySubjectsByStudy(study, true, searchDTO);
    }

    @Test
    public void testStampStudyAndLogStudyMemberActivity() {
        final Study study = Mockito.mock(Study.class);
        final User user = Mockito.mock(User.class);

        studyService.stampStudyAndLogStudyMemberActivity("ip", study, user, user, "action", "req", "prev");

        Mockito.verify(study).setStatusChange(Mockito.any(Date.class));
        Mockito.verify(mockStudyDAO).updateEntity(study);
        Mockito.verify(mockAuditService).logStudyMemberActivity("ip", study, user, user, "action", "req", "prev");
    }

    @Test
    public void testStampStudyAndLogStudyActivity() {
        final Study study = Mockito.mock(Study.class);
        final User user = Mockito.mock(User.class);

        studyService.stampStudyAndLogStudyActivity("ip", study, user, "action", "req", "prev");

        Mockito.verify(study).setStatusChange(Mockito.any(Date.class));
        Mockito.verify(mockStudyDAO).updateEntity(study);
        Mockito.verify(mockAuditService).logStudyActivity("ip", study, user, "action", "req", "prev");
    }

    @Test
    public void testGetStudyMembers() {
        final int studyId = 343;
        final Study study = new Study();
        final List<StudyUser> studyUsers = new ArrayList<>();

        Mockito.when(mockStudyDAO.findStudyById(studyId)).thenReturn(study);
        Mockito.when(mockStudyDAO.findStudyUserRolesByStudy(study)).thenReturn(studyUsers);

        final List<StudyUser> result = this.studyService.getStudyMembers(studyId);

        Assert.assertNotNull(result);
        Assert.assertTrue(result == studyUsers);
        Mockito.verify(mockStudyDAO).findStudyById(studyId);
        Mockito.verify(mockStudyDAO).findStudyUserRolesByStudy(study);
    }

    @Test
    public void testDeleteVisit() {
        final int visit = 777;
        final User user = ServiceTestsDAOUtility.createMockUser();
        final String ipAddress = "the ip address";
        final VisitTemplate visitTemplate = new VisitTemplate();
        visitTemplate.setName("visit name");
        final List<ActivityLog> activityLogs = new ArrayList<>();
        final List<TemplateApprovalHistory> templateApprovalHistories = new ArrayList<>();
        final List<Comments> comments = new ArrayList<>();
        final List<TemplateResource> resources = new ArrayList<>();

        Mockito.when(mockStudyDAO.findVisitById(visit)).thenReturn(visitTemplate);
        Mockito.when(mockStudyDAO.ifBookedVisitsHaveVisit(visitTemplate)).thenReturn(true);

        final BooleanRequest result1 = this.studyService.deleteVisit(visit, user, ipAddress);

        Assert.assertNotNull(result1);
        Assert.assertFalse(result1.isResult());
        Mockito.verify(mockStudyDAO, Mockito.never()).findActivityLogByVisit(visitTemplate);

        Mockito.reset(mockStudyDAO);

        Mockito.when(mockStudyDAO.findVisitById(visit)).thenReturn(visitTemplate);
        Mockito.when(mockStudyDAO.findActivityLogByVisit(visitTemplate)).thenReturn(activityLogs);
        Mockito.when(mockStudyDAO.findTemplateApprovalHistoryByVisit(visitTemplate)).thenReturn(templateApprovalHistories);
        Mockito.when(mockStudyDAO.findVisitTemplateCommentsByVisit(visitTemplate)).thenReturn(comments);
        Mockito.when(mockTemplateResourceDAO.findTemplateResourcesByVisit(visitTemplate)).thenReturn(resources);

        final BooleanRequest result2 = this.studyService.deleteVisit(visit, user, ipAddress);

        Assert.assertNotNull(result2);
        Assert.assertTrue(result2.isResult());
        Mockito.verify(mockStudyDAO).findActivityLogByVisit(visitTemplate);
        Mockito.verify(mockAuditService).logDeleteVisitActivity(ipAddress, visitTemplate.getName(),
                user, Statics.AUDIT_VISIT_DELETE, null, null);
        Mockito.verify(mockStudyDAO).deleteEntity(visitTemplate);
    }

    @Test
    public void testChangeVisitTemplateStatus() {
        final VisitTemplatesRequest visits = new VisitTemplatesRequest();
        final List<Integer> list = new ArrayList<>();
        list.add(123);
        visits.setVisitTemplatesId(list);
        final VisitTemplate visitTemplate = new VisitTemplate();

        final boolean activate = true;
        final User user = ServiceTestsDAOUtility.createMockUser();
        final String ipAddress= "that ip address";

        Mockito.when(mockStudyDAO.findVisitById(123)).thenReturn(visitTemplate);

        final BooleanResultDTO result1 = this.studyService.changeVisitTemplateStatus(visits, activate, user, ipAddress);

        Assert.assertNotNull(result1);
        Assert.assertTrue(result1.isResult());
        Assert.assertTrue(visitTemplate.getActive());
        Mockito.verify(mockStudyDAO).updateEntity(visitTemplate);
        Mockito.verify(mockAuditService).logVisitActivity(ipAddress, visitTemplate, user, Statics.AUDIT_VISIT_ACTIVATE, null, null);

        Mockito.reset(mockStudyDAO);
        Mockito.reset(mockAuditService);

        Mockito.when(mockStudyDAO.findVisitById(123)).thenReturn(visitTemplate);

        final BooleanResultDTO result2 = this.studyService.changeVisitTemplateStatus(visits, false, user, ipAddress);

        Assert.assertNotNull(result2);
        Assert.assertTrue(result2.isResult());
        Assert.assertFalse(visitTemplate.getActive());
        Mockito.verify(mockStudyDAO).updateEntity(visitTemplate);
        Mockito.verify(mockAuditService).logVisitActivity(ipAddress, visitTemplate, user,
                Statics.AUDIT_VISIT_DEACTIVATE, null, null);

        Mockito.reset(mockStudyDAO);
        Mockito.reset(mockAuditService);

        visits.setVisitTemplatesId(new ArrayList<>());

        final BooleanResultDTO result3 = this.studyService.changeVisitTemplateStatus(visits, false, user, ipAddress);

        Assert.assertNotNull(result3);
        Assert.assertFalse(result3.isResult());
        Mockito.verify(mockStudyDAO, Mockito.never()).updateEntity(Mockito.any(BaseEntity.class));
    }

    @Test
    public void testStampStudyAndLogStudySubjectActivity() {
        final Study study = Mockito.mock(Study.class);
        final User user = Mockito.mock(User.class);
        final Subject subject = Mockito.mock(Subject.class);

        studyService.stampStudyAndLogStudySubjectActivity("ip", study, subject, user, "action", "req", "prev");

        Mockito.verify(study).setStatusChange(Mockito.any(Date.class));
        Mockito.verify(mockStudyDAO).updateEntity(study);
        Mockito.verify(mockAuditService).logStudySubjectActivity("ip", study, subject, user, "action", "req", "prev");
    }

    @Test
    public void testAddStudysVisitToList() {
        final User user = ServiceTestsDAOUtility.createMockUser();
        final Study study0 = Mockito.mock(Study.class);
        final Study study1 = Mockito.mock(Study.class);
        final Study study2 = Mockito.mock(Study.class);
        final List<Study> studyList = new ArrayList<>();
        studyList.add(study0);
        studyList.add(study1);
        studyList.add(study2);
        final List<TemplateApprovalHistory> userTemplateHistory = new ArrayList<>();

        final TemplateApprovalHistory templateApproval = Mockito.mock(TemplateApprovalHistory.class);


        final List<TemplateApprovalHistory> history = new ArrayList<>();
        history.add(templateApproval);

        final VisitTemplate visitTemplate0 = Mockito.mock(VisitTemplate.class);

        Mockito.when(visitTemplate0.getId()).thenReturn(1);
        Mockito.when(visitTemplate0.getName()).thenReturn("visit");

        final VisitType visitType = new VisitType();
        visitType.setName(TestUtils.InpatientCRC);
        visitType.setInpatient(true);

        Mockito.when(visitTemplate0.getVisitType()).thenReturn(visitType);

        final Sublocation sublocation = Mockito.mock(Sublocation.class);
        Mockito.when(visitTemplate0.getSublocation()).thenReturn(sublocation);
        Mockito.when(sublocation.getName()).thenReturn("sublocation");

        Mockito.when(visitTemplate0.getStudy()).thenReturn(study0);
        Mockito.when(study0.getLocalId()).thenReturn("local id");
        Mockito.when(study0.getInvestigator()).thenReturn(user);

        Mockito.when(visitTemplate0.getLastUpdateTime()).thenReturn(Calendar.getInstance().getTime());

        final List<VisitTemplate> oneVisitTemplateList = new ArrayList<>();
        oneVisitTemplateList.add(visitTemplate0);

        Mockito.when(mockStudyDAO.findTemplateApprovalHistoryListByUser(Mockito.any(User.class))).thenReturn(userTemplateHistory);

        // can't distinguish the 3 mock study's . eg, can't 'when' hashCode()
        Mockito.when(mockStudyDAO.findVisitTemplateByStudy(Mockito.any(Study.class))).thenReturn(oneVisitTemplateList);

        Mockito.when(mockStudyDAO.findTemplateApprovalHistoryListByVisitAndUser(visitTemplate0, user)).thenReturn(history);

        final List<VisitTemplatesResponse> visitTemplateListResult = studyService.addStudysVisitToList(studyList, new ArrayList<>(), Mockito.any(User.class));

        Mockito.verify(mockStudyDAO, Mockito.times(3)).findVisitTemplateByStudy(Mockito.any(Study.class));

        TestUtils.assertNonNullAndEqual(3, visitTemplateListResult.size());
    }

    private void helpTestSetSomeOtherUsersPresent(final StudyDTO studyDTO, final Study study) {
        Mockito.when(studyDTO.getPhysician()).thenReturn(1);
        Mockito.when(studyDTO.getPhysician2()).thenReturn(1);
        Mockito.when(studyDTO.getAssociateNurse()).thenReturn(1);
        Mockito.when(studyDTO.getAssociateProtocolNutritionist()).thenReturn(1);
        Mockito.when(studyDTO.getSecondaryScheduler()).thenReturn(1);
        Mockito.when(studyDTO.getScheduler3()).thenReturn(1);
        Mockito.when(studyDTO.getProtocolNurse()).thenReturn(1);
        Mockito.when(studyDTO.getProtocolNutritionist()).thenReturn(1);

        studyService.setSomeOtherUsers(studyDTO, study);

        Mockito.verify(study).setPhysician(Mockito.any(User.class));
        Mockito.verify(study).setPhysician2(Mockito.any(User.class));
        Mockito.verify(study).setAssociateNurse(Mockito.any(User.class));
        Mockito.verify(study).setAssociateProtocolNutritionist(Mockito.any(User.class));
        Mockito.verify(study).setSecondaryScheduler(Mockito.any(User.class));
        Mockito.verify(study).setScheduler3(Mockito.any(User.class));
        Mockito.verify(study).setProtocolNurse(Mockito.any(User.class));
        Mockito.verify(study).setProtocolNurseString(null);
        Mockito.verify(study).setProtocolNutritionist(Mockito.any(User.class));
        Mockito.verify(study).setProtocolNutritionistString(null);
    }

    private void helpTestSetSomeOtherUsersAbsent(final StudyDTO studyDTO, final Study study) {
        Mockito.when(studyDTO.getPhysician()).thenReturn(0);
        Mockito.when(studyDTO.getPhysician2()).thenReturn(0);
        Mockito.when(studyDTO.getAssociateNurse()).thenReturn(0);
        Mockito.when(studyDTO.getAssociateProtocolNutritionist()).thenReturn(0);
        Mockito.when(studyDTO.getSecondaryScheduler()).thenReturn(0);
        Mockito.when(studyDTO.getScheduler3()).thenReturn(0);
        Mockito.when(studyDTO.getProtocolNurse()).thenReturn(-1);
        Mockito.when(studyDTO.getProtocolNutritionist()).thenReturn(-1);

        studyService.setSomeOtherUsers(studyDTO, study);

        Mockito.verify(study).setPhysician(null);
        Mockito.verify(study).setPhysician2(null);
        Mockito.verify(study).setAssociateNurse(null);
        Mockito.verify(study).setAssociateProtocolNutritionist(null);
        Mockito.verify(study).setSecondaryScheduler(null);
        Mockito.verify(study).setScheduler3(null);
        Mockito.verify(study).setProtocolNurseString(NA);
        Mockito.verify(study).setProtocolNurse(null);
        Mockito.verify(study).setProtocolNutritionistString(NA);
        Mockito.verify(study).setProtocolNutritionist(null);
    }

    @Test
    public void testSetSomeOtherUsers() {
        final StudyDTO studyDTO = Mockito.mock(StudyDTO.class);
        final Study study = Mockito.mock(Study.class);
        final User user = Mockito.mock(User.class);
        Mockito.when(mockAuthDAO.findUserById(Mockito.anyInt())).thenReturn(user);

        helpTestSetSomeOtherUsersPresent(studyDTO, study);
        helpTestSetSomeOtherUsersAbsent(studyDTO, study);
    }

    @Test
    public void testGetVisitTemplateData() {
        final User user = Mockito.mock(User.class);
        studyService.getVisitTemplateData(1, user, "ipAddress");
        Mockito.verify(mockStudyDAO, Mockito.times(1)).getVisitDataById(1);
    }

    @Test
    public void testComparePreviousAndCurrentFundingSource() {
        final StudyFundingSource backEndStudyFundingSource = Mockito.mock(StudyFundingSource.class);
        final FundingSourceInfo frontEndFundingSourceInfo = Mockito.mock(FundingSourceInfo.class);
        final FundingSource fundingSource = Mockito.mock(FundingSource.class);
        final CentersAndInstitutions centersAndInstitutions = Mockito.mock(CentersAndInstitutions.class);

        final Date startDate = new Date(1000);
        final Date endDate = new Date(2000);

        Mockito.when(centersAndInstitutions.getName()).thenReturn("caiB");
        Mockito.when(fundingSource.getName()).thenReturn("fs");

        Mockito.when(backEndStudyFundingSource.getFundingSource()).thenReturn(fundingSource);
        Mockito.when(backEndStudyFundingSource.getCentersAndInstitutions()).thenReturn(centersAndInstitutions);

        Mockito.when(backEndStudyFundingSource.getGrantId()).thenReturn("g");
        Mockito.when(backEndStudyFundingSource.getSiteCostCenter()).thenReturn("cBack");
        Mockito.when(backEndStudyFundingSource.getTotalDirectAward()).thenReturn(1.0);
        Mockito.when(backEndStudyFundingSource.getTotalIndirectAward()).thenReturn(2.0);
        Mockito.when(backEndStudyFundingSource.getProjectStartDate()).thenReturn(startDate);
        Mockito.when(backEndStudyFundingSource.getProjectEndDate()).thenReturn(endDate);

        Mockito.when(frontEndFundingSourceInfo.getName()).thenReturn("fs");
        Mockito.when(frontEndFundingSourceInfo.getGrant()).thenReturn("g");
        Mockito.when(frontEndFundingSourceInfo.getCenter()).thenReturn("cFront");
        Mockito.when(frontEndFundingSourceInfo.getDirectAward()).thenReturn(1.0);
        Mockito.when(frontEndFundingSourceInfo.getIndirectAward()).thenReturn(3.0);
        Mockito.when(frontEndFundingSourceInfo.getStart()).thenReturn(startDate);
        Mockito.when(frontEndFundingSourceInfo.getEnd()).thenReturn(endDate);
        Mockito.when(frontEndFundingSourceInfo.getCenterAndInstitutionName()).thenReturn("caiFront");

        final String expected1 =
                " Funding Source : 1 fs to fs,  " +
                        "Funding Source GrantId: 1 g to g,  " +
                        "Funding Source Site Cost Center: 1 cBack to cFront,  " +
                        "Funding Source Direct Award: 1 1.0 to 1.0,  " +
                        "Funding Source Indirect Award: 1 2.0 to 3.0,  " +
                        "Funding Source Project Start Date: 1 Wed Dec 31 19:00:01 EST 1969 to Wed Dec 31 19:00:01 EST 1969,  " +
                        "Funding Source Project End Date: 1 Wed Dec 31 19:00:02 EST 1969 to Wed Dec 31 19:00:02 EST 1969,  " +
                        "Funding Source Centers And Institutes: 1 caiB to caiFront, ";
        final String result1 = studyService.comparePreviousAndCurrentFundingSource(backEndStudyFundingSource, frontEndFundingSourceInfo, 1);
        Assert.assertEquals(expected1, result1);

        final String expected2 =
                " Funding Source : 2  NULL  to fs,  " +
                        "Funding Source GrantId: 2  NULL  to g,  " +
                        "Funding Source Site Cost Center: 2  NULL  to cFront,  " +
                        "Funding Source Direct Award: 2  NULL  to 1.0,  " +
                        "Funding Source Indirect Award: 2  NULL  to 3.0,  " +
                        "Funding Source Project Start Date: 2  NULL  to Wed Dec 31 19:00:01 EST 1969,  " +
                        "Funding Source Project End Date: 2  NULL  to Wed Dec 31 19:00:02 EST 1969,  " +
                        "Funding Source Centers And Institutes: 2  NULL  to caiFront, ";
        final String result2 = studyService.comparePreviousAndCurrentFundingSource(null, frontEndFundingSourceInfo, 2);
        Assert.assertEquals(expected2, result2);

        final String expected3 =
                " Funding Source : 3 fs to  NULL ,  " +
                        "Funding Source GrantId: 3 g to  NULL ,  " +
                        "Funding Source Site Cost Center: 3 cBack to  NULL ,  " +
                        "Funding Source Direct Award: 3 1.0 to  NULL ,  " +
                        "Funding Source Indirect Award: 3 2.0 to  NULL ,  " +
                        "Funding Source Project Start Date: 3 Wed Dec 31 19:00:01 EST 1969 to  NULL ,  " +
                        "Funding Source Project End Date: 3 Wed Dec 31 19:00:02 EST 1969 to  NULL ,  " +
                        "Funding Source Centers And Institutes: 3 caiB to  NULL , ";
        final String result3 = studyService.comparePreviousAndCurrentFundingSource(backEndStudyFundingSource, null, 3);
        Assert.assertEquals(expected3, result3);

        // is this one possible in the app?
        final String expected4 =
                " Funding Source : 4  NULL  to  NULL ,  " +
                        "Funding Source GrantId: 4  NULL  to  NULL ,  " +
                        "Funding Source Site Cost Center: 4  NULL  to  NULL ,  " +
                        "Funding Source Direct Award: 4  NULL  to  NULL ,  " +
                        "Funding Source Indirect Award: 4  NULL  to  NULL ,  " +
                        "Funding Source Project Start Date: 4  NULL  to  NULL ,  " +
                        "Funding Source Project End Date: 4  NULL  to  NULL ,  " +
                        "Funding Source Centers And Institutes: 4  NULL  to  NULL , ";
        final String result4 = studyService.comparePreviousAndCurrentFundingSource(null, null, 4);
        Assert.assertEquals(expected4, result4);
    }

    @Test
    public void testAddToBuilderIfStringsDiff() {
        final StringBuilder diffBuilder = new StringBuilder();

        studyService.addToBuilderIfStringsDiff("L", diffBuilder, "f", "f");
        final String result = diffBuilder.toString();

        Assert.assertEquals("", result);

        studyService.addToBuilderIfStringsDiff("L ", diffBuilder, "f1", "f2");
        final String result2 = diffBuilder.toString();

        Assert.assertEquals("L f2 to f1, ", result2);
    }

    @Test
    public void testAddNameToBuilderIfStringsDiffEntityNonNull() {
        helperAddName(2, "L  NULL , ", true);

        helperAddName(1, "", false);
        helperAddName(2, "L Smith to Jones, ", false);
    }

    private void helperAddName(final Integer frontId, final String expected, final boolean isNull) {
        Mockito.when(mockStudyDAO.findNameById("User", 1, false)).thenReturn("Smith");

        User user = new User();
        user.setId(1);

        if (isNull) {
            user = null;
        }

        final StringBuilder diffBuilder = new StringBuilder();
        studyService.addNameToBuilderIfStringsDiffEntityNonNull("L ",
                user,
                diffBuilder,
                frontId,
                "Jones");

        final String result = diffBuilder.toString();
        Assert.assertEquals(expected, result);
    }

    @Test
    public void testAddFirstNameToBuilderIfStringsDiffEntityNonNull() throws Exception {
        helperAddFirstName(2, "L  NULL , ", true);

        helperAddFirstName(1, "", false);
        helperAddFirstName(2, "L Jim to Jones, ", false);
    }

    private void helperAddFirstName(final Integer frontId, final String expected, final boolean isNull) {
        Mockito.when(mockStudyDAO.findNameById("User", 1, true)).thenReturn("Jim");

        User user = new User();
        user.setId(1);

        if (isNull) {
            user = null;
        }

        final StringBuilder diffBuilder = new StringBuilder();
        studyService.addFirstNameToBuilderIfStringsDiffEntityNonNull("L ",
                user,
                diffBuilder,
                frontId,
                "Jones");

        final String result = diffBuilder.toString();
        Assert.assertEquals(expected, result);
    }

    @Test
    public void testFindNameByNonZeroId() {
        Mockito.when(mockStudyDAO.findNameById("User", 1, false)).thenReturn("Jones");

        final String result1 = studyService.findNameByNonZeroId(User.class, 0);
        Assert.assertNull(result1);

        final String result2 = studyService.findNameByNonZeroId(User.class, 1);
        Assert.assertEquals("Jones", result2);
    }

    @Test
    public void testFindFirstNameByNonZeroId() {
        Mockito.when(mockStudyDAO.findNameById("User", 1, true)).thenReturn("Jim");

        final String result1 = studyService.findFirstNameByNonZeroId(User.class, 0);
        Assert.assertNull(result1);

        final String result2 = studyService.findFirstNameByNonZeroId(User.class, 1);
        Assert.assertEquals("Jim", result2);
    }

    @Test
    public void testFindFirstNameNonMinusOne() {
        Mockito.when(mockStudyDAO.findNameById("User", 1, true)).thenReturn("Jim");

        final String result1 = studyService.findFirstNameByNonZeroNonMinusOneId(User.class, 0);
        Assert.assertNull(result1);

        final String result2 = studyService.findFirstNameByNonZeroNonMinusOneId(User.class, -1);
        Assert.assertEquals(NA, result2);

        final String result3 = studyService.findFirstNameByNonZeroNonMinusOneId(User.class, 1);
        Assert.assertEquals("Jim", result3);
    }

    private void testAddObjectHelper(final Object oneThang, final Object anotherThang, final String diffResult) {
        final StringBuilder diffBuilder = new StringBuilder();
        studyService.addToBuilderIfObjectsDiffNonNullBack("L ", diffBuilder, oneThang, anotherThang);
        Assert.assertEquals(diffResult, diffBuilder.toString());
    }
    @Test
    public void testAddToBuilderIfObjectsDifferNonNullBack() throws Exception {
        final Date oneDate = new Date(1000);
        final Date anotherDate = new Date(2000);

        testAddObjectHelper(oneDate, oneDate, "");
        testAddObjectHelper(null, anotherDate, "L Wed Dec 31 19:00:02 EST 1969 to null, ");
        testAddObjectHelper(oneDate, null, "");
        testAddObjectHelper(oneDate, anotherDate, "L Wed Dec 31 19:00:02 EST 1969 to Wed Dec 31 19:00:01 EST 1969, ");

        testAddObjectHelper(Boolean.TRUE, Boolean.TRUE, "");
        testAddObjectHelper(null, Boolean.FALSE, "L false to null, ");
        testAddObjectHelper(Boolean.TRUE, null, "");
        testAddObjectHelper(Boolean.TRUE, Boolean.FALSE, "L false to true, ");

        testAddObjectHelper("thang1", "thang1", "");
        testAddObjectHelper(null, "thang2", "L thang2 to null, ");
        testAddObjectHelper("thang1", null, "");
        testAddObjectHelper("thang1", "thang2", "L thang2 to thang1, ");
    }

    @Test
    public void testNullForZeroElseFind() {
        User user = studyService.nullForZeroFindUserForNonZeroId(0);

        Assert.assertNull(user);

        final User mockUser = Mockito.mock(User.class);
        Mockito.when(mockAuthDAO.findUserById(42)).thenReturn(mockUser);

        user = studyService.nullForZeroFindUserForNonZeroId(42);
        Assert.assertEquals(user, mockUser);
    }

    private VisitTemplatesResponse makeVisitTemplateResponse(final int id) {
        return new VisitTemplatesResponse(id, 1, "", "", "", "", 0L, "", null, "");
    }
    private void checkTotalsSizeFirstAndLast(final List<VisitTemplatesResponse> visitTemplatesResponseList,
            final int grandTotal,
            final int listSize,
            final int firstId,
            final int lastId) {

        Assert.assertEquals("List Size", listSize, visitTemplatesResponseList.size());

        for (final VisitTemplatesResponse visitTemplatesResponse : visitTemplatesResponseList) {
            Assert.assertEquals("Total in Visit", grandTotal, visitTemplatesResponse.getTotalCount());
        }

        Assert.assertEquals("First", firstId, visitTemplatesResponseList.get(0).getId());
        Assert.assertEquals("Last", lastId, visitTemplatesResponseList.get(listSize - 1).getId());
    }

    @Test
    public void testAdjustVisitsPerPageAndMaxResults() {

        final List<VisitTemplatesResponse> visitList = Lists.newArrayList(
                makeVisitTemplateResponse(10),
                makeVisitTemplateResponse(11),
                makeVisitTemplateResponse(12),
                makeVisitTemplateResponse(13));

        final int grandTotal = visitList.size();

        List<VisitTemplatesResponse> adjustedList;

        // page 1
        adjustedList = studyService.adjustVisitsPerPageAndMaxResults(1, 3, visitList);
        checkTotalsSizeFirstAndLast(adjustedList, grandTotal, 3, 10, 12);

        adjustedList = studyService.adjustVisitsPerPageAndMaxResults(1, 2, visitList);
        checkTotalsSizeFirstAndLast(adjustedList, grandTotal, 2, 10, 11);

        adjustedList = studyService.adjustVisitsPerPageAndMaxResults(1, 5, visitList);
        checkTotalsSizeFirstAndLast(adjustedList, grandTotal, 4, 10, 13);

        // page 2
        adjustedList = studyService.adjustVisitsPerPageAndMaxResults(2, 1, visitList);
        checkTotalsSizeFirstAndLast(adjustedList, grandTotal, 1, 11, 11);

        adjustedList = studyService.adjustVisitsPerPageAndMaxResults(2, 2, visitList);
        checkTotalsSizeFirstAndLast(adjustedList, grandTotal, 2, 12, 13);
    }

    @Test
    public void testSeeAllUnapprovedExceptUserApproved() {
        Assert.assertTrue(studyService.seeAllUnapprovedExceptUserApproved(RoleType.ROLE_NURSE_MANAGER_DIRECTOR, InstitutionRoleType.ROLE_SUPER_ADMIN)      );
        Assert.assertTrue(studyService.seeAllUnapprovedExceptUserApproved(RoleType.ROLE_NUTRITION_MANAGER      , InstitutionRoleType.ROLE_SUPER_ADMIN)      );
        Assert.assertTrue(studyService.seeAllUnapprovedExceptUserApproved(RoleType.ROLE_ADMINISTRATIVE_DIRECTOR, InstitutionRoleType.ROLE_SUPER_ADMIN)      );
        Assert.assertTrue(studyService.seeAllUnapprovedExceptUserApproved(RoleType.ROLE_SCHEDULER              , InstitutionRoleType.ROLE_SUPER_ADMIN)      );
        Assert.assertTrue(studyService.seeAllUnapprovedExceptUserApproved(RoleType.ROLE_NUTRITION_MANAGER      , InstitutionRoleType.ROLE_RESOURCE_MANAGER) );
        Assert.assertTrue(studyService.seeAllUnapprovedExceptUserApproved(RoleType.ROLE_NURSE_MANAGER_DIRECTOR , InstitutionRoleType.ROLE_RESOURCE_MANAGER) );
        Assert.assertTrue(studyService.seeAllUnapprovedExceptUserApproved(RoleType.ROLE_SCHEDULER              , InstitutionRoleType.ROLE_SCHEDULER)        );

        Assert.assertFalse(studyService.seeAllUnapprovedExceptUserApproved(RoleType.ROLE_NURSE_MANAGER_DIRECTOR, InstitutionRoleType.ROLE_SCHEDULER));
        Assert.assertFalse(studyService.seeAllUnapprovedExceptUserApproved(RoleType.ROLE_FINAL_APPROVER, InstitutionRoleType.ROLE_SUPER_ADMIN));
        Assert.assertFalse(studyService.seeAllUnapprovedExceptUserApproved(RoleType.ROLE_SCHEDULER, InstitutionRoleType.ROLE_STUDY_STAFF));

        Assert.assertFalse(studyService.seeAllUnapprovedExceptUserApproved(RoleType.ROLE_NURSE_MANAGER_DIRECTOR, null));
        Assert.assertFalse(studyService.seeAllUnapprovedExceptUserApproved(null, InstitutionRoleType.ROLE_SUPER_ADMIN));
        Assert.assertFalse(studyService.seeAllUnapprovedExceptUserApproved(null, null));
    }

    @Test
    public void testSeeAllUnapproved() {

        Assert.assertTrue(studyService.seeAllUnapproved(RoleType.ROLE_FINAL_APPROVER, InstitutionRoleType.ROLE_SUPER_ADMIN));

        Assert.assertFalse(studyService.seeAllUnapproved(RoleType.ROLE_FINAL_APPROVER, InstitutionRoleType.ROLE_RESOURCE_MANAGER));
        Assert.assertFalse(studyService.seeAllUnapproved(RoleType.ROLE_SCHEDULER, InstitutionRoleType.ROLE_SUPER_ADMIN));
        Assert.assertFalse(studyService.seeAllUnapproved(RoleType.ROLE_SCHEDULER, InstitutionRoleType.ROLE_RESOURCE_MANAGER));

        Assert.assertFalse(studyService.seeAllUnapproved(RoleType.ROLE_FINAL_APPROVER, null));
        Assert.assertFalse(studyService.seeAllUnapproved(null, InstitutionRoleType.ROLE_SUPER_ADMIN));
        Assert.assertFalse(studyService.seeAllUnapproved(null, null));
    }

    @Test
    public void testStampStudyAndLogStudySubjectMrnActivity() {
        final String ipAddress = "ip address here";
        final Study study = new Study();
        final SubjectMrn subjectMrn = new SubjectMrn();
        final User user = ServiceTestsDAOUtility.createMockUser();
        final String action = "action";
        final String requiredFieldPreviousData = "required field previous data";
        final String previousData = "prev date";

        this.studyService.stampStudyAndLogStudySubjectMrnActivity(ipAddress,
                study, subjectMrn, user, action, requiredFieldPreviousData, previousData);

        Mockito.verify(mockStudyDAO).updateEntity(study);
        Mockito.verify(mockAuditService).logStudyAndSubjectMrnActivity(
                ipAddress, study, subjectMrn, user, action, requiredFieldPreviousData, previousData);
    }

    @Test
    public void testAssignStudyMembers() {
        final User user = ServiceTestsDAOUtility.createMockUser();
        user.setInstitution(new Institution("", ""));
        final String ipAddress = "this ip address";
        final StringBuilder url = new StringBuilder(Paths.get(".").toAbsolutePath().normalize().toString());
        final int index = url.lastIndexOf("/") + 1;
        url.delete(index, url.length());
        url.append("web/src/main/webapp/WEB-INF");
        final List<Integer> studyMembers = new ArrayList<>();
        studyMembers.add(234);
        final AddStudyMembersDTO dto = new AddStudyMembersDTO();
        dto.setStudyId(654);
        dto.setStudyMembers(studyMembers);

        final Study study = new Study();

        Mockito.when(mockStudyDAO.findStudyById(dto.getStudyId())).thenReturn(study);
        Mockito.when(mockStudyDAO.findByStudyMemberId(234)).thenReturn(user);

        final BooleanResultDTO booleanResultDTO1 = this.studyService.assignStudyMembers(dto, user, ipAddress, url.toString());

        Assert.assertNotNull(booleanResultDTO1);
        Assert.assertTrue(booleanResultDTO1.isResult());
        Mockito.verify(mockStudyDAO).findStudyById(dto.getStudyId());
        Mockito.verify(mockStudyDAO).createEntity(Mockito.any(StudyUser.class));

        Mockito.reset(mockStudyDAO);

        dto.setStudyMembers(new ArrayList<>());
        Mockito.when(mockStudyDAO.findStudyById(dto.getStudyId())).thenReturn(study);
        Mockito.when(mockStudyDAO.findByStudyMemberId(234)).thenReturn(user);

        final BooleanResultDTO booleanResultDTO2 = this.studyService.assignStudyMembers(dto, user, ipAddress, url.toString());

        Assert.assertNotNull(booleanResultDTO2);
        Assert.assertTrue(booleanResultDTO2.isResult());
        Mockito.verify(mockStudyDAO).findStudyById(dto.getStudyId());
        Mockito.verify(mockStudyDAO, Mockito.never()).createEntity(Mockito.any(StudyUser.class));
    }

    @Test
    public void testGetVisitTemplateComments() {
        final int visitId = 456;
        final VisitTemplate visitTemplate = new VisitTemplate();
        final List<Comments> list = new ArrayList<>();

        Mockito.when(mockStudyDAO.findVisitById(456)).thenReturn(visitTemplate);
        Mockito.when(mockStudyDAO.findVisitTemplateCommentsByVisit(visitTemplate)).thenReturn(list);

        final List<Comments> result = this.studyService.getVisitTemplateComments(visitId);

        Assert.assertNotNull(result);
        Assert.assertTrue(result == list);
        Mockito.verify(mockStudyDAO).findVisitById(visitId);
        Mockito.verify(mockStudyDAO).findVisitTemplateCommentsByVisit(visitTemplate);
    }

    @Test
    public void testApproveVisitTemplate() {
        final TemplateApprovalHistoryDTO visit = new TemplateApprovalHistoryDTO();
        visit.setVisitTemplate(987);
        visit.setComment("this comment");
        final User user = ServiceTestsDAOUtility.createMockUser();
        user.setInstitution(new Institution("", ""));
        user.setRole(new Role());
        visit.setUser(user);
        final String ipAddress = "random ip address";
        final StringBuilder url = new StringBuilder(Paths.get(".").toAbsolutePath().normalize().toString());
        final int index = url.lastIndexOf("/") + 1;
        url.delete(index, url.length());
        url.append("web/src/main/webapp/WEB-INF");
        final VisitTemplate visitTemplate = new VisitTemplate();
        visitTemplate.setStudy(new Study());
        final TemplateApprovalHistory approvalHistory = new TemplateApprovalHistory();

        Mockito.when(mockStudyDAO.findVisitById(visit.getVisitTemplate())).thenReturn(visitTemplate);
        Mockito.when(mockAuthDAO.findUserById(visit.getUser().getId())).thenReturn(user);
        Mockito.when(mockStudyDAO.findTemplateApprovalHistoryByVisitAndUser(visitTemplate, user)).thenReturn(approvalHistory);

        final TemplateApprovalHistory result1 = this.studyService.approveVisitTemplate(visit, user, ipAddress, url.toString());

        Assert.assertNotNull(result1);
        Assert.assertTrue(result1 == approvalHistory);
        Assert.assertTrue(result1.isApproved());
        Assert.assertEquals(visit.getComment(), "this comment");
        Assert.assertEquals(result1.getUser(), visit.getUser());
        Mockito.verify(mockStudyDAO, Mockito.times(2)).findTemplateApprovalHistoryByVisitAndUser(visitTemplate, visit.getUser());
        Mockito.verify(mockAuditService).logVisitActivity(
                ipAddress, visitTemplate, user, Statics.AUDIT_VISIT_TEMPLATE_APPROVED, null, null);
        Mockito.verify(mockStudyDAO, Mockito.times(2)).updateEntity(Mockito.any(BaseEntity.class));

        Mockito.reset(mockAuditService);
        Mockito.reset(mockStudyDAO);
        Mockito.reset(mockAuthDAO);

        Mockito.when(mockStudyDAO.findVisitById(visit.getVisitTemplate())).thenReturn(visitTemplate);
        Mockito.when(mockAuthDAO.findUserById(visit.getUser().getId())).thenReturn(user);

        final TemplateApprovalHistory result2 = this.studyService.approveVisitTemplate(visit, user, ipAddress, url.toString());

        Assert.assertNotNull(result2);
        Assert.assertFalse(result2 == approvalHistory);
        Assert.assertTrue(result2.isApproved());
        Assert.assertEquals(visit.getComment(), "this comment");
        Assert.assertEquals(result2.getUser(), visit.getUser());
        Mockito.verify(mockStudyDAO).findTemplateApprovalHistoryByVisitAndUser(visitTemplate, visit.getUser());
        Mockito.verify(mockAuditService).logVisitActivity(
                ipAddress, visitTemplate, user, Statics.AUDIT_VISIT_TEMPLATE_APPROVED, null, null);
        Mockito.verify(mockStudyDAO).updateEntity(Mockito.any(BaseEntity.class));
        Mockito.verify(mockStudyDAO).createEntity(Mockito.any(BaseEntity.class));
    }

    @Test
    public void testCommentVisitTemplate() {
        final TemplateApprovalHistoryDTO visit = new TemplateApprovalHistoryDTO();
        visit.setVisitTemplate(321);
        visit.setComment("A comment");
        final VisitTemplate visitTemplate = new VisitTemplate();
        final User user = ServiceTestsDAOUtility.createMockUser();
        visit.setUser(user);
        final String ipAddress = "an ip address";
        final TemplateApprovalHistory approvalHistory = new TemplateApprovalHistory();

        Mockito.when(mockStudyDAO.findVisitById(visit.getVisitTemplate())).thenReturn(visitTemplate);
        Mockito.when(mockAuthDAO.findUserById(visit.getUser().getId())).thenReturn(user);
        Mockito.when(mockStudyDAO.findTemplateApprovalHistoryByVisitAndUser(visitTemplate, user))
                .thenReturn(approvalHistory);

        final TemplateApprovalHistory result1 = this.studyService.commentVisitTemplate(visit, user, ipAddress);

        Assert.assertNotNull(result1);
        Assert.assertTrue(result1 == approvalHistory);
        Assert.assertEquals(result1.getVisitTemplate(), visitTemplate);
        Assert.assertEquals(result1.getComment(), "A comment");
        Assert.assertEquals(result1.getUser(), user);
        Assert.assertFalse(result1.isApproved());
        Assert.assertNotNull(result1.getStatusChangeTime());
        Mockito.verify(mockStudyDAO, Mockito.times(2)).updateEntity(Mockito.any(TemplateApprovalHistory.class));
        Mockito.verify(mockStudyDAO, Mockito.times(2)).findTemplateApprovalHistoryByVisitAndUser(visitTemplate, user);
        Mockito.verify(mockAuditService).logVisitActivity(ipAddress, visitTemplate, user,
                Statics.AUDIT_VISIT_TEMPLATE_COMMENT, null, null);


        Mockito.reset(mockAuditService);
        Mockito.reset(mockStudyDAO);
        Mockito.reset(mockAuthDAO);


        Mockito.when(mockStudyDAO.findVisitById(visit.getVisitTemplate())).thenReturn(visitTemplate);
        Mockito.when(mockAuthDAO.findUserById(visit.getUser().getId())).thenReturn(user);

        final TemplateApprovalHistory result2 = this.studyService.commentVisitTemplate(visit, user, ipAddress);

        Assert.assertNotNull(result2);
        Assert.assertFalse(result2 == approvalHistory);
        Assert.assertEquals(result2.getVisitTemplate(), visitTemplate);
        Assert.assertEquals(result2.getComment(), "A comment");
        Assert.assertEquals(result2.getUser(), user);
        Assert.assertFalse(result2.isApproved());
        Assert.assertNotNull(result2.getStatusChangeTime());
        Mockito.verify(mockStudyDAO).findTemplateApprovalHistoryByVisitAndUser(visitTemplate, user);
        Mockito.verify(mockStudyDAO).createEntity(Mockito.any(TemplateApprovalHistory.class));
        Mockito.verify(mockStudyDAO).updateEntity(Mockito.any(TemplateApprovalHistory.class));
        Mockito.verify(mockAuditService).logVisitActivity(ipAddress, visitTemplate, user,
                Statics.AUDIT_VISIT_TEMPLATE_COMMENT, null, null);
    }

    @Test
    public void testGetVisitApprovals() {
        final List<VisitApprovalModelResponseDTO.VisitApproval> list = new ArrayList<>();
        final int visitId = 123;
        final String sortBy = "sortBy";
        final String orderBy = "orderBy";
        final int page = 30;
        final int maxResults = 10;

        Mockito.when(mockStudyDAO.getVisitApprovals(visitId, sortBy, orderBy, page, maxResults)).thenReturn(list);

        final List<VisitApprovalModelResponseDTO.VisitApproval> result =
                this.studyService.getVisitApprovals(visitId, sortBy, orderBy, page, maxResults);

        Assert.assertNotNull(result);
        Assert.assertTrue(result == list);
        Mockito.verify(mockStudyDAO).getVisitApprovals(visitId, sortBy, orderBy, page, maxResults);
    }

    @Test
    public void testGetTemplatesToApprove() {
        final User user = ServiceTestsDAOUtility.createMockUser();
        user.setRole(new Role(10, "Final approval", RoleType.ROLE_FINAL_APPROVER));
        final String sortBy = "sortBy";
        final String orderBy = "orderBy";
        final int page = 10;
        final int maxResults = 20;
        final int userId = 373;
        final String ipAddress = "ip address";
        final List<VisitTemplatesResponse> response1 = new ArrayList<>();

        Mockito.when(mockAuthDAO.findUserById(userId)).thenReturn(user);
        Mockito.when(mockStudyDAO.getNotApprovedVisits(sortBy, orderBy, page, maxResults)).thenReturn(response1);

        final List<VisitTemplatesResponse> result1 = this.studyService.getTemplatesToApprove(sortBy, orderBy,
                page, maxResults, userId, ipAddress);

        Assert.assertNotNull(result1);
        Assert.assertTrue(result1 == response1);
        Mockito.verify(mockAuditService).logViewActivity(ipAddress, user, "HOME SCREEN -Template Approval View.");
        Mockito.verify(mockStudyDAO).getNotApprovedVisits(sortBy, orderBy, page, maxResults);

        Mockito.reset(mockAuditService);
        Mockito.reset(mockStudyDAO);

        final List<TemplateApprovalHistory> tempHistory = new ArrayList<>();
        tempHistory.add(new TemplateApprovalHistory());
        user.setRole(new Role(10, "not", RoleType.ROLE_ADMINISTRATIVE_DIRECTOR));

        Mockito.when(mockAuthDAO.findUserById(userId)).thenReturn(user);
        Mockito.when(mockStudyDAO.findTemplateApprovalHistoryListByUser(user)).thenReturn(tempHistory);

        final List<VisitTemplatesResponse> result2 = this.studyService.getTemplatesToApprove(sortBy, orderBy,
                page, maxResults, userId, ipAddress);

        Assert.assertNotNull(result2);
        Mockito.verify(mockAuditService).logViewActivity(ipAddress, user, "HOME SCREEN -Template Approval View.");
        Mockito.verify(mockStudyDAO).getNotApprovedVisits(sortBy, orderBy, page, maxResults);
        Mockito.verify(mockStudyDAO).findTemplateApprovalHistoryListByUser(user);
        Mockito.verify(mockStudyDAO).setVisitTemplateData(Mockito.anyLong(),
                Mockito.any(VisitTemplate.class));
        Mockito.verify(mockStudyDAO).setVisitTemplateData(Mockito.anyLong(),
                Mockito.any(VisitTemplate.class));

        Mockito.reset(mockAuditService);
        Mockito.reset(mockStudyDAO);

        user.setInstitutionRole(new InstitutionRole(1, InstitutionRoleType.ROLE_STUDY_STAFF, "", ""));
        user.setRole(new Role(10, "not", RoleType.ROLE_ADMINISTRATIVE_DIRECTOR));

        Mockito.when(mockAuthDAO.findUserById(userId)).thenReturn(user);
        Mockito.when(mockStudyDAO.findTemplateApprovalHistoryListByUser(user)).thenReturn(tempHistory);

        final List<VisitTemplatesResponse> result3 = this.studyService.getTemplatesToApprove(sortBy, orderBy,
                page, 0, userId, ipAddress);

        Assert.assertNotNull(result3);
        Mockito.verify(mockAuditService).logViewActivity(ipAddress, user, "HOME SCREEN -Template Approval View.");
        Mockito.verify(mockStudyDAO).findStudyListByPerson(user);
        Mockito.verify(mockStudyDAO, Mockito.never()).getNotApprovedVisits(Mockito.anyString(),
                Mockito.anyString(), Mockito.anyInt(), Mockito.anyInt());
        Mockito.verify(mockStudyDAO).findTemplateApprovalHistoryListByUser(user);


        Mockito.reset(mockAuditService);
        Mockito.reset(mockStudyDAO);

        user.setInstitutionRole(new InstitutionRole(1, InstitutionRoleType.ROLE_GENERAL_VIEW, "", ""));
        user.setRole(new Role(10, "not", RoleType.ROLE_ADMINISTRATIVE_DIRECTOR));

        Mockito.when(mockAuthDAO.findUserById(userId)).thenReturn(user);

        final List<VisitTemplatesResponse> result4 = this.studyService.getTemplatesToApprove(sortBy, orderBy,
                page, 0, userId, ipAddress);

        Assert.assertNotNull(result4);
        Mockito.verify(mockStudyDAO).findStudyByScheduler(user);
        Mockito.verify(mockStudyDAO).findStudyByPI(user);
        Mockito.verify(mockStudyDAO).findStudyByNurse(user);
        Mockito.verify(mockStudyDAO).findStudyByNutritionist(user);
        Mockito.verify(mockStudyDAO, Mockito.never()).findTemplateApprovalHistoryListByUser(user);
    }

    @Test
    public void tesGetStudiesList() {
        final User user = ServiceTestsDAOUtility.createMockUser();
        user.setInstitutionRole(new InstitutionRole(1, InstitutionRoleType.ROLE_STUDY_STAFF, "", ""));
        final String sortBy = "sortBy";
        final String orderBy = "orderBy";
        final int page = 10;
        final int maxResults = 20;
        final List<StudyDataResponse> response1 = new ArrayList<>();
        final List<StudyDataResponse> response2 = new ArrayList<>();

        Mockito.when(mockStudyDAO.getStudyListByPerson(sortBy, orderBy, page, maxResults, user, null)).thenReturn(response1);

        final List<StudyDataResponse> result1 =
                this.studyService.getStudiesList(sortBy, orderBy, page, maxResults, user, null);

        Assert.assertNotNull(result1);
        Assert.assertTrue(response1 == result1);
        Mockito.verify(mockStudyDAO, Mockito.never()).getStudyList(sortBy, orderBy, page, maxResults, null);
        Mockito.verify(mockStudyDAO).getStudyListByPerson(sortBy, orderBy, page, maxResults, user, null);

        Mockito.reset(mockStudyDAO);

        user.setInstitutionRole(new InstitutionRole(1, InstitutionRoleType.ROLE_SUPER_ADMIN, "", ""));

        Mockito.when(mockStudyDAO.getStudyList(sortBy, orderBy, page, maxResults, null)).thenReturn(response2);

        final List<StudyDataResponse> result2 =
                this.studyService.getStudiesList(sortBy, orderBy, page, maxResults, user, null);

        Assert.assertNotNull(result2);
        Assert.assertTrue(response2 == result2);
        Mockito.verify(mockStudyDAO).getStudyList(sortBy, orderBy, page, maxResults, null);
        Mockito.verify(mockStudyDAO, Mockito.never()).getStudyListByPerson(sortBy, orderBy, page, maxResults, user, null);
    }

    @Test
    public void testGetStudyData() {
        final Study study = new Study();
        final Institution institution = new Institution("", "");
        institution.setId(232);
        study.setInstitution(institution);
        study.setStudyStatus(new StudyStatus(1, "", "", true, false, false));
        final User user = ServiceTestsDAOUtility.createMockUser();
        final String ipAddress = "an ip address";
        final StudyDetailResponse response = new StudyDetailResponse(study, null);

        Mockito.when(mockStudyDAO.findStudyById(191)).thenReturn(study);
        Mockito.when(mockStudyDAO.getStudyDataById(191)).thenReturn(response);

        final StudyDetailResponse result = this.studyService.getStudyData(191, user, ipAddress);

        Assert.assertNotNull(result);
        Assert.assertTrue(result == response);
        Assert.assertNotNull(study.getStatusChange());
        Mockito.verify(mockStudyDAO).findStudyById(191);
        Mockito.verify(mockStudyDAO).getStudyDataById(191);
        Mockito.verify(mockStudyDAO).updateEntity(study);
        Mockito.verify(mockAuditService).logStudyActivity(ipAddress, study, user, Statics.AUDIT_STUDY_VIEW, null, null);
    }

    @Test
    public void testChangeStudySubjectStatus() {
        final int studySubject = 509;
        final User user = ServiceTestsDAOUtility.createMockUser();
        final String ipAddress = "ip address change";
        final StudySubject subject = new StudySubject();
        subject.setSubjectMrn(new SubjectMrn());
        subject.setStudy(new Study());
        subject.setSubject(new Subject());
        subject.setActive(false);

        Mockito.when(mockSubjectDAO.findStudySubjectById(studySubject)).thenReturn(subject);

        final BooleanRequest result1 = this.studyService.changeStudySubjectStatus(studySubject, user, ipAddress);

        Assert.assertNotNull(result1);
        Assert.assertTrue(result1.isResult());
        Assert.assertTrue(subject.getActive());
        Mockito.verify(mockStudyDAO).updateEntity(subject);

        Mockito.reset(mockStudyDAO);

        subject.setActive(true);

        Mockito.when(mockStudyDAO.findStudySubjectById(studySubject)).thenReturn(subject);

        final BooleanRequest result2 = this.studyService.changeStudySubjectStatus(studySubject, user, ipAddress);

        Assert.assertNotNull(result2);
        Assert.assertTrue(result2.isResult());
        Assert.assertFalse(subject.getActive());

        Mockito.verify(mockStudyDAO).updateEntity(subject);
    }

    @Test
    public void testGetStudyMembersList() {
        final Study study = new Study();
        final List<UserDataResponse> list = new ArrayList<>();
        final SearchDTO searchDto = new SearchDTO();

        Mockito.when(mockStudyDAO.findStudyById(171)).thenReturn(study);
        Mockito.when(mockStudyDAO.getStudyMembers(study, "sortBy", "orderBy", 13, 10, searchDto)).thenReturn(list);

        final List<UserDataResponse> result = this.studyService.getStudyMembersList(171, "sortBy", "orderBy", 13, 10, searchDto);

        Assert.assertNotNull(result);
        Assert.assertTrue(result == list);
        Mockito.verify(mockStudyDAO).findStudyById(171);
        Mockito.verify(mockStudyDAO).getStudyMembers(study, "sortBy", "orderBy", 13, 10, searchDto);
    }

    @Test
    public void testChangeStudyMemberStatus() {
        final StudyUser studyUser = new StudyUser();
        final Study study = new Study();
        studyUser.setStudy(study);
        final User user = ServiceTestsDAOUtility.createMockUser();

        Mockito.when(mockStudyDAO.findStudyUserRoleById(181)).thenReturn(studyUser);

        final BooleanRequest booleanRequest = this.studyService.changeStudyMemberStatus(181, user, "");

        Assert.assertNotNull(booleanRequest);
        Assert.assertTrue(booleanRequest.isResult());
        Assert.assertTrue(studyUser.getActive());

        Mockito.verify(mockStudyDAO, Mockito.times(2)).updateEntity(Mockito.any(Study.class));
        Mockito.verify(mockAuditService).logStudyMemberActivity(Mockito.anyString(), Mockito.any(Study.class),
                Mockito.any(User.class), Mockito.any(User.class), Mockito.anyString(), Mockito.anyString(),
                Mockito.anyString());

        Mockito.reset(mockStudyDAO);
        Mockito.reset(mockAuditService);

        final StudyUser studyUser2 = new StudyUser();
        studyUser2.setActive(true);
        final Study study2 = new Study();
        studyUser2.setStudy(study2);

        Mockito.when(mockStudyDAO.findStudyUserRoleById(182)).thenReturn(studyUser2);

        final BooleanRequest booleanRequest2 = this.studyService.changeStudyMemberStatus(182, user, "");

        Assert.assertNotNull(booleanRequest2);
        Assert.assertTrue(booleanRequest2.isResult());
        Assert.assertFalse(studyUser2.getActive());

        Mockito.verify(mockStudyDAO, Mockito.times(2)).updateEntity(Mockito.any(Study.class));
        Mockito.verify(mockAuditService).logStudyMemberActivity(Mockito.anyString(), Mockito.any(Study.class),
                Mockito.any(User.class), Mockito.any(User.class), Mockito.anyString(), Mockito.anyString(),
                Mockito.anyString());
    }

    private final class MockStudyAnswer implements Answer<Object> {
        @Override
        public Object answer(InvocationOnMock invocation) throws Throwable {
            Study study;
            if (invocation.getArguments()[0] instanceof Study) {
                study = (Study) invocation.getArguments()[0];
                study.setId(100);
            }
            return null;
        }
    }
}
