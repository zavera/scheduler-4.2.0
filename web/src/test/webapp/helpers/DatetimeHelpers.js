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
DatetimeHelpers = {};

// verify the minimum date and time set on a datetimepicker
DatetimeHelpers.verifyDatetimePickerMinDatetime = function(element, expectedDate, expectedTime) {
    // minimumDate and minimumTime are strings. The formats are:
    // MM/DD/YYYY for dates, and HH:MM for times
    var minimumDate = element.datepicker('option', 'minDate');
    if (minimumDate instanceof Date) {
        var actualDatetimeString = showDateTime(minimumDate);
        var expectedDatetimeString = expectedDate + ' ' + expectedTime;
        expect(actualDatetimeString).toBe(expectedDatetimeString);
    }
    else {
        // assume it's a string of one of the following forms:
        // 01/02/2003
        // 01/02/2003 04:05
        // 01/02/2003, 04:05
        // hack for determining whether this string contains a time
        if (minimumDate.length == 16 && minimumDate.indexOf(':') == 13) {
            var expectedDatetimeString = expectedDate + ' ' + expectedTime;
            expect(minimumDate).toBe(expectedDatetimeString);
        }
        if (minimumDate.length == 17 && minimumDate.indexOf(':') == 14 && minimumDate.indexOf(',') == 10) {
            var expectedDatetimeString = expectedDate + ', ' + expectedTime;
            expect(minimumDate).toBe(expectedDatetimeString);
        }
        else {
            if (minimumDate.length > 10) {
                console.log('This date is not in one of the expected formats:', minimumDate);
                expect(true).toBe(false);
            }
            else {
                var minimumTime = element.datepicker('option', 'minTime');
                expect(minimumDate).toBe(expectedDate);
                expect(minimumTime).toBe(expectedTime);
            }
        }
    }

};

DatetimeHelpers.verifyDatetime = function(element, expectedDatetime) {

    var actualDatetime= element.datetimepicker('getDate');
    expect(showDateTime(actualDatetime)).toBe(expectedDatetime);

};

