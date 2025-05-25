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
describe('Resources landing page', function() {

    it("makes UI elements visible according to user role", function () {

        FixtureHelper.loadTestHtmlFixtureIntoDom('resources/html/elements-for-visibility-testing.html');

        // The following drives the tests. It contains the list of all expected states of the UI
        // Warning! the role ids are simple number in the following file. They need to match the
        // the various roles in global2.js
        var roles = FixtureHelper.getTestJsonFixture("resources/json/resources-acl.json");

        roles.forEach(function (role) {
            var userData = {
                institutionRole: {
                    id: role.id
                }
            };
            sessionStorage.setItem("userData", JSON.stringify(userData));

            // payload
            resourceModuleRoles();

            // verify
            role.specs.forEach(function (spec) {
                var matchedElements = $(".resource_" + spec.name);
                expect(matchedElements.length).toEqual(1);
                if (spec.value) {
                    // two equivalent ways to write the expectation that the element is visible:
                    expect(matchedElements.css("visibility")).toBe("visible");
                }
                else {
                    // two equivalent ways to write the expectation that the element is hidden:
                    expect(matchedElements.css("visibility")).toBe("hidden");
                }
            });

        });

    });

    it('shows the correct list of resources given the number of records specified in the UI', function() {

        var server = UtilHelper.getFakeServer();

        SessionDataHelper.setUser(SUPER_ADMIN);

        // load the home page into the DOM
        FixtureHelper.loadSourceHtmlFixtureIntoDom('resource_screen.html');

        // mock the server responding to the AJAX calls
        var url = "rest/resource/getResourcesData?page=1&maxResults=100&orderBy=ASC&sortBy=r.name&status=active&search=%7B%22searchItems%22%3A%5B%5D%7D";
        var responseString = FixtureHelper.getTestFixtureContent('resources/json/resource-list.json');
        var resourcesData = JSON.parse(responseString);
        AjaxHelper.setupFakeServerTextResponse(server, url, responseString);

        // method under test: call the function which populates the 'resource_screen.html' page
        resourcePage.init();

        // this is where things that were asynchronous came to be synchronous
        // i.e. let the mock server respond to pending AJAX calls, and execute the callbacks
        server.respond();

        // expect the initial number of records is 100
        expect($('#resourceTableControls_recordsPerPageSelector').val()).toEqual('100');
        // expect the number of rows in the table is as expected
        var rows = $(".resourceTable-tableRow");
        // expect the raw data and the table to match, in the same order
        expect(rows.length).toEqual(resourcesData.length);
        verifyResourceTableContent(resourcesData, rows);

        // Now select top 10 records

        var url10 = "rest/resource/getResourcesData?page=1&maxResults=10&orderBy=ASC&sortBy=r.name&status=active&search=%7B%22searchItems%22%3A%5B%5D%7D";
        var resourcesData10 = resourcesData.slice(0, 10);
        var responseString10 = JSON.stringify(resourcesData10);
        AjaxHelper.setupFakeServerTextResponse(server, url10, responseString10);

        $('#resourceTableControls_recordsPerPageSelector').val(10);
        // FIXME: kludge! the change event was not triggered when the element's value was changed programmatically
        // Is that expected behavior? are we doing something wrong? needs research
        $('#resourceTableControls_recordsPerPageSelector').trigger('change');
        server.respond();

        rows = $(".resourceTable-tableRow");
        expect(rows.length).toEqual(10);

        verifyResourceTableContent(resourcesData10, rows);

        var actionLink = $(".resourceTable-tableRow td:eq(4) a");
        expect(actionLink.html()).toEqual('More Actions');
        var firstRowResourceId = resourcesData10[0].id;
        var firstRowResourceName = resourcesData10[0].resource;

        expect(actionLink.attr('href')).toEqual('javascript:resource_handleResourceSelection(' + firstRowResourceId + ')');

        var resourceStateSelectionLabel = $("#resourceState").find("strong").text();
        expect(resourceStateSelectionLabel).toBe("View:");
    });

    it('resets the page number after changing the view', function(){
        var server = UtilHelper.getFakeServer();

        SessionDataHelper.setUser(SUPER_ADMIN);

        // load the home page into the DOM
        FixtureHelper.loadSourceHtmlFixtureIntoDom('resource_screen.html');

        // mock the server responding to the AJAX calls
        var url = "rest/resource/getResourcesData?page=1&maxResults=100&orderBy=ASC&sortBy=r.name&status=active&search=%7B%22searchItems%22%3A%5B%5D%7D";
        var responseString = FixtureHelper.getTestFixtureContent('resources/json/resource-list.json');
        var resourcesData = JSON.parse(responseString);
        AjaxHelper.setupFakeServerTextResponse(server, url, responseString);

        // method under test: call the function which populates the 'resource_screen.html' page
        resourcePage.init();

        // this is where things that were asynchronous came to be synchronous
        // i.e. let the mock server respond to pending AJAX calls, and execute the callbacks
        server.respond();

        var resourceStateSelector = $("#stateSelector");
        expect(resourceStateSelector.is(":visible")).toBe(true);

        expect(resourceStateSelector.val()).toBe('active');
        expect($("#stateSelector option:selected").text()).toBe('Active Only');

        expect($("#resourceTableControls_gotoPageInput").val()).toBe('1');
        $("#resourceTableControls_gotoPageInput").val("2");
        expect($("#resourceTableControls_gotoPageInput").val()).toBe('2');


        var resourcesData10 = resourcesData.slice(0, 10);
        var responseString10 = JSON.stringify(resourcesData10);

        var urlInactive = "rest/resource/getResourcesData?page=1&maxResults=100&orderBy=ASC&sortBy=r.name&status=inactive&search=%7B%22searchItems%22%3A%5B%5D%7D";
        AjaxHelper.setupFakeServerTextResponse(server, urlInactive, responseString10);

        $("#stateSelector").val("inactive").change();

        server.respond();

        expect($("#resourceTableControls_gotoPageInput").val()).toBe('1');
    });

    /**
     * A helper function for verifying the content of the resources table
     */
    function verifyResourceTableContent(resourcesData, rows) {

        // Verify that the table contains the resources data that we provided the AJAX call, formatted and rearranged
        $.each(rows, function(rowIndex, row) {
            var cells = $(row).children();
            var resource = resourcesData[rowIndex];
            var expectedResourceData = [];
            expectedResourceData[0] = resource.resource;
            expectedResourceData[1] = resource.resourceType;
            expectedResourceData[2] = resource.sublocation;
            expectedResourceData[3] = resource.active ? 'Active' : 'Inactive';
            $.each(cells, function(cellIndex, cell) {
                if (cellIndex < 4) {
                    expect($(cell).text()).toEqual(expectedResourceData[cellIndex]);
                }
            });
        });
    }

});

