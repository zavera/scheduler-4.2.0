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
import edu.harvard.catalyst.hccrc.core.util.LazyList;
import edu.harvard.catalyst.hccrc.core.util.Range;
import edu.harvard.catalyst.scheduler.core.Statics;
import edu.harvard.catalyst.scheduler.dto.*;
import edu.harvard.catalyst.scheduler.entity.*;
import edu.harvard.catalyst.scheduler.persistence.*;
import edu.harvard.catalyst.scheduler.util.*;
import org.apache.commons.lang.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;

import static edu.harvard.catalyst.hccrc.core.util.LazyList.lazy;
import static edu.harvard.catalyst.hccrc.core.util.ListUtils.enrich;
import static edu.harvard.catalyst.scheduler.util.DateUtility.*;
import static edu.harvard.catalyst.scheduler.util.MiscUtil.*;


@Component
public class SearchAlgorithmService {

    static final int INCREMENT_FACTOR = 30;

    private final SearchAlgorithmDAO searchAlgorithmDAO;
    private final ResourceDAO resourceDAO;
    private final StudyDAO studyDAO;
    private final SubjectDAO subjectDAO;
    private final TemplateResourceDAO templateResourceDAO;

    @Autowired
    public SearchAlgorithmService(
            final SearchAlgorithmDAO searchAlgorithmDAO,
            final ResourceDAO resourceDAO,
            final StudyDAO studyDAO,
            final SubjectDAO subjectDAO,
            final TemplateResourceDAO templateResourceDAO
    ) {
        this.searchAlgorithmDAO = searchAlgorithmDAO;
        this.resourceDAO = resourceDAO;
        this.studyDAO = studyDAO;
        this.subjectDAO = subjectDAO;
        this.templateResourceDAO = templateResourceDAO;
    }

    // Don't use - Needed for spring security cglib proxying
    SearchAlgorithmService() {
        this(null, null, null, null, null);
    }

    ////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////

    void setBookedVisits(final boolean confirmEvent,
                         final boolean rejectedCheck,
                         final User user,
                         final List<BookedVisit> candidateVisits) {

        if (!rejectedCheck && confirmEvent) {
            user.setBookedVisits(candidateVisits);
        } else {
            if (user.getBookedVisits() != null) {
                user.getBookedVisits().addAll(candidateVisits);
            } else {
                user.setBookedVisits(candidateVisits);
            }
        }
    }

    static final Comparator<BookedResource> resourceTimeComparator = (o1, o2) -> {
        final int startOrder = o1.getScheduledStartTime().compareTo(o2.getScheduledStartTime());
        if (startOrder != 0) {
            return startOrder;
        }
        return o1.getScheduledEndTime().compareTo(o2.getScheduledEndTime());
    };

    static final Comparator<BookedVisit> visitStartTimeComparator = (o1, o2) -> o1.getScheduledStartTime().compareTo(o2.getScheduledStartTime());

    void sortCandidateVisits(final boolean rejectedCheck, final List<BookedVisit> candidateVisits) {
        if (rejectedCheck) {
            final List<BookedResource> bookedResourceList = candidateVisits.get(0).getBookedResourceList();
            Collections.sort(bookedResourceList, resourceTimeComparator);
        } else {
            Collections.sort(candidateVisits, visitStartTimeComparator);
        }
    }
    // @Transactional
    // entry point
    public List<BookedVisit> findCandidateVisits(
            final VisitSpecsDTO visitSpecsDTO,
            final UserSession userSession,
            final boolean confirmEvent,
            final boolean rejectedCheck,
            final boolean isInpatient) {

        final VisitTemplate selectedVisit = studyDAO.findVisitTemplateById(visitSpecsDTO.getVisit());
        final Integer visitDurationInMin = selectedVisit.getDuration();

        // null duration could occur if no resources. such a template should not
        // have been approved
        assert (visitDurationInMin != null);

        final SubjectMrn selectedSubjectMrn = subjectDAO.findSubjectMrnById(visitSpecsDTO.getSubjectMrnId());
        final User user = userSession.getUser();
        final Date startDate = new Date(visitSpecsDTO.getStartDate());
        final Date endDate = new Date(visitSpecsDTO.getEndDate());
        final List<BookedVisit> candidateVisits = findCandidateVisits(
                confirmEvent,
                rejectedCheck,
                isInpatient,
                selectedSubjectMrn,
                selectedVisit,
                startDate,
                endDate
        );
        setBookedVisits(confirmEvent, rejectedCheck, user, candidateVisits);
        sortCandidateVisits(rejectedCheck, candidateVisits);
        return candidateVisits;
    }

    boolean isDurationLessThan24Hours(final boolean confirmEvent,
                                      final boolean rejectedCheck,
                                      final int visitDurationInMin) {

        return confirmEvent || rejectedCheck || visitDurationInMin < MINS_PER_DAY;
    }

    List<Date> setupSearchDates(
            final boolean confirmEvent,
            final boolean rejectedCheck,
            final boolean isInpatient,
            final VisitTemplate selectedVisit,
            final Date startDate,
            final Date endDate) {

        List<Date> searchDates;
        final int visitDurationInMin = selectedVisit.getDuration();
        if (isInpatient &&
            isRelativeTimeTemplate(isInpatient, selectedVisit) &&
            isDurationLessThan24Hours(confirmEvent, rejectedCheck, visitDurationInMin)) {

            // only uses the first search date, even if user gave many days
            searchDates = Lists.newArrayList();
            searchDates.add(startDate);
        }
        else if (!confirmEvent && !rejectedCheck) {
            searchDates = dateInterval(startDate, endDate);
        }
        else {
            searchDates = dateInterval(startDate, startDate);
        }
        return searchDates;
    }

    public List<BookedVisit> findCandidateVisits(
            final boolean confirmEvent,
            final boolean rejectedCheck,
            final boolean isInpatient,
            final SubjectMrn selectedSubjectMrn,
            final VisitTemplate selectedVisit,
            final Date startDate,
            final Date endDate) {

        final List<Date> searchDates = setupSearchDates(
                confirmEvent,
                rejectedCheck,
                isInpatient,
                selectedVisit,
                startDate,
                endDate);

        return findCandidateVisits(
                searchDates,
                startDate,
                endDate,
                selectedSubjectMrn,
                selectedVisit,
                rejectedCheck,
                confirmEvent,
                isInpatient);
    }

    boolean isRelativeTimeTemplate(final boolean isInpatient,
                                   final VisitTemplate selectedVisit) {

        return selectedVisit.getRelativeTime() != null && selectedVisit.getRelativeTime()
                || !isInpatient;
    }


    Map<String, List<TemplateResource>> retrieveFloatResourceList(final VisitTemplate visitTemplate) {
        final List<TemplateResource> floatTrList = searchAlgorithmDAO.getSingleFloatTemplateResources(visitTemplate);
        final Map<String, List<TemplateResource>> floatResourcesMap = Maps.newLinkedHashMap();

        if (isNonNullNonEmpty(floatTrList)) {
            for (final TemplateResource ftr : floatTrList) {
                final String groupKey = ftr.getId().toString();

                final List<TemplateResource> floatResources;

                if (floatResourcesMap.containsKey(groupKey)) {
                    floatResources = floatResourcesMap.get(groupKey);
                } else {
                    floatResources = Lists.newArrayList();
                }
                floatResources.add(ftr);
                floatResourcesMap.put(groupKey, floatResources);
            }
        }
        return floatResourcesMap;
    }

    Map<String, List<TemplateResource>> getGroupedResourceListMap(final List<TemplateResourceGroup>
                                                                                  resourceGroupList) {
        final Map<String, List<TemplateResource>> groupedResourceMap = Maps.newLinkedHashMap();

        if (isNonNullNonEmpty(resourceGroupList)) {
            for (final TemplateResourceGroup trg : resourceGroupList) {
                final String groupKey = trg.getGroupId();
                final List<TemplateResource> grpResources;
                if (groupedResourceMap.containsKey(groupKey)) {
                    grpResources = groupedResourceMap.get(groupKey);
                } else {
                    grpResources = Lists.newArrayList();
                }
                grpResources.add(trg.getTemplateResource());
                groupedResourceMap.put(groupKey, grpResources);
            }
        }
        return groupedResourceMap;
    }

    Map<String, List<TemplateResource>> retrieveGroupedResourceList(
            final VisitTemplate visitTemplate,
            final boolean flexVsFloat) {

        final List<TemplateResourceGroup> groupedResourceList =
                searchAlgorithmDAO.getTemplateResourceGroups(visitTemplate, flexVsFloat);

        return getGroupedResourceListMap(groupedResourceList);
    }

    int findMaxEndMinuteRelativeToSearchDate(final Date endDate,
                                             final boolean rejectedCheck,
                                             final boolean confirmEvent,
                                             final boolean isInpatient,
                                             final int visitDurationInMin,
                                             final int startDateMinuteOfDay,
                                             final int endDateMinuteOfDay,
                                             final Date searchDate) {
        // candidate result for outpatient
        int candidateResult = endDateMinuteOfDay;

        if (isInpatient) {
            candidateResult = deltaMinutesBetweenDate1Date2(searchDate, endDate);
            if (isDurationLessThan24Hours(confirmEvent, rejectedCheck, visitDurationInMin)) {
                candidateResult += startDateMinuteOfDay;
            }
        }
        return candidateResult;
    }

    List<BookedVisit> findCandidateVisits(
            final List<Date> searchDates,
            final Date startDate,
            final Date endDate,
            final SubjectMrn subjectMrn,
            final VisitTemplate visitTemplate,
            final boolean rejectedCheck,
            final boolean confirmEvent,
            final boolean isInpatient
    ) {

        final boolean isRelativeTime = isRelativeTimeTemplate(isInpatient, visitTemplate);
        final List<BookedVisit> candidateVisits = Lists.newArrayList();

        final int visitDurationInMin = visitTemplate.getDuration();

        final int startDateMinuteOfDay =
                startDate.getHours() * MINS_PER_HR + startDate.getMinutes();
        final int endDateMinuteOfDay =
                endDate.getHours() * MINS_PER_HR + endDate.getMinutes();

        final List<TemplateResource> fixedResources =
                templateResourceDAO.findFixedTemplateResourcesByVisit(visitTemplate);
        final Map<String, List<TemplateResource>> floatTrList =
                retrieveFloatResourceList(visitTemplate);

        final Map<String, List<TemplateResource>> floatResourceGroupList =
                retrieveGroupedResourceList(visitTemplate, false);
        final Map<String, List<TemplateResource>> flexResourceGroupList =
                retrieveGroupedResourceList(visitTemplate, true);

        for (final Date searchDate : searchDates) {
            if (isRelativeTime) {
                int maxPossibleEndMinute = findMaxEndMinuteRelativeToSearchDate(
                        endDate,
                        rejectedCheck,
                        confirmEvent,
                        isInpatient,
                        visitDurationInMin,
                        startDateMinuteOfDay,
                        endDateMinuteOfDay,
                        searchDate);

                calculateAvailableVisitTimeSlotRelative(
                        startDateMinuteOfDay,
                        maxPossibleEndMinute,
                        visitTemplate,
                        subjectMrn,
                        searchDate,
                        candidateVisits,
                        fixedResources,
                        floatTrList,
                        floatResourceGroupList,
                        flexResourceGroupList,
                        rejectedCheck
                );
            } else {
                calculateAvailableVisitTimeSlotClockTime(
                        visitTemplate,
                        subjectMrn,
                        searchDate,
                        endDate,
                        candidateVisits,
                        fixedResources,
                        floatTrList,
                        floatResourceGroupList,
                        flexResourceGroupList,
                        rejectedCheck
                );
            }
        }// end of Search Date for loop

        return candidateVisits;
    }

