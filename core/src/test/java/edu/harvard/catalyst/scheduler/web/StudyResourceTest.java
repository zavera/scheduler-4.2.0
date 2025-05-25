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

import com.google.common.collect.Lists;
import com.google.gson.JsonElement;
import edu.harvard.catalyst.scheduler.dto.*;
import edu.harvard.catalyst.scheduler.dto.request.BooleanRequest;
import edu.harvard.catalyst.scheduler.util.DateUtility;
import edu.harvard.catalyst.scheduler.util.TestUtils;
import edu.harvard.catalyst.scheduler.dto.response.*;
import edu.harvard.catalyst.scheduler.entity.*;
import edu.harvard.catalyst.scheduler.security.SchedulerSession;
import edu.harvard.catalyst.scheduler.security.SchedulerUserDetails;
import edu.harvard.catalyst.scheduler.service.StudyService;
import edu.harvard.catalyst.scheduler.util.ServiceTestsDAOUtility;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;


/**
 *
 *
 * @author Ankit Panchamia
 * @date 03/24/2014
 *
 */

@RunWith(MockitoJUnitRunner.class)
public class StudyResourceTest {

    StudyResource studyResource;

	// be verbose about method invocations on the mocked service
//	private StudyService mockStudyService = mock(StudyService.class, withSettings().invocationListeners(new VerboseMockInvocationLogger()));
	private StudyService mockStudyService = mock(StudyService.class);

    private final SchedulerSession session = mock(SchedulerSession.class);
    private final HttpServletRequest request = mock(HttpServletRequest.class);
    private final SchedulerUserDetails schedulerUserDetails = mock(SchedulerUserDetails.class);
    private final User mockUserInSession = ServiceTestsDAOUtility.createMockUser();
    private final String mockHostnameInRequest = "mockHostnameInRequest";
    private final ServletContext mockContext = mock(ServletContext.class);

//    @Autowired
//    ServletContext mockContext;

    @Before
	public void setUp() {

        studyResource = new StudyResource(mockStudyService);
        studyResource.setSession(session);
        studyResource.setRequest(request);

        when(session.getUserDetails()).thenReturn(schedulerUserDetails);
        when(schedulerUserDetails.getUser()).thenReturn(mockUserInSession);
        when(request.getRemoteHost()).thenReturn(mockHostnameInRequest);

	}

	/**
	 * The objective of this test is to make sure that the no-args constructor needed by cglib proxying is implemented.
	 */
	@Test
	public void testNoArgumentResourceConstructorExists() {

		StudyResource studyResource = new StudyResource();

	}

	@Test
    /**
     * Tests the JSON data returned by API method StudyResource.getNonClosedStudiesList()
     */
	public void testGetNonClosedStudiesList() {

		final GetStudiesResponse expectedResult = setupStudies();
		final int userId = mockUserInSession.getId();
		when(mockStudyService.getSubjectStudies(userId)).thenReturn(expectedResult);
		final String actualJsonString = studyResource.getNonClosedStudiesList();
        TestUtils.assertEqualJson(studyResource.gson, expectedResult, actualJsonString);

	}

	@Test
    public void testTemplateApprovalList() throws Exception {

		final List<VisitTemplatesResponse> visitList = setupVisitTemplates();
		when(mockStudyService.getTemplatesToApprove( "","", 1, 5, 1, "templatePath")).thenReturn(visitList);
		final List<VisitTemplatesResponse> result = studyResource.templateApprovalList("", "", 1, 5, 1, "templatePath");
		TestUtils.assertNonNullNonEmpty(result);
		TestUtils.assertNonNullAndHasThisMany(result, 2);

	}

    @Test
    /**
     * Tests the JSON data returned by API method StudyResource.getNonClosedStudiesList()
     */
    public void testGetTemplateApprovalList() throws Exception {

        final List<VisitTemplatesResponse> visitList = setupVisitTemplates();

        String sortBy = "";
        String orderBy = "";
        int page = 1;
        int maxResults = 5;
        int userId = 1;
        // This is very strange!!!
        // TODO-XH : is this the way we normally log actions?
        String ipAddress = studyResource.getRemoteHost();
        when(mockStudyService.getTemplatesToApprove( sortBy, orderBy, page, maxResults, userId, ipAddress)).thenReturn(visitList);
        String actualJsonString = studyResource.getTemplatesToApprove(sortBy, orderBy, page, maxResults);
		TestUtils.assertEqualJson(studyResource.gson, visitList, actualJsonString);

    }


