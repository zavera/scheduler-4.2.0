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

//The below 'commented' sourceURL specifies a name for this piece of code which
//  will appear if debugging in the browser
//This is needed because this file is loaded programmatically
//# sourceURL=study_tables.js

var VisitTables = {};

VisitTables.dbColumnVisitName = "v.name";
VisitTables.dbColumnVisitType = "v.visitType.name";
VisitTables.dbColumnSublocation = "v.sublocation.name";
VisitTables.dbColumnApproved = "v.approved";

VisitTables.uiColumnVisitName = "Name";
VisitTables.uiColumnVisitType = "Visit Type";
VisitTables.uiColumnSublocation = "Sub-location";
VisitTables.uiColumnApproved = "Final Status";

VisitTables.activeVisitTemplates = "activeVisitTemplatesTable";
VisitTables.inactiveVisitTemplates = "inactiveVisitTemplatesTable";

VisitTables.activeOrInactiveTableElementId = {
    true: VisitTables.activeVisitTemplates,
    false: VisitTables.inactiveVisitTemplates
};

VisitTables.activeOrInactivePaginationId = {
    true: VisitTables.activeVisitTemplates + PaginationHelper.suffix,
    false: VisitTables.inactiveVisitTemplates + PaginationHelper.suffix
};

VisitTables.isActiveView;
VisitTables.defaultActive = true;

VisitTables.pushSortableColumns = function (columns) {
    var extraWidth = 0;
    if (UserRoleUtil.isGeneralFrontOrStaff(user)) {

        extraWidth = 8;
    }

    columns.push(new Column({
        dbColumn: VisitTables.dbColumnVisitName,
        columnName: VisitTables.uiColumnVisitName,
        columnType: Column.Text,
        width: 24 + extraWidth,
        defaultSortOrder: Column.ASC,
        extractDataFunction: function (val) {
            return val.visitName;
        },
        filter: {
            name: VisitTables.dbColumnVisitName,
            onFilter: VisitTables.populateVisitTemplatesTable
        }
    }));

    columns.push(new Column({
        dbColumn: VisitTables.dbColumnVisitType,
        columnName: VisitTables.uiColumnVisitType,
        width: 15,
        extractDataFunction: function (val) {
            return val.visitTypeName;
        },
        filter: {
            name: VisitTables.dbColumnVisitType,
            onFilter: VisitTables.populateVisitTemplatesTable
        }
    }));

    columns.push(new Column({
        dbColumn: VisitTables.dbColumnSublocation,
        columnName: VisitTables.uiColumnSublocation,
        width: 20 + extraWidth,
        extractDataFunction: function (val) {
            return val.sublocationName;
        },
        filter: {
            name: VisitTables.dbColumnSublocation,
            onFilter: VisitTables.populateVisitTemplatesTable
        }
    }));

    columns.push(new Column({
        dbColumn: VisitTables.dbColumnApproved,
        columnName: VisitTables.uiColumnApproved,
        width: 11,
        extractDataFunction: function (val) {
            return val.finalApprovalStatus;
        }
    }));
};

VisitTables.pushEditColumns = function (user, columns) {
    var viewLinkColumn = new Column({
        columnType: Column.Hyperlink,
        rowElementClass: 'editColumn',
        width: 9,
        rowElementHyperLink: function (val) {
            return 'showVisitDetail(' + val.id + ')';
        },
        extractDataFunction: function () {
            return " View Detail ";
        }
    });
    var copyLinkColumn = new Column({
        columnType: Column.Hyperlink,
        rowElementClass: 'editColumn',
        width: 8,
        rowElementHyperLink: function (val) {
            return 'copyVisitTemplate(' + val.id + ')';
        },
        extractDataFunction: function () {
            return " Copy Visit ";
        }
    });
    var deleteLinkColumn = new Column({
        columnType: Column.Hyperlink,
        rowElementClass: 'editColumn',
        width: 9,
        rowElementHyperLink: function (val) {
            return 'deleteVisitTemplate(' + val.id + ')';
        },
        extractDataFunction: function () {
            return " Delete Visit ";
        }
    });

    if (UserRoleUtil.isGeneralFrontOrStaff(user)) {
        columns.push(viewLinkColumn);
    }
    else if (UserRoleUtil.isAdminUser(user)) {
        columns.push(viewLinkColumn, copyLinkColumn, deleteLinkColumn);
    } else {
        columns.push(viewLinkColumn, copyLinkColumn);
    }
};

