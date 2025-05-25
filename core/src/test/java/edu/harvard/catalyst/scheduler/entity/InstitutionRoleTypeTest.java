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

import org.junit.Test;

import com.google.common.base.Optional;

import junit.framework.TestCase;

import static edu.harvard.catalyst.scheduler.entity.InstitutionRoleType.ROLE_FRONT_DESK;
import static edu.harvard.catalyst.scheduler.entity.InstitutionRoleType.ROLE_GENERAL_VIEW;
import static edu.harvard.catalyst.scheduler.entity.InstitutionRoleType.ROLE_RESOURCE_MANAGER;
import static edu.harvard.catalyst.scheduler.entity.InstitutionRoleType.ROLE_SCHEDULER;
import static edu.harvard.catalyst.scheduler.entity.InstitutionRoleType.ROLE_STUDY_STAFF;
import static edu.harvard.catalyst.scheduler.entity.InstitutionRoleType.ROLE_SUPER_ADMIN;

/**
 * 
 * @author clint
 * @date Jul 31, 2013
 *
 */
public final class InstitutionRoleTypeTest extends TestCase {

    @Test
    public void testGetAttributeAndGetAuthority() {
        for(final InstitutionRoleType institutionRoleType : InstitutionRoleType.values()) {
            assertEquals(institutionRoleType.toString(), institutionRoleType.getAttribute());
            assertEquals(institutionRoleType.toString(), institutionRoleType.getAuthority());
            
            assertEquals(institutionRoleType.name(), institutionRoleType.getAttribute());
            assertEquals(institutionRoleType.name(), institutionRoleType.getAuthority());
        }
    }
    
    @Test
    public void testFromHumanReadableName() {
        assertEquals(Optional.absent(), InstitutionRoleType.fromHumanReadableName(null));
        assertEquals(Optional.absent(), InstitutionRoleType.fromHumanReadableName(""));
        assertEquals(Optional.absent(), InstitutionRoleType.fromHumanReadableName("salkdalksd"));
        
        assertEquals(ROLE_FRONT_DESK, InstitutionRoleType.fromHumanReadableName("front desk").get());
        assertEquals(ROLE_GENERAL_VIEW, InstitutionRoleType.fromHumanReadableName("general view").get());
        assertEquals(ROLE_RESOURCE_MANAGER, InstitutionRoleType.fromHumanReadableName("resource manager").get());
        assertEquals(ROLE_SCHEDULER, InstitutionRoleType.fromHumanReadableName("schedulers").get());
        assertEquals(ROLE_STUDY_STAFF, InstitutionRoleType.fromHumanReadableName("study staff").get());
        assertEquals(ROLE_SUPER_ADMIN, InstitutionRoleType.fromHumanReadableName("super admin").get());
    }
}