	private List<VisitTemplatesResponse> setupVisitTemplates() {

		final Calendar calendar = Calendar.getInstance();
		final VisitTemplatesResponse visit = new VisitTemplatesResponse(1, 1, "visitName", "visitTypeName", "sublocationName", "approved", (long) 2, "localId",
				calendar.getTime(), "piName");
		final VisitTemplatesResponse visit1 = new VisitTemplatesResponse(2, 2, "visitName1", "visitTypeName1", "sublocationName1", "approved", (long) 2, "localId1",
				calendar.getTime(), "piName1");

		final List<VisitTemplatesResponse> visitList = new ArrayList<VisitTemplatesResponse>();
		visitList.add(visit);
		visitList.add(visit1);
		return visitList;

	}

	@Test
	public void testVisitDetailById() throws Exception {

		final User user = ServiceTestsDAOUtility.createMockUser();
		studyResource.loadVisitData(1, user, "templatePath");
		verify(mockStudyService, times(1)).getVisitTemplateData(1, user, "templatePath");

	}

	@Test
	public void testGetOpenStudies() throws Exception {

		final GetStudiesResponse result = setupStudies();
		final User user = ServiceTestsDAOUtility.createMockUser();
		when(mockStudyService.getOpenStudies("", "", "", 1, 5, user)).thenReturn(result);
		final GetStudiesResponse expectedResult = studyResource.getOpenStudiesList("", "", "", 1, 5, user);
		TestUtils.assertNonNullNonEmpty(expectedResult.getStudies());
		TestUtils.assertNonNullAndHasThisMany(expectedResult.getStudies(), 2);

	}

	@Test
	public void testGetSubjectStudiesList() throws Exception {

		final GetStudiesResponse result = setupStudies();
		final int userId = ServiceTestsDAOUtility.createMockUser().getId();
		when(mockStudyService.getSubjectStudies(userId)).thenReturn(result);
		final GetStudiesResponse expectedResult = studyResource.getSubjectStudiesList(userId);
		TestUtils.assertNonNullNonEmpty(expectedResult.getStudies());
		TestUtils.assertNonNullAndHasThisMany(expectedResult.getStudies(), 2);

	}

	private GetStudiesResponse setupStudies() {

		final Study study = new Study();
		study.setId(1);
		study.setName("Study 1");
		study.setLocalId("local 1");
		final StudyStatus status = new StudyStatus();
		status.setId(1);
		status.setShortName("PENDING");
		status.setName("IRB_PROCESS");
		study.setStudyStatus(status);
		final Study study1 = new Study();
		study1.setId(1);
		study1.setName("Study 1");
		study1.setLocalId("local 1");
		final StudyStatus status1 = new StudyStatus();
		status1.setId(2);
		status1.setShortName("OPEN");
		status1.setName("OPEN");
		study1.setStudyStatus(status);

		final List<Study> studyList = new ArrayList<Study>();
		studyList.add(study);
		studyList.add(study1);

		return GetStudiesResponse.createGetStudiesResponse(studyList, (long) 2);

	}

    @Test
    /**
     * Tests the JSON data returned by API method StudyResource.getNonClosedStudiesList()
     */
    // TODO-XH : write test where the expected output is not an empty list
    public void testGetVisitApprovals() throws Exception {

        final List<VisitApprovalModelResponseDTO.VisitApproval> expectedVisitApprovals = Lists.newArrayList();
        String sortBy = "";
        String orderBy = "";
        int page = 1;
        int maxResults = 5;
        int userId = 1;
        int visitId = 1; // FIXME-XH : is this correct?
        when(mockStudyService.getVisitApprovals(visitId, sortBy, orderBy, page, maxResults)).thenReturn(expectedVisitApprovals);
        String actualJsonString = studyResource.getVisitApprovals(sortBy, orderBy, maxResults, userId, visitId);
        TestUtils.assertEqualJson(studyResource.gson, expectedVisitApprovals, actualJsonString);

    }

	@Test
	public void testGetVisitApprovalsList() throws Exception {

		final List<VisitApprovalModelResponseDTO.VisitApproval> expected = Lists.newArrayList();
		when(mockStudyService.getVisitApprovals(1, "", "", 1, 5)).thenReturn(expected);
		final List<VisitApprovalModelResponseDTO.VisitApproval> actual = studyResource.getVisitApprovalsList("", "", 1, 5, 1);
		TestUtils.assertNonNullAndHasThisMany(actual, 0);
		assertEquals(expected, actual);

	}

