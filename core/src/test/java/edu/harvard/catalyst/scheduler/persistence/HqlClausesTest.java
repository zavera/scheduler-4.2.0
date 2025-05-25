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

import org.junit.Test;

import static edu.harvard.catalyst.scheduler.persistence.HqlClauses.WhereBuilder;
import static edu.harvard.catalyst.scheduler.persistence.HqlClauses.whereBuilder;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


/**
 * @author Bill Simons
 * @date 2/20/14
 * @link http://cbmi.med.harvard.edu
 * @link http://chip.org
 */
public class HqlClausesTest {
    @Test
    public void testLikeWithPresentOptional() throws Exception {
        String actual = whereBuilder().like(Optional.of("foo"), "fooColumn").build();
        assertEquals("where fooColumn like '%foo%' ", actual);
    }

    @Test
    public void testLikeWithAbsentOptional() throws Exception {
        String actual = whereBuilder().like(Optional.empty(), "fooColumn").build();
        assertTrue(actual.isEmpty());
    }

    @Test
    public void testInWithPresentOptional() throws Exception {
        String actual = whereBuilder().in(Optional.of("foo"), "value1", "value2", "value3").build();
        assertEquals("where foo in (value1, value2, value3) ", actual);
    }

    @Test
    public void testInWithAbsentOptional() throws Exception {
        String actual = whereBuilder().in(Optional.empty(), "value1", "value2", "value3").build();
        assertTrue(actual.isEmpty());
    }

    @Test
    public void testEqualWithPresentOptional() throws Exception {
        String actual = whereBuilder().equalTo(Optional.of("foo"), "fooColumn").build();
        assertEquals("where fooColumn = foo ", actual);
    }

    @Test
    public void testEqualWithAbsentOptional() throws Exception {
        String actual = whereBuilder().equalTo(Optional.empty(), "fooColumn").build();
        assertTrue(actual.isEmpty());
    }

    @Test
    public void testBetween() throws Exception {
        String actual = whereBuilder().between("middle", "low", "high").build();
        assertEquals("where (middle BETWEEN low AND high) ", actual);
    }

    @Test
    public void testBeforeAndAfter() throws Exception {
        String actual = whereBuilder().beforeAndAfter("earlyParam", "earlyTime", "lateParam", "lateTime").build();
        assertEquals("where (earlyTime >= earlyParam AND lateTime <= lateParam) ", actual);
    }

    @Test
    public void testCombineAndWithPresents() throws Exception {
        WhereBuilder builder = whereBuilder();
        builder.equalTo(Optional.of("foo"), "fooColumn")
                .equalTo(Optional.empty(), "argamarg")
                .equalTo(Optional.of("bar"), "barColumn")
                .equalTo(Optional.of("baz"), "bazColumn");

        Optional<String> optionalResult = builder.combineViaAnd(HqlClauses.Parens.DONT_USE);
        String result = optionalResult.orElse("sorry");
        assertEquals("fooColumn = foo and barColumn = bar and bazColumn = baz", result);
    }

    @Test
    public void testCombineAndWithAbsentOptional() throws Exception {
        WhereBuilder builder = whereBuilder().equalTo(Optional.empty(), "fooColumn");
        Optional<String> optionalResult = builder.combineViaAnd(HqlClauses.Parens.DONT_USE);
        assertTrue(! optionalResult.isPresent());
    }

    @Test
    public void testCombineOrWithPresentsAndParens() throws Exception {
        WhereBuilder builder = whereBuilder();
        builder.equalTo(Optional.of("foo"), "fooColumn")
                .equalTo(Optional.empty(), "argamarg")
                .equalTo(Optional.of("bar"), "barColumn")
                .equalTo(Optional.of("baz"), "bazColumn");

        Optional<String> optionalResult = builder.combineViaOr(HqlClauses.Parens.USE);
        String result = optionalResult.orElse("sorry");
        assertEquals("(fooColumn = foo or barColumn = bar or bazColumn = baz)", result);
    }

    @Test
    public void testCombineOrWithAbsentOptionalAndParens() throws Exception {
        WhereBuilder builder = whereBuilder().equalTo(Optional.empty(), "fooColumn");
        Optional<String> optionalResult = builder.combineViaOr(HqlClauses.Parens.USE);
        assertTrue(! optionalResult.isPresent());
    }

    @Test
    public void testBuild() throws Exception {
        WhereBuilder builder = whereBuilder();
        builder.equalTo(Optional.of("foo"), "fooColumn")
                .equalTo(Optional.empty(), "argamarg")
                .equalTo(Optional.of("bar"), "barColumn")
                .equalTo(Optional.of("baz"), "bazColumn");
        assertEquals("where fooColumn = foo and barColumn = bar and bazColumn = baz ", builder.build());
    }

    @Test
    public void testBuildWithMindBreakingClause() throws Exception {
        WhereBuilder clauses = whereBuilder()
                .equalTo(Optional.of("bar"), "barColumn")
                .between("startTime", "now", "then")
                .beforeAndAfter("startTime", "now", "endTime", "then")
                ;

        WhereBuilder clausesWithAndOr = whereBuilder()
                .equalTo(Optional.of("foo"), "fooColumn")
                .or(clauses)
                .equalTo(Optional.empty(), "argamarg")
                .and(clauses);

        WhereBuilder builder = whereBuilder()
                .and(clausesWithAndOr)
                .or(clausesWithAndOr)
                .and(clauses)
                .or(clauses)
                .equalTo(Optional.empty(), "argamarg");

        String orClausesStringParens = "(barColumn = bar or (startTime BETWEEN now AND then) or (now >= startTime AND then <= endTime))";

        String andClausesString = "barColumn = bar and (startTime BETWEEN now AND then) and (now >= startTime AND then <= endTime)";
        String andClausesStringParens = "(" + andClausesString + ")";

        String andClausesWithAndOrString = "fooColumn = foo and " + orClausesStringParens + " and " + andClausesStringParens;
        String andClausesWithAndOrStringParens = "(" + andClausesWithAndOrString + ")";

        String orClausesWithAndOrStringParens = "(fooColumn = foo or " + orClausesStringParens + " or " + andClausesStringParens + ")";

        String builderString = andClausesWithAndOrStringParens + " and " + orClausesWithAndOrStringParens + " and "
                + andClausesStringParens + " and " + orClausesStringParens;

        assertEquals("where " + andClausesString + " ",          clauses.build());
        assertEquals("where " + andClausesWithAndOrString + " ", clausesWithAndOr.build());
        assertEquals("where " + builderString + " ",             builder.build());
    }
}
