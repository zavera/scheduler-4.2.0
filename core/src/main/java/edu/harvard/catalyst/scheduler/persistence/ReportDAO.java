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
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import edu.harvard.catalyst.hccrc.core.util.Pairs;
import edu.harvard.catalyst.scheduler.core.BookedVisitActivityLogStatics;
import edu.harvard.catalyst.scheduler.core.Statics;
import edu.harvard.catalyst.scheduler.dto.*;
import edu.harvard.catalyst.scheduler.dto.response.CancellationsReportResponseDTO;
import edu.harvard.catalyst.scheduler.dto.response.StudyDataReportResponseDTO;
import edu.harvard.catalyst.scheduler.dto.statics.StudyStatusFilter;
import edu.harvard.catalyst.scheduler.entity.*;
import edu.harvard.catalyst.scheduler.persistence.HqlClauses.WhereBuilder;
import edu.harvard.catalyst.scheduler.util.SubjectDataEncryptor;
import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.query.NativeQuery;
import org.hibernate.query.Query;
import org.hibernate.transform.Transformers;
import org.hibernate.type.DateType;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.google.common.collect.Lists.newArrayList;
import static edu.harvard.catalyst.hccrc.core.util.ListUtils.enrich;
import static edu.harvard.catalyst.hccrc.core.util.Pair.pair;
import static edu.harvard.catalyst.hccrc.core.util.Pairs.toMap;
import static edu.harvard.catalyst.scheduler.core.Statics.NA;
import static edu.harvard.catalyst.scheduler.core.Statics.NO_SUBJECT_ASSIGNED;
import static edu.harvard.catalyst.scheduler.persistence.HqlClauses.whereBuilder;
import static edu.harvard.catalyst.scheduler.persistence.SortStrategy.ASCENDING;
import static edu.harvard.catalyst.scheduler.persistence.SortStrategy.DESCENDING;
import static edu.harvard.catalyst.scheduler.util.DateUtility.*;
import static edu.harvard.catalyst.scheduler.util.MiscUtil.isNonNullNonEmpty;

@Repository
@Transactional
public class ReportDAO extends SiteDAO {

	private static final Logger log = Logger.getLogger(ReportDAO.class);

	static String orEmpty(final String s) {
		return s == null ? "" : s;
	}

	static void setStartAndEndTimeParameters(final ReportDTO dto, final Query query) {
		query.setParameter("startTime", dto.getStartTime());
		query.setParameter("endTime", dto.getEndTime());
	}

	private static void setSubjectDateParam(final Query query, final String subjectDob) {
		if (!subjectDob.isEmpty()) {
			query.setParameter("subjectDate", parse(monthDayYear(), subjectDob), DateType.INSTANCE);
		}
	}

	static String makeLevelMapKey(final Integer visitId, final String levelValue) {
		final String levelString = makeLevelString(levelValue);

		return "Visit" + visitId + levelString;
	}

	static String makeLevelString(final String levelValue) {
		return "Level" + levelValue;
	}

	private static void logHqlQuery(final List<?> results, final Query query) {

		final int numResults = results != null ? results.size() : -1;

		final String logMessage = " HQL Query with " + numResults + " results\n" + query.getQueryString().trim();

		log.debug(logMessage);
	}

	private static void logSqlQuery(final List<?> results, final Query query) {

		final int numResults = results != null ? results.size() : -1;

		final String logMessage = " SQL Query with " + numResults + " results\n" + query.getQueryString().trim();

		log.debug(logMessage);
	}

	private void logCriteriaQuery(final List<?> results,
								  final CriteriaQueryHelper criteria,
								  final String description) {

		final int numResults = results != null ? results.size() : -1;

		final String logMessage = " Criteria Query with " +
				numResults + " results: " +
				criteria.toString().trim() + ", " + description.trim();

		log.debug(logMessage);
	}

	public List<Report> getReports() {
		final CriteriaQueryHelper criteriaHelper = new CriteriaQueryHelper(session(), Report.class);
		criteriaHelper.orderAsc("title");

		@SuppressWarnings("unchecked")
		final List<Report> result = criteriaHelper.getQuery().list();
		
		logCriteriaQuery(result, criteriaHelper, "order by 'title', ascending");

		return result;
	}

	public List<ResourceType> getResourceTypes() {
		return ResourceType.valueList();
	}

	public List<Sublocation> getSublocations() {
		final CriteriaQueryHelper criteriaHelper = new CriteriaQueryHelper(session(), Sublocation.class);
		criteriaHelper.orderAsc("name");
		
		@SuppressWarnings("unchecked")
        final List<Sublocation> sublocations = criteriaHelper.getQuery().list();
		
		final Predicate<Sublocation> isNonCrcOrOffInstitution = sl -> {
		    return sl.getName().equalsIgnoreCase("Non CRC") || sl.getName().equalsIgnoreCase("Off Institution");
		};
		
		final List<Sublocation> otherSublocationList = enrich(sublocations).filter(isNonCrcOrOffInstitution).toList();
		
		// here we remove the 'other' (i.e. non-CRC and off institution) sublocations, and add them right back the list
		// with the effect that they are now at the beginning of the list. Neat trick.
		sublocations.removeAll(otherSublocationList);
		sublocations.addAll(otherSublocationList);
		
		return sublocations;
	}

	public Report getReportDataById(final int id) {
		return this.findById(Report.class, id);
	}

	// TODO - this is duplicated from StudyDAO... obviously DAOs need
	// refactoring but don't want to get sidetracked now. -??
	// NB: Package-private for testing and access in FilterStrategy -Clint
	InstitutionRole findInstitutionRoleByType(final InstitutionRoleType type) {
		final CriteriaQueryHelper criteriaHelper = new CriteriaQueryHelper(session(), InstitutionRole.class);
		criteriaHelper.whereEquals("type", type);

		return (InstitutionRole) criteriaHelper.getQuery().uniqueResult();
	}

	// FIXME: Return something instead of mutating the passed-in DTO
	public List<UserReportDTO> getUserDataReport(final ReportDTO dto) {
		final String filterString = dto.getFilterString();
		final String filterid = dto.getFilterId();
		final String sortid = dto.getSortId();

		final CriteriaQueryHelper criteriaHelper = new CriteriaQueryHelper(session(), User.class);

		final Optional<UserDataReportFilterStrategy> filterStrategyOption = UserDataReportFilterStrategy.fromIdString(filterid);

		if (filterStrategyOption.isPresent()) {
			filterStrategyOption.get().makeCriterion(this, filterString, criteriaHelper);
		}
		final SortStrategy sortStrategy = SortStrategy.fromIdString(sortid).orElse(ASCENDING);

		sortStrategy.makeOrder("lastName", criteriaHelper);

		@SuppressWarnings("unchecked")
		final List<User> users = criteriaHelper.getQuery().list();
		logCriteriaQuery(users, criteriaHelper, "order by 'lastName', default:ascending. actual sortId <" + sortid + ">. may filter on filterId <" + filterid + "> and filterString <" + filterString + ">");

		List<UserReportDTO> userReportDTOS = users.stream()
				.map(u -> new UserReportDTO(u))
				.collect(Collectors.toList());

		dto.setUserDataReport(userReportDTOS);
		return userReportDTOS;
	}

	public List<ProtoNurseReportDTO> getProtoNurseAndNutritionReport(final ReportDTO dto) {
		final String filterString = dto.getFilterString();
		final String filterid = dto.getFilterId();
		final String sortid = dto.getSortId();
		String baseHql = "select u.id, u.firstName, u.middleName, u.lastName, s.name, s.spid, s.catalystId, s.localId, "
				+ "s.studyStatus.name, s.irb, s.irbExpiration from User u, Study s where ";
		if (dto.getName().equalsIgnoreCase("proto_nutritionist")){
			baseHql += " s.protocolNutritionist = u.id ";
		}
		else if (dto.getName().equalsIgnoreCase("proto_by_nurse")){
			baseHql += " s.protocolNurse = u.id ";
		}

		@SuppressWarnings("unchecked")
		final Map<String, String> filterIdsToColumns = Pairs.toMap(pair("1", "u.lastName"), pair("2", "s.localId"), pair("3", "s.studyStatus.name"));

		final String whereSubClause;

		if (filterIdsToColumns.containsKey(filterid)) {
			final String columnName = filterIdsToColumns.get(filterid);

			whereSubClause = " and " + columnName + " like '%" + filterString + "%' ";
		} else {
			whereSubClause = "";
		}

		final String groupByClause = " group by s.id ";

		final String sortByColumnName = "u.lastName";
		final SortStrategy sortStrategy = SortStrategy.fromIdString(sortid).orElse(ASCENDING);
		final String orderByClause = setOrderByClause(sortByColumnName, sortStrategy);

		final String hql = baseHql + whereSubClause + groupByClause + orderByClause;

		final Query query = session().createQuery(hql);

		@SuppressWarnings("unchecked")
		final List<Object[]> queryResults = query.list();
		logHqlQuery(queryResults, query);

		final List<ProtoNurseReportDTO> protoNurseNutritionReportDtos = enrich(queryResults).map(resultRow -> ProtoNurseReportDTO.fromArray(resultRow)).toList();

		dto.setProtoNurseNutritionReport(protoNurseNutritionReportDtos);

		return protoNurseNutritionReportDtos;
	}

	// !! nice to more-clearly distinguish names associated with this report and
	//        those with the report for the following method
	public List<StaffAuditSubjectViewsReportDTO> getStaffAuditSubjectViewsReport(final ReportDTO dto, final User user) {
		final String filterString = dto.getFilterString();
		final String filterid = dto.getFilterId();
		final String sortid = dto.getSortId();

		final InstitutionRoleType institutionRoleType = user.getInstitutionRole().getType();

		final String groupByClause = " group by al.affectedSubject, u.id, al.actionPerformed, al.date ";

		final String baseHql = "select max(sm), al.actionPerformed, al.date, max(al.ipAddress), " +
                "u.firstName, u.middleName, u.lastName, u.id, u.ecommonsId " +
                "from ActivityLog al, SubjectMrn sm, User u  " +
                "where al.date >= :startTime and al.date <= :endTime and "
				+ "al.affectedSubject = sm.subject.id and al.performingUser = u.id ";

		final String baseHqlWithParams;

		if (institutionRoleType == InstitutionRoleType.ROLE_SUPER_ADMIN) {
			baseHqlWithParams = baseHql;
		} else {
			baseHqlWithParams = baseHql + " and u.id = :currentUser ";
		}

		final String filterClause;

		if ("1".equalsIgnoreCase(filterid)) {
			filterClause = " and u.lastName like '%" + filterString + "%' ";
		} else if ("2".equalsIgnoreCase(filterid)) {
			filterClause = " and u.ecommonsId like '%" + filterString + "%' ";
		} else {
			filterClause = "";
		}

		final String hql = baseHqlWithParams + filterClause + groupByClause;

		final Query query = session().createQuery(hql);

		setStartAndEndTimeParameters(dto, query);

		if (institutionRoleType != InstitutionRoleType.ROLE_SUPER_ADMIN) {
			query.setParameter("currentUser", user.getId());
		}

		@SuppressWarnings("unchecked")
		final List<Object[]> resultRows = query.list();
		logHqlQuery(resultRows, query);

		List<StaffAuditSubjectViewsReportDTO> finalList = Lists.newArrayList();

		for (final Object[] resultRow : resultRows) {

			SubjectMrn subjectMrn = (SubjectMrn) resultRow[0];

			if (subjectMrn.getSubject().getArchivalStatus() == null) {
				final StaffAuditSubjectViewsReportDTO localDto = new StaffAuditSubjectViewsReportDTO();

				localDto.setSubjectId(subjectMrn.getId());

				localDto.setSubjectFirstName(SubjectDataEncryptor.decrypt(subjectMrn.getSubject().getFirstName()));
				localDto.setSubjectMiddleName(SubjectDataEncryptor.decrypt(subjectMrn.getSubject().getMiddleName()));
				localDto.setSubjectLastName(SubjectDataEncryptor.decrypt(subjectMrn.getSubject().getLastName()));
				localDto.setMrn(SubjectDataEncryptor.decrypt(subjectMrn.getMrn()));

				localDto.setActionPerformed((String) resultRow[1]);
				localDto.setDate((Date) resultRow[2]);
				localDto.setIpAddress((String) resultRow[3]);
				localDto.setUserFirstName((String) resultRow[4]);
				localDto.setUserMiddleName((String) resultRow[5]);
				localDto.setUserLastName((String) resultRow[6]);
				localDto.setUserId((Integer) resultRow[7]);
				localDto.setEcommons((String) resultRow[8]);
				finalList.add(localDto);
			}
		}


		final Optional<SortStrategy> sortStrategyOption = SortStrategy.fromIdString(sortid);

		final List<StaffAuditSubjectViewsReportDTO> sortedStaffAuditSubjectViewsReportDTOs = Lists.newArrayList(finalList);

		if (sortStrategyOption.isPresent()) {
			if (sortStrategyOption.get() == ASCENDING) {
				Collections.sort(sortedStaffAuditSubjectViewsReportDTOs, StaffAuditNameComparator);
			} else {
				Collections.sort(sortedStaffAuditSubjectViewsReportDTOs, StaffAuditNameComparatorDesc);
			}
		} else {
			Collections.sort(sortedStaffAuditSubjectViewsReportDTOs, StaffAuditNameComparator);
		}

		dto.setStaffAuditSubjectViewsReport(sortedStaffAuditSubjectViewsReportDTOs);
		return sortedStaffAuditSubjectViewsReportDTOs;
	}

	public List<SubjectAuditStaffViewsReportDTO> getSubjectAuditStaffViewsReport(final ReportDTO dto) {
		final String sortid = dto.getSortId();
		final String filterString = dto.getFilterString();
		final String filterid = dto.getFilterId();

		final String baseHql = "select al.performingUser, al.actionPerformed, al.date, " +
                "al.ipAddress, sm from ActivityLog al, SubjectMrn sm " +
                "where al.date >= :startTime and al.date <= :endTime and " +
                "sm.mrn = :mrn and al.affectedSubject = sm.subject.id " +
				" and sm.subject.archivalStatus IS NULL ";

		final String filterClause;

		if ("1".equalsIgnoreCase(filterid)) {
			filterClause = " and al.performingUser.lastName like '%" + filterString + "%' ";
		} else {
			filterClause = "";
		}

		final String sortByColumnName = "al.date";
		final SortStrategy sortStrategy = SortStrategy.fromIdString(sortid).orElse(ASCENDING);
		final String orderByClause = setOrderByClause(sortByColumnName, sortStrategy);

		final Query query = session().createQuery(baseHql + filterClause + orderByClause);

		setStartAndEndTimeParameters(dto, query);
		query.setParameter("mrn", SubjectDataEncryptor.encrypt(dto.getMrn().toUpperCase()));

		@SuppressWarnings("unchecked")
		final List<Object[]> resultRows = query.list();
		logHqlQuery(resultRows, query);

		final List<SubjectAuditStaffViewsReportDTO> subjectAuditStaffViewsReportDTOs = Lists.newArrayList();

		for (final Object[] resultRow : resultRows) {
			final SubjectAuditStaffViewsReportDTO newObj = new SubjectAuditStaffViewsReportDTO();

			newObj.setUserId((User) resultRow[0]);
			newObj.setActionPerformed((String) resultRow[1]);
			newObj.setDate((Date) resultRow[2]);
			newObj.setIpAddress((String) resultRow[3]);

			SubjectMrn subjectMrn = (SubjectMrn) resultRow[4];
			newObj.setSubjectName(SubjectDataEncryptor.decrypt(subjectMrn.getSubject().getFullName()));
			newObj.setSubjectId(subjectMrn.getId());

			subjectAuditStaffViewsReportDTOs.add(newObj);
		}

		dto.setSubjectAuditStaffViewsReport(subjectAuditStaffViewsReportDTOs);
		return subjectAuditStaffViewsReportDTOs;
	}

	public List<OverrideReportDTO> getOverrideReport(final ReportDTO dto) {
		final String sortid = dto.getSortId();
		final String filterString;
		final String filterid;

		if ("0".equals(dto.getFilterId())) {
			filterid = null;
			filterString = null;
		} else {
			filterString = dto.getFilterString();
			filterid = dto.getFilterId();
		}

		// todo: NB: 'ROOM RESERVED' is not ever persisted as a value. Looks like legacy cruft
		final String baseHql = "select al from ActivityLog al "
				+ "where (al.appointmentOverrideReason IS NOT NULL) and "
				+ " al.actionPerformed != 'OVERBOOK' and al.actionPerformed != 'ROOM RESERVED' "
				+ "and al.date >= :startTime and al.date <= :endTime ";

		final String filterClause = setOverrideReportFilterClause(filterString, filterid);

		final String sortByColumnName = "al.performingUser.ecommonsId";
		final SortStrategy sortStrategy = SortStrategy.fromIdString(sortid).orElse(ASCENDING);
		final String orderByClause = setOrderByClause(sortByColumnName, sortStrategy);

		final Query query = session().createQuery(baseHql + filterClause + orderByClause);

		setStartAndEndTimeParameters(dto, query);

		@SuppressWarnings("unchecked")
		final List<ActivityLog> activityLogs = query.list();
		logHqlQuery(activityLogs, query);

		final Comparator<ActivityLog> ordering;

		if (sortStrategy == ASCENDING) {
			ordering = OverrideComparator;
		} else {
			ordering = OverrideComparatorDesc;
		}

		Collections.sort(activityLogs, ordering);

		List<OverrideReportDTO> overrideReportDTOS = activityLogs.stream()
				.map(al -> new OverrideReportDTO(al))
				.collect(Collectors.toList());

		dto.setOverrideReport(overrideReportDTOS);
		return overrideReportDTOS;
	}

	// not private so it can be tested
	protected String setOverrideReportFilterClause(final String filterString, final String filterid) {
		String filterClause;
		if (isNonNullNonEmpty(filterString) && "1".equals(filterid)) {
			filterClause = " and al.performingUser.ecommonsId like '%" + filterString + "%' ";
		} else if (isNonNullNonEmpty(filterString) && "2".equals(filterid)) {
			filterClause = " and al.bookedVisit.visitTemplate.name like '%" + filterString + "%' ";
		} else if (isNonNullNonEmpty(filterString) && "3".equals(filterid)) {
			filterClause = " and al.bookedVisit.study.localId like '%" + filterString + "%' ";
		} else if ("4".equals(filterid)) {
			filterClause = " and al.appointmentOverrideReason.id like '%" + "1" + "%' ";
		} else if ("5".equals(filterid)) {
			filterClause = " and al.appointmentOverrideReason.id like '%" + "2" + "%' ";
		} else if ("6".equals(filterid)) {
			filterClause = " and al.appointmentOverrideReason.id like '%" + "3" + "%' ";
		} else if ("7".equals(filterid)) {
			filterClause = " and al.appointmentOverrideReason.id like '%" + "4" + "%' ";
		} else if ("8".equals(filterid)) {
			filterClause = " and al.appointmentOverrideReason.id like '%" + "5" + "%' ";
		} else if ("9".equals(filterid)) {
			filterClause = " and al.appointmentOverrideReason.id like '%" + "6" + "%' ";
		} else if ("10".equals(filterid)) {
			filterClause = " and al.appointmentOverrideReason.id like '%" + "7" + "%' ";
		} else {
			filterClause = "";
		}
		return filterClause;
	}

	public List<SubjectPurgeReportDTO> getSubjectPurgeReport(final ReportDTO dto) {
		String filterString = dto.getFilterString();
		final String filterid = dto.getFilterId();
		final String sortid = dto.getSortId();

		if (filterid != null && ("1".equals(filterid) || "2".equals(filterid))) {
			filterString = SubjectDataEncryptor.encrypt(dto.getFilterString().toUpperCase());
		}

		final String hql = " select s.firstName, s.middleName, s.lastName, sm.mrn," +
                " s.createdDate " +
                " from Subject s, SubjectMrn sm, BookedVisit bv " +
                "where bv.subjectMrn = sm.id and sm.subject = s.id " +
				" and s.archivalStatus IS NULL ";

		String hql2 = " select s.firstName, s.middleName, s.lastName, sm.mrn," +
                " s.createdDate, u.firstName, u.middleName, u.lastName " +
                " from Subject s, ActivityLog al, SubjectMrn sm, User u " +
                "where sm.subject = s.id and al.affectedSubject = s.id and " +
                " al.actionPerformed = 'CREATE SUBJECT' and al.performingUser = u.id " +
				" and s.archivalStatus IS NULL ";

		if (isNonNullNonEmpty(filterString) && "1".equals(filterid)) {
			hql2 += " and s.lastName like '%" + filterString + "%' ";
		}
		if (isNonNullNonEmpty(filterString) && "2".equals(filterid)) {
			hql2 += " and sm.mrn like '%" + filterString + "%' ";
		}
		if (isNonNullNonEmpty(filterString) && "3".equals(filterid)) {
			hql2 += " and u.lastName like '%" + filterString + "%' ";
		}

		final Session session = sessionFactory.getCurrentSession();

		final Query query = session.createQuery(hql);
		final Query query2 = session.createQuery(hql2);

		@SuppressWarnings("unchecked")
		final List<Object[]> list = query.list();
		logHqlQuery(list, query);

		final List<SubjectPurgeReportDTO> resultList = Lists.newArrayList();
		final List<SubjectPurgeReportDTO> resultList2 = Lists.newArrayList();

		for (final Object[] obj : list) {
			final SubjectPurgeReportDTO newDto = new SubjectPurgeReportDTO();

			newDto.setFirstName(SubjectDataEncryptor.decrypt((String) obj[0]));
			newDto.setMiddleName(SubjectDataEncryptor.decrypt((String) obj[1]));
			newDto.setLastName(SubjectDataEncryptor.decrypt((String) obj[2]));
			newDto.setMrn(SubjectDataEncryptor.decrypt((String) obj[3]));
			newDto.setCreateTime((Date) obj[4]);
			newDto.setUserFirstName(null);
			newDto.setUserMiddleName(null);
			newDto.setUserLastName(null);

			resultList.add(newDto);
		}

		@SuppressWarnings("unchecked")
		final List<Object[]> list2 = query2.list();
		logHqlQuery(list2, query);

		for (final Object[] obj : list2) {
			final SubjectPurgeReportDTO newDto = new SubjectPurgeReportDTO();

			newDto.setFirstName(SubjectDataEncryptor.decrypt((String) obj[0]));
			newDto.setMiddleName(SubjectDataEncryptor.decrypt((String) obj[1]));
			newDto.setLastName(SubjectDataEncryptor.decrypt((String) obj[2]));
			newDto.setMrn(SubjectDataEncryptor.decrypt((String) obj[3]));
			newDto.setCreateTime((Date) obj[4]);
			newDto.setUserFirstName((String) obj[5]);
			newDto.setUserMiddleName((String) obj[6]);
			newDto.setUserLastName((String) obj[7]);

			resultList2.add(newDto);
		}

		for (int i = 0; i < resultList.size(); i++) {
			for (int j = 0; j < resultList2.size(); j++) {
				if (resultList2.get(j).getMrn().equals(resultList.get(i).getMrn())) {
					resultList2.remove(j);
				}
			}
		}

		final SortStrategy sortStrategy = SortStrategy.fromIdString(sortid).orElse(ASCENDING);

		final Comparator<SubjectPurgeReportDTO> ordering;

		if (sortStrategy == DESCENDING) {
			ordering = SubjectPurgeNameComparatorDesc;
		} else {
			ordering = SubjectPurgeNameComparator;
		}

		Collections.sort(resultList2, ordering);

		dto.setSubjectPurgeReport(resultList2);
		return resultList2;
	}

