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

var Appointments = {};

var eventVisitType;
var relativeTime;
var billableVal;
var appt_list_view;
var omitted_activities;
var vary_duration;
var selectedEndDate = showDate(new Date());
var no_appointments_found;
var appt_selectedStudyName;
var appt_selectedStudyLocalId;
var appt_selectedStudyInvestigatorName;
var appt_selectedVisitName;
var appt_selectedVisitType; // does not seem to get set
var appt_selectedVisitRelative;
var appt_selectedVisitDuration;
var appt_selectedSubjectName;
var appt_selectedMrn;
var appt_selectedSubjectMrnId;
var appt_selectedSubjectMrn;
var appt_selectedSubjectGender;
var selectedStudySubject = null;
var filteredString;
var erroMsgFlag = true;

var startDateVal;
var endDateVal;

function adjustSubjectChoiceVisibility(subjectNaCheckbox, chooseElementHash) {

    if (subjectNaCheckbox != null && chooseElementHash != null) {
        var display = subjectNaCheckbox.checked ? "none" : "block";

        $(chooseElementHash).css({display: display});
    }
}

function closeBookedResourceClick() {
    $("#appt_bookedResource_delete_dialog").dialog("close");
}

function closeCheckOutClick() {
    eventid = null;
    studydetailname = null;
    subjectdetailname = null;
    visitdetailname = null;
    detailVisitTime = null;
    studyPIdetailname = null;
    sessionStorage.setItem("is_navigated", false);
    $("#appt_wizard_checkedin_visit_details").dialog("close");
}

function closeCheckOutDetailsClick() {
    eventid = null;
    studydetailname = null;
    subjectdetailname = null;
    visitdetailname = null;
    detailVisitTime = null;
    studyPIdetailname = null;
    sessionStorage.setItem("is_navigated", false);
    $("#appt_wizard_checkedout_visit_details").dialog("close");
}

function closeCancelDetailsClick() {
    eventid = null;
    studydetailname = null;
    subjectdetailname = null;
    visitdetailname = null;
    detailVisitTime = null;
    studyPIdetailname = null;
    sessionStorage.setItem("is_navigated", false);
    $("#appt_wizard_cancelled_visit_details").dialog("close");
}

function closeScheduleDetailsClick() {
    eventid = null;
    studydetailname = null;
    subjectdetailname = null;
    visitdetailname = null;
    detailVisitTime = null;
    studyPIdetailname = null;
    sessionStorage.setItem("is_navigated", false);
    $("#appt_wizard_scheduled_visit_details").dialog("close");
    setLocationHash('');
}

function changeCancellationReasonsClick() {
    if ($.trim($("#wizard_cancellationReasons").val()) != "") {
        document.getElementById("appt_wizard_checkin").style.display = "none";
    }
    else {
        document.getElementById("appt_wizard_checkin").style.display = "inline";
    }
}

function saveBookedVisitCommentClick() {
    commentDialog(true);
}

function saveBookedVisitCommentClick1() {
    commentDialog(false);
}

function commentDialog(first) {
    var commentElement = $("#checkoutWizard_comment");
    var viewCommentElement =  $("#viewCheckedInCommentLink");

    if (first) {
        commentElement = $("#wizard_comment");
        viewCommentElement = $("#viewScheduledCommentLink");
    }

    var wizardcomment = $.trim(WidgetUtil.commentBox.getValue(commentElement));
    if (wizardcomment == '') {
        return;
    }

    DialogsUtil.showConfirmationDialog("#comment-dialog-confirm", {
        buttons: {
            "Yes": function () {
                saveComment(commentElement, viewCommentElement);
                $(this).dialog("close");
            },
            "No": function () {
                $(this).dialog("close");
            }
        }
    });
}

function cancellationDialog() {
    DialogsUtil.showConfirmationDialog("#cancellation-dialog-confirm", {
        buttons: {
            "Yes": function () {
                $(this).dialog("close");
                cancelAppointment();
            },
            "No": function () {
                $(this).dialog("close");
            }
        }
    });
}

function checkoutDialog() {
    DialogsUtil.showConfirmationDialog("#checkout-dialog-confirm", {
        buttons: {
            "Yes": function () {
                $(this).dialog("close");
                checkoutAppointment();
            },
            "No": function () {
                $(this).dialog("close");
            }
        }
    });
}

function cancelAppointmentClick() {
    if ($.trim($("#wizard_cancellationReasons").val()).length < 1) {
        util_showMainMessage("Please select a Cancellation Reason");
    }
    else {
        cancellationDialog();
    }
}

function checkOutVisitClick() {
    var activities_yes = document.getElementById("check_visit_activities_yes").checked;
    var activities_no = document.getElementById("check_visit_activities_no").checked;
    var varyVisitDuration_yes = document.getElementById("check_visit_duration_yes").checked;
    var varyVisitDuration_no = document.getElementById("check_visit_duration_no").checked;
    if (activities_yes == false && activities_no == false) {
        util_showMainMessage("Please select the required fields.");
        return;
    }

    if (varyVisitDuration_yes == false && varyVisitDuration_no == false) {
        util_showMainMessage("Please select the required fields.");
        return;
    }
    if ($.trim($("#wizard_checkoutReasons").val()).length < 1) {
        util_showMainMessage("Please select a Check-Out Reason");
    }
    else {
        checkoutDialog();
    }
}

function rescheduleAppointmentClick() {
    // by default set the start time to 07:00. I am not sure why but
    // if we do the following *after* creating the timepicker, and the
    // time picker was created in a previous display of the rescheduling
    // dialog, the timepicker will ignore the instruction to set the time
    // to 07:00 and instead will be set to the time that was last entered
    // by the user
    $("#reschedule_start_time").val('07:00');
    $("#reschedule_start_time").timepicker('setTime', '07:00');
    $("#reschedule_start_time").timepicker().val('07:00');

    // destroy the time picker before creating the rescheduling dialog
    // otherwise the time picker may appear momentarily before the
    // dialog is created, if it was created previously for another appointment.
    // An alternative might be to systematically destroy the time picker
    // when the rescheduling dialog gets closed.
    $("#reschedule_start_time").timepicker('destroy');

    // set up the dialog
    $("#booked_visit_radio").prop("checked", true);
    $("#appt_reschedule_dialog").dialog({modal: true, height: 300, width: 850});
    $('#rescheduleWizardButtons input').css({width: "75px"});

    // set up the date picker
    WidgetUtil.createDatepicker("#reschedule_start_date", {
        minDate: "+0M +0D"
    });
    $("#reschedule_start_date").datepicker('setDate', new Date());
    $("#reschedule_start_date").prop("disabled", "disabled");

    // show or hide the start time input filed and time picker, depending on the visit type
    if ((eventVisitType == 4 || eventVisitType == 5 || eventVisitType == 6)
        && (relativeTime == "false")) {

        // hide the start time input field
        $('#reschedule_start_time').css({display: 'none'});

    } else {

        // show the start time input field
        $('#reschedule_start_time').css({display: 'inline'});

        // remove focus so that the time picker is not displayed
        $('#reschedule_start_time').blur();

        // create the time picker
        WidgetUtil.createTimepicker("#reschedule_start_time");

        // capture and stop propagation of key down and key press events
        // that prevents manual editing of the time
        $("#reschedule_start_time").on("keydown", function (event) {
            event.preventDefault();
        });
        $("#reschedule_start_time").on("keypress", function (event) {
            event.preventDefault();
        });

    }

}

function cancelRescheduleAppointmentClick() {
    $('#wizard_rescheduleReasons').val('');
    $("#appt_reschedule_dialog").dialog("close");
}

function crudRescheduleAppointmentClick() {
    $.blockUI();
    var checkedVal = $('input[name=reschedule_appt_selected]:checked').val();
    var rescheduleFrom = true;
    if (checkedVal == 'booked_vis') {
        rescheduleFrom = false;
    }
    appt_rescheduleAppointment(rescheduleFrom);
}

function overbookCheckBatchViewClick() {
    $('#renderOverbook').css({display: "block"});
    resetUI();
    loadOverbookedResourcesData(app_selectedBookedVisit.id);
}

//Check for overbooked Resources from appointment Screen
function overbookCheckClick() {
    resetUI();
    loadOverbookedResourcesData(eventid);
    $("#appt_overbook_check_dialog").dialog({modal: true, height: 460, width: 1000});
}

function closeOverbookCheckClick() {
    $("#appt_overbook_check_dialog").dialog("close");
}

function cancelReasonChangeClick() {
    if ($.trim($("#cancellationReasons").val()) != "") {
        $('#checkin_date').val('');
        $("#checkin_time").timepicker().val('');
        $('#checkinRender').css({display: "none"});
    }
    else {
        if (user.institutionRole.id != STUDY_STAFF) {
            Appointments.renderIfPossibleToShowCheckInOut('checkinRender', "block");
        }
    }
}

function checkoutReasonChangeClick() {
    $("#cancel_date").val('');
    $('#cancel_time').val('');
}

function apptBatchData() {
    appointment_viewStack('appointment_form_screen');
    appointment_setFormMode('view');
    $('#renderOverbook').css({display: "none"});
    $('#checkoutReason').val('');
    $('#cancellationReasons').val('');
}

function switchSubjectFilterSubjectClick() {
    var value = $('#switchSubject_subjectFilterButton').attr("value");
    if (value == 'Filter') {
        if ($.trim($("#switchSubject_subjectFilterText").val()) == '') {
            util_showMainMessage("Please enter a filter value.");
            return;
        }
        filteredString = $.trim($("#switchSubject_subjectFilterText").val());
        $('#switchSubject_subjectFilterButton').attr("value", "Reset");
    } else {
        filteredString = '';
        sessionStorage.setItem("filteredString", filteredString);
        $("#switchSubject_subjectFilterText").val('');
        $('#switchSubject_subjectFilterButton').attr("value", "Filter");
    }
    sessionStorage.setItem("filteredString", filteredString);
    resetUI();
    currentPage = 1;
    initial_load = true;
    ApptSubjectsTable.currentTable.filterString = filteredString;
    ApptSubjectsTable.currentTable.setCurrentPage(currentPage);
    ApptSubjectsTable.currentTable.reloadFn();
}

