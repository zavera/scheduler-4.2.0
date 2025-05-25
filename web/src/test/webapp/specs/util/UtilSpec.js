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
describe('pushIfNotDup', function () {
    it('pushes into array iff the element not there already', function() {
        var myArray = [1,2,3];

        pushIfNotDup(42, myArray);
        expect(myArray).toEqual([1,2,3,42]);

        pushIfNotDup(42, myArray);
        pushIfNotDup(1, myArray);
        pushIfNotDup(2, myArray);
        expect(myArray).toEqual([1,2,3,42]);

        pushIfNotDup(666, myArray);
        expect(myArray).toEqual([1,2,3,42,666]);
    });
});
describe('isMissingFromArray', function () {
    it('says true iff the element is NOT already there', function() {
        var myArray = [1,2,3];

        expect(isMissingFromArray(42, myArray)).toBe(true);
        expect(isMissingFromArray(2, myArray)).toBe(false);
    });
});

describe('isAlreadyInArray', function () {
    it('says true iff the element is NOT already there', function() {
        var myArray = [1,2,3];

        expect(isAlreadyInArray(42, myArray)).toBe(false);
        expect(isAlreadyInArray(2, myArray)).toBe(true);
    });
});

describe('removeIfPresent', function () {
    it('says true iff the element is NOT already there', function() {
        var myArray = [1,2,3,4,5];

        removeIfPresent(42, myArray);
        expect(myArray).toEqual([1,2,3,4,5]);

        removeIfPresent(1, myArray);
        expect(myArray).toEqual([2,3,4,5]);

        removeIfPresent(5, myArray);
        expect(myArray).toEqual([2,3,4]);

        removeIfPresent(3, myArray);
        expect(myArray).toEqual([2,4]);
    });
});

