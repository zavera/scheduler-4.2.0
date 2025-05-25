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

//lots of globals here
var startDate;
var endDate;
var selectedStudy = null;
var selectedVisit = null;
var selectedStudySubject = null;
var studyPI = null;
var studyName = null;
var studyVisit = null;
var subjectName = null;
var studyVisitTime = null;
var search_string;
var resources;
var search_startDateVal = null;
var search_endDateVal = null;
var search_startTimeVal = null;
var search_endTimeVal = null;
var start;
var s_start;
var end;
var inpatient_search = false;
var request;
var requests = [];
var view;
var appointment_mode;
var eventid = null;
var studydetailname = null;
var subjectdetailname = null;
var visitdetailname = null;
var detailVisitTime = null;
var studyPIdetailname = null;
var startDateDiff = null;

var visitdate = null;
var visituser = null;
var visitreasons = null;
var visitcomments = null;
var minOverrideDate;
var maxOverrideDate;
var appt_selectedStudy = null;
var appt_selectedVisit = null;
var appt_selectedSubjectMrnId = null;
var appt_currentWizardScreen;
var resources;
var selected;
var doubleSharedRoom;
var scheduleComment;
var roomResources;
var overbookRoomSelected;
var overbookRoomSelectedId;
var overrideRoomReasons;
var overrideRoomComment;
var scheduledata = null;
var userdata = null;
var searching_appointments = false;
var double_start = null;
var home = false;
var mrns = "";
var reset = "All";

var HomeAppointment = {};

var Overbook = {};
Overbook.overrideReasons = [];
Overbook.overrideRooms = [];

function getSchedulingOptionsStaticList(callbackFunc) {
    $.get("rest/app/getStaticLists", {}, function (data) {

        parsedData = JSON.parse(data);

        Overbook.overrideReasons = parsedData.overrideReasons;
        checkOutReasons = parsedData.checkOutReasons;
        cancellationReasons = parsedData.cancellationReasons;
        releaseHoldReasonId = getAttributeValueByOtherAttributeValue(cancellationReasons, 'id', 'name', releaseHoldReasonText);
        Overbook.overrideRooms = parsedData.rooms;
        resources = parsedData.resources;
        sublocations = parsedData.sublocations;

        sublocationSelectOptions = buildSelectOptions(sublocations, 'name', 'choose');
        overrideReasonSelectOptions = buildSelectOptions(Overbook.overrideReasons, 'name', 'choose');
        checkOutReasonSelectOptions = buildSelectOptions(checkOutReasons, 'name', 'choose');
        cancellationReasonSelectOptions = buildSelectOptions(cancellationReasons, 'name', 'choose');
        roomSelectOptions = buildSelectOptions(Overbook.overrideRooms, 'name', 'assign me a random room');
        resourcesSelectOptions = buildSelectOptions(resources, 'name', 'choose');

        $("#checkoutReason").html(checkOutReasonSelectOptions);
        $("#cancellationReasons").html(cancellationReasonSelectOptions);
        $("#wizard_cancellationReasons").html(cancellationReasonSelectOptions);
        $("#wizard_checkoutReasons").html(checkOutReasonSelectOptions);
        $("#wizard_deleteOverrideReasons").html(overrideReasonSelectOptions);
        $("#wizard_editOverrideReasons").html(overrideReasonSelectOptions);
        $("#wizard_addOverrideReasons").html(overrideReasonSelectOptions);
        $("#wizard_overrideResources").html(resourcesSelectOptions);
        $("#wizard_overbookReasons").html(overrideReasonSelectOptions);
        $("#wizard_rescheduleReasons").html(overrideReasonSelectOptions);
        $("#wizard_overbookReasonsRoom").html(overrideReasonSelectOptions);
        $("#appointment_allrooms").html(roomSelectOptions);

        if (callbackFunc) {
            callbackFunc();
        }
    });
}

