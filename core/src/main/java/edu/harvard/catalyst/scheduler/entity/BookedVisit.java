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
import edu.harvard.catalyst.scheduler.util.SubjectDataEncryptor;

import javax.persistence.*;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "booked_visit")
public class BookedVisit extends BaseEntity implements Serializable, Cloneable {

    private static final long serialVersionUID = 1L;

    private Study study;
    private VisitTemplate visitTemplate;
    private String name;
    private String actionName;
    private VisitType visitType;
    private SubjectMrn subjectMrn;
    private AppointmentStatus appointmentStatus;
    private CancellationStatus cancelStatus;
    private AppointmentStatusReason appointmentStatusReason;
    private AppointmentStatusReason cancelStatusReason;
    private AppointmentStatusReason checkoutStatusReason;
    private Date scheduledStartTime;
    private Date scheduledEndTime;
    private Date checkInDate;
    private User checkInUser;
    private Date checkOutDate;
    private User checkOutUser;
    private Date cancelDate;
    private User cancelUser;
    private String comment;
    private String checkoutStatusReasonName;

    private int total;
    private int size;
    private int limit;
    private int start;
    private String sessionId;
    private String uniquekey;

    // transients, used prior to 2.9
    private Date scheduleDate;
    private String userdata;
    private String scheduledata;

    // columns, probably should replace the above transients
    private Date schedulingTime;
    private String schedulingFlavor;
    private User schedulingUser;

    private List<BookedResource> bookedResourceList;
    private Resource selectedRoom;

    private boolean varyDuration;
    private boolean ommittedActivities;

    private String rooms;
    private String errorMsg;

    private boolean rejectedVisit;

    @Deprecated
    public BookedVisit() {
        this(null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null,
                null, null, 0, 0, 0, 0, null, null, null, null, null, null, null, null);
    }

    public BookedVisit(final Study study, final VisitTemplate visitTemplate, final String name,
                       final VisitType visitType,
                        final SubjectMrn subjectMrn, final AppointmentStatus appointmentStatus,
                        final CancellationStatus cancelStatus, final AppointmentStatusReason appointmentStatusReason,
                        final AppointmentStatusReason cancelStatusReason, final AppointmentStatusReason checkoutStatusReason,
                        final Date scheduledStartTime, final Date scheduledEndTime, final Date checkInDate,
                        final Date checkOutDate, final Date cancelDate, final String comment,
                        final Date scheduleDate, final String checkoutStatusReasonName,
                        final int total, final int size, final int limit, final int start, final String sessionId,
                        final String uniquekey, final String userdata, final String scheduledata,
                        final List<BookedResource> bookedResourceList, final Resource selectedRoom,
                        final String rooms, final String errorMsg) {
        super(null);

        this.study = study;
        this.visitTemplate = visitTemplate;
        this.name = name;
        this.visitType = visitType;
        this.subjectMrn = subjectMrn;
        this.appointmentStatus = appointmentStatus;
        this.cancelStatus = cancelStatus;
        this.appointmentStatusReason = appointmentStatusReason;
        this.cancelStatusReason = cancelStatusReason;
        this.checkoutStatusReason = checkoutStatusReason;
        this.scheduledStartTime = scheduledStartTime;
        this.scheduledEndTime = scheduledEndTime;
        this.checkInDate = checkInDate;
        this.checkOutDate = checkOutDate;
        this.cancelDate = cancelDate;
        this.comment = comment;
        this.scheduleDate = scheduleDate;
        this.checkoutStatusReasonName = checkoutStatusReasonName;
        this.total = total;
        this.size = size;
        this.limit = limit;
        this.start = start;
        this.sessionId = sessionId;
        this.uniquekey = uniquekey;
        this.userdata = userdata;
        this.scheduledata = scheduledata;
        this.bookedResourceList = bookedResourceList;
        this.selectedRoom = selectedRoom;
        this.rooms = rooms;
        this.errorMsg = errorMsg;
    }

    @JoinColumn(name = "study", referencedColumnName = "id")
    @ManyToOne(optional = false)
    public Study getStudy() {
        return study;
    }

    public void setStudy(final Study study) {
        this.study = study;
    }

    @JoinColumn(name = "visit_template", referencedColumnName = "id")
    @ManyToOne(optional = false)
    public VisitTemplate getVisitTemplate() {
        return visitTemplate;
    }

    public void setVisitTemplate(final VisitTemplate visitTemplate) {
        this.visitTemplate = visitTemplate;
    }

    @Column(name = "name")
    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    @JoinColumn(name = "visit_type", referencedColumnName = "id")
    @ManyToOne(optional = false)
    @Basic(optional = false)
    public VisitType getVisitType() {
        return visitType;
    }
    public void setVisitType(final VisitType visitType) {
        this.visitType = visitType;
    }


