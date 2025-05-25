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
/**
 *
 */
package edu.harvard.catalyst.scheduler.persistence;

import com.google.common.collect.Lists;
import edu.harvard.catalyst.hccrc.core.util.RichList;
import edu.harvard.catalyst.scheduler.core.SchedulerRuntimeException;
import edu.harvard.catalyst.scheduler.dto.response.Address;
import edu.harvard.catalyst.scheduler.dto.response.MrnInfoDTO;
import edu.harvard.catalyst.scheduler.dto.response.SubjectDetailResponse;
import edu.harvard.catalyst.scheduler.dto.response.SubjectsResponseDTO;
import edu.harvard.catalyst.scheduler.entity.*;
import edu.harvard.catalyst.scheduler.util.DateUtility;
import edu.harvard.catalyst.scheduler.util.MiscUtil;
import edu.harvard.catalyst.scheduler.util.SubjectDataEncryptor;
import org.hibernate.Session;
import org.hibernate.query.Query;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Predicate;

import static edu.harvard.catalyst.hccrc.core.util.ListUtils.enrich;

@Repository
@Transactional
@Component
public class SubjectDAO extends SiteDAO {

    public void createSubject(final Subject entity) {
        final Subject subject = entity == null ? null : SubjectDataEncryptor.encryptSubjectInPlace(entity);
        final Session session = session();

        session.save(subject);

        session.flush();
    }

    public void saveSubjectMrn(SubjectMrn newMrn) {
        final Session session = session();

        String encryptedMrnVal = SubjectDataEncryptor.encrypt(newMrn.getMrn());
        newMrn.setMrn(encryptedMrnVal);

        session.saveOrUpdate(newMrn);

        session.flush();
    }

    public SubjectMrn getSubjectMrnForSubject(SubjectMrn subjectMrn) {
        final StringBuilder findSubjectMrnQueryString = new StringBuilder();
        String encryptedMrn = SubjectDataEncryptor.encrypt(subjectMrn.getMrn());
        //does this subject mrn already exist for this subject?
        findSubjectMrnQueryString.append("FROM SubjectMrn sm WHERE sm.mrn = :mrn and (sm.site is NULL or sm.site = " +
                                         ":site) and sm.subject = :subject");

        final Query query = newQuery(findSubjectMrnQueryString.toString());
        query.setParameter("mrn", encryptedMrn);
        query.setParameter("site", subjectMrn.getSite());
        query.setParameter("subject", subjectMrn.getSubject());

        return (SubjectMrn) query.uniqueResult();
    }

    public void encryptAndSave(final Subject entity) {
        if (entity != null) {
            final Subject subject = SubjectDataEncryptor.encryptSubjectInPlace(entity);
            final Session session = session();
            session.update(subject);
            session.flush();
        }
    }

    public void logNightlyBatchDeltas(final String encryptedChanges) {

        NightlyBatchChanges nightlyBatchChanges = new NightlyBatchChanges();
        nightlyBatchChanges.setChanges(encryptedChanges);
        createEntity(nightlyBatchChanges);
    }

    public void save(final Subject subject) {
        final Session session = session();
        session.update(subject);
        session.flush();
    }

    @SuppressWarnings("unchecked")
    public List<Subject> findAllSubjectsHql() {
        final String findSubjects = "SELECT s FROM Subject s ";

        return newQuery(findSubjects).list();
    }

    static int CHUNK_SIZE = 200;

    int findCountOf(final String tableName) {
        final String queryString = "SELECT count(x) FROM " + tableName + " x";
        Query query = newQuery(queryString);
        Long count = (Long) query.getSingleResult();
        return count.intValue();
    }
    public int findNumberOfSubjects() {
        return findCountOf("Subject");
    }

    public int findNumberOfSubjectMrns() {
        return findCountOf("SubjectMrn");
    }

    public List<Subject> findAllSubjectsInChunks() {
        int numSubjects = findNumberOfSubjects();
        List<Subject> allSubjects = Lists.newArrayList();

        String findAllSubjects = "FROM Subject";
        Query query = newQuery(findAllSubjects);
        for (int start = 0; start < numSubjects; start += CHUNK_SIZE) {

            query.setFirstResult(start);
            query.setMaxResults(CHUNK_SIZE);

            List<Subject> someSubjects = query.list();
            allSubjects.addAll(someSubjects);
        }

        return allSubjects;
    }

