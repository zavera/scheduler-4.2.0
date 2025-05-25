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
var selectedCheckboxes = new Map();
var SchedulerTables = function (options) {
    this.tableId = options.tableId;
    this.tableWrapperId = options.tableWrapperId ? options.tableWrapperId : SchedulerTables.generateWrapperId(this.tableId);
    this.arrayColumns = options.columns;
    this.reloadFn = options.reloadFn;
    this.paginationWidget = null;
    this.filterWidget = null;
    this.sortInfo = new SchedulerTables.SortInfo();
    this.totalCount = 0;
    this.numDisplayedRows = 0;
    this.selectedRows = [];
    this.selectedRowIndexes = [];
    this.expandFn = options.expandFn;
    this.doubleClickRowIdCallback = options.doubleClickRowIdCallback;
    this.tableSpecificExpandRowClass = null;
    this.tableSpecificGroupRowClass = this.tableId + "-tableGroupHeader";
    this.groupBy = options.groupBy;
    this.rowSelectionColumnIndex = null;
    this.fixedHeader = options.fixedHeader || false;
    this.maxHeight = options.maxHeight || "";
    this.hasFilters = false; // gets set to true if any column has filter
    this.overrideDefaultTableClass = "datagridNew";
    this.tableRowIdPrefix = this.tableId + "-tableRow-";
    this.emptyDataMessage = options.emptyDataMessage ? options.emptyDataMessage : undefined;
    this.emptyDataMessageClass = options.emptyDataMessageClass;
    this.rowSelectFnFactory = options.rowSelectFnFactory;

    this.getId = function () {
        return this.tableId;
    };

    this.getWrapperId = function () {
        return this.tableWrapperId;
    };

    this.getColumns = function () {
        return this.arrayColumns;
    };

    this._addGroupHeaderRow = function (tableRow, value, backgroundClass) {
        backgroundClass = backgroundClass || "";
        var groupHeaderRow = $("<tr></tr>", {
            class: this.tableSpecificGroupRowClass + " tableGroupHeaderRow " + backgroundClass
        });

        var groupHeaderRowTd = $("<td></td>", {
            colspan: this.arrayColumns.length
        });

        if (this.groupBy.render) {
            value = this.groupBy.render(value);
        }

        groupHeaderRowTd.append(value);
        groupHeaderRow.append(groupHeaderRowTd);
        groupHeaderRow.insertBefore(tableRow);
    };

    this._getFixedHeaderId = function() {
        return this.tableId + "-fixedHeader";
    };

    this._getFixedHeaderWrapperId = function() {
        return this.tableId + "-header";
    };

    this._createFixHeaderTable = function() {
        var headerId = this._getFixedHeaderId();
        var tableElement = $('<table></table>', { id: headerId });

        var headerWrapperId = this._getFixedHeaderWrapperId();
        var headerWrapper = $("<div></div>", {"id": headerWrapperId});
        headerWrapper.addClass("CSSTableGenerator");
        headerWrapper.css({"overflow-y": "scroll"});
        $('#' + this.tableWrapperId).before(headerWrapper);
        headerWrapper.prepend(tableElement);

        var headerTableTHead = $("<thead></thead>");
        tableElement.append(headerTableTHead);

        var headerTableTBody = $("<tbody></tbody>");
        tableElement.append(headerTableTBody);
    };

    this._createTableHeader = function () {
        var table = this;
        var tableWrapper = $('#' + this.getWrapperId());
        var tableElement = $('#' + this.getId());

        if(!this.fixedHeader)
        {
            var tHead = $("<thead></thead>");
            tableElement.append(tHead);
        }

        var tableBody = $("<tbody></tbody>");
        tableElement.append(tableBody);

        var headerId = this.tableId;
        if(this.fixedHeader) {
            this._createFixHeaderTable();
            headerId = this._getFixedHeaderId();

            tableWrapper.addClass(this.overrideDefaultTableClass).css({"max-height": 0});
        }

        if (this.rowSelectFnFactory) {
            tableWrapper.addClass("hoverable");
        }

        var tableRow = $('<tr></tr>', {"id": table.getId() + "-columnLabelRow"});
        $("#" + headerId + " tbody").append(tableRow);

        $.each(table.arrayColumns, function (indexOfThis, column) {
            column.generateTableHeadElement(tableRow, function (column) {
                table._doSort(column, true);
            });

            // in addition to setting onSort above, set the order if this
            // column is 'special', i.e., is the one with defaultSortOrder
            if (column.getDefaultSortOrder()) {
                table.sortInfo.setSortOrder(column.getDefaultSortOrder());
                table.sortInfo.setSortColumn(column.getDbColumn());
            }
        });

        this.refreshTableColumnsSorting();
    };

    this._toggleValue = function (currentValue, option1, option2) {
        return currentValue === option1 ? option2 : option1;
    };

    this._createTableBody = function (data, totalCount) {
        if(this.rowSelectionColumnIndex != null) {
            var selectAllElemId = this.arrayColumns[this.rowSelectionColumnIndex].getType().getHeaderElementId() ;
            $("#" + selectAllElemId).prop("checked", false);
            if(data.length == 0) {
                $("#" + selectAllElemId).prop("disabled", true);
            }
            else {
                $("#" + selectAllElemId).prop("disabled", false);
            }
        }

        var table = this;
        var tableBody = $('#' + table.getId() + ' tbody');

        var rowHighlightClass = "expandableTable-rowHighlight";
        var noRowHighlightClass = "expandableTable-noRowHighlight";

        var backgroundClass = "";
        table.totalCount = totalCount !== undefined ? totalCount : 0;
        table.numDisplayedRows = data.length;

        var emptyDataMessageId = 'emptyDataMessage_'+ table.getId();
        var tableWrapperElement = $('#' + table.tableWrapperId);
        $('#' + emptyDataMessageId).remove();

        if (table.numDisplayedRows === 0 && this.emptyDataMessage) {
            var noData = $("<div></div>").attr("id", emptyDataMessageId);
            noData.html(this.emptyDataMessage);
            if(this.emptyDataMessageClass){
                noData.addClass(this.emptyDataMessageClass);
            }else {
                noData.addClass('emptyTableData');
            }
            tableWrapperElement.append(noData);
        }

        if(table.fixedHeader) {
            $("#" + table.tableWrapperId).css({"max-height": table.maxHeight,
                "overflow-y": "scroll"});
        }

        $.each(data, function (index, val) {

            if (index === 0) {
                currentPage = val.navigatePage + 1;
                table.setCurrentPage(currentPage);
            }

            if(totalCount === undefined) {
                table.totalCount = val.totalCount;
            }

            var tableRowId = table.tableRowIdPrefix + index;
            var tableRow = $("<tr></tr>",  {"data-rowIndex": index, "id": tableRowId});
            tableRow.addClass(table.getId() + "-tableRow");

            if (table.rowSelectFnFactory) {
                tableRow.click(table.rowSelectFnFactory(tableRowId, val));
            }

            tableBody.append(tableRow);

            if (table.doubleClickRowIdCallback) {
                tableRow.bind("dblclick", function () {
                    table.doubleClickRowIdCallback(val.id);
                });
            }

            $.each(table.arrayColumns, function (columnIndex, column) {
                if (table.groupBy && table.groupBy.columnName === column.getColumnName()) {
                    var currentValue = column.extractData(val);
                    //if current value is different from previous value
                    if (index === 0 || table.groupBy.isNewGroup(currentValue, column.extractData(data[index - 1]))) {
                        backgroundClass = table._toggleValue(backgroundClass, noRowHighlightClass, rowHighlightClass);
                        table._addGroupHeaderRow(tableRow, currentValue, backgroundClass);
                    }
                }
                var cellId = table.getTableCellId(index, columnIndex);
                var tdElement = $('<td></td>', {id: cellId});
                tableRow.append(tdElement);

                column.generateTableElement(tdElement, val);
            });

            if (table.expandFn) {
                backgroundClass = table._toggleValue(backgroundClass, noRowHighlightClass, rowHighlightClass);
                tableRow.addClass(backgroundClass);
            }
        });
    };

    this.updateTableRow = function(val, tableRow){
        var table = this;
        var tableTds = tableRow.children("td");
        $.each(table.arrayColumns, function (columnIndex, column) {
            var tdElement = $(tableTds.get(columnIndex));
            tdElement.empty();
            column.generateTableElement(tdElement, val);
        });
    };

    this._generateColumnId = function (index) {
        return this.getId() + "_" + index;
    };

    this._setupColumns = function () {
        var table = this;

        if (table.groupBy && !table.groupBy.isNewGroup) {
            table.groupBy.isNewGroup = function (oldValue, newValue) {
                return oldValue !== newValue
            };
        }

        var expandRowClass = "expandRow";
        table.tableSpecificExpandRowClass = expandRowClass + "-" + table.getId();
        $.each(table.arrayColumns, function (index, column) {

            if (column.getFilter()) {
                table.hasFilters = true;
            }

            if(column.getType() instanceof Column.Checkbox) {
                table.rowSelectionColumnIndex = index;
            }

            column.setTdId(table._generateColumnId(index));

            column.getType().setOptions({
                onSelect: function (value, checked) {
                    var tdElement = $(this).parents("td").first();
                    var rowIndex = parseInt(table.getRowIndex(tdElement));
                    if (checked) {
                        // avoid duplicates. if there is a spinner in this row, then every click
                        //    to 'spin' the value triggers this onSelect with checked=true!
                        var index = $.inArray(rowIndex, table.selectedRowIndexes);
                        if (index === -1) {
                            table.selectedRowIndexes.push(rowIndex);
                        }
                        index = $.inArray(value, table.selectedRows);
                        if (index === -1) {
                            table.selectedRows.push(value);
                        }
                    }
                    else {
                        table.unSelectRow(value);
                        table.clearRowValues(rowIndex);
                    }
                },
                onExpand: function (tdElement, value) {
                    var parentRow = tdElement.parent("tr");
                    var expandRow = $("<tr></tr>").addClass(expandRowClass)
                        .addClass(table.tableSpecificExpandRowClass)
                        .insertAfter(parentRow);

                    var parentRowBg = parentRow.css("background-color");
                    expandRow.css({"background-color": parentRowBg});

                    var expandRowTd = $("<td></td>", {colspan: table.arrayColumns.length}).appendTo(expandRow);
                    var containerDiv = $("<div></div>").appendTo(expandRowTd);
                    table.expandFn(containerDiv, value);
                },
                onCollapse: function (tdElement) {
                    var parentRow = tdElement.parent("tr");
                    parentRow.next("." + expandRowClass).remove();
                }
            });

            if(column.options !== undefined && column.options.filter !== undefined &&
                column.options.filter.onFilter !== undefined){

                column.options.filter.beforeFilter = function(value)
                {
                    table.setCurrentPage(1);
                }
            }
        });
    };

    this.checkRowById = function(checkboxId)
    {
        $("#" + checkboxId).prop("checked", true).change();
    };

    this.selectRow = function(rowIndex) {
        var index = $.inArray(rowIndex, this.selectedRowIndexes);
        if (index === -1) {
            var column = this.arrayColumns[this.rowSelectionColumnIndex];
            var tdElementId = this.getTableCellId(rowIndex, this.rowSelectionColumnIndex);
            var tdElement = $("#" + tdElementId);

            var checkBoxClass = column.getType().getCheckboxElementClass();
            tdElement.find("." + checkBoxClass).prop("checked", true).change();
        }
    };

    this.unSelectRow = function (value) {
        var index = $.inArray(value, this.selectedRows);
        this.selectedRows.splice(index, 1);
        this.selectedRowIndexes.splice(index, 1);
    };

    this.clearAllSelectedRows = function () {
        this.selectedRows = [];
        this.selectedRowIndexes = [];
    };

    this.checkAllRows = function () {
        if(this.rowSelectionColumnIndex != null) {
            var selectAllElemId = this.arrayColumns[this.rowSelectionColumnIndex].getType().getHeaderElementId() ;
            $("#" + selectAllElemId).prop("checked", true).change();
        }
    };

    this.checkAllRowById = function (rowId) {
        if(this.rowSelectionColumnIndex != null) {
            $("#" + rowId).prop("checked", true).change();
        }
    };

    this.getRowIndex = function(tableElement)
    {
        var rowIndex = parseInt(tableElement.parents("tr").first().attr("data-rowIndex")) ;
        if(isNaN(rowIndex))
        {
            rowIndex = -1;
        }

        return rowIndex;
    };

    this._setup = function () {
        $('#' + this.getId()).remove();
        $('#' + this._getFixedHeaderWrapperId()).remove();

        var tableElement = $('<table></table>', { id: this.tableId });
        $('#' + this.tableWrapperId).append(tableElement);

        if (this.expandFn) {
            $('#' + this.tableId).addClass("expandableTable");
        }
        this._setupColumns();
    };

    this.generateTable = function (data, maxResults, paginationContainerId, paginationReload, displayRecordsReload) {

        this._setup();
        this._createTableHeader();

        this._createTableBody(data);
        //SchedulerTables.reselectCheckboxes(this.getId(), this.totalCount, currentSelected);

        if (paginationContainerId && paginationReload) {
            var table = this;
            this.paginationWidget = PaginationHelper.initPagination(
                paginationContainerId,
                table.totalCount,
                maxResults,
                paginationReload,
                displayRecordsReload
            );
        }

        if (this.hasFilters) {
            this.filterWidget = new Filter();

            var tableHeaderId;
            if(this.fixedHeader) {
                tableHeaderId = this._getFixedHeaderId();
            }
            else {
                tableHeaderId = this.getId();
            }

            var tableHead = $("#" + tableHeaderId + " thead");

            var filterRow = $("<tr></tr>");
            tableHead.append(filterRow);

            this.filterWidget.initFilter(this, filterRow);
        }
    };

    this.refreshTableColumnsSorting = function () {
        this.arrayColumns.map(function (column) {
            column.refreshClass();
        });
    };

    this.clearTableBody = function () {
        return this.refreshTableBody([], 0);
    };

    this.refreshTableBody = function (data, totalData) {

        this.selectedRows = [];
        this.selectedRowIndexes = [];

        $('.' + this.getId() + '-tableRow').each(function () {
            $(this).remove();
        });

        $('.' + this.tableSpecificGroupRowClass).each(function () {
            $(this).remove();
        });

        $('.' + this.tableSpecificExpandRowClass).each(function () {
            $(this).remove();
        });

        this._createTableBody(data, totalData, null);
        if (this.paginationWidget) {
            PaginationHelper.updatePagination(this.paginationWidget, this.totalCount);
        }
    };

    this.getSelectedRows = function () {
        return this.selectedRows;
    };

    this.getSelectedRowIndexes = function () {
        return this.selectedRowIndexes;
    };

    this.getTableCellId = function(rowIndex, columnIndex) {
       return this.getId() + "-" + rowIndex + "_" + columnIndex;
    };

    this.getDataForSelectedRows = function(columnIndex) {
        var values = [];
        var table = this;
        $.each(table.selectedRowIndexes, function(index, rowIndex){
            var rowData = [];

            var tableColumns = table.arrayColumns;
            if(columnIndex) {
                tableColumns = table.arrayColumns[columnIndex];
            }

            $.each(tableColumns, function(columnIndex, column){
                var cellId = table.getTableCellId(rowIndex, columnIndex);
                var tdElement = $("#" + cellId);
                var cellValue = column.extractCellValue(tdElement, cellId);
                rowData.push(cellValue);
            });

            if(columnIndex !== undefined) {
                values.push(rowData[0]);
            }
            else {
                values.push(rowData);
            }
        });

        return values;
    };

    this.getTableData = function(rowIndex, columnIndex) {
        var values = [];
        var table = this;

        var maxRows = table.getNumRows() - 1;
        var r = 0;
        if(rowIndex !== undefined) {
            r = rowIndex;
            maxRows = rowIndex;
        }
        for(;r <= maxRows; r++){
            var rowData = [];

            var tableColumns = table.arrayColumns;
            if(columnIndex !== undefined) {
                tableColumns = [table.arrayColumns[columnIndex]];
            }

            $.each(tableColumns, function (index, column) {
                var cIndex = index;
                if(columnIndex !== undefined)
                {
                    cIndex = columnIndex;
                }

                var cellId = table.getTableCellId(r, cIndex);
                var tdElement = $("#" + cellId);
                var cellValue = column.extractCellValue(tdElement, cellId);
                rowData.push(cellValue);
            });

            if(columnIndex !== undefined) {
                values.push(rowData[0]);
            }
            else {
                values.push(rowData);
            }
        }

        return values;
    };

    this.getRecordsPerPage = function () {
        var recordsPerPage;
        if (this.paginationWidget) {
            recordsPerPage = this.paginationWidget.getRecordsPerPage();
        }

        return recordsPerPage;
    };

    this.getCurrentPage = function () {
        return this.paginationWidget ? this.paginationWidget.getCurrentPageNumber() : 1;
    };

    this.setCurrentPage = function (curr) {
        if (this.paginationWidget && curr) {
            this.paginationWidget.setCurrentPageNumber(curr);
        }
    };

    this.getAllFilterKeyValuePairs = function () {

        return this.filterWidget ? this.filterWidget.getAllFilterKeyValuePairs() : [];
    };

    this.clearAllFilterKeyValuePairs = function () {
        this.filterWidget && this.filterWidget.clearAllFilterKeyValuePairs();
    };

    this.removeFilterKeyValuePair = function (key) {
        if(this.filterWidget) {
            this.filterWidget.removeFilterKeyValuePair(key);
        }
    };

    this.getFilterObjByName = function (filterName) {

        return this.filterWidget ? this.filterWidget.getFilterObjByName(filterName) : null;
    };

    this.getFilterField = function(filterName) {
        return this.filterWidget ? this.filterWidget.getFilterField(this, filterName) : null;
    };

    this.setFilterKeyValuePair = function (key, value) {

        if(this.filterWidget)
        {
            this.filterWidget.setKeyValuePair(key, value);
        }
    };

    this._doSort = function (column) {
        if (!column.getDbColumn()) {
            // not sortable
            return;
        }

        this.sortInfo.setSortOrder(column.toggleOrInitOrderBy());
        this.sortInfo.setSortColumn(column.getDbColumn());

        this.resetSortOrderBy();
        column.setCurrentOrderBy(this.getSortOrder());

        this.refreshTableColumnsSorting();

        this.setCurrentPage(1);
        this.reloadFn();
    };

    this.getSortOrder = function () {
        return this.sortInfo.getSortOrder();
    };

    this.resetSortOrderBy = function () {
        $.each(this.arrayColumns, function (index, column) {
            column.setCurrentOrderBy("");
        });
    };

    this.getSortColumn = function () {
        return this.sortInfo.getSortColumn();
    };

    this.clearRowValues = function(rowIndex) {
        var table = this;
        $.each(table.arrayColumns, function(columnIndex, column){
            var cellId = table.getTableCellId(rowIndex, columnIndex);
            var tdElement = $("#" + cellId);

            column.clearCellValue(tdElement, cellId);
        });
    };

    this.getNumColumns = function() {
        return this.arrayColumns.length;
    };

    this.getNumRows = function() {
        return this.numDisplayedRows;
    };

    this.getTotalCount = function() {
        return this.totalCount;
    };

    this.destroy = function() {
        if (this.fixedHeader) {
            $('#' + this._getFixedHeaderWrapperId()).remove();
            $('#' + this.getWrapperId()).removeClass(this.overrideDefaultTableClass);
        }
        $('#' + this.getWrapperId()).empty();
    };

    this.getDataRowElementAtIndex = function(rowIndex) {
        return $("#" + this.tableRowIdPrefix + rowIndex);
    };

    this.addFooter = function(tableFooter){
        $("#" + this.getId()).append(tableFooter);
    };

    this.editRow = function(tableRow, val){
        var table = this;
        var tableTds = tableRow.children("td");

        $.each(tableTds, function (columnIndex, tdElement) {
            var column = table.arrayColumns[columnIndex];

            if(column.options.editFunction) {
                column.options.editFunction($(tdElement), val);
            }
        });
    };

    this.setEmptyDataMessage = function(message){
        this.emptyDataMessage = message;
    };

    this.setEmptyDataMessageClass = function(messageClass){
        this.emptyDataMessageClass = messageClass;
    };
};