    boolean isStartPlusDurationLteEnd(final int visitTimeSlotEnd,
                                      final int floatStart,
                                      final int resourceDurationTimeInMin) {

        return floatStart + resourceDurationTimeInMin <= visitTimeSlotEnd;
    }

    BookedVisit initializeBookedVisit(
            final VisitTemplate visitTemplate,
            final int visitTimeSlotStart,
            final int visitDurationInMin,
            final Date searchDate) {

        final BookedVisit bookedVisit = new BookedVisit();
        bookedVisit.setId(1234556);
        bookedVisit.setName(visitTemplate.getName());
        bookedVisit.setStudy(visitTemplate.getStudy());
        bookedVisit.setVisitTemplate(visitTemplate);
        bookedVisit.setVisitType(visitTemplate.getVisitType());
        final Date startTime = modifyDateFieldPlusAmtSetHourMinute(
                searchDate,
                Calendar.DAY_OF_YEAR,
                0,
                divideByMinsPerHour(visitTimeSlotStart),
                moduloMinsPerHour(visitTimeSlotStart));

        final Date endTime = modifyDateFieldPlusAmtSetHourMinute(
                searchDate,
                Calendar.DAY_OF_YEAR,
                0,
                divideByMinsPerHour(visitTimeSlotStart + visitDurationInMin),
                moduloMinsPerHour(visitTimeSlotStart + visitDurationInMin));

        bookedVisit.setScheduledStartTime(startTime);
        bookedVisit.setScheduledEndTime(endTime);
        return bookedVisit;
    }

    void calculateAvailableVisitTimeSlotRelative(final int startTimeInMin,
                                                 final int endTimeInMin,
                                                 final VisitTemplate visitTemplate,
                                                 final SubjectMrn subjectMrn,
                                                 final Date searchDate,
                                                 final List<BookedVisit> candidateVisits,
                                                 final List<TemplateResource> fixedResources,
                                                 final Map<String, List<TemplateResource>> floatTrList,
                                                 final Map<String, List<TemplateResource>> floatResourceGroups,
                                                 final Map<String, List<TemplateResource>> flipResourceGroups,
                                                 final boolean rejectedCheck) {

        final int visitDurationInMin = visitTemplate.getDuration();
        for (int visitTimeSlotStart = startTimeInMin;
             isStartPlusDurationLteEnd(endTimeInMin, visitTimeSlotStart, visitDurationInMin);
             visitTimeSlotStart += INCREMENT_FACTOR) {

            final BookedVisit bookedVisit = initializeBookedVisit(
                    visitTemplate,
                    visitTimeSlotStart,
                    visitDurationInMin,
                    searchDate);

            bookedVisit.setSubjectMrn(subjectMrn);
            final String uniqueKey = RandomStringUtils.randomNumeric(8);
            bookedVisit.setUniquekey(uniqueKey);

            calculateSomeAvailableResources(
                    visitDurationInMin,
                    searchDate,
                    candidateVisits,
                    fixedResources,
                    floatTrList,
                    floatResourceGroups,
                    flipResourceGroups,
                    visitTimeSlotStart,
                    bookedVisit,
                    rejectedCheck);
        }
    }

    void computeAndSetDuration(final TemplateResource templateResource) {

        if (templateResource != null) {
            int totalMinutesDelta = 0;
            final Date startDate = templateResource.getStartDate();
            final Date endDate = templateResource.getEndDate();
            if (startDate != null && endDate != null) {
                final int daysDelta = getTemplateResourceDay(endDate) - getTemplateResourceDay(startDate);
                final int hoursDelta = endDate.getHours() - startDate.getHours();
                final int minutesDelta = endDate.getMinutes() - startDate.getMinutes();
                totalMinutesDelta = daysDelta * MINS_PER_DAY + hoursDelta * MINS_PER_HR + minutesDelta;
            }
            templateResource.setDuration(totalMinutesDelta);
        }
    }

    void allocateTimeFixedResources(final int visitTimeSlotStart,
                                    final Date searchDate,
                                    final List<TemplateResource> fixedResources,
                                    final String groupType) {

        for (final TemplateResource fixedTr : fixedResources) {
            // set the TemplateResource start and end Times
            computeAndSetDuration(fixedTr);
            final int resourceDurationTimeInMin = fixedTr.getDuration();
            final int resourceStartTimeInMin =
                    fixedTr.getStartDate().getHours() * MINS_PER_HR + fixedTr.getStartDate().getMinutes();
            final int curResourceStartTimeHr = divideByMinsPerHour(visitTimeSlotStart + resourceStartTimeInMin);
            final int curResourceStartTimeMin = moduloMinsPerHour(visitTimeSlotStart + resourceStartTimeInMin);
            final int curResourceEndTimeHr = divideByMinsPerHour(
                    visitTimeSlotStart + resourceStartTimeInMin + resourceDurationTimeInMin);
            final int curResourceEndTimeMin = moduloMinsPerHour(
                    visitTimeSlotStart + resourceStartTimeInMin + resourceDurationTimeInMin);
            final Date startTime = modifyDateFieldPlusAmtSetHourMinute(
                    searchDate, Calendar.DAY_OF_YEAR, getTemplateResourceDay(fixedTr.getStartDate()) -
                            1, curResourceStartTimeHr, curResourceStartTimeMin);
            final Date endTime = modifyDateFieldPlusAmtSetHourMinute(
                    searchDate, Calendar.DAY_OF_YEAR, getTemplateResourceDay(fixedTr.getStartDate()) -
                            1, curResourceEndTimeHr, curResourceEndTimeMin);
            fixedTr.setScheduledStartTime(startTime);
            fixedTr.setScheduledEndTime(endTime);
            fixedTr.setResourceGroupType(groupType);
        }
    }

    boolean checkIfSublocationClosed(final TemplateResource templateResource) {

        final ResourceSublocation resourceSublocation = studyDAO
                .findSublocationByResource(templateResource.getResource());

        final boolean isResourceSublocationClosed = searchAlgorithmDAO.isSublocationClosed(
                resourceSublocation.getSublocation(),
                templateResource.getScheduledStartTime(),
                templateResource.getScheduledEndTime());

        if (isResourceSublocationClosed) {
            templateResource.setAvailable("No");
            templateResource.setRejectedResourceMessage("Sub-Location Closure");
            return true;
        }
        return false;
    }

    Set<Integer> getDefaultAvailabilityDaysOfWeek(final Date startDate, final Date endDate) {
        final int diff = compareDateDifference(startDate, endDate);
        final Set<Integer> daysOfWeek = Sets.newHashSet();
        final Range allowedDays = Range.from(1).to(7).inclusive();
        for (int count = 0; count <= diff && count < 7; count++) {
            final Date curDate = modifyDateFieldPlusAmtSetHourMinute(startDate, Calendar.DAY_OF_YEAR, count, 0, 0);
            final Calendar cal = Calendar.getInstance();
            cal.clear();
            cal.setTime(curDate);
            final int day = cal.get(Calendar.DAY_OF_WEEK);
            if (allowedDays.contains(day)) {
                daysOfWeek.add(day);
            }
        }
        return daysOfWeek;
    }

    String getDayOfWeekString(final ResourceSchedule rs) {
        String ss = null;
        switch (rs.getDayOfWeek()) {
            case 1: {
                ss = Statics.SUNDAY;
            }
            break;

            case 2: {
                ss = Statics.MONDAY;
            }
            break;

            case 3: {
                ss = Statics.TUESDAY;
            }
            break;

            case 4: {
                ss = Statics.WEDNESDAY;
            }
            break;

            case 5: {
                ss = Statics.THURSDAY;
            }
            break;

            case 6: {
                ss = Statics.FRIDAY;
            }
            break;

            case 7: {
                ss = Statics.SATURDAY;
            }
            break;
        }
        return ss;
    }

    void populateDefaultSchedule(final List<ResourceSchedule> rsList,
                                 final Map<String, List<ResourceSchedule>> dayOfWeekSchedule) {

        // precondition: dayOfWeekSchedule is (out parameter and) passed in as
        // empty
        for (final ResourceSchedule rs : rsList) {

            final String resourceDay = getDayOfWeekString(rs);

            if (dayOfWeekSchedule.get(resourceDay) == null) {
                final List<ResourceSchedule> schedules = new ArrayList<ResourceSchedule>();
                dayOfWeekSchedule.put(resourceDay, schedules);
            }

            dayOfWeekSchedule.get(resourceDay).add(rs);
        }
    }

    Map<String, List<ResourceSchedule>> retrieveResourceDefaultSchedule(
            final Resource resource,
            final Date startDate,
            final Date endDate) {

        final Map<String, List<ResourceSchedule>> dayOfWeekSchedule = new HashMap<String, List<ResourceSchedule>>();
        final Set<Integer> selectedDaysOfWeek = getDefaultAvailabilityDaysOfWeek(startDate, endDate);
        final List<Integer> daysOfWeekList = new ArrayList<Integer>(selectedDaysOfWeek);

        final List<ResourceSchedule> rsl =
                resourceDAO.findResourceScheduleByResource(resource, daysOfWeekList, false);
        if (isNonNullNonEmpty(rsl)) {
            populateDefaultSchedule(rsl, dayOfWeekSchedule);
        }
        return dayOfWeekSchedule;
    }

    void searchDatesMapping(final Map<Date, String> searchDates, final Calendar cal, final Date curDate) {
        cal.clear();
        cal.setTime(curDate);
        final int day = cal.get(Calendar.DAY_OF_WEEK);
        String dayOfWeek = null;
        switch (day) {
            case 1: {
                dayOfWeek = Statics.SUNDAY;
            }
            break;
            case 2: {
                dayOfWeek = Statics.MONDAY;
            }
            break;
            case 3: {
                dayOfWeek = Statics.TUESDAY;
            }
            break;
            case 4: {
                dayOfWeek = Statics.WEDNESDAY;
            }
            break;
            case 5: {
                dayOfWeek = Statics.THURSDAY;
            }
            break;
            case 6: {
                dayOfWeek = Statics.FRIDAY;
            }
            break;
            case 7: {
                dayOfWeek = Statics.SATURDAY;
            }
            break;
        }
        searchDates.put(curDate, dayOfWeek);
    }

    Map<Date, String> buildSearchDates(final Date startDate, final Date endDate) {
        // use a TreeMap to keep things in order
        final Map<Date, String> searchDates = new TreeMap<Date, String>();
        final Calendar cal = Calendar.getInstance();
        Date curDate;
        final int diff = compareDateDifference(startDate, endDate);
        if (diff == 0) {
            curDate = modifyDateFieldPlusAmtSetHourMinute(startDate, Calendar.DAY_OF_YEAR, 0, 0, 0);
            searchDatesMapping(searchDates, cal, curDate);
        } else if (diff > 0) {
            for (int count = 0; count <= diff; count++) {
                curDate = modifyDateFieldPlusAmtSetHourMinute(startDate, Calendar.DAY_OF_YEAR, count, 0, 0);
                searchDatesMapping(searchDates, cal, curDate);
            }
        }
        return searchDates;
    }

    Map<Date, String> retrieveDaysOfWeek(final Date startDate, final Date endDate) {
        return buildSearchDates(startDate, endDate);
    }

