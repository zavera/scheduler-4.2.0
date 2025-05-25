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

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.reflect.TypeToken;
import edu.harvard.catalyst.scheduler.core.BookedVisitActivityLogStatics;
import edu.harvard.catalyst.scheduler.core.Statics;
import edu.harvard.catalyst.scheduler.dto.*;
import edu.harvard.catalyst.scheduler.dto.response.*;
import edu.harvard.catalyst.scheduler.dto.statics.CalendarFilter;
import edu.harvard.catalyst.scheduler.entity.*;
import edu.harvard.catalyst.scheduler.security.AuthorizedRoles;
import edu.harvard.catalyst.scheduler.service.AppointmentService;
import edu.harvard.catalyst.scheduler.service.ConfirmationStatus;
import edu.harvard.catalyst.scheduler.service.GanttInfoSortType;
import edu.harvard.catalyst.scheduler.util.DateUtility;
import edu.harvard.catalyst.scheduler.util.MiscUtil;
import edu.harvard.catalyst.scheduler.util.SchedulingRestriction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

import javax.inject.Singleton;
import javax.print.attribute.standard.Media;
import javax.servlet.ServletContext;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import java.lang.reflect.Type;
import java.util.*;

import static edu.harvard.catalyst.hccrc.core.util.Pair.pair;
import static edu.harvard.catalyst.hccrc.core.util.Pairs.toMap;
import static edu.harvard.catalyst.scheduler.core.Statics.*;
import static edu.harvard.catalyst.scheduler.entity.InstitutionRoleType.*;
import static edu.harvard.catalyst.scheduler.service.ConfirmationStatus.NotConfirmed;
import static edu.harvard.catalyst.scheduler.util.DateUtility.*;
import static edu.harvard.catalyst.scheduler.util.MiscUtil.formatSchedulingRestrictionErrorMessage;
import static edu.harvard.catalyst.scheduler.util.MiscUtil.isNullOrEmpty;

/**
 * @author Bill Simons
 * @date 4/15/13
 * @link http://cbmi.med.harvard.edu
 * @link http://chip.org
 */

@Path("/appointment")
@Singleton
@Component
public class AppointmentResource extends SecuredResource {

    private AppointmentService service;

    @Context
    private ServletContext context;

    @Autowired
    public AppointmentResource(final AppointmentService service) {
        this.service = service;
    }

    //Don't use - Needed for spring security cglib proxying
    AppointmentResource() {
    }

    @GET
    @Path("/getStudyVisits")
    @AuthorizedRoles({ROLE_SUPER_ADMIN, ROLE_RESOURCE_MANAGER, ROLE_SCHEDULER, ROLE_STUDY_STAFF, ROLE_FRONT_DESK})
    public String getStudyVisits(@QueryParam("filterString") final String filterString,
            @QueryParam("sortBy") final String sortBy,
            @QueryParam("orderBy") final String orderBy,
            @QueryParam("page") final int page,
            @QueryParam("maxResults") final int maxResults,
            @QueryParam("study") final int studyId,
            @QueryParam("approved") final boolean approved) {

        final GetStudyVisitsResponse result = getStudyVisitsResponse(filterString, sortBy, orderBy, page, maxResults, studyId, approved);
        return gson.toJson(result);
    }

    GetStudyVisitsResponse getStudyVisitsResponse(final String filterString, final String sortBy, final String orderBy, final int page, final int maxResults, final int studyId, final boolean approved)  {
        return service.getStudyVisits(
                filterString,
                sortBy,
                orderBy,
                page,
                maxResults,
                studyId,
                approved);
    }

    @GET
    @Path("/getVisitsByStudy")
    @AuthorizedRoles({ROLE_SUPER_ADMIN, ROLE_RESOURCE_MANAGER, ROLE_SCHEDULER, ROLE_STUDY_STAFF, ROLE_FRONT_DESK, ROLE_GENERAL_VIEW})
    public String getVisitsByStudy(
            @QueryParam("study") final int studyId,
            @QueryParam("active") final boolean active,
            @QueryParam("sortBy") final String sortBy,
            @QueryParam("orderBy") final String orderBy,
            @QueryParam("page") final int page,
            @QueryParam("maxResults") final int maxResults,
            @QueryParam("search") final String searchJson) {

        final SearchDTO searchDTO = gson.fromJson(searchJson, SearchDTO.class);

        final List<VisitTemplatesResponse> bookedResources =
                getVisitsByStudyList(studyId, active, sortBy, orderBy, page, maxResults, searchDTO);

        return gson.toJson(bookedResources);
    }

    List<VisitTemplatesResponse> getVisitsByStudyList(final int studyId,
                                                      final boolean active,
                                                      final String sortBy,
                                                      final String orderBy,
                                                      final int page,
                                                      final int maxResults,
                                                      final SearchDTO searchDTO) {
        return service.getVisitsByStudy(studyId, active, sortBy, orderBy, page, maxResults, searchDTO);
    }

    @GET
    @Path("/getSearchVisitResource")
    @AuthorizedRoles({ROLE_SUPER_ADMIN, ROLE_RESOURCE_MANAGER, ROLE_SCHEDULER, ROLE_STUDY_STAFF, ROLE_FRONT_DESK})
    public String getSearchVisitResource(
            @QueryParam("sortBy") final String sortBy,
            @QueryParam("orderBy") final String orderBy,
            @QueryParam("page") final int page,
            @QueryParam("maxResults") final int maxResults,
            @QueryParam("visit") final int visitId) {

        final GetSearchVisitResourceResponse result = getSearchVisitResourceResponse(sortBy, orderBy, page, maxResults, visitId);
        return gson.toJson(result);
    }

    GetSearchVisitResourceResponse getSearchVisitResourceResponse(final String sortBy, final String orderBy, final int page, final int maxResults, final int visitId)  {
        return service.getVisitResources(
                sortBy,
                orderBy,
                page,
                maxResults,
                visitId);
    }

    @GET
    @Path("/getAppointmentComments")
    @AuthorizedRoles({ROLE_SUPER_ADMIN, ROLE_RESOURCE_MANAGER, ROLE_SCHEDULER, ROLE_STUDY_STAFF, ROLE_FRONT_DESK})
    public String getAppointmentComments(@QueryParam("id") final int visitId) {
        final Map<String, Object> map = mapAppointmentSizeAndComments(visitId);
        return gson.toJson(map);
    }

    Map<String, Object> mapAppointmentSizeAndComments(final int visitId)  {
        List<VisitCommentsResponse.VisitComment> visitTemplateComments = service.getAppointmentComments(visitId);

        if (visitTemplateComments == null || visitTemplateComments.isEmpty()) {
            visitTemplateComments = Lists.newArrayList();
        }

        return toMap(
                pair("total", visitTemplateComments.size()),
                pair("visitComments", visitTemplateComments));
    }

    @GET
    @Path("/getOnlyTodaysBookedVisitsList")
    @AuthorizedRoles({ROLE_SUPER_ADMIN, ROLE_RESOURCE_MANAGER, ROLE_SCHEDULER, ROLE_STUDY_STAFF, ROLE_FRONT_DESK, ROLE_GENERAL_VIEW})
    public String getOnlyTodaysBookedVisitsList(@QueryParam("sortBy") final String sortBy, @QueryParam("orderBy") final String orderBy,
            @QueryParam("page") final int page, @QueryParam("maxResults") final int maxResults) {
        final Date startDate = startOfDay(Calendar.getInstance());
        final Date endDate = nextDay(startDate);
        final List<BookedVisitsResponse> bookedVisits = getTodaysBookedVisits(sortBy, orderBy, page, maxResults, startDate, endDate, getUser().getId(), getRemoteHost());
        return gson.toJson(bookedVisits);
    }

