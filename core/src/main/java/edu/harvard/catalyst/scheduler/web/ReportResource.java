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

import com.google.common.collect.Lists;
import edu.harvard.catalyst.scheduler.core.SchedulerRuntimeException;
import edu.harvard.catalyst.scheduler.dto.CsvAbleDTO;
import edu.harvard.catalyst.scheduler.dto.ReportDTO;
import edu.harvard.catalyst.scheduler.dto.WorkloadAndResourceResponseDTO;
import edu.harvard.catalyst.scheduler.dto.response.CancellationsReportResponseDTO;
import edu.harvard.catalyst.scheduler.dto.response.StudyDataReportResponseDTO;
import edu.harvard.catalyst.scheduler.dto.statics.StudyStatusFilter;
import edu.harvard.catalyst.scheduler.entity.Report;
import edu.harvard.catalyst.scheduler.entity.ResourceType;
import edu.harvard.catalyst.scheduler.entity.Sublocation;
import edu.harvard.catalyst.scheduler.security.AuthorizedRoles;
import edu.harvard.catalyst.scheduler.service.ReportService;
import edu.harvard.catalyst.scheduler.util.DateUtility;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.inject.Singleton;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import java.io.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static edu.harvard.catalyst.scheduler.entity.InstitutionRoleType.*;
import static edu.harvard.catalyst.scheduler.util.MiscUtil.isNonNullNonEmpty;

/**
 * @author Bill Simons
 * @date 4/15/13
 * @link http://cbmi.med.harvard.edu
 * @link http://chip.org
 */

@Path("/report")
@Singleton
@Component
public class ReportResource extends SecuredResource {
    private ReportService reportService;

    @Autowired
    public ReportResource(ReportService reportService) {
        this.reportService = reportService;
    }

    //Don't use - Needed for spring security cglib proxying
    ReportResource() {
    }

    public static class BadReport {
        static BadReport instance = new BadReport();

        static BadReport getInstance() { return instance; }
    }

    @GET
    @Path("/getReports")
    @AuthorizedRoles({ROLE_SUPER_ADMIN, ROLE_RESOURCE_MANAGER, ROLE_SCHEDULER, ROLE_FRONT_DESK, ROLE_GENERAL_VIEW})
    public String getReports()  {
        reportService.logViewReports(getUser(), getRemoteHost(), "Reports Screen View");
        ReportDTO reportDTO = new ReportDTO();
        List<Report> reports = reportService.getReports(reportDTO);
        reportDTO.setReports(reports);
        return gson.toJson(reportDTO);
    }
    
    @GET
    @Path("/getReportData")
    @AuthorizedRoles({ROLE_SUPER_ADMIN, ROLE_RESOURCE_MANAGER, ROLE_SCHEDULER, ROLE_FRONT_DESK, ROLE_GENERAL_VIEW})
    public String getReportData(@QueryParam("id") int id)  {
        Report report = reportService.getReportData(getUser(), getRemoteHost(), id);
        return gson.toJson(report);
    }

