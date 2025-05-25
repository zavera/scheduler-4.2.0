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
package edu.harvard.catalyst.scheduler.entity;

import com.google.common.collect.Lists;
import edu.harvard.catalyst.scheduler.core.SchedulerRuntimeException;
import edu.harvard.catalyst.scheduler.util.DateUtility;
import edu.harvard.catalyst.scheduler.dto.TemplateResourceDTO;
import org.junit.Test;

import java.time.*;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static edu.harvard.catalyst.scheduler.util.DateUtility.MINS_PER_DAY;
import static edu.harvard.catalyst.scheduler.util.DateUtility.TEMPLATE_RESOURCE_DATE_ORIGIN;
import static edu.harvard.catalyst.scheduler.util.DateUtility.minutesSinceOrigin;
import static org.junit.Assert.*;

public class TemplateResourceTest {


    private final Date createdDate = Calendar.getInstance().getTime();
    private boolean isAlternate = true;
    private boolean isBillable = true;
    private String isFlexible = "false";
    private String isFloatable = "true";

    @Test
    public void testIsValidFlexGroup() throws Exception {
        String statusMsg = TemplateResource.isValidFlexGroup(testTemplateResourceList(
                false,
                new MinutesPair(100, 101, 0, 0),
                new MinutesPair(99, 100, 0, 0)));
        assertEquals("OK", statusMsg);

        statusMsg = TemplateResource.isValidFlexGroup(testTemplateResourceList(
                false,
                new MinutesPair(100, 101, 0, 0),
                new MinutesPair(99, 100, 0, 0),
                new MinutesPair(1000, 1001, 0, 0)));
        assertEquals("There is a time gap between the Flex Resources / does not meet the flex link criteria, hence cannot be linked.", statusMsg);

        statusMsg = TemplateResource.isValidFlexGroup(testTemplateResourceList(
                false,
                new MinutesPair(100, 101, 0, 0),
                new MinutesPair(100, 101, 0, 0),
                new MinutesPair(100, 101, 0, 0)));
        assertEquals("At least one resource must not share the same start time", statusMsg);

        statusMsg = TemplateResource.isValidFlexGroup(testTemplateResourceList(
                false,
                new MinutesPair(100, 101, 0, 0),
                new MinutesPair(100, 101, 0, 0),
                new MinutesPair(101, 102, 0, 0)));
        assertEquals("OK", statusMsg);

        statusMsg = TemplateResource.isValidFlexGroup(testTemplateResourceList(
                false,
                new MinutesPair(100, 101, 0, 0),
                new MinutesPair(101, 102, 0, 0),
                new MinutesPair(102, 103, 0, 0)));
        assertEquals("OK", statusMsg);

        List<TemplateResource> resourceList = testTemplateResourceList(
                false,
                new MinutesPair(100, 101, 0, 0),
                new MinutesPair(101, 102, 0, 0),
                new MinutesPair(102, 103, 0, 0)
        );
        resourceList.get(0).setFlexible(true);
        statusMsg = TemplateResource.isValidFlexGroup(resourceList);
        assertTrue(statusMsg.equalsIgnoreCase("Not all of the Resources are Flex Resources."));

    }

