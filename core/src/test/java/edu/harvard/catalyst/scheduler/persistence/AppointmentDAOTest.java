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

import com.google.common.collect.Lists;
import edu.harvard.catalyst.scheduler.core.Statics;
import edu.harvard.catalyst.scheduler.dto.SearchDTO;
import edu.harvard.catalyst.scheduler.dto.SwitchSubjectResultDTO;
import edu.harvard.catalyst.scheduler.dto.VisitRenderSummaryDTO;
import edu.harvard.catalyst.scheduler.dto.response.BookedVisitsResponse;
import edu.harvard.catalyst.scheduler.dto.response.CalendarVisitsResponse;
import edu.harvard.catalyst.scheduler.dto.response.VisitCommentsResponse;
import edu.harvard.catalyst.scheduler.entity.*;
import edu.harvard.catalyst.scheduler.util.DateUtility;
import edu.harvard.catalyst.scheduler.util.SubjectDataEncryptor;

import edu.harvard.catalyst.scheduler.util.TestUtils;
import org.hibernate.query.Query;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.text.SimpleDateFormat;
import java.util.*;

import static edu.harvard.catalyst.scheduler.core.Statics.NA;
import static edu.harvard.catalyst.scheduler.core.Statics.NO_SUBJECT_ASSIGNED;
import static edu.harvard.catalyst.scheduler.util.DateUtility.dateHourMinSec;
import static edu.harvard.catalyst.scheduler.util.DateUtility.format;
import static edu.harvard.catalyst.scheduler.util.TestUtils.*;
import static java.util.Arrays.asList;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public final class AppointmentDAOTest extends AppointmentTestDataProvider {

    private static final int LIMIT = 50;

    AppointmentDAO daoSpy;

    @Autowired
    private AppointmentDAO appointmentDAO;
    private static final String ORDER_BY_ASC = "ASC";

    private AppointmentDAOTest.Helper testHelper = new Helper();

    private void setUpDaoSpy() {

        // We need a new AppointmentDAO here because for some reason, if we wrap the autowired
        // AppointmentDAO object with spy(), it just doesn't work, i.e. the methods that we
        // think are mocked (i.e. mocked with when()) don't get mocked, i.e. they get
        // executed normally
        AppointmentDAO dao = new AppointmentDAO();
        daoSpy = spy(dao);

        doReturn(holdStatus).when(daoSpy).findHoldStatus();
        doReturn(scheduledStatus).when(daoSpy).findScheduledStatus();

        doReturn(subjectMrn).when(daoSpy).findById(refEq(SubjectMrn.class), refEq(new Integer(SUBJECT_MRN_ID)));
        doNothing().when(daoSpy).updateEntity(anyObject());

        doReturn(ROOM_NAMES).when(daoSpy).findRoomString(anyInt());
        doReturn(session()).when(daoSpy).session();

    }

    //TODO: More.  For now, just check that the generated SQL parses, to address
    //https://jira.cbmi.med.harvard.edu/jira/browse/HCCRCSCHEDULING-4159
    @Test
    public void testGetOverbookTimelineData() {
        final Date now = new Date();

        final User user = new User();

        appointmentDAO.getOverbookTimelineData(now, now, ResourceType.Lab, asList(1, 2, 3), ORDER_BY_ASC, user);
    }


    @Test
    public void testSwitchVisitSubjectToBlockedVisit() {

        setupAppointmentRelatedData();
        setUpDaoSpy();

        int newSubjectMrnId = 0;
        boolean homeScreen = true;

        SwitchSubjectResultDTO returnDto = daoSpy.switchVisitSubject(newSubjectMrnId, bookedVisit, homeScreen, CLASS_NAME);

        verify(daoSpy, times(1)).findHoldStatus();

        VisitRenderSummaryDTO visitSummary = returnDto.getVisitSummary();

        assertEquals(holdStatus.getName(), visitSummary.getClassName());
        assertEquals(true, visitSummary.isAllDay());
        assertEquals(VISIT_ROOMS, visitSummary.getRoom());
        assertEquals(visitSummary.getTitle(), Statics.NO_SUBJECT_ASSIGNED);
        assertEquals(LOCAL_ID, visitSummary.getLocalId());
        assertEquals(PI_LAST_NAME, visitSummary.getPiName());
        assertEquals(BOOKED_VISIT_NAME, visitSummary.getVisitName());
        assertEquals(format(dateHourMinSec(), START_DATE), visitSummary.getStartDate());
        assertEquals(format(dateHourMinSec(), END_DATE), visitSummary.getEndDate());
        assertEquals(true, visitSummary.isInpatientVisitType());

        assertNull(bookedVisit.getSubjectMrn());
        assertEquals(NO_SUBJECT_ASSIGNED, returnDto.getFullName());
        assertEquals(holdStatus, bookedVisit.getAppointmentStatus());

    }

    @Test
    public void testSwitchVisitSubjectToOtherSubject() {

        setupAppointmentRelatedData();
        setUpDaoSpy();

        int newSubjectMrnId = SUBJECT_MRN_ID;
        boolean homeScreen = true;

        SwitchSubjectResultDTO returnDto = daoSpy.switchVisitSubject(newSubjectMrnId, bookedVisit, homeScreen, CLASS_NAME);

        VisitRenderSummaryDTO visitSummary = returnDto.getVisitSummary();

        verify(daoSpy, times(1)).findById(SubjectMrn.class, new Integer(newSubjectMrnId));

        assertEquals(CLASS_NAME, visitSummary.getClassName());
        assertEquals(true, visitSummary.isAllDay());
        assertEquals(VISIT_ROOMS, visitSummary.getRoom());
        assertEquals(SUBJECT_LAST_NAME.toUpperCase(), visitSummary.getTitle());
        assertEquals(LOCAL_ID, visitSummary.getLocalId());
        assertEquals(PI_LAST_NAME, visitSummary.getPiName());
        assertEquals(BOOKED_VISIT_NAME, visitSummary.getVisitName());
        assertEquals(format(dateHourMinSec(), START_DATE), visitSummary.getStartDate());
        assertEquals(format(dateHourMinSec(), END_DATE), visitSummary.getEndDate());
        assertEquals(true, visitSummary.isInpatientVisitType());

        assertEquals(subjectMrn, bookedVisit.getSubjectMrn());
        assertEquals(FULL_NAME.toUpperCase(), returnDto.getFullName());
        assertEquals(scheduledStatus, bookedVisit.getAppointmentStatus());

    }


    @Test
    public void testIsAllDay() {

        boolean[] inpatience = {true, false};
        boolean[] expectedValues = {true, false};

        VisitType visitType = new VisitType();

        for (int i = 0; i < inpatience.length; i++) {
            boolean inpatienceType = inpatience[i];
            visitType.setInpatient(inpatienceType);
            boolean expectedValueWhenHomeViewIsTrue = expectedValues[i];
            assertFalse(appointmentDAO.isAllDay(false, visitType));
            assertEquals(expectedValueWhenHomeViewIsTrue, appointmentDAO.isAllDay(true, visitType));
        }
    }

    @Test
    public void testFindSublocationScheduleAndTestIsSublocationClosed() {

        Institution institution = new Institution("institution 1", "Institution Number One");

        save(institution);

        Sublocation sublocation1 = new Sublocation("location 1", institution);
        Sublocation sublocation2 = new Sublocation("location 2", institution);

        save(sublocation1);
        save(sublocation2);

        String reason = "renovations";

        Date startTime1 = new Date("12/31/1998");
        Date endTime1 = new Date("12/31/1999");

        Date startTime2 = new Date("12/31/1999");
        Date endTime2 = new Date("01/03/2000");

        Date startTime3 = new Date("01/01/2002");
        Date endTime3 = new Date("01/03/2009");

        Date startTime4 = new Date("01/01/2008");
        Date endTime4 = new Date("01/03/2010");

        Date startTime5 = new Date("01/01/2010");
        Date endTime5 = new Date("01/03/2010");

        SublocationClosureInterval closureInterval1a = new SublocationClosureInterval(
                startTime1, endTime1, reason, sublocation1
        );
        SublocationClosureInterval closureInterval1b = new SublocationClosureInterval(
                startTime1, endTime1, reason, sublocation2
        );
        SublocationClosureInterval closureInterval2a = new SublocationClosureInterval(
                startTime2, endTime2, reason, sublocation1
        );
        SublocationClosureInterval closureInterval2b = new SublocationClosureInterval(
                startTime2, endTime2, reason, sublocation2
        );
        SublocationClosureInterval closureInterval3a = new SublocationClosureInterval(
                startTime3, endTime3, reason, sublocation1
        );
        SublocationClosureInterval closureInterval3b = new SublocationClosureInterval(
                startTime3, endTime3, reason, sublocation2
        );
        SublocationClosureInterval closureInterval4a = new SublocationClosureInterval(
                startTime4, endTime4, reason, sublocation1
        );
        SublocationClosureInterval closureInterval4b = new SublocationClosureInterval(
                startTime4, endTime4, reason, sublocation2
        );
        SublocationClosureInterval closureInterval5a = new SublocationClosureInterval(
                startTime5, endTime5, reason, sublocation1
        );
        SublocationClosureInterval closureInterval5b = new SublocationClosureInterval(
                startTime5, endTime5, reason, sublocation2
        );

        save(closureInterval1a);
        save(closureInterval1b);
        save(closureInterval2a);
        save(closureInterval2b);
        save(closureInterval3a);
        save(closureInterval3b);
        save(closureInterval4a);
        save(closureInterval4b);

        Date searchStartDate = new Date("01/01/2000");
        Date searchEndDate = new Date("01/01/2010");

        List<SublocationClosureInterval> sublocationClosureSchedule = appointmentDAO.findSublocationSchedule(
                sublocation2, searchStartDate, searchEndDate);

        assertEquals("The schedule should have ... closure intervals", 3, sublocationClosureSchedule.size());
        assertTrue("closureInterval2b should be in the closure schedule", sublocationClosureSchedule.contains(closureInterval2b));
        assertTrue("closureInterval3b should be in the closure schedule", sublocationClosureSchedule.contains(closureInterval3b));
        assertTrue("closureInterval4b should be in the closure schedule", sublocationClosureSchedule.contains(closureInterval4b));

        // Now test the other method, isSublocationClosed()

        searchStartDate = new Date("01/01/1998");
        searchEndDate = new Date("12/30/1998");

        assertFalse("sublocation 1 should NOT be closed at some point between " + searchStartDate.toString() + " and " +
                searchEndDate.toString(), appointmentDAO.isSublocationClosed(sublocation1, searchStartDate, searchEndDate));

        searchStartDate = new Date("12/30/1998");
        searchEndDate = new Date("01/01/1999");

        assertTrue("sublocation 1 should be closed at some point between " + searchStartDate.toString() + " and " +
                searchEndDate.toString(), appointmentDAO.isSublocationClosed(sublocation1, searchStartDate, searchEndDate));

        searchStartDate = new Date("01/01/2016");
        searchEndDate = new Date("01/02/2016");

        assertFalse("sublocation 1 should NOT be closed at some point between " + searchStartDate.toString() + " and " +
                searchEndDate.toString(), appointmentDAO.isSublocationClosed(sublocation1, searchStartDate, searchEndDate));

    }


    @Test
    public void testFindOverbookConflictResourcesByVisitStatus() {

        Institution institution = new Institution("institution 1", "Institution Number One");

        save(institution);

        Sublocation sublocation1 = new Sublocation("location 1", institution);
        Sublocation sublocation2 = new Sublocation("location 2", institution);

        save(sublocation1);
        save(sublocation2);

        List<Sublocation> sublocations = new ArrayList<>(2);
        sublocations.add(sublocation1);
        sublocations.add(sublocation2);

        // final Integer id, final String name, final ResourceType resourceType, final Integer sharedResource, final List<Sublocation> sublocations,
        // final List<ResourceAlternate> alternateResourceList, final List<ResourceAlternate> sourceResourceList, final List<ResourceSchedule> defaultScheduleList, final List<ResourceSchedule> overrideScheduleList
        Resource resource = new Resource(
                1, "Resource 1", ResourceType.Room, null, sublocations, null, null, null, null
        );
        save(resource);

        Study study = new Study();
        save(study);

        VisitType visitType = new VisitType();
        visitType.setName(InpatientCRC);
        save(visitType);

        VisitTemplate visitTemplate = new VisitTemplate();
        visitTemplate.setVisitType(visitType);
        save(visitTemplate);

        // This is resets the auto increment on the id column to start with 1
        // And that is needed because the HQL query in appointmentDAO.findOverbookConflictResourcesByVisitStatus()
        // looks for appointment status by id in (1,2,5)
        session().createNativeQuery("ALTER TABLE Appointment_Status ALTER COLUMN id RESTART WITH 1").executeUpdate();
        appointmentStatus1 = new AppointmentStatus("appointment status 1 (Open)");
        appointmentStatus1.setIsOpen(true);
        save(appointmentStatus1);

        BookedVisit bookedVisit = new BookedVisit();
        bookedVisit.setAppointmentStatus(appointmentStatus1);//apptStatusList.get(0));
        bookedVisit.setScheduledStartTime(new Date("01/01/2000 00:00:00"));
        bookedVisit.setScheduledEndTime(new Date("01/01/2000 12:00:00"));
        bookedVisit.setStudy(study);
        bookedVisit.setVisitTemplate(visitTemplate);
        bookedVisit.setVisitType(visitType);

        save(bookedVisit);

        BookedResource bookedResource1 = new BookedResource();
        bookedResource1.setResource(resource);
        bookedResource1.setBookedVisit(bookedVisit);
        bookedResource1.setScheduledStartTime(new Date("01/01/2000 00:00:00"));
        bookedResource1.setScheduledEndTime(new Date("01/01/2000 02:00:00"));

        BookedResource bookedResource2 = new BookedResource();
        bookedResource2.setResource(resource);
        bookedResource2.setBookedVisit(bookedVisit);
        bookedResource2.setScheduledStartTime(new Date("01/01/2000 02:00:00"));
        bookedResource2.setScheduledEndTime(new Date("01/01/2000 04:00:00"));

        BookedResource bookedResource3 = new BookedResource();
        bookedResource3.setResource(resource);
        bookedResource3.setBookedVisit(bookedVisit);
        bookedResource3.setScheduledStartTime(new Date("01/01/2000 04:00:00"));
        bookedResource3.setScheduledEndTime(new Date("01/01/2000 06:00:00"));

        BookedResource bookedResource4 = new BookedResource();
        bookedResource4.setResource(resource);
        bookedResource4.setBookedVisit(bookedVisit);
        bookedResource4.setScheduledStartTime(new Date("01/01/2000 06:00:00"));
        bookedResource4.setScheduledEndTime(new Date("01/01/2000 08:00:00"));

        BookedResource bookedResource5 = new BookedResource();
        bookedResource5.setResource(resource);
        bookedResource5.setBookedVisit(bookedVisit);
        bookedResource5.setScheduledStartTime(new Date("01/01/2000 08:00:00"));
        bookedResource5.setScheduledEndTime(new Date("01/01/2000 10:00:00"));

        save(bookedResource1);
        save(bookedResource2);
        save(bookedResource3);
        save(bookedResource4);
        save(bookedResource5);

        Date searchStartDate = new Date("01/01/2000 03:00:00");
        Date searchEndDate = new Date("01/01/2000 07:00:00");
        List<BookedResource> bookedResources = appointmentDAO.findOverbookConflictResourcesByVisitStatus(
                resource, searchStartDate, searchEndDate
        );

        assertEquals("There should be 3 booked resources between " + searchStartDate.toString() + " and " + searchEndDate.toString(), 3, bookedResources.size());
        assertTrue("bookedResource2 should be in the list", bookedResources.contains(bookedResource2));
        assertTrue("bookedResource3 should be in the list", bookedResources.contains(bookedResource3));
        assertTrue("bookedResource4 should be in the list", bookedResources.contains(bookedResource4));

    }

    @Test
    public void testFindBookedResourcesCount() {

        setupAppointmentRelatedData();

        List<Subject> subjects = testHelper.generateXSubjects(10);
        List<VisitTemplate> visitTemplateList = testHelper.generateTwoVisitTemplates();
        testHelper.generateStudySubjects(subjects, visitTemplateList);

        List<BookedVisit> bookedVisits = testHelper.generateBookedVisitsForSubjects(subjects, visitTemplateList);

        assertNotEquals("Expected to find booked visits", 0, bookedVisits.size());

        BookedVisit bookedVisit = bookedVisits.get(0);

        List<BookedResource> bookedResourceList = new ArrayList<>();

        BookedResource bookedResource1 = testHelper.generateBookedResource(1, bookedVisit, ResourceType.Room,
                null, null);
        BookedResource bookedResource2 = testHelper.generateBookedResource(1, bookedVisit, ResourceType.Room,
                null, null);
        BookedResource bookedResource3 = testHelper.generateBookedResource(3, bookedVisit, ResourceType.Nursing,
                null, null);
        BookedResource bookedResource4 = testHelper.generateBookedResource(4, bookedVisit, ResourceType.Lab,
                null, null);
        bookedResourceList.add(bookedResource1);
        bookedResourceList.add(bookedResource2);
        bookedResourceList.add(bookedResource3);
        bookedResourceList.add(bookedResource4);

        bookedVisit.setBookedResourceList(bookedResourceList);

        Long numBookedResources = appointmentDAO.findBookedResourcesCount(bookedVisit.getId());

        assertEquals("Expected to find 4 booked resources", Long.valueOf(4), numBookedResources);

    }

    @Test
    public void testFindStudySubjectBySubject() {

        final List<Subject> subjectList = testHelper.generateXSubjects(4);
        final List<VisitTemplate> visitTemplateList = testHelper.generateTwoVisitTemplates();
        testHelper.generateStudySubjects(subjectList, visitTemplateList);

        final Subject subject = subjectList.get(0);
        final List<StudySubject> studySubjects = appointmentDAO.findStudySubjectBySubject(subject);

        assertEquals(2, studySubjects.size());

        studySubjects.forEach(studySubject -> {
            assertTrue(subjectList.contains(studySubject.getSubject()));
        });

    }

    @Test
    public void testFindStudySubjectBySubjectAndStudy() {

        List<Subject> subjectList = testHelper.generateXSubjects(4);
        List<VisitTemplate> visitTemplateList = testHelper.generateTwoVisitTemplates();
        testHelper.generateStudySubjects(subjectList, visitTemplateList);

        final Subject subject = subjectList.get(0);
        final String expectedSubjectName = subject.getFullName();

        final Study study = visitTemplateList.get(0).getStudy();

        final String expectStudyName = study.getName();
        List<StudySubject> studySubjects = appointmentDAO.findStudySubjectBySubjectAndStudy(subject, study);

        assertEquals(1, studySubjects.size());

        final StudySubject studySubject = studySubjects.get(0);

        final String actualStudyName = studySubject.getStudy().getName();
        assertEquals(expectStudyName, actualStudyName);

        final String actualSubjectName = studySubject.getSubject().getFullName();
        assertEquals(expectedSubjectName, actualSubjectName);

    }

    @Test
    public void testFindAppointmentCommentsByVisit() {

        setupAppointmentRelatedData();

        List<BookedVisit> bookedVisitList = testHelper.generateBookedVisits();
        assertNotEquals(0, bookedVisitList.size());

        BookedVisit bookedVisit = bookedVisitList.get(0);

        Comments comments1 = new Comments();
        comments1.setComment("My first visit comment");
        comments1.setUser(adminUser);
        comments1.setBookedVisit(bookedVisit);
        save(comments1);

        String expectedComment = comments1.getComment();

        Comments comments2 = new Comments();
        comments2.setComment("My second visit comment");
        comments2.setUser(adminUser);
        comments2.setBookedVisit(bookedVisit);
        save(comments2);

        List<VisitCommentsResponse.VisitComment> visitCommentList =
                appointmentDAO.findAppointmentCommentsByVisit(bookedVisit);

        assertEquals(2, visitCommentList.size());

        VisitCommentsResponse.VisitComment secondVisitComment = visitCommentList.get(0);
        assertEquals(expectedComment, secondVisitComment.getComment());

    }

    private class Helper {

        private Date getDate(int year, int month, int day) {
            return DateUtility.parse(DateUtility.monthDayYear(), month + "/" + day + "/" + year);
        }

        private List<SubjectMrn> getSubjectMrns(List<Subject> subjects) {

            List<SubjectMrn> subjectMrnList = new ArrayList<>();

            subjects.forEach(subject -> {

                subjectMrnList.addAll(subject.getSubjectMrnSet());
            });

            return subjectMrnList;
        }

        private List<Subject> generateXSubjects(int numSubjects) {
            List<Subject> subjects = new ArrayList<>();
            Date birthdate = getDate(1995, 5, 10);

            Map<GenderType, Gender> genderMap = generateGenderMap();
            for (int i = 1; i <= numSubjects; i++) {
                final Subject subject = new Subject();
                subject.setFullName(SubjectDataEncryptor.encrypt(FULL_NAME + "Num" + i));
                subject.setFirstName(SubjectDataEncryptor.encrypt(SUBJECT_FIRST_NAME + "Num" + i));
                subject.setLastName(SubjectDataEncryptor.encrypt(SUBJECT_LAST_NAME + "Num" + i));
                subject.setBirthdate(birthdate);

                subject.setGender(genderMap.get(GenderType.M));
                subject.setGenderType(GenderType.M);
                save(subject);

                Set<SubjectMrn> subjectMrnSet = new HashSet<>();
                SubjectMrn subjectMrn1 = new SubjectMrn();
                subjectMrn1.setMrn(SubjectDataEncryptor.encrypt(i + "1"));
                subjectMrn1.setStatus("A");
                subjectMrn1.setSubject(subject);
                save(subjectMrn1);
                subjectMrnSet.add(subjectMrn1);

                SubjectMrn subjectMrn2 = new SubjectMrn();
                subjectMrn2.setMrn(SubjectDataEncryptor.encrypt(i + "2"));
                subjectMrn2.setStatus("A");
                subjectMrn2.setSubject(subject);
                save(subjectMrn2);
                subjectMrnSet.add(subjectMrn2);

                subject.setSubjectMrnSet(subjectMrnSet);
                subjects.add(subject);
            }

            return subjects;
        }

        private Map<GenderType, Gender> generateGenderMap() {
            Map<GenderType, Gender> genderMap = new HashMap<>();
            Gender maleGender = new Gender();
            maleGender.setCode("M");
            maleGender.setName(GenderType.M.getGenderName());
            maleGender.setId(1);
            save(maleGender);
            genderMap.put(GenderType.M, maleGender);

            Gender femaleGender = new Gender();
            femaleGender.setCode("F");
            femaleGender.setName(GenderType.F.getGenderName());
            femaleGender.setId(2);
            save(femaleGender);
            genderMap.put(GenderType.F, femaleGender);

            return genderMap;
        }

        private List<BookedVisit> generateBookedVisits() {

            List<Subject> subjects = testHelper.generateXSubjects(5);
            List<VisitTemplate> visitTemplateList = testHelper.generateTwoVisitTemplates();
            testHelper.generateStudySubjects(subjects, visitTemplateList);

            return testHelper.generateBookedVisitsForSubjects(subjects, visitTemplateList);

        }

        private List<BookedVisit> generateBookedVisitsForSubjects(List<Subject> subjects, List<VisitTemplate> visitTemplateList) {

            List<BookedVisit> bookedVisitList = Lists.newArrayList();

            for (int i = 0; i < subjects.size(); i++) {
                Subject subject = subjects.get(i);
                Set<SubjectMrn> subjectMrnSet = subject.getDecryptedSubjectMrnSet();
                Iterator<SubjectMrn> subjectMrnIterator = subjectMrnSet.iterator();
                int subjectMrnCount = -1;
                while (subjectMrnIterator.hasNext()) {
                    subjectMrnCount++;
                    SubjectMrn subjectMrn = subjectMrnIterator.next();

                    VisitTemplate visitTemplate = visitTemplateList.get(subjectMrnCount);
                    String name = "visit" + subjectMrnCount;

                    AppointmentStatus appointmentStatus;
                    String errorMsg = null;
                    Date checkOutDate = null;
                    Date cancelDate = null;
                    String comment = "test comment";
                    String checkoutStatusReasonName = null;
                    int total = -1;
                    int size = -1;
                    int start = 1;
                    String sessionId = "1099";
                    String userdata = null;
                    List<BookedResource> bookedResourceList = new ArrayList<>();
                    Resource selectedRoom = null;
                    String uniquekey = i + "" + subjectMrnCount;
                    String scheduledata = null;

                    Date scheduledStartTime;
                    Date scheduledEndTime;
                    Date checkInDate;
                    Date scheduleDate;

                    VisitType visitType = new VisitType();
                    visitType.setName(InpatientCRC);
                    visitType.setInpatient(true);
                    save(visitType);

                    if (subjectMrnCount % 2 == 0) {
                        scheduledStartTime = getDate(2016, 10, 15);
                        scheduledEndTime = getDate(2016, 10, 16);
                        checkInDate = null;
                        scheduleDate = getDate(2016, 8, 15);
                        appointmentStatus = appointmentStatus1;
                        visitType.setName(OutpatientNonCRC);
                        visitType.setInpatient(false);
                    } else {
                        scheduledStartTime = getDate(2016, 11, 5);
                        scheduledEndTime = getDate(2016, 11, 6);
                        checkInDate = getDate(2016, 11, 5);
                        scheduleDate = getDate(2016, 10, 20);
                        appointmentStatus = appointmentStatus2;
                    }
                    final BookedVisit bookedVisit = new BookedVisit(visitTemplate.getStudy(), visitTemplate, name,
                            visitType,
                            subjectMrn, appointmentStatus,
                            cancellationStatus, appointmentStatusReason, cancellationStatusReason,
                            checkoutStatusReason, scheduledStartTime, scheduledEndTime, checkInDate, checkOutDate, cancelDate,
                            comment, scheduleDate, checkoutStatusReasonName, total, size, LIMIT, start, sessionId, uniquekey,
                            userdata, scheduledata, bookedResourceList, selectedRoom, VISIT_ROOMS, errorMsg
                    );

                    bookedVisitList.add(bookedVisit);
                    save(bookedVisit);
                }
            }

            return bookedVisitList;
        }

        private List<VisitTemplate> generateTwoVisitTemplates() {
            List<VisitTemplate> visitTemplateList = new ArrayList<>();
            int numVisitTemplates = 2;

            for (int i = 1; i <= numVisitTemplates; i++) {
                final Study study = new Study();
                study.setName("Study Num" + i);
                study.setCrcFunded(false);
                study.setLocalId("Local " + i);
                save(study);

                VisitType visitType = new VisitType();
                visitType.setName(InpatientCRC);
                save(visitType);

                VisitTemplate visitTemplate = new VisitTemplate();
                visitTemplate.setStudy(study);
                visitTemplate.setVisitType(visitType);
                save(visitTemplate);
                visitTemplateList.add(visitTemplate);
            }

            return visitTemplateList;
        }

        private List<StudySubject> generateStudySubjects(List<Subject> subjectList, List<VisitTemplate> visitTemplates) {

            List<StudySubject> studySubjectList = new ArrayList<>();
            List<SubjectMrn> subjectMrnList = testHelper.getSubjectMrns(subjectList);

            for (int i = 0; i < subjectMrnList.size(); i++) {
                final SubjectMrn subjectMrn = subjectMrnList.get(i);
                final Subject subject = subjectMrn.getSubject();

                StudySubject studySubject = new StudySubject();
                studySubject.setSubjectMrn(subjectMrn);
                studySubject.setSubject(subject);
                studySubject.setActive(true);

                Study study = visitTemplates.get(0).getStudy();
                if (i % 2 == 0) {
                    study = visitTemplates.get(1).getStudy();
                }
                studySubject.setStudy(study);
                save(studySubject);

                studySubjectList.add(studySubject);
            }

            return studySubjectList;
        }

        private BookedResource generateBookedResource(int idPrefix, BookedVisit bookedVisit, ResourceType resourceType,
                                                      Date scheduledStartTime, Date scheduledEndTime) {
            Resource resource = new Resource(
                    1, "Resource " + idPrefix, resourceType, null,
                    null, null,
                    null, null, null
            );
            save(resource);

            int duration = 60;
            BookedResource bookedResource = new BookedResource(duration, scheduledStartTime, scheduledEndTime,
                    bookedVisit, null, resource, null,
                    null, null, null,
                    null, null, false
            );

            save(bookedResource);

            return bookedResource;
        }
    }

    @Test
    public void testBookedVisitsDTO() throws java.text.ParseException {

        setupAppointmentRelatedData();
        setUpDaoSpy();

        // TODO: make 8 separate tests + use Helper inner class
        // make calls to a single helper with input + output parameters. So test code
        // would look like 8 method calls.

        // set dates for this test, i.e. override the dates set by setupEntitiesForMockedFunctions()
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss a");
        Date startDate = sdf.parse("21/12/2020 01:45:34 am");
        Date endDate = sdf.parse("21/12/2020 01:23:45 pm");
        String expectedStartDateString = "2020-12-21 01:45";
        String expectedEndDateString = "2020-12-21 13:23";

        bookedVisit.setScheduledStartTime(startDate);
        bookedVisit.setScheduledEndTime(endDate);

        List<BookedVisit> bookedVisits = new ArrayList<>(1);
        bookedVisits.add(bookedVisit);

        // test case #1

        bookedVisit.setSubjectMrn(subjectMrn);
        boolean isHomeView = true;
        visitType.setInpatient(true);
        bookedVisit.setVisitType(visitType);

        List<CalendarVisitsResponse> calendarVisitsResponses = daoSpy.bookedVisitsDTO(isHomeView, bookedVisits);

        verifyCalendarVisitsResponse(
                calendarVisitsResponses,
                true,
                true,
                SUBJECT_LAST_NAME.toUpperCase(),
                expectedStartDateString,
                expectedEndDateString
        );

        // test case #2

        bookedVisit.setSubjectMrn(subjectMrn);
        isHomeView = true;
        visitType.setInpatient(false);
        bookedVisit.setVisitType(visitType);
        calendarVisitsResponses = daoSpy.bookedVisitsDTO(isHomeView, bookedVisits);

        verifyCalendarVisitsResponse(
                calendarVisitsResponses,
                false,
                false,
                SUBJECT_LAST_NAME.toUpperCase(),
                expectedStartDateString,
                expectedEndDateString
        );

        // test case #3

        bookedVisit.setSubjectMrn(subjectMrn);
        isHomeView = false;
        visitType.setInpatient(true);
        bookedVisit.setVisitType(visitType);
        calendarVisitsResponses = daoSpy.bookedVisitsDTO(isHomeView, bookedVisits);

        verifyCalendarVisitsResponse(
                calendarVisitsResponses,
                false,
                true,
                SUBJECT_LAST_NAME.toUpperCase(),
                expectedStartDateString,
                expectedEndDateString
        );

        // test case #4

        bookedVisit.setSubjectMrn(subjectMrn);
        isHomeView = false;
        visitType.setInpatient(false);
        bookedVisit.setVisitType(visitType);
        calendarVisitsResponses = daoSpy.bookedVisitsDTO(isHomeView, bookedVisits);

        verifyCalendarVisitsResponse(
                calendarVisitsResponses,
                false,
                false,
                SUBJECT_LAST_NAME.toUpperCase(),
                expectedStartDateString,
                expectedEndDateString
        );

        // test case #5

        bookedVisit.setSubjectMrn(null);
        isHomeView = true;
        visitType.setInpatient(true);
        bookedVisit.setVisitType(visitType);
        calendarVisitsResponses = daoSpy.bookedVisitsDTO(isHomeView, bookedVisits);

        verifyCalendarVisitsResponse(
                calendarVisitsResponses,
                true,
                true,
                NO_SUBJECT_ASSIGNED,
                expectedStartDateString,
                expectedEndDateString
        );

        // test case #6

        bookedVisit.setSubjectMrn(null);
        isHomeView = true;
        visitType.setInpatient(false);
        bookedVisit.setVisitType(visitType);
        calendarVisitsResponses = daoSpy.bookedVisitsDTO(isHomeView, bookedVisits);

        verifyCalendarVisitsResponse(
                calendarVisitsResponses,
                false,
                false,
                NO_SUBJECT_ASSIGNED,
                expectedStartDateString,
                expectedEndDateString
        );

        // test case #7

        bookedVisit.setSubjectMrn(null);
        isHomeView = false;
        visitType.setInpatient(true);
        bookedVisit.setVisitType(visitType);
        calendarVisitsResponses = daoSpy.bookedVisitsDTO(isHomeView, bookedVisits);

        verifyCalendarVisitsResponse(
                calendarVisitsResponses,
                false,
                true,
                NO_SUBJECT_ASSIGNED,
                expectedStartDateString,
                expectedEndDateString
        );

        // test case #8

        bookedVisit.setSubjectMrn(null);
        isHomeView = false;
        visitType.setInpatient(false);
        bookedVisit.setVisitType(visitType);
        calendarVisitsResponses = daoSpy.bookedVisitsDTO(isHomeView, bookedVisits);

        verifyCalendarVisitsResponse(
                calendarVisitsResponses,
                false,
                false,
                NO_SUBJECT_ASSIGNED,
                expectedStartDateString,
                expectedEndDateString
        );

    }


    private void verifyCalendarVisitsResponse(
            // TODO: rename variable as 'expectedThisOrThat'
            List<CalendarVisitsResponse> calendarVisitsResponses,
            boolean isAllDay,
            boolean isInpatient,
            String subjectName,
            String expectedStartDateString,
            String expectedEndDateString) {

        assertEquals("response should contain just one element", 1, calendarVisitsResponses.size());
        CalendarVisitsResponse calendarVisitsResponse = calendarVisitsResponses.get(0);

        assertEquals(expectedStartDateString, calendarVisitsResponse.getStart());
        assertEquals(expectedEndDateString, calendarVisitsResponse.getEnd());
        assertEquals(VISIT_TEMPLATE_NAME, calendarVisitsResponse.getVisitName());
        assertEquals(PI_LAST_NAME, calendarVisitsResponse.getPiName());
        assertNotNull(calendarVisitsResponse.getRoom());
        assertEquals(LOCAL_ID, calendarVisitsResponse.getLocalId());
        assertEquals(subjectName, calendarVisitsResponse.getTitle());
        assertEquals(expectedStartDateString, calendarVisitsResponse.getStart());
        assertEquals(expectedEndDateString, calendarVisitsResponse.getEnd());
        assertEquals(SCHEDULED_STATUS_NAME, calendarVisitsResponse.getClassName());
        assertEquals(isAllDay, calendarVisitsResponse.isAllDay());
        assertEquals(isInpatient, calendarVisitsResponse.isInpatientVisitType());

    }

    @Test
    public void testGetSyncedZeroIndexedPageNumber() {

        Date fromDate = new Date(5678);

        testGetSyncedZeroIndexedPageNumberHelper(
                5,
                1,
                0
        );
        testGetSyncedZeroIndexedPageNumberHelper(
                5,
                0,
                0
        );
        testGetSyncedZeroIndexedPageNumberHelper(
                5,
                9,
                1
        );
        testGetSyncedZeroIndexedPageNumberHelper(
                5,
                10,
                1
        );
        testGetSyncedZeroIndexedPageNumberHelper(
                5,
                11,
                2
        );
    }

    public void testGetSyncedZeroIndexedPageNumberHelper(
            int maxResultsPerPage,
            long numResults,
            int expectedPageNumber) {

        AppointmentDAO dao = new AppointmentDAO();
        daoSpy = spy(dao);

        Query mockQuery = mock(Query.class);
        doReturn(mockQuery).when(daoSpy).newQuery(any());

        doReturn(numResults).when(mockQuery).uniqueResult();

        int result = daoSpy.getSyncedZeroIndexedPageNumber(
                maxResultsPerPage,
                mockQuery);

        assertEquals(expectedPageNumber, result);
    }

    @Test
    public void testGetTimeRangeString(){
        AppointmentDAO dao = new AppointmentDAO();

        String startTimeColumnName = "v.startTime";
        String endTimeColumnName = "v.endTime";

        String expectedTimeRangeString = "(" +  endTimeColumnName +" >= :startTime and :endTime >= " +  startTimeColumnName + ")";
        String actualTimeRangeString = dao.getTimeRangeString(startTimeColumnName, endTimeColumnName);

        assertEquals(expectedTimeRangeString, actualTimeRangeString);
    }

    @Test
    public void testListToWhereString(){
        AppointmentDAO dao = new AppointmentDAO();
        List<String> whereClauses = new ArrayList<>();
        String emptyWhereClause = "";
        String actualWhereClause = dao.listToSpaceWhereString(whereClauses);
        assertEquals(emptyWhereClause, actualWhereClause);

        String nameClause = "name like '%Foo%'";
        String stateClause = "(state = 'Florida' or state = 'Texas')";
        whereClauses.add(nameClause);
        whereClauses.add(stateClause);
        String nonEmptyWhereClause = " WHERE (" + nameClause + " and "+ stateClause+ ")";
        actualWhereClause = dao.listToSpaceWhereString(whereClauses);
        assertEquals(nonEmptyWhereClause, actualWhereClause);
    }

    @Test
    public void testUseTimeRangeForBookedVisits(){
        AppointmentDAO dao = new AppointmentDAO();

        Date date = null;
        boolean actualResult = dao.useTimeRangeForBookedVisits(date);
        assertFalse(actualResult);

        date = new Date();
        actualResult = dao.useTimeRangeForBookedVisits(date);
        assertTrue(actualResult);
    }

    @Test
    public void testFindBookedVisitsForStudyList() {
        boolean yesTimeRange = true;
        boolean filterByStudy = true;
        boolean yesSyncTime = true;

        boolean noTimeRange = false;
        boolean noFilterByStudy = false;
        boolean noSyncTime = false;

        String fieldSelect = "select " +
                "bv.id,  bv.scheduledStartTime,  bv.scheduledEndTime,  vt.name,  st.localId,  st.irb,  st.catalystId,  s.firstName,  s.lastName,  sm.mrn,  apSt.name";
        String countSelect = "select count ( * )";
        String fromTables = "from " +
                "BookedVisit bv  join bv.study st  join bv.visitTemplate vt  join bv.appointmentStatus apSt  left join bv.subjectMrn sm  left join sm.subject s with ( s.archivalStatus IS NULL )";
        String orderPhrase = "ORDER BY sort order";
        String range = "(bv.scheduledEndTime >= :startTime and :endTime >= bv.scheduledStartTime)";
        String sync = "bv.scheduledStartTime < (:toDate)";
        String inStudy = "bv.study in (:studyList)";
        String where = "WHERE (";
        String endWhere = ")";
        String and = " and ";
        String space = " ";

        String mainWithRangeStudyOrder =
                fieldSelect + space + fromTables +
                space + where + range + and + inStudy + endWhere + space + orderPhrase;

        String countWithRangeStudy =
                countSelect + space + fromTables +
                space + where + range + and + inStudy + endWhere;

        String mainWithOrder =
                fieldSelect + space + fromTables +
                space + orderPhrase;

        String countWithoutStuff =
                countSelect + space + fromTables;

        String syncWithOrder =
                countSelect + space + fromTables +
                space + where + sync + endWhere +
                space + orderPhrase;

        String mainWithRangeOrder =
                fieldSelect + space + fromTables +
                space + where + range + endWhere +
                space + orderPhrase;

        String countWithStudy =
                countSelect + space + fromTables +
                space + where + inStudy + endWhere;


        String mainWithStudyOrder =
                fieldSelect + space + fromTables +
                space + where + inStudy + endWhere +
                space + orderPhrase;

        String countWithRange =
                countSelect + space + fromTables +
                space + where + range + endWhere;

        String syncWithStudyOrder =
                countSelect + space + fromTables +
                space + where + sync + and + inStudy + endWhere +
                space + orderPhrase;

        testFindBookedVisitsForStudyListHelper(yesTimeRange, filterByStudy, yesSyncTime,
                mainWithRangeStudyOrder,
                countWithRangeStudy,
                null);

        testFindBookedVisitsForStudyListHelper(noTimeRange, noFilterByStudy, yesSyncTime,
                mainWithOrder,
                countWithoutStuff,
                syncWithOrder);

        testFindBookedVisitsForStudyListHelper(yesTimeRange, noFilterByStudy, yesSyncTime,
                mainWithRangeOrder,
                countWithRange,
                null);

        testFindBookedVisitsForStudyListHelper(noTimeRange, filterByStudy, yesSyncTime,
                mainWithStudyOrder,
                countWithStudy,
                syncWithStudyOrder);

        testFindBookedVisitsForStudyListHelper(yesTimeRange, filterByStudy, noSyncTime,
                mainWithRangeStudyOrder,
                countWithRangeStudy,
                null);

        testFindBookedVisitsForStudyListHelper(noTimeRange, noFilterByStudy, noSyncTime,
                mainWithOrder,
                countWithoutStuff,
                null);

        testFindBookedVisitsForStudyListHelper(yesTimeRange, noFilterByStudy, noSyncTime,
                mainWithRangeOrder,
                countWithRange,
                null);

        testFindBookedVisitsForStudyListHelper(noTimeRange, filterByStudy, noSyncTime,
                mainWithStudyOrder,
                countWithStudy,
                null);
    }

    public void testFindBookedVisitsForStudyListHelper(
            boolean useTimeRange,
            boolean filterByStudy,
            boolean useSyncTime,
            String expectedQueryStringMain,
            String expectedQueryStringCount,
            String expectedQueryStringSync) {

        AppointmentDAO dao = new AppointmentDAO();
        daoSpy = spy(dao);

        long mainCount100 = 100L;
        int zeroIndexSynced32 = 32;
        int oneIndexed33 = 33;
        int maxResults20 = 20;
        List<Study> studyList = null;
        Date fromDate = null;
        Date toDate = new Date();
        String sort = "sort";
        String order = "order";

        List<BookedVisitsResponse> bookedVisitsResponseList = Lists.newArrayList();
        List<Object[]> mainResults = Lists.newArrayList();
        List<String> emptyStringList = Lists.newArrayList();

        SearchDTO searchDtoMock = mock(SearchDTO.class);
        when(searchDtoMock.generateSearchClauseList()).thenReturn(emptyStringList);

        Query mockQuery = mock(Query.class);
        doReturn(mainCount100).when(mockQuery).uniqueResult();
        doReturn(mainResults).when(mockQuery).list();

        doReturn(mockQuery).when(daoSpy).newQuery(any());
        doReturn(bookedVisitsResponseList).when(daoSpy).bookedVisitsResponses(
                zeroIndexSynced32, mainResults, mainCount100);
        doReturn(zeroIndexSynced32).when(daoSpy).getSyncedZeroIndexedPageNumber(any(Integer.class), any());

        doReturn(useTimeRange).when(daoSpy).useTimeRangeForBookedVisits(fromDate);
        doReturn(filterByStudy).when(daoSpy).filterByStudyForBookedVisits(studyList);
        doReturn(useSyncTime).when(daoSpy).useSyncTimeForBookedVisits(fromDate, toDate);

        List<BookedVisitsResponse> result = daoSpy.findBookedVisitsForStudyList(
                studyList,
                sort,
                order,
                oneIndexed33,
                maxResults20,
                fromDate,
                toDate,
                searchDtoMock
        );

        verify(daoSpy, times(1)).filterByStudyForBookedVisits(studyList);
        verify(daoSpy, times(1)).useTimeRangeForBookedVisits(fromDate);

        verify(daoSpy, times(1)).newQuery(expectedQueryStringMain);
        verify(daoSpy, times(1)).newQuery(expectedQueryStringCount);

        if (!useTimeRange && useSyncTime) {
            verify(daoSpy, times(1)).newQuery(expectedQueryStringSync);
        }
        verify(mockQuery, times(1)).list();

        assertEquals(bookedVisitsResponseList, result);
    }

    @Test
    public void testFilterByStudyForBookedVisits() {
        AppointmentDAO dao = new AppointmentDAO();

        List<Study> studyList = null;
        boolean actualResult = dao.filterByStudyForBookedVisits(studyList);
        assertFalse(actualResult);

        studyList = new ArrayList<>();
        actualResult = dao.filterByStudyForBookedVisits(studyList);
        assertTrue(actualResult);

        Study study = new Study();
        study.setName("Test Study");
        studyList.add(study);
        actualResult = dao.filterByStudyForBookedVisits(studyList);
        assertTrue(actualResult);
    }

    @Test
    public void testAdjustForDecryptionAndNA() {
        AppointmentDAO dao = new AppointmentDAO();

        String rawValue = null;
        String adjustForNull = "This is my default in case of null";
        String actualResult = dao.adjustForDecryptionAndNA(rawValue, adjustForNull);
        assertEquals(adjustForNull, actualResult);

        rawValue = SubjectDataEncryptor.encrypt("Test Value");
        String decryptedRawValue = SubjectDataEncryptor.decrypt(rawValue);
        actualResult = dao.adjustForDecryptionAndNA(rawValue, adjustForNull);
        assertEquals(decryptedRawValue, actualResult);
    }

    @Test
    public void testBookedVisitsResponses() {
        AppointmentDAO dao = new AppointmentDAO();
        daoSpy = spy(dao);

        Date date1 = new Date();
        Date date2 = new Date();

        doReturn("fname00").when(daoSpy).adjustForDecryptionAndNA("fname0", NO_SUBJECT_ASSIGNED);
        doReturn("lname00").when(daoSpy).adjustForDecryptionAndNA("lname0", NO_SUBJECT_ASSIGNED);
        doReturn("code00").when(daoSpy).adjustForDecryptionAndNA("code0", NA);

        doReturn("fname01").when(daoSpy).adjustForDecryptionAndNA("fname1", NO_SUBJECT_ASSIGNED);
        doReturn("lname01").when(daoSpy).adjustForDecryptionAndNA("lname1", NO_SUBJECT_ASSIGNED);
        doReturn("code01").when(daoSpy).adjustForDecryptionAndNA("code1", NA);

        Object[] row0 = {0, date1, date2, "visit0", "lid0", "irb0", "catId0", "fname0", "lname0", "code0", "status0"};
        Object[] row1 = {1, date1, date2, "visit1", "lid1", "irb1", "catId1", "fname1", "lname1", "code1", "status1"};

        List<Object[]> objects = Lists.newArrayList(row0, row1);


        List<BookedVisitsResponse> result = daoSpy.bookedVisitsResponses(45, objects, 42L);

        verify(daoSpy, times(1)).adjustForDecryptionAndNA("fname0", NO_SUBJECT_ASSIGNED);
        verify(daoSpy, times(1)).adjustForDecryptionAndNA("lname0", NO_SUBJECT_ASSIGNED);
        verify(daoSpy, times(1)).adjustForDecryptionAndNA("code0", NA);

        verify(daoSpy, times(1)).adjustForDecryptionAndNA("fname1", NO_SUBJECT_ASSIGNED);
        verify(daoSpy, times(1)).adjustForDecryptionAndNA("lname1", NO_SUBJECT_ASSIGNED);
        verify(daoSpy, times(1)).adjustForDecryptionAndNA("code1", NA);
        
        assertEquals(2, result.size());

        BookedVisitsResponse result0 = result.get(0);
        BookedVisitsResponse result1 = result.get(1);

        assertEquals((Long)42L, result0.getTotalCount());
        assertEquals(45, result0.getNavigatePage());
        assertEquals(0, result0.getId());
        assertEquals("visit0", result0.getVisitName());
        assertEquals("irb0", result0.getIrb());
        assertEquals("catId0", result0.getCatId());
        assertEquals("lid0", result0.getLocalId());
        assertEquals("fname00", result0.getSubjectFirstName());
        assertEquals("lname00", result0.getSubjectLastName());
        assertEquals("code00", result0.getSubjectMRN());
        assertEquals(date1, result0.getScheduledStartTime());
        assertEquals(date2, result0.getScheduledEndTime());
        assertEquals("status0", result0.getAppointmentStatus());

        assertEquals((Long)42L, result1.getTotalCount());
        assertEquals(45, result1.getNavigatePage());
        assertEquals(1, result1.getId());
        assertEquals("visit1", result1.getVisitName());
        assertEquals("irb1", result1.getIrb());
        assertEquals("catId1", result1.getCatId());
        assertEquals("lid1", result1.getLocalId());
        assertEquals("fname01", result1.getSubjectFirstName());
        assertEquals("lname01", result1.getSubjectLastName());
        assertEquals("code01", result1.getSubjectMRN());
        assertEquals(date1, result1.getScheduledStartTime());
        assertEquals(date2, result1.getScheduledEndTime());
        assertEquals("status1", result1.getAppointmentStatus());

     }
}

