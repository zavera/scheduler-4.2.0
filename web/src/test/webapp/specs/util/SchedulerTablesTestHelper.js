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
var SchedulerTablesTestHelper = {};
SchedulerTablesTestHelper.paginationId = "testTablePagination";

SchedulerTablesTestHelper.pushColumns = function(columns){
    columns.push(new Column({
        dbColumn: "quantity",
        columnName: "Quantity",
        width: 11,
        columnType: Column.Custom,
        extractDataFunction: function (val) {
            return val.quantity;
        },
        renderCell: function(tdElement, val, tdElementId) {
            var quantity = $("<input/>", {
                id: tdElementId + "-spinner",
                name:"value",
                class: "numberSpinner"
            });

            tdElement.append(quantity);
            quantity.spinner({
                min: 0,
                max: 999
            });
            quantity.spinner( "value", val.quantity );
        },
        extractCellValue: function(tdElement, cellId) {
            var spinner = $("#" + cellId + "-spinner");
            return spinner.spinner("value");
        },
        clearCellValueFunction: function(tdElement, tdElementId){
            var spinner = $("#" + tdElementId + "-spinner");

            if(spinner.length !== 0) {
                spinner.spinner("value", 1);
            }
        }
    }));

    columns.push(new Column({
        dbColumn: "d.description",
        columnName: "description",
        columnType: Column.Text,
        width:22,
        extractDataFunction: function (val) {return val.description;},
        defaultSortOrder: Column.ASC
    }));
};

SchedulerTablesTestHelper.createEditableTable = function(additionalCols, reloadFn, additionalArgs) {
    var columns = [];

    columns.push(new Column({
        width: 5,
        extractDataFunction: function (val) {
            if (val === undefined) {
                console.log("----------------------------- YO1 ----------");
                console.trace();
                console.log("----------------------------- YO2 ----------");
                return "";
            }
            return val.id;
        },
        columnType: Column.Checkbox
    }));

    SchedulerTablesTestHelper.pushColumns(columns);

    if(additionalCols) {
        columns = columns.concat(additionalCols);
    }

    var reload = SchedulerTablesTestHelper.populateTable;
    if(reloadFn) {
       reload = reloadFn;
    }

    var args = {
        tableId: "testTable",
        columns: columns,
        reloadFn: reload
    };

    if(additionalArgs){
        Object.assign(args, additionalArgs);
    }

    var defaultPerPage = 25;
    var table = new SchedulerTables(args);

    table.generateTable(
        [],
        defaultPerPage,
        SchedulerTablesTestHelper.paginationId,
        args.reloadFn);

    return table;
};

SchedulerTablesTestHelper.populateTable = function(table, totalData) {
    var data = [{
        id: 450,
        quantity: 1,
        description: "this is a quantity field for item 1"
    },
    {
        id: 781,
        quantity: 7,
        description: "this is a quantity field for item 2"
    },
    {
        id: 925,
        quantity: 999,
        description: "this is a quantity field for item 3"
    }];

    table.refreshTableBody(data, totalData);

    return data;
};

SchedulerTablesTestHelper.expandRowText = "This is the expand row with id";
SchedulerTablesTestHelper.expandFn = function(container, rowId){
    container.append(SchedulerTablesTestHelper.expandRowText + " " + rowId);
};

SchedulerTablesTestHelper.createExpandableTable = function(){
    var columns = [];

    columns.push(new Column({
        width: 4,
        extractDataFunction: function (val) {
            return val.id;
        },
        columnType: Column.Expandable
    }));

    SchedulerTablesTestHelper.pushColumns(columns);

    var args = {
        tableId: "testTable",
        columns: columns,
        reloadFn: SchedulerTablesTestHelper.populateTable,
        expandFn: SchedulerTablesTestHelper.expandFn
    };

    var defaultPerPage = 25;
    var table = new SchedulerTables(args);

    table.generateTable(
        [],
        defaultPerPage,
        SchedulerTablesTestHelper.paginationId,
        args.reloadFn);

    return table;
};
