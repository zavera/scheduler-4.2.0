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

import edu.harvard.catalyst.scheduler.core.SchedulerRuntimeException;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "override_booked_resource_annotations")
public class OverrideBookedResourceAnnotations extends BaseEntity implements Serializable, Cloneable {

    private static final long serialVersionUID = -6342183945144289012L;
    private BookedResource bookedResource;
    private LineLevelAnnotations lineLevelAnnotations;
    private Integer quantity;
    private String comment;

    public OverrideBookedResourceAnnotations() {
        super(null);
    }

    @JoinColumn(name = "booked_resource", referencedColumnName = "id")
    @ManyToOne(optional = true)
    public BookedResource getBookedResource() {
        return bookedResource;
    }

    public void setBookedResource(BookedResource bookedResource) {
        this.bookedResource = bookedResource;
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

    @Override
    public String toString() {
        return "OverrideBookedResourceAnnotations [id=" + id + ", getId()=" + getId() + "]";
    }

    public OverrideBookedResourceAnnotations cloneObra() {
        OverrideBookedResourceAnnotations result = null;

        try {
            result = (OverrideBookedResourceAnnotations) super.clone();
        }

        catch (CloneNotSupportedException cnse) {
            SchedulerRuntimeException.logAndThrow("Couldn't clone OverrideBookedResourceAnnotations '" + getId() + "'", cnse);
        }

        return result;
    }

}
