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
var timelineHours = 0;
var startHour = 0;
var startDate;
var displayId = 'timelineHours12';
var classId = 'timeline12';
var selectedSublocations;
var selectedResourceType;
var timeLineOrder = "ASC";

function initTimelineTooltip(){
    $(document).tooltip({
        items: "[title]",
        tooltipClass: "timelineTooltip",
        content: function () {
            var element = $(this);
            var hoverClass = $(this).attr("class");
            if (hoverClass == 'appointment') {
                var hoverText = "";
                if (user.institutionRole.id != STUDY_STAFF) {
                    hoverText = "<div><span class='appointmentHoverLabel'>Subject Name:</span>\
                                 <span class='appointmentHoverItem'>" + element.attr("data-subjectName") + "</span></div>\
                                 <div><span class='appointmentHoverLabel'>Local ID:</span>\
                                 <span class='appointmentHoverItem'>" + element.attr("id") + "</span></div>\
                                 <div><span class='appointmentHoverLabel'>PI Name:</span>\
                                 <span class='appointmentHoverItem'>" + element.attr("value") + "</span></div>\
                                 <div><span class='appointmentHoverLabel'>Visit Name:</span> \
                                 <span class='appointmentHoverItem'>" + element.attr("data-visitName") + "</span></div>";
                }
                return "<div class='timelineHover appointmentHover'> " + hoverText +
                                   "<div><span class='appointmentHoverLabel'>Room(s):</span> \
                                    <span class='appointmentHoverItem'>" + element.attr("data-rooms") + "</span></div> \
                                    <div><span class='appointmentHoverLabel'>Resource Start:</span> \
                                    <span class='appointmentHoverItem'>" + element.attr("data-resourceHoverStartTime") + "</span></div> \
                                    <div><span class='appointmentHoverLabel'>Resource End:</span> \
                                    <span class='appointmentHoverItem'>" + element.attr("data-resourceHoverEndTime") + "</span></div> \
                                    <div><span class='appointmentHoverLabel'>Visit Start:</span> \
                                    <span class='appointmentHoverItem'>" + element.attr("data-visitHoverStartTime") + "</span></div> \
                                    <div><span class='appointmentHoverLabel'>Visit End:</span> \
                                    <span class='appointmentHoverItem'>" + element.attr("data-visitHoverEndTime") + "</span></div> \
                        </div>"
            }
        },
        position: {
            my: "center bottom",
            at: "center top",
            using: function (position, feedback) {
                $(this).css(position);
                $("<div>")
                    .addClass("arrow")
                    .addClass(feedback.vertical)
                    .addClass(feedback.horizontal)
                    .appendTo(this);
            }
        }
    });
}

function sundayBeforeOrEqualDate(date) {
    var dayOfWeek = date.getDay();

    // dayOfWeek goes 0-6 for sunday-saturday, respectively
    var deltaDaysToPreviousSunday = dayOfWeek;

    var millisToPreviousSunday = deltaDaysToPreviousSunday * millisPerDay;

    var previousSunday = new Date(date.getTime() - millisToPreviousSunday);

    return previousSunday;
}

