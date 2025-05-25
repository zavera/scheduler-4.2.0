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

import edu.harvard.catalyst.scheduler.core.Statics;
import edu.harvard.catalyst.scheduler.dto.BooleanResultDTO;
import edu.harvard.catalyst.scheduler.dto.TemplateResourceDTO;
import edu.harvard.catalyst.scheduler.dto.response.GanttResourceInfoDTO;
import edu.harvard.catalyst.scheduler.entity.*;
import edu.harvard.catalyst.scheduler.persistence.*;
import edu.harvard.catalyst.scheduler.util.DateUtility;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.*;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.doNothing;

/**
 * Created by xavier on 7/11/17.
 *
 * This test class is called TemplateResourceServiceTest even though
 * the class TemplateResourceService does not exist. At some point it would
 * be nice to factor the TemplateResource-related code out of AppointmentService
 * and into TemplateResourceService. In the meantime I did not want to add even
 * more code to AppointmentServiceTest.
 */
@RunWith(MockitoJUnitRunner.class)
public class TemplateResourceServiceTest {

    private AppointmentService appointmentService;
    private StudyService studyService;

    @Mock
    private ResourceDAO resourceDAO;

    @Mock
    private StudyDAO studyDAO;

    @Mock
    private AppointmentDAO appointmentDAO;

    @Mock
    private AuditService auditService;

    @Mock
    private AuthDAO mockAuthDAO;

    @Mock
    private TemplateResourceDAO templateResourceDAO;

    @Mock
    private SubjectDAO subjectDAO;
    private SearchAlgorithmService spySearchAlgorithmService = new SearchAlgorithmService();

    @Before
    public void setUp() {

        spySearchAlgorithmService = spy(spySearchAlgorithmService);
        appointmentService = makeService(null, null);
        studyService = makeStudyService();

    }

    private AppointmentService makeService(final ConflictChecker conflictChecker, final AppointmentConfirmer appointmentConfirmer) {
        return new AppointmentService(
                appointmentDAO,
                resourceDAO,
                studyDAO,
                mockAuthDAO,
                auditService,
                null,
                subjectDAO,
                templateResourceDAO, Optional.ofNullable(conflictChecker),
                Optional.ofNullable(appointmentConfirmer),
                spySearchAlgorithmService
        );
    }

    private StudyService makeStudyService() {
        return new StudyService(
                auditService,
                studyDAO,
                mockAuthDAO,
                null,
                subjectDAO,
                templateResourceDAO);
    }

    /**
     * This test could be improved to better probe the logic of the GanttResourceInfoDTO class
     */
    @Test
    public void testGetGanttResourceInfo() {

        Resource resource = new Resource();
        int resourceId = 2;
        resource.setId(resourceId);

        VisitTemplate visitTemplate = new VisitTemplate();
        int templateVisitId = 3;
        visitTemplate.setId(templateVisitId);

        TemplateResource templateResource1 = new TemplateResource();
        int templateResourceId1 = 4;
        templateResource1.setId(templateResourceId1);
        templateResource1.setResource(resource);
        templateResource1.setVisitTemplate(visitTemplate);
        templateResource1.setStartMinutes(10);
        templateResource1.setEndMinutes(20);
        templateResource1.setDuration(10);

        TemplateResource templateResource2 = new TemplateResource();
        int templateResourceId2 = 5;
        templateResource2.setId(templateResourceId2);
        templateResource2.setResource(resource);
        templateResource2.setVisitTemplate(visitTemplate);
        templateResource2.setStartMinutes(15);
        templateResource2.setEndMinutes(35);
        templateResource2.setDuration(20);

        when(appointmentDAO.findTemplateResourceById(templateResourceId1)).thenReturn(templateResource1);
        when(templateResourceDAO.findTemplateResourcesByVisitAndResource(templateVisitId, resourceId)).thenReturn(Arrays.asList(
                templateResource1, templateResource2
        ));

        GanttResourceInfoDTO response = appointmentService.getGanttResourceInfo(templateVisitId, templateResourceId1, 2);

        assertEquals(0, response.totalTimeOfRelevantStintsOfThisResource);
        assertEquals(30, response.totalTimeOfStintsOfThisResourceInVisit);
        assertEquals(0, response.totalRelevantTrStintsOfThisResource);
        assertEquals(2, response.totalTrStintsOfThisResourceInVisit);

    }

    @Test
    public void testGetTemplateResourcesUsedBetween() {

        int selectedDayInMinutes = 10;
        int nextDayInMinutes = 20;

        Resource resource = new Resource();
        int resourceId = 2;
        resource.setId(resourceId);

        VisitTemplate visitTemplate = new VisitTemplate();
        int templateVisitId = 3;
        visitTemplate.setId(templateVisitId);

        TemplateResource templateResource1 = new TemplateResource();
        int templateResourceId1 = 4;
        templateResource1.setId(templateResourceId1);

        TemplateResource templateResource2 = new TemplateResource();
        int templateResourceId2 = 5;
        templateResource2.setId(templateResourceId2);

        when(templateResourceDAO.getTemplateResourcesUsedOnDay(
                templateVisitId, selectedDayInMinutes, nextDayInMinutes
        )).thenReturn(Arrays.asList(templateResource1, templateResource2));

        List<TemplateResource> response = appointmentService.getTemplateResourcesUsedBetween(
                templateVisitId, selectedDayInMinutes, nextDayInMinutes);

        verify(templateResourceDAO, times(1)).getTemplateResourcesUsedOnDay(templateVisitId, selectedDayInMinutes, nextDayInMinutes);

        assertEquals(2, response.size());
        assertEquals(templateResource1.getId(), response.get(0).getId());
        assertEquals(templateResource2.getId(), response.get(1).getId());

    }

    @Test
    public void testGetSelectableTemplateResources() {

        Resource resource = new Resource();
        int resourceId = 2;
        resource.setId(resourceId);

        VisitTemplate visitTemplate = new VisitTemplate();
        int visitTemplateId = 3;
        visitTemplate.setId(visitTemplateId);

        TemplateResource templateResource1 = new TemplateResource();
        int templateResourceId1 = 4;
        templateResource1.setId(templateResourceId1);

        TemplateResource templateResource2 = new TemplateResource();
        int templateResourceId2 = 5;
        templateResource2.setId(templateResourceId2);

        boolean isBillable = true;
        String sortBy = "sort by";
        String orderBy = "boo";
        int page = 4;
        int maxResults = 10;

        when(studyDAO.findVisitById(visitTemplateId)).thenReturn(visitTemplate);
        when(templateResourceDAO.findTemplateResourcesByVisitAndBillable(
                visitTemplate, isBillable, sortBy, orderBy, page, maxResults)).thenReturn(
                        Arrays.asList(templateResource1, templateResource2)
        );
        List<TemplateResource> response = appointmentService.getSelectableTemplateResources(
                visitTemplateId, isBillable, sortBy, orderBy, page, maxResults
        );

        verify(studyDAO, times(1)).findVisitById(visitTemplateId);
        verify(templateResourceDAO, times(1)).findTemplateResourcesByVisitAndBillable(
                visitTemplate, isBillable, sortBy, orderBy, page, maxResults);

        assertEquals(2, response.size());
        assertEquals(templateResource1.getId(), response.get(0).getId());
        assertEquals(templateResource2.getId(), response.get(1).getId());

    }

