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
package edu.harvard.catalyst.scheduler.persistence;

import edu.harvard.catalyst.scheduler.AbstractSpringWiredHibernateTest;
import edu.harvard.catalyst.scheduler.dto.TemplateResourceDTO;
import edu.harvard.catalyst.scheduler.entity.*;
import edu.harvard.catalyst.scheduler.util.DateUtility;
import edu.harvard.catalyst.scheduler.util.SubjectDataEncryptor;
import edu.harvard.catalyst.scheduler.util.TestUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by xavier on 2/9/17.
 */
public abstract class AppointmentTestDataProvider extends AbstractSpringWiredHibernateTest {

    public static final int SUBJECT_MRN_ID = 2;
    public static final String MRN = "123";
    public static final String SITE = "site";
    public static final String ACTIVE = "active";
    public static final String FULL_NAME = "subject's full name";
    public static final String LOCAL_ID = "local id";
    public static final String BOOKED_VISIT_NAME = "booked visit name";
    public static final String VISIT_TEMPLATE_NAME = "visit template name";
    public static final Date START_DATE = new Date();
    public static final Date SCHEDULE_DATE = new Date(START_DATE.getTime() - 1);
    public static final Date END_DATE = new Date(START_DATE.getTime() + 1);
    public static final Date CHECKIN_DATE = new Date(START_DATE.getTime() + 2);
    public static final Date CHECKOUT_DATE = new Date(START_DATE.getTime() + 3);
    public static final Date CANCEL_DATE = new Date(START_DATE.getTime() + 4);
    public static final String CLASS_NAME = "some appointment calendar class name";
    public static final String HOLD_STATUS_NAME = "hold status name";
    public static final String SCHEDULED_STATUS_NAME = "scheduled status name";
    public static final String VISIT_ROOMS = "some rooms";
    public static final String SUBJECT_FIRST_NAME = "subject's first name";
    public static final String SUBJECT_LAST_NAME = "subject's last name";
    public static final String PI_LAST_NAME = "PI's last name";
    public static final String ROOM_NAMES = "Room 1, Room 2";
    public static final String STUDY_NAME = "The study name";
    public static final int VISIT_TEMPLATE_DURATION = 3;
    public static final String CATALYST_STUDY_ID = "My Catalyst ID";
    public static final String IRB = "Some IRB";
    public static final String CANCEL_STATUS_NAME = "cancellation reason";
    public static final String CANCEL_STATUS_REASON_NAME = "A good reason for cancelling";
    public static final String APPOINTMENT_STATUS_REASON_NAME = "some appointment-related reason";
    public static final String BOOKED_VISIT_COMMENT = "some comment";
    public static final String CHECKOUT_STATUS_REASON_NAME ="some reason for checking out";
    public static final int BOOKED_VISIT_TOTAL = 50;
    public static final int BOOKED_VISIT_SIZE = 10;
    public static final int BOOKED_VISIT_LIMIT = 20;
    public static final int BOOKED_VISIT_START = 5;
    public static final String SESSION_ID = "99 and then some";
    public static final String UNIQUE_KEY = "123";
    public static final String USER_DATA = "[{a: some json perhaps?}]";
    public static final String SCHEDULE_DATA = "[{a: some more json data perhaps?}]";
    public static final int RESOURCE_ID = 1234556;
    public static final String RESOURCE_NAME = "My resource name";
    public static final ResourceType RESOURCE_TYPE = ResourceType.Lab;
    public static final String GENDER_CODE = "M";
    public static final String GENDER_NAME = "Male";
    public static final String SUBJECT_BIRTHDAY = "02/03/1994";

    Gender gender;
    Subject subject;
    SubjectMrn subjectMrn;
    Study study;
    VisitTemplate visitTemplate;
    VisitType visitType;
    BookedVisit bookedVisit;
    BookedVisit bookedVisit2;
    AppointmentStatus holdStatus;
    AppointmentStatus scheduledStatus;

    AppointmentStatus appointmentStatus1;
    AppointmentStatus appointmentStatus2;
    AppointmentStatus appointmentStatus3;
    AppointmentStatus appointmentStatus4;
    AppointmentStatus appointmentStatus5;
    AppointmentStatus appointmentStatus6;

    AppointmentStatusReason appointmentStatusReason;
    AppointmentStatusReason checkoutStatusReason;
    AppointmentStatusReason cancellationStatusReason;

    CancellationStatus cancellationStatus;

    Resource resource;
    BookedResource bookedResource;
    List<BookedResource> bookedResourceList;

