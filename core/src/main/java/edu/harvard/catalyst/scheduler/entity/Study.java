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
package edu.harvard.catalyst.scheduler.entity;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static edu.harvard.catalyst.scheduler.core.Statics.NA;

@Entity
@Table(name = "study")
public class Study extends BaseEntity implements Serializable {
	private static final long serialVersionUID = 1L;
	private String catalystId;
	private String localId;
	private String spid;
	private String protocolNumber;
	private String name;
	private String shortTitle;
	private Institution institution;

	private StudyStatus studyStatus;
	private Date expectedStartDate;
	private Date expectedEndDate;

	private String irb;
	private Date irbExpiration;
	private Date irbRenewalDate;
	private Date irbApprovalDate;

	private Integer totalSubjects;
	private Integer totalOutpatientVisits;
	private Integer totalInpatientVisits;
	private Boolean industryInitiated;

	private String studyAbstract;
	private String clinicalTrial;
	private String newDrug;
	private String deviceExemption;

	private User investigator;
	private String investigatorName;
	private User physician;
	private User physician2;
	private User scheduler;
	private User secondaryScheduler;
	private User scheduler3;
	private User protocolNurse;
	private User associateNurse;
	private User protocolNutritionist;
	private User associateProtocolNutritionist;
	private String protocolNurseString;
	private String protocolNutritionistString;
	private Boolean crcFunded;

	private List<StudySubject> studySubjectList;
	private List<StudyUser> studyUserRoleList;

	private IRBInstitution irbInstitution;
	private String pediatric;
	private String crcCategory;
	private Date statusChange;
	private Date firstVisitDate;
	private Date lastScheduledVisitDate;

	private Set<StudyFundingSource> studyFundingSources = new HashSet<StudyFundingSource>();

	public Study() {
		super(null);
	}

	@Column(name = "catalyst_id")
	public String getCatalystId() {
		return catalystId;
	}

	public void setCatalystId(final String catalystId) {
		this.catalystId = catalystId;
	}

	@Column(name = "local_id")
	public String getLocalId() {
		return localId;
	}

	public void setLocalId(final String localId) {
		this.localId = localId;
	}

	@Column(name = "spid")
	public String getSpid() {
		return spid;
	}

	public void setSpid(final String spid) {
		this.spid = spid;
	}

	@Column(name = "protocol_number")
	public String getProtocolNumber() {
		return protocolNumber;
	}

	public void setProtocolNumber(final String protocolNumber) {
		this.protocolNumber = protocolNumber;
	}

	@Column(name = "irb")
	public String getIrb() {
		return irb;
	}

	public void setIrb(final String irb) {
		this.irb = irb;
	}

	@Column(name = "irb_expiration")
	public Date getIrbExpiration() {
		return irbExpiration;
	}

	public void setIrbExpiration(final Date irbExpiration) {
		this.irbExpiration = irbExpiration;
	}

	@Column(name = "irb_renewal_date")
	public Date getIrbRenewalDate() {
		return irbRenewalDate;
	}

	public void setIrbRenewalDate(final Date irbRenewalDate) {
		this.irbRenewalDate = irbRenewalDate;
	}

	@Column(name = "irb_approval_date")
	public Date getIrbApprovalDate() {
		return irbApprovalDate;
	}

	public void setIrbApprovalDate(final Date irbApprovalDate) {
		this.irbApprovalDate = irbApprovalDate;
	}

	@JoinColumn(name = "institution", referencedColumnName = "id")
	@ManyToOne
	public Institution getInstitution() {
		return institution;
	}

	public void setInstitution(final Institution institution) {
		this.institution = institution;
	}

	@Column(name = "name")
	public String getName() {
		return name;
	}

	public void setName(final String name) {
		this.name = name;
	}

	@Column(name = "short_title")
	public String getShortTitle() {
		return shortTitle;
	}

	public void setShortTitle(final String shortTitle) {
		this.shortTitle = shortTitle;
	}

	@JoinColumn(name = "study_status", referencedColumnName = "id")
	@ManyToOne
	public StudyStatus getStudyStatus() {
		return studyStatus;
	}

	public void setStudyStatus(final StudyStatus studyStatus) {
		this.studyStatus = studyStatus;
	}

	@Column(name = "industry_initiated")
	public Boolean getIndustryInitiated() {
		return industryInitiated;
	}

