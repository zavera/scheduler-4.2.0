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
import com.google.common.io.CharStreams;
import edu.harvard.catalyst.scheduler.core.SchedulerRuntimeException;
import edu.harvard.catalyst.scheduler.dto.Epic.EmpiSubjectDto;
import edu.harvard.catalyst.scheduler.dto.Epic.EmpiSubjectDto.Address;
import edu.harvard.catalyst.scheduler.dto.Epic.EmpiSubjectDto.Phone;
import edu.harvard.catalyst.scheduler.dto.Epic.EmpiSubjectDto.Phones;
import edu.harvard.catalyst.scheduler.dto.ExternalSubjectQueryBuilder;
import edu.harvard.catalyst.scheduler.dto.response.MrnInfoDTO;
import edu.harvard.catalyst.scheduler.dto.response.SubjectDetailResponse;
import edu.harvard.catalyst.scheduler.dto.response.SubjectsResponseDTO;
import edu.harvard.catalyst.scheduler.entity.Ethnicity;
import edu.harvard.catalyst.scheduler.entity.EthnicityMapping;
import edu.harvard.catalyst.scheduler.entity.Gender;
import edu.harvard.catalyst.scheduler.entity.GenderType;
import edu.harvard.catalyst.scheduler.service.EpicSubjectService;
import org.hibernate.query.Query;
import org.springframework.stereotype.Repository;

import javax.net.ssl.HttpsURLConnection;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.util.*;

import static edu.harvard.catalyst.scheduler.util.PropertyHelper.getProperties;
/**
 * Created with IntelliJ IDEA.
 * User: carl
 * Date: 5/19/15
 * Time: 3:42 PM
 */
@Repository
public class EpicSubjectDAO extends SubjectDAO {

    public static String BLANK = "";
    // 'volatile' ensures the read operation always gives the latest state from memory across threads
    private static volatile Map<String,Ethnicity> empiSchedulerEthnicityMap;
    private static int UNKNOWN_OR_NOT_REPORTED_ETHNICITY = 3;

    // these are available for same-package tests
    static final int MAX_TIMEOUT_TRIES = 3;
    static final int MAX_TIMEOUT_FAILURES = 3;

    static int nightlyBatchSuccessiveFailures;

    // non-static so it can be mockito'd
    int getNightlyBatchSuccessiveFailures() {
        return nightlyBatchSuccessiveFailures;
    }

    static void incrementNightlyBatchSuccessiveTimeoutFailures() {
        nightlyBatchSuccessiveFailures++;
    }
    static void resetNightlyBatchSuccessiveTimeoutFailures() {
        nightlyBatchSuccessiveFailures = 0;
    }

    String emptyToQuotedEmpty(String input) {
        String result = input;

        if (result != null && result.isEmpty()) {
            result = "\"\"";
        }
        return result;
    }


    public EmpiSubjectDto getSearchedSubjectsAsEmpiSubjectDto(
            ExternalSubjectQueryBuilder externalSubjectQueryBuilder,
            boolean tolerateTimeouts) {

        Properties schedulerProps = getProperties("scheduler.properties", getClass());
        String puidSite = schedulerProps.getProperty("puidSite");

        String queryPayload = externalSubjectQueryBuilder.puidSite(puidSite).build();

        return  httpClientQuery(queryPayload, tolerateTimeouts, MAX_TIMEOUT_TRIES, MAX_TIMEOUT_FAILURES);
    }

