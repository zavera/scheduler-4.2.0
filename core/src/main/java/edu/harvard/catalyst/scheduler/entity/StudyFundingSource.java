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
package edu.harvard.catalyst.scheduler.entity;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name = "study_funding_source_cai")
public class StudyFundingSource extends BaseEntity implements Serializable {

    private static final long serialVersionUID = 6189764272430222862L;

    private Integer studyId; // use only ID to avoid Json circular references

    private FundingSource fundingSource;
    private CentersAndInstitutions centersAndInstitutions;

    private Integer oneToFour;
    private String comment;

    private String grantId;
    private String siteCostCenter;
    private Double totalDirectAward;
    private Double totalIndirectAward;
    private Date projectStartDate;
    private Date projectEndDate;

    @Deprecated
    public StudyFundingSource() {
        super(null);
    }

    public StudyFundingSource(Study study, FundingSource fundingSource, CentersAndInstitutions cai, Integer oneToFour, String comment,
                              String grantId, String siteCostCenter, Double totalDirectAward, Double totalIndirectAward,
                              Date projectStartDate, Date projectEndDate) {
        super(null);

        this.studyId = study.getId();

        this.fundingSource = fundingSource;
        this.centersAndInstitutions = cai;
        this.oneToFour = oneToFour;
        this.comment = comment;
        this.grantId = grantId;
        this.siteCostCenter = siteCostCenter;
        this.totalDirectAward = totalDirectAward;
        this.totalIndirectAward = totalIndirectAward;
        this.projectStartDate = projectStartDate;
        this.projectEndDate = projectEndDate;
    }

    @Column(name = "study")
    @Basic(optional = false)
    public Integer getStudyId() {
        return studyId;
    }
    public void setStudyId(Integer studyId) {
        this.studyId = studyId;
    }

    @ManyToOne(optional = true)
    @JoinColumn(name = "centers_and_institutions", referencedColumnName = "id")
    public CentersAndInstitutions getCentersAndInstitutions() {
        return centersAndInstitutions;
    }
    public void setCentersAndInstitutions(CentersAndInstitutions centersAndInstitutions) {
        this.centersAndInstitutions = centersAndInstitutions;
    }

    @ManyToOne(optional = false)
    @JoinColumn(name = "funding_source", referencedColumnName = "id")
    public FundingSource getFundingSource() {
        return fundingSource;
    }
    public void setFundingSource(FundingSource fundingSource) {
        this.fundingSource = fundingSource;
    }

    @Column(name = "one_to_four")
    @Basic(optional = false)
    public Integer getOneToFour() {
        return oneToFour;
    }
    public void setOneToFour(Integer oneToFour) {
        this.oneToFour = oneToFour;
    }

    @Column(name = "comment")
    @Basic(optional = true)
    public String getComment() {
        return comment;
    }
    public void setComment(String comment) {
        this.comment = comment;
    }

    @Column(name = "grant_id")
    @Basic(optional = true)
    public String getGrantId() {
        return grantId;
    }
    public void setGrantId(String grantId) {
        this.grantId = grantId;
    }

    @Column(name = "site_cost_center")
    @Basic(optional = true)
    public String getSiteCostCenter() {
        return siteCostCenter;
    }
    public void setSiteCostCenter(String siteCostCenter) {
        this.siteCostCenter = siteCostCenter;
    }

    @Column(name = "total_direct_award")
    @Basic(optional = true)
    public Double getTotalDirectAward() {
        return totalDirectAward;
    }
    public void setTotalDirectAward(Double totalDirectAward) {
        this.totalDirectAward = totalDirectAward;
    }

    @Column(name = "total_indirect_award")
    @Basic(optional = true)
    public Double getTotalIndirectAward() {
        return totalIndirectAward;
    }
    public void setTotalIndirectAward(Double totalIndirectAward) {
        this.totalIndirectAward = totalIndirectAward;
    }

    @Column(name = "project_start_date")
    @Basic(optional = true)
    public Date getProjectStartDate() {
        return projectStartDate;
    }
    public void setProjectStartDate(Date projectStartDate) {
        this.projectStartDate = projectStartDate;
    }

    @Column(name = "project_end_date")
    @Basic(optional = true)
    public Date getProjectEndDate() {
        return projectEndDate;
    }
    public void setProjectEndDate(Date projectEndDate) {
        this.projectEndDate = projectEndDate;
    }


    @Override
    public String toString() {
        return "StudyFundingSource [id=" + id + ", getId()=" + getId() + "]";
    }

}
