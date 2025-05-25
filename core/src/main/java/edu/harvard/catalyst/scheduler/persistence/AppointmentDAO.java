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

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import edu.harvard.catalyst.scheduler.dto.SearchDTO;
import edu.harvard.catalyst.scheduler.dto.SwitchSubjectResultDTO;
import edu.harvard.catalyst.scheduler.dto.VisitRenderSummaryDTO;
import edu.harvard.catalyst.scheduler.dto.response.*;
import edu.harvard.catalyst.scheduler.dto.statics.CalendarFilter;
import edu.harvard.catalyst.scheduler.entity.*;
import edu.harvard.catalyst.scheduler.util.DateUtility;
import edu.harvard.catalyst.scheduler.util.SubjectDataEncryptor;
import org.hibernate.Session;
import org.hibernate.query.NativeQuery;
import org.hibernate.query.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.*;
import java.util.function.Function;

import static edu.harvard.catalyst.hccrc.core.util.Pair.pair;
import static edu.harvard.catalyst.hccrc.core.util.Pairs.toMap;
import static edu.harvard.catalyst.hccrc.core.util.RichList.enrich;
import static edu.harvard.catalyst.scheduler.core.SchedulerRuntimeException.logAndThrow;
import static edu.harvard.catalyst.scheduler.core.Statics.NA;
import static edu.harvard.catalyst.scheduler.core.Statics.NO_SUBJECT_ASSIGNED;
import static edu.harvard.catalyst.scheduler.util.MiscUtil.isNonNullNonEmpty;
import static edu.harvard.catalyst.scheduler.util.MiscUtil.negativeIsInfinity;
import static edu.harvard.catalyst.scheduler.util.SubjectDataEncryptor.decrypt;

;

/**
 * @author smitha
 *
 */
@Repository
@Transactional
public class AppointmentDAO extends SiteDAO {

    static boolean isAllDay(final boolean homeView, final VisitType visitType) {

        return isAllDay(homeView, visitType.isInpatient());
    }

    static boolean isAllDay(final boolean homeView, boolean isInpatient) {

         return homeView && isInpatient;
    }

    public List<SublocationClosureInterval> findSublocationSchedule(final Sublocation sublocation,
            final Date startDate, final Date endDate) {
        final String findSublocationClosure = "select s FROM SublocationClosureInterval s where s.sublocation = :sublocation AND "
                + " ((:startTime between s.startTime and s.endTime) "
                + " or (:endTime between s.startTime and s.endTime) "
                + " or (s.startTime >= :startTime and s.endTime <= :endTime))";

        final Query query = newQuery(findSublocationClosure);
        query.setParameter("sublocation", sublocation);
        query.setParameter("startTime", startDate);
        query.setParameter("endTime", endDate);
        return query.list();
    }

    public boolean isSublocationClosed(final Sublocation sublocation, final Date startDate, final Date endDate) {
        final String findSublocationClosure = "select s FROM SublocationClosureInterval s where s.sublocation = :sublocation AND "
                + " ((:startTime >= s.startTime and :startTime < s.endTime) "
                + " or (:endTime > s.startTime and :endTime <= s.endTime) "
                + " or (s.startTime >= :startTime and s.endTime <= :endTime))";


        final Query query = newQuery(findSublocationClosure);
        query.setParameter("sublocation", sublocation);
        query.setParameter("startTime", startDate);
        query.setParameter("endTime", endDate);

        @SuppressWarnings("unchecked")
        final List<SublocationClosureInterval> list = query.list();

        return !list.isEmpty();
    }

    @SuppressWarnings("unchecked")
    public List<BookedResource> findOverbookConflictResourcesByVisitStatus(final Resource resource, final Date startDate, final Date endDate) {

        final String findBookedResource = "select br FROM BookedResource br, BookedVisit bv where "
                + " br.bookedVisit = bv.id and bv.appointmentStatus.isOpen = TRUE and br.resource = :resource "
                + " and ((:startTime >= br.scheduledStartTime and :startTime < br.scheduledEndTime) "
                + " or (:endTime > br.scheduledStartTime and :endTime <= br.scheduledEndTime) "
                + " or (br.scheduledStartTime >= :startTime and br.scheduledEndTime <= :endTime)) ORDER BY br.scheduledStartTime";

        final Query query = newQuery(findBookedResource);

        query.setParameter("resource", resource);
        query.setParameter("startTime", startDate);
        query.setParameter("endTime", endDate);

        return query.list();
    }

    //Work around lambda limitations
    private static final class Ref<A> {
        private A a;

        Ref(final A a) {
            super();
            this.a = a;
        }

        A get() {
            return a;
        }

        void set(final A a) {
            this.a = a;
        }
    }

    public List<OverbookTimelineDataResponseDTO> getOverbookTimelineData(final Date startDate, final Date endDate, final ResourceType resourceType, final List<Integer> sublocations, final String orderBy, final User user) {

        final NativeQuery query = createOverbookTimelineQuery(startDate, endDate, resourceType, sublocations, orderBy);

        @SuppressWarnings("unchecked")
        final List<Object[]> resultRows = query.list();

        final long selectedMinutes = startDate.getTime() / DateUtility.MILLISECS_PER_MIN;

        //get all the dates between the search range
        final List<Date> selectedDates = DateUtility.dateInterval(startDate, endDate);

        //Work around lambda limitations
        final Ref<Integer> previousResource = new Ref<>(0);

        final Function<Object[], OverbookTimelineDataResponseDTO> toOverbookTimelineDataResponseDTO = resultRow -> {
            final Integer resource = (Integer) resultRow[0];
            final String resourceName = (String) resultRow[1];
            final Date scheduledStartTime = (Date) resultRow[7];
            final Date scheduledEndTime = (Date) resultRow[8];
            final Integer bookedVisitId = (Integer) resultRow[9];

            final boolean hasBookedVisitId = bookedVisitId != null;
            final boolean hasSubjectData = resultRow[10] != null;
            final boolean isNewResource = !resource.equals(previousResource.get());
            final boolean notStudyStaff = !user.isStudyStaff();
            final boolean hasBookedVisitIdAndNotStudyStaff = hasBookedVisitId && notStudyStaff;
            final boolean hasBookedVisitIdAndIsNewResource = hasBookedVisitId && isNewResource;

            final int rowCount = hasBookedVisitIdAndIsNewResource ? getResourceCount(resource, startDate, endDate) : 0;

            final long sixtySecondsInMillis = Duration.ofSeconds(DateUtility.MINS_PER_HR).toMillis();

            final String rooms = hasBookedVisitId ? findRoomString(bookedVisitId) : "";
            final long startTime = hasBookedVisitId ? (scheduledStartTime.getTime() / sixtySecondsInMillis) : 0;
            final long endTime = hasBookedVisitId ? (scheduledEndTime.getTime() / sixtySecondsInMillis) : 0;

            final String piName = hasBookedVisitIdAndNotStudyStaff ? (String)resultRow[5] : "";
            final String localId = hasBookedVisitIdAndNotStudyStaff ? (String)resultRow[6] : "";
            final String visit = hasBookedVisitIdAndNotStudyStaff ? (String)resultRow[4] : "";
            final String subjectFirstName = hasSubjectData ?
                    (hasBookedVisitIdAndNotStudyStaff ? decrypt((String)resultRow[10]) : "") : NO_SUBJECT_ASSIGNED;
            final String subjectLastName = hasSubjectData ?
                    (hasBookedVisitIdAndNotStudyStaff ? decrypt((String)resultRow[11]) : "") : NO_SUBJECT_ASSIGNED;

            final String visitHoverStartTime = DateUtility.format(DateUtility.date24HTime(), (Date) resultRow[2]);
            final String visitHoverEndTime = DateUtility.format(DateUtility.date24HTime(), (Date) resultRow[3]);

            final String resourceHoverStartTime = DateUtility.format(DateUtility.date24HTime(), (Date) resultRow[7]);
            final String resourceHoverEndTime = DateUtility.format(DateUtility.date24HTime(), (Date) resultRow[8]);

            final String alternateResources = isNewResource ? findAlternateResourcesString(resource) : "";

            final List<Map<String, String>> defaultResourceSchedule = isNewResource ? getDefaultResourceSchedule(selectedDates, resource) : new ArrayList<>();

            final List<Map<String, String>> temporaryResourceSchedule = isNewResource ? getTemporaryResourceSchedule(startDate, endDate, resource) : new ArrayList<>();

            final OverbookTimelineDataResponseDTO overbookTimeLineData = new OverbookTimelineDataResponseDTO(
                    resource,
                    resourceName,
                    rooms,
                    visitHoverStartTime,
                    visitHoverEndTime,
                    resourceHoverStartTime,
                    resourceHoverEndTime,
                    visit,
                    piName,
                    localId,
                    subjectFirstName,
                    subjectLastName,
                    startTime,
                    endTime,
                    selectedMinutes,
                    defaultResourceSchedule,
                    temporaryResourceSchedule,
                    alternateResources,
                    rowCount);

            previousResource.set(resource);

            return overbookTimeLineData;
        };

        return enrich(resultRows).map(toOverbookTimelineDataResponseDTO).toList();
    }

