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

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import edu.harvard.catalyst.scheduler.core.SchedulerRuntimeException;
import edu.harvard.catalyst.scheduler.entity.TemplateResource;
import edu.harvard.catalyst.scheduler.entity.Resource;

import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Created with IntelliJ IDEA.
 * User: carl
 * Date: 2/28/14
 * Time: 1:02 PM
 */
public final class GanttGroupablesResponseDTO extends GanttResponseDTO {
    private final List<GanttResourceDetail> acceptableGroupies;

    public GanttGroupablesResponseDTO(final List<TemplateResource> allTemplateResourcesFromVisit,
                                      final Predicate<? super TemplateResource> relevancePredicate,
                                      final Integer groupAspirantId,
                                      Function<List<TemplateResource>, String> groupValidityCheck) {

        final List<TemplateResource> groupableTemplateResourcesFromVisit = getRelevantTemplateResources(
                allTemplateResourcesFromVisit, relevancePredicate);

        TemplateResource aspirant = findTrByIdFromList(groupAspirantId, groupableTemplateResourcesFromVisit);
        List<TemplateResource> acceptableGroupieTRs;

        if (aspirant.getFloatable()) {
            groupableTemplateResourcesFromVisit.remove(aspirant);
            acceptableGroupieTRs = groupableTemplateResourcesFromVisit
                    .stream()
                    .filter(tr -> groupValidityCheck.apply(Lists.newArrayList(tr, aspirant)).equals("OK"))
                    .collect(Collectors.toList());
        } else {
            acceptableGroupieTRs = TemplateResource.getValidFlexGroup(groupableTemplateResourcesFromVisit, aspirant);
        }

        acceptableGroupies = acceptableGroupieTRs.stream()
                .map(tr -> new GanttResourceDetail(tr))
                .collect(Collectors.toList());

        acceptableGroupies.sort((o1, o2) -> {
            int timeCompare = o1.resourceStartMinutes.compareTo(o2.resourceStartMinutes);
            if (timeCompare != 0) {
                return timeCompare;
            }
            return o1.resourceName.compareTo(o2.resourceName);
        });
    }

    @Override
    public String jsonify(Gson gson) {
        return gson.toJson(this);
    }

    public static final class GanttResourceDetail {
        private final Integer resourceStartMinutes;
        private final Integer resourceEndMinutes;
        private final String resourceName;
        private final String resourceType;
        private final int id;

        GanttResourceDetail(final TemplateResource templateResource) {

            if (templateResource == null || templateResource.getResource() == null) {
                SchedulerRuntimeException.logAndThrow("TemplateResource and Resource should be non-null");
            }

            Resource itsResource = templateResource.getResource();

            id = templateResource.getId();
            resourceName = itsResource.getName();
            resourceType = itsResource.getResourceType().getName();
            resourceStartMinutes = templateResource.getStartMinutes();
            resourceEndMinutes = templateResource.getEndMinutes();
        }
    }
}
