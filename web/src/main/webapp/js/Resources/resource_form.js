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

function onloadResourceForm() {
    PageLoadUtil.loadPage(".resourcelink", resourceFormPage.init, true);
}

var resourceFormPage = (function () {
    var initFn = function () {
        eraseLicense();
        app_runIdleTimer();
        commonData();

        var deferredTemplateLoading = $.Deferred();

        $('#templateSection').load("table_templates.html", function () {
            deferredTemplateLoading.resolve();
        });

        $.when(deferredTemplateLoading).done(function () {
            resourcePage.initDetails();
            createMainResourceForm();

            // must be called after form has been built b/c it will populate select elements
            // and make them into comboboxes
            // needs fixing: only bother with the top resource form (name etc.) when in NEW mode
            getResourceStaticLists(populateForm);
        });
    };

    var populateForm = function () {

        // populate main form with resource data if a resource is selected
        // Do this after the comboboxes have been set up
        if (app_selectedResource) {

            renderBreadcrumbs('resource_edit_resource_form');

            var indexOfSublocation = app_selectedResource.resource.indexOf(" - " + app_selectedResource.sublocation);
            indexOfSublocation = indexOfSublocation > 0 ? indexOfSublocation : app_selectedResource.resource.length;

            $('#resourceNameInput').val(app_selectedResource.resource.substring(0, indexOfSublocation));
            $("#resource_selected_resource_name").text(app_selectedResource.resource);
            $('#resourceSublocationSelect').combobox("setValue", app_selectedResource.sublocationId);
            $('#resourceTypeName').text(app_selectedResource.resourceType);

            TemporaryAdjustment.init();

            Annotations.init();

            ResourceRestrictions.init();

            DefaultAvailability.init();

            AlternateResources.init();

            // Initialize visibility of all 5 sections
            // collapse all 5 sections of the page, and display the expand-all button
            hideResourceFormSection('resourceTabHeading1', 'resourceTabExpandIcon1', 'resourceTab1');
            hideResourceFormSection('resourceTabHeading2', 'resourceTabExpandIcon2', 'resourceTab2');
            hideResourceFormSection('resourceTabHeading3', 'resourceTabExpandIcon3', 'tempAdjustmentContent');
            hideResourceFormSection('resourceTabHeading4', 'resourceTabExpandIcon4', 'resourceTab4');
            hideResourceFormSection('resourceTabHeading5', 'resourceTabExpandIcon5', 'resourceTab5');
            $("#resourceDetailToggle").show();
        }
        else {

            renderBreadcrumbs('resource_new_resource_form');

            $("#resourceDetailToggle").hide();

            $("#defaultAvailabilitySection").hide();
            $("#temporaryAdjustmentSection").hide();
            $("#alternateResourcesSection").hide();
            $("#resourceRestrictionsSection").hide();
            $("#annotationsSection").hide();
        }
    };
    return {
        init: initFn,
        // expose populateForm for testability
        populateForm: populateForm
    };
}());