    List<BookedVisitsResponse> getTodaysBookedVisits(final String sortBy, final String orderBy, final int page,
            final int maxResults, final Date startDate, final Date endDate,
            final int userId, final String ipAddress) {
        return service.getOnlyTodaysBookedVisitsHomeList(startDate, endDate, sortBy, orderBy, page, maxResults, userId, ipAddress);
    }

    @GET
    @Path("/getOverbookedVisitsData")
    @AuthorizedRoles({ROLE_SUPER_ADMIN, ROLE_RESOURCE_MANAGER, ROLE_SCHEDULER})
    public String getOverbookedVisitsData(@QueryParam("id") final int id,
                                          @DefaultValue("resourceName") @QueryParam("sortOn") final String sortOn,
                                          @DefaultValue("ASC") @QueryParam("sortBy") final String sortBy) {
        service.logViewVisits(getUser(), getRemoteHost(), "Check for Overbooked Resources.");
        final List<OverbookedResourcesResponse> getOverbookData = lookupOverbookedVisitData(id, sortOn, sortBy);
        return gson.toJson(getOverbookData);
    }

    List<OverbookedResourcesResponse> lookupOverbookedVisitData(
            final int id,
            final String sortOn,
            final String sortBy) {
        return service.selectedVisitForOverbookChecks(id, sortOn, sortBy);
    }

    @GET
    @Path("/getEventResources")
    @AuthorizedRoles({ROLE_SUPER_ADMIN, ROLE_RESOURCE_MANAGER, ROLE_SCHEDULER, ROLE_STUDY_STAFF, ROLE_FRONT_DESK, ROLE_GENERAL_VIEW})
    public String getEventResources(@QueryParam("eventId") final String eventId, @QueryParam("sortBy") final String sortBy,
            @QueryParam("orderBy") final String orderBy, @QueryParam("page") final int page, @QueryParam("maxResults") final int maxResults)  {
        final List<BookedResourcesResponse> bookedResources = getListOfEventResources(eventId, getUser());
        return gson.toJson(bookedResources);
    }

    List<BookedResourcesResponse> getListOfEventResources(final String eventId, final User user) {
        return service.getEventResources(eventId, user);
    }

    @POST
    @Path("/getResourcesList")
    @AuthorizedRoles({ROLE_SUPER_ADMIN, ROLE_RESOURCE_MANAGER, ROLE_SCHEDULER, ROLE_STUDY_STAFF, ROLE_FRONT_DESK, ROLE_GENERAL_VIEW})
    public String getResourcesList(final String data) {
        final Map<String, List<Resource>> map = getResourcesList();
        return gson.toJson(map);
    }

    Map<String, List<Resource>> getResourcesList()  {
        return toMap(pair("resourcesList", service.getResources()));
    }

    @POST
    @Path("/saveComment")
    @AuthorizedRoles({ROLE_SUPER_ADMIN, ROLE_RESOURCE_MANAGER, ROLE_SCHEDULER, ROLE_STUDY_STAFF, ROLE_FRONT_DESK})
    public String saveComment(@FormParam("data") final String data) {
        final VisitSpecsDTO visitSpecsDTO = gson.fromJson(data, VisitSpecsDTO.class);
        final Comments comment = service.saveComment(visitSpecsDTO, getUser(), getRemoteHost());
        return gson.toJson("Comment saved successfully");
    }

    @GET
    @Path("/copyDayInpatientResources")
    @AuthorizedRoles({ROLE_SUPER_ADMIN, ROLE_RESOURCE_MANAGER, ROLE_SCHEDULER, ROLE_FRONT_DESK})
    public String copyDayInpatientResources(@QueryParam("visitId") final int visitId,
                                            @QueryParam("currentDay") final int currentDay,
                                            @QueryParam("targetDaysCommaString") final String targetDaysCommaString) {
        service.copyDayVisitResources(visitId, currentDay, targetDaysCommaString, getUser().getInstitution().getLongName(), context.getRealPath("/WEB-INF"));
        return null;
    }

    @GET
    @Path("/isDayCopyable")
    @AuthorizedRoles({ROLE_SUPER_ADMIN, ROLE_RESOURCE_MANAGER, ROLE_SCHEDULER, ROLE_FRONT_DESK})
    public String isDayCopyable(@QueryParam("visitId") final int visitId,
                                @QueryParam("currentDay") final int currentDay) {

        Boolean result = service.isDayCopyable(visitId, currentDay);

        return gson.toJson(result);
    }

    @GET
    @Path("/getGanttInfo/combo")
    @AuthorizedRoles({ROLE_SUPER_ADMIN, ROLE_RESOURCE_MANAGER, ROLE_SCHEDULER, ROLE_STUDY_STAFF, ROLE_FRONT_DESK})
    public String getTemplateResourcesForGanttCombo(
            @QueryParam("visit") final Integer visitId,
            @QueryParam("dayOffset") final int dayOffset,
            @QueryParam("sortType") GanttInfoSortType sortType) {
        if (sortType == null) {
            sortType = GanttInfoSortType.NameAsc;
        }
        GanttComboResponseDTO ganttResponseComboDTO = service.getTemplateResourcesForGanttCombo(visitId, dayOffset, sortType);
        return gson.toJson(ganttResponseComboDTO);
    }

    @GET
    @Path("/getGanttInfo/candidateFloatables")
    @AuthorizedRoles({ROLE_SUPER_ADMIN, ROLE_RESOURCE_MANAGER, ROLE_SCHEDULER, ROLE_STUDY_STAFF, ROLE_FRONT_DESK})
    public String getGanttInfoCandidateFloatables(
            @QueryParam("visit") final Integer visitId,
            @QueryParam("templateResource") final Integer templateResourceId) {
        return service.getGanttInfoCandidateGroupables(
                visitId,
                templateResourceId,
                GanttResponseDTO.GanttGroupingType.Floatables).jsonify(gson);
    }

    @GET
    @Path("/getGanttInfo/candidateFlexibles")
    @AuthorizedRoles({ROLE_SUPER_ADMIN, ROLE_RESOURCE_MANAGER, ROLE_SCHEDULER, ROLE_STUDY_STAFF, ROLE_FRONT_DESK})
    public String getGanttInfoCandidateFlexibles(
            @QueryParam("visit") final Integer visitId,
            @QueryParam("templateResourceId") final Integer templateResourceId) {
        GanttGroupablesResponseDTO dtoResult = service.getGanttInfoCandidateGroupables(
                visitId,
                templateResourceId,
                GanttResponseDTO.GanttGroupingType.Flexibles);
        return dtoResult.jsonify(gson);
    }

    @GET
    @Path("/getRoomResources")
    @AuthorizedRoles({ROLE_SUPER_ADMIN, ROLE_RESOURCE_MANAGER, ROLE_SCHEDULER, ROLE_STUDY_STAFF, ROLE_FRONT_DESK})
    public String getRoomResources(@QueryParam("id") final int visitId, @HeaderParam("User-Agent") final String header)  {
        service.logViewVisits(getUser(), getRemoteHost(), "Appointment Visit Selected");
        final Map<String, String> map = getRoomData(visitId);
        return gson.toJson(map);
    }

