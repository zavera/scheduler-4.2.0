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

describe('booked_resources_table.js:loadOverbookedResourcesData()', function () {
    it('delegates to OverbookTables.loadOverbookTablesDataIntoTablesWidget', function () {
        var spy_load = spyOn(OverbookTables, "loadOverbookTablesDataIntoTablesWidget");

        loadOverbookedResourcesData(42);

        expect(spy_load).toHaveBeenCalledTimes(1);
        expect(spy_load.calls.argsFor(0)).toEqual([42]);
    });
});

describe('booked_resources_table.js:OverbookTables.pushColumns()', function () {
    it('generates the table', function () {
        var spy_Column = spyOn(window, "Column")
            .and.returnValue({});

        OverbookTables.pushSortableColumns([]);

        expect(spy_Column).toHaveBeenCalledTimes(9);

        expect(spy_Column.calls.argsFor(0)).toEqual([{
            dbColumn: OverbookTables.dto_resourceName,
            columnName: OverbookTables.uiColumn_resourceName,
            width: 11,
            defaultSortOrder: Column.ASC,
            extractDataFunction: jasmine.any(Function)
        }
        ]);
        expect(spy_Column.calls.argsFor(8)).toEqual([{
            dbColumn: OverbookTables.dto_conflictedTime,
            columnName: OverbookTables.uiColumn_conflictedTime,
            width: 11,
            extractDataFunction: jasmine.any(Function)
        }
        ]);
    });
});

describe('booked_resources_table.js:' +
    'OverbookTables.loadOverbookTablesDataIntoTablesWidget()', function () {
    it('assembles some columns', function () {
        OverbookTables.tableName = "fooTable";

        var pseudoTable = {generateTable: function(){}};

        var spy_pushColumns = spyOn(OverbookTables, "pushSortableColumns");
        var spy_SchedulerTables = spyOn(window, "SchedulerTables")
            .and.returnValue(pseudoTable);
        var spy_generateTable = spyOn(pseudoTable, "generateTable");
        var spy_populate = spyOn(OverbookTables, "populateOverbookTablesTable");

        OverbookTables.loadOverbookTablesDataIntoTablesWidget(42);

        expect(spy_SchedulerTables).toHaveBeenCalledTimes(1);
        expect(spy_SchedulerTables.calls.argsFor(0)).toEqual([{
            tableId: "overbookResourcesTable",
            columns: [],
            reloadFn: OverbookTables.populateOverbookTablesTable,
            fixedHeader: true,
            maxHeight: "200px"
        }]);

        expect(spy_generateTable).toHaveBeenCalledTimes(1);
        expect(spy_generateTable.calls.argsFor(0)).toEqual([[]]);

        expect(spy_populate).toHaveBeenCalledTimes(1);
        expect(spy_populate.calls.argsFor(0)).toEqual([]);
    });
});

describe('booked_resources_table.js:OverbookTables.populateLinkingResourcesTable()', function () {
    it('populates the table', function () {
        OverbookTables.visitId = 42;

        OverbookTables.currentTable = {
            getSortOrder: function(){},
            getSortColumn: function(){}
        };

        var expectedUrl =
            "rest/appointment/getOverbookedVisitsData?id=42" +
            "&sortBy=ASC" +
            "&sortOn=resourceName";

        var spy_getSortOrder = spyOn(OverbookTables.currentTable, "getSortOrder");
        var spy_getSortColumn = spyOn(OverbookTables.currentTable, "getSortColumn");
        var spy_blockUI = spyOn($, "blockUI");
        var spy_getJSON = spyOn($, "getJSON");

        OverbookTables.populateOverbookTablesTable(true, 42);

        expect(spy_getSortOrder).toHaveBeenCalledTimes(1);
        expect(spy_getSortColumn).toHaveBeenCalledTimes(1);

        expect(spy_blockUI).toHaveBeenCalledTimes(1);

        expect(spy_getJSON).toHaveBeenCalledTimes(1);
        expect(spy_getJSON.calls.argsFor(0)).toEqual([
            expectedUrl, jasmine.any(Function)]);
    });
});
