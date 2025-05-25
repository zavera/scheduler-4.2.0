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
@Table(name = "resource_sublocation")
public class ResourceSublocation extends BaseEntity implements Serializable {

    private static final long serialVersionUID = 4414425139555614696L;
    private Sublocation sublocation;
    private Resource resource;
    private Boolean active;

    public ResourceSublocation() {
        super(null);
    }

    public ResourceSublocation(Sublocation sublocation, Resource resource, Boolean active) {
        super(null);

        this.sublocation = sublocation;
        this.resource = resource;
        this.active = active;
    }

    @JoinColumn(name = "resource", referencedColumnName = "id")
    @ManyToOne(optional = false)
    public Resource getResource() {
        return resource;
    }

    public void setResource(Resource resource) {
        this.resource = resource;
    }

    @JoinColumn(name = "sublocation", referencedColumnName = "id")
    @ManyToOne(optional = false)
    public Sublocation getSublocation() {
        return sublocation;
    }

    public void setSublocation(Sublocation sublocation) {
        this.sublocation = sublocation;
    }

    @Column(name = "active")
    @Basic(optional = false)
    public Boolean isActive() {
        return active;
    }

    public void setActive(final Boolean active) {
        this.active = active;
    }

    @Override
    public String toString() {
        return "ResourceSublocation [id=" + id + ", getId()=" + getId() + "]";
    }

}
