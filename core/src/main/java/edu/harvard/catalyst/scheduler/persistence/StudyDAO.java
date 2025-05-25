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
import edu.harvard.catalyst.scheduler.dto.SearchDTO;
import edu.harvard.catalyst.scheduler.dto.response.*;
import edu.harvard.catalyst.scheduler.entity.*;
import org.hibernate.query.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import static edu.harvard.catalyst.hccrc.core.util.ListUtils.enrich;
import static edu.harvard.catalyst.scheduler.core.Statics.NA;
import static edu.harvard.catalyst.scheduler.persistence.HqlClauses.whereBuilder;
import static edu.harvard.catalyst.scheduler.util.MiscUtil.isNonNullNonEmpty;
import static edu.harvard.catalyst.scheduler.util.MiscUtil.negativeIsInfinity;

@Repository
@Transactional
public class StudyDAO extends SiteDAO {

    private static final Integer CHECKOUT_ID = 3;
    private static final Integer CANCEL_ID = 4;

    public List<VisitType> getVisitTypes() {
        return this.findAll(VisitType.class);
    }

    public List<Role> getRoles() {
        return this.findAll(Role.class);
    }

    public List<InstitutionRole> getInstitutionRoles() {
        return this.findAll(InstitutionRole.class);
    }

    public List<StudyStatus> getStudyStatuses() {
        return this.findAll(StudyStatus.class);
    }

    public List<AppointmentOverrideReason> getOverrideReasons() {
        return this.findAll(AppointmentOverrideReason.class);
    }

    public List<AppointmentStatusReason> getCheckOutReasons() {
        final AppointmentStatus checkOutStatus = findAppointmentStatusById(CHECKOUT_ID);
        
        return findAppointmentStatusReasonByStatus(checkOutStatus);
    }

    @SuppressWarnings("unchecked")
    public List<AppointmentStatusReason> findAppointmentStatusReasonByStatus(final AppointmentStatus appointmentStatus) {
        final CriteriaQueryHelper criteriaHelper = new CriteriaQueryHelper(session(), AppointmentStatusReason.class);
        
        criteriaHelper.whereEquals("appointmentStatus", appointmentStatus);
        criteriaHelper.orderAsc("name");
        
        return criteriaHelper.getQuery().list();
    }

    public List<AppointmentStatusReason> getCancellationReasons() {
        final AppointmentStatus cancelStatus = findAppointmentStatusById(CANCEL_ID);

        return findAppointmentStatusReasonByStatus(cancelStatus);
    }

    public List<AppointmentStatus> getAppointmentStatuses() {
        return this.findAll(AppointmentStatus.class);
    }

    @SuppressWarnings("unchecked")
    public List<CancellationStatus> getVisitCancelStatuses() {
        final CriteriaQueryHelper criteriaHelper = new CriteriaQueryHelper(session(), CancellationStatus.class);

        return criteriaHelper.getQuery().list();
    }

    public List<IRBInstitution> getIRBInstitutions() {
        return this.findAll(IRBInstitution.class);
    }

    public List<Study> getStudies() {
        return this.findAll(Study.class);
    }

    public GetStudiesResponse getNonClosedStudies() {
        final String findStudy = "Select a from Study a WHERE a.studyStatus.isClosed = FALSE ";

        final Query query = newQuery(findStudy);

        @SuppressWarnings("unchecked")
        final List<Study> resultList = query.list();

        final long total = resultList.size();

        return GetStudiesResponse.createGetStudiesResponse(resultList, total);
    }

    public GetStudiesResponse findSubjectStudyListByPerson(final User studyPerson) {
        final Query query = newQuery("select distinct s from Study s, StudyUser su where s.id = su.study and su.user = :user and su.active=true and s.studyStatus.isClosed = FALSE ");

        query.setParameter("user", studyPerson);

        @SuppressWarnings("unchecked")
        final List<Study> resultList = query.list();

        final long total = resultList.size();

        return GetStudiesResponse.createGetStudiesResponse(resultList, total);
    }

    @SuppressWarnings("unchecked")
    public List<Study> findStudyListByPerson(final User studyPerson) {
        final String hql = "select distinct s from Study s, StudyUser su where s.id = su.study and su.user = :user and su.active=true and s.studyStatus.isClosed = FALSE ";

        final Query query = newQuery(hql).setParameter("user", studyPerson);

        return query.list();
    }

    public boolean isStudyByPersonAndStudy(final User studyPerson, final Study study) {
        final String hql = "select distinct s from Study s, StudyUser su where s.id = su.study and su.user = :user and su.study= :study and su.active=true";

        final Query query = newQuery(hql).setParameter("user", studyPerson).setParameter("study", study);

        @SuppressWarnings("unchecked")
        final List<Study> studyList = query.list();

        return !studyList.isEmpty();
    }

    @SuppressWarnings("unchecked")
    public List<Study> findStudyListByPersonAndLocalID(final User user, final String localId) {
        final String studyStaffSuffix = user.isStudyStaff()
                ? " and su.user = :user and s.studyStatus.isOpen = TRUE "
                : "";

        final String findStudy = "select distinct s from Study s, StudyUser su where s.id = su.study and su.active=true and lower(s.localId) LIKE :localId" + studyStaffSuffix;

        final Query query = newQuery(findStudy).setParameter("localId", "%" + localId.toLowerCase() + "%");

        if (user.isStudyStaff()) {
            query.setParameter("user", user);
        }

        return query.list();
    }

