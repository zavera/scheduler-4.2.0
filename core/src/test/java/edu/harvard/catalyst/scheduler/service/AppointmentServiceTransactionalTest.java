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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import edu.harvard.catalyst.scheduler.AbstractSpringWiredHibernateTest;
import edu.harvard.catalyst.scheduler.dto.BooleanResultDTO;
import edu.harvard.catalyst.scheduler.dto.GetTemplateResourceGroupDTO;
import edu.harvard.catalyst.scheduler.dto.VisitSpecsDTO;
import edu.harvard.catalyst.scheduler.dto.response.TemplateResourceWithLlaListDTO;
import edu.harvard.catalyst.scheduler.entity.*;
import edu.harvard.catalyst.scheduler.persistence.StudyDAO;
import edu.harvard.catalyst.scheduler.util.DateUtility;
import edu.harvard.catalyst.scheduler.util.dbpopulator.DbPopulator;
import org.hibernate.query.Query;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;
import java.util.stream.Collectors;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;

/**
 * Created by xavier on 8/2/17.
 */
public class AppointmentServiceTransactionalTest extends AbstractSpringWiredHibernateTest {

    @Autowired
    StudyDAO dao;

    @Autowired
    AppointmentService appointmentService;

    /**
     * Context: There exists a group of template resources. One of them just got
     * deleted. For the purpose of refreshing the UI, we need to know if the
     * other members of the same group are still linkable.
     */
    @Test
    public void testCheckIfResourcesLinkableOnDeleteUnlink() {

        DbPopulator populator = new DbPopulator(dao);

        VisitType visitType = populator.visitTypePopulator.populateOne(1);

        VisitTemplate visitTemplate = populator.visitTemplatePopulator.populateOne(
                1,
                null,
                visitType,
                false,
                false,
                false
        );

        Resource resource = populator.resourcePopulator.populateOne(1);

        String templateResourceGroupId = "group id 1";
        TemplateResource templateResource1 = populator.templateResourcePopulator.populateOne(
                templateResourceGroupId,
                visitTemplate,
                resource,
                0,
                10,
                null,
                null,
                false,
                false,
                false
        );
        TemplateResource templateResource2 = populator.templateResourcePopulator.populateOne(
                templateResourceGroupId,
                visitTemplate,
                resource,
                10,
                20,
                null,
                null,
                false,
                false,
                false
        );
        TemplateResource templateResource3 = populator.templateResourcePopulator.populateOne(
                templateResourceGroupId,
                visitTemplate,
                resource,
                0,
                10,
                null,
                null,
                false,
                false,
                false
        );

        TemplateResourceGroup templateResourceGroup1 = populator.templateResourceGroupPopulator.populateOne(
                1,
                templateResourceGroupId,
                templateResource1,
                visitTemplate
        );
        TemplateResourceGroup templateResourceGroup2 = populator.templateResourceGroupPopulator.populateOne(
                2,
                templateResourceGroupId,
                templateResource2,
                visitTemplate
        );
        TemplateResourceGroup templateResourceGroup3 = populator.templateResourceGroupPopulator.populateOne(
                3,
                templateResourceGroupId,
                templateResource3,
                visitTemplate
        );

        // method under test, case 1
        BooleanResultDTO result = appointmentService.checkIfResourcesLinkableOnDeleteUnlink(
                templateResource1.getId()
        );

        assertEquals(true, result.isResult());

        templateResource3.setStartMinutes(10);
        templateResource3.setEndMinutes(20);

        dao.updateEntity(templateResource3);

        // method under test, case 2
        result = appointmentService.checkIfResourcesLinkableOnDeleteUnlink(
                templateResource1.getId()
        );

        assertEquals(false, result.isResult());
    }

