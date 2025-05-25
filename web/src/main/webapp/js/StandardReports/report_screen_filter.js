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
function filterReports() {
    $("#report_sortDirectionSelect").val(1);
    switch ($("#report_filterSelect option:selected").val()) {

        // staff audit
        case 'staffaudit_selectone':
            $('#report_filterText').css({display: "none"});
            $('#report_filterButton').attr("value", "Reset");
        case 'staff_user_name':
            search_string = $.trim($("#report_filterText").val());
            $('#report_filterButton').css({display: "inline"});
            $('#report_filterButton').attr("value", "Filter");
        case 'staff_user_id':
            search_string = $.trim($("#report_filterText").val());
            $('#report_filterButton').css({display: "inline"});
            $('#report_filterButton').attr("value", "Filter");

            if ($('#report_filterSelect').val() == 'staffaudit_selectone') {
                selectedid = 0;
                $('#report_filterButton').attr("value", "Reset");
            }
            else if ($('#report_filterSelect').val() == 'staff_user_name') {
                selectedid = 1;
                $('#report_filterButton').attr("value", "Filter");
            }
            else if ($('#report_filterSelect').val() == 'staff_user_id') {
                selectedid = 2;
                $('#report_filterButton').attr("value", "Filter");
            }

            if (report_selectedReport.name == 'staff_audit_subject_views') {
                recent_reportUrl = "rest/report/getReport?name=staff_audit_subject_views" +
                    '&filterString=' + search_string + '&filterId=' + selectedid;

                report_getStaffAuditSubjectViewsReport(false, 'staffaudit_selectone');
            }

        // Subject audit
        case 'subjectaudit_selectone':
            selectedid = 0;
            $('#report_filterText').css({display: "none"});
            $('#report_filterButton').attr("value", "Reset");
        case 'subject_user_name':
            selectedid = 1;
            search_string = $.trim($("#report_filterText").val());
            $('#report_filterButton').css({display: "inline"});
            $('#report_filterButton').attr("value", "Filter");

            if (report_selectedReport.name == 'subject_audit_staff_views') {
                recent_reportUrl = "rest/report/getReport?name=subject_audit_staff_views" +
                    '&filterString=' + search_string + '&filterId=' + selectedid;

                report_getSubjectAuditStaffViewsReport(false, 'subjectaudit_selectone');
            }

        // daily resource

        case 'dailyresource_selectone':
            $('#report_filterText').css({display: "none"});
            $('#report_filterButton').attr("value", "Reset");
        case 'resource_name':
            search_string = $.trim($("#report_filterText").val());
            $('#report_filterButton').css({display: "inline"});
            $('#report_filterButton').attr("value", "Filter");
        case 'subject_name':
            search_string = $.trim($("#report_filterText").val());
            $('#report_filterButton').css({display: "inline"});
            $('#report_filterButton').attr("value", "Filter");
        case 'resource_group_nursing':
            $('#report_filterButton').css({display: "inline"});
            $('#report_filterButton').attr("value", "Filter");
        case 'resource_group_nutrition':
            $('#report_filterButton').css({display: "inline"});
            $('#report_filterButton').attr("value", "Filter");
        case 'resource_group_room':
            $('#report_filterButton').css({display: "inline"});
            $('#report_filterButton').attr("value", "Filter");
        case 'resource_group_lab':
            $('#report_filterButton').css({display: "inline"});
            $('#report_filterButton').attr("value", "Filter");
        case 'resource_group_other':
            $('#report_filterButton').css({display: "inline"});
            $('#report_filterButton').attr("value", "Filter");
        case 'daily_resource_crc_approved':
            $('#report_filterButton').css({display: "inline"});
            $('#report_filterButton').attr("value", "Filter");

            if ($('#report_filterSelect').val() == 'dailyresource_selectone') {
                selectedid = 0;
                $('#report_filterButton').attr("value", "Reset");
            }
            else if ($('#report_filterSelect').val() == 'resource_name') {
                selectedid = 1;
                $('#report_filterButton').attr("value", "Filter");
            }
            else if ($('#report_filterSelect').val() == 'subject_name') {
                selectedid = 2;
                $('#report_filterButton').attr("value", "Filter");
            }
            else if ($('#report_filterSelect').val() == 'resource_group_nursing') {
                selectedid = 3;
                $('#report_filterButton').attr("value", "Filter");
            }
            else if ($('#report_filterSelect').val() == 'resource_group_nutrition') {
                selectedid = 4;
                $('#report_filterButton').attr("value", "Filter");
            }
            else if ($('#report_filterSelect').val() == 'resource_group_room') {
                selectedid = 5;
                $('#report_filterButton').attr("value", "Filter");
            }
            else if ($('#report_filterSelect').val() == 'resource_group_lab') {
                selectedid = 6;
                $('#report_filterButton').attr("value", "Filter");
            }
            else if ($('#report_filterSelect').val() == 'resource_group_other') {
                selectedid = 7;
                $('#report_filterButton').attr("value", "Filter");
            }
            else if ($('#report_filterSelect').val() == 'daily_resource_crc_approved') {
                selectedid = 8;
                $('#report_filterButton').attr("value", "Filter");
            }

            if (report_selectedReport.name == 'daily_resource') {
                recent_reportUrl = "rest/report/getReport?name=daily_resource" +
                    '&filterString=' + search_string + '&filterId=' + selectedid;

                report_getDailyResourceReport(false, 'dailyresource_selectone');
            }

        //Daily Admission
        case 'daily_adm_selectone':
            $('#report_filterText').css({display: "none"});
            $('#report_filterButton').attr("value", "Reset");
        case 'daily_adm_subject':
            search_string = $.trim($("#report_filterText").val());
            $('#report_filterButton').css({display: "inline"});
            $('#report_filterButton').attr("value", "Filter");
        case 'daily_adm_local_id':
            search_string = $.trim($("#report_filterText").val());
            $('#report_filterButton').css({display: "inline"});
            $('#report_filterButton').attr("value", "Filter");
        case 'daily_adm_mrn':
            search_string = $.trim($("#report_filterText").val());
            $('#report_filterButton').css({display: "inline"});
            $('#report_filterButton').attr("value", "Filter");
        case 'daily_adm_resource':
            search_string = $.trim($("#report_filterText").val());
            $('#report_filterButton').css({display: "inline"});
            $('#report_filterButton').attr("value", "Filter");
        case 'daily_adm_crc_approved':
            $('#report_filterButton').css({display: "inline"});
            $('#report_filterButton').attr("value", "Filter");

            if ($('#report_filterSelect').val() == 'daily_adm_selectone') {
                selectedid = 0;
                $('#report_filterButton').attr("value", "Reset");
            }
            else if ($('#report_filterSelect').val() == 'daily_adm_subject') {
                selectedid = 1;
                $('#report_filterButton').attr("value", "Filter");
            }
            else if ($('#report_filterSelect').val() == 'daily_adm_local_id') {
                selectedid = 2;
                $('#report_filterButton').attr("value", "Filter");
            }
            else if ($('#report_filterSelect').val() == 'daily_adm_mrn') {
                selectedid = 3;
                $('#report_filterButton').attr("value", "Filter");
            }
            else if ($('#report_filterSelect').val() == 'daily_adm_resource') {
                selectedid = 4;
                $('#report_filterButton').attr("value", "Filter");
            }
            else if ($('#report_filterSelect').val() == 'daily_adm_crc_approved') {
                selectedid = 5;
                $('#report_filterButton').attr("value", "Filter");
            }

            if (report_selectedReport.name == 'daily_adm') {
                recent_reportUrl = "rest/report/getReport?name=daily_adm" +
                    '&filterString=' + search_string + '&filterId=' + selectedid;

                report_getDailyAdmReport(false, 'daily_adm_selectone');
            }


        //Metabolic kitchen (regular and flat)

        case 'metakitch_selectone':
            $('#report_filterText').css({display: "none"});
            $('#report_filterButton').attr("value", "Reset");
        case 'metakitch_subject':
            search_string = $.trim($("#report_filterText").val());
            $('#report_filterButton').css({display: "inline"});
            $('#report_filterButton').attr("value", "Filter");
        case 'metakitch_local_id':
            search_string = $.trim($("#report_filterText").val());
            $('#report_filterButton').css({display: "inline"});
            $('#report_filterButton').attr("value", "Filter");
        case 'metakitch_mrn':
            search_string = $.trim($("#report_filterText").val());
            $('#report_filterButton').css({display: "inline"});
            $('#report_filterButton').attr("value", "Filter");
        case 'metakitch_resource':
            search_string = $.trim($("#report_filterText").val());
            $('#report_filterButton').css({display: "inline"});
            $('#report_filterButton').attr("value", "Filter");
        case 'metakitch_crc_approved':
            $('#report_filterButton').css({display: "inline"});
            $('#report_filterButton').attr("value", "Filter");

            if ($('#report_filterSelect').val() == 'metakitch_selectone') {
                selectedid = 0;
                $('#report_filterButton').attr("value", "Reset");
            }
            else if ($('#report_filterSelect').val() == 'metakitch_subject') {
                selectedid = 1;
                $('#report_filterButton').attr("value", "Filter");
            }
            else if ($('#report_filterSelect').val() == 'metakitch_local_id') {
                selectedid = 2;
                $('#report_filterButton').attr("value", "Filter");
            }
            else if ($('#report_filterSelect').val() == 'metakitch_mrn') {
                selectedid = 3;
                $('#report_filterButton').attr("value", "Filter");
            }
            else if ($('#report_filterSelect').val() == 'metakitch_resource') {
                selectedid = 4;
                $('#report_filterButton').attr("value", "Filter");
            }
            else if ($('#report_filterSelect').val() == 'metakitch_crc_approved') {
                selectedid = 5;
                $('#report_filterButton').attr("value", "Filter");
            }

            if (report_selectedReport.name == 'meta_kitchen') {
                recent_reportUrl = "rest/report/getReport?name=meta_kitchen" +
                    '&filterString=' + search_string + '&filterId=' + selectedid;

                report_getMetaKitchenReport(false, 'metakitch_selectone');
            }

        //Metabolic kitchen By Time

        case 'metakitchbytime_selectone':
            $('#report_filterText').css({display: "none"});
            $('#report_filterButton').attr("value", "Reset");
        case 'metakitchbytime_subject':
            search_string = $.trim($("#report_filterText").val());
            $('#report_filterButton').css({display: "inline"});
            $('#report_filterButton').attr("value", "Filter");
        case 'metakitchbytime_local_id':
            search_string = $.trim($("#report_filterText").val());
            $('#report_filterButton').css({display: "inline"});
            $('#report_filterButton').attr("value", "Filter");
        case 'metakitchbytime_mrn':
            search_string = $.trim($("#report_filterText").val());
            $('#report_filterButton').css({display: "inline"});
            $('#report_filterButton').attr("value", "Filter");
        case 'metakitchbytime_resource':
            search_string = $.trim($("#report_filterText").val());
            $('#report_filterButton').css({display: "inline"});
            $('#report_filterButton').attr("value", "Filter");
        case 'metakitchbytime_crc_approved':
            $('#report_filterButton').css({display: "inline"});
            $('#report_filterButton').attr("value", "Filter");

            if ($('#report_filterSelect').val() == 'metakitchbytime_selectone') {
                selectedid = 0;
                $('#report_filterButton').attr("value", "Reset");
            }
            else if ($('#report_filterSelect').val() == 'metakitchbytime_subject') {
                selectedid = 1;
                $('#report_filterButton').attr("value", "Filter");
            }
            else if ($('#report_filterSelect').val() == 'metakitchbytime_local_id') {
                selectedid = 2;
                $('#report_filterButton').attr("value", "Filter");
            }
            else if ($('#report_filterSelect').val() == 'metakitchbytime_mrn') {
                selectedid = 3;
                $('#report_filterButton').attr("value", "Filter");
            }
            else if ($('#report_filterSelect').val() == 'metakitchbytime_resource') {
                selectedid = 4;
                $('#report_filterButton').attr("value", "Filter");
            }
            else if ($('#report_filterSelect').val() == 'metakitchbytime_crc_approved') {
                selectedid = 5;
                $('#report_filterButton').attr("value", "Filter");
            }

            if (report_selectedReport.name == 'meta_kitchen_by_time') {
                recent_reportUrl = "rest/report/getReport?name=meta_kitchen_by_time" +
                    '&filterString=' + search_string + '&filterId=' + selectedid;

                report_getMetaKitchenByTimeReport(false, 'metakitchbytime_selectone');
            }

        //Weekly Pharmacy

        case 'weeklypharm_selectone':
            $('#report_filterText').css({display: "none"});
            $('#report_filterButton').attr("value", "Reset");
        case 'weeklypharm_local_id':
            search_string = $.trim($("#report_filterText").val());
            $('#report_filterButton').css({display: "inline"});
            $('#report_filterButton').attr("value", "Filter");
        case 'weeklypharm_study':
            search_string = $.trim($("#report_filterText").val());
            $('#report_filterButton').css({display: "inline"});
            $('#report_filterButton').attr("value", "Filter");
        case 'weeklypharm_subject':
            search_string = $.trim($("#report_filterText").val());
            $('#report_filterButton').css({display: "inline"});
            $('#report_filterButton').attr("value", "Filter");
        case 'weeklypharm_sched_start_datetime':
            search_string = $.trim($("#report_filterText").val());
            $('#report_filterButton').css({display: "inline"});
            $('#report_filterButton').attr("value", "Filter");
        case 'weeklypharm_crc_approved':
            $('#report_filterButton').css({display: "inline"});
            $('#report_filterButton').attr("value", "Filter");


            if ($('#report_filterSelect').val() == 'weeklypharm_selectone') {
                selectedid = 0;
                $('#report_filterButton').attr("value", "Reset");
            }
            else if ($('#report_filterSelect').val() == 'weeklypharm_local_id') {
                selectedid = 1;
                $('#report_filterButton').attr("value", "Filter");
            }
            else if ($('#report_filterSelect').val() == 'weeklypharm_study') {
                selectedid = 2;
                $('#report_filterButton').attr("value", "Filter");
            }
            else if ($('#report_filterSelect').val() == 'weeklypharm_subject') {
                selectedid = 3;
                $('#report_filterButton').attr("value", "Filter");
            }
            else if ($('#report_filterSelect').val() == 'weeklypharm_sched_start_datetime') {
                selectedid = 4;
                $('#report_filterButton').attr("value", "Filter");
            }
            else if ($('#report_filterSelect').val() == 'weeklypharm_crc_approved') {
                selectedid = 5;
                $('#report_filterButton').attr("value", "Filter");
            }

            if (report_selectedReport.name == 'weekly_pharm') {
                recent_reportUrl = "rest/report/getReport?name=weekly_pharm" +
                    '&filterString=' + search_string + '&filterId=' + selectedid;

                report_getWeeklyPharmReport(false, 'weeklypharm_selectone');
            }

        //Protocol By Nurse

        case 'protonurse_selectone':
            $('#report_filterText').css({display: "none"});
            $('#report_filterButton').attr("value", "Reset");
        case 'proto_by_nurse':
            search_string = $.trim($("#report_filterText").val());
            $('#report_filterButton').css({display: "inline"});
            $('#report_filterButton').attr("value", "Filter");
        case 'protonurse_local_id':
            search_string = $.trim($("#report_filterText").val());
            $('#report_filterButton').css({display: "inline"});
            $('#report_filterButton').attr("value", "Filter");
        case 'protonurse_study_status':
            search_string = $.trim($("#report_filterText").val());
            $('#report_filterButton').css({display: "inline"});
            $('#report_filterButton').attr("value", "Filter");


            if ($('#report_filterSelect').val() == 'protonurse_selectone') {
                selectedid = 0;
                $('#report_filterButton').attr("value", "Reset");
            }
            else if ($('#report_filterSelect').val() == 'proto_by_nurse') {
                selectedid = 1;
                $('#report_filterButton').attr("value", "Filter");
            }
            else if ($('#report_filterSelect').val() == 'protonurse_local_id') {
                selectedid = 2;
                $('#report_filterButton').attr("value", "Filter");
            }
            else if ($('#report_filterSelect').val() == 'protonurse_study_status') {
                selectedid = 3;
                $('#report_filterButton').attr("value", "Filter");
            }


            if (report_selectedReport.name == 'proto_by_nurse') {
                recent_reportUrl = "rest/report/getReport?name=proto_by_nurse" +
                    '&filterString=' + search_string +
                    '&filterId=' + selectedid;
                report_getProtoNurseReport(false, 'protonurse_selectone');
            }

        //Protocol By Nutritionist

        case 'protonutritionist_selectone':
            $('#report_filterText').css({display: "none"});
            $('#report_filterButton').attr("value", "Reset");
        case 'proto_nutritionist_last':
            search_string = $.trim($("#report_filterText").val());
            $('#report_filterButton').css({display: "inline"});
            $('#report_filterButton').attr("value", "Filter");
        case 'protonutritionist_local_id':
            search_string = $.trim($("#report_filterText").val());
            $('#report_filterButton').css({display: "inline"});
            $('#report_filterButton').attr("value", "Filter");
        case 'protonutritionist_study_status':
            search_string = $.trim($("#report_filterText").val());
            $('#report_filterButton').css({display: "inline"});
            $('#report_filterButton').attr("value", "Filter");


            if ($('#report_filterSelect').val() == 'protonutritionist_selectone') {
                selectedid = 0;
                $('#report_filterButton').attr("value", "Reset");
            }
            else if ($('#report_filterSelect').val() == 'proto_nutritionist_last') {
                selectedid = 1;
                $('#report_filterButton').attr("value", "Filter");
            }
            else if ($('#report_filterSelect').val() == 'protonutritionist_local_id') {
                selectedid = 2;
                $('#report_filterButton').attr("value", "Filter");
            }
            else if ($('#report_filterSelect').val() == 'protonutritionist_study_status') {
                selectedid = 3;
                $('#report_filterButton').attr("value", "Filter");
            }

            if (report_selectedReport.name == 'proto_by_nutritionist') {
                recent_reportUrl = "rest/report/getReport?name=proto_nutritionist" +
                    '&filterString=' + search_string +
                    '&filterId=' + selectedid;
                report_getProtoNutritionistReport(false, 'protonutritionist_selectone');
            }

        // Study Visit Location
        case 'studyvisitlocation_selectone':
            $('#report_filterText').css({display: "none"});
            $('#report_filterButton').attr("value", "Reset");
        case 'studyvisitlocation_local_id':
            search_string = $.trim($("#report_filterText").val());
            $('#report_filterButton').css({display: "inline"});
            $('#report_filterButton').attr("value", "Filter");
        case 'studyvisitlocation_visit':
            search_string = $.trim($("#report_filterText").val());
            $('#report_filterButton').css({display: "inline"});
            $('#report_filterButton').attr("value", "Filter");
        case 'studyvisitlocation_sublocation':
            search_string = $.trim($("#report_filterText").val());
            $('#report_filterButton').css({display: "inline"});
            $('#report_filterButton').attr("value", "Filter");
        case 'studyvisitlocation_crc_approved':
            $('#report_filterButton').css({display: "inline"});
            $('#report_filterButton').attr("value", "Filter");


            if ($('#report_filterSelect').val() == 'studyvisitlocation_selectone') {
                selectedid = 0;
                $('#report_filterButton').attr("value", "Reset");
            }
            else if ($('#report_filterSelect').val() == 'studyvisitlocation_local_id') {
                selectedid = 1;
                $('#report_filterButton').attr("value", "Filter");
            }
            else if ($('#report_filterSelect').val() == 'studyvisitlocation_visit') {
                selectedid = 2;
                $('#report_filterButton').attr("value", "Filter");
            }
            else if ($('#report_filterSelect').val() == 'studyvisitlocation_sublocation') {
                selectedid = 3;
                $('#report_filterButton').attr("value", "Filter");
            }
            else if ($('#report_filterSelect').val() == 'studyvisitlocation_crc_approved') {
                selectedid = 4;
                $('#report_filterButton').attr("value", "Filter");
            }

            if (report_selectedReport.name == 'study_visit_location') {
                recent_reportUrl = "rest/report/getReport?name=study_visit_location" +
                    '&filterString=' + search_string + '&filterId=' + selectedid;
                report_getStudyVisitLocationReport(false, 'studyvisitlocation_selectone');
            }


        //Department and PI

        case 'deptandpi_selectone':
            $('#report_filterText').css({display: "none"});
            $('#report_filterButton').attr("value", "Reset");
        case 'deptandpi_department':
            search_string = $.trim($("#report_filterText").val());
            $('#report_filterButton').css({display: "inline"});
            $('#report_filterButton').attr("value", "Filter");
        case 'deptandpi_division':
            search_string = $.trim($("#report_filterText").val());
            $('#report_filterButton').css({display: "inline"});
            $('#report_filterButton').attr("value", "Filter");
        case 'deptandpi_investigator':
            search_string = $.trim($("#report_filterText").val());
            $('#report_filterButton').css({display: "inline"});
            $('#report_filterButton').attr("value", "Filter");
        case 'deptandpi_institution':
            search_string = $.trim($("#report_filterText").val());
            $('#report_filterButton').css({display: "inline"});
            $('#report_filterButton').attr("value", "Filter");
        case 'deptandpi_credential':
            search_string = $.trim($("#report_filterText").val());
            $('#report_filterButton').css({display: "inline"});
            $('#report_filterButton').attr("value", "Filter");
        case 'deptandpi_faculty_rank':
            search_string = $.trim($("#report_filterText").val());
            $('#report_filterButton').css({display: "inline"});
            $('#report_filterButton').attr("value", "Filter");
        case 'deptandpi_crc_approved':
            $('#report_filterButton').css({display: "inline"});
            $('#report_filterButton').attr("value", "Filter");

            if ($('#report_filterSelect').val() == 'deptandpi_selectone') {
                selectedid = 0;
                $('#report_filterButton').attr("value", "Reset");
            }
            else if ($('#report_filterSelect').val() == 'deptandpi_department') {
                selectedid = 1;
                $('#report_filterButton').attr("value", "Filter");
            }
            else if ($('#report_filterSelect').val() == 'deptandpi_division') {
                selectedid = 2;
                $('#report_filterButton').attr("value", "Filter");
            }
            else if ($('#report_filterSelect').val() == 'deptandpi_investigator') {
                selectedid = 3;
                $('#report_filterButton').attr("value", "Filter");
            }
            else if ($('#report_filterSelect').val() == 'deptandpi_institution') {
                selectedid = 4;
                $('#report_filterButton').attr("value", "Filter");
            }
            else if ($('#report_filterSelect').val() == 'deptandpi_credential') {
                selectedid = 5;
                $('#report_filterButton').attr("value", "Filter");
            }
            else if ($('#report_filterSelect').val() == 'deptandpi_faculty_rank') {
                selectedid = 6;
                $('#report_filterButton').attr("value", "Filter");
            }
            else if ($('#report_filterSelect').val() == 'deptandpi_crc_approved') {
                selectedid = 7;
                $('#report_filterButton').attr("value", "Filter");
            }

            if (report_selectedReport.name == 'dept_and_pi') {
                recent_reportUrl = "rest/report/getReport?name=dept_and_pi" +
                    '&filterString=' + search_string +
                    '&filterId=' + selectedid;

                report_getDeptAndPiReport(false, 'deptandpi_selectone');
            }


        // Billing Report

        case 'billing_selectone':
            $('#report_filterText').css({display: "none"});
            $('#report_filterButton').attr("value", "Reset");
        case 'billing_local_id':
            search_string = $.trim($("#report_filterText").val());
            $('#report_filterButton').css({display: "inline"});
            $('#report_filterButton').attr("value", "Filter");
        case 'billing_study':
            search_string = $.trim($("#report_filterText").val());
            $('#report_filterButton').css({display: "inline"});
            $('#report_filterButton').attr("value", "Filter");
        case 'billing_industry_initiated':
            search_string = $.trim($("#report_filterText").val());
            $('#report_filterButton').css({display: "inline"});
            $('#report_filterButton').attr("value", "Filter");
        case 'billing_investigator':
            search_string = $.trim($("#report_filterText").val());
            $('#report_filterButton').css({display: "inline"});
            $('#report_filterButton').attr("value", "Filter");
        case 'billing_crc_approved':
            $('#report_filterButton').css({display: "inline"});
            $('#report_filterButton').attr("value", "Filter");

            if ($('#report_filterSelect').val() == 'billing_selectone') {
                selectedid = 0;
                $('#report_filterButton').attr("value", "Reset");
            }
            else if ($('#report_filterSelect').val() == 'billing_local_id') {
                selectedid = 1;
                $('#report_filterButton').attr("value", "Filter");
            }
            else if ($('#report_filterSelect').val() == 'billing_study') {
                selectedid = 2;
                $('#report_filterButton').attr("value", "Filter");
            }
            else if ($('#report_filterSelect').val() == 'billing_industry_initiated') {
                selectedid = 3;
                $('#report_filterButton').attr("value", "Filter");
            }
            else if ($('#report_filterSelect').val() == 'billing_investigator') {
                selectedid = 4;
                $('#report_filterButton').attr("value", "Filter");
            }
            else if ($('#report_filterSelect').val() == 'billing_crc_approved') {
                selectedid = 5;
                $('#report_filterButton').attr("value", "Filter");
            }

            if (report_selectedReport.name == 'billing') {
                recent_reportUrl = "rest/report/getReport?name=billing" +
                    '&filterString=' + search_string +
                    '&filterId=' + selectedid;

                report_getBillingReport(false, 'billing_selectone');
            }


        // Billable Resources

        case 'billable_resources_selectone':
            $('#report_filterText').css({display: "none"});
            $('#report_filterButton').attr("value", "Reset");
        case 'billable_resources_resource':
            search_string = $.trim($("#report_filterText").val());
            $('#report_filterButton').css({display: "inline"});
            $('#report_filterButton').attr("value", "Filter");
        case 'billable_resources_study':
            search_string = $.trim($("#report_filterText").val());
            $('#report_filterButton').css({display: "inline"});
            $('#report_filterButton').attr("value", "Filter");
        case 'billable_resources_local_id':
            search_string = $.trim($("#report_filterText").val());
            $('#report_filterButton').css({display: "inline"});
            $('#report_filterButton').attr("value", "Filter");
        case 'billable_resources_investigator':
            search_string = $.trim($("#report_filterText").val());
            $('#report_filterButton').css({display: "inline"});
            $('#report_filterButton').attr("value", "Filter");
        case 'billable_resources_industry_initiated':
            $('#report_filterButton').css({display: "inline"});
            $('#report_filterButton').attr("value", "Filter");
        case 'billable_resources_crc_approved':
            $('#report_filterButton').css({display: "inline"});
            $('#report_filterButton').attr("value", "Filter");

            if ($('#report_filterSelect').val() == 'billable_resources_selectone') {
                selectedid = 0;
                $('#report_filterButton').attr("value", "Reset");
            }
            else if ($('#report_filterSelect').val() == 'billable_resources_resource') {
                selectedid = 1;
                $('#report_filterButton').attr("value", "Filter");
            }
            else if ($('#report_filterSelect').val() == 'billable_resources_study') {
                selectedid = 2;
                $('#report_filterButton').attr("value", "Filter");
            }
            else if ($('#report_filterSelect').val() == 'billable_resources_local_id') {
                selectedid = 3;
                $('#report_filterButton').attr("value", "Filter");
            }
            else if ($('#report_filterSelect').val() == 'billable_resources_investigator') {
                selectedid = 4;
                $('#report_filterButton').attr("value", "Filter");
            }
            else if ($('#report_filterSelect').val() == 'billable_resources_industry_initiated') {
                selectedid = 5;
                $('#report_filterButton').attr("value", "Filter");
            }
            else if ($('#report_filterSelect').val() == 'billable_resources_crc_approved') {
                selectedid = 6;
                $('#report_filterButton').attr("value", "Filter");
            }

            if (report_selectedReport.name == 'billable_resources') {
                recent_reportUrl = "rest/report/getReport?name=billable_resources" +
                    '&filterString=' + search_string + '&filterId=' + selectedid;
                report_getBillableResourcesReport(false, 'billable_resources_selectone');
            }


        //Billing By Investigator

        case 'billingbyinvestigator_selectone':
            $('#report_filterText').css({display: "none"});
            $('#report_filterButton').attr("value", "Reset");
        case 'billingbyinvestigator_resource':
            search_string = $.trim($("#report_filterText").val());
            $('#report_filterButton').css({display: "inline"});
            $('#report_filterButton').attr("value", "Filter");
        case 'billingbyinvestigator_study':
            search_string = $.trim($("#report_filterText").val());
            $('#report_filterButton').css({display: "inline"});
            $('#report_filterButton').attr("value", "Filter");
        case 'billingbyinvestigator_local_id':
            search_string = $.trim($("#report_filterText").val());
            $('#report_filterButton').css({display: "inline"});
            $('#report_filterButton').attr("value", "Filter");
        case 'billingbyinvestigator_investigator':
            search_string = $.trim($("#report_filterText").val());
            $('#report_filterButton').css({display: "inline"});
            $('#report_filterButton').attr("value", "Filter");
        case 'billingbyinvestigator_industry_initiated':
            search_string = $.trim($("#report_filterText").val());
            $('#report_filterButton').css({display: "inline"});
            $('#report_filterButton').attr("value", "Filter");
        case 'billingbyinvestigator_crc_approved':
            $('#report_filterButton').css({display: "inline"});
            $('#report_filterButton').attr("value", "Filter");

            if ($('#report_filterSelect').val() == 'billingbyinvestigator_selectone') {
                selectedid = 0;
                $('#report_filterButton').attr("value", "Reset");
            }
            else if ($('#report_filterSelect').val() == 'billingbyinvestigator_resource') {
                selectedid = 1;
                $('#report_filterButton').attr("value", "Filter");
            }
            else if ($('#report_filterSelect').val() == 'billingbyinvestigator_study') {
                selectedid = 2;
                $('#report_filterButton').attr("value", "Filter");
            }
            else if ($('#report_filterSelect').val() == 'billingbyinvestigator_local_id') {
                selectedid = 3;
                $('#report_filterButton').attr("value", "Filter");
            }
            else if ($('#report_filterSelect').val() == 'billingbyinvestigator_investigator') {
                selectedid = 4;
                $('#report_filterButton').attr("value", "Filter");
            }
            else if ($('#report_filterSelect').val() == 'billingbyinvestigator_industry_initiated') {
                selectedid = 5;
                $('#report_filterButton').attr("value", "Filter");
            }
            else if ($('#report_filterSelect').val() == 'billingbyinvestigator_crc_approved') {
                selectedid = 6;
                $('#report_filterButton').attr("value", "Filter");
            }

            if (report_selectedReport.name == 'billing_by_investigator') {
                recent_reportUrl = "rest/report/getReport?name=billing_by_investigator" +
                    '&filterString=' + search_string + '&filterId=' + selectedid;
                report_getBillingByInvestigatorReport(false,
                    'billingbyinvestigator_selectone');
            }


        // Operation Transactions

        case 'transactions_selectone':
            $('#report_filterText').css({display: "none"});
            $('#report_filterButton').attr("value", "Reset");
        case 'transactions_study_visit_status':
            search_string = $.trim($("#report_filterText").val());
            $('#report_filterButton').css({display: "inline"});
            $('#report_filterButton').attr("value", "Filter");
        case 'transactions_cancellation_type':
            search_string = $.trim($("#report_filterText").val());
            $('#report_filterButton').css({display: "inline"});
            $('#report_filterButton').attr("value", "Filter");
        case 'transactions_cancellation_reason':
            search_string = $.trim($("#report_filterText").val());
            $('#report_filterButton').css({display: "inline"});
            $('#report_filterButton').attr("value", "Filter");
        case 'transactions_performing_user':
            search_string = $.trim($("#report_filterText").val());
            $('#report_filterButton').css({display: "inline"});
            $('#report_filterButton').attr("value", "Filter");


            if ($('#report_filterSelect').val() == 'transactions_selectone') {
                selectedid = 0;
                $('#report_filterButton').attr("value", "Reset");
            }
            else if ($('#report_filterSelect').val() == 'transactions_study_visit_status') {
                selectedid = 1;
                $('#report_filterButton').attr("value", "Filter");
            }
            else if ($('#report_filterSelect').val() == 'transactions_cancellation_type') {
                selectedid = 2;
                $('#report_filterButton').attr("value", "Filter");
            }
            else if ($('#report_filterSelect').val() == 'transactions_cancellation_reason') {
                selectedid = 3;
                $('#report_filterButton').attr("value", "Filter");
            }
            else if ($('#report_filterSelect').val() == 'transactions_performing_user') {
                selectedid = 4;
                $('#report_filterButton').attr("value", "Filter");
            }

            if (report_selectedReport.name == 'transactions') {
                recent_reportUrl = "rest/report/getReport?name=transactions" +
                    '&filterString=' + search_string +
                    '&filterId=' + selectedid;
                report_getTransactionsReport(false, 'transactions_selectone');
            }


        // Census Report

        case 'census_selectone':
            $('#report_filterText').css({display: "none"});
            $('#report_filterButton').attr("value", "Reset");
        case 'census_checkout_reason':
            search_string = $.trim($("#report_filterText").val());
            $('#report_filterButton').css({display: "inline"});
            $('#report_filterButton').attr("value", "Filter");
        case 'census_investigator':
            search_string = $.trim($("#report_filterText").val());
            $('#report_filterButton').css({display: "inline"});
            $('#report_filterButton').attr("value", "Filter");
        case 'census_local_id':
            search_string = $.trim($("#report_filterText").val());
            $('#report_filterButton').css({display: "inline"});
            $('#report_filterButton').attr("value", "Filter");
        case 'census_study':
            search_string = $.trim($("#report_filterText").val());
            $('#report_filterButton').css({display: "inline"});
            $('#report_filterButton').attr("value", "Filter");
        case 'census_visit':
            search_string = $.trim($("#report_filterText").val());
            $('#report_filterButton').css({display: "inline"});
            $('#report_filterButton').attr("value", "Filter");
        case 'census_crc_approved':
            $('#report_filterButton').css({display: "inline"});
            $('#report_filterButton').attr("value", "Filter");

            if ($('#report_filterSelect').val() == 'census_selectone') {
                selectedid = 0;
                $('#report_filterButton').attr("value", "Reset");
            }
            else if ($('#report_filterSelect').val() == 'census_checkout_reason') {
                selectedid = 1;
                $('#report_filterButton').attr("value", "Filter");
            }
            else if ($('#report_filterSelect').val() == 'census_investigator') {
                selectedid = 2;
                $('#report_filterButton').attr("value", "Filter");
            }
            else if ($('#report_filterSelect').val() == 'census_local_id') {
                selectedid = 3;
                $('#report_filterButton').attr("value", "Filter");
            }
            else if ($('#report_filterSelect').val() == 'census_study') {
                selectedid = 4;
                $('#report_filterButton').attr("value", "Filter");
            }
            else if ($('#report_filterSelect').val() == 'census_visit') {
                selectedid = 5;
                $('#report_filterButton').attr("value", "Filter");
            }
            else if ($('#report_filterSelect').val() == 'census_crc_approved') {
                selectedid = 6;
                $('#report_filterButton').attr("value", "Filter");
            }

            if (report_selectedReport.name == 'census') {
                recent_reportUrl = "rest/report/getReport?name=census" +
                    '&filterString=' + search_string +
                    '&filterId=' + selectedid;
                report_getCensusReport(false, 'census_selectone');
            }


        // Subject Purge

        case 'subjectpurge_selectone':
            $('#report_filterText').css({display: "none"});
            $('#report_filterButton').attr("value", "Reset");
        case 'subjectpurge_subject':
            search_string = $.trim($("#report_filterText").val());
            $('#report_filterButton').css({display: "inline"});
            $('#report_filterButton').attr("value", "Filter");
        case 'subjectpurge_mrn':
            search_string = $.trim($("#report_filterText").val());
            $('#report_filterButton').css({display: "inline"});
            $('#report_filterButton').attr("value", "Filter");
        case 'subjectpurge_performing_user':
            search_string = $.trim($("#report_filterText").val());
            $('#report_filterButton').css({display: "inline"});
            $('#report_filterButton').attr("value", "Filter");


            if ($('#report_filterSelect').val() == 'subjectpurge_selectone') {
                selectedid = 0;
                $('#report_filterButton').attr("value", "Reset");
            }
            else if ($('#report_filterSelect').val() == 'subjectpurge_subject') {
                selectedid = 1;
                $('#report_filterButton').attr("value", "Filter");
            }
            else if ($('#report_filterSelect').val() == 'subjectpurge_mrn') {
                selectedid = 2;
                $('#report_filterButton').attr("value", "Filter");
            }
            else if ($('#report_filterSelect').val() == 'subjectpurge_performing_user') {
                selectedid = 3;
                $('#report_filterButton').attr("value", "Filter");
            }

            if (report_selectedReport.name == 'subject_purge') {
                recent_reportUrl = "rest/report/getReport?name=subject_purge" +
                    '&filterString=' + search_string +
                    '&filterId=' + selectedid;
                report_getSubjectPurgeReport(false, 'subjectpurge_selectone');
            }



        // Override Report

        case 'override_selectone':
            $('#report_filterText').css({display: "none"});
            $('#report_filterButton').attr("value", "Reset");
        case 'override_performing_user':
            search_string = $.trim($("#report_filterText").val());
            $('#report_filterButton').css({display: "inline"});
            $('#report_filterButton').attr("value", "Filter");
        case 'override_visit':
            search_string = $.trim($("#report_filterText").val());
            $('#report_filterButton').css({display: "inline"});
            $('#report_filterButton').attr("value", "Filter");
        case 'override_local_id':
            search_string = $.trim($("#report_filterText").val());
            $('#report_filterButton').css({display: "inline"});
            $('#report_filterButton').attr("value", "Filter");
        case 'or_change_resource':
            $('#report_filterButton').css({display: "inline"});
            $('#report_filterButton').attr("value", "Filter");
        case 'or_change_rn':
            $('#report_filterButton').css({display: "inline"});
            $('#report_filterButton').attr("value", "Filter");
        case 'or_change_rd':
            $('#report_filterButton').css({display: "inline"});
            $('#report_filterButton').attr("value", "Filter");
        case 'or_change_other':
            $('#report_filterButton').css({display: "inline"});
            $('#report_filterButton').attr("value", "Filter");
        case 'or_ol_rn':
            $('#report_filterButton').css({display: "inline"});
            $('#report_filterButton').attr("value", "Filter");
        case 'or_ol_rd':
            $('#report_filterButton').css({display: "inline"});
            $('#report_filterButton').attr("value", "Filter");
        case 'or_ol_other':
            $('#report_filterButton').css({display: "inline"});
            $('#report_filterButton').attr("value", "Filter");


            if ($('#report_filterSelect').val() == 'override_selectone') {
                selectedid = 0;
                $('#report_filterButton').attr("value", "Reset");
            }
            else if ($('#report_filterSelect').val() == 'override_performing_user') {
                selectedid = 1;
                $('#report_filterButton').attr("value", "Filter");
            }
            else if ($('#report_filterSelect').val() == 'override_visit') {
                selectedid = 2;
                $('#report_filterButton').attr("value", "Filter");
            }
            else if ($('#report_filterSelect').val() == 'override_local_id') {
                selectedid = 3;
                $('#report_filterButton').attr("value", "Filter");
            }
            else if ($('#report_filterSelect').val() == 'or_change_resource') {
                selectedid = 4;
                $('#report_filterButton').attr("value", "Filter");
            }
            else if ($('#report_filterSelect').val() == 'or_change_rn') {
                selectedid = 5;
                $('#report_filterButton').attr("value", "Filter");
            }
            else if ($('#report_filterSelect').val() == 'or_change_rd') {
                selectedid = 6;
                $('#report_filterButton').attr("value", "Filter");
            }
            else if ($('#report_filterSelect').val() == 'or_change_other') {
                selectedid = 7;
                $('#report_filterButton').attr("value", "Filter");
            }
            else if ($('#report_filterSelect').val() == 'or_ol_rn') {
                selectedid = 8;
                $('#report_filterButton').attr("value", "Filter");
            }
            else if ($('#report_filterSelect').val() == 'or_ol_rd') {
                selectedid = 9;
                $('#report_filterButton').attr("value", "Filter");
            }
            else if ($('#report_filterSelect').val() == 'or_ol_other') {
                selectedid = 10;
                $('#report_filterButton').attr("value", "Filter");
            }

            if (report_selectedReport.name == 'override') {
                recent_reportUrl = "rest/report/getReport?name=override" +
                    '&filterString=' + search_string +
                    '&filterId=' + selectedid;

                report_getOverrideReport(false, 'override_selectone');
            }

        // User Data
        case 'user_data_selectone':
            $('#report_filterText').css({display: "none"});
            $('#report_filterButton').attr("value", "Reset");
        case 'user_data_user_name':
            search_string = $.trim($("#report_filterText").val());
            $('#report_filterButton').css({display: "inline"});
            $('#report_filterButton').attr("value", "Filter");
        case 'user_data_inst_role':
            search_string = $.trim($("#report_filterText").val());
            $('#report_filterButton').css({display: "inline"});
            $('#report_filterButton').attr("value", "Filter");
        case 'user_data_user_title':
            search_string = $.trim($("#report_filterText").val());
            $('#report_filterButton').css({display: "inline"});
            $('#report_filterButton').attr("value", "Filter");
        case 'user_data_status_active':
            $('#report_filterButton').css({display: "inline"});
            $('#report_filterButton').attr("value", "Filter");
        case 'user_data_status_not_active':
            $('#report_filterButton').css({display: "inline"});
            $('#report_filterButton').attr("value", "Filter");

            if ($('#report_filterSelect').val() == 'user_data_selectone') {
                selectedid = 0;
                $('#report_filterButton').attr("value", "Reset");
            }
            else if ($('#report_filterSelect').val() == 'user_data_user_name') {
                selectedid = 1;
                $('#report_filterButton').attr("value", "Filter");
            }
            else if ($('#report_filterSelect').val() == 'user_data_inst_role') {
                selectedid = 2;
                $('#report_filterButton').attr("value", "Filter");
            }
            else if ($('#report_filterSelect').val() == 'user_data_user_title') {
                selectedid = 3;
                $('#report_filterButton').attr("value", "Filter");
            }
            else if ($('#report_filterSelect').val() == 'user_data_status_active') {
                selectedid = 4;
                $('#report_filterButton').attr("value", "Filter");
            }
            else if ($('#report_filterSelect').val() == 'user_data_status_not_active') {
                selectedid = 5;
                $('#report_filterButton').attr("value", "Filter");
            }

            if (report_selectedReport.name == 'user_data') {
                recent_reportUrl = "rest/report/getReport?name=user_data" +
                    '&filterString=' + search_string +
                    '&filterId=' + selectedid;

                report_getUserDataReport(false, 'user_data_selectone');
            }


        // Daily Overview

        case 'daily_overview_selectone':
            $('#report_filterText').css({display: "none"});
            $('#report_filterButton').attr("value", "Reset");
        case 'daily_overview_local_id':
            search_string = $.trim($("#report_filterText").val());
            $('#report_filterButton').css({display: "inline"});
            $('#report_filterButton').attr("value", "Filter");
        case 'daily_overview_visit':
            search_string = $.trim($("#report_filterText").val());
            $('#report_filterButton').css({display: "inline"});
            $('#report_filterButton').attr("value", "Filter");
        case 'daily_overview_resource_type':
            search_string = $("#overview_resource_type option:selected").val();
            $('#report_filterButton').css({display: "inline"});
            $('#report_filterButton').attr("value", "Filter");
            $('#overview_resource_type').css({display: "none"});
        case 'daily_overview_sublocation':
            search_string = $("#overview_sublocation option:selected").val();
            $('#report_filterButton').css({display: "inline"});
            $('#report_filterButton').attr("value", "Filter");
            $('#overview_sublocation').css({display: "none"});
        case 'daily_overview_subject':
            search_string = $.trim($("#report_filterText").val());
            $('#report_filterButton').css({display: "inline"});
            $('#report_filterButton').attr("value", "Filter");
        case 'daily_overview_mrn':
            search_string = $.trim($("#report_filterText").val());
            $('#report_filterButton').css({display: "inline"});
            $('#report_filterButton').attr("value", "Filter");
        case 'daily_overview_is_part_off_unit':
            search_string = $.trim($("#report_filterText").val());
            $('#report_filterButton').css({display: "inline"});
            $('#report_filterButton').attr("value", "Filter");


            if ($('#report_filterSelect').val() == 'daily_overview_selectone') {
                selectedid = 0;
                $('#report_filterButton').attr("value", "Reset");
            }
            else if ($('#report_filterSelect').val() == 'daily_overview_local_id') {
                selectedid = 1;
                $('#report_filterButton').attr("value", "Filter");
            }
            else if ($('#report_filterSelect').val() == 'daily_overview_visit') {
                selectedid = 2;
                $('#report_filterButton').attr("value", "Filter");
            }
            else if ($('#report_filterSelect').val() == 'daily_overview_subject') {
                selectedid = 3;
                $('#report_filterButton').attr("value", "Filter");
            }
            else if ($('#report_filterSelect').val() == 'daily_overview_mrn') {
                selectedid = 4;
                $('#report_filterButton').attr("value", "Filter");
            }
            else if ($('#report_filterSelect').val() == 'daily_overview_is_part_off_unit') {
                selectedid = 5;
                $('#report_filterButton').attr("value", "Filter");
            }
            else if ($('#report_filterSelect').val() == 'daily_overview_resource_type') {
                search_string = $("#overview_resource_type option:selected").val();
                selectedid = 6;
                $('#report_filterButton').attr("value", "Filter");
                $('#report_filterButton').css({display: "inline"});
                $('#overview_resource_type').css({display: "none"});
            }
            else if ($('#report_filterSelect').val() == 'daily_overview_sublocation') {
                search_string = $("#overview_sublocation option:selected").val();
                selectedid = 7;
                $('#report_filterButton').attr("value", "Filter");
                $('#report_filterButton').css({display: "inline"});
                $('#overview_sublocation').css({display: "none"});
            }

            if (report_selectedReport.name == 'daily_overview') {
                recent_reportUrl = "rest/report/getReport?name=daily_overview" +
                    '&filterString=' + search_string +
                    '&filterId=' + selectedid;
                report_getDailyOverviewReport(false, 'daily_overview_selectone');
            }


        // Off Unit Report

        case 'off_unit_selectone':
            $('#report_filterText').css({display: "none"});
            $('#report_filterButton').attr("value", "Reset");
        case 'off_unit_visit':
            search_string = $.trim($("#report_filterText").val());
            $('#report_filterButton').css({display: "inline"});
            $('#report_filterButton').attr("value", "Filter");
        case 'off_unit_local_id':
            search_string = $.trim($("#report_filterText").val());
            $('#report_filterButton').css({display: "inline"});
            $('#report_filterButton').attr("value", "Filter");
        case 'off_unit_last_name':
            search_string = $.trim($("#report_filterText").val());
            $('#report_filterButton').css({display: "inline"});
            $('#report_filterButton').attr("value", "Filter");
        case 'off_unit_visit_type':
            search_string = $.trim($("#report_filterText").val());
            $('#report_filterButton').css({display: "inline"});
            $('#report_filterButton').attr("value", "Filter");
        case 'off_unit_crc_approved':
            $('#report_filterButton').css({display: "inline"});
            $('#report_filterButton').attr("value", "Filter");
        case 'off_unit_group_nursing':
            $('#report_filterButton').css({display: "inline"});
            $('#report_filterButton').attr("value", "Filter");
        case 'off_unit_group_nutrition':
            $('#report_filterButton').css({display: "inline"});
            $('#report_filterButton').attr("value", "Filter");
        case 'off_unit_group_room':
            $('#report_filterButton').css({display: "inline"});
            $('#report_filterButton').attr("value", "Filter");
        case 'off_unit_group_lab':
            $('#report_filterButton').css({display: "inline"});
            $('#report_filterButton').attr("value", "Filter");
        case 'off_unit_group_other':
            $('#report_filterButton').css({display: "inline"});
            $('#report_filterButton').attr("value", "Filter");
        case 'off_unit_mrn':
            search_string = $.trim($("#report_filterText").val());
            $('#report_filterButton').css({display: "inline"});
            $('#report_filterButton').attr("value", "Filter");

            if ($('#report_filterSelect').val() == 'off_unit_selectone') {
                selectedid = 0;
                $('#report_filterButton').attr("value", "Reset");
            }
            else if ($('#report_filterSelect').val() == 'off_unit_visit') {
                selectedid = 1;
                $('#report_filterButton').attr("value", "Filter");
            }
            else if ($('#report_filterSelect').val() == 'off_unit_local_id') {
                selectedid = 2;
                $('#report_filterButton').attr("value", "Filter");
            }
            else if ($('#report_filterSelect').val() == 'off_unit_last_name') {
                selectedid = 3;
                $('#report_filterButton').attr("value", "Filter");
            }
            else if ($('#report_filterSelect').val() == 'off_unit_visit_type') {
                selectedid = 4;
                $('#report_filterButton').attr("value", "Filter");
            }
            else if ($('#report_filterSelect').val() == 'off_unit_crc_approved') {
                selectedid = 5;
                $('#report_filterButton').attr("value", "Filter");
            }
            else if ($('#report_filterSelect').val() == 'off_unit_group_nursing') {
                selectedid = 6;
                $('#report_filterButton').attr("value", "Filter");
            }
            else if ($('#report_filterSelect').val() == 'off_unit_group_nutrition') {
                selectedid = 7;
                $('#report_filterButton').attr("value", "Filter");
            }
            else if ($('#report_filterSelect').val() == 'off_unit_group_room') {
                selectedid = 8;
                $('#report_filterButton').attr("value", "Filter");
            }
            else if ($('#report_filterSelect').val() == 'off_unit_group_lab') {
                selectedid = 9;
                $('#report_filterButton').attr("value", "Filter");
            }
            else if ($('#report_filterSelect').val() == 'off_unit_group_other') {
                selectedid = 10;
                $('#report_filterButton').attr("value", "Filter");
            }
            else if ($('#report_filterSelect').val() == 'off_unit_mrn') {
                selectedid = 11;
                $('#report_filterButton').attr("value", "Filter");
            }

            if (report_selectedReport.name == 'off_unit') {
                recent_reportUrl = "rest/report/getReport?name=off_unit" +
                    '&filterString=' + search_string +
                    '&filterId=' + selectedid;
                report_getOffUnitReport(false, 'off_unit_selectone');
            }


        //Level of Service Report

        case 'levelofservice_selectone':
            $('#report_filterText').css({display: "none"});
            $('#report_filterButton').attr("value", "Reset");
        case 'levelofservice_sl_nursing':
            search_string = $.trim($("#report_filterText").val());
            $('#report_filterButton').css({display: "inline"});
            $('#report_filterButton').attr("value", "Filter");
        case 'levelofservice_sl_nutrition':
            search_string = $.trim($("#report_filterText").val());
            $('#report_filterButton').css({display: "inline"});
            $('#report_filterButton').attr("value", "Filter");
        case 'levelofservice_sl_processing':
            search_string = $.trim($("#report_filterText").val());
            $('#report_filterButton').css({display: "inline"});
            $('#report_filterButton').attr("value", "Filter");
        case 'levelofservice_sl_setup':
            search_string = $.trim($("#report_filterText").val());
            $('#report_filterButton').css({display: "inline"});
            $('#report_filterButton').attr("value", "Filter");
        case 'levelofservice_visit_type':
            search_string = $.trim($("#report_filterText").val());
            $('#report_filterButton').css({display: "inline"});
            $('#report_filterButton').attr("value", "Filter");
        case 'levelofservice_appt_status':
            search_string = $.trim($("#report_filterText").val());
            $('#report_filterButton').css({display: "inline"});
            $('#report_filterButton').attr("value", "Filter");
        case 'levelofservice_checkout_reason':
            search_string = $.trim($("#report_filterText").val());
            $('#report_filterButton').css({display: "inline"});
            $('#report_filterButton').attr("value", "Filter");
        case 'levelofservice_crc_approved':
            $('#report_filterButton').css({display: "inline"});
            $('#report_filterButton').attr("value", "Filter");

            if ($('#report_filterSelect').val() == 'levelofservice_selectone') {
                selectedid = 0;
                $('#report_filterButton').attr("value", "Reset");
            }
            else if ($('#report_filterSelect').val() == 'levelofservice_sl_nursing') {
                selectedid = 1;
                $('#report_filterButton').attr("value", "Filter");
            }
            else if ($('#report_filterSelect').val() == 'levelofservice_sl_nutrition') {
                selectedid = 2;
                $('#report_filterButton').attr("value", "Filter");
            }
            else if ($('#report_filterSelect').val() == 'levelofservice_sl_processing') {
                selectedid = 3;
                $('#report_filterButton').attr("value", "Filter");
            }
            else if ($('#report_filterSelect').val() == 'levelofservice_sl_setup') {
                selectedid = 4;
                $('#report_filterButton').attr("value", "Filter");
            }
            else if ($('#report_filterSelect').val() == 'levelofservice_visit_type') {
                selectedid = 5;
                $('#report_filterButton').attr("value", "Filter");
            }
            else if ($('#report_filterSelect').val() == 'levelofservice_appt_status') {
                selectedid = 6;
                $('#report_filterButton').attr("value", "Filter");
            }
            else if ($('#report_filterSelect').val() == 'levelofservice_checkout_reason') {
                selectedid = 7;
                $('#report_filterButton').attr("value", "Filter");
            }
            else if ($('#report_filterSelect').val() == 'levelofservice_crc_approved') {
                selectedid = 8;
                $('#report_filterButton').attr("value", "Filter");
            }

            if (report_selectedReport.name == 'level_of_service') {
                recent_reportUrl = "rest/report/getReport?name=level_of_service" +
                    '&filterString=' + search_string +
                    '&filterId=' + selectedid;
                report_getLevelOfServiceReport(false, 'levelofservice_selectone');
            }

        //Study Subject Visit Report

        case 'studysubjectvisit_selectone':
            $('#report_filterText').css({display: "none"});
            $('#report_filterButton').attr("value", "Reset");
        case 'studysubjectvisit_localId':
            search_string = $.trim($("#report_filterText").val());
            $('#report_filterButton').css({display: "inline"});
            $('#report_filterButton').attr("value", "Filter");
        case 'studysubjectvisit_appointment':
            search_string = $.trim($("#report_filterText").val());
            $('#report_filterButton').css({display: "inline"});
            $('#report_filterButton').attr("value", "Filter");
        case 'studysubjectvisit_crc_approved':
            $('#report_filterButton').css({display: "inline"});
            $('#report_filterButton').attr("value", "Filter");

            if ($('#report_filterSelect').val() == 'studysubjectvisit_selectone') {
                selectedid = 0;
                $('#report_filterButton').attr("value", "Reset");
            }
            else if ($('#report_filterSelect').val() == 'studysubjectvisit_localId') {
                selectedid = 3;
                $('#report_filterButton').attr("value", "Filter");
            }
            else if ($('#report_filterSelect').val() == 'studysubjectvisit_appointment') {
                selectedid = 4;
                $('#report_filterButton').attr("value", "Filter");
            }
            else if ($('#report_filterSelect').val() == 'studysubjectvisit_crc_approved') {
                selectedid = 5;
                $('#report_filterButton').attr("value", "Filter");
            }

            if (report_selectedReport.name == 'study_subject_visit') {
                recent_reportUrl = "rest/report/getReport?name=study_subject_visit" +
                    '&filterString=' + search_string +
                    '&filterId=' + selectedid;
                report_getStudySubjectVisitReport(false, 'studysubjectvisit_selectone');
            }

        // Study Status Change Report

        case 'studystatuschange_selectone':
            $('#report_filterText').css({display: "none"});
            $('#report_filterButton').attr("value", "Reset");
        case 'studystatuschange_local_id':
            search_string = $.trim($("#report_filterText").val());
            $('#report_filterButton').css({display: "inline"});
            $('#report_filterButton').attr("value", "Filter");
        case 'studystatuschange_investigator':
            search_string = $.trim($("#report_filterText").val());
            $('#report_filterButton').css({display: "inline"});
            $('#report_filterButton').attr("value", "Filter");
        case 'studystatuschange_study_status':
            search_string = $.trim($("#report_filterText").val());
            $('#report_filterButton').css({display: "inline"});
            $('#report_filterButton').attr("value", "Filter");
        case 'studystatuschange_crc_approved':
            $('#report_filterButton').css({display: "inline"});
            $('#report_filterButton').attr("value", "Filter");

            if ($('#report_filterSelect').val() == 'studystatuschange_selectone') {
                selectedid = 0;
                $('#report_filterButton').attr("value", "Reset");
            }
            else if ($('#report_filterSelect').val() == 'studystatuschange_local_id') {
                selectedid = 1;
                $('#report_filterButton').attr("value", "Filter");
            }
            else if ($('#report_filterSelect').val() == 'studystatuschange_investigator') {
                selectedid = 2;
                $('#report_filterButton').attr("value", "Filter");
            }
            else if ($('#report_filterSelect').val() == 'studystatuschange_study_status') {
                selectedid = 3;
                $('#report_filterButton').attr("value", "Filter");
            }
            else if ($('#report_filterSelect').val() == 'studystatuschange_crc_approved') {
                selectedid = 4;
                $('#report_filterButton').attr("value", "Filter");
            }

            if (report_selectedReport.name == 'study_status_change') {

                recent_reportUrl = "rest/report/getReport?name=study_status_change" +
                    '&filterString=' + search_string +
                    '&filterId=' + selectedid;
                report_getStudyStatusChangeReport(false, 'studystatuschange_selectone');
            }


        //Subject Visit History Report

        case 'subjectvisithistory_selectone':
            $('#report_filterText').css({display: "none"});
            $('#report_filterButton').attr("value", "Reset");
        case 'subjectvisithistory_localId':
            search_string = $.trim($("#report_filterText").val());
            $('#report_filterButton').css({display: "inline"});
            $('#report_filterButton').attr("value", "Filter");
        case 'subjectvisithistory_crc_approved':
            $('#report_filterButton').css({display: "inline"});
            $('#report_filterButton').attr("value", "Filter");

            if ($('#report_filterSelect').val() == 'subjectvisithistory_selectone') {
                selectedid = 0;
                $('#report_filterButton').attr("value", "Reset");
            }
            else if ($('#report_filterSelect').val() == 'subjectvisithistory_localId') {
                selectedid = 3;
                $('#report_filterButton').attr("value", "Filter");
            }
            else if ($('#report_filterSelect').val() == 'subjectvisithistory_crc_approved') {
                selectedid = 4;
                $('#report_filterButton').attr("value", "Filter");
            }

            if (report_selectedReport.name == 'subject_visit_history') {
                recent_reportUrl = "rest/report/getReport?name=subject_visit_history" +
                    '&filterString=' + search_string +
                    '&filterId=' + selectedid;
                report_getSubjectVisitHistoryReport(false, 'subjectvisithistory_selectone');
            }

        //CRC Availability Report

        case 'crcavailability_selectone':
            $('#report_filterText').css({display: "none"});
            $('#report_filterButton').attr("value", "Reset");


            if ($('#report_filterSelect').val() == 'crcavailability_selectone') {
                selectedid = 0;
                $('#report_filterButton').attr("value", "Reset");
            }

            if (report_selectedReport.name == 'crc_availability') {
                recent_reportUrl =
                    "rest/report/getReport?name=crc_availability" +
                    '&filterString=' + search_string + '&filterId=' + selectedid;

                report_getCRCAvailabilityReport(false,
                    'crcavailability_selectone');
            }

        //Ancillary Only Visits By Protocol

        case 'ancillaryonlybyprotocol_selectone':
            $('#report_filterText').css({display: "none"});
            $('#report_filterButton').attr("value", "Reset");
        case 'ancillaryonlybyprotocol_localID':
            search_string = $.trim($("#report_filterText").val());
            $('#report_filterButton').css({display: "inline"});
            $('#report_filterButton').attr("value", "Filter");
        case 'ancillaryonlybyprotocol_crc_approved':
            $('#report_filterButton').css({display: "inline"});
            $('#report_filterButton').attr("value", "Filter");

            if ($('#report_filterSelect').val() == 'ancillaryonlybyprotocol_selectone') {
                selectedid = 0;
                $('#report_filterButton').attr("value", "Reset");
            }
            else if ($('#report_filterSelect').val() == 'ancillaryonlybyprotocol_localID') {
                selectedid = 1;
                $('#report_filterButton').attr("value", "Filter");
            }
            else if ($('#report_filterSelect').val() == 'ancillaryonlybyprotocol_crc_approved') {
                selectedid = 2;
                $('#report_filterButton').attr("value", "Filter");
            }


            if (report_selectedReport.name == 'ancillary_only_by_protocol') {
                recent_reportUrl =
                    "rest/report/getReport?name=ancillary_only_by_protocol" +
                    '&filterString=' + search_string + '&filterId=' + selectedid;
                report_getAncillaryOnlyByProtocolReport(false,
                    'ancillaryonlybyprotocol_selectone');
            }

        //Resource Level Of Service Report

        case 'resourcelevelofservice_selectone':
            $('#report_filterText').css({display: "none"});
            $('#report_filterButton').attr("value", "Reset");
        case 'resourcelevelofservice_localID':
            search_string = $.trim($("#report_filterText").val());
            $('#report_filterButton').css({display: "inline"});
            $('#report_filterButton').attr("value", "Filter");
        case 'resourcelevelofservice_visitName':
            search_string = $.trim($("#report_filterText").val());
            $('#report_filterButton').css({display: "inline"});
            $('#report_filterButton').attr("value", "Filter");


            if ($('#report_filterSelect').val() == 'resourcelevelofservice_selectone') {
                selectedid = 0;
                $('#report_filterButton').attr("value", "Reset");
            }
            else if ($('#report_filterSelect').val() == 'resourcelevelofservice_localID') {
                selectedid = 1;
                $('#report_filterButton').attr("value", "Filter");
            }
            else if ($('#report_filterSelect').val() == 'resourcelevelofservice_visitName') {
                selectedid = 2;
                $('#report_filterButton').attr("value", "Filter");
            }

            if (report_selectedReport.name == 'resource_level_of_service') {
                recent_reportUrl = 'rest/report/resourceLevelOfServiceReport?' +
                    'filterString=' + search_string +
                    '&filterId=' + selectedid;
                report_getResourceLevelOfServiceReport(false, 'resourcelevelofservice_selectone');
            }

        // Nutrition Tasks Report

        case 'nutritiontasks_selectone':
            $('#report_filterText').css({display: "none"});
            $('#report_filterButton').attr("value", "Reset");
        case 'nutritiontasks_local_id':
            search_string = $.trim($("#report_filterText").val());
            $('#report_filterButton').css({display: "inline"});
            $('#report_filterButton').attr("value", "Filter");
        case 'nutritiontasks_name':
            search_string = $.trim($("#report_filterText").val());
            $('#report_filterButton').css({display: "inline"});
            $('#report_filterButton').attr("value", "Filter");
        case 'nutritiontasks_anthropometry_simple':
            search_string = $.trim($("#report_filterText").val());
            $('#report_filterButton').css({display: "inline"});
            $('#report_filterButton').attr("value", "Filter");
        case 'nutritiontasks_anthropometry_complex':
            search_string = $.trim($("#report_filterText").val());
            $('#report_filterButton').css({display: "inline"});
            $('#report_filterButton').attr("value", "Filter");
        case 'nutritiontasks_meal_plan_calculation':
            search_string = $.trim($("#report_filterText").val());
            $('#report_filterButton').css({display: "inline"});
            $('#report_filterButton').attr("value", "Filter");
        case 'nutritiontasks_nutrient_analysis':
            search_string = $.trim($("#report_filterText").val());
            $('#report_filterButton').css({display: "inline"});
            $('#report_filterButton').attr("value", "Filter");
        case 'nutritiontasks_education_time':
            search_string = $.trim($("#report_filterText").val());
            $('#report_filterButton').css({display: "inline"});
            $('#report_filterButton').attr("value", "Filter");
        case 'nutritiontasks_questionnaire_time':
            search_string = $.trim($("#report_filterText").val());
            $('#report_filterButton').css({display: "inline"});
            $('#report_filterButton').attr("value", "Filter");
        case 'nutritiontasks_crc_approved':
            $('#report_filterButton').css({display: "inline"});
            $('#report_filterButton').attr("value", "Filter");


            if ($('#report_filterSelect').val() == 'nutritiontasks_selectone') {
                selectedid = 0;
                $('#report_filterButton').attr("value", "Reset");
            }
            else if ($('#report_filterSelect').val() == 'nutritiontasks_local_id') {
                selectedid = 1;
                $('#report_filterButton').attr("value", "Reset");
            }
            else if ($('#report_filterSelect').val() == 'nutritiontasks_name') {
                selectedid = 2;
                $('#report_filterButton').attr("value", "Reset");
            }
            else if ($('#report_filterSelect').val() == 'nutritiontasks_anthropometry_simple') {
                selectedid = 3;
                $('#report_filterButton').attr("value", "Reset");
            }
            else if ($('#report_filterSelect').val() == 'nutritiontasks_anthropometry_complex') {
                selectedid = 4;
                $('#report_filterButton').attr("value", "Reset");
            }
            else if ($('#report_filterSelect').val() == 'nutritiontasks_meal_plan_calculation') {
                selectedid = 5;
                $('#report_filterButton').attr("value", "Reset");
            }
            else if ($('#report_filterSelect').val() == 'nutritiontasks_nutrient_analysis') {
                selectedid = 6;
                $('#report_filterButton').attr("value", "Reset");
            }
            else if ($('#report_filterSelect').val() == 'nutritiontasks_education_time') {
                selectedid = 7;
                $('#report_filterButton').attr("value", "Reset");
            }
            else if ($('#report_filterSelect').val() == 'nutritiontasks_questionnaire_time') {
                selectedid = 8;
                $('#report_filterButton').attr("value", "Reset");
            }
            else if ($('#report_filterSelect').val() == 'nutritiontasks_crc_approved') {
                selectedid = 9;
                $('#report_filterButton').attr("value", "Reset");
            }

        //Visit Duartion By Visit Type

        case 'visitdurationbyvisittype_selectone':
            $('#report_filterText').css({display: "none"});
            $('#report_filterButton').attr("value", "Reset");
        case 'visitdurationbyvisittype_visitname':
            search_string = $.trim($("#report_filterText").val());
            $('#report_filterButton').css({display: "inline"});
            $('#report_filterButton').attr("value", "Filter");
        case 'visitdurationbyvisittype_visittype':
            search_string = $.trim($("#report_filterText").val());
            $('#report_filterButton').css({display: "inline"});
            $('#report_filterButton').attr("value", "Filter");


            if ($('#report_filterSelect').val() == 'visitdurationbyvisittype_selectone') {
                selectedid = 0;
                $('#report_filterButton').attr("value", "Reset");
            }
            else if ($('#report_filterSelect').val() == 'visitdurationbyvisittype_visitname') {
                selectedid = 1;
                $('#report_filterButton').attr("value", "Reset");
            }
            else if ($('#report_filterSelect').val() == 'visitdurationbyvisittype_visittype') {
                selectedid = 2;
                $('#report_filterButton').attr("value", "Reset");
            }

            if (report_selectedReport.name == 'visit_duration_by_visit_type') {
                recent_reportUrl =
                    "rest/report/getReport?name=visit_duration_by_visit_type" +
                    '&filterString=' + search_string + '&filterId=' + selectedid;
                report_getVisitDurationByVisitTypeReport(false,
                    'visitdurationbyvisittype_selectone');
            }

        //Booked Visit Service levels by type

        case 'bookedvisitservicelevelbyvisittype_selectone':
            $('#report_filterText').css({display: "none"});
            $('#report_filterButton').attr("value", "Reset");
        case 'bookedvisitservicelevelbyvisittype_level0':
            $('#report_filterText').css({display: "none"});
            $('#report_filterButton').css({display: "inline"});
            $('#report_filterButton').attr("value", "Filter");
        case 'bookedvisitservicelevelbyvisittype_level1':
            $('#report_filterText').css({display: "none"});
            $('#report_filterButton').css({display: "inline"});
            $('#report_filterButton').attr("value", "Filter");
        case 'bookedvisitservicelevelbyvisittype_level2':
            $('#report_filterText').css({display: "none"});
            $('#report_filterButton').css({display: "inline"});
            $('#report_filterButton').attr("value", "Filter");
        case 'bookedvisitservicelevelbyvisittype_level3':
            $('#report_filterText').css({display: "none"});
            $('#report_filterButton').css({display: "inline"});
            $('#report_filterButton').attr("value", "Filter");
        case 'bookedvisitservicelevelbyvisittype_level4':
            $('#report_filterText').css({display: "none"});
            $('#report_filterButton').css({display: "inline"});
            $('#report_filterButton').attr("value", "Filter");
        case 'bookedvisitservicelevelbyvisittype_level5':
            $('#report_filterText').css({display: "none"});
            $('#report_filterButton').css({display: "inline"});
            $('#report_filterButton').attr("value", "Filter");
        case 'bookedvisitservicelevelbyvisittype_level6':
            $('#report_filterText').css({display: "none"});
            $('#report_filterButton').css({display: "inline"});
            $('#report_filterButton').attr("value", "Filter");
        case 'bookedvisitservicelevelbyvisittype_level7':
            $('#report_filterText').css({display: "none"});
            $('#report_filterButton').css({display: "inline"});
            $('#report_filterButton').attr("value", "Filter");


            if ($('#report_filterSelect').val() == 'bookedvisitservicelevelbyvisittype_selectone') {
                selectedid = -1;
                $('#report_filterButton').attr("value", "Reset");
            }
            else if ($('#report_filterSelect').val() == 'bookedvisitservicelevelbyvisittype_level0') {
                selectedid = 0;
                $('#report_filterButton').attr("value", "Reset");
            }
            else if ($('#report_filterSelect').val() == 'bookedvisitservicelevelbyvisittype_level1') {
                selectedid = 1;
                $('#report_filterButton').attr("value", "Reset");
            }
            else if ($('#report_filterSelect').val() == 'bookedvisitservicelevelbyvisittype_level2') {
                selectedid = 2;
                $('#report_filterButton').attr("value", "Reset");
            }
            else if ($('#report_filterSelect').val() == 'bookedvisitservicelevelbyvisittype_level3') {
                selectedid = 3;
                $('#report_filterButton').attr("value", "Reset");
            }
            else if ($('#report_filterSelect').val() == 'bookedvisitservicelevelbyvisittype_level4') {
                selectedid = 4;
                $('#report_filterButton').attr("value", "Reset");
            }
            else if ($('#report_filterSelect').val() == 'bookedvisitservicelevelbyvisittype_level5') {
                selectedid = 5;
                $('#report_filterButton').attr("value", "Reset");
            }
            else if ($('#report_filterSelect').val() == 'bookedvisitservicelevelbyvisittype_level6') {
                selectedid = 6;
                $('#report_filterButton').attr("value", "Reset");
            }
            else if ($('#report_filterSelect').val() == 'bookedvisitservicelevelbyvisittype_level7') {
                selectedid = 7;
                $('#report_filterButton').attr("value", "Reset");
            }


            if (report_selectedReport.name == 'booked_visit_service_level_by_visit_type') {

                recent_reportUrl =
                    "rest/report/getReport?name=booked_visit_service_level_by_visit_type" +
                    '&sortId=2';
                report_getBookedVisitServiceLevelByTypeReport(false,
                    'bookedvisitservicelevelbyvisittype_selectone');
            }

        //Visit Template Data Report

        case 'visittemplatedata_selectone':
            $('#report_filterText').css({display: "none"});
            $('#report_filterButton').attr("value", "Reset");
        case 'visittemplatedata_localID':
            search_string = $.trim($("#report_filterText").val());
            $('#report_filterButton').css({display: "inline"});
            $('#report_filterButton').attr("value", "Filter");
        case 'visittemplatedata_visitName':
            search_string = $.trim($("#report_filterText").val());
            $('#report_filterButton').css({display: "inline"});
            $('#report_filterButton').attr("value", "Filter");

            if ($('#report_filterSelect').val() == 'visittemplatedata_selectone') {
                selectedid = 0;
                $('#report_filterButton').attr("value", "Reset");
            }
            else if ($('#report_filterSelect').val() == 'visittemplatedata_localID') {
                selectedid = 1;
                $('#report_filterButton').attr("value", "Filter");
            }
            else if ($('#report_filterSelect').val() == 'visittemplatedata_visitName') {
                selectedid = 2;
                $('#report_filterButton').attr("value", "Filter");
            }

            if (report_selectedReport.name == 'visit_template_data') {
                recent_reportUrl = "rest/report/getReport?name=visit_template_data" +
                    '&filterString=' + search_string +
                    '&filterId=' + selectedid;
                report_getVisitTemplateDataReport(false, 'visittemplatedata_selectone');
            }


        //Visit Terminated Prior to Completion

        case 'visitterminatedpriortocompletion_selectone':
            $('#report_filterText').css({display: "none"});
            $('#report_filterButton').attr("value", "Reset");
        case 'visitterminatedpriortocompletion_level0':
            search_string = $.trim($("#report_filterText").val());
            $('#report_filterText').css({display: "none"});
            $('#report_filterButton').css({display: "inline"});
            $('#report_filterButton').attr("value", "Filter");
        case 'visitterminatedpriortocompletion_level1':
            search_string = $.trim($("#report_filterText").val());
            $('#report_filterText').css({display: "none"});
            $('#report_filterButton').css({display: "inline"});
            $('#report_filterButton').attr("value", "Filter");
        case 'visitterminatedpriortocompletion_level2':
            search_string = $.trim($("#report_filterText").val());
            $('#report_filterText').css({display: "none"});
            $('#report_filterButton').css({display: "inline"});
            $('#report_filterButton').attr("value", "Filter");

            if ($('#report_filterSelect').val() == 'visitterminatedpriortocompletion_selectone') {
                selectedid = 0;
                $('#report_filterButton').attr("value", "Reset");
            }
            else if ($('#report_filterSelect').val() == 'visitterminatedpriortocompletion_level0') {
                selectedid = 1;
                $('#report_filterButton').attr("value", "Reset");
            }
            else if ($('#report_filterSelect').val() == 'visitterminatedpriortocompletion_level1') {
                selectedid = 2;
                $('#report_filterButton').attr("value", "Reset");
            }
            else if ($('#report_filterSelect').val() == 'visitterminatedpriortocompletion_level2') {
                selectedid = 3;
                $('#report_filterButton').attr("value", "Reset");
            }

            if (($("#report_filterSelect option:selected").val() != 'subjectaudit_selectone') && ($("#report_filterSelect option:selected").val() != 'staffaudit_selectone')
                && ($("#report_filterSelect option:selected").val() != 'dailyresource_selectone') && ($("#report_filterSelect option:selected").val() != 'resource_group_nursing')
                && ($("#report_filterSelect option:selected").val() != 'resource_group_room') && ($("#report_filterSelect option:selected").val() != 'resource_group_lab')
                && ($("#report_filterSelect option:selected").val() != 'resource_group_other') && ($("#report_filterSelect option:selected").val() != 'resource_group_nutrition')
                && ($("#report_filterSelect option:selected").val() != 'daily_adm_selectone') && ($("#report_filterSelect option:selected").val() != 'metakitch_selectone')
                && ($("#report_filterSelect option:selected").val() != 'weeklypharm_selectone')
                && ($("#report_filterSelect option:selected").val() != 'protonurse_selectone') && ($("#report_filterSelect option:selected").val() != 'studyvisitlocation_selectone')
                && ($("#report_filterSelect option:selected").val() != 'deptandpi_selectone') && ($("#report_filterSelect option:selected").val() != 'billing_selectone')
                && ($("#report_filterSelect option:selected").val() != 'billable_resources_selectone') && ($("#report_filterSelect option:selected").val() != 'billingbyinvestigator_selectone')
                && ($("#report_filterSelect option:selected").val() != 'transactions_selectone') && ($("#report_filterSelect option:selected").val() != 'census_selectone')
                && ($("#report_filterSelect option:selected").val() != 'subjectpurge_selectone') && ($("#report_filterSelect option:selected").val() != 'override_selectone')
                && ($("#report_filterSelect option:selected").val() != 'or_change_resource') && ($("#report_filterSelect option:selected").val() != 'or_change_rn')
                && ($("#report_filterSelect option:selected").val() != 'or_change_rd') && ($("#report_filterSelect option:selected").val() != 'or_change_other')
                && ($("#report_filterSelect option:selected").val() != 'or_ol_rn') && ($("#report_filterSelect option:selected").val() != 'or_ol_rd')
                && ($("#report_filterSelect option:selected").val() != 'or_ol_other')
                && ($("#report_filterSelect option:selected").val() != 'user_data_selectone')
                && ($("#report_filterSelect option:selected").val() != 'user_data_status_active') && ($("#report_filterSelect option:selected").val() != 'user_data_status_not_active')
                && ($("#report_filterSelect option:selected").val() != 'daily_overview_selectone') && ($("#report_filterSelect option:selected").val() != 'off_unit_selectone')
                && ($("#report_filterSelect option:selected").val() != 'levelofservice_selectone') && ($("#report_filterSelect option:selected").val() != 'metakitchbytime_selectone')
                && ($("#report_filterSelect option:selected").val() != 'studysubjectvisit_selectone') && ($("#report_filterSelect option:selected").val() != 'subjectvisithistory_selectone')
                && ($("#report_filterSelect option:selected").val() != 'studystatuschange_selectone') && ($("#report_filterSelect option:selected").val() != 'crcavailability_selectone')
                && ($("#report_filterSelect option:selected").val() != 'resourcelevelofservice_selectone') && ($("#report_filterSelect option:selected").val() != 'nutritiontasks_selectone')
                && ($("#report_filterSelect option:selected").val() != 'visittemplatedata_selectone') && ($("#report_filterSelect option:selected").val() != 'visitdurationbyvisittype_selectone')
                && ($("#report_filterSelect option:selected").val() != 'bookedvisitservicelevelbyvisittype_selectone') && ($("#report_filterSelect option:selected").val() != 'bookedvisitservicelevelbyvisittype_level0')
                && ($("#report_filterSelect option:selected").val() != 'bookedvisitservicelevelbyvisittype_level1') && ($("#report_filterSelect option:selected").val() != 'bookedvisitservicelevelbyvisittype_level2')
                && ($("#report_filterSelect option:selected").val() != 'bookedvisitservicelevelbyvisittype_level3') && ($("#report_filterSelect option:selected").val() != 'bookedvisitservicelevelbyvisittype_level4')
                && ($("#report_filterSelect option:selected").val() != 'bookedvisitservicelevelbyvisittype_level5') && ($("#report_filterSelect option:selected").val() != 'bookedvisitservicelevelbyvisittype_level6')
                && ($("#report_filterSelect option:selected").val() != 'bookedvisitservicelevelbyvisittype_level7') && ($("#report_filterSelect option:selected").val() != 'visitterminatedpriortocompletion_selectone')
                && ($("#report_filterSelect option:selected").val() != 'daily_resource_crc_approved') && ($("#report_filterSelect option:selected").val() != 'deptandpi_crc_approved')
                && ($("#report_filterSelect option:selected").val() != 'levelofservice_crc_approved') && ($("#report_filterSelect option:selected").val() != 'metakitch_crc_approved')
                && ($("#report_filterSelect option:selected").val() != 'metakitchbytime_crc_approved') && ($("#report_filterSelect option:selected").val() != 'nutritiontasks_crc_approved')
                && ($("#report_filterSelect option:selected").val() != 'off_unit_crc_approved') && ($("#report_filterSelect option:selected").val() != 'studystatuschange_crc_approved')
                && ($("#report_filterSelect option:selected").val() != 'studyvisitlocation_crc_approved') && ($("#report_filterSelect option:selected").val() != 'subjectvisithistory_crc_approved')
                && ($("#report_filterSelect option:selected").val() != 'weeklypharm_crc_approved') && ($("#report_filterSelect option:selected").val() != 'daily_adm_crc_approved')
                && ($("#report_filterSelect option:selected").val() != 'billing_crc_approved')
                && ($("#report_filterSelect option:selected").val() != 'census_crc_approved') && ($("#report_filterSelect option:selected").val() != 'billingbyinvestigator_crc_approved')
                && ($("#report_filterSelect option:selected").val() != 'ancillaryonlybyprotocol_selectone') && ($("#report_filterSelect option:selected").val() != 'ancillaryonlybyprotocol_crc_approved')
                && ($("#report_filterSelect option:selected").val() != 'billingbyinvestigator_industry_initiated') && ($("#report_filterSelect option:selected").val() != 'billing_industry_initiated')
                && ($("#report_filterSelect option:selected").val() != 'billable_resources_crc_approved') && ($("#report_filterSelect option:selected").val() != 'billable_resources_industry_initiated')
                && ($("#report_filterSelect option:selected").val() != 'protonutritionist_selectone') && ($("#report_filterSelect option:selected").val() != 'off_unit_group_nursing')
                && ($("#report_filterSelect option:selected").val() != 'off_unit_group_room') && ($("#report_filterSelect option:selected").val() != 'off_unit_group_lab')
                && ($("#report_filterSelect option:selected").val() != 'off_unit_group_other') && ($("#report_filterSelect option:selected").val() != 'off_unit_group_nutrition')) {

                if (($("#report_filterSelect option:selected").val() != 'daily_overview_selectone') && ($("#overview_resource_type option:selected").val() != null)) {
                    search_string = $("#overview_resource_type option:selected").val();
                }
                else if (($("#report_filterSelect option:selected").val() != 'daily_overview_selectone') && ($("#overview_sublocation option:selected").val() != null)) {
                    search_string = $("#overview_sublocation option:selected").val();
                }

                if (search_string == null || search_string == '') {
                    util_showMainMessage("Please Enter the Filter Value.");
                    $("#report_filterSelect option:selected").val();
                    $("#report_filterText").val('');
                    return;
                }
            }
            else {
                $('#report_filterButton').attr("value", "Filter");
                $('#report_filterButton').css({display: "none"});
            }
    }
}