	@Test
	public void testLoadStudyData() throws Exception {

		final User user = ServiceTestsDAOUtility.createMockUser();
		studyResource.loadStudyData(1, user, "templatePath");
		verify(mockStudyService, times(1)).getStudyData(1, user, "templatePath");

	}

	@Test
	public void testGetStudyDataResponses() throws Exception {

		final List<StudyDataResponse> expected = Lists.newArrayList();
		final User user = ServiceTestsDAOUtility.createMockUser();
		when(mockStudyService.getStudiesList("", "", 1, 5, user, null)).thenReturn(expected);
		final List<StudyDataResponse> actual = studyResource.getStudyDataResponses("", "", 1, 5, user, null);
		TestUtils.assertNonNullAndHasThisMany(actual, 0);
		assertEquals(expected, actual);

	}
    @Test
    public void testGetStudyDataResponsesWithSearch() throws Exception {

        final List<StudyDataResponse> expected = Lists.newArrayList();
        expected.add(new StudyDataResponse(Long.valueOf(1),
                                           200,
                                           "1000",
                                           "2000",
                                           "Study Name",
                                           "Approved",
                                           "FirstName",
                                           "LastName"));
        final User user = ServiceTestsDAOUtility.createMockUser();

        SearchDTO searchDTO = new SearchDTO();
        List<SearchDTO.SearchItem> searchItems = new ArrayList<>();
        SearchDTO.SearchItem searchItem = new SearchDTO.SearchItem();
        searchItem.setKey("name");
        searchItem.setValue("testName");
        searchItems.add(searchItem);
        searchDTO.setSearchItems(searchItems);

        when(mockStudyService.getStudiesList("", "", 1, 5, user,
                                             searchDTO)).thenReturn(expected);
        final List<StudyDataResponse> actual = studyResource.getStudyDataResponses("",
                                                                                   "", 1, 5,
                                                                                   user, searchDTO);
        TestUtils.assertNonNullAndHasThisMany(actual, 1);
        assertEquals(expected, actual);

    }

	@Test
	public void testGetStudySubjectsResponse() throws Exception {

		final List<StudySubject> studySubjects = Lists.newArrayList();
		final GetStudySubjectsResponse expected = GetStudySubjectsResponse.createGetStudiesSubjectsResponse(studySubjects, 0L);
		final User user = ServiceTestsDAOUtility.createMockUser();
		final String blah = "blah";
		final Integer zero = new Integer(0);
		final Integer studyId = new Integer(1);
		final Boolean wantAll = new Boolean(false);
        final SearchDTO searchDTO = null;

		final Optional<String> ofBlah = Optional.of(blah);
		final Optional<Integer> ofStudyId = Optional.of(studyId);
		final Optional<Boolean> ofFalse = Optional.of(wantAll);
		final Optional<User> ofUser = Optional.of(user);


        when(mockStudyService.getStudySubjects(
				ofBlah, zero, zero, ofStudyId,
				blah, blah, false, ofUser, searchDTO)).thenReturn(expected);

		final GetStudySubjectsResponse actual = studyResource.getStudySubjectsResponse(
				blah, zero, zero, studyId, wantAll, blah, blah, user, searchDTO);
		assertEquals(expected, actual);

	}

    @Test
    public void testGetStudyMembersList() {

        int studyId = 1;
        Study study = new Study();
        study.setId(studyId);

        User user1 = new User();
        user1.setEcommonsId("1");
        User user2 = new User();
        user1.setEcommonsId("2");

        StudyUser studyUser1 = new StudyUser(study, user1, true);
        StudyUser studyUser2 = new StudyUser(study, user2, true);

        List<StudyUser> expectedMembers = Arrays.asList(studyUser1, studyUser2);

        when(mockStudyService.getStudyMembers(studyId)).thenReturn(expectedMembers);

        String actualJsonString = studyResource.getStudyMembersList(studyId);

        TestUtils.assertEqualJson(studyResource.gson, expectedMembers, actualJsonString);

    }

