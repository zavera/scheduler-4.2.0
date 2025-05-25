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
package edu.harvard.catalyst.scheduler.batchSubjects;

import edu.harvard.catalyst.scheduler.core.SchedulerRuntimeException;
import edu.harvard.catalyst.scheduler.dto.Epic.EmpiSubjectDto;
import edu.harvard.catalyst.scheduler.dto.ExternalSubjectQueryBuilder;
import edu.harvard.catalyst.scheduler.entity.*;
import edu.harvard.catalyst.scheduler.persistence.EpicSubjectDAO;
import edu.harvard.catalyst.scheduler.persistence.SubjectDAO;
import edu.harvard.catalyst.scheduler.service.EpicSubjectService;
import edu.harvard.catalyst.scheduler.util.DateUtility;
import edu.harvard.catalyst.scheduler.util.MiscUtil;
import edu.harvard.catalyst.scheduler.util.SubjectDataEncryptor;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.*;

import static edu.harvard.catalyst.scheduler.util.MiscUtil.*;

/**
 * @author Carl Woolf
 * @link http://cbmi.med.harvard.edu
 */
@Component
public class BatchSubjects {

    private static final Logger LOG = Logger.getLogger(BatchSubjects.class);

    static final String ARROW = "--->";

    private final EpicSubjectService epicSubjectService;
    private final SubjectDAO subjectDAO;
    private final EpicSubjectDAO epicSubjectDAO;

    @Autowired
    public BatchSubjects(SubjectDAO subjectDAO,
                         @Qualifier(value = "encryptionKeyBatch") final Key key,
                         @Qualifier("subjectSSOTConfigured") final EpicSubjectService epicSubjectService,
                         EpicSubjectDAO epicSubjectDAO) {

        this.epicSubjectService = epicSubjectService;
        this.subjectDAO = subjectDAO;
        this.epicSubjectDAO = epicSubjectDAO;

        SubjectDataEncryptor.setEncryptionKey(key);
    }


    public static void main(String[] args) throws SQLException {

        ApplicationContext context = new ClassPathXmlApplicationContext("spring-epic-batch-subjects.xml");

        BatchSubjects batchSubjects = (BatchSubjects) context.getBean("batchSubjects");

        batchSubjects.run();

    }

    void run() throws SQLException {

        LOG.info("\n===========================================" +
                "============================================");
        LOG.info("Counted " + findNumSubjectMrns() + " SubjectMrns to refresh.");

        List<SubjectMrn> subjectMrns = findAllSubjectMrns();

        LOG.info("Found " + subjectMrns.size() + " SubjectMrns to refresh.");

        int numRefreshed = 0;

        for (SubjectMrn subjectMrn : subjectMrns) {
            Subject subject = subjectMrn.getSubject();
            ArchivalStatus archivalStatus = subject.getArchivalStatus();
            String site = subjectMrn.getSite();
            boolean siteExists = MiscUtil.isNonNullNonEmpty(site);

            if (archivalStatus == null && siteExists) {

                LOG.info("++++++++ Refreshing subject, id: " + subject.getId() + " + via a subjectMrn");
                if (refreshExternalSubject(subjectMrn, true)) {
                    numRefreshed++;
                }
                LOG.info("++++++++ Done with subject, id: " + subject.getId());
            }
            else {
                LOG.info("++++++++ Not refreshing subject, id: " + subject.getId() + " + since its archivalStatus is " + archivalStatus + " or Site is " + subjectMrn.getSite());
            }
        }

        LOG.info("Refreshed a total of " + numRefreshed + " SubjectMrn(s).");
    }

    List<SubjectMrn> findAllSubjectMrns() throws SQLException {
        return subjectDAO.findAllSubjectMrns();
    }

    public int findNumSubjectMrns() {
        return subjectDAO.findNumberOfSubjectMrns();
    }

    ///////////// from epicSubjectService

    boolean sanityCheckEmpiMatchCount(EmpiSubjectDto empiSubjectDTO, String institution, Integer id) {
        boolean result = true; // optimistic

        if (empiSubjectDTO == null) {
            result = false;
        }
        else {
            EmpiSubjectDto.Patients patients = empiSubjectDTO.getPatients();

            int count = 0;

            if (patients != null && patients.getPatientList() != null) {
                count = patients.getPatientList().size();
            }

            if (count != 1) {
                SchedulerRuntimeException.logDontThrow(
                        prefix1(institution, id) + " expected 1 but got " + count + " matches.");
                result = false;
            }
        }

        return result;
    }

