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
/**
 * Created by jill on 9/29/16.
 */

describe('getSubjectStaticLists', function() {

    it("sets up role selection options", function () {

        var server = UtilHelper.getFakeServer();

        // NOTE: the return type is not application/json because otherwise the
        // getStaticLists() function would fail (it wants to parse the incoming
        // String and build a JSON object itself.

        AjaxHelper.setupFakeServerTextResponseFromTestFixture(server, "rest/app/getStaticLists", "global/json/static-lists.json");

        var successCallback = sinon.spy();

        // state before ajax call
        expect(stateSelectOptions).not.toBeDefined();


        // payload
        getSubjectStaticLists(successCallback);
        //getSubjectStaticLists();

        // this is where things that were asynchronous came to be synchronous
        server.respond();

        // state after ajax call:
        // 1. verify that the stub-of-a-callback provided by us was called
        sinon.assert.calledOnce(successCallback);
        // 2. verify that the function did populate roleSelectOptions
        expect(stateSelectOptions).toEqual(
            "<option value=''>choose</option><option value='1'>Massachusetts</option><option value='2'>Alabama</option><option value='3'>Alaska</option><option value='4'>Arizona</option><option value='5'>Arkansas</option><option value='6'>California</option><option value='7'>Colorado</option><option value='8'>Connecticut</option><option value='9'>Delaware</option><option value='10'>Florida</option><option value='11'>Georgia</option><option value='12'>Hawaii</option><option value='13'>Idaho</option><option value='14'>Illinois</option><option value='15'>Indiana</option><option value='16'>Iowa</option><option value='17'>Kansas</option><option value='18'>Kentucky</option><option value='19'>Louisiana</option><option value='20'>Maine</option><option value='21'>Maryland</option><option value='22'>Michigan</option><option value='23'>Minnesota</option><option value='24'>Mississippi</option><option value='25'>Missouri</option><option value='26'>Montana</option><option value='27'>Nebraska</option><option value='28'>Nevada</option><option value='29'>New Hampshire</option><option value='30'>New Jersey</option><option value='31'>New Mexico</option><option value='32'>New York</option><option value='33'>North Carolina</option><option value='34'>North Dakota</option><option value='35'>Ohio</option><option value='36'>Oklahoma</option><option value='37'>Oregon</option><option value='38'>Pennsylvania</option><option value='39'>Rhode Island</option><option value='40'>South Carolina</option><option value='41'>South Dakota</option><option value='42'>Tennessee</option><option value='43'>Texas</option><option value='44'>Utah</option><option value='45'>Vermont</option><option value='46'>Virginia</option><option value='47'>Washington</option><option value='48'>West Virginia</option><option value='49'>Wisconsin</option><option value='50'>Wyoming</option><option value='51'>American Samoa</option><option value='52'>District of Columbia</option><option value='53'>Federated States of Micronesia</option><option value='54'>Guam</option><option value='55'>Marshall Islands</option><option value='56'>Northern Mariana Islands</option><option value='57'>Palau</option><option value='58'>Puerto Rico</option><option value='59'>Virgin Islands</option>"
        );

    });

});

describe('shouldLoadResourceData', function() {

    it('knows the pages for which to load resource data', function () {

        var urls = ["https://localhost:10443/scheduler/appt_batch.html",
            "https://localhost:10443/scheduler/home.html",
            "https://brick.catalyst.com/scheduler/home.html#",
            "https://localhost:10443/scheduler/home.html#",
            "https://localhost:10443/scheduler/home.html#homeCalendar"];

        urls.forEach(function (url) {
            expect(shouldLoadResourceData(url)).toEqual(true);
        });

        expect(shouldLoadResourceData()).toEqual(false);

        var other = "https://brick.catalyst.com/scheduler/home.html#other";
        expect(shouldLoadResourceData(other)).toEqual(false);
    });

});

describe('booleanToStatusValue', function() {

    it('returns the status label from a boolean argument', function () {

        expect(booleanToStatusValue()).toEqual("");
        expect(booleanToStatusValue(true)).toEqual("Active");
        expect(booleanToStatusValue(false)).toEqual("Not Active");

        var ack;
        expect(booleanToStatusValue(ack)).toEqual("");
        ack = true;
        expect(booleanToStatusValue(ack)).toEqual("Active");

        ack = false;
        expect(booleanToStatusValue(ack)).toEqual("Not Active");

    });

});

describe('showValue', function() {

    it('returns the passed value if it is truthy, otherwise returns empty string', function () {
        expect(showValue("foo")).toEqual("foo");
        expect(showValue()).toEqual("");
        var ack;
        expect(showValue(ack)).toEqual("");

        ack = "bar";
        expect(showValue(ack)).toEqual("bar");

        ack = null;
        expect(showValue(ack)).toEqual("");
        expect(showValue(null)).toEqual("");

        expect(showValue(55)).toEqual(55);
    });

});

describe('makeExtraArgStringIfPresent', function() {

    it('concatenates non-empty argument to comma', function () {
        expect(makeExtraArgStringIfPresent("foo")).toEqual(",foo");
        expect(makeExtraArgStringIfPresent()).toEqual("");
        expect(makeExtraArgStringIfPresent("")).toEqual("");
    });

});

