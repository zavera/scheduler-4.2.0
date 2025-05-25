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
import com.google.gson.reflect.TypeToken;
import edu.harvard.catalyst.scheduler.dto.*;
import edu.harvard.catalyst.scheduler.dto.request.*;
import edu.harvard.catalyst.scheduler.dto.response.*;
import edu.harvard.catalyst.scheduler.entity.*;
import edu.harvard.catalyst.scheduler.security.SchedulerSession;
import edu.harvard.catalyst.scheduler.security.SchedulerUserDetails;
import edu.harvard.catalyst.scheduler.service.ResourceService;
import edu.harvard.catalyst.scheduler.util.ServiceTestsDAOUtility;
import edu.harvard.catalyst.scheduler.util.TestUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.mock.web.MockHttpServletRequest;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.refEq;
import static org.mockito.Mockito.*;

/**
 * 
 * @author Ankit Panchamia
 * @date 03/24/2014
 *
 */

@RunWith(MockitoJUnitRunner.class)
public class ResourceResourceTest {

    ResourceResource resourceResource;

    private final SchedulerSession session = mock(SchedulerSession.class);
    private final HttpServletRequest request = mock(MockHttpServletRequest.class);
    private final SchedulerUserDetails schedulerUserDetails = mock(SchedulerUserDetails.class);
    private final User mockUserInSession = ServiceTestsDAOUtility.createMockUser();
    private final String mockHostnameInRequest = "mockHostnameInRequest";

    //@Autowired
    //private final ServletContext mockContext = mock(ServletContext.class);

    @Mock private ResourceService mockResourceService;
    
    @Before
    public void setUp() {

        /*MockServletContext sc = new MockServletContext();
        sc.setContextPath(contextPath);

        int pathLength = 0;
        if(contextPath.length() > 0) {
            pathLength += contextPath.length();
        }

        if(servletPath.length() > 0) {
            pathLength += servletPath.length();
        }

        request = new MockHttpServletRequest(sc, method, url);
        request.setContextPath(contextPath);
        request.setServletPath(servletPath);
        request.addHeader("host", host);*/

        resourceResource = new ResourceResource(
                mockResourceService);
        resourceResource.setSession(session);
        resourceResource.setRequest(request);

        when(session.getUserDetails()).thenReturn(schedulerUserDetails);
        when(schedulerUserDetails.getUser()).thenReturn(mockUserInSession);
        when(request.getRemoteHost()).thenReturn(mockHostnameInRequest);
    }
    
    @Test
    public void testGetResourceData() throws Exception {
        List<ResourcesResponse> resourceList = setupResourceResponses();
        User user = ServiceTestsDAOUtility.createMockUser();
        when(mockResourceService.getResourcesWithSublocation( "","", "active", 1, 5, user, "templatePath", null)).thenReturn(resourceList);
        List<ResourcesResponse> result = resourceResource.getResourceData("", "", "active", 1, 5, user, "templatePath", null);
        TestUtils.assertNonNullNonEmpty(result);
        TestUtils.assertNonNullAndHasThisMany(result, 2);
    }
    
    private List<ResourcesResponse> setupResourceResponses() throws Exception{
        ResourcesResponse resource = new ResourcesResponse(1, 1, "resource1", "resourceType1", 1, "sublocation1", true, (long) 2);
        ResourcesResponse resource1 = new ResourcesResponse(2, 2, "resource2", "resourceType2", 2, "sublocation2", true, (long) 2);
        List<ResourcesResponse> resources = new ArrayList<ResourcesResponse>();
        resources.add(resource);
        resources.add(resource1);
        return resources;
    }
    
    @Test
    public void testResourceDefaultSchedule() throws Exception {
        List<ResourceScheduleResponse> resourceScheduleList = setupResourceSchedule();
        when(mockResourceService.getResourceSchedules(1, false, "","", 1, 5)).thenReturn(resourceScheduleList);
        List<ResourceScheduleResponse> result = resourceResource.getResourcesSchedule(1, false, "", "", 1, 5);
        TestUtils.assertNonNullNonEmpty(result);
        TestUtils.assertNonNullAndHasThisMany(result, 2);
    }
    
    private List<ResourceScheduleResponse> setupResourceSchedule() throws Exception {
        ResourceScheduleResponse resourceSchedule = new ResourceScheduleResponse(1, 1, "00:00", "23:59", 4, (long) 2);       
        ResourceScheduleResponse resourceSchedule1 = new ResourceScheduleResponse(1, 1, "00:00", "23:00", 2, (long) 2);              
        List<ResourceScheduleResponse> resourceSchedules = new ArrayList<ResourceScheduleResponse>();
        resourceSchedules.add(resourceSchedule);
        resourceSchedules.add(resourceSchedule1);
        return resourceSchedules;
    }
    
    @Test
    public void testDefaultScheduleById() throws Exception {
        ResourceScheduleResponse expected = new ResourceScheduleResponse(1, 1, "00:00", "23:59", 4, (long) 2);
        when(mockResourceService.getResourceDefaultSchedule(1)).thenReturn(expected);
        ResourceScheduleResponse actual = resourceResource.getDefaultScheduleById(1); 
        assertEquals(expected, actual);
    }

    @Test
    public void testGetResourceDetail() {
        int resourceId = 33;
        ResourcesResponse expectedResponse = new ResourcesResponse(
                23, 22, "some resource", ResourceType.Lab.getName(),
                43, "some sublocation", true, 123L
        );
        when(mockResourceService.getResourceDetail(resourceId, mockUserInSession, mockHostnameInRequest)).thenReturn(expectedResponse);
        String actualResponseString = resourceResource.getResourceDetail(resourceId);
        String expectedResponseString = resourceResource.gson.toJson(expectedResponse);
        assertEquals(expectedResponseString, actualResponseString);
    }


