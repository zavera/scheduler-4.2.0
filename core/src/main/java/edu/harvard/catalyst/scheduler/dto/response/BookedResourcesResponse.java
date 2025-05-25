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
public class BookedResourcesResponse {

    private int id;
    private Long totalCount;
    private Date scheduledStartTime;
    private Date scheduledEndTime;
    private String resourceName;
    private String annotations;
    private String rejectedResourceMessage;
    private String available;
    private String resourceGroup;
    private boolean subjectAvailable;

    public BookedResourcesResponse(int id, String resourceName, String annotationsList, Date scheduledStartTime, Date scheduledEndTime, String rejectedResourceMessage, String available, String resourceGroup, Long totalCount, boolean subjectAvailable) {
        this.id = id;
        this.resourceName = resourceName;
        this.annotations = annotationsList;
        this.scheduledStartTime = scheduledStartTime;
        this.scheduledEndTime = scheduledEndTime;
        this.rejectedResourceMessage = rejectedResourceMessage;
        this.available = available;
        this.resourceGroup = resourceGroup;
        this.subjectAvailable = subjectAvailable;
        this.totalCount = totalCount;    
    }  
}
