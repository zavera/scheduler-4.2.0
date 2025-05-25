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
describe('The column widget', function () {

    it('has Column.Checkbox().render which does render', function () {
        var columnTypeCheckbox = new Column.Checkbox();
        var inputElement = {click: function(){}, change: function(){}};
        var tdElement = {append: function(){}, addClass: function(){}};

        var spy_getRowElementClass = spyOn(columnTypeCheckbox, "getCheckboxElementClass")
            .and.returnValue('fooClass');
        var spy_dollar = spyOn(window, "$").and.returnValue(inputElement)
            .and.returnValue(inputElement);

        var spy_append = spyOn(tdElement, "append");
        var spy_addclass = spyOn(tdElement, "addClass");

        var spy_clickCallbackFactory = spyOn(columnTypeCheckbox, "clickCallbackFactory")
            .and.returnValue("click1");
        var spy_changeCallbackFactory = spyOn(columnTypeCheckbox, "changeCallbackFactory")
            .and.returnValue("change1");

        var clickReturn = {f:1};
        var changeReturn = {f:2};
        var spy_click = spyOn(inputElement, "click")
            .and.returnValue(clickReturn);
        var spy_change = spyOn(inputElement, "change")
            .and.returnValue(changeReturn);

        var checkBoxValue = 42;
        columnTypeCheckbox.render(tdElement, checkBoxValue);

        expect(spy_getRowElementClass).toHaveBeenCalledTimes(1);

        expect(spy_dollar).toHaveBeenCalledTimes(1);
        expect(spy_dollar.calls.argsFor(0)).toEqual(["<input/>", {
            type: "checkbox",
            class: "fooClass",
            value: checkBoxValue,
            id: checkBoxValue
        }]);

        expect(spy_append).toHaveBeenCalledTimes(1);
        expect(spy_addclass).toHaveBeenCalledTimes(1);
        expect(spy_append.calls.argsFor(0)).toEqual([inputElement]);

        expect(spy_clickCallbackFactory).toHaveBeenCalledTimes(1);
        expect(spy_clickCallbackFactory.calls.argsFor(0)).toEqual([columnTypeCheckbox]);

        expect(spy_changeCallbackFactory).toHaveBeenCalledTimes(1);
        expect(spy_changeCallbackFactory.calls.argsFor(0)).toEqual([columnTypeCheckbox, 42]);

        expect(spy_click).toHaveBeenCalledTimes(1);
        expect(spy_click.calls.argsFor(0)).toEqual(["click1"]);

        expect(spy_change).toHaveBeenCalledTimes(1);
        expect(spy_change.calls.argsFor(0)).toEqual(["change1"]);
    });
});

