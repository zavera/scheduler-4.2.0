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

import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import edu.harvard.catalyst.scheduler.core.BookedVisitActivityLogStatics;
import edu.harvard.catalyst.scheduler.dto.*;
import edu.harvard.catalyst.scheduler.dto.statics.CalendarFilter;
import edu.harvard.catalyst.scheduler.util.ServiceTestsDAOUtility;
import edu.harvard.catalyst.scheduler.util.TestUtils;
import edu.harvard.catalyst.scheduler.core.SchedulerRuntimeException;
import edu.harvard.catalyst.scheduler.dto.response.*;
import edu.harvard.catalyst.scheduler.entity.*;
import edu.harvard.catalyst.scheduler.persistence.*;
import edu.harvard.catalyst.scheduler.util.DateUtility;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.runners.MockitoJUnitRunner;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static edu.harvard.catalyst.scheduler.service.AppointmentService.*;
import static edu.harvard.catalyst.scheduler.service.ConfirmationStatus.Confirmed;
import static edu.harvard.catalyst.scheduler.service.ConfirmationStatus.NotConfirmed;
import static edu.harvard.catalyst.scheduler.util.DateUtility.*;
import static java.util.Arrays.asList;
import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.*;

/**
 * @author clint
 * @date Aug 29, 2013
 */
@RunWith(MockitoJUnitRunner.class)
public final class AppointmentServiceTest {

    private AppointmentService appointmentService;

    @Mock
    private ResourceDAO resourceDAO;

    @Mock
    private StudyDAO studyDAO;

    @Mock
    private AppointmentDAO appointmentDAO;

    @Mock
    private AuditService mockAuditService;

    @Mock
    private AuthDAO mockAuthDAO;

    @Mock
    private TemplateResourceDAO mockTemplateResourceDAO;

    @Mock
    private SubjectDAO subjectDAO;
    private SearchAlgorithmService spySearchAlgorithmService = new SearchAlgorithmService();

    MockConflictChecker checker = new MockConflictChecker(true);

    MockAppointmentConfirmer confirmer = new MockAppointmentConfirmer();

    @Before
    public void setUp() {

        spySearchAlgorithmService = spy(spySearchAlgorithmService);
        appointmentService = makeService(checker, confirmer);
    }

    private AppointmentService makeService(final ConflictChecker conflictChecker, final AppointmentConfirmer appointmentConfirmer) {
        return new AppointmentService(
                appointmentDAO,
                resourceDAO,
                studyDAO,
                mockAuthDAO,
                mockAuditService,
                null,
                subjectDAO,
                mockTemplateResourceDAO,
                Optional.ofNullable(conflictChecker),
                Optional.ofNullable(appointmentConfirmer),
                spySearchAlgorithmService
        );
    }

    // mocked and, um, re-mocked in some setups
    private TemplateResource trMock;
    private VisitTemplate vtMock;
    private TemplateResourceAnnotations traMock;
    private LineLevelAnnotations llaMock;
    private Resource rMock;


    private final String visitTimeRoom = "VisitSpecsDTO Room";
    private final String allRooms = "All Rooms";

    // constants used in switch subject tests
    private final int BLOCK_SUBJECT_MRN_ID = 0;
    private final int SUBJECT_ID = 1;
    private final int SUBJECT_MRN_ID = 2;
    private final int VISIT_ID = 3;
    private final int STUDY_ID = 4;
    private final int BOOKED_VISIT_ID = 5;
    private final String FULL_NAME = "subject's full name";


    static final Calendar TEMPLATE_RESOURCE_CANONICAL_DATE = Calendar.getInstance();

    {
        resetCanonicalDate();
    }

    private void resetCanonicalDate() {
        TEMPLATE_RESOURCE_CANONICAL_DATE.set(Calendar.YEAR, 2012);
        TEMPLATE_RESOURCE_CANONICAL_DATE.set(Calendar.MONTH, 1);
        TEMPLATE_RESOURCE_CANONICAL_DATE.set(Calendar.DAY_OF_MONTH, 1);
        TEMPLATE_RESOURCE_CANONICAL_DATE.set(Calendar.HOUR_OF_DAY, 0);
        TEMPLATE_RESOURCE_CANONICAL_DATE.set(Calendar.MINUTE, 0);
        TEMPLATE_RESOURCE_CANONICAL_DATE.set(Calendar.SECOND, 0);
        TEMPLATE_RESOURCE_CANONICAL_DATE.set(Calendar.MILLISECOND, 0);
    }

    @Test
    public void testGetApprovedVisits() throws Exception {
        final List<VisitTemplate> expectedList = Lists.newArrayList();
        final GetStudyVisitsResponse expected = GetStudyVisitsResponse.createGetStudyVisitsResponse(expectedList, 0L,
                                                                                                    "", "", "", 1);

        final String blah = "blah";
        final Integer zero = 0;
        final Boolean hellNo = false;

        final String ofBlah = blah;
        final Integer ofZero = zero;
        final Boolean ofFalse = hellNo;

        when(studyDAO.getStudyVisits(
                ofBlah, ofBlah, ofBlah,
                ofZero, ofZero, ofZero, ofFalse
        )).thenReturn(expected);

        final GetStudyVisitsResponse actual = appointmentService.getStudyVisits(
                ofBlah, ofBlah, ofBlah,
                ofZero, ofZero, ofZero, ofFalse
        );

        assertEquals(expected, actual);
    }

    @Test
    public void testAreFlexResourcesLinkable() throws Exception {
        final GetTemplateResourceGroupDTO dto = new GetTemplateResourceGroupDTO();
        dto.setLinkResources(Arrays.asList(1, 2, 3));

        //first two coincide and followed by third
        List<TemplateResource> templateResourceList = setUpFlexGroupResources(0, 0, 10);
        assertTrue(appointmentService.areFlexResourcesLinkable(dto, templateResourceList));

        //second two coincide preceded by third
        templateResourceList = setUpFlexGroupResources(0, 10, 10);
        assertTrue(appointmentService.areFlexResourcesLinkable(dto, templateResourceList));

        //first and last coincide tangent third
        templateResourceList = setUpFlexGroupResources(10, 0, 10);
        assertTrue(appointmentService.areFlexResourcesLinkable(dto, templateResourceList));

        //linear
        templateResourceList = setUpFlexGroupResources(0, 10, 20);
        assertTrue(appointmentService.areFlexResourcesLinkable(dto, templateResourceList));

        //linear
        templateResourceList = setUpFlexGroupResources(20, 10, 0);
        assertTrue(appointmentService.areFlexResourcesLinkable(dto, templateResourceList));

        //linear
        templateResourceList = setUpFlexGroupResources(20, 0, 10);
        assertTrue(appointmentService.areFlexResourcesLinkable(dto, templateResourceList));

        //overlap
        templateResourceList = setUpFlexGroupResources(0, 5, 10);
        assertFalse(appointmentService.areFlexResourcesLinkable(dto, templateResourceList));

        //not tangent
        templateResourceList = setUpFlexGroupResources(0, 10, 30);
        assertFalse(appointmentService.areFlexResourcesLinkable(dto, templateResourceList));

        //not tangent
        templateResourceList = setUpFlexGroupResources(10, 10, 30);
        assertFalse(appointmentService.areFlexResourcesLinkable(dto, templateResourceList));

        //two resources
        TemplateResource tr1 = templateResourceWithStartEndTime(10, 20);
        TemplateResource tr2 = templateResourceWithStartEndTime(20, 30);
        when(appointmentDAO.findTemplateResourceById(1)).thenReturn(tr1);
        when(appointmentDAO.findTemplateResourceById(2)).thenReturn(tr2);
        dto.setLinkResources(Arrays.asList(1, 2));
        templateResourceList = Lists.newArrayList();
        templateResourceList.add(tr1);
        templateResourceList.add(tr2);
        assertTrue(appointmentService.areFlexResourcesLinkable(dto, templateResourceList));

        tr1 = templateResourceWithStartEndTime(10, 20);
        tr2 = templateResourceWithStartEndTime(30, 40);
        when(appointmentDAO.findTemplateResourceById(1)).thenReturn(tr1);
        when(appointmentDAO.findTemplateResourceById(2)).thenReturn(tr2);
        dto.setLinkResources(Arrays.asList(1, 2));
        templateResourceList = Lists.newArrayList();
        templateResourceList.add(tr1);
        templateResourceList.add(tr2);
        assertFalse(appointmentService.areFlexResourcesLinkable(dto, templateResourceList));

        tr1 = templateResourceWithStartEndTime(10, 20);
        tr2 = templateResourceWithStartEndTime(20, 25);
        when(appointmentDAO.findTemplateResourceById(1)).thenReturn(tr1);
        when(appointmentDAO.findTemplateResourceById(2)).thenReturn(tr2);
        dto.setLinkResources(Arrays.asList(1, 2));
        templateResourceList = Lists.newArrayList();
        templateResourceList.add(tr1);
        templateResourceList.add(tr2);
        assertFalse(appointmentService.areFlexResourcesLinkable(dto, templateResourceList));
    }

    private List<TemplateResource> setUpFlexGroupResources(final int startTime1, final int startTime2, final int
            startTime3) {
        final List<TemplateResource> templateResourceList = Lists.newArrayList();
        final TemplateResource tr1 = templateResourceWithStartEndTime(startTime1, startTime1 + 10);
        final TemplateResource tr2 = templateResourceWithStartEndTime(startTime2, startTime2 + 10);
        final TemplateResource tr3 = templateResourceWithStartEndTime(startTime3, startTime3 + 10);
        when(appointmentDAO.findTemplateResourceById(1)).thenReturn(tr1);
        templateResourceList.add(tr1);
        when(appointmentDAO.findTemplateResourceById(2)).thenReturn(tr2);
        templateResourceList.add(tr2);
        when(appointmentDAO.findTemplateResourceById(3)).thenReturn(tr3);
        templateResourceList.add(tr3);
        return templateResourceList;
    }

    private TemplateResource templateResourceWithStartEndTime(final int startMinutes, final int endMinutes) {
        final TemplateResource tr = new TemplateResource();
        tr.setStartMinutes(startMinutes);
        tr.setEndMinutes(endMinutes);
        tr.setDuration(endMinutes - startMinutes);
        return tr;
    }

    private TemplateResource templateResourceWithFloatStartEndTime(
            final int start, final int end,
            final int floatStart, final int floatEnd
    ) {
        final TemplateResource tr1 = templateResourceWithStartEndTime(start, end);
        tr1.setFloatStart(floatStart);
        tr1.setFloatEnd(floatEnd);
        return tr1;
    }

    @Test
    public void testAreFloatResourcesLinkable() throws Exception {
        final GetTemplateResourceGroupDTO dto = new GetTemplateResourceGroupDTO();
        dto.setLinkResources(Arrays.asList(1, 2, 3));

        //three resources with same float and preferred time
        TemplateResource tr1 = templateResourceWithFloatStartEndTime(10, 20, 0, 30);
        TemplateResource tr2 = templateResourceWithFloatStartEndTime(10, 20, 0, 30);
        TemplateResource tr3 = templateResourceWithFloatStartEndTime(10, 20, 0, 30);
        when(appointmentDAO.findTemplateResourceById(1)).thenReturn(tr1);
        when(appointmentDAO.findTemplateResourceById(2)).thenReturn(tr2);
        when(appointmentDAO.findTemplateResourceById(3)).thenReturn(tr3);
        dto.setLinkResources(Arrays.asList(1, 2, 3));
        List<TemplateResource> templateResourceList = Lists.newArrayList();
        templateResourceList.add(tr1);
        templateResourceList.add(tr2);
        templateResourceList.add(tr3);
        assertTrue(appointmentService.areFloatResourcesLinkable(dto, templateResourceList));

        //three resources with same float and one different preferred time
        tr1 = templateResourceWithFloatStartEndTime(10, 20, 0, 30);
        tr2 = templateResourceWithFloatStartEndTime(10, 20, 0, 30);
        tr3 = templateResourceWithFloatStartEndTime(10, 15, 0, 30);
        when(appointmentDAO.findTemplateResourceById(1)).thenReturn(tr1);
        when(appointmentDAO.findTemplateResourceById(2)).thenReturn(tr2);
        when(appointmentDAO.findTemplateResourceById(3)).thenReturn(tr3);
        dto.setLinkResources(Arrays.asList(1, 2, 3));
        templateResourceList = Lists.newArrayList();
        templateResourceList.add(tr1);
        templateResourceList.add(tr2);
        templateResourceList.add(tr3);
        assertFalse(appointmentService.areFloatResourcesLinkable(dto, templateResourceList));

        //three resources with one different float and same preferred time
        tr1 = templateResourceWithFloatStartEndTime(10, 20, 0, 30);
        tr2 = templateResourceWithFloatStartEndTime(10, 20, 0, 20);
        tr3 = templateResourceWithFloatStartEndTime(10, 20, 0, 30);
        when(appointmentDAO.findTemplateResourceById(1)).thenReturn(tr1);
        when(appointmentDAO.findTemplateResourceById(2)).thenReturn(tr2);
        when(appointmentDAO.findTemplateResourceById(3)).thenReturn(tr3);
        dto.setLinkResources(Arrays.asList(1, 2, 3));
        templateResourceList = Lists.newArrayList();
        templateResourceList.add(tr1);
        templateResourceList.add(tr2);
        templateResourceList.add(tr3);
        assertFalse(appointmentService.areFloatResourcesLinkable(dto, templateResourceList));

        //two resources with same float and preferred time
        tr1 = templateResourceWithFloatStartEndTime(10, 20, 0, 30);
        tr2 = templateResourceWithFloatStartEndTime(10, 20, 0, 30);
        when(appointmentDAO.findTemplateResourceById(1)).thenReturn(tr1);
        when(appointmentDAO.findTemplateResourceById(2)).thenReturn(tr2);
        dto.setLinkResources(Arrays.asList(1, 2));
        templateResourceList = Lists.newArrayList();
        templateResourceList.add(tr1);
        templateResourceList.add(tr2);
        assertTrue(appointmentService.areFloatResourcesLinkable(dto, templateResourceList));

        //two resources with same duration but same float and different preferred time
        tr1 = templateResourceWithFloatStartEndTime(10, 20, 0, 40);
        tr2 = templateResourceWithFloatStartEndTime(20, 30, 0, 40);
        when(appointmentDAO.findTemplateResourceById(1)).thenReturn(tr1);
        when(appointmentDAO.findTemplateResourceById(2)).thenReturn(tr2);
        dto.setLinkResources(Arrays.asList(1, 2));
        templateResourceList = Lists.newArrayList();
        templateResourceList.add(tr1);
        templateResourceList.add(tr2);
        assertFalse(appointmentService.areFloatResourcesLinkable(dto, templateResourceList));

        //two resources with same duration but different float and preferred time
        tr1 = templateResourceWithFloatStartEndTime(10, 20, 0, 40);
        tr2 = templateResourceWithFloatStartEndTime(20, 30, 0, 50);
        when(appointmentDAO.findTemplateResourceById(1)).thenReturn(tr1);
        when(appointmentDAO.findTemplateResourceById(2)).thenReturn(tr2);
        dto.setLinkResources(Arrays.asList(1, 2));
        templateResourceList = Lists.newArrayList();
        templateResourceList.add(tr1);
        templateResourceList.add(tr2);
        assertFalse(appointmentService.areFloatResourcesLinkable(dto, templateResourceList));
    }

    @Test
    public void testDeleteTemplateResource() throws Exception {
        TemplateResourceDTO dto = setupTemplateResourceDTO(null, false);
        appointmentService.deleteTemplateResource(dto, "institution", "templatePath");
        assertNull(dto.getErrorMsg());
        assertEquals(dto.isResult(), true);

        dto = setupTemplateResourceDTO("grouped", true);
        appointmentService.deleteTemplateResource(dto, "institution", "templatePath");
        assertNull(dto.getErrorMsg());
        assertEquals(dto.isResult(), true);
    }

    private TemplateResourceDTO setupTemplateResourceDTO(final String groupedString, final Boolean isFloat) {
        final TemplateResourceDTO dto = new TemplateResourceDTO();

        final VisitTemplate visitTemplate = new VisitTemplate();
        visitTemplate.setId(1);
        visitTemplate.setApproved(false);

        final Study study = new Study();
        study.setName("name");
        study.setLocalId("localId");
        study.setCatalystId("catId");
        study.setIrb("irb");
        visitTemplate.setStudy(study);

        final TemplateResource templateResource = new TemplateResource();
        templateResource.setVisitTemplate(visitTemplate);
        templateResource.setGroupId(groupedString);
        templateResource.setStartMinutes(minutesSinceOrigin(new Date()));
        templateResource.setEndMinutes(minutesSinceOrigin(new Date()));
        templateResource.setFloatable(isFloat);

        if (isFloat) {
            templateResource.setFloatStart(0);
            templateResource.setFloatEnd(10);
        }
        when(appointmentDAO.findTemplateResourceById(1)).thenReturn(templateResource);

        dto.setId(1);
        dto.setVisitTemplate(1);
        dto.setResource(1);
        dto.setBillable(false);
        dto.setAlternate(false);
        dto.setCreatedDate(new Date());
        dto.setDuration(1);
        dto.setFloatStart(0);
        dto.setFloatEnd(0);
        dto.setFloatable(isFloat.toString());
        dto.setResult(true);
        dto.setFlexible("false");
        dto.setStartMinutes(DateUtility.minutesSinceOrigin(new Date()));
        dto.setEndMinutes(DateUtility.minutesSinceOrigin(new Date()));

        dto.setSelectedAnnotations(Lists.newArrayList());
        dto.setSelectedAnnotationsComment(Lists.newArrayList());
        dto.setSelectedAnnotationsQuantity(Lists.newArrayList());

        return dto;
    }

    @Test
    public void testDeleteTemplateResourceValidation() throws Exception {
        final TemplateResourceDTO dto = new TemplateResourceDTO();
        dto.setId(1);
        TemplateResource templateResource = new TemplateResource();
        when(appointmentDAO.findTemplateResourceById(1)).thenReturn(templateResource);
        when(appointmentDAO.findBookedResourcesByBookedVisit(templateResource)).thenReturn(false);
        assertTrue(appointmentService.deleteTemplateResourceValidation(dto, templateResource));

        templateResource = new TemplateResource();
        templateResource.setGroupId("grouped");
        when(appointmentDAO.findTemplateResourceById(1)).thenReturn(templateResource);
        when(appointmentDAO.findBookedResourcesByBookedVisit(templateResource)).thenReturn(false);
        assertTrue(appointmentService.deleteTemplateResourceValidation(dto, templateResource));

        templateResource = new TemplateResource();
        when(appointmentDAO.findTemplateResourceById(1)).thenReturn(templateResource);
        when(appointmentDAO.findBookedResourcesByBookedVisit(templateResource)).thenReturn(true);
        assertFalse(appointmentService.deleteTemplateResourceValidation(dto, templateResource));

        templateResource = new TemplateResource();
        templateResource.setGroupId("grouped");
        when(appointmentDAO.findTemplateResourceById(1)).thenReturn(templateResource);
        when(appointmentDAO.findBookedResourcesByBookedVisit(templateResource)).thenReturn(true);
        assertFalse(appointmentService.deleteTemplateResourceValidation(dto, templateResource));
    }

    @Test
    public void testUpdateTemplateResource() throws Exception {
        TemplateResourceDTO dto = setupTemplateResourceDTO("", false); // not float
        final TemplateResource templateResource = mock(TemplateResource.class);
        final Resource resource = mock(Resource.class);
        final VisitTemplate visitTemplate = mock(VisitTemplate.class);

        when(templateResource.getVisitTemplate()).thenReturn(visitTemplate);
        when(templateResource.getResource()).thenReturn(resource);
        when(templateResource.getFloatable()).thenReturn(true);
//        when(templateResource.getStartDate()).thenReturn(new Date());
//        when(templateResource.getEndDate()).thenReturn(new Date());

        when(resource.getId()).thenReturn(0);
        when(appointmentDAO.findTemplateResourceById(dto.getId())).thenReturn(templateResource);
        when(resourceDAO.findResourceById(0)).thenReturn(resource);

        appointmentService.editTemplateResource(dto, "", "", new User(), "", false);
        verify(appointmentDAO, times(1)).updateEntity(visitTemplate);
        verify(appointmentDAO, times(1)).updateEntity(templateResource);

        dto = setupTemplateResourceDTO("", true); // float
        when(appointmentDAO.findTemplateResourceById(dto.getId())).thenReturn(templateResource);
        when(resourceDAO.findResourceById(dto.getResource())).thenReturn(resource);
        when(templateResource.getFloatable()).thenReturn(true);
        appointmentService.editTemplateResource(dto, "", "", new User(), "", false);
        verify(appointmentDAO, times(2)).updateEntity(visitTemplate);       // times are cumulative
        verify(appointmentDAO, times(2)).updateEntity(templateResource);
    }