    @Test
    public void testGetValidFlexGroup() throws Exception {
        List<TemplateResource> templateResources = testTemplateResourceList(
                false,
                new MinutesPair(100, 101, 0, 0),
                new MinutesPair(99, 100, 0, 0));
        List<TemplateResource> templateResourceList = TemplateResource.getValidFlexGroup(templateResources, templateResources.get(0));
        assertEquals(templateResourceList.size(), 1);

        templateResources = testTemplateResourceList(
                false,
                new MinutesPair(100, 101, 0, 0),
                new MinutesPair(100, 101, 0, 0),
                new MinutesPair(99, 100, 0, 0));
        templateResourceList = TemplateResource.getValidFlexGroup(templateResources, templateResources.get(0));
        assertEquals(templateResourceList.size(), 2);

        templateResources = testTemplateResourceList(
                false,
                new MinutesPair(100, 101, 0, 0),
                new MinutesPair(101, 102, 0, 0),
                new MinutesPair(99, 100, 0, 0));
        templateResourceList = TemplateResource.getValidFlexGroup(templateResources, templateResources.get(0));
        assertEquals(templateResourceList.size(), 2);
        templateResourceList = TemplateResource.getValidFlexGroup(templateResources, templateResources.get(2));
        assertEquals(templateResourceList.size(), 2);

        templateResources = testTemplateResourceList(
                false,
                new MinutesPair(100, 101, 0, 0),
                new MinutesPair(103, 104, 0, 0),
                new MinutesPair(99, 100, 0, 0));
        templateResourceList = TemplateResource.getValidFlexGroup(templateResources, templateResources.get(0));
        assertEquals(templateResourceList.size(), 1);

        templateResources = testTemplateResourceList(
                false,
                new MinutesPair(100, 101, 0, 0),
                new MinutesPair(101, 102, 0, 0),
                new MinutesPair(99, 100, 0, 0),
                new MinutesPair(102, 103, 0, 0));
        templateResourceList = TemplateResource.getValidFlexGroup(templateResources, templateResources.get(0));
        assertEquals(templateResourceList.size(), 3);

        templateResources = testTemplateResourceList(
                false,
                new MinutesPair(98, 99, 0, 0),
                new MinutesPair(100, 101, 0, 0),
                new MinutesPair(101, 102, 0, 0),
                new MinutesPair(102, 103, 0, 0),
                new MinutesPair(104, 106, 0, 0));
        templateResourceList = TemplateResource.getValidFlexGroup(templateResources, templateResources.get(0));
        assertEquals(templateResourceList.size(), 0);
        templateResourceList = TemplateResource.getValidFlexGroup(templateResources, templateResources.get(2));
        assertEquals(templateResourceList.size(), 2);

        templateResources = testTemplateResourceList(
                false,
                new MinutesPair(100, 101, 0, 0),
                new MinutesPair(101, 102, 0, 0),
                new MinutesPair(99, 100, 0, 0),
                new MinutesPair(102, 105, 0, 0));
        templateResourceList = TemplateResource.getValidFlexGroup(templateResources, templateResources.get(0));
        assertEquals(templateResourceList.size(), 2);

        templateResources = testTemplateResourceList(
                false,
                new MinutesPair(100, 101, 0, 0),
                new MinutesPair(101, 102, 0, 0),
                new MinutesPair(99, 100, 0, 0),
                new MinutesPair(102, 103, 0, 0));
        templateResourceList = TemplateResource.getValidFlexGroup(templateResources, templateResources.get(2));
        assertEquals(templateResourceList.size(), 2);

        templateResources = testTemplateResourceList(
                false,
                new MinutesPair(99, 100, 0, 0),
                new MinutesPair(100, 101, 0, 0),
                new MinutesPair(101, 102, 0, 0),
                new MinutesPair(102, 103, 0, 0),
                new MinutesPair(103, 104, 0, 0),
                new MinutesPair(105, 106, 0, 0));
        templateResourceList = TemplateResource.getValidFlexGroup(templateResources, templateResources.get(0));
        assertEquals(templateResourceList.size(), 2);
        templateResourceList = TemplateResource.getValidFlexGroup(templateResources, templateResources.get(1));
        assertEquals(templateResourceList.size(), 3);
        templateResourceList = TemplateResource.getValidFlexGroup(templateResources, templateResources.get(2));
        assertEquals(templateResourceList.size(), 4);
        templateResourceList = TemplateResource.getValidFlexGroup(templateResources, templateResources.get(3));
        assertEquals(templateResourceList.size(), 3);
        templateResourceList = TemplateResource.getValidFlexGroup(templateResources, templateResources.get(4));
        assertEquals(templateResourceList.size(), 2);

        templateResources = testTemplateResourceList(
                false,
                new MinutesPair(99, 100, 0, 0),
                new MinutesPair(100, 101, 0, 0),
                new MinutesPair(101, 102, 0, 0),
                new MinutesPair(102, 103, 0, 0),
                new MinutesPair(104, 105, 0, 0),
                new MinutesPair(105, 106, 0, 0));
        templateResourceList = TemplateResource.getValidFlexGroup(templateResources, templateResources.get(0));
        assertEquals(templateResourceList.size(), 2);
        templateResourceList = TemplateResource.getValidFlexGroup(templateResources, templateResources.get(1));
        assertEquals(templateResourceList.size(), 3);
        templateResourceList = TemplateResource.getValidFlexGroup(templateResources, templateResources.get(2));
        assertEquals(templateResourceList.size(), 3);
        templateResourceList = TemplateResource.getValidFlexGroup(templateResources, templateResources.get(3));
        assertEquals(templateResourceList.size(), 2);
        templateResourceList = TemplateResource.getValidFlexGroup(templateResources, templateResources.get(4));
        assertEquals(templateResourceList.size(), 1);
        templateResourceList = TemplateResource.getValidFlexGroup(templateResources, templateResources.get(5));
        assertEquals(templateResourceList.size(), 1);

        templateResources = testTemplateResourceList(
                false,
                new MinutesPair(99, 100, 0, 0),
                new MinutesPair(99, 100, 0, 0),
                new MinutesPair(100, 101, 0, 0),
                new MinutesPair(101, 102, 0, 0),
                new MinutesPair(101, 102, 0, 0),
                new MinutesPair(102, 103, 0, 0),
                new MinutesPair(104, 105, 0, 0),
                new MinutesPair(105, 106, 0, 0));
        templateResourceList = TemplateResource.getValidFlexGroup(templateResources, templateResources.get(0));
        assertEquals(templateResourceList.size(), 4);
        templateResourceList = TemplateResource.getValidFlexGroup(templateResources, templateResources.get(1));
        assertEquals(templateResourceList.size(), 4);
        templateResourceList = TemplateResource.getValidFlexGroup(templateResources, templateResources.get(2));
        assertEquals(templateResourceList.size(), 5);
        templateResourceList = TemplateResource.getValidFlexGroup(templateResources, templateResources.get(3));
        assertEquals(templateResourceList.size(), 5);
        templateResourceList = TemplateResource.getValidFlexGroup(templateResources, templateResources.get(4));
        assertEquals(templateResourceList.size(), 5);
        templateResourceList = TemplateResource.getValidFlexGroup(templateResources, templateResources.get(5));
        assertEquals(templateResourceList.size(), 3);
    }

