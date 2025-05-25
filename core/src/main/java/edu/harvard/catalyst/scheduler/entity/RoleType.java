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

import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.core.GrantedAuthority;

/**
 * @author Anusha Chitla
 *
 */
public enum RoleType implements ConfigAttribute,GrantedAuthority, HasReportFiltersNameAndId {

	ROLE_STUDY_COORDINATOR_RESEARCH_ASST ("Study Coordinator Research Assistant"),
	ROLE_PI ("PI"),
	ROLE_CO_INVESTIGATOR ("Co-Investigator"),
	ROLE_OTHER_STUDY_STAFF ("Other Study Staff"),
	ROLE_NURSE ("Nurse"),
	ROLE_NUTRITIONIST ("Nutritionist"),
	ROLE_CRC_ADMINISTRATION ("CRC Administration"),
	ROLE_LAB ("Lab"),
	ROLE_ADMINISTRATIVE_DIRECTOR ("Administrative Director"),
	ROLE_NURSE_MANAGER_DIRECTOR ("Nurse Manager Director"),
	ROLE_NUTRITION_MANAGER ("Nutrition Manager"),
	ROLE_FINAL_APPROVER ("Final Approver"),
	ROLE_PHYSICIAN ("Physician"),
	ROLE_PHYSICIAN2 ("Physician 2"),
	ROLE_SCHEDULER ("Scheduler"),
	ROLE_SCHEDULER2 ("Scheduler 2"),
	ROLE_PROTOCOL_NURSE ("Protocol Nurse"),
	ROLE_ASSOCIATE_NURSE ("Associate Nurse"),
	ROLE_PROTOCOL_NUTRITIONIST ("Protocol Nutritionist"),
	ROLE_ASSOCIATE_RD ("Associate RD");

	private String reportFiltersName;

	@Override
	public int getReportFiltersId() {
		return ordinal();
	}
	@Override
	public String getReportFiltersName() {
		return reportFiltersName;
	}

	RoleType(String reportFiltersName) {
		this.reportFiltersName = reportFiltersName;
	}
	
	public String getAuthority() {
		return null;
	}

	public String getAttribute() {
		return null;
	}

}