function doTimeline(numTimelineHours, startHour, displayId, classId, resourceType) {
    //reset UI
    if (validationCheck() == false) {
        return;
    }
    $('#timelineContainer').html("");
    $.blockUI();
    selectedResourceType = resourceType;

    //Date Header above the timeLine
    var sort_header = 'css/images/sort_asc.png';

    if (timeLineOrder == 'DESC') {
        sort_header = 'css/images/sort_desc.png';
    }

    var out = " <div id='timelineBody' class='timelineBody " + classId + "'> <div id ='" +
        displayId + "' class='timelineHours'>" +
        // left column header
        " <div class='resourceLabel' onclick='sortByTimelineResourceName();'>Resource<img src='" +
        sort_header + "' alt='\/' /></div>";

    var endDate = new Date(startDate);
    var endHours = startDate.getHours() + numTimelineHours;
    endDate.setHours(endHours, 59, 0, 0);
    var endDateVal = endDate.valueOf();
    var startDateVal = startDate.valueOf();
    var sundaySOW = sundayBeforeOrEqualDate(startDate);

    // display timeline header
    if (classId == 'timelineWeek') {
        endDate = new Date(sundaySOW.valueOf() + (6 * millisPerDay));
        endDate.setHours(23, 59, 0, 0);
        startDateVal = sundaySOW.valueOf();
        endDateVal = endDate.valueOf();

        for (var i = 0; i < 7; i++) { // start the week from monday
            var curr = new Date(sundaySOW.valueOf() + (millisPerDay * i));
            out += "<div class='timelineDay'><div class='timelineDayDay'>" +
                dayOfWeekString[curr.getDay()] +
                "</div><div class='timelineDayHours'>00-08</div><div class='timelineDayHours'>08-16</div>" +
                "<div class='timelineDayHours'>16-00</div></div>";
        }
    }
    else {
        for (var i = 0; i <= numTimelineHours; i++) {
            var displayHour = i + startDate.getHours();

            var dateForHours = new Date();
            dateForHours.setHours(displayHour, 0, 0, 0);

            var style = "";
            if (i == numTimelineHours) {
                style = "style='padding-right: 0;'";
            }

            out += "<div class='timelineHour' " + style + ">" + twoPad(dateForHours.getHours(), classId) + "</div>";
        }
    }

    out += "</div>"; // end of timeLineBody

    // display date header
    var dateHeader = '';
    if (classId == 'timelineWeek') {
        dateHeader =
            dayOfWeekString[sundaySOW.getDay()] + ", "
            + monthString[sundaySOW.getMonth()] + " "
            + sundaySOW.getDate() + ", "
            + sundaySOW.getFullYear()
            + " - " +
            dayOfWeekString[endDate.getDay()] + ", "
            + monthString[endDate.getMonth()] + " "
            + endDate.getDate() + ", "
            + endDate.getFullYear();
    }
    else {
        dateHeader =
            dayOfWeekString[startDate.getDay()] + ", "
            + monthString[startDate.getMonth()] + " "
            + startDate.getDate() + ", "
            + startDate.getFullYear();

        if (
            endDate.getTime() > startDate.getTime()
            && endDate.getDate() != startDate.getDate()
        ) {
            dateHeader += " - " + dayOfWeekString[endDate.getDay()] + ", "
                + monthString[endDate.getMonth()] + " "
                + endDate.getDate() + ", "
                + endDate.getFullYear();
        }

    }

    $(".dateHeader").text(dateHeader);

    var sublocations = encodeURIComponent(JSON.stringify(selectedSublocations));

    var ajaxUrl = "rest/appointment/getOverbookedResourceDataByResourceType?startDate=" +
        startDateVal + "&endDate=" + endDateVal + "&type=" + selectedResourceType +
        "&selectedSublocationFilters=" + sublocations + "&orderBy=" + timeLineOrder;

    $.getJSON(ajaxUrl, function (data) {

        var trackRows = 1;
        var rowEndDatesList = [];
        var currentId = 0;
        var prevId = 0;
        var iteration = 0;
        var resourceUsedTimeLine = "";
        var slotWidth;
        var finalMinHeight = 0;
        var topValue = 0;
        if (classId == "timeline12") {
            slotWidth = 19;
        }
        else if (classId == "timeline24") {
            slotWidth = 10;
        }
        else {
            slotWidth = 1.432;
        }

        $.each(data, function (key, val) {
            resourceUsedTimeLine = "";
            currentId = val.id;

            // width for 1 hours = 36px (15 mins = 9px) and left = 10px (minimum) at 12am - 24hr
            // width for 1 hours = 76px (15 mins = 19px) and left = 10px (minimum) at 7am - 12hr
            var timeLeft = 0;
            var timeWidth = 0;
            var baseLeft = 0;
            var overFlowWidth = 964;

            if (classId === 'timeline12') {
                baseLeft = 11;
            }
            else if (classId === 'timeline24') {
                baseLeft = 5;
            }
            else if (classId === 'timelineWeek') {
                baseLeft = -2;
            }

            //if val.endTime > val.startTime then the resource is booked (min 15 mins)
            //  so calculate the timeleft and time width
            if (val.endTime == val.startTime) {
                resourceUsedTimeLine += ' <div class="timelineStrip">  <div class="appointment" \
                style="visibility: hidden;" title=' + "" + '></div> ';
            }
            else {
                if (val.startTime < 0) {
                    timeLeft = baseLeft;
                }
                else {
                    timeLeft = baseLeft + (((val.startTime) / 15) * slotWidth);
                }
                timeWidth = (baseLeft + (((val.endTime) / 15) * slotWidth)) - timeLeft;
            }

            //if it overflows into the previous day
            if (timeLeft < 0) {
                timeLeft = baseLeft;
            }
            else if (timeLeft > overFlowWidth) { //if it overflows into the next day
                return;
            }

            //if left + width > 956 (if it overflows into the next day)
            if ((timeLeft + timeWidth) > overFlowWidth) {
                timeWidth = parseInt(overFlowWidth) - timeLeft;
            }

            // if currentId prevId then check if
            if (currentId == prevId) {
                //if the entry of same resource is in the same line
                if (val.startTime != val.endTime) {

                    for (var i = 0; i < rowEndDatesList.length; i++) {
                        var tempRow = i + 1;
                        var hasNextVal = false;
                        if (tempRow < rowEndDatesList.length) {
                            hasNextVal = true;
                        }
                        if (val.startTime >= rowEndDatesList[i]) {
                            rowEndDatesList[i] = val.endTime;
                            iteration = i;
                            break;
                        }
                        else if (val.startTime < rowEndDatesList[i]) {
                            //check if next row exists and is empty
                            if (hasNextVal == true) {
                                if (val.startTime >= rowEndDatesList[tempRow]) {
                                    rowEndDatesList[tempRow] = val.endTime;
                                    iteration = tempRow;
                                    break;
                                }
                            }
                            else {
                                rowEndDatesList.push(val.endTime);
                                iteration = trackRows;
                                trackRows++; // increment the number of rows since create new rows
                                break;
                            }
                        }
                    }

                    topValue = (iteration * 33);
                    if (topValue == 0) {
                        topValue = 4;
                    }

                    resourceUsedTimeLine += ' <div id="' + val.studyLocalId + '" value="' + val.piName + '" \
                    data-visitName="' + val.visitName + '" data-rooms="' + val.rooms +
                        '" data-visitHoverStartTime="' + val.visitHoverStartTime + '" \
                    data-visitHoverEndTime="' + val.visitHoverEndTime + '" data-resourceHoverStartTime="' +
                        val.resourceHoverStartTime + '" \
                    data-resourceHoverEndTime="' + val.resourceHoverEndTime + '" data-subjectName="' +
                        val.subjectLastName + ", " + val.subjectFirstName + '" class="appointment" \
                    style="width:' + timeWidth + 'px; left:' + timeLeft + 'px; top:' + topValue + 'px;" title=' + "" + '></div> ';
                }
            }
            else if (currentId != prevId) {
                trackRows = 1;
                rowEndDatesList = [];
                rowEndDatesList.push(val.endTime);
                finalMinHeight = (28 + ((val.resourceCount) * 33));
                if (val.startTime != val.endTime) {
                    resourceUsedTimeLine += ' <div class="timelineStrip" style="min-height:' +
                        finalMinHeight + 'px;"> <div id="' + val.studyLocalId +
                        '" value="' + val.piName + '" \
                    data-visitName="' + val.visitName + '" data-rooms="' + val.rooms +
                        '" data-visitHoverStartTime="' + val.visitHoverStartTime + '" \
                    data-visitHoverEndTime="' + val.visitHoverEndTime +
                        '" data-resourceHoverStartTime="' + val.resourceHoverStartTime + '" \
                    data-resourceHoverEndTime="' + val.resourceHoverEndTime +
                        '" data-subjectName="' + val.subjectLastName + ", " + val.subjectFirstName +
                        '" class="appointment" \
                    style="width:' + timeWidth + 'px; left:' + timeLeft + 'px;" title=' + "" + '></div> ';
                }
            }

            if (prevId != currentId) {
                // build default availability data
                var defaultAvailability = " ";

                $.each(val.defaultResourceSchedule, function (key, val) {
                    defaultAvailability += "<div class='availableRow'> <div class='availableTime'>" +
                        val.defaultScheduleTime + "</div> \
                                            <div class='availableSlots'>" +
                        val.defaultScheduleQuantity + " </div> </div> ";
                });

                // build temporary adjustment data
                var temporaryAvailability = " <div class='availableRow availableRowHeader'>" +
                    "Temporary Adjustments</div> ";
                if (val.temporaryResourceSchedule.length > 0) {
                    $.each(val.temporaryResourceSchedule, function (key, val) {
                        temporaryAvailability += "<div class='availableRow'> <div class='availableTime'>" +
                            val.exceptionScheduleTime + "</div> \
                                             <div class='availableSlots'>" + val.exceptionScheduleQuantity +
                            " </div> </div> ";
                    });
                }
                else {
                    temporaryAvailability += "<div class='availableRow'><div class='availableTime'>None</div></div> ";
                }

                if (prevId != 0) {
                    out += '</div> </div> ';
                }

                var alternateResources = " <div class='availableRow'>Other Resources Who Can Do This Task</div>  ";
                if (val.alternateResources != "") {
                    alternateResources += "<div class='resourceDetailsOther'>" + val.alternateResources + "</div>";
                }
                else {
                    alternateResources += " <div class='resourceDetailsOther'>None</div>";
                }

                var spanClass = "rowExpanderImage" + currentId;
                out += '<div id="appointmentRow" class="appointmentRow"> <div id="' + currentId +
                    '" class="appointmentLabel resourceGetDetails" onclick="displayResourceDetails(this);" \
                    title="" style="min-height:' + finalMinHeight + 'px;">\
                    <span style="color: #a68d3b; padding-left: 3px;" class=' + spanClass + '>' + "+" + ' </span> \
                    <span style="">' + val.resourceName + '</span> \
                    <div class="resourceDetails"> \
                    <div class="resourceHeader"> ' + val.resourceName + ' </div> \
                    <div class="resourceBody"> \
                    <div class="availableRow availableRowHeader">Default Availability</div> ' +
                    defaultAvailability + temporaryAvailability + alternateResources + '\
                    </div> </div> </div> \
                    ' + resourceUsedTimeLine + ' ';
            }
            else {
                out += resourceUsedTimeLine + ' ';
            }
            prevId = val.id;
        });

        $('#timelineContainer').html(out);
        $.unblockUI();
    });
}

