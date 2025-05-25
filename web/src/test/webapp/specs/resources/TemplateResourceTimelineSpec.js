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

describe('template_resource_timeline.js:loadTemplateResourcesData()', function () {

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

    it("calls several things on the 'yes' path", function () {

        view = 'bar';
        todaysVisits = 'baz';
        inpatient_search = true;

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
            defaultView: 'agendaDay',
            nowIndicator: true,
            firstHour: 7,
            slotLabelFormat: MILITARY_TIME_FORMAT,
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

    it("calls couple of dollars at end, if view is null", function () {

        view = null;
        this.spy_dollar.calls.reset();
        reCreateCalendar(this.data);
        expect(this.spy_dollar).toHaveBeenCalledTimes(5);
    });

    it("calls fewer things on the 'no' path", function () {

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
            slotLabelFormat: MILITARY_TIME_FORMAT,
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
});

describe('template_resource_timeline.js:TRT helpers for loadTemplateResourcesData()', function () {
    it("loadTemplateResourcesData_eventDrop", function () {
        var spy_revertFunc = jasmine.createSpy('spy_revertFunc');
        var spy_eventDragDropResize = spyOn(window, "eventDragDropResize");

        selectedEventResourceId = 42;
        var event = {resourceId: 43};
        TRT.loadTemplateResourcesData_eventDrop(event, "", spy_revertFunc);

        expect(spy_revertFunc).toHaveBeenCalledTimes(1);

        expect(spy_eventDragDropResize).toHaveBeenCalledTimes(1);
        expect(spy_eventDragDropResize.calls.argsFor(0)).toEqual([event, spy_revertFunc]);

        selectedEventResourceId = 43;
        spy_eventDragDropResize.calls.reset();
        spy_revertFunc.calls.reset();
        TRT.loadTemplateResourcesData_eventDrop(event, "", spy_revertFunc);

        expect(spy_revertFunc).toHaveBeenCalledTimes(0);

        expect(spy_eventDragDropResize).toHaveBeenCalledTimes(1);
        expect(spy_eventDragDropResize.calls.argsFor(0)).toEqual([event, spy_revertFunc]);
    });
    it("loadTemplateResourcesData_eventResize", function () {

        var spy_eventDragDropResize = spyOn(window, "eventDragDropResize");

        TRT.loadTemplateResourcesData_eventResize("foo", null, "bar", null, null, null);

        expect(spy_eventDragDropResize).toHaveBeenCalledTimes(1);
        expect(spy_eventDragDropResize.calls.argsFor(0)).toEqual(["foo", "bar"]);
    });
    it("loadTemplateResourcesData_eventDragStart", function () {

        selectedEventResourceId = 1;
        var event = {resourceId: 42};

        TRT.loadTemplateResourcesData_eventDragStart(event);

        expect(selectedEventResourceId).toEqual(42);
    });
    it("loadTemplateResourcesData_eventMouseover", function () {

        var spy_closeAllHovers = spyOn(window, "closeAllHovers");
        var spy_convertMinutesToHrMins = spyOn(window, "convertMinutesToHrMins").and.returnValue('foo');
        var spy_yesNoValue = spyOn(window, "yesNoValue").and.returnValue('maybe');
        var spy_showValue = spyOn(window, "showValue").and.callThrough();

        var spy_dollar = spyOn(window, "$").and.callThrough();
        var spy_append = spyOn($.fn, "append");
        var spy_height = spyOn($.fn, "height").and.returnValue(10);
        var spy_css = spyOn($.fn, "css");
        var spy_remove = spyOn($.fn, "remove");

        var jsEvent = {
            pageY: 500,
            pageX: 600
        };
        var calEvent = {
            color: "#D8BFD8",
            floatable: true,
            displayFloatStart: 'displayFloatStart',
            displayFloatEnd: 'displayFloatEnd',
            displayStart: 'displayStart',
            displayEnd: 'displayEnd',
            duration: 'duration',
            alternate: 'alternate',
            billable: 'billable',
            annotations: 'a1,a2,a3'
        };

        var expectedHoverFloatText =
            "<span class='appointmentHoverLabel'>Float Start Time</span>\
                                 <span class='appointmentHoverItem'>displayFloatStart</span>\
                                 <span class='appointmentHoverLabel'>Float End Time</span>\
                                 <span class='appointmentHoverItem'>displayFloatEnd</span>";
        var expectedToolTip1 =
            "<div id='timelineTooltip' class='appointmentHover' style='z-index:10001; left:" +
            "450px; top:450px; border: solid 5px " + "#662E91" + ";'>\
                                 <span class='appointmentHoverLabel'>Start Time</span>\
                                 <span class='appointmentHoverItem'>displayStart</span>\
                                 <span class='appointmentHoverLabel'>End Time</span>\
                                 <span class='appointmentHoverItem'>displayEnd</span>";

        var expectedToolTip2 =
            " <span class='appointmentHoverLabel'>Duration</span> \
                                <span class='appointmentHoverItem'>foo</span>\
                                             <span class='appointmentHoverLabel'>Standard Alternate?</span> \
                                             <span class='appointmentHoverItem'>maybe</span>\
                                             <span class='appointmentHoverLabel'>Billable?</span> \
                                             <span class='appointmentHoverItem'>maybe</span>\
                                             <span class='appointmentHoverAnnotations'></span>\
                                             <span class='appointmentHoverAnnotations'>" +
            'a1<br />a2<br />a3' + "</span>\
                                 </div>";

        var helper1 = function () {
            spy_append.calls.reset();
            var expectedToolTip = expectedToolTip1 + expectedHoverFloatText + expectedToolTip2;

            TRT.loadTemplateResourcesData_eventMouseover(calEvent, jsEvent);

            expect(spy_append).toHaveBeenCalledTimes(2);
            var actualToolTip = spy_append.calls.argsFor(0)[0];

            actualToolTip = UtilHelper.wsCollapse(actualToolTip);
            expectedToolTip = UtilHelper.wsCollapse(expectedToolTip);

            expect(actualToolTip).toEqual(expectedToolTip);

            return expectedToolTip;
        }
        var toolTip1 = helper1();

        expect(spy_closeAllHovers).toHaveBeenCalledTimes(1);

        expect(spy_convertMinutesToHrMins).toHaveBeenCalledTimes(1);
        expect(spy_convertMinutesToHrMins.calls.argsFor(0)).toEqual(['duration']);

        expect(spy_yesNoValue).toHaveBeenCalledTimes(2);
        expect(spy_yesNoValue.calls.argsFor(0)).toEqual(['alternate']);
        expect(spy_yesNoValue.calls.argsFor(1)).toEqual(['billable']);

        expect(spy_showValue).toHaveBeenCalledTimes(1);
        expect(spy_showValue.calls.argsFor(0)).toEqual(['a1,a2,a3']);

        expect(spy_dollar).toHaveBeenCalledTimes(5);
        expect(spy_dollar.calls.argsFor(0)).toEqual(['body']);
        expect(spy_dollar.calls.argsFor(1)).toEqual(['#timelineTooltip']);
        expect(spy_dollar.calls.argsFor(2)).toEqual(['.appointmentHover']);
        expect(spy_dollar.calls.argsFor(3)).toEqual(['.addStyleRule']);
        expect(spy_dollar.calls.argsFor(4)).toEqual(['body']);

        expect(spy_append).toHaveBeenCalledTimes(2);
        var arg0ForAppend = UtilHelper.wsCollapse(spy_append.calls.argsFor(0)[0]);
        expect(arg0ForAppend).toEqual(toolTip1);
        expect(spy_append.calls.argsFor(1)).toEqual(['<style class="addStyleRule">.appointmentHover:after{border-top: 10px solid ' +
        '#662E91' + ' !important;}</style>']);
        expect(spy_height).toHaveBeenCalledTimes(1);

        expect(spy_css).toHaveBeenCalledTimes(1);
        expect(spy_css.calls.argsFor(0)).toEqual(['top', 440]);

        expect(spy_remove).toHaveBeenCalledTimes(1);


        calEvent.floatable = false;
        expectedHoverFloatText = "";
        expectedToolTip = expectedToolTip1 + expectedHoverFloatText + expectedToolTip2;

        helper1();

        expectedToolTip1 = expectedToolTip1.replace("#662E91", 'foo');
        calEvent.color = 'foo';

        helper1();
    });

    it("loadTemplateResourcesData_eventMouseout", function () {

        var spy_dollar = spyOn(window, "$").and.callThrough();
        var spy_css = spyOn($.fn, "css");
        var spy_remove = spyOn($.fn, "remove");

        TRT.loadTemplateResourcesData_eventMouseout(null, null);

        expect(spy_dollar).toHaveBeenCalledTimes(2);
        // not sure how to handle $(this) here
        expect(spy_dollar.calls.argsFor(1)).toEqual(['.appointmentHover']);

        expect(spy_css).toHaveBeenCalledTimes(1);
        expect(spy_css.calls.argsFor(0)).toEqual(['z-index', 8]);

        expect(spy_remove).toHaveBeenCalledTimes(1);
    });

    it("loadTemplateResourcesData_eventClick", function () {

        var spy_dollar = spyOn(window, "$").and.callThrough();
        var spy_remove = spyOn($.fn, "remove");
        var spy_append = spyOn($.fn, "append");

        var spy_closeAllHovers = spyOn(window, "closeAllHovers");

        user = {institutionRole: {id: STUDY_STAFF}};
        TRT.loadTemplateResourcesData_eventClick(null, null, null);

        expect(spy_dollar).toHaveBeenCalledTimes(1);
        expect(spy_dollar.calls.argsFor(0)).toEqual(['.appointmentHover']);

        expect(spy_remove).toHaveBeenCalledTimes(1);

        expect(spy_closeAllHovers).toHaveBeenCalledTimes(1);

        expect(spy_append).toHaveBeenCalledTimes(0);

        user.institutionRole.id = 'foo';
        var helper1 = function (calEvent,
                                expectedWidth,
                                expectedBorderColor,
                                expectedIsFlexGrouped,
                                expectedIsFloatGrouped,
                                expectedUnlinkOrLinkAction) {

            spy_append.calls.reset();
            spy_dollar.calls.reset();
            spy_remove.calls.reset();

            calEvent.id = 42;

            var jsEvent = {
                pageX: 770,
                pageY: 870
            };

            var expectedTooltip =
                "<div class='eventClickActions' style='z-index:10001; width: " +
                expectedWidth +
                "px; left:" + (jsEvent.pageX - 70) + "px; top:" +
                (jsEvent.pageY - 70) + "px; border: solid 5px " +
                expectedBorderColor + ";'> " +
                " <img src='css/images/gantt/icon_papers.jpg' title='Copy Resource' style='height: 20px;' " +
                "onclick='copyTemplateResourceConfirmDialog(event)'/> &nbsp; " +
                " <img src='css/images/gantt/icon_trash.jpg' title='Delete Resource' onclick='deleteTemplateResourceConfirmDialog(event," +
                expectedIsFlexGrouped + "," +
                expectedIsFloatGrouped + ")'/> &nbsp; " +
                " <img src='css/images/gantt/icon_pencil.jpg' title='Edit Resource' onclick='editTemplateResourceConfirmDialog(event)'/> &nbsp; " +
                expectedUnlinkOrLinkAction +
                "</div>"

            TRT.loadTemplateResourcesData_eventClick(calEvent, jsEvent, null);

            expect(spy_dollar).toHaveBeenCalledTimes(4);
            expect(spy_dollar.calls.argsFor(1)).toEqual(['body']);
            expect(spy_dollar.calls.argsFor(2)).toEqual(['.addEventStyleRule']);
            expect(spy_dollar.calls.argsFor(3)).toEqual(['body']);

            expect(spy_append).toHaveBeenCalledTimes(2);
            UtilHelper.expectEqualCollapse(spy_append.calls.argsFor(0)[0], expectedTooltip);
            expect(spy_append.calls.argsFor(1)).toEqual(['<style class="addEventStyleRule">.eventClickActions:after{border-top: 10px solid ' +
            expectedBorderColor + ' !important;}</style>']);

            expect(spy_remove).toHaveBeenCalledTimes(2);
        }

        var calEvent = {
            floatable: true,
            flexible: true,
            groupId: 42,
            color: "#D8BFD8"
        };
        helper1(calEvent, 110, "#662E91", true, true,
            "<img src='css/images/gantt/icon_unlink.png' title='Unlink Resource' onclick='unlinkIndividualResource()'/>");

        calEvent.flexible = false;
        calEvent.floatable = false;

        helper1(calEvent, 75, "#662E91", false, false,
            "<img src='css/images/gantt/icon_unlink.png' title='Unlink Resource' onclick='unlinkIndividualResource()'/>");

        calEvent.groupId = undefined;
        helper1(calEvent, 75, "#662E91", false, false, "")

        calEvent.floatable = true;
        helper1(calEvent, 110, "#662E91", false, false, "<img src='css/images/gantt/icon_link.png' title='Link Resource' onclick='openLinkResourcesDialog(true)'/>")
    });

    it("loadTemplateResourcesData_eventAfterRender", function () {

        var floatStartPreferred = new Date("01/13/2010 01:40");
        var floatEndPreferred = new Date("01/15/2010 01:40");
        var floatStartDate = new Date("01/13/2010 01:45");
        var floatEndDate = new Date("01/15/2010 01:35");

        daySelected = floatEndPreferred.getDate();

        var spy_addDays = spyOn(Date.prototype, "addDays").and.callFake(function (days) {
            var date = new Date(this);  // in the original, this.valueOf() doesn't work
            date.setDate(date.getDate() + days);
            return date;
        });
        var spy_date = spyOn(window, "Date").and.callFake(function (arg) {
            return arg;
        });

        var spy_dollar = spyOn(window, "$").and.callThrough();

        var spy_css = jasmine.createSpy('spy_css');
        var spy_find = spyOn($.fn, "find").and.returnValue({css: spy_css});


        TRT.loadTemplateResourcesData_eventAfterRender({floatable: false}, null, null);

        expect(spy_date).toHaveBeenCalledTimes(0);
        expect(spy_dollar).toHaveBeenCalledTimes(0);
        expect(spy_css).toHaveBeenCalledTimes(0);
        expect(spy_find).toHaveBeenCalledTimes(0);

        totalResources = 42;
        var myEvent =
            {
                floatable: true,
                floatStartPreferred: floatStartPreferred,
                floatEndPreferred: floatEndDate,
                floatStartDate: floatStartDate,
                floatEndDate: floatEndDate
            };

        TRT.loadTemplateResourcesData_eventAfterRender(myEvent, null, null);

        expect(spy_date).toHaveBeenCalledTimes(9); // 7 explicit, 2 via addDays
        expect(spy_dollar).toHaveBeenCalledTimes(1);
        expect(spy_css).toHaveBeenCalledTimes(4);

        expect(spy_css.calls.argsFor(0)).toEqual(["left", "0px"]);
        expect(spy_css.calls.argsFor(1)).toEqual(["width", "126.66666666666666px"]);
        expect(totalResources).toBe(42);

        console.log("================================");

        daySelected = floatStartPreferred.getDate() + 1;
        spy_css.calls.reset();
        TRT.loadTemplateResourcesData_eventAfterRender(myEvent, null, null);

        expect(spy_css.calls.argsFor(0)).toEqual(["left", "-1905.6000000000001px"]);
        expect(spy_css.calls.argsFor(1)).toEqual(["width", "126.66666666666666px"]);
        expect(totalResources).toBe(41);
    });

    it("loadTemplateResourcesData_render", function () {

        var spy_isNotStudyStaff = spyOn(window, "isNotStudyStaff");
        var spy_append = jasmine.createSpy('spy_append');

        var myResource = {
            templateResourceId: 1,
            resourceName: 'foo',
            groupId: 42,
            groupComboDisplay: 'combo'
        };
        var myEvent = {
            append: spy_append
        };
        TRT.loadTemplateResourcesData_render(myResource, myEvent);

        expect(spy_append).toHaveBeenCalledTimes(1);
        expect(spy_append.calls.argsFor(0)).toEqual([' <img src="css/images/gantt/icon_info.png" ' +
        'style="vertical-align: middle;" border="0" onmouseenter="onResourceMouseEnter(event, this, 1)" onmouseout="onResourceMouseOut();">']);

        myResource.resourceName = undefined;
        spy_append.calls.reset();
        spy_isNotStudyStaff.and.returnValue(true);
        TRT.loadTemplateResourcesData_render(myResource, myEvent);

        expect(spy_append).toHaveBeenCalledTimes(1);
        UtilHelper.expectEqualCollapse(spy_append.calls.argsFor(0)[0], '<span onclick="unlinkGroupResources(\'' + '42' + '\',\'' + 'combo' +
            '\')"> <img src="css/images/gantt/unlink_group_resources.jpg" id="templateResourceFloatGroupContainerImg1"/> ' +
            '<span style="margin: 0; color: #0A507D;">Unlink Group Resources</span> </span>');

        spy_append.calls.reset();
        spy_isNotStudyStaff.and.returnValue(false);
        TRT.loadTemplateResourcesData_render(myResource, myEvent);
        expect(spy_append).toHaveBeenCalledTimes(0);
    });

    it("loadTemplateResourcesData_loading", function () {

        var spy_blockUI = spyOn($, "blockUI");
        var spy_unblockUI = spyOn($, "unblockUI");

        TRT.loadTemplateResourcesData_loading(true);

        expect(spy_blockUI).toHaveBeenCalledTimes(1);
        expect(spy_blockUI.calls.argsFor(0)).toEqual([{fadeIn: 0}]);

        expect(spy_unblockUI).toHaveBeenCalledTimes(0);

        spy_blockUI.calls.reset();
        spy_unblockUI.calls.reset();
        TRT.loadTemplateResourcesData_loading(false);

        expect(spy_blockUI).toHaveBeenCalledTimes(0);
        expect(spy_unblockUI).toHaveBeenCalledTimes(1);
    });

    it("loadTemplateResourcesData", function () {

        var spy_dollar = spyOn(window, "$").and.callThrough();
        var spy_fullCalendar = spyOn($.fn, "fullCalendar");
        var spy_isNotStudyStaff = spyOn(window, "isNotStudyStaff").and.returnValue(true);

        var resourceData = {};
        var eventsData = {};
        var pageDate = 'foo';

        loadTemplateResourcesData('id', resourceData, eventsData, pageDate);

        expect(spy_dollar).toHaveBeenCalledTimes(1);
        expect(spy_dollar.calls.argsFor(0)).toEqual(["#id"]);

        expect(spy_isNotStudyStaff).toHaveBeenCalledTimes(1);

        expect(spy_fullCalendar).toHaveBeenCalledTimes(1);
        expect(spy_fullCalendar.calls.argsFor(0)).toEqual([{
            schedulerLicenseKey: 'GPL-My-Project-Is-Open-Source',
            now: pageDate,
            aspectRatio: 1,
            scrollTime: '00:00',
            slotDuration: '00:15',
            header: {
                left: '',
                center: '',
                right: ''
            },
            lazyFetching: false,
            slotWidth: '20',
            contentHeight: "auto",
            defaultView: 'timelineDay',
            loading: TRT.loadTemplateResourcesData_loading,
            views: {
                day: {
                    titleFormat: '[Day ]' + 'D',
                    columnFormat: 'dddd, MMMM d'
                }
            },
            editable: true,
            slotLabelFormat: MILITARY_TIME_FORMAT,
            selectable: true,
            selectHelper: true,
            eventDrop: TRT.loadTemplateResourcesData_eventDrop,
            eventResize: TRT.loadTemplateResourcesData_eventResize,
            eventDragStart: TRT.loadTemplateResourcesData_eventDragStart,
            eventMouseover: TRT.loadTemplateResourcesData_eventMouseover,
            eventMouseout: TRT.loadTemplateResourcesData_eventMouseout,
            eventClick: TRT.loadTemplateResourcesData_eventClick,
            eventAfterRender: TRT.loadTemplateResourcesData_eventAfterRender,
            resourceAreaWidth: '30%',
            resourceColumns: [
                {
                    labelText: 'Resource',
                    field: 'resourceName',
                    width: '150px',
                    render: TRT.loadTemplateResourcesData_render
                },
                {
                    labelText: 'Type',
                    field: 'resourceType',
                    width: '50px'
                }
            ],
            resources: resourceData,
            events: eventsData
        }]);
    });
});

describe('template_resource_timeline.js:openLinkResourcesDialog()', function () {
    it("opens a dialog and populates a header", function () {
        var spy_dollar = spyOn(window, "$").and.callThrough();
        var spy_dialog = spyOn($.fn, "dialog");
        var spy_text = spyOn($.fn, "text");
        var spy_loadVisitsIntoWidget = spyOn(LRT, "loadVisitsIntoWidget");

        var expectedDialogOptions = {
            modal: true,
            height: 500,
            width: 900,
            resizable: false
        };

        templateResourceObj = {
            resourceName: 'name1',
            displayStart: 'start',
            displayEnd: 'end'
        };

        var linkText = 'Link name1 (start - end) to the following checked resources ';
        var selection = '(select one)';
        var expectedHeader = linkText + selection;

        openLinkResourcesDialog(true);

        expect(isLinkingFloat).toBe(true);
        expect(spy_dollar).toHaveBeenCalledTimes(2);
        expect(spy_dollar.calls.argsFor(0)).toEqual(["#link_template_resources"]);
        expect(spy_dollar.calls.argsFor(1)).toEqual(["#resourceLinkingTableHeading"]);

        expect(spy_dialog).toHaveBeenCalledTimes(1);
        expect(spy_dialog.calls.argsFor(0)).toEqual([expectedDialogOptions]);

        expect(spy_text).toHaveBeenCalledTimes(1);
        UtilHelper.expectEqualCollapse(spy_text.calls.argsFor(0)[0], expectedHeader);

        expect(spy_loadVisitsIntoWidget).toHaveBeenCalledTimes(1);

        ////////////////////////////////

        spy_text.calls.reset();

        selection = '(select up to two)';
        expectedHeader = linkText + selection;

        openLinkResourcesDialog(false);
        expect(isLinkingFloat).toBe(false);

        expect(spy_text).toHaveBeenCalledTimes(1);
        UtilHelper.expectEqualCollapse(spy_text.calls.argsFor(0)[0], expectedHeader);
    });
});

describe('template_resource_timeline.js:loadSelectResourcesData()', function () {
    it("opens a dialog that uses three callbacks", function () {
        var spy_dollar = spyOn(window, "$").and.callThrough();
        var spy_blockUI = spyOn($, "blockUI");
        var spy_dialog = spyOn($.fn, "dialog");

        var expectedDialogOptions = {
            modal: true,
            height: 625,
            width: 900,
            resizable: false,
            open: TRT.loadSelectResourcesDataOpen,
            beforeClose: TRT.loadSelectResourcesDataBeforeClose,
            close: TRT.loadSelectResourcesDataClose
        };

        var blockOptions = {fadeIn: 0};

        loadSelectResourcesData();

        expect(spy_blockUI).toHaveBeenCalledTimes(1);
        expect(spy_blockUI.calls.argsFor(0)).toEqual([blockOptions]);

        expect(spy_dollar).toHaveBeenCalledTimes(1);
        expect(spy_dollar.calls.argsFor(0)).toEqual(["#select_template_resource"]);

        expect(spy_dialog).toHaveBeenCalledTimes(1);
        expect(spy_dialog.calls.argsFor(0)).toEqual([expectedDialogOptions]);
    });
});

describe('template_resource_timeline.js:TRT.loadSelectResourcesDataOpen()', function () {
    it("marks some checkboxes, sets properties and creates a table", function () {
        var spy_dollar = spyOn(window, "$").and.callThrough();
        var spy_prop = spyOn($.fn, "prop");
        var spy_css = spyOn($.fn, "css");
        var spy_createSelectableTable = spyOn(window, "createSelectableTable");
        var spy_buildDisplayFilterSelect =
            spyOn(VisitTables, "buildDisplayFilterSelect");

        TRT.loadSelectResourcesDataOpen();

        expect(spy_dollar).toHaveBeenCalledTimes(3);
        expect(spy_dollar.calls.argsFor(0)).toEqual(["#allResources"]);
        expect(spy_dollar.calls.argsFor(1)).toEqual(["#nonBillableResources"]);
        expect(spy_dollar.calls.argsFor(2)).toEqual([".formValidationRequired"]);

        expect(spy_prop).toHaveBeenCalledTimes(2);
        expect(spy_prop.calls.argsFor(0)).toEqual(["checked", true]);
        expect(spy_prop.calls.argsFor(1)).toEqual(["checked", true]);

        expect(spy_css).toHaveBeenCalledTimes(1);
        expect(spy_css.calls.argsFor(0)).toEqual([{visibility: "hidden"}]);

        expect(spy_createSelectableTable).toHaveBeenCalledTimes(1);

        expect(spy_buildDisplayFilterSelect).toHaveBeenCalledTimes(1);
    });
});

describe('template_resource_timeline.js:TRT.loadSelectResourcesDataBeforeClose()', function () {
    it("marks some checkboxes, sets properties and creates a table", function () {

        var server = UtilHelper.getFakeServer();

        FixtureHelper.loadTestHtmlFixtureIntoDom('resources/html/elements-for-load-select-resource-data-close.html');

        var url = "rest/appointment/getSelectableTemplateResources?visitId=22&isBillable=false&orderBy=ASC&sortBy=tr.resource.name";
        AjaxHelper.setupFakeServerTextResponse(server, url, '[]');

        app_selectedVisit = {
            'id': 22
        };

        ResourceBillingTable.createResourceBillingTable();
        expect(ResourceBillingTable.currentTable.getSelectedRows().length).toBe(0);

        ResourceBillingTable.populateResourceBillingTable();

        server.respond();

        expect(ResourceBillingTable.currentTable.getSelectedRows().length).toBe(0);

        var spy_confirmNavigateDialog = spyOn(window, "confirmNavigateDialog");

        var result = TRT.loadSelectResourcesDataBeforeClose();

        expect(result).toBe(true);
        expect(spy_confirmNavigateDialog).toHaveBeenCalledTimes(0);

        AjaxHelper.setupFakeServerTextResponseFromTestFixture(server, url, 'resources/json/selectable-template-resources.json');

        ResourceBillingTable.populateResourceBillingTable();

        server.respond();

        ResourceBillingTable.currentTable.selectRow(0);
        ResourceBillingTable.currentTable.selectRow(2);

        result = TRT.loadSelectResourcesDataBeforeClose();

        expect(result).toBe(false);

        expect(spy_confirmNavigateDialog).toHaveBeenCalledTimes(1);
        expect(spy_confirmNavigateDialog.calls.argsFor(0)).toEqual([true]);
    });
});

describe('template_resource_timeline.js:TRT.loadSelectResourcesDataClose()', function () {
    it("confirms navigation in some cases, and returns a corresponding boolean", function () {

        FixtureHelper.loadTestHtmlFixtureIntoDom('resources/html/elements-for-load-select-resource-data-close.html');

        var server = UtilHelper.getFakeServer();

        var url = "rest/appointment/getSelectableTemplateResources?visitId=22&isBillable=false&orderBy=ASC&sortBy=tr.resource.name";
        AjaxHelper.setupFakeServerTextResponseFromTestFixture(server, url, 'resources/json/billable-resources-list.json');

        app_selectedVisit = {
            'id': 22
        };

        ResourceBillingTable.createResourceBillingTable();
        expect(ResourceBillingTable.currentTable.getSelectedRows().length).toBe(0);

        ResourceBillingTable.populateResourceBillingTable();

        server.respond();

        expect(ResourceBillingTable.currentTable.getSelectedRows().length).toBe(0);

        ResourceBillingTable.currentTable.selectRow(0);
        ResourceBillingTable.currentTable.selectRow(2);
        expect(ResourceBillingTable.currentTable.getSelectedRows().length).toBe(2);

        $('#select_template_resource_save').css('display', 'block');
        $('#select_template_resource_save_and_close').css('display', 'block');
        $("#select_template_resource").css('display', 'none');
        $("#select_template_resource").dialog();
        expect($("#select_template_resource").dialog('isOpen')).toBe(true);
        expect($("#select_template_resource").css('display')).toBe('block');
        $("#bulkChangeMessage").html('nothing');

        TRT.loadSelectResourcesDataClose();

        expect(ResourceBillingTable.currentTable.getSelectedRows().length).toBe(0);
        expect($('#select_template_resource_save').css('display')).toBe('none');
        expect($('#select_template_resource_save_and_close').css('display')).toBe('none');
        expect($("#select_template_resource").dialog('isOpen')).toBe(false);
        var msg = "Applying changes to checked resources will mark them as Billable.";
        expect($("#bulkChangeMessage").html()).toBe(msg);

    });
});

describe('template_resource_timeline.js:linkTemplateResourceConfirmDialog()', function () {
    beforeEach(function () {
        isLinkingFloat = true;
        templateResourceId = 42;
        actionType = 'snooze';

        LRT.currentTable = new SchedulerTables({
            tableId: "testTable",
            columns: [new Column()]
        });

        this.spy_getSelectedRows = spyOn(LRT.currentTable, "getSelectedRows");
        this.spy_util_showMainMessage = spyOn(window, "util_showMainMessage");
        this.spy_stringify = spyOn(JSON, "stringify")
            .and.returnValue("stringIds");
        this.spy_displayCrudConfirmationDialog = spyOn(window, "displayCrudConfirmationDialog");
        this.spy_linkTemplateResourcesConfirmation = spyOn(window, "linkTemplateResourcesConfirmation");

        this.helpInvoke = function (links, isFloat, approved) {

            this.spy_getSelectedRows.and.returnValue(links);
            this.links = links;
            isLinkingFloat = isFloat;
            approvedTemplate = approved;

            linkTemplateResourceConfirmDialog();
        }
    });
    it("complains if no resources chosen", function () {

        this.helpInvoke([]);

        expect(this.spy_getSelectedRows).toHaveBeenCalledTimes(1);

        expect(this.spy_util_showMainMessage).toHaveBeenCalledTimes(1);
        expect(this.spy_util_showMainMessage.calls.argsFor(0))
            .toEqual(["Please select at least one resource to link."]);
    });
    it("complains if too many flex resources chosen", function () {

        this.helpInvoke([1, 2, 3], false);

        expect(this.spy_getSelectedRows).toHaveBeenCalledTimes(1);

        expect(this.spy_util_showMainMessage).toHaveBeenCalledTimes(1);
        expect(this.spy_util_showMainMessage.calls.argsFor(0))
            .toEqual(["A maximum of three resources can be linked in a flex group."]);
    });
    it("displays crud confirmation for approved template", function () {

        this.helpInvoke([1, 2], false, 'Approved');

        expect(this.spy_getSelectedRows).toHaveBeenCalledTimes(1);

        expect(this.spy_util_showMainMessage).toHaveBeenCalledTimes(0);

        expect(this.spy_stringify).toHaveBeenCalledTimes(1);
        expect(this.spy_stringify.calls.argsFor(0))
            .toEqual([{ linkResources: [1,2,42]}]);

        expect(this.spy_displayCrudConfirmationDialog).toHaveBeenCalledTimes(1);
        expect(this.spy_displayCrudConfirmationDialog.calls.argsFor(0))
            .toEqual(["stringIds", ""]);

        expect(this.spy_linkTemplateResourcesConfirmation).toHaveBeenCalledTimes(0);
    });
    it("displays confirmation", function () {

        this.helpInvoke([1, 2], false, 'not approved');

        expect(this.spy_getSelectedRows).toHaveBeenCalledTimes(1);

        expect(this.spy_util_showMainMessage).toHaveBeenCalledTimes(0);

        expect(this.spy_stringify).toHaveBeenCalledTimes(1);
        expect(this.spy_stringify.calls.argsFor(0))
            .toEqual([{ linkResources: [1,2,42]}]);

        expect(this.spy_displayCrudConfirmationDialog).toHaveBeenCalledTimes(0);

        expect(this.spy_linkTemplateResourcesConfirmation).toHaveBeenCalledTimes(1);
        expect(this.spy_linkTemplateResourcesConfirmation.calls.argsFor(0))
            .toEqual(["stringIds"]);
    });
});

describe('TRT.copyDayCopyableSuccess', function () {

    it('acts according to value of data and approvedTemplate', function () {

        var data = true;
        approvedTemplate = "Approved";
        var confirmationMessage = "Selected copy day has no single day resources " +
            "that can be copied.";

        var spy_displayCrudConfirmationDialog =
            spyOn(window, "displayCrudConfirmationDialog");
        var spy_copyDayConfirmation = spyOn(window, "copyDayConfirmation");
        var spy_showError = spyOn(window, "showError");
        var spy_enableCopyDayBtn = spyOn(window, "enableCopyDayBtn");


        // fn under test
        TRT.copyDayCopyableSuccess(data);

        expect(spy_displayCrudConfirmationDialog).toHaveBeenCalledTimes(1);
        expect(spy_displayCrudConfirmationDialog.calls.argsFor(0))
            .toEqual([[], "", enableCopyDayBtn]);

        expect(spy_copyDayConfirmation).toHaveBeenCalledTimes(0);
        expect(spy_showError).toHaveBeenCalledTimes(0);
        expect(spy_enableCopyDayBtn).toHaveBeenCalledTimes(0);

        spy_displayCrudConfirmationDialog.calls.reset();

        approvedTemplate = "no way";

        TRT.copyDayCopyableSuccess(data);

        expect(spy_copyDayConfirmation).toHaveBeenCalledTimes(1);
        expect(spy_copyDayConfirmation.calls.argsFor(0)).toEqual([enableCopyDayBtn]);

        expect(spy_displayCrudConfirmationDialog).toHaveBeenCalledTimes(0);
        expect(spy_showError).toHaveBeenCalledTimes(0);
        expect(spy_enableCopyDayBtn).toHaveBeenCalledTimes(0);

        spy_copyDayConfirmation.calls.reset();

        data = false;

        TRT.copyDayCopyableSuccess(data);

        expect(spy_showError).toHaveBeenCalledTimes(1);
        expect(spy_showError.calls.argsFor(0))
            .toEqual(['#copy_day_formValidation', confirmationMessage]);

        expect(spy_enableCopyDayBtn).toHaveBeenCalledTimes(1);
        expect(spy_enableCopyDayBtn.calls.argsFor(0)).toEqual([]);

        expect(spy_copyDayConfirmation).toHaveBeenCalledTimes(0);
        expect(spy_displayCrudConfirmationDialog).toHaveBeenCalledTimes(0);
    });
});

describe('TRT.copyDayClick', function () {

    it('acts according to value of data and approvedTemplate', function () {

        var currentDay = 'CURRENT_DAY';
        var confirmationMessage = "Please select at least one destination day.";

        var spy_disableCopyDayBtn = spyOn(window, "disableCopyDayBtn");
        var spy_$ = spyOn(window, "$").and.callThrough();
        var spy_css = spyOn($.fn, "css");
        var spy_trim = spyOn($, "trim").and.returnValue(currentDay);
        var spy_val = spyOn($.fn, "val").and.returnValue(currentDay + '_trim');
        var spy_multipleSelect = spyOn($.fn, "multipleSelect").and.returnValue([]);

        var spy_showError = spyOn(window, "showError");
        var spy_enableCopyDayBtn = spyOn(window, "enableCopyDayBtn");

        // fn under test
        TRT.copyDayClick();

        expect(actionType).toBe('copy_day_resources');

        expect(spy_$).toHaveBeenCalledTimes(3);
        expect(spy_$.calls.argsFor(0)).toEqual(['.formValidationRequired']);
        expect(spy_$.calls.argsFor(1)).toEqual(["#fromCopyDaySelect"]);
        expect(spy_$.calls.argsFor(2)).toEqual(["#toCopyDaySelect"]);

        expect(spy_css).toHaveBeenCalledTimes(1);
        expect(spy_css.calls.argsFor(0)).toEqual([{visibility: "hidden"}]);

        expect(spy_val).toHaveBeenCalledTimes(1);

        expect(spy_trim).toHaveBeenCalledTimes(1);
        expect(spy_trim.calls.argsFor(0)).toEqual([currentDay + '_trim']);

        expect(spy_multipleSelect).toHaveBeenCalledTimes(1);
        expect(spy_multipleSelect.calls.argsFor(0)).toEqual(['getSelects']);

        expect(spy_showError).toHaveBeenCalledTimes(1);
        expect(spy_showError.calls.argsFor(0))
            .toEqual(['#copy_day_formValidation', confirmationMessage]);

        expect(spy_enableCopyDayBtn).toHaveBeenCalledTimes(1);

        /////////
        var foo = { fail: function() {}};

        var spy_fail = spyOn(foo, "fail");
        var spy_ajax = spyOn($, "ajax").and.returnValue(foo);

        spy_multipleSelect.and.returnValue(['one', 'two']);

        TRT.copyDayClick();

        expect(spy_ajax).toHaveBeenCalledTimes(1);
        expect(spy_ajax.calls.argsFor(0))
            .toEqual([{
                url: "rest/appointment/isDayCopyable?visitId=" + app_selectedVisit.id
                + "&currentDay=" + currentDay,
                dataType: 'json',
                success: TRT.copyDayCopyableSuccess
            }]);

        expect(spy_fail).toHaveBeenCalledTimes(1);
        expect(spy_fail.calls.argsFor(0))
            .toEqual([jasmine.any(Function)]);
    });
});

