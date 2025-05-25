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

var breadCrumbFunctions = (function () {

    var homeModuleLink = "<a href='javascript:homeModule()'>Home</a>";
    var apptListModuleLink = "<a href='javascript:appointmentListModule()'>Appointment List View</a>";
    var mgtModuleLink = "<a href='javascript:mgmtModule()'>Management</a>";
    var mgtModuleUserDetailLink = "<a href='javascript:mgmtUserDetails()'>User Details</a>";
    var subjectModuleLink = "<a href='javascript:subjectModule()'>Subjects</a>";
    var subjectModuleLink2 = "<a href='javascript:subjectModule()'>Subject Search Form</a>";
    var subjectFoundLink = "<a href='subject_search.html'>Subject Found</a>";
    var subjectReadDatalink = "<a href='javascript:subjectReadData()'>Subject Details</a>";
    var resourceModuleLink = "<a href='javascript:resourceModule()'>Resources</a>";
    var resourceDetailLink = "<a href='resource_form.html'>Resource Detail</a>";
    var subLocationClosureLink = "<a href='sublocation_closure_screen.html'>Sub-Location Closure List</a>";
    var studyModuleLink = "<a href='javascript:studyModule()'>Studies List</a>";
    var studyModuleLink2 = "<a href='javascript:studyModule()'>Studies</a>";
    var studyViewDetailsLink = "<a href='javascript:studyReadData()'>Study Details</a>";
    var subjectSearchLink = "<a href='subject_search.html'>Add Study Subject</a>";
    var studyShowSubjectsLink = "<a href='javascript:studyShowSubjectsTab();'> Study Subjects</a>";
    var visitDetailsLink = "<a href='visit_detail.html'>Visit Details</a>";
    var reportModuleLink = "<a href='javascript:reportModule()'>Reports List</a>";

    var breadcrumbMap = {

        'home_screen': "",

        'report_screen': homeModuleLink + " <span class='breadcrumbSep'> &gt; </span> Standard Reports",

        'report_builder_screen': homeModuleLink + " <span class='breadcrumbSep'> &gt; </span> Report Builder",

        'mgmt_screen': homeModuleLink + " <span class='breadcrumbSep'> &gt; </span> Management",

        'mgmt_user_view_form': homeModuleLink + " <span class='breadcrumbSep'> &gt; </span> " +
        mgtModuleLink + " <span class='breadcrumbSep'> &gt; </span> User Details",

        'mgmt_user_edit_form': homeModuleLink + " <span class='breadcrumbSep'> &gt; </span> " +
        mgtModuleLink + " <span class='breadcrumbSep'> &gt; </span> " +
        mgtModuleUserDetailLink + " <span class='breadcrumbSep'> &gt; </span> Edit User",

        'mgmt_user_new_form': homeModuleLink + " <span class='breadcrumbSep'> &gt; </span> " +
        mgtModuleLink + " <span class='breadcrumbSep'> &gt; </span> Create New User",

        'subject_screen': homeModuleLink + " <span class='breadcrumbSep'> &gt; </span> Subjects",

        'subject_create_form': homeModuleLink + " <span class='breadcrumbSep'> &gt; </span> " +
        subjectModuleLink + " <span class='breadcrumbSep'> &gt; </span> Create New Subject",

        'subject_edit_form': homeModuleLink + " <span class='breadcrumbSep'> &gt; </span> " +
        subjectModuleLink + " <span class='breadcrumbSep'> &gt; </span> Edit Subject",

        'subject_view_form': homeModuleLink + " <span class='breadcrumbSep'> &gt; </span> " +
        subjectModuleLink + " <span class='breadcrumbSep'> &gt; </span> Subject Details",

        'studyStaff_subject_view_form': homeModuleLink + " <span class='breadcrumbSep'> &gt; </span> " +
        subjectModuleLink2 + " <span class='breadcrumbSep'> &gt; </span> " +
        subjectFoundLink + " <span class='breadcrumbSep'> &gt; </span> Subject Details",

        'subject_studyStaff_edit_form': homeModuleLink + " <span class='breadcrumbSep'> &gt; </span> " +
        subjectModuleLink2 + " <span class='breadcrumbSep'> &gt; </span> " +
        subjectFoundLink + " <span class='breadcrumbSep'> &gt; </span>" +
        subjectReadDatalink + " <span class='breadcrumbSep'> &gt; </span> Edit Subject",

        'subject_search_form': homeModuleLink + " <span class='breadcrumbSep'> &gt; </span> " +
        subjectModuleLink2 + " <span class='breadcrumbSep'> &gt; </span> Subject Found",

        'resource_screen': homeModuleLink + " <span class='breadcrumbSep'> &gt; </span> Resources",

        'resource_new_resource_form': homeModuleLink + " <span class='breadcrumbSep'> &gt; </span> " +
        resourceModuleLink + " <span class='breadcrumbSep'> &gt; </span> Create New Resource",

        'resource_edit_resource_form': homeModuleLink + " <span class='breadcrumbSep'> &gt; </span> " +
        resourceModuleLink + " <span class='breadcrumbSep'> &gt; </span> Edit Resource",

        'resource_close_sub_list': homeModuleLink + " <span class='breadcrumbSep'> &gt; </span> " +
        resourceModuleLink + " <span class='breadcrumbSep'> &gt; </span> Sub-Location Closure List",

        'resource_close_sub_form': homeModuleLink + " <span class='breadcrumbSep'> &gt; </span> " +
        resourceModuleLink + " <span class='breadcrumbSep'> &gt; </span> " +
        subLocationClosureLink + " <span class='breadcrumbSep'> &gt; </span> New Sub-Location Closure",

        'resource_room_gantt_screen': homeModuleLink + " <span class='breadcrumbSep'> &gt; </span> " +
        resourceModuleLink + " <span class='breadcrumbSep'> &gt; </span> Room Resource Usage Gantt",

        'resource_nursing_gantt_screen': homeModuleLink + " <span class='breadcrumbSep'> &gt; </span> " +
        resourceModuleLink + " <span class='breadcrumbSep'> &gt; </span> Nursing Resource Usage Gantt",

        'resource_nutrition_gantt_screen': homeModuleLink + " <span class='breadcrumbSep'> &gt; </span> " +
        resourceModuleLink + " <span class='breadcrumbSep'> &gt; </span> Nutrition Resource Usage Gantt",

        'study_screen': homeModuleLink + " <span class='breadcrumbSep'> &gt; </span> Studies List",

        'study_form_screen': homeModuleLink + " <span class='breadcrumbSep'> &gt; </span> " +
        studyModuleLink + " <span class='breadcrumbSep'> &gt; </span> Study Details",

        'study_new_form': homeModuleLink + " <span class='breadcrumbSep'> &gt; </span> " +
        studyModuleLink + " <span class='breadcrumbSep'> &gt; </span> Create New Study",

        'study_edit_form': homeModuleLink + " <span class='breadcrumbSep'> &gt; </span> " +
        studyModuleLink + " <span class='breadcrumbSep'> &gt; </span> " +
        studyViewDetailsLink + " <span class='breadcrumbSep'> &gt; </span> Edit Study Details",

        'study_visit_main_screen': homeModuleLink + " <span class='breadcrumbSep'> &gt; </span> " +
        studyModuleLink2 + " <span class='breadcrumbSep'> &gt; </span> Visit",

        'study_visit_view_form': homeModuleLink + " <span class='breadcrumbSep'> &gt; </span> " +
        studyModuleLink + " <span class='breadcrumbSep'> &gt; </span> " +
        studyViewDetailsLink + " <span class='breadcrumbSep'> &gt; </span> Visit Details",

        'study_subject_form_screen': homeModuleLink + " <span class='breadcrumbSep'> &gt; </span> " +
        studyModuleLink + " <span class='breadcrumbSep'> &gt; </span> " +
        studyViewDetailsLink + " <span class='breadcrumbSep'> &gt; </span> Add Study Subject",

        'study_staff_subject_form_screen': homeModuleLink + " <span class='breadcrumbSep'> &gt; </span> " +
        studyModuleLink + " <span class='breadcrumbSep'> &gt; </span> " +
        studyViewDetailsLink + " <span class='breadcrumbSep'> &gt; </span> " +
        subjectSearchLink + " <span class='breadcrumbSep'> &gt; </span> Subject Found",

        'study_member_form_screen': homeModuleLink + " <span class='breadcrumbSep'> &gt; </span> " +
        studyModuleLink + " <span class='breadcrumbSep'> &gt; </span> " +
        studyViewDetailsLink + " <span class='breadcrumbSep'> &gt; </span> Add Study Member",

        'study_details_subjects_detail': homeModuleLink + " <span class='breadcrumbSep'> &gt; </span> " +
        studyModuleLink + " <span class='breadcrumbSep'> &gt; </span> " +
        studyViewDetailsLink + " <span class='breadcrumbSep'> &gt; </span> " +
        studyShowSubjectsLink + " <span class='breadcrumbSep'> &gt; </span> Subject Details",

        'study_details_subjects_detail_edit': homeModuleLink + " <span class='breadcrumbSep'> &gt; </span> " +
        studyModuleLink + " <span class='breadcrumbSep'> &gt; </span> " +
        studyViewDetailsLink + " <span class='breadcrumbSep'> &gt; </span> " +
        studyShowSubjectsLink + " <span class='breadcrumbSep'> &gt; </span> Edit Subject",

        'study_visit_edit_form': homeModuleLink + " <span class='breadcrumbSep'> &gt; </span> " +
        studyModuleLink + " <span class='breadcrumbSep'> &gt; </span> " +
        studyViewDetailsLink + " <span class='breadcrumbSep'> &gt; </span>" +
        visitDetailsLink + " <span class='breadcrumbSep'> &gt; </span> Edit Visit Template",

        'study_visit_copy_form': homeModuleLink + " <span class='breadcrumbSep'> &gt; </span> " +
        studyModuleLink + " <span class='breadcrumbSep'> &gt; </span> " +
        studyViewDetailsLink + " <span class='breadcrumbSep'> &gt; </span> Copy Visit Template",

        'study_visit_approval_form_screen': homeModuleLink + " <span class='breadcrumbSep'> &gt; </span> " +
        studyModuleLink + " <span class='breadcrumbSep'> &gt; </span> " +
        studyViewDetailsLink + " <span class='breadcrumbSep'> &gt; </span> " +
        visitDetailsLink + " <span class='breadcrumbSep'> &gt; </span> Approve Visit Template",

        'study_visit_new_form': homeModuleLink + " <span class='breadcrumbSep'> &gt; </span> " +
        studyModuleLink + " <span class='breadcrumbSep'> &gt; </span> " +
        studyViewDetailsLink + " <span class='breadcrumbSep'> &gt; </span> New Visit Template",

        'appt_detail': homeModuleLink + " <span class='breadcrumbSep'> &gt; </span> " +
        apptListModuleLink +" <span class='breadcrumbSep'> &gt; </span> Appointment Batch Entry",

        'appt_list': homeModuleLink + " <span class='breadcrumbSep'> &gt; </span> Appointment List View"
    };

    return {
        'renderBreadcrumbs': function (screen) {
            $(".breadcrumbs").html(breadcrumbMap[screen]);
        },
        'renderReportBreadcrumbs': function (reportTitle) {
            $(".breadcrumbs").html(
                homeModuleLink + " <span class='breadcrumbSep'> &gt; </span> " +
                reportModuleLink + " <span class='breadcrumbSep'> &gt; </span> " + reportTitle
            );
        }
    }
})();


var renderBreadcrumbs = breadCrumbFunctions.renderBreadcrumbs;
var renderReportBreadcrumbs = breadCrumbFunctions.renderReportBreadcrumbs;



