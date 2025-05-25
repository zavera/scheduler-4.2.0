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
package edu.harvard.catalyst.scheduler.service;

import com.google.common.collect.Lists;
import edu.harvard.catalyst.scheduler.core.Statics;
import edu.harvard.catalyst.scheduler.dto.*;
import edu.harvard.catalyst.scheduler.dto.request.*;
import edu.harvard.catalyst.scheduler.dto.response.*;
import edu.harvard.catalyst.scheduler.entity.*;
import edu.harvard.catalyst.scheduler.persistence.AppointmentDAO;
import edu.harvard.catalyst.scheduler.persistence.AuthDAO;
import edu.harvard.catalyst.scheduler.persistence.ResourceDAO;
import edu.harvard.catalyst.scheduler.persistence.TemplateResourceDAO;
import edu.harvard.catalyst.scheduler.util.MailHandler;
import edu.harvard.catalyst.scheduler.util.ServiceTestsDAOUtility;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;
import org.springframework.mail.SimpleMailMessage;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static edu.harvard.catalyst.scheduler.util.DateUtility.dayHrMinSecFormat;
import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;

/**
 * @author Ankit Panchamia
 */

@RunWith(MockitoJUnitRunner.class)
public final class ResourceServiceTest {

    @Mock private AuthDAO mockAuthDAO;
    @Mock private MailHandler mockMailHandler;
    @Mock private ResourceDAO mockResourceDAO;
    @Mock private AppointmentDAO mockAppointmentDAO;
    @Mock private AuditService mockAuditService;
    @Mock private ResourceService resourceService;

    @Mock private SublocationClosureIntervalDTO mockSublocationClosureIntervalDTO;
    @Mock private GetResourcesDTO mockResourcesDTO;
    @Mock private SearchDTO mockSearchDTO;
    @Mock private TemplateResourceDAO mockTemplateResourceDAO;


    @Before
    public void setUp() {
        resourceService = new ResourceService(mockAuthDAO, mockResourceDAO, mockAuditService, mockMailHandler, mockAppointmentDAO, mockTemplateResourceDAO);

    }

    @Test
    public void testConstructor() {
        final ResourceService testResourceService = new ResourceService();
        assertNotNull(testResourceService);
    }

    /**
     * FIXME-XH: I broke this test after updating the way that ResourceSublocation objects are created
     * when a new resource is created. I don't have time to fix it now, but we need to do that asap
     */

    public static final String RESOURCE_NAME_WITH_ALL_VALID_CHARACTERS = " abcdefghijklmnopqrstuvwxzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789,-()/ ";
    public static final String RESOURCE_NAME_WITH_ALL_VALID_CHARACTERS_AND_ONE_NONVALID_CHARACTER =
            RESOURCE_NAME_WITH_ALL_VALID_CHARACTERS + "!";

    @Test
    public void testCreateResource() {
        final int sublocationId = 321;
        final int newResourceId = 777;
        final User user = ServiceTestsDAOUtility.createMockUser();
        final ArgumentCaptor<ResourceSublocation> resourceCaptor = ArgumentCaptor.forClass(ResourceSublocation.class);
        final Sublocation sublocation = new Sublocation("Sublocation 3", new Institution("", ""));
        sublocation.setId(sublocationId);
        final CreateResourceRequestDTO request = new CreateResourceRequestDTO();
        request.setName(RESOURCE_NAME_WITH_ALL_VALID_CHARACTERS);
        request.setSublocationId(sublocationId);
        request.setResourceType("Nursing");

        // mock-i-stuff

        Mockito.when(mockResourceDAO.findSublocationById(sublocationId)).thenReturn(sublocation);
        // doAnswer is used to make a mocked method have side effects
        Mockito.doAnswer(new Answer<Void>() {
            public Void answer(InvocationOnMock invocation){
                Object[] args = invocation.getArguments();
                BaseEntity baseEntity = (BaseEntity) args[0];
                if (args[0] instanceof Resource) {
                    baseEntity.setId(newResourceId);
                }
                else {
                    baseEntity.setId(sublocationId);
                }
                return null;
            }
        }).when(mockResourceDAO).createEntity(Mockito.any(BaseEntity.class));

        // method under test

        final CreateResourceResponse response = this.resourceService.createResource(request, user, "");

        // verification
        Mockito.verify(mockResourceDAO, Mockito.times(2)).createEntity(resourceCaptor.capture());

        // intercept the method call argument! yummy
        final Resource newResource = resourceCaptor.getValue().getResource();
        final Sublocation newSublocation = resourceCaptor.getValue().getSublocation();

        // effect on newResource
        Assert.assertEquals(newResourceId, newResource.getId().intValue());
        Assert.assertEquals(request.getName().trim().concat(" - ").concat(sublocation.getName()), newResource.getName());
        Assert.assertEquals(request.getSublocationId(), newSublocation.getId().intValue());
        Assert.assertEquals(request.getResourceType(), newResource.getResourceType().getName());

        // effect on response
        Assert.assertTrue(response.isResult());
        Assert.assertEquals(request.getName().trim().concat(" - ").concat(sublocation.getName()), response.getName());
        Assert.assertEquals(request.getResourceType(), response.getResourceType());
        Assert.assertEquals(request.getSublocationId(), response.getSublocationId());
        Assert.assertEquals(sublocation.getName(), response.getSublocationName());
    }


    // In the following 6 "testCreateResource***" tests, we don't need to set up the mocking of everything that
    // happens in the method under test, because validation is performed near the
    // beginning of the method, and validation errors cause the method to return immediately

    @Test
    public void testCreateResourceNoName() {
        final User user = ServiceTestsDAOUtility.createMockUser();
        final CreateResourceRequestDTO createResourceRequestDTO = new CreateResourceRequestDTO();
        createResourceRequestDTO.setName("");
        createResourceRequestDTO.setSublocationId(321);
        createResourceRequestDTO.setResourceType("Nursing");

        CreateResourceResponse response = this.resourceService.createResource(createResourceRequestDTO, user, "");

        Mockito.verify(mockResourceDAO, Mockito.never()).createEntity(Mockito.any(Resource.class));

        assertFalse(response.isResult());
        assertEquals(response.getErrorMsg(), "Resource name needs to be specified");
        Assert.assertNull(response.getName());
        Assert.assertNull(response.getResourceType());
        Assert.assertNull(response.getSublocationName());
        Assert.assertNull(response.getSublocationName());
    }

    // Note: testCreateResource() verifies that the valid character set contains the expected characters
    // This test verifies that adding a character outside of this set triggers a validation error. We can't
    // possibly test all the invalid characters, but at least we can verify that the mechanis, for reporting
    // the invalid character works as expected
    @Test
    public void testCreateResourceBadName() {
        final User user = ServiceTestsDAOUtility.createMockUser();
        final CreateResourceRequestDTO createResourceRequestDTO = new CreateResourceRequestDTO();
        int sublocationId = 321;

        // validation: resource name contains only the followong characters:
        // alpha numeric characters, plus "," , "-", "(", ")", and "/".
        createResourceRequestDTO.setName(RESOURCE_NAME_WITH_ALL_VALID_CHARACTERS_AND_ONE_NONVALID_CHARACTER);
        createResourceRequestDTO.setSublocationId(sublocationId);
        createResourceRequestDTO.setResourceType("Nursing");

        CreateResourceResponse response = this.resourceService.createResource(createResourceRequestDTO, user, "");

        Mockito.verify(mockResourceDAO, Mockito.never()).createEntity(Mockito.any(Resource.class));

        assertFalse(response.isResult());
        assertEquals(response.getErrorMsg(), "Resource name must contain only the following characters: a-z A-Z 0-9 , - ( ) / & and whitespace, and cannot be empty");
        assertNull(response.getName());
        assertNull(response.getResourceType());
        assertNull(response.getSublocationName());
        assertNull(response.getSublocationName());
    }

    @Test
    public void testCreateResourceAlreadyExists() {
        final User user = ServiceTestsDAOUtility.createMockUser();
        final CreateResourceRequestDTO createResourceRequestDTO = new CreateResourceRequestDTO();
        createResourceRequestDTO.setName("A good name");
        createResourceRequestDTO.setSublocationId(321);
        createResourceRequestDTO.setResourceType("Nutrition");

        Mockito.when(mockResourceDAO.findResourceByName("A good name - A")).thenReturn(this.getDummyResource());
        Mockito.when(mockResourceDAO.findSublocationById(createResourceRequestDTO.getSublocationId())).thenReturn(new Sublocation("A", new Institution("", "")));

        CreateResourceResponse response = this.resourceService.createResource(createResourceRequestDTO, user, "");

        Mockito.verify(mockResourceDAO, Mockito.never()).createEntity(Mockito.any(Resource.class));

        assertFalse(response.isResult());
        assertEquals(response.getErrorMsg(), "This resource name is already existing");
        assertNull(response.getName());
        assertNull(response.getResourceType());
        assertNull(response.getSublocationName());
    }

    @Test
    public void testCreateResourceInvalidResourceType() {
        final User user = ServiceTestsDAOUtility.createMockUser();
        final CreateResourceRequestDTO createResourceRequestDTO = new CreateResourceRequestDTO();
        createResourceRequestDTO.setName("A good name");
        createResourceRequestDTO.setSublocationId(321);

        final CreateResourceResponse response = this.resourceService.createResource(createResourceRequestDTO, user, "");

        Mockito.verify(mockResourceDAO, Mockito.never()).createEntity(Mockito.any(Resource.class));

        assertFalse(response.isResult());
        assertEquals(response.getErrorMsg(), "Invalid resource type specified");
        assertNull(response.getName());
        assertNull(response.getResourceType());
        assertNull(response.getSublocationName());
    }

    @Test
    public void testCreateResourceBadSublocation() {
        final User user = ServiceTestsDAOUtility.createMockUser();
        final CreateResourceRequestDTO createResourceRequestDTO = new CreateResourceRequestDTO();
        createResourceRequestDTO.setName("A good name");
        createResourceRequestDTO.setSublocationId(321);
        createResourceRequestDTO.setResourceType("Nutrition");

        CreateResourceResponse response = this.resourceService.createResource(createResourceRequestDTO, user, "");

        Mockito.verify(mockResourceDAO, Mockito.never()).createEntity(Mockito.any(Resource.class));

        Assert.assertFalse(response.isResult());
        Assert.assertEquals(response.getErrorMsg(), "Invalid sublocation specified");
        Assert.assertNull(response.getName());
        Assert.assertNull(response.getResourceType());
        Assert.assertNull(response.getSublocationName());
        Assert.assertNull(response.getSublocationName());
    }

    @Test
    public void testCreateResourceAlreadyAssociatedWithSublocation() {
        final int sublocationId = 321;
        final int newResourceId = 777;
        final User user = ServiceTestsDAOUtility.createMockUser();
        final Sublocation sublocation = new Sublocation("Sublocation 3", new Institution("", ""));
        sublocation.setId(sublocationId);
        final CreateResourceRequestDTO request = new CreateResourceRequestDTO();
        request.setName(RESOURCE_NAME_WITH_ALL_VALID_CHARACTERS);
        request.setSublocationId(sublocationId);
        request.setResourceType("Nursing");

        Mockito.when(mockResourceDAO.findSublocationById(sublocationId)).thenReturn(sublocation);
        Mockito.when(mockResourceDAO.findResourceSublocation(newResourceId, sublocationId)).thenReturn(new ResourceSublocation());
        Mockito.doAnswer(new Answer<Void>() {
            public Void answer(InvocationOnMock invocation){
                Object[] args = invocation.getArguments();
                BaseEntity baseEntity = (BaseEntity) args[0];
                if (args[0] instanceof Resource) {
                    baseEntity.setId(777);
                }
                else {
                    baseEntity.setId(321);
                }
                return null;
            }
        }).when(mockResourceDAO).createEntity(Mockito.any(BaseEntity.class));

        // method under test

        final CreateResourceResponse response = this.resourceService.createResource(request, user, "");

        Mockito.verify(mockResourceDAO).createEntity(Mockito.any(BaseEntity.class));
        Mockito.verify(mockAuditService).logResourceActivity(Mockito.anyString(),
                Mockito.any(Resource.class),
                Mockito.any(User.class),
                Mockito.anyString(),
                Mockito.anyString(),
                Mockito.anyString());
        // effect on response
        Assert.assertFalse(response.isResult());
        Assert.assertEquals("There already is a sublocation associated with this resource", response.getErrorMsg());

    }

