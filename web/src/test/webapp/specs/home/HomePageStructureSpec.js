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

// describe('Appointments page', function() {
describe('src/test/webapp/specs/home/HomePageStructureSpec.js', function() {

    // The following rest calls need to be mocked:
    //
    ///rest/app/getStaticLists
    ///rest/auth/getUserData
    ///rest/appointment/getCalendarBookedVisits?starting=1475121600000&ending=1475207999059&todayVisits=true&filterString=&filterId=&%7B%7D

    beforeEach(function() {
        document.body.innerHTML = "";
    });

    it("has a full header", function() {

        expect(document.getElementById('fullHeader')).toBeNull();

        // load the home page into the DOM
        FixtureHelper.loadSourceHtmlFixtureIntoDom('home.html');

        expect(document.getElementById('fullHeader')).not.toBeNull();

    });


    it("has three sub-tabs: Appointments, Visit Templates Needing Approval, and Resource Timeline", function() {

        var server = UtilHelper.getFakeServer();

        // load the home page into the DOM
        FixtureHelper.loadSourceHtmlFixtureIntoDom('home.html');

        AjaxHelper.setUpFakeServerLoadSourceHtmlResponse(server, "meta_headers.html");
        AjaxHelper.setUpFakeServerLoadSourceHtmlResponse(server, "common_dialogs.html");
        AjaxHelper.setUpFakeServerLoadSourceHtmlResponse(server, "header.html");

        // run all the javascript that normally runs on document load
        onloadHomePage();

        server.respond();
        server.respond();
        server.respond();

        var calendarTab = $(".appCallLi_1");
        var visitTemplateApprovalTab = $(".homeLi_2");
        var timelineTab = $(".appCallLi_2");

        expect(calendarTab.attr('aria-selected')).toBe('true');
        expect(visitTemplateApprovalTab.attr('aria-selected')).toBe('false');
        expect(timelineTab.attr('aria-selected')).toBe('false');

        var calendarTabLink = $(".appCallLi_a1").get(0);
        var visitTemplateApprovalTabLink = $(".homeLi_a2").get(0);
        var timelineTabLink = $(".appCallLi_a2").get(0);

        spyOn(calendarTabLink, 'onclick');
        spyOn(visitTemplateApprovalTabLink, 'onclick');
        spyOn(timelineTabLink, 'onclick');


        click(timelineTabLink);

        expect(calendarTab.attr('aria-selected')).toBe('false');
        expect(visitTemplateApprovalTab.attr('aria-selected')).toBe('false');
        expect(timelineTab.attr('aria-selected')).toBe('true');

        click(visitTemplateApprovalTabLink);

        expect(calendarTab.attr('aria-selected')).toBe('false');
        expect(visitTemplateApprovalTab.attr('aria-selected')).toBe('true');
        expect(timelineTab.attr('aria-selected')).toBe('false');

        click(calendarTabLink);

        expect(calendarTab.attr('aria-selected')).toBe('true');
        expect(visitTemplateApprovalTab.attr('aria-selected')).toBe('false');
        expect(timelineTab.attr('aria-selected')).toBe('false');

        expect(calendarTabLink.onclick).toHaveBeenCalledTimes(1);
        expect(timelineTabLink.onclick).toHaveBeenCalledTimes(1);

    });

});

// describe('Appointments Calendar Sub-Location Tabs', function() {
describe('src/test/webapp/specs/home/HomePageStructureSpec.js', function() {
    beforeEach(function() {
        document.body.innerHTML = "";
    });

    it("Sub-Location tab builder test", function() {
        // load the appointment page into the DOM
        FixtureHelper.loadSourceHtmlFixtureIntoDom('home.html');

        sublocations = [
            {id: 1, institution: {id: 1, longName: "Demo Institution", name: "Demo1"}, name:"Sub-Location 1"},
            {id: 3, institution: {id: 1, longName: "Demo Institution", name: "Demo1"}, name:"Sub-Location 2"},
            {id: 2, institution: {id: 1, longName: "Demo Institution", name: "Demo1"}, name:"Sub-Location 3"},
            {id: 5, institution: {id: 1, longName: "Demo Institution", name: "Demo1"}, name:"Sub-Location 4"}
        ];

        expect($('#subLocTop_0').eq(0).length).toBe(0);
        expect($('#subLocTop_1').eq(0).length).toBe(0);
        expect($('#subLocTop_3').eq(0).length).toBe(0);
        expect($('#subLocTop_5').eq(0).length).toBe(0);

        var result = buildSubLocTabs("subLocTop");
        $("#apptSublocationListTop").html(result.html);

        expect($('#subLocTop_0').eq(0).length).toBe(1);
        expect($('#subLocTop_1').eq(0).length).toBe(1);
        expect($('#subLocTop_3').eq(0).length).toBe(1);
        expect($('#subLocTop_5').eq(0).length).toBe(0);

    });

    // In progress
    it("Verifies top and bottom Sub-Location tabs are linked", function() {
        // load the appointment page into the DOM
        FixtureHelper.loadSourceHtmlFixtureIntoDom('home.html');

        sublocations = [
            {id: 1, institution: {id: 1, longName: "Demo Institution", name: "Demo1"}, name:"Sub-Location 1"},
            {id: 3, institution: {id: 1, longName: "Demo Institution", name: "Demo1"}, name:"Sub-Location 2"},
            {id: 2, institution: {id: 1, longName: "Demo Institution", name: "Demo1"}, name:"Sub-Location 3"},
            {id: 5, institution: {id: 1, longName: "Demo Institution", name: "Demo1"}, name:"Sub-Location 4"}
        ];

        createCalendarAndSublocationTabs();

        var subLocTabTopAll = $("#subLocTop");
        var subLocTabBotAll = $("#subLocBtm");
        var subLoc1TabTop = $("#subLocTop_3");
        var subLoc1TabBot = $("#subLocBtm_3");

        expect($('#subLocTop_0').eq(0).length).toBe(1);
        expect($('#subLocTop_1').eq(0).length).toBe(1);
        expect($('#subLocTop_3').eq(0).length).toBe(1);
        expect($('#subLocTop').eq(0).length).toBe(1);

        expect(subLocTabTopAll.attr('aria-expanded')).toBe('true');
        expect(subLocTabBotAll.attr('aria-expanded')).toBe('true');

        $('a[href="#subLocTop_3"]').eq(0).click();

        expect(subLocTabTopAll.attr('aria-expanded')).toBe('false');
        expect(subLocTabBotAll.attr('aria-expanded')).toBe('false');
        expect(subLoc1TabTop.attr('aria-expanded')).toBe('true');
        expect(subLoc1TabBot.attr('aria-expanded')).toBe('true');

        $('a[href="#subLocTop"]').eq(0).click();

        expect(subLocTabTopAll.attr('aria-expanded')).toBe('true');
        expect(subLocTabTopAll.attr('aria-expanded')).toBe('true');
        expect(subLoc1TabTop.attr('aria-expanded')).toBe('false');
        expect(subLoc1TabBot.attr('aria-expanded')).toBe('false');
    });

});
