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
function Filter() {
    this.filterKeys = {};
    this.filterTypes = {};
    this.ignoreKeys = {};

    var ignoreKeys = this.ignoreKeys;

    this.initFilter = function (table, filterRow) {

        var filter = this;
        filterRow.attr("id", table.getId() + "-filterRow");

        var filterClass = "filterField";

        $.each(table.getColumns(), function (columnIndex, column) {
            var headerCell = $("<td></td>", {id: table.getId() + "-headerCell" + columnIndex});

            if (column.getColumnWidth()) {
                headerCell.css({"width": column.getColumnWidth() + "%"});
            }
            filterRow.append(headerCell);
            var columnFilter = column.getFilter();
            if (columnFilter !== undefined) {

                if (columnFilter.ignore) {
                    ignoreKeys[column.getDbColumn()] = true;
                }

                if (columnFilter.type) {
                    filter.filterTypes[columnFilter.name] = columnFilter.type;
                }

                var searchFieldId = filter.getFilterFieldId(table, columnFilter.name);
                var searchField = $("<input type='text'/>")
                    .prop("id", searchFieldId)
                    .prop("class", filterClass)
                    .prop("data-key", columnFilter.name);

                headerCell.append(searchField);

                WidgetUtil.createSearchTextBox(searchField, {
                    prompt: column.getFilterPrompt(),
                    type: {
                        name: columnFilter.inputType
                    },
                    defaultValue: columnFilter.defaultValue,
                    maskFormat: columnFilter.maskFormat,
                    err: function(){}, //this is a workaround to address conflict between and jQuery UI tooltip and jqQuery EasyUI tooltip
                    onSearch: function () {
                        var key = $(this).prop("data-key");
                        var textbox = $(this).textbox("textbox");
                        var value = $.trim(textbox.val());

                        var placeholderValue = textbox.prop("placeholder");
                        if (placeholderValue === value || value === "") {
                            delete filter.filterKeys[key];
                        }
                        else {
                            filter.filterKeys[key] = value;
                        }

                        if(columnFilter.beforeFilter !== undefined) {
                            columnFilter.beforeFilter(value);
                        }
                        columnFilter.onFilter(value);
                    }
                });

                //prevents an onchange event from being indirectly triggered
                searchField.textbox("textbox").unbind("blur.textbox");
            }
        });
        $("." + filterClass).textbox("resize");
    };

    this.setKeyValuePair = function (key, value) {
        this.filterKeys[key] = value;
    };

    this.getAllFilterKeyValuePairs = function () {
        var filter = this;
        var filterKeyValuePair = [];
        var keys = Object.keys(this.filterKeys);
        $.each(keys, function (index, key) {
            if (!ignoreKeys[key]) {
                var keyValuePair = {
                    key: key,
                    value: filter.filterKeys[key]
                };

                if (filter.filterTypes[key]) {
                    keyValuePair.searchType = filter.filterTypes[key];
                }
                filterKeyValuePair.push(keyValuePair);
            }
        });

        return filterKeyValuePair;
    };

    this.clearAllFilterKeyValuePairs = function () {
        this.filterKeys = {};
    };

    this.removeFilterKeyValuePair = function (key) {
        if (this.filterKeys[key]) {
            delete this.filterKeys[key];
        }
    };

    this.getFilterObjByName = function (filterName) {
        var keyValuePair = null;
        if (this.filterKeys[filterName]) {
            keyValuePair = {
                key: filterName,
                value: this.filterKeys[filterName]
            };

            if (this.filterTypes[filterName]) {
                keyValuePair.searchType = this.filterTypes[filterName];
            }
        }
        return keyValuePair;
    };

    this.getFilterFieldId = function (table, filterName) {
        return table.getId() + "-filter-" + filterName;
    };

    this.getFilterField = function (table, filterName) {
        var filterFieldId = this.getFilterFieldId(table, filterName);

        return $("#" + escapeJQSelector(filterFieldId));
    };
}

Filter.Type = {
    EXACT: "EXACT"
};




