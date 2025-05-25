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

import java.util.Date;

/**
 * User: ankit
 * Date: 3/20/14
 * Time: 11:20 AM
 */
public class CalendarVisitsResponse {

    private int id;
    private String visitName;
    private String piName;
    private String room;
    private String localId;
    private String title;
    private String start;
    private String end;
    private String className;
    private boolean allDay;
    private boolean inpatientVisitType;
    private String subjectFirstName;
    private String subjectMrn;
    private Long scheduleData;
    private int commentCount;

    public CalendarVisitsResponse(int id, String visitName, String piName, String room, String localId,
                                  String subjectName, String appointmentStatus, String scheduledStartTime,
                                  String scheduledEndTime, boolean allDay, boolean inpatientVisitType) {
        this.id = id;
        this.visitName = visitName;
        this.piName = piName;
        this.room = room;
        this.localId = localId;
        this.title = subjectName;
        this.className = appointmentStatus;
        this.start = scheduledStartTime;
        this.end = scheduledEndTime;
        this.allDay = allDay;
        this.inpatientVisitType = inpatientVisitType;
    }

    // getters are needed for testability

    public int getId() {
        return id;
    }

    public String getVisitName() {
        return visitName;
    }

    public String getPiName() {
        return piName;
    }

    public String getRoom() {
        return room;
    }

    public String getLocalId() {
        return localId;
    }

    public String getTitle() {
        return title;
    }

    public String getStart() {
        return start;
    }

    public String getEnd() {
        return end;
    }

    public String getClassName() {
        return className;
    }

    public boolean isAllDay() {
        return allDay;
    }

    public boolean isInpatientVisitType() {
        return inpatientVisitType;
    }

    public String getSubjectFirstName(){ return subjectFirstName; }

    public String getSubjectMrn(){ return subjectMrn; }

    public void setSubjectFirstName(String subjectFirstName){ this.subjectFirstName = subjectFirstName; }

    public void setSubjectMrn(String subjectMrn){ this.subjectMrn = subjectMrn; }

    public void setRoom(String room){ this.room = room; }

    public Long getScheduleData() {
        return scheduleData;
    }

    public void setScheduleData(Long scheduleData) {
        this.scheduleData = scheduleData;
    }

    public int getCommentCount() {
        return this.commentCount;
    }

    public void setCommentCount(int commentCount) {
        this.commentCount = commentCount;
    }
}