    public List<SubjectMrn> findAllSubjectMrns() {
        int numSubjectMrns = findNumberOfSubjectMrns();
        List<SubjectMrn> allSubjectsMrns = Lists.newArrayList();

        // reset without projection
        CriteriaQueryHelper criteriaQueryHelper = new CriteriaQueryHelper(session(), SubjectMrn.class);
        Query query = criteriaQueryHelper.getQuery();

        for (int start = 0; start < numSubjectMrns; start += CHUNK_SIZE) {
            query.setFirstResult(start);
            query.setMaxResults(CHUNK_SIZE);

            List<SubjectMrn> someSubjectMrns = query.list();
            allSubjectsMrns.addAll(someSubjectMrns);
        }

        return allSubjectsMrns;
    }

    static final Predicate<Row> subjectsPredicate(final Optional<String> normalizedFilterString) {
        final boolean filterStringIsPresent = normalizedFilterString.isPresent();

        return row -> {
            if (filterStringIsPresent) {
                final String filterStringToUse = normalizedFilterString.get();

                final boolean mrnMatches = row.mrn.contains(filterStringToUse);
                final boolean lastNameMatches = row.subjectData.name.lastName.contains(filterStringToUse);

                return mrnMatches || lastNameMatches;
            }

            return true;
        };
    }

    static Optional<String> normalizeFilterString(final String filterString) {
        return Optional.ofNullable(filterString).map(fs -> fs.trim().toUpperCase()).filter(fs -> !fs.isEmpty());
    }

    static final class Name {
        private final String lastName;
        private final String firstName;
        private final String middleName;

        public Name(final String lastName, final String firstName, final String middleName) {
            this.lastName = lastName;
            this.firstName = firstName;
            this.middleName = middleName;
        }

        public String getLastName() {
            return lastName;
        }

        public String getFirstName() {
            return firstName;
        }

        public String getMiddleName() {
            return middleName;
        }
    }

    static final class SubjectData {
        private final Name name;
        private final Date dob;
        private final String gender;
        private final Address address;
        private final String primaryContact;

        public Name getName() {
            return name;
        }

        public Date getDob() {
            return dob;
        }

        public String getGender() {
            return gender;
        }

        public Address getAddress() {
            return address;
        }

        public String getPrimaryContact() {
            return primaryContact;
        }

        public SubjectData(Name name, final Date dob, final String gender, Address address, String primaryContact) {
            this.name = name;
            this.dob = dob;
            this.gender = gender;
            this.address = address;
            this.primaryContact = primaryContact;
        }
    }

    static final class Row {
        private final Integer id;
        private final String mrn;
        private final Boolean status;
        private final SubjectData subjectData;


        public Row(final Integer id, SubjectData subjectData, final String mrn, final Boolean status) {
            super();
            this.id = id;
            this.mrn = mrn;
            this.subjectData = subjectData;
            this.status = status;
        }

        public static Row fromSubject(final SubjectMrn subjectMrn) {
            Subject subject = subjectMrn.getSubject();
            String gender = "";
            if (subject.getGender() != null) {
                gender = subject.getGender().getName();
            }

            Address address = new Address();

            if (subject.getCity() != null) {
                address.setCity(subject.getCity());
            }

            if (subject.getState() != null) {
                address.setStateName(subject.getState().getName().toUpperCase());
            }

            RowBuilder rowBuilder = new RowBuilder();

            rowBuilder.id(subject.getId())
                    .lastName(subject.getLastName())
                    .firstName(subject.getFirstName())
                    .middleName(subject.getMiddleName())
                    .mrn(subjectMrn.getMrn())
                    .dob(subject.getBirthdate())
                    .gender(gender)
                    .address(address)
                    .primaryContact(subject.getPrimaryContactNumber())
                    .status(subject.getActive());

            return rowBuilder.createRow();
        }

