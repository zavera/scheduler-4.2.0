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

import static edu.harvard.catalyst.hccrc.core.util.RichList.enrich;

import java.util.Date;
import java.util.List;

import edu.harvard.catalyst.scheduler.core.SchedulerRuntimeException;
import edu.harvard.catalyst.scheduler.entity.TemplateResource;

/**
 * Created with IntelliJ IDEA.
 * User: carl
 * Date: 4/7/14
 * Time: 3:27 PM
 */
public final class GetSearchVisitResourceResponse {

    private final Long totalCount;

    // needs to be public for testability
    public List<TemplateResource1> templateResource1s;

    private GetSearchVisitResourceResponse(final List<TemplateResource1> templateResource1s, final Long totalCount) {
        this.totalCount = totalCount;
        this.templateResource1s = templateResource1s;
    }

    // needs to be public for testability
    public static final class TemplateResource1 {
        public final Integer id;
        public final String resourceName;

        public final Integer startMinutes;
        public final Integer endMinutes;

        TemplateResource1(final TemplateResource templateResource) {
            if (templateResource == null) {
                SchedulerRuntimeException.logAndThrow("VisitResource should not be null!");
            }

            id = templateResource.getId();
            resourceName = templateResource.getResource().getName();
            startMinutes = templateResource.getStartMinutes();
            endMinutes = templateResource.getEndMinutes();
        }
    }

    public static GetSearchVisitResourceResponse createGetSearchVisitsResponse(final List<TemplateResource> templateResources, final Long total) {

        final List<TemplateResource1> templateResource1List =  enrich(templateResources).map(tr -> new TemplateResource1(tr)).toList();

        return new GetSearchVisitResourceResponse(templateResource1List, total);
    }
}
