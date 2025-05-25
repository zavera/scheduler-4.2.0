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
describe('createComboBox', function () {
    beforeEach(function () {
        this.spy_dollar = spyOn(window, "$").and.callThrough();
        this.spy_combobox = spyOn($.fn, "combobox");
        this.spy_addClass = spyOn($.fn, "addClass");

        this.element = {};
        this.options = {};
    });

    it('processes options and calls ctor, with empty options', function () {

        var result = WidgetUtil.createComboBox(this.element, this.options);

        expect(this.spy_dollar).toHaveBeenCalledTimes(3);
        expect(this.spy_dollar.calls.argsFor(0)).toEqual([this.element]);
        expect(this.spy_dollar.calls.argsFor(1)).toEqual([".combobox-item:empty"]);
        expect(this.spy_dollar.calls.argsFor(2)).toEqual([this.element]);

        expect(this.spy_combobox).toHaveBeenCalledTimes(1);
        expect(this.spy_combobox.calls.argsFor(0)).toEqual([this.options]);

        expect(this.spy_addClass).toHaveBeenCalledTimes(1);
        expect(this.spy_addClass.calls.argsFor(0)).toEqual(["firstComboItem"]);

        expect(this.options.value).toEqual('');
        expect(this.options.width).toEqual(205);
        expect(this.options.height).toEqual(30);
        expect(this.options.prompt).toEqual("Choose or Type Ahead");
        expect(this.options.limitToList).toEqual(true);

        expect(result.length).toEqual(1);
        expect(result[0]).toEqual(this.element);
    });
    it('also processes options and calls ctor, with non-empty options', function () {

        this.options.value = 'foo';
        this.options.width = 42;
        this.options.height = 43;
        this.options.prompt = "bar";
        this.options.limitToList = false;

        var result = WidgetUtil.createComboBox(this.element, this.options);

        expect(this.spy_combobox).toHaveBeenCalledTimes(1);
        var calledOptions = this.spy_combobox.calls.argsFor(0)[0];

        expect(calledOptions.value).toEqual('foo');
        expect(calledOptions.width).toEqual(42);
        expect(calledOptions.height).toEqual(43);
        expect(calledOptions.prompt).toEqual("bar");
        expect(calledOptions.limitToList).toEqual(false);

        expect(result.length).toEqual(1);
        expect(result[0]).toEqual(this.element);
    });

    it('also processes options and calls ctor, with no options', function () {

        var result = WidgetUtil.createComboBox(this.element);

        expect(this.spy_combobox).toHaveBeenCalledTimes(1);
        var calledOptions = this.spy_combobox.calls.argsFor(0)[0];

        expect(calledOptions.value).toEqual('');
        expect(calledOptions.width).toEqual(205);
        expect(calledOptions.height).toEqual(30);
        expect(calledOptions.prompt).toEqual("Choose or Type Ahead");
        expect(calledOptions.limitToList).toEqual(true);
    });
});