	public List<OffUnitReportDTO> getOffUnitReport(final ReportDTO dto) {
		final String filterString = dto.getFilterString();
		final String filterId = dto.getFilterId();
		final String sortId = dto.getSortId();

		final String baseHql = "select " +
				"bv.id, bv.scheduledStartTime, bv.scheduledEndTime, st.localId, st.irb, " + // five columns
				"vt.name, s.firstName, s.middleName, s.lastName, sm.mrn, " +
				"s.gender, s.birthdate, r.name, br.scheduledStartTime, br.scheduledEndTime, " +
				"vt.visitType, vt.comment, bv.appointmentStatus " +
				"from BookedVisit bv, Study st, VisitTemplate vt, SubjectMrn sm, " +
				"Subject s, BookedResource br, Resource r " +
				"where bv.appointmentStatus.isActive = TRUE AND bv.visitTemplate = vt.id and " +
				"br.bookedVisit = bv.id and bv.study = st.id and br.resource = r.id " +
				"and bv.subjectMrn = sm.id and sm.subject = s.id and s.archivalStatus IS NULL " +
                "and (" +
                "   (vt.visitType.name in (" +
                "       'Outpatient Non CRC'," +
                "       'Outpatient Off Institution'," +
                "       'Inpatient Non CRC'," +
                "       'Inpatient Off Institution'" +
                "       )) " +
                "   OR (vt.sublocation.name LIKE '%Non CRC%') " +
                "   OR (vt.sublocation.name LIKE '%Off Institution%') " +
                "   OR (vt.nonInstitutionNonCRC is TRUE) " +
                "   OR (vt.institutionNonCRC is TRUE)) " +
                "and " +
                "((:startTime between br.scheduledStartTime and br.scheduledEndTime) " +
				" or (:endTime between br.scheduledStartTime and br.scheduledEndTime) " +
				" or (br.scheduledStartTime >= :startTime and br.scheduledEndTime <= :endTime))";

		final String filterClause = setOffUnitReportFilterClause(filterString, filterId);

		final String sortByColumnName = "bv.name, bv.id";
		final SortStrategy sortStrategy = SortStrategy.fromIdString(sortId).orElse(ASCENDING);
		final String orderByClause = setOrderByClause(sortByColumnName, sortStrategy);

		final Query query = session().createQuery(baseHql + filterClause + orderByClause);

		setStartAndEndTimeParameters(dto, query);

		@SuppressWarnings("unchecked")
		final List<Object[]> resultRows = query.list();
		logHqlQuery(resultRows, query);

		final List<OffUnitReportDTO> offUnitReportDTOs = Lists.newArrayList();
		final List<OffUnitReportDTO> inpatientList = Lists.newArrayList();
		final List<OffUnitReportDTO> outpatientList = Lists.newArrayList();

		for (final Object[] row : resultRows) {
			final OffUnitReportDTO newObj = new OffUnitReportDTO();
			newObj.setBookedVisitId((Integer) row[0]);
			newObj.setScheduledStartTime((Date) row[1]);
			newObj.setScheduledEndTime((Date) row[2]);
			newObj.setLocalId((String) row[3]);
			newObj.setIrb((String) row[4]);
			newObj.setVisitName((String) row[5]);
			newObj.setFirstName(SubjectDataEncryptor.decrypt((String) row[6]));
			newObj.setMiddleName(SubjectDataEncryptor.decrypt((String) row[7]));
			newObj.setLastName(SubjectDataEncryptor.decrypt((String) row[8]));
			newObj.setMrn(SubjectDataEncryptor.decrypt((String) row[9]));
			newObj.setGenderName(((Gender) row[10]).getName());
			newObj.setBirthdate((Date) row[11]);
			newObj.setResourceName((String) row[12]);
			newObj.setResourceStartTime((Date) row[13]);
			newObj.setResourceEndTime((Date) row[14]);

			VisitType visitType = ((VisitType) row[15]);

			newObj.setVisitType(visitType.getName());
			newObj.setComment((String) row[16]);
			newObj.setVisitStatus(((AppointmentStatus) row[17]).getName());
			if (visitType.isOutpatient()) {
				outpatientList.add(newObj);
			} else {
				inpatientList.add(newObj);
			}
		}

		final Comparator<OffUnitReportDTO> ordering;

		if (sortStrategy == DESCENDING) {
			ordering = OffUnitComparatorDesc;
		} else {
			ordering = OffUnitComparator;
		}

		Collections.sort(inpatientList, ordering);
		Collections.sort(outpatientList, ordering);
		offUnitReportDTOs.addAll(inpatientList);
		offUnitReportDTOs.addAll(outpatientList);

		dto.setOffUnitReport(offUnitReportDTOs);

		return offUnitReportDTOs;
	}

	private String setOffUnitReportFilterClause(final String filterString, final String filterId) {
		String filterClause;
		if (isNonNullNonEmpty(filterString) && "1".equals(filterId)) {
			filterClause = " and vt.name like '%" + filterString + "%' ";
		} else if (isNonNullNonEmpty(filterString) && "2".equals(filterId)) {
			filterClause = " and st.localId like '%" + filterString + "%' ";
		} else if (isNonNullNonEmpty(filterString) && "3".equals(filterId)) {
			final String encryptedName = SubjectDataEncryptor.encrypt(filterString.toUpperCase());
			filterClause = " and s.lastName = '" + encryptedName + "' ";
		} else if (isNonNullNonEmpty(filterString) && "4".equals(filterId)) {
			filterClause = " and vt.visitType.name like '%" + filterString + "%' ";
		} else if ("5".equals(filterId)) {
			filterClause = " and st.crcFunded = false ";
		} else if ("6".equals(filterId)) {
			filterClause = " and r.resourceType like '%" + "Nursing" + "%' ";
		} else if ("7".equals(filterId)) {
			filterClause = " and r.resourceType like '%" + "Nutrition" + "%' ";
		} else if ("8".equals(filterId)) {
			filterClause = " and r.resourceType like '%" + "Room" + "%' ";
		} else if ("9".equals(filterId)) {
			filterClause = " and r.resourceType like '%" + "Lab" + "%' ";
		} else if ("10".equals(filterId)) {
			filterClause = " and r.resourceType like '%" + "Other" + "%' ";
		} else if (isNonNullNonEmpty(filterString) && "11".equals(filterId)) {
			final String encryptedMrn = SubjectDataEncryptor.encrypt(filterString.toUpperCase());
			filterClause = " and sm.mrn = '" + encryptedMrn + "' ";
		}
		else {
			filterClause = " ";
		}
		return filterClause;
	}

	public List<DailyOverviewReportDTO> getDailyOverviewReport(final ReportDTO dto) {

		final String filterid = dto.getFilterId();
		final String sortid = dto.getSortId();
		final String filterString;

		if ("3".equals(filterid) || "4".equals(filterid)) {
			filterString = SubjectDataEncryptor.encrypt(dto.getFilterString().toUpperCase());
		} else {
			filterString = dto.getFilterString();
		}

    	final String scheduledCheckedInOrHoldApptStatus = "(1,2,5)";
		final String baseHql = "select bv, br "
				+ "from BookedResource br,  BookedVisit bv LEFT JOIN bv.subjectMrn sm LEFT JOIN sm.subject s WITH s.archivalStatus IS NULL"
				+ " where bv.appointmentStatus.isOpen = TRUE and br.bookedVisit = bv.id "
				+ " and ((:startTime between br.scheduledStartTime and br.scheduledEndTime) "
				+ " or (:endTime between br.scheduledStartTime and br.scheduledEndTime) "
				+ " or (br.scheduledStartTime >= :startTime and br.scheduledEndTime <= :endTime))";

		final String filterClause = setDailyOverviewReportFilterClause(filterid, filterString);

		final String orderByClause;

		final Optional<SortStrategy> sortStrategyOption = SortStrategy.fromIdString(sortid);

		if (sortStrategyOption.isPresent()) {
			orderByClause = sortStrategyOption.get().makeHqlOrderBySubClause("bv.id");
		} else {
			orderByClause = " order by bv.id ";
		}

		final Query query = session().createQuery(baseHql + filterClause + orderByClause);

		setStartAndEndTimeParameters(dto, query);

		@SuppressWarnings("unchecked")
		final List<Object[]> resultRows = query.list();
		logHqlQuery(resultRows, query);

		final List<DailyOverviewReportDTO> resultList = Lists.newArrayList();

		final List<DailyOverviewReportDTO> outpatientList = Lists.newArrayList();

		final List<DailyOverviewReportDTO> inpatientList = Lists.newArrayList();

		for (final Object[] row : resultRows) {
			final DailyOverviewReportDTO newObj = new DailyOverviewReportDTO();

			final BookedVisit visit = (BookedVisit) row[0];

			newObj.setBookedVisitId(visit.getId());
			newObj.setScheduledStartTime(visit.getScheduledStartTime());
			newObj.setScheduledEndTime(visit.getScheduledEndTime());
			newObj.setLocalId(visit.getStudy().getLocalId());
			newObj.setIrb(visit.getStudy().getIrb());
			newObj.setVisitName(visit.getVisitTemplate().getName());

      		populateActualOrHoldSubject(newObj, visit);

			final BookedResource resource = (BookedResource) row[1];

			newObj.setResourceName(resource.getResource().getName());
			newObj.setResourceStartTime(resource.getScheduledStartTime());
			newObj.setResourceEndTime(resource.getScheduledEndTime());
			newObj.setVisitType(visit.getVisitType().getName());

			if (visit.getVisitTemplate().getInstitutionNonCRC() || visit.getVisitTemplate().getNonInstitutionNonCRC()) {
				newObj.setOffInstitution("YES");
			} else {
				newObj.setOffInstitution("NO");
			}

			newObj.setComment(visit.getComment());

			newObj.setSublocationName(visit.getVisitTemplate().getSublocation().getName());

			newObj.setVisitStatus(visit.getAppointmentStatus().getName());

			if (isNonNullNonEmpty(filterString) && "5".equals(filterid)) {
				if ("yes".equalsIgnoreCase(filterString) && (visit.getVisitTemplate().getInstitutionNonCRC() || visit.getVisitTemplate().getNonInstitutionNonCRC())) {
					if (visit.getVisitType().isOutpatient()) {
						outpatientList.add(newObj);
					} else {
						inpatientList.add(newObj);
					}
				} else if ("no".equalsIgnoreCase(filterString) && !visit.getVisitTemplate().getInstitutionNonCRC() && !visit.getVisitTemplate().getNonInstitutionNonCRC()) {
					if (visit.getVisitType().isOutpatient()) {
						outpatientList.add(newObj);
					} else {
						inpatientList.add(newObj);
					}
				}
			} else {
				if (visit.getVisitType().isOutpatient()) {
					outpatientList.add(newObj);
				} else {
					inpatientList.add(newObj);
				}
			}
		}

		final SortStrategy sortStrategy = SortStrategy.fromIdString(sortid).orElse(ASCENDING);

		final Comparator<DailyOverviewReportDTO> ordering;

		if (sortStrategy == ASCENDING) {
			ordering = DailyOverviewComparator;
		} else {
			ordering = DailyOverviewComparatorDesc;
		}

		Collections.sort(inpatientList, ordering);
		resultList.addAll(inpatientList);

		Collections.sort(outpatientList, ordering);
		resultList.addAll(outpatientList);

		dto.setDailyOverviewReport(resultList);
		return resultList;
	}

  void populateActualOrHoldSubject(DailyOverviewReportDTO dto, BookedVisit visit) {
    final SubjectMrn decryptedSubjectMrn = visit.getSubjectMrnDecrypted();

    if (decryptedSubjectMrn != null) {
      final Subject decryptedSubject = visit.getSubjectMrnDecrypted().getSubject();

      dto.setFirstName(decryptedSubject.getFirstName());
      dto.setMiddleName(decryptedSubject.getMiddleName());
      dto.setLastName(decryptedSubject.getLastName());
      dto.setMrn(visit.getSubjectMrnDecrypted().getMrn());
      dto.setGenderName(decryptedSubject.getGender().getName());

      final Date subDate = new Date(decryptedSubject.getBirthdate().getTime());
      final String birthDateString = format(monthDayYear(), subDate);
      dto.setBirthdate(birthDateString);
    }
    else {
      dto.setFirstName  ("");
      dto.setMiddleName ("");
      dto.setLastName   (NO_SUBJECT_ASSIGNED);
      dto.setMrn        (NO_SUBJECT_ASSIGNED);
      dto.setGenderName (NO_SUBJECT_ASSIGNED);
      dto.setBirthdate(NO_SUBJECT_ASSIGNED);
    }
  }

	private String setDailyOverviewReportFilterClause(final String filterid, final String filterString) {
		String filterClause;
		if (isNonNullNonEmpty(filterString) && "1".equals(filterid)) {
			filterClause = " and bv.study.localId like '%" + filterString + "%' ";
		} else if (isNonNullNonEmpty(filterString) && "2".equals(filterid)) {
			filterClause = " and bv.visitTemplate.name like '%" + filterString + "%' ";
		} else if (isNonNullNonEmpty(filterString) && "3".equals(filterid)) {
			filterClause = " and (s.lastName like '%" + filterString + "%') ";
		} else if (isNonNullNonEmpty(filterString) && "4".equals(filterid)) {
			filterClause = " and (sm.mrn like '%" + filterString + "%') ";
		} else if (isNonNullNonEmpty(filterString) && "6".equals(filterid)) {
			filterClause = " and br.resource.resourceType like '%" + filterString + "%' ";
		} else if (isNonNullNonEmpty(filterString) && "7".equals(filterid)) {
			filterClause = " and bv.visitTemplate.sublocation.name like '%" + filterString + "%' ";
		} else {
			filterClause = " ";
		}
		return filterClause;
	}

	public List<NursingAndRoomDailyOverviewReportDTO> getExportDailyOverviewReport(final ReportDTO dto) {
		String filterString = dto.getFilterString();
		final String filterid = dto.getFilterId();
		final String sortid = dto.getSortId();

		if (filterid != null && ("3".equals(filterid) || "4".equals(filterid))) {
			filterString = SubjectDataEncryptor.encrypt(dto.getFilterString().toUpperCase());
		}

		String hql = "select " +
				"distinct bv.id, bv.scheduledStartTime, bv.scheduledEndTime, st.localId, st.irb, " + // five columns
				"v.name, s.firstName, s.middleName, s.lastName, sm.mrn, " +
				"s.gender, s.birthdate, r.name, br.scheduledStartTime, br.scheduledEndTime, " +
				"bv.visitTemplate, v.institutionNonCRC, v.nonInstitutionNonCRC, br.id, bv.comment, " +
				"bv.appointmentStatus " +
                "from BookedResource br join br.bookedVisit bv join bv.subjectMrn sm join sm.subject s " +
                "join bv.visitTemplate v join bv.study st join br.resource r " +
                " where bv.appointmentStatus.isActive = TRUE and s.archivalStatus IS NULL and ((:startTime between br.scheduledStartTime and br.scheduledEndTime)" +
                " or (:endTime between br.scheduledStartTime and br.scheduledEndTime) " +
                " or (br.scheduledStartTime >= :startTime and br.scheduledEndTime <= :endTime)) and r.resourceType in ('Room') ";

		if (isNonNullNonEmpty(filterString) && "1".equals(filterid)) {
			hql += " and st.localId like '%" + filterString + "%' ";
		}
		if (isNonNullNonEmpty(filterString) && "2".equals(filterid)) {
			hql += " and v.name like '%" + filterString + "%' ";
		}
		if (isNonNullNonEmpty(filterString) && "3".equals(filterid)) {
			hql += " and s.lastName like '%" + filterString + "%' ";
		}
		if (isNonNullNonEmpty(filterString) && "4".equals(filterid)) {
			hql += " and sm.mrn like '%" + filterString + "%' ";
		}
		if (isNonNullNonEmpty(filterString) && "5".equals(filterid)) {
			hql += " and r.resourceType like '%" + filterString + "%' ";
		}

		hql += " group by br.id ";

		if (sortid != null && sortid.length() > 0 && "1".equalsIgnoreCase(sortid)) {
			hql += " order by bv.scheduledStartTime ASC ";
		}
		if (sortid != null && sortid.length() > 0 && "2".equalsIgnoreCase(sortid)) {
			hql += " order by bv.scheduledStartTime DESC ";
		}
		if (sortid == null) {
			hql += " order by bv.scheduledStartTime ASC ";
		}

		final Query query = session().createQuery(hql);

		setStartAndEndTimeParameters(dto, query);

		@SuppressWarnings("unchecked")
		final List<Object[]> resultRow = query.list();
		logHqlQuery(resultRow, query);

		final List<NursingAndRoomDailyOverviewReportDTO> resultList = Lists.newArrayList();
		final List<NursingAndRoomDailyOverviewReportDTO> outpatientList = Lists.newArrayList();
		for (final Object[] row : resultRow) {
			final NursingAndRoomDailyOverviewReportDTO newObj = new NursingAndRoomDailyOverviewReportDTO();
			newObj.setBookedVisitId((Integer) row[0]);
			newObj.setScheduledStartTime((Date) row[1]);
			newObj.setScheduledEndTime((Date) row[2]);
			newObj.setLocalId((String) row[3]);
			newObj.setIrb((String) row[4]);
			newObj.setVisitName((String) row[5]);
			newObj.setFirstName(SubjectDataEncryptor.decrypt((String) row[6]));
			newObj.setMiddleName(SubjectDataEncryptor.decrypt((String) row[7]));
			newObj.setLastName(SubjectDataEncryptor.decrypt((String) row[8]));
			newObj.setMrn(SubjectDataEncryptor.decrypt((String) row[9]));
			newObj.setGenderName(((Gender) row[10]).getName());

			// TODO: Factor out into a method; this is repeated a few times
			if ((Date) row[11] != null) {
				final Date subDate = new Date(((Date) row[11]).getTime());
				final String subjectDate = format(monthDayYear(), subDate);
				newObj.setBirthdate(subjectDate);
			}
			newObj.setResourceName((String) row[12]);
			newObj.setResourceStartTime((Date) row[13]);
			newObj.setResourceEndTime((Date) row[14]);

			VisitType visitType = ((VisitTemplate) row[15]).getVisitType();
			newObj.setVisitType(visitType.getName());

			// TODO: Factor out into a method; this is repeated a few times
			if ((Boolean) row[16] || (Boolean) row[17]) {
				newObj.setOffInstitution("YES");
			} else {
				newObj.setOffInstitution("NO");
			}

			if (isNonNullNonEmpty(filterString) && "5".equals(filterid)) {
				if ("yes".equalsIgnoreCase(filterString) && ((Boolean) row[16] || (Boolean) row[17])) {
					if (visitType.isOutpatient()) {
						outpatientList.add(newObj);
					} else {
						resultList.add(newObj);

					}

				} else if ("no".equalsIgnoreCase(filterString) && !((Boolean) row[16]) && !((Boolean) row[17])) {
					if (visitType.isOutpatient()) {
						outpatientList.add(newObj);
					} else {
						resultList.add(newObj);

					}

				}
			} else {
				if (visitType.isOutpatient()) {
					outpatientList.add(newObj);
				} else {
					resultList.add(newObj);
				}
			}
			newObj.setComment((String) row[19]);
			newObj.setVisitStatus(((AppointmentStatus) row[20]).getName());
		}

		Collections.sort(outpatientList, DailyOverviewComparator);

		resultList.addAll(outpatientList);

		dto.setNursingAndRoomDailyOverviewReport(resultList);
		return resultList;
	}

	public List<WeeklyPharmReportDTO> getWeeklyPharmReport(final ReportDTO dto) {
		String filterString = dto.getFilterString();
		final String filterid = dto.getFilterId();
		final String sortid = dto.getSortId();

		if ("3".equals(filterid)) {
			filterString = SubjectDataEncryptor.encrypt(dto.getFilterString().toUpperCase());
		}

		String hql = "select st.id, st.name, st.catalystId, st.localId, st.irb, s.firstName, s.middleName, s.lastName, s.gender, sm.mrn, v.name, " +
                "bv.scheduledStartTime, bv.scheduledEndTime, bv.appointmentStatus " +
                "from Subject s, BookedVisit bv, Study st, SubjectMrn sm, VisitTemplate v " +
                "where bv.study = st.id and bv.subjectMrn = sm.id and sm.subject = s.id and bv.visitTemplate = v.id " +
				" and ((:startTime between bv.scheduledStartTime and bv.scheduledEndTime) " +
                " or (:endTime between bv.scheduledStartTime and bv.scheduledEndTime) or " +
				" (bv.scheduledStartTime >= :startTime and bv.scheduledEndTime <= :endTime)) " +
                " and bv.appointmentStatus.isActive = TRUE and v.researchPharmacy = 1 and s.archivalStatus IS NULL ";

		if (isNonNullNonEmpty(filterString) && "1".equals(filterid)) {
			hql += " and st.localId like '%" + filterString + "%' ";
		} else if (isNonNullNonEmpty(filterString) && "2".equals(filterid)) {
			hql += " and st.name like '%" + filterString + "%' ";
		} else if (isNonNullNonEmpty(filterString) && "3".equals(filterid)) {
			hql += " and s.lastName like '%" + filterString + "%' ";
		} else if (isNonNullNonEmpty(filterString) && "4".equals(filterid)) {
			hql += " and bv.scheduledStartTime like :scheduleTime ";
		} else if ("5".equals(filterid)) {
			hql += " and st.crcFunded = false ";
		}

		final String sortByColumnName = "st.name";
		final SortStrategy sortStrategy = SortStrategy.fromIdString(sortid).orElse(ASCENDING);
		final String orderByClause = setOrderByClause(sortByColumnName, sortStrategy);

		final Query query = session().createQuery(hql + orderByClause);

		setStartAndEndTimeParameters(dto, query);
		if (isNonNullNonEmpty(filterString) && "4".equals(filterid)) {
			final Timestamp timestamp = formatFilterByDateTime(filterString);
			query.setParameter("scheduleTime", timestamp);
		}

		@SuppressWarnings("unchecked")
		final List<Object[]> resultRows = query.list();
		logHqlQuery(resultRows, query);

		final List<WeeklyPharmReportDTO> resultList = Lists.newArrayList();

		for (final Object[] row : resultRows) {
			final WeeklyPharmReportDTO newObj = new WeeklyPharmReportDTO();

			newObj.setStudyId((Integer) row[0]);
			newObj.setStudyName((String) row[1]);
			newObj.setCatalystId((String) row[2]);
			newObj.setLocalId((String) row[3]);
			newObj.setIrb((String) row[4]);
			newObj.setFirstName(SubjectDataEncryptor.decrypt((String) row[5]));
			newObj.setMiddleName(SubjectDataEncryptor.decrypt((String) row[6]));
			newObj.setLastName(SubjectDataEncryptor.decrypt((String) row[7]));
			newObj.setGenderName(((Gender) row[8]).getName());
			newObj.setMrn(SubjectDataEncryptor.decrypt((String) row[9]));
			newObj.setVisitName((String) row[10]);
			newObj.setScheduledStartTime((Date) row[11]);
			newObj.setScheduledEndTime((Date) row[12]);
			newObj.setAppointmentStatus(((AppointmentStatus) row[13]).getName());
			resultList.add(newObj);
		}

		final Comparator<WeeklyPharmReportDTO> ordering;

		if (sortStrategy == ASCENDING) {
			ordering = WeeklyPharmComparator;
		} else {
			ordering = WeeklyPharmComparatorDesc;
		}

		Collections.sort(resultList, ordering);

		dto.setWeeklyPharmReport(resultList);
		return resultList;
	}

	public List<StudyVisitLocationReportDTO> getStudyVisitLocationReport(final ReportDTO dto) {
		final String filterString = dto.getFilterString();
		final String filterid = dto.getFilterId();
		final String sortid = dto.getSortId();

		String hql = "select " +
                "bv.id, v.name, sl.name, bv.checkInDate, bv.checkOutDate, " + // five columns
                "s.firstName, s.middleName, s.lastName, sm.mrn, s.gender, " +
                "st.name, st.catalystId, st.localId, st.irb, sl.id, " +
				"v.visitType.name " +
                "from VisitTemplate v, BookedVisit bv, Sublocation sl, Subject s, Study st, SubjectMrn sm " +
                "where bv.visitTemplate = v.id and bv.study = st.id and s.archivalStatus IS NULL and " +
                "bv.subjectMrn = sm.id and sm.subject = s.id and bv.appointmentStatus.isServiced = TRUE and v.sublocation = sl.id " +
                "and ((:startTime between bv.scheduledStartTime and bv.scheduledEndTime) " +
                " or (:endTime between bv.scheduledStartTime and bv.scheduledEndTime) " +
                "or (bv.scheduledStartTime >= :startTime and bv.scheduledEndTime <= :endTime))";

		if (isNonNullNonEmpty(filterString) && "1".equals(filterid)) {
			hql += " and st.localId like '%" + filterString + "%' ";
		} else if (isNonNullNonEmpty(filterString) && "2".equals(filterid)) {
			hql += " and v.name like '%" + filterString + "%' ";
		} else if (isNonNullNonEmpty(filterString) && "3".equals(filterid)) {
			hql += " and sl.name like '%" + filterString + "%' ";
		} else if ("4".equals(filterid)) {
			hql += " and st.crcFunded = false ";
		}

		final String groupByClause = " group by bv.id ";
		hql += groupByClause;

		final String sortByColumnName = "sl.name";
		final SortStrategy sortStrategy = SortStrategy.fromIdString(sortid).orElse(ASCENDING);
		final String orderByClause = setOrderByClause(sortByColumnName, sortStrategy);

		final Query query = session().createQuery(hql + orderByClause);

		setStartAndEndTimeParameters(dto, query);

		@SuppressWarnings("unchecked")
		final List<Object[]> resultRows = query.list();
		logHqlQuery(resultRows, query);

		final List<StudyVisitLocationReportDTO> resultList = Lists.newArrayList();

		for (final Object[] rows : resultRows) {
			final StudyVisitLocationReportDTO newObj = new StudyVisitLocationReportDTO();
			newObj.setVisitTypeId((Integer) rows[0]);
			newObj.setVisitTypeName((String) rows[1]);
			newObj.setSublocationName((String) rows[2]);
			newObj.setCheckInTime((Date) rows[3]);
			newObj.setCheckOutTime((Date) rows[4]);
			newObj.setSubjectFirstName(SubjectDataEncryptor.decrypt((String) rows[5]));
			newObj.setSubjectMiddleName(SubjectDataEncryptor.decrypt((String) rows[6]));
			newObj.setSubjectLastName(SubjectDataEncryptor.decrypt((String) rows[7]));
			newObj.setMrn(SubjectDataEncryptor.decrypt((String) rows[8]));
			newObj.setGenderName(((Gender) rows[9]).getName());
			newObj.setStudyName((String) rows[10]);
			newObj.setCatalystId((String) rows[11]);
			newObj.setLocalId((String) rows[12]);
			newObj.setIrb((String) rows[13]);
			newObj.setVisitId((Integer) rows[14]);
			newObj.setVisitType((String) rows[15]);

			resultList.add(newObj);
		}

		final Comparator<StudyVisitLocationReportDTO> ordering;

		if (sortStrategy == ASCENDING) {
			ordering = StudyVisitLocationComparatorAsc;
		} else {
			ordering = StudyVisitLocationComparatorDesc;
		}

		Collections.sort(resultList, ordering);

		dto.setStudyVisitLocationReport(resultList);
		return resultList;
	}

