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

var subjectPage = (function () {
    var initFn = function () {
        eraseLicense();
        app_runIdleTimer();
        commonData();

        $.get("meta_headers.html", function (content) {
            $("head").prepend(content);
            $('#subject_search').css("visibility", "visible");
            $.unblockUI();
            eraseLicense();
        });

        renderBreadcrumbs("subject_screen");
        initFooter();

        isEpicMode = sessionStorage.getItem("subjectMode").toLowerCase() === "epic";

        //workaround for browser caching previous form input values when 
        //navigating to page via browser back button
        $(".subject_search_field").val("");

        //cleanup data related to last search
        if (sessionStorage.getItem("reloadPreviousSearch") !== "true") {
            sessionStorage.removeItem("searchLastName");
            sessionStorage.removeItem("searchFirstName");
            sessionStorage.removeItem("searchDOB");
            sessionStorage.removeItem("searchMrn");
            sessionStorage.removeItem("searchGenders");
        }

        sessionStorage.removeItem("emptySearch");

        getSubjectStaticLists(function () {
            $("#subject_sex").html(genderSelectOptions);
            $("#subject_race").html(raceSelectOptions);
            $("#subject_ethnicity").html(ethnicitySelectOptions);
            $("#subject_state").html(stateSelectOptions);
            $("#subject_country").html(countrySelectOptions);

            if (!isEpicMode) {
                populateGenderFilter($("#subject_search_gender"), genders, "gender", true);
            } else {
                populateGenderFilter($("#subject_search_gender"), epicGenders, "gender", false);

                //only allow one checkbox to be selected
                $("input[name='gender[]']").on("change", function () {
                    if ($(this).is(":checked")) {
                        $("input[name='gender[]']:not(:checked)").prop("disabled", true);
                    } else {
                        $("input[name='gender[]']:not(:checked)").prop("disabled", false);
                    }
                });
            }

            handleReloadPreviousSearch();
        });

        getNonClosedStudiesList();
        enableEPICFeatures();

        $(".subject_search_field").on('input keyup blur change', function (event) {
            enableSearchAndValidate(event);
        });

        WidgetUtil.createDatepicker("#subject_search_birthdate", {
            yearRange: "-120:+0",
            maxDate: "0D",
            constrainInput: true,
            buttonImage:"css/images/calendar_icn_white_bg.png"
        });
    };
    return {
        init: initFn
    };
}());

function createSubject() {
    sessionStorage.removeItem("selectedSubjectId");
    sessionStorage.removeItem("selectedSubjectByPuid");
    sessionStorage.removeItem("selectedSubjectMrn");
    sessionStorage.removeItem("selectedSubjectInstitution");
    goToSubjectEntryForm();
}

function goToSubjectEntryForm(subjectId) {

    if (subjectId !== undefined) {
        sessionStorage.setItem("selectedSubjectId", subjectId);
        sessionStorage.removeItem("selectedSubjectMrn");
        sessionStorage.removeItem("selectedSubjectInstitution");
    }

    //origin request param must be set in order to open the subject details in edit mode
    window.location.href = "subject_details.html?origin=subjects";
}

function populateGenderFilter(container, genderList, name, checkAll) {
    var genderFilter = "";
    var checked = checkAll ? "checked" : "";
    for (var i = 0; i < genderList.length; i++) {
        genderFilter += "<label>" + "<input id='" + name + "_" + genderList[i].id + "' name='" + name + "[]' type='checkbox' class='subject_search_gender' value='"
            + genderList[i].id + "' " + checked + " /><span>" + genderList[i].name + "</span></label>";
    }

    container.html(genderFilter);
}

function enableEPICFeatures() {
    if (!isEpicMode) {
        //add the create new subject button if in standalone mode only

        $('#create_new_subject').html('<span>' +
            '<input type="submit" id="create_new_subject_btn" onclick="createSubject();"' +
            ' value="Create New Subject" class="saveButton2"/></span>');

        $('#create_new_subject').css({display: "table-cell"});
    } else {
        $("#subject_search_title").text("Search EPIC Subject List");
    }
}

