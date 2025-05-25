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
describe('The table pagination widget', function () {

    // TODO: so far I only have a very simplistic test, but enough to verify that the table pagination widget
    // gets created and inserted into the DOM. Will go back to fleshing out the tests
    // when we work on integrating the various table-related widgets
    it('changes state when interacted with by clicking the controls', function() {

        FixtureHelper.loadTestHtmlFixtureIntoDom('util/html/elements-for-table-pagination.html');

        // Before the widget is created

        var paginationElement = $("#table-pagination-element");
        var selectBox = $(".selectBox");
        var paginationBlock = $(".pagination_block");

        expect(paginationElement.length).toBe(1);
        expect(paginationElement.html()).toBe('');
        expect(selectBox.length).toBe(0);
        expect(paginationBlock.length).toBe(0);

        var widget;
        var config = {
            initialRecordsPerPage: 5,
            paginationElementId: "table-pagination-element",
            paginationEventHandler: function() {
                widget.updatePagination({
                    totalNumberOfPages: 100
                });
            },
            postUpdateCallback: null
        };
        widget = tablePaginationWidget(config);
        widget.generatePaginationElements();
        widget.initTableData();

        // CASE 1 : out of the box, at the first page

        var paginationElement = $("#table-pagination-element");
        var selectBox = $(".selectBox");
        var paginationBlock = $(".pagination_block");

        verifyBasicWidgetStructure(paginationElement, selectBox, paginationBlock);

        var paginationElementChildren = paginationElement.children('div');
        var paginationDiv = $(paginationElementChildren.get(1));

        var paginationSubDiv = paginationDiv.children();
        expect(paginationSubDiv.length).toBe(1);
        DomVerificationHelper.verifyCssClasses(paginationSubDiv[0], []);
        expect(paginationSubDiv.attr('id')).toBe('pagination');

        var pageTextInput = paginationSubDiv.children('input[type=text]');
        expect(pageTextInput.length).toBe(1);
        DomVerificationHelper.verifyCssClasses(pageTextInput[0], ["input_page", "numbersOnly"]);

        var gotoPageButton = paginationSubDiv.children('input[type=button]');
        expect(gotoPageButton.length).toBe(1);
        DomVerificationHelper.verifyCssClasses(gotoPageButton[0], ["formButton", "goButton"]);
        expect(gotoPageButton.attr('id')).toBe("table-pagination-element_gotoPageButton");

        expect(paginationSubDiv.children('img').length).toBe(2);

        var icon1 = paginationSubDiv.children('img:eq(0)');
        expect(icon1.length).toBe(1);
        expect(icon1.attr('src')).toBe('css/images/gantt/forward_arrow.png');
        expect(icon1.attr('id')).toBe('table-pagination-element_nextPageLink');
        expect(icon1.css('vertical-align')).toBe('middle');

        var icon2 = paginationSubDiv.children('img:eq(1)');
        expect(icon2.length).toBe(1);
        expect(icon2.attr('src')).toBe('css/images/gantt/allthewayforward_arrow.png');
        expect(icon2.attr('id')).toBe('table-pagination-element_lastPageLink');
        expect(icon2.css('vertical-align')).toBe('middle');

        // CASE 2 : click to jump to last page

        $('#table-pagination-element_lastPageLink').trigger('click');

        verifyBasicWidgetStructure(paginationElement, selectBox, paginationBlock);

        var paginationElementChildren = paginationElement.children('div');
        var paginationDiv = $(paginationElementChildren.get(1));

        var paginationSubDiv = paginationDiv.children();
        expect(paginationSubDiv.length).toBe(1);
        DomVerificationHelper.verifyCssClasses(paginationSubDiv[0], []);
        expect(paginationSubDiv.attr('id')).toBe('pagination');

        var pageTextInput = paginationSubDiv.children('input[type=text]');
        expect(pageTextInput.length).toBe(1);
        DomVerificationHelper.verifyCssClasses(pageTextInput[0], ["input_page", "numbersOnly"]);

        var gotoPageButton = paginationSubDiv.children('input[type=button]');
        expect(gotoPageButton.length).toBe(1);
        DomVerificationHelper.verifyCssClasses(gotoPageButton[0], ["formButton", "goButton"]);
        expect(gotoPageButton.attr('id')).toBe("table-pagination-element_gotoPageButton");

        expect(paginationSubDiv.children('img').length).toBe(2);

        var icon1 = paginationSubDiv.children('img:eq(0)');
        expect(icon1.length).toBe(1);
        expect(icon1.attr('src')).toBe('css/images/gantt/allthewayback_arrow.png');
        expect(icon1.attr('id')).toBe('table-pagination-element_firstPageLink');
        expect(icon1.css('vertical-align')).toBe('middle');

        var icon2 = paginationSubDiv.children('img:eq(1)');
        expect(icon2.length).toBe(1);
        expect(icon2.attr('src')).toBe('css/images/gantt/back_arrow.png');
        expect(icon2.attr('id')).toBe('table-pagination-element_previousPageLink');
        expect(icon2.css('vertical-align')).toBe('middle');

        // CASE 3 : click to go back one page

        $('#table-pagination-element_previousPageLink').trigger('click');

        verifyBasicWidgetStructure(paginationElement, selectBox, paginationBlock);

        var paginationElementChildren = paginationElement.children('div');
        var paginationDiv = $(paginationElementChildren.get(1));

        var paginationSubDiv = paginationDiv.children();
        expect(paginationSubDiv.length).toBe(1);
        DomVerificationHelper.verifyCssClasses(paginationSubDiv[0], []);
        expect(paginationSubDiv.attr('id')).toBe('pagination');

        var pageTextInput = paginationSubDiv.children('input[type=text]');
        expect(pageTextInput.length).toBe(1);
        DomVerificationHelper.verifyCssClasses(pageTextInput[0], ["input_page", "numbersOnly"]);

        var gotoPageButton = paginationSubDiv.children('input[type=button]');
        expect(gotoPageButton.length).toBe(1);
        DomVerificationHelper.verifyCssClasses(gotoPageButton[0], ["formButton", "goButton"]);
        expect(gotoPageButton.attr('id')).toBe("table-pagination-element_gotoPageButton");

        expect(paginationSubDiv.children('img').length).toBe(4);

        var icon1 = paginationSubDiv.children('img:eq(0)');
        expect(icon1.length).toBe(1);
        expect(icon1.attr('src')).toBe('css/images/gantt/allthewayback_arrow.png');
        expect(icon1.attr('id')).toBe('table-pagination-element_firstPageLink');
        expect(icon1.css('vertical-align')).toBe('middle');

        var icon2 = paginationSubDiv.children('img:eq(1)');
        expect(icon2.length).toBe(1);
        expect(icon2.attr('src')).toBe('css/images/gantt/back_arrow.png');
        expect(icon2.attr('id')).toBe('table-pagination-element_previousPageLink');
        expect(icon2.css('vertical-align')).toBe('middle');

        var icon3 = paginationSubDiv.children('img:eq(2)');
        expect(icon3.length).toBe(1);
        expect(icon3.attr('src')).toBe('css/images/gantt/forward_arrow.png');
        expect(icon3.attr('id')).toBe('table-pagination-element_nextPageLink');
        expect(icon3.css('vertical-align')).toBe('middle');

        var icon4 = paginationSubDiv.children('img:eq(3)');
        expect(icon4.length).toBe(1);
        expect(icon4.attr('src')).toBe('css/images/gantt/allthewayforward_arrow.png');
        expect(icon4.attr('id')).toBe('table-pagination-element_lastPageLink');
        expect(icon4.css('vertical-align')).toBe('middle');

    });

    function verifyBasicWidgetStructure(paginationElement, selectBox, paginationBlock) {

        expect(paginationElement.length).toBe(1);
        expect(selectBox.length).toBe(1);
        expect(paginationBlock.length).toBe(1);

        var children = paginationElement.children('div');
        expect(children.length).toBe(2);

        var numberOfPagesDiv = $(children.get(0));
        expect(numberOfPagesDiv.length).toBe(1);
        DomVerificationHelper.verifyCssClasses(numberOfPagesDiv[0], ['selectBox']);

        var numberOfPagesSelect = $(numberOfPagesDiv.children('select'));
        expect(numberOfPagesSelect.length).toBe(1);
        expect(numberOfPagesSelect.attr('id')).toBe('table-pagination-element_recordsPerPageSelector');
        DomVerificationHelper.verifyDropDown(numberOfPagesSelect,
            [
                {'value': '5', 'text': '5'},
                {'value': '10', 'text': '10'},
                {'value': '25', 'text': '25'},
                {'value': '50', 'text': '50'},
                {'value': '100', 'text': '100'}
            ]
        );

        var paginationDiv = $(children.get(1));
        expect(paginationDiv.length).toBe(1);
        DomVerificationHelper.verifyCssClasses(paginationDiv[0], ['pagination_block']);

    }
});

describe('The table widget', function () {
    var testTable;
    var helper1 = function (filterOption1) {
        var columns = [];

        var optionsCol1 = {
            dbColumn: 'dbCol1',
            columnName: "uiCol1",
            width: 42
        };

        if (filterOption1) {
            optionsCol1.filter = filterOption1;
        }

        columns.push(new Column(optionsCol1));

        testTable = new SchedulerTables({
            tableId: "testTable",
            columns: columns,
            reloadFn: function () {}
        });

        testTable.generateTable([]);
    };

    it('delegates clearing filter', function () {
        helper1({
            name: "filterCol1",
            onFilter: function (value) {}
        });

        var spy_inner_clearAllFilterKeyValuePairs =
            spyOn(testTable.filterWidget, "clearAllFilterKeyValuePairs")

        testTable.clearAllFilterKeyValuePairs();
        expect(spy_inner_clearAllFilterKeyValuePairs).toHaveBeenCalledTimes(1);

        spy_inner_clearAllFilterKeyValuePairs.calls.reset();
        helper1({name: "filterCol1"});

        testTable.clearAllFilterKeyValuePairs();
        expect(spy_inner_clearAllFilterKeyValuePairs).toHaveBeenCalledTimes(0);
    });
});

