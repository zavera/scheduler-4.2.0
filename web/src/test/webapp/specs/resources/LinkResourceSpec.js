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

describe('link_resources_table.js:LRT.showDaysHoursAndMinutes()', function () {
    it('returns indicates which (1-based) day of the appt, given origin date', function () {
        var minutes = 5 * MINS_PER_DAY + 73;
        var result = showDaysHoursAndMinutes(minutes);
        expect(result).toBe("Day 6 01:13");

        minutes = 1439;
        var result = showDaysHoursAndMinutes(minutes);
        expect(result).toBe("Day 1 23:59");

    });
});

describe('link_resources_table.js:LRT.pushColumns()', function () {
    it('generates the table', function () {
        var spy_Column = spyOn(window, "Column")
            .and.returnValue({});

        LRT.pushColumns([]);

        expect(spy_Column).toHaveBeenCalledTimes(5);
        expect(spy_Column.calls.argsFor(0)).toEqual([{
            columnType: Column.Checkbox,
            width: 2,
            extractDataFunction: jasmine.any(Function)
        }
        ]);
        expect(spy_Column.calls.argsFor(1)).toEqual([{
            columnName: LRT.uiColumnResourceName,
            width: 20,
            extractDataFunction: jasmine.any(Function)
        }
        ]);
        expect(spy_Column.calls.argsFor(2)).toEqual([{
            columnName: LRT.uiColumnResourceType,
            width: 10,
            extractDataFunction: jasmine.any(Function)
        }
        ]);
        expect(spy_Column.calls.argsFor(3)).toEqual([{
            columnName: LRT.uiColumnResourceStartTime,
            width: 10,
            extractDataFunction: jasmine.any(Function)
        }
        ]);
        expect(spy_Column.calls.argsFor(4)).toEqual([{
            columnName: LRT.uiColumnResourceEndTime,
            width: 10,
            extractDataFunction: jasmine.any(Function)
        }
        ]);

    });
});

describe('link_resources_table.js:LRT.loadVisitsIntoWidget()', function () {
    it('assembles some columns', function () {
        LRT.tableName = "fooTable"

        var pseudoTable = {generateTable: function(){}};

        var spy_pushColumns = spyOn(LRT, "pushColumns");
        var spy_SchedulerTables = spyOn(window, "SchedulerTables")
            .and.returnValue(pseudoTable);
        var spy_generateTable = spyOn(pseudoTable, "generateTable");
        var spy_populateLinkingResourcesTable = spyOn(LRT, "populateLinkingResourcesTable");

        LRT.loadVisitsIntoWidget(1, true);

        expect(spy_SchedulerTables).toHaveBeenCalledTimes(1);
        expect(spy_SchedulerTables.calls.argsFor(0)).toEqual([{
            tableId: "fooTable",
            columns: [],
            reloadFn: LRT.populateLinkingResourcesTable
        }]);

        expect(spy_generateTable).toHaveBeenCalledTimes(1);
        expect(spy_generateTable.calls.argsFor(0)).toEqual([
            [],
            50,
            null,
            null,
            jasmine.any(Function)
        ]);

        expect(spy_populateLinkingResourcesTable).toHaveBeenCalledTimes(1);
        expect(spy_populateLinkingResourcesTable.calls.argsFor(0)).toEqual([true, 1]);
    });
});

describe('link_resources_table.js:LRT.populateLinkingResourcesTable()', function () {
    it('populates the table', function () {
        LRT.currentTable = {
            setCurrentPage: function(){}
        };
        isLinkingFloat = true;

        app_selectedVisit = {id: 10};
        templateResourceId = 11;
        var expectedUrl = "rest/appointment/getGanttInfo/candidateFloatables?visit=" +
            app_selectedVisit.id +
            "&templateResource=" +
            templateResourceId

        var spy_setCurrentPage = spyOn(LRT.currentTable, "setCurrentPage");
        var spy_getJSON = spyOn($, "getJSON").and.returnValue({always: function(fn){fn();}});
        var spy_$ = spyOn(window, "$").and.callThrough();
        var spy_css = spyOn($.fn, "css");

        LRT.populateLinkingResourcesTable(true, 42);

        expect(spy_setCurrentPage).toHaveBeenCalledTimes(1);
        expect(spy_setCurrentPage.calls.argsFor(0)).toEqual([42]);

        expect(spy_getJSON).toHaveBeenCalledTimes(1);
        expect(spy_getJSON.calls.argsFor(0)).toEqual([
            expectedUrl, jasmine.any(Function)
            ]);

        expect(spy_$).toHaveBeenCalledTimes(1);
        expect(spy_$.calls.argsFor(0)).toEqual(['#table_loading']);

        expect(spy_css).toHaveBeenCalledTimes(1);
        expect(spy_css.calls.argsFor(0)).toEqual([{display: 'none'}]);
    });
});
