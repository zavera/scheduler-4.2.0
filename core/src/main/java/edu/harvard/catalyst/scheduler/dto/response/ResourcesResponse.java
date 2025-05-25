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
package edu.harvard.catalyst.scheduler.dto.response;


import edu.harvard.catalyst.scheduler.entity.ResourceSublocation;

/**
 * User: ankit
 * Date: 3/21/14
 * Time: 10:43 AM
 */
public class ResourcesResponse {

    private int id;
    private Long totalCount;
    private int resourceId;
    private String resource;
    private String resourceType;
    private int sublocationId;
    private String sublocation;
    private Boolean active;
    private Integer sharedResourceId;
    private String sharedResourceNotes;


    public ResourcesResponse(int id, int resourceId, String resourceName, String resourceTypeName,
                             int sublocationId, String sublocationName, Boolean active, Long total) {
        this.id = id;
        this.resourceId = resourceId;
        this.resource = resourceName;
        this.resourceType = resourceTypeName;
        this.sublocation = sublocationName;
        this.sublocationId = sublocationId;
        this.active = active;
        this.totalCount = total;
        this.sharedResourceId = null;
        this.sharedResourceNotes = null;
    }

    public ResourcesResponse(int id, int resourceId, String resourceName, String resourceTypeName,
                             int sublocationId, String sublocationName, Boolean active, Integer sharedResourceId,
                             String sharedResourceNotes, Long total
                             ) {
        this.id = id;
        this.resourceId = resourceId;
        this.resource = resourceName;
        this.resourceType = resourceTypeName;
        this.sublocation = sublocationName;
        this.sublocationId = sublocationId;
        this.active = active;
        this.totalCount = total;
        this.resourceId = resourceId;
        this.sharedResourceId = sharedResourceId;
        this.sharedResourceNotes = sharedResourceNotes;
    }

    public ResourcesResponse(final ResourceSublocation resourceSublocation) {
        this.id = resourceSublocation.getId();
        this.resourceId = resourceSublocation.getResource().getId();
        this.resource = resourceSublocation.getResource().getName();
        this.resourceType = resourceSublocation.getResource().getResourceType().getName();
        this.sublocation = resourceSublocation.getSublocation().getName();
        this.sublocationId = resourceSublocation.getSublocation().getId();
        this.active = resourceSublocation.isActive();
    }

    public Long getTotalCount() {
        return totalCount;
    }

}