//////Select Study Subject Screen///////////////////////////////////////
var sortByApptStudySubjectMRN = "mrn";
var sortByApptStudySubjectLastName = "lastName";
var sortByApptStudySubjectFirstName = "firstName";

function loadSwitchSubjectData(currentPage) {
    loadSwitchSubjectTable(currentPage, true, appt_selectedStudy, 'switchSubject_StudySubjectTable');
}

function loadApptSubjectDataIntoSpecificElement(currentPage, element) {
    loadSubjectData(currentPage, true, appt_selectedStudy, element);
}

function createApptCalendarDatePicker() {
    $(".apptDateRangeContainer").css('width', 215);

    WidgetUtil.createDatepicker("#apptCalendarGoToDate", {
        showOn: "both",
        onSelect: function (selectedDate) {
            var gotoDate = new Date(selectedDate);
            home_calendar.gotoDate(gotoDate);
            $("#apptCalendarGoToDate").datepicker("setDate", gotoDate);
        }
    }, true, true);

    $("#apptCalendarGoToDate").datepicker("setDate", new Date());
}

function filterResources(obj) {
    $.blockUI();
    if (obj.value == "All") {
        $('#searchResourcesTable tbody tr').filter(function () {
            return $.trim($(this).find('td').eq(3).find('img').eq(0).attr('alt')) === "Not Available"
                || $.trim($(this).find('td').eq(3).find('img').eq(0).attr('alt')) === "Available"
        }).show();
    }
    else {
        $('#searchResourcesTable tbody tr').filter(function () {
            if (obj.value === "Yes") {
                return $.trim($(this).find('td').eq(3).find('img').eq(0).attr('alt')) === "Available"
            }
            else {
                return $.trim($(this).find('td').eq(3).find('img').eq(0).attr('alt')) === "Not Available"
            }
        }).show();
        $('#searchResourcesTable tbody tr').filter(function () {
            if (obj.value === "Yes") {
                return $.trim($(this).find('td').eq(3).find('img').eq(0).attr('alt')) === "Not Available"
            }
            else {
                return $.trim($(this).find('td').eq(3).find('img').eq(0).attr('alt')) === "Available"
            }
        }).hide();
    }
    $.unblockUI();
}

function showGenderBlockDialogForNewAppointment() {
    $.unblockUI();
    $("#doubleRoom-dialog-confirm").show();

    $("#doubleRoom-dialog-confirm").dialog({
        resizable: true,
        // let height be determined by the content of the div
        width: 500,
        modal: true,
        buttons: {
            "Yes": function () {
                $.blockUI({ignoreIfBlocked: true});

                calendar_rendered = true;
                jsonData = JSON.stringify({
                    study: AppointmentSearchForm.selectedStudy.id,
                    subjectMrnId: AppointmentSearchForm.selectedSubject.subjectMrnId,
                    visit:AppointmentSearchForm.selectedVisit.id,
                    comment: scheduleComment,
                    user: user.id,
                    doubleRoomMessage: doubleSharedRoom
                });
                var isInpatient = MiscUtil.isInpatientVisit(AppointmentSearchForm.selectedVisit.visitTypeId);

                /* late-in-testing bandaid. may be good to dig deeper to make all dates into longs */
                if (double_start.indexOf(' ') > -1) {
                    double_start = new Date(double_start).getTime();
                }
                if (end.indexOf(' ') > -1) {
                    end = new Date(end).getTime();
                }
                var confirmUrl = 'rest/appointment/confirmAppointment?isInpatient=' + isInpatient + '&startDate=' + double_start + '&endDate=' + end;
                $.post(confirmUrl, {data: jsonData}, function (data) {
                    var parsedData = JSON.parse(data);
                    alternateResourceUsed = parsedData.alternateResourceUsed;
                    confirmationStatus = parsedData.confirmationStatus;
                    if (confirmationStatus == "true") {
                        if (alternateResourceUsed != null) {
                            alert(alternateResourceUsed);
                        }
                        util_showMainMessage("The appointment has been Scheduled");
                        scheduleComment = null;
                        searching_appointments = false;
                        $("#appt_wizard_searchedDetails").dialog("close");
                    }
                    else {
                        alert("Sorry, one or more of the resources for this appointment became unavailable during the confirmation process. Please try your search again and look for alternate times.");
                        scheduleComment = null;
                        searching_appointments = false;
                        $("#appt_wizard_searchedDetails").dialog("close");
                    }
                    util_showMainMessage("The appointment has been Scheduled");
                    scheduleComment = null;
                    doubleSharedRoom = null;
                    var starts = null;
                    var ends = null;
                    var yourDate = new Date();
                    $("#appt_wizard_searchedDetails").dialog("close");
                    start = null;
                    view = null;
                    $('#initial_loading').css({display: 'block'});
                    calendar_rendered = true;
                    starts = new Date(yourDate.getFullYear(), yourDate.getMonth() - 1, 1);
                    ends = new Date(yourDate.getFullYear(), yourDate.getMonth() + 2, 0, 23, 59, 59, 59);
                    var isNotMonthView = !AppointmentCalendar.isMonthView();
                    var url = "rest/appointment/getCalendarBookedVisits?starting=" + starts.getTime() + "&ending=" + ends.getTime() + "&todayVisits=" + isNotMonthView + "&filterString=&filterId=";
                    searching_appointments = false;
                    start = double_start;
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
                        async: false
                    });
                    if (start != null) {
                        home_calendar.gotoDate(new Date(start));
                    }
                    else {
                        home_calendar.gotoDate(new Date());
                    }
                    home_calendar.rerenderEvents();
                    return;
                });
                $(this).dialog("close");
                $("#doubleRoom-dialog-confirm").hide();
                $("#appt_wizard_searchedDetails").dialog("close");
            },
            "No": function () {
                $(this).dialog("close");
                $("#doubleRoom-dialog-confirm").hide();
            }
        }
    });
}


function showConfirmationDialogForSwitchedSubject(subjectMrnId, genderBlockFlag, doubleBookingFlag) {

    var genderBlockWarning = "<p>There is already a patient of a different or undetermined gender booked into the room you are trying to schedule.</p>";
    var doubleBookedWarningPrefixAlone = "<p>PLEASE NOTE: T";
    var doublebookedWarningPrefixNotAlone = "<p>PLEASE NOTE ALSO: t";
    var doubleBookedWarning = "his subject is scheduled for another visit that will conflict with your desired time. Do you wish to continue? If you choose to schedule, please follow the proper notification and operational procedures at your site.</p>";

    var messageHtml = "";

    if (genderBlockFlag) {
        messageHtml += genderBlockWarning;
    }

    if (doubleBookingFlag) {
        if (genderBlockFlag) {
            messageHtml += doublebookedWarningPrefixNotAlone + doubleBookedWarning;
        }
        else {
            messageHtml += doubleBookedWarningPrefixAlone + doubleBookedWarning;
        }
    }

    $('#switch-subject-dialog-confirm-message').html(messageHtml);

    $("#switch-subject-dialog-confirm").dialog({
        resizable: true,
        // let height be determined by the content of the div
        width: 500,
        modal: true,
        buttons: {
            "Yes": function () {
                calendar_rendered = true;
                $(this).dialog("close");
                $("#doubleRoom-dialog-confirm").hide();

                switchSubject(
                    true,
                    subjectMrnId,
                    eventid,
                    handleConfirmedSwitchedSubject,
                    handleCouldNotConfirmSwitchSubject,
                    handleAjaxError
                );
            },
            "No": function () {
                // if the user clicks 'yes', then resetToResourceTableView
                // is called in the handleConfirmedSwitchedSubject,
                // which is left there because when a confirmation dialog is NOT Needed
                // then that method is called directly.
                resetToResourceTableView();
                $(this).dialog("close");
                $("#doubleRoom-dialog-confirm").hide();
            }
        }
    });
}

function showConfirmationDialogForDoubleBookingSubject(continuation) {

    var subjectAvailUrl = 'rest/appointment/isSubjectAvailable?' +
        'subjectMrnId=' + AppointmentSearchForm.selectedSubject.subjectMrnId +
        '&startDate=' + Date.parse(start) +
        '&endDate=' + Date.parse(end);
    $.post(subjectAvailUrl, {}, function (data) {

        if (data === 'no') {
            $("#double-booked-subject-dialog-confirm").dialog({
                resizable: true,
                // let height be determined by the content of the div
                width: 500,
                modal: true,
                buttons: {
                    "Yes": function () {
                        calendar_rendered = true;
                        $(this).dialog("close");
                    },
                    "No": function () {
                        $("#appt_wizard_searchedDetails").dialog("close");
                        $(this).dialog("close");
                    }
                }
            });
        }
    });
}

function apptStopSearchClick() {
    request.abort();
    if (requests.length > 0) {
        for (var i = 0; i < requests.length; i++) {
            requests[i].abort();
        }
    }
    $('#loading').css({display: 'none'});
    $('#filter_loading').css({display: 'none'});
}

function loadTimelineTab() {
    window.location.hash = "resourceTimeline";
    $("#appt_timeline").load("appt_timeline.html", loadTimeline);
}

function getBookedVisitDetails(id) {
    selectedPage = $('.input_page').val();
    sessionStorage.setItem("apptListSelectedPage", selectedPage);
    sessionStorage.setItem("apptListSelectedMaxResults", max_results);
    $.getJSON("rest/appointment/getBookedVisitData?bookedVisit=" + id, function (data) {
        sessionStorage.setItem("bookedVisit", JSON.stringify(data));
        sessionStorage.setItem("apptListSelectedDate", showDate(data.scheduledStartTime));
        appointmentBatchModule();
    });
}

