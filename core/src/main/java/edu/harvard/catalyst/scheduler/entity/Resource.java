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

import java.io.Serializable;
import java.util.List;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import javax.persistence.Transient;

@Entity
@Table(name = "resource")
public class Resource extends BaseEntity implements Serializable {
    private static final long serialVersionUID = -6852528504208574939L;
    private String name;
    private ResourceType resourceType;
    private Integer sharedResource;
    private String sharedResourceNotes;

    private List<Sublocation> sublocations;
    private List<ResourceAlternate> alternateResourceList;
    private List<ResourceAlternate> sourceResourceList;

    private List<ResourceSchedule> defaultScheduleList;
    private List<ResourceSchedule> overrideScheduleList;

    @Deprecated
    public Resource() {
        this(null, null, null, null, null, null, null, null, null);
    }

    public Resource(final Integer id, final String name, final ResourceType resourceType, final Integer sharedResource, final List<Sublocation> sublocations, final List<ResourceAlternate> alternateResourceList, final List<ResourceAlternate> sourceResourceList, final List<ResourceSchedule> defaultScheduleList, final List<ResourceSchedule> overrideScheduleList) {
        super(null);
        this.id = id;
        this.name = name;
        this.resourceType = resourceType;
        this.sharedResource = sharedResource;
        this.sharedResourceNotes = null;
        this.sublocations = sublocations;
        this.alternateResourceList = alternateResourceList;
        this.sourceResourceList = sourceResourceList;
        this.defaultScheduleList = defaultScheduleList;
        this.overrideScheduleList = overrideScheduleList;
    }

    @Column(name = "name")
    @Basic(optional = false)
    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    @Column(name = "shared_resource")
    @Basic(optional = true)
    public Integer getSharedResource() {
        return sharedResource;
    }

    public void setSharedResource(final Integer sharedResource) {
        this.sharedResource = sharedResource;
    }

    @Column(name = "shared_resource_notes")
    @Basic(optional = true)
    public String getSharedResourceNotes() {
        return sharedResourceNotes;
    }

    public void setSharedResourceNotes(final String sharedResourceNotes) {
        this.sharedResourceNotes = sharedResourceNotes;
    }

    @Column(name = "type")
    @Basic(optional = false)
    @Enumerated(EnumType.STRING)
    public ResourceType getResourceType() {
        return resourceType;
    }

    public void setResourceType(final ResourceType resourceType) {
        this.resourceType = resourceType;
    }

    // TODO-XH: this property maps to an association table "resource_sublocation". However there is also a ResourceSublocation entity
    // which maps to this table. Are the 2 compatible?
    // ResourceSublocation has an extra property 'active'
    @ManyToMany(fetch = FetchType.EAGER, targetEntity = Sublocation.class, cascade = { CascadeType.PERSIST, CascadeType.MERGE })
    @JoinTable(name = "resource_sublocation", joinColumns = @JoinColumn(name = "resource"), inverseJoinColumns = @JoinColumn(name = "sublocation"))
    public List<Sublocation> getSublocations() {
        return sublocations;
    }

    public void setSublocations(final List<Sublocation> sublocations) {
        this.sublocations = sublocations;
    }

    @Transient
    public List<ResourceAlternate> getAlternateResourceList() {
        return alternateResourceList;
    }

    public void setAlternateResourceList(final List<ResourceAlternate> alternateResourceList) {
        this.alternateResourceList = alternateResourceList;
    }

    @Transient
    public List<ResourceAlternate> getSourceResourceList() {
        return sourceResourceList;
    }

    public void setSourceResourceList(final List<ResourceAlternate> sourceResourceList) {
        this.sourceResourceList = sourceResourceList;
    }

    @Transient
    public List<ResourceSchedule> getDefaultScheduleList() {
        return defaultScheduleList;
    }

    public void setDefaultScheduleList(final List<ResourceSchedule> defaultScheduleList) {
        this.defaultScheduleList = defaultScheduleList;
    }

    @Transient
    public List<ResourceSchedule> getOverrideScheduleList() {
        return overrideScheduleList;
    }

    public void setOverrideScheduleList(final List<ResourceSchedule> overrideScheduleList) {
        this.overrideScheduleList = overrideScheduleList;
    }

    @Override
    public String toString() {
        return "Resource [id=" + id + ", getId()=" + getId() + "]";
    }

}
