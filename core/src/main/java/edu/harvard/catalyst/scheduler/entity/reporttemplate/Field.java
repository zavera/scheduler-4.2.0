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

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import edu.harvard.catalyst.hccrc.core.util.Pairs;
import edu.harvard.catalyst.scheduler.entity.*;
import edu.harvard.catalyst.scheduler.util.DateUtility;

import javax.persistence.*;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static edu.harvard.catalyst.hccrc.core.util.Pair.pair;

/**
 * Created with IntelliJ IDEA.
 * User: carl
 * Date: 10/23/14
 * Time: 11:10 AM
 */
@Entity
@Table(name = "field")
public class Field extends BaseEntity implements Serializable {

    public static class HasReportFiltersNameAndIdPair {
        private int id;
        private String name;

        public HasReportFiltersNameAndIdPair(HasReportFiltersNameAndId theEnum) {
            id = theEnum.getReportFiltersId();
            name = theEnum.getReportFiltersName();
        }
    }

    public enum FieldType {
        Number { // between
            @Override
            public String commaJoinedStringToSqlString(Field myField, String shortName, String commaString) {
                return betweenHelper(shortName, commaString, "'", "'");
            }
        },
        Date { // between
            @Override
            public String commaJoinedStringToSqlString(Field myField, String shortName, String commaString) {
                return dateHelper(shortName, commaString);
            }
        },
        LongSetString {
            @Override
            public String commaJoinedStringToSqlString(Field myField, String shortName, String commaString) {
                return longStringHelper(shortName, commaString);
            }
        },
        ShortString {
            @Override
            public String commaJoinedStringToSqlString(Field myField, String shortName, String commaString) {
                return shortStringHelper(shortName, commaString);
            }
        },
        ShortSetStringUI {
            @Override
            public String commaJoinedStringToSqlString(Field myField, String shortName, String commaString) {
                return innyHelper(shortName, commaString, "'");
            }
        },
        Boolean {
            @Override
            public String commaJoinedStringToSqlString(Field myField, String shortName, String commaString) {
                return booleanHelper(shortName, commaString);
            }
        },
        ShortSetStringDbLongName {
            @Override
            public String commaJoinedStringToSqlString(Field myField, String shortName, String commaString) {
                return innyHelper(shortName, commaString, "'");
            }
        },
        ShortSetStringDbShortName {
            @Override
            public String commaJoinedStringToSqlString(Field myField, String shortName, String commaString) {
                return innyHelper(shortName, commaString, "'");
            }
        },
        ShortSetStringDbIdBack {
            @Override
            public String commaJoinedStringToSqlString(Field myField, String shortName, String commaString) {
                final String nameToId = shortName.replaceAll("\\.name$", ".id");

                return innyHelper(nameToId, commaString, "");
            }
        },
        ShortSetStringDB {
            @Override
            public String commaJoinedStringToSqlString(Field myField, String shortName, String commaString) {
                return innyHelper(shortName, commaString, "'");
            }
        },
        Enum {
            @Override
            public String commaJoinedStringToSqlString(Field myField, String shortName, String commaString) {
                return enumInnyHelper(myField, shortName, commaString, "'");
            }
        };

        public abstract String commaJoinedStringToSqlString(Field myField, String shortName, String commaString);

        public static String quotifyCommaString(String commaString, String openQuote, String closeQuote, String joinString) {
            List<String> stringList = Lists.newArrayList(Splitter.on(",").split(commaString));
            List<String> quotedStringList = stringList.stream().map(s -> openQuote + s + closeQuote).collect(Collectors.toList());
            String result = Joiner.on(joinString).join(quotedStringList);
            return result;
        }

        private static String dateHelper(String shortName, String commaString) {
            List<String> dateList = Lists.newArrayList(Splitter.on(",").split(commaString));
            List<String> sqlDateList = dateList.stream()
                    .map(s -> s.replaceAll(" +(\\d+:\\d+:\\d+).*", " $1"))
                    .map(s -> DateUtility.parse(DateUtility.dateMonthDayYear(), s))
                    .map(d -> DateUtility.format(DateUtility.dateHourMinSec(), d))
                    .collect(Collectors.toList());
            String sqlCommaString = Joiner.on(",").join(sqlDateList);

            return betweenHelper(shortName, sqlCommaString, "timestamp('", "')");
        }

