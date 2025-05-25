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


describe('Page Footer', function() {

    it("displays no PHI message", function () {

        var server = UtilHelper.getFakeServer();
        // load the home page into the DOM
        FixtureHelper.loadSourceHtmlFixtureIntoDom("home.html");

        AjaxHelper.setUpFakeServerLoadSourceHtmlResponse(server, "meta_headers.html");
        AjaxHelper.setUpFakeServerLoadSourceHtmlResponse(server, "header.html");
        AjaxHelper.setUpFakeServerLoadSourceHtmlResponse(server, "common_dialogs.html");
        AjaxHelper.setUpFakeServerLoadSourceHtmlResponse(server, "footer.html");

        // run all the javascript that normally runs on document load
        onloadHomePage();

        server.respond();
        server.respond();
        server.respond();
        server.respond();
        server.respond();

        expect($("#phiWarning").text()).toBe("Sending PHI through email is strictly prohibited.")
    });

    it("displays a warning popup when the 'help' link is clicked", function() {

        var server = UtilHelper.getFakeServer();
        // load the home page into the DOM
        FixtureHelper.loadSourceHtmlFixtureIntoDom("home.html");

        AjaxHelper.setUpFakeServerLoadSourceHtmlResponse(server, "meta_headers.html");
        AjaxHelper.setUpFakeServerLoadSourceHtmlResponse(server, "header.html");
        AjaxHelper.setUpFakeServerLoadSourceHtmlResponse(server, "common_dialogs.html");
        AjaxHelper.setUpFakeServerLoadSourceHtmlResponse(server, "footer.html");

        // run all the javascript that normally runs on document load
        onloadHomePage();

        server.respond();
        server.respond();
        server.respond();
        server.respond();
        server.respond();

        var link = $("[name='phiWarningLink']");
        expect(link.length).toBe(1);
        expect(link.hasClass('tcell'));

        var messagePara = $("#dialog-message p");
        expect(messagePara.length).toBe(1);
        expect(messagePara.text().trim()).toBe('');

        var warningSpy = sinon.spy(window, "openPhiWarningForZendesk");
        var dialogSpy = sinon.spy(DialogsUtil, "showMessageDialog");

        sinon.assert.notCalled(warningSpy);
        sinon.assert.notCalled(dialogSpy);

        // function under test
        link.click();

        // verify side-effects

        sinon.assert.calledOnce(warningSpy);
        sinon.assert.calledOnce(dialogSpy);

        messagePara = $("#dialog-message p");
        expect(messagePara.length).toBe(1);
        expect(messagePara.text().trim()).toBe('Sending PHI through the help desk is strictly prohibited.');

    });

    it('displays application mode and version', function(){
        var server = UtilHelper.getFakeServer();
        // load the home page into the DOM
        FixtureHelper.loadSourceHtmlFixtureIntoDom("home.html");

        AjaxHelper.setUpFakeServerLoadSourceHtmlResponse(server, "header.html");
        AjaxHelper.setUpFakeServerLoadSourceHtmlResponse(server, "wizard_templates.html");
        AjaxHelper.setUpFakeServerLoadSourceHtmlResponse(server, "appts_dialogs.html");
        AjaxHelper.setUpFakeServerLoadSourceHtmlResponse(server, "common_dialogs.html");
        AjaxHelper.setUpFakeServerLoadSourceHtmlResponse(server, "main_dialog_wrapper.html");
        AjaxHelper.setUpFakeServerLoadSourceHtmlResponse(server, "footer.html");

        var versionData = {
            subjectMode : "STANDALONE",
            version: "3.6.0",
            buildDate: "2017-07-30 11:17:39"
        };
        var url = "rest/app/versionInfo";
        var versionDataString = JSON.stringify(versionData);
        AjaxHelper.setupFakeServerTextResponse(server, url, versionDataString);

        // run all the javascript that normally runs on document load
        onloadHomePage();

        server.respond();
        server.respond();
        server.respond();
        server.respond();
        server.respond();
        server.respond();

        expect($("#version_data").text()).toBe("(STANDALONE) 3.6.0 ( 2017-07-30 11:17:39 )");

        var footer = $("#version_data").parent().parent();
        expect(footer.hasClass("footer")).toBe(true);
        expect(footer.parent().hasClass("wrapperSize")).toBe(true);
    });
});