function parseFullName() {
    var nameObj = {
        firstName: '',
        lastName: ''
    };

    var fullName = $("#subject_search_name").val();

    var result = fullName.split(",");

    var lastName = result[0];
    nameObj.lastName = $.trim(lastName);

    var firstName = '';

    if (result.length > 1) {
        firstName = result[1];
        nameObj.firstName = $.trim(firstName);
    }

    return nameObj;
}

function searchingOnMRNEnabled(mrn) {

    if (mrn.length >= 1) {
        //either an MRN or name and birthdate should be specified but not both
        $("#subject_search_name").addClass("disabledInput");
        $("#subject_search_name").prop("disabled", true);
        $("#subject_search_birthdate").addClass("disabledInput");

        if (!$("#subject_search_birthdate").datepicker("option", "disabled")) {
            $("#subject_search_birthdate").datepicker("option", "disabled", true);
        }
        return true;
    } else {
        $("#subject_search_name").removeClass("disabledInput");
        $("#subject_search_name").prop("disabled", false);
        $("#subject_search_birthdate").prop("disabled", false);

        if ($("#subject_search_birthdate").datepicker("option", "disabled")) {
            $("#subject_search_birthdate").datepicker("option", "disabled", false);
        }

        $("#subject_search_birthdate").removeClass("disabledInput");
        return false;
    }
}

function dateOfBirthSearchValidator(dateString, event, validDate, birthDate, todayDate) {

    var birthDateEntered = false;
    if (dateString !== "__/__/____" && dateString !== "") {
        if (isDOBInvalid(event, dateString, validDate, birthDate, todayDate)) {
            $('#subject_search_birthdate').blur();
            $('#subject_search_birthdate').val('').datepicker("refresh");
            $('#subject_search_birthdate').focus();
            $('#subject_birthdate_error').show();
        } else {
            $('#subject_birthdate_error').hide();
            birthDateEntered = true;
        }
    }
    else if ($('#subject_search_birthdate').is(':focus') != true
        && (dateString === "__/__/____" || dateString === "")) {
        $('#subject_birthdate_error').hide();
    }
    return birthDateEntered;
}


function isDOBInvalid(event, dateString, validDate, birthDate, todayDate) {
    var dateRegex = /\d{2}\/\d{2}\/\d{4}/;
    return ((event.type === 'blur' && (!validDate || birthDate > todayDate))
    || (event.type === 'keyup' && ((dateRegex.test(dateString) && !validDate) || (birthDate > todayDate))));
}

function searchingOnFullNameAndDOBEnabled(fullName, birthDateEntered) {

    if (fullName.length > 0 || birthDateEntered) {
        //either an MRN or Name with min 2 letters for first and last and birthdate should be specified but not both
        $("#subject_search_mrn").addClass("disabledInput");
        $("#subject_search_mrn").prop("disabled", true);
    }
    else {
        $("#subject_search_mrn").removeClass("disabledInput");
        $("#subject_search_mrn").prop("disabled", false);
    }
}

function searchButtonEnable(mrn, mrnSearch, lastName, firstName, birthDateEntered, invalidBirthdate) {

    if (!$('#subject_search_error').is(":visible") && (mrn.length >= 1 && mrnSearch)
        || (lastName.length > 1 && firstName.length > 1 && birthDateEntered && !invalidBirthdate && !mrnSearch)) {
        $("#search_subject_btn").addClass("activeButton").prop("disabled", false);
    } else {
        $("#search_subject_btn").removeClass("activeButton").prop("disabled", true);
    }
}

function mrnSearchValidator(mrn) {

    if (mrn.length > 0 && !containsDigitsOnly(mrn)) {
        $("#subject_mrn_error").show();
        return false;
    } else {
        $("#subject_mrn_error").hide();
        return true;
    }
}

