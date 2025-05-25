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

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import edu.harvard.catalyst.scheduler.core.SchedulerRuntimeException;
import edu.harvard.catalyst.scheduler.core.Statics;
import edu.harvard.catalyst.scheduler.dto.SubjectsDTO;
import edu.harvard.catalyst.scheduler.dto.response.MrnInfoDTO;
import edu.harvard.catalyst.scheduler.entity.*;
import edu.harvard.catalyst.scheduler.persistence.AppointmentDAO;
import edu.harvard.catalyst.scheduler.persistence.StudyDAO;
import edu.harvard.catalyst.scheduler.persistence.SubjectDAO;
import edu.harvard.catalyst.scheduler.util.SubjectDataEncryptor;
import org.apache.log4j.Logger;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import static edu.harvard.catalyst.hccrc.core.util.ListUtils.*;
import static edu.harvard.catalyst.scheduler.core.Statics.*;


@Component
public class SubjectServiceClassic implements ServiceHelpers {

	private static final Logger LOGGER = Logger.getLogger(SubjectServiceClassic.class);

	static final Function<String, String> CRYPT = SubjectDataEncryptor::encrypt;
	private static final Function<String, String> TO_UPPER = String::toUpperCase;
	static final Function<String, String> CRYPT_UPPER = TO_UPPER.andThen(CRYPT);

	private final SubjectDAO subjectDAO;
	private final StudyDAO studyDAO;
	private final AuditService auditService;
	private final StudyService studyService;
	private final AppointmentDAO appointmentDAO;


	@Autowired
	public SubjectServiceClassic(final AuditService auditService,
								 final SubjectDAO subjectDAO,
								 final StudyDAO studyDAO,
								 final StudyService studyService,
								 final AppointmentDAO appointmentDAO,
								 @Qualifier("encryptionKey") final Key encryptionKey) {
		this.auditService = auditService;
		this.studyService = studyService;
		this.subjectDAO = subjectDAO;
		this.studyDAO = studyDAO;
		this.appointmentDAO = appointmentDAO;

		SubjectDataEncryptor.setEncryptionKey(encryptionKey);
	}

	// Don't use - Needed for spring security cglib proxying
	SubjectServiceClassic() {
		this(null, null, null, null, null, null);
	}

	// @Transactional

	/**
	 * create a subject;  sets the new id of that subject in the subjectDTO in the argument
	 */
	public void createSubjectForDto(final SubjectsDTO subjectDTO,
									final User user,
									final String ipAddress) {

		final Subject subject = createSubject(subjectDTO, user, ipAddress);
		if (subject == null) {
			return;
		}

		subjectDTO.setResult(true);
		subjectDTO.setId(subject.getId());
	}

	private boolean validateSubjectMerge (SubjectsDTO subjectDTO, MrnInfoDTO similarMrnInfoDTO, User user){
		boolean isValid = true;

		if (!subjectDTO.mergeSimilarMrn() && similarMrnInfoDTO != null && user != null) {
			LOGGER.info(SIMILAR_MRN_ALREADY_EXISTS_MISMATCH_CREATE);
			subjectDTO.setResult(false);
			subjectDTO.setErrorMsg("Similar mrn exists");
			subjectDTO.setSimilarMrnAlreadyExistsError(true);
			subjectDTO.setSimilarMrnInfo(similarMrnInfoDTO);
			isValid = false;
		}
		return isValid;
	}

	public boolean mrnHasLeadingZeros(MrnInfoDTO mrnInfoDTO){
		boolean mrnWithLeadingZeros = false;

		if(mrnInfoDTO != null) {
			String mrnMinusLeadingZeros = mrnInfoDTO.getValue().replaceAll("^0*", "");
			if (!mrnMinusLeadingZeros.equals(mrnInfoDTO.getValue())) {
				mrnWithLeadingZeros = true;
			}
		}
		return mrnWithLeadingZeros;
	}

