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

var appt_search_view;

function loadBookedResourcesData(currentPage, eventId) {
    [
        "scheduledResourcesTable",
        "checkedInResourcesTable",
        "checkedOutResourcesTable",
        "cancelledResourcesTable",
        "batchResourcesTable"
    ].forEach( function(id) {
        // only create the table if the target DOM element exists
        if ($('#' + SchedulerTables.generateWrapperId(id)).length > 0) {
            BookedResourcesTable.createTable(
                id,
                className,
                eventId,
                appt_search_view,
                appt_list_view,
                user,
                appt_selectedVisit,
                appt_selectedVisitType,
                appt_selectedSubjectMrnId,
                start,
                end
            );
            BookedResourcesTable.currentTables[id].reloadFn();
            BookedResourcesTable.currentTables[id].setCurrentPage(currentPage);
        }
    });
}

function searchResourcesTablePostPopulateFn(data, className) {
    if (typeof className != 'undefined' && className == 'NotFound') {
        $('#searchResourcesData tbody tr').filter(function () {
            return $.trim($(this).find('td').eq(3).attr('title')) === "Not Available"
        }).show();
        $('#searchResourcesData tbody tr').filter(function () {
            return $.trim($(this).find('td').eq(3).attr('title')) === "Available"
        }).hide();
    }
    else if (typeof className != 'undefined' && className == 'Searching') {
        var subjectAvailable = true;
        if (data.length > 0) {
            subjectAvailable = data[data.length-1].subjectAvailable;
        }
        if (subjectAvailable == false) {
            subjectConfirmationDialog();
        }
    }
}

function subjectConfirmationDialog() {
    $("#subject-dialog-confirm").css({display: 'inline-block'});
    $("#subject-dialog-confirm").dialog({
        resizable: false,
        width: 510,
        modal: true,
        buttons: {
            "Yes": function () {
                $(this).dialog("close");
            },
            "No": function () {
                $(".ui-dialog-content").dialog("close");
                $(this).dialog("close");
            }
        }
    });
}

////////////////////////////////////////////Check for Overbooked Data....////////////////////////////////////////////////////////////////////////////////////

function loadOverbookedResourcesData(eventid) {
    OverbookTables.loadOverbookTablesDataIntoTablesWidget(eventid);
}


/**
 * Booked Resources tables. This implementation allows a distinct table widget for each of the
 * resources tables in the UI.
 *
 * @type {{}}
 */

BookedResourcesTable = {};

BookedResourcesTable.dbColumnName = "r.name";
BookedResourcesTable.dbColumnActivity = "lla.name";
BookedResourcesTable.dbColumnStartTime = "br.scheduled_start_time";
BookedResourcesTable.dbColumnEndTime = "br.scheduled_end_time";
BookedResourcesTable.defaultPerPage = 50;
BookedResourcesTable.restEndPointType = {};
BookedResourcesTable.restEndPointType.getSearchedSlotResources = "getSearchedSlotResources";
BookedResourcesTable.restEndPointType.getEventResources = "getEventResources";
BookedResourcesTable.restEndPointType.getBookedVisitResources = "getBookedVisitResources";

BookedResourcesTable.currentTables = {};

BookedResourcesTable.createTable = function (
    tableId,
    className,
    eventId,
    apptSearchView,
    apptListView,
    user,
    visitId,
    visitType,
    subjectMrnId,
    startDate,
    endDate,
    postPopulateFn
) {

    var columns = [];
    BookedResourcesTable.setDynamicConfig(className, apptSearchView, apptListView, user);
    BookedResourcesTable.pushAllColumns(columns, eventId);
    var populateFn = BookedResourcesTable.populateTableFnFactory(tableId, className, eventId, visitId, visitType, subjectMrnId, startDate, endDate, postPopulateFn);
    var args = {
        tableId: tableId,
        columns: columns,
        fixedHeader: true,
        maxHeight: "200px",
        reloadFn: populateFn
    };
    BookedResourcesTable.currentTables[tableId] = new SchedulerTables(args);
    BookedResourcesTable.currentTables[tableId].generateTable(
        [],
        BookedResourcesTable.defaultPerPage,
        BookedResourcesTable.dynamicConfig.hasPagination ? PaginationHelper.getPaginationId(tableId) : undefined,
        populateFn
    );

};