//display resource details
function displayResourceDetails(obj) {
    $(obj).children('.resourceDetails').toggle();
    var id = obj.id;
    var classExpand = '.rowExpanderImage' + id;
    var currText = $.trim($(classExpand).text());
    var minusText = $.trim("-");
    var plusText = $.trim("+");

    if (currText == plusText) {
        $(classExpand).html(minusText);
    }
    else {
        $(classExpand).html(plusText);
    }
}

//display previous day(s)/hour(s)
function doPreviousTimeLine(numTimelineHours, startHour) {
    navigateDays(numTimelineHours, startHour, false);
}

//display next day(s)/hour(s)
function doNextTimeLine(numTimelineHours, startHour) {
    navigateDays(numTimelineHours, startHour, true);
}

function navigateDays(numTimelineHours, startHour, next) {
    if (validationCheck() == false) {
        return;
    }

    var deltaHours = 0;
    if (classId == "timeline12") {
        if (next == true) {
            deltaHours = startDate.getHours() + (numTimelineHours);
        }
        else {
            deltaHours = startDate.getHours() - (numTimelineHours);
        }
    }
    else {
        if (next == true) {
            deltaHours = startDate.getHours() + (numTimelineHours + 1);
        }
        else {
            deltaHours = startDate.getHours() - (numTimelineHours + 1);
        }
    }
    startDate.setHours(deltaHours, 0, 0, 0);
    startHour = startDate.getHours();
    $("#timeline_date").datepicker("setDate", startDate);
    doTimeline(numTimelineHours, startHour, displayId, classId, selectedResourceType);
}

