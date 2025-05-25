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
var homeCalendar;
var home_rendered = false;
var visitId;
var studyId;
var bvId;

var sortByStudy = "study.localId";
var sortByPI = "user.lastName";
var sortByVisit = "visitTemplate.name";
var sortByTemplateVisitType = "visitTemplate.visitType";
var sortByTemplateSublocation = "sublocation.name";

var sortByStart = "bv.scheduledStartTime";
var sortByEnd = "bv.scheduledEndTime";
var sortByIrb = "bv.study.irb";
var sortByBookedVisitName = "bv.visitTemplate.name";
var sortByAppointment = "bv.appointmentStatus.name";

function authDataCallback(data) {
    $('#index_loginError').css({display: 'none'});
    var parsedData = JSON.parse(data);
    user = new User();
    user.id = parsedData.id;
    user.ecommonsId = parsedData.ecommonsId;
    user.firstName = parsedData.firstName;
    user.middleName = parsedData.middleName;
    user.lastName = parsedData.lastName;
    user.streetAddress1 = parsedData.streetAddress1;
    user.streetAddress2 = parsedData.streetAddress2;
    user.city = parsedData.city;
    user.state = parsedData.state;
    user.zip = parsedData.zip;
    user.primaryPhone = parsedData.primaryPhone;
    user.secondaryPhone = parsedData.secondaryPhone;
    user.email = parsedData.email;
    user.fax = parsedData.fax;
    user.pager = parsedData.pager;
    user.division = parsedData.division;
    user.department = parsedData.department;
    user.userGroup = parsedData.userGroup;
    user.institution = parsedData.institution;
    user.institutionRole = parsedData.institutionRole;
    user.credential = parsedData.credential;
    user.facultyRank = parsedData.facultyRank;
    user.irbInstitution = parsedData.irbInstitution;
    user.active = parsedData.active;
    user.authStatus = parsedData.authStatus;
    user.previousLoginTime = parsedData.previousLoginTime;
    sessionStorage.setItem("userData", JSON.stringify(user));
    $('#index_loginLoading').css({visibility: "hidden"});

    if (user.authStatus == USER_STATUS_AUTHORIZED) {
        commonData();
        $('#loginForm').css({display: "none"});
        $('#home_previousLoginTime').css({visibility: "visible"});

        if (PREVIOUS_ACCESS_SHOWN == false && user.previousLoginTime != "") {
            $('#home_previousLoginTime').text("Last Login: " + user.previousLoginTime);
            PREVIOUS_ACCESS_SHOWN = true;
        }
        else {
            window.location.href = "password_reset.html";
        }
        $("#home_toggleCalendar").attr("checked", "checked");
        calendar_rendered = false;
        homeModuleRoles();
    }
    else {
        if (user.authStatus == USER_STATUS_NOT_FOUND) {
            $('#index_loginError').text('User not found in system');
        }
        else if (user.authStatus == USER_STATUS_INVALID_PASSWORD) {
            $('#index_loginError').text('Invalid password');
        }
        else if (user.authStatus == USER_STATUS_INACTIVE) {
            $('#index_loginError').text('User is inactive');
        }
        $('#index_loginError').css({display: 'block'});
        $('#index_loginError').css({opacity: 0.0, visibility: "visible"}).animate({opacity: 1.0});
    }
}

function login(successCallback) {
    $('#index_loginLoading').css({visibility: "visible"});
    $.get("rest/auth/getUserData", function(data){
        authDataCallback(data);
        if(successCallback){
            successCallback();
        }
    });
}

function getHomeCalendar() {
    home = true;
    parent.location.hash = "homeCalendar";
}

function homeModuleRoles() {
    user = JSON.parse(sessionStorage.getItem("userData"));
    if (UserRoleUtil.isCrcStaff(user)) {
        $("#tempApprovalTab").removeClass("hidden");
        $("#tempApprovalTabContent").removeClass("hidden");

        WidgetUtil.ExpandableSection.hideSection('searchNewApptHeading', 'searchNewApptExpandIcon', 'apptSearchForm');

        $('.appt_schedule_list')
            .css({
                visibility: "visible"
            });
        $('.appt_checkoutnewResource')
            .css({
                visibility: "visible"
            });
        $('.appt_checkoutVisitnewResource')
            .css({
                visibility: "visible"
            });
        $('.appt_newStudySubjectButton')
            .css({
                visibility: "visible"
            });
        $('.newOverrideItemButton')
            .css({
                visibility: "visible"
            });
    } else if (UserRoleUtil.isGeneralFrontOrStaff(user)) {
        $("#tempApprovalTab").addClass("hidden");
        $("#tempApprovalTabContent").addClass("hidden");
    }

    if (user.institutionRole.id == STUDY_STAFF) {
        $('.appt_schedule')
            .css({
                visibility: "visible"
            });
        $('.appt_schedule_list')
            .css({
                visibility: "visible"
            });
        $('.appt_checkoutnewResource')
            .css({
                visibility: "hidden"
            });
        $('.appt_checkoutVisitnewResource')
            .css({
                visibility: "hidden"
            });
        $('.appt_newStudySubjectButton')
            .css({
                visibility: "visible"
            });
        $('.newOverrideItemButton')
            .css({
                visibility: "hidden"
            });

        AppointmentSearchForm.init();
        WidgetUtil.ExpandableSection.hideSection('searchNewApptHeading', 'searchNewApptExpandIcon', 'apptSearchForm');

    } else if (user.institutionRole.id == FRONT_DESK) {
        $('.appt_schedule')
            .css({
                visibility: "visible"
            });
        $('.appt_schedule_list')
            .css({
                visibility: "visible"
            });
        $('.appt_checkoutnewResource')
            .css({
                visibility: "hidden"
            });
        $('.appt_checkoutVisitnewResource')
            .css({
                visibility: "hidden"
            });
        $('.appt_newStudySubjectButton')
            .css({
                visibility: "visible"
            });
        $('.newOverrideItemButton')
            .css({
                visibility: "hidden"
            });

        AppointmentSearchForm.init();
        WidgetUtil.ExpandableSection.hideSection('searchNewApptHeading', 'searchNewApptExpandIcon', 'apptSearchForm');
    } else if (user.institutionRole.id == GENERAL_VIEW) {
        $('.appt_schedule')
            .css({
                visibility: "hidden"
            });
        $('.appt_schedule_list')
            .css({
                visibility: "hidden"
            });
        $('.appt_checkoutnewResource')
            .css({
                visibility: "hidden"
            });
        $('.appt_checkoutVisitnewResource')
            .css({
                visibility: "hidden"
            });
        $('.appt_newStudySubjectButton')
            .css({
                visibility: "hidden"
            });
        $('.newOverrideItemButton')
            .css({
                visibility: "hidden"
            });

        $('#apptSearch').hide();
    }
}