describe('yesNoValue', function() {

    it('returns "Yes" for a truthy argument', function(){

        expect(yesNoValue(true)).toEqual("Yes");
        var ack = true;
        expect(yesNoValue(ack)).toEqual("Yes");

    });

    it('returns "" for an undefined argument', function() {

        expect(yesNoValue()).toEqual("");
        var ack;
        expect(yesNoValue(ack)).toEqual("");

    })

    it('returns "No" for a falsy argument', function() {

        var ack = null;
        expect(yesNoValue(ack)).toEqual("No");
        expect(yesNoValue(null)).toEqual("No");
        expect(yesNoValue(55)).toEqual("No");

    })

})

describe('buildFullName', function() {

    it('returns "" for falsy arguments', function () {

        var name;
        expect(buildFullName(name)).toEqual("");
        expect(buildFullName(null)).toEqual("");

        name = {};

        expect(buildFullName(name)).toEqual("");

    });

    it('can build a full name with or without a middle name', function () {

        var name = {
            firstName: "Jon",
            lastName: "Snow"
        };

        expect(buildFullName(name)).toEqual("Jon Snow");

        name = {
            firstName: "Jon",
            middleName: "Snow",
            lastName: "Targaryen"
        };

        expect(buildFullName(name)).toEqual("Jon Snow Targaryen");

    });

});

describe('getNonClosedStudiesList', function() {

    it('loads the list of studies from the server and truncates study names to 50 characters', function() {

        var server = UtilHelper.getFakeServer();

        var url = "rest/study/getNonClosedStudiesList";
        var responseString = FixtureHelper.getTestFixtureContent('global/json/studies-list.json');
        var studyData = JSON.parse(responseString);
        var expectedStudy1TruncatedLabel = studyData.studys[0].localId + ' - ' + studyData.studys[0].name.substring(0, 50);
        AjaxHelper.setupFakeServerTextResponse(server, url, responseString);

        var successCallback = sinon.spy();
        getNonClosedStudiesList(successCallback);

        server.respond();

        sinon.assert.calledOnce(successCallback);
        
        expect(studies.length).toEqual(5);

        var study1TruncatedLabel = studies[0].truncatedLabel;
        expect(study1TruncatedLabel).toEqual(expectedStudy1TruncatedLabel);
    });

});

describe('caseInsensitiveContains', function() {

    it('returns true when the search string is found in the first argument, regardless of case', function() {

        var text = "Lorem ipsum dolor sit amet, consectetur adipiscing elit.";
        var searchString = "DOLOR";

        var result = caseInsensitiveContains(text, searchString);
        expect(result).toBe(true);

        searchString = "foo";
        result = caseInsensitiveContains(text, searchString);
        expect(result).toBe(false);

        var text = "FOOBAR";
        searchString = "bar";
        result = caseInsensitiveContains(text, searchString);
        expect(result).toBe(true);

    });

});
describe('initFooter', function() {

    it('loads and erases', function() {

        var spy_dollar = spyOn(window, "$").and.callThrough().and.returnValues('foo', 'bar');
        var spy_loadAndEraseLicense = spyOn(window, "loadAndEraseLicense");

        initFooter();

        expect(spy_dollar).toHaveBeenCalledTimes(2);
        expect(spy_dollar.calls.argsFor(0)).toEqual(['#main_dialog_wrapper']);
        expect(spy_dollar.calls.argsFor(1)).toEqual(['.footer']);

        expect(spy_loadAndEraseLicense).toHaveBeenCalledTimes(2);
        expect(spy_loadAndEraseLicense.calls.argsFor(0)[0]).toEqual('foo');
        expect(spy_loadAndEraseLicense.calls.argsFor(0)[1]).toEqual("main_dialog_wrapper.html");
        expect(typeof spy_loadAndEraseLicense.calls.argsFor(0)[2]).toBe("function"); // it's an anonymous function
        expect(spy_loadAndEraseLicense.calls.argsFor(1)).toEqual(['bar', "footer.html", setVersionData]);
    });

});
describe('UserRoleUtil', function() {

    it('userIsSuperAdmin reflects user role', function() {

        var spy_getUser = spyOn(UserRoleUtil, "getUser")
            .and.returnValue({institutionRole: {id: SUPER_ADMIN}});

        var result = UserRoleUtil.userIsSuperAdmin();

        expect(spy_getUser).toHaveBeenCalledTimes(1);
        expect(result).toEqual(true);

        spy_getUser.calls.reset();
        spy_getUser.and.returnValue({institutionRole: {id: RESOURCE_MANAGER}});
        result = UserRoleUtil.userIsSuperAdmin();

        expect(spy_getUser).toHaveBeenCalledTimes(1);
        expect(result).toEqual(false);
    });

    it('getUser gets user', function() {

        var fooUser = {institutionRole: {id: 'foo'}};
        var fooJson = JSON.stringify(fooUser);

        var spy_parse = spyOn(JSON, "parse").and.callThrough();
        var spy_getItem = spyOn(sessionStorage, "getItem")
            .and.returnValue(fooJson);

        var result = UserRoleUtil.getUser();

        expect(spy_getItem).toHaveBeenCalledTimes(1);
        expect(spy_getItem.calls.argsFor(0)).toEqual(['userData']);

        expect(spy_parse).toHaveBeenCalledTimes(1);
        expect(spy_parse.calls.argsFor(0)).toEqual([fooJson]);

        expect(result).toEqual(fooUser);
    });

});
