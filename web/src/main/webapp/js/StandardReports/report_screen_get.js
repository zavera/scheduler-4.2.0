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
function postShow(filterSelect) {
    $('#report_filterButton').css({display: "inline"});
    $('#report_filterButton').attr("value", "Reset");
    $("#report_filterText").val('');
    $('#report_filterSelect').val(filterSelect);
    $('#report_filterText').css({display: "none"});
    selectedid = 0;
}

function report_getReportForDisplayOrCsv(
    defaultUrl,
    jsonData,
    reportResultVarName,
    jsonResultVarName,
    resultShowFn,
    toCsv,
    postShowFilter,
    override_url) {

    try {
        // recent url might have some filtering options
        if (recent_reportUrl === undefined) {
            recent_reportUrl = defaultUrl;
        }

        // ignore recent url if override is present
        if (override_url === undefined) {
            url = recent_reportUrl;
        }
        else {
            url = override_url;
        }

        if (toCsv) {
            submitActionAndDataForCsv(url, jsonData, toCsv, jsonResultVarName);
        }
        else {
            $('#report_results').html('');

            clearMiscMessages();
            $('#report_loadMessage').css({display: "block"});
            $.post(url, {data: jsonData}, function(data) {
                handleReportData(data, reportResultVarName, jsonResultVarName, resultShowFn, postShowFilter)
            })
                .fail(handleReportErrorFromBackEnd);
        }
    }
    catch(e) {
        console.log(e);
        handleReportErrorFromBackEnd();
    }
}

function handleReportData(data, reportResultVarName, jsonResultVarName, resultShowFn, postShowFilter) {
    try {
        $('#report_loadMessage').css({display: "none"});
        var parsedData = JSON.parse(data);
        window[reportResultVarName] = parsedData[jsonResultVarName];
        resultShowFn();
        if (postShowFilter !== undefined) {
            postShow(postShowFilter);
        }
    }
    catch(e) {
        console.log(e);
        handleReportErrorFromBackEnd();
    }
}

function timedUrl(url) {
    startTime = getStartTime();
    endTime = getEndTime();

    url += "?startTime=" + startTime;
    url += "&endTime=" + endTime;

    return url;
}

/////////////////////////////////////////////////////

function report_getBillingReport(toCsv, postShowFilter) {
    report_getReportForDisplayOrCsv(
        "rest/report/getReport?name=billing",
        JSON.stringify({
            startTime: getStartTime(),
            endTime: getEndTime()}),
        'report_billingResult',
        'billingReport',
        report_showBillingResults,
        toCsv,
        postShowFilter);
}

function report_getUserDataReport(toCsv, postShowFilter) {
    report_getReportForDisplayOrCsv(
        "rest/report/getReport?name=user_data",
        "{}",
        'report_userDataResult',
        'userDataReport',
        report_showUserDataResults,
        toCsv,
        postShowFilter);
}

function report_getStudyDataReport(toCsv, postShowFilter) {
    report_getReportForDisplayOrCsv(
        "rest/report/studyDataReport",
        "{}",
        'report_studyDataResult',
        'studyDataReport',
        report_showStudyDataResults,
        toCsv,
        postShowFilter);
}

function report_getCancellationsReport(toCsv, postShowFilter) {
    var ajaxUrl = timedUrl("rest/report/cancellationsReport");
    report_getReportForDisplayOrCsv(
        ajaxUrl,
        "{}",
        'report_cancellationsResult',
        'cancellationsReport',
        report_showCancellationsResults,
        toCsv,
        postShowFilter);
}

function report_getResourceLevelOfServiceReport(toCsv, postShowFilter) {
    report_getReportForDisplayOrCsv(
        "rest/report/resourceLevelOfServiceReport",
        "{}",
        'report_resourceLevelOfServiceResult',
        'resourceLevelOfServiceReport',
        report_showResourceLevelOfServiceResults,
        toCsv,
        postShowFilter);
}

function report_getProtoNurseReport(toCsv, postShowFilter) {
    report_getReportForDisplayOrCsv(
        "rest/report/getReport?name=proto_by_nurse",
        "{}",
        'report_protoNurseResult',
        'protoNurseNutritionReport',
        report_showProtoNurseResults,
        toCsv,
        postShowFilter);
}

function report_getProtoNutritionistReport(toCsv, postShowFilter) {
    report_getReportForDisplayOrCsv(
        "rest/report/getReport?name=proto_nutritionist",
        "{}",
        'report_protoNutritionistResult',
        'protoNurseNutritionReport',
        report_showProtoNutritionistResults,
        toCsv,
        postShowFilter);
}

