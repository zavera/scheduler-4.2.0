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

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import edu.harvard.catalyst.scheduler.core.Statics;
import edu.harvard.catalyst.scheduler.dto.VisitSpecsDTO;
import edu.harvard.catalyst.scheduler.entity.*;
import edu.harvard.catalyst.scheduler.persistence.*;
import edu.harvard.catalyst.scheduler.util.DateUtility;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import java.time.LocalDateTime;
import java.time.Month;
import java.util.*;

import static edu.harvard.catalyst.scheduler.service.SearchAlgorithmService.INCREMENT_FACTOR;
import static edu.harvard.catalyst.scheduler.util.DateUtility.*;
import static edu.harvard.catalyst.scheduler.util.MiscUtil.divideByMinsPerHour;
import static edu.harvard.catalyst.scheduler.util.MiscUtil.moduloMinsPerHour;
import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

/**
 * @author clint
 * @date Aug 29, 2013
 */
@RunWith(MockitoJUnitRunner.class)
public final class SearchAlgorithmServiceTest {

    private SearchAlgorithmService searchAlgorithmService;
    private SearchAlgorithmService searchAlgorithmServiceSpy;

    @Mock
    private SearchAlgorithmDAO searchAlgorithmDAO;

    @Mock
    private ResourceDAO resourceDAO;

    @Mock
    private StudyDAO studyDAO;

    @Mock
    private SubjectDAO subjectDAO;

    @Mock
    private TemplateResourceDAO templateResourceDAO;

    @Before
    public void setUp() {
        searchAlgorithmService = makeService();
        searchAlgorithmServiceSpy = Mockito.spy(searchAlgorithmService);
    }

    private SearchAlgorithmService makeService() {
        return new SearchAlgorithmService(searchAlgorithmDAO, resourceDAO, studyDAO, subjectDAO, templateResourceDAO);
    }


    @Test
    public void test_setBookedVisits() throws Exception {
        final boolean confirmEvent = true;
        final boolean rejectedCheck = false;
        final User user = mock(User.class);
        final BookedVisit bookedVisit = mock(BookedVisit.class);
        final List<BookedVisit> bookedVisitList = new ArrayList<BookedVisit>();
        bookedVisitList.add(bookedVisit);

        searchAlgorithmService.setBookedVisits(confirmEvent, rejectedCheck, user, bookedVisitList);

        verify(user, times(1)).setBookedVisits(bookedVisitList);
        when(user.getBookedVisits()).thenReturn(bookedVisitList);
        assertEquals(user.getBookedVisits().size(), 1);
    }

    @Test
    public void test_getSearchedAppointments() throws Exception {
        final boolean confirmEvent = false;
        final boolean rejectedCheck = true;
        final boolean isInpatient = false;

        final SubjectMrn subjectMrn = mock(SubjectMrn.class);
        final VisitTemplate visit = mock(VisitTemplate.class);

        final Calendar startDateCal = Calendar.getInstance();
        startDateCal.set(Calendar.HOUR, 7);
        startDateCal.set(Calendar.MINUTE, 0);
        startDateCal.set(Calendar.MILLISECOND, 0);
        startDateCal.set(Calendar.AM_PM, Calendar.AM);
        final Date startDate = startDateCal.getTime();

        final Calendar endDateCal = Calendar.getInstance();
        endDateCal.setTimeInMillis(startDate.getTime());
        endDateCal.set(Calendar.HOUR, 14);
        endDateCal.set(Calendar.MINUTE, 0);
        endDateCal.set(Calendar.MILLISECOND, 0);
        endDateCal.set(Calendar.PM, Calendar.PM);
        final Date endDate = endDateCal.getTime();

        final List<BookedVisit> result = searchAlgorithmService.findCandidateVisits(confirmEvent, rejectedCheck,
                isInpatient,
                subjectMrn, visit, startDate,
                endDate
        );

        assertNotNull(result);
    }

    @Test
    public void test_allocateTime() {
        final Calendar feb1cal = Calendar.getInstance();
        feb1cal.set(2012, Calendar.FEBRUARY, 1, 0, 30, 0);
        final Date feb1Date = feb1cal.getTime();

        final Calendar may13calSearchDayOnly = Calendar.getInstance();
        may13calSearchDayOnly.set(2014, Calendar.MAY, 13, 0, 0, 0);
        final Date may13DateSearchDayOnlyDate = may13calSearchDayOnly.getTime();

        final Calendar may13calExpectedStart = Calendar.getInstance();
        may13calExpectedStart.set(2014, Calendar.MAY, 13, 1, 30, 0);
        final Date may13DateExpectedStart = may13calExpectedStart.getTime();


        allocateTimeHelper(
                may13DateSearchDayOnlyDate,
                feb1Date,                     // day 1 in template
                60,                           // slot offset
                30,                           // duration
                may13DateExpectedStart,       // slot plus (hours,minutes) from feb?Date
                90,                           // slot plus (hours,minutes), as minutes
                120
        );                         // start plus duration

    }

    void allocateTimeHelper(
            final Date searchDate,
            final Date resourceStartDate,
            final int slotStartOffsetMinutes,
            final int resourceDurationMinutes,
            final Date expectedStartDate,
            final int expectedSlotOffsetStartMinute,
            final int expectedSlotOffsetEndMinute
    ) {

        final TemplateResource templateResource = mock(TemplateResource.class);

        when(templateResource.getDuration()).thenReturn(resourceDurationMinutes);
        when(templateResource.getStartDate()).thenReturn(resourceStartDate);

        final Calendar candidateEndCalendar = Calendar.getInstance();
        candidateEndCalendar.setTimeInMillis(expectedStartDate.getTime()
                + resourceDurationMinutes * MILLISECS_PER_MIN);
        final Date expectedEndDate = candidateEndCalendar.getTime();


        searchAlgorithmService.allocateTime(templateResource, slotStartOffsetMinutes, searchDate);

        verify(templateResource).setAvailableStartTimeInMin(expectedSlotOffsetStartMinute);
        verify(templateResource).setAvailableEndTimeInMin(expectedSlotOffsetEndMinute);

        ArgumentCaptor<Date> dateCaptor = ArgumentCaptor.forClass(Date.class);

        verify(templateResource).setScheduledStartTime(dateCaptor.capture());
        assertEquals(dateCaptor.getValue().toString(), expectedStartDate.toString());

        verify(templateResource).setScheduledEndTime(dateCaptor.capture());
        assertEquals(dateCaptor.getValue().toString(), expectedEndDate.toString());
    }

    @Test
    public void testAllocateTimeManyTimes() {
        int many = 200;

        for (int i=0; i<many; i++) {
            test_allocateTime();
        }
    }

    List<ResourceSchedule> setupResourceScheduleList() {
        final Calendar calendar = Calendar.getInstance();

        final long oneHour = MILLISECS_PER_MIN * MINS_PER_HR;

        calendar.setTimeInMillis(0L);
        final Date zeroDate = calendar.getTime();

        calendar.setTimeInMillis(oneHour);
        final Date oneHourDate = calendar.getTime();

        calendar.setTimeInMillis(oneHour - MILLISECS_PER_MIN);
        final Date oneHourAlmost = calendar.getTime();

        calendar.setTimeInMillis(oneHour * 2 - MILLISECS_PER_MIN);
        final Date twoHoursAlmost = calendar.getTime();

        calendar.setTimeInMillis(oneHour * 3 - MILLISECS_PER_MIN);
        final Date threeHoursAlmost = calendar.getTime();

        final ResourceSchedule resourceSchedule1 = new ResourceSchedule();
        resourceSchedule1.setStartTime(zeroDate);
        resourceSchedule1.setEndTime(oneHourAlmost);
        resourceSchedule1.setQuantity(0);

        final ResourceSchedule resourceSchedule2 = new ResourceSchedule();
        resourceSchedule2.setStartTime(oneHourDate);
        resourceSchedule2.setEndTime(twoHoursAlmost);
        resourceSchedule2.setQuantity(1);

        final ResourceSchedule resourceSchedule3 = new ResourceSchedule();
        resourceSchedule3.setStartTime(zeroDate);
        resourceSchedule3.setEndTime(threeHoursAlmost);
        resourceSchedule3.setQuantity(2);

        final List<ResourceSchedule> resourceScheduleList = new ArrayList<ResourceSchedule>();
        resourceScheduleList.add(resourceSchedule1);
        resourceScheduleList.add(resourceSchedule2);
        resourceScheduleList.add(resourceSchedule3);

        return resourceScheduleList;
    }

    @Test
    public void test_getPeriodToQuantityMap() {

        final List<ResourceSchedule> resourceScheduleList = setupResourceScheduleList();

        final Map<Integer, Integer> periodToQuantityMap = searchAlgorithmService.getPeriodToQuantityMap(resourceScheduleList);

        assertTrue(periodToQuantityMap.size() == 12); // three hours X four periods

        final int gmtOffset = 76;

        for (int i = gmtOffset; i < gmtOffset + 4; i++) {
            final int value = periodToQuantityMap.get(i);
            assertTrue(value == 2);
        }
        for (int i = gmtOffset + 4; i < gmtOffset + 8; i++) {
            final int value = periodToQuantityMap.get(i);
            assertTrue(value == 3);
        }
        for (int i = gmtOffset + 8; i < gmtOffset + 12; i++) {
            final int value = periodToQuantityMap.get(i);
            assertTrue(value == 2);
        }
    }

    @Test
    public void test_resourceTimeComparator() {
        BookedResource br100200 = new BookedResource();
        br100200.setScheduledStartTime(new Date(100));
        br100200.setScheduledEndTime(new Date(200));

        BookedResource secondBr100200 = new BookedResource();
        secondBr100200.setScheduledStartTime(new Date(100));
        secondBr100200.setScheduledEndTime(new Date(200));

        BookedResource br200300 = new BookedResource();
        br200300.setScheduledStartTime(new Date(200));
        br200300.setScheduledEndTime(new Date(300));

        BookedResource br100300 = new BookedResource();
        br100300.setScheduledStartTime(new Date(100));
        br100300.setScheduledEndTime(new Date(300));

        assertEquals(0, SearchAlgorithmService.resourceTimeComparator.compare(br100200, secondBr100200));
        assertEquals(-1, SearchAlgorithmService.resourceTimeComparator.compare(br100200, br100300));
        assertEquals(1, SearchAlgorithmService.resourceTimeComparator.compare(br100300, br100200));

        assertEquals(-1, SearchAlgorithmService.resourceTimeComparator.compare(br100200, br200300));
        assertEquals(1, SearchAlgorithmService.resourceTimeComparator.compare(br200300, br100200));
    }

    @Test
    public void test_visitStartTimeComparator() {
        BookedVisit bv100 = new BookedVisit();
        bv100.setScheduledStartTime(new Date(100));

        BookedVisit secondBv100 = new BookedVisit();
        secondBv100.setScheduledStartTime(new Date(100));

        BookedVisit bv200 = new BookedVisit();
        bv200.setScheduledStartTime(new Date(200));

        assertEquals(0, SearchAlgorithmService.visitStartTimeComparator.compare(bv100, secondBv100));
        assertEquals(-1, SearchAlgorithmService.visitStartTimeComparator.compare(bv100, bv200));
        assertEquals(1, SearchAlgorithmService.visitStartTimeComparator.compare(bv200, bv100));
    }

    @Test
    public void test_sortCandidateVisits() {
        BookedVisit bv100 = new BookedVisit();
        bv100.setScheduledStartTime(new Date(100));

        BookedVisit bv200 = new BookedVisit();
        bv200.setScheduledStartTime(new Date(200));

        BookedResource br100200 = new BookedResource();
        br100200.setScheduledStartTime(new Date(100));
        br100200.setScheduledEndTime(new Date(200));

        BookedResource br200300 = new BookedResource();
        br200300.setScheduledStartTime(new Date(200));
        br200300.setScheduledEndTime(new Date(300));

        List<BookedResource> bookedResourceList = Lists.newArrayList(br200300, br100200);
        List<BookedVisit> bookedVisitList = Lists.newArrayList(bv200, bv100);

        bv200.setBookedResourceList(bookedResourceList);

        assertEquals(br200300, bookedResourceList.get(0));
        assertEquals(bv200, bookedVisitList.get(0));

        searchAlgorithmService.sortCandidateVisits(true, bookedVisitList);
        assertEquals(br100200, bookedResourceList.get(0));
        assertEquals(bv200, bookedVisitList.get(0));

        searchAlgorithmService.sortCandidateVisits(false, bookedVisitList);
        assertEquals(br200300, bookedResourceList.get(0));
        assertEquals(bv200, bookedVisitList.get(0));
    }

    @Test
    public void test_findCandidateVisits1() {

        VisitSpecsDTO visitSpecsDTO = new VisitSpecsDTO();
        visitSpecsDTO.setVisit(10);
        visitSpecsDTO.setSubjectMrnId(20);
        visitSpecsDTO.setStartDate(30L);
        visitSpecsDTO.setEndDate(40L);

        VisitTemplate visitTemplate = new VisitTemplate();
        visitTemplate.setDuration(50);

        SubjectMrn subjectMrn = new SubjectMrn();

        User user = new User();

        UserSession userSession = mock(UserSession.class);
        when(userSession.getUser()).thenReturn(user);

        when(studyDAO.findVisitTemplateById(10)).thenReturn(visitTemplate);
        when(subjectDAO.findSubjectMrnById(20)).thenReturn(subjectMrn);

        Date startDate = new Date(30L);
        Date endDate = new Date(40L);

        List<BookedVisit> candidateVisits = Lists.newArrayList();

        doReturn(candidateVisits).when(searchAlgorithmServiceSpy).findCandidateVisits(
                true,
                true,
                true,
                subjectMrn,
                visitTemplate,
                startDate,
                endDate);

        doNothing().when(searchAlgorithmServiceSpy).sortCandidateVisits(
                true, candidateVisits);

        List<BookedVisit> result = searchAlgorithmServiceSpy.findCandidateVisits(
                visitSpecsDTO,
                userSession,
                true,
                true,
                true);

        assertEquals(candidateVisits, result);

        verify(searchAlgorithmServiceSpy, times(1))
                .findCandidateVisits(
                        true,
                        true,
                        true,
                        subjectMrn,
                        visitTemplate,
                        startDate,
                        endDate);
        verify(searchAlgorithmServiceSpy, times(1))
                .setBookedVisits(
                        true,
                        true,
                        user,
                        candidateVisits);
        verify(searchAlgorithmServiceSpy, times(1))
                .sortCandidateVisits(
                        true,
                        candidateVisits);
    }
    @Test
    public void test_isDurationLessThan24Hours() {
        assertTrue(searchAlgorithmService.isDurationLessThan24Hours(
                false, false, 0));
        assertTrue(searchAlgorithmService.isDurationLessThan24Hours(
                false, false, MINS_PER_DAY - 1));

        assertFalse(searchAlgorithmService.isDurationLessThan24Hours(
                false, false, MINS_PER_DAY));
        assertFalse(searchAlgorithmService.isDurationLessThan24Hours(
                false, false, MINS_PER_DAY + 1));

        assertTrue(searchAlgorithmService.isDurationLessThan24Hours(
                false, true, MINS_PER_DAY * 10));
        assertTrue(searchAlgorithmService.isDurationLessThan24Hours(
                true, false, MINS_PER_DAY * 10));
        assertTrue(searchAlgorithmService.isDurationLessThan24Hours(
                true, true, MINS_PER_DAY * 10));
    }

    @Test
    public void test_setupSearchDates() {

        VisitTemplate visitTemplate = new VisitTemplate();
        visitTemplate.setDuration(10);

        Date startDate = new Date(10L);
        Date endDate = new Date(10L + (3*MILLISECS_PER_MIN*MINS_PER_DAY));

        List<Date> inpatientResult = Lists.newArrayList(startDate);
        List<Date> elseTwoFalseResult = dateInterval(startDate, endDate);
        List<Date> elseResult = dateInterval(startDate, startDate);

        doReturn(true).when(searchAlgorithmServiceSpy).isRelativeTimeTemplate(
                true,
                visitTemplate);

        List<Date> result = searchAlgorithmServiceSpy.setupSearchDates(
                false,
                false,
                true,
                visitTemplate,
                startDate,
                endDate);

        assertEquals(inpatientResult, result);

        verify(searchAlgorithmServiceSpy, times(1))
                .isRelativeTimeTemplate(
                        true,
                        visitTemplate);
        verify(searchAlgorithmServiceSpy, times(1))
                .isDurationLessThan24Hours(
                        false,
                        false,
                        10);

        assertEquals(elseTwoFalseResult, searchAlgorithmServiceSpy.setupSearchDates(
                false,
                false,
                false,
                visitTemplate,
                startDate,
                endDate));
        assertEquals(elseResult, searchAlgorithmServiceSpy.setupSearchDates(
                true,
                true,
                false,
                visitTemplate,
                startDate,
                endDate));
    }

    @Test
    public void test_findCandidateVisits2() {
        VisitTemplate visitTemplate = new VisitTemplate();
        List<BookedVisit> bookedVisitList = Lists.newArrayList(new BookedVisit());
        SubjectMrn subjectMrn = new SubjectMrn();

        Date startDate = new Date(10L);
        Date endDate = new Date(20L);

        List<Date> searchDates = Lists.newArrayList(startDate, endDate);

        doReturn(searchDates).when(searchAlgorithmServiceSpy).setupSearchDates(
                true, true, true,
                visitTemplate, startDate, endDate);

        doReturn(bookedVisitList).when(searchAlgorithmServiceSpy).findCandidateVisits(
                searchDates,
                startDate,
                endDate,
                subjectMrn,
                visitTemplate,
                true, true, true);

        List<BookedVisit> result = searchAlgorithmServiceSpy.findCandidateVisits(
                true,
                true,
                true,
                subjectMrn,
                visitTemplate,
                startDate,
                endDate);

        assertEquals(bookedVisitList, result);

        verify(searchAlgorithmServiceSpy, times(1))
                .findCandidateVisits(
                        searchDates,
                        startDate,
                        endDate,
                        subjectMrn,
                        visitTemplate,
                        true,
                        true,
                        true);
    }

    @Test
    public void test_isRelativeTimeTemplate() {
        VisitTemplate visitTemplate = new VisitTemplate();

        assertFalse(searchAlgorithmService.isRelativeTimeTemplate(true, visitTemplate));

        assertTrue(searchAlgorithmService.isRelativeTimeTemplate(false, visitTemplate));

        visitTemplate.setRelativeTime(true);
        assertTrue(searchAlgorithmService.isRelativeTimeTemplate(true, visitTemplate));
    }

    @Test
    public void test_retrieveFloatResourceList() {
        VisitTemplate visitTemplate = new VisitTemplate();

        TemplateResource tr1 = new TemplateResource();
        tr1.setId(1);

        TemplateResource tr2 = new TemplateResource();
        tr2.setId(2);

        List<TemplateResource> floatTrList = Lists.newArrayList(tr1, tr2);

        doReturn(floatTrList).when(searchAlgorithmDAO).getSingleFloatTemplateResources(
                visitTemplate);

        Map<String, List<TemplateResource>> result = searchAlgorithmService.retrieveFloatResourceList(visitTemplate);

        assertEquals(2, result.size());
        assertEquals(Lists.newArrayList(tr1), result.get("1"));
        assertEquals(Lists.newArrayList(tr2), result.get("2"));

        doReturn(null).when(searchAlgorithmDAO)
                .getSingleFloatTemplateResources(visitTemplate);
        result = searchAlgorithmService.retrieveFloatResourceList(visitTemplate);
        assertTrue(result.isEmpty());

        doReturn(Lists.newArrayList()).when(searchAlgorithmDAO)
                .getSingleFloatTemplateResources(visitTemplate);
        result = searchAlgorithmService.retrieveFloatResourceList(visitTemplate);
        assertTrue(result.isEmpty());
    }

