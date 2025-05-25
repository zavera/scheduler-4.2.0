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

describe('home_util.js:openBookedResourcesAppointmentWindow()', function () {

    it('gets visit data via ajax, to be handed to a callback', function () {

        var spy_getJSON = spyOn($, "getJSON").and.callFake(function() {
            HomeAppointment.openAppointmentWindowCallback('{}', 'Scheduled')
        });
        var spy_OpenAppointmentWindowCallback = spyOn(HomeAppointment, "openAppointmentWindowCallback");

        eventid = 'foo'
        openBookedResourcesAppointmentWindow('Scheduled');

        expect(spy_getJSON).toHaveBeenCalledTimes(1);
        expect(spy_getJSON.calls.argsFor(0)).toEqual(["rest/appointment/getScheduledVisitDetails?id=foo",
            jasmine.any(Function)]);

        expect(spy_OpenAppointmentWindowCallback).toHaveBeenCalledTimes(1);
        expect(spy_OpenAppointmentWindowCallback.calls.argsFor(0)).toEqual(['{}', 'Scheduled']);

    });
});

describe('home_util.js:HomeAppointment.openAppointmentWindowCallback()', function () {

    beforeAll(function() {
        this.spy_openHoldOrScheduledAppointmentWindow =
            spyOn(HomeAppointment, "openHoldOrScheduledAppointmentWindow").and.returnValue('baz');
        this.spy_openCheckedInAppointmentWindow       =
            spyOn(HomeAppointment, "openCheckedInAppointmentWindow").and.returnValue('baz');
        this.spy_openCheckedOutAppointmentWindow      =
            spyOn(HomeAppointment, "openCheckedOutAppointmentWindow").and.returnValue('baz');
        this.spy_openCancelledAppointmentWindow       =
            spyOn(HomeAppointment, "openCancelledAppointmentWindow").and.returnValue('baz');

        this.spy_sanitizeAndSaveClassName = spyOn(window, "sanitizeAndSaveClassName")
            .and.returnValue("Scheduled");

        this.helperResetWindowSpies = function(className) {
            this.spy_openHoldOrScheduledAppointmentWindow.calls.reset();
            this.spy_openCheckedInAppointmentWindow.calls.reset();
            this.spy_openCheckedOutAppointmentWindow.calls.reset();
            this.spy_openCancelledAppointmentWindow.calls.reset();

            this.spy_sanitizeAndSaveClassName.and.returnValue(className);

        };
        this.expectOneWindowOpened = function(hold, checkin, checkout, cancel, calledSpy, data, templateData, baseTemplate) {
            expect(this.spy_openHoldOrScheduledAppointmentWindow).toHaveBeenCalledTimes(hold);
            expect(this.spy_openCheckedInAppointmentWindow).toHaveBeenCalledTimes(checkin);
            expect(this.spy_openCheckedOutAppointmentWindow).toHaveBeenCalledTimes(checkout);
            expect(this.spy_openCancelledAppointmentWindow).toHaveBeenCalledTimes(cancel);

            expect(calledSpy.calls.argsFor(0)).toEqual([data, templateData, baseTemplate]);
        };
    });

    it('uses or refines its data and className, opens a class-dependent ' +
        'window, and sets up comment-views', function () {

        var templateData = { foo: 1};
        var baseTemplate = { bar: 2};
        var data = {actionName: 'action1', visitCommentsTotal: 42};

        var spy_setGlobalVars = spyOn(window, "setGlobalVars");
        var spy_assembleTemplateData = spyOn(HomeAppointment, "assembleTemplateData")
            .and.returnValue(templateData);
        var spy_templates = spyOn($, "templates").and.returnValue(baseTemplate);
        var spy_dollar = spyOn(window, "$").and.callThrough();
        var spy_text = spyOn($.fn, "text");

        var spy_create = spyOn(WidgetUtil.counterDisplay, "create");

        studyId = 1;
        visitId = 2;
        user = {institutionRole: {id: GENERAL_VIEW + 1}};

        this.helperResetWindowSpies("Scheduled");

        HomeAppointment.openAppointmentWindowCallback(data, className);

        expect(spy_setGlobalVars).toHaveBeenCalledTimes(1);
        expect(spy_setGlobalVars.calls.argsFor(0)).toEqual([data]);

        expect(spy_assembleTemplateData).toHaveBeenCalledTimes(1);
        expect(spy_assembleTemplateData.calls.argsFor(0)).toEqual([data, 'action1']);

        expect(spy_templates).toHaveBeenCalledTimes(1);
        expect(spy_templates.calls.argsFor(0)).toEqual(["#apptDetails_base"]);

        expect(spy_dollar).toHaveBeenCalledTimes(3);
        expect(spy_dollar.calls.argsFor(0)).toEqual(['.dialog_study_id']);
        expect(spy_dollar.calls.argsFor(1)).toEqual(['.dialog_visit_id']);
        expect(spy_dollar.calls.argsFor(2)).toEqual(['#baz']);

        expect(spy_text).toHaveBeenCalledTimes(2);
        expect(spy_text.calls.argsFor(0)).toEqual([studyId]);
        expect(spy_text.calls.argsFor(1)).toEqual([visitId]);

        expect(spy_templates).toHaveBeenCalledTimes(1);
        expect(spy_templates.calls.argsFor(0)).toEqual(["#apptDetails_base"]);

        this.expectOneWindowOpened(1, 0, 0, 0,
            this.spy_openHoldOrScheduledAppointmentWindow, data,
            templateData, baseTemplate);

        expect(spy_create).toHaveBeenCalledTimes(1);
        expect(spy_create.calls.argsFor(0)).toEqual([jasmine.any(Object), 42, {
                                                    wrapperClass: "inline-block",
                                                    backgroundColor: "#083e60"
                                                }]);
        // similarly for 'Hold'
        this.helperResetWindowSpies("Hold");
        HomeAppointment.openAppointmentWindowCallback(data, "Hold");
        this.expectOneWindowOpened(1, 0, 0, 0,
            this.spy_openHoldOrScheduledAppointmentWindow, data,
            templateData, baseTemplate);

        // similarly for 'Checked-In'
        this.helperResetWindowSpies("Checked-In");
        HomeAppointment.openAppointmentWindowCallback(data, "Checked-In");
        this.expectOneWindowOpened(0, 1, 0, 0,
            this.spy_openCheckedInAppointmentWindow, data,
            templateData, baseTemplate);

        // similarly for 'Checked-Out'
        this.helperResetWindowSpies("Checked-Out");
        HomeAppointment.openAppointmentWindowCallback(data, "Checked-Out");
        this.expectOneWindowOpened(0, 0, 1, 0,
            this.spy_openCheckedOutAppointmentWindow, data,
            templateData, baseTemplate);

        // similarly for 'Cancellation'
        this.helperResetWindowSpies("Cancellation");
        HomeAppointment.openAppointmentWindowCallback(data, "Cancellation");
        this.expectOneWindowOpened(0, 0, 0, 1,
            this.spy_openCancelledAppointmentWindow, data,
            templateData, baseTemplate);

        // does not create comment view for general-view
        spy_create.calls.reset();
        user.institutionRole.id = GENERAL_VIEW;
        HomeAppointment.openAppointmentWindowCallback(data, "Hold");
        expect(spy_create).toHaveBeenCalledTimes(0);
    });
});

