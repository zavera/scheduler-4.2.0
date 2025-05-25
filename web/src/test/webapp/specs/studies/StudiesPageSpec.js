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
describe('Studies Page', function () {

    beforeEach(function () {
        document.body.innerHTML = "";
    });

    it("has a form for creating a new study", function () {

        // load the home page into the DOM
        FixtureHelper.loadSourceHtmlFixtureIntoDom('create_study.html');

        var server = UtilHelper.getFakeServer();

        // NOTE: the return type is not application/json because otherwise the
        // getStaticLists() function would fail (it wants to parse the incoming
        // String and build a JSON object itself.
        var responseString = FixtureHelper.getTestFixtureContent('studies/json/studies-static-list.json');
        var url = "rest/app/getStaticLists";

        server.respondWith("GET", url,
            [200, {"Content-Type": "text/text"}, responseString]);
        var initCreateStudyPageSpy = sinon.spy(studyPage, "initCreateStudyPage");

        PageLoadUtil.loadPage(".studieslink", studyPage.initCreateStudyPage, true);

        // this is where things that were asynchronous came to be synchronous
        server.respond();

        sinon.assert.calledOnce(initCreateStudyPageSpy);
    });

    it("displays the list of studies table", function () {
        var server = UtilHelper.getFakeServer();

        SessionDataHelper.setUser(SCHEDULER);

        // load the home page into the DOM
        FixtureHelper.loadSourceHtmlFixtureIntoDom('study_screen.html');
        AjaxHelper.setUpFakeServerLoadSourceHtmlResponse(server, "header.html");

        var responseString = FixtureHelper.getTestFixtureContent('studies/json/studies-list.json');
        var studyData = JSON.parse(responseString);

        var getStudyListUrl = "rest/study/getStudyList?page=1&maxResults=50&orderBy=ASC"
            + "&sortBy=s.name&search=%7B%22searchItems%22%3A%5B%5D%7D";

        AjaxHelper.setupFakeServerTextResponse(server, getStudyListUrl, responseString);

        onloadStudyScreen();

        server.respond();
        server.respond();

        expect($("#studiesTable-filterRow .filterField").length).toBe(5);
        //one row with column names and three rows of data
        expect($("#studiesTable").find("tbody tr").length).toBe(4);
        expect($("#studiesTablePagination").find("div").length).toBe(3);
        expect($("#studiesTable_0").hasClass("sorting_asc")).toBe(true);
    });
});

///////////////////// myopic unit tests //////////