    @Test
    public void test_getGroupedResourceListMap() {

        TemplateResource tr1a = new TemplateResource();
        TemplateResource tr1b = new TemplateResource();
        TemplateResource tr2 = new TemplateResource();

        TemplateResourceGroup trg1a = new TemplateResourceGroup();
        trg1a.setGroupId("1");
        trg1a.setTemplateResource(tr1a);

        TemplateResourceGroup trg1b = new TemplateResourceGroup();
        trg1b.setGroupId("1");
        trg1b.setTemplateResource(tr1b);

        TemplateResourceGroup trg2 = new TemplateResourceGroup();
        trg2.setGroupId("2");
        trg2.setTemplateResource(tr2);


        List<TemplateResourceGroup> groupedTrList = Lists.newArrayList(trg1a, trg2, trg1b);

        Map<String, List<TemplateResource>> result = searchAlgorithmService.getGroupedResourceListMap(groupedTrList);

        assertEquals(2, result.size());
        assertEquals(Lists.newArrayList(tr1a, tr1b), result.get("1"));
        assertEquals(Lists.newArrayList(tr2), result.get("2"));

        result = searchAlgorithmService.getGroupedResourceListMap(null);
        assertTrue(result.isEmpty());

        result = searchAlgorithmService.getGroupedResourceListMap(Lists.newArrayList());
        assertTrue(result.isEmpty());
    }

    @Test
    public void test_retrieveGroupedResourceList() {
        VisitTemplate visitTemplate = new VisitTemplate();
        List<TemplateResourceGroup> trGroupList = Lists.newArrayList(new TemplateResourceGroup());

        doReturn(trGroupList).when(searchAlgorithmDAO).getTemplateResourceGroups(
                visitTemplate, true);

        searchAlgorithmServiceSpy.retrieveGroupedResourceList(visitTemplate, true);

        verify(searchAlgorithmDAO, times(1))
                .getTemplateResourceGroups(visitTemplate, true);

        verify(searchAlgorithmServiceSpy, times(1))
                .getGroupedResourceListMap(trGroupList);

    }

    @Test
    public void test_findMaxEndMinuteRelativeToSearchDate() {

        int deltaMinutes = 50;
        Date searchDate = new Date(10 * MILLISECS_PER_MIN);
        Date endDate = new Date((10 + deltaMinutes) * MILLISECS_PER_MIN);
        int visitDurationInMin = 20;
        int startMinute = 30;
        int endMinute = 40;

        doReturn(true).when(searchAlgorithmServiceSpy)
                .isDurationLessThan24Hours(true, true, visitDurationInMin);

        assertEquals(deltaMinutes + startMinute, searchAlgorithmServiceSpy
                .findMaxEndMinuteRelativeToSearchDate(
                        endDate,
                        true,
                        true,
                        true,
                        visitDurationInMin,
                        startMinute,
                        endMinute,
                        searchDate));

        verify(searchAlgorithmServiceSpy, times(1))
                .isDurationLessThan24Hours(true, true, visitDurationInMin);


        doReturn(false).when(searchAlgorithmServiceSpy)
                .isDurationLessThan24Hours(true, true, visitDurationInMin);

        assertEquals(deltaMinutes, searchAlgorithmServiceSpy
                .findMaxEndMinuteRelativeToSearchDate(
                        endDate,
                        true,
                        true,
                        true,
                        visitDurationInMin,
                        startMinute,
                        endMinute,
                        searchDate));

        assertEquals(endMinute, searchAlgorithmServiceSpy
                .findMaxEndMinuteRelativeToSearchDate(
                        endDate,
                        true,
                        true,
                        false,
                        visitDurationInMin,
                        startMinute,
                        endMinute,
                        searchDate));
    }

    @Test
    public void test_findCandidateVisits3() {

        int maxEndMinute = 150;
        int visitDuration = 30;

        int deltaMinutes = 50;
        Date startDate = new Date(10 * MILLISECS_PER_MIN);
        Date endDate = new Date((10 + deltaMinutes) * MILLISECS_PER_MIN);

        int startDateMinuteOfDay =
                startDate.getHours() * MINS_PER_HR + startDate.getMinutes();
        int endDateMinuteOfDay =
                endDate.getHours() * MINS_PER_HR + endDate.getMinutes();

        Date searchDate100 = new Date(100);
        Date searchDate101 = new Date(101);
        List<Date> searchDates =
                Lists.newArrayList(searchDate100, searchDate101);

        List<BookedVisit> candidateVisits =
                Lists.newArrayList();
        List<TemplateResource> fixedResources =
                Lists.newArrayList(new TemplateResource());
        Map<String, List<TemplateResource>> floatTrList =
                Maps.newHashMap();
        Map<String, List<TemplateResource>> floatResourceGroupList =
                Maps.newHashMap();
        Map<String, List<TemplateResource>> flexResourceGroupList =
                Maps.newHashMap();

        SubjectMrn subjectMrn = new SubjectMrn();

        VisitTemplate visitTemplate = new VisitTemplate();
        visitTemplate.setDuration(visitDuration);

        doReturn(true).when(searchAlgorithmServiceSpy)
                .isRelativeTimeTemplate(true, visitTemplate);

        doReturn(fixedResources).when(templateResourceDAO)
                .findFixedTemplateResourcesByVisit(visitTemplate);

        doReturn(floatTrList).when(searchAlgorithmServiceSpy)
                .retrieveFloatResourceList(visitTemplate);

        doReturn(maxEndMinute).when(searchAlgorithmServiceSpy)
                .findMaxEndMinuteRelativeToSearchDate(
                        endDate,
                        true,
                        true,
                        true,
                        visitDuration,
                        startDateMinuteOfDay,
                        endDateMinuteOfDay,
                        searchDate100
                );
        doReturn(maxEndMinute).when(searchAlgorithmServiceSpy)
                .findMaxEndMinuteRelativeToSearchDate(
                        endDate,
                        true,
                        true,
                        true,
                        visitDuration,
                        startDateMinuteOfDay,
                        endDateMinuteOfDay,
                        searchDate101
                );

        doNothing().when(searchAlgorithmServiceSpy)
                .calculateAvailableVisitTimeSlotRelative(
                        startDateMinuteOfDay,
                        maxEndMinute,
                        visitTemplate,
                        subjectMrn,
                        searchDate100,
                        candidateVisits,
                        fixedResources,
                        floatTrList,
                        floatResourceGroupList,
                        flexResourceGroupList,
                        true
                );
        doNothing().when(searchAlgorithmServiceSpy)
                .calculateAvailableVisitTimeSlotRelative(
                        startDateMinuteOfDay,
                        maxEndMinute,
                        visitTemplate,
                        subjectMrn,
                        searchDate101,
                        candidateVisits,
                        fixedResources,
                        floatTrList,
                        floatResourceGroupList,
                        flexResourceGroupList,
                        true
                );
        doNothing().when(searchAlgorithmServiceSpy)
                .calculateAvailableVisitTimeSlotClockTime(
                        visitTemplate,
                        subjectMrn,
                        searchDate100,
                        endDate,
                        candidateVisits,
                        fixedResources,
                        floatTrList,
                        floatResourceGroupList,
                        flexResourceGroupList,
                        true
                );
        doNothing().when(searchAlgorithmServiceSpy)
                .calculateAvailableVisitTimeSlotClockTime(
                        visitTemplate,
                        subjectMrn,
                        searchDate101,
                        endDate,
                        candidateVisits,
                        fixedResources,
                        floatTrList,
                        floatResourceGroupList,
                        flexResourceGroupList,
                        true
                );

        assertEquals(candidateVisits, searchAlgorithmServiceSpy
                .findCandidateVisits(
                        searchDates,
                        startDate,
                        endDate,
                        subjectMrn,
                        visitTemplate,
                        true,
                        true,
                        true));

        verify(searchAlgorithmServiceSpy, times(1))
                .findMaxEndMinuteRelativeToSearchDate(
                        endDate,
                        true,
                        true,
                        true,
                        visitDuration,
                        startDateMinuteOfDay,
                        endDateMinuteOfDay,
                        searchDate100
                );
        verify(searchAlgorithmServiceSpy, times(1))
                .findMaxEndMinuteRelativeToSearchDate(
                        endDate,
                        true,
                        true,
                        true,
                        visitDuration,
                        startDateMinuteOfDay,
                        endDateMinuteOfDay,
                        searchDate101
                );

        verify(searchAlgorithmServiceSpy, times(1))
                .calculateAvailableVisitTimeSlotRelative(
                        startDateMinuteOfDay,
                        maxEndMinute,
                        visitTemplate,
                        subjectMrn,
                        searchDate100,
                        candidateVisits,
                        fixedResources,
                        floatTrList,
                        floatResourceGroupList,
                        flexResourceGroupList,
                        true
                );
        verify(searchAlgorithmServiceSpy, times(1))
                .calculateAvailableVisitTimeSlotRelative(
                        startDateMinuteOfDay,
                        maxEndMinute,
                        visitTemplate,
                        subjectMrn,
                        searchDate101,
                        candidateVisits,
                        fixedResources,
                        floatTrList,
                        floatResourceGroupList,
                        flexResourceGroupList,
                        true
                );


        doReturn(false).when(searchAlgorithmServiceSpy)
                .isRelativeTimeTemplate(true, visitTemplate);

        searchAlgorithmServiceSpy
                .findCandidateVisits(
                        searchDates,
                        startDate,
                        endDate,
                        subjectMrn,
                        visitTemplate,
                        true,
                        true,
                        true);


        verify(searchAlgorithmServiceSpy, times(1))
                .calculateAvailableVisitTimeSlotClockTime(
                        visitTemplate,
                        subjectMrn,
                        searchDate100,
                        endDate,
                        candidateVisits,
                        fixedResources,
                        floatTrList,
                        floatResourceGroupList,
                        flexResourceGroupList,
                        true
                );
        verify(searchAlgorithmServiceSpy, times(1))
                .calculateAvailableVisitTimeSlotClockTime(
                        visitTemplate,
                        subjectMrn,
                        searchDate101,
                        endDate,
                        candidateVisits,
                        fixedResources,
                        floatTrList,
                        floatResourceGroupList,
                        flexResourceGroupList,
                        true
                );
    }

    @Test
    public void test_isStartPlusDurationLtEnd() {
        assertTrue(searchAlgorithmService.isStartPlusDurationLteEnd(100, 10, 0));
        assertTrue(searchAlgorithmService.isStartPlusDurationLteEnd(100, 10, 89));
        assertFalse(searchAlgorithmService.isStartPlusDurationLteEnd(100, 40, 80));
    }

    @Test
    public void test_initializeBookedVisit() {
        VisitType visitType = new VisitType();
        visitType.setId(200);

        Study study = new Study();
        study.setId(200);

        VisitTemplate visitTemplate = new VisitTemplate();
        visitTemplate.setId(200);
        visitTemplate.setName("vtName");
        visitTemplate.setStudy(study);
        visitTemplate.setVisitType(visitType);

        int slotStart = 50;
        int durationInMin = 100;
        Date searchDate = new Date(10000);

        Date startTime = new Date( 20000);
        Date endTime = new Date( 30000);

        doReturn(startTime).when(searchAlgorithmServiceSpy)
                .modifyDateFieldPlusAmtSetHourMinute(
                        searchDate,
                        Calendar.DAY_OF_YEAR,
                        0,
                        divideByMinsPerHour(slotStart),
                        moduloMinsPerHour(slotStart)
                );

        doReturn(endTime).when(searchAlgorithmServiceSpy)
                .modifyDateFieldPlusAmtSetHourMinute(
                        searchDate,
                        Calendar.DAY_OF_YEAR,
                        0,
                        divideByMinsPerHour(slotStart + durationInMin),
                        moduloMinsPerHour(slotStart + durationInMin)
                );

        BookedVisit result = searchAlgorithmServiceSpy.initializeBookedVisit(
                visitTemplate,
                slotStart,
                durationInMin,
                searchDate);

        assertEquals(Integer.valueOf(1234556), result.getId());
        assertEquals("vtName", result.getName());
        assertEquals(study, result.getStudy());
        assertEquals(visitTemplate, result.getVisitTemplate());
        assertEquals(visitType, result.getVisitType());

        assertEquals(startTime, result.getScheduledStartTime());
        assertEquals(endTime, result.getScheduledEndTime());
    }
    @Test
    public void test_calculateAvailableVisitTimeSlotRelative() {

        int visitDuration = 100;
        int startTimeInMin = 50;

        // allow room for exactly 2 possible start times
        int endTimeInMin = startTimeInMin + visitDuration + INCREMENT_FACTOR;

        Date searchDate = new Date(10000);

        SubjectMrn subjectMrn = new SubjectMrn();

        VisitTemplate visitTemplate = new VisitTemplate();
        visitTemplate.setDuration(visitDuration);

        BookedVisit bookedVisit = mock(BookedVisit.class);
        List<BookedVisit> candidateVisits = Lists.newArrayList();

        List<TemplateResource> fixedResources = Lists.newArrayList();
        Map<String, List<TemplateResource>> floatTrList = Maps.newHashMap();
        Map<String, List<TemplateResource>> floatResourceGroups = Maps.newHashMap();
        Map<String, List<TemplateResource>> flipResourceGroups = Maps.newHashMap();

        doReturn(bookedVisit).when(searchAlgorithmServiceSpy)
                .initializeBookedVisit(
                        visitTemplate,
                        startTimeInMin,
                        visitDuration,
                        searchDate
                );
        doReturn(bookedVisit).when(searchAlgorithmServiceSpy)
                .initializeBookedVisit(
                        visitTemplate,
                        startTimeInMin + INCREMENT_FACTOR,
                        visitDuration,
                        searchDate
                );

        doNothing().when(searchAlgorithmServiceSpy)
                .calculateSomeAvailableResources(
                        visitDuration,
                        searchDate,
                        candidateVisits,
                        fixedResources,
                        floatTrList,
                        floatResourceGroups,
                        flipResourceGroups,
                        startTimeInMin,
                        bookedVisit,
                        true
                );
        doNothing().when(searchAlgorithmServiceSpy)
                .calculateSomeAvailableResources(
                        visitDuration,
                        searchDate,
                        candidateVisits,
                        fixedResources,
                        floatTrList,
                        floatResourceGroups,
                        flipResourceGroups,
                        startTimeInMin + INCREMENT_FACTOR,
                        bookedVisit,
                        true
                );

        searchAlgorithmServiceSpy.calculateAvailableVisitTimeSlotRelative(
                startTimeInMin,
                endTimeInMin,
                visitTemplate,
                subjectMrn,
                searchDate,
                candidateVisits,
                fixedResources,
                floatTrList,
                floatResourceGroups,
                flipResourceGroups,
                true
        );

        verify(bookedVisit, times(2))
                .setSubjectMrn(subjectMrn);
    }
    @Test
    public void test_computeAndSetDuration() {

        long millisDelta = (MILLISECS_PER_MIN * MINS_PER_DAY) +
                (MILLISECS_PER_MIN * MINS_PER_HR) +
                (MILLISECS_PER_MIN);

        long startMillis = (MILLISECS_PER_MIN * MINS_PER_DAY) * 2;
        long endMillis = startMillis + millisDelta;

        int minutesDelta = (int)(millisDelta / MILLISECS_PER_MIN);

        Date startDate = new Date( startMillis);
        Date endDate = new Date( endMillis);

        TemplateResource templateResource = mock(TemplateResource.class);

        doReturn(startDate).when(templateResource).getStartDate();
        doReturn(endDate).when(templateResource).getEndDate();

        searchAlgorithmServiceSpy.computeAndSetDuration(templateResource);

        verify(templateResource, times(1))
                .setDuration(minutesDelta);

        doReturn(null).when(templateResource).getStartDate();

        searchAlgorithmServiceSpy.computeAndSetDuration(templateResource);

        // should still be (leftover from above) 1, vs 2
        verify(templateResource, times(1))
                .setDuration(minutesDelta);
    }
    @Test
    public void test_allocateTimeFixedResources() {

        long millisDelta = (MILLISECS_PER_MIN * MINS_PER_DAY) +
                (MILLISECS_PER_MIN * MINS_PER_HR) +
                (MILLISECS_PER_MIN);

        // compensate for GMT - 5
        long startMillis = (MILLISECS_PER_MIN * MINS_PER_HR) * 5;
        long endMillis = startMillis + millisDelta;

        Date startDate = new Date( startMillis);
        Date endDate = new Date( endMillis);

        TemplateResource templateResource = mock(TemplateResource.class);

        doReturn(30).when(templateResource).getDuration();

        doReturn(startDate).when(templateResource).getStartDate();
        doReturn(endDate).when(templateResource).getEndDate();

        Date searchDate = new Date(startMillis * 2);

        doReturn(startDate).when(searchAlgorithmServiceSpy).modifyDateFieldPlusAmtSetHourMinute(
                searchDate,
                Calendar.DAY_OF_YEAR,
                0,
                0,
                0
        );
        doReturn(endDate).when(searchAlgorithmServiceSpy).modifyDateFieldPlusAmtSetHourMinute(
                searchDate,
                Calendar.DAY_OF_YEAR,
                0,
                0,
                30
        );


        List<TemplateResource> fixedResources = Lists.newArrayList(templateResource);
        String groupType = "groupType";

        searchAlgorithmServiceSpy.allocateTimeFixedResources(
                0,
                searchDate,
                fixedResources,
                groupType);

        verify(templateResource, times(1))
                .setScheduledStartTime(startDate);
        verify(templateResource, times(1))
                .setScheduledEndTime(endDate);
        verify(templateResource, times(1))
                .setResourceGroupType(groupType);

    }
    @Test
    public void test_checkIfSublocationClosed() {

        Resource resource = new Resource();
        ResourceSublocation resourceSublocation = new ResourceSublocation();
        TemplateResource templateResource = mock(TemplateResource.class);

        doReturn(resource).when(templateResource).getResource();
        doReturn(null).when(templateResource).getScheduledStartTime();
        doReturn(null).when(templateResource).getScheduledEndTime();

        doReturn(resourceSublocation).when(studyDAO).findSublocationByResource(resource);

        doReturn(false).when(searchAlgorithmDAO).isSublocationClosed(
                null,
                null,
                null);

        boolean result = searchAlgorithmService.checkIfSublocationClosed(templateResource);

        assertFalse(result);

        verify(templateResource, times(0))
                .setAvailable("No");
        verify(templateResource, times(0))
                .setRejectedResourceMessage("Sub-Location Closure");

        doReturn(true).when(searchAlgorithmDAO).isSublocationClosed(
                null,
                null,
                null);

        result = searchAlgorithmService.checkIfSublocationClosed(templateResource);

        assertTrue(result);

        verify(templateResource, times(1))
                .setAvailable("No");
        verify(templateResource, times(1))
                .setRejectedResourceMessage("Sub-Location Closure");

    }
    @Test
    public void test_getDefaultAvailabilityDaysOfWeek() {

        LocalDateTime localDateTimeMonday = LocalDateTime.of(2017, Month.JULY, 17, 7, 0);
        LocalDateTime localDateTimeThursday = LocalDateTime.of(2017, Month.JULY, 20, 7, 0);

        Date startDate = DateUtility.toDate(localDateTimeMonday);
        Date endDate = DateUtility.toDate(localDateTimeThursday);

        // Monday thru Thursday
        int dateDifference = compareDateDifference(startDate, endDate);
        assertEquals(4, dateDifference);

        // expect 1 + #days between start and end inclusive. Mon - Tues ---> 1+2=3
        Set<Integer> result = searchAlgorithmService.getDefaultAvailabilityDaysOfWeek(startDate, endDate);
        assertEquals(dateDifference + 1, result.size());

        for (int i=Calendar.MONDAY; i <= Calendar.MONDAY + dateDifference; i++) {
            assertTrue(result.contains(Integer.valueOf(i)));
        }

        // same day Monday
        dateDifference = compareDateDifference(startDate, startDate);
        assertEquals(1, dateDifference);

        result = searchAlgorithmService.getDefaultAvailabilityDaysOfWeek(startDate, startDate);
        assertEquals(dateDifference + 1, result.size());

        for (int i=Calendar.MONDAY; i <= Calendar.MONDAY + dateDifference; i++) {
            assertTrue(result.contains(Integer.valueOf(i)));
        }
    }