    /**
     * AppointmentService.crudTemplateResources()
     * Case 1a: action type is "add_template_resource"
     */
    @Test
    public void testCrudTemplateResourcesAdd() {

        String actionType = "add_template_resource";

        executeCrudTemplateResourceAddOrCopyTest(actionType);

    }

    /**
     * AppointmentService.crudTemplateResources()
     * Case 1b: action type is "copy_template_resource"
     */
    @Test
    public void testCrudTemplateResourcesCopy() {

        String actionType = "copy_template_resource";

        executeCrudTemplateResourceAddOrCopyTest(actionType);

    }

    private void executeCrudTemplateResourceAddOrCopyTest(String actionType) {

        AppointmentService appointmentServiceSpy = spy(appointmentService);

        Resource resource = new Resource();
        int resourceId = 2;
        resource.setId(resourceId);

        Study study = new Study();

        VisitTemplate visitTemplate = new VisitTemplate();
        int visitTemplateId = 3;
        visitTemplate.setId(visitTemplateId);
        visitTemplate.setApproved(true);
        visitTemplate.setStudy(study);

        TemplateResource templateResource1 = new TemplateResource();
        int templateResourceId1 = 4;
        templateResource1.setId(templateResourceId1);

        TemplateResource templateResource2 = new TemplateResource();
        int templateResourceId2 = 5;
        templateResource2.setId(templateResourceId2);

        TemplateResourceDTO trDto = new TemplateResourceDTO();
        trDto.setVisitTemplate(visitTemplateId);
        trDto.setResource(resourceId);
        trDto.setAlternate(false);
        trDto.setBillable(false);

        String institution = "institution";
        String templatePath = "template path";
        User user = new User();
        String host = "1.2.3.4";

        when(studyDAO.findVisitById(visitTemplateId)).thenReturn(visitTemplate);
        when(resourceDAO.findResourceById(resourceId)).thenReturn(resource);
        doNothing().when(appointmentDAO).createEntity(any(TemplateResource.class));
        doNothing().when(appointmentServiceSpy).createTemplateAnnotations(eq(trDto), any(TemplateResource.class));
        doNothing().when(appointmentServiceSpy).adjustDurationUnapproveAndUpdateVisitTemplate(institution, templatePath, visitTemplate);
        doNothing().when(auditService).logTemplateResourceActivity(
                host, user, visitTemplate, Statics.AUDIT_TEMPLATE_RESOURCE_CREATE, null, null
        );

        BooleanResultDTO response = appointmentServiceSpy.crudTemplateResources(
                trDto, institution, templatePath, actionType, user, host
        );

        assertEquals(trDto, response);
        assertTrue(trDto.isResult());

        verify(studyDAO, times(2)).findVisitById(visitTemplateId);
        verify(resourceDAO, times(1)).findResourceById(resourceId);
        verify(appointmentDAO, times(1)).createEntity(any(TemplateResource.class));
        verify(appointmentServiceSpy, times(1)).createTemplateAnnotations(eq(trDto), any(TemplateResource.class));
        verify(appointmentServiceSpy, times(1)).adjustDurationUnapproveAndUpdateVisitTemplate(institution, templatePath, visitTemplate);
        verify(auditService, times(1)).logTemplateResourceActivity(
                host, user, visitTemplate, Statics.AUDIT_TEMPLATE_RESOURCE_CREATE, null, null
        );

        assertEquals("Approved", trDto.getInfoMsg());
    }


    /**
     * AppointmentService.crudTemplateResources()
     * Case 2: action type: "edit_template_resource"
     */
    @Test
    public void testCrudTemplateResourcesEdit() {

        AppointmentService appointmentServiceSpy = spy(appointmentService);

        Resource resource = new Resource();
        int resourceId = 2;
        resource.setId(resourceId);

        Study study = new Study();

        VisitTemplate visitTemplate = new VisitTemplate();
        int visitTemplateId = 3;
        visitTemplate.setId(visitTemplateId);
        visitTemplate.setApproved(true);
        visitTemplate.setStudy(study);

        TemplateResource templateResource1 = new TemplateResource();
        int templateResourceId1 = 4;
        templateResource1.setId(templateResourceId1);

        TemplateResource templateResource2 = new TemplateResource();
        int templateResourceId2 = 5;
        templateResource2.setId(templateResourceId2);

        TemplateResourceDTO trDto = new TemplateResourceDTO();
        trDto.setVisitTemplate(visitTemplateId);
        trDto.setResource(resourceId);
        trDto.setAlternate(false);
        trDto.setBillable(false);

        String institution = "institution";
        String templatePath = "template path";
        User user = new User();
        String host = "1.2.3.4";

        String actionType = "edit_template_resource";

        when(studyDAO.findVisitById(visitTemplateId)).thenReturn(visitTemplate);
        doReturn(true).when(appointmentServiceSpy).editTemplateResource(trDto, institution, templatePath, user, host, false);

        BooleanResultDTO response = appointmentServiceSpy.crudTemplateResources(
                trDto, institution, templatePath, actionType, user, host
        );

        assertEquals(trDto, response);
        assertTrue(trDto.isResult());

        verify(studyDAO, times(2)).findVisitById(visitTemplateId);
        verify(appointmentServiceSpy, times(1)).editTemplateResource(trDto, institution, templatePath, user, host, false);

        assertEquals("Approved", trDto.getInfoMsg());

    }

    /**
     * AppointmentService.crudTemplateResources()
     * Case 3: action type: "edit_template_resource_check_diff_only"
     */
    @Test
    public void testCrudTemplateResourcesEdit2() {

        AppointmentService appointmentServiceSpy = spy(appointmentService);

        Resource resource = new Resource();
        int resourceId = 2;
        resource.setId(resourceId);

        Study study = new Study();

        VisitTemplate visitTemplate = new VisitTemplate();
        int visitTemplateId = 3;
        visitTemplate.setId(visitTemplateId);
        visitTemplate.setApproved(false);
        visitTemplate.setStudy(study);

        TemplateResource templateResource1 = new TemplateResource();
        int templateResourceId1 = 4;
        templateResource1.setId(templateResourceId1);

        TemplateResource templateResource2 = new TemplateResource();
        int templateResourceId2 = 5;
        templateResource2.setId(templateResourceId2);

        TemplateResourceDTO trDto = new TemplateResourceDTO();
        trDto.setVisitTemplate(visitTemplateId);
        trDto.setResource(resourceId);
        trDto.setAlternate(false);
        trDto.setBillable(false);

        String institution = "institution";
        String templatePath = "template path";
        User user = new User();
        String host = "1.2.3.4";

        String actionType = "edit_template_resource_check_diff_only";

        when(studyDAO.findVisitById(visitTemplateId)).thenReturn(visitTemplate);
        doReturn(true).when(appointmentServiceSpy).editTemplateResource(trDto, institution, templatePath, user, host, true);

        BooleanResultDTO response = appointmentServiceSpy.crudTemplateResources(
                trDto, institution, templatePath, actionType, user, host
        );

        assertEquals(trDto, response);
        assertTrue(trDto.isResult());

        verify(studyDAO, times(2)).findVisitById(visitTemplateId);
        verify(appointmentServiceSpy, times(1)).editTemplateResource(trDto, institution, templatePath, user, host, true);

        assertEquals(true, trDto.isResult());
        assertEquals("Not Approved", trDto.getInfoMsg());

    }

