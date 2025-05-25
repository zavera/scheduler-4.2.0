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
describe('Daily overview report', function() {

    it('displays a date range using 3-letter month format as well as 13 information fields for each visit', function() {

        FixtureHelper.loadTestHtmlFixtureIntoDom("standardReports/html/elements-for-standard-reports.html");

        // report_dailyOverviewResult is a global variable
        report_dailyOverviewResult = FixtureHelper.getTestJsonFixture("standardReports/json/daily-overview-report-data.json");

        report_showDailyOverviewResults();

        var reportDatesDiv = $('.mainName');
        expect(reportDatesDiv.length).toBe(2);
        expect(reportDatesDiv.eq(0).html()).toBe("Jan 30, 2017 11:00 - Mar 20, 2017 15:00"); // as per JSON input file
        expect(reportDatesDiv.eq(1).html()).toBe("Apr 29, 2016 22:00 - May 21, 2016 01:00"); // as per JSON input file

        var statusBlock1 = $('.statusBlock').eq(0);
        var labels1 = statusBlock1.children('.label');
        var values1 = statusBlock1.children('.value');

        var statusBlock2 = $('.statusBlock').eq(1);
        var labels2 = statusBlock2.children('.label');
        var values2 = statusBlock2.children('.value');

        expect(labels1.length).toBe(13);
        expect(values1.length).toBe(13);

        expect(labels2.length).toBe(13);
        expect(values2.length).toBe(13);

        expect(labels1.eq(0).html()).toBe("Subject");
        expect(labels1.eq(1).html()).toBe("MRN");
        expect(labels1.eq(2).html()).toBe("Gender");
        expect(labels1.eq(3).html()).toBe("Birth Date");
        expect(labels1.eq(4).html()).toBe("Local ID");
        expect(labels1.eq(5).html()).toBe("IRB #");
        expect(labels1.eq(6).html()).toBe("Visit Name");
        expect(labels1.eq(7).html()).toBe("Visit Type");
        expect(labels1.eq(8).html()).toBe("Sublocation");
        expect(labels1.eq(9).html()).toBe("Is part of the Visit Off Unit?");
        expect(labels1.eq(10).html()).toBe("Check-In");
        expect(labels1.eq(11).html()).toBe("Check-Out");
        expect(labels1.eq(12).html()).toBe("Comment");

        expect(values1.eq(0).html()).toBe("ORENTHAL CART");
        expect(values1.eq(1).html()).toBe("888031");
        expect(values1.eq(2).html()).toBe("Female to Male");
        expect(values1.eq(3).html()).toBe("12/27/1902");
        expect(values1.eq(4).html()).toBe("AS001");
        expect(values1.eq(5).html()).toBe("2003AS001");
        expect(values1.eq(6).html()).toBe("In AS001 CNY");
        expect(values1.eq(7).html()).toBe("Inpatient CRC");
        expect(values1.eq(8).html()).toBe("Sub-Location 2");
        expect(values1.eq(9).html()).toBe("NO");
        expect(values1.eq(10).html()).toBe("");
        expect(values1.eq(11).html()).toBe("");
        expect(values1.eq(12).html()).toBe("");

        expect(labels2.eq(0).html()).toBe("Subject");
        expect(labels2.eq(1).html()).toBe("MRN");
        expect(labels2.eq(2).html()).toBe("Gender");
        expect(labels2.eq(3).html()).toBe("Birth Date");
        expect(labels2.eq(4).html()).toBe("Local ID");
        expect(labels2.eq(5).html()).toBe("IRB #");
        expect(labels2.eq(6).html()).toBe("Visit Name");
        expect(labels2.eq(7).html()).toBe("Visit Type");
        expect(labels2.eq(8).html()).toBe("Sublocation");
        expect(labels2.eq(9).html()).toBe("Is part of the Visit Off Unit?");
        expect(labels2.eq(10).html()).toBe("Check-In");
        expect(labels2.eq(11).html()).toBe("Check-Out");
        expect(labels2.eq(12).html()).toBe("Comment");

        expect(values2.eq(0).html()).toBe("WYNONA ANN GRAE");
        expect(values2.eq(1).html()).toBe("888033");
        expect(values2.eq(2).html()).toBe("Female");
        expect(values2.eq(3).html()).toBe("10/25/1946");
        expect(values2.eq(4).html()).toBe("AS002");
        expect(values2.eq(5).html()).toBe("2003AS002");
        expect(values2.eq(6).html()).toBe("In AS002 CNY");
        expect(values2.eq(7).html()).toBe("Outpatient CRC");
        expect(values2.eq(8).html()).toBe("Sub-Location 4");
        expect(values2.eq(9).html()).toBe("YES");
        expect(values2.eq(10).html()).toBe("");
        expect(values2.eq(11).html()).toBe("");
        expect(values2.eq(12).html()).toBe("");

    });

});



