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

import edu.harvard.catalyst.scheduler.dto.BooleanResultDTO;
import edu.harvard.catalyst.scheduler.entity.Study;

import java.util.Date;
import java.util.List;

public class StudyDTO extends BooleanResultDTO {

    private int id;
    private Study studyId;
    private String catalystId;
    private String localId;
    private String spid;
    private String protocolNumber;
    private String name;
    private String shortTitle;
    private int institution;

    private int studyStatus;

    private Date expectedStartDate;
    private Date expectedEndDate;

    private String irb;
    private Date irbExpiration;
    private Date irbRenewalDate;
    private Date irbApprovalDate;

    private int totalSubjects;
    private int totalOutpatientVisits;
    private int totalInpatientVisits;
    private String industryInitiated;

    private int investigator;
    private String investigatorName;
    private int physician;
    private int physician2;
    private int scheduler;
    private int secondaryScheduler;
    private int scheduler3;
    private int protocolNurse;
    private int associateNurse;
    private int protocolNutritionist;
    private int associateProtocolNutritionist;

    private int irbInstitution;
    private String pediatric;
    private String crcCategory;
    private Boolean crcFunded;

    private List<FundingSourceInfo> fundingSourceInfoList;

    private String studyAbstract;
    private String clinicalTrial;
    private String newDrug;
    private String deviceExemption;
    
    private Integer userId;


    public String getCatalystId() {
        return catalystId;
    }

    public void setCatalystId(String catalystId) {
        this.catalystId = catalystId;
    }

    public String getLocalId() {
        return localId;
    }

    public void setLocalId(String localId) {
        this.localId = localId;
    }

    public String getSpid() {
        return spid;
    }

    public void setSpid(String spid) {
        this.spid = spid;
    }

    public String getProtocolNumber() {
        return protocolNumber;
    }

