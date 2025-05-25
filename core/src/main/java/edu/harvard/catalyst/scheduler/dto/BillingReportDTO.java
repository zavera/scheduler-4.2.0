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

import static edu.harvard.catalyst.scheduler.util.MiscUtil.*;

public class BillingReportDTO implements CsvAbleDTO {

    private Integer studyId;
    private String studyName;
    private String catalystId;
    private String localId;
    private String irb;
    private Boolean industryInitiated;
    private Boolean crcFunded;
    private Date checkInTime;
    private Date checkOutTime;
    private Date scheduledStartTime;
    private Date scheduledEndTime;
    private Date visitCheckInTime;
    private Date visitCheckOutTime;
    private String sublocation;
    private String resourceName;
    private Boolean billable;
    private String visitName;
    private String visitType;
    private String subjectFirstName;
    private String subjectMiddleName;
    private String subjectLastName;
    private String mrn;
    private String birthdate;
    private String investigatorFirstName;
    private String investigatorMiddleName;
    private String investigatorLastName;
    private String bookedVisitComment;
    private String resourceActivities;
    private String resourceActivitiesComment;
    private String resourceActivitiesQuantity;

    public BillingReportDTO() {
    }

    @Override
    public String toCsvHeaders() {
        return "Study Name,Catalyst ID,Local ID,IRB #," +
                "Industry Initiated,PI Name,CRC Approved Study," +
                "Resource Start Time,Resource End Time," +
                "Resource Duration,Resource Duration In Mins," +
                "Check In,Check Out,Visit Duration," +
                "Visit Duration Total Mins,Visit Over Midnight?," +
                "Sublocation,Resource Name, Resource Activities, " +
                "Resource Activities Comment, " +
                "Resource Activities Quantity, Billable,Visit Name," +
                "Visit Type,Study Subject, MRN,Birth Date," +
                "Booked Visit Comment";
    }

    @Override
    public List<String> toCsvRows(List<?> dtoList) {
        List<String> result = Lists.newArrayList();
        result.add(toCsvHeaders() + "\n");

        for (Object object: dtoList) {
            BillingReportDTO d = (BillingReportDTO) object;

            List<String> columns = Lists.newArrayList();

            columns.add(q(d.studyName));
            columns.add(q(d.catalystId));
            columns.add(q(d.localId));
            columns.add(q(d.irb));
            columns.add(q(yesNoValue(d.industryInitiated)));
            columns.add(q(fullName(
                    d.investigatorFirstName,
                    d.investigatorMiddleName,
                    d.investigatorLastName)));
            columns.add(q(yesNoValue(d.crcFunded)));
            columns.add(q(showDateTime(d.checkInTime)));
            columns.add(q(showDateTime(d.checkOutTime)));
            columns.add(q(formatEndMinusStart(
                    d.checkInTime, d.checkOutTime)));
            columns.add(q(endMinusStartInMinutes(
                    d.checkInTime, d.checkOutTime)));
            columns.add(q(showDateTime(d.visitCheckInTime)));
            columns.add(q(showDateTime(d.visitCheckOutTime)));
            columns.add(q(formatEndMinusStart(
                    d.visitCheckInTime, d.visitCheckOutTime)));
            columns.add(q(endMinusStartInMinutes(
                    d.visitCheckInTime, d.visitCheckOutTime)));
            columns.add(q(localDate2LaterThanDate1YesNo(
                    d.scheduledStartTime, d.scheduledEndTime)));
            columns.add(q(d.sublocation));
            columns.add(q(d.resourceName));
            columns.add(q(d.resourceActivities));
            columns.add(q(d.resourceActivitiesComment));
            columns.add(q(d.resourceActivitiesQuantity));
            columns.add(q(yesNoValue(d.billable)));
            columns.add(q(d.visitName));
            columns.add(q(d.visitType));
            columns.add(q(fullName(
                    d.subjectFirstName,
                    d.subjectMiddleName,
                    d.subjectLastName)));
            columns.add(q(d.mrn));
            columns.add(q(d.birthdate));
            columns.add(q(d.bookedVisitComment));

            String rows = Joiner.on(",").join(columns);
            result.add(rows + "\n");
        }
        return result;
    }

    public Integer getStudyId() {
        return studyId;
    }
    public void setStudyId(Integer studyId) {
        this.studyId = studyId;
    }