    @Test
    public void test_getDayOfWeekString() {
        ResourceSchedule resourceSchedule = new ResourceSchedule();
        List<String> zeroBasedDays = Lists.newArrayList(
                Statics.SUNDAY,
                Statics.MONDAY,
                Statics.TUESDAY,
                Statics.WEDNESDAY,
                Statics.THURSDAY,
                Statics.FRIDAY,
                Statics.SATURDAY
        );

        for (int i=1; i<=7; i++) {
            resourceSchedule.setDayOfWeek(i);

            String dowString = searchAlgorithmService.getDayOfWeekString(resourceSchedule);
            assertEquals(zeroBasedDays.get(i-1), dowString);
        }
    }
    @Test
    public void test_populateDefaultSchedule() {
        ResourceSchedule resourceScheduleMon = new ResourceSchedule();
        resourceScheduleMon.setDayOfWeek(Calendar.MONDAY);

        ResourceSchedule resourceScheduleTues = new ResourceSchedule();
        resourceScheduleTues.setDayOfWeek(Calendar.TUESDAY);

        ResourceSchedule resourceSchedule2Tues = new ResourceSchedule();
        resourceSchedule2Tues.setDayOfWeek(Calendar.TUESDAY);

        List<ResourceSchedule> resourceScheduleList = Lists.newArrayList(
                resourceScheduleMon,
                resourceScheduleTues,
                resourceSchedule2Tues);

        Map<String, List<ResourceSchedule>> dayOfWeekSchedule = Maps.newHashMap();

        searchAlgorithmService.populateDefaultSchedule(resourceScheduleList, dayOfWeekSchedule);

        List<ResourceSchedule> mondaysRSList = dayOfWeekSchedule.get(Statics.MONDAY);
        List<ResourceSchedule> tuesdaysRSList = dayOfWeekSchedule.get(Statics.TUESDAY);

        assertEquals(1, mondaysRSList.size());
        assertTrue(mondaysRSList.contains(resourceScheduleMon));

        assertEquals(2, tuesdaysRSList.size());
        assertTrue(mondaysRSList.contains(resourceScheduleTues));
        assertTrue(mondaysRSList.contains(resourceSchedule2Tues));

        assertNull(dayOfWeekSchedule.get(Statics.FRIDAY));
    }
    @Test
    public void test_retrieveResourceDefaultSchedule() {

        Date startDate = DateUtility.toDate(LocalDateTime.of(2017, Month.JULY, 17, 7, 0));
        Date endDate = DateUtility.toDate(LocalDateTime.of(2017, Month.JULY, 20, 7, 0));
        Resource resource = new Resource();

        ResourceSchedule resourceScheduleMon = new ResourceSchedule();
        resourceScheduleMon.setDayOfWeek(Calendar.MONDAY);

        ResourceSchedule resourceScheduleTues = new ResourceSchedule();
        resourceScheduleTues.setDayOfWeek(Calendar.TUESDAY);

        ResourceSchedule resourceSchedule2Tues = new ResourceSchedule();
        resourceSchedule2Tues.setDayOfWeek(Calendar.TUESDAY);

        List<ResourceSchedule> resourceScheduleList = Lists.newArrayList(
                resourceScheduleMon,
                resourceScheduleTues,
                resourceSchedule2Tues);

        List<Integer> daysOfWeekIntegers = Lists.newArrayList(Sets.newHashSet(
                Calendar.MONDAY,
                Calendar.TUESDAY,
                Calendar.WEDNESDAY,
                Calendar.THURSDAY,
                Calendar.FRIDAY
        ));

        doReturn(resourceScheduleList).when(resourceDAO).findResourceScheduleByResource(
                resource,
                daysOfWeekIntegers,
                false);

        Map<String, List<ResourceSchedule>> schedule = Maps.newHashMap();

        doNothing().when(searchAlgorithmServiceSpy).populateDefaultSchedule(resourceScheduleList, schedule);

        Map<String, List<ResourceSchedule>> result =
                searchAlgorithmServiceSpy.retrieveResourceDefaultSchedule(
                        resource,
                        startDate,
                        endDate);

        assertEquals(schedule, result);

        verify(resourceDAO, times(1))
                .findResourceScheduleByResource(resource, daysOfWeekIntegers, false);
        verify(searchAlgorithmServiceSpy, times(1))
                .populateDefaultSchedule(resourceScheduleList, schedule);
    }
    @Test
    public void test_searchDatesMapping() {

        Date currDateMonday = DateUtility.toDate(LocalDateTime.of(2017, Month.JULY, 17, 7, 0));
        Date currDateFriday = DateUtility.toDate(LocalDateTime.of(2017, Month.JULY, 21, 7, 0));
        Date currDateTuesday = DateUtility.toDate(LocalDateTime.of(2017, Month.JULY, 18, 7, 0));

        Map<Date, String> searchDateToDowMap = Maps.newHashMap();
        Calendar calendar = Calendar.getInstance();

        searchAlgorithmService.searchDatesMapping(searchDateToDowMap, calendar, currDateMonday);

        assertEquals(1, searchDateToDowMap.size());
        assertEquals(Statics.MONDAY, searchDateToDowMap.get(currDateMonday));

        searchAlgorithmService.searchDatesMapping(searchDateToDowMap, calendar, currDateFriday);

        assertEquals(2, searchDateToDowMap.size());
        assertEquals(Statics.FRIDAY, searchDateToDowMap.get(currDateFriday));
        assertEquals(Statics.MONDAY, searchDateToDowMap.get(currDateMonday));
        assertEquals(null, searchDateToDowMap.get(currDateTuesday));
    }
    @Test
    public void test_buildSearchDates() {

        Date dateMonday = DateUtility.toDate(LocalDateTime.of(2017, Month.JULY, 17, 7, 0));
        Date dateThursday = DateUtility.toDate(LocalDateTime.of(2017, Month.JULY, 20, 7, 0));
        Date dateWednesday = DateUtility.toDate(LocalDateTime.of(2017, Month.JULY, 19, 7, 0));
        Date dateSunday = DateUtility.toDate(LocalDateTime.of(2017, Month.JULY, 23, 7, 0));

        dateMonday = searchAlgorithmService.modifyDateFieldPlusAmtSetHourMinute(dateMonday, Calendar.DAY_OF_YEAR, 0, 0, 0);
        dateThursday = searchAlgorithmService.modifyDateFieldPlusAmtSetHourMinute(dateThursday, Calendar.DAY_OF_YEAR, 0, 0, 0);
        dateWednesday = searchAlgorithmService.modifyDateFieldPlusAmtSetHourMinute(dateWednesday, Calendar.DAY_OF_YEAR, 0, 0, 0);
        dateSunday = searchAlgorithmService.modifyDateFieldPlusAmtSetHourMinute(dateSunday, Calendar.DAY_OF_YEAR, 0, 0, 0);

        Map<Date, String> resultMap =
                searchAlgorithmService.buildSearchDates(dateMonday, dateThursday);

        assertEquals(5, resultMap.size());
        assertEquals(Statics.MONDAY, resultMap.get(dateMonday));
        assertEquals(Statics.THURSDAY, resultMap.get(dateThursday));
        assertEquals(Statics.WEDNESDAY, resultMap.get(dateWednesday));
        assertEquals(null, resultMap.get(dateSunday));
    }

    @Test
    public void test_retrieveDaysOfWeek() {
        Date date1 = new Date(20);
        Date date2 = new Date(50);

        Map<Date, String> dateToDowMap =
                searchAlgorithmServiceSpy.retrieveDaysOfWeek(date1, date2);

        verify(searchAlgorithmServiceSpy, times(1))
                .buildSearchDates(date1, date2);
    }

    @Test
    public void test_loadRelevantDaysOfDefaultSchedule() {
        Date dateMonday = DateUtility.toDate(LocalDateTime.of(2017, Month.JULY, 17, 7, 0));
        Date dateTuesday = DateUtility.toDate(LocalDateTime.of(2017, Month.JULY, 18, 7, 0));

        dateMonday = searchAlgorithmServiceSpy.modifyDateFieldPlusAmtSetHourMinute(dateMonday, Calendar.DAY_OF_YEAR, 0, 0, 0);
        dateTuesday = searchAlgorithmServiceSpy.modifyDateFieldPlusAmtSetHourMinute(dateTuesday, Calendar.DAY_OF_YEAR, 0, 0, 0);

        ResourceSchedule resourceScheduleMon = new ResourceSchedule();
        resourceScheduleMon.setDayOfWeek(Calendar.MONDAY);

        ResourceSchedule resourceScheduleTues = new ResourceSchedule();
        resourceScheduleTues.setDayOfWeek(Calendar.TUESDAY);

        ResourceSchedule resourceSchedule2Tues = new ResourceSchedule();
        resourceSchedule2Tues.setDayOfWeek(Calendar.TUESDAY);

        List<ResourceSchedule> resourceScheduleListMon = Lists.newArrayList(
                resourceScheduleMon);

        List<ResourceSchedule> resourceScheduleListTues = Lists.newArrayList(
                resourceScheduleTues,
                resourceSchedule2Tues);

        Map<String, List<ResourceSchedule>> defaultMap = Maps.newHashMap();
        defaultMap.put(Statics.MONDAY, resourceScheduleListMon);
        defaultMap.put(Statics.TUESDAY, resourceScheduleListTues);

        TreeMap<Date, List<ResourceSchedule>> resultMap =
                searchAlgorithmServiceSpy.loadRelevantDaysOfDefaultSchedule(
                        dateMonday,
                        dateTuesday,
                        defaultMap);

        // method under test adds 1 day at end,
        //  so request for Mon-Tues also gives Weds
        // buildSearchDates() then adds 1 more day
        assertEquals(4, resultMap.size());
        assertEquals(resourceScheduleListMon, resultMap.get(dateMonday));
        assertEquals(resourceScheduleListTues, resultMap.get(dateTuesday));
    }

    @Test
    public void test_computeDate2PeriodRelativeToDate1() {

        Date dateMonday7am = DateUtility.toDate(LocalDateTime.of(2017, Month.JULY, 17, 7, 0));
        Date dateWednesday7am = DateUtility.toDate(LocalDateTime.of(2017, Month.JULY, 19, 7, 0));

        int relativePeriod = searchAlgorithmService.computeDate2PeriodRelativeToDate1(dateMonday7am, dateWednesday7am);

        // from midnight: 2 days of 96, 7 hours of 4
        assertEquals((DateUtility.PERIODS_PER_DAY * 2) + (7 * 4), relativePeriod);
    }

    @Test
    public void test_computeLastPeriod() {

        Date dateMonday7am = DateUtility.toDate(LocalDateTime.of(2017, Month.JULY, 17, 7, 0));
        Date dateWednesday8am = DateUtility.toDate(LocalDateTime.of(2017, Month.JULY, 19, 8, 0));

        int resultPeriod = searchAlgorithmService.computeLastPeriod(dateMonday7am, dateWednesday8am);

        assertEquals((7 * 4) + (DateUtility.PERIODS_PER_DAY * 2) + (4 - 1), resultPeriod);
    }

    @Test
    public void test_computePeriodOfDate() {

        Date dateMonday7ish = DateUtility.toDate(LocalDateTime.of(2017, Month.JULY, 17, 7, 3));
        int resultPeriod = searchAlgorithmService.computePeriodOfDate(dateMonday7ish);

        assertEquals((7 * 4), resultPeriod);
    }

    @Test
    public void test_computeMinutesFromHrsAndMins() {

        Date dateMonday7ish = DateUtility.toDate(LocalDateTime.of(2017, Month.JULY, 17, 7, 3));
        int minutes = searchAlgorithmService.computeMinutesFromHrsAndMins(dateMonday7ish);

        assertEquals((7 * 60) + 3, minutes);
    }

    @Test
    public void test_loadIntoPeriodToQuantityMap() {

        Map<Integer, Integer> periodToQuantityMap = Maps.newHashMap();
        periodToQuantityMap.put(5,5);
        periodToQuantityMap.put(7,7);

        searchAlgorithmService.loadIntoPeriodToQuantityMap(2,5,7, periodToQuantityMap);

        assertEquals(Integer.valueOf(7), periodToQuantityMap.get(5));
        assertEquals(Integer.valueOf(2), periodToQuantityMap.get(6));
        assertEquals(Integer.valueOf(9), periodToQuantityMap.get(7));
    }

    @Test
    public void test_loadPeriodToQuantityOverrideMap() {

        Date monday6am = DateUtility.toDate(LocalDateTime.of(2017, Month.JULY, 17, 6, 0));
        Date monday7am = DateUtility.toDate(LocalDateTime.of(2017, Month.JULY, 17, 7, 0));
        Date monday8am = DateUtility.toDate(LocalDateTime.of(2017, Month.JULY, 17, 8, 0));

        ResourceSchedule resourceSchedule6To7 = new ResourceSchedule();
        resourceSchedule6To7.setId(67);
        resourceSchedule6To7.setQuantity(1);
        resourceSchedule6To7.setStartTime(monday6am);
        resourceSchedule6To7.setEndTime(monday7am);

        ResourceSchedule resourceSchedule7To8 = new ResourceSchedule();
        resourceSchedule7To8.setId(78);
        resourceSchedule7To8.setQuantity(1);
        resourceSchedule7To8.setStartTime(monday7am);
        resourceSchedule7To8.setEndTime(monday8am);

        List<ResourceSchedule> resourceOverrideSchedule = Lists.newArrayList(
                resourceSchedule6To7, resourceSchedule7To8);

        TemplateResource requestTr = new TemplateResource();
        requestTr.setScheduledStartTime(monday6am);

        Map<Integer, Integer> resultMap = searchAlgorithmService.loadPeriodToQuantityOverrideMap(
                requestTr, Lists.newArrayList());

        assertEquals(0, resultMap.size());

        resultMap = searchAlgorithmService.loadPeriodToQuantityOverrideMap(
                requestTr, resourceOverrideSchedule);

        assertEquals(2*4, resultMap.size());
    }

    @Test
    public void test_loadResourceScheduleIntoPeriodToQuantityMap() {
        Date dateMonday7am = DateUtility.toDate(LocalDateTime.of(2017, Month.JULY, 17, 7, 0));
        Date dateMonday8am = DateUtility.toDate(LocalDateTime.of(2017, Month.JULY, 17, 8, 0));

        ResourceSchedule resourceSchedule = new ResourceSchedule();
        resourceSchedule.setStartTime(dateMonday7am);
        resourceSchedule.setEndTime(dateMonday8am);
        resourceSchedule.setQuantity(Integer.valueOf(5));

        Map<Integer, Integer> periodToQuantityMap = Maps.newHashMap();

        doNothing().when(searchAlgorithmServiceSpy).loadIntoPeriodToQuantityMap(
                5,
                28,
                31,
                periodToQuantityMap);

        searchAlgorithmServiceSpy.loadResourceScheduleIntoPeriodToQuantityMap(
                resourceSchedule,
                null,
                0);

        verify(searchAlgorithmServiceSpy, times(0))
                .computeLastPeriod(dateMonday7am, dateMonday8am);

        searchAlgorithmServiceSpy.loadResourceScheduleIntoPeriodToQuantityMap(
                resourceSchedule,
                periodToQuantityMap,
                0);

        verify(searchAlgorithmServiceSpy, times(1))
                .computeLastPeriod(dateMonday7am, dateMonday8am);

        verify(searchAlgorithmServiceSpy, times(1))
                .loadIntoPeriodToQuantityMap(
                        5,
                        28,
                        31,
                        periodToQuantityMap);

        searchAlgorithmServiceSpy.loadResourceScheduleIntoPeriodToQuantityMap(
                resourceSchedule,
                periodToQuantityMap,
                2);

        verify(searchAlgorithmServiceSpy, times(1))
                .loadIntoPeriodToQuantityMap(
                        5,
                        28 + (2 * DateUtility.PERIODS_PER_DAY),
                        31 + (2 * DateUtility.PERIODS_PER_DAY),
                        periodToQuantityMap);
    }

    @Test
    public void test_loadPeriodToQuantityDefaultScheduleMap() {

        Date dateMonday7am = DateUtility.toDate(LocalDateTime.of(2017, Month.JULY, 17, 7, 0));
        Date dateMonday8am = DateUtility.toDate(LocalDateTime.of(2017, Month.JULY, 17, 8, 0));
        Date dateMonday9am = DateUtility.toDate(LocalDateTime.of(2017, Month.JULY, 17, 9, 0));

        Date dateTuesday7am = DateUtility.toDate(LocalDateTime.of(2017, Month.JULY, 18, 7, 0));
        Date dateTuesday8am = DateUtility.toDate(LocalDateTime.of(2017, Month.JULY, 18, 8, 0));

        Date dateWednesday7am = DateUtility.toDate(LocalDateTime.of(2017, Month.JULY, 19, 7, 0));
        Date dateWednesday8am = DateUtility.toDate(LocalDateTime.of(2017, Month.JULY, 19, 8, 0));

        ResourceSchedule resourceScheduleMonday = new ResourceSchedule();
        resourceScheduleMonday.setQuantity(1);
        resourceScheduleMonday.setStartTime(dateMonday7am);
        resourceScheduleMonday.setEndTime(dateMonday8am);

        ResourceSchedule resourceScheduleMonday2 = new ResourceSchedule();
        resourceScheduleMonday2.setQuantity(1);
        resourceScheduleMonday2.setStartTime(dateMonday8am);
        resourceScheduleMonday2.setEndTime(dateMonday9am);

        List<ResourceSchedule> mondayList = Lists.newArrayList(resourceScheduleMonday, resourceScheduleMonday2);

        ResourceSchedule resourceScheduleTuesday = new ResourceSchedule();
        resourceScheduleTuesday.setQuantity(1);
        resourceScheduleTuesday.setStartTime(dateTuesday7am);
        resourceScheduleTuesday.setEndTime(dateTuesday8am);

        List<ResourceSchedule> tuesdayList = Lists.newArrayList(resourceScheduleTuesday);

        ResourceSchedule resourceScheduleWednesday = new ResourceSchedule();
        resourceScheduleWednesday.setQuantity(1);
        resourceScheduleWednesday.setStartTime(dateWednesday7am);
        resourceScheduleWednesday.setEndTime(dateWednesday8am);

        List<ResourceSchedule> wednesdayList = Lists.newArrayList(resourceScheduleWednesday);

        TreeMap<Date, List<ResourceSchedule>> defaultMap = new TreeMap<>();
        defaultMap.put(dateMonday7am, mondayList);
        defaultMap.put(dateTuesday7am, tuesdayList);
        defaultMap.put(dateWednesday7am, wednesdayList);

        Map<Integer, Integer> resultMap = searchAlgorithmService.loadPeriodToQuantityDefaultScheduleMap(defaultMap);

        int mondayFirstExpectedPeriod = 28;
        int mondayExpectedPeriods = 8;

        int tuesdayFirstExpectedPeriod = DateUtility.PERIODS_PER_DAY + 28;
        int tuesdayExpectedPeriods = 4;

        int wednesdayFirstExpectedPeriod = (2 * DateUtility.PERIODS_PER_DAY) + 28;
        int wednesdayExpectedPeriods = 4;

        for (int i=mondayFirstExpectedPeriod; i<mondayExpectedPeriods; i++) {
            assertEquals(Integer.valueOf(1), resultMap.get(i));
        }
        for (int i=tuesdayFirstExpectedPeriod; i<tuesdayExpectedPeriods; i++) {
            assertEquals(Integer.valueOf(1), resultMap.get(i));
        }
        for (int i=wednesdayFirstExpectedPeriod; i<wednesdayExpectedPeriods; i++) {
            assertEquals(Integer.valueOf(1), resultMap.get(i));
        }

        // other values null
        assertEquals(null, resultMap.get(10));

    }