describe('global function resource_handleResourceSelection', function() {

    it("loads a resource in the session storage, by ID, and goes to the resource form", function () {

        var server = UtilHelper.getFakeServer();

        // load resource data
        var resourcesData = FixtureHelper.getTestJsonFixture('resources/json/resource-list.json');
        var resourceData = resourcesData[0];
        var resourceId = resourceData.id;
        var editPageRestUrl = "rest/resource/getResourceDetail?resourceId=" + resourceId;
        var resourceDataResponseString = JSON.stringify(resourceData);

        AjaxHelper.setupFakeServerTextResponse(server, editPageRestUrl, resourceDataResponseString);

        //mock setLocationHref
        // This is a jasmine spy as opposed to a sinon spy
        // sinon spy intercepts the method call but then calls the spied on function
        // jasmine spy intercepts the call and does not call the spied on function
        spyOn(window, "setLocationHref");

        // go to the details page:
        // this way does not work
        //actionLink.click();
        // but this one does: why do we need to explicitly call the following function? it should suffice to click the action like as above
        resource_handleResourceSelection(resourceId);

        server.respond();

        expect(window.setLocationHref).toHaveBeenCalledWith("resource_form.html");
        var sessionResourceData = sessionStorage.getItem("resourceData");
        expect(sessionResourceData).toEqual(resourceDataResponseString);
        expect(app_selectedResource).toEqual(resourceData);

    });

});

describe('global function editResource', function() {

    it('sets the mode to edit and goes to the resource form page', function() {

        spyOn(window, "setLocationHref");

        editResource();

        expect(window.setLocationHref).toHaveBeenCalledTimes(1);
        expect(window.setLocationHref).toHaveBeenCalledWith("resource_form.html");
        expect(JSON.parse(sessionStorage.getItem('mode'))).toBe('edit');

    });

});

describe('global function addNewResourceClick', function() {

    it('prepares the resource form for creating a new resource', function() {

        spyOn(window, "setLocationHref");
        spyOn(window, "clearSelectedResource");

        addNewResourceClick();

        expect(JSON.parse(sessionStorage.getItem('mode'))).toBe('new');
        expect(window.clearSelectedResource).toHaveBeenCalledTimes(1);
        expect(window.setLocationHref).toHaveBeenCalledTimes(1);
        expect(window.setLocationHref).toHaveBeenCalledWith("resource_form.html");

    });
});