    @Test
    public void testUpdateTemplateResourcesBillable() throws InterruptedException {

        DbPopulator populator = new DbPopulator(dao);

        VisitType visitType = populator.visitTypePopulator.populateOne(1);

        VisitTemplate visitTemplate1 = populator.visitTemplatePopulator.populateOne(
                1,
                null,
                visitType,
                true,
                false,
                false
        );
        VisitTemplate visitTemplate2 = populator.visitTemplatePopulator.populateOne(
                2,
                null,
                visitType,
                true,
                false,
                false
        );

        Resource resource = populator.resourcePopulator.populateOne(1);

        TemplateResource templateResource1 = populator.templateResourcePopulator.populateOne(
                null,
                visitTemplate1,
                resource,
                0,
                10,
                null,
                null,
                false,
                false,
                false
        );
        TemplateResource templateResource2 = populator.templateResourcePopulator.populateOne(
                null,
                visitTemplate1,
                resource,
                10,
                20,
                null,
                null,
                false,
                false,
                false
        );
        TemplateResource templateResource3 = populator.templateResourcePopulator.populateOne(
                null,
                visitTemplate2,
                resource,
                0,
                10,
                null,
                null,
                false,
                false,
                false
        );
        TemplateResource templateResource4 = populator.templateResourcePopulator.populateOne(
                null,
                visitTemplate2,
                resource,
                0,
                10,
                null,
                null,
                false,
                false,
                false
        );

        String templateResourcesCommaString =
                "" + templateResource3.getId() +
                        "," + templateResource4.getId();

        long visitTemplateLastUpdated1 = visitTemplate1.getLastUpdateTime().getTime();
        long visitTemplateLastUpdated2 = visitTemplate2.getLastUpdateTime().getTime();

        long templateResourceLastUpdated1 = templateResource1.getLastUpdateTime().getTime();
        long templateResourceLastUpdated2 = templateResource2.getLastUpdateTime().getTime();
        long templateResourceLastUpdated3 = templateResource3.getLastUpdateTime().getTime();
        long templateResourceLastUpdated4 = templateResource4.getLastUpdateTime().getTime();

        // method under test

        BooleanResultDTO result = appointmentService.updateTemplateResourcesBillable(
                visitTemplate2.getId(),
                templateResourcesCommaString,
                true
        );

        // verify direct output

        assertTrue(result.isResult());

        // refresh entities from the database

        dao.refreshEntity(visitTemplate1);
        dao.refreshEntity(visitTemplate2);
        dao.refreshEntity(templateResource1);
        dao.refreshEntity(templateResource2);
        dao.refreshEntity(templateResource3);
        dao.refreshEntity(templateResource4);

        // verify entities

        assertTrue(visitTemplate1.getApproved());
        assertFalse(visitTemplate2.getApproved());

        assertFalse(templateResource1.getBillable());
        assertFalse(templateResource2.getBillable());
        assertTrue(templateResource3.getBillable());
        assertTrue(templateResource4.getBillable());

        assertEquals(visitTemplateLastUpdated1, visitTemplate1.getLastUpdateTime().getTime());
        assertTrue(visitTemplateLastUpdated2 < visitTemplate2.getLastUpdateTime().getTime());
        assertEquals(templateResourceLastUpdated1, templateResource1.getLastUpdateTime().getTime());
        assertEquals(templateResourceLastUpdated2, templateResource2.getLastUpdateTime().getTime());
        assertTrue(templateResourceLastUpdated3 < templateResource3.getLastUpdateTime().getTime());
        assertTrue(templateResourceLastUpdated4 < templateResource4.getLastUpdateTime().getTime());

    }


    @Test
    public void testIsDayCopiable() {

        DbPopulator populator = new DbPopulator(dao);

        VisitType visitType = populator.visitTypePopulator.populateOne(1);

        VisitTemplate visitTemplate = populator.visitTemplatePopulator.populateOne(
                1,
                null,
                visitType,
                true,
                false,
                false
        );

        boolean result = appointmentService.isDayCopyable(visitTemplate.getId(), 3);

        assertFalse(result);

        Resource resource = populator.resourcePopulator.populateOne(1);

        populator.templateResourcePopulator.populateOne(
                null,
                visitTemplate,
                resource,
                0,
                2 * DateUtility.MINS_PER_DAY,
                null,
                null,
                false,
                false,
                false
        );

        result = appointmentService.isDayCopyable(visitTemplate.getId(), 3);

        assertFalse(result);

        TemplateResource templateResource = populator.templateResourcePopulator.populateOne(
                null,
                visitTemplate,
                resource,
                3 * DateUtility.MINS_PER_DAY,
                4 * DateUtility.MINS_PER_DAY - 1,
                3 * DateUtility.MINS_PER_DAY,
                4 * DateUtility.MINS_PER_DAY - 1,
                false,
                false,
                false
        );
        String groupId = "some group ID";
        populator.templateResourceGroupPopulator.populateOne(
                1,
                groupId,
                templateResource,
                visitTemplate
        );

        result = appointmentService.isDayCopyable(visitTemplate.getId(), 4);

        assertTrue(result);

        templateResource.setGroupId(null);
        templateResource.setFlexible(true);
        templateResource.setFloatable(false);
        dao.updateEntity(templateResource);

        result = appointmentService.isDayCopyable(visitTemplate.getId(), 4);

        assertTrue(result);

        templateResource.setGroupId(null);
        templateResource.setFlexible(false);
        templateResource.setFloatable(true);
        dao.updateEntity(templateResource);

        result = appointmentService.isDayCopyable(visitTemplate.getId(), 4);

        assertTrue(result);

        templateResource.setGroupId(null);
        templateResource.setFlexible(true);
        templateResource.setFloatable(true);
        dao.updateEntity(templateResource);

        result = appointmentService.isDayCopyable(visitTemplate.getId(), 4);

        assertTrue(result);


        templateResource.setGroupId(groupId);
        templateResource.setFlexible(false);
        templateResource.setFloatable(false);
        dao.updateEntity(templateResource);

        result = appointmentService.isDayCopyable(visitTemplate.getId(), 4);

        assertTrue(result);

        templateResource.setGroupId(groupId);
        templateResource.setFlexible(true);
        templateResource.setFloatable(false);
        dao.updateEntity(templateResource);

        result = appointmentService.isDayCopyable(visitTemplate.getId(), 4);

        assertTrue(result);

        templateResource.setGroupId(groupId);
        templateResource.setFlexible(false);
        templateResource.setFloatable(true);
        dao.updateEntity(templateResource);

        result = appointmentService.isDayCopyable(visitTemplate.getId(), 4);

        assertTrue(result);

        templateResource.setGroupId(groupId);
        templateResource.setFlexible(true);
        templateResource.setFloatable(true);
        dao.updateEntity(templateResource);

        result = appointmentService.isDayCopyable(visitTemplate.getId(), 4);

        assertTrue(result);
    }

