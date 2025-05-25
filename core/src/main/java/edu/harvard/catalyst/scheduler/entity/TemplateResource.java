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

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import edu.harvard.catalyst.hccrc.core.util.Range;
import edu.harvard.catalyst.scheduler.core.SchedulerRuntimeException;
import edu.harvard.catalyst.scheduler.dto.TemplateResourceDTO;
import edu.harvard.catalyst.scheduler.util.DateUtility;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static edu.harvard.catalyst.hccrc.core.util.RichList.enrich;
import static edu.harvard.catalyst.scheduler.util.DateUtility.*;


@Entity
@Table(name = "template_resource")
public class TemplateResource extends BaseEntity implements Serializable, Cloneable {
    private static final long serialVersionUID = 1L;
    private VisitTemplate visitTemplate;
    private Resource resource;
    private boolean alternate;
    private boolean billable;
    private Integer duration;
    private Integer floatStart;
    private Integer floatEnd;
    private Date createdDate;
    private Date lastUpdateTime;
    private Boolean floatable;
    private Boolean flexible;
    private Integer startMinutes;
    private Integer endMinutes;
    private String groupId;
    private Date scheduledStartTime;
    private Date scheduledEndTime;
    private int availableStartTimeInMin;
    private int availableEndTimeInMin;
    private Boolean alternateResourceUsed;
    private String annotations;
    private String rejectedResourceMessage;
    private String available;
    private String resourceGroupType;

    public TemplateResource() {
        super(null);
    }

    public TemplateResource(final TemplateResourceDTO trDto, final Resource resource, final VisitTemplate visitTemplate) {
        super(null);

        this.setResource(resource);
        this.setVisitTemplate(visitTemplate);

        Date now = new Date();
        this.setCreatedDate(now);
        this.setLastUpdateTime(now);

        possiblyUpdateMyNonResourceFields(trDto, true);
    }

    final public boolean possiblyUpdateMyNonResourceFields(final TemplateResourceDTO trDto) {
        return possiblyUpdateMyNonResourceFields(trDto, false);
    }

    final public boolean possiblyUpdateMyNonResourceFields(final TemplateResourceDTO trDto, boolean creationSoGoForIt) {

        if ( ! creationSoGoForIt && trDto.matches(this)) {
            return false;
        }

        setAlternate(trDto.isAlternate());
        setBillable(trDto.isBillable());
        setFlexible(trDto.isFlexible());

        if(trDto.isFloatable()) {
            makeFloatable(trDto.getFloatStart(), trDto.getFloatEnd());
        }
        else {
            makeNotFloatable();
        }
        setStartMinutes(trDto.getStartMinutes());
        setEndMinutes(trDto.getEndMinutes());

        this.calculateDuration();

        return true;
    }

    void calculateDuration() {
        if (this.startMinutes != null && this.endMinutes != null) {
            Integer minutesFromStartToEnd = this.endMinutes - this.startMinutes;
            this.setDuration(minutesFromStartToEnd);
        }
        else {
            duration = null;
        }
    }

    private boolean sameStart(final TemplateResource next) {
        return getStartMinutes().intValue() == next.getStartMinutes().intValue();
    }

    private boolean tangentBefore(final TemplateResource next) {
        return getEndMinutes().intValue() == next.getStartMinutes().intValue();
    }

    public void makeNotFloatable() {
        updateFloatability(Boolean.FALSE, null, null);
    }

    public void makeFloatable(final Integer floatStart, final Integer floatEnd) {
        updateFloatability(Boolean.TRUE, floatStart, floatEnd);
    }

    private void updateFloatability(final Boolean f, final Integer start, final Integer end) {
        this.setFloatable(f);
        this.setFloatStart(start);
        this.setFloatEnd(end);
    }

    @JoinColumn(name = "visit_template", referencedColumnName = "id")
    @ManyToOne(optional = false)
    public VisitTemplate getVisitTemplate() {
        return visitTemplate;
    }

