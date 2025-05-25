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
describe('Appointments -> Resource Timeline', function() {

    xit("displays the 12hr timeline", function()
    {
        var server = UtilHelper.getFakeServer();
        FixtureHelper.loadSourceHtmlFixtureIntoDom('appt_timeline.html');

        var startDateVal = 1487246400000;
        var endDateVal = 1487293140000;
        selectedSublocations = [1,4,5,2,3];
        selectedResourceType = 3;
        timeLineOrder = "ASC";

        var url = "rest/appointment/getOverbookedResourceDataByResourceType?startDate=" + startDateVal
            + "&endDate=" + endDateVal + "&type=" + selectedResourceType + "&selectedSublocationFilters="
            + encodeURIComponent(JSON.stringify(selectedSublocations)) + "&orderBy=" + timeLineOrder;

        var responseString = FixtureHelper.getTestFixtureContent('home/json/resource-timeline.json');

        AjaxHelper.setupFakeServerTextResponse(server, url, responseString);

        var displayId = "timelineHours12";
        var classId = "timeline12";
        var dateString = "02/16/2017";
        $('#timeline_date').val(dateString);
        startDate = new Date(dateString);
        timelineHours = 12;
        startHour = 7;
        startDate.setHours(startHour, 0, 0, 0);

        //12hr
        doTimeline(timelineHours, startHour, displayId, classId, selectedResourceType);

        server.respond();

        expect($(".timelineHour").length).toBe(13);
        expect($($(".timelineHour").get(0)).text()).toBe("07");
        expect($($(".timelineHour").get(12)).text()).toBe("19");

        expect($(".availableTime").length).toBe(4);
        expect($($(".availableTime").get(0)).text()).toBe("THU: 07:30 - 16:00");

        expect($(".timelineStrip").length).toBe(2);
        var tooltipHtml = $($(".timelineStrip").get(1)).html();
        expect(tooltipHtml).toContain('data-visithoverstarttime="01/27/2017 22:00"');
        expect(tooltipHtml).toContain('data-visithoverendtime="03/07/2017 01:00"');
        expect(tooltipHtml).toContain('data-resourcehoverstarttime="01/27/2017 22:00"');
        expect(tooltipHtml).toContain('data-resourcehoverendtime="03/07/2017 01:00"');
    });

   xit("displays the 24Hr timeline", function()
    {
        var server = UtilHelper.getFakeServer();
        FixtureHelper.loadSourceHtmlFixtureIntoDom('appt_timeline.html');

        var startDateVal = 1487221200000;
        var endDateVal = 1487307540000;
        selectedSublocations = [1,4,5,2,3];
        selectedResourceType = 3;
        timeLineOrder = "ASC";

        var url = "rest/appointment/getOverbookedResourceDataByResourceType?startDate=" + startDateVal
            + "&endDate=" + endDateVal + "&type=" + selectedResourceType + "&selectedSublocationFilters="
            + JSON.stringify(selectedSublocations) + "&orderBy=" + timeLineOrder;

        var responseString = FixtureHelper.getTestFixtureContent('home/json/resource-timeline.json');

        AjaxHelper.setupFakeServerTextResponse(server, url, responseString);

        var displayId = "timelineHours24";
        var classId = "timeline24";
        var dateString = "02/16/2017";
        $('#timeline_date').val(dateString);
        startDate = new Date(dateString);
        timelineHours = 23;
        startHour = 0;
        startDate.setHours(startHour, 0, 0, 0);

        doTimeline(timelineHours, startHour, displayId, classId, selectedResourceType);

        server.respond();

        expect($(".timelineHour").length).toBe(24);
        expect($($(".timelineHour").get(0)).text()).toBe("00");
        expect($($(".timelineHour").get(23)).text()).toBe("23");

        expect($(".availableTime").length).toBe(4);
        expect($($(".availableTime").get(0)).text()).toBe("THU: 07:30 - 16:00");

        expect($(".timelineStrip").length).toBe(2);
        var tooltipHtml = $($(".timelineStrip").get(1)).html();
        expect(tooltipHtml).toContain('data-visithoverstarttime="01/27/2017 22:00"');
        expect(tooltipHtml).toContain('data-visithoverendtime="03/07/2017 01:00"');
        expect(tooltipHtml).toContain('data-resourcehoverstarttime="01/27/2017 22:00"');
        expect(tooltipHtml).toContain('data-resourcehoverendtime="03/07/2017 01:00"');
    });

    xit("displays the weekly timeline", function()
    {
        var server = UtilHelper.getFakeServer();
        FixtureHelper.loadSourceHtmlFixtureIntoDom('appt_timeline.html');

        var startDateVal = 1486875600000;
        var endDateVal = 1487480340000;
        selectedSublocations = [1,4,5,2,3];
        selectedResourceType = 3;
        timeLineOrder = "ASC";

        var url = "rest/appointment/getOverbookedResourceDataByResourceType?startDate=" + startDateVal
            + "&endDate=" + endDateVal + "&type=" + selectedResourceType + "&selectedSublocationFilters="
            + JSON.stringify(selectedSublocations) + "&orderBy=" + timeLineOrder;

        var responseString = FixtureHelper.getTestFixtureContent('home/json/resource-timeline.json');

        AjaxHelper.setupFakeServerTextResponse(server, url, responseString);

        var displayId = "timelineHoursWeek";
        var classId = "timelineWeek";
        var dateString = "02/16/2017";
        $('#timeline_date').val(dateString);
        startDate = new Date(dateString);
        timelineHours = 167;
        startHour = 0;
        startDate.setHours(startHour, 0, 0, 0);

        doTimeline(timelineHours, startHour, displayId, classId, selectedResourceType);

        server.respond();

        expect($(".timelineHour").length).toBe(0);
        expect($(".timelineDay").length).toBe(7);
        expect($(".timelineDayHours").length).toBe(21);
        expect($($(".timelineDayHours").get(0)).text()).toBe("00-08");
        expect($($(".timelineDayHours").get(20)).text()).toBe("16-00");

        expect($(".availableTime").length).toBe(4);
        expect($($(".availableTime").get(0)).text()).toBe("THU: 07:30 - 16:00");

        expect($(".timelineStrip").length).toBe(2);

        var tooltipHtml = $($(".timelineStrip").get(1)).html();
        expect(tooltipHtml).toContain('data-visithoverstarttime="01/27/2017 22:00"');
        expect(tooltipHtml).toContain('data-visithoverendtime="03/07/2017 01:00"');
        expect(tooltipHtml).toContain('data-resourcehoverstarttime="01/27/2017 22:00"');
        expect(tooltipHtml).toContain('data-resourcehoverendtime="03/07/2017 01:00"');
    });
});

describe('timeline.js sundayBeforeOrEqualDate()', function() {

    // NB: month param starts at 0/Jan
    it("finds Sunday <= input date", function(){
        var sunday27aug17 = new Date(2017, 7, 27);
        var wednesday30aug17 = new Date(2017, 7, 30);
        var saturday2sep17 = new Date(2017, 8, 2);
        var sunday3sep17 = new Date(2017, 8, 3);

        var previousSunday = sundayBeforeOrEqualDate(saturday2sep17);

        expect(sundayBeforeOrEqualDate(sunday27aug17)).toEqual(sunday27aug17);
        expect(sundayBeforeOrEqualDate(wednesday30aug17)).toEqual(sunday27aug17);
        expect(sundayBeforeOrEqualDate(saturday2sep17)).toEqual(sunday27aug17);

        expect(sundayBeforeOrEqualDate(sunday3sep17)).toEqual(sunday3sep17);
    });
});
