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

var WidgetUtil = function () {
};

WidgetUtil.createComboBox = function (element, options) {
    if (options === undefined) {
        options = {};
    }

    if (options.value === undefined) {
        options.value = '';
    }

    if (options.width === undefined) {
        options.width = 205;
    }

    if (options.height === undefined) {
        options.height = 30;
    }

    if (options.prompt === undefined) {
        options.prompt = "Choose or Type Ahead";
    }

    if (options.limitToList === undefined) {
        options.limitToList = true;
    }

    $(element).combobox(options);

    $(".combobox-item:empty").addClass("firstComboItem");

    return $(element);
};

WidgetUtil.createSelectBox = function (element, options) {

    var dropDownContainer = $('<div>', {
        class: 'selectBox'
    });

    dropDownContainer.append(options.label);

    var select = $('<select>');
    select.attr('id', options.callbackElementId);
    select.attr('name', options.name);

    for (var key in options.values) {
        $('<option>')
            .attr('value', key)
            .text(options.values[key])
            .appendTo(select);
    }

    select.appendTo(dropDownContainer);
    dropDownContainer.appendTo(element);
};

WidgetUtil.createDatepicker = function (element, options, unmaskDate, preventTextInput) {

    if (options === undefined) {
        options = {};
    }

    if (options.buttonImage === undefined) {
        options.buttonImage = "css/images/calendar_icn_transparent.png";
    }

    options.changeMonth = true;
    options.changeYear = true;
    options.numberOfMonths = 3;
    options.buttonText = "Select a date";
    options.buttonImageOnly = true;

    if (options.showOn === undefined) {
        options.showOn = "button";
    }

    $(element).datepicker(options);

    if (!unmaskDate) {
        $(element).mask("99/99/9999");

        var placeholder = $(element).attr("placeholder");

        //add a placeholder only if there isn't already one
        if (placeholder === undefined) {
            $(element).attr("placeholder", "MM/DD/YYYY");
        }
    }

    if (preventTextInput) {
        $(element).on("keydown", function (event) {
            event.preventDefault();
        });
        $(element).on("keypress", function (event) {
            event.preventDefault();
        });
    }

    return $(element);
};

WidgetUtil.createDatetimepicker = function (element, options, preventTextInput) {

    if (options === undefined) {
        options = {};
    }

    if (options.buttonImage === undefined) {
        options.buttonImage = "css/images/calendar_icn_transparent.png";
    }

    if (options.timeFormat === undefined) {
        options.timeFormat = MILITARY_TIME_FORMAT;
    }

    if (options.buttonImageOnly === undefined) {
        options.buttonImageOnly = true;
    }

    if (options.showOn == undefined) {
        options.showOn = "button";
    }

    options.ampm = false;

    // The following options are enforced acroos the app
    options.numberOfMonths = 3;
    options.showButtonPanel = true;
    options.controlType = 'select';
    options.stepMinute = 15;
    options.minuteMax = 59;

    $(element).datetimepicker(options);

    if (preventTextInput) {
        $(element).on("keydown", function (event) {
            event.preventDefault();
        });
        $(element).on("focus", function () {
            $(element).datetimepicker("show");
        });
    }

};

WidgetUtil.limitAndReturnTextInputSize = function (elem, maxCharacters) {

    if (elem.length > 0) {
        var val = elem.val();
        var currentCharacters = val.length;
        var remainingCharacters;
        // limit the number of characters if needed, and count the number of remaining characters
        if (currentCharacters > maxCharacters) {
            var newString = val.substring(0, maxCharacters);
            remainingCharacters = 0;
            elem.val(newString);
        }
        else {
            remainingCharacters = maxCharacters - currentCharacters;
        }
        return remainingCharacters;
    }
    else {
        // element for which we are counting characters is not yet in the DOM
        return 0;
    }

};

