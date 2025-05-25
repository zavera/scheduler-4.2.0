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
 * Created by carl
 */
@Entity
@Table(name = "template_user_filter_selection")
public class TemplateUserFilterSelection extends BaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;
    private TemplateUserSelection templateUserSelection;
    private String expression;

    public TemplateUserFilterSelection() {
        super(null);
    }

    public TemplateUserFilterSelection(final Integer id,
                                       final TemplateUserSelection templateUserSelection,
                                       final String expression) {
        super(id);
        this.templateUserSelection = templateUserSelection;
        this.expression = expression;
    }

    public TemplateUserFilterSelection(TemplateUserSelection tus, String order) {
        this(null, tus, order);
    }

    @Column(name = "expression")
    @Basic(optional = false)
    public String getExpression() {
        return expression;
    }

    public void setExpression(String expression) {
        this.expression = expression;
    }


    @JoinColumn(name = "template_user_selection_id", referencedColumnName = "id")
    @OneToOne(optional = false)
    public TemplateUserSelection getTemplateUserSelection() {
        return templateUserSelection;
    }

    public void setTemplateUserSelection(TemplateUserSelection templateUserSelection) {
        this.templateUserSelection = templateUserSelection;
    }

    @Override
    public String toString() {
        return "TemplateUserFilterSelection [id=" + id + ", getId()=" + getId() + "]";
    }
}

