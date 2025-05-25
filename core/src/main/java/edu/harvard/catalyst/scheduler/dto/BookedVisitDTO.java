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
package edu.harvard.catalyst.scheduler.dto;

import java.util.Date;

public class BookedVisitDTO extends BooleanResultDTO {
    
    private int id;
    private int study;
    private int visitTemplate;
    private String name;
    private int appointmentStatus;
    private int cancelStatus;
    private int appointmentStatusReason;
    private int cancelStatusReason;
    private int checkoutStatusReason;
    private Date scheduledStartTime;
    private Date scheduledEndTime;
    private Date checkInDate;
    private Date checkOutDate;
    private Date cancelDate;
    private String comment;
    private int uniqueid;
    private Date scheduleDate;
    private String cancelStatusReasonName;
    private String checkoutStatusReasonName;

    private int total;
    private int size;
    private int limit;
    private int start;
    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }
    public int getStudy() {
        return study;
    }
    public void setStudy(int study) {
        this.study = study;
    }
    public int getVisitTemplate() {
        return visitTemplate;
    }
    public void setVisitTemplate(int visitTemplate) {
        this.visitTemplate = visitTemplate;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public int getAppointmentStatus() {
        return appointmentStatus;
    }
    public void setAppointmentStatus(int appointmentStatus) {
        this.appointmentStatus = appointmentStatus;
    }
    public int getCancelStatus() {
        return cancelStatus;
    }
    public void setCancelStatus(int cancelStatus) {
        this.cancelStatus = cancelStatus;
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
    public Date getScheduledStartTime() {
        return scheduledStartTime;
    }
    public void setScheduledStartTime(Date scheduledStartTime) {
        this.scheduledStartTime = scheduledStartTime;
    }
    public Date getScheduledEndTime() {
        return scheduledEndTime;
    }
    public void setScheduledEndTime(Date scheduledEndTime) {
        this.scheduledEndTime = scheduledEndTime;
    }
    public Date getCheckInDate() {
        return checkInDate;
    }
    public void setCheckInDate(Date checkInDate) {
        this.checkInDate = checkInDate;
    }
    public Date getCheckOutDate() {
        return checkOutDate;
    }
    public void setCheckOutDate(Date checkOutDate) {
        this.checkOutDate = checkOutDate;
    }
    public Date getCancelDate() {
        return cancelDate;
    }
    public void setCancelDate(Date cancelDate) {
        this.cancelDate = cancelDate;
    }
    public String getComment() {
        return comment;
    }
    public void setComment(String comment) {
        this.comment = comment;
    }
    public int getUniqueid() {
        return uniqueid;
    }
    public void setUniqueid(int uniqueid) {
        this.uniqueid = uniqueid;
    }
    public Date getScheduleDate() {
        return scheduleDate;
    }
    public void setScheduleDate(Date scheduleDate) {
        this.scheduleDate = scheduleDate;
    }
    public String getCancelStatusReasonName() {
        return cancelStatusReasonName;
    }
    public void setCancelStatusReasonName(String cancelStatusReasonName) {
        this.cancelStatusReasonName = cancelStatusReasonName;
    }
    public String getCheckoutStatusReasonName() {
        return checkoutStatusReasonName;
    }
    public void setCheckoutStatusReasonName(String checkoutStatusReasonName) {
        this.checkoutStatusReasonName = checkoutStatusReasonName;
    }
    public int getTotal() {
        return total;
    }
    public void setTotal(int total) {
        this.total = total;
    }
    public int getSize() {
        return size;
    }
    public void setSize(int size) {
        this.size = size;
    }
    public int getLimit() {
        return limit;
    }
    public void setLimit(int limit) {
        this.limit = limit;
    }
    public int getStart() {
        return start;
    }
    public void setStart(int start) {
        this.start = start;
    }
}
