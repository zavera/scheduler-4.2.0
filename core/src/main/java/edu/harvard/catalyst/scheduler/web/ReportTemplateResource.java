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

import com.google.gson.*;
import edu.harvard.catalyst.scheduler.dto.BooleanResultDTO;
import edu.harvard.catalyst.scheduler.dto.request.ReportTemplateCreateUsersDTO;
import edu.harvard.catalyst.scheduler.dto.request.ReportTemplateRequestDTO;
import edu.harvard.catalyst.scheduler.dto.response.ReportTemplateDTO;
import edu.harvard.catalyst.scheduler.dto.response.ReportTemplateMetadataDTO;
import edu.harvard.catalyst.scheduler.entity.reporttemplate.Field;
import edu.harvard.catalyst.scheduler.security.AuthorizedRoles;
import edu.harvard.catalyst.scheduler.service.ReportTemplateService;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.inject.Singleton;
import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import java.lang.reflect.Type;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static edu.harvard.catalyst.scheduler.entity.InstitutionRoleType.ROLE_SUPER_ADMIN;

/**
 * @author Bill Simons
 * @date 4/15/13
 * @link http://cbmi.med.harvard.edu
 * @link http://chip.org
 */
@Path("/reports")
@Singleton
@Component
public class ReportTemplateResource extends SecuredResource {
    
    private static final Logger LOGGER = Logger.getLogger(ReportTemplateResource.class);

    private final ReportTemplateService reportTemplateService;
    protected final Gson gson;

    @Autowired
    public ReportTemplateResource(final ReportTemplateService reportTemplateService) {

        this.reportTemplateService = reportTemplateService;
        
        final GsonBuilder builder = new GsonBuilder();

        // Register an adapter to manage the date types as long values
        builder.registerTypeAdapter(Date.class, new JsonDeserializer<Date>() {
            @Override
            public Date deserialize(final JsonElement json, final Type typeOfT, final JsonDeserializationContext context) {
                return new Date(json.getAsJsonPrimitive().getAsLong());
            }
        });

        gson = builder.setExclusionStrategies(new ExclusionStrategy() {
            @Override
            public boolean shouldSkipClass(final Class<?> clazz) {
                return false;
            }

            @Override
            public boolean shouldSkipField(final FieldAttributes f) {
                return false;
            }

        }).create();
    }

    @Deprecated
    //Don't use - Needed for spring cglib proxying
    ReportTemplateResource() {
        this(null);
    }

    @POST
    @Path("/templates/{id}/{type}/{templateUserId}/results")
    @AuthorizedRoles({ROLE_SUPER_ADMIN})
    public Response runReportTemplate(
            @FormParam("data") final String data,
            @PathParam("id") final Integer id,
            @PathParam("type") final String type,
            @PathParam("templateUserId") final Integer templateUserId) {

        final ReportTemplateRequestDTO reportTemplateRequestDTO = gson.fromJson(data, ReportTemplateRequestDTO.class);

        final StreamingOutput streamingOutput = output -> reportTemplateService.runReportTemplate(getUser(), getRemoteHost(), id, reportTemplateRequestDTO, output, null);

        String reportName = reportTemplateService.getTemplateName(id, type, templateUserId).replaceAll("\\s","_").toLowerCase();
        final String fileName = reportName + ".csv";

        final Response response = Response.ok(streamingOutput).header("Content-Disposition", "attachment; filename=" + fileName).build();
        final String headerFilename = ((String)response.getMetadata().get("Content-Disposition").get(0)).replaceAll("attachment;", "");

        LOGGER.info("\n--> csv download filename is <" + headerFilename + ">");

        return response;
    }

    @POST
    @Path("/templates/{id}/create-users-report")
    @AuthorizedRoles({ROLE_SUPER_ADMIN})
    public String createUsersReport(
            @FormParam("data") final String data,
            @PathParam("id") final Integer id) {

        final ReportTemplateCreateUsersDTO reportTemplateCreateUsersDTO = gson.fromJson(data, ReportTemplateCreateUsersDTO.class);
        ReportTemplateMetadataDTO result = reportTemplateService.createUserReport(getUser(), reportTemplateCreateUsersDTO, id);
        return gson.toJson(result);
    }

