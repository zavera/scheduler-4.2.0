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

describe('appt_calendar.js:reCreateCalendar()', function() {

    beforeEach(function () {
        this.spy_dollar = spyOn(window, "$").and.callThrough();
        this.spy_fullCalendar = spyOn($.fn, "fullCalendar");
        this.spy_css = spyOn($.fn, "css");
        this.spy_datepicker =
            spyOn($.fn, "datepicker").and.returnValue('bar0');

        this.spy_util_showMainMessage = spyOn(window, "util_showMainMessage");

        this.spy_apptCalendarViewRenderFactory =
            spyOn(window, "apptCalendarViewRenderFactory").and.returnValue('bar1');
        this.spy_util_makeCalendarEvents =
            spyOn(window, "makeCalendarEvents").and.returnValue('bar2');

        this.data = 'foo';
    });

    it("calls several things on the 'yes' path", function() {

        view = 'bar';
        todaysVisits = 'baz';
        inpatient_search = true;

        var bar1 = 'bar1';
        var bar2 = 'bar2';

        var fullOptions = {
            aspectRatio:1,
            theme:true,
            header:{
                "left":"prev,next today",
                "center":"title",
                "right":"month,agendaWeek,agendaDay"
            },
            views:{
                month:{
                    titleFormat:"MMMM YYYY",
                    columnFormat:"dddd"},
                week:{
                    titleFormat:"MMM D YYYY",
                    columnFormat:"ddd M/D"
                },
                day:{
                    titleFormat:"dddd, MMMM D, YYYY",
                    columnFormat:"dddd, MMMM D"
                }
            },
            defaultView:"agendaDay",
            nowIndicator: true,
            firstHour:7,
            slotLabelFormat:"HH:mm",
            editable:false,
            ignoreTimezone:true,
            nextDayThreshold:"00:00:00",
            displayEventEnd:true,
            selectable:true,
            selectHelper:true,
            lazyFetching:true,
            viewRender: bar1,
            events: bar2,
            allDayDefault: todaysVisits,
            loading: apptCalendarLoading,
            select: apptCalendarSelect,
            eventAfterRender: apptCalendarEventAfterRender,
            eventRender: apptCalendarEventRender,
            windowResize: apptCalendarWindowResize,
            eventMouseover: apptCalendarEventMouseover,
            eventMouseout: apptCalenderEventMouseout,
            eventClick: AppointmentCalendar.apptCalendarEventClick,
            eventDataTransform: AppointmentCalendar.eventTransform
        };

        reCreateCalendar(this.data);

        expect(this.spy_dollar).toHaveBeenCalledTimes(7);
        var index = 0;
        expect(this.spy_dollar.calls.argsFor(index++)).toEqual(['#appt_calendar']);
        expect(this.spy_dollar.calls.argsFor(index++)).toEqual(['#loading']);
        expect(this.spy_dollar.calls.argsFor(index++)).toEqual(['#filter_loading']);
        expect(this.spy_dollar.calls.argsFor(index++)).toEqual(['#initial_loading']);
        expect(this.spy_dollar.calls.argsFor(index++)).toEqual(['#appt_calendar']);
        expect(this.spy_dollar.calls.argsFor(index++)).toEqual(['#appt_calendar']);
        expect(this.spy_dollar.calls.argsFor(index++)).toEqual(['#apptCalendarGoToDate']);

        expect(this.spy_fullCalendar).toHaveBeenCalledTimes(3);
        expect(this.spy_fullCalendar.calls.argsFor(0)).toEqual(['destroy']);
        expect(this.spy_fullCalendar.calls.argsFor(1)).toEqual([fullOptions]);
        expect(this.spy_fullCalendar.calls.argsFor(2)).toEqual(['gotoDate', 'bar0']);

        expect(this.spy_css).toHaveBeenCalledTimes(3);
        expect(this.spy_css.calls.argsFor(0)).toEqual([{display: 'none'}]);
        expect(this.spy_css.calls.argsFor(1)).toEqual([{display: 'none'}]);
        expect(this.spy_css.calls.argsFor(2)).toEqual([{display: 'none'}]);

        expect(this.spy_datepicker).toHaveBeenCalledTimes(1);
        expect(this.spy_datepicker.calls.argsFor(0)).toEqual(['getDate']);

        expect(this.spy_util_showMainMessage).toHaveBeenCalledTimes(1);
        expect(this.spy_util_showMainMessage.calls.argsFor(0)).toEqual(['Appointment(s) found. Scroll through the calendar to select and schedule an appointment.']);

        expect(this.spy_apptCalendarViewRenderFactory).toHaveBeenCalledTimes(1);
        expect(this.spy_apptCalendarViewRenderFactory.calls.argsFor(0)).toEqual([todaysVisits]);

        expect(this.spy_util_makeCalendarEvents).toHaveBeenCalledTimes(1);
        expect(this.spy_util_makeCalendarEvents.calls.argsFor(0)).toEqual([this.data]);
    });

    it("calls couple of dollars at end, if view is null", function() {

        view = null;
        this.spy_dollar.calls.reset();
        reCreateCalendar(this.data);
        expect(this.spy_dollar).toHaveBeenCalledTimes(5);
    });

    it("calls fewer things on the 'no' path", function() {

        this.spy_dollar.calls.reset();

        view = null;
        todaysVisits = false;
        inpatient_search = false;

        var bar1 = 'bar1';
        var bar2 = 'bar2';


        var fullOptions = {
            aspectRatio: 1,
            theme: true,
            header: {
                left: 'prev,next today',
                center: 'title',
                right: 'month,agendaWeek,agendaDay'
            },
            views : {
                month: {
                    titleFormat: 'MMMM YYYY', // September 2009
                    columnFormat: 'dddd'   // Mon
                },
                week: {
                    titleFormat: "MMM D YYYY", // Sep 7 - 13 2009
                    columnFormat: 'ddd M/D' // Mon 9/7
                },
                day: {
                    titleFormat:  'dddd, MMMM D, YYYY',  // Tuesday, Sep 8, 2009
                    columnFormat:  'dddd, MMMM D'  // Monday 9/7
                }
            },
            defaultView: 'month',
            nowIndicator: true,
            firstHour: 7,
            slotLabelFormat:MILITARY_TIME_FORMAT,
            editable: false,
            ignoreTimezone: true,
            nextDayThreshold: "00:00:00",
            displayEventEnd: true,
            selectable: true,
            selectHelper: true,
            lazyFetching: true,
            loading: apptCalendarLoading,
            select: apptCalendarSelect,
            viewRender: bar1,
            eventAfterRender: apptCalendarEventAfterRender,
            eventRender: apptCalendarEventRender,
            windowResize: apptCalendarWindowResize,
            eventMouseover: apptCalendarEventMouseover,
            eventMouseout: apptCalenderEventMouseout,
            events: bar2,
            allDayDefault: todaysVisits,
            eventClick: AppointmentCalendar.apptCalendarEventClick,
            eventDataTransform: AppointmentCalendar.eventTransform
        };

        reCreateCalendar(this.data);

        expect(this.spy_dollar).toHaveBeenCalledTimes(5);
        var index = 0;
        expect(this.spy_dollar.calls.argsFor(index++)).toEqual(['#appt_calendar']);
        expect(this.spy_dollar.calls.argsFor(index++)).toEqual(['#loading']);
        expect(this.spy_dollar.calls.argsFor(index++)).toEqual(['#filter_loading']);
        expect(this.spy_dollar.calls.argsFor(index++)).toEqual(['#initial_loading']);
        expect(this.spy_dollar.calls.argsFor(index++)).toEqual(['#appt_calendar']);

        expect(this.spy_fullCalendar).toHaveBeenCalledTimes(2);
        expect(this.spy_fullCalendar.calls.argsFor(0)).toEqual(['destroy']);
        expect(this.spy_fullCalendar.calls.argsFor(1)).toEqual([fullOptions]);

        expect(this.spy_css).toHaveBeenCalledTimes(3);
        expect(this.spy_css.calls.argsFor(0)).toEqual([{display: 'none'}]);
        expect(this.spy_css.calls.argsFor(1)).toEqual([{display: 'none'}]);
        expect(this.spy_css.calls.argsFor(2)).toEqual([{display: 'none'}]);

        expect(this.spy_datepicker).toHaveBeenCalledTimes(0);

        expect(this.spy_util_showMainMessage).toHaveBeenCalledTimes(0);

        expect(this.spy_apptCalendarViewRenderFactory).toHaveBeenCalledTimes(1);
        expect(this.spy_apptCalendarViewRenderFactory.calls.argsFor(0)).toEqual([todaysVisits]);

        expect(this.spy_util_makeCalendarEvents).toHaveBeenCalledTimes(1);
        expect(this.spy_util_makeCalendarEvents.calls.argsFor(0)).toEqual([this.data]);
    });

    it("filter the calendar", function()
    {
        this.spy_dollar.and.callThrough();
        this.spy_fullCalendar.and.callThrough();
        this.spy_css.and.callThrough();
        this.spy_datepicker.and.callThrough();

        this.spy_util_showMainMessage.and.callThrough();

        this.spy_apptCalendarViewRenderFactory.and.callThrough();
        this.spy_util_makeCalendarEvents.and.callThrough();

        var server = UtilHelper.getFakeServer();
        AppointmentTestHelpers.setupAppointmentPage(server);

        var url = "rest/appointment/getCalendarBookedVisits?starting=1485925200000&ending=1488344399059" +
            "&todayVisits=false&filterString=&filterId=6";
        $('#appt_calendar').fullCalendar();

        calendarSelectedFilterId = 6;
        calendarFilterString = "searchText";
        sublocations = [
            {id: 1, institution: {id: 1, longName: "Demo Institution", name: "Demo1"}, name:"Sub-Location 1"},
            {id: 2, institution: {id: 1, longName: "Demo Institution", name: "Demo1"}, name:"Sub-Location 3"},
            {id: 5, institution: {id: 1, longName: "Demo Institution", name: "Demo1"}, name:"Sub-Location 5"}
        ];
        var result = buildSubLocTabs("subLocTop");
        $("#apptSublocationListTop").html(result.html);
        $("#apptSublocationListBottom").html(result.html);

        $("#apptSublocationListTop").tabs();
        $("#apptSublocationListBottom").tabs();

        var activeSublocationTabTop = $("#apptSublocationListTop").tabs("option", "active");
        expect(activeSublocationTabTop).toBe(0);
        var activeSublocationTabBottom = $("#apptSublocationListTop").tabs("option", "active");
        expect(activeSublocationTabBottom).toBe(0);

        var responseString = FixtureHelper.getTestFixtureContent('home/json/calendar-filter-data.json');
        AjaxHelper.setupFakeServerTextResponse(server, url, responseString);

        filterCalendarViewClick();

        server.respond();

        activeSublocationTabTop = $("#apptSublocationListTop").tabs("option", "active");
        expect(activeSublocationTabTop).toBe(0);
        activeSublocationTabTop = $("#apptSublocationListBottom").tabs("option", "active");
        expect(activeSublocationTabBottom).toBe(0);
    });
});