	public List<BillableResourcesReportDTO> getBillableResourcesReport(final ReportDTO dto) {

		final String filterid = dto.getFilterId();
		final String sortid = dto.getSortId();

		final String filterString;

		if ("5".equals(filterid)) {
			if ("yes".equalsIgnoreCase(dto.getFilterString())) {
				filterString = "1";
			} else if ("no".equalsIgnoreCase(dto.getFilterString())) {
				filterString = "0";
			} else {
				filterString = "";
			}
		} else {
			filterString = dto.getFilterString();
		}

		final String baseSql = "select " +
                "r.id rid, r.name rname, bv.check_in_date, bv.check_out_date, s.first_name sfirst, " +
                "s.middle_name smiddle, s.last_name slast, sm.mrn, st.industry_initiated, sl.name slname, " +
                "st.name stname, st.catalyst_id, st.local_id, st.irb, pi.first_name pfirst, " +
                "pi.middle_name pmiddle, pi.last_name plast, vt.name vname, vty.name, br.scheduled_start_time, " +
                "br.scheduled_end_time, st.crc_funded, bv.comment, " +

                "group_concat(lla.name separator '; ') nameC, " +
                "group_concat(obra.comment separator '; ') commentC, " +
                "group_concat(cast(obra.quantity as char) separator '; ') quantityC " +

                "from " +
                "booked_resource br " +
                "   LEFT JOIN template_resource tr ON br.template_resource = tr.id and tr.billable = true " +
                "   LEFT JOIN override_booked_resource_annotations obra ON obra.booked_resource = br.id " +
                "   LEFT JOIN line_level_annotations lla ON obra.line_level_annotations = lla.id, " +

                "resource r, visit_template vt, booked_visit bv, sublocation sl, subject s, " +
                "user pi, study st, subject_mrn sm, visit_type vty " +

                "where " +
				"br.billable = true and " +
				"bv.visit_template = vt.id and " +
				"vt.visit_type = vty.id and " +
                "s.archival_status is NULL and " +
                "bv.study = st.id and " +
                "st.principal_investigator = pi.id and " +
                "bv.subject_mrn = sm.id and " +
                "sm.subject = s.id and " +
                "vt.sublocation = sl.id and " +
                "bv.appointment_status = 3 and " +
                "br.booked_visit = bv.id and " +
                "br.resource = r.id " +
                "and ((:startTime between br.scheduled_start_time and br.scheduled_end_time) " +
                "or (:endTime between br.scheduled_start_time and br.scheduled_end_time) " +
                "or (br.scheduled_start_time >= :startTime and br.scheduled_end_time <= :endTime)) ";

		final Map<String, String> filterIdsToHqlColumns = Pairs.toMap(
                pair("1", "r.name"),
                pair("2", "st.name"),
                pair("3", "st.local_id"),
                pair("4", "pi.last_name"));
        
        final boolean filterIdIsPresent = isNonNullNonEmpty(filterString);
		
        final String filterClause;
        
        if(filterIdIsPresent && filterIdsToHqlColumns.containsKey(filterid)) {
            final String filterColumn = filterIdsToHqlColumns.get(filterid);
            
            filterClause = " and " + filterColumn + " like '%" + filterString + "%' ";
        } else if ("5".equals(filterid)) {
            filterClause = " and st.industry_initiated = true ";
        } else if ("6".equals(filterid)) {
            filterClause = " and st.crc_funded = false ";
        } else {
            filterClause = " ";
        }
        
		final String sortByColumnName = "r.name";
		final SortStrategy sortStrategy = SortStrategy.fromIdString(sortid).orElse(ASCENDING);
		final String orderByClause = setOrderByClause(sortByColumnName, sortStrategy);
        final String groupByClause = " group by br.id ";

		final String infiniteSql = baseSql + filterClause + groupByClause + orderByClause;

		final int limitChunk = 10000;
		int offset = 0;
		boolean maybeMore = true;

		final List<BillableResourcesReportDTO> resultList = Lists.newArrayList();

		while (maybeMore) {
			String sql = infiniteSql + " limit " + limitChunk + " offset " + offset;

			final NativeQuery query = session().createNativeQuery(sql);

			setStartAndEndTimeParameters(dto, query);

			@SuppressWarnings("unchecked")
			final List<Object[]> resultRows = query.list();
			logHqlQuery(resultRows, query);

			if (resultRows.size() < limitChunk) {
				maybeMore = false;
			}

			for (final Object[] row : resultRows) {
				int index = 0;

				final BillableResourcesReportDTO newObj = new BillableResourcesReportDTO();
				newObj.setResourceId((Integer) row[index++]);
				newObj.setResourceName((String) row[index++]);
				newObj.setCheckInTime((Date) row[index++]);
				newObj.setCheckOutTime((Date) row[index++]);
				newObj.setSubjectFirstName(SubjectDataEncryptor.decrypt((String) row[index++]));
				newObj.setSubjectMiddleName(SubjectDataEncryptor.decrypt((String) row[index++]));
				newObj.setSubjectLastName(SubjectDataEncryptor.decrypt((String) row[index++]));
				newObj.setMrn(SubjectDataEncryptor.decrypt((String) row[index++]));
				newObj.setIndustryInitiated((Boolean) row[index++]);
				newObj.setSublocationName((String) row[index++]);
				newObj.setStudyName((String) row[index++]);
				newObj.setCatalystId((String) row[index++]);
				newObj.setLocalId((String) row[index++]);
				newObj.setIrb((String) row[index++]);
				newObj.setPiFirstName((String) row[index++]);
				newObj.setPiMiddleName((String) row[index++]);
				newObj.setPiLastName((String) row[index++]);
				newObj.setVisitName((String) row[index++]);
				newObj.setVisitTypeName((String) row[index++]);
				newObj.setScheduledStartTime((Date) row[index++]);
				newObj.setScheduledEndTime((Date) row[index++]);
				newObj.setCrcFunded((Boolean) row[index++]);
				newObj.setBookedVisitComment((String) row[index++]);

				newObj.setResourceActivities(((String) row[index++]));
				newObj.setResourceActivitiesComment(((String) row[index++]));
				newObj.setResourceActivitiesQuantity(((String) row[index++]));
				resultList.add(newObj);
			}
			offset += limitChunk;
		}

		final Comparator<BillableResourcesReportDTO> ordering;

		if (sortStrategy == ASCENDING) {
			ordering = BillableResourcesComparator;
		} else {
			ordering = BillableResourcesComparatorDesc;
		}

		Collections.sort(resultList, ordering);


		dto.setBillableResourcesReport(resultList);
		return resultList;
	}

	public List<TransactionsReportDTO> getTransactionsReport(final ReportDTO dto) {
		final String filterString = dto.getFilterString();
		final String filterid = dto.getFilterId();
		final String sortid = dto.getSortId();

		String alQueryString = "select bv, al.performingUser, al.date " +
				"   from BookedVisit bv, ActivityLog al " +
				" where al.bookedVisit = bv.id and al.appointmentOverrideReason IS NULL " +
				" and bv.subjectMrn IS NOT null " +
				"and al.date >= :startTime and al.date <= :endTime ";

		if (isNonNullNonEmpty(filterString) && "1".equals(filterid)) {
			alQueryString += " and bv.appointmentStatus.name like '%" + filterString + "%' ";
		} else if (isNonNullNonEmpty(filterString) && "2".equals(filterid)) {
			alQueryString += " and bv.cancelStatus.name like '%" + filterString + "%' ";
		} else if (isNonNullNonEmpty(filterString) && "3".equals(filterid)) {
			alQueryString += " and bv.cancelStatusReason.name like '%" + filterString + "%' ";
		} else if (isNonNullNonEmpty(filterString) && "4".equals(filterid)) {
			alQueryString += " and al.performingUser.lastName like '%" + filterString + "%' ";
		}

		final List<TransactionsReportDTO> resultList = Lists.newArrayList();

		addTransactionsDtoList(alQueryString, dto, resultList);

		String bvalQueryString = alQueryString.replaceAll("ActivityLog al", "BookedVisitActivityLog al") +
				" and al.actionPerformed NOT LIKE 'Viewed%' and al.actionPerformed NOT LIKE 'Clicked%'";
		addTransactionsDtoList(bvalQueryString, dto, resultList);

		final Comparator<TransactionsReportDTO> ordering;
		final SortStrategy sortStrategy = SortStrategy.fromIdString(sortid).orElse(ASCENDING);

		if (sortStrategy == ASCENDING) {
			ordering = TransactionsComparator;
		} else {
			ordering = TransactionsComparatorDesc;
		}

		Collections.sort(resultList, ordering);

		dto.setTransactionsReport(resultList);
		return resultList;
	}

	public void addTransactionsDtoList(final String queryString,
									   final ReportDTO dto,
									   final List<TransactionsReportDTO> resultList) {
		final Query query = session().createQuery(queryString);

		setStartAndEndTimeParameters(dto, query);

		@SuppressWarnings("unchecked")
		final List<Object[]> resultRows = query.list();
		logSqlQuery(resultRows, query);

		for (final Object[] row : resultRows) {
			final TransactionsReportDTO newObj = makeTransactionsReportRowFromQueryResult(row);
			resultList.add(newObj);
		}
	}

	// factored out of getTransactionsReport() for testability
	public TransactionsReportDTO makeTransactionsReportRowFromQueryResult(Object[] row) {

		// !! many of these values should be per change as gleaned from activity-log,
		//     rather than from latest state of the booked-visit

		final TransactionsReportDTO newObj = new TransactionsReportDTO();
		BookedVisit bookedVisit = (BookedVisit) row[0];
		newObj.setAsrId(bookedVisit.getId());
		newObj.setAppointmentStatus(bookedVisit.getAppointmentStatus().getName());

		newObj.setAsrName("");
		if (bookedVisit.getAppointmentStatusReason() != null) {
			newObj.setAsrName(bookedVisit.getAppointmentStatusReason().getName());
		}

		newObj.setCancelStatus("");
		if (bookedVisit.getCancelStatus() != null) {
			newObj.setCancelStatus(bookedVisit.getCancelStatus().getName());
		}

		newObj.setCancelStatusReason("");
		if (bookedVisit.getCancelStatusReason() != null) {
			newObj.setCancelStatusReason(bookedVisit.getCancelStatusReason().getName());
		}

		newObj.setScheduledStartTime(bookedVisit.getScheduledStartTime());
		newObj.setScheduledEndTime(bookedVisit.getScheduledEndTime());

		final String name = ((User) row[1]).getEcommonsId() + " - " + ((User) row[1]).getLastName();

		newObj.setEcommonsId(name);
		newObj.setCancelTime((Date) row[2]);
		newObj.setVisitName(bookedVisit.getVisitTemplate().getName());
		newObj.setVisitTypeName(bookedVisit.getVisitType().getName());

		final Study study = bookedVisit.getStudy();

		newObj.setStudyName(study.getName());
		newObj.setCatalystId("");
		if (study.getCatalystId() != null) {
			newObj.setCatalystId(study.getCatalystId());
		}

		newObj.setLocalId(study.getLocalId());

		newObj.setIrb("");
		if (study.getIrb() != null) {
			newObj.setIrb(study.getIrb());
		}

		final User investigator = study.getInvestigator();
		newObj.setPiFirstName("");
		newObj.setPiMiddleName("");
		newObj.setPiLastName("");

		if (investigator != null) {
			newObj.setPiFirstName(investigator.getFirstName());
			newObj.setPiMiddleName(investigator.getMiddleName());
			newObj.setPiLastName(investigator.getLastName());
		}

		final User scheduler = study.getScheduler();
		newObj.setPsFirstName("");
		newObj.setPsMiddleName("");
		newObj.setPsLastName("");

		if (scheduler != null) {
			newObj.setPsFirstName(scheduler.getFirstName());
			newObj.setPsMiddleName(scheduler.getMiddleName());
			newObj.setPsLastName(scheduler.getLastName());
		}

		SubjectMrn subjectMrn = bookedVisit.getSubjectMrnDecrypted();

		if (subjectMrn == null) {
			newObj.setSubjectFirstName("");
			newObj.setSubjectMiddleName("");
			newObj.setSubjectLastName(NO_SUBJECT_ASSIGNED);
			newObj.setMrn(NA);
		}
		else {
			Subject subject = subjectMrn.getSubject();
			newObj.setSubjectFirstName(subject.getFirstName());
			newObj.setSubjectMiddleName(subject.getMiddleName());
			newObj.setSubjectLastName(subject.getLastName());
			newObj.setMrn(subjectMrn.getMrn());
		}
		return newObj;
	}

	public List<DailyResourceReportDTO> getDailyResourceReport(final ReportDTO dto, final boolean export) {
		final String filterid = dto.getFilterId();
		final String sortid = dto.getSortId();

		final boolean searchByLastName = "2".equals(filterid);
		
		final String filterString = getFilterString(dto, searchByLastName);
        
		final String baseHql = dailyResourceReportBaseHql(export);

		final String searchClause = makeDailyResourceSearchClause(filterid, filterString, searchByLastName);

		final String sortByColumnName = "r.name";
		final SortStrategy sortStrategy = SortStrategy.fromIdString(sortid).orElse(ASCENDING);
		final String orderByClause = setOrderByClause(sortByColumnName, sortStrategy);

		final String hql = baseHql + " " + searchClause + " " + orderByClause;
		
		final Query query = newQuery(hql);

		setStartAndEndTimeParameters(dto, query);

		@SuppressWarnings("unchecked")
		final List<Object[]> resultRows = query.list();
		
		logHqlQuery(resultRows, query);

		 final Function<Object[], DailyResourceReportDTO> toDailyResourceReportDTO = row -> {
		    final DailyResourceReportDTO newObj = new DailyResourceReportDTO();
            newObj.setResourceTypeId((Integer) row[0]);
            newObj.setResourceTypeName(((ResourceType) row[1]).getName());
            newObj.setResourceName((String) row[2]);

			BookedVisit bookedVisit = (BookedVisit) row[3];

			SubjectMrn subjectMrn = bookedVisit.getSubjectMrnDecrypted();
            newObj.setSubjectFirstName(subjectMrn == null ? "" : subjectMrn.getSubject().getFirstName());
            newObj.setSubjectMiddleName(subjectMrn == null ? "" : subjectMrn.getSubject().getMiddleName());
            newObj.setSubjectLastName(subjectMrn == null ? NO_SUBJECT_ASSIGNED : subjectMrn.getSubject().getLastName());
            newObj.setMrn(subjectMrn == null ? NO_SUBJECT_ASSIGNED : subjectMrn.getMrn());
            newObj.setLocalId((String) row[4]);
            newObj.setIrb((String) row[5]);
            newObj.setVisitName((String) row[6]);
            newObj.setScheduledStartTime((Date) row[7]);
            newObj.setScheduledEndTime((Date) row[8]);
            newObj.setComment((String) row[9]);
            newObj.setVisitId((Integer) row[10]);

            return newObj;
		};
		
		List<DailyResourceReportDTO> resultList = enrich(resultRows).map(toDailyResourceReportDTO).toList();

		if (export) {
			resultList = resultList.stream()
					.map(drrd -> new NutritionDailyResourceReportDTO(drrd))
					.collect(Collectors.toList());
		}
		
		final Comparator<DailyResourceReportDTO> ordering = sortStrategy.isAscending() ? DailyResourceComparator : DailyResourceComparatorDesc;

		Collections.sort(resultList, ordering);

		dto.setDailyResourceReport(resultList);
		
		return resultList;
	}

    private String getFilterString(final ReportDTO dto, final boolean searchByLastName) {
		if (searchByLastName) {
			return SubjectDataEncryptor.encrypt(dto.getFilterString().toUpperCase());
		} 
		
		return dto.getFilterString();
    }

    String dailyResourceReportBaseHql(final boolean export) {

      String desiredStatus = export ?
			  " and bv.appointmentStatus.isHold = FALSE" :
			  " and bv.appointmentStatus.isCancelled = FALSE";

      String restrictionExpression = " and r.resourceType = 'Nutrition' and " +
			  "r.name NOT LIKE '%Meal,%' and " +
			  "r.name NOT LIKE '%Snack%'" +
			  "";

      String ridRestriction = export ? restrictionExpression : "" ;

      String result =
          "select r.id, r.resourceType, r.name, bv, st.localId, st.irb, v.name, " +
				  "br.scheduledStartTime, br.scheduledEndTime, bv.comment, bv.id " +
          "from Study st, VisitTemplate v, BookedResource br, " +
          " Resource r, " +
          " BookedVisit bv left join bv.subjectMrn sm left join sm.subject s with s.archivalStatus IS NULL " +
          "where bv.study = st.id and bv.visitTemplate = v.id " +
          " and br.bookedVisit = bv.id and br.resource = r.id " +
          desiredStatus +
          " and " +
          "    ((:startTime between br.scheduledStartTime and br.scheduledEndTime) " +
          "  or (:endTime between br.scheduledStartTime and br.scheduledEndTime) " +
          "  or (br.scheduledStartTime >= :startTime " +
          " and br.scheduledEndTime <= :endTime))" +
          ridRestriction;

      return result;
    }

    private String makeDailyResourceSearchClause(final String filterid, final String filterString, final boolean searchByLastName) {
        final boolean filterStringIsPresent = isNonNullNonEmpty(filterString);
        
        final Map<String, ResourceType> filterIdToResourceType = toMap(
                pair("3", ResourceType.Nursing),
                pair("4", ResourceType.Nutrition),
                pair("5", ResourceType.Room),
                pair("6", ResourceType.Lab),
                pair("7", ResourceType.Other));
        
        final boolean searchingByResourceType = filterIdToResourceType.containsKey(filterid);
		
        if (filterStringIsPresent && "1".equals(filterid)) {
            return " and r.name like '%" + filterString + "%' ";
		    }
        
        if (filterStringIsPresent && searchByLastName) {
          return " and s.lastName like '%" + filterString + "%' ";
		    }
        
        if (searchingByResourceType) {
            final String toSearchFor = filterIdToResourceType.get(filterid).getName().toLowerCase();
            //TODO: Skip the like and look for a specific ResourceType
            return " and r.resourceType like '%" + toSearchFor + "%' ";
        } 
        
        if ("8".equals(filterid)) {
            return " and st.crcFunded = false ";
        } 

        return " ";
	}

  	public List<DailyAdmReportDTO> getDailyAdmReport(final ReportDTO dto) {

		final String filterid = dto.getFilterId();
		final String sortid = dto.getSortId();

		final String filterString;

		if ("1".equals(filterid) || "3".equals(filterid)) {
			filterString = SubjectDataEncryptor.encrypt(dto.getFilterString().toUpperCase());
		} else {
			filterString = dto.getFilterString();
		}

		String hql = "select bv.id, s.firstName, s.middleName, s.lastName, s.birthdate, sm.mrn, s.gender, st.localId, st.irb, v, " +
                "r.name, br.scheduledStartTime, br.scheduledEndTime, bv.comment, bv.checkInDate, bv.appointmentStatus " +
                "from Subject s, Study st, VisitTemplate v, Resource r, BookedResource br, BookedVisit bv, SubjectMrn sm " +
                "where bv.subjectMrn = sm.id and sm.subject = s.id and br.bookedVisit = bv.id " +
				" and br.resource = r.id and bv.study = st.id and bv.visitTemplate = v.id " +
				" and s.archivalStatus IS NULL and " +
                " bv.appointmentStatus.isActive = TRUE and ((:startTime between br.scheduledStartTime and br.scheduledEndTime) " +
                " or (:endTime between br.scheduledStartTime and br.scheduledEndTime) " +
				" or (br.scheduledStartTime >= :startTime and br.scheduledEndTime <= :endTime))";

		if (isNonNullNonEmpty(filterString) && "1".equals(filterid)) {
			hql += " and s.lastName like '%" + filterString + "%' ";
		} else if (isNonNullNonEmpty(filterString) && "2".equals(filterid)) {
			hql += " and st.localId like '%" + filterString + "%' ";
		} else if (isNonNullNonEmpty(filterString) && "3".equals(filterid)) {
			hql += " and sm.mrn like '%" + filterString + "%' ";
		} else if (isNonNullNonEmpty(filterString) && "4".equals(filterid)) {
			hql += " and r.name like '%" + filterString + "%' ";
		} else if ("5".equals(filterid)) {
			hql += " and st.crcFunded = false ";
		}

		hql += " order by bv.id ";

		final Query query = session().createQuery(hql);

		setStartAndEndTimeParameters(dto, query);

		@SuppressWarnings("unchecked")
		final List<Object[]> resultRows = query.list();
		logHqlQuery(resultRows, query);

		final List<DailyAdmReportDTO> resultList = Lists.newArrayList();
		final List<DailyAdmReportDTO> inpatientList = Lists.newArrayList();
		final List<DailyAdmReportDTO> outpatientList = Lists.newArrayList();

		for (final Object[] row : resultRows) {
			final DailyAdmReportDTO newObj = new DailyAdmReportDTO();

			// !! should call it visitId, not subjectId
			newObj.setSubjectId((Integer) row[0]);
			newObj.setSubjectFirstName(SubjectDataEncryptor.decrypt((String) row[1]));
			newObj.setSubjectMiddleName(SubjectDataEncryptor.decrypt((String) row[2]));
			newObj.setSubjectLastName(SubjectDataEncryptor.decrypt((String) row[3]));

			if ((Date) row[4] != null) {
				final Date subDate = new Date(((Date) row[4]).getTime());
				final String subjectdate = format(monthDayYear(), subDate);
				newObj.setBirthdate(subjectdate);
			} else {
				newObj.setBirthdate(null);
			}

			newObj.setMrn(SubjectDataEncryptor.decrypt((String) row[5]));
			newObj.setGenderName(((Gender) row[6]).getName());
			newObj.setLocalId((String) row[7]);
			newObj.setIrb((String) row[8]);
			final VisitTemplate visit = (VisitTemplate) row[9];
			newObj.setVisitName(visit.getName());
			newObj.setResourceName((String) row[10]);
			newObj.setScheduledStartTime((Date) row[11]);
			newObj.setScheduledEndTime((Date) row[12]);
			newObj.setComment((String) row[13]);
			newObj.setCheckInTime((Date) row[14]);
			newObj.setVisitStatus(((AppointmentStatus) row[15]).getName());

			if (visit.getVisitType().isOutpatient()) {
				outpatientList.add(newObj);
			} else {
				inpatientList.add(newObj);
			}
		}

		final SortStrategy sortStrategy = SortStrategy.fromIdString(sortid).orElse(ASCENDING);

		final Comparator<DailyAdmReportDTO> ordering;

		final Comparator<DailyAdmReportDTO> orderingByTime = DailyAdmNameComparatorOrderingByTime;

		if (sortStrategy == ASCENDING) {
			ordering = DailyAdmNameComparator;
		} else {
			ordering = DailyAdmNameComparatorDesc;
		}


		// !! The second application of sort() moots the first one
		Collections.sort(inpatientList, ordering);
		Collections.sort(inpatientList, orderingByTime);
		resultList.addAll(inpatientList);

		Collections.sort(outpatientList, ordering);
		Collections.sort(outpatientList, orderingByTime);
		resultList.addAll(outpatientList);

		dto.setDailyAdmReport(resultList);
		return resultList;
	}

	public List<BookedResource> findBookedResourceMeal(final BookedVisit bookedVisit) {
		final String findBookedVisit = "SELECT a FROM BookedResource a WHERE" + " a.bookedVisit = :bookedVisit and a.resource.resourceType = 'Nutrition' order by a.scheduledStartTime";

		final Query query = session().createQuery(findBookedVisit);

		query.setParameter("bookedVisit", bookedVisit);

		@SuppressWarnings("unchecked")
		final List<BookedResource> list = query.list();
		logHqlQuery(list, query);

		final List<BookedResource> bookedMealResources = filterMeals(list);

		return bookedMealResources;
	}

	public List<BookedResource> findBookedResourceRooms(final BookedVisit bookedVisit) {
		final String findBookedVisit = "SELECT a FROM BookedResource a WHERE" + " a.bookedVisit = :bookedVisit and a.resource.resourceType = 'Room' order by a.scheduledStartTime";

		final Query query = session().createQuery(findBookedVisit);

		query.setParameter("bookedVisit", bookedVisit);

		@SuppressWarnings("unchecked")
		final List<BookedResource> bookedRoomResources = query.list();
		logHqlQuery(bookedRoomResources, query);

		return bookedRoomResources;
	}

	public List<BookedResource> filterMeals(final List<BookedResource> bookedResources) {
		return enrich(bookedResources).filter(this::hasAllowedMealName).toList();
	}

	private Function<Predicate<BookedResource>, List<BookedResource>> applyTo(final List<BookedResource> bookedResources) {
		return p -> enrich(bookedResources).filter(p).toList();
	}