    /**
     * AppointmentService.crudTemplateResources()
     * Case 3: action type: "delete_template_resource"
     */
    @Test
    public void testCrudTemplateResourcesDelete() {

        AppointmentService appointmentServiceSpy = spy(appointmentService);

        Resource resource = new Resource();
        int resourceId = 2;
        resource.setId(resourceId);

        Study study = new Study();

        VisitTemplate visitTemplate = new VisitTemplate();
        int visitTemplateId = 3;
        visitTemplate.setId(visitTemplateId);
        visitTemplate.setApproved(false);
        visitTemplate.setStudy(study);

        TemplateResource templateResource1 = new TemplateResource();
        int templateResourceId1 = 4;
        templateResource1.setId(templateResourceId1);

        TemplateResource templateResource2 = new TemplateResource();
        int templateResourceId2 = 5;
        templateResource2.setId(templateResourceId2);

        TemplateResourceDTO trDto = new TemplateResourceDTO();
        trDto.setVisitTemplate(visitTemplateId);
        trDto.setResource(resourceId);
        trDto.setAlternate(false);
        trDto.setBillable(false);

        String institution = "institution";
        String templatePath = "template path";
        User user = new User();
        String host = "1.2.3.4";

        String actionType = "delete_template_resource";

        when(studyDAO.findVisitById(visitTemplateId)).thenReturn(visitTemplate);
        doNothing().when(appointmentServiceSpy).deleteTemplateResource(trDto, institution, templatePath);
        doNothing().when(auditService).logTemplateResourceActivity(
                host, user, visitTemplate, Statics.AUDIT_TEMPLATE_RESOURCE_DELETE, null, null
        );

        BooleanResultDTO response = appointmentServiceSpy.crudTemplateResources(
                trDto, institution, templatePath, actionType, user, host
        );

        assertEquals(trDto, response);
        assertTrue(trDto.isResult());

        verify(studyDAO, times(2)).findVisitById(visitTemplateId);
        verify(appointmentServiceSpy, times(1)).deleteTemplateResource(trDto, institution, templatePath);
        verify(auditService, times(1)).logTemplateResourceActivity(host, user, visitTemplate, Statics
                .AUDIT_TEMPLATE_RESOURCE_DELETE, null, null);

    }

    @Test
    public void testCreateTemplateAnnotations() {

        AppointmentService appointmentServiceSpy = spy(appointmentService);

        Resource resource = new Resource();
        int resourceId = 2;
        resource.setId(resourceId);

        Study study = new Study();

        VisitTemplate visitTemplate = new VisitTemplate();
        int visitTemplateId = 3;
        visitTemplate.setId(visitTemplateId);
        visitTemplate.setApproved(false);
        visitTemplate.setStudy(study);

        TemplateResource templateResource = new TemplateResource();
        int templateResourceId = 4;
        templateResource.setId(templateResourceId);

        LineLevelAnnotations lla1 = new LineLevelAnnotations();
        lla1.setName("line-level annotation 1");
        lla1.setSelected(true);

        LineLevelAnnotations lla2 = new LineLevelAnnotations();
        lla2.setName("line-level annotation 2");
        lla2.setSelected(true);

        int quantity1 = 10;
        int quantity2 = 20;

        String comment1 = "comment a";
        String comment2 = "comment b";
        TemplateResourceDTO trDto = new TemplateResourceDTO();
        trDto.setSelectedAnnotations(Arrays.asList(1, 2));
        trDto.setSelectedAnnotationsQuantity(Arrays.asList(quantity1, quantity2));
        trDto.setSelectedAnnotationsComment(Arrays.asList(comment1, comment2));
        trDto.setVisitTemplate(visitTemplateId);
        trDto.setResource(resourceId);
        trDto.setAlternate(false);
        trDto.setBillable(false);

        when(resourceDAO.findLineLevelAnnotationsById(1)).thenReturn(lla1);
        when(resourceDAO.findLineLevelAnnotationsById(2)).thenReturn(lla2);
        doNothing().when(appointmentDAO).createEntity(any());

        ArgumentCaptor<TemplateResourceAnnotations> argument = ArgumentCaptor.forClass(TemplateResourceAnnotations.class);

        appointmentServiceSpy.createTemplateAnnotations(trDto, templateResource);

        verify(resourceDAO, times(1)).findLineLevelAnnotationsById(1);
        verify(resourceDAO, times(1)).findLineLevelAnnotationsById(2);
        verify(appointmentDAO, times(2)).createEntity(argument.capture());

        List<TemplateResourceAnnotations> capturedTras = argument.getAllValues();
        assertEquals(2, capturedTras.size());

        TemplateResourceAnnotations capturedTra1 = capturedTras.get(0);
        TemplateResourceAnnotations capturedTra2 = capturedTras.get(1);

        assertEquals(new Integer(quantity1), capturedTra1.getQuantity());
        assertEquals(comment1, capturedTra1.getComment());
        assertEquals(lla1, capturedTra1.getLineLevelAnnotations());
        assertEquals(templateResource, capturedTra1.getTemplateResource());

        assertEquals(new Integer(quantity2), capturedTra2.getQuantity());
        assertEquals(comment2, capturedTra2.getComment());
        assertEquals(lla2, capturedTra2.getLineLevelAnnotations());
        assertEquals(templateResource, capturedTra2.getTemplateResource());

    }

    @Test
    public void testAdjustDurationUnapproveAndUpdateVisitTemplate() {

        AppointmentService appointmentServiceSpy = spy(appointmentService);

        Resource resource = new Resource();
        int resourceId = 2;
        resource.setId(resourceId);

        Study study = new Study();

        VisitTemplate visitTemplate = new VisitTemplate();
        int visitTemplateId = 3;
        visitTemplate.setId(visitTemplateId);
        visitTemplate.setApproved(true);
        Date initialUpdateTime = new Date();
        // go back one millisecond so future dates must necessarily be
        // absolutely greater then the initial update time
        initialUpdateTime = new Date(initialUpdateTime.getTime() - 1);
        visitTemplate.setLastUpdateTime(initialUpdateTime);
        visitTemplate.setStudy(study);
        visitTemplate.setRelativeTime(true);

        TemplateResource templateResource1 = new TemplateResource();
        int templateResourceId1 = 4;
        templateResource1.setId(templateResourceId1);
        templateResource1.setVisitTemplate(visitTemplate);

        TemplateResource templateResource2 = new TemplateResource();
        int templateResourceId2 = 5;
        templateResource2.setId(templateResourceId2);
        templateResource2.setVisitTemplate(visitTemplate);

        String institution = "Institution 1";
        String templatePath = "Template 1";

        when(templateResourceDAO.findTemplateResourcesByVisit(visitTemplate))
                .thenReturn(Arrays.asList(templateResource1, templateResource2));
        doNothing().when(appointmentServiceSpy).sendVisitTemplateResourceUpdatedEmail(
                visitTemplate, institution, true, templatePath);
        doNothing().when(appointmentDAO).updateEntity(visitTemplate);

        appointmentServiceSpy.adjustDurationUnapproveAndUpdateVisitTemplate(
                institution, templatePath, visitTemplate
        );

        assertEquals(new Integer(0), visitTemplate.getDuration());
        assertEquals(false, visitTemplate.getApproved());
        verify(appointmentServiceSpy, times(1))
                .sendVisitTemplateResourceUpdatedEmail(visitTemplate, institution, true, templatePath);
        assert(initialUpdateTime.getTime() < visitTemplate.getLastUpdateTime().getTime());
        verify(appointmentDAO, times(1)).updateEntity(visitTemplate);

    }

