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

function report_showStudyDataResults() {
    var out = "";
    report_handleDataResponse(report_studyDataResult.length);

    for (var i = 0; i < report_studyDataResult.length; i++) {

        var r = report_studyDataResult[i];

        var fourFundInfosArray = getFourFundingSources(r);

        var funding1 = fourFundInfosArray[0].name;
        var comment1 = fourFundInfosArray[0].comment;
        var funding2 = fourFundInfosArray[1].name;
        var comment2 = fourFundInfosArray[1].comment;
        var funding3 = fourFundInfosArray[2].name;
        var comment3 = fourFundInfosArray[2].comment;
        var funding4 = fourFundInfosArray[3].name;
        var comment4 = fourFundInfosArray[3].comment;

        out +=
            "    <div class='reportRow'>" +
            "      <div class='secondaryNameBlock'>" +
            "       <div class='mainName'>" + r.localId + "</div>" +
            "      </div>" +
            "      <div class='clear'></div>" +
            "      <div class='CSSTableGenerator'><table width='100%' border='0' cellspacing='0' cellpadding='5'>" +
            "        <tr>" +
            "          <td width='21%' align='left' valign='top' style='font-weight:bold'>Study Name</td>" +
            "          <td width='29%' align='left' valign='top'>" + r.name + "</td>" +
            "          <td width='21%' align='left' valign='top' style='font-weight:bold'>PI Name</td>" +
            "          <td width='29%' align='left' valign='top'>" + buildFullName(r.investigator) + "</td>" +
            "        </tr>";
        out +=
            "        <tr>" +
            "        <tr>" +
            "          <td width='21%' align='left' valign='top' style='font-weight:bold'>Institution</td>" +
            "          <td width='29%' align='left' valign='top'>" + showValue(r.institution.longName) + "</td>" +
            "          <td width='21%' align='left' valign='top' style='font-weight:bold'>PI email</td>" +
            "          <td width='29%' align='left' valign='top'>" + showValue(r.investigator.email) + "</td>" +
            "        </tr>" +
            "        <tr>" +
            "          <td width='21%' align='left' valign='top' style='font-weight:bold'>Local ID</td>" +
            "          <td width='29%' align='left' valign='top'>" + r.localId + "</td>" +
            "          <td width='21%' align='left' valign='top' style='font-weight:bold'>PI phone #1</td>" +
            "          <td width='29%' align='left' valign='top'>" + showValue(r.investigator.primaryPhone) + "</td>" +
            "        </tr>" +
            "        <tr>" +
            "          <td width='21%' align='left' valign='top' style='font-weight:bold'>Catalyst ID</td>" +
            "          <td width='29%' align='left' valign='top'>" + showValue(r.catalystId) + "</td>" +
            "          <td width='21%' align='left' valign='top' style='font-weight:bold'>PI pager</td>" +
            "          <td width='29%' align='left' valign='top'>" + showValue(r.investigator.pager) + "</td>" +
            "        </tr>" +
            "        <tr>" +
            "          <td width='21%' align='left' valign='top' style='font-weight:bold'>Status</td>" +
            "          <td width='29%' align='left' valign='top'>" + r.studyStatus.shortName + "</td>" +
            "          <td width='21%' align='left' valign='top' style='font-weight:bold'>Primary Scheduling Contact</td>" +
            "          <td width='29%' align='left' valign='top'>" + buildFullName(r.scheduler) + "</td>" +
            "        </tr>" +
            "        <tr>" +
            "          <td width='21%' align='left' valign='top' style='font-weight:bold'>Last Status Change</td>" +
            "          <td width='29%' align='left' valign='top'>" + showDateTime(r.statusChange) + "</td>" +
            "          <td width='25%' align='left' valign='top' style='font-weight:bold'>Primary Scheduling Contact email</td>" +
            "          <td width='25%' align='left' valign='top'>" + showValue(r.scheduler.email) + "</td>" +
            "        </tr>" +
            "        <tr>" +
            "          <td width='21%' align='left' valign='top' style='font-weight:bold'>IRB #</td>" +
            "          <td width='29%' align='left' valign='top'>" + showValue(r.irb) + "</td>" +
            "          <td width='25%' align='left' valign='top' style='font-weight:bold'>Primary Scheduling Contact phone</td>" +
            "          <td width='25%' align='left' valign='top'>" + showValue(r.scheduler.primaryPhone) + "</td>" +
            "        </tr>" +
            "        <tr>";
        out +=
            "          <td width='21%' align='left' valign='top' style='font-weight:bold'>IRB Institution</td>" +
            "          <td width='29%' align='left' valign='top'>" + showValue(r.irbInstitution.name) + "</td>" +
            "          <td width='21%' align='left' valign='top' style='font-weight:bold'>Secondary Scheduling Contact</td>" +
            "          <td width='29%' align='left' valign='top'>" + buildFullName(r.secondaryScheduler) + "</td>" +
            "        </tr>" +
            "        <tr>" +
            "          <td width='21%' align='left' valign='top' style='font-weight:bold'>IRB Approval Date</td>" +
            "          <td width='29%' align='left' valign='top'>" + showDate(r.irbApprovalDate) + "</td>" +
            "          <td width='21%' align='left' valign='top' style='font-weight:bold'>Secondary Scheduling Contact Email</td>" +
            "          <td width='29%' align='left' valign='top'>" + buildFullName(r.secondaryScheduler.email) + "</td>" +
            "        </tr>";
        out +=

            "        <tr>" +
            "          <td width='21%' align='left' valign='top' style='font-weight:bold'>IRB Renewal Date</td>" +
            "          <td width='29%' align='left' valign='top'>" + showDate(r.irbRenewalDate) + "</td>" +
            "          <td width='21%' align='left' valign='top' style='font-weight:bold'>Secondary Scheduling Contact phone</td>" +
            "          <td width='29%' align='left' valign='top'>" + showValue(r.secondaryScheduler.primaryPhone) + "</td>" +
            "        </tr>" +
            "        <tr>" +
            "          <td width='21%' align='left' valign='top' style='font-weight:bold'>IRB Expiration Date</td>" +
            "          <td width='29%' align='left' valign='top'>" + showDate(r.irbExpiration) + "</td>" +
            "          <td width='21%' align='left' valign='top' style='font-weight:bold'>Third Scheduling Contact</td>" +
            "          <td width='29%' align='left' valign='top'>" + buildFullName(r.scheduler3) + "</td>" +
            "        </tr>" +
            "        <tr>" +
            "          <td width='21%' align='left' valign='top' style='font-weight:bold'>CRC Approved</td>" +
            "          <td width='29%' align='left' valign='top'>" + yesNoValue(r.crcFunded) + "</td>" +
            "          <td width='21%' align='left' valign='top' style='font-weight:bold'>Third Scheduling Contact Email</td>" +
            "          <td width='29%' align='left' valign='top'>" + buildFullName(r.scheduler3.email) + "</td>" +
            "        </tr>" +
            "        <tr>";
        out +=

            "          <td width='21%' align='left' valign='top' style='font-weight:bold'>CRC Category</td>" +
            "          <td width='29%' align='left' valign='top'>" + r.crcCategory + "</td>" +
            "          <td width='21%' align='left' valign='top' style='font-weight:bold'>Third Scheduling Contact Phone</td>" +
            "          <td width='29%' align='left' valign='top'>" + buildFullName(r.scheduler3.primaryPhone) + "</td>" +
            "        </tr>" +
            "        <tr>" +
            "          <td width='21%' align='left' valign='top' style='font-weight:bold'>Industry Inititated</td>" +
            "          <td width='29%' align='left' valign='top'>" + yesNoValue(r.industryInitiated) + "</td>" +
            "          <td width='21%' align='left' valign='top' style='font-weight:bold'>Responsible Physician 1</td>" +
            "          <td width='29%' align='left' valign='top'>" + buildFullName(r.physician) + "</td>" +
            "        </tr>" +
            "        <tr>" +
            "          <td width='21%' align='left' valign='top' style='font-weight:bold'>Funding Source 1</td>" +
            "          <td width='29%' align='left' valign='top'>" + showValue(funding1) + "</td>" +
            "          <td width='21%' align='left' valign='top' style='font-weight:bold'>Responsible Physician 1 email</td>" +
            "          <td width='29%' align='left' valign='top'>" + showValue(r.physician.email) + "</td>" +
            "        </tr>" +
            "        <tr>" +
            "          <td width='21%' align='left' valign='top' style='font-weight:bold'>Funding Comment 1</td>" +
            "          <td width='29%' align='left' valign='top'>" + showValue(comment1) + "</td>" +
            "          <td width='21%' align='left' valign='top' style='font-weight:bold'>Responsible Physician 1 phone #1</td>" +
            "          <td width='29%' align='left' valign='top'>" + showValue(r.physician.primaryPhone) + "</td>" +
            "        </tr>" +
            "        <tr>" +
            "          <td width='21%' align='left' valign='top' style='font-weight:bold'>Funding Source 2</td>" +
            "          <td width='29%' align='left' valign='top'>" + showValue(funding2) + "</td>" +
            "          <td width='21%' align='left' valign='top' style='font-weight:bold'>Responsible Physician 1 pager</td>" +
            "          <td width='29%' align='left' valign='top'>" + showValue(r.physician.pager) + "</td>" +
            "        </tr>" +
            "        <tr>" +
            "          <td width='21%' align='left' valign='top' style='font-weight:bold'>Funding Comment 2</td>" +
            "          <td width='29%' align='left' valign='top'>" + showValue(comment2) + "</td>" +
            "          <td width='21%' align='left' valign='top' style='font-weight:bold'>Responsible Physician 2</td>" +
            "          <td width='29%' align='left' valign='top'>" + buildFullName(r.physician2) + "</td>" +
            "        </tr>";
        out +=
            "        <tr>" +
            "          <td width='21%' align='left' valign='top' style='font-weight:bold'>Funding Source 3</td>" +
            "          <td width='29%' align='left' valign='top'>" + showValue(funding3) + "</td>" +
            "          <td width='21%' align='left' valign='top' style='font-weight:bold'>Responsible Physician 2 email</td>" +
            "          <td width='29%' align='left' valign='top'>" + showValue(r.physician2.email) + "</td>" +
            "        </tr>" +
            "        <tr>" +
            "          <td width='21%' align='left' valign='top' style='font-weight:bold'>Funding Comment 3</td>" +
            "          <td width='29%' align='left' valign='top'>" + showValue(comment3) + "</td>" +
            "          <td width='21%' align='left' valign='top' style='font-weight:bold'>Responsible Physician 2 phone #1</td>" +
            "          <td width='29%' align='left' valign='top'>" + showValue(r.physician2.primaryPhone) + "</td>" +
            "        </tr>" +
            "        <tr>" +
            "          <td width='21%' align='left' valign='top' style='font-weight:bold'>Funding Source 4</td>" +
            "          <td width='29%' align='left' valign='top'>" + showValue(funding4) + "</td>" +
            "          <td width='21%' align='left' valign='top' style='font-weight:bold'>Responsible Physician 2 pager</td>" +
            "          <td width='29%' align='left' valign='top'>" + showValue(r.physician2.pager) + "</td>" +
            "        </tr>" +
            "        <tr>" +
            "          <td width='21%' align='left' valign='top' style='font-weight:bold'>Funding Comment 4</td>" +
            "          <td width='29%' align='left' valign='top'>" + showValue(comment4) + "</td>" +
            "          <td width='21%' align='left' valign='top' style='font-weight:bold'>Protocol Nurse</td>" +
            "          <td width='29%' align='left' valign='top'>" + buildFullName(r.protocolNurse) + "</td>" +
            "        </tr>" +
            "        <tr>" +
            "          <td width='21%' align='left' valign='top' style='font-weight:bold'>Start Date</td>" +
            "          <td width='29%' align='left' valign='top'>" + showDate(r.expectedStartDate) + "</td>" +
            "          <td width='21%' align='left' valign='top' style='font-weight:bold'>Associate Protocol Nurse</td>" +
            "          <td width='29%' align='left' valign='top'>" + buildFullName(r.associateNurse) + "</td>" +
            "        </tr>" +
            "        <tr>" +
            "          <td width='21%' align='left' valign='top' style='font-weight:bold'>End Date</td>" +
            "          <td width='29%' align='left' valign='top'>" + showDate(r.expectedEndDate) + "</td>" +
            "          <td width='21%' align='left' valign='top' style='font-weight:bold'>Protocol Nutritionist</td>" +
            "          <td width='29%' align='left' valign='top'>" + buildFullName(r.protocolNutritionist) + "</td>" +
            "        </tr>";
        out +=

            "        <tr>" +
            "          <td width='21%' align='left' valign='top' style='font-weight:bold'>First Visit Date</td>" +
            "          <td width='29%' align='left' valign='top'>" + showDateTime(r.firstVisitDate) + "</td>" +
            "          <td width='21%' align='left' valign='top' style='font-weight:bold'>Associate Protocol Nutritionist</td>" +
            "          <td width='29%' align='left' valign='top'>" + buildFullName(r.associateProtocolNutritionist) + "</td>" +
            "        </tr>" +
            "        <tr>" +
            "          <td width='21%' align='left' valign='top' style='font-weight:bold'>Last Scheduled Visit Date</td>" +
            "          <td width='29%' align='left' valign='top'>" + showDateTime(r.lastScheduledVisitDate) + "</td>" +
            "          <td width='21%' align='left' valign='top' style='font-weight:bold'>Total Inpatient Visits</td>" +
            "          <td width='29%' align='left' valign='top'>" + showValue(r.totalInpatientVisits) + "</td>" +
            "        </tr>" +
            "        <tr>";
        out +=
            "          <td width='21%' align='left' valign='top' style='font-weight:bold'>Total Subjects</td>" +
            "          <td width='29%' align='left' valign='top'>" + showValue(r.totalSubjects) + "</td>" +
            "          <td width='21%' align='left' valign='top' style='font-weight:bold'>Total Outpatient Visits</td>" +
            "          <td width='29%' align='left' valign='top'>" + showValue(r.totalOutpatientVisits) + "</td>" +
            "        </tr>" +
            "      </table>" +
            "    <!-- table generator --> </div>" +
            "    <!-- report row --> </div>" +
            "    <hr class='report' />";
    }

    out += "</div>";

    $('#report_results').html(out);
}

