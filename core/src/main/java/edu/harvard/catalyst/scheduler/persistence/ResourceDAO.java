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

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import edu.harvard.catalyst.scheduler.dto.SearchDTO;
import edu.harvard.catalyst.scheduler.dto.response.ResourceScheduleResponse;
import edu.harvard.catalyst.scheduler.dto.response.ResourcesResponse;
import edu.harvard.catalyst.scheduler.dto.response.SublocationClosureIntervalResponse;
import edu.harvard.catalyst.scheduler.entity.*;
import edu.harvard.catalyst.scheduler.util.DateUtility;
import org.hibernate.query.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;

import static edu.harvard.catalyst.hccrc.core.util.ListUtils.enrich;
import static edu.harvard.catalyst.scheduler.util.MiscUtil.negativeIsInfinity;

@Repository
@Transactional
public class ResourceDAO extends SiteDAO {

    public Institution findInstitutionById(final int id) {
        return findById(Institution.class, id);
    }

    public Sublocation findSublocationById(final int id) {
        return findById(Sublocation.class, id);
    }

    public LineLevelAnnotations findLineLevelAnnotationsById(final int id) {
        return findById(LineLevelAnnotations.class, id);
    }

    public Resource findResourceById(final int id) {
        return findById(Resource.class, id);
    }

    public ResourceType findResourceTypeById(final int id) {
        //NB: Preserve old null-returning behavior
        return ResourceType.findById(id).orElse(null);
    }

    public SublocationClosureInterval findBySublocationClosureIntervalId(final Integer id) {
        return findById(SublocationClosureInterval.class, id);
    }

    public List<Sublocation> getSublocations() {
        final CriteriaQueryHelper criteriaHelper = new CriteriaQueryHelper(session(), Sublocation.class);
        criteriaHelper.orderAsc("name");
        
        @SuppressWarnings("unchecked")
        final List<Sublocation> sublocations = criteriaHelper.getQuery().list();
        
        final Set<String> allowedNames = Sets.newHashSet("non crc", "off institution");
        
        final List<Sublocation> otherSublocationList = enrich(sublocations).filter(sublocation -> allowedNames.contains(sublocation.getName().toLowerCase())).toList();
        
        //NB: XXX: Why remove and then add back the same sublocations???
        sublocations.removeAll(otherSublocationList);
        sublocations.addAll(otherSublocationList);

        return sublocations;
    }

    public List<Institution> getInstitutions() {
        return this.findAll(Institution.class);
    }

    public List<ResourceSublocation> getResourceSublocations() {
        return this.findAll(ResourceSublocation.class);
    }

    public List<ResourceType> getResourceTypes() {
        return ResourceType.valueList();
    }

    public List<SublocationClosureInterval> getSublocationClosureIntervals() {
        return this.findAll(SublocationClosureInterval.class);
    }


    public List<String> getAllResourceNames() {

        final String findResource = "SELECT r.name FROM Resource r ORDER by r.name ASC";

        final Query query = newQuery(findResource);

        return query.list();
    }


    public List<LineLevelAnnotations> getAllLineLevelAnnotations() {

        final String queryString = "SELECT lla FROM LineLevelAnnotations lla ORDER by lla.name ASC";

        final Query query = newQuery(queryString);

        return query.list();
    }

    public List<LineLevelAnnotations> getNursingLineLevelAnnotations() {

        final String queryString = "SELECT lla FROM LineLevelAnnotations lla " +
                "WHERE lla.id IN(44, 19, 43, 2, 3, 68, 31, 29, 69, 30, 28, 37, 20, " +
                "66, 6, 39, 40, 41, 9, 10, 7, 8, 12, 13, 14, 15, 16, 17, 11, 18, 38, " +
                "42, 27, 22, 21, 24, 23, 26, 25, 36, 52, 34, 33, 70, 71, 35, 67, 32," +
                "1, 5, 4) ORDER by lla.name ASC";

        final Query query = newQuery(queryString);

        return query.list();
    }

