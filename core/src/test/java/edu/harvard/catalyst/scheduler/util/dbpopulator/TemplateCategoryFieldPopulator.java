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

import edu.harvard.catalyst.scheduler.entity.reporttemplate.Category;
import edu.harvard.catalyst.scheduler.entity.reporttemplate.Field;
import edu.harvard.catalyst.scheduler.entity.reporttemplate.ReportTemplate;
import edu.harvard.catalyst.scheduler.entity.reporttemplate.TemplateCategoryField;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by xavier on 8/2/17.
 */
public class TemplateCategoryFieldPopulator {

    DbPopulator dataPopulator;

    public List<TemplateCategoryField> templateCategoryFields = new ArrayList<>();

    public TemplateCategoryFieldPopulator(DbPopulator dataPopulator) {

        this.dataPopulator = dataPopulator;

    }

    public TemplateCategoryField populateOne(
            int entityNumber,
            Category category,
            Field field,
            ReportTemplate reportTemplate
    ) {

        TemplateCategoryField templateCategoryField = new TemplateCategoryField();
        templateCategoryField.setCategory(category);
        templateCategoryField.setField(field);
        templateCategoryField.setReportTemplate(reportTemplate);
        dataPopulator.dao.createEntity(templateCategoryField);
        this.templateCategoryFields.add(templateCategoryField);

        return templateCategoryField;

    }

}