    private NativeQuery createOverbookTimelineQuery(Date startDate, Date endDate, ResourceType resourceType, List
                <Integer> sublocations, String orderBy) {
        final String[] columnsArray = {
                "resource.id",
                "resource.name",
                "bvStart",
                "bvEnd",
                "visit_template.name",
                "user.last_name",
                "study.local_id",
                "brStart",
                "brEnd",
                "bvId",
                "subject.first_name",
                "subject.last_name"
        };

        final String columnSqlList = Joiner.on(", ").join(columnsArray);

        final String filterBy = (sublocations.isEmpty()) ? " " : (" and resource_sublocation.sublocation IN (" + Joiner.on(",").join(sublocations) + ") ");

        String findBookedResource =
                "SELECT " + columnSqlList + " " +
                        "FROM resource_sublocation, resource " +
                        "LEFT OUTER JOIN (select br.resource, br.id, br.booked_visit, booked_visit.visit_template, booked_visit.id bvId," +
                        "booked_visit.scheduled_start_time AS bvStart, " +
                        "booked_visit.scheduled_end_time AS bvEnd, " +
                        "br.scheduled_start_time AS brStart, " +
                        "br.scheduled_end_time AS brEnd," +
                        "booked_visit.study, booked_visit.appointment_status, s.principal_investigator, su.id subjectId " +
                        "from booked_resource br, study s, booked_visit " +
                        "LEFT OUTER JOIN subject_mrn sm ON sm.id = booked_visit.subject_mrn " +
                        "LEFT OUTER JOIN subject su ON su.id = sm.subject " +
                        "LEFT JOIN appointment_status ON appointment_status.id = booked_visit.appointment_status " +
                        "WHERE " +
                        "br.booked_visit = booked_visit.id " +
                        "and booked_visit.study = s.id " +
                        "and appointment_status.is_open = 1 and  " +
                        "((:startTime between br.scheduled_start_time  " +
                        "                 and br.scheduled_end_time)  " +
                        "                 or (:endTime between br.scheduled_start_time " +
                        "                 and br.scheduled_end_time)  " +
                        "                 or (br.scheduled_start_time > :startTime and  " +
                        "                 br.scheduled_end_time < :endTime)) ) AS bookedData ON resource.id = bookedData.resource " +
                        "                LEFT OUTER JOIN study ON bookedData.study = study.id " +
                        "                LEFT OUTER JOIN subject ON bookedData.subjectId = subject.id " +
                        "                LEFT OUTER JOIN visit_template ON bookedData.visit_template = visit_template.id " +
                        "                LEFT OUTER JOIN user ON bookedData.principal_investigator = user.id " +
                        "                where resource_sublocation.resource = resource.id and " +
                        "                  resource_sublocation.active = 1 and resource.type IN (:resourceType) " +
                                         filterBy +
                        "                ORDER BY resource.name " + orderBy +", bookedData.brStart";

        final NativeQuery query = newNativeQuery(findBookedResource);

        query.setParameter("startTime", startDate);
        query.setParameter("endTime", endDate);
        query.setParameter("resourceType", resourceType.getName());

        for (final String column : columnsArray) {
            query.addScalar(column);
        }
        return query;
    }

    private List<Map<String, String>> getDefaultResourceSchedule(final List<Date> selectedDates, final Integer resource) {
        final List<Integer> dayOfWeeks = enrich(selectedDates).map(date -> DateUtility.getDayOfTheWeek(date)).toList();

        //get resource default availability for the search dates
        final List<ResourceSchedule> defaultSchedules = enrich(dayOfWeeks).flatMapList(day -> findResourceScheduleByResource(resource, day, false)).toList();

        final Function<ResourceSchedule, Map<String, String>> toDefaultScheduleMap = schedule -> toMap(
                pair("defaultScheduleTime", DateUtility.retrieveDayOfWeek(schedule.getDayOfWeek()) + ": " + DateUtility.format(DateUtility.hourMin(), schedule.getStartTime()) + " - " + DateUtility.format(DateUtility.hourMin(), schedule.getEndTime())),
                pair("defaultScheduleQuantity", String.valueOf(schedule.getQuantity())));

        return enrich(defaultSchedules).map(toDefaultScheduleMap).toList();
    }

    private List<Map<String, String>> getTemporaryResourceSchedule(final Date startDate, final Date endDate, final Integer resource) {
        final List<ResourceSchedule> exceptionSchedules = findExceptionSchedule(resource, startDate, endDate, true);

        final Function<ResourceSchedule, Map<String, String>> toExceptionScheduleMap = schedule -> toMap(
                pair("exceptionScheduleTime", DateUtility.format(DateUtility.date24HTime(), schedule.getStartTime()) + " - " + DateUtility.format(DateUtility.date24HTime(), schedule.getEndTime())),
                pair("exceptionScheduleQuantity", String.valueOf(schedule.getQuantity())));

        return enrich(exceptionSchedules).map(toExceptionScheduleMap).toList();
    }

    private int getResourceCount(final int resource, final Date startDate, final Date endDate) {
        final String findResourceCount = "SELECT br FROM BookedResource br, BookedVisit bv, Resource r " +
                " WHERE r.id = :resource AND br.resource = r.id and br.bookedVisit = bv.id and bv.appointmentStatus.isOpen = TRUE and  " +
                "   ((:startTime between br.scheduledStartTime  " +
                "                 and br.scheduledEndTime)  " +
                "                 or (:endTime between br.scheduledStartTime " +
                "                 and br.scheduledEndTime)  " +
                "                 or (br.scheduledStartTime > :startTime and  " +
                "                 br.scheduledEndTime < :endTime)) ORDER BY br.scheduledStartTime ";

        final Query query = newQuery(findResourceCount);

        query.setParameter("resource", resource);
        query.setParameter("startTime", startDate);
        query.setParameter("endTime", endDate);

        @SuppressWarnings("unchecked")
        final List<BookedResource> dateRangeList = query.list();

        return isOverlappingDates(dateRangeList);
    }