function setClassNameBasedOnAppointmentStatus() {
    var className;

    switch (app_selectedBookedVisit.appointmentStatus) {
        case 'Scheduled':
            className = "statusBlock statusScheduled";
            break;
        case 'Checked-In':
            className = "statusBlock statusCheckedIn";
            break;
        case 'Checked-Out':
            className = "statusBlock statusCheckedOut";
            break;
        case 'Cancellation':
            className = "statusBlock statusCancelled";
            break;
        case 'Hold':
            className = "statusBlock statusHold";
            break;
        default:
            className = "";
    }

    document.getElementById("apptStatusClass").className = className;
}

Appointments.renderIfPossibleToShowCheckInOut = function(elementId, display) {
    if (Appointments.showCheckinAndCheckout) {
        $('#' + elementId).css({display: display});
    }
};

function appointment_setFormMode(mode) {
    getBrowserVersion();
    appointment_mode = mode;
    $('.formElementRequired').css({visibility: "hidden"});
    $('.formValidationRequired').css({visibility: "hidden"});
    $('#renderSave').css({display: "none"});
    $('#renderOverbook').css({display: "none"});
    $('#renderCancel').css({display: "block"});
    $('#checkinRender').css({display: "none"});
    $('#checkoutRender').css({display: "none"});
    $('#cancelRender').css({display: "none"});
    $('#holdRender').css({display: "none"});

    search_createWidgets();
    app_selectedBookedVisit = JSON.parse(sessionStorage.getItem("bookedVisit"));

    Appointments.showCheckinAndCheckout = ! (
        app_selectedBookedVisit.isCancelled
        && app_selectedBookedVisit.subjectMRN === "N/A"
    );
    if (mode == 'view') {
        $('#bookedVisitStudy_name').text(app_selectedBookedVisit.studyName);
        $('#bookedVisitDuration').text(app_selectedBookedVisit.visitDuration);
        $('#bookedVisitStatus').text(app_selectedBookedVisit.appointmentStatus);

        setClassNameBasedOnAppointmentStatus();

        var startDate = app_selectedBookedVisit.scheduledStartTime;
        $('#bookedVisitStartTime').text(showDateTime(startDate));

        var endDate = app_selectedBookedVisit.scheduledEndTime;
        $('#bookedVisitEndTime').text(showDateTime(endDate));

        var checkinDate = app_selectedBookedVisit.checkInDate;
        $('#bookedVisitCheckInTime').text(showDateTime(checkinDate));

        var checkoutDate = app_selectedBookedVisit.checkOutDate;
        $('#bookedVisitCheckOutTime').text(showDateTime(checkoutDate));
        $('#bookedVisitCheckOutReasonName').text(app_selectedBookedVisit.checkOutReason);

        var cancelDate = app_selectedBookedVisit.cancelDate;
        $('#bookedVisitCancelTime').text(showDateTime(cancelDate));
        $('#bookedVisitCancelReasonName').text(app_selectedBookedVisit.cancelReason);

        $('#bookedVisitStudyLocalID').text(app_selectedBookedVisit.localId);
        $('#bookedVisitSubjectFullName').text(app_selectedBookedVisit.subjectName);
        $('#bookedVisitStudyCatID').text(app_selectedBookedVisit.catID);
        $('#bookedVisitSubjectMRN').text(app_selectedBookedVisit.subjectMRN);
        $('#bookedVisitStudyIRB').text(app_selectedBookedVisit.irb);

        $('#bookedVisitSubjectSex').text(app_selectedBookedVisit.genderName);
        $('#bookedVisitName').text(app_selectedBookedVisit.visitName);
        $('#bookedVisitSubjectDob').text(app_selectedBookedVisit.birthDate);
        $('#bookedVisitComment').text(app_selectedBookedVisit.comment);
        $('#bookedVisitType').text(app_selectedBookedVisit.visitTypeName);

        appt_list_view = true;
        eventid = app_selectedBookedVisit.id;
        initial_load = true;
        resetUI();
        loadBookedResourcesData(currentPage, eventid);

        $('#checkinTable').css({display: "inline-block"});

        if (app_selectedBookedVisit.isScheduled) {
            if (user.institutionRole.id == GENERAL_VIEW) {
                $('#renderCancel').css({display: "block"});
                $('#checkinRender').css({display: "none"});
                $('#checkoutRender').css({display: "none"});
                $('#cancelRender').css({display: "none"});
            }
            else if (user.institutionRole.id == STUDY_STAFF) {
                $('#checkinRender').css({display: "none"});
                $('#checkoutRender').css({display: "none"});
                $('#checkout_date').hide();
                $('#checkout_time').hide();
                $('#cancelRender').css({display: "block"});
                $('#cancel_date_wrapper').hide();
                $('#cancel_time').hide();
                $('#renderSave').css({display: "block"});
                $('#renderCancel').css({display: "none"});
                $('#holdRender').css({display: "none"});
            }
            else {
                Appointments.renderIfPossibleToShowCheckInOut('checkinRender', "block");
                Appointments.renderIfPossibleToShowCheckInOut('checkoutRender', "block");

                $('#cancelRender').css({display: "block"});
                $('#renderSave').css({display: "block"});
                $('#renderCancel').css({display: "none"});
                $('#holdRender').css({display: "none"});
            }
        }
        else if (app_selectedBookedVisit.isCheckedIn) {
            $('#holdRender').css({display: "none"});

            if (user.institutionRole.id == GENERAL_VIEW || user.institutionRole.id == STUDY_STAFF) {
                $('#checkinRender').css({display: "none"});
                $('#checkoutRender').css({display: "none"});
                $('#cancelRender').css({display: "none"});
                $('#renderSave').css({display: "none"});
                $('#renderCancel').css({display: "block"});
            }
            else if (user.institutionRole.id == SUPER_ADMIN || user.institutionRole.id == SCHEDULER || user.institutionRole.id == RESOURCE_MANAGER) {
                Appointments.renderIfPossibleToShowCheckInOut('checkinRender', "block");
                Appointments.renderIfPossibleToShowCheckInOut('checkoutRender', "block");
                $('#renderSave').css({display: "block"});
                $('#cancelRender').css({display: "block"});
                $('#renderCancel').css({display: "none"});
            }
            else if (user.institutionRole.id == FRONT_DESK) {
                $('#checkinRender').css({display: "none"});
                $('#checkoutRender').css({display: "block"});
                $('#renderSave').css({display: "block"});
                $('#cancelRender').css({display: "none"});
                $('#renderCancel').css({display: "none"});
            }
        }
        else if (app_selectedBookedVisit.isCheckedOut) {
            $('#holdRender').css({display: "none"});
            if (user.institutionRole.id != GENERAL_VIEW && user.institutionRole.id != STUDY_STAFF && user.institutionRole.id != FRONT_DESK) {
                Appointments.renderIfPossibleToShowCheckInOut('checkinRender', "block");
                Appointments.renderIfPossibleToShowCheckInOut('checkoutRender', "block");
                $('#cancelRender').css({display: "block"});
                $('#renderSave').css({display: "block"});
                $('#renderCancel').css({display: "none"});
            }
            else {
                $('#checkinRender').css({display: "none"});
                $('#checkoutRender').css({display: "none"});
                $('#cancelRender').css({display: "none"});
                $('#renderSave').css({display: "none"});
                $('#renderCancel').css({display: "block"});
            }
        }
        else if (app_selectedBookedVisit.isCancelled) {
            $('#holdRender').css({display: "none"});
            if (user.institutionRole.id != GENERAL_VIEW && user.institutionRole.id != STUDY_STAFF && user.institutionRole.id != FRONT_DESK) {
                Appointments.renderIfPossibleToShowCheckInOut('checkinRender', "block");
                Appointments.renderIfPossibleToShowCheckInOut('checkoutRender', "block");
                $('#cancelRender').css({display: "block"});
                $('#renderSave').css({display: "block"});
                $('#renderCancel').css({display: "none"});
            }
            else {
                $('#checkinRender').css({display: "none"});
                $('#checkoutRender').css({display: "none"});
                $('#cancelRender').css({display: "none"});
                $('#renderSave').css({display: "none"});
                $('#renderCancel').css({display: "block"});
            }
        }
        else {
            $('#checkinRender').css({display: "none"});
            $('#checkoutRender').css({display: "none"});
            $('#cancelRender').css({display: "none"});
            $('#renderSave').css({display: "none"});
            $('#checkinTable').css({display: "none"});
            $('#renderCancel').css({display: "block"});
            $('#holdRender').css({display: "block"});
        }
    }
}

function apptCancelBatchListClick() {
    $('#renderOverbook').css({display: "none"});
    appointmentListModule();
}

