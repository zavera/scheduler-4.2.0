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

// Sub-location closures

var SublocationClosureTable = {};

SublocationClosureTable.id = "sublocationClosureTable";
SublocationClosureTable.dbColumnSublocation = "sci.sublocation.name";
SublocationClosureTable.dbColumnStartDate = "sci.startTime";
SublocationClosureTable.dbColumnEndDate = "sci.endTime";
SublocationClosureTable.dbColumnReason = "sci.reason";
SublocationClosureTable.defaultPerPage = 50;

SublocationClosureTable.create = function() {
    var columns = [];

    SublocationClosureTable.pushAllColumns(columns);

    var args = {
        tableId: SublocationClosureTable.id,
        columns: columns,
        reloadFn: SublocationClosureTable.populate
    };

    SublocationClosureTable.currentTable = new SchedulerTables(args);

    SublocationClosureTable.currentTable.generateTable(
        [],
        SublocationClosureTable.defaultPerPage,
        PaginationHelper.getPaginationId(SublocationClosureTable.id),
        SublocationClosureTable.populate
    );

};


SublocationClosureTable.pushAllColumns = function(columns) {

    columns.push(new Column({
        dbColumn: SublocationClosureTable.dbColumnSublocation,
        columnName: "Sub-location",
        width: 20,
        defaultSortOrder: Column.ASC,
        extractDataFunction: function (val) {
            return val.sublocation;
        }
    }));

    columns.push(new Column({
        dbColumn: SublocationClosureTable.dbColumnStartDate,
        columnName: "Start Date",
        width: 20,
        extractDataFunction: function (val) {
            return showDateTime(val.startDate);
        }
    }));

    columns.push(new Column({
        dbColumn: SublocationClosureTable.dbColumnEndDate,
        columnName: "End Date",
        width: 20,
        extractDataFunction: function (val) {
            return showDateTime(val.endDate);
        }
    }));

    columns.push(new Column({
        dbColumn: SublocationClosureTable.dbColumnReason,
        columnName: "Reason",
        width: 20,
        extractDataFunction: function (val) {
            return val.reason;
        }
    }));

    columns.push(new Column({
        columnType: Column.Hyperlink,
        rowElementClass: 'editColumn',
        columnName: "",
        width: 20,
        rowElementHyperLink: function (val) {
            return 'javascript:deleteSublocationDialog(' + val.id + ')';
        },
        extractDataFunction: function () {
            return "Delete";
        }
    }));

};


SublocationClosureTable.populate = function() {

    var url = "rest/resource/getSublocationClosureIntervals" +
        "?page=" + SublocationClosureTable.currentTable.getCurrentPage() +
        "&maxResults=" + SublocationClosureTable.currentTable.getRecordsPerPage() +
        "&orderBy=" + (SublocationClosureTable.currentTable.getSortOrder() || Column.ASC) +
        "&sortBy=" + (SublocationClosureTable.currentTable.getSortColumn() || SublocationClosureTable.dbColumnSublocation);

    $.getJSON(url, function (data) {
        SublocationClosureTable.currentTable.refreshTableBody(data);
        // where is the blockUI() call?
        $.unblockUI();
    });

};


var DefaultAvailabilityTable = {};
DefaultAvailabilityTable.id = "defaultAvailabilityTable";
DefaultAvailabilityTable.dbDayOfWeek = "da.dayOfWeek";
DefaultAvailabilityTable.dbResourceStartTime = "da.startTime";
DefaultAvailabilityTable.dbResourceEndTime = "da.endTime";
DefaultAvailabilityTable.dbQuantity = "da.quantity";
DefaultAvailabilityTable.defaultPerPage = 10;
DefaultAvailabilityTable.resource_da_days = [];
DefaultAvailabilityTable.resource_daToRemove = "";


// track how many rows in the default-availability table are waiting for save/cancel
DefaultAvailabilityTable.defaultAvailabilityEditingCount = 0;

DefaultAvailabilityTable.createTable = function(){
    var columns = [];

    DefaultAvailabilityTable.pushSortColumns(columns);

    if (UserRoleUtil.userIsSuperAdminOrResourceManager()) {
        DefaultAvailabilityTable.pushEditColumns(columns);
    }

    var args = {
        tableId: DefaultAvailabilityTable.id,
        columns: columns,
        reloadFn: DefaultAvailabilityTable.populateTable
    };

    DefaultAvailabilityTable.currentTable = new SchedulerTables(args);

    DefaultAvailabilityTable.currentTable.generateTable(
        [],
        DefaultAvailabilityTable.defaultPerPage,
        PaginationHelper.getPaginationId(DefaultAvailabilityTable.id),
        DefaultAvailabilityTable.populateTable
    );

    DefaultAvailabilityTable.addDefaultAvailabilityRows();
};

DefaultAvailabilityTable.pushSortColumns = function(columns){
    columns.push(new Column({
        dbColumn: DefaultAvailabilityTable.dbDayOfWeek,
        columnName: "Day Of Week",
        width: 20,
        extractDataFunction: function (val) {
            return util_day(val.dayOfWeek);
        },
        defaultSortOrder: Column.ASC,
        editFunction : function(tdElement, val){
            var dayPickerOptions = "";
            for (var i = 1; i <= 7; i++) {
                dayPickerOptions += day_option_maybe_selected(i, val.dayOfWeek);
            }

            var elementId = "resource_defaultAvailability_dayPicker_" + val.id;
            var dayPickerHtml =
                '<select id="' + elementId + '">' +
                dayPickerOptions +
                '</select>' +
                '<span id="resource_da_dayValidation' + val.id + '" class="formElementRequired"></span>';
            $(tdElement).html(dayPickerHtml);
        }
    }));

    columns.push(new Column({
        dbColumn: DefaultAvailabilityTable.dbResourceStartTime,
        columnName: "Start Time",
        width: 20,
        extractDataFunction: function (val) {
            return val.startTime;
        },
        editFunction: function(tdElement, val){
            var startTimePickerHtml = '<input value="' + showStandardTime(val.startTime) + '"\n' +
                'type="text" ' +
                'id="resource_default_availability_start_time_' + val.id + '" ' +
                'style="background:white;" ' +
                'onclick="this.select()" />' +
                '<span id="resource_da_startTimeValidation_' + val.id + '" class="formElementRequired"></span>';
            $(tdElement).html(startTimePickerHtml);

            WidgetUtil.createTimepicker('#resource_default_availability_start_time_' + val.id, {
                display59: true,
                disabled: false
            });
            $("#resource_default_availability_start_time_" + val.id).timepicker('setTime', val.startTime);

        }
    }));

    columns.push(new Column({
        dbColumn: DefaultAvailabilityTable.dbResourceEndTime,
        columnName: "End Time",
        width: 20,
        extractDataFunction: function (val) {
            return val.endTime;
        },
        editFunction: function(tdElement, val){
            var endTimePickerHtml =
                '<input value="' + showStandardTime(val.endTime) + '"\n' +
                'type="text" ' +
                'id="resource_default_availability_end_time_' + val.id + '" ' +
                'style="background:white;" ' +
                'onclick="this.select()" />' +
                '<span id="resource_da_endTimeValidation_' + val.id + '"" class="formElementRequired"></span>';
            $(tdElement).html(endTimePickerHtml);

            WidgetUtil.createTimepicker('#resource_default_availability_end_time_' + val.id, {
                display59: true,
                disabled: false
            });
            $("#resource_default_availability_end_time_" + val.id).timepicker('setTime', val.endTime);
        }
    }));

    columns.push(new Column({
        dbColumn: DefaultAvailabilityTable.dbQuantity,
        columnName: "Quantity",
        width: 16,
        extractDataFunction: function (val) {
            return val.quantity;
        },
        editFunction: function(tdElement, val){
            var quantityPickerHtml = '<input id="resource_default_availability_number_' + val.id + '" value="' + val.quantity +
                '" class="numberUpTo999" type="text" value="1" style="width: 30px;" autocomplete="off" role="spinbutton">' +
                '<span id="resource_da_quantityValidation_' + val.id + '" class="formElementRequired"></span>';
            $(tdElement).html(quantityPickerHtml);

            DefaultAvailabilityTable.setupDefaultAvailabilityQuantitySpinner("resource_default_availability_number_" + val.id);
        }
    }));
};