describe('createSelectBox', function () {

    it('creates a selectBox and adds options', function () {

        var that = this;

        that.spy_appendContainer = jasmine.createSpy();
        that.spy_appendToContainer = jasmine.createSpy();

        that.spy_attrSelect = jasmine.createSpy();
        that.spy_appendToSelect = jasmine.createSpy();

        that.spy_appendToText = jasmine.createSpy();
        that.spy_text = jasmine.createSpy().and.returnValue({appendTo: that.spy_appendToText});
        that.spy_attrOption = jasmine.createSpy().and.returnValue({text: that.spy_text});

        that.dollarDiv = {append: that.spy_appendContainer, appendTo: that.spy_appendToContainer};
        that.dollarSelect = {appendTo: that.spy_appendToSelect, attr: that.spy_attrSelect};
        that.dollarOption = {attr: that.spy_attrOption};

        that.spy_dollar = spyOn(window, "$").and.callFake(function (arg) {
            if (arg === '<div>') {
                return that.dollarDiv;
            }
            else if (arg === '<select>') {
                return that.dollarSelect;
            }
            else if (arg === '<option>') {
                return that.dollarOption;
            }
        });

        that.options = {
            label: 'fooLabel',
            callbackElementId: '42',
            values: ['oneValue', 'twoValue'],
            name: 'nameValue'
        };
        that.element = {};

        WidgetUtil.createSelectBox(that.element, that.options);

        expect(that.spy_dollar).toHaveBeenCalledTimes(4);
        expect(that.spy_dollar.calls.argsFor(0)).toEqual(['<div>', {
            class: 'selectBox'
        }]);
        expect(that.spy_dollar.calls.argsFor(1)).toEqual(['<select>']);
        expect(that.spy_dollar.calls.argsFor(2)).toEqual(['<option>']);
        expect(that.spy_dollar.calls.argsFor(3)).toEqual(['<option>']);

        expect(that.spy_appendContainer).toHaveBeenCalledTimes(1);
        expect(that.spy_appendContainer.calls.argsFor(0)).toEqual(['fooLabel']);

        expect(that.spy_attrSelect).toHaveBeenCalledTimes(2);
        expect(that.spy_attrSelect.calls.argsFor(0)).toEqual(['id', '42']);
        expect(that.spy_attrSelect.calls.argsFor(1)).toEqual(['name', 'nameValue']);

        expect(that.spy_attrOption).toHaveBeenCalledTimes(2);
        expect(that.spy_attrOption.calls.argsFor(0)).toEqual(['value', '0']);
        expect(that.spy_attrOption.calls.argsFor(1)).toEqual(['value', '1']);

        expect(that.spy_text).toHaveBeenCalledTimes(2);
        expect(that.spy_text.calls.argsFor(0)).toEqual(['oneValue']);
        expect(that.spy_text.calls.argsFor(1)).toEqual(['twoValue']);

        expect(that.spy_appendToText).toHaveBeenCalledTimes(2);
        expect(that.spy_appendToText.calls.argsFor(0)).toEqual([that.dollarSelect]);
        expect(that.spy_appendToText.calls.argsFor(1)).toEqual([that.dollarSelect]);

        expect(that.spy_appendToSelect).toHaveBeenCalledTimes(1);
        expect(that.spy_appendToSelect.calls.argsFor(0)).toEqual([that.dollarDiv]);

        expect(that.spy_appendToContainer).toHaveBeenCalledTimes(1);
        expect(that.spy_appendToContainer.calls.argsFor(0)).toEqual([that.element]);
    });
});