VisitTables.pushCheckboxColumn = function (columns) {
    columns.push(new Column({
        columnType: Column.Checkbox,
        onChange: VisitTables.toggleActivationButton,
        width: 5,
        extractDataFunction: function (val) {
            return val.id;
        }
    }));
};

VisitTables.toggleActivationButton = function () {
    var selectedRows = VisitTables.currentTable.getSelectedRows();
    var hide = selectedRows.length === 0;

    if (hide) {
        $('.DeactivateliveLink').css({display: "none"});
        $('.ActivateliveLink').css({display: "none"});
    } else {
        if (user.institutionRole.id == SUPER_ADMIN || user.institutionRole.id == RESOURCE_MANAGER) {
            $('.DeactivateliveLink').css({display: "inline-block"});
            $('.ActivateliveLink').css({display: "inline-block"});
        }
        else {
            $('.DeactivateliveLink').css({display: "none"});
            $('.ActivateliveLink').css({display: "none"});
        }
    }
};

function loadVisitTemplatesDataIntoTablesWidget(activeView) {
    $('#study_localId1').html('(' + app_selectedStudy.localId + ')');
    $('#study_name1').html(app_selectedStudy.name);

    $('.DeactivateliveLink').css({display: "none"});
    $('.ActivateliveLink').css({display: "none"});
    if (UserRoleUtil.isStaff(user)) {
        $('#study_newStudyVisitButton').css({display: "none"});
    }

    VisitTables.isActiveView = activeView;

    var columns = [];

    if(UserRoleUtil.isResourceManagerOrSuperAdmin(user)) {
        VisitTables.pushCheckboxColumn(columns);
    }

    VisitTables.pushSortableColumns(columns);
    VisitTables.pushEditColumns(user, columns);

    VisitTables.currentTable = new SchedulerTables({
        tableId: VisitTables.activeOrInactiveTableElementId[activeView],
        columns: columns,
        reloadFn: VisitTables.populateVisitTemplatesTable,
        doubleClickRowIdCallback: showVisitDetail
    });

    VisitTables.currentTable.generateTable(
        [],
        PaginationHelper.defaultPerPage,
        VisitTables.activeOrInactivePaginationId[activeView],
        VisitTables.populateVisitTemplatesTable);

    VisitTables.populateVisitTemplatesTable();
}

VisitTables.populateVisitTemplatesTable = function () {

    var whichTable = VisitTables.currentTable;
    var activeView = VisitTables.isActiveView;

    var keyValuePairs = {searchItems: whichTable.getAllFilterKeyValuePairs()};
    var stringifiedPairs = JSON.stringify(keyValuePairs);

    var restUrl = "rest/appointment/getVisitsByStudy?study=" + app_selectedStudy.id +
        "&active=" + activeView +
        "&page=" + whichTable.getCurrentPage() +
        "&maxResults=" + whichTable.getRecordsPerPage() +
        "&orderBy=" + (whichTable.getSortOrder() || Column.ASC) +
        "&sortBy=" + (whichTable.getSortColumn() || VisitTables.dbColumnVisitName) +
        "&search=" + encodeURIComponent(stringifiedPairs);

    $.getJSON(restUrl, function (data) {
        whichTable.refreshTableBody(data);
    });
};

VisitTables.buildDisplayFilterSelectOptions = function (options) {
    var htmlString = "";

    for (var i = 0; i < options.length; i++) {
        var option = options[i];

        htmlString += '' +
            '<option value="' + option.value + '">' +
            option.text +
            '</option>';
    }

    return htmlString;
};

VisitTables.buildDisplayFilterSelect = function (tableName, options) {

    var htmlString = VisitTables.buildDisplayFilterSelectOptions(options);

    $('.filterSelectedResourcesSelect').html(htmlString);

    WidgetUtil.createComboBox('.filterSelectedResourcesSelect', {
        width: 216,
        height: comboboxHeight,
        onSelect: function (record) {
            filterSelectedResources(tableName, record.value)
        },
        value: 'All'
    });
};

//////////////////////////////////////////////////////////////////
var StudiesTable = {};
StudiesTable.id = "studiesTable";
StudiesTable.dbColumnStudyName = "s.name";
StudiesTable.dbColumnPILastName = "pi.last_name";
StudiesTable.dbColumnPIFirstName = "pi.first_name";
StudiesTable.dbColumnIRBNumber = "s.irb";
StudiesTable.dbColumnLocalIdName = "s.local_id";
StudiesTable.dbColumnStudyStatus = "ss.short_name";
StudiesTable.defaultPerPage = 50;
StudiesTable.paginationId = StudiesTable.id + PaginationHelper.suffix;

