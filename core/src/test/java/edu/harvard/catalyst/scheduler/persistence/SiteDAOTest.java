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
import edu.harvard.catalyst.hccrc.core.util.TestHelpers;
import edu.harvard.catalyst.scheduler.AbstractSpringWiredHibernateTest;
import edu.harvard.catalyst.scheduler.entity.*;
import org.hibernate.Session;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.criteria.*;
import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.*;

/**
 * @author clint
 * @date Aug 6, 2013
 *
 */
public final class SiteDAOTest extends AbstractSpringWiredHibernateTest implements TestHelpers {

    // using one of SiteDAO's non-abstract manifestations
    @Autowired
    private ReportDAO dao;

    private final User jim = new User();
    private final Institution institution =
            new Institution("nm", "lnm");
    private final InstitutionRole institutionRole =
            new InstitutionRole(1, InstitutionRoleType.ROLE_FRONT_DESK, "d", "n");
    private final Role role =
            new Role(1, "r", RoleType.ROLE_ASSOCIATE_NURSE);

    private Integer jimId;

    private final String JIM = "Jim";

    ////////////////

    private Session mockSession;
    private CriteriaBuilder mockBuilder;
    private CriteriaQuery mockCriteriaQuery;
    private Root mockRoot;
    private Path mockPath;
    private Predicate mockPredicate;
    private Order mockOrder;

    private String propertyName = "prop1";
    private String likeness = "friendly";
    private Integer integerValue = Integer.valueOf(42);
    private Class entityClass = User.class;
    private List<User> candidateList = Lists.newArrayList();

    private CriteriaQueryHelper helper;

    @Before
    @Transactional
    public void populateDbWithTestData() {

        jim.setFirstName(JIM);
        jim.setEcommonsId("e");
        jim.setPassword("p");
        jim.setSalt("s");
        jim.setLastName("Jones");


        jim.setInstitution(institution);
        jim.setInstitutionRole(institutionRole);
        jim.setRole(role);

        save(role);
        save(institutionRole);
        save(institution);
        save(jim);

        jimId = jim.getId();

        //////////////////

        mockSession = mock(Session.class);
        mockBuilder = mock(CriteriaBuilder.class);
        mockRoot = mock(Root.class);
        mockCriteriaQuery = mock(CriteriaQuery.class);
        mockPath = mock(Path.class);
        mockPredicate = mock(Predicate.class);
        mockOrder = mock(Order.class);

        when(mockSession.getCriteriaBuilder()).thenReturn(mockBuilder);
        when(mockBuilder.createQuery(any(Class.class))).thenReturn(mockCriteriaQuery);
        when(mockCriteriaQuery.from(any(Class.class))).thenReturn(mockRoot);

        when(mockRoot.get(any(String.class))).thenReturn(mockPath);
        when(mockPath.in(candidateList)).thenReturn(mockPredicate);

        when(mockBuilder.equal(mockPath, integerValue)).thenReturn(mockPredicate);
        when(mockBuilder.isNull(mockPath)).thenReturn(mockPredicate);
        when(mockBuilder.like(mockPath, likeness)).thenReturn(mockPredicate);

        when(mockBuilder.asc(mockPath)).thenReturn(mockOrder);
        when(mockBuilder.desc(mockPath)).thenReturn(mockOrder);

        when(mockCriteriaQuery.orderBy(mockOrder)).thenReturn(mockCriteriaQuery);

        helper = new CriteriaQueryHelper(mockSession, entityClass);
    }

    @Test
    public void testFindFieldById() {

        final String firstName = dao.findFieldById("User", jimId, "firstName");

        assertEquals(JIM, firstName);

        assertNull(dao.findFieldById("User", Integer.MAX_VALUE, "firstName"));
    }

    @Test
    public void testFindTotalByQuery() {

        final int shouldBeOne = dao.findTotalByQuery("select count(*) from User");

        assertEquals(1, shouldBeOne);

        final int shouldBeZero = dao.findTotalByQuery("select count(*) from User where first_name = 'bob'");

        assertEquals(0, shouldBeZero);

        intercept(Exception.class, () -> dao.findTotalByQuery("select first_name from User where first_name = '" + JIM + "'"));

        intercept(Exception.class, () -> dao.findTotalByQuery("select first_name from User"));

        intercept(Exception.class, () -> dao.findTotalByQuery("askldjalksfjlkasfj"));
    }

    @Test
    public void testFindAll() {
        assertEquals(asList(jim), dao.findAll(User.class));

        assertEquals(emptyList(), dao.findAll(Subject.class));
    }

    @Test
    public void testFindById() {
        Institution institution1 = new Institution("inst1", "institution1");
        Institution institution2 = new Institution("inst2", "institution2");
        Institution institution3 = new Institution("inst3", "institution3");

        save(institution1);
        save(institution2);
        save(institution3);

        Integer id1 = institution1.getId();
        Integer id2 = institution2.getId();
        Integer id3 = institution3.getId();

        final Institution queryInst = dao.findById(Institution.class, id2);

        assertEquals("inst2", queryInst.getName());
    }

    @Test
    public void testCriteriaHelper_equals() {
        helper.whereEquals(propertyName, integerValue);

        verify(mockRoot, times(1)).get(propertyName);
        verify(mockBuilder, times(1)).equal(mockPath, integerValue);
        assertEquals(1, helper.predicates.size());
    }
    @Test
    public void testCriteriaHelper_isNull() {
        helper.whereIsNull(propertyName);

        verify(mockRoot, times(1)).get(propertyName);
        verify(mockBuilder, times(1)).isNull(mockPath);
        assertEquals(1, helper.predicates.size());
    }
    @Test
    public void testCriteriaHelper_whereLike() {
        helper.whereLike(propertyName, likeness);

        verify(mockRoot, times(1)).get(propertyName);
        verify(mockBuilder, times(1)).like(mockPath, likeness);
        assertEquals(1, helper.predicates.size());
    }
    @Test
    public void testCriteriaHelper_whereIn() {
        helper.whereIn(propertyName, candidateList);

        verify(mockRoot, times(1)).get(propertyName);
        verify(mockPath, times(1)).in(candidateList);
        assertEquals(1, helper.predicates.size());
    }
    @Test
    public void testCriteriaHelper_orderAsc() {
        helper.orderAsc(propertyName);

        verify(mockRoot, times(1)).get(propertyName);
        verify(mockBuilder, times(1)).asc(mockPath);
        verify(mockCriteriaQuery, times(1)).orderBy(mockOrder);
        assertEquals(0, helper.predicates.size());
    }
    @Test
    public void testCriteriaHelper_orderDesc() {
        helper.orderDesc(propertyName);

        verify(mockRoot, times(1)).get(propertyName);
        verify(mockBuilder, times(1)).desc(mockPath);
        verify(mockCriteriaQuery, times(1)).orderBy(mockOrder);
        assertEquals(0, helper.predicates.size());
    }
    @Test
    public void testCriteriaHelper_and() {
        helper.whereEquals(propertyName, integerValue);
        helper.whereEquals(propertyName + "1", integerValue);
        helper.whereIsNull(propertyName);
        helper.whereIn(propertyName, candidateList);
        helper.orderAsc(propertyName);
        helper.orderDesc(propertyName);

        verify(mockRoot, times(6)).get(any(String.class));
        verify(mockBuilder, times(2)).equal(mockPath, integerValue);
        assertEquals(4, helper.predicates.size());
    }
}