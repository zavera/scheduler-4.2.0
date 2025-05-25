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

import edu.harvard.catalyst.scheduler.core.SchedulerRuntimeException;
import edu.harvard.catalyst.scheduler.entity.Study;
import edu.harvard.catalyst.scheduler.entity.User;
import edu.harvard.catalyst.scheduler.util.DateUtility;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * User: ankit
 * Date: 3/14/14
 * Time: 1:36 PM
 */
public class StudyDetailResponse {

        private Integer id;
        private String name;
        
        private String studyStatusName;
        private int studyStatusId;
        private boolean isPending;
        private boolean isOpen;
        private boolean isClosed;

        private String institutionName;
        private int institutionId;
        
        private Boolean industryInitiated;
        
        private String catalystId;
        private String localId;
        private String spid;
        private String protocolNumber;
        
        private String shortTitle;
        
        private Date expectedStartDate;
        private Date expectedEndDate;

        private String irb;
        private Date irbExpiration;
        private Date irbRenewalDate;
        private Date irbApprovalDate;

        private Integer totalSubjects;
        private Integer totalOutpatientVisits;
        private Integer totalInpatientVisits;

        private String studyAbstract;
        private String clinicalTrial;
        private String newDrug;
        private String deviceExemption;
              
        private User1 scheduler;
        private User1 secondaryScheduler;
        private User1 scheduler3;
        private User1 protocolNurse;
        private User1 associateNurse;
        private User1 protocolNutritionist;
        private User1 associateProtocolNutritionist;
        
        private User2 investigator;
        private User2 physician;
        private User2 physician2;
        
        private String protocolNurseString;
        private String protocolNutritionistString;
        private Boolean crcFunded;


        private String irbInstitutionName;
        private int irbInstitutionId;
        private String pediatric;
        private String crcCategory;
        private Date statusChange;
        private String firstVisitDate;
        private String lastScheduledVisitDate;

        private List<FundingSourceInfo> fundingSourceInfoList = new ArrayList<FundingSourceInfo>();

        class User1 {
            int id;
            String firstName = "";
            String middleName = "";
            String lastName = "";
            String email = "";
            String institution = "";
            String primaryPhone = "";
            
            User1(User user) {
                if (user == null) {
                    return;
                }
                this.id = user.getId();
                this.firstName = user.getFirstName();
                this.middleName = user.getMiddleName();
                this.lastName = user.getLastName();
                this.email = user.getEmail();
                this.institution = user.getInstitution().getName();
                this.primaryPhone = user.getPrimaryPhone();
            }
        }
        
        class User2 {
            int id;
            String firstName = "";
            String middleName = "";
            String lastName = "";
            String email = "";
            String primaryPhone = "";
            String secondaryPhone = "";
            String pager = "";
            String credential = "";
            String division = "";
            String department = "";
            
            User2(User user) {
                if (user == null) {
                    return;
                }
                this.id = user.getId();
                this.firstName = user.getFirstName();
                this.middleName = user.getMiddleName();
                this.lastName = user.getLastName();
                this.email = user.getEmail();
                this.primaryPhone = user.getPrimaryPhone();
                this.secondaryPhone = user.getSecondaryPhone();
                this.pager = user.getPager();
                if (user.getCredential() != null) {
                    this.credential = user.getCredential().getName();                    
                }
                if (user.getDivision() != null) {
                    this.division = user.getDivision().getName();                    
                }
                if (user.getDepartment() != null) {
                    this.department = user.getDepartment().getName();                    
                }
            }
        }
        
        public StudyDetailResponse(Study study, List<FundingSourceInfo> fundingSourceInfoList) {
            if (study == null) {
                SchedulerRuntimeException.logAndThrow("Study parameter should be non-null");
            }
            this.id = study.getId();
            this.name = study.getName();
            this.shortTitle = study.getShortTitle();
            this.studyStatusName = study.getStudyStatus().getShortName();
            this.studyStatusId = study.getStudyStatus().getId();
            this.isPending = study.getStudyStatus().getIsPending();
            this.isOpen = study.getStudyStatus().getIsOpen();
            this.isClosed = study.getStudyStatus().getIsClosed();
            this.institutionName = study.getInstitution().getName();
            this.institutionId = study.getInstitution().getId();
            this.industryInitiated = study.getIndustryInitiated();
            this.localId = study.getLocalId();   
            this.catalystId = study.getCatalystId();
            this.irb = study.getIrb();
            this.totalSubjects = study.getTotalSubjects();
            this.totalOutpatientVisits = study.getTotalOutpatientVisits();
            this.totalInpatientVisits = study.getTotalInpatientVisits();

            this.fundingSourceInfoList = fundingSourceInfoList;

            this.crcFunded = study.getCrcFunded();

            this.pediatric = study.getPediatric();
            this.crcCategory = study.getCrcCategory();
            this.clinicalTrial = study.getClinicalTrial();
            this.newDrug = study.getNewDrug();
            this.deviceExemption = study.getDeviceExemption();
            this.irbInstitutionName = "";
            if (study.getIrbInstitution() != null) {
                this.irbInstitutionId = study.getIrbInstitution().getId();
                this.irbInstitutionName = study.getIrbInstitution().getName();
            }
            
            this.scheduler = new User1(study.getScheduler());
            this.secondaryScheduler = new User1(study.getSecondaryScheduler());
            this.scheduler3 = new User1(study.getScheduler3());
            this.protocolNurse = new User1(study.getProtocolNurse());
            this.protocolNurseString = study.getProtocolNurseString();
            this.associateNurse = new User1(study.getAssociateNurse());
            this.protocolNutritionist = new User1(study.getProtocolNutritionist());
            this.protocolNutritionistString = study.getProtocolNutritionistString();
            this.associateProtocolNutritionist = new User1(study.getAssociateProtocolNutritionist());

            this.investigator = new User2(study.getInvestigator());
            this.physician = new User2(study.getPhysician());
            this.physician2 = new User2(study.getPhysician2());
            
            this.studyAbstract = study.getStudyAbstract();
            
            this.irbExpiration = study.getIrbExpiration();
            this.irbRenewalDate = study.getIrbRenewalDate();
            this.irbApprovalDate = study.getIrbApprovalDate();
            this.expectedStartDate = study.getExpectedStartDate();
            this.expectedEndDate = study.getExpectedEndDate();
            this.firstVisitDate = DateUtility.format(DateUtility.dateTime(), study.getFirstVisitDate());
            this.lastScheduledVisitDate = DateUtility.format(DateUtility.dateTime(), study.getLastScheduledVisitDate());
        }
}
