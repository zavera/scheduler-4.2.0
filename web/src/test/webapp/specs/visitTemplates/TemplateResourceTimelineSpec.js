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
describe('Delete-resource-from-template popup', function() {

    it('does not cause warning message to show outside of itself', function() {

        // IMPORTANT: A note on debugging this test.
        // If you need to debug this test, comment-in all the console.log()'s, and also
        // comment out parts of the 'testData' array in order to focus on one or few tests at a time.
        // You can also make use of IntelliJ's debugging capability for Karma tests. But the steps
        // described above will make it a lot faster to debug.

        // helper functions and data

        function verify_dialogs(flavor, html) {

            // Keep these console.log's to help debugging this test.
            // Otherwise, because it is data-driven, it is hard to see where it broke without going into debug mode
            // console.log('verifying flavor ' + flavor);
            // console.log("who's open: ");
            // console.log("    1: " + $("#delete_template_resource_dialog_confirm").css('display'));
            // console.log("    2: " + $("#delete_template_resource_approval_dialog_confirm").css('display'));
            // console.log("    3: " + $("#unlink-delete-individual-template-resource-dialog-confirm").css('display'));
            // console.log("HTML: ");
            // console.log("    1: " + $("#delete_template_resource_dialog_confirm").html());
            // console.log("    2: " + $("#delete_template_resource_approval_dialog_confirm").html());
            // console.log("    3: " + $("#individualUnlinkDeleteMessage").html());
            // console.log('wanted HTML:');
            // console.log('       ' + html);

            if (flavor == 1) {

                expect($("#delete_template_resource_dialog_confirm").css('display')).toBe('block');
                expect($('#delete_template_resource_dialog_confirm').html()).toBe(html);
                // console.log('actual HTML: ');
                // console.log('       ' + $('#delete_template_resource_dialog_confirm').html());
                // console.log($('#delete_template_resource_dialog_confirm').html() == html ? 'same' : '*** DIFFERENT');

                expect($("#delete_template_resource_approval_dialog_confirm").css('display')).toBe('none');
                expect($("#delete_template_resource_approval_dialog_confirm").html()).toBe('');

                expect($("#unlink-delete-individual-template-resource-dialog-confirm").css('display')).toBe('none');
                expect($("#individualUnlinkDeleteMessage").html()).toBe('');

            }
            else if (flavor == 2) {

                expect($("#delete_template_resource_dialog_confirm").css('display')).toBe('none');
                expect($("#delete_template_resource_dialog_confirm").html()).toBe('');

                expect($("#delete_template_resource_approval_dialog_confirm").css('display')).toBe('inline-block');
                expect($("#delete_template_resource_approval_dialog_confirm").html()).toBe(html);
                // console.log('actual HTML: ');
                // console.log('       ' + $('#delete_template_resource_approval_dialog_confirm').html());
                // console.log($('#delete_template_resource_approval_dialog_confirm').html() == html ? 'same' : '*** DIFFERENT');

                expect($("#unlink-delete-individual-template-resource-dialog-confirm").css('display')).toBe('none');
                expect($("#individualUnlinkDeleteMessage").html()).toBe('');

            }
            else if (flavor == 3) {

                expect($("#delete_template_resource_dialog_confirm").css('display')).toBe('none');
                expect($("#delete_template_resource_dialog_confirm").html()).toBe('');

                expect($("#delete_template_resource_approval_dialog_confirm").css('display')).toBe('none');
                expect($("#delete_template_resource_approval_dialog_confirm").html()).toBe('');

                expect($("#unlink-delete-individual-template-resource-dialog-confirm").css('display')).toBe('block');
                expect($("#individualUnlinkDeleteMessage").html()).toBe(html);

                // console.log('actual HTML: ');
                // console.log('       ' + $('#individualUnlinkDeleteMessage').html());
                // console.log($('#individualUnlinkDeleteMessage').html() == html ? 'same' : '*** DIFFERENT');

            }
            else {

                // console.log("should never reach this line");
                expect(false).toBe(true);

            }
        }

        function reset_dialogs(flavor) {
            if (flavor == 1) {
                $("#delete_template_resource_dialog_confirm").dialog('close');
            }
            else if (flavor == 2) {
                $("#delete_template_resource_approval_dialog_confirm").dialog('close');
            }
            else if (flavor == 3) {
                $("#unlink-delete-individual-template-resource-dialog-confirm").dialog('close');
            }
            else {
                // no dialog open yet
            }
            $("#delete_template_resource_dialog_confirm").css('display', 'none');
            $("#delete_template_resource_dialog_confirm").html("");
            $("#delete_template_resource_approval_dialog_confirm").css('display', 'none');
            $("#delete_template_resource_approval_dialog_confirm").html("");
            $("#unlink-delete-individual-template-resource-dialog-confirm").css('display', 'none');
            $("#individualUnlinkDeleteMessage").html("");
        }

        // There are 16 logical combinations, and up to as many use cases. The test code is data-driven by
        // the following array, which contains object representations of each case.
        var testData = [
            { "approved": false, "isFlexGrouped": false, "isFloatGrouped": false, "linkableOnDelete": false, "dialogFlavor" : 1,
                "html": "<p>Are you sure you want to delete this resource?<br></p>"},
            { "approved": false, "isFlexGrouped": false, "isFloatGrouped": false, "linkableOnDelete": true, "dialogFlavor" : 1,
                "html": "<p>Are you sure you want to delete this resource?<br></p>"},
            { "approved": false, "isFlexGrouped": false, "isFloatGrouped": true, "linkableOnDelete": false, "dialogFlavor" : 1,
                "html": "<p>Are you sure you want to delete this resource?<br></p><p>Note: The remaining resource(s) in this group will become unlinked.</p>"},
            { "approved": false, "isFlexGrouped": false, "isFloatGrouped": true, "linkableOnDelete": true, "dialogFlavor" : 1,
                "html": "<p>Are you sure you want to delete this resource?<br></p><p>Note: The remaining resource(s) in this group will become unlinked.</p>"},
            { "approved": false, "isFlexGrouped": true, "isFloatGrouped": false, "linkableOnDelete": false, "dialogFlavor" : 3,
                "html": "Are you sure you want to delete this resource? <br><br> Note: The remaining resource(s) in this group will become unlinked."},
            { "approved": false, "isFlexGrouped": true, "isFloatGrouped": false, "linkableOnDelete": true, "dialogFlavor" : 1,
                "html": "<p>Are you sure you want to delete this resource?<br></p>"},
            { "approved": false, "isFlexGrouped": true, "isFloatGrouped": true, "linkableOnDelete": false, "dialogFlavor" : 3,
                "html": "Are you sure you want to delete this resource? <br><br> Note: The remaining resource(s) in this group will become unlinked."},
            { "approved": false, "isFlexGrouped": true, "isFloatGrouped": true, "linkableOnDelete": true, "dialogFlavor" : 1,
                "html": "<p>Are you sure you want to delete this resource?<br></p>"},
            { "approved": true, "isFlexGrouped": false, "isFloatGrouped": false, "linkableOnDelete": false, "dialogFlavor" : 2,
                "html": "<p>Editing this template will change the approval status.<br></p> <p>Are you sure you want to delete this resource?<br></p>"},
            { "approved": true, "isFlexGrouped": false, "isFloatGrouped": false, "linkableOnDelete": true, "dialogFlavor" : 2,
                "html": "<p>Editing this template will change the approval status.<br></p> <p>Are you sure you want to delete this resource?<br></p>"},
            { "approved": true, "isFlexGrouped": false, "isFloatGrouped": true, "linkableOnDelete": false, "dialogFlavor" : 2,
                "html": "<p>Editing this template will change the approval status.<br></p> <p>Are you sure you want to delete this resource?<br></p><p>Note: The remaining resource(s) in this group will become unlinked.</p>"},
            { "approved": true, "isFlexGrouped": false, "isFloatGrouped": true, "linkableOnDelete": true, "dialogFlavor" : 2,
                "html": "<p>Editing this template will change the approval status.<br></p> <p>Are you sure you want to delete this resource?<br></p><p>Note: The remaining resource(s) in this group will become unlinked.</p>"},
            { "approved": true, "isFlexGrouped": true, "isFloatGrouped": false, "linkableOnDelete": false, "dialogFlavor" : 3,
                "html": "<p>Editing this template will change the approval status.</p>Are you sure you want to delete this resource? <br><br> Note: The remaining resource(s) in this group will become unlinked."},
            { "approved": true, "isFlexGrouped": true, "isFloatGrouped": false, "linkableOnDelete": true, "dialogFlavor" : 2,
                "html": "<p>Editing this template will change the approval status.<br></p>  <p>Are you sure you want to delete this resource?</p>"},
            { "approved": true, "isFlexGrouped": true, "isFloatGrouped": true, "linkableOnDelete": false, "dialogFlavor" : 3,
                "html": "<p>Editing this template will change the approval status.</p>Are you sure you want to delete this resource? <br><br> Note: The remaining resource(s) in this group will become unlinked."},
            { "approved": true, "isFlexGrouped": true, "isFloatGrouped": true, "linkableOnDelete": true, "dialogFlavor" : 2,
                "html": "<p>Editing this template will change the approval status.<br></p>  <p>Are you sure you want to delete this resource?</p>"}
        ];

        // set up the test environment
        FixtureHelper.loadSourceHtmlFixtureIntoDom("visit_detail.html");
        templateResourceId = 1;
        var server = UtilHelper.getFakeServer();
        reset_dialogs();

        // execute the tests
        testData.forEach(function(item) {
            approvedTemplate = item.approved ? 'Approved' : null;
            AjaxHelper.setupCheckIfResourcesLinkableOnDeleteUnlinkResponse(server, templateResourceId, item.linkableOnDelete);
            deleteTemplateResourceConfirmDialog(null, item.isFlexGrouped, item.isFloatGrouped);
            server.respond();
            verify_dialogs(item.dialogFlavor, item.html);
            reset_dialogs(item.dialogFlavor);
        })

    });

});

