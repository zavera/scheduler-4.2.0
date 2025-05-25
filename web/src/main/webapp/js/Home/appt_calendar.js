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
var apptDoSearchOuterCall;
var today = new Date();
var todaysVisits = false;
var prevStart = new Date(today.getFullYear(), today.getMonth(), today.getDate(), 0, 0, 0, 0);
var filterCalendar = false;
var className;
var LAST_MINUTE;

var ApptCalendar = {};

// ClassName constants
const NOT_FOUND = "NotFound";
const SEARCHING = "Searching";
const HOLD = "Hold";
const SCHEDULED = "Scheduled";
const CHECKED_IN = "Checked-In";
const CHECKED_OUT = "Checked-Out";
const CANCELLATION = "Cancellation";

function resetSublocationFilterTab(elementIdPrefix) {
    var totalTabCount = $(elementIdPrefix + 'Top >ul >li').length - 1;
    if (totalTabCount > -1) {
        $(elementIdPrefix + "Bottom").tabs("option", "active", totalTabCount);
        $(elementIdPrefix + "Top").tabs("option", "active", totalTabCount);
    }
}

/*
  Checks for each class name in classNames if that class exists within the element with the staticClasses
    - Element: JQuery Element
    - classNames: Array of class names
    - staticClasses: String of class names
    - returns: Boolean
 */
function checkIfElementContainsAnyClassNames(element, classNames, staticClasses) {
    var hasClass = false;
    classNames.forEach(function (className) {
        if ($(element).hasClass(staticClasses + className)) {
            hasClass = true;
        }
    });
    return hasClass;
}

function updateCalendarBlocks(element, event, image, imageAlt) {
    if (event.extendedProps.inpatientVisitType) {
        if (checkIfElementContainsAnyClassNames(element, event.classNames, "fc-day-grid-event fc-h-event fc-event fc-start fc-not-end ")) {
            var width = $(element).width() - 2;
            $(element).css('width', width + 'px');
            if (event.start.getDay() === 6) {
                var search = '<div style="float: left; padding: 2px 2px 28px 2px;"><img src="' + image + '" width="15" height="14" alt="' + imageAlt + '"/></div>';
                $(element).find('.fc-time').html(search + ' ' + startDate + ' - ' + endDate + ":");
            }
        } else if (checkIfElementContainsAnyClassNames(element, event.classNames, "fc-day-grid-event fc-h-event fc-event fc-not-start fc-not-end ")) {
            var width = $(element).width() - 2;
            $(element).css('left', '3px');
            $(element).css('width', width + 'px');
            $(element).css('height', '20px');
        } else if (checkIfElementContainsAnyClassNames(element, event.classNames, "fc-day-grid-event fc-h-event fc-event fc-not-start fc-end ")) {
            var width = $(element).width() - 2;
            $(element).css('left', '3px');
            $(element).css('width', width + 'px');

            //remaining block spans more than one day
            var parentColSpan = $(element).parent(".fc-event-container").attr('colspan');
            if (MiscUtil.isNotUndefinedOrNullOrEmpty(parentColSpan)) {
                $(element).css('height', '20px');
            } else {
                if (event.title.length > 10) {
                    $(element).find('.fc-time').find("div").css({"height": "60px"});
                    $(element).css('height', '65px');
                } else {
                    $(element).css('height', '46px');
                }
            }
        } else if (showDate(event.start) === showDate(event.end)){
            var search = '<div style="float: left; padding: 2px 2px 6px 2px;"><img src="' + image + '" width="15" height="14" alt="' + imageAlt + '"/></div>';
            $(element).find('.fc-time').html(search + ' ' + startDate + ' - ' + endDate);
            $(element).find('.fc-title').html('');
        }
    }
}