    public void setVisitTemplate(final VisitTemplate visitTemplate) {
        this.visitTemplate = visitTemplate;
    }

    @JoinColumn(name = "resource", referencedColumnName = "id")
    @ManyToOne(optional = false)
    public Resource getResource() {
        return resource;
    }

    public void setResource(final Resource resource) {
        this.resource = resource;
    }

    @Column(name = "alternate")
    public boolean getAlternate() {
        return alternate;
    }

    public void setAlternate(final boolean alternate) {
        this.alternate = alternate;
    }

    @Column(name = "billable")
    public boolean getBillable() {
        return billable;
    }

    public void setBillable(final boolean billable) {
        this.billable = billable;
    }

    @Column(name = "duration")
    public Integer getDuration() {
        return duration;
    }
    public void setDuration(final Integer duration) {
        this.duration = duration;
    }

    @Column(name = "float_start")
    public Integer getFloatStart() {
        return floatStart;
    }

    public void setFloatStart(final Integer floatStart) {
        this.floatStart = floatStart;
    }

    @Column(name = "float_end")
    public Integer getFloatEnd() {
        return floatEnd;
    }

    public void setFloatEnd(final Integer floatEnd) {
        this.floatEnd = floatEnd;
    }

    @Column(name = "created_date")
    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(final Date createdDate) {
        this.createdDate = createdDate;
    }

    @Column(name = "last_update_time")
    public Date getLastUpdateTime() {
        return lastUpdateTime;
    }

    public void setLastUpdateTime(final Date lastUpdateTime) {
        this.lastUpdateTime = lastUpdateTime;
    }

    @Column(name = "floatable")
    public Boolean getFloatable() {
        return floatable;
    }

    public void setFloatable(final Boolean floatable) {
        this.floatable = floatable;
    }

    @Column(name = "flexible")
    public Boolean getFlexible() {
        return flexible;
    }

    public void setFlexible(final Boolean flexible) {
        this.flexible = flexible;
    }

    @Transient
    public Date getStartDate() {
        // re-calculate endDate every time. This is temporary code
        // which will go away when the refactoring to integer numbers of minutes
        // is done across the code base.
        if (this.startMinutes != null) {
            return DateUtility.originDatePlusMinutes(startMinutes);
        }
        else {
            return null;
        }
    }

    @Transient
    public Date getEndDate() {
        // re-calculate endDate every time. This is temporary code
        // which will go away when the refactoring to integer numbers of minutes
        // is done across the code base.
        if (this.endMinutes != null) {
            return DateUtility.originDatePlusMinutes(endMinutes);
        }
        else {
            return null;
        }
    }

    @Column(name = "start_minutes")
    public Integer getStartMinutes() {
        return startMinutes;
    }

    public void setStartMinutes(final Integer startMinutes) {
        this.startMinutes = startMinutes;
    }

    @Column(name = "end_minutes")
    public Integer getEndMinutes() {
        return endMinutes;
    }

    public void setEndMinutes(final Integer endMinutes) {
        this.endMinutes = endMinutes;
    }

    @Column(name = "group_id")
    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(final String groupId) {
        this.groupId = groupId;
    }

    @Transient
    public boolean getUngroupedFloatable() {
        return floatable && groupId == null;
    }
    @Transient
    public boolean getUngroupedFlexible() {
        return flexible && groupId == null;
    }
    @Transient
    public boolean getGroupedFloatable() {
        return floatable && groupId != null;
    }
    @Transient
    public boolean getGroupedFlexible() {
        return flexible && groupId != null;
    }

    @Transient
    public Date getScheduledEndTime() {
        return scheduledEndTime;
    }

    public void setScheduledEndTime(final Date scheduledEndTime) {
        this.scheduledEndTime = scheduledEndTime;
    }

    @Transient
    public Date getScheduledStartTime() {
        return scheduledStartTime;
    }

    public void setScheduledStartTime(final Date scheduledStartTime) {
        this.scheduledStartTime = scheduledStartTime;
    }

