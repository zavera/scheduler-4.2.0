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

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import edu.harvard.catalyst.scheduler.core.SchedulerRuntimeException;

import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: carl
 * Date: 10/21/14
 * Time: 1:45 PM
 */


// Do not use unconnected Graphs, or Graphs with multiple entry points
public class Graph {


    private Set<Vertex> vertices;
    private Set<Edge> edges;
    private Vertex rootVertex;

    private Map<String, Vertex> shortNameToVertex;
    private Map<Set<Vertex>,Edge> verticesToEdge;

    private int counter = 0;

    static final String ALIEN_VERTEX = "Alien Vertex(es)!";

    // leaf ctor 'collects' the ingredients, and calls init()
    public Graph() {
    }

    // ingredients explicitly supplied
    public Graph(final Set<Edge> edges, final Vertex rootVertex, final boolean renumber) {
        init(edges, rootVertex, renumber);
    }

    // sub-graph already has numbers
    private Graph(final Set<Edge> edges, final Vertex rootVertex) {
        init(edges, rootVertex, false);
    }

    protected void init(final Set<Edge> totalEdges, final Vertex rootVertex, final boolean renumber) {
        edges = totalEdges;
        this.rootVertex = rootVertex;

        vertices = initVerticesFromEdges();
        verticesToEdge = initVerticesToEdgeMap();
        shortNameToVertex = initShortNameToVertexMap();
        if (renumber) {
            numberTheVertices(rootVertex, new HashSet<>());
        }
        if ( ! isConnected()) {
            SchedulerRuntimeException.logAndThrow("Graph is not connected.");
        }
    }

    void numberTheVertices(final Vertex startingPoint, final Set<Vertex> alreadyDone) {
        if ( ! alreadyDone.contains(startingPoint)) {
            startingPoint.setPriority(counter++);
            alreadyDone.add(startingPoint);
            for (final Edge outEdge : outEdges(startingPoint)) {
                final Vertex nextVertex = outEdge.getPointTwo();
                numberTheVertices(nextVertex, alreadyDone);
            }
        }
    }

    Map<Set<Vertex>, Edge> initVerticesToEdgeMap() {
        final Map<Set<Vertex>, Edge> result = new HashMap<>();
        for (final Edge edge : edges) {
            final Set<Vertex> endpoints = Sets.newHashSet(
                    edge.getPointOne(),
                    edge.getPointTwo());
            result.put(endpoints, edge);
        }
        return result;
    }
    Set<Vertex> initVerticesFromEdges() {
        final Set<Vertex> result = new HashSet<>();
        for (final Edge edge : edges) {
            final Vertex one = edge.getPointOne();
            final Vertex two = edge.getPointTwo();
            result.add(one);
            result.add(two);
        }
        // 'edge' case of no edges, but should be one vertex
        if (edges.size() == 0) {
            result.add(rootVertex);
        }
        return result;
    }

    Map<String, Vertex> initShortNameToVertexMap() {
        final Map<String, Vertex> result = new HashMap<String, Vertex>();
        for (final Vertex vertex : vertices) {
            final String shortName = vertex.getTableShortName();
            result.put(shortName, vertex);
        }
        return result;
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        builder.append("Graph:").append("\n");
        final List<String> edgeStringList = new ArrayList<String>();
        for (final Edge edge : edges) {
            edgeStringList.add(edge.toString());
        }
        final String edgeString = Joiner.on("\n").join(edgeStringList);
        builder.append(edgeString);
        // on the other hand
        if (vertices.size() == 1) {
            for (final Vertex theOneVertex : vertices) {
                builder.append(theOneVertex.toString());
            }
        }
        return builder.toString();
    }

    public Set<Vertex> getVertices() {
        return vertices;
    }

    Set<Edge> getEdgesFromVertices(final Set<Vertex> vertices) {
        final Set<Edge> result = new HashSet<>();
        for (final Vertex pointOne : vertices) {
            final Set<Vertex> others = new HashSet<>(vertices);
            others.remove(pointOne);
            for (final Vertex otherPoint : others) {
                final Set<Vertex> twoPoints = Sets.newHashSet(pointOne, otherPoint);
                final Edge edge = verticesToEdge.get(twoPoints);
                if (edge != null) {
                    result.add(edge);
                }
            }
        }
        return result;
    }