WidgetUtil.commentBox = function (element, options) {

    var elementId = $(element).prop("id");

    if (elementId === undefined) {
        return;
    }

    $(element).empty();

    $(element).addClass("comments");


    var commentId = elementId + "_txtArea";
    var elementTxtArea = $('<textarea id="' + commentId + '"></textarea>');

    $(element).append(elementTxtArea);

    if (options == undefined) {
        options = {};
    }

    if (options.value !== undefined) {
        $(elementTxtArea).val(options.value);
    }

    if (options.placeholder !== undefined) {
        $(elementTxtArea).prop("placeholder", options.placeholder);
    }

    if (options.placeholder !== undefined) {
        $(elementTxtArea).prop("placeholder", options.placeholder);
    }

    if (options.rows !== undefined) {
        $(elementTxtArea).prop("rows", options.rows);
    }

    if (options.cols !== undefined) {
        $(elementTxtArea).prop("cols", options.cols);
    }

    var width = "100%";
    if (options.width !== undefined) {
        width = options.width;
    }
    $(elementTxtArea).css({width: width});

    var height = "50px";
    if (options.height !== undefined) {
        height = options.height;
    }
    $(elementTxtArea).css({height: height});

    $(elementTxtArea).addClass("commentTextArea");
    if (options.textClass !== undefined) {
        $(elementTxtArea).addClass(options.textClass);
    }

    var maxChars = 255;
    if (options.maxChars !== undefined) {
        maxChars = options.maxChars;
    }

    $(elementTxtArea).prop("maxlength", maxChars);

    if (!options.hideMaxCharCount) {
        var charCountElementId = elementId + "_txtAreaCount";
        $(element).append("<div id=" + charCountElementId + "></div>");
        var charCountElement = $("#" + charCountElementId);

        charCountElement.addClass("commentsCount");
        charCountElement.text(maxChars + " characters remaining");

        var updateRemainingChars = function () {

            var remain = WidgetUtil.limitAndReturnTextInputSize($(this), maxChars);
            charCountElement.text(remain + " characters remaining.");
        };

        $(elementTxtArea).on("keyup", updateRemainingChars);

        $(elementTxtArea).on("change", updateRemainingChars);
    }
};

WidgetUtil.commentBox.clearValue = function (element) {
    WidgetUtil.commentBox.setValue(element, '');
};

WidgetUtil.commentBox.setValue = function (element, value) {
    var textArea = $(element).find("textarea");
    textArea.val(value);
    textArea.trigger("change");
};

WidgetUtil.commentBox.getValue = function (element) {
    var textArea = $(element).find("textarea");
    return textArea.val();
};

WidgetUtil.counterDisplay = function () {
};

WidgetUtil.counterDisplay.create = function (element, count, options) {
    var elementId = $(element).prop("id");
    var wrapperElementId = elementId + "_wrapper";

    //cleanup any previous counter widgets
    if ($("#" + wrapperElementId).length > 0) {
        $("#" + wrapperElementId).before($(element));
        $("#" + wrapperElementId).remove();
    }

    var wrapperDiv = $("<div></div>");
    wrapperDiv.prop("id", wrapperElementId);

    var backgroundColor = $(element).css("background-color");

    var fontColor = "#FFFFFF";
    var fontSize = "14px";

    if (options == undefined) {
        options = {};
    }

    wrapperDiv.addClass("counterDisplayWrapper");
    if (options.wrapperClass !== undefined) {
        $(wrapperDiv).addClass(options.wrapperClass);
    }

    if (options.fontColor !== undefined) {
        fontColor = options.fontColor;
    }

    if (options.fontSize !== undefined) {
        fontSize = options.fontSize;
    }

    if (options.backgroundColor !== undefined) {
        backgroundColor = options.backgroundColor;
    }

    var elementTopMargin = $.trim($(element).css("margin-top"));

    $(element).css({"margin-top": "-1px"});
    wrapperDiv.css({"margin-top": elementTopMargin});

    var counterDisplay = $("<div></div>");
    var counterDisplayId = elementId + "_counter";
    counterDisplay.prop("id", counterDisplayId);
    var counterDisplayInner = $("<div>" + count + "</div>");
    counterDisplayInner.css({"margin-top": "4px"});
    counterDisplay.append(counterDisplayInner);

    counterDisplay.addClass("counterDisplay");
    counterDisplay.css({
        color: fontColor,
        "background-color": backgroundColor,
        "font-size": fontSize
    });

    wrapperDiv.append(counterDisplay);

    $(element).before(wrapperDiv);
    wrapperDiv.append(element);
};