    public String getStudyName() {
        return studyName;
    }
    public void setStudyName(String studyName) {
        this.studyName = studyName;
    }

    public String getCatalystId() {
        return catalystId;
    }
    public void setCatalystId(String   catalystId) {
        this.catalystId = catalystId;
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

    public Boolean getIndustryInitiated() {
        return industryInitiated;
    }
    public void setIndustryInitiated(Boolean industryInitiated) {
        this.industryInitiated = industryInitiated;
    }

    public Date getCheckInTime() {
        return checkInTime;
    }
    public void setCheckInTime(Date checkInTime) {
        this.checkInTime = checkInTime;
    }

    public Date getCheckOutTime() {
        return checkOutTime;
    }
    public void setCheckOutTime(Date checkOutTime) {
        this.checkOutTime = checkOutTime;
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

    public Date getVisitCheckInTime() {
        return visitCheckInTime;
    }

    public void setVisitCheckInTime(Date visitCheckInTime) {
        this.visitCheckInTime = visitCheckInTime;
    }

    public Date getVisitCheckOutTime() {
        return visitCheckOutTime;
    }

    public void setVisitCheckOutTime(Date visitCheckOutTime) {
        this.visitCheckOutTime = visitCheckOutTime;
    }

    public String getSublocation() {
        return sublocation;
    }
    public void setSublocation(String sublocation) {
        this.sublocation = sublocation;
    }

    public String getResourceName() {
        return resourceName;
    }
    public void setResourceName(String resourceName) {
        this.resourceName = resourceName;
    }

    public Boolean getBillable() {
        return billable;
    }
    public void setBillable(Boolean billable) {
        this.billable = billable;
    }

    public String getVisitName() {
        return visitName;
    }
    public void setVisitName(String visitName) {
        this.visitName = visitName;
    }

    public String getVisitType() {
        return visitType;
    }
    public void setVisitType(String visitType) {
        this.visitType = visitType;
    }

    public String getSubjectFirstName() {
        return subjectFirstName;
    }
    public void setSubjectFirstName(String subjectFirstName) {
        this.subjectFirstName = subjectFirstName;
    }

    public String getSubjectMiddleName() {
        return subjectMiddleName;
    }
    public void setSubjectMiddleName(String subjectMiddleName) {
        this.subjectMiddleName = subjectMiddleName;
    }

    public String getSubjectLastName() {
        return subjectLastName;
    }
    public void setSubjectLastName(String subjectLastName) {
        this.subjectLastName = subjectLastName;
    }

    public String getMrn() {
        return mrn;
    }
    public void setMrn(String mrn) {
        this.mrn = mrn;
    }

    public String getBirthdate() {
        return birthdate;
    }

    public void setBirthdate(String birthdate) {
        this.birthdate = birthdate;
    }

    public String getInvestigatorFirstName() {
        return investigatorFirstName;
    }

    public void setInvestigatorFirstName(String investigatorFirstName) {
        this.investigatorFirstName = investigatorFirstName;
    }

    public String getInvestigatorMiddleName() {
        return investigatorMiddleName;
    }

    public void setInvestigatorMiddleName(String investigatorMiddleName) {
        this.investigatorMiddleName = investigatorMiddleName;
    }

    public String getInvestigatorLastName() {
        return investigatorLastName;
    }

    public void setInvestigatorLastName(String investigatorLastName) {
        this.investigatorLastName = investigatorLastName;
    }

    public String getBookedVisitComment() {
        return bookedVisitComment;
    }

    public void setBookedVisitComment(String bookedVisitComment) {
        this.bookedVisitComment = bookedVisitComment;
    }    

    public Boolean getCrcFunded() {
        return crcFunded;
    }

    public void setCrcFunded(Boolean crcFunded) {
        this.crcFunded = crcFunded;
    }

    public String getResourceActivities() {
        return resourceActivities;
    }

    public void setResourceActivities(String resourceActivities) {
        this.resourceActivities = resourceActivities;
    }

    public String getResourceActivitiesComment() {
        return resourceActivitiesComment;
    }

    public void setResourceActivitiesComment(String resourceActivitiesComment) {
        this.resourceActivitiesComment = resourceActivitiesComment;
    }

    public String getResourceActivitiesQuantity() {
        return resourceActivitiesQuantity;
    }

    public void setResourceActivitiesQuantity(String resourceActivitiesQuantity) {
        this.resourceActivitiesQuantity = resourceActivitiesQuantity;
    } 
}
