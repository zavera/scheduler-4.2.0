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

/*
 * options - id, dbColumnName, columnName, width, headerClass, rowElementClass, extractDataFunction
 * defaultSortOrder, columnType, orderBy, onChange, extractCellValue
 */
function Column(options) {
    this.options = options || {};
    this.options.columnType = this.options.columnType ? new options.columnType(this) : new Column.Text(this);
    this.options.orderBy = this.options.defaultSortOrder;
    this.options.extractCellValue = this.options.extractCellValue || function (tdElement) {
            return $(tdElement).text()
        };

    this.getFilterPrompt = function () {
        var column = this;

        if (this.options.filterPrompt) {
            return this.options.filterPrompt;
        }
        else {
            return column.getColumnName();
        }
    };

    this.getDefaultSortOrder = function () {
        return this.options.defaultSortOrder;
    };

    this.getDbColumn = function () {
        return this.options.dbColumn;
    };

    this.getSortingClass = function () {

        // sortable iff dbColumn is defined
        if (this.options.dbColumn === undefined) {
            return "not_sortable";
        }

        var ascOrDesc = this.getCurrentOrderBy();

        if (ascOrDesc === Column.DESC) {
            return Column.sorting_desc;
        }
        else if (ascOrDesc === Column.ASC) {
            return Column.sorting_asc;
        }
        else {
            return Column.sortable;
        }
    };

    this.refreshClass = function () {
        $('#' + this.getTdId()).removeClass();
        $('#' + this.getTdId()).addClass(this.getSortingClass());
    };


    this.getCurrentOrderBy = function () {
        return this.options.orderBy;
    };

    this.setCurrentOrderBy = function (value) {
        this.options.orderBy = value;
    };

    this.toggleOrInitOrderBy = function () {

        if (this.getCurrentOrderBy() === Column.ASC) {
            this.setCurrentOrderBy(Column.DESC);

        } else { // either DESC or undefined
            this.setCurrentOrderBy(Column.ASC);
        }
        return this.getCurrentOrderBy();
    };

    this.getColumnNameString = function () {
        if (this.options.columnName) {
            return "<strong>" + this.options.columnName + "</strong>";
        }
        else {
            return "";
        }
    };

    this.getColumnName = function () {
        if (this.options.columnName) {
            return this.options.columnName;
        }
        else {
            return "";
        }
    };

    this.getHeaderClass = function () {
        if (this.options.headerClass) {
            return this.options.headerClass;
        }
        else {
            return "";
        }
    };

    this.getCheckboxElementClass = function () {
        return this.options.rowElementClass || "";
    };

    this.getColumnWidth = function () {
        return this.options.width || "";
    };

    this.getType = function () {
        return this.options.columnType;
    };

    this.setTdId = function (id) {
        this.options.id = id;
    };

    this.getTdId = function () {
        return this.options.id;
    };

    this.getFilter = function () {
        return this.options.filter;
    };

    this.extractData = function (value) {
        if (this.options.extractDataFunction) {
            return this.options.extractDataFunction(value);

        }
        else {
            return value;
        }
    };

    this.extractCellValue = function (tdElement, cellId) {
        return this.options.extractCellValue(tdElement, cellId);
    };

    this.clearCellValue = function (tdElement, cellId) {

        if (this.options.clearCellValueFunction) {
            this.options.clearCellValueFunction(tdElement, cellId);
        }
    };

    this.generateTableHeadElement = function (tableRow, onSort) {
        var tdElement = $("<td></td>", {id: this.getTdId()});
        if (this.getColumnWidth()) {
            tdElement.css({"width": this.getColumnWidth() + "%"});
        }
        tableRow.append(tdElement);

        this.options.columnType.renderHeader(tdElement, onSort);
    };

    this.generateTableElement = function (tdElement, val) {

        if (this.getColumnWidth()) {
            tdElement.css({"width": this.getColumnWidth() + "%"});
        }

        this.options.columnType.render(tdElement, val);
    };
}