describe('staticListsDataCallback', function () {

    it('loads some static data into global variables', function () {

        // override some globals used by fn under test
        yesNo = ['y', 'n'];
        adultPediatric = ['a', 'p'];
        crcCategories = ['a', 'b'];

        // fn under test
        StudyScreen.staticListsDataCallback(HelpStudies.sampleStaticData);

        expect(fundingSources.length).toBe(2);
        expect(centersAndInstitutions.length).toBe(2);
        expect(studyStatuses.length).toBe(2);
        expect(institutions.length).toBe(2);
        expect(irbInstitutions.length).toBe(2);
        expect(visitTypes.length).toBe(2);
        expect(sublocations.length).toBe(2);

        expect(fundingSources[0].name).toBe('Pilot Grant');
        expect(centersAndInstitutions[0].name).toBe('Center for Information Technology (CIT)');
        expect(studyStatuses[0].name).toBe('IRB_PROCESS');
        expect(institutions[0].name).toBe('DEMO1');
        expect(irbInstitutions[0].name).toBe('IRB Demo Inst 1');
        expect(visitTypes[0].name).toBe('Outpatient CRC');
        expect(sublocations[0].name).toBe('Sub-Location 1');

        expect(yesNoSelectOptions).toBe(
            "<option value='y'>y</option>" +
            "<option value='n'>n</option>");
        expect(adultPediatricSelectOptions).toBe(
            "<option value=''></option>" +
            "<option value='a'>a</option>" +
            "<option value='p'>p</option>");
        expect(crcCategorySelectOptions).toBe(
            "<option value=''></option>" +
            "<option value='a'>a</option>" +
            "<option value='b'>b</option>");
    });

    it('loads some options', function () {

        var spy_buildSelectOptions = spyOn(window, "buildSelectOptions");

        StudyScreen.staticListsDataCallback(HelpStudies.sampleStaticData);

        expect(spy_buildSelectOptions).toHaveBeenCalledTimes(7);
        this.callIndex = 0;
        expect(spy_buildSelectOptions.calls.argsFor(this.callIndex++)).toEqual(
            [studyStatuses, 'shortName']);
        expect(spy_buildSelectOptions.calls.argsFor(this.callIndex++)).toEqual(
            [institutions, 'longName']);
        expect(spy_buildSelectOptions.calls.argsFor(this.callIndex++)).toEqual(
            [fundingSources, 'name', ""]);
        expect(spy_buildSelectOptions.calls.argsFor(this.callIndex++)).toEqual(
            [centersAndInstitutions, 'name', ""]);
        expect(spy_buildSelectOptions.calls.argsFor(this.callIndex++)).toEqual(
            [irbInstitutions, 'name', ""]);
        expect(spy_buildSelectOptions.calls.argsFor(this.callIndex++)).toEqual(
            [visitTypes, 'name']);
        expect(spy_buildSelectOptions.calls.argsFor(this.callIndex++)).toEqual(
            [sublocations, 'name']);
    });

    it('calls dollar to load the DOM', function () {

        var spy_dollar = spyOn(window, "$").and.callThrough();
        var spy_html = spyOn($.fn, "html");

        StudyScreen.staticListsDataCallback(HelpStudies.sampleStaticData);

        expect(spy_dollar).toHaveBeenCalledTimes(16);
        this.callIndex = 0;
        expect(spy_dollar.calls.argsFor(this.callIndex++)).toEqual(["#study_status"]);
        expect(spy_dollar.calls.argsFor(this.callIndex++)).toEqual(["#study_institution"]);
        expect(spy_dollar.calls.argsFor(this.callIndex++)).toEqual(["#study_irbInstitution"]);
        expect(spy_dollar.calls.argsFor(this.callIndex++)).toEqual(["#study_fundingSource1"]);
        expect(spy_dollar.calls.argsFor(this.callIndex++)).toEqual(["#study_fundingSource2"]);
        expect(spy_dollar.calls.argsFor(this.callIndex++)).toEqual(["#study_fundingSource3"]);
        expect(spy_dollar.calls.argsFor(this.callIndex++)).toEqual(["#study_fundingSource4"]);
        expect(spy_dollar.calls.argsFor(this.callIndex++)).toEqual(["#study_fundingInstitution1"]);
        expect(spy_dollar.calls.argsFor(this.callIndex++)).toEqual(["#study_fundingInstitution2"]);
        expect(spy_dollar.calls.argsFor(this.callIndex++)).toEqual(["#study_fundingInstitution3"]);
        expect(spy_dollar.calls.argsFor(this.callIndex++)).toEqual(["#study_fundingInstitution4"]);
        expect(spy_dollar.calls.argsFor(this.callIndex++)).toEqual(["#study_industryInitiated"]);
        expect(spy_dollar.calls.argsFor(this.callIndex++)).toEqual(["#study_pediatric"]);
        expect(spy_dollar.calls.argsFor(this.callIndex++)).toEqual(["#study_crcCategory"]);
        expect(spy_dollar.calls.argsFor(this.callIndex++)).toEqual(["#study_visitType"]);
        expect(spy_dollar.calls.argsFor(this.callIndex++)).toEqual(["#study_visitSublocation"]);

        expect(spy_html).toHaveBeenCalledTimes(16);
        this.callIndex = 0;
        expect(spy_html.calls.argsFor(this.callIndex++)).toEqual([studyStatusSelectOptions]);
        expect(spy_html.calls.argsFor(this.callIndex++)).toEqual([institutionSelectOptions]);
        expect(spy_html.calls.argsFor(this.callIndex++)).toEqual([irbInstitutionsSelectOptions]);
        expect(spy_html.calls.argsFor(this.callIndex++)).toEqual([fundingSourcesSelectOptions]);
        expect(spy_html.calls.argsFor(this.callIndex++)).toEqual([fundingSourcesSelectOptions]);
        expect(spy_html.calls.argsFor(this.callIndex++)).toEqual([fundingSourcesSelectOptions]);
        expect(spy_html.calls.argsFor(this.callIndex++)).toEqual([fundingSourcesSelectOptions]);
        expect(spy_html.calls.argsFor(this.callIndex++)).toEqual([centersAndInstitutionsSelectOptions]);
        expect(spy_html.calls.argsFor(this.callIndex++)).toEqual([centersAndInstitutionsSelectOptions]);
        expect(spy_html.calls.argsFor(this.callIndex++)).toEqual([centersAndInstitutionsSelectOptions]);
        expect(spy_html.calls.argsFor(this.callIndex++)).toEqual([centersAndInstitutionsSelectOptions]);
        expect(spy_html.calls.argsFor(this.callIndex++)).toEqual([yesNoSelectOptions]);
        expect(spy_html.calls.argsFor(this.callIndex++)).toEqual([adultPediatricSelectOptions]);
        expect(spy_html.calls.argsFor(this.callIndex++)).toEqual([crcCategorySelectOptions]);
        expect(spy_html.calls.argsFor(this.callIndex++)).toEqual([visitTypeSelectOptions]);
        expect(spy_html.calls.argsFor(this.callIndex++)).toEqual([sublocationSelectOptions]);

    });

    it('calls callbackFuncIfDefined', function () {

        var spy_callbackFuncIfDefined = spyOn(window, "callbackFuncIfDefined");
        var fooFn = function () {
        };

        StudyScreen.staticListsDataCallback(HelpStudies.sampleStaticData, fooFn);

        expect(spy_callbackFuncIfDefined).toHaveBeenCalled();
        expect(spy_callbackFuncIfDefined.calls.argsFor(0)).toEqual([fooFn]);
    });


}); // describe