describe('resource.js:initResourceTable()', function() {
    it("assembles some columns and creates the resource table widget", function() {
        var spy_Column = spyOn(window, "Column")
            .and.returnValue({});
        var spy_SchedulerTables = spyOn(window, "SchedulerTables")
            .and.returnValue({});

        resourcePage.initResourceTable();

        expect(spy_Column).toHaveBeenCalledTimes(5);
        expect(spy_Column.calls.argsFor(0)).toEqual([{
            dbColumn: 'r.name',
            columnName: "Resource Name",
            width: 36,
            extractDataFunction: jasmine.any(Function),
            defaultSortOrder: Column.ASC,
            filter:{
                name: "resourceName",
                onFilter: jasmine.any(Function)}}
        ]);
        expect(spy_Column.calls.argsFor(1)).toEqual([{
            dbColumn: 'r.resourceType',
            columnName: "Resource Type",
            width: 20,
            extractDataFunction: jasmine.any(Function),
            filter:{
                name: "resourceType",
                onFilter: jasmine.any(Function)}}
        ]);
        expect(spy_Column.calls.argsFor(2)).toEqual([{
            dbColumn: 'rs.sublocation.name',
            columnName: "Sub-Location",
            width: 20,
            extractDataFunction: jasmine.any(Function),
            filter:{
                name: "resourceSublocation",
                onFilter: jasmine.any(Function)}}
        ]);
        expect(spy_Column.calls.argsFor(3)).toEqual([{
            dbColumn: '-rs.active',
            columnName: "Status",
            width: 12,
            extractDataFunction: jasmine.any(Function)}
        ]);
        expect(spy_Column.calls.argsFor(4)).toEqual([{
            width: 12,
            extractDataFunction: jasmine.any(Function),
            columnType: Column.Hyperlink,
            rowElementHyperLink: jasmine.any(Function)}
        ]);

        expect(spy_SchedulerTables).toHaveBeenCalledTimes(1);
        expect(spy_SchedulerTables.calls.argsFor(0)).toEqual([{
            tableId: "resourceTable",
            columns: [{},{},{},{},{}],
            reloadFn: reloadResourcesData
        }]);

        ////////////////////////////////////////
        spy_Column.calls.reset();

        resourcePage.setBulkActivateMode(true);
        resourcePage.initResourceTable();

        expect(spy_Column).toHaveBeenCalledTimes(5);
        expect(spy_Column.calls.argsFor(0)).toEqual([{
            width: 5,
            extractDataFunction: jasmine.any(Function),
            columnType: Column.Checkbox}
        ]);
        expect(spy_Column.calls.argsFor(1)).toEqual([{
            dbColumn: 'r.name',
            columnName: "Resource Name",
            width: 36,
            extractDataFunction: jasmine.any(Function),
            defaultSortOrder: Column.ASC,
            filter:{
                name: "resourceName",
                onFilter: jasmine.any(Function)}}
        ]);

    });
});

describe('resource.js:resourcePage.dataHasSomeInactive()', function() {
    it("checks if array includes inactive resources", function() {
        var emptyData = [];
        var onlyActive = [{active:true}, {active:true}];
        var someInactive = [{active:true}, {active:false}, {active:true}];

        var result = resourcePage.dataHasSomeInactive();
        expect(result).toBe(false);

        result = resourcePage.dataHasSomeInactive(emptyData);
        expect(result).toBe(false);

        result = resourcePage.dataHasSomeInactive(onlyActive);
        expect(result).toBe(false);

        result = resourcePage.dataHasSomeInactive(someInactive);
        expect(result).toBe(true);
    });
});

describe('resource.js:resourcePage.attuneBulkActivateButtons()', function() {
    it("adjusts bulk-activate mode save button", function() {

        var emptyFn = function(){};

        var foo = {
            off: emptyFn, on: emptyFn,
            addClass: emptyFn, removeClass: emptyFn};

        var spy_$ = spyOn(window, "$").and.returnValue(foo);
        var spy_off = spyOn(foo, "off");
        var spy_on = spyOn(foo, "on");
        var spy_addClass = spyOn(foo, "addClass");
        var spy_removeClass = spyOn(foo, "removeClass");

        var spy_dataHasSomeInactive =
            spyOn(resourcePage, "dataHasSomeInactive").and
                .returnValue(true);
        var spy_isBulkActivateMode = spyOn(resourcePage, "isBulkActivateMode")
            .and.returnValue(false);

        resourcePage.attuneBulkActivateButtons();

        expect(spy_on).toHaveBeenCalledTimes(0);
        expect(spy_removeClass).toHaveBeenCalledTimes(0);
        expect(spy_off).toHaveBeenCalledTimes(0);
        expect(spy_addClass).toHaveBeenCalledTimes(0);

        spy_isBulkActivateMode.and.returnValue(true);

        ////////
        resourcePage.attuneBulkActivateButtons();

        expect(spy_on).toHaveBeenCalledTimes(1);
        expect(spy_on.calls.argsFor(0))
            .toEqual(['click', saveBulkActivateResources]);

        expect(spy_removeClass).toHaveBeenCalledTimes(1);
        expect(spy_removeClass.calls.argsFor(0))
            .toEqual(['disabled']);

        ////////
        spy_dataHasSomeInactive.and.returnValue(false);

        resourcePage.attuneBulkActivateButtons();

        expect(spy_off).toHaveBeenCalledTimes(1);
        expect(spy_off.calls.argsFor(0))
            .toEqual(['click']);

        expect(spy_addClass).toHaveBeenCalledTimes(1);
        expect(spy_addClass.calls.argsFor(0))
            .toEqual(['disabled']);
    });
});