function apptSubmitBatchListClick() {

    var checkindateVal = $('#checkin_date').val();
    var checkoutdateVal = $('#checkout_date').val();
    var canceldateVal = $('#cancel_date').val();

    var checkintimeVal = $("#checkin_time").timepicker().val();
    if (checkindateVal != "" && checkintimeVal != "") {
        var checkindatetimeVal = checkindateVal + ' ' + checkintimeVal;
        checkindateVal = new Date(checkindatetimeVal);
    } else {
        checkindateVal = null;
    }

    var checkouttimeVal = $("#checkout_time").timepicker().val();
    if (checkoutdateVal != "" && checkouttimeVal != "") {
        var checkoutdatetimeVal = checkoutdateVal + ' ' + checkouttimeVal;
        checkoutdateVal = new Date(checkoutdatetimeVal);
    } else {
        checkoutdateVal = null;
    }

    var canceltimeVal = $("#cancel_time").timepicker().val();
    if (canceldateVal != "" && canceltimeVal != "") {
        var canceldatetimeVal = canceldateVal + ' ' + canceltimeVal;
        canceldateVal = new Date(canceldatetimeVal);
    } else {
        canceldateVal = null;
    }

    var checkoutReasons = $.trim($('#checkoutReason').val());
    var cancellationReasons = $.trim($('#cancellationReasons').val());
    if (app_selectedBookedVisit.isScheduled) {
        if (user.institutionRole.id != STUDY_STAFF) {

            if ($('#checkin_date').val() != "" && $("#checkin_time").timepicker().val() == "" && checkoutReasons == "" && checkoutdateVal == null && cancellationReasons == "" && canceldateVal == null) {
                util_showMainMessage("Please select valid check-in time.");
                return;
            }
            else if ($('#checkin_date').val() != "" && $("#checkin_time").timepicker().val() != "" && checkoutReasons == "" && $('#checkout_date').val() == "" && $("#checkout_time").timepicker().val() != "" && cancellationReasons == "" && canceldateVal == null) {
                util_showMainMessage("Please enter correct check-out data.");
                return;
            }
            else if ($('#checkin_date').val() != "" && $("#checkin_time").timepicker().val() != "" && checkoutReasons == "" && $('#checkout_date').val() != "" && $("#checkout_time").timepicker().val() != "" && cancellationReasons == "" && canceldateVal == null) {
                util_showMainMessage("Please enter correct check-out data.");
                return;
            }
            else if ($('#checkin_date').val() != "" && $("#checkin_time").timepicker().val() != "" && checkoutReasons != "" && $('#checkout_date').val() == "" && $("#checkout_time").timepicker().val() != "" && cancellationReasons == "" && canceldateVal == null) {
                util_showMainMessage("Please enter correct check-out data.");
                return;
            }
            else if ($('#checkin_date').val() != "" && $("#checkin_time").timepicker().val() != "" && checkoutReasons != "" && $('#checkout_date').val() != "" && $("#checkout_time").timepicker().val() == "" && cancellationReasons == "" && canceldateVal == null) {
                util_showMainMessage("Please enter correct check-out data.");
                return;
            }
            else if ($('#checkin_date').val() != "" && $("#checkin_time").timepicker().val() != "" && checkoutReasons != "" && $('#checkout_date').val() == "" && $("#checkout_time").timepicker().val() == "" && cancellationReasons == "" && canceldateVal == null) {
                util_showMainMessage("Please enter correct check-out data.");
                return;
            }
            else if ($('#checkin_date').val() == "" && $("#checkin_time").timepicker().val() != "" && checkoutReasons == "" && checkoutdateVal == null && cancellationReasons == "" && canceldateVal == null) {
                util_showMainMessage("Please select valid check-in date.");
                return;
            }
            else if (checkindateVal != null && checkoutReasons != "" && checkoutdateVal != null && cancellationReasons != "" && canceldateVal != null) {
                util_showMainMessage("The visit can either be checked-in or checked-out or cancelled.");
                return;
            }
            else if (checkindateVal != null && checkoutReasons != "" && checkoutdateVal == null && cancellationReasons != "" && canceldateVal != null) {
                util_showMainMessage("The visit can either be checked-in or checked-out or cancelled.");
                return;
            }
            else if (checkindateVal != null && checkoutReasons == "" && checkoutdateVal != null && cancellationReasons != "" && canceldateVal != null) {
                util_showMainMessage("The visit can either be checked-in or checked-out or cancelled.");
                return;
            }
            else if (checkindateVal != null && checkoutReasons != "" && checkoutdateVal != null && cancellationReasons == "" && canceldateVal != null) {
                util_showMainMessage("The visit can either be checked-in or checked-out or cancelled.");
                return;
            }
            else if (checkindateVal != null && checkoutReasons != "" && checkoutdateVal != null && cancellationReasons != "" && canceldateVal == null) {
                util_showMainMessage("The visit can either be checked-in or checked-out or cancelled.");
                return;
            }
            else if (checkindateVal == null && checkoutReasons != "" && checkoutdateVal == null && cancellationReasons != "" && canceldateVal == null) {
                util_showMainMessage("The visit can either be checked-out or cancelled.");
                return;
            }
            else if (checkindateVal == null && checkoutReasons != "" && checkoutdateVal != null && cancellationReasons != "" && canceldateVal != null) {
                util_showMainMessage("The visit can either be checked-in or checked-out or cancelled.");
                return;
            }
            else if (($('#checkin_date').val() == "" || $("#checkin_time").timepicker().val() == "") &&
                (checkoutReasons != "" ||
                    ($('#checkout_date').val() != null && $('#checkout_date').val() != "")
                    || checkouttimeVal != "")) {
                util_showMainMessage("Please Note: Appointment needs to be Checked-In first, to be able to Check-Out.");
                $('#checkoutReason').val('');
                $('#cancellationReasons').val('');
                $('#checkin_date').val('');
                $('#checkin_time').val('');
                $('#checkout_date').val('');
                $('#checkout_time').val('');
                $('#cancel_date').val('');
                $('#cancel_time').val('');
                return;
            }
            else if (checkindateVal == null && checkoutReasons == "" && cancellationReasons == "" && $('#checkout_date').val() != "") {
                util_showMainMessage("Please select a check out reason, valid check out date, and valid check out time.");
                return;
            }
            else if (checkindateVal == null && checkoutReasons == "" && cancellationReasons == "" && $('#cancel_date').val() != "") {
                util_showMainMessage("Please select a cancel reason, valid cancel date, and valid cancel time.");
                return;
            }
            else if (checkindateVal == null && checkoutReasons == "" && cancellationReasons == "" && $('#checkout_date').val() == "" && $('#checkout_time').timepicker().val() != "") {
                util_showMainMessage("Please select a check out reason, valid check out date, and valid check out time.");
                return;
            }
            else if (checkindateVal == null && checkoutReasons == "" && cancellationReasons == "" && $('#cancel_date').val() == "" && $('#cancel_time').timepicker().val() != "") {
                util_showMainMessage("Please select a cancel reason, valid cancel date, and valid cancel time.");
                return;
            }
            else if (checkindateVal == null && checkoutReasons == "" && cancellationReasons == "") {
                util_showMainMessage("Please enter the correct data to change the appointment status.");
                return;
            }
            else if (checkindateVal == null && checkoutReasons != "" && checkoutdateVal == null && cancellationReasons == "" && canceldateVal == null) {
                util_showMainMessage("Please enter the valid date to check-out the appointment.");
                return;
            }
            else if (checkindateVal == null && checkoutReasons == "" && checkoutdateVal != null && cancellationReasons == "" && canceldateVal == null) {
                util_showMainMessage("Please enter the valid check-out reason to check-out the appointment.");
                return;
            }
            else if (checkindateVal == null && checkoutReasons == "" && checkoutdateVal == null && cancellationReasons != "" && canceldateVal == null) {
                util_showMainMessage("Please enter the valid date to cancel the appointment.");
                return;
            }
            else if (checkindateVal == null && checkoutReasons == "" && checkoutdateVal == null && cancellationReasons == "" && canceldateVal != null) {
                util_showMainMessage("Please enter the valid cancel reason to cancel the appointment.");
                return;
            }
            if ($('#checkin_date').val() != "" && $("#checkin_time").timepicker().val() == "") {
                util_showMainMessage("Please select valid check-in time.");
                return;
            }

        }
    }
    else if (app_selectedBookedVisit.isCheckedIn) {
        if (user.institutionRole.id == STUDY_STAFF) {
            if (checkindateVal != null && checkoutReasons != "" && checkoutdateVal != null) {
                util_showMainMessage("The visit can either be checked-in or checked-out.");
                return;
            }
            else if (checkindateVal != null && checkoutReasons != "" && checkoutdateVal == null) {
                util_showMainMessage("The visit can either be checked-in or checked-out.");
                return;
            }
            else if (checkindateVal != null && checkoutReasons == "" && checkoutdateVal != null) {
                util_showMainMessage("The visit can either be checked-in or checked-out.");
                return;
            }
            else if (checkindateVal == null && checkoutReasons == "") {
                util_showMainMessage("Please enter the correct data to change the appointment status.");
                return;
            }
            else if (checkindateVal == null && checkoutReasons != "" && checkoutdateVal == null) {
                util_showMainMessage("Please enter the valid date to check-out the appointment.");
                return;
            }
            else if (checkindateVal == null && checkoutReasons == "" && checkoutdateVal != null) {
                util_showMainMessage("Please enter the valid check-out reason to check-out the appointment.");
                return;
            }
        }
        else {
            if ($('#checkin_date').val() != "" && $("#checkin_time").timepicker().val() == "" && checkoutReasons == "" && checkoutdateVal == null && cancellationReasons == "" && canceldateVal == null) {
                util_showMainMessage("Please select valid check-in time.");
                return;
            }
            else if ($('#checkin_date').val() != "" && $("#checkin_time").timepicker().val() != "" && checkoutReasons == "" && $('#checkout_date').val() == "" && $("#checkout_time").timepicker().val() != "" && cancellationReasons == "" && canceldateVal == null) {
                util_showMainMessage("Please enter correct check-out data.");
                return;
            }
            else if ($('#checkin_date').val() != "" && $("#checkin_time").timepicker().val() != "" && checkoutReasons == "" && $('#checkout_date').val() != "" && $("#checkout_time").timepicker().val() != "" && cancellationReasons == "" && canceldateVal == null) {
                util_showMainMessage("Please enter correct check-out data.");
                return;
            }
            else if ($('#checkin_date').val() != "" && $("#checkin_time").timepicker().val() != "" && checkoutReasons != "" && $('#checkout_date').val() == "" && $("#checkout_time").timepicker().val() != "" && cancellationReasons == "" && canceldateVal == null) {
                util_showMainMessage("Please enter correct check-out data.");
                return;
            }
            else if ($('#checkin_date').val() != "" && $("#checkin_time").timepicker().val() != "" && checkoutReasons != "" && $('#checkout_date').val() != "" && $("#checkout_time").timepicker().val() == "" && cancellationReasons == "" && canceldateVal == null) {
                util_showMainMessage("Please enter correct check-out data.");
                return;
            }
            else if ($('#checkin_date').val() != "" && $("#checkin_time").timepicker().val() != "" && checkoutReasons != "" && $('#checkout_date').val() == "" && $("#checkout_time").timepicker().val() == "" && cancellationReasons == "" && canceldateVal == null) {
                util_showMainMessage("Please enter correct check-out data.");
                return;
            }
            else if ($('#checkin_date').val() == "" && $("#checkin_time").timepicker().val() != "" && checkoutReasons == "" && checkoutdateVal == null && cancellationReasons == "" && canceldateVal == null) {
                util_showMainMessage("Please select valid check-in date.");
                return;
            }
            else if (checkindateVal != null && checkoutReasons != "" && checkoutdateVal != null && cancellationReasons != "" && canceldateVal != null) {
                util_showMainMessage("The visit can either be checked-in or checked-out or cancelled.");
                return;
            }
            else if (checkindateVal != null && checkoutReasons != "" && checkoutdateVal == null && cancellationReasons != "" && canceldateVal != null) {
                util_showMainMessage("The visit can either be checked-in or checked-out or cancelled.");
                return;
            }
            else if (checkindateVal != null && checkoutReasons == "" && checkoutdateVal != null && cancellationReasons != "" && canceldateVal != null) {
                util_showMainMessage("The visit can either be checked-in or checked-out or cancelled.");
                return;
            }
            else if (checkindateVal != null && checkoutReasons != "" && checkoutdateVal != null && cancellationReasons == "" && canceldateVal != null) {
                util_showMainMessage("The visit can either be checked-in or checked-out or cancelled.");
                return;
            }
            else if (checkindateVal != null && checkoutReasons != "" && checkoutdateVal != null && cancellationReasons != "" && canceldateVal == null) {
                util_showMainMessage("The visit can either be checked-in or checked-out or cancelled.");
                return;
            }
            else if (checkindateVal == null && checkoutReasons != "" && checkoutdateVal == null && cancellationReasons != "" && canceldateVal == null) {
                util_showMainMessage("The visit can either be checked-out or cancelled.");
                return;
            }
            else if (checkindateVal == null && checkoutReasons != "" && checkoutdateVal != null && cancellationReasons != "" && canceldateVal != null) {
                util_showMainMessage("The visit can either be checked-in or checked-out or cancelled.");
                return;
            }
            else if (checkindateVal == null && checkoutReasons == "" && cancellationReasons == "" && $('#checkout_date').val() != "") {
                util_showMainMessage("Please select a check out reason, valid check out date, and valid check out time.");
                return;
            }
            else if (checkindateVal == null && checkoutReasons == "" && cancellationReasons == "" && $('#cancel_date').val() != "") {
                util_showMainMessage("Please select a cancel reason, valid cancel date, and valid cancel time.");
                return;
            }
            else if (checkindateVal == null && checkoutReasons == "" && cancellationReasons == "" && $('#checkout_date').val() == "" && $('#checkout_time').timepicker().val() != "") {
                util_showMainMessage("Please select a check out reason, valid check out date, and valid check out time.");
                return;
            }
            else if (checkindateVal == null && checkoutReasons == "" && cancellationReasons == "" && $('#cancel_date').val() == "" && $('#cancel_time').timepicker().val() != "") {
                util_showMainMessage("Please select a cancel reason, valid cancel date, and valid cancel time.");
                return;
            }
            else if (checkindateVal == null && checkoutReasons == "" && cancellationReasons == "") {
                util_showMainMessage("Please enter the correct data to change the appointment status.");
                return;
            }
            else if (checkindateVal == null && checkoutReasons != "" && checkoutdateVal == null && cancellationReasons == "" && canceldateVal == null) {
                util_showMainMessage("Please enter the valid date to check-out the appointment.");
                return;
            }
            else if (checkindateVal == null && checkoutReasons == "" && checkoutdateVal != null && cancellationReasons == "" && canceldateVal == null) {
                util_showMainMessage("Please enter the valid check-out reason to check-out the appointment.");
                return;
            }
            else if (checkindateVal == null && checkoutReasons == "" && checkoutdateVal == null && cancellationReasons != "" && canceldateVal == null) {
                util_showMainMessage("Please enter the valid date to cancel the appointment.");
                return;
            }
            else if (checkindateVal == null && checkoutReasons == "" && checkoutdateVal == null && cancellationReasons == "" && canceldateVal != null) {
                util_showMainMessage("Please enter the valid cancel reason to cancel the appointment.");
                return;
            }
        }
    }
    else if (app_selectedBookedVisit.isCheckedOut) {
        if (user.institutionRole.id == SUPER_ADMIN || user.institutionRole.id == SCHEDULER || user.institutionRole.id == RESOURCE_MANAGER) {
            if ($('#checkin_date').val() != "" && $("#checkin_time").timepicker().val() == "" && checkoutReasons == "" && checkoutdateVal == null && cancellationReasons == "" && canceldateVal == null) {
                util_showMainMessage("Please select valid check-in time.");
                return;
            }
            else if ($('#checkin_date').val() != "" && $("#checkin_time").timepicker().val() != "" && checkoutReasons == "" && $('#checkout_date').val() == "" && $("#checkout_time").timepicker().val() != "" && cancellationReasons == "" && canceldateVal == null) {
                util_showMainMessage("Please enter correct check-out data.");
                return;
            }
            else if ($('#checkin_date').val() != "" && $("#checkin_time").timepicker().val() != "" && checkoutReasons == "" && $('#checkout_date').val() != "" && $("#checkout_time").timepicker().val() != "" && cancellationReasons == "" && canceldateVal == null) {
                util_showMainMessage("Please enter correct check-out data.");
                return;
            }
            else if ($('#checkin_date').val() != "" && $("#checkin_time").timepicker().val() != "" && checkoutReasons != "" && $('#checkout_date').val() == "" && $("#checkout_time").timepicker().val() != "" && cancellationReasons == "" && canceldateVal == null) {
                util_showMainMessage("Please enter correct check-out data.");
                return;
            }
            else if ($('#checkin_date').val() != "" && $("#checkin_time").timepicker().val() != "" && checkoutReasons != "" && $('#checkout_date').val() != "" && $("#checkout_time").timepicker().val() == "" && cancellationReasons == "" && canceldateVal == null) {
                util_showMainMessage("Please enter correct check-out data.");
                return;
            }
            else if ($('#checkin_date').val() != "" && $("#checkin_time").timepicker().val() != "" && checkoutReasons != "" && $('#checkout_date').val() == "" && $("#checkout_time").timepicker().val() == "" && cancellationReasons == "" && canceldateVal == null) {
                util_showMainMessage("Please enter correct check-out data.");
                return;
            }
            else if ($('#checkin_date').val() == "" && $("#checkin_time").timepicker().val() != "" && checkoutReasons == "" && checkoutdateVal == null && cancellationReasons == "" && canceldateVal == null) {
                util_showMainMessage("Please select valid check-in date.");
                return;
            }
            else if (checkindateVal != null && checkoutReasons != "" && checkoutdateVal != null && cancellationReasons != "" && canceldateVal != null) {
                util_showMainMessage("The visit can either be checked-in or checked-out or cancelled.");
                return;
            }
            else if (checkindateVal != null && checkoutReasons != "" && checkoutdateVal == null && cancellationReasons != "" && canceldateVal != null) {
                util_showMainMessage("The visit can either be checked-in or checked-out or cancelled.");
                return;
            }
            else if (checkindateVal != null && checkoutReasons == "" && checkoutdateVal != null && cancellationReasons != "" && canceldateVal != null) {
                util_showMainMessage("The visit can either be checked-in or checked-out or cancelled.");
                return;
            }
            else if (checkindateVal != null && checkoutReasons != "" && checkoutdateVal != null && cancellationReasons == "" && canceldateVal != null) {
                util_showMainMessage("The visit can either be checked-in or checked-out or cancelled.");
                return;
            }
            else if (checkindateVal != null && checkoutReasons != "" && checkoutdateVal != null && cancellationReasons != "" && canceldateVal == null) {
                util_showMainMessage("The visit can either be checked-in or checked-out or cancelled.");
                return;
            }
            else if (checkindateVal == null && checkoutReasons != "" && checkoutdateVal == null && cancellationReasons != "" && canceldateVal == null) {
                util_showMainMessage("The visit can either be checked-out or cancelled.");
                return;
            }
            else if (checkindateVal == null && checkoutReasons != "" && checkoutdateVal != null && cancellationReasons != "" && canceldateVal != null) {
                util_showMainMessage("The visit can either be checked-in or checked-out or cancelled.");
                return;
            }
            else if (checkindateVal == null && checkoutReasons == "" && cancellationReasons == "" && $('#checkout_date').val() != "") {
                util_showMainMessage("Please select a check out reason, valid check out date, and valid check out time.");
                return;
            }
            else if (checkindateVal == null && checkoutReasons == "" && cancellationReasons == "" && $('#cancel_date').val() != "") {
                util_showMainMessage("Please select a cancel reason, valid cancel date, and valid cancel time.");
                return;
            }
            else if (checkindateVal == null && checkoutReasons == "" && cancellationReasons == "" && $('#checkout_date').val() == "" && $('#checkout_time').timepicker().val() != "") {
                util_showMainMessage("Please select a check out reason, valid check out date, and valid check out time.");
                return;
            }
            else if (checkindateVal == null && checkoutReasons == "" && cancellationReasons == "" && $('#cancel_date').val() == "" && $('#cancel_time').timepicker().val() != "") {
                util_showMainMessage("Please select a cancel reason, valid cancel date, and valid cancel time.");
                return;
            }
            else if (checkindateVal == null && checkoutReasons == "" && cancellationReasons == "") {
                util_showMainMessage("Please enter the correct data to change the appointment status.");
                return;
            }
            else if (checkindateVal == null && checkoutReasons != "" && checkoutdateVal == null && cancellationReasons == "" && canceldateVal == null) {
                util_showMainMessage("Please enter the valid date to check-out the appointment.");
                return;
            }
            else if (checkindateVal == null && checkoutReasons == "" && checkoutdateVal != null && cancellationReasons == "" && canceldateVal == null) {
                util_showMainMessage("Please enter the valid check-out reason to check-out the appointment.");
                return;
            }
            else if (checkindateVal == null && checkoutReasons == "" && checkoutdateVal == null && cancellationReasons != "" && canceldateVal == null) {
                util_showMainMessage("Please enter the valid date to cancel the appointment.");
                return;
            }
            else if (checkindateVal == null && checkoutReasons == "" && checkoutdateVal == null && cancellationReasons == "" && canceldateVal != null) {
                util_showMainMessage("Please enter the valid cancel reason to cancel the appointment.");
                return;
            }
        }
    }
    else if (app_selectedBookedVisit.isCancelled) {
        if ((app_selectedBookedVisit.checkInDate == null || app_selectedBookedVisit.checkInDate == "") &&
            ($('#checkin_date').val() == "" || $("#checkin_time").timepicker().val() == "") &&
            (checkoutReasons != "" ||
                ($('#checkout_date').val() != null && $('#checkout_date').val() != "")
                || checkouttimeVal != "")) {
            util_showMainMessage("Please Note: Appointment needs to be Checked-In first, to be able to Check-Out.");
            $('#checkoutReason').val('');
            $('#cancellationReasons').val('');
            $('#checkin_date').val('');
            $('#checkin_time').val('');
            $('#checkout_date').val('');
            $('#checkout_time').val('');
            $('#cancel_date').val('');
            $('#cancel_time').val('');
            return;
        }

        if ($('#checkin_date').val() != "" && $("#checkin_time").timepicker().val() == "" && checkoutReasons == "" && checkoutdateVal == null && cancellationReasons == "" && canceldateVal == null) {
            util_showMainMessage("Please select valid check-in time.");
            return;
        }
        else if ($('#checkin_date').val() != "" && $("#checkin_time").timepicker().val() != "" && checkoutReasons == "" && $('#checkout_date').val() == "" && $("#checkout_time").timepicker().val() != "" && cancellationReasons == "" && canceldateVal == null) {
            util_showMainMessage("Please enter correct check-out data.");
            return;
        }
        else if ($('#checkin_date').val() != "" && $("#checkin_time").timepicker().val() != "" && checkoutReasons == "" && $('#checkout_date').val() != "" && $("#checkout_time").timepicker().val() != "" && cancellationReasons == "" && canceldateVal == null) {
            util_showMainMessage("Please enter correct check-out data.");
            return;
        }
        else if ($('#checkin_date').val() != "" && $("#checkin_time").timepicker().val() != "" && checkoutReasons != "" && $('#checkout_date').val() == "" && $("#checkout_time").timepicker().val() != "" && cancellationReasons == "" && canceldateVal == null) {
            util_showMainMessage("Please enter correct check-out data.");
            return;
        }
        else if ($('#checkin_date').val() != "" && $("#checkin_time").timepicker().val() != "" && checkoutReasons != "" && $('#checkout_date').val() != "" && $("#checkout_time").timepicker().val() == "" && cancellationReasons == "" && canceldateVal == null) {
            util_showMainMessage("Please enter correct check-out data.");
            return;
        }
        else if ($('#checkin_date').val() != "" && $("#checkin_time").timepicker().val() != "" && checkoutReasons != "" && $('#checkout_date').val() == "" && $("#checkout_time").timepicker().val() == "" && cancellationReasons == "" && canceldateVal == null) {
            util_showMainMessage("Please enter correct check-out data.");
            return;
        }
        else if ($('#checkin_date').val() == "" && $("#checkin_time").timepicker().val() != "" && checkoutReasons == "" && checkoutdateVal == null && cancellationReasons == "" && canceldateVal == null) {
            util_showMainMessage("Please select valid check-in date.");
            return;
        }
        else if (checkindateVal != null && checkoutReasons != "" && checkoutdateVal != null && cancellationReasons != "" && canceldateVal != null) {
            util_showMainMessage("The visit can either be checked-in or checked-out or cancelled.");
            return;
        }
        else if (checkindateVal != null && checkoutReasons != "" && checkoutdateVal == null && cancellationReasons != "" && canceldateVal != null) {
            util_showMainMessage("The visit can either be checked-in or checked-out or cancelled.");
            return;
        }
        else if (checkindateVal != null && checkoutReasons == "" && checkoutdateVal != null && cancellationReasons != "" && canceldateVal != null) {
            util_showMainMessage("The visit can either be checked-in or checked-out or cancelled.");
            return;
        }
        else if (checkindateVal != null && checkoutReasons != "" && checkoutdateVal != null && cancellationReasons == "" && canceldateVal != null) {
            util_showMainMessage("The visit can either be checked-in or checked-out or cancelled.");
            return;
        }
        else if (checkindateVal != null && checkoutReasons != "" && checkoutdateVal != null && cancellationReasons != "" && canceldateVal == null) {
            util_showMainMessage("The visit can either be checked-in or checked-out or cancelled.");
            return;
        }
        else if (checkindateVal == null && checkoutReasons != "" && checkoutdateVal == null && cancellationReasons != "" && canceldateVal == null) {
            util_showMainMessage("The visit can either be checked-out or cancelled.");
            return;
        }
        else if (checkindateVal == null && checkoutReasons != "" && checkoutdateVal != null && cancellationReasons != "" && canceldateVal != null) {
            util_showMainMessage("The visit can either be checked-in or checked-out or cancelled.");
            return;
        }
        else if (checkindateVal == null && checkoutReasons == "" && cancellationReasons == "" && $('#checkout_date').val() != "") {
            util_showMainMessage("Please select a check out reason, valid check out date, and valid check out time.");
            return;
        }
        else if (checkindateVal == null && checkoutReasons == "" && cancellationReasons == "" && $('#cancel_date').val() != "") {
            util_showMainMessage("Please select a cancel reason, valid cancel date, and valid cancel time.");
            return;
        }
        else if (checkindateVal == null && checkoutReasons == "" && cancellationReasons == "" && $('#checkout_date').val() == "" && $('#checkout_time').timepicker().val() != "") {
            util_showMainMessage("Please select a check out reason, valid check out date, and valid check out time.");
            return;
        }
        else if (checkindateVal == null && checkoutReasons == "" && cancellationReasons == "" && $('#cancel_date').val() == "" && $('#cancel_time').timepicker().val() != "") {
            util_showMainMessage("Please select a cancel reason, valid cancel date, and valid cancel time.");
            return;
        }
        else if (checkindateVal == null && checkoutReasons == "" && cancellationReasons == "") {
            util_showMainMessage("Please enter the correct data to change the appointment status.");
            return;
        }
        else if (checkindateVal == null && checkoutReasons != "" && checkoutdateVal == null && cancellationReasons == "" && canceldateVal == null) {
            util_showMainMessage("Please enter the valid date to check-out the appointment.");
            return;
        }
        else if (checkindateVal == null && checkoutReasons == "" && checkoutdateVal != null && cancellationReasons == "" && canceldateVal == null) {
            util_showMainMessage("Please enter the valid check-out reason to check-out the appointment.");
            return;
        }
        else if (checkindateVal == null && checkoutReasons == "" && checkoutdateVal == null && cancellationReasons != "" && canceldateVal == null) {
            util_showMainMessage("Please enter the valid date to cancel the appointment.");
            return;
        }
        else if (checkindateVal == null && checkoutReasons == "" && checkoutdateVal == null && cancellationReasons == "" && canceldateVal != null) {
            util_showMainMessage("Please enter the valid cancel reason to cancel the appointment.");
            return;
        }
        if ($('#checkin_date').val() != "" && $("#checkin_time").timepicker().val() == "") {
            util_showMainMessage("Please select valid check-in time.");
            return;
        }
    }

    var jsonData = JSON.stringify({
        id: app_selectedBookedVisit.id,
        checkoutStatusReason: (checkoutReasons != "" ? checkoutReasons : 0),
        cancelStatusReason: (cancellationReasons != "" ? cancellationReasons : 0),
        checkInDate: (checkindateVal != null ? checkindateVal.valueOf() : 0),
        checkOutDate: (checkoutdateVal != null ? checkoutdateVal.valueOf() : 0),
        cancelDate: (canceldateVal != null ? canceldateVal.valueOf() : 0)
    });

    $.post("rest/appointment/batchEntry", {data: jsonData}, function (errorMsg) {
        var confirmationMessage = "";
        if (errorMsg != "true") {
            confirmationMessage = errorMsg;
            util_showMainMessage(confirmationMessage);
            $('#checkoutReason').val('');
            $('#cancellationReasons').val('');
            $('#checkin_date').val('');
            $('#checkin_time').val('');
            $('#checkout_date').val('');
            $('#checkout_time').val('');
            $('#cancel_date').val('');
            $('#cancel_time').val('');
        }
        else {
            confirmationMessage = "Visit Status Updated.";
            util_showMainMessage(confirmationMessage);
            alert(confirmationMessage);
            $('#checkoutReason').val('');
            $('#cancellationReasons').val('');
            $('#checkin_date').val('');
            $('#checkin_time').val('');
            $('#checkout_date').val('');
            $('#checkout_time').val('');
            $('#cancel_date').val('');
            $('#cancel_time').val('');
            appointmentListModule();
        }
    });
}

