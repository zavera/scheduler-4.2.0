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
 * Created by xavier on 9/26/16.
 */

function onloadHomePage() {

    var deferredHeader = $.Deferred();

    eraseLicense();
    loadMetaHeaders();

    $("#apptModuleTabs").tabs();

    $("#fullHeader").load("header.html", function () {
        $('.homelink').addClass('active');
        app_runIdleTimer();
        initFooter();
        deferredHeader.resolve();
    });

    $.ajax({
        type: "GET",
        url: "rest/appointment/getLastMinuteRestriction",
        data: "",
        success: function (data) {
            var text = "Booked <" + data + " Hours";
            if (data !== "0") {
                $(".lastMinuteText").append(text);
                $("#lastMinuteTop").addClass("calendarKeyLastMinute");
                $("#lastMinuteBottom").addClass("calendarKeyLastMinute");
            }
        }
    });

    $.when(deferredHeader).done(function () {
        renderBreadcrumbs("home_screen");
        $('#templateSection').load("wizard_templates.html");
        $('#appt-common-dialogs').load("appts_dialogs.html");
        login(homeInit);
    });
}

var Home = {};

Home.toggleToCalendarView = function (reload){
    $("#calendarTab").show();
    $("#appt_list").hide();

    window.location.hash = "#homeCalendar";

    if(reload || !calendar_rendered){
        homeModuleRoles();
        homeMain();
    }
};

Home.toggleToListView = function(){
    $("#calendarTab").hide();
    $("#appt_list").show();
    window.location.hash = "homeList";
    sessionStorage.setItem("apptListSelectedMaxResults", 50);
    appointmentListSection();
    $.unblockUI();
};

function appointmentListSection(){
    $("#calendarTab").hide();
    $("#appt_list").show();
    window.location.hash = "homeList";
    $("#appt_list").load("appt_list.html", loadAppointmentList);
}

function loadAppointmentList(){
    appointment_viewStack('appointment_list_view');
}

function homeInit(){
    $.blockUI({ignoreIfBlocked: true});

    getSchedulingOptionsStaticList(homeMain);
}

function homeMain(){
    if(window.location.hash !== "#homeList") {
        if (UserRoleUtil.isCrcStaff(user)) {
            AppointmentSearchForm.init();
        }

        $.blockUI({ignoreIfBlocked: true});

        createCalendarAndSublocationTabs();

        createApptCalendarDatePicker();
        eraseLicense();

        appointment_viewStack('appointment_main_screen');
        $("#booked_visit_radio").prop("checked", true);
        $("#booked_visit_radio").click(function () {
            $("#booked_visit_radio").prop("checked", true);
        });
        $("#visit_template_radio").click(function () {
            $("#visit_template_radio").prop("checked", true);
        });

        $(".weekday").on("change", function () {
            toggleWeekDaysCheckBoxes();
        });

        $(".weekend").on("change", function () {
            toggleWeekDaysCheckBoxes();
        });

        calendar_rendered = true;
    }else{
        $("#home_toggleList").click();
    }
}