    @Test
    public void testConfirmEvent() throws Exception {
        final Date startDate = new Date();
        final Date endDate = new Date();

        final VisitSpecsDTO visitSpecsDTO = new VisitSpecsDTO();

        visitSpecsDTO.setId(9999);
        visitSpecsDTO.setStartDate(startDate.getTime());
        visitSpecsDTO.setEndDate(endDate.getTime());

        final User user = new User();

        user.setEcommonsId("zz99");
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setPassword("foo");
        user.setSalt("salt");

        final Role role = new Role();

        role.setId(9999);
        role.setName("some role");
        role.setType(RoleType.ROLE_ADMINISTRATIVE_DIRECTOR);

        user.setRole(role);

        final Institution institution = new Institution("SH", "Some Hospital");

        institution.setId(9999);

        user.setInstitution(institution);

        final InstitutionRole institutionRole = new InstitutionRole();

        institutionRole.setId(9999);
        institutionRole.setDescription("some institution role");
        institutionRole.setName("bigshot");
        institutionRole.setType(InstitutionRoleType.ROLE_FRONT_DESK);

        user.setInstitutionRole(institutionRole);

        user.setBookedVisits(Lists.<BookedVisit>newArrayList());

        final UserSession userSession = new UserSession();

        userSession.setId(9999);
        userSession.setUser(user);

        userSession.setLastAccessTime(new Date());

        //double-room message is null
        //Appointment is available
        {
            final AppointmentService service = makeService(checker, confirmer);

            final ConfirmationStatus confStatus = service.confirmEvent(visitSpecsDTO, userSession, "", "", "", false);

            assertEquals(Confirmed, confStatus);

            assertTrue(checker.timeSlotAvailableInvoked);
            assertTrue(confirmer.confirmVisitBookingInvoked);
            assertFalse(confirmer.confirmVisitBookingAfterDoubleRoomMessage);
        }

        visitSpecsDTO.setDoubleRoomMessage("");

        //double-room message is NOT null
        //Appointment is available
        {
            final MockConflictChecker checker = new MockConflictChecker(true);

            final MockAppointmentConfirmer confirmer = new MockAppointmentConfirmer();

            final AppointmentService service = makeService(checker, confirmer);
            final ConfirmationStatus confStatus = service.confirmEvent(visitSpecsDTO, userSession, "", "", "", true);

            assertEquals(Confirmed, confStatus);

            assertTrue(checker.timeSlotAvailableInvoked);
            assertFalse(confirmer.confirmVisitBookingInvoked);
            assertTrue(confirmer.confirmVisitBookingAfterDoubleRoomMessage);
        }

        //double-room message is null
        //Appointment is NOT available
        {
            final MockConflictChecker checker = new MockConflictChecker(false);

            final MockAppointmentConfirmer confirmer = new MockAppointmentConfirmer();

            final AppointmentService service = makeService(checker, confirmer);

            final ConfirmationStatus confStatus = service.confirmEvent(visitSpecsDTO, userSession, "", "", "", false);

            assertEquals(NotConfirmed, confStatus);

            assertTrue(checker.timeSlotAvailableInvoked);
            assertFalse(confirmer.confirmVisitBookingInvoked);
            assertFalse(confirmer.confirmVisitBookingAfterDoubleRoomMessage);
        }

        //double-room message is NOT null
        //Appointment is NOT available
        {
            final MockConflictChecker checker = new MockConflictChecker(false);

            final MockAppointmentConfirmer confirmer = new MockAppointmentConfirmer();

            final AppointmentService service = makeService(checker, confirmer);

            final ConfirmationStatus confStatus = service.confirmEvent(visitSpecsDTO, userSession, "", "", "", false);

            assertEquals(NotConfirmed, confStatus);

            assertTrue(checker.timeSlotAvailableInvoked);
            assertFalse(confirmer.confirmVisitBookingInvoked);
            assertFalse(confirmer.confirmVisitBookingAfterDoubleRoomMessage);
        }

        {
            final List<Thread> threads = Lists.newArrayList();

            final int numThreads = 10;

            final List<ConfirmationStatus> results = Lists.newArrayList();

            final ConflictChecker checker = new AvailableFirstTimeOnlyMockConflictChecker();

            final AppointmentConfirmer confirmer = new MockAppointmentConfirmer();

            final AppointmentService service = makeService(checker, confirmer);

            for (int i = 0; i < numThreads; ++i) {
                threads.add(new Thread(() -> {
                    ConfirmationStatus confStatus = null;

                    try {
                        confStatus = service.confirmEvent(visitSpecsDTO, userSession, "", "", "", true);
                    }
                    catch (final Exception e) {
                        SchedulerRuntimeException.logAndThrow("Error confirming appointment: ", e);
                    }

                    synchronized (results) {
                        results.add(confStatus);
                    }
                }));
            }

            for (final Thread t : threads) {
                t.start();
            }

            for (final Thread t : threads) {
                t.join();
            }

            synchronized (results) {
                final int numConfirmed = Collections2.filter(results, status -> status ==
                                                                                ConfirmationStatus.Confirmed).size();

                final int numNotConfirmed = Collections2.filter(results, status -> status ==
                                                                                   ConfirmationStatus.NotConfirmed)
                        .size();

                assertEquals(1, numConfirmed);

                assertEquals(numThreads - 1, numNotConfirmed);
            }
        }
    }

    @Test
    public void testCalculateSomeAvailableResources() throws Exception {

    }

    @Test
    public void testFoundAvailableFlipResources() throws Exception {

    }

    @Test
    public void testFoundAvailableFloatResourceGroups() throws Exception {

    }

    @Test
    public void testFoundAvailableFloatResourceList() throws Exception {

    }

    @Test
    public void testFoundAvailableFixedResources() throws Exception {

    }

    @Test
    public void testSetupBookedResourcesForBookedVisit() throws Exception {
        final BookedVisit bookedVisit = mock(BookedVisit.class);

        final VisitTemplate visitTemplate = mock(VisitTemplate.class);
        VisitType visitType = new VisitType();
        visitType.setName(TestUtils.OutpatientCRC);
        visitType.setInpatient(false);

        when(bookedVisit.getVisitTemplate()).thenReturn(visitTemplate);
        when(bookedVisit.getScheduledStartTime()).thenReturn(new Date());
        when(bookedVisit.getScheduledEndTime()).thenReturn(new Date());

        when(visitTemplate.getVisitType()).thenReturn(visitType);

        final BookedResource bookedResource1 = mock(BookedResource.class);
        final BookedResource bookedResource2 = mock(BookedResource.class);

        final List<BookedResource> bookedResourceList = Lists.newArrayList();
        bookedResourceList.add(bookedResource1);
        bookedResourceList.add(bookedResource2);

        final OverrideBookedResourceAnnotations obra = mock(OverrideBookedResourceAnnotations.class);
        final List<OverrideBookedResourceAnnotations> obraList = Lists.newArrayList();
        obraList.add(obra);

        when(resourceDAO.findOverrideBookedResourceAnnotationsByBookedResource(bookedResource1)).thenReturn(obraList);
        when(resourceDAO.findOverrideBookedResourceAnnotationsByBookedResource(bookedResource2)).thenReturn(obraList);

        when(bookedResource1.getScheduledStartTime()).thenReturn(new Date());
        when(bookedResource1.getScheduledEndTime()).thenReturn(new Date());

        when(bookedResource2.getScheduledStartTime()).thenReturn(new Date());
        when(bookedResource2.getScheduledEndTime()).thenReturn(new Date());

        when(bookedResource1.cloneBookedResource()).thenReturn(bookedResource1);
        when(bookedResource2.cloneBookedResource()).thenReturn(bookedResource2);

        when(obra.cloneObra()).thenReturn(obra);

        appointmentService.setupResourcesForRescheduledByVisit(
                bookedResourceList,
                bookedVisit,
                new Date(),
                new Date()
        );

        verify(appointmentDAO, times(2)).createEntity(obra);

        verify(bookedResource1).setBookedVisit(bookedVisit);
        verify(bookedResource2).setBookedVisit(bookedVisit);

        // should work for 'exactly 1', but somehow mockito thinks it's 2
        //   for each mock. breakpoint confirms that it's 1 each, total 2.
        verify(appointmentDAO, atLeast(1)).createEntity(bookedResource1);
        verify(appointmentDAO, atLeast(1)).createEntity(bookedResource2);
    }

    @Test
    public void testSetupBookedResourcesForAppointmentWithVisitTime() throws Exception {
        final User mockUser = mock(User.class);
        final VisitSpecsDTO mockVisitSpecsDTO = mock(VisitSpecsDTO.class);
        final BookedVisit mockBookedVisit = mock(BookedVisit.class);

        when(mockBookedVisit.getScheduledStartTime()).thenReturn(new Date());
        when(mockBookedVisit.getScheduledEndTime()).thenReturn(new Date());

        final TemplateResource mockTemplateResource = mock(TemplateResource.class);
        final Resource mockResource = mock(Resource.class);
        final ResourceType mockResourceType = ResourceType.Room;

        final List<TemplateResource> templateResourceList = Lists.newArrayList();
        templateResourceList.add(mockTemplateResource);

        when(mockTemplateResource.getResource()).thenReturn(mockResource);
        when(mockResource.getId()).thenReturn(0);

        when(mockTemplateResource.getStartDate()).thenReturn(new Date());
        when(mockTemplateResource.getEndDate()).thenReturn(new Date());

        when(resourceDAO.findResourceById(0)).thenReturn(mockResource);
        when(resourceDAO.findResourceTypeById(1)).thenReturn(mockResourceType);

        when(mockResource.getName()).thenReturn("myName");
        when(mockResource.getResourceType()).thenReturn(mockResourceType);
        when(mockVisitSpecsDTO.getRoomSelected()).thenReturn(0);

        appointmentService.setupBookedResourcesForAppointmentWithVisitTime(
                mockVisitSpecsDTO,
                mockUser,
                "templatePath",
                new Date(),
                mockBookedVisit,
                templateResourceList,
                0
        );
        verify(appointmentDAO, times(1)).createEntity(any(BookedResource.class));
    }


    @Test
    public void testCreateTemplateResource() throws Exception {
        final TemplateResourceDTO templateResourceDTO = mock(TemplateResourceDTO.class);
        when(templateResourceDTO.getStartMinutes()).thenReturn(DateUtility.minutesSinceOrigin(new Date()));
        when(templateResourceDTO.getEndMinutes()).thenReturn(DateUtility.minutesSinceOrigin(new Date()));
        final Resource resource = mock(Resource.class);
        when(resourceDAO.findResourceById(templateResourceDTO.getResource())).thenReturn(resource);
        final VisitTemplate visitTemplate = mock(VisitTemplate.class);
        when(studyDAO.findVisitById(templateResourceDTO.getVisitTemplate())).thenReturn(visitTemplate);
        final TemplateResource templateResource = new TemplateResource(templateResourceDTO, resource, visitTemplate);

        final TemplateResource result = appointmentService.createTemplateResource(
                templateResourceDTO,
                "institution",
                "templatePath"
        );

        verify(appointmentDAO, times(1)).createEntity(templateResource);
        assertEquals(result, templateResource);
    }

    @Test
    public void testComputeTotalDuration() throws Exception {
        final VisitTemplate visitTemplate = new VisitTemplate();
        visitTemplate.setId(1);
        visitTemplate.setRelativeTime(true);

        // startTime has to be midnight
        List<TemplateResource> templateResourceList = setupRelativeTemplateResourceList();
        assertEquals(appointmentService.computeTotalDuration(visitTemplate, templateResourceList), 360);

        //inpatient templateResource added starting at midnight - relative
        final TemplateResource templateResource = new TemplateResource();
        templateResource.setStartMinutes(minutesSinceOrigin(TEMPLATE_RESOURCE_CANONICAL_DATE.getTime()));
        Calendar templateResourceEndDate = Calendar.getInstance();
        templateResourceEndDate.set(2012, Calendar.FEBRUARY, 2, 2, 0, 0); //feb 2 2012 at 2:00 am
        templateResource.setEndMinutes(minutesSinceOrigin(templateResourceEndDate.getTime()));
        templateResourceList.add(templateResource);
        assertEquals(appointmentService.computeTotalDuration(visitTemplate, templateResourceList), 1560);

        //inpatient templateResource added overlapping other resource not starting @ midnight - relative
        final TemplateResource templateResource1 = new TemplateResource();
        final Calendar templateResourceStartDate = Calendar.getInstance();
        templateResourceStartDate.set(2012, Calendar.FEBRUARY, 1, 2, 0, 0); //feb 1 2012 at 2:00 am
        templateResource1.setStartMinutes(minutesSinceOrigin(templateResourceStartDate.getTime()));
        templateResourceEndDate = Calendar.getInstance();
        templateResourceEndDate.set(2012, Calendar.FEBRUARY, 2, 2, 0, 0); //feb 2 2012 at 2:00 am
        templateResource1.setEndMinutes(minutesSinceOrigin(templateResourceEndDate.getTime()));
        templateResourceList.add(templateResource1);
        assertEquals(appointmentService.computeTotalDuration(visitTemplate, templateResourceList), 1560);

        //tests for clock time templates (startTime doesn't have to be midnight)
        visitTemplate.setRelativeTime(false);
        templateResourceList = setupClockTemplateResourceList();
        assertEquals(appointmentService.computeTotalDuration(visitTemplate, templateResourceList), 1440);
    }

    private List<TemplateResource> setupRelativeTemplateResourceList() {
        final List<TemplateResource> list = Lists.newArrayList();
        final Date startDate = TEMPLATE_RESOURCE_CANONICAL_DATE.getTime();

        final TemplateResource templateResource = new TemplateResource();
        templateResource.setStartMinutes(minutesSinceOrigin(startDate));
        final Calendar templateResourceEndDate = Calendar.getInstance();
        templateResourceEndDate.set(2012, Calendar.FEBRUARY, 1, 2, 0, 0); //feb 1 2012 at 2:00 am
        templateResource.setEndMinutes(minutesSinceOrigin(templateResourceEndDate.getTime()));
        list.add(templateResource);

        final TemplateResource templateResource1 = new TemplateResource();
        templateResource1.setStartMinutes(minutesSinceOrigin(startDate));
        templateResourceEndDate.set(2012, Calendar.FEBRUARY, 1, 6, 0, 0); //feb 1 2012 at 6:00 am
        templateResource1.setEndMinutes(minutesSinceOrigin(templateResourceEndDate.getTime()));
        list.add(templateResource1);

        return list;
    }

    private List<TemplateResource> setupClockTemplateResourceList() {
        final List<TemplateResource> list = Lists.newArrayList();
        final TemplateResource templateResource = new TemplateResource();

        Calendar templateResourceStartDate = Calendar.getInstance();
        templateResourceStartDate.set(2012, Calendar.FEBRUARY, 1, 2, 0, 0); //feb 2 2012 at 2:00 am
        templateResource.setStartMinutes(minutesSinceOrigin(templateResourceStartDate.getTime()));

        Calendar templateResourceEndDate = Calendar.getInstance();
        templateResourceEndDate.set(2012, Calendar.FEBRUARY, 2, 2, 0, 0); //feb 2 2012 at 2:00 am
        templateResource.setEndMinutes(minutesSinceOrigin(templateResourceEndDate.getTime()));

        list.add(templateResource);

        final TemplateResource templateResource1 = new TemplateResource();

        templateResourceStartDate = Calendar.getInstance();
        templateResourceStartDate.set(2012, Calendar.FEBRUARY, 1, 2, 0, 0); //feb 2 2012 at 2:00 am
        templateResource1.setStartMinutes(minutesSinceOrigin(templateResourceStartDate.getTime()));

        templateResourceEndDate = Calendar.getInstance();
        templateResourceEndDate.set(2012, Calendar.FEBRUARY, 1, 4, 0, 0); //feb 2 2012 at 2:00 am
        templateResource1.setEndMinutes(minutesSinceOrigin(templateResourceEndDate.getTime()));

        list.add(templateResource1);

        return list;
    }

    @Test
    public void testProbablyAccumulateResult() throws Exception {

        final int mockitoAccumulatedInvocationsForFindById = 0;

        // the if - if block
        helpTestProbablyAccumulateResult(
                allRooms,
                100,
                mockitoAccumulatedInvocationsForFindById,
                allRooms,
                1
        );
        // the else block
        helpTestProbablyAccumulateResult(
                visitTimeRoom,
                100,
                mockitoAccumulatedInvocationsForFindById,
                visitTimeRoom,
                1
        );
    }

    private void helpTestProbablyAccumulateResult(
            final String templateResourceResourceName,
            final int visitTimeSelectedRoomNum,
            final int expectedFindRoomByIdCalls,
            final String expectedRoomName,
            final int expectedAccumulated
    ) {


        final VisitSpecsDTO mockVisitSpecsDTO = mock(VisitSpecsDTO.class);
        when(mockVisitSpecsDTO.getRoomSelected()).thenReturn(visitTimeSelectedRoomNum);

        final List<Resource> accumulator = new ArrayList<Resource>();

        final Resource mockResource2 = mock(Resource.class);
        final ResourceType mockResourceType = ResourceType.Room;

        when(mockResource2.getName()).thenReturn(templateResourceResourceName);
        when(mockResource2.getResourceType()).thenReturn(mockResourceType);

        when(resourceDAO.findResourceTypeById(1)).thenReturn(mockResourceType);
        when(resourceDAO.findResourceById(visitTimeSelectedRoomNum)).thenReturn(mockResource2);

        final TemplateResource mockTemplateResource = mock(TemplateResource.class);
        final Resource mockResource1 = mock(Resource.class);
        final ResourceType mockResourceType1 = ResourceType.Nursing;

        when(mockResource1.getId()).thenReturn(1);
        when(mockResource1.getName()).thenReturn(templateResourceResourceName);

        when(mockResource1.getResourceType()).thenReturn(mockResourceType1);

        when(resourceDAO.findResourceTypeById(0)).thenReturn(mockResourceType1);
        when(resourceDAO.findResourceById(1)).thenReturn(mockResource1);
        when(mockTemplateResource.getResource()).thenReturn(mockResource1);


        final Resource resultResource = appointmentService.probablyAccumulateResult(
                mockVisitSpecsDTO,
                accumulator,
                mockTemplateResource
        );

        verify(resourceDAO, times(expectedFindRoomByIdCalls)).findResourceById(100);
        assertEquals(resultResource.getName(), expectedRoomName);
        TestUtils.assertNonNullAndHasThisMany(accumulator, expectedAccumulated);
    }

    @Test
    public void testSetupOneBookedResourceForAppointment() throws Exception {
        final User mockUser = mock(User.class);
        final VisitSpecsDTO mockVisitSpecsDTO = mock(VisitSpecsDTO.class);
        final List<Resource> accumulator = Lists.newArrayList();
        final BookedVisit mockBookedVisit = mock(BookedVisit.class);
        when(mockBookedVisit.getScheduledStartTime()).thenReturn(new Date());
        when(mockBookedVisit.getScheduledEndTime()).thenReturn(new Date());

        final TemplateResource mockTemplateResource = mock(TemplateResource.class);
        final Resource mockResource = mock(Resource.class);
        final ResourceType mockResourceType = ResourceType.Room;

        when(mockTemplateResource.getResource()).thenReturn(mockResource);
        when(mockResource.getId()).thenReturn(0);

        when(mockTemplateResource.getStartDate()).thenReturn(new Date());
        when(mockTemplateResource.getEndDate()).thenReturn(new Date());

        when(resourceDAO.findResourceById(0)).thenReturn(mockResource);
        when(resourceDAO.findResourceTypeById(0)).thenReturn(mockResourceType);

        when(mockResource.getName()).thenReturn("myName");
        when(mockResource.getResourceType()).thenReturn(mockResourceType);
        when(mockVisitSpecsDTO.getRoomSelected()).thenReturn(0);

        appointmentService.setupOneBookedResourceForAppointment(
                mockVisitSpecsDTO,
                mockUser,
                "templatePath",
                accumulator,
                mockBookedVisit,
                mockTemplateResource,
                0,
                new Date()
        );

        verify(appointmentDAO, times(1)).createEntity(any(BookedResource.class));
    }

