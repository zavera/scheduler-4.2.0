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

import java.util.Date;

/**
 * User: ankit
 * Date: 3/20/14
 * Time: 11:20 AM
 */
public class BookedVisitsResponse {
    private int id;
    private Long totalCount;
    private String visitName;
    private String irb;
    private String catId;
    private String localId;
    private String subjectFirstName;
    private String subjectLastName;
    private String subjectMRN;
    private Date scheduledStartTime;
    private Date scheduledEndTime;
    private String appointmentStatus;
    private int navigatePage;

    public BookedVisitsResponse(int id, String visitName, String irb, String catId,
                                String localId,
                                String subjectFirstName,
                                String subjectLastName,
                                String subjectMRN, String appointmentStatus,
                                Date scheduledStartTime, Date scheduledEndTime,
                                Long total, int navigatePage) {
        this.id = id;
        this.visitName = visitName;
        this.irb = irb;
        this.catId = catId;
        this.localId = localId;
        this.subjectFirstName = subjectFirstName;
        this.subjectLastName = subjectLastName;
        this.subjectMRN = subjectMRN;
        this.appointmentStatus = appointmentStatus;
        this.totalCount = total;   
        this.scheduledStartTime = scheduledStartTime;
        this.scheduledEndTime = scheduledEndTime;
        this.navigatePage = navigatePage;
    }

    public int getId() {
        return id;
    }

    public Long getTotalCount() {
        return totalCount;
    }

    public String getVisitName() {
        return visitName;
    }

    public String getIrb() {
        return irb;
    }

    public String getCatId() {
        return catId;
    }

    public String getLocalId() {
        return localId;
    }

    public String getSubjectFirstName() {
        return subjectFirstName;
    }

    public String getSubjectLastName() {
        return subjectLastName;
    }

    public String getSubjectMRN() {
        return subjectMRN;
    }

    public Date getScheduledStartTime() {
        return scheduledStartTime;
    }

    public Date getScheduledEndTime() {
        return scheduledEndTime;
    }

    public String getAppointmentStatus() {
        return appointmentStatus;
    }

    public int getNavigatePage() {
        return navigatePage;
    }
}