function buildSubLocTabs(prefix) {
    var html = ["<ul>"];

    var htmlDivs = [];
    for (var i = 0; i < sublocations.length; i++) {
        html[html.length] = " <li class='sublocationLocItem'" + "data-sublocation-label='" + i + "'> <a href='#" + prefix + "_" + i + "'>";
        html[html.length] =
            html[html.length] = sublocations[i].name + '</a></li>';

        htmlDivs[htmlDivs.length] = "<div id='" + prefix + "_" + i + "'></div>";

    }

    html[html.length] = "<li class='sublocationLocItem' data-sublocation-label='" + reset + "'> <a href='#" + prefix + "'> All Sub-Locations </a> </li>";

    html[html.length] = "</ul>";

    htmlDivs[htmlDivs.length] = "<div id='" + prefix + "'></div>";
    html = html.concat(htmlDivs);

    return {
        html: html.join(''),
        defaultTabIndex: sublocations.length
    };
}

function getCalendarBySublocation(sublocation) {
    if (sublocation == 'All') {
        sublocationFilterName = '';
    }
    else {
        sublocationFilterName = sublocations[sublocation].name;
    }

    view = home_calendar.view;

    if (view.name === "month") {
        todaysVisits = false;
        searching_appointments = false;
    }
    else {
        todaysVisits = true;
    }

    var sublocationStart = view.activeStart.valueOf();
    var sublocationEnd = view.activeEnd.valueOf();
    var url = "rest/appointment/getCalendarBookedVisits?filterId=&filterString=" + '&sublocation=' + sublocationFilterName + '&starting=' + sublocationStart + '&ending=' + sublocationEnd + '&todayVisits=' + todaysVisits;
    $.ajax({
        type: 'GET',
        url: url,
        data: "",
        success: function (data) {
            resetCalendarEvents(home_calendar, data.visits);
            $('#initial_loading').css({display: 'none'});
        },
        error: function (xhr, status, error) {
            // TODO: check status && error
        },
        dataType: 'json',
        async: true
    });
}

// Used in HTML code generated by javascript
function editBookedResourceOverride(bookedResourceId, bookedVisitId) {
    mode = "override_edit";
    openOverrideDialog(bookedResourceId, mode);
}

// Used in HTML code generated by javascript
function deleteBookedResourceOverride(id) {
    $.getJSON("rest/appointment/getBookedResourceData?id=" + id, function (data) {
        app_selectedBookedResource = data;
        var jsonData = JSON.stringify({id: app_selectedBookedResource.id});
        $.post("rest/appointment/logDeleteResourceOverride", {data: jsonData}, function (data) {
        });
        $('#wizard_deleteOverrideReasons').val('');
        $("#appt_bookedResource_delete_dialog").dialog({modal: true, height: 460, width: 750});
    });
}

function getResourceLists() {
    $.post("rest/appointment/getResourcesList", function (data) {
        parsedData = JSON.parse(data);
        resourcesList = parsedData.resourcesList;
        resourcesSelectOptions = buildResourcesSelectsOptions(resourcesList, 'id', 'choose');
    });
}

function buildResourcesSelectsOptions(list, optionTextProperty, choiceLabel) {
    var html = [];
    html[html.length] = "<option value=''>" + choiceLabel + "</option>";
    for (var i = 0, len = resourcesList.length; i < len; i++) {
        html[html.length] = "<option value='";
        html[html.length] = resourcesList[i].id;
        html[html.length] = "'>";
        html[html.length] = resourcesList[i].name;
        html[html.length] = "</option>";
    }
    return html.join('');
}

function saveComment(element, commentCounter) {
    var wizardcomment = $.trim(WidgetUtil.commentBox.getValue(element));
    if (wizardcomment == null || search_string == '') {
        util_showMainMessage("Please Enter the Comment Value.");
        return;
    }
    jsonData = JSON.stringify({
        id: eventid,
        comment: wizardcomment});

    $.post("rest/appointment/saveComment", {data: jsonData}, function (data) {
        parsedData = JSON.parse(data);
        util_showMainMessage("The comment has been saved.");
        wizardcomment = null;

        WidgetUtil.commentBox.clearValue(element);

        var currentValue = WidgetUtil.counterDisplay.getValue(commentCounter);
        WidgetUtil.counterDisplay.setValue(commentCounter, parseInt(currentValue) + 1);
        searching_appointments = false;
    });
}