    boolean isConnected() {
        return isConnected(vertices);
    }

    boolean isConnected(final Set<Vertex> candidateVertices){
        final Set<Vertex> aliens = new HashSet<Vertex>(candidateVertices);
        aliens.removeAll(vertices);
        if (aliens.size() > 0) {
            SchedulerRuntimeException.logAndThrow(ALIEN_VERTEX);
        }
        if (candidateVertices.size() == 0) {
            return false;
        }
        final Vertex startingPoint = Collections.min(candidateVertices, new Vertex.VertexComparator());
        final Set<Vertex> accountedFor = new HashSet<Vertex>();
        checkConnection(candidateVertices, accountedFor, startingPoint);
        return candidateVertices.equals(accountedFor);
    }

    void checkConnection(final Set<Vertex> candidateVertices, final Set<Vertex> accountedFor, final Vertex startingPoint) {
        // sanity check, shouldn't happen if graph is constructed properly
        if ( ! vertices.contains(startingPoint)) {
            SchedulerRuntimeException.logAndThrow(ALIEN_VERTEX);
        }
        if (candidateVertices.size() == 0) {
            return;
        }
        accountedFor.add(startingPoint);
        final Set<Edge> outEdges = outEdges(startingPoint);
        for (final Edge edge : outEdges) {
            final Vertex pointTwo = edge.getPointTwo();
            if (candidateVertices.contains(pointTwo)) {
                final Set<Vertex> smallerCandidates = new HashSet<Vertex>(candidateVertices);
                smallerCandidates.remove(startingPoint);
                checkConnection(smallerCandidates, accountedFor, pointTwo);
            }
        }
        return;
    }

    public Graph getSmallConnectedSubgraph(final Set<Vertex> requiredSubset) {
        // pu == 'uncarved block' in Taoist literature
        Set<Vertex> puSet = new HashSet<Vertex>(vertices);
        final List<Vertex> verticesAsList = new ArrayList<Vertex>(vertices);
        Collections.sort(verticesAsList, new Vertex.VertexComparator());
        Collections.reverse(verticesAsList);
        for (int i=0; i<verticesAsList.size(); i++) {
            final Vertex currentVertex = verticesAsList.get(i);
            final Set<Vertex> chippedPuSet = new HashSet<Vertex>(puSet);
            chippedPuSet.remove(currentVertex);
            if (isConnected(chippedPuSet) && chippedPuSet.containsAll(requiredSubset)) {
                puSet = chippedPuSet;
            }
        }
        Graph result;
        final Set<Edge> edges = getEdgesFromVertices(puSet);
        final Vertex startingPoint = Collections.min(puSet, new Vertex.VertexComparator());
        if (puSet.size() == 1) {
            result = new Graph(new HashSet<Edge>(), startingPoint);
        }
        else {
            result = new Graph(edges, startingPoint);
        }
        return result;
    }

    Set<Edge> outEdges(final Vertex vertex) {
        final Set<Edge> result = new TreeSet<Edge>();
        for (final Edge edge : edges) {
            final Vertex vertexOne = edge.getPointOne();
            if (vertexOne.equals(vertex)) {
                result.add(edge);
            }
        }
        return result;
    }

