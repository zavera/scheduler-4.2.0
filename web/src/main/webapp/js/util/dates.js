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
Date.prototype.addDays = function (days) {
    var date = new Date(this.valueOf());
    date.setDate(date.getDate() + days);
    return date;
};
Date.prototype.subtractDays = function (days) {
    var date = new Date(this.valueOf());
    date.setDate(date.getDate() - days);
    return date;
};


function dateToDateHelperFormat(date) {
    if (date) {
        var weekdays = ['Sun', 'Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat'];
        var months = ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec'];
        var EEE = weekdays[date.getDay()];
        var MMM = months[date.getMonth()];
        var dd = twoPad(date.getDate()); // 1-31
        var yyyy = date.getFullYear();
        var HH = twoPad(date.getHours());
        var mm = twoPad(date.getMinutes());
        var ss = twoPad(date.getSeconds());
        return EEE + " " + MMM + " " + dd + " " + yyyy + " " + HH + ":" + mm + ":" + ss;
    } else {
        return "";
    }
}

function showDate(value) {
    if (value) {
        return dateFormat(value, 'mm/dd/yyyy');
    } else {
        return "";
    }
}

function showMilitaryTimeAndMonthDay(value) {
    if (value) {
        return dateFormat(value, 'HH:MM mm/dd');
    }
    else {
        return "";
    }
}

function showMilitaryTime(value) {
    if (value) {
        return dateFormat(value, 'HH:MM');
    }
    else {
        return "";
    }
}

function showClockDateTime(value) {
    if (value) {
        return dateFormat(value, 'mm/dd/yyyy hh:MM tt');
    } else {
        return "";
    }
}

function addMinutes(date, minutes) {
    return new Date(date.getTime() + minutes * ONE_MINUTE);
}

function showDateTime(value) {
    if (value) {
        return dateFormat(value, 'mm/dd/yyyy HH:MM');
    } else {
        return "";
    }
}

function showDateTimeWithThreeLetterMonth(value) {
    if (value) {
        return dateFormat(value, 'mmm dd, yyyy HH:MM');
    } else {
        return "";
    }
}

function isValidDate(dateString) {
    // First check for the pattern
    if (!/^\d{1,2}\/\d{1,2}\/\d{4}$/.test(dateString)) {
        return false;
    }
    // Parse the date parts to integers
    var parts = dateString.split("/");
    var day = parseInt(parts[1], 10);
    var month = parseInt(parts[0], 10);
    var year = parseInt(parts[2], 10);
    // Check the ranges of month and year
    if (year < 1000 || year > 3000 || month === 0 || month > 12) {
        return false;
    }
    var monthLength = [31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31];
    // Adjust for leap years
    if (isLeapYear(year)) {
        monthLength[1] = 29;
    }
    // Check the range of the day
    return day > 0 && day <= monthLength[month - 1];
}

function isLeapYear(year) {
    return (year % 400 === 0 ) || (year % 100 !== 0 && year % 4 === 0)
}


//this function checks if a specified birthdate is valid
//a date of birth cannot be later than the current date
function isValidDOB(dobString) {
    if (!dobString || !isValidDate(dobString)) {
        return false;
    }

    var DOB = new Date(dobString);

    //check if DOB is not after the current date
    //hours set to 10 in order to handle daylight savings time
    DOB.setHours(10, 0, 0, 0);
    var today = new Date();
    today.setHours(10, 0, 0, 0);
    return (DOB < today);
}

function showTime(value) {
    if (value) {
        return dateFormat(value, 'HH:MM');
    } else {
        return "";
    }
}

function showDaysHoursAndMinutes(minutes) {
    var days = minutesToDays(minutes);
    return "Day " + (days + 1) + " " + showHoursAndMinutes(minutes);
}

function showHoursAndMinutes(minutes) {
    var leftOverMinutes = minutesIntoDay(minutes);
    var hours = Math.floor(leftOverMinutes / MINS_PER_HOUR);
    leftOverMinutes = leftOverMinutes - hours * MINS_PER_HOUR;
    return showTwoDigits(hours) + ":" + showTwoDigits(leftOverMinutes);
}

function showStandardTime(value) {
    if (value) {
        var hours24 = parseInt(value.substring(0, 2), 10);
        var hours = ((hours24 + 11) % 12) + 1;
        var amPm = hours24 > 11 ? 'pm' : 'am';
        var minutes = value.substring(2);
        return hours + minutes + ' ' + amPm;
    } else {
        return "";
    }
}

function showStandardDateTime(value) {
    if (value) {
        return dateFormat(value, 'mm/dd/yyyy hh:MM tt');
    } else {
        return "";
    }
}

function twoPad(num) {
    return zeroPad(num, 2);
}

// from http://stackoverflow.com/questions/2998784/how-to-output-integers-with-leading-zeros-in-javascript
function zeroPad(num, size) {
    var s = num + "";
    while (s.length < size) {
        s = "0" + s;
    }
    return s;
}

function roundToQuarterHour(minutes) {
    var roundedMinutes =
        minutes == 0 ? 0 :
            (minutes <= 15 ? 15 :
                    (minutes <= 30 ? 30 :
                            (minutes <= 45 ? 45 : 59)
                    )
            );
    return roundedMinutes;
}

function roundDateToQuarterHour(date) {
    var roundedDate = new Date(date);
    var minutes = roundedDate.getMinutes();
    var roundedMinutes = roundToQuarterHour(minutes);
    roundedDate.setMinutes(roundedMinutes);
    roundedDate.setMilliseconds(0);
    return roundedDate;
}

function resourceTimelineDateFromMinutes(minutes) {
    var milliseconds = templateResourceOriginDate.getTime() + minutes * 60 * 1000;
    return new Date(milliseconds);
}

// parsing a formatted date is a trick to avoid offset due to time-zone difference
function resourceTimelineDateToMinutes(formattedDate) {
    var date = new Date(formattedDate);
    var milliseconds = date.getTime();
    var originMilliseconds = templateResourceOriginDate.valueOf();
    var minutes = Math.floor((milliseconds - originMilliseconds) / (60 * 1000));
    return minutes;
}
