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
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.harvard.catalyst.scheduler.dto;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import edu.harvard.catalyst.scheduler.entity.BookedResource;

/**
 * 
 * @author smitha
 */
public class VisitSpecsDTO extends BooleanResultDTO implements Serializable {

    private int id;
    private int resource;                               // set via Gson
    private int study;                                  // set via Gson
    private int subjectMrnId;
    private long startDate;
    private long endDate;
    private int visit;
    private int bookedvisit;
    private int appointmentStatusReason;
    private int cancelStatusReason;
    private int overrideReason;
    private int roomSelected;                           // set via Gson
    private int checkoutStatusReason;

    private String comment;
    private long checkInDate;
    private long checkOutDate;
    private long cancelDate;

    private String doubleRoomMessage;
    private String alternateResourceUsed;

    private List<Integer> selectedAnnotations;          // set via Gson
    private List<String> selectedAnnotationsComment;    // set via Gson
    private List<Integer> selectedAnnotationsQuantity;  // set via Gson

    private boolean ommittedActivities;
    private boolean varyDuration;
    private boolean billable;                           // set via Gson

    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }

    // set via Gson
    public int getStudy() {
        return study;
    }

    public int getBookedvisit() {
        return bookedvisit;
    }
    public void setBookedvisit(int bookedvisit) {
        this.bookedvisit = bookedvisit;
    }

    public int getSubjectMrnId() {
        return subjectMrnId;
    }
    public void setSubjectMrnId(int subjectMrnId) {
        this.subjectMrnId = subjectMrnId;
    }

    public long getStartDate() {
        return startDate;
    }
    public void setStartDate(long startDate) {
        this.startDate = startDate;
    }

    public long getEndDate() {
        return endDate;
    }
    public void setEndDate(long endDate) {
        this.endDate = endDate;
    }

    public int getVisit() {
        return visit;
    }   
    public void setVisit(int visit) {
        this.visit = visit;
    }

    public String getComment() {
        return comment;
    }
    public void setComment(String comment) {
        this.comment = comment;
    }

    public int getAppointmentStatusReason() {
        return appointmentStatusReason;
    }
    public void setAppointmentStatusReason(int appointmentStatusReason) {
        this.appointmentStatusReason = appointmentStatusReason;
    }

    public int getCancelStatusReason() {
        return cancelStatusReason;
    }
    public void setCancelStatusReason(int cancelStatusReason) {
        this.cancelStatusReason = cancelStatusReason;
    }

    public int getCheckoutStatusReason() {
        return checkoutStatusReason;
    }
    public void setCheckoutStatusReason(int checkoutStatusReason) {
        this.checkoutStatusReason = checkoutStatusReason;
    }

    public long getCheckInDate() {
        return checkInDate;
    }
    public void setCheckInDate(long checkInDate) {
        this.checkInDate = checkInDate;
    }

    public long getCheckOutDate() {
        return checkOutDate;
    }
    public void setCheckOutDate(long checkOutDate) {
        this.checkOutDate = checkOutDate;
    }

    public long getCancelDate() {
        return cancelDate;
    }
    public void setCancelDate(long cancelDate) {
        this.cancelDate = cancelDate;
    }

    public int getOverrideReason() {
        return overrideReason;
    }
    public void setOverrideReason(int overrideReason) {
        this.overrideReason = overrideReason;
    }

    // set via Gson
    public int getResource() {
        return resource;
    }

    public String getDoubleRoomMessage() {
        return doubleRoomMessage;
    }
    public void setDoubleRoomMessage(String doubleRoomMessage) {
        this.doubleRoomMessage = doubleRoomMessage;
    }

    // set via Gson
    public int getRoomSelected() {
        return roomSelected;
    }

    // needed for testability
    public void setRoomSelected(int r) {
        this.roomSelected = r;
    }

    public String getAlternateResourceUsed() {
        return alternateResourceUsed;
    }
    public void setAlternateResourceUsed(String alternateResourceUsed) {
        this.alternateResourceUsed = alternateResourceUsed;
    }

    // set via Gson
    public List<Integer> getSelectedAnnotations() {
        return selectedAnnotations;
    }
    public List<String> getSelectedAnnotationsComment() {
        return selectedAnnotationsComment;
    }
    public List<Integer> getSelectedAnnotationsQuantity() {
        return selectedAnnotationsQuantity;
    }

    public boolean isOmmittedActivities() {
        return ommittedActivities;
    }
    public void setOmmittedActivities(boolean ommittedActivities) {
        this.ommittedActivities = ommittedActivities;
    }

    public boolean isVaryDuration() {
        return varyDuration;
    }
    public void setVaryDuration(boolean varyDuration) {
        this.varyDuration = varyDuration;
    }

    // set via Gson
    public boolean isBillable() {
        return billable;
    }
}
