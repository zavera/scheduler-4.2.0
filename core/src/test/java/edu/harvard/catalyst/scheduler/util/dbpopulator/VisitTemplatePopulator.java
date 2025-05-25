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

import edu.harvard.catalyst.scheduler.entity.Study;
import edu.harvard.catalyst.scheduler.entity.Sublocation;
import edu.harvard.catalyst.scheduler.entity.VisitTemplate;
import edu.harvard.catalyst.scheduler.entity.VisitType;
import edu.harvard.catalyst.scheduler.util.DateUtility;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by xavier on 8/2/17.
 */
public class VisitTemplatePopulator {

    DbPopulator dataPopulator;

    public List<VisitTemplate> visitTemplates = new ArrayList<>();

    public VisitTemplatePopulator(DbPopulator dataPopulator) {

        this.dataPopulator = dataPopulator;

    }

    public VisitTemplate populateOne(
            int entityNumber,
            Study study,
            VisitType visitType,
            boolean approved,
            boolean relativeTime,
            boolean active
    ) {

        return populateOne(
            entityNumber,
            study,
            visitType,
            null,
            approved,
            relativeTime,
            active
        );

    }

    public VisitTemplate populateOne(
            int entityNumber,
            Study study,
            VisitType visitType,
            Sublocation sublocation,
            boolean approved,
            boolean relativeTime,
            boolean active
    ) {

        VisitTemplate visitTemplate = new VisitTemplate();
        visitTemplate.setStudy(study);
        visitTemplate.setName("visit template " + entityNumber);
        visitTemplate.setVisitType(visitType);
        visitTemplate.setSublocation(sublocation);
        visitTemplate.setApproved(approved);
        visitTemplate.setRelativeTime(relativeTime);
        visitTemplate.setActive(active);
        // make sure new datestamps will be larger than existing timestamps
        // by subtracting 1 millisecond from the current date
        visitTemplate.setLastUpdateTime(DateUtility.oneMillisecondBeforeNow());
        dataPopulator.dao.createEntity(visitTemplate);
        this.visitTemplates.add(visitTemplate);

        return visitTemplate;

    }

    public List<VisitTemplate> populateMany(
            int startingEntityNumber,
            int numberOfEntities,
            Study study,
            VisitType visitType,
            boolean approved,
            boolean relativeTime,
            boolean active
    ) {

        List<VisitTemplate> newEntities = new ArrayList<>();

        for (int i = startingEntityNumber; i < startingEntityNumber + numberOfEntities; i++) {
            VisitTemplate visitTemplate = populateOne(
                    i,
                    study,
                    visitType,
                    approved,
                    relativeTime,
                    active
            );
            newEntities.add(visitTemplate);
        }

        return newEntities;

    }

    public VisitTemplate populateOneWithDependencies(
            int entityNumber,
            boolean approved,
            boolean relativeTime,
            boolean active
    ) {

        Study study = this.dataPopulator.studyPopulator.populateOne(1);
        VisitType visitType = this.dataPopulator.visitTypePopulator.populateOne(1);
        Sublocation sublocation = this.dataPopulator.sublocationPopulator.populateOneWithDependencies(1);

        return populateOne(
            entityNumber,
            study,
            visitType,
            sublocation,
            approved,
            relativeTime,
            active
        );

    }

}
