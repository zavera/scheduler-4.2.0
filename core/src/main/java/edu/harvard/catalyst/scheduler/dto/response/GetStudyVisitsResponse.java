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

import static edu.harvard.catalyst.hccrc.core.util.ListUtils.enrich;

import java.util.List;

import edu.harvard.catalyst.scheduler.core.SchedulerRuntimeException;
import edu.harvard.catalyst.scheduler.entity.VisitTemplate;

/**
 * Created with IntelliJ IDEA.
 * User: carl
 * Date: 4/7/14
 * Time: 3:27 PM
 */
public final class GetStudyVisitsResponse {

    private final Long totalCount;
    private final Integer studyId;
    private final String studyName;
    private final String localId;
    private final String investigatorFullName;

    // public for testability
    public final List<VisitTemplate1> visitTemplate1s;

    private GetStudyVisitsResponse(final List<VisitTemplate1> visitTemplate1s, final Long totalCount, final String studyName, final String localId, final String investigatorName, final int studyId) {
        this.totalCount = totalCount;
        this.studyId = studyId;
        this.studyName = studyName;
        this.localId = localId;
        investigatorFullName = investigatorName;
        this.visitTemplate1s = visitTemplate1s;
    }

    // public for testability
    public static class VisitTemplate1 {
        // public for testability
        public final Integer id;
        private final String name;
        private final Integer visitTypeId;
        private final Integer visitDuration;
        private final boolean relativeTime;

        VisitTemplate1(final VisitTemplate visitTemplate) {
            if (visitTemplate == null) {
                SchedulerRuntimeException.logAndThrow("VisitTemplate should not be null!");
            }

            id = visitTemplate.getId();
            name = visitTemplate.getName();
            visitTypeId = visitTemplate.getVisitType().getId();
            visitDuration = visitTemplate.getDuration();
            relativeTime = visitTemplate.getRelativeTime();
        }
    }

    public static GetStudyVisitsResponse createGetStudyVisitsResponse(final List<VisitTemplate> visitTemplates, final Long total, final String studyName, final String localId, final String investigatorName, final int studyId) {

        final List<VisitTemplate1> visitTemplate1List = enrich(visitTemplates).map(v -> new VisitTemplate1(v)).toList();
        
        return new GetStudyVisitsResponse(visitTemplate1List, total, studyName, localId, investigatorName, studyId);
    }
}
