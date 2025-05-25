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
package edu.harvard.catalyst.scheduler.persistence;

import edu.harvard.catalyst.scheduler.AbstractSpringWiredHibernateTest;
import edu.harvard.catalyst.scheduler.dto.response.GetSearchVisitResourceResponse;
import edu.harvard.catalyst.scheduler.dto.response.ResourceTimeBoundsAndCountResponseDTO;
import edu.harvard.catalyst.scheduler.dto.response.TemplateResourceWithTraListDTO;
import edu.harvard.catalyst.scheduler.entity.*;
import edu.harvard.catalyst.scheduler.util.TestUtils;
import org.hibernate.criterion.Restrictions;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Created by xavier on 6/30/17.
 */
public class TemplateResourceDAOTest extends AbstractSpringWiredHibernateTest {

    @Autowired
    private TemplateResourceDAO templateResourceDAO;

    @Test
    public void testFindTemplateResourceCountEarliestLatest() throws Exception {

        final ResourceTimeBoundsAndCountResponseDTO dto = templateResourceDAO.findTemplateResourceCountEarliestLatest(0);

        TestUtils.assertNonNullAndHasThisMany(dto.getDaysList(), 1);
    }

    private static VisitType makeVisitTypeIn(boolean inVsOut) {
        VisitType result = new VisitType();

        if (inVsOut) {
            result.setName(TestUtils.InpatientCRC);
            result.setInpatient(true);
        }
        else {
            result.setName(TestUtils.OutpatientCRC);
            result.setInpatient(false);
        }
        return result;
    }


    private Resource makeResource(final String name, final ResourceType resourceType) {
        final Resource result = new Resource();

        result.setName(name);
        result.setResourceType(resourceType);

        return result;
    }

    private TemplateResource makeTemplateResource(final VisitTemplate vt, final Resource r) {
        final TemplateResource result = new TemplateResource();

        result.setVisitTemplate(vt);
        result.setResource(r);

        return result;
    }

    @Test
    public void testFindRoomTemplateResourcesByVisit() {
        final VisitType visitTypeIn = makeVisitTypeIn(true);
        final VisitType visitTypeOut = makeVisitTypeIn(false);
        templateResourceDAO.createEntity(visitTypeIn);
        templateResourceDAO.createEntity(visitTypeOut);

        final VisitTemplate withRoom = new VisitTemplate.VisitTemplateBuilder()
                .visitType(visitTypeIn).create();
        final VisitTemplate withoutRoom = new VisitTemplate.VisitTemplateBuilder()
                .visitType(visitTypeOut).create();

        final Resource room = makeResource("foo", ResourceType.Room);

        final Resource nonRoom = makeResource("bar", ResourceType.Lab);

        final TemplateResource tr0 = makeTemplateResource(withRoom, room);

        final TemplateResource tr1 = makeTemplateResource(withoutRoom, nonRoom);

        templateResourceDAO.createEntity(withRoom);
        templateResourceDAO.createEntity(withoutRoom);
        templateResourceDAO.createEntity(room);
        templateResourceDAO.createEntity(nonRoom);
        templateResourceDAO.createEntity(tr0);
        templateResourceDAO.createEntity(tr1);

        final List<TemplateResource> actual = templateResourceDAO.findRoomTemplateResourcesByVisit(withRoom);

        assertEquals(withRoom.getId(), actual.get(0).getVisitTemplate().getId());

        assertEquals(1, actual.size());

    }

    // This test fails when executed as part of a suite. Alone it seems to work fine.
//    @Test
//    public void testFindTemplateResourcesByVisitAndResource2() {
//
//        final VisitType visitTypeIn = makeVisitTypeIn(true);
//        final VisitType visitTypeOut = makeVisitTypeIn(false);
//        templateResourceDAO.createEntity(visitTypeIn);
//        templateResourceDAO.createEntity(visitTypeOut);
//
//        final Resource resource1 = makeResource("foo", ResourceType.Room);
//        final Resource resource2 = makeResource("foo", ResourceType.Room);
//        templateResourceDAO.createEntity(resource1);
//        templateResourceDAO.createEntity(resource2);
//
//        final VisitTemplate visitTemplate1 = new VisitTemplate.VisitTemplateBuilder()
//                .visitType(visitTypeIn).create();
//        final VisitTemplate visitTemplate2 = new VisitTemplate.VisitTemplateBuilder()
//                .visitType(visitTypeOut).create();
//        templateResourceDAO.createEntity(visitTemplate1);
//        templateResourceDAO.createEntity(visitTemplate2);
//
//        final TemplateResource templateResource1 = makeTemplateResource(visitTemplate1, resource1);
//        final TemplateResource templateResource2 = makeTemplateResource(visitTemplate2, resource2);
//        templateResourceDAO.createEntity(templateResource1);
//        templateResourceDAO.createEntity(templateResource2);
//
//        int resourceId1 = resource1.getId();
//        int visitTemplateId1 = visitTemplate1.getId();
//
//        List<TemplateResource> templateResources = templateResourceDAO
//                .findTemplateResourcesByVisitAndResource(resourceId1, visitTemplateId1);
//
//        assertEquals(1, templateResources.size());
//        assertEquals(templateResource1.getId(), templateResources.get(0).getId());
//
//    }

