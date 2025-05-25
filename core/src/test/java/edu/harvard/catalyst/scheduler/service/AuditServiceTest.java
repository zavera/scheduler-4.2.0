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

import edu.harvard.catalyst.scheduler.core.BookedVisitActivityLogStatics;
import edu.harvard.catalyst.scheduler.dto.ScheduledVisitHistoryDTO;
import edu.harvard.catalyst.scheduler.entity.*;
import edu.harvard.catalyst.scheduler.persistence.AppointmentDAO;
import edu.harvard.catalyst.scheduler.persistence.AuditDAO;
import edu.harvard.catalyst.scheduler.util.DateUtility;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static edu.harvard.catalyst.scheduler.util.DateUtility.date24HTime;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;


/**
 * @author Bill Simons
 * @date 11/5/13
 * @link http://cbmi.med.harvard.edu
 * @link http://chip.org
 */
@RunWith(MockitoJUnitRunner.class)
public class AuditServiceTest {
    @Mock
    private AuditDAO auditDao;
    @Mock
    private AppointmentDAO appointmentDAO;
    private String ipAddress = "1.2.3.4";
    private User performingUser = new User();
    private String action = "jackson";
    private String requiredFieldPreviousData = "req";
    private String previousData = "previous";
    private AuditService service;


    /**
     * An interface to represent the action for the test to invoke on the audit service and any modifications
     * to be made to the expected ActivityLog
     */
    private interface LogTestDriver {
        void performServiceOperation() throws Exception;

        void setUpExpectedLog(ActivityLog expectedLog);
    }

    private ActivityLog expectedLogTemplate() {
        ActivityLog expectedLog = new ActivityLog();
        expectedLog.setIpAddress(ipAddress);
        expectedLog.setPerformingUser(performingUser);
        expectedLog.setActionPerformed(action);
        expectedLog.setChangesDetailRequiredField(requiredFieldPreviousData);
        expectedLog.setChangesDetail(previousData);
        return expectedLog;
    }

    private void driveTest(LogTestDriver driver) throws Exception {
        driver.performServiceOperation();

        ActivityLog expectedLog = expectedLogTemplate();
        driver.setUpExpectedLog(expectedLog);
        ArgumentCaptor<ActivityLog> logArgumentCaptor = ArgumentCaptor.forClass(ActivityLog.class);
        verify(auditDao).createEntity(logArgumentCaptor.capture());
        ActivityLog actualLog = logArgumentCaptor.getValue();

        assertTrue(fieldsEqualTo(expectedLog, actualLog));
    }

    public boolean fieldsEqualTo(ActivityLog expected, ActivityLog actual) {
        if(actual == expected) {
            return true;
        }

        if(expected.getActionPerformed() != null ? !expected.getActionPerformed().equals(actual.getActionPerformed()) : actual.getActionPerformed() != null) {
            return false;
        }
        if(expected.getAffectedResource() != null ? !expected.getAffectedResource().equals(actual.getAffectedResource()) : actual.getAffectedResource() != null) {
            return false;
        }
        if(expected.getAffectedStudy() != null ? !expected.getAffectedStudy().equals(actual.getAffectedStudy()) : actual.getAffectedStudy() != null) {
            return false;
        }
        if(expected.getAffectedSubject() != null ? !expected.getAffectedSubject().equals(actual.getAffectedSubject()) : actual.getAffectedSubject() != null) {
            return false;
        }
        if(expected.getAffectedSublocation() != null ? !expected.getAffectedSublocation().equals(actual.getAffectedSublocation()) : actual.getAffectedSublocation() != null) {
            return false;
        }
        if(expected.getAffectedUser() != null ? !expected.getAffectedUser().equals(actual.getAffectedUser()) : actual.getAffectedUser() != null) {
            return false;
        }
        if(expected.getAffectedVisit() != null ? !expected.getAffectedVisit().equals(actual.getAffectedVisit()) : actual.getAffectedVisit() != null) {
            return false;
        }
        if(expected.getAppointmentOverrideReason() != null ? !expected.getAppointmentOverrideReason().equals(actual.getAppointmentOverrideReason()) : actual.getAppointmentOverrideReason() != null) {
            return false;
        }
        if(expected.getBookedVisit() != null ? !expected.getBookedVisit().equals(actual.getBookedVisit()) : actual.getBookedVisit() != null) {
            return false;
        }
        if(expected.getChangesDetail() != null ? !expected.getChangesDetail().equals(actual.getChangesDetail()) : actual.getChangesDetail() != null) {
            return false;
        }
        if(expected.getChangesDetailRequiredField() != null ? !expected.getChangesDetailRequiredField().equals(actual.getChangesDetailRequiredField()) : actual.getChangesDetailRequiredField() != null) {
            return false;
        }
        if(expected.getIpAddress() != null ? !expected.getIpAddress().equals(actual.getIpAddress()) : actual.getIpAddress() != null) {
            return false;
        }
        if(expected.getPerformingUser() != null ? !expected.getPerformingUser().equals(actual.getPerformingUser()) : actual.getPerformingUser() != null) {
            return false;
        }

        return true;
    }


