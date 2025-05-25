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
 * Created by jill on 9/28/16.
 */

describe('Date utils', function(){


    it('can format dates', function(){
        //saturday; april 1st 2017: (months are 0-based)
        var aprilFirst2017 = new Date(2017, 3,1,9, 0,0,0);

        var expectedFormat = "04/01/2017";
        expect(showDate(aprilFirst2017)).toEqual(expectedFormat);

        var expectedLong = "Sat Apr 01 2017 09:00:00";

        expect(dateToDateHelperFormat(aprilFirst2017)).toEqual(expectedLong);

        var april2 = aprilFirst2017.addDays(1);

        expect(showDate(april2)).toEqual("04/02/2017");

        var march31 = april2.subtractDays(2);
        expect(showDate(march31)).toEqual("03/31/2017");

        expect(showClockDateTime(march31)).toEqual("03/31/2017 09:00 am");

        var tenThirty = addMinutes(march31, 90);
        expect(showClockDateTime(tenThirty)).toEqual("03/31/2017 10:30 am");

        var aprilFirstEve =  new Date(2017, 3,1,20, 30,0,0);

        expect(showStandardDateTime(aprilFirstEve)).toEqual("04/01/2017 08:30 pm");
        expect(showStandardDateTime(tenThirty)).toEqual("03/31/2017 10:30 am");

        expect(showDateTime(aprilFirstEve)).toEqual("04/01/2017 20:30");
        expect(showDateTime(tenThirty)).toEqual("03/31/2017 10:30");

        expect(showDateTimeWithThreeLetterMonth(aprilFirstEve)).toEqual("Apr 01, 2017 20:30");
        expect(showDateTimeWithThreeLetterMonth(tenThirty)).toEqual("Mar 31, 2017 10:30");

        //leaving undefined on purpose:
        var nonDate;

        //null/empty/undefined tests:
        expect(showDateTime()).toEqual("");
        expect(showClockDateTime(nonDate)).toEqual("");
        expect(showClockDateTime(null)).toEqual("");
        expect(showDate(nonDate)).toEqual("");
        expect(dateToDateHelperFormat(nonDate)).toEqual("");
        expect(dateToDateHelperFormat(null)).toEqual("");
        expect(dateToDateHelperFormat("")).toEqual("");
        expect(dateToDateHelperFormat()).toEqual("");

        expect(showStandardDateTime()).toEqual("");
        expect(showStandardDateTime(null)).toEqual("");
        expect(showStandardDateTime(nonDate)).toEqual("");
    });

    it('can format time', function(){
        //saturday; april 1st 2017: (months are 0-based)
        var aprilFirst2017 = new Date(2017, 3,1,9, 0,0,0);

        expect(showTime(aprilFirst2017)).toEqual("09:00");

        var tenThirty = addMinutes(aprilFirst2017, 90);

        expect(showTime(tenThirty)).toEqual("10:30");

        var aprilFirstEve =  new Date(2017, 3,1,20, 30,0,0);

        expect(showTime(aprilFirstEve)).toEqual("20:30");

        //leaving undefined on purpose:
        var nonDate;

        expect(showTime()).toEqual("");
        expect(showTime(null)).toEqual("");
        expect(showTime(nonDate)).toEqual("");

        expect(showStandardTime('09')).toEqual("9 am");
        expect(showStandardTime('9')).toEqual("9 am");
        expect(showStandardTime('18:30')).toEqual("6:30 pm");
        expect(showStandardTime('12:00')).toEqual("12:00 pm");
        expect(showStandardTime('11:59')).toEqual("11:59 am");

        expect(showStandardTime()).toEqual("");
        expect(showStandardTime(null)).toEqual("");
        expect(showStandardTime(nonDate)).toEqual("");

    });

    it('can validate dates', function(){

       //leaving undefined on purpose:
        var nonDate;
        var dateResultArray = [{result:false},
            {argument:nonDate, result:false},
            {argument:"abc",result:false},
            {argument:"",result:false},
            {argument:"monday, april 1st 1997",result:false},
            {argument:"04/01/1997",result:true},
            {argument:"4/01/1997",result:true},
            {argument:"4/1/1997",result:true},
            {argument:"14/01/1997",result:false},
            {argument:"02/29/1997",result:false}];


        doDateValidation(isValidDOB, dateResultArray);
        doDateValidation(isValidDate, dateResultArray);

        var future = new Date();
        future.addDays(2);

        expect(isValidDOB(showDate(future))).toEqual(false);
        expect(isValidDate(showDate(future))).toEqual(true);


        expect(isLeapYear(2000)).toEqual(true);

        // list of leap years from: http://www.miniwebtool.com/leap-years-list/?start_year=1900&end_year=2020
        var leapYears = [1904, 1908, 1912, 1916, 1920, 1924, 1928, 1932, 1936, 1940, 1944, 1948, 1952, 1956, 1960,
            1964, 1968, 1972, 1976, 1980, 1984, 1988, 1992, 1996, 2000, 2004, 2008, 2012, 2016, 2020];

        leapYears.forEach(function(year){
           expect(isLeapYear(year)).toEqual(true);
        });

        expect(isLeapYear(2015)).toEqual(false);
        expect(isLeapYear(2017)).toEqual(false);

        expect(isLeapYear(1900)).toEqual(false);

        function doDateValidation(fn, dateResults) {
            dateResults.forEach(function(oneDate){
                var argument = oneDate.argument;
                var result = oneDate.result;

                expect(fn(argument)).toEqual(result);
            });
        }
    });
});