    @Test
    public void testGetTemplateResourcesUsedOnDay() {

        final VisitType visitTypeIn = makeVisitTypeIn(true);
        final VisitType visitTypeOut = makeVisitTypeIn(false);
        templateResourceDAO.createEntity(visitTypeIn);
        templateResourceDAO.createEntity(visitTypeOut);

        final VisitTemplate withRoom = new VisitTemplate.VisitTemplateBuilder()
                .visitType(visitTypeIn).create();
        final VisitTemplate withoutRoom = new VisitTemplate.VisitTemplateBuilder()
                .visitType(visitTypeOut).create();

        final Resource room = makeResource("foo", ResourceType.Room);
        final Resource nonRoom = makeResource("bar", ResourceType.Lab);

        final TemplateResource tr0 = makeTemplateResource(withRoom, room);
        tr0.setStartMinutes(0);
        tr0.setEndMinutes(100);
        final TemplateResource tr1 = makeTemplateResource(withRoom, room);
        tr1.setStartMinutes(10);
        tr1.setEndMinutes(1010);

        templateResourceDAO.createEntity(withRoom);
        templateResourceDAO.createEntity(withoutRoom);
        templateResourceDAO.createEntity(room);
        templateResourceDAO.createEntity(nonRoom);
        templateResourceDAO.createEntity(tr0);
        templateResourceDAO.createEntity(tr1);

        List<TemplateResource> actual = templateResourceDAO.getTemplateResourcesUsedOnDay(
                withRoom.getId(),
                0,
                1000);

        assertEquals(1, actual.size());
        assertEquals(tr0.getId(), actual.get(0).getId());
        assertEquals(withRoom.getId(), actual.get(0).getVisitTemplate().getId());

    }

    @Test
    public void testFindTemplateResourcesByVisit1() {

        final VisitType visitTypeIn = makeVisitTypeIn(true);
        final VisitType visitTypeOut = makeVisitTypeIn(false);
        templateResourceDAO.createEntity(visitTypeIn);
        templateResourceDAO.createEntity(visitTypeOut);

        final VisitTemplate withRoom = new VisitTemplate.VisitTemplateBuilder().visitType(visitTypeIn).create();
        final VisitTemplate withoutRoom = new VisitTemplate.VisitTemplateBuilder().visitType(visitTypeOut).create();

        final Resource room = makeResource("foo", ResourceType.Room);
        final Resource nonRoom = makeResource("bar", ResourceType.Lab);

        final TemplateResource tr0 = makeTemplateResource(withRoom, room);
        final TemplateResource tr1 = makeTemplateResource(withRoom, room);
        final TemplateResource tr2 = makeTemplateResource(withoutRoom, nonRoom);

        templateResourceDAO.createEntity(withRoom);
        templateResourceDAO.createEntity(withoutRoom);
        templateResourceDAO.createEntity(room);
        templateResourceDAO.createEntity(nonRoom);
        templateResourceDAO.createEntity(tr0);
        templateResourceDAO.createEntity(tr1);
        templateResourceDAO.createEntity(tr2);

        List<TemplateResource> results = templateResourceDAO.findTemplateResourcesByVisit(withRoom);

        assertEquals(2, results.size());

    }


