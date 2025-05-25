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
/**
 * 
 */
package edu.harvard.catalyst.scheduler.persistence;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import edu.harvard.catalyst.scheduler.AbstractSpringWiredHibernateTest;
import edu.harvard.catalyst.scheduler.dto.SearchDTO;
import edu.harvard.catalyst.scheduler.entity.*;
import org.hibernate.query.Query;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.Map.Entry;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.*;

/**
 * @author clint
 * 
 */
public class ResourceDAOTest extends AbstractSpringWiredHibernateTest {

    @Autowired
    private ResourceDAO dao;

    private static final Institution inst1 = new Institution("foo", "Foo University");
    private static final Institution inst2 = new Institution("bar", "Bar University");
    private static final Institution inst3 = new Institution("baz", "Baz University");

    private static final Sublocation subLoc1 = new Sublocation("library", inst1);
    private static final Sublocation subLoc2 = new Sublocation("dorm", inst1);
    private static final Sublocation subLoc3 = new Sublocation("quad", inst2);

    private static final Resource resource1 = new Resource(1, "resource with type 1 and 2 sublocs", ResourceType.Other, 42, asList(subLoc1, subLoc2), Collections.<ResourceAlternate> emptyList(), Collections.<ResourceAlternate> emptyList(), Collections.<ResourceSchedule> emptyList(), Collections.<ResourceSchedule> emptyList());
    private static final Resource resource2 = new Resource(2, "resource with room type and a subloc", ResourceType.Room, 42, asList(subLoc3), Collections.<ResourceAlternate> emptyList(), Collections.<ResourceAlternate> emptyList(), Collections.<ResourceSchedule> emptyList(), Collections.<ResourceSchedule> emptyList());
    private static final Resource resource3 = new Resource(3, "resource with type 1 and no sublocs", ResourceType.Other, 42, Collections.<Sublocation> emptyList(), Collections.<ResourceAlternate> emptyList(), Collections.<ResourceAlternate> emptyList(), Collections.<ResourceSchedule> emptyList(), Collections.<ResourceSchedule> emptyList());

    private static final SublocationClosureInterval subLocClosureInterval1 = new SublocationClosureInterval(new Date(), new Date(), "some lame reason", subLoc1);
    private static final SublocationClosureInterval subLocClosureInterval2 = new SublocationClosureInterval(new Date(), new Date(), "some other lame reason", subLoc2);
    private static final SublocationClosureInterval subLocClosureInterval3 = new SublocationClosureInterval(new Date(), new Date(), "another lame reason", subLoc3);

    private static final ResourceSchedule resourceSchedule1 = new ResourceSchedule(resource1, new Date(), new Date(), 1, 1, true);
    private static final ResourceSchedule resourceSchedule2 = new ResourceSchedule(resource1, new Date(), new Date(), 2, 1, false);
    private static final ResourceSchedule resourceSchedule3 = new ResourceSchedule(resource2, new Date(), new Date(), 3, 1, true);

    @Before
    @Transactional
    public void populateDbWithTestData() {
        saveDummyData();
    }

    private void saveDummyData() {
        save(inst1, inst2, inst3);

        save(subLoc1, subLoc2, subLoc3);

        save(subLocClosureInterval1, subLocClosureInterval2, subLocClosureInterval3);

        save(resource1, resource2, resource3);

        save(resourceSchedule1, resourceSchedule2, resourceSchedule3);

        // activate the ResourceSublocations implicitly created by the above
        List<ResourceSublocation> rsList = dao.getResourceSublocations();
        rsList.stream()
            .forEach(rs -> {
                rs.setActive(true);
                dao.updateEntity(rs);
            });
    }

    @Test
    public void testFindInstitutionById() {
        doTestById(Finders.Institution, asList(0, -1, 99), IdMap.of(inst1, inst2, inst3));
    }

    @Test
    public void testFindSublocationById() {
        doTestById(Finders.Sublocation, asList(0, -1, 99), IdMap.of(subLoc1, subLoc2, subLoc3));
    }

