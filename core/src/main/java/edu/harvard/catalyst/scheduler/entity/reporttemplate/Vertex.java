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

import java.util.Comparator;

/**
 * Created with IntelliJ IDEA.
 * User: carl
 * Date: 10/21/14
 * Time: 1:48 PM
 */

public final class Vertex {

	private static int counter = 0;

	private final String tableName;
	private final String tableShortName;
	private int priority;

	public Vertex(final String tableName, final String tableShortName) {
		// 'bootstrap' priority-number will be revised later by graph during its initialization
		priority = counter++;
		this.tableName = tableName;
		this.tableShortName = tableShortName;
	}

	public String getTableShortName() {
		return tableShortName;
	}

	public int getPriority() {
		return priority;
	}

	public void setPriority(final Integer priority) {
		this.priority = priority;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (tableShortName == null ? 0 : tableShortName.hashCode());
		return result;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final Vertex other = (Vertex) obj;
		if (tableShortName == null && other.tableShortName != null) {
			return false;
		}
		return tableShortName.equals(other.tableShortName);
	}

	@Override
	public String toString() {
		return priority + ":" + fullName();
	}

	public String fullName() {
		return tableName + " " + tableShortName;
	}

	static class VertexComparator implements Comparator<Vertex> {
		@Override
		public int compare(final Vertex thisOne, final Vertex otherOne) {
			final Integer thisPriority = thisOne.getPriority();
			final Integer otherPriority = otherOne.getPriority();
			return thisPriority.compareTo(otherPriority);
		}
	}
}