    public int isOverlappingDates(final List<BookedResource> dateRangeList) {
        int rowCount = 0;
        Date latestEndTimeRow1 = dateRangeList.get(0).getScheduledEndTime();
        Date latestEndTimeRow2 = dateRangeList.get(0).getScheduledEndTime();

        for (int index1 = 1; index1 < dateRangeList.size(); index1++) {

            final Date startDate = dateRangeList.get(index1).getScheduledStartTime();
            final Date endDate = dateRangeList.get(index1).getScheduledEndTime();

            if (startDate.getTime() >= latestEndTimeRow1.getTime()) {
                latestEndTimeRow1 = endDate;
                if (rowCount == 0) {
                    latestEndTimeRow2 = endDate;
                }
            }
            else if (startDate.getTime() < latestEndTimeRow1.getTime() && startDate.getTime() < latestEndTimeRow2.getTime()) {
                latestEndTimeRow2 = endDate;
                // new row created
                rowCount++;
            }
            else if (startDate.getTime() >= latestEndTimeRow2.getTime()) {
                // new entry on the same row or else could be in the first row (which is checked above)...so no need to create a new row
                latestEndTimeRow2 = endDate;
            }
            else if (startDate.getTime() < latestEndTimeRow2.getTime()) {
                latestEndTimeRow2 = endDate;
                // new row created
                rowCount++;
            }
        }
        return rowCount;
    }

    public List<ResourceSchedule> findExceptionSchedule(final Integer resourceId, final Date startDate, final Date endDate, final boolean override) {
        final Resource resource = findById(Resource.class, resourceId);
        final String findExceptionSchedule = "SELECT s FROM ResourceSchedule s WHERE s.resource = :resource AND  s.override = :override  AND "
                + " ((:startTime between s.startTime and s.endTime) " + " or (:endTime between s.startTime and s.endTime) "
                + " or (s.startTime >= :startTime and s.endTime <= :endTime)) ORDER BY s.startTime";

        final Query query = session().createQuery(findExceptionSchedule);

        query.setParameter("resource", resource);
        query.setParameter("override", override);
        query.setParameter("startTime", startDate);
        query.setParameter("endTime", endDate);

        return query.list();
    }

    public String findAlternateResourcesString(final Integer resourceId) {
        final Resource resource = findById(Resource.class, resourceId);
        final String hql = "select ra.alternateResource.name from ResourceAlternate ra where ra.sourceResource = :resource order by ra.alternateResource.name";

        final Query query = newQuery(hql);
        query.setParameter("resource", resource);
        final List<String> list = query.list();
        return Joiner.on(", ").join(list);
    }

    @SuppressWarnings("unchecked")
    public List<ResourceSchedule> findResourceScheduleByResource(final Integer resourceId, final Integer dayOfWeek, final boolean override) {
        final Resource resource = findById(Resource.class, resourceId);
        final CriteriaQueryHelper criteriaHelper = new CriteriaQueryHelper(session(), ResourceSchedule.class);

        criteriaHelper.whereEquals("resource", resource);
        criteriaHelper.whereEquals("dayOfWeek", dayOfWeek);
        criteriaHelper.whereEquals("override", override);
        criteriaHelper.orderAsc("startTime");
        return criteriaHelper.getQuery().list();
    }

    public TemplateResource findTemplateResourceById(final int id) {
        return this.findById(TemplateResource.class, id);
    }

    public CancellationStatus findCancellationStatusById(final int id) {
        return this.findById(CancellationStatus.class, id);
    }

    private AppointmentStatus findUniqueApptStatus(String flagName) {

        String hql = "SELECT apSt FROM AppointmentStatus apSt WHERE apSt." + flagName + " = TRUE";
        final Query query = newQuery(hql);
        List<AppointmentStatus> appointmentStatuses = query.list();
        if (appointmentStatuses.size() != 1) {
            logAndThrow("Could not find exactly one appointment status where " + flagName + " is true");
            // return statement is needed to make java compiler happy.
            // If the exception was thrown in this block of code it would not be needed.
            return null;
        }
        else {
            return appointmentStatuses.get(0);
        }

    }

    public AppointmentStatus findScheduledStatus() {

        return findUniqueApptStatus("isScheduled");

    }

    public AppointmentStatus findCheckedInStatus() {

        return findUniqueApptStatus("isCheckedIn");

    }

    public AppointmentStatus findCheckedOutStatus() {

        return findUniqueApptStatus("isCheckedOut");

    }

    public AppointmentStatus findCancelledStatus() {

        return findUniqueApptStatus("isCancelled");

    }

    public AppointmentStatus findHoldStatus() {

        return findUniqueApptStatus("isHold");

    }

    public TemplateResourceGroup findTemplateResourceGroup(final TemplateResource templateResource) {

        final VisitTemplate visit = templateResource.getVisitTemplate();
        final String groupId = templateResource.getGroupId();

        final CriteriaQueryHelper criteriaHelper = new CriteriaQueryHelper(session(), TemplateResourceGroup.class);
        criteriaHelper.whereEquals("visit", visit);
        criteriaHelper.whereEquals("templateResource", templateResource);
        criteriaHelper.whereEquals("groupId", groupId);
        final List<TemplateResourceGroup>  result = criteriaHelper.getQuery().list();
        return result.get(0);
    }

    public List<TemplateResourceGroup> findTrgListByGroupId(final String group) {

        final CriteriaQueryHelper criteriaHelper = new CriteriaQueryHelper(session(), TemplateResourceGroup.class);
        criteriaHelper.whereEquals("groupId", group);
        return criteriaHelper.getQuery().list();
    }

    public List<BookedResource> findBookedResourcesByBookedVisit(final BookedVisit bookedVisit) {
        final String findBookedVisit = "SELECT a FROM BookedResource a"
                + " WHERE "
                + " a.bookedVisit = :bookedVisit order by a.scheduledStartTime";

        final Query query = newQuery(findBookedVisit);
        query.setParameter("bookedVisit", bookedVisit);
        return query.list();
    }

    public Date findLatestBookedResourcesByBookedVisit(final BookedVisit bookedVisit) {
        final String findBookedVisit = "SELECT a.scheduledEndTime FROM BookedResource a"
                + " WHERE "
                + " a.bookedVisit = :bookedVisit order by a.scheduledEndTime DESC";

        final Query query = newQuery(findBookedVisit);
        query.setParameter("bookedVisit", bookedVisit);
        query.setFirstResult(0);
        query.setMaxResults(1);
        return (Date) query.uniqueResult();
    }

    public boolean findBookedResourcesByBookedVisit(final TemplateResource templateResource) {
        final String findBookedVisit = "SELECT a FROM BookedResource a"
                + " WHERE "
                + " a.templateResource = :templateResource ";

        final Query query = newQuery(findBookedVisit);
        query.setParameter("templateResource", templateResource);
        return query.list().size() > 0;
    }

    public BookedResource findBookedResourceById(final Integer id) {
        return this.findById(BookedResource.class, id);
    }

    public AppointmentOverrideReason findAppointmentOverrideReasonById(final Integer id) {
        return this.findById(AppointmentOverrideReason.class, id);
    }

    public AppointmentStatusReason findAppointmentStatusReasonById(final Integer id) {
        return this.findById(AppointmentStatusReason.class, id);
    }

    public List<VisitCommentsResponse.VisitComment> findAppointmentCommentsByVisit(final BookedVisit visit) {
        final CriteriaQueryHelper criteriaHelper = new CriteriaQueryHelper(session(), Comments.class);
        criteriaHelper.whereEquals("bookedVisit", visit);
        return VisitCommentsResponse.fromTemplateApprovalHistoryList(criteriaHelper.getQuery().list());
    }