    public SubjectsResponseDTO getSearchedSubjects(String subjectLastName,
                                                   String subjectFirstName,
                                                   String subjectMrn,
                                                   String subjectBirthDate,
                                                   String subjectGenderCode) {

        ExternalSubjectQueryBuilder externalSubjectQueryBuilder = new ExternalSubjectQueryBuilder()
            .lastName(subjectLastName)
            .firstName(subjectFirstName)
            .mrn(subjectMrn)
            .birthdate(subjectBirthDate)
            .genderCode(subjectGenderCode);
        EmpiSubjectDto empiSubjectDto = getSearchedSubjectsAsEmpiSubjectDto(externalSubjectQueryBuilder, false);

        //Check that results where returned from EMPI
        if(empiSubjectDto.getPatients() != null) {
            for (EmpiSubjectDto.Patient patient : empiSubjectDto.getPatients().getPatientList()) {
                Gender gender = findGenderByCode(Gender.class, patient.getGender());
                if (gender == null) {
                    gender = findGenderByCode(Gender.class, GenderType.UNREPORTED.getGenderName());
                }
                patient.setGender(gender.getName());
            }
        }

        SubjectsResponseDTO result = SubjectsResponseDTO.createSubjectsResponseDTO(empiSubjectDto);

        return result;
    }

    // extracted as helpers so that tests can when() it
    int getResponseCode(HttpURLConnection conn) throws IOException {
        return conn.getResponseCode();
    }
    OutputStream getOutputStream(HttpURLConnection conn) throws IOException {
        return conn.getOutputStream();
    }
    String getResultXml(HttpURLConnection conn) throws IOException {
        InputStream inputStream = conn.getInputStream();
        String resultXml = CharStreams.toString(new InputStreamReader(inputStream, "UTF-8"));
        return resultXml;
    }

    EmpiSubjectDto httpClientQuery(String queryPayload, 
                                   boolean tolerateSomeTimeouts,
                                   int triesPerTimeout, 
                                   int timeoutsPerFail) {

        return httpClientQuery(queryPayload, 0, tolerateSomeTimeouts, triesPerTimeout, timeoutsPerFail);
    }

    // timeoutsPerFail should be negative for UI related queries -- we don't track successive timeouts in that case
    EmpiSubjectDto httpClientQuery(String queryPayload,
                                   int timeoutTryNumber,
                                   boolean tolerateSomeTimeouts,
                                   int triesPerTimeout,
                                   int timeoutsPerFail) {
        String resultXml = null;
        String logMessage = null;

        EmpiSubjectDto empiSubjectDto = new EmpiSubjectDto();

        Properties schedulerProps = getProperties("scheduler.properties", getClass());

        String partnersUrlHost = schedulerProps.getProperty("pUrlHost");
        String partnersUrlQuery = schedulerProps.getProperty("pUrlQuery");
        String partnersHttp = schedulerProps.getProperty("pHttp");

        String fullUrl = new StringBuilder()
                .append(partnersHttp)
                .append("://")
                .append(partnersUrlHost)
                .append(partnersUrlQuery)
                .toString();

        try {
            URL url = new URL(fullUrl);

            String basicUser = schedulerProps.getProperty("basicUser");
            String basicPassword = schedulerProps.getProperty("basicPassword");

            Authenticator.setDefault(new Authenticator() {
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(basicUser, basicPassword.toCharArray());
                }
            });

            HttpsURLConnection conn = (HttpsURLConnection)url.openConnection();
            conn.setRequestMethod("POST");

            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type", "application/xml");

            OutputStream os = getOutputStream(conn);
            os.write(queryPayload.getBytes());
            os.flush();

            int responseCode = getResponseCode(conn);
            if (responseCode == HttpURLConnection.HTTP_OK) {
                resultXml = getResultXml(conn);
                resetNightlyBatchSuccessiveTimeoutFailures();

            } else if (responseCode == HttpURLConnection.HTTP_UNAVAILABLE) {

                logMessage = "Empi server is unavailable: " + responseCode;
                SchedulerRuntimeException.logAndThrow(logMessage);

            } else if (responseCode == HttpURLConnection.HTTP_GATEWAY_TIMEOUT) {
                if (++timeoutTryNumber < triesPerTimeout) {
                    logMessage = "Service timed out. Please try again later.";
                    SchedulerRuntimeException.logAndMaybeThrow(logMessage, !tolerateSomeTimeouts);

                    // if tolerating some timeouts, politely recurse
                    return httpClientQuery(queryPayload, timeoutTryNumber, tolerateSomeTimeouts, triesPerTimeout, timeoutsPerFail);
                }
                else {
                    incrementNightlyBatchSuccessiveTimeoutFailures();

                    if (getNightlyBatchSuccessiveFailures() == timeoutsPerFail) {

                        logMessage = "Hit the timeout-failure threshold: " + timeoutsPerFail;
                        SchedulerRuntimeException.logAndThrow(logMessage);
                    }
                    else {

                        logMessage = "Service timed out. Please try again later. No more retries of this query.";
                        SchedulerRuntimeException.logAndMaybeThrow(logMessage, !tolerateSomeTimeouts);
                    }
                }
            }
            else {

                logMessage = "Bad code from Empi server: " + responseCode;
                SchedulerRuntimeException.logAndThrow(logMessage);
            }
        }
        catch (IOException e) {

            logMessage = e.getMessage();
            if(logMessage == null)
            {
                logMessage = "SSL handshake not successful";
            }
            SchedulerRuntimeException.logAndThrow(logMessage, e);
        }
        catch (Exception e) {

            logMessage = e.getMessage();
            if(logMessage == null)
            {
                logMessage = "Problem executing POST request to EMPI";
            }

            if (e instanceof SchedulerRuntimeException) {
                // possibly bubbling up out of recursion
                // simply re-throw, do not further embed in a SchedulerRuntimeException
                throw e;
            }
            else {
                SchedulerRuntimeException.logAndThrow(logMessage, e);
            }
        }

