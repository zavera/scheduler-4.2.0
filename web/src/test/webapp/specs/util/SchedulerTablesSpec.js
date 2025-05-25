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
describe('Table Widget', function() {

    it("generates a table with text columns", function()
    {
        FixtureHelper.loadTestHtmlFixtureIntoDom("util/html/table.html");

        var tableId = "testTable";
        var columns = [];
        columns.push(new Column({
            dbColumn: "d.name",
            columnName: "nameColumn",
            columnType: Column.Text,
            width:17,
            extractDataFunction: function (val) {return val.name;}
        }));

        columns.push(new Column({
            dbColumn: "d.description",
            columnName: "descriptionColumn",
            columnType: Column.Text,
            width:17,
            defaultSortOrder: Column.ASC,
            extractDataFunction: function (val) {return val.description;}
        }));

        var reloadFn = function()
        {
            console.log("reloading data");
        };

        var options = {
            tableId: tableId,
            columns: columns,
            reloadFn: reloadFn
        };

        var table = new SchedulerTables(options);
        expect(table.getId()).toBe(tableId);
        expect(table.getColumns()).toBe(columns);

        var data = [
            {
                name: "Column Foo",
                description: "This is a test column foo"
            },
            {
                name: "Column Bar",
                description: "This is a test column bar"
            }
        ];

        var maxResults = 50;
        var paginationContainerId = "testTablePagination";
        table.generateTable(data, maxResults, paginationContainerId, reloadFn);

        expect($("#testTable-filterRow .filterField").length).toBe(0);
        expect($("#testTable_0").hasClass("sortable")).toBe(true);
        expect($("#testTable_1").hasClass("sorting_asc")).toBe(true);

        expect($("#testTablePagination").find("div").length).toBe(3);
        expect(table.getCurrentPage()).toBe(1);
        expect(table.getRecordsPerPage()).toBe(maxResults);

        expect(table.getSortColumn()).toBe(columns[1].getDbColumn());
        expect(table.getSortOrder()).toBe(columns[1].getDefaultSortOrder());

        //one row with column labels and two rows of data
        expect($("#testTable").find("tbody tr").length).toBe(3);

        var firstDataRow = $($("#testTable").find("tbody tr").get(1));
        expect(firstDataRow.children("td:nth-child(1)").text()).toBe(data[0].name);
        expect(firstDataRow.children("td:nth-child(2)").text()).toBe(data[0].description);

        var secondDataRow = $($("#testTable").find("tbody tr").get(2));
        expect(secondDataRow.children("td:nth-child(1)").text()).toBe(data[1].name);
        expect(secondDataRow.children("td:nth-child(2)").text()).toBe(data[1].description);
    });

    it("generates a table with a hyperlink column", function(){
        FixtureHelper.loadTestHtmlFixtureIntoDom("util/html/table.html");

        var tableId = "testTable";
        var columns = [];
        columns.push(new Column({
            dbColumn: "d.name",
            columnName: "nameColumn",
            columnType: Column.Hyperlink,
            width:17,
            rowElementHyperLink: function (val) {
                return 'getDataFn(' + val.id + ')';
            },
            extractDataFunction: function (val) {return val.name;}
        }));

        columns.push(new Column({
            dbColumn: "d.description",
            columnName: "descriptionColumn",
            columnType: Column.Text,
            width:17,
            defaultSortOrder: Column.ASC,
            extractDataFunction: function (val) {return val.description;}
        }));

        var reloadFn = function()
        {
            console.log("reloading data");
        };

        var options = {
            tableId: tableId,
            columns: columns,
            reloadFn: reloadFn
        };

        var table = new SchedulerTables(options);

        var data = [
            {
                id: 100,
                name: "Column Foo",
                description: "This is a test column foo"
            },
            {
                id: 200,
                name: "Column Bar",
                description: "This is a test column bar"
            }
        ];

        var maxResults = 50;
        var paginationContainerId = "testTablePagination";
        table.generateTable(data, maxResults, paginationContainerId, reloadFn);

        //one row with column labels and two rows of data
        expect($("#testTable").find("tbody tr").length).toBe(3);

        var firstDataRow = $($("#testTable").find("tbody tr").get(1));
        expect(firstDataRow.children("td:nth-child(1)").text()).toBe(data[0].name);
        expect(firstDataRow.children("td:nth-child(2)").text()).toBe(data[0].description);

        var hyperlink = firstDataRow.find("a").prop("href");
        expect(hyperlink).toBe("javascript:getDataFn("+ data[0].id + ")");
    });

    it('it generates a table with editable columns', function() {
        var table = SchedulerTablesTestHelper.createEditableTable();
        var tableId = table.getId();
        SchedulerTablesTestHelper.populateTable(table);

        var secondRowCheckbox = $($("." + tableId + "_0-checkbox").get(1));
        secondRowCheckbox.click();

        var thirdRowCheckbox = $($("." + tableId + "_0-checkbox").get(2));
        thirdRowCheckbox.click();

        var selectedRows = table.getSelectedRows();
        expect(selectedRows.length).toBe(2);

        var selectedValues2D = table.getDataForSelectedRows();
        expect(selectedValues2D.length).toBe(2);

        var firstSelectedQuantityValue = selectedValues2D[0][1];
        expect(firstSelectedQuantityValue).toBe(7);

        var secondSelectedQuantityValue = selectedValues2D[1][1];
        expect(secondSelectedQuantityValue).toBe(999);

        //now uncheck the third row
        thirdRowCheckbox.click();

        var resetNumberSpinnerValue = $("#testTable-2_1-spinner").spinner("value");
        selectedValues2D = table.getDataForSelectedRows();
        expect(selectedValues2D.length).toBe(1);

        expect(resetNumberSpinnerValue).toBe(1);
    });

    it("generates a table with expandable rows", function() {
        FixtureHelper.loadTestHtmlFixtureIntoDom("util/html/table.html");

        var table = SchedulerTablesTestHelper.createExpandableTable();
        SchedulerTablesTestHelper.populateTable(table);

        var tableId = table.getId();

        //one row with column labels and three rows of data
        expect($("#" + tableId).find("tbody tr").length).toBe(4);

        expect($("#" + tableId).hasClass("expandableTable")).toBe(true);
        var firstRowExpandToggleImg = $("#" + tableId + "-0_0").find("img");
        expect(firstRowExpandToggleImg.attr("src")).toBe(expandOrCollapseImgUrls.EXPAND);
        expect(firstRowExpandToggleImg.prop("title")).toBe("Expand");

        firstRowExpandToggleImg.click();
        expect(firstRowExpandToggleImg.attr("src")).toBe(expandOrCollapseImgUrls.COLLAPSE);
        expect(firstRowExpandToggleImg.prop("title")).toBe("Collapse");
    });

    it("retrieves all data in table", function(){
        FixtureHelper.loadTestHtmlFixtureIntoDom("util/html/table.html");

        var table = SchedulerTablesTestHelper.createEditableTable();
        var tableId = table.getId();
        var data = SchedulerTablesTestHelper.populateTable(table);
        expect(data.length).toBe(3);

        var dataList = table.getTableData();
        expect(dataList.length).toBe(3);
        expect(dataList[0].length).toBe(3);

        expect(dataList[0].id).toBe(data[0][0]);
        expect(dataList[0].quantity).toBe(data[0][1]);
        expect(dataList[0].description).toBe(data[0][2]);

        expect(dataList[2].id).toBe(data[2][0]);
        expect(dataList[2].quantity).toBe(data[2][1]);
        expect(dataList[2].description).toBe(data[2][2]);
    });

    it("resets the page to one when sorting", function(){
        FixtureHelper.loadTestHtmlFixtureIntoDom("util/html/table.html");

        var reloadFn = function() {/* do nothing*/};
        var additionalCols;
        var table = SchedulerTablesTestHelper.createEditableTable(additionalCols, reloadFn);
        var tableId = table.getId();
        var data = SchedulerTablesTestHelper.populateTable(table);

        //one row with column labels and three rows of data
        expect($("#" + tableId).find("tbody tr").length).toBe(4);
        table.setCurrentPage(7);
        expect(table.getCurrentPage()).toBe(7);

        $("#" + tableId).find(".sorting_asc").click();

        expect(table.getCurrentPage()).toBe(1);
    });

    it("resets the page to one when filtering", function(){
        FixtureHelper.loadTestHtmlFixtureIntoDom("util/html/table.html");

        var filterName = "myFilterCol";
        var filterColList = [new Column({
            dbColumn: "d.comment",
            columnName: "comment",
            columnType: Column.Text,
            width:22,
            extractDataFunction: function (val) {return val.comment;},
            filter: {
                name: filterName,
                onFilter:  function(){/*do nothing*/},
                ignore: true
            }
        })];

        var table = SchedulerTablesTestHelper.createEditableTable(filterColList);
        var tableId = table.getId();
        var data = SchedulerTablesTestHelper.populateTable(table);
        table.setCurrentPage(3);

        expect(table.getCurrentPage()).toBe(3);

        //this will trigger a change event on the textbox element
        $("#" + tableId + "-filter-" + filterName).textbox("setValue", "test");

        expect(table.getCurrentPage()).toBe(1);
    });

    it("can display an empty data message", function(){
        FixtureHelper.loadTestHtmlFixtureIntoDom("util/html/table.html");

        var reloadFn = function() {/* do nothing*/};
        var filterName = "myTextCol";

        var additionalCols = [new Column({
            dbColumn: "a.comment",
            columnName: "comment",
            columnType: Column.Text,
            width:25,
            extractDataFunction: function (val) {return val.comment;},
            filter: {
                name: filterName,
                onFilter:  function(){/*do nothing*/},
                ignore: true
            }
        })];

        var emptyDataMsg = "No Data to display";
        var emptyDataMsgClass = "emptyData";
        var additionalArgs = {
            emptyDataMessage: emptyDataMsg,
            emptyDataMessageClass: emptyDataMsgClass
        };
        var table = SchedulerTablesTestHelper.createEditableTable(additionalCols, reloadFn, additionalArgs);
        var tableId = table.getId();
        table.refreshTableBody([], 0);

        var emptyDataElem = $("#emptyDataMessage_" + tableId);
        expect(emptyDataElem.is(":visible")).toBe(true);
        expect(emptyDataElem.text()).toBe(emptyDataMsg);
        expect(emptyDataElem.hasClass(emptyDataMsgClass)).toBe(true);
    });
});
