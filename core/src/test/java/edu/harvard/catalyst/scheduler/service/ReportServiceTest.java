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
package edu.harvard.catalyst.scheduler.service;

import edu.harvard.catalyst.scheduler.dto.ReportDTO;
import edu.harvard.catalyst.scheduler.dto.UserReportDTO;
import edu.harvard.catalyst.scheduler.dto.response.CancellationsReportResponseDTO;
import edu.harvard.catalyst.scheduler.dto.response.StudyDataReportResponseDTO;
import edu.harvard.catalyst.scheduler.dto.statics.StudyStatusFilter;
import edu.harvard.catalyst.scheduler.entity.*;
import edu.harvard.catalyst.scheduler.persistence.ReportDAO;
import edu.harvard.catalyst.scheduler.util.DateUtility;
import edu.harvard.catalyst.scheduler.util.TestUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.*;

import static org.mockito.Mockito.*;

/**
 * Created with IntelliJ IDEA.
 * User: carl
 * Date: 2/13/14
 * Time: 10:39 AM
 */

@RunWith(MockitoJUnitRunner.class)
public class ReportServiceTest {
    @Mock private ReportDAO mockReportDAO;
    @Mock private AuditService mockAuditService;

    private ReportService reportService;

    @Before
    public void setup() {
        reportService = new ReportService(mockReportDAO, mockAuditService);
    }

    @Test
    public void testGetStudyDataReport() throws Exception {
        User user = mock(User.class);
        List<StudyDataReportResponseDTO> studyDataReportResponseDTOs = new ArrayList<StudyDataReportResponseDTO>();

        Optional<User> ofUser = Optional.of(user);
        Optional<String> ofHost = Optional.of("host");
        Optional<String> absent = Optional.empty();
        Optional<StudyStatusFilter> absentStudyStatusFilter = Optional.empty();

        when(mockReportDAO.getStudyDataReport(
                absent, absent, absent,
                absentStudyStatusFilter, absent)).thenReturn(studyDataReportResponseDTOs);

        List<StudyDataReportResponseDTO> result = reportService.getStudyDataReport(
                ofUser, ofHost,
                absent, absent, absent,
                absentStudyStatusFilter, absent);

        String expectedLogMessage = "Launch - null query ";
        verify(mockAuditService, times(1)).logUserActivity("host", null, user, expectedLogMessage, null, null);
        TestUtils.assertNonNullAndEqual(result, studyDataReportResponseDTOs);
    }

    @Test
    public void testCancellationsReport() throws Exception {

        Institution institution = mock(Institution.class);
        User user = mock(User.class);
        when(user.getInstitution()).thenReturn(institution);
        when(institution.getName()).thenReturn("name");

        List<CancellationsReportResponseDTO> cancellationsReportResponseDTOs = new ArrayList<CancellationsReportResponseDTO>();

        String longForDate = "11111";
        Date date = DateUtility.parseLong(longForDate);
        Optional<Date> ofDate = Optional.of(date);

        Optional<User> ofUser = Optional.of(user);
        Optional<String> ofHost = Optional.of("host");
        Optional<String> absent = Optional.empty();

        when(mockReportDAO.getCancellationsReport(
                absent, absent, absent,
                absent, absent, absent, date, date)).thenReturn(cancellationsReportResponseDTOs);

        List<CancellationsReportResponseDTO> result = reportService.getCancellationsReport(
                ofUser, ofHost, ofDate, ofDate,
                absent, absent, absent,
                absent, absent, absent);

        String expectedLogMessage = "Launch - null query for: 'Wed Dec 31 19:00:11 EST 1969' to 'Wed Dec 31 19:00:11 EST 1969'";
        verify(mockAuditService, times(1)).logUserActivity("host", null, user, expectedLogMessage, null, null);
        TestUtils.assertNonNullAndEqual(result, cancellationsReportResponseDTOs);
    }

    @Test
    public void testGetReport()
    {
        final ReportDTO reportDTO = new ReportDTO();
        reportDTO.setName("user_data");

        final Calendar calendar = Calendar.getInstance();
        final Date startDate = DateUtility.startOfDay(calendar);
        final Date endDate = DateUtility.nextDay(startDate);
        reportDTO.setStartTime(startDate);
        reportDTO.setEndTime(endDate);
        final User user = new User();
        final String remoteHost = "";

        final List<UserReportDTO> usersList = new ArrayList<>();
        final User userData = new User();
        userData.setFirstName("first");
        userData.setLastName("last");
        userData.setRole(new Role());
        userData.setInstitution(new Institution());
        userData.setDivision(new Division());
        userData.setDepartment(new Department());
        userData.setInstitutionRole(new InstitutionRole());
        userData.setCredential(new Credential());
        userData.setFacultyRank(new FacultyRank());
        usersList.add(new UserReportDTO(userData));

        when(mockReportDAO.getUserDataReport(anyObject())).thenReturn(usersList);
        reportService.getReport(reportDTO, user, remoteHost);

        verify(mockReportDAO, times(1)).getUserDataReport(reportDTO);

        final ReportDTO reportDTO2 = new ReportDTO();
        reportDTO2.setName("study_subject_visit");
        reportService.getReport(reportDTO2, user, remoteHost);
        verify(mockReportDAO, times(1)).getStudySubjectVisitReport(reportDTO2);
    }

    @Test
    public void testGetReportData()
    {
        final User user = new User();
        user.setFirstName("first");
        user.setLastName("last");

        final String ipAddress = "ipAddress";
        final int id = 2;

        reportService.getReportData(user, ipAddress, id);

        verify(mockReportDAO, times(1)).getReportDataById(id);
    }

    @Test
    public void testGetReports()
    {
        final ReportDTO reportDTO = new ReportDTO();
        reportDTO.setName("myReportDTO");

        Report report = new Report();
        report.setName("myReport");
        List<Report> reportList = new ArrayList<>();
        reportList.add(report);
        when(mockReportDAO.getReports()).thenReturn(reportList);

        final ReportService reportService = new ReportService(mockReportDAO, mockAuditService);

        List<Report> reportListResult = reportService.getReports(reportDTO);
        Assert.assertEquals("report list", reportList, reportListResult);
    }
}
