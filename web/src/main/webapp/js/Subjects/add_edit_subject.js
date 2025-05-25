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
 * Created by marc-danie on 6/30/16.
 */

var subjectFormPage = (function () {
    var readOnlyMode = true;
    var fromStudyPage = false;
    var fromViewPage = false;

    var currentBreadcrumb = "";

    function getFromViewPage() {
        return fromViewPage;
    }

    function setFromViewPage(viewPage) {
        fromViewPage = viewPage;
    }

    function setFromStudyPage(studyPage) {
        fromStudyPage = studyPage;
    }

    function getFromStudyPage() {
        return fromStudyPage;
    }

    function setCurrentBreadcrumb(crumbString) {
        currentBreadcrumb = crumbString;
    }

    function getCurrentBreadcrumb() {
        return currentBreadcrumb;
    }

    var initFn = function () {

        var subjectId = sessionStorage.getItem("selectedSubjectId");
        var isPuid = sessionStorage.getItem("selectedSubjectByPuid") == "true";
        var subjectMrn = sessionStorage.getItem("selectedSubjectMrn");
        var subjectInstitution = sessionStorage.getItem("selectedSubjectInstitution");
        var origin = getURLParameter("origin");

        getStudyStaticLists();

        var fromStudyPage = true;
        var readOnly = true;
        if (origin && origin === "subjects") {
            fromStudyPage = false;

            var mode = getURLParameter("mode");
            readOnly = mode == "view";
        }

        setReadOnlyMode(readOnly);
        setFromStudyPage(fromStudyPage);

        isEpicMode = sessionStorage.getItem("subjectMode").toLowerCase() == "epic";

        SubjectDetails.loadSubjectEntryForm(subjectId, isPuid, subjectMrn, subjectInstitution);
    };

    var switchToEditMode = function () {
        isEditMode = true;
        setReadOnlyMode(false);

        var currentLocation = window.location.href;
        if (currentLocation.indexOf("?origin=subjects") == -1) {
            currentLocation = currentLocation + "?origin=subjects";
        }
        currentLocation = currentLocation.replace("&mode=view", "");
        currentLocation = currentLocation.replace("?mode=view", "");
        history.pushState("", "", currentLocation);

        setFromViewPage(true);
        var newBreadCrumb = 'subject_edit_form';
        if (subjectFormPage.getCurrentBreadcrumb() === 'subject_view_form') {
            subjectFormPage.setCurrentBreadcrumb(newBreadCrumb);
            newBreadCrumb = 'subject_edit_form';
        } else if (subjectFormPage.getFromStudyPage()) {
            newBreadCrumb = 'study_details_subjects_detail_edit';
            subjectFormPage.setCurrentBreadcrumb(newBreadCrumb);
            renderBreadcrumbs(newBreadCrumb);
        }

        renderBreadcrumbs(newBreadCrumb);
        SubjectDetails.reRenderPage();
    };

    function isReadOnlyMode() {
        return readOnlyMode;
    }

    function setReadOnlyMode(state) {
        readOnlyMode = state;
    }

    return {
        init: initFn,
        switchToEditMode: switchToEditMode,
        isReadOnlyMode: isReadOnlyMode,
        setReadOnlyMode: setReadOnlyMode,
        getFromStudyPage: getFromStudyPage,
        setFromStudyPage: setFromStudyPage,
        isFromViewPage: getFromViewPage,
        setFromViewPage: setFromViewPage,
        setCurrentBreadcrumb: setCurrentBreadcrumb,
        getCurrentBreadcrumb: getCurrentBreadcrumb
    };
}());

function displayBreadCrumbs(){
    if (subjectFormPage.getFromStudyPage()) {
        renderBreadcrumbs('study_details_subjects_detail');
    } else if (isEditMode) {
        renderBreadcrumbs('subject_edit_form');
    } else if (subjectFormPage.isReadOnlyMode()) {
        renderBreadcrumbs('subject_view_form');
    }
    else {
        renderBreadcrumbs('subject_create_form');
    }
}

function setStudyComboboxValue() {
    var studyVal = "";
    var studyToAdd = sessionStorage.getItem("selectedStudyIdForAddingMember");
    if (studyToAdd) {
        studyVal = studyToAdd;
        sessionStorage.removeItem("selectedStudyIdForAddingMember");
    }

    $("#subject_study").combobox("setValue", studyVal);
}

function getSubjectStatus(active) {
    if (!active || active === "false") {
        return "uncheck";
    } else {
        return "check";
    }
}

function setEditVisibility() {
    $('.view_edit_page .readOnly').hide();
    $('#edit_subject_btn').hide();

    $('.view_edit_page .editOnly').show();

    if (isEpicMode) {
        //hide the required asterisk next to the form labels
        $('.view_edit_page .requiredIcon').hide();

        $('.view_edit_page .epicRequiredIcon').show();
    }
    else {
        $('.view_edit_page .requiredIcon').show();
        $('#subject_header .requiredText').show();
    }
    $('#subject_form_buttons .formButton').show();
    $('#subject_form_buttons .newItemButton').show();
}

function clearSubjectErrors() {
    $('.formElementRequired').css({visibility: "hidden"});
}

function restoreSubjectClick() {
    clearSubjectErrors();

    if (isEpicMode) {
        ExternalSubjectDetails.populateSubjectEditForm(app_selectedSubject);
    }
    else {
        StandaloneSubjectDetails.populateSubjectEditForm(app_selectedSubject);
    }
}