function getFourFundingSources(r) {
    var fundingSourceInfoList = r.fundingSourceInfoList;

    var displayFourFundInfos = new Array();

    var listToOrderedFundInfo = new Object();
    for (var i = 0; i < fundingSourceInfoList.length; i++) {
        var fundInfo = fundingSourceInfoList[i];
        var oneToFour = fundInfo.oneToFour;

        listToOrderedFundInfo[oneToFour] = fundInfo;
    }

    for (var i = 1; i < 5; i++) {
        var displayFundInfo = new Object();

        displayFundInfo.name = '';
        displayFundInfo.centers = '';
        displayFundInfo.grant = '';
        displayFundInfo.costCenter = '';
        displayFundInfo.direct = '';
        displayFundInfo.indirect = '';
        displayFundInfo.start = '';
        displayFundInfo.end = '';
        displayFundInfo.comment = '';

        var fundInfo = listToOrderedFundInfo[i];
        if (fundInfo != null) {
            displayFundInfo.name = fundInfo.name ? fundInfo.name : '';
            displayFundInfo.centers = fundInfo.centerAndInstitutionName ? fundInfo.centerAndInstitutionName : '';
            displayFundInfo.grant = fundInfo.grant ? fundInfo.grant : '';
            displayFundInfo.costCenter = fundInfo.center ? fundInfo.center : '';
            displayFundInfo.direct = fundInfo.directAward ? fundInfo.directAward : '';
            displayFundInfo.indirect = fundInfo.indirectAward ? fundInfo.indirectAward : '';
            displayFundInfo.start = fundInfo.start ? fundInfo.start : '';
            displayFundInfo.end = fundInfo.end ? fundInfo.end : '';
            displayFundInfo.comment = fundInfo.comment ? fundInfo.comment : '';
        }

        displayFourFundInfos.push(displayFundInfo);
    }

    return displayFourFundInfos;
}

function report_showUserDataResults() {
    var out = "";
    report_handleDataResponse(report_userDataResult.length);

    for (var i = 0; i < report_userDataResult.length; i++) {
        var r = report_userDataResult[i];
        out +=
            "    <div class='reportRow'>" +
            "      <div class='secondaryNameBlock'>" +
            "       <div class='mainName'>" + util_buildFullName(
                r.firstName,
                r.middleName,
                r.lastName) + "</div>" +
            "      </div>" +
            "      <div style='clear: both;'></div>" +
            "      <div class='CSSTableGenerator'><table width='100%' border='0' cellspacing='0' cellpadding='5'>" +
            "        <tr>" +
            "          <td width='21%' align='left' valign='top' style='font-weight:bold'>User Title</td>" +
            "          <td width='29%' align='left' valign='top'>" + showValue(
                r.role) + "</td>" +
            "          <td width='21%' align='left' valign='top' style='font-weight:bold'>Credential</td>" +
            "          <td width='29%' align='left' valign='top'>" + showValue(showValue(
                r.credential)) + "</td>" +
            "        </tr>" +
            "        <tr>" +
            "          <td width='21%' align='left' valign='top' style='font-weight:bold'>Role</td>" +
            "          <td width='29%' align='left' valign='top'>" + showValue(
                r.institutionRole) + "</td>" +
            "          <td width='21%' align='left' valign='top' style='font-weight:bold'>Faculty Rank</td>" +
            "          <td width='29%' align='left' valign='top'>" + showValue(showValue(
                r.facultyRank)) + "</td>" +
            "        </tr>" +
            "        <tr>" +
            "          <td width='21%' align='left' valign='top' style='font-weight:bold'>Department</td>" +
            "          <td width='29%' align='left' valign='top'>" + showValue(showValue(
                r.department)) + "</td>" +
            "          <td width='21%' align='left' valign='top' style='font-weight:bold'>Primary Phone</td>" +
            "          <td width='29%' align='left' valign='top'>" +
            r.primaryPhone + "</td>" +
            "        </tr>" +
            "        <tr>" +
            "          <td width='21%' align='left' valign='top' style='font-weight:bold'>Division</td>" +
            "          <td width='29%' align='left' valign='top'>" + showValue(showValue(
                r.division)) + "</td>" +
            "          <td width='21%' align='left' valign='top' style='font-weight:bold'>Secondary Phone</td>" +
            "          <td width='29%' align='left' valign='top'>" +
            r.secondaryPhone + "</td>" +
            "        </tr>" +
            "        <tr>" +
            "          <td width='21%' align='left' valign='top' style='font-weight:bold'>Institution</td>" +
            "          <td width='29%' align='left' valign='top'>" + showValue(
                r.institution) + "</td>" +
            "          <td width='21%' align='left' valign='top' style='font-weight:bold'>Email</td>" +
            "          <td width='29%' align='left' valign='top'>" +
            r.email + "</td>" +
            "        </tr>" +
            "        <tr>" +
            "          <td width='21%' align='left' valign='top' style='font-weight:bold'>Status</td>" +
            "          <td width='29%' align='left' valign='top'>" + booleanToStatusValue(
                r.active) + "</td>" +
            "        </tr>" +
            "      </table>" +
            "    <!-- table generator --></div>" +
            "    <!-- report row --></div>" +
            "    <hr class='report' />";
    }

    $('#report_results').html(out);
}

function report_showStaffAuditSubjectViewsResults() {
    var out = "";
    report_handleDataResponse(report_staffAuditSubjectViewsResult.length);
    var currentSubjectId = -1;
    var previousSubjectId = -1;
    var currentUserId = -1;
    var previousUserId = -1;
    var iteration = 0;
    var currentAction;
    var currentDate;
    var previousAction;
    var previousDate;
    var tableRow = 0;

    for (var i = 0; i < report_staffAuditSubjectViewsResult.length; i++) {
        var r = report_staffAuditSubjectViewsResult[i];
        currentSubjectId = r.subjectId;
        currentUserId = r.userId;
        currentAction = r.actionPerformed;
        currentDate = r.date;
        if (iteration != 0 && currentSubjectId != previousSubjectId) {
            out += " </table>" +
                "    <!-- table generator --></div>" +
                "    <!-- report row --></div>" +
                "  <hr class='report' />";
        }

        if (currentSubjectId != previousSubjectId) {
            tableRow = 0;
            out +=
                "    <div class='reportRow'>" +
                "      <div class='secondaryNameBlock'>" +
                "       <div class='mainName'>" + util_buildFullName(r.subjectFirstName, r.subjectMiddleName, r.subjectLastName) + "</div>" +
                "      </div>" +
                "      <div style='clear: both;'></div>" +
                "      <div class='CSSTableGenerator'><table width='100%' border='0' cellspacing='0' cellpadding='5' style='font-size: 90%;'>" +
                "        <tr style='font-weight:bold'>" +
                "          <th width='24%'>MRN</th>" +
                "          <th width='12%'>Action Performed</th>" +
                "          <th width='12%'>Date</th>" +
                "          <th width='11%'>IP Address</th>" +
                "          <th width='11%'>User</th>" +
                "          <th width='11%'>User ID</th>" +
                "        </tr>";
        }

        out += ((tableRow % 2 == 1) ? "<tr class='altRow'>" : "<tr>");
        out += "<td>" + r.mrn + "</td>" +
            "          <td>" + r.actionPerformed + "</td>" +
            "          <td>" + showDateTime(r.date) + "</td>" +
            "          <td>" + r.ipAddress + "</td>" +
            "          <td>" + util_buildFullName(r.userFirstName, r.userMiddleName, r.userLastName) + "</td>" +
            "          <td>" + r.ecommons + "</td>" +
            "        </tr>";

        previousSubjectId = currentSubjectId;
        previousUserId = currentUserId;
        previousAction = currentAction;
        previousDate = currentDate;

        if (iteration != 0 && currentSubjectId == previousSubjectId && currentUserId != previousUserId && currentAction != previousAction && currentDate != previousDate) {
            out += ((tableRow % 2 == 1) ? "<tr class='altRow'>" : "<tr>");
            out += "<td>" + r.mrn + "</td>" +
                "          <td>" + r.actionPerformed + "</td>" +
                "          <td>" + showDateTime(r.date) + "</td>" +
                "          <td>" + r.ipAddress + "</td>" +
                "          <td>" + util_buildFullName(r.userFirstName, r.userMiddleName, r.userLastName) + "</td>" +
                "          <td>" + r.ecommons + "</td>" +
                "        </tr>";
        }
        iteration++;
        tableRow++;
    }

    $('#report_results').html(out);
}


function report_showSubjectAuditStaffViewsResults() {
    var out = "";
    report_handleDataResponse(report_subjectAuditStaffViewsResult.length);
    var currentSubjectId = -1;
    var previousSubjectId = -1;
    var iteration = 0;
    var tableRow = 0;

    for (var i = 0; i < report_subjectAuditStaffViewsResult.length; i++) {
        var r = report_subjectAuditStaffViewsResult[i];
        currentSubjectId = r.subjectId;

        if (iteration != 0 && currentSubjectId != previousSubjectId) {
            out += " </table>" +
                "    <!-- table generator --></div>" +
                "    <!-- report row --></div>" +
                "  <hr class='report' />";
        }

        if (currentSubjectId != previousSubjectId) {
            tableRow = 0;
            out +=
                "    <div class='reportRow'>" +
                "      <div class='secondaryNameBlock'>" +
                "       <div class='mainName'>" + r.subjectName + "</div>" +
                "      </div>" +
                "      <div style='clear: both;'></div>" +
                "      <div class='CSSTableGenerator'><table width='100%' border='0' cellspacing='0' cellpadding='5' style='font-size: 90%;'>" +
                "        <tr style='font-weight:bold'>" +
                "          <th width='12%'>User</th>" +
                "          <th width='12%'>Title</th>" +
                "          <th width='12%'>Department</th>" +
                "          <th width='12%'>Institution</th>" +
                "          <th width='12%'>Action Performed</th>" +
                "          <th width='12%'>Date</th>" +
                "          <th width='11%'>IP Address</th>" +
                "        </tr>";
        }

        out += ((tableRow % 2 == 1) ? "<tr class='altRow'>" : "<tr>");
        out += "<td>" + util_buildFullName(r.userId.firstName, r.userId.middleName, r.userId.lastName) + "</td>" +
            "          <td>" + r.userId.role.name + "</td>" +
            "          <td>" + showValue(showValue(r.userId.department).name) + "</td>" +
            "          <td>" + r.userId.institution.name + "</td>" +
            "          <td>" + r.actionPerformed + "</td>" +
            "          <td>" + showDateTime(r.date) + "</td>" +
            "          <td>" + r.ipAddress + "</td>" +
            "        </tr>";

        previousSubjectId = currentSubjectId;
        iteration++;
        tableRow++;
    }

    $('#report_results').html(out);
}


function report_showProtoNurseResults() {
    var out = "";
    report_handleDataResponse(report_protoNurseResult.length);
    var currentNurseId = -1;
    var previousNurseId = -1;
    var iteration = 0;

    for (var i = 0; i < report_protoNurseResult.length; i++) {
        var r = report_protoNurseResult[i];
        currentNurseId = r.nurseId;

        if (iteration != 0 && currentNurseId != previousNurseId) {
            out += " </table>" +
                "    <!-- table generator --></div>" +
                "    <!-- report row --></div>" +
                "  <hr class='report' />";
        }

        if (currentNurseId != previousNurseId) {
            out +=
                "    <div class='reportRow'>" +
                "      <div class='secondaryNameBlock'>" +
                "       <div class='mainName'>" + util_buildFullName(r.firstName, r.middleName, r.lastName) + "</div>" +
                "      </div>" +
                "      <div style='clear: both;'></div>" +
                "      <div class='CSSTableGenerator'><table width='100%' border='0' cellspacing='0' cellpadding='5' style='font-size: 90%;'>" +
                "        <tr style='font-weight:bold'>" +
                "          <th width='24%'>Study Name</th>" +
                "          <th width='12%'>Catalyst ID</th>" +
                "          <th width='12%'>Local Id</th>" +
                "          <th width='11%'>Study Status</th>" +
                "          <th width='11%'>IRB #</th>" +
                "          <th width='11%'>IRB Expiration</th>" +
                "        </tr>";
        }

        out += ((iteration % 2 == 1) ? "<tr class='altRow'>" : "<tr>");
        out += "<td>" + r.name + "</td>" +
            "          <td>" + r.catalystId + "</td>" +
            "          <td>" + r.localId + "</td>" +
            "          <td>" + r.studyStatusName + "</td>" +
            "          <td>" + r.irb + "</td>" +
            "          <td>" + showDate(r.irbExpiration) + "</td>" +
            "        </tr>";

        previousNurseId = currentNurseId;
        iteration++;
    }

    $('#report_results').html(out);
}

function report_showProtoNutritionistResults() {
    var out = "";
    report_handleDataResponse(report_protoNutritionistResult.length);
    var currentNutritionistId = -1;
    var previousNutritionistId = -1;
    var iteration = 0;

    for (var i = 0; i < report_protoNutritionistResult.length; i++) {
        var r = report_protoNutritionistResult[i];
        currentNutritionistId = r.nurseId;

        if (iteration != 0 && currentNutritionistId != previousNutritionistId) {
            out += " </table>" +
                "    <!-- table generator --></div>" +
                "    <!-- report row --></div>" +
                "  <hr class='report' />";
        }

        if (currentNutritionistId != previousNutritionistId) {
            out +=
                "    <div class='reportRow'>" +
                "      <div class='secondaryNameBlock'>" +
                "       <div class='mainName'>" + util_buildFullName(r.firstName, r.middleName, r.lastName) + "</div>" +
                "      </div>" +
                "      <div class='clear'></div>" +
                "      <div class='CSSTableGenerator'><table width='100%' border='0' cellspacing='0' cellpadding='5' style='font-size: 90%;'>" +
                "        <tr style='font-weight:bold'>" +
                "          <th width='24%'>Study Name</th>" +
                "          <th width='12%'>Catalyst ID</th>" +
                "          <th width='12%'>Local Id</th>" +
                "          <th width='11%'>Study Status</th>" +
                "          <th width='11%'>IRB #</th>" +
                "          <th width='11%'>IRB Expiration</th>" +
                "        </tr>";
        }

        out += ((iteration % 2 == 1) ? "<tr class='altRow'>" : "<tr>");
        out += "<td>" + r.name + "</td>" +
            "          <td>" + r.catalystId + "</td>" +
            "          <td>" + r.localId + "</td>" +
            "          <td>" + r.studyStatusName + "</td>" +
            "          <td>" + r.irb + "</td>" +
            "          <td>" + showDate(r.irbExpiration) + "</td>" +
            "        </tr>";
        previousNutritionistId = currentNutritionistId;
        iteration++;
    }

    $('#report_results').html(out);
}