//load on initial click
function loadTimelineClick() {
    $('.formElementRequired').css({visibility: "hidden"});
    if (validationCheck() == false) {
        return;
    }
    var timeLine_startDateVal = $('#timeline_date').val();
    startDate = new Date(timeLine_startDateVal);
    startDate.setHours(startHour, 0, 0, 0);
    doTimeline(timelineHours, startHour, displayId, classId, selectedResourceType);
}

function createDatePicker() {
    WidgetUtil.createDatepicker("#timeline_date", {}, false, true);
}

function loadTimeline() {
    createDatePicker();
    $("#timeline_date").datepicker("setDate", new Date());
    startDate = new Date();
    startHour = 7;
    timelineHours = 12;
    displayId = 'timelineHours12';
    classId = 'timeline12';
    startDate.setHours(startHour, 0, 0, 0);
    loadSublocationCheckBoxes();
    document.getElementById("room").checked = true;
    initTimelineTooltip();
}

function loadSublocationCheckBoxes() {
    var html = [];

    var jsonData = null;
    $.post("rest/report/loadSublocations", {data: jsonData}, function (data) {
        var parsedData = JSON.parse(data);
        var sublocations = parsedData.sublocations;
        html[html.length] = "<div class='filterLabel'>Filter by :</div>";
        for (var i = 0, len = sublocations.length; i < len; i++) {
            html[html.length] = "<div class='filterCheckbox'><input type='checkbox' onclick='uncheckOtherSublocations();' class='sublocationCheckbox' value='";
            html[html.length] = sublocations[i].id;
            html[html.length] = "' name='" + sublocations[i].id + "'> <label for='subloc_" + sublocations[i].id + "'>";
            html[html.length] = sublocations[i].name + '</label> </div>';
        }
        html[html.length] = "<div class='filterCheckbox'><input type='checkbox' value='0' id='subloc_all' class='subloc_all' name='subloc_all' onclick='toggleAllSublocations(this);'> <label for='subloc_all'>All Sub-Locations</label> </div>";
        html[html.length] = "<a onclick='javascript:filterTimelineData();' class='formButton'>Filter</a>";
        html[html.length] = "<a onclick='javascript:resetTimelineData();' class='formButton'>Reset</a>";
        html[html.length] = "<div class='floatRight'>";
        html[html.length] = "    <div id='radioButtons' class='view12HourRadioContainer'>";
        html[html.length] = "        <div class='viewRadio12'><input name='viewRadio' style='ba' type='radio' value='1' onclick='handleViewClick(this);' checked='checked'/></div>";
        html[html.length] = "        <div class='viewRadio24'><input name='viewRadio' type='radio' value='2' onclick='handleViewClick(this);' /></div>";
        html[html.length] = "        <div class='viewRadioWeek'><input name='viewRadio' type='radio' value='3' onclick='handleViewClick(this);' /></div>";
        html[html.length] = "    </div>";
        html[html.length] = "</div>";
        var result = html.join('');
        $('.sublocationFilterList').html(result);
        document.getElementById("subloc_all").checked = true;
        toggleAllSublocations(document.getElementById("subloc_all"));
        doTimeline(timelineHours, startHour, displayId, classId, '3');
    });
}