    @Test
    public void testFindTemplateResourcesByVisitAndBillable() {

        final VisitType visitTypeIn = makeVisitTypeIn(true);
        final VisitType visitTypeOut = makeVisitTypeIn(false);
        templateResourceDAO.createEntity(visitTypeIn);
        templateResourceDAO.createEntity(visitTypeOut);

        final VisitTemplate withRoom = new VisitTemplate.VisitTemplateBuilder().visitType(visitTypeIn).create();
        final VisitTemplate withoutRoom = new VisitTemplate.VisitTemplateBuilder().visitType(visitTypeOut).create();

        final Resource room = makeResource("foo", ResourceType.Room);
        final Resource nonRoom = makeResource("bar", ResourceType.Lab);

        final TemplateResource tr0 = makeTemplateResource(withRoom, room);
        final TemplateResource tr1 = makeTemplateResource(withRoom, room);
        final TemplateResource tr2 = makeTemplateResource(withoutRoom, nonRoom);
        final TemplateResource tr3 = makeTemplateResource(withoutRoom, nonRoom);
        final TemplateResource tr4 = makeTemplateResource(withoutRoom, nonRoom);
        final TemplateResource tr5 = makeTemplateResource(withoutRoom, nonRoom);
        final TemplateResource tr6 = makeTemplateResource(withoutRoom, nonRoom);
        final TemplateResource tr7 = makeTemplateResource(withoutRoom, nonRoom);
        tr6.setBillable(true);
        tr7.setBillable(true);

        templateResourceDAO.createEntity(withRoom);
        templateResourceDAO.createEntity(withoutRoom);
        templateResourceDAO.createEntity(room);
        templateResourceDAO.createEntity(nonRoom);
        templateResourceDAO.createEntity(tr0);
        templateResourceDAO.createEntity(tr1);
        templateResourceDAO.createEntity(tr2); // should be in results list
        templateResourceDAO.createEntity(tr3); // should be in results list
        templateResourceDAO.createEntity(tr4); // should be in results list
        templateResourceDAO.createEntity(tr5); // should be in results list
        templateResourceDAO.createEntity(tr6);
        templateResourceDAO.createEntity(tr7);

        // Note: page and paxResults arguments are not used. We're passing -1 just to make sure they don't affect anything
        List<TemplateResource> results = templateResourceDAO.findTemplateResourcesByVisitAndBillable(
                withoutRoom, false, "tr.id", "desc", -1, -1
        );

        results.get(0).getId();

        assertEquals(4, results.size());
        assertEquals(tr5.getId(), results.get(0).getId());
        assertEquals(tr4.getId(), results.get(1).getId());
        assertEquals(tr3.getId(), results.get(2).getId());
        assertEquals(tr2.getId(), results.get(3).getId());

    }

    @Test
    public void testFindTemplateResourcesByVisitAndResource() {

        final VisitType visitTypeIn = makeVisitTypeIn(true);
        final VisitType visitTypeOut = makeVisitTypeIn(false);
        templateResourceDAO.createEntity(visitTypeIn);
        templateResourceDAO.createEntity(visitTypeOut);

        final VisitTemplate withRoom = new VisitTemplate.VisitTemplateBuilder().visitType(visitTypeIn).create();
        final VisitTemplate withoutRoom = new VisitTemplate.VisitTemplateBuilder().visitType(visitTypeOut).create();

        final Resource room = makeResource("foo", ResourceType.Room);
        final Resource nonRoom = makeResource("bar", ResourceType.Lab);

        final TemplateResource tr0 = makeTemplateResource(withRoom, room);
        final TemplateResource tr1 = makeTemplateResource(withRoom, room);
        final TemplateResource tr2 = makeTemplateResource(withoutRoom, nonRoom);
        final TemplateResource tr3 = makeTemplateResource(withoutRoom, nonRoom);

        final TemplateResourceAnnotations tra0 = new TemplateResourceAnnotations();
        tra0.setTemplateResource(tr0);
        final TemplateResourceAnnotations tra1 = new TemplateResourceAnnotations();
        tra1.setTemplateResource(tr0);
        final TemplateResourceAnnotations tra2 = new TemplateResourceAnnotations();
        tra2.setTemplateResource(tr1);
        final TemplateResourceAnnotations tra3 = new TemplateResourceAnnotations();
        tra3.setTemplateResource(tr1);
        final TemplateResourceAnnotations tra4 = new TemplateResourceAnnotations();
        tra4.setTemplateResource(tr1);

        templateResourceDAO.createEntity(withRoom);
        templateResourceDAO.createEntity(withoutRoom);
        templateResourceDAO.createEntity(room);
        templateResourceDAO.createEntity(nonRoom);
        templateResourceDAO.createEntity(tr0);
        templateResourceDAO.createEntity(tr1);
        templateResourceDAO.createEntity(tr2);
        templateResourceDAO.createEntity(tr3);
        templateResourceDAO.createEntity(tra0);
        templateResourceDAO.createEntity(tra1);
        templateResourceDAO.createEntity(tra2);
        templateResourceDAO.createEntity(tra3);
        templateResourceDAO.createEntity(tra4);

        List<TemplateResourceWithTraListDTO> results = templateResourceDAO
                .findTemplateResourcesAndAnnotationsByVisit(withRoom.getId(), "order by tra.id desc");

        assertEquals(2, results.size());

        TemplateResourceWithTraListDTO result1 = results.get(0);
        TemplateResourceWithTraListDTO result2 = results.get(1);
        assertEquals(tr1.getId(), result1.getTemplateResource().getId());
        assertEquals(tr0.getId(), result2.getTemplateResource().getId());

        // note that the query specified the results to be ordered by annotations' id in reverse order
        List<TemplateResourceAnnotations> traList1 = result1.getTraList();
        List<TemplateResourceAnnotations> traList2 = result2.getTraList();
        assertEquals(3, traList1.size());
        assertEquals(2, traList2.size());

        assertEquals(tra4.getId(), traList1.get(0).getId());
        assertEquals(tra3.getId(), traList1.get(1).getId());
        assertEquals(tra2.getId(), traList1.get(2).getId());
        assertEquals(tra1.getId(), traList2.get(0).getId());
        assertEquals(tra0.getId(), traList2.get(1).getId());

    }