/*
 * Creates a column that contains a link
 */
Column.Hyperlink = function (column) {
    this.basicColumnData = column;
    this.options = {};

    this.setOptions = function (options) {
        this.options = options;
    };

    this.createRowElementHyperLink = function (hyperlink) {
        if(hyperlink && hyperlink.indexOf("javascript:") == -1) {
            hyperlink = "javascript:" + hyperlink;
        }
        return hyperlink;
    };

    this.getRowElementHyperLink = function (val) {
        return this.basicColumnData.options.rowElementHyperLink ? this.basicColumnData.options.rowElementHyperLink(val) : "";
    };

    this.renderHeader = function (tdElement) {

        if (this.basicColumnData.getColumnNameString()) {
            tdElement.html(this.basicColumnData.getColumnNameString());
        }
    };

    this.render = function (tdElement, val) {
        var linkText = this.basicColumnData.extractData(val, tdElement);

        var column = this;
        if(this.basicColumnData.options.rowElementOnClick)
        {
            var hrefElement = $('<a></a>', {
                href: "#",
                text: linkText
            }).appendTo(tdElement);
            hrefElement.click(function(event){
                event.preventDefault();
                column.basicColumnData.options.rowElementOnClick(val, tdElement);
            });

            tdElement.addClass(this.basicColumnData.getCheckboxElementClass());
        }
        else {
            var hyperlink = this.getRowElementHyperLink(val);

            var hrefElement = $('<a></a>', {
                href: this.createRowElementHyperLink(hyperlink),
                text: linkText
            }).appendTo(tdElement);
            tdElement.addClass(this.basicColumnData.getCheckboxElementClass());
        }
    };
};

/*
 * Creates a column that can contain any text or html
 */
Column.Text = function (column) {
    this.basicColumnData = column;
    this.options = {};

    this.setOptions = function (options) {
        this.options = options;
    };

    this.renderHeader = function (tdElement, onSort) {
        var headerColumn = this;
        tdElement.addClass(this.basicColumnData.getSortingClass());
        tdElement.addClass(this.basicColumnData.getHeaderClass());
        tdElement.html(this.basicColumnData.getColumnNameString());
        tdElement.on("click", function () {
            onSort(headerColumn.basicColumnData);
        });
    };

    this.render = function (tdElement, val) {
        var value = this.basicColumnData.extractData(val);
        if(this.basicColumnData.options.rowElementClass)
        {
            tdElement.addClass(this.basicColumnData.options.rowElementClass);
        }

        tdElement.html(value);
    };
};

/*
 * Creates a column that contains a checkbox
 */