        private static String booleanHelper(String shortName, String commaString) {

            List<String> commaStringValues = Lists.newArrayList(Splitter.on(",").split(commaString));

            List<String> orSubClauses = Lists.newArrayList();

            possiblyMoveNullFromValuesToSubClauses(commaStringValues, orSubClauses, shortName, "");

            if ( ! commaStringValues.isEmpty()) {
                List<String> translatedValues = commaStringValues.stream().map(o -> o.replaceAll("Yes", "true")).map(o -> o.replaceAll("No", "false"))
                        .collect(Collectors.toList());
                String translatedCommaString = Joiner.on(",").join(translatedValues);

                orSubClauses.add(innyHelper(shortName, translatedCommaString, ""));
            }

            StringBuilder builder = new StringBuilder()
                    .append("(")
                    .append(Joiner.on(" OR ").join(orSubClauses))
                    .append(")");


            String result = builder.toString();
            return result;
        }
        private static void possiblyMoveNullFromValuesToSubClauses(final List<String> values, final List<String> clauses, final String shortName, String orEmpty) {
            if (values.contains("Null")) {
                values.remove("Null");
                clauses.add("(" + shortName + " is NULL" + orEmpty + ")");
            }
        }
        private static String shortStringHelper(String shortName, String commaString) {

            List<String> commaStringValues = Lists.newArrayList(Splitter.on(",").split(commaString));

            List<String> orSubClauses = Lists.newArrayList();

            possiblyMoveNullFromValuesToSubClauses(commaStringValues, orSubClauses, shortName, " or " + shortName + " = ''");

            if ( ! commaStringValues.isEmpty()) {
                String nonNullsCommaString = Joiner.on(",").join(commaStringValues);

                orSubClauses.add(innyHelper(shortName, nonNullsCommaString, "'"));
            }

            StringBuilder builder = new StringBuilder()
                    .append("(")
                    .append(Joiner.on(" OR ").join(orSubClauses))
                    .append(")");


            String result = builder.toString();
            return result;
        }
        private static String innyHelper(String shortName, String commaString, String quote) {
            String innyExpression = quotifyCommaString(commaString, quote, quote, ",");
            StringBuilder builder = new StringBuilder()
                    .append("(")
                    .append(shortName)
                    .append(" IN (")
                    .append(innyExpression)
                    .append("))");

            return builder.toString();
        }

        private static String enumInnyHelper(Field myField, String shortName, String commaString, String quote) {
            List<String> enumValues = (Lists.newArrayList(Splitter.on(",").split(commaString))).stream().map(o -> getEnumValue(myField, Integer.valueOf(o))).collect(Collectors.toList());
            String substitutedCommaString = Joiner.on(",").join(enumValues);

            return innyHelper(shortName, substitutedCommaString, quote);
        }

        private static String betweenHelper(String shortName, String commaString, String openQuote, String closeQuote) {
            String betweenAndExpression = quotifyCommaString(commaString, openQuote, closeQuote, " AND ");
            StringBuilder builder = new StringBuilder()
                    .append("(")
                    .append(shortName)
                    .append(" BETWEEN ")
                    .append(betweenAndExpression)
                    .append(")");

            return builder.toString();
        }
        private static String longStringHelper(String shortName, String commaString) {
            List<String> components = Lists.newArrayList(Splitter.on(",").split(commaString));

            if (components.size() == 2) {
                // two mutually exhaustive options have been chosen: includes, does not include
                // so this conjunct of the where 'and' clauses should not exclude anything
                return "true";
            }

            String emptyPhraseConnector;
            String emptyPhrase2;
            String nonOrNull;
            if (components.get(0).trim().equalsIgnoreCase("includes")) {
                nonOrNull = " NOT NULL";
                emptyPhraseConnector = " AND";
                emptyPhrase2 = " != ''";
            }
            else {
                nonOrNull = " NULL";
                emptyPhraseConnector = " OR";
                emptyPhrase2 = " = ''";
            }

            StringBuilder builder = new StringBuilder()
                    .append("(")
                    .append(shortName)
                    .append(" IS")
                    .append(nonOrNull)
                    .append(emptyPhraseConnector)
                    .append(" ")
                    .append(shortName)
                    .append(emptyPhrase2)
                    .append(")");

            return builder.toString();
        }

        // this is a code-based solution for mapping table/column to enums.
        // so any changes to the set of fields with FieldType Enum need to be incorporated here
        // couple of alternatives to this approach: (1) reflection, (2) DB-driven
        ///////////////

        public static List<HasReportFiltersNameAndId> getFieldTypeEnums(Field enumTypeField) {
            String table = enumTypeField.getTable();
            String column = enumTypeField.getColumn();

            Map<Integer, HasReportFiltersNameAndId> hasEtcMap = mapTableToColumnToIntegerToHasEtc.get(table).get(column);

            return Lists.newArrayList(hasEtcMap.values());
        }

        public static String getEnumValue(Field enumTypeField, int ordinal) {
            String table = enumTypeField.getTable();
            String column = enumTypeField.getColumn();

            String result = mapTableToColumnToIntegerToHasEtc.get(table).get(column).get(ordinal).name();

            return result;
        }

