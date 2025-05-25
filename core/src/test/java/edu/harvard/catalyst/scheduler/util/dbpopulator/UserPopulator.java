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
package edu.harvard.catalyst.scheduler.util.dbpopulator;

import edu.harvard.catalyst.scheduler.entity.Institution;
import edu.harvard.catalyst.scheduler.entity.InstitutionRole;
import edu.harvard.catalyst.scheduler.entity.Role;
import edu.harvard.catalyst.scheduler.entity.User;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by xavier on 8/2/17.
 */
public class UserPopulator {

    DbPopulator dataPopulator;

    public List<User> users = new ArrayList<>();

    public UserPopulator(DbPopulator dataPopulator) {

        this.dataPopulator = dataPopulator;

    }

    public User populateOne(
            int entityNumber,
            Institution institution,
            InstitutionRole institutionRole,
            Role role
    ) {

        User user = new User();
        user.setInstitution(institution);
        user.setInstitutionRole(institutionRole);
        user.setEcommonsId("eCommonsId-" + entityNumber);
        user.setPassword("password-" + entityNumber);
        user.setSalt("salt-" + entityNumber);
        user.setLastName("last name for user " + entityNumber);
        user.setFirstName("first name for user " + entityNumber);
        user.setRole(role);
        dataPopulator.dao.createEntity(user);
        this.users.add(user);

        return user;

    }

    public List<User> populateMany(
            int startingEntityNumber,
            int numberOfEntities,
            Institution institution,
            InstitutionRole institutionRole,
            Role role
    ) {

        List<User> newEntities = new ArrayList<>();

        for (int i = startingEntityNumber; i < startingEntityNumber + numberOfEntities; i++) {
            User user = populateOne(
                    i,
                    institution,
                    institutionRole,
                    role
            );
            newEntities.add(user);
        }

        return newEntities;

    }

    public User populateOneWithDependencies(
            int entityNumber
    ) {

        final Institution institution = dataPopulator.institutionPopulator.populateOne(1);
        final InstitutionRole institutionRole = dataPopulator.institutionRolePopulator.populateOne(1);
        final Role role = this.dataPopulator.rolePopulator.populateOne(1);

        return populateOne(
                entityNumber,
                institution,
                institutionRole,
                role
        );

    }
}