	public List<MetaKitchenReportDTO> getMetaKitchenReport(final ReportDTO reportDto) {

		final String filterid = reportDto.getFilterId();
		final String sortid = reportDto.getSortId();
		final String filterString;

		if ("1".equals(filterid) || "3".equals(filterid)) {
			filterString = SubjectDataEncryptor.encrypt(reportDto.getFilterString().toUpperCase());
		} else {
			filterString = reportDto.getFilterString();
		}

		final String findBookedResources = "SELECT b FROM BookedVisit a, BookedResource b, Resource r "
				+ " WHERE a.id = b.bookedVisit and b.resource = r.id and r.resourceType IN ('Nutrition', 'Room') "
				+ " and a.appointmentStatus.isActive = TRUE " + " and ((:startTime between b.scheduledStartTime and b.scheduledEndTime) "
				+ " or (:endTime between b.scheduledStartTime and b.scheduledEndTime) "
				+ " or (b.scheduledStartTime >= :startTime and b.scheduledEndTime <= :endTime)) order by b.bookedVisit ASC";

		final Query query = session().createQuery(findBookedResources);

		setStartAndEndTimeParameters(reportDto, query);

		@SuppressWarnings("unchecked")
		final List<BookedResource> bookedResources = query.list();
		logHqlQuery(bookedResources, query);

		final Optional<Predicate<BookedResource>> predicate;

		if (isNonNullNonEmpty(filterString) && "1".equals(filterid)) {
			predicate = Optional.of(lastNameMatches(filterString));
		} else if (isNonNullNonEmpty(filterString) && "2".equals(filterid)) {
			predicate = Optional.of(studyLocalIdMatches(filterString));
		} else if (isNonNullNonEmpty(filterString) && "3".equals(filterid)) {
			predicate = Optional.of(mrnMatches(filterString));
		} else if (isNonNullNonEmpty(filterString) && "4".equals(filterid)) {
			predicate = Optional.of(nameMatches(filterString));
		} else if ("5".equals(filterid)) {
			predicate = Optional.of(this::notCrcFunded);
		} else {
			predicate = Optional.empty();
		}

		final Collection<BookedResource> filteredBookedResources = predicate.map(applyTo(bookedResources)).orElse(Collections.emptyList());

		final List<BookedResource> finalList = Lists.newArrayList();

		if (filterString == null || filterString.equalsIgnoreCase("")) {
			finalList.addAll(bookedResources);
		} else {
			finalList.addAll(filteredBookedResources);
		}

		final List<MetaKitchenReportDTO> resultList = generateMetaKitchenReports(finalList);

		final SortStrategy sortStrategy = SortStrategy.fromIdString(sortid).orElse(ASCENDING);

		Comparator<MetaKitchenReportDTO> ordering = null;

		if (sortStrategy == ASCENDING) {
			if (reportDto.getName().equalsIgnoreCase("meta_kitchen_by_time")){
				ordering = MetaKitchenByTimeComparator;
			}
			else if (reportDto.getName().equalsIgnoreCase("meta_kitchen")){
				ordering = MetaKitchenNameComparator;
			}
			else if (reportDto.getName().equalsIgnoreCase("meta_kitchen_flat")){
				ordering = MetaKitchenNameComparator;
			}
		} else {
			if (reportDto.getName().equalsIgnoreCase("meta_kitchen_by_time")){
				ordering = MetaKitchenByTimeComparatorDesc;
			} else if (reportDto.getName().equalsIgnoreCase("meta_kitchen")){
				ordering = MetaKitchenNameComparatorDesc;
			} else if (reportDto.getName().equalsIgnoreCase("meta_kitchen_flat")){
				ordering = MetaKitchenNameComparatorDesc;
			}
		}

		Collections.sort(resultList, ordering);

		reportDto.setMetaKitchenReport(resultList);

		return resultList;
	}

	private boolean notCrcFunded(final BookedResource bookedResource) {
		return bookedResource.getBookedVisit().getStudy().getCrcFunded().equals(Boolean.FALSE);
	}

	private Predicate<BookedResource> nameMatches(final String filterString) {
		return bookedResource -> bookedResource.getResource().getName().equalsIgnoreCase(filterString);
	}

	private Predicate<BookedResource> mrnMatches(final String filterString) {
		return bookedResource -> bookedResource.getBookedVisit().getSubjectMrn().getMrn().equalsIgnoreCase(filterString);
	}

	private Predicate<BookedResource> studyLocalIdMatches(final String filterString) {
		return bookedResource -> bookedResource.getBookedVisit().getStudy().getLocalId().equalsIgnoreCase(filterString);
	}

	private Predicate<BookedResource> lastNameMatches(final String filterString) {
		return bookedResource -> bookedResource.getBookedVisit().getSubjectMrn().getSubject().getLastName().equalsIgnoreCase(filterString);
	}

	// TODO: TEST
	boolean hasAllowedMealName(final BookedResource bookedResource) {
		return bookedResource.getResource().getName().startsWith("Snack") || bookedResource.getResource().getName().startsWith("Meal");
	}

	// TODO: TEST
	// TODO: Is this name right???
	boolean mealTimeOverlapsRoomTime(final Date mealStartdate, final Date mealEnddate, final Date roomStartdate, final Date roomEnddate) {
		return mealStartdate.after(roomStartdate) && mealStartdate.before(roomEnddate) || mealStartdate.equals(roomStartdate) && mealStartdate.equals(roomEnddate) || mealStartdate.before(roomStartdate) && mealEnddate.after(roomStartdate) || mealStartdate.after(roomStartdate) && mealStartdate.before(roomEnddate) && mealEnddate.after(roomEnddate) || mealStartdate.equals(roomStartdate) && mealEnddate.before(roomEnddate) || mealStartdate.after(roomStartdate) && mealEnddate.equals(roomEnddate);
	}

	private List<MetaKitchenReportDTO> generateMetaKitchenReports(final List<BookedResource> finalList) {
		final List<MetaKitchenReportDTO> resultList = Lists.newArrayList();
		for (final BookedResource bookedResource : finalList) {

			final MetaKitchenReportDTO metaKitchenReportDto = new MetaKitchenReportDTO();

			SubjectMrn subjectMrn = bookedResource.getBookedVisit().getSubjectMrnDecrypted();

			String lastName = NO_SUBJECT_ASSIGNED;
			String firstName = NO_SUBJECT_ASSIGNED;
			String middleName = NO_SUBJECT_ASSIGNED;
			String subjectMrnVal = NA;
			String subjectBirthDate = NA;

			if (subjectMrn != null) {
				lastName = subjectMrn.getSubject().getLastName();
				firstName = subjectMrn.getSubject().getFirstName();
				middleName = subjectMrn.getSubject().getMiddleName();
				subjectMrnVal = subjectMrn.getMrn();

				final Date subjectDate = new Date(subjectMrn.getSubject().getBirthdate().getTime());
				subjectBirthDate = format(monthDayYear(), subjectDate);
			}

			metaKitchenReportDto.setSubjectId(bookedResource.getBookedVisit().getId());
			metaKitchenReportDto.setSubjectFirstName(firstName);
			metaKitchenReportDto.setSubjectMiddleName(middleName);
			metaKitchenReportDto.setSubjectLastName(lastName);
			metaKitchenReportDto.setMrn(subjectMrnVal);
			metaKitchenReportDto.setBirthdate(subjectBirthDate);
			metaKitchenReportDto.setLocalId(bookedResource.getBookedVisit().getStudy().getLocalId());
			metaKitchenReportDto.setIrb(bookedResource.getBookedVisit().getStudy().getIrb());
			metaKitchenReportDto.setVisitName(bookedResource.getBookedVisit().getVisitTemplate().getName());

			final List<BookedResource> meals = findBookedResourceMeal(bookedResource.getBookedVisit());
			final List<BookedResource> rooms = findBookedResourceRooms(bookedResource.getBookedVisit());

			if (!meals.isEmpty()) {
				final String resourceName = bookedResource.getResource().getName();
				if (hasAllowedMealName(bookedResource)) {
					final Date mealStartDate = parse(dateHourMin(), format(dateHourMin(), bookedResource.getScheduledStartTime()));
					final Date mealEndDate = parse(dateHourMin(), format(dateHourMin(), bookedResource.getScheduledEndTime()));

					if (rooms.size() > 0) {
						for (int i = 0; i < rooms.size(); i++) {
							final Date roomStartDate = parse(dateHourMin(), format(dateHourMin(), rooms.get(i).getScheduledStartTime()));
							final Date roomEndDate = parse(dateHourMin(), format(dateHourMin(), rooms.get(i).getScheduledEndTime()));

							if (mealTimeOverlapsRoomTime(mealStartDate, mealEndDate, roomStartDate, roomEndDate)) {
								metaKitchenReportDto.setRoom(rooms.get(i).getResource().getName());
							}
						}
					}
					metaKitchenReportDto.setResourceName(resourceName);
				}

				metaKitchenReportDto.setScheduledStartTime(bookedResource.getScheduledStartTime());
				metaKitchenReportDto.setCheckInTime(bookedResource.getBookedVisit().getScheduledStartTime());
				metaKitchenReportDto.setCheckOutTime(bookedResource.getBookedVisit().getScheduledEndTime());

				resultList.add(metaKitchenReportDto);
			}
		}
		return resultList;
	}

	public List<DeptAndPiReportDTO> getDeptAndPiReport(final ReportDTO dto) {
		final String filterString = dto.getFilterString();
		final String filterid = dto.getFilterId();
		final String sortid = dto.getSortId();

		final String baseHql = "select bv.study, " +
                "bv.checkInDate, bv.checkOutDate, " +
                "v.name, s.firstName, s.middleName, s.lastName, sm.mrn from " +
                "VisitTemplate v, BookedVisit bv, Subject s, SubjectMrn sm " +
				"where bv.subjectMrn = sm.id and sm.subject = s.id and bv.visitTemplate = v.id and " +
                " bv.appointmentStatus.isCheckedOut = TRUE and s.archivalStatus IS NULL and "+
                " ((:startTime between bv.scheduledStartTime and bv.scheduledEndTime) " +
                " or (:endTime between bv.scheduledStartTime and bv.scheduledEndTime) " +
                " or (bv.scheduledStartTime >= :startTime and bv.scheduledEndTime <= :endTime))";

		final String filterClause;

		if (isNonNullNonEmpty(filterString) && "1".equals(filterid)) {
			filterClause = " and bv.study.investigator.department.name like '%" + filterString + "%' ";
		} else if (isNonNullNonEmpty(filterString) && "2".equals(filterid)) {
			filterClause = " and bv.study.investigator.division.name like '%" + filterString + "%' ";
		} else if (isNonNullNonEmpty(filterString) && "3".equals(filterid)) {
			filterClause = " and bv.study.investigator.lastName like '%" + filterString + "%' ";
		} else if (isNonNullNonEmpty(filterString) && "4".equals(filterid)) {
			filterClause = " and bv.study.investigator.institution.name like '%" + filterString + "%' ";
		} else if (isNonNullNonEmpty(filterString) && "5".equals(filterid)) {
			filterClause = " and bv.study.investigator.credential.name like '%" + filterString + "%' ";
		} else if (isNonNullNonEmpty(filterString) && "6".equals(filterid)) {
			filterClause = " and bv.study.investigator.facultyRank.name like '%" + filterString + "%' ";
		} else if ("7".equals(filterid)) {
			filterClause = " and bv.study.crcFunded = false ";
		} else {
			filterClause = " ";
		}

		final String sortByColumnName = "bv.study.investigator.lastName";
		final SortStrategy sortStrategy = SortStrategy.fromIdString(sortid).orElse(ASCENDING);
		final String orderByClause = setOrderByClause(sortByColumnName, sortStrategy);

		final String hql = baseHql + filterClause + orderByClause;

		final Query query = session().createQuery(hql);

		setStartAndEndTimeParameters(dto, query);

		@SuppressWarnings("unchecked")
		final List<Object[]> resultRows = query.list();
		logHqlQuery(resultRows, query);

		final List<DeptAndPiReportDTO> resultList = Lists.newArrayList();

		for (final Object[] row : resultRows) {
			final DeptAndPiReportDTO deptAndPiReportDto = new DeptAndPiReportDTO();

			final Study study = (Study) row[0];

			final User investigator = study.getInvestigator();
			if (investigator == null) {
				log.info("Study " + study.getId() + " contains a null PI");
				continue;
			}

			deptAndPiReportDto.setPiId(investigator.getId());
			deptAndPiReportDto.setPiFirstName(investigator.getFirstName());
			deptAndPiReportDto.setPiMiddleName(investigator.getMiddleName());
			deptAndPiReportDto.setPiLastName(investigator.getLastName());

			if (investigator.getDepartment() != null) {
				deptAndPiReportDto.setPiDepartment(investigator.getDepartment().getName());
			} else {
				deptAndPiReportDto.setPiDepartment("");
			}

			if (investigator.getDivision() != null) {
				deptAndPiReportDto.setPiDivision(investigator.getDivision().getName());
			} else {
				deptAndPiReportDto.setPiDivision("");
			}

			if (investigator.getFacultyRank() != null) {
				deptAndPiReportDto.setPiFacultyRank(investigator.getFacultyRank().getName());
			} else {
				deptAndPiReportDto.setPiFacultyRank("");
			}

			if (investigator.getCredential() != null) {
				deptAndPiReportDto.setPiCredential(investigator.getCredential().getName());
			} else {
				deptAndPiReportDto.setPiCredential("");
			}

			deptAndPiReportDto.setPiInstitution(investigator.getInstitution().getName());
			deptAndPiReportDto.setStudyName(study.getName());

			deptAndPiReportDto.setCatalystId(orEmpty(study.getCatalystId()));

			deptAndPiReportDto.setLocalId(study.getLocalId());

			deptAndPiReportDto.setIrb(orEmpty(study.getIrb()));

			deptAndPiReportDto.setCheckInTime((Date) row[1]);
			deptAndPiReportDto.setCheckOutTime((Date) row[2]);
			deptAndPiReportDto.setVisitName((String) row[3]);
			deptAndPiReportDto.setSubjectFirstName(SubjectDataEncryptor.decrypt((String) row[4]));
			deptAndPiReportDto.setSubjectMiddleName(SubjectDataEncryptor.decrypt((String) row[5]));
			deptAndPiReportDto.setSubjectLastName(SubjectDataEncryptor.decrypt((String) row[6]));
			deptAndPiReportDto.setMrn(SubjectDataEncryptor.decrypt((String) row[7]));
			resultList.add(deptAndPiReportDto);
		}

		final Comparator<DeptAndPiReportDTO> ordering;

		if (sortStrategy == ASCENDING) {
			ordering = DeptAndPiComparator;
		} else {
			ordering = DeptAndPiComparatorDesc;
		}

		Collections.sort(resultList, ordering);

		dto.setDeptAndPiReport(resultList);
		return resultList;
	}

	public List<BillingReportDTO> getBillingReport(final ReportDTO dto) {
		final String filterString;
		final String filterid = dto.getFilterId();
		final String sortid = dto.getSortId();

		// FIXME: Munging the filterString like this is such a smell
		if ("3".equals(filterid)) {
			if ("yes".equalsIgnoreCase(dto.getFilterString())) {
				filterString = "1";
			} else {
				filterString = null;
			}
		} else {
			filterString = dto.getFilterString();
		}

		final String baseSql = "select " +
				"st.id stid, st.name stname, st.catalyst_id, st.local_id, st.irb, " +
				"st.industry_initiated, u.first_name ufirst, u.middle_name umiddle, " +
				"u.last_name ulast, st.crc_funded, bv.comment, " +

				"group_concat(lla.name separator '; ') nameC, " +
                "group_concat(obra.comment separator '; ') commentC, " +
                "group_concat(cast(obra.quantity as char) separator '; ') quantityC, " +

                "tr.billable, r.name rname, br.scheduled_start_time brst, br.scheduled_end_time bret, bv.scheduled_start_time bvst, " +
                "bv.scheduled_end_time bvet, bv.check_in_date, bv.check_out_date, sl.name slname, vt.name vtname, " +
                "vty.name, s.first_name sfirst, s.last_name slast, s.middle_name smiddle, sm.mrn, " +
                "s.birthdate " +

				"from " +
                "booked_resource br " +
                "   LEFT JOIN template_resource tr ON br.template_resource = tr.id and tr.billable = true " +
                "   LEFT JOIN override_booked_resource_annotations obra ON obra.booked_resource = br.id " +
				"   LEFT JOIN line_level_annotations lla ON obra.line_level_annotations = lla.id, " +

                "study st, user u, booked_visit bv, resource r, visit_template vt, " +
                "sublocation sl, subject_mrn sm, subject s, visit_type vty " +

				"where " +
				"br.billable = true and " +
				"bv.study = st.id and " +
				"vt.sublocation = sl.id and " +
				"vt.visit_type = vty.id and " +
                "bv.subject_mrn = sm.id and " +
                "sm.subject = s.id and " +
                "bv.visit_template = vt.id and " +
                "br.resource = r.id and " +
				"br.booked_visit = bv.id and " +
				"st.principal_investigator = u.id and " +
				"bv.appointment_status = 3 and " +
				"((:startTime between bv.scheduled_start_time and bv.scheduled_end_time) " +
				" or (:endTime between bv.scheduled_start_time and bv.scheduled_end_time) or " +
				"(bv.scheduled_start_time >= :startTime and bv.scheduled_end_time <= :endTime)) ";

		final String filterClauseSql;

		if (!Strings.isNullOrEmpty(filterString) && "1".equals(filterid)) {
			filterClauseSql = " and st.local_id like '%" + filterString + "%' ";
		} else if (!Strings.isNullOrEmpty(filterString) && "2".equals(filterid)) {
			filterClauseSql = " and st.name like '%" + filterString + "%' ";
		} else if ("3".equals(filterid)) {
			filterClauseSql = " and st.industry_initiated = true ";
		} else if (!Strings.isNullOrEmpty(filterString) && "4".equals(filterid)) {
			filterClauseSql = " and u.last_name like '%" + filterString + "%' ";
		} else if ("5".equals(filterid)) {
			filterClauseSql = " and st.crc_funded = false ";
		} else {
			filterClauseSql = " ";
		}

		final String groupByClause = " group by br.id ";

		final String sortByColumnNameSql = "st.name";

		final SortStrategy sortStrategy = SortStrategy.fromIdString(sortid).orElse(ASCENDING);

        final String orderByClauseSql = setOrderByClause(sortByColumnNameSql, sortStrategy);

		final String sql = baseSql + filterClauseSql + groupByClause + orderByClauseSql;

		final NativeQuery query = session().createNativeQuery(sql);

		setStartAndEndTimeParameters(dto, query);

        final List<Object[]> resultRows = query.list();
		logSqlQuery(resultRows, query);

		final List<BillingReportDTO> resultList = Lists.newArrayList();

		for (final Object[] row : resultRows) {
			final BillingReportDTO billingReportDto = new BillingReportDTO();

			int index = 0;
			billingReportDto.setStudyId((Integer) row[index++]);
			billingReportDto.setStudyName((String) row[index++]);
			billingReportDto.setCatalystId((String) row[index++]);
			billingReportDto.setLocalId((String) row[index++]);
			billingReportDto.setIrb((String) row[index++]);
			billingReportDto.setIndustryInitiated((Boolean) row[index++]);

			billingReportDto.setInvestigatorFirstName((String) row[index++]);
			billingReportDto.setInvestigatorMiddleName((String) row[index++]);
			billingReportDto.setInvestigatorLastName((String) row[index++]);
			billingReportDto.setCrcFunded((Boolean) row[index++]);
			billingReportDto.setBookedVisitComment((String) row[index++]);

            billingReportDto.setResourceActivities((String) row[index++]);
            billingReportDto.setResourceActivitiesComment((String) row[index++]);
            billingReportDto.setResourceActivitiesQuantity((String) row[index++]);

            billingReportDto.setBillable((Boolean) row[index++]);

            billingReportDto.setResourceName((String) row[index++]);

            billingReportDto.setCheckInTime((Date) row[index++]);
            billingReportDto.setCheckOutTime((Date) row[index++]);

            billingReportDto.setScheduledStartTime((Date) row[index++]);
            billingReportDto.setScheduledEndTime((Date) row[index++]);

            billingReportDto.setVisitCheckInTime((Date) row[index++]);
            billingReportDto.setVisitCheckOutTime((Date) row[index++]);

            billingReportDto.setSublocation((String) row[index++]);
            billingReportDto.setVisitName((String) row[index++]);
            billingReportDto.setVisitType((String) row[index++]);      // change to use Entity vs Enum

            String firstName = SubjectDataEncryptor.decrypt((String) row[index++]);
            String lastName = SubjectDataEncryptor.decrypt((String) row[index++]);
            String middleName = SubjectDataEncryptor.decrypt((String) row[index++]);
            String mrn = SubjectDataEncryptor.decrypt((String) row[index++]);

            billingReportDto.setSubjectFirstName(firstName);
            billingReportDto.setSubjectLastName(lastName);
            billingReportDto.setSubjectMiddleName(middleName);
            billingReportDto.setMrn(mrn);

            final Date subDate = new Date(((Date)row[index++]).getTime());
            final String subjectBirthDate = format(monthDayYear(), subDate);
            billingReportDto.setBirthdate(subjectBirthDate);

            resultList.add(billingReportDto);
		}

		final Comparator<BillingReportDTO> ordering;

		if (sortStrategy == ASCENDING) {
			ordering = BillingComparator;
		} else {
			ordering = BillingComparatorDesc;
		}

		Collections.sort(resultList, ordering);

		dto.setBillingReport(resultList);
		return resultList;
	}

	public List<BillingByInvestigatorReportDTO> getBillingByInvestigatorReport(final ReportDTO dto) {
		final String piFirstName = dto.getPiFirstName();
		final String piLastName = dto.getPiLastName();

		final String filterid = dto.getFilterId();
		final String sortid = dto.getSortId();

		final String filterString;

		// TODO: Munging the filterString like this is a smell
		if ("5".equals(filterid)) {
			if (countsAsYes(dto.getFilterString())) {
				filterString = "1";
			} else {
				filterString = null;
			}
		} else {
			filterString = dto.getFilterString();
		}

		final String baseSql = "select " +
                "st.id stid, st.principal_investigator, " +
                "u.first_name ufirst, u.middle_name umiddle, u.last_name ulast, " +
                "st.name, st.catalyst_id, st.local_id, " +
                "st.irb, st.industry_initiated, st.crc_funded, " +
                "bv.id bvid, vt.name vtname, bv.check_in_date, " +
                "bv.check_out_date, bv.comment, s.first_name sfirst, " +
                "s.middle_name smiddle, s.last_name slast, sm.mrn, " +
                "br.scheduled_start_time, br.scheduled_end_time, r.name rname, " +

                "group_concat(lla.name separator '; ') nameC, " +
                "group_concat(obra.comment separator '; ') commentC, " +
				"group_concat(cast(obra.quantity as char) separator '; ') quantityC, " +
				"br.id " + /* required for group by */

                "from " +
                "booked_resource br " +
                "   LEFT JOIN template_resource tr ON br.template_resource = tr.id and tr.billable = true " +
                "   LEFT JOIN override_booked_resource_annotations obra ON obra.booked_resource = br.id " +
                "   LEFT JOIN line_level_annotations lla ON obra.line_level_annotations = lla.id, " +

                "study st " +
                "   LEFT JOIN user u ON st.principal_investigator = u.id, " +
                "resource r, " +
                "subject s, " +
                "visit_template vt, " +
                "booked_visit bv, " +
                "subject_mrn sm " +

                "where " +
				"br.billable = true and " +
                "bv.study = st.id and " +
                "bv.subject_mrn = sm.id and " +
                "sm.subject = s.id and " +
                "bv.visit_template = vt.id and " +
                "br.resource = r.id and " +
                "br.booked_visit = bv.id and " +
                "bv.appointment_status = 3 and " +
                "((:startTime between br.scheduled_start_time and br.scheduled_end_time) " +
                " or (:endTime between br.scheduled_start_time and br.scheduled_end_time) or " +
                "(br.scheduled_start_time >= :startTime and br.scheduled_end_time <= :endTime)) ";

		String filterClause = " ";

		if (piFirstName != null && piFirstName.length() > 0) {
			filterClause = "and u.first_name like '%" + piFirstName + "%' ";
		} else if (piLastName != null && piLastName.length() > 0) {
			filterClause = "and u.last_name like '%" + piLastName + "%' ";
		}

		if (!Strings.isNullOrEmpty(filterString) && "1".equals(filterid)) {
			filterClause += " and r.name like '%" + filterString + "%' ";
		} else if (!Strings.isNullOrEmpty(filterString) && "2".equals(filterid)) {
			filterClause += " and st.name like '%" + filterString + "%' ";
		} else if (!Strings.isNullOrEmpty(filterString) && "3".equals(filterid)) {
			filterClause += " and st.local_id like '%" + filterString + "%' ";
		} else if (!Strings.isNullOrEmpty(filterString) && "4".equals(filterid)) {
			filterClause += " and u.last_name like '%" + filterString + "%' ";
		} else if ("5".equals(filterid)) {
			filterClause += " and st.industry_initiated = true ";
		} else if ("6".equals(filterid)) {
			filterClause += " and st.crc_funded = false ";
		}

		final String sortByColumnName = "u.last_name";
		final SortStrategy sortStrategy = SortStrategy.fromIdString(sortid).orElse(ASCENDING);
		final String orderByClause = setOrderByClause(sortByColumnName, sortStrategy);
        final String groupByClause = " group by br.id ";

		final String sql = baseSql + filterClause + groupByClause + orderByClause;

		final NativeQuery query = session().createNativeQuery(sql);

		setStartAndEndTimeParameters(dto, query);

		@SuppressWarnings("unchecked")
		final List<Object[]> resultRows = query.list();
		logHqlQuery(resultRows, query);

		final List<BillingByInvestigatorReportDTO> resultList = Lists.newArrayList();

		for (final Object[] row : resultRows) {

			final BillingByInvestigatorReportDTO billingByInvestigatorReportDto = new BillingByInvestigatorReportDTO();

			int index = 0;

			final Integer studyId = (Integer) row[index++];
			final Integer studyPiFK = (Integer) row[index++];
			if (studyPiFK == null) {
				log.info("Study " + studyId + " contains a null PI");
				continue;
			}

			billingByInvestigatorReportDto.setPiId(studyId);

			billingByInvestigatorReportDto.setPiFirstName((String) row[index++]);
			billingByInvestigatorReportDto.setPiMiddleName((String) row[index++]);
			billingByInvestigatorReportDto.setPiLastName((String) row[index++]);
			billingByInvestigatorReportDto.setStudyName((String) row[index++]);
			billingByInvestigatorReportDto.setCatalystId((String) row[index++]);
			billingByInvestigatorReportDto.setLocalId((String) row[index++]);
			billingByInvestigatorReportDto.setIrb((String) row[index++]);
			billingByInvestigatorReportDto.setIndustryInitiated((Boolean) row[index++]);
			billingByInvestigatorReportDto.setCrcFunded((Boolean) row[index++]);

			billingByInvestigatorReportDto.setBvId((Integer) row[index++]);
			billingByInvestigatorReportDto.setVisitName((String) row[index++]);
			billingByInvestigatorReportDto.setCheckInTime((Date) row[index++]);
			billingByInvestigatorReportDto.setCheckOutTime((Date) row[index++]);
			billingByInvestigatorReportDto.setBookedVisitComment((String) row[index++]);

            String firstName = SubjectDataEncryptor.decrypt((String) row[index++]);
            String middleName = SubjectDataEncryptor.decrypt((String) row[index++]);
            String lastName = SubjectDataEncryptor.decrypt((String) row[index++]);
            String mrn = SubjectDataEncryptor.decrypt((String) row[index++]);

            billingByInvestigatorReportDto.setSubjectFirstName(firstName);
            billingByInvestigatorReportDto.setSubjectMiddleName(middleName);
            billingByInvestigatorReportDto.setSubjectLastName(lastName);
            billingByInvestigatorReportDto.setMrn(mrn);

			billingByInvestigatorReportDto.setScheduledStartTime((Date) row[index++]);
			billingByInvestigatorReportDto.setScheduledEndTime((Date) row[index++]);
			billingByInvestigatorReportDto.setResourceName((String) row[index++]);

			billingByInvestigatorReportDto.setResourceActivities((String) row[index++]);
			billingByInvestigatorReportDto.setResourceActivitiesComment((String) row[index++]);
			billingByInvestigatorReportDto.setResourceActivitiesQuantity((String) row[index++]);
			resultList.add(billingByInvestigatorReportDto);
		}

		final Comparator<BillingByInvestigatorReportDTO> ordering;

		if (sortStrategy == ASCENDING) {
			ordering = BillingByInvestigatorComparator;
		} else {
			ordering = BillingByInvestigatorComparatorDesc;
		}

		Collections.sort(resultList, ordering);

		dto.setBillingByInvestigatorReport(resultList);
		return resultList;
	}

