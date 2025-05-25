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

import com.google.common.collect.Lists;
import org.junit.Test;

import java.util.List;
import java.util.Map;

import static org.springframework.test.util.AssertionErrors.assertEquals;

/**
 * Created with IntelliJ IDEA.
 * User: carl
 * Date: 8/3/16
 * Time: 10:51 AM
 */
public class FieldProcessingEnumTest {
    @Test
    public void testRacePostProcessQueryForSort() {
        String joinChain =
                "left join subject_mrn sm on bv.subject_mrn = sm.id " +
                        "left join subject j on sm.subject = j.id";

        String expected =
                "left join subject_mrn sm on bv.subject_mrn = sm.id " +
                        "left join subject j on sm.subject = j.id " +
                        "left join race race on race.id = j.race";

        FieldProcessingEnum race = FieldProcessingEnum.Race;

        Category category1 = new Category(1000, "CATEGORY1", "CATEGORY1", "C1");

        TemplateCategoryField tcf = new TemplateCategoryField(1, null, category1,
                new Field(1, "subject", "race", "Race"), "_j", true);

        String result = race.postProcessQueryForSort(joinChain, tcf);

        assertEquals("should =", expected, result);

        return;
    }

    @Test
    public void testRacePostFromsForSort() {
        List<String> fromTables = Lists.newArrayList(
                "booked_visit bv inner join subject_mrn sm on bv.sm = sm.id",
                "subject j inner join blah b on j.b = b.j"
        );

        FieldProcessingEnum race = FieldProcessingEnum.Race;

        Category category1 = new Category(1000, "CATEGORY1", "CATEGORY1", "C1");

        TemplateCategoryField tcf = new TemplateCategoryField(1, null, category1,
                new Field(1, "subject", "race", "Race"), "_j", true);

        List<String> resultList = race.postProcessFromTablesForSort(fromTables, tcf);

        assertEquals("0th", "booked_visit bv inner join subject_mrn sm on bv.sm = sm.id", resultList.get(0));
        assertEquals("1sth", "subject j left join race race on race.id = j.race inner join blah b on j.b = b.j", resultList.get(1));

        return;
    }


}