describe('home_util.js:HomeAppointment.assembleTemplateData()', function () {

    it('assemble an object holding various template-related data', function () {

        var spy_userIsCrcStaff = spyOn(UserRoleUtil, "userIsCrcStaff").and.returnValue('foo');

        var result = HomeAppointment.assembleTemplateData(  {visitName: 'visitName2',
                                                             localId: 4200,
                                                             subjectMrn: 2400
                                                            },
                                                            'action2');
        expect(spy_userIsCrcStaff).toHaveBeenCalledTimes(1);

        expect(result.localId).toBe(4200);
        expect(result.subjectMrn).toBe(2400);
        expect(result.actionName).toBe('action2');
    });
});

describe('home_util.js:HomeAppointment.openHoldOrScheduledAppointmentWindow()', function () {

    it('sets up data and opens hold or scheduled appt window', function () {

        var data = {subjectMrn: 42};
        var templateData = { switchSubjectButtonText: "Remove or Switch Subject"};
        var baseTemplate = { render: function(){}};
        user = {institutionRole: {id: GENERAL_VIEW}};

        var spy_isValidMrn = spyOn(window, "isValidMrn").and.returnValue(true);
        var spy_dollar = spyOn(window, "$").and.callThrough();
        var spy_css = spyOn($.fn, "css");
        var spy_val = spyOn($.fn, "val");
        var spy_show = spyOn($.fn, "show");
        var spy_setItem = spyOn(sessionStorage, "setItem");

        var spy_userIsCrcStaff = spyOn(UserRoleUtil, "userIsCrcStaff").and.returnValue(false);
        var spy_hide = spyOn($.fn, "hide");
        var spy_render = spyOn(baseTemplate, "render").and.returnValue('bar');
        var spy_html = spyOn($.fn, "html");
        var spy_prop = spyOn($.fn, "prop");
        var spy_preventComment = spyOn(ApptCalendar, "preventComment");
        var spy_dialog = spyOn($.fn, "dialog");

        var spy_setLocationHash = spyOn(window, "setLocationHash");

        HomeAppointment.openHoldOrScheduledAppointmentWindow(data, templateData, baseTemplate);

        expect(spy_isValidMrn).toHaveBeenCalledTimes(1);
        expect(spy_isValidMrn.calls.argsFor(0)).toEqual([42]);

        expect(spy_dollar).toHaveBeenCalledTimes(17);
        var index = 0;
        expect(spy_dollar.calls.argsFor(index++)).toEqual(['#wizardSetSubjectNA']);
        expect(spy_dollar.calls.argsFor(index++)).toEqual(['#appt_wizard_checkin']);
        expect(spy_dollar.calls.argsFor(index++)).toEqual(['#wizard_cancellationReasons']);
        expect(spy_dollar.calls.argsFor(index++)).toEqual(['#cancellationDropDown']);
        expect(spy_dollar.calls.argsFor(index++)).toEqual(['#appt_wizard_cancelAppointment']);

        expect(spy_dollar.calls.argsFor(index++)).toEqual(['#visitSummaryHeader']);
        expect(spy_dollar.calls.argsFor(index++)).toEqual(['#switchSubject_div']);
        expect(spy_dollar.calls.argsFor(index++)).toEqual(['#appt_wizard_scheduled_visit_details-close']);
        expect(spy_dollar.calls.argsFor(index++)).toEqual(['#wizardTableSwitchSubjectHeader']);

        expect(spy_dollar.calls.argsFor(index++)).toEqual(['#switchSubject_chooseSubject']);
        expect(spy_dollar.calls.argsFor(index++)).toEqual(['#switchSubject_noSubject']);
        expect(spy_dollar.calls.argsFor(index++)).toEqual(['#scheduledResourcesBlock']);

        expect(spy_dollar.calls.argsFor(index++)).toEqual(['#viewScheduledCommentLink']);
        expect(spy_dollar.calls.argsFor(index++)).toEqual(['#appt_wizard_cancelAppointment']);
        expect(spy_dollar.calls.argsFor(index++)).toEqual(['#appt_wizard_checkin']);

        expect(spy_dollar.calls.argsFor(index++)).toEqual(['#appt_wizard_scheduled_visit_details table tbody.forTemplate']);
        expect(spy_dollar.calls.argsFor(index++)).toEqual(['#appt_wizard_scheduled_visit_details']);

        expect(spy_css).toHaveBeenCalledTimes(9);
        index = 0;
        expect(spy_css.calls.argsFor(index++)).toEqual([{display: 'block'}]);
        expect(spy_css.calls.argsFor(index++)).toEqual([{display: 'inline'}]);
        expect(spy_css.calls.argsFor(index++)).toEqual([{display: 'inline'}]);

        expect(spy_css.calls.argsFor(index++)).toEqual([{display: 'none'}]);
        expect(spy_css.calls.argsFor(index++)).toEqual([{display: 'inline'}]);
        expect(spy_css.calls.argsFor(index++)).toEqual([{display: 'inline'}]);

        expect(spy_css.calls.argsFor(index++)).toEqual([{display: 'none'}]);
        expect(spy_css.calls.argsFor(index++)).toEqual([{display: 'none'}]);
        expect(spy_css.calls.argsFor(index++)).toEqual([{display: 'none'}]);

        expect(spy_val).toHaveBeenCalledTimes(1);
        expect(spy_val.calls.argsFor(0)).toEqual(['']);

        expect(spy_show).toHaveBeenCalledTimes(2);

        expect(spy_setItem).toHaveBeenCalledTimes(1);
        expect(spy_setItem.calls.argsFor(0)).toEqual(["assignSubjectMode", 'removeOrSwitch']);

        expect(spy_userIsCrcStaff).toHaveBeenCalledTimes(0);
        expect(spy_hide).toHaveBeenCalledTimes(0);

        expect(spy_render).toHaveBeenCalledTimes(1);
        expect(spy_render.calls.argsFor(0)).toEqual([templateData]);

        expect(spy_html).toHaveBeenCalledTimes(3);
        expect(spy_html.calls.argsFor(0)).toEqual(["Scheduled Visit Summary"]);
        expect(spy_html.calls.argsFor(1)).toEqual(["Remove or Switch Subject"]);
        expect(spy_html.calls.argsFor(2)).toEqual(["bar"]);

        expect(spy_prop).toHaveBeenCalledTimes(1);
        expect(spy_prop.calls.argsFor(0)).toEqual(['checked', false]);

        expect(spy_preventComment).toHaveBeenCalledTimes(1);

        expect(spy_dialog).toHaveBeenCalledTimes(1);
        expect(spy_dialog.calls.argsFor(0)).toEqual([{
            modal: true,
            close: resetCloseDialogFunction,
            height: 875,
            width: 930,
            title: "Check-In/Cancel Appointment"
        }]);

        expect(spy_setLocationHash).toHaveBeenCalledTimes(1);
        expect(spy_setLocationHash.calls.argsFor(0)).toEqual(['ScheduledVisit']);

        //// now try with undefined subjectMrn, user still not crc staff

        data.subjectMrn = undefined;

        spy_dollar.calls.reset();
        spy_css.calls.reset();
        spy_val.calls.reset();
        spy_show.calls.reset();
        spy_setItem.calls.reset();

        spy_userIsCrcStaff.calls.reset();
        spy_hide.calls.reset();
        spy_dialog.calls.reset();

        HomeAppointment.openHoldOrScheduledAppointmentWindow(data, templateData, baseTemplate);

        // re-check calls from the undefined-subj-mrn block
        expect(spy_dollar).toHaveBeenCalledTimes(17);
        var index = 0;
        expect(spy_dollar.calls.argsFor(index++)).toEqual(['#wizardSetSubjectNA']);
        expect(spy_dollar.calls.argsFor(index++)).toEqual(['#appt_wizard_checkin']);
        expect(spy_dollar.calls.argsFor(index++)).toEqual(['#wizard_cancellationReasons']);
        expect(spy_dollar.calls.argsFor(index++)).toEqual(['#cancellationDropDown']);
        expect(spy_dollar.calls.argsFor(index++)).toEqual(['#appt_wizard_cancelAppointment']);

        expect(spy_css).toHaveBeenCalledTimes(9);
        index = 0;
        expect(spy_css.calls.argsFor(index++)).toEqual([{display: 'none'}]);
        expect(spy_css.calls.argsFor(index++)).toEqual([{display: 'none'}]);
        expect(spy_css.calls.argsFor(index++)).toEqual([{display: 'none'}]);

        expect(spy_val).toHaveBeenCalledTimes(1);
        expect(spy_val.calls.argsFor(0)).toEqual(['']);

        expect(spy_show).toHaveBeenCalledTimes(1);
        expect(spy_hide).toHaveBeenCalledTimes(1);

        expect(spy_setItem).toHaveBeenCalledTimes(1);
        expect(spy_setItem.calls.argsFor(0)).toEqual(["assignSubjectMode", 'assign']);

        expect(spy_userIsCrcStaff).toHaveBeenCalledTimes(1);

        expect(spy_dialog.calls.argsFor(0)).toEqual([{
            modal: true,
            close: resetCloseDialogFunction,
            height: 875,
            width: 930,
            title: "Change/Cancel Appointment"
        }]);

        //// now with undefined subjectMrn, crc staff true

        spy_userIsCrcStaff.and.returnValue(true);

        spy_css.calls.reset();
        spy_val.calls.reset();
        spy_show.calls.reset();
        spy_hide.calls.reset();

        HomeAppointment.openHoldOrScheduledAppointmentWindow(data, templateData, baseTemplate);

        // re-check calls from the undefined-subj-mrn, true crc staff block
        expect(spy_css.calls.argsFor(2)).toEqual([{display: 'inline'}]);

        expect(spy_val).toHaveBeenCalledTimes(1);
        expect(spy_val.calls.argsFor(0)).toEqual([releaseHoldReasonId]);

        expect(spy_show).toHaveBeenCalledTimes(2);
        expect(spy_hide).toHaveBeenCalledTimes(0);

        //// now try the block where user role id is study staff
        spy_css.calls.reset();
        spy_dollar.calls.reset();

        user.institutionRole.id = STUDY_STAFF;

        HomeAppointment.openHoldOrScheduledAppointmentWindow(data, templateData, baseTemplate);

        // re-check calls from the undefined-subj-mrn, true crc staff block
        expect(spy_dollar).toHaveBeenCalledTimes(17 - 2);
        expect(spy_dollar.calls.argsFor(12)).toEqual(["#appt_wizard_checkin"]);

        expect(spy_css).toHaveBeenCalledTimes(9 - 2);
        expect(spy_css.calls.argsFor(6)).toEqual([{display: 'none'}]);

        //// now try the block where user role id is neither general nor study staff
        spy_css.calls.reset();
        spy_dollar.calls.reset();

        user.institutionRole.id = STUDY_STAFF + GENERAL_VIEW + 1;

        HomeAppointment.openHoldOrScheduledAppointmentWindow(data, templateData, baseTemplate);

        // re-check calls from the undefined-subj-mrn, true crc staff block
        expect(spy_dollar).toHaveBeenCalledTimes(17 - 2);
        expect(spy_dollar.calls.argsFor(12)).toEqual(["#appt_wizard_saveComment"]);

        expect(spy_css).toHaveBeenCalledTimes(9 - 2);
        expect(spy_css.calls.argsFor(6)).toEqual([{display: 'inline'}]);
    });
});


