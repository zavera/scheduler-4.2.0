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
import edu.harvard.catalyst.hccrc.core.util.TestHelpers;
import edu.harvard.catalyst.scheduler.core.BookedVisitActivityLogStatics;
import edu.harvard.catalyst.scheduler.core.Statics;
import edu.harvard.catalyst.scheduler.dto.ScheduledVisitHistoryDTO;
import edu.harvard.catalyst.scheduler.dto.SearchDTO;
import edu.harvard.catalyst.scheduler.dto.VisitSpecsDTO;
import edu.harvard.catalyst.scheduler.dto.response.*;
import edu.harvard.catalyst.scheduler.dto.statics.CalendarFilter;
import edu.harvard.catalyst.scheduler.entity.*;
import edu.harvard.catalyst.scheduler.security.SchedulerSession;
import edu.harvard.catalyst.scheduler.security.SchedulerUserDetails;
import edu.harvard.catalyst.scheduler.service.AppointmentService;
import edu.harvard.catalyst.scheduler.service.AuditService;
import edu.harvard.catalyst.scheduler.util.DateUtility;
import edu.harvard.catalyst.scheduler.util.TestUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

import static edu.harvard.catalyst.scheduler.util.DateUtility.date24HTime;
import static edu.harvard.catalyst.scheduler.util.DateUtility.minutesSinceOrigin;
import static java.util.Collections.emptyList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

