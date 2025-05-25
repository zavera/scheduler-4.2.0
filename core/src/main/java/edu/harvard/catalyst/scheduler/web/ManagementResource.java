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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import edu.harvard.catalyst.scheduler.dto.BooleanResultDTO;
import edu.harvard.catalyst.scheduler.dto.SearchDTO;
import edu.harvard.catalyst.scheduler.dto.UserDTO;
import edu.harvard.catalyst.scheduler.dto.response.GetUsersResponse;
import edu.harvard.catalyst.scheduler.dto.response.UserDataResponse;
import edu.harvard.catalyst.scheduler.dto.response.UserDetailResponse;
import edu.harvard.catalyst.scheduler.entity.Role;
import edu.harvard.catalyst.scheduler.security.AuthorizedRoles;
import edu.harvard.catalyst.scheduler.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.inject.Singleton;
import javax.ws.rs.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static edu.harvard.catalyst.scheduler.entity.InstitutionRoleType.*;


/**
 * @author Bill Simons
 * @date 4/15/13
 * @link http://cbmi.med.harvard.edu
 * @link http://chip.org
 */

@Path("/management")
@Singleton
@Component
public class ManagementResource extends SecuredResource {

    private AuthService service;

    @Autowired
    public ManagementResource(final AuthService service) {
        this.service = service;
    }

    //Don't use - Needed for spring security cglib proxying
    ManagementResource() {
    }

    @GET
    @Path("/getUsers")
    @AuthorizedRoles(ROLE_SUPER_ADMIN)
    public String getUsers(@QueryParam("sortBy") final String sortBy,
                           @QueryParam("orderBy") final String orderBy,
                           @QueryParam("page") final int page,
                           @QueryParam("maxResults") final int maxResults,
                           @QueryParam("search") final String searchJson)  {

        SearchDTO searchDTO = gson.fromJson(searchJson, SearchDTO.class);
        final GetUsersResponse users = service.getUsers(
                searchDTO,
                sortBy,
                orderBy,
                page,
                maxResults,
                getRemoteHost(),
                getUser());
        return gson.toJson(users);
    }

    @GET
    @Path("/getRolesList")
    @AuthorizedRoles(ROLE_SUPER_ADMIN)
    public String getRolesList()  {
        final List<Role> roles = service.getRoles();
        return gson.toJson(roles);
    }

    @POST
    @Path("/updatePassword")
    @AuthorizedRoles({ROLE_SUPER_ADMIN, ROLE_RESOURCE_MANAGER, ROLE_SCHEDULER, ROLE_STUDY_STAFF, ROLE_FRONT_DESK, ROLE_GENERAL_VIEW})
    public String updatePassword(@FormParam("data") final String data)  {
        final Gson gson = new GsonBuilder().registerTypeAdapter(UserDTO.class, new UserDTO()).create();
        UserDTO dto = gson.fromJson(data, UserDTO.class);
        dto = service.updatePassword(dto, getUser());
        return gson.toJson(dto);
    }

    @POST
    @Path("/createUser")
    @AuthorizedRoles(ROLE_SUPER_ADMIN)
    public String createUser(@FormParam("data") final String data)  {
        final Map<String, String> map = new HashMap<String, String>();
        final Gson gson = new GsonBuilder().registerTypeAdapter(UserDTO.class, new UserDTO()).create();
        UserDTO dto = gson.fromJson(data, UserDTO.class);
        dto = service.createUser(dto, getUser(), getContextPath(), getRemoteHost(), getServerName(), getServerPort());
        if(!dto.isResult()) {
            //TODO: XXX what is this for?  It just mutates a variable local to this method that's not used after this line. :\
            map.put("errorMsg", dto.getErrorMsg());
        }
        return gson.toJson(dto);
    }

    @POST
    @Path("/updateUser")
    @AuthorizedRoles(ROLE_SUPER_ADMIN)
    public String updateUser(@FormParam("data") final String data)  {
        final Map<String, String> map = new HashMap<>();
        
        final Gson gson = new GsonBuilder().registerTypeAdapter(UserDTO.class, new UserDTO()).create();
        
        final UserDTO unmarshalled = gson.fromJson(data, UserDTO.class);
        
        final UserDTO dto = service.updateUser(unmarshalled, getUser(), getRemoteHost());
        
        if(!dto.isResult()) {
            //TODO: XXX What is the point of the Map?  It never leaves this method.
            map.put("errorMsg", dto.getErrorMsg());
        }
        
        return gson.toJson(dto);
    }

    @GET
    @Path("/changeUserStatus")
    @AuthorizedRoles(ROLE_SUPER_ADMIN)
    public String changeUserStatus(@QueryParam("user") final int userId)  {
        final BooleanResultDTO result = service.changeUserStatus(userId, getUser(), getContextPath(), getRemoteHost(), getServerName(), getServerPort());
        return gson.toJson(result);
    }

    @GET
    @Path("/getUserData")
    @AuthorizedRoles(ROLE_SUPER_ADMIN)
    public String getUserData(@QueryParam("user") final int userId)  {
        final UserDetailResponse result = service.getUserData(userId);
        return gson.toJson(result);
    }

    // used by study_member.html page for listing users to add to a study
    @GET
    @Path("/getStudyMembersStudyScreen")
    @AuthorizedRoles({ROLE_SUPER_ADMIN, ROLE_RESOURCE_MANAGER, ROLE_SCHEDULER, ROLE_STUDY_STAFF, ROLE_FRONT_DESK})
    public String getStudyMembersStudyScreen(@QueryParam("filterString") final String filterString, @QueryParam("sortBy") final String sortBy, @QueryParam("orderBy") final String orderBy)  {
        final List<UserDataResponse> users = service.getStudyMembers(filterString, sortBy, orderBy);
        return gson.toJson(users);
    }

}