    TreeMap<Date, List<ResourceSchedule>> loadRelevantDaysOfDefaultSchedule(
            final Date firstDay,
            final Date lastDay,
            final Map<String, List<ResourceSchedule>> resourceDefaultSchedule) {

        final TreeMap<Date, List<ResourceSchedule>> result = new TreeMap<Date, List<ResourceSchedule>>();
        // add one day to cover spill-over for relative times. e.g., relative
        // two days, 0:0 - 1:23
        // could end up as Thurs 7am - Sat 6am. So we want three days, not the
        // apparent two
        final Date last_day = nextDay(lastDay);
        final Map<Date, String> dateToWeekdayMap = retrieveDaysOfWeek(firstDay, last_day);
        for (final Map.Entry<Date, String> mapEntry : dateToWeekdayMap.entrySet()) {
            final Date date = mapEntry.getKey();
            final String weekDay = mapEntry.getValue();
            final List<ResourceSchedule> todayScheduleList = resourceDefaultSchedule.get(weekDay);
            result.put(date, todayScheduleList);
        }
        return result;
    }

    int computeDate2PeriodRelativeToDate1(final Date date1, final Date date2) {
        final int numberOfDays = day2minusDay1(date1, date2);
        return numberOfDays * 96 + computePeriodOfDate(date2);
    }

    int computeLastPeriod(final Date date1, final Date date2) {
        final int period1 = computePeriodOfDate(date1);
        final Calendar calendar1 = Calendar.getInstance();
        calendar1.setTime(date1);
        final int offset = calendar1.get(Calendar.DST_OFFSET);
        final Calendar calendar2 = Calendar.getInstance();
        calendar2.setTime(date2);
        calendar2.set(Calendar.DST_OFFSET, offset);
        // subtract one minute, so that endTime of 2pm acts like 1:59
        final long differenceInMillis = calendar2.getTimeInMillis() - calendar1.getTimeInMillis() - MILLISECS_PER_MIN;
        final int diffInPeriods = (int) Math.ceil(differenceInMillis / MILLISECS_PER_MIN / MINUTES_PER_PERIOD);
        return period1 + diffInPeriods;
    }
    int computePeriodOfDate(final Date date) {
        final int minutes = computeMinutesFromHrsAndMins(date);
        return minutes / MINUTES_PER_PERIOD;
    }

    int computeMinutesFromHrsAndMins(final Date date) {
        final Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        final int timeHr = cal.get(Calendar.HOUR_OF_DAY);
        final int timeMin = cal.get(Calendar.MINUTE);
        return timeHr * MINS_PER_HR + timeMin;
    }

    void loadIntoPeriodToQuantityMap(
            final int resourceQuantity,
            final int firstPeriod,
            final int lastPeriod, // inclusive
            final Map<Integer, Integer> periodToQuantityMap
    ) {

        for (int i = firstPeriod; i <= lastPeriod; i++) {
            Integer previousQuantity = periodToQuantityMap.get(i);
            if (previousQuantity == null) {
                previousQuantity = 0;
            }
            periodToQuantityMap.put(i, previousQuantity + resourceQuantity);
        }
    }

    Map<Integer, Integer> loadPeriodToQuantityOverrideMap(
            final TemplateResource requestResource,
            final List<ResourceSchedule> resourceOverrideSchedule) {

        final Map<Integer, Integer> periodToQuantityOverrideMap = new TreeMap<Integer, Integer>();
        final Date baseDate = requestResource.getScheduledStartTime();

        if (!resourceOverrideSchedule.isEmpty()) {
            final List<TimeBoundedIdentity> overrideScheduleList =
                    ResourceSchedule.toTimeBoundedIdentityList(resourceOverrideSchedule);

            for (final TimeBoundedIdentity overrideSchedule : overrideScheduleList) {
                final Date startTimeDate = overrideSchedule.getStartTime();
                final Date endTimeDate = overrideSchedule.getEndTime();
                final int firstPeriod = computeDate2PeriodRelativeToDate1(baseDate, startTimeDate);
                final int lastPeriod = computeLastPeriod(baseDate, endTimeDate);
                loadIntoPeriodToQuantityMap(overrideSchedule.getQuantity(), firstPeriod, lastPeriod,
                        periodToQuantityOverrideMap);
            }
        }
        return periodToQuantityOverrideMap;
    }

    void loadResourceScheduleIntoPeriodToQuantityMap(final ResourceSchedule resourceSchedule,
                                                     final Map<Integer, Integer> periodToQuantityMap,
                                                     final int dayNumber) {

        if (periodToQuantityMap == null) {
            return;
        }

        final Date scheduledStartTime = resourceSchedule.getStartTime();
        final Date scheduledEndTime = resourceSchedule.getEndTime();

        final int periodsOffset = dayNumber * PERIODS_PER_DAY;

        int firstPeriod = computePeriodOfDate(scheduledStartTime);
        int lastPeriod = computeLastPeriod(scheduledStartTime, scheduledEndTime);

        // adjust periods for multi-day schedules
        firstPeriod += periodsOffset;
        lastPeriod += periodsOffset;

        final Integer resourceQuantity = resourceSchedule.getQuantity();

        loadIntoPeriodToQuantityMap(resourceQuantity, firstPeriod, lastPeriod, periodToQuantityMap);
    }

    Map<Integer, Integer> loadPeriodToQuantityDefaultScheduleMap(
            final TreeMap<Date, List<ResourceSchedule>> defaultScheduleMap) {

        final Map<Integer, Integer> periodToQuantityDefaultAvailableMap = new TreeMap<Integer, Integer>();
        if (!defaultScheduleMap.isEmpty()) {
            int dayNumber = -1;
            for (final Date date : defaultScheduleMap.navigableKeySet()) {
                final List<ResourceSchedule> scheduleList = defaultScheduleMap.get(date);
                dayNumber++;
                if (scheduleList != null) {
                    for (final ResourceSchedule schedule : scheduleList) {
                        loadResourceScheduleIntoPeriodToQuantityMap(schedule, periodToQuantityDefaultAvailableMap,
                                dayNumber);
                    }
                }
            }
        }
        return periodToQuantityDefaultAvailableMap;
    }

    Map<Integer, Integer> updateDefaultMapWithOverrideMap(
            final Map<Integer, Integer> periodToQuantityOverrideMap,
            final Map<Integer, Integer> periodToQuantityDefaultAvailableMap) {

        final Map<Integer, Integer> finalScheduleTreeMap = new TreeMap<Integer, Integer>();
        finalScheduleTreeMap.putAll(periodToQuantityDefaultAvailableMap);
        for (final Integer key : periodToQuantityOverrideMap.keySet()) {
            final Integer value = periodToQuantityOverrideMap.get(key);
            finalScheduleTreeMap.put(key, value);
        }
        return finalScheduleTreeMap;
    }
    boolean isBookedResourceEqualToTemplateResourceId(final Resource candidateTemplateResource,
                                                              final Resource resource) {

        return resource.getId().equals(candidateTemplateResource.getId());
    }
    boolean ifDatesOverlap(final Date candidateStartTime, final Date candidateEndTime, final Date existingStartTime,
                           final Date existingEndTime) {
        if (existingStartTime != null && existingEndTime != null) {
            return candidateStartTime.getTime() < existingEndTime.getTime() &&
                    existingStartTime.getTime() < candidateEndTime.getTime();
        }
        return false;
    }
    void adjustMapsForBookedResources(
            final Map<Integer, Integer> candidatePeriodToQtyMap,
            final Date reservedStartDate,
            final Date reservedEndDate,
            final Date candidateStartDate) {

        Date dateOfFirstCandidatePeriodToUpdate;
        int mapIndexOffset;

        final int reserveDayMinusCandidateDay = day2minusDay1(candidateStartDate, reservedStartDate);
        if (reserveDayMinusCandidateDay < 0) { // candidate starts after reserved
            dateOfFirstCandidatePeriodToUpdate = candidateStartDate;
            mapIndexOffset = 0;
        } else { // candidate starts (at or) before reserved
            dateOfFirstCandidatePeriodToUpdate = reservedStartDate;
            mapIndexOffset = reserveDayMinusCandidateDay * PERIODS_PER_DAY;
        }

        final int firstPeriod = computePeriodOfDate(dateOfFirstCandidatePeriodToUpdate);
        final int lastPeriod = computeLastPeriod(dateOfFirstCandidatePeriodToUpdate, reservedEndDate);

        for (int i = firstPeriod; i <= lastPeriod; i++) {
            final int effectiveIndex = i + mapIndexOffset;
            final Integer currentQuantity = candidatePeriodToQtyMap.get(effectiveIndex);
            if (currentQuantity != null) {
                candidatePeriodToQtyMap.put(effectiveIndex, currentQuantity - 1);
            }
        }
    }
    void adjustMapsForBookedResource(
            final TemplateResource candidateTemplateResource,
            final Map<Integer, Integer> candidatePeriodToQtyMap,
            final List<BookedResource> availableBookedResourceSlots) {

        for (final BookedResource bookedResource : availableBookedResourceSlots) {
            final Date reservedStartDate = bookedResource.getScheduledStartTime();
            final Date reservedEndDate = bookedResource.getScheduledEndTime();
            final Date candidateStartDate = candidateTemplateResource.getScheduledStartTime();
            final Date candidateEndDate = candidateTemplateResource.getScheduledEndTime();
            if (    isBookedResourceEqualToTemplateResourceId(
                            candidateTemplateResource.getResource(),
                            bookedResource.getResource())
                &&
                    ifDatesOverlap(
                            candidateStartDate,
                            candidateEndDate,
                            reservedStartDate,
                            reservedEndDate)) {

                adjustMapsForBookedResources(
                        candidatePeriodToQtyMap,
                        reservedStartDate,
                        reservedEndDate,
                        candidateStartDate);
            }
        }
    }
    void adjustMapsForProvisionalAllocation(final int key,
                                            final Map<Integer, Integer> candidatePeriodToQtyMap) {
        if (candidatePeriodToQtyMap.containsKey(key)) {
            final int quantity = candidatePeriodToQtyMap.get(key) - 1;
            candidatePeriodToQtyMap.put(key, quantity);
        }
    }
    boolean setMessageIfUnavailable(
            final int key,
            final Integer currentQuantity,
            final TemplateResource candidateTemplateResource,
            final Map<Integer, Integer> candidatePeriodToQtyOverrideMap,
            final Map<Integer, Integer> candidatePeriodToQtyDefaultMap,
            boolean isAvailable) {

        if (currentQuantity == null) {
            candidateTemplateResource.setRejectedResourceMessage("No Default Availability Entry Found");
            isAvailable = false;
        } else if (currentQuantity < 1 && candidatePeriodToQtyOverrideMap.containsKey(key)) {
            final int quantity = candidatePeriodToQtyOverrideMap.get(key);
            candidateTemplateResource.setRejectedResourceMessage(
                    "Temporary Adjustment - Quantity (" + quantity + ") Exhausted");
            isAvailable = false;
        } else if (currentQuantity < 1 && candidatePeriodToQtyDefaultMap.containsKey(key)) {
            final int quantity = candidatePeriodToQtyDefaultMap.get(key);
            candidateTemplateResource.setRejectedResourceMessage(
                    "Default Availability - Quantity (" + quantity + ") Exhausted");
            isAvailable = false;
        }
        return isAvailable;
    }
    /**
     * Checks if the given resource time Slot is already reserved for any other
     * Visit in any study. returns true if reserved, else returns false
     *
     * @param candidateTemplateResource
     * @param reservedResourceTimeList
     * @return
     */
    boolean isResourceTimeSlotAvailable(
            final TemplateResource candidateTemplateResource,
            final List<BookedResource> reservedResourceTimeList,
            final Map<Integer, Integer> candidatePeriodToQtyMap,
            final Map<Integer, Integer> candidatePeriodToQtyOverrideMap,
            final Map<Integer, Integer> candidatePeriodToQtyDefaultMap,
            final List<BookedResource> availableBookedResourceSlots,
            final List<TemplateResource> availableResourceSlots) {

        boolean isAvailable = true;

        adjustMapsForBookedResource(
                candidateTemplateResource,
                candidatePeriodToQtyMap,
                reservedResourceTimeList);

        if (availableBookedResourceSlots != null) {
            adjustMapsForBookedResource(
                    candidateTemplateResource,
                    candidatePeriodToQtyMap,
                    availableBookedResourceSlots);
        }
        final Date candidateStartDate = candidateTemplateResource.getScheduledStartTime();
        final Date candidateEndDate = candidateTemplateResource.getScheduledEndTime();

        final int firstPeriod = computePeriodOfDate(candidateStartDate);
        final int lastPeriod = computeLastPeriod(candidateStartDate, candidateEndDate);

        if (isNonNullNonEmpty(availableResourceSlots)) {
            for (final TemplateResource templateResource : availableResourceSlots) {
                if (    isBookedResourceEqualToTemplateResourceId(candidateTemplateResource.getResource(),
                                                                    templateResource.getResource())
                    &&  ifDatesOverlap(candidateStartDate, candidateEndDate, templateResource.getScheduledStartTime(),
                                templateResource.getScheduledEndTime())) {

                    final int usedFirstPeriod = computePeriodOfDate(templateResource.getScheduledStartTime());
                    final int usedLastPeriod = computeLastPeriod(templateResource.getScheduledStartTime(),
                                                                templateResource.getScheduledEndTime());

                    for (int i = usedFirstPeriod; i <= usedLastPeriod; i++) {
                        adjustMapsForProvisionalAllocation(i, candidatePeriodToQtyMap);
                    }
                }
            }
        }

        for (int i = firstPeriod; i <= lastPeriod; i++) {

            final Integer currentQuantity = candidatePeriodToQtyMap.get(i);
            isAvailable = setMessageIfUnavailable(
                    i,
                    currentQuantity,
                    candidateTemplateResource,
                    candidatePeriodToQtyOverrideMap,
                    candidatePeriodToQtyDefaultMap,
                    isAvailable);

            if (!isAvailable) {
                return isAvailable;
            }
        }
        return isAvailable;
    }
    boolean checkResourceAvailability(
            final TemplateResource requestResource,
            final List<BookedResource> reservedResourceTimeList,
            final Map<Integer, Integer> periodToQuantityOverrideMap,
            final Map<Integer, Integer> periodToQuantityDefaultAvailableMap,
            final Map<Integer, Integer> finalScheduleTreeMap,
            final List<BookedResource> availableBookedResourceSLots,
            final List<TemplateResource> availableResourceSlots) {

        if (!isResourceTimeSlotAvailable(
                requestResource,
                reservedResourceTimeList,
                finalScheduleTreeMap,
                periodToQuantityOverrideMap,
                periodToQuantityDefaultAvailableMap,
                availableBookedResourceSLots,
                availableResourceSlots)) {

            requestResource.setAvailable("No");
            return false;
        }
        return true;
    }
    /**
     * Check if the resource is available during the search time range based on
     * its count set in resource Default Schedule
     */
    boolean checkAvailability(
            final TemplateResource requestResource,
            final List<BookedResource> availableBookedResourceSlots,
            final List<TemplateResource> availableResourceSlots) {

        requestResource.setRejectedResourceMessage("");
        requestResource.setAvailable("Yes");

        if (checkIfSublocationClosed(requestResource)) {
            return false;
        }

        final List<BookedResource> reservedResourceTimeList =
                searchAlgorithmDAO.findOverbookConflictResourcesByVisitStatus(
                        requestResource.getResource(),
                        requestResource.getScheduledStartTime(),
                        requestResource.getScheduledEndTime());

        final Map<String, List<ResourceSchedule>> resourceDefaultSchedule =
                retrieveResourceDefaultSchedule(
                        requestResource.getResource(),
                        requestResource.getScheduledStartTime(),
                        requestResource.getScheduledEndTime());

        final TreeMap<Date, List<ResourceSchedule>> defaultScheduleMap =
                loadRelevantDaysOfDefaultSchedule(
                        requestResource.getScheduledStartTime(),
                        requestResource.getScheduledEndTime(),
                        resourceDefaultSchedule);

        final List<ResourceSchedule> resourceOverrideSchedule =
                resourceDAO.findTemporaryAdjustmentsByResource(
                        requestResource.getResource(),
                        requestResource.getScheduledStartTime(),
                        requestResource.getScheduledEndTime(),
                        true);

        final Map<Integer, Integer> periodToQuantityOverrideMap =
                loadPeriodToQuantityOverrideMap(
                        requestResource,
                        resourceOverrideSchedule);

        final Map<Integer, Integer> periodToQuantityDefaultAvailableMap =
                loadPeriodToQuantityDefaultScheduleMap(defaultScheduleMap);

        final Map<Integer, Integer> finalScheduleTreeMap =
                updateDefaultMapWithOverrideMap(
                        periodToQuantityOverrideMap,
                        periodToQuantityDefaultAvailableMap);

        return checkResourceAvailability(
                requestResource,
                reservedResourceTimeList,
                periodToQuantityOverrideMap,
                periodToQuantityDefaultAvailableMap,
                finalScheduleTreeMap,
                availableBookedResourceSlots,
                availableResourceSlots);
    }

