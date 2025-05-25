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
import edu.harvard.catalyst.scheduler.AbstractSpringWiredJunit4Test;
import edu.harvard.catalyst.scheduler.dto.ReportDTO;
import edu.harvard.catalyst.scheduler.dto.TransactionsReportDTO;
import edu.harvard.catalyst.scheduler.dto.WorkloadAndResourceResponseDTO;
import edu.harvard.catalyst.scheduler.dto.response.CancellationsReportResponseDTO;
import edu.harvard.catalyst.scheduler.dto.response.StudyDataReportResponseDTO;
import edu.harvard.catalyst.scheduler.dto.statics.StudyStatusFilter;
import edu.harvard.catalyst.scheduler.entity.*;
import edu.harvard.catalyst.scheduler.util.SubjectDataEncryptor;
import edu.harvard.catalyst.scheduler.util.TestUtils;
import org.hibernate.query.Query;
import org.hibernate.Session;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.security.Key;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static edu.harvard.catalyst.scheduler.core.Statics.NA;
import static edu.harvard.catalyst.scheduler.core.Statics.NO_SUBJECT_ASSIGNED;
import static edu.harvard.catalyst.scheduler.persistence.SortStrategy.ASCENDING;
import static edu.harvard.catalyst.scheduler.persistence.SortStrategy.DESCENDING;
import static java.util.Arrays.asList;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * @author clint
 * @date Aug 6, 2013
 * 
 */
public final class ReportDAOTest extends AbstractSpringWiredJunit4Test {

    @Autowired
    private ReportDAO dao;

    @Autowired @Qualifier("encryptionKey")
    Key encryptionKey;

    @Before
    public void before() {
        SubjectDataEncryptor.setEncryptionKey(encryptionKey);
    }

    /**
     * Test helper methods
     */
    @Test
    public void testOrEmpty() {

        String nonNullInput = "a non-null string";

        String outputForNullInput = dao.orEmpty(null);
        String outputForNonNullInput = dao.orEmpty(nonNullInput);

        assertEquals("Output for null input should be empty string", "", outputForNullInput);
        assertEquals("Output for non-null input should be the same as the input", nonNullInput, outputForNonNullInput);

    }

    @Test
    public void testMakeLevelMapKey() {

        int visitId = 1;
        String levelValue = "someLevelValue";

        String levelMapKey = dao.makeLevelMapKey(visitId, levelValue);

        assertEquals("Visit1LevelsomeLevelValue", levelMapKey);

    }

    @Test
    public void testGetResourceTypes() {

        List<ResourceType> resourceTypes = dao.getResourceTypes();

        assertEquals("There should be exactly 5 resource types", 5, resourceTypes.size());

        assert(resourceTypes.contains(ResourceType.Nursing));
        assert(resourceTypes.contains(ResourceType.Nutrition));
        assert(resourceTypes.contains(ResourceType.Room));
        assert(resourceTypes.contains(ResourceType.Lab));
        assert(resourceTypes.contains(ResourceType.Other));

    }


