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
var app_selectedUser = JSON.parse(sessionStorage.getItem("app_selectedUser"));

function mgmt_setFormMode(mode) {
    mgmt_formMode = mode;
    $('.formElementRequired').css({visibility: "hidden"});
    app_selectedUser = JSON.parse(sessionStorage.getItem("app_selectedUser"));
    if (mode == 'view') {
        renderBreadcrumbs('mgmt_user_view_form');
        $('.formElementRequired').css({visibility: "hidden"});
        $('#mgmt_user_form_title').text('Additional Details');
        $('#mgmt_editUserLink').css({visibility: "visible"});
        $('#mgmt_selected_user_full_name').text(util_buildFullName(app_selectedUser.firstName, app_selectedUser.middleName, app_selectedUser.lastName));
        $('.requiredIcon').css({visibility: "hidden"});
        $('.formTextReadOnly').css({display: "inline"});
        $('#mgmt_firstNameReadOnly').text(app_selectedUser.firstName);
        $('#mgmt_middleNameReadOnly').text(app_selectedUser.middleName);
        $('#mgmt_lastNameReadOnly').text(app_selectedUser.lastName);
        $('#mgmt_ecommonsIdReadOnly').text(app_selectedUser.ecommonsId);
        $('#mgmt_institutionRoleReadOnly').text(app_selectedUser.institutionRoleId == "" ? "" : app_selectedUser.institutionRole);
        $('#mgmt_credentialReadOnly').text(app_selectedUser.credentialId == "" ? "" : app_selectedUser.credential);
        $('#mgmt_departmentReadOnly').text(app_selectedUser.departmentId == "" ? "" : app_selectedUser.department);
        $('#mgmt_divisionReadOnly').text(app_selectedUser.divisionId == "" ? "" : app_selectedUser.division);
        $('#mgmt_institutionReadOnly').text(app_selectedUser.institutionId == "" ? "" : app_selectedUser.institution);
        $("#mgmt_primaryPhoneReadOnly").text(app_selectedUser.primaryPhone);
        $('#mgmt_secondaryPhoneReadOnly').text(app_selectedUser.secondaryPhone);
        $('#mgmt_roleReadOnly').text(app_selectedUser.roleId == "" ? "" : app_selectedUser.role);
        $('#mgmt_facultyRankReadOnly').text(app_selectedUser.facultyRankId == "" ? "" : app_selectedUser.facultyRank);
        $('#mgmt_faxReadOnly').text(app_selectedUser.fax);
        $('#mgmt_pagerReadOnly').text(app_selectedUser.pager);
        $('#mgmt_emailReadOnly').text(app_selectedUser.email);
        $('#mgmt_notificationEmailReadOnly').text(app_selectedUser.notificationEmail == "" ? "" : app_selectedUser.notificationEmail);
    }
    else if (mode == 'new') {
        renderBreadcrumbs('mgmt_user_new_form');
        $('.formTable td').css({height: "50px"});
        $('.formElementRequired').css({visibility: "visible"});
        $('#mgmt_selected_user_full_name').text('');
        $('#mgmt_user_form_title').text('Create New User');
        $('.formTextReadOnly').css({display: "none"});
        $('.formTextInput').css({display: "inline"});
        $('#mgmt_editUserLink').css({visibility: "hidden"});
        $('.requiredFieldKey').css({display: "inline-block"});
        $('#mgmt_editUserLink').css({visibility: "hidden"});
        $('.mgmt_confirmFormItem').css({visibility: "visible"});
        $('.mgmt_passwordFormItem').css({visibility: "visible"});
        $('.requiredIcon').css({visibility: "visible"});
        $('#mgmt_restoreButton').css({display: "none"});
        $('#mgmt_user_form_buttons').css({visibility: "visible"});
        mgmt_clearForm();
    }
    else if (mode == 'edit') {
        renderBreadcrumbs('mgmt_user_edit_form');
        $('.formTable td').css({height: "50px"});
        $('.formElementRequired').css({visibility: "visible"});
        $('#mgmt_user_form_title').text('Edit User');
        $('.requiredFieldKey').css({display: "inline-block"});
        $('.mgmt_confirmFormItem').css({visibility: "visible"});
        $('.mgmt_passwordFormItem').css({visibility: "visible"});
        $('#mgmt_user_form_buttons').css({visibility: "visible"});
        $('.formTextReadOnly').css({display: "none"});
        $('.formTextInput').css({display: "inline"});
        $('.requiredIcon').css({visibility: "visible"});
        $('#mgmt_restoreButton').css({display: "inline"});
        mgmt_loadEditForm();
    }
}