function report_showOverrideResults() {
    var out = "";
    report_handleDataResponse(report_overrideResult.length);
    var currentUserId = -1;
    var previousUserId = -1;
    var iteration = 0;
    var tableRow = 0;

    for (var i = 0; i < report_overrideResult.length; i++) {
        var r = report_overrideResult[i];
        currentUserId = r.performingUser_id;

        if (iteration != 0 && currentUserId != previousUserId) {
            out += " </table>" +
                "    <!-- table generator --></div>" +
                "    <!-- report row --></div>" +
                "  <hr class='report' />";
        }

        if (currentUserId != previousUserId) {
            tableRow = 0;
            out +=
                "    <div class='reportRow'>" +
                "      <div class='secondaryNameBlock'>" +
                "       <div class='mainName'>" + util_buildFullName(
                    r.performingUser_firstName,
                    r.performingUser_middleName,
                    r.performingUser_lastName) + " (" +
                r.performingUser_ecommonsId + ")" + "</div>" +
                "      </div>" +
                "      <div style='clear: both;'></div>" +
                "      <div class='CSSTableGenerator'><table width='100%' border='0' cellspacing='0' cellpadding='5' style='font-size: 90%;'>" +
                "        <tr style='font-weight:bold'>" +
                "          <th width='12%'>Override Time</th>" +
                "          <th width='24%'>Override Reason</th>" +
                "          <th width='12%'>Resource Name</th>" +
                "          <th width='12%'>Visit Name</th>" +
                "          <th width='10%'>Scheduled Visit Time</th>" +
                "          <th width='12%'>Catalyst Id</th>" +
                "          <th width='12%'>Local Id</th>" +
                "          <th width='12%'>IRB #</th>" +
                "        </tr>";
        }

        out += ((tableRow % 2 == 1) ? "<tr class='altRow'>" : "<tr>");
        out += "  <td>" + showDateTime(r.date) + "</td>" +
            "          <td>" + showValue(showValue(r.appointmentOverrideReason)) + "</td>" +
            "          <td>" + showValue(showValue(r.affectedResource)) + "</td>" +
            "          <td>" + showValue(r.bookedVisit) + "</td>" +
            "          <td>" + showDateTime(r.bookedVisit_scheduledStartTime) + "</td>" +
            "          <td>" + showValue(r.bookedVisit_study_catalystId) + "</td>" +
            "          <td>" + showValue(r.bookedVisit_study_localId) + "</td>" +
            "          <td>" + showValue(r.bookedVisit_study_irb) + "</td>" +
            "        </tr>";

        previousUserId = currentUserId;
        iteration++;
        tableRow++;
    }

    $('#report_results').html(out);
}


function report_showOffUnitResults() {
    var out = "";
    report_handleDataResponse(report_offUnitResult.length);
    var currentVisit = -1;
    var previousVisit = -1;
    var iteration = 0;
    var tableRow = 0;

    for (var i = 0; i < report_offUnitResult.length; i++) {
        var result_i = report_offUnitResult[i];
        currentVisit = result_i.bookedVisitId;

        if (iteration != 0 && currentVisit != previousVisit) {
            out += " </table>" +
                "    <!-- table generator --></div>" +
                "    <!-- report row --></div>" +
                "  <hr class='report' />";
        }

        if (currentVisit != previousVisit) {
            var divVisitStatusClass = null;
            if (result_i.visitStatus == 'Scheduled') {
                divVisitStatusClass = " <div class='statusBlock reportSecondary'>";
            }
            else if (result_i.visitStatus == 'Checked-In') {
                divVisitStatusClass = " <div class='statusBlock reportCheckedInSecondary'>";
            }
            tableRow = 0;
            out +=
                "    <div class='reportRow'>" +
                "      <div class='secondaryNameBlock'>" +
                "       <div class='mainName'>" + result_i.visitName + " - " + result_i.visitType + "</div>" +
                "      </div>" +
                "      <div class='statusBlockContainer'>" +
                divVisitStatusClass +
                "          <div class='label'>Subject Name</div>" +
                "          <div class='value'>" + util_buildFullName(result_i.firstName, result_i.middleName, result_i.lastName) + "</div>" +
                "          <div class='label'>MRN</div>" +
                "          <div class='value'>" + result_i.mrn + "</div>" +
                "          <div class='label'>Gender</div>" +
                "          <div class='value'>" + result_i.genderName + "</div>" +
                "          <div class='label'>Birth Date</div>" +
                "          <div class='value'>" + showDate(result_i.birthdate) + "</div>" +
                "          <div class='label'>Local ID</div>" +
                "          <div class='value'>" + result_i.localId + "</div>" +
                "          <div class='label'>IRB #</div>" +
                "          <div class='value'>" + result_i.irb + "</div>" +
                "          <div class='label'>Visit Start Time</div>" +
                "          <div class='value'>" + showDateTime(result_i.scheduledStartTime) + "</div>" +
                "          <div class='label'>Visit End Time</div>" +
                "          <div class='value'>" + showDateTime(result_i.scheduledEndTime) + "</div>" +
                "          <div class='label'>Comment</div>" +
                "          <div class='value'>" + result_i.comment + "</div>" +
                "        </div>" +
                "      </div>" +
                "      <div style='clear: both;'></div>" +
                "      <div class='CSSTableGenerator'><table width='100%' border='0' cellspacing='0' cellpadding='5' style='font-size: 90%;'>" +
                "        <tr style='font-weight:bold'>" +
                "          <th>Resource Name</th>" +
                "          <th>Resource Start Time</th>" +
                "          <th>Resource End Time</th>" +
                "        </tr>";
        }

        out += ((tableRow % 2 == 1) ? "<tr class='altRow'>" : "<tr>");
        out += "  <td>" + result_i.resourceName + "</td>" +
            "          <td>" + showDateTime(result_i.resourceStartTime) + "</td>" +
            "          <td>" + showDateTime(result_i.resourceEndTime) + "</td>" +
            "          <td> </td>" +
            "        </tr>";

        previousVisit = currentVisit;
        iteration++;
        tableRow++;
    }

    $('#report_results').html(out);
}


function report_showDailyOverviewResults() {
    var out = "";
    var currentVisit = -1;
    var previousVisit = -1;
    var iteration = 0;
    var tableRow = 0;
    report_handleDataResponse(report_dailyOverviewResult.length);

    for (var i = 0; i < report_dailyOverviewResult.length; i++) {
        var result_i = report_dailyOverviewResult[i];

        currentVisit = result_i.bookedVisitId;

        if (iteration != 0 && currentVisit != previousVisit) {
            out += " </table>" +
                "    <!-- table generator --></div>" +
                "    <!-- report row --></div>" +
                "  <hr class='report' />";
        }

        if (currentVisit != previousVisit) {
            var divVisitStatusClass = null;
            if (result_i.visitStatus == 'Scheduled') {
                divVisitStatusClass = " <div class='statusBlock reportSecondary'>";
            }
            else if (result_i.visitStatus == 'Checked-In') {
                divVisitStatusClass = " <div class='statusBlock reportCheckedInSecondary'>";
            }
            else if (result_i.visitStatus == 'Hold') {
                divVisitStatusClass = " <div class='statusBlock reportSecondary'>";
            }
            else {
                divVisitStatusClass = " <div>";
            }
            tableRow = 0;
            out +=
                "    <div class='reportRow'>" +
                "      <div class='secondaryNameBlock'>" +
                "       <div class='mainName'>" +
                showDateTimeWithThreeLetterMonth(result_i.scheduledStartTime) + " - " +
                showDateTimeWithThreeLetterMonth(result_i.scheduledEndTime) + "</div>" +
                "      </div>" +
                "      <div class='statusBlockContainer'>" +
                divVisitStatusClass +
                "          <div class='label boldReportField'>Subject</div>" +
                "          <div class='value boldReportField'>" +
                util_buildFullName(result_i.firstName, result_i.middleName, result_i.lastName) + "</div>" +
                "          <div class='label'>MRN</div>" +
                "          <div class='value'>" + result_i.mrn + "</div>" +
                "          <div class='label'>Gender</div>" +
                "          <div class='value'>" + result_i.genderName + "</div>" +
                "          <div class='label'>Birth Date</div>" +
                "          <div class='value'>" + result_i.birthdate + "</div>" +
                "          <div class='label'>Local ID</div>" +
                "          <div class='value'>" + result_i.localId + "</div>" +
                "          <div class='label'>IRB #</div>" +
                "          <div class='value'>" + result_i.irb + "</div>" +
                "          <div class='label'>Visit Name</div>" +
                "          <div class='value'>" + result_i.visitName + "</div>" +
                "          <div class='label'>Visit Type</div>" +
                "          <div class='value'>" + result_i.visitType + "</div>" +
                "          <div class='label'>Sublocation</div>" +
                "          <div class='value'>" + result_i.sublocationName + "</div>" +
                "          <div class='label'>Is part of the Visit Off Unit?</div>" +
                "          <div class='value'>" + result_i.offInstitution + "</div>" +
                "          <div class='label'>Check-In</div>" +
                "          <div class='value'>" + '' + "</div>" +
                "          <div class='label'>Check-Out</div>" +
                "          <div class='value'>" + '' + "</div>" +
                "          <div class='label'>Comment</div>" +
                "          <div class='value'>" + '' + "</div>" +
                "        </div>" +
                "      </div>" +
                "      <div style='clear: both;'></div>" +
                "      <div class='CSSTableGenerator'><table width='100%' border='0' cellspacing='0' " +
                "             cellpadding='5' style='font-size: 90%;'>" +
                "        <tr style='font-weight:bold'>" +
                "          <th>Resource Name</th>" +
                "          <th>Resource Start Time</th>" +
                "          <th>Resource End Time</th>" +
                "          <th>Comment</th>" +
                "        </tr>";
        }

        out += ((tableRow % 2 == 1) ? "<tr class='altRow'>" : "<tr>");
        out += "  <td>" + result_i.resourceName + "</td>" +
            "          <td>" + showDateTime(result_i.resourceStartTime) + "</td>" +
            "          <td>" + showDateTime(result_i.resourceEndTime) + "</td>" +
            "          <td>" + result_i.comment + "</td>" +
            "        </tr>";

        previousVisit = currentVisit;
        iteration++;
        tableRow++;
    }

    $('#report_results').html(out);
}


function formatMinsIntoDaysHrsMins(timeInMinutes) {
    if (timeInMinutes != null) {
        var timeDiffInDays = Math.floor(timeInMinutes / 1440);
        var remainingMinutesInDay = timeInMinutes % 1440;
        var remainingHoursInDay = Math.floor(remainingMinutesInDay / 60);
        var remainingMinutesInHour = remainingMinutesInDay % 60;
        return util_formatDaysHrsMins(timeDiffInDays, remainingHoursInDay, remainingMinutesInHour);
    }
    else {
        return "";
    }
}

function formatEndMinusStart(startTime, endTime) {
    if (startTime != null && endTime != null) {
        var startDateMillis = Date.parse(startTime);
        var endDateMillis = Date.parse(endTime);
        var timeDiffInMillis = endDateMillis - startDateMillis;
        var timeDiffInMinutes = Math.floor(timeDiffInMillis / 60000);
        var timeDiffInDays = Math.floor(timeDiffInMinutes / 1440);
        var remainingMinutesInDay = timeDiffInMinutes % 1440;
        var remainingHoursInDay = Math.floor(remainingMinutesInDay / 60);
        var remainingMinutesInHour = remainingMinutesInDay % 60;
        return util_formatDaysHrsMins(timeDiffInDays, remainingHoursInDay, remainingMinutesInHour);
    }
    else {
        return "";
    }
}

function overnightsBetweenDates(startTime, endTime) {
    if (startTime != null && endTime != null) {
        var startDate = new Date(startTime);
        var endDate = new Date(endTime);

        var diffInMillis = endDate - startDate;
        var wholeDays = Math.floor(diffInMillis / millisPerDay);
        var fractionDay = diffInMillis % millisPerDay;

        var startDatePlusFraction = new Date(startDate);
        startDatePlusFraction.setMilliseconds(
            fractionDay + startDate.getMilliseconds());

        var startDateDate = startDate.getDate();
        var startDatePlusFractionDate = startDatePlusFraction.getDate();

        if (startDateDate != startDatePlusFractionDate) {
            // crosses a day boundary, e.g. 1 to 2, or even 31 to 1
            wholeDays++;
        }

        return wholeDays;
    }
    else {
        return "";
    }
}

function endMinusStartInMinutes(startTime, endTime) {
    if (startTime != null && endTime != null) {
        var startDateMillis = Date.parse(startTime);
        var endDateMillis = Date.parse(endTime);
        var timeDiffInMillis = endDateMillis - startDateMillis;
        var timeDiffInMinutes = Math.floor(timeDiffInMillis / 60000);
        return timeDiffInMinutes;
    }
    else {
        return "";
    }
}


function report_showWeeklyPharmResults() {
    var out = "";
    report_handleDataResponse(report_weeklyPharmResult.length);
    var currentStudyId = -1;
    var previousStudyId = -1;
    var iteration = 0;

    for (var i = 0; i < report_weeklyPharmResult.length; i++) {
        var r = report_weeklyPharmResult[i];
        currentStudyId = r.studyId;

        if (iteration != 0 && currentStudyId != previousStudyId) {
            out += " </table>" +
                "    <!-- table generator --></div>" +
                "    <!-- report row --></div>" +
                "  <hr class='report' />";
        }

        var visitLengthLabel = formatEndMinusStart(showDateTime(r.scheduledStartTime), showDateTime(r.scheduledEndTime));
        if (currentStudyId != previousStudyId) {
            tableRow = 0;
            out +=
                "    <div class='reportRow'>" +
                "      <div class='secondaryNameBlock'>" +
                "       <div class='mainName'>" + r.studyName + "</div>" +
                "      </div>" +
                "      <div class='statusBlockContainer'>" +
                " 	<div class='statusBlock reportNoStatusSecondary'>" +
                "          <div class='label'>Local ID</div>" +
                "          <div class='value'>" + r.localId + "</div>" +
                "          <div class='label'>Catalyst ID</div>" +
                "          <div class='value'>" + r.catalystId + "</div>" +
                "          <div class='label'>IRB #</div>" +
                "          <div class='value'>" + r.irb + "</div>" +
                "          <div class='label'>IRB Expiration</div>" +
                "          <div class='value'>" + showValue(r.irbExpiration) + "</div>" +
                "        </div>" +
                "      </div>" +
                "      <div style='clear: both;'></div>" +
                "      <div class='CSSTableGenerator'><table width='100%' border='0' cellspacing='0' cellpadding='5' style='font-size: 90%;'>" +
                "        <tr style='font-weight:bold'>" +
                "          <th width='15%'>Subject</th>" +
                "          <th width='11%'>MRN</th>" +
                "          <th width='11%'>Gender</th>" +
                "          <th width='11%'>Visit Name</th>" +
                "          <th width='14%'>Visit Start Time</th>" +
                "          <th width='14%'>Visit End Time</th>" +
                "          <th width='15%'>Visit Length</th>" +
                "          <th width='12%'>Appointment Status</th>" +
                "        </tr>";
        }

        out += ((iteration % 2 == 1) ? "<tr class='altRow'>" : "<tr>");
        out += "<td>" + util_buildFullName(r.firstName, r.middleName, r.lastName) + "</td>" +
            "          <td>" + r.mrn + "</td>" +
            "          <td>" + r.genderName + "</td>" +
            "          <td>" + r.visitName + "</td>" +
            "          <td>" + showDateTime(r.scheduledStartTime) + "</td>" +
            "          <td>" + showDateTime(r.scheduledEndTime) + "</td>" +
            "          <td>" + visitLengthLabel + "</td>" +
            "          <td>" + r.appointmentStatus + "</td>" +
            "        </tr>";


        previousStudyId = currentStudyId;
        iteration++;
    }

    $('#report_results').html(out);
}


