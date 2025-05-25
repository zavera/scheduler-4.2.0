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
import edu.harvard.catalyst.scheduler.dto.request.*;
import edu.harvard.catalyst.scheduler.dto.response.*;
import edu.harvard.catalyst.scheduler.entity.LineLevelAnnotations;
import edu.harvard.catalyst.scheduler.entity.Resource;
import edu.harvard.catalyst.scheduler.entity.SublocationClosureInterval;
import edu.harvard.catalyst.scheduler.entity.User;
import edu.harvard.catalyst.scheduler.security.AuthorizedRoles;
import edu.harvard.catalyst.scheduler.service.ResourceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.inject.Singleton;
import javax.servlet.ServletContext;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import java.util.List;

import static edu.harvard.catalyst.scheduler.entity.InstitutionRoleType.*;

/**
 * @author Bill Simons
 * @date 4/15/13
 * @link http://cbmi.med.harvard.edu
 * @link http://chip.org
 */

@Path("/resource")
@Singleton
@Component
public class ResourceResource extends SecuredResource {
    private ResourceService resourceService;

    @Context
    private ServletContext servletContext;

    @Autowired
    public ResourceResource(ResourceService resourceService) {
        this.resourceService = resourceService;
    }

    //Don't use - Needed for spring security cglib proxying
    ResourceResource() {
    }

    @GET
    @Path("/getResourceAnnotations")
    @AuthorizedRoles({ROLE_SUPER_ADMIN, ROLE_RESOURCE_MANAGER, ROLE_SCHEDULER, ROLE_STUDY_STAFF, ROLE_FRONT_DESK, ROLE_GENERAL_VIEW})
    public String getResourceAnnotations(@QueryParam("resourceId") int resourceId) {
        List<LineLevelAnnotations> resourcesAnnotations = getResourceLineLevelAnnotationses(resourceId);
        GetResourcesDTO getResourcesDTO = new GetResourcesDTO();
        getResourcesDTO.setResourceAnnotationsValues(resourcesAnnotations);
        return gson.toJson(getResourcesDTO);
    }

    @GET
    @Path("/getSelectedAnnotations")
    @AuthorizedRoles({ROLE_SUPER_ADMIN, ROLE_RESOURCE_MANAGER, ROLE_SCHEDULER, ROLE_STUDY_STAFF, ROLE_FRONT_DESK})
    public String getSelectedAnnotations(@QueryParam("selectedAnnotations") String annotations, @QueryParam("mode") String mode, @QueryParam("resourceId") int resourceId) {
        List<LineLevelAnnotations> selectedResourcesAnnotations = getSelectedResourceLineLevelAnnotationses(annotations, mode, resourceId);
        GetResourcesDTO getResourcesDTO = new GetResourcesDTO();
        getResourcesDTO.setAnnotationsStore(selectedResourcesAnnotations);
        return gson.toJson(getResourcesDTO);
    }

    public List<LineLevelAnnotations> getSelectedResourceLineLevelAnnotationses(String annotations, String mode, int resourceId) {
        return resourceService.getSelectedResourceAnnotations(annotations, mode, resourceId);
    }

    @GET
    @Path("/getBookedResourceAnnotations")
    @AuthorizedRoles({ROLE_SUPER_ADMIN, ROLE_RESOURCE_MANAGER, ROLE_SCHEDULER, ROLE_STUDY_STAFF, ROLE_FRONT_DESK})
    public String getBookedResourceAnnotations(@QueryParam("resourceId") int templateResourceId) {
        List<LineLevelAnnotations> selectedResourcesAnnotations = resourceService.getBookedResourceAnnotations(templateResourceId);
        GetResourcesDTO getResourcesDTO = new GetResourcesDTO();
        getResourcesDTO.setAnnotationsStore(selectedResourcesAnnotations);
        return gson.toJson(getResourcesDTO);
    }

    @POST
    @Path("/createSublocationClosureInterval")
    @AuthorizedRoles({ROLE_SUPER_ADMIN, ROLE_RESOURCE_MANAGER, ROLE_SCHEDULER})
    public String createSublocationClosureInterval(@FormParam("data") String data) {
        SublocationClosureIntervalDTO sublocationClosureIntervalDTO = gson.fromJson(data, SublocationClosureIntervalDTO.class);
        SublocationClosureInterval interval = resourceService.createSublocationClosureInterval(sublocationClosureIntervalDTO, getUser(), getRemoteHost());
        return gson.toJson(interval);
    }