    @Test
    public void testGetSublocations() {

        Session hibernateSession = session();

        // 		    return sl.getName().equalsIgnoreCase("Non CRC") || sl.getName().equalsIgnoreCase("Off Institution");

        Institution institution1 = new Institution("Inst.1", "Institution number 1");
        hibernateSession.save(institution1);

        Institution institution2 = new Institution("Inst.2", "Institution number 2");
        hibernateSession.save(institution2);


        Sublocation sublocation1a = new Sublocation("sublocation 1a", institution1);
        hibernateSession.save(sublocation1a);

        Sublocation sublocation1b = new Sublocation("NOn CrC", institution1);
        hibernateSession.save(sublocation1b);

        Sublocation sublocation1c = new Sublocation("sublocation 1c", institution1);
        hibernateSession.save(sublocation1c);

        Sublocation sublocation2a = new Sublocation("sublocation 2a", institution2);
        hibernateSession.save(sublocation2a);

        Sublocation sublocation2b = new Sublocation("sublocation 2b", institution2);
        hibernateSession.save(sublocation2b);

        Sublocation sublocation2c = new Sublocation("OFF institution", institution2);
        hibernateSession.save(sublocation2c);


        List<Sublocation> sublocations = dao.getSublocations();

        assertEquals("sublocation list should have exactly 6 elements", 6, sublocations.size());
        assertTrue("sublocation1a should be in the list", sublocations.contains(sublocation1a));
        assertTrue("sublocation1b should be in the list", sublocations.contains(sublocation1b));
        assertTrue("sublocation1c should be in the list", sublocations.contains(sublocation1c));
        assertTrue("sublocation2a should be in the list", sublocations.contains(sublocation2a));
        assertTrue("sublocation2b should be in the list", sublocations.contains(sublocation2b));
        assertTrue("sublocation2c should be in the list", sublocations.contains(sublocation2c));

        // sublocation1a and 2c are the ones we expects to find at the beginningof the list
        int lastIndexOfSublocation1b = sublocations.lastIndexOf(sublocation1b);
        int lastIndexOfSublocation2c = sublocations.lastIndexOf(sublocation2c);

        int firstIndexOfSublocation1a = sublocations.indexOf(sublocation1a);
        int firstIndexOfSublocation1c = sublocations.indexOf(sublocation1c);
        int firstIndexOfSublocation2a = sublocations.indexOf(sublocation2a);
        int firstIndexOfSublocation2b = sublocations.indexOf(sublocation2b);

        // verify that the non-CRC / off-institution sublocations come first.
        // 1b and 2a should come first

        int minIndexOfStandardSubLocations = Math.min(
                Math.min(firstIndexOfSublocation1a, firstIndexOfSublocation1c),
                Math.min(firstIndexOfSublocation2a, firstIndexOfSublocation2b)
        );

        int maxIndexOfOtherSublocations = Math.max(lastIndexOfSublocation1b, lastIndexOfSublocation2c);

        assertTrue("Non-CRC and off-institution sublocations are not all at the end of the list", maxIndexOfOtherSublocations > minIndexOfStandardSubLocations);

    }


    @Test
    public void testGetReportDataById() {

        Session hibernateSession = session();

        Report report = new Report();
        report.setName("my report");
        report.setTitle("my report title");
        report.setDescription("The description of my report");
        hibernateSession.save(report);
        int reportId = report.getId();

        Report retreivedReport = dao.getReportDataById(reportId);
        Report notFoundReport = dao.getReportDataById(reportId + 1);

        // First check that we really roud-tripped the data via the H2 database, i.e. report objects are not the same
        assertEquals(report, retreivedReport);
        assertEquals(report.getName(), retreivedReport.getName());
        assertEquals(report.getDescription(), retreivedReport.getDescription());

        assertNull(notFoundReport);

    }


    /**
     * TODO: get braindump from Ankit because I have no idea how this method works
     */
    @Test
    public void testGetProtoNurseAndNutritionReport() {

        String reportDtoName = "proto_nutritionist"; // alternatively: proto_by_nurse, or anything else -- but not null

        ReportDTO reportDto = new ReportDTO();
        reportDto.setFilterString("");
        reportDto.setFilterId("");
        reportDto.setSortId("");
        reportDto.setName(reportDtoName);

    }


    /**
     * Invoke a DAO method, passing in a ReportDTO built with several input permutations
     * 
     *  NB: For now, just run this method on several permutations of sortId, filterId, 
     *  and filterString values.  Verify that the dao doesn't throw (due to HQL-building
     *  errors, etc) and that the relevant field of the passed-in DTO is correctly mutated. 
     *  This field will always be an empty list for now, since we're not (yet) loading any 
     *  test data.
     *  
     * @param op
     * @throws Exception
     */
    private void doTestDaoOperation(final DaoOp op) throws Exception {
        final List<String> filterIds = asList("0", "1", "2", "3", "4", "5", "6", "7", "", null, "foo");

        final List<String> filterStrings = asList("yes", "no", "0", "", "foo");

        final List<String> sortIds = asList(String.valueOf(ASCENDING.sortId), String.valueOf(DESCENDING.sortId), null, "foo");

        for (final String filterId : filterIds) {
            for (final String filterString : filterStrings) {
                for (final String sortId : sortIds) {
                    final ReportDTO dto = op.setupDto(makeDto(filterId, filterString, sortId));

                    assertNull(op.changedField(dto));

                    op.apply(dto);

                    assertNotNull(op.changedField(dto));

                    assertTrue(op.changedField(dto).isEmpty());
                }
            }
        }
    }

    /**
     * A class to represent a dao method to invoke, and the field of that method's parameter DTO that will be set.
     * 
     * @author clint
     * @date Aug 7, 2013
     *
     */
    private static abstract class DaoOp {
        public abstract void apply(final ReportDTO dto) throws Exception;