function report_showCancellationsResults() {
    var out = "";
    report_handleDataResponse(report_cancellationsResult.length);

    for (var i = 0; i < report_cancellationsResult.length; i++) {

        var r = report_cancellationsResult[i];
        var visitLengthLabel = formatEndMinusStart(showDateTime(r.scheduledStartTime), showDateTime(r.scheduledEndTime));

        out +=
            "    <div class='reportRow'>" +
            "      <div class='secondaryNameBlock'>" +
            "       <div class='mainName'>" + r.asrName + "</div>" +
            "      </div>" +
            "      <div style='clear: both;'></div>" +
            "      <div class='CSSTableGenerator'><table width='100%' border='0' cellspacing='0' cellpadding='5'>" +
            "        <tr>" +
            "          <td width='21%' align='left' valign='top' style='font-weight:bold'>Subject</td>" +
            "          <td width='29%' align='left' valign='top'>" + util_buildFullName(r.subjectFirstName, r.subjectMiddleName, r.subjectLastName) + "</td>" +
            "          <td width='21%' align='left' valign='top' style='font-weight:bold'>PI Name</td>" +
            "          <td width='29%' align='left' valign='top'>" + util_buildFullName(r.piFirstName, r.piMiddleName, r.piLastName) + "</td>" +
            "        </tr>" +
            "        <tr>" +
            "          <td width='21%' align='left' valign='top' style='font-weight:bold'>MRN</td>" +
            "          <td width='29%' align='left' valign='top'>" + r.mrn + "</td>" +
            "          <td width='21%' align='left' valign='top' style='font-weight:bold'>Cancel Date</td>" +
            "          <td width='29%' align='left' valign='top'>" + showDateTime(r.cancelDate) + "</td>" +
            "        </tr>" +
            "        <tr>" +
            "          <td width='21%' align='left' valign='top' style='font-weight:bold'>Visit Start Time</td>" +
            "          <td width='29%' align='left' valign='top'>" + showDateTime(r.scheduledStartTime) + "</td>" +
            "          <td width='21%' align='left' valign='top' style='font-weight:bold'>Catalyst ID</td>" +
            "          <td width='29%' align='left' valign='top'>" + r.catalystId + "</td>" +
            "        </tr>" +
            "        <tr>" +
            "          <td width='21%' align='left' valign='top' style='font-weight:bold'>Visit End Time</td>" +
            "          <td width='29%' align='left' valign='top'>" + showDateTime(r.scheduledEndTime) + "</td>" +
            "          <td width='21%' align='left' valign='top' style='font-weight:bold'>Local ID</td>" +
            "          <td width='29%' align='left' valign='top'>" + r.localId + "</td>" +
            "        </tr>" +
            "        <tr>" +
            "          <td width='21%' align='left' valign='top' style='font-weight:bold'>Visit Length</td>" +
            "          <td width='29%' align='left' valign='top'>" + visitLengthLabel + "</td>" +
            "          <td width='21%' align='left' valign='top' style='font-weight:bold'>IRB #</td>" +
            "          <td width='29%' align='left' valign='top'>" + r.irb + "</td>" +
            "        </tr>" +
            "        <tr>" +
            "          <td width='21%' align='left' valign='top' style='font-weight:bold'>Study Name</td>" +
            "          <td width='29%' align='left' valign='top' style='word-wrap: break-word;'>" + r.studyName + "</td>" +
            "          <td width='21%' align='left' valign='top' style='font-weight:bold'>Visit Name</td>" +
            "          <td width='29%' align='left' valign='top'>" + r.visitName + "</td>" +
            "        </tr>" +
            "        <tr>" +
            "          <td width='21%' align='left' valign='top' style='font-weight:bold'>Cancellation Type</td>" +
            "          <td width='29%' align='left' valign='top'>" + r.cancelStatus + "</td>" +
            "          <td width='21%' align='left' valign='top' style='font-weight:bold'>Visit Type</td>" +
            "          <td width='29%' align='left' valign='top'>" + r.visitTypeName + "</td>" +
            "        </tr>" +
            "        <tr>" +
            "          <td width='21%' align='left' valign='top' style='font-weight:bold'>User</td>" +
            "          <td width='29%' align='left' valign='top'>" + r.userEcommonsId + "</td>" +
            "          <td width='21%' align='left' valign='top' style='font-weight:bold'>Scheduled Date</td>" +
            "          <td width='29%' align='left' valign='top'>" + showDateTime(r.scheduledDate) + "</td>" +
            "        </tr>" +
            "      </table>" +
            "    </div>" +
            "    </div>" +
            "    <hr class='report' />";
    }

    $('#report_results').html(out);
}


function report_showStudyVisitLocationResults() {
    var out = "";
    report_handleDataResponse(report_studyVisitLocationResult.length);
    var currentVisitTypeId = -1;
    var previousVisitTypeId = -1;
    var currentVisitId = -1;
    var previousVisitId = -1;
    var iteration = 0;
    var tableRow = 0;

    for (var i = 0; i < report_studyVisitLocationResult.length; i++) {
        var r = report_studyVisitLocationResult[i];
        currentVisitTypeId = r.visitTypeId;
        currentVisitId = r.visitId;

        if (iteration != 0 && currentVisitTypeId != previousVisitTypeId && previousVisitId != currentVisitId) {
            out += " </table>" +
                "    <!-- table generator --></div>" +
                "    <!-- report row --></div>" +
                "  <hr class='report' />";
        }

        if (currentVisitTypeId != previousVisitTypeId && previousVisitId != currentVisitId) {
            tableRow = 0;
            out +=
                "    <div class='reportRow'>" +
                "      <div class='secondaryNameBlock'>" +
                "       <div class='mainName'>" + r.sublocationName + "</div>" +
                "      </div>" +
                "      <div class='clear'></div>" +
                "      <div class='CSSTableGenerator'><table width='100%' border='0' cellspacing='0' cellpadding='5' style='font-size: 90%; table-layout: fixed;'>" +
                "        <tr style='font-weight:bold'>" +
                "          <th>Visit Name</th>" +
                "          <th>Visit Type</th>" +
                "          <th>Check In Time</th>" +
                "          <th>Check Out Time</th>" +
                "          <th>Visit Duration</th>" +
                "          <th>Subject Name</th>" +
                "          <th>MRN</th>" +
                "          <th>Gender</th>" +
                "          <th>Study Name</th>" +
                "          <th>Catalyst ID</th>" +
                "          <th>Local ID</th>" +
                "          <th>IRB #</th>" +
                "        </tr>";

        }

        var visitLengthLabel = formatEndMinusStart(showDateTime(r.checkInTime), showDateTime(r.checkOutTime));
        out += ((tableRow % 2 == 1) ? "<tr class='altRow'>" : "<tr>");
        out += "  <td>" + r.visitTypeName + "</td>" +
            "          <td>" + r.visitType + "</td>" +
            "          <td>" + showDateTime(r.checkInTime) + "</td>" +
            "          <td>" + showDateTime(r.checkOutTime) + "</td>" +
            "          <td>" + showValue(visitLengthLabel) + "</td>" +
            "          <td>" + util_buildFullName(r.subjectFirstName, r.subjectMiddleName, r.subjectLastName) + "</td>" +
            "          <td>" + r.mrn + "</td>" +
            "          <td>" + r.genderName + "</td>" +
            "          <td>" + r.studyName + "</td>" +
            "          <td>" + showValue(r.catalystId) + "</td>" +
            "          <td>" + r.localId + "</td>" +
            "          <td>" + showValue(r.irb) + "</td>" +
            "        </tr>";

        previousVisitTypeId = currentVisitTypeId;
        previousVisitId = currentVisitId;
        iteration++;
        tableRow++;
    }

    $('#report_results').html(out);
}


function report_showBillableResourcesResults() {
    var out = "";
    report_handleDataResponse(report_billableResourcesResult.length);
    var currentResourceId = -1;
    var previousResourceId = -1;
    var iteration = 0;
    var tableRow = 0;

    for (var i = 0; i < report_billableResourcesResult.length; i++) {
        var result_i = report_billableResourcesResult[i];
        currentResourceId = result_i.resourceId;

        if (iteration != 0 && currentResourceId != previousResourceId) {
            out += " </table>" +
                "    <!-- table generator --></div>" +
                "    <!-- report row --></div>" +
                "  <hr class='report' />";
        }

        if (currentResourceId != previousResourceId) {
            tableRow = 0;
            out +=
                "    <div class='reportRow'>" +
                "      <div class='secondaryNameBlock'>" +
                "       <div class='mainName'>" + result_i.resourceName + "</div>" +
                "      </div>" +
                "      <div class='clear'></div>" +
                "      <div class='CSSTableGenerator billingResourcesReport'>" +
                "       <table>" +
                "        <tr class='stdReport_columnHeading100px'>" +
                "          <th> Check-In </th>" +
                "          <th> Check-Out </th>" +
                "          <th> Visit Duration </th>" +
                "          <th> Study Subject </th>" +
                "          <th> MRN </th>" +
                "          <th> Study Name </th>" +
                "          <th> Catalyst ID </th>" +
                "          <th> Local ID </th>" +
                "          <th> IRB # </th>" +
                "          <th> PI </th>" +
                "          <th> Industry Initiated </th>" +
                "          <th> CRC Approved Study </th>" +
                "          <th> Visit Name </th>" +
                "          <th> Visit Type </th>" +
                "          <th> Sublocation </th>" +
                "          <th> Resource Start Time </th>" +
                "          <th> Resource End Time </th>" +
                "          <th> Booked Visit Comment </th>" +
                "        </tr>";
        }
        var visitLengthLabel = formatEndMinusStart(showDateTime(result_i.checkInTime), showDateTime(result_i.checkOutTime));

        out += ((tableRow % 2 == 1) ? "<tr class='altRow'>" : "<tr>");
        out += "   <td style='word-wrap: break-word;'>" + showDateTime(result_i.checkInTime) + "</td>" +
            "          <td style='word-wrap: break-word;'>" + showDateTime(result_i.checkOutTime) + "</td>" +
            "          <td style='word-wrap: break-word;'>" + visitLengthLabel + "</td>" +
            "          <td style='word-wrap: break-word;'>" + util_buildFullName(result_i.subjectFirstName, result_i.subjectMiddleName, result_i.subjectLastName) + "</td>" +
            "          <td style='word-wrap: break-word;'>" + result_i.mrn + "</td>" +
            "          <td style='word-wrap: break-word;'>" + result_i.studyName + "</td>" +
            "          <td style='word-wrap: break-word;'>" + showValue(result_i.catalystId) + "</td>" +
            "          <td style='word-wrap: break-word;'>" + result_i.localId + "</td>" +
            "          <td style='word-wrap: break-word;'>" + showValue(result_i.irb) + "</td>" +
            "          <td style='word-wrap: break-word;'>" + util_buildFullName(result_i.piFirstName, result_i.piMiddleName, result_i.piLastName) + "</td>" +
            "          <td style='word-wrap: break-word;'>" + yesNoValue(result_i.industryInitiated) + "</td>" +
            "          <td style='word-wrap: break-word;'>" + yesNoValue(result_i.crcFunded) + "</td>" +
            "          <td style='word-wrap: break-word;'>" + result_i.visitName + "</td>" +
            "          <td style='word-wrap: break-word;'>" + result_i.visitTypeName + "</td>" +
            "          <td style='word-wrap: break-word;'>" + result_i.sublocationName + "</td>" +
            "          <td style='word-wrap: break-word;'>" + showDateTime(result_i.scheduledStartTime) + "</td>" +
            "          <td style='word-wrap: break-word;'>" + showDateTime(result_i.scheduledEndTime) + "</td>" +
            "          <td style='word-wrap: break-word;'>" + showValue(result_i.bookedVisitComment) + "</td>" +
            "        </tr>";

        previousResourceId = currentResourceId;
        iteration++;
        tableRow++;
    }
    $('#report_results').html(out);
}