describe('home_util.js:HomeAppointment.openCheckedInAppointmentWindow()', function () {

    it('sets up data and opens check in appt window', function () {

        var data = {subjectMrn: 42};
        var templateData = { };
        var baseTemplate = { render: function(){}};

        var spy_render = spyOn(baseTemplate, "render").and.returnValue('foo');
        var spy_loadAndRenderTemplate = spyOn(window, "loadAndRenderTemplate")
            .and.returnValue('bar');

        var spy_dollar = spyOn(window, "$").and.returnValue($.fn);
        var spy_html = spyOn($.fn, "html");

        var spy_getElementById = spyOn(document, "getElementById")
            .and.returnValue({ checked: true});

        var spy_dialog = spyOn($.fn, "dialog");

        visitdate = '99';
        checkOutReasonSelectOptions = 'foo';
        resetCloseDialogFunction = 'bar';

        var result = HomeAppointment.openCheckedInAppointmentWindow(data, templateData, baseTemplate);
        expect(result).toBe("viewCheckedInCommentLink");

        expect(spy_render).toHaveBeenCalledTimes(1);
        expect(spy_render.calls.argsFor(0)).toEqual([
            {changingInfo: visitdate, changingInfoTitle: "Check-In"}]);

        expect(spy_loadAndRenderTemplate).toHaveBeenCalledTimes(1);
        expect(spy_loadAndRenderTemplate.calls.argsFor(0)).toEqual(["#apptDetails_extraRow",
            {changingInfo: visitdate, changingInfoTitle: "Check-In"}]);

        expect(spy_dollar).toHaveBeenCalledTimes(3);
        expect(spy_dollar.calls.argsFor(0)).toEqual(["#appt_wizard_checkedin_visit_details table tbody.forTemplate"]);
        expect(spy_dollar.calls.argsFor(1)).toEqual(["#wizard_checkoutReasons"]);
        expect(spy_dollar.calls.argsFor(2)).toEqual(["#appt_wizard_checkedin_visit_details"]);

        expect(spy_html).toHaveBeenCalledTimes(2);
        expect(spy_html.calls.argsFor(0)).toEqual(['foobar']);
        expect(spy_html.calls.argsFor(1)).toEqual([checkOutReasonSelectOptions]);

        expect(spy_getElementById).toHaveBeenCalledTimes(4);
        expect(spy_getElementById.calls.argsFor(0)).toEqual(["check_visit_activities_no"]);
        expect(spy_getElementById.calls.argsFor(1)).toEqual(["check_visit_activities_yes"]);
        expect(spy_getElementById.calls.argsFor(2)).toEqual(["check_visit_duration_yes"]);
        expect(spy_getElementById.calls.argsFor(3)).toEqual(["check_visit_duration_no"]);

        expect(spy_dialog).toHaveBeenCalledTimes(1);
        expect(spy_dialog.calls.argsFor(0)).toEqual([{
            modal: true,
            close: resetCloseDialogFunction,
            height: 875,
            width: 900
        }]);

    });
});

