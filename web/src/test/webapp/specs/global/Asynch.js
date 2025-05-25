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

// TODO: we should re-write the describe() and it() arguments to make sentences
xdescribe("Asynch: wait for done", function() {
    var x=0;
    jasmine.DEFAULT_TIMEOUT_INTERVAL = 12000;

    it("part 1", function(done) {
        setTimeout(function() {
            x++;
            done();
        }, 6000);

    });

    it("part 2", function() {
        expect(x).toBe(1);
    });
});

describe("Asynch: github solution, tweaked to work here", function() {
    var server;
    var count=0;

    beforeEach(function () {
        server = UtilHelper.getFakeServer();
    });

    afterEach(function () {
        server.restore();
    });

    it("pt 1. reworked: https://github.com/sinonjs/sinon/issues/671 , http://jsfiddle.net/w4yfyf2x/1/", function(done) {
        server.respondWith('GET', 'foo', [200, { 'Content-Type': 'application/json' }, '{ "foo": 1 }' ]);
        server.respondWith('GET', 'bar', [200, { 'Content-Type': 'application/json' }, '{ "bar": 1 }' ]);

        $.get('foo', function(){
            count++;
            expect(count).toBe(1);
            $.get('bar', function(){
                count++;
                expect(count).toBe(2);
                done();
            });

        });
    });
    it("pt 2. reworked: https://github.com/sinonjs/sinon/issues/671 , http://jsfiddle.net/w4yfyf2x/1/", function() {
        expect(count).toBe(2);
    });

});

var fooCount = 0;

function testFoo() {
    realFoo();
}
function realFoo() {
    fooCount++;
}

describe("Asynch call through and fake", function() {
    it("try it", function() {
        ctcfFirstTime = true;
        spyOn(window, "realFoo").and.callFake(function () {
            window.realFoo.and.callThrough();
            testFoo();

            fooCount += 41;
        });

        testFoo();

        expect(fooCount).toBe(42);
    });
});


describe("Asynch: stateSelectOptions", function() {
    jasmine.DEFAULT_TIMEOUT_INTERVAL = 12000;

    beforeAll(function () {
        this.server = UtilHelper.getFakeServer();
    });

    beforeEach(function () {

        var dataString = FixtureHelper.getTestFixtureContent('global/json/static-lists.json');
        this.server.respondWith("GET", "rest/app/getStaticLists",
            [200,
                { "Content-Type": "text/text" },
                dataString
            ]);
    });

    afterAll(function () {
        this.server.restore();
    });

    it("getStudyStaticLists", function(done) {

        AsynchHelper.doneLoading = 0;
        spyOn(StudyScreen, "staticListsDataCallback").and.callFake(function (data, callbackFunc) {
            StudyScreen.staticListsDataCallback.and.callThrough(data, callbackFunc);
            StudyScreen.staticListsDataCallback(data, callbackFunc);

            AsynchHelper.oneDoneCheckGoal(1, done);
        });

        fundingSourcesSelectOptions = '';

        getStudyStaticLists();

    });
    it("getStudyStaticLists part 2", function() {

        expect(AsynchHelper.doneLoading).toBe(1);

        expect(fundingSourcesSelectOptions).toEqual(
            "<option value='1'>Pilot Grant</option>" +
            "<option value='2'>Federal PHS</option>" +
            "<option value='3'>Foundation</option>" +
            "<option value='4'>Industry</option>" +
            "<option value='5'>Institutional/Departmental Funds</option>" +
            "<option value='6'>Other Federal</option>" +
            "<option value='7'>Other</option>"
        );
    });
});
