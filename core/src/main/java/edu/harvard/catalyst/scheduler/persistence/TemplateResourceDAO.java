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

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import edu.harvard.catalyst.scheduler.dto.response.GetSearchVisitResourceResponse;
import edu.harvard.catalyst.scheduler.dto.response.ResourceTimeBoundsAndCountResponseDTO;
import edu.harvard.catalyst.scheduler.dto.response.TemplateResourceWithTraListDTO;
import edu.harvard.catalyst.scheduler.entity.*;
import org.hibernate.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static edu.harvard.catalyst.scheduler.persistence.HqlClauses.whereBuilder;
import static edu.harvard.catalyst.scheduler.util.MiscUtil.negativeIsInfinity;

/**
 * Created by xavier on 6/29/17.
 */

@Repository
@Transactional
public class TemplateResourceDAO extends SiteDAO {

    @Autowired
    StudyDAO studyDAO;

    public List<TemplateResource> findTemplateResourcesByVisitAndResource(final Integer visitTemplate,
                                                                          final Integer rId) {
        final String hql = "select tr " +
                "from TemplateResource tr where tr.visitTemplate.id = :visitTemplate and tr.resource.id = :rId";

        final Query query = newQuery(hql)
                .setParameter("visitTemplate", visitTemplate)
                .setParameter("rId", rId);

        final List<TemplateResource> result = query.list();

        return result;
    }

    public List<TemplateResource> getTemplateResourcesUsedOnDay(final int vtId,
                                                                int selectedMinutesStartOfDay,
                                                                int nextMinutesStartOfDay) {

        final String hql = "SELECT tr " +
                "FROM TemplateResource tr  " +
                "INNER JOIN tr.visitTemplate vt " +
                "WHERE vt.id = :vtId " +
                "AND (tr.startMinutes >= :selectedMinutesStartOfDay)" +
                "AND (tr.endMinutes < :nextMinutesStartOfDay)";

        final Query hqlQuery = newQuery(hql)
                .setParameter("vtId", vtId)
                .setParameter("selectedMinutesStartOfDay", selectedMinutesStartOfDay)
                .setParameter("nextMinutesStartOfDay", nextMinutesStartOfDay);

        @SuppressWarnings("unchecked")
        final List<TemplateResource> templateResourceList = hqlQuery.list();

        return templateResourceList;
    }

    public List<TemplateResource> findTemplateResourcesByVisit(final VisitTemplate visit) {
        return findTemplateResourcesByVisit(visit, "");
    }

    // Note: page and maxResults arguments are not used
    public List<TemplateResource> findTemplateResourcesByVisitAndBillable(
            VisitTemplate visit, boolean isBillable, String sortBy, String orderBy, int page, int maxResults
    ) {
        String sortExpression = " order by " + sortBy + " " + orderBy;
        String queryString =
                "select tr from TemplateResource tr where tr.visitTemplate = :visit and tr.billable = :isBillable " + sortExpression;

        Query query = newQuery(queryString);
        query.setParameter("visit", visit);
        query.setParameter("isBillable", isBillable);

        final List<TemplateResource> templateResourceList = query.list();

        return templateResourceList;

    }

    public List<TemplateResource> findTemplateResourcesByVisit(final VisitTemplate visit, String sortExpression) {
        String queryString =
                "select tr from TemplateResource tr where tr.visitTemplate = :visit " + sortExpression;

        Query query = newQuery(queryString).setParameter("visit", visit);

        final List<TemplateResource> templateResourceList = query.list();

        return templateResourceList;
    }

    public List<TemplateResourceWithTraListDTO> findTemplateResourcesAndAnnotationsByVisit(
            final Integer visitId,
            final String sortExpression) {

        String queryString =
                "select tr, tra " +
                        "from TemplateResourceAnnotations tra right join tra.templateResource tr " +
                        "where tr.visitTemplate.id = :visitId " +
                        sortExpression;

        Query query = newQuery(queryString).setParameter("visitId", visitId);

        final List<Object[]> queryResultRowList = query.list();

        Map<TemplateResource,TemplateResourceWithTraListDTO> dtoMap = Maps.newHashMap();
        List<TemplateResourceWithTraListDTO> resultList = Lists.newArrayList();

        for (Object[] queryResultRow : queryResultRowList) {
            TemplateResource tr = (TemplateResource)queryResultRow[0];
            TemplateResourceAnnotations tra = (TemplateResourceAnnotations)queryResultRow[1];

            TemplateResourceWithTraListDTO associatedTrwalDto = dtoMap.get(tr);
            if (associatedTrwalDto == null) {
                associatedTrwalDto = new TemplateResourceWithTraListDTO(tr);
                dtoMap.put(tr, associatedTrwalDto);
                resultList.add(associatedTrwalDto);
            }
            TemplateResourceWithTraListDTO trwalDto = dtoMap.get(tr);

            if (tra != null) {
                trwalDto.getTraList().add(tra);
            }
        }

        return resultList;
    }