WidgetUtil.counterDisplay.getValue = function (element, value) {
    var elementId = $(element).prop("id");
    var counterDisplayId = elementId + "_counter";
    var counterDisplay = $("#" + counterDisplayId);

    return counterDisplay.find("div").text();
};

WidgetUtil.counterDisplay.setValue = function (element, value) {
    var elementId = $(element).prop("id");
    var counterDisplayId = "#" + elementId + "_counter";
    var counterDisplay = $(counterDisplayId);

    counterDisplay.find("div").text(value);
};

WidgetUtil.createSliderTimepicker = function (element, options, unmaskDate) {

    // we're setting these options so they will never be
    // modified by the underlying call to WidgetUtil.createTimepicker
    // even if that function changes its own defaults
    options.controlType = 'slider';
    if (options.stepMinute === undefined) {
        options.stepMinute = 1;
    }
    return WidgetUtil.createTimepicker(element, options, unmaskDate);
};

WidgetUtil.createTimepicker = function (element, options, unmaskTime, preventTextInput) {
    if (options === undefined) {
        options = {};
    }

    if (options.controlType === undefined) {
        options.controlType = 'select';
    }

    if (options.timeFormat === undefined) {
        options.timeFormat = MILITARY_TIME_FORMAT;
    }

    if (options.minuteMax === undefined) {
        // this is the default value for timepicker() anyway
        options.minuteMax = 59;
    }

    if (options.stepMinute === undefined) {
        options.stepMinute = 15;
    }

    $(element).timepicker(options);

    if (!unmaskTime) {
        var format = "99:99";
        $(element).mask(format);

        var placeholder = $(element).attr("placeholder");

        //add a placeholder only if there isn't already one
        if (placeholder === undefined) {
            $(element).attr("placeholder", "HH:MM");
        }
    }

    if (preventTextInput) {
        $(element).on("keydown", function (event) {
            event.preventDefault();
        });
        $(element).on("keypress", function (event) {
            event.preventDefault();
        });

    }
    return $(element);
};

WidgetUtil.timepickerOnCloseFactory = function (elementSelector) {

    return function () {
        try {
            var timeStr = $(elementSelector).val();
            if (timeStr != '') {
                var time = $.datepicker.parseTime(MILITARY_TIME_FORMAT, timeStr);
                if (time == false) {
                    alert("Incorrect Time format. It should be " + MILITARY_TIME_FORMAT + ".");
                    $(elementSelector).val('');
                    return;
                }
            }
        } catch (e) {
            alert("Incorrect Time format. It should be " + MILITARY_TIME_FORMAT + ".");
            $(elementSelector).val('');
            return;
        }
    }
};