    @Test
    public void testAddStudyMember() {

        // The incoming data represented as a DTO object. Will be used for two purposes:
        // 1. generate the JSON string argument to pass to the method-uder-test
        // 2. specify the argument value to be passed to mockStudyService.assignStudySubjects() for which we
        // are mocking a non-null return value
        AddStudyMembersDTO incomingDto = new AddStudyMembersDTO();
        incomingDto.setStudyId(1);
        incomingDto.setStudyMembers(Arrays.asList(1, 2));
        JsonElement incomingJsonElement = studyResource.gson.toJsonTree(incomingDto);
        String incomingString = studyResource.gson.toJson(incomingJsonElement);
        String templatePath = "someTemplatePath";
        // The mocked return value of the service method, as well as the expected return value of the Resource call
        BooleanResultDTO expectedResult = new BooleanResultDTO();
        expectedResult.setResult(true);

        // the trick here is to use refEq() for matching the DTO argument with the reference DTO.
        // Not that 'ref' in refEq() means _reflection_, not reference.
        when(mockStudyService.assignStudyMembers(refEq(incomingDto), eq(mockUserInSession), eq(mockHostnameInRequest), eq(templatePath))).thenReturn(expectedResult);
        when(mockContext.getRealPath("/WEB-INF")).thenReturn("someTemplatePath");

        String resultString = studyResource.addStudyMember(incomingString, mockContext);

        TestUtils.assertEqualJson(studyResource.gson, expectedResult, resultString);

        // TODO-XH : add another resource-level call to check that the data made it into the database. Otherwise we have a test with very little meaning..

    }

    StudyDTO createSampleStudyDto() {

        StudyDTO dto = new StudyDTO();

        dto.setId(0);

        dto.setCatalystId("1");
        dto.setLocalId("2");
        dto.setSpid("3");
        dto.setProtocolNumber("4");
        dto.setName("5");
        dto.setShortTitle("6");
        dto.setInstitution(7);
        dto.setStudyStatus(8);
        dto.setExpectedStartDate(new Date());
        dto.setExpectedEndDate(new Date());
        dto.setIrb("9");
        dto.setIrbExpiration(new Date());
        dto.setIrbRenewalDate(new Date());
        dto.setIrbApprovalDate(new Date());
        dto.setTotalSubjects(10);
        dto.setTotalOutpatientVisits(11);
        dto.setTotalInpatientVisits(12);
        dto.setIndustryInitiated("13");
        dto.setInvestigator(14);
        dto.setPhysician(15);
        dto.setPhysician2(16) ;
        dto.setScheduler(17);
        dto.setSecondaryScheduler(18);
        dto.setProtocolNurse(19);
        dto.setAssociateNurse(20);
        dto.setProtocolNutritionist(21);
        dto.setIrbInstitution(22);
        dto.setPediatric("23");
        dto.setCrcCategory("24");
        dto.setUserId(25);
        // FIXME-XH : this is weird. It wants a Study object
        dto.setStudyId(null);
        dto.setInvestigatorName("28");
        dto.setScheduler3(29);
        dto.setAssociateProtocolNutritionist(30);
        dto.setCrcFunded(true);
        dto.setStudyAbstract("31");
        dto.setClinicalTrial("32");
        dto.setNewDrug("33");
        dto.setDeviceExemption("34");
        FundingSourceInfo[] fundingSourceInfos = {
                new FundingSourceInfo(
                        35, "36", "37", 4,
                        "38", "39", 40.0d, 41.0d,
                        new Date(), new Date(), 42, "43"
                ),
                new FundingSourceInfo(
                        44, "45", "46", 3,
                        "47", "48", 49.0d, 50.0d,
                        new Date(), new Date(), 52, "53"
                )
        };
        dto.setFundingSourceInfoList(Arrays.asList(fundingSourceInfos));

        return dto;
    }

    @Test
    public void testCreateStudy() {

        StudyDTO incomingDto = createSampleStudyDto();
        String incomingString = studyResource.gson.toJson(incomingDto);

        // the trick here is to use refEq() for matching the DTO argument with the reference DTO.
        // Not that 'ref' in refEq() means _reflection_, not reference.
        // Note: ignore comparing fundingSourceInfoList because refEq does a shallow comparison
        when(mockStudyService.createStudy(refEq(incomingDto, "fundingSourceInfoList"), eq(mockUserInSession), eq(mockHostnameInRequest))).thenReturn(incomingDto);

        String resultString = studyResource.createStudy(incomingString);

        TestUtils.assertEqualJson(studyResource.gson, incomingDto, resultString);

        // TODO: maybe in a service-level test, check thayt the id was changed to something other than 0

    }

