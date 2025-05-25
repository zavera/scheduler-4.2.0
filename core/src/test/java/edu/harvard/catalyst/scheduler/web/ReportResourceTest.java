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

import edu.harvard.catalyst.scheduler.dto.ReportDTO;
import edu.harvard.catalyst.scheduler.dto.response.CancellationsReportResponseDTO;
import edu.harvard.catalyst.scheduler.dto.response.StudyDataReportResponseDTO;
import edu.harvard.catalyst.scheduler.dto.statics.StudyStatusFilter;
import edu.harvard.catalyst.scheduler.entity.*;
import edu.harvard.catalyst.scheduler.security.SchedulerSession;
import edu.harvard.catalyst.scheduler.security.SchedulerUserDetails;
import edu.harvard.catalyst.scheduler.service.ReportService;
import edu.harvard.catalyst.scheduler.util.DateUtility;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static junit.framework.TestCase.assertNotNull;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

/**
 * Created with IntelliJ IDEA.
 * User: carl
 * Date: 2/13/14
 * Time: 9:56 AM
 */

@RunWith(MockitoJUnitRunner.class)
public class ReportResourceTest {
    ReportResource reportResource;

    @Mock
    private ReportService mockReportService;

    @Mock
    private User user;

    private String host = "host";
    private static final String badMessage = "{\"badMessage\":\"Sorry, there was an error fetching data\"}";

    @Before
    public void setUp() {
        reportResource = new ReportResource(mockReportService);

        SchedulerSession session = mock (SchedulerSession.class);
        SchedulerUserDetails userDetails = mock (SchedulerUserDetails.class);
        HttpServletRequest request = mock(HttpServletRequest.class);

        when(session.getUserDetails()).thenReturn(userDetails);
        when(userDetails.getUser()).thenReturn(user);
        when(request.getRemoteHost()).thenReturn(host);

        reportResource.setSession(session);
        reportResource.setRequest(request);
    }

    @Test
    public void testGetStudyDataReport() throws Exception {
        List<StudyDataReportResponseDTO> dtoList = new ArrayList<StudyDataReportResponseDTO>();

        Optional<User> ofUser = Optional.of(user);
        Optional<String> ofHost = Optional.of(host);
        Optional<String> absent = Optional.empty();
        Optional<StudyStatusFilter> absentStudyStatusFilter = Optional.empty();

        when(mockReportService.getStudyDataReport(
                ofUser, ofHost,
                absent, absent, absent,
                absentStudyStatusFilter, absent)).thenReturn(dtoList);

        Response result = reportResource.studyDataReport(
                null, null, null, null, null, null
        );

        verify(mockReportService, times(1)).getStudyDataReport(ofUser, ofHost,
                absent, absent, absent,
                absentStudyStatusFilter, absent);

        assertEquals(200, result.getStatus());
    }

    @Test
    public void testGetCancellationsReport() throws Exception {
        List<CancellationsReportResponseDTO> dtoList = new ArrayList<CancellationsReportResponseDTO>();

        String longForDate = "11111";
        Date date = DateUtility.parseLong(longForDate);
        Date endOfDate = DateUtility.adjustDateToEndOfDay(date);

        Optional<User> ofUser = Optional.of(user);
        Optional<String> ofHost = Optional.of(host);
        Optional<String> absent = Optional.empty();
        Optional<Date> ofDate = Optional.of(date);
        Optional<Date> ofEndDate = Optional.of(endOfDate);

        when(mockReportService.getCancellationsReport(
                ofUser, ofHost, ofDate, ofDate,
                absent, absent, absent,
                absent, absent, absent)).thenReturn(dtoList);

        Response result = reportResource.cancellationsReport(
                longForDate,
                longForDate,
                null,
                null,
                null,
                null,
                null,
                null,
                ReportDTO.JSON);

        verify(mockReportService,
                times(1)).getCancellationsReport(
                    ofUser, ofHost, ofDate, ofEndDate,
                    absent, absent, absent,
                    absent, absent, absent);

        assertNotNull(result);
    }