    @POST
    @Path("/assignResourceAlternatives")
    @AuthorizedRoles({ROLE_SUPER_ADMIN, ROLE_RESOURCE_MANAGER})
    public String assignResourceAlternatives(@FormParam("data") String data) {
        AssignResourceAlternativesDTO assignResourceAlternativesDTO = gson.fromJson(data, AssignResourceAlternativesDTO.class);
        BooleanResultDTO result = resourceService.addResourceAlternatives(assignResourceAlternativesDTO, getUser(), getRemoteHost());
        return gson.toJson(result);
    }

    @POST
    @Path("/deleteSublocationClosureInterval")
    @AuthorizedRoles({ROLE_SUPER_ADMIN, ROLE_RESOURCE_MANAGER, ROLE_SCHEDULER})
    public String deleteSublocationClosureInterval(@FormParam("data") String data) {
        SublocationClosureIntervalDTO sublocationClosureIntervalDTO = gson.fromJson(data, SublocationClosureIntervalDTO.class);
        BooleanResultDTO result = resourceService.deleteSublocationClosureInterval(sublocationClosureIntervalDTO, getUser(), getRemoteHost());
        return gson.toJson(result);
    }

    @GET
    @Path("/getRoomResources")
    @AuthorizedRoles({ROLE_SUPER_ADMIN, ROLE_RESOURCE_MANAGER, ROLE_SCHEDULER, ROLE_STUDY_STAFF, ROLE_FRONT_DESK, ROLE_GENERAL_VIEW})
    public String getRoomResources(@QueryParam("sublocation") String sublocations) {
        List<Resource> resources = resourceService.getRoomResources(sublocations);
        return gson.toJson(resources);
    }

    @GET
    @Path("/getNursingResources")
    @AuthorizedRoles({ROLE_SUPER_ADMIN, ROLE_RESOURCE_MANAGER, ROLE_SCHEDULER, ROLE_STUDY_STAFF, ROLE_FRONT_DESK, ROLE_GENERAL_VIEW})
    public String getNursingResources(@QueryParam("sublocation") String sublocations) {
        List<Resource> resources = resourceService.getNursingResources(getRemoteHost(), getUser(), sublocations);
        return gson.toJson(resources);
    }

    @GET
    @Path("/getNutritionResources")
    @AuthorizedRoles({ROLE_SUPER_ADMIN, ROLE_RESOURCE_MANAGER, ROLE_SCHEDULER, ROLE_STUDY_STAFF, ROLE_FRONT_DESK, ROLE_GENERAL_VIEW})
    public String getNutritionResources(@QueryParam("sublocation") String sublocations) {
        List<Resource> resources = resourceService.getNutritionResources(getRemoteHost(), getUser(), sublocations);
        return gson.toJson(resources);
    }

    ///////////////////////////////////////////////////methods using new DTOs/////////////////////////////////////////////////////////////
    @GET
    @Path("/getResourcesData")
    @AuthorizedRoles({ROLE_SUPER_ADMIN, ROLE_RESOURCE_MANAGER, ROLE_SCHEDULER, ROLE_STUDY_STAFF, ROLE_FRONT_DESK, ROLE_GENERAL_VIEW})
    public String getResourcesData(@QueryParam("sortBy") String sortBy,
                                   @QueryParam("orderBy") String orderBy,
                                   @QueryParam("status") String status,
                                   @QueryParam("page") int page,
                                   @QueryParam("maxResults") int maxResults,
                                   @QueryParam("search") String searchJson) {
        SearchDTO searchDTO = gson.fromJson(searchJson, SearchDTO.class);

        List<ResourcesResponse> resources = getResourceData(sortBy, orderBy, status, page, maxResults, getUser(), getRemoteHost(), searchDTO);
        return gson.toJson(resources);
    }

    List<ResourcesResponse> getResourceData(String sortBy,
                                            String orderBy,
                                            String status,
                                            int page,
                                            int maxResults,
                                            User user,
                                            String ipAddress,
                                            SearchDTO searchDTO) {
        return resourceService.getResourcesWithSublocation(sortBy, orderBy, status, page, maxResults, user, ipAddress, searchDTO);
    }