WidgetUtil.createSearchTextBox = function (element, options) {
    if (options === undefined) {
        options = {};
    }

    var width = "100%";
    if (options.width != undefined) {
        width = options.width;
    }

    var height = 27;
    if (options.height != undefined) {
        height = options.height;
    }

    var prompt = "";
    if (options.prompt != undefined) {
        prompt = options.prompt;
    }

    var textboxOptions = {
        height: height,
        width: width,
        prompt: prompt,
        value: options.defaultValue,
        icons: [{
            iconCls: 'icon-clear',
            handler: function (e) {
                $(e.data.target).textbox('textbox').val("");
                $(e.data.target).textbox('options').value = "";
                onChange(e.data.target, "");
            }
        }],
        onChange: function (newValue, oldValue) {
            onChange(element, newValue);
        }
    };

    if (options.err != undefined) {
        textboxOptions.err = options.err;
    }

    $(element).textbox(textboxOptions);

    $(element).each(function () {
        $(this).textbox('getIcon', 0).css('visibility', 'hidden');
    });

    WidgetUtil._addClearIconToEasyUITextbox();

    var onChange = function (element, newValue) {
        if (newValue.length === 0) {
            $(element).textbox('getIcon', 0).css('visibility', 'hidden');
        }
        else {
            $(element).textbox('getIcon', 0).css('visibility', 'visible');
        }

        options.onSearch.call(element, newValue);
    };

    var isBirthdatepicker = options.type && options.type.name == InputField.Type.Birthdate;
    var isDatepicker = options.type && (options.type.name == InputField.Type.Date || options.type.name == InputField.Type.Birthdate);

    if (isDatepicker) {
        var inputElement = $(element).textbox('textbox');

        if (options.type.options === undefined) {
            options.type.options = {};
        }

        if (isBirthdatepicker) {
            options.type.options.maxDate = "-1D";
        }

        options.type.options.showOn = "focus";
        options.type.options.buttonImage = "";
        options.type.options.yearRange = "-120:+20";
        options.type.options.constrainInput = true;
        options.type.options.beforeShow = function (datepickerElement, inst) {
            $(element).textbox('getIcon', 0).css('visibility', 'hidden');
        };
        options.type.options.onClose = function (dateText, inst) {
            if (dateText != "") {
                $(element).textbox('getIcon', 0).css('visibility', 'visible');
            }
            else {
                $(element).textbox('getIcon', 0).css('visibility', 'hidden');
            }
        };
        options.type.options.onSelect = function (dateText, inst) {
            if (dateText.length === 0) {
                $(element).textbox('getIcon', 0).css('visibility', 'hidden');
            }
            else {
                $(element).textbox('getIcon', 0).css('visibility', 'visible');
            }

            onChange(element, dateText);

            if(dateText.length > 0) {
                //this is needed so that color of the text will change to gray
                $(element).textbox('textbox').removeClass("textbox-prompt");
            }
        };

        var unmaskDate = true;
        var preventInput = true;
        WidgetUtil.createDatepicker(inputElement, options.type.options, unmaskDate, preventInput);
    }
    else {

        if (options.maskFormat) {
            $(element).textbox("textbox").keyup(function(){
                var newValue = $(this).val();
                var placeHolderText = options.maskFormat.replace(new RegExp("\\9", "g"), "_");
                if(newValue === placeHolderText) {
                    $(element).textbox('getIcon', 0).css('visibility', 'hidden');
                    options.onSearch.call($(element), newValue);
                }
                else {
                    $(element).textbox('getIcon', 0).css('visibility', 'visible');
                }
            });

            $(element).textbox("textbox").on("blur", function() {
                var newValue = $(this).val();

                if(newValue == "" || newValue.indexOf("_") != -1) {
                    $(element).textbox('getIcon', 0).css('visibility', 'hidden');
                }
            });

            $(element).textbox("textbox").mask(options.maskFormat, {
                completed: function () {
                    var newValue = $(this).val();
                    options.onSearch.call($(element), newValue);
                    element.textbox('getIcon', 0).css('visibility', 'visible');
                }
            });

            $(element).textbox("textbox").css({"letter-spacing": "1px"});
        }
        $(element).textbox('onInput', function (element) {
            var value = $(element).textbox("textbox").val();
            var previousValue = $(element).textbox("textbox").data("previousValue");

            if(!((previousValue == undefined || previousValue == "") && value == ""))
            {
                onChange(element, value);
            }
            $(element).textbox("textbox").data("previousValue", value);
        });
    }
};

