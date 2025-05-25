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
var orderBy;
var sortBy;
var sortByVisitName = "v.name";
var sortByVisitType = "v.visitType.name";
var sortBySublocation = "v.sublocation.name";
var sortByApproved = "v.approved";
var totalPages;
var activeView;
var currentPage;
var selected = new Array();
var activate;


var studyPage = (function () {
    var initFn = function () {

        commonInit();

        commonData();

        resetUI();
        currentPage = 1;

        renderBreadcrumbs('study_screen');

        loadStudyScreenData();
        studyModuleRoles();
    };

    var initCreateStudy = function () {

        commonInit(commonCreateStudyData);
    };

    var initDetailsPage = function () {
        commonInit(commonStudyDetailData);

        // tabs analogue of ready()
        $("#VisitTemplatesTabs").tabs({
            activate: function( event, ui ) {
                var panelId = ui.newPanel.attr('id');

                if (panelId == 'active_visits') {
                    getActiveVisitTemplates();
                }
                else {
                    getInactiveVisitTemplates()
                }
            },
            load: function( event, ui ) {
                getActiveVisitTemplates();
            }
        });

        // active tab is default, since it's first in html

        var selectedTab = sessionStorage.getItem("selectedStudyTab");
        sessionStorage.removeItem("selectedStudyTab");
        if (selectedTab == 2) {
            loadStudySubjectsTab();
        }

        $("#StudyDetailsTabs").tabs({
            active: selectedTab,
            activate: function( event, ui ) {
                var panelId = ui.newPanel.attr('id');
                if (panelId === 'studyVisitsTab2') {
                    getActiveVisitTemplates();
                }
                else if(panelId === 'studySubjectsTab3') {
                    loadStudySubjectsTab();
                }
                else if (panelId === 'studyMembersTab4') {
                    loadStudyMembersTab();
                }
            }
        });
    };

    function commonInit(callbackFunc) {
        loadMetaHeaders();
        getStudyStaticLists(function () {
            createStudyComboBoxes();
            callbackFuncIfDefined(callbackFunc);
        });

        initFooter();
        eraseLicense();

        app_runIdleTimer();
    }

    var initCreateVisitPage = function () {

        commonInit(commonCreateVisitData);

        $("#study_nursing").spinner({
            min: 0,
            max: 7
        });
        $("#study_nutrition").spinner({
            min: 0,
            max: 7
        });
        $("#study_processing").spinner({
            min: 0,
            max: 7
        });
        $("#study_setup").spinner({
            min: 0,
            max: 7
        });
    };

    var initMemberPage = function () {
        appt_selectedStudy = JSON.parse(sessionStorage.getItem("studyData"));
        commonInit();
        commonData();
        renderBreadcrumbs('study_member_form_screen');
        $('.mainBody').css('visibility', 'visible');
        $.unblockUI();
        $("#study_id").html('('+appt_selectedStudy.localId+')');
        $("#study_name").html(appt_selectedStudy.name);
    };

    var initVisitDetailPage = function () {
        getBrowserVersion();
        loadMetaHeaders();
        initFooter();
        eraseLicense();
        app_runIdleTimer();
        //don't add getStudyStaticLists();
        //if you read the method, it doesn't run on this page

        commonVisitDetailData();

        selectedTab = sessionStorage.getItem("selectedTab");
        if (selectedTab == null) {
            selectedTab = 0;
        }
        $("#VisitDetailsTabs").tabs({active: selectedTab});

        // Prevent the backspace key from navigating back.
        $(document).unbind('keydown').bind('keydown', function (event) {
            var doPrevent = false;
            if (event.keyCode === 8) {
                var d = event.srcElement || event.target;
                if ((d.tagName.toUpperCase() === 'INPUT' &&
                        (
                        d.type.toUpperCase() === 'TEXT' ||
                        d.type.toUpperCase() === 'PASSWORD' ||
                        d.type.toUpperCase() === 'FILE' ||
                        d.type.toUpperCase() === 'SEARCH' ||
                        d.type.toUpperCase() === 'EMAIL' ||
                        d.type.toUpperCase() === 'NUMBER' ||
                        d.type.toUpperCase() === 'DATE' )
                    ) ||
                    d.tagName.toUpperCase() === 'TEXTAREA') {
                    doPrevent = d.readOnly || d.disabled;
                }
                else {
                    doPrevent = true;
                }
            }

            if (doPrevent) {
                event.preventDefault();
            }
        });
        var txt = 'Resource';
        $('.ganttChartThemed table tr th').filter(function () {
            $(this).text() === txt ? $(this).css('background-color', '#0A507D') : "";
        });
        $.unblockUI();
    };


    return {
        init: initFn,
        initCreateStudyPage: initCreateStudy,
        initDetailsPage: initDetailsPage,
        initMemberPage: initMemberPage,
        initVisitDetailPage: initVisitDetailPage,
        initCreateVisitPage: initCreateVisitPage
    };


}());

function onloadStudyScreen()
{
    PageLoadUtil.loadPage(".studieslink", studyPage.init, false);
}

function study_createDateWidgets() {
    WidgetUtil.createDatepicker("#study_irbExpirationDate");
    WidgetUtil.createDatepicker("#study_irbRenewalDate");
    WidgetUtil.createDatepicker("#study_irbApprovalDate", {
        onSelect: function (selectedDate) {
            showError('#study_irbExpirationDateValidation', 'This is a required field.');
        }
    });
    WidgetUtil.createDatepicker("#study_expectedEndDate");
    WidgetUtil.createDatepicker("#study_expectedStartDate");
    for (var i = 1; i < 5; i++) {
        WidgetUtil.createDatepicker("#study_projectStartDate" + i);
        WidgetUtil.createDatepicker("#study_projectEndDate" + i);
    }
}

function activateVisitsClick() {
    activeView = false;
    activate = true;
    changeVisitStatus();
}

function deactivateVisitsClick() {
    activate = false;
    activeView = true;
    changeVisitStatus();
}

function visitStatusDialog() {
    DialogsUtil.showConfirmationDialog("#visit-status-dialog-confirm", {
        resizable: true,
        width: 600,
        buttons: {
            "Yes": function () {
                var selected = VisitTables.currentTable.getSelectedRows();

                jsonData = JSON.stringify({visitTemplatesId: selected});
                $.post("rest/study/changeVisitTemplateStatus?activate=" + activate, {data: jsonData}, function (data) {
                    var confirmationMessage = "";
                    if (activate == true) {
                        confirmationMessage = "Visit(s) Activated.";
                    }
                    else if (activate == false) {
                        confirmationMessage = "Visit(s) Deactivated.";
                    }
                    util_showMainMessage(confirmationMessage);
                    currentPage = 1;
                    loadVisitTemplatesDataIntoTablesWidget(activeView);
                });
                $(this).dialog("close");
            },
            "No": function () {
                $(this).dialog("close");
            }
        }
    });
}

function changeVisitStatus() {
    visitStatusDialog();
}

function getInactiveVisitTemplates() {
    activeView = false;
    resetUI();
    initial_load = true;
    loadVisitTemplatesDataIntoTablesWidget(activeView);
}

function getActiveVisitTemplates() {
    activeView = true;
    resetUI();
    initial_load = true;
    loadVisitTemplatesDataIntoTablesWidget(activeView);
}


/////////////////////////////////////////////////Study Data////////////////////////////////////////////////////////////////////////////////
function loadStudyScreenData()
{
    StudiesTable.createStudiesTable();

    StudiesTable.populateStudiesTable();
}

function buildPIName(firstName, lastName) {
    if (firstName != null && firstName !== undefined && firstName !== "") {
        return lastName + ", " + firstName;
    }
    else {
        return "";
    }
}

/////////////////////////////////////////////////Study Subject Data////////////////////////////////////////////////////////////////////////////////
function studySubjectStatusDialog(id) {
    DialogsUtil.showConfirmationDialog("#studySubject-status-dialog-confirm",{
        buttons: {
            "Yes": function () {
                $.getJSON("rest/study/changeStudySubjectStatus?studySubjectId=" + id, function (data) {
                    var confirmationMessage;
                    if (data.result == true) {
                        $('#study_responseLoading').css({visibility: "hidden"});
                        confirmationMessage = "Study Subject Status changed.";
                    }
                    else {
                        confirmationMessage = "Error: Study Subject Status cannot be changed.";
                    }
                    util_showMainMessage(confirmationMessage);
                    StudySubjectsTable.populateStudySubjectsTable();
                });
                $(this).dialog("close");
            },
            "No": function () {
                $(this).dialog("close");
            }
        }
    });
}

function changeStudySubjectStatus(id) {
    studySubjectStatusDialog(id);
}

/////////////////////////////////////////////////Study Member Data////////////////////////////////////////////////////////////////////////////////


// toggleStudyMemberStatusDialog is called when the status link (Active/Not Active) is clicked
function toggleStudyMemberStatusDialog(id) {
    DialogsUtil.showConfirmationDialog("#studyMember-status-dialog-confirm", {
        buttons: {
            "Yes": function () {
                $.getJSON("rest/study/changeStudyMemberStatus?studyMemberId=" + id, function (data) {
                    var confirmationMessage;
                    if (data.result == true) {
                        $('#study_responseLoading').css({visibility: "hidden"});
                        confirmationMessage = "Study Member Status changed.";
                    }
                    else {
                        confirmationMessage = "Error: Study Member Status cannot be changed.";
                    }
                    util_showMainMessage(confirmationMessage);
                    StudyMembersTable.populateTable();
                });
                $(this).dialog("close");
            },
            "No": function () {
                $(this).dialog("close");
            }
        }
    });
}


/////////////////////////////////////////////////Add Study Member Data////////////////////////////////////////////////////////////////////////////////
function loadAddStudyMemberData() {
    app_selectedStudy = JSON.parse(sessionStorage.getItem("studyData"));
    $('.addStudyMemberLink').css({display: "none"});

    AddStudyMembersTable.createTable();
    AddStudyMembersTable.populateTable();
}