function mgmt_clearForm() {
    $('#mgmt_firstName').val('');
    $('#mgmt_middleName').val('');
    $('#mgmt_lastName').val('');
    $('#mgmt_ecommonsId').val('');
    $('#mgmt_institutionRole').combobox("clear");
    $('#mgmt_department').combobox("clear");
    $('#mgmt_division').combobox("clear");
    $('#mgmt_institution').combobox("clear");
    $('#mgmt_primaryPhone').val('');
    $('#mgmt_secondaryPhone').val('');
    $('#mgmt_credential').combobox("clear");
    $('#mgmt_role').combobox("clear");
    $('#mgmt_facultyRank').combobox("clear");
    $('#mgmt_fax').val('');
    $('#mgmt_pager').val('');
    $('#mgmt_email').val('');
    $('#mgmt_emailConfirm').val('');
    $('#mgmt_password').val('');
    $('#mgmt_passwordConfirm').val('');
    $('#mgmt_notificationEmail').val('');
    $("#mgmt_primaryPhone").mask("(999) 999-9999");
    $("#mgmt_secondaryPhone").mask("(999) 999-9999");
}

function mgmt_loadEditForm() {
    sessionStorage.setItem("previousMode", JSON.stringify('edit'));
    $('#mgmt_password').val('');
    $('#mgmt_passwordConfirm').val('');
    $('#mgmt_firstName').val(app_selectedUser.firstName);
    $('#mgmt_middleName').val(app_selectedUser.middleName);
    $('#mgmt_lastName').val(app_selectedUser.lastName);
    $('#mgmt_ecommonsId').val(app_selectedUser.ecommonsId);
    $('#mgmt_institutionRole').combobox("setValue", app_selectedUser.institutionRoleType);
    $('#mgmt_department').combobox("setValue", app_selectedUser.departmentId);
    $('#mgmt_division').combobox("setValue", app_selectedUser.divisionId);
    $('#mgmt_institution').combobox("setValue", app_selectedUser.institutionId);
    $('#mgmt_primaryPhone').val(app_selectedUser.primaryPhone);
    $('#mgmt_secondaryPhone').val(app_selectedUser.secondaryPhone);
    $("#mgmt_primaryPhone").mask("(999) 999-9999");
    $("#mgmt_secondaryPhone").mask("(999) 999-9999");
    $('#mgmt_credential').combobox("setValue", app_selectedUser.credentialId);
    $('#mgmt_role').combobox("setValue", app_selectedUser.roleId);
    $('#mgmt_facultyRank').combobox("setValue", app_selectedUser.facultyRankId);
    $('#mgmt_fax').val(app_selectedUser.fax);
    $('#mgmt_pager').val(app_selectedUser.pager);
    $('#mgmt_email').val(app_selectedUser.email);
    $('#mgmt_emailConfirm').val(app_selectedUser.email);
    $('#mgmt_notificationEmail').val(app_selectedUser.notificationEmail);
}