function report_showTransactionsResults() {
    var out = "";
    report_handleDataResponse(report_transactionsResult.length);
    var currentAsrId = -1;
    var previousAsrId = -1;
    var iteration = 0;
    var tableRow = 0;

    for (var i = 0; i < report_transactionsResult.length; i++) {
        var r = report_transactionsResult[i];
        currentAsrId = r.asrId;

        if (iteration != 0 && currentAsrId != previousAsrId) {
            out += " </table>" +
                "    <!-- table generator --></div>" +
                "    <!-- report row --></div>" +
                "  <hr class='report' />";
        }

        if (currentAsrId != previousAsrId) {
            tableRow = 0;
            out +=
                "    <div class='reportRow'>" +
                "      <div class='secondaryNameBlock'>" +
                "       <div class='mainName'>" + r.visitName + " - " + r.appointmentStatus + "</div>" +
                "      </div>" +
                "      <div class='clear'></div>" +
                "      <div class='CSSTableGenerator'><table width='100%' border='0' cellspacing='0' cellpadding='5' style='font-size: 90%;'>" +
                "        <tr style='font-weight:bold'>" +
                "          <th class='stdReport_AppointmentStatusReason'>Appointment Status Reason</th>" +
                "          <th class='stdReport_StatusChangeTime'>Status Change Time</th>" +
                "          <th class='stdReport_CancellationType'>Cancellation Type</th>" +
                "          <th class='stdReport_CancellationReason'>Cancellation Reason</th>" +
                "          <th class='stdReport_UserInitiatingChange'>User Initiating Change</th>" +
                "          <th class='stdReport_ScheduledStartTime'>Scheduled Start Time</th>" +
                "          <th class='stdReport_ScheduledEndTime'>Scheduled End Time</th>" +
                "          <th class='stdReport_VisitLength'>Visit Length</th>" +
                "          <th class='stdReport_StudyName'>Study Name</th>" +
                "          <th class='stdReport_PI'>PI</th>" +
                "          <th class='stdReport_PrimaryContact'>Primary Contact</th>" +
                "          <th class='stdReport_CatalystID'>Catalyst ID</th>" +
                "          <th class='stdReport_LocalID'>Local ID</th>" +
                "          <th class='stdReport_IRB'>IRB #</th>" +
                "          <th class='stdReport_SubjectName'>Subject Name</th>" +
                "          <th class='stdReport_MRN'>MRN</th>" +
                "        </tr>";
        }

        var visitLengthLabel = formatEndMinusStart(showDateTime(r.scheduledStartTime), showDateTime(r.scheduledEndTime));

        out += ((tableRow % 2 == 1) ? "<tr class='altRow'>" : "<tr>");
        out += "   <td>" + r.asrName + "</td>" +
            "          <td>" + showDateTime(r.cancelTime) + "</td>" +
            "          <td>" + r.cancelStatus + "</td>" +
            "          <td>" + r.cancelStatusReason + "</td>" +
            "          <td>" + r.ecommonsId + "</td>" +
            "          <td>" + showDateTime(r.scheduledStartTime) + "</td>" +
            "          <td>" + showDateTime(r.scheduledEndTime) + "</td>" +
            "          <td>" + visitLengthLabel + "</td>" +
            "          <td>" + r.studyName + "</td>" +
            "          <td>" + util_buildFullName(r.piFirstName, r.piMiddleName, r.piLastName) + "</td>" +
            "          <td>" + util_buildFullName(r.psFirstName, r.psMiddleName, r.psLastName) + "</td>" +
            "          <td>" + r.catalystId + "</td>" +
            "          <td>" + r.localId + "</td>" +
            "          <td>" + r.irb + "</td>" +
            "          <td>" + util_buildFullName(r.subjectFirstName, r.subjectMiddleName, r.subjectLastName) + "</td>" +
            "          <td>" + r.mrn + "</td>" +
            "        </tr>";

        previousAsrId = currentAsrId;
        iteration++;
        tableRow++;
    }

    $('#report_results').html(out);
}


function report_showDailyResourceResults() {
    var out = "";
    report_handleDataResponse(report_dailyResourceResult.length);
    var currentResourceTypeId = -1;
    var previousResourceTypeId = -1;
    var iteration = 0;
    var tableRow = 0;

    for (var i = 0; i < report_dailyResourceResult.length; i++) {
        var r = report_dailyResourceResult[i];
        currentResourceTypeId = r.resourceTypeId;

        if (iteration != 0 && currentResourceTypeId != previousResourceTypeId) {
            out += " </table>" +
                "    <!-- table generator --></div>" +
                "    <!-- report row --></div>" +
                "  <hr class='report' />";
        }
        if (currentResourceTypeId != previousResourceTypeId) {
            tableRow = 0;
            out +=
                "    <div class='reportRow'>" +
                "      <div class='secondaryNameBlock'>" +
                "       <div class='mainName'>" + r.resourceName + "</div>" +
                "      </div>" +
                "      <div class='resourceTypeValue'>" +
                "        Resource Type: <span style='font-weight: bold;'>" + r.resourceTypeName + "</span>" +
                "      </div>" +
                "      <div style='clear: both; height: 5px'></div>" +
                "      <div class='CSSTableGenerator'><table width='100%' border='0' cellspacing='0' cellpadding='5' style='font-size: 90%;'>" +
                "        <tr style='font-weight:bold'>" +
                "          <th style='width:12%;'>Subject Name</th>" +
                "          <th style='width:8%;'>MRN</th>" +
                "          <th style='width:9%;'>Local ID</th>" +
                "          <th style='width:10%;'>IRB #</th>" +
                "          <th style='width:10%;'>Visit ID</th>" +
                "          <th style='width:17%;'>Visit Name</th>" +
                "          <th style='width:11%;'>Resource Start Time</th>" +
                "          <th style='width:11%;'>Resource End Time</th>" +
                "          <th style='width:13%;'>Resource Usage Duration</th>" +
                "        </tr>";
        }

        var visitLengthLabel = formatEndMinusStart(showDateTime(r.scheduledStartTime), showDateTime(r.scheduledEndTime));
        out += ((tableRow % 2 == 1) ? "<tr class='altRow'>" : "<tr>");
        out += "   <td>" + util_buildFullName(r.subjectFirstName, r.subjectMiddleName, r.subjectLastName) + "</td>" +
            "          <td>" + r.mrn + "</td>" +
            "          <td>" + r.localId + "</td>" +
            "          <td>" + r.irb + "</td>" +
            "          <td>" + r.visitId + "</td>" +
            "          <td>" + r.visitName + "</td>" +
            "          <td>" + showDateTime(r.scheduledStartTime) + "</td>" +
            "          <td>" + showDateTime(r.scheduledEndTime) + "</td>" +
            "          <td>" + visitLengthLabel + "</td>" +
            "        </tr>";

        if (r.comment && r.comment != "") {
            out += ((tableRow % 2 == 1) ? "<tr class='altRow'>" : "<tr>");
            out +=
                "          <td class='indent11px' colspan='9'>Comment: " + r.comment + "</td>" +
                "        </tr>";
        }

        previousResourceTypeId = currentResourceTypeId;
        iteration++;
        tableRow++;
    }

    $('#report_results').html(out);
}


function report_showDailyAdmResults() {
    var out = "";
    report_handleDataResponse(report_dailyAdmResult.length);
    var currentSubjectId = -1;
    var previousSubjectId = -1;
    var iteration = 0;
    var tableRow = 0;

    for (var i = 0; i < report_dailyAdmResult.length; i++) {
        var r = report_dailyAdmResult[i];
        currentSubjectId = r.subjectId;
        if (iteration != 0 && currentSubjectId != previousSubjectId) {
            out += " </table>" +
                "    <!-- table generator --></div>" +
                "    <!-- report row --></div>" +
                "  <hr class='report' />";
        }
        if (currentSubjectId != previousSubjectId) {
            var divVisitStatusClass = null;
            if (r.visitStatus == 'Scheduled') {
                divVisitStatusClass = " <div class='statusBlock reportSecondary'>";
            }
            else if (r.visitStatus == 'Checked-In') {
                divVisitStatusClass = " <div class='statusBlock reportCheckedInSecondary'>";
            }
            tableRow = 0;
            out +=
                "    <div class='reportRow'>" +
                "      <div class='secondaryNameBlock'>" +
                "       <div class='mainName'>" + util_buildFullName(r.subjectFirstName, r.subjectMiddleName, r.subjectLastName) + "</div>" +
                "      </div>" +
                "      <div class='statusBlockContainer'>" +
                divVisitStatusClass +
                "          <div class='label'>DOB</div>" +
                "          <div class='value'>" + r.birthdate + "</div>" +
                "          <div class='label'>MRN</div>" +
                "          <div class='value'>" + r.mrn + "</div>" +
                "          <div class='label'>Gender</div>" +
                "          <div class='value'>" + r.genderName + "</div>" +
                "          <div class='label'>Local ID</div>" +
                "          <div class='value'>" + r.localId + "</div>" +
                "          <div class='label'>IRB #</div>" +
                "          <div class='value'>" + r.irb + "</div>" +
                "          <div class='label'>Visit Name</div>" +
                "          <div class='value'>" + r.visitName + "</div>" +
                "          <div class='label'>Visit Status</div>" +
                "          <div class='value'>" + r.visitStatus + "</div>" +
                "          <div class='label'>Check In Time</div>" +
                "          <div class='value'>" + showDateTime(r.checkInTime) + "</div>" +
                "        </div>" +
                "      </div>" +
                "      <div style='clear: both;'></div>" +
                "      <div class='CSSTableGenerator'><table  width='100%' border='0' cellspacing='0' cellpadding='5' style='font-size: 90%;'>" +
                "        <tr style='font-weight:bold'>" +
                "          <th>Resource Name</th>" +
                "          <th>Resource Start Time</th>" +
                "          <th>Resource End Time</th>" +
                "          <th>Comment</th>" +
                "        </tr>";
        }

        previousSubjectId = currentSubjectId;

        if (currentSubjectId == previousSubjectId) {
            out += ((tableRow % 2 == 1) ? "<tr class='altRow'>" : "<tr>");
            out += "   <td>" + r.resourceName + "</td>" +
                "          <td>" + showDateTime(r.scheduledStartTime) + "</td>" +
                "          <td>" + showDateTime(r.scheduledEndTime) + "</td>" +
                "          <td>" + r.comment + "</td>" +
                "        </tr>";
        }

        iteration++;
        tableRow++;
    }

    $('#report_results').html(out);
}

function report_showMetaKitchenResults() {
    var out = "";
    report_handleDataResponse(report_metaKitchenResult.length);
    var currentSubjectId = -1;
    var previousSubjectId = -1;
    var iteration = 0;
    var tableRow = 0;

    for (var i = 0; i < report_metaKitchenResult.length; i++) {
        var r = report_metaKitchenResult[i];

        // !! this should go within the below if block
        currentSubjectId = r.subjectId;

        if (r.resourceName != undefined) {
            if (iteration != 0 && currentSubjectId != previousSubjectId) {
                out += " </table>" +
                    "    <!-- table generator --></div>" +
                    "    <!-- report row --></div>" +
                    "  <hr class='report' />";
            }
            if (currentSubjectId != previousSubjectId) {
                tableRow = 0;
                out +=
                    "    <div class='reportRow'>" +
                    "      <div class='secondaryNameBlock'>" +
                    "       <div class='mainName'>" + util_buildFullName(r.subjectFirstName, r.subjectMiddleName, r.subjectLastName) + "</div>" +
                    "      </div>" +
                    "      <div class='statusBlockContainer'>" +
                    "        <div class='statusBlock reportNoStatusSecondary'>" +
                    "          <div class='label'>MRN</div>" +
                    "          <div class='value'>" + r.mrn + "</div>" +
                    "          <div class='label'>DOB</div>" +
                    "          <div class='value'>" + r.birthdate + "</div>" +
                    "          <div class='label'>Local ID</div>" +
                    "          <div class='value'>" + r.localId + "</div>" +
                    "        </div>" +
                    "      </div>" +
                    "      <div style='clear: both;'></div>" +
                    "      <div class='CSSTableGenerator'><table width='100%' border='0' cellspacing='0' cellpadding='5' style='font-size: 90%;'>" +
                    "        <tr style='font-weight:bold'>" +
                    "          <th>Meal Start Date/Time</th>" +
                    "          <th>Resource Name</th>" +
                    "          <th>Room</th>" +
                    "          <th>Visit Start Date/Time</th>" +
                    "        </tr>";
            }

            out += ((tableRow % 2 == 1) ? "<tr class='altRow'>" : "<tr>");
            out += "   <td>" + showDateTime(r.scheduledStartTime) + "</td>" +
                "          <td>" + r.resourceName + "</td>" +
                "          <td>" + showValue(r.room) + "</td>" +
                "          <td>" + showDateTime(r.checkInTime) + ' - ' + showDateTime(r.checkOutTime) + "</td>" +
                "        </tr>";
            previousSubjectId = currentSubjectId;


            iteration++;
            tableRow++;
        }
    }

    $('#report_results').html(out);
}

function report_showMetaKitchenByTimeResults() {
    var out = "";
    report_handleDataResponse(report_metaKitchenByTimeResult.length);
    var currentSubjectId = -1;
    var previousSubjectId = -1;
    var iteration = 0;
    var tableRow = 0;
    for (var i = 0; i < report_metaKitchenByTimeResult.length; i++) {
        var j = i;
        var r = report_metaKitchenByTimeResult[i];
        var rs = report_metaKitchenByTimeResult[j++];

        // !! this should go within if block
        currentSubjectId = r.subjectId;
        if (r.resourceName != undefined) {
            if (iteration != 0 && currentSubjectId != previousSubjectId) {
                out += " </table>" +
                    "    <!-- table generator --></div>" +
                    "    <!-- report row --></div>" +
                    "  <hr class='report' />";
            }

            if (currentSubjectId != previousSubjectId) {
                tableRow = 0;
                out +=
                    "    <div class='reportRow'>" +
                    "      <div style='clear: both;'></div>" +
                    "      <div class='CSSTableGenerator'><table width='100%' border='0' cellspacing='0' cellpadding='5' style='font-size: 90%;'>" +
                    "        <tr style='font-weight:bold'>" +
                    "          <th>Meal Start Date/Time</th>" +
                    "          <th>Subject Name</th>" +
                    "          <th>Local ID</th>" +
                    "          <th>Resource Name</th>" +
                    "          <th>MRN</th>" +
                    "          <th>DOB</th>" +
                    "          <th>Room</th>" +
                    "          <th>Visit Start/End Time</th>" +
                    "        </tr>";
            }

            out += ((tableRow % 2 == 1) ? "<tr class='altRow'>" : "<tr>");
            out += "   <td>" + showDateTime(r.scheduledStartTime) + "</td>" +
                "          <td>" + util_buildFullName(r.subjectFirstName, r.subjectMiddleName, r.subjectLastName) + "</td>" +
                "          <td>" + r.localId + "</td>" +
                "          <td>" + r.resourceName + "</td>" +
                "          <td>" + r.mrn + "</td>" +
                "          <td>" + r.birthdate + "</td>" +
                "          <td>" + showValue(r.room) + "</td>" +
                "          <td>" + showDateTime(r.checkInTime) + ' - ' + showDateTime(r.checkOutTime) + "</td>" +
                "        </tr>";
            previousSubjectId = currentSubjectId;

            iteration++;
            tableRow++;
        }
    }

    $('#report_results').html(out);
}