Column.Checkbox = function (column) {
    var self = this;
    this.basicColumnData = column === undefined ? new Column() : column;
    this.basicColumnData.options.extractCellValue = function (tdElement, tdElementId) {
        return parseInt(tdElement.find("." + self.getCheckboxElementClass()).val());
    };

    this.options = {};

    this.setOptions = function (options) {
        this.options = options;
    };

    this.renderHeader = function (tdElement) {
        var checkboxElement = $("<input/>", {
            id: this.getHeaderElementId(),
            type: "checkbox",
            class: "checkbox"
        });
        tdElement.append(checkboxElement);
        tdElement.css({"text-align" : "center"});

        var checkboxColumn = this;

        checkboxElement.on("change", function () {
            var checked = $(this).prop("checked");
            if (checked) {
                checkboxColumn.checkAll();
            }
            else {
                checkboxColumn.uncheckAll();
            }
        });
    };

    this.getHeaderElementId = function () {
        return this.basicColumnData.getTdId() + "-selectAll";
    };

    this.getCheckboxElementClass = function () {
        return this.basicColumnData.getTdId() + "-checkbox";
    };

    this.clickCallbackFactory = function (checkboxColumn) {

        return function () {
            var checked = $(this).prop("checked");
            checkboxColumn.updateHeaderCheckbox(checked);
        }
    };
    this.changeCallbackFactory = function (checkboxColumn, val) {

        return function () {
            var checked = $(this).prop("checked");
            var value = checkboxColumn.basicColumnData.extractData(val);

            checkboxColumn.options.onSelect.call(this, value, checked);

            var changeFn = checkboxColumn.basicColumnData.options.onChange;

            checkboxColumn.updateHeaderCheckbox(checked);

            changeFn && changeFn.call(this, checked);
        }
    };
    this.render = function (tdElement, val) {
        var id = this.basicColumnData.extractData(val);
        var inputElement = $("<input/>", {
            type: "checkbox",
            class: this.getCheckboxElementClass(),
            value: id,
            id: id
        });

        tdElement.append(inputElement);
        tdElement.addClass("centeredContent");

        inputElement.on("click", this.clickCallbackFactory(this));
        inputElement.on("change", this.changeCallbackFactory(this, val));
    };

    this.updateHeaderCheckbox = function () {
        var headerElementId = this.getHeaderElementId();

        var hasUnselectedCheckBoxes = false;

        $("." + this.getCheckboxElementClass()).each(function () {
            var checked = $(this).prop("checked");

            if (!checked) {
                hasUnselectedCheckBoxes = true;
            }
        });

        if (!hasUnselectedCheckBoxes) {
            $("#" + headerElementId).prop("checked", true);
        }
        else {
            $("#" + headerElementId).prop("checked", false);
        }
    };

    this.checkAll = function () {
        $("." + this.getCheckboxElementClass()).each(function () {
            if ($(this).parents("tr").first().is(":visible")) {

                $(this).prop("checked", true).change();
            }
        });
    };

    this.uncheckAll = function () {
        $("." + this.getCheckboxElementClass()).each(function () {
            if ($(this).parents("tr").first().is(":visible")) {

                $(this).prop("checked", false).change();
            }
        });
    };
};

/*
 * Creates a column with a plus/minus toggle that allows a row below to be
 * expanded and collapsed
 */
Column.Expandable = function (column) {
    this.basicColumnData = column;

    this.options = {};

    this.setOptions = function (options) {
        this.options = options;
    };

    this.renderHeader = function (tdElement) {
    };

    this.render = function (tdElement, val) {
        var expandColumn = this;

        var toggleImage = $("<img/>", {
            src: expandOrCollapseImgUrls.EXPAND,
            title: expandOrCollapseImgTitle.EXPAND
        }).appendTo(tdElement);

        var value = this.basicColumnData.extractData(val);
        toggleImage.click(function () {
            var src = $(this).attr("src");

            if (src === expandOrCollapseImgUrls.EXPAND) {
                $(this).attr("src", expandOrCollapseImgUrls.COLLAPSE);
                $(this).attr("title", expandOrCollapseImgTitle.COLLAPSE);
                expandColumn.options.onExpand(tdElement, value);
            }
            else {
                $(this).attr("src", expandOrCollapseImgUrls.EXPAND);
                $(this).attr("title", expandOrCollapseImgTitle.EXPAND);
                expandColumn.options.onCollapse(tdElement, value);
            }
        });
    }
};

Column.Custom = function (column) {
    this.basicColumnData = column;

    this.options = {};

    this.setOptions = function (options) {
        this.options = options;
    };

    this.renderHeader = function (tdElement) {
        tdElement.addClass(this.basicColumnData.getHeaderClass());
        tdElement.html(this.basicColumnData.getColumnNameString());
    };

    this.render = function (tdElement, val) {
        var cellId = $(tdElement).attr("id");
        this.basicColumnData.options.renderCell(tdElement, val, cellId);
    }
};

Column.ASC = 'ASC';
Column.DESC = 'DESC';

Column.sortable = "sortable";
Column.sorting_desc = "sorting_desc";
Column.sorting_asc = "sorting_asc";





