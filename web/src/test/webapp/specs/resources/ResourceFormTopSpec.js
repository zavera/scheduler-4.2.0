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
describe('Resource creation/editing form', function() {

    // TODO: re-visit this test. It doesn't seem to test much at all, certainly not
    // what the it() argument seems to indicate
    it("provides a list of available sublocations when creating resource", function () {

        var server = UtilHelper.getFakeServer();

        // load the home page into the DOM
        FixtureHelper.loadSourceHtmlFixtureIntoDom('resource_form.html');

        var url = "rest/app/getStaticLists";
        var pathToFixture = 'global/json/static-lists.json';
        AjaxHelper.setupFakeServerTextResponseFromTestFixture(server, url, pathToFixture);
        var initCreateSpy = sinon.spy(resourcePage, "initDetails");

        PageLoadUtil.loadPage(".resourceslink", resourcePage.initDetails, true);

        // this is where things that were asynchronous came to be synchronous
        server.respond();

        sinon.assert.calledOnce(initCreateSpy);
    });

});

describe('global function getResourceStaticLists', function() {

    it('sets up a bunch of UI elements', function() {

        FixtureHelper.loadTestHtmlFixtureIntoDom('global/html/elements-for-static-lists.html');
        // put the templates into the <div> that exists in elements-for-static-lists.html
        // so that render.js can access them
        var tableTemplatesContent = FixtureHelper.getSourceFixtureContent("table_templates.html");
        $('#templateSection').html(tableTemplatesContent);

        var server = UtilHelper.getFakeServer();
        var callbackSpy = sinon.spy();

        var url = "rest/app/getStaticLists";
        var pathToFixture = 'global/json/static-lists.json';
        AjaxHelper.setupFakeServerTextResponseFromTestFixture(server, url, pathToFixture);

        app_selectedResource = null;
        // TODO: try with not null too ^^^

        getResourceStaticLists(callbackSpy);

        server.respond();

        sinon.assert.calledOnce(callbackSpy);

        // should be sorted by name
        expect(sublocations).toEqual([
            {name: "Sub-Location 1", institution: {name: "DEMO1", longName: "Demo Institution 1", id: 1}, id: 4},
            {name: "Sub-Location 2", institution: {name: "DEMO1", longName: "Demo Institution 1", id: 1}, id: 2},
            {name: "Sub-Location 3", institution: {name: "DEMO1", longName: "Demo Institution 1", id: 1}, id: 3},
            {name: "Sub-Location 4", institution: {name: "DEMO1", longName: "Demo Institution 1", id: 1}, id: 1}
        ]);

        // should be sorted by name
        expect(sublocationSelectOptions).toEqual(
            "<option value='4'>Sub-Location 1</option>" +
            "<option value='2'>Sub-Location 2</option>" +
            "<option value='3'>Sub-Location 3</option>" +
            "<option value='1'>Sub-Location 4</option>"
        );

        var expectedResourceNames = FixtureHelper.getTestJsonFixture("resources/json/expected-resource-names.json");
        expect(resourceNames).toEqual(expectedResourceNames);

        var expectedResources = FixtureHelper.getTestJsonFixture("resources/json/expected-resources.json");
        expect(resources).toEqual(expectedResources);

        // the following two expect's don't really need to load the expected value from a file
        // because it's only 4 lines.
        //
        // hey watch out: the HTML got 'normalized' when the HMTL content of the sublocation_sublocation
        // select got set. attribute values are surrounded by double quotes now !
        expect($("#sublocation_sublocation").html()).toEqual(
            '<option value="4">Sub-Location 1</option>' +
            '<option value="2">Sub-Location 2</option>' +
            '<option value="3">Sub-Location 3</option>' +
            '<option value="1">Sub-Location 4</option>'
        );

        expect($('#resourceSublocationSelect').html()).toEqual(
            '<option value="4">Sub-Location 1</option>' +
            '<option value="2">Sub-Location 2</option>' +
            '<option value="3">Sub-Location 3</option>' +
            '<option value="1">Sub-Location 4</option>'
        );

        // resourceTypes gets sorted before generating the resourceTypeSelect dropdown options
        expect($("#resourceTypeSelect").html()).toEqual(
            '<option value="Lab">Lab</option>' +
            '<option value="Nursing">Nursing</option>' +
            '<option value="Nutrition">Nutrition</option>' +
            '<option value="Other">Other</option>' +
            '<option value="Room">Room</option>'
        ); // sorted

        // $('#restrictionTypeSelect') is not really testable because
        // its options are set up in table_templates.html

        // NOTE: the following 5 expectations are very brittle. I am removing all blank spaces because the IDE may or may not strip
        // leading spaces. And browsers may or may not do things like remove the double-quotes aroung the value
        // attribute of the select options.

        var expectedNurseAnnotationListHtml = FixtureHelper.getTestFixtureContent("resources/html/expected-nurse-annotation-list.html");
        expect($('#nurseAnnotationList').html().replace(/\s+/g, '')).toEqual(expectedNurseAnnotationListHtml.replace(/\s+/g, ''));

        var expectedNutritionAnnotationListHtml = FixtureHelper.getTestFixtureContent("resources/html/expected-nutrition-annotation-list.html");
        expect($('#nutritionAnnotationList').html().replace(/\s+/g, '')).toEqual(expectedNutritionAnnotationListHtml.replace(/\s+/g, ''));

        var expectedEquipmentAnnotationListHtml = FixtureHelper.getTestFixtureContent("resources/html/expected-equipment-annotation-list.html");
        expect($('#equipmentAnnotationList').html().replace(/\s+/g, '')).toEqual(expectedEquipmentAnnotationListHtml.replace(/\s+/g, ''));

        var expectedRoomAnnotationListHtml = FixtureHelper.getTestFixtureContent("resources/html/expected-room-annotation-list.html");
        expect($('#roomAnnotationList').html().replace(/\s+/g, '')).toEqual(expectedRoomAnnotationListHtml.replace(/\s+/g, ''));

        var expectedLabAnnotationListHtml = FixtureHelper.getTestFixtureContent("resources/html/expected-lab-annotation-list.html");
        expect($('#labAnnotationList').html().replace(/\s+/g, '')).toEqual(expectedLabAnnotationListHtml.replace(/\s+/g, ''));
    });

});