    @Test
    public void testGetResourceAnnotations() {
        final Resource resource = getDummyResource();
        final LineLevelAnnotations lineLevelAnnotations1 = new LineLevelAnnotations();
        lineLevelAnnotations1.setName("First");
        final LineLevelAnnotations lineLevelAnnotations2 = new LineLevelAnnotations();
        lineLevelAnnotations2.setName("Second");
        final List<LineLevelAnnotations> lineLevelAnnotationsList = new ArrayList<>();
        lineLevelAnnotationsList.add(lineLevelAnnotations1);
        lineLevelAnnotationsList.add(lineLevelAnnotations2);
        final ResourceAnnotation resourceAnnotation1 = new ResourceAnnotation();
        resourceAnnotation1.setLineLevelAnnotations(lineLevelAnnotations1);
        final ResourceAnnotation resourceAnnotation2 = new ResourceAnnotation();
        resourceAnnotation2.setLineLevelAnnotations(lineLevelAnnotations2);
        final List<ResourceAnnotation> resourceAnnotationList = new ArrayList<>();
        resourceAnnotationList.add(resourceAnnotation1);
        resourceAnnotationList.add(resourceAnnotation2);

        Mockito.when(mockResourceDAO.findResourceById(1)).thenReturn(resource);
        Mockito.when(mockResourceDAO.findResourcesAnnotationsByResource(resource)).thenReturn(resourceAnnotationList);

        final List<LineLevelAnnotations> result = resourceService.getResourceAnnotations(1);

        Mockito.verify(mockResourceDAO).findResourceById(1);
        Mockito.verify(mockResourceDAO).findResourcesAnnotationsByResource(resource);

        Collections.sort(lineLevelAnnotationsList, new LineLevelAnnotations.AnnotationsComparator());
        assertEquals(result.get(0), lineLevelAnnotationsList.get(0));
        assertEquals(result.get(1), lineLevelAnnotationsList.get(1));
    }

    @Test
    public void testGetResourceAnnotationsEmpty() {
        final Resource resource = getDummyResource();

        Mockito.when(mockResourceDAO.findResourceById(1)).thenReturn(resource);
        Mockito.when(mockResourceDAO.findResourcesAnnotationsByResource(resource)).thenReturn(new ArrayList<>());

        final List<LineLevelAnnotations> result = resourceService.getResourceAnnotations(1);

        Mockito.verify(mockResourceDAO).findResourceById(1);
        Mockito.verify(mockResourceDAO).findResourcesAnnotationsByResource(resource);

        assertTrue(result.isEmpty());
    }

    @Test
    public void testGetResourceAnnotationsNull() {
        final Resource resource = getDummyResource();

        Mockito.when(mockResourceDAO.findResourceById(1)).thenReturn(resource);
        Mockito.when(mockResourceDAO.findResourcesAnnotationsByResource(resource)).thenReturn(null);

        final List<LineLevelAnnotations> result = resourceService.getResourceAnnotations(1);

        Mockito.verify(mockResourceDAO).findResourceById(1);
        Mockito.verify(mockResourceDAO).findResourcesAnnotationsByResource(resource);

        assertTrue(result.isEmpty());
    }

    @Test
    public void testAssignResourceAlternatives() {
        final User user = ServiceTestsDAOUtility.createMockUser();
        final Resource resource = getDummyResource();
        final Resource dto1 = new Resource(2, "Lab 1", ResourceType.Lab, null, null, null, null, null, null);
        final Resource dto2 = new Resource(3, "Lab 2", ResourceType.Lab, null, null, null, null, null, null);
        final Resource dto3 = new Resource(4, "Lab 3", ResourceType.Lab, null, null, null, null, null, null);
        final List<Integer> list = new ArrayList<>();
        list.add(2);
        list.add(3);
        list.add(4);
        final AssignResourceAlternativesDTO assignResourceAlternativesDTO = new AssignResourceAlternativesDTO();
        assignResourceAlternativesDTO.setResourceId(1);
        assignResourceAlternativesDTO.setAlternatives(list);
        Mockito.when(mockResourceDAO.findResourceById(1)).thenReturn(resource);
        Mockito.when(mockResourceDAO.findResourceById(2)).thenReturn(dto1);
        Mockito.when(mockResourceDAO.findResourceById(3)).thenReturn(dto2);
        Mockito.when(mockResourceDAO.findResourceById(4)).thenReturn(dto3);

        final BooleanResultDTO resultDTO = resourceService.addResourceAlternatives(assignResourceAlternativesDTO, user, "");

        Mockito.verify(mockResourceDAO).findResourceById(1);
        Mockito.verify(mockResourceDAO).findResourceById(2);
        Mockito.verify(mockResourceDAO).findResourceById(3);
        Mockito.verify(mockResourceDAO).findResourceById(4);
        Mockito.verify(mockResourceDAO, Mockito.times(3)).createEntity(Mockito.any(ResourceAlternate.class));
        Mockito.verify(mockAuditService).logResourceActivity("", resource, user, Statics.AUDIT_RESOURCE_ALTERNATE_CREATE, null, null);

        assertNotNull(resultDTO);
        assertTrue(resultDTO.isResult());
    }

    @Test
    public void testCreateSublocationClosureInterval() {
        final User user = ServiceTestsDAOUtility.createMockUser();
        final SublocationClosureIntervalDTO interval = new SublocationClosureIntervalDTO();
        interval.setSublocationId(1);
        final Sublocation sublocation = new Sublocation("", new Institution("", ""));
        sublocation.setId(1);

        final Date startDate = new Date();
        final Date endDate = new Date();
        interval.setStartTime(startDate);
        interval.setEndTime(endDate);
        interval.setReason("Christmas holiday");
        interval.setSublocationId(1);
        Mockito.when(mockResourceDAO.findSublocationById(1)).thenReturn(sublocation);
        final SublocationClosureInterval result = resourceService.createSublocationClosureInterval(interval, ServiceTestsDAOUtility.createMockUser(), "8080");

        Mockito.verify(mockResourceDAO).findSublocationById(1);
        Mockito.verify(mockResourceDAO).createEntity(result);
        Mockito.verify(mockAuditService).logResourceSublocationClosureActivity("8080", sublocation, user,
                Statics.AUDIT_RESOURCE_SUBLOCATION_CLOSURE_CREATE, null, null);

        assertNotNull(result);
        assertEquals(result.getStartTime(), startDate);
        assertEquals(result.getEndTime(), endDate);
        assertEquals(result.getReason(), interval.getReason());
        assertEquals(result.getSublocation(), sublocation);
    }

    @Test
    public void testGetSublocationClosureIntervals() {
        final List<SublocationClosureIntervalResponse> sublocationClosureIntervalDTO = new ArrayList<>();
        final SublocationClosureIntervalResponse dto1 = new SublocationClosureIntervalResponse(1, new Date(), new Date(),
                "holiday 1", "", 2L);
        final SublocationClosureIntervalResponse dto2 = new SublocationClosureIntervalResponse(2, new Date(), new Date(),
                "holiday 2", "", 2L);
        sublocationClosureIntervalDTO.add(dto1);
        sublocationClosureIntervalDTO.add(dto2);

        Mockito.when(mockResourceDAO.getSublocationClosureInterval("", "", 1, 1)).thenReturn(sublocationClosureIntervalDTO);
        assertNotNull(sublocationClosureIntervalDTO);

        final List<SublocationClosureIntervalResponse> sublocationClosureIntervals = resourceService.getSublocationClosureIntervals("","",1,1);

        Mockito.verify(mockResourceDAO).getSublocationClosureInterval("", "", 1, 1);
        assertNotNull(sublocationClosureIntervals);
        assertTrue(sublocationClosureIntervals == sublocationClosureIntervalDTO);
        assertEquals(dto1, sublocationClosureIntervals.get(0));
        assertEquals(dto2, sublocationClosureIntervals.get(1));
    }

    @Test
    public void testGetAllResourceNames() {
        final User user = ServiceTestsDAOUtility.createMockUser();
        final String ipAddress = "192.168.1.1";
        final List<String> resourceNames = new ArrayList<>();
        resourceNames.add("A Resource Name");

        Mockito.when(mockResourceDAO.getAllResourceNames()).thenReturn(resourceNames);

        final List<String> allResourceNames = resourceService.getAllResourceNames(user, ipAddress);

        Mockito.verify(mockAuditService).logViewActivity(ipAddress, user, "All Resource Names Viewed");
        assertNotNull(allResourceNames);
        assertEquals(allResourceNames.size(), resourceNames.size());
        assertEquals(allResourceNames.get(0), resourceNames.get(0));

    }

    @Test
    public void testGetResources() {
        final User user = ServiceTestsDAOUtility.createMockUser();
        final List<Resource> resources = new ArrayList<>();
        final Resource dto1 = new Resource(1, "Nurse 1", ResourceType.Nursing, null, null, null, null, null, null);
        final Resource dto2 = new Resource(2, "Room 1", ResourceType.Room, null, null, null, null, null, null);
        resources.add(dto1);
        resources.add(dto2);

        mockResourcesDTO.setResources(resources);
        Mockito.when(mockResourceDAO.findResourcesActiveInSublocations()).thenReturn(resources);
        assertNotNull(resources);

        final List<Resource> resourceList = resourceService.getResourcesActiveInSublocations(true, "8080", user);

        Mockito.verify(mockAuditService).logViewActivity("8080", user, "All Resources Viewed");

        assertNotNull(resourceList);
        assertTrue(resources != resourceList);
        assertEquals(resourceList.get(0).getName(), "Nurse 1");
        assertEquals(resourceList.get(0).getId(), Integer.valueOf(1));
        assertEquals(resourceList.get(0).getResourceType(), ResourceType.Nursing);
        assertNull(resourceList.get(0).getSharedResource());
        assertEquals(resourceList.get(1).getName(), "Room : Room 1");
        assertEquals(resourceList.get(1).getId(), Integer.valueOf(2));
        assertEquals(resourceList.get(1).getResourceType(), ResourceType.Room);
        assertNull(resourceList.get(1).getSharedResource());
    }

    @Test
    public void testGetResourcesFalse() {
        final User user = ServiceTestsDAOUtility.createMockUser();
        final List<Resource> resources = new ArrayList<>();
        final Resource dto1 = new Resource(1, "Nurse 1", ResourceType.Nursing, null, null, null, null, null, null);
        final Resource dto2 = new Resource(2, "Room 1", ResourceType.Room, null, null, null, null, null, null);
        resources.add(dto1);
        resources.add(dto2);

        mockResourcesDTO.setResources(resources);
        Mockito.when(mockResourceDAO.findResourcesActiveInSublocations()).thenReturn(resources);
        assertNotNull(resources);

        final List<Resource> resourceList = resourceService.getResourcesActiveInSublocations(false, "8080", user);

        Mockito.verify(mockAuditService).logViewActivity("8080", user, "All Resources Viewed");

        assertNotNull(resourceList);
        assertTrue(resources != resourceList);
        assertEquals(resourceList.get(0).getName(), "Nurse 1");
        assertEquals(resourceList.get(0).getId(), Integer.valueOf(1));
        assertEquals(resourceList.get(0).getResourceType(), ResourceType.Nursing);
        assertNull(resourceList.get(0).getSharedResource());
        assertEquals(resourceList.get(1).getName(), "Room 1");
        assertEquals(resourceList.get(1).getId(), Integer.valueOf(2));
        assertEquals(resourceList.get(1).getResourceType(), ResourceType.Room);
        assertNull(resourceList.get(1).getSharedResource());
    }

