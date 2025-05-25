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
 * Created by ankit on 10/6/15.
 */
var totalDays = 1;
var daySelected;
var isInpatientVisitType;
var resourceTypeSelected = "Fixed";
var selectedEventResourceId;
var templateResourceOriginDate = new Date(2012, 1, 1, 0, 0, 0, 0);
var templateResourceOutpatientEndDate = new Date(2012, 1, 1, 23, 59, 59, 59);
var totalResources;
var borderColor;
var isEventClick = false;
var templateResourceId;
var actionType;
var templateResourceObj;
var approvedTemplate = "Not Approved";
var isLinkingFloat = false;
var templateResourcesSortBy;

var TemplateResourceTimeline = {};
var TRT = TemplateResourceTimeline;

var floatGroupTimeline;
var flexGroupTimeline;
var singleDayTimeline;
var multiDayTimeline;

const SINGLE_DAY_ID = "templateResourceSingleDayContainer";
const MULTI_DAY_ID = "templateResourceMultiDayContainer";
const FLOAT_GROUP_ID = "templateResourceFloatGroupContainer";
const FLEX_GROUP_ID = "templateResourceFlexGroupContainer";

function loadTemplateResources() {
    commonData();
    var visit = JSON.parse(sessionStorage.getItem("visitData"));
    $.ajax({
        url: "rest/study/getVisitTemplateData?visit=" + visit.id,
        dataType: 'json',
        success: function (data) {
            sessionStorage.setItem("visitData", JSON.stringify(data));
            $('.formValidationRequired').css({visibility: "hidden"});
            $('.paginateDays').css({visibility: 'hidden'});
            $('#multi_day').css({display: 'none'});
            if (user.institutionRole.id !== STUDY_STAFF) {
                $('.gantt_newResourceButton').css({display: 'inline-block'});
            }
            $('.gantt_newResourceButton').css({float: 'right'});
            $('.gantt_selectResourcesButton').css({display: 'none'});
            $('.gantt_copyDayButton').css({visibility: 'hidden'});
            $('#total_resources_count').html(0);
            $('#day_resources_count').html(0);
            app_selectedVisit = JSON.parse(sessionStorage.getItem("visitData"));
            app_selectedStudy = JSON.parse(sessionStorage.getItem("studyData"));
            $('#template_localId').html(app_selectedStudy.localId);
            $('#template_pi').html(app_selectedStudy.investigator.firstName + " " + app_selectedStudy.investigator.lastName);
            $('#template_studyName').html(app_selectedVisit.studyName);
            $('#template_visitName').html(app_selectedVisit.visitName);
            $('#template_visitSublocation').html(app_selectedVisit.sublocationName);
            $('#template_visitType').html(app_selectedVisit.visitTypeName);
            approvedTemplate = app_selectedVisit.approved;
            setTemplateApprovalStatus();
            isInpatientVisitType = app_selectedVisit.visitTypeName.indexOf("Inpatient") > -1;
            var isRelativeTime = app_selectedVisit.relativeTime;
            $('#template_type').html(isInpatientVisitType && !isRelativeTime ? '<img src="css/images/gantt/clock_time.png" style="vertical-align: bottom; height: 16px;"/> Clock Time' : '<img src="css/images/gantt/relative_time.png" style="vertical-align: bottom; height: 16px;"/> Relative Time ');
            createPagination();
            daySelected = 1;
            $(".pageInput").val(daySelected);
            totalResources = 0;
            var pageDate = templateResourceOriginDate.addDays(daySelected - 1);
            getResourceStaticList();
            visitTemplateDateTimePickers();
            templateResourcesSortBy = "StartAsc";
            setSortTemplateResourceDropDownValues();
            loadTimelineEvents(daySelected, pageDate, templateResourcesSortBy);
        }
    });
}

function createPagination() {
    var pagination = "Day " + "<input type='text' style='width:20px; color: #0A507D; border: 1px solid #0A507D;' \
        class='pageInput' onkeypress='return paginateEnterKeySubmit(this, event)' onchange='updateInputVal(this)'/> of " + totalDays + "\
        <input type='button' value='Go' class='formButton' onclick='goToInputDay();' \
        style='min-width: 30px; margin: 0; height: auto;'/>";
    $("#topGanttDateHeader").html(pagination);
    $("#bottomGanttDateHeader").html(pagination);
    togglePagination();
}

function paginateEnterKeySubmit(myfield, e) {
    var keycode;
    if (window.event) keycode = window.event.keyCode;
    else if (e) keycode = e.which;
    else return true;

    if (keycode == 13) {
        if (isNaN(myfield.value) || myfield.value == 'undefined') {
            util_showMainMessage("Please enter a valid page number.");
            myfield.value = 1;
            $(".pageInput").val(1);
            return false;
        }
        if (myfield.value > totalDays || myfield.value < 1) {
            util_showMainMessage("Entered value outside the page range. Please enter a valid page.");
            $(".pageInput").val(1);
            return false;
        }
        daySelected = myfield.value;
        $(".pageInput").val(daySelected);
        goToInputDay();
        return false;
    }
    else {
        return true;
    }
}

function togglePagination() {
    if (totalDays <= 1) {
        hidePagination();
    }
    else if (daySelected == 1) {
        hidePrevPagination();
    }
    else if (daySelected == totalDays) {
        hideNextPagination();
    }
    else {
        displayPagination();
    }
}

function displayPagination() {
    $('.firstDay').css({visibility: 'visible'});
    $('.previousDay').css({visibility: 'visible'});
    $('.nextDay').css({visibility: 'visible'});
    $('.lastDay').css({visibility: 'visible'});
}

function hidePagination() {
    $('.firstDay').css({visibility: 'hidden'});
    $('.previousDay').css({visibility: 'hidden'});
    $('.nextDay').css({visibility: 'hidden'});
    $('.lastDay').css({visibility: 'hidden'});
}

function hidePrevPagination() {
    $('.firstDay').css({visibility: 'hidden'});
    $('.previousDay').css({visibility: 'hidden'});
    $('.nextDay').css({visibility: 'visible'});
    $('.lastDay').css({visibility: 'visible'});
}

function hideNextPagination() {
    $('.firstDay').css({visibility: 'visible'});
    $('.previousDay').css({visibility: 'visible'});
    $('.nextDay').css({visibility: 'hidden'});
    $('.lastDay').css({visibility: 'hidden'});
}

function updateInputVal(obj) {
    if (isNaN(obj.value) || obj.value == 'undefined') {
        util_showMainMessage("Please enter a valid page number.");
        obj.value = 1;
        $(".pageInput").val(1);
        return false;
    }
    if (obj.value > totalDays || obj.value < 1) {
        util_showMainMessage("Entered value outside the page range. Please enter a valid page.");
        $(".pageInput").val(1);
        return false;
    }
    $(".pageInput").val(obj.value);
    return true;
}

function goToInputDay() {
    $.blockUI();
    daySelected = $(".pageInput").val();
    $(".pageInput").val(daySelected);
    if (daySelected > totalDays) {
        util_showMainMessage("Entered value outside the page range. Please enter a valid page.");
        return;
    }
    var pageDate = templateResourceOriginDate.addDays(daySelected - 1);
    setSortTemplateResourceDropDownValues();
    timelinePagination(pageDate);
}

function goToFirstDay() {
    $.blockUI();
    daySelected = 1;
    $(".pageInput").val(daySelected);
    var pageDate = templateResourceOriginDate;
    setSortTemplateResourceDropDownValues();
    timelinePagination(pageDate);
}

function goToLastDay() {
    $.blockUI();
    daySelected = totalDays;
    $(".pageInput").val(daySelected);
    var pageDate = templateResourceOriginDate.addDays(daySelected - 1);
    setSortTemplateResourceDropDownValues();
    timelinePagination(pageDate);
}

function goToPreviousDay() {
    $.blockUI();
    daySelected = $(".pageInput").val() != "" ? parseInt($(".pageInput").val()) - 1 : 1;
    if (daySelected > 0) {
        $(".pageInput").val(daySelected);
        var pageDate = templateResourceOriginDate.addDays(daySelected - 1);
        setSortTemplateResourceDropDownValues();
        timelinePagination(pageDate);
    }
}

function goToNextDay() {
    $.blockUI();
    daySelected = $(".pageInput").val() != "" ? parseInt($(".pageInput").val()) + 1 : 1;
    if (daySelected <= totalDays) {
        $(".pageInput").val(daySelected);
        var pageDate = templateResourceOriginDate.addDays(daySelected - 1);
        setSortTemplateResourceDropDownValues();
        timelinePagination(pageDate);
    }
}

function renderCopyDayButton() {
    if (isInpatientVisitType) {
        $('.paginateDays').css({visibility: 'visible'});
        daySelected = $(".pageInput").val();
        if (user.institutionRole.id != STUDY_STAFF) {
            $.getJSON("rest/appointment/isDayCopyable?visitId=" + app_selectedVisit.id + "&currentDay=" + daySelected, function (data) {
                if (data) {
                    $('.gantt_copyDayButton').css({visibility: 'visible'});
                }
            });
        }
    }
}

