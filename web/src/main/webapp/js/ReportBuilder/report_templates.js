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
var displayOutput = '';
var noReportMsg = 'Feature Coming Soon.';

var selectedReportId;
var selectedUserReportId;
var selectedReportType;
var selectedReportName;

var selectedSortDropDownIndex;
var selectedFilterDropDownIndex;

var categoryLevelId = 0;
var leftFieldOutput = [];
var rightFieldOutput = [];
var fieldListSize = 0;
var selectedStartDate = '';
var selectedEndDate = '';
var isDateBoundedReport = false;
var level1Height = 430;
var level2Height = 350;

var sortList = [];
var filterList = [];
var sortSelectOptions;
var filterSelectOptions;
var previousSelectedCustomReport = "";

var filterListMap = {};
var filterSelectedListMap = {};
var filterSelectedIds = [];
var sortSelectedIds = [];
var sortSelectedListMap = {};
var trackChangesMade = false;
var selectedFields = [];
var selectedTerms = "";

var reportBuilderPage =(function () {
    var initFn = function () {
        loadMetaHeaders();
        commonData();
        renderBreadcrumbs("report_builder_screen");
        app_runIdleTimer();
        getBrowserVersion();
        loadAllReportTemplates();
        myReady();
        initFooter();
    };
    return {
        init: initFn
    };
}());


function onloadReportBuilderPage() {
    PageLoadUtil.loadPage(".reportslink", reportBuilderPage.init, false);
}

function createReportTemplateDatePickers() {
    WidgetUtil.createDatepicker("#reportStartDate", {
        onSelect: function (selectedDate) {
            $("#reportEndDate").datepicker("option", "minDate", selectedDate);
        },
        onClose: function (dateText, inst) {
            try {
                var selectedDate = $.datepicker.parseDate('mm/dd/yy', dateText);
                $("#reportEndDate").datepicker("option", "minDate", selectedDate);
            } catch (e) {
                alert("Incorrect Date format. It should be MM/DD/YYYY.");
                $("#reportStartDate").val('');
                return;
            }
        }
    });

    WidgetUtil.createDatepicker("#reportEndDate", {
        onClose: function (dateText, inst) {
            try {
                $.datepicker.parseDate('mm/dd/yy', dateText);
            } catch (e) {
                alert("Incorrect Date format. It should be MM/DD/YYYY.");
                $("#reportEndDate").val('');
            }
        }
    });
    var startDate = new Date();
    startDate.setDate(1);
    startDate.setMonth(startDate.getMonth() - 1);
    var endDate = new Date(); // current date
    endDate.setDate(1); // going to 1st of the month
    endDate.setHours(-1);
    $("#reportStartDate").datepicker('setDate', startDate);
    $("#reportEndDate").datepicker('setDate', endDate);
    $("#reportEndDate").datepicker("option", "minDate", startDate);
    $('#ui-datepicker-div').hide();
}