    public List<LineLevelAnnotations> getNutritionLineLevelAnnotations() {

        final String queryString = "SELECT lla FROM LineLevelAnnotations lla " +
                "WHERE lla.id IN(19, 58, 59, 63, 66, 65, 60, 46, 47, 48, 64, " +
                "49, 50, 51, 27, 22, 21, 26, 25, 61, 62, 52, 32, 1) ORDER by lla.name ASC";

        final Query query = newQuery(queryString);

        return query.list();
    }

    public List<LineLevelAnnotations> getEquipmentlLineLevelAnnotations() {

        final String queryString = "SELECT lla FROM LineLevelAnnotations lla " +
                "WHERE lla.id IN(59, 58, 53, 54, 57, 55, 56, 72) ORDER by lla.name ASC";

        final Query query = newQuery(queryString);

        return query.list();
    }

    public List<LineLevelAnnotations> getLabLineLevelAnnotations() {

        final String queryString = "SELECT lla FROM LineLevelAnnotations lla " +
                "WHERE lla.id IN(62) ORDER by lla.name ASC";

        final Query query = newQuery(queryString);

        return query.list();
    }

    public List<LineLevelAnnotations> getRoomLineLevelAnnotations() {

        final String queryString = "SELECT lla FROM LineLevelAnnotations lla " +
                "WHERE lla.id IN(62) ORDER by lla.name ASC";

        final Query query = newQuery(queryString);

        return query.list();
    }

    // TODO: How is this different from
    // findRoomResourcesListedInResourceSublocation() ???
    public List<Resource> getRooms() {
        final List<Resource> resourcesWithSublocations = findResourcesActiveInSublocations();

        return enrich(resourcesWithSublocations).filter(this::isRoomResource).distinct().toList();
    }

    private boolean isRoomResource(final Resource r) {
        return r.getResourceType().isRoom();
    }

    //TODO: Possibly better name: findSublocationsByInstitution
    @SuppressWarnings("unchecked")
    public List<Sublocation> findSublocations(final Institution institution) {
        final CriteriaQueryHelper criteriaHelper = new CriteriaQueryHelper(session(), Sublocation.class);
        criteriaHelper.whereEquals("institution", institution);

        return criteriaHelper.getQuery().list();
    }

    @SuppressWarnings("unchecked")
    public List<Resource> findResourcesActiveInSublocations() {
        final String queryString =
                    "select r from Resource r, ResourceSublocation rs " +
                    "where rs.resource = r.id and rs.active = TRUE " +
                    "order by r.name";

        Query query = newQuery(queryString);

        return query.list();
    }

    @SuppressWarnings("unchecked")
    public List<ResourceAnnotation> findResourcesAnnotationsByResource(final Resource resource) {
        final CriteriaQueryHelper criteriaHelper = new CriteriaQueryHelper(session(), ResourceAnnotation.class);
        
        criteriaHelper.whereEquals("resource", resource);
        
        return criteriaHelper.getQuery().list();
    }

    @SuppressWarnings("unchecked")
    public List<OverrideBookedResourceAnnotations> findOverrideBookedResourceAnnotationsByBookedResource(final BookedResource bookedResource) {
        final CriteriaQueryHelper criteriaHelper = new CriteriaQueryHelper(session(), OverrideBookedResourceAnnotations.class);
        
        criteriaHelper.whereEquals("bookedResource", bookedResource);
        
        return criteriaHelper.getQuery().list();
    }

    @SuppressWarnings("unchecked")
    public List<TemplateResourceAnnotations> findTemplateAnnotationsByTemplateResource(final TemplateResource templateResource) {
        final CriteriaQueryHelper criteriaHelper = new CriteriaQueryHelper(session(), TemplateResourceAnnotations.class);
        
        criteriaHelper.whereEquals("templateResource", templateResource);
        
        return criteriaHelper.getQuery().list();
    }

    public TemplateResourceAnnotations findTemplateAnnotationsByTemplateResourceAndLineLevel(final TemplateResource templateResource, final LineLevelAnnotations lineLevelAnnotations) {
        final CriteriaQueryHelper criteriaHelper = new CriteriaQueryHelper(session(), TemplateResourceAnnotations.class);

        criteriaHelper.whereEquals("templateResource", templateResource);
        criteriaHelper.whereEquals("lineLevelAnnotations", lineLevelAnnotations);
        
        return (TemplateResourceAnnotations) criteriaHelper.getQuery().uniqueResult();
    }

