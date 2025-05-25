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
@Table(name = "comments")
public class Comments extends BaseEntity implements Serializable {

    private static final long serialVersionUID = 925242937038265036L;
    private String comment;
    private VisitTemplate visitTemplate;
    private BookedVisit bookedVisit;
    private User user;
    private Date date;

    public Comments() {
        super(null);
    }

    @Column(name = "comment")
    @Basic(optional = false)  
    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
    
    @JoinColumn(name = "visit_template", referencedColumnName = "id")
    @ManyToOne(optional = true)
    public VisitTemplate getVisitTemplate() {
        return visitTemplate;
    }

    public void setVisitTemplate(VisitTemplate visitTemplate) {
        this.visitTemplate = visitTemplate;
    }

    @JoinColumn(name = "booked_visit", referencedColumnName = "id")
    @ManyToOne(optional = true)
    public BookedVisit getBookedVisit() {
        return bookedVisit;
    }

    public void setBookedVisit(BookedVisit bookedVisit) {
        this.bookedVisit = bookedVisit;
    }
    
    @JoinColumn(name = "user", referencedColumnName = "id")
    @ManyToOne(optional = false)
    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
    
    @Column(name = "date")
    public Date getDate() {
        return date;
    }
    public void setDate(Date date) {
        this.date = date;
    }

    @Override
    public String toString() {
        return "Comments [id=" + id + ", getId()=" + getId() + "]";
    }
}