function fullNameSearchValidator(fullName, firstName, lastName) {

    if ((fullName.length > 0 && (firstName.length < 2 || lastName.length < 2)) && ($("#subject_search_name").is(":focus")) === false) {
        $("#subject_name_error").show();
        return false;
    } else {
        $("#subject_name_error").hide();
        return true;
    }
}

function enableSearchAndValidate(event) {
    var mrn = $("#subject_search_mrn").val();

    var fullName = $("#subject_search_name").val();
    var nameObj = parseFullName(fullName);
    var firstName = nameObj.firstName;
    var lastName = nameObj.lastName;

    var dateString = $("#subject_search_birthdate").val();
    var validDate = isValidDate(dateString);
    var birthDate = new Date(dateString);
    var todayDate = new Date();
    var invalidBirthdate = $("#subject_birthdate_error").is(":visible") || birthDate === 'Invalid Date' || !validDate;

    if (event.type === 'keyup' && event.which === keyCodes.ENTER) {
        var subjectSearchDisabled = $("#search_subject_btn").prop("disabled");
        if (!subjectSearchDisabled) {
            $("#search_subject_btn").click();
        }
    }

    var birthDateEntered = dateOfBirthSearchValidator(dateString, event, validDate, birthDate, todayDate);
    fullNameSearchValidator(fullName, firstName, lastName);
    mrnSearchValidator(mrn);

    if ($("#subject_name_error").css("display") != "none"
        || $("#subject_birthdate_error").css("display") != "none"
        || $("#subject_mrn_error").css("display") != "none") {
        $('#subject_search_error').show();
    } else {
        $('#subject_search_error').hide();
    }

    var mrnSearch = searchingOnMRNEnabled(mrn);
    searchingOnFullNameAndDOBEnabled(fullName, birthDateEntered);
    searchButtonEnable(mrn, mrnSearch, lastName, firstName, birthDateEntered, invalidBirthdate);
}

function handleReloadPreviousSearch() {
    //perform previous search if flag is set
    if (sessionStorage.getItem("reloadPreviousSearch") === "true") {
        var searchMrn = sessionStorage.getItem("searchMrn");
        var searchLastName = sessionStorage.getItem("searchLastName");
        var searchFirstName = sessionStorage.getItem("searchFirstName");
        var searchDOB = sessionStorage.getItem("searchDOB");
        var searchGenders = sessionStorage.getItem("searchGenders");

        var doSearch = false;

        if (!isEmpty(searchGenders)) {

            $(".subject_search_gender").prop("checked", false);

            var genderList = $.parseJSON("[" + sessionStorage.getItem("searchGenders") + "]");
            $.each(genderList, function (index, value) {
                //check the appropriate checkbox
                $("#gender_" + value).prop("checked", true);
            });
        }

        if (searchMrn) {
            $("#subject_search_mrn").val(searchMrn);
            doSearch = true;
        } else if (!isEmpty(searchLastName) && !isEmpty(searchFirstName)) {
            $("#subject_search_name").val(searchLastName + ", " + searchFirstName);

            if (!isEmpty(searchDOB)) {
                $("#subject_search_birthdate").val(searchDOB);
                doSearch = true;
            }
        }

        if (doSearch) {
            $("#search_subject_btn").addClass("activeButton").prop("disabled", false);
            $("#search_subject_btn").click();
        }

        sessionStorage.removeItem("reloadPreviousSearch");
    }
}

function createBadNewsDiv(message, id) {
    return '<div id="badNews_' + id +
        '" class="badNewsMessage"' +
        '">' + message + '</div><div style="min-height: 10px;"></div>';
}

