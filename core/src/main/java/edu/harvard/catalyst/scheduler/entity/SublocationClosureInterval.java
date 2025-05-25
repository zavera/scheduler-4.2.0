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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "sublocation_closure_interval")
public class SublocationClosureInterval extends BaseEntity
implements Serializable, TimeBoundedIdentityAble {

    private static final long serialVersionUID = 1536308073401789166L;

    private Date startTime;
    private Date endTime;
    private String reason;
    private Sublocation sublocation;

    @Deprecated
    public SublocationClosureInterval() {
        this(null, null, null, null);
    }

    public SublocationClosureInterval(final Date startTime, final Date endTime, final String reason, final Sublocation sublocation) {
        super(null);
        this.startTime = startTime;
        this.endTime = endTime;
        this.reason = reason;
        this.sublocation = sublocation;
    }

    @JoinColumn(name = "sublocation", referencedColumnName = "id")
    @ManyToOne(optional = false)
    public Sublocation getSublocation() {
        return sublocation;
    }

    public void setSublocation(final Sublocation sublocation) {
        this.sublocation = sublocation;
    }

    @Column(name = "start_time")
    @Basic(optional = false)
    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(final Date startTime) {
        this.startTime = startTime;
    }

    @Column(name = "end_time")
    @Basic(optional = false)
    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(final Date endTime) {
        this.endTime = endTime;
    }

    @Column(name = "reason")
    public String getReason() {
        return reason;
    }

    public void setReason(final String reason) {
        this.reason = reason;
    }

    @Override
    public String toString() {
        return "SublocationClosureInterval [id=" + id + ", getId()=" + getId() + "]";
    }

    @Override
    public TimeBoundedIdentity asTimeBoundedEntity() {
        return new TimeBoundedIdentity(startTime, endTime, id, 0 /* 'quantity' is N/A */);
    }

    static public List<TimeBoundedIdentity> toTimeBoundedIdentityList(List<SublocationClosureInterval> list) {

        List<TimeBoundedIdentity> result = new ArrayList<TimeBoundedIdentity>();
        if (list != null) {
            for (SublocationClosureInterval sublocationClosureInterval : list) {
                TimeBoundedIdentity timeBoundedIdentity = sublocationClosureInterval.asTimeBoundedEntity();
                result.add(timeBoundedIdentity);
            }
        }
        return result;
    }
}