function toggleAllStudyMembers(source) {
    if (source.checked == false) {
        $('.addStudyMemberLink').css({display: "none"});
    } else {
        $('.addStudyMemberLink').css({display: "inline-block"});
    }
    var checkboxes = document.getElementsByName('studyMemberCheckbox');
    for (var i = 0, n = checkboxes.length; i < n; i++) {
        checkboxes[i].checked = source.checked;
    }
}

function addStudyMemberStatusDialog() {
    DialogsUtil.showConfirmationDialog("#add-studyMember-dialog-confirm",{
        buttons: {
            "Yes": function () {
                var selected = AddStudyMembersTable.currentTable.getSelectedRows();
                var jsonData = JSON.stringify({studyId: app_selectedStudy.id, studyMembers: selected});
                $.post("rest/study/addStudyMember", {data: jsonData}, function (data) {
                    var confirmationMessage = "Study Member(s) Added.";
                    $('#study_responseLoading').css({visibility: "hidden"});
                    util_showMainMessage(confirmationMessage);
                    AddStudyMembersTable.currentTable.destroy();
                    $('.addStudyMemberLink').css({display: "none"});
                });
                $(this).dialog("close");
            },
            "No": function () {
                $(this).dialog("close");
            }
        }
    });
}

function addStudyMembers() {
    app_selectedStudy = JSON.parse(sessionStorage.getItem("studyData"));
    addStudyMemberStatusDialog();
}

function getSelectedStudyMembers() {
    selected = new Array();
    $(".studyMemberCheckbox:checked").each(function () {
        selected.push($(this).val());
    });
    if (selected.length == 0) {
        $('.addStudyMemberLink').css({display: "none"});
    }
    else {
        $('.addStudyMemberLink').css({display: "inline-block"});
    }
    return selected;
}


function showStudyDetail(id) {
    $.getJSON("rest/study/getStudyData?study=" + id, function (data) {
        sessionStorage.setItem("studyData", JSON.stringify(data));
        sessionStorage.setItem("mode", JSON.stringify('view'));
        window.location.href = "study_detail.html#StudyDetail";
    });
}

function studyDoubleClick(row, id) {
    $(row).bind("dblclick", function () {
        return showStudyDetail(id);
    });
}

function loadVisitTemplatesTab() {
    app_selectedStudy = JSON.parse(sessionStorage.getItem("studyData"));
    user = JSON.parse(sessionStorage.getItem("userData"));
    if (typeof app_selectedStudy == 'undefined') {
        return;
    }
    else {
        parent.location.hash = "VisitTemplates";
        resetUI();
        initial_load = true;
        activeView = true;
        loadVisitTemplatesDataIntoTablesWidget(activeView);
    }
}

function loadStudySubjectsTab() {
    app_selectedStudy = JSON.parse(sessionStorage.getItem("studyData"));
    user = JSON.parse(sessionStorage.getItem("userData"));
    isEpicMode = sessionStorage.getItem("subjectMode").toLowerCase() == "epic";

    if (typeof app_selectedStudy !== 'undefined') {
        parent.location.hash = "StudySubjects";
        initial_load = true;

        $('#study_localId2').html('(' + app_selectedStudy.localId + ')');
        $('#study_name2').html(app_selectedStudy.name);

        StudySubjectsTable.createStudySubjectsTable();
        StudySubjectsTable.populateStudySubjectsTable();
    }
}

function loadStudyMembersTab() {
    app_selectedStudy = JSON.parse(sessionStorage.getItem("studyData"));
    user = JSON.parse(sessionStorage.getItem("userData"));
    if (typeof app_selectedStudy == 'undefined') {
        return;
    }
    else {
        parent.location.hash = "StudyMembers";
        if (user.institutionRole.id == STUDY_STAFF) {
            $('.study_newStudyMemberButton').css({visibility: "hidden"});
        }
        else {
            $('.study_newStudyMemberButton').css({visibility: "visible"});
        }

        $('#study_localId3').html('(' + app_selectedStudy.localId + ')');
        $('#study_name3').html(app_selectedStudy.name);

        StudyMembersTable.createTable();
        StudyMembersTable.populateTable();
    }
}

function copyVisitTemplate(id) {
    $.getJSON("rest/study/getVisitTemplateData?visit=" + id, function (data) {
        sessionStorage.setItem("visitData", JSON.stringify(data));
        sessionStorage.setItem("selectedTab", 0);
        sessionStorage.setItem("mode", JSON.stringify('copy'));
        window.location.href = "create_visit.html";
    });
}

function deleteVisitDialog(id) {
    DialogsUtil.showConfirmationDialog("#delete-visit-dialog-confirm", {
        buttons: {
            "Yes": function () {
                $.getJSON("rest/study/deleteVisit?visitId=" + id, function (data) {
                    if (data.result == true) {
                        $('#study_responseLoading').css({visibility: "hidden"});
                        var confirmationMessage = "Visit Deleted.";
                        util_showMainMessage(confirmationMessage);
                    }
                    else {
                        var confirmationMessage = "Visit is currently being used or has been used in the past.";
                        util_showMainMessage(confirmationMessage);
                        return;
                    }
                    resetUI();
                    initial_load = true;
                    loadVisitTemplatesDataIntoTablesWidget(activeView);
                });
                $(this).dialog("close");
            },
            "No": function () {
                $(this).dialog("close");
                return;
            }
        }
    });
}

function deleteVisitTemplate(id) {
    deleteVisitDialog(id);
}

function study_setFormMode(mode) {
    study_mode = mode;
    study_clearErrors();
    app_selectedStudy = JSON.parse(sessionStorage.getItem("studyData"));
    $('.formElementRequired').css({display: "none"});

    initStudyAbstract();
    initStudyFundingComments();
    if (mode == 'view') {
        app_selectedStudy = JSON.parse(sessionStorage.getItem("studyData"));
        renderBreadcrumbs('study_view_form');
        loadStudyData();
    }
    else if (mode == 'new') {
        renderBreadcrumbs('study_new_form');
        $(".mainNameBlock").css({display: "none"});
        $('#study_form_title').text('Create New Study');
        $('.requiredFieldKey').css({visibility: "visible"});
        $('.study_confirmFormItem').css({visibility: "visible"});
        $('#study_restoreButton').css({display: "none"});
        $('#editStudyLink').css({visibility: "hidden"});
        $('#studyVisitLink').css({visibility: "hidden"});
        $('.formTextReadOnly').css({display: "none"});
        $('#studySubjectLink').css({visibility: "hidden"});
        $('#study_contacts').css({display: "none"});
        $('#study_visit_main_screen').css({display: "none"});
        $('#study_subject_main_screen').css({display: "none"});
        $('#study_member_main_screen').css({display: "none"});
        $('#studyDetails').css({display: "none"});
        $('#study_contacts_details').css({display: "none"});
        $('#study_form_buttons').css({visibility: "visible"});
        $('.formTextInput').css({display: "inline"});
        $('.formTextAreaSmaller').css({display: "inline"});
        $('#study_confirmFormItem').css({visibility: "visible"});
        $('.requiredIcon').css({visibility: "visible"});
        $("#study_status").combobox("destroy");
        $('#study_statusReadOnly').css({display: "inline"});
        $('#study_statusReadOnly').text('PENDING');
        document.study_form.study_CRCFunded.checked = true;
        study_clearForm();
        study_createDateWidgets();
    }
    else if (mode == 'edit') {
        renderBreadcrumbs('study_edit_form');
        $('#study_form_title').text('Edit study');
        $('#studyDetails').css({display: "none"});
        $('#study_name').text(app_selectedStudy.name);
        $('.requiredFieldKey').css({visibility: "visible"});
        $('.study_confirmFormItem').css({visibility: "visible"});
        $('#editStudyLink').css({visibility: "hidden"});
        $('#studyVisitLink').css({visibility: "visible"});
        $('#studySubjectLink').css({visibility: "visible"});
        $('#study_form_buttons').css({visibility: "visible"});
        $('.formTextReadOnly').css({display: "none"});
        $('.formTextInput').css({display: "inline"});
        $('.formTextAreaSmaller').css({display: "inline"});
        $('#study_confirmFormItem').css({visibility: "visible"});
        $('.requiredIcon').css({visibility: "visible"});
        $('#study_restoreButton').css({display: "inline"});
        $('#study_contacts').css({display: "block"});
        $('#study_visit_main_screen').css({display: "none"});
        $('#study_subject_main_screen').css({display: "none"});
        $('#study_member_main_screen').css({display: "none"});
        $('#study_contacts_details').css({display: "none"});
        study_createDateWidgets();
        study_loadEditForm();
    }
}

function initStudyFundingComments()
{
    for (var i = 1; i < 5; i++) {
        var fundingCommentLabel = 'study_fundingComment' + i;
        WidgetUtil.commentBox('#' + fundingCommentLabel, {
            rows: "3",
            cols: "200",
            width: "781px"
        });
    }
}

