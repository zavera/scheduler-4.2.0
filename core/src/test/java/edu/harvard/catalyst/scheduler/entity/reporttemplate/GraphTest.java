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
package edu.harvard.catalyst.scheduler.entity.reporttemplate;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import edu.harvard.catalyst.scheduler.core.SchedulerRuntimeException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: carl
 * Date: 10/22/14
 * Time: 10:38 AM
 */
public class GraphTest {

    static int G1_NUM_VERTICES = 10;
    static Graph graph1;
    static List<Edge> graph1Edges;

    @Before
    public void setup() {
        setupGraph1();
    }

    static void setupGraph1() {
        List<Vertex> vertices = new ArrayList<Vertex>();
        for (int i = 0; i < G1_NUM_VERTICES; i++) {
            vertices.add(new Vertex("nm" + i, "sn" + i));
        }

        graph1Edges = new ArrayList<Edge>();
        for (int i = 0; i < G1_NUM_VERTICES; i++) {
            if (i + 1 < G1_NUM_VERTICES) {
                Edge edge = new Edge(
                        vertices.get(i),
                        vertices.get(i + 1),
                        Edge.JoinType.INNER,
                        "field"
                );
                graph1Edges.add(edge);
            }
            if (i == 0 && i + 2 < G1_NUM_VERTICES) {
                Edge edge = new Edge(
                        vertices.get(i),
                        vertices.get(i + 2),
                        Edge.JoinType.INNER,
                        "field"
                );
                graph1Edges.add(edge);
            }
        }

        graph1 = new Graph(new LinkedHashSet<Edge>(graph1Edges), vertices.get(0), true);
    }

    @Test
    public void testGraph1() throws Exception {
        String dumpGraph = graph1.toString();

        Assert.assertEquals("Graph:\n" +
                "0:nm0 sn0 <- (inner join) sn0.field = sn1.id -> 1:nm1 sn1\n" +
                "0:nm0 sn0 <- (inner join) sn0.field = sn2.id -> 2:nm2 sn2\n" +
                "1:nm1 sn1 <- (inner join) sn1.field = sn2.id -> 2:nm2 sn2\n" +
                "2:nm2 sn2 <- (inner join) sn2.field = sn3.id -> 3:nm3 sn3\n" +
                "3:nm3 sn3 <- (inner join) sn3.field = sn4.id -> 4:nm4 sn4\n" +
                "4:nm4 sn4 <- (inner join) sn4.field = sn5.id -> 5:nm5 sn5\n" +
                "5:nm5 sn5 <- (inner join) sn5.field = sn6.id -> 6:nm6 sn6\n" +
                "6:nm6 sn6 <- (inner join) sn6.field = sn7.id -> 7:nm7 sn7\n" +
                "7:nm7 sn7 <- (inner join) sn7.field = sn8.id -> 8:nm8 sn8\n" +
                "8:nm8 sn8 <- (inner join) sn8.field = sn9.id -> 9:nm9 sn9", dumpGraph);

        return;
    }

    @Test
    public void testConnectedSubgraph() throws Exception {

        Vertex zero = graph1Edges.get(0).getPointOne();
        Vertex two = graph1Edges.get(1).getPointTwo();
        Vertex three = graph1Edges.get(4).getPointOne();
        Vertex four = graph1Edges.get(5).getPointOne();
        Vertex five = graph1Edges.get(6).getPointOne();

        Set<Vertex> candidates = new HashSet<Vertex>();
        Set<Vertex> connectionExpected = new HashSet<Vertex>();

        candidates.add(zero);
        candidates.add(five);

        // no 'one' b/c looking for shortest connection
        connectionExpected.add(zero);
        connectionExpected.add(two);
        connectionExpected.add(three);
        connectionExpected.add(four);
        connectionExpected.add(five);

        Graph smallConnectedGraph = graph1.getSmallConnectedSubgraph(candidates);
        Set<Vertex> connectionActual = smallConnectedGraph.getVertices();

        Assert.assertEquals(connectionExpected, connectionActual);

        candidates.clear();
        candidates.add(zero);

        // one vertex is a trivial subgraph
        Graph trivialGraph = graph1.getSmallConnectedSubgraph(candidates);
        connectionActual = trivialGraph.getVertices();
        Assert.assertEquals(candidates, connectionActual);

        // negative test -- bogus (non-present) vertex
        Vertex bogus = new Vertex("bogus", "bg");
        candidates.add(bogus);
        try {
            graph1.isConnected(candidates);
            Assert.assertTrue("Bogus vertex shouldn't let us get here", false);
        } catch (SchedulerRuntimeException se) {
            Assert.assertEquals(Graph.ALIEN_VERTEX, se.getMessage());
        }

    }