    @SuppressWarnings("unchecked")
    public List<Study> findStudyStaffFilterStudyListByPI(final User studyPerson, final String filterString) {

        final String hql = "select distinct s from Study s, StudyUser su where s.id = su.study and su.user = :user and su.active=true and lower(s.investigator.lastName) = :filterString";

        final Query query = newQuery(hql).setParameter("user", studyPerson).setParameter("filterString", filterString.toLowerCase());

        return query.list();
    }

    @SuppressWarnings("unchecked")
    public List<Study> getStudyStaffFilterStudiesByLocalId(final User studyPerson, final String filterString) {
        final String hql = "select distinct s from Study s, StudyUser su where s.id = su.study and su.user = :user and su.active=true and lower(s.localId) = :localId";

        final Query query = newQuery(hql).setParameter("user", studyPerson).setParameter("localId", filterString.toLowerCase());

        return query.list();
    }

    public boolean ifBookedVisitsHaveVisit(final VisitTemplate visit) {
        final String hql = "select bv from BookedVisit bv where bv.visitTemplate = :visit";

        final Query query = newQuery(hql).setParameter("visit", visit);

        @SuppressWarnings("unchecked")
        final List<BookedVisit> bv =  query.list();

        return !bv.isEmpty();
    }

    @SuppressWarnings("unchecked")
    public List<Study> findStudyByName(final String matchLocalId) {
        final CriteriaQueryHelper criteriaHelper = new CriteriaQueryHelper(session(), Study.class);
        criteriaHelper.whereLike("localId", "%" + matchLocalId + "%");
        return criteriaHelper.getQuery().list();
    }

    public Study findStudyDataById(final int id) {
        final String hql = "select s, min(bv.scheduledStartTime), " +
                "max(bv.scheduledStartTime) " +
                "from BookedVisit bv right outer join bv.study s where s.id =:id";

        final Query query = newQuery(hql).setParameter("id", id);

        final Object[] resultRows = (Object[]) query.uniqueResult();

        final Study study = (Study) resultRows[0];
        final Date firstVisitDate = (Date) resultRows[1];
        final Date lastVisitDate = (Date) resultRows[2];
        study.setFirstVisitDate(firstVisitDate);
        study.setLastScheduledVisitDate(lastVisitDate);

        return study;
    }

    public User findByStudyMemberId(final Integer id) {
        return this.findById(User.class, id);
    }

    public StudyUser findStudyUserByStudyAndUser(Study study, User user) {

        String hql = "Select su from StudyUser su where su.user = :user and su.study = :study";
        Query query = newQuery(hql)
                .setParameter("user", user)
                .setParameter("study", study);
        StudyUser studyUser = (StudyUser) query.uniqueResult();

        return studyUser;

    }

    public Gender findGenderById(final Integer id) {
        return this.findById(Gender.class, id);
    }

    public VisitType findVisitTypeById(final Integer id) {
        return this.findById(VisitType.class, id);
    }

    // TO BE INVOKED BY SUBJECT DATA WRAPPER
    public StudySubject findStudySubjectById(final Integer id) {
        return this.findById(StudySubject.class, id);
    }

    public Role findRoleById(final int id) {
        return this.findById(Role.class, id);
    }

    public IRBInstitution findIRBInstitutionById(final int id) {
        return this.findById(IRBInstitution.class, id);
    }

    public StudyUser findStudyUserRoleById(final int id) {
        return this.findById(StudyUser.class, id);
    }

    public FundingSource findFundingSourceById(final int id) {
        return findById(FundingSource.class, id);
    }

    public CentersAndInstitutions findCentersAndInstitutionsById(final int id) {
        return findById(CentersAndInstitutions.class, id);
    }

    public StudyStatus findStudyStatusById(final Integer id) {
        return this.findById(StudyStatus.class, id);
    }

    public InstitutionRole findInstitutionRoleByType(final InstitutionRoleType type) {
        final CriteriaQueryHelper criteriaHelper = new CriteriaQueryHelper(session(), InstitutionRole.class);
        criteriaHelper.whereEquals("type", type);

        return (InstitutionRole) criteriaHelper.getQuery().uniqueResult();
    }

    public Institution findByInstitutionId(final Integer id) {
        return this.findById(Institution.class, id);
    }

    public Study findStudyById(final int id) {
        return this.findById(Study.class, id);
    }

    public VisitTemplate findVisitById(final int id) {
        return this.findById(VisitTemplate.class, id);
    }

    public VisitTemplateDetailResponse getVisitDataById(final int id) {
        final VisitTemplate visit = findVisitById(id);
        return new VisitTemplateDetailResponse(visit);
    }

    public StudyDetailResponse getStudyDataById(final int id) {
        final Study study = findStudyDataById(id);
        final List<FundingSourceInfo> studyFundingSourceInfoList = getFundingSourceInfoList(id);
        return new StudyDetailResponse(study, studyFundingSourceInfoList);
    }