function updateEventRender(event, view, element, visitType) {
    if (event.extendedProps.inpatientVisitType) {
        if (element.find('.fc-time').length === 0 && view.type === "dayGridMonth") {
            //This is a workaround for switching from day/week view to month view so that
            //the date and time appear in the appointment block
            //event.start._i: this is needed because event.start has the time stripped from it (using stripTime)
            //when the allDay property is true
            var newStartDate = showMilitaryTimeAndMonthDay(event.start);
            var newEndDate = showMilitaryTimeAndMonthDay(event.end);
            var fcTime = $("<span></span>").addClass("fc-time").html(visitType + ' ' + newStartDate + ' - ' + newEndDate + ': ');
            element.find('.fc-title').parent().prepend(fcTime);
        } else {
            element.find('.fc-time').html(visitType + ' ' + startDate + ' - ' + endDate + ': ');
        }
        element.find('.fc-title').html(event.title);
    } else {
        element.find('.fc-time').html(visitType + ' ' + startTime + ' - ' + endTime + ': ');
        element.find('.fc-title').html(event.title);
    }
}

function apptDoSearch(url, dayView, defaultView, defaultDate, scrollToCalendar) {
    todaysVisits = dayView;
    apptDoSearchOuterCall = true;
    closeApptDialog();
    request = $.ajax({
        type: "GET",
        contentType: "application/json",
        data: "",
        url: url,
        dataType: "json",
        success: function (data) {
            LAST_MINUTE = data.lastMinute;
            reCreateCalendar(data.visits, defaultView, defaultDate);
            if (scrollToCalendar) AppointmentCalendar.scrollToCalendar();
        },
        error: apptDoSearchError
    });
}

function resetCalendarCallBack(date) {
    return function() {
        if (date === undefined || date === null) {
            date = new Date();
        }
        starts = new Date(date.getFullYear(), date.getMonth(), 1);
        ends = new Date(date.getFullYear(), date.getMonth() + 1, 0, 23, 59, 59, 59);
        url = 'rest/appointment/getCalendarBookedVisits?starting=' + starts.getTime() + '&ending=' + ends.getTime() + '&todayVisits=false' + '&filterString=' + calendarFilterString + '&filterId=' + calendarSelectedFilterId;
        apptDoSearch(url, false, home_calendar.view.type, date, true);
    }
}

function resetCalendar(date) {
    sessionStorage.setItem('scrollPos', $('.fc-scroller').scrollTop());
    var reset = resetCalendarCallBack(date);
    reset(date);
}

var apptDoSearchError = function (XMLHttpRequest, textStatus, errorThrown) {
    $('#loading').css({display: 'none'});
    $.unblockUI();
    $('#filter_loading').css({display: 'none'});
    debug('There was an error while fetching data!');
};

var reCreateCalendar = function (data, defaultView, defaultDate) {
    if (home_rendered) home_calendar.destroy();
    home_rendered = true;

    $('#loading').css({display: 'none'});
    $('#filter_loading').css({display: 'none'});
    if (inpatient_search === true) {
        var confirmationMessage = "Appointment(s) found. Scroll through the calendar to select and schedule an appointment.";
        util_showMainMessage(confirmationMessage);
        inpatient_search = false;
    }

    $('#initial_loading').css({display: 'none'});
    var defaultViewData = sessionStorage.getItem(CALENDAR_VIEW_STORAGE_KEY);
    if (MiscUtil.isNotUndefinedOrNullOrEmpty(defaultView)) {
        defaultViewData = defaultView;
    }
    $.unblockUI();

    var options = {
        plugins: ['dayGrid', 'timeGrid', 'interaction'],
        aspectRatio: 1,
        theme: true,
        header: {
            left: 'prev,next today',
            center: 'title',
            right: 'dayGridMonth,timeGridWeek,timeGridDay'
        },
        views: {
            month: {
                titleFormat: {month: 'long', year: 'numeric'},
                columnHeaderFormat: {weekday: 'long'}
            },
            week: {
                titleFormat: {month: 'short', day: 'numeric', year: 'numeric'},
                columnHeaderText: apptCalendarColumnHeader
            },
            day: {
                titleFormat: {weekday: 'long', month: 'long', day: 'numeric', year: 'numeric'},
                columnHeaderFormat: {weekday: 'long', month: 'long', day: 'numeric'} // Monday 9/7
            }
        },
        defaultView: defaultViewData,
        defaultDate: defaultDate,
        nowIndicator: true,
        firstHour: 7,
        slotLabelFormat: {hour12: 'false', hour: '2-digit', minute: '2-digit'},
        editable: false,
        ignoreTimezone: true,
        displayEventEnd: true,
        selectable: true,
        selectMirror: true,
        lazyFetching: true,
        loading: apptCalendarLoading,
        select: apptCalendarSelect,
        datesRender: apptCalendarViewRenderFactory(todaysVisits),
        eventPositioned: apptCalendarEventAfterRender,
        eventRender: apptCalendarEventRender,
        windowResize: apptCalendarWindowResize,
        eventMouseEnter: apptCalendarEventMouseover,
        eventMouseLeave: apptCalenderEventMouseout,
        events: makeCalendarEvents(data),
        allDayDefault: todaysVisits,
        eventClick: AppointmentCalendar.apptCalendarEventClick,
        eventDataTransform: AppointmentCalendar.eventTransform
    };
    var element = document.getElementById('appt_calendar');
    home_calendar = new FullCalendar.Calendar(element, options);

    home_calendar.render();
    if (view != null) {
        home_calendar.gotoDate($("#apptCalendarGoToDate").datepicker("getDate"));
        view = null;
    }
};