DefaultAvailabilityTable.pushEditColumns = function(columns){
    columns.push(new Column({
        columnType: Column.Hyperlink,
        rowElementClass: 'editColumn',
        columnName: "",
        width: 12,
        rowElementOnClick: function (val, element) {
            var rowIndex =  DefaultAvailabilityTable.currentTable.getRowIndex(element);
            var tableRowElement = DefaultAvailabilityTable.currentTable.getDataRowElementAtIndex(rowIndex);
            DefaultAvailabilityTable.makeDefaultAvailabilityRowEditable(val.id, tableRowElement);
        },
        extractDataFunction: function () {
            return "Edit";
        },
        editFunction: function(tdElement, val){
            var tableRowId = $(tdElement).parent("tr").attr("id");
            var saveColumnsHtml =
                "<a href='javascript:DefaultAvailabilityTable.saveDefaultAvailability(" + val.id + ", \"edit\", \"" + tableRowId + "\")'> Save </a>";
            $(tdElement).html(saveColumnsHtml);
        }
    }));

    columns.push(new Column({
        columnType: Column.Hyperlink,
        rowElementClass: 'editColumn',
        columnName: "",
        width: 12,
        rowElementHyperLink: function (val) {
            return 'DefaultAvailabilityTable.deleteDefaultConfirmation(' + val.id + ')';
        },
        extractDataFunction: function () {
            return "Delete";
        },
        editFunction: function(tdElement, val){
            var tableRowId = $(tdElement).parent("tr").attr("id");
            var cancelColumnHtml = "<a href='javascript:DefaultAvailabilityTable.cancelRowEdit(" + val.id + ", \"" + tableRowId + "\")'> Cancel </a>";
            $(tdElement).html(cancelColumnHtml);

        }
    }));
};

DefaultAvailabilityTable.populateTable = function(){
    var url = "rest/resource/getResourceSchedules?resourceId=" + app_selectedResource.resourceId +
        "&page=" + DefaultAvailabilityTable.currentTable.getCurrentPage() +
        "&maxResults=" + DefaultAvailabilityTable.currentTable.getRecordsPerPage() +
        "&orderBy=" + (DefaultAvailabilityTable.currentTable.getSortOrder() || Column.ASC) +
        "&sortBy=" + (DefaultAvailabilityTable.currentTable.getSortColumn() || DefaultAvailabilityTable.dbDayOfWeek);

    $.getJSON(url, function (data) {
        DefaultAvailabilityTable.currentTable.refreshTableBody(data);
    });
};

DefaultAvailabilityTable.addDefaultAvailabilityRows = function(){
    // add row for adding a new default availability
    if (UserRoleUtil.userIsSuperAdminOrResourceManager()) {
        var addDefaultAvailabilityRow = DefaultAvailabilityTable.createAddDefaultAvailabilityRow();

        // row for validation complaints
        var addDefaultAvailabilityValidationRow = $("<tr></tr>", {"id": "addDaValidation"});

        var span = $("<span></span>", {"id": "resource_da_dayValidation_add"}).addClass("formElementRequired");
        $("<td></td>").append(span).appendTo(addDefaultAvailabilityValidationRow);

        span = $("<span></span>", {"id": "resource_da_startTimeValidation_add"}).addClass("formElementRequired");
        $("<td></td>").append(span).appendTo(addDefaultAvailabilityValidationRow);

        span = $("<span></span>", {"id": "resource_da_endTimeValidation_add"}).addClass("formElementRequired");
        $("<td></td>").append(span).appendTo(addDefaultAvailabilityValidationRow);

        span = $("<span></span>", {"id": "resource_da_quantityValidation_add"}).addClass("formElementRequired");
        $("<td></td>").append(span).appendTo(addDefaultAvailabilityValidationRow);

        $("<td></td>").appendTo(addDefaultAvailabilityValidationRow);
        $("<td></td>").appendTo(addDefaultAvailabilityValidationRow);

        var tableFooter = $("<tfoot></tfoot>");
        tableFooter.append(addDefaultAvailabilityRow);
        tableFooter.append(addDefaultAvailabilityValidationRow);
        DefaultAvailabilityTable.currentTable.addFooter(tableFooter);

        if (UserRoleUtil.userIsSuperAdminOrResourceManager()) {
            DefaultAvailabilityTable.setupAddDefaultAvailabilityWidgets();
        }
    }
};

DefaultAvailabilityTable.createAddDefaultAvailabilityRow = function () {

    var tr = $("<tr></tr>", {id: "addDefaultAvailabilityRow"});

    var dayPickerHtml = '<td><div id="rdda-div"></div>' +
        '<select id="resource_defaultAvailability_dayPicker_add" multiple="multiple"></select>' +
        '</div>' +
        '</td>';

    var startTimePickerHtml = '<td>' +
        '<input type="text" ' +
        'id="resource_default_availability_start_time_add" ' +
        'style="background:white;" ' +
        '/>' +
        '</td>\n';

    var endTimePickerHtml = '<td>' +
        '<input type="text" ' +
        'id="resource_default_availability_end_time_add" ' +
        'style="background:white;" ' +
        '/>' +
        '</td>\n';

    var editColumnsHtml =
        ' <td class="editColumn"> <a href="javascript:DefaultAvailabilityTable.addDefaultAvailability()"> Add New </a> </td>\n' +
        ' <td class="editColumn"> <a href="javascript:DefaultAvailabilityTable.cancelAddDefaultAvailability()"> Cancel </a> </td>\n';

    var quantityPickerHtml = "<td>" +
        '<input id="resource_default_availability_number_add" ' +
        'class="numberUpTo999" type="text" value="1" style="width: 30px;" ' +
        'autocomplete="off" role="spinbutton">' +
        '<span id="resource_da_quantityValidation" class="formElementRequired"></span>' +
        '</td>\n';

    var out = "";
    out += dayPickerHtml;
    out += startTimePickerHtml;
    out += endTimePickerHtml;
    out += quantityPickerHtml;
    out += editColumnsHtml;

    tr.html(out);

    return tr;
};

DefaultAvailabilityTable.addDefaultAvailability = function () {
    DefaultAvailabilityTable.saveDefaultAvailability('add', 'add');
};

DefaultAvailabilityTable.saveDefaultAvailability = function saveDefaultAvailability(id, saveMode, tableRowId) {

    DefaultAvailabilityTable.clearAddErrors();

    var elementId = "#resource_defaultAvailability_dayPicker_" + id;
    var selectedDay = $(elementId).val();

    var isValid = true;

    DefaultAvailabilityTable.resource_da_days = [];
    if (saveMode == 'edit') {
        DefaultAvailabilityTable.resource_da_days.push(selectedDay);
    }
    else {
        var selectedDays = $("#resource_defaultAvailability_dayPicker_add").multipleSelect('getSelects');
        if (selectedDays.length === 0) {
            DefaultAvailabilityTable.validationComplain('#resource_da_dayValidation_add', 'at least one day');
            isValid = false;
        }
        else {
            DefaultAvailabilityTable.resource_da_days = selectedDays;
        }
    }

    var resource_da_startTimeVal = $('#resource_default_availability_start_time_' + id).timepicker().val();
    var resource_da_endTimeVal = $('#resource_default_availability_end_time_' + id).timepicker().val();
    var resource_da_quantity = $("#resource_default_availability_number_" + id).spinner("value");

    if (!resource_da_startTimeVal || resource_da_startTimeVal.length == 0) {
        DefaultAvailabilityTable.validationComplain('#resource_da_startTimeValidation_' + id, 'a valid start time');
        isValid = false;
    }

    if (resource_da_endTimeVal == null) {
        DefaultAvailabilityTable.validationComplain('#resource_da_endTimeValidation_' + id, 'a valid end time');
        isValid = false;
    }

    var resourceStart = (new Date(new Date(2008, 0, 1).toDateString() + ' ' + resource_da_startTimeVal));
    var resourceEnd = (new Date(new Date(2008, 0, 1).toDateString() + ' ' + resource_da_endTimeVal));

    if (resourceStart >= resourceEnd) {
        DefaultAvailabilityTable.validationComplain('#resource_da_startTimeValidation_' + id, 'a valid time range');
        DefaultAvailabilityTable.validationComplain('#resource_da_endTimeValidation_' + id, 'a valid time range');
        isValid = false;
    }

    // the spinner widget returns null if the field's text is blank
    if (resource_da_quantity === null) {
        DefaultAvailabilityTable.validationComplain('#resource_da_quantityValidation_' + id, 'a valid quantity');
        isValid = false;
    }

    if (isValid == false) {
        return;
    }

    var saveAndReloadContinuation = function () {
        DefaultAvailabilityTable.defaultAvailabilitySaveAndReload(saveMode, resourceStart, resourceEnd, resource_da_quantity, id, tableRowId);
    };

    if (saveMode == 'add' && DefaultAvailabilityTable.defaultAvailabilityEditingCount > 0) {
        DialogsUtil.showMessageDialogWithOkCancelFns("Adding this item will cancel any " +
            "Default Availability edits in progress. OK to continue?",
            saveAndReloadContinuation);
    }
    else {
        saveAndReloadContinuation();
    }
};