function loadTimelineEvents(daySelected, pageDate, sortType) {
    $('.gantt_copyDayButton').css({visibility: 'hidden'});
    $('.paginateDays').css({visibility: 'hidden'});
    $('.gantt_newResourceButton').css({float: 'right'});
    $('.gantt_selectResourcesButton').css({display: 'none'});

    if (actionType == "delete_template_resource" && totalResources == 1 && totalDays == daySelected) {
        $("#toCopyDaySelect option[value='" + daySelected + "']").remove();
        daySelected = 1;
        pageDate = templateResourceOriginDate;
        $(".pageInput").val(daySelected);
    }

    totalResources = 0;

    $.ajax({
        url: 'rest/appointment/getGanttInfo/combo?visit=' + app_selectedVisit.id + '&dayOffset=' + daySelected + '&sortType=' + sortType,
        dataType: 'json',
        success: function (data) {
            sortType = sanitizeSortType(sortType);
            var totalVisitResources = data.infoDayResources.totalVisitResources;
            $('#total_resources_count').html(totalVisitResources);
            var resourcesData = data.infoDayResources.ganttResourceDetailList;
            var eventsData = data.infoDayEvents.ganttEventDetailList;
            totalResources += eventsData.length;
            var obj = $('#templateResourceSingleDayContainer').parent().find(' > div')[0];
            obj.title = "Expand";
            toggleGanttView(obj, SINGLE_DAY_ID);
            loadTemplateResourcesData(SINGLE_DAY_ID, resourcesData, eventsData, pageDate, sortType);
            if (resourcesData.length > 0) {
                $('#no_one_resources').css({display: 'none'});
            } else {
                $('#templateResourceSingleDayContainer').parent().find(' > div > div > div > table > tbody').css({display: 'none'});
                $('#no_one_resources').css({display: 'block'});
            }
            if (totalVisitResources > 0) {
                $('.gantt_newResourceButton').css({float: ''});
                user.institutionRole.id != STUDY_STAFF ? $('.gantt_selectResourcesButton').css({display: 'inline-block'}) : $('.gantt_selectResourcesButton').css({display: 'none'});
                renderCopyDayButton();
            }

            if (isInpatientVisitType) {
                resourcesData = data.infoMultiResources.ganttResourceDetailList;
                eventsData = data.infoMultiEvents.ganttEventDetailList;
                totalDays = data.infoMultiResources.totalVisitCalendarDays;
                if (totalVisitResources > 0 && daySelected > totalDays) {
                    daySelected = 1;
                    $(".pageInput").val(daySelected);
                    timelinePagination(templateResourceOriginDate);
                    return;
                }
                $('#multi_day').css({display: 'block'});
                totalResources += eventsData.length;
                var obj = $('#templateResourceMultiDayContainer').parent().find(' > div')[0];
                obj.title = "Expand";
                toggleGanttView(obj, MULTI_DAY_ID);
                loadTemplateResourcesData(MULTI_DAY_ID, resourcesData, eventsData, pageDate, sortType);
                if (resourcesData.length > 0) {
                    $('#no_multi_resources').css({display: 'none'})
                } else {
                    $('#templateResourceMultiDayContainer').parent().find(' > div > div > div > table > tbody').css({display: 'none'});
                    $('#no_multi_resources').css({display: 'block'});
                }
            }

            resourcesData = data.infoFloatResources.ganttResourceGroupList;
            eventsData = data.infoFloatEvents.ganttEventDetailList;
            totalResources += eventsData.length;
            var obj = $('#templateResourceFloatGroupContainer').parent().find(' > div')[0];
            obj.title = "Expand";
            toggleGanttView(obj, FLOAT_GROUP_ID);
            loadTemplateResourcesData(FLOAT_GROUP_ID, resourcesData, eventsData, pageDate, sortType);
            if (resourcesData.length > 0) {
                $('#no_float_resources').css({display: 'none'})
            } else {
                $('#templateResourceFloatGroupContainer').parent().find(' > div > div > div > table > tbody').css({display: 'none'});
                $('#no_float_resources').css({display: 'block'});
            }

            resourcesData = data.infoFlexResources.ganttResourceGroupList;
            eventsData = data.infoFlexEvents.ganttEventDetailList;
            totalResources += eventsData.length;
            var obj = $('#templateResourceFlexGroupContainer').parent().find(' > div')[0];
            obj.title = "Expand";
            toggleGanttView(obj, FLEX_GROUP_ID);
            loadTemplateResourcesData(FLEX_GROUP_ID, resourcesData, eventsData, pageDate, sortType);
            if (resourcesData.length > 0) {
                $('#no_flex_resources').css({display: 'none'})
            } else {
                $('#templateResourceFlexGroupContainer').parent().find(' > div > div > div > table > tbody').css({display: 'none'});
                $('#no_flex_resources').css({display: 'block'});
            }
            $('#day_resources_count').html(totalResources);
            createPagination();
            $(".pageInput").val(daySelected);
            $.unblockUI();
        }
    });
}

function sanitizeSortType(sortType) {
    switch (sortType) {
        case "StartAsc":
            return "earliestStart";
            break;
        case "NameAsc":
            return "resourceName";
            break;
        case "NameDesc":
            return "-resourceName";
            break;
        case "TypeAsc":
            return "resourceType";
            break;
        case "TypeDesc":
            return "-resourceType";
            break;
    }
}

function destroyTimelines() {
    if (singleDayTimeline !== undefined) singleDayTimeline.destroy();
    if (multiDayTimeline !== undefined) multiDayTimeline.destroy();
    if (floatGroupTimeline !== undefined) floatGroupTimeline.destroy();
    if (flexGroupTimeline !== undefined) flexGroupTimeline.destroy();
}

function timelinePagination(pageDate) {
    destroyTimelines();
    loadTimelineEvents(daySelected, pageDate, templateResourcesSortBy);
}

TRT.loadSelectResourcesDataOpen = function() {
    $("#allResources").prop("checked", true);
    $("#nonBillableResources").prop("checked", true);
    $('.formValidationRequired').css({visibility: "hidden"});
    createSelectableTable();

    VisitTables.buildDisplayFilterSelect(ResourceBillingTable.id, ResourceBillingTable.filterOptions);
};

TRT.loadSelectResourcesDataBeforeClose = function() {
    if (ResourceBillingTable.currentTable.getSelectedRows().length > 0) {
        confirmNavigateDialog(true);
        return false;
    }
    else {
        return true;
    }
};

TRT.loadSelectResourcesDataClose = function() {
    ResourceBillingTable.currentTable.clearAllSelectedRows();
    $('#select_template_resource_save').css({display: 'none'});
    $('#select_template_resource_save_and_close').css({display: 'none'});
    $("#select_template_resource").dialog("close");
    var msg = "Applying changes to checked resources will mark them as Billable.";
    $("#bulkChangeMessage").html(msg);
};

function loadSelectResourcesData() {
    $.blockUI({fadeIn: 0});
    $("#select_template_resource").dialog({
        modal: true,
        height: 625,
        width: 900,
        resizable: false,
        open: TRT.loadSelectResourcesDataOpen,
        beforeClose: TRT.loadSelectResourcesDataBeforeClose,
        close: TRT.loadSelectResourcesDataClose
    });
}

function clearAllSelection() {
    createSelectableTable();
    $('#select_template_resource_save').css({display: 'none'});
    $('#select_template_resource_save_and_close').css({display: 'none'});
}

function createSelectableTable() {
    ResourceBillingTable.createResourceBillingTable();
    ResourceBillingTable.populateResourceBillingTable();
}

function filterSelectedResources(tableName, value) {
    var timeout = setTimeout(function() {
        $.blockUI({fadeIn: 0});
    }, 5000);

    var elementsToShow;
    if (value == "All") {
        elementsToShow = $('#'+tableName+' tbody tr.' + tableName  + '-tableRow');
        elementsToShow.show();

        var unselectedRows = $('#'+tableName+' tbody tr.' + tableName  + '-tableRow').filter(function () {
            return $(this).find('td').first().find("input").is(":checked") === false
        });
        if(unselectedRows.length == 0)
        {
            $('#'+tableName+'-columnLabelRow').find(".checkbox").prop("checked", true);
        }
        else {
            $('#'+tableName+'-columnLabelRow').find(".checkbox").prop("checked", false);
        }
    }
    else {
        elementsToShow = $('#'+tableName+' tbody tr.' + tableName  + '-tableRow').filter(function () {
            if (value === "Yes") {
                return $(this).find('td').first().find("input").is(":checked") === true
            }
            else {
                return $(this).find('td').first().find("input").is(":checked") === false
            }
        });
        elementsToShow.show();

        var elementsToHide = $('#'+tableName+' tbody tr.' + tableName  + '-tableRow').filter(function () {
            if (value === "Yes") {
                return $(this).find('td').first().find("input").is(":checked") === false
            }
            else {
                return $(this).find('td').first().find("input").is(":checked") === true
            }
        });
        elementsToHide.hide();

        if(value === "Yes")
        {
            if(elementsToShow.length > 0)
            {
                $('#'+tableName+'-columnLabelRow').find(".checkbox").prop("checked", true);
            }
            else {
                $('#'+tableName+'-columnLabelRow').find(".checkbox").prop("checked", false);
            }
        }
        else if(value === "No")
        {
            $('#'+tableName+'-columnLabelRow').find(".checkbox").prop("checked", false);
        }
    }

    if(elementsToShow.length > 0)
    {
        $('#'+tableName+'-columnLabelRow').find(".checkbox").removeProp("disabled");
    }
    else {
        $('#'+tableName+'-columnLabelRow').find(".checkbox").prop("disabled", true);
    }

    clearTimeout(timeout);
    $.unblockUI();
}

function confirmNavigateDialog(closeAll) {
    $("#ignore-changes-confirm").dialog({
        resizable: false,
        height: 200,
        width: 510,
        modal: true,
        buttons: {
            "Cancel": function () {
                $(this).dialog("close");
            },
            "Continue": function () {
                $("#ignore-changes-confirm").dialog("close");
                ResourceBillingTable.currentTable.clearAllSelectedRows();
                if (closeAll) {
                    $("#select_template_resource").dialog("close");
                    $(".ui-dialog-content").dialog("close");
                }
                else {
                    isBillableViewSelected() ? $("#nonBillableResources").prop("checked", true)
                        : $("#billableResources").prop("checked", true);

                    $('#select_template_resource_save').css({display: 'none'});
                    $('#select_template_resource_save_and_close').css({display: 'none'});
                    changeBillableView();
                }
            }
        }
    })
}

function viewSelectedResources() {
    if (ResourceBillingTable.currentTable.getSelectedRows().length > 0) {
        isBillableViewSelected() ? $("#nonBillableResources").prop("checked", true)
            : $("#billableResources").prop("checked", true);

        confirmNavigateDialog(false);
    }
    else {
        changeBillableView();
    }
}

function changeBillableView() {
    var msg = "Applying changes to checked resources will mark them as Non-Billable.";
    isBillableViewSelected() ? msg : msg = "Applying changes to checked resources will mark them as Billable.";
    $("#bulkChangeMessage").html(msg);

    ResourceBillingTable.populateResourceBillingTable();
}