    public List<CensusReportDTO> getCensusReport(final ReportDTO dto) {
		final String filterString = dto.getFilterString();
		final String filterid = dto.getFilterId();
		final String sortid = dto.getSortId();

		final String baseHql = "select " +
				"asr.id, asr.name, st.name, pi.firstName, pi.middleName, " + // five columns
				"pi.lastName, st.catalystId, st.localId, st.irb, bv.checkInDate, " +
				"bv.checkOutDate, v.name, s.firstName, s.middleName, s.lastName, " +
				"sm.mrn, v.visitType.name, st.industryInitiated, ps.firstName, ps.middleName, " +
				"ps.lastName, st.id, bv.id, sl.name "
				+ " FROM AppointmentStatusReason asr, Study st, User pi, User ps, BookedVisit bv, VisitTemplate v, Subject s, SubjectMrn sm, Sublocation sl "
				+ " WHERE st.scheduler = ps.id and bv.checkoutStatusReason = asr.id and bv.study = st.id and s.archivalStatus IS NULL "
				+ " and st.investigator = pi.id and bv.subjectMrn = sm.id and sm.subject = s.id and bv.visitTemplate = v.id and bv.appointmentStatus.isCheckedOut = TRUE "
				+ " and sl.id = v.sublocation "
				+ " and "
				+ " (   (:startTime between bv.scheduledStartTime and bv.scheduledEndTime) "
				+ "  or (:endTime between bv.scheduledStartTime and bv.scheduledEndTime) "
				+ "  or (bv.scheduledStartTime >= :startTime and bv.scheduledEndTime <= :endTime))";

		final String filterClause;

		if (isNonNullNonEmpty(filterString) && "1".equals(filterid)) {
			filterClause = " and asr.name like '%" + filterString + "%' ";
		} else if (isNonNullNonEmpty(filterString) && "2".equals(filterid)) {
			filterClause = " and pi.lastName like '%" + filterString + "%' ";
		} else if (isNonNullNonEmpty(filterString) && "3".equals(filterid)) {
			filterClause = " and st.localId like '%" + filterString + "%' ";
		} else if (isNonNullNonEmpty(filterString) && "4".equals(filterid)) {
			filterClause = " and st.name like '%" + filterString + "%' ";
		} else if (isNonNullNonEmpty(filterString) && "5".equals(filterid)) {
			filterClause = " and v.name like '%" + filterString + "%' ";
		} else if ("6".equals(filterid)) {
			filterClause = " and st.crcFunded = false ";
		} else {
			filterClause = " ";
		}

		final String sortByColumnName = "asr.name, bv.scheduledStartTime";
		final SortStrategy sortStrategy = SortStrategy.fromIdString(sortid).orElse(ASCENDING);
		final String orderByClause = setOrderByClause(sortByColumnName, sortStrategy);

		final String hql = baseHql + filterClause + orderByClause;

		final Query query = session().createQuery(hql);

		setStartAndEndTimeParameters(dto, query);

		@SuppressWarnings("unchecked")
		final List<Object[]> resultRows = query.list();
		logHqlQuery(resultRows, query);

		final List<CensusReportDTO> resultList = Lists.newArrayList();

		for (final Object[] row : resultRows) {

			final CensusReportDTO censusReportDto = new CensusReportDTO();

			censusReportDto.setAsrId((Integer) row[0]);
			censusReportDto.setAsrName((String) row[1]);
			censusReportDto.setStudyName((String) row[2]);
			censusReportDto.setPiFirstName((String) row[3]);
			censusReportDto.setPiMiddleName((String) row[4]);
			censusReportDto.setPiLastName((String) row[5]);
			censusReportDto.setCatalystId((String) row[6]);
			censusReportDto.setLocalId((String) row[7]);
			censusReportDto.setIrb((String) row[8]);
			censusReportDto.setScheduledStartTime((Date) row[9]); // !! but the field is really 'checkinDate'!
			censusReportDto.setScheduledEndTime((Date) row[10]);
			censusReportDto.setVisitName((String) row[11]);
			censusReportDto.setSubjectFirstName(SubjectDataEncryptor.decrypt((String) row[12]));
			censusReportDto.setSubjectMiddleName(SubjectDataEncryptor.decrypt((String) row[13]));
			censusReportDto.setSubjectLastName(SubjectDataEncryptor.decrypt((String) row[14]));
			censusReportDto.setMrn(SubjectDataEncryptor.decrypt((String) row[15]));
			censusReportDto.setVisitType((String) row[16]);
			censusReportDto.setIndustryInitiated((Boolean) row[17]);
			censusReportDto.setPsFirstName((String) row[18]);
			censusReportDto.setPsMiddleName((String) row[19]);
			censusReportDto.setPsLastName((String) row[20]);
			censusReportDto.setStudyId((Integer) row[21]);
			censusReportDto.setVisitId((Integer) row[22]);
			censusReportDto.setSublocation((String) row[23]);

			resultList.add(censusReportDto);
		}

		final Comparator<CensusReportDTO> ordering;

		if (sortStrategy == ASCENDING) {
			ordering = CensusComparator;
		} else {
			ordering = CensusComparatorDesc;
		}

		Collections.sort(resultList, ordering);

		dto.setCensusReport(resultList);
		return resultList;
	}

	public List<LevelOfServiceReportDTO> getLevelOfServiceReport(final ReportDTO dto) {
		final String filterString = dto.getFilterString();
		final String filterid = dto.getFilterId();
		final String sortid = dto.getSortId();

		final String baseHql = " select " +
				"st.name, v.name, v.nursing, v.nutrition, v.processing, " + // five columns
				"v.setup, v.visitType.name, bv.checkInDate, ap.name, bv.checkOutDate, " +
				"bv.checkoutStatusReason " +
				"from Study st, VisitTemplate v, BookedVisit bv, AppointmentStatus ap "
		                     + " where bv.study = st.id and bv.visitTemplate = v.id and "
			 				 + " bv.appointmentStatus = ap.id and bv.appointmentStatus.isCheckedOut = TRUE and"
		                     + " ((:startTime between bv.scheduledStartTime and bv.scheduledEndTime) "
		                     + " or (:endTime between bv.scheduledStartTime and bv.scheduledEndTime) or (bv.scheduledStartTime >= :startTime and bv.scheduledEndTime <= :endTime))";

		final String filterClause;

		final Map<String, String> filterIdsToHqlColumns = Pairs.toMap(
		        pair("1", "v.nursing"),
		        pair("2", "v.nutrition"),
		        pair("3", "v.processing"),
		        pair("4", "v.setup"),
		        pair("5", "v.visitType.name"),
		        pair("6", "ap.name"),
		        pair("7", "bv.checkoutStatusReason.name"));
		
		final boolean filterIdIsPresent = isNonNullNonEmpty(filterString);

		if(filterIdIsPresent && filterIdsToHqlColumns.containsKey(filterid)) {
		    final String filterColumn = filterIdsToHqlColumns.get(filterid);
		    
		    filterClause = " and " + filterColumn + " like '%" + filterString + "%' ";
		} else if ("8".equals(filterid)) {
			filterClause = " and st.crcFunded = false ";
		} else {
			filterClause = " ";
		}

		final String sortByColumnName = "st.name";
		final SortStrategy sortStrategy = SortStrategy.fromIdString(sortid).orElse(ASCENDING);
		final String orderByClause = setOrderByClause(sortByColumnName, sortStrategy);

		final String hql = baseHql + filterClause + orderByClause;

		final Query query = newQuery(hql);

		setStartAndEndTimeParameters(dto, query);

		@SuppressWarnings("unchecked")
		final List<Object[]> resultRows = query.list();
		logHqlQuery(resultRows, query);

		final List<LevelOfServiceReportDTO> resultList = Lists.newArrayList();

		for (final Object[] row : resultRows) {
			final LevelOfServiceReportDTO newObj = new LevelOfServiceReportDTO();
			newObj.setStudyName((String) row[0]);
			newObj.setVisitName((String) row[1]);
			newObj.setNursing((String) row[2]);
			newObj.setNutrition((String) row[3]);
			newObj.setProcessing((String) row[4]);
			newObj.setSetup((String) row[5]);
			newObj.setVisitType((String) row[6]);
			newObj.setCheckInDate((Date) row[7]);
			newObj.setAppointmentStatus((String) row[8]);
			newObj.setCheckOutDate((Date) row[9]);
			if (((String) row[8]).equalsIgnoreCase("Cancellation")){
				newObj.setCheckOutReason(null);
			}
			else {
				newObj.setCheckOutReason((AppointmentStatusReason) row[10]);
			}

			resultList.add(newObj);
		}

		final Comparator<LevelOfServiceReportDTO> ordering;

		if (sortStrategy == ASCENDING) {
			ordering = LevelOfServiceComparator;
		} else {
			ordering = LevelOfServiceComparatorDesc;
		}

		Collections.sort(resultList, ordering);

		dto.setLevelOfServiceReport(resultList);
		return resultList;
	}

	public List<StudySubjectVisitReportDTO> getStudySubjectVisitReport(final ReportDTO dto) {

		final String filterid = dto.getFilterId();
		final String sortid = dto.getSortId();

		final String subjectLastName;

		if (!dto.getSubjectLastName().isEmpty()) {
			subjectLastName = SubjectDataEncryptor.encrypt(dto.getSubjectLastName().toUpperCase());
		} else {
			subjectLastName = dto.getSubjectLastName();
		}

		final String subjectMRN;

		if (!dto.getSubjectMRN().isEmpty()) {
			subjectMRN = SubjectDataEncryptor.encrypt(dto.getSubjectMRN().toUpperCase());
		} else {
			subjectMRN = dto.getSubjectMRN();
		}

		final String filterString = dto.getFilterString();

		final String baseHql = " select s.lastName, s.firstName, s.middleName, sm.mrn, st.localId, v.name, ap.name, s.birthdate, " +
                " bv.scheduledStartTime, bv.scheduledEndTime " +
                "from Study st, Subject s, VisitTemplate v, BookedVisit bv, SubjectMrn sm, AppointmentStatus ap " +
                " where bv.study = st.id and bv.visitTemplate = v.id and bv.subjectMrn = sm.id and sm.subject = s.id and " +
                " bv.appointmentStatus = ap.id and bv.appointmentStatus.isHold = FALSE and s.archivalStatus IS NULL";

		final List<String> filterClauses = Lists.newArrayList();

		if (!subjectLastName.isEmpty()) {
			filterClauses.add(" and s.lastName like '%" + subjectLastName + "%' ");
		}

		if (!subjectMRN.isEmpty()) {
			filterClauses.add(" and sm.mrn like '%" + subjectMRN + "%' ");
		}

		final String subjectDOB = dto.getSubjectDOB();
		if (!subjectDOB.isEmpty()) {
			filterClauses.add(" and s.birthdate = :subjectDate ");
		}

		if (isNonNullNonEmpty(filterString) && "3".equals(filterid)) {
			filterClauses.add(" and st.localId like '%" + filterString + "%' ");
		} else if (isNonNullNonEmpty(filterString) && "4".equals(filterid)) {
			filterClauses.add(" and ap.name like '%" + filterString + "%' ");
		} else if ("5".equals(filterid)) {
			filterClauses.add(" and st.crcFunded = false ");
		}

		final String sortByColumnName = "s.id";
		final SortStrategy sortStrategy = SortStrategy.fromIdString(sortid).orElse(ASCENDING);
		final String orderByClause = setOrderByClause(sortByColumnName, sortStrategy);

		final String filterClause = Joiner.on(" ").join(filterClauses);

		final String hql = baseHql + filterClause + orderByClause;

		final Query query = session().createQuery(hql);

		setSubjectDateParam(query, subjectDOB);

		@SuppressWarnings("unchecked")
		final List<Object[]> resultRows = query.list();
		logHqlQuery(resultRows, query);

		final List<StudySubjectVisitReportDTO> resultList = Lists.newArrayList();

		for (final Object[] row : resultRows) {

			final StudySubjectVisitReportDTO studySubjectVisitReportDto = new StudySubjectVisitReportDTO();

			studySubjectVisitReportDto.setSubjectLastName(SubjectDataEncryptor.decrypt((String) row[0]));
			studySubjectVisitReportDto.setSubjectFirstName(SubjectDataEncryptor.decrypt((String) row[1]));
			studySubjectVisitReportDto.setSubjectMiddleName(SubjectDataEncryptor.decrypt((String) row[2]));
			studySubjectVisitReportDto.setMrn(SubjectDataEncryptor.decrypt((String) row[3]));
			studySubjectVisitReportDto.setLocalId((String) row[4]);
			studySubjectVisitReportDto.setVisitName((String) row[5]);
			studySubjectVisitReportDto.setAppointmentStatus((String) row[6]);
			final String birthDateString;

			if ((Date) row[7] != null) {
				final Date subDate = new Date(((Date) row[7]).getTime());
				birthDateString = format(monthDayYear(), subDate);
			} else {
				birthDateString = null;
			}

			studySubjectVisitReportDto.setDob(birthDateString);
			studySubjectVisitReportDto.setScheduledStartTime((Date) row[8]);
			studySubjectVisitReportDto.setScheduledEndTime((Date) row[9]);
			resultList.add(studySubjectVisitReportDto);
		}

		final Comparator<StudySubjectVisitReportDTO> ordering;

		if (sortStrategy == ASCENDING) {
			ordering = StudySubjectVisitComparator;
		} else {
			ordering = StudySubjectVisitComparatorDesc;
		}

		Collections.sort(resultList, ordering);

		dto.setStudySubjectVisitReport(resultList);
		return resultList;
	}

	public List<StudyStatusChangeReportDTO> getStudyStatusChangeReport(final ReportDTO dto) {
		String filterString = dto.getFilterString();
        if (isNonNullNonEmpty(filterString)) {
            if (Statics.AUDIT_STUDY_PENDING.indexOf(filterString.toUpperCase()) >= 0) {
                filterString = Statics.AUDIT_STUDY_IRB_PROCESS;
            }
        }
		final String filterid = dto.getFilterId();
		final String sortid = dto.getSortId();

		final String baseHql =
                " select al.changesDetailRequiredField, " +
                        "al.date, " +
                        "al.affectedStudy, " +
                        "al.performingUser, " +
                        "al.actionPerformed " +
                "from ActivityLog al " +
                "where   (al.actionPerformed = 'CREATE STUDY' " +
                    " or (al.actionPerformed = 'UPDATE STUDY'  " +
                    "       and al.changesDetailRequiredField like '%Study Status%')) " +
                    "and (al.date >= :startTime and al.date <= :endTime) ";

		String filterClause = " ";

		final boolean filterStringIsPresent = isNonNullNonEmpty(filterString);
        
		if (filterStringIsPresent && "1".equals(filterid)) {
		    
			filterClause = " and al.affectedStudy.localId like '%" + filterString + "%' ";
		
		} else if (filterStringIsPresent && "2".equals(filterid)) {
		
		    filterClause = " and al.affectedStudy.investigator.lastName like '%" + filterString + "%' ";
		
		} else if (filterStringIsPresent && "3".equals(filterid)) {
			
		    filterClause = " and al.affectedStudy.studyStatus.name like '%" + filterString + "%' ";
		
		} else if ("4".equals(filterid)) {
			
		    filterClause = " and al.affectedStudy.crcFunded = false ";
		}

		final String sortByColumnName = "al.affectedStudy.name";
		final SortStrategy sortStrategy = SortStrategy.fromIdString(sortid).orElse(ASCENDING);
		final String orderByClause = setOrderByClause(sortByColumnName, sortStrategy);

		final String hql = baseHql + filterClause + orderByClause;

		final Query query = session().createQuery(hql);

		setStartAndEndTimeParameters(dto, query);

		@SuppressWarnings("unchecked")
		final List<Object[]> resultRows = query.list();
		logHqlQuery(resultRows, query);

		final List<StudyStatusChangeReportDTO> resultList = Lists.newArrayList();

		for (final Object[] row : resultRows) {

			final StudyStatusChangeReportDTO newObj = new StudyStatusChangeReportDTO();

            final String diffs = (String) row[0];
            final Study study = (Study) row[2];
			final String action = (String) row[4];

            String studyStatus;
            if (action.equals(Statics.AUDIT_STUDY_CREATE)) {
			    studyStatus = Statics.AUDIT_STUDY_PENDING;
            }
            else {
                studyStatus = diffs
                        .replaceAll(".*Study Status:*\\s*", "")
                        .replaceAll(",.*","")
                        .replaceAll(Statics.AUDIT_STUDY_IRB_PROCESS, Statics.AUDIT_STUDY_PENDING);
                if (studyStatus.contains(" to ")) {
                	studyStatus = "From " + studyStatus;
				}
            }

            newObj.setStudyStatus(studyStatus);

            newObj.setDateStatusChange((Date) row[1]);
            newObj.setStudyId(study.getId());
            newObj.setStudyName(study.getName());
            newObj.setLocalId(study.getLocalId());

            if (study.getInvestigator() != null) {
                newObj.setPi(study.getInvestigator().getLastName());
            }

            final User user = (User) row[3];

            newObj.setUserFirstName(user.getFirstName());
            newObj.setUserMiddleName(user.getMiddleName());
            newObj.setUserLastName(user.getLastName());

            resultList.add(newObj);
		}

		final Comparator<StudyStatusChangeReportDTO> ordering;

		if (sortStrategy == ASCENDING) {
			ordering = StudyStatusChangeComparator;
		} else {
			ordering = StudyStatusChangeComparatorDesc;
		}

		Collections.sort(resultList, ordering);

		dto.setStudyStatusChangeReport(resultList);
		return resultList;
	}

	public List<SubjectVisitHistoryReportDTO> getSubjectVisitHistoryReport(final ReportDTO dto) {

		final String filterid = dto.getFilterId();
		final String sortid = dto.getSortId();

		final String subjectLastName;
		final String subjectMRN;

		if (!dto.getSubjectLastName().isEmpty()) {
			subjectLastName = SubjectDataEncryptor.encrypt(dto.getSubjectLastName().toUpperCase());
		} else {
			subjectLastName = dto.getSubjectLastName();
		}

		if (!dto.getSubjectMRN().isEmpty()) {
			subjectMRN = SubjectDataEncryptor.encrypt(dto.getSubjectMRN().toUpperCase());
		} else {
			subjectMRN = dto.getSubjectMRN();
		}

		final String filterString = dto.getFilterString();

		final String baseHql = " select s.lastName, s.firstName, s.middleName, sm.mrn, bv " +
                " from Subject s, BookedVisit bv, SubjectMrn sm " +
                " where bv.subjectMrn = sm.id and sm.subject = s.id " +
				" and s.archivalStatus IS NULL ";

		final List<String> filterClauses = Lists.newArrayList();

		if (!subjectLastName.isEmpty()) {
			filterClauses.add(" and s.lastName like '%" + subjectLastName + "%' ");
		}

		if (!subjectMRN.isEmpty()) {
			filterClauses.add(" and sm.mrn like '%" + subjectMRN + "%' ");
		}

		final String subjectDOB = dto.getSubjectDOB();
		if (!subjectDOB.isEmpty()) {
			filterClauses.add(" and s.birthdate = :subjectDate ");
		}

		if (isNonNullNonEmpty(filterString) && "3".equals(filterid)) {
			filterClauses.add(" and bv.study.localId like '%" + filterString + "%' ");
		} else if ("4".equals(filterid)) {
			filterClauses.add(" and bv.study.crcFunded = false ");
		}

		final String filterClause = Joiner.on(" ").join(filterClauses);

		final String sortByColumnName = "s.id";
		final SortStrategy sortStrategy = SortStrategy.fromIdString(sortid).orElse(ASCENDING);
		final String orderByClause = setOrderByClause(sortByColumnName, sortStrategy);

		final String hql = baseHql + filterClause + orderByClause;

		final Query query = session().createQuery(hql);

		setSubjectDateParam(query, subjectDOB);

		@SuppressWarnings("unchecked")
		final List<Object[]> resultRows = query.list();
		logHqlQuery(resultRows, query);

		final List<SubjectVisitHistoryReportDTO> resultList = Lists.newArrayList();

		for (final Object[] row : resultRows) {
			final SubjectVisitHistoryReportDTO subjectVisitHistoryReportDto = new SubjectVisitHistoryReportDTO();

			subjectVisitHistoryReportDto.setSubjectLastName(SubjectDataEncryptor.decrypt((String) row[0]));
			subjectVisitHistoryReportDto.setSubjectFirstName(SubjectDataEncryptor.decrypt((String) row[1]));
			subjectVisitHistoryReportDto.setSubjectMiddleName(SubjectDataEncryptor.decrypt((String) row[2]));
			subjectVisitHistoryReportDto.setMrn(SubjectDataEncryptor.decrypt((String) row[3]));

			final BookedVisit bookedVisit = (BookedVisit) row[4];

			subjectVisitHistoryReportDto.setLocalId(bookedVisit.getStudy().getLocalId());
			subjectVisitHistoryReportDto.setVisitName(bookedVisit.getName());
			subjectVisitHistoryReportDto.setAppointmentStatus(bookedVisit.getAppointmentStatus().getName());
			subjectVisitHistoryReportDto.setScheduledStartTime(bookedVisit.getScheduledStartTime());
			subjectVisitHistoryReportDto.setScheduledEndTime(bookedVisit.getScheduledEndTime());
			subjectVisitHistoryReportDto.setCancelDate(bookedVisit.getCancelDate());
			subjectVisitHistoryReportDto.setCheckInDate(bookedVisit.getCheckInDate());
			subjectVisitHistoryReportDto.setCheckOutDate(bookedVisit.getCheckOutDate());

			if (bookedVisit.getCancelStatusReason() != null) {
				subjectVisitHistoryReportDto.setCancelStatusReason(bookedVisit.getCancelStatusReason().getName());
			}

			resultList.add(subjectVisitHistoryReportDto);
		}

		final Comparator<SubjectVisitHistoryReportDTO> ordering;

		if (sortStrategy == ASCENDING) {
			ordering = SubjectVisitHistoryComparator;
		} else {
			ordering = SubjectVisitHistoryComparatorDesc;
		}

		Collections.sort(resultList, ordering);

		dto.setSubjectVisitHistoryReport(resultList);
		return resultList;
	}

