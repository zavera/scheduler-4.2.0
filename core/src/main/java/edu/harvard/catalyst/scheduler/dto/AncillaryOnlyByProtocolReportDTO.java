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

import static edu.harvard.catalyst.scheduler.util.MiscUtil.q;

public class AncillaryOnlyByProtocolReportDTO implements CsvAbleDTO {

    private Integer studyId;
    private String studyName;
    private String crcA;
    private String crcB;
    private String crcC;
    private int totalCRCA;
    private int totalCRCB;
    private int totalCRCC;
    

    public AncillaryOnlyByProtocolReportDTO() {
    }
    
    public Integer getStudyId() {
        return studyId;
    }

    public void setStudyId(Integer studyId) {
        this.studyId = studyId;
    }

    public String getStudyName() {
        return studyName;
    }

    public void setStudyName(String studyName) {
        this.studyName = studyName;
    }

    public String getCrcA() {
        return crcA;
    }

    public void setCrcA(String crcA) {
        this.crcA = crcA;
    }

    public String getCrcB() {
        return crcB;
    }

    public void setCrcB(String crcB) {
        this.crcB = crcB;
    }

    public String getCrcC() {
        return crcC;
    }

    public void setCrcC(String crcC) {
        this.crcC = crcC;
    }

    public int getTotalCRCA() {
        return totalCRCA;
    }

    public void setTotalCRCA(int totalCRCA) {
        this.totalCRCA = totalCRCA;
    }

    public int getTotalCRCB() {
        return totalCRCB;
    }

    public void setTotalCRCB(int totalCRCB) {
        this.totalCRCB = totalCRCB;
    }

    public int getTotalCRCC() {
        return totalCRCC;
    }

    public void setTotalCRCC(int totalCRCC) {
        this.totalCRCC = totalCRCC;
    }

    @Override
    public String toCsvHeaders() {
        return "Protocol,,Ancillary Only Visits\n,A, B, D";
    }

    @Override
    public List<String> toCsvRows(List<?> dtoList) {

        List<String> result = Lists.newArrayList();
        result.add(toCsvHeaders() + "\n");

        int listSize = dtoList.size();
        for (int i=0; i<listSize; i++) {
            Object object = dtoList.get(i);

            AncillaryOnlyByProtocolReportDTO d =
                    (AncillaryOnlyByProtocolReportDTO) object;

            List<String> columns = Lists.newArrayList();

            columns.add(q(d.studyName));
            columns.add(q(d.crcA));
            columns.add(q(d.crcB));
            columns.add(q(d.crcC));

            String rows = Joiner.on(",").join(columns);
            result.add(rows + "\n");

            // after last row
            if (i == listSize - 1) {
                columns = Lists.newArrayList();

                columns.add(q("Total"));
                columns.add(q(d.totalCRCA));
                columns.add(q(d.totalCRCB));
                columns.add(q(d.totalCRCC));

                rows = Joiner.on(",").join(columns);
                result.add(rows + "\n");
            }
        }
        return result;
    }
}