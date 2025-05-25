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

import edu.harvard.catalyst.scheduler.dto.*;
import edu.harvard.catalyst.scheduler.dto.request.BooleanRequest;
import edu.harvard.catalyst.scheduler.dto.request.VisitTemplatesRequest;
import edu.harvard.catalyst.scheduler.dto.response.*;
import edu.harvard.catalyst.scheduler.entity.*;
import edu.harvard.catalyst.scheduler.security.AuthorizedRoles;
import edu.harvard.catalyst.scheduler.service.StudyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.inject.Singleton;
import javax.servlet.ServletContext;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import java.util.List;
import java.util.Optional;

import static edu.harvard.catalyst.scheduler.entity.InstitutionRoleType.*;


/**
 * @author Bill Simons
 * @date 4/15/13
 * @link http://cbmi.med.harvard.edu
 * @link http://chip.org
 */

@Path("/study")
@Singleton
@Component
public class StudyResource extends SecuredResource {

    StudyService service;

    @Autowired
    public StudyResource(StudyService service) {
        this.service = service;
    }

    //Don't use - Needed for spring security cglib proxying
    StudyResource(){
    }

    @GET
    @Path("/getNonClosedStudiesList")
    @AuthorizedRoles({ROLE_SUPER_ADMIN, ROLE_RESOURCE_MANAGER, ROLE_SCHEDULER, ROLE_STUDY_STAFF, ROLE_FRONT_DESK})
    public String getNonClosedStudiesList()  {
        int userId = getUser().getId();
        GetStudiesResponse studies = getSubjectStudiesList(userId);
        return gson.toJson(studies);
    }

    public GetStudiesResponse getSubjectStudiesList(int userId)  {
        return service.getSubjectStudies(userId);
    }

    @GET
    @Path("/getTemplatesToApprove")
    @AuthorizedRoles({ROLE_SUPER_ADMIN, ROLE_RESOURCE_MANAGER, ROLE_SCHEDULER})
    public String getTemplatesToApprove(@QueryParam("sortBy") String sortBy, @QueryParam("orderBy") String orderBy, 
            @QueryParam("page") int page, @QueryParam("maxResults") int maxResults)  {
        List<VisitTemplatesResponse> approvalTemplates = templateApprovalList(sortBy, orderBy, page, maxResults, getUser().getId(), getRemoteHost());
        return gson.toJson(approvalTemplates);
    }

    List<VisitTemplatesResponse> templateApprovalList(String sortBy,
            String orderBy, int page, int maxResults, int user, String ipAddress)  {
        return service.getTemplatesToApprove(sortBy, orderBy, page, maxResults, user, ipAddress);
    }

    @GET
    @Path("/getVisitApprovals")
    @AuthorizedRoles({ROLE_SUPER_ADMIN, ROLE_RESOURCE_MANAGER, ROLE_SCHEDULER, ROLE_STUDY_STAFF, ROLE_FRONT_DESK})
    public String getVisitApprovals(@QueryParam("sortBy") String sortBy, @QueryParam("orderBy") String orderBy, 
                                    @QueryParam("page") int page, @QueryParam("maxResults") int maxResults, @QueryParam("visit") int visitId)  {
        List<VisitApprovalModelResponseDTO.VisitApproval> templateApprovers = getVisitApprovalsList(sortBy, orderBy, page, maxResults, visitId);
        return gson.toJson(templateApprovers);
    }

    List<VisitApprovalModelResponseDTO.VisitApproval> getVisitApprovalsList(String sortBy, String orderBy, int page, int maxResults, int visitId)  {
        return service.getVisitApprovals(visitId, sortBy, orderBy, page, maxResults);
    }

    @GET
    @Path("/getStudyMembersList")
    @AuthorizedRoles({ROLE_SUPER_ADMIN, ROLE_RESOURCE_MANAGER, ROLE_SCHEDULER, ROLE_STUDY_STAFF, ROLE_FRONT_DESK})
    public String getStudyMembersList(@QueryParam("studyId") int studyId)  {
        List<StudyUser> studyMembers = service.getStudyMembers(studyId);
        return gson.toJson(studyMembers);
    }