    @Test
    public void testCopyDayVisitResources() {

        DbPopulator populator = new DbPopulator(dao);

        String groupId1 = "some group id 1";
        String groupId2 = "some group id 2";

        VisitType visitType = populator.visitTypePopulator.populateOne(1);

        VisitTemplate visitTemplate = populator.visitTemplatePopulator.populateOne(
                1,
                null,
                visitType,
                true,
                false,
                false
        );
        int visitId = visitTemplate.getId();

        Resource resource = populator.resourcePopulator.populateOne(1);

        int startMinutes1a = 3 * DateUtility.MINS_PER_DAY;
        int endMinutes1a = 4 * DateUtility.MINS_PER_DAY - 1;
        TemplateResource templateResource1a = populator.templateResourcePopulator.populateOne(
                null,
                visitTemplate,
                resource,
                startMinutes1a,
                endMinutes1a,
                null,
                null,
                false,
                false,
                false
        );
        int startMinutes1b = 3 * DateUtility.MINS_PER_DAY + 100;
        int endMinutes1b = 4 * DateUtility.MINS_PER_DAY - 100;
        TemplateResource templateResource1b = populator.templateResourcePopulator.populateOne(
                null,
                visitTemplate,
                resource,
                startMinutes1b,
                endMinutes1b,
                null,
                null,
                false,
                false,
                false
        );
        int startMinutes2 = 3 * DateUtility.MINS_PER_DAY;
        int endMinutes2 = 4 * DateUtility.MINS_PER_DAY - 1;
        int floatStart2 = 3 * DateUtility.MINS_PER_DAY;
        int floatEnd2 = 4 * DateUtility.MINS_PER_DAY - 1;
        TemplateResource templateResource2 = populator.templateResourcePopulator.populateOne(
                groupId1,
                visitTemplate,
                resource,
                startMinutes2,
                endMinutes2,
                floatStart2,
                floatEnd2,
                false,
                false,
                true
        );
        int startMinutes3 = 3 * DateUtility.MINS_PER_DAY;
        int endMinutes3 = 4 * DateUtility.MINS_PER_DAY - 1;
        int floatStart3 = 3 * DateUtility.MINS_PER_DAY;
        int floatEnd3 = 4 * DateUtility.MINS_PER_DAY - 1;
        TemplateResource templateResource3 = populator.templateResourcePopulator.populateOne(
                groupId2,
                visitTemplate,
                resource,
                startMinutes3,
                endMinutes3,
                floatStart3,
                floatEnd3,
                false,
                true,
                false
        );

        populator.templateResourceGroupPopulator.populateOne(
                1,
                groupId1,
                templateResource2,
                visitTemplate
        );
        populator.templateResourceGroupPopulator.populateOne(
                1,
                groupId2,
                templateResource3,
                visitTemplate
        );

        int currentDay = 4;
        String targetDaysCommaString = "4,7";
        String institution = "some institution";
        String templatePath = "some template path";

        List<Integer> preExistingTemplateResourceIds = Arrays.asList(
                templateResource1a.getId(),
                templateResource1b.getId(),
                templateResource2.getId(),
                templateResource3.getId()
        );

        List<TemplateResource> newTemplateResources = findNewTemplateResources(preExistingTemplateResourceIds);
        assertEquals(0, newTemplateResources.size());

        // stub out the email notification method
        AppointmentService appointmentServiceSpy = Mockito.spy(appointmentService);
        Mockito.doNothing().when(appointmentServiceSpy).sendVisitTemplateResourceUpdatedEmail(visitTemplate, institution, true, templatePath);

        // Method under test
        appointmentServiceSpy.copyDayVisitResources(
                visitId,
                currentDay,
                targetDaysCommaString,
                institution,
                templatePath
        );

        newTemplateResources = findNewTemplateResources(preExistingTemplateResourceIds);
        assertEquals(8, newTemplateResources.size());

        List<TemplateResource> newTemplateResourcesWithNullGroupId =
                newTemplateResources.stream().filter(tr -> !tr.getFlexible() && !tr.getFloatable())
                        .collect(Collectors.toList());

        assertEquals(4, newTemplateResourcesWithNullGroupId.size());
        newTemplateResourcesWithNullGroupId.stream().forEach(tr -> {
            assertNull(tr.getGroupId());
        });
        assertEquals(1, newTemplateResourcesWithNullGroupId.stream().filter(tr ->
                tr.getStartMinutes() == startMinutes1a
                        && tr.getEndMinutes() == endMinutes1a
        ).collect(Collectors.toList()).size());
        assertEquals(1, newTemplateResourcesWithNullGroupId.stream().filter(tr ->
                tr.getStartMinutes() == startMinutes1a + 3 * DateUtility.MINS_PER_DAY
                        && tr.getEndMinutes() == endMinutes1a + 3 * DateUtility.MINS_PER_DAY
        ).collect(Collectors.toList()).size());
        assertEquals(1, newTemplateResourcesWithNullGroupId.stream().filter(tr ->
                tr.getStartMinutes() == startMinutes1b
                        && tr.getEndMinutes() == endMinutes1b
        ).collect(Collectors.toList()).size());
        assertEquals(1, newTemplateResourcesWithNullGroupId.stream().filter(tr ->
                tr.getStartMinutes() == startMinutes1b + 3 * DateUtility.MINS_PER_DAY
                        && tr.getEndMinutes() == endMinutes1b + 3 * DateUtility.MINS_PER_DAY
        ).collect(Collectors.toList()).size());

        List<TemplateResource> newFlexibleTemplateResources =
                newTemplateResources.stream().filter(tr -> tr.getFlexible())
                        .collect(Collectors.toList());

        assertEquals(2, newFlexibleTemplateResources.size());
        assertEquals(1, newFlexibleTemplateResources.stream().filter(tr ->
                tr.getStartMinutes() == startMinutes2
                        && tr.getEndMinutes() == endMinutes2
                        && tr.getFloatStart() == floatStart2
                        && tr.getFloatEnd() == floatEnd2
        ).collect(Collectors.toList()).size());
        assertEquals(1, newFlexibleTemplateResources.stream().filter(tr ->
                tr.getStartMinutes() == startMinutes2 + 3 * DateUtility.MINS_PER_DAY
                        && tr.getEndMinutes() == endMinutes2 + 3 * DateUtility.MINS_PER_DAY
                        && tr.getFloatStart() == floatStart2
                        && tr.getFloatEnd() == floatEnd2
        ).collect(Collectors.toList()).size());

        List<TemplateResource> newFloatableTemplateResources =
                newTemplateResources.stream().filter(tr -> tr.getFloatable())
                        .collect(Collectors.toList());

        assertEquals(2, newFloatableTemplateResources.size());
        assertEquals(1, newFloatableTemplateResources.stream().filter(tr ->
                tr.getStartMinutes() == startMinutes3
                        && tr.getEndMinutes() == endMinutes3
                        && tr.getFloatStart() == floatStart3
                        && tr.getFloatEnd() == floatEnd3
        ).collect(Collectors.toList()).size());
        assertEquals(1, newFloatableTemplateResources.stream().filter(tr ->
                tr.getStartMinutes() == startMinutes3 + 3 * DateUtility.MINS_PER_DAY
                        && tr.getEndMinutes() == endMinutes3 + 3 * DateUtility.MINS_PER_DAY
                        && tr.getFloatStart() == floatStart3 + 3 * DateUtility.MINS_PER_DAY
                        && tr.getFloatEnd() == floatEnd3 + 3 * DateUtility.MINS_PER_DAY
        ).collect(Collectors.toList()).size());

    }

