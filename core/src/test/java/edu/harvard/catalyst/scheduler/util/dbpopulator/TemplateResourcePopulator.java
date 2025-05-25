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
package edu.harvard.catalyst.scheduler.util.dbpopulator;

import edu.harvard.catalyst.scheduler.entity.*;
import edu.harvard.catalyst.scheduler.util.DateUtility;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by xavier on 8/2/17.
 */
public class TemplateResourcePopulator {

    DbPopulator dataPopulator;

    public List<TemplateResource> templateResources = new ArrayList<>();

    public TemplateResourcePopulator(DbPopulator dataPopulator) {

        this.dataPopulator = dataPopulator;

    }

    public TemplateResource populateOne(
            String templateResourceGroupId,
            VisitTemplate visitTemplate,
            Resource resource,
            Integer startMinutes,
            Integer endMinutes,
            Integer floatStart,
            Integer floatEnd,
            boolean billable,
            boolean floatable,
            boolean flexible
    ) {

        TemplateResource templateResource = new TemplateResource();
        templateResource.setGroupId(templateResourceGroupId);
        templateResource.setVisitTemplate(visitTemplate);
        templateResource.setResource(resource);
        templateResource.setStartMinutes(startMinutes);
        templateResource.setEndMinutes(endMinutes);
        templateResource.setFloatStart(floatStart);
        templateResource.setFloatEnd(floatEnd);
        templateResource.setBillable(billable);
        templateResource.setFloatable(floatable);
        templateResource.setFlexible(flexible);
        // make sure new datestamps will be larger than existing timestamps
        // by subtracting 1 millisecond from the current date
        templateResource.setLastUpdateTime(DateUtility.oneMillisecondBeforeNow());

        dataPopulator.dao.createEntity(templateResource);
        this.templateResources.add(templateResource);

        return templateResource;

    }

    public List<TemplateResource> populateMany(
            int numberOfEntities,
            String templateResourceGroupId,
            VisitTemplate visitTemplate,
            Resource resource,
            Integer startMinutes,
            Integer endMinutes,
            Integer floatStart,
            Integer floatEnd,
            boolean billable,
            boolean floatable,
            boolean flexible
    ) {

        List<TemplateResource> newEntities = new ArrayList<>();

        for (int i = 1; i <= numberOfEntities; i++) {
            TemplateResource templateResource = populateOne(
                    templateResourceGroupId,
                    visitTemplate,
                    resource,
                    startMinutes,
                    endMinutes,
                    floatStart,
                    floatEnd,
                    billable,
                    floatable,
                    flexible
            );
            newEntities.add(templateResource);
        }

        return newEntities;

    }

}
