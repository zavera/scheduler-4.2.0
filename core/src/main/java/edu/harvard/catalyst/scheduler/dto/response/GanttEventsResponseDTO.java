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
package edu.harvard.catalyst.scheduler.dto.response;

import com.google.gson.Gson;
import edu.harvard.catalyst.hccrc.core.util.RichList;
import edu.harvard.catalyst.scheduler.entity.Resource;
import edu.harvard.catalyst.scheduler.entity.TemplateResource;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.function.Predicate;

import static edu.harvard.catalyst.scheduler.util.DateUtility.*;
import static edu.harvard.catalyst.scheduler.core.SchedulerRuntimeException.logAndThrow;

/**
 * Created with IntelliJ IDEA.
 * User: carl
 * Date: 2/28/14
 * Time: 1:02 PM
 */
public final class GanttEventsResponseDTO extends GanttResponseDTO {
    private final List<GanttEventDetail> ganttEventDetailList;

    // for junit
    public List<GanttEventDetail> getGanttEventDetailList() {
        return ganttEventDetailList;
    }

    public GanttEventsResponseDTO(final List<TemplateResource> allTemplateResourcesFromVisit,
                                  final Predicate<? super TemplateResource> relevancePredicate) {

        final List<TemplateResource> relevantTemplateResourcesFromVisit = getRelevantTemplateResources(allTemplateResourcesFromVisit, relevancePredicate);

        ganttEventDetailList =
                RichList.enrich(relevantTemplateResourcesFromVisit)
                        .map(tr -> new GanttEventDetail(tr))
                        .toList();
    }

    @Override
    public String jsonify(Gson gson) {
        return gson.toJson(ganttEventDetailList);
    }

    public static final class GanttEventDetail {
        private final int id;
        private final int resourceId;
        private final String resourceName;
        private final String groupId;
        private final Boolean billable;
        private final Boolean alternate;
        private final Boolean floatable;
        private final Boolean flexible;
        private final String annotations;
        private final String start;
        private final String end;
        private final String displayStart;
        private final String displayEnd;
        private final int duration;
        private final Integer floatStart;
        private final Integer floatEnd;
        private String displayFloatStart = "";
        private String displayFloatEnd = "";
        private Date floatStartPreferred = null;
        private Date floatEndPreferred = null;
        private Date floatStartDate = null;
        private final String color;

        GanttEventDetail(final TemplateResource templateResource) {
            if (templateResource == null || templateResource.getResource() == null) {
                logAndThrow("TemplateResource and Resource should be non-null");
            }

            Resource itsResource = templateResource.getResource();

            id = templateResource.getId();
            billable = templateResource.getBillable();
            alternate = templateResource.getAlternate();
            boolean isGrouped = templateResource.getGroupId() != null;
            resourceId = isGrouped ? id : itsResource.getId();
            resourceName = itsResource.getName();
            groupId = templateResource.getGroupId();
            floatable = templateResource.getFloatable();
            flexible = templateResource.getFlexible();
            annotations = templateResource.getAnnotations();
            boolean isFloatable = templateResource.getFloatable();
            floatStart = minutesIntoDay(templateResource.getStartMinutes());
            floatEnd = minutesIntoDay(templateResource.getEndMinutes());
            duration = templateResource.getDuration();
            Date templateResourceStartDate = templateResource.getStartDate();
            Date templateResourceEndDate = templateResource.getEndDate();
            templateResourceEndDate.setSeconds(59);
            String startDate = format(dateHourMinSec(), templateResourceStartDate);
            String endDate = format(dateHourMinSec(), templateResourceEndDate);

            displayStart = dateToDisplayDate(templateResourceStartDate);
            displayEnd = dateToDisplayDate(templateResourceEndDate);

            if (isFloatable) {
                final int startDays = convertMinutesToStartDayZeroOffset(templateResource.getFloatStart());
                final int startHours = convertMinutesToHourInDay(templateResource.getFloatStart());
                final int startMinutes = convertTotalMinutesToMinutesInHour(templateResource.getFloatStart());

                final Calendar startCalendar = Calendar.getInstance();
                startCalendar.setTime(TEMPLATE_RESOURCE_DATE_ORIGIN);
                startCalendar.add(Calendar.DATE, startDays);
                startCalendar.set(Calendar.HOUR_OF_DAY, startHours);
                startCalendar.add(Calendar.MINUTE, startMinutes);
                final Date startTime = startCalendar.getTime();

                final int endDays = convertMinutesToStartDayZeroOffset(templateResource.getFloatEnd());
                final int endHours = convertMinutesToHourInDay(templateResource.getFloatEnd());
                final int endMinutes = convertTotalMinutesToMinutesInHour(templateResource.getFloatEnd());

                final Calendar endCalendar = Calendar.getInstance();
                endCalendar.setTime(TEMPLATE_RESOURCE_DATE_ORIGIN);
                endCalendar.add(Calendar.DATE, endDays);
                endCalendar.set(Calendar.HOUR_OF_DAY, endHours);
                endCalendar.add(Calendar.MINUTE, endMinutes);
                endCalendar.add(Calendar.SECOND, 59);
                final Date endTime = endCalendar.getTime();

                floatStartDate = startTime;
                floatStartPreferred = templateResource.getStartDate();
                floatEndPreferred = templateResource.getEndDate();
                startDate = format(dateHourMinSec(), startTime);
                endDate = format(dateHourMinSec(), endTime);

                displayFloatStart = dateToDisplayDate(minutesFromOriginToDate(templateResource.getFloatStart()));
                displayFloatEnd = dateToDisplayDate(minutesFromOriginToDate(templateResource.getFloatEnd()));
            }
            start = startDate;
            end = endDate;
            boolean isFlexible = templateResource.getFlexible();
            color = isFloatable ? "#D8BFD8" : isFlexible ? "#7EB035" : "#F8931D";
        }
        String dateToDisplayDate(Date date) {
            return "Day " + dateToDayNumber(date) + " : "+ format(hourMin(), date);
        }
    }

}