function initStudyAbstract()
{
    WidgetUtil.commentBox("#study_abstract", {
        maxChars: 3000,
        rows: "3",
        cols: "200",
        width: "975px",
        height: "100px"
    });
}
function study_clearForm() {
    $('#study_name').text('');
    $('#study_studyName').val('');
    $('#study_institution').combobox("clear");
    $('#study_status').combobox("clear");
    $('#study_industryInitiated').combobox("clear");
    $('#study_localId').val('');
    $('#study_catalystId').val('');
    $('#study_irb').val('');
    $('#study_shortName').val('');
    $('#study_clinicalTrial').val('');
    $('#study_newDrug').val('');
    $('#study_deviceExemption').val('');
    WidgetUtil.commentBox.clearValue('#study_abstract');
    $('#study_pediatric').combobox("clear");
    $('#study_crcCategory').combobox("clear");
    $('#study_irbInstitution').combobox("clear");
    $("#study_investigator").val('');
    $("#study_primaryscheduling").combobox("clear");
    $("#study_secondaryscheduling").combobox("clear");
    $("#study_thirdscheduling").combobox("clear");
    $("#study_physician1").combobox("clear");
    $("#study_physician2").combobox("clear");
    $("#study_protocolNurse").combobox("clear");
    $("#study_associateProtocolNurse").combobox("clear");
    $("#study_protocolNutritionist").combobox("clear");
    $("#study_associateProtocolNutritionist").combobox("clear");

    $('#study_irbExpirationDate').val('');
    $('#study_irbRenewalDate').val('');
    $('#study_irbApprovalDate').val('');
    $('#study_expectedEndDate').val('');
    $('#study_expectedStartDate').val('');
    $('#study_totalSubj').val('');
    $('#study_totalOutpatients').val('');
    $('#study_totalInpatients').val('');
    $("#study_CRCFunded").val('');
    document.study_form.study_CRCFunded.checked = true;

    for (var i = 1; i < 5; i++) {
        var fundingSourceLabel = 'study_fundingSource' + i;
        var fundingCommentLabel = 'study_fundingComment' + i;
        var fundingInstitutionLabel = 'study_fundingInstitution' + i;
        var fundingGrantLabel = 'study_grantId' + i;
        var fundingSiteCostCenterLabel = 'study_siteCostCenter' + i;
        var fundingTotalDirectAwardLabel = 'study_totalDirectDollarAward' + i;
        var fundingTotalIndirectAwardLabel = 'study_totalIndirectDollarAward' + i;
        var fundingProjectStartDateLabel = 'study_projectStartDate' + i;
        var fundingProjectEndDateLabel = 'study_projectEndDate' + i;
        $('#' + fundingSourceLabel).combobox('clear');
        $('#' + fundingInstitutionLabel).combobox('clear');
        $('#' + fundingGrantLabel).val('');
        $('#' + fundingSiteCostCenterLabel).val('');
        $('#' + fundingTotalDirectAwardLabel).val('');
        $('#' + fundingTotalIndirectAwardLabel).val('');
        $('#' + fundingProjectStartDateLabel).val('');
        $('#' + fundingProjectEndDateLabel).val('');
        WidgetUtil.commentBox.clearValue('#' + fundingCommentLabel);
    }
}

function study_loadEditForm() {
    getBrowserVersion();
    sessionStorage.setItem("previousMode", JSON.stringify('edit'));
    $('#study_studyName').val(app_selectedStudy.name);
    $('#study_institution').combobox("setValue", app_selectedStudy.institutionId);
    $('#study_status').combobox("setValue", app_selectedStudy.studyStatusId);
    $('#study_contacts').css({
        display: app_selectedStudy.isPending ? "none" : "block"
    });
    $('#study_industryInitiated').combobox("setValue", app_selectedStudy.industryInitiated == true ? 'Yes' : 'No');
    $('#study_localId').val(app_selectedStudy.localId);
    $('#study_catalystId').val(app_selectedStudy.catalystId);
    $('#study_irb').val(app_selectedStudy.irb);
    $('#study_shortName').val(app_selectedStudy.shortTitle);

    if (app_selectedStudy.crcFunded == true) {
        document.study_form.study_CRCFunded.checked = true;
    }
    else {
        document.study_form.study_CRCFunded.checked = false;
    }
    WidgetUtil.commentBox.setValue('#study_abstract', app_selectedStudy.studyAbstract);
    $('#study_pediatric').combobox("setValue", app_selectedStudy.pediatric);
    $('#study_crcCategory').combobox("setValue", app_selectedStudy.crcCategory);
    $('#study_clinicalTrial').val(app_selectedStudy.clinicalTrial);
    $('#study_newDrug').val(app_selectedStudy.newDrug);
    $('#study_deviceExemption').val(app_selectedStudy.deviceExemption);

    if (app_selectedStudy.irbInstitutionId == 0) {
        $('#study_irbInstitution').combobox('clear');
    }
    else {
        $('#study_irbInstitution').combobox("setValue", app_selectedStudy.irbInstitutionId);
    }

    $('#study_fundingSource1').combobox("clear");
    $('#study_fundingSource2').combobox("clear");
    $('#study_fundingSource3').combobox("clear");
    $('#study_fundingSource4').combobox("clear");

    for (var i = 0; i < app_selectedStudy.fundingSourceInfoList.length; i++) {
        var fundingSource = app_selectedStudy.fundingSourceInfoList[i];
        var fundingSourceLabel = 'study_fundingSource' + fundingSource.oneToFour;
        var fundingCommentLabel = 'study_fundingComment' + fundingSource.oneToFour;
        var fundingInstitutionLabel = 'study_fundingInstitution' + fundingSource.oneToFour;
        var fundingGrantLabel = 'study_grantId' + fundingSource.oneToFour;
        var fundingSiteCostCenterLabel = 'study_siteCostCenter' + fundingSource.oneToFour;
        var fundingTotalDirectAwardLabel = 'study_totalDirectDollarAward' + fundingSource.oneToFour;
        var fundingTotalIndirectAwardLabel = 'study_totalIndirectDollarAward' + fundingSource.oneToFour;
        var fundingProjectStartDateLabel = 'study_projectStartDate' + fundingSource.oneToFour;
        var fundingProjectEndDateLabel = 'study_projectEndDate' + fundingSource.oneToFour;
        $('#' + fundingSourceLabel).combobox("setValue", fundingSource.id);
        $('#' + fundingInstitutionLabel).combobox("setValue", fundingSource.centerAndInstitutionId);
        $('#' + fundingGrantLabel).val(fundingSource.grant);
        $('#' + fundingSiteCostCenterLabel).val(fundingSource.center);
        $('#' + fundingTotalDirectAwardLabel).val(fundingSource.directAward);
        $('#' + fundingTotalIndirectAwardLabel).val(fundingSource.indirectAward);
        var study_projectStartDateVal = fundingSource.start;
        if (study_projectStartDateVal != null) {
            if (majorVersion == "8") {
                var removeDash = study_projectStartDateVal.replace(/-/g, '/');
                var removeTime = removeDash.replace(/T.*/, '');
                var newDate = new Date(removeTime);
                var format_date = (newDate.getMonth() + 1) + "/" + newDate.getDate() + "/" + newDate.getFullYear();

                $('#' + fundingProjectStartDateLabel).val(format_date);
            }
            else {
                $('#' + fundingProjectStartDateLabel).val(dateFormat(study_projectStartDateVal, 'mm/dd/yyyy'));
            }
        }

        var study_projectEndDateVal = fundingSource.end;
        if (study_projectEndDateVal != null) {
            if (majorVersion == "8") {
                var removeDash = study_projectEndDateVal.replace(/-/g, '/');
                var removeTime = removeDash.replace(/T.*/, '');
                var newDate = new Date(removeTime);
                var format_date = (newDate.getMonth() + 1) + "/" + newDate.getDate() + "/" + newDate.getFullYear();

                $('#' + fundingProjectEndDateLabel).val(format_date);
            }
            else {
                $('#' + fundingProjectEndDateLabel).val(dateFormat(study_projectEndDateVal, 'mm/dd/yyyy'));
            }
        }
        WidgetUtil.commentBox.setValue('#' + fundingCommentLabel, fundingSource.comment);
        toggleFundingSourcesDetail(fundingSourceLabel, fundingSource.id, 'edit');
    }

    $('#study_totalSubj').val(app_selectedStudy.totalSubjects);
    $('#study_totalOutpatients').val(app_selectedStudy.totalOutpatientVisits);
    $('#study_totalInpatients').val(app_selectedStudy.totalInpatientVisits);

    var study_irbExpirationDateVal = app_selectedStudy.irbExpiration;
    if (study_irbExpirationDateVal != null) {
        if (majorVersion == "8") {
            var removeDash = study_irbExpirationDateVal.replace(/-/g, '/');
            var removeTime = removeDash.replace(/T.*/, '');
            var newDate = new Date(removeTime);
            var format_date = (newDate.getMonth() + 1) + "/" + newDate.getDate() + "/" + newDate.getFullYear();
            $('#study_irbExpirationDate').val(format_date);
        }
        else {
            $('#study_irbExpirationDate').val(dateFormat(study_irbExpirationDateVal, 'mm/dd/yyyy'));
        }
    }

    var study_irbRenewalDateVal = app_selectedStudy.irbRenewalDate;
    if (study_irbRenewalDateVal != null) {
        if (majorVersion == "8") {
            var removeDash = study_irbRenewalDateVal.replace(/-/g, '/');
            var removeTime = removeDash.replace(/T.*/, '');
            var newDate = new Date(removeTime);
            var format_date = (newDate.getMonth() + 1) + "/" + newDate.getDate() + "/" + newDate.getFullYear();
            $('#study_irbRenewalDate').val(format_date);
        }
        else {
            $('#study_irbRenewalDate').val(dateFormat(study_irbRenewalDateVal, 'mm/dd/yyyy'));
        }
    }

    var study_irbApprovalDateVal = app_selectedStudy.irbApprovalDate;
    if (study_irbApprovalDateVal != null) {
        if (majorVersion == "8") {
            var removeDash = study_irbApprovalDateVal.replace(/-/g, '/');
            var removeTime = removeDash.replace(/T.*/, '');
            var newDate = new Date(removeTime);
            var format_date = (newDate.getMonth() + 1) + "/" + newDate.getDate() + "/" + newDate.getFullYear();
            $('#study_irbApprovalDate').val(format_date);
        }
        else {
            $('#study_irbApprovalDate').val(dateFormat(study_irbApprovalDateVal, 'mm/dd/yyyy'));
        }
    }

    var study_expectedEndDateVal = app_selectedStudy.expectedEndDate;
    if (study_expectedEndDateVal != null) {
        if (majorVersion == "8") {
            var removeDash = study_expectedEndDateVal.replace(/-/g, '/');
            var removeTime = removeDash.replace(/T.*/, '');
            var newDate = new Date(removeTime);
            var format_date = (newDate.getMonth() + 1) + "/" + newDate.getDate() + "/" + newDate.getFullYear();
            $('#study_expectedEndDate').val(format_date);
        }
        else {
            $('#study_expectedEndDate').val(dateFormat(study_expectedEndDateVal, 'mm/dd/yyyy'));
        }
    }

    var study_expectedStartDateVal = app_selectedStudy.expectedStartDate;
    if (study_expectedStartDateVal != null) {
        if (majorVersion == "8") {
            var removeDash = study_expectedStartDateVal.replace(/-/g, '/');
            var removeTime = removeDash.replace(/T.*/, '');
            var newDate = new Date(removeTime);
            var format_date = (newDate.getMonth() + 1) + "/" + newDate.getDate() + "/" + newDate.getFullYear();
            $('#study_expectedStartDate').val(format_date);
        }
        else {
            $('#study_expectedStartDate').val(dateFormat(study_expectedStartDateVal, 'mm/dd/yyyy'));
        }
    }

    $("#study_investigator").combobox("setValue", app_selectedStudy.investigator.id == 0 ? '' : app_selectedStudy.investigator.id);
    $("#study_primaryscheduling").combobox("setValue", app_selectedStudy.scheduler.id == 0 ? '' : app_selectedStudy.scheduler.id);

    if (app_selectedStudy.secondaryScheduler.id == 0) {
        $('#study_secondaryscheduling').combobox("clear");
    }
    else {
        $("#study_secondaryscheduling").combobox("setValue", app_selectedStudy.secondaryScheduler.id);
    }

    if (app_selectedStudy.scheduler3.id == 0) {
        $('#study_thirdscheduling').combobox("clear");
    }
    else {
        $("#study_thirdscheduling").combobox("setValue", app_selectedStudy.scheduler3.id);
    }

    if (app_selectedStudy.physician.id == 0) {
        $('#study_physician1').combobox("clear");
    }
    else {
        $("#study_physician1").combobox("setValue", app_selectedStudy.physician.id);
    }

    if (app_selectedStudy.physician2.id == 0) {
        $('#study_physician2').combobox("clear");
    }
    else {
        $("#study_physician2").combobox("setValue", app_selectedStudy.physician2.id);
    }

    if (app_selectedStudy.protocolNurseString == "N/A") {
        $("#study_protocolNurse").combobox("setValue", -1);
    }
    else {
        $("#study_protocolNurse").combobox("setValue", app_selectedStudy.protocolNurse.id == 0 ? '' : app_selectedStudy.protocolNurse.id);
    }

    if (app_selectedStudy.associateNurse.id == 0) {
        $('#study_associateProtocolNurse').combobox("clear");
    }
    else {
        $("#study_associateProtocolNurse").combobox("setValue", app_selectedStudy.associateNurse.id);
    }

    if (app_selectedStudy.protocolNutritionistString == "N/A") {
        $("#study_protocolNutritionist").combobox("setValue", -1);
    }
    else {
        $("#study_protocolNutritionist").combobox("setValue", app_selectedStudy.protocolNutritionist.id == 0 ? '' : app_selectedStudy.protocolNutritionist.id);
    }

    if (app_selectedStudy.associateProtocolNutritionist.id == 0) {
        $('#study_associateProtocolNutritionist').combobox("clear");
    }
    else {
        $("#study_associateProtocolNutritionist").combobox("setValue", app_selectedStudy.associateProtocolNutritionist.id);
    }
}

