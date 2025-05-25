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

import java.util.Date;
import java.util.List;

import static edu.harvard.catalyst.scheduler.util.MiscUtil.*;
import static edu.harvard.catalyst.scheduler.util.MiscUtil.q;

public class ProtoNurseReportDTO implements CsvAbleDTO {

    private Integer nurseId;
    private String firstName;
    private String middleName;
    private String lastName;
    private String name;
    private String spid;
    private String catalystId;
    private String localId;
    private String studyStatusName;
    private String irb;
    private Date irbExpiration;

    public ProtoNurseReportDTO() {
    }

    public ProtoNurseReportDTO(final Integer nurseId, final String firstName, final String middleName, final String lastName, final String name, final String spid, final String catalystId, final String localId, final String studyStatusName, final String irb, final Date irbExpiration) {
        super();
        this.nurseId = nurseId;
        this.firstName = firstName;
        this.middleName = middleName;
        this.lastName = lastName;
        this.name = name;
        this.spid = spid;
        this.catalystId = catalystId;
        this.localId = localId;
        this.studyStatusName = studyStatusName;
        this.irb = irb;
        this.irbExpiration = irbExpiration;
    }

    //NB: Deprecated, man this pattern is nasty.  This method exists to factor some code out of ReportDAO, 
    //but we shouldn't be doing this kind of manual unmarshalling at all. :( -Clint
    @Deprecated
    public static ProtoNurseReportDTO fromArray(final Object[] bagOfFields) {
        return new ProtoNurseReportDTO((Integer)bagOfFields[0], (String)bagOfFields[1], (String)bagOfFields[2], (String)bagOfFields[3], (String)bagOfFields[4], (String)bagOfFields[5], (String)bagOfFields[6], (String)bagOfFields[7], (String)bagOfFields[8], (String)bagOfFields[9], (Date)bagOfFields[10]);
    }

    public Integer getNurseId() {
        return nurseId;
    }

    public void setNurseId(final Integer nurseId) {
        this.nurseId = nurseId;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(final String firstName) {
        this.firstName = firstName;
    }

    public String getMiddleName() {
        return middleName;
    }

    public void setMiddleName(final String middleName) {
        this.middleName = middleName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(final String lastName) {
        this.lastName = lastName;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getSpid() {
        return spid;
    }

    public void setSpid(final String spid) {
        this.spid = spid;
    }

    public String getCatalystId() {
        return catalystId;
    }

    public void setCatalystId(final String catalystId) {
        this.catalystId = catalystId;
    }

    public String getLocalId() {
        return localId;
    }

    public void setLocalId(final String localId) {
        this.localId = localId;
    }

    public String getStudyStatusName() {
        return studyStatusName;
    }

    public void setStudyStatusName(final String studyStatusName) {
        this.studyStatusName = studyStatusName;
    }

    public String getIrb() {
        return irb;
    }

    public void setIrb(final String irb) {
        this.irb = irb;
    }

    public Date getIrbExpiration() {
        return irbExpiration;
    }

    public void setIrbExpiration(final Date irbExpiration) {
        this.irbExpiration = irbExpiration;
    }

    @Override
    public String toCsvHeaders() {
        return "Full Name,Study Name,Catalyst ID,Local Id," +
                "Study Status,IRB #,IRB Expiration";
    }

    @Override
    public List<String> toCsvRows(List<?> dtoList) {
        List<String> result = Lists.newArrayList();

        int previousNurseId = -1;

        for (Object object: dtoList) {
            ProtoNurseReportDTO d = (ProtoNurseReportDTO) object;
            List<String> columns = Lists.newArrayList();

            int currentNurseId = d.getNurseId();
            if (currentNurseId != previousNurseId) {

                // blank row in between 'groups'
                if (previousNurseId != -1) {
                    result.add(" \n");
                }

                result.add(toCsvHeaders() + "\n");

                columns.add(q(fullName(
                        d.firstName,
                        d.middleName,
                        d.lastName)));

                previousNurseId = currentNurseId;
            }
            else {
                columns.add("");
            }

            columns.add(q(d.name));
            columns.add(q(d.catalystId));
            columns.add(q(d.localId));
            columns.add(q(d.studyStatusName));
            columns.add(q(d.irb));
            columns.add(q(showDate(d.irbExpiration)));

            String rows = Joiner.on(",").join(columns);
            result.add(rows + "\n");
        }
        return result;
    }
}
