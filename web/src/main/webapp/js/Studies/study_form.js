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
var study_mode;
var study_visit_mode;
var study_subject_mode;
var studySub = [];
var studyMem = [];
var nursingVal = null;
var nutritionVal = null;
var processingVal = null;
var setUpVal = null;
var mealPlanCalculationVal = null;
var nutrientAnalysisVal = null;
var educationTimeVal = null;
var questionnaireTimeVal = null;
var downloadVal = null;
var scalpVal = null;
var phlebotomyVal = null;
var asstMedProcessingVal = null;
var asstMedSpecialProcessingVal = null;
var tempCollectionVal = null;
var monitorVal = null;
var bloodsVal = null;
var vitalSignsVal = null;
var continuousECGMonitorVal = null;
var vitaportStartVal = null;
var ecgVal = null;
var app_visitCommentGrid;

function crudVisitClick() {
    sessionStorage.setItem("previousMode", JSON.stringify('new'));
    processNewVisitForm();
}

function restoreVisitClick() {
    $('#visit_form')[0].reset();
    study_visit_clearErrors();
    study_visit_clearForm();
    study_visit_loadEditForm();
}

function cancelVisitClick() {
    $('#visit_form')[0].reset();
    study_visit_clearErrors();
    mode = JSON.parse(sessionStorage.getItem("mode"));
    if (mode == 'edit') {
        visitReadData();
    }
    else {
        sessionStorage.setItem("previousMode", JSON.stringify('edit'));
        window.location.href = "study_detail.html#VisitTemplates";
    }
}

function visitReadData() {
    sessionStorage.setItem("mode", JSON.stringify('view'));
    window.location.href = "visit_detail.html";
}

function cancelVisitApprovalClick() {
    $('#visit_approval')[0].reset();
    app_selectedVisit = JSON.parse(sessionStorage.getItem("visitData"));
    window.location.href = "visit_detail.html";
}

function submitVisitApprovalClick() {
    processVisitCommentApprovalForm();
}

function submitVisitCommentClick() {
    processVisitCommentForm();
}

function processVisitCommentApprovalForm() {
    $('#study_responseLoading').css({visibility: "visible"});
    jsonData = JSON.stringify({
        visitTemplate: (app_selectedVisit != null ? app_selectedVisit.id : 0),
        approved: true,
        comment: $.trim(WidgetUtil.commentBox.getValue($('#study_visitApprovalComment'))),
        user: user
    });

    $.post("rest/study/approveVisitTemplate", {data: jsonData}, function (data) {
        var confirmationMessage = "Visit Approved";
        $('#study_responseLoading').css({visibility: "hidden"});
        $('#visit_approval')[0].reset();
        $('#study_visitApprovalCommentValidation').css({display: "none"});
        //util_showMainMessage(confirmationMessage);
        DialogsUtil.showMessageDialog(confirmationMessage, function()
        {
            showVisitDetail(app_selectedVisit.id);
        });
    });
}

function processVisitCommentForm() {
    $('#study_responseLoading').css({visibility: "visible"});
    jsonData = JSON.stringify({
        visitTemplate: (app_selectedVisit != null ? app_selectedVisit.id : 0),
        comment: $.trim(WidgetUtil.commentBox.getValue($('#study_visitApprovalComment'))),
        approved: false,
        user: user
    });

    $.post("rest/study/commentVisitTemplate", {data: jsonData}, function (data) {
        var confirmationMessage = "Comment Added";
        $('#study_responseLoading').css({visibility: "hidden"});
        $('#visit_approval')[0].reset();
        $('#study_visitApprovalCommentValidation').css({display: "none"});
        alert(confirmationMessage);
        util_showMainMessage(confirmationMessage);
        showVisitDetail(app_selectedVisit.id);
    });
}