    @GET
    @Path("/getSelectableTemplateResources")
    @AuthorizedRoles({ROLE_SUPER_ADMIN, ROLE_RESOURCE_MANAGER, ROLE_SCHEDULER, ROLE_STUDY_STAFF, ROLE_FRONT_DESK})
    public String getTemplateResources(@QueryParam("visitId") final int visitId,
                                       @QueryParam("isBillable") boolean isBillable,
                                       @QueryParam("sortBy") String sortBy,
                                       @QueryParam("orderBy") final String orderBy,
                                       @QueryParam("page") final int page,
                                       @QueryParam("maxResults") final int maxResults)  {
        final List<TemplateResource> resources = service.getSelectableTemplateResources(
                visitId, isBillable, sortBy, orderBy, page, maxResults);
        return gson.toJson(resources);
    }

    @GET
    @Path("/updateTemplateResourcesBillable")
    @AuthorizedRoles({ROLE_SUPER_ADMIN, ROLE_RESOURCE_MANAGER, ROLE_SCHEDULER, ROLE_STUDY_STAFF, ROLE_FRONT_DESK})
    public String updateTemplateResourcesBillable(@QueryParam("visitId") final Integer visitId,
                                                  @QueryParam("templateResourcesCommaString") final String templateResourcesCommaString,
                                                  @QueryParam("isBillable") final boolean isBillable)  {
        final BooleanResultDTO booleanResultDTO = service.updateTemplateResourcesBillable(visitId, templateResourcesCommaString, isBillable);
        return gson.toJson(booleanResultDTO);
    }

    Map<String, String> getRoomData(final int visitId) {
        final Map<String, String> map = new HashMap<String, String>();
        final List<TemplateResource> r = service.getRoomResources(visitId);
        if(r == null || r.isEmpty() || r.size() == 0) {
            map.put("roomResources", null);
        } else {
            map.put("roomResources", r.get(0).getResource().getName());
        }
        return map;
    }

    @GET
    @Path("/getBookedVisitsList")
    @AuthorizedRoles({ROLE_SUPER_ADMIN, ROLE_RESOURCE_MANAGER, ROLE_SCHEDULER, ROLE_STUDY_STAFF, ROLE_FRONT_DESK, ROLE_GENERAL_VIEW})
    public String getBookedVisitsList(
            @QueryParam("sortBy") final String sortBy,
            @QueryParam("orderBy") final String orderBy,
            @QueryParam("page") final int page,
            @QueryParam("maxResults") final int maxResults,
            @QueryParam("fromDate") final String fromDate,
            @QueryParam("toDate") final String toDate,
            @QueryParam("search") String search)  {

        final SearchDTO searchDTO = assembleSearchDTO(search);
        final List<BookedVisitsResponse> result =
                service.getBookedVisitsList(
                                getUser().getId(),
                                searchDTO,
                                sortBy,
                                orderBy,
                                page,
                                maxResults,
                                getRemoteHost(),
                                !fromDate.isEmpty() ? adjustDateToStartOfDay(DateUtility.parse(DateUtility.monthDayYear(), fromDate)) : null,
                                !toDate.isEmpty() ? adjustDateToStartOfDay(DateUtility.parse(DateUtility.monthDayYear(), toDate)) : null);

        return gson.toJson(result);
    }

    SearchDTO assembleSearchDTO(String input) {
        return gson.fromJson(input, SearchDTO.class);
    }

    @GET
    @Path("/getBookedVisitData")
    @AuthorizedRoles({ROLE_SUPER_ADMIN, ROLE_RESOURCE_MANAGER, ROLE_SCHEDULER, ROLE_STUDY_STAFF, ROLE_FRONT_DESK, ROLE_GENERAL_VIEW})
    public String getBookedVisitData(@QueryParam("bookedVisit") final int bookedVisitId)  {
        final BookedVisitDetailResponse getBookedVisitsDTO = service.getBookedVisitData(bookedVisitId);
        return gson.toJson(getBookedVisitsDTO);
    }

    @GET
    @Path("/getOverbookedResourceDataByResourceType")
    @AuthorizedRoles({ROLE_SUPER_ADMIN, ROLE_RESOURCE_MANAGER, ROLE_SCHEDULER, ROLE_STUDY_STAFF, ROLE_FRONT_DESK, ROLE_GENERAL_VIEW})
    public String getOverbookedResourceDataByResourceType(
            @QueryParam("startDate") final Long startDate,
            @QueryParam("endDate") final Long endDate,
            @QueryParam("type") final int type,
            @QueryParam("selectedSublocationFilters") final String sublocationsJson,
            @QueryParam("orderBy") final String orderBy) {

        final Date selectedStartDate = new Date(startDate);
        final Date selectedEndDate = new Date(endDate);
        final List<Integer> sublocations = unmarshalJsonListOfInts(sublocationsJson);
        final List<OverbookTimelineDataResponseDTO> resourceEventsList = service.getOverbookTimelineData(selectedStartDate, selectedEndDate, type, sublocations, orderBy, getUser());
        return gson.toJson(resourceEventsList);
    }

    List<Integer> unmarshalJsonListOfInts(final String jsonArray) {
        if(jsonArray == null) {
            throw new IllegalArgumentException("input json can't be null");
        }
        final Type listOfIntegers = new TypeToken<List<Integer>>() {}.getType();
        return gson.fromJson(jsonArray, listOfIntegers);
    }

    @GET
    @Path("/getBookedVisitResources")
    @AuthorizedRoles({ROLE_SUPER_ADMIN, ROLE_RESOURCE_MANAGER, ROLE_SCHEDULER, ROLE_STUDY_STAFF, ROLE_FRONT_DESK, ROLE_GENERAL_VIEW})
    public String getBookedVisitResources(@QueryParam("eventid") final int bookedVisitId, @QueryParam("sortBy") final String sortBy,
            @QueryParam("orderBy") final String orderBy, @QueryParam("page") final int page, @QueryParam("maxResults") final int maxResults) {
        final List<BookedResourcesResponse> bookedResources = getBookedResourcesList(bookedVisitId, sortBy, orderBy, page, maxResults);
        return gson.toJson(bookedResources);
    }

    List<BookedResourcesResponse> getBookedResourcesList(final int bookedVisitId,
            final String sortBy, final String orderBy, final int page, final int maxResults) {
        return service.getBookedResources(bookedVisitId, sortBy, orderBy, page, maxResults);
    }

    @GET
    @Path("/getBookedResourceData")
    @AuthorizedRoles({ROLE_SUPER_ADMIN, ROLE_RESOURCE_MANAGER, ROLE_SCHEDULER, ROLE_FRONT_DESK})
    public String getBookedResourceData(@QueryParam("id") final int id) {
        final BookedResource bookedResource = service.getBookedResourceData(id, getRemoteHost(), getUser());
        return subjectMrnSkippingGson.toJson(bookedResource);
    }

