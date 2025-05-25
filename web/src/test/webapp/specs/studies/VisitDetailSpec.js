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
describe("Build Visit Template", function() {
    describe("Add Resource", function () {
        it("Add Resource - displays resource annotations table", function () {

            FixtureHelper.loadSourceHtmlFixtureIntoDom('visit_detail.html');

            var server = UtilHelper.getFakeServer();

            sessionStorage.setItem("selectedTab", 2);
            var visitId = 125;
            var visitData = {
                id: visitId,
                studyName: "Test Study",
                visitName: "Visit 1",
                visitTypeName: "Outpatient Non CRC"
            };
            sessionStorage.setItem("visitData", JSON.stringify(visitData));

            var studyData = {
                localId: 1729,
                investigator: {
                    firstName: 'First',
                    lastName: 'Last'
                }
            };
            sessionStorage.setItem("studyData", JSON.stringify(studyData));

            var visitTemplateUrl = "rest/study/getVisitTemplateData?visit=" + visitId;
            var visitTemplateData = FixtureHelper.getTestJsonFixture("studies/json/visit-template-data.json");

            var visitTemplateId = visitTemplateData.id;

            AjaxHelper.setupFakeServerTextResponseFromTestFixture(server, visitTemplateUrl,
                "studies/json/visit-template-data.json");

            AjaxHelper.setupFakeServerTextResponseFromTestFixture(server, "rest/app/getStaticLists", "global/json/static-lists.json");

            studyPage.initVisitDetailPage();

            $(".gantt_newResourceButton").first().click();

            server.respond();
            server.respond();

            var resourceAnnotationsHeader = $("#resourceAnnotationsTable-fixedHeader");
            expect(resourceAnnotationsHeader.length).toBe(1);

            var resourceAnnotationsColumnNames = ["", "Resource Activity", "Quantity", "Comment"];
            $("#resourceAnnotationsTable-fixedHeader").find("#resourceAnnotationsTable-columnLabelRow")
                .children().each(function (index) {
                var val = $(this).text();
                expect(val).toBe(resourceAnnotationsColumnNames[index]);
            });

            var staticData = FixtureHelper.getTestJsonFixture("global/json/static-lists.json");
            var resources = staticData.resources;
            expect(resources.length).toBe(29);

            var resourceId = resources[0].id;
            var resourceAnnUrl = "rest/resource/getResourceAnnotations?resourceId=" + resourceId;
            AjaxHelper.setupFakeServerTextResponseFromTestFixture(server, resourceAnnUrl, "studies/json/resource-annotations-list.json");

            $("#wizard_templateResources").val(resourceId).trigger("change");

            server.respond();

            var resourceAnnData = FixtureHelper.getTestJsonFixture("studies/json/resource-annotations-list.json");
            resourceAnnData = resourceAnnData.resourceAnnotationsValues;

            var resourceAnnTableRows = $("#resourceAnnotationsTable tbody").find("tr");
            expect(resourceAnnTableRows.length).toBe(2);

            resourceAnnTableRows.each(function (rowIndex, row) {
                var rowData = resourceAnnData[rowIndex];
                var cells = $(row).children('td');
                expect($(cells).length).toBe(4);
                expect($(cells.get(0)).text()).toBe("");
                expect($(cells.get(1)).text()).toBe(rowData.name);

                var quantity = rowData.quantity;
                quantity = quantity == 0 ? 1 : quantity;
                if (rowData.quantifiable) {
                    expect($(cells.get(2)).find(".ui-spinner-input").spinner("value")).toBe(quantity);
                }
                else {
                    expect($(cells.get(2)).text()).toBe(quantity.toString());
                }
                expect($(cells.get(3)).text()).toBe("Click to edit");
                expect($(cells.get(3)).hasClass("commentFieldText")).toBe(true);
            });
        });
    });

    describe("Edit Resource", function () {
        it("displays annotations for selected resource", function () {

            FixtureHelper.loadSourceHtmlFixtureIntoDom('visit_detail.html');

            var server = UtilHelper.getFakeServer();

            sessionStorage.setItem("selectedTab", 2);
            var visitId = 125;
            var visitData = {
                id: visitId,
                studyName: "Test Study",
                visitName: "Visit 1",
                visitTypeName: "Outpatient Non CRC"
            };
            sessionStorage.setItem("visitData", JSON.stringify(visitData));

            var studyData = {
                localId: 1729,
                investigator: {
                    firstName: 'First',
                    lastName: 'Last'
                }
            };
            sessionStorage.setItem("studyData", JSON.stringify(studyData));

            var visitTemplateUrl = "rest/study/getVisitTemplateData?visit=" + visitId;
            var visitTemplateData = FixtureHelper.getTestJsonFixture("studies/json/visit-template-data.json");

            AjaxHelper.setupFakeServerTextResponseFromTestFixture(server, visitTemplateUrl,
                "studies/json/visit-template-data.json");

            templateResourceId = visitTemplateData.id;
            var visitTemplateDataAnnUrl = "rest/appointment/getTemplateResourceDataWithAnnotations?templateResourceId=" + templateResourceId;
            var visitTemplateDataAnnData = FixtureHelper.getTestJsonFixture("studies/json/visit-template-data-annotations.json");
            AjaxHelper.setupFakeServerTextResponseFromTestFixture(server, visitTemplateDataAnnUrl, "studies/json/visit-template-data-annotations.json");


            studyPage.initVisitDetailPage();

            crudResourceClick("edit_template_resource");

            server.respond();
            server.respond();

            var resourceAnnotationsHeader = $("#resourceAnnotationsTable-fixedHeader");
            expect(resourceAnnotationsHeader.length).toBe(1);

            var resourceAnnotationsColumnNames = ["", "Resource Activity", "Quantity", "Comment"];
            $("#resourceAnnotationsTable-fixedHeader").find("#resourceAnnotationsTable-columnLabelRow")
                .children().each(function (index) {
                var val = $(this).text();
                expect(val).toBe(resourceAnnotationsColumnNames[index]);
            });

            var resourceAnnTableRows = $("#resourceAnnotationsTable tbody").find("tr");
            expect(resourceAnnTableRows.length).toBe(1);

            resourceAnnTableRows.each(function (rowIndex, row) {
                var rowData = visitTemplateDataAnnData.llaList[rowIndex];
                var cells = $(row).children('td');
                expect($(cells).length).toBe(4);
                expect($(cells.get(0)).find(".resourceAnnotationsTable_0-checkbox").is(":checked")).toBe(true);
                expect($(cells.get(1)).text()).toBe(rowData.name);

                var quantity = rowData.quantity;
                quantity = quantity == 0 ? 1 : quantity;
                if (rowData.quantifiable) {
                    expect($(cells.get(2)).find(".ui-spinner-input").spinner("value")).toBe(quantity);
                }
                else {
                    expect($(cells.get(2)).text()).toBe(quantity.toString());
                }
                expect($(cells.get(3)).text()).toBe(rowData.comment);

                expect( $("#crud_template_resource").dialog('option', 'title')).toBe("Edit Resource");
            });
        });
    });
});