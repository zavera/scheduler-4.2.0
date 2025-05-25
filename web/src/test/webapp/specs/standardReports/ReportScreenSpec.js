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

describe('report_screen_show.js:report_showDailyOverviewResults()', function() {

    var result_0 = SRH.dailyOverviewResult0;
    var result_1 = SRH.dailyOverviewResult1;

    it("builds 'out'", function() {

        var spy_report_handleDataResponse = spyOn(window, "report_handleDataResponse");
        var spy_showDateTime = spyOn(window, "showDateTime").and.returnValue('DT');
        var spy_showDateTimeWithThreeLetterMonth = spyOn(window, "showDateTimeWithThreeLetterMonth").and.returnValue('DT');
        var spy_util_buildFullName = spyOn(window, "util_buildFullName").and.returnValue('FN');

        var spy_dollar = spyOn(window, "$").and.callThrough();
        var spy_html = spyOn($.fn, "html");

        // set up data needed by report_showDailyOverviewResults()
        report_dailyOverviewResult = [ result_0, result_1 ];

        // the function under test
        report_showDailyOverviewResults();

        expect(spy_report_handleDataResponse).toHaveBeenCalledTimes(1);
        expect(spy_report_handleDataResponse.calls.argsFor(0)).toEqual([2]);

        expect(spy_showDateTime).toHaveBeenCalledTimes(4);
        expect(spy_showDateTimeWithThreeLetterMonth).toHaveBeenCalledTimes(4);

        expect(spy_util_buildFullName).toHaveBeenCalledTimes(2);
        expect(spy_util_buildFullName.calls.argsFor(0)).toEqual(['LOU000', '', 'CRO000']);
        expect(spy_util_buildFullName.calls.argsFor(1)).toEqual(['LOU001', '', 'CRO001']);

        expect(spy_dollar).toHaveBeenCalledTimes(1);
        expect(spy_dollar.calls.argsFor(0)).toEqual(['#report_results']);

        expect(spy_html).toHaveBeenCalledTimes(1);
        UtilHelper.expectEqualCollapse(spy_html.calls.argsFor(0)[0],
            SRH.dailyOverviewOutput1a +
            SRH.dailyOverviewStatusScheduled +
            SRH.dailyOverviewOutput1b +
            SRH.dailyOverviewStatusScheduled +
            SRH.dailyOverviewOutput1c);

        spy_html.calls.reset();
        result_0.visitStatus = 'Checked-In';
        result_1.visitStatus = 'Hold';

        report_showDailyOverviewResults();

        UtilHelper.expectEqualCollapse(spy_html.calls.argsFor(0)[0],
            SRH.dailyOverviewOutput1a +
            SRH.dailyOverviewStatusCheckedIn +
            SRH.dailyOverviewOutput1b +
            SRH.dailyOverviewStatusHold +
            SRH.dailyOverviewOutput1c);
    });
});

describe('report_screen_show.js:report_showOffUnitResults()', function() {

    var result_0 = SRH.offUnitResult0;
    var result_1 = SRH.offUnitResult1;

    report_offUnitResult = [ result_0, result_1, result_1 ];

    it("builds 'out'", function() {

        var spy_report_handleDataResponse = spyOn(window, "report_handleDataResponse");
        var spy_showDateTime = spyOn(window, "showDateTime").and.returnValue('DT');
        var spy_util_buildFullName = spyOn(window, "util_buildFullName").and.returnValue('FN');

        var spy_dollar = spyOn(window, "$").and.callThrough();
        var spy_html = spyOn($.fn, "html");


        report_showOffUnitResults();

        expect(spy_report_handleDataResponse).toHaveBeenCalledTimes(1);
        expect(spy_report_handleDataResponse.calls.argsFor(0)).toEqual([3]);

        expect(spy_showDateTime).toHaveBeenCalledTimes(10);

        expect(spy_util_buildFullName).toHaveBeenCalledTimes(2);
        expect(spy_util_buildFullName.calls.argsFor(0)).toEqual(['MANUAL0', null, 'TESTER000']);
        expect(spy_util_buildFullName.calls.argsFor(1)).toEqual(['MANUAL1', null, 'TESTER001']);

        expect(spy_dollar).toHaveBeenCalledTimes(1);
        expect(spy_dollar.calls.argsFor(0)).toEqual(['#report_results']);

        expect(spy_html).toHaveBeenCalledTimes(1);
        UtilHelper.expectEqualCollapse(spy_html.calls.argsFor(0)[0],
            SRH.offUnitOut1a +
            SRH.offUnitStatusScheduled +
            SRH.offUnitOut1b +
            SRH.offUnitStatusCheckedIn +
            SRH.offUnitOut1c);
    });
});