StudiesTable.createStudiesTable = function () {
    var columns = [];

    StudiesTable.pushSortableColumns(columns);
    StudiesTable.pushEditColumns(columns);

    var args = {
        tableId: StudiesTable.id,
        columns: columns,
        reloadFn: function () {
            StudiesTable.populateStudiesTable();
        }
    };

    StudiesTable.currentTable = new SchedulerTables(args);

    StudiesTable.currentTable.generateTable(
        [],
        StudiesTable.defaultPerPage,
        StudiesTable.paginationId,
        function () {
            StudiesTable.populateStudiesTable.call(this);
        });
};

StudiesTable.pushSortableColumns = function (columns) {
    columns.push(new Column({
        dbColumn: StudiesTable.dbColumnStudyName,
        columnName: "Name",
        width: 28,
        extractDataFunction: function (val) {
            return val.name;
        },
        defaultSortOrder: Column.ASC,
        filter: {
            name: StudiesTable.dbColumnStudyName,
            onFilter: function (value) {
                StudiesTable.populateStudiesTable(1);
            }
        }
    }));

    columns.push(new Column({
        dbColumn: StudiesTable.dbColumnPILastName,
        columnName: "PI Last Name",
        width: 15,
        extractDataFunction: function (val) {
            return val.investigator.lastName;
        },
        filter: {
            name: StudiesTable.dbColumnPILastName,
            onFilter: function (value) {
                StudiesTable.populateStudiesTable(1);
            }
        }
    }));

    columns.push(new Column({
        dbColumn: StudiesTable.dbColumnPIFirstName,
        columnName: "PI First Name",
        width: 15,
        extractDataFunction: function (val) {
            return val.investigator.firstName;
        },
        filter: {
            name: StudiesTable.dbColumnPIFirstName,
            onFilter: function (value) {
                StudiesTable.populateStudiesTable(1);
            }
        }
    }));

    columns.push(new Column({
        dbColumn: StudiesTable.dbColumnIRBNumber,
        columnName: "IRB Number",
        width: 12,
        extractDataFunction: function (val) {
            return val.irbNumber;
        },
        filter: {
            name: StudiesTable.dbColumnIRBNumber,
            onFilter: function (value) {
                StudiesTable.populateStudiesTable(1);
            }
        }
    }));

    columns.push(new Column({
        dbColumn: StudiesTable.dbColumnLocalIdName,
        columnName: "Local ID",
        width: 11,
        extractDataFunction: function (val) {
            return val.localId;
        },
        filter: {
            name: StudiesTable.dbColumnLocalIdName,
            onFilter: function (value) {
                StudiesTable.populateStudiesTable(1);
            }
        }
    }));

    columns.push(new Column({
        dbColumn: StudiesTable.dbColumnStudyStatus,
        columnName: "Study Status",
        width: 11,
        extractDataFunction: function (val) {
            return val.studyStatus;
        }
    }));
};

StudiesTable.pushEditColumns = function (columns) {
    if (!UserRoleUtil.isGeneralView(user)) {
        var viewLinkColumn = new Column({
            columnType: Column.Hyperlink,
            width: 8,
            rowElementHyperLink: function (val) {
                return 'showStudyDetail(' + val.id + ')';
            },
            extractDataFunction: function () {
                return " View Detail ";
            }
        });

        columns.push(viewLinkColumn);
    }
};

StudiesTable.populateStudiesTable = function (currentPage) {
    var searchItemsList = {searchItems: StudiesTable.currentTable.getAllFilterKeyValuePairs()};

    var searchQuery = JSON.stringify(searchItemsList);
    $.getJSON("rest/study/getStudyList?page=" + (currentPage || StudiesTable.currentTable.getCurrentPage()) +
        "&maxResults=" + StudiesTable.currentTable.getRecordsPerPage() +
        "&orderBy=" + (StudiesTable.currentTable.getSortOrder() || Column.ASC) +
        "&sortBy=" + (StudiesTable.currentTable.getSortColumn() || StudiesTable.dbColumnStudyName) +
        "&search=" + encodeURIComponent(searchQuery), function (data) {
        StudiesTable.currentTable.refreshTableBody(data);
    });
};

