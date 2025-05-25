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
import edu.harvard.catalyst.scheduler.entity.AppointmentStatus;
import edu.harvard.catalyst.scheduler.entity.BookedVisit;
import edu.harvard.catalyst.scheduler.entity.SubjectMrn;

import static edu.harvard.catalyst.scheduler.core.Statics.*;
import static edu.harvard.catalyst.scheduler.util.DateUtility.*;

/**
 * User: ankit
 * Date: 4/21/14
 * Time: 2:23 PM
 */
public class BookedVisitDetailResponse {

    private Integer id;

    private String studyName;
    private int visitDuration;
    private boolean isScheduled;
    private boolean isCheckedIn;
    private boolean isCheckedOut;
    private boolean isCancelled;
    private boolean isHold;
    private String appointmentStatus;
    private String scheduledStartTime;
    private String scheduledEndTime;
    private String checkInDate;
    private String checkOutDate;
    private String cancelDate;
    private String checkOutReason;
    private String cancelReason;
    private String localId;
    private String subjectName;
    private String catID;
    private String subjectMRN;
    private String irb;
    private String genderName;
    private String visitName;
    private String birthDate;
    private String comment;
    private String visitTypeName;

    public BookedVisitDetailResponse(BookedVisit bookedVisit) {
        if (bookedVisit == null) {
            SchedulerRuntimeException.logAndThrow("BookedVisit parameter should be non-null");
        }
        SubjectMrn subjectMrn = bookedVisit.getSubjectMrnDecrypted();

        this.id = bookedVisit.getId();
        AppointmentStatus appointmentStatus = bookedVisit.getAppointmentStatus();
        this.appointmentStatus = appointmentStatus.getName();
        this.isScheduled = appointmentStatus.getIsScheduled();
        this.isCheckedIn = appointmentStatus.getIsCheckedIn();
        this.isCheckedOut = appointmentStatus.getIsCheckedOut();
        this.isCancelled = appointmentStatus.getIsCancelled();
        this.isHold = appointmentStatus.getIsHold();

        this.cancelDate = format(dateTime(), bookedVisit.getCancelDate());
        if (bookedVisit.getCancelStatusReason() != null) {
            this.cancelReason = bookedVisit.getCancelStatusReason().getName();                
        }
        this.catID = bookedVisit.getStudy().getCatalystId();
        this.checkInDate = format(dateTime(), bookedVisit.getCheckInDate());
        this.checkOutDate = format(dateTime(), bookedVisit.getCheckOutDate());
        if (bookedVisit.getCheckoutStatusReason() != null) {
            this.checkOutReason = bookedVisit.getCheckoutStatusReason().getName();             
        }
        this.comment = bookedVisit.getComment();
        this.irb = bookedVisit.getStudy().getIrb();
        this.localId = bookedVisit.getStudy().getLocalId();
        this.scheduledEndTime = format(dateTime(), bookedVisit.getScheduledEndTime());
        this.scheduledStartTime = format(dateTime(), bookedVisit.getScheduledStartTime());
        this.studyName = bookedVisit.getStudy().getName();
        this.subjectName = NO_SUBJECT_ASSIGNED;
        this.subjectMRN = NA;
        this.birthDate = NA;
        this.genderName = NA;

        if (subjectMrn != null) {
            this.subjectName = subjectMrn.getSubject().getFirstName() + " " + subjectMrn.getSubject().getLastName();
            this.subjectMRN = subjectMrn.getMrn();
            this.birthDate = format(monthDayYear(), subjectMrn.getSubject().getBirthdate());
            this.genderName = subjectMrn.getSubject().getGender() == null ? NA
                    : subjectMrn.getSubject().getGender().getName();
        }
        this.visitDuration = bookedVisit.getVisitTemplate().getDuration();
        this.visitName = bookedVisit.getVisitTemplate().getName();
        this.visitTypeName = bookedVisit.getVisitTemplate().getVisitType().getName();
    }

    // getters are needed for testability

    public Integer getId() {
        return id;
    }

    public String getStudyName() {
        return studyName;
    }

    public int getVisitDuration() {
        return visitDuration;
    }

    public boolean getIsScheduled() {
        return isScheduled;
    }

    public boolean getIsCheckedIn() {
        return isCheckedIn;
    }

    public boolean getIsCheckedOut() {
        return isCheckedOut;
    }

    public boolean getIsCancelled() {
        return isCancelled;
    }

    public boolean getIsHold() {
        return isHold;
    }

    public String getAppointmentStatus() {
        return appointmentStatus;
    }

    public String getScheduledStartTime() {
        return scheduledStartTime;
    }

    public String getScheduledEndTime() {
        return scheduledEndTime;
    }

    public String getCheckInDate() {
        return checkInDate;
    }

    public String getCheckOutDate() {
        return checkOutDate;
    }

    public String getCancelDate() {
        return cancelDate;
    }

    public String getCheckOutReason() {
        return checkOutReason;
    }

    public String getCancelReason() {
        return cancelReason;
    }

    public String getLocalId() {
        return localId;
    }

    public String getSubjectName() {
        return subjectName;
    }

    public String getCatID() {
        return catID;
    }

    public String getSubjectMRN() {
        return subjectMRN;
    }

    public String getIrb() {
        return irb;
    }

    public String getGenderName() {
        return genderName;
    }

    public String getVisitName() {
        return visitName;
    }

    public String getBirthDate() {
        return birthDate;
    }

    public String getComment() {
        return comment;
    }

    public String getVisitTypeName() {
        return visitTypeName;
    }
}