describe('report_screen_show.js:report_showBillableResourcesResults()', function() {

    var result_0 = SRH.billableResourcesResult0;
    var result_1 = SRH.billableResourcesResult1;

    it("builds 'out'", function() {
        exportStr = "";

        var spy_report_handleDataResponse = spyOn(window, "report_handleDataResponse");
        var spy_showDateTime = spyOn(window, "showDateTime")
            .and.returnValue('DT');
        var spy_util_buildFullName = spyOn(window, "util_buildFullName")
            .and.returnValue('FN');

        var spy_dollar = spyOn(window, "$").and.callThrough();
        var spy_html = spyOn($.fn, "html");

        report_billableResourcesResult = [ result_0, result_1, result_1 ];

        expect(exportStr).toBe("");
        report_showBillableResourcesResults();
        expect(exportStr).toBe("");

        expect(spy_report_handleDataResponse).toHaveBeenCalledTimes(1);
        expect(spy_report_handleDataResponse.calls.argsFor(0)).toEqual([3]);

        expect(spy_showDateTime).toHaveBeenCalledTimes(18);

        expect(spy_util_buildFullName).toHaveBeenCalledTimes(6);
        expect(spy_util_buildFullName.calls.argsFor(0))
            .toEqual(['AUTO', null, 'TESTER100']);
        expect(spy_util_buildFullName.calls.argsFor(1))
            .toEqual(['AutoStudy', '', 'Staff100']);
        expect(spy_util_buildFullName.calls.argsFor(2))
            .toEqual(['AUTO', null, 'TESTER101']);
        expect(spy_util_buildFullName.calls.argsFor(3))
            .toEqual(['AutoStudy', '', 'Staff101']);
        expect(spy_util_buildFullName.calls.argsFor(4))
            .toEqual(['AUTO', null, 'TESTER101']);
        expect(spy_util_buildFullName.calls.argsFor(5))
            .toEqual(['AutoStudy', '', 'Staff101']);

        expect(spy_dollar).toHaveBeenCalledTimes(1);
        expect(spy_dollar.calls.argsFor(0)).toEqual(['#report_results']);

        expect(spy_html).toHaveBeenCalledTimes(1);
        UtilHelper.expectEqualCollapse(spy_html.calls.argsFor(0)[0],
            SRH.billableResourcesOut1 + SRH.billableResourcesOut2);
    });
});
describe('report_screen_show.js:validateReportSubmission()', function() {

    beforeEach(function () {
        this.spy_$ = spyOn(window, "$").and.callThrough();
        this.spy_css = spyOn($.fn, "css");
        this.spy_datepicker =
            spyOn($.fn, "datepicker").and.returnValues(1, 2);

        this.spy_showError = spyOn(window, "showError");
        this.spy_trim = spyOn($, "trim");
        this.spy_val =
            spyOn($.fn, "val").and.returnValue("val");

        this.spy_util_showMainMessage =
            spyOn(window, "util_showMainMessage");
    });

    it("validates dates when valid", function() {
        report_selectedReport = {name: 'blah'};

        var isValid = validateReportSubmission();

        expect(isValid).toBe(true);

        expect(this.spy_css).toHaveBeenCalledTimes(1);
        expect(this.spy_css.calls.argsFor(0)).toEqual([{visibility: "hidden"}]);

        expect(this.spy_datepicker).toHaveBeenCalledTimes(2);
        expect(this.spy_datepicker.calls.argsFor(0)).toEqual(['getDate']);
        expect(this.spy_datepicker.calls.argsFor(1)).toEqual(['getDate']);

        expect(this.spy_showError).toHaveBeenCalledTimes(0);

        expect(this.spy_$).toHaveBeenCalledTimes(3);
        expect(this.spy_$.calls.argsFor(0)).toEqual(['.formElementRequired']);
        expect(this.spy_$.calls.argsFor(1)).toEqual(["#report_startDate"]);
        expect(this.spy_$.calls.argsFor(2)).toEqual(["#report_endDate"]);

        expect(this.spy_trim).toHaveBeenCalledTimes(0);
        expect(this.spy_val).toHaveBeenCalledTimes(0);
        expect(this.spy_util_showMainMessage).toHaveBeenCalledTimes(0);
    });

    it("validates dates with bad start", function() {
        report_selectedReport = {name: 'blah'};

        this.spy_datepicker.and.returnValue(null);

        var isValid = validateReportSubmission();

        expect(isValid).toBe(false);

        expect(this.spy_css).toHaveBeenCalledTimes(1);
        expect(this.spy_css.calls.argsFor(0)).toEqual([{visibility: "hidden"}]);

        expect(this.spy_datepicker).toHaveBeenCalledTimes(2);
        expect(this.spy_datepicker.calls.argsFor(0)).toEqual(['getDate']);
        expect(this.spy_datepicker.calls.argsFor(1)).toEqual(['getDate']);

        expect(this.spy_showError).toHaveBeenCalledTimes(2);
        expect(this.spy_showError.calls.argsFor(0))
            .toEqual(['#report_startDateValidation', 'Please enter valid start date']);
        expect(this.spy_showError.calls.argsFor(1))
            .toEqual(['#report_endDateValidation', 'Please enter valid end date']);

        expect(this.spy_$).toHaveBeenCalledTimes(3);
        expect(this.spy_$.calls.argsFor(0)).toEqual(['.formElementRequired']);
        expect(this.spy_$.calls.argsFor(1)).toEqual(["#report_startDate"]);
        expect(this.spy_$.calls.argsFor(2)).toEqual(["#report_endDate"]);

        expect(this.spy_trim).toHaveBeenCalledTimes(0);
        expect(this.spy_val).toHaveBeenCalledTimes(0);
        expect(this.spy_util_showMainMessage).toHaveBeenCalledTimes(0);
    });

    it("validates dates with bad sequence", function() {
        report_selectedReport = {name: 'blah'};

        this.spy_datepicker.and.returnValues(2, 1);

        var isValid = validateReportSubmission();

        expect(isValid).toBe(false);

        expect(this.spy_css).toHaveBeenCalledTimes(1);
        expect(this.spy_css.calls.argsFor(0)).toEqual([{visibility: "hidden"}]);

        expect(this.spy_datepicker).toHaveBeenCalledTimes(2);
        expect(this.spy_datepicker.calls.argsFor(0)).toEqual(['getDate']);
        expect(this.spy_datepicker.calls.argsFor(1)).toEqual(['getDate']);

        expect(this.spy_showError).toHaveBeenCalledTimes(2);
        expect(this.spy_showError.calls.argsFor(0))
            .toEqual(['#report_startDateValidation', 'Please enter valid date range']);
        expect(this.spy_showError.calls.argsFor(1))
            .toEqual(['#report_endDateValidation', 'Please enter valid date range']);

        expect(this.spy_$).toHaveBeenCalledTimes(3);
        expect(this.spy_$.calls.argsFor(0)).toEqual(['.formElementRequired']);
        expect(this.spy_$.calls.argsFor(1)).toEqual(["#report_startDate"]);
        expect(this.spy_$.calls.argsFor(2)).toEqual(["#report_endDate"]);

        expect(this.spy_trim).toHaveBeenCalledTimes(0);
        expect(this.spy_val).toHaveBeenCalledTimes(0);
        expect(this.spy_util_showMainMessage).toHaveBeenCalledTimes(0);
    });

    it("validates dates with bad mrn", function() {
        report_selectedReport = {name: 'subject_audit_staff_views'};

        this.spy_datepicker.and.returnValues(1, 2);
        this.spy_trim.and.returnValue("");

        var isValid = validateReportSubmission();

        expect(isValid).toBe(false);

        expect(this.spy_showError).toHaveBeenCalledTimes(1);
        expect(this.spy_showError.calls.argsFor(0)).toEqual(['#report_mrnValidation']);

        expect(this.spy_trim).toHaveBeenCalledTimes(1);
        expect(this.spy_val).toHaveBeenCalledTimes(1);
        expect(this.spy_util_showMainMessage).toHaveBeenCalledTimes(0);
    });

    it("validates dates with bad dob", function() {
        report_selectedReport = {name: 'study_subject_visit'};

        this.spy_datepicker.and.returnValues(1, 2);
        this.spy_trim.and.returnValue("");

        var isValid = validateReportSubmission();

        expect(isValid).toBe(false);

        expect(this.spy_showError).toHaveBeenCalledTimes(0);

        expect(this.spy_trim).toHaveBeenCalledTimes(3);
        expect(this.spy_val).toHaveBeenCalledTimes(3);

        expect(this.spy_util_showMainMessage).toHaveBeenCalledTimes(1);
        expect(this.spy_util_showMainMessage.calls.argsFor(0))
            .toEqual(["Please enter value for at least one of the fields."]);
    });
});
describe('report_screen_show.js:adjustDisplayOfSortAndFilter()', function() {

    beforeEach(function () {
        this.spy_$ = spyOn(window, "$").and.callThrough();
        this.spy_css = spyOn($.fn, "css");
    });

    it("adjusts for visits_flagged_edit", function() {
        report_selectedReport = {name: 'visits_flagged_edit'};

        adjustDisplayOfSortAndFilter();

        expect(this.spy_$).toHaveBeenCalledTimes(2);
        expect(this.spy_$.calls.argsFor(0)).toEqual(['#filter_functionality']);
        expect(this.spy_$.calls.argsFor(1)).toEqual(['#sort_functionality']);

        expect(this.spy_css).toHaveBeenCalledTimes(2);
        expect(this.spy_css.calls.argsFor(0)).toEqual([{display: "none"}]);
        expect(this.spy_css.calls.argsFor(1)).toEqual([{display: "none"}]);
    });

    it("adjusts for study_data", function() {
        report_selectedReport = {name: 'study_data'};

        adjustDisplayOfSortAndFilter();

        expect(this.spy_$).toHaveBeenCalledTimes(2);
        expect(this.spy_$.calls.argsFor(0)).toEqual(['#filter_functionality']);
        expect(this.spy_$.calls.argsFor(1)).toEqual(['#sort_functionality']);

        expect(this.spy_css).toHaveBeenCalledTimes(2);
        expect(this.spy_css.calls.argsFor(0)).toEqual([{display: "inline-block"}]);
        expect(this.spy_css.calls.argsFor(1)).toEqual([{display: "none"}]);
    });

    it("adjusts for other", function() {
        report_selectedReport = {name: 'other'};

        adjustDisplayOfSortAndFilter();

        expect(this.spy_$).toHaveBeenCalledTimes(2);
        expect(this.spy_$.calls.argsFor(0)).toEqual(['#filter_functionality']);
        expect(this.spy_$.calls.argsFor(1)).toEqual(['#sort_functionality']);

        expect(this.spy_css).toHaveBeenCalledTimes(2);
        expect(this.spy_css.calls.argsFor(0)).toEqual([{display: "inline-block"}]);
        expect(this.spy_css.calls.argsFor(1)).toEqual([{display: "inline-block"}]);
    });

});
