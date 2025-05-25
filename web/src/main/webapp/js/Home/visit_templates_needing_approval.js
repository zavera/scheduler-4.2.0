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
var VisitTemplatesNeedingApproval = {};
VisitTemplatesNeedingApproval.loadTemplatesToApproveData = function(currentPage) {
    if (initial_load == true) {
        createPageDropDownList();
        initial_load = false;
    }
    if (max_results == undefined) {
        max_results = 5;
    }

    currentPage = isNaN(currentPage) ? 1 : currentPage;
    $.getJSON("rest/study/getTemplatesToApprove?page=" + currentPage + "&maxResults=" + (max_results != undefined ? max_results : 5) + "&orderBy=" + (orderBy != undefined ? orderBy : "ASC") + "&sortBy=" + (sortBy != undefined ? sortBy : sortByStudy), function (data) {
        var iteration = 0;
        var editColumns = " ";

        var sort_header;
        var study_header1 = 'sortable';
        var pi_header2 = 'sortable';
        var visit_header3 = 'sortable';
        var visitType_header4 = 'sortable';
        var sublocation_header5 = 'sortable';

        if (orderBy == 'ASC' || orderBy == undefined) {
            sort_header = 'sorting_asc';
        }
        else if (orderBy == 'DESC') {
            sort_header = 'sorting_desc';
        }

        if (sortBy == undefined || sortBy == sortByStudy) {
            study_header1 = sort_header;
        } else if (sortBy == sortByPI) {
            pi_header2 = sort_header;
        } else if (sortBy == sortByVisit) {
            visit_header3 = sort_header;
        } else if (sortBy == sortByTemplateVisitType) {
            visitType_header4 = sort_header;
        } else if (sortBy == sortByTemplateSublocation) {
            sublocation_header5 = sort_header;
        }

        var out =
            " <table id='visitTemplatesApproval'>" +
            "  <tr>" +
            "   <td class= " + study_header1 + " onclick='sortingFunction(sortByStudy)'><strong>Study ID</strong></td> " +
            "   <td class= " + pi_header2 + " onclick='sortingFunction(sortByPI)'><strong>PI Name</strong></td> " +
            "   <td class= " + visit_header3 + " onclick='sortingFunction(sortByVisit)'><strong>Visit Name</strong></td>" +
            "   <td class= " + visitType_header4 + " onclick='sortingFunction(sortByTemplateVisitType)'><strong>Visit Type</strong></td>" +
            "   <td class= " + sublocation_header5 + " onclick='sortingFunction(sortByTemplateSublocation)'><strong>Sub-location</strong></td>" +
            "   <td class='not_sortable'><strong>Last Updated</strong></td>" +
            "   <td class='not_sortable'><strong></strong></td>" +
            "   <td class='not_sortable'><strong></strong></td>" +
            "  </tr> ";

        $.each(data, function (key, val) {
            totalData = val.totalCount;
            out += "<tr>"
            out += "   <td>" + val.localId + "</td>" +
                "          <td>" + val.piName + "</td>" +
                "          <td>" + val.visitName + "</td>" +
                "          <td>" + val.visitTypeName + "</td>" +
                "          <td>" + val.sublocationName + "</td>" +
                "          <td>" + showDate(val.lastUpdateTime) + "</td>" +
                " <td> <a href='javascript:VisitTemplatesNeedingApproval.home_showVisitTab(" + val.id + "," + val.studyId + "," + 0 + ")'> View Detail </a> </td> " +
                " <td> <a href='javascript:VisitTemplatesNeedingApproval.home_showVisitTab(" + val.id + "," + val.studyId + "," + 1 + ")'> Approve </a> </td> " +
                " </tr>";
            iteration++;
        });

        out += " </table>";

        setupTable(out, $('#homeTemplatesApprovalData'), currentPage, totalData);
    });
};

VisitTemplatesNeedingApproval.home_showVisitTab = function(visitId, studyId, tabId) {
    $.ajax({
        url: "rest/study/getStudyData?study=" + studyId,
        dataType: 'json',
        success: function (data) {
            sessionStorage.setItem("studyData", JSON.stringify(data));
            sessionStorage.setItem("mode", JSON.stringify('view'));
            $.getJSON("rest/study/getVisitTemplateData?visit=" + visitId, function (data) {
                sessionStorage.setItem("visitData", JSON.stringify(data));
                sessionStorage.setItem("selectedTab", tabId);
                sessionStorage.setItem("finalApproval", data.approved);
                sessionStorage.setItem("mode", JSON.stringify('view'));
                var navigatePage = "visit_approval.html";
                if (tabId == 0) {
                    navigatePage = "visit_detail.html";
                }
                window.location.href = navigatePage;
            });
        },
        error: function (xhr, status, error) {
            // TODO: check status && error
        }
    });
};

VisitTemplatesNeedingApproval.getTemplatesToApprove = function() {
    parent.location.hash = "TemplateApprovalList";
    initial_load = true;
    resetUI();
    currentPage = 1;
    VisitTemplatesNeedingApproval.loadTemplatesToApproveData(currentPage);
};