function saveSubjectClick() {
    if (!SubjectDetails.validSubjectEntryForm()) {
        return;
    }

    var subjectDataJsonStr = null;
    if (!isEpicMode) {
        subjectDataJsonStr = StandaloneSubjectDetails.retrieveFormJsonString();
    }
    else {
        subjectDataJsonStr = ExternalSubjectDetails.retrieveFormJsonString();
    }

    var isEditSubject = app_selectedSubject != null && app_selectedSubject.id != null;

    SubjectDetails.addOrEditSubject(subjectDataJsonStr, isEditSubject, SubjectDetails.handleSaveSuccess, SubjectDetails.handleSaveFailure);
}

function cancelSubjectSaveClick() {
    ExternalSubjectDetails.clearWarnings();

    if (subjectFormPage.getFromStudyPage()) {
        //go back to view mode:

        subjectFormPage.setReadOnlyMode(true);

        renderBreadcrumbs('study_details_subjects_detail');
        subjectFormPage.setCurrentBreadcrumb('study_details_subjects_detail');

        SubjectDetails.reRenderPage();
    }
    else if (subjectFormPage.isFromViewPage()) {
        //go back to view mode:

        subjectFormPage.setReadOnlyMode(true);

        renderBreadcrumbs('subject_view_form');
        subjectFormPage.setCurrentBreadcrumb('subject_view_form');

        SubjectDetails.reRenderPage();
    }
    else {
        //set flag whether previous search should be reloaded
        if (isEditMode) {
            sessionStorage.setItem("reloadPreviousSearch", true);
        }
        //go back to the subject search page
        subjectModule();
    }
}

var SubjectDetails = {};

SubjectDetails.loadSubjectEntryForm = function(subjectId, isPuid, subjectMrn, subjectInstitution) {
    if (isEpicMode && MiscUtil.isNotUndefinedOrNull(subjectMrn) && MiscUtil.isNotUndefinedOrNull(subjectInstitution)) {
        getExternalSubjectData(subjectMrn, subjectInstitution, function(subjectData){
            SubjectDetails.processSubject(subjectData, subjectMrn, subjectInstitution)
        });
    }
    else if (MiscUtil.isNotUndefinedOrNull(subjectId)) {
        getSubjectData(subjectId, isPuid, function(subjectData) {
            SubjectDetails.processSubject(subjectData, subjectMrn, subjectInstitution);
        });
    }
    else {
        $.blockUI({ignoreIfBlocked: true});
        subjectFormPage.setReadOnlyMode(false);
        SubjectDetails.renderMain();
    }
};

SubjectDetails.processSubject = function(subjectData, subjectMrn, subjectInstitution) {

    //if no subject data was returned then redirect to the subject search form
    if (!subjectData.id) {
        subjectModule();
    }

    subjectData = SubjectDetails.cleanupSubjectData(subjectData);

    app_selectedSubject = subjectData;

    $.blockUI({ignoreIfBlocked: true});
    if (isEpicMode) {
        ExternalSubjectDetails.processSubject(subjectData, subjectMrn, subjectInstitution);
    }
    else {
        if (subjectData.mrnInfoList.length > 0) {
            subjectData.mrnInfo = subjectData.mrnInfoList[0];
        }
        SubjectDetails.renderMain();
    }
};


SubjectDetails.renderMain = function () {
    isEditMode = app_selectedSubject != null && !subjectFormPage.isReadOnlyMode();

    //do not allow creation of new subject in epic mode
    if (isEpicMode && !isEditMode && !subjectFormPage.isReadOnlyMode()) {
        subjectModule();
    }

    var deferredGetSubjectStaticList = $.Deferred();
    var deferredGetNonClosedStudiesList = $.Deferred();

    getSubjectStaticLists(function () {
        deferredGetSubjectStaticList.resolve();
    });

    getNonClosedStudiesList(function () {
        deferredGetNonClosedStudiesList.resolve();
    });

    $.when(deferredGetSubjectStaticList, deferredGetNonClosedStudiesList).done(function () {
        displayBreadCrumbs();
        SubjectDetails.renderBody(app_selectedSubject, subjectFormPage.isReadOnlyMode());
    });
};

SubjectDetails.renderBody = function(subjectData, readOnlyMode) {

    $.blockUI({ignoreIfBlocked: true});

    $('#subject_name_display').text(buildFullName(subjectData));

    if (readOnlyMode) {
        SubjectDetails.renderReadOnly(subjectData);
    } else {
        setEditVisibility();
        SubjectDetails.renderEditablePage();
    }
    $('#subject_details_content').css("visibility", "visible");

    $.unblockUI();
};

SubjectDetails.renderReadOnly = function(subjectData) {
    $('#addToStudy').remove();

    SubjectDetails.setReadOnlyVisibility();
    SubjectDetails.populateReadOnlyData(subjectData);
};

SubjectDetails.fillReadOnlyValues = function(subjectData){
    $("#subject_firstName_readOnly").text(subjectData.firstName);

    $("#subject_middleName_readOnly").text(subjectData.middleName);

    $("#subject_lastName_readOnly").text(subjectData.lastName);

    if(subjectData.address) {
        $("#subject_streetAddress1_readOnly").text(subjectData.address.addressLine1);

        $("#subject_streetAddress2_readOnly").text(subjectData.address.addressLine2);

        $("#subject_city_readOnly").text(subjectData.address.city);

        $("#subject_state_readOnly").text(subjectData.address.stateName);

        $("#subject_zipcode_readOnly").text(subjectData.address.zipCode);

        $("#subject_country_readOnly").text(subjectData.address.countryName);
    }

    $("#subject_dob_readOnly").text(showDate(subjectData.birthdate));

    $("#subject_gender_readOnly").text(subjectData.genderName);

    $("#subject_race_readOnly").text(subjectData.raceName);

    $("#subject_ethnicity_readOnly").text(subjectData.ethnicityName);

    $("#subject_primaryContactNumber_readOnly").text(unifiedPhoneNumbersFormat(subjectData.primaryContactNumber));

    $("#subject_secondaryContactNumber_readOnly").text(unifiedPhoneNumbersFormat(subjectData.secondaryContactNumber));


    $("#subject_comment_readOnly").text(subjectData.comment);

    var mrnInfoValue = null;
    if (subjectData.mrnInfo !== undefined) {
        mrnInfoValue =   subjectData.mrnInfo.value;
    }

    $("#subject_mrn_readOnly").html(
        '<input type="text" id="subject_mrn_list" class="formTextInput" tabindex="8"/>');

    var options = {
        width: 170,
        height: comboboxHeight,
        prompt: "MRN"
    };

    renderSubjectMrnField($("#subject_mrn_list"), subjectData.mrnInfoList, mrnInfoValue, options);
};

