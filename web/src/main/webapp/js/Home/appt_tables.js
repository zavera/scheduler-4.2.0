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

var ApptTables = {};

ApptTables.dbColumnVisitStartTime = "bv.scheduledStartTime";
ApptTables.dbColumnVisitEndTime = "bv.scheduledEndTime";
ApptTables.dbColumnVisitSubjectFirstName = "s.firstName";
ApptTables.dbColumnVisitSubjectLastName = "s.lastName";
ApptTables.dbColumnVisitMrn = "sm.mrn";
ApptTables.dbColumnStudyLocalId = "st.localId";
ApptTables.dbColumnVisitName = "vt.name";
ApptTables.dbColumnVisitStatus = "apSt.name";

ApptTables.uiColumnVisitStartTime = "Scheduled Start";
ApptTables.uiColumnVisitEndTime = "Scheduled End";
ApptTables.uiColumnVisitSubjectFirstName = "Subject First Name";
ApptTables.uiColumnVisitSubjectLastName = "Subject Last Name";
ApptTables.uiColumnVisitMrn = "MRN";
ApptTables.uiColumnStudyLocalId = "Local ID";
ApptTables.uiColumnVisitName = "Visit Name";
ApptTables.uiColumnVisitStatus = "Status";

ApptTables.tableName = "appointmentListTable";

ApptTables.pushColumns = function(columns) {
    if (UserRoleUtil.userIsSuperAdmin()) {
        columns.push(new Column({
            width: 2,
            extractDataFunction: function (val) {
                return val.id;
            },
            columnType: Column.Expandable
        }));
    }

    columns.push(new Column({
        dbColumn: ApptTables.dbColumnVisitStartTime,
        columnName: ApptTables.uiColumnVisitStartTime,
        columnType: Column.Text,
        width:13,
        defaultSortOrder: Column.ASC, // single presence of this option
                                      // among Columns indicates initial sort
        extractDataFunction: function (val) {return showDateTime(val.scheduledStartTime);},
        filterPrompt: "Scheduled Start",
        filter: {
            name: ApptTables.dbColumnVisitStartTime,
            inputType: InputField.Type.Date,
            onFilter: ApptTables.populateApptListTable,
            ignore: true
        }
    }));

    columns.push(new Column({
        dbColumn: ApptTables.dbColumnVisitEndTime,
        columnName: ApptTables.uiColumnVisitEndTime,
        width:12,
        extractDataFunction: function (val) {return showDateTime(val.scheduledEndTime);},
        filterPrompt: "Scheduled End",
        filter: {
            name: ApptTables.dbColumnVisitEndTime,
            inputType: InputField.Type.Date,
            onFilter:  ApptTables.populateApptListTable,
            ignore: true
        }
    }));

    columns.push(new Column({
        columnName: ApptTables.uiColumnVisitSubjectLastName,
        width:14,
        extractDataFunction: function (val) {return val.subjectLastName;},
        filterPrompt: "Last Name",
        filter: {
            type: Filter.Type.EXACT,
            name: ApptTables.dbColumnVisitSubjectLastName,
            onFilter: ApptTables.clearDatesAndPopulateTable
        }
    }));

    columns.push(new Column({
        columnName: ApptTables.uiColumnVisitSubjectFirstName,
        width:14,
        extractDataFunction: function (val) {return val.subjectFirstName;},
        filterPrompt: "First Name",
        filter: {
            type: Filter.Type.EXACT,
            name: ApptTables.dbColumnVisitSubjectFirstName,
            onFilter: ApptTables.clearDatesAndPopulateTable
        }
    }));

    columns.push(new Column({
        columnName: ApptTables.uiColumnVisitMrn,
        width:9,
        extractDataFunction: function (val) {return val.subjectMRN;},
        filter: {
            type: Filter.Type.EXACT,
            name: ApptTables.dbColumnVisitMrn,
            onFilter: ApptTables.clearDatesAndPopulateTable
        }
    }));

    columns.push(new Column({
        dbColumn: ApptTables.dbColumnStudyLocalId,
        columnName: ApptTables.uiColumnStudyLocalId,
        width:8,
        extractDataFunction: function (val) {return val.localId;},
        filter: {
            name: ApptTables.dbColumnStudyLocalId,
            onFilter: ApptTables.clearDatesAndPopulateTable
        }
    }));

    columns.push(new Column({
        dbColumn: ApptTables.dbColumnVisitName,
        columnName: ApptTables.uiColumnVisitName,
        width:11,
        extractDataFunction: function (val) {return val.visitName;},
        filter: {
            name: ApptTables.dbColumnVisitName,
            onFilter: ApptTables.clearDatesAndPopulateTable
        }
    }));
    columns.push(new Column({
        dbColumn: ApptTables.dbColumnVisitStatus,
        columnName: ApptTables.uiColumnVisitStatus,
        width:9,
        extractDataFunction: function (val) {return val.appointmentStatus;},
        filterPrompt: "Status",
        filter: {
            name: ApptTables.dbColumnVisitStatus,
            onFilter: ApptTables.clearDatesAndPopulateTable
        }
    }));

    columns.push(new Column({
        columnType: Column.Hyperlink,
        rowElementClass: 'editColumn',
        width:8,
        rowElementHyperLink: function (val) {
            return 'getBookedVisitDetails(' + val.id + ')';
        },
        extractDataFunction: function () {
            return " View Detail ";
        }
    }));
};

