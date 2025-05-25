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

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import edu.harvard.catalyst.scheduler.core.SchedulerRuntimeException;
import edu.harvard.catalyst.scheduler.dto.CsvAbleDTO;
import edu.harvard.catalyst.scheduler.entity.*;

import java.util.*;

import static edu.harvard.catalyst.scheduler.util.MiscUtil.*;

/**
 * Created with IntelliJ IDEA.
 * User: carl
 * Date: 1/30/14
 * Time: 2:21 PM
 */


public class StudyDataReportResponseDTO implements CsvAbleDTO {
    // all the non-transient fields from Study
    // gson only looks at fields, not getters :(
    ////////////////////

    private String catalystId;
    private String localId;
    private String name;
    private DtoInstitution institution;

    private DtoStudyStatus studyStatus;

    private Date expectedStartDate;
    private Date expectedEndDate;

    private Date statusChange;
    private Date firstVisitDate;
    private Date lastScheduledVisitDate;

    private String irb;
    private Date irbExpiration;
    private Date irbRenewalDate;
    private Date irbApprovalDate;

    private Integer totalSubjects;
    private Integer totalOutpatientVisits;
    private Integer totalInpatientVisits;
    private Boolean industryInitiated;

    private User4 investigator;
    private User4 physician;
    private User4 physician2;
    private User3 scheduler;
    private User3 secondaryScheduler;
    private User3 scheduler3;
    private User1 protocolNurse;
    private User1 associateNurse;
    private User1 protocolNutritionist;
    private User1 associateProtocolNutritionist;

    private Boolean crcFunded;
    private String crcCategory;

    private DtoIrbInstitution irbInstitution;

    private List<FundingSourceInfo> fundingSourceInfoList =
            new ArrayList<FundingSourceInfo>();
    private Map<Integer, FundingSourceInfo> integerToFundingSourceMap =
            Maps.newHashMap();

    class DtoStudyStatus {
        String shortName = "";

        DtoStudyStatus(StudyStatus studyStatus1) {
            if (studyStatus1 == null) {
                return;
            }

            this.shortName = studyStatus1.getShortName();
        }
    }
    class DtoInstitution {
        String longName = "";

        DtoInstitution(Institution institution1) {
            if (institution1 == null) {
                return;
            }

            this.longName = institution1.getLongName();
        }
    }
    class DtoIrbInstitution {
        String name = "";

        DtoIrbInstitution(IRBInstitution irbInstitution1) {
            if (irbInstitution1 == null) {
                return;
            }

            this.name = irbInstitution1.getName();
        }
    }
    class User4 {
        String firstName = "";
        String middleName = "";
        String lastName = "";
        String email = "";
        String primaryPhone = "";
        String pager = "";

        User4(User user) {
            if (user == null) {
                return;
            }

            this.firstName = user.getFirstName();
            this.middleName = user.getMiddleName();
            this.lastName = user.getLastName();
            this.email = user.getEmail();
            this.primaryPhone = user.getPrimaryPhone();
            this.pager = user.getPager();
        }
    }

    class User3 {
        String firstName = "";
        String middleName = "";
        String lastName = "";
        String email = "";
        String primaryPhone = "";

        User3(User user) {
            if (user == null) {
                return;
            }

            this.firstName = user.getFirstName();
            this.middleName = user.getMiddleName();
            this.lastName = user.getLastName();
            this.email = user.getEmail();
            this.primaryPhone = user.getPrimaryPhone();
        }
    }

    class User1 {
        String firstName = "";
        String middleName = "";
        String lastName = "";

        User1(User user) {
            if (user == null) {
                return;
            }

            this.firstName = user.getFirstName();
            this.middleName = user.getMiddleName();
            this.lastName = user.getLastName();
        }
    }