function override_widgets() {
    var unmaskTime = true;
    var preventTextInput = true;

    WidgetUtil.createDatepicker("#override_start_date");
    WidgetUtil.createTimepicker("#override_start_time", {
        display59: false, // This option is not documented in the most recent documentation
        value: '00:00' // FIXME: this does not seem to be a real timepicker option?
    }, unmaskTime, preventTextInput);
    WidgetUtil.createDatepicker("#override_end_date");
    WidgetUtil.createTimepicker("#override_end_time", {
        display59: false, // This option is not documented in the most recent documentation
        value: '00:00' // FIXME: this does not seem to be a real timepicker option
    }, unmaskTime, preventTextInput);
}

function newOverrideResourceClick(event) {
    preventDefaultAction(event);
    mode = "override_new";
    var jsonData = JSON.stringify({id: eventid});
    $.post("rest/appointment/logAddResourceOverride", {data: jsonData}, function (data) {
    });
    openOverrideDialog(0, mode);
    getSchedulingOptionsStaticList();

    $('.annotationsTable').css({display: "none"});
    $('#overrideWizardButtons input').css({width: "75px"});
}

function openOverrideDialog(bookedResourceId, mode) {
    override_widgets();
    $("#appt_bookedResource_update_dialog").dialog({
        modal: true,
        height: 650,
        width: 900,
        resizable: false
    });
    $('#overrideWizardButtons input').css({width: "75px"});

    var eventObj = home_calendar.getEventById(eventid);

    $("#override_start_date").datepicker("option", "disabled", false);
    $("#override_end_date").datepicker("option", "disabled", false);

    if (!eventObj.extendedProps.inpatientVisitType) {
        $("#override_start_date").datepicker("option", "disabled", true);
        $("#override_end_date").datepicker("option", "disabled", true);
    }
    $('#wizard_addOverrideReasons').val('');

    if (mode == "override_new") {
        $("#appt_bookedResource_update_dialog").dialog('option', 'title', 'Add New Resource');

        document.getElementById("override_billable").checked = false;
        $.getJSON("rest/appointment/getBookedVisitData?bookedVisit=" + eventObj.id, function (data) {

            var addResourceStart = new Date(data.scheduledStartTime);
            $("#override_start_date").datepicker('setDate', addResourceStart);
            $("#override_start_time").datepicker('setTime', showTime(addResourceStart));

            var addResourceEnd = new Date(data.scheduledEndTime);
            $("#override_end_date").datepicker('setDate', addResourceEnd);
            $("#override_end_time").datepicker('setTime', showTime(addResourceEnd));
        });

    }
    else {
        $("#appt_bookedResource_update_dialog").dialog('option', 'title', 'Edit Resource');

        $.getJSON("rest/appointment/getBookedResourceData?id=" + bookedResourceId, function (data) {
            app_selectedBookedResource = data;
            $("#allAnnotations").prop("checked", true);

            if (data.billable == true) {
                billableVal = true;
            }
            else {
                billableVal = false;
            }

            document.getElementById("override_billable").checked = billableVal;

            ResourceAnnotationsTable.loadForResource('bookedResource', bookedResourceId);

            $("#wizard_overrideResources").val(data.resource.id);
            var minOverrideDate = new Date(data.scheduledStartTime);
            var maxOverrideDate = new Date(data.scheduledEndTime);
            $("#override_start_date").datepicker('setDate', minOverrideDate);
            $("#override_start_time").datepicker('setTime', showTime(minOverrideDate));
            $("#override_end_date").datepicker('setDate', maxOverrideDate);
            $("#override_end_time").datepicker('setTime', showTime(maxOverrideDate));
        });
    }
}

