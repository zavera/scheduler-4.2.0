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
describe('Subject page MRN validator', function() {

    it('accepts numeric-only MRNs', function () {

        var mrn = "123";
        expect(mrnSearchValidator(mrn)).toBe(true);

    });

    it('rejects MRNs containing letters', function () {

        var mrn = "ABC";
        expect(mrnSearchValidator(mrn)).toBe(false);

    });

});

describe('Subjects Details', function() {

    it('Verify that values in HTML are based on subject details', function() {
        var subjectId = 100000014;
        var subjectDetails = {
            address : {
                addressLine1 : "Maple St.",
                addressLine2 : "APT 1",
                city : "A City",
                stateName : "MA",
                zipCode : "00301",
                countryName : "United States"
            },
            partnersUid : "100000014",
            primaryContactNumber : "(555) 555-5555",
            secondaryContactNumber : "(555) 555-5551",
            ethnicityName : "Unknown or Not Reported",
            comment : "A comment",
            raceName : "WHITE"
        };

        var theSubjectDetails = generateSubjectDetailsHtmlForSubjectSearch(subjectDetails, subjectId);

        expect(theSubjectDetails.indexOf(subjectDetails.address.addressLine1) !== -1).toBe(true);
        expect(theSubjectDetails.indexOf(subjectDetails.address.addressLine2) !== -1).toBe(true);
        expect(theSubjectDetails.indexOf(subjectDetails.address.city) !== -1).toBe(true);
        expect(theSubjectDetails.indexOf(subjectDetails.address.stateName) !== -1).toBe(true);
        expect(theSubjectDetails.indexOf(subjectDetails.address.zipCode) !== -1).toBe(true);
        expect(theSubjectDetails.indexOf(subjectDetails.address.countryName) !== -1).toBe(true);
        expect(theSubjectDetails.indexOf(subjectDetails.partnersUid) !== -1).toBe(true);
        expect(theSubjectDetails.indexOf("," + subjectDetails.partnersUid) !== -1).toBe(true);
        expect(theSubjectDetails.indexOf(subjectDetails.primaryContactNumber) !== -1).toBe(true);
        expect(theSubjectDetails.indexOf(subjectDetails.secondaryContactNumber) !== -1).toBe(true);
        expect(theSubjectDetails.indexOf(subjectDetails.ethnicityName) !== -1).toBe(true);
        expect(theSubjectDetails.indexOf(subjectDetails.comment) !== -1).toBe(true);
        expect(theSubjectDetails.indexOf(subjectDetails.raceName) !== -1).toBe(true);

        expect(theSubjectDetails.indexOf("Street Address 1") !== -1).toBe(true);
        expect(theSubjectDetails.indexOf("Street Address 2") !== -1).toBe(true);
        expect(theSubjectDetails.indexOf("City") !== -1).toBe(true);
        expect(theSubjectDetails.indexOf("State") !== -1).toBe(true);
        expect(theSubjectDetails.indexOf("Country") !== -1).toBe(true);
        expect(theSubjectDetails.indexOf("Zipcode") !== -1).toBe(true);
        expect(theSubjectDetails.indexOf("Primary Phone") !== -1).toBe(true);
        expect(theSubjectDetails.indexOf("Secondary Phone") !== -1).toBe(true);
        expect(theSubjectDetails.indexOf("Race") !== -1).toBe(true);
        expect(theSubjectDetails.indexOf("Ethnicity") !== -1).toBe(true);
    });

    it('Verify that values in HTML are defaulted to zero', function() {
        var subjectId = 100000014;
        var emptyObject = {address : {}};

        var subjectDetails = {
            address : {
                addressLine1 : "Maple St.",
                addressLine2 : "APT 1",
                city : "A City",
                stateName : "MA",
                zipCode : "00301",
                countryName : "United States"
            },
            partnersUid : "100000014",
            primaryContactNumber : "(555) 555-5555",
            secondaryContactNumber : "(555) 555-5551",
            ethnicityName : "Unknown or Not Reported",
            comment : "A comment",
            raceName : "WHITE"
        };

        var theSubjectDetails = generateSubjectDetailsHtmlForSubjectSearch(emptyObject, subjectId);

        expect(theSubjectDetails.indexOf(subjectDetails.address.addressLine1) === -1).toBe(true);
        expect(theSubjectDetails.indexOf(subjectDetails.address.addressLine2) === -1).toBe(true);
        expect(theSubjectDetails.indexOf(subjectDetails.address.city) === -1).toBe(true);
        expect(theSubjectDetails.indexOf(subjectDetails.address.stateName) === -1).toBe(true);
        expect(theSubjectDetails.indexOf(subjectDetails.address.zipCode) === -1).toBe(true);
        expect(theSubjectDetails.indexOf(subjectDetails.address.countryName) === -1).toBe(true);
        expect(theSubjectDetails.indexOf("," + subjectDetails.partnersUid) === -1).toBe(true);
        expect(theSubjectDetails.indexOf(subjectDetails.primaryContactNumber) === -1).toBe(true);
        expect(theSubjectDetails.indexOf(subjectDetails.secondaryContactNumber) === -1).toBe(true);
        expect(theSubjectDetails.indexOf(subjectDetails.ethnicityName) === -1).toBe(true);
        expect(theSubjectDetails.indexOf(subjectDetails.comment) === -1).toBe(true);
        expect(theSubjectDetails.indexOf(subjectDetails.raceName) === -1).toBe(true);

        expect(theSubjectDetails.indexOf("Street Address 1") !== -1).toBe(true);
        expect(theSubjectDetails.indexOf("Street Address 2") !== -1).toBe(true);
        expect(theSubjectDetails.indexOf("City") !== -1).toBe(true);
        expect(theSubjectDetails.indexOf("State") !== -1).toBe(true);
        expect(theSubjectDetails.indexOf("Country") !== -1).toBe(true);
        expect(theSubjectDetails.indexOf("Zipcode") !== -1).toBe(true);
        expect(theSubjectDetails.indexOf("Primary Phone") !== -1).toBe(true);
        expect(theSubjectDetails.indexOf("Secondary Phone") !== -1).toBe(true);
        expect(theSubjectDetails.indexOf("Race") !== -1).toBe(true);
        expect(theSubjectDetails.indexOf("Ethnicity") !== -1).toBe(true);
    });

    it('Verify HTML that is outputted is exactly as we expect it without spaces', function() {
        var subjectId = 100000014;

        var subjectDetails = {
            address : {
                addressLine1 : "Maple St.",
                addressLine2 : "APT 1",
                city : "A City",
                stateName : "MA",
                zipCode : "00301",
                countryName : "United States"
            },
            primaryContactNumber : "(555) 555-5555",
            secondaryContactNumber : "(555) 555-5551",
            ethnicityName : "Unknown or Not Reported",
            comment : "A comment",
            raceName : "WHITE"
        };

        var theSubjectDetails = generateSubjectDetailsHtmlForSubjectSearch(subjectDetails, subjectId);
        var expectedHTML = "<div style='margin-left:10px;'class='roundBorder6'>" +
            "<div style='float:left;'>" +
            "<div style='float:left;'>" +
            "<table class='subjectDetailsTable'>"+
            "<tr>" + "<td>Street Address 1</td><td id='subject_address1_100000014'>MapleSt.</td></tr>" +
            "<tr><td>StreetAddress2</td><td id='subject_address2_100000014'>APT1</td></tr>" +
            "<tr><td>City</td><tdid='subject_city_100000014'>ACity</td></tr>" +
            "<tr><td>State</td><tdid='subject_stateName_100000014'>MA</td></tr>" +
            "<tr><td>Zipcode</td><tdid='subject_zipcode_100000014'>00301</td></tr>" +
            "<tr><td>Country</td><tdid='subject_countryName_100000014'>UnitedStates</td></tr>" +
            "</table>" +
            "</div>" +
            "<div style='float:left;margin-left:50px'>" +
            "<table class='subjectDetailsTable'>" +
            "<tr><td>PrimaryPhone</td><td id='subject_primaryContactNumber_100000014'>(555)555-5555</td></tr>" +
            "<tr><td>SecondaryPhone</td><td id='subject_secondaryContactNumber_100000014'>(555)555-5551</td></tr>" +
            "<tr><td>Race</td><td id='subject_raceName_100000014'>WHITE</td></tr>" +
            "<tr><td>Ethnicity</td><td id='subject_ethnicityName_100000014'>UnknownorNotReported</td></tr>" +
            "</table></div></div><div style='visibility:hidden;'id='subject_comment_100000014'>Acomment</div>" +
            "<div id='mrnAndMoreActions'><spanstyle='height:25px;'>" +
            "<span><spanid='mrnLabel'>MRN:</span><spanstyle='vertical-align:text-bottom'>" +
            "<input style='vertical-align:bottom; display:none;' id='subject_mrn_100000014'data-subjectid='100000014'" +
            "class='mrnListingeasyui-combobox'/></span></span>" +
            "<span class='moreActions'><a href='javascript:goToSubjectEntryForm(100000014)'>MoreActions</a></span></div></div>" +
            "<div style='clear:both;'/></div>";
        expect(theSubjectDetails.replace(/\s/g, '')).toEqual(expectedHTML.replace(/\s/g, ''));

    });
});