ApptTables.expandFn = function (container, visitId) {

    $.getJSON("rest/appointment/getScheduledVisitHistory?id=" + visitId, function (data) {

        var list = $('<div class="expandContent visitHistory"></div>').appendTo(container);

        var headingElement = $('<div class="heading"></div>').appendTo(list);
        $('<div class="column1"></div>').appendTo(headingElement);
        $('<div class="column2"><div>Appointment History:</div></div>').appendTo(headingElement);

        data.bookedVisitActivity.forEach(function (element) {
            var listElement = $('<div style=""></div>').appendTo(list);
            $('<div class="column1"></div>').appendTo(listElement);
            $('<div class="column2 highlight"><div>' + element.action + ':</div></div>').appendTo(listElement);
            $('<div class="column3"><div>' + showDateTime(element.date) + '</div></div>').appendTo(listElement);
            var userString = element.user ? "by " + element.user : "user data not available";
            $('<div class="column4"><div>' + userString + '</div></div>').appendTo(listElement);
        });
    });
};

ApptTables.loadVisitsIntoApptTablesWidget = function() {

    var columns = [];
    ApptTables.pushColumns(columns);

    ApptTables.currentTable = new SchedulerTables({
        tableId: ApptTables.tableName,
        columns: columns,
        reloadFn: ApptTables.clearDatesAndPopulateTable,
        groupBy: {
            columnName: ApptTables.uiColumnVisitStartTime,
            isNewGroup: function(oldValue, newValue) {
                var oldDate = showDate(oldValue);
                var newDate = showDate(newValue);
                return oldDate !== newDate;
            },
            render: function (value) {
                return showDate(value);
            }
        },
        expandFn: ApptTables.expandFn
    });

    ApptTables.currentTable.generateTable(
        [],
        50,
        ApptTables.tableName + "Pagination",
        ApptTables.clearDatesAndPopulateTable,
        ApptTables.clearDatesAndPopulateTable
    );

    ApptTables.setupTimeFilterFields();
    ApptTables.populateApptListTable();
};

ApptTables.adjustDateMinMax= function() {
    var startTimeFilterField = ApptTables.currentTable.getFilterField(ApptTables.dbColumnVisitStartTime);
    var endTimeFilterField = ApptTables.currentTable.getFilterField(ApptTables.dbColumnVisitEndTime);

    var startDate = startTimeFilterField.textbox('textbox').val();
    var endDate = endTimeFilterField.textbox('textbox').val();

    var startDatePicker = startTimeFilterField.textbox('textbox');
    var endDatePicker = endTimeFilterField.textbox('textbox');

    if(startDate !== ApptTables.uiColumnVisitStartTime) {
        endDatePicker.datepicker("option", "defaultDate", startDate);
        endDatePicker.datepicker("option", "minDate", startDate);
    }
    else {
        endDatePicker.datepicker("option", "minDate", null);
    }

    if(endDate !== ApptTables.uiColumnVisitEndTime) {
        startDatePicker.datepicker("option", "maxDate", endDate);
        startDatePicker.datepicker("option", "defaultDate", endDate);
    }
    else {
        startDatePicker.datepicker("option", "maxDate", null);
    }
};