    @Test
    public void testResourceDetailById() throws Exception {
        User user = ServiceTestsDAOUtility.createMockUser();
        ResourcesResponse expected = new ResourcesResponse(1, 1, "resourceName", "resourceTypeName", 1, "sublocationName", true, (long) 1);
        when(mockResourceService.getResourceDetail(1, user, "templatePath")).thenReturn(expected);
        ResourcesResponse actual = resourceResource.loadResourceDetail(1, user, "templatePath"); 
        assertEquals(expected, actual);
    }
    
    @Test
    public void testResourceAlternates() throws Exception {
        List<ResourcesResponse> resourceAlternateList = setupResourceAlternate();           
        when(mockResourceService.getResourceAlternates(1, "","", 1, 5)).thenReturn(resourceAlternateList);
        List<ResourcesResponse> result = resourceResource.loadResourceAlternateList(1, "", "", 1, 5);
        TestUtils.assertNonNullNonEmpty(result);
        TestUtils.assertNonNullAndHasThisMany(result, 2);
    }
    
    private List<ResourcesResponse> setupResourceAlternate() throws Exception {
        ResourcesResponse resourceAlt = new ResourcesResponse(1, 1, "resource1", "resourceType1", 1,"sublocation1", true, (long) 2);
        ResourcesResponse resourceAlt1 = new ResourcesResponse(2, 2, "resource2", "resourceType2", 2, "sublocation2", true, (long) 2);
        List<ResourcesResponse> resourceAlternates = new ArrayList<ResourcesResponse>();
        resourceAlternates.add(resourceAlt);
        resourceAlternates.add(resourceAlt1);
        return resourceAlternates;
    }
    
    @Test
    public void testAddResourceAlternates() throws Exception {
        List<ResourcesResponse> resourceAlternateList = setupResourceAlternate();           
        when(mockResourceService.getAlternateResources(1, "","", 1, 5)).thenReturn(resourceAlternateList);
        List<ResourcesResponse> result = resourceResource.loadAddAlternateResources(1, "", "", 1, 5);
        TestUtils.assertNonNullNonEmpty(result);
        TestUtils.assertNonNullAndHasThisMany(result, 2);
    }
    

    @Test
    public void testSublocationClosureIntervals() throws Exception {
        List<SublocationClosureIntervalResponse> resourceAlternateList = setupSublocationClosure();
        when(mockResourceService.getSublocationClosureIntervals("","", 1, 5)).thenReturn(resourceAlternateList);
        List<SublocationClosureIntervalResponse> result = resourceResource.loadSublocationIntervals("", "", 1, 5);
        TestUtils.assertNonNullNonEmpty(result);
    }
    
    private List<SublocationClosureIntervalResponse> setupSublocationClosure() throws Exception {
        SublocationClosureIntervalResponse resourceSub = new SublocationClosureIntervalResponse(1, new Date(), new Date(), "reason1", "sublocation1", (long) 2);       
        SublocationClosureIntervalResponse resourceSub1 = new SublocationClosureIntervalResponse(2, new Date(), new Date(), "reason2", "sublocation2", (long) 2);         
        List<SublocationClosureIntervalResponse> resourceSublocations = new ArrayList<SublocationClosureIntervalResponse>();
        resourceSublocations.add(resourceSub);
        resourceSublocations.add(resourceSub1);
        return resourceSublocations;
    }

    private List<Resource> setupResources() throws Exception{
        Resource resource = new Resource();
        resource.setId(1);
        resource.setName("Resource 1");
        Resource resource1 = new Resource();
        resource1.setId(2);
        resource1.setName("Resource 2");
        List<Resource> resources = Lists.newArrayList();
        resources.add(resource);
        resources.add(resource1);
        return resources;
    }

    private List<LineLevelAnnotations> setupLineLevelAnnotationsList() {
        LineLevelAnnotations lineLevelAnnotations = new LineLevelAnnotations();
        lineLevelAnnotations.setId(1);
        lineLevelAnnotations.setName("Annotations 1");
        lineLevelAnnotations.setQuantifiable(true);
        LineLevelAnnotations lineLevelAnnotations1 = new LineLevelAnnotations();
        lineLevelAnnotations1.setId(2);
        lineLevelAnnotations1.setName("Annotations 2");
        lineLevelAnnotations1.setQuantifiable(false);
        List<LineLevelAnnotations> lineLevelAnnotationsList = new ArrayList<LineLevelAnnotations>();
        lineLevelAnnotationsList.add(lineLevelAnnotations);
        lineLevelAnnotationsList.add(lineLevelAnnotations1);
        return lineLevelAnnotationsList;
    }

    private SublocationClosureIntervalDTO setupSublocationClosureIntervalDTO(
        Date startTime, Date endTime,
        String reason, int sublocationId
    ) {

        SublocationClosureIntervalDTO sublocationClosureIntervalDTO = new SublocationClosureIntervalDTO();
        sublocationClosureIntervalDTO.setStartTime(startTime);
        sublocationClosureIntervalDTO.setEndTime(endTime);
        sublocationClosureIntervalDTO.setReason(reason);
        sublocationClosureIntervalDTO.setSublocationId(sublocationId);

        return sublocationClosureIntervalDTO;

    }

    private SublocationClosureInterval setupSublocationClosureInterval(
            Date startTime, Date endTime,
            String reason, String institutionName, String institutionLongName,
            String sublocationName,
            int sublocationId, int intervalId
    ) {

        Institution institution = new Institution(institutionName, institutionLongName);
        Sublocation sublocation = new Sublocation(sublocationName, institution);
        sublocation.setId(sublocationId);
        SublocationClosureInterval sublocationClosureInterval = new SublocationClosureInterval(startTime, endTime, reason, sublocation);
        sublocationClosureInterval.setId(intervalId);

        return sublocationClosureInterval;

    }