function verifyResourceSublocationSubFormsVisibility(isVisible) {

    expect($("#resourceDetailToggle").is(':visible')).toBe(isVisible);
    expect($("#defaultAvailabilitySection").is(':visible')).toBe(isVisible);
    expect($("#temporaryAdjustmentSection").is(':visible')).toBe(isVisible);
    expect($("#alternateResourcesSection").is(':visible')).toBe(isVisible);
    expect($("#resourceRestrictionsSection").is(':visible')).toBe(isVisible);
    expect($("#annotationsSection").is(':visible')).toBe(isVisible);

}

describe('Resource-sublocation form for a new resource-sublocation', function() {

    beforeEach( function() {

        // app_selectedResource is a global
        app_selectedResource = null;
        SessionDataHelper.setUser(SUPER_ADMIN);

        FixtureHelper.loadTestHtmlFixtureIntoDom('resources/html/elements-for-saveResourceClick.html');

        var server = UtilHelper.getFakeServer();

        var url = "rest/app/getStaticLists";
        var pathToFixture = 'global/json/static-lists.json';
        AjaxHelper.setupFakeServerTextResponseFromTestFixture(server, url, pathToFixture);

        getResourceStaticLists(resourceFormPage.populateForm);

        server.respond();

    });

    it("hides the un-needed form sections for a new resource-sublocation", function() {

        verifyResourceSublocationSubFormsVisibility(false);

    });

    it("does not set a default sublocation name suffix without the user's action, for a new resource-sublocation", function() {

        // the default is to have an empty suffix
        expect($('#resourceNameSuffix').text()).toBe("");

    });

    it("sets the sublocation name suffix according to the user's sublocation selection", function() {

        $("#resourceSublocationSelect").combobox("setValue", 2);

        var expectedResourceNameSuffix = " - Sub-Location 2";
        expect($('#resourceNameSuffix').text()).toBe(expectedResourceNameSuffix);

    });

    it('does not allow saving a new resource-sublocation with an empty name', function() {

        // set an empty resource name
        $("#resourceNameInput").val("");

        // function under test
        saveResourceClick({});

        // expected side-effects

        expect($('#resourceNameInputValidation').text()).toBe('Please enter a Resource Name.');
        // not testable (asynchronous)
        // expect($('#resourceNameInputValidation').css('opacity')).toBe('1.0');
        expect($('#resourceNameInputValidation').css('visibility')).toBe("visible");

    });

    it('does not allow saving a new resource-sublocation with an existing name', function() {

        // set the new resource-sublocation name
        $("#resourceSublocationSelect").combobox("setValue", 1);
        $("#resourceNameInput").val("some resource name");

        // take a shortcut: put the resulting full resource name in the existing resource names array,
        // to verify that the pre-existence of a full resource name prevents creating a new one
        // with the same name
        resourceNames.push(
            $("#resourceNameInput").val() + " - " + $("#resourceSublocationSelect").combobox("getText")
        )

        // function under test
        saveResourceClick({});

        // expected side-effects

        expect($('#resourceNameInputValidation').text()).toBe('This resource name already exists.');
        // not testable (asynchronous)
        // expect($('#resourceNameInputValidation').css('opacity')).toBe('1.0');
        expect($('#resourceNameInputValidation').css('visibility')).toBe("visible");

    });

    function executeSaveAndActivateResourceSublocationWithNewName(clickYes, success) {

        // set the new resource-sublocation name
        $("#resourceTypeSelect").combobox("setValue", 1);
        $("#resourceSublocationSelect").combobox("setValue", 1);
        $("#resourceNameInput").val("some new resource name");

        // take a shortcut: put the resulting full resource name in the existing resource names array,
        // to verify that the pre-existence of a full resource name prevents creating a new one
        // with the same name
        resourceNames.push(
            "some old resource name" + " - " + $("#resourceSublocationSelect").combobox("getText")
        );

        // function under test
        saveResourceClick({active: true});

        // set up response to next ** 2 ** ajax calls
        //    POST call to create the new resource-sublocation)
        //    GET call to get selected resource details and save them in browser's session storage

        var server = UtilHelper.getFakeServer();

        var resourceId = 99;
        var url = "rest/resource/createResource";
        var responseString;
        if (success) {
            responseString = JSON.stringify({
                result: true,
                name: 'new name',
                resourceId: resourceId
            });
        }
        else {
            responseString = JSON.stringify({
                result: false,
                errorMsg: "server-side error message"
            });
        }
        AjaxHelper.setupFakeServerPostTextResponse(server, url, responseString);

        url = "rest/resource/getResourceDetail?resourceId=" + resourceId;
        var resourceDetailsResponseData = { something: "something"};
        var resourceDetailsResponseString = JSON.stringify(resourceDetailsResponseData);
        AjaxHelper.setupFakeServerTextResponse(server, url, resourceDetailsResponseString);

        spyOn(window, "setLocationHref");
        spyOn($, "blockUI");
        spyOn(window, "util_showMainMessage");

        // // By now the activation confirmation dialog should be displayed

        if (clickYes) {
            // // Get the dialog's buttons and click on "Yes"
            $('div[aria-describedby="activateDialog"] .ui-button:contains("Yes")').click();

            // 2 ajax calls will be generated by clicking "Yes"
            server.respond();
            server.respond();

            if (success) return {
                resourceDetailsResponseString: resourceDetailsResponseString,
                resourceDetailsResponseData: resourceDetailsResponseData
            }
            else return {
                errorMessage: "server-side error message"
            }
        }
        else {
            $('div[aria-describedby="activateDialog"] .ui-button:contains("No")').click();
            return;
        }

    }

    it('allows saving and activating a new resource-sublocation with a new name', function() {

        // perform the user action(s)

        var expectedThings = executeSaveAndActivateResourceSublocationWithNewName(true, true);

        // expected side-effects

        expect($.blockUI).toHaveBeenCalledTimes(1);
        expect(window.util_showMainMessage).toHaveBeenCalledTimes(1);
        expect(window.util_showMainMessage).toHaveBeenCalledWith("Success! This resource has been created.");

        expect(sessionStorage.getItem("resourceData")).toEqual(expectedThings.resourceDetailsResponseString);
        expect(app_selectedResource).toEqual(expectedThings.resourceDetailsResponseData);
        // note double quotes:
        expect(sessionStorage.getItem("mode")).toEqual('"view"');
        expect(window.setLocationHref).toHaveBeenCalledTimes(1);
        expect(window.setLocationHref).toHaveBeenCalledWith("resource_form.html");

        expect($('#resourceNameInputValidation').text()).toBe('');
        // not testable (asynchronous)
        // expect($('#resourceNameInputValidation').css('opacity')).toBe('1.0');
        expect($('#resourceNameInputValidation').css('visibility')).toBe("hidden");

    });

    it('warns the user when saving a new resource-sublocation with a new name results in a server-side error', function() {

        // Pre-conditions
        var preExistingResourceData = null;
        var preExistingResourceString = JSON.stringify(preExistingResourceData);
        app_selectedResource = preExistingResourceData;
        sessionStorage.setItem("resourceData", preExistingResourceString);
        var preExistingMode = "blah";
        sessionStorage.setItem("mode", JSON.stringify(preExistingMode));

        // perform the user action(s)

        var expectedThings = executeSaveAndActivateResourceSublocationWithNewName(true, false);

        // expected side-effects

        expect(window.util_showMainMessage).toHaveBeenCalledTimes(1);
        expect(window.util_showMainMessage).toHaveBeenCalledWith(expectedThings.errorMessage);

        expect(sessionStorage.getItem("resourceData")).toEqual(preExistingResourceString);
        expect(app_selectedResource).toEqual(preExistingResourceData);

        // note double quotes:
        expect(JSON.parse(sessionStorage.getItem("mode"))).toEqual(preExistingMode);
        expect(window.setLocationHref).toHaveBeenCalledTimes(0);

        expect($('#resourceNameInputValidation').text()).toBe('');
        // not testable (asynchronous)
        // expect($('#resourceNameInputValidation').css('opacity')).toBe('1.0');
        expect($('#resourceNameInputValidation').css('visibility')).toBe("hidden");

    });

    it('lets the user cancel activation of the new resource-sublocation by clicking the "No" button', function() {

        // Pre-conditions
        var preExistingResourceData = null;
        var preExistingResourceString = JSON.stringify(preExistingResourceData);
        app_selectedResource = preExistingResourceData;
        sessionStorage.setItem("resourceData", preExistingResourceString);
        var preExistingMode = "blah";
        sessionStorage.setItem("mode", JSON.stringify(preExistingMode));

        // perform the user action(s)

        executeSaveAndActivateResourceSublocationWithNewName(false);

        // expected side-effects

        expect(window.util_showMainMessage).toHaveBeenCalledTimes(0);

        expect(sessionStorage.getItem("resourceData")).toEqual(preExistingResourceString);
        expect(app_selectedResource).toEqual(preExistingResourceData);

        expect(JSON.parse(sessionStorage.getItem("mode"))).toEqual(preExistingMode);
        expect(window.setLocationHref).toHaveBeenCalledTimes(0);

    });

    function executeSaveWithoutActivatingResourceSublocationWithNewName(success) {

        // set the new resource-sublocation name
        $("#resourceTypeSelect").combobox("setValue", 1);
        $("#resourceSublocationSelect").combobox("setValue", 1);
        $("#resourceNameInput").val("some new resource name");

        // take a shortcut: put the resulting full resource name in the existing resource names array,
        // to verify that the pre-existence of a full resource name prevents creating a new one
        // with the same name
        resourceNames.push(
            "some old resource name" + " - " + $("#resourceSublocationSelect").combobox("getText")
        );

        var server = UtilHelper.getFakeServer();

        var resourceId = 99;
        var url = "rest/resource/createResource";
        var responseString;
        if (success) {
            responseString = JSON.stringify({
                result: true,
                name: 'new name',
                resourceId: resourceId
            });
        }
        else {
            responseString = JSON.stringify({
                result: false,
                errorMsg: "server-side error message"
            });
        }
        AjaxHelper.setupFakeServerPostTextResponse(server, url, responseString);

        // more mocking for simplicity (the mocked functions have been
        // tested elsewhere to some extent).

        spyOn(window, "setLocationHref");
        spyOn($, "blockUI");
        spyOn(window, "util_showMainMessage");
        spyOn(window, 'resource_handleResourceSelection');

        // function under test
        saveResourceClick({active: false});

        server.respond();

        if (success) return {
            resourceId: resourceId
        }
        else return {
            errorMessage: "server-side error message"
        }

    }

    it('lets the user save a new inactive resource-sublocation without confirmation', function() {

        // perform the user action(s)

        var expectedThings = executeSaveWithoutActivatingResourceSublocationWithNewName(true);

        // expected side-effects

        expect(window.resource_handleResourceSelection).toHaveBeenCalledTimes(1);
        expect(window.resource_handleResourceSelection).toHaveBeenCalledWith(expectedThings.resourceId);
        expect(window.util_showMainMessage).toHaveBeenCalledTimes(1);
        expect(window.util_showMainMessage).toHaveBeenCalledWith("Success! This resource has been created.");

    });

    it('does nothing and lets the user know of a server-side error when saving a new inactive resource-sublocation', function() {

        // perform the user action(s)

        var expectedThings = executeSaveWithoutActivatingResourceSublocationWithNewName(false);

        // expected side-effects

        expect(window.resource_handleResourceSelection).toHaveBeenCalledTimes(0);
        expect(window.util_showMainMessage).toHaveBeenCalledTimes(1);
        expect(window.util_showMainMessage).toHaveBeenCalledWith(expectedThings.errorMessage);

    });

});