    @Test
    public void testEditTemplateResourceWithChange() {

       executeEditTemplateResourceTest(false, true);

    }

    @Test
    public void testEditTemplateResourceDiffCheckOnlyWithChange() {

        executeEditTemplateResourceTest(true, true);

    }

    @Test
    public void testEditTemplateResourceDiffCheckOnlyNoChange() {

        executeEditTemplateResourceTest(true, false);

    }

    private void executeEditTemplateResourceTest(boolean diffCheckOnly, boolean changeTemplateResource) {

        AppointmentService appointmentServiceSpy = spy(appointmentService);

        Resource oldResource = new Resource();
        int oldResourceId = 1;
        oldResource.setId(oldResourceId);

        Resource newResource = new Resource();
        int newResourceId = 2;
        newResource.setId(newResourceId);

        Study study = new Study();

        VisitTemplate oldVisitTemplate = new VisitTemplate();
        int oldVisitTemplateId = 3;
        oldVisitTemplate.setId(oldVisitTemplateId);
        oldVisitTemplate.setApproved(false);
        oldVisitTemplate.setStudy(study);

        int templateResourceId = 5;
        boolean oldBillable = false;
        boolean oldAlternate = false;
        int oldFloatStart = 10;
        int oldFloatEnd = 20;
        boolean oldFloatable = false;
        boolean oldFlexible = false;
        int oldStartMinutes = 30;
        int oldEndMinutes = 60;

        TemplateResource templateResource = new TemplateResource();
        templateResource.setId(templateResourceId);
        templateResource.setVisitTemplate(oldVisitTemplate);
        templateResource.setResource(oldResource);
        templateResource.setBillable(oldBillable);
        templateResource.setAlternate(oldAlternate);
        templateResource.setFloatStart(oldFloatStart);
        templateResource.setFloatEnd(oldFloatEnd);
        templateResource.setFloatable(oldFloatable);
        templateResource.setFlexible(oldFlexible);
        templateResource.setStartMinutes(oldStartMinutes);
        templateResource.setEndMinutes(oldEndMinutes);

        int annotationId1 = 8;
        int annotationId2 = 9;
        String annotationComment1 = "line-level annotation 1";
        String annotationComment2 = "line-level annotation 2";

        LineLevelAnnotations lla1 = new LineLevelAnnotations();
        lla1.setId(annotationId1);
        lla1.setName(annotationComment1);
        lla1.setSelected(true);

        LineLevelAnnotations lla2 = new LineLevelAnnotations();
        lla2.setId(annotationId2);
        lla2.setName(annotationComment2);
        lla2.setSelected(true);

        int quantity1 = 10;
        int quantity2 = 20;
        String comment1 = "comment a";
        String comment2 = "comment b";
        int newVisitTemplateId = 4;
        boolean newBillable = true;
        boolean newAlternate = true;
        int newFloatStart = 30;
        int newFloatEnd = 40;
        String newFloatable = "true";
        String newFlexible = "true";
        int newStartMinutes = 50;
        int newEndMinutes = 90;

        TemplateResourceDTO newTrDto = new TemplateResourceDTO();
        if (changeTemplateResource) {
            newTrDto.setId(templateResourceId);
            newTrDto.setVisitTemplate(newVisitTemplateId);
            newTrDto.setSelectedAnnotations(Arrays.asList(annotationId1, annotationId2));
            newTrDto.setSelectedAnnotationsQuantity(Arrays.asList(quantity1, quantity2));
            newTrDto.setSelectedAnnotationsComment(Arrays.asList(comment1, comment2));
            newTrDto.setResource(newResourceId);
            newTrDto.setFloatStart(newFloatStart);
            newTrDto.setFloatEnd(newFloatEnd);
            newTrDto.setFloatable(newFloatable);
            newTrDto.setAlternate(newAlternate);
            newTrDto.setBillable(newBillable);
            newTrDto.setFlexible(newFlexible);
            newTrDto.setStartMinutes(newStartMinutes);
            newTrDto.setEndMinutes(newEndMinutes);
        }
        else {
            newTrDto.setId(templateResourceId);
            newTrDto.setVisitTemplate(oldVisitTemplateId);
            newTrDto.setSelectedAnnotations(Arrays.asList());
            newTrDto.setSelectedAnnotationsQuantity(Arrays.asList());
            newTrDto.setSelectedAnnotationsComment(Arrays.asList());
            newTrDto.setResource(oldResourceId);
            newTrDto.setFloatStart(oldFloatStart);
            newTrDto.setFloatEnd(oldFloatEnd);
            newTrDto.setFloatable("" + oldFloatable);
            newTrDto.setAlternate(oldAlternate);
            newTrDto.setBillable(oldBillable);
            newTrDto.setFlexible("" + oldFlexible);
            newTrDto.setStartMinutes(oldStartMinutes);
            newTrDto.setEndMinutes(oldEndMinutes);
        }

        String institution = "Institution 1";
        String templatePath = "Template 1";
        User user = new User();
        String host = "1.2.3.4";
        String updateString = "{visitTemplate: " + oldVisitTemplateId + " --> " + newVisitTemplateId + "}" +
                "{resource: " + oldResourceId + " --> " + newResourceId + "}" +
                "{billable: " + oldBillable + " --> " + newBillable + "}" +
                "{alternate: " + oldAlternate + " --> " + newAlternate + "}" +
                "{floatStart: " + oldFloatStart + " --> " + newFloatStart + "}" +
                "{floatEnd: " + oldFloatEnd + " --> " + newFloatEnd + "}" +
                "{floatable: " + oldFloatable + " --> " + newFloatable + "}" +
                "{flexible: " + oldFlexible + " --> " + newFlexible + "}" +
                "{startMinutes: " + oldStartMinutes + " --> " + newStartMinutes + "}" +
                "{endMinutes: " + oldEndMinutes + " --> " + newEndMinutes + "}" +
                "{annotations:  --> " +
                "{id: " + annotationId1 + ". comment: " + comment1 + ". quantity: " + quantity1 + "}," +
                "{id: " + annotationId2 + ". comment: " + comment2 + ". quantity: " + quantity2 + "}" +
                "}";

        when(appointmentDAO.findTemplateResourceById(templateResourceId)).thenReturn(templateResource);
        doNothing().when(appointmentDAO).updateEntity(templateResource);
        doNothing().when(auditService).logTemplateResourceActivity(host, user, oldVisitTemplate,
                Statics.AUDIT_TEMPLATE_RESOURCE_UPDATE, updateString, null
        );
        when(resourceDAO.findLineLevelAnnotationsById(annotationId1)).thenReturn(lla1);
        when(resourceDAO.findLineLevelAnnotationsById(annotationId2)).thenReturn(lla2);
        when(resourceDAO.findResourceById(oldResourceId)).thenReturn(oldResource);
        when(resourceDAO.findResourceById(newResourceId)).thenReturn(newResource);

        // method under test
        boolean response = appointmentServiceSpy.editTemplateResource(newTrDto, institution, templatePath, user, host, diffCheckOnly);

        if (diffCheckOnly) {
            assertEquals(changeTemplateResource, response);
            verify(appointmentDAO, times(1)).findTemplateResourceById(templateResourceId);
            verify(appointmentDAO, times(0)).updateEntity(templateResource);
            verify(auditService, times(0)).logTemplateResourceActivity(host, user, oldVisitTemplate,
                    Statics.AUDIT_TEMPLATE_RESOURCE_UPDATE, updateString, null
            );
            assertEquals(new Integer(templateResourceId), templateResource.getId());
            assertEquals(oldResource, templateResource.getResource());
            assertEquals(oldBillable, templateResource.getBillable());
            assertEquals(oldAlternate, templateResource.getAlternate());
            assertEquals(new Integer(oldFloatStart), templateResource.getFloatStart());
            assertEquals(new Integer(oldFloatEnd), templateResource.getFloatEnd());
            assertEquals(Boolean.parseBoolean("" + oldFloatable), templateResource.getFloatable());
            assertEquals(Boolean.parseBoolean("" + oldFlexible), templateResource.getFlexible());
            assertEquals(new Integer(oldStartMinutes), templateResource.getStartMinutes());
            assertEquals(new Integer(oldEndMinutes), templateResource.getEndMinutes());
        }
        else {
            assertTrue(response);
            verify(appointmentDAO, times(1)).findTemplateResourceById(templateResourceId);
            verify(appointmentDAO, times(1)).updateEntity(templateResource);
            verify(auditService, times(1)).logTemplateResourceActivity(host, user, oldVisitTemplate,
                    Statics.AUDIT_TEMPLATE_RESOURCE_UPDATE, updateString, null
            );
            assertEquals(new Integer(templateResourceId), templateResource.getId());
            assertEquals(newResource, templateResource.getResource());
            assertEquals(newBillable, templateResource.getBillable());
            assertEquals(newAlternate, templateResource.getAlternate());
            assertEquals(new Integer(newFloatStart), templateResource.getFloatStart());
            assertEquals(new Integer(newFloatEnd), templateResource.getFloatEnd());
            assertEquals(Boolean.parseBoolean(newFloatable), templateResource.getFloatable());
            assertEquals(Boolean.parseBoolean(newFlexible), templateResource.getFlexible());
            assertEquals(new Integer(newStartMinutes), templateResource.getStartMinutes());
            assertEquals(new Integer(newEndMinutes), templateResource.getEndMinutes());

        }

    }