function cancelAppointment() {
    $.blockUI();
    var wizardcomment = $.trim($("#wizard_comment_txtArea").val());
    jsonData = JSON.stringify({
        id: eventid,
        appointmentStatusReason: $.trim($('#wizard_cancellationReasons').val()),
        comment: wizardcomment
    });
    $("#appt_wizard_scheduled_visit_details").dialog("close");
    window.location.hash = '';
    todaysVisits = false;

    var view = home_calendar.view;
    if (view.name === "agendaDay") {
        todaysVisits = true;
    }

    $.post("rest/appointment/cancelVisit?todaysVisits=" + todaysVisits, {data: jsonData}, function (data) {
        $.unblockUI();
        sessionStorage.setItem("is_navigated", false);
        sessionStorage.setItem("eventid", "");
        wizardcomment = null;
        searching_appointments = false;
        parsedData = JSON.parse(data);

        AppointmentSearchForm.restart();
        util_showMainMessage("The visit has been Cancelled", resetCalendarCallBack(new Date(parsedData.start)));
    });
}

function reRenderEvent(parsedData, message, messageCallback) {
    resetCalendar();
    util_showMainMessage(message, messageCallback);
}


function checkoutAppointment() {
    $.blockUI();
    var activities;
    var activities_yes = document.getElementById("check_visit_activities_yes").checked;
    var activities_no = document.getElementById("check_visit_activities_no").checked;

    if (activities_yes === true) {
        activities = true;
    } else if (activities_no === true) {
        activities = false;
    }

    var varyVisitDuration;
    var varyVisitDuration_yes = document.getElementById("check_visit_duration_yes").checked;
    var varyVisitDuration_no = document.getElementById("check_visit_duration_no").checked;
    if (varyVisitDuration_yes === true) {
        varyVisitDuration = true;
    } else if (varyVisitDuration_no === true) {
        varyVisitDuration = false;
    }

    var checkOutComment = $.trim($("#checkoutWizard_comment_txtArea").val());
    todaysVisits = false;
    var view = home_calendar.view;
    if (view.name === "agendaDay") {
        todaysVisits = true;
    }

    jsonData = JSON.stringify({
        id: eventid,
        appointmentStatusReason: $.trim($('#wizard_checkoutReasons').val()),
        ommittedActivities: activities,
        varyDuration: varyVisitDuration,
        comment: checkOutComment
    });
    $("#appt_wizard_checkedin_visit_details").dialog("close");

    $.post("rest/appointment/checkoutVisit?todaysVisits=" + todaysVisits, {data: jsonData}, function (data) {
        sessionStorage.setItem("is_navigated", false);
        sessionStorage.setItem("eventid", "");
        $.unblockUI();
        checkOutComment = null;
        searching_appointments = false;
        resetCalendar();
        AppointmentSearchForm.restart();
        util_showMainMessage("The visit has been Checked Out");
    });
}
function appt_rescheduleAppointment(fromTemplate) {
    overbook_startdateVal = $('#reschedule_start_date').val();
    if (overbook_startdateVal == null || overbook_startdateVal < new Date()) {
        var confirmationMessage = "Please select valid Date and Time.";
        util_showMainMessage(confirmationMessage);
        return;
    }

    overbook_starttimeVal = $("#reschedule_start_time").timepicker().val();

    var overbook_startdatetimeVal = overbook_startdateVal + ' ' + overbook_starttimeVal;
    overbook_startdateVal = new Date(overbook_startdatetimeVal);

    var overrideReasons = $('#wizard_rescheduleReasons').val();

    if (overrideReasons == null || overrideReasons === "") {
        var confirmationMessage = "Please select an Override Reason.";
        util_showMainMessage(confirmationMessage);
        return;
    }

    $("#overbook_allResources").val(true);

    var dayView = AppointmentCalendar.isDayView();
    var searchurl = 'rest/appointment/rescheduleAppointment?eventid=' + eventid +
        '&followOriginalTemplate=' + fromTemplate +
        '&startDate=' + overbook_startdateVal.valueOf() +
        '&overrideReason=' + overrideReasons +
        '&homeScreen=' + dayView;

    var starts = null;
    var ends = null;
    var yourDate = overbook_startdateVal;

    start = null;
    view = null;
    $('#initial_loading').css({display: 'block'});
    calendar_rendered = true;
    starts = new Date(yourDate.getFullYear(), yourDate.getMonth(), 1);
    ends = new Date(yourDate.getFullYear(), yourDate.getMonth() + 1, 0, 23, 59, 59, 59);
    $.ajax({
        type: 'POST',
        url: searchurl,
        data: "",
        contentType: "application/x-www-form-urlencoded",
        success: function (data) {
            resetCalendar(new Date($('#reschedule_start_date').val()));
        },
        error: function (xhr, status, error) {
            // TODO: check status && error
        },
        complete: function () {
            $('#loading').css({display: 'none'});
            $('#filter_loading').css({display: 'none'});
            AppointmentSearchForm.restart();
            DialogsUtil.showMessageDialog("Visit Rescheduled");
        },
        dataType: 'json',
        async: false
    });
    searching_appointments = false;
    $("#appt_reschedule_dialog").dialog("close");
    $("#overbook_allResources").val(true);
    $("#overbookWizard_comment").val('');
    $('#wizard_rescheduleReasons').val('');
    $('#filter_loading').css({display: 'none'});
    $("#appt_wizard_cancelled_visit_details").dialog("close");
    $.unblockUI();
}

