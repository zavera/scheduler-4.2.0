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

xdescribe('Appointments page', function() {

    // The following rest calls need to be mocked:
    //
    ///rest/app/getStaticLists
    ///rest/auth/getUserData
    ///rest/appointment/getCalendarBookedVisits?starting=1475121600000&ending=1475207999059&todayVisits=true&filterString=&filterId=&%7B%7D

    beforeEach(function() {
        document.body.innerHTML = "";
    });

    it("has a full header", function() {

        expect(document.getElementById('fullHeader')).toBeNull();

        // load the home page into the DOM
        FixtureHelper.loadSourceHtmlFixtureIntoDom('home.html');

        expect(document.getElementById('fullHeader')).not.toBeNull();

    });
});

describe('Search New Appointment Wizard', function() {

    // load from JSON fixtures data that will be used to verify functionality
    var firstStudy = FixtureHelper.getFirstFromOpenStudiesResponse();
    var firstStudyPI = FixtureHelper.getPrincipalInvestigatorFromStudyVisitsResponse();
    var firstVisit = FixtureHelper.getFirstFromStudyVisitsResponse();
    var firstSubject = FixtureHelper.getFirstFromStudySubjectsResponse();

    it('displays the Search New Appointment form for CRC staff', function(){

        var server = UtilHelper.getFakeServer();

        AppointmentTestHelpers.setupAppointmentPage(server, SUPER_ADMIN);

        expect($("#apptSearch").length).toBe(1);
        expect($("#apptSearch").is(":visible")).toBe(true);
        expect($("#apptSearchForm").is(":visible")).toBe(false);

        $("#apptSearchHeading").click();
        expect($("#apptSearchForm").is(":visible")).toBe(true);
    });

    it('does not display the Search New Appointment form for non-CRC staff', function(){

        var server = UtilHelper.getFakeServer();

        AppointmentTestHelpers.setupAppointmentPage(server, GENERAL_VIEW);

        expect($("#apptSearch").length).toBe(1);
        expect($("#apptSearch").is(":visible")).toBe(false);
        expect($("#apptSearchHeading").is(":visible")).toBe(false);
    });

    it('lets the user select a study, a visit, a subject and a time range', function() {
        var server = UtilHelper.getFakeServer();

        loadHomePage(server);

        var studyData = FixtureHelper.getTestJsonFixture("home/json/open-studies.json");

        $("#apptSearchHeading").click();

        expect($("#apptSearchForm").is(":visible")).toBe(true);

        var studyComboboxData = $("#apptSearchStudy").combobox("getData");
        expect(studyComboboxData.length).toBe(13);

        $.each(studyComboboxData, function(index, study){
            if(index === 0){
                expect("").toBe(study.id);
                expect("").toBe(study.truncatedLabel);
            } else{
                var truncatedLabel = study.localId + ' - ' + study.name.substring(0, 50);
                if(study.name.length > 50) {
                    truncatedLabel += "...";
                }
                expect(studyData.studys[index-1].id).toBe(study.id);
                expect(studyData.studys[index-1].name).toBe(study.name);
                expect(truncatedLabel).toBe(study.truncatedLabel);
                expect(studyData.studys[index-1].localId).toBe(study.localId);
            }
        });

        expect($("#apptSearchStudyIcon").length).toBe(1);
        expect($("#apptSearchRestart").is(":visible")).toBe(false);
        expect($("#apptSearchVisit").combobox("options").disabled).toBe(true);
        expect($("#apptSearchSubject").combobox("options").disabled).toBe(true);
        expect($("#apptSearchTypeSchedule").prop("disabled")).toBe(true);
        expect($("#apptSearchTypeOverbook").prop("disabled")).toBe(true);

        var studyId = studyComboboxData[8].id;
        var visitsAndSubjects = loadVisitsAndStudySubjectsForStudy(server, studyId, visitData, studySubjectData);

        var visitData = visitsAndSubjects.visitData;
        var studySubjectData = visitsAndSubjects.studySubjectData;

        var visitComboboxData = $("#apptSearchVisit").combobox("getData");
        expect(visitComboboxData.length).toBe(8);

        $.each(visitComboboxData, function(index, visit){
            if(index === 0){
                expect("").toBe(visit.id);
                expect("").toBe(visit.truncatedLabel);
            } else{
                var truncatedLabel = visit.name.substring(0, 50);
                if(visit.name.length > 50) {
                    truncatedLabel += "...";
                }
                expect(visitData.visitTemplate1s[index-1].id).toBe(visit.id);
                expect(visitData.visitTemplate1s[index-1].name).toBe(visit.name);
                expect(truncatedLabel).toBe(visit.truncatedLabel);
            }
        });
        expect($("#apptSearchVisitIcon").length).toBe(1);

        var studySubjectComboboxData = $("#apptSearchSubject").combobox("getData");
        expect(studySubjectComboboxData.length).toBe(5);

        $.each(studySubjectComboboxData, function(index, studySubject){
            if(index === 0){
                expect("").toBe(studySubject.id);
                expect("").toBe(studySubject.text);
            } else{
                var text = studySubjectData.studySubject1s[index-1].subjectLastName + ", " + studySubjectData.studySubject1s[index-1].subjectFirstName + " - " + studySubjectData.studySubject1s[index-1].subjectMRN
                    + " (" + MiscUtil.genderShortName(studySubjectData.studySubject1s[index-1].subjectSchedulerGender) + ")";

                expect(studySubjectData.studySubject1s[index-1].id).toBe(studySubject.id);
                expect(studySubjectData.studySubject1s[index-1].subjectFirstName).toBe(studySubject.subjectFirstName);
                expect(text).toBe(studySubject.text);
            }
        });

        var visitId = visitData.visitTemplate1s[3].id;
        $("#apptSearchVisit").combobox("setValue", visitId);

        expect($("#apptSearchVisitDurationLabel").text()).toBe("Visit Duration:");
        expect($("#apptSearchVisitDuration").text()).toBe("1410 minutes (23 hr, 30 min )");

        expect($("#apptSearchSubjectIcon").length).toBe(1);
        expect($("#apptSearchInvestigatorLabel").text()).toBe("Investigator:");
        expect($("#apptSearchInvestigator").text()).toBe(visitData.investigatorFullName);

        //Select Search Range section
        expect($("#apptSearchHold").is(":visible")).toBe(true);
        expect($.trim($("#apptSearchHoldMain").text())).toBe("Hold the appointment without a subject");

        expect($.trim($("#apptSearchTypeScheduleMain").text())).toBe('Schedule');
        expect($.trim($("#apptSearchTypeOverbookMain").text())).toBe('Overbook');

        expect($("#apptSearchTypeSchedule").is(":visible")).toBe(true);
        expect($("#apptSearchTypeOverbook").is(":visible")).toBe(true);

        expect($("#apptSearchSchedule").is(":visible")).toBe(true);
        expect($.trim($("#apptSearch_startDateLabel").text())).toBe("Earliest Start Date");
        expect($.trim($("#apptSearch_startTimeLabel").text())).toBe("Earliest Start Time");
        expect($.trim($("#apptSearch_endDateLabel").text())).toBe("Latest End Date");
        expect($.trim($("#apptSearch_endTimeLabel").text())).toBe("Latest End Time");

        expect($("#apptSearch_startDate").hasClass("hasDatepicker")).toBe(true);
        expect($("#apptSearch_startTime").hasClass("hasDatepicker")).toBe(true);
        expect($("#apptSearch_endDate").hasClass("hasDatepicker")).toBe(true);
        expect($("#apptSearch_endTime").hasClass("hasDatepicker")).toBe(true);

        expect($("#apptSearchDayOfWeek").is(":visible")).toBe(false);
        expect($("#apptSearchResourceHeading").is(":visible")).toBe(true);
        expect($("#apptSearchTemplateResources").is(":visible")).toBe(false);

        expect($("#apptSearchRestart").is(":visible")).toBe(true);
    });

    it("displays the template resources for a visit", function(){
        var server = UtilHelper.getFakeServer();
        loadHomePage(server);

        var studyComboboxData = $("#apptSearchStudy").combobox("getData");
        var studyId = studyComboboxData[5].id;

        var visitsAndStudySubjects = loadVisitsAndStudySubjectsForStudy(server, studyId);

        var visitComboboxData = $("#apptSearchVisit").combobox("getData");
        var visitId = visitComboboxData[5].id;
        var visitResourceUrl = "rest/appointment/getSearchVisitResource?visit=" + visitId +
            "&page=1&maxResults=10&orderBy=" + Column.ASC + "&sortBy=" + ApptTemplateResourceTable.dbStartTime;

        var visitResourcesData = FixtureHelper.getTestJsonFixture("home/json/visit-resources.json");

        AjaxHelper.setupFakeServerTextResponseFromTestFixture(server, visitResourceUrl, "home/json/visit-resources.json");

        $("#apptSearchVisit"). combobox("setValue", visitId);

        server.respond();

        $("#apptSearchResourceHeading").click();

        expect($("#apptSearchTemplateResources").css("display")).toBe("block");

        expect($("#apptTemplateResourceTablePagination").css("display")).toBe("block");
        var visitResourceTableHeaderRows = $("#apptTemplateResourceTable-columnLabelRow");
        var headerCells = visitResourceTableHeaderRows.children("td");
        expect(headerCells.length).toBe(3);
        expect($(headerCells.get(0)).text()).toBe("Resource Name");
        expect($(headerCells.get(1)).text()).toBe("Start Time");
        expect($(headerCells.get(2)).text()).toBe("End Time");

        var visitResourceTableRows = $(".apptTemplateResourceTable-tableRow");
        expect(visitResourceTableRows.length).toBe(5);

        visitResourceTableRows.each(function (rowIndex, row) {
            var rowData = visitResourcesData.templateResource1s[rowIndex];
            var cells = $(row).children('td');
            expect($(cells).length).toBe(3);
            expect($(cells.get(0)).text()).toBe(rowData.resourceName);
            expect($(cells.get(1)).text()).toBe(getResourceTimeDisplay(rowData.startMinutes));
            expect($(cells.get(2)).text()).toBe(getResourceTimeDisplay(rowData.endMinutes));
        });
    });

    it("can overbook a visit with a room resource", function(){
        var server = UtilHelper.getFakeServer();
        loadHomePage(server);

        var studyComboboxData = $("#apptSearchStudy").combobox("getData");
        var studyId = studyComboboxData[5].id;

        var visitsAndStudySubjects = loadVisitsAndStudySubjectsForStudy(server, studyId);

        var visitComboboxData = $("#apptSearchVisit").combobox("getData");
        var visitId = visitComboboxData[5].id;
        var visitResourceUrl = "rest/appointment/getSearchVisitResource?visit=" + visitId +
            "&page=1&maxResults=10&orderBy=" + Column.ASC + "&sortBy=" + ApptTemplateResourceTable.dbStartTime;

        AjaxHelper.setupFakeServerTextResponseFromTestFixture(server, visitResourceUrl, "home/json/visit-resources.json");

        $("#apptSearchVisit").combobox("setValue", visitId);

        server.respond();

        var studySubjectData = visitsAndStudySubjects.studySubjectData;

        var subjectId = studySubjectData.studySubject1s[1].id;
        $("#apptSearchSubject").combobox("setValue", subjectId);

        expect($('#apptSearchOverbookBtn').length == 0).toBe(true);

        $("#apptSearchTypeOverbook").prop("checked", true);
        $("#apptSearchTypeOverbook").click();

        expect($("#apptSearchOverbook").css("display")).toBe("block");
        expect($('#apptSearchOverbookBtn').css("display")).toBe("inline-block");

        $("#apptSearchOverbookReason").combobox("setValue", 2);

        expect($("#apptSearchOverbookRoom").combobox("getText")).toBe("assign me a random room");

        var startDate = new Date("10/10/2017");
        $("#apptSearchOverbookStartDate").datepicker("setDate", startDate);

        var startTime = "11:00";
        $("#apptSearchOverbookStartTime").timepicker("setTime", startTime);


        var spy_performOverbookValidate = spyOn(AppointmentSearchForm.Overbook, "validate").and.callThrough();
        var spy_performOverbookMessage = spyOn(window, "util_showMainMessage");

        var performOverbookUrl = "rest/appointment/overbookRoomAppointment";

        AjaxHelper.setupFakeServerPostTextResponse(server, performOverbookUrl, '[{"id": ' + visitId + ',"allDay": false}]');

        $('#apptSearchOverbookBtn').click();

        server.respond();

        expect(spy_performOverbookValidate).toHaveBeenCalledTimes(1);
        expect(spy_performOverbookMessage).toHaveBeenCalledTimes(1);

        expect(spy_performOverbookMessage.calls.argsFor(0)).toEqual(["Visit Reserved.", ""]);
    });

    it("can reset the schedule new appointment form", function(){
        var server = UtilHelper.getFakeServer();
        loadHomePage(server);

        expect($("#apptSearchTypeSchedule").prop("disabled")).toBe(true);
        expect($("#apptSearchTypeOverbook").prop("disabled")).toBe(true);
        expect($("#apptSearchVisit").combobox("options").disabled).toBe(true);
        expect($("#apptSearchSubject").combobox("options").disabled).toBe(true);

        var studyComboboxData = $("#apptSearchStudy").combobox("getData");
        var studyId = studyComboboxData[5].id;

        var visitsAndStudySubjects = loadVisitsAndStudySubjectsForStudy(server, studyId);

        var visitComboboxData = $("#apptSearchVisit").combobox("getData");
        var visitId = visitComboboxData[5].id;
        var visitResourceUrl = "rest/appointment/getSearchVisitResource?visit=" + visitId +
            "&page=1&maxResults=10&orderBy=" + Column.ASC + "&sortBy=" + ApptTemplateResourceTable.dbStartTime;

        AjaxHelper.setupFakeServerTextResponseFromTestFixture(server, visitResourceUrl, "home/json/visit-resources.json");

        $("#apptSearchVisit").combobox("setValue", visitId);

        server.respond();

        var studySubjectData = visitsAndStudySubjects.studySubjectData;

        var subjectId = studySubjectData.studySubject1s[1].id;
        $("#apptSearchSubject").combobox("setValue", subjectId);

        expect($("#apptSearchTypeSchedule").prop("disabled")).toBe(false);
        expect($("#apptSearchTypeOverbook").prop("disabled")).toBe(false);

        expect($("#apptSearchOverbookBtn").length == 0).toBe(true);

        $("#apptSearchTypeOverbook").prop("checked", true);
        $("#apptSearchTypeOverbook").click();

        expect($("#apptSearchOverbook").css("display")).toBe("block");
        expect($('#apptSearchOverbookBtn').css("display")).toBe("inline-block");

        expect($("#apptSearchVisit").combobox("options").disabled).toBe(false);
        expect($("#apptSearchSubject").combobox("options").disabled).toBe(false);

        expect($("#apptSearchStudy").combobox("getValue") == "").toBe(false);

        $("#apptSearchRestart").click();

        expect($("#apptSearchOverbook").css("display")).toBe("none");

        expect($("#apptSearchTypeSchedule").prop("disabled")).toBe(true);
        expect($("#apptSearchTypeOverbook").prop("disabled")).toBe(true);
        expect($("#apptSearchVisit").combobox("options").disabled).toBe(true);
        expect($("#apptSearchSubject").combobox("options").disabled).toBe(true);
        expect($("#apptSearchStudy").combobox("getValue") == "").toBe(true);

        expect($("#apptSearchOverbook").css("display")).toBe("none");
        expect($('#apptSearchOverbookBtn').css("display")).toBe("none");
    });

    it("can search for a hold appointment", function(){
        var server = UtilHelper.getFakeServer();
        loadHomePage(server);

        var studyComboboxData = $("#apptSearchStudy").combobox("getData");
        var studyId = studyComboboxData[5].id;

        var visitsAndStudySubjects = loadVisitsAndStudySubjectsForStudy(server, studyId);

        var visitComboboxData = $("#apptSearchVisit").combobox("getData");
        var visitId = visitComboboxData[5].id;
        var visitResourceUrl = "rest/appointment/getSearchVisitResource?visit=" + visitId +
            "&page=1&maxResults=10&orderBy=" + Column.ASC + "&sortBy=" + ApptTemplateResourceTable.dbStartTime;

        AjaxHelper.setupFakeServerTextResponseFromTestFixture(server, visitResourceUrl, "home/json/visit-resources.json");

        $("#apptSearchVisit").combobox("setValue", visitId);

        server.respond();

        var studySubjectData = visitsAndStudySubjects.studySubjectData;

        $("#apptSearchHold").click();

        expect($("#apptSearchTypeSchedule").prop("checked")).toBe(true);

        expect($("#apptSearchSearch").css("display")).toBe("inline-block");
    });

    // This function mimics what is being done in the code.
    // I decided to copy it as opposed to using a factored out version from the code;
    // This way if the code is inadvertently modified we will catch that change here
    //
    // input duration is expressed in seconds
    function displayDuration(duration) {

        var durationMins = duration % 60;
        var min = durationMins % 60;
        var extrahr = duration - min;
        var hr = extrahr / 60;
        var days = Math.floor(duration / 24 / 60);
        var hours = Math.floor(duration / 60 % 24);
        var durationSrting = days + ' d, ' + hours + ' hr, ' + min + ' min ';
        if (days < 1) {
            durationSrting = hours + ' hr, ' + min + ' min ';
        }
        else if (days < 1 && hours < 1) {
            durationSrting = min + ' min ';
        }
        return duration + " minutes (" + durationSrting + ")";

    }

    function loadVisitsAndStudySubjectsForStudy(server, studyId){

        var loadVisitUrl = "rest/appointment/getStudyVisits?page=1&maxResults=-1&orderBy=ASC&sortBy=v.name&approved=true&study="
            + studyId + "&filterString=";

        var visitData = FixtureHelper.getTestJsonFixture("home/json/study-visits.json");
        AjaxHelper.setupFakeServerTextResponseFromTestFixture(server, loadVisitUrl, "home/json/study-visits.json");

        var loadStudySubjectUrl = 'rest/study/getStudySubjects?study=' + studyId + '&page=1&maxResults=-1&orderBy=ASC&sortBy=' +
            'lastName';
        var studySubjectData = FixtureHelper.getTestJsonFixture("home/json/study-subjects.json");
        AjaxHelper.setupFakeServerTextResponseFromTestFixture(server, loadStudySubjectUrl, "home/json/study-subjects.json");

        $("#apptSearchStudy").combobox("setValue", studyId);

        server.respond();

        return {
            visitData: visitData,
            studySubjectData: studySubjectData
        }
    }

    function loadHomePage(server){
        var loadStudyUrl = 'rest/study/getOpenStudies?&page=1&maxResults=-1&orderBy=ASC&sortBy=s.localId&filterString=';

        AjaxHelper.setupFakeServerTextResponseFromTestFixture(server, "rest/app/getStaticLists", "global/json/static-lists.json");
        AjaxHelper.setupFakeServerTextResponseFromTestFixture(server, loadStudyUrl, "home/json/open-studies.json");

        /**
         * set up the appointment page
         */
        AppointmentTestHelpers.setupAppointmentPage(server, SUPER_ADMIN);

        server.respond();
    }

    function getResourceTimeDisplay(minutes){
        var dateTime = resourceTimelineDateFromMinutes(minutes);
        var displayTime = "";
        var month = dateFormat(dateTime, 'm');
        var day = dateFormat(dateTime, 'd');
        var time = dateFormat(dateTime, 'HH:MM');
        var totalDays = Math.floor(day) + 29;
        if (month > 2) {
            displayTime = '(Day: ' + totalDays + ') ' + time;
        }
        else {
            displayTime = '(Day: ' + day + ') ' + time;
        }

        return displayTime;
    }
});