// created to appease the IE overlords
var apptCalendarColumnHeader = function (date) {
    var weekdays = ['Sun', 'Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat'];
    var EEE = weekdays[date.getDay()];
    var month = date.getMonth() + 1;
    return EEE + ", " + month + "/" + date.getDate();
};

var apptCalendarLoading = function (bool) {
    if (bool) {
        $.blockUI({fadeIn: 0});
    } else {
        $.unblockUI();
    }
};


var apptCalendarSelect = function (selectionInfo) {
    var date = selectionInfo.start;
    var jsEvent = selectionInfo.jsEvent;
    var view = selectionInfo.view;

    sessionStorage.setItem(CALENDAR_VIEW_STORAGE_KEY, "timeGridDay");
    home_calendar.changeView("timeGridDay", date);
};

/**
 * this function factory creates a callback function
 * with a closure containing a todaysVisists variable
 * (i.e. todaysVisists can be modified, and
 * it will be remembered from invocation to invocation)
 *
 * @param todaysVisits
 * @returns {Function}1
 */
function apptCalendarViewRenderFactory(todaysVisits) {
    return function (info) {
        var view = info.view;
        var element = info.el;
        sessionStorage.setItem(CALENDAR_VIEW_STORAGE_KEY, view.type);
        if (view.type === "dayGridMonth") {
            todaysVisits = false;
        } else {
            todaysVisits = true;
        }

        var startDate = view.currentStart;

        if (apptDoSearchOuterCall) {
            apptDoSearchOuterCall = false;
            searching_appointments = false;
            return;
        }

        if (searching_appointments === false) {
            if (filterCalendar === false) {
                if (startDate.getMonth() === (new Date()).getMonth() && view.type === "dayGridMonth") {
                    $("#apptCalendarGoToDate").datepicker("setDate", new Date());
                } else {
                    $("#apptCalendarGoToDate").datepicker("setDate", startDate);
                }
            } else {
                filterCalendar = false;
            }

            $.blockUI({ignoreIfBlocked: true});
            $('#initial_loading').css({display: 'block'});
            var starts = Date.parse(view.activeStart);
            var ends = Date.parse(view.activeEnd);
            prevStart = new Date(view.activeStart);
            url = 'rest/appointment/getCalendarBookedVisits?starting=' + starts + '&ending=' + ends + '&todayVisits=' + todaysVisits + '&filterString=' + calendarFilterString + '&filterId=' + calendarSelectedFilterId;
            $.ajax({
                type: 'GET',
                url: url,
                data: "",
                success: function (data) {
                    resetCalendarEvents(home_calendar, data.visits);
                    $('#loading').css({display: 'none'});
                    $('#filter_loading').css({display: 'none'});
                    $('#initial_loading').css({display: 'none'});
                    //Since home and appointment both have different element Id for sublocation list. Need to merge the element Id.
                    var elementIdPrefix = "#apptSublocationList";
                    resetSublocationFilterTab(elementIdPrefix);
                    $.unblockUI();
                },
                error: function (xhr, status, error) {
                    // TODO: check status && error
                    console.log(error);
                },
                dataType: 'json',
                async: true
            });
        }
    };
}

