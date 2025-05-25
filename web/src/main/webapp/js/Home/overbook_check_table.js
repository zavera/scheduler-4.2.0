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

//The below 'commented' sourceURL specifies a name for this piece of code which
//  will appear if debugging in the browser
//This is needed because this file is loaded programmatically
//# sourceURL=study_tables.js

var OverbookTables = {};

OverbookTables.unwrappedId = 'overbookResourcesTable';

OverbookTables.dto_resourceName = "resourceName";
OverbookTables.dto_visitName = "visitName";
OverbookTables.dto_subjectName = "subjectName";
OverbookTables.dto_localId = "localId";
OverbookTables.dto_visitStartTime = "visitStartTime";
OverbookTables.dto_visitEndTime = "visitEndTime";
OverbookTables.dto_scheduledStartTime = "scheduledStartTime";
OverbookTables.dto_scheduledEndTime = "scheduledEndTime";
OverbookTables.dto_conflictedTime = "conflictedTime";

OverbookTables.uiColumn_resourceName = "Resource Name";
OverbookTables.uiColumn_visitName = "Visit Name";
OverbookTables.uiColumn_subjectName = "Subject Name";
OverbookTables.uiColumn_localId = "Local ID";
OverbookTables.uiColumn_visitStartTime = "Visit Start Time";
OverbookTables.uiColumn_visitEndTime = "Visit End Time";
OverbookTables.uiColumn_scheduledStartTime = "Resource Start Time";
OverbookTables.uiColumn_scheduledEndTime = "Resource End Time";
OverbookTables.uiColumn_conflictedTime = "Conflicted Time";

OverbookTables.pushSortableColumns = function (columns) {

    columns.push(new Column({
        dbColumn: OverbookTables.dto_resourceName,
        columnName: OverbookTables.uiColumn_resourceName,
        width: 11,
        defaultSortOrder: Column.ASC,
        extractDataFunction: function (val) {
            return val.resourceName;
        }
    }));

    columns.push(new Column({
        dbColumn: OverbookTables.dto_visitName,
        columnName: OverbookTables.uiColumn_visitName,
        width: 11,
        extractDataFunction: function (val) {
            return val.visitName;
        }
    }));

    columns.push(new Column({
        dbColumn: OverbookTables.dto_subjectName,
        columnName: OverbookTables.uiColumn_subjectName,
        width: 11,
        extractDataFunction: function (val) {
            return val.subjectName;
        }
    }));

    columns.push(new Column({
        dbColumn: OverbookTables.dto_localId,
        columnName: OverbookTables.uiColumn_localId,
        width: 11,
        extractDataFunction: function (val) {
            return val.localId;
        }
    }));

    columns.push(new Column({
        dbColumn: OverbookTables.dto_visitStartTime,
        columnName: OverbookTables.uiColumn_visitStartTime,
        width: 11,
        extractDataFunction: function (val) {
            return showDateTime(val.visitStartTime);
        }
    }));

    columns.push(new Column({
        dbColumn: OverbookTables.dto_visitEndTime,
        columnName: OverbookTables.uiColumn_visitEndTime,
        width: 11,
        extractDataFunction: function (val) {
            return showDateTime(val.visitEndTime);
        }
    }));

    columns.push(new Column({
        dbColumn: OverbookTables.dto_scheduledStartTime,
        columnName: OverbookTables.uiColumn_scheduledStartTime,
        width: 11,
        extractDataFunction: function (val) {
            return showDateTime(val.scheduledStartTime);
        }
    }));

    columns.push(new Column({
        dbColumn: OverbookTables.dto_scheduledEndTime,
        columnName: OverbookTables.uiColumn_scheduledEndTime,
        width: 11,
        extractDataFunction: function (val) {
            return showDateTime(val.scheduledEndTime);
        }
    }));

    columns.push(new Column({
        dbColumn: OverbookTables.dto_conflictedTime,
        columnName: OverbookTables.uiColumn_conflictedTime,
        width: 11,
        extractDataFunction: function (val) {
            return val.conflictedTime;
        }
    }));
};

OverbookTables.loadOverbookTablesDataIntoTablesWidget = function(visitId) {

    var columns = [];
    OverbookTables.visitId = visitId;

    OverbookTables.pushSortableColumns(columns);

    OverbookTables.currentTable = new SchedulerTables({
        tableId: OverbookTables.unwrappedId,
        columns: columns,
        reloadFn: OverbookTables.populateOverbookTablesTable,
        fixedHeader: true,
        maxHeight: "200px"
    });

    OverbookTables.currentTable.generateTable([]);

    OverbookTables.populateOverbookTablesTable();
};

OverbookTables.populateOverbookTablesTable = function () {

    var restUrl = "rest/appointment/getOverbookedVisitsData?id=" + OverbookTables.visitId +
        "&sortBy=" + (OverbookTables.currentTable.getSortOrder() || Column.ASC) +
        "&sortOn=" + (OverbookTables.currentTable.getSortColumn() || OverbookTables.dto_resourceName);

    $.blockUI();
    $.getJSON(restUrl, function (data) {
        OverbookTables.currentTable.refreshTableBody(data);
        $.unblockUI();
    });
};

//The below 'commented' sourceURL specifies a name for this piece of code which
//  will appear if debugging in the browser
//This is needed because this file is loaded programatically
//# sourceURL=overbook_check_table.js