///////////// open appointment window functions ///////////////

HomeAppointment.openHoldOrScheduledAppointmentWindow = function (data, templateData,
                                                                 baseTemplate) {
    templateData.showApptAndUserDetailByThemselves = true;
    var dialogTitle;
    var visitSummaryHeaderText;
    var wizardTableSwitchSubjectHeaderText;
    if (data.subjectMrn !== undefined && isValidMrn(data.subjectMrn)) {
        dialogTitle = "Check-In/Cancel Appointment";
        visitSummaryHeaderText = "Scheduled Visit Summary";
        wizardTableSwitchSubjectHeaderText = "Remove or Switch Subject";
        templateData.switchSubjectButtonText = "Remove or Switch Subject";
        $('#wizardSetSubjectNA').css({display: 'block'});
        $('#appt_wizard_checkin').css({display: 'inline'});
        $('#wizard_cancellationReasons').val('');
        $('#cancellationDropDown').show();
        $('#appt_wizard_cancelAppointment').css({display: 'inline'});
        sessionStorage.setItem("assignSubjectMode", 'removeOrSwitch');
    }
    else {
        dialogTitle = "Change/Cancel Appointment";
        visitSummaryHeaderText = "Visit Summary";
        wizardTableSwitchSubjectHeaderText = "Assign Subject";
        templateData.switchSubjectButtonText = "Assign Subject";
        $('#wizardSetSubjectNA').css({display: 'none'});
        $('#appt_wizard_checkin').css({display: 'none'});
        if (UserRoleUtil.userIsCrcStaff()) {
            $('#wizard_cancellationReasons').val(releaseHoldReasonId);
            $('#cancellationDropDown').show();
            $('#appt_wizard_cancelAppointment').css({display: 'inline'});
        }
        else {
            $('#wizard_cancellationReasons').val('');
            $('#cancellationDropDown').hide();
            $('#appt_wizard_cancelAppointment').css({display: 'none'});
        }
        sessionStorage.setItem("assignSubjectMode", 'assign');
    }
    var tableContent = baseTemplate.render(templateData);
    $("#visitSummaryHeader").html(visitSummaryHeaderText);
    $('#switchSubject_div').css({display: 'none'});
    $('#appt_wizard_scheduled_visit_details-close').css({display: 'inline'});
    $('#wizardTableSwitchSubjectHeader').html(wizardTableSwitchSubjectHeaderText);
    // reset the visibility and state of various parts of the wizard
    // TODO-XH : abstract this out into some kind of 'reset' method
    $('#switchSubject_chooseSubject').css({display: 'inline'});
    $('#switchSubject_noSubject').prop('checked', false);
    $('#scheduledResourcesBlock').show();

    // check role for 'checkin' button
    if (user.institutionRole.id === GENERAL_VIEW) {
        ApptCalendar.preventComment();

        $("#viewScheduledCommentLink").css({display: "none"});
        $("#appt_wizard_cancelAppointment").css({display: "none"});
        $("#appt_wizard_checkin").css({display: 'none'});
    }
    else if (user.institutionRole.id === STUDY_STAFF) {
        $("#appt_wizard_checkin").css({display: 'none'});
    }
    else {
        $('#appt_wizard_saveComment').css({display: 'inline'});
    }

    $("#appt_wizard_scheduled_visit_details table tbody.forTemplate").html(tableContent);
    $("#appt_wizard_scheduled_visit_details").dialog({
        modal: true,
        close: resetCloseDialogFunction,
        height: 875,
        width: 930,
        title: dialogTitle,
        dialogClass: "scheduledEventDialog"
    });

    setLocationHash('ScheduledVisit');

    return "viewScheduledCommentLink";
};

