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
import java.util.stream.Collectors;

/**
 * Created with IntelliJ IDEA.
 * User: carl
 * Date: 9/18/15
 * Time: 4:49 PM
 */
public enum FieldSortType {
    Default {
        @Override
        public String sortOrderByPhrase(String sortScalarColumn,
                                        String joinTable,
                                        String orderBy) {
            return sortOrderByPhraseUseBareScalarColumn(sortScalarColumn, orderBy);
        }
    },

    InnerJoinShortStringDB {
        @Override
        public void sortFromTableAddition(String joinedTable,
                                          List<String> fromTables) {
            sortFromTableAdditionManyShortDBs(joinedTable, fromTables);
        }

        @Override
        public void sortWhereClauseAddition(String joinedTable,
                                            String lhsColumn,
                                            List<String> whereClauses) {
            sortWhereClauseAdditionManyShortDBs(joinedTable, lhsColumn, whereClauses);
        }
    },
    UserStaff {
        @Override
        public String postProcessQueryForSort(String query, TemplateCategoryField tcf) {
            return postProcessQueryForSortUserStaff(query, tcf);
        }

        @Override
        public String sortOrderByPhrase(String sortScalarColumn,
                                        String joinTable,
                                        String orderBy) {
            return sortOrderByPhraseUseScalarColumnName(sortScalarColumn, orderBy);
        }
    },
    StudyFundingSource {
        @Override
        public String postProcessQueryForSort(String query, TemplateCategoryField tcf) {
            return postProcessQueryForSortStudyFundingSource(query, tcf);
        }

        @Override
        public String sortOrderByPhrase(String sortScalarColumn,
                                        String joinTable,
                                        String orderBy) {
            return sortOrderByPhraseUseScalarColumnName(sortScalarColumn, orderBy);
        }
    },
    LeftJoinShortStringDb {
        @Override
        public List<String> postProcessFromTablesForSort(List<String> fromTables, TemplateCategoryField tcf) {
            return postProcessFromTablesForSortLeftJoinDb(fromTables, tcf);
        }
        @Override
        public String postProcessQueryForSort(String query, TemplateCategoryField tcf) {
            return postProcessJoinChainForSortLeftJoinDb(query, tcf);
        }

    },
    LeftJoinShortStringDbStudy(
            "(INNER JOIN study s)",
            "$1 left join institution institution on s.institution = institution.id") {
        @Override
        public List<String> postProcessFromTablesForSort(List<String> fromTables, TemplateCategoryField tcf) {
            return postProcessFromTablesForSortLeftJoinDb(fromTables, tcf);
        }
    },

    TemplateResourceSublocation(
            "(left join template_resource tr on tr.visit_template = vt.id)",
            "$1 left join resource resource_trs on tr.resource = resource_trs.id " +
                    "  left join resource_sublocation resource_sublocation_trs on resource_sublocation_trs.resource = resource_trs.id " +
                    "  left join sublocation sublocation_trs on resource_sublocation_trs.sublocation = sublocation_trs.id ") {
        @Override
        public String sortOrderByPhrase(String sortScalarColumn,
                                        String joinTable,
                                        String orderBy) {
            return "sublocation_trs.name " + orderBy;
        }
    },
    TemplateResourceResource(
            "(left join template_resource tr on tr.visit_template = vt.id)",
            "$1 left join resource_trr resource on tr.resource = resource_trr.id"
    ) {
        @Override
        public String sortOrderByPhrase(String sortScalarColumn,
                                        String joinTable,
                                        String orderBy) {
            return "resource_trr.name " + orderBy;
        }
    };

    public void sortFromTableAddition(String joinedTable,
                                      List<String> fromTables) {
    }

    public void sortWhereClauseAddition(String joinedTable,
                                        String lhsColumn,
                                        List<String> whereClauses) {
    }

    public String sortOrderByPhrase(String sortScalarColumn,
                                    String joinTable,
                                    String orderBy) {
        return sortOrderByPhraseUseJoinColumn(joinTable, orderBy);
    }

    private static void sortFromTableAdditionManyShortDBs(String joinedTable,
                                                          List<String> fromTables) {
        String joinTableFrom = joinedTable + " " + joinedTable;
        fromTables.add(0, joinTableFrom);
    }

    private static void sortWhereClauseAdditionManyShortDBs(String joinedTable,
                                                            String lhsColumn,
                                                            List<String> whereClauses) {
        String joinCondition = lhsColumn + "=" + joinedTable + ".id";
        whereClauses.add(joinCondition);
    }

