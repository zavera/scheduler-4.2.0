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
import edu.harvard.catalyst.scheduler.entity.VisitType;

import java.io.Serializable;
import java.util.List;

import static edu.harvard.catalyst.scheduler.util.MiscUtil.*;

/**
 * Created by ankit on 7/5/17.
 */
public class WorkloadAndResourceResponseDTO implements Serializable, CsvAbleDTO {

    private Integer visitId;
    private String studyName;
    private String visitName;
    private String nursing;
    private String nutrition;
    private String processing;
    private String setup;
    private String visitType;
    private Integer duration;
    private String resourceName;

    public WorkloadAndResourceResponseDTO() {
    }

    public Integer getVisitId() {
        return visitId;
    }

    public void setVisitId(Integer visitId) {
        this.visitId = visitId;
    }

    public String getStudyName() {
        return studyName;
    }

    public void setStudyName(String studyName) {
        this.studyName = studyName;
    }

    public String getVisitName() {
        return visitName;
    }

    public void setVisitName(String visitName) {
        this.visitName = visitName;
    }

    public String getNursing() {
        return nursing;
    }

    public void setNursing(String nursing) {
        this.nursing = nursing;
    }

    public String getNutrition() {
        return nutrition;
    }

    public void setNutrition(String nutrition) {
        this.nutrition = nutrition;
    }

    public String getProcessing() {
        return processing;
    }

    public void setProcessing(String processing) {
        this.processing = processing;
    }

    public String getSetup() {
        return setup;
    }

    public void setSetup(String setup) {
        this.setup = setup;
    }

    public String getVisitType() {
        return visitType;
    }

    public void setVisitType(VisitType visitType) {
        this.visitType = visitType.getName();
    }

    public Integer getDuration() {
        return duration;
    }

    public void setDuration(Integer duration) {
        this.duration = duration;
    }

    public String getResourceName() {
        return resourceName;
    }

    public void setResourceName(String resourceName) {
        this.resourceName = resourceName;
    }

    @Override
    public String toString() {
        return "{\"visitId\":" + visitId + "," +
                "\"visitName\":\"" + visitName + "\"," +
                "\"nursing\":\"" + nursing + "\"," +
                "\"nutrition\":\"" + nutrition + "\"," +
                "\"processing\":\"" + processing + "\"," +
                "\"setup\":\"" + setup + "\"," +
                "\"duration\":" + duration + "," +
                "\"resourceName\":\"" + resourceName + "\"," +
                "\"studyName\":\"" + studyName + "\"," +
                "\"visitType\":\"" + visitType + "\"}";
    }

    @Override
    public String toCsvHeaders() {
        StringBuilder builder = new StringBuilder();
        builder.append("Visit Name,Visit Type,Local ID,Nursing,");
        builder.append("Nutrition,Processing,Setup,");

        for (int i=0; i<9; i++) {
            builder.append("Resource Name,Resource Duration,");
        }
        builder.append("Resource Name,Resource Duration");

        return builder.toString();
    }

    @Override
    public List<String> toCsvRows(List<?> dtoList) {
        List<String> result = Lists.newArrayList();
        result.add(toCsvHeaders() + "\n");

        Integer previousId = -1;
        List<String> columns = Lists.newArrayList();

        for (Object object: dtoList) {
            WorkloadAndResourceResponseDTO d = (WorkloadAndResourceResponseDTO) object;

            if (! d.visitId.equals(previousId)) {

                if (! previousId.equals(Integer.valueOf(-1))) {
                    columns = harvestNonEmptyColumns(columns, result);
                }
                previousId = d.visitId;

                columns.add(q(d.visitName));
                columns.add(q(d.visitType));
                columns.add(q(d.studyName));
                columns.add(q(d.nursing));
                columns.add(q(d.nutrition));
                columns.add(q(d.processing));
                columns.add(q(d.setup));
            }
            columns.add(q(d.resourceName));
            columns.add(q(formatMinsIntoDaysHrsMins(d.duration)));

        }
        harvestNonEmptyColumns(columns, result);

        return result;
    }
    List<String> harvestNonEmptyColumns(List<String> columns, List<String> output) {
        if (! columns.isEmpty()) {
            String rows = Joiner.on(",").join(columns);
            output.add(rows + "\n");
        }
        return Lists.newArrayList();
    }
}