    @Test
    public void testPossiblyUpdateThisTrsResourceField() {

        AppointmentService appointmentServiceSpy = spy(appointmentService);

        Resource resource1 = new Resource();
        resource1.setId(2);

        int resourceId2 = 3;
        Resource resource2 = new Resource();
        resource2.setId(resourceId2);

        TemplateResource templateResource = new TemplateResource();
        templateResource.setId(1);
        templateResource.setResource(resource1);

        TemplateResourceDTO dto = new TemplateResourceDTO();
        dto.setId(4);
        dto.setResource(resourceId2);

        TemplateResourceAnnotations tra1 = new TemplateResourceAnnotations();
        tra1.setId(99);
        TemplateResourceAnnotations tra2 = new TemplateResourceAnnotations();
        tra2.setId(88);
        List<TemplateResourceAnnotations> tras = Arrays.asList(tra1, tra2);

        when(resourceDAO.findTemplateAnnotationsByTemplateResource(templateResource)).thenReturn(tras);
        doNothing().when(appointmentDAO).deleteEntity(tra1);
        doNothing().when(appointmentDAO).deleteEntity(tra2);
        when(resourceDAO.findResourceById(resourceId2)).thenReturn(resource2);

        // method under test
        boolean returnValue = appointmentServiceSpy.possiblyUpdateThisTrsResourceField(dto, templateResource);

        assertTrue(returnValue);

        verify(resourceDAO, times(1)).findTemplateAnnotationsByTemplateResource(templateResource);
        verify(appointmentDAO, times(1)).deleteEntity(tra1);
        verify(appointmentDAO, times(1)).deleteEntity(tra2);
        verify(resourceDAO, times(1)).findResourceById(resourceId2);


    }

    @Test
    public void testDeleteTemplateResource() {

        AppointmentService appointmentServiceSpy = spy(appointmentService);

        Resource resource = new Resource();
        int resourceId = 1;
        resource.setId(resourceId);

        Study study = new Study();

        VisitTemplate visitTemplate = new VisitTemplate();
        int visitTemplateId = 3;
        visitTemplate.setId(visitTemplateId);
        visitTemplate.setApproved(false);
        visitTemplate.setStudy(study);

        int templateResourceId1 = 5;
        int templateGroupId1 = 6;
        String templateResourceGroupHexId1 = "abcdef111111";
        TemplateResource templateResource1 = new TemplateResource();
        templateResource1.setId(templateResourceId1);
        templateResource1.setVisitTemplate(visitTemplate);
        templateResource1.setResource(resource);
        templateResource1.setGroupId(templateResourceGroupHexId1);
        templateResource1.setFloatable(false);
        templateResource1.setStartMinutes(11);
        templateResource1.setEndMinutes(22);

        int templateResourceId2 = 7;
        int templateGroupId2 = 8;
        String templateResourceGroupHexId2 = "abcdef222222";
        TemplateResource templateResource2 = new TemplateResource();
        templateResource2.setId(templateResourceId2);
        templateResource2.setVisitTemplate(visitTemplate);
        templateResource2.setResource(resource);
        templateResource2.setGroupId(templateResourceGroupHexId2);
        templateResource2.setStartMinutes(22);
        templateResource2.setEndMinutes(33);

        int templateResourceId3 = 9;
        int templateGroupId3 = 10;
        String templateResourceGroupHexId3 = "abcdef333333";
        TemplateResource templateResource3 = new TemplateResource();
        templateResource3.setId(templateResourceId3);
        templateResource3.setVisitTemplate(visitTemplate);
        templateResource3.setResource(resource);
        templateResource3.setGroupId(templateResourceGroupHexId3);
        templateResource3.setStartMinutes(33);
        templateResource3.setEndMinutes(44);

        TemplateResourceGroup trg1 = new TemplateResourceGroup();
        trg1.setId(templateGroupId1);
        trg1.setGroupId(templateResourceGroupHexId1);
        trg1.setTemplateResource(templateResource1);

        TemplateResourceGroup trg2 = new TemplateResourceGroup();
        trg2.setId(templateGroupId2);
        trg2.setGroupId(templateResourceGroupHexId2);
        trg2.setTemplateResource(templateResource2);

        TemplateResourceGroup trg3 = new TemplateResourceGroup();
        trg3.setId(templateGroupId3);
        trg3.setGroupId(templateResourceGroupHexId3);
        trg3.setTemplateResource(templateResource3);

        List<TemplateResourceGroup> trgs = Arrays.asList(trg1, trg2, trg3);

        TemplateResourceDTO dto = new TemplateResourceDTO();
        dto.setId(templateResourceId1);

        String institution = "Institution 1";
        String templatePath = "Template 1";

        when(appointmentDAO.findTemplateResourceById(templateResourceId1)).thenReturn(templateResource1);
        when(appointmentDAO.findBookedResourcesByBookedVisit(templateResource1)).thenReturn(false);
        when(appointmentDAO.findTrgListByGroupId(templateResourceGroupHexId1)).thenReturn(trgs);

        appointmentServiceSpy.deleteTemplateResource(dto, institution, templatePath);

        assertFalse(dto.isResult());
        assertNull(dto.getErrorMsg());
        assertNull(dto.getInfoMsg());

        verify(appointmentDAO, times(1)).findTemplateResourceById(templateResourceId1);
        verify(appointmentDAO, times(1)).findBookedResourcesByBookedVisit(templateResource1);
        verify(appointmentDAO, times(1)).findTrgListByGroupId(templateResourceGroupHexId1);

    }