DefaultAvailabilityTable.defaultAvailabilitySaveAndReload = function(saveMode, resourceStart, resourceEnd, resource_da_quantity, id, tableRowId) {

    if (id == 'add') {
        id = 0;
    }

    $('#resource_da_responseLoading').css({visibility: "visible"});
    var jsonData = JSON.stringify({
        id: id,
        resourceId: app_selectedResource.resourceId,
        days: DefaultAvailabilityTable.resource_da_days,
        startDate: resourceStart.valueOf(),
        endDate: resourceEnd.valueOf(),
        quantity: resource_da_quantity,
        override: false
    });

    var url = saveMode == 'add' ? "rest/resource/addDefaultAvailability" : "rest/resource/updateDefaultAvailability";

    $.post(url, {data: jsonData}, function (data) {
        $('#resource_da_responseLoading').css({visibility: "hidden"});
        var confirmationMessage = 'Default Availability ' + (saveMode == 'add' ? 'Added.' : 'Updated.');

        var onOk;
        if (saveMode == 'add') {
            onOk = function () {
                DefaultAvailabilityTable.populateTable();
            };
        }
        else {
            onOk = function () {
                DefaultAvailabilityTable.makeRowReadOnly(id, tableRowId);
                DefaultAvailabilityTable.decdecrementDefaultAvailabilityEditingCount();
            };
        }

        DefaultAvailabilityTable.clearAddDefaultAvailabilityWidgets();
        DialogsUtil.showMessageDialog(confirmationMessage, onOk);
    });
};

DefaultAvailabilityTable.deleteDefaultConfirmation = function (id) {
    DefaultAvailabilityTable.deleteDefaultDialog(id);
};

DefaultAvailabilityTable.deleteDefaultDialog = function (id) {
    var departureWarning = '';
    if (!DefaultAvailabilityTable.isCleanOfEditsOrAdd()) {
        departureWarning = "You will lose unsaved edits and additions."
    }
    var message = "Are you sure you want to delete this Default Availability? " + departureWarning;

    DialogsUtil.showMessageDialogWithOkFn(message,
        function () {
            DefaultAvailabilityTable.resource_daToRemove = id;
            $.getJSON("rest/resource/deleteDefaultSchedule?id=" + DefaultAvailabilityTable.resource_daToRemove, function (data) {
                if (data.result == true) {
                    $('#resource_deleteDefaultScheduleLoading').css({visibility: "hidden"});
                    DefaultAvailabilityTable.populateTable();
                }
                else {
                    var confirmationMessage = 'There was an error in deleting default availability.';
                    util_showMainMessage(confirmationMessage);
                }
            });
        },
        "Yes",
        "No",
        ""
    );
};

DefaultAvailabilityTable.cancelAddDefaultAvailability = function() {
    $('#addDefaultAvailabilityRow').html(DefaultAvailabilityTable.createAddDefaultAvailabilityRow().html());
    DefaultAvailabilityTable.setupAddDefaultAvailabilityWidgets();
};

DefaultAvailabilityTable.makeDefaultAvailabilityRowEditable = function(id, tableRow) {
    $.getJSON("rest/resource/getDefaultSchedule?id=" + id, function (val) {
        DefaultAvailabilityTable.currentTable.editRow(tableRow, val);
    });

    DefaultAvailabilityTable.incrementDefaultAvailabilityEditingCount();
};

DefaultAvailabilityTable.cancelRowEdit = function(id, tableRowId) {
    DefaultAvailabilityTable.makeRowReadOnly(id, tableRowId);
    DefaultAvailabilityTable.decdecrementDefaultAvailabilityEditingCount();
};

DefaultAvailabilityTable.makeRowReadOnly = function (id, tableRowId) {
    $.getJSON("rest/resource/getDefaultSchedule?id=" + id, function (val) {
        var tableRow = $("#" + tableRowId);
        DefaultAvailabilityTable.currentTable.updateTableRow(val, tableRow);
    });
};

DefaultAvailabilityTable.clearAddDefaultAvailabilityWidgets = function(){
    $("#resource_defaultAvailability_dayPicker_add").multipleSelect('setSelects', []);
    $("#resource_default_availability_start_time_add").timepicker('setTime', '00:00');
    $("#resource_default_availability_end_time_add").timepicker('setTime', '00:00');
    $("#resource_default_availability_number_add").spinner("value", 1);
};

DefaultAvailabilityTable.setupAddDefaultAvailabilityWidgets = function() {

    var dayPickerOptions = [];
    for (var i = 1; i <= 7; i++) {
        dayPickerOptions.push(day_option_for_multi_select(i));
    }

    var selectElement = $("#resource_defaultAvailability_dayPicker_add").html(dayPickerOptions.join(''));
    var options = {minimumCount: 1, width: "178px"};
    WidgetUtil.createMultiSelectComboBox(selectElement, options);

    DefaultAvailabilityTable.setupDefaultAvailabilityQuantitySpinner("resource_default_availability_number_add");

    WidgetUtil.createTimepicker('#resource_default_availability_start_time_add', {
        display59: true,
        disabled: false
    });
    $("#resource_default_availability_start_time_add").timepicker('setTime', '00:00');

    WidgetUtil.createTimepicker('#resource_default_availability_end_time_add', {
        display59: true,
        disabled: false
    });
    $("#resource_default_availability_end_time_add").timepicker('setTime', '00:00');

    DefaultAvailabilityTable.clearAddErrors();
};

DefaultAvailabilityTable.clearAddErrors = function () {
    $('#resource_da_startTimeValidation_add').css({visibility: "hidden"});
    $('#resource_da_endTimeValidation_add').css({visibility: "hidden"});
    $('#resource_da_dayValidation_add').css({visibility: "hidden"});
    $('#resource_da_quantityValidation_add').css({visibility: "hidden"});
    $('#addDaValidation').hide();
};

DefaultAvailabilityTable.validationComplain = function (elementId, type) {
    $('#addDaValidation').show();

    $(elementId).css({display: 'block'});
    $(elementId).text('Please enter ' + type + '.');
    $(elementId).css({opacity: 0.0, visibility: "visible"}).animate({opacity: 1.0});
};

DefaultAvailabilityTable.isDefaultAvailabilityAddRowPristine = function () {
    var addRow = $("#addDefaultAvailabilityRow");
    if (addRow[0] === undefined) {
        return true;
    }

    var selectedDays = $("#resource_defaultAvailability_dayPicker_add").multipleSelect('getSelects');

    var resource_da_startTimeVal = $('#resource_default_availability_start_time_add').timepicker().val();
    var resource_da_endTimeVal = $('#resource_default_availability_end_time_add').timepicker().val();
    var resource_da_quantity = $("#resource_default_availability_number_add").spinner("value");

    return (        selectedDays.length === 0
        && resource_da_quantity == 1
        && resource_da_endTimeVal == "00:00"
        && resource_da_startTimeVal == "00:00"
    );
};

DefaultAvailabilityTable.isCleanOfEditsOrAdd = function () {
    return DefaultAvailabilityTable.isDefaultAvailabilityAddRowPristine() && DefaultAvailabilityTable.defaultAvailabilityEditingCount == 0;
};

