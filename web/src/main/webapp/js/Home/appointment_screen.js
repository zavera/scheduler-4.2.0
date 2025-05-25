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

var AppointmentScreen = {};

function onloadAppointmentDetail()
{
    renderBreadcrumbs("appt_detail");

    loadMetaHeaders();
    $("#fullHeader").load("header.html", function () {

        $('.appointmentslink').addClass('active');
        initFooter();
        app_runIdleTimer();
        commonData();
        apptBatchData();

    });
}

function appointment_viewStack(screen) {
    switch (screen) {
        case 'appointment_main_screen':
            $('.calenderTab').css({display: "block"});

            if(window.location.hash !== "#homeList"){
                parent.location.hash = "homeCalendar";
            }
            $("#filterSelected").val(0);
            $("#appointmentfilterString").val('');
            homeModuleRoles();
            $('.formTextSmallerFilter').css({display: "none"});
            $('#appointment_filterButton').css({display: "none"});
            $('#appointment_main_screen').css({display: "block"});
            $('#appointment_list_view').css({display: "none"});
            $('#appointment_form_screen').css({display: "none"});

            if (start != null) {
                start = null;
            }

            if (view != null) {
                view = null;
            }

            if (calendar_rendered == false) {
                $.blockUI({ignoreIfBlocked: true});
                $('#initial_loading').css({display: 'block'});
                var starts = null;
                var ends = null;
                var yourDate = new Date();
                starts = new Date(yourDate.getFullYear(), yourDate.getMonth(), 1);
                ends = new Date(yourDate.getFullYear(), yourDate.getMonth() + 1, 0, 23, 59, 59, 59);
                var viewData = sessionStorage.getItem(CALENDAR_VIEW_STORAGE_KEY);
                if (MiscUtil.isNotUndefinedOrNullOrEmpty(viewData)) {
                    if (viewData !== "dayGridMonth") {
                        todaysVisits = true;
                    }
                }
                url = 'rest/appointment/getCalendarBookedVisits?starting=' + starts.getTime() + '&ending=' + ends.getTime() + '&todayVisits=' + todaysVisits + '&filterString=' + calendarFilterString + '&filterId=' + calendarSelectedFilterId;
                apptDoSearch(url, false);
            }
            eventid = sessionStorage.getItem("eventid");
            if (eventid != null && eventid != "") {
                $('.formTextAreaSmaller').css({display: "block"});
                $("#wizard_comment_txtArea").val('');
                $("#checkoutWizard_comment_txtArea").val('');
                eventid = sessionStorage.getItem("eventid");
                start = sessionStorage.getItem("selected_start");
                end = sessionStorage.getItem("selected_end");
                setParametersCalendarEventClick(eventid, start, end);
                className = sessionStorage.getItem("className");
                openBookedResourcesAppointmentWindow(className);
                sessionStorage.setItem("eventid", "");
            }
            break;
        case 'appointment_list_view':
            $('.calendarTab').css({display: "block"});
            $('.formTextSmallerFilters').css({display: "none"});
            $('#appointment_list_view').css({display: "block"});
            initial_load = true;
            var prevPage = sessionStorage.getItem("apptListSelectedPage");
            if (prevPage != "null" && prevPage != null) {
                initial_load = false;
                currentPage = prevPage;
                max_results = sessionStorage.getItem("apptListSelectedMaxResults");
                sessionStorage.setItem("apptListSelectedPage", currentPage);
            }
            ApptTables.loadVisitsIntoApptTablesWidget();
            break;
        case 'appointment_form_screen':
            $('.calendarTab').css({display: "block"});
            $('#appointment_main_screen').css({display: "none"});
            $('#appointment_list_view').css({display: "none"});
            $('#appointment_form_screen').css({display: "block"});
            if (user.institutionRole.id == SUPER_ADMIN || user.institutionRole.id == SCHEDULER) {
                $('#appointmentOverbookView').css({display: "inline"});
            }
            else {
                $('#appointmentOverbookView').css({display: "none"});
            }
            search_createWidgets();
            break;
    }
}