    @Test
    public void testEditStudy() {

        StudyDTO incomingDto = createSampleStudyDto();
        String incomingString = studyResource.gson.toJson(incomingDto);

        when(mockStudyService.updateStudy(refEq(incomingDto, "fundingSourceInfoList"), eq(mockUserInSession), eq(mockHostnameInRequest))).thenReturn(incomingDto);

        String resultString = studyResource.editstudy(incomingString);

        TestUtils.assertEqualJson(studyResource.gson, incomingDto, resultString);

        // TODO-XH : service-layer test should check that an existing DTO does get modified

    }

    TemplateApprovalHistoryDTO createTemplateApprovalHistoryDTO() {

        TemplateApprovalHistoryDTO templateApprovalHistoryDTO = new TemplateApprovalHistoryDTO();

        templateApprovalHistoryDTO.setVisitTemplate(1);
        templateApprovalHistoryDTO.setUser(mockUserInSession);
        templateApprovalHistoryDTO.setApproved(true);
        templateApprovalHistoryDTO.setComment("2");
        templateApprovalHistoryDTO.setStatusChangeTime(new Date());

        return templateApprovalHistoryDTO;

    }

    TemplateApprovalHistory createTemplateApprovalHistory() {

        TemplateApprovalHistory history = new TemplateApprovalHistory();
        history.setVisitTemplate(null);
        history.setComment("1");
        history.setApproved(Boolean.TRUE);
        history.setUser(mockUserInSession);
        history.setStatusChangeTime(new Date());
        history.setVisitCreatedDate(new Date());
        history.setUserRole("2");
        history.setUserTitle("3");
        history.setUserLastName("4");
        history.setUserFirstName("5");
        history.setVisitName("6");

        return history;

    }

    VisitTemplate createVisitTemplate() {

        final VisitType visitType = new VisitType();
        visitType.setName(TestUtils.InpatientCRC);
        visitType.setInpatient(true);
        visitType.setId(42);

        VisitTemplate visitTemplate = new VisitTemplate();
        visitTemplate.setVisitType(visitType);

        visitTemplate.setName("VisitTemplate 001");
        visitTemplate.setActive(false);
        visitTemplate.setApproved(true);
        visitTemplate.setComment("This is my visitComment");
        visitTemplate.setDuration(1800);
        visitTemplate.setNonInstitutionNonCRC(false);
        visitTemplate.setResearchPharmacy(true);
        visitTemplate.setInstitutionNonCRC(true);
        visitTemplate.setNursing("Nurse");
        visitTemplate.setNutrition("Nutrition");
        visitTemplate.setProcessing("Processing");
        visitTemplate.setSetup("Setup");
        visitTemplate.setRelativeTime(true);

        Study study = new Study();
        study.setName("Study 0001");
        study.setId(200);
        visitTemplate.setStudy(study);
        Institution inst = new Institution("Inst 001", "Institution 0001");
        Sublocation sublocation = new Sublocation("Sublocation 001", inst);
        sublocation.setId(100);
        visitTemplate.setSublocation(sublocation);

        return visitTemplate;
    }

    @Test
    public void testCommentVisitTemplate() {

        TemplateApprovalHistoryDTO incomingDto = createTemplateApprovalHistoryDTO();
        String incomingString = studyResource.gson.toJson(incomingDto);

        TemplateApprovalHistory history = createTemplateApprovalHistory();

        when(mockStudyService.commentVisitTemplate(refEq(incomingDto), eq(mockUserInSession), eq(mockHostnameInRequest))).thenReturn(history);

        String resultString = studyResource.commentVisitTemplate(incomingString);

        TestUtils.assertEqualJson(studyResource.gson, history, resultString);

    }

    @Test
    public void testGetVisitTemplateData() {

        int visitId = 100;

        VisitTemplate visitTemplate = createVisitTemplate();
        visitTemplate.setId(visitId);
        visitTemplate.setBillable(false);
        VisitTemplateDetailResponse visitTemplateDetailResponse = new VisitTemplateDetailResponse(visitTemplate);
        visitTemplateDetailResponse.setNumVisitTemplateComments(4);

        when(mockStudyService.getVisitTemplateData(eq(visitId), eq(mockUserInSession), eq(mockHostnameInRequest)))
                .thenReturn(visitTemplateDetailResponse);

        String resultString = studyResource.getVisitTemplateData(visitId);

        TestUtils.assertEqualJson(studyResource.gson, visitTemplateDetailResponse, resultString);
    }