WidgetUtil.createNumberSpinner = function (element, options) {
    if (options === undefined) {
        options = {};
    }

    var defaultValue = null;
    if (options.defaultValue) {
        defaultValue = options.defaultValue;
        delete options.defaultValue;
    }

    $(element).spinner(options);

    if (defaultValue) {
        $(element).val(defaultValue);
    }

    if (options.min || options.max) {
        $(element).on("input", function () {
            var strValue = $(this).val();
            var numbersOnly = containsDigitsOnly(strValue);
            var value = parseInt(strValue);

            if (!numbersOnly || (options.min && value < options.min)
                || (options.max && value > options.max)) {
                var newVal = strValue.substring(0, strValue.length - 1);
                $(element).val(newVal);
            }
        });
    }
};

WidgetUtil._addClearIconToEasyUITextbox = function () {
    if ($.fn.textbox) {
        $.extend($.fn.textbox.methods, {
            onInput: function (jq, callbackFunction) {
                return jq.each(function () {
                    var t = $(this);
                    t.textbox('textbox').on('input', function () {
                        //show and hide and clear text icon
                        var icon = t.textbox('getIcon', 0);
                        if ($(this).val()) {
                            icon.css('visibility', 'visible');
                        } else {
                            icon.css('visibility', 'hidden');
                        }
                        callbackFunction(t);
                    });
                });
            }
        });
    }
};

WidgetUtil.resizeInput = function (inputElement, value) {
    var sizerSpan = $("<span></span>").hide();
    $(inputElement).after(sizerSpan);
    sizerSpan.text(value);

    var valueInitW = sizerSpan.width();
    var padding = 4;
    valueInitW = valueInitW < 25 ? 25 : valueInitW + padding;

    sizerSpan.remove();
    $(inputElement).css('width', valueInitW);
};

WidgetUtil.addDynamicResizingToInput = function (inputElement) {
    $(inputElement).on("keypress", function (event) {
        if (event.which && event.charCode) {
            var c = String.fromCharCode(event.keyCode | event.charCode);
            var value = $(this).val() + c;
            WidgetUtil.resizeInput($(this), value);
        }
    });

    // Backspace event only fires for keyup
    $(inputElement).on("keyup", function (event) {
        if (event.keyCode === keyCodes.BACKSPACE || event.keyCode === keyCodes.DELETE) {
            WidgetUtil.resizeInput($(this), $(this).val());
        }
    });

    WidgetUtil.resizeInput(inputElement, $(inputElement).val());
};

WidgetUtil.addSelectFunctionToDatePicker = function (datePicker, funHandler, prepend) {
    var prevhandler = datePicker.datepicker('option', 'onSelect');
    datePicker.datepicker('option', {
        onSelect: function (dateText, inst) {

            if(prepend) {
                funHandler(dateText, inst);

                if (prevhandler) {
                    prevhandler(dateText, inst);
                }
            }
            else {
                if (prevhandler) {
                    prevhandler(dateText, inst);
                }
                funHandler(dateText, inst);
            }
        }
    });
};

InputField = {};
InputField.Type = {
    Birthdate: "birthdate",
    Date: "date"
};

WidgetUtil.createMultiSelectComboBox = function(selectElement, options){
    $(selectElement).multipleSelect({
        placeholder: "choose",
        width: options.width,
        allSelected: "All Selected",
        maxHeight: 200,
        minimumCountSelected: options.minimumCount
    });
};

WidgetUtil.ExpandableSection = function(options) {
    if ($('#' + options.headingId).attr('title') == 'Expand') {
        WidgetUtil.ExpandableSection.displaySection(options.headingId, options.imageId, options.sectionId, options.onExpand);

    } else {
        WidgetUtil.ExpandableSection.hideSection(options.headingId, options.imageId, options.sectionId);
    }
};

WidgetUtil.ExpandableSection.displaySection = function(headingId, imageId, sectionId, expandCallback) {
    $('#' + headingId).attr('title', 'Collapse').css({cursor: 'pointer'});
    $('#' + imageId).attr('src', 'css/images/double_up_arrow.png');
    $('#' + sectionId).css({display: 'block', cursor: 'pointer'});

    if(expandCallback){
        $('#' + sectionId).show(expandCallback);
    }
};