var calendarFilterString = '';
var calendarSelectedFilterId = '';
// The following calendar filter names should match the CalendarFilter enum on the back-end
var CALENDAR_FILTER = {};
CALENDAR_FILTER.BY_SUBJECT_LAST_NAME = "BY_SUBJECT_LAST_NAME";   // 6
CALENDAR_FILTER.BY_STUDY_LOCAL_ID = "BY_STUDY_LOCAL_ID";         // 7
CALENDAR_FILTER.BY_RESOURCE_NAME = "BY_RESOURCE_NAME";           // 8
CALENDAR_FILTER.BY_APPOINTMENT_STATUS_SCHEDULED = "BY_APPOINTMENT_STATUS_SCHEDULED";  // 1
CALENDAR_FILTER.BY_APPOINTMENT_STATUS_CHECKED_IN = "BY_APPOINTMENT_STATUS_CHECKED_IN";  // 2
CALENDAR_FILTER.BY_APPOINTMENT_STATUS_CHECKED_OUT = "BY_APPOINTMENT_STATUS_CHECKED_OUT";  // 3
CALENDAR_FILTER.BY_APPOINTMENT_STATUS_CANCELLED = "BY_APPOINTMENT_STATUS_CANCELLED";  // 4
CALENDAR_FILTER.BY_APPOINTMENT_STATUS_HOLD = "BY_APPOINTMENT_STATUS_HOLD";  // 5

// TODO: store the CALENDAR_FILTER value as $("#filterSelected").val()
// so that we don't need to set calendarSelectedFilterId explicitely in the if/else code blocks below
function filterCalendarViewChangeClick() {
    if ($("#filterSelected").val() == 1) {
        $('#appointment_filterButton').css({display: "inline"});
        $('#appointment_filterButton').attr("value", "Filter");
        $('.formTextSmallerFilter').css({display: "inline"});
        calendarSelectedFilterId = CALENDAR_FILTER.BY_SUBJECT_LAST_NAME;
    }
    else if ($("#filterSelected").val() == 2) {
        $('#appointment_filterButton').css({display: "inline"});
        $('.formTextSmallerFilter').css({display: "inline"});
        $('#appointment_filterButton').attr("value", "Filter");
        calendarSelectedFilterId = CALENDAR_FILTER.BY_STUDY_LOCAL_ID;
    }
    else if ($("#filterSelected").val() == 3) {
        $('#appointment_filterButton').css({display: "inline"});
        $('.formTextSmallerFilter').css({display: "inline"});
        $('#appointment_filterButton').attr("value", "Filter");
        calendarSelectedFilterId = CALENDAR_FILTER.BY_RESOURCE_NAME;
    }
    else if ($("#filterSelected").val() == 4) {
        $('#appointment_filterButton').css({display: "inline"});
        $('.formTextSmallerFilter').css({display: "none"});
        $('#appointment_filterButton').attr("value", "Filter");
        calendarSelectedFilterId = CALENDAR_FILTER.BY_APPOINTMENT_STATUS_SCHEDULED;
    }
    else if ($("#filterSelected").val() == 5) {
        $('#appointment_filterButton').css({display: "inline"});
        $('.formTextSmallerFilter').css({display: "none"});
        $('#appointment_filterButton').attr("value", "Filter");
        calendarSelectedFilterId = CALENDAR_FILTER.BY_APPOINTMENT_STATUS_CHECKED_IN;
    }
    else if ($("#filterSelected").val() == 6) {
        $('#appointment_filterButton').css({display: "inline"});
        $('.formTextSmallerFilter').css({display: "none"});
        $('#appointment_filterButton').attr("value", "Filter");
        calendarSelectedFilterId = CALENDAR_FILTER.BY_APPOINTMENT_STATUS_CHECKED_OUT;
    }
    else if ($("#filterSelected").val() == 7) {
        $('#appointment_filterButton').css({display: "inline"});
        $('#appointment_filterButton').attr("value", "Filter");
        $('.formTextSmallerFilter').css({display: "none"});
        calendarSelectedFilterId = CALENDAR_FILTER.BY_APPOINTMENT_STATUS_CANCELLED;
    }
    else if ($("#filterSelected").val() == 8) {
        $('#appointment_filterButton').css({display: "inline"});
        $('#appointment_filterButton').attr("value", "Filter");
        $('.formTextSmallerFilter').css({display: "none"});
        calendarSelectedFilterId = CALENDAR_FILTER.BY_APPOINTMENT_STATUS_HOLD;
    }
    else if ($("#filterSelected").val() == 0) {
        $('#appointment_filterButton').css({display: "none"});
        $('.formTextSmallerFilter').css({display: "none"});
        return;
    }
}