    @Test
    public void testSetTemplateResourceTimes() throws Exception {
        final int MINUTE_DELTA = 2;

        final TemplateResource templateResource = mock(TemplateResource.class);
        final List<TemplateResource> templateResourceList = new ArrayList<TemplateResource>();
        templateResourceList.add(templateResource);

        final Calendar startCal = Calendar.getInstance();
        final Calendar endCal = Calendar.getInstance();

        endCal.setTimeInMillis(startCal.getTimeInMillis());
        endCal.add(Calendar.MINUTE, MINUTE_DELTA);

        final Date startTime = startCal.getTime();
        final Date endTime = endCal.getTime();

        when(templateResource.getStartDate()).thenReturn(startTime);
        when(templateResource.getEndDate()).thenReturn(endTime);

        appointmentService.setTemplateResourceTimes(startTime, templateResourceList);

        // we test separately the method which computes these two dates
        verify(templateResource, times(1)).setScheduledStartTime(any(Date.class));
        verify(templateResource, times(1)).setScheduledEndTime(any(Date.class));

        verify(templateResource, times(1)).setDuration(MINUTE_DELTA);
    }

    @Test
    public void testGetDateAdjustedForEvent() throws Exception {
        final int EVENT_DAY = 3;
        final int RESOURCE_DAY = 5;

        final int RESOURCE_MINUTE = 2;
        final int DELTA_MINUTE = 3;

        final Calendar eventStartDate = Calendar.getInstance();

        eventStartDate.set(Calendar.DATE, EVENT_DAY);
        eventStartDate.set(Calendar.HOUR_OF_DAY, 0);
        eventStartDate.set(Calendar.MINUTE, 0);
        eventStartDate.set(Calendar.SECOND, 0);
        eventStartDate.set(Calendar.MILLISECOND, 0);

        final Calendar templateResourceStartDate = Calendar.getInstance();
        templateResourceStartDate.set(2012, Calendar.FEBRUARY, RESOURCE_DAY, 1, RESOURCE_MINUTE, 0); //feb 5 2014 at
        // 1:02 am

        final long templateTime = templateResourceStartDate.getTimeInMillis();
        final long templateResourceStartTime = TEMPLATE_RESOURCE_CANONICAL_DATE.getTimeInMillis();

        final long bookedResouceRelativeTime = templateTime - templateResourceStartTime;
        final long adjustedBookedResourceStartTime = eventStartDate.getTimeInMillis() + bookedResouceRelativeTime;

        final Calendar expectedResult = Calendar.getInstance();
        expectedResult.setTimeInMillis(adjustedBookedResourceStartTime);
        expectedResult.set(Calendar.HOUR_OF_DAY, 1);
        expectedResult.set(Calendar.MINUTE, RESOURCE_MINUTE + DELTA_MINUTE);
        expectedResult.set(Calendar.SECOND, 0);
        expectedResult.set(Calendar.MILLISECOND, 0);

        final Calendar result = appointmentService.getDateAdjustedForEvent(
                eventStartDate.getTime(),
                templateResourceStartDate.getTime(),
                DELTA_MINUTE
        );

        final long expectedMillis = expectedResult.getTimeInMillis();
        final long actualMillis = result.getTimeInMillis();
        final SimpleDateFormat sdf = new SimpleDateFormat("yyyy MMM dd HH:mm:ss");
        assertTrue(
                "Wanted " + sdf.format(result.getTime()) + ", not " + sdf.format(expectedResult.getTime()),
                expectedMillis == actualMillis
        );
    }

    @Test
    public void testSetupTemplateResource() throws Exception {
        final int minsPerDay = MINS_PER_DAY;

        final VisitTemplate visitTemplate = mock(VisitTemplate.class);
        final BookedVisit bookedVisit = mock(BookedVisit.class);
        final TemplateResource templateResource = mock(TemplateResource.class);

        final Calendar startTimeCalendar = Calendar.getInstance();
        final Calendar endTimeCalendar = Calendar.getInstance();

        startTimeCalendar.setTimeInMillis(0L);
        endTimeCalendar.setTimeInMillis(0L);
        endTimeCalendar.add(Calendar.DATE, 1);

        final Date startDate = startTimeCalendar.getTime();
        final Date endDate = endTimeCalendar.getTime();

        when(bookedVisit.getScheduledStartTime()).thenReturn(startDate);
        when(bookedVisit.getScheduledEndTime()).thenReturn(endDate);

        appointmentService.setupTemplateResource(templateResource, visitTemplate, bookedVisit);

        verify(templateResource, times(1)).setId(123456);
        verify(templateResource, times(1)).setVisitTemplate(visitTemplate);

        verify(templateResource, times(1)).setStartMinutes(minutesSinceOrigin(startDate));
        verify(templateResource, times(1)).setEndMinutes(minutesSinceOrigin(endDate));

        verify(templateResource, times(1)).setScheduledStartTime(startDate);
        verify(templateResource, times(1)).setScheduledEndTime(endDate);

        verify(templateResource, times(1)).setDuration(minsPerDay);
    }

    /////////////////////////////////////////////////////////

    private static final class AvailableFirstTimeOnlyMockConflictChecker implements ConflictChecker {
        private volatile boolean invoked = false;

        @Override
        public synchronized boolean timeSlotAvailable(final VisitSpecsDTO visitSpecsDTO, final UserSession userSession, final
        boolean isInpatient) {
            try {
                return !invoked;
            }
            finally {
                invoked = true;
            }
        }
    }

    private static final class MockConflictChecker implements ConflictChecker {
        private final boolean available;

        boolean timeSlotAvailableInvoked = false;

        public MockConflictChecker(final boolean available) {
            super();
            this.available = available;
        }

        @Override
        public boolean timeSlotAvailable(final VisitSpecsDTO visitSpecsDTO, final UserSession userSession, final boolean
                isInpatient) {
            timeSlotAvailableInvoked = true;

            return available;
        }
    }

    private static final class MockAppointmentConfirmer implements AppointmentConfirmer {
        boolean confirmVisitBookingInvoked = false;
        boolean confirmVisitBookingAfterDoubleRoomMessage = false;

        @Override
        public void confirmVisitBooking(final AppointmentService appointmentService, final VisitSpecsDTO visitSpecsDTO, final UserSession userSession, final String
                ipAddress, final String institution, final String templatePath, final Date startDate, final Date
                endDate) {
            confirmVisitBookingInvoked = true;
        }

        @Override
        public void confirmVisitBookingAfterDoubleRoomMessage(final AppointmentService appointmentService, final VisitSpecsDTO visitSpecsDTO, final UserSession
                userSession, final String ipAddress, final String institution, final String templatePath, final Date
                startDate, final Date endDate) {
            confirmVisitBookingAfterDoubleRoomMessage = true;
        }
    }

    @Test
    public void testFindTemplateResourceCountEarliestLatest() throws Exception {
        final int visitId = 42;
        final ResourceTimeBoundsAndCountResponseDTO dto = mock(ResourceTimeBoundsAndCountResponseDTO.class);

        when(mockTemplateResourceDAO.findTemplateResourceCountEarliestLatest(visitId)).thenReturn(dto);

        final ResourceTimeBoundsAndCountResponseDTO returnedDto = appointmentService.findTemplateResourceCountEarliestLatest(visitId);

        assertEquals(returnedDto, dto);
    }

    @Test
    public void testGetBookedResources() throws Exception {
        final int Bookedvisitid = 1;
        final String Sortby = "r.name";
        final String Orderby = "ASC";
        final int Page = 1;
        final int Maxresults = 50;
        final BookedResourcesResponse dto = new BookedResourcesResponse(Bookedvisitid, "Resource", "", Calendar
                .getInstance().getTime(), Calendar.getInstance().getTime(), "", "", "", (long) 1, false);

        final List<BookedResourcesResponse> expected = Lists.newArrayList();
        expected.add(dto);

        when(appointmentDAO.getBookedResourcesListByBookedVisit(Bookedvisitid, Sortby, Orderby, Page, Maxresults))
                .thenReturn(expected);

        final List<BookedResourcesResponse> returnedDto = appointmentService.getBookedResources(Bookedvisitid, Sortby, Orderby, Page, Maxresults);

        TestUtils.assertNonNullAndEqual(returnedDto, expected);
        verify(appointmentDAO, times(1)).getBookedResourcesListByBookedVisit(Bookedvisitid, Sortby, Orderby, Page,
                                                                             Maxresults);
        TestUtils.assertNonNullAndHasThisMany(returnedDto, 1);
        assertEquals(expected.get(0), dto);
    }

    @Test
    public void testGetVisitResources() throws Exception {
        final List<TemplateResource> expectedList = Lists.newArrayList();
        final GetSearchVisitResourceResponse expected = GetSearchVisitResourceResponse.createGetSearchVisitsResponse
                (expectedList, 0L);

        final String blah = "blah";
        final Integer zero = 0;
        final Integer visitId = 1;
        final String ofBlah = blah;
        final Integer ofZero = zero;
        final Integer ofVisitId = visitId;

        when(mockTemplateResourceDAO.findTemplateResourcesByVisit(
                ofVisitId, ofBlah, ofBlah,
                ofZero, ofZero
        )).thenReturn(expected);

        final GetSearchVisitResourceResponse actual = appointmentService.getVisitResources(
                ofBlah, ofBlah,
                ofZero, ofZero, ofVisitId
        );

        assertEquals(expected, actual);
    }

    @Test
    public void testGetAppointmentComments() throws Exception {
        final List<Comments> expectedList = Lists.newArrayList();
        final List<VisitCommentsResponse.VisitComment> expected = VisitCommentsResponse
                .fromTemplateApprovalHistoryList(expectedList);

        final int visitId = 1;

        final BookedVisit bookedVisit = mock(BookedVisit.class);

        when(appointmentDAO.findBookedVisitById(visitId)).thenReturn(bookedVisit);
        when(appointmentDAO.findAppointmentCommentsByVisit(bookedVisit)).thenReturn(expected);

        final List<VisitCommentsResponse.VisitComment> actual = appointmentService.getAppointmentComments(visitId);

        assertEquals(expected, actual);
    }

    @Test
    public void testLogViewBookedVisit() {
        final BookedVisitDTO bookedVisitDTO = new BookedVisitDTO();
        bookedVisitDTO.setId(10);
        final BookedVisit bookedVisit = mock(BookedVisit.class);
        final User user = ServiceTestsDAOUtility.createMockUser();
        final String ipAddress = "ip address";

        Mockito.when(appointmentDAO.findBookedVisitById(10)).thenReturn(bookedVisit);

        final BookedVisitDTO result = this.appointmentService.logViewBookedVisit(bookedVisitDTO, user, ipAddress);

        // in-lining action String and not referring to static, so that this test will detect any change
        // to the static String
        Mockito.verify(mockAuditService).logAppointmentActivity(ipAddress, bookedVisit, user,
                BookedVisitActivityLogStatics.VIEWED_DETAILS_FROM_APPOINTMENT_LIST);
        Mockito.verify(appointmentDAO).findBookedVisitById(10);

        assertNotNull(result);
        assertTrue(result == bookedVisitDTO);
    }

    @Test
    public void testLogIncompleteOverrideActions() {
        final BookedVisitDTO bookedVisitDTO = new BookedVisitDTO();
        bookedVisitDTO.setId(10);
        final User user = ServiceTestsDAOUtility.createMockUser();
        final String ipAddress = "ip address";
        final String action = "an action";
        final BookedResource bookedResource = mock(BookedResource.class);

        Mockito.when(bookedResource.getResource()).thenReturn(new Resource());
        Mockito.when(bookedResource.getBookedVisit()).thenReturn(new BookedVisit());
        Mockito.when(appointmentDAO.findBookedResourceById(10)).thenReturn(bookedResource);

        final BookedVisitDTO result = appointmentService.logIncompleteOverrideActions(bookedVisitDTO, user, ipAddress, action);

        Mockito.verify(appointmentDAO).findBookedResourceById(10);
        Mockito.verify(mockAuditService).logAppointmentOverrideActivity(ipAddress, bookedResource.getBookedVisit(),
                bookedResource.getResource(), user, action, null, null);
        assertNotNull(result);
        assertTrue(result == bookedVisitDTO);
    }

    @Test
    public void testGetCalendarBookedVisits() {
        final User user = ServiceTestsDAOUtility.createMockUser();
        final List<CalendarVisitsResponse> visits = new ArrayList<>();
        final String sublocationName = "test sublocation name";
        final String filterString = "";
        final String filterId = "";
        final CalendarFilter calendarFilter = CalendarFilter.NO_FILTER;
        final Date startMonth = new Date();
        final Date endMonth = new Date();
        final String remoteHost = "a remoteHost";
        final boolean homeView = true;

        Mockito.when(mockAuthDAO.findUserById(user.getId())).thenReturn(user);
        Mockito.when(appointmentDAO.getCalendarBookedVisits(sublocationName, startMonth, endMonth,
                                                            homeView, null, user.isStudyStaff()
        )).thenReturn(visits);

        final List<CalendarVisitsResponse> calendarVisitsResponses = this.appointmentService.getCalendarBookedVisits(user.getId(),
                                                                                                                     calendarFilter, filterString,
                                                                                                                     sublocationName, startMonth, endMonth,
                                                                                                                     remoteHost,
                                                                                                                     homeView);

        Mockito.verify(mockAuthDAO).findUserById(user.getId());
        Mockito.verify(mockAuditService).logViewActivity(remoteHost, user, "HOME SCREEN - Appointment Calendar View.");
        Mockito.verify(appointmentDAO).getCalendarBookedVisits(sublocationName, startMonth, endMonth,
                                                               homeView, null, user.isStudyStaff()
        );
        assertEquals(calendarVisitsResponses, visits);
    }

    @Test
    public void testGetCalendarBookedVisitsAppointment() {
        final User user = ServiceTestsDAOUtility.createMockUser();
        final List<CalendarVisitsResponse> visits = new ArrayList<>();
        final String sublocationName = "Test Sublocation 1";
        final CalendarFilter calendarFilter = CalendarFilter.NO_FILTER;
        final String filterString = "";
        final Date startMonth = new Date();
        final Date endMonth = new Date();
        final String remoteHost = "a diferent string";
        final boolean homeView = false;

        Mockito.when(mockAuthDAO.findUserById(user.getId())).thenReturn(user);
        Mockito.when(appointmentDAO.getCalendarBookedVisits(filterString, startMonth, endMonth,
                                                            homeView, null, user.isStudyStaff()
        )).thenReturn(visits);

        final List<CalendarVisitsResponse> calendarVisitsResponses = this.appointmentService.getCalendarBookedVisits(user.getId(),
                                                                                                                     calendarFilter, filterString,
                                                                                                                     sublocationName,
                                                                                                                     startMonth, endMonth,
                                                                                                                     remoteHost,
                                                                                                                     homeView);

        Mockito.verify(mockAuthDAO).findUserById(user.getId());
        Mockito.verify(mockAuditService).logViewActivity(remoteHost, user, "Appointment Calendar Viewed");
        Mockito.verify(appointmentDAO).getCalendarBookedVisits(sublocationName, startMonth, endMonth,
                                                               homeView, null, user.isStudyStaff()
        );
        assertEquals(calendarVisitsResponses, visits);
    }

    @Test
    public void testGetCalendarBookedVisitsIdSpecified() {
        final User user = ServiceTestsDAOUtility.createMockUser();
        final List<CalendarVisitsResponse> visits = new ArrayList<>();
        final String filterString = "";
        final String sublocationName = "";
        final CalendarFilter calendarFilter = CalendarFilter.BY_APPOINTMENT_STATUS_SCHEDULED;
        final Date startMonth = new Date();
        final Date endMonth = new Date();
        final String remoteHost = "a remoteHost";
        final boolean homeView = true;

        Mockito.when(mockAuthDAO.findUserById(user.getId())).thenReturn(user);
        Mockito.when(appointmentDAO.findBookedVisitsByApppointmentStatus(calendarFilter, startMonth, endMonth,
                                                                         homeView, null, user.isStudyStaff(), sublocationName

        )).thenReturn(visits);

        final List<CalendarVisitsResponse> calendarVisitsResponses =
                this.appointmentService.getCalendarBookedVisits(user.getId(), calendarFilter, filterString, sublocationName,
                                                                startMonth, endMonth, remoteHost, homeView);

        Mockito.verify(mockAuthDAO).findUserById(user.getId());
        Mockito.verify(mockAuditService).logViewActivity(remoteHost, user, "HOME SCREEN - Appointment Calendar View.");
        Mockito.verify(appointmentDAO).findBookedVisitsByApppointmentStatus(calendarFilter, startMonth, endMonth,
                                                                            homeView, null, user.isStudyStaff(), sublocationName
        );
        assertEquals(calendarVisitsResponses, visits);
    }

    @Test
    public void testGetCalendarBookedIsStudyStaff() {
        final User user = mock(User.class);
        final List<CalendarVisitsResponse> visits = new ArrayList<>();
        final List<Study> studies = new ArrayList<>();
        final String sublocationName = "Test Sublocation 1";
        final CalendarFilter calendarFilter = CalendarFilter.NO_FILTER;
        final String filterString = "";
        final Date startMonth = new Date();
        final Date endMonth = new Date();
        final String remoteHost = "a remoteHost";
        final boolean homeView = true;

        Mockito.when(user.isStudyStaff()).thenReturn(true);
        Mockito.when(user.getId()).thenReturn(321);
        Mockito.when(mockAuthDAO.findUserById(user.getId())).thenReturn(user);
        Mockito.when(appointmentDAO.findBookedVisitsByApppointmentStatus(calendarFilter, startMonth, endMonth,
                                                                         homeView, null, user.isStudyStaff(), sublocationName
        )).thenReturn(visits);
        Mockito.when(studyDAO.findStudyListByPerson(user)).thenReturn(studies);

        final List<CalendarVisitsResponse> calendarVisitsResponses = this.appointmentService.getCalendarBookedVisits(user.getId(),
                                                                                                                     calendarFilter, filterString,
                                                                                                                     sublocationName, startMonth,
                                                                                                                     endMonth,
                                                                                                                     remoteHost,
                                                                                                                     homeView);

        Mockito.verify(mockAuthDAO).findUserById(user.getId());
        Mockito.verify(mockAuditService).logViewActivity(remoteHost, user, "HOME SCREEN - Appointment Calendar View.");
        Mockito.verify(appointmentDAO).getCalendarBookedVisits(sublocationName, startMonth, endMonth,
                                                               homeView, studies, user.isStudyStaff()
        );
        assertEquals(calendarVisitsResponses, visits);
    }

    @Test
    public void testFilterCalendarVisitsResponses() throws Exception {
        final List<String> filterIds = asList("1", "2", "3", "4", "5", "6", "7");
        final List<CalendarFilter> calendarFilters = asList(CalendarFilter.values());
        final List<String> filterStrings = asList("yes", "no", "0", "", "foo");

        final User user = mock(User.class);
        final Date startMonth = new Date();
        final Date endMonth = new Date();
        final boolean homeView = false;
        final List<CalendarVisitsResponse> expected = Lists.newArrayList();
        final List<Study> userAccessibleStudies = Lists.newArrayList();
        for (final CalendarFilter calendarFilter : calendarFilters) {
            for (final String filterString : filterStrings) {
                final List<CalendarVisitsResponse> actual = appointmentService.filterCalendarVisitsResponses(filterString,
                                                                                                             calendarFilter, null,
                                                                                                             user,
                                                                                                             startMonth, endMonth,
                                                                                                             homeView,
                                                                                                             expected, userAccessibleStudies


                );
                assertEquals(expected, actual);
            }
        }
    }

    @Test
    public void  testFilterCalendarVisitsResponsesBySubjectLastNameAndSublocation() throws Exception {
        final String filterString = "subject";
        final Date startMonth = new Date();
        final User user = mock(User.class);
        final Date endMonth = new Date();
        final String sublocationName = "Test Sublocation 1";
        final boolean homeView = false;

        CalendarVisitsResponse response = new CalendarVisitsResponse(101, "my visit name", "pi name",
                                                                     "room", "1001", "firstname lastname",
                                                                     "3", "2017-03-17 10:00",
                                                                     "2017-03-20 22:00", false, true);

        final List<CalendarVisitsResponse> expected = Lists.newArrayList();
        expected.add(response);
        final List<Subject> subjectsList = Lists.newArrayList();
        final List<StudySubject> totalStudySubjectList = Lists.newArrayList();
        final List<Study> userAccessibleStudies = Lists.newArrayList();

        when(subjectDAO.filterSubjectByLastNames(filterString)).thenReturn(subjectsList);
        when(studyDAO.findStudySubjectBySubjectListAndStudyList(subjectsList, userAccessibleStudies)).thenReturn
                (totalStudySubjectList);
        when(appointmentDAO.findAllBookedVisitsBySubject(user, userAccessibleStudies, subjectsList, sublocationName, startMonth, endMonth, homeView))
                .thenReturn(expected);

        final List<CalendarVisitsResponse> actual = appointmentService.filterCalendarVisitsResponsesBySubjectLastName
                (user, filterString, sublocationName, startMonth, endMonth, homeView, expected, userAccessibleStudies);
        assertEquals(expected, actual);
    }

