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

var reportPage = (function () {
    var initFn = function () {
        $.get("rest/app/getStaticDataForStandardReportPage", function (data) {
            var parsedData = $.parseJSON(data);
            var studyStatusFilterValues = parsedData.studyStatusFilterValues;
            // populate the study status menu options based on data received from the server
            // These menu options are used in the Study Data report
            studyStatusFilterOptions = buildSelectOptions(studyStatusFilterValues, 'label');
            commonInit();
        });
    };

    function commonInit() {
        loadMetaHeaders();
        renderReportData();
        initFooter();
        eraseLicense();
        app_runIdleTimer();
    }

    return {
        init: initFn
    };
}());


function reportData() {
    commonData();
    renderBreadcrumbs('report_screen');
    report_renderReportGrid();
}

var reportName;
var selectedid;
var search_string;
var report_visitsFlaggedResult;
var report_protoNurseResult;
var report_protoNutritionistResult;
var report_staffAuditSubjectViewsResult;
var report_subjectAuditStaffViewsResult;
var report_overrideResult;
var report_subjectPurgeResult;
var report_userDataResult;
var report_offUnitResult;
var report_dailyOverviewResult;
var report_weeklyPharmResult;

var report_cancellationsResult;
var report_studyDataResult;
var recent_reportUrl;

var report_studyVisitLocationResult;
var report_billableResourcesResult;
var report_transactionsResult;
var report_dailyResourceResult;
var report_dailyAdmResult;
var report_metaKitchenResult;
var report_metaKitchenByTimeResult;
var report_deptAndPiResult;
var report_billingResult;
var report_billingByInvestigatorResult;
var report_censusResult;
var report_levelOfServiceResult;
var report_studySubjectVisitResult;
var report_studyStatusChangeResult;
var report_subjectVisitHistoryResult;
var report_ancillaryOnlyByProtocolResult;
var report_crcAvailabilityResult;
var report_resourceLevelOfServiceResult;
var report_visitTemplateResult;
var report_nutritionTasksResult;
var report_visitDurationByVisitTypeResult;
var report_visitTerminatedPriorToCompletionResult;
var report_bookedVisitServiceLevelByTypeResult;
var report_roomAvailabilityCheckResult;
var exportStr = '';
var exportBIStr = '';
var exportCustomStr = '';

var report_csv = 'csv';

// These will be populated based on information received from the server
var studyStatusFilterOptions;