    @Test
    public void testGetSelectedResourceLineLevelAnnotationses() throws Exception {
        List<LineLevelAnnotations> lineLevelAnnotationsList = setupLineLevelAnnotationsList();
        int resourceId = 1;
        String annotations = "[1, 2, 3]";
        String mode = "override_new";
        when(mockResourceService.getSelectedResourceAnnotations(annotations, mode, resourceId)).thenReturn(lineLevelAnnotationsList);
        List<LineLevelAnnotations> result = resourceResource.getSelectedResourceLineLevelAnnotationses(annotations, mode, resourceId);
        TestUtils.assertNonNullNonEmpty(result);
        TestUtils.assertNonNullAndHasThisMany(result, 2);
    }

    @Test
    public void testGetResourceLineLevelAnnotationses() throws Exception {
        List<LineLevelAnnotations> lineLevelAnnotationsList = setupLineLevelAnnotationsList();
        int resourceId = 1;
        when(mockResourceService.getResourceAnnotations(resourceId)).thenReturn(lineLevelAnnotationsList);
        List<LineLevelAnnotations> result = resourceResource.getResourceLineLevelAnnotationses(resourceId);
        TestUtils.assertNonNullNonEmpty(result);
        TestUtils.assertNonNullAndHasThisMany(result, 2);
    }

    @Test public void testNoArgConstructor() {

        ResourceResource resourceResource = new ResourceResource();

    }

    @Test
    public void testGetResourceAnnotations() {

        int resourceId = 9;

        List<LineLevelAnnotations> lineLevelAnnotationsList = setupLineLevelAnnotationsList();
        when(mockResourceService.getResourceAnnotations(resourceId)).thenReturn(lineLevelAnnotationsList);

        String responseString = resourceResource.getResourceAnnotations(resourceId);

        GetResourcesDTO responseDTO = resourceResource.gson.fromJson(responseString, GetResourcesDTO.class);
        int numberOfAnnotationsCreated = lineLevelAnnotationsList.size();
        int numberOfAnnotationsReturned = responseDTO.getResourceAnnotationsValues().size();
        assertEquals("response should contain " + numberOfAnnotationsCreated + " line-level annotations", numberOfAnnotationsCreated, numberOfAnnotationsReturned);

    }

    @Test
    public void testGetSelectedAnnotations() {

        int resourceId = 9;
        String annotationIdsString = "1, 2";
        String mode = "not tested, arbitrary value";

        List<LineLevelAnnotations> lineLevelAnnotationsList = setupLineLevelAnnotationsList();
        when(mockResourceService.getSelectedResourceAnnotations(annotationIdsString, mode, resourceId)).thenReturn(lineLevelAnnotationsList);

        String responseString = resourceResource.getSelectedAnnotations(annotationIdsString, mode, resourceId);

        GetResourcesDTO responseDTO = resourceResource.gson.fromJson(responseString, GetResourcesDTO.class);
        int numberOfAnnotationsCreated = lineLevelAnnotationsList.size();
        int numberOfAnnotationsReturned = responseDTO.getAnnotationsStore().size();
        assertEquals("response should contain " + numberOfAnnotationsCreated + " line-level annotations", numberOfAnnotationsCreated, numberOfAnnotationsReturned);

    }

    @Test
    public void testGetBookedResourceAnnotations() {

        int templateResourceId = 99;
        List<LineLevelAnnotations> lineLevelAnnotationsList = setupLineLevelAnnotationsList();
        when(mockResourceService.getBookedResourceAnnotations(templateResourceId)).thenReturn(lineLevelAnnotationsList);

        String responseString = resourceResource.getBookedResourceAnnotations(templateResourceId);

        GetResourcesDTO responseDTO = resourceResource.gson.fromJson(responseString, GetResourcesDTO.class);
        int numberOfAnnotationsCreated = lineLevelAnnotationsList.size();
        int numberOfAnnotationsReturned = responseDTO.getAnnotationsStore().size();
        assertEquals("response should contain " + numberOfAnnotationsCreated + " line-level annotations", numberOfAnnotationsCreated, numberOfAnnotationsReturned);

    }

    @Test
    public void testCreateSublocationClosureInterval() {

        Date startTime = new Date();
        Date endTime = new Date();
        String reason = "some reason";
        String sublocationName = "sub-location name";
        String institutionName = "institution name";
        String institutionLongName = "institution long name";
        int sublocationId = 88;
        int intervalId = 99;

        SublocationClosureInterval sublocationClosureInterval = setupSublocationClosureInterval(
                startTime, endTime,
                reason, institutionName, institutionLongName,
                sublocationName,
                sublocationId, intervalId
        );

        SublocationClosureIntervalDTO sublocationClosureIntervalDTO = setupSublocationClosureIntervalDTO(
                startTime, endTime,
                reason, intervalId
        );

        String dataString = resourceResource.gson.toJson(sublocationClosureIntervalDTO, SublocationClosureIntervalDTO.class);

        when(mockResourceService.createSublocationClosureInterval(
                refEq(sublocationClosureIntervalDTO), eq(mockUserInSession), eq(mockHostnameInRequest))).thenReturn(sublocationClosureInterval);

        String responseString = resourceResource.createSublocationClosureInterval(dataString);
        SublocationClosureInterval newInterval = resourceResource.gson.fromJson(responseString, SublocationClosureInterval.class);

        assertEquals(new Integer(intervalId), newInterval.getId());
        assertEquals(new Integer(sublocationId), newInterval.getSublocation().getId());
        assertEquals(startTime, newInterval.getStartTime());
        assertEquals(endTime, newInterval.getEndTime());
        assertEquals(reason, newInterval.getReason());
        assertEquals(institutionName, newInterval.getSublocation().getInstitution().getName());
        assertEquals(institutionLongName, newInterval.getSublocation().getInstitution().getLongName());
    }