    public OverrideBookedResourceAnnotations findBookedResourceAnnotationsByBookedResourceAndLineLevel(final BookedResource bookedResource, final LineLevelAnnotations lineLevelAnnotations) {
        final CriteriaQueryHelper criteriaHelper = new CriteriaQueryHelper(session(), OverrideBookedResourceAnnotations.class);

        criteriaHelper.whereEquals("bookedResource", bookedResource);
        criteriaHelper.whereEquals("lineLevelAnnotations", lineLevelAnnotations);
        
        return (OverrideBookedResourceAnnotations) criteriaHelper.getQuery().uniqueResult();
    }

    // TODO: How is this different from getRooms() ???
    public List<Resource> findRoomResourcesListedInResourceSublocation(final String sublocations) {
        return findResourcesWithSublocationsByResourceType(ResourceType.Room, sublocations);
    }

    public List<Resource> findNursingResourcesListedInResourceSublocation(final String sublocations) {
        return findResourcesWithSublocationsByResourceType(ResourceType.Nursing, sublocations);
    }

    public List<Resource> findNutritionResourcesListedInResourceSublocation(final String sublocations) {
        return findResourcesWithSublocationsByResourceType(ResourceType.Nutrition, sublocations);
    }

    @SuppressWarnings("unchecked")
    private List<Resource> findResourcesWithSublocationsByResourceType(final ResourceType resourceType, final String sublocations) {
        final boolean anySublocations = sublocations != null;
        
        final String suffix = anySublocations ? " AND s.name like (:sublocations) " : "";

        final String findResource = "SELECT r FROM Resource r, ResourceSublocation rs, Sublocation s " +
                " WHERE r.resourceType = :resourceType "
                + " AND rs.sublocation = s.id and rs.resource = r.id " + suffix;
        
        final Query query = newQuery(findResource).setParameter("resourceType", resourceType);

        if (anySublocations) {
            query.setParameter("sublocations", sublocations);
        }

        return query.list();
    }

    public Resource findResourceByName(final String resource) {
        final CriteriaQueryHelper criteriaHelper = new CriteriaQueryHelper(session(), Resource.class);
        criteriaHelper.whereEquals("name", resource);
        
        return (Resource) criteriaHelper.getQuery().uniqueResult();
    }

    // TODO: TEST (Effort/Reward ratio too poor right now.)
    @SuppressWarnings("unchecked")
    public List<ResourceSchedule> findExceptionSchedule(final Resource resource, final Date startDate, final Date endDate, final boolean override) {

        final String findExceptionSchedule = "SELECT s FROM ResourceSchedule s WHERE s.resource = :resource AND s.override = :override AND "
                + " ((:startTime between s.startTime and s.endTime) "
                + " or (:endTime between s.startTime and s.endTime) "
                + " or (s.startTime >= :startTime and s.endTime <= :endTime))";

        final Query query = newQuery(findExceptionSchedule);

        query.setParameter("resource", resource);
        query.setParameter("override", override);
        query.setParameter("startTime", startDate);
        query.setParameter("endTime", endDate);

        return query.list();
    }

    @SuppressWarnings("unchecked")
    public List<ResourceSchedule> findTemporaryAdjustmentsByResource(final Resource resource, final Date startDate, final Date endDate, final boolean override) {

        final String findExceptionSchedule = "SELECT s FROM ResourceSchedule s WHERE s.resource = :resource AND s.override = :override AND "
                + " ((:startTime >= s.startTime and :startTime < s.endTime) "
                + " or (:endTime > s.startTime and :endTime <= s.endTime) "
                + " or (s.startTime >= :startTime and s.endTime <= :endTime)) ORDER BY s.startTime";

        final Query query = newQuery(findExceptionSchedule);

        query.setParameter("resource", resource);
        query.setParameter("override", override);
        query.setParameter("startTime", startDate);
        query.setParameter("endTime", endDate);

        return query.list();
    }