var apptCalendarEventAfterRender = function (info) {
    var event = info.event;
    var element = info.el;
    startDate = showMilitaryTimeAndMonthDay(event.start);
    endDate = showMilitaryTimeAndMonthDay(event.end);
    if (info.view.type === "dayGridMonth") {
        var image = "";
        var imageAlt = "";

        if (event.classNames.indexOf(NOT_FOUND) > -1) {
            image = "css/images/icon_unavailable.png";
            imageAlt = "(U)";
        } else if (event.classNames.indexOf(SEARCHING) > -1) {
            image = "css/images/icon_available.png";
            imageAlt = "(A)";
        } else if (event.classNames.indexOf(HOLD) > -1) {
            image = "css/images/icon_hold.png";
            imageAlt = "(H)";
        } else if (event.classNames.indexOf(SCHEDULED) > -1) {
            image = "css/images/icon_scheduled.png";
            imageAlt = "(S)";
        } else if (event.classNames.indexOf(CHECKED_IN) > -1) {
            image = "css/images/icon_checkedin.png";
            imageAlt = "(I)";
        } else if (event.classNames.indexOf(CHECKED_OUT) > -1) {
            image = "css/images/icon_checkedout.png";
            imageAlt = "(O)";

        } else if (event.classNames.indexOf(CANCELLATION) > -1) {
            image = "css/images/icon_canceled.png";
            imageAlt = "(X)";
        }
        updateCalendarBlocks(element, event, image, imageAlt);
    }
};


function apptCalendarEventRender(info) {
    var event = info.event;
    var element = $(info.el);
    var view = info.view;

    startDate = showMilitaryTimeAndMonthDay(event.start);
    endDate = showMilitaryTimeAndMonthDay(event.end);
    startTime = showMilitaryTime(event.start);
    endTime = showMilitaryTime(event.end);

    if (event.classNames.indexOf(NOT_FOUND) > -1) {
        var notFound = '<div style="float: left; padding: 2px;"><img src="css/images/icon_unavailable.png" width="15" height="15" alt="(U)"/></div>';
        if (event.extendedProps.inpatientVisitType) {
            element.find('.fc-time').html(notFound + ' ' + startDate + ' - ' + endDate);
        } else {
            element.find('.fc-time').html(notFound + ' ' + startTime + ' - ' + endTime);
        }
        element.find('.fc-title').html('');
    }
    if (event.classNames.indexOf(SEARCHING) > -1) {
        erroMsgFlag = false;
        var search = '<div style="float: left; padding: 2px;"><img src="css/images/icon_available.png" width="15" height="14" alt="(A)"/></div>';
        if (event.extendedProps.inpatientVisitType) {
            element.find('.fc-time').html(search + ' ' + startDate + ' - ' + endDate);
        } else {
            element.find('.fc-time').html(search + ' ' + startTime + ' - ' + endTime);
        }
        element.find('.fc-title').html('');
    }

    if (event.classNames.indexOf(HOLD) > -1) {
        var scheduled = '<div style="float: left; padding: 2px;"><img src="css/images/icon_hold.png" width="15" height="15" alt="(S)"/></div>';
        wasEventBookedAtLastMinute(event, element);
        updateEventRender(event, view, element, scheduled);
    } else if (event.classNames.indexOf(SCHEDULED) > -1) {
        var scheduled = '<div style="float: left; padding: 2px;"><img src="css/images/icon_scheduled.png" width="15" height="15" alt="(S)"/></div>';
        wasEventBookedAtLastMinute(event, element);
        updateEventRender(event, view, element, scheduled);
    } else if (event.classNames.indexOf(CHECKED_IN) > -1) {
        var checkedin = '<div style="float: left; padding: 2px;"><img src="css/images/icon_checkedin.png" width="15" height="15" alt="(I)"/></div>';
        wasEventBookedAtLastMinute(event, element);
        updateEventRender(event, view, element, checkedin);

    } else if (event.classNames.indexOf(CHECKED_OUT) > -1) {
        var checkedout = '<div style="float: left; padding: 2px;"><img src="css/images/icon_checkedout.png" width="15" height="15" alt="(O)"/></div>';
        wasEventBookedAtLastMinute(event, element);
        updateEventRender(event, view, element, checkedout);

    } else if (event.classNames.indexOf(CANCELLATION) > -1) {
        var cancelled = '<div style="float: left; padding: 2px;"><img src="css/images/icon_canceled.png" width="14" height="14" alt="(X)"/></div>';
        updateEventRender(event, view, element, cancelled);
    }
}