	public void remapBookedVisits(List<BookedVisit> bookedVisits, SubjectMrn primarySubjectMrn, List<String> changeDetails) {
		bookedVisits.stream().forEach( bookedVisit -> {
			changeDetails.add("BookedVisit#" + bookedVisit.getId() + ".subjectMrn:from#" +
							  bookedVisit.getSubjectMrn().getMrn() + ":to#"
							  + primarySubjectMrn.getMrn());
			// no need to change the study
			bookedVisit.setSubjectMrn(primarySubjectMrn);
			appointmentDAO.updateEntity(bookedVisit);
		});
	}

	private void performMergeSimilarSubject(SubjectMrn mergeSubjectMrn, SubjectMrn newSubjectMrn,
											User user, String ipAddress, int studyId){
		List<String> changeDetailsList = new ArrayList<>();

		Subject mergeSubject = mergeSubjectMrn.getSubject();
		Subject newSubject = newSubjectMrn.getSubject();

		final Study newStudy = new Study();

		// Find the study_subject records for the merge subject
		List<StudySubject> mergeStudySubjects = studyDAO.findStudySubjectBySubjectMrn(mergeSubjectMrn);
		mergeStudySubjects.stream().forEach( mergeStudySubject -> {
			Study mergeStudy = mergeStudySubject.getStudy();

			if(studyId == mergeStudy.getId()){
				newStudy.setId(mergeStudy.getId());
			}
			changeDetailsList.add("StudySubject#" + mergeStudySubject.getId() + ".subject:from#" +
							  mergeStudySubject.getSubject().getId() +
							  ":to#" + newSubject.getId());
			mergeStudySubject.setSubjectMrn(newSubjectMrn);
			studyDAO.updateEntity(mergeStudySubject);

			List<BookedVisit> bookedVisits = appointmentDAO.getAllBookedVisitByStudyAndSubjectMrn(mergeStudy, mergeSubjectMrn);
			remapBookedVisits(bookedVisits, newSubjectMrn, changeDetailsList);
		 });

		//update all the other mrns for the merge subject to point to the new subject
		mergeSubject.getSubjectMrnSet().forEach(subjectMrn -> {
			if(!(subjectMrn.getMrn().equals(mergeSubjectMrn.getMrn())
			   && subjectMrn.getSite().equals(mergeSubjectMrn.getSite()))) {
				subjectMrn.setSubject(newSubject);
				subjectDAO.updateEntity(subjectMrn);
			}
		});

		if(studyId != 0 && newStudy.getId() == null){
			addSubjectToStudy(studyId, user, ipAddress, newSubjectMrn);

		}
		//log merging activity
		String newSubjectDescription = "subject with ID " + newSubject.getId() + " and MRN "
									   + mergeSubjectMrn.getMrn() + " and Site " + newSubjectMrn.getSite();
		String mergeSubjectDescription = "subject with ID " + mergeSubject.getId() + " and MRN "
										 + mergeSubjectMrn.getMrn() + " and Site " + mergeSubjectMrn.getSite();

		String actionPerformed = "Merge requested by user " +  user.getEcommonsId() + " to resolve similar mrns ";

		changeDetailsList.add(mergeSubjectDescription + " merged into " + newSubjectDescription);
		String changeDetails = String.join(",", changeDetailsList);

		mergeSubject.setArchivalStatus(ArchivalStatus.MERGED);
		subjectDAO.updateEntity(mergeSubject);

		final ActivityLog al = new ActivityLog();
		al.setPerformingUser(user);
		al.setActionPerformed(actionPerformed);
		al.setDate(new Date());
		al.setIpAddress(ipAddress);
		al.setAffectedUser(null);
		al.setAffectedResource(null);
		al.setAffectedSublocation(null);
		al.setAffectedSubject(mergeSubject);
		al.setAffectedStudy(null);
		al.setAffectedVisit(null);
		al.setChangesDetailRequiredField(null);
		al.setChangesDetail(changeDetails);
		al.setAppointmentOverrideReason(null);
		al.setBookedVisit(null);

		subjectDAO.createEntity(al);
	}