function filterCalendarViewClick() {
    searching_appointments = true;
    filterCalendar = true;
    view = home_calendar.view;
    var yourDate = view.currentStart;
    $("#apptCalendarGoToDate").datepicker("setDate", yourDate);
    var starts = new Date(yourDate.getFullYear(), yourDate.getMonth(), 1);
    var ends = new Date(yourDate.getFullYear(), yourDate.getMonth() + 1, 0, 23, 59, 59, 59);

    if (calendarSelectedFilterId === 0) {
        $('#appointment_filterButton').css({display: "none"});
    }
    calendarFilterString = $.trim($("#appointmentfilterString").val());
    if (calendarSelectedFilterId === 6 || calendarSelectedFilterId === 7 || calendarSelectedFilterId === 8) {
        if (calendarFilterString == null || calendarFilterString === '') {
            util_showMainMessage("Please Enter the Filter Value.", "", "red");
            return;
        }
    }

    var isNotMonthView = !AppointmentCalendar.isMonthView();
    url = 'rest/appointment/getCalendarBookedVisits?starting=' + starts.getTime() + '&ending=' + ends.getTime()
        + '&filterString=' + calendarFilterString + '&filterId=' + calendarSelectedFilterId  + '&sublocation=' + sublocationFilterName
        + '&todayVisits=' + isNotMonthView;
    $('#filter_loading').css({display: 'block'});

    apptDoSearch(url, isNotMonthView, view.type);
}

function resetCalendarViewClick() {
    searching_appointments = true;
    calendarFilterString = '';
    calendarSelectedFilterId = '';
    var yourDate = new Date();
    var starts = new Date(yourDate.getFullYear(), yourDate.getMonth(), 1);
    var ends = new Date(yourDate.getFullYear(), yourDate.getMonth() + 1, 0, 23, 59, 59, 59);

    var isNotMonthView = !AppointmentCalendar.isMonthView();
    var url = "rest/appointment/getCalendarBookedVisits?starting=" + starts.getTime() + "&ending=" + ends.getTime()
      + "&todayVisits=" + isNotMonthView +"&filterString=&filterId=" + '&sublocation=' + sublocationFilterName;
    var view = home_calendar.view.type;

    $("#apptCalendarGoToDate").datepicker("setDate", yourDate);

    apptDoSearch(url, isNotMonthView, view);
    $('#appointment_filterButton').css({display: "none"});
    $('.formTextSmallerFilter').css({display: "none"});
    $("#filterSelected").val(0);
    $("#appointmentfilterString").val('');
    $('#appt_wizard_changeAppointmentSearch').css({display: "none"});
    searching_appointments = false;
}

function closeSearchedDetailsClick() {
    eventid = null;
    studyName = null;
    subjectName = null;
    studyVisit = null;
    studyVisitTime = null;
    studyPI = null;
    $("#appt_wizard_searchedDetails").dialog("close");
}

AppointmentScreen.overbookRoomWidgetsCallback = function(dateText, inst) {
    try {
        $.datepicker.parseDate('mm/dd/yy', dateText);
    } catch (e) {
        alert("Incorrect Date format. It should be MM/DD/YYYY.");
        $("#overbookRoom_startDate").val('');
        return;
    }
};

function overbookRoom_widgets() {
    WidgetUtil.createDatepicker("#overbookRoom_startDate", {
        onClose: AppointmentScreen.overbookRoomWidgetsCallback
    });
    WidgetUtil.createTimepicker('#overbookRoom_startTime');
}