    @Test
    public void testGetRoomResources() {
        final List<Resource> resources = new ArrayList<>();
        final Resource dto1 = new Resource(1, "Room 1", ResourceType.Room, null, null, null, null, null, null);
        final Resource dto2 = new Resource(2, "This is Any Private Room 2", ResourceType.Room, null, null, null, null, null, null);
        final Resource dto3 = new Resource(2, "This is Feldberg Room 3", ResourceType.Room, null, null, null, null, null, null);
        final Resource dto4 = new Resource(2, "This is All Rooms 4", ResourceType.Room, null, null, null, null, null, null);

        resources.add(dto1);
        resources.add(dto2);
        resources.add(dto3);
        resources.add(dto4);
        mockResourcesDTO.setRoomResources(resources);
        Mockito.when(mockResourceDAO.findRoomResourcesListedInResourceSublocation("Sublocation")).thenReturn(resources);

        final List<Resource> results = resourceService.getRoomResources("Sublocation");

        assertNotNull(results);
        assertEquals(results.size(), 1);
        assertEquals(results.get(0).getName(), "Room 1");
        assertEquals(results.get(0).getResourceType(), ResourceType.Room);
    }

    @Test
    public void testGetRoomResourcesNull() {
        final List<Resource> resources = new ArrayList<>();
        final Resource dto1 = new Resource(1, "Room 1", ResourceType.Room, null, null, null, null, null, null);
        final Resource dto2 = new Resource(2, "This is Any Private Room 2", ResourceType.Room, null, null, null, null, null, null);
        resources.add(dto1);
        resources.add(dto2);
        mockResourcesDTO.setRoomResources(resources);
        Mockito.when(mockResourceDAO.findRoomResourcesListedInResourceSublocation("Sublocation")).thenReturn(resources);

        final List<Resource> results = resourceService.getRoomResources("Sublocation");

        assertNotNull(results);
        assertEquals(results.size(), 1);
        assertEquals(results.get(0).getName(), "Room 1");
        assertEquals(results.get(0).getResourceType(), ResourceType.Room);
    }

    @Test
    public void testGetNursingResources() {
        final List<Resource> resources = new ArrayList<Resource>();
        final Resource dto1 = new Resource(1, "Nurse 1", ResourceType.Nursing, null, null, null, null, null, null);
        final Resource dto2 = new Resource(2, "Nurse 2", ResourceType.Nursing, null, null, null, null, null, null);
        resources.add(dto1);
        resources.add(dto2);

        mockResourcesDTO.setNursingResources(resources);
        Mockito.when(mockResourceDAO.findNursingResourcesListedInResourceSublocation("Sublocation")).thenReturn(resources);
        assertNotNull(resources);

        final List<Resource> resourceList = resourceService.getNursingResources("8080", ServiceTestsDAOUtility.createMockUser(), "Sublocation");

        Mockito.verify(mockResourceDAO).findNursingResourcesListedInResourceSublocation("Sublocation");

        assertNotNull(resourceList);
        assertEquals(resourceList.get(0).getId(), resources.get(0).getId());
        assertEquals(resourceList.get(1).getId(), resources.get(1).getId());
    }

    @Test
    public void testGetNutritionResources() {
        final List<Resource> resources = new ArrayList<>();
        final Resource dto1 = new Resource(1, "Nutrition 1", ResourceType.Nutrition, null, null, null, null, null, null);
        final Resource dto2 = new Resource(2, "Nutrition 2", ResourceType.Nutrition, null, null, null, null, null, null);
        resources.add(dto1);
        resources.add(dto2);

        mockResourcesDTO.setNutritionResources(resources);
        Mockito.when(mockResourceDAO.findNutritionResourcesListedInResourceSublocation("Sublocation")).thenReturn(resources);
        assertNotNull(resources);

        final List<Resource> resourceList = resourceService.getNutritionResources("8080",
                ServiceTestsDAOUtility.createMockUser(), "Sublocation");

        Mockito.verify(mockResourceDAO).findNutritionResourcesListedInResourceSublocation("Sublocation");
        assertNotNull(resourceList);
        assertEquals(resourceList, resources);
    }

    @Test
    public void testGetAlternateResources() {
        final Resource resource = new Resource(1, "Nurse", ResourceType.Nursing, null, null, null, null, null, null);
        final List<Resource> resources = new ArrayList<>();
        final Resource dto1 = new Resource(1, "Nurse-Scanner 1", ResourceType.Nursing, null, null, null, null, null, null);
        final Resource dto2 = new Resource(2, "Nurse-Scanner 2", ResourceType.Nursing, null, null, null, null, null, null);
        final Resource dto3 = new Resource(3, "Nurse-Scanner 3", ResourceType.Nursing, null, null, null, null, null, null);
        resources.add(dto1);
        resources.add(dto2);
        resources.add(dto3);
        final List<ResourceAlternate> resourceAlternatesList = new ArrayList<>();
        final ResourceAlternate altResources1 = new ResourceAlternate(dto1, resource);
        final ResourceAlternate altResources2 = new ResourceAlternate(dto2, resource);
        final ResourceAlternate altResources3 = new ResourceAlternate(dto3, resource);
        resourceAlternatesList.add(altResources1);
        resourceAlternatesList.add(altResources2);
        resourceAlternatesList.add(altResources3);
        final List<ResourcesResponse> responses = new ArrayList<>();

        Mockito.when(mockResourceDAO.findResourceById(1)).thenReturn(resource);
        Mockito.when(mockResourceDAO.findResourceAlternates(resource)).thenReturn(resourceAlternatesList);
        Mockito.when(mockResourceDAO.findResourcesWithSublocationAndAlternates(resource, resources, "", "", 1, 3)).thenReturn(responses);
        assertNotNull(resources);

        final List<ResourcesResponse> resourceList = resourceService.getAlternateResources(1, "", "", 1, 3);

        Mockito.verify(mockResourceDAO).findResourcesWithSublocationAndAlternates(resource, resources, "", "", 1, 3);
        Mockito.verify(mockResourceDAO).findResourceById(1);
        Mockito.verify(mockResourceDAO).findResourceAlternates(resource);

        assertNotNull(resourceList);
        assertEquals(resourceList, responses);
    }

    @Test
    public void testGetResourcesGetResourcesAvailableForGenderBlockRestriction() {
        final List<Resource> resources = new ArrayList<>();
        final int resourceId = 123;
        resources.add(this.getDummyResource());
        Mockito.when(mockResourceDAO.getResourcesAvailableForGenderBlockRestriction(resourceId)).thenReturn(resources);

        final List<Resource> availableGenderBlockedResources = resourceService.getResourcesAvailableForGenderBlockRestriction(resourceId);

        Mockito.verify(mockResourceDAO).getResourcesAvailableForGenderBlockRestriction(resourceId);
        assertNotNull(availableGenderBlockedResources);
        assertEquals(availableGenderBlockedResources.get(0).getId(), resources.get(0).getId());
        assertEquals(availableGenderBlockedResources.get(0).getName(), resources.get(0).getName());
    }

    @Test
    public void testGetResourceSchedules() {
        final Resource resource = getDummyResource();
        final List<ResourceScheduleResponse> list = new ArrayList<>();

        Mockito.when(mockResourceDAO.findResourceById(1)).thenReturn(resource);
        Mockito.when(mockResourceDAO.findResourceSchedulesByResource(resource, false, "id", "Name", 1, 3)).thenReturn(list);

        final List<ResourceScheduleResponse> result1 = resourceService.getResourceSchedules(1, false, "id", "Name", 1, 3);
        assertNotNull(result1);
        Mockito.verify(mockResourceDAO).findResourceById(1);
        Mockito.verify(mockResourceDAO).findResourceSchedulesByResource(resource, false, "id", "Name", 1, 3);
        assertEquals(result1, list);
    }

    @Test
    public void testGetResourceSchedulesOverride() {
        final Resource resource = getDummyResource();
        final List<ResourceScheduleResponse> list = new ArrayList<>();

        Mockito.when(mockResourceDAO.findResourceById(1)).thenReturn(resource);
        Mockito.when(mockResourceDAO.findTemporarySchedulesByResource(resource, true, "id", "Name", 1, 3)).thenReturn(list);

        final List<ResourceScheduleResponse> result1 = resourceService.getResourceSchedules(1, true, "id", "Name", 1, 3);
        assertNotNull(result1);
        Mockito.verify(mockResourceDAO).findResourceById(1);
        Mockito.verify(mockResourceDAO).findTemporarySchedulesByResource(resource, true, "id", "Name", 1, 3);
        assertEquals(result1, list);
    }

    @Test
    public void testGetResourceDefaultSchedule() {
        final ResourceScheduleResponse response = new ResourceScheduleResponse(1, 1, "", "", 1, 1L);
        Mockito.when(mockResourceDAO.resourceScheduleById(10)).thenReturn(response);

        final ResourceScheduleResponse result = resourceService.getResourceDefaultSchedule(10);

        Mockito.verify(mockResourceDAO).resourceScheduleById(10);
        assertNotNull(result);
        assertEquals(result, response);
    }

    @Test
    public void testAddDefaultAvailability() {
        final ResourceScheduleRequest resourceRequest = new ResourceScheduleRequest();
        final List<Integer> days = new ArrayList<>();
        days.add(1);
        days.add(2);
        resourceRequest.setResourceId(1);
        resourceRequest.setDays(days);
        final User user = ServiceTestsDAOUtility.createMockUser();
        final String ipAddress = "ip address";
        final Resource resource = getDummyResource();

        Mockito.when(mockResourceDAO.findResourceById(1)).thenReturn(resource);

        final BooleanRequest result = resourceService.addDefaultAvailability(resourceRequest, user, ipAddress);

        Mockito.verify(mockResourceDAO).findResourceById(1);
        Mockito.verify(mockResourceDAO, Mockito.times(2)).createEntity(Mockito.any(ResourceSchedule.class));
        Mockito.verify(mockAuditService).logResourceActivity(ipAddress, resource, user,
                Statics.AUDIT_RESOURCE_DEFAULT_CREATE, null, null);
        assertTrue(result.isResult());
    }

    @Test
    public void testUpdateDefaultAvailability() {
        final Resource resource = getDummyResource();
        final ResourceSchedule resourceSchedule = new ResourceSchedule(resource, new Date(), new Date(), 1, 1, true);
        resourceSchedule.setId(10);
        final ResourceScheduleRequest resourceScheduleRequest = new ResourceScheduleRequest();
        resourceScheduleRequest.setDays(Lists.newArrayList(1));

        final User user = ServiceTestsDAOUtility.createMockUser();
        user.setInstitution(new Institution("short", "long"));
        final List<User> userList = new ArrayList<>();
        final String ipAddress = "23456";
        final StringBuilder url = new StringBuilder(Paths.get(".").toAbsolutePath().normalize().toString());
        final int index = url.lastIndexOf("/") + 1;
        url.delete(index, url.length());
        url.append("web/src/main/webapp/WEB-INF");
        final String previousData = " Start Time: " + dayHrMinSecFormat(resourceSchedule.getStartTime()) + " to " +
                dayHrMinSecFormat(resourceScheduleRequest.getStartDate()) + ", " + " End Time: " +
                dayHrMinSecFormat(resourceSchedule.getEndTime()) + " to " + dayHrMinSecFormat(resourceScheduleRequest.getEndDate()) + ", " + " Quantity: " +
                resourceSchedule.getQuantity() + " to " + resourceScheduleRequest.getQuantity() + ", ";

        Mockito.when(mockResourceDAO.findResourceScheduleById(resourceScheduleRequest.getId())).thenReturn(resourceSchedule);
        Mockito.when(mockAuthDAO.findSchedulerUserByInstitutionRole()).thenReturn(userList);

        final BooleanRequest result = resourceService.updateDefaultAvailability(resourceScheduleRequest, user, ipAddress, url.toString());

        Mockito.verify(mockAuditService).logResourceActivity(ipAddress, resource, user, Statics.AUDIT_RESOURCE_DEFAULT_UPDATE, previousData, null);
        Mockito.verify(mockResourceDAO).updateEntity(resourceSchedule);
        Mockito.verify(mockAuthDAO).findSchedulerUserByInstitutionRole();
        assertTrue(result.isResult());
    }