    @SuppressWarnings("unchecked")
    public List<ResourceSchedule> findResourceScheduleByResource(final Resource resource, final boolean override) {
        final CriteriaQueryHelper criteriaHelper = new CriteriaQueryHelper(session(), ResourceSchedule.class);

        criteriaHelper.whereEquals("resource", resource);
        criteriaHelper.whereEquals("override", override);

        return criteriaHelper.getQuery().list();
    }

    @SuppressWarnings("unchecked")
    public List<ResourceSchedule> findResourceScheduleByResource(final Resource resource, final List<Integer> daysOfWeek, final boolean override) {
        final CriteriaQueryHelper criteriaHelper = new CriteriaQueryHelper(session(), ResourceSchedule.class);

        criteriaHelper.whereEquals("resource", resource);
        criteriaHelper.whereIn("dayOfWeek", daysOfWeek);
        criteriaHelper.whereEquals("override", override);
        criteriaHelper.orderAsc("startTime");
        
        return criteriaHelper.getQuery().list();
    }

    @SuppressWarnings("unchecked")
    public List<SublocationClosureInterval> findSublocationSchedule(final Sublocation sublocations) {
        final String findExceptionSchedule = "SELECT a FROM SublocationClosureInterval a WHERE a.sublocation = :sublocation ";

        final Query query = newQuery(findExceptionSchedule).setParameter("sublocation", sublocations);

        return query.list();
    }

    public ResourceSchedule findResourceScheduleById(final int id) {
        return findById(ResourceSchedule.class, id);
    }

    public ResourceScheduleResponse resourceScheduleById(final int id) {
        final ResourceSchedule rs = findResourceScheduleById(id);
        
        final int dayOfWeek;
        final String startTime;
        final String endTime;
        
        if (!rs.getOverride()) {
            dayOfWeek = rs.getDayOfWeek();
            startTime = formatHourMinute(rs.getStartTime());
            endTime = formatHourMinute(rs.getEndTime());
        } else {
            dayOfWeek = 1;
            startTime = formatDateTime(rs.getStartTime());
            endTime = formatDateTime(rs.getEndTime());
        }

        return new ResourceScheduleResponse(rs.getId(), dayOfWeek, startTime, endTime, rs.getQuantity(), 1);
    }

    private static String formatDateTime(final Date date) {
        return DateUtility.format(DateUtility.dateTime(), date);
    }
    
    private static String formatHourMinute(final Date date) {
        return DateUtility.format(DateUtility.hourMin(), date);
    }

    @SuppressWarnings("unchecked")
    public List<ResourceAlternate> findResourceAlternates(final Resource resource) {
        final CriteriaQueryHelper criteriaHelper = new CriteriaQueryHelper(session(), ResourceAlternate.class);
        criteriaHelper.whereEquals("sourceResource", resource);

        return criteriaHelper.getQuery().list();
    }

    public ResourcesResponse findResourceDetail(final Resource resource) {
        final String findResourceDetail = "SELECT rs FROM ResourceSublocation rs  WHERE rs.resource = :resource ";

        final Query query = newQuery(findResourceDetail).setParameter("resource", resource);

        final ResourceSublocation result = (ResourceSublocation) query.uniqueResult();

        return new ResourcesResponse(
                result.getId(),
                result.getResource().getId(),
                result.getResource().getName(),
                result.getResource().getResourceType().getName(),
                result.getSublocation().getId(),
                result.getSublocation().getName(),
                result.isActive(),
                resource.getSharedResource(),
                resource.getSharedResourceNotes(),
                1L);
    }

