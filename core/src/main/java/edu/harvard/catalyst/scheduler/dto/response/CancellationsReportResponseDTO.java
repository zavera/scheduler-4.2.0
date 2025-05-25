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

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import edu.harvard.catalyst.scheduler.core.SchedulerRuntimeException;
import edu.harvard.catalyst.scheduler.dto.CsvAbleDTO;
import edu.harvard.catalyst.scheduler.entity.BookedVisit;
import edu.harvard.catalyst.scheduler.entity.Study;
import edu.harvard.catalyst.scheduler.entity.SubjectMrn;
import edu.harvard.catalyst.scheduler.entity.User;

import java.util.Date;
import java.util.List;

import static edu.harvard.catalyst.scheduler.core.Statics.NA;
import static edu.harvard.catalyst.scheduler.core.Statics.NO_SUBJECT_ASSIGNED;
import static edu.harvard.catalyst.scheduler.util.MiscUtil.*;

/**
 * Created with IntelliJ IDEA.
 * User: carl
 * Date: 2/13/14
 * Time: 4:16 PM
 */
public class CancellationsReportResponseDTO implements CsvAbleDTO {
    private String asrName;
    private Date cancelDate;
    private String cancelStatus;
    private String catalystId;
    private String irb;
    private String localId;
    private String mrn;
    private String piFirstName;
    private String piMiddleName;
    private String piLastName;
    private Date scheduledDate;
    private Date scheduledStartTime;
    private Date scheduledEndTime;
    private String studyName;
    private String subjectFirstName;
    private String subjectMiddleName;
    private String subjectLastName;
    private String userEcommonsId;
    private String visitName;
    private String visitTypeName;

    public CancellationsReportResponseDTO(BookedVisit bookedVisit) {
        if (bookedVisit == null) {
            SchedulerRuntimeException.logAndThrow("BookedVisit parameter should be non-null"); // too bad
        }

        Study study = bookedVisit.getStudy();
        if (study == null) {
            SchedulerRuntimeException.logAndThrow("Study field of BookedVisit parameter should be non-null"); // too bad
        }

        /// ok, happy with basic invariants...

        this.asrName = bookedVisit.getCancelStatusReason().getName();

        this.cancelDate = bookedVisit.getCancelDate();

        this.cancelStatus = "";
        if (bookedVisit.getCancelStatus() != null) {
            this.cancelStatus = bookedVisit.getCancelStatus().getName();
        }

        this.catalystId = "";
        if (study.getCatalystId() != null) {
            this.catalystId = study.getCatalystId();
        }

        this.irb = "";
        if (study.getIrb() != null) {
            this.irb = study.getIrb();
        }

        this.localId = study.getLocalId();

        User investigator = study.getInvestigator();
        if (investigator != null) {
            this.piFirstName = study.getInvestigator().getFirstName();
            this.piMiddleName = study.getInvestigator().getMiddleName();
            this.piLastName = study.getInvestigator().getLastName();
        }
        else {
            this.piFirstName = "";
            this.piMiddleName = "";
            this.piLastName = NA;
        }

        this.scheduledDate = bookedVisit.getSchedulingTime();

        this.scheduledStartTime = bookedVisit.getScheduledStartTime();
        this.scheduledEndTime = bookedVisit.getScheduledEndTime();

        this.studyName = study.getName();

        SubjectMrn subjectMrn = bookedVisit.getSubjectMrnDecrypted();

        this.subjectFirstName = subjectMrn == null ? NO_SUBJECT_ASSIGNED : subjectMrn.getSubject().getFirstName();
        this.subjectMiddleName = subjectMrn == null ? NO_SUBJECT_ASSIGNED : subjectMrn.getSubject().getMiddleName();
        this.subjectLastName = subjectMrn == null ? NO_SUBJECT_ASSIGNED : subjectMrn.getSubject().getLastName();
        this.mrn = subjectMrn == null ? NA : subjectMrn.getMrn();

        this.userEcommonsId = bookedVisit.getSchedulingUser().getEcommonsId();

        this.visitName = bookedVisit.getVisitTemplate().getName();
        this.visitTypeName = bookedVisit.getVisitTemplate().getVisitType().getName();
    }

    @Override
    public String toCsvHeaders() {
        return "Study Name,Catalyst ID,Local ID,IRB #,PI Name," +
                "Subject,MRN,Visit Name,Visit Type,Visit Start Time," +
                "Visit End Time,Visit Length,Scheduled Date,Cancel Date," +
                "Reason,Cancellation Type,User";
    }

    @Override
    public List<String> toCsvRows(List<?> dtoList) {
        List<String> result = Lists.newArrayList();
        result.add(toCsvHeaders() + "\n");

        for (Object object: dtoList) {
            CancellationsReportResponseDTO d = (CancellationsReportResponseDTO) object;

            List<String> columns = Lists.newArrayList();

            columns.add(q(d.studyName));
            columns.add(q(d.catalystId));
            columns.add(q(d.localId));
            columns.add(q(d.irb));
            columns.add(q(fullName(
                    d.piFirstName,
                    d.piMiddleName,
                    d.piLastName)));
            columns.add(q(fullName(
                    d.subjectFirstName,
                    d.subjectMiddleName,
                    d.subjectLastName)));
            columns.add(q(d.mrn));
            columns.add(q(d.visitName));
            columns.add(q(d.visitTypeName));
            columns.add(q(showDateTime(d.scheduledStartTime)));
            columns.add(q(showDateTime(d.scheduledEndTime)));
            columns.add(q(formatEndMinusStart(
                    d.scheduledStartTime, d.scheduledEndTime)));
            columns.add(q(showDateTime(d.scheduledDate)));
            columns.add(q(showDateTime(d.cancelDate)));
            columns.add(q(d.asrName));
            columns.add(q(d.cancelStatus));
            columns.add(q(d.userEcommonsId));

            String rows = Joiner.on(",").join(columns);
            result.add(rows + "\n");
        }
        return result;
    }
}
