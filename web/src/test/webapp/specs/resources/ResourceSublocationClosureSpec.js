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
describe('Sublocation closure page', function () {

    function spyOnCommonInit() {
        // NOTE: commonInit() is not testable, because it is declared
        // within a IIEF closure and never returned to the caller by the IIEF)
        // so we cannot spy on it. Instead we spy on the methods that it calls
        spyOn(window, "loadMetaHeaders");
        spyOn(window, "initFooter");
        spyOn(window, "eraseLicense");
    }

    function verifyCommonInit() {
        // NOTE: commonInit() is not testable, because it is declared
        // within a IIEF closure and never returned to the caller by the IIEF)
        // so we cannot spy on it. Instead we spy on the methods that it calls
        expect(window.loadMetaHeaders).toHaveBeenCalledTimes(1);
        expect(window.initFooter).toHaveBeenCalledTimes(1);
        expect(window.eraseLicense).toHaveBeenCalledTimes(1);
    }

    it('sets up sublocation-addition data', function () {

        spyOnCommonInit();
        spyOn(window, "getResourceStaticLists");
        spyOn(window, "app_runIdleTimer");

        resourcePage.initSublocationAddition();

        // 3 functions called by commonInit()
        verifyCommonInit();
        // other 2 functions called directly by initSubLocationAddition();
        expect(window.getResourceStaticLists).toHaveBeenCalledWith(createSublocationClosureData);
        expect(window.app_runIdleTimer).toHaveBeenCalledTimes(1);

    });

    it('sets up the add sublocation closure UI', function () {

        spyOn(window, "commonData");
        spyOn(window, "renderBreadcrumbs"); // called with argument 'resource_close_sub_form'
        spyOn(window, "sublocationClosureInterval_createWidgets");
        spyOn(window, "sublocation_clearForm");

        createSublocationClosureData();

        expect(window.commonData).toHaveBeenCalledTimes(1);
        expect(window.renderBreadcrumbs).toHaveBeenCalledTimes(1);
        expect(window.renderBreadcrumbs).toHaveBeenCalledWith('resource_close_sub_form');
        expect(window.sublocation_clearForm).toHaveBeenCalledTimes(1);

    });

    it('sets up sublocation closure data', function () {

        spyOn(window, "getResourceStaticLists");
        spyOnCommonInit();
        spyOn(window, "app_runIdleTimer");
        spyOn(window, "sublocationClosureScreenData");

        resourcePage.initSublocationClosure();

        verifyCommonInit();
        expect(window.getResourceStaticLists).toHaveBeenCalledTimes(1);
        expect(window.app_runIdleTimer).toHaveBeenCalledTimes(1);
        expect(window.sublocationClosureScreenData).toHaveBeenCalledTimes(1);

    });

    it('sets up a sublocation closure list', function () {

        spyOn(window, "commonData");
        spyOn(window, "renderBreadcrumbs"); // called with argument 'resource_close_sub_list'
        spyOn(window, "resetUI");
        spyOn(window, "setParentLocationHash");
        spyOn(window, "loadSublocationClosureData");

        initial_load = false;

        sublocationClosureScreenData();

        expect(window.commonData).toHaveBeenCalledTimes(1);
        expect(window.renderBreadcrumbs).toHaveBeenCalledTimes(1);
        expect(window.resetUI).toHaveBeenCalledTimes(1);
        expect(window.setParentLocationHash).toHaveBeenCalledTimes(1);
        expect(window.setParentLocationHash).toHaveBeenCalledWith("SublocationClosureInterval");
        expect(window.loadSublocationClosureData).toHaveBeenCalledTimes(1);
        expect(initial_load).toBe(true);
    });

    it('populates sublocation table', function () {

        var server = UtilHelper.getFakeServer();
        FixtureHelper.loadSourceHtmlFixtureIntoDom('sublocation_closure_screen.html');

        initial_load = false;

        var responseString = JSON.stringify(sublocationClosureData);

        var currentPage = 1;
        // this number needs to match the default number of records per page for this table
        max_results = 50;

        var url = "rest/resource/getSublocationClosureIntervals?page=1" +
            "&maxResults=" + max_results + "&orderBy=ASC&sortBy=sci.sublocation.name";
        AjaxHelper.setupFakeServerTextResponse(server, url, responseString);

        loadSublocationClosureData();

        server.respond();

        var sublocationTable = $("#sublocationClosureTable");
        expect(sublocationTable.find("tbody > tr").length).toBe(3);

        var firstDataRow = $(sublocationTable.find("tbody > tr").get(1));
        expect(firstDataRow.find("td").length).toBe(5);

        var firstStartDateTime = firstDataRow.find("td:nth-child(2)").text();
        expect(firstStartDateTime).toBe("12/24/2017 17:00");
    });

    it('creates the UI elements for adding a sublocation closure', function () {
        FixtureHelper.loadSourceHtmlFixtureIntoDom('add_sublocation_closure.html');

        sublocationSelectOptions = buildSelectOptions(sublocationData, 'name');
        $("#sublocation_sublocation").html(sublocationSelectOptions);
        WidgetUtil.createComboBox("#sublocation_sublocation");

        sublocationClosureInterval_createWidgets();

        var actualStartDateTimeVal = $("#sublocation_startDate").val();
        var startDateMiltaryTime = showDateTime(actualStartDateTimeVal);
        expect(startDateMiltaryTime).toBe(actualStartDateTimeVal);

        var actualEndDateTimeVal = $("#sublocation_endDate").val();
        var endDateMiltaryTime = showDateTime(actualEndDateTimeVal);
        expect(endDateMiltaryTime).toBe(actualEndDateTimeVal);

        expect($('#sublocation_sublocation').combobox("getValue")).toBe('');
        expect($('#sublocation_reason').val()).toBe('');
    });

    var sublocationClosureData = [
        {
            id: 1,
            totalCount: 146,
            sublocation: "Sublocation 1",
            startDate: "Dec 24, 2017 5:00:00 PM",
            endDate: "Dec 31, 2017 11:59:00 PM",
            reason: "Holiday"
        },
        {
            id: 2,
            totalCount: 146,
            sublocation: "Sublocation 2",
            startDate: "Jan 22, 2017 12:00:00 AM",
            endDate: "Jan 22, 2017 11:59:00 PM",
            reason: "Holiday"
        }
    ];

    var sublocationData = [
        {
            name: "Sublocation 1",
            institution: {
                name: "BWH",
                longName: "Institution 1",
                id: 3
            },
            id: 1
        },
        {
            name: "Sublocation 2",
            institution: {
                name: "BWH",
                longName: "Institution 1",
                id: 5
            },
            id: 2
        }
    ]
});

describe('global function addSublocationClosureClick', function () {

    it('takes the user to the "add sublocation_closure" page', function () {

        spyOn(window, "setLocationHref");

        addSublocationClosureClick();

        expect(window.setLocationHref).toHaveBeenCalledTimes(1);
        expect(window.setLocationHref).toHaveBeenCalledWith("add_sublocation_closure.html");

    });

});

describe('global function sublocationClosureClick', function () {

    it('takes the user to the "sublocation_closure" page', function () {

        spyOn(window, "setLocationHref");

        sublocationClosureClick();

        expect(window.setLocationHref).toHaveBeenCalledTimes(1);
        expect(window.setLocationHref).toHaveBeenCalledWith("sublocation_closure_screen.html");

    });

});

