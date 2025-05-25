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
@Table(name = "visit_template")
public class VisitTemplate extends BaseEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    private String name;
    private String shortName;
    private Boolean approved;
    private Boolean relativeTime;
    private String nursing;
    private String nutrition;
    private String processing;
    private String setup;
    private Sublocation sublocation;
    private Date createdDate;
    private VisitType visitType;
    private Study study;
    private Boolean institutionNonCRC;
    private Boolean nonInstitutionNonCRC;
    private Boolean researchPharmacy;
    private Boolean active;
    private Date lastUpdateTime;
    private Integer duration;
    private Boolean billable;

    private String comment;

    public VisitTemplate() {
        super(null);
    }

    public VisitTemplate(
            final Integer id,
            final String name,
            final String shortName,
            final Boolean approved,
            final Boolean relativeTime,
            final String nursing,
            final String nutrition,
            final String processing,
            final String setup,
            final Sublocation sublocation,
            final Date createdDate,
            final VisitType visitType,
            final Study study,
            final Boolean institutionNonCRC,
            final Boolean nonInstitutionNonCRC,
            final Boolean researchPharmacy,
            final Boolean active,
            final Date lastUpdateTime,
            final Integer duration,
            final Boolean billable)
    {
        super(id);
        this.name = name;
        this.shortName = shortName;
        this.approved = approved;
        this.relativeTime = relativeTime;
        this.nursing = nursing;
        this.nutrition = nutrition;
        this.processing = processing;
        this.setup = setup;
        this.sublocation = sublocation;
        this.createdDate = createdDate;
        this.visitType = visitType;
        this.study = study;
        this.institutionNonCRC = institutionNonCRC;
        this.nonInstitutionNonCRC = nonInstitutionNonCRC;
        this.researchPharmacy = researchPharmacy;
        this.active = active;
        this.lastUpdateTime = lastUpdateTime;
        this.duration = duration;
        this.billable = billable;
    }

    @Column(name = "name")
    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    @Column(name = "short_name")
    public String getShortName() {
        return shortName;
    }

    public void setShortName(final String shortName) {
        this.shortName = shortName;
    }

    @Column(name = "billable")
    public Boolean getBillable() {return billable;}

    public void setBillable(final Boolean billable) {
        this.billable = billable;
    }

    @Column(name = "approved")
    public Boolean getApproved() {
        return approved;
    }

    public void setApproved(final Boolean approved) {
        this.approved = approved;
    }

    @Column(name = "relative_time")
    public Boolean getRelativeTime() {
        return relativeTime;
    }

    public void setRelativeTime(final Boolean relativeTime) {
        this.relativeTime = relativeTime;
    }

    @Column(name = "duration")
    public Integer getDuration() {
        return duration;
    }

    public void setDuration(final Integer duration) {
        this.duration = duration;
    }

    @Column(name = "nursing")
    public String getNursing() {
        return nursing;
    }

    public void setNursing(final String nursing) {
        this.nursing = spaceIfNull(nursing);
    }

    private static String spaceIfNull(final String s) {
        return (s == null) ? " " : s;
    }

    @Column(name = "nutrition")
    public String getNutrition() {
        return nutrition;
    }

    public void setNutrition(final String nutrition) {
        this.nutrition = spaceIfNull(nutrition);
    }

    @Column(name = "processing")
    public String getProcessing() {
        return processing;
    }

    public void setProcessing(final String processing) {
        this.processing = spaceIfNull(processing);
    }

    @Column(name = "setup")
    public String getSetup() {
        return setup;
    }

    public void setSetup(final String setup) {
        this.setup = spaceIfNull(setup);
    }

    @JoinColumn(name = "sublocation", referencedColumnName = "id")
    @ManyToOne(optional = true)
    public Sublocation getSublocation() {
        return sublocation;
    }

    public void setSublocation(final Sublocation sublocation) {
        this.sublocation = sublocation;
    }

    @Column(name = "comment")
    public String getComment() {
        return comment;
    }

    public void setComment(final String comment) {
        this.comment = comment;
    }

    @Column(name = "created_date")
    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(final Date createdDate) {
        this.createdDate = createdDate;
    }

    @JoinColumn(name = "visit_type", referencedColumnName = "id")
    @ManyToOne(optional = false)
    @Basic(optional = false)
    public VisitType getVisitType() {
        return visitType;
    }
    public void setVisitType(final VisitType visitType) {
        this.visitType = visitType;
    }


    @JoinColumn(name = "study", referencedColumnName = "id")
    @ManyToOne(optional = true)
    public Study getStudy() {
        return study;
    }

    public void setStudy(final Study study) {
        this.study = study;
    }

    @Column(name = "institution_non_crc")
    public Boolean getInstitutionNonCRC() {
        return institutionNonCRC;
    }

    public void setInstitutionNonCRC(final Boolean institutionNonCRC) {
        this.institutionNonCRC = institutionNonCRC;
    }

    @Column(name = "non_institution_non_crc")
    public Boolean getNonInstitutionNonCRC() {
        return nonInstitutionNonCRC;
    }

    public void setNonInstitutionNonCRC(final Boolean nonInstitutionNonCRC) {
        this.nonInstitutionNonCRC = nonInstitutionNonCRC;
    }

    @Column(name = "research_pharmacy")
    public Boolean getResearchPharmacy() {
        return researchPharmacy;
    }

    public void setResearchPharmacy(final Boolean researchPharmacy) {
        this.researchPharmacy = researchPharmacy;
    }

    @Column(name = "active")
    public Boolean getActive() {
        return active;
    }

    public void setActive(final Boolean active) {
        this.active = active;
    }

    @Column(name = "last_update_time")
    public Date getLastUpdateTime() {
        return lastUpdateTime;
    }
    public void setLastUpdateTime(final Date lastUpdateTime) {
        this.lastUpdateTime = lastUpdateTime;
    }

    @Transient
    public String getApprovedString() {
        if (approved != null && approved.booleanValue()) {
            return "Approved";
        }
        
        return "Not Approved";
    }

    @Override
    public String toString() {
        return "VisitTemplate [id=" + id + ", getId()=" + getId() + "]";
    }

    static public class VisitTemplateBuilder {
        private String name;
        private String shortName;
        private Boolean approved;
        private Boolean relativeTime;
        private String nursing;
        private String nutrition;
        private String processing;
        private String setup;
        private Sublocation sublocation;
        private Date createdDate;
        private VisitType visitType;
        private Study study;
        private Boolean institutionNonCRC;
        private Boolean nonInstitutionNonCRC;
        private Boolean researchPharmacy;
        private Boolean active;
        private Date lastUpdateTime;
        private Integer duration;
        private Boolean billable;
        private Integer id;

        public VisitTemplateBuilder id(final Integer id) {
            this.id = id;
            return this;
        }
        
        public VisitTemplateBuilder name(final String name) {
            this.name = name;
            return this;
        }

        public VisitTemplateBuilder shortName(final String shortName) {
            this.shortName = shortName;
            return this;
        }

        public VisitTemplateBuilder approved(final Boolean approved) {
            this.approved = approved;
            return this;
        }

        public VisitTemplateBuilder relativeTime(final Boolean relativeTime) {
            this.relativeTime = relativeTime;
            return this;
        }

        public VisitTemplateBuilder nursing(final String nursing) {
            this.nursing = nursing;
            return this;
        }

        public VisitTemplateBuilder nutrition(final String nutrition) {
            this.nutrition = nutrition;
            return this;
        }

        public VisitTemplateBuilder processing(final String processing) {
            this.processing = processing;
            return this;
        }

        public VisitTemplateBuilder setup(final String setup) {
            this.setup = setup;
            return this;
        }

        public VisitTemplateBuilder sublocation(final Sublocation sublocation) {
            this.sublocation = sublocation;
            return this;
        }

        public VisitTemplateBuilder createdDate(final Date createdDate) {
            this.createdDate = createdDate;
            return this;
        }

        public VisitTemplateBuilder visitType(final VisitType visitType) {
            this.visitType = visitType;
            return this;
        }

        public VisitTemplateBuilder study(final Study study) {
            this.study = study;
            return this;
        }

        public VisitTemplateBuilder institutionNonCRC(final Boolean institutionNonCRC) {
            this.institutionNonCRC = institutionNonCRC;
            return this;
        }

        public VisitTemplateBuilder nonInstitutionNonCRC(final Boolean nonInstitutionNonCRC) {
            this.nonInstitutionNonCRC = nonInstitutionNonCRC;
            return this;
        }

        public VisitTemplateBuilder researchPharmacy(final Boolean researchPharmacy) {
            this.researchPharmacy = researchPharmacy;
            return this;
        }

        public VisitTemplateBuilder active(final Boolean active) {
            this.active = active;
            return this;
        }

        public VisitTemplateBuilder lastUpdateTime(final Date lastUpdateTime) {
            this.lastUpdateTime = lastUpdateTime;
            return this;
        }

        public VisitTemplateBuilder duration(final Integer duration) {
            this.duration = duration;
            return this;
        }

        public VisitTemplateBuilder billable(final Boolean billable) {
            this.billable = billable;
            return this;
        }

        public VisitTemplate create() {
            return new VisitTemplate(
                    id,
                    name,
                    shortName,
                    approved,
                    relativeTime,
                    nursing,
                    nutrition,
                    processing,
                    setup,
                    sublocation,
                    createdDate,
                    visitType,
                    study,
                    institutionNonCRC,
                    nonInstitutionNonCRC,
                    researchPharmacy,
                    active,
                    lastUpdateTime,
                    duration,
                    billable);
        }
    }
}