    @JoinColumn(name = "subject_mrn", referencedColumnName = "id")
    @ManyToOne(optional = true)
    public SubjectMrn getSubjectMrn() {
        return subjectMrn;
    }

    public void setSubjectMrn(final SubjectMrn subjectMrn) {
        this.subjectMrn = subjectMrn;
    }

    @Transient
    public SubjectMrn getSubjectMrnDecrypted() {
        if (subjectMrn == null) {
            return null;
        }
        SubjectMrn encryptedSubjectMrn = new SubjectMrn(
                subjectMrn.getSubject(),
                subjectMrn.getMrn(),
                subjectMrn.getSite(),
                subjectMrn.getStatus(),
                subjectMrn.getId());
        SubjectMrn decryptedSubjectMrn = SubjectDataEncryptor.decryptSubjectMrnAndSubject(encryptedSubjectMrn);
        return decryptedSubjectMrn;
    }

    @JoinColumn(name = "appointment_status", referencedColumnName = "id")
    @ManyToOne(optional = false)
    public AppointmentStatus getAppointmentStatus() {
        return appointmentStatus;
    }

    public void setAppointmentStatus(final AppointmentStatus appointmentStatus) {
        this.appointmentStatus = appointmentStatus;
    }

    @JoinColumn(name = "appointment_status_reason", referencedColumnName = "id")
    @ManyToOne(optional = true)
    public AppointmentStatusReason getAppointmentStatusReason() {
        return appointmentStatusReason;
    }

    public void setAppointmentStatusReason(final AppointmentStatusReason appointmentStatusReason) {
        this.appointmentStatusReason = appointmentStatusReason;
    }

    @JoinColumn(name = "cancel_status_reason", referencedColumnName = "id")
    @ManyToOne(optional = true)
    public AppointmentStatusReason getCancelStatusReason() {
        return cancelStatusReason;
    }

    public void setCancelStatusReason(final AppointmentStatusReason cancelStatusReason) {
        this.cancelStatusReason = cancelStatusReason;
    }

    @JoinColumn(name = "checkout_status_reason", referencedColumnName = "id")
    @ManyToOne(optional = true)
    public AppointmentStatusReason getCheckoutStatusReason() {
        return checkoutStatusReason;
    }

    public void setCheckoutStatusReason(final AppointmentStatusReason checkoutStatusReason) {
        this.checkoutStatusReason = checkoutStatusReason;
    }

    @Column(name = "scheduled_start_time")
    @Basic(optional = false)
    public Date getScheduledStartTime() {
        return scheduledStartTime;
    }

    public void setScheduledStartTime(final Date scheduledStartTime) {
        this.scheduledStartTime = scheduledStartTime;
    }

    @Column(name = "scheduled_end_time")
    @Basic(optional = false)
    public Date getScheduledEndTime() {
        return scheduledEndTime;
    }

    public void setScheduledEndTime(final Date scheduledEndTime) {
        this.scheduledEndTime = scheduledEndTime;
    }

    @Column(name = "comment")
    public String getComment() {
        return comment;
    }

    public void setComment(final String comment) {
        this.comment = comment;
    }


    @Column(name = "scheduling_flavor")
    public String getSchedulingFlavor() {
        return schedulingFlavor;
    }

    public void setSchedulingFlavor(final String schedulingFlavor) {
        this.schedulingFlavor = schedulingFlavor;
    }

    @Column(name = "scheduling_time")
    public Date getSchedulingTime() {
        return schedulingTime;
    }

    public void setSchedulingTime(final Date schedulingTime) {
        this.schedulingTime = schedulingTime;
    }

    @JoinColumn(name = "scheduling_user", referencedColumnName = "id")
    @ManyToOne(optional = true)
    public User getSchedulingUser() {
        return schedulingUser;
    }

    public void setSchedulingUser(final User schedulingUser) {
        this.schedulingUser = schedulingUser;
    }

    @Column(name = "check_in_date")
    @Basic(optional = true)
    public Date getCheckInDate() {
        return checkInDate;
    }

    public void setCheckInDate(final Date checkInDate) {
        this.checkInDate = checkInDate;
    }

    @JoinColumn(name = "check_in_user", referencedColumnName = "id")
    @ManyToOne(optional = true)
    public User getCheckInUser() {
        return checkInUser;
    }

    public void setCheckInUser(final User checkInUser) {
        this.checkInUser = checkInUser;
    }

    @Column(name = "check_out_date")
    @Basic(optional = true)
    public Date getCheckOutDate() {
        return checkOutDate;
    }

