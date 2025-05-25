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

import static edu.harvard.catalyst.hccrc.core.util.ListUtils.flatten;

import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;

/**
 * @author Bill Simons
 * @date 2/20/14
 * @link http://cbmi.med.harvard.edu
 * @link http://chip.org
 */
class HqlClauses {

	enum Parens {
		USE("(", ")"), DONT_USE("", "");

		Parens(final String open, final String close) {
			this.open = open;
			this.close = close;
		}

		final private String open;
		final private String close;

		Optional<String> enclose(final String payload) {
			return Optional.of(open + payload + close);
		}
	}

	static WhereBuilder whereBuilder() {
		return new WhereBuilder();
	}

	static class WhereBuilder {
		private final List<Optional<String>> list = Lists.newArrayList();

		private WhereBuilder() {
		}

		private WhereBuilder createClause(final Optional<String> o, final String prefix, final String suffix) {
			list.add(o.map(s -> prefix + s + suffix));

			return this;
		}

		WhereBuilder like(final Optional<String> o, final String column) {
			return createClause(o, column + " like '%", "%'");
		}

		WhereBuilder in(final Optional<String> o, final String... ins) {
			return createClause(o, "", " in (" + Joiner.on(", ").join(ins) + ")");
		}

		WhereBuilder equalTo(final Optional<String> o, final String column) {
			return createClause(o, column + " = ", "");
		}

		WhereBuilder isTrue(final String column) {
			return accumulate(column + " = TRUE");
		}

		WhereBuilder isFalse(final String column) {
			return accumulate(column + " = FALSE");
		}

		WhereBuilder between(final String candidate, final String first, final String second) {
			return accumulate("(" + candidate + " BETWEEN " + first + " AND " + second + ")");
		}

		WhereBuilder beforeAndAfter(final String earlyParam, final String earlyTime, final String lateParam, final String lateTime) {
			return accumulate("(" + earlyTime + " >= " + earlyParam + " AND " + lateTime + " <= " + lateParam + ")");
		}
		
		public WhereBuilder accumulate(final String clauseString) {
			final Optional<String> clause = Optional.of(clauseString);
			
			list.add(clause);
			
			return this;
		}

		WhereBuilder and(final WhereBuilder andBuilder) {
			return conjuction(andBuilder::combineViaAnd);
		}

		WhereBuilder or(final WhereBuilder orBuilder) {
			return conjuction(orBuilder::combineViaOr);
		}
		
		private WhereBuilder conjuction(final Function<Parens, Optional<String>> combine) {
			final Optional<String> conjunctionList = combine.apply(Parens.USE);
			
			if (conjunctionList.isPresent()) {
				list.add(conjunctionList);
			}
			
			return this;
		}

		Optional<String> combineViaAnd(final Parens parens) {
			final Iterator<String> presentWheres = flatten(list).iterator();
			
			if (presentWheres.hasNext()) {
				return parens.enclose(Joiner.on(" and ").join(presentWheres));
			}
			
			return Optional.empty();
		}

		Optional<String> combineViaOr(final Parens parens) {
			final Iterator<String> presentWheres = flatten(list).iterator();
			
			if (presentWheres.hasNext()) {
				return parens.enclose(Joiner.on(" or ").join(presentWheres));
			}
			
			return Optional.empty();
		}

		String build() {
			final Optional<String> andList = combineViaAnd(Parens.DONT_USE);
			
			return andList.map(al -> "where " + al + " ").orElse("");
		}
	}
}