var TemporaryAdjustment = {
    init: function () {
        var self = this;

        if (UserRoleUtil.userIsSuperAdminOrResourceManager()) {
            var tempAdjustActionsOut = "";
            tempAdjustActionsOut += '<input type="button" id="taResetButton" value="Restore" onclick="TemporaryAdjustment.restoreTaRangeForm()" class="formButton">';
            tempAdjustActionsOut += '<input type="button" id="taAddButton" value="Save" onclick="TemporaryAdjustment.addTemporaryAdjustmentRange()" class="formButton">';
            $("#addTempAdjustmentAction").html(tempAdjustActionsOut);

            $("#addTempAdjustments").show();
            //sets up the temp adjustment form widgets (for adding ranges):
            var autoCorrectDate = false;
            var display59 = true;
            appt_createSearchDateTimeWidgets("#addTempAdjustmentsTable", display59, autoCorrectDate, true);

            $(".weekday").on("change", function () {
                toggleWeekDaysCheckBoxes();
            });

            $(".weekend").on("change", function () {
                customCheckbox('weekend');
            });

            self.restoreTaRangeForm();

            $("#taQuantity").spinner({
                min: 0
            });
        }

        TemporaryAdjustmentTable.createTable();
        TemporaryAdjustmentTable.populateTable();
    },
    restoreTaRangeForm: function () {
        resetCheckBoxes();

        $('#ta_overnight_no').click();
        $("#appt_startDate").datepicker('setDate', new Date());
        $("#appt_endDate").datepicker("option", "minDate", $("#appt_startDate").datepicker('getDate'));
        $("#appt_endDate").datepicker('setDate', new Date());

        var startDateDatepicker = $("#appt_startDate").datepicker('getDate');
        adjustSpecificDayCheckboxes(startDateDatepicker, "#addTempAdjustmentsTable");

        $('#addTempAdjustmentsTable .formElementRequired').removeAttr("style");
        $('#addTempAdjustmentsTable .formElementRequired').css({display: "none"});

        $("#appt_startTime").timepicker('setTime', '24:00').mask("99:99");
        $('#appt_endTime').timepicker('setTime', '24:00').mask("99:99");

        $('#taQuantity').val("");
    },
    resource_ta_clearErrors: function (validationElements) {
        $(validationElements.startDateValidation).css({display: "none"});
        $(validationElements.endDateValidation).css({display: "none"});
        $(validationElements.startTimeValidation).css({display: "none"});
        $(validationElements.endTimeValidation).css({display: "none"});
        $(validationElements.quantityValidation).css({display: "none"});
        $(validationElements.dayOfWeekValidation).css({display: "none"});
    },
    _validateTempAdjustmentFormData: function (formData, spanOvernight) {
        var formValidationResult = {
            validStartDate: true,
            validEndDate: true,
            validStartTime: true,
            validEndTime: true,
            validQuantity: true,
            validDayOfWeek: true
        };

        if (formData.startDate.toString() === "Invalid Date") {
            formValidationResult.validStartDate = false;
        }

        if (formData.endDate.toString() === "Invalid Date") {
            formValidationResult.validEndDate = false;
        }

        if (formData.startTime.toString() === "Invalid Date") {
            formValidationResult.validStartTime = false;
        }

        if (formData.endTime.toString() === "Invalid Date") {
            formValidationResult.validEndTime = false;
        }

        var datesValid = formValidationResult.validStartDate && formValidationResult.validEndDate;
        formData.startDate.setHours(0);
        formData.startDate.setMinutes(0);
        formData.endDate.setHours(0);
        formData.endDate.setMinutes(0);

        if (datesValid && formData.startDate.getTime() > formData.endDate.getTime()) {
            formValidationResult.validEndDate = false;
        }

        if (!spanOvernight) {
            // normalize end month before normalizing day of the month. Otherwise we may attempt to set
            // day of the month greater than the number of days in the month.
            formData.endTime.setMonth(formData.startTime.getMonth());
            formData.endTime.setDate(formData.startTime.getDate());
            formData.endTime.setFullYear(formData.startTime.getFullYear());

            if (formData.dayOfWeekCount == 0) {
                formValidationResult.validDayOfWeek = false;
            }
        }

        var timesValid = formValidationResult.validStartTime && formValidationResult.validEndTime;
        if (timesValid && formData.startTime.getTime() >= formData.endTime.getTime()) {
            formValidationResult.validEndTime = false;
        }

        if (formData.quantity === "") {
            formValidationResult.validQuantity = false;
        }

        return formValidationResult;
    },
    _showError: function (element, message) {
        showErrorInValidationSpan(element, message);
    },
    validateSaveTempAdjustment: function (formElements, validationElements, spanOvernight) {
        this.resource_ta_clearErrors(validationElements);

        var validationResult = this._validateTempAdjustmentFormData(formElements, spanOvernight);

        var isValid = true;
        if (!validationResult.validStartDate) {
            this._showError(validationElements.startDateValidation, 'Please enter a valid start date');
            isValid = false;
        }

        if (!validationResult.validEndDate) {
            this._showError(validationElements.endDateValidation, 'Please enter a valid end date');
            isValid = false;
        }

        if (!validationResult.validStartTime) {
            this._showError(validationElements.startTimeValidation, 'Please enter a valid start time');
            isValid = false;
        }

        if (!validationResult.validEndTime) {
            this._showError(validationElements.endTimeValidation, 'Please enter a valid end time');
            isValid = false;
        }

        if (!validationResult.validQuantity) {
            this._showError(validationElements.quantityValidation, 'Please enter a quantity');
            isValid = false;
        }

        if (!validationResult.validDayOfWeek) {
            this._showError(validationElements.dayOfWeekValidation, 'Please specify a day of the week');
            isValid = false;
        }

        return isValid;
    },
    addTemporaryAdjustmentRange: function () {
        var resource_ta_startDateVal = new Date($("#appt_startDate").val());
        var resource_ta_endDateVal = new Date($("#appt_endDate").val());

        var resourceStartTime = $("#appt_startTime").val() !== "" ? $("#appt_startTime").val() : null;
        var resourceEndTime = $("#appt_endTime").val() !== "" ? $("#appt_endTime").val() : null;

        var resource_ta_startDateTimeVal = new Date($("#appt_startDate").val() + " " + resourceStartTime);
        var resource_ta_endDateTimeVal = new Date($("#appt_endDate").val() + " " + resourceEndTime);
        var quantity = $('#taQuantity').val();

        var formElements = {
            startDate: resource_ta_startDateVal,
            endDate: resource_ta_endDateVal,
            startTime: resource_ta_startDateTimeVal,
            endTime: resource_ta_endDateTimeVal,
            quantity: quantity,
            dayOfWeekCount: $("#weeklyCheckboxes .selected").length
        };

        var validationElements = {
            startDateValidation: "#resource_ta_startDateValidation",
            endDateValidation: "#resource_ta_endDateValidation",
            startTimeValidation: "#resource_ta_startTimeValidation",
            endTimeValidation: "#resource_ta_endTimeValidation",
            quantityValidation: "#resource_ta_quantityValidation",
            dayOfWeekValidation: "#dayOfWeekValidation",
        };

        var spanOvernight = $("#ta_overnight_yes").is(":checked");

        if (!this.validateSaveTempAdjustment(formElements, validationElements, spanOvernight)) {
            return;
        }

        var daysOfWeek = getSelectedDaysOfWeekFromCheckboxes();


        var dataPacketsForCalls = [];
        if (spanOvernight) {
            //create one temp adjustment with the specified start and end date
            jsonData = JSON.stringify({
                id: 0,
                resourceId: app_selectedResource.resourceId,
                startDate: resource_ta_startDateTimeVal.valueOf(),
                endDate: resource_ta_endDateTimeVal.valueOf(),
                quantity: $('#taQuantity').val(),
                override: true
            });

            dataPacketsForCalls.push(jsonData);
        }
        else {
            //create multiple temp adjustments within the specified start and end date
            dataPacketsForCalls = this.getTempAdjustmentRangeDataPackets(daysOfWeek);
        }

        var deferments = [];
        for (var i = 0; i < dataPacketsForCalls.length; i++) {
            deferments.push($.Deferred());
        }

        for (i = 0; i < dataPacketsForCalls.length; i++) {
            var jsonData = dataPacketsForCalls[i];
            var url = "rest/resource/addTemporaryAdjustment";

            //need an iife/closure so the index keeps it's value;
            //otherwise the index for ALL the functions is set to i+1
            //(if there are 6 data packets, then the index =6 for all of them,
            //throwing an error
            var doPost = (function () {
                var index = i;
                $.post(url, {data: jsonData}, function () {
                    deferments[index].resolve();
                });
            })();
        }

        //when all the deferments are done (because all the posts went through)
        $.when.apply(null, deferments).done(function () {
            util_showMainMessage("Temporary Adjustments added");
            TemporaryAdjustment.restoreTaRangeForm();
            TemporaryAdjustmentTable.populateTable();
        });
    },
    getTempAdjustmentRangeDataPackets: function (daysOfWeek) {
        //return dataPackets for list of calls to make
        var dataPackets = [];
        var search_startDate = $("#appt_startDate").datepicker('getDate');
        var search_endDate = $("#appt_startDate").datepicker('getDate');
        var timeDiff = Math.abs($("#appt_endDate").datepicker('getDate').getTime() - $("#appt_startDate").datepicker('getDate').getTime());
        var totalDays = Math.ceil(timeDiff / (1000 * 3600 * 24)) + 1;

        //go through each day:

        var startDay = $("#appt_startDate").datepicker('getDate');
        var nextDay = $("#appt_startDate").datepicker('getDate');
        for (var i = 0; i < totalDays; i++) {
            var startDay;
            if (i > 0) {
                nextDay = getNextDay(startDay);
            }

            search_startDate = nextDay;
            search_endDate = nextDay;

            search_startTimeVal = $("#appt_startTime").timepicker().val();
            search_endTimeVal = $("#appt_endTime").timepicker().val();

            var search_startDateTimeVal = search_startDate.toDateString() + ' ' + search_startTimeVal;
            search_startDate = new Date(search_startDateTimeVal);

            var search_endDateTimeVal = search_endDate.toDateString() + ' ' + search_endTimeVal;
            search_endDate = new Date(search_endDateTimeVal);

            if ($.inArray(startDay.getDay().toString(), daysOfWeek) != -1) {
                //if it's a valid day:
                jsonData = JSON.stringify({
                    id: 0,
                    resourceId: app_selectedResource.resourceId,
                    startDate: search_startDate.valueOf(),
                    endDate: search_endDate.valueOf(),
                    quantity: $('#taQuantity').val(),
                    override: true
                });

                dataPackets.push(jsonData);
            }

            startDay = nextDay;
        }
        return dataPackets;

        function getNextDay(startDay) {
            var nextDay = startDay;
            nextDay.setDate(nextDay.getDate() + 1);
            return nextDay;
        }
    },
    toggleWeeklyCheckboxes: function (hide) {
        if (hide) {
            $("#weeklyCheckboxes").css({visibility: "hidden"});
        }
        else {
            $("#weeklyCheckboxes").css({visibility: "visible"});
        }
    },
    // expose 'now' for testability
    now: null
};

