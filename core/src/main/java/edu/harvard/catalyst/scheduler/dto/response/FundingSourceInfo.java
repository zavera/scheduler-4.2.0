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

import edu.harvard.catalyst.scheduler.entity.StudyFundingSource;

import java.util.Date;

/**
 * Created with IntelliJ IDEA.
 * User: carl
 * Date: 8/18/14
 * Time: 4:23 PM
 */
public class FundingSourceInfo {

    private Integer id;
    private String name;
    private String comment;
    private Integer oneToFour;

    private String grant;
    private String center;
    private Double directAward;
    private Double indirectAward;

    private Date start;
    private Date end;

    private Integer centerAndInstitutionId;
    private String centerAndInstitutionName;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public Integer getOneToFour() {
        return oneToFour;
    }

    public void setOneToFour(Integer oneToFour) {
        this.oneToFour = oneToFour;
    }

    public String getGrant() {
        return grant;
    }

    public void setGrant(String grant) {
        this.grant = grant;
    }

    public String getCenter() {
        return center;
    }

    public void setCenter(String center) {
        this.center = center;
    }

    public Double getDirectAward() {
        return directAward;
    }

    public void setDirectAward(Double directAward) {
        this.directAward = directAward;
    }

    public Double getIndirectAward() {
        return indirectAward;
    }

    public void setIndirectAward(Double indirectAward) {
        this.indirectAward = indirectAward;
    }

    public Date getStart() {
        return start;
    }

    public void setStart(Date start) {
        this.start = start;
    }

    public Date getEnd() {
        return end;
    }

    public void setEnd(Date end) {
        this.end = end;
    }

    public Integer getCenterAndInstitutionId() {
        return centerAndInstitutionId;
    }

    public void setCenterAndInstitutionId(Integer centerAndInstitutionId) {
        this.centerAndInstitutionId = centerAndInstitutionId;
    }

    public String getCenterAndInstitutionName() {
        return centerAndInstitutionName;
    }

    public void setCenterAndInstitutionName(String centerAndInstitutionName) {
        this.centerAndInstitutionName = centerAndInstitutionName;
    }

    public static FundingSourceInfo makeBlankFundingSourceInfo() {
        FundingSourceInfo fsi = new FundingSourceInfo();

        fsi.setName("");
        fsi.setCenterAndInstitutionName("");
        fsi.setGrant("");
        fsi.setCenter("");
        fsi.setDirectAward(null);
        fsi.setIndirectAward(null);
        fsi.setStart(null);
        fsi.setEnd(null);
        fsi.setComment("");

        return fsi;
    }

    public FundingSourceInfo() {
    }

    public FundingSourceInfo(StudyFundingSource sfs) {

        this(   sfs.getId(),
                sfs.getFundingSource().getName(),
                sfs.getComment(),
                sfs.getOneToFour(),

                sfs.getGrantId(),
                sfs.getSiteCostCenter(),
                sfs.getTotalDirectAward(),
                sfs.getTotalIndirectAward(),
                sfs.getProjectStartDate(),
                sfs.getProjectEndDate(),
                sfs.getCentersAndInstitutions() == null ? null : sfs.getCentersAndInstitutions().getId(),
                sfs.getCentersAndInstitutions() == null ? null : sfs.getCentersAndInstitutions().getName()
                );
    }

    public FundingSourceInfo(Integer id, String name, String comment, Integer oneToFour,
                             String grant, String center, Double direct, Double indirect,
                             Date start, Date end, Integer centerAndInstitutionId, String centerAndInstitutionName) {
        this.id = id;
        this.name = name;
        this.comment = comment;
        this.oneToFour = oneToFour;

        this.grant = grant;
        this.center = center;
        this.directAward = direct;
        this.indirectAward = indirect;

        this.start = start;
        this.end = end;

        this.centerAndInstitutionId = centerAndInstitutionId;
        this.centerAndInstitutionName = centerAndInstitutionName;
    }
}