    List<TemplateResource> findNewTemplateResources(List<Integer> existingIds) {

        // Alternatively we could set a threshold on TemplateResource.createdDate
        String hql = "SELECT tr FROM TemplateResource tr WHERE tr.id NOT IN :existingIds";
        Query query = dao.newQuery(hql);
        query.setParameterList("existingIds", existingIds);
        List<TemplateResource> newTemplateResources = query.list();

        return newTemplateResources;

    }

    @Test
    public void testGetTemplateResourceDataWithAnnotations() {

        DbPopulator populator = new DbPopulator(dao);

        VisitType visitType = populator.visitTypePopulator.populateOne(1);

        VisitTemplate visitTemplate = populator.visitTemplatePopulator.populateOne(
                1,
                null,
                visitType,
                true,
                false,
                false
        );
        int visitId = visitTemplate.getId();

        Resource resource = populator.resourcePopulator.populateOne(1);

        int startMinutes1a = 3 * DateUtility.MINS_PER_DAY;
        int endMinutes1a = 4 * DateUtility.MINS_PER_DAY - 1;
        String templateResourceGroupId = "some group id";
        List<TemplateResource> templateResources = populator.templateResourcePopulator.populateMany(
                2,
                templateResourceGroupId,
                visitTemplate,
                resource,
                startMinutes1a,
                endMinutes1a,
                null,
                null,
                false,
                false,
                false
        );
        TemplateResource templateResource1 = templateResources.get(0);
        int templateResourceId1 = templateResources.get(0).getId();

        LineLevelAnnotations lineLevelAnnotations1 = populator.lineLevelAnnotationsPopulator.populateOne(
                "name D",
                2,
                0,
                "line-level annotation comment D"
        );
        LineLevelAnnotations lineLevelAnnotations2 = populator.lineLevelAnnotationsPopulator.populateOne(
                "name B",
                3,
                0,
                "line-level annotation comment B"
        );
        LineLevelAnnotations lineLevelAnnotations3 = populator.lineLevelAnnotationsPopulator.populateOne(
                "name C",
                5,
                0,
                "line-level annotation comment C"
        );
        LineLevelAnnotations lineLevelAnnotations4 = populator.lineLevelAnnotationsPopulator.populateOne(
                "name A",
                7,
                0,
                "line-level annotation comment A"
        );

        ResourceAnnotation resourceAnnotation1 = populator.resourceAnnotationPopulator.populateOne(
                1,
                lineLevelAnnotations1,
                resource
        );
        ResourceAnnotation resourceAnnotation2 = populator.resourceAnnotationPopulator.populateOne(
                2,
                lineLevelAnnotations2,
                resource
        );
        ResourceAnnotation resourceAnnotation3 = populator.resourceAnnotationPopulator.populateOne(
                3,
                lineLevelAnnotations3,
                resource
        );

        TemplateResourceAnnotations templateResource1Annotations1 =
                populator.templateResourceAnnotationsPopulator.populateOne(
                        1,
                        templateResource1,
                        lineLevelAnnotations1,
                        9,
                        "template resource annotation comment 1"
                );
        TemplateResourceAnnotations templateResource1Annotations2 =
                populator.templateResourceAnnotationsPopulator.populateOne(
                        1,
                        templateResource1,
                        lineLevelAnnotations3,
                        11,
                        "template resource annotation comment 2"
                );

        TemplateResourceWithLlaListDTO responseDto = appointmentService.getTemplateResourceDataWithAnnotations(templateResourceId1);

        assertEquals(templateResource1.getId(), responseDto.templateResource.getId());

        assertEquals(3, responseDto.llaList.size());

        LineLevelAnnotations responseLla1 = responseDto.llaList.get(0);
        LineLevelAnnotations responseLla2 = responseDto.llaList.get(1);
        LineLevelAnnotations responseLla3 = responseDto.llaList.get(2);

        assertEquals(lineLevelAnnotations2.getId(), responseLla1.getId());
        assertEquals(lineLevelAnnotations3.getId(), responseLla2.getId());
        assertEquals(lineLevelAnnotations1.getId(), responseLla3.getId());

        assertEquals("name B", responseLla1.getName());
        assertEquals("name C", responseLla2.getName());
        assertEquals("name D", responseLla3.getName());

        assertFalse(responseLla1.isSelected());
        assertTrue(responseLla2.isSelected());
        assertTrue(responseLla3.isSelected());

        assertEquals(3, responseLla1.getQuantity());
        assertEquals(11, responseLla2.getQuantity());
        assertEquals(9, responseLla3.getQuantity());

        assertEquals("line-level annotation comment B", responseLla1.getComment());
        assertEquals("template resource annotation comment 2", responseLla2.getComment());
        assertEquals("template resource annotation comment 1", responseLla3.getComment());

        assertEquals(0, responseLla1.getResourceAnnotations());
        assertEquals(templateResource1Annotations2.getId(), Integer.valueOf(responseLla2.getResourceAnnotations()));
        assertEquals(templateResource1Annotations1.getId(), Integer.valueOf(responseLla3.getResourceAnnotations()));

    }

