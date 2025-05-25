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

var debug = function (log_txt) {
    if (window.console != undefined) {
        console.log(log_txt);
    }
};

function checkRegexp(s, regexp) {
    return regexp.test(s);
}

function checkPassword(s) {
    if (checkRegexp(s, /[a-z]/) && checkRegexp(s, /[A-Z]/) && checkRegexp(s, /\d/) && checkRegexp(s, /\W/)) {
        return true;
    }
    return false;
}

function buildSelectOptionsFromStringList(list, choiceLabel) {
	var html = [];
	if (choiceLabel !== undefined && choiceLabel !== null) {
		html[html.length] = "<option value=''>" + choiceLabel + "</option>";
	}
	for (var i = 0, len = list.length; i < len; i++) {
		html[html.length] = "<option value='";
		html[html.length] = list[i];
		html[html.length] = "'>";
		html[html.length] = list[i];
		html[html.length] = "</option>";
	}
	return html.join('');
}

function buildSelectOptions(list, optionTextProperty, choiceLabel) {
    if (list !== undefined) {
	var html = [];
	if (choiceLabel !== undefined && choiceLabel !== null && choiceLabel != "") {
		html[html.length] = "<option value=''>" + choiceLabel + "</option>";
	}
	for (var i = 0, len = list.length; i < len; i++) {
		html[html.length] = "<option value='";
		html[html.length] = list[i].id;
		html[html.length] = "'>";
		html[html.length] = list[i][optionTextProperty];
		html[html.length] = "</option>";
	}
	return html.join('');
}
}

function buildRoleSelectOptions(list, optionTextProperty, choiceLabel) {
    var html = [];
    if (choiceLabel) {
        html[html.length] = "<option value=''>" + choiceLabel + "</option>";
    }
    for (var i = 0, len = list.length; i < len; i++) {
        html[html.length] = "<option value='";
        html[html.length] = list[i].type;
        html[html.length] = "'>";
        html[html.length] = list[i][optionTextProperty];
        html[html.length] = "</option>";
    }
    return html.join('');
}

function buildStudySelectsOptions(list, choiceLabel) {
    var html = [];
    if (choiceLabel !== undefined && choiceLabel !== null) {
        html[html.length] = "<option value=''>" + choiceLabel + "</option>";
    }
    for (var i = 0, len = list.length; i < len; i++) {
        html[html.length] = "<option value='";
        html[html.length] = list[i].id;
        html[html.length] = "'>";
        html[html.length] = list[i].localId + ' - ' + list[i].name.substring(0, 50);
        html[html.length] = "</option>";
    }
    return html.join('');
}

function getAttributeValueByOtherAttributeValue(list, returnAttributeName, lookupAttributeName, lookupValue) {
    var match;
    $.each(list, function (index, value) {
        if (value[lookupAttributeName] === lookupValue) {
            match = value[returnAttributeName];
        }
    });
    return match;
}

//this needs to be run AFTER the footer is added; which is important for the new template/loading pattern
//it will be run once in the document.ready below, but if the element isn't there yet to be set,
//then the first run will be a no-op.
function setVersionData() {
    $.get("rest/app/versionInfo", {}, function (data) {
        var parsedData = JSON.parse(data);
        $("#version_data").text("Version: " + parsedData.version.split("-")[0] + " (" + parsedData.buildDate.split(" ")[0] + ")");
        sessionStorage.setItem("subjectMode", parsedData.subjectMode);
    });
}

function checkLogin() {
    if (getURLParameter("error")) {
        $('#index_loginError')
            .text('Invalid username and/or password.  Please try again.');
        $('#index_loginError')
            .css({
                display: 'block'
            });
        $('#index_loginError')
            .css({
                opacity: 0.0,
                visibility: "visible"
            })
            .animate({
                opacity: 1.0
            });
    }
    if (localStorage.getItem('timeout')) {
        DialogsUtil.showMessageDialog("You have been automatically logged out due to inactivity.",
            removeLogoutWindow, true, "Dismiss", "", true);

        localStorage.removeItem('timeout');
    }
}


function getURLParameter(name) {
    var result = decodeURIComponent((new RegExp('[?|&]' + name + '=' + '([^&;]+?)(&|#|;|$)')
            .exec(location.search) || [, ""])[1].replace(/\+/g, '%20')) || null;
    return result;
}

function removeLogoutWindow() {
    $('#main_dialog_wrapper')
        .css({
            opacity: 1.0,
            visibility: "hidden"
        })
        .animate({
            opacity: 0.0
        });
}