    @Test
    public void test_updateDefaultMapWithOverrideMap() {

        Map<Integer, Integer> defaultMap = Maps.newHashMap();
        Map<Integer, Integer> overrideMap = Maps.newHashMap();

        defaultMap.put(1, 10);
        defaultMap.put(2, 20);
        defaultMap.put(3, 30);

        overrideMap.put(1, 100);
        overrideMap.put(2, 200);

        Map<Integer, Integer> resultMap = searchAlgorithmService.updateDefaultMapWithOverrideMap(
                overrideMap, defaultMap);

        assertEquals(Integer.valueOf(100), resultMap.get(1));
        assertEquals(Integer.valueOf(200), resultMap.get(2));
        assertEquals(Integer.valueOf(30), resultMap.get(3));

    }

    @Test
    public void test_isBookedResourceEqualToTemplateResourceId() {

        Resource resource1 = new Resource();
        resource1.setId(1);

        Resource resource1a = new Resource();
        resource1a.setId(1);

        Resource resource2 = new Resource();
        resource2.setId(2);

        assertTrue(searchAlgorithmService.isBookedResourceEqualToTemplateResourceId(
                resource1, resource1a));
        assertFalse(searchAlgorithmService.isBookedResourceEqualToTemplateResourceId(
                resource1, resource2));

    }

    @Test
    public void test_ifDatesOverlap() {

        Date dateMonday7am = DateUtility.toDate(LocalDateTime.of(2017, Month.JULY, 17, 7, 0));
        Date dateMonday8am = DateUtility.toDate(LocalDateTime.of(2017, Month.JULY, 17, 8, 0));
        Date dateMonday9am = DateUtility.toDate(LocalDateTime.of(2017, Month.JULY, 17, 9, 0));

        Date dateTuesday7am = DateUtility.toDate(LocalDateTime.of(2017, Month.JULY, 18, 7, 0));
        Date dateTuesday8am = DateUtility.toDate(LocalDateTime.of(2017, Month.JULY, 18, 8, 0));

        assertFalse(searchAlgorithmService.ifDatesOverlap(dateMonday7am, dateMonday8am, dateMonday8am, dateMonday9am));
        assertFalse(searchAlgorithmService.ifDatesOverlap(dateMonday7am, dateMonday8am, dateTuesday7am, dateTuesday8am));

        assertFalse(searchAlgorithmService.ifDatesOverlap(dateMonday7am, dateMonday8am,
                null, dateTuesday8am));
        assertFalse(searchAlgorithmService.ifDatesOverlap(dateMonday7am, dateMonday8am,
                dateTuesday7am, null));
        try {
            searchAlgorithmService.ifDatesOverlap(null, dateMonday8am,
                    dateTuesday7am, dateTuesday8am);
            assertFalse(true); // shouldn't reach here
        }
        catch (NullPointerException npe) {
            // happy
        }

        assertTrue(searchAlgorithmService.ifDatesOverlap(dateMonday7am, dateMonday8am, dateMonday7am, dateMonday8am));
        assertTrue(searchAlgorithmService.ifDatesOverlap(dateMonday9am, dateTuesday7am, dateMonday8am, dateTuesday8am));
    }

    private Map<Integer, Integer> candidateMapSpy(int periods, int quantity) {

        Map<Integer, Integer> candidateMap = Maps.newHashMap();
        for (int i=0; i<periods; i++) {
            candidateMap.put(i, quantity);
        }
        return spy(candidateMap);
    }
    @Test
    public void test_adjustMapsForBookedResources1() {

        Date monday0am = DateUtility.toDate(LocalDateTime.of(2017, Month.JULY, 17, 0, 0));
        Date monday7am = DateUtility.toDate(LocalDateTime.of(2017, Month.JULY, 17, 7, 0));
        Date tuesday10am = DateUtility.toDate(LocalDateTime.of(2017, Month.JULY, 18, 10, 0));

        // let existing, 'candidate' map go from mon7am - tues7am
        Date candidateStartDate = monday7am;
        int currentQuantity = 5;
        Map<Integer, Integer> candidateMapSpy = candidateMapSpy(PERIODS_PER_DAY, currentQuantity);

        searchAlgorithmService.adjustMapsForBookedResources(candidateMapSpy,
                monday0am, tuesday10am, candidateStartDate);

        verify(candidateMapSpy, times(PERIODS_PER_DAY + (10*4)))
                .get(anyInt());
        verify(candidateMapSpy, times(PERIODS_PER_DAY)).put(anyInt(), anyInt());
    }
    @Test
    public void test_adjustMapsForBookedResources2() {

        Date monday7am = DateUtility.toDate(LocalDateTime.of(2017, Month.JULY, 17, 7, 0));
        Date tuesday7am = DateUtility.toDate(LocalDateTime.of(2017, Month.JULY, 18, 7, 0));
        Date tuesday8am = DateUtility.toDate(LocalDateTime.of(2017, Month.JULY, 18, 8, 0));

        // let existing, 'candidate' map go from mon7am - tues7am
        Date candidateStartDate = monday7am;
        int currentQuantity = 5;
        Map<Integer, Integer> candidateMapSpy = candidateMapSpy(PERIODS_PER_DAY*2, currentQuantity);

        searchAlgorithmService.adjustMapsForBookedResources(candidateMapSpy,
                tuesday7am, tuesday8am, candidateStartDate);

        verify(candidateMapSpy, times(4))
                .get(anyInt());
        verify(candidateMapSpy, times(4)).put(anyInt(), anyInt());
    }

    @Test
    public void test_adjustMapsForBookedResource() {

        Date monday6am = DateUtility.toDate(LocalDateTime.of(2017, Month.JULY, 17, 6, 0));
        Date monday7am = DateUtility.toDate(LocalDateTime.of(2017, Month.JULY, 17, 7, 0));
        Date monday8am = DateUtility.toDate(LocalDateTime.of(2017, Month.JULY, 17, 8, 0));
        Date tuesday6am = DateUtility.toDate(LocalDateTime.of(2017, Month.JULY, 18, 6, 0));

        Resource resource1 = new Resource();
        resource1.setId(1);

        Resource resource2 = new Resource();
        resource2.setId(2);

        TemplateResource candidateResource = new TemplateResource();
        candidateResource.setResource(resource1);
        candidateResource.setScheduledStartTime(monday8am);
        candidateResource.setScheduledEndTime(tuesday6am);

        BookedResource slotSameIdOverlap = new BookedResource();
        slotSameIdOverlap.setResource(resource1);
        slotSameIdOverlap.setScheduledStartTime(monday7am);
        slotSameIdOverlap.setScheduledEndTime(tuesday6am);

        BookedResource slotSameIdNoOverlap = new BookedResource();
        slotSameIdNoOverlap.setResource(resource1);
        slotSameIdNoOverlap.setScheduledStartTime(monday6am);
        slotSameIdNoOverlap.setScheduledEndTime(monday7am);

        BookedResource slotDifferentResource = new BookedResource();
        slotDifferentResource.setResource(resource2);

        List<BookedResource> availableSlots = Lists.newArrayList(
                slotDifferentResource,
                slotSameIdNoOverlap,
                slotSameIdOverlap);

        Map<Integer, Integer> candidateMap = Maps.newHashMap();
        searchAlgorithmServiceSpy.adjustMapsForBookedResource(
                candidateResource,
                candidateMap,
                availableSlots);

        verify(searchAlgorithmServiceSpy, times(3))
                .isBookedResourceEqualToTemplateResourceId(any(Resource.class),any(Resource.class));
        verify(searchAlgorithmServiceSpy, times(2))
                .ifDatesOverlap(any(Date.class),any(Date.class),any(Date.class),any(Date.class));
        verify(searchAlgorithmServiceSpy, times(1))
                .adjustMapsForBookedResources(candidateMap, monday7am, tuesday6am, monday8am);
    }

    @Test
    public void test_adjustMapsForProvisionalAllocation() {
        Map<Integer, Integer> map = candidateMapSpy(5, 5);
        searchAlgorithmService.adjustMapsForProvisionalAllocation(3, map);

        assertEquals(Integer.valueOf(4), map.get(3));
    }

    @Test
    public void test_setMessageIfUnavailable() {
        int defaultQuantity = 5;
        int overrideQuantity = 15;

        Map<Integer, Integer> defaultMap = candidateMapSpy(14, defaultQuantity);
        Map<Integer, Integer> overrideMap = candidateMapSpy(4, overrideQuantity);

        TemplateResource candidateTr = new TemplateResource();

        boolean available = searchAlgorithmService.setMessageIfUnavailable(
                3,
                null,
                candidateTr,
                overrideMap,
                defaultMap,
                true
                );

        assertFalse(available);
        assertEquals("No Default Availability Entry Found", candidateTr.getRejectedResourceMessage());

        available = searchAlgorithmService.setMessageIfUnavailable(
                3,
                0,
                candidateTr,
                overrideMap,
                defaultMap,
                true
        );

        assertFalse(available);
        assertEquals("Temporary Adjustment - Quantity (" + overrideQuantity + ") Exhausted", candidateTr.getRejectedResourceMessage());

        available = searchAlgorithmService.setMessageIfUnavailable(
                13,
                0,
                candidateTr,
                overrideMap,
                defaultMap,
                true
        );

        assertFalse(available);
        assertEquals("Default Availability - Quantity (" + defaultQuantity + ") Exhausted", candidateTr.getRejectedResourceMessage());

        candidateTr.setRejectedResourceMessage(null);
        available = searchAlgorithmService.setMessageIfUnavailable(
                13,
                1,
                candidateTr,
                overrideMap,
                defaultMap,
                true
        );

        assertTrue(available);
        assertNull(candidateTr.getRejectedResourceMessage());
    }

    @Test
    public void test_isResourceTimeSlotAvailable1() {

        Date monday6am = DateUtility.toDate(LocalDateTime.of(2017, Month.JULY, 17, 6, 0));
        Date monday7am = DateUtility.toDate(LocalDateTime.of(2017, Month.JULY, 17, 7, 0));

        Date monday8am = DateUtility.toDate(LocalDateTime.of(2017, Month.JULY, 17, 8, 0));
        Date tuesday7am = DateUtility.toDate(LocalDateTime.of(2017, Month.JULY, 18, 7, 0));

        List<BookedResource> reserveList = Lists.newArrayList(new BookedResource());
        Map<Integer, Integer> qtyMap = Maps.newHashMap();
        Map<Integer, Integer> overrideMap = Maps.newHashMap();
        Map<Integer, Integer> defaultMap = Maps.newHashMap();
        List<BookedResource> availableBookedRs = Lists.newArrayList();

        TemplateResource candidateTr = new TemplateResource();
        candidateTr.setId(1);
        candidateTr.setScheduledStartTime(monday8am);
        candidateTr.setScheduledEndTime(tuesday7am);

        List<TemplateResource> availableTrs = Lists.newArrayList();

        doNothing().when(searchAlgorithmServiceSpy).adjustMapsForBookedResource(
                candidateTr,
                qtyMap,
                reserveList);
        doNothing().when(searchAlgorithmServiceSpy).adjustMapsForBookedResource(
                candidateTr,
                qtyMap,
                availableBookedRs);

        doReturn(0).when(searchAlgorithmServiceSpy).computePeriodOfDate(any(Date.class));
        doReturn(1).when(searchAlgorithmServiceSpy).computeLastPeriod(any(Date.class), any(Date.class));
        doReturn(false).when(searchAlgorithmServiceSpy)
                .setMessageIfUnavailable(
                        anyInt(),
                        any(Integer.class),
                        any(TemplateResource.class),
                        any(Map.class),
                        any(Map.class),
                        anyBoolean());

        searchAlgorithmServiceSpy.isResourceTimeSlotAvailable(
                candidateTr,
                reserveList,
                qtyMap,
                overrideMap,
                defaultMap,
                null,
                availableTrs);

        verify(searchAlgorithmServiceSpy, times(1))
                .adjustMapsForBookedResource(
                        candidateTr,
                        qtyMap,
                        reserveList);
        verify(searchAlgorithmServiceSpy, times(0))
                .adjustMapsForBookedResource(
                        candidateTr,
                        qtyMap,
                        availableBookedRs);
        verify(searchAlgorithmServiceSpy, times(0))
                .isBookedResourceEqualToTemplateResourceId(
                        any(Resource.class),
                        any(Resource.class));
        verify(searchAlgorithmServiceSpy, times(0))
                .ifDatesOverlap(
                        any(Date.class),
                        any(Date.class),
                        any(Date.class),
                        any(Date.class));
        verify(searchAlgorithmServiceSpy, times(1))
                .setMessageIfUnavailable(
                        anyInt(),
                        any(Integer.class),
                        any(TemplateResource.class),
                        any(Map.class),
                        any(Map.class),
                        anyBoolean());

        Resource resource1 = new Resource();
        resource1.setId(1);
        Resource resource2 = new Resource();
        resource2.setId(2);

        candidateTr.setResource(resource1);

        TemplateResource availableTr1Overlap = new TemplateResource();
        availableTr1Overlap.setResource(resource1);
        availableTr1Overlap.setScheduledStartTime(monday8am);
        availableTr1Overlap.setScheduledEndTime(tuesday7am);

        TemplateResource availableTr1NoOverlap = new TemplateResource();
        availableTr1NoOverlap.setResource(resource1);
        availableTr1NoOverlap.setScheduledStartTime(monday6am);
        availableTr1NoOverlap.setScheduledEndTime(monday7am);

        TemplateResource availableTr2 = new TemplateResource();
        availableTr2.setResource(resource2);
        availableTr2.setScheduledStartTime(monday6am);
        availableTr2.setScheduledEndTime(monday7am);

        availableTrs = Lists.newArrayList(
                availableTr1NoOverlap,
                availableTr1Overlap,
                availableTr2);

        doReturn(true).when(searchAlgorithmServiceSpy)
                .setMessageIfUnavailable(
                        anyInt(),
                        any(Integer.class),
                        any(TemplateResource.class),
                        any(Map.class),
                        any(Map.class),
                        anyBoolean());

        boolean result = searchAlgorithmServiceSpy.isResourceTimeSlotAvailable(
                candidateTr,
                reserveList,
                qtyMap,
                overrideMap,
                defaultMap,
                availableBookedRs,
                availableTrs);

        assertTrue(result);

        verify(searchAlgorithmServiceSpy, times(1))
                .adjustMapsForBookedResource(
                        candidateTr,
                        qtyMap,
                        availableBookedRs);
        verify(searchAlgorithmServiceSpy, times(3))
                .isBookedResourceEqualToTemplateResourceId(
                        any(Resource.class),
                        any(Resource.class));
        verify(searchAlgorithmServiceSpy, times(2))
                .ifDatesOverlap(
                        any(Date.class),
                        any(Date.class),
                        any(Date.class),
                        any(Date.class));
        verify(searchAlgorithmServiceSpy, times(2))
                .adjustMapsForProvisionalAllocation(
                        anyInt(),
                        any(Map.class));
    }

    @Test
    public void test_checkResourceAvailability() {
        TemplateResource requestResource = new TemplateResource();
        requestResource.setId(1);
        requestResource.setAvailable("maybe");

        List<BookedResource> reservedResourceTimeList = Lists.newArrayList(new BookedResource());

        Map<Integer, Integer> overrideMap = Maps.newHashMap();
        overrideMap.put(1,1);
        Map<Integer, Integer> defaultMap = Maps.newHashMap();
        defaultMap.put(1,2);
        Map<Integer, Integer> treeMap = Maps.newHashMap();

        List<BookedResource> availableBookedResourceSLots = Lists.newArrayList(new BookedResource());
        List<TemplateResource> availableResourceSlots = Lists.newArrayList();

        doReturn(true).when(searchAlgorithmServiceSpy).isResourceTimeSlotAvailable(
                any(TemplateResource.class),
                any(List.class),
                any(Map.class),
                any(Map.class),
                any(Map.class),
                any(List.class),
                any(List.class));

        boolean result = searchAlgorithmServiceSpy.checkResourceAvailability(
                requestResource,
                reservedResourceTimeList,
                treeMap,
                overrideMap,
                defaultMap,
                availableBookedResourceSLots,
                availableResourceSlots);

        assertTrue(result);
        assertEquals("maybe", requestResource.getAvailable());

        doReturn(false).when(searchAlgorithmServiceSpy).isResourceTimeSlotAvailable(
                any(TemplateResource.class),
                any(List.class),
                any(Map.class),
                any(Map.class),
                any(Map.class),
                any(List.class),
                any(List.class));

        result = searchAlgorithmServiceSpy.checkResourceAvailability(
                requestResource,
                reservedResourceTimeList,
                treeMap,
                overrideMap,
                defaultMap,
                availableBookedResourceSLots,
                availableResourceSlots);

        assertFalse(result);
        assertEquals("No", requestResource.getAvailable());
    }


