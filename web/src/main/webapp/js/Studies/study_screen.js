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

var StudyScreen = {};

StudyScreen.staticListsDataCallback = function (data, callbackFunc) {
    var parsedData = JSON.parse(data);

    fundingSources = parsedData.fundingSources;
    centersAndInstitutions = parsedData.centersAndInstitutions;
    studyStatuses = parsedData.studyStatuses;

    // Find exactly one study status of type pending
    // If multiple pending statuses exist, the last one encountered
    // will be remembered. If that is a real use case then the code needs
    // to be modified to handle multiple PENDING statuses
    studyStatusIds = {};
    studyStatuses.forEach(function (status) {
        if (status.isPending) {
            studyStatusIds.pending = status.id;
        };
    });
    if (typeof studyStatusIds.pending === 'undefined') {
        DialogsUtil.showErrorMessage("This form cannot be initialized due to a server error. Please contact the system administrator.");
        throw "Found no study status of type PENDING. At least one is required.";
    }

    institutions = parsedData.institutions;
    irbInstitutions = parsedData.irbInstitutions;
    visitTypes = parsedData.visitTypes;
    sublocations = parsedData.sublocations;

    studyStatusSelectOptions = buildSelectOptions(studyStatuses, 'shortName');
    institutionSelectOptions = buildSelectOptions(institutions, 'longName');
    fundingSourcesSelectOptions = buildSelectOptions(fundingSources, 'name', "");
    centersAndInstitutionsSelectOptions = buildSelectOptions(centersAndInstitutions, 'name', "");
    irbInstitutionsSelectOptions = buildSelectOptions(irbInstitutions, 'name', "");
    visitTypeSelectOptions = buildSelectOptions(visitTypes, 'name');
    sublocationSelectOptions = buildSelectOptions(sublocations, 'name');

    yesNoSelectOptions = "";
    for (var i = 0; i < yesNo.length; i++) {
        yesNoSelectOptions += "<option value='" + yesNo[i] + "'>" + yesNo[i] + "</option>";
    }

    adultPediatricSelectOptions = "<option value=''></option>";
    for (var i = 0; i < adultPediatric.length; i++) {
        adultPediatricSelectOptions += "<option value='" + adultPediatric[i] + "'>" + adultPediatric[i] + "</option>";
    }

    crcCategorySelectOptions = "<option value=''></option>";
    for (var i = 0; i < crcCategories.length; i++) {
        crcCategorySelectOptions += "<option value='" + crcCategories[i] + "'>" + crcCategories[i] + "</option>";
    }

    $("#study_status").html(studyStatusSelectOptions);
    $("#study_institution").html(institutionSelectOptions);
    $("#study_irbInstitution").html(irbInstitutionsSelectOptions);
    $("#study_fundingSource1").html(fundingSourcesSelectOptions);
    $("#study_fundingSource2").html(fundingSourcesSelectOptions);
    $("#study_fundingSource3").html(fundingSourcesSelectOptions);
    $("#study_fundingSource4").html(fundingSourcesSelectOptions);
    $("#study_fundingInstitution1").html(centersAndInstitutionsSelectOptions);
    $("#study_fundingInstitution2").html(centersAndInstitutionsSelectOptions);
    $("#study_fundingInstitution3").html(centersAndInstitutionsSelectOptions);
    $("#study_fundingInstitution4").html(centersAndInstitutionsSelectOptions);
    $("#study_industryInitiated").html(yesNoSelectOptions);
    $("#study_pediatric").html(adultPediatricSelectOptions);
    $("#study_crcCategory").html(crcCategorySelectOptions);

    $("#study_visitType").html(visitTypeSelectOptions);
    $("#study_visitSublocation").html(sublocationSelectOptions);

    callbackFuncIfDefined(callbackFunc);
};

function getStudyStaticLists(callbackFunc) {

    // FIXME: this .js is getting loaded *everywhere*,
    //  and so the below loading is happening everywhere.
    // CW special-cased it below not to happen for visit_detail, but
    //  it really shouldn't happen in many other places!
    if (/\/visit_detail.html$/.test(getLocationHref())) {
        return;
    }

    if (/\/create_study.html$/.test(getLocationHref())) {
        user = JSON.parse(sessionStorage.getItem("userData"));
        if (user != null && user.institutionRole.id != 6) {
            getStudyMemberLists();
        }
    }

    $.get("rest/app/getStaticLists", function (data) {
        StudyScreen.staticListsDataCallback(data, callbackFunc);
    });
}

function commonStudyDetailData() {
    commonData();
    studyModuleRoles();
    renderBreadcrumbs('study_form_screen');

    var mode = JSON.parse(sessionStorage.getItem("mode"));
    var previousMode = JSON.parse(sessionStorage.getItem("previousMode"));
    if (previousMode == 'edit' || previousMode == 'new') {
        study_setFormMode('view');
    }
    else {
        study_setFormMode(mode);
    }
    loadStudyTab();
}

function commonVisitDetailData() {
    commonData();
    renderBreadcrumbs('study_visit_view_form');
    selectedTab = sessionStorage.getItem("selectedTab");
    if (selectedTab == null || selectedTab == 0) {
        var mode = JSON.parse(sessionStorage.getItem("mode"));
        var previousMode = JSON.parse(sessionStorage.getItem("previousMode"));
        if (previousMode == 'edit') {
            study_visit_setFormMode('view');
        }
        else {
            study_visit_setFormMode(mode);
        }
    }
    else if (selectedTab == 1) {
        loadVisitTemplateApprovalsTab();
    }
    else if (selectedTab == 2) {
        $.blockUI();
        loadTemplateResources();
    }
}

function commonVisitApprovalScreenData() {
    commonData();
    study_visit_approval();
}