    private static String sortOrderByPhraseUseJoinColumn(String joinTable,
                                                         String orderBy) {
        return joinTable + ".name " + orderBy;
    }

    private static String sortOrderByPhraseUseBareScalarColumn(String sortScalarColumn,
                                                               String orderBy) {
        return sortScalarColumn + " " + orderBy;
    }

    private static String sortOrderByPhraseUseScalarColumnName(String sortScalarColumn,
                                                               String orderBy) {
        return sortScalarColumn + ".name " + orderBy;
    }

    FieldSortType() {
        this("(.*)", "$1");
    }

    FieldSortType(String replacementLhs, String replacementRhs) {
        this.replacementLhs = replacementLhs;
        this.replacementRhs = replacementRhs;
    }

    private String replacementRhs;
    private String replacementLhs;

    // default implementation
    public String postProcessQueryForSort(String query, TemplateCategoryField tcf) {

        return query.replaceAll(replacementLhs, replacementRhs);
    }
    // default implementation
    public List<String> postProcessFromTablesForSort(List<String> fromTables, TemplateCategoryField tcf) {
        return fromTables;
    }

    static String postProcessQueryForSortUserStaff(String query, TemplateCategoryField tcf) {
        String userFkTable = tcf.getField().getColumn();
        String userTableSuffix = tcf.getSuffix();
        String studyFkTable = Field.getUserSuffixToJoinColumn().get(userTableSuffix);

        String intermediateJoinTable = userFkTable + "_u" + userTableSuffix;

        String myReplacementLhs = "left join user u" + userTableSuffix + " on s." + studyFkTable + " = u" + userTableSuffix + ".id";
        String myReplacementRhs = " left join " + userFkTable + " " + intermediateJoinTable +
                " on u" + userTableSuffix + "." + userFkTable + " = " + intermediateJoinTable + ".id ";

        String result = applyChangeIfNotAlreadyPresent(query, myReplacementLhs, myReplacementRhs);
        return result;
    }

    static String postProcessQueryForSortStudyFundingSource(String query, TemplateCategoryField tcf) {

        String studyFkTable = tcf.getField().getColumn();
        String studyTableSuffix = tcf.getSuffix();
        String oneToFour = studyTableSuffix.replaceAll("_f", "");

        String intermediateJoinTable = studyFkTable + "_sfs" + studyTableSuffix;

        String myReplacementLhs = "left join study_funding_source_cai sfs" + studyTableSuffix + " on \\(sfs" + studyTableSuffix +
                ".study = s.id and sfs" + studyTableSuffix + ".one_to_four = " + oneToFour + "\\)";
        String myReplacementRhs = " left join " + studyFkTable + " " + intermediateJoinTable +
                " on sfs" + studyTableSuffix + "." + studyFkTable + " = " + intermediateJoinTable + ".id";

        String result = applyChangeIfNotAlreadyPresent(query, myReplacementLhs, myReplacementRhs);
        return result;
    }

    static List<String> postProcessFromTablesForSortLeftJoinDb(List<String> fromTables, TemplateCategoryField tcf) {
        String table = tcf.getField().getTable();
        String tableAlias = Field.getTableAlias(table);
        String fkColumn = tcf.getField().getColumn();
        String joinedTable = Field.getFkFieldToTable(fkColumn);

        return fromTables.stream()
                .map(t -> applyChangeIfNotAlreadyPresent(t, table + " " + tableAlias, " left join "
                        + joinedTable + " " + fkColumn + " on " + fkColumn + ".id = " +tableAlias+ "." + fkColumn))
                .collect(Collectors.toList());
    }

    static String postProcessJoinChainForSortLeftJoinDb(String joinChain, TemplateCategoryField tcf) {
        String table = tcf.getField().getTable();
        String tableAlias = Field.getTableAlias(table);
        String fkColumn = tcf.getField().getColumn();
        String joinedTable = Field.getFkFieldToTable(fkColumn);

        String result = applyChangeIfNotAlreadyPresent(joinChain, table + " " + tableAlias, " left join "
                        + joinedTable + " " + fkColumn + " on " + fkColumn + ".id = " +tableAlias+ "." + fkColumn);
        return result;
    }

    static String applyChangeIfNotAlreadyPresent(String enclosingString, String lhs, String rhs) {
        if (enclosingString.matches(".*" + lhs + ".*" + rhs + ".*")) {
            return enclosingString;
        }
        String reToReplace = "(" + lhs + ")(\\s*on\\s*\\w+\\.\\w+\\s*=\\s*\\w+\\.\\w+)?";
        String result = enclosingString.replaceAll(reToReplace, "$1$2" + rhs);
        return result;
    }
}
