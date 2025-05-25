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

import java.util.List;

import static edu.harvard.catalyst.scheduler.util.MiscUtil.*;

public class NursingAndRoomDailyOverviewReportDTO extends DailyOverviewReportDTO {


    public NursingAndRoomDailyOverviewReportDTO() {
    }


    @Override
    public String toCsvHeaders() {
        return "Visit Start Time, Visit End Time, Subject Name, MRN, " +
                "Local ID, Visit Name, Visit Type, Resource Name," +
                "Is part of the Visit Off Unit?, Clipboard, Setup, " +
                "Check-In, Check-Out,Comment";
    }

    @Override
    public List<String> toCsvRows(List<?> dtoList) {

        List<String> result = Lists.newArrayList();
        result.add(toCsvHeaders() + "\n");

        for (Object object: dtoList) {
            NursingAndRoomDailyOverviewReportDTO d = (NursingAndRoomDailyOverviewReportDTO) object;

            List<String> columns = Lists.newArrayList();

            columns.add(q(showDateTime(d.scheduledStartTime)));
            columns.add(q(showDateTime(d.scheduledEndTime)));
            columns.add(q(fullName(
                    d.firstName,
                    d.middleName,
                    d.lastName)));
            columns.add(q(d.mrn));
            columns.add(q(d.localId));
            columns.add(q(d.visitName));
            columns.add(q(d.visitType));
            columns.add(q(d.resourceName));
            columns.add(q(d.offInstitution));
            columns.add("");
            columns.add("");
            columns.add("");
            columns.add("");
            columns.add(q(d.comment));

            String rows = Joiner.on(",").join(columns);
            result.add(rows + "\n");
        }
        return result;
    }
}
