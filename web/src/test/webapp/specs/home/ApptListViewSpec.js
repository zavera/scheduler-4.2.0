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

describe('appt_dialogs.js:renderIfPossibleToShowCheckInOut()', function() {

    it("calls css iff ok to render", function() {

        var spy_dollar = spyOn(window, "$").and.callThrough();
        var spy_css = spyOn($.fn, "css");

        Appointments.showCheckinAndCheckout = true;

        Appointments.renderIfPossibleToShowCheckInOut('someId', "someDisplay");

        expect(spy_dollar).toHaveBeenCalledTimes(1);
        expect(spy_dollar.calls.argsFor(0)).toEqual(['#someId']);

        expect(spy_css).toHaveBeenCalledTimes(1);
        expect(spy_css.calls.argsFor(0)).toEqual([{display: "someDisplay"}]);

        spy_dollar.calls.reset();
        spy_css.calls.reset();

        Appointments.showCheckinAndCheckout = false;

        Appointments.renderIfPossibleToShowCheckInOut('someId', "someDisplay");
        expect(spy_dollar).toHaveBeenCalledTimes(0);
        expect(spy_css).toHaveBeenCalledTimes(0);
    });
});

describe('Appointment List View', function() {

    it('has expandable history info', function() {

        var visitId = 9;

        FixtureHelper.loadTestHtmlFixtureIntoDom("home/html/elements-for-activity-history.html");

        var server = UtilHelper.getFakeServer();
        var restUrl = "rest/appointment/getScheduledVisitHistory?id=" + visitId;

        AjaxHelper.setupFakeServerTextResponseFromTestFixture(server, restUrl, "home/json/visit-history.json");

        var container = $("#historyContainer");

        ApptTables.expandFn(container, visitId);

        server.respond();

        var expectedHtml = '<div class="expandContent visitHistory"><div class="heading"><div class="column1"></div><div class="column2"><div>Appointment History:</div></div></div><div style=""><div class="column1"></div><div class="column2 highlight"><div>Checked-In:</div></div><div class="column3"><div>03/27/2017 08:51</div></div><div class="column4"><div>by SA123</div></div></div><div style=""><div class="column1"></div><div class="column2 highlight"><div>Checked-Out:</div></div><div class="column3"><div>03/27/2017 08:51</div></div><div class="column4"><div>by SA123</div></div></div><div style=""><div class="column1"></div><div class="column2 highlight"><div>Overbooked:</div></div><div class="column3"><div>03/27/2017 08:51</div></div><div class="column4"><div>by SA123</div></div></div><div style=""><div class="column1"></div><div class="column2 highlight"><div>Checked-In:</div></div><div class="column3"><div>03/27/2017 08:51</div></div><div class="column4"><div>by SA123</div></div></div></div>';

        expect(container.html()).toBe(expectedHtml);

    });

});

