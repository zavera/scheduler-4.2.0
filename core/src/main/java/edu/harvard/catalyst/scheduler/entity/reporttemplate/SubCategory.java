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

import edu.harvard.catalyst.scheduler.entity.BaseEntity;

import javax.persistence.*;
import java.io.Serializable;

/**
 * Created by ak303 on 11/13/2014.
 */
@Entity
@Table(name = "sub_category")
public class SubCategory extends BaseEntity implements Serializable {
    private static final long serialVersionUID = 1L;
    private ReportTemplate reportTemplate;
    private Category parent;
    private Category child;
    private boolean expanded;

    public SubCategory() {
    }

    public SubCategory(Integer id, ReportTemplate reportTemplate, Category parent, Category child, boolean expanded) {
        super(id);
        this.reportTemplate = reportTemplate;
        this.parent = parent;
        this.child = child;
        this.expanded = expanded;
    }

    @JoinColumn(name = "report_template_id", referencedColumnName = "id")
    @ManyToOne(optional = false)
    public ReportTemplate getReportTemplate() {
        return reportTemplate;
    }

    public void setReportTemplate(ReportTemplate reportTemplate) {
        this.reportTemplate = reportTemplate;
    }

    @JoinColumn(name = "parent_id", referencedColumnName = "id")
    @ManyToOne(optional = false)
    public Category getParent() {
        return parent;
    }

    public void setParent(Category parent) {
        this.parent = parent;
    }

    @JoinColumn(name = "child_id", referencedColumnName = "id")
    @ManyToOne(optional = false)
    public Category getChild() {
        return child;
    }

    public void setChild(Category child) {
        this.child = child;
    }

    @Column(name = "expanded")
    public boolean getExpanded() {
        return expanded;
    }

    public void setExpanded(boolean expanded) {
        this.expanded = expanded;
    }

    @Override
    public String toString() {
        return "SubCategory [id=" + id + ", getId()=" + getId() + "]";
    }
}