        if (resultXml != null) {
            try {
                //catch any errors parsing the result xml
                empiSubjectDto = EmpiSubjectDto.unmarshall(resultXml);
            }
            catch(Exception e)
            {
                return empiSubjectDto;
            }
        }

        return empiSubjectDto;
    }

    public SubjectDetailResponse findSubjectByUid(Integer id) {

        String puid = String.valueOf(id);

        ExternalSubjectQueryBuilder externalSubjectQueryBuilder = new ExternalSubjectQueryBuilder().puid(puid);
        EmpiSubjectDto empiSubjectDto = getSearchedSubjectsAsEmpiSubjectDto(externalSubjectQueryBuilder, false);

        SubjectDetailResponse empiResult = createSubjectDetailResponse(empiSubjectDto);

        //Check if this subject is already in scheduler
        //then override gender and add any comments
        SubjectDetailResponse internalResult = findInternalSubjectByMrn(empiResult.getMrnInfoList());
        if(internalResult.getId() != null)
        {
            empiResult.setComment(internalResult.getComment());
        }
        return empiResult;
    }

    public SubjectDetailResponse createSubjectDetailResponse(EmpiSubjectDto empiSubjectDto) {

        SubjectDetailResponse result;

        if (null == empiSubjectDto || empiSubjectDto.getPatients() == null || empiSubjectDto.getPatients().getPatientList() == null
                || empiSubjectDto.getPatients().getPatientList().isEmpty()) {

            result = new SubjectDetailResponse();
        }
        else {
            result = setFromDto(empiSubjectDto);
        }

        return result;
    }

    public SubjectDetailResponse setFromDto(EmpiSubjectDto empiSubjectDto) {
        SubjectDetailResponse result = new SubjectDetailResponse();
        EmpiSubjectDto.Patient patient = empiSubjectDto.getPatients().getPatientList().get(0);

        result.setId(patient.getUid());
        result.setPartnersUid(String.valueOf(patient.getUid()));
        result.setLastName(patient.getName().getLast());
        result.setFirstName(patient.getName().getFirst());
        result.setMiddleName(patient.getName().getMiddleInitial());

        List<EmpiSubjectDto.Mrn> mrns = patient.getMrns().getMrnList();
        List<MrnInfoDTO> mrnInfoDTOList = new ArrayList<MrnInfoDTO>();

        for(EmpiSubjectDto.Mrn mrn : mrns)
        {
            MrnInfoDTO mrnInfoDTO = new MrnInfoDTO(mrn.getValue(), mrn.getSite(), mrn.getStatus());
            mrnInfoDTOList.add(mrnInfoDTO);
        }
        result.setMrnInfoList(mrnInfoDTOList);

        Address address  = patient.getAddress();

        if (null != address) {
            result.setCity(address.getCity());
            result.setStateName(address.getState());
            result.setCountryName(address.getCountry());
            result.setStreetAddress1(address.getLine1());
            result.setStreetAddress2(address.getLine2());
            result.setZip(address.getZipAsString());
        }

        Gender gender = findGenderByCode(Gender.class, patient.getGender());
        if(gender == null)
        {
            gender = findGenderByCode(Gender.class, GenderType.UNREPORTED.getGenderName());
        }

        result.setGenderName(gender.getName());
        result.setGenderCode(patient.getGender());
        result.setGenderId(gender.getId());

        String ethnicName = null;

        if (null != patient.getEthnicity()){
            ethnicName = patient.getEthnicity().getEthnic1();
        }

        Ethnicity lookedUpEthnicity = lookupEmpiEthnicityString(ethnicName);
        result.setEthnicityName(lookedUpEthnicity != null ? lookedUpEthnicity.getName() : patient.getEthnicity() + " - N/A");

        result.setRaceName(patient.getOtherPid() != null ? patient.getOtherPid().getRace() : patient.getOtherPid() + " - N/A");

        result.setComment(BLANK);

        setPhoneData(result, patient);

        result.setBirthdate(new Date(patient.getDobString()));
        result.setActive(true);
        return result;
    }

    private void setPhoneData(SubjectDetailResponse result, EmpiSubjectDto.Patient patient) {
        Phones phones = patient.getPhones();
        if (null != phones){
            List<Phone> phoneList = phones.getPhoneList();
            if (null != phoneList) {
                int pSize = phoneList.size();

                if (pSize >= 1) {
                    Phone primary = phoneList.get(0);
                    if (null != primary) {
                        result.setPrimaryContactNumber(primary.getNumber());
                    }
                }
                if (pSize >= 2) {
                    Phone secondary = phoneList.get(1);
                    if (null != secondary) {
                        result.setSecondaryContactNumber(secondary.getNumber());
                    }
                }
            }
        }
    }

    public Map<String,Ethnicity> loadEmpiToSchedulerEthnicityMap() {
        Map<String,Ethnicity> result = Maps.newHashMap();

        Query query = newQuery("SELECT ese FROM EthnicityMapping ese");

        List<EthnicityMapping> eseList = query.list();

        eseList.stream().forEach(ese -> result.put(ese.getExternalEthnicityString(), ese.getSchedulerEthnicity()));

        return result;
    }

    void synchLoadEmpiSchedulerEthnicityMap() {
        // 'double-checked locking' to improve performance with synchronization
        // (relies on empiSchedulerEthnicityMap being volatile, i.e., read-op always gets latest version)
        if (empiSchedulerEthnicityMap == null) {

            synchronized (EpicSubjectService.class) {
                empiSchedulerEthnicityMap = loadEmpiToSchedulerEthnicityMap();
            }
        }
    }
    public Ethnicity lookupEmpiEthnicityString(String empiEthnicity) {
        synchLoadEmpiSchedulerEthnicityMap();

        Ethnicity result = empiSchedulerEthnicityMap.get(empiEthnicity);

        if (result == null) {
            result = findByEthnicityId(UNKNOWN_OR_NOT_REPORTED_ETHNICITY);
        }
        return result;
    }
    // non-static getter, for use with mockito
    Map<String, Ethnicity> getEmpiSchedulerEthnicityMap() {
        return empiSchedulerEthnicityMap;
    }

    public SubjectDetailResponse getSubjectDetailsUsingMrnAndSite(String mrn, String site)
    {
        ExternalSubjectQueryBuilder externalSubjectQueryBuilder = new ExternalSubjectQueryBuilder()
            .mrn(mrn)
            .mrnSite(site);
        EmpiSubjectDto empiSubjectDto = getSearchedSubjectsAsEmpiSubjectDto(externalSubjectQueryBuilder, false);

        return createSubjectDetailResponse(empiSubjectDto);
    }
}


