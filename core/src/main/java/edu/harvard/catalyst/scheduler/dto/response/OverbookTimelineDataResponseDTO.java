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

import java.util.List;
import java.util.Map;

/**
 * User: ankit
 * Date: 3/5/14
 * Time: 8:02 AM
 */
public class OverbookTimelineDataResponseDTO {
    private int id;
    private String resourceName;
    private String rooms;
    private String visitHoverStartTime;
    private String visitHoverEndTime;
    private String resourceHoverStartTime;
    private String resourceHoverEndTime;
    private String visitName;
    private String piName;
    private String subjectFirstName;
    private String subjectLastName;
    private String studyLocalId;
    private int startTime;
    private int endTime;
    private int resourceCount;
    private String alternateResources;
    private List<Map<String,String>> defaultResourceSchedule;
    private List<Map<String,String>> temporaryResourceSchedule;

    public OverbookTimelineDataResponseDTO(int id, String resourceName, String rooms, String visitHoverStartTime,
                                           String visitHoverEndTime, String resourceHoverStartTime,
                                           String resourceHoverEndTime, String visitName, String piName,
                                           String studyLocalId, String subjectFirstName, String subjectLastName,
                                           long startMinutes, long endMinutes, long selectedMinutes,
                                           List<Map<String, String>> defaultResourceSchedule,
                                           List<Map<String, String>> temporaryResourceSchedule,
                                           String alternateResources,
                                           int resourceCount) {

        this.id = id;
        this.resourceName = resourceName;
        this.rooms = rooms;
        this.visitHoverStartTime = visitHoverStartTime;
        this.visitHoverEndTime = visitHoverEndTime;
        this.resourceHoverStartTime = resourceHoverStartTime;
        this.resourceHoverEndTime = resourceHoverEndTime;
        this.visitName = visitName;
        this.piName = piName;
        this.subjectFirstName = subjectFirstName;
        this.subjectLastName = subjectLastName;
        this.studyLocalId = studyLocalId;
        this.defaultResourceSchedule = defaultResourceSchedule;
        this.temporaryResourceSchedule = temporaryResourceSchedule;
        this.alternateResources = alternateResources;
        this.resourceCount = resourceCount;

        if (startMinutes == 0) {
            this.startTime = 0;
        }
        else {
            this.startTime = (int) (startMinutes - selectedMinutes);
        }

        if (endMinutes == 0) {
            this.endTime = 0;
        }
        else {
            this.endTime = (int) (endMinutes - selectedMinutes);
        }
    }

    public int getEndTime() {
        return endTime;
    }

    public int getResourceCount() {
        return resourceCount;
    }

    public String getAlternateResources() {
        return alternateResources;
    }

    public int getId() {
        return id;
    }

    public String getResourceName() {
        return resourceName;
    }

    public String getRooms() {
        return rooms;
    }

    public String getVisitHoverStartTime() {
        return visitHoverStartTime;
    }

    public String getVisitHoverEndTime() {
        return visitHoverEndTime;
    }

    public String getResourceHoverStartTime() {
        return resourceHoverStartTime;
    }

    public String getResourceHoverEndTime() {
        return resourceHoverEndTime;
    }

    public String getVisitName() {
        return visitName;
    }

    public String getPiName() {
        return piName;
    }

    public String getSubjectFirstName() {
        return subjectFirstName;
    }

    public String getSubjectLastName() {
        return subjectLastName;
    }

    public String getStudyLocalId() {
        return studyLocalId;
    }

    public int getStartTime() {
        return startTime;
    }

    public List<Map<String, String>> getDefaultResourceSchedule() {
        return defaultResourceSchedule;
    }

    public List<Map<String, String>> getTemporaryResourceSchedule() {
        return temporaryResourceSchedule;
    }
}