    @Test
    public void testAssignResourceAlternatives() {

        AssignResourceAlternativesDTO assignResourceAlternativesDTO = new AssignResourceAlternativesDTO();

        String dataString = resourceResource.gson.toJson(assignResourceAlternativesDTO, AssignResourceAlternativesDTO.class);
        BooleanResultDTO expectedResultDTO = new BooleanResultDTO(true);

        when(mockResourceService.addResourceAlternatives(refEq(assignResourceAlternativesDTO), eq(mockUserInSession), eq(mockHostnameInRequest))).thenReturn(expectedResultDTO);

        String responseString = resourceResource.assignResourceAlternatives(dataString);
        BooleanResultDTO actualResultDTO = resourceResource.gson.fromJson(responseString, BooleanResultDTO.class);

        assertEquals(expectedResultDTO.isResult(), actualResultDTO.isResult());

    }

    @Test
    public void testDeleteSublocationClosureInterval() {

        Date startTime = new Date();
        Date endTime = new Date();
        String reason = "some reason";
        String sublocationName = "sub-location name";
        String institutionName = "institution name";
        String institutionLongName = "institution long name";
        int sublocationId = 88;
        int intervalId = 99;

        SublocationClosureInterval sublocationClosureInterval = setupSublocationClosureInterval(
                startTime, endTime,
                reason, institutionName, institutionLongName,
                sublocationName,
                sublocationId, intervalId
        );

        SublocationClosureIntervalDTO sublocationClosureIntervalDTO = setupSublocationClosureIntervalDTO(
                startTime, endTime,
                reason, intervalId
        );

        String dataString = resourceResource.gson.toJson(sublocationClosureIntervalDTO, SublocationClosureIntervalDTO.class);
        BooleanResultDTO expectedResultDTO = new BooleanResultDTO(true);

        when(mockResourceService.deleteSublocationClosureInterval(refEq(sublocationClosureIntervalDTO), eq(mockUserInSession), eq(mockHostnameInRequest))).thenReturn(expectedResultDTO);

        String responseString = resourceResource.deleteSublocationClosureInterval(dataString);
        BooleanResultDTO actualResultDTO = resourceResource.gson.fromJson(responseString, BooleanResultDTO.class);

        assertEquals(expectedResultDTO.isResult(), actualResultDTO.isResult());

    }

    @Test
    public void testGetRoomResources() throws Exception {

        String sublocationSearchString = "this string does not matter in this test";

        List<Resource> resources = setupResources();

        when(mockResourceService.getRoomResources(sublocationSearchString)).thenReturn(resources);

        String responseString = resourceResource.getRoomResources(sublocationSearchString);

        Type resourceListType = new TypeToken<ArrayList<Resource>>(){}.getType();
        List<Resource> actualResources = resourceResource.gson.fromJson(responseString, resourceListType);

        assertEquals(resources, actualResources);

    }

    @Test
    public void testGetNursingResources() throws Exception {

        String sublocationSearchString = "this string does not matter in this test";

        List<Resource> resources = setupResources();

        when(mockResourceService.getNursingResources(mockHostnameInRequest, mockUserInSession, sublocationSearchString)).thenReturn(resources);

        String responseString = resourceResource.getNursingResources(sublocationSearchString);

        Type resourceListType = new TypeToken<ArrayList<Resource>>(){}.getType();
        List<Resource> actualResources = resourceResource.gson.fromJson(responseString, resourceListType);

        assertEquals(resources, actualResources);

    }

    @Test
    public void testGetNutritionResources() throws Exception {

        String sublocationSearchString = "this string does not matter in this test";

        List<Resource> resources = setupResources();

        when(mockResourceService.getNutritionResources(mockHostnameInRequest, mockUserInSession, sublocationSearchString)).thenReturn(resources);

        String responseString = resourceResource.getNutritionResources(sublocationSearchString);

        Type resourceListType = new TypeToken<ArrayList<Resource>>(){}.getType();
        List<Resource> actualResources = resourceResource.gson.fromJson(responseString, resourceListType);

        assertEquals(resources, actualResources);

    }

    @Test
    public void testGetResourcesData() throws Exception {

        String sortBy = "name";
        String orderBy = "asc";
        String status = "some status";
        int page = 2;
        int maxResults = 10;

        List<ResourcesResponse> resourcesResponses = setupResourceResponses();

        // TODO: modify SearchItem so its fields can be set from this test
        // TODO: modify SearchDTO so its field searchItems can be initialized
//        SearchDTO.SearchItem searchItem = searchDTO. new SearchItem();
//        searchDTO.getSearchItems().add( searchItem );
//        String searchJson = resourceResource.gson.toJson(searchDTO);

        String searchJson = "{'searchItems': [{'key': 'a', 'value': 'b'}]}";
        SearchDTO searchDTO = resourceResource.gson.fromJson(searchJson, SearchDTO.class);

        // TODO: For some reason when specifying the last argument as refEq(searchDTO) the method stub is not triggered
        // So instead I specify it as any SearchDTO object
        when(mockResourceService.getResourcesWithSublocation(
                eq(sortBy), eq(orderBy), eq(status), eq(page), eq(maxResults),
                refEq(mockUserInSession), refEq(mockHostnameInRequest),
                any(SearchDTO.class))).thenReturn(resourcesResponses);

        String responseString = resourceResource.getResourcesData(
                sortBy, orderBy,
                status, page,
                maxResults, searchJson
        );

        // The properties of ResourcesResponse are private, so we can only access them in order to test them
        // via some kind of reflection/introspection. One easy way to do that is to use gson to
        // convert the expected list or ResourcesResponses to JSON and then compare that to the
        // actual JSON string returned by the resource call.
        Type resourceListType = new TypeToken<ArrayList<ResourcesResponse>>(){}.getType();
        String expectedResponseString = resourceResource.gson.toJson(resourcesResponses, resourceListType);
        assertEquals(
                "expect and actual JSON strings for resource responses should be identical",
                expectedResponseString,
                responseString
        );

    }

