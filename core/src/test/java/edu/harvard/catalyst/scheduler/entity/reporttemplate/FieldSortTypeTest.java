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

import edu.harvard.catalyst.scheduler.entity.reporttemplate.FieldSortType;
import org.junit.Test;

import static junit.framework.TestCase.assertEquals;

public final class FieldSortTypeTest {
    @Test
    public void testApplyChangeIfNotAlreadyPresent() throws Exception {
        applyChangeIfNotAlreadyPresentHelper("blah on user", " on user", " on developer", "blah on user on developer");
        applyChangeIfNotAlreadyPresentHelper("blah on user on developer", " on user", " on developer", "blah on user on developer");
        applyChangeIfNotAlreadyPresentHelper("blah on user more blah on developer", " on user", " on developer", "blah on user more blah on developer");
    }

    private void applyChangeIfNotAlreadyPresentHelper(String query, String lhs, String rhs, String expected) {
        String result = FieldSortType.applyChangeIfNotAlreadyPresent(query, lhs, rhs);

        assertEquals(expected, result);
    }

    @Test
    public void testPostProcessQueryForSortStudyFundingSource() throws Exception {
        Category category1 = new Category(1000, "CATEGORY1", "CATEGORY1", "C1");

        TemplateCategoryField tcf = new TemplateCategoryField(1, null, category1,
                new Field(1, "study_funding_source_cai", "funding_source", "Funding_source"), "_f1", true);

        String query = " INNER JOIN study s left join study_funding_source_cai sfs_f1 on (sfs_f1.study = s.id and sfs_f1.one_to_four = 1)";

        String expected = " INNER JOIN study s " +
                "left join study_funding_source_cai sfs_f1 on (sfs_f1.study = s.id and sfs_f1.one_to_four = 1) " +
                "left join funding_source funding_source_sfs_f1 on sfs_f1.funding_source = funding_source_sfs_f1.id";

        String result = FieldSortType.postProcessQueryForSortStudyFundingSource(query, tcf);

        assertEquals("==", expected, result);
    }

    @Test
    public void testPostProcessQueryForSortUserStaff() throws Exception {
        Category category1 = new Category(1000, "CATEGORY1", "CATEGORY1", "C1");

        TemplateCategoryField tcf = new TemplateCategoryField(1, null, category1,
                new Field(1, "user", "faculty_rank", "Faculty Rank"), "_inv", true);

        String query = " INNER JOIN study s left join user u_inv on s.principal_investigator = u_inv.id";

        String expected = " INNER JOIN study s left join user u_inv on s.principal_investigator = u_inv.id " +
                "left join faculty_rank faculty_rank_u_inv on u_inv.faculty_rank = faculty_rank_u_inv.id ";

        String result = FieldSortType.postProcessQueryForSortUserStaff(query, tcf);

        assertEquals("==", expected, result);
    }

}

