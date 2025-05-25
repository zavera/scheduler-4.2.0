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
/**
 * Created by xavier on 5/1/17.
 */

UserTable = {};

UserTable.id = "userTable";

UserTable.dbColumnLastName = "u.lastName";
UserTable.dbColumnFirstName = "u.firstName";
UserTable.dbColumnPrimaryPhone = "u.primaryPhone";
UserTable.dbColumnECommonsId = "u.ecommonsId";
UserTable.dbColumnActive = "u.active";

UserTable.uiColumnLastName = "Last Name";
UserTable.uiColumnFirstName = "First Name";
UserTable.uiColumnPrimaryPhone = "Primary Contact Number";
UserTable.uiColumnECommonsId = "User ID";
UserTable.uiColumnActive = "Status";
UserTable.uiColumnDetails = "";

UserTable.defaultPerPage = 50;

UserTable.createUserTable = function() {
    var columns = [];

    UserTable.pushColumns(columns);

    UserTable.currentTable = new SchedulerTables({
        tableId: UserTable.id,
        columns: columns,
        reloadFn: UserTable.populateUserTable
    });

    UserTable.currentTable.generateTable(
        [],
        UserTable.defaultPerPage,
        PaginationHelper.getPaginationId(UserTable.id),
        UserTable.populateUserTable
    );
};

UserTable.pushColumns = function(columns) {

    columns.push(new Column({
        dbColumn: UserTable.dbColumnLastName,
        columnName: UserTable.uiColumnLastName,
        width: 20,
        extractDataFunction: function (val) {
            return val.lastName;
        },
        defaultSortOrder: Column.ASC,
        filter: {
            name: UserTable.dbColumnLastName,
            onFilter: UserTable.resetToPageOne
        }
    }));

    columns.push(new Column({
        dbColumn: UserTable.dbColumnFirstName,
        columnName: UserTable.uiColumnFirstName,
        width: 20,
        extractDataFunction: function (val) {
            return val.firstName;
        },
        filter: {
            name: UserTable.dbColumnFirstName,
            onFilter: UserTable.resetToPageOne
        }
    }));

    columns.push(new Column({
        dbColumn: UserTable.dbColumnPrimaryPhone,
        columnName: UserTable.uiColumnPrimaryPhone,
        width: 20,
        extractDataFunction: function (val) {
            return unifiedPhoneNumbersFormat(val.primaryPhone);
        },
        filter: {
            name: UserTable.dbColumnPrimaryPhone,
            onFilter: UserTable.resetToPageOne,
            maskFormat: "(999) 999-9999"
        }
    }));

    columns.push(new Column({
        dbColumn: UserTable.dbColumnECommonsId,
        columnName: UserTable.uiColumnECommonsId,
        width: 20,
        extractDataFunction: function (val) {
            return val.ecommonsId;
        },
        filter: {
            name: UserTable.dbColumnECommonsId,
            onFilter: UserTable.resetToPageOne
        }
    }));

    columns.push(new Column({
        columnType: Column.Hyperlink,
        rowElementClass: 'editColumn',
        columnName: UserTable.uiColumnActive,
        width: 10,
        rowElementHyperLink: function (val) {
            return 'userChangeStatusConfirmation(' + val.id + ')';
        },
        extractDataFunction: function (val) {
            return booleanToStatusValue(val.active);
        }
    }));

    columns.push(new Column({
        columnType: Column.Hyperlink,
        rowElementClass: 'editColumn',
        columnName: UserTable.uiColumnDetails,
        width: 10,
        rowElementHyperLink: function (val) {
            return 'showUserDetail(' + val.id + ')';
        },
        extractDataFunction: function (val) {
            return "View Detail";
        }
    }));

};

UserTable.resetToPageOne = function() {

    UserTable.currentTable.setCurrentPage(1);
    UserTable.populateUserTable();
};

UserTable.populateUserTable = function() {

    var table = UserTable.currentTable;

    var keyValuePairs = { searchItems : table.getAllFilterKeyValuePairs() };
    var stringifiedPairs = JSON.stringify(keyValuePairs);

    var restUrl = "rest/management/getUsers" +
        "?page=" +          table.getCurrentPage() +
        "&maxResults=" +    table.getRecordsPerPage() +
        "&orderBy=" +       (table.getSortOrder() || Column.ASC) +
        "&sortBy=" +        (table.getSortColumn() || UserTable.dbColumnLastName) +
        "&search=" +        encodeURIComponent(stringifiedPairs);

    $.getJSON(restUrl, function(data) {
        table.refreshTableBody(data.users, data.total);
    });
};


