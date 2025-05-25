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

public class StudySubjectVisitReportDTO implements CsvAbleDTO {

    private String subjectLastName;
    private String subjectFirstName;
    private String subjectMiddleName;
    private String mrn;
    private String dob;
    private String localId;
    private String visitName;
    private Date scheduledStartTime;
    private Date scheduledEndTime;
    private String appointmentStatus;

    public StudySubjectVisitReportDTO() {
    }

    public String getVisitName() {
        return visitName;
    }
    public void setVisitName(String visitName) {
        this.visitName = visitName;
    }

    public String getSubjectLastName() {
        return subjectLastName;
    }

    public void setSubjectLastName(String subjectLastName) {
        this.subjectLastName = subjectLastName;
    }

    public String getSubjectFirstName() {
        return subjectFirstName;
    }

    public void setSubjectFirstName(String subjectFirstName) {
        this.subjectFirstName = subjectFirstName;
    }

    public String getMrn() {
        return mrn;
    }

    public void setMrn(String mrn) {
        this.mrn = mrn;
    }

    public String getLocalId() {
        return localId;
    }

    public void setLocalId(String localId) {
        this.localId = localId;
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

    public String getAppointmentStatus() {
        return appointmentStatus;
    }

    public void setAppointmentStatus(String appointmentStatus) {
        this.appointmentStatus = appointmentStatus;
    }

    public String getSubjectMiddleName() {
        return subjectMiddleName;
    }

    public void setSubjectMiddleName(String subjectMiddleName) {
        this.subjectMiddleName = subjectMiddleName;
    }

	public String getDob() {
		return dob;
	}

	public void setDob(String dob) {
		this.dob = dob;
	}

    @Override
    public String toCsvHeaders() {
        return "Subject Name,MRN,DOB,Visit Name," +
                "Scheduled Visit Start Time," +
                "Scheduled Visit End Time,Appointment Status," +
                "Local ID";
    }

    @Override
    public List<String> toCsvRows(List<?> dtoList) {
        List<String> result = Lists.newArrayList();

        String previousSubjectId = null;

        for (Object object: dtoList) {
            StudySubjectVisitReportDTO d =
                    (StudySubjectVisitReportDTO) object;
            List<String> columns = Lists.newArrayList();

            String currentSubjectId = d.mrn;
            if (! currentSubjectId.equals(previousSubjectId)) {

                result.add(toCsvHeaders() + "\n");

                previousSubjectId = currentSubjectId;
            }
            columns.add(q(fullName(
                    d.subjectFirstName,
                    d.subjectMiddleName,
                    d.subjectLastName)));

            columns.add(q(d.mrn));
            columns.add(q(d.dob));
            columns.add(q(d.visitName));
            columns.add(q(showDateTime(d.scheduledStartTime)));
            columns.add(q(showDateTime(d.scheduledEndTime)));
            columns.add(q(d.appointmentStatus));
            columns.add(q(d.localId));

            String rows = Joiner.on(",").join(columns);
            result.add(rows + "\n");
        }
        return result;
    }
}