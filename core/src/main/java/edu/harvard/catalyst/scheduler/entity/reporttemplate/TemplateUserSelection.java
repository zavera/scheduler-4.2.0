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
@Table(name = "template_user_selection")
public class TemplateUserSelection extends BaseEntity implements Serializable {
    private static final long serialVersionUID = 1L;
    private TemplateUser templateUser;
    private TemplateCategoryField tcf;

    TemplateUserSortSelection userSortSelection;
    TemplateUserFilterSelection userFilterSelection;

    public TemplateUserSelection() {
        super(null);
    }

    public TemplateUserSelection(Integer id, TemplateUser templateUser, TemplateCategoryField tcf) {
        super(id);
        this.templateUser = templateUser;
        this.tcf = tcf;
    }

    public TemplateUserSelection(TemplateUser templateUser, TemplateCategoryField tcf) {
        this(null, templateUser, tcf);
    }

    @JoinColumn(name = "template_user_id", referencedColumnName = "id")
    @ManyToOne(optional = false)
    public TemplateUser getTemplateUser() {
        return templateUser;
    }

    public void setTemplateUser(TemplateUser templateUser) {
        this.templateUser = templateUser;
    }

    @JoinColumn(name = "tcf_id", referencedColumnName = "id")
    @ManyToOne(optional = false)
    public TemplateCategoryField getTcf() {
        return tcf;
    }

    public void setTcf(TemplateCategoryField tcf) {
        this.tcf = tcf;
    }

    @OneToOne(fetch = FetchType.EAGER, mappedBy = "templateUserSelection")
    public TemplateUserSortSelection getUserSortSelection() {
        return userSortSelection;
    }

    public void setUserSortSelection(TemplateUserSortSelection userSortSelection) {
        this.userSortSelection = userSortSelection;
    }

    @OneToOne(fetch = FetchType.EAGER, mappedBy = "templateUserSelection")
    public TemplateUserFilterSelection getUserFilterSelection() {
        return userFilterSelection;
    }

    public void setUserFilterSelection(TemplateUserFilterSelection userFilterSelection) {
        this.userFilterSelection = userFilterSelection;
    }

    @Override
    public String toString() {
        return "TemplateUser [id=" + id + ", getId()=" + getId() + "]";
    }
}