	public Subject createSubject(SubjectsDTO subjectDTO, User user, String ipAddress) {
		Subject subject = null;

        if (mrnAlreadyExists(subjectDTO)) {
            LOGGER.info(MRN_ALREADY_EXISTS_MISMATCH_CREATE);
            subjectDTO.setResult(false);
            subjectDTO.setMrnAlreadyExistsError(true);
        }
		else {
			subject = new Subject();
			MrnInfoDTO mrnInfoDTO = subjectDTO.getMrnInfo();
			subject.setActive(Boolean.TRUE);
			subject.setCreatedDate(new Date());

			setSomeEntityValuesViaDto(subject, subjectDTO);

			subjectDAO.createSubject(subject);

			SubjectMrn newSubjectMrn = new SubjectMrn(
					subject,
					mrnInfoDTO.getValue(),
					mrnInfoDTO.getInstitution(),
					mrnInfoDTO.getStatus(),
					null);
			// dao.save() gives ID a value

			subject.getSubjectMrnSet().add(newSubjectMrn);

			subjectDAO.saveSubjectMrn(newSubjectMrn);

			auditService.logSubjectActivity(ipAddress, subject, user, Statics.AUDIT_SUBJECT_CREATE, null, null);
			addSubjectToStudy(subjectDTO.getStudyId(), user, ipAddress, newSubjectMrn);
		}
		return subject;
	}

	private void addSubjectToStudy(int studyId,
								   User user, String ipAddress, SubjectMrn subjectMrn){
		if (studyId > 0) {
			final Study study = studyDAO.findStudyById(studyId);
			addSubjectToStudy(user, ipAddress, subjectMrn, study);
		}
	}

	private SubjectMrn mergeSimilarSubjects(final SubjectsDTO subjectDTO, SubjectMrn similarSubjectMrn,
									 final User user, final String ipAddress) {

		SubjectMrn newSubjectMrn;
		boolean createSubjectWithNewPuid = false;
		Subject internalSubjectByPuid = subjectDAO.findInternalSubjectByPuid(subjectDTO.getPartnersUid());
		MrnInfoDTO mrnInfoDTO = subjectDTO.getMrnInfo();

		if(internalSubjectByPuid == null || internalSubjectByPuid.getId().intValue() == similarSubjectMrn.getSubject().getId().intValue()){
			createSubjectWithNewPuid = true;
		}

		int studyId = subjectDTO.getStudyId();
		subjectDTO.setStudyId(0);

		if(createSubjectWithNewPuid) {
			internalSubjectByPuid = createSubject(subjectDTO, user, ipAddress);
			newSubjectMrn = internalSubjectByPuid.getSubjectMrnSet().stream()
					.filter(sm -> {
						String smDecrypted = SubjectDataEncryptor.decrypt(sm.getMrn());
						return smDecrypted.equals(mrnInfoDTO.getValue()) && sm.getSite().equals(mrnInfoDTO.getInstitution());
					}).findFirst().orElse(null);
		}else{
			newSubjectMrn = new SubjectMrn(
					internalSubjectByPuid,
					mrnInfoDTO.getValue(),
					mrnInfoDTO.getInstitution(),
					mrnInfoDTO.getStatus(),
					null);
			subjectDAO.saveSubjectMrn(newSubjectMrn);
		}

		performMergeSimilarSubject(similarSubjectMrn, newSubjectMrn, user, ipAddress, studyId);

		return newSubjectMrn;
	}