    @Test
    public void testConnected() throws Exception {
        Set<Edge> guineaEdges = new HashSet<Edge>();

        boolean connected = graph1.isConnected();
        Assert.assertTrue(connected);

        Graph guineaGraph;

        Edge edge0 = graph1Edges.get(0);
        Vertex vertex0 = edge0.getPointOne();

        guineaEdges.add(this.graph1Edges.get(0));
        guineaEdges.add(this.graph1Edges.get(1));
        guineaEdges.add(this.graph1Edges.get(2));

        guineaGraph = new Graph(guineaEdges, vertex0, true);
        connected = guineaGraph.isConnected();
        Assert.assertTrue(connected);

        guineaEdges.clear();
        guineaEdges.add(this.graph1Edges.get(0));
        guineaEdges.add(this.graph1Edges.get(2));

        guineaGraph = new Graph(guineaEdges, vertex0, true);
        connected = guineaGraph.isConnected();
        Assert.assertTrue(connected);

        Set<Vertex> disconnectedVertices = new HashSet<Vertex>();
        disconnectedVertices.add(graph1Edges.get(0).getPointOne());
        disconnectedVertices.add(graph1Edges.get(7).getPointOne());

        connected = graph1.isConnected(disconnectedVertices);
        Assert.assertTrue(!connected);
    }