	public List<CRCAvailabilityReportDTO> getCRCAvailabilityReport(final ReportDTO reportDTO) {

		final String sortId = reportDTO.getSortId();

		String hqlForAllVisits =
				"SELECT b.visitTemplate.id, b.visitTemplate.name, b.study.name, a.actionPerformed " +
						"FROM BookedVisitActivityLog a, BookedVisit b " +
						"WHERE a.bookedVisit = b.id  " +
						"and a.actionPerformed in " +
						"('" + BookedVisitActivityLogStatics.OVERBOOKED.getLogString() + "', '" + BookedVisitActivityLogStatics.SCHEDULED.getLogString() + "')  " +
						"and  ((:startTime between b.scheduledStartTime and b.scheduledEndTime)  " +
						"or (:endTime between b.scheduledStartTime and b.scheduledEndTime)  " +
						"or (b.scheduledStartTime >= :startTime and b.scheduledEndTime <= :endTime)) ";

		final String sortByColumnName = "b.visitTemplate";
		final SortStrategy sortStrategy = SortStrategy.fromIdString(sortId).orElse(ASCENDING);
		final String orderByClause = setOrderByClause(sortByColumnName, sortStrategy);

		hqlForAllVisits += orderByClause;

		final Session session = session();

		final Query queryForAllVisits = session.createQuery(hqlForAllVisits);

		setStartAndEndTimeParameters(reportDTO, queryForAllVisits);

		@SuppressWarnings("unchecked")
		final List<Object[]> resultRowsForAllVisits = queryForAllVisits.list();

		logHqlQuery(resultRowsForAllVisits, queryForAllVisits);

		final List<CRCAvailabilityReportDTO> resultList = Lists.newArrayList();

		class VisitInfo {
			int visitId;
			String visitName;
			String studyName;
			int numScheduled;
			int numOverbooked;

			VisitInfo(final int visitId, final String visitName, final String studyName) {
				this.visitId = visitId;
				this.visitName = visitName;
				this.studyName = studyName;
			}
		}

		final List<VisitInfo> visitInfos = new ArrayList<VisitInfo>();
		final Map<Integer,VisitInfo> idToVisitInfo = new HashMap<Integer, VisitInfo>();

		for (final Object[] resultRow : resultRowsForAllVisits) {
			final Integer visitId = (Integer)resultRow[0];
			final String visitName = (String)resultRow[1];
			final String studyName = (String)resultRow[2];
			final String action = (String)resultRow[3];

			VisitInfo visitInfo = idToVisitInfo.get(visitId);
			if (visitInfo == null) {
				visitInfo = new VisitInfo(visitId, visitName, studyName);
				idToVisitInfo.put(visitId, visitInfo);
				visitInfos.add(visitInfo);
			}

			if (action.equals(BookedVisitActivityLogStatics.OVERBOOKED.getLogString())) {
				visitInfo.numOverbooked++;
			}
			else if (action.equals(BookedVisitActivityLogStatics.SCHEDULED.getLogString())) {
				visitInfo.numScheduled++;
			}
		}

		for (final VisitInfo visitInfo : visitInfos) {
			final CRCAvailabilityReportDTO crcAvailabilityReportDto = new CRCAvailabilityReportDTO();

			crcAvailabilityReportDto.setVisitName(visitInfo.visitName);
			crcAvailabilityReportDto.setStudyName(visitInfo.studyName);
			crcAvailabilityReportDto.setOverbook(Integer.toString(visitInfo.numOverbooked));
			crcAvailabilityReportDto.setScheduled(Integer.toString(visitInfo.numScheduled));

			resultList.add(crcAvailabilityReportDto);
		}

		reportDTO.setCrcAvailabilityReport(resultList);
		return resultList;
	}

	public List<WorkloadAndResourceResponseDTO> getResourceLevelOfServiceReport(final ReportDTO dto) {
		final String sortid = dto.getSortId();
		final String filterid = dto.getFilterId();
		final String filterString = dto.getFilterString();

		final String baseHql = " select v.id as visitId, v.visitType as visitType, v.name as visitName, v.nursing as nursing, v.nutrition as nutrition," +
				" v.processing as processing, v.setup as setup, tr.duration as duration, " +
				" r.name as resourceName, s.localId as studyName " +
				"from VisitTemplate v, TemplateResource tr, Resource r, Study s " +
				" where v.study = s.id and tr.visitTemplate = v.id and tr.resource = r.id ";

		final List<String> filterClauses = setFilterClauses(filterid, filterString);

		final String filterClause = Joiner.on(" ").join(filterClauses);

		final String sortByColumnName = "v.name";
		final SortStrategy sortStrategy = SortStrategy.fromIdString(sortid).orElse(ASCENDING);
		final String orderByClause = setOrderByClause(sortByColumnName, sortStrategy);

		final String hql = baseHql + filterClause + orderByClause;

		final Query query = session().createQuery(hql).setResultTransformer(Transformers.aliasToBean(WorkloadAndResourceResponseDTO.class));

		@SuppressWarnings("unchecked")
		final List<WorkloadAndResourceResponseDTO> resultRows = query.list();

		logHqlQuery(resultRows, query);

		return resultRows;

	}

	private String setOrderByClause(final String sortByColumnName, final SortStrategy sortStrategy) {
		return sortStrategy.makeHqlOrderBySubClause(sortByColumnName);
	}

	private List<String> setFilterClauses(final String filterid, final String filterString) {
		final boolean filterStringIsPresent = isNonNullNonEmpty(filterString);
		
        if (filterStringIsPresent && "1".equals(filterid)) {
			return newArrayList(" and s.localId like '%" + filterString + "%' ");
		} 
        
        if (filterStringIsPresent && "2".equals(filterid)) {
            return newArrayList(" and v.name like '%" + filterString + "%' ");
		}
		
		return newArrayList();
	}

	public List<VisitTemplateReportDTO> getVisitTemplateReport(final ReportDTO dto) {
		final String filterString = dto.getFilterString();
		final String filterid = dto.getFilterId();
		final String sortid = dto.getSortId();

		final String baseHql = " select s.localId, v, s.id, s.name, s.studyStatus "
				+ " from VisitTemplate v, Study s where v.study = s.id";

		final List<String> filterClauses = setFilterClauses(filterid, filterString);

		final String sortByColumnName = "s.localId";
		final SortStrategy sortStrategy = SortStrategy.fromIdString(sortid).orElse(ASCENDING);
		final String orderByClause = setOrderByClause(sortByColumnName, sortStrategy);

		final String filterClause = Joiner.on(" ").join(filterClauses);

		final String hql = baseHql + filterClause + orderByClause;

		final Query query = newQuery(hql);

		@SuppressWarnings("unchecked")
		final List<Object[]> resultRows = query.list();
		
		logHqlQuery(resultRows, query);

		final Function<Object[], VisitTemplateReportDTO> toVisitTemplateReportDTO = row -> {
		    final VisitTemplateReportDTO newObj = new VisitTemplateReportDTO();

            newObj.setStudyId((String) row[0]);

            final VisitTemplate visitTemplate = (VisitTemplate) row[1];

            newObj.setName(visitTemplate.getName());
            newObj.setSublocation(visitTemplate.getSublocation().getName());
            newObj.setBillable(visitTemplate.getBillable());
            newObj.setVisitType(visitTemplate.getVisitType().getName());

            newObj.setResearchPharmacy(toBoolean(visitTemplate.getResearchPharmacy()));
            newObj.setInstitutionNonCRC(toBoolean(visitTemplate.getInstitutionNonCRC()));
            newObj.setNonInstitutionNonCRC(toBoolean(visitTemplate.getNonInstitutionNonCRC()));
            newObj.setRelativeTime(toBoolean(visitTemplate.getRelativeTime()));

            newObj.setNursing(visitTemplate.getNursing());
            newObj.setNutrition(visitTemplate.getNutrition());
            newObj.setProcessing(visitTemplate.getProcessing());
            newObj.setSetup(visitTemplate.getSetup());
            newObj.setComment(visitTemplate.getComment());
            newObj.setId((Integer) row[2]);
            newObj.setStudyName((String) row[3]);
            newObj.setStudyStatus(((StudyStatus) row[4]).getName());
            
            return newObj;
		};
		
		final List<VisitTemplateReportDTO> resultList = enrich(resultRows).map(toVisitTemplateReportDTO).toList();
		
		dto.setVisitTemplateReport(resultList);

		return resultList;
	}
	
	private static boolean toBoolean(final Boolean b) {
	    return (b != null) ? b : false;
	}

	Optional<NutritionTasksReportDTO> findAndMungeVisit(final List<NutritionTasksReportDTO> resultList, final Object[] row) {
		return enrich(resultList).find(nutritionTasksReportDto -> {
			final String encryptedLastName = (String) row[4];
			final String visitName = (String) row[1];

			return nutritionTasksReportDto.getVisitName().equalsIgnoreCase(visitName) && nutritionTasksReportDto.getSubjectLastName().equalsIgnoreCase(SubjectDataEncryptor.decrypt(encryptedLastName));
		});
	}

	// TODO: TEST
	private boolean countsAsNo(final String s) {
		return "no".equalsIgnoreCase(s) || "n".equalsIgnoreCase(s);
	}

	// TODO: TEST
	boolean countsAsYes(final String s) {
		return "yes".equalsIgnoreCase(s) || "ye".equalsIgnoreCase(s) || "y".equalsIgnoreCase(s);
	}

	public List<VisitDurationByVisitTypeReportDTO> getVisitDurationByVisitTypeReport(final ReportDTO dto) {
		final String filterString = dto.getFilterString();
		final String filterid = dto.getFilterId();
		final String sortid = dto.getSortId();

		final String baseHql = " select " +
				"st.localId, bv.id, bv.scheduledStartTime, bv.scheduledEndTime, v.visitType.name, " +
				"v.name, bv.checkInDate, bv.checkOutDate  " +
		                       " from Study st, VisitTemplate v, BookedVisit bv " + 
		                       " where bv.appointmentStatus.isCheckedOut = TRUE and bv.study = st.id and bv.visitTemplate = v.id and " +
		                       " ((:startTime between bv.scheduledStartTime and bv.scheduledEndTime) "
				+ " or (:endTime between bv.scheduledStartTime and bv.scheduledEndTime) or (bv.scheduledStartTime >= :startTime and bv.scheduledEndTime <= :endTime))";

		final Set<String> handledFilterIds = Sets.newHashSet("1", "2");
		
		final boolean filterIdIsPresent = isNonNullNonEmpty(filterString);
		
		final String filterClause;
		
		if(filterIdIsPresent && handledFilterIds.contains(filterid)) {
		    filterClause = " and v.visitType.name like '%" + filterString + "%' ";
		} else {
			filterClause = " ";
		}

		final String sortByColumnName = "bv.scheduledStartTime";
		final SortStrategy sortStrategy = SortStrategy.fromIdString(sortid).orElse(ASCENDING);
		final String orderByClause = setOrderByClause(sortByColumnName, sortStrategy);

		final String hql = baseHql + filterClause + orderByClause;

		final Query query = session().createQuery(hql);

		setStartAndEndTimeParameters(dto, query);

		@SuppressWarnings("unchecked")
		final List<Object[]> resultRows = query.list();
		logHqlQuery(resultRows, query);

		final List<VisitDurationByVisitTypeReportDTO> resultList = Lists.newArrayList();

		for (final Object[] row : resultRows) {
			final VisitDurationByVisitTypeReportDTO visitDurationByVisitTypeReportDto = new VisitDurationByVisitTypeReportDTO();

			visitDurationByVisitTypeReportDto.setStudyName((String) row[0]);
			visitDurationByVisitTypeReportDto.setVisitId((Integer) row[1]);
			visitDurationByVisitTypeReportDto.setScheduledStartTime((Date) row[2]);
			visitDurationByVisitTypeReportDto.setScheduledEndTime((Date) row[3]);
			visitDurationByVisitTypeReportDto.setVisitType((String) row[4]);
			visitDurationByVisitTypeReportDto.setVisitName((String) row[5]);
			visitDurationByVisitTypeReportDto.setCheckInTime((Date) row[6]);
			visitDurationByVisitTypeReportDto.setCheckOutTime((Date) row[7]);
			resultList.add(visitDurationByVisitTypeReportDto);
		}

		dto.setVisitDurationByVisitType(resultList);
		return resultList;
	}

