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

public class SwitchSubjectResultDTO extends BooleanResultDTO {

    private Integer newSubjectMrnId;
    private String fullName;
    private boolean genderBlockWarning;
    private boolean doubleBookingWarning;
    private VisitRenderSummaryDTO visitSummary;
    public SwitchSubjectResultDTO() {
    }

    public SwitchSubjectResultDTO(
            boolean success,
            Integer newSubjectMrnId,
            String fullName,
            VisitRenderSummaryDTO visitRenderSummaryDTO) {

        super(success);
        this.newSubjectMrnId = newSubjectMrnId;
        this.fullName = fullName;
        this.visitSummary = visitRenderSummaryDTO;
    }

    public SwitchSubjectResultDTO(
            boolean success,
            Integer newSubjectMrnId,
            String fullName,
            VisitRenderSummaryDTO visitRenderSummaryDTO,
            boolean genderBlockWarning,
            boolean doubleBookingWarning) {

        super(success);
        this.newSubjectMrnId = newSubjectMrnId;
        this.fullName = fullName;
        this.visitSummary = visitRenderSummaryDTO;
        this.genderBlockWarning = genderBlockWarning;
        this.doubleBookingWarning = doubleBookingWarning;
    }

    public Integer getNewSubjectMrnId() {

        return newSubjectMrnId;

    }

    public boolean getGenderBlockWarning() {

        return this.genderBlockWarning;

    }

    public boolean getDoubleBookingWarning() {

        return this.doubleBookingWarning;

    }

    public String getFullName() {
        return fullName;
    }

    public VisitRenderSummaryDTO getVisitSummary() {

        return visitSummary;
    }
}