    @Test
    public void testUpdateResourceAvailabilityData() {
        final Date firstStartTime = new Date(0);
        final Date firstEndTime = new Date(0);
        final Date secondStartTime = new Date();
        final Date secondEndTime = new Date();
        final ResourceScheduleRequest resourceScheduleRequest = new ResourceScheduleRequest();
        resourceScheduleRequest.setStartDate(secondStartTime);
        resourceScheduleRequest.setEndDate(secondEndTime);
        resourceScheduleRequest.setQuantity(16);
        resourceScheduleRequest.setOverride(true);
        resourceScheduleRequest.setId(10);
        resourceScheduleRequest.setDays(Lists.newArrayList(1));
        final Resource resource = getDummyResource();
        final ResourceSchedule resourceSchedule = new ResourceSchedule(resource, firstStartTime, firstEndTime, null, 1, true);
        resourceSchedule.setId(10);
        final String previousData =
                " Start Time: " + dayHrMinSecFormat(resourceSchedule.getStartTime()) + " to " + dayHrMinSecFormat(resourceScheduleRequest.getStartDate()) + ", " +
                " End Time: " + dayHrMinSecFormat(resourceSchedule.getEndTime()) + " to " + dayHrMinSecFormat(resourceScheduleRequest.getEndDate()) + ", " +
                " Quantity: " + resourceSchedule.getQuantity() + " to " + resourceScheduleRequest.getQuantity() + ", " +
                " Day Of Week: null to 1, ";

      final String result1 = resourceService.deltaOfResourceAvailability(resourceScheduleRequest, resourceSchedule);
        assertTrue(resourceSchedule.getStartTime() == secondStartTime);
        assertTrue(resourceSchedule.getEndTime() == secondEndTime);
        assertEquals(resourceSchedule.getQuantity(), Integer.valueOf(16));
        assertEquals(result1, previousData);

        final String result2 = resourceService.deltaOfResourceAvailability(resourceScheduleRequest, resourceSchedule);
        assertTrue(resourceSchedule.getStartTime() == secondStartTime);
        assertTrue(resourceSchedule.getEndTime() == secondEndTime);
        assertEquals(resourceSchedule.getQuantity(), Integer.valueOf(16));
        assertEquals(result2, "");
    }

    @Test
    public void testSendDefaultAvailabilityChangeEmail() {
        final User user = ServiceTestsDAOUtility.createMockUser();
        final List<User> userList = new ArrayList<>();
        userList.add(user);
        final Resource resource = getDummyResource();
        final ResourceSchedule resourceSchedule = new ResourceSchedule(resource, new Date(), new Date(), 6, 1, true);
        final String start = resourceSchedule.getStartTime().getHours() + " : " + resourceSchedule.getStartTime().getMinutes();
        final String end = resourceSchedule.getEndTime().getHours() + " : " + resourceSchedule.getEndTime().getMinutes();
        ArgumentCaptor<SimpleMailMessage> mail = ArgumentCaptor.forClass(SimpleMailMessage.class);

        Mockito.when(mockAuthDAO.findSchedulerUserByInstitutionRole()).thenReturn(userList);

        // Get the absolute path to the project then appending to the directory I wish to get to.
        final StringBuilder url = new StringBuilder(Paths.get(".").toAbsolutePath().normalize().toString());
        final int index = url.lastIndexOf("/") + 1;
        url.delete(index, url.length());
        url.append("web/src/main/webapp/WEB-INF");

        resourceService.sendDefaultAvailabilityChangeEmail(resourceSchedule, 6, "this is editStart", "this is editEnd", "31", "This is an institution", url.toString());

        Mockito.verify(mockAuthDAO).findSchedulerUserByInstitutionRole();
        Mockito.verify(mockMailHandler).sendOptionalEmails(mail.capture());
        assertTrue(mail.getValue().getSubject().equals("Change in the default availability of the resource."));
        assertTrue(mail.getValue().getText().contains("This is an institution"));
        assertTrue(mail.getValue().getText().contains("this is editStart"));
        assertTrue(mail.getValue().getText().contains("this is editEnd"));
        assertTrue(mail.getValue().getText().contains(start));
        assertTrue(mail.getValue().getText().contains(end));
        assertTrue(mail.getValue().getText().contains(Integer.toString(6)));
        assertTrue(mail.getValue().getText().contains(resourceService.returnDayofWeek(resourceSchedule.getDayOfWeek())));
        assertTrue(mail.getValue().getText().contains(resourceSchedule.getQuantity().toString()));
        assertEquals(user.getEmail(), mail.getValue().getTo()[0]);
    }

    @Test
    public void testSendDefaultAvailabilityChangeEmailNull() {
        final User user = ServiceTestsDAOUtility.createMockUser();
        user.setEmail(null);
        final List<User> userList = new ArrayList<>();
        userList.add(user);
        final Resource resource = getDummyResource();
        final ResourceSchedule resourceSchedule = new ResourceSchedule(resource, new Date(), new Date(), 6, 1, true);

        Mockito.when(mockAuthDAO.findSchedulerUserByInstitutionRole()).thenReturn(userList);

        // Get the absolute path to the project then appending to the directory I wish to get to.
        final StringBuilder url = new StringBuilder(Paths.get(".").toAbsolutePath().normalize().toString());
        final int index = url.lastIndexOf("/") + 1;
        url.delete(index, url.length());
        url.append("web/src/main/webapp/WEB-INF");

        resourceService.sendDefaultAvailabilityChangeEmail(resourceSchedule, 6, "this is editStart", "this is editEnd", "31", "This is an institution", url.toString());

        Mockito.verify(mockAuthDAO).findSchedulerUserByInstitutionRole();
        Mockito.verify(mockMailHandler, Mockito.never()).sendOptionalEmails(Mockito.any(SimpleMailMessage.class));
    }

    @Test
    public void testSendDefaultAvailabilityChangeEmailEmpty() {
        final List<User> userList = new ArrayList<>();
        final Resource resource = getDummyResource();
        final ResourceSchedule resourceSchedule = new ResourceSchedule(resource, new Date(), new Date(), 6, 1, true);

        Mockito.when(mockAuthDAO.findSchedulerUserByInstitutionRole()).thenReturn(userList);

        resourceService.sendDefaultAvailabilityChangeEmail(resourceSchedule, 6, "", "", "", "", "");

        Mockito.verify(mockAuthDAO).findSchedulerUserByInstitutionRole();
        Mockito.verify(mockMailHandler, Mockito.never()).sendOptionalEmails(Mockito.any(SimpleMailMessage.class));
    }

    @Test
    public void testReturnDayofWeek() {
        // sunny cases
        //////////////////
        final String expectedDay1 = resourceService.returnDayofWeek(1);
        assertNotNull(expectedDay1);
        assertEquals(expectedDay1, "SUN");

        final String expectedDay2 = resourceService.returnDayofWeek(2);
        assertNotNull(expectedDay2);
        assertEquals(expectedDay2, "MON");

        final String expectedDay3 = resourceService.returnDayofWeek(3);
        assertNotNull(expectedDay3);
        assertEquals(expectedDay3, "TUE");

        final String expectedDay4 = resourceService.returnDayofWeek(4);
        assertNotNull(expectedDay4);
        assertEquals(expectedDay4, "WED");

        final String expectedDay5 = resourceService.returnDayofWeek(5);
        assertNotNull(expectedDay5);
        assertEquals(expectedDay5, "THU");

        final String expectedDay6 = resourceService.returnDayofWeek(6);
        assertNotNull(expectedDay6);
        assertEquals(expectedDay6, "FRI");

        final String expectedDay7 = resourceService.returnDayofWeek(7);
        assertNotNull(expectedDay7);
        assertEquals(expectedDay7, "SAT");

        // rainy cases
        ////////////////////
        final String badDay0 = resourceService.returnDayofWeek(0);
        assertEquals(badDay0, null);

        final String badDay8 = resourceService.returnDayofWeek(8);
        assertEquals(badDay8, null);
    }

    @Test
    public void testAddTemporaryAdjustment() {
        final ResourceScheduleRequest resourceScheduleRequest = new ResourceScheduleRequest();
        resourceScheduleRequest.setStartDate(new Date());
        resourceScheduleRequest.setEndDate(new Date());
        resourceScheduleRequest.setQuantity(16);
        resourceScheduleRequest.setOverride(true);
        resourceScheduleRequest.setResourceId(10);
        final User user = ServiceTestsDAOUtility.createMockUser();
        final String ipAddress = "23.23.23.23";
        final Resource resource = getDummyResource();

        Mockito.when(mockResourceDAO.findResourceById(10)).thenReturn(resource);

        final BooleanRequest result = resourceService.addTemporaryAdjustment(resourceScheduleRequest, user, ipAddress);

        Mockito.verify(mockResourceDAO).findResourceById(10);
        Mockito.verify(mockResourceDAO).createEntity(Mockito.any(ResourceSchedule.class));
        Mockito.verify(mockAuditService).logResourceActivity(ipAddress, resource, user,
                Statics.AUDIT_RESOURCE_TEMPADJ_CREATE, null, null);
        assertTrue(result.isResult());
    }

    @Test
    public void testUpdateTemporaryAdjustment() {
        final ResourceScheduleRequest resourceScheduleRequest = new ResourceScheduleRequest();
        resourceScheduleRequest.setStartDate(new Date(0));
        resourceScheduleRequest.setEndDate(new Date(0));
        resourceScheduleRequest.setQuantity(16);
        resourceScheduleRequest.setOverride(true);
        resourceScheduleRequest.setId(10);
        resourceScheduleRequest.setDays(Lists.newArrayList(1));

      final User user = ServiceTestsDAOUtility.createMockUser();
        final String ipAddress = "23.23.23.23";
        final Resource resource = getDummyResource();
        final ResourceSchedule resourceSchedule = new ResourceSchedule(resource, new Date(), new Date(), null, 1, true);
        resourceSchedule.setId(10);
        final String previousData = " Start Time: " + dayHrMinSecFormat(resourceSchedule.getStartTime()) + " to " +
                dayHrMinSecFormat(resourceScheduleRequest.getStartDate()) + ", " +
              " End Time: " +
                dayHrMinSecFormat(resourceSchedule.getEndTime()) + " to " + dayHrMinSecFormat(resourceScheduleRequest.getEndDate()) + ", " +
              " Quantity: " +
              resourceSchedule.getQuantity() + " to " + resourceScheduleRequest.getQuantity() + ", " +
              " Day Of Week: null to 1, ";

        Mockito.when(mockResourceDAO.findResourceScheduleById(10)).thenReturn(resourceSchedule);

        final BooleanRequest result = resourceService.updateTemporaryAdjustment(resourceScheduleRequest, user, ipAddress);

        Mockito.verify(mockResourceDAO).findResourceScheduleById(10);
        Mockito.verify(mockResourceDAO).updateEntity(resourceSchedule);
        Mockito.verify(mockAuditService).logResourceActivity(ipAddress, resourceSchedule.getResource(), user,
                Statics.AUDIT_RESOURCE_TEMPADJ_UPDATE, previousData, null);
        assertTrue(result.isResult());
    }