function cancelOverrideClick() {
    $('#wizard_overrideResources').val('');
    $('body').css('overflow', 'scroll');
    $("#appt_bookedResource_update_dialog").dialog("close");
}

function initiateOverride() {
    var resourceId = $("#wizard_overrideResources").val();
    if (resourceId == "") {
        var confirmationMessage = "Please select a resource.";
        util_showMainMessage(confirmationMessage);
        return;
    }

    var overrideReason = $("#wizard_addOverrideReasons").val();
    if (overrideReason == "") {
        var confirmationMessage = "Please select an override reason.";
        util_showMainMessage(confirmationMessage);
        return;
    }
    var startTimeVal = $("#override_start_time").timepicker().val();
    var endTimeVal = $("#override_end_time").timepicker().val();
    if (startTimeVal == "") {
        util_showMainMessage("Please enter valid start time.");
        return;
    }

    if (endTimeVal == "") {
        util_showMainMessage("Please enter valid end time.");
        return;
    }

    startDateVal = $("#override_start_date").datepicker('getDate');
    endDateVal = $("#override_end_date").datepicker('getDate');
    if (startDateVal == null) {
        util_showMainMessage("Please enter valid start date.");
        return;
    }

    if (endDateVal == null) {
        util_showMainMessage("Please enter valid end date.");
        return;
    }

    if (endDateVal < startDateVal) {
        util_showMainMessage("Please enter valid end date greater than start date.");
        return;
    }

    startDateVal = setDateTime(startDateVal, startTimeVal);
    endDateVal = setDateTime(endDateVal, endTimeVal);

    if (startDateVal >= endDateVal) {
        util_showMainMessage("Please enter valid time range.");
        return;
    }
    crudOverrideResource();
}