    static final String arrow2 = "====> ";
    static final String arrow1 = "--> ";
    String prefix(String institution, Integer id, String arrow) {
        return "For Subject id: " + id + ", institution: " + institution + ":\n" + arrow;
    }
    String prefix1(String institution, Integer id) {
        return prefix(institution, id, arrow1);
    }
    String prefix2(String institution, Integer id) {
        return prefix(institution, id, arrow2);
    }

    // helper for mockito instrumentation
    String decryptMrn(String mrn) {
        return SubjectDataEncryptor.decrypt(mrn);
    }

    EmpiSubjectDto.Mrn relevantMrn(EmpiSubjectDto.Patient patient, String institution, String decryptedMrn) {
        EmpiSubjectDto.Mrn resultMrn = null;

        EmpiSubjectDto.Mrns mrns = patient.getMrns();
        if (mrns != null && isNonNullNonEmpty(mrns.getMrnList())) {
            List<EmpiSubjectDto.Mrn> mrnList = mrns.getMrnList();

            Optional<EmpiSubjectDto.Mrn> mrnOptional = mrnList.stream()
                    .filter(m -> m.getSite().equalsIgnoreCase(institution) && m.getValue().equalsIgnoreCase(decryptedMrn) ) //   uniqueness --> code would also match!
                    .findFirst();

            if (mrnOptional.isPresent()) {
                resultMrn = mrnOptional.get();
            }
        }

        return resultMrn;
    }

    String canonicalPhoneNumber(String input) {
        String result = null;
        if (input != null) {

            result = input.replaceAll("[^\\d]", ""); // leave only digits
            result = result.replaceAll("(\\d\\d\\d)(\\d\\d\\d)(\\d\\d\\d\\d)", "($1) $2-$3");
        }

        return result;
    }
    public String imposeUpon(Subject subject, EmpiSubjectDto empiSubjectDto, String institution, SubjectMrn originalSubjectMrn) {

        EmpiSubjectDto.Patient patient = empiSubjectDto.getPatients().getPatientList().get(0);
        EmpiSubjectDto.Address address = patient.getAddress();
        EmpiSubjectDto.Name name = patient.getName();
        EmpiSubjectDto.Phones phones = patient.getPhones();

        String decryptedMrn = decryptMrn(originalSubjectMrn.getMrn());
        EmpiSubjectDto.Mrn empiMrn = relevantMrn(patient, institution, decryptedMrn);
        String genderEmpi = patient.getGender();

        StringBuilder changes = new StringBuilder();

        changes.append(verboseSetPuid(subject, String.valueOf(patient.getUid())));

        changes.append(verboseSetLastName(subject, name == null ? null : name.getLast()));
        changes.append(verboseSetFirstName(subject, name == null ? null : name.getFirst()));
        changes.append(verboseSetMiddleName(subject, name == null ? null : name.getMiddleInitial()));

        changes.append(verboseSetStreetAddress1(subject, address == null ? null : address.getLine1()));
        changes.append(verboseSetStreetAddress2(subject, address == null ? null : address.getLine2()));
        changes.append(verboseSetCity(subject, address == null ? null : address.getCity()));
        changes.append(verboseSetZip(subject, address == null ? null : address.getZipAsString()));

        changes.append(verboseSetPrimaryContactNumber(subject, phones == null ? null : canonicalPhoneNumber(phones.getPrimary())));
        changes.append(verboseSetSecondaryContactNumber(subject, phones == null ? null : canonicalPhoneNumber(phones.getSecondary())));
        changes.append(verboseSetGenderEmpi(subject, genderEmpi == null ? null : genderEmpi));

        // what date to choose if dobString is null?
        if (patient.getDobString() != null) {
            try {
                DateFormat dateFormat = DateUtility.monthDayYear();
                java.util.Date dobDate0 = dateFormat.parse(patient.getDobString());
                java.util.Date dobDate = DateUtility.adjustDateToHMS(dobDate0,
                        6, 0, 0); // mid-day, so daylight savings does not change date

                changes.append(verboseSetBirthdate(subject, dobDate));
            } catch (ParseException pe) {
                SchedulerRuntimeException.logDontThrow("Could not parse: " + patient.getDobString());
            }
        }

        imposeAndSaveMrn(subject, empiMrn, originalSubjectMrn, changes);

        imposeState(subject, patient, changes);
        imposeCountry(subject, patient, changes);
        imposeRace(subject, patient, changes);
        imposeEthnicity(subject, patient, changes);

        return changes.toString();
    }

    // nice if the language somehow allowed us to abstract / macro-ize the following similar methods
    ////////////////////////////////////