    @POST
    @Path("/addStudyMember")
    @AuthorizedRoles({ROLE_SUPER_ADMIN, ROLE_RESOURCE_MANAGER, ROLE_SCHEDULER, ROLE_STUDY_STAFF, ROLE_FRONT_DESK})
    public String addStudyMember(@FormParam("data") String data, @Context ServletContext context)  {
        AddStudyMembersDTO assignStudyMembersDTO = gson.fromJson(data, AddStudyMembersDTO.class);
        BooleanResultDTO result = service.assignStudyMembers(assignStudyMembersDTO, getUser(), getRemoteHost(), context.getRealPath("/WEB-INF"));
        return gson.toJson(result);
    }

    @POST
    @Path("/createStudy")
    @AuthorizedRoles(ROLE_SUPER_ADMIN)
    public String createStudy(@FormParam("data") String data)  {
        StudyDTO s = gson.fromJson(data, StudyDTO.class);
        s = service.createStudy(s, getUser(), getRemoteHost());
        return gson.toJson(s);
    }

    @POST
    @Path("/editstudy")
    @AuthorizedRoles({ROLE_SUPER_ADMIN, ROLE_RESOURCE_MANAGER})
    public String editstudy(@FormParam("data") String data)  {
        StudyDTO s = gson.fromJson(data, StudyDTO.class);
        s = service.updateStudy(s, getUser(), getRemoteHost());
        return gson.toJson(s);
    }

    @POST
    @Path("/commentVisitTemplate")
    @AuthorizedRoles({ROLE_SUPER_ADMIN, ROLE_RESOURCE_MANAGER, ROLE_SCHEDULER, ROLE_FRONT_DESK})
    public String commentVisitTemplate(@FormParam("data") String data)  {
        TemplateApprovalHistoryDTO v = gson.fromJson(data, TemplateApprovalHistoryDTO.class);
        TemplateApprovalHistory visit = service.commentVisitTemplate(v, getUser(), getRemoteHost());
        return gson.toJson(visit);
    }

    @POST
    @Path("/approveVisitTemplate")
    @AuthorizedRoles({ROLE_SUPER_ADMIN, ROLE_RESOURCE_MANAGER, ROLE_SCHEDULER, ROLE_FRONT_DESK})
    public String approveVisitTemplate(@FormParam("data") String data, @Context ServletContext context)  {
        TemplateApprovalHistoryDTO v = gson.fromJson(data, TemplateApprovalHistoryDTO.class);
        TemplateApprovalHistory visit = service.approveVisitTemplate(v, getUser(), getRemoteHost(), context.getRealPath("/WEB-INF"));
        return gson.toJson(visit);
    }

    @GET
    @Path("/getVisitTemplateData")
    @AuthorizedRoles({ROLE_SUPER_ADMIN, ROLE_RESOURCE_MANAGER, ROLE_SCHEDULER, ROLE_STUDY_STAFF, ROLE_FRONT_DESK})
    public String getVisitTemplateData(@QueryParam("visit") int visitId)  {
        VisitTemplateDetailResponse visit = loadVisitData(visitId, getUser(), getRemoteHost());
        return gson.toJson(visit);
    }

    VisitTemplateDetailResponse loadVisitData(int visitId, User user, String ipAddress)  {
        VisitTemplateDetailResponse visitTemplateDetailResponse = service.getVisitTemplateData(visitId, user, ipAddress);
        return visitTemplateDetailResponse;
    }
    
    @GET
    @Path("/getStudyData")
    @AuthorizedRoles({ROLE_SUPER_ADMIN, ROLE_RESOURCE_MANAGER, ROLE_SCHEDULER, ROLE_STUDY_STAFF, ROLE_FRONT_DESK})
    public String getStudyData(@QueryParam("study") int studyId)  {
        StudyDetailResponse study = loadStudyData(studyId, getUser(), getRemoteHost());
        return gson.toJson(study);
    }

    StudyDetailResponse loadStudyData(int studyId, User user, String ipAddress)  {
        return service.getStudyData(studyId, user, ipAddress);
    }