	// @Transactional
	public SubjectsDTO updateSubject(final SubjectsDTO subjectDTO, final User user, final String ipAddress, final boolean allowOnlyOneMrn) {

		Subject subject = subjectDAO.findBySubjectId(subjectDTO.getId());
		SubjectMrn subjectMrn = null;

		boolean mrnHasLeadingZeros = mrnHasLeadingZeros(subjectDTO.getMrnInfo());

		boolean doUpdate = true;
		if(!allowOnlyOneMrn) {
			if(mrnHasLeadingZeros) {
				SubjectMrn similarSubjectMrn = findSimilarMrn(subjectDTO);
				if (similarSubjectMrn != null) {
					String decryptedMrn = SubjectDataEncryptor.decrypt(similarSubjectMrn.getMrn());
					MrnInfoDTO similarMrnInfoDTO = new MrnInfoDTO(decryptedMrn);
					similarMrnInfoDTO.setInstitution(similarSubjectMrn.getSite());
					similarMrnInfoDTO.setStatus(similarSubjectMrn.getStatus());

					if(validateSubjectMerge(subjectDTO, similarMrnInfoDTO, user)) {
						subjectMrn = mergeSimilarSubjects(subjectDTO, similarSubjectMrn, user, ipAddress);
						//if a new subject was created then no update is needed
						if(subject.getId().intValue() != subjectMrn.getSubject().getId()) {
							doUpdate = false;
							subjectDTO.setResult(true);
						}
						subject = subjectMrn.getSubject();
						SubjectDataEncryptor.decryptSubjectInPlace(subject);
						subjectDTO.setId(subject.getId());
					}
					else{
						doUpdate = false;
					}
				}
			}

			SubjectMrn differingPuidSubjectMrn = subjectExistsWithSameMrnAndDiffPuid(subjectDTO);
			if(doUpdate && differingPuidSubjectMrn != null) {
				final Subject newPuidSubject = subjectDAO.findInternalSubjectByPuid(subjectDTO.getPartnersUid());

				//the puid of the subject has changed and there is
				// a subject in the database with this new puid so do a merge
				if(newPuidSubject != null){
					subject = mergeSubjectWithSamePuid(differingPuidSubjectMrn, newPuidSubject, user, ipAddress);
					SubjectDataEncryptor.decryptSubjectInPlace(subject);
					subjectDTO.setId(subject.getId());
				}
			}
		}

		if (doUpdate) {
			MrnInfoDTO mrnInfoDTO = subjectDTO.getMrnInfo();

			SubjectMrn newSubjectMrn = new SubjectMrn(
					subject,
					mrnInfoDTO.getValue(),
					mrnInfoDTO.getInstitution(),
					mrnInfoDTO.getStatus(),
					null
			);

			if (subjectMrn == null) {
				// id for this subject-mrn is added iff we save it
				subjectMrn = subjectDAO.getSubjectMrnForSubject(newSubjectMrn);
			}

			//the mrn above does NOT belong to the subject:
			if (subjectMrn == null) {
				if (mrnAlreadyExists(subjectDTO)) {
					LOGGER.info(MRN_ALREADY_EXISTS_MISMATCH_UPDATE);
					subjectDTO.setMrnAlreadyExistsError(true);
					subjectDTO.setResult(false);
					return subjectDTO;
				}
				//create the SubjectMrn if it does not already exist
				try {
					subjectMrn = createSubjectMrnIfNeeded(allowOnlyOneMrn, subject, newSubjectMrn);
				}
				catch (NoMrnFoundException e) {
					subjectDTO.setErrorMsg(e.getMessage());
					subjectDTO.setResult(false);
					return subjectDTO;
				}
			}

			// if a study was selected to add the subject to:
			if (subjectDTO.getStudyId() > 0) {
				final Study study = studyDAO.findStudyById(subjectDTO.getStudyId());

				if (isSubjectAlreadyInThisStudy(user, subject, study)) {
					subjectDTO.setErrorMsg(SUBJECT_ALREADY_ENROLLED);
					subjectDTO.setResult(false);
					return subjectDTO;
				}

				addSubjectToStudy(user, ipAddress, subjectMrn, study);
			}

			updateSubjectFieldsIfNeeded(subjectDTO, user, ipAddress, subject);

			subjectDTO.setResult(true);
		}
		return subjectDTO;
	}