    public List<ResourcesResponse> findResourceListWithSublocation(final String sortBy,
                                                                   final String orderBy,
                                                                   String status,
                                                                   final int page,
                                                                   int maxResults,
                                                                   SearchDTO searchDTO)
    {
        maxResults = negativeIsInfinity(maxResults);

        StringBuffer queryStringBuffer = new StringBuffer();

        queryStringBuffer.append("SELECT r, rs, s FROM Resource r, ResourceSublocation rs, Sublocation s ");
        queryStringBuffer.append("WHERE rs.resource = r.id and rs.sublocation = s.id ");

        // It would be tempting to do the following mapping using SearchDTO.mapSearchItemKeyAndValue
        // however there is too much of an
        // impedence mistmatch. the value of status is driven by a radio button or menu selection,
        // and is not returned from the front-end to the back-end as a SearchItem object
        Map<String, String> statusFilterMap = Maps.newHashMap();
        statusFilterMap.put("active", "and rs.active = TRUE ");
        statusFilterMap.put("inactive", "and rs.active = FALSE ");
        statusFilterMap.put("both", "");

        queryStringBuffer.append(statusFilterMap.get(status));

        String totalSearchClause = "";
        if(searchDTO != null) {
            totalSearchClause = searchDTO.toSearchClause();
        }
        queryStringBuffer.append(totalSearchClause);

        queryStringBuffer.append(" order by " + sortBy + " " + orderBy);

        String queryString = queryStringBuffer.toString();

        final Query query = newQuery(queryString);

        if(searchDTO != null) {
            searchDTO.assignQueryParameterValues(query);
        }

        final long total = (long) query.list().size();

        final int offset = (page - 1) * maxResults;
        
        query.setFirstResult(offset);
        query.setMaxResults(maxResults);

        @SuppressWarnings("unchecked")
        final List<Object[]> resultSet = query.list();

        return enrich(resultSet).map(resultRow -> {
            final Resource resource = (Resource)resultRow[0];
            final ResourceSublocation resourceSublocation = (ResourceSublocation)resultRow[1];

            return new ResourcesResponse(resource.getId(), resourceSublocation.getResource().getId(),
                resource.getName(),
                    resource.getResourceType().getName(), resourceSublocation.getSublocation().getId(),
                resourceSublocation.getSublocation().getName(), resourceSublocation.isActive(), total);
        }).toList();
    }

    public List<ResourceScheduleResponse> findResourceSchedulesByResource(
            final Resource resource,
            final boolean override,
            final String sortBy,
            final String orderBy,
            final int page,
            int maxResults) {

        maxResults = negativeIsInfinity(maxResults);

        final String sortOn;

        if (sortBy.equalsIgnoreCase("da.startTime")) {
            sortOn = " da.startTime " +orderBy+ ", da.endTime ";
        } else if (sortBy.equalsIgnoreCase("da.endTime")) {
            sortOn = " da.endTime " +orderBy+ ", da.startTime ";
        } else {
            sortOn = sortBy;
        }

        final String findResource = "SELECT da FROM ResourceSchedule da  WHERE da.resource = :resource and da.override = :override  order by " + sortOn + " " + orderBy;

        final Query query = newQuery(findResource).setParameter("resource", resource).setParameter("override", override);

        final long total = (long) query.list().size();

        final int offset = (page - 1) * maxResults;
        
        query.setFirstResult(offset);
        query.setMaxResults(maxResults);

        @SuppressWarnings("unchecked")
        final List<ResourceSchedule> resultSet = query.list();

        return enrich(resultSet).map(resultRow -> {
            return new ResourceScheduleResponse(resultRow.getId(), resultRow.getDayOfWeek(),
                    DateUtility.format(DateUtility.hourMin(), resultRow.getStartTime()),
                    DateUtility.format(DateUtility.hourMin(), resultRow.getEndTime()),
                    resultRow.getQuantity(),
                    total);
        }).toList();
    }

    public List<ResourceScheduleResponse> findTemporarySchedulesByResource(final Resource resource, final boolean override, final String sortBy, final String orderBy, final int page, int maxResults) {

        maxResults = negativeIsInfinity(maxResults);

        // Get today as a Calendar
        final Calendar today = Calendar.getInstance();
        
        // Subtract 180 days (6 months)
        today.add(Calendar.DATE, -181);
        
        final Date dayBeforeSixMonths = new Date(today.getTimeInMillis());

        final String deleteTempResource = "DELETE ResourceSchedule da WHERE da.resource =:resource and da.override =:override and da.endTime < :dayBeforeSixMonths";
        
        final Query query = newQuery(deleteTempResource);
        query.setParameter("resource", resource);
        query.setParameter("override", override);
        query.setParameter("dayBeforeSixMonths", dayBeforeSixMonths);
        query.executeUpdate();

        final String sortOn = makeSortOn(sortBy, orderBy);

        final String findResource = "SELECT da FROM ResourceSchedule da  WHERE da.resource =:resource and da.override =:override  order by " + sortOn + " " + orderBy;

        final Query query1 = newQuery(findResource).setParameter("resource", resource).setParameter("override", override);

        final int offset = (page - 1) * maxResults;
        query1.setFirstResult(offset);
        query1.setMaxResults(maxResults);

        @SuppressWarnings("unchecked")
        final
        List<ResourceSchedule> resultSet = query1.list();

        final Long total = findResourceScheduleCount(resource, override);
        
        return enrich(resultSet).map(resultRow -> {
            return new ResourceScheduleResponse(resultRow.getId(), 1,
                    DateUtility.format(DateUtility.dateTime(), resultRow.getStartTime()),
                    DateUtility.format(DateUtility.dateTime(), resultRow.getEndTime()),
                    resultRow.getQuantity(),
                    total);
        }).toList();
    }