    @Test
    public void testIsValidFloatGroup() throws Exception{
        String statusMsg = TemplateResource.isValidFloatGroup(testTemplateResourceList(
                true,
                new MinutesPair(100, 101, 99, 102),
                new MinutesPair(100, 101, 99, 102)));

        assertEquals("OK", statusMsg);

        statusMsg = TemplateResource.isValidFloatGroup(testTemplateResourceList(
                true,
                new MinutesPair(100, 102, 99, 102),
                new MinutesPair(100, 101, 99, 102)));

        assertEquals("The preferred start time and end time of the Float Resources do not match.", statusMsg);

        statusMsg = TemplateResource.isValidFloatGroup(testTemplateResourceList(
                true,
                new MinutesPair(100, 101, 99, 102),
                new MinutesPair(103, 104, 102, 105)));

        assertEquals("The preferred start time and end time of the Float Resources do not match.", statusMsg);

        statusMsg = TemplateResource.isValidFloatGroup(testTemplateResourceList(
                true,
                new MinutesPair(100, 101, 99, 102),
                new MinutesPair(100, 101, 99, 105)));

        assertEquals("The float start time and end time of the Float Resources do not match.", statusMsg);

        statusMsg = TemplateResource.isValidFloatGroup(testTemplateResourceList(
                true,
                new MinutesPair(100, 101, 99, 105),
                new MinutesPair(100, 102, 99, 105)));

        assertEquals("The preferred start time and end time of the Float Resources do not match.", statusMsg);

        List<TemplateResource> resourceList = testTemplateResourceList(
                true,
                new MinutesPair(100, 101, 99, 102),
                new MinutesPair(100, 101, 99, 102));
        resourceList.get(0).setFloatable(false);
        statusMsg = TemplateResource.isValidFloatGroup(resourceList);
        assertEquals("Not all of the Resources are Float Resources.", statusMsg);

    }


    private List<TemplateResource> testTemplateResourceList(boolean setFloatResource, MinutesPair... pairs) {
        List<TemplateResource> templateResourceList = Lists.newArrayList();
        for(MinutesPair minutesPair : pairs) {
            TemplateResource resource = new TemplateResource();
            setStartAndEndTimes(resource, minutesPair.startMinutes, minutesPair.endMinutes);
            if (setFloatResource) {
                setFloatStartAndEndTimes(resource, minutesPair.floatStart, minutesPair.floatEnd);
            }
            templateResourceList.add(resource);
        }
        return templateResourceList;
    }

    private class MinutesPair {
        public int startMinutes;
        public int endMinutes;
        public int floatStart;
        public int floatEnd;

