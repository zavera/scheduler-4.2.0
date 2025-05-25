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


function handleCrcFunding() {
    var crcFunded = app_selectedStudy.crcFunded == true;
    if (document.study_form == undefined) {
        $("#study_CRCFundedReadOnly").checked = crcFunded;
    }
    else {
        document.study_form.study_CRCFundedReadOnly.checked = crcFunded;
    }
}

function initializeDateUI(date, labels) {
    // if only one label is passed, as opposed to an array, then make it an array of one value so that the code below
    // works for both cases -- where the input is a single value, or an array
    if (date != null) {
        if (!Array.isArray(labels)) {
            labels = [labels];
        }
        var formattedDate;
        if (majorVersion == "8") {
            var dateWithoutDashes = date.replace(/-/g, '/');
            var dateWithoutTime = dateWithoutDashes.replace(/T.*/, '');
            var sanitizedDate = new Date(dateWithoutTime);
            formattedDate = (sanitizedDate.getMonth() + 1) + "/" + sanitizedDate.getDate() + "/" + sanitizedDate.getFullYear();
        }
        else {
            formattedDate = dateFormat(date, 'mm/dd/yyyy')
        }
        var numberOfLabels = labels.length;
        for (var i = 0; i < numberOfLabels; ++i) {
            $('#' + labels[i]).text(formattedDate);
        }
    }
}

