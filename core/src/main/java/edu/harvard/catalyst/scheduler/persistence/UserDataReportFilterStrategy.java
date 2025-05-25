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

import edu.harvard.catalyst.scheduler.entity.BaseEntity;
import edu.harvard.catalyst.scheduler.entity.InstitutionRoleType;
import edu.harvard.catalyst.scheduler.entity.Role;

import java.util.Optional;


/**
 * 
 * @author clint
 * @date Jul 31, 2013
 * 
 */
public enum UserDataReportFilterStrategy {
    LASTNAME(1) {
        @Override
        public void makeCriterion(final ReportDAO dao,
                                       final String filterString,
                                       final CriteriaQueryHelper criteriaHelper) {

            criteriaHelper.whereLike("lastName", filterString);
        }
    },
    INSTITUTION_ROLE(2) {
        //TODO: TEST
        @Override
        public void makeCriterion(final ReportDAO dao,
                                       final String filterString,
                                       final CriteriaQueryHelper criteriaHelper) {
            final InstitutionRoleType institutionRoleType = InstitutionRoleType.fromHumanReadableName(filterString).orNull();

            final BaseEntity ap = dao.findInstitutionRoleByType(institutionRoleType);

            criteriaHelper.whereEquals("institutionRole", ap);
        }
    },
    ROLE(3) {
        //TODO: TEST
        @Override
        public void makeCriterion(final ReportDAO dao,
                                       final String filterString,
                                       final CriteriaQueryHelper criteriaHelper) {
            final Optional<UserRoleType> roleOption = UserRoleType.fromHumanReadableName(filterString);

            final Integer id = roleOption.isPresent() ? roleOption.get().databaseId : null;

            final BaseEntity ap = dao.findById(Role.class, id);

            criteriaHelper.whereEquals("role", ap);
        }
    },
    IS_ACTIVE(4) {
        @Override
        public void makeCriterion(final ReportDAO dao,
                                       final String filterString,
                                       final CriteriaQueryHelper criteriaHelper) {
            criteriaHelper.whereEquals("active", Boolean.TRUE);
        }
    },
    IS_NOT_ACTIVE(5) {
        @Override
        public void makeCriterion(final ReportDAO dao,
                                       final String filterString,
                                       final CriteriaQueryHelper criteriaHelper) {
            criteriaHelper.whereEquals("active", Boolean.FALSE);
        }
    };

    public abstract void makeCriterion(final ReportDAO reportDao,
                                            final String filterString,
                                            final CriteriaQueryHelper criteriaHelper);

    @Deprecated
    public final int databaseId;

    public int getDatabaseId() {
        return databaseId;
    }

    private UserDataReportFilterStrategy(final int databaseId) {
        this.databaseId = databaseId;
    }

    public static Optional<UserDataReportFilterStrategy> fromIdString(final String idString) {
        for(final UserDataReportFilterStrategy strategy : values()) {
            if(String.valueOf(strategy.getDatabaseId()).equals(idString)) {
                return Optional.of(strategy);
            }
        }
        
        return Optional.empty();
    }
}