    List<ResourceAlternate> getResourceAlternates(final TemplateResource templateResource) {
        return resourceDAO.findResourceAlternates(templateResource.getResource());
    }

    boolean noGroupId(final TemplateResource res) {
        return res.getGroupId() == null;
    }

    boolean isFixedResource(final TemplateResource templateResource) {

        return !templateResource.getFloatable() &&
                noGroupId(templateResource) &&
                !templateResource.getFlexible();
    }

    boolean checkIfReturnResource(
            final TemplateResource templateResource,
            final boolean rejectedCheck,
            final boolean isAvailable,
            final boolean emptyAltList) {

        return isAvailable ||
                (rejectedCheck && !templateResource.getAlternate()) ||
                (rejectedCheck && emptyAltList && isFixedResource(templateResource));
    }

    static boolean hasGroupId(final TemplateResource r) {

        return r.getGroupId() != null;
    }

    boolean isNotFixedResource(final TemplateResource templateResource) {
        return  templateResource.getFloatable() ||
                hasGroupId(templateResource) && templateResource.getFlexible();
    }

    boolean checkIfReturnNull(
            final TemplateResource templateResource,
            final boolean rejectedCheck,
            final boolean emptyAltList) {

        return  (!rejectedCheck && (!templateResource.getAlternate() || emptyAltList)) ||
                (rejectedCheck && emptyAltList && isNotFixedResource(templateResource));
    }

    TemplateResource createTempResourceSlot(final Resource selectedResource, final TemplateResource templateResource) {

        final TemplateResource slot = templateResource.cloneTemplateResource();
        slot.setResource(selectedResource);
        return slot;
    }

    TemplateResource checkForStandardAlternate(
            final List<ResourceAlternate> resourceAltList,
            final TemplateResource templateResource,
            final List<BookedResource> availableBookedResourceSlots,
            final List<TemplateResource> availableResourceSlots) {

        final Function<ResourceAlternate, TemplateResource> toTemplateResource = resourceAlternate -> {
            final TemplateResource altResource = createTempResourceSlot(resourceAlternate.getAlternateResource(),
                    templateResource);
            altResource.setAlternateResourceUsed(false);
            return altResource;
        };

        final Predicate<TemplateResource> isAvailable = tr -> checkAvailability(tr, availableBookedResourceSlots,
                availableResourceSlots);

        final Optional<TemplateResource> available = lazy(resourceAltList).map(toTemplateResource).find(isAvailable);

        return available.map(altResource -> {
            altResource.setAlternateResourceUsed(true);
            altResource.setResourceGroupType(templateResource.getResourceGroupType());
            return altResource;
        }).orElse(null);
    }
    /**
     * Find availability of the given resource in the Time slot indicated in the
     * resource data. If not available, then checks to see if standard
     * alternates need to be used. If alternates used is true, Then check to see
     * if any of the alternate Resources listed are found available for the time
     * slot chosen. Returns null if no resource is found available in the given
     * time slot.
     */
    TemplateResource findAvailableResource(
            final TemplateResource templateResource,
            final boolean rejectedCheck,
            final List<BookedResource> availableBookedResourceSlots,
            final List<TemplateResource> availableResourceSlots) {

        final boolean isAvailable = checkAvailability(templateResource, availableBookedResourceSlots, availableResourceSlots);
        final List<ResourceAlternate> resourceAltList = getResourceAlternates(templateResource);

        if (checkIfReturnResource(templateResource, rejectedCheck, isAvailable, resourceAltList.isEmpty())) {
            return templateResource;
        }

        if (checkIfReturnNull(templateResource, rejectedCheck, resourceAltList.isEmpty())) {
            return null;
        }

        final TemplateResource altResource = checkForStandardAlternate(resourceAltList, templateResource,
                availableBookedResourceSlots,
                availableResourceSlots);
        if (altResource != null && altResource.getAlternateResourceUsed()) {
            return altResource;
        }

        if (rejectedCheck && !isNotFixedResource(templateResource)) {
            templateResource.setAlternateResourceUsed(true);
            return templateResource;
        }
        return null;
    }

    List<TemplateResource> calculateFixedResourceAvailability(
            final List<TemplateResource> fixedResources,
            final boolean rejectedCheck,
            final List<BookedResource> availableBookedResourceSlots) {

        final List<TemplateResource> availableResourceSlots = new ArrayList<TemplateResource>();
        for (final TemplateResource resource : fixedResources) {
            final TemplateResource foundSlot = findAvailableResource(resource, rejectedCheck,
                    availableBookedResourceSlots,
                    availableResourceSlots);
            if (foundSlot != null) {
                availableResourceSlots.add(foundSlot);
            } else {
                return null;
            }
        }
        return availableResourceSlots;
    }

    BookedResource createBookedResource(final TemplateResource templateResource, final BookedVisit bookedVisit) {
        final BookedResource bookedResource = new BookedResource();
        bookedResource.setId(4567);
        bookedResource.setBookedVisit(bookedVisit);
        bookedResource.setDuration(templateResource.getDuration());
        bookedResource.setScheduledStartTime(templateResource.getScheduledStartTime());
        bookedResource.setScheduledEndTime(templateResource.getScheduledEndTime());
        bookedResource.setResource(templateResource.getResource());
        bookedResource.setTemplateResource(templateResource);
        bookedResource.setAvailable(templateResource.getAvailable());
        bookedResource.setRejectedResourceMessage(templateResource.getRejectedResourceMessage());
        bookedResource.setBillable(templateResource.getBillable());
        return bookedResource;
    }