    @Test
    public void testUnlinkOneResource() {

        DbPopulator populator = new DbPopulator(dao);

        VisitType visitType = populator.visitTypePopulator.populateOne(1);

        VisitTemplate visitTemplate = populator.visitTemplatePopulator.populateOne(
                1,
                null,
                visitType,
                true,
                false,
                false
        );
        int visitId = visitTemplate.getId();

        Resource resource = populator.resourcePopulator.populateOne(1);

        int startMinutes1a = 3 * DateUtility.MINS_PER_DAY;
        int endMinutes1a = 4 * DateUtility.MINS_PER_DAY - 1;
        List<TemplateResource> templateResources = populator.templateResourcePopulator.populateMany(
                2,
                null,
                visitTemplate,
                resource,
                startMinutes1a,
                endMinutes1a,
                null,
                null,
                false,
                false,
                false
        );
        TemplateResource templateResource1 = templateResources.get(0);
        int templateResourceId1 = templateResources.get(0).getId();

        // case where there is no such TemplateResource

        BooleanResultDTO response = appointmentService.unlinkOneResource(
                0,
                "some institution",
                "some template path"
        );

        // case where the groupId String on the template resource is null

        assertFalse(response.isResult());
        assertEquals("Selected Resource does not exist, or is not linked in a group.", response.getErrorMsg());

        response = appointmentService.unlinkOneResource(
                templateResourceId1,
                "some institution",
                "some template path"
        );

        assertFalse(response.isResult());
        assertEquals("Selected Resource does not exist, or is not linked in a group.", response.getErrorMsg());

        // case where there is no TemplateResourceGroup by that group Id

        String templateResourceGroupId = "some group id";
        templateResource1.setGroupId(templateResourceGroupId);
        dao.updateEntity(templateResource1);

        response = appointmentService.unlinkOneResource(
                templateResourceId1,
                "some institution",
                "some template path"
        );

        assertTrue(response.isResult());
        assertNull(response.getErrorMsg());

        // now test the non-trivial cases

    }