function setInitialTime(picker) {
    if (picker == 1) {
        if ($('#checkin_time').val() == '') {
            $('#checkin_time').val('12:00 am');
            $('#checkin_time').timepicker('setTime', "12:00 am");
        }
    }
    else if (picker == 2) {
        if ($('#checkout_time').val() == '') {
            $('#checkout_time').val('12:00 am');
            $('#checkout_time').timepicker('setTime', "12:00 am");
        }
    }
    else {
        if ($('#cancel_time').val() == '') {
            $('#cancel_time').val('12:00 am');
            $('#cancel_time').timepicker('setTime', "12:00 am");
        }
    }
}

function search_createWidgets() {
    WidgetUtil.createDatepicker("#checkin_date", {}, false, true);
    WidgetUtil.createSliderTimepicker('#checkin_time', {
        onClose: WidgetUtil.timepickerOnCloseFactory("#checkin_time")
    });
    WidgetUtil.createDatepicker("#checkout_date", {}, false, true);
    WidgetUtil.createSliderTimepicker('#checkout_time', {
        onClose: WidgetUtil.timepickerOnCloseFactory("#checkout_time")
    });
    WidgetUtil.createDatepicker("#cancel_date", {}, false, true);
    WidgetUtil.createSliderTimepicker('#cancel_time', {
        onClose: WidgetUtil.timepickerOnCloseFactory("#cancel_time")
    });
}

function appt_checkOverBookRoom() {
    $.blockUI();
    overbookRoomSelected = null;
    overbookRoomSelectedId = null;
    $('.dialog_room_selected').text('');
    searching_appointments = false;
    overbook_startdateVal = $("#overbookRoom_startDate").val();
    overbook_starttimeVal = $("#overbookRoom_startTime").timepicker().val();

    var overbook_startdateTimeVal = overbook_startdateVal + ' ' + overbook_starttimeVal;
    overbook_startdateVal = new Date(overbook_startdateTimeVal);

    overrideRoomReasons = $.trim($('#wizard_overbookReasonsRoom').val());
    overrideRoomComment = $("#overbookWizard_roomcomment").val();

    if (overrideRoomReasons == null || overrideRoomReasons == "") {
        var confirmationMessage = "Please select an override reason.";
        util_showMainMessage(confirmationMessage, "", "red");
        return;
    }

    var roomSelected = $.trim($('#appointment_allrooms').val());

    if (roomSelected == '') {
        roomSelected = 0;
    }
    $('#appt_wizard_form-next').css({display: "none"});
    $('#appt_responseLoading').css({display: "inline-block"});
    var jsonData = JSON.stringify({
        study: (appt_selectedStudy != null ? appt_selectedStudy : 0),
        subjectMrnId: (appt_selectedSubjectMrnId != null ? appt_selectedSubjectMrnId : 0),
        visit: (appt_selectedVisit != null ? appt_selectedVisit : 0),
        startDate: overbook_startdateVal.valueOf(),
        overrideReason: overrideRoomReasons,
        roomSelected: (roomSelected != "" ? roomSelected : 0),
        comment: $.trim($("#overbookWizard_comment").val())
    });

    $.post("rest/appointment/overbookRoomSelected", {data: jsonData}, function (data) {
        var parsedData = JSON.parse(data);
        overbookRoomSelected = parsedData.overbookRoomSelected;
        overbookRoomSelectedId = parsedData.overbookRoomSelectedId;
        $('.dialog_room_selected').text(overbookRoomSelected);
        $('#appt_responseLoading').css({display: "none"});
        //appt_wizard_viewStack('appt_wizard_overbookRoomSubmit');
        $('#appt_wizard_form-prev').attr("value", "Previous");
        if (overbookRoomSelectedId == undefined) {
            $('#appt_wizard_form-next').css({display: "none"});
        }
        else {
            $('#appt_wizard_form-next').css({display: "inline-block"});
        }
        $.unblockUI();
        $('#appt_wizard_form-next').css({width: "115px"});
        $('#appt_wizard_form-next').attr("value", "Reserve Visit");
    });
}