function renderReportData() {
    commonData();
    if (user.institutionRole.id == STUDY_STAFF) {
        window.location.href = "access_denied.html";
    }

    var reportJson = sessionStorage.getItem("reportData");
    report_selectedReport = JSON.parse(reportJson);
    renderReportBreadcrumbs(report_selectedReport.title);
    $('#report_reportTitle').html(report_selectedReport.title);
    $('#report_result_screen').css({display: "none"});

    $('.report_startDate').css({height: "0px"});
    $('.report_startDate').css({visibility: "hidden"});
    $('.report_endDate').css({height: "0px"});
    $('.report_endDate').css({visibility: "hidden"});

    $('.report_piForm').css({height: "0px"});
    $('.report_piForm').css({display: "none"});
    $('.report_subjectForm').css({height: "0px"});
    $('.report_subjectForm').css({display: "none"});
    $('.report_mrnForm').css({height: "0px"});
    $('.report_mrnForm').css({display: "none"});

    if (report_selectedReport.name == 'daily_overview' ||
        report_selectedReport.name == 'daily_resource') {

        $('#report_customdownloadButton').css({display: "inline-block"});
        var buttonVal = "Nutrition Export";
        report_selectedReport.name == 'daily_overview'
            ? buttonVal = "Nursing & Room Export" : buttonVal;
        document.getElementById("report_customdownloadButton").value = buttonVal;
    }
    else {
        $('#report_customdownloadButton').css({display: "none"});
    }

    if (report_selectedReport.name == 'meta_kitchen') {
        $('#report_flatloadButton').css({display: "inline-block"});
    }
    else {
        $('#report_flatloadButton').css({display: "none"});
    }




    $('#filter_functionality').css({display: "none"});
    $('#sort_functionality').css({display: "none"});
    report_clearForm();
    report_createWidgets();
    var today = new Date();
    var tomorrow = today.addDays(1);
    if (report_selectedReport.name == 'daily_overview' ||
        report_selectedReport.name == 'daily_adm' ||
        report_selectedReport.name == 'daily_resource') {
        var yesterday = today.subtractDays(1);
        $("#report_startDate").datepicker("setDate", yesterday);
    }
    else {
        var oneMonthAgo = new Date();
        oneMonthAgo.setMonth(today.getMonth() - 1);
        $("#report_startDate").datepicker("setDate", oneMonthAgo);
    }

    $("#report_endDate").datepicker("setDate", tomorrow);

    if (report_selectedReport.name == 'proto_by_nurse' ||
        report_selectedReport.name == 'proto_by_nutritionist' ||
        report_selectedReport.name == 'subject_purge' ||
        report_selectedReport.name == 'study_data' ||
        report_selectedReport.name == 'user_session' ||
        report_selectedReport.name == 'user_data' ||
        report_selectedReport.name == 'resource_level_of_service' ||
        report_selectedReport.name == 'visit_template_data') {

        $('.report_mrnForm').css({height: "0px"});
        $('.report_mrnForm').css({display: "none"});
        $('.report_subjectForm').css({height: "50px"});
        $('.report_subjectForm').css({display: "none"});
        $('.report_piForm').css({height: "50px"});
        $('.report_piForm').css({display: "none"});

        $('.report_startDate').css({height: "50px"});
        $('.report_startDate').css({visibility: "hidden"});
        $('.report_endDate').css({height: "50px"});
        $('.report_endDate').css({visibility: "hidden"});
    }
    else if (report_selectedReport.name == 'billing_by_investigator') {

        $('.report_piForm').css({height: "50px"});
        $('.report_piForm').show();
        $('.report_mrnForm').css({height: "0px"});
        $('.report_mrnForm').css({display: "none"});
        $('.report_subjectForm').css({height: "50px"});
        $('.report_subjectForm').css({display: "none"});

        $('.report_startDate').css({height: "50px"});
        $('.report_startDate').css({visibility: "visible"});
        $('.report_endDate').css({height: "50px"});
        $('.report_endDate').css({visibility: "visible"});
    }
    else if (report_selectedReport.name == 'subject_audit_staff_views') {
        $('.report_mrnForm').css({height: "50px"});
        $('.report_mrnForm').show();
        $('.report_subjectForm').css({height: "50px"});
        $('.report_subjectForm').css({display: "none"});
        $('.report_piForm').css({height: "50px"});
        $('.report_piForm').css({display: "none"});

        $('.report_startDate').css({height: "50px"});
        $('.report_startDate').css({visibility: "visible"});
        $('.report_endDate').css({height: "50px"});
        $('.report_endDate').css({visibility: "visible"});
    }
    else if (report_selectedReport.name == 'subject_visit_history' || report_selectedReport.name == 'study_subject_visit') {

        $('.report_piForm').css({height: "0px"});
        $('.report_piForm').css({display: "none"});
        $('.report_mrnForm').css({height: "0px"});
        $('.report_mrnForm').css({display: "none"});
        $('.report_subjectForm').css({height: "50px"});
        $('.report_subjectForm').show();

        $('.report_startDate').css({height: "50px"});
        $('.report_startDate').css({visibility: "hidden"});
        $('.report_endDate').css({height: "50px"});
        $('.report_endDate').css({visibility: "hidden"});
    }
    else {
        $('.report_piForm').css({height: "50px"});
        $('.report_piForm').css({display: "none"});
        $('.report_mrnForm').css({height: "0px"});
        $('.report_mrnForm').css({display: "none"});
        $('.report_subjectForm').css({height: "50px"});
        $('.report_subjectForm').css({display: "none"});
        $('.report_startDate').css({height: "50px"});
        $('.report_startDate').css({visibility: "visible"});
        $('.report_endDate').css({height: "50px"});
        $('.report_endDate').css({visibility: "visible"});
    }

    // afterthoughts for certain reports
    if (report_selectedReport.name == 'daily_resource') {
        $('.report_piForm').css({display: "none"});
        $('.report_mrnForm').css({display: "none"});
        $('.report_subjectForm').css({display: "none"});
    }

    setFilterControls(report_selectedReport.name);
    toggleFilterTextInputVisibility();

    if (report_selectedReport.name == 'proto_by_nurse') {
        $('#report_sortTitle').html("Protocol Nurse Last Name");
    }
    else if (report_selectedReport.name == 'proto_by_nutritionist') {
        $('#report_sortTitle').html("Protocol Nutritionist Last Name");
    }
    else if (report_selectedReport.name == 'daily_resource') {
        $('#report_sortTitle').html("Resource Name");
    }
    else if (report_selectedReport.name == 'daily_adm') {
        $('#report_sortTitle').html("Subject Last Name");
    }
    else if (report_selectedReport.name == 'meta_kitchen') {
        $('#report_sortTitle').html("Subject Last Name");
    }
    else if (report_selectedReport.name == 'meta_kitchen_by_time') {
        $('#report_sortTitle').html("Resource Start Time");
    }
    else if (report_selectedReport.name == 'weekly_pharm') {
        $('#report_sortTitle').html("Study Name");
    }
    else if (report_selectedReport.name == 'study_visit_location') {
        $('#report_sortTitle').html("Visit Name");
    }
    else if (report_selectedReport.name == 'dept_and_pi') {
        $('#report_sortTitle').html("PI Last Name");
    }
    else if (report_selectedReport.name == 'billing') {
        $('#report_sortTitle').html("Study Name");
    }
    else if (report_selectedReport.name == 'billable_resources') {
        $('#report_sortTitle').html("Resource Name");
    }
    else if (report_selectedReport.name == 'billing_by_investigator') {
        $('#report_sortTitle').html("PI Last Name");
    }
    else if (report_selectedReport.name == 'transactions') {
        $('#report_sortTitle').html("Appointment Status");
    }
    else if (report_selectedReport.name == 'census') {
        $('#report_sortTitle').html("Appointment Status Reason");
    }
    else if (report_selectedReport.name == 'subject_purge') {
        $('#report_sortTitle').html("Subject Last Name");
    }
    else if (report_selectedReport.name == 'override') {
        $('#report_sortTitle').html("Performing User ID");
    }
    else if (report_selectedReport.name == 'subject_audit_staff_views') {
        $('#report_sortTitle').html("");
    }
    else if (report_selectedReport.name == 'staff_audit_subject_views') {
        $('#report_sortTitle').html("Subject Last Name");
    }
    else if (report_selectedReport.name == 'level_of_service') {
        $('#report_sortTitle').html("Study Name");
    }
    else if (report_selectedReport.name == 'user_data') {
        $('#report_sortTitle').html("User Last Name");
    }
    else if (report_selectedReport.name == 'daily_overview') {
        $('#report_sortTitle').html("Scheduled Start Time");
    }
    else if (report_selectedReport.name == 'off_unit') {
        $('#report_sortTitle').html(" Subject Last Name");
    }
    else if (report_selectedReport.name == 'study_subject_visit') {
        $('#report_sortTitle').html(" Subject Last Name");
    }
    else if (report_selectedReport.name == 'study_status_change') {
        $('#report_sortTitle').html("Date of Status Change");
    }
    else if (report_selectedReport.name == 'subject_visit_history') {
        $('#report_sortTitle').html(" Scheduled Visit Start Time");
    }
    else if (report_selectedReport.name == 'crc_availability') {
        $('#filter_functionality').css({display: "none"});
        $('#sort_functionality').css({display: "none"});
    }
    else if (report_selectedReport.name == 'resource_level_of_service') {
        $('#filter_functionality').css({display: "none"});
        $('#report_sortTitle').html(" Visit Name");
    }
    else if (report_selectedReport.name == 'visit_template_data') {
        $('#filter_functionality').css({display: "none"});
        $('#report_sortTitle').html(" Local ID");
    }
    else if (report_selectedReport.name == 'visit_duration_by_visit_type') {
        $('#report_sortTitle').html(" Local ID");
    }
    else if (report_selectedReport.name == 'booked_visit_service_level_by_visit_type') {
        $('#report_sortTitle').html(" Visit Type");
    }
    else if (report_selectedReport.name == 'ancillary_only_by_protocol') {
        $('#report_sortTitle').html(" Local ID");
    }
    else {
        $('#report_sortTitle').html("");
    }

    $("#report_sortDirectionSelect").val(1);
    $("#report_filterText").val('');

    adjustDisplayOfSortAndFilter();
}

