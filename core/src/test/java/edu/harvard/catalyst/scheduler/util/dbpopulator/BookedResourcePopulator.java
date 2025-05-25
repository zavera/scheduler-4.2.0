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

import edu.harvard.catalyst.scheduler.entity.BookedResource;
import edu.harvard.catalyst.scheduler.entity.BookedVisit;
import edu.harvard.catalyst.scheduler.entity.TemplateResource;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by xavier on 9/22/17.
 */
public class BookedResourcePopulator {

    DbPopulator dataPopulator;

    public List<BookedResource> bookedResources = new ArrayList<>();

    public BookedResourcePopulator(DbPopulator dataPopulator) {

        this.dataPopulator = dataPopulator;

    }

    public BookedResource populateOne(
            TemplateResource templateResource,
            BookedVisit bookedVisit,
            Date scheduledStartTime,
            Date scheduledEndTime
    ) {

        BookedResource bookedResource = new BookedResource();
        bookedResource.setTemplateResource(templateResource);
        bookedResource.setBookedVisit(bookedVisit);
        bookedResource.setScheduledStartTime(scheduledStartTime);
        bookedResource.setScheduledEndTime(scheduledEndTime);
        dataPopulator.dao.createEntity(bookedResource);
        this.bookedResources.add(bookedResource);

        return bookedResource;

    }

    public List<BookedResource> populateMany(
            int startingEntityNumber,
            int numberOfEntities,
            TemplateResource templateResource,
            BookedVisit bookedVisit,
            Date scheduledStartTime,
            Date scheduledEndTime
    ) {

        List<BookedResource> newEntities = new ArrayList<>();

        for (int i = startingEntityNumber; i < startingEntityNumber + numberOfEntities; i++) {
            BookedResource bookedResource = populateOne(
                    templateResource,
                    bookedVisit,
                    scheduledStartTime,
                    scheduledEndTime);
            newEntities.add(bookedResource);
        }

        return newEntities;

    }

}