function changeStudyStatusClick() {
    var selectedStudyStatusId = $.trim($("#study_status").combobox("getValue"));
    $('#study_contacts').css({
        display: findStudyStatusById(selectedStudyStatusId).isPending ? "none" : "block"
    });
}

//display Funding Source details
function displayFundingSourceDetails(obj, id) {
    var selectedValue = $.trim($("#" + obj.id).combobox("getValue"));
    if (selectedValue != 2) {
        $('#study_fundingInstitution' + id).combobox("clear");
    }
    var fundingCommentLabel = 'study_fundingComment' + id;
    var fundingInstitutionLabel = 'study_fundingInstitution' + id;
    var fundingGrantLabel = 'study_grantId' + id;
    var fundingSiteCostCenterLabel = 'study_siteCostCenter' + id;
    var fundingTotalDirectAwardLabel = 'study_totalDirectDollarAward' + id;
    var fundingTotalIndirectAwardLabel = 'study_totalIndirectDollarAward' + id;
    var fundingProjectStartDateLabel = 'study_projectStartDate' + id;
    var fundingProjectEndDateLabel = 'study_projectEndDate' + id;
    $('#' + fundingInstitutionLabel).combobox("clear");
    $('#' + fundingGrantLabel).val('');
    $('#' + fundingSiteCostCenterLabel).val('');
    $('#' + fundingTotalDirectAwardLabel).val('');
    $('#' + fundingTotalIndirectAwardLabel).val('');
    $('#' + fundingProjectStartDateLabel).val('');
    $('#' + fundingProjectEndDateLabel).val('');
    WidgetUtil.commentBox.clearValue('#' + fundingCommentLabel);
    toggleFundingSourcesDetail(obj.id, selectedValue, 'create');
}

function toggleFundingSourcesDetail(id, selectedValue, displayMode) {
    var elements = $("." + id.replace(/\s/g, "."));
    var elementId = 0;
    if (displayMode == 'view') {
        elementId = 1;
    }
    for (var index = 0; index < elements.length; index++) {
        if ((selectedValue != 2 && index == elementId) || selectedValue == '') {
            elements[index].style.display = "none";
        }
        else {
            elements[index].style.display = "";
        }
    }
}

function study_clearErrors() {
    $('#study_institutionValidation').css({visibility: "hidden"});
    $('#study_studyNameValidation').css({visibility: "hidden"});
    $('#study_statusValidation').css({visibility: "hidden"});
    $('#study_industryInitiatedValidation').css({visibility: "hidden"});
    $('#study_expectedStartDateValidation').css({visibility: "hidden"});
    $('#study_expectedEndDateValidation').css({visibility: "hidden"});
    $('#study_irbApprovalDateValidation').css({visibility: "hidden"});
    $('#study_irbRenewalDateValidation').css({visibility: "hidden"});
    $('#study_irbExpirationDateValidation').css({visibility: "hidden"});
    $('#study_investigatorValidation').css({visibility: "hidden"});
    $('#study_primaryschedulingValidation').css({visibility: "hidden"});
    $('#study_protocolNurseValidation').css({visibility: "hidden"});
    $('#study_protocolNutritionistValidation').css({visibility: "hidden"});
    $('#study_localIdValidation').css({visibility: "hidden"});
    $('#study_fundingSourceValidation').css({display: "none"});
    $('#study_fundingSourceValidation').text('');
    for (var i = 1; i < 5; i++) {
        $('#study_fundingInstitution' + i + 'Validation').css({visibility: "hidden"});
        $('#study_totalDirectDollarAward' + i + 'Validation').css({visibility: "hidden"});
        $('#study_totalIndirectDollarAward' + i + 'Validation').css({visibility: "hidden"});
        $('#study_projectStartDate' + i + 'Validation').css({visibility: "hidden"});
        $('#study_projectEndDate' + i + 'Validation').css({visibility: "hidden"});
    }
    $('.formElementRequired').css({display: "none"});
}

function getStudyMemberLists() {
    app_selectedStudy = JSON.parse(sessionStorage.getItem("studyData"));
    $.getJSON("rest/study/getStudyMembersList?studyId=" + (app_selectedStudy != null ? app_selectedStudy.id : 0), function (data) {
        studyMembersSelectOptions = buildSelectsOptions(data);
        var studyMembersSelectOptionsOptional = buildSelectsOptions(data, '');

        var protocolNutritionistSelectOptions = protocolNutritionistBuildSelectsOptions(data, 'N/A');

        $("#study_investigator").html(studyMembersSelectOptions);
        $("#study_primaryscheduling").html(studyMembersSelectOptions);
        $("#study_secondaryscheduling").html(studyMembersSelectOptionsOptional);
        $("#study_thirdscheduling").html(studyMembersSelectOptionsOptional);
        $("#study_physician1").html(studyMembersSelectOptionsOptional);
        $("#study_physician2").html(studyMembersSelectOptionsOptional);
        $("#study_protocolNurse").html(protocolNutritionistSelectOptions);
        $("#study_associateProtocolNurse").html(studyMembersSelectOptionsOptional);
        $("#study_protocolNutritionist").html(protocolNutritionistSelectOptions);
        $("#study_associateProtocolNutritionist").html(studyMembersSelectOptionsOptional);
    });
}

function buildSelectsOptions(list, choiceLabel) {
    var html = [];

    if (choiceLabel !== undefined) {
        html[html.length] = "<option value=''>" + choiceLabel + "</option>";
    }
    for (var i = 0, len = list.length; i < len; i++) {
        html[html.length] = "<option value='";
        html[html.length] = list[i].user.id;
        html[html.length] = "'>";
        html[html.length] = list[i].user.firstName + " " + list[i].user.lastName;
        html[html.length] = "</option>";
    }
    return html.join('');
}

function protocolNutritionistBuildSelectsOptions(list, choiceLabel) {
    var html = [];

    if (choiceLabel !== undefined) {
        html[html.length] = "<option value='-1'>" + choiceLabel + "</option>";
    }
    for (var i = 0, len = list.length; i < len; i++) {
        html[html.length] = "<option value='";
        html[html.length] = list[i].user.id;
        html[html.length] = "'>";
        html[html.length] = list[i].user.firstName + " " + list[i].user.lastName;
        html[html.length] = "</option>";
    }
    return html.join('');
}

function filterStudyMemberClick() {
    $('#addStudyMember').css({display: "block"});
    resetUI();
    initial_load = true;
    var filterString = $.trim($("#filter_studymember_string").val());
    if (filterString == '') {
        $('#addStudyMemberHeaderData').css({display: "none"});
        $('#addStudyMemberData').css({display: "none"});
        util_showMainMessage("Please enter the filter string.");
        return;
    }
    loadAddStudyMemberData();
}

function emptyOrNull(theField) {

    var result = false;

    if (theField == null || theField.length < 1) {
        result = true;
    }

    return result;
}