function setFilterControls(reportName) {
    $('#study_data_div').css({display: "none"});
    $('#cancellations_div').css({display: "none"});

    $("#report_filterSelect").html('');
    var html = [];
    switch (reportName) {
        case 'daily_resource':
            html[html.length] = "<option value='dailyresource_selectone'>Select One</option>";
            html[html.length] = "<option value='resource_name'>Resource Name</option>";
            html[html.length] = "<option value='subject_name'>Subject Last Name</option>";
            html[html.length] = "<option value='resource_group_nursing'>Resource Group: Nursing</option>";
            html[html.length] = "<option value='resource_group_nutrition'>Resource Group: Nutrition</option>";
            html[html.length] = "<option value='resource_group_room'>Resource Group: Room</option>";
            html[html.length] = "<option value='resource_group_lab'>Resource Group: Lab</option>";
            html[html.length] = "<option value='resource_group_other'>Resource Group: Other</option>";
            html[html.length] = "<option value='daily_resource_crc_approved'>CRC Approved: No</option>";
            break;
        case 'daily_adm':
            html[html.length] = "<option value='daily_adm_selectone'>Select One</option>";
            html[html.length] = "<option value='daily_adm_subject'>Subject Last Name</option>";
            html[html.length] = "<option value='daily_adm_local_id'>Local ID</option>";
            html[html.length] = "<option value='daily_adm_mrn'>MRN</option>";
            html[html.length] = "<option value='daily_adm_resource'>Resource Name</option>";
            html[html.length] = "<option value='daily_adm_crc_approved'>CRC Approved: No</option>";
            break;
        case 'meta_kitchen':
            html[html.length] = "<option value='metakitch_selectone'>Select One</option>";
            html[html.length] = "<option value='metakitch_subject'>Subject Last Name</option>";
            html[html.length] = "<option value='metakitch_local_id'>Local ID</option>";
            html[html.length] = "<option value='metakitch_mrn'>MRN</option>";
            html[html.length] = "<option value='metakitch_resource'>Resource Name</option>";
            html[html.length] = "<option value='metakitch_crc_approved'>CRC Approved: No</option>";
            break;
        case 'meta_kitchen_by_time':
            html[html.length] = "<option value='metakitchbytime_selectone'>Select One</option>";
            html[html.length] = "<option value='metakitchbytime_subject'>Subject Last Name</option>";
            html[html.length] = "<option value='metakitchbytime_local_id'>Local ID</option>";
            html[html.length] = "<option value='metakitchbytime_mrn'>MRN</option>";
            html[html.length] = "<option value='metakitchbytime_resource'>Resource Name</option>";
            html[html.length] = "<option value='metakitchbytime_crc_approved'>CRC Approved: No</option>";
            break;
        case 'weekly_pharm':
            html[html.length] = "<option value='weeklypharm_selectone'>Select One</option>";
            html[html.length] = "<option value='weeklypharm_local_id'>Local ID</option>";
            html[html.length] = "<option value='weeklypharm_study'>Study Name</option>";
            html[html.length] = "<option value='weeklypharm_subject'>Subject Last Name</option>";
            html[html.length] = "<option value='weeklypharm_sched_start_datetime'>Scheduled Visit Start Date/Time</option>";
            html[html.length] = "<option value='weeklypharm_crc_approved'>CRC Approved: No</option>";
            break;
        case 'proto_by_nurse':
            html[html.length] = "<option value='protonurse_selectone'>Select One</option>";
            html[html.length] = "<option value='proto_by_nurse'>Protocol Nurse Last Name</option>";
            html[html.length] = "<option value='protonurse_local_id'>Local ID</option>";
            html[html.length] = "<option value='protonurse_study_status'>Study Status</option>";
            break;
        case 'proto_by_nutritionist':
            html[html.length] = "<option value='protonutritionist_selectone'>Select One</option>";
            html[html.length] = "<option value='proto_nutritionist_last'>Protocol Nutritionist Last Name</option>";
            html[html.length] = "<option value='protonutritionist_local_id'>Local ID</option>";
            html[html.length] = "<option value='protonutritionist_study_status'>Study Status</option>";
            break;
        case 'study_visit_location':
            html[html.length] = "<option value='studyvisitlocation_selectone'>Select One</option>";
            html[html.length] = "<option value='studyvisitlocation_local_id'>Local ID</option>";
            html[html.length] = "<option value='studyvisitlocation_visit'>Visit Name</option>";
            html[html.length] = "<option value='studyvisitlocation_sublocation'>Sub-Location</option>";
            html[html.length] = "<option value='studyvisitlocation_crc_approved'>CRC Approved: No</option>";
            break;
        case 'dept_and_pi':
            html[html.length] = "<option value='deptandpi_selectone'>Select One</option>";
            html[html.length] = "<option value='deptandpi_department'>Department</option>";
            html[html.length] = "<option value='deptandpi_division'>Division</option>";
            html[html.length] = "<option value='deptandpi_investigator'>PI Last Name</option>";
            html[html.length] = "<option value='deptandpi_institution'>Institution</option>";
            html[html.length] = "<option value='deptandpi_credential'>Credential</option>";
            html[html.length] = "<option value='deptandpi_faculty_rank'>Faculty Rank</option>";
            html[html.length] = "<option value='deptandpi_crc_approved'>CRC Approved: No</option>";
            break;
        case 'billing':
            html[html.length] = "<option value='billing_selectone'>Select One</option>";
            html[html.length] = "<option value='billing_local_id'>Local ID</option>";
            html[html.length] = "<option value='billing_study'>Study Name</option>";
            html[html.length] = "<option value='billing_industry_initiated'>Industry Initiated: Yes</option>";
            html[html.length] = "<option value='billing_investigator'>PI Last Name</option>";
            html[html.length] = "<option value='billing_crc_approved'>CRC Approved: No</option>";
            break;
        case 'billable_resources':
            html[html.length] = "<option value='billable_resources_selectone'>Select One</option>";
            html[html.length] = "<option value='billable_resources_resource'>Resource Name</option>";
            html[html.length] = "<option value='billable_resources_study'>Study Name</option>";
            html[html.length] = "<option value='billable_resources_local_id'>Local ID</option>";
            html[html.length] = "<option value='billable_resources_investigator'>PI Last Name</option>";
            html[html.length] = "<option value='billable_resources_industry_initiated'>Industry Initiated: Yes</option>";
            html[html.length] = "<option value='billable_resources_crc_approved'>CRC Approved: No</option>";
            break;
        case 'billing_by_investigator':
            html[html.length] = "<option value='billingbyinvestigator_selectone'>Select One</option>";
            html[html.length] = "<option value='billingbyinvestigator_resource'>Resource Name</option>";
            html[html.length] = "<option value='billingbyinvestigator_study'>Study Name</option>";
            html[html.length] = "<option value='billingbyinvestigator_local_id'>Local ID</option>";
            html[html.length] = "<option value='billingbyinvestigator_investigator'>PI Last Name</option>";
            html[html.length] = "<option value='billingbyinvestigator_industry_initiated'>Industry Initiated: Yes</option>";
            html[html.length] = "<option value='billingbyinvestigator_crc_approved'>CRC Approved: No</option>";
            break;
        case 'transactions':
            html[html.length] = "<option value='transactions_selectone'>Select One</option>";
            html[html.length] = "<option value='transactions_study_visit_status'>Appointment Visit Status</option>";
            html[html.length] = "<option value='transactions_cancellation_type'>Cancellation Type</option>";
            html[html.length] = "<option value='transactions_cancellation_reason'>Cancellation Reason</option>";
            html[html.length] = "<option value='transactions_performing_user'>User Initiating Change Last Name</option>";
            break;
        case 'census':
            html[html.length] = "<option value='census_selectone'>Select One</option>";
            html[html.length] = "<option value='census_checkout_reason'>Check Out Reason</option>";
            html[html.length] = "<option value='census_investigator'>PI Last Name</option>";
            html[html.length] = "<option value='census_local_id'>Local ID</option>";
            html[html.length] = "<option value='census_study'>Study Name</option>";
            html[html.length] = "<option value='census_visit'>Visit Name</option>";
            html[html.length] = "<option value='census_crc_approved'>CRC Approved: No</option>";
            break;
        case 'subject_purge':
            html[html.length] = "<option value='subjectpurge_selectone'>Select One</option>";
            html[html.length] = "<option value='subjectpurge_subject'>Subject Last Name</option>";
            html[html.length] = "<option value='subjectpurge_mrn'>MRN</option>";
            html[html.length] = "<option value='subjectpurge_performing_user'>Creating User</option>";
            break;
        case 'override':
            html[html.length] = "<option value='override_selectone'>Select One</option>";
            html[html.length] = "<option value='override_performing_user'>Performing User Id</option>";
            html[html.length] = "<option value='override_visit'>Visit Name</option>";
            html[html.length] = "<option value='override_local_id'>Local ID</option>";
            html[html.length] = "<option value='or_change_resource'>Override Reason - Change of Resource</option>";
            html[html.length] = "<option value='or_change_rn'>Override (resource otherwise not available) Approved by RN Resource Manager</option>";
            html[html.length] = "<option value='or_change_rd'>Override (resource otherwise not available) Approved by RD Resource Manager</option>";
            html[html.length] = "<option value='or_change_other'>Override (resource otherwise not available) Approved by Other Resource Manager</option>";
            html[html.length] = "<option value='or_ol_rn'>Overlap (double booking) Approved by RN Resource Manager</option>";
            html[html.length] = "<option value='or_ol_rd'>Overlap (double booking) Approved by RD Resource Manager</option>";
            html[html.length] = "<option value='or_ol_other'>Overlap (double booking) Approved by Other Resource Manager</option>";
            break;
        case 'subject_audit_staff_views':
            html[html.length] = "<option value='subjectaudit_selectone'>Select One</option>";
            html[html.length] = "<option value='subject_user_name'>User Last Name</option>";
            break;
        case 'staff_audit_subject_views':
            html[html.length] = "<option value='staffaudit_selectone'>Select One</option>";
            html[html.length] = "<option value='staff_user_name'>User Last Name</option>";
            html[html.length] = "<option value='staff_user_id'>User ID</option>";
            break;

        case 'study_data':
            $('#study_data_div').css({display: "block"});
            $('#old_filters').css({display: "none"});
            $('#sort_functionality').css({display: "none"});
            $('#study_data_study_status').html(studyStatusFilterOptions);

            break;
        case 'cancellations':
            $('#cancellations_div').css({display: "block"});
            $('#old_filters').css({display: "none"});
            $('#sort_functionality').css({display: "none"});
            break;

        case 'user_data':
            html[html.length] = "<option value='user_data_selectone'>Select One</option>";
            html[html.length] = "<option value='user_data_user_name'>User Last Name</option>";
            html[html.length] = "<option value='user_data_inst_role'>Institution Role</option>";
            html[html.length] = "<option value='user_data_user_title'>User Title</option>";
            html[html.length] = "<option value='user_data_status_active'>Status: Active</option>";
            html[html.length] = "<option value='user_data_status_not_active'>Status: Not Active</option>";
            break;
        case 'daily_overview':
            html[html.length] = "<option value='daily_overview_selectone'>Select One</option>";
            html[html.length] = "<option value='daily_overview_local_id'>Local ID</option>";
            html[html.length] = "<option value='daily_overview_visit'>Visit Name</option>";
            html[html.length] = "<option value='daily_overview_subject'>Subject Last Name</option>";
            html[html.length] = "<option value='daily_overview_mrn'>MRN</option>";
            html[html.length] = "<option value='daily_overview_is_part_off_unit'>Is Part of Visit Off Unit?</option>";
            html[html.length] = "<option value='daily_overview_resource_type'>Resource Type</option>";
            html[html.length] = "<option value='daily_overview_sublocation'>Sublocation</option>";
            break;
        case 'off_unit':
            html[html.length] = "<option value='off_unit_selectone'>Select One</option>";
            html[html.length] = "<option value='off_unit_visit'>Visit Name</option>";
            html[html.length] = "<option value='off_unit_local_id'>Local ID</option>";
            html[html.length] = "<option value='off_unit_last_name'>Subject Last Name</option>";
            html[html.length] = "<option value='off_unit_mrn'>MRN</option>";
            html[html.length] = "<option value='off_unit_visit_type'>Visit Type</option>";
            html[html.length] = "<option value='off_unit_crc_approved'>CRC Approved: No</option>";
            html[html.length] = "<option value='off_unit_group_nursing'>Resource Group: Nursing</option>";
            html[html.length] = "<option value='off_unit_group_nutrition'>Resource Group: Nutrition</option>";
            html[html.length] = "<option value='off_unit_group_room'>Resource Group: Room</option>";
            html[html.length] = "<option value='off_unit_group_lab'>Resource Group: Lab</option>";
            html[html.length] = "<option value='off_unit_group_other'>Resource Group: Other</option>";
            break;
        case 'level_of_service':
            html[html.length] = "<option value='levelofservice_selectone'>Select One</option>";
            html[html.length] = "<option value='levelofservice_sl_nursing'>Nursing</option>";
            html[html.length] = "<option value='levelofservice_sl_nutrition'>Nutrition</option>";
            html[html.length] = "<option value='levelofservice_sl_processing'>Processing</option>";
            html[html.length] = "<option value='levelofservice_sl_setup'>Setup</option>";
            html[html.length] = "<option value='levelofservice_visit_type'>Visit Type</option>";
            html[html.length] = "<option value='levelofservice_appt_status'>Appointment Status</option>";
            html[html.length] = "<option value='levelofservice_checkout_reason'>Check Out Reason</option>";
            html[html.length] = "<option value='levelofservice_crc_approved'>CRC Approved: No</option>";
            break;
        case 'study_subject_visit':
            html[html.length] = "<option value='studysubjectvisit_selectone'>Select One</option>";
            html[html.length] = "<option value='studysubjectvisit_localId'>Local ID</option>";
            html[html.length] = "<option value='studysubjectvisit_appointment'>Appointment Status</option>";
            html[html.length] = "<option value='studysubjectvisit_crc_approved'>CRC Approved: No</option>";
            break;
        case 'subject_visit_history':
            html[html.length] = "<option value='subjectvisithistory_selectone'>Select One</option>";
            html[html.length] = "<option value='subjectvisithistory_localId'>Local ID</option>";
            html[html.length] = "<option value='subjectvisithistory_crc_approved'>CRC Approved: No</option>";
            break;
        case 'study_status_change':
            html[html.length] = "<option value='studystatuschange_selectone'>Select One</option>";
            html[html.length] = "<option value='studystatuschange_local_id'>Local ID</option>";
            html[html.length] = "<option value='studystatuschange_investigator'>PI Last Name </option>";
            html[html.length] = "<option value='studystatuschange_study_status'>Study Status</option>";
            html[html.length] = "<option value='studystatuschange_crc_approved'>CRC Approved: No</option>";
            break;
        case 'crc_availability':
            html[html.length] = "<option value='crcavailability_selectone'>Select One</option>";
            break;
        case 'resource_level_of_service':
            html[html.length] = "<option value='resourcelevelofservice_selectone'>Select One</option>";
            html[html.length] = "<option value='resourcelevelofservice_localID'>Local ID</option>";
            html[html.length] = "<option value='resourcelevelofservice_visitName'>Visit Name</option>";
            break;
        case 'visit_template_data':
            html[html.length] = "<option value='visittemplatedata_selectone'>Select One</option>";
            html[html.length] = "<option value='visittemplatedata_localID'>Local ID</option>";
            html[html.length] = "<option value='visittemplatedata_visitName'>Visit Name</option>";
            break;
        case 'visit_duration_by_visit_type':
            html[html.length] = "<option value='visitdurationbyvisittype_selectone'> Select One </option>";
            html[html.length] = "<option value='visitdurationbyvisittype_visitname'>Visit Name</option>";
            html[html.length] = "<option value='visitdurationbyvisittype_visittype'>Visit Type</option>";
            break;
        case 'booked_visit_service_level_by_visit_type':
            html[html.length] = "<option value='bookedvisitservicelevelbyvisittype_selectone'> Select One </option>";
            html[html.length] = "<option value='bookedvisitservicelevelbyvisittype_level0'>Level 0</option>";
            html[html.length] = "<option value='bookedvisitservicelevelbyvisittype_level1'>Level 1</option>";
            html[html.length] = "<option value='bookedvisitservicelevelbyvisittype_level2'>Level 2</option>";
            html[html.length] = "<option value='bookedvisitservicelevelbyvisittype_level3'>Level 3</option>";
            html[html.length] = "<option value='bookedvisitservicelevelbyvisittype_level4'>Level 4</option>";
            html[html.length] = "<option value='bookedvisitservicelevelbyvisittype_level5'>Level 5</option>";
            html[html.length] = "<option value='bookedvisitservicelevelbyvisittype_level6'>Level 6</option>";
            html[html.length] = "<option value='bookedvisitservicelevelbyvisittype_level7'>Level 7</option>";
            break;
        case 'ancillary_only_by_protocol':
            html[html.length] = "<option value='ancillaryonlybyprotocol_selectone'> Select One </option>";
            html[html.length] = "<option value='ancillaryonlybyprotocol_localID'>Local ID </option>";
            html[html.length] = "<option value='ancillaryonlybyprotocol_crc_approved'>CRC Approved: No</option>";
            break;
    }

    var result = html.join('');
    $("#report_filterSelect").html(result);
}