function isBillableViewSelected() {
    return $('input:radio[name=viewSelectedResource]:checked').val() == "billable";
}

function cancelSelectTemplateResourceClick() {
    if (ResourceBillingTable.currentTable.getSelectedRows().length > 0) {
        confirmNavigateDialog(true);
    }
    else {
        $("#select_template_resource").dialog("close");
    }
}

function saveSelectTemplateResource(closeDialog) {

    var templateResourcesCommaString = ResourceBillingTable.currentTable.getSelectedRows().join(",");

    actionType = "billable_selected";
    if (approvedTemplate == "Approved") {
        displayCrudConfirmationDialog(templateResourcesCommaString, closeDialog);
    }
    else {
        confirmChangeBillableResources(templateResourcesCommaString, isBillableViewSelected(), closeDialog);
    }
}

function confirmChangeBillableResources(templateResourcesCommaString, isBillable, closeDialog) {
    $.ajax({
        url: "rest/appointment/updateTemplateResourcesBillable?visitId=" + app_selectedVisit.id + "&templateResourcesCommaString=" + templateResourcesCommaString
        + "&isBillable=" + !isBillable,
        dataType: 'json',
        success: function (data) {
            actionType = "";
            clearAllSelection();
            if (closeDialog) {
                cancelSelectTemplateResourceClick();
            }
            var confirmationMessage = "Resources successfully updated.";
            util_showMainMessage(confirmationMessage);
            approvedTemplate = "Not Approved";
            setTimeout(function () {
                reloadTemplateResourcesData();
            }, 1000);
        }
    });
}

function updateTemplateResourceTime(event) {
    destroyTimelines();
    var startMinutes = resourceTimelineDateToMinutes(event.start.format());
    var endMinutes = resourceTimelineDateToMinutes(event.end.format());
    $.ajax({
        url: "rest/appointment/updateTemplateResourceTime?templateResourceId=" + event.id + "&startMinutes=" + startMinutes + "&endMinutes=" + endMinutes,
        dataType: 'json',
        success: function (data) {
            util_showMainMessage("Resource was successfully updated.");
            approvedTemplate = "Not Approved";
            setTimeout(function () {
                reloadTemplateResourcesData();
            }, 1000);
            return;
        },
        error: function () {
            util_showMainMessage("There was an error updating the resource.");
            return;
        }
    });
}

function dragResizeEventsConfirmation(event, revertFunc) {
    var displayEventStart = event.start;
    var displayEventEnd = event.end;
    var newDuration = ((displayEventEnd - displayEventStart) / 60000);
    if (displayEventStart < templateResourceOriginDate ||
        (!isInpatientVisitType && displayEventEnd > templateResourceOutpatientEndDate) ||
        (newDuration < 15)) {
        revertFunc();
        return;
    }

    if (event.extendedProps.floatable) {
        var displayEventPreferredStart = new Date(event.extendedProps.floatStartPreferred);
        var displayEventPreferredEnd = new Date(event.extendedProps.floatEndPreferred);

        if (((displayEventStart > displayEventPreferredStart)
            || (displayEventEnd < displayEventPreferredEnd))
            || (displayEventStart.getTime() == displayEventPreferredStart.getTime()
            && displayEventEnd.getTime() == displayEventPreferredEnd.getTime())) {
            revertFunc();
            return;
        }
        else {
            updateTemplateResourceTime(event);
        }
    }
    else {
        updateTemplateResourceTime(event);
    }
}

function eventDragDropResize(event, revertFunc) {
    actionType = "drag_drop_events";
    if (approvedTemplate == "Approved") {
        displayCrudConfirmationDialog(event, revertFunc);
    }
    else {
        dragResizeEventsConfirmation(event, revertFunc);
    }
}

TRT.loadTemplateResourcesData_eventDrop = function(info) {
    var event = info.event;
    var revertFunc = info.revert;
    if (event._def.resourceIds[0] !== selectedEventResourceId) {
        revertFunc();
    }

    eventDragDropResize(event, revertFunc);
};

TRT.loadTemplateResourcesData_eventResize = function(info) {
    var event = info.event;
    var revertFunc = info.revert;
    eventDragDropResize(event, revertFunc);
};

TRT.loadTemplateResourcesData_eventDragStart = function(info) {
    var event = info.event;
    selectedEventResourceId = event._def.resourceIds[0];
};

TRT.loadTemplateResourcesData_eventMouseover = function(info) {
    var calEvent = info.event;
    var jsEvent = info.jsEvent;
    closeAllHovers();
    var topHeight = jsEvent.pageY - 50;
    var leftWidth = jsEvent.pageX - 150;
    borderColor = calEvent.color == "#D8BFD8" ? "#662E91" : calEvent.color;
    var isFloatable = calEvent.extendedProps.floatable;
    var hoverFloatText = isFloatable ? "<div class='appointmentHoverRow'>\
                                            <span class='appointmentHoverLabel'>Float Start Time</span>\
                                            <span class='appointmentHoverItem'>" + calEvent.extendedProps.displayFloatStart + "</span>\
                                        </div>\
                                        <div class='appointmentHoverRow'>\
                                            <span class='appointmentHoverLabel'>Float End Time</span>\
                                            <span class='appointmentHoverItem'>" + calEvent.extendedProps.displayFloatEnd + "</span></div>" : "";

    var toolTip = "<div id='timelineTooltip' class='appointmentHover' style='z-index:10001; left:" +
        leftWidth + "px; top:" + topHeight + "px; border: solid 5px " + borderColor + ";'>\
                                 <div class='appointmentHoverRow'>\
                                    <span class='appointmentHoverLabel'>Start Time</span>\
                                    <span class='appointmentHoverItem'>" + calEvent.extendedProps.displayStart + "</span>\
                                 </div>\
                                 <div class='appointmentHoverRow'>\
                                    <span class='appointmentHoverLabel'>End Time</span>\
                                    <span class='appointmentHoverItem'>" + calEvent.extendedProps.displayEnd + "</span>\
                                 </div>"
                                 + hoverFloatText +
                                "<div class='appointmentHoverRow'>\
                                     <span class='appointmentHoverLabel'>Duration</span> \
                                     <span class='appointmentHoverItem'>" + convertMinutesToHrMins(calEvent.extendedProps.duration) + "</span>\
                                 </div>\
                                 <div class='appointmentHoverRow'>\
                                    <span class='appointmentHoverLabel'>Standard Alternate?</span> \
                                    <span class='appointmentHoverItem'>" + yesNoValue(calEvent.extendedProps.alternate) + "</span>\
                                 </div>\
                                 <div class='appointmentHoverRow'>\
                                    <span class='appointmentHoverLabel'>Billable?</span> \
                                    <span class='appointmentHoverItem'>" + yesNoValue(calEvent.extendedProps.billable) + "</span>\
                                 </div>\
                                 <div class='appointmentHoverRow'>\
                                    <span class='appointmentHoverAnnotations'></span>\
                                    <span class='appointmentHoverAnnotations'>" +
                                    showValue(calEvent.extendedProps.annotations).split(",").join("<br />") + "</span>\
                                 </div>\
                  </div>";
    $("body").append(toolTip);

    var tooltipHeight = $('#timelineTooltip').height();
    $('.appointmentHover').css('top', topHeight - tooltipHeight);
    $('.addStyleRule').remove();
    $("body").append('<style class="addStyleRule">.appointmentHover:after{border-top: 10px solid ' +
            borderColor + ' !important;}</style>');
};

TRT.loadTemplateResourcesData_eventMouseout = function(info) {
    var calEvent = info.event;
    var jsEvent = info.jsEvent;
    $(this).css('z-index', 8);
    $('.appointmentHover').remove();
};

TRT.loadTemplateResourcesData_eventClick = function(info) {
    var calEvent = info.event;
    var jsEvent = info.jsEvent;
    $('.appointmentHover').remove();
    closeAllHovers();
    if (user.institutionRole.id !== STUDY_STAFF) {
        isEventClick = true;
        templateResourceObj = calEvent;
        var isFloatable = calEvent.extendedProps.floatable;
        var isFlexible = calEvent.extendedProps.flexible;
        var isGroupedResource = calEvent.groupId !== undefined && calEvent.groupId !== "";
        var isNotFixedResource = isFloatable || isFlexible;
        var isFlexGrouped = isGroupedResource && isFlexible;
        var isFloatGrouped = isGroupedResource && isFloatable;
        borderColor = calEvent.color === "#D8BFD8" ? "#662E91" : calEvent.color;
        templateResourceId = calEvent.id;
        var displayLinkAction = isNotFixedResource ? "<img src='css/images/gantt/icon_link.png' title='Link Resource' onclick='openLinkResourcesDialog(" + isFloatable + ")'/>" : "";
        var displayUnLinkAction = "<img src='css/images/gantt/icon_unlink.png' title='Unlink Resource' onclick='unlinkIndividualResource()'/>";
        var unlinkOrLinkAction = isGroupedResource ? displayUnLinkAction : displayLinkAction;
        var tooltip = "<div class='eventClickActions' style='z-index:10001; left:" + (jsEvent.pageX - 70) + "px; top:" + (jsEvent.pageY - 70) + "px; border: solid 5px " + borderColor + ";'> " +
            " <img src='css/images/gantt/icon_papers.jpg' title='Copy Resource' style='height: 20px;' onclick='copyTemplateResourceConfirmDialog(event)'/> &nbsp; " +
            " <img src='css/images/gantt/icon_trash.jpg' title='Delete Resource' onclick='deleteTemplateResourceConfirmDialog(event," + isFlexGrouped + "," + isFloatGrouped + ")'/> &nbsp; " +
            " <img src='css/images/gantt/icon_pencil.jpg' title='Edit Resource' onclick='editTemplateResourceConfirmDialog(event)'/> &nbsp; " +
            unlinkOrLinkAction +
            "</div>";
        $("body").append(tooltip);
        $('.addEventStyleRule').remove();
        $("body").append('<style class="addEventStyleRule">.eventClickActions:after{border-top: 10px solid ' + borderColor + ' !important;}</style>');
    }
};