/**
 * BookedResourcesTable.dynamicConfig()
 *
 * encapsulates all aspects of the BookedResources table which cn vary given the context.
 * Can be used when the table is first created
 *
 * @returns {{}}
 */
BookedResourcesTable.setDynamicConfig = function(className, apptSearchView, apptListView, user) {

    // initially, set all the dynamic configuration properties to null;
    // If a column width is then set to something other than undefined/null/""/0 then the
    // corresponding column will be displayed
    var dConf = {
        nameColumnWidth: null,
        startTimeColumnWidth: null,
        endTimeColumnWidth: null,
        availabilityColumnWidth: null,
        messageColumnWidth: null,
        activitiesColumnWidth: null,
        editAndDeleteColumnWidth: null,
        restEndPointType: BookedResourcesTable.restEndPointType.getBookedVisitResources,
        hasSorting: null,
        hasPagination: null
    };

    if (apptSearchView == true) {
        if (className == 'NotFound') {
            if (user.institutionRole.id == SUPER_ADMIN || user.institutionRole.id == RESOURCE_MANAGER || user.institutionRole.id == SCHEDULER) {
                dConf.nameColumnWidth = "30";
                dConf.startTimeColumnWidth = "19";
                dConf.endTimeColumnWidth = "19";
                dConf.availabilityColumnWidth = "12";
                dConf.messageColumnWidth = "20";
            }
            else {
                dConf.nameColumnWidth = "38";
                dConf.startTimeColumnWidth = "25";
                dConf.endTimeColumnWidth = "25";
                dConf.availabilityColumnWidth = "12";
            }
            dConf.restEndPointType = BookedResourcesTable.restEndPointType.getSearchedSlotResources;
            dConf.hasSorting = false;
            dConf.hasPagination = false;
        }
        else {
            dConf.nameColumnWidth = "50";
            dConf.startTimeColumnWidth = "25";
            dConf.endTimeColumnWidth = "25";
            dConf.restEndPointType = BookedResourcesTable.restEndPointType.getEventResources;
            dConf.hasSorting = false;
            dConf.hasPagination = false;
        }
    }
    else {
        dConf.nameColumnWidth = "16";
        dConf.startTimeColumnWidth = "15";
        dConf.endTimeColumnWidth = "15";
        dConf.restEndPointType = BookedResourcesTable.restEndPointType.getBookedVisitResources
        dConf.hasSorting = false;
        dConf.hasPagination = true;
    }

    if (
        user.institutionRole.id != GENERAL_VIEW &&
        user.institutionRole.id != STUDY_STAFF &&
        user.institutionRole.id != FRONT_DESK &&
        !apptListView &&
        !apptSearchView
    ) {
        dConf.activitiesColumnWidth = 20;
        dConf.editAndDeleteColumnWidth = 15;
    }

    BookedResourcesTable.dynamicConfig = dConf;

};