function processUserForm() {
    var generateNewPassword = false;
    var isValid = true;
    var emailValid = checkRegexp($.trim($("#mgmt_email").val()), /^((([a-z]|\d|[!#\$%&'\*\+\-\/=\?\^_`{\|}~]|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])+(\.([a-z]|\d|[!#\$%&'\*\+\-\/=\?\^_`{\|}~]|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])+)*)|((\x22)((((\x20|\x09)*(\x0d\x0a))?(\x20|\x09)+)?(([\x01-\x08\x0b\x0c\x0e-\x1f\x7f]|\x21|[\x23-\x5b]|[\x5d-\x7e]|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])|(\\([\x01-\x09\x0b\x0c\x0d-\x7f]|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF]))))*(((\x20|\x09)*(\x0d\x0a))?(\x20|\x09)+)?(\x22)))@((([a-z]|\d|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])|(([a-z]|\d|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])([a-z]|\d|-|\.|_|~|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])*([a-z]|\d|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])))\.)+(([a-z]|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])|(([a-z]|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])([a-z]|\d|-|\.|_|~|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])*([a-z]|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])))\.?$/i);
    var phoneRegexObj = /^\(?([0-9]{3})\)?[-. ]?([0-9]{3})[-. ]?([0-9]{4})$/;
    var pagerRegexObj = /^[0-9]{1,10}$/;
    mgmt_clearErrors();

    if ($.trim($("#mgmt_firstName").val()).length < 1) {
        showError('#mgmt_firstNameValidation');
        isValid = false;
    }
    if ($.trim($("#mgmt_lastName").val()).length < 1) {
        showError('#mgmt_lastNameValidation');
        isValid = false;
    }
    if ($.trim($("#mgmt_ecommonsId").val()).length < 1) {
        showError('#mgmt_ecommonsIdValidation');
        isValid = false;
    }

    if ($.trim($("#mgmt_password").val()).length > 0) {
        if ($.trim($("#mgmt_passwordConfirm").val()).length < 1) {
            showError('#mgmt_passwordConfirmValidation');
            isValid = false;
        }
        if ($.trim($("#mgmt_password").val()).length < 8 || $.trim($("#mgmt_passwordConfirm").val()).length < 8) {
            showError('#mgmt_passwordValidation', 'must be at least 8 characters long');
            isValid = false;
        }
        if (checkPassword($.trim($("#mgmt_password").val())) == false) {
            showError('#mgmt_passwordValidation', 'must contain a lowercase, uppercase, digit, and special character');
            isValid = false;
        }
        if ($.trim($("#mgmt_password").val()) != $.trim($("#mgmt_passwordConfirm").val())) {
            showError('#mgmt_passwordValidation', 'passwords must match');
            isValid = false;
        }
    } else {
        if ($.trim($("#mgmt_password").val()) != $.trim($("#mgmt_passwordConfirm").val())) {
            showError('#mgmt_passwordValidation', 'passwords must match');
            isValid = false;
        }
    }

    if ($.trim($("#mgmt_institutionRole").combobox("getValue")).length < 1) {
        showError('#mgmt_institutionRoleValidation');
        isValid = false;
    }
    if ($.trim($("#mgmt_institution").combobox("getValue")).length < 1) {
        showError('#mgmt_institutionValidation');
        isValid = false;
    }
    if ($.trim($("#mgmt_role").combobox("getValue")).length < 1) {
        showError('#mgmt_roleValidation');
        isValid = false;
    }
    if ($.trim($("#mgmt_primaryPhone").val()).length < 1) {
        showError('#mgmt_primaryPhoneValidation');
        isValid = false;
    }
    if ($.trim($("#mgmt_email").val()).length < 1) {
        showError('#mgmt_emailValidation');
        isValid = false;
    }
    if ($.trim($("#mgmt_emailConfirm").val()).length < 1) {
        showError('#mgmt_emailConfirmValidation');
        isValid = false;
    }

    if (phoneRegexObj.test($.trim($("#mgmt_primaryPhone").val()))) {
        $("#mgmt_primaryPhone").val($.trim($("#mgmt_primaryPhone").val()).replace(phoneRegexObj, "($1) $2-$3"));
    }
    else {
        $('#mgmt_primaryPhoneValidation').text('not a valid phone number format');
        $('#mgmt_primaryPhoneValidation').css({opacity: 0.0, visibility: "visible"}).animate({opacity: 1.0});
        isValid = false;
    }
    if ($.trim($("#mgmt_secondaryPhone").val()).length > 0) {
        if (phoneRegexObj.test($.trim($("#mgmt_secondaryPhone").val()))) {
            $("#mgmt_secondaryPhone").val($.trim($("#mgmt_secondaryPhone").val()).replace(phoneRegexObj, "($1) $2-$3"));
        }
        else {
            $('#mgmt_secondaryPhoneValidation').text('secondary phone not a valid phone number format');
            $('#mgmt_secondaryPhoneValidation').css({opacity: 0.0, visibility: "visible"}).animate({opacity: 1.0});
            isValid = false;
        }
    }

    if (emailValid == false) {
        $('#mgmt_emailValidation').text('not a valid email address');
        $('#mgmt_emailValidation').css({opacity: 0.0, visibility: "visible"}).animate({opacity: 1.0});
        isValid = false;
    }
    if ($.trim($("#mgmt_email").val()) != $.trim($("#mgmt_emailConfirm").val())) {
        $('#mgmt_emailValidation').text('Email addresses must match');
        $('#mgmt_emailValidation').css({opacity: 0.0, visibility: "visible"}).animate({opacity: 1.0});
        isValid = false;
    }
    if ($.trim($("#mgmt_fax").val()).length > 0) {
        if (phoneRegexObj.test($.trim($("#mgmt_fax").val()))) {
            $("#mgmt_fax").val($.trim($("#mgmt_fax").val()).replace(phoneRegexObj, "($1) $2-$3"));
        }
        else {
            $('#mgmt_faxValidation').text('fax not a valid fax number format');
            $('#mgmt_faxValidation').css({opacity: 0.0, visibility: "visible"}).animate({opacity: 1.0});
            isValid = false;
        }
    }
    if ($.trim($("#mgmt_pager").val()).length > 0) {
        if (!pagerRegexObj.test($.trim($("#mgmt_pager").val()))) {
            $('#mgmt_pagerValidation').text('pager not a valid pager number format (1 to 10 digits, numbers only)');
            $('#mgmt_pagerValidation').css({opacity: 0.0, visibility: "visible"}).animate({opacity: 1.0});
            isValid = false;
        }
    }

    if (isValid == false) {
        return;
    }

    if (mgmt_formMode == 'new' && $.trim($("#mgmt_password").val()).length == 0) {
        generateNewPassword = true;
        alert("Password will be sent to the user through email.");
    }

    var credentialValue = $('#mgmt_credential').combobox("getValue");
    var departmentValue = $('#mgmt_department').combobox("getValue");
    var divisionValue = $('#mgmt_division').combobox("getValue");
    var facultyRankValue = $('#mgmt_facultyRank').combobox("getValue");
    var notificationEmailValue = $('#mgmt_notificationEmail').val();

    $('#mgmt_responseLoading').css({visibility: "visible"});
    var jsonData = JSON.stringify({
        id: (app_selectedUser != null ? app_selectedUser.id : 0),
        firstName: $.trim($("#mgmt_firstName").val()),
        middleName: $.trim($("#mgmt_middleName").val()),
        lastName: $.trim($("#mgmt_lastName").val()),
        ecommonsId: $.trim($("#mgmt_ecommonsId").val()),
        password: $.trim($("#mgmt_password").val()),
        institutionRoleType: $.trim($("#mgmt_institutionRole").combobox("getValue")),
        department: (departmentValue != "" ? departmentValue : null),
        division: (divisionValue != "" ? divisionValue : null),
        institutionId: $.trim($("#mgmt_institution").combobox("getValue")),
        credential: credentialValue != "" ? credentialValue : null,
        facultyRank: (facultyRankValue != "" ? facultyRankValue : null),
        roleId: $.trim($("#mgmt_role").combobox("getValue")),
        primaryPhone: $.trim($("#mgmt_primaryPhone").val()),
        secondaryPhone: $.trim($("#mgmt_secondaryPhone").val()),
        email: $.trim($("#mgmt_email").val()),
        notificationEmail: (notificationEmailValue != "" ? notificationEmailValue : null),
        fax: $.trim($("#mgmt_fax").val()),
        pager: $.trim($("#mgmt_pager").val()),
        active: true,
        generateNewPassword: generateNewPassword
    });

    var url = "rest/management/createUser";

    if (mgmt_formMode == 'edit') {
        url = "rest/management/updateUser";
    }

    $.post(url, {data: jsonData}, function (data) {
            var parsedData = JSON.parse(data);
            $('#mgmt_responseLoading').css({visibility: "hidden"});
            var confirmationMessage;
            if (parsedData.result == true) {
                confirmationMessage = "New user created";
                if (mgmt_formMode == 'edit') {
                    confirmationMessage = "User updated";
                }
                mgmt_clearErrors();
                DialogsUtil.showMessageDialog(
                    confirmationMessage,
                    mgmtModule,
                    null,
                    null,
                    null,
                    null,
                    mgmtModule
                );
            }
            else {
                confirmationMessage = parsedData.errorMsg;
                $('#mgmt_formValidation').text(confirmationMessage);
                $('#mgmt_formValidation').css({opacity: 0.0, visibility: "visible"}).animate({opacity: 1.0});
                return;
            }
        }
    );
}

function mgmt_clearErrors() {
    $('#mgmt_firstNameValidation').css({visibility: "hidden"});
    $('#mgmt_lastNameValidation').css({visibility: "hidden"});
    $('#mgmt_primaryPhoneValidation').css({visibility: "hidden"});
    $('#mgmt_secondaryPhoneValidation').css({visibility: "hidden"});
    $('#mgmt_faxValidation').css({visibility: "hidden"});
    $('#mgmt_pagerValidation').css({visibility: "hidden"});
    $('#mgmt_emailValidation').css({visibility: "hidden"});
    $('#mgmt_emailConfirmValidation').css({visibility: "hidden"});
    $('#mgmt_passwordValidation').css({visibility: "hidden"});
    $('#mgmt_passwordConfirmValidation').css({visibility: "hidden"});
    $('#mgmt_institutionRoleValidation').css({visibility: "hidden"});
    $('#mgmt_institutionValidation').css({visibility: "hidden"});
    $('#mgmt_roleValidation').css({visibility: "hidden"});
    $('#mgmt_ecommonsIdValidation').css({visibility: "hidden"});
}

//This specifies a name for this piece of code which will appear when debugging
//This is needed because this file is loaded programatically
//# sourceURL=mgmt_form.js