    void addResourcesToBookedVisit(final List<TemplateResource> templateResources,
                                   final BookedVisit bookedVisit) {
        List<BookedResource> bookedResources = bookedVisit.getBookedResourceList();
        String rooms = bookedVisit.getRooms();

        if (rooms == null) {
            rooms = " ";
        }

        if (bookedResources == null) {
            bookedResources = new ArrayList<>();
        }

        for (final TemplateResource templateResource : templateResources) {
            final BookedResource bookedResource = createBookedResource(templateResource, bookedVisit);
            bookedResources.add(bookedResource);
            if (bookedResource.getResource().getResourceType().getName().equalsIgnoreCase("Room")) {
                rooms += bookedResource.getResource().getName() + ", ";
                bookedVisit.setRooms(rooms);
            }
        }

        bookedVisit.setBookedResourceList(bookedResources);
    }
    boolean foundAvailableFixedResources(final Date searchDate,
                                         final List<TemplateResource> fixedResources,
                                         final int visitTimeSlotStart,
                                         final BookedVisit bookedVisit,
                                         final boolean rejectedCheck,
                                         final String groupType) {
        boolean result = true;

        if (isNonNullNonEmpty(fixedResources)) {
            allocateTimeFixedResources(visitTimeSlotStart, searchDate, fixedResources, groupType);
            final List<TemplateResource> availableFixedResources = removeDuplicates
                    (calculateFixedResourceAvailability(
                            fixedResources,
                            rejectedCheck,
                            bookedVisit.getBookedResourceList()));

            if (availableFixedResources != null) {
                this.addResourcesToBookedVisit(availableFixedResources, bookedVisit);
            } else {
                // No (further) work on this time slot (?)
                result = false;
            }
        }
        return result;
    }

    boolean isRelativeTimeRejectedVisit(
            final BookedVisit bookedVisit,
            final boolean rejectedCheck,
            final boolean fixedResourcesAvailable) {

        if (!fixedResourcesAvailable && !rejectedCheck) {
            bookedVisit.setRejectedVisit(true);
            bookedVisit.setBookedResourceList(new ArrayList<BookedResource>());
            return true;
        }
        return false;
    }

    int findMultiDayMinutesOffset(final List<TemplateResource> originalSequence,
                                  final int relativeOffset) {

        final Date earliestDate = enrich(originalSequence)
                .map(TemplateResource::getStartDate)
                .min((lhs, rhs) -> lhs.compareTo(rhs))
                .orElse(null);

        final int baseStartTime =
                (getTemplateResourceDay(earliestDate) - 1) * MINS_PER_DAY +
                earliestDate.getHours() * MINS_PER_HR +
                earliestDate.getMinutes();

        return baseStartTime + relativeOffset;
    }

    TemplateResource duplicateResourceSlot(final TemplateResource givenResourceSlot) {
        final TemplateResource slot = new TemplateResource();
        slot.setId(givenResourceSlot.getId());
        slot.setAvailableEndTimeInMin(givenResourceSlot.getAvailableEndTimeInMin());
        slot.setAlternate(givenResourceSlot.getAlternate());
        slot.setResource(givenResourceSlot.getResource());
        slot.setBillable(givenResourceSlot.getBillable());
        slot.setAvailableStartTimeInMin(givenResourceSlot.getAvailableStartTimeInMin());
        slot.setDuration(givenResourceSlot.getDuration());
        slot.setFlexible(givenResourceSlot.getFlexible());
        slot.setFloatEnd(givenResourceSlot.getFloatEnd());
        slot.setFloatStart(givenResourceSlot.getFloatStart());
        slot.setFloatable(givenResourceSlot.getFloatable());
        slot.setScheduledEndTime(givenResourceSlot.getScheduledEndTime());
        slot.setScheduledStartTime(givenResourceSlot.getScheduledStartTime());
        slot.setVisitTemplate(givenResourceSlot.getVisitTemplate());
        slot.setStartMinutes(givenResourceSlot.getStartMinutes());
        slot.setEndMinutes(givenResourceSlot.getEndMinutes());
        slot.setGroupId(givenResourceSlot.getGroupId());
        return slot;
    }

    void allocateTime(  final List<TemplateResource> sortedSequence,
                        final int baseStartTime,
                        final Date searchDate) {

        int startTimeInMin = baseStartTime;
        final List<TemplateResource> templateResources = Lists.newArrayList();

        for (final TemplateResource templateResource : sortedSequence) {

            final int startHr = divideByMinsPerHour(startTimeInMin);
            final int resourceDurationTimeInMin = templateResource.getDuration();
            final int endTimeInMin = startTimeInMin + resourceDurationTimeInMin;
            final int endHr = divideByMinsPerHour(endTimeInMin);
            // the start/end-TimeInMin already accounts for how many days into
            // the template does the resource start/end
            final int startDay = 0;
            final int endDay = 0;
            final Date startTime = modifyDateFieldPlusAmtSetHourMinute(
                                        searchDate, Calendar.DAY_OF_YEAR, startDay,
                                        startHr, moduloMinsPerHour(startTimeInMin));
            final Date endTime = modifyDateFieldPlusAmtSetHourMinute(
                                        searchDate, Calendar.DAY_OF_YEAR, endDay,
                                        endHr, moduloMinsPerHour(endTimeInMin));

            templateResource.setScheduledStartTime(startTime);
            templateResource.setScheduledEndTime(endTime);
            startTimeInMin = endTimeInMin;

            for (final TemplateResource tr : templateResources) {
                if (tr.getStartDate().getTime() == templateResource.getStartDate().getTime()) {

                    templateResource.setScheduledStartTime(tr.getScheduledStartTime());
                    templateResource.setScheduledEndTime(tr.getScheduledEndTime());
                    startTimeInMin = endTimeInMin - resourceDurationTimeInMin;
                }
            }
            templateResources.add(templateResource);
        }
    }

    /**
     * Checks and returns List of available resources only if every single
     * resource in the List is available. This method returns a list of resource
     * Slots because If the specified resource in the list is not found
     * available then its alternates are checked for availability for the given
     * time. So the Returned List could contain either the given resources or
     * one of their alternates If none available returns null
     *
     * @param resources
     * @return
     */
    List<TemplateResource> checkTimeSlotAvailability(
            final List<TemplateResource> resources,
            final boolean rejectedCheck,
            final List<BookedResource> availableBookedResourceSlots,
            final List<TemplateResource> availableResourceSlots) {

        final List<TemplateResource> availableSlots = new ArrayList<TemplateResource>();
        availableSlots.addAll(availableResourceSlots);
        for (final TemplateResource resource : resources) {
            final TemplateResource slot = findAvailableResource(
                    resource,
                    rejectedCheck,
                    availableBookedResourceSlots,
                    availableSlots);
            if (slot == null) {
                availableSlots.clear();
                return availableSlots;
            }
            availableSlots.add(slot);
        }
        return availableSlots;
    }

    List<TemplateResource> findAvailablePermutationSequence(
            final List<TemplateResource> resources,
            final int minuteOffsetForSlot,
            final Date searchDate,
            final boolean rejectedCheck,
            final String groupType,
            final List<BookedResource> availableBookedResourceSlots,
            final List<TemplateResource> availableResourceSlots) {

        List<TemplateResource> availableSequence = null;
        List<TemplateResource> defaultSequence = null;
        boolean defaultSequences = true;
        TemplateResource previousFirstResource = null;
        boolean isFirstResourceAvailable = true;
        List<TemplateResource> foundSlots;
        TemplateResource slot;
        // minuteOffset will be zero in the case of 'clock time'
        final int baseStartTime = findMultiDayMinutesOffset(resources, minuteOffsetForSlot);

        final PermutationGenerator generator = new PermutationGenerator(resources.size());

        while (generator.hasMore()) {
            availableSequence = new ArrayList<>();
            final List<Integer> indices = generator.getNext();
            final TemplateResource currentFirstResource = duplicateResourceSlot(resources.get(indices.get(0)));

            for (final int index : indices) {
                slot = duplicateResourceSlot(resources.get(index));
                slot.setResourceGroupType(groupType);
                availableSequence.add(slot);
            }

            if (defaultSequences) {
                defaultSequence = new ArrayList<TemplateResource>();
                defaultSequences = false;
                defaultSequence.addAll(availableSequence);
            }

            if (previousFirstResource != null &&
                    previousFirstResource.getId().equals(currentFirstResource.getId())) {

                if (!isFirstResourceAvailable) {

                    availableSequence = null;
                    continue;// skip processing this sequence as the resource in
                    // the first slot was not found available
                } else {

                    this.allocateTime(availableSequence, baseStartTime, searchDate);
                    // The resource in the first index is found available. Hence
                    // continue to find availability of the rest of the
                    // resources in the Sequence
                    foundSlots = checkTimeSlotAvailability(availableSequence, rejectedCheck,
                            availableBookedResourceSlots, new
                                    ArrayList<TemplateResource>());
                    if (!foundSlots.isEmpty()) {
                        return foundSlots;// No further processing is required.
                        // The first available sequence is returned
                    } else {
                        availableSequence = null;
                    }
                }
            } else {

                previousFirstResource = currentFirstResource;
                this.allocateTime(availableSequence, baseStartTime, searchDate);
                final TemplateResource foundSlot = findAvailableResource(
                        availableSequence.get(0),
                        rejectedCheck,
                        availableBookedResourceSlots,
                        availableResourceSlots);
                if (foundSlot != null) {

                    // The resource in the first index is found available. Hence
                    // continue to find availability of the rest of the
                    // resources in the Sequence
                    isFirstResourceAvailable = true;
                    availableResourceSlots.add(foundSlot);
                    availableSequence.remove(foundSlot);
                    foundSlots = checkTimeSlotAvailability(availableSequence, rejectedCheck,
                            availableBookedResourceSlots, availableResourceSlots);
                    if (!foundSlots.isEmpty()) {
                        return foundSlots;// No further processing is required.
                        // THe first available sequence is
                        // returned
                    } else {
                        availableResourceSlots.remove(foundSlot);
                        availableSequence = null;
                    }
                } else {

                    // if first resource not available then skip processing any
                    // such sequence which starts with the current First element
                    isFirstResourceAvailable = false;
                    availableSequence = null;
                    continue;
                }
            }
        }

        if (rejectedCheck && availableSequence == null) {
            final List<TemplateResource> rejectedSequence = new ArrayList<TemplateResource>();
            for (final TemplateResource resource : defaultSequence) {
                resource.setFlexible(false);
                slot = findAvailableResource(resource, rejectedCheck, availableBookedResourceSlots,
                        availableResourceSlots);
                slot.setFlexible(true);
                availableResourceSlots.add(slot);
                rejectedSequence.add(slot);
            }
            return rejectedSequence;
        }
        return availableSequence;
    }

    List<TemplateResource> allocateCheckAvailabilityFlip(
            final int visitTimeSlotStart,
            final Date searchDate,
            final Map<String, List<TemplateResource>> flipResources,
            final boolean rejectedCheck,
            final String groupType,
            final List<BookedResource> availableResourceSlots) {

        final List<TemplateResource> availableFlipResourceSlots = new ArrayList<TemplateResource>();

        for (final Map.Entry<String, List<TemplateResource>> flipGrp : flipResources.entrySet()) {
            final List<TemplateResource> resources = flipGrp.getValue();

            final List<TemplateResource> availableFlipGrp = this.findAvailablePermutationSequence(
                    resources,
                    visitTimeSlotStart,
                    searchDate,
                    rejectedCheck,
                    groupType,
                    availableResourceSlots,
                    availableFlipResourceSlots);
            if (isNullOrEmpty(availableFlipGrp)) {
                // The resources in this flip group are not available in any order .
                // Stop processing any further and return to the calling method.
                return null;
            } else {
                // add the resources in the Flip group to the list of available Resources.
                availableFlipResourceSlots.addAll(removeDuplicates(availableFlipGrp));
            }
        }
        return availableFlipResourceSlots;
    }

