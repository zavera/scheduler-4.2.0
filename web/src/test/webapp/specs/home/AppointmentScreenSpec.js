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

// describe('appointment_screen.js:overbookRoom_widgets()', function () {
describe('src/test/webapp/specs/home/AppointmentScreenSpec.js', function () {

    it("sets up overbook date/time widgets", function () {

        var spy_createDatepicker = spyOn(WidgetUtil, "createDatepicker");
        var spy_dollar = spyOn(window, "$").and.callThrough();
        var spy_timepicker = spyOn($.fn, "timepicker");

        overbookRoom_widgets();

        expect(spy_createDatepicker).toHaveBeenCalledTimes(1);
        expect(spy_createDatepicker.calls.argsFor(0)).toEqual(["#overbookRoom_startDate", {
            onClose: AppointmentScreen.overbookRoomWidgetsCallback
        }]);

        expect(spy_dollar).toHaveBeenCalledTimes(5);
        expect(spy_dollar.calls.argsFor(0)).toEqual(['#overbookRoom_startTime']);

        expect(spy_timepicker).toHaveBeenCalledTimes(1);
        expect(spy_timepicker.calls.argsFor(0)).toEqual([{
            controlType: 'select',
            timeFormat: MILITARY_TIME_FORMAT,
            minuteMax: 59,
            stepMinute: 15
        }]);
    });
});

describe('appointment_screen.js:overbookRoomWidgetsCallback()', function () {

    it("tries to parse date", function () {

        var spy_parseDate = spyOn($.datepicker, "parseDate");
        var spy_alert = spyOn(window, "alert");
        var spy_dollar = spyOn(window, "$").and.callThrough();
        var spy_val = spyOn($.fn, "val");

        AppointmentScreen.overbookRoomWidgetsCallback("01/02/2003");

        expect(spy_parseDate).toHaveBeenCalledTimes(1);
        expect(spy_parseDate.calls.argsFor(0)).toEqual(['mm/dd/yy', "01/02/2003"]);

        expect(spy_alert).toHaveBeenCalledTimes(0);
        expect(spy_dollar).toHaveBeenCalledTimes(0);
        expect(spy_val).toHaveBeenCalledTimes(0);

        spy_parseDate.calls.reset();
        spy_parseDate.and.callThrough();
        AppointmentScreen.overbookRoomWidgetsCallback("foo");

        expect(spy_parseDate).toHaveBeenCalledTimes(1);
        expect(spy_parseDate.calls.argsFor(0)).toEqual(['mm/dd/yy', "foo"]);
        expect(spy_parseDate).toThrow();

        expect(spy_alert).toHaveBeenCalledTimes(1);
        expect(spy_alert.calls.argsFor(0)).toEqual(["Incorrect Date format. It should be MM/DD/YYYY."]);

        expect(spy_dollar).toHaveBeenCalledTimes(1);
        expect(spy_dollar.calls.argsFor(0)).toEqual(["#overbookRoom_startDate"]);

        expect(spy_val).toHaveBeenCalledTimes(1);
        expect(spy_val.calls.argsFor(0)).toEqual([""]);
    });
});