        public abstract Collection<?> changedField(final ReportDTO dto) throws Exception;

        /**
         * Override this to add any needed extra data to the DTOs passed to the dao method beyond what makeDto() provides
         * NB: NOOP by default
         * @param dto
         * @return
         */
        public ReportDTO setupDto(final ReportDTO dto) {
            return dto;
        }
    }

    private static ReportDTO makeDto(final String filterId, final String filterString, final String sortId) {
        final ReportDTO dto = new ReportDTO();

        dto.setFilterId(filterId);
        dto.setFilterString(filterString);
        dto.setSortId(sortId);

        dto.setStartTime(new Date());
        dto.setEndTime(new Date());

        return dto;
    }

    // @Test
    public void disabled_testGetBillableResourcesReport() throws Exception {
        // TODO
        fail("TODO");
    }

    // @Test
    public void disabled_testGetReports() throws Exception {
        // TODO
        fail("TODO");
    }

    // @Test
    public void disabled_testGetResourceTypes() throws Exception {
        // TODO
        fail("TODO");
    }

    //@Test
    public void disabled_testGetSublocations() throws Exception {
        // TODO
        fail("TODO");
    }

    @Test
    public void testGetUserDataReport() throws Exception {
        doTestDaoOperation(new DaoOp() {
            public Collection<?> changedField(final ReportDTO dto) throws Exception {
                return dto.getUserDataReport();
            }

            public void apply(final ReportDTO dto) throws Exception {
                dao.getUserDataReport(dto);
            }
        });
    }

    //@Test
    public void disabled_testGetStaffAuditSubjectViewsReport() throws Exception {
        //TODO
        fail("TODO");
    }

    @Test
    public void testGetSubjectAuditStaffViewsReport() throws Exception {
        doTestDaoOperation(new DaoOp() {
            public Collection<?> changedField(final ReportDTO dto) throws Exception {
                return dto.getSubjectAuditStaffViewsReport();
            }

            public void apply(final ReportDTO dto) throws Exception {
                dao.getSubjectAuditStaffViewsReport(dto);
            }

            @Override
            public ReportDTO setupDto(final ReportDTO dto) {

                dto.setMrn("12345");

                return dto;
            }
        });
    }

    @Test
    public void testGetOverrideReport() throws Exception {
        doTestDaoOperation(new DaoOp() {
            public Collection<?> changedField(final ReportDTO dto) throws Exception {
                return dto.getOverrideReport();
            }

            public void apply(final ReportDTO dto) throws Exception {
                dao.getOverrideReport(dto);
            }
        });
    }

    @Test
    public void testGetSubjectPurgeReport() throws Exception {
        doTestDaoOperation(new DaoOp() {
            public Collection<?> changedField(final ReportDTO dto) throws Exception {
                return dto.getSubjectPurgeReport();
            }

            public void apply(final ReportDTO dto) throws Exception {
                dao.getSubjectPurgeReport(dto);
            }
        });
    }

    @Test
    public void testGetOffUnitReport() throws Exception {
        doTestDaoOperation(new DaoOp() {
            public Collection<?> changedField(final ReportDTO dto) throws Exception {
                return dto.getOffUnitReport();
            }

            public void apply(final ReportDTO dto) throws Exception {
                dao.getOffUnitReport(dto);
            }
        });
    }

    @Test
    public void testGetDailyOverviewReport() throws Exception {
        doTestDaoOperation(new DaoOp() {
            public Collection<?> changedField(final ReportDTO dto) throws Exception {
                return dto.getDailyOverviewReport();
            }

            public void apply(final ReportDTO dto) throws Exception {
                dao.getDailyOverviewReport(dto);
            }
        });
    }

    @Test
    public void testGetExportDailyOverviewReport() throws Exception {
        doTestDaoOperation(new DaoOp() {
            public Collection<?> changedField(final ReportDTO dto) throws Exception {
                return dto.getNursingAndRoomDailyOverviewReport();
            }

            public void apply(final ReportDTO dto) throws Exception {
                dao.getExportDailyOverviewReport(dto);
            }
        });
    }

    @Test
    public void testGetWeeklyPharmReport() throws Exception {
        doTestDaoOperation(new DaoOp() {
            public Collection<?> changedField(final ReportDTO dto) throws Exception {
                return dto.getWeeklyPharmReport();
            }

            public void apply(final ReportDTO dto) throws Exception {
                dao.getWeeklyPharmReport(dto);
            }
        });
    }