    public long findTotalAppointmentCommentsByVisit(final int bookedVisitId) {

        final String findTotalVisitComments = "SELECT COUNT(c) FROM Comments c"
                                                + " WHERE c.bookedVisit.id = :bookedVisit";

        final Query query = newQuery(findTotalVisitComments);
        query.setParameter("bookedVisit", bookedVisitId);
        return (Long)query.uniqueResult();
    }

    public List<TemplateResource> getSingleFloatTemplateResources(final VisitTemplate selectedVisit) {
        final String findFloatTemplateResource = "SELECT tr FROM TemplateResource tr"
                + " WHERE tr.visitTemplate = :visit AND tr.floatable = true AND tr.groupId IS NULL ";

        final Query query = newQuery(findFloatTemplateResource);
        query.setParameter("visit", selectedVisit);

        return query.list();
    }

    public List<TemplateResourceGroup> getTemplateResourceGroups(final VisitTemplate selectedVisit, final boolean flexVsFloat) {
        final String findTemplateResourceGroups =
                "SELECT trg FROM TemplateResourceGroup trg"
                + " WHERE trg.visit = :visit AND trg.flexGroup = :flexGroup ";

        final Query query = newQuery(findTemplateResourceGroups);
        query.setParameter("visit", selectedVisit);
        query.setParameter("flexGroup", flexVsFloat);

        return query.list();
    }

    public BookedResource findOrderedBookedResource(final BookedVisit bookedVisit, final String columnSort, final String orderBy){
        final String findBookedVisit = "SELECT a FROM BookedResource a"
                + " WHERE a.bookedVisit = :bookedVisit order by " + columnSort +" "+ orderBy;

        final Query query = newQuery(findBookedVisit);
        query.setParameter("bookedVisit", bookedVisit);
        query.setFirstResult(0);
        query.setMaxResults(1);
        final List<BookedResource> brs = query.list();
        return brs.get(0);
    }

    public List<BookedResource> findRoomBookedResourcesByBookedVisit(final int bookedVisit){
        final String findBookedVisit = "SELECT a FROM BookedResource a, Resource r, BookedVisit bv "
                + " WHERE a.bookedVisit = bv.id and bv.id = :bookedVisit and a.resource = r.id and r.resourceType = 'Room'";

        final Query query = newQuery(findBookedVisit);
        query.setParameter("bookedVisit", bookedVisit);
        return query.list();
    }

    public List<BookedResourcesResponse> getBookedResourcesListByBookedVisit(final int bookedVisit, final String sortBy, final String orderBy, final int page, int maxResults) {
        maxResults = negativeIsInfinity(maxResults);
        String sortOn = sortBy;
        if (sortBy.equalsIgnoreCase("br.scheduled_start_time")) {
            sortOn = " br.scheduled_start_time " +orderBy+ ", br.scheduled_end_time ";
        } else if (sortBy.equalsIgnoreCase("br.scheduled_end_time")) {
            sortOn = " br.scheduled_end_time " +orderBy+ ", br.scheduled_start_time ";
        }

        final String findBookedVisit = "SELECT br.id, r.name, group_concat(lla.name separator ', '), br.scheduled_start_time, br.scheduled_end_time "
                + " FROM booked_visit bv, resource r, booked_resource br "
                + " LEFT OUTER JOIN override_booked_resource_annotations obra ON obra.booked_resource = br.id "
                + " LEFT OUTER JOIN line_level_annotations lla ON obra.line_level_annotations = lla.id "
                + " WHERE br.booked_visit = bv.id and bv.id = :bookedVisit and br.resource = r.id "
                + " group by br.id order by " + sortOn + " " + orderBy;

        final Query query = newNativeQuery(findBookedVisit);
        query.setParameter("bookedVisit", bookedVisit);
        final int offSet = (page - 1) * maxResults;
        query.setFirstResult(offSet);
        query.setMaxResults(maxResults);
        @SuppressWarnings("unchecked")
        final List<Object[]> resultRows = query.list();
        final List<BookedResourcesResponse> bookedResourcesResponseDTOs = Lists.newArrayList();
        final Long total = findBookedResourcesCount(bookedVisit);
        for (final Object[] row : resultRows) {
            final Integer bookedResource = (Integer) row[0];
            final String resourceName = (String) row[1];
            final String annotations = (String) row[2];
            final Date scheduledStartTime = (Date) row[3];
            final Date scheduledEndTime = (Date) row[4];

            final BookedResourcesResponse bookedResourcesResponseDTO = new BookedResourcesResponse(bookedResource, resourceName, annotations, scheduledStartTime, scheduledEndTime, "", "", "", total, false);
            bookedResourcesResponseDTOs.add(bookedResourcesResponseDTO);
        }
        return bookedResourcesResponseDTOs;
    }

    Long findBookedResourcesCount(final int bookedVisit){
        final String findCount =
                "SELECT count(br.id) FROM BookedResource br "
                        + " WHERE br.bookedVisit.id = :bookedVisit";

        final Query query = newQuery(findCount).setParameter("bookedVisit", bookedVisit);

        return (Long) query.uniqueResult();
    }

    public String findRoomString(final int visit) {

        final Session session = session();

        final String hql = "select br.resource.name from BookedResource br where br.bookedVisit.id = :visit and br.resource.resourceType = 'Room' order by br.resource.name ASC";

        final Query query = session.createQuery(hql);

        query.setParameter("visit", visit);

        @SuppressWarnings("unchecked")
        final List<String> list = query.list();

        return Joiner.on(", ").join(list);
    }

    public boolean canUserSeeBookedVisit(final User user, final Integer bookedVisitId) {
        boolean result = true;

        if (user.isStudyStaff()) {

            final Query query = newQuery(
                    "select bv from Study s, StudyUser su, BookedVisit bv " +
                            " where s.id = su.study " +
                            "   and :user = su.user " +
                            "   and bv.study = s.id " +
                            "   and bv.id = :bvId " +
                            "   and su.active=true " +
                    "   and s.studyStatus.isClosed = FALSE");

            query.setParameter("user", user);
            query.setParameter("bvId", bookedVisitId);

            final BookedVisit bookedVisit = (BookedVisit)query.uniqueResult();

            if (bookedVisit == null) {
                result = false;
            }
        }

        return result;
    }

    static public String hqlForBookedVisitsNotCancelledOver48HoursAgo(
            String bookedVisitFieldName,
            String dayBeforeYesterdayParamName) {

        return hqlForAllApptStatuses(bookedVisitFieldName, dayBeforeYesterdayParamName, true);

    }

    static public String hqlForBookedVisitsNotCancelledOver48HoursAgoAndNotOnHold(
            String bookedVisitFieldName,
            String dayBeforeYesterdayParamName) {

        return hqlForAllApptStatuses(bookedVisitFieldName, dayBeforeYesterdayParamName, false);

    }

    static private String hqlForAllApptStatuses(
            String bookedVisitFieldName,
            String dayBeforeYesterdayParamName,
            boolean includeHold) {

        String hql = " ( ";
        hql += bookedVisitFieldName + ".appointmentStatus.isCancelled = FALSE ";
        if (!includeHold) {
            hql += " AND " + bookedVisitFieldName + ".appointmentStatus.isHold = FALSE ";
        }
        hql += " ) ";
        hql += " OR (" + bookedVisitFieldName + ".appointmentStatus.isCancelled = TRUE AND " + bookedVisitFieldName + ".cancelDate  > :" + dayBeforeYesterdayParamName + ")";

        return hql;

    }

