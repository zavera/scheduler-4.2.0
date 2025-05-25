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
import static edu.harvard.catalyst.scheduler.util.MiscUtil.fullName;
import static edu.harvard.catalyst.scheduler.util.MiscUtil.q;

public class OffUnitReportDTO implements CsvAbleDTO {

    private Date scheduledStartTime;
    private Date scheduledEndTime;
    private Date resourceStartTime;
    private Date resourceEndTime;
    private String localId;
    private String irb;
    private String visitName;
    private String visitType;
    private String visitStatus;
    private String firstName;
    private String middleName;
    private String lastName;
    private String mrn;
    private String comment;
    private String genderName;
    private Date birthdate;
    private String resourceName;
    private Integer bookedVisitId;

    public OffUnitReportDTO() {
    }



    public Integer getBookedVisitId() {
        return bookedVisitId;
    }

    public void setBookedVisitId(Integer bookedVisitId) {
        this.bookedVisitId = bookedVisitId;
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

    public Date getBirthdate() {
        return birthdate;
    }
    public void setBirthdate(Date birthdate) {
        this.birthdate = birthdate;
    }

    public String getResourceName() {
        return resourceName;
    }
    public void setResourceName(String resourceName) {
        this.resourceName = resourceName;
    }

    public Date getResourceStartTime() {
        return resourceStartTime;
    }

    public void setResourceStartTime(Date resourceStartTime) {
        this.resourceStartTime = resourceStartTime;
    }

    public Date getResourceEndTime() {
        return resourceEndTime;
    }

    public void setResourceEndTime(Date resourceEndTime) {
        this.resourceEndTime = resourceEndTime;
    }

    public String getVisitType() {
        return visitType;
    }

    public void setVisitType(String visitType) {
        this.visitType = visitType;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getVisitStatus() {
        return visitStatus;
    }

    public void setVisitStatus(String visitStatus) {
        this.visitStatus = visitStatus;
    }

    @Override
    public String toCsvHeaders() {
        return "Visit Name-Visit Type,Subject Name,MRN,Gender,Birth Date," +
                "Local ID,IRB #,Visit Start Time,Visit End Time,Comment," +
                "Resource Name,Start Time,End Time";
    }

    @Override
    public List<String> toCsvRows(List<?> dtoList) {

        List<String> result = Lists.newArrayList();

        int previousVisit = -1;
        boolean firstTime = true;

        for (Object object: dtoList) {
            OffUnitReportDTO d = (OffUnitReportDTO) object;

            List<String> columns = Lists.newArrayList();

            int currentVisit = d.bookedVisitId;
            if (currentVisit != previousVisit) {
                if (!firstTime) {
                    result.add(" \n");
                }
                firstTime = false;

                result.add(toCsvHeaders() + "\n");

                columns.add(q(d.visitName + " - " + d.visitType));
                columns.add(q(fullName(
                        d.firstName,
                        d.middleName,
                        d.lastName)));
                columns.add(q(d.mrn));
                columns.add(q(d.genderName));
                columns.add(q(showDateTime(d.birthdate)));
                columns.add(q(d.localId));
                columns.add(q(d.irb));
                columns.add(q(showDateTime(d.scheduledStartTime)));
                columns.add(q(showDateTime(d.scheduledEndTime)));
                columns.add(q(d.comment));
            }
            else {
                for (int i=0; i<10; i++) {
                    columns.add("");
                }
            }
            previousVisit = currentVisit;

            columns.add(q(d.resourceName));
            columns.add(q(showDateTime(d.resourceStartTime)));
            columns.add(q(showDateTime(d.resourceEndTime)));

            String rows = Joiner.on(",").join(columns);
            result.add(rows + "\n");
        }
        return result;
    }
}