describe('getStudyStaticLists', function () {
    beforeAll(function () {
        window.location.ref = 'foo';
    });

    it("rest-calls getStaticLists for most pages", function () {

        var spy_staticListsDataCallback = spyOn(StudyScreen, "staticListsDataCallback");
        // cut out the sin(on)ful middleman
        var spy_get = spyOn($, "get").and.callFake(function (url, callback) {
            callback('"foo"');
        });

        getStudyStaticLists();

        expect(spy_get).toHaveBeenCalledTimes(1);
        expect(spy_get.calls.argsFor(0)).toContain("rest/app/getStaticLists");

        expect(spy_staticListsDataCallback).toHaveBeenCalledTimes(1);
        expect(spy_staticListsDataCallback.calls.argsFor(0)).toContain('"foo"');
    });
    it("can call getStudyMemberLists for create_study page", function () {

        var spy_getLocationHref = spyOn(window, "getLocationHref").and.returnValue('/create_study.html');
        var spy_getItem = spyOn(sessionStorage, "getItem").and.returnValue(HelpStudies.sampleUser);
        var spy_getStudyMemberLists = spyOn(window, "getStudyMemberLists");
        // cut out the sin(on)ful middleman
        var spy_get = spyOn($, "get").and.callFake(function (url, callback) {
            callback();
        });
        var spyStaticListsDataCallback = spyOn(StudyScreen, "staticListsDataCallback");

        getStudyStaticLists();

        expect(spy_getLocationHref).toHaveBeenCalledTimes(2);

        expect(spy_getItem).toHaveBeenCalledTimes(1);
        expect(spy_getItem.calls.argsFor(0)).toEqual(['userData']);

        expect(spy_getStudyMemberLists).toHaveBeenCalledTimes(1);

        expect(spy_get).toHaveBeenCalledTimes(1);
        expect(spy_get.calls.argsFor(0)).toContain("rest/app/getStaticLists");

        expect(spyStaticListsDataCallback).toHaveBeenCalledTimes(1);
    });

    it("can refuse to call getStudyMemberLists for create_study page", function () {
        HelpStudies.sampleUser = HelpStudies.sampleUser.replace('123456', '6');

        var spy_getLocationHref = spyOn(window, "getLocationHref").and.returnValue('/create_study.html');
        var spy_getItem = spyOn(sessionStorage, "getItem").and.returnValue(HelpStudies.sampleUser);
        var spy_getStudyMemberLists = spyOn(window, "getStudyMemberLists");

        // id == 6
        getStudyStaticLists();

        expect(spy_getLocationHref).toHaveBeenCalledTimes(2);
        expect(spy_getStudyMemberLists).toHaveBeenCalledTimes(0);

        spy_getItem.and.returnValue(null);

        // user is null
        getStudyStaticLists();

        expect(spy_getStudyMemberLists).toHaveBeenCalledTimes(0);
    });
    it("bails quickly for visit_detail.html", function () {

        var spy_getLocationHref = spyOn(window, "getLocationHref").and.returnValue('/visit_detail.html');
        var spy_getItem = spyOn(sessionStorage, "getItem");
        var spy_getStudyMemberLists = spyOn(window, "getStudyMemberLists");
        var spy_staticListsDataCallback = spyOn(StudyScreen, "staticListsDataCallback");

        getStudyStaticLists();

        expect(spy_getLocationHref).toHaveBeenCalledTimes(1);

        expect(spy_getItem).toHaveBeenCalledTimes(0);
        expect(spy_getStudyMemberLists).toHaveBeenCalledTimes(0);
        expect(spy_staticListsDataCallback).toHaveBeenCalledTimes(0);

    });
    it("works if we pass in a callback fn", function () {

        var spy_get = spyOn($, "get").and.callFake(function (url, callback) {
            var foo = '{ "studyStatuses":[{"name":"IRB_PROCESS","shortName":"PENDING", "isPending": true, "isOpen": false, "isClosed": false, "id":11},{"name":"OPEN","shortName":"OPEN", "isPending": false, "isOpen": true, "isClosed": false,"id":22},{"name":"CLOSED","shortName":"CLOSED", "isPending": false, "isOpen": false, "isClosed": true,"id":33}]}';
            callback(foo);
        });

        var spy_staticListsDataCallback =
            spyOn(StudyScreen, "staticListsDataCallback").and.callThrough();

        var fooFn = function () {
        };
        getStudyStaticLists(fooFn);

        expect(spy_staticListsDataCallback).toHaveBeenCalledTimes(1);
        expect(spy_staticListsDataCallback.calls.argsFor(0)).toContain(fooFn);
    });

});

describe('commonStudyDetailData', function () {

    it("calls several functions unconditionally", function () {

        var spy_commonData = spyOn(window, "commonData");
        var spy_studyModuleRoles = spyOn(window, "studyModuleRoles");
        var spy_renderBreadcrumbs = spyOn(window, "renderBreadcrumbs");
        var spy_study_setFormMode = spyOn(window, "study_setFormMode");
        var spy_loadStudyTab = spyOn(window, "loadStudyTab");

        commonStudyDetailData();

        expect(spy_commonData).toHaveBeenCalledTimes(1);
        expect(spy_studyModuleRoles).toHaveBeenCalledTimes(1);

        expect(spy_renderBreadcrumbs).toHaveBeenCalledTimes(1);
        expect(spy_renderBreadcrumbs.calls.argsFor(0)).toEqual(["study_form_screen"]);

        expect(spy_study_setFormMode).toHaveBeenCalledTimes(1);

        expect(spy_loadStudyTab).toHaveBeenCalledTimes(1);

    });

    it("calls study_setFormMode", function () {

        spyOn(window, "commonData");
        spyOn(window, "studyModuleRoles");
        spyOn(window, "renderBreadcrumbs");
        spyOn(window, "loadStudyTab");

        var spy_getItem = spyOn(sessionStorage, "getItem")
            .and.returnValue('"miscMode"');
        var spy_study_setFormMode = spyOn(window, "study_setFormMode");

        commonStudyDetailData();

        expect(spy_getItem).toHaveBeenCalledTimes(2);
        expect(spy_getItem.calls.argsFor(0)).toEqual(["mode"]);
        expect(spy_getItem.calls.argsFor(1)).toEqual(["previousMode"]);

        expect(spy_study_setFormMode).toHaveBeenCalledTimes(1);
        expect(spy_study_setFormMode.calls.argsFor(0)).toEqual(["miscMode"]);

        spy_getItem.and.returnValue('"edit"');

        commonStudyDetailData();

        // count of calls accumulates
        expect(spy_study_setFormMode).toHaveBeenCalledTimes(2);
        expect(spy_study_setFormMode.calls.argsFor(1)).toEqual(["view"]);

        spy_getItem.and.returnValue('"new"');

        commonStudyDetailData();

        // count of calls accumulates
        expect(spy_study_setFormMode).toHaveBeenCalledTimes(3);
        expect(spy_study_setFormMode.calls.argsFor(2)).toEqual(["view"]);
    });
});

