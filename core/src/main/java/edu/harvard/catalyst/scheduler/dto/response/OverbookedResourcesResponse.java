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

import com.google.common.collect.Maps;
import edu.harvard.catalyst.scheduler.entity.BookedResource;
import edu.harvard.catalyst.scheduler.entity.SubjectMrn;

import java.util.Comparator;
import java.util.Date;
import java.util.Map;

import static edu.harvard.catalyst.scheduler.core.Statics.NO_SUBJECT_ASSIGNED;
import static edu.harvard.catalyst.scheduler.util.MiscUtil.compareVia;
import static edu.harvard.catalyst.scheduler.util.MiscUtil.invert;

/**
 * User: ankit
 * Date: 3/5/14
 * Time: 8:02 AM
 */
public class OverbookedResourcesResponse {

    private int id;
    private int totalCount;

    private String localId;

    private Date visitStartTime;
    private Date visitEndTime;
    private Date scheduledStartTime;
    private Date scheduledEndTime;

    private String conflictedTime;

    private String subjectName;
    private String resourceName;
    private String visitName;

    public OverbookedResourcesResponse(BookedResource bookedResource, int totalCount) {
        this.id = bookedResource.getId();
        this.totalCount = totalCount;

        this.localId = bookedResource.getBookedVisit().getStudy().getLocalId();

        this.visitStartTime = bookedResource.getBookedVisit().getScheduledStartTime();
        this.visitEndTime = bookedResource.getBookedVisit().getScheduledEndTime();
        this.scheduledStartTime = bookedResource.getScheduledStartTime();
        this.scheduledEndTime = bookedResource.getScheduledEndTime();

        this.conflictedTime = bookedResource.getConflictedTime();

        SubjectMrn subjectMrn = bookedResource.getBookedVisit().getSubjectMrnDecrypted();

        this.subjectName = subjectMrn == null ? NO_SUBJECT_ASSIGNED :
                subjectMrn.getSubject().getFirstName() + " " + subjectMrn.getSubject().getLastName();
        this.resourceName = bookedResource.getResource().getName();
        this.visitName = bookedResource.getBookedVisit().getVisitTemplate().getName();
    }

    public String getLocalId() {
        return localId;
    }

    public Date getVisitStartTime() {
        return visitStartTime;
    }

    public Date getVisitEndTime() {
        return visitEndTime;
    }

    public Date getScheduledStartTime() {
        return scheduledStartTime;
    }

    public Date getScheduledEndTime() {
        return scheduledEndTime;
    }

    public String getConflictedTime() {
        return conflictedTime;
    }

    public String getSubjectName() {
        return subjectName;
    }

    public String getResourceName() {
        return resourceName;
    }

    public String getVisitName() {
        return visitName;
    }

    ////////////

    private static Map<String, Comparator<OverbookedResourcesResponse>> comparatorMap;
    static {
        comparatorMap = Maps.newHashMap();

        comparatorMap.put("resourceName", compareVia(OverbookedResourcesResponse::getResourceName));
        comparatorMap.put("visitName", compareVia(OverbookedResourcesResponse::getVisitName));
        comparatorMap.put("subjectName", compareVia(OverbookedResourcesResponse::getSubjectName));
        comparatorMap.put("conflictedTime", compareVia(OverbookedResourcesResponse::getConflictedTime));
        comparatorMap.put("scheduledEndTime", compareVia(OverbookedResourcesResponse::getScheduledEndTime));
        comparatorMap.put("scheduledStartTime", compareVia(OverbookedResourcesResponse::getScheduledStartTime));
        comparatorMap.put("visitEndTime", compareVia(OverbookedResourcesResponse::getVisitEndTime));
        comparatorMap.put("visitStartTime", compareVia(OverbookedResourcesResponse::getVisitStartTime));
        comparatorMap.put("localId", compareVia(OverbookedResourcesResponse::getLocalId));

    }
    public static Comparator<OverbookedResourcesResponse> getComparator(final String sortOn, final String sortBy) {

        Comparator<OverbookedResourcesResponse> comparator = comparatorMap.get(sortOn);
        if (sortBy != null && sortBy.equalsIgnoreCase("desc")) {
            comparator = invert(comparator);
        }
        return comparator;
    }
}
