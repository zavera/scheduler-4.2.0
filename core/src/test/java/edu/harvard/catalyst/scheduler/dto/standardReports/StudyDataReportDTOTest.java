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
package edu.harvard.catalyst.scheduler.dto.standardReports;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import edu.harvard.catalyst.scheduler.dto.response.StudyDataReportResponseDTO;
import edu.harvard.catalyst.scheduler.entity.*;
import org.junit.Before;
import org.junit.Test;

import java.util.Date;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;

/**
 * @author carl
 *
 */
public class StudyDataReportDTOTest {
    private User prNurse;
    private User assNurse;
    private User prNutr;
    private User assNutr;

    private User sched1;
    private User secSched1;
    private User thirdSched1;
    
    private User invest1;
    private User doc1;
    private User secDoc1;

    private Study s1;
    private Study s2;

    private Set<StudyFundingSource> sfsSet1;
    private Set<StudyFundingSource> sfsSet2;

    @Before
    public void setup() {
        prNurse = new User();
        assNurse = new User();
        prNutr = new User();
        assNutr = new User();

        prNurse.setFirstName("fprNurse");
        prNurse.setMiddleName("mprNurse");
        prNurse.setLastName("lprNurse");

        assNurse.setFirstName("fassNurse");
        assNurse.setMiddleName("massNurse");
        assNurse.setLastName("lassNurse");

        prNutr.setFirstName("fprNutr");
        prNutr.setMiddleName("mprNutr");
        prNutr.setLastName("lprNutr");

        assNutr.setFirstName("fassNutr");
        assNutr.setMiddleName("massNutr");
        assNutr.setLastName("lassNutr");

        sched1 = new User();
        secSched1 = new User();
        thirdSched1 = new User();

        sched1.setFirstName("fsched1");
        sched1.setMiddleName("msched1");
        sched1.setLastName("lsched1");
        sched1.setEmail("esched1");
        sched1.setPrimaryPhone("psched1");

        secSched1.setFirstName("fsecSched1");
        secSched1.setMiddleName("msecSched1");
        secSched1.setLastName("lsecSched1");
        secSched1.setEmail("esecSched1");
        secSched1.setPrimaryPhone("psecSched1");

        thirdSched1.setFirstName("fthirdSched1");
        thirdSched1.setMiddleName("mthirdSched1");
        thirdSched1.setLastName("lthirdSched1");
        thirdSched1.setEmail("ethirdSched1");
        thirdSched1.setPrimaryPhone("thirdSched1");

        invest1 = new User();
        doc1 = new User();
        secDoc1 = new User();

        invest1.setFirstName("finvest1");
        invest1.setMiddleName("minvest1");
        invest1.setLastName("linvest1");
        invest1.setEmail("einvest1");
        invest1.setPrimaryPhone("pinvest1");
        invest1.setPager("ginvest1");

        doc1.setFirstName("fdoc1");
        doc1.setMiddleName("mdoc1");
        doc1.setLastName("ldoc1");
        doc1.setEmail("edoc1");
        doc1.setPrimaryPhone("pdoc1");
        doc1.setPager("gdoc1");

        secDoc1.setFirstName("fsecDoc1");
        secDoc1.setMiddleName("msecDoc1");
        secDoc1.setLastName("lsecDoc1");
        secDoc1.setEmail("esecDoc1");
        secDoc1.setPrimaryPhone("psecDoc1");
        secDoc1.setPager("gsecDoc1");

        /////////////////////

        s1 = new Study();
        s2 = new Study();

        s1.setId(1);
        s2.setId(2);

        Date date11 = new Date(60000);
        Date date12 = new Date(120000);
        Date date13 = new Date(180000);
        Date date14 = new Date(240000);
        Date date15 = new Date(300000);
        Date date16 = new Date(360000);
        Date date17 = new Date(420000);
        Date date18 = new Date(480000);
        Date date19 = new Date(540000);
        Date date1A = new Date(600000);

        Date date21 = new Date(260000);
        Date date22 = new Date(2120000);
        Date date23 = new Date(2180000);
        Date date24 = new Date(2240000);
        Date date25 = new Date(2300000);
        Date date26 = new Date(2360000);
        Date date27 = new Date(2420000);
        Date date28 = new Date(2480000);
        Date date29 = new Date(2540000);
        Date date2A = new Date(2600000);

        FundingSource fs11 = new FundingSource();
        fs11.setName("fs11");
        FundingSource fs12 = new FundingSource();
        fs12.setName("fs12");
        FundingSource fs13 = new FundingSource();
        fs13.setName("fs13");
        FundingSource fs14 = new FundingSource();
        fs14.setName("fs14");

        FundingSource fs21 = new FundingSource();
        fs21.setName("fs21");
        FundingSource fs22 = new FundingSource();
        fs22.setName("fs22");
        FundingSource fs23 = new FundingSource();
        fs23.setName("fs23");
        FundingSource fs24 = new FundingSource();
        fs24.setName("fs24");

        CentersAndInstitutions cai11 = new CentersAndInstitutions();
        cai11.setName("cai11");
        CentersAndInstitutions cai12 = new CentersAndInstitutions();
        cai12.setName("cai12");
        CentersAndInstitutions cai13 = new CentersAndInstitutions();
        cai13.setName("cai13");
        CentersAndInstitutions cai14 = new CentersAndInstitutions();
        cai13.setName("cai14");

        CentersAndInstitutions cai21 = new CentersAndInstitutions();
        cai21.setName("cai21");
        CentersAndInstitutions cai22 = new CentersAndInstitutions();
        cai22.setName("cai22");
        CentersAndInstitutions cai23 = new CentersAndInstitutions();
        cai23.setName("cai23");
        CentersAndInstitutions cai24 = new CentersAndInstitutions();
        cai23.setName("cai24");

        StudyFundingSource sfs11 = new StudyFundingSource(s1, fs11, cai11, 1, "c1", "g1", "cc11", 1.1D, 1.11D, date19, date1A);
        sfs11.setId(11);
        StudyFundingSource sfs12 = new StudyFundingSource(s1, fs12, cai12, 2, "c2", "g2", "cc12", 1.2D, 1.22D, date19, date1A);
        sfs12.setId(12);
        StudyFundingSource sfs13 = new StudyFundingSource(s1, fs13, cai13, 3, "c3", "g3", "cc13", 1.3D, 1.33D, date19, date1A);
        sfs13.setId(13);
        StudyFundingSource sfs14 = new StudyFundingSource(s1, fs14, cai14, 4, "c4", "g4", "cc14", 1.4D, 1.44D, date19, date1A);
        sfs14.setId(14);

        StudyFundingSource sfs21 = new StudyFundingSource(s1, fs21, cai21, 1, "c1", "g1", "cc11", 1.1D, 2.11D, date29, date2A);
        sfs21.setId(21);
        StudyFundingSource sfs22 = new StudyFundingSource(s1, fs22, cai22, 2, "c2", "g2", "cc12", 1.2D, 2.22D, date29, date2A);
        sfs22.setId(22);
        StudyFundingSource sfs23 = new StudyFundingSource(s1, fs23, cai23, 3, "c3", "g3", "cc13", 1.3D, 2.33D, date29, date2A);
        sfs23.setId(23);
        StudyFundingSource sfs24 = new StudyFundingSource(s1, fs24, cai24, 4, "c4", "g4", "cc14", 1.4D, 2.44D, date29, date2A);
        sfs24.setId(24);

        sfsSet1 = Sets.newHashSet(sfs11, sfs12, sfs13, sfs14);
        sfsSet2 = Sets.newHashSet(sfs21, sfs22, sfs23, sfs24);
       
        StudyStatus studyStatus1 = new StudyStatus();
        studyStatus1.setShortName("status1");
        StudyStatus studyStatus2 = new StudyStatus();
        studyStatus2.setShortName("status2");
        
        Institution institution = new Institution();
        institution.setLongName("inst");
        
        IRBInstitution irbInstitution = new IRBInstitution();
        irbInstitution.setName("irbInst");

        s1.setCatalystId("ci1");
        s1.setLocalId("li1");
        s1.setIrb("i1");
        s1.setIrbExpiration(date11);
        s1.setIrbRenewalDate(date12);
        s1.setIrbApprovalDate(date13);
        s1.setStatusChange(date14);
        s1.setFirstVisitDate(date15);
        s1.setLastScheduledVisitDate(date16);
        s1.setName("n1");
        s1.setIndustryInitiated(true);
        s1.setTotalSubjects(1);
        s1.setTotalOutpatientVisits(1);

        s1.setStudyStatus(studyStatus1);
        s1.setInstitution(institution);
        s1.setIrbInstitution(irbInstitution);
        s1.setInvestigator(invest1);
        s1.setPhysician(doc1);
        s1.setPhysician2(secDoc1);
        s1.setScheduler(sched1);
        s1.setSecondaryScheduler(secSched1);
        s1.setScheduler3(thirdSched1);
        s1.setProtocolNurse(prNurse);
        s1.setAssociateNurse(assNurse);
        s1.setProtocolNutritionist(prNutr);
        s1.setAssociateProtocolNutritionist(assNutr);

        s1.setExpectedStartDate(date16);
        s1.setExpectedEndDate(date17);
        s1.setTotalInpatientVisits(1);
        s1.setCrcCategory("cat1");
        s1.setCrcFunded(true);

        s2.setCatalystId("ci2");
        s2.setLocalId("li2");
        s2.setIrb("i2");
        s2.setIrbExpiration(date21);
        s2.setIrbRenewalDate(date22);
        s2.setIrbApprovalDate(date23);
        s2.setStatusChange(date24);
        s2.setFirstVisitDate(date25);
        s2.setLastScheduledVisitDate(date26);
        s2.setName("n2");
        s2.setIndustryInitiated(false);
        s2.setTotalSubjects(2);
        s2.setTotalOutpatientVisits(2);

        s2.setStudyStatus(studyStatus2);
        s2.setInstitution(institution);
        s2.setIrbInstitution(irbInstitution);
        s2.setInvestigator(invest1);
        s2.setPhysician(doc1);
        s2.setPhysician2(secDoc1);
        s2.setScheduler(sched1);
        s2.setSecondaryScheduler(secSched1);
        s2.setScheduler3(thirdSched1);
        s2.setProtocolNurse(prNurse);
        s2.setAssociateNurse(assNurse);
        s2.setProtocolNutritionist(prNutr);
        s2.setAssociateProtocolNutritionist(assNutr);

        s2.setExpectedStartDate(date26);
        s2.setExpectedEndDate(date27);
        s2.setTotalInpatientVisits(2);
        s2.setCrcCategory("cat2");
        s2.setCrcFunded(false);
    }

