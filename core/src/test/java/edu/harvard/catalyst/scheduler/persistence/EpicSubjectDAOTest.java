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

import com.google.common.collect.Maps;
import edu.harvard.catalyst.scheduler.core.SchedulerRuntimeException;
import edu.harvard.catalyst.scheduler.dto.ExternalSubjectQueryBuilder;
import edu.harvard.catalyst.scheduler.dto.Epic.EmpiSubjectDto;
import edu.harvard.catalyst.scheduler.dto.response.SubjectDetailResponse;
import edu.harvard.catalyst.scheduler.entity.Ethnicity;
import edu.harvard.catalyst.scheduler.entity.Gender;
import org.apache.commons.httpclient.util.HttpURLConnection;
import org.apache.log4j.Appender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.spi.LoggingEvent;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static edu.harvard.catalyst.scheduler.persistence.EpicSubjectDAO.MAX_TIMEOUT_FAILURES;
import static edu.harvard.catalyst.scheduler.persistence.EpicSubjectDAO.MAX_TIMEOUT_TRIES;
import static edu.harvard.catalyst.scheduler.util.TestUtils.verifyLog;
import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.util.AssertionErrors.assertEquals;

/**
 * @author Bill Simons
 * @date 1/7/14
 * @link http://cbmi.med.harvard.edu
 * @link http://chip.org
 */
@RunWith(MockitoJUnitRunner.class)
public class EpicSubjectDAOTest {

    private EpicSubjectDAO epicSubjectDAOSpy;

    private Appender mockAppender;
    @Captor
    private ArgumentCaptor<LoggingEvent> captorLoggingEvent;

    @Autowired
    private static EpicSubjectDAO dao;

    @BeforeClass
    static public void setup() {
        dao = new EpicSubjectDAO();
    }
    EpicSubjectDAO spyDao = spy(dao);

    @Before
    public void setup2() {
        mockAppender = mock(Appender.class);

        Logger root = Logger.getRootLogger();
        root.addAppender(mockAppender);
        root.setLevel(Level.INFO);

        epicSubjectDAOSpy = spy(new EpicSubjectDAO());//null));
    }

    @Test
    public void testNameAndMrnToParameter() {
        ExternalSubjectQueryBuilder externalSubjectQueryBuilder = new ExternalSubjectQueryBuilder();
        assertEquals("", "nn", externalSubjectQueryBuilder.nameAndMrnToEmpiSearchParameter("nn", null));
        assertEquals("", "nn", externalSubjectQueryBuilder.nameAndMrnToEmpiSearchParameter("nn", ""));
        assertEquals("", "nn", externalSubjectQueryBuilder.nameAndMrnToEmpiSearchParameter(null, "nn"));
        assertEquals("", "nn", externalSubjectQueryBuilder.nameAndMrnToEmpiSearchParameter("", "nn"));

        // mrn wins
        assertEquals("", "nn", externalSubjectQueryBuilder.nameAndMrnToEmpiSearchParameter("foo", "nn"));

        // oops
        assertEquals("", "", externalSubjectQueryBuilder.nameAndMrnToEmpiSearchParameter("", ""));
        assertEquals("", null, externalSubjectQueryBuilder.nameAndMrnToEmpiSearchParameter(null, null));

        assertEquals("", "", externalSubjectQueryBuilder.nameAndMrnToEmpiSearchParameter(null, ""));
        assertEquals("", null, externalSubjectQueryBuilder.nameAndMrnToEmpiSearchParameter("", null));
    }

    @Test
    public void testEmptyToQuotedEmpty() {
        assertEquals("", "nn", dao.emptyToQuotedEmpty("nn"));
        assertEquals("", null, dao.emptyToQuotedEmpty(null));
        assertEquals("", "\"\"", dao.emptyToQuotedEmpty(""));
    }

    @Test
    public void testEmpiUnavailable() throws Exception {
        doReturn(HttpURLConnection.HTTP_UNAVAILABLE).when(spyDao).getResponseCode(any(HttpURLConnection.class));
        doReturn(new OutputStream() {
            @Override
            public void write(int b) throws IOException {
            }
        }).when(spyDao).getOutputStream(any(HttpURLConnection.class));

        try {
            spyDao.httpClientQuery("", true, MAX_TIMEOUT_TRIES, MAX_TIMEOUT_FAILURES);

            assertTrue("Shouldn't get here", false);
        }
        catch (Exception ioe) {
            String message = ioe.getMessage();

            assertEquals("", "Empi server is unavailable: 503", message);
        }
    }