    @Test
    public void test_checkAvailability() {
        TemplateResource requestResource = new TemplateResource();
        requestResource.setId(1);

        List<BookedResource> availableBookedResourceSlots = Lists.newArrayList(new BookedResource());
        List<TemplateResource> availableResourceSlots = Lists.newArrayList(new TemplateResource());

        List<BookedResource> reservedResourceTimeList = Lists.newArrayList();

        Map<String, List<ResourceSchedule>> resourceDefaultSchedule = Maps.newHashMap();
        TreeMap<Date, List<ResourceSchedule>> defaultScheduleMap = Maps.newTreeMap();
        List<ResourceSchedule> resourceOverrideSchedule = Lists.newArrayList();
        Map<Integer, Integer> periodToQuantityOverrideMap = Maps.newHashMap();
        Map<Integer, Integer> periodToQuantityDefaultAvailableMap = Maps.newHashMap();
        Map<Integer, Integer> finalScheduleTreeMap = Maps.newHashMap();

        // make maps distinct for mockito
        periodToQuantityOverrideMap.put(1,1);
        periodToQuantityDefaultAvailableMap.put(1,2);
        finalScheduleTreeMap.put(1,3);

        doReturn(false).when(searchAlgorithmServiceSpy).checkIfSublocationClosed(requestResource);

        doReturn(reservedResourceTimeList).when(searchAlgorithmDAO).findOverbookConflictResourcesByVisitStatus(
                any(Resource.class),
                any(Date.class),
                any(Date.class));

        doReturn(resourceDefaultSchedule).when(searchAlgorithmServiceSpy).retrieveResourceDefaultSchedule(
                any(Resource.class),
                any(Date.class),
                any(Date.class));

        doReturn(defaultScheduleMap).when(searchAlgorithmServiceSpy).loadRelevantDaysOfDefaultSchedule(
                any(Date.class),
                any(Date.class),
                any(Map.class));

        doReturn(resourceOverrideSchedule).when(resourceDAO).findTemporaryAdjustmentsByResource(
                any(Resource.class),
                any(Date.class),
                any(Date.class),
                anyBoolean());

        doReturn(periodToQuantityOverrideMap).when(searchAlgorithmServiceSpy).loadPeriodToQuantityOverrideMap(
                any(TemplateResource.class),
                any(List.class));

        doReturn(periodToQuantityDefaultAvailableMap).when(searchAlgorithmServiceSpy).loadPeriodToQuantityDefaultScheduleMap(
                any(TreeMap.class));

        doReturn(finalScheduleTreeMap).when(searchAlgorithmServiceSpy).updateDefaultMapWithOverrideMap(
                any(Map.class),
                any(Map.class));

        doReturn(true).when(searchAlgorithmServiceSpy).checkResourceAvailability(
                requestResource,
                reservedResourceTimeList,
                periodToQuantityOverrideMap,
                periodToQuantityDefaultAvailableMap,
                finalScheduleTreeMap,
                availableBookedResourceSlots,
                availableResourceSlots);

        boolean result = searchAlgorithmServiceSpy.checkAvailability(
                requestResource,
                availableBookedResourceSlots,
                availableResourceSlots);

        assertTrue(result);
        assertEquals("Yes", requestResource.getAvailable());
        assertEquals("", requestResource.getRejectedResourceMessage());

        verify(searchAlgorithmServiceSpy, times(1)).checkIfSublocationClosed(requestResource);

        verify(searchAlgorithmDAO, times(1)).findOverbookConflictResourcesByVisitStatus(
                any(Resource.class),
                any(Date.class),
                any(Date.class));

        verify(searchAlgorithmServiceSpy, times(1)).retrieveResourceDefaultSchedule(
                any(Resource.class),
                any(Date.class),
                any(Date.class));

        verify(searchAlgorithmServiceSpy, times(1)).loadRelevantDaysOfDefaultSchedule(
                any(Date.class),
                any(Date.class),
                any(Map.class));

        verify(resourceDAO, times(1)).findTemporaryAdjustmentsByResource(
                any(Resource.class),
                any(Date.class),
                any(Date.class),
                anyBoolean());

        verify(searchAlgorithmServiceSpy, times(1)).loadPeriodToQuantityOverrideMap(
                any(TemplateResource.class),
                any(List.class));

        verify(searchAlgorithmServiceSpy, times(1)).loadPeriodToQuantityDefaultScheduleMap(
                any(TreeMap.class));

        verify(searchAlgorithmServiceSpy, times(1)).updateDefaultMapWithOverrideMap(
                any(Map.class),
                any(Map.class));

        verify(searchAlgorithmServiceSpy, times(1)).checkResourceAvailability(
                requestResource,
                reservedResourceTimeList,
                periodToQuantityOverrideMap,
                periodToQuantityDefaultAvailableMap,
                finalScheduleTreeMap,
                availableBookedResourceSlots,
                availableResourceSlots);

        doReturn(true).when(searchAlgorithmServiceSpy).checkIfSublocationClosed(requestResource);

        result = searchAlgorithmServiceSpy.checkAvailability(
                requestResource,
                availableBookedResourceSlots,
                availableResourceSlots);

        assertFalse(result);
    }

    @Test
    public void test_getResourceAlternates() {

        Resource resource = new Resource();
        TemplateResource templateResource = new TemplateResource();
        templateResource.setResource(resource);

        List<ResourceAlternate> resourceAlternateList = Lists.newArrayList(new ResourceAlternate());

        doReturn(resourceAlternateList).when(resourceDAO).findResourceAlternates(resource);

        List<ResourceAlternate> resultList = searchAlgorithmService.getResourceAlternates(templateResource);

        assertNotEquals(Lists.newArrayList(), resultList);
        assertEquals(resourceAlternateList, resultList);
    }

    @Test
    public void test_noGroupId() {

        TemplateResource tr = new TemplateResource();

        assertTrue(searchAlgorithmService.noGroupId(tr));

        tr.setGroupId("");

        assertFalse(searchAlgorithmService.noGroupId(tr));

    }

    void help_isFixedResource(TemplateResource tr,
                              boolean isFloat,
                              String id,
                              boolean isFlex) {

        tr.setGroupId(id);
        tr.setFloatable(isFloat);
        tr.setFlexible(isFlex);
    }
    @Test
    public void test_isFixedResource() {

        TemplateResource templateResource = new TemplateResource();

        help_isFixedResource(templateResource,
                false, null, false);

        assertTrue(searchAlgorithmService.isFixedResource(templateResource));

        help_isFixedResource(templateResource,
                true, null, false);

        assertFalse(searchAlgorithmService.isFixedResource(templateResource));

        help_isFixedResource(templateResource,
                false, "group", false);

        assertFalse(searchAlgorithmService.isFixedResource(templateResource));

        help_isFixedResource(templateResource,
                false, null, true);

        assertFalse(searchAlgorithmService.isFixedResource(templateResource));
    }


    @Test
    public void test_checkIfReturnResource() {

        assertFalse(searchAlgorithmService.checkIfReturnResource(
                null,
                false,
                false,
                false));

        assertTrue(searchAlgorithmService.checkIfReturnResource(
                null,
                false,
                true,
                false));

        TemplateResource templateResource = new TemplateResource();
        templateResource.setAlternate(false);
        assertTrue(searchAlgorithmService.checkIfReturnResource(
                templateResource,
                true,
                false,
                false));

        doReturn(true).when(searchAlgorithmServiceSpy).isFixedResource(templateResource);
        templateResource.setAlternate(true);
        assertTrue(searchAlgorithmServiceSpy.checkIfReturnResource(
                templateResource,
                true,
                false,
                true));
    }

    @Test
    public void test_hasGroupId() {

        TemplateResource tr = new TemplateResource();

        tr.setGroupId(null);
        assertFalse(searchAlgorithmService.hasGroupId(tr));

        tr.setGroupId("gid");
        assertTrue(searchAlgorithmService.hasGroupId(tr));

    }

    @Test
    public void test_isNotFixedResource() {

        TemplateResource tr = new TemplateResource();

        tr.setFloatable(true);
        tr.setGroupId(null);

        assertTrue(searchAlgorithmService.isNotFixedResource(tr));

        tr.setFloatable(false);
        tr.setGroupId("gid");
        tr.setFlexible(true);

        assertTrue(searchAlgorithmService.isNotFixedResource(tr));

        tr.setFloatable(false);
        tr.setGroupId("gid");
        tr.setFlexible(false);

        assertFalse(searchAlgorithmService.isNotFixedResource(tr));

        tr.setFloatable(false);
        tr.setGroupId(null);
        tr.setFlexible(true);

        assertFalse(searchAlgorithmService.isNotFixedResource(tr));
    }

    @Test
    public void test_checkIfReturnNull() {
        TemplateResource tr = new TemplateResource();

        tr.setAlternate(false);
        assertTrue(searchAlgorithmService.checkIfReturnNull(
                tr,
                false,
                false));

        tr.setAlternate(true);
        assertTrue(searchAlgorithmService.checkIfReturnNull(
                tr,
                false,
                true));

        doReturn(true).when(searchAlgorithmServiceSpy)
                .isNotFixedResource(tr);
        assertTrue(searchAlgorithmServiceSpy.checkIfReturnNull(
                tr,
                true,
                true));

        doReturn(true).when(searchAlgorithmServiceSpy)
                .isNotFixedResource(tr);
        assertFalse(searchAlgorithmServiceSpy.checkIfReturnNull(
                tr,
                false,
                false));
    }

    @Test
    public void test_createTempResourceSlot() {

        Resource resource1 = new Resource();
        Resource resource2 = new Resource();

        TemplateResource tr = new TemplateResource();
        tr.setResource(resource1);
        tr.setId(1);
        tr.setDuration(25);

        TemplateResource tr2 = searchAlgorithmService.createTempResourceSlot(
                resource2,
                tr);

        assertEquals(resource2, tr2.getResource());
        assertEquals(Integer.valueOf(1), tr2.getId());
        assertEquals(Integer.valueOf(25), tr2.getDuration());
    }

    @Test
    public void test_checkForStandardAlternate() {

        Resource resource = new Resource();
        TemplateResource tr = new TemplateResource();
        TemplateResource altTr = new TemplateResource();

        ResourceAlternate resourceAlternate = new ResourceAlternate();
        resourceAlternate.setAlternateResource(resource);

        List<BookedResource> bookedResourceList = Lists.newArrayList();
        List<TemplateResource> templateResourceList = Lists.newArrayList();
        List<ResourceAlternate> resourceAlternateList = Lists.newArrayList(resourceAlternate);

        doReturn(altTr).when(searchAlgorithmServiceSpy)
                .createTempResourceSlot(resource, tr);

        doReturn(true).when(searchAlgorithmServiceSpy)
                .checkAvailability(altTr, bookedResourceList, templateResourceList);

        TemplateResource resultTr = searchAlgorithmServiceSpy.checkForStandardAlternate(
                resourceAlternateList,
                tr,
                bookedResourceList,
                templateResourceList);

        assertEquals(altTr, resultTr);

        doReturn(false).when(searchAlgorithmServiceSpy)
                .checkAvailability(altTr, bookedResourceList, templateResourceList);

        resultTr = searchAlgorithmServiceSpy.checkForStandardAlternate(
                resourceAlternateList,
                tr,
                bookedResourceList,
                templateResourceList);

        assertEquals(null, resultTr);
    }

    @Test
    public void test_findAvailableResource() {

        Resource resource = new Resource();
        TemplateResource tr = new TemplateResource();
        TemplateResource altTr = new TemplateResource();

        ResourceAlternate resourceAlternate = new ResourceAlternate();
        resourceAlternate.setAlternateResource(resource);

        List<BookedResource> bookedResourceList = Lists.newArrayList();
        List<TemplateResource> templateResourceList = Lists.newArrayList();
        List<ResourceAlternate> resourceAlternateList = Lists.newArrayList();

        doReturn(true).when(searchAlgorithmServiceSpy)
                .checkAvailability(tr,
                        bookedResourceList,
                        templateResourceList);
        doReturn(resourceAlternateList).when(searchAlgorithmServiceSpy)
                .getResourceAlternates(tr);
        doReturn(true).when(searchAlgorithmServiceSpy)
                .checkIfReturnResource(tr,
                        true,
                        true,
                        true);

        TemplateResource resultTr = searchAlgorithmServiceSpy.findAvailableResource(
                tr,
                true,
                bookedResourceList,
                templateResourceList);

        assertEquals(tr, resultTr);

        doReturn(false).when(searchAlgorithmServiceSpy)
                .checkIfReturnResource(tr,
                        true,
                        true,
                        true);
        doReturn(true).when(searchAlgorithmServiceSpy)
                .checkIfReturnNull(tr,
                        true,
                        true);

        resultTr = searchAlgorithmServiceSpy.findAvailableResource(
                tr,
                true,
                bookedResourceList,
                templateResourceList);

        assertEquals(null, resultTr);

        doReturn(false).when(searchAlgorithmServiceSpy)
                .checkIfReturnNull(tr,
                        true,
                        true);
        altTr.setAlternateResourceUsed(true);
        doReturn(altTr).when(searchAlgorithmServiceSpy)
                .checkForStandardAlternate(resourceAlternateList,
                        tr,
                        bookedResourceList,
                        templateResourceList);

        resultTr = searchAlgorithmServiceSpy.findAvailableResource(
                tr,
                true,
                bookedResourceList,
                templateResourceList);

        assertEquals(altTr, resultTr);

        doReturn(null).when(searchAlgorithmServiceSpy)
                .checkForStandardAlternate(resourceAlternateList,
                        tr,
                        bookedResourceList,
                        templateResourceList);

        doReturn(false).when(searchAlgorithmServiceSpy)
                .isNotFixedResource(tr);

        resultTr = searchAlgorithmServiceSpy.findAvailableResource(
                tr,
                true,
                bookedResourceList,
                templateResourceList);

        assertEquals(tr, resultTr);

        doReturn(true).when(searchAlgorithmServiceSpy)
                .isNotFixedResource(tr);

        resultTr = searchAlgorithmServiceSpy.findAvailableResource(
                tr,
                true,
                bookedResourceList,
                templateResourceList);

        assertEquals(null, resultTr);
    }

    @Test
    public void test_calculateFixedResourceAvailability() {
        TemplateResource tr1 = new TemplateResource();

        tr1.setId(1);

        List<BookedResource> bookedResourceList = Lists.newArrayList();
        List<TemplateResource> templateResourceList = Lists.newArrayList();

        List<TemplateResource> fixedTrs = Lists.newArrayList(tr1);

        doReturn(tr1).when(searchAlgorithmServiceSpy).findAvailableResource(
                tr1,
                true,
                bookedResourceList,
                templateResourceList);

        List<TemplateResource> resultTrs = searchAlgorithmServiceSpy
                .calculateFixedResourceAvailability(
                        fixedTrs,
                        true,
                        bookedResourceList);

        assertEquals(fixedTrs, resultTrs);


        doReturn(null).when(searchAlgorithmServiceSpy).findAvailableResource(
                tr1,
                true,
                bookedResourceList,
                templateResourceList);

        resultTrs = searchAlgorithmServiceSpy
                .calculateFixedResourceAvailability(
                        fixedTrs,
                        true,
                        bookedResourceList);

        assertEquals(null, resultTrs);
    }

    @Test
    public void test_createBookedResource() {
        BookedVisit bookedVisit1 = new BookedVisit();
        bookedVisit1.setId(1);
        Resource resource1 = new Resource();
        resource1.setId(1);
        TemplateResource tr = new TemplateResource();
        tr.setId(1);

        Date date10 = new Date(10);
        Date date30 = new Date(30);

        tr.setDuration(20);
        tr.setScheduledStartTime(date10);
        tr.setScheduledEndTime(date30);
        tr.setResource(resource1);
        tr.setAvailable("avail");
        tr.setRejectedResourceMessage("rej");
        tr.setBillable(true);

        BookedResource resultBr = searchAlgorithmService.createBookedResource(tr, bookedVisit1);

        assertEquals(Integer.valueOf(4567), resultBr.getId());
        assertEquals(bookedVisit1, resultBr.getBookedVisit());
        assertEquals(Integer.valueOf(20), resultBr.getDuration());
        assertEquals(date10, resultBr.getScheduledStartTime());
        assertEquals(date30, resultBr.getScheduledEndTime());
        assertEquals(resource1, resultBr.getResource());
        assertEquals(tr, resultBr.getTemplateResource());
        assertEquals("avail", resultBr.getAvailable());
        assertEquals("rej", resultBr.getRejectedResourceMessage());
        assertEquals(true, resultBr.getBillable());
    }

    @Test
    public void test_addResourcesToBookedVisit() {
        BookedVisit bookedVisit1 = new BookedVisit();
        bookedVisit1.setId(1);

        TemplateResource templateResource1 = new TemplateResource();
        templateResource1.setId(1);
        TemplateResource templateResource2 = new TemplateResource();
        templateResource2.setId(2);

        List<TemplateResource> trList = Lists.newArrayList(
                templateResource1,
                templateResource2);

        BookedResource bookedResource1 = new BookedResource();
        bookedResource1.setId(1);
        BookedResource bookedResource2 = new BookedResource();
        bookedResource2.setId(2);
        BookedResource bookedResource0 = new BookedResource();
        bookedResource0.setId(0);

        Resource resource1 = new Resource();
        resource1.setResourceType(ResourceType.Room);
        resource1.setId(1);
        resource1.setName("R1");
        Resource resource2 = new Resource();
        resource2.setResourceType(ResourceType.Lab);
        resource2.setId(2);

        bookedResource1.setResource(resource1);
        bookedResource2.setResource(resource2);

        List<BookedResource> brList2 = Lists.newArrayList(
                bookedResource1,
                bookedResource2);

        List<BookedResource> brList3 = Lists.newArrayList(
                bookedResource0,
                bookedResource1,
                bookedResource2);

        bookedVisit1.setRooms(null);
        bookedVisit1.setBookedResourceList(null);

        doReturn(bookedResource1).when(searchAlgorithmServiceSpy)
                .createBookedResource(templateResource1, bookedVisit1);
        doReturn(bookedResource2).when(searchAlgorithmServiceSpy)
                .createBookedResource(templateResource2, bookedVisit1);

        searchAlgorithmServiceSpy.addResourcesToBookedVisit(
                trList,
                bookedVisit1);

        assertEquals(brList2, bookedVisit1.getBookedResourceList());
        assertEquals(" R1, ", bookedVisit1.getRooms());

        bookedVisit1.setRooms("Rms: ");
        bookedVisit1.setBookedResourceList(Lists.newArrayList(bookedResource0));

        searchAlgorithmServiceSpy.addResourcesToBookedVisit(
                trList,
                bookedVisit1);

        assertEquals(brList3, bookedVisit1.getBookedResourceList());
        assertEquals("Rms: R1, ", bookedVisit1.getRooms());
    }

    @Test
    public void test_foundAvailableFixedResources() {
        Date searchDate = new Date(10);
        List<TemplateResource> fixedResources = Lists.newArrayList(
                new TemplateResource());

        List<TemplateResource> availableFixedResources =
                Lists.newArrayList(
                        new TemplateResource(),
                        new TemplateResource());

        assertFalse(fixedResources.equals(availableFixedResources));

        BookedVisit bookedVisit = new BookedVisit();
        bookedVisit.setBookedResourceList(null);

        doNothing().when(searchAlgorithmServiceSpy)
                .addResourcesToBookedVisit(availableFixedResources, bookedVisit);
        doNothing().when(searchAlgorithmServiceSpy)
                .allocateTimeFixedResources(5, searchDate, fixedResources, "group");
        doReturn(availableFixedResources).when(searchAlgorithmServiceSpy)
                .calculateFixedResourceAvailability(
                        fixedResources,
                        false,
                        null);
        doReturn(availableFixedResources).when(searchAlgorithmServiceSpy)
                .removeDuplicates(availableFixedResources);
        boolean result = searchAlgorithmServiceSpy.foundAvailableFixedResources(
                searchDate,
                null,
                5,
                bookedVisit,
                false,
                "group");

        verify(searchAlgorithmServiceSpy, times(0))
                .addResourcesToBookedVisit(availableFixedResources, bookedVisit);

        assertTrue(result);

        result = searchAlgorithmServiceSpy.foundAvailableFixedResources(
                searchDate,
                fixedResources,
                5,
                bookedVisit,
                false,
                "group");

        verify(searchAlgorithmServiceSpy, times(1))
                .addResourcesToBookedVisit(availableFixedResources, bookedVisit);

        doReturn(null).when(searchAlgorithmServiceSpy)
                .removeDuplicates(availableFixedResources);

        result = searchAlgorithmServiceSpy.foundAvailableFixedResources(
                searchDate,
                fixedResources,
                5,
                bookedVisit,
                false,
                "group");

        assertFalse(result);
    }

    @Test
    public void test_isRelativeTimeRejectedVisit() {
        BookedVisit bookedVisit = new BookedVisit();
        bookedVisit.setRejectedVisit(false);
        bookedVisit.setBookedResourceList(null);

        boolean result = searchAlgorithmService.isRelativeTimeRejectedVisit(
                bookedVisit, false, false);

        assertTrue(result);
        assertTrue(bookedVisit.isRejectedVisit());
        assertEquals(new ArrayList<BookedResource>(), bookedVisit.getBookedResourceList());

        bookedVisit.setRejectedVisit(false);
        bookedVisit.setBookedResourceList(null);

        result = searchAlgorithmService.isRelativeTimeRejectedVisit(
                bookedVisit, true, false);

        assertFalse(result);
        assertFalse(bookedVisit.isRejectedVisit());
        assertNull(bookedVisit.getBookedResourceList());
    }

