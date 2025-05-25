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
function passwordResetLink() {
    $("#index_passwordResetDiv").css({display: 'block'});
    $('#index_passwordResetDiv').css({opacity: 0.0, visibility: "visible"}).animate({opacity: 1.0});
    $("#index_passwordResetLink").css({display: 'none'});
    $("#index_passwordResetMsg").css({display: 'inline'});
    $('#index_passwordResetMsg').css({opacity: 0.0, visibility: "visible"}).animate({opacity: 1.0});
    $("#index_passwordResetSuccess").css({display: 'none'});
    $("#index_passwordResetFailure").css({display: 'none'});
}

function initLogin() {
    $(".headerLoginNav").show();

    initFooter();
}

function initRegistration() {
    $(".headerNavTabs").hide();
    $(".headerLoginNav").show();
    $(".logoutLink").hide();

    getRegistrationStaticLists();

    initFooter();
}

function resetParameters() {
    sessionStorage.setItem("is_navigated", false);
    sessionStorage.setItem("eventid", "");
}

function registrationLink() {
    window.location.href = "registration.html";
}

function cancelRegistration() {
    window.location.href = ".";
}

function submitRegistration() {
    var isValid = true;
    var emailValid = checkRegexp($.trim($("#registration_email").val()), /^((([a-z]|\d|[!#\$%&'\*\+\-\/=\?\^_`{\|}~]|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])+(\.([a-z]|\d|[!#\$%&'\*\+\-\/=\?\^_`{\|}~]|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])+)*)|((\x22)((((\x20|\x09)*(\x0d\x0a))?(\x20|\x09)+)?(([\x01-\x08\x0b\x0c\x0e-\x1f\x7f]|\x21|[\x23-\x5b]|[\x5d-\x7e]|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])|(\\([\x01-\x09\x0b\x0c\x0d-\x7f]|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF]))))*(((\x20|\x09)*(\x0d\x0a))?(\x20|\x09)+)?(\x22)))@((([a-z]|\d|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])|(([a-z]|\d|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])([a-z]|\d|-|\.|_|~|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])*([a-z]|\d|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])))\.)+(([a-z]|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])|(([a-z]|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])([a-z]|\d|-|\.|_|~|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])*([a-z]|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])))\.?$/i);
    var regexObj = /^\(?([0-9]{3})\)?[-. ]?([0-9]{3})[-. ]?([0-9]{4})$/;
    registration_clearErrors();

    if ($.trim($("#registration_firstName").val()).length < 1) {
        showError('#registration_firstNameValidation');
        isValid = false;
    }
    if ($.trim($("#registration_lastName").val()).length < 1) {
        showError('#registration_lastNameValidation');
        isValid = false;
    }
    if ($.trim($("#registration_ecommonsId").val()).length < 1) {
        showError('#registration_ecommonsIdValidation');
        isValid = false;
    }

    if ($.trim($("#registration_password").val()).length < 1) {
        showError('#registration_passwordValidation');
        isValid = false;
    }

    if ($.trim($("#registration_password").val()).length > 0) {
        if ($.trim($("#registration_passwordConfirm").val()).length < 1) {
            showError('#registration_passwordConfirmValidation');
            isValid = false;
        }
        if ($.trim($("#registration_passwordConfirm").val()).length < 1) {
            showError('#registration_passwordConfirmValidation');
            isValid = false;
        }
        if ($.trim($("#registration_password").val()).length < 8 || $.trim($("#registration_passwordConfirm").val()).length < 8) {
            showError('#registration_passwordValidation', 'must be at least 8 chararcters long');
            isValid = false;
        }
        if (checkPassword($.trim($("#registration_password").val())) == false) {
            showError('#registration_passwordValidation', 'must contain a lowercase, uppercase, digit, and special character');
            isValid = false;
        }
        if ($.trim($("#registration_password").val()) != $.trim($("#registration_passwordConfirm").val())) {
            showError('#registration_passwordValidation', 'passwords must match');
            isValid = false;
        }
    }

    if ($.trim($("#registration_institution").combobox("getValue")).length < 1) {
        showError('#registration_institutionValidation');
        isValid = false;
    }
    if ($.trim($("#registration_role").combobox("getValue")).length < 1) {
        showError('#registration_roleValidation');
        isValid = false;
    }
    if ($.trim($("#registration_primaryPhone").val()).length < 1) {
        showError('#registration_primaryPhoneValidation');
        isValid = false;
    }
    if ($.trim($("#registration_email").val()).length < 1) {
        showError('#registration_emailValidation');
        isValid = false;
    }
    if ($.trim($("#registration_emailConfirm").val()).length < 1) {
        showError('#registration_emailConfirmValidation');
        isValid = false;
    }

    if (regexObj.test($.trim($('#registration_primaryPhone').val()))) {
        $("#registration_primaryPhone").val($.trim($('#registration_primaryPhone').val()).replace(regexObj, "($1) $2-$3"));
    }
    else {
        $('#registration_primaryPhoneValidation').text('not a valid phone number format');
        $('#registration_primaryPhoneValidation').css({opacity: 0.0, visibility: "visible"}).animate({opacity: 1.0});
        isValid = false;
    }
    if ($.trim($("#registration_secondaryPhone").val()).length > 0) {
        if (regexObj.test($('#registration_secondaryPhone').val())) {
            $("#registration_secondaryPhone").val($('#registration_secondaryPhone').val().replace(regexObj, "($1) $2-$3"));
        }
        else {
            $('#registration_secondaryPhoneValidation').text('secondary phone not a valid phone number format');
            $('#registration_secondaryPhoneValidation').css({
                opacity: 0.0,
                visibility: "visible"
            }).animate({opacity: 1.0});
            isValid = false;
        }
    }

    if (emailValid == false) {
        $('#registration_emailValidation').text('not a valid email address');
        $('#registration_emailValidation').css({opacity: 0.0, visibility: "visible"}).animate({opacity: 1.0});
        isValid = false;
    }
    if ($.trim($("#registration_email").val()) != $.trim($("#registration_emailConfirm").val())) {
        $('#registration_emailValidation').text('email addresses must match');
        $('#registration_emailValidation').css({opacity: 0.0, visibility: "visible"}).animate({opacity: 1.0});
        isValid = false;
    }
    if ($.trim($("#registration_fax").val()).length > 0) {
        if (regexObj.test($('#registration_fax').val())) {
            $("#registration_fax").val($('#registration_fax').val().replace(regexObj, "($1) $2-$3"));
        }
        else {
            $('#registration_faxValidation').text('fax not a valid fax number format');
            $('#registration_faxValidation').css({opacity: 0.0, visibility: "visible"}).animate({opacity: 1.0});
            isValid = false;
        }
    }
    if ($.trim($("#registration_pager").val()).length > 0) {
        if (regexObj.test($('#registration_pager').val())) {
            $("#registration_pager").val($('#registration_pager').val().replace(regexObj, "($1) $2-$3"));
        }
        else {
            $('#registration_pagerValidation').text('pager not a valid pager number format');
            $('#registration_pagerValidation').css({opacity: 0.0, visibility: "visible"}).animate({opacity: 1.0});
            isValid = false;
        }
    }

    if (isValid == false) {
        return;
    }

    var credentialValue = $('#registration_credential').combobox("getValue");
    var departmentValue = $('#registration_department').combobox("getValue");
    var divisionValue = $('#registration_division').combobox("getValue");
    var facultyRankValue = $('#registration_facultyRank').combobox("getValue");

    $('#registration_responseLoading').css({visibility: "visible"});

    var jsonData = JSON.stringify({
        firstName: $.trim($("#registration_firstName").val()),
        middleName: $.trim($("#registration_middleName").val()),
        lastName: $.trim($("#registration_lastName").val()),
        ecommonsId: $.trim($("#registration_ecommonsId").val()),
        password: $.trim($("#registration_password").val()),
        department: (departmentValue != "" ? departmentValue : null),
        division: (divisionValue != "" ? divisionValue : null),
        institutionId: $.trim($('#registration_institution').combobox("getValue")),
        credential: credentialValue != "" ? credentialValue : null,
        facultyRank: (facultyRankValue != "" ? facultyRankValue : null),
        roleId: $.trim($('#registration_role').combobox("getValue")),
        primaryPhone: $.trim($("#registration_primaryPhone").val()),
        secondaryPhone: $.trim($("#registration_secondaryPhone").val()),
        email: $.trim($("#registration_email").val()),
        fax: $.trim($("#registration_fax").val()),
        pager: $.trim($("#registration_pager").val()),
        active: false
    });

    $.post("rest/app/registerUser", {data: jsonData}, function (data) {
            var parsedData = JSON.parse(data);
            $('#registration_responseLoading').css({visibility: "hidden"});
            var confirmationMessage;
            if (parsedData.result == true) {
                confirmationMessage = "New user registration complete.";
                registration_clearErrors();
                registration_clearForm();
                alert(confirmationMessage);

                var jsonData = JSON.stringify({
                    id: parsedData.id,
                });
                $.post("rest/app/sendRegistrationEmail", {data: jsonData}, function (data) {
                    console.log("Done");
                })
            }
            else {
                fontColor = "red";
                confimationMessage = parsedData.errorMsg;
                $('#registration_formValidation').text(confimationMessage);
                $('#registration_formValidation').css({opacity: 0.0, visibility: "visible"}).animate({opacity: 1.0});
                return;
            }
        }
    );
}

function registration_clearForm() {
    $('#registration_firstName').val('');
    $('#registration_middleName').val('');
    $('#registration_lastName').val('');
    $('#registration_ecommonsId').val('');
    $('#registration_department').combobox("setValue", '');
    $('#registration_division').combobox("setValue", '');
    $('#registration_institution').combobox("setValue", '');
    $('#registration_primaryPhone').val('');
    $('#registration_secondaryPhone').val('');
    $('#registration_credential').combobox("setValue", '');
    $('#registration_role').combobox("setValue", '');
    $('#registration_facultyRank').combobox("setValue", '');
    $('#registration_fax').val('');
    $('#registration_pager').val('');
    $('#registration_email').val('');
    $('#registration_emailConfirm').val('');
    $('#registration_password').val('');
    $('#registration_passwordConfirm').val('');
    $('#registration_formValidation').css({visibility: "hidden"});
}

function registration_clearErrors() {
    $('#registration_firstNameValidation').css({visibility: "hidden"});
    $('#registration_lastNameValidation').css({visibility: "hidden"});
    $('#registration_primaryPhoneValidation').css({visibility: "hidden"});
    $('#registration_emailValidation').css({visibility: "hidden"});
    $('#registration_emailConfirmValidation').css({visibility: "hidden"});
    $('#registration_passwordValidation').css({visibility: "hidden"});
    $('#registration_passwordConfirmValidation').css({visibility: "hidden"});
    $('#registration_institutionValidation').css({visibility: "hidden"});
    $('#registration_roleValidation').css({visibility: "hidden"});
    $('#registration_ecommonsIdValidation').css({visibility: "hidden"});
    $('#registration_formValidation').css({visibility: "hidden"});
}


function submitResetPasswordLink() {
    if ($.trim($("#index_email").val()).length < 1) {
        return;
    }
    $('#index_passwordResetLoading').css({visibility: "visible"});
    var jsonData = JSON.stringify({email: $('#index_email').val()});

    $.post("rest/app/unAuthenticatedPasswordReset", {data: jsonData},
        function (data) {
            var parsedData = JSON.parse(data);
            var result = parsedData.result;

            $("#index_passwordResetSuccess").css({display: 'none'});
            $("#index_passwordResetFailure").css({display: 'none'});
            $('#index_passwordResetLoading').css({visibility: "hidden"});
            $("#index_passwordResetMsg").css({display: 'none'});

            if (result == true) {
                $("#index_passwordResetSuccess").css({display: 'inline'});
                $('#index_passwordResetSuccess').css({opacity: 0.0, visibility: "visible"}).animate({opacity: 1.0});
                $("#index_passwordResetDiv").css({display: 'none'});
                $("#index_passwordResetLink").css({display: 'block'});
                $("#index_email").val('');
            }
            else {
                $("#index_passwordResetFailure").css({display: 'inline'});
                $('#index_passwordResetFailure').css({opacity: 0.0, visibility: "visible"}).animate({opacity: 1.0});
            }
        }
    );
}

//This specifies a name for this piece of code which will appear when debugging
//This is needed because this file is loaded programatically
//# sourceURL=landing_screen.js