    @Test
    public void testDeleteTemporaryAdjustment() {
        final List<User> userList = new ArrayList<>();
        final User user = ServiceTestsDAOUtility.createMockUser();
        user.setInstitution(new Institution("", ""));
        userList.add(user);
        final String ipAddress = "23.23.23.23";
        final Resource resource = getDummyResource();
        final ResourceSchedule resourceSchedule = new ResourceSchedule(resource, new Date(), new Date(), null, 1, true);
        resourceSchedule.setId(10);

        final String editStart = resourceSchedule.getStartTime().toString();
        final String editEnd = resourceSchedule.getEndTime().toString();
        // Get the absolute path to the project then appending to the directory I wish to get to.
        final StringBuilder url = new StringBuilder(Paths.get(".").toAbsolutePath().normalize().toString());
        final int index = url.lastIndexOf("/") + 1;
        url.delete(index, url.length());
        url.append("web/src/main/webapp/WEB-INF");

        Mockito.when(mockResourceDAO.findResourceScheduleById(10)).thenReturn(resourceSchedule);
        Mockito.when(mockAuthDAO.findSchedulerUserByInstitutionRole()).thenReturn(userList);
        final BooleanRequest booleanRequest = resourceService.deleteTemporaryAdjustment(10, user, ipAddress ,url.toString());

        Mockito.verify(mockAuditService).logResourceActivity(ipAddress, resourceSchedule.getResource(), user,
                Statics.AUDIT_RESOURCE_TEMPADJ_DELETE, null, null);
        Mockito.verify(mockAuthDAO).findSchedulerUserByInstitutionRole();
        Mockito.verify(mockMailHandler).sendOptionalEmails(Mockito.any(SimpleMailMessage.class));
        assertTrue(booleanRequest.isResult());
    }

    @Test
    public void testSendRemoveOverrideScheduleMail() {
        final User user = ServiceTestsDAOUtility.createMockUser();
        final List<User> userList = new ArrayList<>();
        userList.add(user);
        final Resource resource = getDummyResource();
        final ResourceSchedule resourceSchedule = new ResourceSchedule(resource, new Date(), new Date(), null, 1, true);
        final String start = resourceSchedule.getStartTime().toString();
        final String end = resourceSchedule.getEndTime().toString();
        ArgumentCaptor<SimpleMailMessage> mail = ArgumentCaptor.forClass(SimpleMailMessage.class);

        Mockito.when(mockAuthDAO.findSchedulerUserByInstitutionRole()).thenReturn(userList);

        // Get the absolute path to the project then appending to the directory I wish to get to.
        final StringBuilder url = new StringBuilder(Paths.get(".").toAbsolutePath().normalize().toString());
        final int index = url.lastIndexOf("/") + 1;
        url.delete(index, url.length());
        url.append("web/src/main/webapp/WEB-INF");

        resourceService.sendRemoveOverrideScheduleMail(resourceSchedule, "this is editStart", "this is editEnd", "This is an institution", url.toString());

        Mockito.verify(mockAuthDAO).findSchedulerUserByInstitutionRole();
        Mockito.verify(mockMailHandler).sendOptionalEmails(mail.capture());
        assertTrue(mail.getValue().getSubject().equals("Change in the temporary adjustments of the resource."));
        assertTrue(mail.getValue().getText().contains("This is an institution"));
        assertTrue(mail.getValue().getText().contains("this is editStart"));
        assertTrue(mail.getValue().getText().contains("this is editEnd"));
        assertTrue(mail.getValue().getText().contains(start));
        assertTrue(mail.getValue().getText().contains(end));
        assertTrue(mail.getValue().getText().contains(resourceSchedule.getQuantity().toString()));
        assertEquals(user.getEmail(), mail.getValue().getTo()[0]);
    }

    @Test
    public void testSendRemoveOverrideScheduleMailIsEmpty() {
        final List<User> userList = new ArrayList<>();
        final Resource resource = getDummyResource();
        final ResourceSchedule resourceSchedule = new ResourceSchedule(resource, new Date(), new Date(), null, 1, true);

        Mockito.when(mockAuthDAO.findSchedulerUserByInstitutionRole()).thenReturn(userList);

        resourceService.sendRemoveOverrideScheduleMail(resourceSchedule, "this is editStart", "this is editEnd",
                "This is an institution", "");

        Mockito.verify(mockAuthDAO).findSchedulerUserByInstitutionRole();
        Mockito.verify(mockMailHandler, Mockito.never()).sendOptionalEmails(Mockito.any(SimpleMailMessage.class));
    }

    @Test
    public void testSendRemoveOverrideScheduleMailNullEmail() {
        final User user = ServiceTestsDAOUtility.createMockUser();
        user.setEmail(null);
        final List<User> userList = new ArrayList<>();
        userList.add(user);
        final Resource resource = getDummyResource();
        final ResourceSchedule resourceSchedule = new ResourceSchedule(resource, new Date(), new Date(), null, 1, true);

        Mockito.when(mockAuthDAO.findSchedulerUserByInstitutionRole()).thenReturn(userList);

        // Get the absolute path to the project then appending to the directory I wish to get to.
        final StringBuilder url = new StringBuilder(Paths.get(".").toAbsolutePath().normalize().toString());
        final int index = url.lastIndexOf("/") + 1;
        url.delete(index, url.length());
        url.append("web/src/main/webapp/WEB-INF");

        resourceService.sendRemoveOverrideScheduleMail(resourceSchedule, "this is editStart", "this is editEnd", "This is an institution", url.toString());

        Mockito.verify(mockAuthDAO).findSchedulerUserByInstitutionRole();
        Mockito.verify(mockMailHandler, Mockito.never()).sendOptionalEmails(Mockito.any(SimpleMailMessage.class));
    }

    @Test
    public void testDeleteDefaultSchedule() {
        final User user = ServiceTestsDAOUtility.createMockUser();
        final String ipAddress = "23.23.23.23";
        final Resource resource = getDummyResource();
        final ResourceSchedule resourceSchedule = new ResourceSchedule(resource, null, null, null, null, true);

        Mockito.when(mockResourceDAO.findResourceScheduleById(10)).thenReturn(resourceSchedule);

        final BooleanRequest result = resourceService.deleteDefaultAvailability(10, user, ipAddress);

        Mockito.verify(mockResourceDAO).findResourceScheduleById(10);
        Mockito.verify(mockAuditService).logResourceActivity(ipAddress, resourceSchedule.getResource(), user,
                Statics.AUDIT_RESOURCE_DEFAULT_DELETE, null, null);
        Mockito.verify(mockResourceDAO).deleteEntity(resourceSchedule);
        assertTrue(result.isResult());
    }

    @Test
    public void testDeleteSublocationClosureInterval() {
        final User user = ServiceTestsDAOUtility.createMockUser();
        final String ipAddress = "14.33.170.1";
        final SublocationClosureInterval closureInterval = new SublocationClosureInterval(null, null, "",
                new Sublocation("", new Institution("","")));
        closureInterval.setId(10);

        Mockito.when(mockSublocationClosureIntervalDTO.getSublocationClosureIntervalId()).thenReturn(10);
        Mockito.when(mockResourceDAO.findBySublocationClosureIntervalId(10)).thenReturn(closureInterval);

        final BooleanResultDTO result= resourceService.deleteSublocationClosureInterval(mockSublocationClosureIntervalDTO,user, ipAddress);

        Mockito.verify(mockResourceDAO).findBySublocationClosureIntervalId(10);
        Mockito.verify(mockAuditService).logResourceSublocationClosureActivity(ipAddress, closureInterval.getSublocation(),
                user, Statics.AUDIT_RESOURCE_SUBLOCATION_CLOSURE_DELETE, null, null);
        Mockito.verify(mockResourceDAO).deleteEntity(closureInterval);

        assertTrue(result.isResult());
    }

    @Test
    public void testRemoveResourceAlternative() {
        final Resource resource = this.getDummyResource();
        final Resource altResource1 = this.getDummyResource();
        altResource1.setId(12);
        final Resource altResource2 = this.getDummyResource();
        altResource2.setId(13);
        final User user =ServiceTestsDAOUtility.createMockUser();
        final List<ResourceAlternate> list = new ArrayList<>();
        final ResourceAlternate alternate1 = new ResourceAlternate(altResource1, resource);
        alternate1.setId(2);
        list.add(alternate1);
        final ResourceAlternate alternate2 = new ResourceAlternate(altResource2, resource);
        alternate2.setId(3);
        list.add(alternate2);

        Mockito.when(mockResourceDAO.findResourceById(1)).thenReturn(resource);
        Mockito.when(mockResourceDAO.findResourceAlternates(resource)).thenReturn(list);

        final BooleanRequest result = resourceService.deleteResourceAlternative(1, 2, user, "86.75.30.9");

        Mockito.verify(mockResourceDAO).findResourceById(1);
        Mockito.verify(mockResourceDAO).findResourceAlternates(resource);
        Mockito.verify(mockResourceDAO).deleteEntity(alternate1);
        Mockito.verify(mockAuditService).logResourceActivity("86.75.30.9", resource, user,
                Statics.AUDIT_RESOURCE_ALTERNATE_DELETE, null, null);

        assertTrue(result.isResult());
    }

    @Test
    public void testGetSelectedResourceAnnotationsNull() {
        final String annotationsList = "1";

        final TemplateResource templateResource = new TemplateResource();
        templateResource.setId(11);

        final TemplateResourceAnnotations templateResourceAnnotations = new TemplateResourceAnnotations();
        templateResourceAnnotations.setId(100);
        templateResourceAnnotations.setComment("Template Comment");
        templateResourceAnnotations.setQuantity(15);

        final LineLevelAnnotations lineLevelAnnotations1 = new LineLevelAnnotations();
        lineLevelAnnotations1.setId(1);

        Mockito.when(mockResourceDAO.findLineLevelAnnotationsById(1)).thenReturn(lineLevelAnnotations1);
        Mockito.when(mockTemplateResourceDAO.findTemplateResourceById(11)).thenReturn(templateResource);
        Mockito.when(mockResourceDAO.findTemplateAnnotationsByTemplateResourceAndLineLevel(templateResource,
                lineLevelAnnotations1)).thenReturn(templateResourceAnnotations);

        final List<LineLevelAnnotations> results = resourceService.getSelectedResourceAnnotations(annotationsList, null, 11);

        Mockito.verify(mockResourceDAO).findLineLevelAnnotationsById(1);
        Mockito.verify(mockResourceDAO, Mockito.never()).findTemplateAnnotationsByTemplateResourceAndLineLevel(Mockito.any(TemplateResource.class),
                Mockito.any(LineLevelAnnotations.class));
        Mockito.verify(mockResourceDAO, Mockito.never()).findBookedResourceAnnotationsByBookedResourceAndLineLevel(
                Mockito.any(BookedResource.class), Mockito.any(LineLevelAnnotations.class));

        assertEquals(results.get(0).getId().intValue(), 1);
        assertEquals(results.get(0).getQuantity(), 0);
        assertEquals(results.get(0).getComment(), null);
        assertEquals(results.get(0).getResourceAnnotations(), 0);
    }