    @GET
    @Path("/getResourceSchedules")
    @AuthorizedRoles({ROLE_SUPER_ADMIN, ROLE_RESOURCE_MANAGER, ROLE_SCHEDULER, ROLE_STUDY_STAFF, ROLE_FRONT_DESK, ROLE_GENERAL_VIEW})
    public String getResourceSchedules(@QueryParam("resourceId") int resourceId, @QueryParam("sortBy") String sortBy, @QueryParam("orderBy") String orderBy,
            @QueryParam("page") int page, @QueryParam("maxResults") int maxResults) {
        List<ResourceScheduleResponse> resourceSchedules = getResourcesSchedule(resourceId, false, sortBy, orderBy, page, maxResults);
        return gson.toJson(resourceSchedules);
    }

    List<ResourceScheduleResponse> getResourcesSchedule(int resourceId, boolean override,
            String sortBy, String orderBy, int page, int maxResults) {
        return resourceService.getResourceSchedules(resourceId, override, sortBy, orderBy, page, maxResults);
    }

    @GET
    @Path("/getDefaultSchedule")
    @AuthorizedRoles({ROLE_SUPER_ADMIN, ROLE_RESOURCE_MANAGER, ROLE_SCHEDULER, ROLE_STUDY_STAFF, ROLE_FRONT_DESK, ROLE_GENERAL_VIEW})
    public String getDefaultSchedule(@QueryParam("id") int id) {
        ResourceScheduleResponse resourceSchedules = getDefaultScheduleById(id);
        return gson.toJson(resourceSchedules);
    }

    ResourceScheduleResponse getDefaultScheduleById(int id) {
        return resourceService.getResourceDefaultSchedule(id);
    }

    @GET
    @Path("/getResourceDetail")
    @AuthorizedRoles({ROLE_SUPER_ADMIN, ROLE_RESOURCE_MANAGER, ROLE_SCHEDULER, ROLE_STUDY_STAFF, ROLE_FRONT_DESK, ROLE_GENERAL_VIEW})
    public String getResourceDetail(@QueryParam("resourceId") int resourceId) {
        ResourcesResponse resource = loadResourceDetail(resourceId, getUser(), getRemoteHost());
        return gson.toJson(resource);
    }

    ResourcesResponse loadResourceDetail(int resourceId, User user, String ipAddress) {
        return resourceService.getResourceDetail(resourceId, user, ipAddress);
    }

    // Returns the list of resources which are *CURRENTLY* used as alternatives for the resource with the specified ID
    @GET
    @Path("/getResourceAlternates")
    @AuthorizedRoles({ROLE_SUPER_ADMIN, ROLE_RESOURCE_MANAGER, ROLE_SCHEDULER, ROLE_STUDY_STAFF, ROLE_FRONT_DESK, ROLE_GENERAL_VIEW})
    public String getResourceAlternates(@QueryParam("resourceId") int resourceId,
                                        @QueryParam("sortBy") String sortBy,
                                        @QueryParam("orderBy") String orderBy,
                                        @QueryParam("page") int page,
                                        @QueryParam("maxResults") int maxResults) {
        List<ResourcesResponse> resourceAlternates = loadResourceAlternateList(resourceId, sortBy, orderBy, page, maxResults);
        ResponseWithCount response = new ResponseWithCount();
        // TODO-XH: once the new "count" property is made to work, remove the totalCount from the individual items
        if (resourceAlternates.size() > 0) {
            response.setCount(resourceAlternates.get(0).getTotalCount());
        }
        else {
            response.setCount(0);
        }
        response.setJsonData(gson.toJson(resourceAlternates));
        return gson.toJson(response);
    }

    List<ResourcesResponse> loadResourceAlternateList(int resourceId,
            String sortBy, String orderBy, int page, int maxResults) {
        return resourceService.getResourceAlternates(resourceId, sortBy, orderBy, page, maxResults);
    }

    // Returns the list of all resources which may be set to Alternative Resources for the resource with the specified ID
    @GET
    @Path("/getAlternateResources")
    @AuthorizedRoles({ROLE_SUPER_ADMIN, ROLE_RESOURCE_MANAGER, ROLE_SCHEDULER, ROLE_STUDY_STAFF, ROLE_FRONT_DESK, ROLE_GENERAL_VIEW})
    public String getAlternateResources(@QueryParam("resourceId") int resourceId,
                                        @QueryParam("sortBy") String sortBy,
                                        @QueryParam("orderBy") String orderBy,
                                        @QueryParam("page") int page,
                                        @QueryParam("maxResults") int maxResults) {
        List<ResourcesResponse> resources = loadAddAlternateResources(resourceId, sortBy, orderBy, page, maxResults);
        return gson.toJson(resources);
    }