SubjectDetails.reRenderPage = function(reloadFromDB, newId, selectedMrnInfo) {

    $.blockUI({ignoreIfBlocked: true});

    //rendering after switching between modes
    var readOnly = subjectFormPage.isReadOnlyMode();

    if (!readOnly) {
        //it's in edit mode
        SubjectDetails.renderEditablePage();
        setEditVisibility();
        $.unblockUI();
    }
    //todo:  handle when switching back to read only mode
    // need to realize that the data may have changed
    else {
        //in read only mode

        $("#subject_form_title").text("Additional Details");

        //this was added dynamically, so remove:
        $('#addToStudy').remove();

        if (reloadFromDB) {
            //data changed; get the data from the db again, then show

            function afterDbRetrieval(subjectData) {
                if(selectedMrnInfo){
                    subjectData.mrnInfo = selectedMrnInfo;
                }
                else if(app_selectedSubject && app_selectedSubject.mrnInfo){
                    subjectData.mrnInfo = app_selectedSubject.mrnInfo;
                }

                SubjectDetails.reloadSubject(subjectData);
                app_selectedSubject = subjectData;
                SubjectDetails.renderBody(app_selectedSubject, readOnly);
                $.unblockUI();
            }

            var resolvedId = getNewId(app_selectedSubject, newId);

            getSubjectData(resolvedId, true, afterDbRetrieval);
        }
        else {
            //from a cancel
            SubjectDetails.setReadOnlyVisibility();
            $.unblockUI();
        }
    }

    function getNewId(subjectData, newId) {
        //get from subjectData first, if not there; then use the newd
        if (subjectData && subjectData.id) {
            return subjectData.id;
        }
        return newId;
    }
};

SubjectDetails.renderEditablePage = function() {

    if (isEditMode) {
        //Set the title of the subject form accordingly
        $("#subject_form_title").text("Edit Subject");
    }
    else {
        //Creating a new subject
        //Set the title of the subject form accordingly
        $("#subject_form_title").text("Create New Subject");

        //hide the restore button
        $("#subject_restore_btn").hide();
    }

    SubjectDetails.initComboboxes();

    if (isEditMode) {
        // move the elements in the last column down one row
        // and insert the available to add to study toggle before
        $("#studyAreaLabel").insertAfter("#streetAddress2Area");
        $("#studyArea").insertAfter("#studyAreaLabel");
        $("#commentAreaLabel").insertAfter("#cityArea");
        $("#commentArea").insertAfter("#commentAreaLabel");
        var addToStudy = $("<td id='addToStudy' colspan='2'/>");
        addToStudy.append("<span class='addToStudyLabel formLabel'> Available to Add to Any Study</span>")
            .append("<input class='activateSubjectBtn' id = 'subject_status'/>");
        addToStudy.insertAfter("#streetAddress1Area");

        SubjectDetails.initStudyStatusToggle();

        if (isEpicMode) {
            ExternalSubjectDetails.initSubjectEditForm(app_selectedSubject);
        }
        else {
            StandaloneSubjectDetails.populateSubjectEditForm(app_selectedSubject);
        }
    }
    else {
        //populate any values that were set in the search fields
        //only if search failed to return results
        var emptySearch = sessionStorage.getItem("emptySearch") == "true";
        var searchMrn = sessionStorage.getItem("searchMrn");
        var searchLastName = sessionStorage.getItem("searchLastName");
        var searchFirstName = sessionStorage.getItem("searchFirstName");
        var searchDOB = sessionStorage.getItem("searchDOB");
        var searchGenders = sessionStorage.getItem("searchGenders");

        if (emptySearch) {
            if (searchMrn != undefined) {
                $("#subject_mrn").val(searchMrn);
            }

            if (searchLastName != undefined) {
                $("#subject_lastName").val(searchLastName);
            }

            if (searchFirstName != undefined) {
                $("#subject_firstName").val(searchFirstName);
            }

            if (searchDOB != undefined) {
                $("#subject_dob").val(searchDOB);
            }

            if (searchGenders != undefined && searchGenders.length == 1) {
                $("#subject_gender").combobox("setValue", searchGenders[0]);
            }
        }

    }
};

SubjectDetails.populateReadOnlyData = function(subjectData) {

    if (subjectData.empiFailoverMessage) {
        $("#subject_details_content_failoverMessage").html('<div class="badNewsMessage smaller">'
            + subjectData.empiFailoverMessage + '</div>');
    }

    SubjectDetails.fillReadOnlyValues(subjectData);
};

SubjectDetails.setReadOnlyVisibility = function() {
    $('.view_edit_page .editOnly').hide();
    $('.view_edit_page .requiredIcon').hide();
    $('#subject_header .requiredText').hide();

    $('#subject_form_buttons .formButton').hide();
    $('#subject_form_buttons .newItemButton').hide();

    $('.view_edit_page .readOnly').show();
    $('#edit_subject_btn').show();
};