        public Row decrypt() {

            RowBuilder rowBuilder = new RowBuilder();

            subjectData.address.setAddressLine1(SubjectDataEncryptor.decrypt(subjectData.address.getAddressLine1()));
            subjectData.address.setAddressLine2(SubjectDataEncryptor.decrypt(subjectData.address.getAddressLine2()));
            subjectData.address.setCity(SubjectDataEncryptor.decrypt(subjectData.address.getCity()));
            subjectData.address.setZipCode(SubjectDataEncryptor.decrypt(subjectData.address.getZipCode()));

            rowBuilder.id(id)
                    .lastName(SubjectDataEncryptor.decrypt(subjectData.getName().getLastName()))
                    .firstName(SubjectDataEncryptor.decrypt(subjectData.getName().getFirstName()))
                    .middleName(SubjectDataEncryptor.decrypt(subjectData.getName().getMiddleName()))
                    .mrn(SubjectDataEncryptor.decrypt(mrn))
                    .dob(subjectData.getDob())
                    .gender(subjectData.getGender())
                    .address(subjectData.getAddress())
                    .primaryContact(
                            subjectData.primaryContact == null ? null :
                            SubjectDataEncryptor.decrypt(subjectData.primaryContact).replaceAll("[^a-zA-Z0-9]+", ""))
                    .status(status);

            return rowBuilder.createRow();
        }

        public SubjectsResponseDTO toSubjectsResponse() {
            return new SubjectsResponseDTO(id, subjectData.getName().getLastName(), subjectData.getName()
                    .getFirstName(),
                                           subjectData.getName().getMiddleName(), subjectData.getDob(), subjectData
                                                   .getGender(),
                                           subjectData.getAddress(), subjectData.getPrimaryContact(), status
            );
        }

        public static class RowBuilder {
            private Integer id;
            private String lastName;
            private String firstName;
            private String middleName;
            private String mrn;
            private Date dob;
            private String gender;
            private Address address;
            private String primaryContact;
            private Boolean status;

            public RowBuilder id(final Integer id) {
                this.id = id;
                return this;
            }

            public RowBuilder lastName(final String lastName) {
                this.lastName = lastName;
                return this;
            }

            public RowBuilder firstName(final String firstName) {
                this.firstName = firstName;
                return this;
            }

            public RowBuilder middleName(final String middleName) {
                this.middleName = middleName;
                return this;
            }

            public RowBuilder mrn(String mrn) {
                this.mrn = mrn;
                return this;
            }

            public RowBuilder dob(final Date dob) {
                this.dob = dob;
                return this;
            }

            public RowBuilder gender(final String gender) {
                this.gender = gender;
                return this;
            }

            public RowBuilder address(final Address address) {
                this.address = address;
                return this;
            }

            public RowBuilder primaryContact(final String primaryContact) {
                this.primaryContact = primaryContact;
                return this;
            }

            public RowBuilder status(final Boolean status) {
                this.status = status;
                return this;
            }

            public Row createRow() {
                Name name = new Name(lastName, firstName, middleName);
                SubjectData subjectData = new SubjectData(name, dob, gender, address, primaryContact);
                return new Row(id, subjectData, mrn, status);
            }
        }
    }

    static final String encryptIgnoringLiteralNullsNullsOrBlanks(final String rawField) {
        if (!isLiteralNullNullOrBlank(rawField)) {
            return SubjectDataEncryptor.encrypt(rawField.toUpperCase());
        }

        return null;
    }

    static boolean isLiteralNullNullOrBlank(final String lastName) {
        return lastName == null || lastName.equalsIgnoreCase("null") || lastName.isEmpty();
    }

    public Subject findById(final int id) {
        return this.findById(Subject.class, id);
    }

    public Subject findBySubjectId(final int id) {
        final Subject subject = this.findById(Subject.class, id);
        if (subject == null) {
            SchedulerRuntimeException.logAndThrow("No Subject found with ID: " + id);
        }
        // NB: SubjectDataEncryptor.decryptSubject() decrypts and returns a copy
        // of its param
        return SubjectDataEncryptor.decryptSubject(subject);
    }

    public SubjectDetailResponse getSubjectDataById(final int subjectId) {
        final Subject subject = findBySubjectId(subjectId);
        final Subject subjectCopy = Subject.defensiveCopy(subject);
        // NB: Subject.defensiveCopy() can never return null
        return new SubjectDetailResponse(subjectCopy);
    }

    public StudySubject findStudySubjectById(final Integer id) {
        final StudySubject studySubject = this.findById(StudySubject.class, id);
        return studySubject;
    }

    public SubjectMrn findSubjectMrnById(final Integer id) {
        final SubjectMrn subjectMrn = this.findById(SubjectMrn.class, id);
        return subjectMrn;
    }

