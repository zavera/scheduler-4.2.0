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

import java.util.Optional;

import static edu.harvard.catalyst.hccrc.core.util.ListUtils.enrich;

/**
 * @author clint
 * @date Jul 31, 2013
 * 
 */
public enum SortStrategy {
    ASCENDING(1, "ASC") {
        @Override
        public void makeOrder(final String columnName,
                               final CriteriaQueryHelper criteriaQueryHelper) {

            criteriaQueryHelper.orderAsc(columnName);
        }
    },
    DESCENDING(2, "DESC") {
        @Override
        public void makeOrder(final String columnName,
                               final CriteriaQueryHelper criteriaQueryHelper) {
            criteriaQueryHelper.orderDesc(columnName);
        }
    };

    public final int sortId;
    
    public final String orderTerm;

    public boolean isAscending() {
        return this == ASCENDING;
    }
    
    public boolean isDescending() {
        return this == DESCENDING;
    }
    
    private SortStrategy(final int sortId, final String orderTerm) {
        this.sortId = sortId;
        this.orderTerm = orderTerm;
    }
    
    public final String makeHqlOrderBySubClause(final String columnName) {
        return " ORDER BY " + columnName + " " + orderTerm;
    }
    
    public abstract void makeOrder(final String columnName, final CriteriaQueryHelper criteriaQueryHelper);

    public static Optional<SortStrategy> fromOptionalId(final Optional<String> idString) {
        return idString.flatMap(SortStrategy::fromIdString);
    }

    public static Optional<SortStrategy> fromIdString(final String idString) {
        return enrich(values()).find(strategy -> String.valueOf(strategy.sortId).equals(idString));
    }
}