describe('commonVisitDetailData', function () {
    // doesn't work as 'this.getItemHelper = function()...'
    var getItemHelper =
        function (theSpy, tab, mode, previous) {
            // returnValues doesn't work, at least with null and non-nulls both in the list
            theSpy.and
                .callFake(function (key) {
                    // map doesn't work if map['blah'] = null
                    if (key == "selectedTab") {
                        return tab;
                    }
                    else if (key == "previousMode") {
                        return previous;
                    }
                    else if (key == "mode") {
                        return mode;
                    }
                    else {
                        return null;
                    }
                });
        }

    beforeEach(function () {

        this.spy_commonData = spyOn(window, "commonData");
        this.spy_renderBreadcrumbs = spyOn(window, "renderBreadcrumbs");
        this.spy_getItem = spyOn(sessionStorage, "getItem");

        this.spy_study_visit_setFormMode = spyOn(window, "study_visit_setFormMode");
        this.spy_loadVisitTemplateApprovalsTab = spyOn(window, "loadVisitTemplateApprovalsTab");
        this.spy_loadTemplateResources = spyOn(window, "loadTemplateResources");
    });

    it("calls couple functions unconditionally", function () {

        this.spy_getItem.and.returnValue(-1);

        commonVisitDetailData();

        expect(this.spy_commonData).toHaveBeenCalledTimes(1);

        expect(this.spy_renderBreadcrumbs).toHaveBeenCalledTimes(1);
        expect(this.spy_renderBreadcrumbs.calls.argsFor(0)).toEqual(["study_visit_view_form"]);

        expect(this.spy_getItem).toHaveBeenCalledTimes(1);
        expect(this.spy_getItem.calls.argsFor(0)).toEqual(["selectedTab"]);

        expect(this.spy_study_visit_setFormMode).toHaveBeenCalledTimes(0);
        expect(this.spy_loadVisitTemplateApprovalsTab).toHaveBeenCalledTimes(0);
        expect(this.spy_loadTemplateResources).toHaveBeenCalledTimes(0);
    });

    it("null tab, previous=edit calls study_visit_setFormMode", function () {

        getItemHelper(this.spy_getItem, null, null, '"edit"');

        commonVisitDetailData();

        expect(this.spy_getItem).toHaveBeenCalledTimes(3);
        expect(this.spy_getItem.calls.argsFor(0)).toEqual(["selectedTab"]);
        expect(this.spy_getItem.calls.argsFor(1)).toEqual(["mode"]);
        expect(this.spy_getItem.calls.argsFor(2)).toEqual(["previousMode"]);

        expect(this.spy_study_visit_setFormMode).toHaveBeenCalledTimes(1);
        expect(this.spy_study_visit_setFormMode.calls.argsFor(0)).toEqual(["view"]);

        expect(this.spy_loadVisitTemplateApprovalsTab).toHaveBeenCalledTimes(0);
        expect(this.spy_loadTemplateResources).toHaveBeenCalledTimes(0);
    });
    it("zero tab, previous=edit calls study_visit_setFormMode(view)", function () {

        getItemHelper(this.spy_getItem, '0', null, '"edit"');

        commonVisitDetailData();

        expect(this.spy_getItem).toHaveBeenCalledTimes(3);
        expect(this.spy_getItem.calls.argsFor(0)).toEqual(["selectedTab"]);
        expect(this.spy_getItem.calls.argsFor(1)).toEqual(["mode"]);
        expect(this.spy_getItem.calls.argsFor(2)).toEqual(["previousMode"]);

        expect(this.spy_study_visit_setFormMode).toHaveBeenCalledTimes(1);
        expect(this.spy_study_visit_setFormMode.calls.argsFor(0)).toEqual(["view"]);

        expect(this.spy_loadVisitTemplateApprovalsTab).toHaveBeenCalledTimes(0);
        expect(this.spy_loadTemplateResources).toHaveBeenCalledTimes(0);
    });
    it("zero tab, previous=edit, mode=foo calls study_visit_setFormMode(foo)", function () {

        getItemHelper(this.spy_getItem, '0', '"foo"', '"foo"');

        commonVisitDetailData();

        expect(this.spy_getItem).toHaveBeenCalledTimes(3);
        expect(this.spy_getItem.calls.argsFor(0)).toEqual(["selectedTab"]);
        expect(this.spy_getItem.calls.argsFor(1)).toEqual(["mode"]);
        expect(this.spy_getItem.calls.argsFor(2)).toEqual(["previousMode"]);

        expect(this.spy_study_visit_setFormMode).toHaveBeenCalledTimes(1);
        expect(this.spy_study_visit_setFormMode.calls.argsFor(0)).toEqual(["foo"]);

        expect(this.spy_loadVisitTemplateApprovalsTab).toHaveBeenCalledTimes(0);
        expect(this.spy_loadTemplateResources).toHaveBeenCalledTimes(0);
    });
    it("1 tab calls loadVisitTemplateApprovalsTab()", function () {

        getItemHelper(this.spy_getItem, '1');

        commonVisitDetailData();

        expect(this.spy_getItem).toHaveBeenCalledTimes(1);
        expect(this.spy_loadVisitTemplateApprovalsTab).toHaveBeenCalledTimes(1);

        expect(this.spy_study_visit_setFormMode).toHaveBeenCalledTimes(0);
        expect(this.spy_loadTemplateResources).toHaveBeenCalledTimes(0);
    });
    it("2 tab calls loadVisitTemplateApprovalsTab()", function () {

        getItemHelper(this.spy_getItem, '2');

        commonVisitDetailData();

        expect(this.spy_getItem).toHaveBeenCalledTimes(1);
        expect(this.spy_loadTemplateResources).toHaveBeenCalledTimes(1);

        expect(this.spy_loadVisitTemplateApprovalsTab).toHaveBeenCalledTimes(0);
        expect(this.spy_study_visit_setFormMode).toHaveBeenCalledTimes(0);
    });
});

describe('commonVisitApprovalScreenData', function () {

    it('calls couple functions', function () {

        this.spy_commonData = spyOn(window, "commonData");
        this.spy_study_visit_approval = spyOn(window, "study_visit_approval");

        commonVisitApprovalScreenData();

        expect(this.spy_commonData).toHaveBeenCalledTimes(1);
        expect(this.spy_study_visit_approval).toHaveBeenCalledTimes(1);
    });
});

describe('commonCreateStudyData', function () {

    it('calls couple functions', function () {

        this.spy_commonData = spyOn(window, "commonData");
        this.spy_getItem = spyOn(sessionStorage, "getItem").and.returnValue('"foo"');
        this.spy_study_setFormMode = spyOn(window, "study_setFormMode");

        commonCreateStudyData();

        expect(this.spy_commonData).toHaveBeenCalledTimes(1);

        expect(this.spy_getItem).toHaveBeenCalledTimes(1);
        expect(this.spy_getItem.calls.argsFor(0)).toEqual(["mode"]);

        expect(this.spy_study_setFormMode).toHaveBeenCalledTimes(1);
        expect(this.spy_study_setFormMode.calls.argsFor(0)).toEqual(["foo"]);
    });
});

