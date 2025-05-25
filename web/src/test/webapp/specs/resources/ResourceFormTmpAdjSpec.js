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
describe('TemporaryAdjustment.validateSaveTempAdjustment', function() {

    it('validates temporary adjustments that do not span overnight', function () {
        // load the home page into the DOM
        FixtureHelper.loadSourceHtmlFixtureIntoDom('resource_form.html');

        $("#appt_startDate").val(new Date().toDateString());
        $("#appt_endDate").val(new Date().toDateString());

        $("#appt_startTime").val("00:00");
        $("#appt_endTime").val("00:00");

        var resource_ta_startDateVal = new Date($("#appt_startDate").val());
        var resource_ta_endDateVal = new Date($("#appt_endDate").val());

        var resourceStartTime = $("#appt_startTime").val() !== "" ? $("#appt_startTime").val() : null;
        var resourceEndTime = $("#appt_endTime").val() !== "" ? $("#appt_endTime").val() : null;

        var resource_ta_startDateTimeVal = new Date($("#appt_startDate").val() + " " + resourceStartTime);
        var resource_ta_endDateTimeVal = new Date($("#appt_endDate").val() + " " + resourceEndTime);
        var quantity = $('#taQuantity').val();

        var formElements = {
            startDate: resource_ta_startDateVal,
            endDate: resource_ta_endDateVal,
            startTime: resource_ta_startDateTimeVal,
            endTime: resource_ta_endDateTimeVal,
            quantity: quantity
        };

        var validationElements = {
            startDateValidation: "#resource_ta_startDateValidation",
            endDateValidation: "#resource_ta_endDateValidation",
            startTimeValidation: "#resource_ta_startTimeValidation",
            endTimeValidation: "#resource_ta_endTimeValidation",
            quantityValidation: "#resource_ta_quantityValidation"
        };

        var spanOvernight = false;

        var result = TemporaryAdjustment.validateSaveTempAdjustment(formElements, validationElements, spanOvernight);

        expect(result).toEqual(false);

        var quantityErrorVisible = $("#resource_ta_quantityValidation").is(":visible");
        expect(quantityErrorVisible).toBe(true);

        var startDateErrorVisible = $("#resource_ta_startDateValidation").is(":visible");
        expect(startDateErrorVisible).toBe(false);

        var endDateErrorVisible = $("#resource_ta_endDateValidation").is(":visible");
        expect(endDateErrorVisible).toBe(false);

        var startTimeErrorVisible = $("#resource_ta_startTimeValidation").is(":visible");
        expect(startTimeErrorVisible).toBe(false);

        var endTimeErrorVisible = $("#resource_ta_endTimeValidation").is(":visible");
        expect(endTimeErrorVisible).toBe(true);

        //Now test valid input does not result in validation errors
        $("#appt_startTime").val("08:30");
        $("#appt_endTime").val("23:59");
        $('#taQuantity').val("3");
        resourceStartTime = $("#appt_startTime").val() !== "" ? $("#appt_startTime").val() : null;
        var resourceEndTime = $("#appt_endTime").val() !== "" ? $("#appt_endTime").val() : null;

        var resource_ta_startDateTimeVal = new Date($("#appt_startDate").val() + " " + resourceStartTime);
        var resource_ta_endDateTimeVal = new Date($("#appt_endDate").val() + " " + resourceEndTime);
        var quantity = $('#taQuantity').val();

        var formElements = {
            startDate: resource_ta_startDateVal,
            endDate: resource_ta_endDateVal,
            startTime: resource_ta_startDateTimeVal,
            endTime: resource_ta_endDateTimeVal,
            quantity: quantity
        };

        var result = TemporaryAdjustment.validateSaveTempAdjustment(formElements, validationElements, spanOvernight);
        expect(result).toEqual(true);

        quantityErrorVisible = $("#resource_ta_quantityValidation").is(":visible");
        expect(quantityErrorVisible).toBe(false);

        startDateErrorVisible = $("#resource_ta_startDateValidation").is(":visible");
        expect(startDateErrorVisible).toBe(false);

        endDateErrorVisible = $("#resource_ta_endDateValidation").is(":visible");
        expect(endDateErrorVisible).toBe(false);

        startTimeErrorVisible = $("#resource_ta_startTimeValidation").is(":visible");
        expect(startTimeErrorVisible).toBe(false);

        endTimeErrorVisible = $("#resource_ta_endTimeValidation").is(":visible");
        expect(endTimeErrorVisible).toBe(false);
    });

    it('validates temporary adjustments that span overnight', function () {
        // load the home page into the DOM
        FixtureHelper.loadSourceHtmlFixtureIntoDom('resource_form.html');

        var startDate = new Date();
        $("#appt_startDate").val(startDate.toDateString());

        var endDate = new Date(startDate.toDateString());
        endDate.setDate(startDate.getDate() + 1);
        $("#appt_endDate").val(endDate.toDateString());

        $("#appt_startTime").val("13:00");
        $("#appt_endTime").val("07:00");

        var resource_ta_startDateVal = new Date($("#appt_startDate").val());
        var resource_ta_endDateVal = new Date($("#appt_endDate").val());

        var resourceStartTime = $("#appt_startTime").val() !== "" ? $("#appt_startTime").val() : null;
        var resourceEndTime = $("#appt_endTime").val() !== "" ? $("#appt_endTime").val() : null;

        var resource_ta_startDateTimeVal = new Date($("#appt_startDate").val() + " " + resourceStartTime);
        var resource_ta_endDateTimeVal = new Date($("#appt_endDate").val() + " " + resourceEndTime);
        var quantity = $('#taQuantity').val();

        var formElements = {
            startDate: resource_ta_startDateVal,
            endDate: resource_ta_endDateVal,
            startTime: resource_ta_startDateTimeVal,
            endTime: resource_ta_endDateTimeVal,
            quantity: quantity
        };

        var validationElements = {
            startDateValidation: "#resource_ta_startDateValidation",
            endDateValidation: "#resource_ta_endDateValidation",
            startTimeValidation: "#resource_ta_startTimeValidation",
            endTimeValidation: "#resource_ta_endTimeValidation",
            quantityValidation: "#resource_ta_quantityValidation"
        };

        var spanOvernight = true;

        var result = TemporaryAdjustment.validateSaveTempAdjustment(formElements, validationElements, spanOvernight);

        expect(result).toEqual(false);

        var quantityErrorVisible = $("#resource_ta_quantityValidation").is(":visible");
        expect(quantityErrorVisible).toBe(true);

        var startDateErrorVisible = $("#resource_ta_startDateValidation").is(":visible");
        expect(startDateErrorVisible).toBe(false);

        var endDateErrorVisible = $("#resource_ta_endDateValidation").is(":visible");
        expect(endDateErrorVisible).toBe(false);

        var startTimeErrorVisible = $("#resource_ta_startTimeValidation").is(":visible");
        expect(startTimeErrorVisible).toBe(false);

        var endTimeErrorVisible = $("#resource_ta_endTimeValidation").is(":visible");
        expect(endTimeErrorVisible).toBe(false);
    });
});

