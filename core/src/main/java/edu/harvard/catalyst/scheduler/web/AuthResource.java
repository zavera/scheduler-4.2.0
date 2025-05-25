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
package edu.harvard.catalyst.scheduler.web;

import edu.harvard.catalyst.scheduler.entity.User;
import edu.harvard.catalyst.scheduler.security.AuthorizedRoles;
import edu.harvard.catalyst.scheduler.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.inject.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import java.util.Date;

import static edu.harvard.catalyst.scheduler.entity.InstitutionRoleType.*;


/**
 * @author Bill Simons
 * @date 4/15/13
 * @link http://cbmi.med.harvard.edu
 * @link http://chip.org
 */

@Path("/auth")
@Singleton
@Component
public class AuthResource extends SecuredResource {
    private AuthService service;

    @Autowired
    public AuthResource(AuthService service) {
        this.service = service;
    }

    //Don't use - Needed for spring security cglib proxying
    AuthResource() {
    }

    @GET
    @Path("/getUserData")
    @AuthorizedRoles({ROLE_SUPER_ADMIN, ROLE_RESOURCE_MANAGER, ROLE_SCHEDULER, ROLE_STUDY_STAFF, ROLE_FRONT_DESK, ROLE_GENERAL_VIEW})
    public String getUserData()  {
        User user = getUser();
        user.setBookedVisits(null);
        return (gson.toJson(user));
    }

    @POST
    @Path("/pingSession")
    @AuthorizedRoles({ROLE_SUPER_ADMIN, ROLE_RESOURCE_MANAGER, ROLE_SCHEDULER, ROLE_STUDY_STAFF, ROLE_FRONT_DESK, ROLE_GENERAL_VIEW})
    public String pingSession()  {
        long time = request.getSession().getLastAccessedTime();
        Date lastTime = new Date(time);
        return "OK";
    }
}
