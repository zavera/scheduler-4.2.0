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

import edu.harvard.catalyst.hccrc.core.util.Pairs;
import edu.harvard.catalyst.scheduler.dto.ReportDTO;
import edu.harvard.catalyst.scheduler.dto.WorkloadAndResourceResponseDTO;
import edu.harvard.catalyst.scheduler.dto.response.CancellationsReportResponseDTO;
import edu.harvard.catalyst.scheduler.dto.response.StudyDataReportResponseDTO;
import edu.harvard.catalyst.scheduler.dto.statics.StudyStatusFilter;
import edu.harvard.catalyst.scheduler.entity.Report;
import edu.harvard.catalyst.scheduler.entity.ResourceType;
import edu.harvard.catalyst.scheduler.entity.Sublocation;
import edu.harvard.catalyst.scheduler.entity.User;
import edu.harvard.catalyst.scheduler.persistence.ReportDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.function.Function;

import static edu.harvard.catalyst.hccrc.core.util.Pair.pair;


@Component
public class ReportService {

	private final ReportDAO reportDAO;
	private final AuditService auditService;

	private final List<Report> allReports;
	private final Map<String, String> reportNameToTitle;

	@Autowired
	public ReportService(final ReportDAO reportDAO, final AuditService auditService) {
		this.reportDAO = reportDAO;
		this.auditService = auditService;
		reportNameToTitle = new HashMap<String, String>();
		allReports = reportDAO.getReports();
		for (final Report report : allReports) {
			final String name = report.getName();
			final String title = report.getTitle();

			reportNameToTitle.put(name, title);
		}
	}

	// Don't use - Needed for spring security cglib proxying
	ReportService() {
		this(null, null);
	}

	// @Transactional
	public List<Report> getReports(final ReportDTO reportDTO) {
		return allReports;
	}

	// @Transactional
	public Report getReportData(final User user, final String ipAddress, final int id) {
		logViewReports(user, ipAddress, "Reports Screen View");

		return reportDAO.getReportDataById(id);
	}

	// @Transactional
	public void logViewReports(
			final User user,
			final String ipAddress,
			final String action) {

		auditService.logUserActivity(
				ipAddress,
				null,
				user,
				action,
				null,
				null);
	}

	void logReportExecuted(final List<?> list,
						   final User user,
						   final String remoteHost,
						   final String reportTitle) {
		if (list != null) { // the 'else' case when switching on all report titles
			if (list.size() > 0) {
				auditService.logUserActivity(
						remoteHost,
						null,
						user,
						"Report Displayed - " + reportTitle,
						null,
						null);
			} else {
				auditService.logUserActivity(
						remoteHost,
						null,
						user,
						reportTitle +
								" Report - No relevant data found in the " +
								"specified date range.",
						null,
						null);
			}
		}
	}

	public List<?>  getReport(
			final ReportDTO reportDTO,
			final User user,
			final String remoteHost) {

		final String reportName = reportDTO.getName();
		final String reportTitle = reportNameToTitle.get(reportName);
		final String timeSpanString = reportDTO.getTimeSpanString();

		auditService.logUserActivity(
				remoteHost,
				null,
				user,
				"Launch - " + reportTitle + " query " + timeSpanString,
				null,
				null);

		final List<?> reportResultList =
				determineReportResultList(reportDTO, user, reportName);

		logReportExecuted(reportResultList, user, remoteHost, reportTitle);

		return reportResultList;
	}

	private volatile Map<String, Function<ReportDTO, List<?>>> reportListStrategies = null;

	private synchronized Map<String, Function<ReportDTO, List<?>>>
											getReportListStrategies(final User user) {
		if(reportListStrategies == null) {
			reportListStrategies = Pairs.toMap(
					pair("user_data", reportDAO::getUserDataReport),
					pair("proto_by_nurse", reportDAO::getProtoNurseAndNutritionReport),
					pair("proto_nutritionist", reportDAO::getProtoNurseAndNutritionReport),
					pair("staff_audit_subject_views", (ReportDTO dto) ->
							reportDAO.getStaffAuditSubjectViewsReport(dto, user)),
					pair("subject_audit_staff_views", dto ->
							reportDAO.getSubjectAuditStaffViewsReport(dto)),
					pair("override", reportDAO::getOverrideReport),
					pair("subject_purge", dto -> reportDAO.getSubjectPurgeReport(dto)),
					pair("off_unit", reportDAO::getOffUnitReport),
					pair("daily_overview", dto -> reportDAO.getDailyOverviewReport(dto)),
					pair("export_daily_overview", reportDAO::getExportDailyOverviewReport),
					pair("weekly_pharm", reportDAO::getWeeklyPharmReport),
					pair("study_visit_location", reportDAO::getStudyVisitLocationReport),
					pair("billable_resources", reportDAO::getBillableResourcesReport),
					pair("transactions", reportDAO::getTransactionsReport),
					pair("daily_resource", dto ->
							reportDAO.getDailyResourceReport(dto, false)),
					pair("export_daily_resource", dto ->
							reportDAO.getDailyResourceReport(dto, true)),
					pair("daily_adm", reportDAO::getDailyAdmReport),
					pair("meta_kitchen", dto -> reportDAO.getMetaKitchenReport(dto)),
					pair("meta_kitchen_by_time", dto ->
							reportDAO.getMetaKitchenReport(dto)),
					pair("meta_kitchen_flat", dto ->
							reportDAO.getMetaKitchenReport(dto)),
					pair("dept_and_pi", reportDAO::getDeptAndPiReport),
					pair("billing", dto -> reportDAO.getBillingReport(dto)),
					pair("billing_by_investigator", dto ->
							reportDAO.getBillingByInvestigatorReport(dto)),
					pair("census", reportDAO::getCensusReport),
					pair("level_of_service", reportDAO::getLevelOfServiceReport),
					pair("study_subject_visit", reportDAO::getStudySubjectVisitReport),
					pair("study_status_change", reportDAO::getStudyStatusChangeReport),
					pair("subject_visit_history", reportDAO::getSubjectVisitHistoryReport),
					pair("crc_availability", reportDAO::getCRCAvailabilityReport),
					pair("resource_level_of_service",
							reportDAO::getResourceLevelOfServiceReport),
					pair("visit_template_data",
							reportDAO::getVisitTemplateReport),
					pair("visit_duration_by_visit_type",
							reportDAO::getVisitDurationByVisitTypeReport),
					pair("booked_visit_service_level_by_visit_type",
							reportDAO::getBookedVisitServiceLevelByVisitTypeReport),
					pair("ancillary_only_by_protocol",
							reportDAO::getAncillaryOnlyByProtocolReport),
					pair("visits_flagged_edit", dto ->
							reportDAO.getVisitsFlaggedForEditReport(dto))
			);
		}

		return reportListStrategies;
	}