function filterSelectionChanged() {
    //code to check if the selected value is resource type
    if ($("#report_filterSelect option:selected").val() == 'daily_overview_resource_type') {
        var html = [];
        $('#overview_resource_type').css({display: ''});
        var jsonData = null;
        $.post("rest/report/loadResourceTypes", {data: jsonData}, function (data) {
            parsedData = $.parseJSON(data);
            var resourceTypes = parsedData.resourceTypes;
            html[html.length] = "<option value=''>Select Resource Type</option>";
            $.each(resourceTypes, function (i, item) {
                html[html.length] = "<option value=" + item + ">" + item + "</option>";

            });
            var result = html.join('');
            $("#overview_resource_type").html(result);
        });
    }
    else if ($("#report_filterSelect option:selected").val() == 'daily_overview_sublocation') {
        var html = [];
        $('#overview_sublocation').css({display: ''});
        var jsonData = null;
        $.post("rest/report/loadSublocations", {data: jsonData}, function (data) {
            parsedData = $.parseJSON(data);
            var sublocations = parsedData.sublocations;
            html[html.length] = "<option value=''>Select Sublocation</option>";
            $.each(sublocations, function (i, item) {
                var itemName = item.name;
                html[html.length] = "<option value='" + itemName + "'>" + itemName + "</option>";

            });
            var result = html.join('');
            $("#overview_sublocation").html(result);
        });
    }
    else {
        $('#overview_resource_type').css({display: 'none'});
        $('#overview_sublocation').css({display: 'none'});
    }
    toggleFilterTextInputVisibility();
}

