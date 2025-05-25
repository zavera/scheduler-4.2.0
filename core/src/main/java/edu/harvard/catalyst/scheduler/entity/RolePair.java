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

import com.google.common.collect.Maps;

import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: carl
 * Date: 2/2/15
 * Time: 7:12 PM
 */
public class RolePair {
    private RoleType userRole;
    private InstitutionRoleType institutionRole;

    public static Map<RolePair, Boolean> getRolePairMap() {
        return rolePairMap;
    }

    private static Map<RolePair, Boolean> rolePairMap = Maps.newHashMap();

    static {
        {
            rolePairMap.put(new RolePair(RoleType.ROLE_NURSE_MANAGER_DIRECTOR , InstitutionRoleType.ROLE_SUPER_ADMIN)     , true);
            rolePairMap.put(new RolePair(RoleType.ROLE_NUTRITION_MANAGER      , InstitutionRoleType.ROLE_SUPER_ADMIN)     , true);
            rolePairMap.put(new RolePair(RoleType.ROLE_ADMINISTRATIVE_DIRECTOR, InstitutionRoleType.ROLE_SUPER_ADMIN)     , true);
            rolePairMap.put(new RolePair(RoleType.ROLE_SCHEDULER              , InstitutionRoleType.ROLE_SUPER_ADMIN)     , true);
            rolePairMap.put(new RolePair(RoleType.ROLE_NUTRITION_MANAGER      , InstitutionRoleType.ROLE_RESOURCE_MANAGER), true);
            rolePairMap.put(new RolePair(RoleType.ROLE_NURSE_MANAGER_DIRECTOR , InstitutionRoleType.ROLE_RESOURCE_MANAGER), true);
            rolePairMap.put(new RolePair(RoleType.ROLE_SCHEDULER              , InstitutionRoleType.ROLE_SCHEDULER)       , true);
        }
    }

    public RolePair(RoleType userRole, InstitutionRoleType institutionRole) {
        this.userRole = userRole;
        this.institutionRole = institutionRole;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        RolePair rolePair = (RolePair) o;

        if (institutionRole != rolePair.institutionRole) {
            return false;
        }
        if (userRole != rolePair.userRole) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = userRole != null ? userRole.hashCode() : 0;
        result = 31 * result + (institutionRole != null ? institutionRole.hashCode() : 0);
        return result;
    }
}