describe('Temporary Adjustment Table', function(){
    it("correctly displays the temporary adjustment table", function() {
        var server = UtilHelper.getFakeServer();

        SessionDataHelper.setUser(SUPER_ADMIN);

        var resourceSchedule = setupTempAdjustmentTable(server);

        var rows = $('.temporaryAdjustmentTable-tableRow');
        expect(rows.length).toBe(10);

        rows.each(function (rowIndex, row) {
            var rowData = resourceSchedule[rowIndex];
            var cells = $(row).children('td');
            expect($(cells).length).toBe(7);
            var startDayOfWeek = (new Date(rowData.startTime)).getDay();
            var endDayOfWeek = (new Date(rowData.endTime)).getDay();
            expect($(cells.get(0)).text()).toBe(dayOfWeekString[startDayOfWeek]);
            expect($(cells.get(1)).text()).toBe(new Date(rowData.startTime).format("mm/dd/yyyy, HH:MM"));
            expect($(cells.get(2)).text()).toBe(dayOfWeekString[endDayOfWeek]);
            expect($(cells.get(3)).text()).toBe(new Date(rowData.endTime).format("mm/dd/yyyy, HH:MM"));
            expect($(cells.get(4)).text()).toBe(rowData.quantity.toString());
            expect($(cells.get(5)).find('a').text()).toBe("Edit");
            expect($(cells.get(6)).find('a').text()).toBe("Delete");
        });

        //Now check that the edit and delete columns are not displayed for study staff
        SessionDataHelper.setUser(STUDY_STAFF);

        setupTempAdjustmentTable(server);

        var rows = $('.temporaryAdjustmentTable-tableRow');
        expect(rows.length).toBe(10);

        rows.each(function (rowIndex, row) {
            var rowData = resourceSchedule[rowIndex];
            var cells = $(row).children('td');
            expect($(cells).length).toBe(5);
            var startDayOfWeek = (new Date(rowData.startTime)).getDay();
            var endDayOfWeek = (new Date(rowData.endTime)).getDay();
            expect($(cells.get(0)).text()).toBe(dayOfWeekString[startDayOfWeek]);
            expect($(cells.get(1)).text()).toBe(new Date(rowData.startTime).format("mm/dd/yyyy, HH:MM"));
            expect($(cells.get(2)).text()).toBe(dayOfWeekString[endDayOfWeek]);
            expect($(cells.get(3)).text()).toBe(new Date(rowData.endTime).format("mm/dd/yyyy, HH:MM"));
            expect($(cells.get(4)).text()).toBe(rowData.quantity.toString());
        });
    });

    it('displays an editable row', function(){

        var server = UtilHelper.getFakeServer();

        SessionDataHelper.setUser(SUPER_ADMIN);

        var resourceSchedule = setupTempAdjustmentTable(server);

        var rows = $('.temporaryAdjustmentTable-tableRow');
        expect(rows.length).toBe(10);

        var rowIndex = 0;

        var url = "rest/resource/getTemporaryAdjustment?id=" + resourceSchedule[rowIndex].id;
        AjaxHelper.setupFakeServerTextResponse(server, url, JSON.stringify(resourceSchedule[rowIndex]));

        //click the edit link in the first row
        $("#temporaryAdjustmentTable-0_5").find("a").click();

        server.respond();

        var rows = $('.temporaryAdjustmentTable-tableRow');
        var row = rows.get(rowIndex);
        var rowData = resourceSchedule[rowIndex];
        var cells = $(row).children('td');
        expect($(cells).length).toBe(7);
        var startDayOfWeek = (new Date(rowData.startTime)).getDay();
        var endDayOfWeek = (new Date(rowData.endTime)).getDay();
        expect($(cells.get(0)).text()).toBe(dayOfWeekString[startDayOfWeek]);
        expect($(cells.get(1)).find(".hasDatepicker").val()).toBe(new Date(rowData.startTime).format("mm/dd/yyyy HH:MM"));
        expect($(cells.get(2)).text()).toBe(dayOfWeekString[endDayOfWeek]);
        expect($(cells.get(3)).find(".hasDatepicker").val()).toBe(new Date(rowData.endTime).format("mm/dd/yyyy HH:MM"));
        expect($(cells.get(4)).find(".numberSpinner").val()).toBe(rowData.quantity.toString());
        expect($.trim($(cells.get(5)).find('a').text())).toBe("Save");
        expect($(cells.get(5)).find('a').attr("href"))
            .toBe('javascript:TemporaryAdjustmentTable.updateTemporaryAdjustment(' + rowData.id + ', "temporaryAdjustmentTable-tableRow-0")');
        expect($.trim($(cells.get(6)).find('a').text())).toBe("Cancel");
        expect($(cells.get(6)).find('a').attr("href"))
            .toBe('javascript:TemporaryAdjustmentTable.cancelRowEdit(' + rowData.id + ', "temporaryAdjustmentTable-tableRow-0")');
    });

    function setupTempAdjustmentTable(server){

        //load the home page into the DOM
        FixtureHelper.loadSourceHtmlFixtureIntoDom('resource_form.html');

        var resource = {
            resourceId: 42,
            resourceType: "Room"
        };

        var pathToFixture = "resources/json/resource-detail.json";
        var resourceDetailString = FixtureHelper.getTestFixtureContent(pathToFixture);
        var selectedResource = JSON.parse(resourceDetailString);
        sessionStorage.setItem("resourceData", JSON.stringify(selectedResource));

        AjaxHelper.setUpFakeServerLoadSourceHtmlResponse(server, "header.html");
        AjaxHelper.setUpFakeServerLoadSourceHtmlResponse(server, "common_dialogs.html");
        AjaxHelper.setUpFakeServerLoadSourceHtmlResponse(server, "table_templates.html");
        AjaxHelper.setupFakeServerTextResponseFromTestFixture(server, "rest/app/getStaticLists", "global/json/static-lists.json");

        var pathToFixtureTemAdjustments = "resources/json/resource-schedules.json";
        var resourceScheduleString = FixtureHelper.getTestFixtureContent(pathToFixtureTemAdjustments);
        var resourceSchedule = JSON.parse(resourceScheduleString);

        var url = "rest/resource/getTemporaryAdjustments?resourceId=" + selectedResource.resourceId +
            "&page=1&maxResults=10&orderBy=ASC&sortBy=" + TemporaryAdjustmentTable.dbStartTime;

        AjaxHelper.setupFakeServerTextResponseFromTestFixture(server, url, pathToFixtureTemAdjustments);

        resourceFormPage.init();

        server.respond();
        server.respond();
        server.respond();

        //expand the temporary adjustment section
        $("#resourceTabHeading2").click();

        return resourceSchedule;
    }
});