function wasEventBookedAtLastMinute(event, element) {
    var lastMinute = LAST_MINUTE;
    var scheduleData = event.extendedProps.scheduleData;
    if (scheduleData !== undefined && Math.abs(event.start - new Date(scheduleData)) / 36e5 < lastMinute) {
        element.addClass("lastMinuteEvent");
    }
}

function apptCalendarWindowResize(view) {

}

function apptCalendarEventMouseover(mouseEnterInfo) {
    var calEvent = mouseEnterInfo.event;
    var jsEvent = mouseEnterInfo.jsEvent;
    renderToolTip(jsEvent, calEvent);
    $('.appointmentHover').css('top', jsEvent.pageY + 10);
    $('.appointmentHover').css('left', jsEvent.pageX + 20);
}

function renderToolTip(mouseEvent, calEvent) {
    var top = mouseEvent.pageY + 10;
    var left = mouseEvent.pageX + 20;
    var commentCount = calEvent.extendedProps.commentCount;
    var tooltip = renderToolTipDiv(calEvent, commentCount, top, left);
    var promise = $.ajax({
        type: "GET",
        url: "rest/appointment/getAppointmentComments?id=" + calEvent.id,
        data: "",
        success: function (data) {
            var updatedCommentCount = JSON.parse(data).total;
            if (updatedCommentCount !== commentCount) {
                $("#appointmentHoverCommentCount").text(updatedCommentCount);
            }
        }
    });
    $("body").append(tooltip);
    promise.then();
}

function renderToolTipDiv(calEvent, commentCount, top, left) {
    var room;
    calEvent.extendedProps.room ? room = calEvent.extendedProps.room : room = "<br>";
    var commentLine = "";
    if (commentCount !== null || commentCount !== undefined) {
        commentLine = "<span class='appointmentHoverLabel'>Comment(s):</span>\
                       <span id='appointmentHoverCommentCount' class='appointmentHoverItem'>" + commentCount + "</span>";
    }

    return "<div class='appointmentHover' style='z-index:10001; left:" + left + "px; top:" + top + "px;'>\
                <div class='appointmentHoverRow'>\
                    <span class='appointmentHoverLabel'>Local ID:</span>\
                    <span class='appointmentHoverItem'>" + calEvent.extendedProps.localId + "</span>\
                </div>\
                <div class='appointmentHoverRow'>\
                    <span class='appointmentHoverLabel'>PI Name:</span>\
                    <span class='appointmentHoverItem'>" + calEvent.extendedProps.piName + "</span>\
                </div>\
                <div class='appointmentHoverRow'>\
                    <span class='appointmentHoverLabel'>Visit Name:</span>\
                    <span class='appointmentHoverItem'>" + calEvent.extendedProps.visitName + "</span>\
                </div>\
                <div class='appointmentHoverRow'>\
                    <span class='appointmentHoverLabel'>Room(s):</span>\
                    <span class='appointmentHoverItem'>" + room + "</span>\
                </div>\
                <div class='appointmentHoverRow'>\
                    "+ commentLine + "\
                </div>\
            </div>";

    // return "<div class='appointmentHover' style='z-index:10001; left:" + left + "px; top:" + top + "px;'> <span class='appointmentHoverLabel'>Local ID:</span>\
    //                                          <span class='appointmentHoverItem'>" + calEvent.extendedProps.localId + "</span>\
    //                                          <span class='appointmentHoverLabel'>PI Name:</span>\
    //                                          <span class='appointmentHoverItem'>" + calEvent.extendedProps.piName + "</span>\
    //                                          <span class='appointmentHoverLabel'>Visit Name:</span> \
    //                                          <span class='appointmentHoverItem'>" + calEvent.extendedProps.visitName + "</span>\
    //                                          <span class='appointmentHoverLabel'>Room(s):</span>\
    //                                          <span class='appointmentHoverItem'>" + room + "</span>\
    //                                          " + commentLine;
}