xdescribe('appointment_screen.js:startOverbookingAppointment()', function () {

    beforeAll(function () {
        this.someDate = new Date();

        this.spy_dollar = spyOn(window, "$").and.returnValue($.fn);
        this.spy_css = spyOn($.fn, "css");
        this.spy_appt_wizard_viewStack = spyOn(window, "appt_wizard_viewStack");
        this.spy_overbookRoom_widgets = spyOn(window, "overbookRoom_widgets");
        this.spy_datepicker = spyOn($.fn, "datepicker");
        this.spy_date = spyOn(window, "Date").and.returnValue(this.someDate);
        this.spy_hide = spyOn($.fn, "hide");
        this.spy_attr = spyOn($.fn, "attr");
        this.spy_show = spyOn($.fn, "show");

        this.spy_dollar.calls.reset();

        this.helperReset = function () {
            this.spy_dollar.calls.reset();
            this.spy_css.calls.reset();
            this.spy_appt_wizard_viewStack.calls.reset();
            this.spy_overbookRoom_widgets.calls.reset();
            this.spy_datepicker.calls.reset();
            this.spy_date.calls.reset();
            this.spy_hide.calls.reset();
            this.spy_attr.calls.reset();
            this.spy_show.calls.reset();
        };

        this.helperExpectForNoOverbookStartAndNoResources = function () {
            expect(this.spy_dollar).toHaveBeenCalledTimes(9);
            var index = 0;
            expect(this.spy_dollar.calls.argsFor(index++)).toEqual(['.formElementRequired']);
            expect(this.spy_dollar.calls.argsFor(index++)).toEqual(['.formTextAreaSmaller']);
            expect(this.spy_dollar.calls.argsFor(index++)).toEqual(['#overbookRoom_startDate']);
            expect(this.spy_dollar.calls.argsFor(index++)).toEqual(['#overbookRoom_startTime']);
            expect(this.spy_dollar.calls.argsFor(index++)).toEqual(['#overbookRoom_startTime']);
            expect(this.spy_dollar.calls.argsFor(index++)).toEqual(['#overbookRoom_startTime']);
            expect(this.spy_dollar.calls.argsFor(index++)).toEqual(['.allrooms_appointment_tr']);
            expect(this.spy_dollar.calls.argsFor(index++)).toEqual(['#appt_wizard_form-prev']);
            expect(this.spy_dollar.calls.argsFor(index++)).toEqual(['#appt_wizard_form-next']);

            expect(this.spy_css).toHaveBeenCalledTimes(4);
            expect(this.spy_css.calls.argsFor(0)).toEqual([{visibility: "hidden"}]);
            expect(this.spy_css.calls.argsFor(1)).toEqual([{display: "block"}]);
            expect(this.spy_css.calls.argsFor(2)).toEqual([{display: "inline-block"}]);
            expect(this.spy_css.calls.argsFor(3)).toEqual([{display: "none"}]);

            expect(this.spy_appt_wizard_viewStack).toHaveBeenCalledTimes(1);
            expect(this.spy_appt_wizard_viewStack.calls.argsFor(0)).toEqual(['appt_wizard_overbook_room']);

            expect(this.spy_overbookRoom_widgets).toHaveBeenCalledTimes(1);

            expect(this.spy_datepicker).toHaveBeenCalledTimes(2);
            expect(this.spy_datepicker.calls.argsFor(0)).toEqual(['setDate', this.someDate]);
            expect(this.spy_datepicker.calls.argsFor(1)).toEqual(['setTime', '07:00']);

            expect(this.spy_date).toHaveBeenCalledTimes(1);
            expect(this.spy_hide).toHaveBeenCalledTimes(1);

            expect(this.spy_attr).toHaveBeenCalledTimes(2);
            expect(this.spy_attr.calls.argsFor(0)).toEqual(["value", "Previous"]);
            expect(this.spy_attr.calls.argsFor(1)).toEqual(["value", "Reserve Visit"]);

            expect(this.spy_show).toHaveBeenCalledTimes(0);
        }
    });


    it("does very little -- e.g., does not set up fields, but clear validation field, if not form4",
        function () {

            this.helperReset();

            /// not form4,
            appt_currentWizardScreen = 'not form4';
            //startOverbookingAppointment();

            expect(this.spy_dollar).toHaveBeenCalledTimes(1);
            expect(this.spy_dollar.calls.argsFor(0)).toEqual(['.formElementRequired']);

            expect(this.spy_css).toHaveBeenCalledTimes(1);
            expect(this.spy_css.calls.argsFor(0)).toEqual([{visibility: "hidden"}]);

        });

    xit("clears validation field, and for form4, sets up overbook fields",
        function () {

            /// cases where VisitType is InpatientCRC

            appt_currentWizardScreen = 'appt_wizard_form4';
            appt_selectedVisitType = VisitTypes.InpatientCRC;
            appt_selectedVisitRelative = false;

            /// ... and where there are no roomResources
            this.helperReset();
            roomResources == undefined;
            //startOverbookingAppointment();
            this.helperExpectForNoOverbookStartAndNoResources();

            this.helperReset();
            roomResources == null;
            //startOverbookingAppointment();
            this.helperExpectForNoOverbookStartAndNoResources();

            this.helperReset();
            roomResources == '';
            //startOverbookingAppointment();
            this.helperExpectForNoOverbookStartAndNoResources();

            /// some other visit types should work the same
            appt_selectedVisitType = VisitTypes.InpatientNonCRC;
            this.helperReset();
            //startOverbookingAppointment();
            this.helperExpectForNoOverbookStartAndNoResources();

            appt_selectedVisitType = VisitTypes.InpatientOffInstitution;
            this.helperReset();
            //startOverbookingAppointment();
            this.helperExpectForNoOverbookStartAndNoResources();

            /// cases where 'yes' overbook start
            appt_selectedVisitType = VisitTypes.InpatientOffInstitution + ' - not';
            this.helperReset();
            //startOverbookingAppointment();
            expect(this.spy_dollar).toHaveBeenCalledTimes(9 - 1); // vs 9 calls in earlier cases
            expect(this.spy_dollar.calls.argsFor(5)).toEqual(['.allrooms_appointment_tr']);

            appt_selectedVisitRelative = true;
            this.helperReset();
            //startOverbookingAppointment();
            expect(this.spy_dollar).toHaveBeenCalledTimes(9 - 1); // vs 9 calls in earlier cases
            expect(this.spy_dollar.calls.argsFor(5)).toEqual(['.allrooms_appointment_tr']);

            /// cases where there are room resources
            roomResources = 'yes rooms';
            this.helperReset();
            //startOverbookingAppointment();
            expect(this.spy_hide).toHaveBeenCalledTimes(0);
            expect(this.spy_show).toHaveBeenCalledTimes(1);
        });
});