function generateSubjectDetailHtml(val) {
    var html = "";

    var middleName = !isEmpty(val.middleName) ? val.middleName : "";
    var nameSuffix = !isEmpty(val.nameSuffix) ? "&nbsp;" + val.nameSuffix : "";
    var overviewInfoText = "<input id='subject_partnersUid_" + val.id + "' type='hidden' name='puid' value='" + val.partnersUid
        + "'> <input id='subject_active_" + val.id + "' type='hidden' name='active' value='" + val.active + "'><span style='margin-left: 20px;' class='bold' id='subject_lastName_" + val.id + "'>" + val.lastName
        + "</span><span class='bold' id='subject_nameSuffix_" + val.id + "'>" + nameSuffix + "</span>, <span> " + "<span class='bold' id='subject_firstName_" + val.id + "'> " + val.firstName + "</span>" + " <span class='bold' id='subject_middleName_" + val.id + "'>" + middleName + "</span>"
        + "<span style='margin-left: 40px;'>DOB: " + "<span class='bold' id='subject_dob_" + val.id + "'>" + showDate(val.dob) + "</span></span>"
        + "<span style='margin-left: 40px;'> Gender: <span class='bold' id='subject_gender_" + val.id + "'>" + val.gender + "</span><span>"
        + "<span style='margin-left: 40px;' class='bold'>";

    if (!isEmpty(val.address.stateName)) {
        if (!isEmpty(val.address.city)) {
            overviewInfoText += "<span id='header_subject_city_" + val.id + "'>" + val.address.city + "</span>, ";
        }

        overviewInfoText += "<span id='header_subject_state_" + val.id + "'>" + val.address.stateName;
    }

    overviewInfoText += "</span>";

    html += "<div id ='subject_listing_" + val.id + "' class='subjectListing subjectCollapse' data-subjectid='" + val.id + "'> "
        + "<div id='subject_header_" + val.id + "' class='subjectHeader'>"
        + "<div style='float: left'><img src='css/images/sm_circle_plus.png' title='Collapse' " +
        "class='parentHeaderImages subjectToggle' onclick='toggleSubjects(event, this);'/>" + overviewInfoText + "</div></div>"
        + "<div style='clear: both;'/>"
        + "<div id ='subject_details_" + val.id + "'class='subjectDetails' style='display: none;'></div> </div>";

    html += "<div style='clear: both;'/>";

    return html;
}

function displaySubjectOverview(parsedData) {
    var out = "<div style='width: 100%;'>";

    var totalLength = parsedData.subjectList.length;

    var failoverMessage = parsedData.empiFailoverMessage;
    var failedOver = (failoverMessage ? true : false);
    sessionStorage.setItem("alreadyDisplayingFailoverMessage", failedOver);

    $('#searchOutput').css({display: 'block'});

    //Check if no subjects were found for the search
    if (totalLength === 0) {
        var message = "";
        if (failoverMessage) {
            message = failoverMessage;
        } else {
            message = "No results found.";
        }

        $('#searchOutput').html(createBadNewsDiv(message, "top"));

        sessionStorage.setItem("emptySearch", true);

        return;
    }

    //search returned results
    sessionStorage.setItem("emptySearch", false);
    $('#search_place_holder').remove();

    var objList = parsedData.subjectList;

    if (failoverMessage) {
        out += createBadNewsDiv(failoverMessage, "top");
    }

    out += '<div onclick="toggleAllSubjects(event, this);" title="Expand" id="toggleAllSubjects">'
        + '<img src="css/images/arrows_expand.png" hspace="5"'
        + 'class="toggleSubjects" id="toggleSubjectsImg">'
        + '<p id="toggleText" style="margin: 0; width: 135px">Expand All</p></img></div>';

    out += '<div id="subjectListingWrapper">';
    $.each(objList, function (key, val) {

        out += generateSubjectDetailHtml(val);

        $('#searchOutput').append(out);
    });

    out += '</div>'; //close subjectListingWrapper div
    out += "</div>";
    $('#searchOutput').html(out);

    //when resizing the window
    $(window).resize(function () {
        hideOrShowScrollbars($("#subjectListingWrapper"), $(window).height() - $("#subject_search").height());
    });
}