function report_showStudySubjectVisitResults() {
    var out = "";
    report_handleDataResponse(report_studySubjectVisitResult.length);
    var currentSubjectId = -1;
    var previousSubjectId = -1;
    var iteration = 0;
    var tableRow = 0;

    for (var i = 0; i < report_studySubjectVisitResult.length; i++) {
        var r = report_studySubjectVisitResult[i];
        currentSubjectId = r.mrn;

        if (iteration != 0 && currentSubjectId != previousSubjectId) {
            out += " </table>" +
                "    <!-- table generator --></div>" +
                "    <!-- report row --></div>" +
                "  <hr class='report' />";
        }
        if (currentSubjectId != previousSubjectId) {
            tableRow = 0;
            out +=
                "    <div class='reportRow'>" +
                "      <div class='secondaryNameBlock'>" +
                "       <div class='mainName'>" + util_buildFullName(r.subjectFirstName, r.subjectMiddleName, r.subjectLastName) + "</div>" +
                "      </div>" +
                "      <div class='statusBlockContainer'>" +
                "        <div class='statusBlock reportNoStatusSecondary'>" +
                "          <div class='label'>MRN</div>" +
                "          <div class='value'>" + r.mrn + "</div>" +
                "          <div class='label'>DOB</div>" +
                "          <div class='value'>" + r.dob + "</div>" +
                "        </div>" +
                "      </div>" +
                "      <div style='clear: both;'></div>" +
                "      <div class='CSSTableGenerator'><table width='100%' border='0' cellspacing='0' cellpadding='5' style='font-size: 90%;'>" +
                "        <tr style='font-weight:bold'>" +
                "          <th>Visit Name</th>" +
                "          <th>Scheduled Visit Start Time</th>" +
                "          <th>Scheduled Visit End Time</th>" +
                "          <th>Appointment Status</th>" +
                "          <th>Local Id</th>" +
                "        </tr>";
        }

        out += ((tableRow % 2 == 1) ? "<tr class='altRow'>" : "<tr>");
        out += "   <td>" + r.visitName + "</td>" +
            "          <td>" + showDateTime(r.scheduledStartTime) + "</td>" +
            "          <td>" + showDateTime(r.scheduledEndTime) + "</td>" +
            "          <td>" + r.appointmentStatus + "</td>" +
            "          <td>" + r.localId + "</td>" +
            "        </tr>";
        previousSubjectId = currentSubjectId;
        iteration++;
        tableRow++;
    }

    $('#report_results').html(out);
}

function report_showStudyStatusChangeResults() {
    var out = "";
    report_handleDataResponse(report_studyStatusChangeResult.length);
    var currentStudyId = -1;
    var previousStudyId = -1;
    var iteration = 0;
    var tableRow = 0;
    for (var i = 0; i < report_studyStatusChangeResult.length; i++) {
        var r = report_studyStatusChangeResult[i];
        currentStudyId = r.studyId;

        if (iteration != 0 && currentStudyId != previousStudyId) {
            out += " </table>" +
                "    <!-- table generator --></div>" +
                "    <!-- report row --></div>" +
                "  <hr class='report' />";
        }
        if (currentStudyId != previousStudyId) {
            tableRow = 0;
            out +=
                "    <div class='reportRow'>" +
                "      <div class='secondaryNameBlock'>" +
                "       <div class='mainName'>" + r.studyName + "</div>" +
                "      </div>" +
                "      <div class='statusBlockContainer'>" +
                "        <div class='statusBlock reportNoStatusSecondary'>" +
                "          <div class='label'>Local ID</div>" +
                "          <div class='value'>" + r.localId + "</div>" +
                "          <div class='label'>PI</div>" +
                "          <div class='value'>" + showValue(r.pi) + "</div>" +
                "        </div>" +
                "      </div>" +
                "      <div style='clear: both;'></div>" +
                "      <div class='CSSTableGenerator'><table width='100%' border='0' cellspacing='0' cellpadding='5' style='font-size: 90%;'>" +
                "        <tr style='font-weight:bold'>" +
                "          <th>Study Status</th>" +
                "          <th>Date of Status Change</th>" +
                "          <th>User</th>" +
                "        </tr>";
        }

        out += ((tableRow % 2 == 1) ? "<tr class='altRow'>" : "<tr>");
        out += "    <td>" + showValue(r.studyStatus) + "</td>" +
            "          <td>" + showDateTime(r.dateStatusChange) + "</td>" +
            "          <td>" + util_buildFullName(r.userFirstName, r.userMiddleName, r.userLastName) + "</td>" +
            "        </tr>";

        previousStudyId = currentStudyId;
        iteration++;
        tableRow++;
    }

    $('#report_results').html(out);
}
function report_showSubjectVisitHistoryResults() {
    var out = "";
    report_handleDataResponse(report_subjectVisitHistoryResult.length);
    var currentSubjectId = -1;
    var previousSubjectId = -1;
    var iteration = 0;
    var tableRow = 0;

    for (var i = 0; i < report_subjectVisitHistoryResult.length; i++) {
        var r = report_subjectVisitHistoryResult[i];
        currentSubjectId = r.mrn;

        if (iteration != 0 && currentSubjectId != previousSubjectId) {
            out += " </table>" +
                "    <!-- table generator --></div>" +
                "    <!-- report row --></div>" +
                "  <hr class='report' />";
        }
        if (currentSubjectId != previousSubjectId) {
            tableRow = 0;
            out +=
                "    <div class='reportRow'>" +
                "      <div class='secondaryNameBlock'>" +
                "       <div class='mainName'>" + util_buildFullName(r.subjectFirstName, r.subjectMiddleName, r.subjectLastName) + "</div>" +
                "      </div>" +
                "      <div class='statusBlockContainer'>" +
                "        <div class='statusBlock reportNoStatusSecondary'>" +
                "          <div class='label'>MRN</div>" +
                "          <div class='value'>" + r.mrn + "</div>" +
                "        </div>" +
                "      </div>" +
                "      <div style='clear: both;'></div>" +
                "      <div class='CSSTableGenerator'><table width='100%' border='0' cellspacing='0' cellpadding='5' style='font-size: 90%; table-layout: fixed;'>" +
                "        <tr style='font-weight:bold'>" +
                "          <th>Visit Name</th>" +
                "          <th>Scheduled Visit Start Time</th>" +
                "          <th>Scheduled Visit End Time</th>" +
                "          <th>Appointment Status</th>" +
                "          <th>Local Id</th>" +
                "          <th>Cancel Date</th>" +
                "          <th>Cancel Status Reason</th>" +
                "          <th>Check In Date</th>" +
                "          <th>Check Out Date</th>" +
                "        </tr>";
        }

        out += ((tableRow % 2 == 1) ? "<tr class='altRow'>" : "<tr>");
        out += "   <td>" + r.visitName + "</td>" +
            "          <td>" + showDateTime(r.scheduledStartTime) + "</td>" +
            "          <td>" + showDateTime(r.scheduledEndTime) + "</td>" +
            "          <td>" + r.appointmentStatus + "</td>" +
            "          <td>" + r.localId + "</td>" +
            "          <td>" + showDateTime(r.cancelDate) + "</td>" +
            "          <td>" + showValue(r.cancelStatusReason) + "</td>" +
            "          <td>" + showDateTime(r.checkInDate) + "</td>" +
            "          <td>" + showDateTime(r.checkOutDate) + "</td>" +
            "        </tr>";

        previousSubjectId = currentSubjectId;
        iteration++;
        tableRow++;
    }

    $('#report_results').html(out);
}


function report_showDeptAndPiResults() {
    var out = "";
    report_handleDataResponse(report_deptAndPiResult.length);
    var currentPiId = -1;
    var previousPiId = -1;
    var iteration = 0;
    var tableRow = 0;

    for (var i = 0; i < report_deptAndPiResult.length; i++) {
        var r = report_deptAndPiResult[i];
        currentPiId = r.piId;

        if (iteration != 0 && currentPiId != previousPiId) {
            out += " </table>" +
                "    <!-- table generator --></div>" +
                "    <!-- report row --></div>" +
                "  <hr class='report' />";
        }

        if (currentPiId != previousPiId) {
            tableRow = 0;
            out +=
                "    <div class='reportRow'>" +
                "      <div class='secondaryNameBlock'>" +
                "       <div class='mainName'>" + util_buildFullName(r.piFirstName, r.piMiddleName, r.piLastName) + "</div>" +
                "      </div>" +
                "      <div class='statusBlockContainer'>" +
                "        <div class='statusBlockBlackText reportCheckedOutSecondary'>" +
                "          <div class='label'>Department</div>" +
                "          <div class='value'>" + r.piDepartment + "</div>" +
                "          <div class='label'>Division</div>" +
                "          <div class='value'>" + r.piDivision + "</div>" +
                "          <div class='label'>Faculty Rank</div>" +
                "          <div class='value'>" + r.piFacultyRank + "</div>" +
                "          <div class='label'>Credential</div>" +
                "          <div class='value'>" + r.piCredential + "</div>" +
                "          <div class='label'>Institution</div>" +
                "          <div class='value'>" + r.piInstitution + "</div>" +
                "        </div>" +
                "      </div>" +
                "      <div style='clear: both;'></div>" +
                "      <div class='CSSTableGenerator'><table width='100%' border='0' cellspacing='0' cellpadding='5' style='font-size: 90%;'>" +
                "        <tr style='font-weight:bold'>" +
                "          <th>Study Name</th>" +
                "          <th>Catalyst ID</th>" +
                "          <th>Local ID</th>" +
                "          <th>IRB #</th>" +
                "          <th>Visit Name</th>" +
                "          <th>Check In Time</th>" +
                "          <th>Check Out Time</th>" +
                "          <th>Study Subject</th>" +
                "          <th>MRN</th>" +
                "        </tr>";
        }

        out += ((tableRow % 2 == 1) ? "<tr class='altRow'>" : "<tr>");
        out += "   <td>" + r.studyName + "</td>" +
            "          <td>" + showValue(r.catalystId) + "</td>" +
            "          <td>" + r.localId + "</td>" +
            "          <td>" + showValue(r.irb) + "</td>" +
            "          <td>" + r.visitName + "</td>" +
            "          <td>" + showDateTime(r.checkInTime) + "</td>" +
            "          <td>" + showDateTime(r.checkOutTime) + "</td>" +
            "          <td>" + util_buildFullName(r.subjectFirstName, r.subjectMiddleName, r.subjectLastName) + "</td>" +
            "          <td>" + r.mrn + "</td>" +
            "        </tr>";

        previousPiId = currentPiId;
        iteration++;
        tableRow++;
    }

    $('#report_results').html(out);
}

function report_showBillingResults() {
    var out = "";
    report_handleDataResponse(report_billingResult.length);
    var currentStudyId = -1;
    var previousStudyId = -1;
    var iteration = 0;
    var tableRow = 0;

    for (var i = 0; i < report_billingResult.length; i++) {
        var r = report_billingResult[i];
        currentStudyId = r.studyId;
        var visitLengthLabel = formatEndMinusStart(showDateTime(r.visitCheckInTime), showDateTime(r.visitCheckOutTime));
        var visitLengthLabel2 = formatEndMinusStart(showDateTime(r.checkInTime), showDateTime(r.checkOutTime));
        var visitLengthLabel3 = endMinusStartInMinutes(showDateTime(r.visitCheckInTime), showDateTime(r.visitCheckOutTime));
        var visitLengthLabel4 = endMinusStartInMinutes(showDateTime(r.checkInTime), showDateTime(r.checkOutTime));
        var startDate = dateFormat(r.scheduledStartTime, 'd');
        var endDate = dateFormat(r.scheduledEndTime, 'd');
        var overMidnight;
        if (startDate < endDate) {
            overMidnight = "YES";
        }
        else {
            overMidnight = "NO";
        }
        if (iteration != 0 && currentStudyId != previousStudyId) {
            out += " </table>" +
                "    <!-- table generator --></div>" +
                "    <!-- report row --></div>" +
                "  <hr class='report' />";
        }

        if (currentStudyId != previousStudyId) {
            tableRow = 0;
            out +=
                "    <div class='reportRow'>" +
                "      <div class='secondaryNameBlock'>" +
                "       <div class='mainName'>" + r.studyName + "</div>" +
                "      </div>" +
                "      <div class='statusBlockContainer'>" +
                "        <div class='statusBlockBlackText reportCheckedOutSecondary'>" +
                "          <div class='label'>Catalyst ID</div>" +
                "          <div class='value'>" + r.catalystId + "</div>" +
                "          <div class='label'>Local ID</div>" +
                "          <div class='value'>" + r.localId + "</div>" +
                "          <div class='label'>IRB #</div>" +
                "          <div class='value'>" + r.irb + "</div>" +
                "          <div class='label'>Industry Initiated</div>" +
                "          <div class='value'>" + yesNoValue(r.industryInitiated) + "</div>" +
                "          <div class='label'>PI Name</div>" +
                "          <div class='value'>" + util_buildFullName(r.investigatorFirstName, r.investigatorMiddleName, r.investigatorLastName) + "</div>" +
                "          <div class='label'>CRC Approved Study</div>" +
                "          <div class='value'>" + yesNoValue(r.crcFunded) + "</div>" +
                "        </div>" +
                "      </div>" +
                "      <div style='clear: both;'></div>" +
                // TODO: implement CSS for scrolling the table horizontally the same way as billable resources
                "      <div class='CSSTableGenerator'>" +
                "       <table width='100%' border='0' cellspacing='0' cellpadding='5' style='font-size: 90%;'>" +
                "        <tr style='font-weight:bold'>" +
                "          <th class='stdReport_ResourceStartTime'>Resource Start Time</th>" +
                "          <th class='stdReport_ResourceEndTime'>Resource End Time</th>" +
                "          <th class='stdReport_ResourceDuration'>Resource Duration</th>" +
                "          <th class='stdReport_CheckIn'>Check In</th>" +
                "          <th class='stdReport_CheckOut'>Check Out</th>" +
                "          <th class='stdReport_VisitDuration'>Visit Duration</th>" +
                "          <th class='stdReport_VisitDurationTotalMins'>Visit Duration total mins</th>" +
                "          <th class='stdReport_VisitOverMidnight'>Visit Over Midnight?</th>" +
                "          <th class='stdReport_Sublocation'>Sublocation</th>" +
                "          <th class='stdReport_ResourceName'>Resource Name</th>" +
                "          <th class='stdReport_Billable'>Billable</th>" +
                "          <th class='stdReport_VisitName'>Visit Name</th>" +
                "          <th class='stdReport_VisitType'>Visit Type</th>" +
                "          <th class='stdReport_StudySubject'>Study Subject</th>" +
                "          <th class='stdReport_MRN'>MRN</th>" +
                "          <th class='stdReport_BirthDate'>Birth Date</th>" +
                "          <th class='stdReport_BookedVisitComment'> Booked Visit Comment </th>" +
                "        </tr>";
        }
        out += ((tableRow % 2 == 1) ? "<tr class='altRow'>" : "<tr>");
        out += "   <td>" + showDateTime(r.checkInTime) + "</td>" +
            "          <td>" + showDateTime(r.checkOutTime) + "</td>" +
            "          <td>" + visitLengthLabel2 + "</td>" +
            "          <td>" + showDateTime(r.visitCheckInTime) + "</td>" +
            "          <td>" + showDateTime(r.visitCheckOutTime) + "</td>" +
            "          <td>" + visitLengthLabel + "</td>" +
            "          <td>" + visitLengthLabel3 + "</td>" +
            "          <td>" + overMidnight + "</td>" +
            "          <td>" + r.sublocation + "</td>" +
            "          <td>" + r.resourceName + "</td>" +
            "          <td>" + yesNoValue(r.billable) + "</td>" +
            "          <td>" + r.visitName + "</td>" +
            "          <td>" + r.visitType + "</td>" +
            "          <td>" + util_buildFullName(r.subjectFirstName, r.subjectMiddleName, r.subjectLastName) + "</td>" +
            "          <td>" + r.mrn + "</td>" +
            "          <td>" + r.birthdate + "</td>" +
            "          <td>" + showValue(r.bookedVisitComment) + "</td>" +
            "        </tr>";

        previousStudyId = currentStudyId;
        iteration++;
        tableRow++;
    }

    out += "</table></div></div>";
    $('#report_results').html(out);
}


