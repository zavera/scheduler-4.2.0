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

import edu.harvard.catalyst.scheduler.dto.BooleanResultDTO;

/**
 * Created by brian on 10/12/16.
 *
 */
public final class CreateResourceResponse extends BooleanResultDTO {
    private int resourceId;
    private String name;
    private String resourceType;
    private int sublocationId;
    private String sublocationName;

    public int getResourceId() {
        return this.resourceId;
    }

    public void setResourceId(final int resourceId) {
        this.resourceId = resourceId;
    }

    public String getName() {
        return this.name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getResourceType() {
        return this.resourceType;
    }

    public void setResourceType(final String resourceType) {
        this.resourceType = resourceType;
    }

    public int getSublocationId() {
        return this.sublocationId;
    }

    public void setSublocationId(final int sublocationId) {
        this.sublocationId = sublocationId;
    }

    public String getSublocationName() {
        return this.sublocationName;
    }

    public void setSublocationName(final String sublocationName) {
        this.sublocationName = sublocationName;
    }
}
