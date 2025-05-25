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
describe('The study details page', function() {

    it('shows dates in military format', function () {

        FixtureHelper.loadSourceHtmlFixtureIntoDom('study_detail.html');

        expect($("#study_earliestStartDateValReadOnly").html()).toBe('');
        expect($("#study_latestStartDateValReadOnly").html()).toBe('');

        window.app_selectedStudy = {};
        app_selectedStudy.localId = 1;
        app_selectedStudy.name = "The study";
        app_selectedStudy.studyStatusName = "status";
        app_selectedStudy.institutionName = "The institution";
        app_selectedStudy.industryInitiated = false;
        app_selectedStudy.catalystId = "Catalyst ID 1";
        app_selectedStudy.irb = "IRB?";
        app_selectedStudy.shortTitle = "short title";
        app_selectedStudy.totalSubjects = 12;
        app_selectedStudy.totalOutpatientVisits = 20;
        app_selectedStudy.totalInpatientVisits = 4;
        app_selectedStudy.pediatric = false;
        app_selectedStudy.crcCategory = "some category?";
        app_selectedStudy.clinicalTrial = false;
        app_selectedStudy.newDrug = false;
        app_selectedStudy.deviceExemption = false;
        app_selectedStudy.irbInstitutionName = "IRB institution";
        // TODO: test funding source information too:
        app_selectedStudy.fundingSourceInfoList = [];
        app_selectedStudy.scheduler = {
            "firstName": "John",
            "lastName": "Smith",
            "email": "john.smith@hospital.org",
            "institution": "scheduler's institution",
            "primaryPhone": "1234567890"

        };
        app_selectedStudy.secondaryScheduler = {
            "firstName": "Alice",
            "lastName": "In Wonderlanf",
            "email": "alice@wonderland.edu",
            "institution": "Rabbit hole",
            "primaryPhone": "0001110000"
        };
        app_selectedStudy.scheduler3 = {
            "firstName": "Bob",
            "lastName": "The Chef",
            "email": "bob@cooking.com",
            "institution": "The restaurant",
            "primaryPhone": "2222222222"
        };
        app_selectedStudy.protocolNurseString = "N/A";
        app_selectedStudy.associateNurse = {
            "firstName": "Joanne",
            "lastName": "Stitches",
            "email": "joanne.stitches@everything.insight.com",
            "institution": "Stitch-MS",
            "primaryPhone": "3334445555"
        };
        app_selectedStudy.protocolNutritionistString = "N/A";
        app_selectedStudy.associateProtocolNutritionist = {
            "firstName": "Joanne2",
            "lastName": "Stitches2",
            "email": "joanne.stitches@everything.insight.com2",
            "institution": "Stitch-MS2",
            "primaryPhone": "3334445555_2"
        };
        app_selectedStudy.investigator = {};
        app_selectedStudy.investigator.credential = 'ABC';
        app_selectedStudy.investigator.division = 'def';
        app_selectedStudy.investigator.department = 'ghi';
        app_selectedStudy.investigator.firstName = 'jkl';
        app_selectedStudy.investigator.lastName = 'mno';
        app_selectedStudy.investigator.email = 'pqr';
        app_selectedStudy.investigator.primaryPhone = '123';
        app_selectedStudy.investigator.secondaryPhone = '456';
        app_selectedStudy.investigator.pager = '789';
        app_selectedStudy.physician = {};
        app_selectedStudy.physician.credential = 'ABC_1';
        app_selectedStudy.physician.division = 'def_1';
        app_selectedStudy.physician.department = 'ghi_1';
        app_selectedStudy.physician.firstName = 'jkl_1';
        app_selectedStudy.physician.lastName = 'mno_1';
        app_selectedStudy.physician.email = 'pqr_1';
        app_selectedStudy.physician.primaryPhone = '123_1';
        app_selectedStudy.physician.secondaryPhone = '456_1';
        app_selectedStudy.physician.pager = '789_1';
        app_selectedStudy.physician2 = {};
        app_selectedStudy.physician2.credential = 'ABC_2';
        app_selectedStudy.physician2.division = 'def_2';
        app_selectedStudy.physician2.department = 'ghi_2';
        app_selectedStudy.physician2.firstName = 'jkl_2';
        app_selectedStudy.physician2.lastName = 'mno_2';
        app_selectedStudy.physician2.email = 'pqr_2';
        app_selectedStudy.physician2.primaryPhone = '123_2';
        app_selectedStudy.physician2.secondaryPhone = '456_2';
        app_selectedStudy.physician2.pager = '789_2';
        app_selectedStudy.studyAbstract = 'this study will save the world';
        app_selectedStudy.irbExpiration = '2020/03/04 11:03 pm';
        app_selectedStudy.irbRenewalDate = '2020/03/04 11:04 pm';
        app_selectedStudy.irbApprovalDate = '2020/03/04 11:05 pm';
        app_selectedStudy.expectedEndDate = '2020/03/04 11:06 PM';
        app_selectedStudy.expectedStartDate = '2020/03/04 11:06 am';
        app_selectedStudy.firstVisitDate = '2020/03/04 11:07 AM';
        app_selectedStudy.lastScheduledVisitDate = '2020/03/04 11:08 pm';

        loadStudyData();

        // TODO: many expectations to have here - flesh out this test

        // expect visit dates to be expressed in military time format
        expect($("#study_earliestStartDateValReadOnly").html()).toBe('03/04/2020 11:07');
        expect($("#study_latestStartDateValReadOnly").html()).toBe('03/04/2020 23:08');

    });

    describe('Show Study SubjectsTab', function () {
        it('displays study subject tab with all non table related data', function () {

            sessionStorage.setItem("selectedStudyTab", "studySubjectsTab3");
            sessionStorage.setItem("userData", JSON.stringify({
                institutionRole: {
                    id: STUDY_STAFF
                }
            }));

            sessionStorage.setItem('subjectMode', "default");
            var selectedStudy = {
                id: 349,
                localId: 850,
                name: 'Test Study'
            };

            sessionStorage.setItem("studyData", JSON.stringify(selectedStudy));


            FixtureHelper.loadSourceHtmlFixtureIntoDom('study_detail.html');

            var server = UtilHelper.getFakeServer();
            var getStudySubjectListUrl = "rest/study/getStudySubjects?study=349&inactive=true&page=1&maxResults=50&orderBy=ASC&sortBy=mrn&search=%7B%22searchItems%22%3A%5B%5D%7D";

            AjaxHelper.setupFakeServerTextResponseFromTestFixture(server, getStudySubjectListUrl, "studies/json/study-subjects-list.json");

            loadStudySubjectsTab();

            server.respond();

            expect($("#study_localId2").text()).toBe("(" + selectedStudy.localId + ")");
            expect($("#study_name2").text()).toBe(selectedStudy.name);
            expect($("#studySubjectsTab3 .primaryTableHeader").is(':visible')).toBe(true);
            expect($("#studySubjectsTab3 .primaryTableHeader").text()).toBe("Study Subjects");

            expect($("#study_newStudySubjectButton").is(":visible")).toBe(true);
        });

        it('displays a study subject table', function () {

            sessionStorage.setItem("selectedStudyTab", "studySubjectsTab3");
            sessionStorage.setItem("userData", JSON.stringify({
                institutionRole: {
                    id: STUDY_STAFF
                }
            }));

            sessionStorage.setItem('subjectMode', "default");
            var selectedStudy = {
                id: 349,
                localId: 850,
                name: 'Test Study'
            };

            sessionStorage.setItem("studyData", JSON.stringify(selectedStudy));

            FixtureHelper.loadSourceHtmlFixtureIntoDom('study_detail.html');

            var server = UtilHelper.getFakeServer();
            var getStudySubjectListUrl = "rest/study/getStudySubjects?study=349&inactive=true&page=1&maxResults=50&orderBy=ASC&sortBy=mrn&search=%7B%22searchItems%22%3A%5B%5D%7D";

            AjaxHelper.setupFakeServerTextResponseFromTestFixture(server, getStudySubjectListUrl, "studies/json/study-subjects-list.json");

            loadStudySubjectsTab();

            server.respond();

            var studySubjectsData = FixtureHelper.getTestJsonFixture("studies/json/study-subjects-list.json");

            var filterCells = $("#studySubjectsTable-filterRow").find(".filterField");
            expect(filterCells.length).toBe(7);

            expect($(filterCells.get(0)).parent().find('span input').attr('placeholder')).toBe('MRN');
            expect($(filterCells.get(1)).parent().find('span input').attr('placeholder')).toBe('Last Name');
            expect($(filterCells.get(2)).parent().find('span input').attr('placeholder')).toBe('First Name');
            expect($(filterCells.get(3)).parent().find('span input').attr('placeholder')).toBe('DOB');
            expect($(filterCells.get(4)).parent().find('span input').attr('placeholder')).toBe("City");
            expect($(filterCells.get(5)).parent().find('span input').attr('placeholder')).toBe("State");
            expect($(filterCells.get(6)).parent().find('span input').attr('placeholder')).toBe("Contact Number");

            //one row with column names and three rows of data
            expect($("#studySubjectsTable").find("tbody tr").length).toBe(3);

            expect($("#studySubjectsTablePagination").is(":visible")).toBe(true);
            expect($("#studySubjectsTablePagination .selectBox").length == 1).toBe(true);
            expect($("#studySubjectsTablePagination .pagination_block").length == 1).toBe(true);

            var colNameCells = $("#studySubjectsTable-columnLabelRow").children("td");
            expect($(colNameCells.get(0)).text()).toBe('MRN');
            expect($(colNameCells.get(1)).text()).toBe('Last Name');
            expect($(colNameCells.get(2)).text()).toBe('First Name');
            expect($(colNameCells.get(3)).text()).toBe('DOB');
            expect($(colNameCells.get(4)).text()).toBe("City");
            expect($(colNameCells.get(5)).text()).toBe("State");
            expect($(colNameCells.get(6)).text()).toBe("Primary Contact Number");
            expect($(colNameCells.get(7)).text()).toBe("Study Subjects status");

            var rows = $('.studySubjectsTable-tableRow');
            expect(rows.length).toBe(2);

            rows.each(function (rowIndex, row) {
                var rowData = studySubjectsData.studySubject1s[rowIndex];
                var cells = $(row).children('td');
                expect($(cells).length).toBe(9);
                expect($(cells.get(0)).text()).toBe(rowData.subjectMRN);
                expect($(cells.get(1)).text()).toBe(rowData.subjectLastName);
                expect($(cells.get(2)).text()).toBe(rowData.subjectFirstName);
                expect($(cells.get(3)).text()).toBe(showDate(rowData.subjectDOB));
                expect($(cells.get(4)).text()).toBe(rowData.subjectCity);
                expect($(cells.get(5)).text()).toBe(rowData.subjectState);
                expect($(cells.get(6)).text()).toBe(unifiedPhoneNumbersFormat(rowData.subjectPrimaryContact));
                expect($(cells.get(7)).text()).toBe(rowData.subjectStatus ? 'Active' : 'Not Active');
                expect($(cells.get(7)).find("a").attr("href")).toBe("javascript:changeStudySubjectStatus(" + rowData.id + ")");
                expect($.trim($(cells.get(8)).text())).toBe("View Details");
                expect($(cells.get(8)).find("a").attr("href")).toBe("javascript:goToStudySubjectForm(\"" + rowData.subjectId
                    + "\",\"" + rowData.subjectMRN + "\",\"" + rowData.subjectMRNInstitution + "\")");
            });

            var hasDatepickerBirthdateFilter = $("#studySubjectsTable-filter-birthdate").parent()
                    .find('.hasDatepicker').length > 0;
            expect(hasDatepickerBirthdateFilter).toBe(true);

            var hasContactNumberMask = $("#studySubjectsTable-filter-primaryContactNumber")
                    .parent().find('input[type="hidden"]').length > 0;
            expect(hasContactNumberMask).toBe(true);
        });
    });

    describe("Study Members Tab", function () {
        it('displays study members tab with all non table related data', function () {

            sessionStorage.setItem("selectedStudyTab", "studyMembersTab4");
            sessionStorage.setItem("userData", JSON.stringify({
                institutionRole: {
                    id: STUDY_STAFF
                }
            }));

            var selectedStudy = {
                id: 1,
                localId: 238,
                name: 'Test Study for Members'
            };
            sessionStorage.setItem("studyData", JSON.stringify(selectedStudy));

            FixtureHelper.loadSourceHtmlFixtureIntoDom('study_detail.html');

            var server = UtilHelper.getFakeServer();
            var membersUrl = "rest/study/getStudyMembers?study=" + selectedStudy.id + "&page=1&maxResults=50&orderBy=ASC&sortBy=su.user.lastName&search=%7B%22searchItems%22%3A%5B%5D%7D";

            AjaxHelper.setupFakeServerTextResponseFromTestFixture(server, membersUrl, "studies/json/study-members-list.json");

            loadStudyMembersTab();

            server.respond();

            expect($("#study_localId3").text()).toBe("(" + selectedStudy.localId + ")");
            expect($("#study_name3").text()).toBe(selectedStudy.name);
            expect($("#studyMembersTab4 .primaryTableHeader").is(":visible")).toBe(true);
            expect($("#studyMembersTab4 .primaryTableHeader").text()).toBe("Study Members");

            expect($(".study_newStudyMemberButton").css("visibility") == "hidden").toBe(true);

            sessionStorage.setItem("userData", JSON.stringify({
                institutionRole: {
                    id: FRONT_DESK
                }
            }));

            AjaxHelper.setupFakeServerTextResponseFromTestFixture(server, membersUrl, "studies/json/study-members-list.json");

            loadStudyMembersTab();

            server.respond();

            expect($(".study_newStudyMemberButton").css("visibility") == "hidden").toBe(false);
        });

        it('has a Study Member table', function () {

            sessionStorage.setItem("selectedStudyTab", "studyMembersTab4");
            sessionStorage.setItem("userData", JSON.stringify({
                institutionRole: {
                    id: FRONT_DESK
                }
            }));

            sessionStorage.setItem("studyData", JSON.stringify({
                id: 1
            }));

            FixtureHelper.loadSourceHtmlFixtureIntoDom('study_detail.html');

            var server = UtilHelper.getFakeServer();
            var membersUrl = "rest/study/getStudyMembers?study=1&page=1&maxResults=50&orderBy=ASC&sortBy=su.user.lastName&search=%7B%22searchItems%22%3A%5B%5D%7D";

            AjaxHelper.setupFakeServerTextResponseFromTestFixture(server, membersUrl, "studies/json/study-members-list.json");

            loadStudyMembersTab();

            server.respond();

            var membersData = FixtureHelper.getTestJsonFixture("studies/json/study-members-list.json");

            var table = $('#studyMembersTable');
            expect(table.length).toBe(1);

            expect($("#studyMembersTablePagination").is(":visible")).toBe(true);
            expect($("#studyMembersTablePagination .selectBox").length == 1).toBe(true);
            expect($("#studyMembersTablePagination .pagination_block").length == 1).toBe(true);

            var filterRow = $('#studyMembersTable-filterRow');
            expect(filterRow.length).toBe(1);
            var filterCells = $(filterRow).children('td');
            expect($(filterCells).length).toBe(4);
            expect($(filterCells.get(0)).children('input').hasClass('filterField')).toBe(true);
            expect($(filterCells.get(1)).children('input').hasClass('filterField')).toBe(true);
            expect($(filterCells.get(2)).children('input').hasClass('filterField')).toBe(true);
            expect($(filterCells.get(3)).children('input').hasClass('filterField')).toBe(false);
            expect($(filterCells.get(0)).find('span input').attr('placeholder')).toBe('Last Name');
            expect($(filterCells.get(1)).find('span input').attr('placeholder')).toBe('First Name');
            expect($(filterCells.get(2)).find('span input').attr('placeholder')).toBe('Title');
            expect($(filterCells.get(3)).find('span input').attr('placeholder')).toBeUndefined();

            var headerRow = $('#studyMembersTable-columnLabelRow');
            expect(headerRow.length).toBe(1);
            var headerCells = $(headerRow).children('td');
            expect($(headerCells).length).toBe(4);
            expect($(headerCells.get(0)).text()).toBe("Last Name");
            expect($(headerCells.get(1)).text()).toBe("First Name");
            expect($(headerCells.get(2)).text()).toBe("Title");
            expect($(headerCells.get(3)).text()).toBe("Status");

            var rows = $('.studyMembersTable-tableRow');
            expect(rows.length).toBe(4);

            rows.each(function (rowIndex, row) {
                var rowData = membersData[rowIndex];
                var cells = $(row).children('td');
                expect($(cells).length).toBe(4);
                expect($(cells.get(0)).text()).toBe(rowData.lastName);
                expect($(cells.get(1)).text()).toBe(rowData.firstName);
                expect($(cells.get(2)).text()).toBe(rowData.title);
                expect($(cells.get(3)).text()).toBe(rowData.status ? 'Active' : 'Not Active');
                expect($(cells.get(3)).find("a").attr("href")).toBe("javascript:toggleStudyMemberStatusDialog(" + rowData.id + ")");
            });

            sessionStorage.setItem("userData", JSON.stringify({
                institutionRole: {
                    id: STUDY_STAFF
                }
            }));

            //now check rows for study staff
            AjaxHelper.setupFakeServerTextResponseFromTestFixture(server, membersUrl, "studies/json/study-members-list.json");

            loadStudyMembersTab();

            server.respond();

            var rows = $('.studyMembersTable-tableRow');
            expect(rows.length).toBe(4);

            rows.each(function (rowIndex, row) {
                var rowData = membersData[rowIndex];
                var cells = $(row).children('td');
                expect($(cells).length).toBe(4);
                expect($(cells.get(0)).text()).toBe(rowData.lastName);
                expect($(cells.get(1)).text()).toBe(rowData.firstName);
                expect($(cells.get(2)).text()).toBe(rowData.title);
                expect($(cells.get(3)).text()).toBe(rowData.status ? 'Active' : 'Not Active');
                expect($(cells.get(3)).find("a").length).toBe(0);
                expect($(cells.get(3)).hasClass("bold")).toBe(true);
            });
        });

    });

    describe("Add Study Members", function(){
        it("shows study member search", function(){
            FixtureHelper.loadSourceHtmlFixtureIntoDom('study_member.html');

            var pageHeader = $(".primaryTableHeader").text();
            expect(pageHeader).toBe("Add Study Members");

            var searchLabel = $(".filterContainer").find("strong").text();
            expect(searchLabel).toBe("Search by User Last Name or User ID:");

            expect($("#filter_studymember_string").is(":visible")).toBe(true);
            expect($("#subject_filter_studymemberLast_Button").is(":visible")).toBe(true);
        });

        it("displays add study member table", function(){
            FixtureHelper.loadSourceHtmlFixtureIntoDom('study_member.html');

            var server = UtilHelper.getFakeServer();

            var addMembersData = FixtureHelper.getTestJsonFixture("studies/json/add-study-members-list.json");

            var userSearch = "Smith";
            var addMembersUrl = "rest/management/getStudyMembersStudyScreen?filterString=" + userSearch
                + "&orderBy=ASC&sortBy=u.lastName";

            AjaxHelper.setupFakeServerTextResponseFromTestFixture(server, addMembersUrl, "studies/json/add-study-members-list.json");

            $("#filter_studymember_string").val(userSearch);
            $("#subject_filter_studymemberLast_Button").click();

            server.respond();

            var headerRow = $('#addStudyMembersTable-columnLabelRow');
            expect(headerRow.length).toBe(1);
            var headerCells = $(headerRow).children('td');
            expect($(headerCells).length).toBe(5);
            expect($(headerCells.get(0)).find("input[type='checkbox']").length).toBe(1);
            expect($(headerCells.get(1)).text()).toBe("Last Name");
            expect($(headerCells.get(2)).text()).toBe("First Name");
            expect($(headerCells.get(3)).text()).toBe("Title");
            expect($(headerCells.get(4)).text()).toBe("User ID");

            var rows = $('.addStudyMembersTable-tableRow');
            expect(rows.length).toBe(4);

            rows.each(function (rowIndex, row) {
                var rowData = addMembersData[rowIndex];
                var cells = $(row).children('td');
                expect($(cells).length).toBe(5);
                expect($(cells.get(0)).find("input[type='checkbox']").length).toBe(1);
                expect($(cells.get(1)).text()).toBe(rowData.lastName);
                expect($(cells.get(2)).text()).toBe(rowData.firstName);
                expect($(cells.get(3)).text()).toBe(rowData.title);
                expect($(cells.get(4)).text()).toBe(rowData.userId);
            });
        });
    });
});