function report_showBillingByInvestigatorResults() {
    var out = "";
    report_handleDataResponse(report_billingByInvestigatorResult.length);
    var currentPiId = -1;
    var previousPiId = -1;
    var iteration = 0;
    var tableRow = 0;

    for (var i = 0; i < report_billingByInvestigatorResult.length; i++) {
        var r = report_billingByInvestigatorResult[i];
        currentPiId = r.piId;
        var visitLengthLabel = formatEndMinusStart(showDateTime(r.checkInTime), showDateTime(r.checkOutTime));
        var visitLengthLabel1 = formatEndMinusStart(showDateTime(r.scheduledStartTime), showDateTime(r.scheduledEndTime));
        if (iteration != 0 && currentPiId != previousPiId) {
            out += " </table>" +
                "    <!-- table generator --></div>" +
                "    <!-- report row --></div>" +
                "  <hr class='report' />";
        }

        if (currentPiId != previousPiId) {
            tableRow = 0;
            out +=
                "    <div class='reportRow'>" +
                "      <div class='secondaryNameBlock'>" +
                "       <div class='mainName'>" + util_buildFullName(r.piFirstName, r.piMiddleName, r.piLastName) + "</div>" +
                "      </div>" +
                "      <div class='statusBlockContainer'>" +
                "        <div class='statusBlockBlackText reportCheckedOutSecondary'>" +
                "          <div class='label'>Study Name</div>" +
                "          <div class='value'>" + r.studyName + "</div>" +
                "          <div class='label'>Catalyst ID</div>" +
                "          <div class='value'>" + r.catalystId + "</div>" +
                "          <div class='label'>Local ID</div>" +
                "          <div class='value'>" + r.localId + "</div>" +
                "          <div class='label'>IRB </div>" +
                "          <div class='value'>" + r.irb + "</div>" +
                "          <div class='label'>Industry Initiated </div>" +
                "          <div class='value'>" + yesNoValue(r.industryInitiated) + "</div>" +
                "          <div class='label'>CRC Approved Study</div>" +
                "          <div class='value'>" + yesNoValue(r.crcFunded) + "</div>" +
                "        </div>" +
                "      </div>" +
                "      <div style='clear: both;'></div>" +
                "      <div class='CSSTableGenerator'><table width='100%' border='0' cellspacing='0' cellpadding='5' style='font-size: 90%; table-layout: fixed;'>" +
                "        <tr style='font-weight:bold'>" +
                "          <th>Visit Name</th>" +
                "          <th>Study Subject</th>" +
                "          <th>MRN</th>" +
                "          <th>Check In Time</th>" +
                "          <th>Check Out Time</th>" +
                "          <th>Visit Length</th>" +
                "          <th>Resource Start Time</th>" +
                "          <th>Resource End Time</th>" +
                "          <th>Resource Duration</th>" +
                "          <th>Resource Name</th>" +
                "          <th>Booked Visit Comment </th>" +
                "        </tr>";
        }

        out += ((tableRow % 2 == 1) ? "<tr class='altRow'>" : "<tr>");
        out += "   <td style='word-wrap: break-word;'>" + r.visitName + "</td>" +
            "          <td style='word-wrap: break-word;'>" + util_buildFullName(r.subjectFirstName, r.subjectMiddleName, r.subjectLastName) + "</td>" +
            "          <td style='word-wrap: break-word;'>" + r.mrn + "</td>" +
            "          <td style='word-wrap: break-word;'>" + showDateTime(r.checkInTime) + "</td>" +
            "          <td style='word-wrap: break-word;'>" + showDateTime(r.checkOutTime) + "</td>" +
            "          <td style='word-wrap: break-word;'>" + visitLengthLabel + "</td>" +
            "          <td style='word-wrap: break-word;'>" + showDateTime(r.scheduledStartTime) + "</td>" +
            "          <td style='word-wrap: break-word;'>" + showDateTime(r.scheduledEndTime) + "</td>" +
            "          <td style='word-wrap: break-word;'>" + visitLengthLabel1 + "</td>" +
            "          <td style='word-wrap: break-word;'>" + r.resourceName + "</td>" +
            "          <td style='word-wrap: break-word;'>" + showValue(r.bookedVisitComment) + "</td>" +
            "        </tr>";

        previousPiId = currentPiId;
        iteration++;
        tableRow++;
    }

    $('#report_results').html(out);
}


function report_showCensusResults() {
    var out = "";
    report_handleDataResponse(report_censusResult.length);
    var currentAsrId = -1;
    var previousAsrId = -1;
    var currentStudyId = -1;
    var previousStudyId = -1;
    var iteration = 0;
    var tableRow = 0;

    for (var i = 0; i < report_censusResult.length; i++) {
        var r = report_censusResult[i];
        currentAsrId = r.asrId;
        currentStudyId = r.studyId;
        var visitLengthLabel = formatEndMinusStart(showDateTime(r.scheduledStartTime), showDateTime(r.scheduledEndTime));
        var visitLengthLabel1 = endMinusStartInMinutes(showDateTime(r.scheduledStartTime), showDateTime(r.scheduledEndTime));
        var overMidnightDays = overnightsBetweenDates(showDateTime(r.scheduledStartTime), showDateTime(r.scheduledEndTime));
        var visitId = r.visitId;

        if (iteration != 0 && (currentAsrId != previousAsrId || currentStudyId != previousStudyId)) {
            out += " </table>" +
                "    <!-- table generator --></div>" +
                "    <!-- report row --></div>" +
                "  <hr class='report' />";
        }

        if (currentAsrId != previousAsrId || currentStudyId != previousStudyId) {
            tableRow = 0;
            out +=
                "    <div class='reportRow'>" +
                "      <div class='secondaryNameBlock'>" +
                "       <div class='mainName'>" + r.asrName + "</div>" +
                "      </div>" +
                "      <div class='statusBlockContainer'>" +
                "        <div class='statusBlockBlackText reportCheckedOutSecondary'>" +
                "          <div class='label'>Study Name</div>" +
                "          <div class='value' style='width: 235px;'>" + r.studyName + "</div>" +
                "          <div class='label'>PI Name</div>" +
                "          <div class='value' style='width: 235px;'>" + util_buildFullName(r.piFirstName, r.piMiddleName, r.piLastName) + "</div>" +
                "          <div class='label'>Primary Scheduling Contact</div>" +
                "          <div class='value' style='width: 235px;'>" + util_buildFullName(r.psFirstName, r.psMiddleName, r.psLastName) + "</div>" +
                "          <div class='label'>Catalyst ID</div>" +
                "          <div class='value' style='width: 235px;'>" + r.catalystId + "</div>" +
                "          <div class='label'>Local ID</div>" +
                "          <div class='value' style='width: 235px;'>" + r.localId + "</div>" +
                "          <div class='label'>IRB </div>" +
                "          <div class='value' style='width: 235px;'>" + r.irb + "</div>" +
                "          <div class='label'>Industry Initiated </div>" +
                "          <div class='value' style='width: 235px;'>" + yesNoValue(r.industryInitiated) + "</div>" +
                "        </div>" +
                "      </div>" +
                "      <div style='clear: both;'></div>" +
                "      <div class='CSSTableGenerator'><table width='100%' border='0' cellspacing='0' cellpadding='5'>" +
                "        <tr style='font-weight:bold'>" +
                "          <th>Check In Date</th>" +
                "          <th>Check Out Date</th>" +
                "          <th># Days Over Midnight</th>" +
                "          <th>Visit ID</th>" +
                "          <th>Visit Length</th>" +
                "          <th>Visit Length total Minutes</th>" +
                "          <th>Visit Name</th>" +
                "          <th>Sub-Location</th>" +
                "          <th>Visit Type</th>" +
                "          <th>Study Subject</th>" +
                "          <th>MRN</th>" +
                "        </tr>";
        }
        out += ((tableRow % 2 == 1) ? "<tr class='altRow'>" : "<tr>");
        out += "   <td>" + showDateTime(r.scheduledStartTime) + "</td>" +
            "          <td>" + showDateTime(r.scheduledEndTime) + "</td>" +
            "          <td>" + overMidnightDays + "</td>" +
            "          <td>" + visitId + "</td>" +
            "          <td>" + visitLengthLabel + "</td>" +
            "          <td>" + visitLengthLabel1 + "</td>" +
            "          <td>" + r.visitName + "</td>" +
            "          <td>" + r.sublocation + "</td>" +
            "          <td>" + r.visitType + "</td>" +
            "          <td>" + util_buildFullName(r.subjectFirstName, r.subjectMiddleName, r.subjectLastName) + "</td>" +
            "          <td>" + r.mrn + "</td>" +
            "        </tr>";

        previousAsrId = currentAsrId;
        previousStudyId = currentStudyId;
        iteration++;
        tableRow++;
    }

    $('#report_results').html(out);
}

function report_showLevelOfServiceResults() {
    report_handleDataResponse(report_levelOfServiceResult.length);
    var tableRow = 0;

    var out = "    <div class='reportRow'>" +
        "      <div class='CSSTableGenerator'><table width='100%' border='0' cellspacing='0' cellpadding='5' style='font-size: 90%;'>" +
        "        <tr style='font-weight:bold'>" +
        "          <th>Study Name</th>" +
        "          <th>Visit Name</th>" +
        "          <th>Visit Type</th>" +
        "          <th>Nursing</th>" +
        "          <th>Nutrition</th>" +
        "          <th>Processing</th>" +
        "          <th>Setup</th>" +
        "          <th>Appointment Status</th>" +
        "          <th>Check In Date</th>" +
        "          <th>Check Out Date</th>" +
        "          <th>Check Out Reason</th>" +
        "        </tr>";

    for (var i = 0; i < report_levelOfServiceResult.length; i++) {
        var r = report_levelOfServiceResult[i];

        out += ((tableRow % 2 == 1) ? "<tr class='altRow'>" : "<tr>");
        out += "  <td>" + r.studyName + "</td>" +
            "          <td>" + r.visitName + "</td>" +
            "          <td>" + r.visitType + "</td>" +
            "          <td>" + showValue(r.nursing) + "</td>" +
            "          <td>" + showValue(r.nutrition) + "</td>" +
            "          <td>" + showValue(r.processing) + "</td>" +
            "          <td>" + showValue(r.setup) + "</td>" +
            "          <td>" + r.appointmentStatus + "</td>" +
            "          <td>" + showDateTime(r.checkInDate) + "</td>" +
            "          <td>" + showDateTime(r.checkOutDate) + "</td>" +
            "          <td>" + r.checkOutReason.name + "</td>" +
            "        </tr>";
        tableRow++;
    }
    out += " </table>" +
        "    </div>" +
        "    </div>" +
        "  <hr class='report' />";
    $('#report_results').html(out);
}

function report_showSubjectPurgeResults() {
    report_handleDataResponse(report_subjectPurgeResult.length);
    var tableRow = 0;

    var out = "    <div class='reportRow'>" +
        "      <div class='CSSTableGenerator'><table width='100%' border='0' cellspacing='0' cellpadding='5' style='font-size: 90%;'>" +
        "        <tr style='font-weight:bold'>" +
        "          <th>Subject Name</th>" +
        "          <th>MRN</th>" +
        "          <th>User</th>" +
        "          <th>Subject Creation Date</th>" +
        "        </tr>";
    for (var i = 0; i < report_subjectPurgeResult.length; i++) {
        var r = report_subjectPurgeResult[i];

        out += ((tableRow % 2 == 1) ? "<tr class='altRow'>" : "<tr>");
        out += "  <td>" + util_buildFullName(r.firstName, r.middleName, r.lastName) + "</td>" +
            "          <td>" + r.mrn + "</td>" +
            "          <td>" + util_buildFullName(r.userFirstName, r.userMiddleName, r.userLastName) + "</td>" +
            "          <td>" + showDateTime(r.createTime) + "</td>" +
            "        </tr>";
        tableRow++;
    }
    out += " </table>" +
        "    </div>" +
        "    </div>" +
        "  <hr class='report' />";
    $('#report_results').html(out);
}

// !! better if name matches UI, eg WorkloadAndResources
function report_showResourceLevelOfServiceResults() {
    var out = "";
    report_handleDataResponse(report_resourceLevelOfServiceResult.length);
    var currentVisit = -1;
    var previousVisit = -1;
    var iteration = 0;
    var tableRow = 0;

    for (var i = 0; i < report_resourceLevelOfServiceResult.length; i++) {
        var r = report_resourceLevelOfServiceResult[i];
        currentVisit = r.visitId;

        if (iteration != 0 && currentVisit != previousVisit) {
            out += " </table>" +
                "    <!-- table generator --></div>" +
                "    <!-- report row --></div>" +
                "  <hr class='report' />";
        }

        var resourceLengthLabel = formatMinsIntoDaysHrsMins(r.duration);

        if (currentVisit != previousVisit) {
            tableRow = 0;
            out +=
                "    <div class='reportRow'>" +
                "      <div class='secondaryNameBlock'>" +
                "       <div class='mainName'>" + r.visitName + "</div>" +
                "      </div>" +
                "      <div class='statusBlockContainer'>" +
                "        <div class='statusBlock reportNoStatusSecondary'>" +
                "          <div class='label'>Visit Type</div>" +
                "          <div class='value'>" + r.visitType + "</div>" +
                "          <div class='label'>Local ID</div>" +
                "          <div class='value'>" + r.studyName + "</div>" +
                "          <div class='label'>Nursing</div>" +
                "          <div class='value'>" + showValue(r.nursing) + "</div>" +
                "          <div class='label'>Nutrition</div>" +
                "          <div class='value'>" + showValue(r.nutrition) + "</div>" +
                "          <div class='label'>Processing</div>" +
                "          <div class='value'>" + showValue(r.processing) + "</div>" +
                "          <div class='label'>Setup</div>" +
                "          <div class='value'>" + showValue(r.setup) + "</div>" +
                "        </div>" +
                "      </div>" +
                "      <div style='clear: both;'></div>" +
                "      <div class='CSSTableGenerator'><table width='100%' border='0' cellspacing='0' cellpadding='5' style='font-size: 90%;'>" +
                "        <tr style='font-weight:bold'>" +
                "          <th>Resource Name</th>" +
                "          <th>Resource Duration</th>" +
                "        </tr>";
        }

        out += ((tableRow % 2 == 1) ? "<tr class='altRow'>" : "<tr>");
        out += "  <td>" + r.resourceName + "</td>" +
            "          <td>" + resourceLengthLabel + "</td>" +
            "        </tr>";

        previousVisit = currentVisit;
        iteration++;
        tableRow++;
    }

    $('#report_results').html(out);
}