    @POST
    @Path("/changeVisitTemplateStatus")
    @AuthorizedRoles({ROLE_SUPER_ADMIN, ROLE_RESOURCE_MANAGER})
    public String changeVisitTemplateStatus(@FormParam("data") String data, @QueryParam("activate") boolean activate)  {
        VisitTemplatesRequest visitTemplatesRequest = gson.fromJson(data, VisitTemplatesRequest.class);
        BooleanResultDTO result = service.changeVisitTemplateStatus(visitTemplatesRequest, activate, getUser(), getRemoteHost());
        return gson.toJson(result);
    }
    
    @POST
    @Path("/createVisit")
    @AuthorizedRoles({ROLE_SUPER_ADMIN, ROLE_RESOURCE_MANAGER, ROLE_SCHEDULER, ROLE_FRONT_DESK})
    public String createVisit(@FormParam("data") String data)  {
        VisitDTO visitData = gson.fromJson(data, VisitDTO.class);
        VisitTemplate visit = service.createVisit(visitData, getUser(), getRemoteHost());
        return gson.toJson(visit);
    }

    @POST
    @Path("/editVisit")
    @AuthorizedRoles({ROLE_SUPER_ADMIN, ROLE_RESOURCE_MANAGER, ROLE_SCHEDULER, ROLE_FRONT_DESK})
    public String editVisit(@FormParam("data") String data)  {
        VisitDTO visitData = gson.fromJson(data, VisitDTO.class);
        VisitTemplate visit = service.updateVisit(visitData, getUser(), getRemoteHost());
        return gson.toJson(visit);
    }

    @POST
    @Path("/copyVisit")
    @AuthorizedRoles({ROLE_SUPER_ADMIN, ROLE_RESOURCE_MANAGER, ROLE_SCHEDULER})
    public String copyVisit(@FormParam("data") String data)  {
        VisitDTO visitData = gson.fromJson(data, VisitDTO.class);
        VisitTemplate visit = service.copyVisit(visitData, getUser(), getRemoteHost());
        return gson.toJson(visit);
    }
    
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @GET
    @Path("/getOpenStudies")
    @AuthorizedRoles({ROLE_SUPER_ADMIN, ROLE_RESOURCE_MANAGER, ROLE_SCHEDULER, ROLE_STUDY_STAFF, ROLE_FRONT_DESK})
    public String getOpenStudies(@QueryParam("filterString") String filterString,
                                 @QueryParam("sortBy") String sortBy,
                                 @QueryParam("orderBy") String orderBy,
                                 @QueryParam("page") int page,
                                 @QueryParam("maxResults") int maxResults) {
        GetStudiesResponse result = getOpenStudiesList(filterString, sortBy, orderBy, page, maxResults, getUser());
        return gson.toJson(result);
    }

    public GetStudiesResponse getOpenStudiesList(String filterString, String sortBy, String orderBy, int page, int maxResults, User user)  {
        return service.getOpenStudies(
                filterString, sortBy, orderBy, page, maxResults,
                user);
    }

    @GET
    @Path("/getStudyList")
    @AuthorizedRoles({ROLE_SUPER_ADMIN, ROLE_RESOURCE_MANAGER, ROLE_SCHEDULER, ROLE_STUDY_STAFF, ROLE_FRONT_DESK})
    public String getStudyList(@QueryParam("sortBy") String sortBy,
                               @QueryParam("orderBy") String orderBy,
                               @QueryParam("page") int page,
                               @QueryParam("maxResults") int maxResults,
                               @QueryParam("search") String search)  {
        SearchDTO searchDTO = gson.fromJson(search, SearchDTO.class);
        List<StudyDataResponse> studies = getStudyDataResponses(sortBy, orderBy, page, maxResults, getUser(), searchDTO);
        return gson.toJson(studies);
    }

    List<StudyDataResponse> getStudyDataResponses(String sortBy, String orderBy, int page, int maxResults, User user, SearchDTO searchDTO)  {
        return service.getStudiesList(sortBy, orderBy, page, maxResults, user, searchDTO);
    }