    @Before
    public void beforeTest() {
        service = new AuditService(auditDao, appointmentDAO);
    }

    @Test
    public void testLogSubjectActivity() throws Exception {
        driveTest(new LogTestDriver() {
            final public Subject subject = mock(Subject.class);
            public void performServiceOperation() throws Exception {
                service.logSubjectActivity(ipAddress, subject, performingUser, action, requiredFieldPreviousData, previousData);
            }

            public void setUpExpectedLog(ActivityLog expectedLog) {
                expectedLog.setAffectedSubject(subject);
            }
        });
    }

    @Test
    public void testLogAppointmentActivity() throws Exception {

        BookedVisit bookedVisit = mock(BookedVisit.class);

        AppointmentOverrideReason overrideReason = new AppointmentOverrideReason();

        service.logAppointmentActivity(ipAddress, bookedVisit, performingUser, action, overrideReason);

        BookedVisitActivityLog expectedLog = new BookedVisitActivityLog();
        expectedLog.setIpAddress(ipAddress);
        expectedLog.setPerformingUser(performingUser);
        expectedLog.setActionPerformed(action);
        expectedLog.setBookedVisit(bookedVisit);
        expectedLog.setAppointmentOverrideReason(overrideReason);

        ArgumentCaptor<BookedVisitActivityLog> logArgumentCaptor = ArgumentCaptor.forClass(BookedVisitActivityLog.class);
        verify(auditDao).createEntity(logArgumentCaptor.capture());
        BookedVisitActivityLog actualLog = logArgumentCaptor.getValue();

        assertTrue(expectedLog.getActionPerformed().equals(actualLog.getActionPerformed()));
        assertTrue(expectedLog.getAppointmentOverrideReason().equals(actualLog.getAppointmentOverrideReason()));
        assertTrue(expectedLog.getBookedVisit().equals(actualLog.getBookedVisit()));
        assertTrue(expectedLog.getIpAddress().equals(actualLog.getIpAddress()));
        assertTrue(expectedLog.getPerformingUser().equals(actualLog.getPerformingUser()));

    }

    @Test
    public void testLogAppointmentActivity2() throws Exception {

        AuditService serviceSpy = spy(service);

        BookedVisit bookedVisit = mock(BookedVisit.class);

        serviceSpy.logAppointmentActivity(ipAddress, bookedVisit, performingUser, action);

        verify(serviceSpy).logAppointmentActivity(ipAddress, bookedVisit, performingUser, action, null);

    }

    @Test
    public void testLogAppointmentActivity3() throws Exception {

        AuditService serviceSpy = spy(service);

        BookedVisit bookedVisit = mock(BookedVisit.class);

        doNothing().when(serviceSpy).logAppointmentActivity(ipAddress, bookedVisit, performingUser, BookedVisitActivityLogStatics.SCHEDULED.getLogString(), null);

        serviceSpy.logAppointmentActivity(ipAddress, bookedVisit, performingUser, BookedVisitActivityLogStatics.SCHEDULED);

        verify(serviceSpy).logAppointmentActivity(ipAddress, bookedVisit, performingUser, BookedVisitActivityLogStatics.SCHEDULED.getLogString(), null);

    }

