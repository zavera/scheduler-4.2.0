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
package edu.harvard.catalyst.scheduler.entity;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name = "template_approval_history")
public class TemplateApprovalHistory extends BaseEntity implements Serializable {

    private static final long serialVersionUID = 8647990931383698371L;

    private VisitTemplate visitTemplate;
    private User user;
    private boolean approved;
    private String comment;
    private Date statusChangeTime;
    private Date visitCreatedDate;
    private String userRole;
    private String userTitle;
    private String userLastName;
    private String userFirstName;
    private String visitName;

	public TemplateApprovalHistory() {
        super(null);
    }
    
	public TemplateApprovalHistory(
					User user,
					String userLastName,
					String userFirstName,
					String userRole,
					String userTitle,
					boolean approved,
					String comment,
					Date statusChangeTime,
					VisitTemplate visitTemplate,
					String visitName,
					Date visitCreatedDate) {

        super(null);

        this.user = user;
    	this.userLastName = userLastName;
    	this.userFirstName = userFirstName;
    	this.userRole = userRole;
    	this.userTitle = userTitle;
    	this.approved = approved;
    	this.comment = comment;
    	this.statusChangeTime = statusChangeTime;
    	this.visitTemplate = visitTemplate;
    	this.visitName = visitName;
    	this.visitCreatedDate = visitCreatedDate;
    }


    @JoinColumn(name = "visit_template", referencedColumnName = "id")
    @ManyToOne(optional = false)
    public VisitTemplate getVisitTemplate() {
        return visitTemplate;
    }

    public void setVisitTemplate(VisitTemplate visitTemplate) {
        this.visitTemplate = visitTemplate;
    }

    @JoinColumn(name = "user", referencedColumnName = "id")
    @ManyToOne(optional = false)
    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    @Column(name = "approved")
    public boolean isApproved() {
        return approved;
    }

    public void setApproved(boolean approved) {
        this.approved = approved;
    }

    @Column(name = "comment")
    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    @Column(name = "status_change_time")
    public Date getStatusChangeTime() {
        return statusChangeTime;
    }

    public void setStatusChangeTime(Date statusChangeTime) {
        this.statusChangeTime = statusChangeTime;
    }

    @Transient
    public String getUserRole() {
        return userRole;
    }

    public void setUserRole(String userRole) {
        this.userRole = userRole;
    }

    @Transient
    public String getUserTitle() {
        return userTitle;
    }

    public void setUserTitle(String userTitle) {
        this.userTitle = userTitle;
    }

    @Transient
    public String getUserLastName() {
        return userLastName;
    }

    public void setUserLastName(String userLastName) {
        this.userLastName = userLastName;
    }

    @Transient
    public String getUserFirstName() {
        return userFirstName;
    }

    public void setUserFirstName(String userFirstName) {
        this.userFirstName = userFirstName;
    }

    @Transient
    public String getVisitName() {
        return visitName;
    }

    public void setVisitName(String visitName) {
        this.visitName = visitName;
    }

    @Transient
    public Date getVisitCreatedDate() {
        return visitCreatedDate;
    }

    public void setVisitCreatedDate(Date visitCreatedDate) {
        this.visitCreatedDate = visitCreatedDate;
    }

    @Override
    public String toString() {
        return "TemplateApprovalHistory [id=" + id + ", getId()=" + getId() + "]";
    }

}