describe('createStudyComboBoxes_onChange', function () {

    var helper = function (theSpyAttr, theSpyDollar, theSpyDispatch,
                           attrRetVal, dispatchParam) {

        // doesn't work to define these spies as 'this.--' and try to use them here
        theSpyAttr.calls.reset();
        theSpyDollar.calls.reset();
        theSpyDispatch.calls.reset();

        theSpyAttr.and.returnValue(attrRetVal);

        StudyScreen.createStudyComboBoxes_onChange();

        expect(theSpyDollar).toHaveBeenCalledTimes(1);
        expect(theSpyAttr).toHaveBeenCalledTimes(1);
        expect(theSpyDispatch).toHaveBeenCalledTimes(1);

        expect(theSpyAttr.calls.argsFor(0)).toEqual(["id"]);

        if (dispatchParam) {
            expect(theSpyDispatch.calls.argsFor(0)).toContain(dispatchParam);
        }
    };

    it('dispatches on attr(id)', function () {

        // for var-iety, using var here rather than 'this.'
        var spy_dollar = spyOn(window, "$").and.callThrough();
        var spy_attr = spyOn($.fn, "attr");

        var spy_displayFundingSourceDetails = spyOn(window, "displayFundingSourceDetails");
        this.spy_changeStudyStatusClick = spyOn(window, "changeStudyStatusClick");

        helper(spy_attr, spy_dollar, spy_displayFundingSourceDetails,
            "study_fundingSource1", 1);

        helper(spy_attr, spy_dollar, spy_displayFundingSourceDetails,
            "study_fundingSource2", 2);

        helper(spy_attr, spy_dollar, spy_displayFundingSourceDetails,
            "study_fundingSource3", 3);

        helper(spy_attr, spy_dollar, spy_displayFundingSourceDetails,
            "study_fundingSource4", 4);

        helper(spy_attr, spy_dollar, this.spy_changeStudyStatusClick,
            "study_status");

    });
});

describe('createStudyComboBoxes', function () {

    it('makes a combobox', function () {

        var spy_createComboBox = spyOn(WidgetUtil, "createComboBox");

        createStudyComboBoxes();

        expect(spy_createComboBox).toHaveBeenCalledTimes(1);

        var argsForCall = spy_createComboBox.calls.argsFor(0);
        expect(argsForCall).toContain(".formSelectInput");

        var optionsObject = argsForCall[1];
        expect(optionsObject.onChange).toBe(StudyScreen.createStudyComboBoxes_onChange);
    });
});

describe('commonCreateVisitData', function () {
    it('call couple of functions', function () {

        var spy_commonData = spyOn(window, "commonData");
        var spy_getItem = spyOn(sessionStorage, "getItem");
        var spy_study_visit_setFormMode = spyOn(window, "study_visit_setFormMode");
        var spy_study_visit_loadEditForm = spyOn(window, "study_visit_loadEditForm");

        spy_getItem.and.returnValue('"foo"');
        commonCreateVisitData();

        expect(spy_commonData).toHaveBeenCalledTimes(1);

        expect(spy_getItem).toHaveBeenCalledTimes(1);
        expect(spy_getItem.calls.argsFor(0)).toEqual(["mode"]);

        expect(spy_study_visit_setFormMode).toHaveBeenCalledTimes(1);
        expect(spy_study_visit_setFormMode.calls.argsFor(0)).toEqual(["foo"]);

        expect(spy_study_visit_loadEditForm).not.toHaveBeenCalled();

        spy_getItem.and.returnValue('"edit"');
        commonCreateVisitData();

        expect(spy_commonData).toHaveBeenCalledTimes(2);

        expect(spy_getItem).toHaveBeenCalledTimes(2);
        expect(spy_getItem.calls.argsFor(1)).toEqual(["mode"]);

        expect(spy_study_visit_setFormMode).toHaveBeenCalledTimes(2);
        expect(spy_study_visit_setFormMode.calls.argsFor(1)).toEqual(["edit"]);
    });
});

describe('addNewStudyClick', function () {

    it('sets mode and href', function () {
        var spy_setItem = spyOn(sessionStorage, "setItem");
        var spy_setLocationHref = spyOn(window, "setLocationHref");

        addNewStudyClick();

        expect(spy_setItem).toHaveBeenCalledTimes(1);
        expect(spy_setItem.calls.argsFor(0)).toEqual(["mode", '"new"']);

        expect(spy_setLocationHref).toHaveBeenCalledTimes(1);
        expect(spy_setLocationHref.calls.argsFor(0)).toEqual(["create_study.html"]);
    });
});

describe('addNewVisitClick', function () {

    it('sets mode and href', function () {
        var spy_setItem = spyOn(sessionStorage, "setItem");
        var spy_setLocationHref = spyOn(window, "setLocationHref");

        addNewVisitClick();

        expect(spy_setItem).toHaveBeenCalledTimes(2);
        expect(spy_setItem.calls.argsFor(0)).toEqual(["mode", '"new"']);
        expect(spy_setItem.calls.argsFor(1)).toEqual(["previousMode", '"new"']);

        expect(spy_setLocationHref).toHaveBeenCalledTimes(1);
        expect(spy_setLocationHref.calls.argsFor(0)).toEqual(["create_visit.html"]);
    });
});

describe('newVisitApprovalClick', function () {

    it('calls showNewVisitApprovalForm()', function () {
        var spy_showNewVisitApprovalForm = spyOn(window, "showNewVisitApprovalForm");

        newVisitApprovalClick();

        expect(showNewVisitApprovalForm).toHaveBeenCalledTimes(1);
    });
});