	public void setIndustryInitiated(final Boolean industryInitiated) {
		this.industryInitiated = industryInitiated;
	}

	@Column(name = "total_subjects")
	public Integer getTotalSubjects() {
		return totalSubjects;
	}

	public void setTotalSubjects(final Integer totalSubjects) {
		this.totalSubjects = totalSubjects;
	}

	@Column(name = "total_outpatient_visits")
	public Integer getTotalOutpatientVisits() {
		return totalOutpatientVisits;
	}

	public void setTotalOutpatientVisits(final Integer totalOutpatientVisits) {
		this.totalOutpatientVisits = totalOutpatientVisits;
	}

	@JoinColumn(name = "principal_investigator", referencedColumnName = "id")
	@ManyToOne
	public User getInvestigator() {
		return investigator;
	}

	public void setInvestigator(final User investigator) {
		this.investigator = investigator;
	}

	@Transient
	public String getInvestigatorLastNameOrNA() {
		if (investigator != null) {
			final String lastName = investigator.getLastName();

			if (lastName != null) {
				return lastName;
			}
		}

		return NA;
	}

	@JoinColumn(name = "responsible_physician", referencedColumnName = "id")
	@ManyToOne
	public User getPhysician() {
		return physician;
	}

	public void setPhysician(final User physician) {
		this.physician = physician;
	}

	@JoinColumn(name = "responsible_physician2", referencedColumnName = "id")
	@ManyToOne
	public User getPhysician2() {
		return physician2;
	}

	public void setPhysician2(final User physician2) {
		this.physician2 = physician2;
	}

	@JoinColumn(name = "primary_scheduling_contact", referencedColumnName = "id")
	@ManyToOne
	public User getScheduler() {
		return scheduler;
	}

	public void setScheduler(final User scheduler) {
		this.scheduler = scheduler;
	}

	@JoinColumn(name = "secondary_scheduling_contact", referencedColumnName = "id")
	@ManyToOne
	public User getSecondaryScheduler() {
		return secondaryScheduler;
	}

	public void setSecondaryScheduler(final User secondaryScheduler) {
		this.secondaryScheduler = secondaryScheduler;
	}

	@JoinColumn(name = "protocol_nurse", referencedColumnName = "id")
	@ManyToOne
	public User getProtocolNurse() {
		return protocolNurse;
	}

	public void setProtocolNurse(final User protocolNurse) {
		this.protocolNurse = protocolNurse;
	}

	@Column(name = "protocol_nurse_string")
	public String getProtocolNurseString() {
		return protocolNurseString;
	}

	public void setProtocolNurseString(final String protocolNurseString) {
		this.protocolNurseString = protocolNurseString;
	}

	@Column(name = "protocol_nutritionist_string")
	public String getProtocolNutritionistString() {
		return protocolNutritionistString;
	}

	public void setProtocolNutritionistString(final String protocolNutritionistString) {
		this.protocolNutritionistString = protocolNutritionistString;
	}

	@JoinColumn(name = "associate_nurse", referencedColumnName = "id")
	@ManyToOne
	public User getAssociateNurse() {
		return associateNurse;
	}

	public void setAssociateNurse(final User associateNurse) {
		this.associateNurse = associateNurse;
	}

	@JoinColumn(name = "protocol_nutritionist", referencedColumnName = "id")
	@ManyToOne
	public User getProtocolNutritionist() {
		return protocolNutritionist;
	}

	public void setProtocolNutritionist(final User protocolNutritionist) {
		this.protocolNutritionist = protocolNutritionist;
	}

	@JoinColumn(name = "associate_protocol_nutritionist", referencedColumnName = "id")
	@ManyToOne
	public User getAssociateProtocolNutritionist() {
		return associateProtocolNutritionist;
	}

	public void setAssociateProtocolNutritionist(final User associateProtocolNutritionist) {
		this.associateProtocolNutritionist = associateProtocolNutritionist;
	}

	@JoinColumn(name = "irb_institution", referencedColumnName = "id")
	@ManyToOne
	public IRBInstitution getIrbInstitution() {
		return irbInstitution;
	}

	public void setIrbInstitution(final IRBInstitution irbInstitution) {
		this.irbInstitution = irbInstitution;
	}

	@Column(name = "pediatric")
	public String getPediatric() {
		return pediatric;
	}

	public void setPediatric(final String pediatric) {
		this.pediatric = pediatric;
	}