describe('appt_tables.js:ApptTables.pushColumns()', function() {

    it("creates several columns, including 'expand' if user is superAdmin", function() {

        var expectColumnHelper = function(index, expectedOptions){
            actualOptions = spy_column.calls.argsFor(index)[0];
            expect(actualOptions.width).toEqual(expectedOptions.width);

            // comparing the dynamically defined extractDataFunction's doesn't work
            for (var option in ['columnType', 'dbColumn', 'columnName',
                'rowElementClass', 'defaultSortOrder']) {

                expectedOptions[option] && expect(actualOptions[option]).toEqual(expectedOptions[option]);
            }
        };

        var spy_column = spyOn(window, "Column");

        var spy_userIsSuperAdmin = spyOn(UserRoleUtil, "userIsSuperAdmin").and.returnValue(true);

        ApptTables.pushColumns([]);

        var index = 0;
        expect(spy_column).toHaveBeenCalledTimes(10);
        expectColumnHelper(index++, {
            width: 2,
            extractDataFunction: function (val) {
                return val.id;
            },
            columnType: Column.Expandable
        });
        expectColumnHelper(index++, {
            dbColumn: ApptTables.dbColumnVisitStartTime,
            columnName: ApptTables.uiColumnVisitStartTime,
            columnType: Column.Text,
            width:13,
            defaultSortOrder: Column.ASC, // single presence of this option
                                          // among Columns indicates initial sort
            extractDataFunction: function (val) {return showDateTime(val.scheduledStartTime);}
        });
        expectColumnHelper(index++, {
            dbColumn: ApptTables.dbColumnVisitEndTime,
            columnName: ApptTables.uiColumnVisitEndTime,
            width:12,
            extractDataFunction: function (val) {return showDateTime(val.scheduledEndTime);}
        });
        expectColumnHelper(index++, {
            columnName: ApptTables.uiColumnVisitSubjectLastName,
            width:14,
            extractDataFunction: function (val) {return val.subjectLastName;}
        });
        expectColumnHelper(index++, {
            columnName: ApptTables.uiColumnVisitSubjectFirstName,
            width:14,
            extractDataFunction: function (val) {return val.subjectLastName;}
        });
        expectColumnHelper(index++, {
            dbColumn: ApptTables.dbColumnVisitMrn,
            columnName: ApptTables.uiColumnVisitMrn,
            width:9,
            extractDataFunction: function (val) {return val.subjectMRN;}
        });
        expectColumnHelper(index++, {
            dbColumn: ApptTables.dbColumnStudyLocalId,
            columnName: ApptTables.uiColumnStudyLocalId,
            width:8,
            extractDataFunction: function (val) {return val.localId;}
        });
        expectColumnHelper(index++, {
            dbColumn: ApptTables.dbColumnVisitName,
            columnName: ApptTables.uiColumnVisitName,
            width:11,
            extractDataFunction: function (val) {return val.visitName;}
        });
        expectColumnHelper(index++, {
            dbColumn: ApptTables.dbColumnVisitStatus,
            columnName: ApptTables.uiColumnVisitStatus,
            width:9,
            extractDataFunction: function (val) {return val.appointmentStatus;}
        });
        expectColumnHelper(index++, {
            columnType: Column.Hyperlink,
            rowElementClass: 'editColumn',
            width:8,
            rowElementHyperLink: function (val) {
                return 'getBookedVisitDetails(' + val.id + ')';
            },
            extractDataFunction: function () {
                return " View Detail ";
            }
        });

        spy_column.calls.reset();
        spy_userIsSuperAdmin.and.returnValue(false);

        ApptTables.pushColumns([]);
        expect(spy_column).toHaveBeenCalledTimes(9);
        expectColumnHelper(0, {
            dbColumn: ApptTables.dbColumnVisitStartTime,
            columnName: ApptTables.uiColumnVisitStartTime,
            columnType: Column.Text,
            width:13,
            defaultSortOrder: Column.ASC, // single presence of this option
                                          // among Columns indicates initial sort
            extractDataFunction: function (val) {return showDateTime(val.scheduledStartTime);}
        });
    });
});

describe('appt_tables.js:ApptTables.expandFn()', function() {

    it("makes a rest call in order to expand", function() {

        var spy_getJSON = spyOn($, "getJSON");

        ApptTables.expandFn({}, 42);

        expect(spy_getJSON).toHaveBeenCalledTimes(1);
        expect(spy_getJSON.calls.argsFor(0)).toEqual(["rest/appointment/getScheduledVisitHistory?id=42",
            jasmine.any(Function)]);

    });
});

describe('appt_tables.js:ApptTables.loadVisitsIntoApptTablesWidget()', function() {

    it("creates the table and populates it", function() {

        var apptTable = new SchedulerTables({
            tableId: ApptTables.tableName,
            columns: [],
            reloadFn: ApptTables.populateApptListTable,
            expandFn: ApptTables.expandFn
        });

        var spy_pushColumns = spyOn(ApptTables, "pushColumns");
        var spy_SchedulerTables = spyOn(window, "SchedulerTables").and.returnValue(apptTable);

        var spy_generateTable = spyOn(apptTable, "generateTable");
        var spy_setupTimeFilterFields = spyOn(ApptTables, "setupTimeFilterFields");
        var spy_populateApptListTable = spyOn(ApptTables, "populateApptListTable");

        ApptTables.loadVisitsIntoApptTablesWidget();

        expect(spy_pushColumns).toHaveBeenCalledTimes(1);
        expect(spy_pushColumns.calls.argsFor(0)).toEqual([[]]);

        expect(spy_SchedulerTables).toHaveBeenCalledTimes(1);
        var schedulerTablesOptions = spy_SchedulerTables.calls.argsFor(0)[0];
        expect (schedulerTablesOptions.tableId).toBe(ApptTables.tableName);
        expect (schedulerTablesOptions.reloadFn).toBe(ApptTables.clearDatesAndPopulateTable);
        expect (schedulerTablesOptions.expandFn).toBe(ApptTables.expandFn);

        expect(spy_generateTable).toHaveBeenCalledTimes(1);
        var generateTableArgs = spy_generateTable.calls.argsFor(0);
        expect (generateTableArgs[0]).toEqual([]);
        expect (generateTableArgs[1]).toEqual(50);
        expect (generateTableArgs[2]).toEqual(ApptTables.tableName + "Pagination");

        expect(spy_setupTimeFilterFields).toHaveBeenCalledTimes(1);

        expect(spy_populateApptListTable).toHaveBeenCalledTimes(1);
        expect(spy_populateApptListTable.calls.argsFor(0)).toEqual([]);
    });
});