TRT.loadTemplateResourcesData_eventAfterRender = function(info) {
    var event = info.event;
    var element = info.el;
    if (event.extendedProps.floatable) {

        var startDayPreferred = new Date(event.extendedProps.floatStartPreferred).getDate();
        var endDayPreferred = new Date(event.extendedProps.floatEndPreferred).getDate();
        var notSingleDayPreferred = (startDayPreferred != endDayPreferred);

        var notSingleDay = (new Date(event.extendedProps.floatStartDate).getDate()
                            !== new Date(event.extendedProps.floatEndDate).getDate());

        var isMultiDay = notSingleDayPreferred || notSingleDay;

        var floatStartDate = new Date(event.extendedProps.floatStartDate);
        if (isMultiDay && floatStartDate.getDate() != daySelected) {
            floatStartDate.setHours(0);
            floatStartDate.setMinutes(0);
            floatStartDate.setSeconds(0);
        }
        var displayFloatPreferredStart = new Date(event.extendedProps.floatStartPreferred);
        var displayFloatPreferredEnd = new Date(event.extendedProps.floatEndPreferred);
        displayFloatPreferredEnd.setSeconds(0);

        if (notSingleDayPreferred && displayFloatPreferredStart.getDate() != daySelected) {
            displayFloatPreferredStart = displayFloatPreferredStart.addDays(daySelected - displayFloatPreferredStart.getDate());
            displayFloatPreferredStart.setHours(0);
            displayFloatPreferredStart.setMinutes(0);
            displayFloatPreferredStart.setSeconds(0);
        }
        var floatStartTime = floatStartDate.addDays(daySelected - floatStartDate.getDate());
        var leftOffset = ((displayFloatPreferredStart.getTime() - floatStartTime.getTime()) / 1000) / 60;
        var floatLeft = (leftOffset / 15) * 19.85;

        var eventDuration = ((displayFloatPreferredEnd.getTime() - displayFloatPreferredStart.getTime()) / 1000) / 60;
        var floatWidth = (eventDuration / 15) * 20;

        var sameDay = daySelected == startDayPreferred || daySelected == endDayPreferred;
        var overlappingDay = daySelected > startDayPreferred && daySelected < endDayPreferred;
        var preferredTimeInView = sameDay || overlappingDay;
        !preferredTimeInView ? totalResources-- : totalResources;

        var contentElement = $(element).find(".fc-content");
        contentElement.css('left', floatLeft + "px");
        contentElement.css('width', floatWidth + 'px');
        contentElement.css('background-color', '#662E91');
        contentElement.css('position', 'relative');
    }
};

TRT.loadTemplateResourcesData_render = function(resource, el) {
    var tId = resource.extendedProps.templateResourceId;
    var img = $('<img>', {src: 'css/images/gantt/icon_info.png', style: 'vertical-align: middle;', border: '0',
                            onmouseenter: 'onResourceMouseEnter(event, this,' + tId + ')',
                            onmouseout: 'onResourceMouseOut();'});

    if (resource.extendedProps.resourceName !== undefined) {
        $(el).append(img);
    }
    else {
        var groupId = resource.extendedProps.groupId;
        var groupResourceString = resource.extendedProps.groupComboDisplay;
        var img = $('<img>', {src: 'css/images/gantt/unlink_group_resources.jpg',
                                id: 'templateResourceFloatGroupContainerImg1'});
        var span1 = $('<span></span>');
        span1.click(function () {
           unlinkGroupResources(groupId, groupResourceString);
        });
        var span2 = $('<span> Unlink Group Resources</span>');
        span2.css({'margin': '0', 'color': '#0a507d'});
        var imageEl = span1.append(img);
        imageEl.append(span2);
        isNotStudyStaff() ? $(el).append(imageEl) : el;
    }
};

TRT.loadTemplateResourcesData_loading = function(bool) {
    if (bool) {
        $.blockUI({fadeIn: 0});
    }
    else {
        $.unblockUI();
    }
};

TRT.eventRender = function (info) {
    if (info.isMirror) {
        console.log(info.el);
        console.log(info.el.firstElementChild);
        $(info.el.firstElementChild).css("visibility", "hidden");
    }
};

function loadTemplateResourcesData(containerId, resourceData, eventsData, pageDate, sortType) {
    var isIndividualResourceContainer =
        (   containerId !== FLOAT_GROUP_ID
         && containerId !== FLEX_GROUP_ID);
    var draggable = canDragEvents(user.institutionRole.id);
    var options = {
        plugins: ['resourceTimeline', 'interaction'],
        schedulerLicenseKey: 'GPL-My-Project-Is-Open-Source',
        now: pageDate,
        aspectRatio: 1,
        scrollTime: '00:00',
        slotDuration: '00:15',
        header: {
            left: '',
            center: '',
            right: ''
        },
        lazyFetching: false,
        slotWidth: '20',
        contentHeight: "auto",
        defaultView: 'resourceTimelineDay',
        loading: TRT.loadTemplateResourcesData_loading ,
        views: {
            resourceTimelineDay: {
                titleFormat: {day: "numeric"},
                columnHeaderFormat: {weekday: "long", month: "numeric", year: "numeric"}
            }
        },
        editable: isNotStudyStaff() && isIndividualResourceContainer,
        slotLabelFormat: {hour: "numeric", hour12: false, minute: "2-digit"},
        selectable: true,
        selectMirror: true,
        eventDrop: draggable ? TRT.loadTemplateResourcesData_eventDrop : null,
        eventResize: TRT.loadTemplateResourcesData_eventResize ,
        eventDragStart: draggable ? TRT.loadTemplateResourcesData_eventDragStart : null,
        eventMouseEnter: TRT.loadTemplateResourcesData_eventMouseover ,
        eventMouseLeave: TRT.loadTemplateResourcesData_eventMouseout ,
        eventClick: TRT.loadTemplateResourcesData_eventClick ,
        eventPositioned: TRT.loadTemplateResourcesData_eventAfterRender ,
        resourceAreaWidth: '30%',
        resourceColumns: [
            {
                labelText: 'Resource',
                field: 'resourceName',
                width: '150px',
                render: TRT.loadTemplateResourcesData_render
            },
            {
                labelText: 'Type',
                field: 'resourceType',
                width: '50px'
            }
        ],
        resources: resourceData,
        events: eventsData,
        eventOrder: "start",
        resourceOrder: sortType,
        eventRender: TRT.eventRender
    };

    var element = document.getElementById(containerId);

    switch (containerId) {
        case SINGLE_DAY_ID:
            singleDayTimeline = new FullCalendar.Calendar(element, options);
            singleDayTimeline.render();
            break;
        case MULTI_DAY_ID:
            multiDayTimeline = new FullCalendar.Calendar(element, options);
            multiDayTimeline.render();
            break;
        case FLOAT_GROUP_ID:
            floatGroupTimeline = new FullCalendar.Calendar(element, options);
            floatGroupTimeline.render();
            break;
        case FLEX_GROUP_ID:
            flexGroupTimeline = new FullCalendar.Calendar(element, options);
            flexGroupTimeline.render();
            break;
    }
}

function canDragEvents(role) {
    return role === SUPER_ADMIN|| role === FRONT_DESK || role === RESOURCE_MANAGER || role === SCHEDULER;
}

function onResourceMouseOut() {
    $('.resourceHoverText').remove();
}

function onResourceMouseEnter(event, obj, templateResourceId) {
    preventDefaultAction(event);
    daySelected = $(".pageInput").val();
    $('.appointmentHover').remove();
    $('.eventClickActions').remove();
    $.getJSON("rest/appointment/getGanttResourceInfo?visitId=" + app_selectedVisit.id
        + "&templateResourceId=" + templateResourceId + "&dayOffset=" + daySelected, function (data) {
        var totalDayInstances = data.totalRelevantTrStintsOfThisResource;
        var totalInstances = data.totalTrStintsOfThisResourceInVisit;
        var totalVisitTime = data.totalTimeOfStintsOfThisResourceInVisit;
        var totalDayTime = data.totalTimeOfRelevantStintsOfThisResource;

        var left = cumulativeLeftOffset(obj) + 30;
        var topHeight = cumulativeTopOffset(obj) + 10;
        var totalTimeHeader = isInpatientVisitType ? "Total Day Time" : "Total Time";
        var instancesHeader = isInpatientVisitType ? "Day Instances" : "Instances";
        var addMoreColumns = "";

        if (isInpatientVisitType) {
            addMoreColumns = " \<div style='display: flex;'><span class='appointmentHoverLabel' style='width: 100px;'>Total Visit Time</span> \
                                 <span class='appointmentHoverItem' style='width: 50px;'>" + convertMinutesToHrMins(totalVisitTime) + "</span></div>\
                                 <div style='display: flex;'><span class='appointmentHoverLabel' style='width: 100px;'>Visit Instances</span> \
                                 <span class='appointmentHoverItem' style='width: 50px;'>" + totalInstances + "</span></div>";
        }
        var tooltip = "<div id='resourceTooltip' class='resourceHoverText' style='z-index:10001; width: 160px; left:" + left + "px; top:" + topHeight + "px;'>\
                                 <div style='display: flex;'><span class='appointmentHoverLabel' style='width: 100px;'>" + totalTimeHeader + "</span>\
                                 <span class='appointmentHoverItem' style='width: 50px;'>" + convertMinutesToHrMins(totalDayTime) + "</span></div>\
                                 <div style='display: flex;'><span class='appointmentHoverLabel' style='width: 100px;'>" + instancesHeader + "</span>\
                                 <span class='appointmentHoverItem' style='width: 50px;'>" + totalDayInstances + "</span></div>"
            + addMoreColumns +
            "</div>";
        $("body").append(tooltip);

        var tooltipHeight = $('#resourceTooltip').height();
        $('.resourceHoverText').css('top', topHeight - tooltipHeight + 20);
    });
}

function cumulativeTopOffset(element) {
    var top = 0;
    do {
        top += element.offsetTop || 0;
        element = element.offsetParent;
    } while (element);

    return top;
};

function cumulativeLeftOffset(element) {
    var left = 0;
    do {
        left += element.offsetLeft || 0;
        element = element.offsetParent;
    } while (element);

    return left;
};