    @Test
    public void testFindResourceById() {
        doTestById(Finders.Resource, asList(0, -1, 99), IdMap.of(resource1, resource2, resource3));
    }

    @Test
    public void testFindResourceTypeById() {
        doTestById(Finders.ResourceType, asList(0, -1, 99), IdMap.of(ResourceType.Lab, ResourceType.Nursing, ResourceType.Nutrition, ResourceType.Other, ResourceType.Room));
    }
    
    @Test
    public void testFindBySublocationClosureIntervalId() {
        doTestById(Finders.SublocationClosureInterval, asList(0, -1, 99), IdMap.of(subLocClosureInterval1, subLocClosureInterval2, subLocClosureInterval3));
    }

    @Test
    public void testFindResourceScheduleById() {
        doTestById(Finders.ResourceSchedule, asList(0, -1, 99), IdMap.of(resourceSchedule1, resourceSchedule2, resourceSchedule3));
    }

    @Test
    public void testGetSublocations() {
        doTestGetAll(Finders.Sublocation, subLoc1, subLoc2, subLoc3);
    }

    @Test
    public void testGetInstitutions() {
        doTestGetAll(Finders.Institution, inst1, inst2, inst3);
    }

    @Test
    public void testGetResourceTypes() {
        doTestGetAll(Finders.ResourceType, ResourceType.values());
    }

    @Test
    public void testGetSublocationClosureIntervals() {
        doTestGetAll(Finders.SublocationClosureInterval, subLocClosureInterval1, subLocClosureInterval2, subLocClosureInterval3);
    }

    @Test
    public void testFindSublocations() {
        assertEquals(asList(subLoc1, subLoc2), dao.findSublocations(inst1));
        assertEquals(asList(subLoc3), dao.findSublocations(inst2));
        assertEquals(Collections.emptyList(), dao.findSublocations(inst3));
    }

    @Test
    public void testFindResourcesActiveInSublocations() {
        // Only resource1 and resource2 have non-empty sublocations lists
        // TODO: Fix findResourcesListedInResourceSublocation??? it returns some
        // dupes.
        // Oh well, that explains why getRooms() /removes/ dupes. Sigh.
        assertEquals(asList(resource2, resource1, resource1), dao.findResourcesActiveInSublocations());
    }

    @Test
    public void testGetRooms() {
        assertEquals(asList(resource2), dao.getRooms());

        final Resource resourceWithRoomAndNoSublocs = new Resource(1, "resource with room type and NO sublocs", ResourceType.Room, 42, Collections.<Sublocation> emptyList(), Collections.<ResourceAlternate> emptyList(), Collections.<ResourceAlternate> emptyList(), Collections.<ResourceSchedule> emptyList(), Collections.<ResourceSchedule> emptyList());

        try {
            save(resourceWithRoomAndNoSublocs);

            assertEquals(asList(resource2), dao.getRooms());

        } finally {
            delete(resourceWithRoomAndNoSublocs);
        }
    }

    @Test
    public void testFindRoomResourcesListedInResourceSublocation() {
        assertEquals(asList(resource2), dao.findRoomResourcesListedInResourceSublocation(subLoc3.getName()));

        final Resource resourceWithRoomAndNoSublocs = new Resource(1, "resource with room type and NO sublocs", ResourceType.Room, 42, Collections.<Sublocation> emptyList(), Collections.<ResourceAlternate> emptyList(), Collections.<ResourceAlternate> emptyList(), Collections.<ResourceSchedule> emptyList(), Collections.<ResourceSchedule> emptyList());

        try {
            save(resourceWithRoomAndNoSublocs);

            assertEquals(asList(resource2), dao.findRoomResourcesListedInResourceSublocation(subLoc3.getName()));

        } finally {
            delete(resourceWithRoomAndNoSublocs);
        }
    }