describe('createDatepicker', function () {
    beforeEach(function () {
        this.spy_dollar = spyOn(window, "$").and.callThrough();
        this.spy_datepicker = spyOn($.fn, "datepicker");
        this.spy_mask = spyOn($.fn, "mask");
        this.spy_attr = spyOn($.fn, "attr");
        this.spy_keydown = spyOn($.fn, "keydown");
        this.spy_keypress = spyOn($.fn, "keypress");

        this.element = {};
        this.options = {extra: 'extra'};
    });

    it('adjusts options and calls datepicker, with mask and preventText', function () {

        var expectedResult = $(this.element);

        var result = WidgetUtil.createDatepicker(this.element, this.options, false, true);

        expect(this.spy_dollar).toHaveBeenCalledTimes(7 + 1);
        for (var i = 0; i < 7 + 1; i++) {
            expect(this.spy_dollar.calls.argsFor(i)).toEqual([this.element]);
        }

        expect(this.spy_datepicker).toHaveBeenCalledTimes(1);
        expect(this.spy_datepicker.calls.argsFor(0)).toEqual([this.options]);

        expect(this.spy_mask).toHaveBeenCalledTimes(1);
        expect(this.spy_mask.calls.argsFor(0)).toEqual(["99/99/9999"]);

        expect(this.spy_attr).toHaveBeenCalledTimes(2);
        expect(this.spy_attr.calls.argsFor(0)).toEqual(["placeholder"]);
        expect(this.spy_attr.calls.argsFor(1)).toEqual(["placeholder", "MM/DD/YYYY"]);

        expect(this.spy_keydown).toHaveBeenCalledTimes(1);
        expect(this.spy_keydown.calls.argsFor(0)).toEqual([jasmine.any(Function)]);

        expect(this.spy_keypress).toHaveBeenCalledTimes(1);
        expect(this.spy_keypress.calls.argsFor(0)).toEqual([jasmine.any(Function)]);

        expect(result).toEqual(expectedResult);
    });

    it('adjusts options and calls datepicker, with no mask or preventText', function () {

        var expectedResult = $(this.element);

        var result = WidgetUtil.createDatepicker(this.element, this.options, true, false);

        expect(this.spy_dollar).toHaveBeenCalledTimes(2 + 1);
        for (var i = 0; i < 2 + 1; i++) {
            expect(this.spy_dollar.calls.argsFor(i)).toEqual([this.element]);
        }

        expect(this.spy_datepicker).toHaveBeenCalledTimes(1);
        expect(this.spy_datepicker.calls.argsFor(0)).toEqual([this.options]);
        // preserved the option we provided
        expect(this.options.extra).toBe('extra');

        expect(this.spy_mask).toHaveBeenCalledTimes(0);
        expect(this.spy_attr).toHaveBeenCalledTimes(0);
        expect(this.spy_keydown).toHaveBeenCalledTimes(0);
        expect(this.spy_keypress).toHaveBeenCalledTimes(0);

        expect(result).toEqual(expectedResult);
    });
    it('adjusts options when they are undefined', function () {

        var expectedResult = $(this.element);

        var result = WidgetUtil.createDatepicker(this.element, undefined, true, false);

        expect(this.spy_dollar).toHaveBeenCalledTimes(2 + 1);
        for (var i = 0; i < 2 + 1; i++) {
            expect(this.spy_dollar.calls.argsFor(i)).toEqual([this.element]);
        }

        expect(this.spy_datepicker).toHaveBeenCalledTimes(1);
        var adjustedOptions = this.spy_datepicker.calls.argsFor(0)[0];
        expect(adjustedOptions.buttonImage).toEqual("css/images/calendar_icn_transparent.png");
        expect(adjustedOptions.showOn).toEqual("button");
        expect(adjustedOptions.changeMonth).toEqual(true);
        expect(adjustedOptions.changeYear).toEqual(true);
        expect(adjustedOptions.numberOfMonths).toEqual(3);
        expect(adjustedOptions.buttonText).toEqual("Select a date");
        expect(adjustedOptions.buttonImageOnly).toEqual(true);

        expect(this.spy_mask).toHaveBeenCalledTimes(0);
        expect(this.spy_attr).toHaveBeenCalledTimes(0);
        expect(this.spy_keydown).toHaveBeenCalledTimes(0);
        expect(this.spy_keypress).toHaveBeenCalledTimes(0);

        expect(result).toEqual(expectedResult);
    });
});
describe('createDatetimepicker', function () {
    beforeEach(function () {
        this.spy_dollar = spyOn(window, "$").and.callThrough();
        this.spy_datetimepicker = spyOn($.fn, "datetimepicker");
        this.spy_keydown = spyOn($.fn, "keydown");
        this.spy_focus = spyOn($.fn, "focus");

        this.element = {};
        this.options = {extra: 'extra'};
    });

    it('adjusts options and calls spy_datetimepicker, with preventText', function () {

        WidgetUtil.createDatetimepicker(this.element, this.options, true);

        expect(this.spy_dollar).toHaveBeenCalledTimes(3);
        for (var i = 0; i < 3; i++) {
            expect(this.spy_dollar.calls.argsFor(i)).toEqual([this.element]);
        }

        expect(this.spy_datetimepicker).toHaveBeenCalledTimes(1);
        expect(this.spy_datetimepicker.calls.argsFor(0)).toEqual([this.options]);

        expect(this.spy_keydown).toHaveBeenCalledTimes(1);
        expect(this.spy_keydown.calls.argsFor(0)).toEqual([jasmine.any(Function)]);

        expect(this.spy_focus).toHaveBeenCalledTimes(1);
        expect(this.spy_focus.calls.argsFor(0)).toEqual([jasmine.any(Function)]);
    });
    it('adjusts options and calls spy_datetimepicker, without preventText', function () {

        WidgetUtil.createDatetimepicker(this.element, this.options, false);

        expect(this.spy_dollar).toHaveBeenCalledTimes(1);
        expect(this.spy_dollar.calls.argsFor(0)).toEqual([this.element]);

        expect(this.spy_datetimepicker).toHaveBeenCalledTimes(1);
        expect(this.spy_datetimepicker.calls.argsFor(0)).toEqual([this.options]);

        expect(this.spy_keydown).toHaveBeenCalledTimes(0);
        expect(this.spy_focus).toHaveBeenCalledTimes(0);

        // preserved the option we provided
        expect(this.options.extra).toBe('extra');
    });
    it('adjusts options when they are undefined', function () {

        WidgetUtil.createDatetimepicker(this.element, undefined, false);

        expect(this.spy_dollar).toHaveBeenCalledTimes(1);
        expect(this.spy_dollar.calls.argsFor(0)).toEqual([this.element]);

        expect(this.spy_datetimepicker).toHaveBeenCalledTimes(1);

        var adjustedOptions = this.spy_datetimepicker.calls.argsFor(0)[0];
        expect(adjustedOptions.buttonImage).toEqual("css/images/calendar_icn_transparent.png");
        expect(adjustedOptions.timeFormat).toEqual(MILITARY_TIME_FORMAT);
        expect(adjustedOptions.buttonImageOnly).toEqual(true);
        expect(adjustedOptions.showOn).toEqual("button");
        expect(adjustedOptions.numberOfMonths).toEqual(3);
        expect(adjustedOptions.showButtonPanel).toEqual(true);
        expect(adjustedOptions.controlType).toEqual('select');
        expect(adjustedOptions.stepMinute).toEqual(15);
        expect(adjustedOptions.minuteMax).toEqual(59);


        expect(this.spy_keydown).toHaveBeenCalledTimes(0);
        expect(this.spy_focus).toHaveBeenCalledTimes(0);
    });
});