describe('appt_tables.js:ApptTables.loadVisitsIntoApptTablesWidget()', function() {

    it("loads data into the table", function() {

        var apptTable = new SchedulerTables({
            tableId: ApptTables.tableName,
            columns: [],
            reloadFn: ApptTables.populateApptListTable,
            expandFn: ApptTables.expandFn
        });
        var ajaxResult = { always: function() {}};

        var spy_setTimeout = spyOn(window, "setTimeout");
        var spy_getCurrentPage = spyOn(apptTable, "getCurrentPage").and.returnValue("1");
        var spy_getRecordsPerPage = spyOn(apptTable, "getRecordsPerPage").and.returnValue("2");
        var spy_getSortOrder = spyOn(apptTable, "getSortOrder").and.returnValue("3");
        var spy_getSortColumn = spyOn(apptTable, "getSortColumn").and.returnValue("4");
        var spy_getJSON = spyOn($, "getJSON").and.returnValue(ajaxResult);

        var endDate = "";
        var startDateObj = {value: "03/25/2007"};
        var spy_getFilterObjByName = spyOn(apptTable, "getFilterObjByName").and.returnValues(startDateObj, endDate);

        selected = "0";
        ApptTables.currentTable = apptTable;

        ApptTables.populateApptListTable();

        expect(spy_getFilterObjByName).toHaveBeenCalledTimes(2);
        expect(spy_setTimeout).toHaveBeenCalledTimes(1);
        expect(spy_getCurrentPage).toHaveBeenCalledTimes(1);
        expect(spy_getRecordsPerPage).toHaveBeenCalledTimes(1);
        expect(spy_getSortOrder).toHaveBeenCalledTimes(1);
        expect(spy_getSortColumn).toHaveBeenCalledTimes(1);

        selectedEndDate = "";
        var expectedUrl = "rest/appointment/getBookedVisitsList" +
            "?fromDate=" + endDate +
            "&toDate=" + startDateObj.value +
            "&page=" +          "1" +
            "&maxResults=" +    "2" +
            "&orderBy=" +       "3" +
            "&sortBy=" +        "4" +
            "&search=%7B%22searchItems%22%3A%5B%5D%7D";

        expect(spy_getJSON).toHaveBeenCalledTimes(1);
        UtilHelper.expectEqualCollapse(spy_getJSON.calls.argsFor(0)[0],
                expectedUrl);

        ApptTables.populateApptListTable();
    });
});