SchedulerTables.SortInfo = function (sortColumn, sortOrder) {
    this.sortColumn = sortColumn;
    this.sortOrder = sortOrder;

    this.setSortColumn = function (sortColumn) {
        this.sortColumn = sortColumn;
    };

    this.getSortColumn = function () {
        return this.sortColumn;
    };

    this.setSortOrder = function (sortOrder) {
        this.sortOrder = sortOrder;
    };

    this.getSortOrder = function () {
        return this.sortOrder;
    };
};


SchedulerTables.getSelectedCheckboxes = function (tableId) {
    return selectedCheckboxes.get(tableId);
};

SchedulerTables.clearSelectedCheckboxes = function (tableId) {
    selectedCheckboxes.set(tableId, []);
};

SchedulerTables.addSelectedCheckbox = function (tableId, value) {
    if (selectedCheckboxes.get(tableId)) {
        selectedCheckboxes.get(tableId).push(value);
    }
    else {
        selectedCheckboxes.set(tableId, []);
        selectedCheckboxes.get(tableId).push(value);
    }
};

SchedulerTables.setSelectedCheckboxes = function (tableId, arrayOfSelected) {
    selectedCheckboxes.set(tableId, arrayOfSelected);
};

SchedulerTables.reselectCheckboxes = function (tableId, totalData, currentSelected) {
    SchedulerTables.setSelectedCheckboxes(tableId, currentSelected);

    if (totalData === SchedulerTables.getSelectedCheckboxes(tableId).length && totalData !== 0) {
        $('#' + tableId + '-selectAll').prop('checked', true);
    }
    else {
        $('#' + tableId + '-selectAll').prop('checked', false);
    }
};

SchedulerTables.selectAllCheckboxes = function (tableId) {
    var checkboxes = $('.' + tableId + '-checkbox');
    for (var i = 0; i < checkboxes.length; i++) {
        var checkbox = $(checkboxes[i]);
        if (($.inArray(checkbox.val(), SchedulerTables.getSelectedCheckboxes(tableId)) == -1)) {
            if ($.isNumeric(checkbox.val())) {
                this.addSelectedCheckbox(tableId, parseInt(checkbox.val()));
            }
            checkbox.prop('checked', true);
        }
        else if (checkbox.checked != true) {
            checkbox.prop('checked', true);
        }
    }
    $('#' + tableId + '-selectAll').prop('checked', true);
};

SchedulerTables.deselectAllCheckboxes = function (tableId) {
    var checkboxes = $('.' + tableId + '-checkbox');
    this.clearSelectedCheckboxes(tableId);
    checkboxes.prop('checked', false);
    $('#' + tableId + '-selectAll').prop('checked', false);
};

SchedulerTables.suffix = "Wrapper";
SchedulerTables.generateWrapperId = function(tableId) {
    return tableId + SchedulerTables.suffix;
};