function report_showVisitTemplateResults() {
    var out = "";
    report_handleDataResponse(report_visitTemplateResult.length);
    var currentVisit = -1;
    var previousVisit = -1;
    var iteration = 0;
    var tableRow = 0;

    for (var i = 0; i < report_visitTemplateResult.length; i++) {
        var r = report_visitTemplateResult[i];
        currentVisit = r.id;

        if (iteration != 0 && currentVisit != previousVisit) {
            out += " </table>" +
                "    <!-- table generator --></div>" +
                "    <!-- report row --></div>" +
                "  <hr class='report' />";
        }


        if (currentVisit != previousVisit) {
            tableRow = 0;
            out +=
                "    <div class='reportRow'>" +
                "      <div class='secondaryNameBlock'>" +
                "       <div class='mainName'>" + r.studyId + "</div>" +
                "      </div>" +
                "      <div style='clear: both;'></div>" +
                "      <div class='CSSTableGenerator'><table width='100%' border='0' cellspacing='0' cellpadding='5' style='font-size: 90%; table-layout: fixed;'>" +
                "        <tr style='font-weight:bold'>" +
                "          <th class='stdReport_StudyName'>Study Name</th>" +
                "          <th class='stdReport_StudyStatus'>Study Status</th>" +
                "          <th class='stdReport_VisitName'>Visit Name</th>" +
                "          <th class='stdReport_VisitType'>Visit Type</th>" +
                "          <th class='stdReport_Sublocation'>Sublocation</th>" +
                "          <th class='stdReport_Billable'>Billable Template?</th>" +
                "          <th class='stdReport_ResearchPharmacy'>Research Pharmacy?</th>" +
                "          <th class='stdReport_InstitutionNonCRC'>Institution: Non CRC</th>" +
                "          <th class='stdReport_NonInstitutionNonCRC'>Non Institution: Non CRC</th>" +
                "          <th class='stdReport_RelativeTime'>Relative Time?</th>" +
                "          <th class='stdReport_Nursing'>Nursing</th>" +
                "          <th class='stdReport_Nutrition'>Nutrition</th>" +
                "          <th class='stdReport_Processing'>Processing</th>" +
                "          <th class='stdReport_Setup'>Setup</th>" +
                "          <th class='stdReport_Comment'>Comment</th>" +
                "        </tr>";
        }

        out += ((tableRow % 2 == 1) ? "<tr class='altRow'>" : "<tr>");
        out += "  <td>" + r.studyName + "</td>" +
            "  	   <td>" + r.studyStatus + "</td>" +
            "          <td>" + r.name + "</td>" +
            "          <td>" + r.visitType + "</td>" +
            "          <td>" + r.sublocation + "</td>" +
            "          <td>" + yesNoValue(r.billable) + "</td>" +
            "          <td>" + yesNoValue(r.researchPharmacy) + "</td>" +
            "          <td>" + yesNoValue(r.institutionNonCRC) + "</td>" +
            "          <td>" + yesNoValue(r.nonInstitutionNonCRC) + "</td>" +
            "          <td>" + yesNoValue(r.relativeTime) + "</td>" +
            "          <td>" + showValue(r.nursing) + "</td>" +
            "          <td>" + showValue(r.nutrition) + "</td>" +
            "          <td>" + showValue(r.processing) + "</td>" +
            "          <td>" + showValue(r.setup) + "</td>" +
            "          <td>" + showValue(r.comment) + "</td>" +
            "        </tr>";
        previousVisit = currentVisit;
        iteration++;
        tableRow++;
    }

    $('#report_results').html(out);
}

function report_showCRCAvailabilityResults() {
    report_handleDataResponse(report_crcAvailabilityResult.length);
    var tableRow = 0;

    var out = "    <div class='reportRow'>" +
        "      <div class='CSSTableGenerator'><table width='100%' border='0' cellspacing='0' cellpadding='5' style='font-size: 90%;'>" +
        "        <tr style='font-weight:bold'>" +
        "          <th>Study Name</th>" +
        "          <th>Visit Name</th>" +
        "          <th># visits scheduled</th>" +
        "          <th># visits overbooked</th>" +
        "        </tr>";
    for (var i = 0; i < report_crcAvailabilityResult.length; i++) {
        var r = report_crcAvailabilityResult[i];

        out += ((tableRow % 2 == 1) ? "<tr class='altRow'>" : "<tr>");
        out += "  <td>" + r.studyName + "</td>" +
            "          <td>" + r.visitName + "</td>" +
            "          <td>" + r.scheduled + "</td>" +
            "          <td>" + showValue(r.overbook) + "</td>" +
            "        </tr>";
        tableRow++;
    }
    out += " </table>" +
        "   </div> " +
        "    </div>" +
        "  <hr class='report' />";
    $('#report_results').html(out);
}

function report_showVisitDurationByVisitTypeResults() {
    report_handleDataResponse(report_visitDurationByVisitTypeResult.length);
    var tableRow = 0;

    var out = "    <div class='reportRow'>" +
        "      <div class='CSSTableGenerator'><table width='100%' border='0' cellspacing='0' cellpadding='5' style='font-size: 90%;'>" +
        "        <tr style='font-weight:bold'>" +
        "          <th>Local ID</th>" +
        "          <th>Booked Visit ID</th>" +
        "          <th>Scheduled Start Time</th>" +
        "          <th>Scheduled End Time</th>" +
        "          <th>Visit Type</th>" +
        "          <th>Visit Name</th>" +
        "          <th>Check In Date</th>" +
        "          <th>Check Out Date</th>" +
        "          <th>Visit Duration</th>" +
        "        </tr>";
    for (var i = 0; i < report_visitDurationByVisitTypeResult.length; i++) {
        var r = report_visitDurationByVisitTypeResult[i];
        var visitLengthLabel = formatEndMinusStart(showDateTime(r.checkInTime), showDateTime(r.checkOutTime));
        out += ((tableRow % 2 == 1) ? "<tr class='altRow'>" : "<tr>");
        out += "  <td>" + r.studyName + "</td>" +
            "          <td>" + r.visitId + "</td>" +
            "          <td>" + showDateTime(r.scheduledStartTime) + "</td>" +
            "          <td>" + showDateTime(r.scheduledEndTime) + "</td>" +
            "          <td>" + r.visitType + "</td>" +
            "          <td>" + r.visitName + "</td>" +
            "          <td>" + showDateTime(r.checkInTime) + "</td>" +
            "          <td>" + showDateTime(r.checkOutTime) + "</td>" +
            "          <td>" + showValue(visitLengthLabel) + "</td>" +
            "        </tr>";
        tableRow++;
    }
    out += " </table>" +
        "    </div>" +
        "    </div>" +
        "  <hr class='report' />";
    $('#report_results').html(out);
}

function report_showBookedVisitServiceLevelByTypeResults() {
    var out = "";
    report_handleDataResponse(
        report_bookedVisitServiceLevelByTypeResult.length);
    var currentVisitTypeId = -1;
    var previousVisitTypeId = -1;
    var iteration = 0;

    for (var i = 0; i < report_bookedVisitServiceLevelByTypeResult.length; i++) {
        var r = report_bookedVisitServiceLevelByTypeResult[i];
        currentVisitTypeId = r.visitId;

        if (iteration != 0 && currentVisitTypeId != previousVisitTypeId) {
            out += " </table>" +
                "    <!-- table generator --></div>" +
                "    <!-- report row --></div>" +
                "  <hr class='report' />";
        }

        if (currentVisitTypeId != previousVisitTypeId) {
            out +=
                "    <div class='reportRow'>" +
                "      <div class='secondaryNameBlock'>" +
                "       <div class='mainName'>" + r.visitType + "</div>" +
                "      </div>" +
                "      <div style='clear: both;'></div>" +
                "      <div class='CSSTableGenerator'><table width='100%' border='0' cellspacing='0' cellpadding='5' style='font-size: 90%;'>" +
                "        <tr style='font-weight:bold'>" +
                "          <th width='15%'>Nursing</th>" +
                "          <th width='11%'># of Visits</th>" +
                "          <th width='11%'>Nutrition</th>" +
                "          <th width='9%'># of Visits</th>" +
                "          <th width='9%'>Processing</th>" +
                "          <th width='9%'># of Visits</th>" +
                "          <th width='11%'>Set Up</th>" +
                "          <th width='11%'># of Visits</th>" +
                "        </tr>";

        }

        out += ((iteration % 2 == 1) ? "<tr class='altRow'>" : "<tr>");
        out += "<td> " + r.nursinglevel + "</td>" +
            "          <td>" + showValue(r.nursing) + "</td>" +
            "          <td> " + r.nutritionlevel + "</td>" +
            "          <td>" + showValue(r.nutrition) + "</td>" +
            "          <td> " + r.processinglevel + "</td>" +
            "          <td>" + showValue(r.processing) + "</td>" +
            "          <td> " + r.setuplevel + "</td>" +
            "          <td>" + showValue(r.setup) + "</td>" +
            "        </tr>";

        previousVisitTypeId = currentVisitTypeId;
        iteration++;
    }

    $('#report_results').html(out);
}

function report_showVisitsFlaggedResults() {
    var out = "";
    report_handleDataResponse(report_visitsFlaggedResult.length);
    var currentStudyId = -1;
    var previousStudyId = -1;
    var iteration = 0;
    var tableRow = 0;

    for (var i = 0; i < report_visitsFlaggedResult.length; i++) {
        var r = report_visitsFlaggedResult[i];
        currentStudyId = r.id;

        if (iteration != 0 && currentStudyId != previousStudyId) {
            out += " </table>" +
                "    <!-- table generator --></div>" +
                "    <!-- report row --></div>" +
                "  <hr class='report' />";
        }

        if (currentStudyId != previousStudyId) {
            tableRow = 0;
            out +=
                "    <div class='reportRow'>" +
                "      <div class='statusBlockContainer'>" +
                "        <div class='statusBlock reportNoStatusSecondary'>" +
                "          <div class='label'>Study Name</div>" +
                "          <div class='value'>" + r.studyName + "</div>" +
                "          <div class='label'>Local ID</div>" +
                "          <div class='value'>" + r.localId + "</div>" +
                "          <div class='label'>PI Name</div>" +
                "          <div class='value'>" + r.piName + "</div>" +
                "        </div>" +
                "      </div>" +
                "      <div style='clear: both;'></div>" +
                "      <div class='CSSTableGenerator'><table width='100%' border='0' cellspacing='0' cellpadding='5' style='font-size: 90%;'>" +
                "        <tr style='font-weight:bold'>" +
                "          <th width='7%'>Subject Name</th>" +
                "          <th width='7%'>DOB</th>" +
                "          <th width='7%'>MRN</th>" +
                "          <th width='7%'>Visit Name</th>" +
                "          <th width='7%'>Visit Type</th>" +
                "          <th width='7%'>Check In</th>" +
                "          <th width='7%'>Check Out</th>" +
                "          <th width='7%'>Resource Duration Variation?</th>" +
                "          <th width='7%'>Activity Variation?</th>" +
                "        </tr>";
        }
        out += ((tableRow % 2 == 1) ? "<tr class='altRow'>" : "<tr>");
        out += "   <td>" + util_buildFullName(r.subjectFirstName, r.subjectMiddleName, r.subjectLastName) + "</td>" +
            "          <td>" + r.birthdate + "</td>" +
            "          <td>" + r.mrn + "</td>" +
            "          <td>" + r.visitName + "</td>" +
            "          <td>" + r.visitType + "</td>" +
            "          <td>" + showDateTime(r.checkInTime) + "</td>" +
            "          <td>" + showDateTime(r.checkOutTime) + "</td>" +
            "          <td>" + yesNoValue(r.varyDuration) + "</td>" +
            "          <td>" + yesNoValue(r.ommittedActivities) + "</td>" +
            "        </tr>";

        previousStudyId = currentStudyId;
        iteration++;
        tableRow++;
    }

    $('#report_results').html(out);
}

function report_showAncillaryOnlyByProtocolResults() {
    var out = "";
    report_handleDataResponse(report_ancillaryOnlyByProtocolResult.length);
    var currentStudyId = -1;
    var previousStudyId = -1;
    var iteration = 0;

    for (var i = 0; i < report_ancillaryOnlyByProtocolResult.length; i++) {
        var count = report_ancillaryOnlyByProtocolResult.length - 1;
        var r = report_ancillaryOnlyByProtocolResult[i];
        currentStudyId = r.studyId;

        if (iteration != 0 && currentStudyId != previousStudyId) {
            out += " </table>" +
                "    <!-- table generator --></div>" +
                "    <!-- report row --></div>" +
                "  <hr class='report' />";
        }

        if (currentStudyId != previousStudyId) {
            out +=
                "    <div class='reportRow'>" +
                "      <div style='clear: both;'></div>" +
                "      <div class='CSSTableGenerator'><table width='100%' border='0' cellspacing='0' cellpadding='5' style='font-size: 90%; table-layout: fixed;'>" +
                "        <tr style='font-weight:bold'>" +
                "          <th>Protocol</th>" +
                "          <th></th>" +
                "          <th colspan='2' align='center'>Ancillary Only Visits</th>" +
                "        </tr>" +
                "        <tr style='font-weight:bold'>" +
                "          <th></th>" +
                "          <th>A</th>" +
                "          <th>B</th>" +
                "          <th>D</th>" +
                "        </tr>";
        }

        out += ((iteration % 2 == 1) ? "<tr class='altRow'>" : "<tr>");
        out += "<td style='word-wrap: break-word;'>" + r.studyName + "</td>" +
            "          <td>" + showValue(r.crcA) + "</td>" +
            "          <td>" + showValue(r.crcB) + "</td>" +
            "          <td>" + showValue(r.crcC) + "</td>" +
            "        </tr>";

        if (i == count) {
            out += ("<tr>");
            out += "<td style='word-wrap: break-word;'>" + 'Total' + "</td>" +
                "          <td>" + showValue(r.totalCRCA) + "</td>" +
                "          <td>" + showValue(r.totalCRCB) + "</td>" +
                "          <td>" + showValue(r.totalCRCC) + "</td>" +
                "        </tr>";
        }

        previousStudyId = currentStudyId;
        iteration++;
    }

    $('#report_results').html(out);
}