    @POST
    @Path("/overbookRoomSelected")
    @AuthorizedRoles({ROLE_SUPER_ADMIN, ROLE_RESOURCE_MANAGER, ROLE_SCHEDULER})
    public String overbookRoomSelected(@FormParam("data") final String data) {
        service.logViewVisits(getUser(), getRemoteHost(), "Searching for available room through Reserve Room functionality");
        final Map<String, String> map = new HashMap<String, String>();
        final VisitSpecsDTO visitSpecsDTO = gson.fromJson(data, VisitSpecsDTO.class);
        BookedVisit searchedVisits = null;
        if(visitSpecsDTO != null) {
            searchedVisits = service.overbookRoomData(visitSpecsDTO);
        }

        boolean visitUsesRooms = service.getRoomResources(visitSpecsDTO.getVisit()) != null;
        if(visitUsesRooms && searchedVisits.getSelectedRoom() == null) {
            map.put("overbookRoomSelected", "No Rooms Available. Please look for a room at a different time, or select a room from the list to overbook.");
            map.put("overbookRoomSelectedId", null);
        } else {
            map.put("overbookRoomSelected", searchedVisits.getSelectedRoom().getName());
            map.put("overbookRoomSelectedId", searchedVisits.getSelectedRoom().getId().toString());
        }
        return gson.toJson(map);
    }

    @POST
    @Path("/overbookRoomAppointment")
    @AuthorizedRoles({ROLE_SUPER_ADMIN, ROLE_RESOURCE_MANAGER, ROLE_SCHEDULER})
    public String overbookRoomAppointment(@FormParam("data") final String data) {
        List<Map<String, Object>> visitsList = new ArrayList<Map<String, Object>>();

        final VisitSpecsDTO visitSpecsDTO = gson.fromJson(data, VisitSpecsDTO.class);
        BookedVisit searchedVisit;

        if(visitSpecsDTO != null) {
            searchedVisit = service.confirmOverbookRoomData(visitSpecsDTO, getUser(), getRemoteHost(), getContextRealPath());
            if(searchedVisit != null) {
                final String eventId = searchedVisit.getId().toString();
                setEventDataInVisitsList(searchedVisit.getAppointmentStatus().getName(), eventId, searchedVisit, false, visitsList);
            }
        }
        return gson.toJson(visitsList);
    }

    @POST
    @Path("/addBookedResourceOverride")
    @AuthorizedRoles({ROLE_SUPER_ADMIN, ROLE_RESOURCE_MANAGER, ROLE_SCHEDULER})
    public String addBookedResourceOverride(@FormParam("data") final String data) {
        final VisitSpecsDTO visitSpecsDTO = gson.fromJson(data, VisitSpecsDTO.class);
        final BooleanResultDTO booleanResultDTO = service.addBookedResourceOverride(visitSpecsDTO, getUser(), getRemoteHost());
        return gson.toJson(booleanResultDTO);
    }

    @POST
    @Path("/deleteBookedResourceOverride")
    @AuthorizedRoles({ROLE_SUPER_ADMIN, ROLE_RESOURCE_MANAGER, ROLE_SCHEDULER})
    public String deleteBookedResourceOverride(@FormParam("data") final String data) {
        final VisitSpecsDTO visitSpecsDTO = gson.fromJson(data, VisitSpecsDTO.class);
        final BooleanResultDTO booleanResultDTO = service.deleteBookedResourceOverride(visitSpecsDTO, getUser(), getRemoteHost());
        return gson.toJson(booleanResultDTO);
    }

    @POST
    @Path("/editBookedResourceOverride")
    @AuthorizedRoles({ROLE_SUPER_ADMIN, ROLE_RESOURCE_MANAGER, ROLE_SCHEDULER})
    public String editBookedResourceOverride(@FormParam("data") final String data) {
        final VisitSpecsDTO visitSpecsDTO = gson.fromJson(data, VisitSpecsDTO.class);
        final BooleanResultDTO booleanResultDTO = service.editBookedResourceOverride(visitSpecsDTO, getUser(), getRemoteHost());
        return gson.toJson(booleanResultDTO);
    }

    @POST
    @Path("/batchEntry")
    @AuthorizedRoles({ROLE_SUPER_ADMIN, ROLE_RESOURCE_MANAGER, ROLE_SCHEDULER, ROLE_STUDY_STAFF, ROLE_FRONT_DESK})
    public String batchEntry(@FormParam("data") final String data) {
        final VisitSpecsDTO visitSpecsDTO = gson.fromJson(data, VisitSpecsDTO.class);
        final String errorMsg = service.batchEntryUpdate(visitSpecsDTO, getUser(), getRemoteHost(), getContextRealPath());

        return errorMsg;
    }


    String getContextRealPath(){
      return context.getRealPath("/WEB-INF");
    }

    @POST
    @Path("/logAppointmentSearch")
    @AuthorizedRoles({ROLE_SUPER_ADMIN, ROLE_RESOURCE_MANAGER, ROLE_SCHEDULER, ROLE_STUDY_STAFF, ROLE_FRONT_DESK})
    public String logAppointmentSearch(@FormParam("data") final String data) {
        final BookedVisitDTO dto = new BookedVisitDTO();
        service.logViewVisits(getUser(), getRemoteHost(), "Searching for a new Appointment");
        return gson.toJson(dto);
    }

    @POST
    @Path("/logViewBookedVisit")
    @AuthorizedRoles({ROLE_SUPER_ADMIN, ROLE_RESOURCE_MANAGER, ROLE_SCHEDULER, ROLE_STUDY_STAFF, ROLE_FRONT_DESK, ROLE_GENERAL_VIEW})
    public String logViewBookedVisit(@FormParam("data") final String data) {
        BookedVisitDTO dto = gson.fromJson(data, BookedVisitDTO.class);
        dto = service.logViewBookedVisit(dto, getUser(), getRemoteHost());
        return gson.toJson(dto);
    }

    @POST
    @Path("/logAddResourceOverride")
    @AuthorizedRoles({ROLE_SUPER_ADMIN, ROLE_RESOURCE_MANAGER, ROLE_SCHEDULER})
    public String logAddResourceOverride(@FormParam("data") final String data) {
        return logOverrideActions(data, "Add");
    }

    @POST
    @Path("/logEditResourceOverride")
    @AuthorizedRoles({ROLE_SUPER_ADMIN, ROLE_RESOURCE_MANAGER, ROLE_SCHEDULER})
    public String logEditResourceOverride(@FormParam("data") final String data) {
        return logOverrideActions(data, "Edit");
    }

    @POST
    @Path("/logDeleteResourceOverride")
    @AuthorizedRoles({ROLE_SUPER_ADMIN, ROLE_RESOURCE_MANAGER, ROLE_SCHEDULER})
    public String logDeleteResourceOverride(@FormParam("data") final String data) {
        return logOverrideActions(data, "Delete");
    }

    private String logOverrideActions(final String data, final String action) {
        BookedVisitDTO dto = gson.fromJson(data, BookedVisitDTO.class);
        if(dto == null) {
            dto = new BookedVisitDTO();
        }
        dto = service.logIncompleteOverrideActions(dto, getUser(), getRemoteHost(),  action +" - Overriding an Appointment (Incomplete Action)");
        return gson.toJson(dto);
    }