    @Test
    public void testCreateVisit() {
        VisitDTO visitDTO = new VisitDTO();
        visitDTO.setName("Visit Name 1");
        visitDTO.setNutrition("Nutrition-1");
        visitDTO.setApproved(true);
        visitDTO.setActive(false);
        visitDTO.setDuration(3025);

        VisitTemplate visitTemplateResponse = new VisitTemplate();
        visitTemplateResponse.setName(visitDTO.getName());
        visitTemplateResponse.setNutrition(visitDTO.getNutrition());
        visitTemplateResponse.setApproved(visitDTO.isApproved());
        visitTemplateResponse.setActive(visitDTO.isActive());
        visitTemplateResponse.setDuration(visitDTO.getDuration());

        when(mockStudyService.createVisit(any(VisitDTO.class), eq(mockUserInSession), eq(mockHostnameInRequest)))
                .thenReturn(visitTemplateResponse);

        String incomingString = studyResource.gson.toJson(visitDTO);
        String resultString = studyResource.createVisit(incomingString);

        TestUtils.assertEqualJson(studyResource.gson, visitTemplateResponse, resultString);
    }

    @Test
    public void testGetStudyData(){
        int studyId = 2301;
        Study study = new Study();
        study.setId(studyId);
        study.setName("Test Study");
        StudyStatus studyStatus = new StudyStatus();
        studyStatus.setId(1);
        studyStatus.setName("Approved");
        studyStatus.setIsOpen(true);
        study.setStudyStatus(studyStatus);
        Institution institution = new Institution("Demo Institution", "Inst 1");
        institution.setId(4);
        study.setInstitution(institution);
        List<FundingSourceInfo> fundingSourceInfoList = new ArrayList<>();
        Integer id = 9283;
        String name = "Funding Source 1";
        String comment = "This is a test comment";
        Integer oneToFour = 1;
        String grant = "Grant 1";
        String center = "Center 1";
        Double direct = 520000.00;
        Double indirect = 100000.00;
        Date start = DateUtility.parse(DateUtility.monthDayYear(), "11/12/2009");
        Date end = DateUtility.parse(DateUtility.monthDayYear(), "12/31/2010");
        Integer centerAndInstitutionId = 12093;
        String centerAndInstitutionName = "Institution 1";
        FundingSourceInfo fundingSourceInfo = new FundingSourceInfo(id, name, comment, oneToFour, grant, center, direct, indirect,
                                                                    start, end, centerAndInstitutionId, centerAndInstitutionName);
        fundingSourceInfoList.add(fundingSourceInfo);
        StudyDetailResponse studyDetailResponse = new StudyDetailResponse(study, fundingSourceInfoList);
        when(mockStudyService.getStudyData(eq(studyId), eq(mockUserInSession), eq(mockHostnameInRequest)))
                .thenReturn(studyDetailResponse);

        String resultString = studyResource.getStudyData(studyId);
        TestUtils.assertEqualJson(studyResource.gson, studyDetailResponse, resultString);
    }

    @Test
    public void testEditVisit(){
        VisitDTO visitDTO = createSampleVisitDTO();
        VisitTemplate visitTemplate = new VisitTemplate();
        visitTemplate.setId(visitDTO.getId());
        visitTemplate.setName(visitDTO.getName());
        visitTemplate.setDuration(visitDTO.getDuration());
        visitTemplate.setNutrition(visitDTO.getNutrition());
        visitTemplate.setApproved(visitDTO.isApproved());
        visitTemplate.setCreatedDate(visitDTO.getCreatedDate());
        Study study = new Study();
        study.setId(visitDTO.getStudyId());
        visitTemplate.setStudy(study);
        String incomingString = studyResource.gson.toJson(visitDTO);

        when(mockStudyService.updateVisit(any(VisitDTO.class), eq(mockUserInSession), eq(mockHostnameInRequest)))
                .thenReturn(visitTemplate);

        String resultString = studyResource.editVisit(incomingString);
        TestUtils.assertEqualJson(studyResource.gson, visitTemplate, resultString);
    }

    @Test
    public void testChangeStudyMemberStatus(){

        int studyMemberId = 1;

        when(studyResource.getUser()).thenReturn(mockUserInSession);
        when(studyResource.getRemoteHost()).thenReturn(mockHostnameInRequest);

        BooleanRequest booleanRequest = new BooleanRequest();
        booleanRequest.setResult(true);
        when(mockStudyService.changeStudyMemberStatus(eq(studyMemberId), any(User.class), any(String.class))).thenReturn(booleanRequest);

        String resultString = studyResource.changeStudyMemberStatus(studyMemberId);

        verify(mockStudyService, times(1)).changeStudyMemberStatus(studyMemberId, mockUserInSession, mockHostnameInRequest);
        TestUtils.assertEqualJson(studyResource.gson, booleanRequest, resultString);
    }