    @Transient
    public int getAvailableEndTimeInMin() {
        return availableEndTimeInMin;
    }

    public void setAvailableEndTimeInMin(final int availableEndTimeInMin) {
        this.availableEndTimeInMin = availableEndTimeInMin;
    }

    @Transient
    public int getAvailableStartTimeInMin() {
        return availableStartTimeInMin;
    }

    public void setAvailableStartTimeInMin(final int availableStartTimeInMin) {
        this.availableStartTimeInMin = availableStartTimeInMin;
    }

    @Transient
    public Boolean getAlternateResourceUsed() {
        return alternateResourceUsed;
    }

    public void setAlternateResourceUsed(final Boolean alternateResourceUsed) {
        this.alternateResourceUsed = alternateResourceUsed;
    }

    @Transient
    public String getAnnotations() {
        return annotations;
    }

    public void setAnnotations(final String annotations) {
        this.annotations = annotations;
    }

    @Transient
    public String getRejectedResourceMessage() {
        return rejectedResourceMessage;
    }

    public void setRejectedResourceMessage(final String rejectedResourceMessage) {
        this.rejectedResourceMessage = rejectedResourceMessage;
    }

    @Transient
    public String getAvailable() {
        return available;
    }

    public void setAvailable(final String available) {
        this.available = available;
    }

    @Transient
    public String getResourceGroupType() {
        return resourceGroupType;
    }

    public void setResourceGroupType(final String resourceGroupType) {
        this.resourceGroupType = resourceGroupType;
    }

    public TemplateResource cloneTemplateResource() {
        TemplateResource result = null;

        try {
            result = (TemplateResource) super.clone();
        } catch(final CloneNotSupportedException exception) {
            SchedulerRuntimeException.logAndThrow("Couldn't clone TemplateResource '" + getId() + "'", exception);
        }

        return result;
    }

    @Override
    public String toString() {
        return "TemplateResource [id=" + id + ", getId()=" + getId() + "]";
    }

    public static <T> boolean allTheSame(final List<TemplateResource> list, final Function<TemplateResource, T> function) {
        return enrich(list).map(function).toSet().size() == 1;
    }

    public static String isValidFlexGroup(final List<TemplateResource> templateResourceList) {

        if (allTheSame(templateResourceList, TemplateResource::getStartMinutes)) {
            return "At least one resource must not share the same start time";
        }

        if ( ! allTheSame(templateResourceList, TemplateResource::getDuration)) {
            return "The duration of all the Flex Resources should be the same.";
        }

        if ( ! allTheSame(templateResourceList, TemplateResource::getFlexible)) {
            return "Not all of the Resources are Flex Resources.";
        }

        Collections.sort(templateResourceList, TemplateResourceComparator);

        for(int i = 0; i < templateResourceList.size() - 1; i++) {
            final TemplateResource current = templateResourceList.get(i);
            final TemplateResource next = templateResourceList.get(i + 1);

            if(!(current.tangentBefore(next) || current.sameStart(next))) {
                return "There is a time gap between the Flex Resources / does not meet the flex link criteria, hence cannot be linked.";
            }
        }
        return "OK";
    }

