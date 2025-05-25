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
import static edu.harvard.catalyst.scheduler.util.MiscUtil.q;

public class VisitsFlaggedEditReportDTO extends BooleanResultDTO implements CsvAbleDTO {

    private int id;
    private String visitType;
    private String piName;
    private String visitName;
    private String localId;
    private String studyName;
    private Date checkInTime;
    private Date checkOutTime;
    private String subjectFirstName;
    private String subjectMiddleName;
    private String subjectLastName;
    private String mrn;
    private String birthdate;
    private boolean varyDuration;
    private boolean ommittedActivities;

    public int getId() {
        return id;
    }

    public void setId(final int id) {
        this.id = id;
    }

    public String getVisitName() {
        return visitName;
    }

    public void setVisitName(final String visitName) {
        this.visitName = visitName;
    }

    public String getLocalId() {
        return localId;
    }

    public void setLocalId(final String localId) {
        this.localId = localId;
    }

    public String getStudyName() {
        return studyName;
    }

    public void setStudyName(final String studyName) {
        this.studyName = studyName;
    }

    public String getSubjectFirstName() {
        return subjectFirstName;
    }

    public void setSubjectFirstName(final String subjectFirstName) {
        this.subjectFirstName = subjectFirstName;
    }

    public String getSubjectMiddleName() {
        return subjectMiddleName;
    }

    public void setSubjectMiddleName(final String subjectMiddleName) {
        this.subjectMiddleName = subjectMiddleName;
    }

    public String getSubjectLastName() {
        return subjectLastName;
    }

    public void setSubjectLastName(final String subjectLastName) {
        this.subjectLastName = subjectLastName;
    }

    public String getVisitType() {
        return visitType;
    }

    public void setVisitType(String visitType) {
        this.visitType = visitType;
    }

    public String getPiName() {
        return piName;
    }

    public void setPiName(String piName) {
        this.piName = piName;
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

    public boolean isVaryDuration() {
        return varyDuration;
    }

    public void setVaryDuration(boolean varyDuration) {
        this.varyDuration = varyDuration;
    }

    public boolean isOmmittedActivities() {
        return ommittedActivities;
    }

    public void setOmmittedActivities(boolean ommittedActivities) {
        this.ommittedActivities = ommittedActivities;
    }

    @Override
    public String toCsvHeaders() {
        return "Study Name,Local ID,PI Name,Subject Name,DOB,MRN," +
                "Visit Name, Visit Type, Check In, Check Out, " +
                "Resource Duration Variation?,Activity Variation?";
    }

    @Override
    public List<String> toCsvRows(List<?> dtoList) {

        List<String> result = Lists.newArrayList();

        int previousStudyId = -1;
        boolean firstHeader = true;

        for (Object object : dtoList) {
            VisitsFlaggedEditReportDTO d = (VisitsFlaggedEditReportDTO) object;

            List<String> columns = Lists.newArrayList();

            int currentStudyId = d.id;
            if (currentStudyId != previousStudyId) {
                if (!firstHeader) {
                    result.add(" \n");
                }
                result.add(toCsvHeaders() + "\n");

                firstHeader = false;
            }
            previousStudyId = currentStudyId;

            columns.add(q(d.studyName));
            columns.add(q(d.localId));
            columns.add(q(d.piName));
            columns.add(q(fullName(
                    d.subjectFirstName,
                    d.subjectMiddleName,
                    d.subjectLastName)));
            columns.add(q(d.birthdate));
            columns.add(q(d.mrn));
            columns.add(q(d.visitName));
            columns.add(q(d.visitType));
            columns.add(showDateTime(d.checkInTime));
            columns.add(showDateTime(d.checkOutTime));
            columns.add(yesNoValue(d.varyDuration));
            columns.add(yesNoValue(d.ommittedActivities));

            String rows = Joiner.on(",").join(columns);
            result.add(rows + "\n");
        }
        return result;
    }
}