function commonCreateStudyData() {
    commonData();
    var mode = JSON.parse(sessionStorage.getItem("mode"));
    study_setFormMode(mode);
}

StudyScreen.createStudyComboBoxes_onChange = function () {
    var id = $(this).attr("id");

    if (id == "study_fundingSource1") {
        displayFundingSourceDetails(this, 1);
    }
    else if (id == "study_fundingSource2") {
        displayFundingSourceDetails(this, 2);
    }
    else if (id == "study_fundingSource3") {
        displayFundingSourceDetails(this, 3);
    }
    else if (id == "study_fundingSource4") {
        displayFundingSourceDetails(this, 4);
    }
    else if (id == "study_status") {
        changeStudyStatusClick();
    }
};

function createStudyComboBoxes() {
    var comboBoxOptions = {
        onChange: StudyScreen.createStudyComboBoxes_onChange
    };

    WidgetUtil.createComboBox(".formSelectInput", comboBoxOptions);
}

function commonCreateVisitData() {
    commonData();
    var mode = JSON.parse(sessionStorage.getItem("mode"));
    study_visit_setFormMode(mode);
}

function addNewStudyClick() {
    sessionStorage.setItem("mode", JSON.stringify('new'));
    setLocationHref("create_study.html");
}

function addNewVisitClick() {
    sessionStorage.setItem("mode", JSON.stringify('new'));
    sessionStorage.setItem("previousMode", JSON.stringify('new'));
    setLocationHref("create_visit.html");
}

function newVisitApprovalClick() {
    showNewVisitApprovalForm();
}

function study_visit_approval() {
    renderBreadcrumbs('study_visit_approval_form_screen');
    $('.study_confirmFormItem').css({visibility: "visible"});
    $('.formTextReadOnly').css({display: "visible"});
    $('#study_visit_form_buttons').css({visibility: "visible"});
    $('.formTextAreaSmaller').css({display: "block"});
    $('#study_confirmFormItem').css({visibility: "visible"});

    WidgetUtil.commentBox($("#study_visitApprovalComment"));
    study_visit_approval_clearForm();
}

function study_visit_approval_clearForm() {

    app_selectedStudy = JSON.parse(sessionStorage.getItem("studyData"));
    app_selectedVisit = JSON.parse(sessionStorage.getItem("visitData"));
    user = JSON.parse(sessionStorage.getItem("userData"));

    $('#study_approval_study_name').html('(' + app_selectedStudy.localId + ')' + '<div style="padding: 3px;"></div>' + app_selectedStudy.name);
    $('#study_approval_visit_name').text(app_selectedVisit.visitName);
    $('#study_visitApprovalUser').text(user.ecommonsId);
    $('#study_visitApprovalCreated').text(showDateTime(app_selectedVisit.createdDate));
    $('#visit_finalApprovalStatus').text(app_selectedVisit.approved);
    WidgetUtil.commentBox.clearValue($("#study_visitApprovalComment"));
}

StudyScreen.showVisitDetailCallback = function (data) {
    sessionStorage.setItem("visitData", JSON.stringify(data));
    sessionStorage.setItem("selectedTab", 0);
    sessionStorage.setItem("mode", JSON.stringify('view'));
    setLocationHref("visit_detail.html");
};

function showVisitDetail(id) {
    $.getJSON("rest/study/getVisitTemplateData?visit=" + id, StudyScreen.showVisitDetailCallback);
}

function showNewVisitApprovalForm() {
    setLocationHref("visit_approval.html");
    study_visit_approval();
}

function newStudySubjectClick() {
    sessionStorage.setItem("selectedStudyIdForAddingMember", app_selectedStudy.id);
    subjectModule();
}

function newStudyMemberClick() {
    $.blockUI();
    setLocationHref("study_member.html");
}

function editStudyClick() {
    sessionStorage.setItem("mode", JSON.stringify('edit'));
    $.blockUI();
    setLocationHref("create_study.html");
}

function editVisitClick() {
    sessionStorage.setItem("mode", JSON.stringify('edit'));
    setLocationHref("create_visit.html");
}

function crudStudyClick() {
    $.blockUI();
    processStudyForm();
    $.unblockUI();
}

function restoreStudyClick() {
    // reload current study edit form
    editStudyClick();
}

function cancelStudyClick() {

    $('#study_form')[0].reset();
    study_clearErrors();
    mode = JSON.parse(sessionStorage.getItem("mode"));
    if (mode == 'edit') {
        showStudyDetail(app_selectedStudy.id);
    }
    else {
        studyModule();
    }
}

function studyShowSubjectsTab() {
    sessionStorage.setItem("selectedStudyTab", 2);
    studyReadData('StudySubjects');
}

function studyReadData(tabName) {
    sessionStorage.setItem("mode", JSON.stringify('view'));
    if (tabName) {
        setLocationHref("study_detail.html#" + tabName);
    } else {
        sessionStorage.setItem("selectedStudyTab", 0);
        setLocationHref("study_detail.html#StudyDetail");
    }
}

function loadStudyTab() {
    setLocationHref("study_detail.html#StudyDetail");
}


//this is used, by dynamically generated code; so do not remove
function goToStudySubjectForm(subjectId, subjectMrn, subjectInstitution) {
    sessionStorage.removeItem("selectedSubjectByPuid");

    sessionStorage.setItem("selectedSubjectId", subjectId);
    sessionStorage.setItem("selectedSubjectMrn", subjectMrn);
    sessionStorage.setItem("selectedSubjectInstitution", subjectInstitution);

    setLocationHref("subject_details.html?mode=view");
}

//This specifies a name for this piece of code which will appear when debugging
//This is needed because this file is loaded programatically
//# sourceURL=study_screen.js
