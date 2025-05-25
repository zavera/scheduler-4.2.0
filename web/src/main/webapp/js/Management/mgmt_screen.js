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
var updatedRoles;
var rolesExceptFinalApprover;

var MgmtPage = (function () {
    var initFn = function (getMgmtStaticListsCallback) {
        app_runIdleTimer();
        commonUserScreenData();
        loadMetaHeaders();
        initFooter();
        eraseLicense();
        getMgmtStaticLists(getMgmtStaticListsCallback);
    };
    return {
        init: initFn
    };
}());

function getMgmtStaticLists(callback) {
    $.get("rest/app/getStaticLists", {}, function (data) {

        parsedData = JSON.parse(data);

        roles = parsedData.roles;
        institutionRoles = parsedData.institutionRoles;
        institutions = parsedData.institutions;
        credentials = parsedData.credentials;
        divisions = parsedData.divisions;
        departments = parsedData.departments;
        facultyRanks = parsedData.facultyRanks;

        roleSelectOptions = buildSelectOptions(roles, 'name');
        institutionRoleSelectOptions = buildRoleSelectOptions(institutionRoles, 'name');
        institutionSelectOptions = buildSelectOptions(institutions, 'longName');
        credentialSelectOptions = buildSelectOptions(credentials, 'name', "");
        departmentSelectOptions = buildSelectOptions(departments, 'name', "");
        divisionSelectOptions = buildSelectOptions(divisions, 'name', "");
        facultyRankSelectOptions = buildSelectOptions(facultyRanks, 'name', "");

        $("#mgmt_institution").html(institutionSelectOptions);
        $("#mgmt_institutionRole").html(institutionRoleSelectOptions);
        $("#mgmt_credential").html(credentialSelectOptions);
        $("#mgmt_division").html(divisionSelectOptions);
        $("#mgmt_department").html(departmentSelectOptions);
        $("#mgmt_facultyRank").html(facultyRankSelectOptions);

        WidgetUtil.createComboBox("#mgmt_role", {
            textField: "name",
            valueField: "id",
            data: roles
        });
        WidgetUtil.createComboBox("#mgmt_institution");
        WidgetUtil.createComboBox("#mgmt_credential");
        WidgetUtil.createComboBox("#mgmt_division");
        WidgetUtil.createComboBox("#mgmt_department");
        WidgetUtil.createComboBox("#mgmt_facultyRank");

        WidgetUtil.createComboBox($("#mgmt_institutionRole"), {
            onChange: function () {
                institutionRoleChangeClick();
            }
        });

        if (callback != undefined) {
            callback();
        }
    });
}

function commonUserScreenData() {
    commonData();
    renderBreadcrumbs('mgmt_screen');
    initial_load = true;
    resetUI();
}

function commonCreateUserData() {
    commonData();
    getUpdatedRolesLists(function() {
        var mode = JSON.parse(sessionStorage.getItem("mode"));
        mgmt_setFormMode(mode);
        if (mode == 'edit') {
            mgmt_loadEditForm();
        }
    });
}

function commonUserDetailData() {
    commonData();
    app_selectedUser = JSON.parse(sessionStorage.getItem("app_selectedUser"));
    mgmt_setFormMode('view');
}

function addNewUserClick() {
    sessionStorage.setItem("mode", JSON.stringify('new'));
    window.location.href = "create_user.html";
}


function editUserClick() {
    sessionStorage.setItem("mode", JSON.stringify('edit'));
    window.location.href = "create_user.html";
}

function editUser() {
    sessionStorage.setItem("mode", JSON.stringify('edit'));
    window.location.href = "create_user.html";
};

function institutionRoleChangeClick() {
    var newRoles;
    if (($.trim($("#mgmt_institutionRole").combobox("getValue")) == "ROLE_STUDY_STAFF")
        || ($.trim($("#mgmt_institutionRole").combobox("getValue")) == "ROLE_GENERAL_VIEW")) {
        newRoles = rolesExceptFinalApprover;
    }
    else {
        newRoles = roles;
    }

    $("#mgmt_role").combobox("loadData", newRoles);
}


function getUpdatedRolesLists(callback) {
    $.getJSON("rest/management/getRolesList", function (data) {
        rolesExceptFinalApprover = data;

        if(callback)
        {
            callback();
        }
    });
}

function userEditData() {
    sessionStorage.setItem("mode", JSON.stringify('edit'));
    window.location.href = "create_user.html";
}

function userReadData() {
    sessionStorage.setItem("mode", JSON.stringify('search_view'));
    window.location.href = "mgmt_detail.html";
}