        static private Map<String, Map<String, Map<Integer, HasReportFiltersNameAndId>>> mapTableToColumnToIntegerToHasEtc;
        static {
            mapTableToColumnToIntegerToHasEtc = Maps.newHashMap();

            Map<String, Map<Integer, HasReportFiltersNameAndId>> userToHasEtcList = Maps.newHashMap();
            Map<String, Map<Integer, HasReportFiltersNameAndId>> visitTemplateToHasEtcList = Maps.newHashMap();
            Map<String, Map<Integer, HasReportFiltersNameAndId>> bookedVisitToHasEtcList = Maps.newHashMap();
            Map<String, Map<Integer, HasReportFiltersNameAndId>> resourceToHasEtcList = Maps.newHashMap();

            userToHasEtcList.put("institution_role", enumArrayToMapIntegerToHasEtc(InstitutionRoleType.values()));
            userToHasEtcList.put("role",             enumArrayToMapIntegerToHasEtc(RoleType.values()));
            resourceToHasEtcList.put("type",         enumArrayToMapIntegerToHasEtc(ResourceType.values()));
            
            mapTableToColumnToIntegerToHasEtc.put("user", userToHasEtcList);
            mapTableToColumnToIntegerToHasEtc.put("visit_template", visitTemplateToHasEtcList);
            mapTableToColumnToIntegerToHasEtc.put("booked_visit", bookedVisitToHasEtcList);
            mapTableToColumnToIntegerToHasEtc.put("resource", resourceToHasEtcList);
        }
    }
    public String commaJoinedStringToSqlString(String shortName, String commaString) {
        return fieldType.commaJoinedStringToSqlString(this, shortName, commaString);
    }

    private static Map<Integer, HasReportFiltersNameAndId> enumArrayToMapIntegerToHasEtc(Enum[] enumArray) {

        List<HasReportFiltersNameAndId> hasReportFiltersNameAndIdList = Lists.newArrayList(enumArray).stream()
                .map(e -> (HasReportFiltersNameAndId)e).collect(Collectors.toList());

        Map<Integer, HasReportFiltersNameAndId> result = hasReportFiltersNameAndIdList.stream().collect(Collectors
                .toMap(HasReportFiltersNameAndId::getReportFiltersId, Function.identity()));

        return result;
    }
    private static final long serialVersionUID = 1L;

    private String table;

    private String column;
    private String displayName;

    private boolean sortable;
    private FieldType fieldType;

    private FieldProcessingEnum fieldProcessingEnum;

    private static Map<String, String> fkFieldToTable = Pairs.toMap(
            pair("cancel_status", "cancellation_status")
    );
    private static Map<String, String> userSuffixToJoinColumn = Pairs.toMap(
            pair("_inv", "principal_investigator"),
            pair("_sch", "primary_scheduling_contact"),
            pair("_nrs", "protocol_nurse"),
            pair("_nut", "protocol_nutritionist"),
            pair("_doc", "responsible_physician"),
            pair("_doc2","responsible_physician2"),
            pair("_sch2","secondary_scheduling_contact"),
            pair("_sch3","scheduler3"),
            pair("_nrs2","associate_nurse"),
            pair("_nut2","associate_protocol_nutritionist")
    );

    public static String getFkFieldToTable(String fkColumn) {
        String lookup = fkFieldToTable.get(fkColumn);

        // identity map be default
        return lookup != null ? lookup : fkColumn;
    }


    public static Map<String, String> getUserSuffixToJoinColumn() {
        return userSuffixToJoinColumn;
    }

    private static Map<String, String> tableAlias = Pairs.toMap(
        pair("booked_visit", "bv"),
        pair("visit_template", "vt"),
        pair("subject", "j"),
        pair("booked_resource", "br"),
        pair("template_resource", "tr"),
        pair("override_booked_resource_annotations", "bra"),
        pair("template_resource_annotations", "tra"),
        pair("line_level_annotations", "lla"),
        pair("study", "s"),
        pair("user", "u"),

        pair("study_funding_source_cai", "sfs"),
        pair("subject_mrn", "sm"),
        pair("resource_type", "rsrc_type"),
        pair("resource", "rsrc"));

    public Field() {
        this(null,null,null,null);
    }

    public Field(final Integer id, final String table, final String column, final String displayName) {
        super(id);

        this.table = table;
        this.column = column;
        this.displayName = displayName;
        fieldProcessingEnum = FieldProcessingEnum.DEFAULT;
    }

    public static String getTableAlias(final String table) {
        return tableAlias.get(table);
    }

    @Column(name = "processing")
    @Enumerated(EnumType.STRING)
    @Basic(optional = true)
    public FieldProcessingEnum getFieldProcessingEnum() {
        return fieldProcessingEnum;
    }
    public void setFieldProcessingEnum(final FieldProcessingEnum fieldProcessingEnum) {
        this.fieldProcessingEnum = fieldProcessingEnum;
    }

    @Column(name = "table_name")
    public String getTable() {
        return table;
    }
    public void setTable(final String table) {
        this.table = table;
    }

    @Column(name = "name")
    public String getColumn() {
        return column;
    }
    public void setColumn(final String column) {
        this.column = column;
    }

    @Column(name = "display_name")
    public String getDisplayName() {
        return displayName;
    }
    public void setDisplayName(final String displayName) {
        this.displayName = displayName;
    }

    @Column(name = "sortable")
    public boolean isSortable() {
        return sortable;
    }

    public void setSortable(boolean sortable) {
        this.sortable = sortable;
    }

    @Column(name = "type")
    @Enumerated(EnumType.STRING)
    public FieldType getFieldType() {
        return fieldType;
    }

    public void setFieldType(FieldType fieldType) {
        this.fieldType = fieldType;
    }
}