    User adminUser;
    User studyStaffUser;
    User piUser;

    Institution testInst;
    InstitutionRole superAdminInstRole;
    InstitutionRole studyStaffInstRole;

    Role superAdminRole;
    Role studyStaffRole;
    Role piRole;

    void setupAppointmentRelatedData() {

        int HOLD_ID = 1;
        int SCHEDULE_ID = 2;

        testInst = new Institution("instShort", "instLong");
        save(testInst);

        superAdminInstRole = new InstitutionRole();
        superAdminInstRole.setType(InstitutionRoleType.ROLE_SUPER_ADMIN);
        superAdminInstRole.setDescription("this is the super admin role");
        superAdminInstRole.setName("super admin role");
        save(superAdminInstRole);

        studyStaffInstRole = new InstitutionRole();
        studyStaffInstRole.setType(InstitutionRoleType.ROLE_STUDY_STAFF);
        studyStaffInstRole.setDescription("this is the study staff role");
        studyStaffInstRole.setName("study staff role");
        save(studyStaffInstRole);

        superAdminRole = new Role();
        superAdminRole.setType(RoleType.ROLE_FINAL_APPROVER);
        superAdminRole.setName("final approval role");
        save(superAdminRole);

        studyStaffRole = new Role();
        studyStaffRole.setType(RoleType.ROLE_NURSE);
        studyStaffRole.setName("nurse role");
        save(studyStaffRole);

        piRole = new Role();
        piRole.setType(RoleType.ROLE_PI);
        piRole.setName("PI role");
        save(piRole);

        adminUser = new User();
        adminUser.setInstitutionRole(superAdminInstRole);
        adminUser.setFirstName("myFirstName");
        adminUser.setLastName("myLastName");
        adminUser.setEcommonsId("adminU");
        adminUser.setInstitution(testInst);
        adminUser.setPassword("foo");
        adminUser.setRole(superAdminRole);
        adminUser.setSalt("AAAAA");
        save(adminUser);

        studyStaffUser = new User();
        studyStaffUser.setInstitutionRole(studyStaffInstRole);
        studyStaffUser.setFirstName("my other FirstName");
        studyStaffUser.setLastName("my other LastName");
        studyStaffUser.setEcommonsId("staffU");
        studyStaffUser.setInstitution(testInst);
        studyStaffUser.setPassword("bar");
        studyStaffUser.setRole(studyStaffRole);
        studyStaffUser.setSalt("9876");
        save(studyStaffUser);

        appointmentStatus1 = new AppointmentStatus("appointment status 1");
        appointmentStatus2 = new AppointmentStatus("appointment status 2");
        appointmentStatus3 = new AppointmentStatus("appointment status 3");
        appointmentStatus4 = new AppointmentStatus("appointment status 4");
        appointmentStatus5 = new AppointmentStatus("appointment status 5");
        appointmentStatus6 = new AppointmentStatus("appointment status 6");
        save(appointmentStatus1);
        save(appointmentStatus2);
        save(appointmentStatus3);
        save(appointmentStatus4);
        save(appointmentStatus5);
        save(appointmentStatus6);

        appointmentStatusReason = new AppointmentStatusReason();
        appointmentStatusReason.setAppointmentStatus(appointmentStatus1);
        appointmentStatusReason.setName(APPOINTMENT_STATUS_REASON_NAME);
        save(appointmentStatusReason);

        cancellationStatus = new CancellationStatus(1, CANCEL_STATUS_NAME);
        save(cancellationStatus);

        cancellationStatusReason = new AppointmentStatusReason();
        cancellationStatusReason.setAppointmentStatus(appointmentStatus1);
        cancellationStatusReason.setName(CANCEL_STATUS_REASON_NAME);
        save(cancellationStatusReason);

        checkoutStatusReason = new AppointmentStatusReason();
        checkoutStatusReason.setAppointmentStatus(appointmentStatus3);
        checkoutStatusReason.setName(CHECKOUT_STATUS_REASON_NAME);
        save(checkoutStatusReason);

        holdStatus = new AppointmentStatus();
        holdStatus.setId(HOLD_ID);
        holdStatus.setName(HOLD_STATUS_NAME);
        save(holdStatus);

        scheduledStatus = new AppointmentStatus();
        scheduledStatus.setId(SCHEDULE_ID);
        scheduledStatus.setName(SCHEDULED_STATUS_NAME);
        save(scheduledStatus);

        gender = new Gender();
        gender.setCode(GENDER_CODE);
        gender.setName(GENDER_NAME);
        save(gender);

        subject = new Subject();
        subject.setFullName(FULL_NAME);
        subject.setLastName(SUBJECT_LAST_NAME);
        subject.setFirstName(SUBJECT_FIRST_NAME);
        subject.setGender(gender);
        subject.setGenderType(GenderType.F);
        subject.setBirthdate(new Date(SUBJECT_BIRTHDAY));
        // Important: do the encryption using the following method, because it
        // sets the "secure" flag on the subject object
        SubjectDataEncryptor.encryptSubjectInPlace(subject);
        save(subject);

        subjectMrn = new SubjectMrn(subject, SubjectDataEncryptor.encrypt(MRN), SITE, ACTIVE, SUBJECT_MRN_ID);
        save(subjectMrn);

        piUser = new User();
        piUser.setInstitutionRole(studyStaffInstRole);
        piUser.setFirstName("PI FirstName");
        piUser.setLastName(PI_LAST_NAME);
        piUser.setEcommonsId("piU");
        piUser.setInstitution(testInst);
        piUser.setPassword("barbar");
        piUser.setRole(piRole);
        piUser.setSalt("AAAA");
        save(piUser);

        study = new Study();
        study.setLocalId(LOCAL_ID);
        study.setInvestigator(piUser);
        study.setName(STUDY_NAME);
        study.setCatalystId(CATALYST_STUDY_ID);
        study.setIrb(IRB);
        save(study);

        visitType = new VisitType();
        visitType.setName(TestUtils.InpatientCRC);
        visitType.setInpatient(true);
        save(visitType);

        visitTemplate = new VisitTemplate();
        visitTemplate.setVisitType(visitType);
        visitTemplate.setName(VISIT_TEMPLATE_NAME);
        visitTemplate.setDuration(VISIT_TEMPLATE_DURATION);
        save(visitTemplate);

        resource = new Resource(
                RESOURCE_ID, RESOURCE_NAME, RESOURCE_TYPE, 0,
                new ArrayList<Sublocation>(), new ArrayList<ResourceAlternate>(), new ArrayList<ResourceAlternate>(),
                new ArrayList<ResourceSchedule>(), new ArrayList<ResourceSchedule>()
        );
        save(resource);

        TemplateResourceDTO templateResourceDTO = new TemplateResourceDTO();
        templateResourceDTO.setAlternate(false);
        templateResourceDTO.setBillable(false);
        templateResourceDTO.setStartMinutes(DateUtility.minutesSinceOrigin(new Date(2000L)));
        templateResourceDTO.setEndMinutes(DateUtility.minutesSinceOrigin(new Date(4000L)));

        TemplateResource templateResource = new TemplateResource(
                templateResourceDTO, resource, visitTemplate
        );
        save(templateResource);

        List<BookedResource> bookedResourceList = new ArrayList<>();

        bookedVisit = new BookedVisit(
            study, visitTemplate, BOOKED_VISIT_NAME,
                visitType,
            subjectMrn, scheduledStatus,
            cancellationStatus, appointmentStatusReason,
            cancellationStatusReason, checkoutStatusReason,
            START_DATE, END_DATE, CHECKIN_DATE,
            CHECKOUT_DATE, CANCEL_DATE, BOOKED_VISIT_COMMENT,
            SCHEDULE_DATE, CHECKOUT_STATUS_REASON_NAME,
            BOOKED_VISIT_TOTAL, BOOKED_VISIT_SIZE, BOOKED_VISIT_LIMIT, BOOKED_VISIT_START, SESSION_ID,
            UNIQUE_KEY, USER_DATA, SCHEDULE_DATA,
            bookedResourceList, null, VISIT_ROOMS, ""
        );
        save(bookedVisit);

        bookedVisit2 = new BookedVisit(
                study, visitTemplate, BOOKED_VISIT_NAME,
                visitType,
                subjectMrn, scheduledStatus,
                cancellationStatus, appointmentStatusReason,
                cancellationStatusReason, checkoutStatusReason,
                START_DATE, END_DATE, CHECKIN_DATE,
                CHECKOUT_DATE, CANCEL_DATE, BOOKED_VISIT_COMMENT,
                SCHEDULE_DATE, CHECKOUT_STATUS_REASON_NAME,
                BOOKED_VISIT_TOTAL, BOOKED_VISIT_SIZE, BOOKED_VISIT_LIMIT, BOOKED_VISIT_START, SESSION_ID,
                UNIQUE_KEY, USER_DATA, SCHEDULE_DATA,
                bookedResourceList, null, VISIT_ROOMS, ""
        );
        save(bookedVisit2);

    }

}