    @Test
    public void testDeleteVisit(){
        int visitId = 241;

        when(studyResource.getUser()).thenReturn(mockUserInSession);
        when(studyResource.getRemoteHost()).thenReturn(mockHostnameInRequest);

        BooleanRequest booleanRequest = new BooleanRequest();
        booleanRequest.setResult(true);
        when(mockStudyService.deleteVisit(eq(visitId), any(User.class), any(String.class))).thenReturn(booleanRequest);

        String resultString = studyResource.deleteVisit(visitId);

        verify(mockStudyService, times(1)).deleteVisit(visitId, mockUserInSession, mockHostnameInRequest);
        TestUtils.assertEqualJson(studyResource.gson, booleanRequest, resultString);
    }

    @Test
    public void testGetVisitComments(){
        int visitId = 305;

        List<Comments> visitComments = new ArrayList<>();
        Comments comment1 = new Comments();
        User user1 = new User();
        user1.setLastName("Last1");
        user1.setFirstName("First1");
        user1.setEcommonsId("user1");
        comment1.setUser(user1);
        comment1.setComment("This is a comment from user1");
        comment1.setDate(DateUtility.parse(DateUtility.monthDayYear(), "03/20/2007"));
        visitComments.add(comment1);

        Comments comment2 = new Comments();
        User user2 = new User();
        user2.setLastName("Last2");
        user2.setFirstName("First2");
        user2.setEcommonsId("user2");
        comment2.setUser(user2);
        comment2.setDate(DateUtility.parse(DateUtility.monthDayYear(), "05/28/2002"));
        comment2.setComment("This is user2 comment");
        visitComments.add(comment2);

        List<VisitCommentsResponse.VisitComment> visitTemplateComments
                = VisitCommentsResponse.fromTemplateApprovalHistoryList(visitComments);

        when(mockStudyService.getVisitTemplateComments(eq(visitId))).thenReturn(visitComments);

        String resultString = studyResource.getVisitComments(visitId);

        verify(mockStudyService, times(1)).getVisitTemplateComments(visitId);

        TestUtils.assertEqualJson(studyResource.gson, visitTemplateComments, resultString);
    }

    @Test
    public void testGetStudyMembers(){
        int studyId = 874;
        String sortBy = "ASC";
        String orderBy = "";
        int page = 1;
        int maxResults = 5;
        String searchJson = "";

        when(studyResource.getUser()).thenReturn(mockUserInSession);
        when(studyResource.getRemoteHost()).thenReturn(mockHostnameInRequest);

        List<UserDataResponse> studyMembersList = new ArrayList<>();
        User user = new User();
        user.setId(10);
        user.setLastName("Foo");
        user.setFirstName("Bar");
        user.setEcommonsId("foobar");
        InstitutionRole instRole = new InstitutionRole();
        instRole.setName("Miss");
        user.setInstitutionRole(instRole);

        long totalCount = 1;
        UserDataResponse userDataResponse = new UserDataResponse(user, totalCount);
        studyMembersList.add(userDataResponse);

        when(mockStudyService.getStudyMembersList(eq(studyId), eq(sortBy),
                                                  eq(orderBy), eq(page),eq(maxResults), any(SearchDTO.class))).thenReturn(studyMembersList);

        String resultString = studyResource.getStudyMembers(sortBy, orderBy, page, maxResults, studyId, searchJson);

        verify(mockStudyService, times(1)).getStudyMembersList(studyId, sortBy, orderBy, page, maxResults, null);
        TestUtils.assertEqualJson(studyResource.gson, studyMembersList, resultString);
    }

    @Test
    public void testGetStudyList(){
        String sortBy = "ASC";
        String orderBy = "";
        int page = 1;
        int maxResults = 5;
        String searchJson = "";

        when(studyResource.getUser()).thenReturn(mockUserInSession);
        when(studyResource.getRemoteHost()).thenReturn(mockHostnameInRequest);

        List<StudyDataResponse> studiesList = new ArrayList<>();
        Long totalCount = Long.valueOf(1);
        Integer id= 203;
        String irbNumber = "IRB101";
        String localId = "LOCAL375";
        String name = "Test Study";
        String studyStatus= "OPEN";
        String piFirstName= "Bar";
        String piLastName = "Foo";
        StudyDataResponse studyDataResponse = new StudyDataResponse(totalCount, id, irbNumber, localId, name, studyStatus, piFirstName, piLastName);
        studiesList.add(studyDataResponse);
        when(mockStudyService.getStudiesList(eq(sortBy),
                                              eq(orderBy), eq(page),eq(maxResults), any(User.class), any(SearchDTO.class))).thenReturn(studiesList);

        String resultString = studyResource.getStudyList(sortBy, orderBy, page, maxResults, searchJson);

        verify(mockStudyService, times(1)).getStudiesList(sortBy, orderBy, page, maxResults, mockUserInSession, null);
        TestUtils.assertEqualJson(studyResource.gson, studiesList, resultString);
    }

