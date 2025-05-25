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

import java.util.List;

/**
 * @author Carl Woolf
 */
public enum FieldProcessingEnum {
    DEFAULT,
    actualDuration,
    scheduleDuration,
    visitVariation,
    actualDurationOvernights,
    earliestBookedVisit,
    latestBookedVisit,
    decrypt,

    StudyStatus,
    Resource,

    DefaultPlusAnnotations {
        @Override
        public String getStaticClassName() {
            return "LineLevelAnnotations";
        }
    },

    SublocationFromTr(FieldSortType.TemplateResourceSublocation) {
        @Override
        public String getStaticClassName() {
            return "Sublocation";
        }
    },
    ResourceFromTr(FieldSortType.TemplateResourceResource) {
        @Override
        public String getStaticClassName() {
            return "Resource";
        }
    },
    Department(FieldSortType.UserStaff),
    Division(FieldSortType.UserStaff),
    FacultyRank(FieldSortType.UserStaff),
    Credential(FieldSortType.UserStaff),
    InstitutionRole(FieldSortType.UserStaff),
    Role(FieldSortType.UserStaff),
    Institution(FieldSortType.UserStaff),

    CentersAndInstitutions(FieldSortType.StudyFundingSource),
    FundingSource(FieldSortType.StudyFundingSource),

    Sublocation(FieldSortType.InnerJoinShortStringDB),
    AppointmentStatus(FieldSortType.InnerJoinShortStringDB),
    VisitType(FieldSortType.InnerJoinShortStringDB),

    Ethnicity(FieldSortType.LeftJoinShortStringDb),
    Gender(FieldSortType.LeftJoinShortStringDb),

    InstitutionFromStudy(FieldSortType.LeftJoinShortStringDbStudy) {
        @Override
        public String getStaticClassName() {
            return "Institution";
        }
    },
    MrnFromSubjectMrn,
    AppointmentStatusReason(FieldSortType.LeftJoinShortStringDb),
    CancellationStatus(FieldSortType.LeftJoinShortStringDb),
    Country(FieldSortType.LeftJoinShortStringDb),
    State(FieldSortType.LeftJoinShortStringDb),
    Race(FieldSortType.LeftJoinShortStringDb),
    IRBInstitution(FieldSortType.LeftJoinShortStringDb);

    FieldSortType sortType;

    FieldProcessingEnum() {
        this(FieldSortType.Default);
    }

    FieldProcessingEnum(FieldSortType sortType) {
        this.sortType = sortType;
    }
    ////////////////////////////////////////////////////////////////

    // override or don't use for enum-values that don't name() tables
    public String getStaticClassName() {
        return name();
    }

    // delegation
    public void sortFromTableAddition(String joinedTable,
                                      List<String> fromTables) {
        sortType.sortFromTableAddition(joinedTable, fromTables);
    }

    public void sortWhereClauseAddition(String joinedTable,
                                        String lhsColumn,
                                        List<String> whereClauses) {
        sortType.sortWhereClauseAddition(joinedTable, lhsColumn, whereClauses);
    }

    public String sortOrderByPhrase(String sortScalarColumn,
                                    String joinTable,
                                    String orderBy) {
        return sortType.sortOrderByPhrase(sortScalarColumn, joinTable, orderBy);
    }
    public String postProcessQueryForSort(String query, TemplateCategoryField tcf) {

        return sortType.postProcessQueryForSort(query, tcf);
    }

    public List<String> postProcessFromTablesForSort(List<String> fromTables, TemplateCategoryField tcf) {
        return sortType.postProcessFromTablesForSort(fromTables, tcf);
    }
}