function uncheckOtherSublocations() {
    selectedSublocations = new Array();

    if($(".sublocationCheckbox:not(:checked)").length == 0){
        $(".subloc_all").prop('checked', true);
    }else{
        $(".subloc_all").prop('checked', false);
    }

    $(".sublocationCheckbox:checked").each(function () {
        selectedSublocations.push(Number($(this).val()));
    });
}

function toggleAllSublocations(source) {
    selectedSublocations = new Array();
    var checkboxes = $(".sublocationCheckbox");
    for (var i = 0, n = checkboxes.length; i < n; i++) {
        checkboxes[i].checked = source.checked;
    }
    $(".sublocationCheckbox:checked").each(function () {
        selectedSublocations.push(Number($(this).val()));
    });
}

function filterTimelineData() {
    $('.formElementRequired').css({visibility: "hidden"});
    if (validationCheck() == false) {
        return;
    }
    doTimeline(timelineHours, startHour, displayId, classId, selectedResourceType);
}

function resetTimelineData() {
    document.getElementById("subloc_all").checked = true;
    toggleAllSublocations(document.getElementById("subloc_all"));
    doTimeline(timelineHours, startHour, displayId, classId, selectedResourceType);
}

// changing the timeline view handler
function handleViewClick(view) {
    var elementClass = document.getElementById('radioButtons');
    if (view.value == 1) {
        $('#timelineBody').addClass('timeline12');
        $('#timelineBody').removeClass('timeline24');
        $('#timelineBody').removeClass('timelineWeek');
        $('#timelineHours12').show();
        $('#timelineHours24').hide();
        $('#timelineHoursWeek').hide();
        timelineHours = 12;
        startHour = 7;
        startDate.setHours(startHour, 0, 0, 0);
        $('#timelineBody').css('timelineBody timeline12');
        displayId = 'timelineHours12';
        classId = 'timeline12';
        $("#timeline_date").datepicker("setDate", startDate);
        elementClass.setAttribute('class', 'view12HourRadioContainer');
    }
    else if (view.value == 2) {
        $('#timelineBody').removeClass('timeline12');
        $('#timelineBody').addClass('timeline24');
        $('#timelineBody').removeClass('timelineWeek');
        $('#timelineHours12').hide();
        $('#timelineHours24').show();
        $('#timelineHoursWeek').hide();
        timelineHours = 23;
        startHour = 0;
        startDate.setHours(startHour, 0, 0, 0);
        $('#timelineBody').css('timelineBody timeline24');
        displayId = 'timelineHours24';
        classId = 'timeline24';
        $("#timeline_date").datepicker("setDate", startDate);
        elementClass.setAttribute('class', 'view24HourRadioContainer');
    }
    else if (view.value == 3) {
        $('#timelineBody').removeClass('timeline12');
        $('#timelineBody').removeClass('timeline24');
        $('#timelineBody').addClass('timelineWeek');
        $('#timelineHours12').hide();
        $('#timelineHours24').hide();
        $('#timelineHoursWeek').show();
        timelineHours = 167;
        startHour = 0;
        startDate.setHours(startHour, 0, 0, 0);
        $('#timelineBody').css('timelineBody timelineWeek');
        displayId = 'timelineHoursWeek';
        classId = 'timelineWeek';
        elementClass.setAttribute('class', 'viewWeeklyRadioContainer');
    }
    doTimeline(timelineHours, startHour, displayId, classId, selectedResourceType);
}

function sortByTimelineResourceName() {
    if (validationCheck() == false) {
        return;
    }
    if (timeLineOrder == "ASC") {
        timeLineOrder = "DESC";
    }
    else {
        timeLineOrder = "ASC";
    }
    doTimeline(timelineHours, startHour, displayId, classId, selectedResourceType);
}

function validationCheck() {
    showError('#sublocation_validation', '');
    showError('#timeline_dateValidation', '');
    var timeLine_startDateVal = $('#timeline_date').val();
    if (timeLine_startDateVal == '') {
        $('.formElementRequired').css({visibility: "visible"});
        showError('#timeline_dateValidation', 'Please enter valid start date');
        return false;
    }
    if (selectedSublocations.length == 0) {
        $('.formElementRequired').css({visibility: "visible"});
        showError('#sublocation_validation', 'Please select sub-location(s) to filter on.');
        return false;
    }
    return true;
}