SubjectDetails.reloadSubject = function(subjectData) {
    if (isEpicMode) {
        ExternalSubjectDetails.refreshSubject(subjectData)
    }
    else {
        sessionStorage.setItem("selectedSubject", JSON.stringify(subjectData));
    }
};

SubjectDetails.initComboboxes = function(){
    //no need to create comboboxes if in epic mode
    //if it's a create *or* is not in epic mode
    if (!(isEditMode && isEpicMode)) {

        $("#subject_gender").html(buildSelectOptions(genders, 'name'));

        WidgetUtil.createComboBox("#subject_gender", {
            width: 216,
            height: comboboxHeight,
            onBeforeLoad: MiscUtil.adjustSelectElemTabIndex
        });

        $("#subject_state").html(buildSelectOptions(states, 'name'));

        WidgetUtil.createComboBox("#subject_state", {
            width: 216,
            height: comboboxHeight,
            onBeforeLoad: MiscUtil.adjustSelectElemTabIndex
        });

        $("#subject_country").html(buildSelectOptions(countries, 'name'));
        WidgetUtil.createComboBox("#subject_country", {
            width: 215,
            height: comboboxHeight,
            onBeforeLoad: MiscUtil.adjustSelectElemTabIndex
        });

        $("#subject_race").html(buildSelectOptions(races, 'name'));
        WidgetUtil.createComboBox("#subject_race", {
            width: 216,
            height: comboboxHeight,
            onBeforeLoad: MiscUtil.adjustSelectElemTabIndex
        });

        $("#subject_ethnicity").html(buildSelectOptions(ethnicities, 'name'));
        WidgetUtil.createComboBox("#subject_ethnicity", {
            width: 216,
            height: comboboxHeight,
            onBeforeLoad: MiscUtil.adjustSelectElemTabIndex
        });

        WidgetUtil.createDatepicker("#subject_dob", {
            yearRange: "-120:+0",
            maxDate: "-1D",
            constrainInput: true
        });

        $("#subject_primaryContactNumber").mask("(999) 999-9999");
        $("#subject_secondaryContactNumber").mask("(999) 999-9999");
    }

    WidgetUtil.createComboBox("#subject_study", {
        width: 215,
        height: comboboxHeight,
        valueField: 'id',
        textField: 'truncatedLabel',
        data: studies,
        filter: function (userText, row) {
            var nonTruncatedFullText = row.localId + ' - ' + row.name;
            return caseInsensitiveContains(nonTruncatedFullText, userText);
        },
        onBeforeLoad: MiscUtil.adjustSelectElemTabIndex
    });
};

SubjectDetails.initStudyStatusToggle = function(){
    //set the tabindex to the same as the element below it
    $("#subject_status").attr("tabindex", $("#subject_study").attr("tabindex"));

    //render the switch toggle for available to study
    $("#subject_status").switchbutton({
        onText: "YES",
        offText: "NO",
        height: 22,
        checked: false,
        disabled: isStudyStaff(),
        onChange: function (checked) {
            if (checked) {
                //show a check mark if toggle is set to yes
                $(".switchbutton-handle").append("<img class='checkMark' src='css/images/check-mark-11.png'/>");

                //enable the add to study combobox
                $("#subject_study").combobox("enable");

                $(this).switchbutton("setValue", "on");
            }
            else {
                $(".switchbutton-handle").find(".checkMark").remove();
                $(this).switchbutton("setValue", "off");

                //remove any values set and disable the add to study combobox
                $("#subject_study").combobox("setValue", "");
                $("#subject_study").combobox("disable");
            }

            MiscUtil.adjustSwitchToggleTabIndex(this);

            //add a tooltip
            $(".switchbutton").attr("title", "Clicking this toggle will change the subject's availability for all studies.");
        }
    });

    // this is done to trigger the onChange event
    // since there is no onCreate event
    $("#subject_status").switchbutton("check");
};

SubjectDetails.cleanupSubjectData = function(subjectData){
    //replace undefined phone numbers with empty string
    subjectData.primaryContactNumber = subjectData.primaryContactNumber ? subjectData.primaryContactNumber : "";
    subjectData.secondaryContactNumber = subjectData.secondaryContactNumber ? subjectData.secondaryContactNumber : "";

    subjectData.address.addressLine1 = subjectData.address.addressLine1 ? subjectData.address.addressLine1 : "";
    subjectData.address.addressLine2 = subjectData.address.addressLine2 ? subjectData.address.addressLine2 : "";
    subjectData.address.city = subjectData.address.city ? subjectData.address.city : "";
    subjectData.address.stateName = subjectData.address.stateName ? subjectData.address.stateName : "";
    subjectData.address.zipCode = subjectData.address.zipCode ? subjectData.address.zipCode : "";
    subjectData.address.countryName = subjectData.address.countryName ? subjectData.address.countryName : "";
    subjectData.raceName = subjectData.raceName ? subjectData.raceName : "";

    return subjectData;
};

SubjectDetails.validSubjectEntryForm = function() {
    var isValid = true;
    clearSubjectErrors();

    if ($("#subject_study").length != 0 && $("#subject_study").combobox("getValue").length > 0 && !validComboboxValue($("#subject_study"), "id")) {
        showError('#subject_studyValidation', "Study not found");
        isValid = false;
    }

    if (!isEpicMode) {
        isValid = StandaloneSubjectDetails.isValidForm() && isValid;
    }
    else {    //epic mode only
        isValid = ExternalSubjectDetails.isValidForm() && isValid;
    }

    return isValid;
};