    boolean foundAvailableFlipResources(
            final Date searchDate,
            final Map<String, List<TemplateResource>> flipResourceGroups,
            final int visitTimeSlotStart,
            final BookedVisit bookedVisit,
            final boolean rejectedCheck,
            final String groupType) {

        boolean result = true;
        if (MiscUtil.isNonNullNonEmpty(flipResourceGroups)) {
            final List<TemplateResource> availableFlipResources = allocateCheckAvailabilityFlip(
                    visitTimeSlotStart,
                    searchDate,
                    flipResourceGroups,
                    rejectedCheck,
                    groupType,
                    bookedVisit.getBookedResourceList());

            if (isNonNullNonEmpty(availableFlipResources)) {
                this.addResourcesToBookedVisit(removeDuplicates(availableFlipResources), bookedVisit);
            } else {
                // No (further) work on this time slot (?)
                result = false;
            }
        }
        return result;
    }

    void allocateTime(final TemplateResource resource,
                      final int visitTimeSlotStart,
                      final Date searchDate) {

        final int resourceDurationTimeInMin = resource.getDuration();
        final int resourceStartTimeInMin =
                resource.getStartDate().getHours() * MINS_PER_HR + resource.getStartDate().getMinutes();
        final int resourceEndTimeInMin = resourceStartTimeInMin + resourceDurationTimeInMin;
        final int slotOffsetResourceStartMinute = visitTimeSlotStart + resourceStartTimeInMin;
        final int slotOffsetResourceEndMinute = visitTimeSlotStart + resourceEndTimeInMin;
        final Date startDate = new Date(searchDate.getTime() + slotOffsetResourceStartMinute * MILLISECS_PER_MIN);
        final Date candidateResourceStartTime = modifyDateFieldPlusAmtSetHourMinute(
                startDate, Calendar.DAY_OF_YEAR, getTemplateResourceDay(resource.getStartDate()) -
                        1, multiDayMinutesToDailyHour(slotOffsetResourceStartMinute),
                multiDayMinutesToHourlyMinute(slotOffsetResourceStartMinute));

        final Calendar candidateEndCalendar = Calendar.getInstance();
        candidateEndCalendar.setTimeInMillis(
                candidateResourceStartTime.getTime() + resourceDurationTimeInMin * MILLISECS_PER_MIN);
        final Date candidateResourceEndTime = candidateEndCalendar.getTime();

        resource.setAvailableStartTimeInMin(slotOffsetResourceStartMinute);
        resource.setAvailableEndTimeInMin(slotOffsetResourceEndMinute);

        resource.setScheduledStartTime(candidateResourceStartTime);
        resource.setScheduledEndTime(candidateResourceEndTime);
    }

    List<TemplateResource> allocateCheckAvailabilityUserPreferred(
            final List<TemplateResource> resources,
            final int visitTimeSlotStart,
            final Date searchDate,
            final boolean rejectedCheck,
            final List<BookedResource> availableBookedResourceSlots,
            final List<TemplateResource> foundAvailableSlots) {

        for (final TemplateResource templateResource : resources) {
            allocateTime(templateResource, visitTimeSlotStart, searchDate);
            final TemplateResource foundSlot = findAvailableResource(
                                                    templateResource,
                                                    rejectedCheck,
                                                    availableBookedResourceSlots,
                                                    foundAvailableSlots);
            if (foundSlot == null) {
                return null;
            }
            foundAvailableSlots.add(foundSlot);
        }
        return foundAvailableSlots;
    }

    boolean possiblyCheckVisitEnd(
            final boolean yesCheck,
            final int visitTimeSlotEnd,
            final int floatStart,
            final int resourceDurationTimeInMin) {

        return !yesCheck || isStartPlusDurationLteEnd(visitTimeSlotEnd, floatStart, resourceDurationTimeInMin);
    }

    void prepareResourceTimeSlots(final int resourceStartTime,
                                  final int resourceEndTime,
                                  final Date scheduledStartTime,
                                  final Date scheduledEndTime,
                                  final List<TemplateResource> resources) {
        for (final TemplateResource resource : resources) {
            resource.setAvailableStartTimeInMin(resourceStartTime);
            resource.setAvailableEndTimeInMin(resourceEndTime);
            resource.setScheduledStartTime(scheduledStartTime);
            resource.setScheduledEndTime(scheduledEndTime);
        }
    }

    List<TemplateResource> findTemplateResourceSlots(
            final List<TemplateResource> resources,
            final int visitTimeSlotEnd,
            final Date searchDate,
            final boolean notUsingClockTime,
            final int floatDelta,
            final boolean rejectedCheck,
            final List<BookedResource> availableBookedResourceSlots,
            final List<TemplateResource> availableResourceSlots) {

        final TemplateResource resource = resources.get(0);

        final int resourceDurationTimeInMin = resource.getDuration();

        // these minutes are multi-day, i.e., hour 1 of day 2 is (1440 + 60),
        // not 60
        final int resourceFloatDurationStartTimeInMin = resource.getFloatStart();
        final int resourceFloatDurationEndTimeInMin = resource.getFloatEnd();

        int floatStart = floatDelta + resourceFloatDurationStartTimeInMin;
        final int floatEnd = floatDelta + resourceFloatDurationEndTimeInMin;

        for (;
             isStartPlusDurationLteEnd(floatEnd, floatStart, resourceDurationTimeInMin)
                     &&
             possiblyCheckVisitEnd(notUsingClockTime, visitTimeSlotEnd,
                                    floatStart, resourceDurationTimeInMin);
             floatStart += INCREMENT_FACTOR) {

            final int startDays = convertMinutesToStartDayZeroOffset(floatStart);
            final int startHours = convertMinutesToHourInDay(floatStart);
            final int startMinutes = convertTotalMinutesToMinutesInHour(floatStart);

            final int endFloatMinutes = floatStart + resourceDurationTimeInMin;
            final int endDays = convertMinutesToStartDayZeroOffset(endFloatMinutes);
            final int endHours = convertMinutesToHourInDay(endFloatMinutes);
            final int endMinutes = convertTotalMinutesToMinutesInHour(endFloatMinutes);

            final Calendar startCalendar = Calendar.getInstance();
            startCalendar.setTime(searchDate);
            startCalendar.add(Calendar.DATE, startDays);
            startCalendar.set(Calendar.HOUR_OF_DAY, startHours);
            startCalendar.add(Calendar.MINUTE, startMinutes);

            final Date startTime = startCalendar.getTime();

            final Calendar endCalendar = Calendar.getInstance();
            endCalendar.setTime(searchDate);
            endCalendar.add(Calendar.DATE, endDays);
            endCalendar.set(Calendar.HOUR_OF_DAY, endHours);
            endCalendar.add(Calendar.MINUTE, endMinutes);

            final Date endTime = endCalendar.getTime();

            prepareResourceTimeSlots(
                    floatStart,
                    floatStart + resourceDurationTimeInMin,
                    startTime,
                    endTime,
                    resources);

            final List<TemplateResource> foundSlots = checkTimeSlotAvailability(
                    resources,
                    rejectedCheck,
                    availableBookedResourceSlots,
                    availableResourceSlots);

            if (!foundSlots.isEmpty()) {
                return foundSlots;
            }
        }
        return null;
    }

    List<TemplateResource> findAvailableFloatDurationSlots(
            final List<TemplateResource> resources,
            final int visitTimeSlotStart,
            final int visitTimeSlotEnd,
            final Date searchDate,
            final boolean rejectedCheck,
            final List<BookedResource> availableBookedResourceSlots,
            final List<TemplateResource> availableResourceSlots) {

        List<TemplateResource> foundSlots;
        final List<TemplateResource> originalResources = new ArrayList<TemplateResource>();
        if (rejectedCheck) {
            originalResources.addAll(resources);
            foundSlots = allocateCheckAvailabilityUserPreferred(
                    resources,
                    visitTimeSlotStart,
                    searchDate,
                    rejectedCheck,
                    availableBookedResourceSlots,
                    availableResourceSlots);
            if (isNonNullNonEmpty(foundSlots)) {
                return foundSlots;
            } else {
                final boolean notUsingClockTime = true;
                foundSlots = findTemplateResourceSlots(
                        resources,
                        visitTimeSlotEnd,
                        searchDate,
                        notUsingClockTime,
                        visitTimeSlotStart,
                        rejectedCheck,
                        availableBookedResourceSlots,
                        availableResourceSlots);
                if (isNonNullNonEmpty(foundSlots)) {
                    return foundSlots;
                } else {
                    for (final TemplateResource r : originalResources) {
                        r.setFloatable(false);
                    }
                    foundSlots = allocateCheckAvailabilityUserPreferred(
                            resources,
                            visitTimeSlotStart,
                            searchDate,
                            rejectedCheck,
                            availableBookedResourceSlots,
                            availableResourceSlots);
                    for (final TemplateResource r : originalResources) {
                        r.setFloatable(true);
                    }
                    return foundSlots;
                }
            }
        } else {
            // Check if User preferred Time Slot is found available
            foundSlots = allocateCheckAvailabilityUserPreferred(
                    resources,
                    visitTimeSlotStart,
                    searchDate,
                    rejectedCheck,
                    availableBookedResourceSlots,
                    availableResourceSlots);

            if (isNonNullNonEmpty(foundSlots)) {
                return foundSlots;
            }

            final boolean notUsingClockTime = true;
            return findTemplateResourceSlots(
                    resources,
                    visitTimeSlotEnd,
                    searchDate,
                    notUsingClockTime,
                    visitTimeSlotStart,
                    rejectedCheck,
                    availableBookedResourceSlots,
                    availableResourceSlots);
        }
    }

    void prepareAndAddToAvailableSlots(
            final int resourceStartTime,
            final int resourceEndTime,
            final Date scheduledStartTime,
            final Date scheduledEndTime,
            final List<TemplateResource> currentResources,
            final List<TemplateResource> availableResourceSlots,
            final String groupType,
            final boolean singleFloatResources) {

        for (final TemplateResource resource : currentResources) {
            if (!singleFloatResources &&
                    resource.getGroupId().equals(currentResources.get(
                        currentResources.size() - 1).getGroupId())) {
                resource.setAvailableStartTimeInMin(resourceStartTime);
                resource.setAvailableEndTimeInMin(resourceEndTime);
                resource.setScheduledStartTime(scheduledStartTime);
                resource.setScheduledEndTime(scheduledEndTime);
            }
            resource.setResourceGroupType(groupType);
            availableResourceSlots.add(resource);
        }
    }

    List<TemplateResource> allocateCheckAvailabilityFloat(
            final int visitTimeSlotStart,
            final int visitTimeSlotEnd,
            final Date searchDate,
            final Map<String, List<TemplateResource>> floatResources,
            final boolean rejectedCheck,
            final String groupType,
            final List<BookedResource> availableBookedResourceSlots) {

        final List<TemplateResource> availableResourceSlots = new ArrayList<TemplateResource>();

        for (final Map.Entry<String, List<TemplateResource>> floatGrps
                                                    : floatResources.entrySet()) {

            final List<TemplateResource> resources = floatGrps.getValue();
            if (resources.isEmpty()) {
                continue;
            }
            /*
             * Check if the user defined start and End Times for this Resource
             * Group is available. If available, then add the resources in the
             * floatGroup to the availableList of resources and return to the
             * calling method. If not available, then continue to process until
             * the resource time slots are checked for all possible time slots
             * in the float duration.
             */
            final List<TemplateResource> availableSlots = new ArrayList<TemplateResource>();
            availableSlots.addAll(availableResourceSlots);
            final List<TemplateResource> availableFloatResources =
                    findAvailableFloatDurationSlots(
                        resources,
                        visitTimeSlotStart,
                        visitTimeSlotEnd,
                        searchDate,
                        rejectedCheck,
                        availableBookedResourceSlots,
                        availableSlots);

            if (isNullOrEmpty(availableFloatResources)) {
                return null;
            }
            prepareAndAddToAvailableSlots(
                    resources.get(0).getAvailableStartTimeInMin(),
                    resources.get(0).getAvailableEndTimeInMin(),
                    resources.get(0).getScheduledStartTime(),
                    resources.get(0).getScheduledEndTime(),
                    availableFloatResources,
                    availableResourceSlots,
                    groupType,
                    resources.size() == 1
            );
        }

        return availableResourceSlots;
    }