function appt_overBookRoomAppointment() {
    $.blockUI();
    searching_appointments = true;

    overbook_startdateVal = $("#overbookRoom_startDate").val();
    overbook_starttimeVal = $("#overbookRoom_startTime").timepicker().val();

    var overbook_startdateTimeVal = overbook_startdateVal + ' ' + overbook_starttimeVal;
    overbook_startdateVal = new Date(overbook_startdateTimeVal);

    overrideRoomReasons = $.trim($('#wizard_overbookReasonsRoom').val());
    overrideRoomComment = $("#overbookWizard_roomcomment_txtArea").val();

    var roomSelected = $.trim($('#appointment_allrooms').val());

    if (roomSelected == '') {
        roomSelected = 0;
    }
    if (overrideRoomReasons == null || overrideRoomReasons == '') {
        var confirmationMessage = "Please select an override reason.";
        util_showMainMessage(confirmationMessage, "", "red");
        return;
    }

    $('#appt_wizard_form-next').css({display: "none"});
    var jsonData = JSON.stringify({
        study: (appt_selectedStudy != null ? appt_selectedStudy : 0),
        subjectMrnId: (appt_selectedSubjectMrnId != null ? appt_selectedSubjectMrnId : 0),
        visit: (appt_selectedVisit != null ? appt_selectedVisit : 0),
        startDate: overbook_startdateVal.valueOf(),
        startTime: overbook_startdateVal.valueOf(),
        overrideReason: overrideRoomReasons,
        roomSelected: overbookRoomSelectedId,
        comment: overrideRoomComment
    });

    $.post("rest/appointment/overbookRoomAppointment", {data: jsonData}, function (data) {
        var parsedData = JSON.parse(data);
        home_home_calendar.addEventSource(parsedData);
        home_calendar.gotoDate($("#overbookRoom_startDate").datepicker('getDate'));
        $('#loading').css({display: 'none'});
        $('#filter_loading').css({display: 'none'});
        $("#appt_wizard_screen").dialog("close");
        $.unblockUI();
        $("#overbookWizard_roomcomment").val('');
        $('#wizard_overbookReasonsRoom').val('');
        util_showMainMessage("Visit Reserved.", "", "blue");
        $('#appt_wizard_form-next').css({display: ''});
    });
}

function createCalendarAndSublocationTabs() {
    var result = buildSubLocTabs("subLocTop");

    $("#apptSublocationListTop").html(result.html);
    $("#apptSublocationListTop").tabs({
        active: result.defaultTabIndex,
        activate: function( event, ui ) {
            var activeTab = $(this).tabs("option", "active");

            var activeBottomTab = $("#apptSublocationListBottom").tabs("option", "active");
            if( activeBottomTab !== activeTab)
            {
                $("#apptSublocationListBottom").tabs("option", "active", activeTab);

                var sublocationLabel = $(ui.newTab).attr("data-sublocation-label");
                getCalendarBySublocation(sublocationLabel);
            }
        }
    });

    result = buildSubLocTabs("subLocBtm");

    $("#apptSublocationListBottom").html(result.html);
    $("#apptSublocationListBottom").tabs({
        active: result.defaultTabIndex,
        activate: function( event, ui ) {
            var activeTab = $(this).tabs("option", "active");

            var activeTopTab = $("#apptSublocationListTop").tabs("option", "active");

            if (activeTopTab !== activeTab) {
                $("#apptSublocationListTop").tabs("option", "active", activeTab);

                var sublocationLabel = $(ui.newTab).attr("data-sublocation-label");
                getCalendarBySublocation(sublocationLabel)
            }
        }
    });
}

function buildCalendarFromStaticsOnReady() {
    getSchedulingOptionsStaticList();
}

$(document).ready(buildCalendarFromStaticsOnReady);

//This specifies a name for this piece of code which will appear when debugging
//This is needed because this file is loaded programatically
//# sourceURL=appointment_screen.js