function apptCalenderEventMouseout(mouseLeaveInfo) {
    var calEvent = mouseLeaveInfo.event;
    var jsEvent = mouseLeaveInfo.jsEvent;
    $(mouseLeaveInfo.el).css('z-index', 8);
    $('.appointmentHover').remove();
}

function displayScheduledAppt(event) {
    WidgetUtil.commentBox($("#wizard_comment"));
    $("#wizard_cancellationReasons").val('');

    eventid = event.id;
    start = event.start;
    s_start = event.start;
    end = event.end;
    setParametersCalendarEventClick(eventid, start, end);

    if (user.institutionRole.id === GENERAL_VIEW || user.institutionRole.id === STUDY_STAFF || user.institutionRole.id === FRONT_DESK) {
        document.getElementById("appt_newResourceButton").style.display = "none";
    }

    document.getElementById("appt_wizard_checkin").style.display = "inline";

    if (user.institutionRole.id === STUDY_STAFF) {
        document.getElementById("appt_wizard_checkin").style.display = "none";
        document.getElementById("wizard_cancellationReasons").style.display = "inline";
        $('.statusWrap').css({display: "inline"});
    }

    if (user.institutionRole.id === GENERAL_VIEW || user.institutionRole.id === STUDY_STAFF || user.institutionRole.id === FRONT_DESK
        || user.institutionRole.id === RESOURCE_MANAGER) {
        document.getElementById("appt_wizard_overbookCheckAppointment").style.display = "none";
        document.getElementById("appt_wizard_overbookCheckAppointment1").style.display = "none";
        document.getElementById("appt_wizard_overbookCheckAppointment2").style.display = "none";
        document.getElementById("appt_wizard_overbookCheckAppointment3").style.display = "none";
    }

    if (user.institutionRole.id === GENERAL_VIEW) {
        $("#viewScheduledCommentLink").css({display: "none"});
        $("#appt_wizard_saveComment").css({display: "none"});
        $("#appt_wizard_checkin").css({display: 'none'});
        $("#appt_wizard_cancelAppointment").css({display: "none"});
        $('.statusWrap').css({display: "none"});
    }

    getResourceLists();
    openBookedResourcesAppointmentWindow(SCHEDULED);
}

ApptCalendar.preventComment = function () {
    $(".comment_label_and_input").css({display: "none"});
    $(".appt_wizard_saveComment").css({display: "none"});
};

function displayedCheckedInAppt(event) {

    WidgetUtil.commentBox($("#checkoutWizard_comment"), {
        width: "286px",
        border: "2px",
        padding: "5px"
    });

    eventid = event.id;
    start = event.start;
    s_start = event.start;
    end = event.end;
    setParametersCalendarEventClick(eventid, start, end);

    if (user.institutionRole.id === GENERAL_VIEW || user.institutionRole.id === STUDY_STAFF
        || user.institutionRole.id === FRONT_DESK) {
        document.getElementById("appt_newResourceButton").style.display = "none";
        $('.appt_checkoutnewResource').css({visibility: "hidden"});
    }

    if (user.institutionRole.id === STUDY_STAFF) {
        document.getElementById("appt_wizard_checkout").style.display = "none";
        document.getElementById("wizard_checkoutReasons").style.display = "none";
        document.getElementById("checkOutBlock").style.display = "none";
        document.getElementById("checkOutRadio").style.display = "none";
        $('.statusWrap').css({display: "none"});
    }

    if (user.institutionRole.id === GENERAL_VIEW || user.institutionRole.id === STUDY_STAFF
        || user.institutionRole.id === FRONT_DESK || user.institutionRole.id === RESOURCE_MANAGER) {
        document.getElementById("appt_wizard_overbookCheckAppointment").style.display = "none";
        document.getElementById("appt_wizard_overbookCheckAppointment1").style.display = "none";
        document.getElementById("appt_wizard_overbookCheckAppointment2").style.display = "none";
        document.getElementById("appt_wizard_overbookCheckAppointment3").style.display = "none";
    }

    if (user.institutionRole.id === GENERAL_VIEW) {
        ApptCalendar.preventComment();

        document.getElementById("viewCheckedInCommentLink").style.display = "none";
        document.getElementById("checkOutRadio").style.display = "none";
        document.getElementById("checkOutBlock").style.display = "none";
        document.getElementById("appt_wizard_checkout").style.display = "none";
        document.getElementById("wizard_checkoutReasons").style.display = "none";
    }
    getResourceLists();
    openBookedResourcesAppointmentWindow(CHECKED_IN);
}

