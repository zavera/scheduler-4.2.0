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

var DomVerificationHelper = {};

/**
 * verifies the entire set of CSS classes applied to an element. Order
 * does not matter.
 *
 * This function may not work on some browsers where element.classList is not supported
 * but this is used only in tests, so that should not matter so long as the tests
 * are not run against browsers such as IE9
 */
DomVerificationHelper.verifyCssClasses = function(
    element,
    expectedClasses
) {

    var classList = element.classList ? element.classList : [];
    expect(classList.length).toBe(expectedClasses.length);
    for (var i = 0; i < expectedClasses.length; i++) {
        expect(classList.contains(expectedClasses[i])).toBe(true);
    }

};

DomVerificationHelper.verifyDropDown = function(
    selectElement, expectedOptions
) {

    var options = selectElement.children('option');


    expect(options.length).toBe(expectedOptions.length);
    for (var i = 0; i < expectedOptions.length; i++) {
        var option = options[i];
        var expectedValue = expectedOptions[i].value;
        var expectedText = expectedOptions[i].text;
        expect(options.filter(':eq(' + i + ')').attr('value')).toBe(expectedValue);
        expect(options.filter(':eq(' + i + ')').text()).toBe(expectedText);
    }

};

DomVerificationHelper.verifyAnchor = function(element, href, content) {
    expect(element.length).toBe(1);
    expect(element.is("a")).toBe(true);
    expect(element.attr('href')).toBe(href);
    expect(element.html()).toBe(content);
};

DomVerificationHelper.verifyBreadcrumbs = function(breadcrumbData) {

    var breadcrumbs = $('div.breadcrumbs');
    expect(breadcrumbs.length).toBe(1);

    var breadcrumbAnchors = breadcrumbs.find('a');
    var breadcrumbSpans = breadcrumbs.find('span.breadcrumbSep');
    var expectedOverallText = "";

    breadcrumbData.forEach(function(datum, index) {
        if (datum.href) {
            var anchor = breadcrumbAnchors.eq(index);
            expect(anchor.attr('href')).toBe(datum.href);
            expect(anchor.html()).toBe(datum.text);
            var span = breadcrumbSpans.eq(index);
            expect(span.html()).toBe(' &gt; ');
            expectedOverallText += datum.text + "  >  ";
        }
        else {
            expectedOverallText += datum.text;
            expect(breadcrumbs.text()).toBe(expectedOverallText);
        }
    })

}

DomVerificationHelper.verifyMatchedElements = function(selector, expectations) {

    $(selector).each(function(index, element) {
        expectations.forEach(function(expectation) {
            if (expectation.type == 'css') {
                expect($(element).css(expectation.property)).toBe(expectation.value);
            }
            else if (expectation.type = 'html') {
                expect($(element).html()).toBe(expectation.value);
            }
        });
    });

}

DomVerificationHelper.filterArray = function(array, elementsToRemove, callback) {

    // this works only for primitives
    var filteredArray = array.slice();

    elementsToRemove.forEach(function(exception) {
        callback(exception);
        filteredArray = filteredArray.filter(function(el) {
            return el !== exception;
        });
    });

    return filteredArray;

};