function getStartTime() {
    return $("#report_startDate").datepicker('getDate').valueOf();
}

function getEndTime() {
    return $("#report_endDate").datepicker('getDate').valueOf();
}

function toggleFilterTextInputVisibility() {
    switch ($("#report_filterSelect option:selected").val()) {
        case 'resource_group_nursing':
        case 'resource_group_nutrition':
        case 'resource_group_room':
        case 'resource_group_lab':
        case 'resource_group_other':
        case 'off_unit_group_nursing':
        case 'off_unit_group_nutrition':
        case 'off_unit_group_room':
        case 'off_unit_group_lab':
        case 'off_unit_group_other':
        case 'daily_adm_crc_approved':
        case 'daily_resource_crc_approved':
        case 'billable_resources_crc_approved':
        case 'billable_resources_industry_initiated':
        case 'billing_crc_approved':
        case 'billing_industry_initiated':
        case 'billingbyinvestigator_crc_approved':
        case 'billingbyinvestigator_industry_initiated':
        case 'census_crc_approved':
        case 'deptandpi_crc_approved':
        case 'levelofservice_crc_approved':
        case 'metakitch_crc_approved':
        case 'metakitchbytime_crc_approved':
        case 'weeklypharm_crc_approved':
        case 'off_unit_crc_approved':
        case 'nutritiontasks_crc_approved':
        case 'studyvisitlocation_crc_approved':
        case 'studystatuschange_crc_approved':
        case 'studysubjectvisit_crc_approved':
        case 'subjectvisithistory_crc_approved':
        case 'ancillaryonlybyprotocol_crc_approved':
        case 'or_change_resource':
        case 'or_change_rn':
        case 'or_change_rd':
        case 'or_change_other':
        case 'or_ol_rn':
        case 'or_ol_rd':
        case 'or_ol_other':
        case 'user_data_status_active':
        case 'user_data_status_not_active':
        case 'bookedvisitservicelevelbyvisittype_level0':
        case 'bookedvisitservicelevelbyvisittype_level1':
        case 'bookedvisitservicelevelbyvisittype_level2':
        case 'bookedvisitservicelevelbyvisittype_level3':
        case 'bookedvisitservicelevelbyvisittype_level4':
        case 'bookedvisitservicelevelbyvisittype_level5':
        case 'bookedvisitservicelevelbyvisittype_level6':
        case 'bookedvisitservicelevelbyvisittype_level7':
            $('#report_filterText').css({display: "none"});
            $('#report_filterButton').attr("value", "Filter");
            $('#report_filterButton').css({display: "inline"});
            break;
        case 'subjectaudit_selectone':
        case 'staffaudit_selectone':
        case 'dailyresource_selectone':
        case 'daily_adm_selectone':
        case 'metakitch_selectone':
        case 'metakitchbytime_selectone':
        case 'weeklypharm_selectone':
        case 'protonurse_selectone':
        case 'protonutritionist_selectone':
        case 'studyvisitlocation_selectone':
        case 'deptandpi_selectone':
        case 'billing_selectone':
        case 'billable_resources_selectone':
        case 'billingbyinvestigator_selectone':
        case 'transactions_selectone':
        case 'census_selectone':
        case 'subjectpurge_selectone':
        case 'override_selectone':
        case 'user_data_selectone':
        case 'daily_overview_selectone':
        case 'off_unit_selectone':
        case 'levelofservice_selectone':
        case 'studysubjectvisit_selectone':
        case 'studystatuschange_selectone':
        case 'subjectvisithistory_selectone':
        case 'crcavailability_selectone':
        case 'resourcelevelofservice_selectone':
        case 'visittemplatedata_selectone':
        case 'nutritiontasks_selectone':
        case 'visitdurationbyvisittype_selectone':
        case 'bookedvisitservicelevelbyvisittype_selectone':
        case 'ancillaryonlybyprotocol_selectone':
        case 'visitterminatedpriortocompletion_selectone':
            $('#report_filterText').css({display: "none"});
            $('#report_filterButton').css({display: "none"});
            break;
        default:
            $('#report_filterText').css({display: "inline"});
            $('#report_filterButton').attr("value", "Filter");
            $('#report_filterButton').css({display: "inline"});
            break;
    }
    if ($("#report_filterSelect option:selected").val() == 'daily_overview_resource_type'
        || $("#report_filterSelect option:selected").val() == 'daily_overview_sublocation') {
        $('#report_filterText').css({display: "none"});
    }

}
function handleReportErrorFromBackEnd(xhr, status, error) {
    clearMiscMessages();
    $('#report_results').html('');

    $('#report_errorMessage').show();
}