function setDateTime(date, time) {
    var index = time.split(":");

    var hours = parseInt(index[0], 10);
    var minutes = parseInt(index[1], 10);

    date.setHours(hours);
    date.setMinutes(minutes);
    date.setSeconds(0);

    return date;
}

function crudOverrideResource() {
    $.blockUI();
    var id;
    var override_url;
    var confirmationMessage = "";
    if (mode == "override_new") {
        id = eventid;
        override_url = "rest/appointment/addBookedResourceOverride";
        confirmationMessage = "Resource Added to Visit.";
    }
    else {
        id = app_selectedBookedResource.id;
        override_url = "rest/appointment/editBookedResourceOverride";
        confirmationMessage = "Visit Resource Updated.";
    }

    var resourceId = $("#wizard_overrideResources").val();
    var overrideReason = $("#wizard_addOverrideReasons").val();

    var billable = null;
    if ($('#override_billable').is(':checked')) {
        billable = true;
    }
    else {
        billable = false;
    }

    var selectedAnnotationsId = [];
    var selectedAnnotationsQuantity = [];
    var selectedAnnotationsComment = [];

    var selectedRowData = ResourceAnnotationsTable.currentTable.getDataForSelectedRows();

    $.each(selectedRowData, function (index, rowData) {

        if(rowData.length === 4) {
            var resourceId = rowData[0];
            selectedAnnotationsId.push(resourceId);

            var quantity = rowData[2];
            selectedAnnotationsQuantity.push(parseInt(quantity));

            var comment = rowData[3];
            selectedAnnotationsComment.push(comment);
        }
    });

    var jsonData = JSON.stringify({
        id: id,
        resource: resourceId,
        billable: billable,
        startDate: startDateVal.valueOf(),
        endDate: endDateVal.valueOf(),
        overrideReason: overrideReason,
        selectedAnnotations: selectedAnnotationsId,
        selectedAnnotationsQuantity: selectedAnnotationsQuantity,
        selectedAnnotationsComment: selectedAnnotationsComment
    });

    $.post(override_url, {data: jsonData}, function (data) {
        var parsedData = JSON.parse(data);

        if (parsedData.result = true) {
            util_showMainMessage(confirmationMessage);
        }
        else {
            confirmationMessage = "There was an error in adding a resource.";
            util_showMainMessage(confirmationMessage);
        }
        $("#appt_bookedResource_update_dialog").dialog("close");
        resetCalendarOnCrud();
    });
}

function resetCalendarOnCrud() {
    initial_load = false;
    loadBookedResourcesData(1, eventid);
    searching_appointments = false;
    var isNotMonthView = !AppointmentCalendar.isMonthView();
    var startDate = new Date(home_calendar.view.activeStart);
    var endDate = new Date(home_calendar.view.activeEnd);

    var url = "rest/appointment/getCalendarBookedVisits?starting=" + startDate.getTime() + "&ending=" + endDate.getTime() + "&todayVisits=" + isNotMonthView + "&filterString=&filterId=";
    apptDoSearch(url, isNotMonthView, home_calendar.view.type);
}

function deleteBookedResourceClick() {
    $.blockUI();
    var overrideReasons = $.trim($('#wizard_deleteOverrideReasons').val());
    if (overrideReasons == null || overrideReasons == "") {
        util_showMainMessage("Please select an override reason.");
        return;
    }
    var jsonData = JSON.stringify({
        id: app_selectedBookedResource.id,
        overrideReason: (overrideReasons != "" ? overrideReasons : 0)
    });
    $.post("rest/appointment/deleteBookedResourceOverride", {data: jsonData}, function (jsonData) {
        var data = $.parseJSON(jsonData);
        var confirmationMessage;
        if (data.result) {
            confirmationMessage = "Resource Successfully deleted.";
        }
        else {
            confirmationMessage = data.errorMsg;
        }
        util_showMainMessage(confirmationMessage, function() {
            // reset the page: close the delete dialog, reset the delete-override reason to blank,
            // reload the appointment dialog
            $("#appt_bookedResource_delete_dialog").dialog("close");
            $('#wizard_deleteOverrideReasons').val('');
            resetCalendarOnCrud();
        });
    });
}

// this function is called from a DOM event handler specify in HTML code in an HTML file
function switchSubjectWizard() {
    editSearch = false;
    sessionStorage.setItem("filteredString", '');
    selectedStudy = null;
    selectedStudySubject = null;
    initial_load = true;

    $('#switchSubject_subjectFilterText').val("");
    $('#switchSubject_subjectFilterButton').attr("value", "Filter");

    resetUI();

    $('#switchSubject_div').css({display: "block"});

    $('.formElementRequired').css({visibility: "hidden"});
    $('.formValidationRequired').css({visibility: "hidden"});
    $('#scheduledResourcesBlock').hide();

    loadSwitchSubjectTable(currentPage, false, $('.dialog_study_id').text(), 'switchSubject_StudySubjectTable');
    tableKey = switchSubjectTableKey;
}