    @Test
    public void testGetResourceSchedules() throws Exception {

        int resourceId = 777;
        boolean isOverride = false;
        String sortBy = "name";
        String orderBy = "asc";
        int page = 2;
        int maxResults = 10;

        List<ResourceScheduleResponse> resourceScheduleResponse = setupResourceSchedule();

        when(mockResourceService.getResourceSchedules(
                resourceId, isOverride, sortBy, orderBy, page, maxResults
        )).thenReturn(resourceScheduleResponse);

        String responseString = resourceResource.getResourceSchedules(
                resourceId,
                sortBy, orderBy,
                page,
                maxResults
        );

        // The properties of ResourceScheduleResponse are private, so we can only access them in order to test them
        // via some kind of reflection/introspection. One easy way to do that is to use gson to
        // convert the expected list or ResourceScheduleResponses to JSON and then compare that to the
        // actual JSON string returned by the resource call.
        Type ResourceScheduleResponseListType = new TypeToken<ArrayList<ResourceScheduleResponse>>(){}.getType();
        String expectedResponseString = resourceResource.gson.toJson(resourceScheduleResponse, ResourceScheduleResponseListType);
        assertEquals(
                "expect and actual JSON strings for resource responses should be identical",
                expectedResponseString,
                responseString
        );

    }

    @Test
    public void testGetDefaultSchedule() {

        int id = 987;
        int dayOfWeek = 6;
        String startTime = "123456";
        String endTime = "234565";
        int quantity = 10;
        long total = 2;

        ResourceScheduleResponse resourceScheduleResponse = new ResourceScheduleResponse(
            id, dayOfWeek, startTime, endTime, quantity, total
        );

        when(mockResourceService.getResourceDefaultSchedule(id)).thenReturn(resourceScheduleResponse);

        String responseString = resourceResource.getDefaultSchedule(id);

        ResourceScheduleResponse actualResponse = resourceResource.gson.fromJson(responseString, ResourceScheduleResponse.class);
        String expectedResponseString = resourceResource.gson.toJson(resourceScheduleResponse, ResourceScheduleResponse.class);
        assertEquals(
                "expect and actual JSON strings should be identical",
                expectedResponseString,
                responseString
        );

    }

    @Test
    public void testGetResourceAlternates() throws Exception {

        int resourceId = 3;
        String sortBy = "blah";
        String orderBy = "asc";
        int page = 7;
        int maxResults = 2;

        Type ResourcesResponseListType = new TypeToken<ArrayList<ResourcesResponse>>(){}.getType();

        List<ResourcesResponse> resourceAlternates = setupResourceAlternate();

        when(mockResourceService.getResourceAlternates(resourceId, sortBy, orderBy, page, maxResults)).thenReturn(resourceAlternates);

        String responseString = resourceResource.getResourceAlternates(resourceId, sortBy, orderBy, page, maxResults);

        ResponseWithCount expectedResourceAlternatesWithCount = new ResponseWithCount();
        expectedResourceAlternatesWithCount.setCount(resourceAlternates.size());
        expectedResourceAlternatesWithCount.setJsonData(
                resourceResource.gson.toJson(resourceAlternates, ResourcesResponseListType)
        );

        String expectedResponseString = resourceResource.gson.toJson(expectedResourceAlternatesWithCount, ResponseWithCount.class);

        assertEquals(
                "expect and actual JSON strings should be identical",
                expectedResponseString,
                responseString
        );

        // this time there are 0 Resources found

        resourceAlternates = new ArrayList<ResourcesResponse>();

        when(mockResourceService.getResourceAlternates(resourceId, sortBy, orderBy, page, maxResults)).thenReturn(resourceAlternates);

        responseString = resourceResource.getResourceAlternates(resourceId, sortBy, orderBy, page, maxResults);

        expectedResourceAlternatesWithCount = new ResponseWithCount();
        expectedResourceAlternatesWithCount.setCount(resourceAlternates.size());
        expectedResourceAlternatesWithCount.setJsonData(
                resourceResource.gson.toJson(resourceAlternates, ResourcesResponseListType)
        );

        expectedResponseString = resourceResource.gson.toJson(expectedResourceAlternatesWithCount, ResponseWithCount.class);

        assertEquals(
                "expect and actual JSON strings should be identical",
                expectedResponseString,
                responseString
        );

    }

    @Test
    public void testGetAlternateResources() throws Exception {

        int resourceId = 3;
        String sortBy = "blah";
        String orderBy = "asc";
        int page = 7;
        int maxResults = 2;

        List<ResourcesResponse> alternateResources = setupResourceAlternate();

        when(mockResourceService.getAlternateResources(resourceId, sortBy, orderBy, page, maxResults)).thenReturn(alternateResources);
        String responseString = resourceResource.getAlternateResources(resourceId, sortBy, orderBy, page, maxResults);

        // The properties of ResourcesResponse are private, so we can only access them in order to test them
        // via some kind of reflection/introspection. One easy way to do that is to use gson to
        // convert the expected list or ResourcesResponses to JSON and then compare that to the
        // actual JSON string returned by the resource call.
        Type resourceListType = new TypeToken<ArrayList<ResourcesResponse>>(){}.getType();
        String expectedResponseString = resourceResource.gson.toJson(alternateResources, resourceListType);
        assertEquals(
                "expect and actual JSON strings for resource responses should be identical",
                expectedResponseString,
                responseString
        );

    }

