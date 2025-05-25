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
package edu.harvard.catalyst.scheduler.entity;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "template_resource_annotations")
public class TemplateResourceAnnotations extends BaseEntity implements Serializable {

    private static final long serialVersionUID = 7967883768282015803L;
    private TemplateResource templateResource;
    private LineLevelAnnotations lineLevelAnnotations;
    private Integer quantity;
    private String comment;

    public TemplateResourceAnnotations() {
        super(null);
    }

    @JoinColumn(name = "template_resource", referencedColumnName = "id")
    @ManyToOne(optional = true)
    public TemplateResource getTemplateResource() {
        return templateResource;
    }

    public void setTemplateResource(final TemplateResource templateResource) {
        this.templateResource = templateResource;
    }
    
    @JoinColumn(name = "line_level_annotations", referencedColumnName = "id")
    @ManyToOne(optional = true)
    public LineLevelAnnotations getLineLevelAnnotations() {
        return lineLevelAnnotations;
    }

    public void setLineLevelAnnotations(LineLevelAnnotations lineLevelAnnotations) {
        this.lineLevelAnnotations = lineLevelAnnotations;
    }
    
    @Column(name = "quantity")
    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(final Integer quantity) {
        this.quantity = quantity;
    }
    
    @Column(name = "comment")
    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    @Transient
    public String getQuantifiedLlaName() {
        return lineLevelAnnotations.getName() + " (" + quantity + ")";
    }

    @Override
    public String toString() {
        return "TemplateResourceAnnotations [id=" + id + ", getId()=" + getId() + "]";
    }

    // todo: we should probably add a unique constraint for the two FK combo
    @Override
    public final boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final TemplateResourceAnnotations other = (TemplateResourceAnnotations) obj;
        if (id == null && other.id == null) {
            return true;
        }
        if (id != null && other.id != null) {
            return id.equals(other.id)
                    && lineLevelAnnotations.equals(other.lineLevelAnnotations)
                    && templateResource.equals(other.templateResource)
                    && quantity.equals(other.quantity)
                    && comment.equals(other.comment);
        }
        // OK, one id is null, meaning it's just been created and hasn't yet been persisted
        if (   lineLevelAnnotations.equals(other.lineLevelAnnotations)
            && templateResource.equals(other.templateResource)
            && quantity.equals(other.quantity)
            && comment.equals(other.comment)
            ) {

            return true;
        }
        return false;
    }
    @Override
    public final int hashCode() {
        int hash = 0;
        hash += lineLevelAnnotations != null ? lineLevelAnnotations.hashCode() : 0;
        hash += templateResource != null ? templateResource.hashCode() : 0;
        hash += quantity != null ? quantity.hashCode() : 0;
        hash += comment != null ? comment.hashCode() : 0;
        return hash;
    }
}
