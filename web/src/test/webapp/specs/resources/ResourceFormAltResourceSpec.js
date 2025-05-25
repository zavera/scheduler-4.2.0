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

describe('global function clearAlternateResourceSelection', function() {

    it('clears the alternate resource multi-select drop-down', function() {

        FixtureHelper.loadTestHtmlFixtureIntoDom('resources/html/clear-alternate-resource-selection.html');

        var selectElement = $('#addAlternateResourcesData');
        selectElement.multipleSelect();
        expect(selectElement.multipleSelect('getSelects')).toEqual(['1']);
        selectElement.multipleSelect("checkAll");
        expect(selectElement.multipleSelect('getSelects')).toEqual(['1', '2', '3']);

        spyOn(AlternateResourcesTable, "clearAlternativesError");

        var result = AlternateResourcesTable.clearAlternateResourceSelection();

        expect(result).toBe(false);
        expect(AlternateResourcesTable.clearAlternativesError).toHaveBeenCalledTimes(1);
        expect(selectElement.multipleSelect('getSelects')).toEqual([]);

    });

    it("correctly displays the alternate resources table", function() {
        var server = UtilHelper.getFakeServer();

        SessionDataHelper.setUser(SUPER_ADMIN);

        // load the home page into the DOM
        FixtureHelper.loadSourceHtmlFixtureIntoDom('resource_form.html');

        var resource = {
            resourceId: 42,
            resourceType: "Room"
        };

        var pathToFixture = "resources/json/resource-detail.json";
        var resourceDetailString = FixtureHelper.getTestFixtureContent(pathToFixture);
        var selectedResource = JSON.parse(resourceDetailString);
        sessionStorage.setItem("resourceData", JSON.stringify(selectedResource));

        AjaxHelper.setUpFakeServerLoadSourceHtmlResponse(server, "header.html");
        AjaxHelper.setUpFakeServerLoadSourceHtmlResponse(server, "common_dialogs.html");
        AjaxHelper.setUpFakeServerLoadSourceHtmlResponse(server, "table_templates.html");
        AjaxHelper.setupFakeServerTextResponseFromTestFixture(server, "rest/app/getStaticLists", "global/json/static-lists.json");

        var pathToFixtureAlternates = "resources/json/alternate-resources-list.json";
        var resourceAlternatesString = FixtureHelper.getTestFixtureContent(pathToFixtureAlternates);
        var resourceAlternates = JSON.parse(resourceAlternatesString);
        resourceAlternates = JSON.parse(resourceAlternates.data);

        var url = "rest/resource/getResourceAlternates?resourceId=" + resource.resourceId +
            "&page=1&maxResults=10&orderBy=" + Column.ASC + "&sortBy=" + AlternateResourcesTable.dbResourceName;

        AjaxHelper.setupFakeServerTextResponseFromTestFixture(server, url, pathToFixtureAlternates);

        resourceFormPage.init();

        server.respond();
        server.respond();
        server.respond();

        //expand the alternate resources section
        $("#resourceTabHeading2").click();

        var rows = $('.alternateResourcesTable-tableRow');
        expect(rows.length).toBe(3);

        rows.each(function (rowIndex, row) {
            var rowData = resourceAlternates[rowIndex];
            var cells = $(row).children('td');
            expect($(cells).length).toBe(5);
            expect($(cells.get(0)).text()).toBe(rowData.resource);
            expect($(cells.get(1)).text()).toBe(rowData.resourceType);
            expect($(cells.get(2)).text()).toBe(rowData.sublocation);
            expect($(cells.get(3)).find('a').text()).toBe("Delete");
            expect($(cells.get(3)).find('a').attr("href"))
                .toBe("javascript:AlternateResourcesTable.deleteAlternateDialog(" +  rowData.id+ ")");
            expect($(cells.get(4)).find('a').text()).toBe("");
        });

        var footer = $("#alternateResourcesTable").find("tfoot");
        var addARRow = $(footer.children("tr").get(0));
        expect(addARRow.is(":visible")).toBe(true);

        var tFootCells = $(addARRow).children('td');
        expect(tFootCells.length).toBe(5);
        expect($.trim($(tFootCells.get(0)).text())).toBe("");
        expect($.trim($(tFootCells.get(1)).text())).toBe("");
        expect($.trim($(tFootCells.get(3)).find('a').text())).toBe("Add New");
        expect($.trim($(tFootCells.get(4)).find('a').text())).toBe("Cancel");

        //Now check that the edit and delete columns are not displayed for study staff
        SessionDataHelper.setUser(STUDY_STAFF);

        AjaxHelper.setupFakeServerTextResponseFromTestFixture(server, url, pathToFixtureAlternates);
        resourceFormPage.init();

        server.respond();
        server.respond();
        server.respond();

        //expand the alternate resources section
        $("#resourceTabHeading2").click();

        var rows = $('.alternateResourcesTable-tableRow');
        expect(rows.length).toBe(3);

        rows.each(function (rowIndex, row) {
            var rowData = resourceAlternates[rowIndex];
            var cells = $(row).children('td');
            expect($(cells).length).toBe(3);
            expect($(cells.get(0)).text()).toBe(rowData.resource);
            expect($(cells.get(1)).text()).toBe(rowData.resourceType);
            expect($(cells.get(2)).text()).toBe(rowData.sublocation);
        });
    });
});
