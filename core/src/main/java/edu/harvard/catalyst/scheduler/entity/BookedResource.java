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

import edu.harvard.catalyst.scheduler.core.SchedulerRuntimeException;
import edu.harvard.catalyst.scheduler.util.DateUtility;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name = "booked_resource")
public class BookedResource extends BaseEntity implements Serializable, Cloneable {
    private static final long serialVersionUID = 1L;
    private Integer duration;
    private Date scheduledStartTime;
    private Date scheduledEndTime;
    private BookedVisit bookedVisit;
    private TemplateResource templateResource;
    private Resource resource;
    private String conflictedTime;
    private String visitName;
    private String subjectName;
    private String localId;
    private Date visitStartTime;
    private Date visitEndTime;
    private String annotations;
    private Boolean billable;
    private String rejectedResourceMessage;
    private String available;

    @Deprecated
    public BookedResource() {
        this(null, null, null, null, null, null, null, null, null, null, null, null, null);
    }

    public BookedResource(final Integer duration, final Date scheduledStartTime, final Date scheduledEndTime, final BookedVisit bookedVisit, final TemplateResource templateResource, final Resource resource, final String conflictedTime, final String visitName, final String subjectName, final String localId, final Date visitStartTime, final Date visitEndTime, final Boolean billable) {
        super(null);

        this.duration = duration;
        this.scheduledStartTime = scheduledStartTime;
        this.scheduledEndTime = scheduledEndTime;
        this.bookedVisit = bookedVisit;
        this.templateResource = templateResource;
        this.resource = resource;
        this.conflictedTime = conflictedTime;
        this.visitName = visitName;
        this.subjectName = subjectName;
        this.localId = localId;
        this.visitStartTime = visitStartTime;
        this.visitEndTime = visitEndTime;
        this.billable = billable;
    }

    @JoinColumn(name = "booked_visit", referencedColumnName = "id")
    @ManyToOne(optional = false)
    public BookedVisit getBookedVisit() {
        return bookedVisit;
    }

    public void setBookedVisit(final BookedVisit bookedVisit) {
        this.bookedVisit = bookedVisit;
    }

    @Column(name = "duration")
    public Integer getDuration() {
        return duration;
    }

    public void setDuration(final Integer duration) {
        this.duration = duration;
    }

    @Column(name = "scheduled_start_time")
    public Date getScheduledStartTime() {
        return scheduledStartTime;
    }

    public void setScheduledStartTime(final Date scheduledStartTime) {
        this.scheduledStartTime = scheduledStartTime;
    }

    @Column(name = "scheduled_end_time")
    public Date getScheduledEndTime() {
        return scheduledEndTime;
    }

    public void setScheduledEndTime(final Date scheduledEndTime) {
        this.scheduledEndTime = scheduledEndTime;
    }

    @Column(name = "billable")
    public Boolean getBillable() {
        return billable;
    }

    public void setBillable(Boolean billable) {
        this.billable = billable;
    }

    @JoinColumn(name = "template_resource", referencedColumnName = "id")
    @ManyToOne(optional = true)
    public TemplateResource getTemplateResource() {
        return templateResource;
    }

    public void setTemplateResource(final TemplateResource templateResource) {
        this.templateResource = templateResource;
    }

    @JoinColumn(name = "resource", referencedColumnName = "id")
    @ManyToOne(optional = true)
    public Resource getResource() {
        return resource;
    }

    public void setResource(final Resource resource) {
        this.resource = resource;
    }

    @Transient
    public String getConflictedTime() {
        return conflictedTime;
    }

    public void setConflictedTime(final String conflictedTime) {
        this.conflictedTime = conflictedTime;
    }

    @Transient
    public String getSubjectName() {
        return subjectName;
    }

    public void setSubjectName(final String subjectName) {
        this.subjectName = subjectName;
    }

    @Transient
    public String getVisitName() {
        return visitName;
    }

    public void setVisitName(final String visitName) {
        this.visitName = visitName;
    }

    @Transient
    public String getLocalId() {
        return localId;
    }

    public void setLocalId(final String localId) {
        this.localId = localId;
    }

    @Transient
    public Date getVisitStartTime() {
        return visitStartTime;
    }

    public void setVisitStartTime(final Date visitStartTime) {
        this.visitStartTime = visitStartTime;
    }

    @Transient
    public Date getVisitEndTime() {
        return visitEndTime;
    }

    public void setVisitEndTime(final Date visitEndTime) {
        this.visitEndTime = visitEndTime;
    }
        
    @Transient
    public String getAnnotations() {
        return annotations;
    }

    public void setAnnotations(String annotations) {
        this.annotations = annotations;
    }

    public BookedResource cloneBookedResource() {
        BookedResource clone = null;
        try {
            clone = (BookedResource) super.clone();
        }
        catch (CloneNotSupportedException cnse) {
            SchedulerRuntimeException.logAndThrow("Couldn't clone BookedResource '" + getId() + "'", cnse);
        }

        return clone;
    }

    @Transient
    public String getRejectedResourceMessage() {
        return rejectedResourceMessage;
    }

    public void setRejectedResourceMessage(String rejectedResourceMessage) {
        this.rejectedResourceMessage = rejectedResourceMessage;
    }

    @Transient
    public String getAvailable() {
        return available;
    }

    public void setAvailable(String available) {
        this.available = available;
    }

    @Override
    public String toString() {
        return "BookedResource [id=" + id + ", getId()=" + getId() + "]";
    }

    public boolean overlapsWith(BookedResource other) {

        Date thisStartTime = this.getScheduledStartTime();
        Date thisEndTime = this.getScheduledEndTime();
        Date otherStartTime = other.getScheduledStartTime();
        Date otherEndTime = other.getScheduledEndTime();

        return DateUtility.overlap(thisStartTime, thisEndTime, otherStartTime, otherEndTime);

    }

}