function showUserDetail(id) {
    $.getJSON("rest/management/getUserData?user=" + id, function (data) {
        sessionStorage.setItem("app_selectedUser", JSON.stringify(data));
        sessionStorage.setItem("mode", JSON.stringify('view'));
        window.location.href = "mgmt_detail.html";
    });
}
function mgmtUserDetails() {
    var user = JSON.parse(sessionStorage.getItem("app_selectedUser"));
    showUserDetail(user.id);
}

function statusDialog(id) {
    DialogsUtil.showConfirmationDialog("#status-dialog-confirm", {
        buttons: {
            "Yes": function () {
                $.getJSON("rest/management/changeUserStatus?user=" + id, function (data) {
                    UserTable.resetToPageOne()
                    util_showMainMessage("User Status changed");
                });
                $(this).dialog("close");
            },
            "No": function () {
                $(this).dialog("close");
                return;
            }
        }
    });
}

// used by user table, "Detail" column
function userChangeStatusConfirmation(id) {
    statusDialog(id);
}

function crudUserClick() {
    $.blockUI();
    processUserForm();
    $.unblockUI();
}

function passwordResetClick() {
    user = JSON.parse(sessionStorage.getItem("userData"));
    var isValid = true;
    $('#mgmt_passwordChangeValidation').css({visibility: "hidden"});
    $('#mgmt_passwordChangeConfirmValidation').css({visibility: "hidden"});
    if ($.trim($("#mgmt_passwordChange").val()).length < 1) {
        showError('#mgmt_passwordChangeValidation');
        isValid = false;
    }
    if ($.trim($("#mgmt_passwordChangeConfirm").val()).length < 1) {
        showError('#mgmt_passwordChangeConfirmValidation');
        isValid = false;
    }
    if ($.trim($("#mgmt_passwordChangeConfirm").val()).length < 1) {
        showError('#mgmt_passwordChangeConfirmValidation');
        isValid = false;
    }
    if ($.trim($("#mgmt_passwordChange").val()).length < 8 || $.trim($("#mgmt_passwordChangeConfirm").val()).length < 8) {
        showError('#mgmt_passwordChangeValidation', 'must be at least 8 chararcters long');
        isValid = false;
    }
    if (checkPassword($.trim($("#mgmt_passwordChange").val())) == false) {
        showError('#mgmt_passwordChangeValidation', 'must contain a lowercase, uppercase, digit, and special character');
        isValid = false;
    }
    if ($.trim($("#mgmt_passwordChange").val()) != $.trim($("#mgmt_passwordChangeConfirm").val())) {
        showError('#mgmt_passwordChangeValidation', 'passwords must match');
        isValid = false;
    }

    if (isValid == false) {
        return;
    }

    $('#mgmt_passwordChangeLoading').css({visibility: "visible"});
    var jsonData = JSON.stringify({id: user.id, password: $.trim($("#mgmt_passwordChange").val())});

    $.post("rest/management/updatePassword", {data: jsonData}, function (data) {
        var parsedData = JSON.parse(data);
        $('#mgmt_responseLoading').css({visibility: "hidden"});
        var confirmationMessage = "";
        if (parsedData.result == true) {
            confirmationMessage = "Password updated";
            $('#mgmt_passwordChangeValidation').css({visibility: "hidden"});
            $('#mgmt_passwordChangeConfirmValidation').css({visibility: "hidden"});
            $('#mgmt_passwordChange').val('');
            $('#mgmt_passwordChangeConfirm').val('');
            if (user.previousLoginTime == "") {
                alert("Password updated and system is now going to log you out. Please login with your new password.");
                sessionStorage.removeItem("userData");
                window.location.href = "logout";
            }
            else {
                util_showMainMessage(confirmationMessage);
            }
        }
        else {
            confirmationMessage = parsedData.errorMsg;
            util_showMainMessage(confirmationMessage);
        }
    });
}

function restoreUserClick() {
    mgmt_clearErrors();
    mgmt_loadEditForm();
}

function cancelUserClick() {
    if (mgmt_formMode == 'new') {
        window.location.href = "mgmt_screen.html";
    }
    else {
        sessionStorage.setItem("mode", JSON.stringify('view'));
        window.location.href = "mgmt_detail.html";
    }
}

//This specifies a name for this piece of code which will appear when debugging
//This is needed because this file is loaded programatically
//# sourceURL=mgmt_screen.js

function loadUserData() {

    // NEW implementation:
    UserTable.createUserTable();
    UserTable.resetToPageOne();

    commonData();

}