    @POST
    @Path("/crudTemplateResources")
    @AuthorizedRoles({ROLE_SUPER_ADMIN, ROLE_RESOURCE_MANAGER, ROLE_SCHEDULER, ROLE_FRONT_DESK})
    public String crudTemplateResources(@FormParam("data") final String data,
                                        @QueryParam("actionType") final String actionType,
                                        @HeaderParam("User-Agent") final String header) {
        final TemplateResourceDTO tr = gson.fromJson(data, TemplateResourceDTO.class);
        final BooleanResultDTO result = service.crudTemplateResources(
                tr,
                getUser().getInstitution().getLongName(),
                context.getRealPath("/WEB-INF"),
                actionType,
                getUser(),
                getRemoteHost());
        return gson.toJson(result);
    }

    @GET
    @Path("/checkIfResourcesLinkableOnDeleteUnlink")
    @AuthorizedRoles({ROLE_SUPER_ADMIN, ROLE_RESOURCE_MANAGER, ROLE_SCHEDULER, ROLE_FRONT_DESK})
    public String checkIfResourcesLinkableOnDeleteUnlink(
            @QueryParam("templateResourceId") final Integer templateResourceId) {
        final BooleanResultDTO result = service.checkIfResourcesLinkableOnDeleteUnlink(
                                        templateResourceId
        );
        return gson.toJson(result);
    }

    @GET
    @Path("/getTemplateResourceDataWithAnnotations")
    @AuthorizedRoles({ROLE_SUPER_ADMIN, ROLE_RESOURCE_MANAGER, ROLE_SCHEDULER, ROLE_FRONT_DESK})
    public String getTemplateResourceData(@QueryParam("templateResourceId") final Integer templateResourceId) {
        final TemplateResourceWithLlaListDTO template = service.getTemplateResourceDataWithAnnotations(templateResourceId);
        return gson.toJson(template);
    }

    // works for both float and flex. assumes that the list of resources identified really is linkable
    @POST
    @Path("/linkResources")
    @AuthorizedRoles({ROLE_SUPER_ADMIN, ROLE_RESOURCE_MANAGER, ROLE_SCHEDULER, ROLE_FRONT_DESK})
    public String linkResources(@FormParam("data") final String data) {
        GetTemplateResourceGroupDTO getTemplateResourceGroupDTO = gson.fromJson(data, GetTemplateResourceGroupDTO.class);
        BooleanResultDTO booleanResultDTO = service.linkTemplateResourcesAsNewGroup(
                getTemplateResourceGroupDTO,
                getUser().getInstitution().getLongName(),
                context.getRealPath("/WEB-INF"));

        return gson.toJson(booleanResultDTO);
    }

    @GET
    @Path("/unlinkOneResource")
    @AuthorizedRoles({ROLE_SUPER_ADMIN, ROLE_RESOURCE_MANAGER, ROLE_SCHEDULER, ROLE_FRONT_DESK})
    public String unlinkOneResource(
            @QueryParam("templateResourceId") final Integer templateResourceId) {
        BooleanResultDTO booleanResultDTO = service.unlinkOneResource(
                templateResourceId,
                getUser().getInstitution().getLongName(),
                context.getRealPath("/WEB-INF"));
        return gson.toJson(booleanResultDTO);
    }

    @GET
    @Path("/unlinkGroup")
    @AuthorizedRoles({ROLE_SUPER_ADMIN, ROLE_RESOURCE_MANAGER, ROLE_SCHEDULER, ROLE_FRONT_DESK})
    public String unlinkGroup(@QueryParam("groupId") final String groupId) {
        BooleanResultDTO booleanResultDTO = service.unlinkGroup(
                groupId,
                getUser().getInstitution().getLongName(),
                context.getRealPath("/WEB-INF"));
        return gson.toJson(booleanResultDTO);
    }

    @GET
    @Path("/updateTemplateResourceTime")
    @AuthorizedRoles({ROLE_SUPER_ADMIN, ROLE_RESOURCE_MANAGER, ROLE_SCHEDULER, ROLE_FRONT_DESK})
    public String updateTemplateResourceTime(@QueryParam("templateResourceId") final Integer templateResourceId,
                                             @QueryParam("startMinutes") final Integer startMinutes,
                                             @QueryParam("endMinutes") final Integer endMinutes) {
        BooleanResultDTO booleanResultDTO = service.updateTemplateResourceTime(
                templateResourceId,
                startMinutes,
                endMinutes);
        return gson.toJson(booleanResultDTO);
    }

    @GET
    @Path("/getGanttResourceInfo")
    @AuthorizedRoles({ROLE_SUPER_ADMIN, ROLE_RESOURCE_MANAGER, ROLE_SCHEDULER, ROLE_STUDY_STAFF, ROLE_FRONT_DESK})
    public String getGanttResourceInfo(
            @QueryParam("visitId") final Integer visitId,
            @QueryParam("templateResourceId") final Integer templateResourceId,
            @QueryParam("dayOffset") final int dayOffset) {

        GanttResourceInfoDTO ganttResponseComboDTO = service.getGanttResourceInfo(visitId, templateResourceId, dayOffset);
        return gson.toJson(ganttResponseComboDTO);
    }

    @GET
    @Path("/getSchedulingRestriction")
    @AuthorizedRoles({ROLE_SUPER_ADMIN, ROLE_RESOURCE_MANAGER, ROLE_SCHEDULER, ROLE_STUDY_STAFF, ROLE_FRONT_DESK, ROLE_GENERAL_VIEW})
    public String getSchedulingRestriction() {
        return gson.toJson(SchedulingRestriction.getInstance().getSchedulingRestriction());
    }

    @GET
    @Path("/getLastMinuteRestriction")
    @AuthorizedRoles({ROLE_SUPER_ADMIN, ROLE_RESOURCE_MANAGER, ROLE_SCHEDULER, ROLE_STUDY_STAFF, ROLE_FRONT_DESK, ROLE_GENERAL_VIEW})
    public String getLastMinuteRestriction() {
        return gson.toJson(SchedulingRestriction.getInstance().getLastMinuteIndicator());
    }

    @GET
    @Path("/getCalendarBookedVisits")
    @AuthorizedRoles({ROLE_SUPER_ADMIN, ROLE_RESOURCE_MANAGER, ROLE_SCHEDULER, ROLE_STUDY_STAFF, ROLE_FRONT_DESK, ROLE_GENERAL_VIEW})
    public String getCalendarBookedVisits(@HeaderParam("User-Agent") final String header,
                                          @QueryParam("start") final int start,
                                          @QueryParam("starting") final Long starting,
                                          @QueryParam("ending") final Long ending,
                                          @QueryParam("filterId") final String filterId,
                                          @QueryParam("filterString") final String filterString,
                                          @QueryParam("sublocation") final String sublocationName,
                                          @QueryParam("todayVisits") final boolean todayVisits) {

        String adjustedSublocation = MiscUtil.undefinedToNull(sublocationName);

        Date startDate = new Date(starting);
        Date endDate = adjustDateToEndOfDay(new Date(ending));
        final int userId = getUser().getId();
        final String remoteHost = getRemoteHost();

        CalendarFilter calendarFilter;
        if (isNullOrEmpty(filterId)) {
            calendarFilter = CalendarFilter.NO_FILTER;
        }
        else {
            // This will throw an exception if the value of filterId does not map to any of
            // the CalendarFilter enum values
            calendarFilter = CalendarFilter.valueOf(filterId);
        }

        final List<CalendarVisitsResponse> bookedVisits =
                getAppointmentsCalendar(
                        userId,
                        calendarFilter,
                        filterString,
                        adjustedSublocation,
                        todayVisits,
                        startDate,
                        endDate,
                        remoteHost
                );
        CalendarVisitsLastMinuteResponse response = new CalendarVisitsLastMinuteResponse(bookedVisits);
        return gson.toJson(response);
    }