describe('limitAndReturnTextInputSize', function () {
    beforeEach(function () {
        this.element = {
            size: function () {
                return this.innerVal.length;
            },
            val: function (newVal) {
                if (newVal !== undefined) {
                    this.innerVal = newVal;
                }
                return this.innerVal;
            },
            tenVal: "123456789A",
            twoVal: "12",
            fiveVal: "12345"
        };
    });

    it('return 0 unless elem-size > 0', function () {

        var element = this.element;

        element.val("");

        var result = WidgetUtil.limitAndReturnTextInputSize(element, 5);
        expect(element.val()).toBe('');
        expect(result).toBe(0);

    });
    it('return 0 and truncates value is elem-size >= max', function () {

        var element = this.element;

        element.val(element.tenVal);
        result = WidgetUtil.limitAndReturnTextInputSize(element, 5);
        expect(result).toBe(0);
        expect(element.val()).toBe(element.fiveVal);

        element.val(element.fiveVal);
        result = WidgetUtil.limitAndReturnTextInputSize(element, 5);
        expect(result).toBe(0);
        expect(element.val()).toBe(element.fiveVal);
    });
    it('return remainder size and retains submitted value if elem-size < max', function () {

        var element = this.element;

        element.val(element.twoVal);
        result = WidgetUtil.limitAndReturnTextInputSize(element, 5);
        expect(result).toBe(3);
        expect(element.val()).toBe(element.twoVal);
    });
});

describe('commentBox', function () {
    beforeEach(function () {
        this.spy_dollar = spyOn(window, "$").and.callThrough();
        this.spy_prop = spyOn($.fn, "prop");
        this.spy_empty = spyOn($.fn, "empty");
        this.spy_addClass = spyOn($.fn, "addClass");
        this.spy_append = spyOn($.fn, "append");
        this.spy_val = spyOn($.fn, "val");
        this.spy_css = spyOn($.fn, "css");
        this.spy_keyup = spyOn($.fn, "keyup");
        this.spy_change = spyOn($.fn, "change");
        this.spy_text = spyOn($.fn, "text");

        this.element = {};
        this.options = {extra: 'extra'};
    });

    it('bails if element has no id', function () {

        WidgetUtil.commentBox(this.element, this.options);

        expect(this.spy_dollar).toHaveBeenCalledTimes(1);
        expect(this.spy_dollar.calls.argsFor(0)).toEqual([this.element]);

        expect(this.spy_prop).toHaveBeenCalledTimes(1);
        expect(this.spy_prop.calls.argsFor(0)).toEqual(["id"]);

        expect(this.spy_empty).toHaveBeenCalledTimes(0);
    });
});

