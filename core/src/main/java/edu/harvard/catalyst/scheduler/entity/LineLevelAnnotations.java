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
import java.util.Comparator;

@Entity
@Table(name = "line_level_annotations")
public class LineLevelAnnotations extends BaseEntity implements Serializable {

    private static final long serialVersionUID = -2866625557191737132L;
    private String name;
    private String comment;
    private int quantity;
    private int resourceAnnotations;
    private boolean quantifiable;
    private boolean selected;

    public LineLevelAnnotations() {
        super(null);
    }
    @Column(name = "name")
    @Basic(optional = false)
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    
    @JoinColumn(name = "quantifiable")
    @Basic(optional = true)
    public boolean isQuantifiable() {
        return quantifiable;
    }

    public void setQuantifiable(boolean quantifiable) {
        this.quantifiable = quantifiable;
    }
    
    @Transient    
    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    @Transient
    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
      
    @Transient
    public int getResourceAnnotations() {
        return resourceAnnotations;
    }

    public void setResourceAnnotations(int resourceAnnotations) {
        this.resourceAnnotations = resourceAnnotations;
    }

    @Transient
    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public static final class AnnotationsComparator implements Comparator<LineLevelAnnotations> {
        public int compare(final LineLevelAnnotations o1, final LineLevelAnnotations o2) {
            return o1.getName().compareTo(o2.getName());
        }
    }

    @Override
    public String toString() {
        return "LineLevelAnnotations [id=" + id + ", getId()=" + getId() + "]";
    }
}