    @Test
    public void testGetStudyVisitLocationReport() throws Exception {
        doTestDaoOperation(new DaoOp() {
            public Collection<?> changedField(final ReportDTO dto) throws Exception {
                return dto.getStudyVisitLocationReport();
            }

            public void apply(final ReportDTO dto) throws Exception {
                dao.getStudyVisitLocationReport(dto);
            }
        });
    }

    @Test
    public void testGetBillableResourcesReport() throws Exception {
        doTestDaoOperation(new DaoOp() {
            public Collection<?> changedField(final ReportDTO dto) throws Exception {
                return dto.getBillableResourcesReport();
            }

            public void apply(final ReportDTO dto) throws Exception {
                dao.getBillableResourcesReport(dto);
            }
        });
    }

    @Test
    public void testGetTransactionsReport() throws Exception {
        doTestDaoOperation(new DaoOp() {
            public Collection<?> changedField(final ReportDTO dto) throws Exception {
                return dto.getTransactionsReport();
            }

            public void apply(final ReportDTO dto) throws Exception {
                dao.getTransactionsReport(dto);
            }
        });
    }

    @Test
    public void testMakeTransactionsReportRowFromQueryResult() {

        Date now = new Date();
        long nowTime = now.getTime();

        // the following are the values expected in the DTO
        Integer asrId = 1;
        String asrName = "asr Name";
        Date cancelTime = new Date(nowTime + 1000);
        String ecommonsId = "eCommons Id";
        String userLastName = "user last name";
        String studyName = "study name";
        String piFirstName = "PI first name";
        String piMiddleName = "PI middle name";
        String piLastName = "PI last name";
        String psFirstName = "PS first name";
        String psMiddleName = "PS middle name";
        String psLastName = "PS last name";
        String catalystId = "Catalyst id";
        String localId = "local ID";
        String irb = "IRB";
        String visitTemplateName = "visit template name";
        VisitType visitType = new VisitType();
        visitType.setName(TestUtils.InpatientCRC);

        String subjectFirstName = "subject first name";
        String subjectMiddleName = "subject middle name";
        String subjectLastName = "subject last name";
        String mrn = "MRN";
        String appointmentStatusStr = "appointment status";
        String cancellationStatusStr = "cancellation status";
        String cancellationStatusReasonStr = "cancellation status reason";
        Date scheduledStartTime = new Date(nowTime + 4000);
        Date scheduledEndTime =  new Date(nowTime + 5000);

        // Here we build the input to the method under test

        User user = new User();
        user.setEcommonsId(ecommonsId);
        user.setLastName(userLastName);

        User pi = new User();
        pi.setFirstName(piFirstName);
        pi.setMiddleName(piMiddleName);
        pi.setLastName(piLastName);

        User scheduler = new User();
        scheduler.setFirstName(psFirstName);
        scheduler.setMiddleName(psMiddleName);
        scheduler.setLastName(psLastName);

        Study study = new Study();
        study.setName(studyName);
        study.setInvestigator(pi);
        study.setScheduler(scheduler);
        study.setCatalystId(catalystId);
        study.setLocalId(localId);
        study.setIrb(irb);

        Subject subject = new Subject();
        subject.setFirstName(subjectFirstName);
        subject.setMiddleName(subjectMiddleName);
        subject.setLastName(subjectLastName);

        String encryptedMrn = SubjectDataEncryptor.encrypt(mrn);
        SubjectMrn subjectMrn = new SubjectMrn(
                subject, encryptedMrn, null, null
        );

        VisitTemplate visitTemplate = new VisitTemplate();
        visitTemplate.setName(visitTemplateName);

        AppointmentStatus appointmentStatus = new AppointmentStatus();
        appointmentStatus.setName(appointmentStatusStr);

        CancellationStatus cancellationStatus = new CancellationStatus();
        cancellationStatus.setName(cancellationStatusStr);

        AppointmentStatusReason appointmentStatusReason = new AppointmentStatusReason();
        appointmentStatusReason.setName(asrName);

        AppointmentStatusReason cancellationStatusReason = new AppointmentStatusReason();
        cancellationStatusReason.setName(cancellationStatusReasonStr);

        AppointmentStatusReason checkoutStatusReason = new AppointmentStatusReason();

        BookedVisit bookedVisit = new BookedVisit(
                study, visitTemplate, null, visitType,
                subjectMrn, appointmentStatus, cancellationStatus, appointmentStatusReason,
                cancellationStatusReason, checkoutStatusReason,
                scheduledStartTime, scheduledEndTime, null,
                null, null, null,
                null, null,
                0, 0, 0, 0, null,
                null, null, null,
                null, null,
                null, null
        );
        bookedVisit.setId(asrId);
        bookedVisit.setAppointmentStatusReason(appointmentStatusReason);

        Object[] row = new Object[3];
        row[0] = bookedVisit;
        row[1] = user;
        row[2] = cancelTime;

        TransactionsReportDTO dto = dao.makeTransactionsReportRowFromQueryResult(row);

        assertEquals(asrId, dto.getAsrId());
        assertEquals(asrName, dto.getAsrName());
        assertEquals(cancelTime, dto.getCancelTime());
        assertEquals(ecommonsId + " - " + userLastName, dto.getEcommonsId());
        // apparently check-in and check0out time are not set
        assertEquals(null, dto.getCheckInTime());
        assertEquals(null, dto.getCheckOutTime());
        assertEquals(studyName, dto.getStudyName());
        assertEquals(piFirstName, dto.getPiFirstName());
        assertEquals(piMiddleName, dto.getPiMiddleName());
        assertEquals(piLastName, dto.getPiLastName());
        assertEquals(psFirstName, dto.getPsFirstName());
        assertEquals(psMiddleName, dto.getPsMiddleName());
        assertEquals(psLastName, dto.getPsLastName());
        assertEquals(catalystId, dto.getCatalystId());
        assertEquals(localId, dto.getLocalId());
        assertEquals(irb, dto.getIrb());
        assertEquals(visitTemplateName, dto.getVisitName());
        assertEquals(visitType.getName(), dto.getVisitTypeName());
        assertEquals(subjectFirstName, dto.getSubjectFirstName());
        assertEquals(subjectMiddleName, dto.getSubjectMiddleName());
        assertEquals(subjectLastName, dto.getSubjectLastName());
        assertEquals(mrn, dto.getMrn());
        assertEquals(appointmentStatusStr, dto.getAppointmentStatus());
        assertEquals(cancellationStatusStr, dto.getCancelStatus());
        assertEquals(cancellationStatusReasonStr, dto.getCancelStatusReason());
        assertEquals(scheduledStartTime, dto.getScheduledStartTime());
        assertEquals(scheduledEndTime, dto.getScheduledEndTime());

        // now try a blocked visit (i.e. subjectMrn is null)
        bookedVisit.setSubjectMrn(null);

        dto = dao.makeTransactionsReportRowFromQueryResult(row);

        assertEquals(asrId, dto.getAsrId());
        assertEquals(asrName, dto.getAsrName());
        assertEquals(cancelTime, dto.getCancelTime());
        assertEquals(ecommonsId + " - " + userLastName, dto.getEcommonsId());
        assertEquals(null, dto.getCheckInTime());
        assertEquals(null, dto.getCheckOutTime());
        assertEquals(studyName, dto.getStudyName());
        assertEquals(piFirstName, dto.getPiFirstName());
        assertEquals(piMiddleName, dto.getPiMiddleName());
        assertEquals(piLastName, dto.getPiLastName());
        assertEquals(psFirstName, dto.getPsFirstName());
        assertEquals(psMiddleName, dto.getPsMiddleName());
        assertEquals(psLastName, dto.getPsLastName());
        assertEquals(catalystId, dto.getCatalystId());
        assertEquals(localId, dto.getLocalId());
        assertEquals(irb, dto.getIrb());
        assertEquals(visitTemplateName, dto.getVisitName());
        assertEquals(visitType.getName(), dto.getVisitTypeName());
        assertEquals("", dto.getSubjectFirstName());
        assertEquals("", dto.getSubjectMiddleName());
        assertEquals(NO_SUBJECT_ASSIGNED, dto.getSubjectLastName());
        assertEquals(NA, dto.getMrn());
        assertEquals(appointmentStatusStr, dto.getAppointmentStatus());
        assertEquals(cancellationStatusStr, dto.getCancelStatus());
        assertEquals(cancellationStatusReasonStr, dto.getCancelStatusReason());
        assertEquals(scheduledStartTime, dto.getScheduledStartTime());
        assertEquals(scheduledEndTime, dto.getScheduledEndTime());
    }