describe("Appointment Calendar Mouse Events", function(){
    it("Mouse out event: apptCalenderEventMouseout()", function(){
       FixtureHelper.loadTestHtmlFixtureIntoDom("home/html/calendar.html");

       var tooltipClass="appointmentHover";
       var calDiv = $("#calendar");
       calDiv.css("position", "absolute");

       expect($("." + tooltipClass).length).toBe(1);
       apptCalenderEventMouseout.call(calDiv);

       expect(calDiv.css("z-index")).toBe('8');
       expect($("." + tooltipClass).length).toBe(0);
    });

    it("Mouse over event: apptCalendarEventMouseover()", function(){
        FixtureHelper.loadTestHtmlFixtureIntoDom("home/html/calendar.html");

        var server = UtilHelper.getFakeServer();

        var calEvent = {
            id: 34,
            localId: 1012,
            piName: "Foo Barlast",
            visitName: "Visit 1"
        };

        var roomData = {
            "successful": true,
            "message": "Room 2"
        };
        var tooltipClass="appointmentHover";
        $("." + tooltipClass).remove();
        expect($("." + tooltipClass).length).toBe(0);

        var calDiv = $("#calendar");
        var roomString = JSON.stringify(roomData);
        var findRoomStringURL = "rest/appointment/getRoomString?visitId=34";
        AjaxHelper.setupFakeServerTextResponse(server, findRoomStringURL, roomString);

        apptCalendarEventMouseover.call(calDiv, calEvent);

        server.respond();

        var mouseOverEvent = jQuery.Event("mouseover");
        mouseOverEvent.pageX = 200;
        mouseOverEvent.pageY = 250;
        calDiv.trigger(mouseOverEvent);
        server.respond();

        expect($("." + tooltipClass).length).toBe(1);


        var hoverItems = $(".appointmentHoverItem");
        expect(hoverItems.length).toBe(4);

        var hoverLabels = $(".appointmentHoverLabel");
        expect(hoverLabels.length).toBe(4);

        expect($(hoverLabels[0]).text()).toBe('Local ID:');
        expect($(hoverItems[0]).text()).toBe(calEvent.localId.toString());

        expect($(hoverLabels[1]).text()).toBe('PI Name:');
        expect($(hoverItems[1]).text()).toBe(calEvent.piName);

        expect($(hoverLabels[2]).text()).toBe('Visit Name:');
        expect($(hoverItems[2]).text()).toBe(calEvent.visitName);

        expect($(hoverLabels[3]).text()).toBe('Room(s):');
        expect($(hoverItems[3]).text()).toBe(calEvent.room);

        var apptHover = $(".appointmentHover");
        $(".appointmentHover").css("position", "absolute");
        expect($(".appointmentHover").length).toBe(1);
        expect($(".appointmentHover").css("top")).toBe(mouseOverEvent.pageY + 10 + "px");
        expect($(".appointmentHover").css("left")).toBe(mouseOverEvent.pageX + 20 + "px");
        expect($(".appointmentHover").css("z-index")).toBe("10001");
    });
});