    private static final class StudyFundingSourceRow {
        private final Integer id;
        private final String name;
        private final String comment;
        private final Integer oneToFour;

        private final String grant;
        private final String center;
        private final Double direct;
        private final Double indirect;

        private final Date start;
        private final Date end;

        private final Optional<CentersAndInstitutions> centerAndInstitution;

        StudyFundingSourceRow(final Integer id, final String name, final String comment, final Integer oneToFour, final String grant, final String center, final Double direct, final Double indirect, final Date start, final Date end, final Optional<CentersAndInstitutions> centerAndInstitution) {
            super();
            this.id = id;
            this.name = name;
            this.comment = comment;
            this.oneToFour = oneToFour;
            this.grant = grant;
            this.center = center;
            this.direct = direct;
            this.indirect = indirect;
            this.start = start;
            this.end = end;
            this.centerAndInstitution = centerAndInstitution;
        }

        FundingSourceInfo toFundingSourceInfo() {
            final Optional<Integer> centerId = centerAndInstitution.map(CentersAndInstitutions::getId);
            final Optional<String> centerName = centerAndInstitution.map(CentersAndInstitutions::getName);

            return new FundingSourceInfo(
                    id, name, comment, oneToFour,
                    grant, center, direct, indirect,
                    start, end,
                    centerId.orElse(null),
                    centerName.orElse(null));
        }
    }

    private StudyFundingSourceRow arrayToStudyFundingSourceRow(final Object[] resultRow) {
        final String name = (String) resultRow[1];
        final Integer sfsId = (Integer) resultRow[10];

        final Optional<CentersAndInstitutions> centerAndInstitution =
                Optional.ofNullable(name).filter("Federal PHS"::equalsIgnoreCase).flatMap(n -> Optional.ofNullable(getCenterAndInstitution(sfsId)));

        return new StudyFundingSourceRow(
                (Integer) resultRow[0],
                name,
                (String) resultRow[2],
                (Integer) resultRow[3],
                (String) resultRow[4],
                (String) resultRow[5],
                (Double) resultRow[6],
                (Double) resultRow[7],
                (Date)resultRow[8],
                (Date)resultRow[9],
                centerAndInstitution);

    }

    List<FundingSourceInfo> getFundingSourceInfoList(final int studyId) {
        final String hql =    "select fs.id, " +
                "       fs.name, " +
                "       sfs.comment, " +
                "       sfs.oneToFour, " +
                "       sfs.grantId, " +
                "       sfs.siteCostCenter, " +
                "       sfs.totalDirectAward, " +
                "       sfs.totalIndirectAward, " +
                "       sfs.projectStartDate, " +
                "       sfs.projectEndDate, " +
                "       sfs.id " +
                "  from StudyFundingSource sfs, FundingSource fs" +
                "  where sfs.studyId = :id " +
                "   and fs.id = sfs.fundingSource.id ";

        final Query query = newQuery(hql).setParameter("id", studyId);

        @SuppressWarnings("unchecked")
        final List<Object[]> resultSet = query.list();

        return enrich(resultSet).map(this::arrayToStudyFundingSourceRow).map(StudyFundingSourceRow::toFundingSourceInfo).toList();
    }

    CentersAndInstitutions getCenterAndInstitution(final int sfsId) {
        final String hql =    "select sfs.centersAndInstitutions " +
                "  from StudyFundingSource sfs" +
                "  where sfs.id = :id ";

        final Query query = newQuery(hql).setParameter("id", sfsId);

        return (CentersAndInstitutions) query.uniqueResult();
    }

    public VisitTemplate findVisitTemplateById(final int id) {
        return this.findById(VisitTemplate.class, id);
    }

    public Sublocation findSubLocationById(final int id) {
        return this.findById(Sublocation.class, id);
    }

    public ResourceSublocation findSublocationByResource(final Resource r) {
        final CriteriaQueryHelper criteriaHelper = new CriteriaQueryHelper(session(), ResourceSublocation.class);
        criteriaHelper.whereEquals("resource", r);

        return (ResourceSublocation) criteriaHelper.getQuery().uniqueResult();
    }

    // THIS METHOD APPEARS NOT TO BE USED
    @SuppressWarnings("unchecked")
    public List<StudySubject> findStudySubjectBySubjectListAndStudyList(final List<Subject> subjectList, final List<Study> studyList) {
            final CriteriaQueryHelper criteriaHelper = new CriteriaQueryHelper(session(), StudySubject.class);

            if (subjectList != null) {
                criteriaHelper.whereIn("subject", subjectList);
            }

            if (studyList != null) {
                criteriaHelper.whereIn("study", studyList);
            }

            return criteriaHelper.getQuery().list();
    }

    public List<FundingSource> getFundingSources() {
        return this.findAll(FundingSource.class);
    }