    @Test
    public void testDeleteTemplateResourceGroups() {

        AppointmentService appointmentServiceSpy = spy(appointmentService);

        int templateResourceId1 = 1;
        TemplateResource templateResource1 = new TemplateResource();
        templateResource1.setId(templateResourceId1);
        templateResource1.setGroupId("12345abcde");

        int templateResourceId2 = 3;
        TemplateResource templateResource2 = new TemplateResource();
        templateResource2.setId(templateResourceId2);
        templateResource2.setGroupId("abcdef012345");

        int templateGroupId1 = 6;
        TemplateResourceGroup trg1 = new TemplateResourceGroup();
        trg1.setId(templateGroupId1);
        trg1.setTemplateResource(templateResource1);

        int templateGroupId2 = 7;
        TemplateResourceGroup trg2 = new TemplateResourceGroup();
        trg2.setId(templateGroupId2);
        trg2.setTemplateResource(templateResource2);

        List<TemplateResourceGroup> trgs = Arrays.asList(trg1, trg2);

        doNothing().when(appointmentDAO).updateEntity(any());
        doNothing().when(appointmentDAO).deleteEntity(any());
        when(templateResourceDAO.findTemplateResourceById(templateResourceId1)).thenReturn(templateResource1);
        when(templateResourceDAO.findTemplateResourceById(templateResourceId2)).thenReturn(templateResource2);

        appointmentServiceSpy.deleteTemplateResourceGroups(templateResource1, trgs);

        assertNull(templateResource1.getGroupId());
        assertNull(templateResource2.getGroupId());

        verify(appointmentDAO, times(1)).updateEntity(templateResource1);
        verify(appointmentDAO, times(1)).updateEntity(templateResource2);
        verify(appointmentDAO, times(1)).deleteEntity(trg1);
        verify(appointmentDAO, times(1)).deleteEntity(trg2);
        verify(appointmentDAO, times(1)).deleteEntity(templateResource1);

    }

    @Test
    public void testUpdateTemplateResourceTimeFloatable() {

        executeTestUpdateTemplateResourceTime(true);

    }

    @Test
    public void testUpdateTemplateResourceTimeNotFloatable() {

        executeTestUpdateTemplateResourceTime(false);

    }

    private void executeTestUpdateTemplateResourceTime(boolean floatable) {

        AppointmentService appointmentServiceSpy = spy(appointmentService);

        VisitTemplate visitTemplate = new VisitTemplate();
        int visitTemplateId = 3;
        visitTemplate.setId(visitTemplateId);
        visitTemplate.setApproved(true);
        visitTemplate.setRelativeTime(true);

        int templateResourceId1 = 1;
        int oldStartMinutes = 100;
        int oldEndMinutes = 200;
        TemplateResource templateResource1 = new TemplateResource();
        templateResource1.setId(templateResourceId1);
        templateResource1.setStartMinutes(oldStartMinutes);
        templateResource1.setEndMinutes(oldEndMinutes);
        templateResource1.setFloatable(floatable);
        templateResource1.setVisitTemplate(visitTemplate);
        long lastUpdateTime = (new Date()).getTime() - 1;
        templateResource1.setLastUpdateTime(new Date(lastUpdateTime));

        int templateResourceId2 = 2;
        TemplateResource templateResource2 = new TemplateResource();
        templateResource2.setId(templateResourceId2);
        templateResource2.setStartMinutes(50);
        templateResource2.setEndMinutes(150);
        templateResource2.setFloatable(floatable);
        templateResource2.setVisitTemplate(visitTemplate);

        List<TemplateResource> templateResources = Arrays.asList(templateResource1, templateResource2);

        int newStartMinutes = 10;
        int newEndMinutes = 630;

        when(appointmentDAO.findTemplateResourceById(templateResourceId1)).thenReturn(templateResource1);
        when(studyDAO.findVisitTemplateById(visitTemplateId)).thenReturn(visitTemplate);
        doNothing().when(appointmentDAO).updateEntity(templateResource1);
        when(templateResourceDAO.findTemplateResourcesByVisit(visitTemplate)).thenReturn(templateResources);
        doNothing().when(appointmentDAO).updateEntity(visitTemplate);

        // method under test
        BooleanResultDTO result = appointmentServiceSpy.updateTemplateResourceTime(templateResourceId1, newStartMinutes, newEndMinutes);

        assertTrue(result.isResult());
        if (floatable) {
            assertEquals(new Integer(newStartMinutes), templateResource1.getFloatStart());
            assertEquals(new Integer(newEndMinutes), templateResource1.getFloatEnd());
            assertEquals(new Integer(oldStartMinutes), templateResource1.getStartMinutes());
            assertEquals(new Integer(oldEndMinutes), templateResource1.getEndMinutes());
            assertEquals(null, templateResource1.getDuration());
            assertEquals(new Integer(oldEndMinutes), visitTemplate.getDuration());
        }
        else {
            assertEquals(null, templateResource1.getFloatStart());
            assertEquals(null, templateResource1.getFloatEnd());
            assertEquals(new Integer(newStartMinutes), templateResource1.getStartMinutes());
            assertEquals(new Integer(newEndMinutes), templateResource1.getEndMinutes());
            assertEquals(new Integer(newEndMinutes - newStartMinutes), templateResource1.getDuration());
            assertEquals(new Integer(newEndMinutes), visitTemplate.getDuration());
        }
        assertFalse(visitTemplate.getApproved());
        assertTrue(visitTemplate.getLastUpdateTime().getTime() > lastUpdateTime);

        verify(appointmentDAO, times(1)).findTemplateResourceById(templateResourceId1);
        verify(studyDAO, times(1)).findVisitTemplateById(visitTemplateId);
        verify(appointmentDAO, times(1)).updateEntity(templateResource1);
        verify(templateResourceDAO, times(1)).findTemplateResourcesByVisit(visitTemplate);
        verify(appointmentDAO, times(1)).updateEntity(visitTemplate);


    }