    @Test
    public void testFindTemplateResourcesByVisit2() {

        final VisitType visitTypeIn = makeVisitTypeIn(true);
        final VisitType visitTypeOut = makeVisitTypeIn(false);
        templateResourceDAO.createEntity(visitTypeIn);
        templateResourceDAO.createEntity(visitTypeOut);

        final VisitTemplate visitTemplate1 = new VisitTemplate.VisitTemplateBuilder().visitType(visitTypeIn).create();
        final VisitTemplate visitTemplate2 = new VisitTemplate.VisitTemplateBuilder().visitType(visitTypeOut).create();

        final Resource resource1 = makeResource("foo", ResourceType.Room);
        final Resource resource2 = makeResource("bar", ResourceType.Lab);

        final TemplateResource tr1 = makeTemplateResource(visitTemplate1, resource1);
        tr1.setStartMinutes(10);
        tr1.setEndMinutes(11);
        final TemplateResource tr2 = makeTemplateResource(visitTemplate1, resource1);
        tr2.setStartMinutes(5);
        tr2.setEndMinutes(15);
        final TemplateResource tr3 = makeTemplateResource(visitTemplate1, resource1);
        tr3.setStartMinutes(15);
        tr3.setEndMinutes(20);
        final TemplateResource tr4 = makeTemplateResource(visitTemplate1, resource1);
        tr4.setStartMinutes(10);
        tr4.setEndMinutes(25);
        final TemplateResource tr5 = makeTemplateResource(visitTemplate1, resource1);
        tr5.setStartMinutes(10);
        tr5.setEndMinutes(15);
        final TemplateResource tr6 = makeTemplateResource(visitTemplate2, resource2);
        tr6.setStartMinutes(10);
        tr6.setEndMinutes(15);
        final TemplateResource tr7 = makeTemplateResource(visitTemplate2, resource1);
        tr7.setStartMinutes(10);
        tr7.setEndMinutes(15);

        templateResourceDAO.createEntity(visitTemplate1);
        templateResourceDAO.createEntity(visitTemplate2);
        templateResourceDAO.createEntity(resource1);
        templateResourceDAO.createEntity(resource2);
        templateResourceDAO.createEntity(tr1);
        templateResourceDAO.createEntity(tr2);
        templateResourceDAO.createEntity(tr3);
        templateResourceDAO.createEntity(tr4);
        templateResourceDAO.createEntity(tr5);
        templateResourceDAO.createEntity(tr6);
        templateResourceDAO.createEntity(tr7);

        // case 1a

        GetSearchVisitResourceResponse response = templateResourceDAO.findTemplateResourcesByVisit(
                visitTemplate1.getId(),
                "tr.startMinutes",
                "desc",
                1,
                3
        );

        List<GetSearchVisitResourceResponse.TemplateResource1> templateResource1List = response.templateResource1s;

        assertEquals(3, templateResource1List.size());
        assertEquals(tr3.getId(), templateResource1List.get(0).id);
        assertEquals(tr4.getId(), templateResource1List.get(1).id);
        assertEquals(tr5.getId(), templateResource1List.get(2).id);

        // case 1b

        response = templateResourceDAO.findTemplateResourcesByVisit(
                visitTemplate1.getId(),
                "tr.startMinutes",
                "desc",
                2,
                3
        );

        templateResource1List = response.templateResource1s;

        assertEquals(2, templateResource1List.size());
        assertEquals(tr1.getId(), templateResource1List.get(0).id);
        assertEquals(tr2.getId(), templateResource1List.get(1).id);

        // case 2a

        response = templateResourceDAO.findTemplateResourcesByVisit(
                visitTemplate1.getId(),
                "tr.endMinutes",
                "asc",
                1,
                3
        );

        templateResource1List = response.templateResource1s;

        assertEquals(3, templateResource1List.size());
        assertEquals(tr1.getId(), templateResource1List.get(0).id);
        assertEquals(tr2.getId(), templateResource1List.get(1).id);
        assertEquals(tr5.getId(), templateResource1List.get(2).id);

        // case 2b

        response = templateResourceDAO.findTemplateResourcesByVisit(
                visitTemplate1.getId(),
                "tr.endMinutes",
                "asc",
                2,
                3
        );

        templateResource1List = response.templateResource1s;

        assertEquals(2, templateResource1List.size());
        assertEquals(tr3.getId(), templateResource1List.get(0).id);
        assertEquals(tr4.getId(), templateResource1List.get(1).id);

        // case 3: arbitrary "order by"

        response = templateResourceDAO.findTemplateResourcesByVisit(
                visitTemplate2.getId(),
                "r.id",
                "asc",
                1,
                10
        );

        templateResource1List = response.templateResource1s;

        assertEquals(2, templateResource1List.size());
        assertEquals(tr7.getId(), templateResource1List.get(0).id);
        assertEquals(tr6.getId(), templateResource1List.get(1).id);

    }