    @POST
    @Path("/getReport")
    @Consumes({MediaType.APPLICATION_FORM_URLENCODED, MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN})
    @Produces({MediaType.APPLICATION_FORM_URLENCODED, MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN})
    @AuthorizedRoles({ROLE_SUPER_ADMIN, ROLE_RESOURCE_MANAGER, ROLE_SCHEDULER, ROLE_FRONT_DESK, ROLE_GENERAL_VIEW})
    public Response getReport(@FormParam("data") String data,
                              @QueryParam("name") String name,
                              @QueryParam("filterString") String filterString,
                              @QueryParam("filterId") String filterId,
                              @QueryParam("sortId") String sortId,
                              @DefaultValue(ReportDTO.JSON) @QueryParam("output") String output) {

        List<?> reportResultList = new ArrayList<>();
        ReportDTO dto = new ReportDTO();

        try {
            dto = gson.fromJson(data, ReportDTO.class);
            if (dto == null) {
                dto = new ReportDTO();
            }
            dto.setName(name);
            dto.setFilterString(filterString);
            dto.setFilterId(filterId);
            dto.setSortId(sortId);

            reportResultList = reportService.getReport(
                    dto,
                    getUser(),
                    getRemoteHost());

            // throwing error puts an ugly display on UI. this, in the case of
            //    form submit, blanks the display. which one is better?
            return handleListAndOutputChoice(reportResultList, dto, output, name);
        }
        catch (Exception e) {

            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    String adjustFileName(final String name) {
        String result = name;

        if (name.equals("export_daily_overview")) {
            result = "daily_overview";
        }
        if (name.equals("export_daily_resource")) {
            result = "daily_resource";
        }
        if (name.equals("meta_kitchen_flat")) {
            result = "meta_kitchen";
        }

        return result;
    }

    Response handleListAndOutputChoice(List<?> reportResultList,
                                       ReportDTO dto,
                                       String output,
                                       String reportName) {

        Response resultResponse = null;

        if (output.equalsIgnoreCase(ReportDTO.JSON)) {
            String result = subjectMrnSkippingGson.toJson(dto);
            resultResponse = Response.ok(result).build();
        } else {

            List<String> csvRows;
            if (isNonNullNonEmpty(reportResultList) &&
                    reportResultList.get(0) instanceof CsvAbleDTO) {

                CsvAbleDTO csvAbleDTO = (CsvAbleDTO) reportResultList.get(0);

                csvRows = csvAbleDTO.toCsvRows(reportResultList, reportName);
            }
            else {
                csvRows = Lists.newArrayList("");
            }

            final StreamingOutput streamingOutput =
                    out -> {
                        try (final Writer writer = new BufferedWriter(
                                new OutputStreamWriter(out))) {

                            for (String row : csvRows) {
                                writer.flush();
                                writer.write(row);
                                writer.flush();
                            }
                        } catch (final IOException ioe) {
                            SchedulerRuntimeException.logAndThrow(
                                    "Problem streaming with " + csvRows + "rows",
                                    ioe);
                        }
                    };

            reportName = adjustFileName(reportName).toLowerCase() + ".csv";

            resultResponse = Response.ok(streamingOutput, MediaType.APPLICATION_OCTET_STREAM).header(
                    "Content-Disposition", "attachment; filename=" + reportName).build();

        }

        return resultResponse;
    }

    @POST
    @Path("/logViewReport")
    @AuthorizedRoles({ROLE_SUPER_ADMIN, ROLE_RESOURCE_MANAGER, ROLE_SCHEDULER, ROLE_FRONT_DESK, ROLE_GENERAL_VIEW})
    public void logViewReport(@FormParam("data") String data)  {
        ReportDTO dto = gson.fromJson(data, ReportDTO.class);
        reportService.logViewReports(getUser(), getRemoteHost(), "Report Clicked - " + dto.getName());
    }

    @POST
    @Path("/loadResourceTypes")
    @AuthorizedRoles({ROLE_SUPER_ADMIN, ROLE_RESOURCE_MANAGER, ROLE_SCHEDULER, ROLE_FRONT_DESK, ROLE_GENERAL_VIEW})
    public String loadResourceTypes(@FormParam("data") String data)  {
        List<ResourceType> resourceTypes = reportService.getResourceTypes();
        ReportDTO dto = new ReportDTO();
        dto.setResourceTypes(resourceTypes);
        return gson.toJson(dto);
    }
    
    @POST
    @Path("/loadSublocations")
    @AuthorizedRoles({ROLE_SUPER_ADMIN, ROLE_RESOURCE_MANAGER, ROLE_STUDY_STAFF, ROLE_SCHEDULER, ROLE_FRONT_DESK, ROLE_GENERAL_VIEW})
    public String loadSublocations(@FormParam("data") String data)  {
        List<Sublocation> sublocations = reportService.getSublocations();
        ReportDTO dto = new ReportDTO();
        dto.setSublocations(sublocations);
        return gson.toJson(dto);
    }

    ///////////////////////////////////////////////////
    //////// 2.9+ reports //////////////////////////////
    ///////////////////////////////////////////////////
    @POST
    @Path("/studyDataReport")
    @AuthorizedRoles({ROLE_SUPER_ADMIN, ROLE_RESOURCE_MANAGER, ROLE_SCHEDULER, ROLE_FRONT_DESK, ROLE_GENERAL_VIEW})
    public Response studyDataReport (
            @QueryParam("localId") String localId,
            @QueryParam("name") String name,
            @QueryParam("studyStatus") String studyStatus,
            @QueryParam("fundingSource") String fundingSource,
            @QueryParam("sortStrategy") String sortStrategy,
            @DefaultValue(ReportDTO.JSON)
            @QueryParam("output") String output)  {

        ReportDTO dto = new ReportDTO();
        List<StudyDataReportResponseDTO> studyDataReportResponseDTOs =
                new ArrayList<>();

        StudyStatusFilter studyStatusFilter = null;
        if (isNonNullNonEmpty(studyStatus)) {
            // This will throw an exception if the value of filterId does not map to any of
            // the CalendarFilter enum values
            studyStatusFilter = StudyStatusFilter.valueOf(studyStatus);
        }

        try {
            studyDataReportResponseDTOs =
                    reportService.getStudyDataReport(
                            Optional.of(getUser()),
                            Optional.of(getRemoteHost()),
                            Optional.ofNullable(localId),
                            Optional.ofNullable(name),
                            Optional.ofNullable(fundingSource),
                            Optional.ofNullable(studyStatusFilter),
                            Optional.ofNullable(sortStrategy)
                    );

            dto.setStudyDataReport(studyDataReportResponseDTOs);

            return handleListAndOutputChoice(
                    studyDataReportResponseDTOs,
                    dto,
                    output,
                    "study_data");
        }
        catch (Exception e) {
            studyDataReportResponseDTOs.clear();

            String result = subjectMrnSkippingGson.toJson(dto);

            return Response.ok(result).build();
        }
    }
    @POST
    @Path("/cancellationsReport")
    @AuthorizedRoles({ROLE_SUPER_ADMIN, ROLE_RESOURCE_MANAGER, ROLE_SCHEDULER, ROLE_FRONT_DESK, ROLE_GENERAL_VIEW})
    public Response cancellationsReport(
            @QueryParam("startTime") String startTimeLongString,
            @QueryParam("endTime") String endTimeLongString,
            @QueryParam("localId") String localId,
            @QueryParam("name") String name,
            @QueryParam("type") String type,
            @QueryParam("reason") String reason,
            @QueryParam("noApprove") String noApprove,
            @QueryParam("sortStrategy") String sortStrategy,
            @DefaultValue(ReportDTO.JSON)
                @QueryParam("output") String output)    {

        ReportDTO dto = new ReportDTO();
        List<CancellationsReportResponseDTO>
                cancellationsReportResponseDTOs = new ArrayList<>();

        try {
            Date startTime = DateUtility.parseLong(startTimeLongString);
            Date endTime = DateUtility.parseLong(endTimeLongString);
            endTime = DateUtility.adjustDateToEndOfDay(endTime);

            cancellationsReportResponseDTOs =
                    reportService.getCancellationsReport(
                            Optional.of(getUser()),
                            Optional.of(getRemoteHost()),
                            Optional.of(startTime),
                            Optional.of(endTime),
                            Optional.ofNullable(localId),
                            Optional.ofNullable(name),
                            Optional.ofNullable(type),
                            Optional.ofNullable(reason),
                            Optional.ofNullable(noApprove),
                            Optional.ofNullable(sortStrategy)
                    );
            dto.setCancellationsReport(cancellationsReportResponseDTOs);

            return handleListAndOutputChoice(
                    cancellationsReportResponseDTOs,
                    dto,
                    output,
                    "cancellations");
        }
        catch (Exception e) {
            cancellationsReportResponseDTOs.clear();

            String result = subjectMrnSkippingGson.toJson(dto);

            return Response.ok(result).build();
        }
    }

    @POST
    @Path("/resourceLevelOfServiceReport")
    @AuthorizedRoles({ROLE_SUPER_ADMIN, ROLE_RESOURCE_MANAGER, ROLE_SCHEDULER, ROLE_FRONT_DESK, ROLE_GENERAL_VIEW})
    public Response resourceLevelOfServiceReport (
            @QueryParam("filterString") String filterString,
            @QueryParam("filterId") String filterId,
            @QueryParam("sortId") String sortId,
            @DefaultValue(ReportDTO.JSON)
                @QueryParam("output") String output) {

        ReportDTO dto = new ReportDTO();
        List<WorkloadAndResourceResponseDTO> workloadAndResourceResponseDTOS =
                new ArrayList<>();

        try {
            dto.setFilterString(filterString);
            dto.setFilterId(filterId);
            dto.setSortId(sortId);

            workloadAndResourceResponseDTOS =
                    reportService.getResourceLevelOfServiceReport(
                            dto,
                            Optional.of(getUser()),
                            Optional.of(getRemoteHost()));

            dto.setWorkloadAndResourcesReport(workloadAndResourceResponseDTOS);

            return handleListAndOutputChoice(
                    workloadAndResourceResponseDTOS,
                    dto,
                    output,
                    "resource_level_of_service");
        }
        catch (Exception e) {
            workloadAndResourceResponseDTOS.clear();

            String result = subjectMrnSkippingGson.toJson(dto);

            return Response.serverError().build();
        }
    }
}