    void imposeAndSaveMrn(Subject subject, EmpiSubjectDto.Mrn empiMrn, SubjectMrn originalSubjectMrn, StringBuilder changes) {

        if (empiMrn == null) {
            SchedulerRuntimeException.logDontThrow("Could not impose new value of Mrn for Subject: " + subject.getId());
        }

        else {
            String change = verboseUpdateMrn(originalSubjectMrn, empiMrn.getStatus());
            if (!change.isEmpty()) {
                changes.append(change);

                epicSubjectDAO.updateEntity(originalSubjectMrn);
            }
        }
    }

    void imposeState(Subject subject, EmpiSubjectDto.Patient patient, StringBuilder changes) {
        String stateString = null;
        EmpiSubjectDto.Address address = patient.getAddress();

        try {
            State stateEntity = null;

            if (address != null && MiscUtil.isNonNullNonEmpty(address.getState())) {
                stateString = address.getState();

                stateEntity = (State)findEntityByFieldString("State", "name", stateString);
                if (stateEntity == null) {
                    throw new IllegalArgumentException("Null lookup result for State");
                }
                changes.append(verboseSetState(subject, stateEntity));
            }
            else {
                changes.append(verboseSetState(subject, null));
            }
        }
        catch (Exception e) {
            SchedulerRuntimeException.logDontThrow("Could not impose new value of State, " + stateString + ", for Subject: " + subject.getId(), e);
        }
    }

    void imposeCountry(Subject subject, EmpiSubjectDto.Patient patient, StringBuilder changes) {
        String countryString = null;
        EmpiSubjectDto.Address address = patient.getAddress();

        try {
            Country countryEntity = null;

            if (address != null && MiscUtil.isNonNullNonEmpty(address.getCountry())) {
                countryString = address.getCountry();

                countryEntity = (Country)findEntityByFieldString("Country", "name", countryString);
                if (countryEntity == null) {
                    throw new IllegalArgumentException("Null lookup result for Country");
                }
                changes.append(verboseSetCountry(subject, countryEntity));
            }
            else {
                changes.append(verboseSetCountry(subject, null));
            }
        }
        catch (Exception e) {
            SchedulerRuntimeException.logDontThrow("Could not impose new value of Country, " + countryString + ", for Subject: " + subject.getId(), e);
        }
    }
    void imposeRace(Subject subject, EmpiSubjectDto.Patient patient, StringBuilder changes) {
        String raceString = null;
        try {
            Race raceEntity = null;

            if (patient.getOtherPid() != null && MiscUtil.isNonNullNonEmpty(patient.getOtherPid().getRace())) {
                raceString = patient.getOtherPid().getRace();

                raceEntity = (Race)findEntityByFieldString("Race", "name", raceString);
                if (raceEntity == null) {
                    throw new IllegalArgumentException("Null lookup result for Race");
                }
                changes.append(verboseSetRace(subject, raceEntity));
            }
            else {
                changes.append(verboseSetRace(subject, null));
            }
        }
        catch (Exception e) {
            SchedulerRuntimeException.logDontThrow("Could not impose new value of Race, " + raceString + ", for Subject: " + subject.getId(), e);
        }
    }
    void imposeEthnicity(Subject subject, EmpiSubjectDto.Patient patient, StringBuilder changes) {
        String ethnicityString = null;
        try {
            Ethnicity ethnicityEntity = null;

            if (patient.getEthnicity() != null &&
                    patient.getEthnicity().getEthnic1() != null &&
                    ! patient.getEthnicity().getEthnic1().isEmpty()) {
                ethnicityString = patient.getEthnicity().getEthnic1();

                ethnicityEntity = epicSubjectService.lookupEmpiEthnicityString(ethnicityString);
                if (ethnicityEntity == null) {
                    throw new IllegalArgumentException("Null lookup result for Ethnicity");
                }
                changes.append(verboseSetEthnicity(subject, ethnicityEntity));
            }
            else {
                ethnicityEntity = epicSubjectService.lookupEmpiEthnicityString(null);

                changes.append(verboseSetEthnicity(subject, ethnicityEntity));
            }
        }
        catch (Exception e) {
            SchedulerRuntimeException.logDontThrow("Could not impose new value of Ethnicity, " + ethnicityString + ", for Subject: " + subject.getId(), e);
        }
    }

    public BaseEntity findEntityByFieldString(String table, String column, String columnValue) {
        return epicSubjectDAO.findEntityByFieldString(table, column, columnValue);
    }