    @Test
    public void testFilterBookedVisitsByAppointmentStatus() throws Exception {
        final List<String> filterIds = asList("1", "2", "3", "4", "5");
        final List<CalendarFilter> calendarFilters = asList(CalendarFilter.values());

        final User user = mock(User.class);
        final Date startMonth = new Date();
        final Date endMonth = new Date();
        final boolean homeView = false;
        final List<CalendarVisitsResponse> expected = Lists.newArrayList();
        final List<Study> userAccessibleStudies = Lists.newArrayList();

        for (final CalendarFilter calendarFilter : calendarFilters) {
            when(appointmentDAO.findBookedVisitsByApppointmentStatus(calendarFilter, startMonth, endMonth,
                                                                     homeView, userAccessibleStudies, user
                                                                             .isStudyStaff(),null
            )).thenReturn(expected);
            final List<CalendarVisitsResponse> actual = appointmentService.filterBookedVisitsByAppointmentStatus(calendarFilter, null ,
                                                                                                                 user,
                                                                                                                 startMonth,
                                                                                                                 endMonth,
                                                                                                                 homeView, userAccessibleStudies


            );
            assertEquals(expected, actual);
        }
    }

    @Test
    public void testFilterCalendarVisitsResponsesBySubjectLastName() throws Exception {
        final String filterString = "subject";
        final Date startMonth = new Date();
        final User user = mock(User.class);
        final Date endMonth = new Date();
        final boolean homeView = false;
        final List<CalendarVisitsResponse> expected = Lists.newArrayList();
        final List<Subject> subjectsList = Lists.newArrayList();
        final List<StudySubject> totalStudySubjectList = Lists.newArrayList();
        final List<Study> userAccessibleStudies = Lists.newArrayList();

        when(subjectDAO.filterSubjectByLastNames(filterString)).thenReturn(subjectsList);
        when(studyDAO.findStudySubjectBySubjectListAndStudyList(subjectsList, userAccessibleStudies)).thenReturn
                (totalStudySubjectList);
        when(appointmentDAO.findAllBookedVisitsBySubject(user, userAccessibleStudies, subjectsList, null, startMonth, endMonth, homeView))
                .thenReturn(expected);

        final List<CalendarVisitsResponse> actual = appointmentService.filterCalendarVisitsResponsesBySubjectLastName
                                                (user, filterString, null, startMonth, endMonth, homeView, expected, userAccessibleStudies);
        assertEquals(expected, actual);
    }

    @Test
    public void testFilterCalendarVisitsResponsesByStudyLocalId() throws Exception {
        final String filterString = "study";
        final User user = mock(User.class);
        final Date startMonth = new Date();
        final Date endMonth = new Date();
        final boolean homeView = false;
        final List<CalendarVisitsResponse> expected = Lists.newArrayList();
        final CalendarVisitsResponse newObj = new CalendarVisitsResponse(1, "visitName", "piLastName", "rooms",
                                                                         "localId",
                                                                         "subjectLastName", "appointmentStatus",
                                                                         "scheduledStartTime", "scheduledEndTime",
                                                                         false, false);
        expected.add(newObj);

        final List<Study> studyList = Lists.newArrayList();
        final Study study = mock(Study.class);
        studyList.add(study);

        when(studyDAO.findStudyListByPersonAndLocalID(user, filterString)).thenReturn(studyList);
        when(appointmentDAO.findAllBookedVisitsByStudy(studyList, startMonth, endMonth, homeView, null)).thenReturn(expected);

        final List<CalendarVisitsResponse> actual = appointmentService.filterCalendarVisitsResponsesByStudyLocalId(filterString, null,
                                                                                                                   user,
                                                                                                                   startMonth,
                                                                                                                   endMonth,
                                                                                                                   homeView, expected

        );
        assertEquals(expected, actual);
    }

    @Test
    public void testFilterCalendarVisitsResponsesByResourceNameNonStudyStaff() throws Exception {
        final String filterString = "resource";
        final Date startMonth = new Date();
        final Date endMonth = new Date();
        final boolean homeView = false;
        final List<Study> userAccessibleStudies = Lists.newArrayList();

        final User mockUser = mock(User.class);
        when(mockUser.isStudyStaff()).thenReturn(false);

        final List<CalendarVisitsResponse> expected = Lists.newArrayList();
        final CalendarVisitsResponse newObj = new CalendarVisitsResponse(1, "visitName", "piLastName", "rooms",
                                                                         "localId",
                                                                         "subjectLastName", "appointmentStatus",
                                                                         "scheduledStartTime", "scheduledEndTime",
                                                                         false, false
        );
        expected.add(newObj);

        when(appointmentDAO.findBookedVisitsByResource(filterString, mockUser, userAccessibleStudies, startMonth,
                                                       endMonth, homeView,null
        )).thenReturn(expected);

        final List<CalendarVisitsResponse> actual = appointmentService.filterCalendarVisitsResponsesByResourceName(filterString, null,
                                                                                                                   mockUser,
                                                                                                                   startMonth,
                                                                                                                   endMonth,
                                                                                                                   homeView, userAccessibleStudies

        );
        assertEquals(expected, actual);
    }

    @Test
    public void testFilterCalendarVisitsResponsesByResourceNameStudyStaff() throws Exception {
        final String filterString = "resource";
        final Date startMonth = new Date();
        final Date endMonth = new Date();
        final boolean homeView = false;
        final List<Study> userAccessibleStudies = Lists.newArrayList();
        final User mockUser = mock(User.class);

        when(mockUser.isStudyStaff()).thenReturn(true);

        final List<CalendarVisitsResponse> expected = Lists.newArrayList();
        when(appointmentDAO.findBookedVisitsByResource(filterString, mockUser, userAccessibleStudies, startMonth,
                                                       endMonth, homeView, null
        )).thenReturn(expected);

        final List<CalendarVisitsResponse> actual = appointmentService.filterCalendarVisitsResponsesByResourceName(filterString, null,
                                                                                                                   mockUser,
                                                                                                                   startMonth,
                                                                                                                   endMonth,
                                                                                                                   homeView, userAccessibleStudies
        );
        assertEquals(expected, actual);
    }

    @Test
    public void testGetUngroupedTemplateResourcesByType() throws Exception {
        final VisitTemplate visitTemplate = mock(VisitTemplate.class);
        final String templateResourceType = "flexible";
        final List<TemplateResource> expected = Lists.newArrayList();
        final int visitId = 1;
        when(studyDAO.findVisitById(visitId)).thenReturn(visitTemplate);
        when(mockTemplateResourceDAO.findUngroupedTemplateResourcesTypeByVisit(visitTemplate, templateResourceType)).thenReturn
                (expected);
        final List<TemplateResource> actual = appointmentService.getUngroupedTemplateResourcesByType(visitId,
                                                                                                     templateResourceType);
        assertEquals(expected, actual);
    }

    @Test
    public void testGetVisitDetails() throws Exception {
        final User mockUser = mock(User.class);

        when(appointmentDAO.canUserSeeBookedVisit(mockUser, 1)).thenReturn(true);
        when(appointmentDAO.canUserSeeBookedVisit(mockUser, 0)).thenReturn(false);

        try {
            appointmentService.getVisitDetails(1, mockUser, "foo");

            verify(appointmentDAO, times(1)).canUserSeeBookedVisit(mockUser, 1);
        }
        catch (final Exception e) {
            // didn't expect rest of dao call to work. need to develop separate
            //  test for the other getVisitDetails() called by the one we're testing
        }

        try {
            appointmentService.getVisitDetails(0, mockUser, "foo");
        }
        catch (final Exception e) {
            verify(appointmentDAO, times(1)).canUserSeeBookedVisit(mockUser, 1);

            assertTrue(e instanceof SchedulerRuntimeException);
            assertEquals(e.getMessage(), "User not allowed to get details for requested study");
        }
    }

    @Test
    public void testGetVisitHistory() {

        String host = "123.234.345.456";
        String cancellationStatus = "cancellation";
        String fixedCancellationStatus = "cancelled";
        String otherVisitStatus = "some other status";

        String viewedHistoryActionStringForCancelledVisit = BookedVisitActivityLogStatics.getActionForViewedHistoryForAppointment(fixedCancellationStatus);
        String viewedHistoryActionStringForOtherVisit = BookedVisitActivityLogStatics.getActionForViewedHistoryForAppointment(otherVisitStatus);

        int bookedVisitId = 99;
        String eCommonsId = "user1";

        final User user0 = new User();
        user0.setId(7);
        user0.setLastName("last0");
        user0.setFirstName("first0");

        final User user1 = new User();
        user1.setId(8);
        user1.setLastName("last1");
        user1.setFirstName("first1");
        user1.setEcommonsId(eCommonsId);

        AppointmentStatus cancelledAppointmentStatus = new AppointmentStatus();
        cancelledAppointmentStatus.setName(cancellationStatus);

        AppointmentStatus otherAppointmentStatus = new AppointmentStatus();
        otherAppointmentStatus.setName(otherVisitStatus);

        BookedVisit bookedVisit1 = new BookedVisit();
        bookedVisit1.setId(99);
        bookedVisit1.setAppointmentStatus(cancelledAppointmentStatus);

        BookedVisitActivityLog bookedVisitActivityLog1 = new BookedVisitActivityLog();
        bookedVisitActivityLog1.setActionPerformed(BookedVisitActivityLogStatics.SCHEDULED.getLogString());
        bookedVisitActivityLog1.setAppointmentOverrideReason(new AppointmentOverrideReason());
        bookedVisitActivityLog1.setBookedVisit(bookedVisit1);
        Date date1 = new Date();
        bookedVisitActivityLog1.setDate(date1);
        bookedVisitActivityLog1.setId(11);
        bookedVisitActivityLog1.setIpAddress("123.456.789.000");
        bookedVisitActivityLog1.setPerformingUser(user1);

        BookedVisitActivityLog bookedVisitActivityLog2 = new BookedVisitActivityLog();
        bookedVisitActivityLog2.setActionPerformed(BookedVisitActivityLogStatics.RESCHEDULED.getLogString());
        bookedVisitActivityLog2.setAppointmentOverrideReason(new AppointmentOverrideReason());
        bookedVisitActivityLog2.setBookedVisit(bookedVisit1);
        Date date2 = new Date(date1.getTime() + 1000000);
        bookedVisitActivityLog2.setDate(date2);
        bookedVisitActivityLog2.setId(12);
        bookedVisitActivityLog2.setIpAddress("000.999.888.777");
        bookedVisitActivityLog2.setPerformingUser(null);

        List<BookedVisitActivityLog> bookedVisitActivityLogList = new ArrayList<>();
        bookedVisitActivityLogList.add(bookedVisitActivityLog1);
        bookedVisitActivityLogList.add(bookedVisitActivityLog2);

        ScheduledVisitHistoryDTO scheduledVisitHistoryDTO = new ScheduledVisitHistoryDTO(bookedVisitActivityLogList);

        when(appointmentDAO.findBookedVisitById(bookedVisitId)).thenReturn(bookedVisit1);
        doNothing().when(mockAuditService).logAppointmentActivity(host, bookedVisit1, user0,
                viewedHistoryActionStringForCancelledVisit);
        when(mockAuditService.getActivityLogForBookedVisit(bookedVisitId)).thenReturn(scheduledVisitHistoryDTO);

        ScheduledVisitHistoryDTO returnedScheduledVisitHistoryDTO = appointmentService.getVisitHistory(bookedVisitId, user0, host);

        verify(appointmentDAO, times(1)).findBookedVisitById(bookedVisitId);
        verify(mockAuditService, times(1)).logAppointmentActivity(host, bookedVisit1, user0,
                viewedHistoryActionStringForCancelledVisit);
        verify(mockAuditService, times(1)).getActivityLogForBookedVisit(bookedVisitId);

        List<Map<String, String>> returnedBookedActivityList = returnedScheduledVisitHistoryDTO.getBookedVisitActivity();
        assertEquals(2, returnedBookedActivityList.size());
        Map<String, String> returnedLogItem1 = returnedBookedActivityList.get(0);
        Map<String, String> returnedLogItem2 = returnedBookedActivityList.get(1);

        assertEquals(3, returnedLogItem1.size());
        assertEquals(BookedVisitActivityLogStatics.SCHEDULED.getLogString(), returnedLogItem1.get("action"));
        assertEquals(eCommonsId, returnedLogItem1.get("user"));
        assertEquals(DateUtility.format(date24HTime(), date1), returnedLogItem1.get("date"));

        assertEquals(3, returnedLogItem2.size());
        assertEquals(BookedVisitActivityLogStatics.RESCHEDULED.getLogString(), returnedLogItem2.get("action"));
        assertEquals(null, returnedLogItem2.get("user"));
        assertEquals(DateUtility.format(date24HTime(), date2), returnedLogItem2.get("date"));

        // another scenario.

        bookedVisit1.setAppointmentStatus(otherAppointmentStatus);

        returnedScheduledVisitHistoryDTO = appointmentService.getVisitHistory(bookedVisitId, user0, host);

        // Note: the times() function in the mockito verify() method calls
        // should match the cumulative number of calls to the specified method, not just the
        // number of calls since the last call to verify()

        verify(appointmentDAO, times(2)).findBookedVisitById(bookedVisitId);
        verify(mockAuditService, times(1)).logAppointmentActivity(host, bookedVisit1, user0,
                viewedHistoryActionStringForOtherVisit);
        verify(mockAuditService, times(2)).getActivityLogForBookedVisit(bookedVisitId);

    }

    @Test
    public void testComputeLastPeriod() {
        Date startDate = DateUtility.parse(dateHourMinSec(), "2015-11-01 00:00:00");
        Date endDate = DateUtility.parse(dateHourMinSec(), "2015-11-01 01:00:00");
        int startPeriod = appointmentService.computePeriodOfDate(startDate);
        assertEquals(0, startPeriod);
        int endPeriod = appointmentService.computePeriodOfDate(endDate);
        assertEquals(4, endPeriod);
        int lastPeriod = appointmentService.computeLastPeriod(startDate, endDate);
        assertEquals(3, lastPeriod);

        startDate = DateUtility.parse(dateHourMinSec(), "2015-11-01 01:00:00");
        endDate = DateUtility.parse(dateHourMinSec(), "2015-11-01 02:00:00");
        lastPeriod = appointmentService.computeLastPeriod(startDate, endDate);
        assertEquals(7, lastPeriod);

        startDate = DateUtility.parse(dateHourMinSec(), "2015-09-15 01:00:00");
        endDate = DateUtility.parse(dateHourMinSec(), "2015-09-15 03:00:00");
        lastPeriod = appointmentService.computeLastPeriod(startDate, endDate);
        assertEquals(11, lastPeriod);


        startDate = DateUtility.parse(dateHourMinSec(), "2016-03-08 00:00:00");
        startPeriod = appointmentService.computePeriodOfDate(startDate);
        assertEquals(0, startPeriod);
        endDate = DateUtility.parse(dateHourMinSec(), "2016-03-08 01:00:00");
        endPeriod = appointmentService.computePeriodOfDate(endDate);
        assertEquals(4, endPeriod);
        lastPeriod = appointmentService.computeLastPeriod(startDate, endDate);
        assertEquals(3, lastPeriod);

        startDate = DateUtility.parse(dateHourMinSec(), "2016-03-08 01:00:00");
        startPeriod = appointmentService.computePeriodOfDate(startDate);
        assertEquals(4, startPeriod);
        endDate = DateUtility.parse(dateHourMinSec(), "2016-03-08 02:00:00");
        endPeriod = appointmentService.computePeriodOfDate(endDate);
        assertEquals(8, endPeriod);
        lastPeriod = appointmentService.computeLastPeriod(startDate, endDate);
        assertEquals(7, lastPeriod);
    }

    void setupMocksTrVtTraLLa() { // don't care re param specifics
        setupMocksTrVtTraLLa(true, false, "blah");
    }

    void setupMocksTrVtTraLLa(
            boolean flexGroup,
            boolean floatGroup,
            String annotationName
    ) {
        trMock = mock(TemplateResource.class);
        vtMock = mock(VisitTemplate.class);
        traMock = mock(TemplateResourceAnnotations.class);
        llaMock = mock(LineLevelAnnotations.class);
        rMock = mock(Resource.class);

        TemplateResourceWithTraListDTO trwtlDto = new TemplateResourceWithTraListDTO(trMock);

        when(studyDAO.findVisitById(0)).thenReturn(vtMock);
        when(mockTemplateResourceDAO.findTemplateResourcesByVisit(vtMock, "")).thenReturn(Lists.newArrayList(trMock));
        when(mockTemplateResourceDAO.findTemplateResourcesAndAnnotationsByVisit(any(Integer.class), any(String.class)))
                .thenReturn(Lists.newArrayList(trwtlDto));

        when(mockTemplateResourceDAO.findTemplateResourceAnnotationsByTemplateResource(trMock)).thenReturn(Lists.newArrayList
                (traMock));
        when(traMock.getQuantifiedLlaName()).thenReturn(annotationName);
        when(traMock.getLineLevelAnnotations()).thenReturn(llaMock);
        when(trMock.getResource()).thenReturn(rMock);
        when(llaMock.getName()).thenReturn(annotationName);
        when(trMock.getGroupId()).thenReturn("Group1");
        when(trMock.getGroupedFlexible()).thenReturn(flexGroup);
        when(trMock.getGroupedFloatable()).thenReturn(floatGroup);

        when(trMock.getStartDate()).thenReturn(TEMPLATE_RESOURCE_DATE_ORIGIN);
        when(trMock.getEndDate()).thenReturn(TEMPLATE_RESOURCE_DATE_ORIGIN);

        when(rMock.getResourceType()).thenReturn(ResourceType.Nursing);
    }

    void getTemplateResourcesTestHelper(
            boolean flexGroup,
            boolean floatGroup,
            boolean wantAnnotationQuantities
    ) throws Exception {

        String annotationName;
        if (wantAnnotationQuantities) {
            annotationName = "1-LlaName";
        } else {
            annotationName = "LlaName";
        }

        String groupName;
        if (flexGroup) {
            groupName = "flexibleGroup1";
        } else if (floatGroup) {
            groupName = "floatableGroup1";
        } else {
            groupName = "fixed";
        }

        setupMocksTrVtTraLLa(flexGroup, floatGroup, annotationName);

        List<TemplateResource> trList = appointmentService.getTemplateResources(0, wantAnnotationQuantities);

        assertEquals("Should be 1, not " + trList.size(), 1, trList.size());
        assertEquals("Should be our mock", trList.get(0), trMock);
    }

    @Test
    public void getTemplateResourcesTest() throws Exception {

        //                              flex  float  quantities
        getTemplateResourcesTestHelper(true, false, false);
        getTemplateResourcesTestHelper(false, true, false);
        getTemplateResourcesTestHelper(false, false, false);

        getTemplateResourcesTestHelper(true, false, true);
        getTemplateResourcesTestHelper(false, true, true);
        getTemplateResourcesTestHelper(false, false, true);
    }

    void getGanttInfoDayTestHelper(
            AppointmentService service,
            boolean oneDay,
            GanttResponseDTO.GanttInfoType infoType
    ) {

        int expectedSize = oneDay ? 1 : 0;
        when(trMock.occursInOneDay()).thenReturn(oneDay);

        GanttResponseDTO result = service.getGanttInfoDay(
                Lists.newArrayList(trMock),
                0,
                infoType
        );

        if (infoType.equals(GanttResponseDTO.GanttInfoType.Events)) {
            assertTrue(result instanceof GanttEventsResponseDTO);
            assertEquals(expectedSize, ((GanttEventsResponseDTO) result).getGanttEventDetailList().size());
        } else {
            assertTrue(result instanceof GanttResourcesResponseDTO);
            assertEquals(expectedSize, ((GanttResourcesResponseDTO) result).getGanttResourceDetailList().size());
        }
    }

    @Test
    public void getGanttInfoDayTest() throws Exception {
        setupMocksTrVtTraLLa();

        when(trMock.startDayOffsetMatches(0)).thenReturn(true);
        when(trMock.isGrouped()).thenReturn(false);

        getGanttInfoDayTestHelper(appointmentService, true, GanttResponseDTO.GanttInfoType.Events);
        getGanttInfoDayTestHelper(appointmentService, false, GanttResponseDTO.GanttInfoType.Events);

        getGanttInfoDayTestHelper(appointmentService, true, GanttResponseDTO.GanttInfoType.Resources);
        getGanttInfoDayTestHelper(appointmentService, false, GanttResponseDTO.GanttInfoType.Resources);
    }