    public List<CalendarVisitsResponse> getCalendarBookedVisits(
            final String sublocationName, final Date startDate,
            final Date endDate, final boolean homeView,
            final List<Study> studies, final boolean isStudyStaff
    ) {
        // if no studies for study staff return empty list
        if (isStudyStaff && (studies == null || studies.isEmpty())) {
            return Lists.newArrayList();
        }

        String findBookedVisit = "SELECT bv FROM BookedVisit bv " +
                " WHERE (" +
                hqlForBookedVisitsNotCancelledOver48HoursAgo("bv", "dayBeforeYesterday") +
                " ) AND ( " +
                "   (:startTime BETWEEN bv.scheduledStartTime AND bv.scheduledEndTime) OR " +
                "   (:endTime BETWEEN bv.scheduledStartTime AND bv.scheduledEndTime) OR " +
                "   (bv.scheduledStartTime >= :startTime AND bv.scheduledEndTime <= :endTime)" +
                " ) ";

        final boolean sublocationNameIsPresent = isNonNullNonEmpty(sublocationName);

        if (sublocationNameIsPresent) {
            findBookedVisit += " and bv.visitTemplate.sublocation.name = :sublocation ";
        }

        if (isStudyStaff) {
            findBookedVisit += " and bv.study IN (:studies) and bv.study.studyStatus.isOpen = TRUE ";
        }

        findBookedVisit += " ORDER BY bv.id ";

        final Session session = session();

        final Query query = session.createQuery(findBookedVisit);
        query.setParameter("startTime", startDate);
        query.setParameter("endTime", endDate);
        if (sublocationNameIsPresent) {
            query.setParameter("sublocation", sublocationName);
        }
        query.setParameter("dayBeforeYesterday", getDayBeforeYesterday());
        if (isStudyStaff) {
            query.setParameterList("studies", studies);
        }

        return bookedVisitsDTO(homeView, query.list());
    }

    public List<CalendarVisitsResponse> findBookedVisitsByResource(final String resource, final User user, final List<Study> studies, final Date startDate, final Date endDate, final boolean homeView, String sublocationName) {
        final String columnHqlList = CalendarBookedVisitsColumn.columnHqlList;
        String findBookedVisit = getCalendarBookedVisitsQueryHql(columnHqlList);

        if (studies != null)
        {
            if((!studies.isEmpty() && !user.isStudyStaff()) || user.isStudyStaff())
            {
                findBookedVisit += " and bv.study in (:studies)";
            }
        }

        if (isNonNullNonEmpty(sublocationName) ) {
            findBookedVisit += " and bv.visitTemplate.sublocation.name = :sublocation ";
        }

        findBookedVisit += " and br.resource.name = :resource GROUP BY bv.id ";

        final Session session = session();

        final Query query = session.createQuery(findBookedVisit);

        if (studies != null)
        {
            if((!studies.isEmpty() && !user.isStudyStaff()) || user.isStudyStaff())
            {
                query.setParameterList("studies", studies);
            }
        }

        query.setParameter("resource", resource);
        query.setParameter("dayBeforeDate", getDayBeforeYesterday());
        query.setParameter("startTime", startDate);
        query.setParameter("endTime", endDate);

        if (isNonNullNonEmpty(sublocationName)) {
            query.setParameter("sublocation", sublocationName);
        }

        return getCalendarVisitsDTOs(homeView, query.list());
    }

    public List<CalendarVisitsResponse> findBookedVisitsByApppointmentStatus(
            final CalendarFilter calendarFilter, final Date startDate,
            final Date endDate, final boolean homeView,
            final List<Study> studies, final boolean isStudyStaff, final String sublocationName
    ) {
        String appointmentStatusFlag = calendarFilter.getFlagName();
        if (appointmentStatusFlag == null) {
            logAndThrow("Appointment status flag cannot be inferred from CalendarFilter value " + calendarFilter);
        }

        String findBookedVisit = "SELECT bv FROM BookedVisit bv " +
                "WHERE bv.appointmentStatus." + appointmentStatusFlag + " = TRUE AND "
                + " ((:startTime BETWEEN bv.scheduledStartTime AND bv.scheduledEndTime) "
                + " OR (:endTime BETWEEN bv.scheduledStartTime AND bv.scheduledEndTime) "
                + " OR (bv.scheduledStartTime >= :startTime AND bv.scheduledEndTime <= :endTime)) ";

        if (isNonNullNonEmpty(studies)) {
            findBookedVisit += " AND bv.study IN (:studies)";
        }

        if(calendarFilter.equals(CalendarFilter.BY_APPOINTMENT_STATUS_CANCELLED)) {
            findBookedVisit += " AND (bv.cancelDate >= :dayBeforeYesterday)";
        }

        if (isNonNullNonEmpty(sublocationName) ) {
            findBookedVisit += " AND bv.visitTemplate.sublocation.name = :sublocation ";
        }

        findBookedVisit += " GROUP BY bv.id ";

        final Session session = session();

        final Query query = session.createQuery(findBookedVisit);
        query.setParameter("startTime", startDate);
        query.setParameter("endTime", endDate);
        if (isStudyStaff) {
            query.setParameterList("studies", studies);
        }
        if(calendarFilter.equals(CalendarFilter.BY_APPOINTMENT_STATUS_CANCELLED)) {
            Date twoDaysAgo = getDayBeforeYesterday();
            query.setParameter("dayBeforeYesterday", twoDaysAgo);
        }

        if (isNonNullNonEmpty(sublocationName)) {
            query.setParameter("sublocation", sublocationName);
        }

        return bookedVisitsDTO(homeView, query.list());
    }

    List<CalendarVisitsResponse> bookedVisitsDTO(final boolean homeView, List<BookedVisit> resultRows) {

        List<CalendarVisitsResponse> results = Lists.newArrayList();

        for (BookedVisit bv : resultRows) {
            final Integer eventId = bv.getId();
            final String appointmentStatus = bv.getAppointmentStatus().getName();
            final String localId = bv.getStudy().getLocalId();
            final String piLastName = bv.getStudy().getInvestigator() != null ? bv.getStudy().getInvestigator().getLastName() : "";
            final String visitName = bv.getVisitTemplate().getName();
            final SubjectMrn decryptedSubjectMrn = bv.getSubjectMrnDecrypted();
            final String subjectFirstName = bv.getSubjectMrn() == null ? "" : decryptedSubjectMrn.getSubject().getFirstName();
            final String subjectLastName = bv.getSubjectMrn() == null ? NO_SUBJECT_ASSIGNED : decryptedSubjectMrn.getSubject().getLastName();
            final String subjectMrn = bv.getSubjectMrn() == null ? NA : decryptedSubjectMrn.getMrn();
            final String scheduledStartTime = DateUtility.format(DateUtility.dateHourMin(), bv.getScheduledStartTime());
            final String scheduledEndTime = DateUtility.format(DateUtility.dateHourMin(), bv.getScheduledEndTime());
            final String room = this.findRoomString(bv.getId());
            final Date schedulingTime = bv.getSchedulingTime();
            final int commentCount = this.findAppointmentCommentsByVisit(bv).size();

            final boolean allDay = isAllDay(homeView, bv.getVisitType());
            CalendarVisitsResponse calendarVisitsResponse = new CalendarVisitsResponse(
                    eventId, visitName, piLastName, room, localId,
                    subjectLastName, appointmentStatus, scheduledStartTime,
                    scheduledEndTime, allDay, bv.getVisitType().isInpatient());
            calendarVisitsResponse.setSubjectFirstName(subjectFirstName);
            calendarVisitsResponse.setSubjectMrn(subjectMrn);
            calendarVisitsResponse.setCommentCount(commentCount);
            if (schedulingTime != null) {
                calendarVisitsResponse.setScheduleData(schedulingTime.getTime());
            }
            results.add(calendarVisitsResponse);
        }

        return results;
    }