    private String makeSortOn(final String sortBy, final String orderBy) {
        if (sortBy.equalsIgnoreCase("da.startTime")) {
            return " da.startTime " +orderBy+ ", da.endTime ";
        } 
        
        if (sortBy.equalsIgnoreCase("da.endTime")) {
            return " da.endTime " +orderBy+ ", da.startTime ";
        } 
          
        return sortBy;
    }

    Long findResourceScheduleCount(final Resource resource, final boolean override) {
        final String findCount = "SELECT count(da) FROM ResourceSchedule da  WHERE da.resource = :resource and da.override = :override ";

        final Query query = newQuery(findCount).setParameter("resource", resource).setParameter("override", override);
        
        return (Long) query.uniqueResult();
    }

    // get list of existing laternate resources for a given resource
    public List<ResourcesResponse> findResourceAlternatesByResource(final Resource resource, final String sortBy,
            final String orderBy, final int page, int maxResults) {

        maxResults = negativeIsInfinity(maxResults);

        final String findResource = "SELECT ra, rs FROM ResourceAlternate ra, ResourceSublocation rs  " +
            "WHERE ra.sourceResource =:resource and rs.resource = ra.alternateResource  order by " + sortBy + " " + orderBy;
        
        final Query query = newQuery(findResource).setParameter("resource", resource);

        final Long total = (long) query.list().size();

        final int offset = (page - 1) * maxResults;
        
        query.setFirstResult(offset);
        query.setMaxResults(maxResults);

        @SuppressWarnings("unchecked")
        final List<Object[]> resultSet = query.list();

        final Function<Object[], ResourcesResponse> toResponse = resultRow -> {
            final ResourceAlternate resourceAlternate = (ResourceAlternate) resultRow[0];
            final ResourceSublocation resourceSublocation = (ResourceSublocation) resultRow[1];

            return new ResourcesResponse(resourceAlternate.getId(), resourceAlternate.getAlternateResource().getId(),
                    resourceAlternate.getAlternateResource().getName(),
                    resourceAlternate.getAlternateResource().getResourceType().getName(),
                    resourceSublocation.getSublocation().getId(),
                    resourceSublocation.getSublocation().getName(), resourceSublocation.isActive(), total);
        };
        
        return enrich(resultSet).map(toResponse).toList();
    }

    /**
     * This method returns the list of all resources which are candidates for sharing a gender block restriction with the
     * resource whose ID is provided as parameter. In essence it is the list of all Room-type resources, which are not currently
     * shared, are active, and are not the resource whose ID is provided.
     *
     * @param resourceId
     */
    public List<Resource> getResourcesAvailableForGenderBlockRestriction(int resourceId) {

        // Note: we want to include the resource currently shared with the resource passed
        // as argument, so we can list it in a drop-down menu (and show is as selected)
        final String findResource = "SELECT r FROM Resource r WHERE " +
                                    "r.id != :resourceId " +
                                    "and (r.sharedResource = null or r.sharedResource = :resourceId) " +
                                    "and r.resourceType = 'Room' " +
                                    "and (SELECT COUNT(*) FROM ResourceSublocation rs WHERE rs.resource.id = r.id and rs.active = 1) > 0 order by r.name ASC";

        final Query query = newQuery(findResource).setParameter("resourceId", resourceId);

        final List<Resource> resultSet = query.list();

        return resultSet;

    }

