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

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import edu.harvard.catalyst.scheduler.entity.ActivityLog;

import java.util.Date;
import java.util.List;

import static edu.harvard.catalyst.scheduler.util.MiscUtil.fullName;
import static edu.harvard.catalyst.scheduler.util.MiscUtil.q;
import static edu.harvard.catalyst.scheduler.util.MiscUtil.showDateTime;

public class OverrideReportDTO implements CsvAbleDTO {

    private String performingUser_firstName;
    private String performingUser_middleName;
    private String performingUser_lastName;
    private String performingUser_ecommonsId;
    private Integer performingUser_id;

    private Date date;
    private String appointmentOverrideReason;
    private String affectedResource;
    private String bookedVisit;
    private String bookedVisit_study_catalystId;
    private String bookedVisit_study_localId;
    private String bookedVisit_study_irb;
    private Date bookedVisit_scheduledStartTime;

    public OverrideReportDTO(ActivityLog al) {
        performingUser_firstName = al.getPerformingUser().getFirstName();
        performingUser_middleName = al.getPerformingUser().getMiddleName();
        performingUser_lastName = al.getPerformingUser().getLastName();
        performingUser_ecommonsId = al.getPerformingUser().getEcommonsId();
        performingUser_id = al.getPerformingUser().getId();

        date = al.getDate();
        appointmentOverrideReason = al.getAppointmentOverrideReason().getName();
        affectedResource = al.getAffectedResource().getName();
        bookedVisit = al.getBookedVisit().getName();
        bookedVisit_scheduledStartTime = al.getBookedVisit().getScheduledStartTime();
        bookedVisit_study_catalystId = al.getBookedVisit().getStudy().getCatalystId();
        bookedVisit_study_localId = al.getBookedVisit().getStudy().getLocalId();
        bookedVisit_study_irb = al.getBookedVisit().getStudy().getIrb();
    }

    @Override
    public String toCsvHeaders() {
        return "Performing User,Override Time,Override Reason," +
                "Resource Name,Visit Name,Scheduled Visit Time," +
                "Catalyst ID,Local Id,IRB #";
    }

    @Override
    public List<String> toCsvRows(List<?> dtoList) {
        List<String> result = Lists.newArrayList();
        result.add(toCsvHeaders() + "\n");

        int previousUserId = -1;

        for (Object object: dtoList) {
            OverrideReportDTO d = (OverrideReportDTO) object;

            List<String> columns = Lists.newArrayList();

            int currentUserId = d.performingUser_id;
            if (currentUserId != previousUserId) {
                columns.add(q(fullName(
                        d.performingUser_firstName,
                        d.performingUser_middleName,
                        d.performingUser_lastName)));
            }
            else {
                columns.add("");
            }
            previousUserId = currentUserId;

            columns.add(q(showDateTime(d.date)));
            columns.add(q(d.appointmentOverrideReason));
            columns.add(q(d.affectedResource));
            columns.add(q(d.bookedVisit));
            columns.add(q(showDateTime(d.bookedVisit_scheduledStartTime)));
            columns.add(q(d.bookedVisit_study_catalystId));
            columns.add(q(d.bookedVisit_study_localId));
            columns.add(q(d.bookedVisit_study_irb));

            String rows = Joiner.on(",").join(columns);
            result.add(rows + "\n");
        }
        return result;
    }
}