describe('Subject page initializer', function() {

    it('erases the license, runs the idle timer, and calls commonData()', function() {

        FixtureHelper.loadSourceHtmlFixtureIntoDom("subject_search.html");

        sessionStorage.setItem("subjectMode", "default");
        sessionStorage.setItem("reloadPreviousSearch", false);

        spyOn(window, 'eraseLicense');
        spyOn(window, 'app_runIdleTimer');
        spyOn(window, 'commonData');
        spyOn($, 'get');
        spyOn(window, 'renderBreadcrumbs');
        spyOn(window, 'initFooter');
        spyOn(window, 'getSubjectStaticLists');
        spyOn(window, 'getNonClosedStudiesList');
        spyOn(window, 'enableEPICFeatures');
        spyOn(WidgetUtil, 'createDatepicker');

        subjectPage.init();

        expect(window.eraseLicense).toHaveBeenCalledTimes(1);
        expect(window.eraseLicense).toHaveBeenCalledWith();

        expect(window.app_runIdleTimer).toHaveBeenCalledTimes(1);
        expect(window.app_runIdleTimer).toHaveBeenCalledWith();

        expect(window.commonData).toHaveBeenCalledTimes(1);
        expect(window.commonData).toHaveBeenCalledWith();

        expect($.get).toHaveBeenCalledTimes(1);
        expect($.get).toHaveBeenCalledWith("meta_headers.html", jasmine.any(Function));

        expect(window.renderBreadcrumbs).toHaveBeenCalledTimes(1);
        expect(window.renderBreadcrumbs).toHaveBeenCalledWith("subject_screen");

        expect(window.initFooter).toHaveBeenCalledTimes(1);
        expect(window.initFooter).toHaveBeenCalledWith();

        expect(window.getSubjectStaticLists).toHaveBeenCalledTimes(1);
        expect(window.getSubjectStaticLists).toHaveBeenCalledWith(jasmine.any(Function));

        expect(window.getNonClosedStudiesList).toHaveBeenCalledTimes(1);
        expect(window.getNonClosedStudiesList).toHaveBeenCalledWith();

        expect(window.enableEPICFeatures).toHaveBeenCalledTimes(1);
        expect(window.enableEPICFeatures).toHaveBeenCalledWith();

        expect(WidgetUtil.createDatepicker).toHaveBeenCalledTimes(1);
        expect(WidgetUtil.createDatepicker).toHaveBeenCalledWith(
            "#subject_search_birthdate",
            jasmine.objectContaining({
                yearRange: "-120:+0",
                maxDate: "0D",
                constrainInput: true,
                buttonImage:"css/images/calendar_icn_white_bg.png"
            })
        );

    })
})