    @Test
    public void testEmpiTimeoutOneFailure() throws Exception {
        doReturn(HttpURLConnection.HTTP_GATEWAY_TIMEOUT)
                .doReturn(HttpURLConnection.HTTP_GATEWAY_TIMEOUT)
                .doReturn(HttpURLConnection.HTTP_GATEWAY_TIMEOUT)
                .doReturn(HttpURLConnection.HTTP_OK)
                .when(spyDao).getResponseCode(any(HttpURLConnection.class));

        doReturn(new OutputStream() {
            @Override
            public void write(int b) throws IOException {
            }
        }).when(spyDao).getOutputStream(any(HttpURLConnection.class));

        doReturn("").when(spyDao).getResultXml(any()); // more flexible form of any()

        spyDao.httpClientQuery("", true, MAX_TIMEOUT_TRIES, MAX_TIMEOUT_FAILURES);

        verifyLog("Service timed out. Please try again later.", 3, mockAppender, captorLoggingEvent);
    }

    @Test
    public void testEmpiTimeoutTooManyFailures() throws Exception {
        doReturn(HttpURLConnection.HTTP_GATEWAY_TIMEOUT)
                .doReturn(HttpURLConnection.HTTP_GATEWAY_TIMEOUT)
                .doReturn(HttpURLConnection.HTTP_GATEWAY_TIMEOUT)

                .when(spyDao).getResponseCode(any(HttpURLConnection.class));

        doReturn(new OutputStream() {
            @Override
            public void write(int b) throws IOException {
            }
        }).when(spyDao).getOutputStream(any(HttpURLConnection.class));

        doReturn(3).when(spyDao).getNightlyBatchSuccessiveFailures();

        try {
            spyDao.httpClientQuery("", true, MAX_TIMEOUT_TRIES, MAX_TIMEOUT_FAILURES);

            assertTrue("Shouldn't get here", false);
        }
        catch (SchedulerRuntimeException sre) {
            String message = sre.getMessage();

            assertEquals("", "Hit the timeout-failure threshold: 3", message);
        }

        verifyLog("Hit the timeout-failure threshold: 3",
                1, mockAppender, captorLoggingEvent);
    }

    @Test
    public void testEmpiUnavailableNoThrow() throws Exception {
        doReturn(HttpURLConnection.HTTP_UNAVAILABLE).when(spyDao).getResponseCode(any(HttpURLConnection.class));
        doReturn(new OutputStream() {
            @Override
            public void write(int b) throws IOException {
            }
        }).when(spyDao).getOutputStream(any(HttpURLConnection.class));

        try {
            spyDao.httpClientQuery("", true, 1, 1);
            assertTrue("Shouldn't get here", false);
        }
        catch (SchedulerRuntimeException sre){

            verifyLog("Empi server is unavailable: 503",
                    1, mockAppender, captorLoggingEvent);
        }
    }
    @Test
    public void testEmpiTimeoutTooManyFailuresNoThrow() throws Exception {
        doReturn(HttpURLConnection.HTTP_GATEWAY_TIMEOUT)
                .when(spyDao).getResponseCode(any(HttpURLConnection.class));

        doReturn(new OutputStream() {
            @Override
            public void write(int b) throws IOException {
            }
        }).when(spyDao).getOutputStream(any(HttpURLConnection.class));

        EmpiSubjectDto empiSubjectDto = spyDao.httpClientQuery("", true, 1, -1);

        verifyLog("Service timed out. Please try again later.",
                1, mockAppender, captorLoggingEvent);
    }

    private void  spyOnEthnicity(){

        List<Ethnicity> eths = new ArrayList<>();

        Ethnicity hispanic = mock(Ethnicity.class);
        when(hispanic.getId()).thenReturn(42);
        when(hispanic.getName()).thenReturn("HISPANIC");
        Map<String,Ethnicity> map = Maps.newHashMap();
        map.put("HISPANIC", hispanic);

        doReturn(map).when(epicSubjectDAOSpy).loadEmpiToSchedulerEthnicityMap();
        doReturn(map).when(epicSubjectDAOSpy).getEmpiSchedulerEthnicityMap();

        Ethnicity emptyEth = mock(Ethnicity.class);
        when(emptyEth.getId()).thenReturn(3);


        when(emptyEth.getName()).thenReturn("Unknown or Not Reported");
        doReturn(emptyEth).when(epicSubjectDAOSpy).findByEthnicityId(3);
    }

    @Test
    public void testLookupEmpiEthnicityString() {
        spyOnEthnicity();

        assertEquals("hispanic lookup", "HISPANIC", epicSubjectDAOSpy.lookupEmpiEthnicityString("HISPANIC").getName());
        assertEquals("empty lookup", "Unknown or Not Reported", epicSubjectDAOSpy.lookupEmpiEthnicityString(null).getName());
    }