    public static List<TemplateResource> getValidFlexGroup(final List<TemplateResource> templateResourceList, final TemplateResource aspirantResource) {

        Collections.sort(templateResourceList, TemplateResourceComparator);
        Collection<TemplateResource> sameDurationResources = templateResourceList.stream()
                .filter(s -> s.getDuration().equals(aspirantResource.getDuration()))
                .collect(Collectors.toList());

        List<TemplateResource> initialCandidateResources = new ArrayList<>();
        List<Integer> possibleFlexTimes = aspirantResource.getFlexTimes();

        for(TemplateResource templateResource : sameDurationResources) {
            final int currentMinutes = templateResource.getStartMinutes();

            if (possibleFlexTimes.contains(currentMinutes)) {
                initialCandidateResources.add(templateResource);
            }
        }

        Collection<TemplateResource> nonDuplicatedResources = initialCandidateResources.stream()
                .<Map<Integer, TemplateResource>>collect(HashMap::new,(m,e)->m.put(e.getStartMinutes(), e), Map::putAll)
                .values();

        List<TemplateResource> tr = Lists.newArrayList(nonDuplicatedResources);
        Collections.sort(tr, TemplateResourceComparator);

        List<Integer> finalTimeList = Lists.newArrayList();
        for(int i = 0; i < tr.size() - 1; i++) {
            final TemplateResource current = tr.get(i);
            final TemplateResource next = tr.get(i + 1);

            if(current.tangentBefore(next)) {
                finalTimeList.add(current.getStartMinutes());
                finalTimeList.add(next.getStartMinutes());
            }
        }

        Collection<TemplateResource> candidateResourcesCollection = initialCandidateResources.stream()
                .filter(s -> finalTimeList.contains(s.getStartMinutes()))
                .collect(Collectors.toList());
        List<TemplateResource> candidateResources = Lists.newArrayList(candidateResourcesCollection);
        candidateResources.remove(aspirantResource);
        Collections.sort(candidateResources, TemplateResourceComparator);
        return candidateResources;
    }

    @Transient
    private List<Integer> getFlexTimes() {
        List<Integer> possibleFlexTimes = Lists.newArrayList();
        Integer resourceDuration = this.getDuration();

        int aspirantStartMinutes = this.getStartMinutes();
        possibleFlexTimes.add(aspirantStartMinutes);

        int oneBeforeAspirantStartMinutes = this.getStartMinutes() - resourceDuration;
        possibleFlexTimes.add(oneBeforeAspirantStartMinutes);

        int twoBeforeAspirantStartMinutes = oneBeforeAspirantStartMinutes - resourceDuration;
        possibleFlexTimes.add(twoBeforeAspirantStartMinutes);

        int oneAfterAspirantStartMinutes = this.getStartMinutes() + resourceDuration;
        possibleFlexTimes.add(oneAfterAspirantStartMinutes);

        int twoAfterAspirantStartMinutes = oneAfterAspirantStartMinutes + resourceDuration;
        possibleFlexTimes.add(twoAfterAspirantStartMinutes);

        return possibleFlexTimes;
    }

    public static String isValidFloatGroup(final List<TemplateResource> templateResourceList) {
        if ( ! (allTheSame(templateResourceList, TemplateResource::getStartMinutes) &&
                allTheSame(templateResourceList, TemplateResource::getEndMinutes))) {
            return "The preferred start time and end time of the Float Resources do not match.";
        }

        if ( ! (allTheSame(templateResourceList, TemplateResource::getFloatStart) &&
                allTheSame(templateResourceList, TemplateResource::getFloatEnd))) {
            return "The float start time and end time of the Float Resources do not match.";
        }

        if ( ! allTheSame(templateResourceList, TemplateResource::getFloatable)) {
            return "Not all of the Resources are Float Resources.";
        }

        return "OK";
    }


    static final Comparator<TemplateResource> TemplateResourceComparator = (o1, o2) -> o1.getStartMinutes().compareTo(o2.getStartMinutes());

    static int minutesFromOriginToDayNumber(int minutes) {
        int daysFromOrigin = minutes / MINS_PER_DAY;
        return 1 + daysFromOrigin;
    }