function processNewVisitForm() {
    app_selectedStudy = JSON.parse(sessionStorage.getItem("studyData"));
    var isValid = true;
    study_visit_clearErrors();

    if ($.trim($("#study_visitName").val()).length < 1) {
        showError('#study_visitNameValidation');
        isValid = false;
    }

    if ($.trim($("#study_visitName").val()).length > 255) {
        showError('#study_visitNameValidation', 'Must be 255 characters or fewer');
        isValid = false;
    }

    if (study_visit_mode === 'copy') {
        var visitVal = $.trim($('#study_visitName').val());
        if (visitVal === app_selectedVisit.visitName) {
            showError('#study_visitNameValidation', 'Identical Visit Name..Please enter unique visit name.');
            isValid = false;
        }
    }

    if ($.trim($("#study_visitSublocation").combobox("getValue")).length < 1) {
        showError('#study_visitSublocationValidation');
        isValid = false;
    }
    if ($.trim($("#study_visitType").combobox("getValue")).length < 1) {
        showError('#study_visitTypeValidation');
        isValid = false;
    }

    if ($('#study_relativeTime').is(':checked')) {
        $("#study_relativeTime").val(true);
    }
    else {
        $("#study_relativeTime").val(false);
    }

    if ($("#study_billableTemplate").is(":checked")) {
        $("#study_billableTemplate").val(true);
    }
    else {
        $("#study_billableTemplate").val(false);
    }

    if ($('#study_researchPharmacy').is(':checked')) {
        $("#study_researchPharmacy").val(true);
    }
    else {
        $("#study_researchPharmacy").val(false);
    }
    if ($('#study_institution_noncrc').is(':checked')) {
        $("#study_institution_noncrc").val(true);
    }
    else {
        $("#study_institution_noncrc").val(false);
    }
    if ($('#study_nonInstitution_nonCrc').is(':checked')) {
        $("#study_nonInstitution_nonCrc").val(true);
    }
    else {
        $("#study_nonInstitution_nonCrc").val(false);
    }

    var sublocationVal = $.trim($('#study_visitSublocation').combobox("getValue"));
    var visitTypeVal = $.trim($('#study_visitType').combobox("getValue"));

    nursingVal = $.trim($('#study_nursing').val());
    if (nursingVal != null && (nursingVal < 0 || nursingVal > 7)) {
        util_showMainMessage("Nursing Value is outside the range");
        return;
    }
    nutritionVal = $.trim($('#study_nutrition').val());
    if (nutritionVal != null && (nutritionVal < 0 || nutritionVal > 7)) {
        util_showMainMessage("Nutrition Value is outside the range");
        return;
    }
    processingVal = $.trim($('#study_processing').val());
    if (processingVal != null && (processingVal < 0 || processingVal > 7)) {
        util_showMainMessage("Processing Value is outside the range");
        return;
    }
    setUpVal = $.trim($('#study_setup').val());
    if (setUpVal != null && (setUpVal < 0 || setUpVal > 7)) {
        util_showMainMessage("Setup Value is outside the range");
        return;
    }

    if (isValid == false) {
        return;
    }

    $('#study_responseLoading').css({visibility: "visible"});
    var jsonData = JSON.stringify({
        id: (app_selectedVisit != null ? app_selectedVisit.id : 0),
        name: $.trim($('#study_visitName').val()),
        shortName: $.trim($('#study_shortVisitName').val()),
        sublocation: (sublocationVal != "" ? sublocationVal : 0),
        visitType: (visitTypeVal != "" ? visitTypeVal : 0),
        relativeTime: $.trim($('#study_relativeTime').val()),
        billable: $.trim($("#study_billableTemplate").val()),
        researchPharmacy: $.trim($('#study_researchPharmacy').val()),
        institutionNonCRC: $.trim($('#study_institution_noncrc').val()),
        nonInstitutionNonCRC: $.trim($('#study_nonInstitution_nonCrc').val()),
        comment: $.trim($('#study_visitComment_txtArea').val()),
        nursing: nursingVal,
        nutrition: nutritionVal,
        processing: processingVal,
        setup: setUpVal,
        studyId: (app_selectedStudy != null ? app_selectedStudy.id : 0)
    });

    var url = "rest/study/createVisit";

    if (study_visit_mode == 'edit') {
        url = "rest/study/editVisit";
    }
    else if (study_visit_mode == 'copy') {
        $.blockUI();
        url = "rest/study/copyVisit";
    }

    $.post(url, {data: jsonData}, function (data) {
        var parsedData = JSON.parse(data);
        $.unblockUI();
        var confirmationMessage = "New Visit Template created";
        if (study_visit_mode == 'edit') {
            confirmationMessage = "Visit Template updated";
        }
        $('#study_responseLoading').css({visibility: "hidden"});
        DialogsUtil.showMessageDialog(confirmationMessage, function()
        {
            showVisitDetail(parsedData.id);
        });
    });
}

function study_visit_clearErrors() {
    $('#study_visitNameValidation').css({visibility: "hidden"});
    $('#study_visitSublocationValidation').css({visibility: "hidden"});
    $('#study_visitTypeValidation').css({visibility: "hidden"});
}