    public List<Subject> filterSubjectByLastNames(final String lastName) {
        final String encName = lastName == null ? null : SubjectDataEncryptor.encrypt(lastName.toUpperCase());

        final List<Subject> list = findSubjectByLastNames(encName);

        if (list != null) {
            // NB: SubjectDataEncryptor::decryptSubject makes and returns
            // defensive copies
            return enrich(list).map(SubjectDataEncryptor::decryptSubject).toList();
        }

        return new ArrayList<>();
    }

    public List<Subject> getSubjectByLastName(final String lastName, final InstitutionRole institutionRole) {

        final boolean isStudyStaff = InstitutionRole.isStudyStaff(institutionRole);

        final Predicate<Subject> matches = subject -> isStudyStaff ? lastNameIs(subject, lastName)
                                                                   : lastNameMatches(subject, lastName);

        final List<Subject> subjects = findAllSubjectsInChunks();

        // NB: SubjectDataEncryptor::decryptSubject makes and returns defensive
        // copies
        return enrich(subjects).map(SubjectDataEncryptor::decryptSubjectLastName).filter(matches).toList();
    }

    private static boolean lastNameIs(final Subject subject, final String lastName) {
        return subject.getLastName().equalsIgnoreCase(lastName);
    }

    private static boolean lastNameMatches(final Subject subject, final String lastName) {
        return subject.getLastName().toLowerCase().contains(lastName.toLowerCase());
    }

    public List<Subject> filterSubjectsByMRN(
            final String mrn,
            final InstitutionRole institutionRole,
            final String institution
    ) {
        if (mrn != null) {
            final List<Subject> encryptedList = findAllSubjectsInChunks();

            // NB: SubjectDataEncryptor.decryptSubject makes and returns
            // defensive copies
            final RichList<Subject> decryptedList = enrich(encryptedList).map
                    (SubjectDataEncryptor::decryptSubjectMrnWithinSubject);

            final boolean isStudyStaff = InstitutionRole.isStudyStaff(institutionRole);

            final Predicate<Subject> shouldBeReturned = (final Subject subject) -> isStudyStaff
                                                                                   ? mrnMatchesIgnoreCaseExactly
                                                                                           (subject, mrn)
                                                                                   :
                                                                                   mrnMatchesIgnoreCaseAtLeastPartially(subject, mrn);

            return decryptedList.sortWith(subjectComparator).filter(shouldBeReturned).toList();
        }

        return new ArrayList<>();
    }

    static boolean mrnMatchesIgnoreCaseExactly(final Subject subject, final String desiredMrn) {

        Set<SubjectMrn> mrnSet = subject.getSubjectMrnSet();

        Optional<SubjectMrn> match = mrnSet.stream()
                .filter(sm -> sm.getMrn().equalsIgnoreCase(desiredMrn))
                .findFirst();

        return match.isPresent();
    }

    static boolean mrnMatchesIgnoreCaseAtLeastPartially(final Subject subject, final String desiredMrn) {

        Set<SubjectMrn> mrnSet = subject.getSubjectMrnSet();

        Optional<SubjectMrn> match = mrnSet.stream()
                .filter(sm -> sm.getMrn().toUpperCase().contains(desiredMrn.toUpperCase()))
                .findFirst();

        return match.isPresent();
    }

    private static final Comparator<Subject> subjectComparator = (o1, o2) -> o1.getLastName().compareToIgnoreCase
            (o2.getLastName());

    public List<State> getStates() {
        return this.findAll(State.class);
    }

    public List<Race> getRaces() {
        return this.findAll(Race.class);
    }

    public List<Ethnicity> getEthnicities() {
        return this.findAll(Ethnicity.class);
    }

    public List<Gender> getGenders() {
        return this.findAll(Gender.class);
    }

    public List<Country> getCountries() {
        return this.findAll(Country.class);
    }

    @SuppressWarnings("unchecked")
    public List<Subject> findSubjectByLastNames(final String lastName) {
        final CriteriaQueryHelper criteriaHelper = new CriteriaQueryHelper(session(), Subject.class);

        criteriaHelper.whereEquals("lastName", lastName);

        return criteriaHelper.getQuery().list();
    }

    public boolean mrnInfoExists(final MrnInfoDTO mrnInfoDTO) {

        List<SubjectMrn> resultRows = findMrn(mrnInfoDTO);
        return !resultRows.isEmpty();
    }