DefaultAvailabilityTable.setupDefaultAvailabilityQuantitySpinner = function(id) {
    var inputElement = $("#" + id);
    inputElement.spinner({
        "min": 0,
        "max": 999,
        "disabled": false
    });
};

DefaultAvailabilityTable.decdecrementDefaultAvailabilityEditingCount = function() {
    if (DefaultAvailabilityTable.defaultAvailabilityEditingCount > 0) {
        DefaultAvailabilityTable.defaultAvailabilityEditingCount--;
    }
};

DefaultAvailabilityTable.incrementDefaultAvailabilityEditingCount = function() {
    DefaultAvailabilityTable.defaultAvailabilityEditingCount++;
};

var TemporaryAdjustmentTable = {};
TemporaryAdjustmentTable.id = "temporaryAdjustmentTable";
TemporaryAdjustmentTable.dbStartTime =  "da.startTime";
TemporaryAdjustmentTable.dbEndTime =  "da.endTime";
TemporaryAdjustmentTable.dbQuantity = "da.quantity";
TemporaryAdjustmentTable.defaultPerPage = 10;
TemporaryAdjustmentTable.startDateTimeInputPrefix = "resource_ta_startDateTime_";
TemporaryAdjustmentTable.endDateTimeInputPrefix = "resource_ta_endDateTime_";

TemporaryAdjustmentTable.createTable = function(){
    var columns = [];


    TemporaryAdjustmentTable.pushSortColumns(columns);

    if (UserRoleUtil.userIsSuperAdminOrResourceManager()) {
        TemporaryAdjustmentTable.pushEditColumns(columns);
    }

    var args = {
        tableId: TemporaryAdjustmentTable.id,
        columns: columns,
        reloadFn: TemporaryAdjustmentTable.populateTable,
        emptyDataMessage: "No Temporary Adjustments Yet"
    };

    TemporaryAdjustmentTable.currentTable = new SchedulerTables(args);

    TemporaryAdjustmentTable.currentTable.generateTable(
        [],
        TemporaryAdjustmentTable.defaultPerPage,
        PaginationHelper.getPaginationId(TemporaryAdjustmentTable.id),
        TemporaryAdjustmentTable.populateTable
    );
};

TemporaryAdjustmentTable.pushSortColumns = function(columns){
    columns.push(new Column({
        columnName: "Start Day of Week",
        width: 15,
        extractDataFunction: function (val) {
            var startDayOfWeek = (new Date(val.startTime)).getDay();

            return dayOfWeekString[startDayOfWeek];
        }
    }));

    columns.push(new Column({
        dbColumn: TemporaryAdjustmentTable.dbStartTime,
        columnName: "Start Date",
        width: 20,
        rowElementClass: "dateContainer",
        extractDataFunction: function (val) {
            return new Date(val.startTime).format("mm/dd/yyyy, HH:MM");
        },
        defaultSortOrder: Column.ASC,
        editFunction: function(tdElement, val){
            var startDateTime = $(tdElement).text();

            var resourceTaStartDateTimeInputId = TemporaryAdjustmentTable.startDateTimeInputPrefix + val.id;

            $(tdElement).html(
            "<input type='text' " +
            "name='resource_ta_startDateTime'" +
            "id='" + resourceTaStartDateTimeInputId + "'" +
            "value='" + startDateTime + "' " +
            "placeholder='MM/DD/YYY HH:MM' />  " +
            "<span id='resource_ta_startDateValidation_" + val.id + "' class='formElementRequired'></span>" +
            "<span id='resource_ta_startTimeValidation_" + val.id + "' class='formElementRequired'></span>");
        }
    }));

    columns.push(new Column({
        columnName: "End Day of Week",
        width: 15,
        extractDataFunction: function (val) {
            var endDayOfWeek = (new Date(val.endTime)).getDay();

            return dayOfWeekString[endDayOfWeek];
        }
    }));

    columns.push(new Column({
        dbColumn: TemporaryAdjustmentTable.dbEndTime,
        columnName: "End Date",
        width: 20,
        rowElementClass: "dateContainer",
        extractDataFunction: function (val) {
            return new Date(val.endTime).format("mm/dd/yyyy, HH:MM");
        },
        editFunction: function(tdElement, val){
            var endDateTime = new Date(val.endTime).format("mm/dd/yyyy, HH:MM");

            var resourceTaEndDateTimeInputId = TemporaryAdjustmentTable.endDateTimeInputPrefix + val.id;
            var resourceTaEndDateTimeInputSelector = "#" + resourceTaEndDateTimeInputId;

            var resourceTaStartDateTimeInputId = TemporaryAdjustmentTable.startDateTimeInputPrefix + val.id;
            var resourceTaStartDateTimeInputSelector = "#" + resourceTaStartDateTimeInputId;

            $(tdElement).html(
                "<input type='text' " +
                "name='resource_ta_endDateTime'" +
                "id='" + resourceTaEndDateTimeInputId + "'" +
                "value='" + endDateTime + "' " +
                "placeholder='MM/DD/YYY HH:MM' />  " +
                "<span id='resource_ta_endDateValidation_" + val.id + "' class='formElementRequired'></span>" +
                "<span id='resource_ta_endTimeValidation_" + val.id + "' class='formElementRequired'></span>");

            // now is not a local variable. It is exposed as an attribute
            // of TemporaryAdjustment for testability
            TemporaryAdjustment.now = roundDateToQuarterHour(new Date());
            WidgetUtil.createDatetimepicker(
                resourceTaStartDateTimeInputSelector,
                {
                    showOn: "both",
                    minDate: TemporaryAdjustment.now,
                    onSelect: function (selectedStartDate) {
                        $(resourceTaEndDateTimeInputSelector).datetimepicker(
                            "option", "minDate", showDate(selectedStartDate)
                        );

                        TemporaryAdjustmentTable.resetTempAdStartDateMinTime(val.id, selectedStartDate);
                        TemporaryAdjustmentTable.onSelectDatetimepicker(val.id);
                    }
                },
                true
            );

            var startDateTime = $(resourceTaStartDateTimeInputSelector).val();
            $(resourceTaStartDateTimeInputSelector).datetimepicker('setTime', new Date(startDateTime));

            WidgetUtil.createDatetimepicker(
                resourceTaEndDateTimeInputSelector,
                {
                    showOn: "both",
                    minDate: startDateTime,
                    onSelect: function (selectedEndDate) {
                        TemporaryAdjustmentTable.resetTempAdjEndDateMinTime(val.id, selectedEndDate);
                        TemporaryAdjustmentTable.onSelectDatetimepicker(val.id);
                    }
                },
                true
            );

            $(resourceTaEndDateTimeInputSelector).datetimepicker('setDate', new Date(endDateTime));
        }
    }));

    columns.push(new Column({
        columnName: "Quantity",
        width: 10,
        extractDataFunction: function (val) {
            return val.quantity;
        },
        editFunction: function(tdElement, val){
            var quantity = val.quantity;
            $(tdElement).html("<input type='text' " +
                "min='0'" +
                "name='resource_ta_quantity' " +
                "id='resource_ta_quantity_" + val.id + "'" +
                "value='" + quantity + "' class='numberSpinner numberUpTo999' />  " +
                "<span id='resource_ta_quantityValidation_" + val.id + "' class='formElementRequired'></span>");

            $("#resource_ta_quantity_" + val.id).spinner({
                min: 0
            });
        }
    }));
};