        public MinutesPair(int startMinutes, int endMinutes, int floatStart, int floatEnd) {
            this.startMinutes = startMinutes;
            this.endMinutes = endMinutes;
            this.floatStart = floatStart;
            this.floatEnd = floatEnd;
        }
    }

    private void setStartAndEndTimes(TemplateResource templateResource, final int startMinutes, final int endMinutes) {
        templateResource.setStartMinutes(startMinutes);
        templateResource.setEndMinutes(endMinutes);
        templateResource.setDuration(endMinutes - startMinutes);
    }

    private void setFloatStartAndEndTimes(TemplateResource templateResource,
                                          final int floatStart, final int floatEnd) {
        templateResource.setFloatStart(floatStart);
        templateResource.setFloatEnd(floatEnd);
    }

    @Test
    public void testInit() throws Exception {
        TemplateResource templateResource = new TemplateResource();
        TemplateResourceDTO dto = testDto();
        boolean returnValue = templateResource.possiblyUpdateMyNonResourceFields(dto);
        assertEquals(true, returnValue);
        assertEquals(dto.isAlternate(), templateResource.getAlternate());
        assertEquals(dto.isBillable(), templateResource.getBillable());
        assertEquals(Boolean.valueOf(dto.getFlexible()), templateResource.getFlexible());
        assertEquals(Boolean.valueOf(dto.getFloatable()), templateResource.getFloatable());
        assertEquals(dto.getStartMinutes(), templateResource.getStartMinutes());
        assertEquals(dto.getEndMinutes(), templateResource.getEndMinutes());
    }

    @Test
    public void initWithMatchingDto() {

        TemplateResource templateResource = new TemplateResource();
        templateResource.setAlternate(true);
        templateResource.setBillable(true);
        templateResource.setFlexible(true);
        templateResource.setFloatable(false);
        templateResource.setFloatStart(1);
        templateResource.setFloatEnd(2);
        templateResource.setStartMinutes(3);
        templateResource.setEndMinutes(3);

        TemplateResourceDTO dto = testDto();
        dto.setAlternate(true);
        dto.setBillable(true);
        dto.setFlexible("true");
        dto.setFloatable("false");
        dto.setFloatStart(1);
        dto.setFloatEnd(2);
        dto.setStartMinutes(3);
        dto.setEndMinutes(3);

        boolean returnValue = templateResource.possiblyUpdateMyNonResourceFields(dto, false);
        assertEquals(false, returnValue);

    }

    private TemplateResourceDTO testDto() {
        TemplateResourceDTO dto = new TemplateResourceDTO();
        dto.setCreatedDate(createdDate);
        dto.setAlternate(isAlternate);
        dto.setBillable(isBillable);
        dto.setFlexible(isFlexible);
        dto.setFloatable(isFloatable);

        Integer createdDateInMinutesSinceOrigin = DateUtility.minutesSinceOrigin(createdDate);
        dto.setStartMinutes(createdDateInMinutesSinceOrigin);
        dto.setEndMinutes(createdDateInMinutesSinceOrigin);

        return dto;
    }

    @Test
    public void testMakeFloatable() throws Exception {
        TemplateResource templateResource = new TemplateResource();
        Integer floatEnd = 20;
        Integer floatStart = 10;
        templateResource.makeFloatable(floatStart, floatEnd);
        assertTrue(templateResource.getFloatable());
        assertEquals(floatStart, templateResource.getFloatStart());
        assertEquals(floatEnd, templateResource.getFloatEnd());
    }

    @Test
    public void testMakeNotFloatable() throws Exception {
        TemplateResource templateResource = new TemplateResource();
        templateResource.makeNotFloatable();
        assertFalse(templateResource.getFloatable());
        assertNull(templateResource.getFloatStart());
        assertNull(templateResource.getFloatEnd());
    }