describe('Resource-sublocation form for an existing resource-sublocation', function() {

    var existingResourceName;
    var existingSublocationId;
    var existingSublocationName;
    var existingFullResourceName;

    beforeEach(function () {

        // app_selectedResource is a global
        app_selectedResource = null;
        SessionDataHelper.setUser(SUPER_ADMIN);
        FixtureHelper.loadTestHtmlFixtureIntoDom('resources/html/elements-for-saveResourceClick.html');

        var server = UtilHelper.getFakeServer();

        var url = "rest/app/getStaticLists";
        var pathToFixture = 'global/json/static-lists.json';
        AjaxHelper.setupFakeServerTextResponseFromTestFixture(server, url, pathToFixture);

        getResourceStaticLists(resourceFormPage.populateForm);

        server.respond();

        // set the new resource-sublocation name
        existingSublocationId = 1;
        $("#resourceSublocationSelect").combobox("setValue", existingSublocationId);
        existingSublocationName = $("#resourceSublocationSelect").combobox("getText");
        existingResourceName = "a resource name";
        $("#resourceNameInput").val(existingResourceName);
        existingFullResourceName = $("#resourceNameInput").val() + " - " + $("#resourceSublocationSelect").combobox("getText");

    });

    it('does not allow saving an existing resource-sublocation with a modified name ' +
        ' matching the name of another pre-existing resource', function () {

        // take a shortcut: set the selected resource to have a name different from
        // the full resource name in the form, but put that full resource name
        // in the existing resource names array,
        // to verify that the pre-existence of a full resource name does prevent saving
        // the selected resource when its full name was modified to be the
        // pre-existing resource name
        app_selectedResource = {
            resource: existingFullResourceName + "it originally was something else"
        };
        resourceNames.push(
            existingFullResourceName
        );

        // function under test
        saveResourceClick({});

        // expected side-effects

        expect($('#resourceNameInputValidation').text()).toBe('This resource name already exists.');
        // not testable (asynchronous)
        // expect($('#resourceNameInputValidation').css('opacity')).toBe('1.0');
        expect($('#resourceNameInputValidation').css('visibility')).toBe("visible");

    });

    it('does allow saving an existing resource-sublocation when its name ' +
        ' has not been modified', function () {

        // take a shortcut: set the selected resource to have a name the same as
        // the full resource name in the form, and put that full resource name
        // in the existing resource names array,
        // to verify that the pre-existence of a full resource name does not prevent saving
        // the selected resource when its full name was not modified
        app_selectedResource = {
            resource: existingFullResourceName
        };
        resourceNames.push(
            existingFullResourceName
        );

        // function under test
        saveResourceClick({});

        // expected side-effects

        expect($('#resourceNameInputValidation').text()).toBe('');
        expect($('#resourceNameInputValidation').css('visibility')).toBe("hidden");

    });

    it('does not allow saving an existing resource-sublocation when its name ' +
        ' has invalid characters', function () {

        app_selectedResource = {
            resource: existingFullResourceName
        };
        resourceNames.push(
            existingFullResourceName
        );

        // (/^[a-zA-Z0-9()/ ,&-]*
        var someLegalCharacters = ['a', 'A', '0', '(', ')', '/', ' ', ',', '&', '-'];
        var someIllegalCharacters = ['!', '@', '#', '$', '%', '^', '*', '?', '_', '#', '"', "'", '[', ']', '=', '+', ';', ':'];
        var baseName = "my new name";

        someLegalCharacters.forEach(function(value) {
            $("#resourceNameInput").val(baseName + value);
            // function under test
            saveResourceClick({});
            // expected side-effects
            expect($('#resourceNameInputValidation').text()).toBe('');
            expect($('#resourceNameInputValidation').css('visibility')).toBe("hidden");
        });

        someIllegalCharacters.forEach(function(value) {
            $("#resourceNameInput").val(baseName + value);
            // function under test
            saveResourceClick({});
            // expected side-effects
            expect($('#resourceNameInputValidation').text()).toBe('This resource name contains illegal characters.');
            expect($('#resourceNameInputValidation').css('visibility')).toBe("visible");
        });

    });

    it('asks for confirmation when activating an existing resource-sublocation, ' +
        'calls the function to modify the resource data, ' +
        'and closes the dialog, if the user clicks "Yes"', function() {

        spyOn(window, 'modifyResource');

        app_selectedResource = {
            resource: existingFullResourceName
        };
        resourceNames.push(
            existingFullResourceName
        );

        $("#activateResourceCheckbox").prop('checked', true);

        saveResourceClick({});

        expect($('#activateDialog').is(':visible')).toBe(true);

        $('div[aria-describedby="activateDialog"] .ui-button:contains("Yes")').click();

        expect($('#activateDialog').is(':visible')).toBe(false);
        expect(window.modifyResource).toHaveBeenCalled();

    })

    it('asks for confirmation when activating an existing resource-sublocation, ' +
        'does NOT call the function to modify the resource data, ' +
        'unsets the "activate" checkbox, and closes the dialog, if the user clicks "No"', function() {

        spyOn(window, 'modifyResource');

        app_selectedResource = {
            resource: existingFullResourceName
        };
        resourceNames.push(
            existingFullResourceName
        );

        $("#activateResourceCheckbox").prop('checked', true);

        saveResourceClick({});

        expect($('#activateDialog').is(':visible')).toBe(true);

        $('div[aria-describedby="activateDialog"] .ui-button:contains("No")').click();

        expect($('#activateDialog').is(':visible')).toBe(false);
        expect(window.modifyResource).not.toHaveBeenCalled();
        expect($("#activateResourceCheckbox").prop('checked')).toBe(false);

    })

});