    public void setCheckOutDate(final Date checkOutDate) {
        this.checkOutDate = checkOutDate;
    }

    @JoinColumn(name = "check_out_user", referencedColumnName = "id")
    @ManyToOne(optional = true)
    public User getCheckOutUser() {
        return checkOutUser;
    }

    public void setCheckOutUser(final User checkOutUser) {
        this.checkOutUser = checkOutUser;
    }

    @Column(name = "cancel_date")
    @Basic(optional = true)
    public Date getCancelDate() {
        return cancelDate;
    }

    public void setCancelDate(final Date cancelDate) {
        this.cancelDate = cancelDate;
    }

    @JoinColumn(name = "cancel_user", referencedColumnName = "id")
    @ManyToOne(optional = true)
    public User getCancelUser() {
        return cancelUser;
    }

    public void setCancelUser(final User cancelUser) {
        this.cancelUser = cancelUser;
    }

    @JoinColumn(name = "cancel_status", referencedColumnName = "id")
    @ManyToOne(optional = true)
    public CancellationStatus getCancelStatus() {
        return cancelStatus;
    }

    public void setCancelStatus(final CancellationStatus cancelStatus) {
        this.cancelStatus = cancelStatus;
    }
    
    @Column(name = "vary_duration")
    public boolean isVaryDuration() {
        return varyDuration;
    }

    public void setVaryDuration(boolean varyDuration) {
        this.varyDuration = varyDuration;
    }

    @Column(name = "ommitted_activities")
    public boolean isOmmittedActivities() {
        return ommittedActivities;
    }

    public void setOmmittedActivities(boolean ommittedActivities) {
        this.ommittedActivities = ommittedActivities;
    }

    @Transient
    public List<BookedResource> getBookedResourceList() {
        return bookedResourceList;
    }

    public void setBookedResourceList(final List<BookedResource> bookedResourceList) {
        this.bookedResourceList = bookedResourceList;
    }

    @Transient
    public String getActionName() {
        return actionName;
    }

    public void setActionName(String actionName) {
        this.actionName = actionName;
    }

    @Transient
    public Date getScheduleDate() {
        return scheduleDate;
    }

    public void setScheduleDate(final Date scheduleDate) {
        this.scheduleDate = scheduleDate;
    }

    @Transient
    public String getCheckoutStatusReasonName() {
        return checkoutStatusReasonName;
    }

    public void setCheckoutStatusReasonName(final String checkoutStatusReasonName) {
        this.checkoutStatusReasonName = checkoutStatusReasonName;
    }

    @Transient
    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(final String sessionId) {
        this.sessionId = sessionId;
    }

    @Transient
    public int getTotal() {
        return total;
    }

    public void setTotal(final int total) {
        this.total = total;
    }

    @Transient
    public int getSize() {
        return size;
    }

    public void setSize(final int size) {
        this.size = size;
    }

    @Transient
    public int getLimit() {
        return limit;
    }

    public void setLimit(final int limit) {
        this.limit = limit;
    }

    @Transient
    public int getStart() {
        return start;
    }

    public void setStart(final int start) {
        this.start = start;
    }

    @Transient
    public String getUniquekey() {
        return uniquekey;
    }

    public void setUniquekey(final String uniquekey) {
        this.uniquekey = uniquekey;
    }

    @Transient
    public String getUserdata() {
        return userdata;
    }

    public void setUserdata(final String userdata) {
        this.userdata = userdata;
    }

    @Transient
    public String getScheduledata() {
        return scheduledata;
    }

    public void setScheduledata(final String string) {
        scheduledata = string;
    }

    @Transient
    public Resource getSelectedRoom() {
        return selectedRoom;
    }

    public void setSelectedRoom(final Resource selectedRoom) {
        this.selectedRoom = selectedRoom;
    }
     
    @Transient
    public String getRooms() {
        return rooms;
    }

    public void setRooms(String rooms) {
        this.rooms = rooms;
    }
    
    @Transient
    public String getErrorMsg() {
        return errorMsg;
    }

    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }

    @Transient
    public boolean isRejectedVisit() {
        return rejectedVisit;
    }

    public void setRejectedVisit(boolean rejectedVisit) {
        this.rejectedVisit = rejectedVisit;
    }

    public BookedVisit cloneBookedVisit() {
        BookedVisit result = null;

        try {
            result = (BookedVisit) super.clone();
        }
        catch (CloneNotSupportedException cnse) {
            SchedulerRuntimeException.logAndThrow("Couldn't clone BookedVisit '" + getId() + "'", cnse);
        }
        return result;
    }

    @Override
    public String toString() {
        return "BookedVisit [id=" + id + ", getId()=" + getId() + "]";
    }
}