	private List<?> determineReportResultList(
			final ReportDTO reportDTO,
			final User user,
			final String reportName) {

		final String key = reportName.toLowerCase();

		final Map<String, Function<ReportDTO, List<?>>> mapping = getReportListStrategies(user);

		if(mapping.containsKey(key)) {
			return mapping.get(key).apply(reportDTO);
		}

		return null;
	}

	// @Transactional
	public List<ResourceType> getResourceTypes() {
		return reportDAO.getResourceTypes();
	}

	// @Transactional
	public List<Sublocation> getSublocations() {
		return reportDAO.getSublocations();
	}

	// /////////////////////////////////////////////////
	// ////// 2.9 reports //////////////////////////////
	// /////////////////////////////////////////////////

	void launchAudit(final String reportName, final Date startTime, final Date endTime, final User user, final String remoteHost) {
		final String reportTitle = reportNameToTitle.get(reportName);
		final String timeSpanString;

		if (startTime != null && endTime != null) {
			timeSpanString = "for: '" + startTime + "' to '" + endTime + "'";
		} else {
			timeSpanString = "";
		}

		auditService.logUserActivity(remoteHost, null, user, "Launch - " + reportTitle + " query " + timeSpanString, null, null);
	}

	void returnAudit(final String reportName, final User user, final String remoteHost, final int resultSize) {
		final String reportTitle = reportNameToTitle.get(reportName);

		auditService.logUserActivity(remoteHost, null, user, "Report query for - " + reportTitle + " - returned " + resultSize + " result(s)", null, null);
	}

    // @Transactional
    public List<StudyDataReportResponseDTO> getStudyDataReport(
            final Optional<User> ofUser,
            final Optional<String> ofHost,
            final Optional<String> nullableLocalId,
            final Optional<String> nullableName,
            final Optional<String> nullableFundingSource,
            final Optional<StudyStatusFilter> nullableStudyStatus,
            final Optional<String> nullableSortDirection) {

		final String reportName = "study_data";
		final User user = ofUser.get();
		final String host = ofHost.get();

		/* no dates for studyData */
		launchAudit(reportName, null, null, user, host);

        final List<StudyDataReportResponseDTO> result = reportDAO.getStudyDataReport(
                nullableLocalId,
                nullableName,
                nullableFundingSource,
                nullableStudyStatus,
                nullableSortDirection);

		returnAudit(reportName, user, host, result.size());

		return result;
	}

	// @Transactional
	public List<CancellationsReportResponseDTO> getCancellationsReport(
            final Optional<User> ofUser,
            final Optional<String> ofHost,
            final Optional<Date> ofStartTime,
            final Optional<Date> ofEndTime,
            final Optional<String> nullableLocalId,
            final Optional<String> nullableName,
            final Optional<String> nullableType,
            final Optional<String> nullableReason,
            final Optional<String> nullableNoApprove,
            final Optional<String> nullableSortDirection) {

        final String institution = ofUser.get().getInstitution().getName();
		final String reportName = "cancellations";

		final Date startTime = ofStartTime.get();
		final Date endTime = ofEndTime.get();
		final User user = ofUser.get();
		final String remoteHost = ofHost.get();

		launchAudit(reportName, startTime, endTime, user, remoteHost);

		final List<CancellationsReportResponseDTO> result = reportDAO.getCancellationsReport(
                nullableLocalId,
                nullableName,
                nullableType,
                nullableReason,
                nullableNoApprove,
                nullableSortDirection,
                startTime,
                endTime);

		returnAudit(reportName, user, remoteHost, result.size());

		return result;
	}

	// @Transactional
	public List<WorkloadAndResourceResponseDTO> getResourceLevelOfServiceReport(
			ReportDTO dto,
			final Optional<User> ofUser,
			final Optional<String> ofHost) {

		final String reportName = "resource_level_of_service";

		final User user = ofUser.get();
		final String remoteHost = ofHost.get();

		launchAudit(reportName, null, null, user, remoteHost);

		final List<WorkloadAndResourceResponseDTO> result = reportDAO.getResourceLevelOfServiceReport(dto);

		returnAudit(reportName, user, remoteHost, result.size());

		return result;
	}
}
