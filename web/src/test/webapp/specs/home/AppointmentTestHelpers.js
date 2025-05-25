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
var AppointmentTestHelpers = {};

AppointmentTestHelpers.userData = {
    1: '{"authStatus": 1, ' +
        '"institutionRole":{"type":"ROLE_SUPER_ADMIN","description":"none","name":"Super Admin","id":1}}',
    5: '{"authStatus": 1, ' +
    '"institutionRole":{"type":"ROLE_FRONT_DESK","description":"none","name":"Front Desk","id":5}}',
    6: '{"authStatus": 1, ' +
    '"institutionRole":{"type":"ROLE_GENERAL_VIEW","description":"none","name":"General View Desk","id":6}}'
};
/**
 * load and set up the page
 *
 * verify that:
 * - the page contains the appointment wizard HMTL
 */
AppointmentTestHelpers.setupAppointmentPage = function(server, userType) {

    if(userType == undefined) {
        SessionDataHelper.setUser(SCHEDULER);
        userType = 1;
    }else{
        SessionDataHelper.setUser(userType);
    }

    FixtureHelper.loadSourceHtmlFixtureIntoDom('home.html');
    currentPage = 1;
    PREVIOUS_ACCESS_SHOWN = false;

    // prepare for call to onloadHomePage()
    AjaxHelper.setUpFakeServerLoadSourceHtmlResponse(server, "meta_headers.html");
    AjaxHelper.setUpFakeServerLoadSourceHtmlResponse(server, "header.html");
    AjaxHelper.setUpFakeServerLoadSourceHtmlResponse(server, "common_dialogs.html");
    AjaxHelper.setUpFakeServerLoadSourceHtmlResponse(server, "appts_dialogs.html");
    AjaxHelper.setUpFakeServerLoadSourceHtmlResponse(server, "wizard_templates.html");

    var authUrl = "rest/auth/getUserData";
    var authResponse =AppointmentTestHelpers.userData[userType];

    AjaxHelper.setupFakeServerTextResponse(server, authUrl, authResponse);

    // run all the javascript that normally runs on document load
    onloadHomePage();

    // respond to all the pending AJAX calls
    server.respond();
    server.respond();
    server.respond();

    // check that we loaded at least the basic page including the wizard HTML
    expect($.trim($('#apptSearchHeading').text())).toBe("Schedule New Appointment");

};

AppointmentTestHelpers.createCalendarEvent = function(id, eventClassName, startDateTime, endDateTime) {

    event = {
        "id": id,
        "className": eventClassName,
        "start": startDateTime,
        "end": endDateTime
    };

};

AppointmentTestHelpers.verifyCommonBookedVisitSummary = function(bookedVisitData, summaryTableRows)
{
    var label = $.trim($(summaryTableRows.get(0)).find("td").first().text());
    var value = $.trim($(summaryTableRows.get(0)).find("td:nth-child(2)").text());

    expect(label).toBe("Study:");
    expect(value).toBe(bookedVisitData.studydetailname);

    label = $.trim($(summaryTableRows.get(1)).find("td").first().text());
    value = $.trim($(summaryTableRows.get(1)).find("td:nth-child(2)").text());

    expect(label).toBe("Local ID:");
    expect(value).toBe(bookedVisitData.localId);

    label = $.trim($(summaryTableRows.get(2)).find("td").first().text());
    value = $.trim($(summaryTableRows.get(2)).find("td:nth-child(2)").text());

    expect(label).toBe("PI Last Name:");
    expect(value).toBe(bookedVisitData.studyPIdetailname);

    label = $.trim($(summaryTableRows.get(3)).find("td").first().text());
    value = $.trim($(summaryTableRows.get(3)).find("td:nth-child(2)").text());

    expect(label).toBe("Visit Name:");
    expect(value).toBe(bookedVisitData.visitdetailname);

    label = $.trim($(summaryTableRows.get(4)).find("td").first().text());
    value = $.trim($(summaryTableRows.get(4)).find("td:nth-child(2)").text());

    expect(label).toBe("Subject Name:");
    expect(value).toBe(bookedVisitData.subjectdetailname);

    label = $.trim($(summaryTableRows.get(5)).find("td").first().text());
    value = $.trim($(summaryTableRows.get(5)).find("td:nth-child(2)").text());

    expect(label).toBe("MRN:");
    expect(value).toBe(bookedVisitData.subjectMrn);

    var totalRows = $(summaryTableRows).length;
    label = $.trim($(summaryTableRows.get(totalRows - 1)).find("td").first().text());
    value = $.trim($(summaryTableRows.get(totalRows - 1)).find("td:nth-child(2)").text());

    expect(label).toBe($.trim(bookedVisitData.actionName));
    expect(value).toBe(bookedVisitData.scheduledata + "   " + bookedVisitData.userdata);
};