    @Test
    public void testGetSelectedResourceAnnotationsEdit() {
        final String annotationsList = "1,  2, 12";

        final TemplateResource templateResource = new TemplateResource();
        templateResource.setId(11);

        final TemplateResourceAnnotations templateResourceAnnotations = new TemplateResourceAnnotations();
        templateResourceAnnotations.setId(100);
        templateResourceAnnotations.setComment("Template Comment");
        templateResourceAnnotations.setQuantity(15);

        final LineLevelAnnotations lineLevelAnnotations1 = new LineLevelAnnotations();
        lineLevelAnnotations1.setId(1);

        final LineLevelAnnotations lineLevelAnnotations2 = new LineLevelAnnotations();
        lineLevelAnnotations2.setId(2);

        final LineLevelAnnotations lineLevelAnnotations12 = new LineLevelAnnotations();
        lineLevelAnnotations12.setId(12);

        Mockito.when(mockResourceDAO.findLineLevelAnnotationsById(1)).thenReturn(lineLevelAnnotations1);
        Mockito.when(mockResourceDAO.findLineLevelAnnotationsById(12)).thenReturn(lineLevelAnnotations12);
        Mockito.when(mockResourceDAO.findLineLevelAnnotationsById(2)).thenReturn(lineLevelAnnotations2);
        Mockito.when(mockTemplateResourceDAO.findTemplateResourceById(11)).thenReturn(templateResource);
        Mockito.when(mockResourceDAO.findTemplateAnnotationsByTemplateResourceAndLineLevel(templateResource,
                lineLevelAnnotations1)).thenReturn(templateResourceAnnotations);
        Mockito.when(mockResourceDAO.findTemplateAnnotationsByTemplateResourceAndLineLevel(templateResource,
                lineLevelAnnotations2)).thenReturn(templateResourceAnnotations);
        Mockito.when(mockResourceDAO.findTemplateAnnotationsByTemplateResourceAndLineLevel(templateResource,
                lineLevelAnnotations12)).thenReturn(null);

        final List<LineLevelAnnotations> results = resourceService.getSelectedResourceAnnotations(annotationsList, "edit", 11);

        Mockito.verify(mockResourceDAO).findLineLevelAnnotationsById(1);
        Mockito.verify(mockResourceDAO).findLineLevelAnnotationsById(12);
        Mockito.verify(mockResourceDAO).findLineLevelAnnotationsById(2);
        Mockito.verify(mockTemplateResourceDAO, times(3)).findTemplateResourceById(11);
        Mockito.verify(mockResourceDAO).findTemplateAnnotationsByTemplateResourceAndLineLevel(templateResource,
                lineLevelAnnotations1);
        Mockito.verify(mockResourceDAO).findTemplateAnnotationsByTemplateResourceAndLineLevel(templateResource,
                lineLevelAnnotations2);
        Mockito.verify(mockResourceDAO).findTemplateAnnotationsByTemplateResourceAndLineLevel(templateResource,
                lineLevelAnnotations12);

        assertEquals(results.get(0).getId().intValue(), 1);
        assertEquals(results.get(0).getQuantity(), 15);
        assertEquals(results.get(0).getComment(), "Template Comment");
        assertEquals(results.get(0).getResourceAnnotations(), 100);

        assertEquals(results.get(1).getId().intValue(), 2);
        assertEquals(results.get(1).getQuantity(), 15);
        assertEquals(results.get(1).getComment(), "Template Comment");
        assertEquals(results.get(1).getResourceAnnotations(), 100);

        assertEquals(results.get(2).getId().intValue(), 12);
        assertEquals(results.get(2).getQuantity(), 1);
        assertEquals(results.get(2).getComment(), null);
    }

    @Test
    public void testGetSelectedResourceAnnotationsOverrideEdit() {
        final String annotationsList = "1, 12";
        final BookedResource bookedResource = new BookedResource(null, null, null, null, null, null, "",
                "", "", "", null, null, null);
        bookedResource.setId(11);
        final OverrideBookedResourceAnnotations overrideBookedResourceAnnotations =
                new OverrideBookedResourceAnnotations();
        overrideBookedResourceAnnotations.setId(117);
        overrideBookedResourceAnnotations.setComment("All your base belongs to us");
        overrideBookedResourceAnnotations.setQuantity(16);
        final LineLevelAnnotations lineLevelAnnotations1 = new LineLevelAnnotations();
        lineLevelAnnotations1.setId(1);
        final LineLevelAnnotations lineLevelAnnotations12 = new LineLevelAnnotations();
        lineLevelAnnotations12.setId(12);

        Mockito.when(mockResourceDAO.findLineLevelAnnotationsById(1)).thenReturn(lineLevelAnnotations1);
        Mockito.when(mockResourceDAO.findLineLevelAnnotationsById(12)).thenReturn(lineLevelAnnotations12);
        Mockito.when(mockAppointmentDAO.findBookedResourceById(11)).thenReturn(bookedResource);
        Mockito.when(mockResourceDAO.findBookedResourceAnnotationsByBookedResourceAndLineLevel(bookedResource,
                lineLevelAnnotations1)).thenReturn(overrideBookedResourceAnnotations);
        Mockito.when(mockResourceDAO.findBookedResourceAnnotationsByBookedResourceAndLineLevel(bookedResource,
                lineLevelAnnotations12)).thenReturn(null);

        final List<LineLevelAnnotations> results = resourceService.getSelectedResourceAnnotations(annotationsList,
                "override_edit", 11);

        Mockito.verify(mockResourceDAO).findLineLevelAnnotationsById(1);
        Mockito.verify(mockResourceDAO).findLineLevelAnnotationsById(12);
        Mockito.verify(mockAppointmentDAO, times(2)).findBookedResourceById(11);
        Mockito.verify(mockResourceDAO).findBookedResourceAnnotationsByBookedResourceAndLineLevel(bookedResource,
                lineLevelAnnotations1);
        Mockito.verify(mockResourceDAO).findBookedResourceAnnotationsByBookedResourceAndLineLevel(bookedResource,
                lineLevelAnnotations12);

        assertEquals(results.get(0).getId().intValue(), 1);
        assertEquals(results.get(0).getQuantity(), 16);
        assertEquals(results.get(0).getComment(), "All your base belongs to us");
        assertEquals(results.get(0).getResourceAnnotations(), 117);

        assertEquals(results.get(1).getId().intValue(), 12);
        assertEquals(results.get(1).getQuantity(), 1);
        assertEquals(results.get(1).getComment(), null);

    }

    @Test
    public void testGetSelectedResourceAnnotationsNew() {
        final List<String> list = new ArrayList<>();
        list.add("new");
        list.add("override_new");
        final String annotationsList = "1, 12";
        final LineLevelAnnotations lineLevelAnnotations1 = new LineLevelAnnotations();
        lineLevelAnnotations1.setId(1);
        final LineLevelAnnotations lineLevelAnnotations12 = new LineLevelAnnotations();
        lineLevelAnnotations12.setId(12);

        Mockito.when(mockResourceDAO.findLineLevelAnnotationsById(1)).thenReturn(lineLevelAnnotations1);
        Mockito.when(mockResourceDAO.findLineLevelAnnotationsById(12)).thenReturn(lineLevelAnnotations12);

        for(String mode : list) {
            final List<LineLevelAnnotations> results = resourceService.getSelectedResourceAnnotations(annotationsList,
                    mode, -1);

            assertEquals(results.get(0).getId().intValue(), 1);
            assertEquals(results.get(0).getQuantity(), 1);
            assertEquals(results.get(0).getComment(), null);

            assertEquals(results.get(1).getId().intValue(), 12);
            assertEquals(results.get(1).getQuantity(), 1);
            assertEquals(results.get(1).getComment(), null);
        }

        Mockito.verify(mockResourceDAO, Mockito.times(2)).findLineLevelAnnotationsById(1);
        Mockito.verify(mockResourceDAO, Mockito.times(2)).findLineLevelAnnotationsById(12);

    }

    @Test
    public void testGetBookedResourceAnnotations() {
        final Resource resource = this.getDummyResource();
        final BookedResource bookedResourceById = new BookedResource(60, null, null, null, null, resource,
                "", "", "", "", null, null, true);
        final OverrideBookedResourceAnnotations bookedAnnotations = new OverrideBookedResourceAnnotations();
        final LineLevelAnnotations lineLevelAnnotations = new LineLevelAnnotations();
        final ResourceAnnotation resourceAnnotation = new ResourceAnnotation();
        resourceAnnotation.setLineLevelAnnotations(lineLevelAnnotations);
        bookedAnnotations.setId(10);
        bookedAnnotations.setQuantity(15);
        bookedAnnotations.setComment("Comment");
        bookedAnnotations.setLineLevelAnnotations(lineLevelAnnotations);
        final List<OverrideBookedResourceAnnotations> listOverride = new ArrayList<>();
        listOverride.add(bookedAnnotations);
        final List<LineLevelAnnotations> listLineLevel = new ArrayList<>();
        listLineLevel.add(lineLevelAnnotations);
        final List<ResourceAnnotation> listResourceAnnotation = new ArrayList<>();
        listResourceAnnotation.add(resourceAnnotation);

        Mockito.when(mockAppointmentDAO.findBookedResourceById(1)).thenReturn(bookedResourceById);
        Mockito.when(mockResourceDAO.findResourceById(1)).thenReturn(resource);
        Mockito.when(mockResourceDAO.findOverrideBookedResourceAnnotationsByBookedResource(bookedResourceById)).thenReturn(listOverride);
        Mockito.when(mockResourceDAO.findResourcesAnnotationsByResource(resource)).thenReturn(listResourceAnnotation);

        final List<LineLevelAnnotations> list = resourceService.getBookedResourceAnnotations(1);

        Mockito.verify(mockAppointmentDAO).findBookedResourceById(1);
        Mockito.verify(mockResourceDAO).findOverrideBookedResourceAnnotationsByBookedResource(bookedResourceById);
        Mockito.verify(mockResourceDAO).findResourceById(1);
        Mockito.verify(mockResourceDAO).findResourcesAnnotationsByResource(resource);

        assertEquals(list,listLineLevel);
        assertEquals(list.get(0).isSelected(), true);
        assertEquals(list.get(0).getQuantity(), 15);
        assertEquals(list.get(0).getComment(), "Comment");
        assertEquals(list.get(0).getResourceAnnotations(), 10);
    }

    @Test
    public void testGetResourcesWithSublocation(){
        final User user = ServiceTestsDAOUtility.createMockUser();
        final ResourcesResponse resourcesResponse = new ResourcesResponse(0,0,"","", 0,"", true, 1L);
        final List<ResourcesResponse> resourcesResponseList = new ArrayList<>();
        resourcesResponseList.add(resourcesResponse);

        Mockito.when(mockResourceDAO.findResourceListWithSublocation("id", "name", "active", 1, 2, null)).thenReturn(resourcesResponseList);

        final List<ResourcesResponse> list = resourceService.getResourcesWithSublocation("id", "name", "active", 1, 2, user,  "192.168.0.1", null);

        Mockito.verify(mockAuditService).logViewActivity("192.168.0.1", user, Statics.AUDIT_RESOURCE_VIEW);
        Mockito.verify(mockResourceDAO).findResourceListWithSublocation("id", "name", "active", 1, 2, null);

        assertEquals(list, resourcesResponseList);
    }

    @Test
    public void testGetResourcesWithSublocationSearchSpecified(){
        final User user = ServiceTestsDAOUtility.createMockUser();
        final ResourcesResponse resourcesResponse = new ResourcesResponse(0,0,"","", 0,"", true, 1L);
        final List<ResourcesResponse> resourcesResponseList = new ArrayList<>();
        resourcesResponseList.add(resourcesResponse);

        Mockito.when(mockResourceDAO.findResourceListWithSublocation("id", "name", "active", 1, 2, mockSearchDTO)).thenReturn(resourcesResponseList);

        final List<ResourcesResponse> list = resourceService.getResourcesWithSublocation("id", "name", "active", 1, 2, user,  "192.168.0.1", mockSearchDTO);

        Mockito.verify(mockAuditService).logViewActivity("192.168.0.1", user, Statics.AUDIT_RESOURCE_VIEW);
        Mockito.verify(mockResourceDAO).findResourceListWithSublocation("id", "name", "active", 1, 2, mockSearchDTO);

        assertEquals(list, resourcesResponseList);
    }

    @Test
    public void testGetResourceDetail(){
        final Resource resource = this.getDummyResource();
        final ResourcesResponse resourcesResponse = new ResourcesResponse(0,0,"","", 0, "", true, 1L);
        final User user = ServiceTestsDAOUtility.createMockUser();

        Mockito.when(mockResourceDAO.findResourceById(1)).thenReturn(resource);
        Mockito.when(mockResourceDAO.findResourceDetail(resource)).thenReturn(resourcesResponse);

        final ResourcesResponse response = resourceService.getResourceDetail(1, user, "192.168.0.1");

        Mockito.verify(mockAuditService).logResourceActivity("192.168.0.1", resource, user, Statics.AUDIT_RESOURCE_VIEW, null, null);
        Mockito.verify(mockResourceDAO).findResourceDetail(resource);

        assertEquals(response, resourcesResponse);
    }