    void getGanttInfoMultiTestHelper(
            AppointmentService service,
            boolean oneDay,
            GanttResponseDTO.GanttInfoType infoType
    ) {

        int expectedSize = oneDay ? 0 : 1;
        when(trMock.occursInOneDay()).thenReturn(oneDay);

        GanttResponseDTO result = service.getGanttInfoMulti(
                Lists.newArrayList(trMock),
                0,
                infoType
        );

        if (infoType.equals(GanttResponseDTO.GanttInfoType.Events)) {
            assertTrue(result instanceof GanttEventsResponseDTO);
            assertEquals(expectedSize, ((GanttEventsResponseDTO) result).getGanttEventDetailList().size());
        } else {
            assertTrue(result instanceof GanttResourcesResponseDTO);
            assertEquals(expectedSize, ((GanttResourcesResponseDTO) result).getGanttResourceDetailList().size());
        }
    }

    @Test
    public void getGanttInfoMultiTest() throws Exception {
        setupMocksTrVtTraLLa();

        when(trMock.resourceOverlapsDayOffset(0)).thenReturn(true);
        when(trMock.isGrouped()).thenReturn(false);

        getGanttInfoMultiTestHelper(appointmentService, true, GanttResponseDTO.GanttInfoType.Events);
        getGanttInfoMultiTestHelper(appointmentService, false, GanttResponseDTO.GanttInfoType.Events);

        getGanttInfoMultiTestHelper(appointmentService, true, GanttResponseDTO.GanttInfoType.Resources);
        getGanttInfoMultiTestHelper(appointmentService, false, GanttResponseDTO.GanttInfoType.Resources);
    }


    AppointmentService appointmentServiceSpy;
    SubjectMrn switchedSubjectMrn;
    BookedVisit bookedVisitForSwitchedSubject;

    private void setUpSwitchSubjectTests() {

        appointmentServiceSpy = spy(appointmentService);

        Subject switchedSubject = new Subject();
        switchedSubject.setId(SUBJECT_ID);
        switchedSubject.setFullName(FULL_NAME);
        switchedSubjectMrn = new SubjectMrn();
        switchedSubjectMrn.setSubject(switchedSubject);
        switchedSubjectMrn.setId(SUBJECT_MRN_ID);

        Study study = new Study();
        study.setId(STUDY_ID);
        study.setLocalId("Study Local ID");

        VisitType visitType = new VisitType();
        visitType.setName(TestUtils.InpatientCRC);
        visitType.setInpatient(true);

        VisitTemplate visitTemplate = new VisitTemplate();
        visitTemplate.setVisitType(visitType);

        bookedVisitForSwitchedSubject = new BookedVisit();
        bookedVisitForSwitchedSubject.setId(BOOKED_VISIT_ID);
        bookedVisitForSwitchedSubject.setStudy(study);
        bookedVisitForSwitchedSubject.setVisitTemplate(visitTemplate);
        bookedVisitForSwitchedSubject.setName("Visit name");
        bookedVisitForSwitchedSubject.setScheduledStartTime(new Date());
        bookedVisitForSwitchedSubject.setScheduledEndTime(new Date());
        bookedVisitForSwitchedSubject.setVisitType(visitType);

        // mock some method calls

        when(appointmentDAO.findBookedVisitById(VISIT_ID)).thenReturn(bookedVisitForSwitchedSubject);

        when(appointmentDAO.switchVisitSubject(refEq(new Integer(SUBJECT_MRN_ID)), refEq(bookedVisitForSwitchedSubject), anyBoolean(), anyString())).thenReturn(
                new SwitchSubjectResultDTO(
                        true,
                        new Integer(SUBJECT_MRN_ID),
                        FULL_NAME,
                        null,
                        false,
                        false
                ));
        when(appointmentDAO.switchVisitSubject(refEq(new Integer(BLOCK_SUBJECT_MRN_ID)), refEq(bookedVisitForSwitchedSubject), anyBoolean(), anyString())).thenReturn(
                new SwitchSubjectResultDTO(
                        true,
                        new Integer(BLOCK_SUBJECT_MRN_ID),
                        FULL_NAME,
                        null,
                        false,
                        false
                ));

        when(subjectDAO.findSubjectMrnById(new Integer(SUBJECT_MRN_ID))).thenReturn(switchedSubjectMrn);

    }

    @Test
    public void test_switchVisitSubjectBlockVisit() {

        this.setUpSwitchSubjectTests();

        // case 0: no subject, block visit

        SwitchSubjectResultDTO resultDto = appointmentServiceSpy.switchVisitSubject(BLOCK_SUBJECT_MRN_ID, VISIT_ID, true, "hello");

        // verify that the made-up result DTO gets returned from the service method, when it gets it from the DAO
        verify(appointmentDAO, times(1)).switchVisitSubject(BLOCK_SUBJECT_MRN_ID, bookedVisitForSwitchedSubject, true, "hello");

        assertTrue(resultDto.isResult());
        assertEquals(new Integer(BLOCK_SUBJECT_MRN_ID), resultDto.getNewSubjectMrnId());
        assertEquals(FULL_NAME, resultDto.getFullName());
        assertFalse(resultDto.getGenderBlockWarning());
        assertFalse(resultDto.getDoubleBookingWarning());

    }

    @Test
    public void test_switchVisitSubjectNoWarnings() {

        this.setUpSwitchSubjectTests();

        // case 1: no warnings

        when(appointmentDAO.subjectHasBookedVisitInDateRange(anyInt(), anyObject(), anyObject())).thenReturn(false);
        doReturn(null).when(appointmentServiceSpy).checkForGenderBlock(anyObject(), anyObject());

        SwitchSubjectResultDTO resultDto = appointmentServiceSpy.switchVisitSubject(SUBJECT_MRN_ID, VISIT_ID, false, "bye");

        verify(appointmentDAO, times(1)).switchVisitSubject(SUBJECT_MRN_ID, bookedVisitForSwitchedSubject, false, "bye");
        assertTrue(resultDto.isResult());
        assertEquals(new Integer(SUBJECT_MRN_ID), resultDto.getNewSubjectMrnId());
        assertEquals(FULL_NAME, resultDto.getFullName());
        assertFalse(resultDto.getGenderBlockWarning());
        assertFalse(resultDto.getDoubleBookingWarning());

    }


    @Test
    public void test_switchVisitSubjectGenderBlockWarning() {

        this.setUpSwitchSubjectTests();// case 2: gender block warning

        when(appointmentDAO.subjectHasBookedVisitInDateRange(anyInt(), anyObject(), anyObject())).thenReturn(false);
        doReturn("something").when(appointmentServiceSpy).checkForGenderBlock(anyObject(), anyObject());

        SwitchSubjectResultDTO resultDto = appointmentServiceSpy.switchVisitSubject(SUBJECT_MRN_ID, VISIT_ID, true, "hello");

        verify(appointmentDAO, never()).switchVisitSubject(anyInt(), anyObject(), anyBoolean(), anyString());
        assertFalse(resultDto.isResult());
        assertEquals(new Integer(SUBJECT_MRN_ID), resultDto.getNewSubjectMrnId());
        assertEquals(FULL_NAME, resultDto.getFullName());
        assertTrue(resultDto.getGenderBlockWarning());
        assertFalse(resultDto.getDoubleBookingWarning());

    }


    @Test
    public void test_switchVisitSubjectDoubleBookedWarning() {

        this.setUpSwitchSubjectTests();

        // case 3: double booked warning

        when(appointmentDAO.subjectHasBookedVisitInDateRange(anyInt(), anyObject(), anyObject())).thenReturn(true);
        doReturn(null).when(appointmentServiceSpy).checkForGenderBlock(anyObject(), anyObject());

        SwitchSubjectResultDTO resultDto = appointmentServiceSpy.switchVisitSubject(SUBJECT_MRN_ID, VISIT_ID, true, "hello");

        verify(appointmentDAO, never()).switchVisitSubject(anyInt(), anyObject(), anyBoolean(), anyString());
        assertFalse(resultDto.isResult());
        assertEquals(new Integer(SUBJECT_MRN_ID), resultDto.getNewSubjectMrnId());
        assertEquals(FULL_NAME, resultDto.getFullName());
        assertFalse(resultDto.getGenderBlockWarning());
        assertTrue(resultDto.getDoubleBookingWarning());

    }


    @Test
    public void test_switchVisitSubjectGenderBlockAndDoubleBookedWarnings() {

        this.setUpSwitchSubjectTests();
        // case 5: both warnings

        when(appointmentDAO.subjectHasBookedVisitInDateRange(anyInt(), anyObject(), anyObject())).thenReturn(true);
        doReturn("something").when(appointmentServiceSpy).checkForGenderBlock(anyObject(), anyObject());

        SwitchSubjectResultDTO resultDto = appointmentServiceSpy.switchVisitSubject(SUBJECT_MRN_ID, VISIT_ID, true, "hello");

        verify(appointmentDAO, never()).switchVisitSubject(anyInt(), anyObject(), anyBoolean(), anyString());
        assertFalse(resultDto.isResult());
        assertEquals(new Integer(SUBJECT_MRN_ID), resultDto.getNewSubjectMrnId());
        assertEquals(FULL_NAME, resultDto.getFullName());
        assertTrue(resultDto.getGenderBlockWarning());
        assertTrue(resultDto.getDoubleBookingWarning());

    }


    @Test
    public void test_confirmSwitchVisitSubject() {

        Subject subject = new Subject();
        SubjectMrn subjectMrn = new SubjectMrn();
        subjectMrn.setSubject(subject);

        final BookedVisit bookedVisit = mock(BookedVisit.class);

        when(appointmentDAO.switchVisitSubject(SUBJECT_MRN_ID, bookedVisit, true, "hello")).thenReturn(new SwitchSubjectResultDTO());
        when(appointmentDAO.switchVisitSubject(BLOCK_SUBJECT_MRN_ID, bookedVisit, false, "bye")).thenReturn(new SwitchSubjectResultDTO());
        when(appointmentDAO.findBookedVisitById(BOOKED_VISIT_ID)).thenReturn(bookedVisit);
        when(subjectDAO.findSubjectMrnById(SUBJECT_MRN_ID)).thenReturn(subjectMrn);

        appointmentService.confirmSwitchVisitSubject(SUBJECT_MRN_ID, BOOKED_VISIT_ID, true, "hello");
        verify(this.appointmentDAO, times(1)).switchVisitSubject(SUBJECT_MRN_ID, bookedVisit, true, "hello");

        appointmentService.confirmSwitchVisitSubject(BLOCK_SUBJECT_MRN_ID, BOOKED_VISIT_ID, false, "bye");
        verify(this.appointmentDAO, times(1)).switchVisitSubject(BLOCK_SUBJECT_MRN_ID, bookedVisit, false, "bye");
    }

    /**
     * Features to be tested:
     * - From the list of all to-be-booked resources, picks only the ones for a (any) shared-room resource
     * - Check every booked shared-room resource for gender block with a shared-resource booking of incompatible gender
     * - accumulates and returns messages from called method checkForGenderBlockInBookingsOfSharedResource()
     * - returns null instead of empty String, for caller's convenience
     */
    // TODO-XH : factor out the set up code from this test which is also used in the following test
    @Test
    public void testCheckForGenderBlock() {

        MockConflictChecker checker = new MockConflictChecker(true);
        MockAppointmentConfirmer confirmer = new MockAppointmentConfirmer();
        AppointmentService service = makeService(checker, confirmer);
        AppointmentService serviceSpy = spy(service);

        BookedVisit bookedVisit = new BookedVisit();

        Study study = new Study();
        Subject subject = new Subject();
        StudySubject studySubject = new StudySubject();
        SubjectMrn subjectMrn = new SubjectMrn();
        subjectMrn.setMrn("mrn");
        subjectMrn.setSubject(subject);
        bookedVisit.setSubjectMrn(subjectMrn);

        Resource resource1 = new Resource();
        Resource resource2 = new Resource();
        Resource resource3 = new Resource();

        resource1.setId(1);
        resource2.setId(2);
        resource3.setId(3);

        // this is the shared resource for resource1. See below for mocking: when(resourceDAO.findResourceById(1))....
        Resource resource1shared = new Resource();
        resource1shared.setId(4);
        resource1shared.setSharedResource(1);
        resource1.setSharedResource(4);

        // this is the shared resource for resource1. See below for mocking: when(resourceDAO.findResourceById(1))....
        Resource resource2shared = new Resource();
        resource2shared.setId(5);
        resource2shared.setSharedResource(2);
        resource2.setSharedResource(5);

        String resourceName1 = "Shared Room 1 - Bed A";
        String resourceName1shared = "Shared Room 1 - Bed B";
        String resourceName2 = "Shared Room 2 - Bed A";
        String resourceName2shared = "Shared Room 2 - Bed B";
        String resourceName3 = "Lab";

        resource1.setName(resourceName1);
        resource2.setName(resourceName2);
        resource3.setName(resourceName3);
        resource1shared.setName(resourceName1shared);
        resource2shared.setName(resourceName2shared);

        resource1.setResourceType(ResourceType.Room);
        resource2.setResourceType(ResourceType.Room);
        resource3.setResourceType(ResourceType.Lab);
        resource1shared.setResourceType(ResourceType.Room);
        resource2shared.setResourceType(ResourceType.Room);

        BookedResource bookedResource1 = new BookedResource();
        BookedResource bookedResource2 = new BookedResource();
        BookedResource bookedResource3 = new BookedResource();

        bookedResource1.setResource(resource1);
        bookedResource2.setResource(resource2);
        bookedResource3.setResource(resource3);

        List<BookedResource> bookedResources = new ArrayList<>();
        bookedResources.add(bookedResource1);
        bookedResources.add(bookedResource2);
        bookedResources.add(bookedResource3);
        bookedVisit.setBookedResourceList(bookedResources);

        doReturn("checkForGenderBlockInBookingsOfSharedResource called for " + bookedResource1.getResource().getName()).when(serviceSpy).checkForGenderBlockInBookingsOfSharedResource(refEq(bookedResource1), any());
        doReturn("checkForGenderBlockInBookingsOfSharedResource called for " + bookedResource2.getResource().getName()).when(serviceSpy).checkForGenderBlockInBookingsOfSharedResource(refEq(bookedResource2), any());
        doReturn("checkForGenderBlockInBookingsOfSharedResource called in ERROR for " + bookedResource3.getResource().getName()).when(serviceSpy).checkForGenderBlockInBookingsOfSharedResource(refEq(bookedResource3), any());

        when(resourceDAO.findResourceById(1)).thenReturn(resource1);
        when(resourceDAO.findResourceById(2)).thenReturn(resource2);
        when(resourceDAO.findResourceById(3)).thenReturn(resource3);
        when(resourceDAO.findResourceById(4)).thenReturn(resource1shared);
        when(resourceDAO.findResourceById(5)).thenReturn(resource2shared);

        // Payload happens here
        Subject newSubject = subjectMrn.getSubject();
        String result = serviceSpy.checkForGenderBlock(newSubject, bookedResources);

        // never called with resources other than resource1 or resource2
        verify(serviceSpy, never()).checkForGenderBlockInBookingsOfSharedResource(AdditionalMatchers.not(AdditionalMatchers.or(Matchers.refEq(bookedResource1), Matchers.refEq(bookedResource2))), anyObject());
        // called only once with resource1
        verify(serviceSpy, times(1)).checkForGenderBlockInBookingsOfSharedResource(refEq(bookedResource1), anyObject());
        verify(serviceSpy, times(1)).checkForGenderBlockInBookingsOfSharedResource(refEq(bookedResource2), anyObject());
        // called only once with resource1, resource1shared, subject. In light of the verify() call just above,
        // this means that the only call made is the one below:
        verify(serviceSpy, times(1)).checkForGenderBlockInBookingsOfSharedResource(refEq(bookedResource1), refEq(subject));
        verify(serviceSpy, times(1)).checkForGenderBlockInBookingsOfSharedResource(refEq(bookedResource2), refEq(subject));

        assertEquals(
                "checkForGenderBlockInBookingsOfSharedResource called for " + bookedResource1.getResource().getName() +
                "checkForGenderBlockInBookingsOfSharedResource called for " + bookedResource2.getResource().getName(),
                result);

    }

    /**
     * This is a regression test for a bug that I introduced while refactoring and modifying the
     * return values from checkForGenderBlockInBookingsOfSharedResource() to checkForGenderBlock()
     * Before refactoring, the inner method would return null if there was no block, and the
     * outer method would only add to its return message if the inner method returned a non-null string.
     * After refactoring, the inner method would still return null (and not an empty string) and the
     * outer method would add the inner method's output to its return message regardless of the value.
     */
    @Test
    public void testCheckForGenderBlockWhereNoBlockIsFound() {

        MockConflictChecker checker = new MockConflictChecker(true);
        MockAppointmentConfirmer confirmer = new MockAppointmentConfirmer();
        AppointmentService service = makeService(checker, confirmer);
        AppointmentService serviceSpy = spy(service);

        BookedVisit bookedVisit = new BookedVisit();

        Study study = new Study();
        Subject subject = new Subject();

        SubjectMrn subjectMrn = new SubjectMrn();
        subjectMrn.setSubject(subject);
        bookedVisit.setSubjectMrn(subjectMrn);

        Resource resource1 = new Resource();
        Resource resource2 = new Resource();
        Resource resource3 = new Resource();

        resource1.setId(1);
        resource2.setId(2);
        resource3.setId(3);

        // this is the shared resource for resource1. See below for mocking: when(resourceDAO.findResourceById(1))....
        Resource resource1shared = new Resource();
        resource1shared.setId(4);
        resource1shared.setSharedResource(1);
        resource1.setSharedResource(4);

        // this is the shared resource for resource1. See below for mocking: when(resourceDAO.findResourceById(1))....
        Resource resource2shared = new Resource();
        resource2shared.setId(5);
        resource2shared.setSharedResource(2);
        resource2.setSharedResource(5);

        String resourceName1 = "Shared Room 1 - Bed A";
        String resourceName1shared = "Shared Room 1 - Bed B";
        String resourceName2 = "Shared Room 2 - Bed A";
        String resourceName2shared = "Shared Room 2 - Bed B";
        String resourceName3 = "Lab";

        resource1.setName(resourceName1);
        resource2.setName(resourceName2);
        resource3.setName(resourceName3);
        resource1shared.setName(resourceName1shared);
        resource2shared.setName(resourceName2shared);

        resource1.setResourceType(ResourceType.Room);
        resource2.setResourceType(ResourceType.Room);
        resource3.setResourceType(ResourceType.Lab);
        resource1shared.setResourceType(ResourceType.Room);
        resource2shared.setResourceType(ResourceType.Room);

        BookedResource bookedResource1 = new BookedResource();
        BookedResource bookedResource2 = new BookedResource();
        BookedResource bookedResource3 = new BookedResource();

        bookedResource1.setResource(resource1);
        bookedResource2.setResource(resource2);
        bookedResource3.setResource(resource3);

        List<BookedResource> bookedResources = new ArrayList<>();
        bookedResources.add(bookedResource1);
        bookedResources.add(bookedResource2);
        bookedResources.add(bookedResource3);
        bookedVisit.setBookedResourceList(bookedResources);

        when(resourceDAO.findResourceById(1)).thenReturn(resource1);
        when(resourceDAO.findResourceById(2)).thenReturn(resource2);
        when(resourceDAO.findResourceById(3)).thenReturn(resource3);
        when(resourceDAO.findResourceById(4)).thenReturn(resource1shared);
        when(resourceDAO.findResourceById(5)).thenReturn(resource2shared);

        // Payload happens here
        Subject newSubject = subjectMrn.getSubject();
        String result = serviceSpy.checkForGenderBlock(newSubject, bookedResources);

        assertEquals(null, result);

    }