    @Test
    public void testGetDailyResourceReport() throws Exception {
        doTestDaoOperation(new DaoOp() {
            public Collection<?> changedField(final ReportDTO dto) throws Exception {
                return dto.getDailyResourceReport();
            }

            public void apply(final ReportDTO dto) throws Exception {
                dao.getDailyResourceReport(dto, false);
            }
        });
    }

    @Test
    public void testGetExportDailyResourceReport() throws Exception {
        doTestDaoOperation(new DaoOp() {
            public Collection<?> changedField(final ReportDTO dto) throws Exception {
                return dto.getDailyResourceReport();
            }

            public void apply(final ReportDTO dto) throws Exception {
                dao.getDailyResourceReport(dto, true);
            }
        });
    }

    @Test
    public void testGetDailyAdmReport() throws Exception {
        doTestDaoOperation(new DaoOp() {
            public Collection<?> changedField(final ReportDTO dto) throws Exception {
                return dto.getDailyAdmReport();
            }

            public void apply(final ReportDTO dto) throws Exception {
                dao.getDailyAdmReport(dto);
            }
        });
    }

    @Test
    public void testGetMetaKitchenReport() throws Exception {
        final List<String> reportStrings = asList("meta_kitchen", "meta_kitchen_by_time", "");

            doTestDaoOperation(new DaoOp() {
                public Collection<?> changedField(final ReportDTO dto) throws Exception {
                    return dto.getMetaKitchenReport();
                }

                public void apply(final ReportDTO dto) throws Exception {
                    for (String reportName: reportStrings){
                        dto.setName(reportName); 
                        dao.getMetaKitchenReport(dto);
                    }        
                }
            });

    }