HomeAppointment.openCheckedInAppointmentWindow = function (data, templateData,
                                                           baseTemplate) {
    templateData.changingInfo = visitdate + (visituser ? (" " + visituser) : "");
    templateData.changingInfoTitle = "Check-In";

    var tableContent = baseTemplate.render(templateData);

    var extraRow = loadAndRenderTemplate("#apptDetails_extraRow", templateData);

    tableContent += extraRow;

    $("#appt_wizard_checkedin_visit_details table tbody.forTemplate").html(tableContent);


    $("#wizard_checkoutReasons").html(checkOutReasonSelectOptions);
    document.getElementById("check_visit_activities_no").checked = false;
    document.getElementById("check_visit_activities_yes").checked = false;
    document.getElementById("check_visit_duration_yes").checked = false;
    document.getElementById("check_visit_duration_no").checked = false;
    $("#appt_wizard_checkedin_visit_details").dialog({
        modal: true,
        close: resetCloseDialogFunction,
        height: 875,
        width: 900,
        dialogClass: "scheduledEventDialog"
    });

    return "viewCheckedInCommentLink";
};
HomeAppointment.openCheckedOutAppointmentWindow = function (data, templateData,
                                                            baseTemplate) {
    templateData.changingInfo = visitdate + (visituser ? (" " + visituser) : "");
    templateData.changingInfoTitle = "Checked Out";

    var tableContent = baseTemplate.render(templateData);

    var extraRow = loadAndRenderTemplate("#apptDetails_extraRow", templateData);

    tableContent += extraRow;

    $("#appt_wizard_checkedout_visit_details table tbody.forTemplate").html(tableContent);

    document.getElementById("check_visit_activities_yes_readonly").checked = omitted_activities;
    document.getElementById("check_visit_activities_no_readonly").checked = !omitted_activities;

    document.getElementById("check_visit_duration_yes_readonly").checked = vary_duration;
    document.getElementById("check_visit_duration_no_readonly").checked = !vary_duration;

    $("#appt_wizard_checkedout_visit_details").dialog({
        modal: true,
        close: resetCloseDialogFunction,
        height: 875,
        width: 900,
        dialogClass: "scheduledEventDialog"
    });

    return "viewCheckedoutCommentLink";
};

HomeAppointment.openCancelledAppointmentWindow = function (data, templateData,
                                                           baseTemplate) {

    var tableContent = baseTemplate.render(templateData);

    templateData.cancelledDate = visitdate + (visituser ? (" " + visituser) : "");
    templateData.cancellationReason = visitreasons;

    var cancelRows = loadAndRenderTemplate("#apptDetails_cancelRows", templateData);
    tableContent += cancelRows;

    var extraRow = loadAndRenderTemplate("#apptDetails_extraRow", templateData);

    tableContent += extraRow;

    $("#appt_wizard_cancelled_visit_details table tbody.forTemplate").html(tableContent);

    $("#appt_wizard_cancelled_visit_details").dialog({
        modal: true,
        close: resetCloseDialogFunction,
        height: 875,
        width: 900,
        dialogClass: "scheduledEventDialog"
    });

    return "viewCancelledCommentLink";
};

HomeAppointment.assembleTemplateData = function (data, actionName) {
    return {
        studyName: studydetailname,
        localId: data.localId,
        piName: studyPIdetailname,
        visitName: visitdetailname,
        subjectName: subjectdetailname,
        apptDetail: scheduledata,
        actionName: actionName,
        userDataDetail: userdata,
        subjectMrn: data.subjectMrn,
        showApptAndUserDetailByThemselves: false,
        showSwitchSubjectLink: UserRoleUtil.userIsCrcStaff(),
        activityLog: data.activityLog
    };
};