    boolean foundAvailableFloatResources(
            final int visitDurationInMin,
            final Date searchDate,
            final Map<String, List<TemplateResource>> floatTrList,
            final int visitTimeSlotStart,
            final BookedVisit bookedVisit,
            final boolean rejectedCheck,
            final String groupType) {

        boolean result = true;
        final Calendar startCal = Calendar.getInstance();
        startCal.setTime(searchDate);
        final Date searchStartDate = DateUtility.startOfDay(startCal);

        if (MiscUtil.isNonNullNonEmpty(floatTrList)) {
            final List<TemplateResource> availableFloatResourceList =
                    removeDuplicates(allocateCheckAvailabilityFloat(
                            visitTimeSlotStart,
                            visitTimeSlotStart + visitDurationInMin,
                            searchStartDate,
                            floatTrList,
                            rejectedCheck,
                            groupType,
                            bookedVisit.getBookedResourceList()));

            if (isNonNullNonEmpty(availableFloatResourceList)) {
                if (    groupType.equalsIgnoreCase("float") &&
                        availableFloatResourceList.size() == floatTrList.size()
                    ||
                        groupType.equalsIgnoreCase("float group")) {

                    this.addResourcesToBookedVisit(availableFloatResourceList,
                                                    bookedVisit);
                } else {
                    result = false;
                }
            } else {
                // No (further) work on this time slot (?)
                result = false;
            }
        }
        return result;
    }

    void calculateSomeAvailableResources(
            final int visitDurationInMin,
            final Date searchDate,
            final List<BookedVisit> candidateVisits,
            final List<TemplateResource> fixedResources,
            final Map<String,
                    List<TemplateResource>> floatTrList,
            final Map<String, List<TemplateResource>> floatResourceGroups,
            final Map<String, List<TemplateResource>> flipResourceGroups,
            final int visitTimeSlotStart,
            final BookedVisit bookedVisit,
            final boolean rejectedCheck) {

        this.addToCandidateVisitList(bookedVisit, candidateVisits);

        // 1. Handle Time allocation and availability check for fixed resources
        final boolean fixedResourcesAvailable =
                foundAvailableFixedResources(
                    searchDate,
                    fixedResources,
                    visitTimeSlotStart,
                    bookedVisit,
                    rejectedCheck,
                    "fixed");
        if (isRelativeTimeRejectedVisit(
                bookedVisit,
                rejectedCheck,
                fixedResourcesAvailable)) {
            return;
        }

        final boolean singleFloatResourcesAvailable =
                foundAvailableFloatResources(
                        visitDurationInMin,
                        searchDate,
                        floatTrList,
                        visitTimeSlotStart,
                        bookedVisit,
                        rejectedCheck,
                        "float");
        // 2. Next Check for availability of all the individual Floating
        // resources (not groups).
        if (isRelativeTimeRejectedVisit(
                bookedVisit,
                rejectedCheck,
                singleFloatResourcesAvailable)) {
            return;
        }

        final boolean floatGroupResourcesAvailable =
                foundAvailableFloatResources(
                        visitDurationInMin,
                        searchDate,
                        floatResourceGroups,
                        visitTimeSlotStart,
                        bookedVisit,
                        rejectedCheck,
                        "float group");
        // 3. Next Check for availability of all the Floating resource Groups.
        if (isRelativeTimeRejectedVisit(
                bookedVisit,
                rejectedCheck,
                floatGroupResourcesAvailable)) {
            return;
        }

        final boolean flexGroupResourcesAvailable =
                foundAvailableFlipResources(
                        searchDate,
                        flipResourceGroups,
                        visitTimeSlotStart,
                        bookedVisit,
                        rejectedCheck,
                        "flex");
        // 4. Next Check for availability of all the Flip resource Groups.
        if (isRelativeTimeRejectedVisit(
                bookedVisit,
                rejectedCheck,
                flexGroupResourcesAvailable)) {
            return;
        }
    }

    Date modifyDateFieldPlusAmtSetHourMinute(final Date date,
                                             final int field,
                                             final int amount,
                                             final int hour,
                                             final int minute) {
        final Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(field, amount);
        cal.set(Calendar.HOUR_OF_DAY, hour);
        cal.set(Calendar.MINUTE, minute);
        return cal.getTime();
    }

    void addToCandidateVisitList(final BookedVisit candidateVisit,
                                 final List<BookedVisit> candidateVisits) {
        final List<BookedVisit> candidateVisitsToAdd;
        if (candidateVisits == null) {
            candidateVisitsToAdd = Lists.newArrayList();
        } else {
            candidateVisitsToAdd = candidateVisits;
        }
        candidateVisitsToAdd.add(candidateVisit);
    }

    int findEarliestInpatientClockStartTime(final List<TemplateResource> resources) {
        int earliestStartTimeInMin = Integer.MAX_VALUE;
        int currentStartTimeInMin = 0;
        for (final TemplateResource r : resources) {
            if (r.getStartDate() != null) {
                currentStartTimeInMin = getTemplateResourceDay(r.getStartDate()) * MINS_PER_DAY +
                        r.getStartDate().getHours() * MINS_PER_HR + r.getStartDate().getMinutes() -
                        MINS_PER_DAY;
            }
            if (currentStartTimeInMin < earliestStartTimeInMin) {
                earliestStartTimeInMin = currentStartTimeInMin;
            }
        }
        return earliestStartTimeInMin;
    }

    int findLatestInpatientEndTime(final List<TemplateResource> resources) {
        int latestEndTimeInMin = 0;
        int currentEndTimeInMin = 0;
        for (final TemplateResource r : resources) {
            if (r.getEndDate() != null) {
                currentEndTimeInMin =
                        getTemplateResourceDay(r.getEndDate()) * MINS_PER_DAY +
                        r.getEndDate().getHours() * MINS_PER_HR +
                        r.getEndDate().getMinutes() -
                        MINS_PER_DAY;
            }
            if (latestEndTimeInMin == 0) {
                latestEndTimeInMin = currentEndTimeInMin;
            } else if (latestEndTimeInMin > 0 &&
                    currentEndTimeInMin > latestEndTimeInMin) {

                latestEndTimeInMin = currentEndTimeInMin;
            }
        }
        return latestEndTimeInMin;
    }

    int subtractDaysWorthOfMinutes(final int totalBefore, final int numDaysToSub) {
        return totalBefore - numDaysToSub * MINS_PER_DAY;
    }

    BookedVisit initializeClockTimeBookedVisit(final VisitTemplate visitTemplate, final Date searchDate) {
        final BookedVisit bookedVisit = new BookedVisit();
        bookedVisit.setId(1234556);
        bookedVisit.setName(visitTemplate.getName());
        bookedVisit.setStudy(visitTemplate.getStudy());
        bookedVisit.setVisitTemplate(visitTemplate);
        bookedVisit.setVisitType(visitTemplate.getVisitType());
        final List<TemplateResource> trs =
                templateResourceDAO.findTemplateResourcesByVisit(visitTemplate);
        final int startTimeInMin = findEarliestInpatientClockStartTime(trs);
        final int endTimeInMin = findLatestInpatientEndTime(trs);

        final int startDay = startTimeInMin / MINS_PER_DAY;
        final int startInMin =
                subtractDaysWorthOfMinutes(startTimeInMin, startDay);
        final int startHr = divideByMinsPerHour(startInMin);
        final int startMin = moduloMinsPerHour(startInMin);

        final int endDay = divideByMinsPerDay(endTimeInMin);
        final int endInMin = subtractDaysWorthOfMinutes(endTimeInMin, endDay);
        final int endHr = divideByMinsPerHour(endInMin);
        final int endMin = moduloMinsPerHour(endInMin);

        final Date startTime = modifyDateFieldPlusAmtSetHourMinute(
                searchDate,
                Calendar.DAY_OF_YEAR,
                startDay,
                startHr,
                startMin);
        final Date endTime = modifyDateFieldPlusAmtSetHourMinute(
                searchDate,
                Calendar.DAY_OF_YEAR,
                endDay,
                endHr,
                endMin);

        bookedVisit.setScheduledStartTime(startTime);
        bookedVisit.setScheduledEndTime(endTime);
        return bookedVisit;
    }

    boolean isRejectedVisit(
            final boolean rejectedCheck,
            final BookedVisit bookedVisit,
            final List<TemplateResource> availableResourceList) {

        if (isNonNullNonEmpty(availableResourceList)) {
            bookedVisit.setRejectedVisit(false);
            this.addResourcesToBookedVisit(availableResourceList, bookedVisit);
            if (rejectedCheck) {
                bookedVisit.setRejectedVisit(true);
            }
        } else {
            bookedVisit.setRejectedVisit(true);
            bookedVisit.setBookedResourceList(new ArrayList<BookedResource>());
            return true;
        }
        return false;
    }

    void calculateAvailableVisitTimeSlotClockTime(
        final VisitTemplate visitTemplate,
        final SubjectMrn subjectMrn,
        final Date searchDate,
        final Date endDateParam,
        final List<BookedVisit> candidateVisits,
        final List<TemplateResource> fixedResources,
        final Map<String, List<TemplateResource>> floatTrList,
        final Map<String, List<TemplateResource>> floatResourceGroups,
        final Map<String, List<TemplateResource>> flipResourceGroups,
        final boolean rejectedCheck) {

        final BookedVisit bookedVisit =
                initializeClockTimeBookedVisit(visitTemplate, searchDate);

        final Date endDate =
                modifyDateFieldPlusAmtSetHourMinute(
                        endDateParam,
                        Calendar.DAY_OF_YEAR,
                        0,
                        bookedVisit.getScheduledEndTime().getHours(),
                        bookedVisit.getScheduledEndTime().getMinutes());

        if (bookedVisit.getScheduledEndTime().equals(endDate) ||
                bookedVisit.getScheduledEndTime().before(endDate)) {

            bookedVisit.setSubjectMrn(subjectMrn);
            final String uniqueKey = RandomStringUtils.randomNumeric(8);
            bookedVisit.setUniquekey(uniqueKey);

            addToCandidateVisitList(bookedVisit, candidateVisits);

            // 1. Handle Time allocation and availability check for fixed
            // resources
            allocateTimeFixedResourcesClockTime(
                    searchDate,
                    fixedResources,
                    "fixed");

            final List<TemplateResource> availableFixedResources =
                    calculateFixedResourceAvailability(
                            fixedResources,
                            rejectedCheck,
                            bookedVisit.getBookedResourceList());

            if (isRejectedVisit(
                    rejectedCheck,
                    bookedVisit,
                    availableFixedResources)) {
                return;
            }
            // 2. Next Check for availability of all the individual Floating
            // resources (not groups).
            if (MiscUtil.isNonNullNonEmpty(floatTrList)) {
                final List<TemplateResource> availableFloatResourceList =
                        removeDuplicates(
                                allocateCheckAvailabilityFloatClockTime(
                                        searchDate,
                                        floatTrList,
                                        rejectedCheck,
                                        "float",
                                        bookedVisit.getBookedResourceList()));

                if (isRejectedVisit(
                        rejectedCheck,
                        bookedVisit,
                        availableFloatResourceList)) {
                    return;
                }
            }
            // 3. Next Check for availability of all the Floating resource
            // Groups.
            if (MiscUtil.isNonNullNonEmpty(floatResourceGroups)) {
                final List<TemplateResource> availableFloatResources =
                        removeDuplicates(
                                allocateCheckAvailabilityFloatClockTime(
                                    searchDate,
                                    floatResourceGroups,
                                    rejectedCheck,
                                    "float group",
                                    bookedVisit.getBookedResourceList()));

                if (isRejectedVisit(
                        rejectedCheck,
                        bookedVisit,
                        availableFloatResources)) {
                    return;
                }
            }
            // 4. Next Check for availability of all the Flip resource Groups.
            if (MiscUtil.isNonNullNonEmpty(flipResourceGroups)) {
                final List<TemplateResource> availableFlipResources =
                        removeDuplicates(
                                allocateCheckAvailabilityFlipClockTime(
                                        searchDate,
                                        flipResourceGroups,
                                        rejectedCheck,
                                        "flex",
                                        bookedVisit.getBookedResourceList()));

                if (isRejectedVisit(
                        rejectedCheck,
                        bookedVisit,
                        availableFlipResources)) {
                    return;
                }
            }
        }
    }