describe('home_util.js:HomeAppointment.openCheckedOutAppointmentWindow()', function () {

    it('sets up data and opens check out appt window', function () {

        var data = {subjectMrn: 42};
        var templateData = { };
        var baseTemplate = { render: function(){}};

        var spy_render = spyOn(baseTemplate, "render").and.returnValue('foo');
        var spy_loadAndRenderTemplate = spyOn(window, "loadAndRenderTemplate")
            .and.returnValue('bar');

        var spy_dollar = spyOn(window, "$").and.returnValue($.fn);
        var spy_html = spyOn($.fn, "html");

        var spy_getElementById = spyOn(document, "getElementById")
            .and.returnValue({ checked: true});

        var spy_dialog = spyOn($.fn, "dialog");

        visitdate = '99';
        checkOutReasonSelectOptions = 'foo';
        resetCloseDialogFunction = 'bar';

        var result = HomeAppointment.openCheckedOutAppointmentWindow(data, templateData, baseTemplate);
        expect(result).toBe("viewCheckedoutCommentLink");

        expect(spy_render).toHaveBeenCalledTimes(1);
        expect(spy_render.calls.argsFor(0)).toEqual([
            {changingInfo: visitdate, changingInfoTitle: "Checked Out"}]);

        expect(spy_loadAndRenderTemplate).toHaveBeenCalledTimes(1);
        expect(spy_loadAndRenderTemplate.calls.argsFor(0)).toEqual(["#apptDetails_extraRow",
            {changingInfo: visitdate, changingInfoTitle: "Checked Out"}]);

        expect(spy_dollar).toHaveBeenCalledTimes(2);
        expect(spy_dollar.calls.argsFor(0)).toEqual(["#appt_wizard_checkedout_visit_details table tbody.forTemplate"]);
        expect(spy_dollar.calls.argsFor(1)).toEqual(["#appt_wizard_checkedout_visit_details"]);

        expect(spy_html).toHaveBeenCalledTimes(1);
        expect(spy_html.calls.argsFor(0)).toEqual(['foobar']);

        expect(spy_getElementById).toHaveBeenCalledTimes(4);
        expect(spy_getElementById.calls.argsFor(0)).toEqual(["check_visit_activities_yes_readonly"]);
        expect(spy_getElementById.calls.argsFor(1)).toEqual(["check_visit_activities_no_readonly"]);
        expect(spy_getElementById.calls.argsFor(2)).toEqual(["check_visit_duration_yes_readonly"]);
        expect(spy_getElementById.calls.argsFor(3)).toEqual(["check_visit_duration_no_readonly"]);

        expect(spy_dialog).toHaveBeenCalledTimes(1);
        expect(spy_dialog.calls.argsFor(0)).toEqual([{
            modal: true,
            close: resetCloseDialogFunction,
            height: 875,
            width: 900
        }]);

    });
});