function displayedCheckedOutAppt(event) {
    eventid = event.id;
    start = event.start;
    end = event.end;
    setParametersCalendarEventClick(eventid, start, end);

    if (user.institutionRole.id === GENERAL_VIEW || user.institutionRole.id === STUDY_STAFF
        || user.institutionRole.id === FRONT_DESK) {
        document.getElementById("appt_newResourceButton").style.display = "none";
        $('.appt_checkoutVisitnewResource').css({visibility: "hidden"});
    }

    if (user.institutionRole.id === GENERAL_VIEW) {
        document.getElementById("viewCheckedoutCommentLink").style.display = "none";
        document.getElementById("checkedOutRadioRead").style.display = "none";
    }

    if (user.institutionRole.id === GENERAL_VIEW || user.institutionRole.id === STUDY_STAFF
        || user.institutionRole.id === FRONT_DESK || user.institutionRole.id === RESOURCE_MANAGER) {
        document.getElementById("appt_wizard_overbookCheckAppointment").style.display = "none";
        document.getElementById("appt_wizard_overbookCheckAppointment1").style.display = "none";
        document.getElementById("appt_wizard_overbookCheckAppointment2").style.display = "none";
        document.getElementById("appt_wizard_overbookCheckAppointment3").style.display = "none";
    }
    openBookedResourcesAppointmentWindow(CHECKED_OUT);
}

function displayCancelledAppt(event) {
    eventid = event.id;
    start = event.start;
    end = event.end;
    setParametersCalendarEventClick(eventid, start, end);

    if (user.institutionRole.id === GENERAL_VIEW || user.institutionRole.id === STUDY_STAFF
        || user.institutionRole.id === FRONT_DESK || user.institutionRole.id === RESOURCE_MANAGER) {
        document.getElementById("appt_wizard_overbookCheckAppointment").style.display = "none";
        document.getElementById("appt_wizard_overbookCheckAppointment1").style.display = "none";
        document.getElementById("appt_wizard_overbookCheckAppointment2").style.display = "none";
        document.getElementById("appt_wizard_overbookCheckAppointment3").style.display = "none";
    }
    if (user.institutionRole.id === GENERAL_VIEW) {
        document.getElementById("viewCancelledCommentLink").style.display = "none";
    }
    if (user.institutionRole.id === GENERAL_VIEW || user.institutionRole.id === STUDY_STAFF
        || user.institutionRole.id === FRONT_DESK) {
        document.getElementById("appt_wizard_rescheduleAppointment").style.display = "none";
    }
    openBookedResourcesAppointmentWindow(CANCELLATION);
}

function displayedSearchAppt(event) {
    appt_search_view = true;
    document.getElementById("filterSearchedResources").style.display = "none";
    document.getElementById("commentTable").style.display = "inline";
    document.getElementById("appt_wizard_scheduleAppointment").style.display = "inline";
    eventid = event.id;
    start = showDateTime(event.start);
    end = showDateTime(event.end);
    $('#dialog_searchstudy_name_detail').text(appt_selectedStudyName);
    $('#dialog_searchstudy_localid').text(appt_selectedStudyLocalId);
    $('#dialog_searchstudy_piname_detail').text(appt_selectedStudyInvestigatorName);
    $('#dialog_searchsubject_name_detail').text(appt_selectedSubjectName);
    $('#dialog_searchsubject_mrn_detail').text(appt_selectedSubjectMrn);
    $('#dialog_searchsubject_gender_detail').text(appt_selectedSubjectGender);
    $('#dialog_searchvisit_name_detail').text(appt_selectedVisitName);
    $('#dialog_searchvisit_time_detail').text(appt_selectedVisitDuration);
    $('#dialog_searchvisit_start_detail').text(showDateTime(event.start));
    $('#dialog_searchvisit_end_detail').text(showDateTime(event.end));
    resetUI();
    loadBookedResourcesData(currentPage, eventid);

    WidgetUtil.commentBox($("#scheduleWizard_comment"), {width: "240px"});
    $("#appt_wizard_searchedDetails").dialog({modal: true, height: 800, width: 700});
}

