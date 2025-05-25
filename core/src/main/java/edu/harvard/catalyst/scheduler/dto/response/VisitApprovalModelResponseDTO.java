/**
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
package edu.harvard.catalyst.scheduler.dto.response;

import edu.harvard.catalyst.scheduler.core.SchedulerRuntimeException;
import edu.harvard.catalyst.scheduler.entity.TemplateApprovalHistory;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: carl
 * Date: 2/28/14
 * Time: 1:02 PM
 */
public class VisitApprovalModelResponseDTO {
    private List<VisitApproval> templateApprovals;

    VisitApprovalModelResponseDTO(List<VisitApproval> resourceList) {
        this.templateApprovals = resourceList;
    }

    static public class VisitApproval {
        private Integer id;

        private String userLastName;
        private String userFirstName;
        private Date statusChangeTime;
        private String comment;
        private Date visitCreatedDate;
        private String userRole;
        private String userTitle;
        private String approved;
        private String finalApproval;
        
        private Long totalCount;

        VisitApproval(TemplateApprovalHistory templateApprovalHistory, Long total) {
        if (templateApprovalHistory == null) {
            SchedulerRuntimeException.logAndThrow("TemplateApprovalHistory parameter should be non-null");
        }

        /// ok, happy with basic invariants...

        this.id = templateApprovalHistory.getId();

        this.userLastName = templateApprovalHistory.getUserLastName();
        this.userFirstName = templateApprovalHistory.getUserFirstName();
        this.statusChangeTime = templateApprovalHistory.getStatusChangeTime();
        this.comment = templateApprovalHistory.getComment();
        this.visitCreatedDate = templateApprovalHistory.getVisitCreatedDate();
        this.userRole = templateApprovalHistory.getUserRole();
        this.userTitle = templateApprovalHistory.getUserTitle();
        this.finalApproval = templateApprovalHistory.getVisitTemplate().getApproved() ? "Approved" : "Not Approved";
        this.approved = templateApprovalHistory.isApproved() ? "Approved" : "Not Approved";
        this.totalCount = total;
        }

        public Integer getId() {
            return id;
        }

        public String getUserLastName() {
            return userLastName;
        }

        public String getUserFirstName() {
            return userFirstName;
        }

        public Date getStatusChangeTime() {
            return statusChangeTime;
        }

        public String getComment() {
            return comment;
        }

        public Date getVisitCreatedDate() {
            return visitCreatedDate;
        }

        public String getUserRole() {
            return userRole;
        }

        public String getUserTitle() {
            return userTitle;
        }

        public String getApproved() {
            return approved;
        }

        public String getFinalApproval() {
            return finalApproval;
        }

        public Long getTotalCount() {
            return totalCount;
        }

    }

    public static List<VisitApproval> fromTemplateApprovalHistoryList(List<TemplateApprovalHistory> templateApprovalHistorys, Long total) {
        List<VisitApproval> resourceList = new ArrayList<VisitApproval>();

        for (TemplateApprovalHistory templateApprovalHistory : templateApprovalHistorys) {
            VisitApproval visitApproval = new VisitApproval(templateApprovalHistory, total);
            resourceList.add(visitApproval);
        }
        return resourceList;
    }

}