    @Test
    public void testGetStudySubjects(){
        String filterString = "";
        int studyId = 459;
        boolean wantAll = false;
        String sortBy = "ASC";
        String orderBy = "";
        int page = 1;
        int maxResults = 5;
        String searchJson = "";

        User user = new User();
        user.setLastName("Last");
        user.setLastName("First");

        when(studyResource.getUser()).thenReturn(user);
        when(studyResource.getRemoteHost()).thenReturn(mockHostnameInRequest);

        long totalCount = Long.valueOf(1);
        List<StudySubject> studySubjectsList = new ArrayList<>();
        StudySubject studySubject = new StudySubject();
        studySubject.setId(10);
        studySubject.setActive(true);
        SubjectMrn subjectMrn = new SubjectMrn();
        subjectMrn.setMrn("12345");
        subjectMrn.setStatus("A");
        subjectMrn.setSite("INST");
        studySubject.setSubjectMrn(subjectMrn);
        GetStudySubjectsResponse studySubjectsResponse = GetStudySubjectsResponse.createGetStudiesSubjectsResponse(studySubjectsList, totalCount);
        when(mockStudyService.getStudySubjects(any(Optional.class), eq(page), eq(maxResults), any(Optional.class), eq(sortBy),
                                             eq(orderBy), eq(wantAll), any(Optional.class), any(SearchDTO.class))).thenReturn(studySubjectsResponse);

        String resultString = studyResource.getStudySubjects(filterString, page, maxResults, studyId, wantAll, sortBy, orderBy, searchJson);

        Optional<String> optionalFilterString = Optional.ofNullable(filterString);
        Optional<Integer> optionalStudyId = Optional.of(studyId);
        Optional<User> optionalUser = Optional.of(user);
        verify(mockStudyService, times(1)).getStudySubjects(
                optionalFilterString, page, maxResults, optionalStudyId, sortBy, orderBy,  wantAll, optionalUser, null);
        TestUtils.assertEqualJson(studyResource.gson, studySubjectsResponse, resultString);
    }

    @Test
    public void testCopyVisit(){

        VisitDTO visitDTO = new VisitDTO();
        visitDTO.setStudyId(1);
        visitDTO.setName("First Visit");
        visitDTO.setActive(true);
        visitDTO.setCreatedDate(DateUtility.parse(DateUtility.monthDayYear(), "02/15/2007"));
        String incomingString = studyResource.gson.toJson(visitDTO);

        when(studyResource.getUser()).thenReturn(mockUserInSession);
        when(studyResource.getRemoteHost()).thenReturn(mockHostnameInRequest);

        VisitTemplate visitTemplate = new VisitTemplate();
        visitTemplate.setCreatedDate(DateUtility.parse(DateUtility.monthDayYear(), "02/15/2007"));
        visitTemplate.setId(1);
        visitTemplate.setName("First Visit");

        Study study = new Study();
        study.setName("Test Study");
        study.setId(304);
        visitTemplate.setStudy(study);
        when(mockStudyService.copyVisit(any(VisitDTO.class), any(User.class), any(String.class))).thenReturn(visitTemplate);

        String resultString = studyResource.copyVisit(incomingString);

        TestUtils.assertEqualJson(studyResource.gson, visitTemplate, resultString);
    }

    VisitDTO createSampleVisitDTO(){
        VisitDTO visitDTO = new VisitDTO();
        visitDTO.setName("Sample Visit");
        visitDTO.setId(2394);
        visitDTO.setNutrition("Nutrition ABC");
        visitDTO.setDuration(192839);
        visitDTO.setCreatedDate(DateUtility.parse(DateUtility.monthDayYear(), "04/23/2014"));
        visitDTO.setStudyId(3849);
        visitDTO.setApproved(true);
        return visitDTO;
    }
}