    public List<VisitTemplatesResponse> getNotApprovedVisits(final String sortBy, final String orderBy, final int page,
            int maxResults) {

        maxResults = negativeIsInfinity(maxResults);

        final String[] columnsArray = {
                "user.lastName",
                "user.firstName",
                "visitTemplate.id",
                "visitTemplate.name",
                "visitTemplate.approved", // will need to turn into string as did hql version
                "visitTemplate.visitType.name",
                "visitTemplate.lastUpdateTime",
                "sublocation.name",
                "study.localId",
                "study.id"
        };

        final String columnList = Joiner.on(", ").join(columnsArray);

        final String findVisitsQueryString = "SELECT " + columnList + " " +
                "FROM " +
                "VisitTemplate visitTemplate, " +
                "Sublocation sublocation, " +
                "Study study LEFT JOIN study.investigator user " +
                "WHERE " +
                "visitTemplate.study = study.id " +
                " and visitTemplate.sublocation = sublocation.id " +
                " and visitTemplate.approved = false and visitTemplate.active = true " +
                "order by " + sortBy + " " + orderBy;

        final int offset = (page - 1) * maxResults;
        
        final Query query = newQuery(findVisitsQueryString);

        query.setFirstResult(offset);
        query.setMaxResults(maxResults);

        @SuppressWarnings("unchecked")
        final List<Object[]> resultRows = query.list();

        final Long total = findNotApprovedVisitTemplateCount();
        
        return enrich(resultRows).map(VisitTemplateRow::fromArray).map(row -> row.toVisitTemplatesResponse(total)).toList();
    }

    private static final class VisitTemplateRow {
        private final String lastName;
        private final String firstName;
        private final Integer vId;
        private final String vName;
        private final Boolean approved;
        private final String vTypeName;
        private final Date vLastUpdateTime;
        private final String sublocName;
        private final String stLocalId;
        private final Integer stId;

        public VisitTemplateRow(final String lastName, final String firstName, final Integer vId,
                                final String vName, final Boolean approved, final String vTypeName,
                                final Date vLastUpdateTime, final String sublocName, final String stLocalId,
                                final Integer stId) {
            super();
            this.lastName = lastName;
            this.firstName = firstName;
            this.vId = vId;
            this.vName = vName;
            this.approved = approved;
            this.vTypeName = vTypeName;
            this.vLastUpdateTime = vLastUpdateTime;
            this.sublocName = sublocName;
            this.stLocalId = stLocalId;
            this.stId = stId;
        }

        static VisitTemplateRow fromArray(final Object[] row) {
            return new VisitTemplateRow(
                    (String)row[0],
                    (String)row[1],
                    (Integer)row[2],
                    (String)row[3],
                    (Boolean)row[4],
                    (String)row[5],
                    (Date)row[6],
                    (String)row[7],
                    (String)row[8],
                    (Integer)row[9]);
        }
        
        VisitTemplatesResponse toVisitTemplatesResponse(final Long total) {
          final String piName = lastName != null ? lastName + " " + firstName : " ";
            
          return new VisitTemplatesResponse(
                vId,
                stId,
                vName,
                vTypeName,
                sublocName,
                approved ? "Approved" : "Not Approved",
                total,
                stLocalId,
                vLastUpdateTime,
                piName);
        }
    }

    Long findNotApprovedVisitTemplateCount(){
        final String findCount = "SELECT count(v) from VisitTemplate v WHERE v.approved = false and v.active = true";
        final Query query = newQuery(findCount);
        return (Long) query.uniqueResult();
    }

    @SuppressWarnings("unchecked")
    public List<StudyUser> findStudyUserRolesByStudy(final Study study) {
        final String findStudyUsers = "SELECT su from StudyUser su WHERE su.study = :study and su.active = true";
        final Query query = newQuery(findStudyUsers).setParameter("study", study);
        return query.list();
    }

    @SuppressWarnings("unchecked")
    public List<Comments> findVisitTemplateCommentsByVisit(final VisitTemplate visit) {
        final CriteriaQueryHelper criteriaHelper = new CriteriaQueryHelper(session(), Comments.class);
        criteriaHelper.whereEquals("visitTemplate", visit);

        return criteriaHelper.getQuery().list();
    }

    @SuppressWarnings("unchecked")
    public Long findNumVisitTemplateCommentsByVisit(final int visitId) {

        final String findCount = "SELECT count(c) from Comments c WHERE c.visitTemplate.id = :visitId ";

        final Query query = newQuery(findCount);
        query.setParameter("visitId", visitId);

        return (Long) query.uniqueResult();
    }

    public long findBookedResourcesCount(final BookedVisit bookedVisit){
        final String hql = "SELECT count(a) FROM BookedResource a"
                + " WHERE "
                + " a.bookedVisit = :bookedVisit";
        final Query query = newQuery(hql).setParameter("bookedVisit", bookedVisit);

        final long count = (long)query.uniqueResult();
        return count;
    }

    public BookedResource findBookedResourceLowest(final BookedVisit bookedVisit){
        final String findTemplateResource = "SELECT a FROM BookedResource a"
                + " WHERE "
                + " a.bookedVisit = :bookedVisit order by a.scheduledStartTime ASC";

        final Query query = newQuery(findTemplateResource).setParameter("bookedVisit", bookedVisit).setFirstResult(0).setMaxResults(1);
        
        @SuppressWarnings("unchecked")
        final List<BookedResource> brs = query.list();
        
        return brs.get(0);
    }

