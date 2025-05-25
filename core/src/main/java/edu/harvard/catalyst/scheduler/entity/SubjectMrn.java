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
@Table(name = "subject_mrn")
public class SubjectMrn extends BaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    private Subject subject;
    private String mrn;
    private String site;
    private String status;

    public SubjectMrn() {
        super(null);
    }

    public SubjectMrn(Subject subject, String mrn, String site, String status) {
        this(subject, mrn, site, status, null);
    }

    public SubjectMrn(Subject subject, String mrn, String site, String status, Integer id) {
        super(id);

        this.subject = subject;
        this.mrn = mrn;
        this.site = site;
        this.status = status;
    }

    /** make a defensive copy, so we can use the object without affecting the entity/changing the database at all*/
    public static SubjectMrn defensiveCopy(final SubjectMrn original) {

        Subject origSubject = original.getSubject();

        SubjectMrn result = new SubjectMrn(
                Subject.defensiveCopy(origSubject),
                original.getMrn(),
                original.getSite(),
                original.getStatus(),
                original.getId());

        return result;
    }

    @JoinColumn(name = "subject", referencedColumnName = "id")
    @ManyToOne(optional = false)
    public Subject getSubject() {
        return subject;
    }

    public void setSubject(Subject subject) {
        this.subject = subject;
    }

    @Column(name = "mrn")
    public String getMrn() {
        return mrn;
    }

    public void setMrn(String mrn) {
        this.mrn = mrn;
    }

    @Column(name = "site")
    public String getSite() {
        return site;
    }

    public void setSite(String site) {
        this.site = site;
    }

    @Column(name = "status")
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public int hashCode() {

        int result = getId() == null ? 0 : getId();
        result +=    subject == null ? 0 : subject.hashCode();
        result +=    mrn == null ? 0 : mrn.hashCode();
        result +=    site == null ? 0 : site.hashCode();
        result +=    status == null ? 0 : status.hashCode();

        return result;
    }

    @Override
    public boolean equals(final Object obj) {

        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final SubjectMrn other = (SubjectMrn) obj;

        boolean siteEquals = doSitesEqual(site, other.site);

        return siteEquals
                && subject.equals(other.subject)
                && mrn.equalsIgnoreCase(other.mrn)
                && (status != null && status.equalsIgnoreCase(other.status));
    }

    @Override
    public String toString() {
        return "Code: " + mrn + " Site: " + site + " Status: " + status;
    }

    private boolean doSitesEqual(String site1, String site2) {
        return (site1 == null ? site2 == null : site1.equalsIgnoreCase(site2));
    }
}