    public void setProtocolNumber(String protocolNumber) {
        this.protocolNumber = protocolNumber;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getShortTitle() {
        return shortTitle;
    }

    public void setShortTitle(String shortTitle) {
        this.shortTitle = shortTitle;
    }

    public int getInstitution() {
        return institution;
    }

    public void setInstitution(int institution) {
        this.institution = institution;
    }

    public int getStudyStatus() {
        return studyStatus;
    }

    public void setStudyStatus(int studyStatus) {
        this.studyStatus = studyStatus;
    }

    public Date getExpectedStartDate() {
        return expectedStartDate;
    }

    public void setExpectedStartDate(Date expectedStartDate) {
        this.expectedStartDate = expectedStartDate;
    }

    public Date getExpectedEndDate() {
        return expectedEndDate;
    }

    public void setExpectedEndDate(Date expectedEndDate) {
        this.expectedEndDate = expectedEndDate;
    }

    public String getIrb() {
        return irb;
    }

    public void setIrb(String irb) {
        this.irb = irb;
    }

    public Date getIrbExpiration() {
        return irbExpiration;
    }

    public void setIrbExpiration(Date irbExpiration) {
        this.irbExpiration = irbExpiration;
    }

    public Date getIrbRenewalDate() {
        return irbRenewalDate;
    }

    public void setIrbRenewalDate(Date irbRenewalDate) {
        this.irbRenewalDate = irbRenewalDate;
    }

    public Date getIrbApprovalDate() {
        return irbApprovalDate;
    }

    public void setIrbApprovalDate(Date irbApprovalDate) {
        this.irbApprovalDate = irbApprovalDate;
    }

    public int getTotalSubjects() {
        return totalSubjects;
    }

    public void setTotalSubjects(int totalSubjects) {
        this.totalSubjects = totalSubjects;
    }

    public int getTotalOutpatientVisits() {
        return totalOutpatientVisits;
    }

    public void setTotalOutpatientVisits(int totalOutpatientVisits) {
        this.totalOutpatientVisits = totalOutpatientVisits;
    }

    public int getTotalInpatientVisits() {
        return totalInpatientVisits;
    }

    public void setTotalInpatientVisits(int totalInpatientVisits) {
        this.totalInpatientVisits = totalInpatientVisits;
    }

    public String getIndustryInitiated() {
        return industryInitiated;
    }

    public void setIndustryInitiated(String industryInitiated) {
        this.industryInitiated = industryInitiated;
    }

    public int getInvestigator() {
        return investigator;
    }

    public void setInvestigator(int investigator) {
        this.investigator = investigator;
    }

    public int getPhysician() {
        return physician;
    }

    public void setPhysician(int physician) {
        this.physician = physician;
    }

    public int getPhysician2() {
        return physician2;
    }

    public void setPhysician2(int physician2) {
        this.physician2 = physician2;
    }

    public int getScheduler() {
        return scheduler;
    }

    public void setScheduler(int scheduler) {
        this.scheduler = scheduler;
    }

    public int getSecondaryScheduler() {
        return secondaryScheduler;
    }

    public void setSecondaryScheduler(int secondaryScheduler) {
        this.secondaryScheduler = secondaryScheduler;
    }

    public int getProtocolNurse() {
        return protocolNurse;
    }

    public void setProtocolNurse(int protocolNurse) {
        this.protocolNurse = protocolNurse;
    }

    public int getAssociateNurse() {
        return associateNurse;
    }

    public void setAssociateNurse(int associateNurse) {
        this.associateNurse = associateNurse;
    }

    public int getProtocolNutritionist() {
        return protocolNutritionist;
    }

    public void setProtocolNutritionist(int protocolNutritionist) {
        this.protocolNutritionist = protocolNutritionist;
    }

    public int getIrbInstitution() {
        return irbInstitution;
    }

    public void setIrbInstitution(int irbInstitution) {
        this.irbInstitution = irbInstitution;
    }

    public String getPediatric() {
        return pediatric;
    }

    public void setPediatric(String pediatric) {
        this.pediatric = pediatric;
    }

    public String getCrcCategory() {
        return crcCategory;
    }

    public void setCrcCategory(String crcCategory) {
        this.crcCategory = crcCategory;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Study getStudyId() {
        return studyId;
    }

    // TODO-XH FIXME-XH: this is very confusing!!
    public void setStudyId(Study studyId) {
        this.studyId = studyId;
    }

    public String getInvestigatorName() {
        return investigatorName;
    }

    public void setInvestigatorName(String investigatorName) {
        this.investigatorName = investigatorName;
    }

    public int getScheduler3() {
        return scheduler3;
    }

    public void setScheduler3(int scheduler3) {
        this.scheduler3 = scheduler3;
    } 

    public int getAssociateProtocolNutritionist() {
        return associateProtocolNutritionist;
    }

    public void setAssociateProtocolNutritionist(int associateProtocolNutritionist) {
        this.associateProtocolNutritionist = associateProtocolNutritionist;
    }

    public Boolean getCrcFunded() {
        return crcFunded;
    }

    public void setCrcFunded(Boolean crcFunded) {
        this.crcFunded = crcFunded;
    }

    public String getStudyAbstract() {
        return studyAbstract;
    }

    public void setStudyAbstract(String studyAbstract) {
        this.studyAbstract = studyAbstract;
    }

    public String getClinicalTrial() {
        return clinicalTrial;
    }

    public void setClinicalTrial(String clinicalTrial) {
        this.clinicalTrial = clinicalTrial;
    }

    public String getNewDrug() {
        return newDrug;
    }

    public void setNewDrug(String newDrug) {
        this.newDrug = newDrug;
    }

    public String getDeviceExemption() {
        return deviceExemption;
    }

    public void setDeviceExemption(String deviceExemption) {
        this.deviceExemption = deviceExemption;
    }

    public List<FundingSourceInfo> getFundingSourceInfoList() {
        return fundingSourceInfoList;
    }

    public void setFundingSourceInfoList(List<FundingSourceInfo> fundingSourceInfoList) {
        this.fundingSourceInfoList = fundingSourceInfoList;
    }
}