describe('study_visit_approval', function () {
    it('calls couple functions and css-es a few elements', function () {

        var spy_renderBreadcrumbs = spyOn(window, "renderBreadcrumbs");
        var spy_dollar = spyOn(window, "$").and.callThrough();
        var spy_css = spyOn($.fn, "css");
        var spy_study_visit_approval_clearForm = spyOn(window, "study_visit_approval_clearForm");
        var commentBox = spyOn(WidgetUtil, "commentBox");

        study_visit_approval();

        expect(spy_renderBreadcrumbs).toHaveBeenCalledTimes(1);
        expect(spy_renderBreadcrumbs.calls.argsFor(0)).toEqual(['study_visit_approval_form_screen']);

        expect(spy_dollar).toHaveBeenCalledTimes(6);
        expect(spy_css).toHaveBeenCalledTimes(5);

        var index = 0;
        expect(spy_dollar.calls.argsFor(index++)).toEqual(['.study_confirmFormItem']);
        expect(spy_dollar.calls.argsFor(index++)).toEqual(['.formTextReadOnly']);
        expect(spy_dollar.calls.argsFor(index++)).toEqual(['#study_visit_form_buttons']);
        expect(spy_dollar.calls.argsFor(index++)).toEqual(['.formTextAreaSmaller']);
        expect(spy_dollar.calls.argsFor(index++)).toEqual(['#study_confirmFormItem']);

        index = 0;
        expect(spy_css.calls.argsFor(index++)).toEqual([{visibility: "visible"}]);
        expect(spy_css.calls.argsFor(index++)).toEqual([{display: "visible"}]);
        expect(spy_css.calls.argsFor(index++)).toEqual([{visibility: "visible"}]);
        expect(spy_css.calls.argsFor(index++)).toEqual([{display: "block"}]);
        expect(spy_css.calls.argsFor(index++)).toEqual([{visibility: "visible"}]);

        expect(spy_study_visit_approval_clearForm).toHaveBeenCalledTimes(1);

        expect(commentBox).toHaveBeenCalledTimes(1);
    });
});

describe('study_visit_approval_clearForm', function () {

    it('calls a few functions and mutates the DOM a bit', function () {

        var DATE = '1/2/2003 2:54 pm';
        var FORMATTED_DATE = '01/02/2003 14:54';
        var spy_getItem = spyOn(sessionStorage, "getItem").and.callFake(
            function (key) {
                // map doesn't work if map['blah'] = null
                if (key == "studyData") {
                    return '{"localId":"localId1", "name":"name1"}';
                }
                else if (key == "visitData") {
                    return '{"visitName":"visit1", "createdDate":"' + DATE + '", "approved": "Approved"}';
                }
                else if (key == "userData") {
                    return '{"ecommonsId":"ecommonsId1"}';
                }
                else {
                    return null;
                }
            });

        var spy_dollar = spyOn(window, "$").and.callThrough();
        var spy_html = spyOn($.fn, "html");
        var spy_text = spyOn($.fn, "text");

        var spy_commentBox = spyOn(WidgetUtil.commentBox, "clearValue");

        study_visit_approval_clearForm();

        expect(spy_getItem).toHaveBeenCalledTimes(3);
        expect(spy_dollar).toHaveBeenCalledTimes(6);

        expect(spy_html).toHaveBeenCalledTimes(1);
        expect(spy_text).toHaveBeenCalledTimes(4);

        var index = 0;
        expect(spy_getItem.calls.argsFor(index++)).toEqual(['studyData']);
        expect(spy_getItem.calls.argsFor(index++)).toEqual(['visitData']);
        expect(spy_getItem.calls.argsFor(index++)).toEqual(['userData']);

        index = 0;
        expect(spy_dollar.calls.argsFor(index++)).toEqual(['#study_approval_study_name']);
        expect(spy_dollar.calls.argsFor(index++)).toEqual(['#study_approval_visit_name']);
        expect(spy_dollar.calls.argsFor(index++)).toEqual(['#study_visitApprovalUser']);
        expect(spy_dollar.calls.argsFor(index++)).toEqual(['#study_visitApprovalCreated']);
        expect(spy_dollar.calls.argsFor(index++)).toEqual(['#visit_finalApprovalStatus']);
        expect(spy_dollar.calls.argsFor(index++)).toEqual(['#study_visitApprovalComment']);

        expect(spy_html.calls.argsFor(0)).toEqual([
            '(localId1)' +
            '<div style="padding: 3px;"></div>' +
            'name1'
        ]);

        index = 0;
        expect(spy_text.calls.argsFor(index++)).toEqual(['visit1']);
        expect(spy_text.calls.argsFor(index++)).toEqual(['ecommonsId1']);
        expect(spy_text.calls.argsFor(index++)).toEqual([FORMATTED_DATE]);
        expect(spy_text.calls.argsFor(index++)).toEqual(['Approved']);

        expect(spy_commentBox).toHaveBeenCalledTimes(1);
    });

    it('displays study_visitApprovalCreated date-time in military format', function () {

        FixtureHelper.loadTestHtmlFixtureIntoDom('studies/html/elements-for-testing-study_visit_approval_clearForm.html');

        var STUDY_NAME = "study name";
        var VISIT_NAME = "visit1";
        var USER_ECOMMONS_ID = "user\'s eCommons id";
        var VISIT_CREATED_DATE_PRE_FORMAT = "03/14/2017 1:03 pm";
        var VISIT_CREATED_DATE_POST_FORMAT = "03/14/2017 13:03";
        var FINAL_APPROVAL = "final approval text";

        expect($("#study_visitApprovalCreated").html()).toBe("");

        sessionStorage.setItem("studyData", '{' +
            '"localId":"local study ID",' +
            '"name": "' + STUDY_NAME + '"' +
            '}');
        sessionStorage.setItem("visitData", '{' +
            '"visitName": "' + VISIT_NAME + '",' +
            '"createdDate":"' + VISIT_CREATED_DATE_PRE_FORMAT + '",' +
            '"approved":"' + FINAL_APPROVAL + '"' +
            '}');
        sessionStorage.setItem("userData", '{' +
            '"ecommonsId": "' + USER_ECOMMONS_ID + '"' +
            '}');

        WidgetUtil.commentBox("#study_visitApprovalComment");
        expect(WidgetUtil.commentBox.getValue("#study_visitApprovalComment")).toBe('');
        expect($("#study_approval_study_name").html()).toBe('');
        expect($("#study_approval_visit_name").html()).toBe('');
        expect($("#study_visitApprovalUser").html()).toBe('');
        expect($("#study_visitApprovalCreated").html()).toBe('');
        expect($("#visit_finalApprovalStatus").html()).toBe('');

        study_visit_approval_clearForm();

        // expect($("#study_visitApprovalCreated").html()).toBe("03/14/2017 13:03");

        expect($("#study_approval_study_name").html()).toBe('(' + app_selectedStudy.localId + ')' + '<div style="padding: 3px;"></div>' + STUDY_NAME);
        expect($("#study_approval_visit_name").html()).toBe(VISIT_NAME);
        expect($("#study_visitApprovalUser").html()).toBe(USER_ECOMMONS_ID);
        expect($("#study_visitApprovalCreated").html()).toBe(VISIT_CREATED_DATE_POST_FORMAT);
        expect($("#visit_finalApprovalStatus").html()).toBe(FINAL_APPROVAL);
        expect(WidgetUtil.commentBox.getValue("#study_visitApprovalComment")).toBe('');


    })
});

