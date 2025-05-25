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
describe('Resource Annotations Section', function() {

    it('Displays categorized list of resource annotations', function () {
        var server = UtilHelper.getFakeServer();

        SessionDataHelper.setUser(SUPER_ADMIN);

        //load the home page into the DOM
        FixtureHelper.loadSourceHtmlFixtureIntoDom('resource_form.html');

        var resource = {
            resourceId: 42,
            resourceType: "Room"
        };

        var pathToFixture = "resources/json/resource-detail.json";
        var resourceDetailString = FixtureHelper.getTestFixtureContent(pathToFixture);
        var selectedResource = JSON.parse(resourceDetailString);

        AjaxHelper.setUpFakeServerLoadSourceHtmlResponse(server, "header.html");
        AjaxHelper.setUpFakeServerLoadSourceHtmlResponse(server, "common_dialogs.html");
        AjaxHelper.setUpFakeServerLoadSourceHtmlResponse(server, "table_templates.html");
        AjaxHelper.setupFakeServerTextResponseFromTestFixture(server, "rest/app/getStaticLists", "resources/json/resource-annotations-list.json");

        var resourceAnnotationsString = FixtureHelper.getTestFixtureContent("resources/json/resource-annotations-list.json");
        var resourceAnnotations = JSON.parse(resourceAnnotationsString);

        //var url = "rest/resource/getResourceAnnotations?resourceId=" + resource.resourceId;
        //AjaxHelper.setupFakeServerTextResponseFromTestFixture(server, url, pathToFixtureTemAdjustments);

        resourceFormPage.init();

        server.respond();
        server.respond();
        server.respond();

        //expand the annotations section
        $("#resourceTabHeading5").click();

        expect($("#toggleAnnotationText").text()).toBe("Expand All");
        expect($.trim($("#clearAnnotationsTop").find("a").text())).toBe("Clear All Field Selections");

        var annotationsHeader = $(".annotationsHeader");
        var nursingAnnotationHeader = $(annotationsHeader.get(0));
        expect($.trim(nursingAnnotationHeader.text())).toBe("Nursing Annotations");
        nursingAnnotationHeader.click();
        expect($("#clearOrSelectNursingLink").text()).toBe("Select All Nursing Annotations");
        var nursingAnnotationsList = resourceAnnotations.nurseAnnotations;
        var nurseUList = $("#nurseList");
        nurseUList.children("li").each(function (index, nurseAnn) {
            expect($(nurseAnn).text()).toBe(nursingAnnotationsList[index].name);
        });

        var nutritionAnnotationHeader = $(annotationsHeader.get(1));
        expect($.trim(nutritionAnnotationHeader.text())).toBe("Nutrition Annotations");
        nutritionAnnotationHeader.click();
        expect($("#clearOrSelectNutritionLink").text()).toBe("Select All Nutrition Annotations");
        var nutritionAnnotationsList = resourceAnnotations.nutritionAnnotations;
        var nutritionUList = $("#nutritionList");
        nutritionUList.children("li").each(function (index, nutritionAnn) {
            expect($(nutritionAnn).text()).toBe(nutritionAnnotationsList[index].name);
        });

        var equipmentAnnotationHeader = $(annotationsHeader.get(2));
        expect($.trim(equipmentAnnotationHeader.text())).toBe("Equipment Annotations");
        equipmentAnnotationHeader.click();
        expect($("#clearOrSelectEquipmentLink").text()).toBe("Select All Equipment Annotations");
        var equipmentAnnotationsList = resourceAnnotations.equipmentAnnotations;
        var equipmentUList = $("#equipmentList");
        equipmentUList.children("li").each(function (index, equipmentAnn) {
            expect($(equipmentAnn).text()).toBe(equipmentAnnotationsList[index].name);
        });

        var roomAnnotationHeader = $(annotationsHeader.get(3));
        expect($.trim(roomAnnotationHeader.text())).toBe("Room Annotations");
        roomAnnotationHeader.click();
        var roomAnnotationsList = resourceAnnotations.roomAnnotations;
        var roomUList = $("#roomList");
        roomUList.children("li").each(function (index, roomAnn) {
            expect($(roomAnn).text()).toBe(roomAnnotationsList[index].name);
        });

        var labAnnotationHeader = $(annotationsHeader.get(4));
        expect($.trim(labAnnotationHeader.text())).toBe("Lab Annotations");
        labAnnotationHeader.click();
        var labAnnotationsList = resourceAnnotations.labAnnotations;
        var labUList = $("#labList");
        labUList.children("li").each(function (index, labAnn) {
            expect($(labAnn).text()).toBe(labAnnotationsList[index].name);
        });

        expect($("#annotationsSaveTop").is(":visible")).toBe(true);
        expect($("#annotationsSaveBottom").is(":visible")).toBe(true);

        expect($("#toggleAnnotationText").text()).toBe("Collapse All");
    });
});