    public List<CalendarVisitsResponse> findAllBookedVisitsByStudy(final List<Study> study, final Date startDate, final Date endDate, final boolean homeView, String sublocationName) {
        String findBookedVisit = "select bv FROM BookedVisit bv " +
                "where bv.study IN (:study) and "
                + " ( "
                + hqlForBookedVisitsNotCancelledOver48HoursAgo("bv", "dayBeforeYesterday")
                + " ) AND "
                + " ((:startTime between bv.scheduledStartTime and bv.scheduledEndTime) "
                + " or (:endTime between bv.scheduledStartTime and bv.scheduledEndTime) "
                + " or (bv.scheduledStartTime >= :startTime and bv.scheduledEndTime <= :endTime))";

        if (isNonNullNonEmpty(sublocationName) ) {
            findBookedVisit += " and bv.visitTemplate.sublocation.name = :sublocation ";
        }

        findBookedVisit += " GROUP BY bv.id";

        final Session session = session();

        final Query query = session.createQuery(findBookedVisit);

        query.setParameterList("study", study);
        query.setParameter("dayBeforeYesterday", getDayBeforeYesterday());
        query.setParameter("startTime", startDate);
        query.setParameter("endTime", endDate);

        if (isNonNullNonEmpty(sublocationName)) {
            query.setParameter("sublocation", sublocationName);
        }

        return bookedVisitsDTO(homeView, query.list());
    }

    public List<CalendarVisitsResponse> findAllBookedVisitsBySubject(
            final User user, final List<Study> studyList,
            final List<Subject> subject, String sublocationName, final Date startDate,
            final Date endDate, final boolean homeView
    ) {

        String findBookedVisit = "SELECT bv FROM BookedVisit bv " +
                "WHERE " +
                "bv.subjectMrn IS NOT NULL " +
                "AND bv.subjectMrn.subject IN (:subjectList) AND "
                + " ( "
                + hqlForBookedVisitsNotCancelledOver48HoursAgoAndNotOnHold("bv", "dayBeforeYesterday")
                + " ) AND "
                + " ((:startTime BETWEEN bv.scheduledStartTime AND bv.scheduledEndTime) "
                + " OR (:endTime BETWEEN bv.scheduledStartTime AND bv.scheduledEndTime) "
                + " OR (bv.scheduledStartTime >= :startTime AND bv.scheduledEndTime <= :endTime)) ";

                if (user.isStudyStaff()) {
                    findBookedVisit += " AND bv.study IN (:studyList) ";
                }

        if (isNonNullNonEmpty(sublocationName) ) {
            findBookedVisit += " AND bv.visitTemplate.sublocation.name = :sublocation ";
        }

        findBookedVisit += " GROUP BY bv.id";

        final Session session = session();

        final Query query = session.createQuery(findBookedVisit);
        query.setParameterList("subjectList", subject);
        query.setParameter("dayBeforeYesterday", getDayBeforeYesterday());
        query.setParameter("startTime", startDate);
        query.setParameter("endTime", endDate);

        if (isNonNullNonEmpty(sublocationName)) {
            query.setParameter("sublocation", sublocationName);
        }

        if (user.isStudyStaff()) {
            query.setParameterList("studyList", studyList);
        }

        return bookedVisitsDTO(homeView, query.list());
    }

    List<CalendarVisitsResponse> getCalendarVisitsDTOs(
            final boolean homeView, final List<Object[]> resultRows
    ) {
        @SuppressWarnings("unchecked")

        final Function<Object[], CalendarVisitsResponse> toCalendarVisitsResponse = resultRow -> {
            final String resultSubjectLastName = resultRow[5] == null
                    ? NO_SUBJECT_ASSIGNED
                    : decrypt((String) resultRow[5]);

            final Integer eventId = (Integer) resultRow[0];
            final String appointmentStatus = (String) resultRow[1];
            final String localId = (String) resultRow[2];
            final String userLastName = (String) resultRow[3];
            final String piLastName = userLastName != null ? userLastName : "";
            final String visitName = (String)resultRow[4];
            final String subjectLastName = resultSubjectLastName;
            final String scheduledStartTime = DateUtility.format(DateUtility.dateHourMin(), (Date) resultRow[6]);
            final String scheduledEndTime = DateUtility.format(DateUtility.dateHourMin(), (Date) resultRow[7]);
            final Boolean isInpatient = (Boolean)resultRow[8];
            final String rooms = null;

            final boolean allDay = isAllDay(homeView, isInpatient);

            return new CalendarVisitsResponse(
                    eventId, visitName, piLastName, rooms, localId,
                    subjectLastName, appointmentStatus, scheduledStartTime,
                    scheduledEndTime, allDay, isInpatient);
        };

        return enrich(resultRows).map(toCalendarVisitsResponse).toList();
    }

    private static final class CalendarBookedVisitsColumn {
        static final List<String> columnsHql = Arrays.asList(
                "bv.id",
                "apSt.name",
                "bv.study.localId",
                "u.lastName",
                "bv.name",
                "subj.lastName",
                "bv.scheduledStartTime",
                "bv.scheduledEndTime",
                "bv.visitType.inpatient"
        );

        static final String columnHqlList = Joiner.on(", ").join(columnsHql);
    }

    private String getCalendarBookedVisitsQueryHql(final String columnHqlList) {
        return "SELECT " + columnHqlList + " " +
                "FROM " +
                "BookedVisit bv JOIN bv.study s " +
                "LEFT JOIN s.investigator u " +
                "LEFT JOIN bv.subjectMrn sm " +
                "LEFT JOIN sm.subject subj, " +
                "BookedResource br, " +
                "AppointmentStatus apSt, " +
                "VisitTemplate vt, " +
                "Sublocation sub " +
                "WHERE " +
                "br.bookedVisit = bv.id AND " +
                "bv.visitTemplate = vt.id AND " +
                "bv.appointmentStatus = apSt.id AND " +
                "vt.sublocation = sub.id AND " +
                " ( " +
                hqlForBookedVisitsNotCancelledOver48HoursAgo("bv", "dayBeforeDate") +
                " ) " +
                " AND ((:startTime between bv.scheduledStartTime AND bv.scheduledEndTime) " +
                " OR (:endTime between bv.scheduledStartTime AND bv.scheduledEndTime) " +
                " OR (bv.scheduledStartTime >= :startTime AND bv.scheduledEndTime <= :endTime))";
    }

    public List<BookedVisitsResponse> getOnlyTodaysBookedVisits(final Date startDate,
                                                                final Date endDate,
                                                                final String sortBy,
                                                                final String orderBy,
                                                                final int page,
                                                                int maxResults) {
        maxResults = negativeIsInfinity(maxResults);
        final String findBookedVisit = "select bv " +
                "FROM BookedVisit bv "
                + "where "
                + " ( "
                + hqlForBookedVisitsNotCancelledOver48HoursAgo("bv", "dayBeforeYesterday")
                + " ) AND "
                + " ((:startTime between bv.scheduledStartTime and bv.scheduledEndTime) "
                + " or (:endTime between bv.scheduledStartTime and bv.scheduledEndTime) "
                + " or (bv.scheduledStartTime >= :startTime and bv.scheduledEndTime <= :endTime)) "
                + " order by " + sortBy + " " + orderBy;

        final Query query = newQuery(findBookedVisit);
        query.setParameter("startTime", startDate);
        query.setParameter("endTime", endDate);
        query.setParameter("dayBeforeYesterday", getDayBeforeYesterday());

        final Long total = Long.valueOf(query.list().size());
        final int offset = (page - 1) * maxResults;
        query.setFirstResult(offset);
        query.setMaxResults(maxResults);

        return bookedVisitsResponses(page - 1, query, total);
    }