    @Test
    public void testFindTemplateResourceAnnotationsByTemplateResource() {

        final VisitType visitTypeIn = makeVisitTypeIn(true);
        final VisitType visitTypeOut = makeVisitTypeIn(false);
        templateResourceDAO.createEntity(visitTypeIn);
        templateResourceDAO.createEntity(visitTypeOut);

        final VisitTemplate visitTemplate1 = new VisitTemplate.VisitTemplateBuilder().visitType(visitTypeIn).create();
        final VisitTemplate visitTemplate2 = new VisitTemplate.VisitTemplateBuilder().visitType(visitTypeOut).create();

        final Resource resource1 = makeResource("foo", ResourceType.Room);
        final Resource resource2 = makeResource("bar", ResourceType.Lab);

        final TemplateResource tr1 = makeTemplateResource(visitTemplate1, resource1);
        tr1.setStartMinutes(10);
        tr1.setEndMinutes(11);
        final TemplateResource tr2 = makeTemplateResource(visitTemplate1, resource1);
        tr2.setStartMinutes(5);
        tr2.setEndMinutes(15);

        final TemplateResourceAnnotations tra1 = new TemplateResourceAnnotations();
        tra1.setTemplateResource(tr1);
        final TemplateResourceAnnotations tra2 = new TemplateResourceAnnotations();
        tra2.setTemplateResource(tr1);
        final TemplateResourceAnnotations tra3 = new TemplateResourceAnnotations();
        tra3.setTemplateResource(tr1);
        final TemplateResourceAnnotations tra4 = new TemplateResourceAnnotations();
        tra4.setTemplateResource(tr2);

        templateResourceDAO.createEntity(visitTemplate1);
        templateResourceDAO.createEntity(visitTemplate2);
        templateResourceDAO.createEntity(resource1);
        templateResourceDAO.createEntity(resource2);
        templateResourceDAO.createEntity(tr1);
        templateResourceDAO.createEntity(tr2);
        templateResourceDAO.createEntity(tra1);
        templateResourceDAO.createEntity(tra2);
        templateResourceDAO.createEntity(tra3);
        templateResourceDAO.createEntity(tra4);

        List<TemplateResourceAnnotations> response = templateResourceDAO.findTemplateResourceAnnotationsByTemplateResource(tr2);

        assertEquals(1, response.size());

        assertEquals(tra4.getId(), response.get(0).getId());

        response = templateResourceDAO.findTemplateResourceAnnotationsByTemplateResource(tr1);

        assertEquals(3, response.size());

        assertEquals(tra1.getId(), response.get(0).getId());
        assertEquals(tra2.getId(), response.get(1).getId());
        assertEquals(tra3.getId(), response.get(2).getId());

    }