function studyFilterSelectionChanged() {
    var ajaxUrl = "rest/report/studyDataReport" +
        "?sortStrategy=" + $.trim($("#study_data_sort_direction option:selected").val());

    fundingValue = $.trim($("#study_data_funding_source option:selected").val());
    studyStatus = $.trim($("#study_data_study_status option:selected").val());
    localId = $.trim($("#study_data_local_id").val());
    name = $.trim($("#study_data_study_name").val());

    if (fundingValue != 0) ajaxUrl += "&fundingSource=" + fundingValue;
    ajaxUrl += "&studyStatus=" + studyStatus;
    if (localId)           ajaxUrl += "&localId=" + localId;
    if (name)              ajaxUrl += "&name=" + name;

    $('#report_loadMessage').css({display: "block"});
    $('#report_emptyMessage').css({display: "none"});
    $('#report_errorMessage').css({display: "none"});
    $('#report_result_screen').css({display: "block"});

    recent_reportUrl = ajaxUrl;
    report_getStudyDataReport(false);
}

function cancellationFilterSelectionChanged() {
    var ajaxUrl = timedUrl("rest/report/cancellationsReport") + "&sortStrategy=" +
        $.trim($("#cancellations_sort_direction option:selected").val());

    localId = $.trim($("#cancellations_local_id").val());
    name = $.trim($("#cancellations_pi_name").val());
    type = $.trim($("#cancellations_type option:selected").val());
    reason = $.trim($("#cancellationReasons").val());
    noApprove = $.trim($("#cancellations_crcApproved option:selected").val());

    if (localId)         ajaxUrl += "&localId=" + localId;
    if (name)            ajaxUrl += "&name=" + name;
    if (type != 0)       ajaxUrl += "&type=" + type;
    if (reason != 0)     ajaxUrl += "&reason=" + reason;
    if (noApprove != 0)  ajaxUrl += "&noApprove=" + noApprove;

    $('#report_loadMessage').css({display: "block"});
    $('#report_emptyMessage').css({display: "none"});
    $('#report_errorMessage').css({display: "none"});
    $('#report_result_screen').css({display: "block"});

    recent_reportUrl = ajaxUrl;
    report_getCancellationsReport(false);
}