WidgetUtil.ExpandableSection.hideSection = function(headingId, imageId, sectionId) {
    $('#' + headingId).attr('title', 'Expand').css({cursor: 'pointer'});
    $('#' + imageId).attr('src', 'css/images/double_down_arrow.png');
    $('#' + sectionId).css({display: 'none', cursor: 'default'});
};

WidgetUtil.DateTimeAndSpecificDaySection = function (options) {
    if (options.display59 === undefined) {
        options.display59 = false;
    }

    if (options.autoCorrectDate === undefined) {
        options.autoCorrectDate = true;
    }

    WidgetUtil.createDatepicker(
        $(options.startDateElem), {
            showOn: "both",
            minDate: options.minDate,
            onSelect: function (selectedDate) {
                resetCheckBoxes();
                var startDateDatepicker = $(options.startDateElem).datepicker('getDate');
                $(options.endDateElem).datepicker("option", "minDate", selectedDate);

                if (options.autoCorrectDate) {
                    $(options.endDateElem).datepicker("setDate", selectedDate);
                }
                var endDateDatepicker = $(options.endDateElem).datepicker('getDate');

                adjustSpecificDayCheckboxes(startDateDatepicker, options.parentElement, endDateDatepicker);

                $(".ui-dialog").focus();
            },
            onClose: function (dateText, inst) {
                WidgetUtil.DateTimeAndSpecificDaySection._onClosing(dateText, $(options.startDateElem));
            }
        },
        true,
        options.preventTextInput
    );

    WidgetUtil.createTimepicker($(options.startTimeElem), {
        display59: options.display59,
        timeFormat: MILITARY_TIME_FORMAT,
        value: '07:00',
        onSelect: function () {
            var timeObject = $(options.startTimeElem).timepicker().val();
            $(options.endTimeElem).val(timeObject);
        }
    }, true, options.preventTextInput);

    WidgetUtil.createDatepicker(
        $(options.endDateElem), {
            showOn: "both",
            minDate: options.minDate,
            onSelect: function () {
                resetCheckBoxes();
                var startDateDatepicker = $(options.startDateElem).datepicker('getDate');
                var endDateDatepicker = $(options.endDateElem).datepicker('getDate');

                adjustSpecificDayCheckboxes(startDateDatepicker, options.parentElement, endDateDatepicker);
                $(".ui-dialog").focus();
            },
            onClose: function (dateText, inst) {
                WidgetUtil.DateTimeAndSpecificDaySection._onClosing(dateText, $(options.endDateElem));
            }
        },
        true,
        options.preventTextInput);

    WidgetUtil.createTimepicker($(options.endTimeElem), {
        display59: options.display59,
        timeFormat: MILITARY_TIME_FORMAT,
        value: '15:00'
    }, true, options.preventTextInput);
};

WidgetUtil.DateTimeAndSpecificDaySection._onClosing = function(dateText, datePickerElement) {
    try {
        var selectedDate = $.datepicker.parseDate('mm/dd/yy', dateText);
        var today = new Date();
        today.setHours(0, 0, 0, 0);
        if (selectedDate < today) {
            alert("Incorrect Date. Entered date is outside the range.");
            datePickerElement.val('');
        }
    } catch (e) {
        alert("Incorrect Date format. It should be MM/DD/YYYY.");
        datePickerElement.val('');
    }
};

WidgetUtil.DateTimeAndSpecificDaySection.getSelectedDaysOfWeek = function(element){
    var checkboxes = $(element).find("input");
    var selectedDaysOfWeek = [];
    for (var i = 0, l = checkboxes.length; i < l; ++i) {
        if (checkboxes[i].checked) {
            selectedDaysOfWeek.push(checkboxes[i].value);
        }
    }

    return selectedDaysOfWeek;
};
