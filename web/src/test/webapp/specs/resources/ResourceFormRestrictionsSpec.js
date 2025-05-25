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
describe("Resource Restriction ", function(){
    it('Display restriction table - has no shared resource', function()
    {
        var server = UtilHelper.getFakeServer();

        SessionDataHelper.setUser(SUPER_ADMIN);

        // load the home page into the DOM
        FixtureHelper.loadSourceHtmlFixtureIntoDom('resource_form.html');

        var pathToFixture = 'resources/json/room-resources.json';
        var resource = {
            resourceId: 42,
            resourceType: "Room"
        };

        var resourceDetailString = FixtureHelper.getTestFixtureContent('resources/json/resource-detail.json');
        var selectedResource = JSON.parse(resourceDetailString);
        selectedResource.sharedResourceId = null;
        selectedResource.sharedResourceNotes= null;
        sessionStorage.setItem("resourceData", JSON.stringify(selectedResource));

        AjaxHelper.setUpFakeServerLoadSourceHtmlResponse(server, "header.html");
        AjaxHelper.setUpFakeServerLoadSourceHtmlResponse(server, "common_dialogs.html");
        AjaxHelper.setUpFakeServerLoadSourceHtmlResponse(server, "table_templates.html");
        AjaxHelper.setupFakeServerTextResponseFromTestFixture(server, "rest/app/getStaticLists", "global/json/static-lists.json");

        var url = "rest/resource/getResourcesAvailableForGenderBlockRestriction?resourceId=" + resource.resourceId;
        AjaxHelper.setupFakeServerTextResponseFromTestFixture(server, url, pathToFixture);

        resourceFormPage.init();

        server.respond();
        server.respond();
        server.respond();

        //expand the resource restriction section
        $("#resourceTabHeading4").click();

        expect($("#restrictionTypeSelect").combobox("getValue")).toBe("Gender Restriction");

        expect($("#sharedResourceSelect").combobox("getValue")).toBe("");
        var sharedResourceSelectionOptions = $("#sharedResourceSelect").combobox("getData");
        expect(sharedResourceSelectionOptions.length).toBe(5);
        expect(sharedResourceSelectionOptions[0].text).toBe("ROOM 1");
        expect(sharedResourceSelectionOptions[0].selected).toBe(false);
        expect(sharedResourceSelectionOptions[0].disabled).toBe(false);

        var notesInputCount = $("#restrictionNotesInput_txtAreaCount");
        expect(notesInputCount.is(":visible")).toBe(true);
        expect(notesInputCount.text()).toBe("255 characters remaining");

        var dataRow = $("#resourceRestrictionsTable tr:nth-child(2)");
        var addColumn = dataRow.find("td:nth-child(4)");
        expect(addColumn.find("a").text()).toBe("Add New");

        var cancelAddColumn = dataRow.find("td:nth-child(5)");
        expect(cancelAddColumn.find("a").text()).toBe("Cancel");
    });

    it('Display restriction table - has a shared resource', function()
    {
        var server = UtilHelper.getFakeServer();

        SessionDataHelper.setUser(SUPER_ADMIN);

        // load the home page into the DOM
        FixtureHelper.loadSourceHtmlFixtureIntoDom('resource_form.html');

        var resource = {
            resourceId: 42,
            resourceType: "Room",
            sharedResourceId: 300
        };

        var resourceDetailString = FixtureHelper.getTestFixtureContent('resources/json/resource-detail.json');
        var selectedResource = JSON.parse(resourceDetailString);
        sessionStorage.setItem("resourceData", JSON.stringify(selectedResource));

        AjaxHelper.setUpFakeServerLoadSourceHtmlResponse(server, "header.html");
        AjaxHelper.setUpFakeServerLoadSourceHtmlResponse(server, "common_dialogs.html");
        AjaxHelper.setUpFakeServerLoadSourceHtmlResponse(server, "table_templates.html");
        AjaxHelper.setupFakeServerTextResponseFromTestFixture(server, "rest/app/getStaticLists", "global/json/static-lists.json");

        var resourceDetailUrl = "rest/resource/getResourceDetail?resourceId=" + resource.sharedResourceId;
        var pathToRDFixture = 'resources/json/resource-detail.json';
        AjaxHelper.setupFakeServerTextResponseFromTestFixture(server, resourceDetailUrl, pathToRDFixture);

        resourceFormPage.init();

        server.respond();
        server.respond();
        server.respond();

        var notesInput = $("#restrictionNotesInput_txtArea");
        expect(notesInput.is(":visible")).toBe(false);

        var dataRow = $("#resourceRestrictionsTable tr:nth-child(2)");

        var restrictionColumn = $.trim(dataRow.find("td:nth-child(1)").text());
        expect(restrictionColumn).toBe("Gender Restriction");

        var associatedResource = $.trim(dataRow.find("td:nth-child(2)").text());
        expect(associatedResource).toBe("ROOM 2");

        var commentText = $.trim(dataRow.find("td:nth-child(3)").text());
        expect(commentText).toBe("I am a shared resource");

        var editColumn = dataRow.find("td:nth-child(4)");
        expect(editColumn.find("a").text()).toBe("Edit");

        var deleteColumn = dataRow.find("td:nth-child(5)");
        expect(deleteColumn.find("a").text()).toBe("Delete");
    });
});
