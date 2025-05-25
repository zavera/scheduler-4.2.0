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

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import edu.harvard.catalyst.scheduler.entity.TemplateResource;
import edu.harvard.catalyst.scheduler.entity.TemplateResourceAnnotations;
import edu.harvard.catalyst.scheduler.persistence.AppointmentDAO;
import edu.harvard.catalyst.scheduler.persistence.TemplateResourceDAO;

import java.util.Date;
import java.util.List;
import java.util.Set;

import static edu.harvard.catalyst.scheduler.util.DateUtility.minutesSinceOrigin;


public class TemplateResourceDTO extends BooleanResultDTO {

    private Integer id;
    private Integer visitTemplate;
    private Integer resource;
    private Boolean billable;
    private Boolean alternate;
    private Date createdDate;
    private Integer duration;
    private Integer floatStart;
    private Integer floatEnd;
    private String floatable;
    private String flexible;
    private Integer startMinutes;
    private Integer endMinutes;
    private List<Integer> selectedAnnotations;
    private List<String> selectedAnnotationsComment;
    private List<Integer> selectedAnnotationsQuantity;

    public static final String NO_CHANGE = "No Change";

    public TemplateResourceDTO() {}

        // for audit comparisons
    public TemplateResourceDTO(final TemplateResource tr, TemplateResourceDAO templateResourceDAO) {
        id = tr.getId();
        visitTemplate = tr.getVisitTemplate().getId();
        resource = tr.getResource().getId();
        billable = tr.getBillable();
        alternate = tr.getAlternate();
        floatStart = tr.getFloatStart();
        floatEnd = tr.getFloatEnd();
        floatable = tr.getFloatable() ? "true" : "false";
        flexible = tr.getFlexible() ? "true" : "false";
        startMinutes = tr.getStartMinutes();
        endMinutes = tr.getEndMinutes();

        selectedAnnotations = Lists.newArrayList();
        selectedAnnotationsComment = Lists.newArrayList();
        selectedAnnotationsQuantity = Lists.newArrayList();

        List<TemplateResourceAnnotations> traList = templateResourceDAO.findTemplateResourceAnnotationsByTemplateResource(tr);
        traList.forEach(tra -> {
            selectedAnnotations.add(tra.getLineLevelAnnotations().getId());
            selectedAnnotationsComment.add(tra.getComment());
            selectedAnnotationsQuantity.add(tra.getQuantity());
        });
    }

    void appendIfDifferent(StringBuilder builder, String label, Object from, Object to) {
        if (from == null && to != null) {
            builder.append("{").append(label).append(": ").append("null").append(" --> ").append(to).append("}");
        }
        else if ( from != null && ! from.equals(to)) {
            builder.append("{").append(label).append(": ").append(from).append(" --> ").append(to).append("}");
        }
    }