    @Test
    public void testVisitTemplateGraph() throws Exception {
        StudyAndVisitTemplateGraph visitTemplateGraph = StudyAndVisitTemplateGraph.getInstance();

        String expected = "" +
                "Graph:\n" +
                "0:study s <- (left join) vt.study = s.id -> 1:visit_template vt\n" +
                "0:study s <- (left join) s.principal_investigator = u_inv.id -> 6:user u_inv\n" +
                "0:study s <- (left join) s.primary_scheduling_contact = u_sch.id -> 7:user u_sch\n" +
                "0:study s <- (left join) s.protocol_nurse = u_nrs.id -> 8:user u_nrs\n" +
                "0:study s <- (left join) s.protocol_nutritionist = u_nut.id -> 9:user u_nut\n" +
                "0:study s <- (left join) s.responsible_physician = u_doc.id -> 10:user u_doc\n" +
                "0:study s <- (left join) s.responsible_physician2 = u_doc2.id -> 11:user u_doc2\n" +
                "0:study s <- (left join) s.secondary_scheduling_contact = u_sch2.id -> 12:user u_sch2\n" +
                "0:study s <- (left join) s.scheduler3 = u_sch3.id -> 13:user u_sch3\n" +
                "0:study s <- (left join) s.associate_nurse = u_nrs2.id -> 14:user u_nrs2\n" +
                "0:study s <- (left join) s.associate_protocol_nutritionist = u_nut2.id -> 15:user u_nut2\n" +
                "0:study s <- (left join) (sfs_f1.study = s.id and sfs_f1.one_to_four = 1) -> 16:study_funding_source_cai sfs_f1\n" +
                "0:study s <- (left join) (sfs_f2.study = s.id and sfs_f2.one_to_four = 2) -> 17:study_funding_source_cai sfs_f2\n" +
                "0:study s <- (left join) (sfs_f3.study = s.id and sfs_f3.one_to_four = 3) -> 18:study_funding_source_cai sfs_f3\n" +
                "0:study s <- (left join) (sfs_f4.study = s.id and sfs_f4.one_to_four = 4) -> 19:study_funding_source_cai sfs_f4\n" +
                "1:visit_template vt <- (left join) tr.visit_template = vt.id -> 2:template_resource tr\n" +
                "2:template_resource tr <- (left join) tra.template_resource = tr.id -> 3:template_resource_annotations tra\n" +
                "2:template_resource tr <- (inner join) tr.resource = rsrc.id -> 5:resource rsrc\n" +
                "3:template_resource_annotations tra <- (left join) tra.line_level_annotations = lla_vt.id -> 4:line_level_annotations lla_vt" +
                "";

        Assert.assertEquals(expected, visitTemplateGraph.toString());

        Category category1 = new Category(1000, "CATEGORY1", "CATEGORY1", "C1");
        Category vt_path = new Category(1001, "VT_PATH", "BR VT_PATH", "VT");

        Field f1ForSort = new Field(1, "visit_template", "name", "displayVt");
        Field f2ForFilter = new Field(2, "line_level_annotations", "name", "displayLlaT");
        f1ForSort.setFieldType(Field.FieldType.ShortString);
        f2ForFilter.setFieldType(Field.FieldType.ShortString);

        TemplateCategoryField tcf1ForSort = new TemplateCategoryField(1, null, category1, f1ForSort, "", true);
        TemplateCategoryField tcf2ForFilter = new TemplateCategoryField(2, null, vt_path, f2ForFilter, "_vt", true);

        Map<Integer, String> tcfsWithFilters = Maps.newHashMap();
        Map<Integer, String> tcfsWithSorts = Maps.newHashMap();

        tcfsWithFilters.put(1, "sortString");
        tcfsWithSorts.put(2, "filterString");

        List<TemplateCategoryField> fieldSet1 = Lists.newArrayList(tcf1ForSort, tcf2ForFilter);

        Graph.QueryScalarsTcfs qst = visitTemplateGraph.createQueryScalarsTcfs(
                fieldSet1,
                tcfsWithFilters,
                tcfsWithSorts,
                Lists.newArrayList(tcf2ForFilter),
                Lists.newArrayList(tcf1ForSort),
                Lists.newArrayList(1));

        String query = qst.getQueryString();
        Assert.assertEquals(" ORDER BY name_vt sortString", qst.getOrderString());

        String expectedQueryVt1 =
                "SELECT vt.name name_vt,lla_vt.name name_lla_vt " +
                        "FROM study s " +
                        "left join visit_template vt on vt.study = s.id " +
                        "left join template_resource tr on tr.visit_template = vt.id " +
                        "left join template_resource_annotations tra on tra.template_resource = tr.id " +
                        "left join line_level_annotations lla_vt on tra.line_level_annotations = lla_vt.id " +
                        "WHERE ((lla_vt.name IN ('filterString')))";

        Assert.assertEquals(expectedQueryVt1, query);

        Category category2 = new Category(1000, "CATEGORY2", "CATEGORY2", "C2");

        List<TemplateCategoryField> fieldSet2 = Lists.newArrayList(
                new TemplateCategoryField(1, null, category1, new Field(1, "visit_template", "name", "displayVt"), "", true),
                new TemplateCategoryField(2, null, category2, new Field(2, "study", "name", "displayS"), "", true)
        );

        query = visitTemplateGraph.createQueryScalarsTcfs(fieldSet2, tcfsWithFilters, tcfsWithSorts, Lists.newArrayList(), Lists.newArrayList(), Lists.newArrayList()).getQueryString();

        String expectedQueryVt2 =
                "SELECT vt.name name_vt,s.name name_s FROM study s left join visit_template vt on vt.study = s.id";

        Assert.assertEquals(expectedQueryVt2, query);

        BookedVisitGraph visitOneBr = BookedVisitGraph.getInstance();

        query = visitOneBr.createQueryScalarsTcfs(fieldSet1, tcfsWithFilters, tcfsWithSorts, Lists.newArrayList(), Lists.newArrayList(), Lists.newArrayList()).getQueryString();

        String expectedQueryBr1 =
                "SELECT vt.name name_vt,lla_vt.name name_lla_vt FROM booked_visit bv inner join visit_template vt INNER JOIN booked_resource br left join template_resource tr on br.template_resource = tr.id left join template_resource_annotations tra on tra.template_resource = tr.id left join line_level_annotations lla_vt on tra.line_level_annotations = lla_vt.id WHERE bv.visit_template = vt.id AND br.booked_visit = bv.id";

        Assert.assertEquals(expectedQueryBr1, query);

        return;
    }

    @Test
    public void testUniqueScalar() throws Exception {
        List<String> scalars = Lists.newArrayList( "zeroa", "one", "two" );

        Graph myGraph = new Graph();

        String unique = myGraph.uniqueTheScalar(scalars, "three");
        Assert.assertEquals("three", unique);

        unique = myGraph.uniqueTheScalar(scalars,"two");
        Assert.assertEquals("twoa", unique);

        unique = myGraph.uniqueTheScalar(scalars,"zeroa");
        Assert.assertEquals("zeroaa", unique);

    }
}