function report_getSubjectPurgeReport(toCsv, postShowFilter) {
    report_getReportForDisplayOrCsv(
        "rest/report/getReport?name=subject_purge",
        "{}",
        'report_subjectPurgeResult',
        'subjectPurgeReport',
        report_showSubjectPurgeResults,
        toCsv,
        postShowFilter);
}

function report_getVisitTemplateDataReport(toCsv, postShowFilter) {
    report_getReportForDisplayOrCsv(
        "rest/report/getReport?name=visit_template_data",
        "{}",
        'report_visitTemplateResult',
        'visitTemplateReport',
        report_showVisitTemplateResults,
        toCsv,
        postShowFilter);
}

// 'visit status history by subject'
function report_getSubjectVisitHistoryReport(toCsv, postShowFilter) {
    report_getReportForDisplayOrCsv(
        "rest/report/getReport?name=subject_visit_history",
        JSON.stringify({
            subjectLastName: $.trim($("#report_subjectLastName").val()),
            subjectMRN: $.trim($("#report_subjectMRN").val()),
            subjectDOB: $.trim($("#report_subjectDOB").val())
        }),
        'report_subjectVisitHistoryResult',
        'subjectVisitHistoryReport',
        report_showSubjectVisitHistoryResults,
        toCsv,
        postShowFilter);
}

// 'visit history by subject'
function report_getStudySubjectVisitReport(toCsv, postShowFilter) {
    report_getReportForDisplayOrCsv(
        "rest/report/getReport?name=study_subject_visit",
        JSON.stringify({
            subjectLastName: $.trim($("#report_subjectLastName").val()),
            subjectMRN: $.trim($("#report_subjectMRN").val()),
            subjectDOB: $.trim($("#report_subjectDOB").val())
        }),
        'report_studySubjectVisitResult',
        'studySubjectVisitReport',
        report_showStudySubjectVisitResults,
        toCsv,
        postShowFilter);
}

function report_getAncillaryOnlyByProtocolReport(toCsv, postShowFilter) {
    report_getReportForDisplayOrCsv(
        "rest/report/getReport?name=ancillary_only_by_protocol",
        JSON.stringify({
            startTime: getStartTime(),
            endTime: getEndTime()
        }),
        'report_ancillaryOnlyByProtocolResult',
        'ancillaryOnlyByProtocolReport',
        report_showAncillaryOnlyByProtocolResults,
        toCsv,
        postShowFilter);
}

function report_getBillableResourcesReport(toCsv, postShowFilter) {
    report_getReportForDisplayOrCsv(
        "rest/report/getReport?name=billable_resources",
        JSON.stringify({
            startTime: getStartTime(),
            endTime: getEndTime()
        }),
        'report_billableResourcesResult',
        'billableResourcesReport',
        report_showBillableResourcesResults,
        toCsv,
        postShowFilter);
}

function report_getBillingByInvestigatorReport(toCsv, postShowFilter) {
    report_getReportForDisplayOrCsv(
        "rest/report/getReport?name=billing_by_investigator",
        JSON.stringify({
            startTime: getStartTime(),
            endTime: getEndTime(),
            piFirstName: $.trim($("#report_piFirstName").val()),
            piLastName: $.trim($("#report_piLastName").val())
        }),
        'report_billingByInvestigatorResult',
        'billingByInvestigatorReport',
        report_showBillingByInvestigatorResults,
        toCsv,
        postShowFilter);
}

function report_getBookedVisitServiceLevelByTypeReport(toCsv, postShowFilter) {
    report_getReportForDisplayOrCsv(
        "rest/report/getReport?name=booked_visit_service_level_by_visit_type",
        JSON.stringify({
            startTime: getStartTime(),
            endTime: getEndTime()
        }),
        'report_bookedVisitServiceLevelByTypeResult',
        'bookedVisitServiceLevelByTypeReport',
        report_showBookedVisitServiceLevelByTypeResults,
        toCsv,
        postShowFilter);
}

function report_getWeeklyPharmReport(toCsv, postShowFilter) {
    report_getReportForDisplayOrCsv(
        "rest/report/getReport?name=weekly_pharm",
        JSON.stringify({
            startTime: getStartTime(),
            endTime: getEndTime()
        }),
        'report_weeklyPharmResult',
        'weeklyPharmReport',
        report_showWeeklyPharmResults,
        toCsv,
        postShowFilter);
}

function report_getVisitsFlaggedReport(toCsv, postShowFilter) {
    report_getReportForDisplayOrCsv(
        "rest/report/getReport?name=visits_flagged_edit",
        JSON.stringify({
            startTime: getStartTime(),
            endTime: getEndTime()
        }),
        'report_visitsFlaggedResult',
        'visitsFlaggedResult',
        report_showVisitsFlaggedResults,
        toCsv,
        postShowFilter);
}