    @Test
    public void testLogAppointmentActivity4() throws Exception {

        AuditService serviceSpy = spy(service);

        BookedVisit bookedVisit = mock(BookedVisit.class);

        AppointmentOverrideReason overrideReason = new AppointmentOverrideReason();

        doNothing().when(serviceSpy).logAppointmentActivity(ipAddress, bookedVisit, performingUser, BookedVisitActivityLogStatics.SCHEDULED.getLogString(), overrideReason);

        serviceSpy.logAppointmentActivity(ipAddress, bookedVisit, performingUser, BookedVisitActivityLogStatics.SCHEDULED, overrideReason);

        verify(serviceSpy).logAppointmentActivity(ipAddress, bookedVisit, performingUser, BookedVisitActivityLogStatics.SCHEDULED.getLogString(), overrideReason);

    }

    @Test
    public void testLogAppointmentOverrideActivity() throws Exception {
        driveTest(new LogTestDriver() {
            final public BookedVisit bookedVisit = mock(BookedVisit.class);
            final public Resource resource = mock(Resource.class);

            public void performServiceOperation() throws Exception {
                service.logAppointmentOverrideActivity(ipAddress, bookedVisit, resource, performingUser, action, requiredFieldPreviousData, previousData);
            }

            public void setUpExpectedLog(ActivityLog expectedLog) {
                expectedLog.setBookedVisit(bookedVisit);
                expectedLog.setAffectedResource(resource);
            }
        });

    }

    @Test
    public void testLogUserActivity() throws Exception {
        driveTest(new LogTestDriver() {
            final public User createdUser = mock(User.class);

            public void performServiceOperation() throws Exception {
                service.logUserActivity(ipAddress, createdUser, performingUser, action, requiredFieldPreviousData, previousData);
            }

            public void setUpExpectedLog(ActivityLog expectedLog) {
                expectedLog.setAffectedUser(createdUser);
            }
        });

    }

    @Test
    public void testLogStudyActivity() throws Exception {
        driveTest(new LogTestDriver() {
            final public Study study = mock(Study.class);

            public void performServiceOperation() throws Exception {
                service.logStudyActivity(ipAddress, study, performingUser, action, requiredFieldPreviousData, previousData);
            }

            public void setUpExpectedLog(ActivityLog expectedLog) {
                expectedLog.setAffectedStudy(study);
            }
        });
    }

    @Test
    public void testLogStudySubjectActivity() throws Exception {
        driveTest(new LogTestDriver() {
            final public Study study = mock(Study.class);
            final public Subject subject = mock(Subject.class);

            public void performServiceOperation() throws Exception {
                service.logStudySubjectActivity(ipAddress, study, subject, performingUser, action, requiredFieldPreviousData, previousData);
            }

            public void setUpExpectedLog(ActivityLog expectedLog) {
                expectedLog.setAffectedStudy(study);
                expectedLog.setAffectedSubject(subject);
            }
        });
    }
    
    @Test
    public void testLogStudyAndSubjectMrnActivity() throws Exception {
        driveTest(new LogTestDriver() {
            final public Study study = mock(Study.class);
            final public Subject subject = mock(Subject.class);
            final public SubjectMrn subjectMrn = mock(SubjectMrn.class);


            public void performServiceOperation() throws Exception {
                when(subjectMrn.getSubject()).thenReturn(subject);
                service.logStudyAndSubjectMrnActivity(ipAddress, study, subjectMrn,performingUser, action, requiredFieldPreviousData, previousData);
            }

            public void setUpExpectedLog(ActivityLog expectedLog) {
                expectedLog.setAffectedStudy(study);
                expectedLog.setAffectedSubject(subject);
                expectedLog.setAffectedSubjectMrn(subjectMrn);
            }
        });
    }
    @Test
    public void testLogStudyMemberActivity() throws Exception {
        driveTest(new LogTestDriver() {
            final public Study study = mock(Study.class);
            final public User affectedUser = mock(User.class);

            public void performServiceOperation() throws Exception {
                service.logStudyMemberActivity(ipAddress, study, affectedUser, performingUser, action, requiredFieldPreviousData, previousData);
            }

            public void setUpExpectedLog(ActivityLog expectedLog) {
                expectedLog.setAffectedStudy(study);
                expectedLog.setAffectedUser(affectedUser);
            }
        });
    }

