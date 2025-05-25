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
 * Created by carl on 5/11/17.
 */
describe('Filter.getAllFilterKeyValuePairs', function () {

    it('produces array of filterKeyValue pairs', function () {
        var filter = new Filter();

        var result = filter.getAllFilterKeyValuePairs();

        expect(result).toEqual([]);

        filter.filterKeys = { 0: 'zero', 1: 'one'};

        result = filter.getAllFilterKeyValuePairs();

        var expectedArray = [
            {key: '0', value: 'zero'},
            {key: '1', value: 'one'}
        ];
        expect(result).toEqual(expectedArray);
    });
});
describe('Filter.initFilter', function () {

    it('sets up widths and filters for each table column', function () {

        var fakeTable = {
            getId: function() {},
            getColumns: function() {}
        };

        var fakeColumn1 = {
            getColumnWidth: function() {},
            getFilter: function() {},
            getDbColumn: function() {},
            getFilterPrompt: function() {}
        };

        var fakeColumn2 = {
            getColumnWidth: function() {},
            getFilter: function() {},
            getDbColumn: function() {},
            getFilterPrompt: function() {}
        };

        var fakeColumnFilter1 = {
            ignore: 1,
            name: 2,
            type: 3,
            inputType: 4,
            maskFormat: 5
        };

        var fakeColumnFilter2 = {
            ignore: 21,
            name: 22,
            type: 23,
            inputType: 24,
            maskFormat: 25
        };

        var fakeColumns = [fakeColumn1, fakeColumn2];
        var fakeFilterRow = {
            id: 0,
            append: function() {},
            attr: function() {}
        };

        var headerCell1 = { css: function() {}, append: function() {} };
        var headerCell2 = { css: function() {}, append: function() {} };
        var searchField = {
            prop: function(one, two) {return this;},
            textbox: function() {
                return { off: function() {}, unbind: function(){}
                }
            }
        };
        var filterField = { textbox: function(){}} ;

        var filter = new Filter();

        var spy_$ = spyOn(window, "$").and.returnValues(headerCell1, searchField, headerCell2, filterField);

        var spy_getId = spyOn(fakeTable, "getId").and.returnValue("42");
        var spy_attr = spyOn(fakeFilterRow, "attr");

        var spy_getColumns = spyOn(fakeTable, "getColumns").and.returnValue(fakeColumns);

        var spy_getColumnWidth1 = spyOn(fakeColumn1, "getColumnWidth").and.returnValue(42);
        var spy_getColumnWidth2 = spyOn(fakeColumn2, "getColumnWidth").and.returnValue(undefined);

        var spy_append = spyOn(fakeFilterRow, "append");

        var spy_getFilter1 = spyOn(fakeColumn1, "getFilter").and.returnValue(fakeColumnFilter1);
        var spy_getFilter2 = spyOn(fakeColumn2, "getFilter").and.returnValue(undefined);

        var spy_getDbColumn1 = spyOn(fakeColumn1, "getDbColumn").and.returnValue('col1');
        var spy_getDbColumn2 = spyOn(fakeColumn2, "getDbColumn").and.returnValue('col2');

        var spy_getFilterPrompt1 = spyOn(fakeColumn1, "getFilterPrompt").and.returnValue('pr1');
        var spy_getFilterPrompt2 = spyOn(fakeColumn2, "getFilterPrompt").and.returnValue('pr2');

        var spy_getFilterFieldId = spyOn(filter, "getFilterFieldId").and.returnValue(142);

        var spy_prop = spyOn(searchField, "prop").and.returnValue(searchField);
        var spy_textbox = spyOn(searchField, "textbox").and.callThrough();

        var spy_createSearchTextBox = spyOn(WidgetUtil, "createSearchTextBox");

        filter.initFilter(fakeTable, fakeFilterRow);

        expect(spy_$).toHaveBeenCalledTimes(4);
        expect(spy_$.calls.argsFor(0)).toEqual(["<td></td>", {id: '42' + "-headerCell" + 0}]);
        expect(spy_$.calls.argsFor(1)).toEqual(["<input type='text'/>"]);
        expect(spy_$.calls.argsFor(2)).toEqual(["<td></td>", {id: '42' + "-headerCell" + 1}]);
        expect(spy_$.calls.argsFor(3)).toEqual([".filterField"]);

        expect(spy_prop).toHaveBeenCalledTimes(3);
        expect(spy_prop.calls.argsFor(0)).toEqual(["id", 142]);
        expect(spy_prop.calls.argsFor(1)).toEqual(["class", "filterField"]);
        expect(spy_prop.calls.argsFor(2)).toEqual(["data-key", 2]);

        expect(spy_getColumns).toHaveBeenCalledTimes(1);
        expect(spy_attr).toHaveBeenCalledTimes(1);
        expect(spy_getId).toHaveBeenCalledTimes(3);

        expect(spy_getColumnWidth1).toHaveBeenCalledTimes(2);
        expect(spy_getColumnWidth2).toHaveBeenCalledTimes(1);

        expect(spy_append).toHaveBeenCalledTimes(2);

        expect(spy_getFilter1).toHaveBeenCalledTimes(1);
        expect(spy_getFilter2).toHaveBeenCalledTimes(1);

        expect(spy_getDbColumn1).toHaveBeenCalledTimes(1);
        expect(spy_getDbColumn2).toHaveBeenCalledTimes(0);

        expect(spy_getFilterFieldId).toHaveBeenCalledTimes(1);

        expect(spy_getFilterPrompt1).toHaveBeenCalledTimes(1);
        expect(spy_getFilterPrompt2).toHaveBeenCalledTimes(0);

        expect(spy_textbox).toHaveBeenCalledTimes(1);
        expect(spy_textbox.calls.argsFor(0)).toEqual(["textbox"]);

        expect(spy_createSearchTextBox).toHaveBeenCalledTimes(1);
    });
});