function cancelSwitchSubject() {
    $('.formElementRequired').css({visibility: "hidden"});
    $('.formValidationRequired').css({visibility: "hidden"});
    $('#scheduledResourcesBlock').show();
    $('#switchSubject_div').css({display: "none"});
    resetToResourceTableView();
}

function switchSubjectSubmit() {
    $('.formElementRequired').css({visibility: "hidden"});
    $('.formValidationRequired').css({visibility: "hidden"});


    var assignSubjectMode = sessionStorage.getItem("assignSubjectMode");
    if (assignSubjectMode === "assign" && selectedStudySubject === null) {
        showError('#switchSubject_formValidation', 'Please select a subject');
        return;
    }
    else {
        var subjectNa = document.getElementById("switchSubject_noSubject").checked;
        if (selectedStudySubject == null && !subjectNa) {
            showError('#switchSubject_formValidation', 'Please select a subject or click Hold appointment without a subject.');
            return;
        }
    }

    $('#scheduledResourcesBlock').show();
    $('#switchSubject_subjectFilterButton').attr("value", "Filter");

    $('#switchSubject_div').css({display: "none"});

    if (subjectNa) {
        appt_selectedSubjectName = noSubjectAssigned;
        appt_selectedMrn = nSlashA;
        appt_selectedSubjectMrnId = 0; // treated as null in resource layer
    }

    switchSubject(
        false,
        appt_selectedSubjectMrnId,
        eventid,
        handleConfirmedSwitchedSubject,
        handleConfirmationNeededForSwitchedSubject,
        handleAjaxError
    );

}

function loadSubjectData(currentPage, doCommon, studyId, subjectDataElement) {

    if (doCommon) {
        commonData();
    }

    if (initial_load == true) {
        createPageDropDownList();
        initial_load = false;
    }
    if (max_results == undefined) {
        max_results = 10;
    }

    var filterString = $.trim($("#appt_subjectFilterText").val());

    if (filterString == '') {
        filterString = sessionStorage.getItem("filteredString");
    }

    $.getJSON("rest/study/getStudySubjects?study=" + studyId + "&filterString=" + filterString + "&page=" +
        (currentPage != undefined ? currentPage : 1) + "&maxResults=" + (max_results != undefined ? max_results : 10) +
        "&orderBy=" + (orderBy != undefined ? orderBy : "ASC") + "&sortBy=" +
        (sortBy != undefined ? sortBy : sortByApptStudySubjectLastName),
        function (data) {

            Appointments.studySubjectGender = {};

            var iteration = 0;

            var sort_header;
            var mrn_header1 = 'sortable';
            var lastName_header2 = 'sortable';
            var firstName_header3 = 'sortable';

            if (orderBy == 'ASC') {
                sort_header = 'sorting_asc';
            }
            else if (orderBy == 'DESC') {
                sort_header = 'sorting_desc';
            }

            if (sortBy == sortByApptStudySubjectMRN) {
                mrn_header1 = sort_header;
            } else if (sortBy == undefined || sortBy == sortByApptStudySubjectLastName) {
                lastName_header2 = sort_header;
            } else if (sortBy == sortByApptStudySubjectFirstName) {
                firstName_header3 = sort_header;
            }

            appt_selectedStudy = studyId;

            $('.apptStudySubjectHeaderData').html("<table id='apptStudySubjectHeaderTable'> <thead> <tr>" +
                "   <th width='34%' class= " + lastName_header2 + " onclick='sortingFunction(sortByApptStudySubjectLastName, studySubjectsTableElementId)'>Last Name</th>" +
                "   <th width='33%' class= " + firstName_header3 + " onclick='sortingFunction(sortByApptStudySubjectFirstName, studySubjectsTableElementId)'>First Name</th>" +
                "   <th width='33%' class= " + mrn_header1 + " onclick='sortingFunction(sortByApptStudySubjectMRN, studySubjectsTableElementId)'>MRN</th>" +
                "  </tr> </thead> </table>");

            var out = " <table id='apptStudySubjectTable'> <tbody>";
            $('.dialog_study_name').text(appt_selectedStudyName);
            $('.dialog_study_pi').text(appt_selectedStudyInvestigatorName);
            totalData = data.totalCount;
            $.each(data.studySubject1s, function (key, val) {

                var fullName = "\"" + escapeQuotesHTML(val.subjectFirstName + " " + val.subjectLastName) + "\"";

                out += " <tr id='subject_row_" + val.subjectMrnId + "' " +
                    "onclick='getSubjectSelectedRowId(\"" + $(subjectDataElement).attr('id') + "\", " + val.subjectMrnId + ", " + fullName + ");'>";
                var tdClass = 'selectClass' + val.subjectMrnId;
                out += " <td width='34%' class=" + tdClass + ">" + val.subjectLastName + "</td>" +
                    " <td width='33%' class=" + tdClass + ">" + val.subjectFirstName + "</td>" +
                    " <td width='33%' class=" + tdClass + ">" + val.subjectMRN + "</td>" +
                    " </tr>";

                Appointments.studySubjectGender[val.subjectMrnId] = val.subjectSchedulerGender;

                iteration++;
            });

            out += " </tbody> </table>";

            setupTable(out, subjectDataElement,
                currentPage, totalData, $("#appt_subjectFilterText"));
        });

}

function loadSwitchSubjectTable(currentPage, doCommon, studyId, newTableId) {

    if (doCommon) {
        commonData();
    }

    ApptSubjectsTable.createTable(
        newTableId,
        studyId
    );
    ApptSubjectsTable.currentTable.reloadFn();
    ApptSubjectsTable.currentTable.setCurrentPage(currentPage);

    // set up last-name / mrn filter
    $("#switchSubject_subjectFilterText").on("keypress", switchSubjectfilterKeypressHandler);
    $("#switchSubject_subjectFilterButton").on("click", switchSubjectFilterSubjectClick);

}

function switchSubject(isConfirmation,
                       subjectMrnId,
                       eventId,
                       handleSuccessfulSwitch,
                       handleWarnings,
                       handleAjaxError) {

    var dayView = AppointmentCalendar.isDayView();
    var url = "rest/appointment/switchVisitSubject";
    if (isConfirmation) {
        url += "?confirm=true";
    }
    else {
        url += "?confirm=false";
    }

    url +=
        "&newSubjectMrnId=" + subjectMrnId +
        "&visitId=" + eventId +
        "&className=" + "Scheduled" + // how to get value dynamically?
        "&homeScreen=" + dayView +
        "";

    $.ajax({
        type: 'GET',
        url: url,
        data: "",
        success: function (data) {
            $.unblockUI();
            if (data.result) {
                //reset dialog display values HERE; if there are no warnings
                $('.dialog_subject_name_detail').text(appt_selectedSubjectName);
                $('.dialog_subject_mrn_detail').text(appt_selectedMrn);

                dialog_subject_name_detail = data.fullName;
                var visitSummary = data.visitSummary;
                // need to override inherited (wrong-value) members
                visitSummary.end = visitSummary.endDate;
                visitSummary.start = visitSummary.startDate;
                handleSuccessfulSwitch(data);

            } else {
                handleWarnings(data);
            }
        },
        error: function (xhr, status, error) {
            // TODO: check status && error
            handleAjaxError();
        },
        dataType: 'json',
        async: false
    });
}

function handleGenderBlockForNewVisit(data, confirmCallback) {

    doubleSharedRoom = data.doubleSharedRoom;
    alternateResourceUsed = data.alternateResourceUsed;
    confirmationStatus = data.confirmationStatus;

    if (doubleSharedRoom == null) {
        if (confirmationStatus == "true") {
            if (alternateResourceUsed != null) {
                alert(alternateResourceUsed);
            }
            util_showMainMessage("The appointment has been Scheduled", confirmCallback);
            scheduleComment = null;
            searching_appointments = false;
            $("#appt_wizard_searchedDetails").dialog("close");
        }
        else {
            alert("Sorry, one or more of the resources for this appointment became unavailable during the confirmation process. Please try your search again and look for alternate times.");
            scheduleComment = null;
            searching_appointments = false;
            $("#appt_wizard_searchedDetails").dialog("close");
            $.unblockUI();
            confirmCallback();
        }
    }
    else {
        if (user.institutionRole.id == STUDY_STAFF) {
            alert("There is already a patient of a different or unknown gender booked into this room. Please contact your site administrator if you want to schedule this subject into this room.");
            $.unblockUI();
            confirmCallback();
        }
        else {
            showGenderBlockDialogForNewAppointment();
        }
    }

}

function handleConfirmationNeededForSwitchedSubject(data) {

    if (user.institutionRole.id == STUDY_STAFF) {
        alert("There is already a patient of a different or unknown gender booked into this room. Please contact your site administrator if you want to schedule this subject into this room.");
    }
    else {
        showConfirmationDialogForSwitchedSubject(data.newSubjectMrnId, data.genderBlockWarning, data.doubleBookingWarning);
    }

}

function handleConfirmedSwitchedSubject(data) {
    AppointmentSearchForm.restart();
    openBookedResourcesAppointmentWindow(sessionStorage.getItem("className"));
    util_showMainMessage("The Subject of the visit is now " + data.fullName, function () {
       scheduleComment = null;
       searching_appointments = false;
       resetToResourceTableView();
       resetCalendar(new Date(data.visitSummary.startDate));
    });
}

function resetToResourceTableView() {
    tableKey = resourceTableKey;

    resetUI();
    loadDataForRespectivePage();
}

function handleCouldNotConfirmSwitchSubject(data) {
    alert('An internal error prevented the subject to be switched. Please contact a system administrator.');
}

function handleAjaxError() {
    alert('An error prevented communication with the Server. Please try again or contact a system administrator.')
}