    @Test
    public void test_findMultiDayMinutesOffset() {

        int startMinutes135 = MINS_PER_DAY +
                3 * MINS_PER_HR +
                5;
        int startMinutes246 = 2 * MINS_PER_DAY +
                4 * MINS_PER_HR +
                6;

        TemplateResource tr1 = new TemplateResource();
        tr1.setStartMinutes(startMinutes135);
        TemplateResource tr2 = new TemplateResource();
        tr2.setStartMinutes(startMinutes246);

        List<TemplateResource> templateResourceList = Lists.newArrayList(
                tr2, tr1);

        int relativeOffset = 100;

        int result = searchAlgorithmService.findMultiDayMinutesOffset(
                templateResourceList,
                relativeOffset);

        assertEquals(startMinutes135 + 100, result);
    }

    @Test
    public void test_duplicateResourceSlot() {
        TemplateResource tr = new TemplateResource();

        tr.setId(1);
        tr.setAvailableEndTimeInMin(10);
        tr.setBillable(true);
        tr.setAvailableStartTimeInMin(5);
        tr.setDuration(6);
        tr.setFlexible(false);
        tr.setFloatEnd(7);
        tr.setFloatStart(2);
        tr.setStartMinutes(100);
        tr.setEndMinutes(110);

        TemplateResource tr2 = searchAlgorithmService.duplicateResourceSlot(tr);

        assertEquals(Integer.valueOf(1), tr2.getId());
        assertEquals(10, tr2.getAvailableEndTimeInMin());
        assertEquals(true, tr2.getBillable());
        assertEquals(5, tr2.getAvailableStartTimeInMin());
        assertEquals(false, tr2.getFlexible());
        assertEquals(Integer.valueOf(7), tr2.getFloatEnd());
        assertEquals(Integer.valueOf(2), tr2.getFloatStart());
        assertEquals(Integer.valueOf(100), tr2.getStartMinutes());
        assertEquals(Integer.valueOf(110), tr2.getEndMinutes());
    }

    @Test
    public void test_allocateTime2() {
        // trs are temporal contiguous
        int startMinutes13 = MINS_PER_DAY +
                3 * MINS_PER_HR;
        int startMinutes1330 = MINS_PER_DAY +
                3 * MINS_PER_HR +
                30;

        TemplateResource tr1 = new TemplateResource();
        tr1.setStartMinutes(startMinutes13);
        tr1.setDuration(30);

        TemplateResource tr1a = new TemplateResource();
        tr1a.setStartMinutes(startMinutes13);
        tr1a.setDuration(60);

        TemplateResource tr2 = new TemplateResource();
        tr2.setStartMinutes(startMinutes1330);
        tr2.setDuration(90);

        List<TemplateResource> templateResourceList = Lists.newArrayList(
                tr1, tr1a, tr2);

        Date monday6am = DateUtility.toDate(LocalDateTime.of(2017, Month.JULY, 17, 6, 0));

        Date monday0am = DateUtility.toDate(LocalDateTime.of(2017, Month.JULY, 17, 0, 0));
        Date monday0030am = DateUtility.toDate(LocalDateTime.of(2017, Month.JULY, 17, 0, 30));
        Date monday2am = DateUtility.toDate(LocalDateTime.of(2017, Month.JULY, 17, 2, 0));

        searchAlgorithmServiceSpy.allocateTime(
                templateResourceList,
                0,
                monday6am);

        assertEquals(tr1.getScheduledStartTime(), monday0am);
        assertEquals(tr1.getScheduledEndTime(), monday0030am);

        // Note: delta(endTime, startTime) != duration??!!
        assertEquals(tr1a.getScheduledStartTime(), monday0am);
        assertEquals(tr1a.getScheduledEndTime(), monday0030am);
        assertEquals(Integer.valueOf(60), tr1a.getDuration());

        assertEquals(tr2.getScheduledStartTime(), monday0030am);
        assertEquals(tr2.getScheduledEndTime(), monday2am);
    }

    @Test
    public void test_checkTimeSlotAvailability() {
        TemplateResource templateResource1 = new TemplateResource();
        templateResource1.setId(1);
        TemplateResource templateResource2 = new TemplateResource();
        templateResource2.setId(2);

        List<TemplateResource> resources = Lists.newArrayList(
                templateResource1);
        List<TemplateResource> availableSlots = Lists.newArrayList(
                templateResource2);
        assertNotEquals(resources, availableSlots);

        List<BookedResource> bookedResourceList = Lists.newArrayList();

        doReturn(templateResource1).when(searchAlgorithmServiceSpy)
                .findAvailableResource(
                        templateResource1,
                        false,
                        bookedResourceList,
                        availableSlots);

        List<TemplateResource> resultList = searchAlgorithmServiceSpy
                .checkTimeSlotAvailability(
                        resources,
                        false,
                        bookedResourceList,
                        availableSlots);

        assertEquals(2, resultList.size());
        assertTrue(resultList.contains(templateResource1));
        assertTrue(resultList.contains(templateResource2));

        doReturn(null).when(searchAlgorithmServiceSpy)
                .findAvailableResource(
                        templateResource1,
                        false,
                        bookedResourceList,
                        availableSlots);

        resultList = searchAlgorithmServiceSpy
                .checkTimeSlotAvailability(
                        resources,
                        false,
                        bookedResourceList,
                        availableSlots);

        assertEquals(0, resultList.size());
    }

    @Test
    public void test_findAvailablePermutationSequence() {
        TemplateResource templateResource1 = new TemplateResource();
        templateResource1.setId(1);
        TemplateResource templateResource2 = new TemplateResource();
        templateResource2.setId(2);
        TemplateResource templateResource3 = new TemplateResource();
        templateResource3.setId(3);

        List<TemplateResource> resources123 = Lists.newArrayList(
                templateResource1,
                templateResource2,
                templateResource3);
        List<TemplateResource> resources132 = Lists.newArrayList(
                templateResource1,
                templateResource3,
                templateResource2);
        List<TemplateResource> resources213 = Lists.newArrayList(
                templateResource2,
                templateResource1,
                templateResource3);
        List<TemplateResource> resources231 = Lists.newArrayList(
                templateResource2,
                templateResource3,
                templateResource1);
        List<TemplateResource> resources312 = Lists.newArrayList(
                templateResource3,
                templateResource1,
                templateResource2);

        List<TemplateResource> resources23 = Lists.newArrayList(
                templateResource2,
                templateResource3);
        List<TemplateResource> resources13 = Lists.newArrayList(
                templateResource1,
                templateResource3);
        List<TemplateResource> resources1 = Lists.newArrayList(
                templateResource1);
        List<TemplateResource> resources2 = Lists.newArrayList(
                templateResource2);

        List<TemplateResource> availableSlots = Lists.newArrayList();
        List<TemplateResource> availableSlots21 = Lists.newArrayList(
                templateResource2,
                templateResource1);
        List<TemplateResource> availableSlots12 = Lists.newArrayList(
                templateResource1,
                templateResource2);
        List<TemplateResource> availableSlots212 = Lists.newArrayList(
                templateResource2,
                templateResource1,
                templateResource2);
        List<TemplateResource> availableSlots121 = Lists.newArrayList(
                templateResource1,
                templateResource2,
                templateResource1);
        List<TemplateResource> availableSlots1212 = Lists.newArrayList(
                templateResource1,
                templateResource2,
                templateResource1,
                templateResource2);

        List<BookedResource> bookedResourceList = Lists.newArrayList();

        Date searchDate = new Date(100);

        doReturn(0).when(searchAlgorithmServiceSpy)
                .findMultiDayMinutesOffset(resources123, 0);

        doReturn(templateResource1).when(searchAlgorithmServiceSpy)
                .duplicateResourceSlot(templateResource1);
        doReturn(templateResource2).when(searchAlgorithmServiceSpy)
                .duplicateResourceSlot(templateResource2);
        doReturn(templateResource3).when(searchAlgorithmServiceSpy)
                .duplicateResourceSlot(templateResource3);

        doNothing().when(searchAlgorithmServiceSpy)
                .allocateTime(resources123, 0, searchDate);
        doNothing().when(searchAlgorithmServiceSpy)
                .allocateTime(resources132, 0, searchDate);
        doNothing().when(searchAlgorithmServiceSpy)
                .allocateTime(resources213, 0, searchDate);
        doNothing().when(searchAlgorithmServiceSpy)
                .allocateTime(resources312, 0, searchDate);

        doReturn(Lists.newArrayList()).when(searchAlgorithmServiceSpy)
                .checkTimeSlotAvailability(resources23,
                        true, bookedResourceList,
                        resources1);
        doReturn(Lists.newArrayList()).when(searchAlgorithmServiceSpy)
                .checkTimeSlotAvailability(resources132,
                        true, bookedResourceList,
                        new ArrayList<TemplateResource>());
        doReturn(resources213).when(searchAlgorithmServiceSpy)
                .checkTimeSlotAvailability(resources13,
                        true, bookedResourceList,
                        resources2);

        doReturn(templateResource1).when(searchAlgorithmServiceSpy)
                .findAvailableResource(templateResource1, true,
                        bookedResourceList, availableSlots);
        doReturn(templateResource2).when(searchAlgorithmServiceSpy)
                .findAvailableResource(templateResource2, true,
                        bookedResourceList, availableSlots);
        doReturn(templateResource1).when(searchAlgorithmServiceSpy)
                .findAvailableResource(templateResource1, true,
                        bookedResourceList, availableSlots12);

        List<TemplateResource> resultList = searchAlgorithmServiceSpy.findAvailablePermutationSequence(
                resources123,
                0,
                searchDate,
                true,
                "group",
                bookedResourceList,
                availableSlots);

        assertEquals(resources213, resultList);

        doReturn(templateResource1).when(searchAlgorithmServiceSpy)
                .findAvailableResource(templateResource1, true,
                        bookedResourceList, availableSlots);

        doReturn(resources123).when(searchAlgorithmServiceSpy)
                .checkTimeSlotAvailability(resources23,
                        true, bookedResourceList,
                        availableSlots21);

        resultList = searchAlgorithmServiceSpy.findAvailablePermutationSequence(
                resources123,
                0,
                searchDate,
                true,
                "group",
                bookedResourceList,
                availableSlots);

        assertEquals(resources123, resultList);

        doReturn(null).when(searchAlgorithmServiceSpy)
                .findAvailableResource(templateResource1, true,
                        bookedResourceList, availableSlots);

        doReturn(templateResource1).when(searchAlgorithmServiceSpy)
                .findAvailableResource(templateResource1, true,
                        bookedResourceList, availableSlots12);

        doReturn(templateResource2).when(searchAlgorithmServiceSpy)
                .findAvailableResource(templateResource1, true,
                        bookedResourceList, availableSlots121);

        doReturn(templateResource3).when(searchAlgorithmServiceSpy)
                .findAvailableResource(templateResource3, true,
                        bookedResourceList, availableSlots1212);

        doReturn(null).when(searchAlgorithmServiceSpy)
                .findAvailableResource(templateResource3, true,
                        bookedResourceList, availableSlots12);

        doReturn(Lists.newArrayList()).when(searchAlgorithmServiceSpy)
                .checkTimeSlotAvailability(resources13,
                        true, bookedResourceList,
                        availableSlots212);
        doReturn(Lists.newArrayList()).when(searchAlgorithmServiceSpy)
                .checkTimeSlotAvailability(resources231,
                        true, bookedResourceList,
                        Lists.newArrayList());

        doNothing().when(searchAlgorithmServiceSpy)
                .allocateTime(resources231, 0, searchDate);

        resultList = searchAlgorithmServiceSpy.findAvailablePermutationSequence(
                resources123,
                0,
                searchDate,
                true,
                "group",
                bookedResourceList,
                availableSlots);

        assertEquals(resources123, resultList);
    }

    @Test
    public void test_allocateCheckAvailabilityFlip() {
        TemplateResource tr1 = new TemplateResource();
        tr1.setId(1);
        TemplateResource tr2 = new TemplateResource();
        tr2.setId(2);

        List<TemplateResource> trList1 = Lists.newArrayList(tr1);
        List<TemplateResource> trList2 = Lists.newArrayList(tr2);
        List<TemplateResource> trList11 = Lists.newArrayList(tr1, tr1);
        List<TemplateResource> trList22 = Lists.newArrayList(tr2, tr2);
        List<TemplateResource> trList12 = Lists.newArrayList(tr1, tr2);

        Map<String, List<TemplateResource>> flipResources = Maps.newHashMap();
        flipResources.put("one", trList1);
        flipResources.put("two", trList2);

        List<BookedResource> availableResourceSlot = Lists.newArrayList();
        Date searchDate = new Date(200);

        doReturn(trList11).when(searchAlgorithmServiceSpy)
                .findAvailablePermutationSequence(
                        trList1,
                        0,
                        searchDate,
                        false,
                        "group",
                        availableResourceSlot,
                        new ArrayList<TemplateResource>()
                );
        doReturn(trList22).when(searchAlgorithmServiceSpy)
                .findAvailablePermutationSequence(
                        trList2,
                        0,
                        searchDate,
                        false,
                        "group",
                        availableResourceSlot,
                        trList1
                );
        List resultTrList = searchAlgorithmServiceSpy.allocateCheckAvailabilityFlip(
                0,
                searchDate,
                flipResources,
                false,
                "group",
                availableResourceSlot);

        verify(searchAlgorithmServiceSpy, times(2))
                .findAvailablePermutationSequence(
                        anyList(),
                        anyInt(),
                        any(Date.class),
                        anyBoolean(),
                        anyString(),
                        anyList(),
                        anyList()
                );
        assertEquals(trList12, resultTrList);

        doReturn(null).when(searchAlgorithmServiceSpy)
                .findAvailablePermutationSequence(
                        trList1,
                        0,
                        searchDate,
                        false,
                        "group",
                        availableResourceSlot,
                        new ArrayList<TemplateResource>()
                );

        resultTrList = searchAlgorithmServiceSpy.allocateCheckAvailabilityFlip(
                0,
                searchDate,
                flipResources,
                false,
                "group",
                availableResourceSlot);

        assertNull(resultTrList);
    }

    @Test
    public void test_foundAvailableFlipResources() {
        TemplateResource tr1 = new TemplateResource();
        tr1.setId(1);

        List<TemplateResource> trList1 = Lists.newArrayList(tr1);
        Map<String, List<TemplateResource>> flipResourceGroups = Maps.newHashMap();
        flipResourceGroups.put("one", trList1);

        BookedVisit bookedVisit = new BookedVisit();
        Date searchDate = new Date(200);
        List<BookedResource> bookedResourceList = Lists.newArrayList();

        bookedVisit.setBookedResourceList(bookedResourceList);

        doReturn(trList1).when(searchAlgorithmServiceSpy)
                .allocateCheckAvailabilityFlip(
                        0,
                        searchDate,
                        flipResourceGroups,
                        false,
                        "group",
                        bookedResourceList);

        doNothing().when(searchAlgorithmServiceSpy)
                .addResourcesToBookedVisit(trList1, bookedVisit);

        boolean result = searchAlgorithmServiceSpy.foundAvailableFlipResources(
                searchDate,
                flipResourceGroups,
                0,
                bookedVisit,
                false,
                "group");

        assertTrue(result);

        verify(searchAlgorithmServiceSpy, times(1))
                .removeDuplicates(trList1);
        verify(searchAlgorithmServiceSpy, times(1))
                .addResourcesToBookedVisit(trList1, bookedVisit);

        doReturn(null).when(searchAlgorithmServiceSpy)
                .allocateCheckAvailabilityFlip(
                        0,
                        searchDate,
                        flipResourceGroups,
                        false,
                        "group",
                        bookedResourceList);

        result = searchAlgorithmServiceSpy.foundAvailableFlipResources(
                searchDate,
                flipResourceGroups,
                0,
                bookedVisit,
                false,
                "group");

        assertFalse(result);  }

    @Test
    public void test_allocateTime3() {
        Date monday6am = DateUtility.toDate(LocalDateTime.of(2017, Month.JULY, 31, 6, 0));
        Date monday1am = DateUtility.toDate(LocalDateTime.of(2017, Month.JULY, 31, 1, 0));
        Date monday3am = DateUtility.toDate(LocalDateTime.of(2017, Month.JULY, 31, 3, 0));

        TemplateResource resource = new TemplateResource();
        resource.setStartMinutes(60);
        resource.setDuration(120);

        searchAlgorithmServiceSpy.allocateTime(
                resource,
                0,
                monday6am);

        assertEquals(monday1am, resource.getScheduledStartTime());
        assertEquals(monday3am, resource.getScheduledEndTime());
        assertEquals(180, resource.getAvailableEndTimeInMin());
    }

    @Test
    public void test_allocateCheckAvailabilityUserPreferred() {
        Date searchDate = new Date(100);

        TemplateResource tr1 = new TemplateResource();
        tr1.setId(1);
        TemplateResource tr2 = new TemplateResource();
        tr2.setId(2);

        List<TemplateResource> tr12List = Lists.newArrayList(tr1,tr2);
        List<TemplateResource> tr1List = Lists.newArrayList(tr1);
        List<BookedResource> availableBookedResourceSlots = Lists.newArrayList();

        List<TemplateResource> expectedFound = Lists.newArrayList(tr1, tr2);

        doNothing().when(searchAlgorithmServiceSpy)
                .allocateTime(any(TemplateResource.class),
                        anyInt(), any(Date.class));
        doReturn(tr1).when(searchAlgorithmServiceSpy).findAvailableResource(
                tr1,
                false,
                availableBookedResourceSlots,
                Lists.newArrayList());
        doReturn(tr2).when(searchAlgorithmServiceSpy).findAvailableResource(
                tr2,
                false,
                availableBookedResourceSlots,
                tr1List);

        List<TemplateResource> resultList = searchAlgorithmServiceSpy
                .allocateCheckAvailabilityUserPreferred(
                        tr12List,
                        0,
                        searchDate,
                        false,
                        availableBookedResourceSlots,
                        Lists.newArrayList());

        assertEquals(expectedFound, resultList);

        doReturn(null).when(searchAlgorithmServiceSpy).findAvailableResource(
                tr1,
                false,
                availableBookedResourceSlots,
                Lists.newArrayList());

        resultList = searchAlgorithmServiceSpy
                .allocateCheckAvailabilityUserPreferred(
                        tr12List,
                        0,
                        searchDate,
                        false,
                        availableBookedResourceSlots,
                        Lists.newArrayList());

        assertNull(resultList);
    }

    @Test
    public void test_possiblyCheckVisitEnd() {

        doReturn(false).when(searchAlgorithmServiceSpy)
                .isStartPlusDurationLteEnd(1,1,1);

        boolean result = searchAlgorithmServiceSpy
                .possiblyCheckVisitEnd(true, 1,1,1);

        assertFalse(result);

        verify(searchAlgorithmServiceSpy, times(1))
                .isStartPlusDurationLteEnd(1,1,1);

        result = searchAlgorithmServiceSpy
                .possiblyCheckVisitEnd(false, 1,1,1);

        assertTrue(result);
    }

