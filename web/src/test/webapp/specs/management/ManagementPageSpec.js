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

describe('crudUserClick', function () {
    it('calls processUserForm', function () {
        var spy_processUserForm = spyOn(window, "processUserForm");
        var spy_dollarblockUI = spyOn($, "blockUI");
        var spy_dollarunblockUI = spyOn($, "unblockUI");

        crudUserClick();
        expect(spy_processUserForm).toHaveBeenCalledTimes(1);
        expect(spy_dollarblockUI).toHaveBeenCalledTimes(1);
        expect(spy_dollarunblockUI).toHaveBeenCalledTimes(1);
    });
});

describe('User management page', function() {

    it('shows the correct list of users given the number of records specified in the UI', function() {

        var server = UtilHelper.getFakeServer();

        // SessionDataHelper.setUser(SUPER_ADMIN);

        // load the home page into the DOM
        FixtureHelper.loadSourceHtmlFixtureIntoDom('mgmt_screen.html');

        // verify that the old filter controls are gone

        expect($("div.filterContainer").length).toBe(0);
        expect($("div#selectBox").length).toBe(0);
        expect($("div#pagination_block").length).toBe(0);
        expect($("div#userDataTable").length).toBe(0);

        // mock the server responding to the AJAX calls
        var url = "rest/management/getUsers?page=1&maxResults=50&orderBy=ASC&sortBy=u.lastName&search=%7B%22searchItems%22%3A%5B%5D%7D";
        var responseString = FixtureHelper.getTestFixtureContent('management/json/users.json');
        var jsonResponse = JSON.parse(responseString);
        var userData = jsonResponse.users;
        AjaxHelper.setupFakeServerTextResponse(server, url, responseString);

        // method under test: call the functions which populates the 'mgmt_screen.html' page
        MgmtPage.init();
        loadUserData();

        // this is where things that were asynchronous came to be synchronous
        // i.e. let the mock server respond to pending AJAX calls, and execute the callbacks
        server.respond();

        // expect the initial number of records is 50
        expect($('#userTablePagination_recordsPerPageSelector').val()).toEqual('50');
        // expect the number of rows in the table is as expected
        var rows = $(".userTable-tableRow");
        // expect the raw data and the table to match, in the same order
        expect(rows.length).toEqual(userData.length);
        verifyUserTableContent(userData, rows);

        // Now select top 10 records

        var url10 = "rest/management/getUsers?page=1&maxResults=10&orderBy=ASC&sortBy=u.lastName&search=%7B%22searchItems%22%3A%5B%5D%7D";
        var userData10 = userData.slice(0, 10);
        var jsonResponse10 = jsonResponse;
        jsonResponse10.users = userData10;
        var responseString10 = JSON.stringify(jsonResponse10);
        AjaxHelper.setupFakeServerTextResponse(server, url10, responseString10);

        $('#userTablePagination_recordsPerPageSelector').val(10);
        // FIXME: kludge! the change event was not triggered when the element's value was changed programmatically
        $('#userTablePagination_recordsPerPageSelector').trigger('change');
        server.respond();

        rows = $(".userTable-tableRow");
        expect(rows.length).toEqual(10);

        verifyUserTableContent(userData10, rows);

        var actionLink = $(".userTable-tableRow td:eq(5) a");
        expect(actionLink.html()).toEqual('View Detail');
        var firstRowUserId = userData10[0].id;

        expect(actionLink.attr('href')).toEqual('javascript:showUserDetail(' + firstRowUserId + ')');

    });

    /**
     * A helper function for verifying the content of the users table
     */
    function verifyUserTableContent(userData, rows) {

        // Verify that the table contains the users data that we provided the AJAX call, formatted and rearranged
        $.each(rows, function(rowIndex, row) {
            var cells = $(row).children();
            var user = userData[rowIndex];
            var expectedUserData = [];
            expectedUserData[0] = user.lastName;
            expectedUserData[1] = user.firstName;
            expectedUserData[2] = user.primaryPhone;
            expectedUserData[3] = user.ecommonsId;
            expectedUserData[4] = user.active ? 'Active' : 'Inactive';
            expectedUserData[5] = 'View Detail';
            var expectedId = user.id;
            var expectedHref = [];
            expectedHref[0] = null;
            expectedHref[1] = null;
            expectedHref[2] = null;
            expectedHref[3] = null;
            expectedHref[4] = 'javascript:userChangeStatusConfirmation(' + expectedId + ')';
            expectedHref[5] = 'javascript:showUserDetail(' + expectedId + ')';
            $.each(cells, function(cellIndex, cell) {
                expect($(cell).text()).toEqual(expectedUserData[cellIndex]);
                if (expectedHref[cellIndex]) {
                    var anchor = $($(cell).find('a'));
                    expect(anchor.length).toBe(1);
                    expect(anchor.attr('href')).toEqual(expectedHref[cellIndex]);
                }
            });
        });
    }

});
