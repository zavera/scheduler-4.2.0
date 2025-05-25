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

FixtureHelper = { }

// When running tests from the maven build, the jasmine-jquery seems to not
// be able to load fixtures. So instead we use the html2js karma preprocessor,
// which puts all the fixtures specified in the karma config
// into the window.__html__ variable.
// see karma.conf.**.js

/**
 * FixtureHelper.getFixtureContent() is the single function which
 * encapsulates the interaction with the __html__ area of fixtures.
 * It logs an error message if the fixture was not found in __html__
 */
FixtureHelper.getFixtureContent = function (path, filename) {
    var fullPath = path + '/' + filename;
    var content = __html__[fullPath];
    if (content === undefined) {
        console.log(
            '\nFixture not found: ' + fullPath + '\n' +
            '  Base path: ' + path + '\n' +
            '  Relative file path: ' + filename + '\n' +
            '  possible reasons:\n' +
            '    - It is not listed in your Karma config file\n' +
            '    - It is listed in your Karma config file but the file does not exist\n'
        );
    }
    return content;
};

/**
 * This function abstracts out the root path to fixtures (files) in
 * the tree of source files
 */
FixtureHelper.getSourceFixtureContent = function (filename) {
    var path = 'main/webapp';
    return FixtureHelper.getFixtureContent(path, filename);
};

/**
 * This function abstracts out the root path to fixtures (files) in
 * the tree of test files
 */
FixtureHelper.getTestFixtureContent = function (filename) {
    var path = 'test/webapp/specs';
    return FixtureHelper.getFixtureContent(path, filename);
};

/**
 * This function loads the content of an HTML fixture found in the source tree,
 * into the DOM
 */
FixtureHelper.loadSourceHtmlFixtureIntoDom = function (filename) {
    var content = FixtureHelper.getSourceFixtureContent(filename);
    document.body.innerHTML = content;
};

/**
 * This function _appends_ the content of an HTML fixture found in the source tree,
 * into the DOM
 */
FixtureHelper.appendSourceHtmlFixtureIntoDom = function (filename) {
    var content = FixtureHelper.getSourceFixtureContent(filename);
    $("body").append(content);
};

/**
 * This function loads the content of an HTML fixture found in the test tree,
 * into the DOM
 */
FixtureHelper.loadTestHtmlFixtureIntoDom = function (filename) {
    var content = FixtureHelper.getTestFixtureContent(filename);
    document.body.innerHTML = content;
};

/**
 * This function appends the content of an HTML fixture found in the test tree,
 * into the DOM
 */
FixtureHelper.appendTestHtmlFixtureIntoDom = function (filename) {
    var content = FixtureHelper.getTestFixtureContent(filename);
    $("body").append(content);
};

/**
 * This function loads and parses the content of a JSON fixture found in the test tree,
 * and returns the parsed data as an object
 */
FixtureHelper.getTestJsonFixture = function (filename) {
    var path = 'test/webapp/specs';
    var stringContent = FixtureHelper.getFixtureContent(path, filename);
    return JSON.parse(stringContent);
};


/**
 *
 * The following functions returns an object obtained from
 * loading (and parsing) each a specific JSON fixture.
 * The functions encapsulate thepath to the fixture file.
 */

FixtureHelper.getFirstFromOpenStudiesResponse = function() {

    var studies = FixtureHelper.getTestJsonFixture("home/json/open-studies.json");
    return studies.studys[0];

};

FixtureHelper.getFirstFromStudyVisitsResponse = function() {

    var visits = FixtureHelper.getTestJsonFixture("home/json/study-visits.json");
    return visits.visitTemplate1s[0];

};

FixtureHelper.getPrincipalInvestigatorFromStudyVisitsResponse = function() {
    var visits = FixtureHelper.getTestJsonFixture("home/json/study-visits.json");
    return visits.investigatorFullName;
};

FixtureHelper.getFirstFromStudySubjectsResponse = function() {

    var subjects = FixtureHelper.getTestJsonFixture("home/json/study-subjects.json");
    return subjects.studySubject1s[0];

};

FixtureHelper.getFirstFromVisitResourcesResponse = function() {

    var resources = FixtureHelper.getTestJsonFixture("home/json/room-resources.json");
    return resources.roomResources;

};