describe('showVisitDetailCallback', function () {

    it('sets some session items and location', function () {
        var spy_setItem = spyOn(sessionStorage, "setItem");
        var spy_setLocationHref = spyOn(window, "setLocationHref");

        StudyScreen.showVisitDetailCallback('data');

        expect(spy_setItem).toHaveBeenCalledTimes(3);
        expect(spy_setItem.calls.argsFor(0)).toEqual(["visitData", '"data"']);
        expect(spy_setItem.calls.argsFor(1)).toEqual(["selectedTab", 0]);
        expect(spy_setItem.calls.argsFor(2)).toEqual(["mode", '"view"']);

        expect(spy_setLocationHref).toHaveBeenCalledTimes(1);
        expect(spy_setLocationHref.calls.argsFor(0)).toEqual(["visit_detail.html"]);
    });
});

describe('showVisitDetail', function () {
    it('calls getJSON', function () {
        var spy_getJSON = spyOn($, "getJSON");

        showVisitDetail(42);

        expect(spy_getJSON).toHaveBeenCalledTimes(1);
        expect(spy_getJSON.calls.argsFor(0)).toEqual(
            ["rest/study/getVisitTemplateData?visit=42", StudyScreen.showVisitDetailCallback]);
    });
});

describe('showNewVisitApprovalForm', function () {
    it('calls a few functions and mutates the DOM a bit', function () {
        var spy_setLocationHref = spyOn(window, "setLocationHref");
        var spy_study_visit_approval = spyOn(window, "study_visit_approval");

        showNewVisitApprovalForm();

        expect(spy_setLocationHref).toHaveBeenCalledTimes(1);
        expect(spy_setLocationHref.calls.argsFor(0)).toEqual(["visit_approval.html"]);

        expect(spy_study_visit_approval).toHaveBeenCalledTimes(1);
    });
});

describe('newStudySubjectClick', function () {
    it('sets an item in storage', function () {
        var spy_setItem = spyOn(sessionStorage, "setItem");
        var spy_subjectModule = spyOn(window, "subjectModule");

        app_selectedStudy.id = 42;
        newStudySubjectClick();

        expect(spy_setItem).toHaveBeenCalledTimes(1);
        expect(spy_setItem.calls.argsFor(0)).toEqual(["selectedStudyIdForAddingMember", 42]);

        expect(spy_subjectModule).toHaveBeenCalledTimes(1);
    });
});


describe('newStudyMemberClick', function () {
    it('changes href', function () {
        var spy_setLocationHref = spyOn(window, "setLocationHref");

        newStudyMemberClick();

        expect(spy_setLocationHref).toHaveBeenCalledTimes(1);
        expect(spy_setLocationHref.calls.argsFor(0)).toEqual(["study_member.html"]);
    });
});


describe('editStudyClick', function () {
    it('sets an item in storage and changes href', function () {
        var spy_setItem = spyOn(sessionStorage, "setItem");
        var spy_setLocationHref = spyOn(window, "setLocationHref");

        editStudyClick();

        expect(spy_setItem).toHaveBeenCalledTimes(1);
        expect(spy_setItem.calls.argsFor(0)).toEqual(["mode", '"edit"']);

        expect(spy_setLocationHref).toHaveBeenCalledTimes(1);
        expect(spy_setLocationHref.calls.argsFor(0)).toEqual(["create_study.html"]);
    });
});

describe('editVisitClick', function () {
    it('sets an item in storage and changes href', function () {
        var spy_setItem = spyOn(sessionStorage, "setItem");
        var spy_setLocationHref = spyOn(window, "setLocationHref");

        editVisitClick();

        expect(spy_setItem).toHaveBeenCalledTimes(1);
        expect(spy_setItem.calls.argsFor(0)).toEqual(["mode", '"edit"']);

        expect(spy_setLocationHref).toHaveBeenCalledTimes(1);
        expect(spy_setLocationHref.calls.argsFor(0)).toEqual(["create_visit.html"]);
    });
});

describe('crudStudyClick', function () {
    it('calls processStudyForm', function () {
        var spy_processStudyForm = spyOn(window, "processStudyForm");
        var spy_dollarblockUI = spyOn($, "blockUI");
        var spy_dollarunblockUI = spyOn($, "unblockUI");

        crudStudyClick();
        expect(spy_processStudyForm).toHaveBeenCalledTimes(1);
        expect(spy_dollarblockUI).toHaveBeenCalledTimes(1);
        expect(spy_dollarunblockUI).toHaveBeenCalledTimes(1);
    });
});

describe('restoreStudyClick', function () {
    it('calls editStudyClick', function () {
        var spy_editStudyClick = spyOn(window, "editStudyClick");

        restoreStudyClick();

        expect(spy_editStudyClick).toHaveBeenCalledTimes(1);
    });
});