SubjectDetails.addOrEditSubject = function(subjectData, editMode, successCallback, failureCallback) {
    var url = "rest/subject/createSubject";

    if (editMode) {
        url = "rest/subject/updateSubject";
    }

    $("#subject_submit_btn").prop("disabled", true);

    var savingIndicator = setTimeout(function() {
        $.blockUI({ignoreIfBlocked: true, message:""});
    }, 3000);


    $.post(url, {data: subjectData}, function (data) {
        $("#subject_submit_btn").prop("disabled", false);
        $.unblockUI();
        clearTimeout(savingIndicator);
        var parsedData = JSON.parse(data);
        if (parsedData.result == false) {
            if (typeof failureCallback == 'function') {
                failureCallback(parsedData);
            }
        } else {
            if (typeof successCallback == 'function') {
                successCallback(parsedData);
            }
        }
    });
};

SubjectDetails.handleSaveSuccess = function (returnedData) {
    if (isEditMode) {
        var genderName = lookUpNameById(genders, returnedData.gender);
        genderName = genderName != null ? genderName : '';
        app_selectedSubject.id = returnedData.id;
        app_selectedSubject.comment = returnedData.comment;
        app_selectedSubject.genderName = genderName;
        app_selectedSubject.genderId = returnedData.gender;
        app_selectedSubject.mrnInfo = returnedData.mrnInfo;
        app_selectedSubject.active = returnedData.active;

        sessionStorage.setItem("selectedSubjectMrn", app_selectedSubject.mrnInfo.value);

        util_showMainMessage("Subject data saved successfully", SubjectDetails.afterSaveMessageForEdit);
    }
    else {
        //created a new subject

        //not relevant for read-only mode/from study page
        util_showMainMessage("Subject has been created successfully", function() {
            SubjectDetails.afterSaveMessageForCreate(returnedData);
        });
    }
};

SubjectDetails.afterSaveMessageForCreate = function (responseData) {
    subjectFormPage.setReadOnlyMode(true);
    var newId = null;
    var mrnInfo = null;
    if (responseData && responseData.id) {
        newId = responseData.id;
        mrnInfo = responseData.mrnInfo; //the selected mrn
    }
    renderBreadcrumbs('subject_view_form');
    subjectFormPage.setCurrentBreadcrumb('subject_view_form');
    isEditMode = false;
    SubjectDetails.reRenderPage(true, newId, mrnInfo);

};

SubjectDetails.afterSaveMessageForEdit = function () {
    sessionStorage.removeItem("searchLastName");
    sessionStorage.removeItem("searchFirstName");
    sessionStorage.removeItem("searchDOB");
    sessionStorage.removeItem("searchMrn");
    sessionStorage.removeItem("searchGenders");

    history.pushState("", "", window.location.href + "&mode=view");

    $("#subject_form_title").text("Additional Details");

    var newBreadCrumb = 'subject_view_form';

    if (subjectFormPage.getFromStudyPage()) {
        newBreadCrumb = 'study_details_subjects_detail';
    }
    renderBreadcrumbs(newBreadCrumb);
    subjectFormPage.setCurrentBreadcrumb(newBreadCrumb);

    subjectFormPage.setReadOnlyMode(true);

    if (!isEpicMode) {
        SubjectDetails.reRenderPage(true);
    } else {
        $("#warningMessageRow").hide().empty();

        SubjectDetails.renderBody(app_selectedSubject, true);
    }
};

SubjectDetails.handleSaveFailure = function (response) {
    if (response.mrnAlreadyExistsError) {
        showError('#subject_mrnValidation', "MRN already exists");
    }
    else if(response.similarMrnAlreadyExistsError) {
        ExternalSubjectDetails.displaySimilarMrnAlertBox(response.similarMrnInfo);
    }
    else if (response.errorMsg) {
        util_showMainMessage(response.errorMsg);
    }
};


var ExternalSubjectDetails = {};

ExternalSubjectDetails.initSubjectEditForm = function (data) {
    //add the gender drop down
    $("#subject_gender").html(buildSelectOptions(genders, 'name'));
    WidgetUtil.createComboBox("#subject_gender", {
        width: 155,
        height: comboboxHeight,
        onBeforeLoad: MiscUtil.adjustSelectElemTabIndex
    });

    var options = {
        width: 170,
        height: comboboxHeight,
        prompt: "MRN",
        onBeforeLoad: MiscUtil.adjustSelectElemTabIndex,
        onSelect: function (record) {
            var currentMrnInfo = record;
            if($("#warningMessageRow").is(":visible") &&
                (currentMrnInfo.value != app_selectedSubject.mrnInfo.value ||
                currentMrnInfo.institution != app_selectedSubject.mrnInfo.institution)){
                ExternalSubjectDetails.clearWarnings()
            }
            app_selectedSubject.mrnInfo = record;
        }
    };

    var searchMrn = sessionStorage.getItem("searchMrn");
    if (data.mrnInfo != undefined && data.mrnInfo.value != undefined) {
        searchMrn = data.mrnInfo.value;
    }

    renderSubjectMrnField($("#subject_mrn"), data.mrnInfoList, searchMrn, options);

    ExternalSubjectDetails.populateSubjectEditForm(data);
};

/*
 * this is for integration mode
 */