    public QueryScalarsTcfs createQueryScalarsTcfs(final List<TemplateCategoryField> tcfList,
                                                   Map<Integer, String> tcfIdToStringSortList,
                                                   Map<Integer, String> tcfIdToStringFilterList,
                                                   List<TemplateCategoryField> tcfsWithFilters,
                                                   List<TemplateCategoryField> tcfsWithSorts,
                                                   List<Integer> tcfIdsForSortPriority) {
        final QueryScalarsTcfs qsfResult = new QueryScalarsTcfs();
        qsfResult.setTcfs(tcfList);
        if (tcfList.size() > 0) {
            final List<String> qsfScalars = qsfResult.getScalars();
            final List<String> selectColumns = Lists.newArrayList();
            final List<String> whereClauseList = Lists.newArrayList();
            final List<String> orderClauseList = Lists.newArrayList();
            final Set<Vertex> vertexSet = new HashSet<Vertex>();

            List<String> fromTables = Lists.newArrayList();

            // every sub-graph needs to include the root of the main graph
            vertexSet.add(rootVertex);
            fromTables.add(rootVertex.fullName());
            final Map<Integer, String> tcfIdToSortPhrase = Maps.newHashMap();

            for (final TemplateCategoryField tcf : tcfList) {
                processOneTcf(
                        qsfScalars,
                        selectColumns,
                        fromTables,
                        whereClauseList,
                        vertexSet, tcf,
                        tcfIdToStringSortList,
                        tcfIdToStringFilterList,
                        tcfsWithFilters,
                        tcfsWithSorts,
                        tcfIdToSortPhrase);
            }

            for (Integer tcfId : tcfIdsForSortPriority) {
                orderClauseList.add(tcfIdToSortPhrase.get(tcfId));
            }

            final Graph subGraph = getSmallConnectedSubgraph(vertexSet);
            final StringBuilder queryBuilder = new StringBuilder();
            queryBuilder.append("SELECT ")
                .append(Joiner.on(",").join(selectColumns))
                .append(" FROM ");
            final StringBuilder joinBuilder = new StringBuilder();
            traverseEdgesForJoin(subGraph, rootVertex, whereClauseList, false, null, joinBuilder, fromTables);

            fromTables = postProcessFromTablesForSorts(fromTables, tcfsWithSorts);
            queryBuilder.append(Joiner.on(" inner join ").join(fromTables));

            String joinResult = postProcessQueryForSorts(joinBuilder, tcfsWithSorts);
            queryBuilder.append(joinResult);

            if (whereClauseList.size() > 0) {
                queryBuilder.append(" WHERE ")
                .append(Joiner.on(" AND ").join(whereClauseList));
            }
            final String qsfQuery = queryBuilder.toString();
            qsfResult.setQueryString(qsfQuery);

            if ( ! orderClauseList.isEmpty()) {
                final StringBuilder orderBuilder = new StringBuilder();
                orderBuilder.append(" ORDER BY ").append(Joiner.on(",").join(orderClauseList));
                qsfResult.setOrderString(orderBuilder.toString());
            }
        }
        return qsfResult;
    }

    List<String>  postProcessFromTablesForSorts(List<String> fromTables, List<TemplateCategoryField> tcfsWithSorts) {
        for (TemplateCategoryField tcf: tcfsWithSorts) {
            FieldProcessingEnum fieldProcessingEnum = tcf.getField().getFieldProcessingEnum();
            fromTables = fieldProcessingEnum.postProcessFromTablesForSort(fromTables, tcf);
        }
        return fromTables;
    }

    String postProcessQueryForSorts(StringBuilder joinBuilder, List<TemplateCategoryField> tcfsWithSorts) {
        String result = joinBuilder.toString();

        for (TemplateCategoryField tcf: tcfsWithSorts) {
            FieldProcessingEnum fieldProcessingEnum = tcf.getField().getFieldProcessingEnum();
            result = fieldProcessingEnum.postProcessQueryForSort(result, tcf);
        }

        return result;
    }

    void processOneTcf(final List<String> qsfScalars,
                       final List<String> selectColumns,
                       final List<String> fromTables,
                       final List<String> whereClauses,
                       final Set<Vertex> vertexSet,
                       final TemplateCategoryField tcf,
                       Map<Integer, String> tcfIdToStringSortList,
                       Map<Integer, String> tcfIdToStringFilterList,
                       List<TemplateCategoryField> tcfsWithFilters,
                       List<TemplateCategoryField> tcfsWithSorts,
                       Map<Integer, String> tcfIdToSortPhrase) {
            final String suffix = tcf.getSuffix();
            final Field field = tcf.getField();
            final String table = field.getTable();
            final String shortName = Field.getTableAlias(table) + suffix;
            final Vertex vertex = shortNameToVertex.get(shortName);
            if (vertex == null) {
                SchedulerRuntimeException.logAndThrow("No Vertex for <" + shortName + ">");
            }
            vertexSet.add(vertex);
            addScalarsAndSelectColumns(
                    tcf,
                    qsfScalars,
                    selectColumns,
                    fromTables,
                    whereClauses,
                    field.getColumn(),
                    shortName,
                    tcfIdToStringSortList,
                    tcfIdToStringFilterList,
                    tcfsWithFilters,
                    tcfsWithSorts,
                    tcfIdToSortPhrase);
    }

