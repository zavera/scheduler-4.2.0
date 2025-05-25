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
import edu.harvard.catalyst.scheduler.entity.TemplateResource;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Created with IntelliJ IDEA.
 * User: carl
 * Date: 10/14/15
 * Time: 12:36 PM
 */
abstract public class GanttResponseDTO {

    abstract public String jsonify(Gson gson);

    List<TemplateResource> getRelevantTemplateResources(List<TemplateResource> allTemplateResourcesFromVisit, Predicate<? super TemplateResource> relevancePredicate) {
        List<TemplateResource> result = allTemplateResourcesFromVisit.stream()
                .filter(relevancePredicate)
                .collect(Collectors.toList());
        return result;
    }

    TemplateResource findTrByIdFromList(Integer id, List<TemplateResource> trList) {
        Optional<TemplateResource> trOptional = trList.stream().filter(tr -> tr.getId().equals(id)).findFirst();

        // precondition: it's really there in the list
        return trOptional.get();
    }

    public enum GanttInfoType {
        Resources {
            @Override
            public GanttResponseDTO create(final List<TemplateResource> allTemplateResourcesFromVisit,
                                           final int dayOffset,
                                           final Predicate<? super TemplateResource> relevancePredicate) {
                return new GanttResourcesResponseDTO(allTemplateResourcesFromVisit, relevancePredicate);
            }
        },
        ResourcesGroup {
            @Override
            public GanttResponseDTO create(final List<TemplateResource> allTemplateResourcesFromVisit,
                                           final int dayOffset,
                                           final Predicate<? super TemplateResource> relevancePredicate) {
                return new GanttResourcesGroupResponseDTO(allTemplateResourcesFromVisit, relevancePredicate);
            }
        },
        Events {
            @Override
            public GanttResponseDTO create(final List<TemplateResource> allTemplateResourcesFromVisit,
                                           final int dayOffset,
                                           final Predicate<? super TemplateResource> relevancePredicate) {
                return new GanttEventsResponseDTO(allTemplateResourcesFromVisit, relevancePredicate);
            }
        }
        ;

        abstract public GanttResponseDTO create(
                final List<TemplateResource> allTemplateResourcesFromVisit,
                final int dayOffset,
                final Predicate<? super TemplateResource> relevancePredicate);
    }
    public enum GanttGroupingType {

        Floatables {
            @Override
            public GanttGroupablesResponseDTO create(final List<TemplateResource> allTemplateResourcesFromVisit,
                                           final Integer groupAspirantId) {

                return new GanttGroupablesResponseDTO(
                        allTemplateResourcesFromVisit,
                        TemplateResource::getUngroupedFloatable,
                        groupAspirantId,
                        TemplateResource::isValidFloatGroup);
            }
        },
        Flexibles {
            @Override
            public GanttGroupablesResponseDTO create(final List<TemplateResource> allTemplateResourcesFromVisit,
                                           final Integer groupAspirantId) {

                return new GanttGroupablesResponseDTO(
                        allTemplateResourcesFromVisit,
                        TemplateResource::getUngroupedFlexible,
                        groupAspirantId,
                        TemplateResource::isValidFlexGroup);
            }
        };

        abstract public GanttGroupablesResponseDTO create(
                final List<TemplateResource> allTemplateResourcesFromVisit,
                final Integer groupAspirantId);
        }

}