function report_getVisitDurationByVisitTypeReport(toCsv, postShowFilter) {
    report_getReportForDisplayOrCsv(
        "rest/report/getReport?name=visit_duration_by_visit_type",
        JSON.stringify({
            startTime: getStartTime(),
            endTime: getEndTime()
        }),
        'report_visitDurationByVisitTypeResult',
        'visitDurationByVisitType',
        report_showVisitDurationByVisitTypeResults,
        toCsv,
        postShowFilter);
}


function report_getStudyVisitLocationReport(toCsv, postShowFilter) {
    report_getReportForDisplayOrCsv(
        "rest/report/getReport?name=study_visit_location",
        JSON.stringify({
            startTime: getStartTime(),
            endTime: getEndTime()
        }),
        'report_studyVisitLocationResult',
        'studyVisitLocationReport',
        report_showStudyVisitLocationResults,
        toCsv,
        postShowFilter);
}

function report_getStudyStatusChangeReport(toCsv, postShowFilter) {
    report_getReportForDisplayOrCsv(
        "rest/report/getReport?name=study_status_change",
        JSON.stringify({
            startTime: getStartTime(),
            endTime: getEndTime()
        }),
        'report_studyStatusChangeResult',
        'studyStatusChangeReport',
        report_showStudyStatusChangeResults,
        toCsv,
        postShowFilter);
}

// staff views of subjects by user
function report_getStaffAuditSubjectViewsReport(toCsv, postShowFilter) {
    report_getReportForDisplayOrCsv(
        "rest/report/getReport?name=staff_audit_subject_views",
        JSON.stringify({
            startTime: getStartTime(),
            endTime: getEndTime()
        }),
        'report_staffAuditSubjectViewsResult',
        'staffAuditSubjectViewsReport',
        report_showStaffAuditSubjectViewsResults,
        toCsv,
        postShowFilter);
}

// staff views of subjects by mrn
function report_getSubjectAuditStaffViewsReport(toCsv, postShowFilter) {
    report_getReportForDisplayOrCsv(
        "rest/report/getReport?name=subject_audit_staff_views",
        JSON.stringify({
            startTime: getStartTime(),
            endTime: getEndTime(),
            mrn: $.trim($("#report_mrn").val())
        }),
        'report_subjectAuditStaffViewsResult',
        'subjectAuditStaffViewsReport',
        report_showSubjectAuditStaffViewsResults,
        toCsv,
        postShowFilter);
}

function report_getOverrideReport(toCsv, postShowFilter) {
    report_getReportForDisplayOrCsv(
        "rest/report/getReport?name=override",
        JSON.stringify({
            startTime: getStartTime(),
            endTime: getEndTime()
        }),
        'report_overrideResult',
        'overrideReport',
        report_showOverrideResults,
        toCsv,
        postShowFilter);
}

function report_getTransactionsReport(toCsv, postShowFilter) {
    report_getReportForDisplayOrCsv(
        "rest/report/getReport?name=transactions",
        JSON.stringify({
            startTime: getStartTime(),
            endTime: getEndTime()
        }),
        'report_transactionsResult',
        'transactionsReport',
        report_showTransactionsResults,
        toCsv,
        postShowFilter);
}

function report_getOffUnitReport(toCsv, postShowFilter) {
    report_getReportForDisplayOrCsv(
        "rest/report/getReport?name=off_unit",
        JSON.stringify({
            startTime: getStartTime(),
            endTime: getEndTime()
        }),
        'report_offUnitResult',
        'offUnitReport',
        report_showOffUnitResults,
        toCsv,
        postShowFilter);
}

function report_getCensusReport(toCsv, postShowFilter) {
    report_getReportForDisplayOrCsv(
        "rest/report/getReport?name=census",
        JSON.stringify({
            startTime: getStartTime(),
            endTime: getEndTime()
        }),
        'report_censusResult',
        'censusReport',
        report_showCensusResults,
        toCsv,
        postShowFilter);
}

function report_getCRCAvailabilityReport(toCsv, postShowFilter) {
    report_getReportForDisplayOrCsv(
        "rest/report/getReport?name=crc_availability",
        JSON.stringify({
            startTime: getStartTime(),
            endTime: getEndTime()
        }),
        'report_crcAvailabilityResult',
        'crcAvailabilityReport',
        report_showCRCAvailabilityResults,
        toCsv,
        postShowFilter);
}

function report_getDailyAdmReport(toCsv, postShowFilter) {
    report_getReportForDisplayOrCsv(
        "rest/report/getReport?name=daily_adm",
        JSON.stringify({
            startTime: getStartTime(),
            endTime: getEndTime()
        }),
        'report_dailyAdmResult',
        'dailyAdmReport',
        report_showDailyAdmResults,
        toCsv,
        postShowFilter);
}