function processSearchSubject() {
    $('#searchOutput').css({display: 'none'});

    var mrn = $.trim($("#subject_search_mrn").val());

    var fullName = $.trim($("#subject_search_name").val());
    var firstName = '';
    var lastName = '';
    var firstNameStartIndex = fullName.indexOf(",");
    if (firstNameStartIndex > -1) {
        lastName = fullName.substring(0, firstNameStartIndex);
        lastName = $.trim(lastName);

        if (fullName.length > firstNameStartIndex) {
            firstName = fullName.substring(firstNameStartIndex + 1);
            firstName = $.trim(firstName);
        }
    }
    var dob = $.trim($("#subject_search_birthdate").val());
    var genderList = [];

    $("input[name='gender[]']:checked").each(function () {
        genderList.push($(this).val());
    });

    sessionStorage.setItem("searchLastName", lastName);
    sessionStorage.setItem("searchFirstName", firstName);
    sessionStorage.setItem("searchDOB", dob);
    sessionStorage.setItem("searchMrn", mrn);
    sessionStorage.setItem("searchGenders", genderList);

    var url = "rest/subject/ssot/getSearchSubjects";

    var jsonData = JSON.stringify({
        lastName: lastName,
        firstName: firstName,
        mrn: mrn,
        birthday: dob,
        genderList: genderList
    });

    $.post(url, {data: jsonData}, function (data) {
        var parsedData = JSON.parse(data);

        displaySubjectOverview(parsedData);

    });

    return;
}

function loadMRNData(data, subjectId) {
    var mrnInfoList = data.mrnInfoList;
    var searchMrn = sessionStorage.getItem("searchMrn");

    var mrnField = $("#subject_mrn_" + subjectId);
    if (mrnInfoList != null && mrnInfoList.length > 0) {
        var options = {
            prompt: "MRN"
        };

        renderSubjectMrnField(mrnField, mrnInfoList, searchMrn, options);
    } else {
        mrnField.replaceWith("<span> No MRN found </span>");
        mrnField.css({display: 'inline'});
    }
}

function toggleSubjects(event, obj) {
    preventDefaultAction(event);

    var subjectListing = $(obj).parents(".subjectListing").first();
    var subjectDetail = subjectListing.find(".subjectDetails");

    if (subjectDetail.is(":visible")) {
        expandOrCollapseSubject(subjectListing, false);
    }
    else {
        expandOrCollapseSubject(subjectListing, true);
    }

    checkIfAnySubjectListOpen();
}

function checkIfAnySubjectListOpen() {
    var className = 'subjectListing';
    var subjectListings = $("." + className.replace(/\s/g, "."));
    for (var j = 0; j < subjectListings.length; j++) {
        var object = $(subjectListings[j]).find('img').first();
        if ($(object).attr("title") === "Collapse") {
            document.getElementById("toggleText").innerHTML = "Collapse All";
            document.getElementById("toggleSubjectsImg").src = "css/images/arrows_collapse.png";
            document.getElementById("toggleAllSubjects").title = "Collapse";
            return;
        }
    }

    document.getElementById("toggleText").innerHTML = "Expand All";
    document.getElementById("toggleAllSubjects").title = "Expand";
    document.getElementById("toggleSubjectsImg").src = "css/images/arrows_expand.png";
}

function toggleAllSubjects(event, obj) {
    preventDefaultAction(event);

    var expand = false;
    if (obj.title === "Collapse") {

        obj.title = "Expand";
        document.getElementById("toggleText").innerHTML = "Expand All";
        document.getElementById("toggleSubjectsImg").src = "css/images/arrows_expand.png";
    } else {

        obj.title = "Collapse";
        document.getElementById("toggleText").innerHTML = "Collapse All";
        document.getElementById("toggleSubjectsImg").src = "css/images/arrows_collapse.png";

        expand = true;
    }

    //expand or collapse each of the subject listings
    var className = 'subjectListing';
    var subjectListing = $("." + className.replace(/\s/g, "."));
    for (var j = 0; j < subjectListing.length; j++) {
        var subjectElement = subjectListing[j];
        expandOrCollapseSubject(subjectElement, expand);
    }
}