    List<ResourcesResponse> loadAddAlternateResources(int resourceId, String sortBy, String orderBy, int page, int maxResults) {
        return resourceService.getAlternateResources(resourceId, sortBy, orderBy, page, maxResults);
    }

    @GET
    @Path("/getTemporaryAdjustments")
    @AuthorizedRoles({ROLE_SUPER_ADMIN, ROLE_RESOURCE_MANAGER, ROLE_SCHEDULER, ROLE_STUDY_STAFF, ROLE_FRONT_DESK, ROLE_GENERAL_VIEW})
    public String getTemporaryAdjustments(@QueryParam("resourceId") int resourceId, @QueryParam("sortBy") String sortBy, @QueryParam("orderBy") String orderBy,
            @QueryParam("page") int page, @QueryParam("maxResults") int maxResults) {
        List<ResourceScheduleResponse> resourceSchedules = getResourcesSchedule(resourceId, true, sortBy, orderBy, page, maxResults);
        return gson.toJson(resourceSchedules);
    }

    @GET
    @Path("/getTemporaryAdjustment")
    @AuthorizedRoles({ROLE_SUPER_ADMIN, ROLE_RESOURCE_MANAGER, ROLE_SCHEDULER, ROLE_STUDY_STAFF, ROLE_FRONT_DESK, ROLE_GENERAL_VIEW})
    public String getTemporaryAdjustment(@QueryParam("id") int id) {
        ResourceScheduleResponse resourceSchedules = resourceService.getResourceDefaultSchedule(id);
        return gson.toJson(resourceSchedules);
    }

    @GET
    @Path("/getSublocationClosureIntervals")
    @AuthorizedRoles({ROLE_SUPER_ADMIN, ROLE_RESOURCE_MANAGER, ROLE_SCHEDULER})
    public String getSublocationClosureIntervals(@QueryParam("sortBy") String sortBy, @QueryParam("orderBy") String orderBy,
            @QueryParam("page") int page, @QueryParam("maxResults") int maxResults) {
        List<SublocationClosureIntervalResponse> sublocationClosureIntervals = loadSublocationIntervals(sortBy, orderBy, page, maxResults);
        return gson.toJson(sublocationClosureIntervals);
    }

    List<SublocationClosureIntervalResponse> loadSublocationIntervals(String sortBy, String orderBy, int page, int maxResults) {
        return resourceService.getSublocationClosureIntervals(sortBy, orderBy, page, maxResults);
    }

    @POST
    @Path("/modifyResource")
    @AuthorizedRoles({ROLE_SUPER_ADMIN, ROLE_RESOURCE_MANAGER})
    public String updateResource(@FormParam("data") String data) {
        ModifyResourceRequest modifyResourceRequest = gson.fromJson(data, ModifyResourceRequest.class);
        StatusAndMessageResponseDTO result = resourceService.updateResource(modifyResourceRequest, getUser(), getRemoteHost());
        return gson.toJson(result);
    }

    @POST
    @Path("/createResource")
    @AuthorizedRoles({ROLE_SUPER_ADMIN, ROLE_RESOURCE_MANAGER})
    public String createResource(@FormParam("data") String data) {
        final CreateResourceRequestDTO createResourceRequestDTO = gson.fromJson(data, CreateResourceRequestDTO.class);
        final CreateResourceResponse result = resourceService.createResource(createResourceRequestDTO, getUser(), getRemoteHost());
        return gson.toJson(result);
    }

    @GET
    @Path("/getResourceAnnotationsNames")
    @AuthorizedRoles({ROLE_SUPER_ADMIN, ROLE_RESOURCE_MANAGER, ROLE_SCHEDULER, ROLE_STUDY_STAFF, ROLE_FRONT_DESK, ROLE_GENERAL_VIEW})
    public String getResourceAnnotationsNames(@QueryParam("resourceId") int resourceId) {
        List<LineLevelAnnotations> resourcesAnnotations = getResourceLineLevelAnnotationses(resourceId);
        AnnotationsNamesResponseDTO resultDTO = AnnotationsNamesResponseDTO.fromAnnotationsList(resourcesAnnotations);
        return gson.toJson(resultDTO);
    }