var Annotations = {
    annotationsList: null,

    init: function () {
        if (UserRoleUtil.userIsSuperAdminOrResourceManager()) {
            Annotations._buildAnnotationActions();
        }
        else {
            Annotations._disableAnnotations();
        }
        Annotations.annotationsList = new Array();
        Annotations.highlightAnnotationsAlreadyAdded();
        Annotations.expandOrCollapseAllAnnotationSections();
    },

    _disableAnnotations: function () {
        $.each($(".annotationNode"), function (index, annotation) {
            $(annotation).prop('onclick', null);
        });
    },

    _buildAnnotationActions: function () {
        var nurseSelect = "<a id='clearOrSelectNursingLink' class='annotationsSelectLink' " +
            "onclick='Annotations.selectAllAnnotationsOfType(\"nurse\");'>Select All Nursing Annotations</a>";
        $("#clearOrSelectNursingAnnotations").html(nurseSelect);

        var nutritionSelect = "<a id='clearOrSelectNutritionLink' class='annotationsSelectLink' " +
            "onclick='Annotations.selectAllAnnotationsOfType(\"nutrition\");'>Select All Nutrition Annotations</a>";
        $("#clearOrSelectNutritionAnnotations").html(nutritionSelect);

        var equipmentSelect = "<a id='clearOrSelectEquipmentLink' class='annotationsSelectLink' " +
            "onclick='Annotations.selectAllAnnotationsOfType(\"equipment\");'>Select All Equipment Annotations</a>";
        $("#clearOrSelectEquipmentAnnotations").html(equipmentSelect);

        var saveTopButton = "<input type='button' id='annotationsSaveTop' class='formButton annotationsSave' " +
            "value='Save' onclick='Annotations.addAnnotationsToResource()'>";
        $("#saveAnnotationsTop").html(saveTopButton);

        var saveBottomButton = "<input type='button' id='annotationsSaveBottom' class='formButton annotationsSave' " +
            "value='Save' onclick='Annotations.addAnnotationsToResource()'>";
        $("#saveAnnotationsBottom").html(saveBottomButton);

        var bottomToggle = "<span id='clearAnnotationsBottom' class='clearSelectedFieldsLink'>" +
            "<a onclick='Annotations.deSelectAll();'> Clear All Field Selections </a></span>";
        $("#annotationBottomToggle").html(bottomToggle);

        var topToggle = "<a onclick='Annotations.deSelectAll();'> Clear All Field Selections </a>";
        $("#clearAnnotationsTop").html(topToggle)
    },

    highlightAnnotationsAlreadyAdded: function () {
        $.getJSON("rest/resource/getResourceAnnotations?resourceId=" + app_selectedResource.resourceId, function (data) {
            $.each(data.resourceAnnotationsValues, function (index, resourceAnnotation) {
                $(".node-" + resourceAnnotation.id).addClass("savedAnnotation").removeAttr('onclick');
            });
        });
    },

    selectOrDeselectNode: function (element) {
        var annotations;
        var theJQueryElement = $(element);
        if (theJQueryElement.hasClass("selectedChildNode") && !theJQueryElement.hasClass("savedAnnotation")) {
            annotations = $(".selectedChildNode");
            for (var i = 0; i < annotations.length; i++) {
                if ($(annotations[i]).val() === theJQueryElement.val()) {
                    $(annotations[i]).removeClass("selectedChildNode").addClass("childNode");
                }
            }
            theJQueryElement.removeClass("selectedChildNode").addClass("childNode");
            Annotations.annotationsList.splice($.inArray(theJQueryElement.val(), Annotations.annotationsList), 1);
        }
        else if (theJQueryElement.hasClass("childNode") && !theJQueryElement.hasClass("savedAnnotation")) {
            annotations = $(".childNode");
            for (var j = 0; j < annotations.length; j++) {
                if ($(annotations[j]).val() === theJQueryElement.val()) {
                    $(annotations[j]).addClass("selectedChildNode").removeClass("childNode");
                }
            }
            theJQueryElement.addClass("selectedChildNode").removeClass("childNode");
            Annotations.annotationsList.push(parseInt(theJQueryElement.val()));
        }
        if ($(".nurseNode.selectedChildNode").length > 0) {
            $("#clearOrSelectNursingLink").attr('onclick', "Annotations.clearAllAnnotationsOfType('nurse');").text("Deselect All Nursing Annotations");
        }
        else {
            $("#clearOrSelectNursingLink").attr('onclick', "Annotations.selectAllAnnotationsOfType('nurse');").text("Select All Nursing Annotations");
        }
        if ($(".nutritionNode.selectedChildNode").length > 0) {
            $("#clearOrSelectNutritionLink").attr('onclick', "Annotations.clearAllAnnotationsOfType('nutrition');").text("Deselect All Nutrition Annotations");
        }
        else {
            $("#clearOrSelectNutritionLink").attr('onclick', "Annotations.selectAllAnnotationsOfType('nutrition');").text("Select All Nutrition Annotations");
        }
        if ($(".equipmentNode.selectedChildNode").length > 0) {
            $("#clearOrSelectEquipmentLink").attr('onclick', "Annotations.clearAllAnnotationsOfType('equipment');").text("Deselect All Equipment Annotations");
        }
        else {
            $("#clearOrSelectEquipmentLink").attr('onclick', "Annotations.selectAllAnnotationsOfType('equipment');").text("Select All Equipment Annotations");
        }
        Annotations.removeError();
    },

    expandOrCollapseAllAnnotationSections: function () {
        if ($('#toggleAllAnnotationSections').attr('title') == 'Expand') {
            Annotations.displayAnnotationSection("nurse");
            Annotations.displayAnnotationSection("nutrition");
            Annotations.displayAnnotationSection("equipment");
            Annotations.displayAnnotationSection("room");
            Annotations.displayAnnotationSection("lab");
        } else {
            Annotations.hideAnnotationSection("nurse");
            Annotations.hideAnnotationSection("nutrition");
            Annotations.hideAnnotationSection("equipment");
            Annotations.hideAnnotationSection("room");
            Annotations.hideAnnotationSection("lab");
        }
    },

    displayOrHideSection: function (section) {
        if ($('#' + section + 'Heading').attr('title') == 'Collapse') {
            Annotations.hideAnnotationSection(section);
        }
        else {
            Annotations.displayAnnotationSection(section);
        }
    },

    displayAnnotationSection: function (section) {
        $('#' + section + 'Heading').attr('title', 'Collapse').css({cursor: 'pointer'});
        $('#' + section + 'Icon').attr('src', 'css/images/sm_circle_minus.png');
        $('#' + section + 'AnnotationsSection').css({display: 'block', cursor: 'pointer'});
        Annotations._expandOrCollapse();
    },

    hideAnnotationSection: function (section) {
        $('#' + section + 'Heading').attr('title', 'Expand').css({cursor: 'pointer'});
        $('#' + section + 'Icon').attr('src', 'css/images/sm_circle_plus.png');
        $('#' + section + 'AnnotationsSection').css({display: 'none', cursor: 'default'});
        Annotations._expandOrCollapse();
    },

    _expandOrCollapse: function () {
        var annotationHeaders = $(".annotationsHeader");
        for (var i = 0; i < annotationHeaders.length; i++) {
            if ($(annotationHeaders[i]).attr('title') == 'Collapse') {
                Annotations._collapseAll();
                return;
            }
        }
        Annotations._expandAll();
    },

    _expandAll: function () {
        $('#toggleAllAnnotationSections').attr('title', 'Expand');
        $('#toggleAllAnnotationSectionsImg').attr('src', 'css/images/arrows_expand.png');
        $('#toggleAnnotationText').text("Expand All");
    },

    _collapseAll: function () {
        $('#toggleAllAnnotationSections').attr('title', 'Collapse');
        $('#toggleAllAnnotationSectionsImg').attr('src', 'css/images/arrows_collapse.png');
        $('#toggleAnnotationText').text("Collapse All");
    },

    selectAllAnnotationsOfType: function (type) {
        $.each($("." + type + "Node.childNode"), function (index, nonSelectedAnnotationsOfType) {
            Annotations.selectOrDeselectNode(nonSelectedAnnotationsOfType);
        });
    },

    clearAllAnnotationsOfType: function (type) {
        $.each($("." + type + "Node.selectedChildNode"), function (index, aSelectedAnnotationOfType) {
            Annotations.selectOrDeselectNode(aSelectedAnnotationOfType);
        });
    },

    deSelectAll: function () {
        $.each($(".selectedChildNode"), function (index, nonSelectedAnnotations) {
            if (!$(nonSelectedAnnotations).hasClass("childNode")) {
                Annotations.selectOrDeselectNode(nonSelectedAnnotations);
            }
        });
    },

    displayError: function () {
        $('#annotationValidationTop').text('Please select at least one annotation.')
            .css({opacity: 0.0, visibility: "visible"}).animate({opacity: 1.0});
        $('#annotationValidationBottom').text('Please select at least one annotation.')
            .css({opacity: 0.0, visibility: "visible"}).animate({opacity: 1.0});
    },

    removeError: function () {
        $('#annotationValidationTop').text('').css({visibility: "hidden"});
        $('#annotationValidationBottom').text('').css({visibility: "hidden"});
    },

    addAnnotationsToResource: function () {
        if (Annotations.annotationsList.length > 0) {
            DialogsUtil.showConfirmationDialog("#annotations-add-dialog-confirm", {
                buttons: {
                    "Yes": function () {
                        jsonData = JSON.stringify(
                            {resourceId: app_selectedResource.resourceId, annotationIds: Annotations.annotationsList});
                        $.post("rest/resource/addAnnotations", {data: jsonData}, function (data) {
                            var parsedData = JSON.parse(data);
                            var confirmationMessage = "";
                            if (parsedData.result == true) {
                                Annotations.deSelectAll();
                                Annotations.highlightAnnotationsAlreadyAdded();
                                confirmationMessage = "Annotations successfully activated!";
                            }
                            else {
                                confirmationMessage = parsedData.errorMsg;
                            }
                            util_showMainMessage(confirmationMessage);
                        });
                        $(this).dialog("close");
                    },
                    "No": function () {
                        $(this).dialog("close");
                    }
                }
            });
        }
        else {
            Annotations.displayError();
        }
    },
    populateAnnotations: function(parsedData) {
        var tmpl = $.templates("#nurseAnnotationsListTemplate");
        var renderedTemplate = tmpl.render({annotations: parsedData.nurseAnnotations});
        $('#nurseAnnotationList').html(renderedTemplate);

        var tmpl2 = $.templates("#nutritionAnnotationsListTemplate");
        var renderedTemplate2 = tmpl2.render({annotations: parsedData.nutritionAnnotations});
        $('#nutritionAnnotationList').html(renderedTemplate2);

        var tmpl3 = $.templates("#equipmentAnnotationsListTemplate");
        var renderedTemplate3 = tmpl3.render({annotations: parsedData.equipmentAnnotations});
        $('#equipmentAnnotationList').html(renderedTemplate3);

        var tmpl4 = $.templates("#roomAnnotationsListTemplate");
        var renderedTemplate4 = tmpl4.render({annotations: parsedData.roomAnnotations});
        $('#roomAnnotationList').html(renderedTemplate4);

        var tmpl5 = $.templates("#labAnnotationsListTemplate");
        var renderedTemplate5 = tmpl5.render({annotations: parsedData.labAnnotations});
        $('#labAnnotationList').html(renderedTemplate5);
    }
};

var ResourceRestrictions = {
    init: function() {
        ResourceRestrictionsTable.createTable();
        ResourceRestrictionsTable.populateTable();
    }
};

var DefaultAvailability = {
    init: function(){
        DefaultAvailabilityTable.createTable();
        DefaultAvailabilityTable.populateTable();
    }
};

var AlternateResources = {
    init: function(){
        AlternateResourcesTable.createTable();
        AlternateResourcesTable.populateTable();
    }
};