function createEachReportTemplateElement(title, viewUserReportId, viewReportType, viewReportId, viewReportUpdateTime, viewReportName, viewReportBase) {
    return '<li style="width: 300px;" title="' + title + '"> ' +
        '<span class="childNodes"> <span class="custom-radio"> ' +
        '<input type="radio" id="' + viewUserReportId + "" + viewReportType + '" name="reports" ' +
        'onclick="loadCategories(this,' + viewReportId + ',' + viewUserReportId + ',\'' + viewReportUpdateTime + '\', \'' + viewReportName.replace(/'/g, "\\'").replace(/;/g, "\\;") + '\',\'' + viewReportType + '\',\'' + viewReportBase + '\')"/> ' +
        '</span><label style="cursor: pointer;" class="breakWords" id="' + viewUserReportId + "_" + viewReportType + '" ' +
        'for="' + viewUserReportId + "" + viewReportType + '">' + viewReportName + '</label></span></li>';
}

function loadAllReportTemplates() {
    $.ajax({
        url: 'rest/reports/templates',
        type: 'GET',
        dataType: 'json',
        data: '',
        success: function (data) {
            var customDisplayOutput = '';
            var adminDisplayOutput = '';
            var operationalDisplayOutput = '';
            $.each(data, function (key, val) {
                var displayOutput = '';
                var viewReportName = val.reportTemplateName;
                var viewReportId = val.reportTemplateId;
                var viewUserReportId = val.id;
                var viewReportType = val.reportTemplateType;
                var viewReportUpdateTime = val.latestUpdate;
                var viewReportBase = val.reportTemplateBase;
                var title = "Display default field selections";
                if (viewReportUpdateTime != undefined) {
                    viewReportType = 'Custom';
                    title = "";
                }
                displayOutput += createEachReportTemplateElement(title, viewUserReportId, viewReportType, viewReportId, viewReportUpdateTime, viewReportName, viewReportBase);

                if (viewReportType == 'Custom') {
                    customDisplayOutput += displayOutput;
                }
                else if (viewReportType == 'Administrative') {
                    adminDisplayOutput += displayOutput;
                }
                else if (viewReportType == 'Operational') {
                    operationalDisplayOutput += displayOutput;
                }
            });

            $('#selectedCustomList').html("");

            if (customDisplayOutput == '') {
                $('#CustomList').attr("class", 'notAvailableReports');
            }
            else {
                $('#CustomList').html(customDisplayOutput);
            }

            var numItems = $("#CustomList li").length;
            $('#CustomList').attr("class", '');
            if (numItems >= 5) {
                $('#CustomList').attr("class", 'borderList');
            }

            if (adminDisplayOutput == '') {
                $('#AdministrativeList').html(noReportMsg);
                $('#AdministrativeList').attr("class", 'notAvailableReports');
            }
            else {
                $('#AdministrativeList').html(adminDisplayOutput);
            }

            if (operationalDisplayOutput == '') {
                $('#OperationalList').html(noReportMsg);
                $('#OperationalList').attr("class", 'notAvailableReports');
            }
            else {
                $('#OperationalList').html(operationalDisplayOutput);
            }
        },
        error: function (xhr, status, error) {
            alert("There was a problem with the report. Please select different parameters for the report.");
        }
    });
}

function adjustLevel1CategoryHeight() {
    var categoryLevel1 = $(".parentNode" + ".1");
    var previousHeight = 0;
    for (var i = 0; i < categoryLevel1.length; i++) {
        var object = categoryLevel1[i];
        var child = $(object).parent('li.parent_li');
        var childHeight = child.height;
        if (childHeight > previousHeight) {
            level2Height = childHeight;
            previousHeight = childHeight;
        }
    }
    for (var i = 0; i < categoryLevel1.length; i++) {
        var object = categoryLevel1[i];
        $(object).parent('li.parent_li').css({height: level2Height});
    }
}

function selectRadioButton() {
    var radioButton = $('input[name="reports"]');
    $(radioButton).each(function () {
        if ($(this).is(':checked')) {
            var parentNode = $(this).parent();
            parentNode.addClass("selected");
            parentNode.parent().css({"color": "#b24407", "font-weight": "bold"});
        }
        else {
            var parentNode = $(this).parent();
            parentNode.removeClass("selected");
            parentNode.parent().css({"color": "#000000", "font-weight": "300"});
        }
    });
}

function loadCategoriesMetadata(reportId, reportType, userReportId, reportName, obj, reportBase, lastUpdatedTime) {
    resetToggleCategories();
    $('#selectedCustomList').html("");
    if (reportType != 'Custom') {
        previousSelectedCustomReport = "";
        sortSavedReportList(0);
    }
    selectedReportId = reportId;
    selectedReportType = reportType;
    selectedUserReportId = userReportId;
    selectedReportName = reportName;
    $('#reportTemplateMetadata').html('');
    $('#selectedReportName').html('');
    var children = $(obj).parent('li.parent_li').find(' > ul > li');
    selectRadioButton();
    var reportInfoVal = "Report Type: " + reportBase + "\nLast Saved: " + lastUpdatedTime;
    document.getElementById("reportTopInfo").title = reportInfoVal;
    document.getElementById("reportBottomInfo").title = reportInfoVal;
    $('#selectedReportName').html(selectedReportName);
    var categoryUrl = 'rest/reports/templates/' + selectedReportId;
    $('#editReportName').css({display: 'none'});
    $('#CustomList').append(previousSelectedCustomReport);

    if (selectedReportType == 'Custom') {
        var objParent = obj.parentNode;
        var outerParent = objParent.parentNode;
        var outermostParent = outerParent.parentNode;
        var parent = outermostParent.parentNode;
        $('#selectedCustomList').html(outermostParent.outerHTML);
        if (previousSelectedCustomReport == "") {
            $('#selectedReportName').html(outermostParent.innerText);
        }
        var child = $(outermostParent).find(' > span ');
        child.css({"color": "#000000", "font-weight": "300"});
        var child1 = $(outermostParent).find(' > span > span ');
        child1.removeClass("selected");
        var child2 = $(outermostParent).find(' > span > span > input ');
        document.getElementById(child2[0].id).removeAttribute("onclick");
        previousSelectedCustomReport = outermostParent;
        parent.removeChild(outermostParent);
        sortSavedReportList(selectedUserReportId);
        categoryUrl = 'rest/reports/templates/users/' + selectedUserReportId;
        $('#editReportName').css({display: 'inline'});
    }

    $.ajax({
        url: categoryUrl,
        type: 'GET',
        dataType: 'json',
        data: '',
        success: function (data) {
            $('#dateRangeContainer').css({display: 'none'});
            $('.sub_body').css({display: 'inline-block'});
            displayAllActionBlocks();
            sortList = [];
            filterList = [];
            displayOutput = '<div class="tree"> ';
            isDateBoundedReport = false;
            level1Height = 430;
            level2Height = 350;
            if (data.dateBounded) {
                $('#dateRangeContainer').css({display: 'block'});
                createReportTemplateDatePickers();
                isDateBoundedReport = true;
            }
            $.each(data.categories, function (key, val) {
                if (key == 0) {
                    displayOutput += '<ul style="padding-left: 15px;">';
                }
                else {
                    displayOutput += '<ul>';
                }
                buildReportTemplateMetadataTree(val, 0);
                displayOutput += '</ul>';
            });

            resetDropDowns(sortList, filterList);

            if (selectedReportType == 'Custom') {
                $.each(data.sortDTOList, function (key, val) {
                    var listName = "sort_list_" + (key + 1);
                    selectDropDownElement(listName, val);
                    showAllByTag(listName);
                    var sortDirectionVal = data.sortDirectionList[key];
                    var checkedRadioId = (listName + "_" + sortDirectionVal);
                    document.getElementById(checkedRadioId).checked = true;
                });

                $.each(data.filterDTOList, function (key, val) {
                    var id = (key + 1);
                    var listName = "filter_list_" + id;
                    selectDropDownElement(listName, val);
                    //based on type create element and then assign value to the element
                    var filterDirectionVal = data.filterExpressionDTOList[key];
                    createElementsBasedOnFieldType(id, "", filterDirectionVal);
                });
            }
            else {
                $('#editReportName').css({display: 'none'});
            }

            $('#reportTemplateMetadata').html(displayOutput);

            children.show('fast');
            highlightFields();
            adjustLevel1CategoryHeight();
        }
    });
}

function loadCategories(obj, reportId, userReportId, lastUpdatedTime, reportName, reportType, reportBase) {
    if (trackChangesMade) {
        return confirmNavigateDialog(reportId, reportType, userReportId, reportName, obj, reportBase, lastUpdatedTime);
    }
    loadCategoriesMetadata(reportId, reportType, userReportId, reportName, obj, reportBase, lastUpdatedTime);
}

function selectDropDownElement(listName, valueToSelect) {
    var element = document.getElementById(listName);
    element.value = valueToSelect;
}

function buildSortFilterSelectOptions(list, optionTextProperty, choiceLabel) {
    var html = [];
    if (choiceLabel !== null) {
        html[html.length] = "<option value=''>" + choiceLabel + "</option>";
    }
    for (var i = 0, len = list.length; i < len; i++) {
        html[html.length] = "<option value='";
        html[html.length] = list[i].id;
        html[html.length] = "'>";
        html[html.length] = list[i][optionTextProperty] + " (" + list[i].categoryDisplayName + ")";
        html[html.length] = "</option>";
    }
    return html.join('');
}

function isEven(n) {
    return n % 2 == 0;
}

function getFieldListSize(val, addOneForSelectAll) {
    var totalLength = val.templateCategoryFieldList.length + addOneForSelectAll;

    return totalLength > 10 ?
        (isEven(totalLength)
            ? totalLength / 2 : (totalLength / 2) + 0.5)
        : totalLength;
}

function convertFieldsIntoTwoColumns(val, maybeAddOne) {
    var addOneOffset = maybeAddOne ? 1 : 0;

    $.each(val.templateCategoryFieldList, function (key, val) {

        if (key + addOneOffset < fieldListSize) {
            leftFieldOutput.push(val);
        }
        else {
            rightFieldOutput.push(val);
        }
    });
}

function mergeFields(selectedFieldClass, displayRightColumnAsLeft) {
    for (var i = 0; i < fieldListSize; i++) {
        var rightFieldObj = rightFieldOutput[i];
        var leftFieldObj = leftFieldOutput[i];

        if (displayRightColumnAsLeft) {
            if (rightFieldObj != undefined) {
                selectedFieldClass = "childNodes";
                if (rightFieldObj.selected) {
                    if (rightFieldObj.sortable) {
                        sortList.push(rightFieldObj);
                    }
                    if (rightFieldObj.filterable) {
                        filterList.push(rightFieldObj);
                        filterListMap[rightFieldObj.id] = rightFieldObj.fieldType;
                    }
                    selectedFieldClass = "selectedChildNode";
                }
                displayOutput += '  <li class="' + selectedFieldClass + '" title="' + rightFieldObj.categoryDisplayName + '" id="' + rightFieldObj.id + '" onclick="selectChildNode(event,this,' + rightFieldObj.sortable + ',\'' + rightFieldObj.fieldType + '\');">' + rightFieldObj.displayName + ' </li> ';
            }
        }

        // might be undefined if i==0 and that spot is occupied by 'select all'
        if (leftFieldObj) {
            selectedFieldClass = "childNodes";
            if (leftFieldObj.selected) {
                if (leftFieldObj.sortable) {
                    sortList.push(leftFieldObj);
                }
                if (leftFieldObj.filterable) {
                    filterList.push(leftFieldObj);
                    filterListMap[leftFieldObj.id] = leftFieldObj.fieldType;
                }
                selectedFieldClass = "selectedChildNode";
            }

            displayOutput += '  <li class="' + selectedFieldClass + '" title="' + leftFieldObj.categoryDisplayName + '" id="' + leftFieldObj.id + '" onclick="selectChildNode(event,this,' + leftFieldObj.sortable + ',\'' + leftFieldObj.fieldType + '\');">' + leftFieldObj.displayName + ' </li> ';
        }

        if (!displayRightColumnAsLeft) {
            if (rightFieldObj != undefined) {
                selectedFieldClass = "childNodes";
                if (rightFieldObj.selected) {
                    if (rightFieldObj.sortable) {
                        sortList.push(rightFieldObj);
                    }
                    if (rightFieldObj.filterable) {
                        filterList.push(rightFieldObj);
                        filterListMap[rightFieldObj.id] = rightFieldObj.fieldType;
                    }
                    selectedFieldClass = "selectedChildNode";
                }
                displayOutput += '  <li class="' + selectedFieldClass + '" title="' + rightFieldObj.categoryDisplayName + '" id="' + rightFieldObj.id + '" onclick="selectChildNode(event,this,' + rightFieldObj.sortable + ',\'' + rightFieldObj.fieldType + '\');">' + rightFieldObj.displayName + ' </li> ';
            }
        }
    }
    return selectedFieldClass;
}

function createCategoriesHeader(val, displayClassName, categoryLevelId, currentCategoryHeight) {
    var categoryHeight = "";
    if (categoryLevelId == 0) {
        if (currentCategoryHeight > level1Height) {
            categoryHeight = "height: " + currentCategoryHeight + "px";
            level1Height = currentCategoryHeight;
        }
        else {
            categoryHeight = "height: " + level1Height + "px";
        }
    }
    else if (categoryLevelId == 1) {
        if (currentCategoryHeight > level2Height) {
            categoryHeight = "height: " + currentCategoryHeight + "px";
            level2Height = currentCategoryHeight;
        }
        else {
            categoryHeight = "height: " + level2Height + "px";
        }
    }
    else if (categoryLevelId == 2) {
        categoryHeight = "min-height: 100px";
    }

    displayOutput += ' <li class="parent_li" style="' + categoryHeight + '"> <span title="Collapse" class="parentNode ' + categoryLevelId + '" onclick="toggleCategories(event, this);">' +
        '    <img src="css/images/sm_circle_minus.png" class="parentHeaderImages"/>' + val.displayName + ' </span> ' +
        '    <ul class="' + displayClassName + '"> ';
}

function resetFieldsAndSize(val, addOneForSelectAll) {
    leftFieldOutput = [];
    rightFieldOutput = [];
    fieldListSize = getFieldListSize(val, addOneForSelectAll);
}

function buildReportTemplateMetadataTree(val, level) {
    var selectedFieldClass = "childNodes";
    var displayClassName = "one-col-special";

    var fieldLength = val.templateCategoryFieldList.length;
    var categoryHeight = fieldLength * 42;
    if (fieldLength > 10) {
        displayClassName = "two-col-special";
        categoryHeight = (Math.ceil(fieldLength / 2) + 1) * 33;
    }

    resetFieldsAndSize(val, 0);

    if (level == 0) {
        categoryLevelId = 0;

        createCategoriesHeader(val, displayClassName, categoryLevelId, categoryHeight);

        convertFieldsIntoTwoColumns(val);

        selectedFieldClass = mergeFields(selectedFieldClass, false);

        displayOutput += ' </ul> </li>';
    }
    else if (level == 1) {
        categoryLevelId++;
        if (fieldLength > 10) {
            categoryHeight = (Math.ceil(fieldLength / 2) + 1) * 35;
        }
        createCategoriesHeader(val, displayClassName, categoryLevelId, categoryHeight);

        convertFieldsIntoTwoColumns(val);

        selectedFieldClass = mergeFields(selectedFieldClass, false);

        $.each(val.subCategories, function (key, val) {

            var rowId = val.id;

            displayOutput += '  <li class="childNodes" onclick="displayRelatedFields(event, this);" id="row' + rowId + '"> ' + val.displayName + ' <span class="studyStaffMenu"> <img src="css/images/btn_arrow.png"/> ' +
                ' <div tabindex="0" class="onclick-menu"> <ul class="onclick-menu-content-hide" id="row' + rowId + '"> <img class="close-image" onclick="closeAllOpenMenus(event);" src="css/images/close_image.png"/>';

            resetFieldsAndSize(val, 1);

            var addOneForSelect = true;
            convertFieldsIntoTwoColumns(val, addOneForSelect);

            displayOutput += ' <li class="childNodes" onclick="selectAllChildNodes(event, this);">Select All</li>';

            selectedFieldClass = mergeFields(selectedFieldClass, true);

            displayOutput += '</ul> </div> </span> </li> ';
        });

        displayOutput += ' </ul> </li>';
    }

    $.each(val.subCategories, function (key, val) {
        buildReportTemplateMetadataTree(val, level + 1);
    });
}

function resetToggleCategories() {
    $("#validationMsg").css({display: 'none'});
    $("#validationMsg2").css({display: 'none'});
    $("#validationEmptyMsg").css({display: 'none'});
    $("#validationSameMsg").css({display: 'none'});
    $("#validationEmptyMsg2").css({display: 'none'});
    $("#validationSameMsg2").css({display: 'none'});
    document.getElementById("toggleText").innerHTML = "Collapse All";
    document.getElementById("toggleCategoriesImg").src = "css/images/arrows_collapse.png";
    document.getElementById("toggleAllCategories").title = "Collapse";
}

function toggleAllCategories(event, obj) {
    preventDefaultAction(event);
    var toggleView = "";
    var toggleTitle = "";
    var toggleImgSrc = "";
    var toggleCursor = "";
    if (obj.title == "Collapse") {
        toggleView = 'none';
        toggleTitle = "Expand";
        toggleImgSrc = "css/images/sm_circle_plus.png";
        obj.title = toggleTitle;
        toggleCursor = 'default';
        document.getElementById("toggleText").innerHTML = "Expand All";
        document.getElementById("toggleCategoriesImg").src = "css/images/arrows_expand.png";
    }
    else {
        toggleView = 'block';
        toggleTitle = "Collapse";
        toggleImgSrc = "css/images/sm_circle_minus.png";
        obj.title = toggleTitle;
        toggleCursor = 'pointer';
        document.getElementById("toggleText").innerHTML = "Collapse All";
        document.getElementById("toggleCategoriesImg").src = "css/images/arrows_collapse.png";
    }

    for (var i = 0; i <= 2; i++) {
        var className = 'parentNode ' + i;
        var categoryLevels = $("." + className.replace(/\s/g, "."));
        for (var j = 0; j < categoryLevels.length; j++) {
            var object = categoryLevels[j];
            var children_header = $(object).parent('li.parent_li').find(' > ul');
            $(children_header).css({display: toggleView, cursor: toggleCursor});
            $(object).attr('title', toggleTitle).find('> img').attr('src', toggleImgSrc);
            adjustHeight(className);
        }
    }
}

function checkIfAnyCategoryOpen() {
    for (var i = 0; i <= 2; i++) {
        var className = 'parentNode ' + i;
        var categoryLevels = $("." + className.replace(/\s/g, "."));
        for (var j = 0; j < categoryLevels.length; j++) {
            var object = categoryLevels[j];
            if (object.title == "Collapse") {
                document.getElementById("toggleText").innerHTML = "Collapse All";
                document.getElementById("toggleCategoriesImg").src = "css/images/arrows_collapse.png";
                document.getElementById("toggleAllCategories").title = "Collapse";
                return;
            }
        }
    }
    document.getElementById("toggleAllCategories").title = "Expand";
    document.getElementById("toggleText").innerHTML = "Expand All";
    document.getElementById("toggleCategoriesImg").src = "css/images/arrows_expand.png";
}

function testLength(ta) {
    var maxLength = 50;
    if (ta.value.length > maxLength) {
        ta.value = ta.value.substring(0, maxLength);
    }
}

function updateSelectedReportName(event) {
    preventDefaultAction(event);
    getBrowserVersion();
    var currentEle = document.getElementById('selectedReportName');
    var name = $.trim(currentEle.textContent);
    if (majorVersion == "8") {
        name = $.trim($('#selectedReportName').text());
    }
    $('#editReportName').css({display: 'none'});
    var maxLength = "";
    if (majorVersion != "8") {
        maxLength = " maxlength='50' ";
    }
    else {
        maxLength = ' onchange="testLength(this)" onkeyup="testLength(this)" onpaste="testLength(this)" ';
    }
    $(currentEle).html('<input type="text" name="reportName" id="txtReportName" size="30" ' + maxLength + '/>');
    $('#txtReportName').focus().val(name);
    document.getElementById('txtReportName').setAttribute("value", name);
    $("#txtReportName").keyup(function (event) {
        if (event.keyCode == 13) {
            inlineEditingConditions(event, name, currentEle);
        }
    });
    $("#txtReportName").focusout(function () {
        inlineEditingConditions(event, name, currentEle);
    });
}

function inlineEditingConditions(event, name, currentEle) {
    var inputValue = $.trim($("#txtReportName").val());
    $("#validationMsg").css({display: 'none'});
    $("#validationMsg2").css({display: 'none'});
    $("#validationEmptyMsg").css({display: 'none'});
    $("#validationSameMsg").css({display: 'none'});
    $("#validationEmptyMsg2").css({display: 'none'});
    $("#validationSameMsg2").css({display: 'none'});
    if (name == inputValue) {
        $(currentEle).html(inputValue);
        $("#" + selectedUserReportId + "_Custom").text(inputValue);
        $('#editReportName').css({display: 'inline'});
        return;
    }
    else if (inputValue == "") {
        $("#validationEmptyMsg2").css({display: 'block'});
        return;
    }
    else if (!validateSpecialCharacters(inputValue)) {
        $("#validationMsg2").css({display: 'block'});
        return;
    }
    selectedReportName = inputValue;
    inlineSaveReport(event, currentEle, selectedUserReportId, inputValue);
}

function inlineSaveReport(event, currentEle, selectedUserReportId, inputValue) {
    preventDefaultAction(event);
    selectedTcfIds();
    allSelectedDropDownValues('sort_list_', 3);
    selectedSortFilterOrderIds();

    var jsonData = JSON.stringify({
        reportName: selectedReportName,
        selectedTemplateCategoryFieldIds: selectedFields,
        filterSelectedIds: filterSelectedIds,
        sortSelectedIds: sortSelectedIds,
        tcfIdToStringSortList: sortSelectedListMap,
        tcfIdToStringFilterList: filterSelectedListMap
    });

    var reportUrl = "rest/reports/templates/" + parseInt(selectedUserReportId) + "/update-users-report";
    $.post(reportUrl, {data: jsonData}, function (data) {
        if (data != "") {
            var parsedData = JSON.parse(data);
            var latestUpdateTime = parsedData.latestUpdate;
            var reportInfoVal = "Report Type: " + parsedData.reportTemplateBase + "\nLast Saved: " + latestUpdateTime;
            document.getElementById("reportTopInfo").title = reportInfoVal;
            document.getElementById("reportBottomInfo").title = reportInfoVal;
            alert("Your saved report is updated.");
            $("#validationMsg").css({display: 'none'});
            $("#validationMsg2").css({display: 'none'});
            $("#validationEmptyMsg").css({display: 'none'});
            $("#validationSameMsg").css({display: 'none'});
            $("#validationEmptyMsg2").css({display: 'none'});
            $("#validationSameMsg2").css({display: 'none'});
            $(currentEle).html(selectedReportName);
            $("#" + selectedUserReportId + "_Custom").text(inputValue);
            $('#editReportName').css({display: 'inline'});
            previousSelectedCustomReport = createEachReportTemplateElement("", selectedUserReportId, "Custom", selectedReportId,
                latestUpdateTime, inputValue, parsedData.reportTemplateBase);
        }
        else {
            $("#validationSameMsg2").css({display: 'block'});
            return;
        }
    });
}

function toggleCategories(event, obj) {
    preventDefaultAction(event);

    var children = $(obj).parent('li.parent_li').find(' > ul > li');
    var children_header = $(obj).parent('li.parent_li').find(' > ul');

    if (children.is(":visible")) {
        $(children_header).css({display: 'none', cursor: 'default'});
        $(obj).attr('title', 'Expand').find('> img').attr('src', 'css/images/sm_circle_plus.png');
        adjustHeight(obj.className);
    }
    else {
        $(children_header).css({display: 'block', cursor: 'pointer'});
        $(obj).attr('title', 'Collapse').find('> img').attr('src', 'css/images/sm_circle_minus.png');
        adjustHeight(obj.className);
    }
    checkIfAnyCategoryOpen();
}

function adjustHeight(className) {
    var categoryLevels = $("." + className.replace(/\s/g, "."));
    var isAnyMenuOpen = false;
    for (var i = 0; i < categoryLevels.length; i++) {
        var object = categoryLevels[i];
        var children = $(object).parent('li.parent_li').find(' > ul > li');
        if (children.is(":visible")) {
            isAnyMenuOpen = true;
        }
    }

    if (!isAnyMenuOpen) {
        for (var i = 0; i < categoryLevels.length; i++) {
            var object = categoryLevels[i];
            $(object).parent('li.parent_li').css({height: '51px'});
        }
    }
    else {
        for (var i = 0; i < categoryLevels.length; i++) {
            var object = categoryLevels[i];
            if (className.indexOf("1") != -1) {
                $(object).parent('li.parent_li').css({height: level2Height});
            } else if (className.indexOf("2") != -1) {
                $(object).parent('li.parent_li').css({minHeight: '100px'});
                $(object).parent('li.parent_li').css({height: ''});
            } else {
                $(object).parent('li.parent_li').css({height: level1Height});
            }
        }
    }
}

function displayRelatedFields(event, obj) {
    var children = $(obj).find(' > span > div > ul ');

    if (children[0].className == 'onclick-menu-content-hide') {
        closeAllOpenMenus(event);
        children.show('fast');
        $(obj).addClass('selectedChildNode').removeClass('childNodes');
        children.addClass('onclick-menu-content-display two-col-special').removeClass('onclick-menu-content-hide');
    }
    else {
        closeAllOpenMenus(event);
    }
}

function closeAllOpenMenus(event) {
    preventDefaultAction(event);

    var displayedMenus = $(".onclick-menu-content-display.two-col-special");
    // only one menu will be open at a time
    if (displayedMenus.length > 0) {
        var object = displayedMenus[0];
        var clickedObject = document.getElementById(object.id);

        $(object).find('.selectedChildNode').length > 0 ?
            $(clickedObject).addClass('selectedChildNode').removeClass('childNodes') :
            $(clickedObject).addClass('childNodes').removeClass('selectedChildNode');
        $(object).addClass('onclick-menu-content-hide').removeClass('onclick-menu-content-display two-col-special');
    }
}

function highlightFields() {
    var hiddenMenus = $(".onclick-menu-content-hide");
    // only one menu will be open at a time
    if (hiddenMenus.length > 0) {
        for (var i = 0; i < hiddenMenus.length; i++) {
            var object = hiddenMenus[i];
            var parentObject = document.getElementById(object.id);

            $(object).find('.selectedChildNode').length > 0 ?
                $(parentObject).addClass('selectedChildNode').removeClass('childNodes') :
                $(parentObject).addClass('childNodes').removeClass('selectedChildNode');
        }
    }
}

function selectChildNode(event, obj, sortable, fieldType) {
    preventDefaultAction(event);

    if (obj.className == 'childNodes') {
        $(obj).addClass('selectedChildNode').removeClass('childNodes');
    }
    else {
        $(obj).addClass('childNodes').removeClass('selectedChildNode');
    }

    trackChangesMade = false;

    if (selectedReportType == "Custom") {
        trackChangesMade = true;
    }
    var displayedMenuFields = $(".onclick-menu-content-display.two-col-special");
    var childNodes = $(displayedMenuFields).find(' > li ');
    var closeDialog = true;
    for (var i = 0; i < childNodes.length; i++) {
        var child = childNodes[i];
        if (child.id == obj.id) {
            closeDialog = false;
        }
    }

    if (closeDialog) {
        var displayedMenus = $(".onclick-menu-content-display.two-col-special");
        // only one menu can be open at a time
        if (displayedMenus.length > 0) {
            var object = displayedMenus[0];
            var clickedObject = document.getElementById(object.id);

            $(object).find('.selectedChildNode').length > 0 ?
                $(clickedObject).addClass('selectedChildNode').removeClass('childNodes') :
                $(clickedObject).addClass('childNodes').removeClass('selectedChildNode');
            $(object).addClass('onclick-menu-content-hide').removeClass('onclick-menu-content-display two-col-special');
        }
    }
    updateSortFilterListWithSelection(obj, sortable, fieldType);
}

function displayAllActionBlocks() {
    $(".actionButtons").css({display: 'block'});
    $(".reportTrash").css({display: 'none'});
    $(".reportInfo").css({display: 'none'});
    $("#reportBottomSaveNew").css({display: 'none'});
    $("#reportTopSaveNew").css({display: 'none'});
    $("#reportBottomSave").css({display: 'none'});
    $("#reportTopSave").css({display: 'none'});
    $(".reportExport").css({display: 'none'});

    if (selectedReportType == 'Custom') {
        $("#reportBottomSaveNew").css({display: 'inline'});
        $("#reportTopSaveNew").css({display: 'inline'});
        $(".reportTrash").css({display: 'inline'});
        $(".reportInfo").css({display: 'inline'});
    }
    $(".reportExport").css({display: 'inline'});
    $("#reportBottomSave").css({display: 'inline'});
    $("#reportTopSave").css({display: 'inline'});
    $('#clearSelectedFieldsLink').css({display: 'block'});
    $("#sortGroup").css({display: 'block'});
    $("#filterGroup").css({display: 'block'});
}

function sortDropDownLists(selectId) {
    var foption = $(selectId + ' option:first');
    var soptions = $(selectId + ' option:not(:first)').sort(function (a, b) {
        return a.text == b.text ? 0 : a.text < b.text ? -1 : 1
    });
    $(selectId).html(soptions).prepend(foption);
    $(selectId).get(0).selectedIndex = 0;
}

function updateSortFilterListWithSelection(obj, sortable, fieldType) {
    getBrowserVersion();
    var templateCategoryFieldSelection = $(".selectedChildNode");
    $("#sortGroup").css({display: 'none'});
    $("#filterGroup").css({display: 'none'});
    $('#clearSelectedFieldsLink').css({display: 'none'});
    $(".actionButtons").css({display: 'none'});
    var tcfsLength = templateCategoryFieldSelection.length;
    var finalLength = 0;
    for (var i = 0; i < tcfsLength; i++) {
        var id = templateCategoryFieldSelection[i].id;
        if (id && id.indexOf("row") == -1) {
            finalLength++;
        }
    }

    if (finalLength == 0) {
        sortList = [], filterList = [];
        resetDropDowns(sortList, filterList);
        removeValFromDropDown(obj, 'sort_list_', 3);
        removeValFromDropDown(obj, 'filter_list_', 5);
    }

    if (finalLength > 0) {
        displayAllActionBlocks();
        if (obj.className == "childNodes") {
            removeValFromDropDown(obj, 'sort_list_', 3);
            removeValFromDropDown(obj, 'filter_list_', 5);
            $("option[value='" + obj.id + "']").remove();

            var selectSort = document.getElementById('sort_list_1');
            if (selectSort.length < 2) {
                $("#sortGroup").css({display: 'none'});
            }
            else {
                $("#sortGroup").css({display: 'block'});
            }
            var selectFilter = document.getElementById('filter_list_1');
            if (selectFilter.length < 2) {
                $("#filterGroup").css({display: 'none'});
            }
            else {
                $("#filterGroup").css({display: 'block'});
            }
            delete filterListMap[obj.id];
        }
        else {
            for (var i = 1; i <= 5; i++) {
                var sortIdName = "sort_list_" + i;
                var filterIdName = "filter_list_" + i;
                var selectSort;

                if (i < 4 && sortable) {
                    selectSort = document.getElementById(sortIdName);
                    var selectedSortId = selectSort.options[selectSort.selectedIndex].value;
                    var option = document.createElement('option');
                    var name = obj.textContent;
                    if (majorVersion == "8") {
                        name = obj.innerText;
                    }
                    option.text = name + " ( " + obj.title + " )";
                    option.value = obj.id;
                    selectSort.add(option, 1);
                    var sortId = "#" + sortIdName;
                    sortDropDownLists(sortId);
                    if ($(sortId).val() != null) {
                        $(sortId).val("");
                    }
                    if (selectedSortId != "") {
                        $(sortId).val(selectedSortId);
                    }
                }

                if (i < 4) {
                    selectSort = document.getElementById(sortIdName);
                    if (selectSort.length < 2) {
                        $("#sortGroup").css({display: 'none'});
                    }
                    else {
                        $("#sortGroup").css({display: 'block'});
                    }
                }

                var selectFilter = document.getElementById(filterIdName);
                if (fieldType != "undefined") {
                    var selectedFilterId = selectFilter.options[selectFilter.selectedIndex].value;
                    var option = document.createElement('option');
                    var name = obj.textContent;
                    if (majorVersion == "8") {
                        name = obj.innerText;
                    }
                    option.text = name + " ( " + obj.title + " )";
                    option.value = obj.id;
                    selectFilter.add(option, 1);
                    var filterId = "#" + filterIdName;
                    sortDropDownLists(filterId);
                    delete filterListMap[obj.id];
                    filterListMap[obj.id] = fieldType.replace(/[']/g, "");

                    if ($(filterId).val() != null) {
                        $(filterId).val("");
                    }
                    if (filterId != "") {
                        $(filterId).val(selectedFilterId);
                    }
                }
                else {
                    if (selectFilter.length < 2) {
                        $("#filterGroup").css({display: 'none'});
                    }
                    else {
                        $("#filterGroup").css({display: 'block'});
                    }
                }
            }
        }
    }
}

function selectAllChildNodes(event, obj) {
    preventDefaultAction(event);
    var displayText = obj.innerHTML;
    toggleFields(displayText);
    $(".actionButtons").css({display: 'block'});
    if (displayText == "Select All") {
        obj.innerHTML = 'Deselect All';
    }
    else {
        obj.innerHTML = 'Select All';
    }
    trackChangesMade = false;

    if (selectedReportType == "Custom") {
        trackChangesMade = true;
    }
}

function toggleFields(displayText) {
    var displayedMenuFields = $(".onclick-menu-content-display.two-col-special");
    var childrens = $(displayedMenuFields).find(' > li ');
    for (var i = 0; i < childrens.length; i++) {
        var child = childrens[i];
        if (displayText == "Select All") {
            $(child).addClass('selectedChildNode').removeClass('childNodes');
        }
        else {
            $(child).addClass('childNodes').removeClass('selectedChildNode');
        }
        if (!child.id) continue;
        var onclickFunc = $("#" + child.id).attr('onclick');
        var f = onclickFunc != undefined ? onclickFunc.toString().replace(/[;']$/, '').replace(/\(|\)/g, "") : "";
        var match = f.split(',');
        var sort = false;
        var fieldType = undefined;
        for (var a = 2; a < match.length - 1; a++) {
            sort = match[a];
            fieldType = match[a + 1];
            for (var l = 1; l <= 5; l++) {
                var dropDownListId = "filter_list_" + l;
                var select2 = document.getElementById(dropDownListId);
                var selectedFilterId = select2.options[select2.selectedIndex].value;
                if ($("#filter_list_" + l + " option[value='" + child.id + "']").length != 0) {
                    $("#filter_list_" + l + " option[value='" + child.id + "']").remove();
                    (child.id == selectedFilterId) ? removeChildNodes(l) : "";
                }
            }
            updateSortFilterListWithSelection(child, sort, fieldType);
        }
    }
}

function selectedTcfIds() {
    selectedFields = [];
    var templateCategoryFieldSelection = $(".selectedChildNode");

    for (var index = 0; index < templateCategoryFieldSelection.length; index++) {
        var id = templateCategoryFieldSelection[index].id;

        // sub-category show/hide group name has id of the form "row<number>"
        // 'select all' has id == ""

        if (id && id.indexOf("row") == -1) {
            selectedFields.push(id);
        }
    }
}
var form = document.createElement("form");
var hiddenField = document.createElement("input");

function exportToExcel() {
    setTimeout(function () {
        selectedTcfIds();

        if (selectedFields.length == 0) {
            alert('No fields selected!');
            return;
        }

        for (var key in filterSelectedListMap) {
            if (typeof filterSelectedListMap[key] !== 'function') {
                var value = filterSelectedListMap[key];
                if (value == "") {
                    validateFilterDropdowns(key);
                    return;
                }
            }
        }

        selectedStartDate = '';
        selectedEndDate = '';

        if (isDateBoundedReport) {
            selectedStartDate = $('#reportStartDate').datepicker('getDate');
            selectedEndDate = $('#reportEndDate').datepicker('getDate');

            if ($('#reportStartDate').datepicker('getDate') == null || $('#reportEndDate').datepicker('getDate') == null) {
                alert("Please select a valid date range!");
                return;
            }
            else {
                if (selectedStartDate > selectedEndDate) {
                    $("#reportEndDate").datepicker("option", "minDate", selectedStartDate);
                    alert("Please select a valid date range!");
                    return;
                }
            }
            selectedEndDate.setHours(23, 59, 59, 59);
        }
        allSelectedDropDownValues('sort_list_', 3);
        selectedSortFilterOrderIds();

        var jsonData = JSON.stringify({
            selectedTemplateCategoryFieldIds: selectedFields,
            startDate: selectedStartDate != '' ? selectedStartDate.valueOf() : null,
            endDate: selectedEndDate != '' ? selectedEndDate.valueOf() : null,
            filterSelectedIds: filterSelectedIds,
            sortSelectedIds: sortSelectedIds,
            tcfIdToStringSortList: sortSelectedListMap,
            tcfIdToStringFilterList: filterSelectedListMap
        });

        var actionUrl = 'rest/reports/templates/' + selectedReportId + '/' + selectedReportType + '/' + selectedUserReportId + '/results';

        // Seems to be no need to worry about polluting the DOM with many such forms, since they seem
        //    to disappear after each call to this function
        // NB: $.post() does not seem to work, even if it omits the data-receiving function

        document.body.appendChild(form);
        form.appendChild(hiddenField);

        form.setAttribute("method", "post");
        form.setAttribute("action", actionUrl);

        hiddenField.style.display = "none";
        hiddenField.setAttribute("name", "data");
        hiddenField.setAttribute("value", jsonData);

        form.submit();
    }, 100);
}

function clearAllSelectedFields(event) {
    $("#clear-dialog-confirm").dialog({
        resizable: false,
        height: 170,
        width: 510,
        modal: true,
        open: function () {
            $('body').css('overflow', 'hidden');
        },
        close: function () {
            $('body').css('overflow', 'scroll');
        },
        buttons: {
            "Cancel": function () {
                $(".ui-dialog-content").dialog("close");
                $(this).dialog("close");
            },
            "Clear": function () {
                $(this).dialog("close");
                $("#sortGroup").css({display: 'none'});
                $("#filterGroup").css({display: 'none'});
                $('#clearSelectedFieldsLink').css({display: 'none'});
                $(".reportExport").css({display: 'none'});
                $(".saveButtons").css({display: 'none'});
                $(".saveButtons .activeButton").css({display: 'none'});
                sortList = [];
                filterList = [];
                if (selectedReportType == "Custom") {
                    trackChangesMade = true;
                    $(".reportInfo").css({display: 'inline-block'});
                    $(".reportTrash").css({display: 'inline-block'});
                }
                resetDropDowns(sortList, filterList);
                closeAllOpenMenus(event);
                var displayedTwoColumnFields = $(".two-col-special");
                var displayedOneColumnFields = $(".one-col-special");
                var twoColumnFields = $(displayedTwoColumnFields).find(' > li ');
                var oneColumnsFields = $(displayedOneColumnFields).find(' > li ');
                var displayedMenuFields = $(".onclick-menu-content-hide");
                var menuFields = $(displayedMenuFields).find(' > li ');
                for (var i = 0; i < menuFields.length; i++) {
                    var child = menuFields[i];
                    var displayText = child.innerHTML;
                    if (displayText == "Deselect All") {
                        child.innerHTML = 'Select All';
                    }
                    $(child).addClass('childNodes').removeClass('selectedChildNode');
                }

                for (var i = 0; i < twoColumnFields.length; i++) {
                    var child = twoColumnFields[i];
                    $(child).addClass('childNodes').removeClass('selectedChildNode');
                }
                for (var i = 0; i < oneColumnsFields.length; i++) {
                    var child = oneColumnsFields[i];
                    $(child).addClass('childNodes').removeClass('selectedChildNode');
                }
            }
        }
    });
}

function removeChildNodes(id) {
    var node = document.getElementById("filter" + id);
    while (node.hasChildNodes()) {
        node.removeChild(node.lastChild);
    }
}

function resetSortDirections() {
    for (var id = 1; id <= 5; id++) {
        if (id < 4) {
            $("input[name=sort_list_" + id + "][value=asc]").attr('checked', true);
            $("#sort_list_" + id).html(sortSelectOptions);
            sortDropDownLists("#sort_list_" + id);
            hideAllByTag("sort_list_" + id);
            var selectedRadio = "sort_list_" + id + "_asc";
            document.getElementById(selectedRadio).checked = true;
        }
        $("#filter_list_" + id).html(filterSelectOptions);
        removeChildNodes(id);
        sortDropDownLists("#filter_list_" + id);
    }
}

function resetDropDowns(sortList, filterList) {
    selectedSortDropDownIndex = [];
    selectedFilterDropDownIndex = [];
    filterSelectedListMap = {};
    filterSelectedIds = [];
    sortSelectedIds = [];
    sortSelectedListMap = {};
    sortSelectOptions = buildSortFilterSelectOptions(sortList, 'displayName', 'choose');
    filterSelectOptions = buildSortFilterSelectOptions(filterList, 'displayName', 'choose');
    resetSortDirections();
}

function validateFilterDropdowns(key) {
    for (var i = 1; i <= 5; i++) {
        var filterVal = $('#filter_list_' + i + ' :selected').val();
        if (filterVal == key) {
            alert("Invalid entry in Filter " + i);
        }
    }
}

function selectedSortFilterOrderIds() {
    filterSelectedIds = [];
    sortSelectedIds = [];

    for (var i = 1; i <= 5; i++) {
        var filterVal = $('#filter_list_' + i + ' :selected').val();
        if (filterVal != "") {
            filterSelectedIds.push(filterVal);
        }
        if (i < 4) {
            var sortVal = $('#sort_list_' + i + ' :selected').val();
            if (sortVal != "") {
                sortSelectedIds.push(sortVal);
            }
        }
    }
}

function saveReport(event, isNewReport) {
    preventDefaultAction(event);

    for (var key in filterSelectedListMap) {
        if (typeof filterSelectedListMap[key] !== 'function') {
            var value = filterSelectedListMap[key];
            if (value == "") {
                validateFilterDropdowns(key);
                return;
            }
        }
    }

    if (!isNewReport && selectedReportType == 'Custom') {
        selectedTcfIds();
        allSelectedDropDownValues('sort_list_', 3);
        selectedSortFilterOrderIds();

        var jsonData = JSON.stringify({
            reportName: selectedReportName,
            selectedTemplateCategoryFieldIds: selectedFields,
            filterSelectedIds: filterSelectedIds,
            sortSelectedIds: sortSelectedIds,
            tcfIdToStringSortList: sortSelectedListMap,
            tcfIdToStringFilterList: filterSelectedListMap
        });

        var reportUrl = "rest/reports/templates/" + parseInt(selectedUserReportId) + "/update-users-report";

        $.post(reportUrl, {data: jsonData}, function (data) {
            if (data != "") {
                var parsedData = JSON.parse(data);
                var lastUpdateTime = parsedData.latestUpdate;
                var reportInfoVal = "Report Type: " + parsedData.reportTemplateBase + "\nLast Saved: " + lastUpdateTime;
                document.getElementById("reportTopInfo").title = reportInfoVal;
                document.getElementById("reportBottomInfo").title = reportInfoVal;

                alert("Your saved report is updated.");
                trackChangesMade = false;
                $("#validationMsg").css({display: 'none'});
                $("#validationMsg2").css({display: 'none'});
                $("#validationEmptyMsg").css({display: 'none'});
                $("#validationSameMsg").css({display: 'none'});
                $("#validationEmptyMsg2").css({display: 'none'});
                $("#validationSameMsg2").css({display: 'none'});
            }
            else {
                $("#validationSameMsg").css({display: 'block'});
                return;
            }
        });
    }
    else {
        $("#update-saved-report-confirm").dialog({
            resizable: false,
            height: "auto",
            width: 530,
            modal: true,
            open: function () {
                $('body').css('overflow', 'hidden');
                var reportName = $("#selectedReportName").text();
                if (isNewReport && selectedReportType == 'Custom') {
                    $("#savedReportName").val(reportName + "_Copy");
                }
                else {
                    $("#savedReportName").val(reportName);
                }
            },
            close: function () {
                $('body').css('overflow', 'scroll');
                $("#validationMsg").css({display: 'none'});
                $("#validationEmptyMsg").css({display: 'none'});
                $("#validationSameMsg").css({display: 'none'});
                $("#validationEmptyMsg2").css({display: 'none'});
                $("#validationSameMsg2").css({display: 'none'});
            },
            buttons: {
                "Cancel": function () {
                    $(".ui-dialog-content").dialog("close");
                    $(this).dialog("close");
                },
                "Save": function () {
                    var reportNameInput = $.trim($("#savedReportName").val());
                    $("#validationMsg").css({display: 'none'});
                    $("#validationEmptyMsg").css({display: 'none'});
                    $("#validationSameMsg").css({display: 'none'});

                    if (reportNameInput == "") {
                        $("#validationEmptyMsg").css({display: 'inline-block'});
                        return;
                    }
                    else if (!validateSpecialCharacters(reportNameInput)) {
                        $("#validationMsg").css({display: 'inline-block'});
                        return;
                    }

                    selectedTcfIds();
                    allSelectedDropDownValues('sort_list_', 3);
                    selectedSortFilterOrderIds();

                    var jsonData = JSON.stringify({
                        reportName: reportNameInput,
                        selectedTemplateCategoryFieldIds: selectedFields,
                        filterSelectedIds: filterSelectedIds,
                        sortSelectedIds: sortSelectedIds,
                        tcfIdToStringSortList: sortSelectedListMap,
                        tcfIdToStringFilterList: filterSelectedListMap
                    });

                    var reportUrl = "rest/reports/templates/" + parseInt(selectedUserReportId) + "/update-users-report";
                    if (!isNewReport || (isNewReport && selectedReportType == 'Custom')) {
                        reportUrl = "rest/reports/templates/" + parseInt(selectedReportId) + "/create-users-report";
                    }

                    $.post(reportUrl, {data: jsonData}, function (data) {
                        if (data != "") {
                            var parsedData = JSON.parse(data);
                            $('#CustomList').attr("class", '');
                            var numItems = $("#CustomList li").length;
                            if (numItems >= 5) {
                                $('#CustomList').attr("class", 'borderList');
                            }
                            var latestUpdateTime = parsedData.latestUpdate;
                            var reportInfoVal = "Report Type: " + parsedData.reportTemplateBase + "\nLast Saved: " + latestUpdateTime;
                            document.getElementById("reportTopInfo").title = reportInfoVal;
                            document.getElementById("reportBottomInfo").title = reportInfoVal;
                            loadAllReportTemplates();
                            var radioId = parsedData.id + 'Custom';
                            previousSelectedCustomReport = "";
                            trackChangesMade = false;

                            setTimeout(function () {
                                document.getElementById(radioId).checked = true;
                                loadCategories(document.getElementById(radioId),
                                    parsedData.reportTemplateId,
                                    parsedData.id,
                                    latestUpdateTime,
                                    parsedData.reportTemplateName,
                                    'Custom',
                                    parsedData.reportTemplateBase);
                            }, 100);
                            $("#validationMsg").css({display: 'none'});
                            $("#validationMsg2").css({display: 'none'});
                            $("#validationEmptyMsg").css({display: 'none'});
                            $("#validationSameMsg").css({display: 'none'});
                            $("#validationEmptyMsg2").css({display: 'none'});
                            $("#validationSameMsg2").css({display: 'none'});
                            $(".ui-dialog-content").dialog("close");
                            $("#update-saved-report-confirm").dialog("close");
                        }
                        else {
                            $("#validationSameMsg").css({display: 'inline-block'});
                            return;
                        }
                    });
                }
            }
        });
    }
}

function validateSpecialCharacters(input) {
    var iChars = "*:\\\/|\"<>?";
    for (var i = 0; i < input.length; i++) {
        if (iChars.indexOf(input.charAt(i)) != -1) {
            return false;
        }
    }
    return true;
}

function deleteSavedReport(event) {
    preventDefaultAction(event);
    $("#delete-dialog-confirm").dialog({
        resizable: false,
        height: 170,
        width: 510,
        modal: true,
        open: function () {
            $('body').css('overflow', 'hidden');
        },
        close: function () {
            $('body').css('overflow', 'scroll');
        },
        buttons: {
            "Cancel": function () {
                $(".ui-dialog-content").dialog("close");
                $(this).dialog("close");
            },
            "Delete": function () {
                $.post("rest/reports/templates/users/delete/" + selectedUserReportId, {}, function (data) {
                    var parsedData = JSON.parse(data);
                    if (parsedData.result) {
                        trackChangesMade = false;
                        reportsModule();
                    }
                    else {
                        alert("System Error: An error occurred while deleting the saved report.")
                    }
                });
            }
        }
    });
}

function allSelectedDropDownValues(listName, maxSize) {
    if (maxSize == 3) {
        selectedSortDropDownIndex = [];
    }
    else {
        selectedFilterDropDownIndex = [];
    }

    for (var i = 1; i <= maxSize; i++) {
        var dropDownListId = listName + (i);

        var objId = document.getElementById(dropDownListId);
        var id = objId.options[objId.selectedIndex].index;
        if (objId.value != "") {
            if (maxSize == 3) {
                selectedSortDropDownIndex.push(id);
                var selectedVal = objId.options[objId.selectedIndex].value;
                delete sortSelectedListMap[selectedVal];
                var orderVal = $('input[name="' + dropDownListId + '"]:checked').val();
                sortSelectedListMap[selectedVal] = orderVal;
            }
            else {
                selectedFilterDropDownIndex.push(id);
            }
        }
    }
}

function disableAndTrackChangesOnSelection(index, select) {
    if (select.options[index].value != "") {
        select.options[index].disabled = true;
    }

    trackChangesMade = false;
    if (selectedReportType == "Custom") {
        trackChangesMade = true;
    }
}

function avoidSortDupes(len, options) {
    while (len--) {
        options[len].disabled = false;
        for (var i = 0; i < selectedSortDropDownIndex.length; i++) {
            if (selectedSortDropDownIndex[i].value != ""
                && len == selectedSortDropDownIndex[i]) {
                options[len].disabled = true;
            }
        }
    }
}

function avoidFilterDupes(len, options) {
    while (len--) {
        options[len].disabled = false;
        for (var i = 0; i < selectedFilterDropDownIndex.length; i++) {
            if (selectedFilterDropDownIndex[i].value != ""
                && len == selectedFilterDropDownIndex[i]) {
                options[len].disabled = true;
            }
        }
    }
}

function preventDupes(select, index, listName, maxSize) {
    allSelectedDropDownValues(listName, maxSize);
    var options = select.options,
        len = options.length;

    if (maxSize == 3) {
        avoidSortDupes(len, options);
    }
    else {
        avoidFilterDupes(len, options);
    }
    disableAndTrackChangesOnSelection(index, select);
}

function removeValFromDropDown(obj, listName, maxSize) {
    for (var i = 1; i <= maxSize; i++) {
        var dropDownListId = listName + (i);
        var select2 = document.getElementById(dropDownListId);
        for (var j = 0; j < select2.options.length; j++) {
            if (obj.id == select2.options[j].value) {
                var selectedFilterId = select2.options[select2.selectedIndex].value;
                (maxSize == "3" && (selectedFilterId == obj.id)) ? hideAllByTag(dropDownListId) : "";
                (maxSize == "5" && (selectedFilterId == obj.id)) ? removeChildNodes(i) : "";
                select2.options[j] = null;
                break;
            }
        }
    }
}

function trackValueChanges() {
    trackChangesMade = false;
    if (selectedReportType == "Custom") {
        trackChangesMade = true;
    }
}

function confirmNavigate() {
    if (trackChangesMade && selectedReportType == "Custom") {
        return "You have unsaved changes. If you continue, you will lose those changes.";
    }
}

function confirmNavigateDialog(reportId, reportType, userReportId, reportName, obj, reportBase, lastUpdatedTime) {
    $("#ignore-changes-confirm").dialog({
        resizable: false,
        height: 200,
        width: 510,
        modal: true,
        open: function () {
            $('body').css('overflow', 'hidden');
        },
        close: function () {
            $('body').css('overflow', 'scroll');
            $("#validationMsg").css({display: 'none'});
            $("#validationEmptyMsg").css({display: 'none'});
            $("#validationSameMsg").css({display: 'none'});
            $("#validationEmptyMsg2").css({display: 'none'});
            $("#validationSameMsg2").css({display: 'none'});
        },
        buttons: {
            "Cancel": function () {
                $(".ui-dialog-content").dialog("close");
                $(this).dialog("close");
            },
            "Continue": function () {
                trackChangesMade = false;
                $(".ui-dialog-content").dialog("close");
                $("#ignore-changes-confirm").dialog("close");
                loadCategoriesMetadata(reportId, reportType, userReportId, reportName, obj, reportBase, lastUpdatedTime);
            }
        }
    })
}

function sortSavedReportList(id) {
    $.ajax({
        url: 'rest/reports/sortSavedTemplates/' + id,
        type: 'GET',
        dataType: 'json',
        data: '',
        success: function (data) {
            var customDisplayOutput = '';
            $.each(data, function (key, val) {
                var displayOutput = '';
                var viewReportName = val.reportTemplateName;
                var viewReportId = val.reportTemplateId;
                var viewUserReportId = val.id;
                var viewReportType = val.reportTemplateType;
                var viewReportUpdateTime = val.latestUpdate;
                var viewReportBase = val.reportTemplateBase;
                var title = "Display default field selections";
                if (viewReportUpdateTime != undefined) {
                    viewReportType = 'Custom';
                    title = "";
                }
                displayOutput += createEachReportTemplateElement(title, viewUserReportId, viewReportType, viewReportId, viewReportUpdateTime, viewReportName, viewReportBase);
                customDisplayOutput += displayOutput;
            });

            $('#CustomList').html(customDisplayOutput);
            $('#CustomList').attr("class", '');
            var numItems = $("#CustomList li").length;
            if (numItems >= 5) {
                $('#CustomList').attr("class", 'borderList');
            }
        },
        error: function (xhr, status, error) {
            alert("There was a problem with the report. Please select different parameters for the report.");
        }
    });
}

var pediatric = ["Adult", "Adult/Pediatric", "Pediatric"];
var crc_category = ["A", "B", "C", "D"];
var booleanSets = ["No", "Yes", "Null"];
var longSets = ["Does Not Include", "Includes"];
var fundingSourceIds = ["1", "2", "3", "4"];

function createElementsBasedOnFieldType(id, previousValue, expressionVal) {
    var currentEle = document.getElementById('filter_list_' + id);
    var selectedVal = currentEle.value;
    var type = filterListMap[selectedVal];
    var newEle = document.getElementById('filter' + id);
    if (selectedVal == "") {
        removeChildNodes(id);
    }
    else {
        filterSelectedListMap[selectedVal] = "";
    }

    if (previousValue != "") {
        delete filterSelectedListMap[previousValue];
    }

    if (type == "ShortSetStringDbLongName") {
        var selectId = type + "_" + id;
        $(newEle).html('<select id="' + selectId + '" multiple="multiple"></select>');
        var url = "rest/reports/getStaticLists/" + selectedVal + "/longName";
        createStaticListsDropdown(url, id, selectId, expressionVal, 'longName', 3);
    }
    else if (type == "ShortSetStringDbShortName") {
        var selectId = type + "_" + id;
        $(newEle).html('<select id="' + selectId + '" multiple="multiple"></select>');
        var url = "rest/reports/getStaticLists/" + selectedVal + "/shortName";
        createStaticListsDropdown(url, id, selectId, expressionVal, 'shortName', 3);
    }
    else if (type == "ShortSetStringDB") {
        var selectId = type + "_" + id;
        $(newEle).html('<select id="' + selectId + '" multiple="multiple"></select>');
        var url = "rest/reports/getStaticLists/" + selectedVal + "/name";
        createStaticListsDropdown(url, id, selectId, expressionVal, 'name', 3);
    }
    else if (type == "ShortSetStringDbIdBack") {
        var selectId = type + "_" + id;
        $(newEle).html('<select id="' + selectId + '" multiple="multiple"></select>');
        var url = "rest/reports/getStaticLists/" + selectedVal + "/name";
        createStaticListsDropdown(url, id, selectId, expressionVal, 'name', 3);
    }
    else if (type == "Enum") {
        var selectId = type + "_" + id;
        $(newEle).html('<select id="' + selectId + '" multiple="multiple"></select>');
        var url = "rest/reports/getListsByEnumField/" + selectedVal;
        createStaticListsDropdown(url, id, selectId, expressionVal, 'name', 3);
    }
    else if (type == "Boolean") {
        var booleanId = type + "_" + id;
        $(newEle).html('<select id="' + booleanId + '" multiple="multiple"></select>');
        createUIMultiDropdown(booleanId, booleanSets, id, expressionVal, 3);
    }
    else if (type == "ShortSetStringUI") {
        var selectedText = currentEle.options[currentEle.selectedIndex].text;
        if (selectedText.indexOf("CRC Category") > -1) {
            var crcId = type + "_" + id;
            $(newEle).html('<select id="' + crcId + '" multiple="multiple"></select>');
            createUIMultiDropdown(crcId, crc_category, id, expressionVal, 3);
        }
        else if (selectedText.indexOf("Funding Source ID") > -1) {
            var fundingSourceId = type + "_" + id;
            $(newEle).html('<select id="' + fundingSourceId + '" multiple="multiple"></select>');
            createUIMultiDropdown(fundingSourceId, fundingSourceIds, id, expressionVal, 3);
        }
        else {
            var pediatricId = type + "_" + id;
            $(newEle).html('<select id="' + pediatricId + '" multiple="multiple"></select>');
            createUIMultiDropdown(pediatricId, pediatric, id, expressionVal, 3);
        }
    }
    else if (type == "LongSetString") {
        var longSetId = type + "_" + id;
        $(newEle).html('<select id="' + longSetId + '" multiple="multiple"></select>');
        createUIMultiDropdown(longSetId, longSets, id, expressionVal, 2);
    }
    else if (type == "Date") {
        createDatePickerRange(newEle, id, selectedVal, expressionVal);
    }
    else if (type == "ShortString") {
        var shortStringId = type + "_" + id;
        $(newEle).html('<input type="text" style="width: 400px; height: 20px;" id="' + shortStringId + '"/>');
        shortStringAutoComplete(shortStringId, selectedVal);
        if (expressionVal != "") {
            var value = expressionVal.replace(/,\s*$/, "");
            $("#" + shortStringId).val(expressionVal);
            filterSelectedListMap[selectedVal] = value;
            selectedTerms = value;
        }
    }
    else if (type == "Number") {
        var startNumberId = "startNumber" + id;
        var endNumberId = "endNumber" + id;

        var filterVal = $('#filter_list_' + id + ' :selected').text();
        var dispMins = "";
        if (filterVal.indexOf("Duration") > -1) {
            dispMins = " minutes";
        }
        $(newEle).html('<input id="' + startNumberId + '"/>' + dispMins + "&nbsp;&nbsp;&nbsp;to&nbsp;&nbsp;&nbsp;" +
            '<input id="' + endNumberId + '"/>' + dispMins);

        $('#' + startNumberId).spinner({
            min: 0, step: 1,
            stop: function (e, ui) {
                var startValue = document.getElementById(startNumberId).value;
                var endValue = document.getElementById(endNumberId).value;
                if (isNaN(startValue)) {
                    startValue = "";
                }
                $('#' + endNumberId).spinner('option', 'min', startValue);
                if (startValue > endValue || endValue == "") {
                    $('#' + endNumberId).val(startValue);
                }
            }
        });

        $("#" + startNumberId).keyup(function (event) {
            if (isNaN($(this).val())) {
                return $(this).val('');
            }
        });

        $("#" + startNumberId).focusout(function (event) {
            updateNumberRangeFields(event, selectedVal, id);
        });
        $('#' + startNumberId).width(120);

        $('#' + endNumberId).spinner({min: 0, step: 1});
        $("#" + endNumberId).keyup(function (event) {
            if (isNaN($(this).val())) {
                return $(this).val('');
            }
        });
        $("#" + endNumberId).focusout(function (event) {
            updateNumberRangeFields(event, selectedVal, id);
        });
        $('#' + endNumberId).width(120);

        if (expressionVal != "") {
            var numbers = expressionVal.split(",");
            var startNumber = numbers[0];
            var endNumber = numbers[1];
            $('#' + startNumberId).val(startNumber);
            $('#' + endNumberId).val(endNumber);
            filterSelectedListMap[selectedVal] = expressionVal;
        }
    }
}

function updateNumberRangeFields(event, selectedVal, id) {
    preventDefaultAction(event);
    delete filterSelectedListMap[selectedVal];
    trackChangesMade = false;

    if (selectedReportType == "Custom") {
        trackChangesMade = true;
    }
    var startNumberId = "startNumber" + id;
    var endNumberId = "endNumber" + id;
    var startObj = document.getElementById(startNumberId);
    var endObj = document.getElementById(endNumberId);

    var startNumber = startObj != null ? startObj.value : "";
    var endNumber = endObj != null ? endObj.value : "";

    if (startNumber == "" || endNumber == "") {
        filterSelectedListMap[selectedVal] = "";
    }
    else if (startNumber < 0) {
        filterSelectedListMap[selectedVal] = "";
        $("#" + startNumberId).val("");
        alert("Invalid Start Number");
    }
    else if (endNumber < 0) {
        filterSelectedListMap[selectedVal] = "";
        $("#" + endNumberId).val("");
        alert("Invalid End Number");
    }
    else if (parseInt(startNumber) > parseInt(endNumber)) {
        filterSelectedListMap[selectedVal] = "";
        $("#" + startNumberId).val("");
        $("#" + endNumberId).val("");
        alert("Invalid Range");
    }
    else {
        var numberRange = startNumber + "," + endNumber;
        filterSelectedListMap[selectedVal] = numberRange;
    }
}

function createDatePickerRange(newEle, dateId, selectedVal, expressionVal) {
    var startDateId = "startDateRange" + dateId;
    var endDateId = "endDateRange" + dateId;
    var datePicker = '<div id="dateRangeContainer"> ' +
        '<span class="dateContainer" style="padding: 0 20px 0 0;"> ' +
        '<input type="text" id="' + startDateId + '" onkeyup="validateDatePickers(event, \'' + startDateId + '\',\'' + endDateId + '\',' + selectedVal + ')" class="ll-skin-latoja"/> </span> ' +
        '<span class="dateContainer" style="padding: 0"> ' +
        '<input type="text" id="' + endDateId + '" onkeyup="validateDatePickers(event, \'' + startDateId + '\',\'' + endDateId + '\',' + selectedVal + ')" class="ll-skin-latoja"/> </span>' +
        ' </div>';
    $(newEle).html(datePicker);
    createRangeDatePickers(startDateId, endDateId, selectedVal, expressionVal);
}

function validateDatePickers(event, startDateId, endDateId, selectedVal) {
    preventDefaultAction(event);
    delete filterSelectedListMap[selectedVal];
    trackChangesMade = false;

    if (selectedReportType == "Custom") {
        trackChangesMade = true;
    }
    var startDate = $("#" + startDateId).datepicker('getDate');
    var endDate = $("#" + endDateId).datepicker('getDate');
    filterSelectedListMap[selectedVal] = "";
    var startVal = $("#" + startDateId).val();
    var endVal = $("#" + endDateId).val();
    if (startVal == "" || endVal == ""
        || startVal == "__/__/____"
        || endVal == "__/__/____") {
        return;
    }
    if (startDate <= endDate) {
        trackChangesMade = false;

        if (selectedReportType == "Custom") {
            trackChangesMade = true;
        }
        filterSelectedListMap[selectedVal] = dateFilterCommaString(startDate, endDate);
    }
}

function createRangeDatePickers(startDateId, endDateId, selectedVal, expressionVal) {
    WidgetUtil.createDatepicker("#" + startDateId, {
        onSelect: function (selectedDate) {
            $("#" + endDateId).datepicker("option", "minDate", selectedDate);
            delete filterSelectedListMap[selectedVal];
            trackChangesMade = false;

            if (selectedReportType == "Custom") {
                trackChangesMade = true;
            }
            var startDate = $("#" + startDateId).datepicker('getDate');
            var endDate = $("#" + endDateId).datepicker('getDate');
            var endVal = $("#" + endDateId).val();
            if (endVal == "") {
                filterSelectedListMap[selectedVal] = "";
            }
            else {
                filterSelectedListMap[selectedVal] = dateFilterCommaString(startDate, endDate);
            }
        },
        onClose: function (dateText, inst) {
            try {
                var selectedDate = $.datepicker.parseDate('mm/dd/yy', dateText);
                $("#" + endDateId).datepicker("option", "minDate", selectedDate);
            } catch (e) {
                alert("Incorrect Date format. It should be MM/DD/YYYY.");
                $("#" + startDateId).val('');
                return;
            }
            ;
        }
    });

    WidgetUtil.createDatepicker("#" + endDateId, {
        onSelect: function (selectedDate) {
            delete filterSelectedListMap[selectedVal];
            trackChangesMade = false;

            if (selectedReportType == "Custom") {
                trackChangesMade = true;
            }
            var startDate = $("#" + startDateId).datepicker('getDate');
            var endDate = $("#" + endDateId).datepicker('getDate');
            var startVal = $("#" + startDateId).val();
            if (startVal == "") {
                filterSelectedListMap[selectedVal] = "";
            }
            else {
                filterSelectedListMap[selectedVal] = dateFilterCommaString(startDate, endDate);
            }
        },
        onClose: function (dateText, inst) {
            try {
                $.datepicker.parseDate('mm/dd/yy', dateText);
            } catch (e) {
                alert("Incorrect Date format. It should be MM/DD/YYYY.");
                $("#" + endDateId).val('');
                return;
            }
            ;
        }
    });

    if (expressionVal != "") {
        var dates = expressionVal.split(",");
        var startDate = new Date(dates[0]);
        var endDate = new Date(dates[1]);
        $("#" + startDateId).datepicker('setDate', startDate);
        $("#" + endDateId).datepicker('setDate', endDate);
        $("#" + endDateId).datepicker("option", "minDate", startDate);
    }
    else {
        var startDate = new Date();
        startDate.setDate(1);
        startDate.setMonth(startDate.getMonth() - 1);
        var endDate = new Date(); // current date
        endDate.setDate(1); // going to 1st of the month
        endDate.setHours(-1);
        $("#" + startDateId).datepicker('setDate', startDate);
        $("#" + endDateId).datepicker('setDate', endDate);
        $("#" + endDateId).datepicker("option", "minDate", startDate);
    }
    $('#ui-datepicker-div').hide();
    delete filterSelectedListMap[selectedVal];
    var startDate = $("#" + startDateId).datepicker('getDate');
    var endDate = $("#" + endDateId).datepicker('getDate');

    filterSelectedListMap[selectedVal] = dateFilterCommaString(startDate, endDate);
}

function createStaticListsDropdown(url, selectId, selectElementId, expressionVal, column, minimumCount) {
    $.get(url, {}, function (data) {
        var parsedData = JSON.parse(data);
        var staticList = parsedData.staticList;
        buildMultiSelectOptions(staticList, column, selectId, selectElementId, expressionVal, minimumCount);
    });
}

function setMultiSelectValues(selectElementId, expressionVal, selectListId, html, minimumCount) {
    $('#' + selectElementId).html(html.join('')).multipleSelect({
        placeholder: "choose",
        width: "490px",
        allSelected: "All Selected",
        maxHeight: 150,
        minimumCountSelected: minimumCount,
        onClose: function () {
            trackChangesMade = false;

            if (selectedReportType == "Custom") {
                trackChangesMade = true;
            }
            selectedFilterMaps(selectListId, selectElementId);
        }
    });
    var inputId = "#" + selectElementId;
    if (expressionVal != "") {
        var objId = document.getElementById("filter_list_" + selectListId);
        var selectedVal = objId.options[objId.selectedIndex].value;
        delete filterSelectedListMap[selectedVal];
        filterSelectedListMap[selectedVal] = expressionVal;
        var exp = expressionVal.split(',');
        $(inputId).multipleSelect('setSelects', exp);
    }
    else {
        $(inputId).multipleSelect("checkAll");
        selectedFilterMaps(selectListId, selectElementId);
    }
}

function buildMultiSelectOptions(list, optionTextProperty, selectListId, selectElementId, expressionVal, minimumCount) {
    var html = [];
    for (var i = 0, len = list.length; i < len; i++) {
        html[html.length] = "<option value='";
        html[html.length] = list[i].id;
        html[html.length] = "'>";
        html[html.length] = list[i][optionTextProperty];
        html[html.length] = "</option>";
    }
    setMultiSelectValues(selectElementId, expressionVal, selectListId, html, minimumCount);
}

function createUIMultiDropdown(selectElementId, listName, selectListId, expressionVal, minimumCount) {
    var html = [];
    for (var i = 0, len = listName.length; i < len; i++) {
        html[html.length] = "<option value='";
        html[html.length] = listName[i];
        html[html.length] = "'>";
        html[html.length] = listName[i];
        html[html.length] = "</option>";
    }
    setMultiSelectValues(selectElementId, expressionVal, selectListId, html, minimumCount);
}

function selectedFilterMaps(selectListId, selectElementId) {
    var objId = document.getElementById("filter_list_" + selectListId);
    var selectedVal = objId.options[objId.selectedIndex].value;
    delete filterSelectedListMap[selectedVal];
    var inputId = "#" + selectElementId;
    var selectedValues = $(inputId).multipleSelect('getSelects');
    if (selectedValues.length == 0) {
        filterSelectedListMap[selectedVal] = "";
    }
    else {
        var stringList = selectedValues.join(",");
        filterSelectedListMap[selectedVal] = stringList;
    }
}

function split(val) {
    return val.split(/,\s*/);
}

function extractLast(term) {
    return split(term).pop();
}

function removeExtraSpaces(string) {
    return string.replace(/\s{2,}/g, ' ');
}

function shortStringAutoComplete(shortStringId, selectedVal) {
    $("#" + shortStringId)
    // don't navigate away from the field on tab when selecting an item
        .bind('paste', function () {
            $("#" + shortStringId).autocomplete("close");

            setTimeout(function () {
                delete filterSelectedListMap[selectedVal];
                trackChangesMade = false;

                if (selectedReportType == "Custom") {
                    trackChangesMade = true;
                }
                var finalValue = removeExtraSpaces($("#" + shortStringId).val());
                $("#" + shortStringId).val(finalValue);
                filterSelectedListMap[selectedVal] = finalValue;
                selectedTerms = finalValue;
                $("#" + shortStringId).autocomplete('search', finalValue);
            }, 100);
        })
        .bind('cut', function () {
            $("#" + shortStringId).autocomplete("close");

            setTimeout(function () {
                delete filterSelectedListMap[selectedVal];
                trackChangesMade = false;

                if (selectedReportType == "Custom") {
                    trackChangesMade = true;
                }
                var finalValue = removeExtraSpaces($("#" + shortStringId).val());
                $("#" + shortStringId).val(finalValue);
                filterSelectedListMap[selectedVal] = finalValue;
                selectedTerms = finalValue;
                $("#" + shortStringId).autocomplete('search', finalValue);
            }, 100);
        })
        .bind("keydown", function (event) {
            if (event.keyCode === $.ui.keyCode.TAB
                && $(this).autocomplete("instance").menu.active) {
                event.preventDefault();
            }
            else if (event.keyCode === $.ui.keyCode.SPACE) {
                setTimeout(function () {
                    delete filterSelectedListMap[selectedVal];
                    var finalValue = removeExtraSpaces($("#" + shortStringId).val());
                    $("#" + shortStringId).val(finalValue);
                    filterSelectedListMap[selectedVal] = finalValue;
                    selectedTerms = finalValue;
                    $("#" + shortStringId).autocomplete('search', finalValue);
                }, 100);
            }
            else if (event.keyCode === $.ui.keyCode.BACKSPACE
                || event.keyCode === $.ui.keyCode.DELETE) {
                $("#" + shortStringId).autocomplete("close");

                setTimeout(function () {
                    delete filterSelectedListMap[selectedVal];
                    trackChangesMade = false;

                    if (selectedReportType == "Custom") {
                        trackChangesMade = true;
                    }
                    var finalValue = removeExtraSpaces($("#" + shortStringId).val());
                    $("#" + shortStringId).val(finalValue);
                    filterSelectedListMap[selectedVal] = finalValue;
                    selectedTerms = finalValue;
                    $("#" + shortStringId).autocomplete('search', finalValue);
                }, 100);
            }
            else if (event.keyCode != $.ui.keyCode.DOWN
                && event.keyCode != $.ui.keyCode.RIGHT
                && event.keyCode != $.ui.keyCode.UP
                && event.keyCode != $.ui.keyCode.LEFT) {
                setTimeout(function () {
                    delete filterSelectedListMap[selectedVal];
                    var finalValue = removeExtraSpaces($("#" + shortStringId).val());
                    $("#" + shortStringId).val(finalValue);
                    filterSelectedListMap[selectedVal] = finalValue;
                    selectedTerms = finalValue;
                }, 100);
            }
        })
        .autocomplete({
            minLength: 0,
            source: function (request, response) {
                $.getJSON("rest/reports/getListsByField/" + selectedVal, {
                    term: extractLast(request.term),
                    selectedTerms: selectedTerms
                }, response);
            },
            search: function () {
                // custom minLength
                var term = extractLast(this.value);
                if (term.length < 1) {
                    $("#" + shortStringId).autocomplete("close");
                    return false;
                }
            },
            focus: function () {
                return false;
            },
            select: function (event, ui) {
                delete filterSelectedListMap[selectedVal];
                trackChangesMade = false;

                if (selectedReportType == "Custom") {
                    trackChangesMade = true;
                }
                var terms = split(this.value);
                // remove the current input
                terms.pop();
                // add the selected item
                terms.push(ui.item.value);
                // add placeholder to get the comma-and-space at the end
                terms.push("");
                this.value = terms.join(",");
                var finalValue = (this.value).replace(/,\s*$/, "");
                filterSelectedListMap[selectedVal] = finalValue;
                selectedTerms = finalValue;
                return false;
            },
            close: function () {
                $("#" + shortStringId).autocomplete("destroy");
                shortStringAutoComplete(shortStringId, selectedVal);
            }
        });
    $('.ui-autocomplete-input').css('width', '400px')
}

function dateFilterCommaString(start, end) {
    end.setHours(23, 59, 59, 59);

    var dateString = dateToDateHelperFormat(start) + "," + dateToDateHelperFormat(end);
    return dateString;
}

function updateSortDropDownLists(selectSort1, selectSort2, selectSort3) {
    preventDupes.call(this, selectSort1, this.selectedIndex, 'sort_list_', 3);
    preventDupes.call(this, selectSort2, this.selectedIndex, 'sort_list_', 3);
    preventDupes.call(this, selectSort3, this.selectedIndex, 'sort_list_', 3);
}

function updateFilterDropDownLists(selectFilter1, selectFilter2, selectFilter3, selectFilter4, selectFilter5) {
    preventDupes.call(this, selectFilter1, this.selectedIndex, 'filter_list_', 5);
    preventDupes.call(this, selectFilter2, this.selectedIndex, 'filter_list_', 5);
    preventDupes.call(this, selectFilter3, this.selectedIndex, 'filter_list_', 5);
    preventDupes.call(this, selectFilter4, this.selectedIndex, 'filter_list_', 5);
    preventDupes.call(this, selectFilter5, this.selectedIndex, 'filter_list_', 5);
}

function showAllByTag(name) {
    var elements = $("." + name.replace(/\s/g, "."));
    var i = 0;
    while (i < elements.length) {
        elements[i].style.visibility = "";
        i++;
    }
}

function hideAllByTag(tagName) {
    var elements = $("." + tagName.replace(/\s/g, "."));
    var i = 0;
    while (i < elements.length) {
        elements[i].style.visibility = "hidden";
        i++;
    }
}

function myReady() {

    var prevChange = false;
    $("img").on("mousedown", function () {
        prevChange = trackChangesMade;
        trackChangesMade = false;
    });

    $("img").on("mouseup", function () {
        setTimeout(function () {
            trackChangesMade = prevChange;
        }, 200);
    });

    $('#saveDialog').on('keyup keypress', function (e) {
        var code = e.keyCode || e.which;
        if (code == 13) {
            e.preventDefault();
            return false;
        }
    });

    var selectSort1 = document.getElementById('sort_list_1');
    var selectSort2 = document.getElementById('sort_list_2');
    var selectSort3 = document.getElementById('sort_list_3');

    selectSort1.onchange = function () {
        if (selectSort1.value == "") {
            var selectedRadio = "sort_list_1_asc";
            document.getElementById(selectedRadio).checked = true;
            hideAllByTag('sort_list_1');
        }
        else {
            showAllByTag('sort_list_1');
        }
        updateSortDropDownLists.call(this, selectSort1, selectSort2, selectSort3);
    };

    if (majorVersion != "8" && majorVersion != "9") {
        selectSort1.onclick = function () {
            if (selectSort1.value == "") {
                hideAllByTag('sort_list_1');
            }
            else {
                showAllByTag('sort_list_1');
            }
            updateSortDropDownLists.call(this, selectSort1, selectSort2, selectSort3);
        };
    }

    selectSort2.onchange = function () {
        if (selectSort2.value == "") {
            var selectedRadio = "sort_list_2_asc";
            document.getElementById(selectedRadio).checked = true;
            hideAllByTag('sort_list_2');
        }
        else {
            showAllByTag('sort_list_2');
        }
        updateSortDropDownLists.call(this, selectSort1, selectSort2, selectSort3);
    };

    if (majorVersion != "8" && majorVersion != "9") {
        selectSort2.onclick = function () {
            if (selectSort2.value == "") {
                hideAllByTag('sort_list_2');
            }
            else {
                showAllByTag('sort_list_2');
            }
            updateSortDropDownLists.call(this, selectSort1, selectSort2, selectSort3);
        };
    }

    selectSort3.onchange = function () {
        if (selectSort3.value == "") {
            var selectedRadio = "sort_list_3_asc";
            document.getElementById(selectedRadio).checked = true;
            hideAllByTag('sort_list_3');
        }
        else {
            showAllByTag('sort_list_3');
        }
        updateSortDropDownLists.call(this, selectSort1, selectSort2, selectSort3);
    };

    if (majorVersion != "8" && majorVersion != "9") {
        selectSort3.onclick = function () {
            if (selectSort3.value == "") {
                hideAllByTag('sort_list_3');
            }
            else {
                showAllByTag('sort_list_3');
            }
            updateSortDropDownLists.call(this, selectSort1, selectSort2, selectSort3);
        };
    }

    var selectFilter1 = document.getElementById('filter_list_1');
    var selectFilter2 = document.getElementById('filter_list_2');
    var selectFilter3 = document.getElementById('filter_list_3');
    var selectFilter4 = document.getElementById('filter_list_4');
    var selectFilter5 = document.getElementById('filter_list_5');
    var previous;
    selectFilter1.onfocus = function () {
        previous = this.value;
    };
    selectFilter1.onchange = function () {
        createElementsBasedOnFieldType('1', previous, "");
        previous = this.value;
        updateFilterDropDownLists.call(this, selectFilter1, selectFilter2, selectFilter3, selectFilter4, selectFilter5);
    };

    if (majorVersion != "8" && majorVersion != "9") {
        selectFilter1.onclick = function () {
            updateFilterDropDownLists.call(this, selectFilter1, selectFilter2, selectFilter3, selectFilter4, selectFilter5);
        };
    }

    var previous2;
    selectFilter2.onfocus = function () {
        previous2 = this.value;
    };
    selectFilter2.onchange = function () {
        createElementsBasedOnFieldType('2', previous2, "");
        previous2 = this.value;
        updateFilterDropDownLists.call(this, selectFilter1, selectFilter2, selectFilter3, selectFilter4, selectFilter5);
    };

    if (majorVersion != "8" && majorVersion != "9") {
        selectFilter2.onclick = function () {
            updateFilterDropDownLists.call(this, selectFilter1, selectFilter2, selectFilter3, selectFilter4, selectFilter5);
        };
    }

    var previous3;
    selectFilter3.onfocus = function () {
        previous3 = this.value;
    };
    selectFilter3.onchange = function () {
        createElementsBasedOnFieldType('3', previous3, "");
        previous3 = this.value;
        updateFilterDropDownLists.call(this, selectFilter1, selectFilter2, selectFilter3, selectFilter4, selectFilter5);
    };

    if (majorVersion != "8" && majorVersion != "9") {
        selectFilter3.onclick = function () {
            updateFilterDropDownLists.call(this, selectFilter1, selectFilter2, selectFilter3, selectFilter4, selectFilter5);
        };
    }

    var previous4;
    selectFilter4.onfocus = function () {
        previous4 = this.value;
    };
    selectFilter4.onchange = function () {
        createElementsBasedOnFieldType('4', previous4, "");
        previous4 = this.value;
        updateFilterDropDownLists.call(this, selectFilter1, selectFilter2, selectFilter3, selectFilter4, selectFilter5);
    };

    if (majorVersion != "8" && majorVersion != "9") {
        selectFilter4.onclick = function () {
            updateFilterDropDownLists.call(this, selectFilter1, selectFilter2, selectFilter3, selectFilter4, selectFilter5);
        };
    }

    var previous5;
    selectFilter5.onfocus = function () {
        previous5 = this.value;
    };
    selectFilter5.onchange = function () {
        createElementsBasedOnFieldType('5', previous5, "");
        previous5 = this.value;
        updateFilterDropDownLists.call(this, selectFilter1, selectFilter2, selectFilter3, selectFilter4, selectFilter5);
    };

    if (majorVersion != "8" && majorVersion != "9") {
        selectFilter5.onclick = function () {
            updateFilterDropDownLists.call(this, selectFilter1, selectFilter2, selectFilter3, selectFilter4, selectFilter5);
        };
    }
}