    /*
    This test is identical to testGetResourceSchedules(), except that it calls an identica method
    with a different name. The underlying reason could be that authorizations for both methods are
    different
     */
    @Test
    public void testGetTemporaryAdjustments() throws Exception {

        int resourceId = 777;
        String sortBy = "name";
        String orderBy = "asc";
        int page = 2;
        int maxResults = 10;

        List<ResourceScheduleResponse> resourceScheduleResponse = setupResourceSchedule();

        when(mockResourceService.getResourceSchedules(
                resourceId, true, sortBy, orderBy, page, maxResults
        )).thenReturn(resourceScheduleResponse);

        String responseString = resourceResource.getTemporaryAdjustments(
                resourceId,
                sortBy, orderBy,
                page,
                maxResults
        );

        // The properties of ResourceScheduleResponse are private, so we can only access them in order to test them
        // via some kind of reflection/introspection. One easy way to do that is to use gson to
        // convert the expected list or ResourceScheduleResponses to JSON and then compare that to the
        // actual JSON string returned by the resource call.
        Type ResourceScheduleResponseListType = new TypeToken<ArrayList<ResourceScheduleResponse>>(){}.getType();
        String expectedResponseString = resourceResource.gson.toJson(resourceScheduleResponse, ResourceScheduleResponseListType);
        assertEquals(
                "expect and actual JSON strings for resource responses should be identical",
                expectedResponseString,
                responseString
        );

    }

    @Test
    public void testGetSublocationClosureIntervals() throws Exception {

        String sortBy = "name";
        String orderBy = "asc";
        int page = 2;
        int maxResults = 10;

        List<SublocationClosureIntervalResponse> sublocationClosures = setupSublocationClosure();

        when(mockResourceService.getSublocationClosureIntervals(sortBy, orderBy, page, maxResults)).thenReturn(sublocationClosures);

        String responseString = resourceResource.getSublocationClosureIntervals(
                sortBy, orderBy, page, maxResults
        );

        Type SublocationClosureIntervalResponseListType = new TypeToken<ArrayList<SublocationClosureIntervalResponse>>() {
        }.getType();
        String expectedResponseString = resourceResource.gson.toJson(sublocationClosures, SublocationClosureIntervalResponseListType);
        assertEquals(
                "expect and actual JSON strings for resource responses should be identical",
                expectedResponseString,
                responseString
        );

    }

    @Test
    public void testModifyResource() {

        int resourceId = 3;
        int sublocationId = 4;
        String resourceName = "my resource";
        boolean activate = true;

        ModifyResourceRequest modifyResourceRequest = new ModifyResourceRequest(resourceId, sublocationId, resourceName, activate);
        String requestString = resourceResource.gson.toJson(modifyResourceRequest, ModifyResourceRequest.class);

        StatusAndMessageResponseDTO expectedResponseDTO = new StatusAndMessageResponseDTO();
        expectedResponseDTO.setMessage("");
        expectedResponseDTO.setSuccessful(true);

        when(mockResourceService.updateResource(refEq(modifyResourceRequest), refEq(mockUserInSession), refEq(mockHostnameInRequest))).thenReturn(expectedResponseDTO);
        String responseString = resourceResource.updateResource(requestString);

        String expectedResponseString = resourceResource.gson.toJson(expectedResponseDTO);
        assertEquals(expectedResponseString, responseString);

    }

    @Test
    public void testCreateResource() {

        String resourceName = "Hello resource";
        String resourceType = "resource type";
        int sublocationId = 87;
        boolean active = true;
        String sublocationName = "my sublocation";
        int resourceId = 23;

        CreateResourceRequestDTO requestDTO = new CreateResourceRequestDTO();
        requestDTO.setName(resourceName);
        requestDTO.setResourceType(resourceType);
        requestDTO.setSublocationId(sublocationId);
        requestDTO.setActive(active);

        CreateResourceResponse responseDTO = new CreateResourceResponse();
        responseDTO.setName(resourceName);
        responseDTO.setResourceType(resourceType);
        responseDTO.setSublocationId(sublocationId);
        responseDTO.setSublocationName(sublocationName);
        responseDTO.setResourceId(resourceId);

        when(mockResourceService.createResource(refEq(requestDTO), refEq(mockUserInSession), refEq(mockHostnameInRequest))).thenReturn(responseDTO);

        String requestString = resourceResource.gson.toJson(requestDTO);
        String responseString = resourceResource.createResource(requestString);
        String expecteResponseString = resourceResource.gson.toJson(responseDTO);
        assertEquals(expecteResponseString, responseString);

    }

    @Test
    public void testGetResourceAnnotationsNames() {

        int resourceId = 22;

        List<LineLevelAnnotations> annotationsList = setupLineLevelAnnotationsList();

        when(mockResourceService.getResourceAnnotations(resourceId)).thenReturn(annotationsList);

        String responseString = resourceResource.getResourceAnnotationsNames(resourceId);

        AnnotationsNamesResponseDTO expectedResultDTO = AnnotationsNamesResponseDTO.fromAnnotationsList(annotationsList);
        String expectedResponseString = resourceResource.gson.toJson(expectedResultDTO);
        assertEquals(expectedResponseString, responseString);

    }

    @Test
    public void testDeleteDefaultSchedule() {

        int resourceScheduleId = 76;

        // TODO: Why are we expecting a DTO named ***Request? shouldn't it be ***Response

        BooleanRequest responseDTO = new BooleanRequest();
        responseDTO.setResult(true);

        when(mockResourceService.deleteDefaultAvailability(resourceScheduleId, mockUserInSession, mockHostnameInRequest)).thenReturn(responseDTO);

        String responseString = resourceResource.deleteDefaultSchedule(resourceScheduleId);
        String expectedResponseString = resourceResource.gson.toJson(responseDTO);
        assertEquals(expectedResponseString, responseString);

    }

    @Test
    public void testRemoveResourceAlternative() {

        int resourceId = 76;
        int alternateResourceId = 54;

        // TODO: Why are we expecting a DTO named ***Request? shouldn't it be ***Response

        BooleanRequest responseDTO = new BooleanRequest();
        responseDTO.setResult(true);

        when(mockResourceService.deleteResourceAlternative(resourceId, alternateResourceId, mockUserInSession, mockHostnameInRequest)).thenReturn(responseDTO);

        String responseString = resourceResource.removeResourceAlternative(resourceId, alternateResourceId);
        String expectedResponseString = resourceResource.gson.toJson(responseDTO);
        assertEquals(expectedResponseString, responseString);

    }