    @Test
    public void testAllVisitCalendarDayStrings() throws Exception {

        allVisitCalendarDayStringsHelper(0);
        allVisitCalendarDayStringsHelper(1);
        allVisitCalendarDayStringsHelper(28);
        allVisitCalendarDayStringsHelper(29);
        allVisitCalendarDayStringsHelper(15);
        allVisitCalendarDayStringsHelper(45);
    }
    void allVisitCalendarDayStringsHelper(int delta) {

        TemplateResource tr1 = new TemplateResource();
        TemplateResource tr2 = new TemplateResource();

        tr1.setStartMinutes(0);
        tr1.setEndMinutes(delta * MINS_PER_DAY);

        tr2.setStartMinutes(delta * MINS_PER_DAY);
        tr2.setEndMinutes(delta * MINS_PER_DAY);

        List<TemplateResource> trList = Lists.newArrayList(tr1, tr2);
        List<String> allDaysNewMethod = TemplateResource.allVisitCalendarDayStrings(trList);

        int numDaysNew = allDaysNewMethod.size();

        assertEquals(numDaysNew, delta + 1);
    }

    @Test
    public void testStartDayOffsetMatches() throws Exception {
        testStartDayOffsetHelper(0);
        testStartDayOffsetHelper(1);
        testStartDayOffsetHelper(15);
        testStartDayOffsetHelper(28);
        testStartDayOffsetHelper(29);
        testStartDayOffsetHelper(45);
    }

    void testStartDayOffsetHelper(int delta) {

        int startMinutes = delta * MINS_PER_DAY;
        TemplateResource tr = new TemplateResource();
        tr.setStartMinutes(startMinutes);

        assertTrue(tr.startDayOffsetMatches(delta + 1));

    }

    @Test
    public void getOneDayDurationTest() throws Exception {
        getOneDayDurationTestHelper(10, 0, 25, 0,   0, 15, null);
        getOneDayDurationTestHelper(10, 0, 25, 1,   0, DateUtility.MINS_PER_DAY - 10, null);
        getOneDayDurationTestHelper(10, 0, 25, 1,   1, 25, null);
        getOneDayDurationTestHelper(10, 0, 25, 2,   1, DateUtility.MINS_PER_DAY, null);
        getOneDayDurationTestHelper(10, 0, 25, 2,   4, 0, null);
        // I'm unable to find a combination of parameters which generates an exception
//        getOneDayDurationTestHelper(0, 10, 0, 0,   -1, 0, "Unexpected combination of startMinutes, endMinutes, dayOffset in getOneDayDuration()");
    }

    void getOneDayDurationTestHelper(int startMinutes, int startDayDelta,
                                     int endMinutes, int endDayDelta,
                                     int dayOffsetZeroBased,
                                     int expectedDuration, String expectedExceptionMsg) throws Exception {
        LocalDateTime originLdt = DateUtility.TEMPLATE_RESOURCE_LOCAL_DATE_TIME_ORIGIN;

        LocalDateTime startLdt = originLdt.plusDays(startDayDelta).plusMinutes(startMinutes);
        LocalDateTime endLdt = originLdt.plusDays(endDayDelta).plusMinutes(endMinutes);

        TemplateResource tr = new TemplateResource();
        tr.setStartMinutes(minutesSinceOrigin(DateUtility.toDate(startLdt)));
        tr.setEndMinutes(minutesSinceOrigin(DateUtility.toDate(endLdt)));

        if (expectedExceptionMsg == null) {
            int duration = tr.getOneDayDuration(dayOffsetZeroBased + 1);
            assertEquals(expectedDuration, duration);
        }
        else {
            try {
                int duration = tr.getOneDayDuration(dayOffsetZeroBased + 1);
                // should not be here
                assertFalse("This code should never be reached - duration = " + duration, true);
            }
            catch (SchedulerRuntimeException e) {
                assertEquals(expectedExceptionMsg, e.getMessage());
            }
        }
    }

