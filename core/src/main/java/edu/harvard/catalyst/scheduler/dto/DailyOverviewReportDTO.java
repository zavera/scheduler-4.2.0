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

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;

import java.util.Date;
import java.util.List;

import static edu.harvard.catalyst.scheduler.util.MiscUtil.fullName;
import static edu.harvard.catalyst.scheduler.util.MiscUtil.q;
import static edu.harvard.catalyst.scheduler.util.MiscUtil.showDateTime;

public class DailyOverviewReportDTO implements CsvAbleDTO {

    protected Date scheduledStartTime;
    protected Date scheduledEndTime;
    protected Date resourceStartTime;
    protected Date resourceEndTime;
    protected String sublocationName;
    protected String localId;
    protected String irb;
    protected String visitName;
    protected String visitStatus;
    protected String visitType;
    protected String firstName;
    protected String middleName;
    protected String lastName;
    protected String mrn;
    protected String offInstitution;
    protected String genderName;
    protected String birthdate;
    protected String resourceName;
    protected Integer bookedVisitId;
    protected String comment;


    public DailyOverviewReportDTO() {
    }

    public String getOffInstitution() {
        return offInstitution;
    }

    public void setOffInstitution(String offInstitution) {
        this.offInstitution = offInstitution;
    }

    public Integer getBookedVisitId() {
        return bookedVisitId;
    }

    public void setBookedVisitId(Integer bookedVisitId) {
        this.bookedVisitId = bookedVisitId;
    }

    public Date getResourceStartTime() {
        return resourceStartTime;
    }

    public void setResourceStartTime(Date resourceStartTime) {
        this.resourceStartTime = resourceStartTime;
    }

    public String getVisitType() {
        return visitType;
    }

    public void setVisitType(String visitType) {
        this.visitType = visitType;
    }

    public Date getResourceEndTime() {
        return resourceEndTime;
    }

    public void setResourceEndTime(Date resourceEndTime) {
        this.resourceEndTime = resourceEndTime;
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

    public String getLocalId() {
        return localId;
    }
    
    public void setLocalId(String localId) {
        this.localId = localId;
    }

    public String getIrb() {
        return irb;
    }
    
    public void setIrb(String irb) {
        this.irb = irb;
    }

    public String getVisitName() {
        return visitName;
    }
    
    public void setVisitName(String visitName) {
        this.visitName = visitName;
    }

    public String getFirstName() {
        return firstName;
    }
    
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getMiddleName() {
        return middleName;
    }
    
    public void setMiddleName(String middleName) {
        this.middleName = middleName;
    }

    public String getLastName() {
        return lastName;
    }
    
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getMrn() {
        return mrn;
    }
    
    public void setMrn(String mrn) {
        this.mrn = mrn;
    }

    public String getGenderName() {
        return genderName;
    }
    
    public void setGenderName(String genderName) {
        this.genderName = genderName;
    }

    public String getBirthdate() {
        return birthdate;
    }
    
    public void setBirthdate(String birthdate) {
        this.birthdate = birthdate;
    }

    public String getResourceName() {
        return resourceName;
    }
    
    public void setResourceName(String resourceName) {
        this.resourceName = resourceName;
    }
    
    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getSublocationName() {
        return sublocationName;
    }

    public void setSublocationName(String sublocationName) {
        this.sublocationName = sublocationName;
    }

    public String getVisitStatus() {
        return visitStatus;
    }

    public void setVisitStatus(String visitStatus) {
        this.visitStatus = visitStatus;
    }

    @Override
    public String toCsvHeaders() {
        return "Visit Length,Subject,MRN,Gender,Birth Date," +
                "Local ID,IRB #,Visit Name,Visit Type,Sublocation," +
                "Is part of the Visit Off Unit?,Resource Name," +
                "Start Time,End Time,Comment";
    }

    @Override
    public List<String> toCsvRows(List<?> dtoList) {

        List<String> result = Lists.newArrayList();
        result.add(toCsvHeaders() + "\n");

        for (Object object: dtoList) {
            DailyOverviewReportDTO d = (DailyOverviewReportDTO) object;

            List<String> columns = Lists.newArrayList();

            columns.add(q(
                    showDateTime(d.scheduledStartTime) + " - " +
                    showDateTime(d.scheduledEndTime)));
            columns.add(q(fullName(
                    d.firstName,
                    d.middleName,
                    d.lastName)));
            columns.add(q(d.mrn));
            columns.add(q(d.genderName));
            columns.add(q(d.birthdate));
            columns.add(q(d.localId));
            columns.add(q(d.irb));
            columns.add(q(d.visitName));
            columns.add(q(d.visitType));
            columns.add(q(d.sublocationName));
            columns.add(q(d.offInstitution));
            columns.add(q(d.resourceName));
            columns.add(q(showDateTime(d.resourceStartTime)));
            columns.add(q(showDateTime(d.resourceEndTime)));
            columns.add(q(d.comment));

            String rows = Joiner.on(",").join(columns);
            result.add(rows + "\n");
        }
        return result;
    }
}