    @Test
    public void testCheckIfDoubleRoomBeingUsed() {

        //this id is used in two places, so extracted out to avoid magic numbers in the code
        final int resourceAlreadyBookedId = 1;

        String resourceName = "Room 1 - shared - Bed A";
        String sharedResourceName = "Room 1 - shared - Bed B";

        // Goodies used for setting up different scenarios

        Date now = new Date();
        long nowMilliseconds = now.getTime();

        Date date1 = new Date(nowMilliseconds);
        Date date2 = new Date(nowMilliseconds + 1);
        Date date3 = new Date(nowMilliseconds + 2);
        Date date4 = new Date(nowMilliseconds + 3);

        Gender maleGender = new Gender();
        maleGender.setCode("M");
        maleGender.setName("Male");
        Gender femaleGender = new Gender();
        femaleGender.setCode("F");
        femaleGender.setName("Female");

        // build the appointment service with some mocks where needed

        MockConflictChecker checker = new MockConflictChecker(true);
        MockAppointmentConfirmer confirmer = new MockAppointmentConfirmer();
        AppointmentService service = makeService(checker, confirmer);

        // Setup the objects for the new appointment

        Subject subjectToBeBooked = new Subject();

        Resource resourceToBeBooked = new Resource();
        resourceToBeBooked.setName(resourceName);
        resourceToBeBooked.setSharedResource(resourceAlreadyBookedId);

        BookedResource newApptBookedResource = new BookedResource();
        newApptBookedResource.setResource(resourceToBeBooked);

        // Set up the objects for the existing appointments

        Subject existingSubject = new Subject();

        // This one is the matching shared resource (see how resourceDAO.findResourceById() is mocked below) with is double-booked
        Resource resourceAlreadyBooked1 = new Resource();
        resourceAlreadyBooked1.setName(sharedResourceName);

        BookedResource existingBookedResource = new BookedResource();
        existingBookedResource.setResource(resourceAlreadyBooked1);
        final List<BookedResource> existingBookedResources = new ArrayList<>();
        existingBookedResources.add(existingBookedResource);

        SubjectMrn subjectMrn = new SubjectMrn();
        subjectMrn.setMrn("mrn");
        subjectMrn.setSubject(existingSubject);

        BookedVisit existingBookedVisit = new BookedVisit();
        existingBookedVisit.setSubjectMrn(subjectMrn);
        existingBookedResource.setBookedVisit(existingBookedVisit);

        final List<BookedResource> bookedSharedResources = new ArrayList<>();
        BookedResource bookedSharedResource = mock(BookedResource.class);
        bookedSharedResources.add(bookedSharedResource);

        // Mock methods that are called from within the method under test (
        when(appointmentDAO.findBookedResources(resourceAlreadyBooked1)).thenReturn(existingBookedResources);
        when(resourceDAO.findResourceById(resourceAlreadyBookedId)).thenReturn(resourceAlreadyBooked1);

        class TestInput {
            int index; // useful for debugging, bcause otherwise an assertion failure cannot be associated with a specific test input
            GenderType genderTypeOfSubjectToBeBooked;
            GenderType genderTypeOfExistingSubject;
            Gender genderObjectOfSubjectToBeBooked;
            Gender genderObjectOfExistingSubject;
            Date newBookingStartDate;
            Date newBookingEndDate;
            Date existingBookingStartDate;
            Date existingBookingEndDate;

            String expectedString;

            TestInput(
                    int index,
                    GenderType genderTypeOfSubjectToBeBooked,
                    GenderType genderTypeOfExistingSubject,
                    Gender genderObjectOfSubjectToBeBooked,
                    Gender genderObjectOfExistingSubject,
                    Date newBookingStartDate,
                    Date newBookingEndDate,
                    Date existingBookingStartDate,
                    Date existingBookingEndDate,
                      String expectedString) {
                this.index = index;
                this.genderTypeOfSubjectToBeBooked = genderTypeOfSubjectToBeBooked;
                this.genderTypeOfExistingSubject = genderTypeOfExistingSubject;
                this.genderObjectOfSubjectToBeBooked = genderObjectOfSubjectToBeBooked;
                this.genderObjectOfExistingSubject = genderObjectOfExistingSubject;
                this.newBookingStartDate = newBookingStartDate;
                this.newBookingEndDate = newBookingEndDate;
                this.existingBookingStartDate = existingBookingStartDate;
                this.existingBookingEndDate = existingBookingEndDate;
                this.expectedString = expectedString;
            }

        }

        TestInput[] testInputs = new TestInput[]{

                // Test case:
                // - exact same time
                // - GenderType's M and F
                // - gender object F and F
                // etc.
                new TestInput(1, GenderType.M, GenderType.M, femaleGender, maleGender, date1, date2, date1, date2, ""),
                new TestInput(2, GenderType.M, GenderType.F, femaleGender, femaleGender, date1, date2, date1, date2, sharedResourceName + " (Shared Private room) has been booked for a Subject whose Sex is Female"),

                new TestInput(3, GenderType.F, GenderType.F, maleGender, femaleGender, date1, date2, date1, date2, ""),
                new TestInput(4, GenderType.F, GenderType.M, femaleGender, femaleGender, date1, date2, date1, date2, sharedResourceName + " (Shared Private room) has been booked for a Subject whose Sex is Male"),

                new TestInput(5, GenderType.MF, GenderType.MF, femaleGender, maleGender, date1, date2, date1, date2, ""),
                new TestInput(6, GenderType.MF, GenderType.F, femaleGender, femaleGender, date1, date2, date1, date2, sharedResourceName + " (Shared Private room) has been booked for a Subject whose Sex is Female"),

                new TestInput(7, GenderType.FM, GenderType.FM, maleGender, femaleGender, date1, date2, date1, date2, ""),
                new TestInput(8, GenderType.FM, GenderType.M, femaleGender, femaleGender, date1, date2, date1, date2, sharedResourceName + " (Shared Private room) has been booked for a Subject whose Sex is Male"),


                new TestInput(9, GenderType.UNREPORTED, GenderType.UNREPORTED, maleGender, maleGender, date1, date2, date1, date2, ""),
                new TestInput(10, GenderType.UNREPORTED, GenderType.M, maleGender, maleGender, date1, date2, date1, date2, sharedResourceName + " (Shared Private room) has been booked for a Subject whose Sex is Male"),
                new TestInput(11, GenderType.DECLINED, GenderType.DECLINED, maleGender, maleGender, date1, date2, date1, date2, ""),
                new TestInput(12, GenderType.DECLINED, GenderType.M, maleGender, maleGender, date1, date2, date1, date2, sharedResourceName + " (Shared Private room) has been booked for a Subject whose Sex is Male"),

                // now test time boundaries and overlap

                new TestInput(13, GenderType.M, GenderType.F, null, null, date1, date2, date3, date4, ""),
                new TestInput(14, GenderType.M, GenderType.F, null, null, date3, date4, date1, date2, ""),

                new TestInput(15, GenderType.M, GenderType.F, null, null, date1, date2, date1, date2, sharedResourceName + " (Shared Private room) has been booked for a Subject whose Sex is Female"),

                new TestInput(16, GenderType.M, GenderType.F, null, null, date1, date2, date2, date3, ""),
                new TestInput(17, GenderType.M, GenderType.F, null, null, date1, date3, date1, date2, sharedResourceName + " (Shared Private room) has been booked for a Subject whose Sex is Female"),
                new TestInput(18, GenderType.M, GenderType.F, null, null, date1, date3, date2, date3, sharedResourceName + " (Shared Private room) has been booked for a Subject whose Sex is Female"),

                new TestInput(19, GenderType.M, GenderType.F, null, null, date2, date3, date1, date2, ""),
                new TestInput(20, GenderType.M, GenderType.F, null, null, date1, date2, date1, date3, sharedResourceName + " (Shared Private room) has been booked for a Subject whose Sex is Female"),
                new TestInput(21, GenderType.M, GenderType.F, null, null, date2, date3, date1, date3, sharedResourceName + " (Shared Private room) has been booked for a Subject whose Sex is Female"),
                new TestInput(22, GenderType.M, GenderType.F, null, null, date2, date4, date1, date3, sharedResourceName + " (Shared Private room) has been booked for a Subject whose Sex is Female")

        };

        for (int i=0; i<testInputs.length; i++) {

            TestInput input = testInputs[i];

            // set gender
            subjectToBeBooked.setGenderType(input.genderTypeOfSubjectToBeBooked);
            existingSubject.setGenderType(input.genderTypeOfExistingSubject);
            subjectToBeBooked.setGender(input.genderObjectOfSubjectToBeBooked);
            existingSubject.setGender(input.genderObjectOfExistingSubject);

            // set time spans
            newApptBookedResource.setScheduledStartTime(input.newBookingStartDate);
            newApptBookedResource.setScheduledEndTime(input.newBookingEndDate);
            existingBookedResource.setScheduledStartTime(input.existingBookingStartDate);
            existingBookedResource.setScheduledEndTime(input.existingBookingEndDate);

            // get output
            String result = service.checkForGenderBlockInBookingsOfSharedResource(newApptBookedResource, subjectToBeBooked);

            // compare to expected result
            assertEquals(input.expectedString, result);

        }

    }


    // Regression test for HCCRCSCHEDULING-5307. If the subjectMrn of the other side of the shared resource is NULL,
    // code would throw an exception trying to dereference the subject off of the null subjectMrn
    @Test
    public void testCheckIfDoubleRoomBeingUsedWhenExistingBookingIsHold() {

        //this id is used in two places, so extracted out to avoid magic numbers in the code
        final int resourceAlreadyBookedId = 1;

        String resourceName = "Room 1 - shared - Bed A";
        String sharedResourceName = "Room 1 - shared - Bed B";

        // Goodies used for setting up different scenarios

        Date now = new Date();
        long nowMilliseconds = now.getTime();

        Date date1 = new Date(nowMilliseconds);
        Date date2 = new Date(nowMilliseconds + 1);

        Gender maleGender = new Gender();
        maleGender.setCode("M");
        maleGender.setName("Male");

        // build the appointment service with some mocks where needed

        MockConflictChecker checker = new MockConflictChecker(true);
        MockAppointmentConfirmer confirmer = new MockAppointmentConfirmer();
        AppointmentService service = makeService(checker, confirmer);

        // Setup the objects for the new appointment

        Subject subjectToBeBooked = new Subject();
        subjectToBeBooked.setGenderType(GenderType.M);
        subjectToBeBooked.setGender(maleGender);

        Resource resourceToBeBooked = new Resource();
        resourceToBeBooked.setName(resourceName);
        resourceToBeBooked.setSharedResource(resourceAlreadyBookedId);

        BookedResource newApptBookedResource = new BookedResource();
        newApptBookedResource.setResource(resourceToBeBooked);
        newApptBookedResource.setScheduledStartTime(date1);
        newApptBookedResource.setScheduledEndTime(date2);

        // Set up the objects for the existing appointments

        Subject existingSubject = new Subject();

        // This one is the matching shared resource (see how resourceDAO.findResourceById() is mocked below) with is double-booked
        Resource resourceAlreadyBooked1 = new Resource();
        resourceAlreadyBooked1.setName(sharedResourceName);

        BookedResource existingBookedResource = new BookedResource();
        existingBookedResource.setResource(resourceAlreadyBooked1);
        final List<BookedResource> existingBookedResources = new ArrayList<>();
        existingBookedResources.add(existingBookedResource);

        BookedVisit existingBookedVisit = new BookedVisit();
        // Make it a blocked visit (hold)
        existingBookedVisit.setSubjectMrn(null);
        existingBookedResource.setBookedVisit(existingBookedVisit);
        existingBookedResource.setScheduledStartTime(date1);
        existingBookedResource.setScheduledEndTime(date2);

        final List<BookedResource> bookedSharedResources = new ArrayList<>();
        BookedResource bookedSharedResource = mock(BookedResource.class);
        bookedSharedResources.add(bookedSharedResource);

        // Mock methods that are called from within the method under test (
        when(appointmentDAO.findBookedResources(resourceAlreadyBooked1)).thenReturn(existingBookedResources);
        when(resourceDAO.findResourceById(resourceAlreadyBookedId)).thenReturn(resourceAlreadyBooked1);

        // get output
        String result = service.checkForGenderBlockInBookingsOfSharedResource(newApptBookedResource, subjectToBeBooked);

        // compare to expected result
        assertEquals("", result);

    }

    @Test
    public void testIsSharedRoom() {

        // build the appointment service with some mocks where needed

        MockConflictChecker checker = new MockConflictChecker(true);
        MockAppointmentConfirmer confirmer = new MockAppointmentConfirmer();
        AppointmentService service = makeService(checker, confirmer);

        Resource resource = new Resource();
        Resource sharedResource = new Resource();

        when(resourceDAO.findResourceById(0)).thenReturn(null);
        when(resourceDAO.findResourceById(1)).thenReturn(sharedResource);

        resource.setResourceType(ResourceType.Room);
        resource.setSharedResource(1);
        assertTrue(service.isSharedRoom(resource));

        resource.setResourceType(ResourceType.Room);
        resource.setSharedResource(null);
        assertFalse(service.isSharedRoom(resource));

        resource.setResourceType(ResourceType.Room);
        resource.setSharedResource(0);
        assertFalse(service.isSharedRoom(resource));

        resource.setResourceType(ResourceType.Lab);
        resource.setSharedResource(1);
        assertFalse(service.isSharedRoom(resource));

        resource.setResourceType(ResourceType.Lab);
        resource.setSharedResource(null);
        assertFalse(service.isSharedRoom(resource));

        resource.setResourceType(ResourceType.Lab);
        resource.setSharedResource(0);
        assertFalse(service.isSharedRoom(resource));

    }

    @Test
    public void testGetOverbookTimelineData()
    {
        Date selectedStartDate = DateUtility.parse(DateUtility.monthDayYear(), "12/12/2016");
        final Date selectedEndDate = DateUtility.parse(DateUtility.monthDayYear(), "12/15/2016");
        final ResourceType resourceType = ResourceType.Room;
        final List<Integer> sublocations = Arrays.asList(200, 300);
        final String orderBy = "ASC";
        final User user = new User();

        int id = 4;
        String resourceName = "Resource 200";
        String rooms = "ROOM1";
        String visitHoverStartTime = "02/10/2017 07:00";
        String visitHoverEndTime = "02/11/2017 07:00";
        String resourceHoverStartTime = "02/10/2017 07:00";
        String resourceHoverEndTime = "02/11/2017 07:00";
        String visitName = "Visit88";
        String piName = "staff900";
        String studyLocalId = "Local-100";
        String subjectFirstName = "TestUserFirst";
        String subjectLastName = "TestUserLast";
        long startMinutes = 0;
        long endMinutes = 90;
        long selectedMinutes = 0;
        List<Map<String, String>> defaultResourceSchedule = new ArrayList<>();
        Map defaultResourceScheduleItem = new HashMap<>();
        defaultResourceScheduleItem.put("defaultScheduleTime", "WED: 07:00 - 18:00");
        defaultResourceScheduleItem.put("defaultScheduleQuantity", 1);
        defaultResourceSchedule.add(defaultResourceScheduleItem);

        List<Map<String, String>> temporaryResourceSchedule = new ArrayList<>();
        Map tempResourceScheduleItem = new HashMap<>();
        tempResourceScheduleItem.put("exceptionScheduleTime", "2/17/2017 07:00 - 2/20/2017 22:00");
        tempResourceScheduleItem.put("exceptionScheduleTime", 2);
        temporaryResourceSchedule.add(tempResourceScheduleItem);

        String alternateResources = "OtherAlternateResource";
        int resourceCount = 1;

        OverbookTimelineDataResponseDTO overbookTimelineDataResponseDTO1 = new OverbookTimelineDataResponseDTO(id,
            resourceName, rooms, visitHoverStartTime, visitHoverEndTime, resourceHoverStartTime, resourceHoverEndTime,
            visitName, piName, studyLocalId, subjectFirstName, subjectLastName, startMinutes, endMinutes,
            selectedMinutes, defaultResourceSchedule, temporaryResourceSchedule, alternateResources, resourceCount);

        List<OverbookTimelineDataResponseDTO> overbookTimelineDataResponse = new ArrayList<>();
        overbookTimelineDataResponse.add(overbookTimelineDataResponseDTO1);
        when(appointmentDAO.getOverbookTimelineData(refEq(selectedStartDate),
                                                    refEq(selectedEndDate), refEq(resourceType), refEq(sublocations),
                                                    eq(orderBy), refEq(user)))
                                                    .thenReturn(overbookTimelineDataResponse);

        List<OverbookTimelineDataResponseDTO> overbookTimelineDataResponseDTOList =
                appointmentService.getOverbookTimelineData(selectedStartDate, selectedEndDate, resourceType.getId(), sublocations, orderBy, user);
        verify(this.appointmentDAO, times(1)).getOverbookTimelineData(selectedStartDate,
                                                                    selectedEndDate, resourceType, sublocations, orderBy, user);

        assertEquals(overbookTimelineDataResponseDTOList.size(), 1);

        OverbookTimelineDataResponseDTO overbookTimelineDataResponseDTO = overbookTimelineDataResponseDTOList.get(0);
        assertEquals(overbookTimelineDataResponseDTO.getId(), id);
        assertEquals(overbookTimelineDataResponseDTO.getResourceHoverStartTime(), resourceHoverStartTime);
        assertEquals(overbookTimelineDataResponseDTO.getResourceHoverEndTime(), resourceHoverEndTime);
        assertEquals(overbookTimelineDataResponseDTO.getDefaultResourceSchedule(), defaultResourceSchedule);
        assertEquals(overbookTimelineDataResponseDTO.getSubjectFirstName(), subjectFirstName);
        assertEquals(overbookTimelineDataResponseDTO.getVisitHoverStartTime(), visitHoverStartTime);
        assertEquals(overbookTimelineDataResponseDTO.getVisitHoverEndTime(), visitHoverEndTime);
        assertEquals(overbookTimelineDataResponseDTO.getAlternateResources(), alternateResources);

        List<Map<String, String>> tempResourceSchedule = overbookTimelineDataResponseDTO.getTemporaryResourceSchedule();
        //assertEquals(overbookTimelineDataResponseDTO.getTemporaryResourceSchedule(), tempResourceSchedule);
    }

    class ObjectsForTestConfirmer {

        AppointmentService serviceSpy = spy(appointmentService);

        VisitSpecsDTO visitSpecsDtoM = mock(VisitSpecsDTO.class);
        BookedVisit bookedVisitM = mock(BookedVisit.class);
        SubjectMrn subjectMrnM = mock(SubjectMrn.class);
        Subject subjectM = mock(Subject.class);
        UserSession userSessionM = mock(UserSession.class);

        List<BookedResource> bookedResourceList = Lists.newArrayList();
        Date startDate = new Date();
        Date endDate = new Date();
        String ipAddress = "ipAddress";
        String institution = "institution";
        String templatePath = "templatePath";
        String genderBlockMessage = "genderBlockMessage";
    }

    @Test
    public void testDefaultAppointmentConfirmer_confirmVisitBooking() {
        ObjectsForTestConfirmer o = new ObjectsForTestConfirmer();

        DefaultAppointmentConfirmer defaultAppointmentConfirmer =
                new AppointmentService.DefaultAppointmentConfirmer();

        doReturn(o.bookedVisitM).when(o.serviceSpy)
                .createBookedVisit(refEq(o.visitSpecsDtoM), refEq(o.userSessionM),
                        refEq(o.startDate), refEq(o.endDate));

        when(o.bookedVisitM.getSubjectMrn()).thenReturn(o.subjectMrnM);
        when(o.subjectMrnM.getSubject()).thenReturn(o.subjectM);
        when(o.bookedVisitM.getBookedResourceList()).thenReturn(o.bookedResourceList);

        doReturn(o.genderBlockMessage).when(o.serviceSpy)
                .checkForGenderBlock(refEq(o.subjectM), refEq(o.bookedResourceList));

        defaultAppointmentConfirmer.confirmVisitBooking(
                o.serviceSpy, o.visitSpecsDtoM, o.userSessionM,
                o.ipAddress, o.institution, o.templatePath,
                o.startDate, o.endDate);


        verify(o.serviceSpy, times(1))
                .createBookedVisit(o.visitSpecsDtoM, o.userSessionM,
                        o.startDate, o.endDate);

        verify(o.bookedVisitM, times(1))
                .getSubjectMrn();

        verify(o.serviceSpy, times(1))
                .checkForGenderBlock(o.subjectM, o.bookedResourceList);

        verify(o.visitSpecsDtoM, times(1))
                .setDoubleRoomMessage(o.genderBlockMessage);


        //// case where genderBlockMessage is null -- check meals and persist
        doReturn(null).when(o.serviceSpy)
                .checkForGenderBlock(refEq(o.subjectM), refEq(o.bookedResourceList));

        doNothing().when(o.serviceSpy)
                .checkMealsAndPersistVisit(o.userSessionM, o.ipAddress, o.institution,
                        o.templatePath, o.bookedVisitM);

        defaultAppointmentConfirmer.confirmVisitBooking(
                o.serviceSpy, o.visitSpecsDtoM, o.userSessionM,
                o.ipAddress, o.institution, o.templatePath,
                o.startDate, o.endDate);

        verify(o.serviceSpy, times(1))
                .checkMealsAndPersistVisit(o.userSessionM, o.ipAddress,
                        o.institution, o.templatePath, o.bookedVisitM);

        //// case where bvSubjectMrn is null -- do not check for gender block
        when(o.bookedVisitM.getSubjectMrn()).thenReturn(null);

        defaultAppointmentConfirmer.confirmVisitBooking(
                o.serviceSpy, o.visitSpecsDtoM, o.userSessionM,
                o.ipAddress, o.institution, o.templatePath,
                o.startDate, o.endDate);

        // counts accumulate: 2 before latest invocation, now should be 3
        verify(o.bookedVisitM, times(3))
                .getSubjectMrn();

        // should not have been called this time. 2 before, still 2
        verify(o.serviceSpy, times(2))
                .checkMealsAndPersistVisit(o.userSessionM, o.ipAddress,
                        o.institution, o.templatePath, o.bookedVisitM);
    }