    <A> List<A> removeDuplicates(final List<A> availableResourceList) {
        if (isNonNullNonEmpty(availableResourceList)) {
            return enrich(availableResourceList).distinct().toList();
        }

        return availableResourceList;
    }

    int getTemplateResourceDay(final Date r) {
        if (r.getMonth() > Calendar.FEBRUARY) {
            //NB: XXX Why always 29??
            //TODO: Use java.time classes
            return r.getDate() + (r.getMonth() - 1) * 29;
        } else {
            return r.getDate();
        }
    }

    void allocateTimeFixedResourcesClockTime(final Date searchDate, final List<TemplateResource> fixedResources,
                                             final String groupType) {
        int startDay = 1;
        int endDay = 1;
        for (final TemplateResource resource : fixedResources) {
            // set the resource start and end Times
            if (resource.getStartDate() != null) {
                startDay = getTemplateResourceDay(resource.getStartDate());
            }
            if (resource.getEndDate() != null) {
                endDay = getTemplateResourceDay(resource.getEndDate());
            }
            startDay -= 1; // This value since the DurationDay always starts
            // with Day 1 for the first day of the visit.
            endDay -= 1;
            final Date startTime = modifyDateFieldPlusAmtSetHourMinute(searchDate, Calendar.DAY_OF_YEAR, startDay,
                    resource.getStartDate().getHours(), resource
                            .getStartDate().getMinutes());
            final Date endTime = modifyDateFieldPlusAmtSetHourMinute(searchDate, Calendar.DAY_OF_YEAR, endDay,
                    resource.getEndDate().getHours(), resource
                            .getEndDate().getMinutes());
            resource.setScheduledStartTime(startTime);
            resource.setScheduledEndTime(endTime);
            resource.setResourceGroupType(groupType);
        }
    }

    void allocateTimeClockTime(final TemplateResource resource, final Date searchDate) {

        final int resourceStartTimeInMin =
                resource.getStartDate().getHours() * MINS_PER_HR + resource.getStartDate().getMinutes();
        final int resourceEndTimeInMin =
                resource.getEndDate().getHours() * MINS_PER_HR + resource.getEndDate().getMinutes();
        final Date startTime = modifyDateFieldPlusAmtSetHourMinute(
                searchDate, Calendar.DAY_OF_YEAR, getTemplateResourceDay(resource.getStartDate()) -
                        1, resource.getStartDate().getHours(), resource.getStartDate()
                        .getMinutes());
        final Date endTime = modifyDateFieldPlusAmtSetHourMinute(
                searchDate, Calendar.DAY_OF_YEAR, getTemplateResourceDay(resource.getEndDate()) -
                        1, divideByMinsPerHour(resourceEndTimeInMin), moduloMinsPerHour
                        (resourceEndTimeInMin));
        resource.setAvailableStartTimeInMin(resourceStartTimeInMin);
        resource.setAvailableEndTimeInMin(resourceEndTimeInMin);
        resource.setScheduledStartTime(startTime);
        resource.setScheduledEndTime(endTime);
    }

    List<TemplateResource> allocateCheckAvailabilityUserPreferredClockTime(final List<TemplateResource> resources,
                                                                           final Date searchDate, final boolean
                                                                                   rejectedCheck, final
                                                                           List<BookedResource>
                                                                                   availableBookedResourceSlots,
                                                                           final List<TemplateResource>
                                                                                   foundAvailableSlots) {
        for (final TemplateResource resource : resources) {
            allocateTimeClockTime(resource, searchDate);
            final TemplateResource foundSlot = findAvailableResource(resource, rejectedCheck,
                    availableBookedResourceSlots, foundAvailableSlots);
            if (foundSlot != null) {
                foundAvailableSlots.add(foundSlot);
            } else {
                return null;
            }
        }
        return foundAvailableSlots;
    }

    List<TemplateResource> findAvailableFloatDurationSlotsClockTime(
            final List<TemplateResource> resources,
            final Date searchDate,
            final boolean rejectedCheck,
            final List<BookedResource> availableBookedResourceSlots,
            final List<TemplateResource> availableResourceSlots) {

        List<TemplateResource> foundSlots;
        final List<TemplateResource> originalResources = new ArrayList<TemplateResource>();
        if (rejectedCheck) {
            originalResources.addAll(resources);
            foundSlots = allocateCheckAvailabilityUserPreferredClockTime(resources, searchDate, rejectedCheck,
                    availableBookedResourceSlots,
                    availableResourceSlots);
            if (isNonNullNonEmpty(foundSlots)) {
                return foundSlots;
            } else {
                final boolean notUsingClockTime = false;
                final int visitTimeSlotEnd = -1;
                final int floatDelta = 0;
                foundSlots = findTemplateResourceSlots(resources, visitTimeSlotEnd, searchDate, notUsingClockTime,
                        floatDelta, rejectedCheck, availableBookedResourceSlots,
                        availableResourceSlots);
                if (isNonNullNonEmpty(foundSlots)) {
                    return foundSlots;
                } else {
                    for (final TemplateResource r : originalResources) {
                        r.setFloatable(false);
                    }
                    foundSlots = allocateCheckAvailabilityUserPreferredClockTime(originalResources, searchDate,
                            rejectedCheck,
                            availableBookedResourceSlots,
                            availableResourceSlots);
                    for (final TemplateResource r : originalResources) {
                        r.setFloatable(true);
                    }
                    return foundSlots;
                }
            }
        } else {
            // Check if User preferred Time Slot is found available
            foundSlots = allocateCheckAvailabilityUserPreferredClockTime(resources, searchDate, rejectedCheck,
                    availableBookedResourceSlots,
                    availableResourceSlots);
            if (isNonNullNonEmpty(foundSlots)) {
                return foundSlots;
            }
            final boolean notUsingClockTime = false;
            final int visitTimeSlotEnd = -1;
            final int floatDelta = 0;
            return findTemplateResourceSlots(resources, visitTimeSlotEnd, searchDate, notUsingClockTime, floatDelta,
                    rejectedCheck, availableBookedResourceSlots, availableResourceSlots);
        }
    }

    List<TemplateResource> allocateCheckAvailabilityFloatClockTime(
            final Date searchDate,
            final Map<String,
                    List<TemplateResource>> floatResources,
            final boolean rejectedCheck,
            final String groupType,
            final List<BookedResource> availableBookedResourceSlots) {

        final List<TemplateResource> availableResourceSlots = new ArrayList<TemplateResource>();
        final Calendar startCal = Calendar.getInstance();
        startCal.setTime(searchDate);
        final Date searchStartDate = DateUtility.startOfDay(startCal);
        for (final Map.Entry<String, List<TemplateResource>> floatGroups : floatResources.entrySet()) {
            final List<TemplateResource> resources = floatGroups.getValue();
            if (resources.isEmpty()) {
                continue;
            }
            // Check if the user defined start and End Times for this Resource
            // Grp is available.
            // If available, then add the resources in the floatGroup to the
            // availableList of resources and return to the calling method.
            // If not available, then continue to process until the resource
            // time slots are checked for all possible time slots in the float
            // duration.
            final List<TemplateResource> availableSlots = new ArrayList<TemplateResource>();
            availableSlots.addAll(availableResourceSlots);
            final List<TemplateResource> availableFloatResources = findAvailableFloatDurationSlotsClockTime
                    (resources, searchStartDate, rejectedCheck, availableBookedResourceSlots, availableSlots);
            if (isNonNullNonEmpty(availableFloatResources)) {
                prepareAndAddToAvailableSlots(
                        resources.get(0).getAvailableStartTimeInMin(), resources.get(0).getAvailableEndTimeInMin(),
                        resources.get(0).getScheduledStartTime(), resources.get(0).getScheduledEndTime(),
                        availableFloatResources, availableResourceSlots, groupType,
                        resources.size() == 1
                );
                continue;
            } else {
                // Do not process any further as one or more resources in this
                // visit are not available for the given time period.
                return null;
            }
        }
        return availableResourceSlots;
    }

    List<TemplateResource> allocateCheckAvailabilityFlipClockTime(final Date searchDate, final Map<String,
            List<TemplateResource>> flipResources, final boolean rejectedCheck, final String groupType, final
                                                                  List<BookedResource> availableBookedResourceSlots) {

        final List<TemplateResource> availableFlipResourceSlots = new ArrayList<>();

        //NB: Find a way to get rid of the side effect (mutating availableFlipResourceSlots) in the call to map() :(
        // :( :(
        final LazyList<List<TemplateResource>> lazyTemplateResourceLists = lazy(new ArrayList<>(flipResources
                .entrySet()))
                .map(flipGrp -> {
                    final List<TemplateResource> resources = flipGrp.getValue();

                    final List<TemplateResource> availableFlipGrp = removeDuplicates(findAvailablePermutationSequence
                            (resources, 0, searchDate,
                                    rejectedCheck, groupType,
                                    availableBookedResourceSlots,
                                    availableFlipResourceSlots));

                    if (isNullOrEmpty(availableFlipGrp)) {
                        // The resources in this flip group are not available in any
                        // order.
                        // Stop processing any further and return to the calling method.
                        return null;
                    } else {
                        // add the resources in the Flip group to the list of available
                        // Resources.
                        availableFlipResourceSlots.addAll(availableFlipGrp);
                    }

                    return availableFlipGrp;
                });

        if (lazyTemplateResourceLists.exists(MiscUtil::isNullOrEmpty)) {
            // The resources in this flip group are not available in any
            // order.
            // Stop processing any further and return to the calling method.
            return null;
        }

        return availableFlipResourceSlots;
    }

    // provided as helper for tests
    Map<Integer, Integer> getPeriodToQuantityMap(final List<ResourceSchedule> resourceScheduleList) {

        final Map<Integer, Integer> periodToQuantityMap = new HashMap<>();
        for (final ResourceSchedule resourceSchedule : resourceScheduleList) {
            loadResourceScheduleIntoPeriodToQuantityMap(resourceSchedule, periodToQuantityMap, 0);
        }
        return periodToQuantityMap;
    }

}
