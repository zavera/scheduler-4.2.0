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
describe('user management form', function() {

    function setUpUserInSession() {

        user = {
            firstName: "first name",
            middleName: "middle name",
            lastName: "last name",
            ecommonsId: "1",
            institutionRoleId: "3", //
            institutionRole: "Co-Investigator",
            institutionRoleType: "Schedulers",
            credentialId: "2",
            credential: "PhD",
            departmentId: "5",
            department: "Neurology",
            divisionId: "7",
            division: "Nutrition",
            institutionId: "5",
            institution: "Demo Institution 5",
            primaryPhone: "(111) 111-1111",
            secondaryPhone: "(222) 222-2222",
            roleId: "3",
            role: "Schedulers",
            facultyRankId: "4",
            facultyRank: "Professor",
            fax: "(333) 333-3333",
            pager: "(444) 444-4444",
            email: "a@example.com",
            notificationEmail: "b@example.com"
        };
        sessionStorage.setItem("app_selectedUser", JSON.stringify(user));

        return user;

    }

    function loadPage(mode) {

        var server = UtilHelper.getFakeServer();

        AjaxHelper.setupFakeServerTextResponseFromTestFixture(server, "rest/app/getStaticLists", "global/json/static-lists.json");
        AjaxHelper.setupFakeServerTextResponseFromTestFixture(server, "rest/management/getRolesList", "management/json/roles.json");

        sessionStorage.setItem("mode", JSON.stringify(mode));
        MgmtPage.init(commonCreateUserData);

        // respond once for each AJAX call
        server.respond();
        server.respond();

    }

    it('has a view mode', function() {

        var user = setUpUserInSession();

        FixtureHelper.loadSourceHtmlFixtureIntoDom("mgmt_detail.html");

        loadPage("view");

        // mgmt_setFormMode('view');

        // verify breadcrumbs

        var expectedBreadcrumbData = [
            {
                href: "javascript:homeModule()",
                text: "Home"
            },
            {
                href: "javascript:mgmtModule()",
                text: "Management"
            },
            {
                text: "User Details"
            }
        ];
        DomVerificationHelper.verifyBreadcrumbs(expectedBreadcrumbData);

        // verify displayed user data

        $('.formElementRequired').each(function(index, element) {
            expect(element.css('visibility')).toBe('hidden');
        });
        expect($('#mgmt_user_form_title').text()).toBe('Additional Details');

        expect($('#mgmt_editUserLink').css('visibility')).toBe("visible");

        expect($('.requiredIcon').css('visibility')).toBe("hidden");

        expect($('#mgmt_selected_user_full_name').text()).toBe(user.firstName + " " + user.middleName + " " + user.lastName);
        expect($('#mgmt_firstNameReadOnly').text()).toBe(user.firstName);
        expect($('#mgmt_middleNameReadOnly').text()).toBe(user.middleName);
        expect($('#mgmt_lastNameReadOnly').text()).toBe(user.lastName);
        expect($('#mgmt_ecommonsIdReadOnly').text()).toBe(user.ecommonsId);
        expect($('#mgmt_institutionRoleReadOnly').text()).toBe(user.institutionRole);
        expect($('#mgmt_credentialReadOnly').text()).toBe(user.credential);
        expect($('#mgmt_departmentReadOnly').text()).toBe(user.department);
        expect($('#mgmt_divisionReadOnly').text()).toBe(user.division);
        expect($('#mgmt_institutionReadOnly').text()).toBe(user.institution);
        expect($("#mgmt_primaryPhoneReadOnly").text()).toBe(user.primaryPhone);
        expect($('#mgmt_secondaryPhoneReadOnly').text()).toBe(user.secondaryPhone);
        expect($('#mgmt_roleReadOnly').text()).toBe(user.role);
        expect($('#mgmt_facultyRankReadOnly').text()).toBe(user.facultyRank);
        expect($('#mgmt_faxReadOnly').text()).toBe(user.fax);
        expect($('#mgmt_pagerReadOnly').text()).toBe(user.pager);
        expect($('#mgmt_emailReadOnly').text()).toBe(user.email);
        expect($('#mgmt_notificationEmailReadOnly').text()).toBe(user.notificationEmail);

    });

    it('has an edit mode', function() {

        var user = setUpUserInSession();

        FixtureHelper.loadSourceHtmlFixtureIntoDom("create_user.html");

        loadPage('edit');

        // verify breadcrumbs

        var expectedBreadcrumbData = [
            {
                href: "javascript:homeModule()",
                text: "Home"
            },
            {
                href: "javascript:mgmtModule()",
                text: "Management"
            },
            {
                href: 'javascript:mgmtUserDetails()',
                text: "User Details"
            },
            {
                text: 'Edit User'
            }
        ];
        DomVerificationHelper.verifyBreadcrumbs(expectedBreadcrumbData);

        // verify displayed user data

        DomVerificationHelper.verifyMatchedElements('.formElementRequired', [
            {type: 'css', property: 'visibility', value: 'visible'},
            {type: 'html', value: ''}
        ]);
        expect($('#mgmt_user_form_title').text()).toBe('Edit User');
        DomVerificationHelper.verifyMatchedElements('.requiredFieldKey', [
            {type: 'css', property: 'display', value: 'inline-block'}
        ]);
        DomVerificationHelper.verifyMatchedElements('.mgmt_confirmFormItem', [
            {type: 'css', property: 'visibility', value: 'visible'}
        ]);
        DomVerificationHelper.verifyMatchedElements('.mgmt_passwordFormItem', [
            {type: 'css', property: 'visibility', value: 'visible'}
        ]);
        expect($('#mgmt_user_form_buttons').css('visibility')).toBe("visible");
        DomVerificationHelper.verifyMatchedElements('.formTextReadOnly', [
            {type: 'css', property: 'display', value: 'none'}
        ]);
        DomVerificationHelper.verifyMatchedElements('.formTextInput', [
            {type: 'css', property: 'display', value: 'inline-block'} // why not inline?
        ]);
        DomVerificationHelper.verifyMatchedElements('.mgmt_passwordFormItem', [
            {type: 'css', property: 'visibility', value: 'visible'}
        ]);
        DomVerificationHelper.verifyMatchedElements('.requiredIcon', [
            {type: 'css', property: 'visibility', value: 'visible'}
        ]);
        expect($('#mgmt_restoreButton').css('display')).toBe("inline-block"); // why not inline?

        expect($('#mgmt_firstName').val()).toBe(user.firstName);
        expect($('#mgmt_middleName').val()).toBe(user.middleName);
        expect($('#mgmt_lastName').val()).toBe(user.lastName);
        expect($('#mgmt_ecommonsId').val()).toBe(user.ecommonsId);
        expect($('#mgmt_role').combobox('getText')).toBe(user.institutionRole);
        expect($('#mgmt_credential').combobox('getText')).toBe(user.credential);
        expect($('#mgmt_department').combobox('getText')).toBe(user.department);
        expect($('#mgmt_division').combobox('getText')).toBe(user.division);
        expect($('#mgmt_institution').combobox('getText')).toBe(user.institution);
        expect($("#mgmt_primaryPhone").val()).toBe(user.primaryPhone);
        expect($('#mgmt_secondaryPhone').val()).toBe(user.secondaryPhone);
        expect($('#mgmt_facultyRank').combobox('getText')).toBe(user.facultyRank);
        expect($('#mgmt_fax').val()).toBe(user.fax);
        expect($('#mgmt_pager').val()).toBe(user.pager);
        expect($('#mgmt_email').val()).toBe(user.email);
        expect($('#mgmt_notificationEmail').val()).toBe(user.notificationEmail);

    });

    it('has a new-user mode', function() {

        FixtureHelper.loadSourceHtmlFixtureIntoDom("create_user.html");

        loadPage('new');

        // verify breadcrumbs

        var expectedBreadcrumbData = [
            {
                href: "javascript:homeModule()",
                text: "Home"
            },
            {
                href: "javascript:mgmtModule()",
                text: "Management"
            },
            {
                text: "Create New User"
            }
        ];
        DomVerificationHelper.verifyBreadcrumbs(expectedBreadcrumbData);

        // verify displayed user data

        DomVerificationHelper.verifyMatchedElements('.formElementRequired', [
            {type: 'css', property: 'visibility', value: 'visible'},
            {type: 'html', value: ''}
        ]);
        expect($('#mgmt_user_form_title').text()).toBe('Create New User');
        DomVerificationHelper.verifyMatchedElements('.requiredFieldKey', [
            {type: 'css', property: 'display', value: 'inline-block'}
        ]);
        DomVerificationHelper.verifyMatchedElements('.mgmt_confirmFormItem', [
            {type: 'css', property: 'visibility', value: 'visible'}
        ]);
        DomVerificationHelper.verifyMatchedElements('.mgmt_passwordFormItem', [
            {type: 'css', property: 'visibility', value: 'visible'}
        ]);
        expect($('#mgmt_user_form_buttons').css('visibility')).toBe("visible");
        DomVerificationHelper.verifyMatchedElements('.formTextReadOnly', [
            {type: 'css', property: 'display', value: 'none'}
        ]);
        DomVerificationHelper.verifyMatchedElements('.formTextInput', [
            {type: 'css', property: 'display', value: 'inline-block'} // why not inline?
        ]);
        DomVerificationHelper.verifyMatchedElements('.mgmt_passwordFormItem', [
            {type: 'css', property: 'visibility', value: 'visible'}
        ]);
        DomVerificationHelper.verifyMatchedElements('.requiredIcon', [
            {type: 'css', property: 'visibility', value: 'visible'}
        ]);
        expect($('#mgmt_restoreButton').css('display')).toBe("none");

        expect($('#mgmt_firstName').val()).toBe('');
        expect($('#mgmt_middleName').val()).toBe('');
        expect($('#mgmt_lastName').val()).toBe('');
        expect($('#mgmt_ecommonsId').val()).toBe('');
        expect($('#mgmt_role').combobox('getText')).toBe('');
        expect($('#mgmt_credential').combobox('getText')).toBe('');
        expect($('#mgmt_department').combobox('getText')).toBe('');
        expect($('#mgmt_division').combobox('getText')).toBe('');
        expect($('#mgmt_institution').combobox('getText')).toBe('');
        expect($("#mgmt_primaryPhone").val()).toBe('');
        expect($('#mgmt_secondaryPhone').val()).toBe('');
        expect($('#mgmt_facultyRank').combobox('getText')).toBe('');
        expect($('#mgmt_fax').val()).toBe('');
        expect($('#mgmt_pager').val()).toBe('');
        expect($('#mgmt_email').val()).toBe('');
        expect($('#mgmt_notificationEmail').val()).toBe('');

    });

    it('validates user input', function() {

        // set up the new user form

        FixtureHelper.loadSourceHtmlFixtureIntoDom("create_user.html");

        loadPage('new');

        // initial empty form has no validation errors

        verifyValidationErrorsNotVisibleExceptFor([]);

        // immediately submitted empty form has validation errors on all required fields

        $('#mgmt_submitButton').click();

        verifyValidationErrorMessagesVisible();

        // form with all valid required input has no validation errors

        $('#mgmt_firstName').val('a');
        $('#mgmt_lastName').val('b');
        $('#mgmt_ecommonsId').val('c');
        $('#mgmt_institutionRole').combobox('setValue', 1);
        $('#mgmt_institution').combobox('setValue', 1);
        $('#mgmt_role').combobox('setValue', 1);
        $('#mgmt_email').val('g');
        $('#mgmt_emailConfirm').val('g');
        $('#mgmt_primaryPhone').val('1111111111');
        $('#mgmt_email').val('a@example.com');
        $('#mgmt_emailConfirm').val('a@example.com');

        $('#mgmt_submitButton').click();

        verifyValidationErrorsNotVisibleExceptFor([]);

        // email confirmation does not match

        $('#mgmt_email').val('bb@cc');
        $('#mgmt_emailConfirm').val('a@example.com');

        $('#mgmt_submitButton').click();

        verifyValidationErrorsNotVisibleExceptFor(['#mgmt_emailValidation']);
        verifyValidationErrorMessage('#mgmt_emailValidation', 'Email addresses must match');

        // confirmation email matches but email is not valid

        $('#mgmt_email').val('bb@cc');
        $('#mgmt_emailConfirm').val('bb@cc');

        $('#mgmt_submitButton').click();

        verifyValidationErrorsNotVisibleExceptFor(['#mgmt_emailValidation']);
        verifyValidationErrorMessage('#mgmt_emailValidation', "not a valid email address");

        // valid, matching emails

        $('#mgmt_email').val('bb@cc.com');
        $('#mgmt_emailConfirm').val('bb@cc.com');

        $('#mgmt_submitButton').click();

        verifyValidationErrorsNotVisibleExceptFor([]);

        // password and confirmation mismatch

        $('#mgmt_password').val('8');
        $('#mgmt_passwordConfirm').val('88');

        $('#mgmt_submitButton').click();

        verifyValidationErrorsNotVisibleExceptFor(['#mgmt_passwordValidation']);
        verifyValidationErrorMessage('#mgmt_passwordValidation', "passwords must match");

        // password and confirmation match, but not valid because it does not contain all the required types of characters

        $('#mgmt_password').val('88');
        $('#mgmt_passwordConfirm').val('88');

        $('#mgmt_submitButton').click();

        verifyValidationErrorsNotVisibleExceptFor(['#mgmt_passwordValidation']);
        verifyValidationErrorMessage('#mgmt_passwordValidation', "must contain a lowercase, uppercase, digit, and special character");

        // invalid password, this time because is is less than 8 characters

        $('#mgmt_password').val('Eight8*');
        $('#mgmt_passwordConfirm').val('Eight8*');

        $('#mgmt_submitButton').click();

        verifyValidationErrorsNotVisibleExceptFor(['#mgmt_passwordValidation']);
        verifyValidationErrorMessage('#mgmt_passwordValidation', "must be at least 8 characters long");

        // password and confirmation match and are valid

        $('#mgmt_password').val('Eight8*.');
        $('#mgmt_passwordConfirm').val('Eight8*.');

        $('#mgmt_submitButton').click();

        verifyValidationErrorsNotVisibleExceptFor([]);

        // verify primary phone number validation

        verifyPhoneNumberValidation("#mgmt_primaryPhone", '#mgmt_primaryPhoneValidation', 'not a valid phone number format');

        // verify secondary phone number validation

        verifyPhoneNumberValidation("#mgmt_secondaryPhone", '#mgmt_secondaryPhoneValidation', 'secondary phone not a valid phone number format');

        // verify fax number validation

        verifyPhoneNumberValidation("#mgmt_fax", '#mgmt_faxValidation', 'fax not a valid fax number format');

        // verify pager number valid - case 1

        $('#mgmt_pager').val('1');

        $('#mgmt_submitButton').click();

        verifyValidationErrorsNotVisibleExceptFor([]);

        // pager number valid - case 2

        $('#mgmt_pager').val('1111111111');

        $('#mgmt_submitButton').click();

        verifyValidationErrorsNotVisibleExceptFor([]);

        // pager number valid - case 4

        $('#mgmt_pager').val('');

        $('#mgmt_submitButton').click();

        verifyValidationErrorsNotVisibleExceptFor([]);

        // pager number not valid - case 1

        $('#mgmt_pager').val('a');

        $('#mgmt_submitButton').click();

        verifyValidationErrorsNotVisibleExceptFor(["#mgmt_pagerValidation"]);
        verifyValidationErrorMessage('#mgmt_pagerValidation', "pager not a valid pager number format (1 to 10 digits, numbers only)");

        // pager number not valid - case 2

        $('#mgmt_pager').val('11111111111');

        $('#mgmt_submitButton').click();

        verifyValidationErrorsNotVisibleExceptFor(["#mgmt_pagerValidation"]);
        verifyValidationErrorMessage('#mgmt_pagerValidation', "pager not a valid pager number format (1 to 10 digits, numbers only)");

        // pager number not valid - case 2

        $('#mgmt_pager').val('1111111a');

        $('#mgmt_submitButton').click();

        verifyValidationErrorsNotVisibleExceptFor(["#mgmt_pagerValidation"]);
        verifyValidationErrorMessage('#mgmt_pagerValidation', "pager not a valid pager number format (1 to 10 digits, numbers only)");

    });

    function verifyPhoneNumberValidation(phoneNumberFieldSelector, validationMessageSelector, expectedErrorMessage) {

        // phone number not valid, case 1

        $(phoneNumberFieldSelector).val('1'); // note: the UI masks the input and would not normally allow a single-digit input

        $('#mgmt_submitButton').click();

        verifyValidationErrorsNotVisibleExceptFor([validationMessageSelector]);
        verifyValidationErrorMessage(validationMessageSelector, expectedErrorMessage);

        // phone number not valid, case 2

        $(phoneNumberFieldSelector).val('(111)111- 1111'); // this is what the masked input will generate

        $('#mgmt_submitButton').click();

        verifyValidationErrorsNotVisibleExceptFor([validationMessageSelector]);
        verifyValidationErrorMessage(validationMessageSelector, expectedErrorMessage);

        // phone number valid, case 1

        $(phoneNumberFieldSelector).val('1111111111'); // note: the UI masks the input and would not normally allow digits-only

        $('#mgmt_submitButton').click();

        verifyValidationErrorsNotVisibleExceptFor([]);

        // phone number valid, case 2

        $(phoneNumberFieldSelector).val('(111) 111-1111'); // this is what the masked input will generate

        $('#mgmt_submitButton').click();

    }

    function verifyValidationErrorsNotVisibleExceptFor(exceptions) {

        var fieldsToVerify = [
            '#mgmt_firstNameValidation',
            '#mgmt_lastNameValidation',
            '#mgmt_ecommonsIdValidation',
            '#mgmt_institutionRoleValidation',
            '#mgmt_institutionValidation',
            '#mgmt_roleValidation',
            '#mgmt_emailValidation',
            '#mgmt_emailConfirmValidation',
            '#mgmt_primaryPhoneValidation',
            '#mgmt_secondaryPhoneValidation',
            '#mgmt_faxValidation',
            '#mgmt_pagerValidation',
            '#mgmt_passwordValidation',
            '#mgmt_passwordConfirmValidation'
        ];

        if (exceptions) {
            fieldsToVerify = DomVerificationHelper.filterArray(fieldsToVerify, exceptions, verifyValidationFailed);
        }

        fieldsToVerify.forEach(function(selector) {
            since(selector + ' should be hidden or empty - it\'s content is "' + $(selector).text() + '"')
                .expect($(selector).css('visibility') == 'hidden' || $(selector).text() == '')
                .toBe(true);
        });

    }

    function verifyValidationErrorMessagesVisible() {

        verifyValidationErrorMessage('#mgmt_firstNameValidation', "Required Field");
        verifyValidationErrorMessage('#mgmt_lastNameValidation', "Required Field");
        verifyValidationErrorMessage('#mgmt_ecommonsIdValidation', "Required Field");
        verifyValidationErrorMessage('#mgmt_institutionRoleValidation', "Required Field");
        verifyValidationErrorMessage('#mgmt_institutionValidation', "Required Field");
        verifyValidationErrorMessage('#mgmt_roleValidation', "Required Field");
        verifyValidationErrorMessage('#mgmt_emailValidation', "not a valid email address");
        verifyValidationErrorMessage('#mgmt_emailConfirmValidation', "Required Field");
        verifyValidationErrorMessage('#mgmt_primaryPhoneValidation', "not a valid phone number format");

    }

    function verifyValidationErrorMessage(selector, message) {

        since(selector + ' should be visible').expect($(selector).css('visibility')).toBe('visible');
        since(selector + ' should contain the message "' + message + '", not "' + $(selector).text() + '"').expect($(selector).text()).toBe(message);

    }

    function verifyValidationFailed(selector) {

        since(selector + ' should be visible').expect($(selector).css('visibility')).toBe('visible');
        since(selector + ' should have a non-0-length message').expect($(selector).text().length).toBeGreaterThan(0);

    }

});
