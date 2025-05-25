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
describe('Visit Templates Tab', function() {

    it("displays all non table related data", function(){
        FixtureHelper.loadSourceHtmlFixtureIntoDom('study_detail.html');

        app_selectedStudy = {};
        app_selectedStudy.localId = 200;
        app_selectedStudy.name = "Test Study 1";

        user = {
            institutionRole: {
                id: SUPER_ADMIN
            }
        };
        loadVisitTemplatesDataIntoTablesWidget(false);
        expect($("#study_newStudyVisitButton").is(":visible")).toBe(true);


        user = {
            institutionRole: {
                id: STUDY_STAFF
            }
        };
        loadVisitTemplatesDataIntoTablesWidget(true);

        expect($("#study_localId1").text()).toBe("(" + app_selectedStudy.localId + ")");
        expect($("#study_name1").text()).toBe(app_selectedStudy.name);
        expect($("#study_newStudyVisitButton").is(":visible")).toBe(false);
    });

    function loadVisitTemplateTable(active) {
        FixtureHelper.loadSourceHtmlFixtureIntoDom('study_detail.html');

        var server = UtilHelper.getFakeServer();

        app_selectedStudy = {};
        app_selectedStudy.id = 258;
        app_selectedStudy.localId = 1923;
        app_selectedStudy.name = "The study";

        var visitsByStudyUrl = "rest/appointment/getVisitsByStudy?study=258&active=" + active + "&page=1" +
            "&maxResults=25&orderBy=ASC&sortBy=v.name&search=%7B%22searchItems%22%3A%5B%5D%7D";

        AjaxHelper.setupFakeServerTextResponseFromTestFixture(server, visitsByStudyUrl, "visitTemplates/json/visit-templates-data.json");

        loadVisitTemplatesDataIntoTablesWidget(active);

        server.respond();

        var visitTemplateData = FixtureHelper.getTestJsonFixture("visitTemplates/json/visit-templates-data.json");

        var visitTemplateTableId = "activeVisitTemplatesTable";
        if(!active)
        {
            visitTemplateTableId = "inactiveVisitTemplatesTable";
        }
        var filterCells = $("#" + visitTemplateTableId+ "-filterRow").find(".filterField");
        expect(filterCells.length).toBe(3);

        expect($(filterCells.get(0)).parent().find('span input').attr('placeholder')).toBe('Name');
        expect($(filterCells.get(1)).parent().find('span input').attr('placeholder')).toBe('Visit Type');
        expect($(filterCells.get(2)).parent().find('span input').attr('placeholder')).toBe('Sub-location');

        //one row with column names and two rows of data
        expect($("#" + visitTemplateTableId).find("tbody tr").length).toBe(3);

        var rows = $("." + visitTemplateTableId + "-tableRow");
        expect(rows.length).toBe(2);

        rows.each(function(rowIndex, row) {
            var rowData = visitTemplateData[rowIndex];
            var cells = $(row).children('td');

            if(UserRoleUtil.isResourceManagerOrSuperAdmin(user)) {
                expect($(cells).length).toBe(8);
                expect($(cells.get(0)).find("input[type='checkbox']").length == 1).toBe(true);
                expect($(cells.get(1)).text()).toBe(rowData.visitName);
                expect($(cells.get(2)).text()).toBe(rowData.visitTypeName);
                expect($(cells.get(3)).text()).toBe(rowData.sublocationName);
                expect($(cells.get(4)).text()).toBe(rowData.finalApprovalStatus);
                expect($.trim($(cells.get(5)).text())).toBe("View Detail");
                expect($(cells.get(5)).find("a").attr("href")).toBe("javascript:showVisitDetail(" + rowData.id + ")");
                expect($.trim($(cells.get(6)).text())).toBe("Copy Visit");
                expect($(cells.get(6)).find("a").attr("href")).toBe("javascript:copyVisitTemplate(" + rowData.id + ")");
                expect($.trim($(cells.get(7)).text())).toBe("Delete Visit");
                expect($(cells.get(7)).find("a").attr("href")).toBe("javascript:deleteVisitTemplate(" + rowData.id + ")");
            }
            else {
                expect($(cells).length).toBe(5);
                expect($(cells.get(0)).text()).toBe(rowData.visitName);
                expect($(cells.get(1)).text()).toBe(rowData.visitTypeName);
                expect($(cells.get(2)).text()).toBe(rowData.sublocationName);
                expect($(cells.get(3)).text()).toBe(rowData.finalApprovalStatus);
                expect($.trim($(cells.get(4)).text())).toBe("View Detail");
                expect($(cells.get(4)).find("a").attr("href")).toBe("javascript:showVisitDetail(" + rowData.id + ")");
            }
        });
    }
    describe('Active Visit Templates Tab', function() {

        it("displays active visits table for admin user", function(){
            user = {
                institutionRole: {
                    id: SUPER_ADMIN
                }
            };

            loadVisitTemplateTable(true);
        });

        it("displays active visits table for staff user", function(){
            user = {
                institutionRole: {
                    id: STUDY_STAFF
                }
            };

            loadVisitTemplateTable(true);
        });
    });

    describe('Inactive Visit Templates Tab', function() {

        it("displays inactive visits table for admin user", function () {
            user = {
                institutionRole: {
                    id: SUPER_ADMIN
                }
            };

            loadVisitTemplateTable(false);
        });
    });
});