ExternalSubjectDetails.populateSubjectEditForm = function(data) {
   ExternalSubjectDetails.clearWarnings();

    $("#subject_firstName").replaceWith("<span id='subject_firstName'>" + data.firstName + "</span>");

    var middleName = "";
    if (data.middleName != null) {
        middleName = data.middleName
    }

    $("#subject_middleName").replaceWith("<span id='subject_middleName'>" + middleName + "</span>");

    $("#subject_lastName").replaceWith("<span id='subject_lastName'>" + data.lastName + "</span>");

    if(data.address) {
        $("#subject_streetAddress1").replaceWith("<span id='subject_streetAddress1'>" + data.address.addressLine1 + "</span>");

        $("#subject_streetAddress2").replaceWith("<span id='subject_streetAddress2'>" + data.address.addressLine2 + "</span>");

        $("#subject_city").replaceWith("<span id='subject_city'>" + data.address.city + "</span>");

        $("#subject_state").replaceWith("<span id='subject_state'>" + data.address.stateName + "</span>");

        $("#subject_zipcode").replaceWith("<span id='subject_zipcode'>" + data.address.zipCode + "</span>");

        $("#subject_country").replaceWith("<span id='subject_country'>" + data.address.countryName + "</span>");
    }

    var dob = showDate(data.birthdate);
    $("#subject_dob").replaceWith("<span id='subject_dob'>" + dob + "</span>");

    $("#subject_race").replaceWith("<span id='subject_race'>" + data.raceName + "</span>");

    $("#subject_ethnicity").replaceWith("<span id='subject_ethnicity'>" + data.ethnicityName + "</span>");

    $("#subject_primaryContactNumber").replaceWith("<span id='subject_primaryContactNumber'>" + unifiedPhoneNumbersFormat(data.primaryContactNumber) + "</span>");


    $("#subject_secondaryContactNumber").replaceWith("<span id='subject_secondaryContactNumber'>" + unifiedPhoneNumbersFormat(data.secondaryContactNumber) + "</span>");


    $("#subject_gender").combobox("setValue", app_selectedSubject.genderId);

    var mrnDefaultValue = $("#subject_mrn").data("defaultMrn");
    if (app_selectedSubject.mrnInfoList.length > 0) {
        if (mrnDefaultValue != undefined) {
            $("#subject_mrn").combobox("setValue", mrnDefaultValue);
        }
        else {
            $("#subject_mrn").combobox("setValue", '');
        }
    }

    var subjectStatus = "check";
    if (isEditMode) {
        subjectStatus = getSubjectStatus(data.active);
    }
    $("#subject_status").switchbutton(subjectStatus);

    if (isStudyStaff() && subjectStatus === "uncheck") {
        ///remove the study selection if it is study staff and available to add to study is no
        $("#studyAreaLabel").replaceWith($("#commentAreaLabel"));
        $("#studyArea").replaceWith($("#commentArea"));
    }

    setStudyComboboxValue();

    $("#subject_comment").val(data.comment);
};

ExternalSubjectDetails.retrieveFormJsonString = function(){
    var state = lookUpIdByName(states, $('#subject_state').text());
    var country = lookUpIdByName(countries, $('#subject_country').text());
    var race = lookUpIdByName(races, $('#subject_race').text());
    var ethnicity = lookUpIdByName(ethnicities, $('#subject_ethnicity').text());

    var subjectDOB1 = new Date($("#subject_dob").text());
    subjectDOB1.setHours(10, 0, 0, 0);

    var mrnInfo = lookUpComboboxDataByValue($("#subject_mrn"), $("#subject_mrn").combobox("getValue"));

    var mergeSimilarMrn = $("#overrideConfirmationCheckbox").is(":visible") && $("#overrideConfirmationCheckbox").is(":checked");
    var subjectDataJsonStr = JSON.stringify({
        id: app_selectedSubject != null && app_selectedSubject.id != null ? app_selectedSubject.id : 0,
        partnersUid: app_selectedSubject.partnersUid,
        firstName: $.trim($("#subject_firstName").text()),
        middleName: $.trim($("#subject_middleName").text()),
        lastName: $.trim($("#subject_lastName").text()),
        mrnInfo: mrnInfo,
        gender: $("#subject_gender").combobox("getValue"),
        genderEmpi: app_selectedSubject.genderCode,
        primaryContactNumber: $("#subject_primaryContactNumber").text(),
        secondaryContactNumber: $("#subject_secondaryContactNumber").text(),
        birthday: subjectDOB1.toString(),
        race: race != null ? race : 0,
        ethnicity: ethnicity != null ? ethnicity : 0,
        streetAddress1: $("#subject_streetAddress1").text(),
        streetAddress2: $("#subject_streetAddress2").text(),
        city: $('#subject_city').text(),
        state: state != null ? state : 0,
        country: country != null ? country : 0,
        zip: $("#subject_zipcode").text(),
        comment: $.trim($("#subject_comment").val()),
        studyId: ($("#subject_study").length != 0 && $('#subject_study').combobox("getValue") != '') ? $('#subject_study').combobox("getValue") : 0,
        active: $('#subject_status').val() == "on",
        mergeSimilarMrn: mergeSimilarMrn
    });

    return subjectDataJsonStr;
};

ExternalSubjectDetails.refreshSubject = function(subjectData){

    findInternalSubject(subjectData.mrnInfoList, function (data) {
        if (data && data.id !== undefined) {
            subjectData.id = data.id;
            subjectData.active = data.active;

            //Override the EMPI gender with the internal gender
            subjectData.genderId = data.genderId;
            subjectData.genderName = data.genderName;
        }
        sessionStorage.setItem("selectedSubject", JSON.stringify(subjectData));
    });
};

ExternalSubjectDetails.processSubject = function(subjectData, subjectMrn, subjectInstitution){
    // the subjectData id is the puid when data is retrieved from an external source
    // we need to lookup and set this id to be the id of the subject in the internal db
    subjectData.id = null;

    if (MiscUtil.isNotUndefinedOrNull(subjectMrn)) {
        subjectData.mrnInfo = {
            value: subjectMrn,
            institution: subjectInstitution
        };
    }

    findInternalSubject(subjectData.mrnInfoList, function (internalData) {
        if (internalData && internalData.id !== undefined) {
            subjectData.id = internalData.id;
            subjectData.active = internalData.active;

            //Override the EMPI gender with the internal gender
            subjectData.genderId = internalData.genderId;
            subjectData.genderName = internalData.genderName;
            subjectData.comment = internalData.comment;
        }

        SubjectDetails.renderMain();
    });
};

