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
 * This javascript file is not intended to be executed.
 * It has the js extension so that IDEs can perform syntax-highlighting
 * on it.
 *
 * ======================================================
 *  FRONT-END TEST COOKBOOK FOR SCHEDULER 3.7 AND BEYOND
 * ======================================================
 *
 * We use the following libraries to write front-end unit tests:
 * - jasmine (https://jasmine.github.io/)
 * - sinon (http://sinonjs.org/)
 * - html2js (karma preprocessor, https://github.com/karma-runner/karma-html2js-preprocessor)
 *
 * Some of the sinon and html2js functionality is abstracted out into helper functions found in:
 * - AjaxHelper.js
 * - FixtureHelper.js
 *
 */

// A typical front-end test would have the following structure:

describe('My page', function () {

    it("does something", function () {

        // load the page's HTML content into the DOM

        FixtureHelper.loadSourceHtmlFixtureIntoDom('my_page.html');

        // set up a fake server to receive and respond to AJAX calls

        var server = UtilHelper.getFakeServer();

        // load a json file's content as a string into a variable (the JSON does not get parsed)

        var responseString = FixtureHelper.getTestFixtureContent('json_fixtures/my_response.json');

        // set up a fake AJAX response for the given REST url

        server.respondWith("GET", "my_rest_call_URL", [200, {"Content-Type": "text/text"}, responseString]);

        // The following helper function provides a short cut for setting up a fake AJAX response in one line:

        AjaxHelper.setupFakeServerTextResponseFromTestFixture(server, url, 'json_fixtures/my_response.json');

        // If we need to load and parse a JSON file's content into a variable that we can then use in our
        // tests, for instance as expected data, we can do the following:

        var myDataObject = FixtureHelper.getTestJsonFixture('json_fixtures/my_expectations.json');

        // With Sinon we can set up a spy (aka a test double) to verify whether a certain
        // function gets called (or potentially we can mock its behavior):

        var mySinonSpy = sinon.spy(anObjectWithMemberFunctions, "myFunction");

        // Note that when sinon spies on a function, that function will still be called through
        // by the spy function

        // In the above case we are spying on anObjectWithMemberFunctions.myFunction()
        // If the function is global then the spy would be set up as follows:

        var mySinonSpy = sinon.spy(window, "myFunction");

        // One can also set up spies using the jasmine library.
        // The following spy will record how many times the spied function was called,
        // and with which arguments. The spied function wll NOT be executed (as opposed
        // to Sinon spies which do call-through)

        var myJasmineSpy = spyOn(window, "myOtherFunction2");

        // If we want the spied method to be called-through:

        var myJasmineSpy = spyOn(window, "myOtherFunction2").and.callThrough();

        // Now we can invoke the function under test. AJAX calls will be made but the success/failure
        // callbacks will not be called yet

        myFunctionUnderTest();

        // Here we let the AJAX callbacks be called; sinon will pass to the AJAX callbacks
        // the fake AJAX responses that were set up above with:

        server.respond();

        // If there are multiple chained AJAX calls we may have to call server.respond() more than once

        // Now we can verify that some functions were called, using the spies
        // that we set up earlier:

        sinon.assert.calledOnce(mySinonSpy);

        // We can also verify function calls to the jasmine spy:

        expect(myJasmineSpy).toHaveBeenCalledTimes(1);

        // We can verify the argument values that the method was called with (in this case
        // we assume the function in question has two argument):

        expect(mySinonSpy.calls.argsFor(0)).toEqual(["my first argument value", "my second argument value"]);

        // and of course we can check the state pf anything that's in the global scope
        // That includes the jQuery object $ and everything that comes with it.
        // Therefore we can verify the state of the DOM:

        expect($(".my-class").length).toBe(5);
        expect($("#my-element").html).toBe("Hello World!");

    });

});

// the tests below will not be executed. xdescribe() is a no-op. It makes it
// easier to skip some of the tests, without having to comment them out.
// Also, skipped tests get reported in the unit test execution info, whereas
// commented out tests do not.
xdescribe('My other page', function () {

});

// Same as above, tests are skipped, but at a more granular level:
describe('My other page', function () {

    xit("is skipped", function () {

    });

    it("is not skipped", function () {

    });

});
