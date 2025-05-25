/*
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
function sortSelectionChanged() {
    var sortWhichWay = $("#report_sortDirectionSelect").val();
    if (sortWhichWay == 1 || sortWhichWay == 2) {

        if (report_selectedReport.name == 'staff_audit_subject_views') {
            recent_reportUrl = "rest/report/getReport?name=staff_audit_subject_views" +
                '&sortId=' + sortWhichWay;
            report_getStaffAuditSubjectViewsReport(false);
        }
        else if (report_selectedReport.name == 'subject_audit_staff_views') {

        }
        else if (report_selectedReport.name == 'daily_resource') {
            recent_reportUrl = 'rest/report/getReport?name=daily_resource' +
                '&sortId=' + sortWhichWay;

            report_getDailyResourceReport(false);
        }
        else if (report_selectedReport.name == 'daily_adm') {
            recent_reportUrl = 'rest/report/getReport?name=daily_adm' +
                '&sortId=' + sortWhichWay;

            report_getDailyAdmReport(false);
        }
        else if (report_selectedReport.name == 'meta_kitchen') {
            recent_reportUrl = 'rest/report/getReport?name=meta_kitchen' +
                '&sortId=' + sortWhichWay;

            report_getMetaKitchenReport(false);
        }
        else if (report_selectedReport.name == 'meta_kitchen_by_time') {
            recent_reportUrl = 'rest/report/getReport?name=meta_kitchen_by_time' +
                '&sortId=' + sortWhichWay;

            report_getMetaKitchenByTimeReport(false);
        }
        else if (report_selectedReport.name == 'weekly_pharm') {
            recent_reportUrl = 'rest/report/getReport?name=weekly_pharm' +
                '&sortId=' + sortWhichWay;

            report_getWeeklyPharmReport(false);
        }
        else if (report_selectedReport.name == 'proto_by_nurse') {
            recent_reportUrl = "rest/report/getReport?name=proto_by_nurse" +
                '&sortId=' + sortWhichWay;
            report_getProtoNurseReport(false);
        }
        else if (report_selectedReport.name == 'proto_by_nutritionist') {
            recent_reportUrl = "rest/report/getReport?name=proto_nutritionist" +
                '&sortId=' + sortWhichWay;
            report_getProtoNutritionistReport(false);
        }
        else if (report_selectedReport.name == 'study_visit_location') {
            recent_reportUrl = "rest/report/getReport?name=study_visit_location" +
                '&sortId=' + sortWhichWay;
            report_getStudyVisitLocationReport(false);
        }
        else if (report_selectedReport.name == 'dept_and_pi') {
            recent_reportUrl = "rest/report/getReport?name=dept_and_pi" +
                '&sortId=' + sortWhichWay;
            report_getDeptAndPiReport(false);
        }
        else if (report_selectedReport.name == 'billing') {
            recent_reportUrl = "rest/report/getReport?name=billing" +
                '&sortId=' + sortWhichWay;
            report_getBillingReport(false);
        }
        else if (report_selectedReport.name == 'transactions') {
            recent_reportUrl = "rest/report/getReport?name=transactions" +
                '&sortId=' + sortWhichWay;
            report_getTransactionsReport(false);
        }
        else if (report_selectedReport.name == 'census') {
            recent_reportUrl = "rest/report/getReport?name=census" +
                '&sortId=' + sortWhichWay;
            report_getCensusReport(false);
        }
        else if (report_selectedReport.name == 'subject_purge') {
            recent_reportUrl = "rest/report/getReport?name=subject_purge" +
                '&sortId=' + sortWhichWay;
            report_getSubjectPurgeReport(false);
        }
        else if (report_selectedReport.name == 'override') {

            recent_reportUrl = "rest/report/getReport?name=override" +
                '&sortId=' + sortWhichWay;

            report_getOverrideReport(false);
        }
        else if (report_selectedReport.name == 'user_data') {
            recent_reportUrl = "rest/report/getReport?name=user_data" +
                '&sortId=' + sortWhichWay;

            report_getUserDataReport(false);
        }
        else if (report_selectedReport.name == 'daily_overview') {
            recent_reportUrl = "rest/report/getReport?name=daily_overview" +
                '&sortId=' + sortWhichWay;
        }
        else if (report_selectedReport.name == 'off_unit') {
            recent_reportUrl = "rest/report/getReport?name=off_unit" +
                '&sortId=' + sortWhichWay;
            report_getOffUnitReport(false);
        }
        else if (report_selectedReport.name == 'level_of_service') {
            recent_reportUrl = "rest/report/getReport?name=level_of_service" +
                '&sortId=' + sortWhichWay;
            report_getLevelOfServiceReport(false);
        }
        else if (report_selectedReport.name == 'study_subject_visit') {
            recent_reportUrl = "rest/report/getReport?name=study_subject_visit" +
                '&sortId=' + sortWhichWay;
            report_getStudySubjectVisitReport(false);
        }
        else if (report_selectedReport.name == 'study_status_change') {
            recent_reportUrl = "rest/report/getReport?name=study_status_change" +
                '&sortId=' + sortWhichWay;
            report_getStudyStatusChangeReport(false);
        }
        else if (report_selectedReport.name == 'subject_visit_history') {
            recent_reportUrl = "rest/report/getReport?name=subject_visit_history" +
                '&sortId=' + sortWhichWay;
            report_getSubjectVisitHistoryReport(false);
        }
        else if (report_selectedReport.name == 'crc_availability') {
            recent_reportUrl = 'rest/report/getReport?name=crc_availability' +
                '?sortId=' + sortWhichWay;
            report_getCRCAvailabilityReport(false);
        }
        else if (report_selectedReport.name == 'resource_level_of_service') {
            recent_reportUrl = 'rest/report/resourceLevelOfServiceReport' +
                '?sortId=' + sortWhichWay;
            report_getResourceLevelOfServiceReport(false);
        }
        else if (report_selectedReport.name == 'visit_duration_by_visit_type') {
            recent_reportUrl = 'rest/report/visit_duration_by_visit_type' +
                '?sortId=' + sortWhichWay;
            report_getVisitDurationByVisitTypeReport(false);
        }
        else if (report_selectedReport.name == 'visits_flagged_edit') {
            recent_reportUrl = "rest/report/getReport?name=visits_flagged_edit" +
                '&sortId=' + sortWhichWay;
            report_getVisitsFlaggedReport(false);
        }
        else if (report_selectedReport.name == 'visit_template_data') {
            recent_reportUrl = "rest/report/getReport?name=visit_template_data" +
                '&sortId=' + sortWhichWay;
            report_getVisitTemplateDataReport(false);
        }
        else if (report_selectedReport.name == 'ancillary_only_by_protocol') {
            recent_reportUrl = "rest/report/getReport?name=ancillary_only_by_protocol" +
                '&sortId=' + sortWhichWay;
            report_getAncillaryOnlyByProtocolReport(false);
        }
        else if (report_selectedReport.name == 'billable_resources') {
            recent_reportUrl = "rest/report/getReport?name=billable_resources" +
                '&sortId=' + sortWhichWay;
            report_getBillableResourcesReport(false);
        }
        else if (report_selectedReport.name == 'billing_by_investigator') {
            recent_reportUrl = "rest/report/getReport?name=billing_by_investigator" +
                '&sortId=' + sortWhichWay;
            report_getBillingByInvestigatorReport(false);
        }
        else if (report_selectedReport.name == 'booked_visit_service_level_by_visit_type') {
            recent_reportUrl =
                "rest/report/getReport?name=booked_visit_service_level_by_visit_type" +
                '&sortId=' + sortWhichWay;
            report_getBookedVisitServiceLevelByTypeReport(false);
        }
    }
}
