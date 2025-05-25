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

public class VisitDTO extends AuthorizedDTO {

    private int id;
    private int studyId;
    private String name;
    private String shortName;
    private Boolean approved;
    private Boolean relativeTime;
    private Integer duration;
    private String nursing;
    private String nutrition;
    private String processing;
    private String setup;
    private Integer sublocation;
    private String comment;
    private Date createdDate;
    private Integer visitType;
    private int studyArm;
    private Boolean institutionNonCRC;
    private Boolean nonInstitutionNonCRC;
    private Boolean anthropometrySimple;
    private Boolean anthropometryComplex;
    private String mealPlanCalculation;
    private String nutrientAnalysis;
    private String educationTime;
    private String questionnaireTime;
    private Boolean vitaport;
    private Boolean assistantTechResearch;
    private Boolean assistantTechMed;
    private Boolean templateNurse;
    private String vitaportStart;
    private String vitaportMonitor;
    private String vitaportDownload;
    private String ecgMonitor;
    private String tempCollection;
    private String scalpElectrode;
    private String vitalSigns;
    private String techMedEcg;
    private String techMedPhlebotomy;
    private String techMedProcessing;
    private String techMedSpecialProcessing;
    private String bloods;
    private Boolean researchPharmacy;
    private int visitToDeactivateId;
    private int visitToActivateId; 
    private int deleteSelectedVisitId;
    private Boolean active;
    private int user;
    private Boolean billable;


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

    public int getStudyId() {
        return studyId;
    }

    public void setStudyId(int studyId) {
        this.studyId = studyId;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    public Boolean isApproved() {
        return approved;
    }

    public void setApproved(Boolean approved) {
        this.approved = approved;
    }

    public Boolean isRelativeTime() {
        return relativeTime;
    }

    public void setRelativeTime(Boolean relativeTime) {
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

    public Integer getSublocation() {
        return sublocation;
    }

    public void setSublocation(Integer sublocation) {
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

    public Integer getVisitType() {
        return visitType;
    }

    public void setVisitType(Integer visitType) {
        this.visitType = visitType;
    }

    public int getStudyArm() {
        return studyArm;
    }

    public void setStudyArm(int studyArm) {
        this.studyArm = studyArm;
    }

    public Boolean isInstitutionNonCRC() {
        return institutionNonCRC;
    }

    public void setInstitutionNonCRC(Boolean institutionNonCRC) {
        this.institutionNonCRC = institutionNonCRC;
    }

    public Boolean isNonInstitutionNonCRC() {
        return nonInstitutionNonCRC;
    }

    public void setNonInstitutionNonCRC(Boolean nonInstitutionNonCRC) {
        this.nonInstitutionNonCRC = nonInstitutionNonCRC;
    }

    public Boolean isAnthropometrySimple() {
        return anthropometrySimple;
    }

    public void setAnthropometrySimple(Boolean anthropometrySimple) {
        this.anthropometrySimple = anthropometrySimple;
    }

    public Boolean isAnthropometryComplex() {
        return anthropometryComplex;
    }

    public void setAnthropometryComplex(Boolean anthropometryComplex) {
        this.anthropometryComplex = anthropometryComplex;
    }

    public String getMealPlanCalculation() {
        return mealPlanCalculation;
    }

    public void setMealPlanCalculation(String mealPlanCalculation) {
        this.mealPlanCalculation = mealPlanCalculation;
    }

    public String getNutrientAnalysis() {
        return nutrientAnalysis;
    }

    public void setNutrientAnalysis(String nutrientAnalysis) {
        this.nutrientAnalysis = nutrientAnalysis;
    }

    public String getEducationTime() {
        return educationTime;
    }

    public void setEducationTime(String educationTime) {
        this.educationTime = educationTime;
    }

    public String getQuestionnaireTime() {
        return questionnaireTime;
    }

    public void setQuestionnaireTime(String questionnaireTime) {
        this.questionnaireTime = questionnaireTime;
    }

    public Boolean getVitaport() {
        return vitaport;
    }

    public void setVitaport(Boolean vitaport) {
        this.vitaport = vitaport;
    }

    public Boolean getAssistantTechResearch() {
        return assistantTechResearch;
    }

    public void setAssistantTechResearch(Boolean assistantTechResearch) {
        this.assistantTechResearch = assistantTechResearch;
    }

    public Boolean getAssistantTechMed() {
        return assistantTechMed;
    }

    public void setAssistantTechMed(Boolean assistantTechMed) {
        this.assistantTechMed = assistantTechMed;
    }

    public Boolean getTemplateNurse() {
        return templateNurse;
    }

    public void setTemplateNurse(Boolean templateNurse) {
        this.templateNurse = templateNurse;
    }

    public String getVitaportStart() {
        return vitaportStart;
    }

    public void setVitaportStart(String vitaportStart) {
        this.vitaportStart = vitaportStart;
    }

    public String getVitaportMonitor() {
        return vitaportMonitor;
    }

    public void setVitaportMonitor(String vitaportMonitor) {
        this.vitaportMonitor = vitaportMonitor;
    }

    public String getVitaportDownload() {
        return vitaportDownload;
    }

    public void setVitaportDownload(String vitaportDownload) {
        this.vitaportDownload = vitaportDownload;
    }

    public String getEcgMonitor() {
        return ecgMonitor;
    }

    public void setEcgMonitor(String ecgMonitor) {
        this.ecgMonitor = ecgMonitor;
    }

    public String getTempCollection() {
        return tempCollection;
    }

    public void setTempCollection(String tempCollection) {
        this.tempCollection = tempCollection;
    }

    public String getScalpElectrode() {
        return scalpElectrode;
    }

    public void setScalpElectrode(String scalpElectrode) {
        this.scalpElectrode = scalpElectrode;
    }

    public String getVitalSigns() {
        return vitalSigns;
    }

    public void setVitalSigns(String vitalSigns) {
        this.vitalSigns = vitalSigns;
    }

    public String getTechMedEcg() {
        return techMedEcg;
    }

    public void setTechMedEcg(String techMedEcg) {
        this.techMedEcg = techMedEcg;
    }

    public String getTechMedPhlebotomy() {
        return techMedPhlebotomy;
    }

    public void setTechMedPhlebotomy(String techMedPhlebotomy) {
        this.techMedPhlebotomy = techMedPhlebotomy;
    }

    public String getTechMedProcessing() {
        return techMedProcessing;
    }

    public void setTechMedProcessing(String techMedProcessing) {
        this.techMedProcessing = techMedProcessing;
    }

    public String getTechMedSpecialProcessing() {
        return techMedSpecialProcessing;
    }

    public void setTechMedSpecialProcessing(String techMedSpecialProcessing) {
        this.techMedSpecialProcessing = techMedSpecialProcessing;
    }

    public String getBloods() {
        return bloods;
    }

    public void setBloods(String bloods) {
        this.bloods = bloods;
    }

    public Boolean isResearchPharmacy() {
        return researchPharmacy;
    }

    public void setResearchPharmacy(Boolean researchPharmacy) {
        this.researchPharmacy = researchPharmacy;
    }

    public Boolean isActive() {
        return active;
    }

    public void setActive(Boolean active) {
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

    public Boolean getBillable() {
        return billable;
    }

    public void setBillable(Boolean billable) {
        this.billable = billable;
    }
}