    @Test
    public void testFindTemplateResourceLowest() {

        final VisitType visitTypeIn = makeVisitTypeIn(true);
        final VisitType visitTypeOut = makeVisitTypeIn(false);
        templateResourceDAO.createEntity(visitTypeIn);
        templateResourceDAO.createEntity(visitTypeOut);

        final VisitTemplate visitTemplate1 = new VisitTemplate.VisitTemplateBuilder().visitType(visitTypeIn).create();
        final VisitTemplate visitTemplate2 = new VisitTemplate.VisitTemplateBuilder().visitType(visitTypeOut).create();

        final Resource resource1 = makeResource("foo", ResourceType.Room);
        final Resource resource2 = makeResource("bar", ResourceType.Lab);

        final TemplateResource tr1 = makeTemplateResource(visitTemplate1, resource1);
        tr1.setStartMinutes(10);
        final TemplateResource tr2 = makeTemplateResource(visitTemplate1, resource1);
        tr2.setStartMinutes(5);
        final TemplateResource tr3 = makeTemplateResource(visitTemplate1, resource1);
        tr3.setStartMinutes(15);
        final TemplateResource tr4 = makeTemplateResource(visitTemplate1, resource1);
        tr4.setStartMinutes(20);
        final TemplateResource tr5 = makeTemplateResource(visitTemplate1, resource1);
        tr5.setStartMinutes(25);
        final TemplateResource tr6 = makeTemplateResource(visitTemplate2, resource2);
        tr6.setStartMinutes(20);
        final TemplateResource tr7 = makeTemplateResource(visitTemplate2, resource1);
        tr7.setStartMinutes(0);

        templateResourceDAO.createEntity(visitTemplate1);
        templateResourceDAO.createEntity(visitTemplate2);
        templateResourceDAO.createEntity(resource1);
        templateResourceDAO.createEntity(resource2);
        templateResourceDAO.createEntity(tr1);
        templateResourceDAO.createEntity(tr2);
        templateResourceDAO.createEntity(tr3);
        templateResourceDAO.createEntity(tr4);
        templateResourceDAO.createEntity(tr5);
        templateResourceDAO.createEntity(tr6);
        templateResourceDAO.createEntity(tr7);

        TemplateResource response = templateResourceDAO.findTemplateResourceLowest(visitTemplate1);

        assertEquals(tr2.getId(), response.getId());

    }

    @Test
    public void testFindFixedTemplateResourcesByVisit() {

        final VisitType visitTypeIn = makeVisitTypeIn(true);
        final VisitType visitTypeOut = makeVisitTypeIn(false);
        templateResourceDAO.createEntity(visitTypeIn);
        templateResourceDAO.createEntity(visitTypeOut);

        final VisitTemplate visitTemplate1 = new VisitTemplate.VisitTemplateBuilder().visitType(visitTypeIn).create();
        final VisitTemplate visitTemplate2 = new VisitTemplate.VisitTemplateBuilder().visitType(visitTypeOut).create();

        final Resource resource1 = makeResource("foo", ResourceType.Room);
        final Resource resource2 = makeResource("bar", ResourceType.Lab);

        final TemplateResource tr1 = makeTemplateResource(visitTemplate1, resource1);
        tr1.setGroupId(null);
        tr1.setFloatable(false);
        final TemplateResource tr2 = makeTemplateResource(visitTemplate1, resource1);
        tr2.setGroupId(null);
        tr2.setFloatable(true);
        final TemplateResource tr3 = makeTemplateResource(visitTemplate1, resource1);
        tr3.setGroupId("");
        tr3.setFloatable(false);
        final TemplateResource tr4 = makeTemplateResource(visitTemplate1, resource1);
        tr4.setGroupId(null);
        tr4.setFloatable(true);
        final TemplateResource tr5 = makeTemplateResource(visitTemplate1, resource1);
        tr5.setGroupId(null);
        tr5.setFloatable(false);
        final TemplateResource tr6 = makeTemplateResource(visitTemplate2, resource2);
        tr6.setGroupId(null);
        tr6.setFloatable(false);
        final TemplateResource tr7 = makeTemplateResource(visitTemplate2, resource1);
        tr7.setGroupId(null);
        tr7.setFloatable(false);

        templateResourceDAO.createEntity(visitTemplate1);
        templateResourceDAO.createEntity(visitTemplate2);
        templateResourceDAO.createEntity(resource1);
        templateResourceDAO.createEntity(resource2);
        templateResourceDAO.createEntity(tr1);
        templateResourceDAO.createEntity(tr2);
        templateResourceDAO.createEntity(tr3);
        templateResourceDAO.createEntity(tr4);
        templateResourceDAO.createEntity(tr5);
        templateResourceDAO.createEntity(tr6);
        templateResourceDAO.createEntity(tr7);

        List<TemplateResource> response = templateResourceDAO.findFixedTemplateResourcesByVisit(visitTemplate1);

        assertEquals(2, response.size());

        assertEquals(tr1.getId(), response.get(0).getId());
        assertEquals(tr5.getId(), response.get(1).getId());

    }