    @Test
    public void testFindNursingResourcesListedInResourceSublocation() {
        assertEquals(Collections.emptyList(), dao.findNursingResourcesListedInResourceSublocation(subLoc1.getName()));

        final Resource resourceWithNurseAndNOSublocs = new Resource(1, "resource with nursing type and NO sublocs", ResourceType.Nursing, 42, Collections.<Sublocation> emptyList(), Collections.<ResourceAlternate> emptyList(), Collections.<ResourceAlternate> emptyList(), Collections.<ResourceSchedule> emptyList(), Collections.<ResourceSchedule> emptyList());
        final Resource resourceWithNurseAnd1Subloc = new Resource(1, "resource with nursing type and 1 subloc", ResourceType.Nursing, 42, asList(subLoc1), Collections.<ResourceAlternate> emptyList(), Collections.<ResourceAlternate> emptyList(), Collections.<ResourceSchedule> emptyList(), Collections.<ResourceSchedule> emptyList());

        try {
            save(resourceWithNurseAnd1Subloc, resourceWithNurseAndNOSublocs);

            assertEquals(asList(resourceWithNurseAnd1Subloc), dao.findNursingResourcesListedInResourceSublocation(subLoc1.getName()));
        } finally {
            delete(resourceWithNurseAnd1Subloc, resourceWithNurseAndNOSublocs);
        }
    }

    @Test
    public void testFindNutritionResourcesListedInResourceSublocation() {
        assertEquals(Collections.emptyList(), dao.findNutritionResourcesListedInResourceSublocation(subLoc1.getName()));

        final Resource resourceWithNutritionAndNOSublocs = new Resource(1, "resource with nutrition type and NO sublocs", ResourceType.Nutrition, 42, Collections.<Sublocation> emptyList(), Collections.<ResourceAlternate> emptyList(), Collections.<ResourceAlternate> emptyList(), Collections.<ResourceSchedule> emptyList(), Collections.<ResourceSchedule> emptyList());
        final Resource resourceWithNutritionAnd1Subloc = new Resource(1, "resource with nutrition type and 1 subloc", ResourceType.Nutrition, 42, asList(subLoc1), Collections.<ResourceAlternate> emptyList(), Collections.<ResourceAlternate> emptyList(), Collections.<ResourceSchedule> emptyList(), Collections.<ResourceSchedule> emptyList());

        try {
            save(resourceWithNutritionAnd1Subloc, resourceWithNutritionAndNOSublocs);

            assertEquals(asList(resourceWithNutritionAnd1Subloc), dao.findNutritionResourcesListedInResourceSublocation(subLoc1.getName()));
        } finally {
            delete(resourceWithNutritionAnd1Subloc, resourceWithNutritionAndNOSublocs);
        }
    }

    @Test
    public void testFindResourceScheduleByResource() {
        final Resource resourceWithNutritionAndNOSublocs = new Resource(1, "resource with nutrition type and NO sublocs", ResourceType.Nutrition, 42, Collections.<Sublocation> emptyList(), Collections.<ResourceAlternate> emptyList(), Collections.<ResourceAlternate> emptyList(), Collections.<ResourceSchedule> emptyList(), Collections.<ResourceSchedule> emptyList());

        try {
            save(resourceWithNutritionAndNOSublocs);

            for (final boolean override : asList(true, false)) {
                assertEquals(Collections.emptyList(), dao.findResourceScheduleByResource(resourceWithNutritionAndNOSublocs, override));
            }

            assertEquals(asList(resourceSchedule1), dao.findResourceScheduleByResource(resource1, true));
            assertEquals(asList(resourceSchedule2), dao.findResourceScheduleByResource(resource1, false));
            assertEquals(asList(resourceSchedule3), dao.findResourceScheduleByResource(resource2, true));
        } finally {
            delete(resourceWithNutritionAndNOSublocs);
        }
    }

