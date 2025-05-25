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

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.io.Serializable;

@Entity
@Table(name = "appointment_status")
public class AppointmentStatus extends BaseEntity implements Serializable {

    private static final long serialVersionUID = -3120814057132914220L;
    
    private String name;

    private boolean isCancelled;
    private boolean isCheckedIn;
    private boolean isCheckedOut;
    private boolean isHold;
    private boolean isScheduled;
    private boolean isOpen;
    private boolean isActive;
    private boolean isServiced;
    private boolean isServiceable;

    @Deprecated
    public AppointmentStatus() {
        super(null);
        name = null;
    }

    // Setting the ID can be useful when writing unit tests and not persisting
    // entities
    public AppointmentStatus(final Integer id, final String name) {

        super(id);
        this.name = name;

    }

    public AppointmentStatus(final String name) {

        super(null);
        this.name = name;

    }

    @Column(name = "name")
    @Basic(optional = false)  
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    @Column(name = "is_cancelled")
    @Basic(optional = false)
    public boolean getIsCancelled() {
        return isCancelled;
    }
    public void setIsCancelled(boolean isCancelled) {
        this.isCancelled = isCancelled;
    }

    @Column(name = "is_checked_in")
    @Basic(optional = false)
    public boolean getIsCheckedIn() {
        return isCheckedIn;
    }
    public void setIsCheckedIn(boolean isCheckedIn) {
        this.isCheckedIn = isCheckedIn;
    }

    @Column(name = "is_checked_out")
    @Basic(optional = false)
    public boolean getIsCheckedOut() {
        return isCheckedOut;
    }
    public void setIsCheckedOut(boolean isCheckedOut) {
        this.isCheckedOut = isCheckedOut;
    }

    @Column(name = "is_hold")
    @Basic(optional = false)
    public boolean getIsHold() {
        return isHold;
    }
    public void setIsHold(boolean isHold) {
        this.isHold = isHold;
    }

    @Column(name = "is_scheduled")
    @Basic(optional = false)
    public boolean getIsScheduled() {
        return isScheduled;
    }
    public void setIsScheduled(boolean isScheduled) {
        this.isScheduled = isScheduled;
    }

    @Column(name = "is_open")
    @Basic(optional = false)
    public boolean getIsOpen() {
        return isOpen;
    }
    public void setIsOpen(boolean isOpen) {
        this.isOpen = isOpen;
    }

    @Column(name = "is_active")
    @Basic(optional = false)
    public boolean getIsActive() {
        return isActive;
    }
    public void setIsActive(boolean isActive) {
        this.isActive = isActive;
    }

    @Column(name = "is_serviced")
    @Basic(optional = false)
    public boolean getIsServiced() {
        return isServiced;
    }
    public void setIsServiced(boolean isServiced) {
        this.isServiced = isServiced;
    }

    @Column(name = "is_serviceable")
    @Basic(optional = false)
    public boolean getIsServiceable() {
        return isServiceable;
    }
    public void setIsServiceable(boolean isServiceable) {
        this.isServiceable = isServiceable;
    }

    @Override
    public String toString() {
        return "AppointmentStatus [id=" + id + ", getId()=" + getId() + "]";
    }

}