TemporaryAdjustmentTable.pushEditColumns = function(columns){
    columns.push(new Column({
        columnType: Column.Hyperlink,
        rowElementClass: 'editColumn',
        columnName: "",
        width: 10,
        rowElementOnClick: function (val, element) {
            var rowIndex =  TemporaryAdjustmentTable.currentTable.getRowIndex(element);
            var tableRowElement = TemporaryAdjustmentTable.currentTable.getDataRowElementAtIndex(rowIndex);
            TemporaryAdjustmentTable.makeRowEditable(val.id, tableRowElement);
        },
        extractDataFunction: function () {
            return "Edit";
        },
        editFunction: function(tdElement, val){
            var tableRowId = $(tdElement).parent("tr").attr("id");
            var saveColumnsHtml =
                "<a href='javascript:TemporaryAdjustmentTable.updateTemporaryAdjustment(" + val.id + ", \"" + tableRowId + "\")'> Save </a>";
            $(tdElement).html(saveColumnsHtml);
        }
    }));

    columns.push(new Column({
        columnType: Column.Hyperlink,
        rowElementClass: 'editColumn',
        columnName: "",
        width: 10,
        rowElementHyperLink: function (val) {
            return 'TemporaryAdjustmentTable.deleteTempAdjustmentDialog(' + val.id + ')';
        },
        extractDataFunction: function () {
            return "Delete";
        },
        editFunction: function(tdElement, val){
            var tableRowId = $(tdElement).parent("tr").attr("id");
            var cancelColumnHtml = "<a href='javascript:TemporaryAdjustmentTable.cancelRowEdit(" + val.id + ", \"" + tableRowId + "\")'> Cancel </a>";
            $(tdElement).html(cancelColumnHtml);
        }
    }));
};

TemporaryAdjustmentTable.populateTable = function(){
    var url = "rest/resource/getTemporaryAdjustments?resourceId=" + app_selectedResource.resourceId +
        "&page=" + TemporaryAdjustmentTable.currentTable.getCurrentPage() +
        "&maxResults=" + TemporaryAdjustmentTable.currentTable.getRecordsPerPage() +
        "&orderBy=" + (TemporaryAdjustmentTable.currentTable.getSortOrder() || Column.ASC) +
        "&sortBy=" + (TemporaryAdjustmentTable.currentTable.getSortColumn() || TemporaryAdjustmentTable.dbStartTime);

    $.getJSON(url, function (data) {
        TemporaryAdjustmentTable.currentTable.refreshTableBody(data);
    });
};

TemporaryAdjustmentTable.makeRowEditable = function(id, tableRow){
    $.getJSON("rest/resource/getTemporaryAdjustment?id=" + id, function (val) {
        TemporaryAdjustmentTable.currentTable.editRow(tableRow, val);
    });
};

TemporaryAdjustmentTable.cancelRowEdit = function(id, tableRowId){
    TemporaryAdjustmentTable.makeRowReadOnly(id, tableRowId);
};

TemporaryAdjustmentTable.makeRowReadOnly = function (id, tableRowId){
    $.getJSON("rest/resource/getTemporaryAdjustment?id=" + id, function (val) {
        var tableRow = $("#" + tableRowId);
        TemporaryAdjustmentTable.currentTable.updateTableRow(val, tableRow);
    });
};

TemporaryAdjustmentTable.onSelectDatetimepicker = function (id) {

    var resourceTaStartDateTimeInputId = TemporaryAdjustmentTable.startDateTimeInputPrefix + id;

    var startDate = $("#" + resourceTaStartDateTimeInputId).val();
    var startDayOfWeek = (new Date(startDate)).getDay();
    startDayOfWeek = dayOfWeekString[startDayOfWeek];
    $("#resource_ta_startDayOfWeek_" + id).text(startDayOfWeek);


    var resourceTaEndDateTimeInputId = TemporaryAdjustmentTable.startDateTimeInputPrefix + id;

    var endDate = $("#" + resourceTaEndDateTimeInputId).val();
    var endDayOfWeek = (new Date(endDate)).getDay();
    endDayOfWeek = dayOfWeekString[endDayOfWeek];
    $("#resource_ta_endDayOfWeek_" + id).text(endDayOfWeek);
};

TemporaryAdjustmentTable.resetTempAdStartDateMinTime = function (id, selectedStartDate) {

    var nowHourAndMinutes = showTime(selectedStartDate);
    var currentStartDate = showDate(selectedStartDate);

    var resourceTaEndDateTimeInputId = TemporaryAdjustmentTable.endDateTimeInputPrefix + id;
    var endDateTimeElement = $("#" + resourceTaEndDateTimeInputId);
    var endDateValue = endDateTimeElement.val();
    var currentEndDate = showDate(endDateValue);
    var currentEndTime = showTime(endDateValue);

    if (currentStartDate >= currentEndDate
        && nowHourAndMinutes > currentEndTime) {

        endDateTimeElement.datetimepicker(
            "option", "minTime", nowHourAndMinutes
        ).datetimepicker(
            "setTime", nowHourAndMinutes
        );
    }
    else if (currentStartDate == currentEndDate
        && nowHourAndMinutes <= currentEndTime) {

        endDateTimeElement.datetimepicker(
            "option", "minTime", nowHourAndMinutes
        );
    }
    else if (currentStartDate < currentEndDate) {

        endDateTimeElement.datetimepicker(
            "option", "minTime", "00:00"
        );
    }
};

TemporaryAdjustmentTable.resetTempAdjEndDateMinTime = function (id, selectedEndDate) {

    var resourceTaStartDateTimeInputId = TemporaryAdjustmentTable.startDateTimeInputPrefix + id;

    var currentEndDate = showDate(selectedEndDate);
    var startDateElement = $("#" + resourceTaStartDateTimeInputId);
    var currentStartDate = showDate(startDateElement.val());
    var currentStartTime = showTime(startDateElement.val());

    var resourceTaEndDateTimeInputId = TemporaryAdjustmentTable.endDateTimeInputPrefix + id;
    var endDateTimeElement = $("#" + resourceTaEndDateTimeInputId);
    var endDateTime = showTime(endDateTimeElement.val());

    if (currentEndDate > currentStartDate) {
        endDateTimeElement.datetimepicker(
            "option", "minTime", "00:00"
        );
    }
    else if (currentEndDate == currentStartDate) {
        endDateTimeElement.datetimepicker(
            "option", "minTime", currentStartTime
        );

        endDateTimeElement.datetimepicker(
            "setTime", endDateTime
        );
    }
};

TemporaryAdjustmentTable.updateTemporaryAdjustment = function(id, tableRowId){
    var resource_ta_startDateTimeVal = new Date($("#resource_ta_startDateTime_" + id).val());
    var resource_ta_endDateTimeVal = new Date($("#resource_ta_endDateTime_" + id).val());

    var quantity = $('#resource_ta_quantity_' + id).val();

    var spanOvernight = true;
    if (resource_ta_startDateTimeVal.getDate() === resource_ta_endDateTimeVal.getDate()
        && resource_ta_startDateTimeVal.getMonth() === resource_ta_endDateTimeVal.getMonth()
        && resource_ta_startDateTimeVal.getFullYear() === resource_ta_endDateTimeVal.getFullYear()) {
        spanOvernight = false;
    }

    var formElements = {
        startDate: resource_ta_startDateTimeVal,
        endDate: resource_ta_endDateTimeVal,
        startTime: new Date(resource_ta_startDateTimeVal),
        endTime: new Date(resource_ta_endDateTimeVal),
        quantity: quantity
    };

    var validationElements = {
        startDateValidation: "#resource_ta_startDateValidation_" + id,
        endDateValidation: "#resource_ta_endDateValidation_" + id,
        startTimeValidation: "#resource_ta_startTimeValidation_" + id,
        endTimeValidation: "#resource_ta_endTimeValidation_" + id,
        quantityValidation: "#resource_ta_quantityValidation_" + id
    };

    if (!TemporaryAdjustment.validateSaveTempAdjustment(formElements, validationElements, spanOvernight)) {
        return;
    }

    var jsonData = JSON.stringify({
        id: id,
        resourceId: app_selectedResource.resourceId,
        startDate: new Date($("#resource_ta_startDateTime_" + id).val()).valueOf(),
        endDate: new Date($("#resource_ta_endDateTime_" + id).val()).valueOf(),
        quantity: formElements.quantity,
        override: true
    });

    var url = "rest/resource/updateTemporaryAdjustment";
    $.post(url, {data: jsonData}, function () {
        util_showMainMessage("Temporary Adjustments updated");
        TemporaryAdjustmentTable.makeRowReadOnly(id, tableRowId);
    });
};

