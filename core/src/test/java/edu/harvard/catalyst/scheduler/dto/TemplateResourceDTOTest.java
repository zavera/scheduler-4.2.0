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
package edu.harvard.catalyst.scheduler.dto;

import edu.harvard.catalyst.scheduler.AbstractSpringWiredHibernateTest;
import edu.harvard.catalyst.scheduler.entity.*;
import edu.harvard.catalyst.scheduler.persistence.AppointmentDAO;
import edu.harvard.catalyst.scheduler.persistence.TemplateResourceDAO;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by xavier on 6/29/17.
 */
public class TemplateResourceDTOTest  extends AbstractSpringWiredHibernateTest {

    @Test
    public void testConstructor() {

        TemplateResource templateResource = new TemplateResource();

        templateResource.setId(9);
        templateResource.setBillable(true);
        templateResource.setAlternate(true);
        templateResource.setFloatable(true);
        templateResource.setFloatStart(11);
        templateResource.setFloatEnd(22);
        templateResource.setFlexible(true);
        templateResource.setStartMinutes(888);
        templateResource.setEndMinutes(999);

        Resource resource = new Resource();
        resource.setId(8);
        templateResource.setResource(resource);

        VisitTemplate visitTemplate = new VisitTemplate();
        visitTemplate.setId(66);
        templateResource.setVisitTemplate(visitTemplate);

        List<TemplateResourceAnnotations> traList = new ArrayList<>();
        TemplateResourceAnnotations templateResourceAnnotations1 = new TemplateResourceAnnotations();
        templateResourceAnnotations1.setId(1);
        templateResourceAnnotations1.setComment("Comment 1");
        templateResourceAnnotations1.setQuantity(10);
        LineLevelAnnotations lineLevelAnnotations1 = new LineLevelAnnotations();
        lineLevelAnnotations1.setId(33);
        templateResourceAnnotations1.setLineLevelAnnotations(lineLevelAnnotations1);
        traList.add(templateResourceAnnotations1);

        TemplateResourceAnnotations templateResourceAnnotations2 = new TemplateResourceAnnotations();
        templateResourceAnnotations2.setId(2);
        templateResourceAnnotations2.setComment("Comment 2");
        templateResourceAnnotations2.setQuantity(20);
        LineLevelAnnotations lineLevelAnnotations2 = new LineLevelAnnotations();
        lineLevelAnnotations2.setId(44);
        templateResourceAnnotations2.setLineLevelAnnotations(lineLevelAnnotations2);
        traList.add(templateResourceAnnotations2);

        TemplateResourceDAO templateResourceDAO = mock(TemplateResourceDAO.class);
        when(templateResourceDAO.findTemplateResourceAnnotationsByTemplateResource(templateResource)).thenReturn(traList);

        TemplateResourceDTO templateResourceDTO = new TemplateResourceDTO(
                templateResource, templateResourceDAO
        );

        assertEquals(templateResourceDTO.getSelectedAnnotations(), Arrays.asList(33, 44));
        assertEquals(templateResourceDTO.getSelectedAnnotationsComment(), Arrays.asList("Comment 1", "Comment 2"));
        assertEquals(templateResourceDTO.getSelectedAnnotationsQuantity(), Arrays.asList(10, 20));

    }

    @Test
    public void testAppendIfDifferent() {

        TemplateResourceDTO templateResourceDTO= new TemplateResourceDTO();
        StringBuilder stringBuilder = new StringBuilder();
        String from = null;
        String to = "to";

        templateResourceDTO.appendIfDifferent(stringBuilder, "my label", from, to);
        assertEquals("{my label: null --> to}", stringBuilder.toString());

        stringBuilder = new StringBuilder();
        from = "from";
        to = "to";
        templateResourceDTO.appendIfDifferent(stringBuilder, "my label", from, to);
        assertEquals("{my label: from --> to}", stringBuilder.toString());

        stringBuilder = new StringBuilder();
        from = "same";
        to = "same";
        templateResourceDTO.appendIfDifferent(stringBuilder, "my label", from, to);
        assertEquals("", stringBuilder.toString());

        stringBuilder = new StringBuilder();
        from = "from";
        to = null;
        templateResourceDTO.appendIfDifferent(stringBuilder, "my label", from, to);
        assertEquals("{my label: from --> null}", stringBuilder.toString());

        stringBuilder = new StringBuilder();
        from = null;
        to = null;
        templateResourceDTO.appendIfDifferent(stringBuilder, "my label", from, to);
        assertEquals("", stringBuilder.toString());

    }

