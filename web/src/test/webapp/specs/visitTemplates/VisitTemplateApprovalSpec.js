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

describe('Visit Template Approval tab', function() {

    xit('displays non table related visit template approval data', function() {

        FixtureHelper.loadSourceHtmlFixtureIntoDom('visit_detail.html');

        var server = UtilHelper.getFakeServer();

        sessionStorage.setItem("mode", "\"view\"");

        var selectedStudy = {
            id: 349,
            localId: 850
        };
        sessionStorage.setItem("studyData", JSON.stringify(selectedStudy));

        var selectedVisit = {
            id: 1,
            visitName: "Visit Foo",
            studyName: "Test Study",
            approved: "Not Approved"
        };
        sessionStorage.setItem("visitData", JSON.stringify(selectedVisit));

        var approvalHistoryUrl = "rest/study/getVisitApprovals?" +
            "visit=1" +
            "&page=1" +
            "&maxResults=50" +
            "&orderBy=" + Column.ASC +
            "&sortBy=" + VisitApprovalTable.dbColumnLastEdited;

        AjaxHelper.setupFakeServerTextResponseFromTestFixture(server, approvalHistoryUrl, "visitTemplates/json/visit-template-approval-history.json");

        sessionStorage.setItem("selectedTab", 0);

        commonVisitDetailData();

        server.respond();

        expect($("#study_visit_name2").text()).toBe("(" + selectedStudy.localId + ") " + selectedVisit.studyName);
        expect($("#visit_name2").text()).toBe(selectedVisit.visitName);
        expect($("#visitgrid_finalApprovalStatus").text()).toBe(selectedVisit.approved);
    });

    xit('has a visit template approval history table', function () {

        FixtureHelper.loadSourceHtmlFixtureIntoDom('visit_detail.html');

        var server = UtilHelper.getFakeServer();

        var approvalHistoryUrl = "rest/study/getVisitApprovals?" +
            "visit=1" +
            "&page=1" +
            "&maxResults=50" +
            "&orderBy=" + Column.ASC +
            "&sortBy=" + VisitApprovalTable.dbColumnLastEdited;

        AjaxHelper.setupFakeServerTextResponseFromTestFixture(server, approvalHistoryUrl, "visitTemplates/json/visit-template-approval-history.json");

        app_selectedVisit = {
            id: 1
        };

        loadVisitTemplateApprovalsTab();

        server.respond();

        var historyData = FixtureHelper.getTestJsonFixture("visitTemplates/json/visit-template-approval-history.json");
        // make sure our test data has the correct length
        expect(historyData.length).toBe(15);

        var table = $('#visitApprovalTable');
        expect(table.length).toBe(1);

        var headerRow = $('#visitApprovalTable-columnLabelRow');
        expect(headerRow.length).toBe(1);
        var headerCells = $(headerRow).children('td');
        expect($(headerCells).length).toBe(7);
        expect($(headerCells.get(0)).text()).toBe("Last Name");
        expect($(headerCells.get(1)).text()).toBe("First Name");
        expect($(headerCells.get(2)).text()).toBe("User Role");
        expect($(headerCells.get(3)).text()).toBe("User Title");
        expect($(headerCells.get(4)).text()).toBe("User Approval Status");
        expect($(headerCells.get(5)).text()).toBe("Last Edited");
        expect($(headerCells.get(6)).text()).toBe("Comment");

        var rows = $('.visitApprovalTable-tableRow');
        expect(rows.length).toBe(historyData.length);

        rows.each(function (rowIndex, row) {
            var rowData = historyData[rowIndex];
            var cells = $(row).children('td');
            expect($(cells).length).toBe(7);
            expect($(cells.get(0)).text()).toBe(rowData.userLastName);
            expect($(cells.get(1)).text()).toBe(rowData.userFirstName);
            expect($(cells.get(2)).text()).toBe(rowData.userRole);
            expect($(cells.get(3)).text()).toBe(rowData.userTitle);
            expect($(cells.get(4)).text()).toBe(rowData.approved);
            expect($(cells.get(5)).text()).toBe(dateFormat(rowData.statusChangeTime, 'mm/dd/yyyy HH:MM'));
            expect($(cells.get(6)).text()).toBe(rowData.comment ? rowData.comment : "");
        });

    });

    it('shows Comment/Approve button for front desk and crc staff', function () {

        FixtureHelper.loadSourceHtmlFixtureIntoDom('visit_detail.html');

        var server = UtilHelper.getFakeServer();

        sessionStorage.setItem("userData", JSON.stringify({
            institutionRole: {
                id:  SCHEDULER
            }
        }));

        var approvalHistoryUrl = "rest/study/getVisitApprovals?" +
            "visit=1" +
            "&page=1" +
            "&maxResults=50" +
            "&orderBy=" + Column.ASC +
            "&sortBy=" + VisitApprovalTable.dbColumnLastEdited;

        AjaxHelper.setupFakeServerTextResponseFromTestFixture(server, approvalHistoryUrl, "visitTemplates/json/visit-template-approval-history.json");

        app_selectedVisit = {
            id: 1
        };

        loadVisitTemplateApprovalsTab();

        server.respond();

        expect($("#study_newVisitApprovalButton").length).toBe(1);
    });

    it('does not show Comment/Approve button for study staff', function () {

        FixtureHelper.loadSourceHtmlFixtureIntoDom('visit_detail.html');

        var server = UtilHelper.getFakeServer();

        sessionStorage.setItem("userData", JSON.stringify({
            institutionRole: {
                id: STUDY_STAFF
            }
        }));

        var approvalHistoryUrl = "rest/study/getVisitApprovals?" +
            "visit=1" +
            "&page=1" +
            "&maxResults=50" +
            "&orderBy=" + Column.ASC +
            "&sortBy=" + VisitApprovalTable.dbColumnLastEdited;

        AjaxHelper.setupFakeServerTextResponseFromTestFixture(server, approvalHistoryUrl, "visitTemplates/json/visit-template-approval-history.json");

        app_selectedVisit = {
            id: 1
        };

        loadVisitTemplateApprovalsTab();

        server.respond();

        expect($("#study_newVisitApprovalButton").length).toBe(0);
    });

});