    @Test
    public void testChangeResourceStatus() {

        int resourceId = 1;
        int sublocationId = 2;
        boolean active = true;

        BooleanResultDTO responseDTO = new BooleanResultDTO(true);
        when(mockResourceService.changeResourceStatus(resourceId, sublocationId, active)).thenReturn(responseDTO);

        String responseString = resourceResource.changeResourceStatus(resourceId, sublocationId, active);

        String expectedResponseString = resourceResource.gson.toJson(responseDTO);
        assertEquals(expectedResponseString, responseString);

    }

    @Test
    public void testActivateResources() {

        List<Integer> resourceIds = new ArrayList<Integer>(Arrays.asList(new Integer[]{1, 2}));
        ResourceIdsRequestDTO requestDTO = new ResourceIdsRequestDTO();
        requestDTO.setResourcesIds(resourceIds);
        String requestString = resourceResource.gson.toJson(requestDTO);

        ResourcesBooleanResponseDTO responseDTO = new ResourcesBooleanResponseDTO(true);

        when(mockResourceService.activateResources(refEq(requestDTO))).thenReturn(responseDTO);

        String responseString = resourceResource.activateResources(requestString);

        String expectedResponseString = resourceResource.gson.toJson(responseDTO);
        assertEquals(expectedResponseString, responseString);

    }

    @Test
    public void testGetResourcesAvailableForGenderBlockRestriction() throws Exception {

        int resourceId = 42; // at last!

        List<Resource> resourceList = setupResources();

        when(mockResourceService.getResourcesAvailableForGenderBlockRestriction(resourceId)).thenReturn(resourceList);

        String responseString = resourceResource.getResourcesAvailableForGenderBlockRestriction(resourceId);

        SimpleResourceListResponse expectedResponseDTO = SimpleResourceListResponse.fromResourcesList(resourceList);
        String expectedResponseString = resourceResource.gson.toJson(expectedResponseDTO);
        assertEquals(expectedResponseString, responseString);

    }

    @Test
    public void testRemoveRestriction() {

        int resourceId = 24;

        BooleanRequest response = new BooleanRequest();
        response.setResult(true);

        when(mockResourceService.deleteRestriction(resourceId, mockUserInSession, mockHostnameInRequest)).thenReturn(response);

        String responseString = resourceResource.removeRestriction(resourceId);

        String expectedResponseString = resourceResource.gson.toJson(response);
        assertEquals(expectedResponseString, responseString);

    }


    @Test
    public void testAddRestriction() {

        int resourceId = 33;
        int sharedResourceId = 44;
        String notes = "aabbcc";

        AddOrModifyRestrictionRequestDTO requestDTO = new AddOrModifyRestrictionRequestDTO();
        requestDTO.setResourceId(resourceId);
        requestDTO.setSharedResourceId(sharedResourceId);
        requestDTO.setNotes(notes);

        String requestString = resourceResource.gson.toJson(requestDTO);

        BooleanRequest responseDTO = new BooleanRequest();
        responseDTO.setResult(true);

        when(mockResourceService.addRestriction(refEq(requestDTO), eq(mockUserInSession), eq(mockHostnameInRequest))).thenReturn(responseDTO);

        String responseString = resourceResource.addRestriction(requestString);
        String expectedResponseString = resourceResource.gson.toJson(responseDTO);
        assertEquals(expectedResponseString, responseString);
    }

    @Test
    public void testModifyRestriction() {

        int resourceId = 33;
        int sharedResourceId = 44;
        String notes = "aabbcc";

        AddOrModifyRestrictionRequestDTO requestDTO = new AddOrModifyRestrictionRequestDTO();
        requestDTO.setResourceId(resourceId);
        requestDTO.setSharedResourceId(sharedResourceId);
        requestDTO.setNotes(notes);

        String requestString = resourceResource.gson.toJson(requestDTO);

        BooleanRequest responseDTO = new BooleanRequest();
        responseDTO.setResult(true);

        when(mockResourceService.modifyRestriction(refEq(requestDTO), eq(mockUserInSession), eq(mockHostnameInRequest))).thenReturn(responseDTO);

        String responseString = resourceResource.modifyRestriction(requestString);
        String expectedResponseString = resourceResource.gson.toJson(responseDTO);
        assertEquals(expectedResponseString, responseString);

    }

    @Test
    public void testGetAllResourceNames() {

        List<String> resourceNames = Arrays.asList(new String[]{"hello", "goodbye"});

        when(mockResourceService.getAllResourceNames(mockUserInSession, mockHostnameInRequest)).thenReturn(resourceNames);

        String responseString = resourceResource.getAllResourceNames();
        Type StringListType = new TypeToken<ArrayList<String>>() {}.getType();
        String expectedResponseString = resourceResource.gson.toJson(resourceNames, StringListType);
        assertEquals(expectedResponseString, responseString);

    }

    @Test
    public void testAddAnnotations() {

        int resourceId = 776;
        List<Integer> annotationIds = Arrays.asList(new Integer[]{1, 4, 7});

        AnnotationsIdRequestDTO requestDTO = new AnnotationsIdRequestDTO();
        requestDTO.setResourceId(resourceId);
        requestDTO.setAnnotationIds(annotationIds);
        String requestString = resourceResource.gson.toJson(requestDTO);

        BooleanResultDTO responseDTO = new BooleanResultDTO(true);

        when(mockResourceService.addAnnotations(refEq(requestDTO), eq(mockUserInSession), eq(mockHostnameInRequest))).thenReturn(responseDTO);

        String responseString = resourceResource.addAnnotations(requestString);
        String expectedResponseString = resourceResource.gson.toJson(responseDTO);
        assertEquals(expectedResponseString, responseString);

    }