    @Test
    public void testDeleteBookedResourceOverride() {

        AppointmentService appointmentServiceSpy = spy(appointmentService);

        DbPopulator populator = new DbPopulator(dao);

        Study study = populator.studyPopulator.populateOne(1);

        VisitType visitType = populator.visitTypePopulator.populateOne(1);
        VisitTemplate visitTemplate = populator.visitTemplatePopulator.populateOne(
                1,
                null,
                visitType,
                true,
                false,
                false
        );

        Resource resource1 = populator.resourcePopulator.populateOne(1);
        Resource resource2 = populator.resourcePopulator.populateOne(2);

        int startMinutes1a = 3 * DateUtility.MINS_PER_DAY;
        int endMinutes1a = 4 * DateUtility.MINS_PER_DAY - 1;
        TemplateResource templateResource1 = populator.templateResourcePopulator.populateOne(
                null,
                visitTemplate,
                resource1,
                startMinutes1a,
                endMinutes1a,
                null,
                null,
                false,
                false,
                false
        );
        TemplateResource templateResource2 = populator.templateResourcePopulator.populateOne(
                null,
                visitTemplate,
                resource2,
                startMinutes1a,
                endMinutes1a,
                null,
                null,
                false,
                false,
                false
        );

        AppointmentStatus appointmentStatus = populator.appointmentStatusPopulator.populateOne(1);
        BookedVisit bookedVisit = populator.bookedVisitPopulator.populateOne(
                study,
                visitType,
                visitTemplate,
                appointmentStatus,
                new Date(),
                new Date(),
                new Date(),
                null,
                new Date(),
                null,
                new Date(),
                null
        );

        BookedResource bookedResource1 = populator.bookedResourcePopulator.populateOne(
                templateResource1,
                bookedVisit,
                new Date(),
                new Date()
        );
        BookedResource bookedResource2 = populator.bookedResourcePopulator.populateOne(
                templateResource2,
                bookedVisit,
                new Date(),
                new Date()
        );

        AppointmentOverrideReason appointmentOverrideReason = populator.appointmentOverrideReasonPopulator.populateOne(1);

        final User user = populator.userPopulator.populateOne(
                1,
                populator.institutionPopulator.populateOne(1),
                populator.institutionRolePopulator.populateOne(1),
                populator.rolePopulator.populateOne(1)
        );

        final String ipAddress = "1234";

        Gson gson = new GsonBuilder().create();

        int bookedResourceId1 = bookedResource1.getId();
        int appointmentOverrideReasonId = appointmentOverrideReason.getId();

        String data = "{\"id\":\"" + bookedResourceId1 + "\",\"overrideReason\":\"" + appointmentOverrideReasonId + "\"}";

        final VisitSpecsDTO visitSpecsDTO = gson.fromJson(data, VisitSpecsDTO.class);


        BooleanResultDTO result = appointmentServiceSpy.deleteBookedResourceOverride(visitSpecsDTO, user, ipAddress);

    }