function report_createWidgets() {
    WidgetUtil.createDatepicker("#report_startDate", {
        onSelect: function (selectedDate) {
            $("#report_endDate").datepicker("option", "minDate", selectedDate);
        }
    });

    WidgetUtil.createDatepicker("#report_endDate");
    $("#report_startDate").datepicker("setDate", new Date());
    $("#report_endDate").datepicker("setDate", new Date());
}

function report_renderReportGrid() {
    commonData();
    $.getJSON("rest/report/getReports", function (data) {
        var iteration = 0;

        var out =
            " <table id='reports' class='hoverable'>" +
            "  <tr>" +
            "   <td><strong>Name</strong></td> " +
            "   <td><strong>Description</strong></td>" +
            "  </tr> ";

        $.each(data.reports, function (key, val) {
            out += "<tr onclick='getReportSelectedRowId(" + val.id + ")'>"
            out += "   <td>" + val.title + "</td>" +
                "      <td>" + val.description + "</td>" +
                " </tr>";
            iteration++;
        });

        out += "  </table>";

        $('#reportDataTable').html(out);
    });
}

function clearMiscMessages() {
    $('#report_loadMessage').css({display: "none"});
    $('#report_emptyMessage').css({display: "none"});
    $('#report_errorMessage').css({display: "none"});
}
function getReportSelectedRowId(id) {
    $.getJSON("rest/report/getReportData?id=" + id, function (data) {
        sessionStorage.setItem("reportData", JSON.stringify(data));
        window.location.href = "report_screen.html";

        recent_reportUrl = undefined;
        clearMiscMessages();
    });
}

function submitActionAndDataForCsv(actionUrl, jsonData, csvOutput, csvName) {

    if (actionUrl === undefined || jsonData == undefined) {
        return;
    }

    var separator = (actionUrl.indexOf('?') === -1) ? '?' : '&';
    actionUrl = actionUrl + separator + "output=" + csvOutput;

    fileSaverSubmit(actionUrl, 'data=' +  jsonData, csvName );
}

function fileSaverSubmit(actionUrl, data, csvName) {

    clearMiscMessages();
    $('#report_loadMessage').css({display: "block"});

    var request = new XMLHttpRequest();
    request.onreadystatechange = function () {
        if (request.readyState == 4 && request.status == 200) {
            try {
                var csvData = request.responseText;
                var fileName = csvName + ".csv";

                var blob = new Blob([csvData], {type: "application/octet-binary"});

                var size = csvData.length;
                console.log('Downloaded file: ' + fileName + ', size: ' + size);
                if (size > 500 * 1000 * 1000) {
                    throw size + ' is too many bytes to download';
                }

                saveAs(blob, fileName);

                $('#report_loadMessage').css({display: "none"});
            }
            catch(e) {
                console.log(e);
                handleReportErrorFromBackEnd();
            }
        }
        else if (request.readyState == 4) {
            handleReportErrorFromBackEnd();
        }
    };
    request.open('POST', actionUrl);
    request.setRequestHeader("Content-Type", "application/x-www-form-urlencoded");
    request.send(data);
}

function adjustDisplayOfSortAndFilter() {
    if (report_selectedReport.name == 'crc_availability' ||
        report_selectedReport.name == 'visits_flagged_edit') {

        $('#filter_functionality').css({display: "none"});
    }
    else {
        $('#filter_functionality').css({display: "inline-block"});
    }

    if (report_selectedReport.name == 'subject_audit_staff_views'
        || report_selectedReport.name == 'crc_availability'
        || report_selectedReport.name == 'visits_flagged_edit'

        || report_selectedReport.name == 'study_data'
        || report_selectedReport.name == 'cancellations'

    ) {
        $('#sort_functionality').css({display: "none"});
    }
    else {
        $('#sort_functionality').css({display: "inline-block"});
    }

    if (report_selectedReport.name == 'cancellations') {
        $.get("rest/app/getStaticLists", {}, function (data) {
            var parsedData = $.parseJSON(data);
            var cancellationReasons = parsedData.cancellationReasons;
            var cancellationReasonSelectOptions =
                buildSelectOptions(cancellationReasons, 'name', 'choose');
            $("#cancellationReasons").html(cancellationReasonSelectOptions);
        });
    }
}

function validateReportSubmission() {
    var isValid = true;
    $('.formElementRequired').css({visibility: "hidden"});

    if (report_selectedReport.name != 'proto_by_nurse' &&
        report_selectedReport.name != 'proto_by_nutritionist' &&
        report_selectedReport.name != 'subject_purge' &&
        report_selectedReport.name != 'study_data' &&
        report_selectedReport.name != 'user_session' &&
        report_selectedReport.name != 'user_data' &&
        report_selectedReport.name != 'resource_level_of_service' &&
        report_selectedReport.name != 'visit_template_data') {

        var report_startDateVal = $("#report_startDate").datepicker('getDate');
        var report_endDateVal = $("#report_endDate").datepicker('getDate');

        if (report_startDateVal == null) {
            showError('#report_startDateValidation', 'Please enter valid start date');
            isValid = false;
        }
        if (report_endDateVal == null) {
            showError('#report_endDateValidation', 'Please enter valid end date');
            isValid = false;
        }
        if (report_startDateVal > report_endDateVal) {
            showError('#report_startDateValidation', 'Please enter valid date range');
            showError('#report_endDateValidation', 'Please enter valid date range');
            isValid = false;
        }
    }
    if (report_selectedReport.name == 'subject_audit_staff_views') {
        if ($.trim($("#report_mrn").val()).length < 1) {
            showError('#report_mrnValidation');
            isValid = false;
        }
    }

    if (report_selectedReport.name == 'subject_visit_history' ||
        report_selectedReport.name == 'study_subject_visit') {

        if (($.trim($("#report_subjectLastName").val()).length < 1) &&
            ($.trim($("#report_subjectMRN").val()).length < 1) &&
            ($.trim($("#report_subjectDOB").val()).length < 1)) {

            util_showMainMessage("Please enter value for at least one of the fields.");
            isValid = false;
        }
    }

    return isValid;
}