    public String diffFromThisToOther(TemplateResourceDTO other) {
        StringBuilder diffBuilder = new StringBuilder();

        appendIfDifferent(diffBuilder, "id", id, other.id);
        appendIfDifferent(diffBuilder, "visitTemplate", visitTemplate, other.visitTemplate);
        appendIfDifferent(diffBuilder, "resource", resource, other.resource);
        appendIfDifferent(diffBuilder, "billable", billable, other.billable);
        appendIfDifferent(diffBuilder, "alternate", alternate, other.alternate);
        appendIfDifferent(diffBuilder, "floatStart", floatStart, other.floatStart);
        appendIfDifferent(diffBuilder, "floatEnd", floatEnd, other.floatEnd);
        appendIfDifferent(diffBuilder, "floatable", floatable, other.floatable);
        appendIfDifferent(diffBuilder, "flexible", flexible, other.flexible);
        appendIfDifferent(diffBuilder, "startMinutes", startMinutes, other.startMinutes);
        appendIfDifferent(diffBuilder, "endMinutes", endMinutes, other.endMinutes);
        appendIfDifferent(diffBuilder, "annotations", canonicalInfoLla(), other.canonicalInfoLla());

        String builderResult = diffBuilder.toString();

        return builderResult.isEmpty() ? NO_CHANGE : builderResult;
    }
    String canonicalInfoLla() {
        Set<String> canonicalStringSet = Sets.newTreeSet();

        for (int i=0; i<selectedAnnotations.size(); i++) {
            String info = new StringBuilder()
                    .append("{")
                    .append("id: ")
                    .append(selectedAnnotations.get(i))
                    .append(". comment: ")
                    .append(selectedAnnotationsComment.get(i))
                    .append(". quantity: ")
                    .append(selectedAnnotationsQuantity.get(i))
                    .append("}")
                    .toString();

            canonicalStringSet.add(info);
        }
        return Joiner.on(",").join(canonicalStringSet);
    }

    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }
    public int getVisitTemplate() {
        return visitTemplate;
    }
    public void setVisitTemplate(int visitTemplate) {
        this.visitTemplate = visitTemplate;
    }
    public int getResource() {
        return resource;
    }
    public void setResource(int resource) {
        this.resource = resource;
    }
    public boolean isBillable() {
        return billable;
    }
    public void setBillable(boolean billable) {
        this.billable = billable;
    }

    public boolean isAlternate() {
        return alternate;
    }
    public void setAlternate(boolean alternate) {
        this.alternate = alternate;
    }

    public Date getCreatedDate() {
        return createdDate;
    }
    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }
    public Integer getDuration() {
        return duration;
    }
    public void setDuration(Integer duration) {
        this.duration = duration;
    }
    public String getFloatable() {
        return floatable;
    }
    public void setFloatable(String floatable) {
        this.floatable = floatable;
    }

    public String getFlexible() {
        return flexible;
    }
    public void setFlexible(String flexible) {
        this.flexible = flexible;
    }

    public List<Integer> getSelectedAnnotations() {
        return selectedAnnotations == null ? Lists.newArrayList() : selectedAnnotations;
    }
    public List<Integer> getSelectedAnnotationsQuantity() {
        return selectedAnnotationsQuantity == null ? Lists.newArrayList() : selectedAnnotationsQuantity;
    }
    public List<String> getSelectedAnnotationsComment() {
        return selectedAnnotationsComment == null ? Lists.newArrayList() : selectedAnnotationsComment;
    }
    public boolean isFlexible() {
        return getFlexible() != null && getFlexible().equalsIgnoreCase("true");
    }

    public boolean isFloatable() {
        return getFloatable() != null && getFloatable().equalsIgnoreCase("true");
    }

    public Integer getFloatStart() {
        return floatStart;
    }

    public Integer getFloatEnd() {
        return floatEnd;
    }
    public void setFloatStart(Integer floatStart) {
        this.floatStart = floatStart;
    }

    public void setFloatEnd(Integer floatEnd) {
        this.floatEnd = floatEnd;
    }

    public void setStartMinutes(Integer startMinutes) {
        this.startMinutes = startMinutes;
    }

    public Integer getStartMinutes() {
        return startMinutes;
    }
    public Integer getEndMinutes() {
        return endMinutes;
    }

    public void setEndMinutes(Integer endMinutes) {
        this.endMinutes = endMinutes;
    }

    public void setSelectedAnnotations(List<Integer> selectedAnnotations) {
        this.selectedAnnotations = selectedAnnotations;
    }

    public void setSelectedAnnotationsComment(List<String> selectedAnnotationsComment) {
        this.selectedAnnotationsComment = selectedAnnotationsComment;
    }

    public void setSelectedAnnotationsQuantity(List<Integer> selectedAnnotationsQuantity) {
        this.selectedAnnotationsQuantity = selectedAnnotationsQuantity;
    }

    public boolean matches(TemplateResource tr) {
        boolean changed = false;

        changed = changed || this.isAlternate() != tr.getAlternate();
        changed = changed || this.isBillable() != tr.getBillable();
        changed = changed || this.isFlexible() != tr.getFlexible();

        changed = changed || this.isFloatable() != tr.getFloatable();

        changed = changed || tr.getStartMinutes()  != null && this.getStartMinutes().intValue()  != tr.getStartMinutes().intValue();
        changed = changed || tr.getEndMinutes()    != null && this.getEndMinutes().intValue()    != tr.getEndMinutes().intValue();

        changed = changed || tr.getFloatStart() != null && this.getFloatStart().intValue() != tr.getFloatStart().intValue();
        changed = changed || tr.getFloatEnd()   != null && this.getFloatEnd().intValue()   != tr.getFloatEnd().intValue();

        return ! changed;
    }
}