function processStudyForm() {
    var isValid = true;
    study_clearErrors();
    if (emptyOrNull($("#study_institution").combobox("getValue"))) {
        showError('#study_institutionValidation');
        isValid = false;
    }
    if (emptyOrNull($.trim($("#study_studyName").val()))) {
        showError('#study_studyNameValidation');
        isValid = false;
    }
    else if ($.trim($("#study_studyName").val()).length > 255) {
        showError('#study_studyNameValidation', "Maximum characters of 255 exceeded");
        isValid = false;
    }

    if (study_mode == 'edit') {
        if (emptyOrNull($("#study_status").combobox("getValue"))) {
            showError('#study_statusValidation');
            isValid = false;
        }

        // Let's not rely on automatic conversions of int to string (or the reverse) by !=
        // Instead parse the option's string value to an int
        // (studyStatusIds.pending is an int)
        if (parseInt($("#study_status").combobox("getValue")) !== studyStatusIds.pending) {
            if (emptyOrNull($("#study_investigator").combobox("getValue"))) {
                showError('#study_investigatorValidation');
                isValid = false;
            }
            if (emptyOrNull($("#study_primaryscheduling").combobox("getValue"))) {
                showError('#study_primaryschedulingValidation');
                isValid = false;
            }
            if (emptyOrNull($("#study_protocolNurse").combobox("getValue"))) {
                showError('#study_protocolNurseValidation');
                isValid = false;
            }
            if (emptyOrNull($("#study_protocolNutritionist").combobox("getValue"))) {
                showError('#study_protocolNutritionistValidation');
                isValid = false;
            }
        }
    }

    if (emptyOrNull($("#study_industryInitiated").combobox("getValue"))) {
        showError('#study_industryInitiatedValidation');
        isValid = false;
    }

    if (emptyOrNull($.trim($("#study_localId").val()))) {
        showError('#study_localIdValidation');
        isValid = false;
    }

    var processStudyFormPart2 = function() {
        $('#study_fundingSourceValidation').text('Please select at least one Funding Source.');
        $('#study_fundingSourceValidation').css({opacity: 0.0, display: "inline-block"}).animate({opacity: 1.0});

        processStudyFormPart3(false);
    };

    if ($('#study_fundingSource1').combobox("getValue") == '' && $('#study_fundingSource2').combobox("getValue") == '' &&
        $('#study_fundingSource3').combobox("getValue") == '' && $('#study_fundingSource4').combobox("getValue") == '') {
        var isError = true;
        DialogsUtil.showMessageDialog("Please select at least one Funding Source.", processStudyFormPart2, isError);
    }
    else
    {
        processStudyFormPart3(isValid);
    }
}

function processStudyFormPart3(isValid) {

    var study_irbExpirationDateVal = $('#study_irbExpirationDate').val();
    var study_irbRenewalDateVal = $('#study_irbRenewalDate').val();
    var study_irbApprovalDateVal = $('#study_irbApprovalDate').val();
    var study_expectedEndDateVal = $('#study_expectedEndDate').val();
    var study_expectedStartDateVal = $('#study_expectedStartDate').val();

    if (study_irbApprovalDateVal != "" && study_irbExpirationDateVal == "") {
        showError('#study_irbExpirationDateValidation', 'Please enter IRB Expiration Date');
        isValid = false;
    }

    if (study_irbApprovalDateVal == "" && study_irbExpirationDateVal != "") {
        showError('#study_irbApprovalDateValidation', 'Please enter IRB Approval Date');
        isValid = false;
    }

    if (study_irbRenewalDateVal != "" && study_irbExpirationDateVal == "" && study_irbApprovalDateVal != "") {
        showError('#study_irbExpirationDateValidation', 'Please enter IRB Expiration Date');
        isValid = false;
    }

    if (study_irbRenewalDateVal != "" && study_irbExpirationDateVal != "" && study_irbApprovalDateVal == "") {
        showError('#study_irbApprovalDateValidation', 'Please enter IRB Approval Date');
        isValid = false;
    }

    if (study_irbRenewalDateVal != "" && study_irbExpirationDateVal == "" && study_irbApprovalDateVal == "") {
        showError('#study_irbApprovalDateValidation', 'Please enter IRB Approval Date');
        showError('#study_irbExpirationDateValidation', 'Please enter IRB Expiration Date');
        isValid = false;
    }


    if (study_irbRenewalDateVal == "" && study_irbExpirationDateVal != "" && study_irbApprovalDateVal != "") {
        if (($('#study_irbExpirationDate').datepicker('getDate') <=
            $('#study_irbApprovalDate').datepicker('getDate'))) {
            showError('#study_irbApprovalDateValidation', 'IRB Approval Date should be smaller than IRB Expiration Date.');
            showError('#study_irbExpirationDateValidation', 'IRB Expiration Date should be greater than IRB Approval Date.');
            isValid = false;
        }
    }

    if (study_irbRenewalDateVal != "" && study_irbExpirationDateVal != "" && study_irbApprovalDateVal != "") {
        if (($('#study_irbApprovalDate').datepicker('getDate') <=
            $('#study_irbRenewalDate').datepicker('getDate'))
            && ($('#study_irbRenewalDate').datepicker('getDate') <
            $('#study_irbExpirationDate').datepicker('getDate'))) {
        }
        else {
            showError('#study_irbRenewalDateValidation', 'Please enter valid date range');
            showError('#study_irbApprovalDateValidation', 'Please enter valid date range');
            showError('#study_irbExpirationDateValidation', 'Please enter valid date range');
            isValid = false;
        }
    }

    if ($('#study_expectedStartDate').datepicker('getDate') == "" && $('#study_expectedEndDate').datepicker('getDate') != "") {
        showError('#study_expectedStartDateValidation');
        isValid = false;
    }

    if ($('#study_expectedStartDate').datepicker('getDate') != "" && $('#study_expectedEndDate').datepicker('getDate') == "") {
        showError('#study_expectedEndDateValidation');
        isValid = false;
    }

    if ($('#study_expectedStartDate').datepicker('getDate') != "" && $('#study_expectedEndDate').datepicker('getDate') != ""
        && $('#study_expectedStartDate').datepicker('getDate') > $('#study_expectedEndDate').datepicker('getDate')) {
        showError('#study_expectedStartDateValidation', 'invalid date range');
        isValid = false;
    }

    if (study_expectedStartDateVal != "") {
        var expectedStartDate = new Date(study_expectedStartDateVal);
        expectedStartDate.setHours(0, 0, 0, 0);
        expectedStartDate.setHours(10);
        expectedStartDate.setMinutes(0);
    }
    if (study_expectedEndDateVal != "") {
        var expectedEndDate = new Date(study_expectedEndDateVal);
        expectedEndDate.setHours(0, 0, 0, 0);
        expectedEndDate.setHours(10);
        expectedEndDate.setMinutes(0);
    }
    if (study_irbApprovalDateVal != "") {
        var irbApprovalDate = new Date(study_irbApprovalDateVal);
        irbApprovalDate.setHours(0, 0, 0, 0);
        irbApprovalDate.setHours(10);
        irbApprovalDate.setMinutes(0);
    }
    if (study_irbRenewalDateVal != "") {
        var irbRenewalDate = new Date(study_irbRenewalDateVal);
        irbRenewalDate.setHours(0, 0, 0, 0);
        irbRenewalDate.setHours(10);
        irbRenewalDate.setMinutes(0);
    }
    if (study_irbExpirationDateVal != "") {
        var irbExpirationDate = new Date(study_irbExpirationDateVal);
        irbExpirationDate.setHours(0, 0, 0, 0);
        irbExpirationDate.setHours(10);
        irbExpirationDate.setMinutes(0);
    }


    var irbInstitutionValue = $.trim($('#study_irbInstitution').combobox("getValue"));

    if (isValid == false) {
        return;
    }

    var fundingSourceList = [];

    for (var i = 1; i < 5; i++) {
        var fundingSourceValue = $('#study_fundingSource' + i).combobox("getValue");
        var study_centerInstitutionVal = $.trim($('#study_fundingInstitution' + i).combobox("getValue"));
        if (fundingSourceValue == 2 && study_centerInstitutionVal == '') {
            showError('#study_fundingInstitution'+i+'Validation', 'Please select Center and Institutes for the Funding Source.');
            return;
        }

        if ($.trim($('#study_totalDirectDollarAward' + i).val()) != "" && !isNumber($.trim($('#study_totalDirectDollarAward' + i).val()))) {
            showError('#study_totalDirectDollarAward'+i+'Validation', 'Please enter valid Total Direct Dollar Award.');
            return;
        }

        if ($.trim($('#study_totalIndirectDollarAward' + i).val()) != "" && !isNumber($.trim($('#study_totalIndirectDollarAward' + i).val()))) {
            showError('#study_totalIndirectDollarAward'+i+'Validation', 'Please enter valid Total Indirect Dollar Award.');
            return;
        }

        if (($('#study_projectStartDate'+ i).val() != "" && $('#study_projectEndDate'+ i).val() != "") &&
            (new Date($('#study_projectStartDate' + i).val())) >= (new Date($('#study_projectEndDate'+ i).val()))) {
            showError('#study_projectStartDate'+i+'Validation', 'Please enter valid Project Start Date.');
            showError('#study_projectEndDate'+i+'Validation', 'Please enter valid Project End Date.');
            return;
        }

        if (fundingSourceValue != '') {
            var fundingData = {
                id: fundingSourceValue,
                comment: $.trim(WidgetUtil.commentBox.getValue('#study_fundingComment' + i)),
                oneToFour: i,
                grant: $('#study_grantId' + i).val(),
                center: $('#study_siteCostCenter' + i).val(),
                directAward: ($('#study_totalDirectDollarAward' + i).val() != "" ? $('#study_totalDirectDollarAward' + i).val() : null),
                indirectAward: ($('#study_totalIndirectDollarAward' + i).val() != "" ? $('#study_totalIndirectDollarAward' + i).val() : null),
                start: $('#study_projectStartDate' + i).val() != "" ? (new Date($('#study_projectStartDate' + i).val())).valueOf() : null,
                end: $('#study_projectEndDate' + i).val() != "" ? (new Date($('#study_projectEndDate' + i).val())).valueOf() : null,
                centerAndInstitutionId: (study_centerInstitutionVal != "" ? study_centerInstitutionVal : null)
            };
            fundingSourceList.push(fundingData);
        }
    }

    var investigatorValue = $.trim($('#study_investigator').combobox("getValue"));
    var physicianValue = $.trim($('#study_physician1').combobox("getValue"));
    var physician2Value = $.trim($('#study_physician2').combobox("getValue"));
    var schedulerValue = $.trim($('#study_primaryscheduling').combobox("getValue"));
    var secondarySchedulerValue = $.trim($('#study_secondaryscheduling').combobox("getValue"));
    var thirdSchedulerValue = $.trim($('#study_thirdscheduling').combobox("getValue"));
    var protocolNurseValue = $.trim($('#study_protocolNurse').combobox("getValue"));
    var associateNurseValue = $.trim($('#study_associateProtocolNurse').combobox("getValue"));
    var protocolNutritionistValue = $.trim($('#study_protocolNutritionist').combobox("getValue"));
    var associateProtocolNutritionistValue = $.trim($('#study_associateProtocolNutritionist').combobox("getValue"));
    var totalSubjectsValue = $.trim($('#study_totalSubj').val());
    var totalOutpatientVisitsValue = $.trim($('#study_totalOutpatients').val());
    var totalInpatientVisitsValue = $.trim($('#study_totalInpatients').val());
    var crcFunded = null;
    if ($('#study_CRCFunded').is(':checked')) {
        crcFunded = true;
    }
    else {
        crcFunded = false;
    }

    $('#study_responseLoading').css({visibility: "visible"});
    jsonData = JSON.stringify({
        id: (app_selectedStudy != null ? app_selectedStudy.id : 0),
        name: $.trim($("#study_studyName").val()),
        institution: $.trim($("#study_institution").combobox("getValue")),
        studyStatus: (study_mode == 'edit' ? $('#study_status').combobox("getValue") : studyStatusIds.pending),
        industryInitiated: $('#study_industryInitiated').combobox("getValue"),
        localId: $.trim($("#study_localId").val()),
        catalystId: $.trim($('#study_catalystId').val()),
        irb: $.trim($('#study_irb').val()),
        shortTitle: $.trim($('#study_shortName').val()),
        pediatric: $.trim($('#study_pediatric').combobox("getValue")),
        crcCategory: $.trim($('#study_crcCategory').combobox("getValue")),
        fundingSourceInfoList: fundingSourceList,
        studyAbstract: $.trim(WidgetUtil.commentBox.getValue('#study_abstract')),
        clinicalTrial: $.trim($('#study_clinicalTrial').val()),
        newDrug: $.trim($('#study_newDrug').val()),
        deviceExemption: $.trim($('#study_deviceExemption').val()),
        irbInstitution: (irbInstitutionValue != "" ? irbInstitutionValue : null),
        investigator: (investigatorValue != "" ? investigatorValue : null),
        crcFunded: crcFunded,
        physician: (physicianValue != "" ? physicianValue : null),
        physician2: (physician2Value != "" ? physician2Value : null),
        scheduler: (schedulerValue != "" ? schedulerValue : null),
        secondaryScheduler: (secondarySchedulerValue != "" ? secondarySchedulerValue : null),
        scheduler3: (thirdSchedulerValue != "" ? thirdSchedulerValue : null),
        protocolNurse: (protocolNurseValue != "" ? protocolNurseValue : null),
        associateNurse: (associateNurseValue != "" ? associateNurseValue : null),
        protocolNutritionist: (protocolNutritionistValue != "" ? protocolNutritionistValue : null),
        associateProtocolNutritionist: (associateProtocolNutritionistValue != "" ? associateProtocolNutritionistValue : null),
        irbExpiration: (study_irbExpirationDateVal != "" ? irbExpirationDate.valueOf() : null),
        irbRenewalDate: (study_irbRenewalDateVal != "" ? irbRenewalDate.valueOf() : null),
        irbApprovalDate: (study_irbApprovalDateVal != "" ? irbApprovalDate.valueOf() : null),
        expectedStartDate: (study_expectedStartDateVal != "" ? expectedStartDate.valueOf() : null),
        expectedEndDate: (study_expectedEndDateVal != "" ? expectedEndDate.valueOf() : null),
        totalSubjects: (totalSubjectsValue != "" ? totalSubjectsValue : 0),
        totalOutpatientVisits: (totalOutpatientVisitsValue != "" ? totalOutpatientVisitsValue : 0),
        totalInpatientVisits: (totalInpatientVisitsValue != "" ? totalInpatientVisitsValue : 0)
    });

    var url = "rest/study/createStudy";

    if (study_mode == 'edit') {
        url = "rest/study/editstudy";
    }
    $.post(url, {data: jsonData},
        function (data) {
            var parsedData = JSON.parse(data);
            if (parsedData.result === false) {
                util_showMainMessage("Local ID already exists.");
                $('#study_responseLoading').css({visibility: "hidden"});
                return;
            } else {
                var confirmationMessage = "New Study created";
                if (study_mode == 'edit') {
                    confirmationMessage = "Study updated";
                }
                study_clearErrors();
                $('#study_responseLoading').css({visibility: "hidden"});

                var showOnOk = function () {
                    showStudyDetail(parsedData.id);
                };

                DialogsUtil.showMessageDialog(confirmationMessage, showOnOk);
            }
        });
}

