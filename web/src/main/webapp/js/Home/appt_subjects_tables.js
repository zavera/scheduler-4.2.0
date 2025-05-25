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
ApptSubjectsTable = {};

ApptSubjectsTable.dbColumnLastName = "lastName";
ApptSubjectsTable.dbColumnFirstName = "firstName";
ApptSubjectsTable.dbColumnMrn = "mrn";
ApptSubjectsTable.defaultPerPage = 10;

ApptSubjectsTable.currentTable = null;

ApptSubjectsTable.createTable = function (
    tableId,
    studyId
) {

    var columns = [];

    ApptSubjectsTable.pushAllColumns(columns);

    var populateFn = ApptSubjectsTable.populateTableFnFactory(studyId);

    var args = {
        tableId: tableId,
        columns: columns,
        fixedHeader: true,
        maxHeight: "200px",
        reloadFn: populateFn,
        rowSelectFnFactory: function(tableRowId, val) {
            return function() {
                var fullName = escapeQuotesHTML(val.subjectFirstName + " " + val.subjectLastName);
                return selectSubject(tableId, tableRowId, val.subjectMrnId, val.subjectMrn, fullName, val.subjectSchedulerGender);
            }
        }
    };

    ApptSubjectsTable.currentTable = new SchedulerTables(args);

    ApptSubjectsTable.currentTable.generateTable(
        [],
        ApptSubjectsTable.defaultPerPage,
        PaginationHelper.getPaginationId(tableId),
        populateFn
    );
};

ApptSubjectsTable.pushAllColumns = function (columns) {

    columns.push(new Column({
        columnName: "Last Name",
        dbColumn: ApptSubjectsTable.dbColumnLastName,
        width: 34,
        defaultSortOrder: Column.ASC,
        extractDataFunction: function (val) {
            return val.subjectLastName;
        }
    }));

    columns.push(new Column({
        columnName: "First Name",
        dbColumn: ApptSubjectsTable.dbColumnFirstName,
        width: 33,
        extractDataFunction: function (val) {
            return val.subjectFirstName;
        }
    }));

    columns.push(new Column({
        columnName: "MRN",
        width: 33,
        dbColumn: ApptSubjectsTable.dbColumnMrn,
        extractDataFunction: function (val) {
            return val.subjectMRN;
        }
    }));

};

ApptSubjectsTable.populateTable = function (studyId) {

    var table = ApptSubjectsTable.currentTable;

    var filterString = table.filterString || "";

    var url = "rest/study/getStudySubjects?study=" + studyId +
        "&filterString=" + filterString +
        "&page=" + table.getCurrentPage() +
        "&maxResults=" + table.getRecordsPerPage() +
        "&orderBy=" + (table.getSortOrder() || Column.ASC) +
        "&sortBy=" + (table.getSortColumn() || ApptSubjectsTable.dbColumnLastName);

    $.blockUI();
    $.getJSON(url, function (data) {
        ApptSubjectsTable.currentTable.refreshTableBody(data.studySubject1s,  data.totalCount);
        $.unblockUI();
    });

};

ApptSubjectsTable.populateTableFnFactory = function (studyId) {

    return function () {

        return ApptSubjectsTable.populateTable(studyId);

    };

};