	private Subject mergeSubjectWithSamePuid(SubjectMrn mergeSubjectMrn, Subject activeSubject, User user, String ipAddress) {
		Subject mergeSubject = mergeSubjectMrn.getSubject();

		List<String> changeDetailsList = new ArrayList<>();

		changeDetailsList.add("SubjectMrn#" + mergeSubjectMrn.getId() + ".subject:from#" +
							 mergeSubjectMrn.getSubject().getId() +
							 ":to#" + activeSubject.getId());
		mergeSubjectMrn.setSubject(activeSubject);
		subjectDAO.updateEntity(mergeSubjectMrn);


		long mergeSubjectMrnCount = subjectDAO.getSubjectMrnCountBySubjectId(mergeSubject.getId());
		if(mergeSubjectMrnCount == 0) {
			String activeSubjectDescription = "subject with ID " + activeSubject.getId();
			String mergeSubjectDescription = "subject with ID " + mergeSubject.getId();
			changeDetailsList.add(mergeSubjectDescription + " merged into " + activeSubjectDescription);
			mergeSubject.setArchivalStatus(ArchivalStatus.MERGED);
			subjectDAO.updateEntity(mergeSubject);
		}

		String changeDetails = String.join(",", changeDetailsList);

		String actionPerformed = "Changed subject of subject mrn because puid changed";

		final ActivityLog al = new ActivityLog();
        al.setPerformingUser(user);
        al.setActionPerformed(actionPerformed);
		al.setDate(new Date());
		al.setIpAddress(ipAddress);
		al.setAffectedUser(null);
		al.setAffectedResource(null);
		al.setAffectedSublocation(null);
		al.setAffectedSubject(mergeSubject);
		al.setAffectedStudy(null);
		al.setAffectedVisit(null);
		al.setChangesDetailRequiredField(null);
		al.setChangesDetail(changeDetails);
		al.setAppointmentOverrideReason(null);
		al.setBookedVisit(null);
		subjectDAO.createEntity(al);

		return activeSubject;
	}

	private SubjectMrn subjectExistsWithSameMrnAndDiffPuid(SubjectsDTO subjectDTO) {
		SubjectMrn subjectMrnDiffPuid =null;
		SubjectMrn subjectMrn = subjectDAO.getSubjectMrnByMrnAndSite(subjectDTO.getMrnInfo().getValue(), subjectDTO.getMrnInfo().getInstitution());
		if(subjectMrn != null){
			String decryptedPuid = SubjectDataEncryptor.decrypt(subjectMrn.getSubject().getPuid());

			if(subjectDTO.getPartnersUid() != null && !subjectDTO.getPartnersUid().equals(decryptedPuid)){
				subjectMrnDiffPuid = subjectMrn;
			}
		}

		return subjectMrnDiffPuid;
	}

	/** @return true if this subject is already in this study, false otherwise */
	boolean isSubjectAlreadyInThisStudy(final User user, final Subject subject, final Study study) {
		final List<StudySubject> studySubjectList = getStudySubjects(user, subject, study);

		if (nullOrEmpty(studySubjectList)) {
			return false;
		}
		return enrich(studySubjectList).exists(s -> s.getStudy().getId().equals(study.getId()));
	}

	private void updateSubjectFieldsIfNeeded(SubjectsDTO subjectDTO, User user, String ipAddress, Subject subject) {
		//need to do this before the data is encrypted
		final List<Optional<String>> previousDataComponents = gatherPreviousData(subjectDTO, subject);

		final List<Optional<String>> otherPreviousDataComponents = gatherOtherPreviousData(subjectDTO, subject);

		final Joiner joiner = Joiner.on("");

		final String previousData = joiner.join(flatten(previousDataComponents));
		final String otherPreviousData = joiner.join(flatten(otherPreviousDataComponents));

		subject.setId(subjectDTO.getId());

		setSomeEntityValuesViaDto(subject, subjectDTO);

		subjectDAO.encryptAndSave(subject);

		auditService.logSubjectActivity(ipAddress, subject, user, Statics.AUDIT_SUBJECT_UPDATE, previousData, otherPreviousData);
	}

	@SuppressWarnings("unchecked")
	private List<Optional<String>> gatherOtherPreviousData(SubjectsDTO subjectDTO, Subject subject) {
		int stateId = 0;
		if(subject.getState() != null){
			stateId = subject.getState().getId();
		}
		return Lists.newArrayList(
				makeFieldString("State", subjectDTO.getState(), stateId),
				makeFieldString("Middle Name", subjectDTO.getMiddleName(), subject.getMiddleName(), CRYPT, CRYPT_UPPER),
				makeFieldString("Secondary Phone", subjectDTO.getSecondaryContactNumber(), subject.getSecondaryContactNumber(), CRYPT, CRYPT_UPPER));
	}