//////////////////////////////////////////////////////////////////
var StudySubjectsTable = {};
StudySubjectsTable.id = "studySubjectsTable";
StudySubjectsTable.subjectMRN = "mrn";
StudySubjectsTable.subjectLastName = "lastName";
StudySubjectsTable.subjectFirstName = "firstName";
StudySubjectsTable.subjectDOB = "birthdate";
StudySubjectsTable.subjectCity = "city";
StudySubjectsTable.subjectState = "state";
StudySubjectsTable.subjectContact = "primaryContactNumber";
StudySubjectsTable.defaultPerPage = 50;
StudySubjectsTable.paginationId = PaginationHelper.getPaginationId(StudySubjectsTable.id);

StudySubjectsTable.pushSortableColumns = function (columns) {
    columns.push(new Column({
        dbColumn: StudySubjectsTable.subjectMRN,
        columnName: "MRN",
        defaultSortOrder: Column.ASC,
        width: 10,
        extractDataFunction: function (val) {
            return val.subjectMRN;
        },
        filter: {
            name: StudySubjectsTable.subjectMRN,
            onFilter: function (value) {
                StudySubjectsTable.resetToPageOne();
            }
        }
    }));

    columns.push(new Column({
        dbColumn: StudySubjectsTable.subjectLastName,
        columnName: "Last Name",
        width: 13,
        extractDataFunction: function (val) {
            return val.subjectLastName;
        },
        filter: {
            name: StudySubjectsTable.subjectLastName,
            onFilter: function (value) {
                StudySubjectsTable.resetToPageOne();
            }
        }
    }));

    columns.push(new Column({
        dbColumn: StudySubjectsTable.subjectFirstName,
        columnName: "First Name",
        width: 13,
        extractDataFunction: function (val) {
            return val.subjectFirstName;
        },
        filter: {
            name: StudySubjectsTable.subjectFirstName,
            onFilter: function (value) {
                StudySubjectsTable.resetToPageOne();
            }
        }
    }));

    columns.push(new Column({
        dbColumn: StudySubjectsTable.subjectDOB,
        columnName: "DOB",
        width: 10,
        extractDataFunction: function (val) {
            return showDate(val.subjectDOB);
        },
        filter: {
            name: StudySubjectsTable.subjectDOB,
            inputType: InputField.Type.Birthdate,
            onFilter: function (value) {
                StudySubjectsTable.resetToPageOne();
            }
        }
    }));

    columns.push(new Column({
        dbColumn: StudySubjectsTable.subjectCity,
        columnName: "City",
        width: 12,
        extractDataFunction: function (val) {
            return val.subjectCity;
        },
        filter: {
            name: StudySubjectsTable.subjectCity,
            onFilter: function (value) {
                StudySubjectsTable.resetToPageOne();
            }
        }
    }));

    columns.push(new Column({
        dbColumn: StudySubjectsTable.subjectState,
        columnName: "State",
        width: 12,
        extractDataFunction: function (val) {
            return val.subjectState;
        },
        filter: {
            name: StudySubjectsTable.subjectState,
            type: Filter.Type.EXACT,
            onFilter: function (value) {
                StudySubjectsTable.resetToPageOne();
            }
        }
    }));

    columns.push(new Column({
        dbColumn: StudySubjectsTable.subjectContact,
        columnName: "Primary Contact Number",
        filterPrompt: "Contact Number",
        width: 14,
        extractDataFunction: function (val) {
            return unifiedPhoneNumbersFormat(val.subjectPrimaryContact);
        },
        filter: {
            name: StudySubjectsTable.subjectContact,
            maskFormat: "(999) 999-9999",
            type: Filter.Type.EXACT,
            onFilter: function (value) {
                StudySubjectsTable.resetToPageOne();
            }
        }
    }));
};

StudySubjectsTable.pushEditColumns = function (columns) {
    if (!UserRoleUtil.isGeneralView(user)) {
        var viewLinkColumn = new Column({
            columnType: Column.Hyperlink,
            columnName: "Study Subjects status",
            width: 11,
            rowElementHyperLink: function (val) {
                return 'changeStudySubjectStatus(' + val.id + ')';
            },
            extractDataFunction: function (val) {
                return booleanToStatusValue(val.subjectStatus);
            }
        });
        columns.push(viewLinkColumn);

        viewLinkColumn = new Column({
            columnType: Column.Hyperlink,
            width: 9,
            rowElementHyperLink: function (val) {
                return 'goToStudySubjectForm(\"' + val.subjectId + '\",\"' + val.subjectMRN + '\",\"' + val.subjectMRNInstitution + '\")';
            },
            extractDataFunction: function () {
                return " View Details ";
            }
        });

        columns.push(viewLinkColumn);
    }
};

