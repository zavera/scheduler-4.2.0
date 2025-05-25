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
var orderBy;

function checkinAppointmentClick() {
    var comment = $.trim($("#wizard_comment_txtArea").val());
    DialogsUtil.showConfirmationDialog("#checkin-visit-dialog-confirm", {
        buttons: {
            "Yes": function () {
                sessionStorage.setItem("eventid", JSON.stringify(eventid));
                sessionStorage.setItem("comment", JSON.stringify(comment));
                checkinAppointment();
                $(this).dialog("close");
            },
            "No": function () {
                $(this).dialog("close");
                return;
            }
        }
    });
}

function restoreSubjectClick() {
    subject_clearErrors();
    subject_loadEditForm();
}

function subject_clearErrors() {
    $('#subject_firstNameValidation').css({visibility: "hidden"});
    $('#subject_lastNameValidation').css({visibility: "hidden"});
    $('#subject_primaryPhoneValidation').css({visibility: "hidden"});
    $('#subject_sexValidation').css({visibility: "hidden"});
    $('#subject_birthdateValidation').css({visibility: "hidden"});
    $('#subject_raceValidation').css({visibility: "hidden"});
    $('#subject_ethnicityValidation').css({visibility: "hidden"});
    $('#subject_streetAddress1Validation').css({visibility: "hidden"});
    $('#subject_streetAddress2Validation').css({visibility: "hidden"});
    $('#subject_cityValidation').css({visibility: "hidden"});
    $('#subject_zipValidation').css({visibility: "hidden"});
    $('#subject_countryValidation').css({visibility: "hidden"});
    $('#subject_generalValidation').css({visibility: "hidden"});
    $('#subject_mrnValidation').css({visibility: "hidden"});
}

function checkinAppointment() {
    var checkInComment = JSON.parse(sessionStorage.getItem("comment"));
    eventid = JSON.parse(sessionStorage.getItem("eventid"));
    $("#appt_wizard_scheduled_visit_details").dialog("close");
    window.location.hash = '';
    $.blockUI();
    jsonData = JSON.stringify({
        id: eventid,
        comment: checkInComment});

    $.post("rest/appointment/checkinVisit", {data: jsonData}, function (data) {
        $.unblockUI();
        checkInComment = null;
        searching_appointments = false;
        sessionStorage.setItem("is_navigated", false);
        sessionStorage.setItem("eventid", "");

        var checkinCallback = function () {
            homeModule();
            if (start != null) {
                home_calendar.gotoDate(start);
            }
            else {
                home_calendar.gotoDate(new Date());
            }
        };
        DialogsUtil.showMessageDialog("The visit has been Checked-In.", checkinCallback);
    });
}
