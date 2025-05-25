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
/**
 * Created by jill singer on 11/16/16.
 *
 * contains all the common code needed for date ranges; used in both the temporary adjustments
 * and when scheduling appts
 */

var selectedDaysOfWeek = [];

function appt_createSearchDateTimeWidgets(parentElement, display59, autoCorrectDate, preventTextInput) {
    var options =  {
        parentElement: parentElement,
        display59: display59,
        autoCorrectDate: autoCorrectDate,
        preventTextInput: preventTextInput,
        startDateElem: "#appt_startDate",
        endDateElem: "#appt_endDate",
        startTimeElem: "#appt_startTime",
        endTimeElem: "#appt_endTime",
        minDate: "+0M +0D"
    };

    WidgetUtil.DateTimeAndSpecificDaySection(options);
}

function adjustSpecificDayCheckboxes(startDateDatepicker, parentElement, endDateDatepicker) {
    if(MiscUtil.isUndefinedOrNull(endDateDatepicker)) {
        endDateDatepicker = $("#appt_endDate").datepicker('getDate');
    }

    if (parentElement !== undefined) {
        $(parentElement).find('.formElementRequired').css({visibility: "hidden"});
        $(parentElement).find('.formValidationRequired').css({visibility: "hidden"});
        $('#schedulingRestrictionWarning').removeClass("redBorder");
    }
    else {
        $('.formElementRequired').css({visibility: "hidden"});
        $('.formValidationRequired').css({visibility: "hidden"});
        $('#schedulingRestrictionWarning').removeClass("redBorder");
    }

    //don't worry....used for validation and in sending out rest call; harmless to leave this as a global (for now....)
    selectedDaysOfWeek = [];

    var dateArray = getDatesInterval(startDateDatepicker, endDateDatepicker);

    if ((dateArray[0].getDay() == 1 && dateArray.length >= 5) || dateArray.length >= 7) {
        $(".week_days").prop('disabled', false);
    } else {
        $(".week_days").prop('disabled', true);
    }

    for (var i = 0; i < dateArray.length; i++) {
        if (dateArray[i].getDay() != 0 && dateArray[i].getDay() != 6) {
            var selectDay = dateArray[i].getDay().toString();
            $(".weekday[value=" + selectDay + "]").prop("checked", true);
            $(".weekday[value=" + selectDay + "]").prop("disabled", false);
        }
        else if (dateArray[i].getDay() == 0 || dateArray[i].getDay() == 6) {
            var selectDay = dateArray[i].getDay().toString();
            $(".weekend[value=" + selectDay + "]").prop("disabled", false);
        }
    }
    toggleWeekDaysCheckBoxes();
}


/* resets the day of the week checkboxes*/
function resetCheckBoxes() {
    $(".weekday[value='']").prop("checked", false);
    $(".weekday[value='']").prop("disabled", true);
    for (var i = 0; i < 7; i++) {
        $(".weekday[value=" + i + "]").prop("disabled", true);
        $(".weekday[value=" + i + "]").prop("checked", false);
    }

    $(".weekend").prop("checked", false);
    $(".weekend").prop("disabled", true);
}

function getDatesInterval(startDate, endDate) {
    var dateArray = [];
    var currentDate = startDate;
    while (currentDate <= endDate) {
        dateArray.push(new Date(currentDate));
        currentDate = currentDate.addDays(1);
    }
    return dateArray;
}

function toggleWeekDaysCheckBoxes() {
    selectedDaysOfWeek = [];
    var weekday = $('.weekday').length;
    var allcheckboxes = $('.weekday:checked').length;
    if (weekday == allcheckboxes) {
        $(".week_days").prop('checked', true);
    }
    else {
        $(".week_days").prop('checked', false);
    }
    customCheckbox('allWeekDays');
    customCheckbox('weekend');
    customCheckbox('weekday');
}

/* makes sure the parents of the checkbox are set to selected/disabled/notselected correctly;
 * used for the three custom boxes; see above (all weekdays, weekend, weekday) */
function customCheckbox(checkboxName) {
    var checkBox = $('input[name="' + checkboxName + '"]');
    $(checkBox).each(function () {
        $(this).parent().removeClass("selected");
        $(this).parent().removeClass("notselected");
        $(this).parent().removeClass("disabled");

        if ($(this).is(':checked')) {
            $(this).parent().addClass("selected");
        }
        else if ($(this).is(':disabled') == false && $(this).is(':checked') == false) {
            $(this).parent().addClass("notselected");
        }
        else {
            $(this).parent().addClass("disabled");
        }
    });
}

function toggleAllWeekDays(source) {
    $('.formElementRequired').css({visibility: "hidden"});
    $('.formValidationRequired').css({visibility: "hidden"});
    $('#schedulingRestrictionWarning').removeClass("redBorder");
    selectedDaysOfWeek = [];
    var checkboxes = document.getElementsByName("weekday");
    for (var i = 0, n = checkboxes.length; i < n; i++) {
        checkboxes[i].checked = source.checked;
        checkboxes[i].disabled = false;
    }
    customCheckbox('allWeekDays');
    customCheckbox('weekend');
    customCheckbox('weekday');
}

function setSelectedDaysOfWeekFromCheckboxes() {
    var checkboxes = document.getElementById("weeklyCheckboxes").getElementsByTagName("input");
    var daysOfWeekStringList = "";
    for (var i = 0, l = checkboxes.length; i < l; ++i) {
        if (checkboxes[i].checked) {
            selectedDaysOfWeek.push(checkboxes[i].value);
            if (dayOfWeekString[checkboxes[i].value] != undefined) {
                daysOfWeekStringList += dayOfWeekString[checkboxes[i].value] + ", ";
            }
        }
    }

    return daysOfWeekStringList;
}

function getSelectedDaysOfWeekFromCheckboxes() {
    var checkboxes = document.getElementById("weeklyCheckboxes").getElementsByTagName("input");
    var selectedDaysOfWeek = [];
    for (var i = 0, l = checkboxes.length; i < l; ++i) {
        if (checkboxes[i].checked) {
            selectedDaysOfWeek.push(checkboxes[i].value);
        }
    }

    return selectedDaysOfWeek;
}