    @Test
    public void testFindResourceByName() {
        assertNull(dao.findResourceByName("foo"));

        final Resource fooResource = new Resource(1, "foo", ResourceType.Other, 42, Collections.<Sublocation> emptyList(), Collections.<ResourceAlternate> emptyList(), Collections.<ResourceAlternate> emptyList(), Collections.<ResourceSchedule> emptyList(), Collections.<ResourceSchedule> emptyList());

        try {
            save(fooResource);

            assertEquals(fooResource, dao.findResourceByName("foo"));
        } finally {
            delete(fooResource);
        }
    }

    @Test
    public void testFindSublocationSchedule() {
        final Institution inst = new Institution("x", "X College");

        final Sublocation subLoc4 = new Sublocation("quad", inst);

        final SublocationClosureInterval subLocClosureInterval4 = new SublocationClosureInterval(new Date(), new Date(), "another lame reason", subLoc4);

        try {
            save(inst, subLoc4);

            assertEquals(Collections.emptyList(), dao.findSublocationSchedule(subLoc4));

            try {
                save(subLocClosureInterval4);

                assertEquals(asList(subLocClosureInterval4), dao.findSublocationSchedule(subLoc4));
            } finally {
                delete(subLocClosureInterval4);
            }
        } finally {
            delete(inst, subLoc4);
        }
    }

    @Test
    public void testFindResourceAlternates() {
        final ResourceAlternate alt1 = new ResourceAlternate(resource2, resource1);
        final ResourceAlternate alt2 = new ResourceAlternate(resource3, resource1);

        for (final Resource resource : asList(resource1, resource2, resource3)) {
            assertEquals(Collections.emptyList(), dao.findResourceAlternates(resource));
        }

        try {
            save(alt1, alt2);

            assertEquals(asList(alt1, alt2), dao.findResourceAlternates(resource1));
            assertEquals(Collections.emptyList(), dao.findResourceAlternates(resource2));
            assertEquals(Collections.emptyList(), dao.findResourceAlternates(resource3));

        } finally {
            delete(alt1, alt2);
        }
    }

    @Test
    public void testFindResourceScheduleByResourceDowAndOverride() {
        final Resource resourceWithNutritionAndNOSublocs = new Resource(1, "resource with nutrition type and NO sublocs", ResourceType.Nutrition, 42, Collections.<Sublocation> emptyList(), Collections.<ResourceAlternate> emptyList(), Collections.<ResourceAlternate> emptyList(), Collections.<ResourceSchedule> emptyList(), Collections.<ResourceSchedule> emptyList());

        try {
            save(resourceWithNutritionAndNOSublocs);

            for (final int dayOfWeek : asList(-1, 0, 1, 2, 3, 4, 5, 6, 7)) {
                for (final boolean override : asList(true, false)) {
                    assertEquals(Collections.emptyList(), dao.findResourceScheduleByResource(resourceWithNutritionAndNOSublocs, asList(dayOfWeek), override));
                }
            }

            assertEquals(asList(resourceSchedule1), dao.findResourceScheduleByResource(resource1, asList(1), true));
            assertEquals(asList(resourceSchedule2), dao.findResourceScheduleByResource(resource1, asList(2), false));
            assertEquals(asList(resourceSchedule3), dao.findResourceScheduleByResource(resource2, asList(3), true));

            for (final int dayOfWeek : asList(4, 5, 6, 7)) {
                assertEquals(Collections.emptyList(), dao.findResourceScheduleByResource(resource1, asList(dayOfWeek), true));
                assertEquals(Collections.emptyList(), dao.findResourceScheduleByResource(resource1, asList(dayOfWeek), false));
                assertEquals(Collections.emptyList(), dao.findResourceScheduleByResource(resource2, asList(dayOfWeek), true));
            }
        } finally {
            delete(resourceWithNutritionAndNOSublocs);
        }
    }
    
