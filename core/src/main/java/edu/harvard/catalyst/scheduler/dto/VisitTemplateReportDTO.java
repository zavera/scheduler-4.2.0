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

public class VisitTemplateReportDTO extends AuthorizedDTO implements CsvAbleDTO {

    private Integer id;
    private String studyId; // !! this seems to be used for localID
    private String name;
    private String shortName;
    private Boolean approved;
    private Boolean relativeTime;
    private Integer duration;
    private String nursing;
    private String nutrition;
    private String processing;
    private String setup;
    private String sublocation;
    private String comment;
    private Date createdDate;
    private String visitType;
    private Boolean institutionNonCRC;
    private Boolean nonInstitutionNonCRC;
    private Boolean researchPharmacy;
    private Integer visitToDeactivateId;
    private Integer visitToActivateId; 
    private Integer deleteSelectedVisitId;
    private Boolean active;
    private String studyName;
    private String studyStatus;
    private Boolean billable;

    private int user;


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getShortName() {
        return shortName;
    }

    public String getStudyId() {
        return studyId;
    }

    public void setStudyId(String studyId) {
        this.studyId = studyId;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    public boolean isApproved() {
        return approved;
    }

    public void setApproved(boolean approved) {
        this.approved = approved;
    }

    public boolean isRelativeTime() {
        return relativeTime;
    }

    public void setRelativeTime(boolean relativeTime) {
        this.relativeTime = relativeTime;
    }

    public Integer getDuration() {
        return duration;
    }

    public void setDuration(Integer duration) {
        this.duration = duration;
    }

    public String getNursing() {
        return nursing;
    }

    public void setNursing(String nursing) {
        this.nursing = nursing;
    }

    public String getNutrition() {
        return nutrition;
    }

    public void setNutrition(String nutrition) {
        this.nutrition = nutrition;
    }

    public String getProcessing() {
        return processing;
    }

    public void setProcessing(String processing) {
        this.processing = processing;
    }

    public String getSetup() {
        return setup;
    }

    public void setSetup(String setup) {
        this.setup = setup;
    }

    public String getSublocation() {
        return sublocation;
    }

    public void setSublocation(String sublocation) {
        this.sublocation = sublocation;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }

    public String getVisitType() {
        return visitType;
    }

    public void setVisitType(String visitType) {
        this.visitType = visitType;
    }


    public boolean isInstitutionNonCRC() {
        return institutionNonCRC;
    }

    public void setInstitutionNonCRC(boolean institutionNonCRC) {
        this.institutionNonCRC = institutionNonCRC;
    }

    public boolean isNonInstitutionNonCRC() {
        return nonInstitutionNonCRC;
    }

    public void setNonInstitutionNonCRC(boolean nonInstitutionNonCRC) {
        this.nonInstitutionNonCRC = nonInstitutionNonCRC;
    }

    public boolean isResearchPharmacy() {
        return researchPharmacy;
    }

    public void setResearchPharmacy(boolean researchPharmacy) {
        this.researchPharmacy = researchPharmacy;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public int getUser() {
        return user;
    }

    public void setUser(int user) {
        this.user = user;
    }

    public int getVisitToDeactivateId() {
        return visitToDeactivateId;
    }

    public void setVisitToDeactivateId(int visitToDeactivateId) {
        this.visitToDeactivateId = visitToDeactivateId;
    }

    public int getVisitToActivateId() {
        return visitToActivateId;
    }

    public void setVisitToActivateId(int visitToActivateId) {
        this.visitToActivateId = visitToActivateId;
    }

    public int getDeleteSelectedVisitId() {
        return deleteSelectedVisitId;
    }

    public void setDeleteSelectedVisitId(int deleteSelectedVisitId) {
        this.deleteSelectedVisitId = deleteSelectedVisitId;
    }

    public String getStudyName() {
        return studyName;
    }

    public void setStudyName(String studyName) {
        this.studyName = studyName;
    }

    public String getStudyStatus() {
        return studyStatus;
    }

    public void setStudyStatus(String studyStatus) {
        this.studyStatus = studyStatus;
    }

    @Override
    public String toCsvHeaders() {
        return "Study Id,Study Name,Study Status," +
                "Visit Name,Visit Type,Sublocation,Billable Template?,Research Pharmacy?," +
                "Institution: Non CRC,Non Institution: Non CRC," +
                "Relative Time?,Nursing,Nutrition,Processing," +
                "Setup,Comment";
    }

    @Override
    public List<String> toCsvRows(List<?> dtoList) {
        List<String> result = Lists.newArrayList();
        result.add(toCsvHeaders() + "\n");

        for (Object object: dtoList) {
            VisitTemplateReportDTO d = (VisitTemplateReportDTO) object;

            List<String> columns = Lists.newArrayList();

            columns.add(q(d.studyId));
            columns.add(q(d.studyName));
            columns.add(q(d.studyStatus));
            columns.add(q(d.name));
            columns.add(q(d.visitType));
            columns.add(q(d.sublocation));
            columns.add(q(yesNoValue(d.billable)));
            columns.add(q(yesNoValue(d.researchPharmacy)));
            columns.add(q(yesNoValue(d.institutionNonCRC)));
            columns.add(q(yesNoValue(d.nonInstitutionNonCRC)));
            columns.add(q(yesNoValue(d.relativeTime)));
            columns.add(q(d.nursing));
            columns.add(q(d.nutrition));
            columns.add(q(d.processing));
            columns.add(q(d.setup));
            columns.add(q(d.comment));

            String rows = Joiner.on(",").join(columns);
            result.add(rows + "\n");
        }
        return result;
    }

    public Boolean getBillable() {
        return billable;
    }

    public void setBillable(Boolean billable) {
        this.billable = billable;
    }
}