    @Test
    public void testOccursInOneDay()
    {
        TemplateResource templateResource = new TemplateResource();

        LocalDateTime originLdt = DateUtility.TEMPLATE_RESOURCE_LOCAL_DATE_TIME_ORIGIN;

        LocalDateTime startLdt = originLdt.plusDays(1);
        LocalDateTime endLdt = originLdt.plusDays(2);

        Date day = DateUtility.toDate(startLdt);
        Date dayAfter = DateUtility.toDate(endLdt);

        templateResource.setStartMinutes(minutesSinceOrigin(day));
        templateResource.setEndMinutes(minutesSinceOrigin(day));
        templateResource.setFloatable(false);

        boolean occursInOneDay = templateResource.occursInOneDay();
        assertTrue("main and float in one day", occursInOneDay);

        templateResource.setFloatable(true);
        templateResource.setFloatStart(30);
        templateResource.setFloatEnd(60);

        occursInOneDay = templateResource.occursInOneDay();
        assertTrue(occursInOneDay);

        templateResource.setStartMinutes(minutesSinceOrigin(day));
        templateResource.setEndMinutes(minutesSinceOrigin(dayAfter));
        templateResource.setFloatable(true);
        templateResource.setFloatStart(30);
        templateResource.setFloatEnd(1800);

        occursInOneDay = templateResource.occursInOneDay();
        assertFalse("main and float not in one day", occursInOneDay);

        templateResource.setStartMinutes(minutesSinceOrigin(day));
        templateResource.setEndMinutes(minutesSinceOrigin(dayAfter));
        templateResource.setFloatable(true);
        templateResource.setFloatStart(30);
        templateResource.setFloatEnd(60);

        occursInOneDay = templateResource.occursInOneDay();
        assertFalse("main not in one day and float in one day", occursInOneDay);

        templateResource.setStartMinutes(minutesSinceOrigin(day));
        templateResource.setEndMinutes(minutesSinceOrigin(day));
        templateResource.setFloatable(true);
        templateResource.setFloatStart(30);
        templateResource.setFloatEnd(1800);

        occursInOneDay = templateResource.occursInOneDay();
        assertFalse("float not in one day", occursInOneDay);

        templateResource.setStartMinutes(minutesSinceOrigin(day));
        templateResource.setEndMinutes(minutesSinceOrigin(day));
        templateResource.setFloatable(false);

        occursInOneDay = templateResource.occursInOneDay();
        assertTrue("main in one day and not floatable", occursInOneDay);
    }

    @Test
    public void testResourceOverlapsDayOffset()
    {
        LocalDateTime originLdt = DateUtility.TEMPLATE_RESOURCE_LOCAL_DATE_TIME_ORIGIN;
        LocalDateTime startLdt = originLdt.plusDays(1);
        LocalDateTime endLdt = originLdt.plusDays(20);

        Date startDay = DateUtility.toDate(startLdt);
        Date endDay = DateUtility.toDate(endLdt);

        TemplateResource templateResource = new TemplateResource();
        templateResource.setStartMinutes(minutesSinceOrigin(startDay));
        templateResource.setEndMinutes(minutesSinceOrigin(endDay));
        templateResource.setFloatable(true);
        templateResource.setFloatStart(20);
        templateResource.setFloatEnd(45);

        boolean resourceOverlaps = templateResource.resourceOverlapsDayOffset(5);
        assertTrue("resource overlaps main and floatable", resourceOverlaps);

        templateResource.setStartMinutes(minutesSinceOrigin(startDay));
        templateResource.setEndMinutes(minutesSinceOrigin(endDay));
        templateResource.setFloatable(false);

        resourceOverlaps = templateResource.resourceOverlapsDayOffset(7);
        assertTrue("resource overlaps main and not floatable", resourceOverlaps);

        templateResource.setStartMinutes(minutesSinceOrigin(startDay));
        templateResource.setEndMinutes(minutesSinceOrigin(endDay));
        templateResource.setFloatable(false);

        resourceOverlaps = templateResource.resourceOverlapsDayOffset(60);
        assertFalse("resource does not overlap main and not floatable", resourceOverlaps);

        templateResource.setStartMinutes(minutesSinceOrigin(startDay));
        templateResource.setEndMinutes(minutesSinceOrigin(endDay));
        templateResource.setFloatable(true);
        templateResource.setFloatStart(20);
        templateResource.setFloatEnd(45);
        templateResource.setFloatable(false);

        resourceOverlaps = templateResource.resourceOverlapsDayOffset(60);
        assertFalse("resource does not overlap main and floatable", resourceOverlaps);
    }