    /**
     * A factored-out method to handle a common case: testing that we can find all entities of a given type.
     * 
     * @param <T> the type of entity being considered 
     * @param finder an instance of Finder that can - via the dao field - give us all the entities of type T in the test DB 
     * @param expected the entities we expect to be in the test DB
     * 
     * Checks that the dao returns all the expected entities.  
     * 
     * NB: If dupes are returned, they are ignored, since the results from the dao are stored in a set to disregard order.
     */
    private <T> void doTestGetAll(final Finder<T> finder, final T... expected) {
        // NB: Use sets to disregard order of returned results
        // TODO: Is order important?

        final Set<T> distinctExpected = Sets.newHashSet(expected);

        final Set<T> actual = Sets.newHashSet(finder.all());

        assertEquals(distinctExpected, actual);
    }

    /**
     * A factored-out method to handle a common case: testing that we can find entities in the test DB by their ids.
     * 
     * @param <T> the type of entity being considered
     * @param finder an instance of Finder that can - via the dao field - look up entities of type T in the test DB by their ids
     * @param unusedIds ids we expect to NOT be mapped to any entities
     * @param expectedValuesById a mapping of ids to entities: the data we expect to find in the test DB
     * 
     * 1) Checks that for every id => entity mapping in expectedValuesById, looking up that id returns the expected entity
     * 2) Checks that all the ids in unusedIds ARE NOT mapped to any entities.
     */
    private <T> void doTestById(final Finder<T> finder, final Collection<Integer> unusedIds, final Map<Integer, T> expectedValuesById) {
        for (final Entry<Integer, T> mapping : expectedValuesById.entrySet()) {
            final int id = mapping.getKey();
            final T expectedValue = mapping.getValue();

            assertEquals("Expected id " + id + " to yield " + expectedValue, expectedValue, finder.findById(id));
        }

        for (final Integer unusedId : unusedIds) {
            assertNull(finder.findById(unusedId));
        }
    }

    /**
     * A class to represent a mapping of integer ids to entity instances.
     * Used to store expectations for tests that look up entities by id.
     * Has a convenience constructor to allow succinct instantiations. 
     * 
     * @author clint
     *
     * @param <E> the entity class being considered
     */
    @SuppressWarnings("serial")
    private static class IdMap<E extends HasId> extends HashMap<Integer, E> {
        @SafeVarargs
        public static final <E extends HasId> IdMap<E> of(final E... entities) {
            return new IdMap<E>(entities);
        }

        @SafeVarargs
        public IdMap(final E... entities) {
            super();

            for (final E entity : entities) {
                this.put(entity.getId(), entity);
            }
        }
    }

    /**
     * 
     * A class to represent finding entities of type T in the test DB, both without restriction and by id.
     * Allows writing generic test methods parameterized on an entity type and the dao methods used to look 
     * up instances of that type. 
     * 
     * @author clint
     *
     * @param <T> the entity class being considered
     */
    private static interface Finder<T> {
        public T findById(final int id);

        public List<T> all();
    }

    private final FindersHolder Finders = new FindersHolder();

    private final class FindersHolder {
        FindersHolder() {
            super();
        }

        Finder<Institution> Institution = new Finder<Institution>() {
            public Institution findById(final int id) {
                return dao.findInstitutionById(id);
            }

            public List<Institution> all() {
                return dao.getInstitutions();
            }
        };

        Finder<Sublocation> Sublocation = new Finder<Sublocation>() {
            public Sublocation findById(final int id) {
                return dao.findSublocationById(id);
            }

            public List<Sublocation> all() {
                return dao.getSublocations();
            }
        };

        Finder<Resource> Resource = new Finder<Resource>() {
            public Resource findById(final int id) {
                return dao.findResourceById(id);
            }

            public List<Resource> all() {
                throw new UnsupportedOperationException();
            }
        };

        Finder<ResourceType> ResourceType = new Finder<ResourceType>() {
            public ResourceType findById(final int id) {
                return dao.findResourceTypeById(id);
            }

            public List<ResourceType> all() {
                return dao.getResourceTypes();
            }
        };