describe('VisitTables.buildDisplayFilterSelect', function() {
    it('uses fn to generate the select options', function() {
        var options = [
            {value: 'No', text: 'Display Unselected Annotations'},
            {value: 'Yes', text: 'Display Selected Annotations'},
            {value: 'All', text: 'Display Both'}
        ];

        expect(VisitTables.buildDisplayFilterSelectOptions(options)).toBe(
            '<option value="No">Display Unselected Annotations</option>' +
            '<option value="Yes">Display Selected Annotations</option>' +
            '<option value="All">Display Both</option>');
    });

    it('applies options to combobox', function () {

        // for var-iety, using var here rather than 'this.'
        var spy_buildDisplayFilterSelectOptions =
            spyOn(VisitTables, "buildDisplayFilterSelectOptions").and.returnValue('options');
        var spy_dollar = spyOn(window, "$").and.callThrough();
        var spy_html = spyOn($.fn, "html");
        var spy_createComboBox = spyOn(WidgetUtil, "createComboBox");

        VisitTables.buildDisplayFilterSelect();

        expect(spy_buildDisplayFilterSelectOptions).toHaveBeenCalledTimes(1);

        expect(spy_dollar).toHaveBeenCalledTimes(1);
        expect(spy_dollar.calls.argsFor(0)).toEqual(['.filterSelectedResourcesSelect']);

        expect(spy_html).toHaveBeenCalledTimes(1);
        expect(spy_html.calls.argsFor(0)).toEqual(['options']);

        expect(spy_createComboBox).toHaveBeenCalledTimes(1);
        expect(spy_createComboBox.calls.argsFor(0)).toEqual(['.filterSelectedResourcesSelect', {
            width: 216,
            height: comboboxHeight,
            onSelect: jasmine.any(Function),
            value: 'All'
        }]);
    });
});