    @Test
    public void testLinkTemplateResourcesAsNewGroup() {

        AppointmentService appointmentServiceSpy = spy(appointmentService);

        DbPopulator dataPopulator = new DbPopulator(dao);

        Study study = dataPopulator.studyPopulator.populateOne(1);

        VisitType visitType = dataPopulator.visitTypePopulator.populateOne(1);
        VisitTemplate visitTemplate = dataPopulator.visitTemplatePopulator.populateOne(
                1,
                null,
                visitType,
                true,
                false,
                false
        );

        Resource resource1 = dataPopulator.resourcePopulator.populateOne(1);
        Resource resource2 = dataPopulator.resourcePopulator.populateOne(2);

        int startMinutes1a = 3 * DateUtility.MINS_PER_DAY;
        int endMinutes1a = 4 * DateUtility.MINS_PER_DAY - 1;
        TemplateResource templateResource1 = dataPopulator.templateResourcePopulator.populateOne(
                null,
                visitTemplate,
                resource1,
                startMinutes1a,
                endMinutes1a,
                null,
                null,
                false,
                false,
                false
        );
        TemplateResource templateResource2 = dataPopulator.templateResourcePopulator.populateOne(
                null,
                visitTemplate,
                resource2,
                startMinutes1a,
                endMinutes1a,
                null,
                null,
                false,
                false,
                false
        );

        List<Integer> linkResources = new ArrayList<>();
        linkResources.add(templateResource1.getId());
        linkResources.add(templateResource2.getId());

        GetTemplateResourceGroupDTO booleanResultDTO = new GetTemplateResourceGroupDTO();
        booleanResultDTO.setLinkResources(linkResources);

        String institution = "institution";
        final String templatePath = "template/path";

        BooleanResultDTO tmp = new BooleanResultDTO();

        // CASE 1: resources are not linkable

        templateResource1.setFloatable(false);
        templateResource1.setFlexible(false);
        templateResource2.setFloatable(false);
        templateResource2.setFlexible(false);
        save(templateResource1);
        save(templateResource2);

        Mockito.reset(appointmentServiceSpy);
        Mockito.doNothing().when(appointmentServiceSpy).sendVisitTemplateResourceUpdatedEmail(
                any(VisitTemplate.class), any(String.class), any(Boolean.class), any(String.class)
        );

        appointmentServiceSpy.linkTemplateResourcesAsNewGroup(booleanResultDTO, institution, templatePath);

        Mockito.verify(appointmentServiceSpy, times(0)).sendVisitTemplateResourceUpdatedEmail(
                any(VisitTemplate.class), any(String.class), any(Boolean.class), any(String.class)
        );
        assertFalse(booleanResultDTO.isResult());

        // CASE 2: one resource is floatable

        templateResource1.setFloatable(true);
        templateResource1.setFlexible(false);
        templateResource2.setFloatable(false);
        templateResource2.setFlexible(false);
        save(templateResource1);
        save(templateResource2);

        Mockito.reset(appointmentServiceSpy);
        Mockito.doNothing().when(appointmentServiceSpy).sendVisitTemplateResourceUpdatedEmail(
                any(VisitTemplate.class), any(String.class), any(Boolean.class), any(String.class)
        );

        appointmentServiceSpy.linkTemplateResourcesAsNewGroup(booleanResultDTO, institution, templatePath);

        Mockito.verify(appointmentServiceSpy, times(0)).sendVisitTemplateResourceUpdatedEmail(
                any(VisitTemplate.class), any(String.class), any(Boolean.class), any(String.class)
        );
        assertFalse(booleanResultDTO.isResult());

        // CASE 3: both resources are floatable

        templateResource1.setFloatable(true);
        templateResource1.setFlexible(false);
        templateResource2.setFloatable(true);
        templateResource2.setFlexible(false);
        save(templateResource1);
        save(templateResource2);

        Mockito.reset(appointmentServiceSpy);
        Mockito.doNothing().when(appointmentServiceSpy).sendVisitTemplateResourceUpdatedEmail(
                any(VisitTemplate.class), any(String.class), any(Boolean.class), any(String.class)
        );

        appointmentServiceSpy.linkTemplateResourcesAsNewGroup(booleanResultDTO, institution, templatePath);

        Mockito.verify(appointmentServiceSpy, times(1)).sendVisitTemplateResourceUpdatedEmail(
                any(VisitTemplate.class), any(String.class), any(Boolean.class), any(String.class)
        );
        assertTrue(booleanResultDTO.isResult());

        // CASE 4: one resource is flexible, the other floatable

        templateResource1.setFloatable(true);
        templateResource1.setFlexible(false);
        templateResource2.setFloatable(false);
        templateResource2.setFlexible(true);
        save(templateResource1);
        save(templateResource2);

        Mockito.reset(appointmentServiceSpy);
        Mockito.doNothing().when(appointmentServiceSpy).sendVisitTemplateResourceUpdatedEmail(
                any(VisitTemplate.class), any(String.class), any(Boolean.class), any(String.class)
        );

        appointmentServiceSpy.linkTemplateResourcesAsNewGroup(booleanResultDTO, institution, templatePath);

        Mockito.verify(appointmentServiceSpy, times(0)).sendVisitTemplateResourceUpdatedEmail(
                any(VisitTemplate.class), any(String.class), any(Boolean.class), any(String.class)
        );
        assertFalse(booleanResultDTO.isResult());

        // CASE 5: one resource is flexible, the other neither flexible nor floatable

        templateResource1.setFloatable(false);
        templateResource1.setFlexible(false);
        templateResource2.setFloatable(false);
        templateResource2.setFlexible(true);
        save(templateResource1);
        save(templateResource2);

        Mockito.reset(appointmentServiceSpy);
        Mockito.doNothing().when(appointmentServiceSpy).sendVisitTemplateResourceUpdatedEmail(
                any(VisitTemplate.class), any(String.class), any(Boolean.class), any(String.class)
        );

        appointmentServiceSpy.linkTemplateResourcesAsNewGroup(booleanResultDTO, institution, templatePath);

        Mockito.verify(appointmentServiceSpy, times(0)).sendVisitTemplateResourceUpdatedEmail(
                any(VisitTemplate.class), any(String.class), any(Boolean.class), any(String.class)
        );
        assertFalse(booleanResultDTO.isResult());

        // CASE 6: empty resource list

        booleanResultDTO.setLinkResources(new ArrayList<>());

        Mockito.reset(appointmentServiceSpy);
        Mockito.doNothing().when(appointmentServiceSpy).sendVisitTemplateResourceUpdatedEmail(
                any(VisitTemplate.class), any(String.class), any(Boolean.class), any(String.class)
        );

        appointmentServiceSpy.linkTemplateResourcesAsNewGroup(booleanResultDTO, institution, templatePath);

        Mockito.verify(appointmentServiceSpy, times(0)).sendVisitTemplateResourceUpdatedEmail(
                any(VisitTemplate.class), any(String.class), any(Boolean.class), any(String.class)
        );
        assertFalse(booleanResultDTO.isResult());

        // CASE 7: only one resource list

        linkResources = new ArrayList<>();
        linkResources.add(templateResource1.getId());
        booleanResultDTO.setLinkResources(linkResources);

        Mockito.reset(appointmentServiceSpy);
        Mockito.doNothing().when(appointmentServiceSpy).sendVisitTemplateResourceUpdatedEmail(
                any(VisitTemplate.class), any(String.class), any(Boolean.class), any(String.class)
        );

        appointmentServiceSpy.linkTemplateResourcesAsNewGroup(booleanResultDTO, institution, templatePath);

        Mockito.verify(appointmentServiceSpy, times(0)).sendVisitTemplateResourceUpdatedEmail(
                any(VisitTemplate.class), any(String.class), any(Boolean.class), any(String.class)
        );
        assertFalse(booleanResultDTO.isResult());
    }