function closeAllHovers() {
    if (!isEventClick) {
        $('.eventClickActions').remove();
    }
    $('.resourceHoverText').remove();
    $('.appointmentHover').remove();
    isEventClick = false;
}

function convertMinutesToHrMins(mins) {
    var minutes = mins % 60;
    var hours = Math.floor(mins / 60);

    minutes = (minutes < 10 ? '0' : '') + minutes;
    hours = (hours < 10 ? '0' : '') + hours;

    return hours + ':' + minutes;
}

function toggleResourceTypeSelected() {
    disableFloatInput();
    resourceTypeSelected = $('input:radio[name=category]:checked').val();
    if (resourceTypeSelected == 'Float') {
        if (isInpatientVisitType) {
            $("#template_resource_float_start_day").spinner("option", "disabled", false);
            $("#template_resource_float_end_day").spinner("option", "disabled", false);
        }
        $("#template_resource_float_start_time").timepicker("option", "disabled", false);
        $("#template_resource_float_end_time").timepicker("option", "disabled", false);
        enableOverlayCss("template_resource_float_start_time");
        enableOverlayCss("template_resource_float_end_time");
    }
}

function toggleCategoryRadioButtons(disabled) {
    var radios = document.getElementsByName('category');
    for (var i = 0; i < radios.length; i++) {
        radios[i].disabled = disabled;
    }
}

function disableTemplateWidgets() {
    toggleCategoryRadioButtons(true);
    $("#template_resource_start_day").spinner("option", "disabled", true);
    $("#template_resource_end_day").spinner("option", "disabled", true);
    $("#template_resource_start_time").timepicker("option", "disabled", true);
    $("#template_resource_end_time").timepicker("option", "disabled", true);
    $("#template_resource_float_start_day").spinner("option", "disabled", true);
    $("#template_resource_float_end_day").spinner("option", "disabled", true);
    $("#template_resource_float_start_time").timepicker("option", "disabled", true);
    $("#template_resource_float_end_time").timepicker("option", "disabled", true);

    disableOverlayCss("template_resource_start_time");
    disableOverlayCss("template_resource_end_time");
    disableOverlayCss("template_resource_float_start_time");
    disableOverlayCss("template_resource_float_end_time");
}

function enableRegularTemplateWidgets() {
    toggleCategoryRadioButtons(false);
    if (isInpatientVisitType) {
        $("#template_resource_start_day").spinner("option", "disabled", false);
        $("#template_resource_end_day").spinner("option", "disabled", false);
    }
    else {
        $("#template_resource_start_day").spinner("option", "disabled", true);
        $("#template_resource_end_day").spinner("option", "disabled", true);
    }
    $("#template_resource_start_time").timepicker("option", "disabled", false);
    $("#template_resource_end_time").timepicker("option", "disabled", false);
    enableOverlayCss("template_resource_start_time");
    enableOverlayCss("template_resource_end_time");

    disableFloatInput();

    $("#allAnnotations").prop("checked", true);
    var responseData = [];
    createAnnotationsTable(responseData);
}

function disableOverlayCss(elementId) {
    var elementObj = document.getElementById(elementId);
    elementObj.style.color = "lightgray";
}

function enableOverlayCss(elementId) {
    var elementObj = document.getElementById(elementId);
    elementObj.style.color = "";
}

function disableFloatInput() {
    $("#template_resource_float_start_day").val(daySelected);
    $("#template_resource_float_end_day").val(daySelected);
    $('#template_resource_float_start_time').timepicker('setTime', "00:00");
    $('#template_resource_float_end_time').timepicker('setTime', "00:00");

    $("#template_resource_float_start_day").spinner("option", "disabled", true);
    $("#template_resource_float_end_day").spinner("option", "disabled", true);
    $("#template_resource_float_start_time").timepicker("option", "disabled", true);
    $("#template_resource_float_end_time").timepicker("option", "disabled", true);
    disableOverlayCss("template_resource_float_start_time");
    disableOverlayCss("template_resource_float_end_time");
}

function crudResourceClick(mode) {
    actionType = mode;
    $("#crud_template_resource").dialog({
        modal: true,
        height: 750,
        width: 900,
        resizable: false,
        create: function() {
            var resourceId = $('#wizard_overrideResources').val();
            ResourceAnnotationsTable.createResourceAnnotationsTable(resourceId);
        },
        open: function () {
            VisitTables.buildDisplayFilterSelect(ResourceAnnotationsTable.id, ResourceAnnotationsTable.filterOptions);
            ResourceAnnotationsTable.currentTable.clearTableBody();

            $('.formValidationRequired').css({visibility: "hidden"});
            $("#wizard_templateResources").val('');
            $('#templateResourceButtons input').css({width: "75px"});

            var dayInput = totalDays > 1 ? $(".pageInput").val() : 1;
            $(".pageInput").val(dayInput);

            if (mode == "add_template_resource") {
                $("#crud_template_resource").dialog('option', 'title', 'Add New Resource');

                document.getElementById("template_resource_alternate").checked = true;
                document.getElementById("template_resource_billable").checked = false;
                $("#template_resource_start_day").val(dayInput);
                $("#template_resource_end_day").val(dayInput);
                $("#template_resource_float_start_day").val(dayInput);
                $("#template_resource_float_end_day").val(dayInput);
                $('#template_resource_start_time').timepicker('setTime', "00:00");
                $('#template_resource_end_time').timepicker('setTime', "00:00");
                $("#template_resource_fixed").prop("checked", true);
            }
            else {
                if (mode == "edit_template_resource") {
                    $("#crud_template_resource").dialog('option', 'title', 'Edit Resource');
                }
                else {
                    $("#crud_template_resource").dialog('option', 'title', 'Copy Resource');
                }
                $.ajax({
                    url: "rest/appointment/getTemplateResourceDataWithAnnotations?templateResourceId=" + templateResourceId,
                    dataType: 'json',
                    success: function (data) {
                        var responseData = data.llaList;

                        app_selectedTemplateResource = data.templateResource;
                        var resourceId = app_selectedTemplateResource.resource.id;
                        ResourceAnnotationsTable.createResourceAnnotationsTable(resourceId);
                        ResourceAnnotationsTable.currentTable.refreshTableBody(responseData);
                        ResourceAnnotationsTable.selectResourceAnnotationsByRow(responseData);

                        $("#allAnnotations").prop("checked", true);
                        var billableVal = app_selectedTemplateResource.billable;
                        var floatableVal = app_selectedTemplateResource.floatable;

                        if (floatableVal == true) {
                            $("#template_resource_float").prop("checked", floatableVal);

                            toggleResourceTypeSelected();
                            var floatStartVal = app_selectedTemplateResource.floatStart;
                            var floatStartMinutes = floatStartVal % 60;
                            var floatStartDays = Math.floor(floatStartVal / 24 / 60);
                            var floatStartDate = templateResourceOriginDate.addDays(floatStartDays);
                            var floatStartHours = Math.floor(floatStartVal / 60 % 24);
                            floatStartDate.setHours(floatStartHours);
                            floatStartDate.setMinutes(floatStartMinutes);
                            var floatStartDay = daysBetween(templateResourceOriginDate, floatStartDate) + 1;
                            var floatEndVal = app_selectedTemplateResource.floatEnd;
                            var floatEndMinutes = floatEndVal % 60;
                            var floatEndDays = Math.floor(floatEndVal / 24 / 60);
                            var floatEndDate = templateResourceOriginDate.addDays(floatEndDays);
                            var floatEndHours = Math.floor(floatEndVal / 60 % 24);
                            floatEndDate.setHours(floatEndHours);
                            floatEndDate.setMinutes(floatEndMinutes);
                            var floatEndDay = daysBetween(templateResourceOriginDate, floatEndDate) + 1;
                            $("#template_resource_float_start_day").val(floatStartDay);
                            $('#template_resource_float_start_time').timepicker('setTime', showTime(floatStartDate));
                            $("#template_resource_float_end_day").val(floatEndDay);
                            $('#template_resource_float_end_time').timepicker('setTime', showTime(floatEndDate));
                        }

                        var flexibleVal = app_selectedTemplateResource.flexible;
                        if (!floatableVal && !flexibleVal) {
                            $("#template_resource_fixed").prop("checked", true);
                        }

                        var alternateVal = app_selectedTemplateResource.alternate;
                        document.getElementById("template_resource_billable").checked = billableVal;

                        $("#template_resource_flex").prop("checked", flexibleVal);
                        document.getElementById("template_resource_alternate").checked = alternateVal;

                        $("#wizard_templateResources").val(resourceId);

                        var startMinutes = app_selectedTemplateResource.startMinutes;
                        var startDay = minutesToDays(startMinutes) + 1;
                        $("#template_resource_start_day").val(startDay);
                        $("#template_resource_start_time").timepicker('setTime', showHoursAndMinutes(startMinutes));

                        var endMinutes = app_selectedTemplateResource.endMinutes;
                        var endDay = minutesToDays(endMinutes) + 1;
                        $("#template_resource_end_day").val(endDay);
                        $("#template_resource_end_time").timepicker('setTime', showHoursAndMinutes(endMinutes));

                        if (app_selectedTemplateResource.groupId != null && app_selectedTemplateResource.groupId != "") {
                            if (mode == "edit_template_resource") {
                                disableTemplateWidgets();
                            }
                            else {
                                toggleCategoryRadioButtons(false);
                            }
                        }
                        $.unblockUI();
                    }
                });
            }
        },
        close: function () {
            $.blockUI({fadeIn: 0});
            $('.formValidationRequired').css({visibility: "hidden"});
            enableRegularTemplateWidgets();
            $.unblockUI();
        }
    });
}

function daysBetween(first, second) {

    var second1 = new Date(second);
    // Copy date parts of the timestamps, discarding the time parts.
    var one = new Date(first.getFullYear(), first.getMonth(), first.getDate());
    var two = new Date(second1.getFullYear(), second1.getMonth(), second1.getDate());

    // Do the math.
    var millisecondsPerDay = 1000 * 60 * 60 * 24;
    var millisBetween = two.getTime() - one.getTime();
    var days = millisBetween / millisecondsPerDay;
    // Round down.
    return Math.round(days);
}