    @Test
    public void testGetResourceAlternates() {
        final Resource resource = this.getDummyResource();
        final ResourcesResponse resourcesResponse = new ResourcesResponse(0,0,"","", 0, "", true, 1L);
        final List<ResourcesResponse> resourcesResponseList = new ArrayList<>();
        resourcesResponseList.add(resourcesResponse);

        Mockito.when(mockResourceDAO.findResourceById(1)).thenReturn(resource);
        Mockito.when(mockResourceDAO.findResourceAlternatesByResource(resource, "id", "id", 2, 3)).thenReturn(resourcesResponseList);

        final List<ResourcesResponse> result = resourceService.getResourceAlternates(1, "id", "id", 2, 3);

        Mockito.verify(mockResourceDAO).findResourceById(1);
        Mockito.verify(mockResourceDAO).findResourceAlternatesByResource(resource, "id", "id", 2, 3);

        assertEquals(result, resourcesResponseList);
    }

    @Test
    public void testUpdateResource() {
        final Resource resource = new Resource(100, "Nurse Test", ResourceType.Nursing, null, new ArrayList<>(),
                null, null, null, null);
        final Sublocation sublocation = new Sublocation("", new Institution("", ""));
        sublocation.setId(2);
        final ResourceSublocation resourceSublocation = new ResourceSublocation(sublocation, resource, null);
        final User user = ServiceTestsDAOUtility.createMockUser();

        Mockito.when(mockResourceDAO.findResourceById(100)).thenReturn(resource);
        Mockito.when(mockResourceDAO.findSublocationById(2)).thenReturn(sublocation);
        Mockito.when(mockResourceDAO.findUniqueResourceSublocationByResource(resource)).thenReturn(resourceSublocation);

        final ModifyResourceRequest modifyResourceRequest =
                new ModifyResourceRequest(resource.getId(), 2, "Room", false);

        final StatusAndMessageResponseDTO response = resourceService.updateResource(modifyResourceRequest, user, "8080");

        Mockito.verify(mockResourceDAO).findResourceById(100);
        Mockito.verify(mockResourceDAO).findSublocationById(2);
        Mockito.verify(mockResourceDAO).updateEntity(resource);
        Mockito.verify(mockResourceDAO).updateEntity(resourceSublocation);
        Mockito.verify(mockAuditService).logResourceActivity("8080", resource, user,
            Statics.AUDIT_RESOURCE_UPDATE,
            "name: Nurse Test --> Room - . sublocation:  (no change). isActive: null (no change). ",
            null);

        Assert.assertTrue(response.isSuccessful());
        Assert.assertEquals("Room - ", resource.getName());
    }

    @Test
    public void testUpdateResourceActivate() {
        final Resource resource = new Resource(100, "Room - 1", ResourceType.Nursing, null, new ArrayList<>(),
                null, null, null, null);
        final Sublocation sublocation = new Sublocation("1", new Institution("", ""));
        sublocation.setId(2);
        final ResourceSublocation resourceSublocation = new ResourceSublocation(sublocation, resource, false);
        final User user = ServiceTestsDAOUtility.createMockUser();
        final ModifyResourceRequest modifyResourceRequest = new ModifyResourceRequest(resource.getId(), 2, "Room", true);

        Mockito.when(mockResourceDAO.findResourceByName(resource.getName())).thenReturn(resource);
        Mockito.when(mockResourceDAO.findResourceById(100)).thenReturn(resource);
        Mockito.when(mockResourceDAO.findSublocationById(2)).thenReturn(sublocation);
        Mockito.when(mockResourceDAO.findUniqueResourceSublocationByResource(resource)).thenReturn(resourceSublocation);

        final StatusAndMessageResponseDTO response = resourceService.updateResource(modifyResourceRequest, user, "8080");

        Mockito.verify(mockResourceDAO).findResourceById(100);
        Mockito.verify(mockResourceDAO).findSublocationById(2);
        Mockito.verify(mockResourceDAO).updateEntity(resource);
        Mockito.verify(mockResourceDAO).updateEntity(resourceSublocation);
        Mockito.verify(mockAuditService).logResourceActivity("8080", resource, user,
                Statics.AUDIT_RESOURCE_UPDATE,
                "name: Room - 1 (no change). sublocation: 1 (no change). isActive: false --> true. ",
                null);

        assertTrue(response.isSuccessful());
        assertEquals("Room - 1", resource.getName());
        assertTrue(resourceSublocation.isActive());

    }

    @Test
    public void testUpdateResourceNoName() {
        final Resource resource = new Resource(100, "Nurse Test", ResourceType.Nursing, null, new ArrayList<>(),
                null, null, null, null);
        final Sublocation sublocation = new Sublocation("", new Institution("", ""));
        sublocation.setId(2);
        final ResourceSublocation resourceSublocation = new ResourceSublocation(sublocation, resource, null);
        final User user = ServiceTestsDAOUtility.createMockUser();

        Mockito.when(mockResourceDAO.findResourceById(100)).thenReturn(resource);
        Mockito.when(mockResourceDAO.findSublocationById(2)).thenReturn(sublocation);
        Mockito.when(mockResourceDAO.findUniqueResourceSublocationByResource(resource)).thenReturn(resourceSublocation);

        ModifyResourceRequest modifyResourceRequest = new ModifyResourceRequest(resource.getId(), 4, "", false);

        final StatusAndMessageResponseDTO response = resourceService.updateResource(modifyResourceRequest, user, "8080");

        Mockito.verify(mockResourceDAO).findResourceById(100);
        Mockito.verify(mockResourceDAO).findSublocationById(4);
        Mockito.verify(mockResourceDAO, Mockito.never()).updateEntity(resource);
        Mockito.verify(mockResourceDAO, Mockito.never()).updateEntity(resourceSublocation);
        Mockito.verify(mockAuditService,
                Mockito.never()).logResourceActivity("8080", resource, user, Statics.AUDIT_RESOURCE_UPDATE, null, null);

        Assert.assertFalse(response.isSuccessful());
        Assert.assertEquals("Nurse Test", resource.getName());
        Assert.assertEquals(2L, resourceSublocation.getSublocation().getId().longValue());
    }

    @Test
    public void testUpdateResourceNameAlreadyExists() {
        final Resource resource = new Resource(100, "Nurse Test", ResourceType.Nursing, null, new ArrayList<>(),
                null, null, null, null);
        final Sublocation sublocation = new Sublocation("", new Institution("", ""));
        sublocation.setId(4);
        final ResourceSublocation resourceSublocation = new ResourceSublocation(sublocation, resource, null);
        final User user = ServiceTestsDAOUtility.createMockUser();

        Mockito.when(mockResourceDAO.findResourceById(100)).thenReturn(resource);
        Mockito.when(mockResourceDAO.findResourceById(100)).thenReturn(getDummyResource());
        Mockito.when(mockResourceDAO.findSublocationById(4)).thenReturn(sublocation);
        Mockito.when(mockResourceDAO.findUniqueResourceSublocationByResource(resource)).thenReturn(resourceSublocation);
        Mockito.when(mockResourceDAO.findResourceByName("Nurse Test Seven - ")).thenReturn(this.getDummyResource());

        ModifyResourceRequest modifyResourceRequest = new ModifyResourceRequest(resource.getId(), 4, "Nurse Test Seven", false);

        final StatusAndMessageResponseDTO response = resourceService.updateResource(modifyResourceRequest, user, "8080");

        Mockito.verify(mockResourceDAO).findResourceById(100);
        Mockito.verify(mockResourceDAO).findSublocationById(4);
        Mockito.verify(mockResourceDAO, Mockito.never()).updateEntity(resource);
        Mockito.verify(mockResourceDAO, Mockito.never()).updateEntity(resourceSublocation);
        Mockito.verify(mockAuditService,
                Mockito.never()).logResourceActivity("8080", resource, user, Statics.AUDIT_RESOURCE_UPDATE, null, null);

        assertFalse(response.isSuccessful());
        assertEquals(response.getMessage(), "There exists a resource of the same name.");
        assertEquals("Nurse Test", resource.getName());
        assertEquals(4L, resourceSublocation.getSublocation().getId().longValue());
    }

    @Test
    public void testUpdateResourceBadNameCharacters() {
        final Resource resource = new Resource(100, "Nurse Test", ResourceType.Nursing, null, new ArrayList<>(),
                null, null, null, null);
        final Sublocation sublocation = new Sublocation("", new Institution("", ""));
        sublocation.setId(4);
        final ResourceSublocation resourceSublocation = new ResourceSublocation(sublocation, resource, null);
        final User user = ServiceTestsDAOUtility.createMockUser();

        Mockito.when(mockResourceDAO.findResourceById(100)).thenReturn(resource);
        Mockito.when(mockResourceDAO.findSublocationById(4)).thenReturn(sublocation);

        ModifyResourceRequest modifyResourceRequest = new ModifyResourceRequest(resource.getId(), 4, "$$$$$$$ Test Seven", false);

        final StatusAndMessageResponseDTO response = resourceService.updateResource(modifyResourceRequest, user, "8080");

        Mockito.verify(mockResourceDAO).findResourceById(100);
        Mockito.verify(mockResourceDAO).findSublocationById(4);
        Mockito.verify(mockResourceDAO, Mockito.never()).updateEntity(resource);
        Mockito.verify(mockResourceDAO, Mockito.never()).updateEntity(resourceSublocation);
        Mockito.verify(mockAuditService, Mockito.never()).logResourceActivity("8080", resource, user, Statics.AUDIT_RESOURCE_UPDATE, null, null);

        assertFalse(response.isSuccessful());
        assertEquals(response.getMessage(), "Resource name must contain only the following characters: " +
                "a-z A-Z 0-9 , - ( ) / & and whitespace, and cannot be empty");
        assertEquals("Nurse Test", resource.getName());
        assertEquals(4L, resourceSublocation.getSublocation().getId().longValue());
    }

    @Test
    public void testUpdateResourceNoResourceSublocation() {
        final Resource resource = new Resource(100, "Nurse Test", ResourceType.Nursing, null, new ArrayList<>(),
                null, null, null, null);
        final Sublocation sublocation = new Sublocation("", new Institution("", ""));
        sublocation.setId(4);
        final ResourceSublocation resourceSublocation = new ResourceSublocation(sublocation, resource, null);
        final User user = ServiceTestsDAOUtility.createMockUser();

        Mockito.when(mockResourceDAO.findResourceById(100)).thenReturn(resource);
        Mockito.when(mockResourceDAO.findSublocationById(4)).thenReturn(sublocation);
        Mockito.when(mockResourceDAO.findResourceByName("Nurse Test Seven - ")).thenReturn(this.getDummyResource());

        ModifyResourceRequest modifyResourceRequest = new ModifyResourceRequest(resource.getId(), 4, "Nurse Test", false);

        final StatusAndMessageResponseDTO response = resourceService.updateResource(modifyResourceRequest, user, "8080");

        Mockito.verify(mockResourceDAO).findResourceById(100);
        Mockito.verify(mockResourceDAO).findSublocationById(4);
        Mockito.verify(mockResourceDAO, Mockito.never()).updateEntity(resource);
        Mockito.verify(mockResourceDAO, Mockito.never()).updateEntity(resourceSublocation);
        Mockito.verify(mockAuditService, Mockito.never()).logResourceActivity("8080", resource, user, Statics.AUDIT_RESOURCE_UPDATE, null, null);

        assertFalse(response.isSuccessful());
        assertEquals(response.getMessage(), "There is no Sub-Location associated with this resource");
        assertEquals("Nurse Test", resource.getName());
        assertEquals(4L, resourceSublocation.getSublocation().getId().longValue());
    }


    @Test
    public void testChangeResourceStatus() {
        final Resource resource = getDummyResource();
        final Sublocation sublocation = new Sublocation("Sublocation 1",
                new Institution("An institute", "longer institute name"));
        final ResourceSublocation resourceSublocation = new ResourceSublocation(sublocation, resource, false);

        Mockito.when(mockResourceDAO.findResourceSublocation(1, 11)).thenReturn(resourceSublocation);

        final BooleanResultDTO resultDTO = resourceService.changeResourceStatus(1, 11, true);

        Mockito.verify(mockResourceDAO).updateEntity(resourceSublocation);
        assertTrue(resultDTO.isResult());
        assertNull(resultDTO.getErrorMsg());
    }