    @Test
    public void toCsvHeadersTest() {
        StudyDataReportResponseDTO dto1 = new StudyDataReportResponseDTO(s1, sfsSet1);

        String result = dto1.toCsvHeaders();

        assertEquals("Study Name," +
                "Institution," +
                "Local ID," +
                "Catalyst ID," +
                "Status," +
                "Last Status Change," +
                "IRB #," +
                "IRB Institution," +
                "IRB Approval Date," +
                "IRB Renewal Date," +
                "IRB Expiration Date," +
                "CRC Approved," +
                "CRC Category," +
                "Industry Initiated," +
                "Start Date," +
                "End Date," +
                "First Visit Date," +
                "Last Scheduled Visit Date," +
                "Total Inpatient Visits," +
                "Total Outpatient Visits," +
                "Total Subjects," +

                "Funding Source 1," +
                "Funding Centers and Institutes 1," +
                "Funding Grant ID 1," +
                "Funding Site Cost Center 1," +
                "Funding Total Direct Dollar Award 1," +
                "Funding Total Indirect Dollar Award 1," +
                "Funding Project Start Date 1," +
                "Funding Project End Date 1," +
                "Funding Comment 1," +

                "Funding Source 2," +
                "Funding Centers and Institutes 2," +
                "Funding Grant ID 2," +
                "Funding Site Cost Center 2," +
                "Funding Total Direct Dollar Award 2," +
                "Funding Total Indirect Dollar Award 2," +
                "Funding Project Start Date 2," +
                "Funding Project End Date 2," +
                "Funding Comment 2," +

                "Funding Source 3," +
                "Funding Centers and Institutes 3," +
                "Funding Grant ID 3," +
                "Funding Site Cost Center 3," +
                "Funding Total Direct Dollar Award 3," +
                "Funding Total Indirect Dollar Award 3," +
                "Funding Project Start Date 3," +
                "Funding Project End Date 3," +
                "Funding Comment 3," +

                "Funding Source 4," +
                "Funding Centers and Institutes 4," +
                "Funding Grant ID 4," +
                "Funding Site Cost Center 4," +
                "Funding Total Direct Dollar Award 4," +
                "Funding Total Indirect Dollar Award 4," +
                "Funding Project Start Date 4," +
                "Funding Project End Date 4," +
                "Funding Comment 4," +

                "PI Name," +
                "PI Email," +
                "PI Phone #1," +
                "PI Pager," +
                "Primary Scheduling Contact," +
                "Primary Scheduling Contact Email," +
                "Primary Scheduling Contact Phone," +
                "Secondary Scheduling Contact," +
                "Secondary Scheduling Contact Email," +
                "Secondary Scheduling Contact Phone," +
                "Third Scheduling Contact," +
                "Third Scheduling Contact Email," +
                "Third Scheduling Contact Phone," +
                "Responsible Physician 1," +
                "Responsible Physician 1 email," +
                "Responsible Physician 1 Phone#1," +
                "Responsible Physician 1 Pager," +
                "Responsible Physician 2," +
                "Responsible Physician 2 Email," +
                "Responsible Physician 2 Phone#1," +
                "Responsible Physician 2 Pager," +
                "Protocol Nurse," +
                "Associate Protocol Nurse," +
                "Protocol Nutritionist," +
                "Associate Protocol Nutritionist",
                result);
    }