    @Test
    public void test_prepareResourceTimeSlots() {

        int startMinute = 100;
        int endMinute = 200;

        Date startDate = new Date(50);
        Date endDate = new Date(150);

        TemplateResource tr = new TemplateResource();
        List<TemplateResource> trList = Lists.newArrayList(tr);

        searchAlgorithmService.prepareResourceTimeSlots(
                startMinute,
                endMinute,
                startDate,
                endDate,
                trList);

        assertEquals(startMinute, tr.getAvailableStartTimeInMin());
        assertEquals(endMinute, tr.getAvailableEndTimeInMin());

        assertEquals(startDate, tr.getScheduledStartTime());
        assertEquals(endDate, tr.getScheduledEndTime());

    }

    @Test
    public void test_findTemplateResourceSlots() {
        Date searchDate = DateUtility.toDate(LocalDateTime.of(2017, Month.JULY, 31, 6, 0));
        Date date00 = DateUtility.toDate(LocalDateTime.of(2017, Month.JULY, 31, 0, 0));
        Date date30 = DateUtility.toDate(LocalDateTime.of(2017, Month.JULY, 31, 0, 30));
        Date date10 = DateUtility.toDate(LocalDateTime.of(2017, Month.JULY, 31, 1, 0));
        Date date130 = DateUtility.toDate(LocalDateTime.of(2017, Month.JULY, 31, 1, 30));

        int visitTimeSlotEnd = 600;
        int floatDelta = 0;
        int floatStart = 0;
        int floatEnd = 60;
        int duration = 30;
        int increment = 30;

        TemplateResource tr1 = new TemplateResource();
        tr1.setId(1);
        tr1.setFloatStart(floatStart);
        tr1.setFloatEnd(floatEnd);
        tr1.setDuration(duration);

        List<TemplateResource> resources = Lists.newArrayList(tr1);
        List<BookedResource> availableBookedResourceSlots = Lists.newArrayList();
        List<TemplateResource> availableResourceSlots = Lists.newArrayList(tr1);
        List<TemplateResource> foundSlots = Lists.newArrayList(tr1);

        doNothing().when(searchAlgorithmServiceSpy).prepareResourceTimeSlots(
                anyInt(),
                anyInt(),
                any(Date.class),
                any(Date.class),
                any(List.class));

        doReturn(new ArrayList<TemplateResource>()).when(searchAlgorithmServiceSpy)
                .checkTimeSlotAvailability(
                        resources,
                        true,
                        availableBookedResourceSlots,
                        availableResourceSlots);

        List<TemplateResource> result =
                searchAlgorithmServiceSpy.findTemplateResourceSlots(
                    resources,
                    visitTimeSlotEnd,
                    searchDate,
                    false,
                    floatDelta,
                    true,
                    availableBookedResourceSlots,
                    availableResourceSlots);

        assertEquals(null, result);

        verify(searchAlgorithmServiceSpy, times(1))
                .prepareResourceTimeSlots(
                        floatStart,
                        duration,
                        date00,
                        date30,
                        resources);
        verify(searchAlgorithmServiceSpy, times(1))
                .prepareResourceTimeSlots(
                        floatStart + increment,
                        duration + increment,
                        date30,
                        date10,
                        resources);
        verify(searchAlgorithmServiceSpy, times(2))
                .prepareResourceTimeSlots(
                        anyInt(),
                        anyInt(),
                        any(Date.class),
                        any(Date.class),
                        any(List.class));

        doReturn(foundSlots).when(searchAlgorithmServiceSpy)
                .checkTimeSlotAvailability(
                        resources,
                        true,
                        availableBookedResourceSlots,
                        availableResourceSlots);

        result = searchAlgorithmServiceSpy.findTemplateResourceSlots(
                        resources,
                        visitTimeSlotEnd,
                        searchDate,
                        false,
                        floatDelta,
                        true,
                        availableBookedResourceSlots,
                        availableResourceSlots);

        assertEquals(foundSlots, result);

        // previous 2 + 1
        verify(searchAlgorithmServiceSpy, times(3))
                .prepareResourceTimeSlots(
                        anyInt(),
                        anyInt(),
                        any(Date.class),
                        any(Date.class),
                        any(List.class));
    }

    @Test
    public void test_findAvailableFloatDurationSlots() {
        Date searchDate = DateUtility.toDate(LocalDateTime.of(2017, Month.JULY, 31, 6, 0));

        int visitTimeSlotStart = 0;
        int visitTimeSlotEnd = 600;

        TemplateResource tr1 = new TemplateResource();
        tr1.setId(1);
        TemplateResource tr2 = new TemplateResource();
        tr2.setId(2);

        TemplateResource tr11 = new TemplateResource();
        tr11.setId(11);
        TemplateResource tr12 = new TemplateResource();
        tr11.setId(12);
        TemplateResource tr13 = new TemplateResource();
        tr11.setId(13);
        TemplateResource tr14 = new TemplateResource();
        tr11.setId(14);
        TemplateResource tr15 = new TemplateResource();
        tr11.setId(15);

        List<TemplateResource> resources = Lists.newArrayList(tr1);
        List<BookedResource> availableBookedResourceSlots = Lists.newArrayList();
        List<TemplateResource> availableResourceSlots = Lists.newArrayList(tr2);

        List<TemplateResource> foundTr11 = Lists.newArrayList(tr11);
        List<TemplateResource> foundTr12 = Lists.newArrayList(tr12);
        List<TemplateResource> foundTr13 = Lists.newArrayList(tr13);
        List<TemplateResource> foundTr14 = Lists.newArrayList(tr14);
        List<TemplateResource> foundTr15 = Lists.newArrayList(tr15);

        doReturn(foundTr11).when(searchAlgorithmServiceSpy)
                .allocateCheckAvailabilityUserPreferred(
                        resources,
                        visitTimeSlotStart,
                        searchDate,
                        true,
                        availableBookedResourceSlots,
                        availableResourceSlots);

        List<TemplateResource> result =
                searchAlgorithmServiceSpy.findAvailableFloatDurationSlots(
                    resources,
                    visitTimeSlotStart,
                    visitTimeSlotEnd,
                    searchDate,
                    true,
                    availableBookedResourceSlots,
                    availableResourceSlots);

        assertEquals(foundTr11, result);

        ////////////
        doReturn(null).when(searchAlgorithmServiceSpy)
                .allocateCheckAvailabilityUserPreferred(
                        resources,
                        visitTimeSlotStart,
                        searchDate,
                        true,
                        availableBookedResourceSlots,
                        availableResourceSlots);

        doReturn(foundTr12).when(searchAlgorithmServiceSpy)
                .findTemplateResourceSlots(
                        resources,
                        visitTimeSlotEnd,
                        searchDate,
                        true,
                        visitTimeSlotStart,
                        true,
                        availableBookedResourceSlots,
                        availableResourceSlots);

        result = searchAlgorithmServiceSpy.findAvailableFloatDurationSlots(
                        resources,
                        visitTimeSlotStart,
                        visitTimeSlotEnd,
                        searchDate,
                        true,
                        availableBookedResourceSlots,
                        availableResourceSlots);

        assertEquals(foundTr12, result);

        //////////
        doReturn(null).when(searchAlgorithmServiceSpy)
                .findTemplateResourceSlots(
                        resources,
                        visitTimeSlotEnd,
                        searchDate,
                        true,
                        visitTimeSlotStart,
                        true,
                        availableBookedResourceSlots,
                        availableResourceSlots);

        doReturn(foundTr13).when(searchAlgorithmServiceSpy)
                .allocateCheckAvailabilityUserPreferred(
                        resources,
                        visitTimeSlotStart,
                        searchDate,
                        true,
                        availableBookedResourceSlots,
                        availableResourceSlots);

        result = searchAlgorithmServiceSpy.findAvailableFloatDurationSlots(
                resources,
                visitTimeSlotStart,
                visitTimeSlotEnd,
                searchDate,
                true,
                availableBookedResourceSlots,
                availableResourceSlots);

        assertEquals(foundTr13, result);
    //////////////


        doReturn(foundTr14).when(searchAlgorithmServiceSpy)
                .allocateCheckAvailabilityUserPreferred(
                        resources,
                        visitTimeSlotStart,
                        searchDate,
                        false,
                        availableBookedResourceSlots,
                        availableResourceSlots);
        result = searchAlgorithmServiceSpy.findAvailableFloatDurationSlots(
                resources,
                visitTimeSlotStart,
                visitTimeSlotEnd,
                searchDate,
                false,
                availableBookedResourceSlots,
                availableResourceSlots);

        assertEquals(foundTr14, result);
        ///////////////

        doReturn(null).when(searchAlgorithmServiceSpy)
                .allocateCheckAvailabilityUserPreferred(
                        resources,
                        visitTimeSlotStart,
                        searchDate,
                        false,
                        availableBookedResourceSlots,
                        availableResourceSlots);

        doReturn(foundTr15).when(searchAlgorithmServiceSpy)
                .findTemplateResourceSlots(
                        resources,
                        visitTimeSlotEnd,
                        searchDate,
                        true,
                        visitTimeSlotStart,
                        false,
                        availableBookedResourceSlots,
                        availableResourceSlots);

        result = searchAlgorithmServiceSpy.findAvailableFloatDurationSlots(
                resources,
                visitTimeSlotStart,
                visitTimeSlotEnd,
                searchDate,
                false,
                availableBookedResourceSlots,
                availableResourceSlots);

        assertEquals(foundTr15, result);
    }

    @Test
    public void test_prepareAndAddToAvailableSlots() {
        Date date600 = DateUtility.toDate(LocalDateTime.of(2017, Month.JULY, 31, 6, 0));
        Date date630 = DateUtility.toDate(LocalDateTime.of(2017, Month.JULY, 31, 6, 30));

        int resourceStartTime = 0;
        int resourceEndTime = 30;

        String groupId1 = "gr1";
        String groupId2 = "gr2";

        TemplateResource tr1 = new TemplateResource();
        tr1.setId(1);
        tr1.setGroupId(groupId1);
        TemplateResource tr2 = new TemplateResource();
        tr2.setId(2);
        tr2.setGroupId(groupId1);

        List<TemplateResource> currentResources = Lists.newArrayList(tr1, tr2);
        List<TemplateResource> availableResourceSlots = Lists.newArrayList();

        searchAlgorithmServiceSpy.prepareAndAddToAvailableSlots(
                resourceStartTime,
                resourceEndTime,
                date600,
                date630,
                currentResources,
                availableResourceSlots,
                "group",
                false);

        assertEquals(2, availableResourceSlots.size());
        for (TemplateResource tr: availableResourceSlots) {
            assertEquals(groupId1, tr.getGroupId());
            assertEquals(date600, tr.getScheduledStartTime());
            assertEquals(date630, tr.getScheduledEndTime());
            assertEquals(resourceStartTime, tr.getAvailableStartTimeInMin());
            assertEquals(resourceEndTime, tr.getAvailableEndTimeInMin());
            assertEquals("group", tr.getResourceGroupType());
        }
        /////////////////

        tr1 = new TemplateResource();
        tr1.setId(1);
        tr1.setGroupId(groupId1);
        tr2 = new TemplateResource();
        tr2.setId(2);
        tr2.setGroupId(groupId1);

        currentResources = Lists.newArrayList(tr1, tr2);
        availableResourceSlots = Lists.newArrayList();

        searchAlgorithmServiceSpy.prepareAndAddToAvailableSlots(
                resourceStartTime,
                resourceEndTime,
                date600,
                date630,
                currentResources,
                availableResourceSlots,
                "group",
                true);

        assertEquals(2, availableResourceSlots.size());
        for (TemplateResource tr: availableResourceSlots) {
            assertEquals(groupId1, tr.getGroupId());
            assertEquals(null, tr.getScheduledStartTime());
            assertEquals(null, tr.getScheduledEndTime());
            assertEquals(0, tr.getAvailableStartTimeInMin());
            assertEquals(0, tr.getAvailableEndTimeInMin());
            assertEquals("group", tr.getResourceGroupType());
        }
        ///////////////
        tr1 = new TemplateResource();
        tr1.setId(1);
        tr1.setGroupId(groupId1);

        tr2 = new TemplateResource();
        tr2.setId(2);
        tr2.setGroupId(groupId2);

        currentResources = Lists.newArrayList(tr1, tr2);

        availableResourceSlots = Lists.newArrayList();

        searchAlgorithmServiceSpy.prepareAndAddToAvailableSlots(
                resourceStartTime,
                resourceEndTime,
                date600,
                date630,
                currentResources,
                availableResourceSlots,
                "group",
                false);

        assertEquals(2, availableResourceSlots.size());

        assertEquals(null, tr1.getScheduledStartTime());
        assertEquals(null, tr1.getScheduledEndTime());
        assertEquals(0, tr1.getAvailableStartTimeInMin());
        assertEquals(0, tr1.getAvailableEndTimeInMin());
        assertEquals("group", tr1.getResourceGroupType());

        assertEquals(date600, tr2.getScheduledStartTime());
        assertEquals(date630, tr2.getScheduledEndTime());
        assertEquals(resourceStartTime, tr2.getAvailableStartTimeInMin());
        assertEquals(resourceEndTime, tr2.getAvailableEndTimeInMin());
        assertEquals("group", tr2.getResourceGroupType());
    }

    @Test
    public void test_allocateCheckAvailabilityFloat() {
        int visitTimeSlotStart = 0;
        int visitTimeSlotEnd = 30;
        Date searchDate = new Date(300);
        Map<String, List<TemplateResource>> floatResources = Maps.newHashMap();
        boolean rejectedCheck = true;
        String groupType = "group";
        List<BookedResource> availableBookedResourceSlots = Lists.newArrayList();

        TemplateResource tr1 = new TemplateResource();
        List<TemplateResource> tr1List = Lists.newArrayList(tr1);
        List<TemplateResource> emptyTrList = Lists.newArrayList();

        List<TemplateResource> result =
                searchAlgorithmServiceSpy.allocateCheckAvailabilityFloat(
                        visitTimeSlotStart,
                        visitTimeSlotEnd,
                        searchDate,
                        floatResources,
                        rejectedCheck,
                        groupType,
                        availableBookedResourceSlots);

        assertEquals(emptyTrList, result);

        ////////////////

        floatResources.put("one", emptyTrList);

        result =
                searchAlgorithmServiceSpy.allocateCheckAvailabilityFloat(
                    visitTimeSlotStart,
                    visitTimeSlotEnd,
                    searchDate,
                    floatResources,
                    rejectedCheck,
                    groupType,
                    availableBookedResourceSlots);

        assertEquals(emptyTrList, result);

        ///////////////////
        doReturn(emptyTrList).when(searchAlgorithmServiceSpy)
                .findAvailableFloatDurationSlots(
                        tr1List,
                        visitTimeSlotStart,
                        visitTimeSlotEnd,
                        searchDate,
                        rejectedCheck,
                        availableBookedResourceSlots,
                        emptyTrList);

        floatResources.put("one", tr1List);
        result = searchAlgorithmServiceSpy.allocateCheckAvailabilityFloat(
                        visitTimeSlotStart,
                        visitTimeSlotEnd,
                        searchDate,
                        floatResources,
                        rejectedCheck,
                        groupType,
                        availableBookedResourceSlots);

        assertNull(result);
        /////////////////
        doReturn(tr1List).when(searchAlgorithmServiceSpy)
                .findAvailableFloatDurationSlots(
                        tr1List,
                        visitTimeSlotStart,
                        visitTimeSlotEnd,
                        searchDate,
                        rejectedCheck,
                        availableBookedResourceSlots,
                        emptyTrList);

        doNothing().when(searchAlgorithmServiceSpy).
                prepareAndAddToAvailableSlots(
                        anyInt(),
                        anyInt(),
                        any(Date.class),
                        any(Date.class),
                        anyList(),
                        anyList(),
                        anyString(),
                        anyBoolean());

        result = searchAlgorithmServiceSpy.allocateCheckAvailabilityFloat(
                visitTimeSlotStart,
                visitTimeSlotEnd,
                searchDate,
                floatResources,
                rejectedCheck,
                groupType,
                availableBookedResourceSlots);

        assertEquals(emptyTrList, result);
    }

    @Test
    public void test_foundAvailableFloatResources() {

        int visitTimeSlotStart = 0;
        int visitDurationInMin = 120;
        Date searchDate = new Date(300);
        Map<String, List<TemplateResource>> floatTrList = Maps.newHashMap();
        boolean rejectedCheck = true;
        String groupType = "float";

        BookedVisit bookedVisit = new BookedVisit();
        bookedVisit.setBookedResourceList(Lists.newArrayList());

        List<TemplateResource> emptyTrList = Lists.newArrayList();

        TemplateResource tr1 = new TemplateResource();
        List<TemplateResource> tr1List = Lists.newArrayList(tr1);
        TemplateResource tr2 = new TemplateResource();
        List<TemplateResource> tr2List = Lists.newArrayList(tr2);

        boolean result = searchAlgorithmServiceSpy.foundAvailableFloatResources(
                visitDurationInMin,
                searchDate,
                floatTrList,
                visitTimeSlotStart,
                bookedVisit,
                rejectedCheck,
                groupType);

        assertTrue(result);
        //////////////////////

        floatTrList.put("one", tr1List);

        doReturn(emptyTrList).when(searchAlgorithmServiceSpy).
                allocateCheckAvailabilityFloat(
                        anyInt(),
                        anyInt(),
                        any(Date.class),
                        anyMap(),
                        anyBoolean(),
                        anyString(),
                        anyList());

        result = searchAlgorithmServiceSpy.foundAvailableFloatResources(
            visitDurationInMin,
                searchDate,
                floatTrList,
                visitTimeSlotStart,
                bookedVisit,
                rejectedCheck,
                groupType);

        assertFalse(result);
        /////////////

        doReturn(tr1List).when(searchAlgorithmServiceSpy).
                allocateCheckAvailabilityFloat(
                        anyInt(),
                        anyInt(),
                        any(Date.class),
                        anyMap(),
                        anyBoolean(),
                        anyString(),
                        anyList());

        doNothing().when(searchAlgorithmServiceSpy).
                addResourcesToBookedVisit(tr1List, bookedVisit);

        result = searchAlgorithmServiceSpy.foundAvailableFloatResources(
                visitDurationInMin,
                searchDate,
                floatTrList,
                visitTimeSlotStart,
                bookedVisit,
                rejectedCheck,
                groupType);

        assertTrue(result);
        /////////////////////

        result = searchAlgorithmServiceSpy.foundAvailableFloatResources(
                visitDurationInMin,
                searchDate,
                floatTrList,
                visitTimeSlotStart,
                bookedVisit,
                rejectedCheck,
                "non-float");

        assertFalse(result);
    }