    public GetSearchVisitResourceResponse findTemplateResourcesByVisit(
            final Integer visitId,
            final String sortByParam,
            final String orderBy,
            final Integer page,
            Integer maxResults) {

        maxResults = negativeIsInfinity(maxResults);

        final String sortBy;

        if (sortByParam.equalsIgnoreCase("tr.startMinutes")) {
            sortBy = " tr.startMinutes " + orderBy + ", tr.endMinutes ";
        } else if (sortByParam.equalsIgnoreCase("tr.endMinutes")) {
            sortBy = " tr.endMinutes " + orderBy + ", tr.startMinutes ";
        } else {
            sortBy = sortByParam;
        }

        final VisitTemplate visitTemplate = studyDAO.findVisitById(visitId);

        final String whereClause = whereBuilder()
                .equalTo(Optional.of("r.id"), "tr.resource")
                .equalTo(Optional.of(":vt"), "tr.visitTemplate")
                .build();

        final String simpleHql = "select tr from TemplateResource tr, Resource r " + whereClause + " order by " + sortBy + " " + orderBy;

        final Query mainQuery = newQuery(simpleHql).setParameter("vt", visitTemplate);

        final long total = mainQuery.list().size();

        final int offset = (page - 1) * maxResults;

        mainQuery.setFirstResult(offset);
        mainQuery.setMaxResults(maxResults);

        @SuppressWarnings("unchecked")
        final List<TemplateResource> resultRows = mainQuery.list();

        return GetSearchVisitResourceResponse.createGetSearchVisitsResponse(resultRows, total);
    }

    @SuppressWarnings("unchecked")
    public List<TemplateResourceAnnotations> findTemplateResourceAnnotationsByTemplateResource(final TemplateResource tr) {
        final CriteriaQueryHelper criteriaHelper = new CriteriaQueryHelper(session(), TemplateResourceAnnotations.class);
        criteriaHelper.whereEquals("templateResource", tr);

        return criteriaHelper.getQuery().list();
    }

    public TemplateResource findTemplateResourceLowest(final VisitTemplate visitTemplate){
        final String findTemplateResource = "SELECT tr FROM TemplateResource tr"
                + " WHERE "
                + " tr.visitTemplate = :visitTemplate order by tr.startMinutes ASC";

        final Query query = newQuery(findTemplateResource).setParameter("visitTemplate", visitTemplate).setFirstResult(0).setMaxResults(1);

        @SuppressWarnings("unchecked")
        final List<TemplateResource> brs = query.list();

        return brs.get(0);
    }

    public ResourceTimeBoundsAndCountResponseDTO findTemplateResourceCountEarliestLatest(final int visitTemplate){
        final String findTemplateResource =
                "SELECT min(startMinutes), max(endMinutes), count(id) FROM TemplateResource a"
                        + " WHERE a.visitTemplate.id = :visitTemplate";

        final Query query = newQuery(findTemplateResource).setParameter("visitTemplate", visitTemplate);

        @SuppressWarnings("unchecked")
        final List<Object[]> resultRows = query.list();

        final Object[] row0 = resultRows.get(0);

        final Date earliestStartDate = (Date)row0[0];
        final Date latestEndDate = (Date)row0[1];
        final Long count = (Long)row0[2];

        return ResourceTimeBoundsAndCountResponseDTO.fromTriple(count, earliestStartDate, latestEndDate);
    }

    @SuppressWarnings("unchecked")
    public List<TemplateResource> findRoomTemplateResourcesByVisit(final VisitTemplate visitTemplate) {
        final String findTemplateResource = "SELECT tr FROM TemplateResource tr, Resource r"
                + " WHERE "
                + " tr.visitTemplate = :visitTemplate and tr.resource = r.id and r.resourceType = :resourceType";

        final Query query = newQuery(findTemplateResource).
                setParameter("visitTemplate", visitTemplate).
                setParameter("resourceType", ResourceType.Room);

        return query.list();
    }

    @SuppressWarnings("unchecked")
    public List<TemplateResource> findFixedTemplateResourcesByVisit(final VisitTemplate visit) {
        final CriteriaQueryHelper criteriaHelper = new CriteriaQueryHelper(session(), TemplateResource.class);

        criteriaHelper.whereEquals("visitTemplate", visit);
        criteriaHelper.whereIsNull("groupId");
        criteriaHelper.whereEquals("floatable", Boolean.FALSE);

        return criteriaHelper.getQuery().list();
    }

    @SuppressWarnings("unchecked")
    public List<TemplateResource> findUngroupedTemplateResourcesTypeByVisit(final VisitTemplate visit, final String templateResourceType) {
        final CriteriaQueryHelper criteriaHelper = new CriteriaQueryHelper(session(), TemplateResource.class);

        criteriaHelper.whereEquals("visitTemplate", visit);
        criteriaHelper.whereIsNull("groupId");
        criteriaHelper.whereEquals(templateResourceType, Boolean.TRUE);

        return criteriaHelper.getQuery().list();
    }

    @SuppressWarnings("unchecked")
    public List<TemplateResourceGroup> findTemplateResourceGroupByVisitAndTemplate(final VisitTemplate visit, final TemplateResource templateResource){
        final CriteriaQueryHelper criteriaHelper = new CriteriaQueryHelper(session(), TemplateResourceGroup.class);

        criteriaHelper.whereEquals("templateResource", templateResource);
        criteriaHelper.whereEquals("visit", visit);

        return criteriaHelper.getQuery().list();
    }

    public TemplateResource findTemplateResourceById(final int id) {
        return this.findById(TemplateResource.class, id);
    }

}