HomeAppointment.openAppointmentWindowCallback = function (data, className) {
    className = sanitizeAndSaveClassName(className, true);

    setGlobalVars(data);

    var actionName = data.actionName ? data.actionName : "Scheduled on:";

    var templateData = HomeAppointment.assembleTemplateData(data, actionName);

    var baseTemplate = $.templates("#apptDetails_base");

    $('.dialog_study_id').text(studyId);
    $('.dialog_visit_id').text(visitId);

    var viewCommentsButton;
    switch (className) {
        case "Scheduled":
        case "Hold":
            viewCommentsButton =
                HomeAppointment.openHoldOrScheduledAppointmentWindow(
                    data, templateData, baseTemplate);
            break;
        case "Checked-In":
            viewCommentsButton =
                HomeAppointment.openCheckedInAppointmentWindow(
                    data, templateData, baseTemplate);
            break;
        case "Checked-Out":
            viewCommentsButton =
                HomeAppointment.openCheckedOutAppointmentWindow(
                    data, templateData, baseTemplate);
            break;
        case "Cancellation":
            viewCommentsButton =
                HomeAppointment.openCancelledAppointmentWindow(
                    data, templateData, baseTemplate);
            break;
    }
    if (user.institutionRole.id !== GENERAL_VIEW && viewCommentsButton !== undefined) {
        WidgetUtil.counterDisplay.create($("#" + viewCommentsButton), data.visitCommentsTotal, {
            wrapperClass: "inline-block",
            backgroundColor: "#083e60"
        });
    }
};

function openBookedResourcesAppointmentWindow(className) {

    $.getJSON("rest/appointment/getScheduledVisitDetails?id=" + eventid,
        function(data) { HomeAppointment.openAppointmentWindowCallback(data, className); });
}

///////////// (end) open appointment window functions ///////////////

function setGlobalVars(data) {
    studyId = data.studyId;
    visitId = data.visitId;
    studydetailname = data.studydetailname;
    subjectdetailname = data.subjectdetailname;
    visitdetailname = data.visitdetailname;
    detailVisitTime = data.detailVisitTime;
    studyPIdetailname = data.studyPIdetailname;
    visitdate = data.visitdate;
    visituser = data.visituser;
    visitreasons = data.visitreasons;
    visitcomments = data.visitcomments;
    eventVisitType = data.visitTypeId;
    relativeTime = data.relativeTime;
    scheduledata = data.scheduledata;
    userdata = data.userdata;
    omitted_activities = data.check_visit_activities_readonly;
    vary_duration = data.check_visit_duration_readonly;
}

function resetCloseDialogFunction() {
    sessionStorage.setItem("is_navigated", false);
    sessionStorage.setItem("eventid", "");
}

function setParametersCalendarEventClick(eventid, start, end) {
    home = false;
    appt_search_view = false;
    sessionStorage.setItem("selected_start", start);
    sessionStorage.setItem("selected_end", end);
    sessionStorage.setItem("eventid", eventid);
    resetUI();
    loadBookedResourcesData(currentPage, eventid);
}

function closeApptDialog() {
    if (user != null) {
        if (user.institutionRole.id == STUDY_STAFF || user.institutionRole.id == FRONT_DESK || user.institutionRole.id == GENERAL_VIEW) {
            closeDialogWindows();
        }
        else {
            var isNavigation = sessionStorage.getItem("is_navigated");
            if (isNavigation == true) {
                return;
            }
            else if (isNavigation == false) {
                closeDialogWindows();
            }
        }
        ;
    }
}

function closeDialogWindows() {
    if ($("#appt_wizard_scheduled_visit_details").hasClass('ui-dialog-content')) {
        $("#appt_wizard_scheduled_visit_details").dialog("close");
        window.location.hash = '';
    }
    else if ($("#appt_wizard_checkedin_visit_details").hasClass('ui-dialog-content')) {
        $("#appt_wizard_checkedin_visit_details").dialog("close");
    }
    else if ($("#appt_wizard_checkedout_visit_details").hasClass('ui-dialog-content')) {
        $("#appt_wizard_checkedout_visit_details").dialog("close");
    }
    else if ($("#appt_wizard_cancelled_visit_details").hasClass('ui-dialog-content')) {
        $("#appt_wizard_cancelled_visit_details").dialog("close");
    }
    sessionStorage.setItem("is_navigated", false);
    sessionStorage.setItem("eventid", "");
}

function navigateOverrideAction() {
    sessionStorage.setItem("is_navigated", true);
    setTimeout("window.location.href = 'home.html';", 2500);
    setParametersCalendarEventClick(eventid, start, end);
    className = sessionStorage.getItem("className");
    openBookedResourcesAppointmentWindow(className);
}