    @POST
    @Path("/templates/{id}/update-users-report")
    @AuthorizedRoles({ROLE_SUPER_ADMIN})
    public String updateUsersReport(
            @FormParam("data") final String data,
            @PathParam("id") final Integer id) {

        final ReportTemplateCreateUsersDTO reportTemplateCreateUsersDTO = gson.fromJson(data, ReportTemplateCreateUsersDTO.class);
        ReportTemplateMetadataDTO result = reportTemplateService.updateUserReport(getUser(), reportTemplateCreateUsersDTO, id);
        return gson.toJson(result);
    }

    @GET
    @Path("/templates")
    @AuthorizedRoles({ROLE_SUPER_ADMIN})
    public String getReportTemplateList() {
        final List<ReportTemplateMetadataDTO> reportMetadataDTOs = reportTemplateService.getReportTemplateList(getUser());
        return gson.toJson(reportMetadataDTOs);
    }

    @GET
    @Path("/templates/{id}")
    @AuthorizedRoles({ROLE_SUPER_ADMIN})
    public String getReportTemplate(@PathParam("id") final Integer id) {
        final ReportTemplateDTO reportTemplateDTO = reportTemplateService.getReportTemplate(id);
        return gson.toJson(reportTemplateDTO);
    }

    @GET
    @Path("/templates/users/{id}")
    @AuthorizedRoles({ROLE_SUPER_ADMIN})
    public String getUsersReport(@PathParam("id") final Integer id) {
        final ReportTemplateDTO reportTemplateDTO = reportTemplateService.getUsersReport(id);
        return gson.toJson(reportTemplateDTO);
    }

    @POST
    @Path("/templates/users/delete/{id}")
    @AuthorizedRoles({ROLE_SUPER_ADMIN})
    public String deleteUsersReport(
            @PathParam("id") final Integer id) {

        BooleanResultDTO result = reportTemplateService.deleteUsersReport(id);
        return gson.toJson(result);
    }

    @GET
    @Path("/sortSavedTemplates/{id}")
    @AuthorizedRoles({ROLE_SUPER_ADMIN})
    public String sortSavedTemplates(@PathParam("id") final Integer id) {
        final List<ReportTemplateMetadataDTO> reportMetadataDTOs = reportTemplateService.sortSavedReportTemplateList(getUser(), id);
        return gson.toJson(reportMetadataDTOs);
    }

    @GET
    @Path("/getStaticLists/{id}/{columnName}")
    @AuthorizedRoles({ROLE_SUPER_ADMIN})
    public String getStaticLists(@PathParam("id") final Integer id,
                                 @PathParam("columnName") final String columnName) {
        final Map<String, List<?>> map = new HashMap<>();
        map.put("staticList", reportTemplateService.getStaticListByTcf(id, columnName));
        return gson.toJson(map);
    }

    @GET
    @Path("/getListsByEnumField/{id}")
    @AuthorizedRoles({ROLE_SUPER_ADMIN})
    public String getListsByEnumField(@PathParam("id") final Integer id) {
        final Map<String, List<Field.HasReportFiltersNameAndIdPair>> map = new HashMap<>();
        map.put("staticList", reportTemplateService.getEnumListByTcf(id));
        return gson.toJson(map);
    }

    @GET
    @Path("/getListsByField/{id}")
    @AuthorizedRoles({ROLE_SUPER_ADMIN})
    public String getListsByField(@PathParam("id") final Integer id,
                                  @QueryParam("term") final String filterBy,
                                  @QueryParam("selectedTerms") final String selectedTerms) {
        List<Object> result = reportTemplateService.getListsByField(id, filterBy, selectedTerms);
        return gson.toJson(result);
    }
}
