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

import com.google.gson.Gson;
import edu.harvard.catalyst.hccrc.core.util.RichList;
import edu.harvard.catalyst.scheduler.entity.Resource;
import edu.harvard.catalyst.scheduler.entity.TemplateResource;

import java.util.List;
import java.util.function.Predicate;

import static edu.harvard.catalyst.scheduler.core.SchedulerRuntimeException.logAndThrow;

/**
 * Created with IntelliJ IDEA.
 * User: carl
 * Date: 2/28/14
 * Time: 1:02 PM
 */
public final class GanttResourcesResponseDTO extends GanttResponseDTO {
    private final List<GanttResourceDetail> ganttResourceDetailList;
    private final int totalVisitResources;
    private final int totalVisitCalendarDays;

    // for junit
    public List<GanttResourceDetail> getGanttResourceDetailList() {
        return ganttResourceDetailList;
    }

    public GanttResourcesResponseDTO(final List<TemplateResource> allTemplateResourcesFromVisit,
                                     final Predicate<? super TemplateResource> relevancePredicate) {

        final List<TemplateResource> relevantTemplateResourcesFromVisit = getRelevantTemplateResources(
                allTemplateResourcesFromVisit, relevancePredicate);

        ganttResourceDetailList =
                RichList.enrich(relevantTemplateResourcesFromVisit)
                        .map(tr -> new GanttResourceDetail(tr))
                        .toList();

        this.totalVisitResources = allTemplateResourcesFromVisit.size();
        this.totalVisitCalendarDays = TemplateResource.totalVisitCalendarDays(allTemplateResourcesFromVisit);
    }

    @Override
    public String jsonify(Gson gson) {
        return gson.toJson(this);
    }

    public static final class GanttResourceDetail {

        private final String resourceName;
        private final String resourceType;
        private final int id;
        private final int templateResourceId;
        private final int earliestStart;

        GanttResourceDetail(final TemplateResource templateResource) {

            if (templateResource == null || templateResource.getResource() == null) {
                logAndThrow("TemplateResource and Resource should be non-null");
            }
            templateResourceId = templateResource.getId();

            Resource itsResource = templateResource.getResource();

            boolean isGrouped = templateResource.getGroupId() != null;
            id = isGrouped ? templateResourceId : itsResource.getId();
            resourceName = itsResource.getName();
            resourceType = itsResource.getResourceType().getName();
            earliestStart = templateResource.getStartMinutes();
        }
    }
}
