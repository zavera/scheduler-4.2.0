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
import edu.harvard.catalyst.scheduler.entity.BaseEntity;
import org.hibernate.Session;
import org.hibernate.query.Query;

import javax.persistence.criteria.*;
import java.util.List;

class CriteriaQueryHelper {
    private CriteriaBuilder builder;
    private CriteriaQuery criteria;
    private Root root;
    private Session session;

    // merely package-private, for testing
    List<Predicate> predicates = Lists.newArrayList();

    CriteriaQueryHelper(Session session,
                        final Class<? extends BaseEntity> entityClass) {
        this.session = session;
        builder = session.getCriteriaBuilder();
        criteria = builder.createQuery(entityClass);
        root = criteria.from(entityClass);
        criteria.select(root);
    }

    void whereEquals(final String propertyName, final Object value) {
        Predicate predicate = builder.equal(root.get(propertyName), value);
        predicates.add(predicate);
    }

    void whereIsNull(final String propertyName) {
        Predicate predicate = builder.isNull(root.get(propertyName));
        predicates.add(predicate);
    }

    void whereLike(final String propertyName, final String likeness) {
        Predicate predicate = builder.like(root.get(propertyName), likeness);
        predicates.add(predicate);
    }

    void whereIn(final String propertyName, final List<?> candidateList) {
        Expression<String> nameExpression = root.get(propertyName);
        Predicate predicate = nameExpression.in(candidateList);

        predicates.add(predicate);
    }

    void orderAsc(final String propertyName) {
        criteria.orderBy(builder.asc(root.get(propertyName)));
    }

    void orderDesc(final String propertyName) {
        criteria.orderBy(builder.desc(root.get(propertyName)));
    }

    Query getQuery() {
        if (! predicates.isEmpty()) {
            Predicate[] predicateArray =
                    predicates.toArray(new Predicate[predicates.size()]);

            criteria.where(predicateArray);
        }

        Query query = session.createQuery(criteria);
        return query;
    }
}