function loadStudyData() {
    getBrowserVersion();
    $('#study_form_title').text('Additional Details');
    $('#study_localId').html('(' + app_selectedStudy.localId + ')');
    $('#study_name').html(app_selectedStudy.name);

    var statusText = app_selectedStudy.studyStatusName;
    $('#study_statusName').text(statusText);

    //zero out the class from last time:
    var statusBlock = $('#study_statusName').parent();

    statusBlock.attr('class', '');

    //now set the class name to the status for background coloring:
    statusBlock.addClass(statusText.toLowerCase());
    statusBlock.addClass('statusBlock');

    $('#studyDetails').css({display: "block"});
    $('#editStudyLink').css({visibility: "visible"});
    $('#studyVisitLink').css({visibility: "visible"});
    $('#studySubjectLink').css({visibility: "visible"});
    $('.requiredFieldKey').css({display: 'none'});
    $('#study_form_buttons').css({visibility: "hidden"});
    $('.requiredIcon').css({visibility: "hidden"});
    $('.formTextReadOnly').css({display: "inline"});
    $('.formTextInput').css({display: "none"});
    $('.formSelectInput').css({display: "none"});
    $('.formTextAreaSmaller').css({display: "none"});
    $('#study_contacts').css({display: "none"});
    $('.study_confirmFormItem').css({visibility: "hidden"});
    $('#study_visit_main_screen').css({display: "block"});
    $('#study_subject_main_screen').css({display: "block"});
    $('#study_member_main_screen').css({display: "block"});
    $('#study_studyNameReadOnly').text(app_selectedStudy.name);
    $('#study_institutionReadOnly').text(app_selectedStudy.institutionName);
    $('#study_statusReadOnly').text(app_selectedStudy.studyStatusName);
    $('#study_industryInitiatedReadOnly').text(app_selectedStudy.industryInitiated == true ? 'Yes' : 'No');
    $('#study_localIdReadOnly').text(app_selectedStudy.localId);
    $('#study_catalystIdReadOnly').text(app_selectedStudy.catalystId);
    $('#study_irbReadOnly').text(app_selectedStudy.irb);
    $('#study_shortNameReadOnly').text(app_selectedStudy.shortTitle);
    $('#study_totalSubjReadOnly').text(app_selectedStudy.totalSubjects);
    $('#study_totalOutpatientsReadOnly').text(app_selectedStudy.totalOutpatientVisits);
    $('#study_totalInpatientsReadOnly').text(app_selectedStudy.totalInpatientVisits);
    $('#study_totalOutpatient').text(app_selectedStudy.totalOutpatientVisits);
    $('#study_totalInpatient').text(app_selectedStudy.totalInpatientVisits);
    $('#study_totalSubject').text(app_selectedStudy.totalSubjects);

    handleCrcFunding();

    $('#study_fundingSource1ReadOnly').val('');
    $('#study_fundingSource2ReadOnly').val('');
    $('#study_fundingSource3ReadOnly').val('');
    $('#study_fundingSource4ReadOnly').val('');

    for (var i = 0; i < app_selectedStudy.fundingSourceInfoList.length; i++) {
        var fundingSource = app_selectedStudy.fundingSourceInfoList[i];
        var fundingSourceLabel = 'study_fundingSource' + fundingSource.oneToFour + 'ReadOnly';
        var fundingCommentLabel = 'study_fundingComment' + fundingSource.oneToFour + 'ReadOnly';
        var fundingInstitutionLabel = 'study_fundingInstitution' + fundingSource.oneToFour + 'ReadOnly';
        var fundingGrantLabel = 'study_grantId' + fundingSource.oneToFour + 'ReadOnly';
        var fundingSiteCostCenterLabel = 'study_siteCostCenter' + fundingSource.oneToFour + 'ReadOnly';
        var fundingTotalDirectAwardLabel = 'study_totalDirectDollarAward' + fundingSource.oneToFour + 'ReadOnly';
        var fundingTotalIndirectAwardLabel = 'study_totalIndirectDollarAward' + fundingSource.oneToFour + 'ReadOnly';
        var fundingProjectStartDateLabel = 'study_projectStartDate' + fundingSource.oneToFour + 'ReadOnly';
        var fundingProjectEndDateLabel = 'study_projectEndDate' + fundingSource.oneToFour + 'ReadOnly';
        $('#' + fundingSourceLabel).text(fundingSource.name);
        $('#' + fundingInstitutionLabel).text(fundingSource.centerAndInstitutionName);
        $('#' + fundingGrantLabel).text(fundingSource.grant);
        $('#' + fundingSiteCostCenterLabel).text(fundingSource.center);

        $('#' + fundingTotalDirectAwardLabel).text(fundingSource.directAward);
        $('#' + fundingTotalIndirectAwardLabel).text(fundingSource.indirectAward);

        initializeDateUI(fundingSource.start, fundingProjectStartDateLabel);
        initializeDateUI(fundingSource.end, fundingProjectEndDateLabel);

        $('#' + fundingCommentLabel).text(fundingSource.comment);
        toggleFundingSourcesDetail(fundingSourceLabel, fundingSource.id, 'view');
    }

    $('#study_pediatricReadOnly').text(app_selectedStudy.pediatric);
    $('#study_crcCategoryReadOnly').text(app_selectedStudy.crcCategory);
    $('#study_clinicalTrialReadOnly').text(app_selectedStudy.clinicalTrial);
    $('#study_newDrugReadOnly').text(app_selectedStudy.newDrug);
    $('#study_deviceExemptionReadOnly').text(app_selectedStudy.deviceExemption);
    $('#study_irbInstitutionReadOnly').text(app_selectedStudy.irbInstitutionName);

    if (app_selectedStudy.scheduler.firstName == '') {
        $('#study_contacts_details').css({display: "none"});
    }
    else {
        $('#study_contacts_details').css({display: "block"});
        $('#study_primarySchedulingNameReadOnly').text(app_selectedStudy.scheduler.firstName + ' ' + app_selectedStudy.scheduler.lastName);
        $('#study_primarySchedulingEmailReadOnly').text(app_selectedStudy.scheduler.email);
        $('#study_primarySchedulingInstitutionReadOnly').text(app_selectedStudy.scheduler.institution);
        $('#study_primarySchedulingContactReadOnly').text(app_selectedStudy.scheduler.primaryPhone);

        $('#study_secondarySchedulingNameReadOnly').text(app_selectedStudy.secondaryScheduler.firstName + ' ' + app_selectedStudy.secondaryScheduler.lastName);
        $('#study_secondarySchedulingEmailReadOnly').text(app_selectedStudy.secondaryScheduler.email);
        $('#study_secondarySchedulingInstitutionReadOnly').text(app_selectedStudy.secondaryScheduler.institution);
        $('#study_secondarySchedulingContactReadOnly').text(app_selectedStudy.secondaryScheduler.primaryPhone);

        $('#study_thirdSchedulingNameReadOnly').text(app_selectedStudy.scheduler3.firstName + ' ' + app_selectedStudy.scheduler3.lastName);
        $('#study_thirdSchedulingEmailReadOnly').text(app_selectedStudy.scheduler3.email);
        $('#study_thirdSchedulingInstitutionReadOnly').text(app_selectedStudy.scheduler3.institution);
        $('#study_thirdSchedulingContactReadOnly').text(app_selectedStudy.scheduler3.primaryPhone);


        if (app_selectedStudy.protocolNurseString == "N/A") {
            $('#study_protocolNurseNameReadOnly').text(app_selectedStudy.protocolNurseString);
        }
        else {
            $('#study_protocolNurseNameReadOnly').text(app_selectedStudy.protocolNurse.firstName + ' ' + app_selectedStudy.protocolNurse.lastName);
            $('#study_protocolNurseEmailReadOnly').text(app_selectedStudy.protocolNurse.email);
            $('#study_protocolNurseInstitutionReadOnly').text(app_selectedStudy.protocolNurse.institution);
            $('#study_protocolNurseContactReadOnly').text(app_selectedStudy.protocolNurse.primaryPhone);
        }

        $('#study_associateProtocolNurseNameReadOnly').text(app_selectedStudy.associateNurse.firstName + ' ' + app_selectedStudy.associateNurse.lastName);
        $('#study_associateProtocolNurseEmailReadOnly').text(app_selectedStudy.associateNurse.email);
        $('#study_associateProtocolNurseInstitutionReadOnly').text(app_selectedStudy.associateNurse.institution);
        $('#study_associateProtocolNurseContactReadOnly').text(app_selectedStudy.associateNurse.primaryPhone);

        if (app_selectedStudy.protocolNutritionistString == "N/A") {
            $('#study_protocolNutritionistNameReadOnly').text(app_selectedStudy.protocolNutritionistString);
        }
        else {
            $('#study_protocolNutritionistNameReadOnly').text(app_selectedStudy.protocolNutritionist.firstName + ' ' + app_selectedStudy.protocolNutritionist.lastName);
            $('#study_protocolNutritionistEmailReadOnly').text(app_selectedStudy.protocolNutritionist.email);
            $('#study_protocolNutritionistInstitutionReadOnly').text(app_selectedStudy.protocolNutritionist.institution);
            $('#study_protocolNutritionistContactReadOnly').text(app_selectedStudy.protocolNutritionist.primaryPhone);
        }

        $('#study_associateProtocolNutritionistNameReadOnly').text(app_selectedStudy.associateProtocolNutritionist.firstName + ' ' + app_selectedStudy.associateProtocolNutritionist.lastName);
        $('#study_associateProtocolNutritionistEmailReadOnly').text(app_selectedStudy.associateProtocolNutritionist.email);
        $('#study_associateProtocolNutritionistInstitutionReadOnly').text(app_selectedStudy.associateProtocolNutritionist.institution);
        $('#study_associateProtocolNutritionistContactReadOnly').text(app_selectedStudy.associateProtocolNutritionist.primaryPhone);
        $('#study_investigatorCredentialReadOnly').text(app_selectedStudy.investigator.credential);
        $('#study_investigatorDivisionReadOnly').text(app_selectedStudy.investigator.division);
        $('#study_investigatorDepartmentReadOnly').text(app_selectedStudy.investigator.department);
        $('#study_investigatorNameReadOnly').text(app_selectedStudy.investigator.firstName + ' ' + app_selectedStudy.investigator.lastName);
        $('#study_investigatorEmailReadOnly').text(app_selectedStudy.investigator.email);
        $('#study_investigatorContactReadOnly').text(app_selectedStudy.investigator.primaryPhone);
        $('#study_investigatorContact2ReadOnly').text(app_selectedStudy.investigator.secondaryPhone);
        $('#study_investigatorPagerReadOnly').text(app_selectedStudy.investigator.pager);

        $('#study_physician1CredentialReadOnly').text(app_selectedStudy.physician.credential);
        $('#study_physician1DivisionReadOnly').text(app_selectedStudy.physician.division);
        $('#study_physician1DepartmentReadOnly').text(app_selectedStudy.physician.department);
        $('#study_physician1NameReadOnly').text(app_selectedStudy.physician.firstName + ' ' + app_selectedStudy.physician.lastName);
        $('#study_physician1EmailReadOnly').text(app_selectedStudy.physician.email);
        $('#study_physician1ContactReadOnly').text(app_selectedStudy.physician.primaryPhone);
        $('#study_physician1Contact2ReadOnly').text(app_selectedStudy.physician.secondaryPhone);
        $('#study_physician1PagerReadOnly').text(app_selectedStudy.physician.pager);

        $('#study_physician2CredentialReadOnly').text(app_selectedStudy.physician2.credential);
        $('#study_physician2DivisionReadOnly').text(app_selectedStudy.physician2.division);
        $('#study_physician2DepartmentReadOnly').text(app_selectedStudy.physician2.department);
        $('#study_physician2NameReadOnly').text(app_selectedStudy.physician2.firstName + ' ' + app_selectedStudy.physician2.lastName);

        $('#study_physician2EmailReadOnly').text(app_selectedStudy.physician2.email);
        $('#study_physician2ContactReadOnly').text(app_selectedStudy.physician2.primaryPhone);
        $('#study_physician2Contact2ReadOnly').text(app_selectedStudy.physician2.secondaryPhone);
        $('#study_physician2PagerReadOnly').text(app_selectedStudy.physician2.pager);
    }

    $('#study_abstractReadOnly').text('');

    $('#study_abstractReadOnly').text(app_selectedStudy.studyAbstract);

    initializeDateUI(app_selectedStudy.irbExpiration, 'study_irbExpirationDateReadOnly');
    initializeDateUI(app_selectedStudy.irbRenewalDate, 'study_irbRenewalDateReadOnly');
    initializeDateUI(app_selectedStudy.irbApprovalDate, 'study_irbApprovalDateReadOnly');
    initializeDateUI(app_selectedStudy.expectedEndDate, ['study_expectedEndDateReadOnly', 'study_expectedEndDateStatusBlock']);
    initializeDateUI(app_selectedStudy.expectedStartDate, ['study_expectedStartDateReadOnly', 'study_expectedStartDateStatusBlock']);

    $('#study_earliestStartDateValReadOnly').text(showDateTime(app_selectedStudy.firstVisitDate));
    $('#study_latestStartDateValReadOnly').text(showDateTime(app_selectedStudy.lastScheduledVisitDate));
}
