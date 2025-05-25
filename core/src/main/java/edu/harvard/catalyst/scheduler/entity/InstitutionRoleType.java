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

import com.google.common.base.Optional;
import com.google.common.collect.Maps;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.core.GrantedAuthority;

import java.util.Map;

/**
 * @author Bill Simons
 * @date 5/15/13
 * @link http://cbmi.med.harvard.edu
 * @link http://chip.org
 */
public enum InstitutionRoleType implements ConfigAttribute, GrantedAuthority, HasReportFiltersNameAndId {
    ROLE_SUPER_ADMIN("Super Admin"),
    ROLE_RESOURCE_MANAGER("Resource Manager"),
    ROLE_SCHEDULER("Schedulers"),
    ROLE_STUDY_STAFF("Study Staff"),
    ROLE_FRONT_DESK("Front Desk"),
    ROLE_GENERAL_VIEW("General View");

    private String reportFiltersName;
    
    @Override
    public int getReportFiltersId() {
        return ordinal();
    }
    @Override
    public String getReportFiltersName() {
        return reportFiltersName;
    }
    
    InstitutionRoleType(String reportFiltersName) {
        this.reportFiltersName = reportFiltersName;
    }
    
    public String getAttribute() {
        return toString();
    }

    public String getAuthority() {
        return toString();
    }
    
	public static Optional<InstitutionRoleType> fromHumanReadableName(final String humanReadableName) {
        if(humanReadableName == null) {
            return Optional.absent();
        }
        
        final String lowercaseName = humanReadableName.toLowerCase();
        
        return Optional.fromNullable(byHumanReadableName.get(lowercaseName));
    }

    //NB: Use a lookup table to find enum constants by their human-readable names.  We could give each enum constant
    //a human-readable-name field and scan through the values() array, but I opted to skip the linear scan every time,
    //at the expense of a (maybe) more fragile mapping. -Clint
    private static final Map<String, InstitutionRoleType> byHumanReadableName = makeByHumanReadableNameMap();

    private static Map<String, InstitutionRoleType> makeByHumanReadableNameMap() {
        final Map<String, InstitutionRoleType> result = Maps.newHashMap();
        
        result.put("super admin",      ROLE_SUPER_ADMIN);
        result.put("resource manager", ROLE_RESOURCE_MANAGER);
        result.put("schedulers",       ROLE_SCHEDULER);
        result.put("study staff",      ROLE_STUDY_STAFF);
        result.put("front desk",       ROLE_FRONT_DESK);
        result.put("general view",     ROLE_GENERAL_VIEW);
        
        return result;
    }
}