describe('home_util.js:HomeAppointment.openCancelledAppointmentWindow()', function () {

    it('sets up data and opens cancel appt window', function () {

        var data = {subjectMrn: 42};
        var templateData = { };
        var baseTemplate = { render: function(){}};

        var spy_render = spyOn(baseTemplate, "render").and.returnValue('foo');
        var spy_loadAndRenderTemplate = spyOn(window, "loadAndRenderTemplate")
            .and.returnValue('bar');

        var spy_dollar = spyOn(window, "$").and.returnValue($.fn);
        var spy_html = spyOn($.fn, "html");

        var spy_dialog = spyOn($.fn, "dialog");

        visitdate = '99';
        visitreasons = 'foo';
        resetCloseDialogFunction = 'bar';

        var result = HomeAppointment.openCancelledAppointmentWindow(data, templateData, baseTemplate);
        expect(result).toBe("viewCancelledCommentLink");

        expect(spy_render).toHaveBeenCalledTimes(1);
        expect(spy_render.calls.argsFor(0)).toEqual([
            {cancelledDate: visitdate, cancellationReason: visitreasons}]);

        expect(spy_loadAndRenderTemplate).toHaveBeenCalledTimes(2);
        expect(spy_loadAndRenderTemplate.calls.argsFor(0)).toEqual(["#apptDetails_cancelRows",
            {cancelledDate: visitdate, cancellationReason: visitreasons}]);
        expect(spy_loadAndRenderTemplate.calls.argsFor(1)).toEqual(["#apptDetails_extraRow",
            {cancelledDate: visitdate, cancellationReason: visitreasons}]);

        expect(spy_dollar).toHaveBeenCalledTimes(2);
        expect(spy_dollar.calls.argsFor(0)).toEqual([
            "#appt_wizard_cancelled_visit_details table tbody.forTemplate"]);
        expect(spy_dollar.calls.argsFor(1)).toEqual(["#appt_wizard_cancelled_visit_details"]);

        expect(spy_html).toHaveBeenCalledTimes(1);
        expect(spy_html.calls.argsFor(0)).toEqual(['foobarbar']);

        expect(spy_dialog).toHaveBeenCalledTimes(1);
        expect(spy_dialog.calls.argsFor(0)).toEqual([{
            modal: true,
            close: resetCloseDialogFunction,
            height: 875,
            width: 900
        }]);
    });
});