    @Test
    public void testDiffFromThisToOther() {

        TemplateResourceDTO templateResourceDTO1= new TemplateResourceDTO();

        templateResourceDTO1.setId(1);
        templateResourceDTO1.setVisitTemplate(99);
        templateResourceDTO1.setResource(88);
        templateResourceDTO1.setBillable(true);
        templateResourceDTO1.setAlternate(true);
        templateResourceDTO1.setFloatStart(11);
        templateResourceDTO1.setFloatEnd(12);
        templateResourceDTO1.setFloatable("true");
        templateResourceDTO1.setFlexible("false");
        templateResourceDTO1.setStartMinutes(33);
        templateResourceDTO1.setEndMinutes(44);

        templateResourceDTO1.setSelectedAnnotations(new ArrayList<>());

        TemplateResourceDTO templateResourceDTO2 = new TemplateResourceDTO();

        templateResourceDTO2.setId(1);
        templateResourceDTO2.setVisitTemplate(99);
        templateResourceDTO2.setResource(88);
        templateResourceDTO2.setBillable(true);
        templateResourceDTO2.setAlternate(true);
        templateResourceDTO2.setFloatStart(11);
        templateResourceDTO2.setFloatEnd(12);
        templateResourceDTO2.setFloatable("true");
        templateResourceDTO2.setFlexible("false");
        templateResourceDTO2.setStartMinutes(33);
        templateResourceDTO2.setEndMinutes(44);

        templateResourceDTO2.setSelectedAnnotations(new ArrayList<>());

        String result = templateResourceDTO1.diffFromThisToOther(templateResourceDTO2);

        assertEquals("No Change", result);

        templateResourceDTO1.setId(11);
        templateResourceDTO1.setVisitTemplate(999);
        templateResourceDTO1.setResource(888);
        templateResourceDTO1.setBillable(false);
        templateResourceDTO1.setAlternate(false);
        templateResourceDTO1.setFloatStart(111);
        templateResourceDTO1.setFloatEnd(123);
        templateResourceDTO1.setFloatable("false");
        templateResourceDTO1.setFlexible("true");
        templateResourceDTO1.setStartMinutes(333);
        templateResourceDTO1.setEndMinutes(444);

        templateResourceDTO1.setSelectedAnnotations(Arrays.asList(33, 44));
        templateResourceDTO1.setSelectedAnnotationsComment(Arrays.asList("Comment 1", "Comment 2"));
        templateResourceDTO1.setSelectedAnnotationsQuantity(Arrays.asList(10, 20));

        result = templateResourceDTO1.diffFromThisToOther(templateResourceDTO2);

        assertEquals(
                "{id: 11 --> 1}" +
                "{visitTemplate: 999 --> 99}" +
                "{resource: 888 --> 88}" +
                "{billable: false --> true}" +
                "{alternate: false --> true}" +
                "{floatStart: 111 --> 11}" +
                "{floatEnd: 123 --> 12}" +
                "{floatable: false --> true}" +
                "{flexible: true --> false}" +
                "{startMinutes: 333 --> 33}" +
                "{endMinutes: 444 --> 44}" +
                "{annotations: {id: 33. comment: Comment 1. quantity: 10},{id: 44. comment: Comment 2. quantity: 20} --> }",
                result
        );

    }

    @Test
    public void testAccessors() {

        TemplateResourceDTO templateResourceDTO = new TemplateResourceDTO();

        templateResourceDTO.setId(1);
        templateResourceDTO.setVisitTemplate(2);
        templateResourceDTO.setResource(3);
        templateResourceDTO.setBillable(true);
        templateResourceDTO.setAlternate(true);
        Date createdDate = new Date();
        templateResourceDTO.setCreatedDate(createdDate);
        templateResourceDTO.setDuration(4);
        templateResourceDTO.setFloatable("true");
        templateResourceDTO.setFlexible("true");
        templateResourceDTO.setFloatStart(5);
        templateResourceDTO.setFloatEnd(6);
        templateResourceDTO.setStartMinutes(7);
        templateResourceDTO.setEndMinutes(8);

        assertEquals(1 , templateResourceDTO.getId());
        assertEquals(2 , templateResourceDTO.getVisitTemplate());
        assertEquals(3 , templateResourceDTO.getResource());
        assertEquals(true , templateResourceDTO.isBillable());
        assertEquals(true , templateResourceDTO.isAlternate());
        assertEquals(createdDate, templateResourceDTO.getCreatedDate());
        assertEquals(Integer.valueOf(4) , templateResourceDTO.getDuration());
        assertEquals(Integer.valueOf(5), templateResourceDTO.getFloatStart());
        assertEquals(Integer.valueOf(6), templateResourceDTO.getFloatEnd());
        assertEquals(Integer.valueOf(7), templateResourceDTO.getStartMinutes());
        assertEquals(Integer.valueOf(8), templateResourceDTO.getEndMinutes());

        assertEquals("true" , templateResourceDTO.getFloatable());
        assertEquals("true" , templateResourceDTO.getFlexible());
        assertEquals(true, templateResourceDTO.isFlexible());
        assertEquals(true, templateResourceDTO.isFloatable());

        templateResourceDTO.setFloatable("false");
        templateResourceDTO.setFlexible("false");

        assertEquals("false" , templateResourceDTO.getFloatable());
        assertEquals("false" , templateResourceDTO.getFlexible());
        assertEquals(false, templateResourceDTO.isFlexible());
        assertEquals(false, templateResourceDTO.isFloatable());

    }