    public List<BookedVisitsResponse> getOnlyTodaysBookedVisitsByStudy(
            final List<Study> study,
            final Date startDate,
            final Date endDate,
            final String sortBy,
            final String orderBy,
            final int page,
            int maxResults) {

        maxResults = negativeIsInfinity(maxResults);
        final String findBookedVisit = "select bv " +
                " FROM BookedVisit bv "
                + " ( "
                + hqlForBookedVisitsNotCancelledOver48HoursAgo("bv", "dayBeforeDate")
                + " ) "
                + " and ((:startTime between bv.scheduledStartTime and bv.scheduledEndTime) "
                + " or (:endTime between bv.scheduledStartTime and bv.scheduledEndTime) "
                + " or (bv.scheduledStartTime >= :startTime and bv.scheduledEndTime <= :endTime))"
                + " order by " + sortBy + " " + orderBy;

        final Query query = newQuery(findBookedVisit);
        query.setParameterList("studies", study);
        query.setParameter("startTime", startDate);
        query.setParameter("endTime", endDate);
        query.setParameter("dayBeforeDate", getDayBeforeYesterday());

        final Long total = Long.valueOf(query.list().size());
        final int offset = (page - 1) * maxResults;
        query.setFirstResult(offset);
        query.setMaxResults(maxResults);

        return bookedVisitsResponses(page - 1, query, total);
    }

    String listToSpaceWhereString(final List<String> whereClauses) {
        String result = "";

        if (! whereClauses.isEmpty()) {
            result = " WHERE (" +
                    Joiner.on(" and ").join(whereClauses) +
                    ")";
        }
        return result;
    }

    boolean useTimeRangeForBookedVisits(Date fromDate) {
        return (fromDate != null);
    }
    boolean useSyncTimeForBookedVisits(Date fromDate, Date toDate) {
        return (fromDate == null && toDate != null);
    }

    boolean filterByStudyForBookedVisits(List<Study> studyList) {

        return studyList != null;
    }

    String getTimeRangeString(String start, String end) {
        return "(" + end + " >= :startTime and :endTime >= " + start + ")";
    }

    public List<BookedVisitsResponse> findBookedVisitsForStudyList(
            final List<Study> studyList,
            final String sortBy,
            final String orderBy,
            final int oneIndexedPageNumber,
            int maxResults,
            final Date fromDate,
            final Date toDate,
            final SearchDTO searchDTO
    ) {

        maxResults = negativeIsInfinity(maxResults);

        String timeBeforeTodate = "bv.scheduledStartTime < (:toDate)";

        String selectFields = "bv.id, " +
                              " bv.scheduledStartTime, " +
                              " bv.scheduledEndTime, " +
                              " vt.name, " +
                              " st.localId, " +
                              " st.irb, " +
                              " st.catalystId, " +
                              " s.firstName, " +
                              " s.lastName, " +
                              " sm.mrn, " +
                              " apSt.name";
        String fromTables = "BookedVisit bv " +
                            " join bv.study st " +
                            " join bv.visitTemplate vt " +
                            " join bv.appointmentStatus apSt " +
                            " left join bv.subjectMrn sm " +
                            " left join sm.subject s with ( s.archivalStatus IS NULL " +
                            ")";

        boolean filteringByStudy = filterByStudyForBookedVisits(studyList);
        boolean usingRange = useTimeRangeForBookedVisits(fromDate);
        boolean usingSync = useSyncTimeForBookedVisits(fromDate, toDate);

        StringBuilder mainQueryBuilder = new StringBuilder("select ").append(selectFields);
        StringBuilder mainCountQueryBuilder = new StringBuilder("select count ( * )");
        StringBuilder syncOffsetQueryBuilder = new StringBuilder("select count ( * )");
        List<StringBuilder> queryBuilders = Lists.newArrayList(mainQueryBuilder, mainCountQueryBuilder, syncOffsetQueryBuilder);

        queryBuilders.stream().forEach(b -> b.append(" from ").append(fromTables));

        List<String> mainWhereClauses = Lists.newArrayList();
        List<String> mainCountWhereClauses = Lists.newArrayList();
        List<String> syncOffsetWhereClauses = Lists.newArrayList();
        List<List<String>> whereClauseLists = Lists.newArrayList(mainWhereClauses, mainCountWhereClauses, syncOffsetWhereClauses);

        Map<StringBuilder, List<String>> queryBuilderToWhereClauseList = Maps.newHashMap();
        queryBuilderToWhereClauseList.put(mainQueryBuilder, mainWhereClauses);
        queryBuilderToWhereClauseList.put(mainCountQueryBuilder, mainCountWhereClauses);
        queryBuilderToWhereClauseList.put(syncOffsetQueryBuilder, syncOffsetWhereClauses);

        if (usingRange) {
            String timeRange = getTimeRangeString("bv.scheduledStartTime", "bv.scheduledEndTime");

            mainWhereClauses.add(timeRange);
            mainCountWhereClauses.add(timeRange);
        }
        else if(usingSync) {
            syncOffsetWhereClauses.add(timeBeforeTodate);
        }

        if (filteringByStudy) {
            whereClauseLists.stream().forEach(c -> c.add("bv.study in (:studyList)"));
        }

        List<String> whereClauseAtomsForFilters = searchDTO.generateSearchClauseList();

        whereClauseLists.stream().forEach(c -> c.addAll(whereClauseAtomsForFilters));

        queryBuilders.stream().forEach(b -> b.append(listToSpaceWhereString(queryBuilderToWhereClauseList.get(b))));

        String sortAndOrder = "ORDER BY "+  sortBy + " " + orderBy;

        mainQueryBuilder.append(" ").append(sortAndOrder);
        syncOffsetQueryBuilder.append(" ").append(sortAndOrder);

        final Query mainQuery = newQuery(mainQueryBuilder.toString());
        final Query mainCountQuery = newQuery(mainCountQueryBuilder.toString());
        final Query syncOffsetQuery = newQuery(syncOffsetQueryBuilder.toString());

        if (filteringByStudy) {
            mainQuery.setParameterList("studyList", studyList);
            mainCountQuery.setParameterList("studyList", studyList);
            syncOffsetQuery.setParameterList("studyList", studyList);
        }

        int zeroIndexedPageNumber = oneIndexedPageNumber - 1;

        searchDTO.assignQueryParameterValues(mainQuery);
        searchDTO.assignQueryParameterValues(mainCountQuery);
        searchDTO.assignQueryParameterValues(syncOffsetQuery);

        if (usingRange) {
            mainCountQuery.setParameter("startTime", fromDate);
            mainQuery.setParameter("startTime", fromDate);

            Date endDateAdjustedToEndOfDay = DateUtility.adjustDateToEndOfDay(toDate);
            mainCountQuery.setParameter("endTime", endDateAdjustedToEndOfDay);
            mainQuery.setParameter("endTime", endDateAdjustedToEndOfDay);
        }
        else if(usingSync) {
            syncOffsetQuery.setParameter("toDate", toDate);
            zeroIndexedPageNumber = getSyncedZeroIndexedPageNumber(maxResults, syncOffsetQuery);
        }

        final int resultOffset = zeroIndexedPageNumber * maxResults;
        mainQuery.setFirstResult(resultOffset);

        mainQuery.setMaxResults(maxResults);
        List<Object[]> mainResults = mainQuery.list();

        final long total = (Long) mainCountQuery.uniqueResult();

        return bookedVisitsResponses(zeroIndexedPageNumber, mainResults, total);
    }