    @Test
    public void testDefaultAppointmentConfirmer_confirmVisitBookingAfterDoubleRoomMessage() {
        ObjectsForTestConfirmer o = new ObjectsForTestConfirmer();

        DefaultAppointmentConfirmer defaultAppointmentConfirmer =
                new AppointmentService.DefaultAppointmentConfirmer();

        doReturn(o.bookedVisitM).when(o.serviceSpy)
                .createBookedVisit(refEq(o.visitSpecsDtoM), refEq(o.userSessionM),
                        refEq(o.startDate), refEq(o.endDate));


        doNothing().when(o.serviceSpy)
                .checkMealsAndPersistVisit(o.userSessionM, o.ipAddress, o.institution,
                        o.templatePath, o.bookedVisitM);

        defaultAppointmentConfirmer.confirmVisitBookingAfterDoubleRoomMessage(
                o.serviceSpy, o.visitSpecsDtoM, o.userSessionM,
                o.ipAddress, o.institution, o.templatePath,
                o.startDate, o.endDate);


        verify(o.visitSpecsDtoM, times(1))
                .setDoubleRoomMessage(null);

        verify(o.serviceSpy, times(1))
                .createBookedVisit(o.visitSpecsDtoM, o.userSessionM,
                        o.startDate, o.endDate);

        verify(o.serviceSpy, times(1))
                .checkMealsAndPersistVisit(o.userSessionM, o.ipAddress,
                        o.institution, o.templatePath, o.bookedVisitM);
    }

    @Test
    public void testRescheduleDataFromTemplate() {

        AppointmentService serviceSpy = spy(appointmentService);

        int bookedVisitId = 1;
        int overrideReasonId = 2;
        String ipAddress = "some ip address";
        String templatePath = "some template path";
        int eventDateMinutesDelta = 0;

        Date visitTimeStartDate = new Date();
        VisitSpecsDTO visitSpecsDTO = new VisitSpecsDTO();
        visitSpecsDTO.setBookedvisit(bookedVisitId);
        visitSpecsDTO.setStartDate(visitTimeStartDate.getTime());
        visitSpecsDTO.setOverrideReason(overrideReasonId);

        User user = new User();

        VisitType visitType = new VisitType();
        visitType.setName(TestUtils.InpatientCRC);
        visitType.setInpatient(true);

        VisitTemplate visitTemplate = new VisitTemplate();
        visitTemplate.setDuration(1000);
        visitTemplate.setVisitType(visitType);
        visitTemplate.setRelativeTime(true);

        BookedVisit bookedVisit = new BookedVisit();
        bookedVisit.setVisitTemplate(visitTemplate);

        BookedVisit clonedVisit = new BookedVisit();
        clonedVisit.setVisitTemplate(visitTemplate);

        AppointmentOverrideReason overrideReason = new AppointmentOverrideReason();

        TemplateResource templateResourceLowest = new TemplateResource();
        Date templateResourceLowestStartDate = new Date();
        templateResourceLowest.setStartMinutes(minutesSinceOrigin(templateResourceLowestStartDate));

        final Calendar scheduledEndTimeCal = Calendar.getInstance();
        scheduledEndTimeCal.clear();
        scheduledEndTimeCal.setTime(visitTimeStartDate);
        scheduledEndTimeCal.add(Calendar.MINUTE, visitTemplate.getDuration());
        scheduledEndTimeCal.set(Calendar.SECOND, 0);
        scheduledEndTimeCal.set(Calendar.MILLISECOND, 0);

        List<TemplateResource> templateResourceList = new ArrayList<>();

        when(appointmentDAO.findBookedVisitById(bookedVisitId)).thenReturn(bookedVisit);
        when(appointmentDAO.findAppointmentOverrideReasonById(overrideReasonId)).thenReturn(overrideReason);
        when(mockTemplateResourceDAO.findTemplateResourceLowest(visitTemplate)).thenReturn(templateResourceLowest);
        doReturn(scheduledEndTimeCal).when(serviceSpy).updateDatesForBV(visitTemplate, visitTimeStartDate, templateResourceLowestStartDate);
        doReturn(clonedVisit).when(serviceSpy).createClonedBookedVisit(user, bookedVisit, visitTimeStartDate, scheduledEndTimeCal);
        when(mockTemplateResourceDAO.findTemplateResourcesByVisit(visitTemplate)).thenReturn(templateResourceList);
        doNothing().when(serviceSpy).setRoomsForBVFromTemplate(clonedVisit, templateResourceList);
        doNothing().when(serviceSpy).createBookedVisitComments(visitSpecsDTO, user, ipAddress, clonedVisit);
        doNothing().when(serviceSpy).setupBookedResourcesForBookedVisit(user, templatePath, templateResourceList, visitTimeStartDate, clonedVisit,
                eventDateMinutesDelta);
        doNothing().when(mockAuditService).logAppointmentActivity(ipAddress, clonedVisit, user, BookedVisitActivityLogStatics.RESCHEDULED, overrideReason);

        serviceSpy.rescheduleDataFromTemplate(visitSpecsDTO, user, ipAddress, templatePath);

        verify(mockAuditService, times(1)).logAppointmentActivity(ipAddress, clonedVisit, user, BookedVisitActivityLogStatics.RESCHEDULED, overrideReason);

    }

    @Test
    public void testRescheduleDataFromVisit() {

        // set constants
        final VisitType visitType = new VisitType();
        visitType.setName(TestUtils.InpatientCRC);
        visitType.setInpatient(true);
        final int bookedVisitId = 99;
        final int overrideReasonId = 88;
        final String ipAddress = "123.456.789.000";
        final Date startDate = new Date();
        final String visitTimeComment = "some comment";
        final String resourceName1 = "resource 1";
        final String resourceName2 = "resource 2";

        final Date latestBookedResourceTime = new Date(startDate.getTime() + 1000000);

        // Create entities

        final User user = new User();

        final VisitSpecsDTO visitSpecsDTO = new VisitSpecsDTO();
        visitSpecsDTO.setBookedvisit(bookedVisitId);
        visitSpecsDTO.setStartDate(startDate.getTime());
        visitSpecsDTO.setOverrideReason(overrideReasonId);
        visitSpecsDTO.setComment(visitTimeComment);

        VisitTemplate visitTemplate = new VisitTemplate();
        visitTemplate.setRelativeTime(false);
        visitTemplate.setVisitType(visitType);

        BookedVisit bookedVisit = new BookedVisit();
        bookedVisit.setVisitTemplate(visitTemplate);
        bookedVisit.setVisitType(visitType);
        bookedVisit.setScheduledStartTime(startDate);

        final AppointmentOverrideReason overrideReason = new AppointmentOverrideReason();

        final Resource resourceLowest = new Resource();
        resourceLowest.setResourceType(ResourceType.Lab);
        resourceLowest.setName("lowest resource");
        final Resource resource1 = new Resource();
        resource1.setResourceType(ResourceType.Nursing);
        resource1.setName(resourceName1);
        final Resource resource2 = new Resource();
        resource2.setResourceType(ResourceType.Room);
        resource2.setName(resourceName2);

        final BookedResource bookedResourceLowest = new BookedResource();
        bookedResourceLowest.setResource(resourceLowest);
        bookedResourceLowest.setScheduledStartTime(startDate);
        bookedResourceLowest.setDuration(1000);
        final BookedResource bookedResource1 = new BookedResource();
        bookedResource1.setResource(resource1);
        bookedResource1.setScheduledStartTime(new Date(startDate.getTime() + 2000000));
        bookedResource1.setDuration(2000);
        final BookedResource bookedResource2 = new BookedResource();
        bookedResource2.setResource(resource2);
        bookedResource2.setScheduledStartTime(new Date(startDate.getTime() + 3000000));
        bookedResource2.setDuration(3000);

        final List<BookedResource> bookedResourceList = new ArrayList<>();
        bookedResourceList.add(bookedResource1);
        bookedResourceList.add(bookedResource2);

        // mocked methods, captured arguments

        when(appointmentDAO.findBookedVisitById(bookedVisitId)).thenReturn(bookedVisit);
        when(appointmentDAO.findAppointmentOverrideReasonById(overrideReasonId)).thenReturn(overrideReason);
        when(studyDAO.findBookedResourceLowest(bookedVisit)).thenReturn(bookedResourceLowest);
        when(appointmentDAO.findBookedResourcesByBookedVisit(bookedVisit)).thenReturn(bookedResourceList);
        when(appointmentDAO.findLatestBookedResourcesByBookedVisit(bookedVisit)).thenReturn(latestBookedResourceTime);
        doNothing().when(appointmentDAO).createEntity(any());
        doNothing().when(mockAuditService).logAppointmentActivity(
                refEq(ipAddress),
                isA(BookedVisit.class),
                refEq(user),
                refEq(BookedVisitActivityLogStatics.RESCHEDULED),
                refEq(overrideReason)
        );
        ArgumentCaptor<Comments> commentsArgumentCaptor = ArgumentCaptor.forClass(Comments.class);

        // Do the deed

        BookedVisit clonedVisit = appointmentService.rescheduleDataFromVisit(visitSpecsDTO, user, ipAddress);

        // verify internal method calls

        verify(appointmentDAO, times(1)).findBookedVisitById(bookedVisitId);
        verify(appointmentDAO, times(1)).findAppointmentOverrideReasonById(overrideReasonId);
        verify(studyDAO, times(1)).findBookedResourceLowest(bookedVisit);
        verify(appointmentDAO, times(1)).findBookedResourcesByBookedVisit(bookedVisit);
        verify(appointmentDAO, times(1)).findLatestBookedResourcesByBookedVisit(bookedVisit);
        verify(appointmentDAO, times(4)).createEntity(any());
        verify(mockAuditService, times(1)).logAppointmentActivity(
                refEq(ipAddress),
                isA(BookedVisit.class),
                refEq(user),
                refEq(BookedVisitActivityLogStatics.RESCHEDULED),
                refEq(overrideReason)
        );

        // verify side-effects

        verify(appointmentDAO, times(4)).createEntity(commentsArgumentCaptor.capture());
        Comments capturedComment = commentsArgumentCaptor.getAllValues().get(1);
        assert(capturedComment.getClass() == Comments.class);
        assertEquals(visitTimeComment, capturedComment.getComment());
        assertEquals(bookedVisit, capturedComment.getBookedVisit());
        assertEquals(user, capturedComment.getUser());
        assert(capturedComment.getDate().getTime() >= startDate.getTime());

        // verify returned value

        Date eventDate = new Date(startDate.getTime());
        eventDate.setHours(startDate.getHours());
        eventDate.setMinutes(startDate.getMinutes());
        eventDate.setSeconds(0);
        assertEquals(eventDate, clonedVisit.getScheduledStartTime());
        assertNull(bookedVisit.getRooms());
        assertEquals(resourceName2 + ", ", clonedVisit.getRooms());

    }

    // already covered by testRescheduleDataFromVisit()
    @Test
    public void testCreateBookedVisitComments() {

    }

    @Test
    public void testPersistVisit() {

        // constants

        final String ipAddress = "111.222.333.444";
        final Date testStartDate = new Date();

        // entities

        User user = new User();

        UserSession userSession = new UserSession();
        userSession.setUser(user);

        BookedVisit bookedVisit = new BookedVisit();

        // mocked methods

        doNothing().when(appointmentDAO).createEntity(any());
        doNothing().when(mockAuditService).logAppointmentActivity(
                refEq(ipAddress),
                isA(BookedVisit.class),
                refEq(user),
                refEq(BookedVisitActivityLogStatics.RESCHEDULED)
        );

        // payload

        appointmentService.persistVisit(bookedVisit, userSession, ipAddress);

        // verify internal method calls

        verify(appointmentDAO, times(1)).createEntity(bookedVisit);
        verify(mockAuditService, times(1)).logAppointmentActivity(
                refEq(ipAddress),
                isA(BookedVisit.class),
                refEq(user),
                refEq(BookedVisitActivityLogStatics.SCHEDULED)
        );

        // verify side effects

        assert(bookedVisit.getSchedulingTime().getTime() >= testStartDate.getTime());
        assertEquals(user, bookedVisit.getSchedulingUser());
        assertEquals(BookedVisitActivityLogStatics.SCHEDULED.getLogString(), bookedVisit.getSchedulingFlavor());

        // no return value to check (method returns void)

    }

    @Test
    public void testCreateCommentsRecordIfNonemptyComment() {

        // constants

        final String ipAddress = "111.222.333.444";
        final Date testStartDate = new Date();
        final String commentString = "some comment";

        // entities

        User user = new User();

        BookedVisit bookedVisit = new BookedVisit();
        bookedVisit.setComment(commentString);

        // mocked methods

        doNothing().when(appointmentDAO).createEntity(any());
        doNothing().when(mockAuditService).logAppointmentActivity(
                refEq(ipAddress),
                refEq(bookedVisit),
                refEq(user),
                refEq(BookedVisitActivityLogStatics.COMMENTED)
        );

        // paydirt

        Comments comment = appointmentService.createCommentsRecordIfNonemptyComment(bookedVisit, user, ipAddress);

        // verify internal method calls

        verify(appointmentDAO, times(1)).createEntity(comment);
        verify(mockAuditService, times(1)).logAppointmentActivity(
                refEq(ipAddress),
                refEq(bookedVisit),
                refEq(user),
                refEq(BookedVisitActivityLogStatics.COMMENTED)
        );

        // verify return value

        assertEquals(commentString, comment.getComment());
        assertEquals(bookedVisit, comment.getBookedVisit());
        assertEquals(user, comment.getUser());
        assert(comment.getDate().getTime() >= testStartDate.getTime());

    }

    @Test
    public void testCheckOutVisit() {

        // constants

        final String ipAddress = "111.222.333.444";
        final Date testStartDate = new Date();
        final String commentString = "some comment";
        final int bookedVisitId = 87;
        final Date checkOutDate = new Date();
        final int appointmentStatusReasonId = 22;
        final String checkedOutStatusName = "CHECKED out";
        final String resourceName1 = "resource A";
        final String resourceName2 = "resource B";

        // entities

        User user = new User();

        VisitSpecsDTO visitSpecsDTO = new VisitSpecsDTO();
        visitSpecsDTO.setId(bookedVisitId);
        visitSpecsDTO.setCheckOutDate(checkOutDate.getTime());
        visitSpecsDTO.setOmmittedActivities(true);
        visitSpecsDTO.setVaryDuration(true);
        visitSpecsDTO.setAppointmentStatusReason(appointmentStatusReasonId);

        BookedVisit bookedVisit = new BookedVisit();
        bookedVisit.setId(bookedVisitId);

        AppointmentStatusReason appointmentStatusReason = new AppointmentStatusReason();
        appointmentStatusReason.setId(appointmentStatusReasonId);

        AppointmentStatus checkedOutAppointmentStatus = new AppointmentStatus();
        checkedOutAppointmentStatus.setName(checkedOutStatusName);

        final Resource resourceLowest = new Resource();
        resourceLowest.setResourceType(ResourceType.Lab);
        resourceLowest.setName("lowest resource");
        final Resource resource1 = new Resource();
        resource1.setResourceType(ResourceType.Room);
        resource1.setName(resourceName1);
        final Resource resource2 = new Resource();
        resource2.setResourceType(ResourceType.Room);
        resource2.setName(resourceName2);

        final BookedResource bookedResourceLowest = new BookedResource();
        bookedResourceLowest.setResource(resourceLowest);
        final BookedResource bookedResource1 = new BookedResource();
        bookedResource1.setResource(resource1);
        final BookedResource bookedResource2 = new BookedResource();
        bookedResource2.setResource(resource2);

        final List<BookedResource> bookedResourceList = new ArrayList<>();
        bookedResourceList.add(bookedResource1);
        bookedResourceList.add(bookedResource2);

        // mocked methods

        when(appointmentDAO.findBookedVisitById(bookedVisitId)).thenReturn(bookedVisit);
        when(appointmentDAO.findAppointmentStatusReasonById(appointmentStatusReasonId))
                .thenReturn(appointmentStatusReason);
        when(appointmentService.checkedOutStatus()).thenReturn(checkedOutAppointmentStatus);
        when(appointmentDAO.findRoomBookedResourcesByBookedVisit(bookedVisitId))
                .thenReturn(bookedResourceList);
        doNothing().when(appointmentDAO).updateEntity(bookedVisit);
        doNothing().when(mockAuditService).logAppointmentActivity(
                refEq(ipAddress),
                refEq(bookedVisit),
                refEq(user),
                refEq(BookedVisitActivityLogStatics.CHECKED_OUT)
        );

        // payload -- TEST CASE 1 (check out date set in visitSpecsDTO)

        BookedVisit returnedBookedVisit  = appointmentService.checkOutVisit(visitSpecsDTO, user, ipAddress);

        // verify internal method calls

        verify(appointmentDAO, times(1)).updateEntity(bookedVisit);
        verify(mockAuditService, times(1)).logAppointmentActivity(
                refEq(ipAddress),
                refEq(bookedVisit),
                refEq(user),
                refEq(BookedVisitActivityLogStatics.CHECKED_OUT)
        );

        // verify return value

        assertEquals(bookedVisit, returnedBookedVisit);
        assertEquals(checkOutDate, returnedBookedVisit.getCheckOutDate());
        assertEquals(user, returnedBookedVisit.getCheckOutUser());
        assertTrue(returnedBookedVisit.isOmmittedActivities());
        assertTrue(returnedBookedVisit.isVaryDuration());
        assertEquals(appointmentStatusReason, returnedBookedVisit.getAppointmentStatusReason());
        assertEquals(checkedOutAppointmentStatus, returnedBookedVisit.getAppointmentStatus());
        assertEquals(appointmentStatusReason, returnedBookedVisit.getCheckoutStatusReason());
        String expectedRoomsString = " " + resourceName1 + ", " + resourceName2 + ", ";
        assertEquals(expectedRoomsString, returnedBookedVisit.getRooms());

        // TEST CASE 2 (check out date NOT set in visitSpecsDTO)

        visitSpecsDTO.setCheckOutDate(0);

        returnedBookedVisit  = appointmentService.checkOutVisit(visitSpecsDTO, user, ipAddress);

        assert(returnedBookedVisit.getCheckOutDate().getTime() >= testStartDate.getTime());


    }

    @Test
    public void testCheckInVisit() {

        // constants

        final String ipAddress = "111.222.333.444";
        final Date testStartDate = new Date();
        final int bookedVisitId = 87;
        final Date checkInDate = new Date();
        final String checkedInStatusName = "CHECKED in";

        // entities

        User user = new User();

        AppointmentStatus checkedInAppointmentStatus = new AppointmentStatus();
        checkedInAppointmentStatus.setName(checkedInStatusName);

        VisitSpecsDTO visitSpecsDTO = new VisitSpecsDTO();
        visitSpecsDTO.setId(bookedVisitId);
        visitSpecsDTO.setCheckInDate(checkInDate.getTime());

        BookedVisit bookedVisit = new BookedVisit();
        bookedVisit.setId(bookedVisitId);

        // mocked methods

        // need a spy so we can mock and verify some calls to methods of AppointmentService
        // without making the whole service a mock
        AppointmentService appointmentServiceSpy = spy(appointmentService);
        when(appointmentServiceSpy.checkedInStatus()).thenReturn(checkedInAppointmentStatus);
        when(appointmentDAO.findBookedVisitById(bookedVisitId)).thenReturn(bookedVisit);
        doNothing().when(appointmentDAO).updateEntity(bookedVisit);
        doNothing().when(mockAuditService).logAppointmentActivity(
                refEq(ipAddress),
                refEq(bookedVisit),
                refEq(user),
                refEq(BookedVisitActivityLogStatics.CHECKED_OUT)
        );

        // method under test

        BookedVisit returnedBookedVisit  = appointmentService.checkInVisit(visitSpecsDTO, user, ipAddress);

        // verify internal method calls

        verify(appointmentServiceSpy, times(1)).checkedInStatus();
        verify(appointmentDAO, times(1)).findBookedVisitById(bookedVisitId);
        verify(appointmentDAO, times(1)).updateEntity(bookedVisit);
        verify(mockAuditService, times(1)).logAppointmentActivity(
                refEq(ipAddress),
                refEq(bookedVisit),
                refEq(user),
                refEq(BookedVisitActivityLogStatics.CHECKED_IN)
        );

        // verify return value

        assertEquals(bookedVisit, returnedBookedVisit);
        assertEquals(checkInDate, returnedBookedVisit.getCheckInDate());
        assertEquals(user, returnedBookedVisit.getCheckInUser());
        assertEquals(checkedInAppointmentStatus, returnedBookedVisit.getAppointmentStatus());

        // TEST CASE 2 (check in date NOT set in visitSpecsDTO)

        visitSpecsDTO.setCheckInDate(0);

        returnedBookedVisit  = appointmentService.checkOutVisit(visitSpecsDTO, user, ipAddress);

        assert(returnedBookedVisit.getCheckInDate().getTime() >= testStartDate.getTime());

    }