describe("dynamic input field", function () {
    it("makes input field resize dynamically", function () {

        FixtureHelper.loadTestHtmlFixtureIntoDom("util/html/input-for-resizing.html");

        var inputElement = $("#resizableElement");
        inputElement.val("");
        WidgetUtil.addDynamicResizingToInput(inputElement);

        var minWidth = 25;
        var initialWidth = inputElement.width();
        expect(initialWidth).toBe(minWidth);

        inputElement.val("This is test input");

        var inputChar = "2";
        var inputCharCode = inputChar.charCodeAt(0);
        var keypressEvent = jQuery.Event("keypress");
        keypressEvent.which = inputCharCode;
        keypressEvent.charCode = inputCharCode;

        inputElement.trigger(keypressEvent);

        var newWidth = inputElement.width();
        expect(newWidth).not.toBeLessThan(118);
    });
});

describe('createSearchTextBox', function () {
    beforeEach(function () {

        this.spy_mask = jasmine.createSpy();
        this.spy_css = jasmine.createSpy();
        this.spy_keyup = jasmine.createSpy();
        this.spy_focus = jasmine.createSpy();
        this.spy_blur = jasmine.createSpy();

        this.textbox = {mask: this.spy_mask, css: this.spy_css, keyup: this.spy_keyup, focus: this.spy_focus, blur: this.spy_blur};

        this.spy_dollar = spyOn(window, "$").and.callThrough();
        this.spy_each = spyOn($.fn, "each");
        this.spy_textbox = spyOn($.fn, "textbox").and.returnValue(this.textbox);
        this.spy_val = spyOn($.fn, "val");
        this.spy_datepicker = spyOn($.fn, "datepicker");
        this.spy_keydown = spyOn($.fn, "keydown");
        this.spy_keypress = spyOn($.fn, "keypress");

        this.spy_addClearIconToEasyUITextbox = spyOn(WidgetUtil, "_addClearIconToEasyUITextbox");
        this.spy_createDatepicker = spyOn(WidgetUtil, "createDatepicker");

        this.options = {
            type: {name: InputField.Type.Birthdate},
            width: 'width',
            height: 'height',
            prompt: 'prompt',
            value: 'default',
            defaultValue: 'default',
            icons: [{
                iconCls: 'icon-clear',
                handler: jasmine.any(Function)
            }],
            onChange: jasmine.any(Function)
        };
    });

    it('works if using isBirthdatepicker', function () {

        WidgetUtil.createSearchTextBox(this.element, this.options);

        expect(this.spy_dollar).toHaveBeenCalledTimes(3);
        for (var index = 0; index < 3; index++) {
            expect(this.spy_dollar.calls.argsFor(index)).toEqual([this.element]);
        }

        expect(this.spy_addClearIconToEasyUITextbox).toHaveBeenCalledTimes(1);

        expect(this.spy_textbox).toHaveBeenCalledTimes(2);
        expect(this.spy_textbox.calls.argsFor(0)).toEqual([{
            height: 'height',
            width: 'width',
            prompt: 'prompt',
            value: 'default',
            icons: [{
                iconCls: 'icon-clear',
                handler: jasmine.any(Function)
            }],
            onChange: jasmine.any(Function)
        }]);
        expect(this.spy_textbox.calls.argsFor(1)).toEqual(['textbox']);

        expect(this.spy_each).toHaveBeenCalledTimes(1);

        expect(this.spy_createDatepicker).toHaveBeenCalledTimes(1);
        expect(this.spy_createDatepicker.calls.argsFor(0)).toEqual([
            this.textbox,
            {
                maxDate: '-1D',
                showOn: 'focus',
                buttonImage: '',
                yearRange: '-120:+20',
                constrainInput: true,
                beforeShow: jasmine.any(Function),
                onClose: jasmine.any(Function),
                onSelect: jasmine.any(Function)
            },
            true,
            true
        ]);
    });
    it('works if using (non Birthday) datepicker', function () {

        this.options.type.name = InputField.Type.Date;

        WidgetUtil.createSearchTextBox(this.element, this.options);

        expect(this.spy_dollar).toHaveBeenCalledTimes(3);
        for (var index = 0; index < 3; index++) {
            expect(this.spy_dollar.calls.argsFor(index)).toEqual([this.element]);
        }

        expect(this.spy_addClearIconToEasyUITextbox).toHaveBeenCalledTimes(1);

        expect(this.spy_textbox).toHaveBeenCalledTimes(2);
        expect(this.spy_textbox.calls.argsFor(0)).toEqual([{
            height: 'height',
            width: 'width',
            prompt: 'prompt',
            value: 'default',
            icons: [{
                iconCls: 'icon-clear',
                handler: jasmine.any(Function)
            }],
            onChange: jasmine.any(Function)
        }]);
        expect(this.spy_textbox.calls.argsFor(1)).toEqual(['textbox']);

        expect(this.spy_each).toHaveBeenCalledTimes(1);

        expect(this.spy_createDatepicker).toHaveBeenCalledTimes(1);
        expect(this.spy_createDatepicker.calls.argsFor(0)).toEqual([
            this.textbox,
            {
                showOn: 'focus',
                buttonImage: '',
                yearRange: '-120:+20',
                constrainInput: true,
                beforeShow: jasmine.any(Function),
                onClose: jasmine.any(Function),
                onSelect: jasmine.any(Function)
            },
            true,
            true
        ]);
    });
    it('works if NOT using datepicker, no maskFormat', function () {

        this.options.type.name = 'notDatePicker';

        WidgetUtil.createSearchTextBox(this.element, this.options);

        expect(this.spy_dollar).toHaveBeenCalledTimes(3);
        for (var index = 0; index < 3; index++) {
            expect(this.spy_dollar.calls.argsFor(index)).toEqual([this.element]);
        }

        expect(this.spy_addClearIconToEasyUITextbox).toHaveBeenCalledTimes(1);

        expect(this.spy_textbox).toHaveBeenCalledTimes(2);
        expect(this.spy_textbox.calls.argsFor(0)).toEqual([{
            height: 'height',
            width: 'width',
            prompt: 'prompt',
            value: 'default',
            icons: [{
                iconCls: 'icon-clear',
                handler: jasmine.any(Function)
            }],
            onChange: jasmine.any(Function)
        }]);
        expect(this.spy_textbox.calls.argsFor(1)).toEqual(['onInput', jasmine.any(Function)]);

        expect(this.spy_each).toHaveBeenCalledTimes(1);
    });
    it('works if NOT using datepicker, WITH maskFormat', function () {

        this.options.type.name = 'notDatePicker';
        this.options.maskFormat = 'mask';

        WidgetUtil.createSearchTextBox(this.element, this.options);

        expect(this.spy_dollar).toHaveBeenCalledTimes6;
        for (var index = 0; index < 5; index++) {
            expect(this.spy_dollar.calls.argsFor(index)).toEqual([this.element]);
        }

        expect(this.spy_addClearIconToEasyUITextbox).toHaveBeenCalledTimes(1);

        expect(this.spy_textbox).toHaveBeenCalledTimes(6);
        expect(this.spy_textbox.calls.argsFor(1)).toEqual(["textbox"]);
        expect(this.spy_textbox.calls.argsFor(2)).toEqual(["textbox"]);
        expect(this.spy_textbox.calls.argsFor(3)).toEqual(["textbox"]);
        expect(this.spy_textbox.calls.argsFor(4)).toEqual(["textbox"]);
        expect(this.spy_textbox.calls.argsFor(5)).toEqual(['onInput', jasmine.any(Function)]);
        expect(this.spy_blur).toHaveBeenCalledTimes(1);
    });
});

describe('it filters after backspace removal of value in a masked filter field', function(){

});