	@SuppressWarnings("unchecked")
	private List<Optional<String>> gatherPreviousData(SubjectsDTO subjectDTO, Subject subject) {
		final String ethnicity = lookupFieldById(subjectDTO.getEthnicity(), subjectDAO::findByEthnicityId);
		final String gender = lookupFieldById(subjectDTO.getGender(), subjectDAO::findByGenderId);
		final String race = lookupFieldById(subjectDTO.getRace(), subjectDAO::findByRaceId);
		final String country = lookupFieldById(subjectDTO.getCountry(), subjectDAO::findCountryById);


		return Lists.newArrayList(
				makeFieldString("Birth Date", subjectDTO.getBirthdate(), subject.getBirthdate()),
				makeFieldString("City", subjectDTO.getCity(), subject.getCity(), CRYPT_UPPER, CRYPT_UPPER),

				makeFieldString("Ethnicity", ethnicity.toUpperCase(), subject.getEthnicity(), subjectDTO.getEthnicity(), Ethnicity::getId, CRYPT, CRYPT_UPPER),
				makeFieldString("Gender", gender, subject.getGender(), subjectDTO.getGender(), Gender::getId, CRYPT, CRYPT_UPPER),

				makeFieldString("First Name", subjectDTO.getFirstName(), subject.getFirstName(), CRYPT, CRYPT_UPPER),
				makeFieldString("Last Name", subjectDTO.getLastName(), subject.getLastName(), CRYPT, CRYPT_UPPER),

				makeFieldString("Primary Phone", subjectDTO.getPrimaryContactNumber(), subject.getPrimaryContactNumber(), CRYPT, CRYPT_UPPER),

				makeFieldString("Race", race, subject.getRace(), subjectDTO.getRace(), Race::getId, CRYPT, CRYPT_UPPER),

				makeFieldString("Street Address 1", subjectDTO.getStreetAddress1(), subject.getStreetAddress1(), CRYPT, CRYPT_UPPER),
				makeFieldString("Street Address 2", subjectDTO.getStreetAddress2(), subject.getStreetAddress2(), CRYPT, CRYPT_UPPER),

				makeFieldString("Zip", subjectDTO.getZip(), subject.getZip(), CRYPT, CRYPT_UPPER),
				makeFieldString("Country", country, subject.getCountry(), subjectDTO.getCountry(), Country::getId, CRYPT, CRYPT_UPPER),

				makeFieldString("Active", subjectDTO.getActive(), subject.getActive()),

				makeFieldString("Puid", subjectDTO.getPartnersUid(), subject.getPuid(), CRYPT, CRYPT_UPPER));
	}

	/*
	 * Checks the database for an mrn without leading zeros
	 */
	SubjectMrn findSimilarMrn(SubjectsDTO subjectDTO) {
		SubjectMrn subjectMrn = new SubjectMrn();
		if(subjectDTO.getMrnInfo() != null)
		{
			String mrn = subjectDTO.getMrnInfo().getValue();
			String mrnMinusLeadingZeros = mrn.replaceAll("^0*", "");
			subjectMrn = subjectDAO.getSubjectMrnByMrnAndSite(mrnMinusLeadingZeros, subjectDTO.getMrnInfo().getInstitution());
		}

		return subjectMrn;
	}

	boolean mrnAlreadyExists(SubjectsDTO subjectDTO) {
		return subjectDTO.getMrnInfo() != null && subjectDAO.mrnInfoExists(subjectDTO.getMrnInfo());
	}

