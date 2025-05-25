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
describe("Default Availability", function() {
    it("correctly displays the default availability table", function() {
        var server = UtilHelper.getFakeServer();

        SessionDataHelper.setUser(SUPER_ADMIN);

        // load the home page into the DOM
        FixtureHelper.loadSourceHtmlFixtureIntoDom('resource_form.html');

        var resource = {
            resourceId: 42,
            resourceType: "Room"
        };

        var pathToFixtureSched = "resources/json/resource-schedules.json";
        var resourceScheduleString = FixtureHelper.getTestFixtureContent(pathToFixtureSched);
        var resourceSchedule = JSON.parse(resourceScheduleString);

        var pathToFixture = "resources/json/resource-detail.json";
        var resourceDetailString = FixtureHelper.getTestFixtureContent(pathToFixture);
        var selectedResource = JSON.parse(resourceDetailString);
        sessionStorage.setItem("resourceData", JSON.stringify(selectedResource));

        AjaxHelper.setUpFakeServerLoadSourceHtmlResponse(server, "header.html");
        AjaxHelper.setUpFakeServerLoadSourceHtmlResponse(server, "common_dialogs.html");
        AjaxHelper.setUpFakeServerLoadSourceHtmlResponse(server, "table_templates.html");
        AjaxHelper.setupFakeServerTextResponseFromTestFixture(server, "rest/app/getStaticLists", "global/json/static-lists.json");

        var url = "rest/resource/getResourceSchedules?resourceId=" + resource.resourceId
                + "&page=1&maxResults=10&orderBy=ASC&sortBy=da.dayOfWeek";
        AjaxHelper.setupFakeServerTextResponseFromTestFixture(server, url, pathToFixtureSched);

        resourceFormPage.init();

        server.respond();
        server.respond();
        server.respond();

        //expand the default availability section
        $("#resourceTabHeading1").click();

        var rows = $('.defaultAvailabilityTable-tableRow');
        expect(rows.length).toBe(10);

        rows.each(function (rowIndex, row) {
            var rowData = resourceSchedule[rowIndex];
            var cells = $(row).children('td');
            expect($(cells).length).toBe(6);
            expect($(cells.get(0)).text()).toBe(util_day(rowData.dayOfWeek));
            expect($(cells.get(1)).text()).toBe(rowData.startTime);
            expect($(cells.get(2)).text()).toBe(rowData.endTime);
            expect($(cells.get(3)).text()).toBe(rowData.quantity.toString());
            expect($(cells.get(4)).find('a').text()).toBe("Edit");
            expect($(cells.get(5)).find('a').text()).toBe("Delete");
        });

        var footer = $("#defaultAvailabilityTable").find("tfoot");
        var addDARow = footer.find("#addDefaultAvailabilityRow");
        expect(addDARow.is(":visible")).toBe(true);

        var tFootCells = $(addDARow).children('td');
        expect(tFootCells.length).toBe(6);
        expect($(tFootCells.get(0)).find(".ms-parent").length).toBe(1);
        expect($(tFootCells.get(1)).find(".hasDatepicker").length).toBe(1);
        expect($(tFootCells.get(2)).find(".hasDatepicker").length).toBe(1);
        expect($(tFootCells.get(3)).find("#resource_da_quantityValidation").length).toBe(1);
        expect($.trim($(tFootCells.get(4)).find('a').text())).toBe("Add New");
        expect($.trim($(tFootCells.get(5)).find('a').text())).toBe("Cancel");

        //Now check that the edit and delete columns are not displayed for study staff
        SessionDataHelper.setUser(STUDY_STAFF);

        AjaxHelper.setupFakeServerTextResponseFromTestFixture(server, url, pathToFixtureSched);
        resourceFormPage.init();

        server.respond();
        server.respond();
        server.respond();

        //expand the default availability section
        $("#resourceTabHeading1").click();

        var rows = $('.defaultAvailabilityTable-tableRow');
        expect(rows.length).toBe(10);

        rows.each(function (rowIndex, row) {
            var rowData = resourceSchedule[rowIndex];
            var cells = $(row).children('td');
            expect($(cells).length).toBe(4);
            expect($(cells.get(0)).text()).toBe(util_day(rowData.dayOfWeek));
            expect($(cells.get(1)).text()).toBe(rowData.startTime);
            expect($(cells.get(2)).text()).toBe(rowData.endTime);
            expect($(cells.get(3)).text()).toBe(rowData.quantity.toString());
        });
    });
});