StudySubjectsTable.createStudySubjectsTable = function () {
    var columns = [];

    StudySubjectsTable.pushSortableColumns(columns);
    StudySubjectsTable.pushEditColumns(columns);

    var args = {
        tableId: StudySubjectsTable.id,
        columns: columns,
        reloadFn: StudySubjectsTable.populateStudySubjectsTable
    };

    StudySubjectsTable.currentTable = new SchedulerTables(args);

    StudySubjectsTable.currentTable.generateTable(
        [],
        StudySubjectsTable.defaultPerPage,
        StudySubjectsTable.paginationId,
        StudySubjectsTable.populateStudySubjectsTable);

};

StudySubjectsTable.resetToPageOne = function () {

    StudySubjectsTable.currentTable.setCurrentPage(1);
    StudySubjectsTable.populateStudySubjectsTable();
};

StudySubjectsTable.populateStudySubjectsTable = function () {
    var searchItemsList = {searchItems: StudySubjectsTable.currentTable.getAllFilterKeyValuePairs()};

    var searchQuery = JSON.stringify(searchItemsList);

    $.getJSON("rest/study/getStudySubjects?study=" + app_selectedStudy.id +
        "&inactive=true" +
        "&page=" + StudySubjectsTable.currentTable.getCurrentPage() +
        "&maxResults=" + StudySubjectsTable.currentTable.getRecordsPerPage() +
        "&orderBy=" + (StudySubjectsTable.currentTable.getSortOrder() || Column.ASC) +
        "&sortBy=" + (StudySubjectsTable.currentTable.getSortColumn() || StudySubjectsTable.subjectMRN) +
        "&search=" + encodeURIComponent(searchQuery), function (data) {

        StudySubjectsTable.currentTable.refreshTableBody(data.studySubject1s, data.totalCount);
    });
};

//////////////////////////////////
ResourceAnnotationsTable = {};
ResourceAnnotationsTable.id = "resourceAnnotationsTable";
ResourceAnnotationsTable.defaultPerPage = 50;
ResourceAnnotationsTable.minQuantity = 0;
ResourceAnnotationsTable.maxQuantity = 999;
ResourceAnnotationsTable.commentPlaceHolderText = "Click to edit";
ResourceAnnotationsTable.filterOptions = [
    {value: 'No', text: 'Display Unselected Annotations'},
    {value: 'Yes', text: 'Display Selected Annotations'},
    {value: 'All', text: 'Display Both'}
];
ResourceAnnotationsTable.modes = {
    resource: "resource",
    bookedResource: "bookedResource"
};

ResourceAnnotationsTable.pushNonEditableColumns = function (columns) {
    columns.push(new Column({
        columnType: Column.Checkbox,
        width: 5,
        extractDataFunction: function (val) {
            return val.id;
        }
    }));
    columns.push(new Column({
        columnName: "Resource Activity",
        width: 30,
        extractDataFunction: function (val) {
            return val.name;
        }
    }));
};