    @Test
    public void testEmptyFields() {
        spyOnEthnicity();
        EmpiSubjectDto dto = makeEmpiDtoPatient();

        Gender mockGender = mock(Gender.class);
        doReturn(mockGender).when(epicSubjectDAOSpy).findGenderByCode(Gender.class, null);
        SubjectDetailResponse sdr = epicSubjectDAOSpy.setFromDto(dto);

        assertEquals("null ethnicity", "Unknown or Not Reported", sdr.getEthnicityName());
        assertEquals("blank state", "", sdr.getStateName());
    }

    @Test
    public void testNullGender() {
        spyOnEthnicity();
        EmpiSubjectDto dto = makeEmpiDtoPatient();
        doReturn(null).when(epicSubjectDAOSpy).findGenderByCode(Gender.class, null);

        Gender UnreportedGender = new Gender();
        UnreportedGender.setCode("UNREPORTED");
        UnreportedGender.setName("Unreported");
        UnreportedGender.setId(5);

        doReturn(UnreportedGender).when(epicSubjectDAOSpy).findGenderByCode(Gender.class, "Unreported");

        SubjectDetailResponse resp = epicSubjectDAOSpy.setFromDto(dto);

        assertEquals("genderId", 5, resp.getGenderId());
        assertEquals("gender name", "Unreported", resp.getGenderName());
    }


    @Test
    public void testEthnicityAndCity() {
        spyOnEthnicity();
        EmpiSubjectDto.Ethnicity dtoHispanic = new EmpiSubjectDto.Ethnicity("HISPANIC", null, null);
        EmpiSubjectDto dto2 = makeEmpiDtoPatientWithAddress(dtoHispanic);

        Gender mockGender = mock(Gender.class);
        doReturn(mockGender).when(epicSubjectDAOSpy).findGenderByCode(Gender.class, null);

        SubjectDetailResponse resp2 = epicSubjectDAOSpy.setFromDto(dto2);

        assertEquals("hispanic ethnicity", "HISPANIC", resp2.getEthnicityName());
        assertEquals("florida", "Florida", resp2.getStateName());

        assertNull(resp2.getPrimaryContactNumber());
        assertNull(resp2.getSecondaryContactNumber());
    }

    @Test
    public void testPrimaryPhoneOnly(){
       doPhoneTest(false);
    }

    @Test
    public void testPrimaryAndSecondaryPhone(){
        doPhoneTest(true);
    }