    @Test
    public void testOverbookRoomData() {

        DbPopulator dataPopulator = new DbPopulator(dao);

        String templateResourceGroupId = "111";

        int startMinutes = 10;
        int endMinutes = 20;
        int floatStart = 0;
        int floatEnd = 0;
        boolean billable = false;
        boolean floatable = false;
        boolean flexible = false;

        int duration = 20;

        Date scheduledStartTime = new Date();
        Date scheduledEndTime = new Date();
        Date checkInDate = new Date();
        User checkInUser = dataPopulator.userPopulator.populateOneWithDependencies(1);
        Date checkOutDate = new Date();
        User checkOutUser = dataPopulator.userPopulator.populateOneWithDependencies(2);
        Date cancelDate = new Date();
        User cancelUser = dataPopulator.userPopulator.populateOneWithDependencies(3);

        Gender gender = dataPopulator.genderPopulator.populateOne(1);
        GenderType genderType = GenderType.F;
        Subject subject = dataPopulator.subjectPopulator.populateOne(
                1,
                gender,
                genderType
        );
        SubjectMrn subjectMrn = dataPopulator.subjectMrnPopulator.populateOne(subject, "000111");

        Resource resource = dataPopulator.resourcePopulator.populateOne(1);

        VisitTemplate visitTemplate = dataPopulator.visitTemplatePopulator.populateOneWithDependencies(
                1,
                true,
                false,
                false
        );
        visitTemplate.setDuration(duration);
        save(visitTemplate);

        TemplateResource templateResource = dataPopulator.templateResourcePopulator.populateOne(
                templateResourceGroupId,
                visitTemplate,
                resource,
                startMinutes,
                endMinutes,
                floatStart,
                floatEnd,
                billable,
                floatable,
                flexible
        );

        AppointmentStatus appointmentStatus = dataPopulator.appointmentStatusPopulator.populateOne(1);
        appointmentStatus.setIsScheduled(true);
        save(appointmentStatus);

        BookedVisit bookedVisit = dataPopulator.bookedVisitPopulator.populateOne(
                visitTemplate.getStudy(),
                visitTemplate.getVisitType(),
                visitTemplate,
                appointmentStatus,
                scheduledStartTime,
                scheduledEndTime,
                checkInDate,
                checkInUser,
                checkOutDate,
                checkOutUser,
                cancelDate,
                cancelUser
        );

        BookedResource bookedResource = dataPopulator.bookedResourcePopulator.populateOne(
                templateResource,
                bookedVisit,
                scheduledStartTime,
                scheduledEndTime
        );

        Date startDate = new Date();

        final VisitSpecsDTO visitSpecsDTO = new VisitSpecsDTO();
        visitSpecsDTO.setId(bookedVisit.getId());
        visitSpecsDTO.setStartDate(startDate.getTime());
        visitSpecsDTO.setSubjectMrnId(subjectMrn.getId());
        visitSpecsDTO.setVisit(visitTemplate.getId());
        visitSpecsDTO.setRoomSelected(templateResource.getResource().getId());

        BookedVisit bookedVisit2 = appointmentService.overbookRoomData(visitSpecsDTO);

        assertEquals(appointmentStatus, bookedVisit2.getAppointmentStatus());
        assertNull(bookedVisit2.getAppointmentStatusReason());
        assertNull(bookedVisit2.getCancelDate());
        assertNull(bookedVisit2.getCancelUser());
        assertNull(bookedVisit2.getCancelStatus());
        assertNull(bookedVisit2.getCheckInDate());
        assertNull(bookedVisit2.getCheckInUser());
        assertNull(bookedVisit2.getCheckoutStatusReason());
        assertEquals("", bookedVisit2.getComment());
        assertEquals(visitTemplate.getName(), bookedVisit2.getName());
        assertTrue(bookedVisit2.getScheduledEndTime().getTime() > startDate.getTime());
        assertEquals(startDate, bookedVisit2.getScheduledStartTime());
        assertEquals(visitTemplate.getStudy(), bookedVisit2.getStudy());
        assertEquals(subjectMrn, bookedVisit2.getSubjectMrn());
        assertEquals(visitTemplate, bookedVisit2.getVisitTemplate());
        assertEquals(visitTemplate.getVisitType(), bookedVisit2.getVisitType());
        assertEquals(resource, bookedVisit2.getSelectedRoom());
    }

}

