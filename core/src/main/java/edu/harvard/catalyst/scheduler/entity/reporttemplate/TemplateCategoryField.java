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

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import edu.harvard.catalyst.scheduler.entity.BaseEntity;

/**
 * Created by ak303 on 11/13/2014.
 */
@Entity
@Table(name = "template_category_field")
public class TemplateCategoryField extends BaseEntity implements Serializable {
    private static final long serialVersionUID = 1L;
    private ReportTemplate reportTemplate;
    private Field field;
    private Category category;
    private String suffix;
    private boolean selected;
    private Integer csvOrder;
    private Integer csvOrder2;

    public TemplateCategoryField() {
        this(null, null, null, null, null, false);
    }

    public TemplateCategoryField(
            final Integer id,
            final ReportTemplate reportTemplate,
            final Category category,
            final Field field, 
            final String suffix,
            final boolean selected) {
        
        super(id);
        this.reportTemplate = reportTemplate;
        this.category = category;
        this.field = field;
        this.selected = selected;
        this.suffix = suffix;
    }

    @JoinColumn(name = "report_template_id", referencedColumnName = "id")
    @ManyToOne(optional = false)
    public ReportTemplate getReportTemplate() {
        return reportTemplate;
    }

    public void setReportTemplate(final ReportTemplate reportTemplate) {
        this.reportTemplate = reportTemplate;
    }

    @JoinColumn(name = "field_id", referencedColumnName = "id")
    @ManyToOne(optional = false)
    public Field getField() {
        return field;
    }

    public void setField(final Field field) {
        this.field = field;
    }

    @JoinColumn(name = "category_id", referencedColumnName = "id")
    @ManyToOne(optional = false)
    public Category getCategory() {
        return category;
    }

    public void setCategory(final Category category) {
        this.category = category;
    }

    @Column(name = "selected")
    public boolean isSelected() {
        return selected;
    }

    public void setSelected(final boolean selected) {
        this.selected = selected;
    }

    @Column(name = "suffix")
    public String getSuffix() {
        return suffix;
    }

    public void setSuffix(final String suffix) {
        this.suffix = suffix;
    }

    @Column(name = "csv_order")
    public Integer getCsvOrder() {
        return csvOrder;
    }
    public void setCsvOrder(final Integer csvOrder) {
        this.csvOrder = csvOrder;
    }
    @Column(name = "csv_order2")
    public Integer getCsvOrder2() {
        return csvOrder2;
    }
    public void setCsvOrder2(final Integer csvOrder2) {
        this.csvOrder2 = csvOrder2;
    }

    @Override
    public String toString() {
        return "TemplateCategoryField [id=" + id + ", getId()=" + getId() + "]";
    }
}