    @Test
    public void testEmpiQueryConstruction(){

        String mrnOnlyQuery1= new ExternalSubjectQueryBuilder().lastName("Jones").firstName("Shirley").mrn("123").build();
        String mrnOnlyQuery2= new ExternalSubjectQueryBuilder().mrn("123").birthdate(" ").genderCode(" ").mrnSite("").build();
        String mrnOnlyQuery3= new ExternalSubjectQueryBuilder().mrn("123").birthdate(" ").genderCode("").mrnSite("").build();


        ExternalSubjectQueryBuilder externalSubjectQueryBuilder2 = new ExternalSubjectQueryBuilder()
                .lastName("Jones")
                .firstName("Shirley");
        String query2= externalSubjectQueryBuilder2.build();

        String mrnAndGenderQuery1 = new ExternalSubjectQueryBuilder().mrn("abc").genderCode("F").build();
        String mrnAndGenderQuery2 = new ExternalSubjectQueryBuilder().mrn("abc").genderCode("F").mrnSite("").build();
        String mrnAndGenderQuery3 = new ExternalSubjectQueryBuilder().mrn("abc").genderCode("F").mrnSite("  ").build();
        String mrnAndGenderQuery4 = new ExternalSubjectQueryBuilder().mrn("abc").birthdate("").genderCode("F").mrnSite("").build();

        String[] mrnAndGenderQueries = {mrnAndGenderQuery1, mrnAndGenderQuery2, mrnAndGenderQuery3, mrnAndGenderQuery4};
        String[] mrnOnlyQueries = {mrnOnlyQuery1, mrnOnlyQuery2, mrnOnlyQuery3};

        String mrnMrnSiteBirthdateGenderQuery= new ExternalSubjectQueryBuilder().mrn("abc").birthdate("11/02/1977").genderCode("F").mrnSite("Site1").build();
        String nameBirthdateQuery= new ExternalSubjectQueryBuilder().lastName("Ruby").firstName("Sapphire").birthdate("11/01/1999").build();
        String nameMrnSite= new ExternalSubjectQueryBuilder().lastName("Jones").firstName("Shirley").mrnSite("siteA").build();
        String nameMrnSiteBirthdateQuery= new ExternalSubjectQueryBuilder().lastName("Jones").firstName("Shirley").birthdate("11/09/1987").mrnSite("siteA").build();
        String nameBirthdateGenderQuery= new ExternalSubjectQueryBuilder().lastName("Jones").firstName("Shirley").birthdate("11/09/1987").genderCode("F").build();
        String mrnMrnSiteGenderQuery= new ExternalSubjectQueryBuilder().mrn("abc").genderCode("F").mrnSite("Site1").build();

        for (String mrnOnlyQuery: mrnOnlyQueries) {
            Assert.assertEquals("<?xml version=\"1.0\"?><Query Search=\"123\" Max=\"50\" />", mrnOnlyQuery);
        }

        Assert.assertEquals("<?xml version=\"1.0\"?><Query Search=\"Jones, Shirley\" Max=\"50\" />", query2);

        for(String mrnAndGenderQuery: mrnAndGenderQueries) {
            Assert.assertEquals("<?xml version=\"1.0\"?><Query Search=\"abc\" Sex=\"F\" Max=\"50\" />", mrnAndGenderQuery);
        }

        Assert.assertEquals("<?xml version=\"1.0\"?><Query Search=\"abc\" Age=\"11/02/1977\" Sex=\"F\" Site=\"Site1\" Max=\"50\" />", mrnMrnSiteBirthdateGenderQuery);
        Assert.assertEquals("<?xml version=\"1.0\"?><Query Search=\"Ruby, Sapphire\" Age=\"11/01/1999\" Max=\"50\" />", nameBirthdateQuery);
        Assert.assertEquals("<?xml version=\"1.0\"?><Query Search=\"Jones, Shirley\" Site=\"siteA\" Max=\"50\" />", nameMrnSite);
        Assert.assertEquals("<?xml version=\"1.0\"?><Query Search=\"Jones, Shirley\" Age=\"11/09/1987\" Site=\"siteA\" Max=\"50\" />", nameMrnSiteBirthdateQuery);
        Assert.assertEquals("<?xml version=\"1.0\"?><Query Search=\"Jones, Shirley\" Age=\"11/09/1987\" Sex=\"F\" Max=\"50\" />", nameBirthdateGenderQuery);
        Assert.assertEquals("<?xml version=\"1.0\"?><Query Search=\"abc\" Sex=\"F\" Site=\"Site1\" Max=\"50\" />", mrnMrnSiteGenderQuery);

        String puidQuery = new ExternalSubjectQueryBuilder().puid("123").build();
        Assert.assertEquals("<?xml version=\"1.0\"?><Query Search=\"123\" Max=\"50\" />", puidQuery);

        String puidPuidSiteQuery = new ExternalSubjectQueryBuilder().puid("123").puidSite("XYZ").build();
        Assert.assertEquals("<?xml version=\"1.0\"?><Query Search=\"123\" Site=\"XYZ\" Max=\"50\" />", puidPuidSiteQuery);

        String puidQueryMrnMrnSiteQuery = new ExternalSubjectQueryBuilder().mrn("789").mrnSite("EFG").puid("123").puidSite("XYZ").build();
        Assert.assertEquals("<?xml version=\"1.0\"?><Query Search=\"123\" Site=\"XYZ\" Max=\"50\" />", puidQueryMrnMrnSiteQuery);
    }


    @Test
    public void setCreateSubjectDetailResponse(){

        //#0:  empty
        EmpiSubjectDto  tester = new EmpiSubjectDto();
        SubjectDetailResponse response = epicSubjectDAOSpy.createSubjectDetailResponse(tester);

        checkEmptyResponse(response);

        //#1:  empty patient list
        List<EmpiSubjectDto.Patient> pList = new ArrayList<>();
        tester = new EmpiSubjectDto(pList);
        response = epicSubjectDAOSpy.createSubjectDetailResponse(tester);

        checkEmptyResponse(response);

        //#2:  the empisubjectdto is null
        response = epicSubjectDAOSpy.createSubjectDetailResponse(null);
        checkEmptyResponse(response);
    }

    private void checkEmptyResponse(SubjectDetailResponse response ){
        assertNull(response.getPrimaryContactNumber());
        assertNull(response.getFirstName());
        assertNull(response.getLastName());
    }

