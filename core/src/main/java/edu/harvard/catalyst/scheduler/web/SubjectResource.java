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
package edu.harvard.catalyst.scheduler.web;

import edu.harvard.catalyst.scheduler.dto.SubjectMrnDTO;
import edu.harvard.catalyst.scheduler.dto.SubjectsDTO;
import edu.harvard.catalyst.scheduler.dto.response.SubjectDetailResponse;
import edu.harvard.catalyst.scheduler.dto.response.SubjectsResponseDTO;
import edu.harvard.catalyst.scheduler.security.AuthorizedRoles;
import edu.harvard.catalyst.scheduler.service.SubjectService;
import edu.harvard.catalyst.scheduler.service.SubjectServiceClassic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import javax.inject.Singleton;
import javax.ws.rs.*;
import java.util.Date;
import java.util.List;

import static edu.harvard.catalyst.scheduler.entity.InstitutionRoleType.*;
import static edu.harvard.catalyst.scheduler.util.DateUtility.parse;
import static edu.harvard.catalyst.scheduler.util.DateUtility.subjectDate;
/**
 * @author Bill Simons
 * @date 4/15/13
 * @link http://cbmi.med.harvard.edu
 * @link http://chip.org
 */

@Path("/subject")
@Singleton
@Component
public class SubjectResource extends SecuredResource {
	private final SubjectServiceClassic service;
	private final SubjectService subjectService;
	private final boolean isEpicMode;

	@Autowired
	public SubjectResource(final SubjectServiceClassic service,
						   @Qualifier("subjectSSOTConfigured") final SubjectService subjectService,
						   @Qualifier("subjectMode") String subjectMode) {
		this.service = service;
		this.subjectService = subjectService;

		if(subjectMode != null)
		{
			isEpicMode = subjectMode.equalsIgnoreCase("epic");
		}
		else
		{
			isEpicMode = false;
		}

	}

	//Don't use - Needed for spring security cglib proxying
	SubjectResource() {
		this(null, null, null);
	}

	@POST
	@Path("/createSubject")
	@AuthorizedRoles({ROLE_SUPER_ADMIN, ROLE_RESOURCE_MANAGER, ROLE_SCHEDULER, ROLE_STUDY_STAFF, ROLE_FRONT_DESK})
	public String createSubject(@FormParam("data") final String data, @HeaderParam("User-Agent") final String header) {
		SubjectsDTO subjectsDTO = gson.fromJson(data, SubjectsDTO.class);
        subjectsDTO.nullifyBlanks();

        final Date birthdate = getFormattedBirthDate(subjectsDTO.getBirthday());
		subjectsDTO.setBirthdate(birthdate);
		service.createSubjectForDto(subjectsDTO, getUser(), getRemoteHost());

		return gson.toJson(subjectsDTO);
	}

	@POST
	@Path("/updateSubject")
	@AuthorizedRoles({ROLE_SUPER_ADMIN, ROLE_RESOURCE_MANAGER, ROLE_SCHEDULER, ROLE_STUDY_STAFF, ROLE_FRONT_DESK})
	public String updateSubject(@FormParam("data") final String data, @HeaderParam("User-Agent") final String header) {
		SubjectsDTO subjectsDTO = gson.fromJson(data, SubjectsDTO.class);
        subjectsDTO.nullifyBlanks();

		final Date birthdate = getFormattedBirthDate(subjectsDTO.getBirthday());
		subjectsDTO.setBirthdate(birthdate);

		final boolean allowOnlyOneMrn= !isEpicMode;
		subjectsDTO = service.updateSubject(subjectsDTO, getUser(), getRemoteHost(), allowOnlyOneMrn);
		return gson.toJson(subjectsDTO);
	}

	public Date getFormattedBirthDate(final String date) {
		return parse(subjectDate(), date);
	}

	/*
     * accessible at: http://localhost:8080/scheduler/rest/ssot/getSearchSubjects
     * with parameters: http://localhost:8080/scheduler/rest/ssot/getSearchSubjects?lastName=&mrn=&dob=&gender=&sortBy=&orderBy=&maxResults=&page=
     */
	@POST
	@Path("/ssot/getSearchSubjects")
	@AuthorizedRoles({ROLE_SUPER_ADMIN, ROLE_RESOURCE_MANAGER, ROLE_SCHEDULER, ROLE_STUDY_STAFF, ROLE_FRONT_DESK})
	public String getSearchSubjects(@FormParam("data") final String data) {

		SubjectsDTO subjectsDTO = gson.fromJson(data, SubjectsDTO.class);
		final String lastName = subjectsDTO.getLastName();
		final String firstName = subjectsDTO.getFirstName();
		final String mrn = subjectsDTO.getMrn();
		final String dob = subjectsDTO.getBirthday();
		final List<String> genderList = subjectsDTO.getGenderList();

		final SubjectsResponseDTO subjectsResponseDTO = subjectService.getSearchedSubjects(
				lastName,
				firstName,
				mrn,
				dob,
				genderList);
		return gson.toJson(subjectsResponseDTO);
	}

	@GET
	@Path("/getSubjectData")
	@AuthorizedRoles({ROLE_SUPER_ADMIN, ROLE_RESOURCE_MANAGER, ROLE_SCHEDULER, ROLE_STUDY_STAFF, ROLE_FRONT_DESK})
	public String getSubjectData(@QueryParam("isPuid") final Boolean isPuid,
                                 @QueryParam("data") final String data) {
		SubjectsDTO subjectsDTO = gson.fromJson(data, SubjectsDTO.class);

		final SubjectDetailResponse subject = subjectService.getSubjectData(isPuid, subjectsDTO.getId(), getRemoteHost(), getUser());
		return gson.toJson(subject);
	}

	@POST
	@Path("/getExternalSubjectDataUsingMrn")
	@AuthorizedRoles({ROLE_SUPER_ADMIN, ROLE_RESOURCE_MANAGER, ROLE_SCHEDULER, ROLE_STUDY_STAFF, ROLE_FRONT_DESK})
	public String getExternalSubjectDataUsingMrnInfo(@FormParam("data") final String data) {
		SubjectMrnDTO subjectMrnDTO = gson.fromJson(data, SubjectMrnDTO.class);

		final SubjectDetailResponse subject = subjectService.getExternalSubjectDataUsingMrn(subjectMrnDTO.getMrnInfo());
		return gson.toJson(subject);
	}

	@GET
	@Path("/findInternalSubjectByMrn")
	@AuthorizedRoles({ROLE_SUPER_ADMIN, ROLE_RESOURCE_MANAGER, ROLE_SCHEDULER, ROLE_STUDY_STAFF, ROLE_FRONT_DESK})
	public String findInternalSubjectByMrn(@QueryParam("mrnInfo") final String mrnInfoStr)
	{
		SubjectMrnDTO subjectsDTO = gson.fromJson(mrnInfoStr, SubjectMrnDTO.class);

		final SubjectDetailResponse result = subjectService.findInternalSubjectByMrn(subjectsDTO.getMrnInfoList());

		return gson.toJson(result);
	}
}