function report_getDailyOverviewReport(toCsv, postShowFilter) {
    report_getReportForDisplayOrCsv(
        "rest/report/getReport?name=daily_overview",
        JSON.stringify({
            startTime: getStartTime(),
            endTime: getEndTime()
        }),
        'report_dailyOverviewResult',
        'dailyOverviewReport',
        report_showDailyOverviewResults,
        toCsv,
        postShowFilter);
}

function report_getDailyResourceReport(toCsv, postShowFilter) {
    report_getReportForDisplayOrCsv(
        "rest/report/getReport?name=daily_resource",
        JSON.stringify({
            startTime: getStartTime(),
            endTime: getEndTime()
        }),
        'report_dailyResourceResult',
        'dailyResourceReport',
        report_showDailyResourceResults,
        toCsv,
        postShowFilter);
}

function report_getDeptAndPiReport(toCsv, postShowFilter) {
    report_getReportForDisplayOrCsv(
        "rest/report/getReport?name=dept_and_pi",
        JSON.stringify({
            startTime: getStartTime(),
            endTime: getEndTime()
        }),
        'report_deptAndPiResult',
        'deptAndPiReport',
        report_showDeptAndPiResults,
        toCsv,
        postShowFilter);
}

function report_getLevelOfServiceReport(toCsv, postShowFilter) {
    report_getReportForDisplayOrCsv(
        "rest/report/getReport?name=level_of_service",
        JSON.stringify({
            startTime: getStartTime(),
            endTime: getEndTime()
        }),
        'report_levelOfServiceResult',
        'levelOfServiceReport',
        report_showLevelOfServiceResults,
        toCsv,
        postShowFilter);
}
function report_getMetaKitchenReport(toCsv, postShowFilter) {
    // save filtering options, but ensure correct report-name variant
    if (recent_reportUrl != undefined) {
        recent_reportUrl = recent_reportUrl.replace(/meta_kitchen(_\w*)?/, 'meta_kitchen');
    }

    report_getReportForDisplayOrCsv(
        "rest/report/getReport?name=meta_kitchen",
        JSON.stringify({
            startTime: getStartTime(),
            endTime: getEndTime()
        }),
        'report_metaKitchenResult',
        'metaKitchenReport',
        report_showMetaKitchenResults,
        toCsv,
        postShowFilter);
}
function report_getMetaKitchenByTimeReport(toCsv, postShowFilter) {
    // save filtering options, but ensure correct report-name variant
    if (recent_reportUrl != undefined) {
        recent_reportUrl = recent_reportUrl.replace(/meta_kitchen(_\w*)?/, 'meta_kitchen_by_time');
    }

    report_getReportForDisplayOrCsv(
        "rest/report/getReport?name=meta_kitchen_by_time",
        JSON.stringify({
            startTime: getStartTime(),
            endTime: getEndTime()
        }),
        'report_metaKitchenByTimeResult',
        'metaKitchenReport',
        report_showMetaKitchenByTimeResults,
        toCsv,
        postShowFilter);
}


/// for miscellaneous export buttons


function report_getExportDailyOverviewReport(toCsv) {
    report_getReportForDisplayOrCsv(
        "rest/report/getReport?name=daily_overview",
        JSON.stringify({
            startTime: getStartTime(),
            endTime: getEndTime()
        }),
        '',
        'dailyOverviewReport',
        undefined,
        toCsv,
        undefined,
        "rest/report/getReport?name=export_daily_overview");
}

function report_getExportDailyResourceReport(toCsv) {
    report_getReportForDisplayOrCsv(
        "rest/report/getReport?name=daily_resource",
        JSON.stringify({
            startTime: getStartTime(),
            endTime: getEndTime()
        }),
        '',
        'dailyResourceReport',
        undefined,
        toCsv,
        undefined,
        "rest/report/getReport?name=export_daily_resource");
}

function report_getMetaKitchenFlatReport(toCsv) {
    // piggyback off of url for 'containing' meta_kitchen report

    // save filtering options, but ensure correct report-name variant
    if (recent_reportUrl != undefined) {
        recent_reportUrl = recent_reportUrl.replace(/meta_kitchen(_\w*)?/, 'meta_kitchen_flat');
    }

    var flatUrlOverride = "rest/report/getReport?name=meta_kitchen_flat";
    report_getReportForDisplayOrCsv(
        "rest/report/getReport?name=meta_kitchen_flat",
        JSON.stringify({
            startTime: getStartTime(),
            endTime: getEndTime()
        }),
        '',
        'metaKitchenReport',
        undefined,
        toCsv,
        undefined,
        flatUrlOverride);
}