    @Test
    public void testDetermineAnnotationsString()
    {
        final List<TemplateResourceAnnotations> traList = new ArrayList<>();

        TemplateResourceAnnotations tra1 = new TemplateResourceAnnotations();
        tra1.setComment("template resource annotation 1");
        tra1.setQuantity(4);

        LineLevelAnnotations lineLevelAnn = new LineLevelAnnotations();
        lineLevelAnn.setComment("line level annotation 1");
        lineLevelAnn.setName("first line level");
        tra1.setLineLevelAnnotations(lineLevelAnn);
        traList.add(tra1);

        TemplateResourceAnnotations tra2 = new TemplateResourceAnnotations();
        tra2.setComment("template resource annotation 2");
        tra2.setQuantity(21);

        LineLevelAnnotations lineLevelAnn2 = new LineLevelAnnotations();
        lineLevelAnn2.setComment("line level annotation 2");
        lineLevelAnn2.setName("second line level");
        tra2.setLineLevelAnnotations(lineLevelAnn2);
        traList.add(tra2);

        boolean addQuantities = true;

        String expectAnnotations = "first line level (4), second line level (21)";

        TemplateResource templateResource = new TemplateResource();
        templateResource.determineAnnotationsString(traList, addQuantities);
        String annotations = templateResource.getAnnotations();

        assertEquals("annotations with quantities", expectAnnotations, annotations);

        addQuantities = false;
        String expectAnnotations2 = "first line level, second line level";
        TemplateResource templateResource2 = new TemplateResource();
        templateResource2.determineAnnotationsString(traList, addQuantities);

        String annotations2 = templateResource2.getAnnotations();

        assertEquals("annotations without quantities", expectAnnotations2, annotations2);
    }

    @Test
    public void testStartAndEndDatesAndDuration() {

        // reference:
        final long referenceMilli = DateUtility.TEMPLATE_RESOURCE_DATE_ORIGIN.getTime();

        // Start date:

        // offset: 100 days
        final int startOffsetMinutes = 60 * 24 * 100;
        final long startOffsetMilli = 1000l * 60l * (long)startOffsetMinutes;
        // reference + offset
        Date expectedStoredStartDate = new Date(referenceMilli + startOffsetMilli);
        // reference + offset + 1 nanosecond
        Date startDate = new Date(referenceMilli + startOffsetMilli + 1);
        int expectedStartMinutes = startOffsetMinutes;

        TemplateResource templateResource = new TemplateResource();
        templateResource.setStartMinutes(minutesSinceOrigin(startDate));

        // Keep testing TemplateResource.getStartDate(), as long as that
        // method is used by the scheduling algorithm
        Date retrievedStartDate = templateResource.getStartDate();
        assertEquals(expectedStoredStartDate, retrievedStartDate);

        long retrievedStartMinutes = templateResource.getStartMinutes();
        assertEquals(expectedStartMinutes, retrievedStartMinutes);

        // End date:

        // offset: 200 days. Should straddle transition to daylight savings time
        // and hence prove that TemplateResource.duration is not affected by it
        final int endOffsetMinutes = 60 * 24 * 200;
        final long endOffsetMilli = 1000l * 60l * (long)endOffsetMinutes;
        // reference + offset
        Date expectedStoredEndDate = new Date(referenceMilli + endOffsetMilli);
        // reference + offset + 1 nanosecond
        Date endDate = new Date(referenceMilli + endOffsetMilli + 1);
        int expectedEndMinutes = endOffsetMinutes;

        templateResource.setEndMinutes(0);
        templateResource.setEndMinutes(minutesSinceOrigin(endDate));

        Date retrievedEndDate = templateResource.getEndDate();
        assertEquals(expectedStoredEndDate, retrievedEndDate);

        long retrievedEndMinutes = templateResource.getEndMinutes();
        assertEquals(expectedEndMinutes, retrievedEndMinutes);

        // Duration:

        templateResource.calculateDuration();
        int expectedDuration = endOffsetMinutes - startOffsetMinutes;
        assertEquals(templateResource.getDuration().intValue(), expectedDuration);

    }

    @Test
    public void testCalculateDuration() {

        TemplateResource templateResource = new TemplateResource();

        templateResource.calculateDuration();

        assertNull(templateResource.getDuration());

        templateResource.setStartMinutes(1);
        templateResource.setEndMinutes(2);
        templateResource.calculateDuration();

        assertEquals(Integer.valueOf(1), templateResource.getDuration());

    }

    @Test
    public void testGetStartDate() {

        TemplateResource templateResource = new TemplateResource();

        Date startDate = templateResource.getStartDate();

        assertNull(startDate);

        templateResource.setStartMinutes(10);

        startDate = templateResource.getStartDate();

        assertEquals(
                new Date(TEMPLATE_RESOURCE_DATE_ORIGIN.getTime() + 10 * 60 * 1000),
                startDate
        );

    }

