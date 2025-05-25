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
@Table(name = "institution_role")
public class InstitutionRole extends BaseEntity implements Serializable, HasName {

    private static final long serialVersionUID = -6519424469305426378L;
    private InstitutionRoleType type;
    private String description;
    private String name;

    public InstitutionRole() {
        super(null);
    }
    
    //NB: Can't be an instance method or Hibernate will treat it as a column. :(
    public static boolean isStudyStaff(final InstitutionRole role) {
        //NB: Corresponds to id = 4 in insitution_role table (from data/src/main/sql/base.sql) 
        return role.type == InstitutionRoleType.ROLE_STUDY_STAFF;
    }

    public InstitutionRole(final Integer id, final InstitutionRoleType type, final String description, final String name) {
        super(id);
        this.type = type;
        this.description = description;
        this.name = name;
    }

    @Column(name = "type")
    @Basic(optional = false)
    @Enumerated(EnumType.STRING)
    public InstitutionRoleType getType() {
        return type;
    }

    public void setType(final InstitutionRoleType type) {
        this.type = type;
    }

    @Override
    @Column(name = "name")
    @Basic(optional = false)
    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    @Column(name = "description")
    @Basic(optional = false)
    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return "InstitutionRole [id=" + id + ", getId()=" + getId() + "]";
    }

}