ApptTables.setupTimeFilterFields = function() {
    var currentDate = new Date();
    var startTimeFilterField = ApptTables.currentTable.getFilterField(ApptTables.dbColumnVisitStartTime);
    var endTimeFilterField = ApptTables.currentTable.getFilterField(ApptTables.dbColumnVisitEndTime);

    if(startTimeFilterField !== null) {
        var startDate = showDate(currentDate);
        startTimeFilterField.textbox('textbox').val(startDate);

        // options.value is done here so that the textbox value is not cleared on focus
        startTimeFilterField.textbox('options').value = startDate;
        startTimeFilterField.textbox('textbox').removeClass("textbox-prompt");
        startTimeFilterField.textbox('getIcon', 0).css('visibility', 'visible');
        ApptTables.currentTable.setFilterKeyValuePair(ApptTables.dbColumnVisitStartTime, startDate);

        var startDatePicker = startTimeFilterField.textbox('textbox');
        var endDatePicker = endTimeFilterField.textbox('textbox');

        endDatePicker.datepicker("option", "minDate", startDate);

        startTimeFilterField.textbox("getIcon", 0).click(function() {
            startDatePicker.val("");
            ApptTables.adjustDateMinMax();
        });

        endTimeFilterField.textbox("getIcon", 0).click(function() {
            endDatePicker.val("");
            ApptTables.adjustDateMinMax();
        });

        var prepend = true;
        WidgetUtil.addSelectFunctionToDatePicker(startDatePicker, ApptTables.adjustDateMinMax, prepend);
        WidgetUtil.addSelectFunctionToDatePicker(endDatePicker, ApptTables.adjustDateMinMax, prepend);
    }
};

ApptTables.populateApptListTable = function() {

    var whichTable = ApptTables.currentTable;

    var startDateFilterObj = whichTable.getFilterObjByName(ApptTables.dbColumnVisitStartTime);
    var endDateFilterObj = whichTable.getFilterObjByName(ApptTables.dbColumnVisitEndTime);

    var fromDate = "";
    var toDate = "";

    var filtersMinusTime = whichTable.getAllFilterKeyValuePairs();

    if(startDateFilterObj && endDateFilterObj) {
        fromDate = startDateFilterObj.value;
    }
    else if(startDateFilterObj) {
        toDate = startDateFilterObj.value;
    }

    if(endDateFilterObj) {
        toDate = endDateFilterObj.value;
    }

    var searchItemsList = {searchItems: filtersMinusTime};
    var searchQuery = JSON.stringify(searchItemsList);

    var restUrl = "rest/appointment/getBookedVisitsList" +
        "?fromDate=" + fromDate +
        "&toDate=" + toDate +
        "&page=" + whichTable.getCurrentPage() +
        "&maxResults=" + whichTable.getRecordsPerPage() +
        "&orderBy=" + (whichTable.getSortOrder() || Column.ASC) +
        "&sortBy=" + (whichTable.getSortColumn()) +
        "&search=" + encodeURIComponent(searchQuery);

    //keep track of the current rest URL in case there are multiple responses pending
    //so that if the responses come back in different order the table is updated using
    //the response from the last request
    ApptTables.currentRestUrl = restUrl;

    var loadingIndicator = setTimeout(function() {
        $('#table_loading').css({display: 'block'});
    }, 2000);

    $.getJSON(restUrl, function (data) {
        if(ApptTables.currentRestUrl === restUrl) {
            whichTable.refreshTableBody(data);
        }
    }).always(function () {
        clearTimeout(loadingIndicator);
        $('#table_loading').css({display: 'none'});
    });
};

ApptTables.resetToPageOne = function() {
    ApptTables.currentTable.setCurrentPage(1);
    ApptTables.populateApptListTable();
};

ApptTables.isDateFilterMode = function() {
    var startDateFilterObj = ApptTables.currentTable.getFilterObjByName(ApptTables.dbColumnVisitStartTime);

    var endDateFilterObj = ApptTables.currentTable.getFilterObjByName(ApptTables.dbColumnVisitEndTime);
    var filterMode = false;

    var dateRange = startDateFilterObj !== null && endDateFilterObj !== null;

    if (dateRange){
        filterMode = true;
    }

    return filterMode;
};

