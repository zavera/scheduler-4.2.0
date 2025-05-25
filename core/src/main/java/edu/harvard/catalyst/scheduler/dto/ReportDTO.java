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
package edu.harvard.catalyst.scheduler.dto;

import edu.harvard.catalyst.scheduler.dto.response.CancellationsReportResponseDTO;
import edu.harvard.catalyst.scheduler.dto.response.StudyDataReportResponseDTO;
import edu.harvard.catalyst.scheduler.entity.Report;
import edu.harvard.catalyst.scheduler.entity.ResourceType;
import edu.harvard.catalyst.scheduler.entity.Sublocation;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class ReportDTO extends AuthorizedDTO {

    private List<Report> reports;

    public static final String JSON = "json";
    public static final String CSV_REGULAR = "csv";

    private List<ResourceType> resourceTypes;
    private List<Sublocation> sublocations;

    private List<UserReportDTO> userDataReport;
    private List<OverrideReportDTO> overrideReport;
    private List<StaffAuditSubjectViewsReportDTO> staffAuditSubjectViewsReport;
    private List<SubjectAuditStaffViewsReportDTO> subjectAuditStaffViewsReport;
    private List<SubjectPurgeReportDTO> subjectPurgeReport;
    private List<OffUnitReportDTO> offUnitReport;

    private List<DailyOverviewReportDTO> dailyOverviewReport;
    private List<NursingAndRoomDailyOverviewReportDTO> nursingAndRoomDailyOverviewReport;
    private List<WeeklyPharmReportDTO> weeklyPharmReport;
    private List<StudyVisitLocationReportDTO> studyVisitLocationReport;
    private List<BillableResourcesReportDTO> billableResourcesReport;
    private List<TransactionsReportDTO> transactionsReport;
    private List<DailyResourceReportDTO> dailyResourceReport;
    private List<DailyAdmReportDTO> dailyAdmReport;
    private List<StudySubjectVisitReportDTO> studySubjectVisitReport;
    private List<StudyStatusChangeReportDTO> studyStatusChangeReport;
    private List<SubjectVisitHistoryReportDTO> subjectVisitHistoryReport;
    private List<MetaKitchenReportDTO> metaKitchenReport;
    private List<DeptAndPiReportDTO> deptAndPiReport;
    private List<BillingReportDTO> billingReport;
    private List<BillingByInvestigatorReportDTO> billingByInvestigatorReport;
    private List<CensusReportDTO> censusReport;
    private List<LevelOfServiceReportDTO> levelOfServiceReport;
    private List<CRCAvailabilityReportDTO> crcAvailabilityReport;
    private List<AncillaryOnlyByProtocolReportDTO> ancillaryOnlyByProtocolReport;
    private List<VisitTemplateReportDTO> visitTemplateReport;
    private List<NutritionTasksReportDTO> nutritionTasksReport;
    private List<VisitDurationByVisitTypeReportDTO> visitDurationByVisitType;
    private List<BookedVisitServiceLevelByVisitTypeReportDTO>
            bookedVisitServiceLevelByTypeReport;
    private List<VisitTerminatedPriorToCompletionDTO> visitTerminatedPriorToCompletion;
    private List<ProtoNurseReportDTO> protoNurseNutritionReport;
    private List<VisitsFlaggedEditReportDTO> visitsFlaggedResult;
    private List<StudyDataReportResponseDTO> studyDataReport;
    List<CancellationsReportResponseDTO> cancellationsReport;
    List<WorkloadAndResourceResponseDTO> resourceLevelOfServiceReport;

    private String name;
    private Date startTime;
    private Date endTime;
    private String mrn;
    private String piFirstName;
    private String piLastName;
    private String subjectLastName;
    private String subjectMRN;
    private String subjectDOB;

    public ReportDTO() {
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(final Date startTime) {
        this.startTime = startTime;
    }

    public Date getEndTime() {
        final Calendar cal = Calendar.getInstance();
        cal.setTime(endTime);
        cal.set(Calendar.HOUR, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        return cal.getTime();
    }

    public void setEndTime(final Date endTime) {
        final Calendar cal = Calendar.getInstance();
        cal.setTime(endTime);
        cal.set(Calendar.HOUR, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        this.endTime = cal.getTime();
    }

    public String getMrn() {
        return mrn;
    }

    public void setMrn(final String mrn) {
        this.mrn = mrn;
    }

    public String getSubjectLastName() {
        return subjectLastName;
    }

    public void setSubjectLastName(final String subjectLastName) {
        this.subjectLastName = subjectLastName;
    }

    public String getSubjectMRN() {
        return subjectMRN;
    }

    public void setSubjectMRN(final String subjectMRN) {
        this.subjectMRN = subjectMRN;
    }

    public String getSubjectDOB() {
        return subjectDOB;
    }

    public void setSubjectDOB(final String subjectDOB) {
        this.subjectDOB = subjectDOB;
    }

    public String getPiFirstName() {
        return piFirstName;
    }

    public void setPiFirstName(final String piFirstName) {
        this.piFirstName = piFirstName;
    }

    public String getPiLastName() {
        return piLastName;
    }

    public void setPiLastName(final String piLastName) {
        this.piLastName = piLastName;
    }

    public List<Report> getReports() {
        return reports;
    }

    public void setReports(final List<Report> reports) {
        this.reports = reports;
    }

    public String getTimeSpanString() {
        String result = "";
        if (startTime != null && endTime != null) {
            result = "for :\"" + startTime + "\" to \"" + endTime;
        }
        return result;
    }

    //////////////////////////
    // used but not in /getReport

    public void setResourceTypes(final List<ResourceType> resourceTypes) {
        this.resourceTypes = resourceTypes;
    }

    public void setSublocations(List<Sublocation> sublocations) {
        this.sublocations = sublocations;
    }

    ///////////////////////

    public List<OverrideReportDTO> getOverrideReport() {
        return overrideReport;
    }

    public void setOverrideReport(final List<OverrideReportDTO> overrideReport) {
        this.overrideReport = overrideReport;
    }

    public List<UserReportDTO> getUserDataReport() {
        return userDataReport;
    }

    public void setUserDataReport(final List<UserReportDTO> userDataReport) {
        this.userDataReport = userDataReport;
    }

    public List<BookedVisitServiceLevelByVisitTypeReportDTO> getBookedVisitServiceLevelByTypeReport() {
        return bookedVisitServiceLevelByTypeReport;
    }

    public void setBookedVisitServiceLevelByTypeReport(final List<BookedVisitServiceLevelByVisitTypeReportDTO> bookedVisitServiceLevelByTypeReport) {
        this.bookedVisitServiceLevelByTypeReport = bookedVisitServiceLevelByTypeReport;
    }

    public List<VisitDurationByVisitTypeReportDTO> getVisitDurationByVisitType() {
        return visitDurationByVisitType;
    }

    public void setVisitDurationByVisitType(final List<VisitDurationByVisitTypeReportDTO> visitDurationByVisitType) {
        this.visitDurationByVisitType = visitDurationByVisitType;
    }

    public void setProtoNurseNutritionReport(
            List<ProtoNurseReportDTO> protoNurseNutritionReport) {
        this.protoNurseNutritionReport = protoNurseNutritionReport;
    }

    public List<StaffAuditSubjectViewsReportDTO> getStaffAuditSubjectViewsReport() {
        return staffAuditSubjectViewsReport;
    }

    public void setStaffAuditSubjectViewsReport(final List<StaffAuditSubjectViewsReportDTO> staffAuditSubjectViewsReport) {
        this.staffAuditSubjectViewsReport = staffAuditSubjectViewsReport;
    }

    public List<SubjectAuditStaffViewsReportDTO> getSubjectAuditStaffViewsReport() {
        return subjectAuditStaffViewsReport;
    }

    public void setSubjectAuditStaffViewsReport(final List<SubjectAuditStaffViewsReportDTO> subjectAuditStaffViewsReport) {
        this.subjectAuditStaffViewsReport = subjectAuditStaffViewsReport;
    }

    public List<VisitTemplateReportDTO> getVisitTemplateReport() {
        return visitTemplateReport;
    }

    public void setVisitTemplateReport(final List<VisitTemplateReportDTO> visitTemplateReport) {
        this.visitTemplateReport = visitTemplateReport;
    }

    public List<SubjectPurgeReportDTO> getSubjectPurgeReport() {
        return subjectPurgeReport;
    }

    public void setSubjectPurgeReport(final List<SubjectPurgeReportDTO> subjectPurgeReport) {
        this.subjectPurgeReport = subjectPurgeReport;
    }

    public List<OffUnitReportDTO> getOffUnitReport() {
        return offUnitReport;
    }

    public void setOffUnitReport(final List<OffUnitReportDTO> offUnitReport) {
        this.offUnitReport = offUnitReport;
    }

    public List<DailyOverviewReportDTO> getDailyOverviewReport() {
        return dailyOverviewReport;
    }

    public void setDailyOverviewReport(final List<DailyOverviewReportDTO> dailyOverviewReport) {
        this.dailyOverviewReport = dailyOverviewReport;
    }

    public List<WeeklyPharmReportDTO> getWeeklyPharmReport() {
        return weeklyPharmReport;
    }

    public void setWeeklyPharmReport(final List<WeeklyPharmReportDTO> weeklyPharmReport) {
        this.weeklyPharmReport = weeklyPharmReport;
    }

    public List<StudyVisitLocationReportDTO> getStudyVisitLocationReport() {
        return studyVisitLocationReport;
    }

    public void setStudyVisitLocationReport(final List<StudyVisitLocationReportDTO> studyVisitLocationReport) {
        this.studyVisitLocationReport = studyVisitLocationReport;
    }

    public List<BillableResourcesReportDTO> getBillableResourcesReport() {
        return billableResourcesReport;
    }

    public void setBillableResourcesReport(final List<BillableResourcesReportDTO> billableResourcesReport) {
        this.billableResourcesReport = billableResourcesReport;
    }

    public List<TransactionsReportDTO> getTransactionsReport() {
        return transactionsReport;
    }

    public void setTransactionsReport(final List<TransactionsReportDTO> transactionsReport) {
        this.transactionsReport = transactionsReport;
    }

    public List<DailyResourceReportDTO> getDailyResourceReport() {
        return dailyResourceReport;
    }

    public void setDailyResourceReport(final List<DailyResourceReportDTO> dailyResourceReport) {
        this.dailyResourceReport = dailyResourceReport;
    }

    public List<DailyAdmReportDTO> getDailyAdmReport() {
        return dailyAdmReport;
    }

    public void setDailyAdmReport(final List<DailyAdmReportDTO> dailyAdmReport) {
        this.dailyAdmReport = dailyAdmReport;
    }

    public List<MetaKitchenReportDTO> getMetaKitchenReport() {
        return metaKitchenReport;
    }

    public void setMetaKitchenReport(final List<MetaKitchenReportDTO> metaKitchenReport) {
        this.metaKitchenReport = metaKitchenReport;
    }

    public List<DeptAndPiReportDTO> getDeptAndPiReport() {
        return deptAndPiReport;
    }

    public void setDeptAndPiReport(final List<DeptAndPiReportDTO> deptAndPiReport) {
        this.deptAndPiReport = deptAndPiReport;
    }

    public List<BillingReportDTO> getBillingReport() {
        return billingReport;
    }

    public void setBillingReport(final List<BillingReportDTO> billingReport) {
        this.billingReport = billingReport;
    }

    public List<BillingByInvestigatorReportDTO> getBillingByInvestigatorReport() {
        return billingByInvestigatorReport;
    }

    public void setBillingByInvestigatorReport(final List<BillingByInvestigatorReportDTO> billingByInvestigatorReport) {
        this.billingByInvestigatorReport = billingByInvestigatorReport;
    }

    public List<CensusReportDTO> getCensusReport() {
        return censusReport;
    }

    public List<NursingAndRoomDailyOverviewReportDTO> getNursingAndRoomDailyOverviewReport() {
        return nursingAndRoomDailyOverviewReport;
    }

    ///////////////

    public void setCensusReport(final List<CensusReportDTO> censusReport) {
        this.censusReport = censusReport;
    }

    public List<LevelOfServiceReportDTO> getLevelOfServiceReport() {
        return levelOfServiceReport;
    }

    public void setLevelOfServiceReport(final List<LevelOfServiceReportDTO> levelOfServiceReport) {
        this.levelOfServiceReport = levelOfServiceReport;
    }

    public List<StudySubjectVisitReportDTO> getStudySubjectVisitReport() {
        return studySubjectVisitReport;
    }

    public void setStudySubjectVisitReport(final List<StudySubjectVisitReportDTO> studySubjectVisitReport) {
        this.studySubjectVisitReport = studySubjectVisitReport;
    }

    public List<SubjectVisitHistoryReportDTO> getSubjectVisitHistoryReport() {
        return subjectVisitHistoryReport;
    }

    public void setSubjectVisitHistoryReport(final List<SubjectVisitHistoryReportDTO> subjectVisitHistoryReport) {
        this.subjectVisitHistoryReport = subjectVisitHistoryReport;
    }

    public void setStudyStatusChangeReport(final List<StudyStatusChangeReportDTO> studyStatusChangeReport) {
        this.studyStatusChangeReport = studyStatusChangeReport;
    }

    public List<StudyStatusChangeReportDTO> getStudyStatusChangeReport() {
        return studyStatusChangeReport;
    }

    public List<CRCAvailabilityReportDTO> getCrcAvailabilityReport() {
        return crcAvailabilityReport;
    }

    public void setCrcAvailabilityReport(final List<CRCAvailabilityReportDTO> crcAvailabilityReport) {
        this.crcAvailabilityReport = crcAvailabilityReport;
    }

    public List<NutritionTasksReportDTO> getNutritionTasksReport() {
        return nutritionTasksReport;
    }

    public void setNutritionTasksReport(final List<NutritionTasksReportDTO> nutritionTasksReport) {
        this.nutritionTasksReport = nutritionTasksReport;
    }

    public List<VisitTerminatedPriorToCompletionDTO> getVisitTerminatedPriorToCompletion() {
        return visitTerminatedPriorToCompletion;
    }

    public void setVisitTerminatedPriorToCompletion(final List<VisitTerminatedPriorToCompletionDTO> visitTerminatedPriorToCompletion) {
        this.visitTerminatedPriorToCompletion = visitTerminatedPriorToCompletion;
    }

    public List<AncillaryOnlyByProtocolReportDTO> getAncillaryOnlyByProtocolReport() {
        return ancillaryOnlyByProtocolReport;
    }

    public void setAncillaryOnlyByProtocolReport(
            List<AncillaryOnlyByProtocolReportDTO> ancillaryOnlyByProtocolReport) {
        this.ancillaryOnlyByProtocolReport = ancillaryOnlyByProtocolReport;
    }

    public void setVisitsFlaggedResult(
            List<VisitsFlaggedEditReportDTO> visitsFlaggedResult) {
        this.visitsFlaggedResult = visitsFlaggedResult;
    }

    public void setStudyDataReport(List<StudyDataReportResponseDTO> studyDataReport) {
        this.studyDataReport = studyDataReport;
    }

    public void setCancellationsReport(List<CancellationsReportResponseDTO> cancellationsReport) {
        this.cancellationsReport = cancellationsReport;
    }

    public void setWorkloadAndResourcesReport(List<WorkloadAndResourceResponseDTO> resourceLevelOfServiceReport) {
        this.resourceLevelOfServiceReport = resourceLevelOfServiceReport;
    }

    public void setNursingAndRoomDailyOverviewReport(List<NursingAndRoomDailyOverviewReportDTO> nursingAndRoomDailyOverviewReport) {
        this.nursingAndRoomDailyOverviewReport = nursingAndRoomDailyOverviewReport;
    }
}
