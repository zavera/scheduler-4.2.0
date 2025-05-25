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

import edu.harvard.catalyst.scheduler.core.Statics;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name = "activity_log")

public class ActivityLog  extends BaseEntity  implements Serializable {

    private static final long serialVersionUID = 1L;
    private User performingUser;
    private String actionPerformed;
    private Date date;
    private String ipAddress;
    private User affectedUser;
    private Resource affectedResource;
    private Sublocation affectedSublocation;
    private Subject affectedSubject;
    private SubjectMrn affectedSubjectMrn;
    private Study affectedStudy;
    private VisitTemplate affectedVisit;
    private String changesDetailRequiredField;
    private String changesDetail;
    private AppointmentOverrideReason appointmentOverrideReason;
    private BookedVisit bookedVisit;

    public ActivityLog() {
        super(null);
    }

    @Column(name = "action_performed")
    public String getActionPerformed() {
        return actionPerformed;
    }
    public void setActionPerformed(String action) {
        if (action.length() > Statics.MAX_255) {
            action = action.substring(0, Statics.MAX_255 - 3) + "...";
        }

        this.actionPerformed = action;
    }

    @Column(name = "date")
    public Date getDate() {
        return date;
    }
    public void setDate(Date date) {
        this.date = date;
    }

    @JoinColumn(name = "booked_visit", referencedColumnName = "id")
    @ManyToOne(optional = true)
    public BookedVisit getBookedVisit() {
        return bookedVisit;
    }

    public void setBookedVisit(BookedVisit bookedVisit) {
        this.bookedVisit = bookedVisit;
    }


    @Column(name = "ip_address")
    public String getIpAddress() {
        return ipAddress;
    }
    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    @Column(name = "changes_detail")
    public String getChangesDetail() {
        return changesDetail;
    }

    public void setChangesDetail(String changesDetail) {
        this.changesDetail = changesDetail;
    }

    @Column(name = "changes_detail_required_field")
    public String getChangesDetailRequiredField() {
        return changesDetailRequiredField;
    }

    public void setChangesDetailRequiredField(String changesDetailRequiredField) {
        this.changesDetailRequiredField = changesDetailRequiredField;
    }

    @JoinColumn(name = "appointment_override_reason", referencedColumnName = "id")
    @ManyToOne(optional = true)
    public AppointmentOverrideReason getAppointmentOverrideReason() {
        return appointmentOverrideReason;
    }

    public void setAppointmentOverrideReason(
            AppointmentOverrideReason appointmentOverrideReason) {
        this.appointmentOverrideReason = appointmentOverrideReason;
    }

    @JoinColumn(name = "performing_user", referencedColumnName = "id")
    @ManyToOne(optional = true)
    public User getPerformingUser() {
        return performingUser;
    }
    public void setPerformingUser(User performingUser) {
        this.performingUser = performingUser;
    }

    @JoinColumn(name = "affected_user", referencedColumnName = "id")
    @ManyToOne(optional = true)
    public User getAffectedUser() {
        return affectedUser;
    }
    public void setAffectedUser(User affectedUser) {
        this.affectedUser = affectedUser;
    }

    @JoinColumn(name = "affected_resource", referencedColumnName = "id")
    @ManyToOne(optional = true)
    public Resource getAffectedResource() {
        return affectedResource;
    }

    public void setAffectedResource(Resource affectedResource) {
        this.affectedResource = affectedResource;
    }

    @JoinColumn(name = "affected_sublocation", referencedColumnName = "id")
    @ManyToOne(optional = true)
    public Sublocation getAffectedSublocation() {
        return affectedSublocation;
    }

    public void setAffectedSublocation(Sublocation affectedSublocation) {
        this.affectedSublocation = affectedSublocation;
    }

    @JoinColumn(name = "affected_study", referencedColumnName = "id")
    @ManyToOne(optional = true)
    public Study getAffectedStudy() {
        return affectedStudy;
    }
    public void setAffectedStudy(Study affectedStudy) {
        this.affectedStudy = affectedStudy;
    }

    @JoinColumn(name = "affected_visit", referencedColumnName = "id")
    @ManyToOne(optional = true)
    public VisitTemplate getAffectedVisit() {
        return affectedVisit;
    }
    public void setAffectedVisit(VisitTemplate affectedVisit) {
        this.affectedVisit = affectedVisit;
    }

    @JoinColumn(name = "affected_subject", referencedColumnName = "id")
    @ManyToOne(optional = true)
    public Subject getAffectedSubject() {
        return affectedSubject;
    }
    public void setAffectedSubject(Subject affectedSubject) {
        this.affectedSubject = affectedSubject;
    }

    @JoinColumn(name ="affected_subject_mrn", referencedColumnName = "id")
    @ManyToOne(optional = true)
    public SubjectMrn getAffectedSubjectMrn() {return affectedSubjectMrn;}
    public void setAffectedSubjectMrn(SubjectMrn affectedSubjectMrn) {
        this.affectedSubjectMrn = affectedSubjectMrn;
    }

    @Override
    public String toString() {
        return "ActivityLog [id=" + id + ", getId()=" + getId() + "]";
    }
}
