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
package edu.harvard.catalyst.scheduler.entity;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import edu.harvard.catalyst.scheduler.entity.reporttemplate.Field;
import org.junit.Test;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static edu.harvard.catalyst.scheduler.entity.reporttemplate.Field.FieldType;
import static org.junit.Assert.assertEquals;

public final class FieldTest {
    @Test
    public void testGetEnumValues() throws Exception {
        testGetEnumValuesHelper("user", "institution_role",
                                            "Front Desk,General View,Resource Manager,Schedulers,Study Staff,Super Admin");
        testGetEnumValuesHelper("user", "role",
                                            "Administrative Director,Associate Nurse,Associate RD,CRC Administration," +
                                                    "Co-Investigator,Final Approver,Lab,Nurse,Nurse Manager Director," +
                                                    "Nutrition Manager,Nutritionist,Other Study Staff," +
                                                    "PI,Physician,Physician 2,Protocol Nurse,Protocol Nutritionist," +
                                                    "Scheduler,Scheduler 2,Study Coordinator Research Assistant");
        testGetEnumValuesHelper("resource", "type",
                                            "Lab,Nursing,Nutrition,Other,Room");
    }

    private void testGetEnumValuesHelper(String table, String column, String expectedValue) {
        Field enumTypeField = new Field();
        enumTypeField.setTable(table);
        enumTypeField.setColumn(column);

        List<HasReportFiltersNameAndId> enumList = Field.FieldType.getFieldTypeEnums(enumTypeField);
        List<String> enumValues =
                Lists.newArrayList(enumList.stream().map(o -> o.getReportFiltersName()).collect(Collectors.toList()));
        Collections.sort(enumValues);
        String enumValuesString = Joiner.on(",").join(enumValues);

        assertEquals(table + "." + column, expectedValue, enumValuesString);
    }

    @Test
    public void testCommaJoinedStringToSqlString() throws Exception {
        testCommaJoinedStringToSqlStringHelper(FieldType.Enum,                       "2,3",                   "(XX IN ('Room','Lab'))");
        testCommaJoinedStringToSqlStringHelper(FieldType.Boolean,                    "Yes",                   "((XX IN (true)))");
        testCommaJoinedStringToSqlStringHelper(FieldType.Boolean,                    "Yes,Null",              "((XX is NULL) OR (XX IN (true)))");
        testCommaJoinedStringToSqlStringHelper(FieldType.Boolean,                    "Null",                  "((XX is NULL))");
        testCommaJoinedStringToSqlStringHelper(FieldType.Date,                       "Sat Aug 01 2015 00:00:00 GMT-0400 (EDT),Sat Aug 01 2015 23:59:59 GMT-0400 (EDT)", "(XX BETWEEN timestamp('2015-08-01 00:00:00') AND timestamp('2015-08-01 23:59:59'))");
        testCommaJoinedStringToSqlStringHelper(FieldType.LongSetString,              "includes",              "(XX IS NOT NULL AND XX != '')");
        testCommaJoinedStringToSqlStringHelper(FieldType.LongSetString,              "not includes",          "(XX IS NULL OR XX = '')");
        testCommaJoinedStringToSqlStringHelper(FieldType.Number,                     "3,4",                   "(XX BETWEEN '3' AND '4')");
        testCommaJoinedStringToSqlStringHelper(FieldType.ShortSetStringDB,           "one,two",               "(XX IN ('one','two'))");
        testCommaJoinedStringToSqlStringHelper(FieldType.ShortSetStringDbLongName,   "one,two",               "(XX IN ('one','two'))");
        testCommaJoinedStringToSqlStringHelper(FieldType.ShortSetStringDbShortName,  "one,two",               "(XX IN ('one','two'))");
        testCommaJoinedStringToSqlStringHelper(FieldType.ShortSetStringUI,           "one,two",               "(XX IN ('one','two'))");
        testCommaJoinedStringToSqlStringHelper(FieldType.ShortString,                "one,two",               "((XX IN ('one','two')))");
        testCommaJoinedStringToSqlStringHelper(FieldType.ShortString,                "one,two,Null",          "((XX is NULL or XX = '') OR (XX IN ('one','two')))");
    }

    private void testCommaJoinedStringToSqlStringHelper(FieldType fieldType, String commaString, String expected) {
        Field field = new Field();
        field.setFieldType(FieldType.Enum);
        field.setTable("resource");
        field.setColumn("type");

        String whereExpression = fieldType.commaJoinedStringToSqlString(field, "XX", commaString);

        assertEquals(fieldType.name(), expected, whereExpression);
    }
}