TemporaryAdjustmentTable.deleteTempAdjustmentDialog = function(id) {
    DialogsUtil.showConfirmationDialog("#delete-adjustment-dialog-confirm", {
        buttons: {
            "Yes": function () {
                resource_taToRemove = id;
                $.getJSON("rest/resource/deleteTemporaryAdjustment?id=" + resource_taToRemove, function (data) {
                    if (data.result == true) {
                        $('#resource_deleteTempAdjLoading').css({visibility: "hidden"});
                        var confirmationMessage = 'Temporary Adjustment Deleted.';
                        util_showMainMessage(confirmationMessage);
                        TemporaryAdjustmentTable.populateTable();
                    }
                    else {
                        var confirmationMessage = 'There was an error in deleting temporary adjustment.';
                        util_showMainMessage(confirmationMessage);
                    }
                });
                $(this).dialog("close");
            },
            "No": function () {
                $(this).dialog("close");
            }
        }
    });
};

var AlternateResourcesTable = {};
AlternateResourcesTable.id = "alternateResourcesTable";
AlternateResourcesTable.defaultPerPage = 10;
AlternateResourcesTable.dbResourceName = "ra.alternateResource.name";
AlternateResourcesTable.dbResourceType = "ra.alternateResource.resourceType";
AlternateResourcesTable.dbAltResourceSublocation = "rs.sublocation.name";


AlternateResourcesTable.createTable = function(){
    var columns = [];

    AlternateResourcesTable.pushAllColumns(columns);

    var args = {
        tableId: AlternateResourcesTable.id,
        columns: columns,
        reloadFn: AlternateResourcesTable.populateTable
    };

    AlternateResourcesTable.currentTable = new SchedulerTables(args);

    AlternateResourcesTable.currentTable.generateTable(
        [],
        AlternateResourcesTable.defaultPerPage,
        PaginationHelper.getPaginationId(AlternateResourcesTable.id),
        AlternateResourcesTable.populateTable
    );

    AlternateResourcesTable.addAlternateResourcesRows();
};


AlternateResourcesTable.pushAllColumns = function(columns){
    AlternateResourcesTable.pushSortColumns(columns);

    if (UserRoleUtil.userIsSuperAdminOrResourceManager()) {
        AlternateResourcesTable.pushEditColumns(columns);

        columns.push(new Column({
            width: 10
        }));
    }
};

AlternateResourcesTable.pushSortColumns = function(columns){
    columns.push(new Column({
        dbColumn: AlternateResourcesTable.dbResourceName,
        columnName: "Resource Name",
        width: 25,
        extractDataFunction: function (val) {
            return val.resource;
        },
        defaultSortOrder: Column.ASC
    }));

    columns.push(new Column({
        dbColumn: AlternateResourcesTable.dbResourceType,
        columnName: "Resource Type",
        width: 20,
        extractDataFunction: function (val) {
            return val.resourceType;
        }
    }));

    columns.push(new Column({
        dbColumn: AlternateResourcesTable.dbAltResourceSublocation,
        columnName: "Sub-location",
        width: 25,
        extractDataFunction: function (val) {
            return val.sublocation;
        }
    }));
};

AlternateResourcesTable.pushEditColumns = function(columns) {
    columns.push(new Column({
        columnType: Column.Hyperlink,
        rowElementClass: 'editColumn',
        columnName: "",
        width: 10,
        rowElementHyperLink: function (val) {
            return 'AlternateResourcesTable.deleteAlternateDialog(' + val.id + ')';
        },
        extractDataFunction: function () {
            return "Delete";
        }
    }));
};

AlternateResourcesTable.populateTable = function(){
    var url = "rest/resource/getResourceAlternates?resourceId=" + app_selectedResource.resourceId +
        "&page=" + AlternateResourcesTable.currentTable.getCurrentPage() +
        "&maxResults=" + AlternateResourcesTable.currentTable.getRecordsPerPage() +
        "&orderBy=" + (AlternateResourcesTable.currentTable.getSortOrder() || Column.ASC) +
        "&sortBy=" + (AlternateResourcesTable.currentTable.getSortColumn() || AlternateResourcesTable.dbResourceName);

    $.getJSON(url, function (data) {
        AlternateResourcesTable.currentTable.refreshTableBody(JSON.parse(data.data));
        if (UserRoleUtil.userIsSuperAdminOrResourceManager()) {
            AlternateResourcesTable.updateAlternateResourceMultiSelect();
        }
    });
};

AlternateResourcesTable.addAlternateResourcesRows = function(){
    if (UserRoleUtil.userIsSuperAdminOrResourceManager()) {
        var addAlternateResourcesRow = $("<tr></tr>");

        var addAlternateResourcesDataDiv = $("<div></div>", {"id": "ralt-div"});
        addAlternateResourcesDataDiv.append($("<select></select>",
            {"id": "addAlternateResourcesData", "multiple": "multiple"}));
        $("<td></td>").append(addAlternateResourcesDataDiv).appendTo(addAlternateResourcesRow);

        addAlternateResourcesRow.append("<td>&nbsp;</td>");
        addAlternateResourcesRow.append("<td>&nbsp;</td>");
        $("<td></td>").addClass("editColumn").append($("<a></a>", {"id": "addAlternateResources", "href": "#"}).text("Add New")).appendTo(addAlternateResourcesRow);
        $("<td></td>").addClass("editColumn").append($("<a></a>", {"id": "cancelAddAlternateResources", "href": "#"}).text("Cancel")).appendTo(addAlternateResourcesRow);

        var addAlternateResourcesValidationRow = $("<tr></tr>", {"id": "addAlternateResourceDataValidationDiv"});
        $("<td></td>", {"colspan": "5"}).append(
            $("<span></span>", {"id": "addAlternateResourceDataValidation", "class": "formElementRequired"}))
            .appendTo(addAlternateResourcesValidationRow);

        var tableFooter = $("<tfoot></tfoot>");
        tableFooter.append(addAlternateResourcesRow);
        tableFooter.append(addAlternateResourcesValidationRow);
        AlternateResourcesTable.currentTable.addFooter(tableFooter);

        $("#addAlternateResources").on('click', AlternateResourcesTable.addResourceAlternatives);
        $("#cancelAddAlternateResources").on('click', AlternateResourcesTable.clearAlternateResourceSelection);

        if (UserRoleUtil.userIsSuperAdminOrResourceManager()) {
            AlternateResourcesTable.createAddAlternateResourcesComboBox();
        }
    }
};

AlternateResourcesTable.clearAlternateResourceSelection = function() {
    $('#addAlternateResourcesData').multipleSelect("uncheckAll");
    AlternateResourcesTable.clearAlternativesError();
    return false;
};

AlternateResourcesTable.showAlternativesError = function() {
    $('#addAlternateResourceDataValidationDiv').css({display: "inline-block"});
    showErrorInValidationSpan('#addAlternateResourceDataValidation',
        "Please choose a resource.");
};

AlternateResourcesTable.clearAlternativesError = function() {
    $('#addAlternateResourceDataValidationDiv').css({display: "none"});
};

AlternateResourcesTable.addResourceAlternatives = function() {
    AlternateResourcesTable.assignAlternatives();
    return false;
};

AlternateResourcesTable.assignAlternatives = function() {
    // TODO: convert to a list of integers, not strings
    var selectedAlternateResourceValues = $("#addAlternateResourcesData").multipleSelect('getSelects');
    if (selectedAlternateResourceValues.length === 0) {
        AlternateResourcesTable.showAlternativesError();
    }
    else {
        AlternateResourcesTable.addAlternateDialog(selectedAlternateResourceValues);
        AlternateResourcesTable.clearAlternativesError();
    }
};

AlternateResourcesTable.addAlternateDialog = function(alternateResourcesId) {
    var finalList = alternateResourcesId.toString().split(',').map(Number);
    DialogsUtil.showConfirmationDialog("#add-alternate-dialog-confirm", {
        buttons: {
            "Yes": function () {
                var jsonData = JSON.stringify({resourceId: app_selectedResource.resourceId, alternatives: finalList});
                $.post("rest/resource/assignResourceAlternatives", {data: jsonData}, function (data) {
                    $('#resource_ra_responseLoading').css({visibility: "hidden"});
                    AlternateResourcesTable.populateTable();
                    AlternateResourcesTable.clearAlternateResourceSelection();
                });
                $(this).dialog("close");
            },
            "No": function () {
                $(this).dialog("close");
            }
        }
    });
};

