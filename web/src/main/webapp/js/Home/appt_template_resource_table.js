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

var ApptTemplateResourceTable = {};
ApptTemplateResourceTable.id = "apptTemplateResourceTable";
ApptTemplateResourceTable.defaultPerPage = 10;
ApptTemplateResourceTable.dbResourceName = "r.name";
ApptTemplateResourceTable.dbStartTime = "tr.startMinutes";
ApptTemplateResourceTable.dbEndTime = "tr.endMinutes";

ApptTemplateResourceTable.create = function() {
    var columns = [];

    ApptTemplateResourceTable.pushAllColumns(columns);

    var args = {
        tableId: ApptTemplateResourceTable.id,
        columns: columns,
        reloadFn: ApptTemplateResourceTable.populate
    };

    ApptTemplateResourceTable.currentTable = new SchedulerTables(args);

    ApptTemplateResourceTable.currentTable.generateTable(
        [],
        ApptTemplateResourceTable.defaultPerPage,
        PaginationHelper.getPaginationId(ApptTemplateResourceTable.id),
        ApptTemplateResourceTable.populate
    );
};

ApptTemplateResourceTable.pushAllColumns = function(columns) {

    columns.push(new Column({
        dbColumn: ApptTemplateResourceTable.dbResourceName,
        columnName: "Resource Name",
        width: 20,
        extractDataFunction: function (val) {
            return val.resourceName;
        }
    }));

    columns.push(new Column({
        dbColumn: ApptTemplateResourceTable.dbStartTime,
        columnName: "Start Time",
        width: 20,
        defaultSortOrder: Column.ASC,
        extractDataFunction: function (val) {
            val.startTime = resourceTimelineDateFromMinutes(val.startMinutes);
            var startTime = "";
            var month = dateFormat(val.startTime, 'm');
            var day = dateFormat(val.startTime, 'd');
            var time = dateFormat(val.startTime, 'HH:MM');
            var totalDays = Math.floor(day) + 29;
            if (month > 2) {
                startTime = '(Day: ' + totalDays + ') ' + time;
            }
            else {
                startTime = '(Day: ' + day + ') ' + time;
            }
            return startTime;
        }
    }));

    columns.push(new Column({
        dbColumn: ApptTemplateResourceTable.dbEndTime,
        columnName: "End Time",
        width: 20,
        extractDataFunction: function (val) {
            val.endTime = resourceTimelineDateFromMinutes(val.endMinutes);
            var endTime = "";
            var month = dateFormat(val.endTime, 'm');
            var day = dateFormat(val.endTime, 'd');
            var time = dateFormat(val.endTime, 'HH:MM');
            var totalDays = Math.floor(day) + 29;
            if (month > 2) {
                endTime = '(Day: ' + totalDays + ') ' + time;
            }
            else {
                endTime = '(Day: ' + day + ') ' + time;
            }

            return endTime;
        }
    }));
};

ApptTemplateResourceTable.populate = function() {
    var visitId = $("#apptSearchVisit").combobox("getValue");

    var url = "rest/appointment/getSearchVisitResource?visit=" + visitId +
        "&page=" + ApptTemplateResourceTable.currentTable.getCurrentPage() +
        "&maxResults=" + ApptTemplateResourceTable.currentTable.getRecordsPerPage() +
        "&orderBy=" + (ApptTemplateResourceTable.currentTable.getSortOrder() || Column.ASC) +
        "&sortBy=" + (ApptTemplateResourceTable.currentTable.getSortColumn() || ApptTemplateResourceTable.dbStartTime);

    $.getJSON(url, function (data) {
        ApptTemplateResourceTable.currentTable.refreshTableBody(data.templateResource1s, data.totalCount);
    });

};