    @Test
    public void testMatches() {

        TemplateResourceDTO templateResourceDTO = new TemplateResourceDTO();

        templateResourceDTO.setBillable(true);
        templateResourceDTO.setAlternate(true);
        templateResourceDTO.setFloatable("true");
        templateResourceDTO.setFlexible("true");
        templateResourceDTO.setFloatStart(5);
        templateResourceDTO.setFloatEnd(6);
        templateResourceDTO.setStartMinutes(7);
        templateResourceDTO.setEndMinutes(8);

        TemplateResource templateResource = new TemplateResource();

        boolean match = templateResourceDTO.matches(templateResource);

        assertFalse(match);

        templateResource.setBillable(true);
        templateResource.setAlternate(true);
        templateResource.setFloatable(true);
        templateResource.setFlexible(true);
        templateResource.setFloatStart(5);
        templateResource.setFloatEnd(6);
        templateResource.setStartMinutes(7);
        templateResource.setEndMinutes(8);

        match = templateResourceDTO.matches(templateResource);

        assertTrue(match);

        // From hereon, do permutations of a single field at a time

        templateResource.setBillable(false);
        templateResource.setAlternate(true);
        templateResource.setFloatable(true);
        templateResource.setFlexible(true);
        templateResource.setFloatStart(5);
        templateResource.setFloatEnd(6);
        templateResource.setStartMinutes(7);
        templateResource.setEndMinutes(8);

        match = templateResourceDTO.matches(templateResource);

        assertFalse(match);

        templateResource.setBillable(true);
        templateResource.setAlternate(false);
        templateResource.setFloatable(true);
        templateResource.setFlexible(true);
        templateResource.setFloatStart(5);
        templateResource.setFloatEnd(6);
        templateResource.setStartMinutes(7);
        templateResource.setEndMinutes(8);

        match = templateResourceDTO.matches(templateResource);

        assertFalse(match);

        templateResource.setBillable(true);
        templateResource.setAlternate(true);
        templateResource.setFloatable(false);
        templateResource.setFlexible(true);
        templateResource.setFloatStart(5);
        templateResource.setFloatEnd(6);
        templateResource.setStartMinutes(7);
        templateResource.setEndMinutes(8);

        match = templateResourceDTO.matches(templateResource);

        assertFalse(match);

        templateResource.setBillable(true);
        templateResource.setAlternate(true);
        templateResource.setFloatable(true);
        templateResource.setFlexible(false);
        templateResource.setFloatStart(5);
        templateResource.setFloatEnd(6);
        templateResource.setStartMinutes(7);
        templateResource.setEndMinutes(8);

        match = templateResourceDTO.matches(templateResource);

        assertFalse(match);

        templateResource.setBillable(true);
        templateResource.setAlternate(true);
        templateResource.setFloatable(true);
        templateResource.setFlexible(true);
        templateResource.setFloatStart(55);
        templateResource.setFloatEnd(6);
        templateResource.setStartMinutes(7);
        templateResource.setEndMinutes(8);

        match = templateResourceDTO.matches(templateResource);

        assertFalse(match);

        templateResource.setBillable(true);
        templateResource.setAlternate(true);
        templateResource.setFloatable(true);
        templateResource.setFlexible(true);
        templateResource.setFloatStart(5);
        templateResource.setFloatEnd(66);
        templateResource.setStartMinutes(7);
        templateResource.setEndMinutes(8);

        match = templateResourceDTO.matches(templateResource);

        assertFalse(match);

        templateResource.setBillable(true);
        templateResource.setAlternate(true);
        templateResource.setFloatable(true);
        templateResource.setFlexible(true);
        templateResource.setFloatStart(5);
        templateResource.setFloatEnd(6);
        templateResource.setStartMinutes(77);
        templateResource.setEndMinutes(8);

        match = templateResourceDTO.matches(templateResource);

        assertFalse(match);

        templateResource.setBillable(true);
        templateResource.setAlternate(true);
        templateResource.setFloatable(true);
        templateResource.setFlexible(true);
        templateResource.setFloatStart(5);
        templateResource.setFloatEnd(6);
        templateResource.setStartMinutes(7);
        templateResource.setEndMinutes(88);

        match = templateResourceDTO.matches(templateResource);

        assertFalse(match);

    }

}
