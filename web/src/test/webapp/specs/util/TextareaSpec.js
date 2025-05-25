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
describe('The textarea character-counting widget', function() {

    var keyupHandler = function() {
        var elem = $("#myTextarea");
        var remainingCharacters = WidgetUtil.limitAndReturnTextInputSize(elem, 11);
        $("#myCharacterCountIndicator").text(remainingCharacters + " characters remaining.");
    }

    it('does not truncate the initial content if it is not longer than the limit', function() {

        FixtureHelper.loadTestHtmlFixtureIntoDom("util/html/elements-for-character-counter-at-the-limit.html");

        // verify the initial HTML content
        expect($('#myTextarea').val()).toBe("just eleven");
        expect($('#myCharacterCountIndicator').html()).toBe('');

        keyupHandler();

        expect($('#myTextarea').val()).toBe("just eleven");
        expect($('#myCharacterCountIndicator').html()).toBe('0 characters remaining.');

    });


    it('truncates the initial content if it is over the maximum', function() {

        FixtureHelper.loadTestHtmlFixtureIntoDom("util/html/elements-for-character-counter-over-the-limit.html");

        // verify the initial HTML content
        expect($('#myTextarea').val()).toBe("initial content is more than 10 characters");
        expect($('#myCharacterCountIndicator').html()).toBe('');

        keyupHandler();

        expect($('#myTextarea').val()).toBe("initial con");
        expect($('#myCharacterCountIndicator').html()).toBe('0 characters remaining.');

    });


    it('updates the character count when content is replaced with new content', function() {

        FixtureHelper.loadTestHtmlFixtureIntoDom("util/html/elements-for-character-counter-at-the-limit.html");

        // verify the initial HTML content
        keyupHandler();
        $('#myTextarea').keyup(keyupHandler);

        $('#myTextarea').val('13 in a dozen');
        $('#myTextarea').keyup();

        expect($('#myTextarea').val()).toBe("13 in a doz");
        expect($('#myCharacterCountIndicator').html()).toBe('0 characters remaining.');

        $('#myTextarea').val('just 6');
        $('#myTextarea').keyup();

        expect($('#myTextarea').val()).toBe("just 6");
        expect($('#myCharacterCountIndicator').html()).toBe('5 characters remaining.');

    });

});