    public List<SubjectMrn> findMrn(final MrnInfoDTO mrnInfoDTO){
        List<SubjectMrn> resultRows = new ArrayList<>();
        if (mrnInfoDTO != null || mrnInfoDTO.getValue() != null) {

            final StringBuffer findSubjects = new StringBuffer();
            findSubjects.append("FROM SubjectMrn sm WHERE sm.mrn = :mrn");

            if (mrnInfoDTO.getInstitution() == null) {
                findSubjects.append(" and sm.site IS NULL");
            } else {
                findSubjects.append(" and sm.site = :site");
            }

            final Query query = newQuery(findSubjects.toString());

            String encryptedMrn = SubjectDataEncryptor.encrypt(mrnInfoDTO.getValue());
            query.setParameter("mrn", encryptedMrn);

            if (mrnInfoDTO.getInstitution() != null) {

                query.setParameter("site", mrnInfoDTO.getInstitution());
            }

            resultRows = query.list();
        }
        return resultRows;
    }

    public Gender findByGenderId(final Integer id) {
        return this.findById(Gender.class, id);
    }

    public Gender findGenderByCode(final Class<Gender> genderClass, final String code) {
        final CriteriaQueryHelper criteriaHelper = new CriteriaQueryHelper(session(), genderClass);

        criteriaHelper.whereEquals("code", code);

        return (Gender) criteriaHelper.getQuery().uniqueResult();
    }

    public Race findByRaceId(final Integer id) {
        return this.findById(Race.class, id);
    }

    public Ethnicity findByEthnicityId(final Integer id) {
        return this.findById(Ethnicity.class, id);
    }

    public State findByStateId(final Integer id) {
        return this.findById(State.class, id);
    }

    public Country findCountryById(final Integer id) {
        return this.findById(Country.class, id);
    }

    public SubjectsResponseDTO getSearchSubjects(
            final String lastName,
            final String firstName,
            final String mrn,
            final String bday,
            final List<String> genderList
    ) {
        final String encLastName = encryptIgnoringLiteralNullsNullsOrBlanks(lastName);
        final String encFirstName = encryptIgnoringLiteralNullsNullsOrBlanks(firstName);
        final String encMrn = encryptIgnoringLiteralNullsNullsOrBlanks(mrn);

        final Date date = (!bday.isEmpty()) ? DateUtility.parse(DateUtility.monthDayYear(), bday) : null;

        //convert genders to a list of integers
        List<Integer> genderIdList = new ArrayList<Integer>();
        for (String gender : genderList) {
            genderIdList.add(Integer.parseInt(gender));
        }

        final List<SubjectsResponseDTO> resultRows = findSubjectByLastAndFirstNameMrnBdateGender(encLastName,
                                                                                                 encFirstName,
                                                                                                 encMrn, date,
                                                                                                 genderIdList);

        final Long total = (long) resultRows.size();


        return new SubjectsResponseDTO(resultRows, total);
    }

    public List<SubjectsResponseDTO> findSubjectByLastAndFirstNameMrnBdateGender(
            final String lastName, final String firstName, final String medicalRecordNumber,
            final Date birthDate, final List<Integer> genderIdList
    ) {
        final String findSubjects = "FROM SubjectMrn sm" +
                                    findSubjectWhereClause(lastName, firstName, medicalRecordNumber, birthDate,
                                                           genderIdList);

        final Query query = newQuery(findSubjects);

        setSubjectQueryParameters(query, lastName, firstName, medicalRecordNumber, birthDate, genderIdList);

        @SuppressWarnings("unchecked")
        final List<SubjectMrn> resultRows = query.list();

        // todo make sure mrn makes it into dto
        return enrich(resultRows).map(s -> Row.fromSubject(s)).map(Row::decrypt).map(Row::toSubjectsResponse).toList();
    }

    private String findSubjectWhereClause(
            final String lastName, final String firstName, final String medicalRecordNumber,
            final Date birthDate, final List<Integer> genderIdList
    ) {
        String whereClause = "";

        if (medicalRecordNumber != null) {
            whereClause = " WHERE sm.mrn = :medicalRecordNumber";
        } else if (lastName != null && firstName != null && birthDate != null) {
            whereClause = " WHERE sm.subject.lastName = :lastName and sm.subject.firstName = :firstName and sm" +
                          ".subject.birthdate = :birthDate";
        }

        if(MiscUtil.isNonNullNonEmpty(genderIdList))
        {
            whereClause += " and sm.subject.gender.id in (:genderIdList)";
        }

        return whereClause;
    }