    @Test
    public void testCopyGroupResources() {

        AppointmentService appointmentServiceSpy = spy(appointmentService);

        TemplateResource tr1 = new TemplateResource();
        tr1.setId(1);
        tr1.setFloatable(false);
        tr1.setFlexible(false);
        tr1.setStartMinutes(10);
        tr1.setEndMinutes(11);

        TemplateResource tr2 = new TemplateResource();
        tr2.setId(2);
        tr2.setStartMinutes(20);
        tr2.setFlexible(false);
        tr2.setEndMinutes(21);
        tr2.setFloatable(true);
        tr2.setFloatStart(200);
        tr2.setFloatEnd(210);

        TemplateResource tr3 = new TemplateResource();
        tr3.setId(3);
        tr3.setStartMinutes(30);
        tr3.setFlexible(true);
        tr3.setEndMinutes(31);
        tr3.setFloatable(false);

        TemplateResourceAnnotations tra1_1 = new TemplateResourceAnnotations();
        tra1_1.setId(11);
        TemplateResourceAnnotations tra1_2 = new TemplateResourceAnnotations();
        tra1_2.setId(12);
        TemplateResourceAnnotations tra2_1 = new TemplateResourceAnnotations();
        tra2_1.setId(21);
        TemplateResourceAnnotations tra2_2 = new TemplateResourceAnnotations();
        tra2_2.setId(22);
        TemplateResourceAnnotations tra3_1 = new TemplateResourceAnnotations();
        tra3_1.setId(31);
        TemplateResourceAnnotations tra3_2 = new TemplateResourceAnnotations();
        tra3_2.setId(32);

        List<TemplateResourceAnnotations> tras1 = Arrays.asList(tra1_1, tra1_2);
        List<TemplateResourceAnnotations> tras2 = Arrays.asList(tra2_1, tra2_2);
        List<TemplateResourceAnnotations> tras3 = Arrays.asList(tra3_1, tra3_2);

        int currentDay = 1;
        int targetDayOffset1 = 3;
        int targetDayOffset2 = 6;
        List<Integer> targetDayOffsets = Arrays.asList(targetDayOffset1, targetDayOffset2);
        List<TemplateResource> theGroupsTrgListFromThisDay = Arrays.asList(tr1, tr2, tr3);

        when(templateResourceDAO.findTemplateResourceAnnotationsByTemplateResource(tr1)).thenReturn(tras1);
        when(templateResourceDAO.findTemplateResourceAnnotationsByTemplateResource(tr2)).thenReturn(tras2);
        when(templateResourceDAO.findTemplateResourceAnnotationsByTemplateResource(tr3)).thenReturn(tras3);
        doNothing().when(appointmentServiceSpy).copyTemplateAnnotations(eq(tras1), any());
        doNothing().when(appointmentServiceSpy).copyTemplateAnnotations(eq(tras2), any());
        doNothing().when(appointmentServiceSpy).copyTemplateAnnotations(eq(tras3), any());

        doNothing().when(appointmentDAO).createEntity(any());
        doNothing().when(studyDAO).createEntity(any());

        ArgumentCaptor<TemplateResourceGroup> templateResourceGroupArgumentCaptor = ArgumentCaptor.forClass(TemplateResourceGroup.class);
        ArgumentCaptor<TemplateResource> templateResourceArgumentCaptor = ArgumentCaptor.forClass(TemplateResource.class);

        // method under test
        appointmentServiceSpy.copyGroupResources(currentDay, targetDayOffsets, theGroupsTrgListFromThisDay);

        verify(templateResourceDAO, times(2)).findTemplateResourceAnnotationsByTemplateResource(tr1);
        verify(templateResourceDAO, times(2)).findTemplateResourceAnnotationsByTemplateResource(tr2);
        verify(templateResourceDAO, times(2)).findTemplateResourceAnnotationsByTemplateResource(tr3);
        verify(appointmentServiceSpy, times(2)).copyTemplateAnnotations(eq(tras1), any());
        verify(appointmentServiceSpy, times(2)).copyTemplateAnnotations(eq(tras2), any());
        verify(appointmentServiceSpy, times(2)).copyTemplateAnnotations(eq(tras3), any());

        verify(studyDAO, times(6)).createEntity(templateResourceArgumentCaptor.capture());

        List<TemplateResource> capturedTrs = templateResourceArgumentCaptor.getAllValues();
        assertEquals(6, capturedTrs.size());

        TemplateResource capturedTr1 = capturedTrs.get(0);
        TemplateResource capturedTr2 = capturedTrs.get(1);
        TemplateResource capturedTr3 = capturedTrs.get(2);
        TemplateResource capturedTr4 = capturedTrs.get(3);
        TemplateResource capturedTr5 = capturedTrs.get(4);
        TemplateResource capturedTr6 = capturedTrs.get(5);

        verify(appointmentDAO, times(6)).createEntity(templateResourceGroupArgumentCaptor.capture());

        List<TemplateResourceGroup> capturedTrgs = templateResourceGroupArgumentCaptor.getAllValues();
        assertEquals(6, capturedTrgs.size());

        TemplateResourceGroup capturedTrg1 = capturedTrgs.get(0);
        TemplateResourceGroup capturedTrg2 = capturedTrgs.get(1);
        TemplateResourceGroup capturedTrg3 = capturedTrgs.get(2);
        TemplateResourceGroup capturedTrg4 = capturedTrgs.get(3);
        TemplateResourceGroup capturedTrg5 = capturedTrgs.get(4);
        TemplateResourceGroup capturedTrg6 = capturedTrgs.get(5);

        assertEquals(capturedTr1, capturedTrg1.getTemplateResource());
        assertEquals(capturedTr2, capturedTrg2.getTemplateResource());
        assertEquals(capturedTr3, capturedTrg3.getTemplateResource());
        assertEquals(capturedTr4, capturedTrg4.getTemplateResource());
        assertEquals(capturedTr5, capturedTrg5.getTemplateResource());
        assertEquals(capturedTr6, capturedTrg6.getTemplateResource());

        assertEquals(tr1.getFlexible(), capturedTrg1.isFlexGroup());
        assertEquals(tr2.getFlexible(), capturedTrg2.isFlexGroup());
        assertEquals(tr3.getFlexible(), capturedTrg3.isFlexGroup());
        assertEquals(tr1.getFlexible(), capturedTrg4.isFlexGroup());
        assertEquals(tr2.getFlexible(), capturedTrg5.isFlexGroup());
        assertEquals(tr3.getFlexible(), capturedTrg6.isFlexGroup());

        assertEquals(tr1.getVisitTemplate(), capturedTrg1.getVisit());
        assertEquals(tr2.getVisitTemplate(), capturedTrg2.getVisit());
        assertEquals(tr3.getVisitTemplate(), capturedTrg3.getVisit());
        assertEquals(tr1.getVisitTemplate(), capturedTrg4.getVisit());
        assertEquals(tr2.getVisitTemplate(), capturedTrg5.getVisit());
        assertEquals(tr3.getVisitTemplate(), capturedTrg6.getVisit());

        int minutesDelta1 = (targetDayOffset1 - currentDay) * DateUtility.MINS_PER_DAY;
        int minutesDelta2 = (targetDayOffset2 - currentDay) * DateUtility.MINS_PER_DAY;

        assertEquals(new Integer(tr1.getStartMinutes() + minutesDelta1), capturedTr1.getStartMinutes());
        assertEquals(new Integer(tr1.getEndMinutes() + minutesDelta1), capturedTr1.getEndMinutes());
        assertEquals(new Integer(tr2.getStartMinutes() + minutesDelta1), capturedTr2.getStartMinutes());
        assertEquals(new Integer(tr2.getEndMinutes() + minutesDelta1), capturedTr2.getEndMinutes());
        assertEquals(new Integer(tr3.getStartMinutes() + minutesDelta1), capturedTr3.getStartMinutes());
        assertEquals(new Integer(tr3.getEndMinutes() + minutesDelta1), capturedTr3.getEndMinutes());

        assertEquals(new Integer(tr2.getFloatStart() + minutesDelta1), capturedTr2.getFloatStart());
        assertEquals(new Integer(tr2.getFloatEnd() + minutesDelta1), capturedTr2.getFloatEnd());

        assertEquals(new Integer(tr1.getStartMinutes() + minutesDelta2), capturedTr4.getStartMinutes());
        assertEquals(new Integer(tr1.getEndMinutes() + minutesDelta2), capturedTr4.getEndMinutes());
        assertEquals(new Integer(tr2.getStartMinutes() + minutesDelta2), capturedTr5.getStartMinutes());
        assertEquals(new Integer(tr2.getEndMinutes() + minutesDelta2), capturedTr5.getEndMinutes());
        assertEquals(new Integer(tr3.getStartMinutes() + minutesDelta2), capturedTr6.getStartMinutes());
        assertEquals(new Integer(tr3.getEndMinutes() + minutesDelta2), capturedTr6.getEndMinutes());

        assertEquals(new Integer(tr2.getFloatStart() + minutesDelta2), capturedTr5.getFloatStart());
        assertEquals(new Integer(tr2.getFloatEnd() + minutesDelta2), capturedTr5.getFloatEnd());

    }