    @Test
    public void testFindUngroupedTemplateResourcesTypeByVisit() {

        final VisitType visitTypeIn = makeVisitTypeIn(true);
        final VisitType visitTypeOut = makeVisitTypeIn(false);
        templateResourceDAO.createEntity(visitTypeIn);
        templateResourceDAO.createEntity(visitTypeOut);

        final VisitTemplate visitTemplate1 = new VisitTemplate.VisitTemplateBuilder().visitType(visitTypeIn).create();
        final VisitTemplate visitTemplate2 = new VisitTemplate.VisitTemplateBuilder().visitType(visitTypeOut).create();

        final Resource resource1 = makeResource("foo", ResourceType.Room);
        final Resource resource2 = makeResource("bar", ResourceType.Lab);

        final TemplateResource tr1 = makeTemplateResource(visitTemplate1, resource1);
        tr1.setGroupId(null);
        tr1.setAlternate(true);
        final TemplateResource tr2 = makeTemplateResource(visitTemplate1, resource1);
        tr2.setGroupId(null);
        tr2.setFloatable(true);
        final TemplateResource tr3 = makeTemplateResource(visitTemplate1, resource1);
        tr3.setGroupId("");
        tr3.setFlexible(true);
        final TemplateResource tr4 = makeTemplateResource(visitTemplate1, resource1);
        tr4.setGroupId(null);
        tr4.setBillable(true);
        final TemplateResource tr5 = makeTemplateResource(visitTemplate1, resource1);
        tr5.setGroupId(null);
        tr5.setFloatable(true);
        final TemplateResource tr6 = makeTemplateResource(visitTemplate2, resource2);
        tr6.setGroupId(null);
        tr6.setFlexible(true);
        final TemplateResource tr7 = makeTemplateResource(visitTemplate2, resource1);
        tr7.setGroupId(null);
        tr7.setFloatable(true);

        templateResourceDAO.createEntity(visitTemplate1);
        templateResourceDAO.createEntity(visitTemplate2);
        templateResourceDAO.createEntity(resource1);
        templateResourceDAO.createEntity(resource2);
        templateResourceDAO.createEntity(tr1);
        templateResourceDAO.createEntity(tr2);
        templateResourceDAO.createEntity(tr3);
        templateResourceDAO.createEntity(tr4);
        templateResourceDAO.createEntity(tr5);
        templateResourceDAO.createEntity(tr6);
        templateResourceDAO.createEntity(tr7);

        List<TemplateResource> response = templateResourceDAO.findUngroupedTemplateResourcesTypeByVisit(visitTemplate1, "alternate");

        assertEquals(1, response.size());
        assertEquals(tr1.getId(), response.get(0).getId());

        response = templateResourceDAO.findUngroupedTemplateResourcesTypeByVisit(visitTemplate1, "floatable");

        assertEquals(2, response.size());
        assertEquals(tr2.getId(), response.get(0).getId());
        assertEquals(tr5.getId(), response.get(1).getId());

        response = templateResourceDAO.findUngroupedTemplateResourcesTypeByVisit(visitTemplate1, "flexible");

        assertEquals(0, response.size());

        response = templateResourceDAO.findUngroupedTemplateResourcesTypeByVisit(visitTemplate1, "billable");

        assertEquals(1, response.size());
        assertEquals(tr4.getId(), response.get(0).getId());

    }

