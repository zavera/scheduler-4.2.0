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

import edu.harvard.catalyst.scheduler.entity.reporttemplate.ReportTemplate;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by xavier on 8/2/17.
 */
public class ReportTemplatePopulator {

    DbPopulator dataPopulator;

    public List<ReportTemplate> reportTemplates = new ArrayList<>();

    public ReportTemplatePopulator(DbPopulator dataPopulator) {

        this.dataPopulator = dataPopulator;

    }

    public List<ReportTemplate> populateMany(
            int startingEntityNumber,
            int numberOfEntities
    ) {

        List<ReportTemplate> newEntities = new ArrayList<>();

        for (int i = startingEntityNumber; i < startingEntityNumber + numberOfEntities; i++) {
            ReportTemplate reportTemplate = new ReportTemplate();
            reportTemplate.setDisplayName("display name for report template " + i);
            reportTemplate.setType(ReportTemplate.TemplateType.Administrative);
            dataPopulator.dao.createEntity(reportTemplate);
            this.reportTemplates.add(reportTemplate);
            newEntities.add(reportTemplate);
        }

        return newEntities;

    }

}