    public List<LineLevelAnnotations> getResourceLineLevelAnnotationses(int resourceId) {
        return resourceService.getResourceAnnotations(resourceId);
    }

    @GET
    @Path("/getSelectedAnnotationsNQC")
    @AuthorizedRoles({ROLE_SUPER_ADMIN, ROLE_RESOURCE_MANAGER, ROLE_SCHEDULER, ROLE_STUDY_STAFF, ROLE_FRONT_DESK})
    public String getSelectedAnnotationsNQC(@QueryParam("selectedAnnotations") String annotations, @QueryParam("mode") String mode, @QueryParam("resourceId") int resourceId) {
        List<LineLevelAnnotations> selectedResourcesAnnotations = getSelectedResourceLineLevelAnnotationses(annotations, mode, resourceId);
        AnnotationsNamesQuantityCommentResponseDTO resultDTO = AnnotationsNamesQuantityCommentResponseDTO.fromAnnotationsList(selectedResourcesAnnotations);
        return gson.toJson(resultDTO);
    }

    @POST
    @Path("/addDefaultAvailability")
    @AuthorizedRoles({ROLE_SUPER_ADMIN, ROLE_RESOURCE_MANAGER})
    public String addDefaultAvailability(@FormParam("data") String data) {
        ResourceScheduleRequest dto = gson.fromJson(data, ResourceScheduleRequest.class);
        BooleanRequest result = resourceService.addDefaultAvailability(dto, getUser(), getRemoteHost());
        return gson.toJson(result);
    }

    @POST
    @Path("/updateDefaultAvailability")
    @AuthorizedRoles({ROLE_SUPER_ADMIN, ROLE_RESOURCE_MANAGER})
    public String updateDefaultAvailability(@FormParam("data") String data) {
        ResourceScheduleRequest dto = gson.fromJson(data, ResourceScheduleRequest.class);
        BooleanRequest result = resourceService.updateDefaultAvailability(dto, getUser(), getRemoteHost(), getContextRealPath());
        return gson.toJson(result);
    }

    @GET
    @Path("/deleteDefaultSchedule")
    @AuthorizedRoles({ROLE_SUPER_ADMIN, ROLE_RESOURCE_MANAGER})
    public String deleteDefaultSchedule(@QueryParam("id") int id) {
        BooleanRequest result = resourceService.deleteDefaultAvailability(id, getUser(), getRemoteHost());
        return gson.toJson(result);
    }

    @POST
    @Path("/addTemporaryAdjustment")
    @AuthorizedRoles({ROLE_SUPER_ADMIN, ROLE_RESOURCE_MANAGER})
    public String addTemporaryAdjustment(@FormParam("data") String data) {
        ResourceScheduleRequest dto = gson.fromJson(data, ResourceScheduleRequest.class);
        BooleanRequest result = resourceService.addTemporaryAdjustment(dto, getUser(), getRemoteHost());
        return gson.toJson(result);
    }

    @POST
    @Path("/updateTemporaryAdjustment")
    @AuthorizedRoles({ROLE_SUPER_ADMIN, ROLE_RESOURCE_MANAGER})
    public String updateTemporaryAdjustment(@FormParam("data") String data) {
        ResourceScheduleRequest dto = gson.fromJson(data, ResourceScheduleRequest.class);
        BooleanRequest result = resourceService.updateTemporaryAdjustment(dto, getUser(), getRemoteHost());
        return gson.toJson(result);
    }

    @GET
    @Path("/deleteTemporaryAdjustment")
    @AuthorizedRoles({ROLE_SUPER_ADMIN, ROLE_RESOURCE_MANAGER})
    public String deleteTemporaryAdjustment(@QueryParam("id") int id) {
        BooleanRequest result = resourceService.deleteTemporaryAdjustment(id, getUser(), getRemoteHost(), getContextRealPath());
        return gson.toJson(result);
    }