    private void setSubjectQueryParameters(
            final Query query, final String lastName, final String firstName, final String medicalRecordNumber,
            final Date birthDate, final List<Integer> genderIdList
    ) {
        if (lastName != null) {
            query.setParameter("lastName", lastName);
        }

        if (firstName != null) {
            query.setParameter("firstName", firstName);
        }

        if (medicalRecordNumber != null) {
            query.setParameter("medicalRecordNumber", medicalRecordNumber);
        }

        if (birthDate != null) {
            query.setParameter("birthDate", birthDate);
        }

        if(MiscUtil.isNonNullNonEmpty(genderIdList)) {
            query.setParameterList("genderIdList", genderIdList);
        }
    }

    public Subject findInternalSubjectByPuid(final String puid) {
        String encryptedPuid = SubjectDataEncryptor.encrypt(puid);
        String queryString = "Select subject FROM Subject subject WHERE puid = '" + encryptedPuid
                             + "' and subject.archivalStatus IS NULL";

        final Query query = newQuery(queryString);

        Subject result = (Subject) query.uniqueResult();

        return result;
    }

    public SubjectDetailResponse findInternalSubjectByMrn(final List<MrnInfoDTO> mrnInfoDTOList) {
        SubjectDetailResponse subjectDetailResponse = new SubjectDetailResponse();

        if (MiscUtil.isNonNullNonEmpty(mrnInfoDTOList)) {
            final StringBuilder findSubjects = new StringBuilder();

            findSubjects.append("Select sm.subject FROM SubjectMrn sm WHERE sm.subject.archivalStatus IS NULL and (");

            for (int i = 0; i < mrnInfoDTOList.size(); i++) {
                if (i != 0) {
                    findSubjects.append(" or ");
                }

                String mrn = mrnInfoDTOList.get(i).getValue();

                String encryptedMrn = SubjectDataEncryptor.encrypt(mrn);

                String institution = mrnInfoDTOList.get(i).getInstitution();
                findSubjects.append("(sm.mrn = '").append(encryptedMrn);
                findSubjects.append("' and sm.site = '").append(institution).append("')");

                //check if this mrn has leading zeros
                //if so then also look for mrn without any leading zeros
                String mrnMinusLeadingZeros = mrn.replaceAll("^0*", "");

                if (!mrn.equals(mrnMinusLeadingZeros)) {
                    encryptedMrn = SubjectDataEncryptor.encrypt(mrnMinusLeadingZeros);
                    findSubjects.append(" or (sm.mrn = '").append(encryptedMrn);
                    findSubjects.append("' and sm.site = '").append(institution).append("')");
                }
            }

            findSubjects.append(")");

            final Query query = newQuery(findSubjects.toString());

            @SuppressWarnings("unchecked")
            final List<Subject> subjectList = query.list();


            if (!subjectList.isEmpty()) {
                Subject decryptedSubject = SubjectDataEncryptor.decryptSubject(subjectList.get(0));
                subjectDetailResponse = new SubjectDetailResponse(decryptedSubject);
            }
        }

        return subjectDetailResponse;
    }

    public SubjectMrn getSubjectMrnByMrnAndSite(String mrn, String site) {
        final StringBuilder findSubjectMrnQueryString = new StringBuilder();
        String encryptedMrn = SubjectDataEncryptor.encrypt(mrn);
        findSubjectMrnQueryString.append("FROM SubjectMrn sm WHERE sm.subject.archivalStatus IS NULL and sm.mrn = :mrn and (sm.site is NULL or sm.site = " +
                                         ":site) ");

        final Query query = newQuery(findSubjectMrnQueryString.toString());
        query.setParameter("mrn", encryptedMrn);
        query.setParameter("site", site);

        return (SubjectMrn)query.uniqueResult();
    }

    public long getSubjectMrnCountBySubjectId(int subjectId) {
        final StringBuilder findSubjectMrnQueryString = new StringBuilder();
        findSubjectMrnQueryString.append("SELECT COUNT(*) FROM SubjectMrn sm WHERE sm.subject.id = :subjectId ");

        final Query query = newQuery(findSubjectMrnQueryString.toString());
        query.setParameter("subjectId", subjectId);

        return (Long)query.uniqueResult();
    }
}