    public List<VisitApprovalModelResponseDTO.VisitApproval> getVisitApprovals(final int id, final String sortBy, final String orderBy, final int page, int maxResults) {

        maxResults = negativeIsInfinity(maxResults);

        final String hql = "select v, u, tah " +
                "from VisitTemplate v, User u, TemplateApprovalHistory tah " +
                "where v.id =:id " +
                "and v.id = tah.visitTemplate " +
                "and u.id = tah.user " +
                " order by " + sortBy + " " + orderBy;

        final int offset = (page - 1) * maxResults;
        
        final Query query = newQuery(hql).setParameter("id", id).setFirstResult(offset).setMaxResults(maxResults);

        @SuppressWarnings("unchecked")
        final List<Object[]> resultRows = query.list();

        final Function<Object[], TemplateApprovalHistory> toTemplateApprovalHistory = resultRow -> {
            final VisitTemplate visitTemplate = (VisitTemplate)resultRow[0];
            final User user = (User)resultRow[1];
            final TemplateApprovalHistory tah = (TemplateApprovalHistory)resultRow[2];

            tah.setUserLastName(user.getLastName());
            tah.setUserFirstName(user.getFirstName());
            tah.setUserRole(user.getRole().getName());
            tah.setUserTitle(user.getInstitutionRole().getName());
            tah.setVisitCreatedDate(visitTemplate.getCreatedDate());
            
            return tah;
        };
        
        final List<TemplateApprovalHistory> tahList = enrich(resultRows).map(toTemplateApprovalHistory).toList();
        
        final Long total = findTemplateApprovalCount(id);
        
        return VisitApprovalModelResponseDTO.fromTemplateApprovalHistoryList(tahList, total);
    }

    Long findTemplateApprovalCount(final int id){
        final String findCount = "select v, u, tah " +
                "from VisitTemplate v, User u, TemplateApprovalHistory tah " +
                "where v.id =:id " +
                "and v.id = tah.visitTemplate " +
                "and u.id = tah.user ";
        
        final Query query = newQuery(findCount).setParameter("id", id);
        
        return (long) query.list().size();
    }

    @SuppressWarnings("unchecked")
    public List<TemplateApprovalHistory> findTemplateApprovalHistoryByVisit(final VisitTemplate visit) {
        final CriteriaQueryHelper criteriaHelper = new CriteriaQueryHelper(session(), TemplateApprovalHistory.class);
        criteriaHelper.whereEquals("visitTemplate", visit);
        
        return criteriaHelper.getQuery().list();
    }

    @SuppressWarnings("unchecked")
    public List<ActivityLog> findActivityLogByVisit(final VisitTemplate visitTemplate) {
        final CriteriaQueryHelper criteriaHelper = new CriteriaQueryHelper(session(), ActivityLog.class);
        criteriaHelper.whereEquals("affectedVisit", visitTemplate);
        
        return criteriaHelper.getQuery().list();
    }

    public TemplateApprovalHistory findTemplateApprovalHistoryByVisitAndUser( final VisitTemplate visit, final User user) {
        final CriteriaQueryHelper criteriaHelper = new CriteriaQueryHelper(session(), TemplateApprovalHistory.class);
        
        criteriaHelper.whereEquals("visitTemplate", visit);
        criteriaHelper.whereEquals("user", user);
        
        return (TemplateApprovalHistory) criteriaHelper.getQuery().uniqueResult();
    }

    @SuppressWarnings("unchecked")
    public List<TemplateApprovalHistory> findTemplateApprovalHistoryListByVisitAndUser(final VisitTemplate visit, final User user) {
        final CriteriaQueryHelper criteriaHelper = new CriteriaQueryHelper(session(), TemplateApprovalHistory.class);
        
        criteriaHelper.whereEquals("visitTemplate", visit);
        criteriaHelper.whereEquals("user", user);
        criteriaHelper.whereEquals("approved", Boolean.FALSE);
        
        return criteriaHelper.getQuery().list();
    }

    @SuppressWarnings("unchecked")
    public List<TemplateApprovalHistory> findTemplateApprovalHistoryListByUser(final User user) {
        final CriteriaQueryHelper criteriaHelper = new CriteriaQueryHelper(session(), TemplateApprovalHistory.class);
        
        criteriaHelper.whereEquals("user", user);
        criteriaHelper.whereEquals("approved", Boolean.TRUE);
        
        return criteriaHelper.getQuery().list();
    }

    public AppointmentStatus findAppointmentStatusById(final Integer id){
        return this.findById(AppointmentStatus.class, id);
    }

    public boolean checkLocalId(final String localid) {
        final CriteriaQueryHelper criteriaHelper = new CriteriaQueryHelper(session(), Study.class);
        criteriaHelper.whereEquals("localId", localid);
        
        final Study localId = (Study) criteriaHelper.getQuery().uniqueResult();
        
        return localId == null;
    }


    @SuppressWarnings("unchecked")
    public List<Study> findStudyByScheduler(final User user) {
        final CriteriaQueryHelper criteriaHelper = new CriteriaQueryHelper(session(), Study.class);
        criteriaHelper.whereEquals("scheduler", user);
        
        return criteriaHelper.getQuery().list();
    }