    @Test
    public void testBatchEntryUpdate() {

        // constants

        final String ipAddress = "111.222.333.444";
        final Date testStartDate = new Date();
        final int bookedVisitId = 87;
        final Date checkInDate = new Date(testStartDate.getTime() + 1000);
        final Date checkOutDate = new Date(testStartDate.getTime() + 2000);
        final Date cancelDate = new Date(testStartDate.getTime() + 3000);
        final Date scheduledStartTime = new Date(testStartDate.getTime() + 4000);
        final String templatePath = "some template path";
        final String checkedInStatusName = "CHECKED in";
        final String checkedOutStatusName = "CHECKED out";
        final int checkOutStatusReasonId = 33;
        final int cancelStatusReasonId = 44;
        final int cancelStatusId = 6; // 6 is a magic number which triggers an email (late cancellation). not sure about 7
        final String institutionLongName = "Institution long name";

        // entities

        Institution institution = new Institution();
        institution.setLongName(institutionLongName);

        User user = new User();
        user.setInstitution(institution);

        AppointmentStatus checkedInAppointmentStatus = new AppointmentStatus();
        checkedInAppointmentStatus.setName(checkedInStatusName);

        AppointmentStatus checkedOutAppointmentStatus = new AppointmentStatus();
        checkedOutAppointmentStatus.setName(checkedOutStatusName);

        AppointmentStatusReason checkOutStatusReason = new AppointmentStatusReason();
        checkOutStatusReason.setId(checkOutStatusReasonId);

        AppointmentStatusReason cancelStatusReason = new AppointmentStatusReason();
        cancelStatusReason.setId(cancelStatusReasonId);

        AppointmentStatus checkedOutStatus = new AppointmentStatus();

        CancellationStatus cancelStatus = new CancellationStatus();
        cancelStatus.setId(cancelStatusId);

        VisitSpecsDTO visitSpecsDTO = new VisitSpecsDTO();
        visitSpecsDTO.setId(bookedVisitId);
        // Is it possible in real life to have both a check-in and a check-out dates non-0?
        visitSpecsDTO.setCheckOutDate(checkOutDate.getTime());
        visitSpecsDTO.setCheckInDate(checkInDate.getTime());
        visitSpecsDTO.setCheckoutStatusReason(checkOutStatusReasonId);
        visitSpecsDTO.setCancelStatusReason(cancelStatusReasonId);
        visitSpecsDTO.setCancelDate(cancelDate.getTime());

        BookedVisit bookedVisit = new BookedVisit();
        bookedVisit.setId(bookedVisitId);
        bookedVisit.setCheckInDate(checkInDate);
        bookedVisit.setScheduledStartTime(scheduledStartTime);

        // mocked methods

        AppointmentService appointmentServiceSpy = spy(appointmentService);
        when(appointmentDAO.findBookedVisitById(bookedVisitId)).thenReturn(bookedVisit);
        when(appointmentDAO.findAppointmentStatusReasonById(checkOutStatusReasonId)).thenReturn(checkOutStatusReason);
        when(appointmentDAO.findAppointmentStatusReasonById(cancelStatusReasonId)).thenReturn(cancelStatusReason);
        when(appointmentDAO.findCheckedOutStatus()).thenReturn(checkedOutStatus);
        when(appointmentDAO.findCancellationStatusById(cancelStatusId)).thenReturn(cancelStatus);
        doNothing().when(appointmentServiceSpy).sendLateCancellationEmail(bookedVisit, institutionLongName, templatePath);
        doNothing().when(appointmentDAO).updateEntity(bookedVisit);
        doNothing().when(mockAuditService).logAppointmentActivity(
                refEq(ipAddress),
                refEq(bookedVisit),
                refEq(user),
                refEq(BookedVisitActivityLogStatics.CHECKED_OUT)
        );

        // method under test

        String errorMsg  = appointmentServiceSpy.batchEntryUpdate(visitSpecsDTO, user, ipAddress, templatePath);

        // verify internal method calls

        verify(appointmentDAO, times(1)).findBookedVisitById(bookedVisitId);
        verify(appointmentDAO, times(1)).findAppointmentStatusReasonById(checkOutStatusReasonId);
        verify(appointmentServiceSpy, times(1)).checkedOutStatus();
        verify(appointmentDAO, times(1)).findCancellationStatusById(cancelStatusId);
        verify(appointmentServiceSpy, times(1)).sendLateCancellationEmail(bookedVisit, institutionLongName, templatePath);
        verify(appointmentDAO, times(1)).updateEntity(bookedVisit);
        verify(mockAuditService, times(1)).logAppointmentActivity(
                refEq(ipAddress),
                refEq(bookedVisit),
                refEq(user),
                refEq(BookedVisitActivityLogStatics.BATCH_ENTRY)
        );

        // verify return value

        assertEquals("true", errorMsg);
    }

    @Test
    public void testCancelVisit() {

    }

    // tests AppointmentService.getVisitDetails
    // There already is a test named testGetVisitDetails,
    // for another method getVisitDetails() with a different signature
    // so this one is named with a suffix '2'
    @Test
    public void testGetVisitDetails2() {

    }

    @Test
    public void testGetOnlyTodaysBookedVisitsHomeList()
    {
        final Date startDate = DateUtility.toDate(LocalDate.of(2017, 3, 27));
        final Date endDate = DateUtility.adjustDateToEndOfDay(startDate);
        final String sortBy = "bv.scheduledStartTime";
        final String orderBy= "ASC";
        final int page = 1;
        final int maxResults = 25;
        final int userId = 230;
        final String remoteHost = "remote host";

        final List<BookedVisitsResponse> bookedVisitsResponses = new ArrayList<>();
        final BookedVisitsResponse bookedVisitResponse1 = new BookedVisitsResponse(
                145, "Visit 1" , "IRB4500", "CAT9000",
                "LOCAL5890",
                "Foo",
                "Last",
                "2309", "Scheduled",
                DateUtility.toDate(LocalDateTime.of(2017, 3, 27, 10, 10)),
                DateUtility.toDate(LocalDateTime.of(2017, 3, 27, 12, 20)), (long)2, 0);
        final BookedVisitsResponse bookedVisitResponse2= new BookedVisitsResponse(
                230, "Visit 2" , "IRB9800",
                "CAT5000", "LOCAL9875",
                "Bar",
                "Lastfoo",
                "2975", "Checked-Out", DateUtility.toDate(LocalDateTime.of(2017, 3, 27, 11, 30)),
                DateUtility.toDate(LocalDateTime.of(2017, 3, 27, 12, 45)), (long)2, 0);

        bookedVisitsResponses.add(bookedVisitResponse1);
        bookedVisitsResponses.add(bookedVisitResponse2);

        final User user = mock(User.class);
        List<Study> studyList = new ArrayList<>();
        when(mockAuthDAO.findUserById(userId)).thenReturn(user);
        when(user.isStudyStaff()).thenReturn(true);
        when(studyDAO.findStudyListByPerson(any(User.class))).thenReturn(studyList);
        when(appointmentDAO.getOnlyTodaysBookedVisitsByStudy(eq(studyList), eq(startDate), eq(endDate), eq(sortBy),eq(orderBy),
                                                             eq(page), eq(maxResults))).thenReturn(bookedVisitsResponses);

        List<BookedVisitsResponse> responseList = appointmentService.getOnlyTodaysBookedVisitsHomeList(startDate, endDate, sortBy, orderBy, page,
                                                                                            maxResults, userId, remoteHost);

        assertEquals(responseList.size(), 2);
        assertEquals(responseList.get(0).getSubjectFirstName(), bookedVisitResponse1.getSubjectFirstName());
        assertEquals(responseList.get(0).getSubjectLastName(), bookedVisitResponse1.getSubjectLastName());
        assertEquals(responseList.get(0).getSubjectMRN(), bookedVisitResponse1.getSubjectMRN());
        assertEquals(responseList.get(0).getVisitName(), bookedVisitResponse1.getVisitName());
        assertEquals(responseList.get(0).getId(), bookedVisitResponse1.getId());
        assertEquals(responseList.get(0).getIrb(), bookedVisitResponse1.getIrb());
        assertEquals(responseList.get(0).getLocalId(), bookedVisitResponse1.getLocalId());
        assertEquals(responseList.get(0).getCatId(), bookedVisitResponse1.getCatId());
        assertEquals(responseList.get(0).getAppointmentStatus(), bookedVisitResponse1.getAppointmentStatus());
        assertEquals(responseList.get(0).getScheduledStartTime(), bookedVisitResponse1.getScheduledStartTime());
        assertEquals(responseList.get(0).getScheduledEndTime(), bookedVisitResponse1.getScheduledEndTime());
        Mockito.verify(mockAuditService).logViewActivity(remoteHost, user, "HOME SCREEN - Appointment List View.");

        when(mockAuthDAO.findUserById(userId)).thenReturn(user);
        when(user.isStudyStaff()).thenReturn(false);
        when(appointmentDAO.getOnlyTodaysBookedVisits(eq(startDate), eq(endDate), eq(sortBy),eq(orderBy),
                                                      eq(page), eq(maxResults))).thenReturn(bookedVisitsResponses);

        responseList = appointmentService.getOnlyTodaysBookedVisitsHomeList(startDate, endDate, sortBy, orderBy, page,
                                                                 maxResults, userId, remoteHost);
        assertEquals(responseList.size(), 2);
        assertEquals(responseList.get(0).getSubjectFirstName(), bookedVisitResponse1.getSubjectFirstName());
        assertEquals(responseList.get(0).getSubjectLastName(), bookedVisitResponse1.getSubjectLastName());
        assertEquals(responseList.get(0).getSubjectMRN(), bookedVisitResponse1.getSubjectMRN());
        assertEquals(responseList.get(0).getVisitName(), bookedVisitResponse1.getVisitName());
        assertEquals(responseList.get(0).getId(), bookedVisitResponse1.getId());
        assertEquals(responseList.get(0).getIrb(), bookedVisitResponse1.getIrb());
        assertEquals(responseList.get(0).getLocalId(), bookedVisitResponse1.getLocalId());
        assertEquals(responseList.get(0).getCatId(), bookedVisitResponse1.getCatId());
        assertEquals(responseList.get(0).getAppointmentStatus(), bookedVisitResponse1.getAppointmentStatus());
        assertEquals(responseList.get(0).getScheduledStartTime(), bookedVisitResponse1.getScheduledStartTime());
        assertEquals(responseList.get(0).getScheduledEndTime(), bookedVisitResponse1.getScheduledEndTime());
        Mockito.verify(mockAuditService, Mockito.times(2)).logViewActivity(remoteHost, user, "HOME SCREEN - Appointment List View.");
    }

    @Test
    public void testConfirmOverbookRoomData() {

        AppointmentService appointmentServiceSpy = spy(appointmentService);

        String studyName = "my study";

        Study study = new Study();
        study.setName(studyName);

        int mrnId = 1;
        int visitId = 2;
        long startTime = (new Date()).getTime();
        int overrideReasonId = 3;
        String commentString = "some comment";

        AppointmentOverrideReason appointmentOverrideReason = new AppointmentOverrideReason();
        appointmentOverrideReason.setId(overrideReasonId);

        VisitSpecsDTO dto = new VisitSpecsDTO();
        dto.setStartDate(startTime);
        dto.setSubjectMrnId(mrnId);
        dto.setVisit(visitId);
        dto.setOverrideReason(overrideReasonId);
        dto.setComment(commentString);

        String institutionName = "institution name";

        Institution institution = new Institution();
        institution.setLongName(institutionName);

        User user = new User();
        user.setInstitution(institution);

        String ipAddress = "1.2.3.4";
        String templatePath = "template path";

        SubjectMrn subjectMrn = new SubjectMrn();
        subjectMrn.setId(mrnId);

        VisitType inpatientVisitType = new VisitType();
        inpatientVisitType.setInpatient(true);

        VisitTemplate visitTemplate = new VisitTemplate();
        visitTemplate.setId(visitId);
        visitTemplate.setVisitType(inpatientVisitType);
        visitTemplate.setRelativeTime(false);
        visitTemplate.setStudy(study);

        AppointmentStatus scheduledStatus = new AppointmentStatus();
        scheduledStatus.setName("Scheduled");

        Resource resource1 = new Resource();
        resource1.setResourceType(ResourceType.Room);

        Resource resource2 = new Resource();
        resource2.setResourceType(ResourceType.Nutrition);

        Resource resource3 = new Resource();
        resource3.setResourceType(ResourceType.Lab);

        Resource resource4 = new Resource();
        resource4.setResourceType(ResourceType.Nursing);

        List<Resource> resources = Arrays.asList(resource1, resource2);

        int templateResourceStartMinutes = 12;
        int templateResourceStartHours = 3;

        TemplateResource tr1 = new TemplateResource();
        tr1.setResource(resource1);

        TemplateResource tr2 = new TemplateResource();
        tr2.setResource(resource2);

        TemplateResource tr3 = new TemplateResource();
        tr3.setResource(resource3);

        TemplateResource templateResourceLowest = new TemplateResource();
        templateResourceLowest.setId(99);
        templateResourceLowest.setStartMinutes(templateResourceStartHours * 60 + templateResourceStartMinutes);
        templateResourceLowest.setResource(resource4);

        List<TemplateResource> trs = Arrays.asList(templateResourceLowest, tr1, tr2, tr3);

        // mock methods

        when(subjectDAO.findSubjectMrnById(mrnId)).thenReturn(subjectMrn);
        when(studyDAO.findVisitTemplateById(visitId)).thenReturn(visitTemplate);
        when(mockTemplateResourceDAO.findTemplateResourceLowest(visitTemplate)).thenReturn(templateResourceLowest);
        when(appointmentDAO.findAppointmentOverrideReasonById(overrideReasonId)).thenReturn(appointmentOverrideReason);
        when(appointmentServiceSpy.scheduledStatus()).thenReturn(scheduledStatus);
        doNothing().when(appointmentDAO).createEntity(any(BookedVisit.class));
        doNothing().when(mockAuditService).logAppointmentActivity(anyString(), any(BookedVisit.class), any(User.class), eq(BookedVisitActivityLogStatics.COMMENTED));
        when(mockTemplateResourceDAO.findTemplateResourcesByVisit(visitTemplate)).thenReturn(trs);
        doReturn(resources).when(appointmentServiceSpy).setupBookedResourcesForAppointmentWithVisitTime(
                anyObject(),
                anyObject(),
                anyObject(),
                anyObject(),
                anyObject(),
                anyObject(),
                anyInt()
        );
        doNothing().when(appointmentDAO).createEntity(any(BookedVisitActivityLog.class));
        doNothing().when(appointmentDAO).updateEntity(any(BookedVisit.class));
        doNothing().when(appointmentServiceSpy).sendOverBookedVisitEmail(
                any(BookedVisit.class), eq(institutionName), eq(templatePath), any(List.class));

        // method under test

        BookedVisit returnedBookedVisit = appointmentServiceSpy.confirmOverbookRoomData(dto, user, ipAddress, templatePath);

        assertEquals(scheduledStatus, returnedBookedVisit.getAppointmentStatus());
        assertNull(returnedBookedVisit.getAppointmentStatusReason());
        assertNull(returnedBookedVisit.getBookedResourceList());
        assertNull(returnedBookedVisit.getCancelDate());
        assertNull(returnedBookedVisit.getCancelUser());
        assertNull(returnedBookedVisit.getCancelStatus());
        assertNull(returnedBookedVisit.getCancelStatusReason());
        assertNull(returnedBookedVisit.getCheckInDate());
        assertNull(returnedBookedVisit.getCheckInUser());
        assertNull(returnedBookedVisit.getCheckOutDate());
        assertNull(returnedBookedVisit.getCheckOutUser());
        assertNull(returnedBookedVisit.getCheckoutStatusReason());
        assertEquals(visitTemplate.getName(), returnedBookedVisit.getName());
        Date expectedStartDate = new Date(startTime);
        expectedStartDate.setHours(templateResourceStartHours);
        expectedStartDate.setMinutes(templateResourceStartMinutes);
        expectedStartDate.setSeconds(0);
        assertEquals(expectedStartDate, returnedBookedVisit.getScheduledEndTime());
        assertEquals(expectedStartDate, returnedBookedVisit.getScheduledStartTime());
        assertEquals(study, returnedBookedVisit.getStudy());
        assertEquals(subjectMrn, returnedBookedVisit.getSubjectMrn());
        assertEquals(visitTemplate, returnedBookedVisit.getVisitTemplate());
        assertEquals(inpatientVisitType, returnedBookedVisit.getVisitType());
        assertEquals(commentString, returnedBookedVisit.getComment());

        verify(subjectDAO, times(1)).findSubjectMrnById(mrnId);
        verify(studyDAO, times(1)).findVisitTemplateById(visitId);
        verify(mockTemplateResourceDAO, times(1)).findTemplateResourceLowest(visitTemplate);
        verify(appointmentDAO, times(1)).findAppointmentOverrideReasonById(overrideReasonId);
        verify(appointmentServiceSpy, times(2)).scheduledStatus();

        // set-up verification of entities created:
        ArgumentCaptor<BaseEntity> plainCaptor = new ArgumentCaptor<>();
        verify(appointmentDAO, times(3)).createEntity(plainCaptor.capture());

        // check created booked visit
        BookedVisit capturedVisit = (BookedVisit) plainCaptor.getAllValues().get(0);
        assertEquals(returnedBookedVisit, capturedVisit);

        // check created comments
        Comments capturedComments = (Comments) plainCaptor.getAllValues().get(1);
        assertEquals(commentString, capturedComments.getComment());
        assertEquals(returnedBookedVisit, capturedComments.getBookedVisit());
        assertEquals(user, capturedComments.getUser());
        assertNotNull(capturedComments.getDate());

        // check created booked visit activity log
        BookedVisitActivityLog capturedBval = (BookedVisitActivityLog) plainCaptor.getAllValues().get(2);
        assertEquals(BookedVisitActivityLogStatics.OVERBOOKED.getLogString(), capturedBval.getActionPerformed());
        assertEquals(returnedBookedVisit, capturedBval.getBookedVisit());
        assertEquals(user, capturedBval.getPerformingUser());
        assertNotNull(capturedBval.getDate());
        assertEquals(ipAddress, capturedBval.getIpAddress());
        assertEquals(appointmentOverrideReason, capturedBval.getAppointmentOverrideReason());

        ArgumentCaptor<BookedVisit> bookedVisitCaptor = new ArgumentCaptor<>();
        verify(mockAuditService, times(1)).logAppointmentActivity(eq(ipAddress), bookedVisitCaptor.capture(), eq(user), eq(BookedVisitActivityLogStatics.COMMENTED));
        BookedVisit capturedVisit1 = bookedVisitCaptor.getAllValues().get(0);
        assertEquals(returnedBookedVisit, capturedVisit1);

        verify(mockTemplateResourceDAO, times(2)).findTemplateResourcesByVisit(visitTemplate);

        // capture anything below?
        verify(appointmentServiceSpy).setupBookedResourcesForAppointmentWithVisitTime(
                anyObject(),
                anyObject(),
                anyObject(),
                anyObject(),
                anyObject(),
                anyObject(),
                anyInt()
        );

        verify(appointmentDAO, times(1)).updateEntity(bookedVisitCaptor.capture());
        BookedVisit capturedVisit2 = bookedVisitCaptor.getAllValues().get(1);
        assertEquals(returnedBookedVisit, capturedVisit2);

        ArgumentCaptor<List<String>> stringListCaptor = new ArgumentCaptor<>();
        verify(appointmentServiceSpy, times(1)).sendOverBookedVisitEmail(
                bookedVisitCaptor.capture(), eq(institutionName), eq(templatePath), stringListCaptor.capture());
        BookedVisit capturedVisit3 = bookedVisitCaptor.getAllValues().get(2);
        assertEquals(returnedBookedVisit, capturedVisit3);
        List<String> capturedConditions = stringListCaptor.getAllValues().get(0);
        assertEquals(3, capturedConditions.size());
        assertEquals("This visit contains Nursing resources. ", capturedConditions.get(0));
        assertEquals("This visit contains Nutrition resources. ", capturedConditions.get(1));
        assertEquals("This visit contains Lab resources. ", capturedConditions.get(2));

    }

}