function util_buildFullName(first, middle, last) {
    var middleToken = "";
    if (first !== undefined && first !== null && first.length > 0) {
        if (middle !== undefined && middle !== null && middle.length > 0) {
            middleToken = middle + " ";
        }
        return first + " " + middleToken + last;
    } else {
        return last;
    }
}

function util_showMainMessage(title, onUnblock) {
    $.blockUI({
        message: title,
        fadeIn: 700,
        fadeOut: 700,
        timeout: 2500,
        showOverlay: true,
        centerY: true,
        onUnblock: onUnblock,
        css: {
            width: '350px',
            border: 'none',
            padding: '20px',
            'font-size': '17px',
            backgroundColor: '#0A507D',
            '-webkit-border-radius': '10px',
            '-moz-border-radius': '10px',
            color: '#fff'
        }
    });
}

function util_day(day) {
  if (day == 1) {
    return 'Sun';
  } else if (day == 2) {
    return 'Mon';
  } else if (day == 3) {
    return 'Tues';
  } else if (day == 4) {
    return 'Wed';
  } else if (day == 5) {
    return 'Thur';
  } else if (day == 6) {
    return 'Fri';
  } else if (day == 7) {
    return 'Sat';
  }
}

function day_option_for_multi_select(day) {
  return day_option_maybe_selected(day, -1); // do not select
}
function day_option_maybe_selected(dayCandidate, daySelected) {
  var selectedString = (dayCandidate == daySelected) ? ' selected' : "";

  var result = "<option value=\"" + dayCandidate + "\"" + selectedString + ">" + util_day(dayCandidate) + "</option>";

  return result;
}

function util_formatDaysHrsMins(days, hours, minutes) {
    if (days > 0 && hours > 0 && minutes > 0) {
        var formattedString = days + (days == 1 ? " Day " : " Days ") + hours + (hours == 1 ? " Hr " : " Hrs ") + minutes + (minutes == 1 ? " Min" : " Mins");
        return formattedString;
    }
    if (days > 0 && (hours == 0 || hours == "") && minutes > 0) {
        var formattedString = days + (days == 1 ? " Day " : " Days ") + minutes + (minutes == 1 ? " Min" : " Mins");
        return formattedString;
    }
    if (days > 0 && hours > 0 && (minutes == 0 || minutes == "")) {
        var formattedString = days + (days == 1 ? " Day " : " Days ") + hours + (hours == 1 ? " Hr " : " Hrs ");
        return formattedString;
    }
    if (days > 0 && (hours == 0 || hours == "") && (minutes == 0 || minutes == "")) {
        var formattedString = days + (days == 1 ? " Day " : " Days ");
        return formattedString;
    }
    if ((days == 0 || days == "") && hours > 0 && minutes > 0) {
        var formattedString = hours + (hours == 1 ? " Hr " : " Hrs ") + minutes + (minutes == 1 ? " Min" : " Mins");
        return formattedString;
    }
    if ((days == 0 || days == "") && hours > 0 && (minutes == 0 || minutes == "")) {
        var formattedString = hours + (hours == 1 ? " Hr " : " Hrs ");
        return formattedString;
    }
    if ((days == 0 || days == "") && (hours == 0 || hours == "")) {
        var formattedString = minutes + (minutes == 1 ? " Min" : " Mins");
        return formattedString;
    }
}

jQuery.loadCSS = function (url) {
    if (!$('link[href="' + url + '"]').length) {
        $('head').append('<link rel="stylesheet" type="text/css" media="screen, projection" href="' + url + '">');
    }
};

// There are instances of className being an array of length 1, instead of a string.
// The following is a patch, not having taken the time to find where the className
// is wrongly set to an array
// It might have worked in the past because 'a' == ['a'] but 'a' !==['a'].
// In addition a case statement works like ===
// FIXME-XH : find where className is set to an array and fix it
function sanitizeAndSaveClassName(className, saveToSession) {
    var sanitizedClassName = (className.constructor === Array && className.length == 1) ?
        className[0] :
        className;
    if (saveToSession) {
        sessionStorage.setItem("className", sanitizedClassName);
    }
    return sanitizedClassName;
}

function pushIfNotDup(element, array) {
    if (-1 === $.inArray(element, array)) {
        array.push(element);
    }
}
function isMissingFromArray(element, array) {
    return (-1 === $.inArray(element, array));
}
function isAlreadyInArray(element, array) {
    return ! isMissingFromArray(element, array);
}
function removeIfPresent(element, array) {
    var index = $.inArray(element, array);

    if (index !== -1) {
        array.splice(index, 1);
    }
}