    @Transient
    public int getOneDayDuration(final int dayOffset) {
        final int startDayOffset = minutesFromOriginToDayNumber(getStartMinutes());
        final int endDayOffset = minutesFromOriginToDayNumber(getEndMinutes());

        final int startMinutes = minutesIntoDay(getStartMinutes());
        final int endMinutes = minutesIntoDay(getEndMinutes());

        final int result;

        if (endDayOffset < dayOffset || startDayOffset > dayOffset) {
            // no overlap
            result = 0;
        }
        else if (startDayOffset < dayOffset && endDayOffset == dayOffset) {
            // overlap from left / earlier
            result = endMinutes;
        }
        else if (startDayOffset == dayOffset && endDayOffset > dayOffset) {
            // overlap from right / later
            result = MINS_PER_DAY - startMinutes;
        }
        else if (startDayOffset < dayOffset && endDayOffset > dayOffset) {
            result = MINS_PER_DAY;
        }
        else if (startDayOffset == dayOffset && endDayOffset == dayOffset) {
            result = endMinutes - startMinutes;
        }
        else {
            result = 0;
            SchedulerRuntimeException.logAndThrow("Unexpected combination of startMinutes, endMinutes, dayOffset in getOneDayDuration()");
        }
        return result;
    }

    public void determineAnnotationsString(final List<TemplateResourceAnnotations> traList, final boolean addQuantities) {

        final List<String> annotationStringList = traList.stream()
                .map(tra -> addQuantities ? tra.getQuantifiedLlaName() : tra.getLineLevelAnnotations().getName())
                .collect(Collectors.toList());

        annotationStringList.sort(Comparator.<String>naturalOrder());

        setAnnotations(Joiner.on(", ").join(annotationStringList));
    }

    public boolean occursInOneDay() {

        boolean mainStartEndAreSameDay = earlierAndLaterAreSameLocalDay(startMinutes, endMinutes);

        boolean floatSameDay = true;
        if (floatable) {
            floatSameDay = earlierAndLaterAreSameLocalDay(floatStart, floatEnd);
        }

        return mainStartEndAreSameDay && floatSameDay;
    }

    public boolean startDayOffsetMatches(int desiredOffset) {
        return minutesFromOriginToDayNumber(startMinutes) == desiredOffset;
    }

    public boolean resourceOverlapsDayOffset(int desiredDaysOffset) {

        boolean mainDatesOverlap = minutesIntervalOverlapsDaysOffset(startMinutes, endMinutes, desiredDaysOffset);

        boolean floatDatesOverlap = false; // burden of proof
        if (floatable) {
            floatDatesOverlap = minutesIntervalOverlapsDaysOffset(floatStart, floatEnd, desiredDaysOffset);
        }

        return mainDatesOverlap || floatDatesOverlap;
    }

    @Transient
    public boolean isGrouped() {
        return groupId != null;
    }

    public static int totalVisitCalendarDays(List<TemplateResource> allVisitTrs) {
        if (allVisitTrs.isEmpty()) {
            return 0;
        }
        Integer latestEndMinutes = allVisitTrs.stream()
                .map(tr -> tr.getEndMinutes() == null ? 0 : tr.getEndMinutes())
                .max(Comparator.<Integer>naturalOrder())
                .get();

        Integer latestFloatEndMinutes = allVisitTrs.stream()
                .map(tr -> tr.getFloatEnd() == null ? 0 : tr.getFloatEnd())
                .max(Comparator.<Integer>naturalOrder())
                .get();

        int latestEndMinutesAtStartOfDay = (latestEndMinutes / MINS_PER_DAY) * MINS_PER_DAY;
        int latestFloatEndMinutesAtStartOfDay = (latestFloatEndMinutes / MINS_PER_DAY) * MINS_PER_DAY;

        int latestEnd = latestEndMinutesAtStartOfDay < latestFloatEndMinutesAtStartOfDay ?
                latestFloatEndMinutesAtStartOfDay :
                latestEndMinutesAtStartOfDay;

        return 1 + latestEnd / MINS_PER_DAY;
    }

    public static List<String> allVisitCalendarDayStrings(List<TemplateResource> allVisitTrs) {
        int totalDays = totalVisitCalendarDays(allVisitTrs);

        LocalDate relativeOrigin = TEMPLATE_RESOURCE_LOCAL_DATE_ORIGIN;

        return Range.from(0).to(totalDays).toRichList()
                .map(i -> format(dateMonthYear(), toDate(relativeOrigin.plusDays(i))))
                .toList();
    }

}