    @Test
    public void testLogVisitActivity() throws Exception {
        driveTest(new LogTestDriver() {
            public VisitTemplate visit = mock(VisitTemplate.class);

            public void performServiceOperation() throws Exception {
                service.logVisitActivity(ipAddress, visit, performingUser, action, requiredFieldPreviousData, previousData);
            }

            public void setUpExpectedLog(ActivityLog expectedLog) {
                expectedLog.setAffectedVisit(visit);
                expectedLog.setActionPerformed(action + " : " + visit.getName() + " by " + performingUser.getFirstName() + " " + performingUser.getLastName());
            }
        });
    }

    @Test
    public void testLogDeleteVisitActivity() throws Exception {
        driveTest(new LogTestDriver() {
            public String visit = "visit";

            public void performServiceOperation() throws Exception {
                service.logDeleteVisitActivity(ipAddress, visit, performingUser, action, requiredFieldPreviousData, previousData);
            }

            public void setUpExpectedLog(ActivityLog expectedLog) {
                //TODO - may need to refactor value here
                expectedLog.setActionPerformed(action + " : " + visit + " by " + performingUser.getFirstName() + " " + performingUser.getLastName());
            }
        });
    }

    @Test
    public void testLogViewActivity() throws Exception {
        driveTest(new LogTestDriver() {
            public void performServiceOperation() throws Exception {
                service.logViewActivity(ipAddress, performingUser, action);
            }

            public void setUpExpectedLog(ActivityLog expectedLog) {
                //replace the changes details that are needed for all other tests
                expectedLog.setChangesDetail(null);
                expectedLog.setChangesDetailRequiredField(null);
            }
        });
    }

    @Test
    public void testLogVisitApprovalActivity() throws Exception {
        driveTest(new LogTestDriver() {
            public VisitTemplate visit = mock(VisitTemplate.class);
            public User affectedUser = mock(User.class);

            public void performServiceOperation() throws Exception {
                service.logVisitApprovalActivity(ipAddress, visit, performingUser, affectedUser, action, requiredFieldPreviousData, previousData);
            }

            public void setUpExpectedLog(ActivityLog expectedLog) {
                expectedLog.setAffectedUser(affectedUser);
                expectedLog.setAffectedVisit(visit);
            }
        });
    }

    @Test
    public void testLogResourceActivity() throws Exception {
        driveTest(new LogTestDriver() {
            public Resource resource = mock(Resource.class);

            public void performServiceOperation() throws Exception {
                service.logResourceActivity(ipAddress, resource, performingUser, action, requiredFieldPreviousData, previousData);
            }

            public void setUpExpectedLog(ActivityLog expectedLog) {
                expectedLog.setAffectedResource(resource);
            }
        });
    }

    @Test
    public void testLogResourceSublocationClosureActivity() throws Exception {
        driveTest(new LogTestDriver() {
            public Sublocation sublocation = mock(Sublocation.class);

            public void performServiceOperation() throws Exception {
                service.logResourceSublocationClosureActivity(ipAddress, sublocation, performingUser, action, requiredFieldPreviousData, previousData);
            }

            public void setUpExpectedLog(ActivityLog expectedLog) {
                expectedLog.setAffectedSublocation(sublocation);
            }
        });
    }

    @Test
    public void testLogTemplateResourceActivity() throws Exception {
        driveTest(new LogTestDriver() {
            final public VisitTemplate visitTemplate = mock(VisitTemplate.class);

            public void performServiceOperation() throws Exception {

                    service.logTemplateResourceActivity(ipAddress, performingUser, visitTemplate, action, requiredFieldPreviousData, previousData);
            }

            public void setUpExpectedLog(ActivityLog expectedLog) {
                expectedLog.setAffectedVisit(visitTemplate);
            }
        });
    }

    @Test
    public void testGetActivityLogForBookedVisit() {

        BookedVisit bookedVisit = new BookedVisit();

        List<BookedVisitActivityLog> list = service.getActivityLogForBookedVisit(bookedVisit);

        verify(auditDao, times(1)).getActivityLogForBookedVisit(bookedVisit);

    }