        Finder<SublocationClosureInterval> SublocationClosureInterval = new Finder<SublocationClosureInterval>() {
            public SublocationClosureInterval findById(final int id) {
                return dao.findBySublocationClosureIntervalId(id);
            }

            public List<SublocationClosureInterval> all() {
                return dao.getSublocationClosureIntervals();
            }
        };

        Finder<ResourceSchedule> ResourceSchedule = new Finder<ResourceSchedule>() {
            public ResourceSchedule findById(final int id) {
                return dao.findResourceScheduleById(id);
            }

            public List<ResourceSchedule> all() {
                throw new UnsupportedOperationException();
            }
        };
    }

    /**
     * This test only verifies the correctness of the generated HQL string
     */
    @Test
    public void testFindResourceListWithSublocationQueryString() {

        String sortBy = "resourceType";
        String orderBy = "DESC";
        int page = 2;
        int maxResults = 3;
        String status = "active";

        // IMPORTANT: using the ResourceDAO wired-in by spring does not work
        // Instead create a mock, and stub every method that gets called by the method
        // under test
        ResourceDAO resourceDAO = new ResourceDAO();
        ResourceDAO resourceDAOSpy = Mockito.spy(resourceDAO);

        org.hibernate.Query mockQuery = mock(Query.class);

        doReturn(session()).when(resourceDAOSpy).session();
        doReturn(mockQuery).when(resourceDAOSpy).newQuery(any());
        doReturn(mockQuery).when(mockQuery).setParameter(anyString(), any());
        doReturn(mockQuery).when(mockQuery).setFirstResult(anyInt());
        doReturn(mockQuery).when(mockQuery).setMaxResults(anyInt());

        // CASE 1: only active resources

        // filtered columns:
        //        resourceType, resourceName, resourceSublocation
        // DO NOT reuse the SearchItems because they remember the query parameter names
        // that were used in the generated where clause, and they will prevent the same name
        // from being used again
        List<SearchDTO.SearchItem> searchItems = Lists.newArrayList(
                new SearchDTO.SearchItem(
                        SearchDTO.SearchType.ANY,
                        "resourceType",
                        "partial resource type",
                        null
                ),
                new SearchDTO.SearchItem(
                        SearchDTO.SearchType.ANY,
                        "resourceName",
                        "partial resource name",
                        null
                ),
                new SearchDTO.SearchItem(
                        SearchDTO.SearchType.ANY,
                        "resourceSublocation",
                        "partial resource sublocation",
                        null
                )
        );
        SearchDTO searchDTO = new SearchDTO();
        searchDTO.setSearchItems(searchItems);

        searchDTO.mapSearchItemKeyAndValue( "resourceName", "r.name", null);
        searchDTO.mapSearchItemKeyAndValue("resourceType", "r.resourceType", null);
        searchDTO.mapSearchItemKeyAndValue("resourceSublocation", "s.name", null);

        resourceDAOSpy.findResourceListWithSublocation(sortBy, orderBy, status, page, maxResults, searchDTO);

        String expectedHql = "SELECT r, rs, s FROM Resource r, ResourceSublocation rs, Sublocation s " +
                "WHERE rs.resource = r.id and rs.sublocation = s.id and rs.active = TRUE " +
                " AND r.resourceType LIKE :r_resourceType " +
                "AND r.name LIKE :r_name " +
                "AND s.name LIKE :s_name order by resourceType DESC";

        Mockito.verify(resourceDAOSpy, times(1)).newQuery(expectedHql);

        assertEquals("r_resourceType", searchItems.get(0).getQueryParamName());
        assertEquals("r_name", searchItems.get(1).getQueryParamName());
        assertEquals("s_name", searchItems.get(2).getQueryParamName());

        assertEquals("%partial resource type%", searchItems.get(0).getQueryParamValue());
        assertEquals("%partial resource name%", searchItems.get(1).getQueryParamValue());
        assertEquals("%partial resource sublocation%", searchItems.get(2).getQueryParamValue());

        // CASE 2: only inactive resources

        // DO NOT reuse the SearchItems because they remember the query parameter names
        // that were used in the generated where clause, and they will prevent the same name
        // from being used again
        searchItems = Lists.newArrayList(
                new SearchDTO.SearchItem(
                        SearchDTO.SearchType.ANY,
                        "resourceType",
                        "partial resource type",
                        null
                ),
                new SearchDTO.SearchItem(
                        SearchDTO.SearchType.ANY,
                        "resourceName",
                        "partial resource name",
                        null
                ),
                new SearchDTO.SearchItem(
                        SearchDTO.SearchType.ANY,
                        "resourceSublocation",
                        "partial resource sublocation",
                        null
                )
        );
        searchDTO = new SearchDTO();
        searchDTO.setSearchItems(searchItems);

        status = "inactive";

        searchDTO.mapSearchItemKeyAndValue("resourceName", "r.name", null);
        searchDTO.mapSearchItemKeyAndValue("resourceType", "r.resourceType", null);
        searchDTO.mapSearchItemKeyAndValue("resourceSublocation", "s.name", null);

        resourceDAOSpy.findResourceListWithSublocation(sortBy, orderBy, status, page, maxResults, searchDTO);

        expectedHql = "SELECT r, rs, s FROM Resource r, ResourceSublocation rs, Sublocation s " +
                "WHERE rs.resource = r.id and rs.sublocation = s.id and rs.active = FALSE " +
                " AND r.resourceType LIKE :r_resourceType " +
                "AND r.name LIKE :r_name " +
                "AND s.name LIKE :s_name order by resourceType DESC";

        Mockito.verify(resourceDAOSpy, times(1)).newQuery(expectedHql);

        assertEquals("r_resourceType", searchItems.get(0).getQueryParamName());
        assertEquals("r_name", searchItems.get(1).getQueryParamName());
        assertEquals("s_name", searchItems.get(2).getQueryParamName());

        assertEquals("%partial resource type%", searchItems.get(0).getQueryParamValue());
        assertEquals("%partial resource name%", searchItems.get(1).getQueryParamValue());
        assertEquals("%partial resource sublocation%", searchItems.get(2).getQueryParamValue());

        // CASE 3: both active and inactive resources

        // DO NOT reuse the SearchItems because they remember the query parameter names
        // that were used in the generated where clause, and they will prevent the same name
        // from being used again
        searchItems = Lists.newArrayList(
                new SearchDTO.SearchItem(
                        SearchDTO.SearchType.ANY,
                        "resourceType",
                        "partial resource type",
                        null
                ),
                new SearchDTO.SearchItem(
                        SearchDTO.SearchType.ANY,
                        "resourceName",
                        "partial resource name",
                        null
                ),
                new SearchDTO.SearchItem(
                        SearchDTO.SearchType.ANY,
                        "resourceSublocation",
                        "partial resource sublocation",
                        null
                )
        );
        searchDTO = new SearchDTO();
        searchDTO.setSearchItems(searchItems);

        status = "both";

        searchDTO.mapSearchItemKeyAndValue("resourceName", "r.name", null);
        searchDTO.mapSearchItemKeyAndValue("resourceType", "r.resourceType", null);
        searchDTO.mapSearchItemKeyAndValue("resourceSublocation", "s.name", null);

        resourceDAOSpy.findResourceListWithSublocation(sortBy, orderBy, status, page, maxResults, searchDTO);

        expectedHql = "SELECT r, rs, s FROM Resource r, ResourceSublocation rs, Sublocation s " +
                "WHERE rs.resource = r.id and rs.sublocation = s.id " +
                " AND r.resourceType LIKE :r_resourceType " +
                "AND r.name LIKE :r_name " +
                "AND s.name LIKE :s_name order by resourceType DESC";

        Mockito.verify(resourceDAOSpy, times(1)).newQuery(expectedHql);

    }

}