AlternateResourcesTable.deleteAlternateDialog = function(id) {
    DialogsUtil.showConfirmationDialog("#delete-alternate-dialog-confirm", {
        buttons: {
            "Yes": function () {
                $.getJSON("rest/resource/removeResourceAlternative?resourceId=" + app_selectedResource.resourceId + "&alternateResourceId=" + id, function (data) {
                    if (data.result == true) {
                        $('#resource_removeAlternativeLoading').css({visibility: "hidden"});
                        AlternateResourcesTable.populateTable();
                    }
                    else {
                        $(this).dialog("close");
                        var confirmationMessage = 'There was an error in removing the alternate resource.';
                        util_showMainMessage(confirmationMessage);
                    }
                });
                $(this).dialog("close");
            },
            "No": function () {
                $(this).dialog("close");
            }
        }
    });
};

AlternateResourcesTable.getAlternateResources = function(callback){
    var sortByAlternateResourceName = "r.name";

    //if no resource is currently selected (i.e. 'new' mode) then get the list of all active resources
    // since any of them can be set as alternative
    var url = "rest/resource/getResourcesData?" +
        "&page=1" +
        "&maxResults=1000000" +
        "&orderBy=ASC" +
        "&sortBy=" +
        sortByAlternateResourceName +
        "&status=active";

    // if a resource is currently selected (i.e. edit mode) then look for alternate resources
    // allowed for this resource
    if (app_selectedResource) {
        url = "rest/resource/getAlternateResources?" +
            "resourceId=" + app_selectedResource.resourceId +
            "&page=1" +
            "&maxResults=1000000" +
            "&orderBy=ASC" +
            "&sortBy=" +
            sortByAlternateResourceName;
    }

    $.getJSON(url, function (data) {
       if(callback) {
           callback(data);
       }
    });
};

AlternateResourcesTable.createAddAlternateResourcesComboBox = function() {
     AlternateResourcesTable.getAlternateResources(function(data){
        var selectElement = $("#addAlternateResourcesData");
        AlternateResourcesTable.setAlternateResourceMultiSelectOptions(data, selectElement);
        var minimumCount = 4;
        var options = {minimumCount: minimumCount, width: "268px"};
        WidgetUtil.createMultiSelectComboBox(selectElement, options);
    });
};

// Get the list of *allowed* alternative resources for the resource with the specified ID (if specified)
AlternateResourcesTable.setAlternateResourceMultiSelectOptions = function(alternateResources, selectElement) {
    for (var i = 0, len = alternateResources.length; i < len; i++) {
        var option = $("<option></option>");
        option.val(alternateResources[i].id);
        option.text(alternateResources[i].resource + " (" + alternateResources[i].resourceType + ")");
        $(selectElement).append(option);
    }
};

AlternateResourcesTable.updateAlternateResourceMultiSelect = function(){
    AlternateResourcesTable.getAlternateResources(function(data){
        var selectElement = $("#addAlternateResourcesData");
        selectElement.empty();
        AlternateResourcesTable.setAlternateResourceMultiSelectOptions(data, selectElement);
        $("#" + AlternateResourcesTable.id + " tfoot").find(".ms-drop").empty();
        selectElement.multipleSelect();
    });
};

var ResourceRestrictionsTable = {};
ResourceRestrictionsTable.id = "resourceRestrictionsTable";
ResourceRestrictionsTable.error = {
    editNotAllowedText : "Not editable",
    restrictionNotAllowed: "restriction not allowed for this resource type"
};


ResourceRestrictionsTable.createTable = function(){
    var columns = [];

    ResourceRestrictionsTable.pushAllColumns(columns);

    var args = {
        tableId: ResourceRestrictionsTable.id,
        columns: columns
    };

    ResourceRestrictionsTable.currentTable = new SchedulerTables(args);

    ResourceRestrictionsTable.currentTable.generateTable([]);
};

ResourceRestrictionsTable.populateTable = function(){

    var tableObj = {};
    if(app_selectedResource.resourceType !== 'Room'){
        ResourceRestrictionsTable.currentTable.setEmptyDataMessage(ResourceRestrictionsTable.error.restrictionNotAllowed);
        ResourceRestrictionsTable.currentTable.setEmptyDataMessageClass("resourceNotEditable");
        ResourceRestrictionsTable.currentTable.refreshTableBody([], 1);
    }
    else if(MiscUtil.isUndefinedOrNull(app_selectedResource.sharedResourceId) && !UserRoleUtil.userIsSuperAdminOrResourceManager()){
        ResourceRestrictionsTable.currentTable.setEmptyDataMessage(ResourceRestrictionsTable.error.editNotAllowedText);
        ResourceRestrictionsTable.currentTable.setEmptyDataMessageClass("resourceNotEditable");
        ResourceRestrictionsTable.currentTable.refreshTableBody([], 1);
    }
    else if (app_selectedResource.sharedResourceId) {
        var resourceDetailUrl = "rest/resource/getResourceDetail?resourceId=" + app_selectedResource.sharedResourceId;
        $.getJSON(resourceDetailUrl, function (data) {
            app_selectedResource.sharedResource = data;

            tableObj.sharedResource = data;
            tableObj.id = app_selectedResource.id;
            var tableData = [tableObj];

            ResourceRestrictionsTable.currentTable.refreshTableBody(tableData, 1);
        });
    }
    else {
        ResourceRestrictionsTable.loadWithResourcesAvailableForRestriction();
    }
};

ResourceRestrictionsTable.pushAllColumns = function(columns) {

    ResourceRestrictionsTable.pushNonEditColumns(columns);

    if (UserRoleUtil.userIsSuperAdminOrResourceManager()) {
        ResourceRestrictionsTable.pushEditColumns(columns);
    }
};


ResourceRestrictionsTable.pushNonEditColumns = function(columns){
    columns.push(new Column({
        columnName: "Restriction",
        width: 20,
        columnType: Column.Custom,
        extractDataFunction: function (val) {
            return val.resource;
        },
        defaultSortOrder: Column.ASC,
        renderCell: function (tdElement, val, tdElementId){
            if (val.sharedResource) {
                tdElement.text("Gender Restriction");
            }
            else {
                var restrictionTypeSelect = $("<select></select>", {"id": "restrictionTypeSelect", "class": "comboboxListItem"})
                    .append($("<option></option>", {"value": "Gender Restriction", "text": "Gender Restriction"}));
                $("<span></span>", {"id": "restrictionTypeSelectWrapper"}).append(restrictionTypeSelect).appendTo(tdElement);

                WidgetUtil.createComboBox(restrictionTypeSelect, {
                    width: '180px',
                    value: 'Gender Restriction',
                    prompt: 'Gender Restriction'
                });

                if (val.sharedResource) {
                    $("#restrictionTypeSelect").combobox('setValue', "Gender Restriction");
                }
            }
        }
    }));

    columns.push(new Column({
        columnName: "Associated Resource",
        width: 20,
        columnType: Column.Custom,
        extractDataFunction: function (val) {
            return val.resource;
        },
        defaultSortOrder: Column.ASC,
        renderCell: function (tdElement, val, tdElementId){
            if (val.sharedResource) {
                tdElement.text(val.sharedResource.resource);
            }
            else {
                ResourceRestrictionsTable.createShareableResourceComboboxField(tdElement, val);
            }
        }
    }));

    columns.push(new Column({
        columnName: "Note",
        width: 30,
        columnType: Column.Custom,
        extractDataFunction: function (val) {
            return val.resource;
        },
        defaultSortOrder: Column.ASC,
        renderCell: function (tdElement, val, tdElementId) {
            if (val.sharedResource) {
                tdElement.text(val.sharedResource.sharedResourceNotes);
            }
            else {
                ResourceRestrictionsTable.createNotesField(tdElement);
            }
        },
        editFunction: function(tdElement, val){
            tdElement.empty();
            var inputField = ResourceRestrictionsTable.createNotesField(tdElement);
            WidgetUtil.commentBox.setValue(inputField, val.sharedResource.sharedResourceNotes);
        }
    }));
};