function isNumber(n) {
    return !isNaN(parseFloat(n));
}

var displayStudyVisitTemplateData = function () {
    var visitNameDisplay = '(' + app_selectedStudy.localId + ')' +
        '<br> <div style="padding: 3px;"></div>' +
        app_selectedVisit.studyName;

    $('#study_visit_name').html(visitNameDisplay);
    $('#visit_name').text(app_selectedVisit.visitName);
    $('#study_visit_name2').html(visitNameDisplay);
    $('#visit_name2').text(app_selectedVisit.visitName);
};

function study_visit_setFormMode(mode) {
    study_visit_mode = mode;
    app_selectedVisit = JSON.parse(sessionStorage.getItem("visitData"));
    app_selectedStudy = JSON.parse(sessionStorage.getItem("studyData"));
    $('.formElementRequired').css({visibility: "hidden"});
    if (mode == 'view') {
        renderBreadcrumbs('study_visit_view_form');
        $('#study_visit_form_title').text('Additional Details');
        displayStudyVisitTemplateData();
        if (user.institutionRole.id == STUDY_STAFF) {
            $('#editStudyVisitLink').css({visibility: "hidden"});
        }
        else {
            $('#editStudyVisitLink').css({visibility: "visible"});
        }
        $('#editStudyLink').css({visibility: "visible"});
        $('.requiredFieldKey').css({visibility: "hidden"});
        $('#study_visit_form_buttons').css({visibility: "hidden"});
        $('.requiredIcon').css({visibility: "hidden"});
        $('.numbersOnly').css({display: "none"});
        $('.formTextReadOnly').css({display: "inline-block"});
        $('.formTextInput').css({display: "none"});
        $('.formSelectInput').css({display: "none"});
        $('.formTextAreaSmaller').css({display: "none"});
        $('.formCheckBox').css({display: "none"});
        $('.formCheckBoxReadOnly').css({display: "inline"});
        $('.study_confirmFormItem').css({visibility: "hidden"});
        $('#study_visitNameReadOnly').text(app_selectedVisit.visitName);
        $('#study_shortVisitNameReadOnly').text(app_selectedVisit.shortName);
        $('#study_visitSublocationReadOnly').text(app_selectedVisit.sublocationName);
        $('#study_visitTypeReadOnly').text(app_selectedVisit.visitTypeName);
        $('#inpatient_relative_time').css((app_selectedVisit.visitTypeId == 1 || app_selectedVisit.visitTypeId == 2 || app_selectedVisit.visitTypeId == 3) ? {display: "none"} : {display: "block"});

        WidgetUtil.counterDisplay.create($("#viewCommentLink"), app_selectedVisit.numVisitTemplateComments,
            { backgroundColor: "#9b874b"});

        if(app_selectedVisit.relativeTime === true) {
            document.visit_form.study_relativeTimeReadOnly.checked = true;
        }
        else {
            document.visit_form.study_relativeTimeReadOnly.checked = false;
        }

        if(app_selectedVisit.billable === true) {
            document.visit_form.study_billableTemplateReadOnly.checked = true;
        }
        else {
            document.visit_form.study_billableTemplateReadOnly.checked = false;
        }

        if(app_selectedVisit.researchPharmacy === true) {
            document.visit_form.study_researchPharmacyReadOnly.checked = true;
        }
        else {
            document.visit_form.study_researchPharmacyReadOnly.checked = false;
        }
        if(app_selectedVisit.institutionNonCrc === true) {
            document.visit_form.study_institution_noncrcReadOnly.checked = true;
        }
        else {
            document.visit_form.study_institution_noncrcReadOnly.checked = false;
        }
        if(app_selectedVisit.nonInstitutionNonCrc === true) {
            document.visit_form.study_nonInstitution_nonCrcReadOnly.checked = true;
        }
        else {
            document.visit_form.study_nonInstitution_nonCrcReadOnly.checked = false;
        }

        $('#study_visitCommentReadOnly').text(app_selectedVisit.comment);

        $('#study_nursingReadOnly').text(app_selectedVisit.nursing);
        $('#study_nutritionReadOnly').text(app_selectedVisit.nutrition);
        $('#study_processingReadOnly').text(app_selectedVisit.processing);
        $('#study_setupReadOnly').text(app_selectedVisit.setup);
        $('#visitgrid_finalApprovalStatus').text(app_selectedVisit.approved);
    }
    else if (mode === 'new') {
        renderBreadcrumbs('study_visit_new_form');
        $('#visit_name').text('');
        $('#study_visit_name').text('');
        $('#study_visit_form_title').text('Create New Visit Template');
        $('.requiredFieldKey').css({visibility: "visible"});
        $('.study_confirmFormItem').css({visibility: "visible"});
        $('.formTextReadOnly').css({display: "none"});
        $('#study_visit_restoreButton').css({display: "none"});
        $('#editStudyVisitLink').css({visibility: "hidden"});
        $('.requiredIcon').css({visibility: "hidden"});
        $('.numbersOnly').css({display: "inline"});
        $('#study_visit_form_buttons').css({visibility: "visible"});
        $('.formTextInput').css({display: "inline"});
        $('.formTextAreaSmaller').css({display: "inline"});
        $('.formCheckBox').css({display: "inline"});
        $('.formCheckBoxReadOnly').css({display: "none"});
        $('#study_confirmFormItem').css({visibility: "visible"});
        $('.requiredIcon').css({visibility: "visible"});
        $('#editStudyLink').css({visibility: "hidden"});
        $('#visit_approval_screen').css({display: "none"});
        $('#scheduler_form_screen').css({display: "none"});
        $('#inpatient_relative_time').css({display: "none"});
        WidgetUtil.commentBox($("#study_visitComment"));
        study_visit_clearForm();
    }
    else if (mode === 'edit') {
        renderBreadcrumbs('study_visit_edit_form');
        $('#study_visit_form_title').text('Edit Visit');
        $('#study_visit_name').text(app_selectedVisit.studyName);
        $('#visit_name').text(app_selectedVisit.visitName);
        $('.requiredFieldKey').css({visibility: "visible"});
        $('.study_confirmFormItem').css({visibility: "visible"});
        $('#editStudyVisitLink').css({visibility: "hidden"});
        $('#editStudyLink').css({visibility: "hidden"});
        $('#study_visit_form_buttons').css({visibility: "visible"});
        $('.numbersOnly').css({display: "inline"});
        $('.formTextReadOnly').css({display: "none"});
        $('.formTextInput').css({display: "inline"});
        $('.requiredIcon').css({visibility: "hidden"});
        $('.formCheckBox').css({display: "inline"});
        $('.formCheckBoxReadOnly').css({display: "none"});
        $('.formTextAreaSmaller').css({display: "inline"});
        $('#study_confirmFormItem').css({visibility: "visible"});
        $('.requiredIcon').css({visibility: "visible"});
        $('#study_restoreButton').css({display: "inline"});
        $('#visit_approval_screen').css({display: "none"});
        $('#scheduler_form_screen').css({display: "none"});
        $('#inpatient_relative_time').css((app_selectedVisit.visitTypeId === 1 || app_selectedVisit.visitTypeId === 2 || app_selectedVisit.visitTypeId === 3) ? {display: "none"} : {display: "block"});
        study_visit_loadEditForm();
    }
    else if (mode === 'copy') {
        renderBreadcrumbs('study_visit_copy_form');
        $('#study_visit_form_title').text('Copying Visit Template: ' + app_selectedVisit.visitName);
        $('#study_visit_name').text(app_selectedVisit.studyName);
        $('#visit_name').text(app_selectedVisit.visitName);
        $('#editStudyVisitLink').css({visibility: "hidden"});
        $('#editStudyLink').css({visibility: "hidden"});
        $('.requiredFieldKey').css({visibility: "visible"});
        $('#study_visit_form_buttons').css({visibility: "visible"});
        $('.requiredIcon').css({visibility: "hidden"});
        $('.formTextInput').css({display: "inline"});
        $('.formTextReadOnly').css({display: "inline"});
        $('.formSelectInput').css({display: "none"});
        $('.formTextAreaSmaller').css({display: "inline"});
        $('#study_confirmFormItem').css({visibility: "visible"});
        $('.formCheckBox').css({display: "none"});
        $('.formCheckBoxReadOnly').css({display: "inline"});
        $('.requiredIcon').css({visibility: "visible"});
        $('#study_restoreButton').css({display: "inline"});
        $('#study_visit_restoreButton').css({display: "none"});
        $('#visit_approval_screen').css({display: "none"});
        $('#scheduler_form_screen').css({display: "inline"});
        $('#inpatient_relative_time').css((app_selectedVisit.visitTypeId === 1 || app_selectedVisit.visitTypeId === 2 || app_selectedVisit.visitTypeId === 3) ? {display: "none"} : {display: "block"});

        $('#study_visitNameReadOnly').text('');
        $('#study_visitName').val(app_selectedVisit.visitName);
        $('#study_shortVisitName').val(app_selectedVisit.shortName);

        $('#study_visitSublocation').combobox("setValue", app_selectedVisit.sublocationId);
        $('#study_visitType').combobox("setValue", app_selectedVisit.visitTypeId);
        WidgetUtil.commentBox($("#study_visitComment"));

        if(app_selectedVisit.relativeTime === true) {
            document.visit_form.study_relativeTimeReadOnly.checked = true;
        }
        else {
            document.visit_form.study_relativeTimeReadOnly.checked = false;
        }

        if(app_selectedVisit.billable === true) {
            document.visit_form.study_billableTemplateReadOnly.checked = true;
        }
        else {
            document.visit_form.study_billableTemplateReadOnly.checked = false;
        }

        if(app_selectedVisit.researchPharmacy === true) {
            document.visit_form.study_researchPharmacyReadOnly.checked = true;
        }
        else {
            document.visit_form.study_researchPharmacyReadOnly.checked = false;
        }
        if(app_selectedVisit.institutionNonCrc === true) {
            document.visit_form.study_institution_noncrcReadOnly.checked = true;
        }
        else {
            document.visit_form.study_institution_noncrcReadOnly.checked = false;
        }
        if(app_selectedVisit.nonInstitutionNonCrc === true) {
            document.visit_form.study_nonInstitution_nonCrcReadOnly.checked = true;
        }
        else {
            document.visit_form.study_nonInstitution_nonCrcReadOnly.checked = false;
        }

        WidgetUtil.commentBox.clearValue($("#study_visitComment"));

        $("#study_nursing").spinner("value", app_selectedVisit.nursing);
        $("#study_nutrition").spinner("value", app_selectedVisit.nutrition);
        $("#study_processing").spinner("value", app_selectedVisit.processing);
        $("#study_setup").spinner("value", app_selectedVisit.setup);
    }
}

