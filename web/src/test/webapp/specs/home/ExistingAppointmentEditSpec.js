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
describe('Appointment calendar', function() {

    // The linch pin for the appointment calendar functionality is the
    // function called apptCalendarEventClick() in appt_calendar.js.
    // It is called with a calendar "even" object, by the calendar widget
    // when the user clicks on an appointment in the calendar. The calendar
    // event is NOT a browser event, it is just a simple javascript object, containing
    // 4 fields (id, className, start, end), which is created by the calendar
    // in order to communicate the context of a click even to the handler (apptCalendarEventClick)

    // other important methods that are called during this test:
    // openBookedResourcesAppointmentWindow
    // appointment_viewStack

    it('display a scheduled appointment', function()
    {
        var server = UtilHelper.getFakeServer();

        var event = {
            id: 101,
            className: ["Scheduled"],
            visitName: "This is the visit name"
        };

        AppointmentTestHelpers.setupAppointmentPage(server);

        var url = "rest/appointment/getScheduledVisitDetails?id=" + event.id;

        bookedVisitData.actionName = "Overbooked on: ";
        bookedVisitData.subjectdetailname = "TESTER101";
        bookedVisitData.subjectMrn = "111101";
        var responseString = JSON.stringify(bookedVisitData);

        AjaxHelper.setupFakeServerTextResponse(server, url, responseString);

        var view = {
            name:  "month"
        };
        apptCalendarEventClick(event, null, view);

        server.respond();

        var dialogTitle = $(".ui-dialog-title");
        expect(dialogTitle.is(":visible")).toBe(true);

        var dialogTitleText = dialogTitle.text();
        expect(dialogTitleText).toBe("Check-In/Cancel Appointment");

        var summaryTable = $("#appt_wizard_scheduled_visit_details").find(".forTemplate");
        var summaryTableRows = summaryTable.find("tr");
        var summaryTableRowCount = summaryTableRows.length;
        expect(summaryTableRowCount).toBe(7);

        AppointmentTestHelpers.verifyCommonBookedVisitSummary(bookedVisitData, summaryTableRows);

        var removeOrSwitchLink = $("#appt_wizard_scheduled_visit_details").find("#switchSubject_button");
        expect(removeOrSwitchLink.is(":visible")).toBe(true);

        expect(removeOrSwitchLink.text()).toBe("Remove or Switch Subject");

        var commentCounterElement = $("#wizard_comment_txtAreaCount");
        expect(commentCounterElement.text()).toBe("255 characters remaining");
    });

    it('display a hold appointment', function()
    {
        var server = UtilHelper.getFakeServer();

        var event = {
            id: 101,
            className: ["Hold"],
            visitName: "This is the visit name"
        };

        AppointmentTestHelpers.setupAppointmentPage(server);

        var url = "rest/appointment/getScheduledVisitDetails?id=" + event.id;

        bookedVisitData.actionName = "Scheduled on: ";
        bookedVisitData.subjectdetailname = "No Subject Assigned";
        bookedVisitData.subjectMrn = "N/A";

        var responseString = JSON.stringify(bookedVisitData);

        AjaxHelper.setupFakeServerTextResponse(server, url, responseString);

        var view = {
            name:  "month"
        };
        apptCalendarEventClick(event, null, view);

        server.respond();

        var dialogTitle = $(".ui-dialog-title");
        expect(dialogTitle.is(":visible")).toBe(true);

        var dialogTitleText = dialogTitle.text();
        expect(dialogTitleText).toBe("Change/Cancel Appointment");

        var summaryTable = $("#appt_wizard_scheduled_visit_details").find(".forTemplate");
        var summaryTableRows = summaryTable.find("tr");
        var summaryTableRowCount = summaryTableRows.length;
        expect(summaryTableRowCount).toBe(7);

        AppointmentTestHelpers.verifyCommonBookedVisitSummary(bookedVisitData, summaryTableRows);

        var removeOrSwitchLink = $("#switchSubject_button");
        expect(removeOrSwitchLink.is(":visible")).toBe(true);

        expect(removeOrSwitchLink.text()).toBe("Assign Subject");

        var commentCounterElement = $("#wizard_comment_txtAreaCount");
        expect(commentCounterElement.text()).toBe("255 characters remaining");
    });

    it('display a checked-in appointment', function()
    {
        var server = UtilHelper.getFakeServer();

        var event = {
            id: 101,
            className: ["Checked-In"],
            visitName: "This is the visit name"
        };

        AppointmentTestHelpers.setupAppointmentPage(server);

        var url = "rest/appointment/getScheduledVisitDetails?id=" + event.id;

        bookedVisitData.actionName = "Overbooked on: ";
        bookedVisitData.subjectdetailname = "TESTER101";
        bookedVisitData.subjectMrn = "111101";
        bookedVisitData.visitdate = "2017-01-27 12:13";

        var responseString = JSON.stringify(bookedVisitData);

        AjaxHelper.setupFakeServerTextResponse(server, url, responseString);

        var view = {
            name:  "month"
        };
        apptCalendarEventClick(event, null, view);

        server.respond();

        var dialogTitle = $("#appt_wizard_checkedin_visit_details").parent()
            .find(".ui-dialog-title");
        expect(dialogTitle.is(":visible")).toBe(true);

        var dialogTitleText = dialogTitle.text();
        expect(dialogTitleText).toBe("Check-Out Appointment");

        var summaryTable = $("#appt_wizard_checkedin_visit_details").find(".forTemplate");
        var summaryTableRows = summaryTable.find("tr");
        var summaryTableRowCount = summaryTableRows.length;
        expect(summaryTableRowCount).toBe(8);

        AppointmentTestHelpers.verifyCommonBookedVisitSummary(bookedVisitData, summaryTableRows);

        var removeOrSwitchLink = $("#appt_wizard_checkedin_visit_details").find("#switchSubject_button");
        expect(removeOrSwitchLink.is(":visible")).toBe(false);

        var commentCounterElement = $("#checkoutWizard_comment_txtAreaCount");
        expect(commentCounterElement.text()).toBe("255 characters remaining");

        var label = $.trim($(summaryTableRows.get(6)).find("td").first().text());
        var value = $.trim($(summaryTableRows.get(6)).find("td:nth-child(2)").text());

        expect(label).toBe("Check-In:");
        expect(value).toBe(bookedVisitData.visitdate);
    });

    it('display a checked-out appointment', function()
    {
        var server = UtilHelper.getFakeServer();

        var event = {
            id: 101,
            className: ["Checked-Out"],
            visitName: "This is the visit name"
        };

        AppointmentTestHelpers.setupAppointmentPage(server);

        var url = "rest/appointment/getScheduledVisitDetails?id=" + event.id;

        bookedVisitData.actionName = "Overbooked on: ";
        bookedVisitData.subjectdetailname = "TESTER101";
        bookedVisitData.subjectMrn = "111101";
        bookedVisitData.visitdate = "2017-01-27 12:13";
        var responseString = JSON.stringify(bookedVisitData);

        AjaxHelper.setupFakeServerTextResponse(server, url, responseString);

        var view = {
            name:  "week"
        };
        apptCalendarEventClick(event, null, view);

        server.respond();

        var dialogTitle = $("#appt_wizard_checkedout_visit_details").parent()
            .find(".ui-dialog-title");
        expect(dialogTitle.is(":visible")).toBe(true);

        var dialogTitleText = dialogTitle.text();
        expect(dialogTitleText).toBe("Checked-Out Appointment");

        var summaryTable = $("#appt_wizard_checkedout_visit_details").find(".forTemplate");
        var summaryTableRows = summaryTable.find("tr");
        var summaryTableRowCount = summaryTableRows.length;
        expect(summaryTableRowCount).toBe(8);

        AppointmentTestHelpers.verifyCommonBookedVisitSummary(bookedVisitData, summaryTableRows);

        var removeOrSwitchLink = $("#switchSubject_button");
        expect(removeOrSwitchLink.is(":visible")).toBe(false);
    });

    it('display a cancelled appointment', function()
    {
        var server = UtilHelper.getFakeServer();

        var event = {
            id: 101,
            className: ["Cancellation"],
            visitName: "This is the visit name"
        };

        AppointmentTestHelpers.setupAppointmentPage(server);

        var url = "rest/appointment/getScheduledVisitDetails?id=" + event.id;

        bookedVisitData.actionName = "Overbooked on: ";
        bookedVisitData.subjectdetailname = "TESTER101";
        bookedVisitData.subjectMrn = "111101";
        bookedVisitData.visitdate = "2017-02-12 00:40";
        bookedVisitData.visitreasons = "Administrative Cancel: Equipment Failure";
        var responseString = JSON.stringify(bookedVisitData);

        AjaxHelper.setupFakeServerTextResponse(server, url, responseString);

        var view = {
            name:  "day"
        };
        apptCalendarEventClick(event, null , view);

        server.respond();

        var dialogTitle = $("#appt_wizard_cancelled_visit_details").parent()
            .find(".ui-dialog-title");
        expect(dialogTitle.is(":visible")).toBe(true);

        var dialogTitleText = dialogTitle.text();
        expect(dialogTitleText).toBe("Cancelled Appointment");

        var summaryTable = $("#appt_wizard_cancelled_visit_details").find(".forTemplate");
        var summaryTableRows = summaryTable.find("tr");
        var summaryTableRowCount = summaryTableRows.length;
        expect(summaryTableRowCount).toBe(9);

        AppointmentTestHelpers.verifyCommonBookedVisitSummary(bookedVisitData, summaryTableRows);

        var removeOrSwitchLink = $("#switchSubject_button");
        expect(removeOrSwitchLink.is(":visible")).toBe(false);

        var label = $.trim($(summaryTableRows.get(6)).find("td").first().text());
        var value = $.trim($(summaryTableRows.get(6)).find("td:nth-child(2)").text());

        expect(label).toBe("Cancelled:");
        expect(value).toBe(bookedVisitData.visitdate);

        label = $.trim($(summaryTableRows.get(7)).find("td").first().text());
        value = $.trim($(summaryTableRows.get(7)).find("td:nth-child(2)").text());

        expect(label).toBe("Cancellation Reason:");
        expect(value).toBe(bookedVisitData.visitreasons);

        label = $.trim($(summaryTableRows.get(8)).find("td").first().text());
        expect(label).not.toBe("Comment:");
    });

    function verifyReschedulingDialog(displaysStartTime) {

        // check start date widget
        var todaysDate = new Date();
        var startDate = $("#reschedule_start_date").datepicker("getDate");
        expect($('#reschedule_start_date').is(":visible")).toBe(true);
        expect(startDate.toDateString()).toBe(todaysDate.toDateString());

        // check start time widget
        var startTime = $("#reschedule_start_time").timepicker().val();
        if (displaysStartTime) {
            expect($('#reschedule_start_time').is(":visible")).toBe(true);
            expect($('#reschedule_start_time').css("display")).toBe("inline-block");
            expect(startTime).toBe("07:00");
        }
        else {
            expect($('#reschedule_start_time').css("display")).toBe("none");
            // jquery says that reschedule_start_time is visible even though its display css attribute is none
            // read online https://api.jquery.com/visible-selector/ : the :visible selector may not be the best thing to use
            // expect($('#reschedule_start_time').is(":visible")).toBe(false);
        }

        // check other UI elements: reason drop-down and radio buttons
        expect($('#wizard_rescheduleReasons').is(":visible")).toBe(true);
        expect($('#booked_visit_radio').is(":visible")).toBe(true);
        expect($('#visit_template_radio').is(":visible")).toBe(true);

    }

    it("reschedule a cancelled appointment", function()
    {
        var server = UtilHelper.getFakeServer();
        AppointmentTestHelpers.setupAppointmentPage(server);

        // CASE 1:

        eventVisitType = VisitTypes.OutpatientCRC;
        relativeTime = null;

        rescheduleAppointmentClick();

        verifyReschedulingDialog(true);

        // CASE 2:

        eventVisitType = VisitTypes.InpatientCRC;
        relativeTime = null;

        rescheduleAppointmentClick();

        verifyReschedulingDialog(true);

        // CASE 3:

        eventVisitType = VisitTypes.InpatientCRC;
        console.log("visit type: " + eventVisitType);
        relativeTime = "false";

        rescheduleAppointmentClick();

        verifyReschedulingDialog(false);

    });

    it('lets user check-in existing appointments', function() {

        // the location when sarting the test is "context.html", i.e. Karma's
        // top-level HTML file which contains everything under test.
        // Note that you can't set location.href without getting an error from
        // Karma that the page is not allowed to be reloaded.
        // However because the click event handler relies on the value
        // of location.href, we'll need to mock getLocationHref()

        console.log('href', getLocationHref());


        var server = UtilHelper.getFakeServer();

        /**
         * set up the appointment page
         */
        AppointmentTestHelpers.setupAppointmentPage(server);

        // un-finished event object
        // var calendarEvent = AppointmentTestHelpers.createCalendarEvent(1, null, null, null);
        // apptCalendarEventClick(calendarEvent);

    });

    var bookedVisitData = {
        "visitTypeId":"4",
        "visitCommentsTotal":3,
        "localId":"TA-001",
        "detailVisitTime":"54900",
        "scheduledata":"2017-01-27 12:14",
        "relativeTime":"false",
        "userdata":"by user:TGI",
        "studydetailname":"Test Study 001",
        "visitId":433,
        "studyId":287,
        "visitdetailname":"TA-001 PRI",
        "studyPIdetailname":"Staff1"
    };

});