describe('cancelStudyClick', function () {
    it('calls a few functions', function () {

        // load the home page into the DOM
        // NOTE-XH: I experienced transient failures with $('#study_form')
        // not being found
        FixtureHelper.loadSourceHtmlFixtureIntoDom('create_study.html');

        var spy_dollar = spyOn(window, "$").and.callThrough();
        var spy_reset = spyOn($('#study_form')[0], "reset");
        var spy_getItem = spyOn(sessionStorage, "getItem");

        var spy_study_clearErrors = spyOn(window, "study_clearErrors");
        var spy_studyShowSubjectsTab = spyOn(window, "studyShowSubjectsTab");
        var spy_studyModule = spyOn(window, "studyModule");
        var spy_showStudyDetail = spyOn(window, 'showStudyDetail');

        spy_getItem.and.returnValue('"foo"');

        spy_dollar.calls.reset();
        cancelStudyClick();

        expect(spy_dollar).toHaveBeenCalledTimes(1);
        expect(spy_dollar.calls.argsFor(0)).toEqual(['#study_form']);

        expect(spy_study_clearErrors).toHaveBeenCalledTimes(1);
        expect(spy_reset).toHaveBeenCalledTimes(1);

        expect(spy_getItem).toHaveBeenCalledTimes(1);
        expect(spy_getItem.calls.argsFor(0)).toEqual(['mode']);

        expect(spy_studyShowSubjectsTab).toHaveBeenCalledTimes(0);
        expect(spy_studyModule).toHaveBeenCalledTimes(1);

        spy_getItem.and.returnValue('"edit"');

        spy_studyShowSubjectsTab.calls.reset();
        spy_studyModule.calls.reset();
        cancelStudyClick();

        expect(spy_showStudyDetail).toHaveBeenCalledTimes(1);
        expect(spy_studyShowSubjectsTab).toHaveBeenCalledTimes(0);
        expect(spy_studyModule).toHaveBeenCalledTimes(0);

    });
});

describe('studyShowSubjectsTab', function () {
    it('calls studyReadData and mutates the session a bit', function () {

        var spy_setItem = spyOn(sessionStorage, "setItem");
        var spy_studyReadData = spyOn(window, "studyReadData");

        studyShowSubjectsTab();

        expect(spy_setItem).toHaveBeenCalledTimes(1);
        expect(spy_setItem.calls.argsFor(0)).toEqual(["selectedStudyTab", 2]);

        expect(spy_studyReadData).toHaveBeenCalledTimes(1);
        expect(spy_studyReadData.calls.argsFor(0)).toEqual(["StudySubjects"]);
    });
});

describe('studyReadData', function () {
    it('calls setLocationHref and mutates the session a bit', function () {

        var spy_setItem = spyOn(sessionStorage, "setItem");
        var spy_setLocationHref = spyOn(window, "setLocationHref");

        studyReadData();

        expect(spy_setItem).toHaveBeenCalledTimes(2);
        expect(spy_setItem.calls.argsFor(0)).toEqual(["mode", '"view"']);
        expect(spy_setItem.calls.argsFor(1)).toEqual(["selectedStudyTab", 0]);

        expect(spy_setLocationHref).toHaveBeenCalledTimes(1);
        expect(spy_setLocationHref.calls.argsFor(0)).toEqual(["study_detail.html#StudyDetail"]);

        spy_setItem.calls.reset();
        spy_setLocationHref.calls.reset();
        studyReadData("foo");

        expect(spy_setItem).toHaveBeenCalledTimes(1);
        expect(spy_setItem.calls.argsFor(0)).toEqual(["mode", '"view"']);

        expect(spy_setLocationHref).toHaveBeenCalledTimes(1);
        expect(spy_setLocationHref.calls.argsFor(0)).toEqual(["study_detail.html#foo"]);
    });
});

describe('loadStudyTab', function () {
    it('calls setLocationHref', function () {
        var spy_setLocationHref = spyOn(window, "setLocationHref");

        loadStudyTab();

        expect(spy_setLocationHref).toHaveBeenCalledTimes(1);
        expect(spy_setLocationHref.calls.argsFor(0)).toEqual(["study_detail.html#StudyDetail"]);
    });
});

describe('goToStudySubjectForm', function () {
    it('calls setLocationHref and mutates the session a bit', function () {

        var spy_setItem = spyOn(sessionStorage, "setItem");
        var spy_setLocationHref = spyOn(window, "setLocationHref");

        isEpicMode = true;
        goToStudySubjectForm("id", "mrn", "inst");

        expect(spy_setItem).toHaveBeenCalledTimes(3);
        expect(spy_setItem.calls.argsFor(0)).toEqual(["selectedSubjectId", "id"]);
        expect(spy_setItem.calls.argsFor(1)).toEqual(["selectedSubjectMrn", "mrn"]);
        expect(spy_setItem.calls.argsFor(2)).toEqual(["selectedSubjectInstitution", "inst"]);

        expect(spy_setLocationHref).toHaveBeenCalledTimes(1);
        expect(spy_setLocationHref.calls.argsFor(0)).toEqual(["subject_details.html?mode=view"]);

        isEpicMode = false;
        spy_setItem.calls.reset();
        goToStudySubjectForm("id", "mrn", "inst");

        expect(spy_setItem).toHaveBeenCalledTimes(3);
        expect(spy_setItem.calls.argsFor(0)).toEqual(["selectedSubjectId", 'id']);
    });
});


describe('Studies Creation/Editing Page', function () {

    it('returns to the view-mode of the same edit when study editing form is cancelled', function () {

        var preExistingMode = sessionStorage.getItem('mode');
        var preExistingStudy = app_selectedStudy;

        sessionStorage.setItem("mode", '"edit"');
        app_selectedStudy = {id: 99};

        spyOn(window, 'study_clearErrors');
        spyOn(window, 'showStudyDetail');
        spyOn(window, 'studyModule');

        cancelStudyClick();

        expect(window.study_clearErrors).toHaveBeenCalledTimes(1);
        expect(window.showStudyDetail).toHaveBeenCalledTimes(1);
        expect(window.showStudyDetail).toHaveBeenCalledWith(99);
        expect(window.studyModule).not.toHaveBeenCalled();

        sessionStorage.setItem("mode", preExistingMode);
        sessionStorage.setItem(app_selectedStudy, preExistingStudy);

    });

});