    @Test
    public void testGetDeptAndPiReport() throws Exception {
        doTestDaoOperation(new DaoOp() {
            public Collection<?> changedField(final ReportDTO dto) throws Exception {
                return dto.getDeptAndPiReport();
            }

            public void apply(final ReportDTO dto) throws Exception {
                dao.getDeptAndPiReport(dto);
            }
        });
    }

    @Test
    public void testGetBillingReport() throws Exception {
        doTestDaoOperation(new DaoOp() {
            public Collection<?> changedField(final ReportDTO dto) throws Exception {
                return dto.getBillingReport();
            }

            public void apply(final ReportDTO dto) throws Exception {
                dao.getBillingReport(dto);
            }
        });
    }

    @Test
    public void testGetBillingByInvestigatorReport() throws Exception {
        doTestDaoOperation(new DaoOp() {
            public Collection<?> changedField(final ReportDTO dto) throws Exception {
                return dto.getBillingByInvestigatorReport();
            }

            public void apply(final ReportDTO dto) throws Exception {
                dao.getBillingByInvestigatorReport(dto);
            }
        });
    }

    @Test
    public void testGetCensusReport() throws Exception {
        doTestDaoOperation(new DaoOp() {
            public Collection<?> changedField(final ReportDTO dto) throws Exception {
                return dto.getCensusReport();
            }

            public void apply(final ReportDTO dto) throws Exception {
                dao.getCensusReport(dto);
            }
        });
    }

    @Test
    public void testGetLevelOfServiceReport() throws Exception {
        doTestDaoOperation(new DaoOp() {
            public Collection<?> changedField(final ReportDTO dto) throws Exception {
                return dto.getLevelOfServiceReport();
            }

            public void apply(final ReportDTO dto) throws Exception {
                dao.getLevelOfServiceReport(dto);
            }
        });
    }

    @Test
    public void testGetStudySubjectVisitReport() throws Exception {
        doTestDaoOperation(new DaoOp() {
            public Collection<?> changedField(final ReportDTO dto) throws Exception {
                return dto.getStudySubjectVisitReport();
            }

            public void apply(final ReportDTO dto) throws Exception {
                dao.getStudySubjectVisitReport(dto);
            }

            @Override
            public ReportDTO setupDto(final ReportDTO dto) {

                dto.setSubjectLastName("Smith");
                dto.setSubjectMRN("12345");
                dto.setSubjectDOB("01/01/1970");

                return dto;
            }
        });
    }