ResourceAnnotationsTable.pushEditableColumns = function (columns) {
    columns.push(new Column({
        columnName: "Quantity",
        width: 9,
        columnType: Column.Custom,
        clearCellValueFunction: function (tdElement, tdElementId) {
            var spinner = $("#" + tdElementId + "-spinner");

            if (spinner.length !== 0) {
                spinner.spinner("value", 1);
            }
        },
        extractDataFunction: function (val) {
            return val.quantity;
        },
        renderCell: function (tdElement, val, tdElementId) {
            var defaultQuantity = val.quantity;

            if (defaultQuantity == 0) {
                defaultQuantity = 1;
            }

            if (val.quantifiable) {
                var quantityField = $("<input/>", {
                    id: tdElementId + "-spinner",
                    class: "numberSpinner"
                });

                tdElement.append(quantityField);

                WidgetUtil.createNumberSpinner(quantityField, {
                    min: 1,
                    max: 999,
                    defaultValue: defaultQuantity,
                    change: function (event, ui) {
                        var value = $(event.target).val();
                        if (value === "") {
                            $(event.target).spinner("value", 1);
                        }
                    }
                });
            }
            else {
                tdElement.text(defaultQuantity);
            }

            var selectRowOnFocus = function (event) {
                preventDefaultAction(event);

                var rowIndex = ResourceAnnotationsTable.currentTable.getRowIndex($(this));
                ResourceAnnotationsTable.currentTable.selectRow(rowIndex);
            };
            tdElement.on("click", function (event) {
                selectRowOnFocus.call(this, event);
            });
            tdElement.on("keydown", function (event) {
                selectRowOnFocus.call(this, event);
            });
        },
        extractCellValue: function (tdElement, tdElementId) {
            var value;
            var spinner = $("#" + tdElementId + "-spinner");

            if (spinner.length !== 0) {
                value = spinner.spinner("value");
            }
            else {
                value = tdElement.text();
            }
            return value;
        }
    }));

    columns.push(new Column({
        columnName: "Comment",
        columnType: Column.Custom,
        width: 40,
        extractDataFunction: function (val) {
            return val.name;
        },
        clearCellValueFunction: function (tdElement, tdElementId) {

            $(tdElement).text(ResourceAnnotationsTable.commentPlaceHolderText);
            $(tdElement).addClass("commentFieldText");
        },
        renderCell: function (tdElement, val, tdElementId) {
            var comment = showValue(val.comment);


            if (comment.length === 0) {
                tdElement.text(ResourceAnnotationsTable.commentPlaceHolderText);
                $(tdElement).addClass("commentFieldText");
            }
            else {
                tdElement.text(comment);
            }

            tdElement.click(function (event) {
                var commentText = $(this).text();

                tdElement.empty();
                $(tdElement).removeClass("commentFieldText");

                var maxLength = 255;
                var commentField = $('<input class="commentField" type="text" maxlength="' + maxLength + '"/>');
                commentField.attr("id", tdElementId + "-comment");

                if (commentText !== ResourceAnnotationsTable.commentPlaceHolderText) {
                    commentField.val(commentText);
                }

                commentField.click(function (event) {
                    preventDefaultAction(event);
                });

                commentField.keyup(function (event) {
                    preventDefaultAction(event);

                    if (event.keyCode == 13) {
                        $(this).trigger("focusout");
                    }
                });

                commentField.focusout(function () {

                    var newComment = $(this).val();
                    if (newComment.length === 0) {
                        newComment = ResourceAnnotationsTable.commentPlaceHolderText;
                        $(tdElement).addClass("commentFieldText");
                    }

                    tdElement.empty();
                    tdElement.text(newComment);
                });

                tdElement.append(commentField);
                commentField.focus();

                var rowIndex = ResourceAnnotationsTable.currentTable.getRowIndex($(this));
                ResourceAnnotationsTable.currentTable.selectRow(rowIndex);
            });
        },
        extractCellValue: function (tdElement, tdElementId) {
            var value = tdElement.text();
            return value === ResourceAnnotationsTable.commentPlaceHolderText ? "" : value;
        }
    }));
};

ResourceAnnotationsTable.createResourceAnnotationsTable = function (mode, resourceId) {

    var columns = [];

    // Note that resourceId could be the ID of a bookedResource record, or a resource record
    ResourceAnnotationsTable.resourceId = resourceId;
    ResourceAnnotationsTable.mode = mode; // should be "resource" or "bookedResource"

    // this is a shortcut. The checkbox class really should be obtained directly from the table widget,
    // as opposed to mimicking the way it sets the checkbox class
    ResourceAnnotationsTable.checkboxClass = ResourceAnnotationsTable.id + "_0-checkbox";
    ResourceAnnotationsTable.pushNonEditableColumns(columns);
    ResourceAnnotationsTable.pushEditableColumns(columns);

    var args = {
        tableId: ResourceAnnotationsTable.id,
        columns: columns,
        reloadFn: ResourceAnnotationsTable.populateTable,
        fixedHeader: true,
        maxHeight: "200px"
    };

    ResourceAnnotationsTable.currentTable = new SchedulerTables(args);

    ResourceAnnotationsTable.currentTable.generateTable(
        [],
        ResourceAnnotationsTable.defaultPerPage
    );
};

ResourceAnnotationsTable.populateTable = function (callback) {
    var url;
    if (ResourceAnnotationsTable.mode == ResourceAnnotationsTable.modes.bookedResource) {
        url = "rest/resource/getBookedResourceAnnotations?resourceId=" + ResourceAnnotationsTable.resourceId;
    }
    else if (ResourceAnnotationsTable.mode == ResourceAnnotationsTable.modes.resource) {
        url = "rest/resource/getResourceAnnotations?resourceId=" + ResourceAnnotationsTable.resourceId;
    }
    else {
        throw "invalid mode"
    }
    $("#allAnnotations").prop("checked", true);
    $.ajax({
        url: url,
        dataType: 'json',
        success: function (data) {
            var annotationsData;
            if (ResourceAnnotationsTable.mode == ResourceAnnotationsTable.modes.bookedResource) {
                annotationsData = data.annotationsStore;
            }
            else if (ResourceAnnotationsTable.mode == ResourceAnnotationsTable.modes.resource) {
                annotationsData = data.resourceAnnotationsValues;
            }
            else {
                throw "invalid mode"
            }
            // ResourceAnnotationsTable.createResourceAnnotationsTable(resourceId);
            ResourceAnnotationsTable.currentTable.refreshTableBody(annotationsData);
            ResourceAnnotationsTable.selectResourceAnnotationsByRow(annotationsData);
            if (callback) {
                callback(annotationsData);
            }
        }
    });
};

