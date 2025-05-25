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

import edu.harvard.catalyst.scheduler.AbstractSpringWiredHibernateTest;
import edu.harvard.catalyst.scheduler.dto.VisitDTO;
import edu.harvard.catalyst.scheduler.entity.*;
import edu.harvard.catalyst.scheduler.persistence.StudyDAO;
import edu.harvard.catalyst.scheduler.persistence.TemplateResourceDAO;
import edu.harvard.catalyst.scheduler.util.dbpopulator.DbPopulator;
import static org.junit.Assert.*;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;


public class StudyServiceTransactionalTest extends AbstractSpringWiredHibernateTest {

    @Autowired
    StudyDAO dao;

    @Autowired
    TemplateResourceDAO templateResourceDAO;

    @Autowired
    StudyService service;


    @Test
    public void testCopyVisit() {

        int oldDuration = 10;

        String oldName = "old name";
        String oldShortName = "old short name";
        String oldNursing = "old nursing";
        String oldNutrition = "old nutrition";
        String oldProcessing = "old processing";
        String oldSetup = "old setup";
        String oldComment = "old comment";
        boolean oldInstitutionNonCRC = true;
        boolean oldNonInstitutionNonCRC = true;
        boolean oldResearchPharmacy = true;
        boolean oldActive = true;
        boolean oldRelativeTime = false;
        String templateResourceGroupId = "abc1";

        String newName = "new name";
        String newShortName = "new short name";
        String newNursing = "new nursing";
        String newNutrition = "new nutrition";
        String newProcessing = "new processing";
        String newSetup = "new setup";
        String newComment = "new comment";
        boolean newRelativeTime = true;

        Integer tr1StartMinutes = 2;
        Integer tr1EndMinutes = 4;
        Integer tr1FloatStart = 6;
        Integer tr1FloatEnd = 8;
        boolean tr1Billable = true;
        boolean tr1Floatable = true;
        boolean tr1Flexible = true;

        Integer tr2StartMinutes = 3;
        Integer tr2EndMinutes = 5;
        Integer tr2FloatStart = 7;
        Integer tr2FloatEnd = 9;
        boolean tr2Billable = false;
        boolean tr2Floatable = false;
        boolean tr2Flexible = false;

        final DbPopulator dataPopulator = new DbPopulator(dao);

        final User user = dataPopulator.userPopulator.populateOneWithDependencies(
                1
        );

        final String ipAddress = "my IP address";

        final Sublocation oldSublocation =
                dataPopulator.sublocationPopulator.populateOneWithDependencies(1);

        final Sublocation newSublocation =
                dataPopulator.sublocationPopulator.populateOneWithDependencies(2);

        final Study study = dataPopulator.studyPopulator.populateOne(1);

        final VisitType oldVisitType = dataPopulator.visitTypePopulator.populateOne(1);
        final VisitType newVisitType = dataPopulator.visitTypePopulator.populateOne(2);

        final VisitTemplate oldVisitTemplate =
                dataPopulator.visitTemplatePopulator.populateOne(
                        1,
                        study,
                        oldVisitType,
                        oldSublocation,
                        true,
                        true,
                        true
        );
        oldVisitTemplate.setInstitutionNonCRC(oldInstitutionNonCRC);
        oldVisitTemplate.setNonInstitutionNonCRC(oldNonInstitutionNonCRC);
        oldVisitTemplate.setResearchPharmacy(oldResearchPharmacy);
        oldVisitTemplate.setActive(oldActive);
        oldVisitTemplate.setDuration(oldDuration);
        oldVisitTemplate.setName(oldName);
        oldVisitTemplate.setShortName(oldShortName);
        oldVisitTemplate.setNursing(oldNursing);
        oldVisitTemplate.setNutrition(oldNutrition);
        oldVisitTemplate.setProcessing(oldProcessing);
        oldVisitTemplate.setSetup(oldSetup);
        oldVisitTemplate.setComment(oldComment);
        oldVisitTemplate.setRelativeTime(oldRelativeTime);

        save(oldVisitTemplate);

        Resource resource1 = dataPopulator.resourcePopulator.populateOne(1);
        Resource resource2 = dataPopulator.resourcePopulator.populateOne(2);

        TemplateResource templateResource1 = dataPopulator.templateResourcePopulator.populateOne(
                templateResourceGroupId,
                oldVisitTemplate,
                resource1,
                tr1StartMinutes,
                tr1EndMinutes,
                tr1FloatStart,
                tr1FloatEnd,
                tr1Billable,
                tr1Floatable,
                tr1Flexible
        );

        TemplateResource templateResource2 = dataPopulator.templateResourcePopulator.populateOne(
                null,
                oldVisitTemplate,
                resource2,
                tr2StartMinutes,
                tr2EndMinutes,
                tr2FloatStart,
                tr2FloatEnd,
                tr2Billable,
                tr2Floatable,
                tr2Flexible
        );

        TemplateResourceGroup templateResourceGroup1 = dataPopulator.templateResourceGroupPopulator.populateOne(
                1,
                templateResourceGroupId,
                templateResource1,
                oldVisitTemplate
        );

        LineLevelAnnotations annotation1 = dataPopulator.lineLevelAnnotationsPopulator.populateOne(
                "annotation1",
                1,
                1,
                "annotation comment 1"
        );
        LineLevelAnnotations annotation2 = dataPopulator.lineLevelAnnotationsPopulator.populateOne(
                "annotation2",
                1,
                1,
                "annotation comment 2"
        );
        LineLevelAnnotations annotation3 = dataPopulator.lineLevelAnnotationsPopulator.populateOne(
                "annotation3",
                1,
                1,
                "annotation comment 3"
        );

        TemplateResourceAnnotations tra1 = dataPopulator.templateResourceAnnotationsPopulator.populateOne(
                1, templateResource1, annotation1, 1, null
        );
        TemplateResourceAnnotations tra2 = dataPopulator.templateResourceAnnotationsPopulator.populateOne(
                1, templateResource1, annotation2, 1, null
        );
        TemplateResourceAnnotations tra3 = dataPopulator.templateResourceAnnotationsPopulator.populateOne(
                1, templateResource2, annotation3, 1, null
        );

        final VisitDTO visitDTO = new VisitDTO();
        visitDTO.setId(oldVisitTemplate.getId());
        visitDTO.setSublocation(newSublocation.getId());
        visitDTO.setVisitType(newVisitType.getId());
        visitDTO.setName(newName);
        visitDTO.setShortName(newShortName);
        visitDTO.setApproved(true);
        visitDTO.setRelativeTime(newRelativeTime);
        visitDTO.setNursing(newNursing);
        visitDTO.setNutrition(newNutrition);
        visitDTO.setProcessing(newProcessing);
        visitDTO.setSetup(newSetup);
        visitDTO.setComment(newComment);

        // method under test

        final VisitTemplate newVisitTemplate = service.copyVisit(
                visitDTO, user, ipAddress
        );

        // make sure the new visit has been persisted
        final VisitTemplate retrievedNewVisit = dao.findVisitById(newVisitTemplate.getId());

        assertNotNull(retrievedNewVisit);
        assertEquals(newName, retrievedNewVisit.getName());
        assertEquals(newShortName, retrievedNewVisit.getShortName());
        assertFalse(retrievedNewVisit.getApproved());
        // The following is counter-intuitive:
        // The "new" value of relativeTime in the VisitDTO does not get used.
        // instead, the "old" value stored in oldVisitTemplate is duplicated
        // to the new visit
        assertEquals(oldRelativeTime, retrievedNewVisit.getRelativeTime());
        assertEquals(newNursing, retrievedNewVisit.getNursing());
        assertEquals(newNutrition, retrievedNewVisit.getNutrition());
        assertEquals(newProcessing, retrievedNewVisit.getProcessing());
        assertEquals(newSetup, retrievedNewVisit.getSetup());
        assertEquals(newSublocation, retrievedNewVisit.getSublocation());
        assertNotNull(retrievedNewVisit.getCreatedDate());
        assertEquals(newVisitType, retrievedNewVisit.getVisitType());
        assertEquals(study, retrievedNewVisit.getStudy());
        assertEquals(oldInstitutionNonCRC, retrievedNewVisit.getInstitutionNonCRC());
        assertEquals(oldNonInstitutionNonCRC, retrievedNewVisit.getNonInstitutionNonCRC());
        assertEquals(oldResearchPharmacy, retrievedNewVisit.getResearchPharmacy());
        assertEquals(oldActive, retrievedNewVisit.getActive());
        assertNotNull(retrievedNewVisit.getLastUpdateTime());
        assertEquals(new Integer(oldDuration), retrievedNewVisit.getDuration());
        assertEquals(newComment, retrievedNewVisit.getComment());

        Comments comments = (Comments) dao.findEntityByFieldString("Comments", "comment", newComment);
        assertEquals(newComment, comments.getComment());
        assertEquals(retrievedNewVisit, comments.getVisitTemplate());
        assertEquals(user, comments.getUser());
        assertNotNull(comments.getDate());

        List<TemplateResource> newTemplateResources1 = templateResourceDAO.findTemplateResourcesByVisitAndResource(
                newVisitTemplate.getId(),
                resource1.getId()
        );

        assertEquals(1, newTemplateResources1.size());

        TemplateResource newTemplateResource1 = newTemplateResources1.get(0);

        assertEquals(newVisitTemplate, newTemplateResource1.getVisitTemplate());
        assertEquals(resource1, newTemplateResource1.getResource());
        assertEquals(tr1StartMinutes, newTemplateResource1.getStartMinutes());
        assertEquals(tr1EndMinutes, newTemplateResource1.getEndMinutes());
        assertEquals(tr1FloatStart, newTemplateResource1.getFloatStart());
        assertEquals(tr1FloatEnd, newTemplateResource1.getFloatEnd());
        assertEquals(tr1Billable, newTemplateResource1.getBillable());
        assertEquals(tr1Floatable, newTemplateResource1.getFloatable());
        assertEquals(tr1Flexible, newTemplateResource1.getFlexible());

        List<TemplateResourceAnnotations> newTemplateResourceAnnotations1 = templateResourceDAO.findTemplateResourceAnnotationsByTemplateResource(
                newTemplateResource1
        );

        assertEquals(2, newTemplateResourceAnnotations1.size());

        TemplateResourceAnnotations templateResourceAnnotations1 = newTemplateResourceAnnotations1.get(0);
        assertEquals("annotation comment 1", templateResourceAnnotations1.getLineLevelAnnotations().getComment());

        TemplateResourceAnnotations templateResourceAnnotations2 = newTemplateResourceAnnotations1.get(1);
        assertEquals("annotation comment 2", templateResourceAnnotations2.getLineLevelAnnotations().getComment());

        List<TemplateResource> newTemplateResources2 = templateResourceDAO.findTemplateResourcesByVisitAndResource(
                newVisitTemplate.getId(),
                resource2.getId()
        );

        assertEquals(1, newTemplateResources2.size());

        TemplateResource newTemplateResource2 = newTemplateResources2.get(0);

        assertEquals(newVisitTemplate,  newTemplateResource2.getVisitTemplate());
        assertEquals(resource2,  newTemplateResource2.getResource());
        assertEquals(tr2StartMinutes,  newTemplateResource2.getStartMinutes());
        assertEquals(tr2EndMinutes,  newTemplateResource2.getEndMinutes());
        assertEquals(tr2FloatStart,  newTemplateResource2.getFloatStart());
        assertEquals(tr2FloatEnd,  newTemplateResource2.getFloatEnd());
        assertEquals(tr2Billable,  newTemplateResource2.getBillable());
        assertEquals(tr2Floatable,  newTemplateResource2.getFloatable());
        assertEquals(tr2Flexible,  newTemplateResource2.getFlexible());

        List<TemplateResourceAnnotations> newTemplateResourceAnnotations2 = templateResourceDAO.findTemplateResourceAnnotationsByTemplateResource(
                 newTemplateResource2
        );

        assertEquals(1, newTemplateResourceAnnotations2.size());

        TemplateResourceAnnotations templateResourceAnnotations3 = newTemplateResourceAnnotations2.get(0);
        assertEquals("annotation comment 3", templateResourceAnnotations3.getLineLevelAnnotations().getComment());

    }

}