ExternalSubjectDetails.displaySimilarMrnAlertBox = function(similarMrnInfo){
    $("#subject_submit_btn").prop("disabled", true);

    var warningMessageRow = $("#warningMessageRow").empty().show();
    var overrideConfirmationMainDiv = $("<div></div>",{"id": "overrideConfirmation"}).addClass("overrideConfirmation");

    $("<td></td>", {"colspan": "6"}).append(overrideConfirmationMainDiv).appendTo(warningMessageRow);

    var similarMrnMessage = ExternalSubjectDetails.generateSimilarMrnMessageText(similarMrnInfo);
    if(UserRoleUtil.userIsSuperAdmin()) {
        var confirmationCheckbox = $("<input/>", {"id": "overrideConfirmationCheckbox", "type": "checkbox"});
        confirmationCheckbox.click(function(){
            if($(this).is(":checked")){
                $("#subject_submit_btn").prop("disabled", false);
            }
            else{
                $("#subject_submit_btn").prop("disabled", true);
            }
        });

        $("<div></div>").addClass("centerVertical").append(confirmationCheckbox).appendTo(overrideConfirmationMainDiv);

        var overrideConfirmationMessage = $("<div></div>", {"id": "overrideConfirmationMessage"}).appendTo(overrideConfirmationMainDiv);

        $("<span></span>").append(similarMrnMessage).appendTo(overrideConfirmationMessage);

    }
    else {
        $("<span></span>").append(similarMrnMessage).appendTo(overrideConfirmationMainDiv);
    }
};

ExternalSubjectDetails.generateSimilarMrnMessageText = function(similarMrnInfo){
    var similarMrn = similarMrnInfo.value + " " + similarMrnInfo.institution;
    var selectedMrn = app_selectedSubject.mrnInfo.value + " " + app_selectedSubject.mrnInfo.institution;

    var messageText = "";

    var formattedSimilarMrn = "<span class='mrnFormat'>" + similarMrn + "</span>";
    var formattedSelectedMrn = "<span class='mrnFormat'>" + selectedMrn + "</span>";

    if(UserRoleUtil.userIsSuperAdmin()) {
        messageText = "Please note that the Epic MRN selected " + formattedSelectedMrn + " is a similar but different " +
            "MRN to Scheduler MRN " + formattedSimilarMrn + ". Please work with your CRC and your Epic contact to determine " +
            "if these two MRNs pertain to the exact same patient. Once verified these two MRNs are for the same patient " +
            "and you would like Scheduler to use the Epic MRN for this patient, please check the check box to the left " +
            "and click Save. Once saved, the Scheduler application will overwrite the Scheduler MRN with the Epic MRN. " +
            "<b>Please note that once overwritten, this change cannot be reverted.</b>";
    }
    else{
        messageText = "The Epic MRN selected " + formattedSelectedMrn + " is a similar but different MRN to Scheduler MRN "
        + formattedSimilarMrn + ". Please contact your hospital administrator.";
    }
    return messageText;
};

ExternalSubjectDetails.isValidForm = function(){
    var isValid = true;
    if ($("#subject_mrn").combobox("getValue").length == 0) {
        showError('#subject_mrnValidation', "Please select an MRN");
        isValid = false;
    }
    else if (!validComboboxValue($("#subject_mrn"))) {
        showError('#subject_mrnValidation', "MRN not found");
        isValid = false;
    }

    if ($("#subject_gender").combobox("getValue").length == 0) {
        showError('#subject_genderValidation', "Please select a gender");
        isValid = false;
    } else if (!validComboboxValue($("#subject_gender"))) {
        showError('#subject_genderValidation', "Gender not found");
        isValid = false;
    }

    return isValid;
};

ExternalSubjectDetails.clearWarnings = function clearWarning(){
    $("#warningMessageRow").empty().hide();
    $("#subject_submit_btn").prop("disabled", false);
};

var StandaloneSubjectDetails = {};
StandaloneSubjectDetails.populateSubjectEditForm = function (data) {
    $("#subject_firstName").val(data.firstName);

    $("#subject_middleName").val(data.middleName);

    $("#subject_lastName").val(data.lastName);

    $("#subject_streetAddress1").val(data.address.addressLine1);

    $("#subject_streetAddress2").val(data.address.addressLine2);

    $("#subject_city").val(data.address.city);

    //check if state value was set
    if (lookUpComboboxDataByValue($("#subject_state"), data.address.stateId) !== null) {
        $("#subject_state").combobox("setValue", data.address.stateId);
    }

    $("#subject_zipcode").val(data.address.zipCode);

    $("#subject_country").combobox("setValue", data.address.countryId);

    var dob = new Date(data.birthdate);
    $("#subject_dob").datepicker("setDate", dob);

    $("#subject_gender").combobox("setValue", data.genderId);

    $("#subject_race").combobox("setValue", data.raceId);

    $("#subject_ethnicity").combobox("setValue", data.ethnicityId);

    var phone1 = data.primaryContactNumber ? data.primaryContactNumber : "";
    $("#subject_primaryContactNumber").val(phone1);

    var phone2 = data.secondaryContactNumber ? data.secondaryContactNumber : "";
    $("#subject_secondaryContactNumber").val(phone2);

    if (data.mrnInfoList.length > 0) {
        $("#subject_mrn").val(data.mrnInfoList[0].value);
    }

    var subjectStatus = getSubjectStatus(data.active);
    $("#subject_status").switchbutton(subjectStatus);

    $("#subject_comment").val(data.comment);


    setStudyComboboxValue();
};