function study_visit_clearForm() {
    $('#study_visitName').val('');
    $('#study_shortVisitName').val('');
    $('#study_visitSublocation').combobox("clear");
    $('#study_visitType').combobox("clear");
    $('#study_researchPharmacy').val('');
    $('#study_institution_noncrc').val('');
    $('#study_nonInstitution_nonCrc').val('');
    $('#study_visitComment').val('');
    $('#study_nursing').val('');
    $('#study_nutrition').val('');
    $('#study_processing').val('');
    $('#study_setup').val('');
    $('#study_relative_time').val('');
}

function study_visit_loadEditForm() {
    sessionStorage.setItem("previousMode", JSON.stringify('edit'));
    $('#study_visitName').val(app_selectedVisit.visitName);
    $('#study_shortVisitName').val(app_selectedVisit.shortName);
    $('#study_visitSublocation').combobox("setValue", app_selectedVisit.sublocationId);
    $('#study_visitType').combobox("setValue", app_selectedVisit.visitTypeId);
    WidgetUtil.commentBox($("#study_visitComment"));

    if(app_selectedVisit.relativeTime === true) {
        document.visit_form.study_relativeTime.checked = true;
    }
    else {
        document.visit_form.study_relativeTime.checked = false;
    }

    if(app_selectedVisit.billable === true) {
        document.visit_form.study_billableTemplate.checked = true;
    }
    else {
        document.visit_form.study_billableTemplate.checked = false;
    }

    if(app_selectedVisit.researchPharmacy === true) {
        $("#study_researchPharmacy").prop("checked", true);
    }
    else {
        $("#study_researchPharmacy").prop("checked", false);
    }
    if(app_selectedVisit.institutionNonCrc === true) {
        document.visit_form.study_institution_noncrc.checked = true;
    }
    else {
        document.visit_form.study_institution_noncrc.checked = false;
    }
    if(app_selectedVisit.nonInstitutionNonCrc === true) {
        document.visit_form.study_nonInstitution_nonCrc.checked = true;
    }
    else {
        document.visit_form.study_nonInstitution_nonCrc.checked = false;
    }
    $('#study_visitComment').val('');

    if (app_selectedVisit.nursing == 0 || app_selectedVisit.nursing > 0) {
        $('#study_nursing').val(app_selectedVisit.nursing);
    }
    else {
        $('#study_nursing').val('');
    }

    if (app_selectedVisit.nutrition == 0 || app_selectedVisit.nutrition > 0) {
        $('#study_nutrition').val(app_selectedVisit.nutrition);
    }
    else {
        $('#study_nutrition').val('');
    }

    if (app_selectedVisit.processing == 0 || app_selectedVisit.processing > 0) {
        $('#study_processing').val(app_selectedVisit.processing);
    }
    else {
        $('#study_processing').val('');
    }

    if (app_selectedVisit.setup == 0 || app_selectedVisit.setup > 0) {
        $('#study_setup').val(app_selectedVisit.setup);
    }
    else {
        $('#study_setup').val('');
    }
}

function loadVisitTemplateApprovalsTab() {
    sessionStorage.setItem("selectedTab", 0);
    app_selectedVisit = JSON.parse(sessionStorage.getItem("visitData"));
    app_selectedStudy = JSON.parse(sessionStorage.getItem("studyData"));

    if(UserRoleUtil.userIsCrcStaff() || UserRoleUtil.userIsFrontDesk()) {

        var visitApprovalBtn = $("<a></a>", {"id": "study_newVisitApprovalButton",
            "onclick": "javascript:newVisitApprovalClick();", "class": "liveLink"}).text("Comment/Approve");
        $("#visitApproval").empty();
        $("#visitApproval").append(visitApprovalBtn);
    }

    $.getJSON("rest/study/getVisitTemplateData?visit=" + app_selectedVisit.id, function (data) {
        sessionStorage.setItem("visitData", JSON.stringify(data));
        $('#visitgrid_finalApprovalStatus').text(data.approved);
    });
    displayStudyVisitTemplateData();
    VisitApprovalTable.createVisitApprovalTable();
    VisitApprovalTable.populateVisitApprovalTable();
}

function viewVisitCommentClick() {
    $("#visit_comment_dialog").dialog({modal: true, height: 460, width: 700});
    loadVisitTemplateComments();
}

function loadVisitTemplateComments() {
    app_selectedVisit = JSON.parse(sessionStorage.getItem("visitData"));
    commonData();
    $.getJSON("rest/study/getVisitComments?id=" + app_selectedVisit.id, function (data) {
        var iteration = 0;

        var out = " <table id='visitComments'>"
            + " <tr>" +
            "   <td class='not_sortable'><strong>User ID</strong></td>" +
            "   <td class='not_sortable'><strong>Date</strong></td> " +
            "   <td class='not_sortable'><strong>Comment</strong></td>" +
            "  </tr> ";

        $.each(data, function (key, val) {
            out += "<tr>"
            out += "   <td id='visitCommentUserId_" + key + "'>" + showValue(val.ecommonsId) + "</td>" +
                "          <td id='visitCommentDate_" + key + "'>" + showDateTime(val.commentDate) + "</td>" +
                "          <td id='visitCommentText_" + key + "'>" + showValue(val.comment) + "</td>" +
                " </tr>";
            iteration++;
        });

        out += " </table>";

        $('#visitComments_data').html(out);

    });
}