    public StudyDataReportResponseDTO(Study study, Set<StudyFundingSource> sfsSet) {

        if (study == null) {
            SchedulerRuntimeException.logAndThrow("Study parameter should be non-null"); // too bad
        }

        this.catalystId = study.getCatalystId();
        this.localId = study.getLocalId();
        this.irb = study.getIrb();
        this.irbExpiration = study.getIrbExpiration();
        this.irbRenewalDate = study.getIrbRenewalDate();
        this.irbApprovalDate = study.getIrbApprovalDate();

        this.statusChange = study.getStatusChange();
        this.firstVisitDate = study.getFirstVisitDate();
        this.lastScheduledVisitDate = study.getLastScheduledVisitDate();

        this.name = study.getName();
        this.industryInitiated = study.getIndustryInitiated();
        this.totalSubjects = study.getTotalSubjects();
        this.totalOutpatientVisits = study.getTotalOutpatientVisits();

        for (StudyFundingSource sfs : sfsSet) {
            FundingSourceInfo fsi = new FundingSourceInfo(sfs);
            this.fundingSourceInfoList.add(fsi);
            this.integerToFundingSourceMap.put(fsi.getOneToFour(), fsi);
        }

        // instantiate instances of other local classes
        this.studyStatus = new DtoStudyStatus(study.getStudyStatus());
        this.institution = new DtoInstitution(study.getInstitution());
        this.irbInstitution = new DtoIrbInstitution(study.getIrbInstitution());
        this.investigator = new User4(study.getInvestigator());
        this.physician = new User4(study.getPhysician());
        this.physician2 = new User4(study.getPhysician2());
        this.scheduler = new User3(study.getScheduler());
        this.secondaryScheduler = new User3(study.getSecondaryScheduler());
        this.scheduler3 = new User3(study.getScheduler3());
        this.protocolNurse = new User1(study.getProtocolNurse());
        this.associateNurse = new User1(study.getAssociateNurse());
        this.protocolNutritionist = new User1(study.getProtocolNutritionist());
        this.associateProtocolNutritionist = new User1(study.getAssociateProtocolNutritionist());

        this.expectedStartDate = study.getExpectedStartDate();
        this.expectedEndDate = study.getExpectedEndDate();
        this.totalInpatientVisits = study.getTotalInpatientVisits();
        this.crcCategory = study.getCrcCategory();
        this.crcFunded = study.getCrcFunded();
    }

    @Override
    public String toCsvHeaders() {
        return  "Study Name," +
                "Institution," +
                "Local ID," +
                "Catalyst ID," +
                "Status," +
                "Last Status Change," +
                "IRB #," +
                "IRB Institution," +
                "IRB Approval Date," +
                "IRB Renewal Date," +
                "IRB Expiration Date," +
                "CRC Approved," +
                "CRC Category," +
                "Industry Initiated," +
                "Start Date," +
                "End Date," +
                "First Visit Date," +
                "Last Scheduled Visit Date," +
                "Total Inpatient Visits," +
                "Total Outpatient Visits," +
                "Total Subjects," +

                "Funding Source 1," +
                "Funding Centers and Institutes 1," +
                "Funding Grant ID 1," +
                "Funding Site Cost Center 1," +
                "Funding Total Direct Dollar Award 1," +
                "Funding Total Indirect Dollar Award 1," +
                "Funding Project Start Date 1," +
                "Funding Project End Date 1," +
                "Funding Comment 1," +

                "Funding Source 2," +
                "Funding Centers and Institutes 2," +
                "Funding Grant ID 2," +
                "Funding Site Cost Center 2," +
                "Funding Total Direct Dollar Award 2," +
                "Funding Total Indirect Dollar Award 2," +
                "Funding Project Start Date 2," +
                "Funding Project End Date 2," +
                "Funding Comment 2," +

                "Funding Source 3," +
                "Funding Centers and Institutes 3," +
                "Funding Grant ID 3," +
                "Funding Site Cost Center 3," +
                "Funding Total Direct Dollar Award 3," +
                "Funding Total Indirect Dollar Award 3," +
                "Funding Project Start Date 3," +
                "Funding Project End Date 3," +
                "Funding Comment 3," +

                "Funding Source 4," +
                "Funding Centers and Institutes 4," +
                "Funding Grant ID 4," +
                "Funding Site Cost Center 4," +
                "Funding Total Direct Dollar Award 4," +
                "Funding Total Indirect Dollar Award 4," +
                "Funding Project Start Date 4," +
                "Funding Project End Date 4," +
                "Funding Comment 4," +

                "PI Name," +
                "PI Email," +
                "PI Phone #1," +
                "PI Pager," +
                "Primary Scheduling Contact," +
                "Primary Scheduling Contact Email," +
                "Primary Scheduling Contact Phone," +
                "Secondary Scheduling Contact," +
                "Secondary Scheduling Contact Email," +
                "Secondary Scheduling Contact Phone," +
                "Third Scheduling Contact," +
                "Third Scheduling Contact Email," +
                "Third Scheduling Contact Phone," +
                "Responsible Physician 1," +
                "Responsible Physician 1 email," +
                "Responsible Physician 1 Phone#1," +
                "Responsible Physician 1 Pager," +
                "Responsible Physician 2," +
                "Responsible Physician 2 Email," +
                "Responsible Physician 2 Phone#1," +
                "Responsible Physician 2 Pager," +
                "Protocol Nurse," +
                "Associate Protocol Nurse," +
                "Protocol Nutritionist," +
                "Associate Protocol Nutritionist";
    }