    @Test
    public void toCsvRowsTest() {

        StudyDataReportResponseDTO dto1 = new StudyDataReportResponseDTO(s1, sfsSet1);
        StudyDataReportResponseDTO dto2 = new StudyDataReportResponseDTO(s2, sfsSet2);

        List<?> dtoList = Lists.newArrayList(dto1, dto2);

        List<String> threeRows = dto1.toCsvRows(dtoList);

        String expected1 = "\"n1\",\"inst\",\"li1\",\"ci1\",\"status1\",\"12/31/1969 19:04\"," +
                "\"i1\",\"irbInst\",\"12/31/1969\",\"12/31/1969\",\"12/31/1969\",\"Yes\"," +
                "\"cat1\",\"Yes\",\"12/31/1969\",\"12/31/1969\",\"12/31/1969 19:05\"," +
                "\"12/31/1969 19:06\",\"1\",\"1\",\"1\",\"fs11\",\"cai11\",\"g1\",\"cc11\"," +
                "\"1.1\",\"1.11\",\"12/31/1969\",\"12/31/1969\",\"c1\",\"fs12\",\"cai12\",\"g2\"," +
                "\"cc12\",\"1.2\",\"1.22\",\"12/31/1969\",\"12/31/1969\",\"c2\",\"fs13\",\"cai14\"," +
                "\"g3\",\"cc13\",\"1.3\",\"1.33\",\"12/31/1969\",\"12/31/1969\",\"c3\",\"fs14\",\"\"," +
                "\"g4\",\"cc14\",\"1.4\",\"1.44\",\"12/31/1969\",\"12/31/1969\",\"c4\"," +
                "\"finvest1 minvest1 linvest1\",\"einvest1\",\"pinvest1\",\"ginvest1\"," +
                "\"fsched1 msched1 lsched1\",\"esched1\",\"psched1\",\"fsecSched1 msecSched1 lsecSched1\"," +
                "\"esecSched1\",\"psecSched1\",\"fthirdSched1 mthirdSched1 lthirdSched1\"," +
                "\"ethirdSched1\",\"thirdSched1\",\"fdoc1 mdoc1 ldoc1\",\"edoc1\",\"pdoc1\"," +
                "\"gdoc1\",\"fsecDoc1 msecDoc1 lsecDoc1\",\"esecDoc1\",\"psecDoc1\",\"gsecDoc1\"," +
                "\"fprNurse mprNurse lprNurse\",\"fassNurse massNurse lassNurse\"," +
                "\"fprNutr mprNutr lprNutr\",\"fassNutr massNutr lassNutr\"\n";

        String expected2 = "\"n2\",\"inst\",\"li2\",\"ci2\",\"status2\",\"12/31/1969 19:37\",\"i2\"," +
                "\"irbInst\",\"12/31/1969\",\"12/31/1969\",\"12/31/1969\",\"No\",\"cat2\",\"No\"," +
                "\"12/31/1969\",\"12/31/1969\",\"12/31/1969 19:38\",\"12/31/1969 19:39\",\"2\",\"2\"," +
                "\"2\",\"fs21\",\"cai21\",\"g1\",\"cc11\",\"1.1\",\"2.11\",\"12/31/1969\",\"12/31/1969\"," +
                "\"c1\",\"fs22\",\"cai22\",\"g2\",\"cc12\",\"1.2\",\"2.22\",\"12/31/1969\",\"12/31/1969\"," +
                "\"c2\",\"fs23\",\"cai24\",\"g3\",\"cc13\",\"1.3\",\"2.33\",\"12/31/1969\",\"12/31/1969\"," +
                "\"c3\",\"fs24\",\"\",\"g4\",\"cc14\",\"1.4\",\"2.44\",\"12/31/1969\",\"12/31/1969\"," +
                "\"c4\",\"finvest1 minvest1 linvest1\",\"einvest1\",\"pinvest1\",\"ginvest1\"," +
                "\"fsched1 msched1 lsched1\",\"esched1\",\"psched1\",\"fsecSched1 msecSched1 lsecSched1\"," +
                "\"esecSched1\",\"psecSched1\",\"fthirdSched1 mthirdSched1 lthirdSched1\",\"ethirdSched1\"," +
                "\"thirdSched1\",\"fdoc1 mdoc1 ldoc1\",\"edoc1\",\"pdoc1\",\"gdoc1\"," +
                "\"fsecDoc1 msecDoc1 lsecDoc1\",\"esecDoc1\",\"psecDoc1\",\"gsecDoc1\"," +
                "\"fprNurse mprNurse lprNurse\",\"fassNurse massNurse lassNurse\"," +
                "\"fprNutr mprNutr lprNutr\",\"fassNutr massNutr lassNutr\"\n";

        assertEquals(3, threeRows.size());
        
        assertEquals(expected1, threeRows.get(1));
        assertEquals(expected2, threeRows.get(2));
    }
}