    @Test
    public void testCopyNonGroupTrs() {

        AppointmentService appointmentServiceSpy = spy(appointmentService);

        TemplateResource tr1 = new TemplateResource();
        tr1.setId(1);
        tr1.setFloatable(false);
        tr1.setFlexible(false);
        tr1.setStartMinutes(10);
        tr1.setEndMinutes(11);

        TemplateResource tr2 = new TemplateResource();
        tr2.setId(2);
        tr2.setStartMinutes(20);
        tr2.setFlexible(false);
        tr2.setEndMinutes(21);
        tr2.setFloatable(false);

        TemplateResourceAnnotations tra1_1 = new TemplateResourceAnnotations();
        TemplateResourceAnnotations tra1_2 = new TemplateResourceAnnotations();
        TemplateResourceAnnotations tra2_1 = new TemplateResourceAnnotations();
        TemplateResourceAnnotations tra2_2 = new TemplateResourceAnnotations();

        List<TemplateResourceAnnotations> tras1 = Arrays.asList(tra1_1, tra1_2);
        List<TemplateResourceAnnotations> tras2 = Arrays.asList(tra2_1, tra2_2);

        List<TemplateResource> templateResources = Arrays.asList(tr1, tr2);

        int targetOffset1 = 3;
        int targetOffset2 = 5;
        int currentDay = 1;

        List<Integer> targetOffsets = Arrays.asList(targetOffset1, targetOffset2);

        TemplateResource clonedTr1_1 = new TemplateResource();
        clonedTr1_1.setId(11);
        TemplateResource clonedTr1_2 = new TemplateResource();
        clonedTr1_2.setId(12);
        TemplateResource clonedTr2_1 = new TemplateResource();
        clonedTr2_1.setId(21);
        TemplateResource clonedTr2_2 = new TemplateResource();
        clonedTr2_2.setId(22);

        when(templateResourceDAO.findTemplateResourceAnnotationsByTemplateResource(tr1)).thenReturn(tras1);
        when(templateResourceDAO.findTemplateResourceAnnotationsByTemplateResource(tr2)).thenReturn(tras2);

        when(appointmentServiceSpy.copyTemplateResourceMostOfTheWay(currentDay, tr1, targetOffset1)).thenReturn(clonedTr1_1);
        when(appointmentServiceSpy.copyTemplateResourceMostOfTheWay(currentDay, tr1, targetOffset2)).thenReturn(clonedTr1_2);
        when(appointmentServiceSpy.copyTemplateResourceMostOfTheWay(currentDay, tr2, targetOffset1)).thenReturn(clonedTr2_1);
        when(appointmentServiceSpy.copyTemplateResourceMostOfTheWay(currentDay, tr2, targetOffset2)).thenReturn(clonedTr2_2);

        doNothing().when(appointmentServiceSpy).copyTemplateResourceFinalSteps(tras1, clonedTr1_1);
        doNothing().when(appointmentServiceSpy).copyTemplateResourceFinalSteps(tras1, clonedTr1_2);
        doNothing().when(appointmentServiceSpy).copyTemplateResourceFinalSteps(tras2, clonedTr2_1);
        doNothing().when(appointmentServiceSpy).copyTemplateResourceFinalSteps(tras2, clonedTr2_2);

        // method under test
        appointmentServiceSpy.copyNonGroupTrs(1, templateResources, targetOffsets);

        verify(templateResourceDAO, times(1)).findTemplateResourceAnnotationsByTemplateResource(tr1);
        verify(templateResourceDAO, times(1)).findTemplateResourceAnnotationsByTemplateResource(tr2);

        verify(appointmentServiceSpy, times(1)).copyTemplateResourceMostOfTheWay(currentDay, tr1, targetOffset1);
        verify(appointmentServiceSpy, times(1)).copyTemplateResourceMostOfTheWay(currentDay, tr1, targetOffset2);
        verify(appointmentServiceSpy, times(1)).copyTemplateResourceMostOfTheWay(currentDay, tr2, targetOffset1);
        verify(appointmentServiceSpy, times(1)).copyTemplateResourceMostOfTheWay(currentDay, tr2, targetOffset2);

        verify(appointmentServiceSpy).copyTemplateResourceFinalSteps(tras1, clonedTr1_1);
        verify(appointmentServiceSpy).copyTemplateResourceFinalSteps(tras1, clonedTr1_2);
        verify(appointmentServiceSpy).copyTemplateResourceFinalSteps(tras2, clonedTr2_1);
        verify(appointmentServiceSpy).copyTemplateResourceFinalSteps(tras2, clonedTr2_2);

        // In this test, all the action is in the mocked methods, so there is nothing much to assert
        // once the method calls have been verified

    }

    @Test
    public void testStudyServiceDeleteTemplateResource() {

        StudyService studyServiceSpy = spy(studyService);

        VisitTemplate visitTemplate = new VisitTemplate();

        TemplateResource templateResource = new TemplateResource();
        templateResource.setVisitTemplate(visitTemplate);

        TemplateResourceAnnotations tra1 = new TemplateResourceAnnotations();
        tra1.setId(11);
        TemplateResourceAnnotations tra2 = new TemplateResourceAnnotations();
        tra2.setId(12);
        List<TemplateResourceAnnotations> annotations = Arrays.asList(tra1, tra2);

        TemplateResourceGroup trg1 = new TemplateResourceGroup();
        trg1.setId(99);
        TemplateResourceGroup trg2 = new TemplateResourceGroup();
        trg2.setId(88);
        List<TemplateResourceGroup> templateResourceGroups = Arrays.asList(trg1, trg2);

        when(templateResourceDAO.findTemplateResourceAnnotationsByTemplateResource(templateResource)).thenReturn(annotations);
        when(templateResourceDAO.findTemplateResourceGroupByVisitAndTemplate(visitTemplate, templateResource)).thenReturn(templateResourceGroups);

        // method under test
        studyServiceSpy.deleteTemplateResource(visitTemplate, templateResource);

        verify(studyDAO, times(1)).deleteEntity(trg1);
        verify(studyDAO, times(1)).deleteEntity(trg2);

        verify(studyDAO, times(1)).deleteEntity(tra1);
        verify(studyDAO, times(1)).deleteEntity(tra2);

        verify(studyDAO, times(1)).deleteEntity(templateResource);

    }

}