    @Test
    public void testGetActivityLogForBookedVisit2() {

        String cancellationStatus = "cancellation";
        String otherVisitStatus = "some other status";

        int visitId = 99;
        String eCommonsId = "user1";

        final User user0 = new User();
        user0.setId(7);
        user0.setLastName("last0");
        user0.setFirstName("first0");

        final User user1 = new User();
        user1.setId(8);
        user1.setLastName("last1");
        user1.setFirstName("first1");
        user1.setEcommonsId(eCommonsId);

        AppointmentStatus cancelledAppointmentStatus = new AppointmentStatus();
        cancelledAppointmentStatus.setName(cancellationStatus);

        AppointmentStatus otherAppointmentStatus = new AppointmentStatus();
        otherAppointmentStatus.setName(otherVisitStatus);

        BookedVisit bookedVisit = new BookedVisit();
        bookedVisit.setId(visitId);
        bookedVisit.setAppointmentStatus(cancelledAppointmentStatus);

        BookedVisitActivityLog bookedVisitActivityLog1 = new BookedVisitActivityLog();
        bookedVisitActivityLog1.setActionPerformed(BookedVisitActivityLogStatics.SCHEDULED.getLogString());
        bookedVisitActivityLog1.setAppointmentOverrideReason(new AppointmentOverrideReason());
        bookedVisitActivityLog1.setBookedVisit(bookedVisit);
        Date date1 = new Date();
        bookedVisitActivityLog1.setDate(date1);
        bookedVisitActivityLog1.setId(11);
        bookedVisitActivityLog1.setIpAddress("123.456.789.000");
        bookedVisitActivityLog1.setPerformingUser(user1);

        BookedVisitActivityLog bookedVisitActivityLog2 = new BookedVisitActivityLog();
        bookedVisitActivityLog2.setActionPerformed(BookedVisitActivityLogStatics.RESCHEDULED.getLogString());
        bookedVisitActivityLog2.setAppointmentOverrideReason(new AppointmentOverrideReason());
        bookedVisitActivityLog2.setBookedVisit(bookedVisit);
        Date date2 = new Date(date1.getTime() + 1000000);
        bookedVisitActivityLog2.setDate(date2);
        bookedVisitActivityLog2.setId(12);
        bookedVisitActivityLog2.setIpAddress("000.999.888.777");
        bookedVisitActivityLog2.setPerformingUser(null);

        List<BookedVisitActivityLog> bookedVisitActivityLogList = new ArrayList<>();
        bookedVisitActivityLogList.add(bookedVisitActivityLog1);
        bookedVisitActivityLogList.add(bookedVisitActivityLog2);

        when(appointmentDAO.findBookedVisitById(visitId)).thenReturn(bookedVisit);
        when(auditDao.getActivityLogForBookedVisit(bookedVisit)).thenReturn(bookedVisitActivityLogList);

        ScheduledVisitHistoryDTO dto = service.getActivityLogForBookedVisit(visitId);

        List<Map<String, String>> returnedBookedActivityList = dto.getBookedVisitActivity();
        assertEquals(2, returnedBookedActivityList.size());
        Map<String, String> returnedLogItem1 = returnedBookedActivityList.get(0);
        Map<String, String> returnedLogItem2 = returnedBookedActivityList.get(1);

        assertEquals(3, returnedLogItem1.size());
        assertEquals(BookedVisitActivityLogStatics.SCHEDULED.getLogString(), returnedLogItem1.get("action"));
        assertEquals(eCommonsId, returnedLogItem1.get("user"));
        assertEquals(DateUtility.format(date24HTime(), date1), returnedLogItem1.get("date"));

        assertEquals(3, returnedLogItem2.size());
        assertEquals(BookedVisitActivityLogStatics.RESCHEDULED.getLogString(), returnedLogItem2.get("action"));
        assertEquals(null, returnedLogItem2.get("user"));
        assertEquals(DateUtility.format(date24HTime(), date2), returnedLogItem2.get("date"));

    }

}
