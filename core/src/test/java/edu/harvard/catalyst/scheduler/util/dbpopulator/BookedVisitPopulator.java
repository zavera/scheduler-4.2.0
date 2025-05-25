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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by xavier on 9/22/17.
 */
public class BookedVisitPopulator {

    DbPopulator dataPopulator;

    public List<BookedVisit> bookedVisits = new ArrayList<>();

    public BookedVisitPopulator(DbPopulator dataPopulator) {

        this.dataPopulator = dataPopulator;

    }

    public BookedVisit populateOne(
            Study study,
            VisitType visitType,
            VisitTemplate visitTemplate,
            AppointmentStatus appointmentStatus,
            Date scheduledStartTime,
            Date scheduledEndTime,
            Date checkInDate,
            User checkInUser,
            Date checkOutDate,
            User checkOutUser,
            Date cancelDate,
            User cancelUser
    ) {

        BookedVisit bookedVisit = new BookedVisit();
        bookedVisit.setStudy(study);
        bookedVisit.setVisitType(visitType);
        bookedVisit.setVisitTemplate(visitTemplate);
        bookedVisit.setAppointmentStatus(appointmentStatus);
        bookedVisit.setScheduledStartTime(scheduledStartTime);
        bookedVisit.setScheduledEndTime(scheduledEndTime);
        bookedVisit.setCheckInDate(checkInDate);
        bookedVisit.setCheckInUser(checkInUser);
        bookedVisit.setCheckOutDate(checkOutDate);
        bookedVisit.setCheckOutUser(checkOutUser);
        bookedVisit.setCancelDate(cancelDate);
        bookedVisit.setCancelUser(cancelUser);

        this.dataPopulator.dao.createEntity(bookedVisit);
        this.bookedVisits.add(bookedVisit);

        return bookedVisit;

    }

    public List<BookedVisit> populateMany(
            int startingEntityNumber,
            int numberOfEntities,
            Study study,
            VisitType visitType,
            VisitTemplate visitTemplate,
            AppointmentStatus appointmentStatus,
            Date scheduledStartTime,
            Date scheduledEndTime,
            Date checkInDate,
            User checkInUser,
            Date checkOutDate,
            User checkOutUser,
            Date cancelDate,
            User cancelUser
    ) {

        List<BookedVisit> newEntities = new ArrayList<>();

        for (int i = startingEntityNumber; i < startingEntityNumber + numberOfEntities; i++) {
            BookedVisit bookedVisit = populateOne(
                    study,
                    visitType,
                    visitTemplate,
                    appointmentStatus,
                    scheduledStartTime,
                    scheduledEndTime,
                    checkInDate,
                    checkInUser,
                    checkOutDate,
                    checkOutUser,
                    cancelDate,
                    cancelUser
            );
            newEntities.add(bookedVisit);
        }

        return newEntities;

    }

    public BookedVisit populateOneWithDependencies(VisitTemplate visitTemplate) {

        BookedVisit bookedVisit = new BookedVisit();
        bookedVisit.setStudy(visitTemplate.getStudy());
        bookedVisit.setVisitType(visitTemplate.getVisitType());
        bookedVisit.setVisitTemplate(visitTemplate);
        AppointmentStatus appointmentStatus = this.dataPopulator.appointmentStatusPopulator.populateOne(1);
        bookedVisit.setAppointmentStatus(appointmentStatus);

        Date scheduledStartTime = new Date();
        User user = dataPopulator.userPopulator.populateOneWithDependencies(1);

        bookedVisit.setScheduledStartTime(scheduledStartTime);
        bookedVisit.setScheduledEndTime(new Date(scheduledStartTime.getTime() + 1));
        bookedVisit.setCheckInDate(new Date(scheduledStartTime.getTime() + 2));
        bookedVisit.setCheckInUser(user);
        bookedVisit.setCheckOutDate(new Date(scheduledStartTime.getTime() + 3));
        bookedVisit.setCheckOutUser(user);
        bookedVisit.setCancelDate(new Date(scheduledStartTime.getTime() + 4));
        bookedVisit.setCancelUser(user);

        this.dataPopulator.dao.createEntity(bookedVisit);
        this.bookedVisits.add(bookedVisit);

        return bookedVisit;

    }

}