describe("Start Time and End Time Columns", function(){
    xit("applies initial minimum and range to end date", function() {
        FixtureHelper.loadSourceHtmlFixtureIntoDom("appt_list.html");

        var currentDate = new Date();

        ApptTables.loadVisitsIntoApptTablesWidget();

        var startDatepicker =  $("#" + escapeJQSelector("appointmentListTable-filter-bv.scheduledStartTime")).textbox('textbox');
        var startDate = startDatepicker.val();
        expect(startDate).toBe(showDate(currentDate));

        var endDatepicker =  $("#" + escapeJQSelector("appointmentListTable-filter-bv.scheduledEndTime")).textbox('textbox');
        var minEndDate = endDatepicker.datepicker("option", "minDate");
        expect(minEndDate).toBe(startDate);

        var endDateRange = endDatepicker.datepicker("option", "yearRange");
        expect(endDateRange).toBe("-120:+20");
    });

    xit('adjusts the minimum and maximum start and end dates', function(){
        FixtureHelper.loadSourceHtmlFixtureIntoDom("appt_list.html");

        ApptTables.loadVisitsIntoApptTablesWidget();

        var startDate = new Date("04/18/2005");
        var startDatepicker =  $("#" + escapeJQSelector("appointmentListTable-filter-bv.scheduledStartTime")).textbox('textbox');
        var endDatepicker =  $("#" + escapeJQSelector("appointmentListTable-filter-bv.scheduledEndTime")).textbox('textbox');

        startDatepicker.datepicker("setDate", startDate);
        ApptTables.adjustDateMinMax();
        var minEndDate = endDatepicker.datepicker("option", "minDate");
        var defaultEndDate = endDatepicker.datepicker("option", "defaultDate");

        var startDateStr = showDate(startDate);
        expect(minEndDate).toBe(startDateStr);
        expect(defaultEndDate).toBe(startDateStr);

        var endDate = new Date("11/28/2014");
        endDatepicker.datepicker("setDate", endDate);
        ApptTables.adjustDateMinMax();
        var maxStartDate = startDatepicker.datepicker("option", "maxDate");
        expect(maxStartDate).toBe(showDate(endDate));
    });
});