function minutesToDays(minutes) {
    return Math.floor(minutes / MINS_PER_DAY);
}

function minutesIntoDay(minutes) {
    return minutes - MINS_PER_DAY * minutesToDays(minutes);
}

function showTwoDigits(number) {
    return (number < 10) ?
        "0" + number :
        number;
}

function setTemplateApprovalStatus() {
    var imgSrc = approvedTemplate == "Approved" ? '<img src="css/images/gantt/approved.png"/> ' : '<img src="css/images/gantt/not_approved.png"/> ';
    $('#template_status').html(imgSrc + approvedTemplate);
    $('#visitgrid_finalApprovalStatus').text(approvedTemplate);
}

function postCrudTemplateResources(jsonData, onCompleteCallback) {
    $.post("rest/appointment/crudTemplateResources?actionType=" + actionType, {data: jsonData}, function (data) {
        var parsedData = JSON.parse(data);
        if (parsedData.result) {
            var confirmationMessage = "";
            if (actionType == "edit_template_resource") {
                confirmationMessage = "Template Resource Successfully updated.";
            }
            else if (actionType == "add_template_resource") {
                confirmationMessage = "Template Resource Successfully added.";
            }
            else if (actionType == "delete_template_resource") {
                confirmationMessage = "Template Resource Successfully deleted.";
            }
            else if (actionType == "copy_template_resource") {
                confirmationMessage = "Template Resource Successfully copied.";
            }
            util_showMainMessage(confirmationMessage);
            approvedTemplate = parsedData.infoMsg;
            setTimeout(function () {
                actionType != "delete_template_resource" ? $("#crud_template_resource").dialog("close") : "";
                actionType = "";
                reloadTemplateResourcesData();
            }, 1000);
        }
        else {
            if (actionType == "delete_template_resource") {
                if (parsedData.infoMsg == "resource is booked") {
                    util_showMainMessage(parsedData.errorMsg);
                }
                else {
                    confirmBeforeDeleteUnlinkingResource(parsedData.infoMsg, "Delete");
                }
            }
            else {
                util_showMainMessage(parsedData.errorMsg);
            }
        }
    }).always(function(){
        if(onCompleteCallback){
            onCompleteCallback();
        }
    });
}

function displayCrudConfirmationDialog(jsonData, dialog, onCompleteCallback) {
    var height = 300;
    var dialogName = dialog;

    if (dialog == "" || typeof dialog != "string") {
        height = 200;
        dialogName = "edit_template_resource_dialog_confirm";
    }

    $("#" + dialogName).css({display: 'inline-block'});
    $("#" + dialogName).dialog({
        resizable: false,
        height: height,
        width: 600,
        modal: true,
        close: function () {
            if (actionType == "drag_drop_events") {
                dialog();
            }
            if (actionType == "add_template_resource" || actionType == "edit_template_resource") {
                enableTemplateResourceSaveBtn();
            }
        },
        buttons: {
            "Continue": function () {
                if (actionType == "link_template_resource") {
                    linkTemplateResourcesConfirmation(jsonData);
                }
                else if (actionType == "unlink_template_resource") {
                    unlinkIndividualConfirmation();
                }
                else if (actionType == "copy_day_resources") {
                    copyDayConfirmation(onCompleteCallback);
                }
                else if (actionType == "unlink_group_template_resource") {
                    unlinkGroupResourcesConfirmation(jsonData, dialogName);
                }
                else if (actionType == "billable_selected") {
                    confirmChangeBillableResources(jsonData, isBillableViewSelected(), dialog);
                }
                else if (actionType == "drag_drop_events") {
                    dragResizeEventsConfirmation(jsonData, dialog);
                }
                else {
                    // actionType == "add_template_resource" || actionType == "edit_template_resource"
                    postCrudTemplateResources(jsonData, onCompleteCallback);
                }
                $(this).dialog("close");
            },
            "Cancel": function () {
                if (actionType == "add_template_resource" || actionType == "edit_template_resource") {
                    enableTemplateResourceSaveBtn();
                }
                $(this).dialog("close");
            }
        }
    });
}

function saveTemplateResource() {
    $('.formValidationRequired').css({visibility: "hidden"});
    var confirmationMessage = "";
    var resourceId = $("#wizard_templateResources").val();
    if (resourceId == "") {
        confirmationMessage = "Please select a resource.";
        showError('#template_wizard_formValidation', confirmationMessage);
        return;
    }

    var startValidationResult = $.isNumeric($("#template_resource_start_day").val());
    var endValidationResult = $.isNumeric($("#template_resource_end_day").val());
    var startDay = parseInt($("#template_resource_start_day").val());
    var endDay = parseInt($("#template_resource_end_day").val());

    if (isNaN(startDay) || startDay < 1 || endDay < 1 || isNaN(endDay)
        || !startValidationResult || !endValidationResult) {
        confirmationMessage = "Please check resource start and end times.";
        showError('#template_wizard_formValidation', confirmationMessage);
        return;
    }

    var startTimeVal = $("#template_resource_start_time").timepicker().val();
    var endTimeVal = $("#template_resource_end_time").timepicker().val();

    if (startDay > endDay
        || (startDay == endDay && endTimeVal <= startTimeVal)) {
        confirmationMessage = "Please check resource start and end times.";
        showError('#template_wizard_formValidation', confirmationMessage);
        return;
    }
    resourceTypeSelected = ($('input:radio[name=category]:checked').val());

    if (resourceTypeSelected == "Float") {
        var floatStartDay = parseInt($("#template_resource_float_start_day").val());
        var floatEndDay = parseInt($("#template_resource_float_end_day").val());
        var floatStartTimeVal = $("#template_resource_float_start_time").timepicker().val();
        var floatEndTimeVal = $("#template_resource_float_end_time").timepicker().val();
        var startValidationResult = $.isNumeric($("#template_resource_float_start_day").val());
        var endValidationResult = $.isNumeric($("#template_resource_float_end_day").val());

        if (isNaN(floatStartDay) || floatStartDay < 1 || floatEndDay < 1
            || isNaN(floatEndDay) || !startValidationResult || !endValidationResult) {
            confirmationMessage = "Please check float start and end times.";
            showError('#template_wizard_formValidation', confirmationMessage);
            return;
        }

        if (floatStartDay == startDay && floatEndDay == endDay &&
            floatEndTimeVal == endTimeVal && startTimeVal == floatStartTimeVal) {
            confirmationMessage = "Please check resource and float start and end times.";
            showError('#template_wizard_formValidation', confirmationMessage);
            return;
        }
        else if (floatStartDay > floatEndDay ||
            (floatStartDay == floatEndDay && floatEndTimeVal <= floatStartTimeVal)) {
            confirmationMessage = "Please check float start and end times.";
            showError('#template_wizard_formValidation', confirmationMessage);
            return;
        }
        else if (startDay < floatStartDay || endDay > floatEndDay ||
            (startDay == floatStartDay && startTimeVal < floatStartTimeVal) ||
            (endDay == floatEndDay && endTimeVal > floatEndTimeVal)) {

            confirmationMessage = "Please check resource and float start and end times.";
            showError('#template_wizard_formValidation', confirmationMessage);
            return;
        }
    }

    var billable = $('#template_resource_billable').is(':checked');
    var alternate = $('#template_resource_alternate').is(':checked');

    var startMinutes = convertDayAndTimeToMinutes(startDay, startTimeVal);
    var endMinutes = convertDayAndTimeToMinutes(endDay, endTimeVal)
    var floatStart = 0;
    var floatEnd = 0;

    if (resourceTypeSelected == "Float") {
        floatStart = convertTimeStringToMinutes(floatStartDay, floatStartTimeVal);
        floatEnd = convertTimeStringToMinutes(floatEndDay, floatEndTimeVal);
    }

    var selectedAnnotationsId = [];
    var selectedAnnotationsQuantity = [];
    var selectedAnnotationsComment = [];

    var selectedRowData = ResourceAnnotationsTable.currentTable.getDataForSelectedRows();
    $.each(selectedRowData, function (index, rowData) {

        if(rowData.length === 4) {
            var resourceId = rowData[0];
            selectedAnnotationsId.push(resourceId);

            var quantity = rowData[2];
            selectedAnnotationsQuantity.push(parseInt(quantity));

            var comment = rowData[3];
            selectedAnnotationsComment.push(comment);
        }
    });

    var jsonData = JSON.stringify({
        id: (app_selectedTemplateResource != null ? app_selectedTemplateResource.id : 0),
        visitTemplate: app_selectedVisit.id,
        resource: resourceId,
        alternate: alternate,
        billable: billable,
        flexible: resourceTypeSelected == "Flex" ? true : false,
        floatable: resourceTypeSelected == "Float" ? true : false,
        selectedAnnotations: selectedAnnotationsId,
        selectedAnnotationsQuantity: selectedAnnotationsQuantity,
        selectedAnnotationsComment: selectedAnnotationsComment,
        floatStart: resourceTypeSelected == "Float" ? floatStart : null,
        floatEnd: resourceTypeSelected == "Float" ? floatEnd : null,
        startMinutes: startMinutes,
        endMinutes: endMinutes
    });

    disableTemplateResourceSaveBtn();
    if (approvedTemplate == "Approved") {
        if (actionType == "edit_template_resource") {
            $.post("rest/appointment/crudTemplateResources?actionType=edit_template_resource_check_diff_only", {data: jsonData}, function (data) {
                var parsedData = JSON.parse(data);
                if (parsedData.result) {
                    displayCrudConfirmationDialog(jsonData, "", enableTemplateResourceSaveBtn);
                }
                else {
                    postCrudTemplateResources(jsonData, enableTemplateResourceSaveBtn);
                }
            });
        }
        else {
            displayCrudConfirmationDialog(jsonData, "", enableTemplateResourceSaveBtn);
        }
    }
    else {
        postCrudTemplateResources(jsonData, enableTemplateResourceSaveBtn);
    }
}

function disableTemplateResourceSaveBtn(){
    $("#saveTemplateResourceBtn") .prop("disabled", true);
}

function enableTemplateResourceSaveBtn(){
    $("#saveTemplateResourceBtn") .prop("disabled", false);
}