StandaloneSubjectDetails.retrieveFormJsonString = function(){
    //set hours to 10 to handle daylight savings time
    var subjectDOB = $("#subject_dob").datepicker('getDate');
    subjectDOB.setHours(10, 0, 0, 0);
    var subjectDataJsonStr = JSON.stringify({
        id: (app_selectedSubject != null && app_selectedSubject.id != null ? app_selectedSubject.id : 0),
        firstName: $.trim($("#subject_firstName").val()),
        middleName: $.trim($("#subject_middleName").val()),
        lastName: $.trim($("#subject_lastName").val()),
        mrnInfo: {
            value: $.trim($("#subject_mrn").val())
        },
        gender: $("#subject_gender").combobox("getValue"),
        primaryContactNumber: $.trim($("#subject_primaryContactNumber").val()),
        secondaryContactNumber: $.trim($("#subject_secondaryContactNumber").val()),
        birthday: subjectDOB.toString(),
        race: $("#subject_race").combobox("getValue"),
        ethnicity: $("#subject_ethnicity").combobox("getValue"),
        streetAddress1: $.trim($("#subject_streetAddress1").val()),
        streetAddress2: $.trim($("#subject_streetAddress2").val()),
        city: $('#subject_city').val(),
        state: $('#subject_state').combobox("getValue") != '' ? $('#subject_state').combobox("getValue") : 0,
        country: $('#subject_country').combobox("getValue"),
        zip: $.trim($("#subject_zipcode").val()),
        comment: $.trim($("#subject_comment").val()),
        studyId: ($('#subject_study').combobox("getValue") != '' ? $('#subject_study').combobox("getValue") : 0),
        active: isEditMode ? $('#subject_status').val() == "on" : true
    });

    return subjectDataJsonStr;
};

StandaloneSubjectDetails.isValidForm = function(){

    var isValid = true;

    if ($.trim($("#subject_firstName").val()).length < 1) {
        showError('#subject_firstNameValidation');
        isValid = false;
    }
    if ($.trim($("#subject_lastName").val()).length < 1) {
        showError('#subject_lastNameValidation');
        isValid = false;
    }

    if ($.trim($("#subject_gender").combobox("getValue")).length < 1) {
        showError('#subject_genderValidation');
        isValid = false;
    }
    else if (!validComboboxValue($("#subject_gender"))) {
        showError('#subject_genderValidation', "Gender not found");
        isValid = false;
    }

    if ($.trim($("#subject_race").combobox("getValue")).length < 1) {
        showError('#subject_raceValidation');
        isValid = false;
    }
    else if (!validComboboxValue($("#subject_race"))) {
        showError('#subject_raceValidation', "Race not found");
        isValid = false;
    }

    if ($.trim($("#subject_primaryContactNumber").val()).length < 1) {
        showError('#subject_primaryPhoneValidation');
        isValid = false;
    }

    if ($("#subject_ethnicity").combobox("getValue").length < 1) {
        showError('#subject_ethnicityValidation');
        isValid = false;
    }
    else if (!validComboboxValue($("#subject_ethnicity"))) {
        showError('#subject_ethnicityValidation', "Ethnicity not found");
        isValid = false;
    }

    if ($.trim($("#subject_streetAddress1").val()).length < 1) {
        showError('#subject_streetAddress1Validation');
        isValid = false;
    }

    if ($.trim($("#subject_city").val()).length < 1) {
        showError('#subject_cityValidation');
        isValid = false;
    }

    if ($("#subject_state").combobox("getValue").length > 0 && !validComboboxValue($("#subject_state"))) {
        showError('#subject_stateValidation', "State not found");
        isValid = false;
    }

    if ($("#subject_country").combobox("getValue").length < 1) {
        showError('#subject_countryValidation');
        isValid = false;
    }
    else if (!validComboboxValue($("#subject_country"))) {
        showError('#subject_countryValidation', "Country not found");
        isValid = false;
    }

    var zipcode = $.trim($("#subject_zipcode").val());
    if (zipcode.length < 1) {
        showError('#subject_zipcodeValidation');
        isValid = false;
    }

    //if state is selected, make sure zip code only contains digits and the "dash" character
    if (zipcode.length > 0 && $("#subject_state").combobox("getValue").length > 0 && validComboboxValue($("#subject_state"))) {
        var zipCodeRegex = /(^\d{5}$)/;
        var isValidZip = zipCodeRegex.test(zipcode);

        if (!isValidZip) {
            showError('#subject_zipcodeValidation', "Please enter a valid zipcode");
            isValid = false;
        }
    }

    var candidateMrn = $.trim($("#subject_mrn").val());
    var mrnRegexObj = /^[0-9]+$/;
    if (candidateMrn.length < 1) {
        showError('#subject_mrnValidation');
        isValid = false;
    }
    else if (!mrnRegexObj.test(candidateMrn)) {
        showError('#subject_mrnValidation', "MRN can only contain digits");
        isValid = false;
    }

    var dobString = $("#subject_dob").val();

    if (dobString == null || dobString == "") {
        showError('#subject_dobValidation');
        isValid = false;
    }
    else {
        if (!isValidDOB(dobString)) {
            showError('#subject_dobValidation', 'Please enter valid birth date');
            isValid = false;
        }
    }

    return isValid;
};

//This specifies a name for this piece of code which will appear when debugging
//This is needed because this file is loaded programatically
//# sourceURL=add_edit_subject.js