    @Test
    public void testLoadResourceTypes()
    {
        String data = null;

        List<ResourceType> resourceTypes = new ArrayList<>();
        resourceTypes.add(ResourceType.Lab);
        resourceTypes.add(ResourceType.Nursing);
        resourceTypes.add(ResourceType.Room);
        when(mockReportService.getResourceTypes()).thenReturn(resourceTypes);

        String expectedResult = "{\"resourceTypes\":[\"Lab\"," +
                "\"Nursing\",\"Room\"],\"authenticated\":true,\"total\":0,\"size\":0,\"start\":0}";
        String result = reportResource.loadResourceTypes(data);

        assertEquals("load resource types", expectedResult, result);
    }

    @Test
    public void testLoadSublocations()
    {
        String data = null;

        List<Sublocation> sublocationList = new ArrayList<>();
        Institution institution1 = new Institution("site1", "site1 long name");
        Sublocation sublocation1 = new Sublocation("sublocation1", institution1);

        Institution institution2 = new Institution("site2", "site2 long name");
        Sublocation sublocation2 = new Sublocation("sublocation2", institution2);
        sublocationList.add(sublocation1);
        sublocationList.add(sublocation2);

        when(mockReportService.getSublocations()).thenReturn(sublocationList);

        String expectedResult = "{\"sublocations\":[{\"name\":\"sublocation1\"," +
                "\"institution\":{\"name\":\"site1\",\"longName\":\"site1 long name\"}}," +
                "{\"name\":\"sublocation2\",\"institution\":{\"name\":\"site2\",\"longName\":\"site2 long name\"}}]," +
                "\"authenticated\":true,\"total\":0,\"size\":0,\"start\":0}";
        String result = reportResource.loadSublocations(data);

        assertEquals("load sublocations", expectedResult, result);
    }

    @Test
    public void testLogViewReport()
    {
        String reportName = "myReport";
        ReportDTO reportDTO = new ReportDTO();
        reportDTO.setName(reportName);

        String data = reportResource.gson.toJson(reportDTO);

        reportResource.logViewReport(data);

        verify(mockReportService, times(1)).logViewReports(user,  host, "Report Clicked - " + reportName);
    }

    @Test
    public void testGetReportData()
    {
        int reportId = 25;

        String result = reportResource.getReportData(reportId);

        verify(mockReportService, times(1)).getReportData(user, host, reportId);
    }

    @Test
    public void testGetReports()
    {
        List<Report> reports = new ArrayList<>();
        Report report1 = new Report();
        report1.setName("report1");
        report1.setDescription("report1 description");
        report1.setTitle("Report 1 Title");
        reports.add(report1);

        Report report2 = new Report();
        report2.setName("report2");
        report2.setDescription("report2 description");
        report2.setTitle("Report 2 Title");
        reports.add(report2);

        when(mockReportService.getReports(any(ReportDTO.class))).thenReturn(reports);

        String expectedResult = "{\"reports\":[{\"name\":\"report1\",\"title\":\"Report 1 Title\"," +
                "\"description\":\"report1 description\"},{\"name\":\"report2\",\"title\":\"Report 2 Title\"," +
                "\"description\":\"report2 description\"}],\"authenticated\":true,\"total\":0,\"size\":0,\"start\":0}";
        String result = reportResource.getReports();
        assertEquals("get reports", expectedResult, result);

        verify(mockReportService, times(1)).logViewReports(user,  host, "Reports Screen View");
    }

    @Test
    public void testGetReport()
    {
        String data =  null;
        String filterString = "filter";
        String name = "name";
        String filterId = "filterId";
        String sortId = "sortId";

        reportResource.getReport(data, name, filterString, filterId, sortId, ReportDTO.JSON);

        verify(mockReportService, times(1)).getReport(any(ReportDTO.class), any(User.class), anyString());
    }
}
