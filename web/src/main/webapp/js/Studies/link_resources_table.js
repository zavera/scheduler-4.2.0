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

var LinkResourcesTable = {};
var LRT = LinkResourcesTable;

LRT.uiColumnResourceName = "Resource Name";
LRT.uiColumnResourceType = "Resource Type";
LRT.uiColumnResourceStartTime = "Resource Start Time";
LRT.uiColumnResourceEndTime = "Resource End Time";

LRT.tableName = "resourceLinkingTable";

LRT.pushColumns = function(columns) {

    columns.push(new Column({
        columnType: Column.Checkbox,
        width:2,
        extractDataFunction: function (val) {return val.id;}
    }));

    columns.push(new Column({
        columnName: LRT.uiColumnResourceName,
        width:20,
        extractDataFunction: function (val) {return val.resourceName;}
    }));
    columns.push(new Column({
        columnName: LRT.uiColumnResourceType,
        width:10,
        extractDataFunction: function (val) {return val.resourceType;}
    }));
    columns.push(new Column({
        columnName: LRT.uiColumnResourceStartTime,
        width:10,
        extractDataFunction: function (val) {
            return showDaysHoursAndMinutes(val.resourceStartMinutes);
        }
    }));
    columns.push(new Column({
        columnName: LRT.uiColumnResourceEndTime,
        width:10,
        extractDataFunction: function (val) {
            return showDaysHoursAndMinutes(val.resourceEndMinutes);
        }
}));

};

LRT.loadVisitsIntoWidget = function(currentPage, initial) {

    initial = initial || true;

    var columns = [];
    LRT.pushColumns(columns);

    LRT.currentTable = new SchedulerTables({
        tableId: LRT.tableName,
        columns: columns,
        reloadFn: LRT.populateLinkingResourcesTable
    });

    LRT.currentTable.generateTable(
        [],
        50,
        null, null, // no pagination
        function() {
            LRT.currentTable.reloadFn(true);
        }
    );

    LRT.populateLinkingResourcesTable(initial, currentPage);
};

LRT.populateLinkingResourcesTable = function(initial, currentPage) {

    var whichTable = LRT.currentTable;

    if (currentPage) {
        whichTable.setCurrentPage(currentPage);
    }

    var restUrl;
    if (isLinkingFloat) {
        restUrl = "rest/appointment/getGanttInfo/candidateFloatables?visit=" + app_selectedVisit.id + "&templateResource=" + templateResourceId;
    }
    else {
        restUrl = "rest/appointment/getGanttInfo/candidateFlexibles?visit=" + app_selectedVisit.id + "&templateResourceId=" + templateResourceId;
    }

    $.getJSON(restUrl, function(data) {
        whichTable.refreshTableBody(data.acceptableGroupies);

    }).always(function() {
        $('#table_loading').css({display: 'none'});
    });
};

//The below 'commented' sourceURL specifies a name for this piece of code which
//  will appear if debugging in the browser
//This is needed because this file is loaded programatically
//# sourceURL=link_resources_table.js