	public List<BookedVisitServiceLevelByVisitTypeReportDTO> getBookedVisitServiceLevelByVisitTypeReport(final ReportDTO dto) {

		final String filterid = dto.getFilterId();
		final String sortid = dto.getSortId();

		final String hql1 = " select max(vt.visitType), count(vt.nursing), CONCAT('NursingLevel' , vt.nursing) " +
				" from VisitTemplate vt, BookedVisit bv " +
				" where vt.nursing IS NOT NULL and vt.nursing != '' " +
				" and bv.visitTemplate = vt.id and bv.appointmentStatus.isServiceable = TRUE and ((:startTime between bv.scheduledStartTime and bv.scheduledEndTime) " +
				"or (:endTime between bv.scheduledStartTime and bv.scheduledEndTime) or (bv.scheduledStartTime >= :startTime and bv.scheduledEndTime <= :endTime)) " +
				" group by vt.visitType.name, vt.nursing " +
				" order by vt.visitType.name, vt.nursing";

		final String hql2 = " select max(vt.visitType), count(vt.nutrition), CONCAT('NutritionLevel' , vt.nutrition) " +
				" from VisitTemplate vt, BookedVisit bv " +
				" where vt.nutrition IS NOT NULL and vt.nutrition != '' " +
				" and bv.visitTemplate = vt.id and bv.appointmentStatus.isServiceable = TRUE and ((:startTime between bv.scheduledStartTime and bv.scheduledEndTime) " +
				"or (:endTime between bv.scheduledStartTime and bv.scheduledEndTime) or (bv.scheduledStartTime >= :startTime and bv.scheduledEndTime <= :endTime)) " +
				" group by vt.visitType.name, vt.nutrition " +
				" order by vt.visitType.name, vt.nutrition";

		final String hql3 = " select max(vt.visitType), count(vt.processing), CONCAT('ProcessingLevel' , vt.processing) " +
				" from VisitTemplate vt, BookedVisit bv " +
				" where vt.processing IS NOT NULL and vt.processing != '' " +
				" and bv.visitTemplate = vt.id and bv.appointmentStatus.isServiceable = TRUE and ((:startTime between bv.scheduledStartTime and bv.scheduledEndTime) " +
				"or (:endTime between bv.scheduledStartTime and bv.scheduledEndTime) or (bv.scheduledStartTime >= :startTime and bv.scheduledEndTime <= :endTime))" +
				" group by vt.visitType.name, vt.processing " +
				" order by vt.visitType.name, vt.processing";

		final String hql4 = " select max(vt.visitType), count(vt.setup), CONCAT('SetupLevel' , vt.setup)  " +
				" from VisitTemplate vt, BookedVisit bv " +
				" where vt.setup IS NOT NULL and vt.setup != '' " + " and bv.visitTemplate = vt.id and bv.appointmentStatus.isServiceable = TRUE and " +
				"((:startTime between bv.scheduledStartTime and bv.scheduledEndTime) or (:endTime between bv.scheduledStartTime and bv.scheduledEndTime) or " +
				"(bv.scheduledStartTime >= :startTime and bv.scheduledEndTime <= :endTime))" +
				" group by vt.visitType.name, vt.setup " +
				" order by vt.visitType.name, vt.setup";

		final Query query1 = newQuery(hql1);
		setStartAndEndTimeParameters(dto, query1);
		@SuppressWarnings("unchecked")
		final List<Object[]> list1 = query1.list();
		logHqlQuery(list1, query1);

		final Query query2 = newQuery(hql2);
		setStartAndEndTimeParameters(dto, query2);
		@SuppressWarnings("unchecked")
		final List<Object[]> list2 = query2.list();
		logHqlQuery(list2, query2);

		final Query query3 = newQuery(hql3);
		setStartAndEndTimeParameters(dto, query3);
		@SuppressWarnings("unchecked")
		final List<Object[]> list3 = query3.list();
		logHqlQuery(list3, query3);

		final Query query4 = newQuery(hql4);
		setStartAndEndTimeParameters(dto, query4);
		@SuppressWarnings("unchecked")
		final List<Object[]> list4 = query4.list();
		logHqlQuery(list4, query4);

		final List<Object[]> allResultRows = Lists.newArrayList();

		allResultRows.addAll(list1);
		allResultRows.addAll(list2);
		allResultRows.addAll(list3);
		allResultRows.addAll(list4);

		//FIXME: FIGURE OUT ALL THE SIDE-EFFECTS OF HAVING THIS BE MUTABLE WE'RE RELYING ON
		BookedVisitServiceLevelByVisitTypeReportDTO mydto = null;

		final Map<String, BookedVisitServiceLevelByVisitTypeReportDTO> levelMap = Maps.newHashMap();

		final Map<Integer, String> visitTypes = Maps.newHashMap();

		for (final Object[] row : allResultRows) {
			final VisitType visitType = ((VisitType) row[0]);
			final String visitTypeName = visitType.getName();
            final Integer visitTypeId = visitType.getId();

			final String count = ((Long) row[1]).toString();
			final String level = (String) row[2];


            final String[] mylevel = level.split("Level");
			final String levelValue = mylevel[1];
			final BookedVisitServiceLevelByVisitTypeReportDTO bookedVisitServiceLevelByVisitTypeReportDto = levelMap.get(makeLevelMapKey(visitTypeId, levelValue));

			if (bookedVisitServiceLevelByVisitTypeReportDto == null) {
				mydto = new BookedVisitServiceLevelByVisitTypeReportDTO();

				mydto.setVisitId(visitTypeId);
				mydto.setVisitType(visitTypeName);

				visitTypes.put(visitTypeId, "Visit" + visitTypeId);

				if ("0".equals(filterid)) {
					if ("0".equalsIgnoreCase(levelValue)) {
						levelMap.put(makeLevelMapKey(visitTypeId, levelValue), mydto);
					}
				} else if ("1".equals(filterid)) {
					if ("1".equalsIgnoreCase(levelValue)) {
						levelMap.put(makeLevelMapKey(visitTypeId, levelValue), mydto);
					}
				} else if ("2".equals(filterid)) {
					if ("2".equalsIgnoreCase(levelValue)) {
						levelMap.put(makeLevelMapKey(visitTypeId, levelValue), mydto);
					}
				} else if ("3".equals(filterid)) {
					if ("3".equalsIgnoreCase(levelValue)) {
						levelMap.put(makeLevelMapKey(visitTypeId, levelValue), mydto);
					}
				} else if ("4".equals(filterid)) {
					if ("4".equalsIgnoreCase(levelValue)) {
						levelMap.put(makeLevelMapKey(visitTypeId, levelValue), mydto);
					}
				} else if ("5".equals(filterid)) {
					if ("5".equalsIgnoreCase(levelValue)) {
						levelMap.put(makeLevelMapKey(visitTypeId, levelValue), mydto);
					}
				} else if ("6".equals(filterid)) {
					if ("6".equalsIgnoreCase(levelValue)) {
						levelMap.put(makeLevelMapKey(visitTypeId, levelValue), mydto);
					}
				} else if ("7".equals(filterid)) {
					if ("7".equalsIgnoreCase(levelValue)) {
						levelMap.put(makeLevelMapKey(visitTypeId, levelValue), mydto);
					}
				} else {
					levelMap.put(makeLevelMapKey(visitTypeId, levelValue), mydto);
				}
			} else {
				mydto = bookedVisitServiceLevelByVisitTypeReportDto;
			}

			if (level.contains("Nursing")) {
				mydto.setNursing(count);
				mydto.setNursinglevel("Level " + levelValue);
				mydto.setSetuplevel("Level " + levelValue);
				mydto.setNutritionlevel("Level " + levelValue);
				mydto.setProcessinglevel("Level " + levelValue);
			} else if (level.contains("Nutrition")) {
				mydto.setNutrition(count);
				mydto.setNutritionlevel("Level " + levelValue);
				mydto.setNursinglevel("Level " + levelValue);
				mydto.setSetuplevel("Level " + levelValue);
				mydto.setProcessinglevel("Level " + levelValue);
			} else if (level.contains("Processing")) {
				mydto.setProcessing(count);
				mydto.setProcessinglevel("Level " + levelValue);
				mydto.setNursinglevel("Level " + levelValue);
				mydto.setSetuplevel("Level " + levelValue);
				mydto.setNutritionlevel("Level " + levelValue);
			} else if (level.contains("Setup")) {
				mydto.setSetup(count);
				mydto.setSetuplevel("Level " + levelValue);
				mydto.setNursinglevel("Level " + levelValue);
				mydto.setNutritionlevel("Level " + levelValue);
				mydto.setProcessinglevel("Level " + levelValue);
			}
		}
		// for each visit type in visitTypes Map
		// Get the for 7 levels by doing this -- "Visit"+visitId +
		// "Level"+levelValue from levelMap if object not there create a dummy
		// object for the display purpose.

		final List<String> visitlists = Lists.newArrayList(visitTypes.values());

		for (final String visitString : visitlists) {
			if ("Visit1".equalsIgnoreCase(visitString)) {
				final List<BookedVisitServiceLevelByVisitTypeReportDTO> lists = Lists.newArrayList(levelMap.values());

				final List<BookedVisitServiceLevelByVisitTypeReportDTO> s1 = enrich(lists).filter(visitTypeIdIs(1)).toList();

				Collections.sort(s1, BookedVisitLevelServiceComparatorString);

				for (int in = 0; in < 8; in++) {
					//FIXME: s and in need better names
					final Integer s = in;

					mydto = new BookedVisitServiceLevelByVisitTypeReportDTO();

					if (s1.size() > 0) {

						mydto.setVisitId(s1.get(0).getVisitId());
						mydto.setVisitType(s1.get(0).getVisitType());
						mydto.setSetup("");
						mydto.setNursing("");
						mydto.setProcessing("");
						mydto.setNutrition("");
						mydto.setSetuplevel("Level " + s.toString());
						mydto.setNursinglevel("Level " + s.toString());
						mydto.setNutritionlevel("Level " + s.toString());
						mydto.setProcessinglevel("Level " + s.toString());

						final String levels = "Visit1" + "Level" + s;

						if (levelMap.containsKey(levels)) {
							//FIXME: USE OF CONTINUE IS A SMELL
							continue;
						} else {
							if ("0".equals(filterid)) {
								if (s == 0) {
									visitTypes.put(s1.get(0).getVisitId(), "Visit" + s1.get(0).getVisitId());
									levelMap.put("Visit" + s1.get(0).getVisitId() + "Level" + s, mydto);
								}
							} else if ("1".equals(filterid)) {
								if (s == 1) {
									visitTypes.put(s1.get(0).getVisitId(), "Visit" + s1.get(0).getVisitId());
									levelMap.put("Visit" + s1.get(0).getVisitId() + "Level" + s, mydto);
								}
							} else if ("2".equals(filterid)) {
								if (s == 2) {
									visitTypes.put(s1.get(0).getVisitId(), "Visit" + s1.get(0).getVisitId());
									levelMap.put("Visit" + s1.get(0).getVisitId() + "Level" + s, mydto);
								}
							} else if ("3".equals(filterid)) {
								if (s == 3) {
									visitTypes.put(s1.get(0).getVisitId(), "Visit" + s1.get(0).getVisitId());
									levelMap.put("Visit" + s1.get(0).getVisitId() + "Level" + s, mydto);
								}
							} else if ("4".equals(filterid)) {
								if (s == 4) {
									visitTypes.put(s1.get(0).getVisitId(), "Visit" + s1.get(0).getVisitId());
									levelMap.put("Visit" + s1.get(0).getVisitId() + "Level" + s, mydto);
								}
							} else if ("5".equals(filterid)) {
								if (s == 5) {
									visitTypes.put(s1.get(0).getVisitId(), "Visit" + s1.get(0).getVisitId());
									levelMap.put("Visit" + s1.get(0).getVisitId() + "Level" + s, mydto);
								}
							} else if ("6".equals(filterid)) {
								if (s == 6) {
									visitTypes.put(s1.get(0).getVisitId(), "Visit" + s1.get(0).getVisitId());
									levelMap.put("Visit" + s1.get(0).getVisitId() + "Level" + s, mydto);
								}
							} else if ("7".equals(filterid)) {
								if (s == 7) {
									visitTypes.put(s1.get(0).getVisitId(), "Visit" + s1.get(0).getVisitId());
									levelMap.put("Visit" + s1.get(0).getVisitId() + "Level" + s, mydto);
								}
							} else {
								visitTypes.put(s1.get(0).getVisitId(), "Visit" + s1.get(0).getVisitId());
								levelMap.put("Visit" + s1.get(0).getVisitId() + "Level" + s, mydto);
							}
						}
					}
				}

				//FIXME: WHY SORT HERE?  sorted list is never used again
				Collections.sort(s1, BookedVisitLevelServiceComparatorString);

			} else if ("Visit2".equalsIgnoreCase(visitString)) {
				final List<BookedVisitServiceLevelByVisitTypeReportDTO> lists = Lists.newArrayList(levelMap.values());

				final List<BookedVisitServiceLevelByVisitTypeReportDTO> s2 = enrich(lists).filter(visitTypeIdIs(2)).toList();

				Collections.sort(s2, BookedVisitLevelServiceComparatorString);

				//FIXME: s and in need better names
				for (int in = 0; in < 8; in++) {
					final Integer s = in;
					mydto = new BookedVisitServiceLevelByVisitTypeReportDTO();
					if (s2.size() > 0) {
						mydto.setVisitId(s2.get(0).getVisitId());
						mydto.setVisitType(s2.get(0).getVisitType());
						mydto.setSetup("");
						mydto.setNursing("");
						mydto.setProcessing("");
						mydto.setNutrition("");
						mydto.setSetuplevel("Level " + s.toString());
						mydto.setNursinglevel("Level " + s.toString());
						mydto.setNutritionlevel("Level " + s.toString());
						mydto.setProcessinglevel("Level " + s.toString());

						final String levels = "Visit2" + "Level" + s;
						if (levelMap.containsKey(levels)) {
							//FIXME: USE OF CONTINUE IS A SMELL
							continue;
						} else {
							if ("0".equals(filterid)) {
								if (s == 0) {
									visitTypes.put(s2.get(0).getVisitId(), "Visit" + s2.get(0).getVisitId());
									levelMap.put("Visit" + s2.get(0).getVisitId() + "Level" + s, mydto);
								}
							} else if ("1".equals(filterid)) {
								if (s == 1) {
									visitTypes.put(s2.get(0).getVisitId(), "Visit" + s2.get(0).getVisitId());
									levelMap.put("Visit" + s2.get(0).getVisitId() + "Level" + s, mydto);
								}
							} else if ("2".equals(filterid)) {
								if (s == 2) {
									visitTypes.put(s2.get(0).getVisitId(), "Visit" + s2.get(0).getVisitId());
									levelMap.put("Visit" + s2.get(0).getVisitId() + "Level" + s, mydto);
								}
							} else if ("3".equals(filterid)) {
								if (s == 3) {
									visitTypes.put(s2.get(0).getVisitId(), "Visit" + s2.get(0).getVisitId());
									levelMap.put("Visit" + s2.get(0).getVisitId() + "Level" + s, mydto);
								}
							} else if ("4".equals(filterid)) {
								if (s == 4) {
									visitTypes.put(s2.get(0).getVisitId(), "Visit" + s2.get(0).getVisitId());
									levelMap.put("Visit" + s2.get(0).getVisitId() + "Level" + s, mydto);
								}
							} else if ("5".equals(filterid)) {
								if (s == 5) {
									visitTypes.put(s2.get(0).getVisitId(), "Visit" + s2.get(0).getVisitId());
									levelMap.put("Visit" + s2.get(0).getVisitId() + "Level" + s, mydto);
								}
							} else if ("6".equals(filterid)) {
								if (s == 6) {
									visitTypes.put(s2.get(0).getVisitId(), "Visit" + s2.get(0).getVisitId());
									levelMap.put("Visit" + s2.get(0).getVisitId() + "Level" + s, mydto);
								}
							} else if ("7".equals(filterid)) {
								if (s == 7) {
									visitTypes.put(s2.get(0).getVisitId(), "Visit" + s2.get(0).getVisitId());
									levelMap.put("Visit" + s2.get(0).getVisitId() + "Level" + s, mydto);
								}
							} else {
								visitTypes.put(s2.get(0).getVisitId(), "Visit" + s2.get(0).getVisitId());
								levelMap.put("Visit" + s2.get(0).getVisitId() + "Level" + s, mydto);
							}
						}

					}
				}

				//FIXME: WHY SORT HERE?  sorted list is never used again
				Collections.sort(s2, BookedVisitLevelServiceComparatorString);

			} else if ("Visit3".equalsIgnoreCase(visitString)) {
				final List<BookedVisitServiceLevelByVisitTypeReportDTO> lists = Lists.newArrayList(levelMap.values());

				final List<BookedVisitServiceLevelByVisitTypeReportDTO> s3 = enrich(lists).filter(visitTypeIdIs(3)).toList();

				Collections.sort(s3, BookedVisitLevelServiceComparatorString);

				for (int in = 0; in < 8; in++) {
					final Integer s = in;
					mydto = new BookedVisitServiceLevelByVisitTypeReportDTO();
					if (s3.size() > 0) {
						mydto.setVisitId(s3.get(0).getVisitId());
						mydto.setVisitType(s3.get(0).getVisitType());
						mydto.setSetup("");
						mydto.setNursing("");
						mydto.setProcessing("");
						mydto.setNutrition("");
						mydto.setSetuplevel("Level " + s.toString());
						mydto.setNursinglevel("Level " + s.toString());
						mydto.setNutritionlevel("Level " + s.toString());
						mydto.setProcessinglevel("Level " + s.toString());

						final String levels = "Visit3" + "Level" + s;
						if (levelMap.containsKey(levels)) {
							//FIXME: USE OF CONTINUE IS A SMELL
							continue;
						} else {
							if ("0".equals(filterid)) {
								if (s == 0) {
									visitTypes.put(s3.get(0).getVisitId(), "Visit" + s3.get(0).getVisitId());
									levelMap.put("Visit" + s3.get(0).getVisitId() + "Level" + s, mydto);
								}
							} else if ("1".equals(filterid)) {
								if (s == 1) {
									visitTypes.put(s3.get(0).getVisitId(), "Visit" + s3.get(0).getVisitId());
									levelMap.put("Visit" + s3.get(0).getVisitId() + "Level" + s, mydto);
								}
							} else if ("2".equals(filterid)) {
								if (s == 2) {
									visitTypes.put(s3.get(0).getVisitId(), "Visit" + s3.get(0).getVisitId());
									levelMap.put("Visit" + s3.get(0).getVisitId() + "Level" + s, mydto);
								}
							} else if ("3".equals(filterid)) {
								if (s == 3) {
									visitTypes.put(s3.get(0).getVisitId(), "Visit" + s3.get(0).getVisitId());
									levelMap.put("Visit" + s3.get(0).getVisitId() + "Level" + s, mydto);
								}
							} else if ("4".equals(filterid)) {
								if (s == 4) {
									visitTypes.put(s3.get(0).getVisitId(), "Visit" + s3.get(0).getVisitId());
									levelMap.put("Visit" + s3.get(0).getVisitId() + "Level" + s, mydto);
								}
							} else if ("5".equals(filterid)) {
								if (s == 5) {
									visitTypes.put(s3.get(0).getVisitId(), "Visit" + s3.get(0).getVisitId());
									levelMap.put("Visit" + s3.get(0).getVisitId() + "Level" + s, mydto);
								}
							} else if ("6".equals(filterid)) {
								if (s == 6) {
									visitTypes.put(s3.get(0).getVisitId(), "Visit" + s3.get(0).getVisitId());
									levelMap.put("Visit" + s3.get(0).getVisitId() + "Level" + s, mydto);
								}
							} else if ("7".equals(filterid)) {
								if (s == 7) {
									visitTypes.put(s3.get(0).getVisitId(), "Visit" + s3.get(0).getVisitId());
									levelMap.put("Visit" + s3.get(0).getVisitId() + "Level" + s, mydto);
								}
							} else {
								visitTypes.put(s3.get(0).getVisitId(), "Visit" + s3.get(0).getVisitId());
								levelMap.put("Visit" + s3.get(0).getVisitId() + "Level" + s, mydto);
							}
						}
					}
				}

				//FIXME: WHY SORT HERE?  sorted list is never used again
				Collections.sort(s3, BookedVisitLevelServiceComparatorString);
			} else if ("Visit4".equalsIgnoreCase(visitString)) {
				final List<BookedVisitServiceLevelByVisitTypeReportDTO> lists = Lists.newArrayList(levelMap.values());

				final List<BookedVisitServiceLevelByVisitTypeReportDTO> s4 = enrich(lists).filter(visitTypeIdIs(4)).toList();

				Collections.sort(s4, BookedVisitLevelServiceComparatorString);

				for (int in = 0; in < 8; in++) {
					final Integer s = in;
					mydto = new BookedVisitServiceLevelByVisitTypeReportDTO();
					if (s4.size() > 0) {
						mydto.setVisitId(s4.get(0).getVisitId());
						mydto.setVisitType(s4.get(0).getVisitType());
						mydto.setSetup("");
						mydto.setNursing("");
						mydto.setProcessing("");
						mydto.setNutrition("");
						mydto.setSetuplevel("Level " + s.toString());
						mydto.setNursinglevel("Level " + s.toString());
						mydto.setNutritionlevel("Level " + s.toString());
						mydto.setProcessinglevel("Level " + s.toString());

						final String levels = "Visit4" + "Level" + s;
						if (levelMap.containsKey(levels)) {
							//FIXME: USE OF CONTINUE IS A SMELL
							continue;
						} else {
							if ("0".equals(filterid)) {
								if (s == 0) {
									visitTypes.put(s4.get(0).getVisitId(), "Visit" + s4.get(0).getVisitId());
									levelMap.put("Visit" + s4.get(0).getVisitId() + "Level" + s, mydto);
								}
							} else if ("1".equals(filterid)) {
								if (s == 1) {
									visitTypes.put(s4.get(0).getVisitId(), "Visit" + s4.get(0).getVisitId());
									levelMap.put("Visit" + s4.get(0).getVisitId() + "Level" + s, mydto);
								}
							} else if ("2".equals(filterid)) {
								if (s == 2) {
									visitTypes.put(s4.get(0).getVisitId(), "Visit" + s4.get(0).getVisitId());
									levelMap.put("Visit" + s4.get(0).getVisitId() + "Level" + s, mydto);
								}
							} else if ("3".equals(filterid)) {
								if (s == 3) {
									visitTypes.put(s4.get(0).getVisitId(), "Visit" + s4.get(0).getVisitId());
									levelMap.put("Visit" + s4.get(0).getVisitId() + "Level" + s, mydto);
								}
							} else if ("4".equals(filterid)) {
								if (s == 4) {
									visitTypes.put(s4.get(0).getVisitId(), "Visit" + s4.get(0).getVisitId());
									levelMap.put("Visit" + s4.get(0).getVisitId() + "Level" + s, mydto);
								}
							} else if ("5".equals(filterid)) {
								if (s == 5) {
									visitTypes.put(s4.get(0).getVisitId(), "Visit" + s4.get(0).getVisitId());
									levelMap.put("Visit" + s4.get(0).getVisitId() + "Level" + s, mydto);
								}
							} else if ("6".equals(filterid)) {
								if (s == 6) {
									visitTypes.put(s4.get(0).getVisitId(), "Visit" + s4.get(0).getVisitId());
									levelMap.put("Visit" + s4.get(0).getVisitId() + "Level" + s, mydto);
								}
							} else if ("7".equals(filterid)) {
								if (s == 7) {
									visitTypes.put(s4.get(0).getVisitId(), "Visit" + s4.get(0).getVisitId());
									levelMap.put("Visit" + s4.get(0).getVisitId() + "Level" + s, mydto);
								}
							} else {
								visitTypes.put(s4.get(0).getVisitId(), "Visit" + s4.get(0).getVisitId());
								levelMap.put("Visit" + s4.get(0).getVisitId() + "Level" + s, mydto);
							}
						}
					}
				}

				//FIXME: WHY SORT HERE?  sorted list is never used again
				Collections.sort(s4, BookedVisitLevelServiceComparatorString);

			} else if ("Visit5".equalsIgnoreCase(visitString)) {
				final List<BookedVisitServiceLevelByVisitTypeReportDTO> lists = Lists.newArrayList(levelMap.values());

				final List<BookedVisitServiceLevelByVisitTypeReportDTO> s5 = enrich(lists).filter(visitTypeIdIs(5)).toList();

				Collections.sort(s5, BookedVisitLevelServiceComparatorString);

				for (int in = 0; in < 8; in++) {
					final Integer s = in;
					mydto = new BookedVisitServiceLevelByVisitTypeReportDTO();
					if (s5.size() > 0) {
						mydto.setVisitId(s5.get(0).getVisitId());
						mydto.setVisitType(s5.get(0).getVisitType());
						mydto.setSetup("");
						mydto.setNursing("");
						mydto.setProcessing("");
						mydto.setNutrition("");
						mydto.setSetuplevel("Level " + s.toString());
						mydto.setNursinglevel("Level " + s.toString());
						mydto.setNutritionlevel("Level " + s.toString());
						mydto.setProcessinglevel("Level " + s.toString());

						final String levels = "Visit5" + "Level" + s;
						if (levelMap.containsKey(levels)) {
							//FIXME: USE OF CONTINUE IS A SMELL
							continue;
						} else {
							if ("0".equals(filterid)) {
								if (s == 0) {
									visitTypes.put(s5.get(0).getVisitId(), "Visit" + s5.get(0).getVisitId());
									levelMap.put("Visit" + s5.get(0).getVisitId() + "Level" + s, mydto);
								}
							} else if ("1".equals(filterid)) {
								if (s == 1) {
									visitTypes.put(s5.get(0).getVisitId(), "Visit" + s5.get(0).getVisitId());
									levelMap.put("Visit" + s5.get(0).getVisitId() + "Level" + s, mydto);
								}
							} else if ("2".equals(filterid)) {
								if (s == 2) {
									visitTypes.put(s5.get(0).getVisitId(), "Visit" + s5.get(0).getVisitId());
									levelMap.put("Visit" + s5.get(0).getVisitId() + "Level" + s, mydto);
								}
							} else if ("3".equals(filterid)) {
								if (s == 3) {
									visitTypes.put(s5.get(0).getVisitId(), "Visit" + s5.get(0).getVisitId());
									levelMap.put("Visit" + s5.get(0).getVisitId() + "Level" + s, mydto);
								}
							} else if ("4".equals(filterid)) {
								if (s == 4) {
									visitTypes.put(s5.get(0).getVisitId(), "Visit" + s5.get(0).getVisitId());
									levelMap.put("Visit" + s5.get(0).getVisitId() + "Level" + s, mydto);
								}
							} else if ("5".equals(filterid)) {
								if (s == 5) {
									visitTypes.put(s5.get(0).getVisitId(), "Visit" + s5.get(0).getVisitId());
									levelMap.put("Visit" + s5.get(0).getVisitId() + "Level" + s, mydto);
								}
							} else if ("6".equals(filterid)) {
								if (s == 6) {
									visitTypes.put(s5.get(0).getVisitId(), "Visit" + s5.get(0).getVisitId());
									levelMap.put("Visit" + s5.get(0).getVisitId() + "Level" + s, mydto);
								}
							} else if ("7".equals(filterid)) {
								if (s == 7) {
									visitTypes.put(s5.get(0).getVisitId(), "Visit" + s5.get(0).getVisitId());
									levelMap.put("Visit" + s5.get(0).getVisitId() + "Level" + s, mydto);
								}
							} else {
								visitTypes.put(s5.get(0).getVisitId(), "Visit" + s5.get(0).getVisitId());
								levelMap.put("Visit" + s5.get(0).getVisitId() + "Level" + s, mydto);
							}
						}
					}
				}

				//FIXME: WHY SORT HERE?  sorted list is never used again
				Collections.sort(s5, BookedVisitLevelServiceComparatorString);
			} else if ("Visit6".equalsIgnoreCase(visitString)) {
				final List<BookedVisitServiceLevelByVisitTypeReportDTO> lists = Lists.newArrayList(levelMap.values());

				final List<BookedVisitServiceLevelByVisitTypeReportDTO> s6 = enrich(lists).filter(visitTypeIdIs(6)).toList();

				Collections.sort(s6, BookedVisitLevelServiceComparatorString);

				for (int in = 0; in < 8; in++) {
					final Integer s = in;
					mydto = new BookedVisitServiceLevelByVisitTypeReportDTO();
					if (s6.size() > 0) {
						mydto.setVisitId(s6.get(0).getVisitId());
						mydto.setVisitType(s6.get(0).getVisitType());
						mydto.setSetup("");
						mydto.setNursing("");
						mydto.setProcessing("");
						mydto.setNutrition("");
						mydto.setSetuplevel("Level " + s.toString());
						mydto.setNursinglevel("Level " + s.toString());
						mydto.setNutritionlevel("Level " + s.toString());
						mydto.setProcessinglevel("Level " + s.toString());

						final String levels = "Visit6" + "Level" + s;
						if (levelMap.containsKey(levels)) {
							//FIXME: USE OF CONTINUE IS A SMELL
							continue;
						} else {
							if ("0".equals(filterid)) {
								if (s == 0) {
									visitTypes.put(s6.get(0).getVisitId(), "Visit" + s6.get(0).getVisitId());
									levelMap.put("Visit" + s6.get(0).getVisitId() + "Level" + s, mydto);
								}
							} else if ("1".equals(filterid)) {
								if (s == 1) {
									visitTypes.put(s6.get(0).getVisitId(), "Visit" + s6.get(0).getVisitId());
									levelMap.put("Visit" + s6.get(0).getVisitId() + "Level" + s, mydto);
								}
							} else if ("2".equals(filterid)) {
								if (s == 2) {
									visitTypes.put(s6.get(0).getVisitId(), "Visit" + s6.get(0).getVisitId());
									levelMap.put("Visit" + s6.get(0).getVisitId() + "Level" + s, mydto);
								}
							} else if ("3".equals(filterid)) {
								if (s == 3) {
									visitTypes.put(s6.get(0).getVisitId(), "Visit" + s6.get(0).getVisitId());
									levelMap.put("Visit" + s6.get(0).getVisitId() + "Level" + s, mydto);
								}
							} else if ("4".equals(filterid)) {
								if (s == 4) {
									visitTypes.put(s6.get(0).getVisitId(), "Visit" + s6.get(0).getVisitId());
									levelMap.put("Visit" + s6.get(0).getVisitId() + "Level" + s, mydto);
								}
							} else if ("5".equals(filterid)) {
								if (s == 5) {
									visitTypes.put(s6.get(0).getVisitId(), "Visit" + s6.get(0).getVisitId());
									levelMap.put("Visit" + s6.get(0).getVisitId() + "Level" + s, mydto);
								}
							} else if ("6".equals(filterid)) {
								if (s == 6) {
									visitTypes.put(s6.get(0).getVisitId(), "Visit" + s6.get(0).getVisitId());
									levelMap.put("Visit" + s6.get(0).getVisitId() + "Level" + s, mydto);
								}
							} else if ("7".equals(filterid)) {
								if (s == 7) {
									visitTypes.put(s6.get(0).getVisitId(), "Visit" + s6.get(0).getVisitId());
									levelMap.put("Visit" + s6.get(0).getVisitId() + "Level" + s, mydto);
								}
							} else {
								visitTypes.put(s6.get(0).getVisitId(), "Visit" + s6.get(0).getVisitId());
								levelMap.put("Visit" + s6.get(0).getVisitId() + "Level" + s, mydto);
							}
						}
					}
				}

				//FIXME: WHY SORT HERE?  sorted list is never used again
				Collections.sort(s6, BookedVisitLevelServiceComparatorString);
			}
		}

		// get all the values from Map and create a list.
		final List<BookedVisitServiceLevelByVisitTypeReportDTO> results = Lists.newArrayList(levelMap.values());

		final SortStrategy sortStrategy = SortStrategy.fromIdString(sortid).orElse(DESCENDING);

		final Comparator<BookedVisitServiceLevelByVisitTypeReportDTO> ordering;

		if (sortStrategy == ASCENDING) {
			ordering = BookedVisitLevelServiceComparator;
		} else {
			ordering = BookedVisitLevelServiceComparatorDesc;
		}

		Collections.sort(results, ordering);

		dto.setBookedVisitServiceLevelByTypeReport(results);
		
		return results;
	}

	public List<AncillaryOnlyByProtocolReportDTO> getAncillaryOnlyByProtocolReport(final ReportDTO dto) {
		final String filterString = dto.getFilterString();
		final String filterid = dto.getFilterId();
		final String sortid = dto.getSortId();
		String hql = null;
		String hql1 = null;
		String hql2 = null;

		final String finalhql = " SELECT COUNT(DISTINCT bv.id), s "
				+ " FROM BookedResource br, " + "BookedVisit bv, Study s "
				+ " WHERE br.bookedVisit = bv.id " + "and br.resource.name LIKE '%Ancillar%' "
				+ " and bv.study = s.id " + "and bv.appointmentStatus.isCheckedOut = TRUE and "
				+ " ((:startTime between bv.scheduledStartTime and bv.scheduledEndTime) "
				+ " or (:endTime between bv.scheduledStartTime and bv.scheduledEndTime) or "
				+ " (bv.scheduledStartTime >= :startTime and bv.scheduledEndTime <= :endTime))";

		final String filterClause;

		if (isNonNullNonEmpty(filterString) && "1".equals(filterid)) {
			filterClause = " and s.localId like '%" + filterString + "%' ";
		} else if ("2".equals(filterid)) {
			filterClause = " and s.crcFunded = false ";
		} else {
			filterClause = " ";
		}

		final String crcCategoryClause = " and s.crcCategory = 'A' ";
		final String crcCategoryClause1 = " and s.crcCategory = 'B' ";
		final String crcCategoryClause2 = " and s.crcCategory = 'D' ";

		final String sortByColumnName = "s.localId";
		final SortStrategy sortStrategy = SortStrategy.fromIdString(sortid).orElse(ASCENDING);
		final String orderByClause = setOrderByClause(sortByColumnName, sortStrategy);

		final String groupByClause = " GROUP BY s.id ";

		hql = finalhql + filterClause + crcCategoryClause + groupByClause + orderByClause;

		hql1 = finalhql + filterClause + crcCategoryClause1 + groupByClause + orderByClause;

		hql2 = finalhql + filterClause + crcCategoryClause2 + groupByClause + orderByClause;

		final Session session = session();

		final Query query = session.createQuery(hql);
		final Query query1 = session.createQuery(hql1);
		final Query query2 = session.createQuery(hql2);

		setStartAndEndTimeParameters(dto, query);

		setStartAndEndTimeParameters(dto, query1);

		setStartAndEndTimeParameters(dto, query2);

		@SuppressWarnings("unchecked")
		final List<Object[]> resultRows = query.list();
		logHqlQuery(resultRows, query);

		@SuppressWarnings("unchecked")
		final List<Object[]> resultRows1 = query1.list();
		logHqlQuery(resultRows1, query1);

		@SuppressWarnings("unchecked")
		final List<Object[]> resultRows2 = query2.list();
		logHqlQuery(resultRows2, query2);

		final List<AncillaryOnlyByProtocolReportDTO> resultList = Lists.newArrayList();
		int totalA = 0;
		int totalB = 0;
		int totalC = 0;
		for (final Object[] row : resultRows) {
			final AncillaryOnlyByProtocolReportDTO ancillaryOnlyByProtocolReportDto = new AncillaryOnlyByProtocolReportDTO();

			ancillaryOnlyByProtocolReportDto.setCrcA(row[0].toString());
			ancillaryOnlyByProtocolReportDto.setCrcB("0");
			ancillaryOnlyByProtocolReportDto.setCrcC("0");
			totalA += Integer.parseInt(ancillaryOnlyByProtocolReportDto.getCrcA());
			ancillaryOnlyByProtocolReportDto.setTotalCRCA(totalA);
			ancillaryOnlyByProtocolReportDto.setTotalCRCB(totalB);
			ancillaryOnlyByProtocolReportDto.setTotalCRCC(totalC);
			final Study study = (Study) row[1];

			final User investigator = study.getInvestigator();
			final String investigatorName = assembleInvestigatorName(investigator);

			ancillaryOnlyByProtocolReportDto.setStudyId(study.getId());

			ancillaryOnlyByProtocolReportDto.setStudyName(study.getLocalId() + investigatorName +" - " +study.getName());

			resultList.add(ancillaryOnlyByProtocolReportDto);
		}

		for (final Object[] row : resultRows1) {
			final AncillaryOnlyByProtocolReportDTO ancillaryOnlyByProtocolReportDto = new AncillaryOnlyByProtocolReportDTO();

			ancillaryOnlyByProtocolReportDto.setCrcA("0");
			ancillaryOnlyByProtocolReportDto.setCrcB(row[0].toString());
			ancillaryOnlyByProtocolReportDto.setCrcC("0");
			totalB += Integer.parseInt(ancillaryOnlyByProtocolReportDto.getCrcB());
			ancillaryOnlyByProtocolReportDto.setTotalCRCA(totalA);
			ancillaryOnlyByProtocolReportDto.setTotalCRCB(totalB);
			ancillaryOnlyByProtocolReportDto.setTotalCRCC(totalC);
			final Study study = (Study) row[1];
			ancillaryOnlyByProtocolReportDto.setStudyId(study.getId());
			final String investigatorName = assembleInvestigatorName(study.getInvestigator());

			ancillaryOnlyByProtocolReportDto.setStudyName(study.getLocalId() + investigatorName +" - " +study.getName());

			resultList.add(ancillaryOnlyByProtocolReportDto);
		}

		for (final Object[] row : resultRows2) {
			final AncillaryOnlyByProtocolReportDTO ancillaryOnlyByProtocolReportDto = new AncillaryOnlyByProtocolReportDTO();

			ancillaryOnlyByProtocolReportDto.setCrcA("0");
			ancillaryOnlyByProtocolReportDto.setCrcB("0");
			ancillaryOnlyByProtocolReportDto.setCrcC(row[0].toString());
			totalC += Integer.parseInt(ancillaryOnlyByProtocolReportDto.getCrcC());
			ancillaryOnlyByProtocolReportDto.setTotalCRCA(totalA);
			ancillaryOnlyByProtocolReportDto.setTotalCRCB(totalB);
			ancillaryOnlyByProtocolReportDto.setTotalCRCC(totalC);
			final Study study = (Study) row[1];
			ancillaryOnlyByProtocolReportDto.setStudyId(study.getId());
			final String investigatorName = assembleInvestigatorName(study.getInvestigator());

			ancillaryOnlyByProtocolReportDto.setStudyName(study.getLocalId() + investigatorName +" - " +study.getName());

			resultList.add(ancillaryOnlyByProtocolReportDto);
		}

		dto.setAncillaryOnlyByProtocolReport(resultList);
		return resultList;
	}

	String assembleInvestigatorName(final User user) {
		String result = " ";

		if (user != null) {
			result = "(" + user.getFirstName() + " , " + user.getLastName() + ")";
		}

		return result;
	}

