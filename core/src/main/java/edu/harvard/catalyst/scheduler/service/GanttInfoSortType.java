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
package edu.harvard.catalyst.scheduler.service;

/**
 * Created with IntelliJ IDEA.
 * User: carl
 * Date: 11/19/15
 * Time: 10:25 AM
 */
public enum GanttInfoSortType {
    NameAsc("tr.resource.name asc"),
    NameDesc("tr.resource.name desc"),
    TypeAsc("tr.resource.resourceType asc"),
    TypeDesc("tr.resource.resourceType desc"),

    StartAsc("tr.startMinutes asc"),
    StartDesc("tr.startMinutes desc"),
    EndAsc("tr.endMinutes asc"),
    EndDesc("tr.endMinutes desc"),
    BillableAsc("tr.billable asc"),
    BillableDesc("tr.billable desc");


    final private String primarySort;

    GanttInfoSortType(String primarySort) {
        this.primarySort = primarySort;
    }

    public String getSortClause() {
        String orderBy = ", tr.startMinutes asc, tr.endMinutes desc";
        if (this.primarySort.equals(StartAsc.primarySort)) {
            orderBy = ", tr.endMinutes desc";
        }
        return " order by "
                + primarySort
                + orderBy;
    }

    public String getSingleSortClause() {
        return " order by "
                + primarySort;
    }
}
