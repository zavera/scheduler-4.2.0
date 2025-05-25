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

import edu.harvard.catalyst.scheduler.core.SchedulerRuntimeException;
import edu.harvard.catalyst.scheduler.entity.BaseEntity;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.NativeQuery;
import org.hibernate.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigInteger;
import java.util.List;

@Transactional
public abstract class SiteDAO {

    @Autowired
    protected SessionFactory sessionFactory;

    private static String fullGroupByString = "ONLY_FULL_GROUP_BY";

    //TODO: Should flush() call go in a finally block?
    public void createEntity(final BaseEntity entity) {
        final Session session = session();

        session.save(entity);

        session.flush();
    }

    //TODO: Should flush() call go in a finally block?
    public void updateEntity(final BaseEntity entity) {
        final Session session = session();

        session.update(entity);

        session.flush();
    }

    //TODO: Should flush() call go in a finally block?
    public void deleteEntity(final BaseEntity entity) {
        final Session session = session();

        session.delete(entity);

        session.flush();
    }

    // Needed by database-aware unit-tests
    public void refreshEntity(final BaseEntity entity) {
        final Session session = session();
        session.refresh(entity);
    }

    @SuppressWarnings("unchecked")
    public <T extends BaseEntity> T findById(final Class<T> entityClass, final Integer id) {
        final CriteriaQueryHelper criteriaHelper = new CriteriaQueryHelper(session(), entityClass);

        criteriaHelper.whereEquals("id", id);
        Query query = criteriaHelper.getQuery();

        return (T) query.uniqueResult();
    }

    @SuppressWarnings("unchecked")
    protected <T extends BaseEntity> List<T> findAll(final Class<T> entityClass) {
        final CriteriaQueryHelper criteriaHelper = new CriteriaQueryHelper(session(), entityClass);

        Query query = criteriaHelper.getQuery();

        return (List<T>) query.list();
    }

    public String findNameById(final String tableName, final Integer id, final boolean first) {
        return first ? findFieldById(tableName, id, "firstName")
                : findFieldById(tableName, id, "name");
    }

    public String findNameById(final String tableName, final Integer id) {
        return findFieldById(tableName, id, "name");
    }

    public String findFieldById(final String tableName, final Integer id, final String fieldName) {
        final String findName = "SELECT table." + fieldName + " FROM " + tableName + " table  WHERE table.id = :id ";

        final Query query = newQuery(findName);

        query.setParameter("id", id);
        query.setCacheable(true);

        final String result = (String) query.uniqueResult();

        return result;
    }
    public BaseEntity findEntityByFieldString(final String tableName, final String fieldName, final String value) {
        BaseEntity result = null;

        final String queryString = "SELECT t FROM " + tableName + " t  " +
                "WHERE UPPER(t." + fieldName + ") = UPPER('" +value+ "')";

        final Query<BaseEntity> query = newQuery(queryString);

        query.setCacheable(true);

        final List<BaseEntity> results = query.list();
        if (results.size() == 0) {
            SchedulerRuntimeException.logDontThrow("No results for " + tableName + ":" + fieldName + ":" + value);
        }
        else {
            result = results.get(0);
            if (results.size() > 1) {
                SchedulerRuntimeException.logDontThrow("Choosing first from multiple results for " + tableName + ":" + fieldName + ":" + value);
            }
        }

        return result;
    }

    public int findTotalByQuery(final String sqlQuery) {

        final BigInteger bi = (BigInteger) newNativeQuery(sqlQuery).uniqueResult();

        return bi.intValue();
    }

    Session session() {
        Session session = sessionFactory.getCurrentSession();
        return session;
    }

    public Query newQuery(final String hql) {
        final Session session = session();

        return session.createQuery(hql);
    }

    protected NativeQuery newNativeQuery(final String sql) {
        final Session session = session();

        return session.createNativeQuery(sql);
    }

    String getMode(Session session) {
        final String queryString = "SELECT @@sql_mode";
        final NativeQuery query = session.createSQLQuery(queryString);
        final String mode = (String)query.getSingleResult();

        return mode.toUpperCase();
    }
}