    void traverseEdgesForJoin(final Graph subGraph,
            final Vertex currentVertex,
            final List<String> whereClauseList,
            final boolean previousLeftJoin,
            final Vertex previousRhs,
            final StringBuilder builder,
            final List<String> fromTables) {
        for (final Edge edge : subGraph.outEdges(currentVertex)) {
            final boolean leftJoin = edge.getJoinType().equals(Edge.JoinType.LEFT);
            final Vertex rhs = edge.getPointTwo();
            edge.produceJoinLists(whereClauseList, previousLeftJoin, previousRhs, builder, fromTables);
            traverseEdgesForJoin(subGraph, rhs, whereClauseList, leftJoin, rhs, builder, fromTables);
        }
    }

    String uniqueTheScalar(final List<String> scalars, String candidate) {
        String result = candidate;

        while (scalars.contains(result)) {
            result = result + "a";
        }
        return result;
    }

    /////////// operations for FieldProcessingEnum
    public void addScalarsAndSelectColumns(final TemplateCategoryField tcf,
                                           final List<String> qsfScalars,
                                           final List<String> selectColumns,
                                           final List<String> fromTables,
                                           final List<String> whereClauses,
                                           final String columnsString,
                                           final String shortName,
                                           Map<Integer, String> tcfIdToStringSortList,
                                           Map<Integer, String> tcfIdToStringFilterList,
                                           List<TemplateCategoryField> tcfsWithFilters,
                                           List<TemplateCategoryField> tcfsWithSorts,
                                           Map<Integer, String> tcfIdToSortPhrase) {

        final List<String> columns = Lists.newArrayList(Splitter.on(",").split(columnsString));

        for (final String column : columns) {
            final String officialColumn = shortName + "." + column;

            String scalarColumn = uniqueTheScalar(qsfScalars, column + "_" + shortName);

            qsfScalars.add(scalarColumn);
            final StringBuilder builder = new StringBuilder();
            builder.append(officialColumn)
            .append(" ")
            .append(scalarColumn);
            final String selectColumn = builder.toString();
            selectColumns.add(selectColumn);

            if (tcfsWithSorts.contains(tcf)) {
                FieldProcessingEnum processingEnum = tcf.getField().getFieldProcessingEnum();

                processingEnum.sortFromTableAddition(column, fromTables);
                processingEnum.sortWhereClauseAddition(column, officialColumn, whereClauses);

                String orderBy = tcfIdToStringSortList.get(tcf.getId());
                String orderString = processingEnum.sortOrderByPhrase(scalarColumn, column, orderBy);
                tcfIdToSortPhrase.put(tcf.getId(), orderString);
            }

            if (tcfsWithFilters.contains(tcf)) {
                String commaJoinedString = tcfIdToStringFilterList.get(tcf.getId());
                Field tcfField = tcf.getField();
                Field.FieldType fieldType = tcfField.getFieldType();

                if (fieldType == null) {
                    SchedulerRuntimeException.logAndThrow("Hey, field " + tcfField.getDisplayName() + " doesn't have a filter!");
                }

                String whereClause = tcfField.commaJoinedStringToSqlString(officialColumn, commaJoinedString);
                whereClauses.add(whereClause);
            }
        }
    }

    public static class QueryScalarsTcfs {
        String queryString = "";
        String orderString = "";

        List<String> scalars = new ArrayList<String>();
        List<TemplateCategoryField> tcfs = new ArrayList<TemplateCategoryField>();

        public void setQueryString(final String queryString) {
            this.queryString = queryString;
        }

        public String getQueryString() {
            return queryString;
        }

        public String getOrderString() {
            return orderString;
        }

        public void setOrderString(String orderString) {
            this.orderString = orderString;
        }

        public List<String> getScalars() {
            return scalars;
        }
        public List<TemplateCategoryField> getTcfs() {
            return tcfs;
        }

        public void setTcfs(final List<TemplateCategoryField> tcfs) {
            this.tcfs = tcfs;
        }

        // for tests
        @Override
        public boolean equals(final Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            final QueryScalarsTcfs that = (QueryScalarsTcfs) o;
            return queryString.equals(that.queryString);
        }

        @Override
        public int hashCode() {
            return queryString.hashCode();
        }
    }
}
