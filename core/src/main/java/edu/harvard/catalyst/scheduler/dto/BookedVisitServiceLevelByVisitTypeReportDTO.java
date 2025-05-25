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

public class BookedVisitServiceLevelByVisitTypeReportDTO implements CsvAbleDTO {

    private Integer visitId;
    private String visitName;
    private String visitType;
    private String nursing;
    private String nursinglevel;
    private String nutrition;
    private String processing;
    private String setup;
    private String nutritionlevel;
    private String processinglevel;
    private String setuplevel;
    
    public BookedVisitServiceLevelByVisitTypeReportDTO() {
    }

    
    public String getNursinglevel() {
        return nursinglevel;
    }


    public void setNursinglevel(String nursinglevel) {
        this.nursinglevel = nursinglevel;
    }


    public Integer getVisitId() {
        return visitId;
    }

    public void setVisitId(Integer visitId) {
        this.visitId = visitId;
    }

    public String getVisitName() {
        return visitName;
    }
    public void setVisitName(String visitName) {
        this.visitName = visitName;
    }

    public String getVisitType() {
        return visitType;
    }

    public void setVisitType(String visitType) {
        this.visitType = visitType;
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


    public String getNutritionlevel() {
        return nutritionlevel;
    }


    public void setNutritionlevel(String nutritionlevel) {
        this.nutritionlevel = nutritionlevel;
    }


    public String getProcessinglevel() {
        return processinglevel;
    }


    public void setProcessinglevel(String processinglevel) {
        this.processinglevel = processinglevel;
    }


    public String getSetuplevel() {
        return setuplevel;
    }


    public void setSetuplevel(String setuplevel) {
        this.setuplevel = setuplevel;
    }


    @Override
    public String toCsvHeaders() {
        return "Visit Type,Nursing,# of Visits,Nutrition,# of Visits," +
                "Processing,# of Visits,Set Up,# of Visits";
    }

    @Override
    public List<String> toCsvRows(List<?> dtoList) {

        List<String> result = Lists.newArrayList();
        result.add(toCsvHeaders() + "\n");

        for (Object object: dtoList) {
            BookedVisitServiceLevelByVisitTypeReportDTO d =
                    (BookedVisitServiceLevelByVisitTypeReportDTO) object;

            List<String> columns = Lists.newArrayList();

            columns.add(q(d.visitType));
            columns.add(q(d.nursinglevel));
            columns.add(q(d.nursing));
            columns.add(q(d.nutritionlevel));
            columns.add(q(d.nutrition));
            columns.add(q(d.processinglevel));
            columns.add(q(d.processing));
            columns.add(q(d.setuplevel));
            columns.add(q(d.setup));

            String rows = Joiner.on(",").join(columns);
            result.add(rows + "\n");
        }
        return result;
    }
}