    @SuppressWarnings("unchecked")
    public List<Study> findStudyByPI(final User user) {
        final CriteriaQueryHelper criteriaHelper = new CriteriaQueryHelper(session(), Study.class);
        criteriaHelper.whereEquals("investigator", user);
        
        return criteriaHelper.getQuery().list();
    }

    @SuppressWarnings("unchecked")
    public List<Study> findStudyByNurse(final User user) {
        final CriteriaQueryHelper criteriaHelper = new CriteriaQueryHelper(session(), Study.class);
        criteriaHelper.whereEquals("protocolNurse", user);
        
        return criteriaHelper.getQuery().list();
    }

    @SuppressWarnings("unchecked")
    public List<Study> findStudyByNutritionist(final User user) {
        final CriteriaQueryHelper criteriaHelper = new CriteriaQueryHelper(session(), Study.class);
        criteriaHelper.whereEquals("protocolNutritionist", user);
        
        return criteriaHelper.getQuery().list();
    }

    @SuppressWarnings("unchecked")
    public List<VisitTemplate> findVisitTemplateByStudy(final Study study) {
        final CriteriaQueryHelper criteriaHelper = new CriteriaQueryHelper(session(), VisitTemplate.class);
        
        criteriaHelper.whereEquals("study", study);
        criteriaHelper.whereEquals("approved", Boolean.FALSE);
        
        return criteriaHelper.getQuery().list();
    }

    String flipOrderByForVisitDotApproved(final String sortBy, final String orderBy) {
        String result = orderBy;

        if (sortBy.equals("v.approved")) {
            result = (orderBy.equals("DESC")) ? "ASC" : "DESC";
        }

        return result;
    }

    public List<VisitTemplatesResponse> getStudyVisitsByStatus(
            final int study,
            final boolean active,
            final String sortBy,
            final String orderBy,
            final int page,
            int maxResults,
            final SearchDTO searchDTO) {

        maxResults = negativeIsInfinity(maxResults);

        String searchClause = searchDTO.toSearchClause();

        final String findVisitsCoreHql =
                "from VisitTemplate v, Study s " +
                "WHERE v.study = s.id and s.id =:study and v.active = :active " +
                searchClause;

        final String possiblyAdjustedOrderBy = flipOrderByForVisitDotApproved(sortBy, orderBy);
        final String findVisitsHql = "SELECT v " + findVisitsCoreHql + " ORDER BY " + sortBy + " " + possiblyAdjustedOrderBy;
        final int offset = (page - 1) * maxResults;
        final Query query = newQuery(findVisitsHql).setParameter("study", study).setParameter("active", active).setFirstResult(offset).setMaxResults(maxResults);
        searchDTO.assignQueryParameterValues(query);
        @SuppressWarnings("unchecked")
        final List<VisitTemplate> resultRows = query.list();

        final String countVisitsHql = "SELECT count(v) " + findVisitsCoreHql;
        final Query countQuery = newQuery(countVisitsHql).setParameter("study", study).setParameter("active", active);
        searchDTO.assignQueryParameterValues(countQuery);
        Long total = (Long) countQuery.uniqueResult();

        return enrich(resultRows).map(visitTemplate -> setVisitTemplateData(total, visitTemplate)).toList();

    }

    public VisitTemplatesResponse setVisitTemplateData(final long total, final VisitTemplate visitTemplate) {
        
        final String piName = getPiName(visitTemplate);
        
        return new VisitTemplatesResponse(visitTemplate.getId(), visitTemplate.getStudy().getId(), visitTemplate.getName(),
                visitTemplate.getVisitType().getName(), visitTemplate.getSublocation().getName(),
                visitTemplate.getApprovedString(), total, visitTemplate.getStudy().getLocalId(),
                visitTemplate.getLastUpdateTime(), piName);
    }

    private String getPiName(final VisitTemplate visitTemplate) {
        if (visitTemplate.getStudy().getInvestigator() != null) {
            return visitTemplate.getStudy().getInvestigator().getLastName() + " " + visitTemplate.getStudy().getInvestigator().getFirstName();
        }
        
        return " ";
    }

    Long findVisitTemplateCount(final int study, final boolean active){
        final String findCount = "SELECT count(v) from VisitTemplate v, Study s WHERE v.study = s.id and s.id = :study and v.active = :active";

        final Query query = newQuery(findCount).setParameter("study", study).setParameter("active", active);
        
        return (Long) query.uniqueResult();
    }

    public List<StudyDataResponse> getStudyListByPerson(final String sortBy, final String orderBy,
            final int page, final int maxResults, final User studyPerson, SearchDTO searchDTO) {

        return getStudyList(sortBy, orderBy, page, maxResults,
                            "study_user su, ",
                            " s.id = su.study and su.user = :user and su.active=true and ",
                            studyPerson, searchDTO
        );

    }

    public List<StudyDataResponse> getStudyList(final String sortBy, final String orderBy,
            final int page, final int maxResults, SearchDTO searchDTO) {

        return getStudyList(sortBy, orderBy, page, maxResults, "", "", null, searchDTO);
    }