    private void doPhoneTest(boolean secondaryPhoneToo){

        spyOnEthnicity();

        EmpiSubjectDto.Ethnicity dtoHispanic = new EmpiSubjectDto.Ethnicity("HISPANIC", null, null);
        EmpiSubjectDto dto2 = makeEmpiDtoPatientWithAddressandPhone(dtoHispanic, secondaryPhoneToo);

        Gender mockGender = mock(Gender.class);
        doReturn(mockGender).when(epicSubjectDAOSpy).findGenderByCode(Gender.class, null);

        SubjectDetailResponse resp2 = epicSubjectDAOSpy.setFromDto(dto2);

        assertEquals(" hispanic ethnicity", "HISPANIC", resp2.getEthnicityName());
        assertEquals("florida", "Florida", resp2.getStateName());

        assertEquals("old home phone(defunct)", "2015738351", resp2.getPrimaryContactNumber());

        if (secondaryPhoneToo) {
            assertEquals("cell phone number; made up", "2015738331", resp2.getSecondaryContactNumber());
        } else {
            assertNull(resp2.getSecondaryContactNumber());
        }
    }

    private EmpiSubjectDto makeEmpiDtoPatient() {
        EmpiSubjectDto.Patient patient = makeSimplePatient(null);

        List<EmpiSubjectDto.Patient> pList = new ArrayList<>();
        pList.add(patient);

        return new EmpiSubjectDto(pList);
    }

    private EmpiSubjectDto makeEmpiDtoPatientWithAddress(EmpiSubjectDto.Ethnicity eth) {
        EmpiSubjectDto.Patient patient = makeSimplePatient(eth);

        EmpiSubjectDto.Address address = new EmpiSubjectDto.Address(null, null, null, "Florida", null, null);

        patient.setAddress(address);

        List<EmpiSubjectDto.Patient> pList = new ArrayList<>();
        pList.add(patient);

        return new EmpiSubjectDto(pList);
    }


    private EmpiSubjectDto makeEmpiDtoPatientWithAddressandPhone(EmpiSubjectDto.Ethnicity eth, boolean addSecondaryPhone) {
        EmpiSubjectDto.Patient patient = makeSimplePatient(eth);

        EmpiSubjectDto.Address address = new EmpiSubjectDto.Address(null, null, null, "Florida", null, null);

        patient.setAddress(address);
        EmpiSubjectDto.Phone primary = new EmpiSubjectDto.Phone("home", "2015738351");
        List<EmpiSubjectDto.Phone> phoneList = new ArrayList<>();
        phoneList.add(primary);

        if (addSecondaryPhone){
            EmpiSubjectDto.Phone secondary = new EmpiSubjectDto.Phone("cell", "2015738331");
            phoneList.add(secondary);
        }

        EmpiSubjectDto.Phones empiPhones= new EmpiSubjectDto.Phones(phoneList);
        patient.setPhones(empiPhones);

        List<EmpiSubjectDto.Patient> pList = new ArrayList<>();
        pList.add(patient);

        return new EmpiSubjectDto(pList);
    }


    private  EmpiSubjectDto  packagePatient(EmpiSubjectDto.Patient patient){
        List<EmpiSubjectDto.Patient> pList = new ArrayList<>();
        pList.add(patient);

        return new EmpiSubjectDto(pList);
    }

    private EmpiSubjectDto.Patient makeSimplePatient(EmpiSubjectDto.Ethnicity eth) {
        EmpiSubjectDto.Patient patient = new EmpiSubjectDto.Patient();

        EmpiSubjectDto.Name name1= new EmpiSubjectDto.Name("Tom","Jones", "B");
        patient.setName(name1);
        patient.setUid(876);
        patient.setDobString("04/04/1977");

        EmpiSubjectDto.Mrn mrn1 = new EmpiSubjectDto.Mrn("site1", "99876", "A");

        List<EmpiSubjectDto.Mrn> actualMrnList = new ArrayList<>();
        actualMrnList.add(mrn1);
        EmpiSubjectDto.Mrns mrnList = new EmpiSubjectDto.Mrns();
        mrnList.setMrnList(actualMrnList);

        patient.setMrns(mrnList);

        if (null != eth) {
            patient.setEthnicity(eth);
        }

        return patient;
    }

    @Test
    public void testDefaultValueForActive() {
        spyOnEthnicity();
        EmpiSubjectDto dto2 = makeEmpiDtoPatient();

        Gender mockGender = mock(Gender.class);
        doReturn(mockGender).when(epicSubjectDAOSpy).findGenderByCode(Gender.class, null);

        SubjectDetailResponse resp2 = epicSubjectDAOSpy.setFromDto(dto2);

        assertTrue("active", resp2.isActive());
    }
}
