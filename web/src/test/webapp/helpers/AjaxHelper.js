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
 * Sinon fake server helpers
 *
 * Sinon provides "Standalone test spies, stubs and mocks for JavaScript"
 * See http://sinonjs.org/
 *
 * This utility class, AjaxHelper, simplifies the use of Sinon for
 * mocking AJAX calls.
 */

AjaxHelper = {};

AjaxHelper.setupFakeServerTextResponse = function(server, url, content) {

    server.respondWith("GET", url,
        [200, {"Content-Type": "text/text"}, content]);

};

AjaxHelper.setupFakeServerPostTextResponse = function(server, url, content) {

    server.respondWith("POST", url,
        [200, {"Content-Type": "text/text"}, content]);

};

AjaxHelper.setupFakeServerTextResponseFromTestFixture = function(server, url, pathToTestFixture) {

    var content = FixtureHelper.getTestFixtureContent(pathToTestFixture);
    server.respondWith("GET", url,
        [200, {"Content-Type": "text/text"}, content]);

};

/**
 * In the following function, the REST call URL and the file path to the source HTML
 * fixture are the same.
 *
 * @param server
 * @param url
 */
AjaxHelper.setUpFakeServerLoadSourceHtmlResponse = function(server, url) {

    var htmlContent = FixtureHelper.getSourceFixtureContent(url);
    server.respondWith("GET", url,
        [200, {"Content-Type": "text/text"}, htmlContent]);

};

/**
 *
 * The following methods set up fake server AJAX responses specific to
 * certain REST end-points. Each of these functions encapsulate the
 * REST end-point's URL and the response's content.
 *
 */

AjaxHelper.setupGetOpenStudiesResponse = function(server) {

    var openStudiesUrl = "rest/study/getOpenStudies?filterString=&page=1&maxResults=10&orderBy=ASC&sortBy=s.localId";
    AjaxHelper.setupFakeServerTextResponseFromTestFixture(server, openStudiesUrl, "home/json/open-studies.json");

};

AjaxHelper.setupGetStudyVisitsResponse = function(server) {

    var studyVisitsUrl = "rest/appointment/getStudyVisits?study=7&approved=true&filterString=&page=1&maxResults=10&orderBy=ASC&sortBy=v.name";
    AjaxHelper.setupFakeServerTextResponseFromTestFixture(server, studyVisitsUrl, "home/json/study-visits.json");

};

AjaxHelper.setupGetStudySubjectsResponse = function(server) {

    var subjectsUrl = "rest/study/getStudySubjects?study=7&filterString=&page=1&maxResults=10&orderBy=ASC&sortBy=lastName";
    AjaxHelper.setupFakeServerTextResponseFromTestFixture(server, subjectsUrl, "home/json/study-subjects.json");

};

AjaxHelper.setupGetVisitResourcesResponse = function(server) {

    var subjectsUrl = "rest/appointment/getRoomResources?id=22";
    AjaxHelper.setupFakeServerTextResponseFromTestFixture(server, subjectsUrl, "home/json/room-resources.json");

};

AjaxHelper.setupGetVisitResourcesResponse = function(server) {

    var subjectsUrl = "rest/appointment/getRoomResources?id=22";
    AjaxHelper.setupFakeServerTextResponseFromTestFixture(server, subjectsUrl, "home/json/room-resources.json");

};

AjaxHelper.setupCheckIfResourcesLinkableOnDeleteUnlinkResponse = function(server, templateResourceId, booleanResponse) {
    var subjectsUrl = "rest/appointment/checkIfResourcesLinkableOnDeleteUnlink?templateResourceId=" + templateResourceId;
    var response = '{ "result": ' + booleanResponse + ' }';
    AjaxHelper.setupFakeServerTextResponse(server, subjectsUrl, response);
};

AjaxHelper.setupGetVisitComments = function(server, id) {
    var subjectsUrl = "rest/study/getVisitComments?id=" + id;
    AjaxHelper.setupFakeServerTextResponseFromTestFixture(server, subjectsUrl, "visitTemplates/json/visit-template-comments.json");
};