    List<CalendarVisitsResponse> getAppointmentsCalendar(
            final int userId, final CalendarFilter calendarFilter,
            final String filterString, String sublocationName, final boolean todayVisits,
            final Date startDate, final Date endDate, final String remoteHost
    ) {
        List<CalendarVisitsResponse> responses =
                service.getCalendarBookedVisits(userId, calendarFilter, filterString, sublocationName, startDate, endDate, remoteHost, todayVisits);
        return responses;
    }

    @POST
    @Path("/isSubjectAvailable")
    @AuthorizedRoles({ROLE_SUPER_ADMIN, ROLE_RESOURCE_MANAGER, ROLE_SCHEDULER, ROLE_STUDY_STAFF, ROLE_FRONT_DESK})
    public String isSubjectAvailable(@QueryParam("subjectMrnId") final Integer subjectMrnId,
                                     @QueryParam("startDate") final Long startDate,
                                     @QueryParam("endDate") final Long endDate) {

        boolean subjectAvailable = service.isSubjectAvailable(subjectMrnId, new Date(startDate), new Date(endDate));
        return subjectAvailable ? "yes" : "no";
    }

    @POST
    @Path("/confirmAppointment")
    @AuthorizedRoles({ROLE_SUPER_ADMIN, ROLE_RESOURCE_MANAGER, ROLE_SCHEDULER, ROLE_STUDY_STAFF, ROLE_FRONT_DESK})
    public String confirmAppointment(@FormParam("data") final String data,
                                     @HeaderParam("User-Agent") final String header,
                                     @QueryParam("isInpatient") final boolean isInpatient,
                                     @QueryParam("startDate") final Long startDate,
                                     @QueryParam("endDate") final Long endDate) {
        final VisitSpecsDTO visitSpecsDTO = gson.fromJson(data, VisitSpecsDTO.class);
        final ConfirmationStatus confirmationStatus;
        final Map<String, String> resultMap = Maps.newHashMap();

        if(visitSpecsDTO != null) {

            visitSpecsDTO.setStartDate(startDate);
            visitSpecsDTO.setEndDate(endDate);
            confirmationStatus = service.confirmEvent(visitSpecsDTO, getUserSession(), getRemoteHost(),
                    getUser().getInstitution().getLongName(), context.getRealPath("/WEB-INF"), isInpatient);
            if(visitSpecsDTO.getDoubleRoomMessage() != null) {
                resultMap.put("doubleSharedRoom", visitSpecsDTO.getDoubleRoomMessage());
                return gson.toJson(resultMap);
            }
        } else {
            confirmationStatus = NotConfirmed;
        }


        if(visitSpecsDTO.getAlternateResourceUsed() != null) {
            resultMap.put("alternateResourceUsed", visitSpecsDTO.getAlternateResourceUsed());
        }

        resultMap.put("doubleSharedRoom", null);
        resultMap.put("confirmationStatus", Boolean.valueOf(confirmationStatus.isConfirmed).toString());

        return gson.toJson(resultMap);
    }

    @POST
    @Path("/getSearchedVisit")
    @AuthorizedRoles({ROLE_SUPER_ADMIN, ROLE_RESOURCE_MANAGER, ROLE_SCHEDULER, ROLE_STUDY_STAFF, ROLE_FRONT_DESK})
    public String getSearchedVisit(@QueryParam("subjectMrnId") final String subjectMrnId,
                                   @QueryParam("visit") final String visit,
                                   @QueryParam("startDate") final String startDate,
                                   @QueryParam("endDate") final String endDate,
                                   @QueryParam("isInpatient") final boolean isInpatientType) {

        service.logViewVisits(getUser(), getRemoteHost(), "Searching for a new Appointment");
        List<Map<String, Object>> visitsList = new ArrayList<>();
        Map<String, Object> visitInstance;
        VisitSpecsDTO visitSpecsDTO = new VisitSpecsDTO();

        if (!"null".equalsIgnoreCase(subjectMrnId)) {
            visitSpecsDTO.setSubjectMrnId(Integer.parseInt(subjectMrnId));
        } // otherwise leave the value in visitSpecsDTO as 0

        visitSpecsDTO.setVisit(Integer.parseInt(visit));
        visitSpecsDTO.setStartDate(Long.parseLong(startDate));
        visitSpecsDTO.setEndDate(Long.parseLong(endDate));
        visitSpecsDTO.setSessionId(getUser().getSessionId());

        if (!MiscUtil.canScheduleAppointment(Long.parseLong(startDate), getUser().getInstitutionRole().getType())) {
            visitSpecsDTO.setResult(false);
            visitSpecsDTO.setErrorMsg(formatSchedulingRestrictionErrorMessage());
            return gson.toJson(visitSpecsDTO);
        }

        final List<BookedVisit> searchedVisits = Lists.newArrayList(service.findCandidateVisits(visitSpecsDTO, getUserSession(), false, false, isInpatientType));

        if(searchedVisits != null) {
            for(final BookedVisit bookedVisit : searchedVisits) {
                final String eventId = bookedVisit.getUniquekey();
                String className = "Searching";

                if (bookedVisit.isRejectedVisit()) {
                    className = "NotFound";
                }
                setEventDataInVisitsList(className, eventId, bookedVisit, false, visitsList);
            }
        }

        if(visitsList.size() == 0) {
            visitInstance = new HashMap<>();
            visitInstance.put("id", 11111111);
            visitInstance.put("className", "false");
            visitsList.add(visitInstance);
            visitSpecsDTO.setResult(false);
            visitSpecsDTO.setErrorMsg("No Available Appointments were found. Please select another date range.");
            return gson.toJson(visitSpecsDTO);
        }

        return gson.toJson(visitsList);
    }

    @GET
    @Path("/getSearchedSlotResources")
    @AuthorizedRoles({ROLE_SUPER_ADMIN, ROLE_RESOURCE_MANAGER, ROLE_SCHEDULER, ROLE_STUDY_STAFF, ROLE_FRONT_DESK})
    public String getSearchedSlotResources(@QueryParam("subjectMrnId") final String subjectMrnId, @QueryParam("visit") final String visit,
                                           @QueryParam("startDate") final Long startDate, @QueryParam("endDate") final Long endDate,
                                           @QueryParam("isInpatient") final boolean isInpatientType) {
        final List<BookedResourcesResponse> bookedResources = Lists.newArrayList();
        VisitSpecsDTO visitSpecsDTO = null;

        if(subjectMrnId != null) {
            visitSpecsDTO = new VisitSpecsDTO();
            visitSpecsDTO.setSubjectMrnId(Integer.parseInt(subjectMrnId));
            visitSpecsDTO.setVisit(Integer.parseInt(visit));
            visitSpecsDTO.setStartDate(startDate);
            visitSpecsDTO.setEndDate(endDate);
            visitSpecsDTO.setSessionId(getUser().getSessionId());
        }

        if(visitSpecsDTO != null) {
            final BookedVisit potentialVisit = Lists.newArrayList(service.findCandidateVisits(visitSpecsDTO, getUserSession(), false, true, isInpatientType)).get(0);

            final List<BookedResource> potentialBookedResourceList = potentialVisit.getBookedResourceList();

            for (int i = 0; i < potentialBookedResourceList.size(); i++) {
                String rejectedMessage = "";

                if (getUser().getInstitutionRole().getType() == InstitutionRoleType.ROLE_SUPER_ADMIN ||
                        getUser().getInstitutionRole().getType() == InstitutionRoleType.ROLE_RESOURCE_MANAGER ||
                        getUser().getInstitutionRole().getType() == InstitutionRoleType.ROLE_SCHEDULER) {
                    rejectedMessage = potentialBookedResourceList.get(i).getTemplateResource().getRejectedResourceMessage();
                }
                final BookedResourcesResponse bookedResource = new BookedResourcesResponse(potentialBookedResourceList.get(i).getId(),
                        potentialBookedResourceList.get(i).getResource().getName(), "",
                        potentialBookedResourceList.get(i).getScheduledStartTime(),
                        potentialBookedResourceList.get(i).getScheduledEndTime(),
                        rejectedMessage,
                        potentialBookedResourceList.get(i).getTemplateResource().getAvailable(),
                        potentialBookedResourceList.get(i).getTemplateResource().getResourceGroupType(),
                        (long) potentialBookedResourceList.size(),
                        false);
                bookedResources.add(bookedResource);
            }
        }
        return gson.toJson(bookedResources);
    }