	public List<VisitsFlaggedEditReportDTO> getVisitsFlaggedForEditReport(final ReportDTO dto) {
		final String sortid = dto.getSortId();

		final String baseHql = " select bv from BookedVisit bv "
				+ " where bv.appointmentStatus.isCheckedOut = TRUE and bv.subjectMrn.subject.archivalStatus IS NULL and "
				+ " ((:startTime between bv.scheduledStartTime and bv.scheduledEndTime) "
				+ " or (:endTime between bv.scheduledStartTime and bv.scheduledEndTime) or "
				+ " (bv.scheduledStartTime >= :startTime and bv.scheduledEndTime <= :endTime))";

		final String sortByColumnName = "bv.study";
		final SortStrategy sortStrategy = SortStrategy.fromIdString(sortid).orElse(ASCENDING);
		final String orderByClause = setOrderByClause(sortByColumnName, sortStrategy);

		final String hql = baseHql + orderByClause;

		final Query query = session().createQuery(hql);

		setStartAndEndTimeParameters(dto, query);

		@SuppressWarnings("unchecked")
		final List<BookedVisit> resultRows = query.list();
		logHqlQuery(resultRows, query);

		final List<VisitsFlaggedEditReportDTO> resultList = Lists.newArrayList();

		for (BookedVisit bv : resultRows) {
			final VisitsFlaggedEditReportDTO visitsFlaggedEditReportDTO = new VisitsFlaggedEditReportDTO();

			visitsFlaggedEditReportDTO.setId(bv.getStudy().getId());
			visitsFlaggedEditReportDTO.setStudyName(bv.getStudy().getName());
			visitsFlaggedEditReportDTO.setLocalId(bv.getStudy().getLocalId());
			if (bv.getStudy().getInvestigator() != null){
				visitsFlaggedEditReportDTO.setPiName(bv.getStudy().getInvestigator().getFirstName() + " " +bv.getStudy().getInvestigator().getLastName());
			}
			else {
				visitsFlaggedEditReportDTO.setPiName(null);
			}
			visitsFlaggedEditReportDTO.setVisitName(bv.getVisitTemplate().getName());
			visitsFlaggedEditReportDTO.setVisitType(bv.getVisitTemplate().getVisitType().getName());
			visitsFlaggedEditReportDTO.setCheckInTime(bv.getCheckInDate());
			visitsFlaggedEditReportDTO.setCheckOutTime(bv.getCheckOutDate());
			visitsFlaggedEditReportDTO.setVaryDuration(bv.isVaryDuration());
			visitsFlaggedEditReportDTO.setOmmittedActivities(bv.isOmmittedActivities());

			SubjectMrn subjectMrn = bv.getSubjectMrnDecrypted();

			Subject subject = subjectMrn.getSubject();
			final Date subDate = new Date(subject.getBirthdate().getTime());
			String birthDateString = format(monthDayYear(), subDate);

			visitsFlaggedEditReportDTO.setSubjectFirstName(subject.getFirstName());
			visitsFlaggedEditReportDTO.setSubjectMiddleName(subject.getMiddleName());
			visitsFlaggedEditReportDTO.setSubjectLastName(subject.getLastName());
			visitsFlaggedEditReportDTO.setMrn(subjectMrn.getMrn());
			visitsFlaggedEditReportDTO.setBirthdate(birthDateString);

			resultList.add(visitsFlaggedEditReportDTO);
		}

		dto.setVisitsFlaggedResult(resultList);

		return resultList;
	}

	public List<StudyDataReportResponseDTO> getStudyDataReport (
			final Optional<String> nullableLocalId,
			final Optional<String> nullableName,
			final Optional<String> nullableFundingSource,
			final Optional<StudyStatusFilter> nullableStudyStatus,
			final Optional<String> nullableSortDirection) {

		final List<StudyDataReportResponseDTO> studyDataReportResponseDTOs = new ArrayList<StudyDataReportResponseDTO>();

		String hql =
				"select s, min(bv.scheduledStartTime), " +
						"max(bv.scheduledStartTime) " +
						"from BookedVisit bv right outer join bv.study s ";


		WhereBuilder whereBuilder = whereBuilder()
				.like(nullableLocalId, "s.localId")
				.like(nullableName, "s.name");

		if (nullableStudyStatus.isPresent()) {
			StudyStatusFilter filter = nullableStudyStatus.get();
			String entityFlagName = filter.getEntityFlagName();
			if (entityFlagName != null) {
				whereBuilder = whereBuilder.isTrue("s.studyStatus." + entityFlagName);
			}
		}

		final String filterClause = whereBuilder.build();

		hql += filterClause;

		final SortOrderDirectionEnum sortOrderDirectionEnum = SortOrderDirectionEnum.fromDirection(nullableSortDirection).orElse(SortOrderDirectionEnum.ASCENDING);
		final String orderByClause = "order by s.localId " + sortOrderDirectionEnum + " ";
		final String groupByClause = "group by s.id ";

		hql += groupByClause;
		hql += orderByClause;

		hql = hql.replaceAll("\\s+", " ");

		final Query query = session().createQuery(hql);

		@SuppressWarnings("unchecked")
		final List<Object[]> resultRows = query.list();
		logHqlQuery(resultRows, query);

		for (final Object[] row : resultRows) {
			final Study study = (Study) row[0];
			final Date firstVisitDate = (Date) row[1];
			final Date lastVisitDate = (Date) row[2];

			// I suppose there is a way to do this in the SQL. Any takers?
			final Set<StudyFundingSource> sfsSet = study.getStudyFundingSources();

			if (nullableFundingSource.isPresent()) {
				final Integer fundingSourceId = Integer.valueOf(nullableFundingSource.get());
				final Function<StudyFundingSource, Integer> sfsToIdString = sfs -> sfs.getFundingSource().getId();
				
				final List<StudyFundingSource> sfsList = new ArrayList<>(sfsSet);
				final List<Integer> fundingIds = enrich(sfsList).map(sfsToIdString).toList();

				if ( ! fundingIds.contains(fundingSourceId)) {
					continue;
				}
			}

			// hmm, maybe Study should be maintaining these fields when visits are booked!
			study.setFirstVisitDate(firstVisitDate);
			study.setLastScheduledVisitDate(lastVisitDate);

			final StudyDataReportResponseDTO studyDataReportResponseDTO = new StudyDataReportResponseDTO(study, sfsSet);
			studyDataReportResponseDTOs.add(studyDataReportResponseDTO);
		}
		return studyDataReportResponseDTOs;
	}

	WhereBuilder standardTimingClause(final String earlyTime, final String lateTime) {
		final WhereBuilder timeOrClauses = whereBuilder()
				.between(":startTime", earlyTime, lateTime)
				.between(":endTime", earlyTime, lateTime)
				.beforeAndAfter(":startTime", earlyTime, ":endTime", lateTime);
		return timeOrClauses;
	}

	public List<CancellationsReportResponseDTO> getCancellationsReport(
			final Optional<String> nullableLocalId,
			final Optional<String> nullableName,
			final Optional<String> nullableType,
			final Optional<String> nullableReason,
			final Optional<String> nullableNoApprove,
			final Optional<String> nullableSortDirection,
			final Date startTime,
			final Date endTime) {

		String hql = "SELECT bv FROM BookedVisit bv ";

		final WhereBuilder timeOrClauses = standardTimingClause("bv.scheduledStartTime", "bv.scheduledEndTime");

		final String filterClause = whereBuilder()
				.isTrue("bv.appointmentStatus.isCancelled")
				.or(timeOrClauses)
				.like(nullableLocalId, "bv.study.localId")
				.like(nullableName, "bv.study.investigator.lastName")
				.equalTo(nullableType, "bv.cancelStatus")
				.equalTo(nullableReason, "bv.cancelStatusReason")
				.equalTo(nullableNoApprove, "bv.study.crcFunded")
				.build();

		final SortOrderDirectionEnum sortOrderDirectionEnum =
				SortOrderDirectionEnum.fromDirection(nullableSortDirection).orElse(SortOrderDirectionEnum.ASCENDING);
		final String orderByClause =
				"order by bv.appointmentStatusReason.name " + sortOrderDirectionEnum + ", bv.scheduledStartTime ASC ";

		hql += filterClause;
		hql += orderByClause;

		hql = hql.replaceAll("\\s+", " ");

		final Query query = session().createQuery(hql);

		query.setParameter("startTime", startTime);
		query.setParameter("endTime", endTime);

		final List<BookedVisit> resultRows = query.list();

		logHqlQuery(resultRows, query);

		final List<CancellationsReportResponseDTO> cancellationsReportResponseDTOs = new ArrayList<CancellationsReportResponseDTO>();

		for (final BookedVisit bookedVisit : resultRows) {

			final CancellationsReportResponseDTO cancellationsReportResponseDTO = new CancellationsReportResponseDTO(bookedVisit);

			cancellationsReportResponseDTOs.add(cancellationsReportResponseDTO);
		}
		return cancellationsReportResponseDTOs;
	}

	enum SortOrderDirectionEnum {
		ASCENDING("asc"),
		DESCENDING("desc");

		String direction;

		SortOrderDirectionEnum(final String direction) {
			this.direction = direction;
		}

		public static Optional<SortOrderDirectionEnum> fromDirection(final Optional<String> optionalDirection) {
			final String direction = optionalDirection.orElse(null);
			
			return enrich(values()).find(v -> v.direction.equals(direction));
		}

		@Override
		public String toString() {
			return direction;
		}
	}

	static final Comparator<StaffAuditSubjectViewsReportDTO> StaffAuditNameComparator = (o1, o2) -> {
		final int nameOrder = o1.getSubjectLastName().compareToIgnoreCase(o2.getSubjectLastName());
		if(nameOrder != 0) {
			return nameOrder;
		}
		return o1.getDate().compareTo(o2.getDate());
	};

	static final Comparator<StaffAuditSubjectViewsReportDTO> StaffAuditNameComparatorDesc = (o1, o2) -> {
		final int nameOrder = o2.getSubjectLastName().compareToIgnoreCase(o1.getSubjectLastName());
		if(nameOrder != 0) {
			return nameOrder;
		}
		return o1.getDate().compareTo(o2.getDate());
	};

	static final Comparator<ActivityLog> OverrideComparator = (o1, o2) -> {
		final int nameOrder = o1.getPerformingUser().getEcommonsId().compareToIgnoreCase(o2.getPerformingUser().getEcommonsId());
		if(nameOrder != 0) {
			return nameOrder;
		}
		return o1.getDate().compareTo(o2.getDate());
	};

	static final Comparator<ActivityLog> OverrideComparatorDesc = (o1, o2) -> {
		final int nameOrder = o2.getPerformingUser().getEcommonsId().compareToIgnoreCase(o1.getPerformingUser().getEcommonsId());
		if(nameOrder != 0) {
			return nameOrder;
		}
		return o1.getDate().compareTo(o2.getDate());
	};

	static final Comparator<SubjectPurgeReportDTO> SubjectPurgeNameComparator = (o1, o2) -> o1.getLastName().compareToIgnoreCase(o2.getLastName());

	static final Comparator<SubjectPurgeReportDTO> SubjectPurgeNameComparatorDesc = (o1, o2) -> o2.getLastName().compareToIgnoreCase(o1.getLastName());

	static final Comparator<OffUnitReportDTO> OffUnitComparator = (o1, o2) -> {
		final int nameOrder = o1.getScheduledStartTime().compareTo(o2.getScheduledStartTime());
		if(nameOrder != 0) {
			return nameOrder;
		}
		return o1.getResourceStartTime().compareTo(o2.getResourceStartTime());
	};

	static final Comparator<OffUnitReportDTO> OffUnitComparatorDesc = (o1, o2) -> {
		final int nameOrder = o2.getScheduledStartTime().compareTo(o1.getScheduledStartTime());
		if(nameOrder != 0) {
			return nameOrder;
		}
		return o1.getResourceStartTime().compareTo(o2.getResourceStartTime());
	};

	static final Comparator<DailyOverviewReportDTO> DailyOverviewComparator = (o1, o2) -> {
		final int nameOrder = o1.getScheduledStartTime().compareTo(o2.getScheduledStartTime());
		if(nameOrder != 0) {
			return nameOrder;
		}
		if (o1.getBookedVisitId() == o2.getBookedVisitId()){
			return o1.getResourceStartTime().compareTo(o2.getResourceStartTime());
		}
		return nameOrder;
	};

	static final Comparator<DailyOverviewReportDTO> DailyOverviewComparatorDesc = (o1, o2) -> {
		final int nameOrder = o2.getScheduledStartTime().compareTo(o1.getScheduledStartTime());
		if(nameOrder != 0) {
			return nameOrder;
		}
		if (o1.getBookedVisitId() == o2.getBookedVisitId()){
			return o1.getResourceStartTime().compareTo(o2.getResourceStartTime());
		}
		return nameOrder;
	};

	static final Comparator<WeeklyPharmReportDTO> WeeklyPharmComparator = (o1, o2) -> {
		final int nameOrder = o1.getStudyName().compareTo(o2.getStudyName());
		if(nameOrder != 0) {
			return nameOrder;
		}
		return o1.getScheduledStartTime().compareTo(o2.getScheduledStartTime());
	};

	static final Comparator<WeeklyPharmReportDTO> WeeklyPharmComparatorDesc = (o1, o2) -> {
		final int nameOrder = o2.getStudyName().compareTo(o1.getStudyName());
		if(nameOrder != 0) {
			return nameOrder;
		}
		return o1.getScheduledStartTime().compareTo(o2.getScheduledStartTime());
	};

	static final Comparator<StudyVisitLocationReportDTO> StudyVisitLocationComparatorAsc = (o1, o2) -> {
		final int nameOrder = o1.getSublocationName().compareTo(o2.getSublocationName());
		if(nameOrder != 0) {
			return nameOrder;
		}
		if (o1.getCheckInTime() != null && o2.getCheckInTime() != null) {
			return o1.getCheckInTime().compareTo(o2.getCheckInTime());
		}

		return 0;
	};

	static final Comparator<StudyVisitLocationReportDTO> StudyVisitLocationComparatorDesc = (o1, o2) -> {
		final int nameOrder = o2.getSublocationName().compareTo(o1.getSublocationName());
		if(nameOrder != 0) {
			return nameOrder;
		}

		if (o1.getCheckInTime() != null && o2.getCheckInTime() != null) {
			return o1.getCheckInTime().compareTo(o2.getCheckInTime());
		}

		return 0;
	};

	static final Comparator<BillableResourcesReportDTO> BillableResourcesComparator = (o1, o2) -> {
		final int nameOrder = o1.getResourceName().compareToIgnoreCase(o2.getResourceName());
		if(nameOrder != 0) {
			return nameOrder;
		}

		if (o1.getCheckInTime() != null && o2.getCheckInTime() != null) {
			return o1.getCheckInTime().compareTo(o2.getCheckInTime());
		}

		return 0;
	};

	static final Comparator<BillableResourcesReportDTO> BillableResourcesComparatorDesc = (o1, o2) -> {
		final int nameOrder = o2.getResourceName().compareToIgnoreCase(o1.getResourceName());
		if(nameOrder != 0) {
			return nameOrder;
		}

		if (o1.getCheckInTime() != null && o2.getCheckInTime() != null) {
			return o1.getCheckInTime().compareTo(o2.getCheckInTime());
		}

		return 0;
	};

	static final Comparator<TransactionsReportDTO> TransactionsComparator = (p1, p2) -> {
		final int nameOrder = p1.getAppointmentStatus().compareToIgnoreCase(p2.getAppointmentStatus());
		if(nameOrder != 0) {
			return nameOrder;
		}
		return p1.getCancelTime().compareTo(p2.getCancelTime());
	};

	static final Comparator<TransactionsReportDTO> TransactionsComparatorDesc = (p1, p2) -> {
		final int nameOrder = p2.getAppointmentStatus().compareToIgnoreCase(p1.getAppointmentStatus());
		if(nameOrder != 0) {
			return nameOrder;
		}
		return p1.getCancelTime().compareTo(p2.getCancelTime());
	};

	static final Comparator<DailyResourceReportDTO> DailyResourceComparator  = (p1, p2) -> {
		final int nameOrder = p1.getResourceName().compareTo(p2.getResourceName());

		if(nameOrder != 0) {
			return nameOrder;
		}
		return p1.getScheduledStartTime().compareTo(p2.getScheduledStartTime());
	};

	static final Comparator<DailyResourceReportDTO> DailyResourceComparatorDesc = (p1, p2) -> {
		final int nameOrder = p2.getResourceName().compareTo(p1.getResourceName());
		if(nameOrder != 0) {
			return nameOrder;
		}
		return p1.getScheduledStartTime().compareTo(p2.getScheduledStartTime());
	};

	static final Comparator<DailyAdmReportDTO> DailyAdmNameComparator = (p1, p2) -> {
		final int nameOrder = p1.getSubjectId().compareTo(p2.getSubjectId());
		if (nameOrder != 0){
			return nameOrder;
		}
		return p1.getResourceName().compareTo(p2.getResourceName());
	};

	static final Comparator<DailyAdmReportDTO> DailyAdmNameComparatorDesc = (p1, p2) -> {
		final int nameOrder = p1.getSubjectId().compareTo(p2.getSubjectId());
		if (nameOrder != 0){
			return nameOrder;
		}
		return p2.getResourceName().compareTo(p1.getResourceName());
	};

	static final Comparator<DailyAdmReportDTO> DailyAdmNameComparatorOrderingByTime = (p1, p2) -> p1.getScheduledStartTime().compareTo(p2.getScheduledStartTime());

	static final Comparator<MetaKitchenReportDTO> MetaKitchenNameComparator = (p1, p2) -> {
		final int nameOrder = p1.getSubjectLastName().compareTo(p2.getSubjectLastName());
		if(nameOrder != 0) {
			return nameOrder;
		}
		return p1.getScheduledStartTime().compareTo(p2.getScheduledStartTime());
	};

	static final Comparator<MetaKitchenReportDTO> MetaKitchenNameComparatorDesc = (p1, p2) -> {
		final int nameOrder = p2.getSubjectLastName().compareTo(p1.getSubjectLastName());
		if(nameOrder != 0) {
			return nameOrder;
		}
		return p1.getScheduledStartTime().compareTo(p2.getScheduledStartTime());
	};

	static final Comparator<MetaKitchenReportDTO> MetaKitchenByTimeComparator = (p1, p2) -> p1.getScheduledStartTime().compareTo(p2.getScheduledStartTime());

	static final Comparator<MetaKitchenReportDTO> MetaKitchenByTimeComparatorDesc = (p1, p2) -> p1.getScheduledStartTime().compareTo(p2.getScheduledStartTime());

	static final Comparator<DeptAndPiReportDTO> DeptAndPiComparator = (p1, p2) -> {
		final int nameOrder = p1.getPiLastName().compareTo(p2.getPiLastName());
		
		if(nameOrder != 0) {
			return nameOrder;
		}

		if (p1.getCheckInTime() != null && p2.getCheckInTime() != null) {
			return p1.getCheckInTime().compareTo(p2.getCheckInTime());
		}

		return 0;
	};

	static final Comparator<DeptAndPiReportDTO> DeptAndPiComparatorDesc = (p1, p2) -> {
		final int nameOrder = p2.getPiLastName().compareTo(p1.getPiLastName());
		if(nameOrder != 0) {
			return nameOrder;
		}

		if (p1.getCheckInTime() != null && p2.getCheckInTime() != null) {
			return p1.getCheckInTime().compareTo(p2.getCheckInTime());
		}
		
		return 0;
	};

	static final Comparator<BillingReportDTO> BillingComparator = (o1, o2) -> {
		final int nameOrder = o1.getStudyName().compareToIgnoreCase(o2.getStudyName());
		if(nameOrder != 0) {
			return nameOrder;
		}
		return o1.getCheckInTime().compareTo(o2.getCheckInTime());
	};

	static final Comparator<BillingReportDTO> BillingComparatorDesc = (o1, o2) -> {
		final int nameOrder = o2.getStudyName().compareToIgnoreCase(o1.getStudyName());
		if(nameOrder != 0) {
			return nameOrder;
		}
		return o1.getCheckInTime().compareTo(o2.getCheckInTime());
	};

	static final Comparator<BillingByInvestigatorReportDTO> BillingByInvestigatorComparator = (p1, p2) -> {
		final int nameOrder = p1.getPiLastName().compareTo(p2.getPiLastName());
		if(nameOrder != 0) {
			return nameOrder;
		}

		if (p1.getCheckInTime() != null && p2.getCheckInTime() != null) {
			return p1.getCheckInTime().compareTo(p2.getCheckInTime());
		}
		
		return 0;
	};

	static final Comparator<BillingByInvestigatorReportDTO> BillingByInvestigatorComparatorDesc = (p1, p2) -> {
		final int nameOrder = p2.getPiLastName().compareTo(p1.getPiLastName());
		if(nameOrder != 0) {
			return nameOrder;
		}
		if (p1.getCheckInTime() != null && p2.getCheckInTime() != null) {
			return p1.getCheckInTime().compareTo(p2.getCheckInTime());
		}
		return 0;
	};

	static final Comparator<CensusReportDTO> CensusComparator = (p1, p2) -> {
		final int nameOrder = p1.getAsrName().compareTo(p2.getAsrName());
		if(nameOrder != 0) {
			return nameOrder;
		}

		if (p1.getScheduledStartTime() != null && p2.getScheduledStartTime() != null) {
			return p1.getScheduledStartTime().compareTo(p2.getScheduledStartTime());
		}
		
		return 0;
	};

	static final Comparator<CensusReportDTO> CensusComparatorDesc = (p1, p2) -> {
		final int nameOrder = p2.getAsrName().compareTo(p1.getAsrName());
		if(nameOrder != 0) {
			return nameOrder;
		}
		
		if (p1.getScheduledStartTime() != null && p2.getScheduledStartTime() != null) {
			return p1.getScheduledStartTime().compareTo(p2.getScheduledStartTime());
		}
		
		return 0;
	};

	static final Comparator<LevelOfServiceReportDTO> LevelOfServiceComparator = (p1, p2) -> {
		final int nameOrder = p1.getStudyName().compareTo(p2.getStudyName());
		if(nameOrder != 0) {
			return nameOrder;
		}
		
		if (p1.getCheckInDate() != null && p2.getCheckInDate() != null){
			return p1.getCheckInDate().compareTo(p2.getCheckInDate());
		}
		
		return nameOrder;
	};

	static final Comparator<LevelOfServiceReportDTO> LevelOfServiceComparatorDesc = (p1, p2) -> {
		final int nameOrder = p2.getStudyName().compareTo(p1.getStudyName());
		if(nameOrder != 0) {
			return nameOrder;
		}
		if (p1.getCheckInDate() != null && p2.getCheckInDate() != null){
			return p1.getCheckInDate().compareTo(p2.getCheckInDate());
		}
		return nameOrder;
	};

	static final Comparator<StudySubjectVisitReportDTO> StudySubjectVisitComparator = (p1, p2) -> {
		final int nameOrder = p1.getSubjectLastName().compareTo(p2.getSubjectLastName());
		if(nameOrder != 0) {
			return nameOrder;
		}
		return p1.getScheduledStartTime().compareTo(p2.getScheduledStartTime());
	};

	static final Comparator<StudySubjectVisitReportDTO> StudySubjectVisitComparatorDesc = (p1, p2) -> {
		final int nameOrder = p2.getSubjectLastName().compareTo(p1.getSubjectLastName());
		if(nameOrder != 0) {
			return nameOrder;
		}
		return p1.getScheduledStartTime().compareTo(p2.getScheduledStartTime());
	};

	static final Comparator<StudyStatusChangeReportDTO> StudyStatusChangeComparator = (p1, p2) -> {
		final int nameOrder = p1.getStudyName().compareTo(p2.getStudyName());
		if(nameOrder != 0) {
			return nameOrder;
		}
		return p1.getDateStatusChange().compareTo(p2.getDateStatusChange());
	};

	static final Comparator<StudyStatusChangeReportDTO> StudyStatusChangeComparatorDesc = (p1, p2) -> {
		final int nameOrder = p2.getStudyName().compareTo(p1.getStudyName());
		if(nameOrder != 0) {
			return nameOrder;
		}
		return p1.getDateStatusChange().compareTo(p2.getDateStatusChange());
	};

	static final Comparator<SubjectVisitHistoryReportDTO> SubjectVisitHistoryComparator = (p1, p2) -> {
		final int nameOrder = p1.getSubjectLastName().compareTo(p2.getSubjectLastName());
		if(nameOrder != 0) {
			return nameOrder;
		}
		return p1.getScheduledStartTime().compareTo(p2.getScheduledStartTime());
	};

	static final Comparator<SubjectVisitHistoryReportDTO> SubjectVisitHistoryComparatorDesc = (p1, p2) -> {
		final int nameOrder = p2.getSubjectLastName().compareTo(p1.getSubjectLastName());
		if(nameOrder != 0) {
			return nameOrder;
		}
		return p2.getScheduledStartTime().compareTo(p1.getScheduledStartTime());
	};

	static final Comparator<NutritionTasksReportDTO> NutritionTasksComparator = (o1, o2) -> {
		final int nameOrder = o1.getLocalId().compareToIgnoreCase(o2.getLocalId());
		if(nameOrder != 0) {
			return nameOrder;
		}
		return o1.getScheduledStartTime().compareTo(o2.getScheduledStartTime());
	};

	static final Comparator<NutritionTasksReportDTO> NutritionTasksComparatorDesc = (o1, o2) -> {
		final int nameOrder = o2.getLocalId().compareToIgnoreCase(o1.getLocalId());
		if(nameOrder != 0) {
			return nameOrder;
		}
		return o1.getScheduledStartTime().compareTo(o2.getScheduledStartTime());
	};

	///////////////////////////////////////////////////
	//////// 2.9 reports //////////////////////////////
	///////////////////////////////////////////////////

	static final Predicate<BookedVisitServiceLevelByVisitTypeReportDTO> visitTypeIdIs(final int desiredTypeId) {
		return sa -> sa.getVisitId().equals(desiredTypeId);
	}

	static final Comparator<BookedVisitServiceLevelByVisitTypeReportDTO> BookedVisitLevelServiceComparatorString = (o1, o2) -> o1.getNursinglevel().compareTo(o2.getNursinglevel());

	static final Comparator<BookedVisitServiceLevelByVisitTypeReportDTO> BookedVisitLevelServiceComparator = (o1, o2) -> {
		final int nameOrder = o1.getVisitId().compareTo(o2.getVisitId());
		if(nameOrder != 0) {
			return nameOrder;
		}
		return o2.getNursinglevel().compareTo(o1.getNursinglevel());
	};

	static final Comparator<BookedVisitServiceLevelByVisitTypeReportDTO> BookedVisitLevelServiceComparatorDesc = (o1, o2) -> {
		final int nameOrder = o2.getVisitId().compareTo(o1.getVisitId());
		if(nameOrder != 0) {
			return nameOrder;
		}
		return o2.getNursinglevel().compareTo(o1.getNursinglevel());
	};
}