function viewBookedVisitCommentClick() {
    $("#appt_visit_comment_dialog").dialog({modal: true, height: 460, width: 703, dialogClass: "commentDialog"});
    BookedVisitCommentsTable.createBookedVisitCommentsTable();
    BookedVisitCommentsTable.populateBookedVisitCommentsTable();
}

var nursingData = ['None', '5 - 15 min', '16 - 30 min', '31 - 60 min', '61 - 120 min', '121 - 240 min', '241 - 420 min', 'Over 420 min'];

var nutritionData = ['0', 'None', '5 - 15 min', '16 - 30 min', '31 - 60 min', '61 - 90 min', '91 - 150 min', '151 - 240 min', 'Over 240 min'];

var processingData = ['None', '1 - 15 min', '16 - 30 min', '31 - 60 min', '61 - 180 min', '181 - 300 min', '301 - 480 min', 'Over 480 min'];

var setupData = ['None', '2.5 - 15 min', '16 - 30 min', '31 - 60 min', '60 - 120 min', '120+ min', 'N/A', 'N/A'];

function loadLevelOfServices(lineLevelData, columnName) {
    app_selectedVisit = JSON.parse(sessionStorage.getItem("visitData"));
    commonData();
    var iteration = 0;
    var out = " <table id='lineLevelService'>"
        + " <tr>" +
        "   <td class='not_sortable'><strong>Level</strong></td>" +
        "   <td class='not_sortable'><strong>" + columnName + "</strong></td> " +
        "  </tr> ";

    $.each(nursingData, function (index, val) {
        out += "<tr>"
        out += "   <td>" + index + "</td>" +
            "          <td>" + lineLevelData[index] + "</td>" +
            " </tr>";
        iteration++;
    });

    out += " </table>";

    $('#nursing_data').html(out);
    $('#nutrition_data').html(out);
    $('#processing_data').html(out);
    $('#setup_data').html(out);
}

function nursingIntensityClick() {
    $("#nursingLevelOfService_dialog").dialog({modal: true, height: 400, width: 335});
    loadLevelOfServices(nursingData, "Nursing Time Required");
}

function nutritionIntensityClick() {
    $("#nutritionLevelOfService_dialog").dialog({modal: true, height: 400, width: 335});
    loadLevelOfServices(nutritionData, "Nutrition Time Required");
}


function processingIntensityClick() {
    $("#processingLevelOfService_dialog").dialog({modal: true, height: 400, width: 335});
    loadLevelOfServices(processingData, "Processing Time Required");
}

function setupIntensityClick() {
    $("#setupLevelOfService_dialog").dialog({modal: true, height: 400, width: 335});
    loadLevelOfServices(setupData, "Setup Time Required");
}

function loadBuildVisitTemplateData() {
    $.blockUI();
    sessionStorage.setItem("selectedTab", 2);
    window.location.href = "visit_detail.html";
}

function loadVisitTemplateDetailData() {
    sessionStorage.setItem("selectedTab", 0);
    commonVisitDetailData();
}

function createAnnotationsTable(responseData) {
    var iteration = 0;

    var header_table =
        " <table id='overrideAnnotations' rules='cols' width='100%' border='0' cellspacing='0'>" +
        "  <thead> <tr>" +
        "   <th width='7%' style='padding: 5px; border: 1px solid #CCC;'>Select</th> " +
        "   <th width='34%' style='padding: 5px; border: 1px solid #CCC;'><strong>Resource Activity</strong></th> " +
        "   <th width='9%' style='padding: 5px; border: 1px solid #CCC;'><strong>Quantity</strong></th>" +
        "   <th width='50%' style='padding: 5px; border: 1px solid #CCC;'><strong>Comment</strong></th>" +
        "  </tr> </thead> <table>";

    $('#resourceAnnotationsHeaderData').html(header_table);

    var out = " <table id='overrideAnnotations' class='overrideTableClass'> <tbody>";

    $.each(responseData, function (key, val) {
        var annotationsId = val.id;
        var quantityCellId = 'q' + annotationsId;
        var commentCellId = 'c' + annotationsId;
        out += ((iteration % 2 == 1) ? "<tr class='altRow'>" : "<tr> ");
        var selected = " <input type='checkbox' class='form-checkbox' ";
        if (val.selected) {
            selected = " <input type='checkbox' class='form-checkbox' checked='checked' ";
        }

        var disableCell = "disabledCell";
        var defaultQuantity = val.quantity;

        if (defaultQuantity == 0) {
            defaultQuantity = 1;
        }

        if (val.quantifiable) {
            disableCell = "";
        }

        out +=
            "   <td width='7%' style='text-align:center;'> " +
            selected +
            " onclick='updateOverrideRow(" + annotationsId + "," + val.quantifiable + ")' " +
            "   id=" + annotationsId + " value=" + annotationsId + "/> " +
            "   </td>" +

            "   <td width='34%' align='left' valign='top' id='" + annotationsId + "'>" + val.name + "</td>" +

            "   <td width='9%' align='left' valign='top' id='" + quantityCellId + "' class='" + disableCell + "'" +
            "   onclick='return inlineOverrideEdit(this, event, 40, 3, " + annotationsId + "," + val.quantifiable + ");'>"
            + defaultQuantity +
            "   </td>" +

            "   <td width='50%' align='left' valign='top' id=" + commentCellId +
            "   onclick='return inlineOverrideEdit(this, event, 400, 255, " + annotationsId + "," + val.quantifiable + ");'>"
            + showValue(val.comment) +
            "   </td>" +

            " </tr>";
        iteration++;
    });

    out += "</tbody> </table>";

    $('#resourceAnnotationsData').html(out);
    responseData.length > 0 ? $('#resourceAnnotationsData').css({display: "inline-block"}) : $('#resourceAnnotationsData').css({display: "none"});
    $('.annotationsTable').css({display: "inline-block"});
}

function filterAnnotations(obj) {
    $.blockUI();
    var checkboxClass = "." + ResourceAnnotationsTable.checkboxClass;
    var checkedCheckboxClass = "." + ResourceAnnotationsTable.checkboxClass + ":checked";
    if (obj.value == "All") {
        $(checkboxClass).closest("tr").show();
        $(checkedCheckboxClass).closest("tr").show();
    }
    else if (obj.value == "Yes") {
        $(checkboxClass).closest("tr").hide();
        $(checkedCheckboxClass).closest("tr").show();
    }
    else if (obj.value == "No") {
        $(checkboxClass).closest("tr").show();
        $(checkedCheckboxClass).closest("tr").hide();
    }
    $.unblockUI();
}

function updateOverrideRow(id, quantifiable) {
    var checkbox = document.getElementById(id);
    var quantityId = 'q' + id;
    var commentId = 'c' + id;

    if (checkbox.checked) {
        checkbox.checked = true;
        var commentValue = document.getElementById(commentId).innerHTML;
        updateOverrideVal('#' + commentId, commentValue, 400, 255, "text");
        if (quantifiable) {
            var quantityValue = document.getElementById(quantityId).innerHTML;
            updateOverrideVal('#' + quantityId, quantityValue, 40, 3, "number");
        }
        document.getElementById(commentId).innerHTML = "";
    }
    else {
        checkbox.checked = false;
        document.getElementById(quantityId).innerHTML = "1";
        document.getElementById(commentId).innerHTML = "";
    }
}

function inlineOverrideEdit(cell, event, columnSize, maxlength, id, quantifiable) {
    preventDefaultAction(event);
    validateInlineEditing(id);
    var value = $('#' + cell.id).html();
    var cellId = '#' + cell.id;
    var columnType = columnSize > 40 ? columnType = 'text' : columnType = 'number';
    if (columnType == 'number' && !quantifiable) {
        return false;
    }
    else {
        updateOverrideVal(cellId, value, columnSize, maxlength, columnType);
    }
}

function validateInlineEditing(id) {
    var checkbox = document.getElementById(id);
    if (!checkbox.checked) {
        checkbox.checked = true;
    }
}

function updateOverrideVal(currentEle, value, columnSize, maxlength, columnType) {
    getBrowserVersion();

    var validations = "";
    if (columnType == 'number') {
        validations = ' min="1" max="99999999999" ';
    }

    if (value.indexOf("<input") >= 0 || value.indexOf("<INPUT") >= 0) {
        return false;
    }
    var maxLength = "";
    if (majorVersion != "8") {
        maxLength = " maxlength='" + maxlength + "' ";
    }

    $(currentEle).html('<input class="inlineVal" style="width: ' + columnSize + 'px' +
        '" value="' + $.trim(value) + '" type="' + columnType + '"' + validations + '' + maxLength + '/>');

    $(".inlineVal").focus();
    $(".inlineVal").keyup(function (event) {
        if (event.keyCode == 13) {
            var inputValue = $(".inlineVal").val();
            if (columnType == 'number') {
                if (inputValue == "" || inputValue <= 0) {
                    $(currentEle).html("1");
                }
                else {
                    $(currentEle).html(parseInt(inputValue.replace(/^0+/, '')));
                }
            }
            else {
                $(currentEle).html(inputValue);
            }
        }
    });
    $(".inlineVal").focusout(function () {
        var inputValue = $(".inlineVal").val();
        if (columnType == 'number') {
            if (inputValue == "" || inputValue <= 0) {
                $(currentEle).html("1");
            }
            else {
                $(currentEle).html(parseInt(inputValue.replace(/^0+/, '')));
            }
        }
        else {
            $(currentEle).html(inputValue);
        }
    });
}

//The below 'commented' sourceURL specifies a name for this piece of code which
//  will appear if debugging in the browser
//This is needed because this file is loaded programatically
//# sourceURL=study_module.js