    public boolean refreshExternalSubject(SubjectMrn subjectMrn, boolean tolerateSomeTimeouts) {
        boolean result = true; // optimism

        String site = subjectMrn.getSite();
        Subject subject = subjectMrn.getSubject();

        String mrn = subjectMrn.getMrn();
        String decryptedMrn = SubjectDataEncryptor.decrypt(mrn);
        Subject decryptedSubject = SubjectDataEncryptor.decryptSubject(subject);

        Integer id = decryptedSubject.getId();

        ExternalSubjectQueryBuilder externalSubjectQueryBuilder = new ExternalSubjectQueryBuilder();
        externalSubjectQueryBuilder.mrn(decryptedMrn).mrnSite(site);
        EmpiSubjectDto empiSubjectDto = epicSubjectService.getSubjectsAsEmpiSubjectDto(externalSubjectQueryBuilder, tolerateSomeTimeouts);

        if (sanityCheckEmpiMatchCount(empiSubjectDto, site, id)) {

            String changes = imposeUpon(decryptedSubject, empiSubjectDto, site, subjectMrn);

            String logMessage = "";
            if (!changes.isEmpty()) {
                try {
                    epicSubjectDAO.encryptAndSave(decryptedSubject);

                    String wrappedChanges = "Subject " + id + ". Mrn " + decryptedMrn + "-" + subjectMrn.getSite() + "-" + subjectMrn.getStatus() + ": " + changes;
                    String encryptedChanges = SubjectDataEncryptor.encrypt(wrappedChanges);
                    int changesLength = encryptedChanges.length();

                    logMessage = prefix2(site, id) + "There are changes for Subject: " + id +
                            ". Encrypted description has length: " + changesLength;
                    LOG.info(logMessage);

                    epicSubjectDAO.logNightlyBatchDeltas(encryptedChanges);
                }
                catch (Exception e) {
                    SchedulerRuntimeException.logAndThrow(logMessage, e);
                }

            } else {
                String message = prefix2(site, id) + "No Changes for Subject: " + id;
                LOG.info(message);
                System.out.println(message + ". MRN=" + decryptedMrn + ". Site=" + subjectMrn.getSite());
            }
        } else {
            String message = prefix1(site, id) + "No (Unique) match from EMPI for Subject: " + id;
            LOG.info(message);
            System.out.println(message + ". MRN=" + decryptedMrn + ". Site=" + subjectMrn.getSite());
            result = false;
        }
        return result;
    }

    //////////////// verbose setters ///////////

    public String verboseSetPuid(Subject subject, String newOne) {

        String result = "";
        if (differentStringsIgnoreCase(subject.getPuid(), newOne)) {
            result = "puid:'" + subject.getPuid() + "'" + ARROW + "'" + newOne + "'\n";
            subject.setPuid(newOne);
        }
        return result;
    }

    public String verboseSetLastName(Subject subject, String newOne) {

        String result = "";
        if (differentStringsIgnoreCase(subject.getLastName(), newOne)) {
            result = "lastName:'" + subject.getLastName() + "'" + ARROW + "'" + newOne + "'\n";
            subject.setLastName(newOne);

            subject.setDerivedFullName();
        }
        return result;
    }

    public String verboseSetFirstName(Subject subject, String newOne) {

        String result = "";
        if (differentStringsIgnoreCase(subject.getFirstName(), newOne)) {
            result = "firstName:'" + subject.getFirstName() + "'" + ARROW + "'" + newOne + "'\n";
            subject.setFirstName(newOne);

            subject.setDerivedFullName();
        }
        return result;
    }

    public String verboseSetMiddleName(Subject subject, String newOne) {

        String result = "";
        if (differentStringsIgnoreCase(subject.getMiddleName(), newOne)) {
            result = "middleName:'" + subject.getMiddleName() + "'" + ARROW + "'" + newOne + "'\n";
            subject.setMiddleName(newOne);
        }
        return result;
    }

    public String verboseSetStreetAddress1(Subject subject, String newOne) {

        String result = "";
        if (differentStringsIgnoreCase(subject.getStreetAddress1(), newOne)) {
            result = "streetAddress1:'" + subject.getStreetAddress1() + "'" + ARROW + "'" + newOne + "'\n";
            subject.setStreetAddress1(newOne);
        }
        return result;
    }

    public String verboseSetStreetAddress2(Subject subject, String newOne) {

        String result = "";
        if (differentStringsIgnoreCase(subject.getStreetAddress2(), newOne)) {
            result = "streetAddress2:'" + subject.getStreetAddress2() + "'" + ARROW + "'" + newOne + "'\n";
            subject.setStreetAddress2(newOne);
        }
        return result;
    }

