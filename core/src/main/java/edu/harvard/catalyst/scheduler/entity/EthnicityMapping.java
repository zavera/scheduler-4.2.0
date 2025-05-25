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

@Entity
@Table(name = "ethnicity_mapping")
public class EthnicityMapping extends BaseEntity implements Serializable {
    private static final long serialVersionUID = 1L;
    private String externalEthnicityString;
    private Ethnicity subjectEthnicity;

    public EthnicityMapping() {
        super(null);
    }

    public EthnicityMapping(String externalEthnicityString, Ethnicity subjectEthnicity) {
        this.externalEthnicityString = externalEthnicityString;
        this.subjectEthnicity = subjectEthnicity;
    }

    @JoinColumn(name = "scheduler", referencedColumnName = "id")
    @ManyToOne(optional = false)
    public Ethnicity getSchedulerEthnicity() {
        return subjectEthnicity;
    }

    public void setSchedulerEthnicity(Ethnicity subjectEthnicity) {
        this.subjectEthnicity = subjectEthnicity;
    }

    @Column(name = "external")
    public String getExternalEthnicityString() {
        return externalEthnicityString;
    }

    public void setExternalEthnicityString(String empiEthnicityString) {
        this.externalEthnicityString = empiEthnicityString;
    }
}