    @Test
    public void testGetStudyStatusChangeReport() throws Exception {
        doTestDaoOperation(new DaoOp() {
            public Collection<?> changedField(final ReportDTO dto) throws Exception {
                return dto.getStudyStatusChangeReport();
            }

            public void apply(final ReportDTO dto) throws Exception {
                dao.getStudyStatusChangeReport(dto);
            }
        });
    }

    //FIXME: TEMPORARILY disabled, as this fails for some inputs.  Is this expected, or a bug? 
    //@Test
    public void testGetSubjectVisitHistoryReport() throws Exception {
        doTestDaoOperation(new DaoOp() {
            public Collection<?> changedField(final ReportDTO dto) throws Exception {
                return dto.getSubjectVisitHistoryReport();
            }

            public void apply(final ReportDTO dto) throws Exception {
                dao.getSubjectVisitHistoryReport(dto);
            }

            @Override
            public ReportDTO setupDto(final ReportDTO dto) {

                dto.setSubjectLastName("Smith");
                dto.setSubjectMRN("12345");
                dto.setSubjectDOB("1970-01-01");

                return dto;
            }
        });
    }

    //FIXME: TEMPORARILY disabled, as this fails for some inputs.  Is this expected, or a bug?
    //@Test
    public void testGetCRCAvailabilityReport() throws Exception {
        doTestDaoOperation(new DaoOp() {
            public Collection<?> changedField(final ReportDTO dto) throws Exception {
                return dto.getCrcAvailabilityReport();
            }

            public void apply(final ReportDTO dto) throws Exception {
                dao.getCRCAvailabilityReport(dto);
            }
        });
    }

    @Test
    public void testGetAncillaryOnlyByProtocolReport() throws Exception {
        doTestDaoOperation(new DaoOp() {
            public Collection<?> changedField(final ReportDTO dto) throws Exception {
                return dto.getAncillaryOnlyByProtocolReport();
            }

            public void apply(final ReportDTO dto) throws Exception {
                dao.getAncillaryOnlyByProtocolReport(dto);
            }
        });
    }

    @Test
    public void testGetResourceLevelOfServiceReport() throws Exception {

        List<WorkloadAndResourceResponseDTO> result = dao.getResourceLevelOfServiceReport(new ReportDTO());

        TestUtils.assertNonNullAndHasThisMany(result, 0);

    }

    @Test
    public void testGetVisitTemplateReport() throws Exception {
        doTestDaoOperation(new DaoOp() {
            public Collection<?> changedField(final ReportDTO dto) throws Exception {
                return dto.getVisitTemplateReport();
            }

            public void apply(final ReportDTO dto) throws Exception {
                dao.getVisitTemplateReport(dto);
            }
        });
    }

    @Test
    public void testGetVisitDurationByVisitTypeReport() throws Exception {
        doTestDaoOperation(new DaoOp() {
            public Collection<?> changedField(final ReportDTO dto) throws Exception {
                return dto.getVisitDurationByVisitType();
            }

            public void apply(final ReportDTO dto) throws Exception {
                dao.getVisitDurationByVisitTypeReport(dto);
            }
        });
    }

    @Test
    public void testGetBookedVisitServiceLevelByVisitTypeReport() throws Exception {
        doTestDaoOperation(new DaoOp() {
            public Collection<?> changedField(final ReportDTO dto) throws Exception {
                return dto.getBookedVisitServiceLevelByTypeReport();
            }

            public void apply(final ReportDTO dto) throws Exception {
                dao.getBookedVisitServiceLevelByVisitTypeReport(dto);
            }
        });
    }

    @Test
    public void testGetStaffAuditSubjectViewsReport() throws Exception {
        doTestDaoOperation(new DaoOp() {
            public Collection<?> changedField(final ReportDTO dto) throws Exception {
                return dto.getStaffAuditSubjectViewsReport();
            }

            public void apply(final ReportDTO dto) throws Exception {
                User userMock = mock(User.class);
                when(userMock.getInstitutionRole()).thenReturn(TestInstitutionRoles.SuperAdmin);

                dao.getStaffAuditSubjectViewsReport(dto, userMock);

                when(userMock.getInstitutionRole()).thenReturn(TestInstitutionRoles.FrontDesk);
            }
        });
    }