function displayBadNewsDiv(parsedData, subjectDetailsText) {
    var message = parsedData.empiFailoverMessage;
    var badSchedulerCornerCaseDisplayOnTop = (message.indexOf(ERROR_XX) !== -1);

    console.log("Bad Scheduler Corner Case? " + badSchedulerCornerCaseDisplayOnTop);
    console.log(message);

    var alreadyDisplayingFailoverMessage = sessionStorage.getItem("alreadyDisplayingFailoverMessage");
    if (alreadyDisplayingFailoverMessage === "false") {
        subjectDetailsText += createBadNewsDiv(message, "plus") + "</br>";
    }
    else {
        var badNewsDivTop = $("#badNews_top");
        var badNewsText = badNewsDivTop.text();
        var wasBadEmpiGoodSched = badNewsText.indexOf(ERROR_EMPI_FAILS_BUT_SCHED_WORKS) !== -1;
        var wasBadSslGoodSched = badNewsText.indexOf(ERROR_SSL_FAILS_BUT_SCHED_WORKS) !== -1;
        console.log("Bad news text is: " + badNewsText);
        if (badSchedulerCornerCaseDisplayOnTop) {
            var message2 = badNewsText.replace(/\..*\./, ". " + message);
            if (wasBadSslGoodSched) {
                message2 = message2.replace(ERROR_XX, ERROR_SSL_FAILS_AND_SCHED_FAILS);
            }
            else if (wasBadEmpiGoodSched) {
                message2 = message2.replace(ERROR_XX, ERROR_EMPI_FAILS_AND_SCHED_FAILS);
            }
        }
        badNewsDivTop.text(message2);
    }
    return subjectDetailsText;
}

function displaySubjectDetails(subjectId) {
    var failedOver = sessionStorage.getItem("alreadyDisplayingFailoverMessage");
    var isPuid = failedOver !== "true" && isEpicMode;
    sessionStorage.setItem("selectedSubjectByPuid", isPuid);

    var jsonData = JSON.stringify({id: subjectId});
    $.get("rest/subject/getSubjectData?isPuid=" + isPuid + "&data=" + encodeURIComponent(jsonData), function (data) {
        var parsedData = JSON.parse(data);

        var subjectDetailsText = "";

        if (parsedData.empiFailoverMessage) {
            subjectDetailsText = displayBadNewsDiv(parsedData, subjectDetailsText);
        }
        if (parsedData.id) {
            subjectDetailsText += generateSubjectDetailsHtmlForSubjectSearch(parsedData, subjectId);
        }

        var subjectDetail = $("#subject_details_" + subjectId);
        subjectDetail.html(subjectDetailsText);

        subjectDetail.css({display: 'block'});

        loadMRNData(parsedData, subjectId);

        //the subject details should be expanded by default
        $("#subject_listing_" + subjectId).removeClass("subjectCollapse").addClass("subjectExpand");

        //adjust the appearance of scrollbars on the subject listing
        hideOrShowScrollbars($("#subjectListingWrapper"), $(window).height() - $("#subject_search").height());
    });
}