    @Test
    public void testChangeResourceStatusNoSublocation() {
        final BooleanResultDTO resultDTO = resourceService.changeResourceStatus(1, 11, true);

        Mockito.verify(mockResourceDAO, never()).updateEntity(any(BaseEntity.class));
        assertFalse(resultDTO.isResult());
        assertNull(resultDTO.getErrorMsg());
    }

    @Test
    public void testActivateResources() {
        final ResourceIdsRequestDTO resourceIdsRequestDTO = new ResourceIdsRequestDTO();
        final Resource resource = this.getDummyResource();
        final Sublocation sublocation = new Sublocation("", new Institution("", ""));
        sublocation.setId(10);
        final ResourceSublocation resourceSublocation = new ResourceSublocation();
        final List<Integer> list = new ArrayList<>();
        list.add(1);
        final List<Sublocation> sublocationList = new ArrayList<>();
        sublocationList.add(sublocation);
        resourceSublocation.setResource(resource);
        resourceSublocation.setSublocation(sublocation);
        resourceSublocation.setId(33);
        resource.setSublocations(sublocationList);
        resourceIdsRequestDTO.setResourcesIds(list);

        Mockito.when(mockResourceDAO.findResourceById(1)).thenReturn(resource);
        Mockito.when(mockResourceDAO.findResourceSublocation(1, 10)).thenReturn(resourceSublocation);

        final ResourcesBooleanResponseDTO result = this.resourceService.activateResources(resourceIdsRequestDTO);

        Mockito.verify(mockResourceDAO).findResourceSublocation(1, 10);
        Mockito.verify(mockResourceDAO).findResourceById(1);
        Mockito.verify(mockResourceDAO).updateEntity(Mockito.any(ResourceSublocation.class));

        Assert.assertTrue(result.isResult());
        Assert.assertEquals(result.getResourcesResponses().size(), 1);
    }

    @Test
    public void testActivateResourcesNotAllResourcesFound() {
        final ResourceIdsRequestDTO resourceIdsRequestDTO = new ResourceIdsRequestDTO();
        final Resource resource = this.getDummyResource();
        final Sublocation sublocation = new Sublocation("", new Institution("", ""));
        sublocation.setId(10);
        final ResourceSublocation resourceSublocation = new ResourceSublocation();
        final List<Integer> list = new ArrayList<>();
        list.add(1);
        resourceSublocation.setResource(resource);
        resourceSublocation.setSublocation(sublocation);
        resourceSublocation.setId(33);
        resourceIdsRequestDTO.setResourcesIds(list);

        Mockito.when(mockResourceDAO.findResourceById(1)).thenReturn(resource);

        final ResourcesBooleanResponseDTO result = this.resourceService.activateResources(resourceIdsRequestDTO);

        Mockito.verify(mockResourceDAO).findResourceById(1);
        Mockito.verify(mockResourceDAO, Mockito.never()).findResourceSublocation(1, 10);
        Mockito.verify(mockResourceDAO, Mockito.never()).updateEntity(Mockito.any(ResourceSublocation.class));

        Assert.assertFalse(result.isResult());
        Assert.assertEquals(result.getErrorMsg(), "Not all resources specified were able to be found.");
    }

    @Test
    public void testActivateResourcesNoResources() {
        final ResourcesBooleanResponseDTO result = this.resourceService.activateResources(new ResourceIdsRequestDTO());

        Mockito.verify(mockResourceDAO, Mockito.never()).findResourceSublocation(1, 10);
        Mockito.verify(mockResourceDAO, Mockito.never()).findResourceById(1);
        Mockito.verify(mockResourceDAO, Mockito.never()).updateEntity(Mockito.any(ResourceSublocation.class));

        Assert.assertFalse(result.isResult());
        Assert.assertEquals(result.getErrorMsg(), "No resources specified to be activated.");
    }

    @Test
    public void testAddAnnotations() {
        final Resource resource = this.getDummyResource();
        final AnnotationsIdRequestDTO dto = new AnnotationsIdRequestDTO();
        final User user = ServiceTestsDAOUtility.createMockUser();
        final List<Integer> list = new ArrayList<>();
        list.add(10);
        dto.setResourceId(resource.getId());
        dto.setAnnotationIds(list);

        Mockito.when(mockResourceDAO.findResourceById(1)).thenReturn(resource);
        Mockito.when(mockResourceDAO.findLineLevelAnnotationsById(10)).thenReturn(new LineLevelAnnotations());

        final BooleanResultDTO resultDTO = resourceService.addAnnotations(dto, user, "");

        Assert.assertTrue(resultDTO.isResult());
        Assert.assertNull(resultDTO.getErrorMsg());
        Mockito.verify(mockResourceDAO).createEntity(Mockito.any(ResourceAnnotation.class));
    }

    @Test
    public void testAddAlreadyAddedAnnotations() {
        final Resource resource = this.getDummyResource();
        final AnnotationsIdRequestDTO dto = new AnnotationsIdRequestDTO();
        final User user = ServiceTestsDAOUtility.createMockUser();
        final List<Integer> list = new ArrayList<>();
        list.add(10);
        final LineLevelAnnotations lineLevelAnnotations = new LineLevelAnnotations();
        lineLevelAnnotations.setId(10);
        lineLevelAnnotations.setName("A name");
        final ResourceAnnotation resourceAnnotation = new ResourceAnnotation();
        resourceAnnotation.setResource(resource);
        resourceAnnotation.setLineLevelAnnotations(lineLevelAnnotations);
        final List<ResourceAnnotation> resourceAnnotations = new ArrayList<>();
        resourceAnnotations.add(resourceAnnotation);
        dto.setResourceId(resource.getId());
        dto.setAnnotationIds(list);

        Mockito.when(mockResourceDAO.findResourceById(1)).thenReturn(resource);
        Mockito.when(mockResourceDAO.findLineLevelAnnotationsById(10)).thenReturn(lineLevelAnnotations);
        Mockito.when(mockResourceDAO.findResourcesAnnotationsByResource(resource)).thenReturn(resourceAnnotations);

        final BooleanResultDTO resultDTO = resourceService.addAnnotations(dto, user, "");

        Assert.assertTrue(resultDTO.isResult());
        Assert.assertNull(resultDTO.getErrorMsg());
        Mockito.verify(mockResourceDAO, Mockito.never()).createEntity(Mockito.any(ResourceAnnotation.class));
    }

    @Test
    public void testAddAnnotationsNoResource() {
        final AnnotationsIdRequestDTO dto = new AnnotationsIdRequestDTO();
        final User user = ServiceTestsDAOUtility.createMockUser();
        final List<Integer> list = new ArrayList<>();
        list.add(10);
        dto.setResourceId(0);
        dto.setAnnotationIds(list);

        Mockito.when(mockResourceDAO.findLineLevelAnnotationsById(10)).thenReturn(new LineLevelAnnotations());

        final BooleanResultDTO resultDTO = resourceService.addAnnotations(dto, user, "");

        Assert.assertFalse(resultDTO.isResult());
        Assert.assertEquals(resultDTO.getErrorMsg(), "Unable to find specified resource.");
        Mockito.verify(mockResourceDAO, Mockito.never()).createEntity(Mockito.any(ResourceSublocation.class));
    }

    @Test
    public void testAddAnnotationsNoAnnotations() {
        final Resource resource = this.getDummyResource();
        final AnnotationsIdRequestDTO dto = new AnnotationsIdRequestDTO();
        final User user = ServiceTestsDAOUtility.createMockUser();
        final List<Integer> list = new ArrayList<>();
        list.add(10);
        dto.setResourceId(resource.getId());
        dto.setAnnotationIds(list);

        Mockito.when(mockResourceDAO.findResourceById(1)).thenReturn(resource);

        final BooleanResultDTO resultDTO = resourceService.addAnnotations(dto, user, "");

        Assert.assertFalse(resultDTO.isResult());
        Assert.assertEquals(resultDTO.getErrorMsg(), "Not all specified annotations exist. Please try again.");
        Mockito.verify(mockResourceDAO, Mockito.never()).createEntity(Mockito.any(ResourceSublocation.class));
    }

    @Test
    public void testAddRestriction()
    {
        final int sharedResourceId = 4;
        final Resource resource = this.getDummyResource();
        final Resource sharedResource = new Resource(
                sharedResourceId, "Shared", ResourceType.Lab, 0, new ArrayList<>(),
                new ArrayList<>(), new ArrayList<>(),
                new ArrayList<>(), new ArrayList<>());

        final AddOrModifyRestrictionRequestDTO requestDTO = new AddOrModifyRestrictionRequestDTO();
        requestDTO.setSharedResourceId(sharedResourceId);
        requestDTO.setNotes("My Note");
        requestDTO.setResourceId(resource.getId());
        final User user = ServiceTestsDAOUtility.createMockUser();

        Mockito.when(mockResourceDAO.findResourceById(resource.getId())).thenReturn(resource);
        Mockito.when(mockResourceDAO.findResourceById(sharedResourceId)).thenReturn(sharedResource);

        final BooleanRequest resultDTO = resourceService.addRestriction(requestDTO, user, "");

        Assert.assertTrue(resultDTO.isResult());
        Mockito.verify(mockResourceDAO, Mockito.never()).createEntity(Mockito.any(Resource.class));
    }

    @Test
    public void testModifyRestriction()
    {
        final int sharedResourceId = 4;
        final Resource resource = this.getDummyResource();
        resource.setSharedResource(sharedResourceId);

        final Resource sharedResource = new Resource(
                sharedResourceId, "Shared", ResourceType.Lab, 0, new ArrayList<>(),
                new ArrayList<>(), new ArrayList<>(),
                new ArrayList<>(), new ArrayList<>());

        final int differentSharedResourceId = 10;
        final AddOrModifyRestrictionRequestDTO requestDTO = new AddOrModifyRestrictionRequestDTO();
        requestDTO.setSharedResourceId(differentSharedResourceId);
        requestDTO.setNotes("My Note");
        requestDTO.setResourceId(resource.getId());
        final User user = ServiceTestsDAOUtility.createMockUser();

        Mockito.when(mockResourceDAO.findResourceById(resource.getId())).thenReturn(resource);
        Mockito.when(mockResourceDAO.findResourceById(sharedResourceId)).thenReturn(sharedResource);

        final BooleanRequest resultDTO = resourceService.modifyRestriction(requestDTO, user, "");

        Assert.assertTrue(resultDTO.isResult());
        Mockito.verify(mockResourceDAO, Mockito.never()).createEntity(Mockito.any(Resource.class));
        Mockito.verify(mockResourceDAO).updateEntity(resource);

        Mockito.verify(mockResourceDAO, Mockito.never()).findResourceById(differentSharedResourceId);
    }

    @Test
    public void testDeleteRestriction()
    {
        final int sharedResourceId = 4;
        final Resource resource = this.getDummyResource();
        resource.setSharedResource(sharedResourceId);
        resource.setSharedResourceNotes("blah");

        Assert.assertNotNull(resource.getSharedResource());

        final Resource sharedResource = new Resource(
                sharedResourceId, "Shared", ResourceType.Lab, 0, new ArrayList<>(),
                new ArrayList<>(), new ArrayList<>(),
                new ArrayList<>(), new ArrayList<>());
        Assert.assertNotNull(sharedResource.getSharedResource());

        final User user = ServiceTestsDAOUtility.createMockUser();

        Mockito.when(mockResourceDAO.findResourceById(resource.getId())).thenReturn(resource);
        Mockito.when(mockResourceDAO.findResourceById(sharedResourceId)).thenReturn(sharedResource);

        final BooleanRequest resultDTO = resourceService.deleteRestriction(resource.getId(), user, "");

        Assert.assertTrue(resultDTO.isResult());
        Assert.assertNull(resource.getSharedResource());
        Assert.assertNull(sharedResource.getSharedResource());
    }

    private Resource getDummyResource() {
        return new Resource(
                1, "Original", ResourceType.Lab, 0, new ArrayList<>(),
                new ArrayList<>(), new ArrayList<>(),
                new ArrayList<>(), new ArrayList<>());
    }
}