    @Test
    public void test_addTransactionsDtoList() {
        ReportDAO reportDaoSpy = spy(new ReportDAO());
        Query mockQuery = mock(Query.class);
        Session mockSession = mock(Session.class);

        TransactionsReportDTO resultDto = new TransactionsReportDTO();
        Object[] resultRows1 = { "item1" };
        Object[] resultRows2 = { "item2" };
        String queryString = "q1";

        when(mockSession.createQuery(anyString())).thenReturn(mockQuery);
        when(mockQuery.list()).thenReturn(Lists.newArrayList(resultRows1, resultRows2));
        when(mockQuery.getQueryString()).thenReturn(queryString);

        doReturn(mockSession).when(reportDaoSpy).session();
        doReturn(resultDto).when(reportDaoSpy)
                .makeTransactionsReportRowFromQueryResult(any());

        List<TransactionsReportDTO> transactionsReportDTOList = Lists.newArrayList();

        ReportDTO reportDTO = new ReportDTO();
        reportDTO.setStartTime(new Date());
        reportDTO.setEndTime(new Date());

        reportDaoSpy.addTransactionsDtoList(
                queryString,
                reportDTO,
                transactionsReportDTOList);

        verify(mockSession, times(1)).createQuery(queryString);
        verify(mockQuery, times(1)).list();
        verify(reportDaoSpy, times(2))
                .makeTransactionsReportRowFromQueryResult(any());

        assertEquals(2, transactionsReportDTOList.size());
        assertEquals(resultDto, transactionsReportDTOList.get(0));
        assertEquals(resultDto, transactionsReportDTOList.get(1));
    }


    ///////////////////////////////////////////////////
    //////// 2.9 reports //////////////////////////////
    ///////////////////////////////////////////////////

    @Test
    public void testGetStudyDataReport1() throws Exception {
        Optional<String> bogus = Optional.of("bogus-bogus");
        Optional<String> absent = Optional.empty();
        Optional<StudyStatusFilter> absentStudyStatusFilter = Optional.empty();

        List<StudyDataReportResponseDTO> result = dao.getStudyDataReport(
                bogus, bogus, absent, absentStudyStatusFilter, absent);

        TestUtils.assertNonNullAndHasThisMany(result, 0);

    }
    @Test
    public void testGetCancellationsReport1() throws Exception {
        Optional<String> bogus = Optional.of("bogus-bogus");
        Optional<String> absent = Optional.empty();

        Date now = new Date();
        List<CancellationsReportResponseDTO> result = dao.getCancellationsReport(
                bogus, bogus, absent, absent, absent, absent, now, now);

        TestUtils.assertNonNullAndHasThisMany(result, 0);
    }

    @Test
    public void testAssembleInvestigatorName() {
        User userMock = mock(User.class);

        when(userMock.getLastName()).thenReturn("last");
        when(userMock.getFirstName()).thenReturn("first");

        String name = dao.assembleInvestigatorName(userMock);

        assertEquals("(first , last)", name);

        userMock = null;
        name = dao.assembleInvestigatorName(userMock);

        assertEquals(" ", name);
    }


    // pre-refactoring regression test
    @Test
    public void testSetOverrideReportFilterClause() {


        assertEquals(" and al.performingUser.ecommonsId like '%AAA%' ", dao.setOverrideReportFilterClause("AAA", "1"));
        assertEquals(" and al.bookedVisit.visitTemplate.name like '%AAA%' ", dao.setOverrideReportFilterClause("AAA", "2"));
        assertEquals(" and al.bookedVisit.study.localId like '%AAA%' ", dao.setOverrideReportFilterClause("AAA", "3"));
        assertEquals(" and al.appointmentOverrideReason.id like '%1%' ", dao.setOverrideReportFilterClause("AAA", "4"));
        assertEquals(" and al.appointmentOverrideReason.id like '%2%' ", dao.setOverrideReportFilterClause("AAA", "5"));
        assertEquals(" and al.appointmentOverrideReason.id like '%3%' ", dao.setOverrideReportFilterClause("AAA", "6"));
        assertEquals(" and al.appointmentOverrideReason.id like '%4%' ", dao.setOverrideReportFilterClause("AAA", "7"));
        assertEquals(" and al.appointmentOverrideReason.id like '%5%' ", dao.setOverrideReportFilterClause("AAA", "8"));
        assertEquals(" and al.appointmentOverrideReason.id like '%6%' ", dao.setOverrideReportFilterClause("AAA", "9"));
        assertEquals(" and al.appointmentOverrideReason.id like '%7%' ", dao.setOverrideReportFilterClause("AAA", "10"));


    }

}