/**
 * This method sets the following global variables:
 *   appt_list_view
 *   initial_load
 *   appt_search_view
 *   eventid
 *   start
 *   end
 *   className
 *   s_start
 *
 * This method accesses the following global variables:
 *   sessionStorage
 *   appt_selectedSubjectName
 *   user
 *
 * @param event
 */
function apptCalendarEventClick(event, jsEvent, view) {
    $.blockUI();
    $('.formTextAreaSmaller').css({display: "block"});
    $("#wizard_comment_txtArea").val('');
    $("#scheduleWizard_comment_txtArea").val('');
    $("#checkoutWizard_comment").val('');
    appt_list_view = false;
    initial_load = true;

    if (appt_selectedSubjectName == null || appt_selectedSubjectName === "null") {
        appt_selectedSubjectName = noSubjectAssigned;
    }

    if (event.classNames.indexOf(NOT_FOUND) > -1) {
        appt_search_view = true;
        $("#notAvailableResources").prop("checked", true);
        document.getElementById("commentTable").style.display = "none";
        document.getElementById("filterSearchedResources").style.display = "inline";
        document.getElementById("appt_wizard_scheduleAppointment").style.display = "none";
        eventid = event.id;
        start = showDateTime(event.start);
        end = showDateTime(event.end);
        $('#dialog_searchstudy_name_detail').text(appt_selectedStudyName);
        $('#dialog_searchstudy_localid').text(appt_selectedStudyLocalId);
        $('#dialog_searchstudy_piname_detail').text(appt_selectedStudyInvestigatorName);
        $('#dialog_searchsubject_name_detail').text(appt_selectedSubjectName);
        $('#dialog_searchsubject_mrn_detail').text(appt_selectedSubjectMrn);
        $('#dialog_searchsubject_gender_detail').text(appt_selectedSubjectGender);
        $('#dialog_searchvisit_name_detail').text(appt_selectedVisitName);
        $('#dialog_searchvisit_time_detail').text(appt_selectedVisitDuration);
        $('#dialog_searchvisit_start_detail').text(showDateTime(event.start));
        $('#dialog_searchvisit_end_detail').text(showDateTime(event.end));
        resetUI();
        loadBookedResourcesData(currentPage, eventid);
        $("#appt_wizard_searchedDetails").dialog({modal: true, height: 800, width: 700});
    } else if (event.classNames.indexOf(SEARCHING) > -1) {
        displayedSearchAppt(event);
    } else if (event.classNames.indexOf(SCHEDULED) > -1 || event.classNames.indexOf(HOLD) > -1) {
        displayScheduledAppt(event);
    } else if (event.classNames.indexOf(CHECKED_IN) > -1) {
        displayedCheckedInAppt(event);
    } else if (event.classNames.indexOf(CHECKED_OUT) > -1) {
        displayedCheckedOutAppt(event);
    } else if (event.classNames.indexOf(CANCELLATION) > -1) {
        displayCancelledAppt(event);
    } else {
        alert(event.classNames + ' event className: Not implemented');
    }
}


function makeCalendarEvents(data) {
    return $.map(data, function (item, i) {
        var event = {};
        if (item != null) {
            event.id = item.id;
            event.className = item.className;
            event.allDay = item.allDay;
            event.inpatientVisitType = item.inpatientVisitType;
            event.start = item.start;
            event.end = item.end;
            event.localId = item.localId;
            event.piName = item.piName;
            event.visitName = item.visitName;
            event.room = item.room;
            event.title = item.title;
            event.subjectMrn = item.subjectMrn;
            event.subjectFirstName = item.subjectFirstName;
            event.scheduleData = item.scheduleData;
            event.commentCount = item.commentCount;
            if (event.className === "false") {
                var confirmationMessage = "Please Note: No new appointment(s) found for the search criteria.";
                util_showMainMessage(confirmationMessage);
            }
            if (event.className === "no_appointments_found") {
                var confirmationMessage = "Please Note: No appointment(s) found for the filter criteria.";
                util_showMainMessage(confirmationMessage);
            }
            return event;
        }
    });
}