function convertTimeStringToMinutes(day, time) {
    var minutes = (day - 1) * MINS_PER_DAY;
    var index = time.split(":");

    var hours = parseInt(index[0], 10);
    var mins = parseInt(index[1], 10);

    return (minutes + (hours * MINS_PER_HOUR) + mins);
}

function convertDayAndTimeToMinutes(dayOffset, timeVal) {
    var index = timeVal.split(":");

    var hoursOffset = parseInt(index[0], 10);
    var minutesOffset = parseInt(index[1], 10);
    var minutes = (dayOffset - 1) * 1440 + hoursOffset * 60 + minutesOffset;

    return minutes;
}

function visitTemplateDateTimePickers() {
    $("#template_resource_start_day").spinner({min: 1});
    $("#template_resource_float_start_day").spinner({min: 1});
    WidgetUtil.createTimepicker('#template_resource_start_time', {
        display59: false,
        value: '00:00'
    });
    WidgetUtil.createTimepicker('#template_resource_float_start_time', {
        display59: false,
        value: '00:00'
    });
    $("#template_resource_end_day").spinner({min: 1});
    $("#template_resource_float_end_day").spinner({min: 1});
    WidgetUtil.createTimepicker('#template_resource_end_time', {
        display59: false,
        value: '00:00'
    });
    WidgetUtil.createTimepicker('#template_resource_float_end_time', {
        display59: false,
        value: '00:00'
    });
    $('#template_resource_float_start_time').timepicker('setTime', "00:00");
    $('#template_resource_float_end_time').timepicker('setTime', "00:00");
    enableRegularTemplateWidgets();
}

function cancelTemplateResourceClick() {
    $('#wizard_templateResources').val('');
    $("#crud_template_resource").dialog("close");
}

function getResourceStaticList() {
    $.get("rest/app/getStaticLists", {}, function (data) {
        var parsedData = JSON.parse(data);
        resources = parsedData.resources;
        resourcesSelectOptions = buildSelectOptions(resources, 'name', 'choose');
        $("#wizard_templateResources").html(resourcesSelectOptions);
    });
}

function editTemplateResourceConfirmDialog(event) {
    $.blockUI({fadeIn: 0});
    $('.eventClickActions').remove();
    crudResourceClick("edit_template_resource");
}

function deleteTemplateResourceConfirmDialog(event, isFlexGrouped, isFloatGrouped) {
    $('.eventClickActions').remove();
    actionType = "delete_template_resource";
    var jsonData = JSON.stringify({
        id: templateResourceId,
        visitTemplate: app_selectedVisit.id
    });
    if (approvedTemplate == "Approved") {
        if (isFlexGrouped) {
            checkIfResourcesCanStillBeLinkedOnDelete();
        }
        else {
            var msg = "<p>Editing this template will change the approval status.<br></p> " +
                "<p>Are you sure you want to delete this resource?<br></p>";
            if (isFloatGrouped) {
                msg += "<p>Note: The remaining resource(s) in this group will become unlinked.</p>";
            }
            $('#delete_template_resource_approval_dialog_confirm').html(msg);
            displayCrudConfirmationDialog(jsonData, "delete_template_resource_approval_dialog_confirm");
        }
    }
    else {
        var msg = "<p>Are you sure you want to delete this resource?<br></p>";
        if (isFlexGrouped) {
            // FIXME: This message sometimes does not get displayed b/c another dialog box
            // is opened instead of delete_template_resource_dialog_confirm. Need to implement
            // the correct logic to set the html of the dialog only when it will be displayed.
            $('#delete_template_resource_dialog_confirm').html(msg);
            checkIfResourcesCanStillBeLinkedOnDelete();
        }
        else {
            if (isFloatGrouped) {
                msg += "<p>Note: The remaining resource(s) in this group will become unlinked.</p>";
            }
            $('#delete_template_resource_dialog_confirm').html(msg);
            deleteConfirmationDialog(jsonData);
        }
    }
}

function deleteConfirmationDialog(jsonData) {
    $("#delete_template_resource_dialog_confirm").dialog({
        resizable: false,
        height: 220,
        width: 600,
        modal: true,
        buttons: {
            "Delete": function () {
                $(this).dialog("close");
                postCrudTemplateResources(jsonData);
            },
            "Cancel": function () {
                $(this).dialog("close");
            }
        }
    });
}

function checkIfResourcesCanStillBeLinkedOnDelete() {
    $.ajax({
        url: "rest/appointment/checkIfResourcesLinkableOnDeleteUnlink?templateResourceId=" + templateResourceId,
        dataType: 'json',
        success: function (data) {
            if (data.result) {
                var jsonData = JSON.stringify({
                    id: templateResourceId,
                    visitTemplate: app_selectedVisit.id
                });
                if (approvedTemplate == "Approved") {
                    var msg = '<p>Editing this template will change the approval status.<br></p> ' +
                        ' <p>Are you sure you want to delete this resource?</p>';
                    var dialogName = "delete_template_resource_approval_dialog_confirm";
                    // FIXME : should not have to clear a dialog box here. Should never be set in the first place.
                    // however I don't want to take the risk of refactoring that now, so I am just setting
                    // the html content to empty string
                    $('#delete_template_resource_dialog_confirm').html('');
                    $('#delete_template_resource_approval_dialog_confirm').html(msg);
                    displayCrudConfirmationDialog(jsonData, dialogName);
                }
                else {
                    deleteConfirmationDialog(jsonData);
                }
            }
            else {
                confirmBeforeDeleteUnlinkingResource(data.infoMsg, "Delete");
            }
        }
    });
}

function checkIfResourcesCanStillBeLinkedOnUnlink() {
    $.ajax({
        url: "rest/appointment/checkIfResourcesLinkableOnDeleteUnlink?templateResourceId=" + templateResourceId,
        dataType: 'json',
        success: function (data) {
            if (data.result) {
                if (data.errorMsg == "2") {
                    confirmBeforeDeleteUnlinkingResource(data.infoMsg, "Continue")
                }
                else {
                    if (approvedTemplate == "Approved") {
                        displayCrudConfirmationDialog();
                    }
                    else {
                        unlinkIndividualConfirmation();
                    }
                }
            }
            else {
                confirmBeforeDeleteUnlinkingResource(data.infoMsg, "Continue");
            }
        }
    });
}

function copyTemplateResourceConfirmDialog(event) {
    $.blockUI({fadeIn: 0});
    $('.eventClickActions').remove();
    crudResourceClick("copy_template_resource");
}

function openLinkResourcesDialog(isFloatableResource) {
    isLinkingFloat = isFloatableResource;
    $("#link_template_resources").dialog({
        modal: true,
        height: 500,
        width: 900,
        resizable: false
    });

    var displayResourceData = templateResourceObj.extendedProps.resourceName
        + " (" + templateResourceObj.extendedProps.displayStart + " - " +
        templateResourceObj.extendedProps.displayEnd + ") ";

    var headerText = "Link " + displayResourceData +
        " to the following checked resources " +
                 (isLinkingFloat ? "(select one)" : "(select up to two)");

    $('#resourceLinkingTableHeading').text(headerText);
    LRT.loadVisitsIntoWidget();
}

function cancelLinkTemplateResourceClick() {
    $("#link_template_resources").dialog("close");
}

function linkTemplateResourceConfirmDialog() {
    // slice to get copy, vs reference
    var linkResourcesIds = LRT.currentTable.getSelectedRows().slice(0);

    var selectedResourcesLength = linkResourcesIds.length;
    if (selectedResourcesLength === 0) {
        util_showMainMessage("Please select at least one resource to link.");
        return;
    }
    else if (isLinkingFloat && selectedResourcesLength > 1) {
        util_showMainMessage("A maximum of two resources can be linked in a float group.");
        return;
    }
    else if (!isLinkingFloat && selectedResourcesLength > 2) {
        util_showMainMessage("A maximum of three resources can be linked in a flex group.");
        return;
    }

    pushIfNotDup(templateResourceId, linkResourcesIds);

    var jsonData = JSON.stringify({
        linkResources: linkResourcesIds
    });
    actionType = "link_template_resource";
    if (approvedTemplate === "Approved") {
        displayCrudConfirmationDialog(jsonData, "");
    }
    else {
        linkTemplateResourcesConfirmation(jsonData);
    }

}

function linkTemplateResourcesConfirmation(jsonData) {
    $.post("rest/appointment/linkResources", {data: jsonData}, function (data) {
        var parsedData = JSON.parse(data);
        if (parsedData.result) {
            actionType = "";
            $("#link_template_resources").dialog("close");
            util_showMainMessage("Linking Resources Successful.");
            approvedTemplate = "Not Approved";
            setTimeout(function () {
                reloadTemplateResourcesData();
            }, 1000);
        }
        else {
            util_showMainMessage(parsedData.errorMsg);
        }
    });
}

function unlinkIndividualResource() {
    actionType = "unlink_template_resource";
    checkIfResourcesCanStillBeLinkedOnUnlink();
}

function reloadTemplateResourcesData() {
    setTemplateApprovalStatus();
    daySelected = $(".pageInput").val();
    $(".pageInput").val(daySelected);
    var pageDate = templateResourceOriginDate.addDays(daySelected - 1);
    timelinePagination(pageDate);
}

function postLoadDataOnIndividualUnlink() {
    actionType = "";
    util_showMainMessage("UnLinking Resource(s) Successful.");
    approvedTemplate = "Not Approved";
    setTimeout(function () {
        reloadTemplateResourcesData();
    }, 1000);
}