    @Test
    public void testGetEndDate() {

        TemplateResource templateResource = new TemplateResource();

        Date endDate = templateResource.getEndDate();

        assertNull(endDate);

        templateResource.setEndMinutes(10);

        endDate = templateResource.getEndDate();

        assertEquals(
                new Date(TEMPLATE_RESOURCE_DATE_ORIGIN.getTime() + 10 * 60 * 1000),
                endDate
        );

    }

    @Test
    public void testTransients() {

        TemplateResource templateResource = new TemplateResource();

        templateResource.setFloatable(false);
        templateResource.setGroupId(null);
        templateResource.setFlexible(false);

        assertFalse(templateResource.getUngroupedFloatable());
        assertFalse(templateResource.getUngroupedFlexible());
        assertFalse(templateResource.getGroupedFloatable());
        assertFalse(templateResource.getGroupedFlexible());

        templateResource.setFloatable(true);
        templateResource.setGroupId(null);
        templateResource.setFlexible(false);

        assertTrue(templateResource.getUngroupedFloatable());
        assertFalse(templateResource.getUngroupedFlexible());
        assertFalse(templateResource.getGroupedFloatable());
        assertFalse(templateResource.getGroupedFlexible());

        templateResource.setFloatable(true);
        templateResource.setGroupId(null);
        templateResource.setFlexible(true);

        assertTrue(templateResource.getUngroupedFloatable());
        assertTrue(templateResource.getUngroupedFlexible());
        assertFalse(templateResource.getGroupedFloatable());
        assertFalse(templateResource.getGroupedFlexible());

        templateResource.setFloatable(true);
        templateResource.setGroupId("1");
        templateResource.setFlexible(true);

        assertFalse(templateResource.getUngroupedFloatable());
        assertFalse(templateResource.getUngroupedFlexible());
        assertTrue(templateResource.getGroupedFloatable());
        assertTrue(templateResource.getGroupedFlexible());

        templateResource.setFloatable(false);
        templateResource.setGroupId("1");
        templateResource.setFlexible(true);

        assertFalse(templateResource.getUngroupedFloatable());
        assertFalse(templateResource.getUngroupedFlexible());
        assertFalse(templateResource.getGroupedFloatable());
        assertTrue(templateResource.getGroupedFlexible());

        templateResource.setFloatable(false);
        templateResource.setGroupId("1");
        templateResource.setFlexible(false);

        assertFalse(templateResource.getUngroupedFloatable());
        assertFalse(templateResource.getUngroupedFlexible());
        assertFalse(templateResource.getGroupedFloatable());
        assertFalse(templateResource.getGroupedFlexible());

        Date date1 = new Date(1);
        templateResource.setScheduledStartTime(date1);
        Date date2 = new Date(2);
        templateResource.setScheduledEndTime(date2);
        templateResource.setAvailableStartTimeInMin(3);
        templateResource.setAvailableEndTimeInMin(4);

        assertEquals(date1, templateResource.getScheduledStartTime());
        assertEquals(date2, templateResource.getScheduledEndTime());
        assertEquals(3, templateResource.getAvailableStartTimeInMin());
        assertEquals(4, templateResource.getAvailableEndTimeInMin());

        templateResource.setAlternateResourceUsed(true);
        assertTrue(templateResource.getAlternateResourceUsed());

        templateResource.setRejectedResourceMessage("my 2 cents");
        assertEquals("my 2 cents", templateResource.getRejectedResourceMessage());

        templateResource.setAvailable("??");
        assertEquals("??", templateResource.getAvailable());

    }

    @Test
    public void testCloneTemplateResource() {

        TemplateResource templateResource = new TemplateResource();

        TemplateResource clonedResource = templateResource.cloneTemplateResource();

        assertEquals(templateResource.getStartMinutes(), clonedResource.getStartMinutes());

    }

    @Test
    public void testToString() {

        TemplateResource templateResource = new TemplateResource();

        templateResource.setId(9);

        assertEquals("TemplateResource [id=" + templateResource.id + ", getId()=" + templateResource.getId() + "]",
                templateResource.toString()
        );

    }

    @Test
    public void testIsGrouped() {

        TemplateResource templateResource = new TemplateResource();

        assertFalse(templateResource.isGrouped());

        templateResource.setGroupId("000");

        assertTrue(templateResource.isGrouped());

    }

    @Test
    public void testTotalVisitCalendarDays() {

        TemplateResource templateResource = new TemplateResource();

        int days = TemplateResource.totalVisitCalendarDays(new ArrayList<TemplateResource>());

        assertEquals(0, days);

    }

}