    void setEventDataInVisitsList(final String classNameString,
                                  final String eventId,
                                  final BookedVisit bookedVisit,
                                  final boolean allDay,
                                  final List<Map<String, Object>> visitDataMapList) {

        final Map<String, Object> visitDataMap = new HashMap<String, Object>();

        final String rooms = bookedVisit.getRooms() == null ? "" : bookedVisit.getRooms();
        // FIXME-XH : duplicates code in AppointmentDAO.getCalendarVisitsDTOs()
        final String title = bookedVisit.getSubjectMrnDecrypted() != null ?
                bookedVisit.getSubjectMrnDecrypted().getSubject().getLastName() :
                Statics.NO_SUBJECT_ASSIGNED;

        visitDataMap.put("id", eventId);
        visitDataMap.put("localId", bookedVisit.getStudy().getLocalId());
        visitDataMap.put("piName", bookedVisit.getStudy().getInvestigatorLastNameOrNA());
        visitDataMap.put("visitName", bookedVisit.getName());
        visitDataMap.put("room", rooms);
        visitDataMap.put("title", title);
        visitDataMap.put("start", format(dateHourMinSec(), bookedVisit.getScheduledStartTime()));
        visitDataMap.put("end", format(dateHourMinSec(), bookedVisit.getScheduledEndTime()));
        visitDataMap.put("className", classNameString);
        visitDataMap.put("inpatientVisitType",
                bookedVisit.getVisitTemplate().getVisitType().isInpatient());
        visitDataMap.put("allDay", allDay);

        visitDataMapList.add(visitDataMap);
    }

    @POST
    @Path("/rescheduleAppointment")
    @Consumes({MediaType.APPLICATION_FORM_URLENCODED, MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN})
    @Produces({MediaType.APPLICATION_FORM_URLENCODED, MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN})
    @AuthorizedRoles({ROLE_SUPER_ADMIN, ROLE_RESOURCE_MANAGER, ROLE_SCHEDULER})
    public String rescheduleAppointment(
            @FormParam("data") final String data,
            @QueryParam("eventid") final String bookedvisit,
            @QueryParam("startDate") final Long startDate,
            @QueryParam("overrideReason") final int overrideReason,
            @QueryParam("followOriginalTemplate") final Boolean followOriginalTemplate,
            @QueryParam("homeScreen") final boolean homeScreen) {

        List<Map<String, Object>> visitsList = new ArrayList<Map<String, Object>>();
        VisitSpecsDTO visitSpecsDTO = gson.fromJson(data, VisitSpecsDTO.class);
        BookedVisit searchedVisits;
        if(visitSpecsDTO == null && bookedvisit != null) {
            visitSpecsDTO = new VisitSpecsDTO();
            visitSpecsDTO.setStartDate(startDate);
            visitSpecsDTO.setOverrideReason(overrideReason);
            visitSpecsDTO.setBookedvisit(Integer.parseInt(bookedvisit));
        }

        if(visitSpecsDTO != null) {
            searchedVisits = service.rescheduleData(visitSpecsDTO, getUser(), getRemoteHost(),
                    context.getRealPath("/WEB-INF"), followOriginalTemplate);

            if(searchedVisits != null) {
                final String eventId = searchedVisits.getId().toString();
                final boolean allDay = homeScreen && searchedVisits.getVisitType().isInpatient();

                String className = "Scheduled";
                if (null == searchedVisits.getSubjectMrn()) {
                    className = "Hold";
                }

                setEventDataInVisitsList(className, eventId, searchedVisits, allDay, visitsList);
            }
        }
        return gson.toJson(visitsList);
    }

    @POST
    @Path("/checkinVisit")
    @AuthorizedRoles({ROLE_SUPER_ADMIN, ROLE_RESOURCE_MANAGER, ROLE_SCHEDULER, ROLE_FRONT_DESK})
    public void checkinVisit(@FormParam("data") final String data) {
        final VisitSpecsDTO visitSpecsDTO = gson.fromJson(data, VisitSpecsDTO.class);
        service.checkInVisit(visitSpecsDTO, getUser(), getRemoteHost());
    }

    @POST
    @Path("/checkoutVisit")
    @AuthorizedRoles({ROLE_SUPER_ADMIN, ROLE_RESOURCE_MANAGER, ROLE_SCHEDULER, ROLE_FRONT_DESK})
    public String checkoutVisit(@FormParam("data") final String data, @QueryParam("todaysVisits") final boolean todaysVisits) {
        final VisitSpecsDTO visitSpecsDTO = gson.fromJson(data, VisitSpecsDTO.class);
        final BookedVisit visit = service.checkOutVisit(visitSpecsDTO, getUser(), getRemoteHost());
        final String eventId = visit.getId().toString();
        final boolean allDay = todaysVisits && visit.getVisitType().isInpatient();

        final List<Map<String, Object>> visitsList = new ArrayList<Map<String, Object>>();
        setEventDataInVisitsList(visit.getAppointmentStatus().getName(), eventId, visit, allDay, visitsList);
        return gson.toJson(visitsList.get(0));
    }

    @POST
    @Path("/cancelVisit")
    @AuthorizedRoles({ROLE_SUPER_ADMIN, ROLE_RESOURCE_MANAGER, ROLE_SCHEDULER, ROLE_STUDY_STAFF, ROLE_FRONT_DESK})
    public String cancelVisit(@FormParam("data") final String data, @QueryParam("todaysVisits") final boolean todaysVisits)  {
        final VisitSpecsDTO visitSpecsDTO = gson.fromJson(data, VisitSpecsDTO.class);
        if (getUser().isStudyStaff() && !service.isStudyMember(getUser(), visitSpecsDTO.getId())) {
            new AccessDeniedException("Cannot cancel a visit that you are not associated with.");
        }
        final BookedVisit visit = service.cancelVisit(visitSpecsDTO, getUser(), getRemoteHost(), context.getRealPath("/WEB-INF"));
        final String eventId = visit.getId().toString();
        final boolean allDay = todaysVisits && visit.getVisitType().isInpatient();

        List<Map<String, Object>> visitsList = new ArrayList<Map<String, Object>>();

        setEventDataInVisitsList(visit.getAppointmentStatus().getName(), eventId, visit, allDay, visitsList);
        return gson.toJson(visitsList.get(0));
    }