function confirmBeforeDeleteUnlinkingResource(groupId, buttonName) {
    var dialog_buttons = {};
    dialog_buttons[buttonName] = function () {
        unlinkGroupResourcesConfirmation(groupId, "unlink-individual-template-resource-dialog-confirm");
        $(".ui-dialog-content").dialog("close");
    };
    dialog_buttons['Cancel'] = function () {
        $(this).dialog('close');
    };

    // FIXME: below is a cheap and dirty way to reset the HMTL in the
    // delete_template_resource_dialog_confirm because it gets set to some
    // other message in the upstream code. Ideally it should never get set for this
    // use case since it will never be displayed.
    $("#delete_template_resource_dialog_confirm").html('');
    $("#unlink-delete-individual-template-resource-dialog-confirm").dialog({
        resizable: false,
        height: 250,
        width: 535,
        modal: true,
        open: function () {
            var msg = "";
            if (approvedTemplate == "Approved") {
                msg += "<p>Editing this template will change the approval status.</p>";
            }
            else {
                if (actionType == "unlink_template_resource") {
                    msg += "<p>Are you sure you want to unlink this resource? </p>";
                }
            }
            if (actionType == "unlink_template_resource") {
                msg += "If you continue, all resources in this group will become unlinked.";
            }
            else {
                msg += "Are you sure you want to delete this resource? <br><br> Note: The remaining resource(s) in this group will become unlinked.";
            }
            $("#individualUnlinkDeleteMessage").html(msg);
        },
        buttons: dialog_buttons
    });
}

function unlinkIndividualConfirmation() {
    $.ajax({
        url: "rest/appointment/unlinkOneResource?templateResourceId=" + templateResourceId,
        dataType: 'json',
        success: function (data) {
            if (!data.result) {
                confirmBeforeDeleteUnlinkingResource(data.infoMsg, "Continue");
            }
            else {
                postLoadDataOnIndividualUnlink();
            }
        }
    });
}

function toggleGanttView(obj, ganttId, viewParam) {
    var toggleTitle = "";
    if (obj.title == "Collapse") {
        $('#no_' + viewParam + '_resources').css({display: 'none'});
        $('#' + ganttId).css({display: 'none'});
        toggleTitle = "Expand";
        obj.title = toggleTitle;
        document.getElementById(ganttId + "Img").src = "css/images/gantt/expand_icon.png";
    }
    else {
        toggleTitle = "Collapse";
        obj.title = toggleTitle;
        document.getElementById(ganttId + "Img").src = "css/images/gantt/collapse_icon.jpg";
        daySelected = $(".pageInput").val();
        $('#' + ganttId).css({display: 'block'});

        var timeline;
        switch (ganttId) {
            case SINGLE_DAY_ID:
                timeline = singleDayTimeline;
                break;
            case MULTI_DAY_ID:
                timeline = multiDayTimeline;
                break;
            case FLOAT_GROUP_ID:
                timeline = floatGroupTimeline;
                break;
            case FLEX_GROUP_ID:
                timeline = flexGroupTimeline;
                break;
        }
        // timeline is undefined if this function is called before the timelines are setup.
        if (timeline) {
            timeline.getEvents().length > 0 ?
                $('#no_' + viewParam + '_resources').css({display: 'none'}) :
                $('#no_' + viewParam + '_resources').css({display: 'block'});
        }
    }
}

function buildFromCopyDaySelectOptions() {
    var html = [];
    for (var i = 1, len = totalDays; i <= len; i++) {
        html[html.length] = "<option value='" + i + "'>" + 'Day ' + i + "</option>";
    }
    $("#fromCopyDaySelect").html(html);
}

TRT.copyDayCopyableSuccess = function (data) {
    if (data) {
        if (approvedTemplate == "Approved") {
            var jsonData = [];
            displayCrudConfirmationDialog(jsonData, "", enableCopyDayBtn);
        }
        else {
            copyDayConfirmation(enableCopyDayBtn);
        }
    }
    else {
        var confirmationMessage = "Selected copy day has no single day resources " +
                                        "that can be copied.";
        showError('#copy_day_formValidation', confirmationMessage);
        enableCopyDayBtn();
    }
};
TRT.copyDayClick = function () {
    disableCopyDayBtn();

    actionType = "copy_day_resources";
    $('.formValidationRequired').css({visibility: "hidden"});
    var currentDay = $.trim($("#fromCopyDaySelect").val());

    var selectedCopyToValues = $("#toCopyDaySelect").multipleSelect('getSelects').join(",");
    if (selectedCopyToValues.length == 0) {
        var confirmationMessage = "Please select at least one destination day.";
        showError('#copy_day_formValidation', confirmationMessage);
        enableCopyDayBtn();
    }
    else {
        $.ajax({
            url: "rest/appointment/isDayCopyable?visitId=" + app_selectedVisit.id
            + "&currentDay=" + currentDay,
            dataType: 'json',
            success: TRT.copyDayCopyableSuccess
        })
            .fail(function () {
                enableCopyDayBtn();
            });
    }
};

function copyDay() {
    buildFromCopyDaySelectOptions();
    $("#fromCopyDaySelect").val(daySelected);
    buildCopyToMultiSelectOptions();
    $('#toCopyDaySelect').multipleSelect('disable');
    fromCopyDayChangeClick();
    $("#copy-day-template-resource-dialog").css({display: 'block'});
    $("#copy-day-template-resource-dialog").dialog({
        resizable: false,
        height: 325,
        width: 510,
        modal: true,
        close: function () {
            $('.formValidationRequired').css({visibility: "hidden"});
        },
        buttons: [
            {
                id: "copyDayBtn",
                text: "Copy",
                click: TRT.copyDayClick
            },
            {
                text: "Cancel",
                click: function () {
                    $(this).dialog("close");
                }
            }
        ]
    });
}

function disableCopyDayBtn(){
    $("#copyDayBtn").prop("disabled", true).addClass( 'ui-state-disabled' );
}

function enableCopyDayBtn(){
    $("#copyDayBtn").prop("disabled", false).removeClass( 'ui-state-disabled' );
}

function copyDayConfirmation(onCompleteCallback) {
    var currentDay = $.trim($("#fromCopyDaySelect").val());
    var selectedCopyToValues = $("#toCopyDaySelect").multipleSelect('getSelects').join(",");
    $.ajax({
        url: "rest/appointment/copyDayInpatientResources?visitId=" + app_selectedVisit.id
        + "&currentDay=" + currentDay + "&targetDaysCommaString=" + selectedCopyToValues,
        dataType: 'json',
        success: function (data) {
            actionType = "";
            approvedTemplate = "Not Approved";
            util_showMainMessage("Resource(s) successfully copied", onCompleteCallback);
            $("#copy-day-template-resource-dialog").dialog("close");
            setTimeout(function () {
                reloadTemplateResourcesData();
            }, 1000);
        }
    }).fail(function(){
        if(onCompleteCallback){
            onCompleteCallback();
        }
    });
}

function fromCopyDayChangeClick() {
    var selectedFromCopyDayVal = $("#fromCopyDaySelect").val();
    selectedFromCopyDayVal == "" ? $('#toCopyDaySelect').multipleSelect('disable') : $('#toCopyDaySelect').multipleSelect('enable');
    $(".ms-parent .ms-drop ul").remove();
    var $select = $('#toCopyDaySelect');
    $('#toCopyDaySelect option').each(function () {
        var val = $(this).attr('value');
        if (val > totalDays) {
            $("#toCopyDaySelect option[value='" + val + "']").remove();
        }
    });
    for (var i = 1, len = totalDays; i <= len; i++) {
        $("#toCopyDaySelect option[value='" + i + "']").remove();
        if (selectedFromCopyDayVal != i) {
            var value = $.trim(selectedFromCopyDayVal),
                $opt = $("<option />", {
                    value: i,
                    text: 'Day ' + i
                });

            $select.append($opt);
        }
    }
    $select.multipleSelect("refresh");
}

function buildCopyToMultiSelectOptions() {
    $('#toCopyDaySelect').multipleSelect({
        placeholder: "choose",
        width: "80%",
        allSelected: "All Selected",
        maxHeight: 150,
        minimumCountSelected: 3
    });
    $('#toCopyDaySelect').multipleSelect('setSelects', []);
}

function unlinkGroupResources(groupId, groupResourceString) {
    actionType = "unlink_group_template_resource";
    if (approvedTemplate == "Approved") {
        $("#unlink-template-resource-group-approval-dialog-confirm").css({display: 'inline-block'});
        $('.unlinkGroupedResources').html(groupResourceString.replace(/;/g, "<br>"));
        displayCrudConfirmationDialog(groupId, "unlink-template-resource-group-approval-dialog-confirm");
    } else {
        $("#unlink-template-resource-group-dialog-confirm").css({display: 'inline-block'});
        $('.unlinkGroupedResources').html(groupResourceString.replace(/;/g, "<br>"));
        $("#unlink-template-resource-group-dialog-confirm").dialog({
            resizable: false,
            height: 250,
            width: 600,
            modal: true,
            buttons: {
                "Unlink": function () {
                    unlinkGroupResourcesConfirmation(groupId, "unlink-template-resource-group-dialog-confirm");
                },
                "Cancel": function () {
                    $(this).dialog("close");
                    return;
                }
            }
        });
    }
}

// MAJOR ISSUE:
// If the group was unlinked before attempting to delete a resource in the group,
// and the deletion fails because that resource is used in a booked visit,
// the un-linking will not be undone
function unlinkGroupResourcesConfirmation(groupId, dialogName) {
    $.ajax({
        url: "rest/appointment/unlinkGroup?groupId=" + groupId,
        dataType: 'json',
        success: function (data) {
            if (actionType != "delete_template_resource") {
                actionType = "";
                $("#" + dialogName).dialog("close");
                approvedTemplate = "Not Approved";
                reloadTemplateResourcesData();
            }
            else if (actionType == "delete_template_resource") {
                var jsonData = JSON.stringify({
                    id: templateResourceId,
                    visitTemplate: app_selectedVisit.id
                });
                postCrudTemplateResources(jsonData);
            }
        }
    });
}

function sortTemplateResources(obj) {
    destroyTimelines();
    templateResourcesSortBy = obj.value;
    setSortTemplateResourceDropDownValues();
    var pageDate = templateResourceOriginDate.addDays(daySelected - 1);
    loadTimelineEvents(daySelected, pageDate, templateResourcesSortBy);
}

function setSortTemplateResourceDropDownValues() {
    document.getElementById("topSortTemplateResourcesDropDown").value = templateResourcesSortBy;
    document.getElementById("bottomSortTemplateResourcesDropDown").value = templateResourcesSortBy;
    $("select.fix-me")
        .each(function () {
            $(this).data("origWidth", $(this).outerWidth()); // IE 8 will take padding on selects
            $(this).css("width", "auto");
        });
}