ResourceAnnotationsTable.selectResourceAnnotationsByRow = function (responseData) {
    $.each(responseData, function (index, val) {
        if (val.selected) {
            ResourceAnnotationsTable.currentTable.selectRow(index);
        }
    });
};

ResourceAnnotationsTable.loadFromResourceMenuSelection = function(mode, menuId) {

    var elem = $('#' + menuId);
    var resourceId = $('#' + menuId).val();
    ResourceAnnotationsTable.loadForResource(mode, resourceId);
};

ResourceAnnotationsTable.loadForResource = function(mode, resourceId) {

    $("#allAnnotations").prop("checked", true);
    ResourceAnnotationsTable.createResourceAnnotationsTable(mode, resourceId);
    ResourceAnnotationsTable.populateTable();
    $('.annotationsTable').css({display: "inline-block"});

};


// Visit template approval table

var VisitApprovalTable = {};

VisitApprovalTable.id = "visitApprovalTable";
VisitApprovalTable.dbColumnLastName = "u.lastName";
VisitApprovalTable.dbColumnFirstName = "u.firstName";
VisitApprovalTable.dbColumnUserTitle = "u.institutionRole.name";
VisitApprovalTable.dbColumnLastEdited = "tah.statusChangeTime";
VisitApprovalTable.dbColumnUserRole = "u.role.name";
VisitApprovalTable.defaultPerPage = 50;
VisitApprovalTable.paginationId = PaginationHelper.getPaginationId(VisitApprovalTable.id);

VisitApprovalTable.createVisitApprovalTable = function () {
    var columns = [];

    VisitApprovalTable.pushAllColumns(columns);

    var args = {
        tableId: VisitApprovalTable.id,
        columns: columns,
        reloadFn: function () {
            VisitApprovalTable.populateVisitApprovalTable();
        }
    };

    VisitApprovalTable.currentTable = new SchedulerTables(args);

    VisitApprovalTable.currentTable.generateTable(
        [],
        VisitApprovalTable.defaultPerPage,
        VisitApprovalTable.paginationId,
        function () {
            VisitApprovalTable.populateVisitApprovalTable.call(this);
        });
};

VisitApprovalTable.pushAllColumns = function (columns) {

    columns.push(new Column({
        dbColumn: VisitApprovalTable.dbColumnLastName,
        columnName: "Last Name",
        width: 14,
        extractDataFunction: function (val) {
            return val.userLastName;
        },
        defaultSortOrder: Column.ASC
    }));

    columns.push(new Column({
        dbColumn: VisitApprovalTable.dbColumnFirstName,
        columnName: "First Name",
        width: 14,
        extractDataFunction: function (val) {
            return val.userFirstName;
        }
    }));

    columns.push(new Column({
        dbColumn: VisitApprovalTable.dbColumnUserRole,
        columnName: "User Role",
        width: 14,
        extractDataFunction: function (val) {
            return val.userRole;
        }
    }));

    columns.push(new Column({
        dbColumn: VisitApprovalTable.dbColumnUserTitle,
        columnName: "User Title",
        width: 14,
        extractDataFunction: function (val) {
            return val.userTitle;
        }
    }));

    columns.push(new Column({
        columnName: "User Approval Status",
        width: 14,
        extractDataFunction: function (val) {
            return val.approved;
        }
    }));

    columns.push(new Column({
        dbColumn: VisitApprovalTable.dbColumnLastEdited,
        columnName: "Last Edited",
        width: 14,
        extractDataFunction: function (val) {
            return showDateTime(val.statusChangeTime);
        },
        defaultSortOrder: Column.ASC
    }));

    columns.push(new Column({
        columnName: "Comment",
        width: 14,
        extractDataFunction: function (val) {
            return val.comment;
        }
    }));


};