    @Test
    public void testFindTemplateResourceGroupByVisitAndTemplate() {

        final VisitType visitTypeIn = makeVisitTypeIn(true);
        final VisitType visitTypeOut = makeVisitTypeIn(false);
        templateResourceDAO.createEntity(visitTypeIn);
        templateResourceDAO.createEntity(visitTypeOut);

        final VisitTemplate visitTemplate1 = new VisitTemplate.VisitTemplateBuilder().visitType(visitTypeIn).create();
        final VisitTemplate visitTemplate2 = new VisitTemplate.VisitTemplateBuilder().visitType(visitTypeOut).create();

        final Resource resource1 = makeResource("foo", ResourceType.Room);
        final Resource resource2 = makeResource("bar", ResourceType.Lab);

        final TemplateResource tr1 = makeTemplateResource(visitTemplate1, resource1);
        tr1.setGroupId(null);
        tr1.setAlternate(true);
        final TemplateResource tr2 = makeTemplateResource(visitTemplate1, resource1);
        tr2.setGroupId(null);
        tr2.setFloatable(true);
        final TemplateResource tr3 = makeTemplateResource(visitTemplate1, resource1);
        tr3.setGroupId("");
        tr3.setFlexible(true);
        final TemplateResource tr4 = makeTemplateResource(visitTemplate1, resource1);
        tr4.setGroupId(null);
        tr4.setBillable(true);
        final TemplateResource tr5 = makeTemplateResource(visitTemplate1, resource1);
        tr5.setGroupId(null);
        tr5.setFloatable(true);
        final TemplateResource tr6 = makeTemplateResource(visitTemplate2, resource2);
        tr6.setGroupId(null);
        tr6.setFlexible(true);
        final TemplateResource tr7 = makeTemplateResource(visitTemplate2, resource1);
        tr7.setGroupId(null);
        tr7.setFloatable(true);

        final TemplateResourceGroup trg1 = new TemplateResourceGroup();
        trg1.setTemplateResource(tr1);
        trg1.setVisit(visitTemplate1);
        final TemplateResourceGroup trg2 = new TemplateResourceGroup();
        trg2.setTemplateResource(tr1);
        trg2.setVisit(visitTemplate2);
        final TemplateResourceGroup trg3 = new TemplateResourceGroup();
        trg3.setTemplateResource(tr2);
        trg3.setVisit(visitTemplate1);
        final TemplateResourceGroup trg4 = new TemplateResourceGroup();
        trg4.setTemplateResource(tr1);
        trg4.setVisit(visitTemplate1);

        templateResourceDAO.createEntity(visitTemplate1);
        templateResourceDAO.createEntity(visitTemplate2);
        templateResourceDAO.createEntity(resource1);
        templateResourceDAO.createEntity(resource2);
        templateResourceDAO.createEntity(tr1);
        templateResourceDAO.createEntity(tr2);
        templateResourceDAO.createEntity(tr3);
        templateResourceDAO.createEntity(tr4);
        templateResourceDAO.createEntity(tr5);
        templateResourceDAO.createEntity(tr6);
        templateResourceDAO.createEntity(tr7);
        templateResourceDAO.createEntity(trg1);
        templateResourceDAO.createEntity(trg2);
        templateResourceDAO.createEntity(trg3);
        templateResourceDAO.createEntity(trg4);

        List<TemplateResourceGroup> response = templateResourceDAO.findTemplateResourceGroupByVisitAndTemplate(visitTemplate1, tr1);

        assertEquals(2, response.size());

        assertEquals(trg1.getId(), response.get(0).getId());
        assertEquals(trg4.getId(), response.get(1).getId());

    }

    @Test
    public void testFindTemplateResourceById() {

        final VisitType visitTypeIn = makeVisitTypeIn(true);
        final VisitType visitTypeOut = makeVisitTypeIn(false);
        templateResourceDAO.createEntity(visitTypeIn);
        templateResourceDAO.createEntity(visitTypeOut);

        final VisitTemplate visitTemplate1 = new VisitTemplate.VisitTemplateBuilder().visitType(visitTypeIn).create();
        final VisitTemplate visitTemplate2 = new VisitTemplate.VisitTemplateBuilder().visitType(visitTypeOut).create();

        final Resource resource1 = makeResource("foo", ResourceType.Room);
        final Resource resource2 = makeResource("bar", ResourceType.Lab);

        final TemplateResource tr1 = makeTemplateResource(visitTemplate1, resource1);
        tr1.setGroupId(null);
        tr1.setAlternate(true);
        final TemplateResource tr2 = makeTemplateResource(visitTemplate1, resource1);
        tr2.setGroupId(null);
        tr2.setFloatable(true);

        templateResourceDAO.createEntity(visitTemplate1);
        templateResourceDAO.createEntity(visitTemplate2);
        templateResourceDAO.createEntity(resource1);
        templateResourceDAO.createEntity(resource2);
        templateResourceDAO.createEntity(tr1);
        templateResourceDAO.createEntity(tr2);

        TemplateResource response = templateResourceDAO.findTemplateResourceById(tr2.getId());

        assertEquals(tr2.getId(), response.getId());

    }

}
