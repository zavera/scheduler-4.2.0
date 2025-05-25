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
import edu.harvard.catalyst.scheduler.core.Statics;
import edu.harvard.catalyst.scheduler.dto.ScheduledVisitHistoryDTO;
import edu.harvard.catalyst.scheduler.entity.*;
import edu.harvard.catalyst.scheduler.persistence.AppointmentDAO;
import edu.harvard.catalyst.scheduler.persistence.AuditDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class AuditService {

    private AuditDAO auditDAO;
    private AppointmentDAO appointmentDAO;


    @Autowired
    public AuditService(AuditDAO auditDAO, AppointmentDAO appointmentDAO) {

        this.auditDAO = auditDAO;
        this.appointmentDAO = appointmentDAO;

    }

    //Don't use - Needed for spring security cglib proxying
    AuditService() {
    }

    void logSubjectActivity(String ipAddress, final Subject subject, User user, String action, String requiredFieldPreviousData, String previousData) {
        doLog(ipAddress, user, action, requiredFieldPreviousData, previousData, new LogOperation() {
            public void modifyActivityLog(ActivityLog log) {
                log.setAffectedSubject(subject);
            }
        });
    }

    void logAppointmentActivity(String ipAddress, final BookedVisit bookedVisit, User user, BookedVisitActivityLogStatics action) {
        logAppointmentActivity(ipAddress, bookedVisit, user, action.getLogString(), null);
    }

    void logAppointmentActivity(String ipAddress, final BookedVisit bookedVisit, User user, String action) {
        logAppointmentActivity(ipAddress, bookedVisit, user, action, null);
    }

    void logAppointmentActivity(String ipAddress, final BookedVisit bookedVisit, User user, BookedVisitActivityLogStatics action, AppointmentOverrideReason overrideReason) {
        logAppointmentActivity(ipAddress, bookedVisit, user, action.getLogString(), overrideReason);
    }

    void logAppointmentActivity(String ipAddress, final BookedVisit bookedVisit, User user, String action, AppointmentOverrideReason overrideReason) {
        BookedVisitActivityLog log = new BookedVisitActivityLog();
        log.setPerformingUser(user);
        log.setActionPerformed(action);
        log.setDate(new Date());
        log.setIpAddress(ipAddress);
        log.setBookedVisit(bookedVisit);
        log.setAppointmentOverrideReason(overrideReason);
        auditDAO.createEntity(log);
    }

    public ScheduledVisitHistoryDTO getActivityLogForBookedVisit(int visitId) {

        BookedVisit visit = appointmentDAO.findBookedVisitById(visitId);

        List<BookedVisitActivityLog> bookedVisitActivityList = getActivityLogForBookedVisit(visit);

        return new ScheduledVisitHistoryDTO(bookedVisitActivityList);

    }

    public List<BookedVisitActivityLog> getActivityLogForBookedVisit(BookedVisit bookedVisit) {

        return auditDAO.getActivityLogForBookedVisit(bookedVisit);

    }

    void logAppointmentOverrideActivity(String ipAddress, final BookedVisit bv, final Resource r, User user, String action, String requiredFieldPreviousData, String previousData) {
        doLog(ipAddress, user, action, requiredFieldPreviousData, previousData, new LogOperation() {
            public void modifyActivityLog(ActivityLog log) {
                log.setBookedVisit(bv);
                log.setAffectedResource(r);
            }
        });
    }

    void logUserActivity(String ipAddress, final User createdUser, User user, String action, String requiredFieldPreviousData, String previousData) {
        doLog(ipAddress, user, action, requiredFieldPreviousData, previousData, new LogOperation() {
            public void modifyActivityLog(ActivityLog log) {
                log.setAffectedUser(createdUser);
            }
        });
    }

    void logStudyActivity(String ipAddress, final Study study, User user, String action, String requiredFieldPreviousData, String previousData) {
        doLog(ipAddress, user, action, requiredFieldPreviousData, previousData, new LogOperation() {
            public void modifyActivityLog(ActivityLog log) {
                log.setAffectedStudy(study);
            }
        });
    }

    void logTemplateResourceActivity(String host, User user, VisitTemplate visitTemplate, String action, String requiredFieldPreviousData, String previousData) {
        doLog(host, user, action, requiredFieldPreviousData, previousData, new LogOperation() {
            public void modifyActivityLog(ActivityLog log) {
                log.setAffectedVisit(visitTemplate);
            }
        });
    }

    void logStudySubjectActivity(String ipAddress, final Study study, final Subject subject, User user, String action, String requiredFieldPreviousData, String previousData) {
        doLog(ipAddress, user, action, requiredFieldPreviousData, previousData, new LogOperation() {
            public void modifyActivityLog(ActivityLog log) {
                log.setAffectedStudy(study);
                log.setAffectedSubject(subject);
            }
        });
    }

    void logStudyAndSubjectMrnActivity(String ipAddress, final Study study, final SubjectMrn subjectMrn, User user, String action, String requiredFieldPreviousData, String previousData) {
        doLog(ipAddress, user, action, requiredFieldPreviousData, previousData, new LogOperation() {
            public void modifyActivityLog(ActivityLog log) {
                log.setAffectedStudy(study);
                log.setAffectedSubject(subjectMrn.getSubject());
                log.setAffectedSubjectMrn(subjectMrn);
            }
        });
    }

    void logStudyMemberActivity(String ipAddress, final Study study, final User u, final User user, final String action, String requiredFieldPreviousData, String previousData) {
        doLog(ipAddress, user, action, requiredFieldPreviousData, previousData, new LogOperation() {
            public void modifyActivityLog(ActivityLog log) {
                log.setAffectedUser(u);
                log.setAffectedStudy(study);
            }
        });
    }

    void logVisitActivity(String ipAddress, final VisitTemplate visit, final User user, final String action, String requiredFieldPreviousData, String previousData) {
        doLog(ipAddress, user, action, requiredFieldPreviousData, previousData, new LogOperation() {
            public void modifyActivityLog(ActivityLog log) {
                log.setActionPerformed(action + " : " + visit.getName() + " by " + user.getFirstName() + " " + user.getLastName());
                log.setAffectedVisit(visit);
            }
        });
    }

    void logDeleteVisitActivity(String ipAddress, final String visit, final User user, final String action, String requiredFieldPreviousData, String previousData) {
        doLog(ipAddress, user, action, requiredFieldPreviousData, previousData, new LogOperation() {
            public void modifyActivityLog(ActivityLog log) {
                log.setActionPerformed(action + " : " + visit + " by " + user.getFirstName() + " " + user.getLastName());
            }
        });
    }

    void logViewActivity(String ipAddress, User user, String action) {
        doLog(ipAddress, user, action, null, null, new LogOperation() {
            public void modifyActivityLog(ActivityLog log) {
                //no modifications necessary
            }
        });
    }

    void logVisitApprovalActivity(String ipAddress, final VisitTemplate visit, User user, final User affectedUser, String action, String requiredFieldPreviousData, String previousData) {
        doLog(ipAddress, user, action, requiredFieldPreviousData, previousData, new LogOperation() {
            public void modifyActivityLog(ActivityLog log) {
                log.setAffectedUser(affectedUser);
                log.setAffectedVisit(visit);
            }
        });
    }

    void logResourceActivity(String ipAddress, final Resource resource, User user, String action, String requiredFieldPreviousData, String previousData) {
        doLog(ipAddress, user, action, requiredFieldPreviousData, previousData, new LogOperation() {
            public void modifyActivityLog(ActivityLog log) {
                log.setAffectedResource(resource);
            }
        });
    }

    void logResourceSublocationClosureActivity(String ipAddress, final Sublocation sublocation, User user, String action, String requiredFieldPreviousData, String previousData) {
        doLog(ipAddress, user, action, requiredFieldPreviousData, previousData, new LogOperation() {
            public void modifyActivityLog(ActivityLog log) {
                log.setAffectedSublocation(sublocation);
            }
        });
    }

    ActivityLog generateActivityLog(String ipAddress, User user, String action, String requiredFieldPreviousData, String previousData) {
        ActivityLog log = new ActivityLog();
        log.setActionPerformed(action);
        log.setDate(new Date());
        log.setIpAddress(ipAddress);
        log.setPerformingUser(user);
        log.setChangesDetail(previousData);
        log.setChangesDetailRequiredField(requiredFieldPreviousData);
        return log;
    }

    void doLog(String ipAddress, User user, String action, String requiredFieldPreviousData, String previousData, LogOperation op) {
        ActivityLog log = generateActivityLog(ipAddress, user, action, requiredFieldPreviousData, previousData);
        op.modifyActivityLog(log);
        auditDAO.createEntity(log);
    }

    private interface LogOperation {
        void modifyActivityLog(ActivityLog log);
    }
}