VisitApprovalTable.resetToPageOne = function () {

    VisitApprovalTable.currentTable.setCurrentPage(1);
    VisitApprovalTable.populateVisitApprovalTable();

};

VisitApprovalTable.populateVisitApprovalTable = function () {

    var url = "rest/study/getVisitApprovals?visit=" + app_selectedVisit.id +
        "&page=" + (VisitApprovalTable.currentTable.getCurrentPage()) +
        "&maxResults=" + VisitApprovalTable.currentTable.getRecordsPerPage() +
        "&orderBy=" + (VisitApprovalTable.currentTable.getSortOrder() || Column.ASC) +
        "&sortBy=" + (VisitApprovalTable.currentTable.getSortColumn() || VisitApprovalTable.dbColumnStudyName);

    $.getJSON(url, function (data) {
        VisitApprovalTable.currentTable.refreshTableBody(data);
    });

};


// Billable/Non-billable resources table

var ResourceBillingTable = {};

ResourceBillingTable.id = "billableResourcesTable";
ResourceBillingTable.dbColumnBillableResourceName = "tr.resource.name";
ResourceBillingTable.dbColumnBillableResourceType = "tr.resource.resourceType";
ResourceBillingTable.dbColumnBillableResourceStart = "tr.startMinutes";
ResourceBillingTable.dbColumnBillableResourceEnd = "tr.endMinutes";
ResourceBillingTable.filterOptions = [
    {value: 'No', text: 'Display Unselected Resources'},
    {value: 'Yes', text: 'Display Selected Resources'},
    {value: 'All', text: 'Display Both'}
];

ResourceBillingTable.createResourceBillingTable = function () {
    var columns = [];

    ResourceBillingTable.pushAllColumns(columns);

    var args = {
        tableId: ResourceBillingTable.id,
        columns: columns,
        reloadFn: function () {
            ResourceBillingTable.populateResourceBillingTable();
        },
        fixedHeader: true,
        maxHeight: "290px"
    };

    ResourceBillingTable.currentTable = new SchedulerTables(args);

    ResourceBillingTable.currentTable.generateTable(
        [],
        null,
        null,
        null,
        null
    );
};


ResourceBillingTable.pushAllColumns = function (columns) {

    columns.push(new Column({
        columnType: Column.Checkbox,
        onChange: ResourceBillingTable.toggleActivationButton,
        width: 8,
        extractDataFunction: function (val) {
            return val.id;
        }
    }));

    columns.push(new Column({
        dbColumn: ResourceBillingTable.dbColumnBillableResourceName,
        columnName: "Resource Name",
        width: 24,
        extractDataFunction: function (val) {
            return val.resource.name;
        }
    }));

    columns.push(new Column({
        dbColumn: ResourceBillingTable.dbColumnBillableResourceType,
        columnName: "Resource Type",
        width: 22,
        extractDataFunction: function (val) {
            return val.resource.resourceType;
        }
    }));

    columns.push(new Column({
        dbColumn: ResourceBillingTable.dbColumnBillableResourceStart,
        columnName: "Resource Start",
        width: 23,
        extractDataFunction: function (val) {
            return showDaysHoursAndMinutes(val.startMinutes);
        }
    }));

    columns.push(new Column({
        dbColumn: ResourceBillingTable.dbColumnBillableResourceEnd,
        columnName: "Resource End",
        width: 23,
        extractDataFunction: function (val) {
            return showDaysHoursAndMinutes(val.endMinutes);
        }
    }));

};


ResourceBillingTable.populateResourceBillingTable = function () {

    var url = "rest/appointment/getSelectableTemplateResources?visitId=" + app_selectedVisit.id +
        "&isBillable=" + isBillableViewSelected() +
        "&orderBy=" + (ResourceBillingTable.currentTable.getSortOrder() || Column.ASC) +
        "&sortBy=" + (ResourceBillingTable.currentTable.getSortColumn() || ResourceBillingTable.dbColumnBillableResourceName);
    $.getJSON(url, function (data) {
        ResourceBillingTable.currentTable.refreshTableBody(data);
        $.unblockUI();
    });

};


ResourceBillingTable.toggleActivationButton = function (source) {

    var selectedRows = ResourceBillingTable.currentTable.getSelectedRows();

    if (selectedRows.length > 0) {
        $('#select_template_resource_save').css({display: 'inline-block'});
        $('#select_template_resource_save_and_close').css({display: 'inline-block'});
    }
    else {
        $('#select_template_resource_save').css({display: 'none'});
        $('#select_template_resource_save_and_close').css({display: 'none'});
    }

};
