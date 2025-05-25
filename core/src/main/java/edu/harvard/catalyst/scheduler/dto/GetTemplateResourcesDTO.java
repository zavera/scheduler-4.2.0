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
package edu.harvard.catalyst.scheduler.dto;

import java.util.List;

import edu.harvard.catalyst.scheduler.entity.Comments;
import edu.harvard.catalyst.scheduler.entity.TemplateResource;


public class GetTemplateResourcesDTO extends BooleanResultDTO {

    private List<TemplateResource> templateResources;
    private List<TemplateResource> groupedTemplateResources;
    private List<TemplateResource> flexTemplateResources;
    private List<TemplateResource> flexGroupResources;
    private List<TemplateResource> floatTemplateResources;
    private TemplateResource floatTemplateResourceField;
    private int visit;
    private int templateResource;
    private String startDate;
    private long startdate;
    private String endDate;
    private Integer totalPages;
    private List<String> days;
    private String totalVisitResources;
    private List<String> name;
    private List<Comments> visitComments;
    
    public GetTemplateResourcesDTO() {
    }

    public String getTotalVisitResources() {
		return totalVisitResources;
	}

	public void setTotalVisitResources(String totalVisitResources) {
		this.totalVisitResources = totalVisitResources;
	}
	public List<TemplateResource> getTemplateResources() {
        return templateResources;
    }

    public void setTemplateResources(List<TemplateResource> templateResources) {
        this.templateResources = templateResources;
    }

    public List<TemplateResource> getFlexTemplateResources() {
        return flexTemplateResources;
    }

    public void setFlexTemplateResources(
            List<TemplateResource> flexTemplateResources) {
        this.flexTemplateResources = flexTemplateResources;
    }

    public List<TemplateResource> getFlexGroupResources() {
		return flexGroupResources;
	}

	public void setFlexGroupResources(List<TemplateResource> flexGroupResources) {
		this.flexGroupResources = flexGroupResources;
	}

	public List<TemplateResource> getFloatTemplateResources() {
        return floatTemplateResources;
    }


    public void setFloatTemplateResources(
            List<TemplateResource> floatTemplateResources) {
        this.floatTemplateResources = floatTemplateResources;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public int getVisit() {
        return visit;
    }

    public void setVisit(int visit) {
        this.visit = visit;
    }

    public int getTemplateResource() {
        return templateResource;
    }

    public void setTemplateResource(int templateResource) {
        this.templateResource = templateResource;
    }

    public TemplateResource getFloatTemplateResourceField() {
        return floatTemplateResourceField;
    }

    public void setFloatTemplateResourceField(
            TemplateResource floatTemplateResourceField) {
        this.floatTemplateResourceField = floatTemplateResourceField;
    }

    public List<TemplateResource> getGroupedTemplateResources() {
        return groupedTemplateResources;
    }

    public void setGroupedTemplateResources(
            List<TemplateResource> groupedTemplateResources) {
        this.groupedTemplateResources = groupedTemplateResources;
    }

    public Integer getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(Integer totalPages) {
        this.totalPages = totalPages;
    }

    public List<String> getDays() {
        return days;
    }

    public void setDays(List<String> days) {
        this.days = days;
    }

    public long getStartdate() {
        return startdate;
    }

    public void setStartdate(long startdate) {
        this.startdate = startdate;
    }

    public List<String> getName() {
        return name;
    }

    public void setName(List<String> name) {
        this.name = name;
    }

    public List<Comments> getVisitComments() {
        return visitComments;
    }

    public void setVisitComments(List<Comments> visitComments) {
        this.visitComments = visitComments;
    }
}