BookedResourcesTable.pushAllColumns = function (columns, eventId) {

    var dConf = BookedResourcesTable.dynamicConfig;

    columns.push(new Column({
        columnName: "Resource Name",
        dbColumn: dConf.hasSorting ? BookedResourcesTable.dbColumnName : undefined,
        width: dConf.nameColumnWidth,
        extractDataFunction: function (val) {
            var resourceImage = '';
            if (val.resourceGroup == "fixed") {
                resourceImage = "<img src='css/images/icon_fixed.png' style='padding-right: 2px;'/>";
            }
            else if (val.resourceGroup == "float") {
                resourceImage = "<img src='css/images/icon_float.png' style='padding-right: 2px;'/>";
            }
            else if (val.resourceGroup == "float group") {
                resourceImage = "<img src='css/images/icon_floatgroup.png' style='padding-right: 2px;'/>";
            }
            else if (val.resourceGroup == "flex") {
                resourceImage = "<img src='css/images/icon_flex.png' style='padding-right: 2px;'/>";
            }
            return resourceImage + val.resourceName;
        }
    }));

    if (dConf.activitiesColumnWidth) {
        columns.push(new Column({
            columnName: "Resource Activities",
            dbColumn: dConf.hasSorting ? BookedResourcesTable.dbColumnActivity : undefined,
            width: dConf.activitiesColumnWidth,
            extractDataFunction: function (val) {
                return showValue(val.annotations);
            }
        }));
    }

    columns.push(new Column({
        columnName: "Start Time",
        dbColumn: dConf.hasSorting ? BookedResourcesTable.dbColumnStartTime : undefined,
        width: dConf.startTimeColumnWidth,
        extractDataFunction: function (val) {
            return showDateTime(val.scheduledStartTime);
        }
    }));

    columns.push(new Column({
        columnName: "End Time",
        dbColumn: dConf.hasSorting ? BookedResourcesTable.dbColumnEndTime : undefined,
        width: dConf.endTimeColumnWidth,
        extractDataFunction: function (val) {
            return showDateTime(val.scheduledEndTime);
        }
    }));

    if (dConf.availabilityColumnWidth) {
        columns.push(new Column({
            columnName: "Available",
            // is the following needed?
            // rowElementClass: 'resourceAvailabilityColumnData',
            width: dConf.availabilityColumnWidth,
            extractDataFunction: function (val) {
                if (showValue(val.available) == 'Yes') {
                    availabilityImage = 'css/images/icon_checkmark_hex4ab092.png';
                    availabilityText = 'Available';
                }
                else {
                    var availabilityImage = 'css/images/icon_x_hexb2382d.png';
                    var availabilityText = 'Not Available';
                }
                return "<img src='" + availabilityImage + "' alt='" + availabilityText + "' />";
            }
        }));
    }

    if (dConf.messageColumnWidth) {
        columns.push(new Column({
            columnName: "Reason for Unavailability",
            width: dConf.messageColumnWidth,
            extractDataFunction: function (val) {
                return showValue(val.rejectedResourceMessage);
            }
        }));
    }

    if (dConf.editAndDeleteColumnWidth) {
        columns.push(new Column({
            columnName: "",
            rowElementClass: 'editColumn',
            width: dConf.editAndDeleteColumnWidth,
            extractDataFunction: function (val) {
                var html = " <a href='javascript:editBookedResourceOverride(" + val.id + "," + eventId + ")'>Edit</a>";
                if (val.totalCount > 1) {
                    html += " | <a href='javascript:deleteBookedResourceOverride(" + val.id + ")'>Delete</a> ";
                }
                return html;
            }
        }));
    }

};

BookedResourcesTable.populateTable = function (tableId, className, eventId, visitId, visitType, subjectMrnId, startDate, endDate) {

    var table = BookedResourcesTable.currentTables[tableId];
    var dConf = BookedResourcesTable.dynamicConfig;

    var url;

    if (dConf.restEndPointType == BookedResourcesTable.restEndPointType.getSearchedSlotResources) {
        var isInpatient = MiscUtil.isInpatientVisit(visitType);

        // no sorting back-end nor front-end
        url = 'rest/appointment/getSearchedSlotResources?subjectMrnId=' + (subjectMrnId != null ? subjectMrnId : 0) +
                '&visit=' + visitId +
                '&startDate=' + Date.parse(startDate) +
                '&endDate=' + Date.parse(endDate) +
                '&isInpatient=' + isInpatient;
    }
    else if (dConf.restEndPointType == BookedResourcesTable.restEndPointType.getEventResources) {
        // accepts sorting params in back-end code but UI does not set them
        url = "rest/appointment/getEventResources?eventId=" + eventId;
    }
    else if (dConf.restEndPointType == BookedResourcesTable.restEndPointType.getBookedVisitResources) {
        // has sorting params but no UI controls. i.e. static value of sortBy and orderBy
        url = "rest/appointment/getBookedVisitResources?eventid=" + eventId +
            "&page=" +          table.getCurrentPage() +
            "&maxResults=" +    table.getRecordsPerPage() +
            "&orderBy=" +       (table.getSortOrder() || Column.ASC) +
            "&sortBy=" +        (table.getSortColumn() || BookedResourcesTable.dbColumnStartTime);
    }
    else {
        console.log("Error in BookedResourcesTable");
    }

    $.blockUI();
    $.getJSON(url, function (data) {
        BookedResourcesTable.currentTables[tableId].refreshTableBody(data);
        if (typeof postPopulateFn !== 'undefined') {
            postPopulateFn(data, className);
        }
        $.unblockUI();
    });

};

BookedResourcesTable.populateTableFnFactory = function (tableId, eventId, visitId, visitType, subjectMrnId, startDate, endDate, postPopulateFn) {

    return function () {

        return BookedResourcesTable.populateTable(tableId, eventId, visitId, visitType, subjectMrnId, startDate, endDate, postPopulateFn);

    };

};