describe("Navigation and Filter Mode", function(){
    xit("performs filtering with start and end dates", function() {
        FixtureHelper.loadSourceHtmlFixtureIntoDom("appt_list.html");

        ApptTables.loadVisitsIntoApptTablesWidget();

        var server = UtilHelper.getFakeServer();

        var currentDate = new Date();

        var expectedFilteringUrl = "rest/appointment/getBookedVisitsList?fromDate=" + showDate(currentDate)  +
            "&toDate=03/10/2008&page=1&maxResults=50&orderBy=ASC&sortBy=bv.scheduledStartTime" +
            "&search=%7B%22searchItems%22%3A%5B%7B%22key%22%3A%22apSt.name%22%2C%22value%22%3A%22Scheduled%22%7D%5D%7D";

        var filteredBookedVisitData = getTestBookedVisitData();
        var filterResponseString = JSON.stringify(filteredBookedVisitData);

        AjaxHelper.setupFakeServerTextResponse(server, expectedFilteringUrl, filterResponseString);

        ApptTables.currentTable.setFilterKeyValuePair(ApptTables.dbColumnVisitEndTime, "03/10/2008");
        ApptTables.currentTable.setFilterKeyValuePair(ApptTables.dbColumnVisitStatus, "Scheduled");

        ApptTables.populateApptListTable();

        server.respond();

        var filterCells = $("#appointmentListTable-filterRow").find(".filterField");
        expect(filterCells.length).toBe(8);

        //one row with column names and 2 rows of visible and hidden expandable data
        expect($("#appointmentListTable").find("tbody tr").length).toBe(3);
    });

    xit("performs navigation using end date", function()
    {
        FixtureHelper.loadSourceHtmlFixtureIntoDom("appt_list.html");

        ApptTables.loadVisitsIntoApptTablesWidget();

        var server = UtilHelper.getFakeServer();

        var date = new Date();

        var expectedNavigationUrl = "rest/appointment/getBookedVisitsList?fromDate=" +
            "&toDate=" + showDate(date) + "&page=1&maxResults=50&orderBy=ASC" +
            "&sortBy=bv.scheduledStartTime" +
            "&search=%7B%22searchItems%22%3A%5B%5D%7D";

        var bookedVisitData = getTestBookedVisitData();
        var responseString = JSON.stringify(bookedVisitData);
        AjaxHelper.setupFakeServerTextResponse(server, expectedNavigationUrl, responseString);

        ApptTables.currentTable.setFilterKeyValuePair(ApptTables.dbColumnVisitStartTime, null);
        ApptTables.currentTable.setFilterKeyValuePair(ApptTables.dbColumnVisitEndTime, showDate(date));

        ApptTables.populateApptListTable();

        server.respond();

        //one row with column names and 2 rows of visible and hidden expandable data
        expect($("#appointmentListTable").find("tbody tr").length).toBe(3);
    });

    xit("it performs pagination after navigation", function(){
        FixtureHelper.loadSourceHtmlFixtureIntoDom("appt_list.html");

        ApptTables.loadVisitsIntoApptTablesWidget();

        var server = UtilHelper.getFakeServer();

        var date = new Date();

        var expectedUrl = "rest/appointment/getBookedVisitsList?fromDate=" +
            "&toDate=" + showDate(date) + "&page=1&maxResults=50&orderBy=ASC" +
            "&sortBy=bv.scheduledStartTime" +
            "&search=%7B%22searchItems%22%3A%5B%5D%7D";

        var bookedVisitData = getTestBookedVisitData();
        var totalCount = 505;
        bookedVisitData[0].totalCount = totalCount;
        bookedVisitData[0].navigatePage = 3;

        var responseString = JSON.stringify(bookedVisitData);
        AjaxHelper.setupFakeServerTextResponse(server, expectedUrl, responseString);

        ApptTables.populateApptListTable();

        server.respond();

        var pageNumber1 = $("#appointmentListTablePagination_gotoPageInput").val();
        expect(pageNumber1).toBe("4");

        var totalPageText = $("#appointmentListTablePagination_gotoPageInput").parent().text();
        expect(totalPageText).toBe("Page  of 11");

        var startTimeFilterField = ApptTables.currentTable.getFilterField(ApptTables.dbColumnVisitStartTime);
        var startDatePicker = startTimeFilterField.textbox('textbox');
        var startDate = startDatePicker.val();
        expect(startDate).toBe(showDate(date));

        var expectedNextUrl = "rest/appointment/getBookedVisitsList?fromDate=" +
            "&toDate=&page=5&maxResults=50&orderBy=ASC" +
            "&sortBy=bv.scheduledStartTime" +
            "&search=%7B%22searchItems%22%3A%5B%5D%7D";

        bookedVisitData.push({
            id: 1283,
            visitName: "Sample Visit1",
            subjectFirstName: "Barfirst",
            subjectLastName: "Foolast",
            localId: "2914",
            scheduledStartTime: "Oct 5, 2014 12:00:00 PM",
            scheduledEndTime: "Oct 5, 2014 12:30:00 AM",
            appointmentStatus: "Cancellation",
            totalCount: totalCount
        });

        delete bookedVisitData[0].navigatePage;
        responseString = JSON.stringify(bookedVisitData);
        AjaxHelper.setupFakeServerTextResponse(server, expectedNextUrl, responseString);

        $("#appointmentListTablePagination_nextPageLink").click();

        server.respond();

        //one row with column names and 4 rows of visible and hidden expandable data
        expect($("#appointmentListTable").find("tbody tr").length).toBe(5);

        startTimeFilterField = ApptTables.currentTable.getFilterField(ApptTables.dbColumnVisitStartTime);
        startDatePicker = startTimeFilterField.textbox('textbox');
        startDate = startDatePicker.val();
        expect(startDate).toBe("");

        var pageNumber = $("#appointmentListTablePagination_gotoPageInput").val();
        expect(pageNumber).toBe("5");
    });

    xit("can identify filtered date mode", function(){
        FixtureHelper.loadSourceHtmlFixtureIntoDom("appt_list.html");

        ApptTables.loadVisitsIntoApptTablesWidget();

        var startDate = new Date();
        var endDate = startDate.addDays(1);
        ApptTables.currentTable.setFilterKeyValuePair(ApptTables.dbColumnVisitStartTime, startDate);
        ApptTables.currentTable.setFilterKeyValuePair(ApptTables.dbColumnVisitEndTime, endDate);

        var filteredDateMode = ApptTables.isDateFilterMode();
        expect(filteredDateMode).toBe(true);

        ApptTables.currentTable.setFilterKeyValuePair(ApptTables.dbColumnVisitEndTime, null);
        filteredDateMode = ApptTables.isDateFilterMode();
        expect(filteredDateMode).toBe(false);
    });

    xit("can identify date navigation mode", function(){
        FixtureHelper.loadSourceHtmlFixtureIntoDom("appt_list.html");

        ApptTables.loadVisitsIntoApptTablesWidget();

        var startDate = new Date();
        var endDate = showDate(startDate.addDays(1));
        startDate = showDate(startDate);

        ApptTables.currentTable.setFilterKeyValuePair(ApptTables.dbColumnVisitStartTime, null);
        ApptTables.currentTable.setFilterKeyValuePair(ApptTables.dbColumnStudyLocalId, "1234");
        var filteredDateMode = ApptTables.isNavigationMode();
        expect(filteredDateMode).toBe(false);

        ApptTables.currentTable.setFilterKeyValuePair(ApptTables.dbColumnVisitStartTime, startDate);
        ApptTables.currentTable.removeFilterKeyValuePair(ApptTables.dbColumnStudyLocalId);
        filteredDateMode = ApptTables.isNavigationMode();
        expect(filteredDateMode).toBe(true);

        ApptTables.currentTable.setFilterKeyValuePair(ApptTables.dbColumnVisitEndTime, endDate);
        filteredDateMode = ApptTables.isNavigationMode();
        expect(filteredDateMode).toBe(false);
    });

    function getTestBookedVisitData(){
        var bookedVisitData = [{
            id: 7485,
            visitName: "Sample Visit2",
            subjectFirstName: "Foobarfirst",
            subjectLastName: "Foobarlast",
            localId: "3749",
            scheduledStartTime: "Jan 1, 2005 11:00:00 AM",
            scheduledEndTime: "Jan 1, 2005 11:45:00 AM",
            appointmentStatus: "Cancellation"
        }];

        return bookedVisitData;
    }

    xit("navigates after filtering", function(){
        FixtureHelper.loadSourceHtmlFixtureIntoDom("appt_list.html");

        ApptTables.loadVisitsIntoApptTablesWidget();

        var server = UtilHelper.getFakeServer();

        var expectedFilteringUrl = "rest/appointment/getBookedVisitsList?fromDate="  +
            "&toDate=&page=1&maxResults=50&orderBy=ASC&sortBy=bv.scheduledStartTime" +
            "&search=%7B%22searchItems%22%3A%5B%7B%22key%22%3A%22apSt.name%22%2C%22value%22%3A%22Scheduled%22%7D%5D%7D";

        var filteredBookedVisitData = getTestBookedVisitData();
        var filterResponseString = JSON.stringify(filteredBookedVisitData);

        AjaxHelper.setupFakeServerTextResponse(server, expectedFilteringUrl, filterResponseString);

        ApptTables.currentTable.removeFilterKeyValuePair(ApptTables.dbColumnVisitStartTime);
        ApptTables.currentTable.setFilterKeyValuePair(ApptTables.dbColumnVisitStatus, "Scheduled");

        ApptTables.populateApptListTable();

        server.respond();

        //one row with column names and 2 rows of visible and hidden expandable data
        expect($("#appointmentListTable").find("tbody tr").length).toBe(3);

        var startDate = new Date();

        var expectedNavigationUrl = "rest/appointment/getBookedVisitsList?fromDate=" +
            "&toDate=" + showDate(startDate) + "&page=1&maxResults=50&orderBy=ASC&sortBy=bv.scheduledStartTime" +
            "&search=%7B%22searchItems%22%3A%5B%7B%22key%22%3A%22apSt.name%22%2C%22value%22%3A%22Scheduled%22%7D%5D%7D";

        filteredBookedVisitData = getTestBookedVisitData();
        filteredBookedVisitData.push({
            id: 2374,
            visitName: "Visit ABC",
            subjectFirstName: "Bar First",
            subjectLastName: "Bar Last",
            localId: "3749",
            scheduledStartTime: "Jun 7, 2012 10:15:00 AM",
            scheduledEndTime: "Jun 7, 2012 10:45:00 AM",
            appointmentStatus: "scheduled"
        });

        filterResponseString = JSON.stringify(filteredBookedVisitData);
        AjaxHelper.setupFakeServerTextResponse(server, expectedNavigationUrl, filterResponseString);

        ApptTables.currentTable.setFilterKeyValuePair(ApptTables.dbColumnVisitStartTime, showDate(startDate));

        ApptTables.populateApptListTable();

        server.respond();

        //one row with column names and 2 rows of visible and hidden expandable data
        expect($("#appointmentListTable").find("tbody tr").length).toBe(5);
    });
});