    @GET
    @Path("/removeResourceAlternative")
    @AuthorizedRoles({ROLE_SUPER_ADMIN, ROLE_RESOURCE_MANAGER})
    public String removeResourceAlternative(@QueryParam("resourceId") int resourceId, @QueryParam("alternateResourceId") int alternateResourceId) {
        BooleanRequest result = resourceService.deleteResourceAlternative(resourceId, alternateResourceId, getUser(), getRemoteHost());
        return gson.toJson(result);
    }

    @GET
    @Path("/changeResourceStatus")
    @AuthorizedRoles({ROLE_SUPER_ADMIN, ROLE_RESOURCE_MANAGER})
    public String changeResourceStatus(@QueryParam("resourceId") final int resourceId,
                                       @QueryParam("sublocationId") final int sublocationId,
                                       @QueryParam("active") boolean active)  {
        final BooleanResultDTO result = resourceService.changeResourceStatus(resourceId, sublocationId, active);
        return gson.toJson(result);
    }

    @POST
    @Path("/activateResources")
    @AuthorizedRoles({ROLE_SUPER_ADMIN, ROLE_RESOURCE_MANAGER})
    public String activateResources(@FormParam("data") final String data)  {
        final ResourceIdsRequestDTO dto = gson.fromJson(data, ResourceIdsRequestDTO.class);
        final ResourcesBooleanResponseDTO result = resourceService.activateResources(dto);
        return gson.toJson(result);
    }

    @GET
    @Path("/getResourcesAvailableForGenderBlockRestriction")
    @AuthorizedRoles({ROLE_SUPER_ADMIN, ROLE_RESOURCE_MANAGER, ROLE_SCHEDULER, ROLE_STUDY_STAFF, ROLE_FRONT_DESK, ROLE_GENERAL_VIEW})
    public String getResourcesAvailableForGenderBlockRestriction(@QueryParam("resourceId") int resourceId) {
        List<Resource> resources = resourceService.getResourcesAvailableForGenderBlockRestriction(resourceId);
        SimpleResourceListResponse resultDTO = SimpleResourceListResponse.fromResourcesList(resources);
        return gson.toJson(resultDTO);
    }

    @GET
    @Path("/removeRestriction")
    @AuthorizedRoles({ROLE_SUPER_ADMIN, ROLE_RESOURCE_MANAGER})
    public String removeRestriction(@QueryParam("resourceId") int resourceId) {
        BooleanRequest result = resourceService.deleteRestriction(resourceId, getUser(), getRemoteHost());
        return gson.toJson(result);
    }

    @POST
    @Path("/addRestriction")
    @AuthorizedRoles({ROLE_SUPER_ADMIN, ROLE_RESOURCE_MANAGER})
    public String addRestriction(@FormParam("data") final String data)  {
        AddOrModifyRestrictionRequestDTO requestDto = gson.fromJson(data, AddOrModifyRestrictionRequestDTO.class);
        BooleanRequest result = resourceService.addRestriction(requestDto, getUser(), getRemoteHost());
        return gson.toJson(result);
    }

    @POST
    @Path("/modifyRestriction")
    @AuthorizedRoles({ROLE_SUPER_ADMIN, ROLE_RESOURCE_MANAGER})
    public String modifyRestriction(@FormParam("data") final String data)  {
        AddOrModifyRestrictionRequestDTO requestDto = gson.fromJson(data, AddOrModifyRestrictionRequestDTO.class);
        BooleanRequest result = resourceService.modifyRestriction(requestDto, getUser(), getRemoteHost());
        return gson.toJson(result);
    }

    @GET
    @Path("/getAllResourceNames")
    @AuthorizedRoles({ROLE_SUPER_ADMIN, ROLE_RESOURCE_MANAGER})
    public String getAllResourceNames() {
        List<String> names = resourceService.getAllResourceNames(getUser(), getRemoteHost());
        return gson.toJson(names);
    }

    @POST
    @Path("/addAnnotations")
    @AuthorizedRoles({ROLE_SUPER_ADMIN, ROLE_RESOURCE_MANAGER})
    public String addAnnotations(@FormParam("data") String data) {
        final AnnotationsIdRequestDTO dto = gson.fromJson(data, AnnotationsIdRequestDTO.class);
        BooleanResultDTO result = resourceService.addAnnotations(dto, getUser(), getRemoteHost());
        return gson.toJson(result);
    }

    String getContextRealPath(){
        return servletContext.getRealPath("/WEB-INF");
    }

}
