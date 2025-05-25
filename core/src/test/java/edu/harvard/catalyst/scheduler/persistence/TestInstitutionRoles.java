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
package edu.harvard.catalyst.scheduler.persistence;

import edu.harvard.catalyst.scheduler.entity.InstitutionRole;
import edu.harvard.catalyst.scheduler.entity.InstitutionRoleType;

/**
 * @author Bill Simons
 * @date 1/13/14
 * @link http://cbmi.med.harvard.edu
 * @link http://chip.org
 */
public class TestInstitutionRoles extends TestEntityPseudoEnum<InstitutionRole> {
    private static final TestInstitutionRoles Instance = new TestInstitutionRoles();

    public static final InstitutionRole SuperAdmin = Instance.register(new InstitutionRole(1, InstitutionRoleType.ROLE_SUPER_ADMIN, "desc", "Super Admin"));
    public static final InstitutionRole ResourceManager = Instance.register(new InstitutionRole(2, InstitutionRoleType.ROLE_RESOURCE_MANAGER, "desc", "Resource Manager"));
    public static final InstitutionRole Scheduler = Instance.register(new InstitutionRole(3, InstitutionRoleType.ROLE_SCHEDULER, "desc", "Schedulers"));
    public static final InstitutionRole StudyStaff = Instance.register(new InstitutionRole(4, InstitutionRoleType.ROLE_STUDY_STAFF, "desc", "Study Staff"));
    public static final InstitutionRole FrontDesk = Instance.register(new InstitutionRole(5, InstitutionRoleType.ROLE_FRONT_DESK, "desc", "Front Desk"));
    public static final InstitutionRole GeneralView = Instance.register(new InstitutionRole(6, InstitutionRoleType.ROLE_GENERAL_VIEW, "desc", "GeneralView"));

    public static final InstitutionRole[] values() {
        return Instance._values();
    }

    private TestInstitutionRoles() {
        super(new InstitutionRole[0]);
    }
}