ResourceRestrictionsTable.createShareableResourceComboboxField = function(tdElement, val){
    var sharedResourceSelectId = "sharedResourceSelect";
    var sharedResourceSelect = $("<select></select>", {
        "id": sharedResourceSelectId,
        "class": "comboboxListItem"
    });
    $("<span></span>", {"id": "sharedResourceSelectWrapper"}).append(sharedResourceSelect).appendTo(tdElement);

    $("<span></span>", {
        "id": "sharedResourceSelectValidation",
        "class": "formElementRequired"
    }).appendTo(tdElement);

    var shareableResources = val.shareableResources;
    $("#" + sharedResourceSelectId).html(buildIdAndNameMenu(shareableResources));
    WidgetUtil.createComboBox("#sharedResourceSelect", {width: '180px'});

    return $("#sharedResourceSelect");
};

ResourceRestrictionsTable.createNotesField = function(tdElement){
    var resourceRestrictionInput = $("<div></div>", {"id": "restrictionNotesInput"});
    $("<div></div>", {"id": "restrictionNotesInputWrapper"})
        .append(resourceRestrictionInput).appendTo(tdElement);

    WidgetUtil.commentBox(resourceRestrictionInput, {placeholder: "notes", width: "94%"});

    return resourceRestrictionInput;
};

ResourceRestrictionsTable.pushEditColumns = function(columns){
    columns.push(new Column({
        columnType: Column.Hyperlink,
        rowElementClass: 'editColumn',
        columnName: "",
        width: 10,
        rowElementOnClick: function (val, element) {
            if(val.sharedResource){
                var rowIndex =  ResourceRestrictionsTable.currentTable.getRowIndex(element);
                var tableRowElement = ResourceRestrictionsTable.currentTable.getDataRowElementAtIndex(rowIndex);
                ResourceRestrictionsTable.currentTable.editRow(tableRowElement, app_selectedResource);
            }
            else {
                ResourceRestrictionsTable.addResourceRestriction();
            }
        },
        extractDataFunction: function (val) {
            if (val.sharedResource) {
                return "Edit";
            }
            else {
                return "Add New";
            }
        },
        editFunction: function(tdElement, val){
            var tableRowId = $(tdElement).parent("tr").attr("id");
            var saveColumnsHtml = "<a href='javascript:ResourceRestrictionsTable.modifyResourceRestriction(" + val.id + ")'> Save </a>";
            $(tdElement).html(saveColumnsHtml);
        }
    }));

    columns.push(new Column({
        columnType: Column.Hyperlink,
        rowElementClass: 'editColumn',
        columnName: "",
        width: 10,
        rowElementHyperLink: function (val) {
            if (val.sharedResource) {
                return 'ResourceRestrictionsTable.deleteRestrictionDialog(' + val.sharedResource.sharedResourceId + ')';
            }
            else{
                return 'ResourceRestrictionsTable.loadWithResourcesAvailableForRestriction()';
            }
        },
        extractDataFunction: function (val) {
            if (val.sharedResource) {
                return "Delete";
            }
            else{
                return "Cancel";
            }
        },
        editFunction: function(tdElement, val){
            var tableRowId = $(tdElement).parent("tr").attr("id");
            var cancelColumnsHtml =
                "<a href='javascript:ResourceRestrictionsTable.reloadResourceRestriction()'> Cancel </a>";
            $(tdElement).html(cancelColumnsHtml);
        }
    }));
};

ResourceRestrictionsTable.addResourceRestriction = function() {

    ResourceRestrictionsTable.clearRestrictionValidation();

    var restrictionTypeId = $("#restrictionTypeSelect").combobox('getValue');
    if (!restrictionTypeId) {
        $("#restrictionTypeSelect").combobox('setValue', "Gender Restriction");
        restrictionTypeId = "Gender Restriction";
    }

    if (!ResourceRestrictionsTable.validateResourceRestriction()) {
        return;
    }

    var restrictionNotes = WidgetUtil.commentBox.getValue("#restrictionNotesInput");
    var sharedResourceId = $("#sharedResourceSelect").combobox('getValue');

    var jsonData = JSON.stringify({
        resourceId: app_selectedResource.resourceId,
        sharedResourceId: sharedResourceId,
        notes: restrictionNotes
    });

    $.post("rest/resource/addRestriction", {data: jsonData}, function (data) {
        var parsedData = JSON.parse(data);
        ResourceRestrictionsTable.handleUpdateRestriction(parsedData);
    });
};

ResourceRestrictionsTable.handleUpdateRestriction = function(parsedData) {
    var confirmationMessage = "";
    if (parsedData.result == true) {
        confirmationMessage = app_selectedResource.resource + ' has been updated.';
    }
    else {
        confirmationMessage = "An internal error prevented " + app_selectedResource.resource + " from being updated";
    }
    util_showMainMessage(confirmationMessage);
    ResourceRestrictionsTable.reloadResourceRestriction();
};

ResourceRestrictionsTable.reloadResourceRestriction = function(){
    $.getJSON("rest/resource/getResourceDetail?resourceId=" + app_selectedResource.resourceId, function (data) {
        sessionStorage.setItem("resourceData", JSON.stringify(data));
        app_selectedResource = data;

        var resourceDetailUrl = "rest/resource/getResourceDetail?resourceId=" + app_selectedResource.sharedResourceId;
        $.getJSON(resourceDetailUrl, function (data) {
            app_selectedResource.sharedResource = data;
            var tableObj = {};
            tableObj.sharedResource = data;
            tableObj.id = app_selectedResource.id;
            ResourceRestrictionsTable.currentTable.refreshTableBody([tableObj]);
        });
    });
};

ResourceRestrictionsTable.modifyResourceRestriction = function () {

    ResourceRestrictionsTable.clearRestrictionValidation();

    var restrictionNotes = WidgetUtil.commentBox.getValue("#restrictionNotesInput");

    var jsonData = JSON.stringify({
        resourceId: app_selectedResource.resourceId,
        notes: restrictionNotes
    });

    $.post("rest/resource/modifyRestriction", {data: jsonData}, function (data) {
        var parsedData = JSON.parse(data);
        ResourceRestrictionsTable.handleUpdateRestriction(parsedData);
    });
};

ResourceRestrictionsTable.clearRestrictionValidation = function() {

    $('#restrictionTypeSelectValidation').val('');
    $('#sharedResourceSelectValidation').val('');

    $('#restrictionTypeSelectValidation').css({visibility: "hidden"});
    $('#sharedResourceSelectValidation').css({visibility: "hidden"});
};

ResourceRestrictionsTable.validateResourceRestriction = function() {
    var isValid = true;

    var sharedResourceId = $("#sharedResourceSelect").combobox('getValue');
    if (!sharedResourceId) {
        $('#sharedResourceSelectValidation').text('Please select associated resource');
        $('#sharedResourceSelectValidation').css({opacity: 0.0, visibility: "visible"}).animate({opacity: 1.0});
        isValid = false;
    }

    return isValid;
};

ResourceRestrictionsTable.deleteRestrictionDialog = function(resourceId) {
    DialogsUtil.showConfirmationDialog("#delete-restriction-dialog-confirm", {
        buttons: {
            "Yes": function () {
                $(this).dialog("close");
                $.getJSON("rest/resource/removeRestriction?resourceId=" + resourceId, function (data) {
                    if (data.result) {
                        // reload resource from server
                        $.getJSON("rest/resource/getResourceDetail?resourceId=" + resourceId, function (data) {
                            sessionStorage.setItem("resourceData", JSON.stringify(data));
                            app_selectedResource = JSON.parse(sessionStorage.getItem("resourceData"));
                            ResourceRestrictionsTable.populateTable();
                        });
                    }
                    else {
                        alert("An internal error occurred");
                    }
                });
            },
            "No": function () {
                $(this).dialog("close");
            }
        }
    });
};


ResourceRestrictionsTable.loadWithResourcesAvailableForRestriction = function() {
    var tableObj = {};

    var shareableResourceUrl = "rest/resource/getResourcesAvailableForGenderBlockRestriction?resourceId=" + app_selectedResource.resourceId;
    $.getJSON(shareableResourceUrl, function(data){
        tableObj.shareableResources= data.resources;
        var tableData = [tableObj];

        ResourceRestrictionsTable.currentTable.refreshTableBody(tableData, 1);
    });
};