    void test_calculateSomeAvailableResourcesHelper(
          int numFoundAvailableFixedResources,
          int numFoundAvailableFloatResources,
          int numFoundAvailableFlipResources) {

        verify(searchAlgorithmServiceSpy,
                times(numFoundAvailableFixedResources)).
                foundAvailableFixedResources(
                        any(Date.class),
                        anyList(),
                        anyInt(),
                        any(BookedVisit.class),
                        anyBoolean(),
                        anyString());
        verify(searchAlgorithmServiceSpy,
                times(numFoundAvailableFloatResources)).
                foundAvailableFloatResources(
                        anyInt(),
                        any(Date.class),
                        anyMap(),
                        anyInt(),
                        any(BookedVisit.class),
                        anyBoolean(),
                        anyString());
        verify(searchAlgorithmServiceSpy,
                times(numFoundAvailableFlipResources)).
                foundAvailableFlipResources(
                        any(Date.class),
                        anyMap(),
                        anyInt(),
                        any(BookedVisit.class),
                        anyBoolean(),
                        anyString());

    }
    @Test
    public void test_calculateSomeAvailableResources() {
        TemplateResource tr1 = new TemplateResource();
        tr1.setId(1);
        TemplateResource tr2 = new TemplateResource();
        tr2.setId(2);
        TemplateResource tr3 = new TemplateResource();
        tr3.setId(3);
        TemplateResource tr4 = new TemplateResource();
        tr4.setId(4);

        int visitDurationInMin = 30;
        Date searchDate = new Date(500);
        List<BookedVisit> candidateVisits = Lists.newArrayList();
        List<TemplateResource> fixedResources = Lists.newArrayList(tr1);

        Map<String, List<TemplateResource>> floatTrList = Maps.newHashMap();
        floatTrList.put("floatTr", Lists.newArrayList(tr2));
        Map<String, List<TemplateResource>> floatResourceGroups =
                Maps.newHashMap();
        floatResourceGroups.put("floatTr", Lists.newArrayList(tr3));
        Map<String, List<TemplateResource>> flipResourceGroups =
                Maps.newHashMap();
        flipResourceGroups.put("floatTr", Lists.newArrayList(tr4));

        int visitTimeSlotStart = 0;
        BookedVisit bookedVisit = new BookedVisit();
        boolean rejectedCheck = false;

        doReturn(false).when(searchAlgorithmServiceSpy).
                foundAvailableFixedResources(
                        searchDate,
                        fixedResources,
                        visitTimeSlotStart,
                        bookedVisit,
                        rejectedCheck,
                        "fixed");

        doAnswer(new Answer() {
            private int count = 0;

            public Object answer(InvocationOnMock invocation) {
                return true;
            }
        }).when(searchAlgorithmServiceSpy).
                isRelativeTimeRejectedVisit(
                        any(BookedVisit.class),
                        anyBoolean(),
                        anyBoolean());

        searchAlgorithmServiceSpy.calculateSomeAvailableResources(
                visitDurationInMin,
                searchDate,
                candidateVisits,
                fixedResources,
                floatTrList,
                floatResourceGroups,
                flipResourceGroups,
                visitTimeSlotStart,
                bookedVisit,
                rejectedCheck
        );

        assertTrue(candidateVisits.size() > 0);
        test_calculateSomeAvailableResourcesHelper(
                1,
                0,
                0);
        ////////////////

        doReturn(false).when(searchAlgorithmServiceSpy).
                foundAvailableFloatResources(
                        anyInt(),
                        any(Date.class),
                        anyMap(),
                        anyInt(),
                        any(BookedVisit.class),
                        anyBoolean(),
                        anyString());

        doAnswer(new Answer() {
            private int count = 0;

            public Object answer(InvocationOnMock invocation) {
                if (++count == 1)
                    return false;

                return true;
            }
        }).when(searchAlgorithmServiceSpy).
                isRelativeTimeRejectedVisit(
                        any(BookedVisit.class),
                        anyBoolean(),
                        anyBoolean());

        searchAlgorithmServiceSpy.calculateSomeAvailableResources(
                visitDurationInMin,
                searchDate,
                candidateVisits,
                fixedResources,
                floatTrList,
                floatResourceGroups,
                flipResourceGroups,
                visitTimeSlotStart,
                bookedVisit,
                rejectedCheck
        );

        test_calculateSomeAvailableResourcesHelper(
                2,
                1,
                0);

        //////////////

        doAnswer(new Answer() {
            private int count = 0;

            public Object answer(InvocationOnMock invocation) {
                if (++count < 3)
                    return false;

                return true;
            }
        }).when(searchAlgorithmServiceSpy).
                isRelativeTimeRejectedVisit(
                        any(BookedVisit.class),
                        anyBoolean(),
                        anyBoolean());

        searchAlgorithmServiceSpy.calculateSomeAvailableResources(
                visitDurationInMin,
                searchDate,
                candidateVisits,
                fixedResources,
                floatTrList,
                floatResourceGroups,
                flipResourceGroups,
                visitTimeSlotStart,
                bookedVisit,
                rejectedCheck
        );

        test_calculateSomeAvailableResourcesHelper(
                3,
                3,
                0);

        ////////////
        doReturn(false).when(searchAlgorithmServiceSpy).
                foundAvailableFlipResources(
                        any(Date.class),
                        anyMap(),
                        anyInt(),
                        any(BookedVisit.class),
                        anyBoolean(),
                        anyString());

        doAnswer(new Answer() {
            private int count = 0;

            public Object answer(InvocationOnMock invocation) {
                if (++count < 4)
                    return false;

                return true;
            }
        }).when(searchAlgorithmServiceSpy).
                isRelativeTimeRejectedVisit(
                        any(BookedVisit.class),
                        anyBoolean(),
                        anyBoolean());

        searchAlgorithmServiceSpy.calculateSomeAvailableResources(
                visitDurationInMin,
                searchDate,
                candidateVisits,
                fixedResources,
                floatTrList,
                floatResourceGroups,
                flipResourceGroups,
                visitTimeSlotStart,
                bookedVisit,
                rejectedCheck
        );

        test_calculateSomeAvailableResourcesHelper(
                4,
                5,
                1);

    }

    @Test
    public void test_modifyDateFieldPlusAmtSetHourMinute() {
        Date date15th630 = DateUtility.toDate(
                LocalDateTime.of(2017, Month.JULY, 15, 6, 30));
        Date date17th2100 = DateUtility.toDate(
                LocalDateTime.of(2017, Month.JULY, 17, 21, 0));

        Date result = searchAlgorithmService.
                modifyDateFieldPlusAmtSetHourMinute(
                        date15th630,
                        Calendar.DAY_OF_MONTH,
                        2,
                        21,
                        0
                        );

        assertEquals(date17th2100, result);
    }

    @Test
    public void test_addToCandidateVisitList() {
        BookedVisit bookedVisit = new BookedVisit();
        bookedVisit.setId(1);

        List<BookedVisit> atFirstEmptyList = Lists.newArrayList();
        List<BookedVisit> atFirstBv1List =
                Lists.newArrayList(bookedVisit);
        List<BookedVisit> bv1bv1List =
                Lists.newArrayList(bookedVisit, bookedVisit);

        // this case is essentially a no-op
        searchAlgorithmService.addToCandidateVisitList(
                bookedVisit,
                null);

        searchAlgorithmService.addToCandidateVisitList(
                bookedVisit,
                atFirstEmptyList);

        assertEquals(atFirstBv1List, atFirstEmptyList);

        searchAlgorithmService.addToCandidateVisitList(
                bookedVisit,
                atFirstBv1List);

        assertEquals(bv1bv1List, atFirstBv1List);
    }

    @Test
    public void test_findEarliestInpatientClockStartTime() {

        TemplateResource tr20 = new TemplateResource();
        tr20.setStartMinutes(20);

        TemplateResource tr30 = new TemplateResource();
        tr30.setStartMinutes(30);

        TemplateResource trNull = new TemplateResource();
        trNull.setStartMinutes(null);

        int result = searchAlgorithmService.
                findEarliestInpatientClockStartTime(
                        Lists.newArrayList(trNull, tr20, tr30));

        assertEquals(0, result);

        result = searchAlgorithmService.
                findEarliestInpatientClockStartTime(
                        Lists.newArrayList(tr20, tr30, tr30));

        assertEquals(20, result);
    }

    @Test
    public void test_findLatestInpatientClockStartTime() {

        TemplateResource tr20 = new TemplateResource();
        tr20.setEndMinutes(20);

        TemplateResource tr30 = new TemplateResource();
        tr30.setEndMinutes(30);

        TemplateResource trNull = new TemplateResource();
        trNull.setEndMinutes(null);

        int result = searchAlgorithmService.
                findLatestInpatientEndTime(
                        Lists.newArrayList(trNull, tr20, tr30));

        assertEquals(30, result);

        result = searchAlgorithmService.
                findLatestInpatientEndTime(
                        Lists.newArrayList(tr20, tr30, tr30));

        assertEquals(30, result);

        result = searchAlgorithmService.
                findLatestInpatientEndTime(
                        Lists.newArrayList(trNull, trNull));

        assertEquals(0, result);
    }

    @Test
    public void test_subtractDaysWorthOfMinutes() {
        assertEquals(
                42,
                searchAlgorithmService
                .subtractDaysWorthOfMinutes(
                        42 + (2*MINS_PER_DAY),
                        2));
    }

    @Test
    public void test_initializeClockTimeBookedVisit() {
        Study study = new Study();
        study.setId(1);
        VisitType visitType = new VisitType();
        visitType.setId(1);

        VisitTemplate vt1 = new VisitTemplate();
        vt1.setName("vt1");
        vt1.setVisitType(visitType);
        vt1.setStudy(study);

        int earlyStartMin = 30;
        int lateEndMin = (2*MINS_PER_DAY) + 90;

        TemplateResource tr1 = new TemplateResource();
        tr1.setId(1);
        tr1.setStartMinutes(earlyStartMin);
        tr1.setEndMinutes(lateEndMin - 10);

        TemplateResource tr2 = new TemplateResource();
        tr2.setId(2);
        tr2.setStartMinutes(earlyStartMin + 10);
        tr2.setEndMinutes(lateEndMin);

        List<TemplateResource> trList = Lists.newArrayList(tr1, tr2);

        doReturn(trList).when(templateResourceDAO).
                        findTemplateResourcesByVisit(vt1);

        Date date15th630 = DateUtility.toDate(
                LocalDateTime.of(2017, Month.JULY, 15, 6, 30));
        Date date15th030 = DateUtility.toDate(
                LocalDateTime.of(2017, Month.JULY, 15, 0, 30));
        Date date17th130 = DateUtility.toDate(
                LocalDateTime.of(2017, Month.JULY, 17, 1, 30));

        BookedVisit bookedVisit = searchAlgorithmService.
                initializeClockTimeBookedVisit(vt1, date15th630);

        assertEquals(Integer.valueOf(1234556), bookedVisit.getId());

        assertEquals("vt1", bookedVisit.getName());
        assertEquals(study, bookedVisit.getStudy());
        assertEquals(vt1, bookedVisit.getVisitTemplate());
        assertEquals(visitType, bookedVisit.getVisitType());

        assertEquals(date15th030, bookedVisit.getScheduledStartTime());
        assertEquals(date17th130, bookedVisit.getScheduledEndTime());
    }

    @Test
    public void test_isRejectedVisit() {

        TemplateResource templateResource = new TemplateResource();
        BookedVisit bookedVisit = new BookedVisit();

        List<TemplateResource> emptyList = Lists.newArrayList();
        List<TemplateResource> oneList =
                Lists.newArrayList(templateResource);

        assertNull(bookedVisit.getBookedResourceList());
        assertFalse(bookedVisit.isRejectedVisit());

        boolean result = searchAlgorithmServiceSpy.isRejectedVisit(
                true,
                bookedVisit,
                emptyList);

        assertTrue(result);
        assertTrue(bookedVisit.isRejectedVisit());
        ///////////////

        doNothing().when(searchAlgorithmServiceSpy).
                addResourcesToBookedVisit(
                        anyList(),
                        any(BookedVisit.class));

        result = searchAlgorithmServiceSpy.isRejectedVisit(
                true,
                bookedVisit,
                oneList);

        assertFalse(result);
        assertTrue(bookedVisit.isRejectedVisit());
        ///////
        bookedVisit.setRejectedVisit(true);
        result = searchAlgorithmServiceSpy.isRejectedVisit(
                false,
                bookedVisit,
                oneList);

        assertFalse(result);
        assertFalse(bookedVisit.isRejectedVisit());
    }

    void test_calculateAvailableVisitTimeSlotClockTimeHelper(
            int fixed, int floating, int flex){

        verify(searchAlgorithmServiceSpy, times(fixed)).
                calculateFixedResourceAvailability(
                        anyList(),
                        anyBoolean(),
                        anyList());
        verify(searchAlgorithmServiceSpy, times(floating)).
                allocateCheckAvailabilityFloatClockTime(
                        any(Date.class),
                        anyMap(),
                        anyBoolean(),
                        anyString(),
                        anyList());
        verify(searchAlgorithmServiceSpy, times(flex)).
                allocateCheckAvailabilityFlipClockTime(
                        any(Date.class),
                        anyMap(),
                        anyBoolean(),
                        anyString(),
                        anyList());
    }
    @Test
    public void test_calculateAvailableVisitTimeSlotClockTime() {
        VisitTemplate vt = new VisitTemplate();
        SubjectMrn sm = new SubjectMrn();

        Date searchDate = DateUtility.toDate(
                LocalDateTime.of(2017, Month.JULY, 15, 0, 30));
        Date endDateParam = DateUtility.toDate(
                LocalDateTime.of(2017, Month.JULY, 15, 1, 30));
        Date earlierDate = DateUtility.toDate(
                LocalDateTime.of(2017, Month.JULY, 13, 1, 30));

        List<BookedResource> emptyBrList = Lists.newArrayList();

        BookedVisit bv = new BookedVisit();
        bv.setId(1);
        bv.setScheduledEndTime(endDateParam);
        bv.setBookedResourceList(emptyBrList);

        TemplateResource tr1 = new TemplateResource();
        tr1.setId(1);
        TemplateResource tr2 = new TemplateResource();
        tr2.setId(2);
        TemplateResource tr3 = new TemplateResource();
        tr3.setId(3);
        TemplateResource tr4 = new TemplateResource();
        tr4.setId(4);

        List<BookedVisit> candidateVisits = Lists.newArrayList(bv);

        List<TemplateResource> fixedResources = Lists.newArrayList(tr1);

        List<TemplateResource> floatTrs = Lists.newArrayList(tr2);
        List<TemplateResource> floatResources = Lists.newArrayList(tr3);
        List<TemplateResource> flipResources = Lists.newArrayList(tr4);

        Map<String, List<TemplateResource>> floatTrList =
                Maps.newHashMap();
        floatTrList.put("one", floatTrs);

        Map<String, List<TemplateResource>> floatResourceGroups =
                Maps.newHashMap();
        floatResourceGroups.put("one", floatResources);

        Map<String, List<TemplateResource>> flipResourceGroups =
                Maps.newHashMap();
        flipResourceGroups.put("one", flipResources);

        doReturn(endDateParam).when(searchAlgorithmServiceSpy).
                modifyDateFieldPlusAmtSetHourMinute(
                        any(Date.class),
                        anyInt(),
                        anyInt(),
                        anyInt(),
                        anyInt());

        doNothing().when(searchAlgorithmServiceSpy).
                allocateTimeFixedResourcesClockTime(
                        searchDate,
                        fixedResources,
                        "fixed");

        doReturn(fixedResources).when(searchAlgorithmServiceSpy).
                calculateFixedResourceAvailability(
                        fixedResources, true, emptyBrList);

        doReturn(true).when(searchAlgorithmServiceSpy).
                isRejectedVisit(true, bv, fixedResources);

        doReturn(bv).when(searchAlgorithmServiceSpy).
                initializeClockTimeBookedVisit(vt, searchDate);

        searchAlgorithmServiceSpy.
                calculateAvailableVisitTimeSlotClockTime(
                        vt,
                        sm,
                        searchDate,
                        endDateParam,
                        candidateVisits,
                        fixedResources,
                        floatTrList,
                        floatResourceGroups,
                        flipResourceGroups,
                        true
        );

        test_calculateAvailableVisitTimeSlotClockTimeHelper(
                1,0,0);
        //////////
        doReturn(false).when(searchAlgorithmServiceSpy).
                isRejectedVisit(true, bv, fixedResources);


        doReturn(floatTrs).when(searchAlgorithmServiceSpy).
                allocateCheckAvailabilityFloatClockTime(
                        searchDate,
                        floatTrList,
                        true,
                        "float",
                        emptyBrList);
        doReturn(true).when(searchAlgorithmServiceSpy).
                isRejectedVisit(true, bv, floatTrs);


        searchAlgorithmServiceSpy.
                calculateAvailableVisitTimeSlotClockTime(
                        vt,
                        sm,
                        searchDate,
                        endDateParam,
                        candidateVisits,
                        fixedResources,
                        floatTrList,
                        floatResourceGroups,
                        flipResourceGroups,
                        true
                );

        test_calculateAvailableVisitTimeSlotClockTimeHelper(
                2,1,0);
        //////////////
        doReturn(false).when(searchAlgorithmServiceSpy).
                isRejectedVisit(true, bv, floatTrs);
        doReturn(true).when(searchAlgorithmServiceSpy).
                isRejectedVisit(true, bv, floatResources);

        doReturn(floatResources).when(searchAlgorithmServiceSpy).
                allocateCheckAvailabilityFloatClockTime(
                        searchDate,
                        floatResourceGroups,
                        true,
                        "float group",
                        emptyBrList);

        searchAlgorithmServiceSpy.
                calculateAvailableVisitTimeSlotClockTime(
                        vt,
                        sm,
                        searchDate,
                        endDateParam,
                        candidateVisits,
                        fixedResources,
                        floatTrList,
                        floatResourceGroups,
                        flipResourceGroups,
                        true
                );

        test_calculateAvailableVisitTimeSlotClockTimeHelper(
                3,3,0);
        ////////////
        doReturn(false).when(searchAlgorithmServiceSpy).
                isRejectedVisit(true, bv, floatResources);
        doReturn(false).when(searchAlgorithmServiceSpy).
                isRejectedVisit(true, bv, flipResources);

        doReturn(flipResources).when(searchAlgorithmServiceSpy).
                allocateCheckAvailabilityFlipClockTime(
                        searchDate,
                        flipResourceGroups,
                        true,
                        "flex",
                        emptyBrList);

        searchAlgorithmServiceSpy.
                calculateAvailableVisitTimeSlotClockTime(
                        vt,
                        sm,
                        searchDate,
                        endDateParam,
                        candidateVisits,
                        fixedResources,
                        floatTrList,
                        floatResourceGroups,
                        flipResourceGroups,
                        true
                );

        test_calculateAvailableVisitTimeSlotClockTimeHelper(
                4,5,1);
        ////////
        doReturn(true).when(searchAlgorithmServiceSpy).
                isRejectedVisit(true, bv, flipResources);

        searchAlgorithmServiceSpy.
                calculateAvailableVisitTimeSlotClockTime(
                        vt,
                        sm,
                        searchDate,
                        endDateParam,
                        candidateVisits,
                        fixedResources,
                        floatTrList,
                        floatResourceGroups,
                        flipResourceGroups,
                        true
                );

        test_calculateAvailableVisitTimeSlotClockTimeHelper(
                5,7,2);
        /////////
        doReturn(earlierDate).when(searchAlgorithmServiceSpy).
                modifyDateFieldPlusAmtSetHourMinute(
                        any(Date.class),
                        anyInt(),
                        anyInt(),
                        anyInt(),
                        anyInt());

        searchAlgorithmServiceSpy.
                calculateAvailableVisitTimeSlotClockTime(
                        vt,
                        sm,
                        searchDate,
                        endDateParam,
                        candidateVisits,
                        fixedResources,
                        floatTrList,
                        floatResourceGroups,
                        flipResourceGroups,
                        true
                );

        test_calculateAvailableVisitTimeSlotClockTimeHelper(
                5,7,2);

    }

    @Test
    public void test_removeDuplicates() {

        String one = "one";
        String two = "two";

        List<String> noDups = Lists.newArrayList(one, two);
        List<String> dups = Lists.newArrayList(one, two, one, two);

        assertEquals(noDups, searchAlgorithmService.
                removeDuplicates(noDups));
        assertEquals(noDups, searchAlgorithmService.
                removeDuplicates(dups));
    }

    @Test
    public void test_() {

    }
}