    @Test
    public void testAddTemporaryAdjustments() {
        ResourceScheduleRequest requestDTO = new ResourceScheduleRequest();
        Date date = new Date();
        requestDTO.setId(200);
        requestDTO.setStartDate(date);
        requestDTO.setEndDate(date);
        requestDTO.setOverride(true);
        requestDTO.setQuantity(5);
        String requestString = resourceResource.gson.toJson(requestDTO);

        BooleanRequest booleanResponse = new BooleanRequest();
        booleanResponse.setResult(true);
        when(mockResourceService.addTemporaryAdjustment(refEq(requestDTO), eq(mockUserInSession), eq(mockHostnameInRequest))).thenReturn(booleanResponse);

        String responseString = resourceResource.addTemporaryAdjustment(requestString);

        String expectedResponseString = resourceResource.gson.toJson(booleanResponse);
        assertEquals(expectedResponseString, responseString);
    }

    @Test
    public void testUpdateTemporaryAdjustments() {
        ResourceScheduleRequest requestDTO = new ResourceScheduleRequest();
        Date date = new Date();
        requestDTO.setId(100);
        requestDTO.setStartDate(date);
        requestDTO.setEndDate(date);
        requestDTO.setOverride(true);
        requestDTO.setQuantity(3);
        String requestString = resourceResource.gson.toJson(requestDTO);

        BooleanRequest booleanResponse = new BooleanRequest();
        booleanResponse.setResult(true);
        when(mockResourceService.updateTemporaryAdjustment(refEq(requestDTO), eq(mockUserInSession), eq(mockHostnameInRequest))).thenReturn(booleanResponse);

        String responseString = resourceResource.updateTemporaryAdjustment(requestString);

        String expectedResponseString = resourceResource.gson.toJson(booleanResponse);
        assertEquals(expectedResponseString, responseString);
    }

    @Test
    public void testDeleteTemporaryAdjustments() {
        int resourceScheduleId = 12;

        String context = "textPath";
        BooleanRequest booleanResponse = new BooleanRequest();
        booleanResponse.setResult(true);

        ResourceResource resourceSpy = spy(new ResourceResource(mockResourceService));

        Mockito.doReturn(mockUserInSession).when(resourceSpy).getUser();
        Mockito.doReturn(mockHostnameInRequest).when(resourceSpy).getRemoteHost();
        Mockito.doReturn(context).when(resourceSpy).getContextRealPath();

        when(mockResourceService.deleteTemporaryAdjustment(eq(resourceScheduleId), eq(mockUserInSession), eq(mockHostnameInRequest), eq(context))).thenReturn(booleanResponse);

        String responseString = resourceSpy.deleteTemporaryAdjustment(resourceScheduleId);

        String expectedResponseString = resourceResource.gson.toJson(booleanResponse);
        assertEquals(expectedResponseString, responseString);
    }

    @Test
    public void testAddDefaultAvailability()
    {
        ResourceScheduleRequest requestDTO = new ResourceScheduleRequest();
        Date date = new Date();
        requestDTO.setId(400);
        requestDTO.setStartDate(date);
        requestDTO.setEndDate(date);

        List<Integer> days = new ArrayList<>();
        days.add(1);
        days.add(4);
        requestDTO.setDays(days);
        requestDTO.setOverride(false);
        requestDTO.setQuantity(3);
        String requestString = resourceResource.gson.toJson(requestDTO);

        BooleanRequest booleanResponse = new BooleanRequest();
        booleanResponse.setResult(true);
        when(mockResourceService.addDefaultAvailability(refEq(requestDTO), eq(mockUserInSession), eq(mockHostnameInRequest))).thenReturn(booleanResponse);

        String responseString = resourceResource.addDefaultAvailability(requestString);

        String expectedResponseString = resourceResource.gson.toJson(booleanResponse);
        assertEquals(expectedResponseString, responseString);
    }

    @Test
    public void testUpdateDefaultAvailability()
    {
        ResourceResource resourceSpy = spy(new ResourceResource(mockResourceService));
        String context = "textPath";

        ResourceScheduleRequest requestDTO = new ResourceScheduleRequest();
        Date date = new Date();
        requestDTO.setId(400);
        requestDTO.setStartDate(date);
        requestDTO.setEndDate(date);

        List<Integer> days = new ArrayList<>();
        days.add(1);
        days.add(4);
        requestDTO.setDays(days);
        requestDTO.setOverride(false);
        requestDTO.setQuantity(3);
        String requestString = resourceResource.gson.toJson(requestDTO);

        Mockito.doReturn(mockUserInSession).when(resourceSpy).getUser();
        Mockito.doReturn(mockHostnameInRequest).when(resourceSpy).getRemoteHost();
        Mockito.doReturn(context).when(resourceSpy).getContextRealPath();

        BooleanRequest booleanResponse = new BooleanRequest();
        booleanResponse.setResult(true);
        when(mockResourceService.updateDefaultAvailability(refEq(requestDTO), eq(mockUserInSession), eq(mockHostnameInRequest), eq(context))).thenReturn(booleanResponse);

        String responseString = resourceSpy.updateDefaultAvailability(requestString);

        String expectedResponseString = resourceResource.gson.toJson(booleanResponse);
        assertEquals(expectedResponseString, responseString);
    }

    @Test
    public void testGetSelectedAnnotationsNQC() {

        String annotations = "55 99";
        String mode = null;
        int resourceId = 23;

        List<LineLevelAnnotations> annotationsList = setupLineLevelAnnotationsList();
        AnnotationsNamesQuantityCommentResponseDTO expectedResponse = AnnotationsNamesQuantityCommentResponseDTO.fromAnnotationsList(annotationsList);
        String expectedResponseString = resourceResource.gson.toJson(expectedResponse);

        when(mockResourceService.getSelectedResourceAnnotations(annotations, mode, resourceId)).thenReturn(annotationsList);

        String responseString = resourceResource.getSelectedAnnotationsNQC(annotations, mode, resourceId);

        assertEquals(expectedResponseString, responseString);

    }

}