	SubjectMrn createSubjectMrnIfNeeded(boolean allowOnlyOneMrn,
										Subject subject,
										SubjectMrn newSubjectMrn) throws NoMrnFoundException {
		SubjectMrn subjectMrnResult = null;
		if (allowOnlyOneMrn) {
			Optional<SubjectMrn> result = subject.getSubjectMrnSet().stream().findFirst();

			if (result.isPresent()) {
				subjectMrnResult = result.get();

				//lookup the existing mrn for this subject (to get the id set properly)
				subjectMrnResult = subjectDAO.getSubjectMrnForSubject(subjectMrnResult);

				// and set the old mrn value to the new mrn value
				subjectMrnResult.setMrn(newSubjectMrn.getMrn());

			} else {
				//shouldn't get here normally; as this is only called on an update, and the subject is req'd to have an mrn
				throw new NoMrnFoundException(MRN_NOT_FOUND_FOR_SUBJECT);
			}
		} else {
			subjectMrnResult = newSubjectMrn;
		}

		subjectDAO.saveSubjectMrn(subjectMrnResult);

		return subjectMrnResult;
	}

	private void addSubjectToStudy(final User user, final String ipAddress, final SubjectMrn subjectMrn, final Study study) {
		final StudySubject studySubject = new StudySubject(study, subjectMrn, Boolean.TRUE);

		try {
			studyDAO.createEntity(studySubject);
		} catch (final ConstraintViolationException ex) {
			// Ignore as this prevents persistence of duplicate entries
			// not all databases have the uniqueness constraints (some sites have different
			// structures; this accounts for that)
			logDontThrow(UPDATE_SUBJECT_CONSTRAINT_VIOLATION, ex);
		}

		studyService.stampStudyAndLogStudySubjectMrnActivity(ipAddress, study, subjectMrn, user, Statics.AUDIT_STUDY_SUBJECT_CREATE, null, null);
	}

	void logDontThrow(String message, Exception cause) {
		SchedulerRuntimeException.logDontThrow(message, cause);
	}

	private List<StudySubject> getStudySubjects(final User user, final Subject subject, final Study study) {
		// note: since this method's caller filters on study id, the second path
		//         would be redundant. may be better to use second path only and
		//         filter in the caller?
		if (user.isStudyStaff()) {
			return appointmentDAO.findStudySubjectBySubject(subject);
		} else {
			return appointmentDAO.findStudySubjectBySubjectAndStudy(subject, study);
		}
	}

	// TODO: better name
	// used only in this class
	void setSomeEntityValuesViaDto(final Subject subject,
								   final SubjectsDTO subjectDTO) {
		subject.setBirthdate(subjectDTO.getBirthdate());
		subject.setCity(subjectDTO.getCity());
		subject.setEthnicity(subjectDAO.findByEthnicityId(subjectDTO.getEthnicity()));
		subject.setFirstName(subjectDTO.getFirstName());
		subject.setLastName(subjectDTO.getLastName());
		Gender genderEntity = subjectDAO.findByGenderId(subjectDTO.getGender());
		subject.setGender(genderEntity);
		subject.setGenderEmpi(subjectDTO.getGenderEmpi());
		String genderCode = genderEntity.getCode();
		if (genderCode == null) {
			SchedulerRuntimeException.logAndThrow("Cannot set subject.genderType because of null gender code on gender entity");
		} else {
			subject.setGenderType(GenderType.valueOf(genderCode));
		}
		subject.setDerivedFullName();

		subject.setMiddleName(subjectDTO.getMiddleName());
		subject.setPrimaryContactNumber(subjectDTO.getPrimaryContactNumber());
		subject.setRace(subjectDAO.findByRaceId(subjectDTO.getRace()));
		subject.setSecure(Boolean.FALSE);
		subject.setSecondaryContactNumber(subjectDTO.getSecondaryContactNumber());
		subject.setStreetAddress1(subjectDTO.getStreetAddress1());
		subject.setStreetAddress2(subjectDTO.getStreetAddress2());

		subject.setState((subjectDTO.getState() != 0) ? subjectDAO.findByStateId(subjectDTO.getState()) : null);

		subject.setCountry(subjectDAO.findCountryById(subjectDTO.getCountry()));
		subject.setZip(subjectDTO.getZip());
		subject.setComment(subjectDTO.getComment());
		subject.setActive(subjectDTO.getActive());

		subject.setComment(subjectDTO.getComment());

		subject.setPuid(subjectDTO.getPartnersUid());
	}


    class NoMrnFoundException extends Exception {

		 NoMrnFoundException(String message) {
			super(message);
		}
	}

}