    public List<SublocationClosureIntervalResponse> getSublocationClosureInterval(final String sortBy, final String orderBy, final int page, int maxResults) {

        maxResults = negativeIsInfinity(maxResults);

        final String findSublocationIntervals = "SELECT sci FROM SublocationClosureInterval sci  order by " + sortBy + " " + orderBy;
        
        final Query query = newQuery(findSublocationIntervals);

        final Long total = (long) query.list().size();

        final int offset = (page - 1) * maxResults;
        
        query.setFirstResult(offset);
        query.setMaxResults(maxResults);

        @SuppressWarnings("unchecked")
        final List<SublocationClosureInterval> resultSet = query.list();

        final Function<SublocationClosureInterval, SublocationClosureIntervalResponse> toResponse = resultRow -> {
            return new SublocationClosureIntervalResponse(
                    resultRow.getId(), resultRow.getStartTime(),
                    resultRow.getEndTime(),
                    resultRow.getReason(),
                    resultRow.getSublocation().getName(),
                    total);
        };
        
        return enrich(resultSet).map(toResponse).toList();
    }

    public List<ResourcesResponse> findResourcesWithSublocationAndAlternates(
            final Resource resource, final List<Resource> altResource,
            final String sortBy, final String orderBy, final int page, int maxResults) {

        maxResults = negativeIsInfinity(maxResults);

        final String ignoreCurrentAlts = altResource.isEmpty() ? "" : " and rs.resource NOT IN (:altResource) ";

        final String findResource = "SELECT r FROM Resource r, ResourceSublocation rs " +
                " WHERE rs.resource = r.id and rs.resource != :resource " +
                "and rs.active = true " +
                ignoreCurrentAlts +
                " order by " + sortBy + " " + orderBy;
        
        final Query query = newQuery(findResource).setParameter("resource", resource);

        if (!ignoreCurrentAlts.isEmpty()) {
            query.setParameterList("altResource", altResource);
        }

        final Long total = (long) query.list().size();

        final int offset = (page - 1) * maxResults;
        
        query.setFirstResult(offset);
        query.setMaxResults(maxResults);

        @SuppressWarnings("unchecked")
        final List<Resource> resourceList = query.list();

        final Function<Resource, ResourcesResponse> toResourcesResponse = r -> {
            final ResourceSublocation resourceSublocation = findSublocationByResource(r);
            
            return new ResourcesResponse(r.getId(), r.getId(),r.getName(), r.getResourceType().getName(), resourceSublocation.getSublocation().getId(), resourceSublocation.getSublocation().getName(), resourceSublocation.isActive(), (long) total);
        };
        
        return enrich(resourceList).map(toResourcesResponse).toList();
    }

    public ResourceSublocation findSublocationByResource(final Resource r) {
        final CriteriaQueryHelper criteriaHelper = new CriteriaQueryHelper(session(), ResourceSublocation.class);
        criteriaHelper.whereEquals("resource", r);
        
        return (ResourceSublocation) criteriaHelper.getQuery().uniqueResult();
    }

    public ResourceSublocation findResourceSublocation(Integer resourceId, Integer sublocationId)
    {
        final String findResourceSublocation = "SELECT rs FROM ResourceSublocation rs " +
                "   where rs.resource.id=:resourceId and rs.sublocation.id=:sublocationId";

        final Query query = newQuery(findResourceSublocation);
        query.setParameter("resourceId", resourceId);
        query.setParameter("sublocationId", sublocationId);

        final ResourceSublocation resourceSublocation = (ResourceSublocation)query.uniqueResult();

        return resourceSublocation;
    }

    public ResourceSublocation findUniqueResourceSublocationByResource(Resource resource)
    {
        final String findResourceSublocation = "SELECT rs FROM ResourceSublocation rs where rs.resource = :resource";

        final Query query = newQuery(findResourceSublocation);
        query.setParameter("resource", resource);

        final ResourceSublocation resourceSublocation = (ResourceSublocation)query.uniqueResult();

        return resourceSublocation;
    }
}