function generateSubjectDetailsHtmlForSubjectSearch(subjectDetails, subjectId) {
    var subjectDetailsTextHtml = "";
    var addressLine1 = subjectDetails.address.addressLine1 ? subjectDetails.address.addressLine1 : "";
    var addressLine2 = subjectDetails.address.addressLine2 ? subjectDetails.address.addressLine2 : "";
    var cityName = subjectDetails.address.city             ? subjectDetails.address.city : "";
    var stateName = subjectDetails.address.stateName       ? subjectDetails.address.stateName : "";
    var zipCode = subjectDetails.address.zipCode           ? subjectDetails.address.zipCode : "";
    var countryName = subjectDetails.address.countryName   ? subjectDetails.address.countryName : "";
    var subjectPuidParam = subjectDetails.partnersUid      ? "," + subjectDetails.partnersUid : "";
    var phone1 = subjectDetails.primaryContactNumber       ? subjectDetails.primaryContactNumber : "";
    var phone2 = subjectDetails.secondaryContactNumber     ? subjectDetails.secondaryContactNumber : "";
    var raceName = subjectDetails.raceName                 ? subjectDetails.raceName : "";

    subjectDetailsTextHtml += "<div style='margin-left: 10px;' class='roundBorder6'>";

    subjectDetailsTextHtml += " <div style='float: left;'>"
        + "<div style='float: left;'><table class='subjectDetailsTable'>"
        + "<tr><td> Street Address 1 </td><td id='subject_address1_" + subjectId + "'>" + addressLine1 + "</td></tr>"
        + "<tr><td> Street Address 2 </td><td id='subject_address2_" + subjectId + "'>" + addressLine2 + "</td></tr>"
        + "<tr><td> City  </td><td id='subject_city_" + subjectId + "'>" + cityName + "</td></tr>"
        + "<tr><td> State </td><td id='subject_stateName_" + subjectId + "'>" + stateName + "</td></tr>"
        + "<tr><td> Zipcode </td><td id='subject_zipcode_" + subjectId + "'>" + zipCode + "</td></tr>"
        + "<tr><td> Country </td><td id='subject_countryName_" + subjectId + "'>" + countryName + "</td></tr>"
        + "</table></div>";

    subjectDetailsTextHtml += "<div style='float: left;margin-left: 50px'>"
        + "<table class='subjectDetailsTable'>"
        + "<tr><td> Primary Phone </td><td id='subject_primaryContactNumber_" + subjectId + "'>" + unifiedPhoneNumbersFormat(phone1) + "</td></tr>"
        + "<tr><td> Secondary Phone </td><td id='subject_secondaryContactNumber_" + subjectId + "'>" + unifiedPhoneNumbersFormat(phone2) + "</td></tr>"
        + "<tr><td> Race </td><td id='subject_raceName_" + subjectId + "'>" + raceName + "</td></tr>"
        + "<tr><td> Ethnicity </td><td id='subject_ethnicityName_" + subjectId + "'>" + subjectDetails.ethnicityName + "</td></tr>"
        + "</table></div>"
        + "</div>";

    subjectDetailsTextHtml += "<div style='visibility:hidden;' id='subject_comment_" + subjectId + "'>"
        + subjectDetails.comment + "</div>";

    subjectDetailsTextHtml += "<div id='mrnAndMoreActions'><span style='height: 25px;'><span>"
        + "<span id='mrnLabel'>MRN: </span> <span style='vertical-align: text-bottom' ><input style='vertical-align: bottom;display: none;' "
        + "id='subject_mrn_" + subjectId + "' data-subjectid='" + subjectId + "' class='mrnListing easyui-combobox'/> </span> </span>"
        + "<span class='moreActions'><a href='javascript:goToSubjectEntryForm(" + subjectId + subjectPuidParam + ")'>More Actions</a></span>"
        + "</div></div>";

    subjectDetailsTextHtml += "<div style='clear:both;'/></div>";
    return subjectDetailsTextHtml;
}

function expandOrCollapseSubject(subjectElement, expand) {
    var subjectId = $(subjectElement).attr("data-subjectid");
    var subjectDetail = $("#subject_details_" + subjectId);

    if (expand) {
        //populate the subjectDetails
        displaySubjectDetails(subjectId);
        $(subjectElement).find('img').first().attr('src', 'css/images/sm_circle_minus.png').attr('title', 'Collapse');
    } else {
        subjectDetail.css({display: 'none', cursor: 'default'});
        $(subjectElement).find('img').first().attr('title', 'Expand').attr('src', 'css/images/sm_circle_plus.png');
        $(subjectElement).removeClass("subjectExpand").addClass("subjectCollapse");

        //empty the contents of the subject details
        subjectDetail.html("");
    }
}

//This specifies a name for this piece of code which will appear when debugging
//This is needed because this file is loaded programatically
//# sourceURL=subjects.js
