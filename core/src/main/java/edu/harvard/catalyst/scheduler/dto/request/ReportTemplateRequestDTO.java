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
package edu.harvard.catalyst.scheduler.dto.request;

import edu.harvard.catalyst.scheduler.entity.reporttemplate.Graph;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: carl
 * Date: 11/6/14
 * Time: 12:18 PM
 */
public class ReportTemplateRequestDTO {
    private Map<Integer, String> tcfIdToStringSortList;
    private Map<Integer, String> tcfIdToStringFilterList;
    private List<Integer> selectedTemplateCategoryFieldIds;
    private List<Integer> filterSelectedIds;
    private List<Integer> sortSelectedIds;
    private Date startDate;
    private Date endDate;
    private Graph.QueryScalarsTcfs cachedQsTcfs;

    public List<Integer> getSelectedTemplateCategoryFieldIds() {
        return selectedTemplateCategoryFieldIds;
    }

    public void setSelectedTemplateCategoryFieldIds(List<Integer> selectedTemplateCategoryFieldIds) {
        this.selectedTemplateCategoryFieldIds = selectedTemplateCategoryFieldIds;
    }

    public Map<Integer, String> getTcfIdToStringSortList() {
        return tcfIdToStringSortList;
    }

    public void setTcfIdToStringSortList(Map<Integer, String> tcfIdToStringSortList) {
        this.tcfIdToStringSortList = tcfIdToStringSortList;
    }

    public Map<Integer, String> getTcfIdToStringFilterList() {
        return tcfIdToStringFilterList;
    }

    public void setTcfIdToStringFilterList(Map<Integer, String> tcfIdToStringFilterList) {
        this.tcfIdToStringFilterList = tcfIdToStringFilterList;
    }

    public List<Integer> getFilterSelectedIds() {
        return filterSelectedIds;
    }

    public void setFilterSelectedIds(List<Integer> filterSelectedIds) {
        this.filterSelectedIds = filterSelectedIds;
    }

    public List<Integer> getSortSelectedIds() {
        return sortSelectedIds;
    }

    public void setSortSelectedIds(List<Integer> sortSelectedIds) {
        this.sortSelectedIds = sortSelectedIds;
    }

    public Date getStartDate() {
        return startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public Graph.QueryScalarsTcfs getCachedQuery() {
        return cachedQsTcfs;
    }

    public void setCachedQuery(Graph.QueryScalarsTcfs cachedQsTcfs) {
        this.cachedQsTcfs = cachedQsTcfs;
    }
}
