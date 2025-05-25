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
package edu.harvard.catalyst.scheduler.dto;

import edu.harvard.catalyst.scheduler.core.Statics;
import edu.harvard.catalyst.scheduler.entity.BookedVisit;
import edu.harvard.catalyst.scheduler.util.SubjectDataEncryptor;

import static edu.harvard.catalyst.scheduler.util.DateUtility.dateHourMinSec;
import static edu.harvard.catalyst.scheduler.util.DateUtility.format;

public class VisitRenderSummaryDTO extends DTO {

    private String id;
    private String localId;
    private String piName;
    private String visitName;
    private String room;
    private String title;
    private String startDate;
    private String endDate;
    private String className;
    private boolean inpatientVisitType;
    private boolean allDay;

    public VisitRenderSummaryDTO(BookedVisit bookedVisit,
                                 String className,
                                 boolean homeScreen) {


        this.className = className;

        this.room = bookedVisit.getRooms() == null ? "" : bookedVisit.getRooms();
        this.title = bookedVisit.getSubjectMrn() != null
                ? SubjectDataEncryptor.decrypt(bookedVisit.getSubjectMrn().getSubject().getLastName())
                : Statics.NO_SUBJECT_ASSIGNED;

        this.id = bookedVisit.getId().toString();
        
        this.localId = bookedVisit.getStudy().getLocalId();
        this.piName = bookedVisit.getStudy().getInvestigatorLastNameOrNA();
        this.visitName = bookedVisit.getName();
        this.startDate = format(dateHourMinSec(), bookedVisit.getScheduledStartTime());
        this.endDate = format(dateHourMinSec(), bookedVisit.getScheduledEndTime());
        this.inpatientVisitType = bookedVisit.getVisitTemplate().getVisitType().isInpatient();
        this.allDay = homeScreen && bookedVisit.getVisitType().isInpatient();
    }

    public String getClassName() {
        return className;
    }

    public boolean isAllDay() {
        return allDay;
    }

    public String getId() {
        return id;
    }

    public String getRoom() {
        return room;
    }

    public String getTitle() {
        return title;
    }

    public String getLocalId() {
        return localId;
    }

    public String getPiName() {
        return piName;
    }

    public String getVisitName() {
        return visitName;
    }

    public String getStartDate() {
        return startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public boolean isInpatientVisitType() {
        return inpatientVisitType;
    }

}