    int getSyncedZeroIndexedPageNumber(final int maxResults,
                                       final Query syncOffsetQuery) {

        int result = 0;
        long totalRows = (Long) syncOffsetQuery.uniqueResult();

        if (totalRows > 0) {
            result = (int)(totalRows - 1) / maxResults;
        }

        return result;
    }

    private List<BookedVisitsResponse> bookedVisitsResponses(int page, Query query, Long total) {
        final List<BookedVisit> resultRows = query.list();

        List<BookedVisitsResponse> results = Lists.newArrayList();

        for (BookedVisit bv : resultRows) {
            final String visitName = bv.getVisitTemplate().getName();
            final String irb = bv.getStudy().getIrb();
            final String catId = bv.getStudy().getCatalystId();
            final String localId = bv.getStudy().getLocalId();

            SubjectMrn subjectMrn = bv.getSubjectMrnDecrypted();

            final String subjectFirstName = (subjectMrn == null) ? NO_SUBJECT_ASSIGNED :
                    subjectMrn.getSubject().getFirstName();
            final String subjectLastName = (subjectMrn == null) ? NO_SUBJECT_ASSIGNED :
                    subjectMrn.getSubject().getLastName();

            final String subjectMrnCode = (subjectMrn == null) ? NA : subjectMrn.getMrn();
            final String apptStatusName = bv.getAppointmentStatus().getName();
            final Date scheduledStartTime = bv.getScheduledStartTime();
            final Date scheduledEndTime = bv.getScheduledEndTime();

            BookedVisitsResponse result = new BookedVisitsResponse(bv.getId(), visitName,
                    irb, catId, localId, subjectFirstName, subjectLastName, subjectMrnCode, apptStatusName,
                    scheduledStartTime, scheduledEndTime, total, page);

            results.add(result);
        }

        return results;
    }

    String adjustForDecryptionAndNA(String in, String adjusted) {
        String result = (null == in) ? adjusted : SubjectDataEncryptor.decrypt(in);
        return result;
    }

    List<BookedVisitsResponse> bookedVisitsResponses(int page, List<Object[]> resultSetList, Long total) {
        List<BookedVisitsResponse> results = Lists.newArrayList();

        for (Object[] row : resultSetList) {

            int index = 0;
            Integer id = (Integer)row[index++];
            Date scheduledStartTime = (Date)row[index++];
            Date scheduledEndTime = (Date)row[index++];
            String visitName = (String)row[index++];
            String localId = (String)row[index++];
            String irb = (String)row[index++];
            String catId = (String)row[index++];

            String subjectFirstName = adjustForDecryptionAndNA((String)row[index++], NO_SUBJECT_ASSIGNED);
            String subjectLastName =  adjustForDecryptionAndNA((String)row[index++], NO_SUBJECT_ASSIGNED);
            String subjectMrnCode =   adjustForDecryptionAndNA((String)row[index++], NA);

            String apptStatusName = (String)row[index++];

            BookedVisitsResponse result = new BookedVisitsResponse(id, visitName,
                    irb, catId, localId, subjectFirstName, subjectLastName, subjectMrnCode,
                    apptStatusName,
                    scheduledStartTime, scheduledEndTime, total, page);

            results.add(result);
        }

        return results;
    }

    @SuppressWarnings("unchecked")
    public List<StudySubject> findStudySubjectBySubject(final Subject subject) {

        String findStudySubjectQueryString = "FROM StudySubject ss WHERE ss.subjectMrn.subject = :subject ";
        final Query query = newQuery(findStudySubjectQueryString);
        query.setParameter("subject", subject);

        return query.list();
    }

    @SuppressWarnings("unchecked")
    public List<StudySubject> findStudySubjectBySubjectAndStudy(final Subject subject, final Study study) {

        String findStudySubjectQueryString = "FROM StudySubject ss WHERE ss.study = :study"
                + " and ss.subjectMrn.subject = :subject ";
        final Query query = newQuery(findStudySubjectQueryString);
        query.setParameter("subject", subject);
        query.setParameter("study", study);

        return query.list();
    }

    public boolean subjectHasBookedVisitInDateRange(final Integer subject, final Date startDate, final Date endDate) {
        final String findBookedVisit = "SELECT bv.id FROM BookedVisit bv, Subject s, SubjectMrn sm "
                + " WHERE "
                +"  bv.subjectMrn = sm.id AND sm.subject = s.id AND s.id = :subject "
                +"  AND bv.appointmentStatus.isActive = TRUE "
                + " AND (   (:startTime >= bv.scheduledStartTime AND :startTime < bv.scheduledEndTime) "
                +      " or (:endTime > bv.scheduledStartTime AND :endTime <= bv.scheduledEndTime) "
                +      " or (bv.scheduledStartTime >= :startTime AND bv.scheduledEndTime <= :endTime) )";


        final Query query = newQuery(findBookedVisit);
        query.setParameter("subject", subject);
        query.setParameter("startTime", startDate);
        query.setParameter("endTime", endDate);
        return query.list().size() != 0;
    }

    public BookedVisit findBookedVisitById(final Integer id) {
        if (id == null) {
            return null;
        }
        return this.findById(BookedVisit.class, id);
    }

    public Date getDayBeforeYesterday() {
        final Calendar calendar = Calendar.getInstance();

        calendar.add(Calendar.DATE, -2);

        return new Date(calendar.getTimeInMillis());
    }

    @SuppressWarnings("unchecked")
    public List<BookedResource> findBookedResources(final Resource sharedResource) {

        final String findBookedResources = "SELECT br FROM BookedVisit bv, BookedResource br, AppointmentStatus ap"
                                           + " WHERE "
                                           + "  br.bookedVisit = bv.id AND bv.appointmentStatus = ap.id AND br" +
                                           ".resource = :resource "
                                           + "  AND bv.appointmentStatus.isOpen = TRUE ";

        final Query query = newQuery(findBookedResources).setParameter("resource", sharedResource);

        return query.list();
    }

    public SwitchSubjectResultDTO switchVisitSubject(Integer newSubjectMrnId, BookedVisit visit, boolean homeScreen, String className) {

        String fullName;

        if (newSubjectMrnId.equals(Integer.valueOf(0))) {
            visit.setSubjectMrn(null);
            fullName = NO_SUBJECT_ASSIGNED;
            visit.setAppointmentStatus(findHoldStatus());
            className = visit.getAppointmentStatus().getName();
        }
        else {
            SubjectMrn subjectMrn = findById(SubjectMrn.class, newSubjectMrnId);
            visit.setSubjectMrn(subjectMrn);
            visit.setAppointmentStatus(findScheduledStatus());
            fullName = decrypt(subjectMrn.getSubject().getFullName());
        }
        updateEntity(visit);

        VisitRenderSummaryDTO visitRenderSummaryDTO = new VisitRenderSummaryDTO(visit, className, homeScreen);

        return new SwitchSubjectResultDTO(true, newSubjectMrnId, fullName, visitRenderSummaryDTO);
    }


    public List<BookedVisit> getAllBookedVisitByStudyAndSubjectMrn(Study study, SubjectMrn subjectMrn) {

        String queryString = "select bv from BookedVisit bv where bv.study = :study and bv.subjectMrn = :subjectMrn";

        final Query query = newQuery(queryString);
        query.setParameter("study", study);
        query.setParameter("subjectMrn", subjectMrn);
        return query.list();

    }

}