	@Column(name = "expected_start_date")
	public Date getExpectedStartDate() {
		return expectedStartDate;
	}

	public void setExpectedStartDate(final Date expectedStartDate) {
		this.expectedStartDate = expectedStartDate;
	}

	@Column(name = "expected_end_date")
	public Date getExpectedEndDate() {
		return expectedEndDate;
	}

	public void setExpectedEndDate(final Date expectedEndDate) {
		this.expectedEndDate = expectedEndDate;
	}

	@Column(name = "total_inpatient_visits")
	public Integer getTotalInpatientVisits() {
		return totalInpatientVisits;
	}

	public void setTotalInpatientVisits(final Integer totalInpatientVisits) {
		this.totalInpatientVisits = totalInpatientVisits;
	}

	@Column(name = "crc_category")
	public String getCrcCategory() {
		return crcCategory;
	}

	public void setCrcCategory(final String crcCategory) {
		this.crcCategory = crcCategory;
	}

	@Column(name = "crc_funded")
	public Boolean getCrcFunded() {
		return crcFunded;
	}

	public void setCrcFunded(final Boolean crcFunded) {
		this.crcFunded = crcFunded;
	}

	@JoinColumn(name = "scheduler3", referencedColumnName = "id")
	@ManyToOne
	public User getScheduler3() {
		return scheduler3;
	}

	public void setScheduler3(final User scheduler3) {
		this.scheduler3 = scheduler3;
	}

	@Column(name = "study_abstract")
	public String getStudyAbstract() {
		return studyAbstract;
	}

	public void setStudyAbstract(final String studyAbstract) {
		this.studyAbstract = studyAbstract;
	}

	@Column(name = "clinical_trial")
	public String getClinicalTrial() {
		return clinicalTrial;
	}

	public void setClinicalTrial(final String clinicalTrial) {
		this.clinicalTrial = clinicalTrial;
	}

	@Column(name = "new_drug")
	public String getNewDrug() {
		return newDrug;
	}

	public void setNewDrug(final String newDrug) {
		this.newDrug = newDrug;
	}

	@Column(name = "device_exemption")
	public String getDeviceExemption() {
		return deviceExemption;
	}

	public void setDeviceExemption(final String deviceExemption) {
		this.deviceExemption = deviceExemption;
	}

	@Column(name = "status_change")
	public Date getStatusChange() {
		return statusChange;
	}

	public void setStatusChange(final Date statusChange) {
		this.statusChange = statusChange;
	}

	// using transient fields could be an alternative to detached,
	// defensive-copy, or dto
	@Transient
	public String getInvestigatorName() {
		return investigatorName;
	}

	public void setInvestigatorName(final String investigatorName) {
		this.investigatorName = investigatorName;
	}

	@Transient
	// DO NOT FETCH THIS DATA AS SUBJECTS DATA IS ENCRYPTED AND NEEDS TO BE
	// HANDLED SEPERATELY
	public List<StudySubject> getStudySubjectList() {
		return studySubjectList;
	}

	public void setStudySubjectList(final List<StudySubject> studySubjectList) {
		this.studySubjectList = studySubjectList;
	}

	@Transient
	public List<StudyUser> getStudyUserRoleList() {
		return studyUserRoleList;
	}

	public void setStudyUserRoleList(final List<StudyUser> studyUserRoleList) {
		this.studyUserRoleList = studyUserRoleList;
	}

	@Transient
	public Date getFirstVisitDate() {
		return firstVisitDate;
	}

	public void setFirstVisitDate(final Date firstVisitDate) {
		this.firstVisitDate = firstVisitDate;
	}

	@Transient
	public Date getLastScheduledVisitDate() {
		return lastScheduledVisitDate;
	}

	public void setLastScheduledVisitDate(final Date lastScheduledVisitDate) {
		this.lastScheduledVisitDate = lastScheduledVisitDate;
	}

	@OneToMany(fetch = FetchType.EAGER, mappedBy = "studyId")
	public Set<StudyFundingSource> getStudyFundingSources() {
		return studyFundingSources;
	}

	public void setStudyFundingSources(final Set<StudyFundingSource> studyFundingSources) {
		this.studyFundingSources = studyFundingSources;
	}

	@Override
	public String toString() {
		return "Study [id=" + id + ", getId()=" + getId() + "]";
	}
}
