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

import edu.harvard.catalyst.scheduler.util.SubjectDataEncryptor;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "study_subject")
public class StudySubject extends BaseEntity implements Serializable {
    private static final long serialVersionUID = 1L;
    private Study study;
    private SubjectMrn subjectMrn;
    private boolean active;

    public StudySubject() {
        super(null);
    }
    
    public StudySubject(Study study, SubjectMrn subjectMrn, boolean active) {
        super(null);

        this.study = study;
    	this.subjectMrn = subjectMrn;
    	this.active = active;
    }

    @JoinColumn(name = "study", referencedColumnName = "id")
    @ManyToOne(optional = false)
    public Study getStudy() {
        return study;
    }

    public void setStudy(Study study) {
        this.study = study;
    }

    @JoinColumn(name = "subject_mrn", referencedColumnName = "id")
    @ManyToOne(optional = false, cascade = { CascadeType.ALL })
    public SubjectMrn getSubjectMrn() {
        return subjectMrn;
    }

    public void setSubjectMrn(SubjectMrn subjectMrn) {
        this.subjectMrn = subjectMrn;
    }

    @Column(name = "active")
    public boolean getActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    @Transient
    public Subject getSubject() {
        return subjectMrn.getSubject();
    }
    public void setSubject(Subject subject) {
        subjectMrn.setSubject(subject);
    }
    @Transient
    public String getMrn() {
        return subjectMrn.getMrn();
    }
    @Transient
    public String getDecryptedMrn() {
        return SubjectDataEncryptor.decrypt(getMrn());
    }

    @Override
    public String toString() {
        return "StudySubject [id=" + id + ", getId()=" + getId() + "]";
    }
}
