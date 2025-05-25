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
import edu.harvard.catalyst.scheduler.core.SchedulerRuntimeException;
import edu.harvard.catalyst.scheduler.dto.BooleanResultDTO;
import edu.harvard.catalyst.scheduler.dto.PasswordResetDTO;
import edu.harvard.catalyst.scheduler.dto.UserDTO;
import edu.harvard.catalyst.scheduler.dto.VersionInfo;
import edu.harvard.catalyst.scheduler.security.AuthorizedRoles;
import edu.harvard.catalyst.scheduler.service.AppService;
import edu.harvard.catalyst.scheduler.service.AuthService;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import javax.inject.Singleton;
import javax.ws.rs.*;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static edu.harvard.catalyst.scheduler.entity.InstitutionRoleType.ROLE_SUPER_ADMIN;

/**
 * @author Bill Simons
 * @date 5/1/13
 * @link http://cbmi.med.harvard.edu
 * @link http://chip.org
 */
@Path("/app")
@Singleton
@Component
public class AppResource extends BaseResource {
    private AppService appService;
    private AuthService authService;
    private String subjectMode;

    @Autowired
    public AppResource(AppService appService,
                       AuthService authService,
                       @Qualifier("subjectMode") String subjectMode) {
        this.appService = appService;
        this.authService = authService;
        this.subjectMode = subjectMode;
    }

    //Don't use - Needed for spring security cglib proxying
    AppResource() {
    }

    @GET
    @Path("/versionInfo")
    public String getVersionInfo() {
        VersionInfo versionInfo = new VersionInfo();
        InputStream propsFile = getClass().getClassLoader().getResourceAsStream("build-number.properties");
        Properties schedulerProps = new Properties();

        try {
            schedulerProps.load(propsFile);
        }
        catch (IOException ioe) {
            SchedulerRuntimeException.logAndThrow("Problem loading 'build-number.properties'", ioe);
        }

        versionInfo.setBuildDate(schedulerProps.getProperty("buildDate", "UNKNOWN"));
        versionInfo.setScmBranch(schedulerProps.getProperty("scmBranch", "UNKNOWN"));
        versionInfo.setScmRevision(schedulerProps.getProperty("scmRevision", "UNKNOWN"));
        versionInfo.setVersion(schedulerProps.getProperty("version", "UNKNOWN"));
        versionInfo.setSubjectMode(subjectMode);
        return gson.toJson(versionInfo);
    }

    @GET
    @Path("/getStaticLists")
    public String getStaticLists()  {
        return appService.getStaticLists();
    }

    @GET
    @Path("/getStaticDataForStandardReportPage")
    public String getStaticDataForStandardReportPage()  {
        return appService.getStaticDataForStandardReportPage();
    }

    @POST
    @Path("/registerUser")
    public String registerUser(@FormParam("data") String data)  {
        Map<String, String> map = new HashMap<String, String>();
        Gson gson = new GsonBuilder().registerTypeAdapter(UserDTO.class, new UserDTO()).create();
        UserDTO dto = gson.fromJson(data, UserDTO.class);
        dto = authService.doRegisterUser(dto, getRemoteHost());
        if(!dto.isResult()) {
            map.put("errorMsg", dto.getErrorMsg());
        }
        return (gson.toJson(dto));
    }

    @POST
    @Path("/sendRegistrationEmail")
    public String sendRegistrationEmail(@FormParam("data") String data)  {
        UserDTO dto = gson.fromJson(data, UserDTO.class);
        dto = authService.sendRegistrationEmail(dto, getContextPath(), getServerName(), getServerPort());
        return (gson.toJson(dto));
    }

    @POST
    @Path("/unAuthenticatedPasswordReset")
    public String unAuthenticatedPasswordReset(@FormParam("data") String data)  {
        PasswordResetDTO passwordResetDTO = gson.fromJson(data, PasswordResetDTO.class);
        boolean result = authService.doPasswordReset(passwordResetDTO);
        BooleanResultDTO booleanResultDTO = new BooleanResultDTO();
        booleanResultDTO.setResult(result);
        return (gson.toJson(booleanResultDTO));
    }

    private static final Logger log = Logger.getLogger(AppResource.class);
    @GET
    @Path("/logMessage")
    @AuthorizedRoles({ROLE_SUPER_ADMIN})
    public String logMessage(@QueryParam("m") String message)  {
        log.warn("\n===================> " + message);
        return message;
    }

}
