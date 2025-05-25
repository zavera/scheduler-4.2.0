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

import java.util.Date;

/**
 * User: ankit
 * Date: 3/5/14
 * Time: 8:02 AM
 */
public class VisitTemplatesResponse {

    // public for testability
    public int id;
    public int studyId;
    public Long totalCount;
    public String visitName;
    public String visitTypeName;
    public String sublocationName;
    public String localId;
    public Date lastUpdateTime;
    public String piName;
    public String finalApprovalStatus;

    // provide Object overrides so that these babies work with Collections, eg contains/subtraction
    @Override
    public boolean equals(Object other) {
       if (other == null) {
           return false;
       }

       if ( ! (other instanceof VisitTemplatesResponse)) {
           return false;
       }

       return id == ((VisitTemplatesResponse)other).id;
    }
    @Override
    public int hashCode() {
        return id;
    }

    public VisitTemplatesResponse(int id, int studyId, String visitName, String visitTypeName, String sublocationName, String finalApprovalStatus, Long total, String localId, Date lastUpdateTime, String piName) {
        this.id = id;
        this.studyId = studyId;
        this.visitName = visitName;
        this.visitTypeName = visitTypeName;
        this.sublocationName = sublocationName;
        this.finalApprovalStatus = finalApprovalStatus;
        this.totalCount = total;   
        this.localId = localId;
        this.lastUpdateTime = lastUpdateTime;
        this.piName = piName;                
    }

    public void setTotalCount(long value) {
        totalCount = value;
    }
    public long getTotalCount() { return totalCount; }
    public int getId() { return id; }
}