/**
 *
 * @author Ankit Panchamia
 * @date Jan 07, 2014
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class AppointmentResourceTest implements TestHelpers {

    AppointmentResource appointmentResource;
    AppointmentResource spyAppointmentResource;

    private final AppointmentService mockAppointmentService = mock(AppointmentService.class);
    private final AuditService mockAuditService = mock(AuditService.class);

    private final SchedulerSession session = mock(SchedulerSession.class);
    private final HttpServletRequest request = mock(HttpServletRequest.class);
    private final SchedulerUserDetails schedulerUserDetails = mock(SchedulerUserDetails.class);
    private final User user = mock(User.class);
    private final String host = "host";

    @Before
    public void setUp() {
        appointmentResource = new AppointmentResource(mockAppointmentService);
        spyAppointmentResource = spy(appointmentResource);

        appointmentResource.setSession(session);
        appointmentResource.setRequest(request);

        spyAppointmentResource.setSession(session);
        spyAppointmentResource.setRequest(request);

        when(session.getUserDetails()).thenReturn(schedulerUserDetails);
        when(request.getRemoteHost()).thenReturn(host);
        when(schedulerUserDetails.getUser()).thenReturn(user);
    }

    List<TemplateResource> setupTemplateResources() {

        final TemplateResource templateResource = new TemplateResource();
        templateResource.setId(1);

        final Resource resource = new Resource();
        resource.setId(1);
        resource.setName("Room 1");
        templateResource.setResource(resource);
        templateResource.setStartMinutes(minutesSinceOrigin(new Date("02/01/2012 12:00")));
        templateResource.setEndMinutes(minutesSinceOrigin(new Date("02/01/2012 15:00")));
        templateResource.setFlexible(false);

        final TemplateResource templateResource1 = new TemplateResource();
        templateResource1.setId(2);

        final Resource resource1 = new Resource();
        resource1.setId(2);
        resource1.setName("Room 2");
        templateResource1.setResource(resource1);
        templateResource1.setStartMinutes(minutesSinceOrigin(new Date("02/02/2012 12:00")));
        templateResource1.setEndMinutes(minutesSinceOrigin(new Date("02/02/2012 15:00")));
        templateResource1.setFlexible(true);

        final List<TemplateResource> templateResourceList = new ArrayList<TemplateResource>();
        templateResourceList.add(templateResource);
        templateResourceList.add(templateResource1);
        return templateResourceList;
    }

    @Test
    public void mapAppointmentSizeAndCommentsTest() throws Exception {
        final List<Comments> commentsList = setupComments();
        when(mockAppointmentService.getAppointmentComments(1)).thenReturn(VisitCommentsResponse.fromTemplateApprovalHistoryList(commentsList));
        final Map result = appointmentResource.mapAppointmentSizeAndComments(1);

        final Collection<Comments> commentCollection = (Collection<Comments>) result.get("visitComments");

        TestUtils.assertNonNullAndHasThisMany(result, 2);
        TestUtils.assertNonNullAndHasThisMany(commentCollection, 2);
        assertEquals(2, result.get("total"));
    }

    @Test
    public void mapAppointmentSizeAndCommentsNullTest() throws Exception {
        when(mockAppointmentService.getAppointmentComments(1)).thenReturn(null);

        final Map result = appointmentResource.mapAppointmentSizeAndComments(1);
        assertEquals(0, result.get("total"));
    }

    List<Comments> setupComments() {
        final Comments comments = new Comments();
        comments.setId(1);
        final User user = new User();
        user.setId(1);
        user.setEcommonsId("1");
        comments.setUser(user);
        comments.setDate(new Date());
        comments.setComment("user 1");
        final Comments comments1 = new Comments();
        comments1.setId(2);
        final User user1 = new User();
        user1.setId(2);
        user1.setEcommonsId("2");
        comments1.setUser(user1);
        comments1.setDate(new Date());
        comments1.setComment("user 2");
        final List<Comments> commentsList = new ArrayList<Comments>();
        commentsList.add(comments);
        commentsList.add(comments1);
        return commentsList;
    }

    @Test
    public void todaysBookedVisitTest() throws Exception {
        final List<BookedVisitsResponse> bookedVisitList = setupBookedVisits();
        final Calendar calendar = Calendar.getInstance();
        final Date startDate = DateUtility.startOfDay(calendar);
        final Date endDate = DateUtility.nextDay(startDate);
        when(mockAppointmentService.getOnlyTodaysBookedVisitsHomeList(startDate, endDate, "", "", 1, 5, 1, "templatePath")).thenReturn(bookedVisitList);
        final List<BookedVisitsResponse> result = appointmentResource.getTodaysBookedVisits("", "", 1, 5, startDate, endDate, 1, "templatePath");
        TestUtils.assertNonNullNonEmpty(result);
        TestUtils.assertNonNullAndHasThisMany(result, 2);
    }

    private List<BookedVisitsResponse> setupBookedVisits() throws Exception {
        final Calendar calendar = Calendar.getInstance();
        final Date startDate = DateUtility.startOfDay(calendar);
        final Date endDate = DateUtility.nextDay(startDate);
        final BookedVisitsResponse bookedVisit = new BookedVisitsResponse(1, "visit1", "irb1",
                "catId1", "localId1", "subject1", "subject1","subjectmrn1",
                "scheduled", startDate, endDate, (long) 2, 1);
        final BookedVisitsResponse bookedVisit1 = new BookedVisitsResponse(2, "visit2", "irb2",
                "catId2", "localId2", "subject2", "subject1","subjectmrn2",
                "cancelled", startDate, endDate, (long) 2, 1);
        final List<BookedVisitsResponse> bookedVisitList = new ArrayList<BookedVisitsResponse>();
        bookedVisitList.add(bookedVisit);
        bookedVisitList.add(bookedVisit1);
        return bookedVisitList;
    }

    @Test
    public void lookupOverbookedVisitDataTest() throws Exception {
        final List<OverbookedResourcesResponse> bookedResourceList = new ArrayList<OverbookedResourcesResponse>();
        when(mockAppointmentService.selectedVisitForOverbookChecks(1, "", "")).thenReturn(bookedResourceList);
        final List<OverbookedResourcesResponse> result = appointmentResource.lookupOverbookedVisitData(1, "", "");
        TestUtils.assertNonNullAndHasThisMany(result, 0);
    }

    private List<BookedResource> setupBookedResources(final int type) throws Exception {
        final BookedResource bookedResource = new BookedResource();
        bookedResource.setId(1);
        final ResourceType resourceType = ResourceType.Nursing;
        final Resource resource = new Resource();
        resource.setId(1);
        resource.setName("Resource 1");
        resource.setResourceType(resourceType);
        bookedResource.setResource(resource);
        bookedResource.setScheduledStartTime(new Date());
        bookedResource.setScheduledEndTime(new Date());
        final TemplateResource templateResource = new TemplateResource();
        templateResource.setId(1);
        templateResource.setResourceGroupType("fixed");
        bookedResource.setTemplateResource(templateResource);
        final BookedVisit bookedVisit = new BookedVisit();
        bookedVisit.setId(1);
        final AppointmentStatus appointmentStatus = new AppointmentStatus();
        appointmentStatus.setId(1);
        bookedVisit.setAppointmentStatus(appointmentStatus);
        bookedResource.setBookedVisit(bookedVisit);

        final BookedResource bookedResource1 = new BookedResource();
        bookedResource1.setId(2);
        final ResourceType resourceType1 = ResourceType.Nutrition;
        final Resource resource1 = new Resource();
        resource1.setId(2);
        resource1.setName("Resource 2");
        resource1.setResourceType(resourceType1);
        bookedResource1.setResource(resource1);
        bookedResource1.setScheduledStartTime(new Date());
        bookedResource1.setScheduledEndTime(new Date());
        final TemplateResource templateResource1 = new TemplateResource();
        templateResource1.setId(2);
        templateResource1.setResourceGroupType("fixed");
        bookedResource1.setTemplateResource(templateResource1);
        final BookedVisit bookedVisit1 = new BookedVisit();
        bookedVisit1.setId(2);
        final AppointmentStatus appointmentStatus1 = new AppointmentStatus();
        appointmentStatus1.setId(1);
        bookedVisit1.setAppointmentStatus(appointmentStatus1);
        bookedResource1.setBookedVisit(bookedVisit1);

        final BookedResource bookedResource2 = new BookedResource();
        bookedResource2.setId(3);
        final ResourceType resourceType2 = ResourceType.Room;
        final Resource resource2 = new Resource();
        resource2.setId(3);
        resource2.setName("Resource 3");
        resource2.setResourceType(resourceType2);
        bookedResource2.setResource(resource2);
        bookedResource2.setScheduledStartTime(new Date());
        bookedResource2.setScheduledEndTime(new Date());
        final TemplateResource templateResource2 = new TemplateResource();
        templateResource2.setId(3);
        templateResource2.setResourceGroupType("fixed");
        bookedResource2.setTemplateResource(templateResource2);
        final BookedVisit bookedVisit2 = new BookedVisit();
        bookedVisit2.setId(3);
        final AppointmentStatus appointmentStatus2 = new AppointmentStatus();
        appointmentStatus2.setId(2);
        bookedVisit2.setAppointmentStatus(appointmentStatus2);
        bookedResource2.setBookedVisit(bookedVisit2);

        final List<BookedResource> bookedResourceList = new ArrayList<BookedResource>();
        if (type == 1) {
            bookedResourceList.add(bookedResource);
        } else if (type == 2) {
            bookedResourceList.add(bookedResource1);
        } else if (type == 3) {
            bookedResourceList.add(bookedResource2);
        } else {
            bookedResourceList.add(bookedResource);
            bookedResourceList.add(bookedResource1);
            bookedResourceList.add(bookedResource2);
        }

        return bookedResourceList;
    }

    @Test
    public void getResourcesListTest() throws Exception {
        setupTemplateResources();
        final List<Resource> resourceList = setupResources();
        when(mockAppointmentService.getResources()).thenReturn(resourceList);

        final Map<String, List<Resource>> getResourcesList = appointmentResource.getResourcesList();
        TestUtils.isNonNullNonEmpty(getResourcesList);
        TestUtils.assertNonNullAndHasThisMany(getResourcesList.get("resourcesList"), 2);
    }

    List<Resource> setupResources() {
        final Resource resource = new Resource();
        resource.setId(1);
        final Resource resource1 = new Resource();
        resource1.setId(2);
        final List<Resource> resourceList = new ArrayList<Resource>();
        resourceList.add(resource);
        resourceList.add(resource1);
        return resourceList;
    }

    @Test
    public void getEmptyResourcesListTest() throws Exception {
        when(mockAppointmentService.getResources()).thenReturn(null);
        final Map<String, List<Resource>> getResourcesList = appointmentResource.getResourcesList();
        TestUtils.assertNull(getResourcesList.get("resourcesList"));
    }

    @Test
    public void getRoomDataTest() throws Exception {
        final List<TemplateResource> templateResourceList = setupTemplateResources();
        when(mockAppointmentService.getRoomResources(1)).thenReturn(templateResourceList);
        final Map<String, String> roomResource = appointmentResource.getRoomData(1);
        TestUtils.isNonNullNonEmpty(roomResource);
        assertTrue("Expecting Resource with Name: Room 1", roomResource.get("roomResources").equals("Room 1"));
    }

    @Test
    public void getBookedResourceListTest() throws Exception {
        final List<BookedResourcesResponse> bookedResources = setupBookedResourcesResponseDTO();
        final int bookedVisitId = 1;
        final String sortBy = "r.name";
        final String orderBy = "ASC";
        final int page = 1;
        final int maxResults = 50;
        when(mockAppointmentService.getBookedResources(bookedVisitId, sortBy, orderBy, page, maxResults)).thenReturn(bookedResources);
        final List<BookedResourcesResponse> bookedresourcelist = appointmentResource.getBookedResourcesList(bookedVisitId, sortBy, orderBy, page, maxResults);
        verify(mockAppointmentService, times(1)).getBookedResources(bookedVisitId, sortBy, orderBy, page, maxResults);
        TestUtils.assertNonNullAndHasThisMany(bookedresourcelist, 1);
    }

    private List<BookedResourcesResponse> setupBookedResourcesResponseDTO() throws Exception {
        final BookedResourcesResponse bookedResource = new BookedResourcesResponse(1, "Resource", "", Calendar.getInstance().getTime(), Calendar.getInstance().getTime(), "", "", "", (long) 1, false);

        final List<BookedResourcesResponse> bookedResourceList = Lists.newArrayList();
        bookedResourceList.add(bookedResource);

        return bookedResourceList;
    }

    @Test
    public void getListOfEventResourcesTest() throws Exception {
        final List<BookedResourcesResponse> bookedResourcesResponses = getBookedResourcesResponses();
        final User user = setupSearchedEventResources();
        when(mockAppointmentService.getEventResources("1", user)).thenReturn(bookedResourcesResponses);
        final List<BookedResourcesResponse> listofeventresources = appointmentResource.getListOfEventResources("1", user);
        TestUtils.assertNonNullAndHasThisMany(listofeventresources, 3);
    }

    private List<BookedResourcesResponse> getBookedResourcesResponses() throws Exception {
        final List<BookedResource> bookedResourceList = setupBookedResources(0);
        final List<BookedResourcesResponse> bookedResourcesResponses = new ArrayList<BookedResourcesResponse>();
        for (final BookedResource resource : bookedResourceList) {
            final BookedResourcesResponse bookedResourcesResponse = new BookedResourcesResponse(resource.getId(), resource.getResource().getName(), "",
                    resource.getScheduledStartTime(), resource.getScheduledEndTime(), resource.getRejectedResourceMessage(),
                    resource.getAvailable(), resource.getTemplateResource().getResourceGroupType(), (long) bookedResourceList.size(), false);
            bookedResourcesResponses.add(bookedResourcesResponse);
        }
        return bookedResourcesResponses;
    }


    private User setupSearchedEventResources() throws Exception {
        final User user = new User();
        user.setId(1);

        final BookedVisit bookedVisit = new BookedVisit();
        bookedVisit.setId(1);
        bookedVisit.setUniquekey("1");
        final BookedResource bookedResource = new BookedResource();
        bookedResource.setId(1);
        final Resource resource = new Resource();
        resource.setId(1);
        resource.setName("Resource 1");
        bookedResource.setResource(resource);
        bookedResource.setScheduledStartTime(new Date());
        bookedResource.setScheduledEndTime(new Date());
        bookedResource.setBookedVisit(bookedVisit);
        List<BookedResource> bookedResourceList = new ArrayList<BookedResource>();
        bookedResourceList.add(bookedResource);
        bookedVisit.setBookedResourceList(bookedResourceList);

        final BookedVisit bookedVisit1 = new BookedVisit();
        bookedVisit1.setId(2);
        bookedVisit1.setUniquekey("1");
        final BookedResource bookedResource1 = new BookedResource();
        bookedResource1.setId(2);
        final Resource resource1 = new Resource();
        resource1.setId(2);
        resource1.setName("Resource 2");
        bookedResource1.setResource(resource1);
        bookedResource1.setScheduledStartTime(new Date());
        bookedResource1.setScheduledEndTime(new Date());
        bookedResource1.setBookedVisit(bookedVisit1);
        bookedResourceList = new ArrayList<BookedResource>();
        bookedResourceList.add(bookedResource1);
        bookedVisit1.setBookedResourceList(bookedResourceList);

        final List<BookedVisit> bookedVisitList = new ArrayList<BookedVisit>();
        bookedVisitList.add(bookedVisit);
        bookedVisitList.add(bookedVisit1);

        user.setBookedVisits(bookedVisitList);
        return user;
    }

    @Test
    public void getAppointmentsCalendarTest() throws Exception {
        final Calendar calendar = Calendar.getInstance();
        final Date startDate = DateUtility.startOfDay(calendar);
        final Date endDate = DateUtility.nextDay(startDate);
        final CalendarFilter calendarFilter = CalendarFilter.NO_FILTER;
        final String filterString = "";
        final List<CalendarVisitsResponse> appointmentList = setupAppointmentsList(calendarFilter, filterString);
        when(mockAppointmentService.getCalendarBookedVisits(1, calendarFilter, filterString, null,
                                                            startDate, endDate, "remoteHost", false
        )).thenReturn(appointmentList);
        final List<CalendarVisitsResponse> listOfBookedVisits = appointmentResource.getAppointmentsCalendar(1, calendarFilter,
                                                                                                            filterString, null, false, startDate, endDate, "remoteHost");
        TestUtils.assertNonNullAndHasThisMany(listOfBookedVisits, 4);
    }

    private List<CalendarVisitsResponse> setupAppointmentsList(CalendarFilter calendarFilter, final String filterString) throws Exception {
        final List<CalendarVisitsResponse> bookedVisits = new ArrayList<CalendarVisitsResponse>();
        for (int i = 1; i <= 4; i++) {
            final CalendarVisitsResponse bookedVisit = new CalendarVisitsResponse(i, "visit" + i, "pi" + i, "room" + i, "localId" + i, "subject" + i, "" + i, "07/11/2014 12:00 AM", "07/11/2014 07:00 AM", false, false);
            if (calendarFilter.equals(CalendarFilter.NO_FILTER)) {
                bookedVisits.add(bookedVisit);
            } else {
                if (filterString.equalsIgnoreCase("localId" + i)) {
                    bookedVisits.add(bookedVisit);
                } else {
                    if (calendarFilter == CalendarFilter.values()[i]) {
                        bookedVisits.add(bookedVisit);
                    }
                }
            }
        }
        return bookedVisits;
    }

    @Test
    public void testGetStudyVisitsResponse() throws Exception {
        final List<VisitTemplate> expectedList = Lists.newArrayList();
        final GetStudyVisitsResponse expected = GetStudyVisitsResponse.createGetStudyVisitsResponse(expectedList, 0L, "", "", "", 1);

        final String blah = "blah";
        final Integer zero = 0;
        final Boolean hellNo = false;

        final String ofBlah = blah;
        final Integer ofZero = zero;
        final Boolean ofFalse = hellNo;

        when(mockAppointmentService.getStudyVisits(
                ofBlah, ofBlah, ofBlah,
                ofZero, ofZero, ofZero, ofFalse)).thenReturn(expected);

        final GetStudyVisitsResponse actual = appointmentResource.getStudyVisitsResponse(
                blah, blah, blah,
                zero, zero, zero, hellNo);
        assertEquals(expected, actual);
    }

    @Test
    public void testGetVisitsByStudyList() throws Exception {
        final List<VisitTemplatesResponse> expected = Lists.newArrayList();
        final int studyId = 1;
        final boolean active = true;
        final String sortBy = "ASC";
        final String orderBy = "s.name";
        final int page = 1;
        final int maxResults = 5;
        final SearchDTO searchDTO = new SearchDTO();

        when(mockAppointmentService.getVisitsByStudy(studyId, active, sortBy, orderBy,
                page, maxResults, searchDTO)).thenReturn(expected);
        final List<VisitTemplatesResponse> actual = appointmentResource.getVisitsByStudyList(studyId, active, sortBy, orderBy,
                page, maxResults, searchDTO);
        assertEquals(expected, actual);
        TestUtils.assertNonNullAndHasThisMany(actual, 0);
    }

    @Test
    public void testGetSearchVisitResourceResponse() throws Exception {
        final List<TemplateResource> expectedList = Lists.newArrayList();
        final GetSearchVisitResourceResponse expected = GetSearchVisitResourceResponse.createGetSearchVisitsResponse(expectedList, 0L);

        final String blah = "blah";
        final Integer zero = 0;
        final Integer visitId = 1;
        final String ofBlah = blah;
        final Integer ofZero = zero;
        final Integer ofVisitId = visitId;

        when(mockAppointmentService.getVisitResources(
                ofBlah, ofBlah,
                ofZero, ofZero, ofVisitId)).thenReturn(expected);

        final GetSearchVisitResourceResponse actual = appointmentResource.getSearchVisitResourceResponse(
                blah, blah,
                zero, zero, visitId);

        assertEquals(expected, actual);
    }

    @Test
    public void testUnmarshalJsonListOfInts() {
        assertEquals(emptyList(), appointmentResource.unmarshalJsonListOfInts("[]"));
        assertEquals(Arrays.asList(1, 2, 3), appointmentResource.unmarshalJsonListOfInts("[1,2,3]"));
        assertEquals(Arrays.asList(-99, 0, 42), appointmentResource.unmarshalJsonListOfInts("[-99,0,42]"));

        intercept(Exception.class, () -> appointmentResource.unmarshalJsonListOfInts("asdf"));
        intercept(Exception.class, () -> appointmentResource.unmarshalJsonListOfInts("42"));
        intercept(Exception.class, () -> appointmentResource.unmarshalJsonListOfInts(null));
    }

    @Test
    public void test_switchVisitSubject() {

        try {
            appointmentResource.switchVisitSubject(false, 1, 2, true, "hello");
        } catch (NullPointerException npe) {
            verify(mockAppointmentService, times(1)).switchVisitSubject(1, 2, true, "hello");
        }

        try {
            appointmentResource.switchVisitSubject(false, 0, 2, false, "bye");
        } catch (NullPointerException npe) {
            verify(mockAppointmentService, times(1)).switchVisitSubject(0, 2, false, "bye");
        }
    }

    @Test
    public void test_confirmSwitchVisitSubject() {

        try {
            appointmentResource.switchVisitSubject(true, 1, 2, true, "hello");
        } catch (NullPointerException npe) {
            verify(mockAppointmentService, times(1)).confirmSwitchVisitSubject(1, 2, true, "hello");
        }

        try {
            appointmentResource.switchVisitSubject(true, 0, 2, false, "bye");
        } catch (NullPointerException npe) {
            verify(mockAppointmentService, times(1)).confirmSwitchVisitSubject(0, 2, false, "bye");
        }
    }

    @Test
    public void testGetBookedVisitsList()
    {
        final int userId = 1;
        final String search = "{\"searchItems\":[{\"key\":\"key1\",\"value\":\"val1\"}]}";
        final String sortBy = "asc";
        final String orderBy = "any";
        final int page = 1;
        final int maxResults = 5;

        final String sampleDate = "01/01/2016";

        final Date fromDate = DateUtility.parse(DateUtility.monthDayYear(), sampleDate);
        final Date toDate = DateUtility.nextDay(fromDate);
        final long fromDateLong = fromDate.getTime();
        final long toDateLong = toDate.getTime();

        final SearchDTO searchDTO = new SearchDTO();
        SearchDTO.SearchItem searchItem = new SearchDTO.SearchItem();
        searchItem.setKey("key1");
        searchItem.setValue("val1");
        searchDTO.setSearchItems(Lists.newArrayList(searchItem));

        //when(appointmentResource.assembleSearchDTO(search)).thenReturn(searchDTO);
        when(appointmentResource.getUser().getId()).thenReturn(userId);

        doReturn(appointmentResource.getUser()).when(spyAppointmentResource).getUser();
        doReturn(searchDTO).when(spyAppointmentResource).assembleSearchDTO(search);

        spyAppointmentResource.getBookedVisitsList(
                sortBy, orderBy,page, maxResults,sampleDate, "", search);
        verify(mockAppointmentService, times(1)).getBookedVisitsList(
                userId, searchDTO, sortBy, orderBy, page, maxResults,
                request.getRemoteHost(), fromDate, null);
    }

    @Test
    public void testGetBookedVisitData()
    {
        final int bookedVisitId = 2;

        appointmentResource.getBookedVisitData(bookedVisitId);
        verify(mockAppointmentService, times(1)).getBookedVisitData(bookedVisitId);
    }

    @Test
    public void testGetVisitsByStudy()
    {
        final int studyId = 2;
        final boolean active = true;
        final String sortBy = "asc";
        final String orderBy = "any";
        final int page = 1;
        final int maxResults = 14;

        appointmentResource.getVisitsByStudy(studyId, active, sortBy, orderBy,
                page, maxResults, null);

        verify(mockAppointmentService, times(1)).getVisitsByStudy(studyId, active,
                sortBy, orderBy, page, maxResults, null);
    }

    @Test
    public void testGetStudyVisits()
    {
        final String filterString = "filter";
        final String sortBy = "asc";
        final String orderBy = "any";
        final int page = 1;
        final int maxResults = 14;
        final int studyId = 2;
        final boolean approved = true;

        appointmentResource.getStudyVisits(filterString, sortBy, orderBy, page, maxResults, studyId, approved);
        verify(mockAppointmentService, times(1)).getStudyVisits(filterString, sortBy, orderBy, page, maxResults, studyId, approved);
    }

    @Test
    public void testGetSearchVisitResource()
    {
        final String sortBy = "asc";
        final String orderBy = "any";
        final int page = 1;
        final int maxResults = 11;
        final int visitId = 2;

        appointmentResource.getSearchVisitResource(sortBy,orderBy, page,maxResults,visitId);
        verify(mockAppointmentService, times(1)).getVisitResources(sortBy,orderBy, page,maxResults,visitId);
    }

    @Test
    public void testGetScheduledVisitDetails()
    {
        final int id = 2;
        final User user = new User();
        user.setId(3);
        user.setLastName("last");
        user.setFirstName("first");
        when(appointmentResource.getUser()).thenReturn(user);

        BookedVisit bookedVisit = createdSampleBookedVisit();
        String expectedJson = "{" +
                "\"visitdate\":\"2016-02-05 00:00\",\"visitTypeId\":\"4\"," +
                "\"check_visit_activities_readonly\":false,\"detailVisitTime\":\"35\"," +
                "\"scheduledata\":\"schedule information\",\"visitCommentsTotal\":0,\"visituser\":\"by user: user data not available.\",\"relativeTime\":\"false\"," +
                "\"userdata\":\"user information\",\"subjectdetailname\":\"No Subject Assigned\"," +
                "\"check_visit_duration_readonly\":false,\"subjectMrn\":\"N/A\",\"studyId\":44," +
                "\"studyPIdetailname\":\"N/A\"}";
        when(mockAppointmentService.getVisitDetails(id, user, request.getRemoteHost())).thenReturn(bookedVisit);
        String jsonString = appointmentResource.getScheduledVisitDetails(id);

        verify(mockAppointmentService, times(1)).getVisitDetails(id, user, request.getRemoteHost());
        Assert.assertEquals(expectedJson, jsonString);
    }

    @Test
    public void testGetScheduledVisitHistory() {

        final User user0 = new User();
        user0.setId(7);
        user0.setLastName("last0");
        user0.setFirstName("first0");

        final User user1 = new User();
        user1.setId(8);
        user1.setLastName("last1");
        user1.setFirstName("first1");
        user1.setEcommonsId("user1");

        when(appointmentResource.getUser()).thenReturn(user0);

        BookedVisit bookedVisit1 = new BookedVisit();
        bookedVisit1.setId(99);

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

        when(mockAppointmentService.getVisitHistory(99, user0, request.getRemoteHost())).thenReturn(scheduledVisitHistoryDTO);

        String jsonString = appointmentResource.getScheduledVisitHistory(99);

        ScheduledVisitHistoryDTO returnedDTO = appointmentResource.gson.fromJson(jsonString, ScheduledVisitHistoryDTO.class);

        List<Map<String, String>> returnedBookedActivityList = returnedDTO.getBookedVisitActivity();
        assertEquals(2, returnedBookedActivityList.size());
        Map<String, String> returnedLogItem1 = returnedBookedActivityList.get(0);
        Map<String, String> returnedLogItem2 = returnedBookedActivityList.get(1);

        assertEquals(3, returnedLogItem1.size());
        assertEquals(BookedVisitActivityLogStatics.SCHEDULED.getLogString(), returnedLogItem1.get("action"));
        assertEquals("user1", returnedLogItem1.get("user"));
        assertEquals(DateUtility.format(date24HTime(), date1), returnedLogItem1.get("date"));

        // user is not set in the following case
        assertEquals(2, returnedLogItem2.size());
        assertEquals(BookedVisitActivityLogStatics.RESCHEDULED.getLogString(), returnedLogItem2.get("action"));
        assertEquals(DateUtility.format(date24HTime(), date2), returnedLogItem2.get("date"));

    }

    private BookedVisit createdSampleBookedVisit()
    {
        final Study study = new Study();
        study.setId(44);

        final VisitTemplate visitTemplate= new VisitTemplate();
        visitTemplate.setDuration(35);
        visitTemplate.setRelativeTime(false);
        final String name = "name";
        final VisitType visitType = new VisitType();
        visitType.setId(4);

        final SubjectMrn subjectMrn = null;
        final AppointmentStatus appointmentStatus = new AppointmentStatus(3, Statics.CHECKED_OUT_APPT_STATUS);
        final CancellationStatus cancelStatus = null;
        final AppointmentStatusReason appointmentStatusReason = null;
        final AppointmentStatusReason cancelStatusReason = null;
        final AppointmentStatusReason checkoutStatusReason = new AppointmentStatusReason();
        checkoutStatusReason.setId(15);
        checkoutStatusReason.setName("Visit Completed");
        final Date scheduledStartTime = DateUtility.parse(DateUtility.monthDayYear(), "01/01/2016");
        final Date scheduledEndTime = DateUtility.parse(DateUtility.monthDayYear(), "02/05/2016");
        final Date checkInDate = DateUtility.parse(DateUtility.monthDayYear(), "01/01/2016");
        final Date checkOutDate = DateUtility.parse(DateUtility.monthDayYear(), "2/05/2016");
        final Date cancelDate = null;
        final String comment = "This is a comment";
        final Date scheduleDate = DateUtility.parse(DateUtility.monthDayYear(), "12/11/2015");;
        final String checkoutStatusReasonName = checkoutStatusReason.getName();
        final int total = 1;
        final int size = 1;
        final int limit = 11;
        final int start = 1;
        final String sessionId = "abc101";
        final String uniquekey = "unique99";
        final String userdata = "user information";
        final String scheduledata = "schedule information";
        final List<BookedResource> bookedResourceList = null;
        final Resource selectedRoom = new Resource(1, "resourceRoom", ResourceType.Room,
                0, null, null, null, null, null);

        final String rooms = "rooms";
        final String errorMsg = null;

        BookedVisit bookedVisit = new BookedVisit(study, visitTemplate, name,
                visitType,
                subjectMrn,
                appointmentStatus, cancelStatus, appointmentStatusReason, cancelStatusReason,
                checkoutStatusReason, scheduledStartTime, scheduledEndTime, checkInDate, checkOutDate,
                cancelDate, comment, scheduleDate, checkoutStatusReasonName, total, size, limit, start,
                sessionId, uniquekey, userdata, scheduledata, bookedResourceList, selectedRoom, rooms, errorMsg);


        return bookedVisit;
    }

    @Test
    public void testBatchEntry()
    {
        AppointmentResource appointmentResourceSpy = spy(appointmentResource);

        String data = "{\"visit\":\"42\"}";
        String status = "status";

        when(mockAppointmentService.batchEntryUpdate(
                any(VisitSpecsDTO.class),
                any(User.class),
                anyString(),
                anyString())).thenReturn(status);

        doReturn("path").when(appointmentResourceSpy).getContextRealPath();

        // method under test
        String result = appointmentResourceSpy.batchEntry(data);
        verify(mockAppointmentService, times(1))
                .batchEntryUpdate(any(VisitSpecsDTO.class),
                        any(User.class),
                        anyString(),
                        anyString());


        assertEquals(status, result);
    }

    @Test
    public void testGetOverbookedVisitsData()
    {
        int id = 22;

        String result = appointmentResource.getOverbookedVisitsData(id, "", "");
        verify(mockAppointmentService, times(1)).logViewVisits(user, request.getRemoteHost(), "Check for Overbooked Resources.");
        verify(mockAppointmentService, times(1)).selectedVisitForOverbookChecks(id, "", "");
    }

    @Test
    public void testOverbookRoomAppointment()
    {
        String data = "{\"study\":120,\"subjectMrnId\":100,\"visit\":559,\"startDate\":1486728000000," +
                      "\"startTime\":1486728000000,\"overrideReason\":\"4\",\"roomSelected\":\"45\",\"comment\":\"My " +
                      "overbook room comment\"}";

        AppointmentResource appointmentResourceSpy = spy(appointmentResource);


        Mockito.doReturn("testPath").when(appointmentResourceSpy).getContextRealPath();

        appointmentResourceSpy.overbookRoomAppointment(data);

        verify(mockAppointmentService, times(1)).confirmOverbookRoomData(any(VisitSpecsDTO.class),
                                                                         any(User.class), anyString(), anyString());

    }
    @Test
    public void testSaveComment()
    {
        String comment = "newComment";
        String data = "{\"comment\":\"" + comment + "\"}";
        final VisitSpecsDTO visitTime = new VisitSpecsDTO();
        visitTime.setComment(comment);

        Comments comments = new Comments();
        comments.setComment(comment);

        User user = new User();
        user.setFirstName("First");
        user.setLastName("Last");
        user.setId(100);
        comments.setUser(user);

        when(mockAppointmentService.saveComment(any(VisitSpecsDTO.class), any(User.class), anyString())).thenReturn(comments);
        String commentResult = appointmentResource.saveComment(data);
        verify(mockAppointmentService, times(1)).saveComment(any(VisitSpecsDTO.class), any(User.class), anyString());
    }

}