    public List<StudyDataResponse> getStudyList(final String sortBy, final String orderBy, final int page, int maxResults,
            final String fromStudyUser, final String whereStudyUser, final User studyUser, SearchDTO searchDTO) {

        maxResults = negativeIsInfinity(maxResults);

        StringBuffer findStudySql = new StringBuffer();
        findStudySql.append("select " +
                "s.id, " +
                "s.irb, " +
                "s.local_id, " +
                "s.name, " +
                "ss.short_name, " +
                "pi.first_name, " +
                "pi.last_name " +
                " from study_status ss, " +
                fromStudyUser);

        findStudySql.append(" study s left outer join user pi on pi.id = s.principal_investigator " +
                " where " + whereStudyUser);

        String totalSearchClause = "";
        if(searchDTO != null) {
            totalSearchClause = searchDTO.toSearchClause();
        }

        findStudySql.append(" ss.id = s.study_status" +  totalSearchClause  + " order by " + sortBy + " " + orderBy);

        final Query query = newNativeQuery(findStudySql.toString());

        if (searchDTO != null) {
            searchDTO.assignQueryParameterValues(query);
        }

        if (studyUser != null) {
            query.setParameter("user", studyUser.getId());
        }

        final List<Integer> countIds = query.list();
        
        final Long total = Long.valueOf(countIds.size());

        final int offset = (page - 1) * maxResults;
        
        query.setFirstResult(offset);
        query.setMaxResults(maxResults);

        final List<Object[]> resultRowList = query.list();

        final Function<Object[], StudyDataResponse> toStudyDataResponse = resultRow -> new StudyDataResponse(
                total,                 // Long totalCount,
                (Integer)resultRow[0], // Integer id,            "s.id, " +
                (String)resultRow[1],  // String irbNumber,       "s.irb, " +
                (String)resultRow[2],  // String localId,         "s.local_id, " +
                (String)resultRow[3],  // String name,            "s.name, " +
                (String)resultRow[4],  // String studyStatus      "ss.short_name, " +
                (String)resultRow[5],  // String piFirstName      "pi.first_name, " +
                (String)resultRow[6]   // String piLastName)      "pi.last_name, " +
                );
        
        return enrich(resultRowList).map(toStudyDataResponse).toList();
    }

    public GetStudiesResponse findOpenStudyListByPerson(
            final User studyPerson,
            final String filterString,
            final String sortBy,
            final String orderBy,
            final int page,
            final int maxResults) {

        String queryString = "select distinct s from Study s, StudyUser su, VisitTemplate v " +
                "where s.id = su.study and s.id = v.study and v.approved = true and su.user = :user and su.active=true and s.studyStatus.isOpen = TRUE ";

        queryString = addFilterAndOrder(filterString, sortBy, orderBy, queryString);

        final Query mainQuery = newQuery(queryString).setParameter("user", studyPerson);
        
        return getStudies(page, maxResults, mainQuery);
    }

    public GetStudiesResponse getOpenStudies(
            final String filterString,
            final String sortBy,
            final String orderBy,
            final int page,
            final int maxResults) {

        String queryString = "select distinct s from Study s, VisitTemplate v where v.study = s.id and s.studyStatus.isOpen = TRUE and v.approved = true ";

        queryString = addFilterAndOrder(filterString, sortBy, orderBy, queryString);

        final Query mainQuery = newQuery(queryString);

        return getStudies(page, maxResults, mainQuery);
    }

    String addFilterAndOrder(String filterString, String sortBy, String orderBy, String queryString) {
        if ( isNonNullNonEmpty(filterString)) {
            queryString += " and (CONCAT(lower(s.localId), ' - ', lower(s.name)) LIKE '%" +filterString+ "%') ";
        }

        queryString += " order by " + sortBy + " " + orderBy;
        return queryString;
    }

    private GetStudiesResponse getStudies(
            final int page,
            int maxResults,
            final Query mainQuery) {

        maxResults = negativeIsInfinity(maxResults);

        final long total = mainQuery.list().size();

        final int offset = (page - 1) * maxResults;
        
        mainQuery.setFirstResult(offset);
        mainQuery.setMaxResults(maxResults);

        @SuppressWarnings("unchecked")
        final List<Study> resultRows = mainQuery.list();

        return GetStudiesResponse.createGetStudiesResponse(resultRows, total);
    }