    @GET
    @Path("/getStudySubjects")
    @AuthorizedRoles({ROLE_SUPER_ADMIN, ROLE_RESOURCE_MANAGER, ROLE_SCHEDULER, ROLE_STUDY_STAFF, ROLE_FRONT_DESK})
    public String getStudySubjects(@QueryParam("filterString") String filterString,
                                   @QueryParam("page") int page,
                                   @QueryParam("maxResults") int maxResults,
                                   @QueryParam("study") int studyId,
                                   @QueryParam("inactive") boolean wantAll,
                                   @QueryParam("sortBy") String sortBy,
                                   @QueryParam("orderBy") String orderBy,
                                   @QueryParam("search") String search) {
        SearchDTO searchDTO = gson.fromJson(search, SearchDTO.class);

        GetStudySubjectsResponse result = getStudySubjectsResponse(filterString, page, maxResults, studyId, wantAll, sortBy, orderBy, getUser(), searchDTO);
        return gson.toJson(result);
    }

    GetStudySubjectsResponse getStudySubjectsResponse(String filterString, int page, int maxResults, int studyId, boolean wantAll, String sortBy, String orderBy, User user, SearchDTO searchDTO)  {
        return service.getStudySubjects(
                    Optional.ofNullable(filterString),
                    page,
                    maxResults,
                    Optional.of(studyId),
                    sortBy,
                    orderBy,
                    wantAll,
                    Optional.of(user),
                    searchDTO);
    }

    @GET
    @Path("/changeStudySubjectStatus")
    @AuthorizedRoles({ROLE_SUPER_ADMIN, ROLE_RESOURCE_MANAGER, ROLE_SCHEDULER, ROLE_STUDY_STAFF, ROLE_FRONT_DESK})
    public String changeStudySubjectStatus(@QueryParam("studySubjectId") int studySubjectId)  {
        BooleanRequest result = service.changeStudySubjectStatus(studySubjectId, getUser(), getRemoteHost());
        return gson.toJson(result);
    }
    
    @GET
    @Path("/getStudyMembers")
    @AuthorizedRoles({ROLE_SUPER_ADMIN, ROLE_RESOURCE_MANAGER, ROLE_SCHEDULER, ROLE_STUDY_STAFF, ROLE_FRONT_DESK})
    public String getStudyMembers(@QueryParam("sortBy") String sortBy, @QueryParam("orderBy") String orderBy, 
                                  @QueryParam("page") int page, @QueryParam("maxResults") int maxResults, 
                                  @QueryParam("study") int studyId, @QueryParam("search") String searchJson)  {

        SearchDTO searchDTO = gson.fromJson(searchJson, SearchDTO.class);

        List<UserDataResponse> studyMembers = service.getStudyMembersList(studyId, sortBy, orderBy, page, maxResults, searchDTO);
       return gson.toJson(studyMembers);
    }
    
    @GET
    @Path("/changeStudyMemberStatus")
    @AuthorizedRoles({ROLE_SUPER_ADMIN, ROLE_RESOURCE_MANAGER, ROLE_SCHEDULER, ROLE_STUDY_STAFF, ROLE_FRONT_DESK})
    public String changeStudyMemberStatus(@QueryParam("studyMemberId") int studyMemberId)  {
        BooleanRequest result = service.changeStudyMemberStatus(studyMemberId, getUser(), getRemoteHost());
        return gson.toJson(result);
    }
    
    @GET
    @Path("/deleteVisit")
    @AuthorizedRoles(ROLE_SUPER_ADMIN)
    public String deleteVisit(@QueryParam("visitId") int visitId)  {
        BooleanRequest result = service.deleteVisit(visitId, getUser(), getRemoteHost());
        return gson.toJson(result);
    }
    
    @GET
    @Path("/getVisitComments")
    @AuthorizedRoles({ROLE_SUPER_ADMIN, ROLE_RESOURCE_MANAGER, ROLE_SCHEDULER, ROLE_STUDY_STAFF, ROLE_FRONT_DESK})
    public String getVisitComments(@QueryParam("id") int visitId)  {
        List<Comments> visitTemplateComments = service.getVisitTemplateComments(visitId);
        List<VisitCommentsResponse.VisitComment> comments = VisitCommentsResponse.fromTemplateApprovalHistoryList(visitTemplateComments);
        return gson.toJson(comments);
    }
}
