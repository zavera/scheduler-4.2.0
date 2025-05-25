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

import edu.harvard.catalyst.scheduler.core.SchedulerRuntimeException;
import edu.harvard.catalyst.scheduler.entity.VisitTemplate;
import static edu.harvard.catalyst.scheduler.core.Statics.NA;

/**
 * User: ankit
 * Date: 3/14/14
 * Time: 1:36 PM
 */
public final class VisitTemplateDetailResponse {

    private final Integer id;

    private final String visitName;
    private final String studyName;
    private final String shortName;
    private final String sublocationName;
    private final int sublocationId;
    private final String visitTypeName;
    private final int visitTypeId;
    private final String comment;

    private final boolean researchPharmacy;
    private final boolean institutionNonCrc;
    private final boolean nonInstitutionNonCrc;

    private final String nursing;
    private final String nutrition;
    private final String processing;
    private final String setup;

    private final boolean relativeTime;
    private final String approved;
    private final Date createdDate;
    private long numVisitTemplateComments;

    private final boolean billable;

    public VisitTemplateDetailResponse(final VisitTemplate visit) {
        if (visit == null) {
            SchedulerRuntimeException.logAndThrow("Visit parameter should be non-null");
        }

        id = visit.getId();
        visitName = visit.getName();
        studyName = visit.getStudy().getName();
        shortName = visit.getShortName();
        sublocationName = visit.getSublocation().getName();
        sublocationId = visit.getSublocation().getId();
        visitTypeName = visit.getVisitType().getName();
        visitTypeId = visit.getVisitType().getId();
        comment = visit.getComment();
        researchPharmacy = visit.getResearchPharmacy();
        institutionNonCrc = visit.getInstitutionNonCRC();
        nonInstitutionNonCrc = visit.getNonInstitutionNonCRC();
        createdDate = visit.getCreatedDate();
        nursing = visit.getNursing().isEmpty() || visit.getNursing() == null ? NA : visit.getNursing();
        nutrition = visit.getNutrition().isEmpty() || visit.getNutrition() == null ? NA : visit.getNutrition();
        processing = visit.getProcessing().isEmpty() || visit.getProcessing() == null ? NA : visit.getProcessing();
        setup = visit.getSetup().isEmpty() || visit.getSetup() == null ? NA : visit.getSetup();
        relativeTime = visit.getRelativeTime();
        approved = visit.getApproved() ? "Approved" : "Not Approved";
        billable = visit.getBillable();
    }

    public void setNumVisitTemplateComments(long numVisitTemplateComments) {
        this.numVisitTemplateComments = numVisitTemplateComments;
    }

    public Long getNumVisitTemplateComments() {
        return numVisitTemplateComments;
    }

}
