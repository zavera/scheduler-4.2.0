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

import java.util.Date;

public class VisitTerminatedPriorToCompletionDTO extends BooleanResultDTO {

    private int id;
    private int visitTemplate;
    private int resource;
    private String visitName;
    private String localId;
    private String studyName;
    private Date startDate;
    private String endDate;
    private String subjectFirstName;
    private String subjectMiddleName;
    private String subjectLastName;
    private String resourceName;
    private String percentCompleted;

    public int getId() {
        return id;
    }

    public void setId(final int id) {
        this.id = id;
    }

    public int getVisitTemplate() {
        return visitTemplate;
    }

    public void setVisitTemplate(final int visitTemplate) {
        this.visitTemplate = visitTemplate;
    }

    public int getResource() {
        return resource;
    }

    public void setResource(final int resource) {
        this.resource = resource;
    }

    public String getVisitName() {
        return visitName;
    }

    public void setVisitName(final String visitName) {
        this.visitName = visitName;
    }

    public String getLocalId() {
        return localId;
    }

    public void setLocalId(final String localId) {
        this.localId = localId;
    }

    public String getStudyName() {
        return studyName;
    }

    public void setStudyName(final String studyName) {
        this.studyName = studyName;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(final Date startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(final String endDate) {
        this.endDate = endDate;
    }

    public String getSubjectFirstName() {
        return subjectFirstName;
    }

    public void setSubjectFirstName(final String subjectFirstName) {
        this.subjectFirstName = subjectFirstName;
    }

    public String getSubjectMiddleName() {
        return subjectMiddleName;
    }

    public void setSubjectMiddleName(final String subjectMiddleName) {
        this.subjectMiddleName = subjectMiddleName;
    }

    public String getSubjectLastName() {
        return subjectLastName;
    }

    public void setSubjectLastName(final String subjectLastName) {
        this.subjectLastName = subjectLastName;
    }

    public String getResourceName() {
        return resourceName;
    }

    public void setResourceName(final String resourceName) {
        this.resourceName = resourceName;
    }

    public String getPercentCompleted() {
        return percentCompleted;
    }

    public void setPercentCompleted(final String percentCompleted) {
        this.percentCompleted = percentCompleted;
    }
}
