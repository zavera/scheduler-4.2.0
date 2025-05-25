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
//# sourceURL=study_member_tables.js

////////////////////////// Study members table////////////////

var StudyMembersTable = {};

StudyMembersTable.id = "studyMembersTable";
StudyMembersTable.dbColumnMemberLastName = "su.user.lastName";
StudyMembersTable.dbColumnMemberFirstName = "su.user.firstName";
StudyMembersTable.dbColumnMemberTitle = "su.user.institutionRole.name";
StudyMembersTable.dbColumnMemberStatus = "su.status";
StudyMembersTable.defaultPerPage = 50;
StudyMembersTable.paginationId = PaginationHelper.getPaginationId(StudyMembersTable.id);

StudyMembersTable.createTable = function () {
    var columns = [];

    StudyMembersTable.pushAllColumns(columns);

    var args = {
        tableId: StudyMembersTable.id,
        columns: columns,
        reloadFn: function () {
            StudyMembersTable.populateTable();
        }
    };

    StudyMembersTable.currentTable = new SchedulerTables(args);

    StudyMembersTable.currentTable.generateTable(
        [],
        StudyMembersTable.defaultPerPage,
        StudyMembersTable.paginationId,
        function () {
            StudyMembersTable.populateTable.call(this);
        });
};

StudyMembersTable.pushAllColumns = function (columns) {
    columns.push(new Column({
        dbColumn: StudyMembersTable.dbColumnMemberLastName,
        columnName: "Last Name",
        width: 25,
        extractDataFunction: function (val) {
            return val.lastName;
        },
        defaultSortOrder: Column.ASC,
        filter: {
            name: StudyMembersTable.dbColumnMemberLastName,
            onFilter: function (value) {
                StudyMembersTable.resetToPageOne();
            }
        }
    }));

    columns.push(new Column({
        dbColumn: StudyMembersTable.dbColumnMemberFirstName,
        columnName: "First Name",
        width: 25,
        extractDataFunction: function (val) {
            return val.firstName;
        },
        filter: {
            name: StudyMembersTable.dbColumnMemberFirstName,
            onFilter: function (value) {
                StudyMembersTable.resetToPageOne();
            }
        }
    }));

    columns.push(new Column({
        dbColumn: StudyMembersTable.dbColumnMemberTitle,
        columnName: "Title",
        width: 25,
        extractDataFunction: function (val) {
            return val.title;
        },
        filter: {
            name: StudyMembersTable.dbColumnMemberTitle,
            onFilter: function (value) {
                StudyMembersTable.resetToPageOne();
            }
        }
    }));

    if (UserRoleUtil.isCrcStaff(user) || UserRoleUtil.isFrontDesk(user)) {
        columns.push(new Column({
            columnType: Column.Hyperlink,
            columnName: "Status",
            width: 25,
            rowElementHyperLink: function (val) {
                return 'toggleStudyMemberStatusDialog(' + val.id + ')';
            },
            extractDataFunction: function (val) {
                return val.status ? "Active" : "Not Active";
            }
        }));
    }
    else {
        columns.push(new Column({
            columnType: Column.Text,
            columnName: "Status",
            width: 25,
            rowElementClass: "bold",
            extractDataFunction: function (val) {
                return val.status ? "Active" : "Not Active";
            }
        }));
    }
};

StudyMembersTable.resetToPageOne = function () {

    StudyMembersTable.currentTable.setCurrentPage(1);
    StudyMembersTable.populateTable();

};

StudyMembersTable.populateTable = function () {

    var searchItemsList = {searchItems: StudyMembersTable.currentTable.getAllFilterKeyValuePairs()};
    var searchQuery = JSON.stringify(searchItemsList);
    var url = "rest/study/getStudyMembers?study=" + app_selectedStudy.id +
        "&page=" + (StudyMembersTable.currentTable.getCurrentPage()) +
        "&maxResults=" + StudyMembersTable.currentTable.getRecordsPerPage() +
        "&orderBy=" + (StudyMembersTable.currentTable.getSortOrder() || Column.ASC) +
        "&sortBy=" + (StudyMembersTable.currentTable.getSortColumn() || StudyMembersTable.dbColumnStudyName) +
        "&search=" + encodeURIComponent(searchQuery);
    $.getJSON(url, function (data) {
        StudyMembersTable.currentTable.refreshTableBody(data);
    });
};

var AddStudyMembersTable = {};
AddStudyMembersTable.id = "addStudyMembersTable";

AddStudyMembersTable.createTable = function () {
    var columns = [];

    AddStudyMembersTable.pushAllColumns(columns);

    var args = {
        tableId: AddStudyMembersTable.id,
        columns: columns,
        fixedHeader: true,
        maxHeight: "600px"
    };

    AddStudyMembersTable.currentTable = new SchedulerTables(args);

    AddStudyMembersTable.currentTable.generateTable([]);
};

AddStudyMembersTable.pushAllColumns = function (columns) {
    columns.push(new Column({
        columnType: Column.Checkbox,
        width: 5,
        onChange: AddStudyMembersTable.toggleAddStudyMemberButton,
        extractDataFunction: function (val) {
            return val.id;
        }
    }));

    columns.push(new Column({
        columnName: "Last Name",
        width: 25,
        extractDataFunction: function (val) {
            return val.lastName;
        }
    }));

    columns.push(new Column({
        columnName: "First Name",
        width: 25,
        extractDataFunction: function (val) {
            return val.firstName;
        }
    }));

    columns.push(new Column({
        columnName: "Title",
        width: 25,
        extractDataFunction: function (val) {
            return val.title;
        }
    }));

    columns.push(new Column({
        columnName: "User ID",
        width: 20,
        extractDataFunction: function (val) {
            return val.userId;
        }
    }));
};

AddStudyMembersTable.populateTable = function () {
    var filterString = $.trim($("#filter_studymember_string").val());

    var url = "rest/management/getStudyMembersStudyScreen?filterString=" + filterString
            + "&orderBy=ASC&sortBy=u.lastName";

    $.blockUI();
    $.getJSON(url, function (data) {
        $("#filter_studymember_string").val('');
        AddStudyMembersTable.currentTable.refreshTableBody(data);
        $.unblockUI();
    });
};

AddStudyMembersTable.toggleAddStudyMemberButton = function(){
    var selectedRows = AddStudyMembersTable.currentTable.getSelectedRows();
    var hide = selectedRows.length === 0;

    if (hide) {
        $('.addStudyMemberLink').css({display: "none"});
    }
    else {
        $('.addStudyMemberLink').css({display: "inline-block"});
    }
};