    public GetStudyVisitsResponse getStudyVisits(
            final String filterString,
            final String ofSortBy,
            final String ofOrderBy,
            final Integer ofPage,
            Integer ofMaxResults,
            final Integer studyId,
            final Boolean ofApproved) {

        final Study study = findStudyById(studyId);
        
        final HqlClauses.WhereBuilder whereClause = whereBuilder();
        
        whereClause.equalTo(Optional.of("v.study"), ":sid");
        whereClause.accumulate("(select count(tr) from TemplateResource tr where tr.visitTemplate = v) > 0 ");

        if (ofApproved) {
            whereClause
            .equalTo(Optional.of("true"), "v.active")
            .equalTo(Optional.of("true"), "v.approved");
        }

        if (isNonNullNonEmpty(filterString)) {
            whereClause.like(Optional.of(":filterString"), "lower(v.name) ");
        }
        
        final String whereString = whereClause.build();

        final String baseHql = "select v from VisitTemplate v " + whereString + " order by " + ofSortBy + " " + ofOrderBy;

        final boolean filterStringIsPresent = isNonNullNonEmpty(filterString);
        
        final String simpleHql;
        
        if (filterStringIsPresent) {
            simpleHql = baseHql.replaceAll(":filterString", filterString.toLowerCase());
        } else {
            simpleHql = baseHql;
        }

        final Query mainQuery = newQuery(simpleHql).setParameter("sid", study);

        final long total = mainQuery.list().size();

        final int page = ofPage;
        int maxResults = ofMaxResults;
        maxResults = negativeIsInfinity(maxResults);

        final int offset = (page - 1) * maxResults;
        
        mainQuery.setFirstResult(offset);
        mainQuery.setMaxResults(maxResults);

        @SuppressWarnings("unchecked")
        final List<VisitTemplate> resultRows = mainQuery.list();

        final String investigator = getInvestigator(study);
        
        return GetStudyVisitsResponse.createGetStudyVisitsResponse(resultRows, total, study.getName(), study.getLocalId(), investigator, study.getId());
    }

    private String getInvestigator(final Study study) {
        if (study.getInvestigator() != null) {
            return study.getInvestigator().getFirstName() + " " + study.getInvestigator().getLastName();
        } 
        
        return NA;
    }

    public List<UserDataResponse> getStudyMembers(final Study study, final String sortBy, final String orderBy, final int page, int maxResults, final SearchDTO searchDTO) {

        maxResults = negativeIsInfinity(maxResults);

        final StringBuffer commonQueryStringBuffer = new StringBuffer(
                "from StudyUser su WHERE su.study = :study "
        );

        if(searchDTO != null) {
            String totalSearchClause = searchDTO.toSearchClause();
            commonQueryStringBuffer.append(totalSearchClause);
        }

        final StringBuffer rowsQueryStringBuffer = new StringBuffer("SELECT su ");
        rowsQueryStringBuffer.append(commonQueryStringBuffer);
        rowsQueryStringBuffer.append(" order by " + sortBy + " " + orderBy);
        final int offset = (page - 1) * maxResults;
        final Query rowsQuery = newQuery(rowsQueryStringBuffer.toString()).setParameter("study", study);
        if(searchDTO != null) {
            searchDTO.assignQueryParameterValues(rowsQuery);
        }
        rowsQuery.setFirstResult(offset);
        rowsQuery.setMaxResults(maxResults);

        @SuppressWarnings("unchecked")
        final List<StudyUser> resultRows = rowsQuery.list();

        final StringBuffer countQueryStringBuffer = new StringBuffer("SELECT count(su.id) ");
        countQueryStringBuffer.append(commonQueryStringBuffer);
        final Query countQuery = newQuery(countQueryStringBuffer.toString()).setParameter("study", study);

        if(searchDTO != null) {
            searchDTO.assignQueryParameterValues(countQuery);
        }

        final Long total = (Long) countQuery.uniqueResult();
        return UserDataResponse.getStudyUserData(resultRows, total);
    }


    public List<StudySubject> findStudySubjectBySubjectMrn(SubjectMrn subjectMrn) {

        String queryString = "Select ss FROM StudySubject ss WHERE ss.subjectMrn = :subjectMrn";
        final Query query = newQuery(queryString);
        query.setParameter("subjectMrn", subjectMrn);
        return query.list();

    }


    public List<StudySubject> getRawStudySubjectsByStudy(
            final Study study,
            final Boolean ofWantAll,
            final SearchDTO searchDTO) {

        final List<StudySubject> resultRows = findStudySubjectsByStudyClausically(study, ofWantAll, searchDTO);

        return resultRows;
    }

    @SuppressWarnings("unchecked")
    public List<StudySubject> findStudySubjectsByStudyClausically(
            final Study study,
            final Boolean ofWantAll,
            final SearchDTO searchDTO) {

        StringBuilder findStudySubjects = new StringBuilder();
        findStudySubjects.append("SELECT ss FROM StudySubject ss, Subject su, SubjectMrn sm ");

        final HqlClauses.WhereBuilder builder = whereBuilder();

        builder
                .equalTo(Optional.of(":study"), "ss.study")
                .equalTo(Optional.of("ss.subjectMrn"), "sm.id")
                .equalTo(Optional.of("sm.subject"), "su.id")
                .equalTo(Optional.of("true"), "su.active");

        if (! ofWantAll) {
            builder.equalTo(Optional.of("true"), "ss.active");
        }

        final String whereClause = builder.build();
        findStudySubjects.append(whereClause);
        findStudySubjects.append(" and su.archivalStatus IS NULL ");

        String fullSearchClauseString = "";

        if(searchDTO != null) {

            List<SearchDTO.SearchItem> searchItems = searchDTO.getSearchItems();

            fullSearchClauseString = searchDTO.toSearchClause();
        }

        findStudySubjects.append(fullSearchClauseString);

        final Query mainQuery = newQuery(findStudySubjects.toString()).setParameter("study", study);

        if(searchDTO != null) {
            searchDTO.assignQueryParameterValues(mainQuery);
        }

        return mainQuery.list();
    }

}