    @GET
    @Path("getBookedVisitScheduledDate")
    @AuthorizedRoles({ROLE_SUPER_ADMIN, ROLE_RESOURCE_MANAGER, ROLE_SCHEDULER, ROLE_STUDY_STAFF, ROLE_FRONT_DESK, ROLE_GENERAL_VIEW})
    public String getBookedVisitScheduledDate(@QueryParam("id") final int id) {
        return service.getVisitDetails(id, getUser(), getRemoteHost()).getScheduledata();
    }

    @GET
    @Path("/getScheduledVisitDetails")
    @AuthorizedRoles({ROLE_SUPER_ADMIN, ROLE_RESOURCE_MANAGER, ROLE_SCHEDULER, ROLE_STUDY_STAFF, ROLE_FRONT_DESK, ROLE_GENERAL_VIEW})
    public String getScheduledVisitDetails(@QueryParam("id") final int id) {
        final Map<String, Object> map = new HashMap<String, Object>();

        final BookedVisit bookedVisit = service.getVisitDetails(id, getUser(), getRemoteHost());

        final Integer studyId = bookedVisit.getStudy().getId();

        map.put("studyId", studyId);
        map.put("visitId", bookedVisit.getId());
        map.put("studydetailname", bookedVisit.getStudy().getName());
        map.put("localId", bookedVisit.getStudy().getLocalId());
        map.put("studyPIdetailname", bookedVisit.getStudy().getInvestigatorLastNameOrNA());

        map.put("visitdetailname", bookedVisit.getVisitTemplate().getName());
        map.put("detailVisitTime", bookedVisit.getVisitTemplate().getDuration().toString());
        map.put("visitTypeId", Integer.toString(bookedVisit.getVisitType().getId()));
        map.put("relativeTime", bookedVisit.getVisitTemplate().getRelativeTime().toString());
        map.put("scheduledata", bookedVisit.getScheduledata());
        map.put("actionName", bookedVisit.getActionName());
        map.put("userdata", bookedVisit.getUserdata());

        SubjectMrn subjectMrn = bookedVisit.getSubjectMrnDecrypted();
        map.put("subjectdetailname", subjectMrn == null ? NO_SUBJECT_ASSIGNED : subjectMrn.getSubject().getFullName());
        map.put("subjectMrn", subjectMrn == null ? NA : subjectMrn.getMrn());

        long visitCommentsTotal = service.getTotalAppointmentComments(id);
        map.put("visitCommentsTotal", visitCommentsTotal);

        if (bookedVisit.getAppointmentStatus().getName().equalsIgnoreCase(CHECKED_IN_APPT_STATUS) && bookedVisit.getCheckInDate() != null) {
            map.put("visitdate", format(dateHourMin(), bookedVisit.getCheckInDate()));
            User checkInUser = bookedVisit.getCheckInUser();
            String checkInUsername;
            if (checkInUser != null) {
                checkInUsername = checkInUser.getEcommonsId();
            } else {
                checkInUsername = BookedVisitActivityLogStatics.USER_DATA_NOT_AVAILABLE;
            }
            map.put("visituser", "by user: " + checkInUsername);
        } else if (bookedVisit.getAppointmentStatus().getName().equalsIgnoreCase(CHECKED_OUT_APPT_STATUS) && bookedVisit.getCheckOutDate() != null) {
            map.put("visitdate", format(dateHourMin(), bookedVisit.getCheckOutDate()));
            map.put("check_visit_duration_readonly", bookedVisit.isVaryDuration());
            map.put("check_visit_activities_readonly", bookedVisit.isOmmittedActivities());
            User checkOutUser = bookedVisit.getCheckOutUser();
            String checkOutUsername;
            if (checkOutUser != null) {
                checkOutUsername = checkOutUser.getEcommonsId();
            } else {
                checkOutUsername = BookedVisitActivityLogStatics.USER_DATA_NOT_AVAILABLE;
            }
            map.put("visituser", "by user: " + checkOutUsername);
        } else if (bookedVisit.getAppointmentStatus().getName().equalsIgnoreCase(CANCELLED_APPT_STATUS) && bookedVisit.getCancelDate() != null) {
            map.put("visitdate", format(dateHourMin(), bookedVisit.getCancelDate()));
            map.put("visitreasons", bookedVisit.getCancelStatusReason().getName());
            map.put("visitcomments", bookedVisit.getComment());
            User cancelUser = bookedVisit.getCancelUser();
            String cancelUsername;
            if (cancelUser != null) {
                cancelUsername = cancelUser.getEcommonsId();
            } else {
                cancelUsername = BookedVisitActivityLogStatics.USER_DATA_NOT_AVAILABLE;
            }
            map.put("visituser", "by user: " + cancelUsername);
        }

        return gson.toJson(map);

    }

    @GET
    @Path("/getScheduledVisitHistory")
    @AuthorizedRoles({ROLE_SUPER_ADMIN})
    public String getScheduledVisitHistory(@QueryParam("id") final int id) {

        ScheduledVisitHistoryDTO scheduledVisitHistoryDTO = service.getVisitHistory(id, getUser(), getRemoteHost());

        return gson.toJson(scheduledVisitHistoryDTO);
    }


    /**
     * note: newSubjectMrnId = 0 means change to null subject, whose name is treated as 'N/A'
     */
    @GET
    @Path("/switchVisitSubject")
    @AuthorizedRoles({ROLE_SUPER_ADMIN, ROLE_RESOURCE_MANAGER, ROLE_SCHEDULER, ROLE_STUDY_STAFF, ROLE_FRONT_DESK})
    public String switchVisitSubject(
            @QueryParam("confirm") boolean confirm,
            @QueryParam("newSubjectMrnId") Integer newSubjectMrnId,
            @QueryParam("visitId") final Integer visitId,
            @QueryParam("homeScreen") final boolean homeScreen,
            @QueryParam("className") final String className) {

        SwitchSubjectResultDTO switchSubjectResultDTO;

        if (!confirm) {
            // this case happens first
            switchSubjectResultDTO = service.switchVisitSubject(newSubjectMrnId, visitId, homeScreen, className);
        }
        else {
            // that case happens second, but only if there are warnings that require the user to confirm the switch
            switchSubjectResultDTO = service.confirmSwitchVisitSubject(newSubjectMrnId, visitId, homeScreen, className);
        }

        return gson.toJson(switchSubjectResultDTO);

    }

    @GET
    @Path("/getRoomString")
    @AuthorizedRoles({ROLE_SUPER_ADMIN, ROLE_RESOURCE_MANAGER, ROLE_SCHEDULER, ROLE_STUDY_STAFF, ROLE_FRONT_DESK, ROLE_GENERAL_VIEW})
    public String getRoomString(@HeaderParam("User-Agent") final String header,
                                          @QueryParam("visitId") final Integer visitId) {
        String roomString = service.getRoomString(visitId);
        StatusAndMessageResponseDTO dto = new StatusAndMessageResponseDTO();
        dto.setSuccessful(true);
        dto.setMessage(roomString);
        return  gson.toJson(dto);
    }
}