function submitReport(toCsv) {
    if (! validateReportSubmission()) {
        return;
    }

    if (toCsv !== 'csv') {
        $("#report_sortDirectionSelect").val(1);
    }

    $('#report_result_screen').css({display: "block"});

    if (report_selectedReport.name == 'user_data') {
        report_getUserDataReport(toCsv);
    }
    else if (report_selectedReport.name == 'weekly_pharm') {
        report_getWeeklyPharmReport(toCsv);
    }
    else if (report_selectedReport.name == 'study_visit_location') {
        report_getStudyVisitLocationReport(toCsv);
    }
    else if (report_selectedReport.name == 'transactions') {
        report_getTransactionsReport(toCsv);
    }
    else if (report_selectedReport.name == 'daily_resource') {
        report_getDailyResourceReport(toCsv);
    }
    else if (report_selectedReport.name == 'daily_adm') {
        report_getDailyAdmReport(toCsv);
    }
    else if (report_selectedReport.name == 'meta_kitchen') {
        report_getMetaKitchenReport(toCsv);
    }
    else if (report_selectedReport.name == 'meta_kitchen_by_time') {
        report_getMetaKitchenByTimeReport(toCsv);
    }
    else if (report_selectedReport.name == 'dept_and_pi') {
        report_getDeptAndPiReport(toCsv);
    }
    else if (report_selectedReport.name == 'billing') {
        report_getBillingReport(toCsv);
    }
    else if (report_selectedReport.name == 'census') {
        report_getCensusReport(toCsv);
    }
    else if (report_selectedReport.name == 'level_of_service') {
        report_getLevelOfServiceReport(toCsv);
    }
    else if (report_selectedReport.name == 'study_subject_visit') {
        report_getStudySubjectVisitReport(toCsv);
    }
    else if (report_selectedReport.name == 'study_status_change') {
        report_getStudyStatusChangeReport(toCsv);
    }
    else if (report_selectedReport.name == 'subject_visit_history') {
        report_getSubjectVisitHistoryReport(toCsv);
    }
    else if (report_selectedReport.name == 'crc_availability') {
        report_getCRCAvailabilityReport(toCsv);
    }
    else if (report_selectedReport.name == 'resource_level_of_service') {
        report_getResourceLevelOfServiceReport(toCsv);
    }
    else if (report_selectedReport.name == 'visit_template_data') {
        report_getVisitTemplateDataReport(toCsv);
    }
    else if (report_selectedReport.name == 'visit_duration_by_visit_type') {
        report_getVisitDurationByVisitTypeReport(toCsv);
    }
    else if (report_selectedReport.name == 'booked_visit_service_level_by_visit_type') {
        report_getBookedVisitServiceLevelByTypeReport(toCsv);
    }
    else if (report_selectedReport.name == 'ancillary_only_by_protocol') {
        report_getAncillaryOnlyByProtocolReport(toCsv);
    }
    else if (report_selectedReport.name == 'visits_flagged_edit') {
        report_getVisitsFlaggedReport(toCsv);
    }
    else if (report_selectedReport.name == 'daily_overview') {
        report_getDailyOverviewReport(toCsv);
    }
    else if (report_selectedReport.name == 'off_unit') {
        report_getOffUnitReport(toCsv);
    }
    else if (report_selectedReport.name == 'subject_purge') {
        report_getSubjectPurgeReport(toCsv);
    }
    else if (report_selectedReport.name == 'override') {
        report_getOverrideReport(toCsv);
    }
    else if (report_selectedReport.name == 'subject_audit_staff_views') {
        report_getSubjectAuditStaffViewsReport(toCsv);
    }
    else if (report_selectedReport.name == 'staff_audit_subject_views') {
        report_getStaffAuditSubjectViewsReport(toCsv);
    }
    else if (report_selectedReport.name == 'proto_by_nutritionist') {
        report_getProtoNutritionistReport(toCsv);
    }
    else if (report_selectedReport.name == 'proto_by_nurse') {
        report_getProtoNurseReport(toCsv);
    }
    else if (report_selectedReport.name == 'study_data') {
        report_getStudyDataReport(toCsv);
    }
    else if (report_selectedReport.name == 'cancellations') {
        report_getCancellationsReport(toCsv);
    }
    else if (report_selectedReport.name == 'billable_resources') {
        report_getBillableResourcesReport(toCsv);
    }
    else if (report_selectedReport.name == 'billing_by_investigator') {
        report_getBillingByInvestigatorReport(toCsv);
    }
}
function downloadCustomReport(toCsv) {
    if (report_selectedReport.name == 'daily_overview') {
        report_getExportDailyOverviewReport(toCsv);
    }
    if (report_selectedReport.name == 'daily_resource') {
        report_getExportDailyResourceReport(toCsv);
    }
}
function downloadFlatReport(event) {
    event.preventDefault();

    report_getMetaKitchenFlatReport(report_csv);
}


function report_clearForm() {
    $('#report_piFirstName').val('');
    $('#report_piLastName').val('');
    $('#report_mrn').val('');
    $('#report_subjectLastName').val('');
    $('#report_subjectMRN').val('');
    $('#report_subjectDOB').val('');
    $('.formElementRequired').css({visibility: "hidden"});
}

function report_handleDataResponse(rowCount) {
    clearMiscMessages();

    if (rowCount < 1) {
        $('#report_emptyMessage').css({display: "block"});
    }
}