    public String verboseSetCity(Subject subject, String newOne) {

        String result = "";
        if (differentStringsIgnoreCase(subject.getCity(), newOne)) {
            result = "city:'" + subject.getCity() + "'" + ARROW + "'" + newOne + "'\n";
            subject.setCity(newOne);
        }
        return result;
    }

    public String verboseSetZip(Subject subject, String newOne) {

        String result = "";
        if (differentStringsIgnoreCase(subject.getZip(), newOne)) {
            result = "zip:'" + subject.getZip() + "'" + ARROW + "'" + newOne + "'\n";
            subject.setZip(newOne);
        }
        return result;
    }

    public String verboseSetPrimaryContactNumber(Subject subject, String newOne) {

        String result = "";
        if (differentStringsIgnoreCase(subject.getPrimaryContactNumber(), newOne)) {
            result = "primaryContactNumber:'" + subject.getPrimaryContactNumber() + "'" + ARROW + "'" + newOne + "'\n";
            subject.setPrimaryContactNumber(newOne);
        }
        return result;
    }
    public String verboseSetSecondaryContactNumber(Subject subject, String newOne) {

        String result = "";
        if (differentStringsIgnoreCase(subject.getSecondaryContactNumber(), newOne)) {
            result = "secondaryContactNumber:'" + subject.getSecondaryContactNumber() + "'" + ARROW + "'" + newOne + "'\n";
            subject.setSecondaryContactNumber(newOne);
        }
        return result;
    }

    public String verboseSetGenderEmpi(Subject subject, String newOne) {
        String result = "";
        if (differentStringsIgnoreCase(subject.getGenderEmpi(), newOne)) {
            result = "genderEmpi:'" + (subject.getGenderEmpi() == null ? "null" : subject.getGenderEmpi()) + "'" +
                    ARROW + "'" + (newOne == null ? "null" : newOne) + "'\n";
            subject.setGenderEmpi(newOne);
        }
        return result;
    }

    public String verboseUpdateMrn(SubjectMrn subjectMrn, String newOne) {

        String result = "";
        String status = subjectMrn.getStatus();
        if ( differentStringsIgnoreCase(status, newOne)) {
            result = "mrn status:'" + status + "'" + ARROW + "'" + newOne + "'\n";
            subjectMrn.setStatus(newOne);
        }
        return result;
    }

    public String verboseSetBirthdate(Subject subject, java.util.Date newOne) {
        String result = "";

        java.util.Date oldOne = subject.getBirthdate();
        String oldDateString = DateUtility.format(DateUtility.yearMonthDay(), oldOne);
        String newDateString = DateUtility.format(DateUtility.yearMonthDay(), newOne);

        if (differentObjects(oldDateString, newDateString)) {
            result = "birthdate:'" +
                    oldDateString + "'" + ARROW + "'" +
                    newDateString + "'\n";
            subject.setBirthdate(newOne);
        }
        return result;
    }

    public String verboseSetState(Subject subject, State newOne) {
        String result = "";
        if (differentObjects(subject.getState(), newOne)) {
            result = "state:'" + (subject.getState() == null ? "null" : subject.getState().getName())
                    + "'" + ARROW + "'" + (newOne == null ? "null" : newOne.getName()) + "'\n";
            subject.setState(newOne);
        }
        return result;
    }

    public String verboseSetCountry(Subject subject, Country newOne) {
        String result = "";
        if (differentObjects(subject.getCountry(), newOne)) {
            result = "country:'" + (subject.getCountry() == null ? "null" : subject.getCountry().getName())
                    + "'" + ARROW + "'" + (newOne == null ? "null" : newOne.getName()) + "'\n";
            subject.setCountry(newOne);
        }
        return result;
    }

    public String verboseSetRace(Subject subject, Race newOne) {
        String result = "";
        if (differentObjects(subject.getRace(), newOne)) {
            result = "race:'" + (subject.getRace() == null ? "null" : subject.getRace().getName())
                    + "'" + ARROW + "'" + (newOne == null ? "null" : newOne.getName()) + "'\n";
            subject.setRace(newOne);
        }
        return result;
    }

    public String verboseSetEthnicity(Subject subject, Ethnicity newOne) {
        String result = "";
        if (differentObjects(subject.getEthnicity(), newOne)) {
            result = "ethnicity:'" + (subject.getEthnicity() == null ? "null" : subject.getEthnicity().getName())
                    + "'" + ARROW + "'" + (newOne == null ? "null" : newOne.getName()) + "'\n";
            subject.setEthnicity(newOne);
        }
        return result;
    }
}