ApptTables.isNavigationMode = function() {
    var startDateFilterObj = ApptTables.currentTable.getFilterObjByName(ApptTables.dbColumnVisitStartTime);
    var endDateFilterObj = ApptTables.currentTable.getFilterObjByName(ApptTables.dbColumnVisitEndTime);
    var allFilters = ApptTables.currentTable.getAllFilterKeyValuePairs();

    var navMode = false;

    var startDateOnly = startDateFilterObj && !endDateFilterObj;
    var endDateOnly = !startDateFilterObj && endDateFilterObj;

    if ((startDateOnly || endDateOnly) && allFilters.length === 0){
        navMode = true;
    }

    return navMode;
};

ApptTables.clearStartAndEndDateValues = function(){
    if(!ApptTables.isDateFilterMode()) {
        var startTimeFilterField = ApptTables.currentTable.getFilterField(ApptTables.dbColumnVisitStartTime);
        var startDatePicker = startTimeFilterField.textbox('textbox');
        startTimeFilterField.textbox("options").value = "";
        startDatePicker.val("");
        ApptTables.currentTable.setFilterKeyValuePair(ApptTables.dbColumnVisitStartTime, null);
        startTimeFilterField.textbox('getIcon', 0).css('visibility', 'hidden');
        startDatePicker.datepicker("option", "maxDate", null);

        var endTimeFilterField = ApptTables.currentTable.getFilterField(ApptTables.dbColumnVisitEndTime);
        var endDatePicker = endTimeFilterField.textbox('textbox');
        endDatePicker.val("");
        endTimeFilterField.textbox("options").value = "";
        ApptTables.currentTable.setFilterKeyValuePair(ApptTables.dbColumnVisitEndTime, null);
        endTimeFilterField.textbox('getIcon', 0).css('visibility', 'hidden');
        endDatePicker.datepicker("option", "minDate", null);
    }
};

ApptTables.clearDatesAndPopulateTable = function() {
    ApptTables.clearStartAndEndDateValues();
    ApptTables.populateApptListTable();
};

//The below 'commented' sourceURL specifies a name for this piece of code which
//will appear if debugging in the browser
//This is needed because this file is loaded programatically
//# sourceURL=appt_tables.js

/**
 *
 * Booked visit comments
 *
 */

var BookedVisitCommentsTable = {};

BookedVisitCommentsTable.id = "BookedVisitCommentsTable";

BookedVisitCommentsTable.createBookedVisitCommentsTable = function () {

    var columns = [];

    BookedVisitCommentsTable.pushAllColumns(columns);

    var args = {
        tableId: BookedVisitCommentsTable.id,
        columns: columns,
        reloadFn: function () {
            BookedVisitCommentsTable.populateBookedVisitCommentsTable();
        },
        fixedHeader: true,
        maxHeight: "300px",
        emptyDataMessage: " There are no comments to display. ",
        emptyDataMessageClass: "wizardEmptyTableMessage"
    };

    BookedVisitCommentsTable.currentTable = new SchedulerTables(args);

    BookedVisitCommentsTable.currentTable.generateTable(
        [],
        null,
        null,
        null,
        null
    );
};


BookedVisitCommentsTable.pushAllColumns = function (columns) {

    columns.push(new Column({
        columnName: "User ID",
        width: 30,
        extractDataFunction: function (val) {
            return showValue(val.ecommonsId);
        }
    }));

    columns.push(new Column({
        columnName: "Date",
        width: 30,
        extractDataFunction: function (val) {
            return showDateTime(val.commentDate);
        }
    }));

    columns.push(new Column({
        columnName: "Comment",
        width: 40,
        extractDataFunction: function (val) {
            return showValue(val.comment);
        }
    }));

};


BookedVisitCommentsTable.populateBookedVisitCommentsTable = function () {

    var url = "rest/appointment/getAppointmentComments?id=" + eventid;
    $.blockUI();
    $.getJSON(url, function (data) {
        BookedVisitCommentsTable.currentTable.refreshTableBody(data.visitComments, data.total);
        $.unblockUI();
    });

};