    void addFundingSourceColumns(
            List<String> columns, StudyDataReportResponseDTO s) {

        for (int i=1; i<=4; i++) {
            FundingSourceInfo fundingSourceInfo = 
                    s.integerToFundingSourceMap.get(i);
            if (fundingSourceInfo == null) {
                fundingSourceInfo = 
                        FundingSourceInfo.makeBlankFundingSourceInfo();
            }

            columns.add(q(fundingSourceInfo.getName()));
            columns.add(q(fundingSourceInfo.getCenterAndInstitutionName()));
            columns.add(q(fundingSourceInfo.getGrant()));
            columns.add(q(fundingSourceInfo.getCenter()));
            columns.add(q(fundingSourceInfo.getDirectAward()));
            columns.add(q(fundingSourceInfo.getIndirectAward()));
            columns.add(q(showDate(fundingSourceInfo.getStart())));
            columns.add(q(showDate(fundingSourceInfo.getEnd())));
            columns.add(q(fundingSourceInfo.getComment()));
        }
    }

    @Override
    public List<String> toCsvRows(List<?> dtoList) {
        List<String> result = Lists.newArrayList();
        result.add(toCsvHeaders() + "\n");

        for (Object object : dtoList) {
            StudyDataReportResponseDTO s = (StudyDataReportResponseDTO) object;

            List<String> columns = Lists.newArrayList();

            columns.add(q(s.name));
            columns.add(q(s.institution.longName));
            columns.add(q(s.localId));
            columns.add(q(s.catalystId));
            columns.add(q(s.studyStatus.shortName));
            columns.add(q(showDateTime(s.statusChange)));
            columns.add(q(s.irb));
            columns.add(q(s.irbInstitution.name));
            columns.add(q(showDate(s.irbApprovalDate)));
            columns.add(q(showDate(s.irbRenewalDate)));
            columns.add(q(showDate(s.irbExpiration)));
            columns.add(q(yesNoValue(s.crcFunded)));
            columns.add(q(s.crcCategory));
            columns.add(q(yesNoValue(s.industryInitiated)));
            columns.add(q(showDate(s.expectedStartDate)));
            columns.add(q(showDate(s.expectedEndDate)));
            columns.add(q(showDateTime(s.firstVisitDate)));
            columns.add(q(showDateTime(s.lastScheduledVisitDate)));
            columns.add(q(String.valueOf(s.totalInpatientVisits)));
            columns.add(q(String.valueOf(s.totalOutpatientVisits)));
            columns.add(q(String.valueOf(s.totalSubjects)));

            addFundingSourceColumns(columns, s);

            columns.add(q(fullName(
                    s.investigator.firstName,
                    s.investigator.middleName,
                    s.investigator.lastName)));
            columns.add(q(s.investigator.email));
            columns.add(q(s.investigator.primaryPhone));
            columns.add(q(s.investigator.pager));
            columns.add(q(fullName(
                    s.scheduler.firstName,
                    s.scheduler.middleName,
                    s.scheduler.lastName)));
            columns.add(q(s.scheduler.email));
            columns.add(q(s.scheduler.primaryPhone));
            columns.add(q(fullName(
                    s.secondaryScheduler.firstName,
                    s.secondaryScheduler.middleName,
                    s.secondaryScheduler.lastName)));
            columns.add(q(s.secondaryScheduler.email));
            columns.add(q(s.secondaryScheduler.primaryPhone));
            columns.add(q(fullName(
                    s.scheduler3.firstName,
                    s.scheduler3.middleName,
                    s.scheduler3.lastName)));
            columns.add(q(s.scheduler3.email));
            columns.add(q(s.scheduler3.primaryPhone));
            columns.add(q(fullName(
                    s.physician.firstName,
                    s.physician.middleName,
                    s.physician.lastName)));
            columns.add(q(s.physician.email));
            columns.add(q(s.physician.primaryPhone));
            columns.add(q(s.physician.pager));
            columns.add(q(fullName(
                    s.physician2.firstName,
                    s.physician2.middleName,
                    s.physician2.lastName)));
            columns.add(q(s.physician2.email));
            columns.add(q(s.physician2.primaryPhone));
            columns.add(q(s.physician2.pager));
            columns.add(q(fullName(
                    s.protocolNurse.firstName,
                    s.protocolNurse.middleName,
                    s.protocolNurse.lastName)));
            columns.add(q(fullName(
                    s.associateNurse.firstName,
                    s.associateNurse.middleName,
                    s.associateNurse.lastName)));
            columns.add(q(fullName(
                    s.protocolNutritionist.firstName,
                    s.protocolNutritionist.middleName,
                    s.protocolNutritionist.lastName)));
            columns.add(q(fullName(
                    s.associateProtocolNutritionist.firstName,
                    s.associateProtocolNutritionist.middleName,
                    s.associateProtocolNutritionist.lastName)));

            String rows = Joiner.on(",").join(columns);
            result.add(rows + "\n");
        }
        return result;
    }

}

