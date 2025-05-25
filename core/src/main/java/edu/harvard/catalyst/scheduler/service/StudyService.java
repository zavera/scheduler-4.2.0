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

import edu.harvard.catalyst.hccrc.core.util.ListUtils;
import edu.harvard.catalyst.hccrc.core.util.RichList;
import edu.harvard.catalyst.scheduler.core.SchedulerRuntimeException;
import edu.harvard.catalyst.scheduler.core.Statics;
import edu.harvard.catalyst.scheduler.dto.*;
import edu.harvard.catalyst.scheduler.dto.request.VisitTemplatesRequest;
import edu.harvard.catalyst.scheduler.dto.response.FundingSourceInfo;
import edu.harvard.catalyst.scheduler.dto.response.GetStudiesResponse;
import edu.harvard.catalyst.scheduler.dto.response.GetStudySubjectsResponse;
import edu.harvard.catalyst.scheduler.dto.response.StudyDTO;
import edu.harvard.catalyst.scheduler.dto.response.StudyDataResponse;
import edu.harvard.catalyst.scheduler.dto.response.StudyDetailResponse;
import edu.harvard.catalyst.scheduler.dto.response.TemplateResourceWithTraListDTO;
import edu.harvard.catalyst.scheduler.dto.response.UserDataResponse;
import edu.harvard.catalyst.scheduler.dto.response.VisitApprovalModelResponseDTO;
import edu.harvard.catalyst.scheduler.dto.response.VisitTemplateDetailResponse;
import edu.harvard.catalyst.scheduler.dto.response.VisitTemplatesResponse;
import edu.harvard.catalyst.scheduler.entity.*;
import edu.harvard.catalyst.scheduler.persistence.AuthDAO;
import edu.harvard.catalyst.scheduler.persistence.StudyDAO;
import edu.harvard.catalyst.scheduler.persistence.TemplateResourceDAO;
import edu.harvard.catalyst.scheduler.util.*;
import org.antlr.stringtemplate.StringTemplate;
import org.antlr.stringtemplate.StringTemplateGroup;
import org.antlr.stringtemplate.language.DefaultTemplateLexer;
import edu.harvard.catalyst.scheduler.dto.request.BooleanRequest;
import edu.harvard.catalyst.scheduler.persistence.SubjectDAO;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static edu.harvard.catalyst.hccrc.core.util.ListUtils.enrich;
import static edu.harvard.catalyst.hccrc.core.util.ListUtils.flatten;
import static edu.harvard.catalyst.hccrc.core.util.Range.from;
import static edu.harvard.catalyst.scheduler.util.SubjectDataEncryptor.decryptSubjectWithinStudySubject;
import static edu.harvard.catalyst.scheduler.core.Statics.NA;

@Component
public class StudyService {

	private final MailHandler mailHandler;
	private final StudyDAO studyDAO;
	private final AuthDAO authDAO;
	private final AuditService auditService;
	private final SubjectDAO subjectDAO;
	private final TemplateResourceDAO templateResourceDAO;

	private static final String NULL = " NULL ";
	private static final String SPACE = " ";
	private static final String TO = " to ";
	private static final String COMMA = ", ";

	@Autowired
	public StudyService(
			final AuditService auditService,
			final StudyDAO studyDAO,
			final AuthDAO authDAO,
			final MailHandler mailHandler,
			final SubjectDAO subjectDAO,
			final TemplateResourceDAO templateResourceDAO) {
		
		this.auditService = auditService;
		this.studyDAO = studyDAO;
		this.authDAO = authDAO;
		this.mailHandler = mailHandler;
		this.subjectDAO = subjectDAO;
		this.templateResourceDAO = templateResourceDAO;
	}

	//Don't use - Needed for spring security cglib proxying
	StudyService() {
		this(null, null, null, null, null, null);
	}

	//@Transactional // read-only, not counting setting a transient field (investigatorsName)
	public GetStudiesResponse getSubjectStudies(final int userId) {
		final User user = authDAO.findUserById(userId);
		
		if (user.isStudyStaff()) {
			return studyDAO.findSubjectStudyListByPerson(user);
		} else {
			return studyDAO.getNonClosedStudies();
		}
	}

	//@Transactional // read-only
	public GetStudiesResponse getOpenStudies(final String filterString,
			final String sortBy,
			final String orderBy,
			final int page,
			final int maxResults,
			final User user) {

		if (user.isStudyStaff()) {
			return studyDAO.findOpenStudyListByPerson(
					user,
					filterString,
					sortBy,
					orderBy,
					page,
					maxResults);
		} else {
			return studyDAO.getOpenStudies(
					filterString,
					sortBy,
					orderBy,
					page,
					maxResults);
		}
	}

	//@Transactional
	public StudyDTO createStudy(final StudyDTO dto, final User user, final String ipAddress) {
		
		if (!studyDAO.checkLocalId(dto.getLocalId())) {
			dto.setResult(false);
			dto.setErrorMsg("Local Id already exists");
			return dto;
		}

		final Study study = new Study();
		final List<StudyFundingSource> dependentSFSs = setStudyData(dto, study);

		if (!study.getStudyStatus().getIsPending()) {
			setSomeOtherUsers(dto, study);
		}

		studyDAO.createEntity(study);

		for (final StudyFundingSource sfs : dependentSFSs) {
			sfs.setStudyId(study.getId());
			studyDAO.createEntity(sfs);
		}

		stampStudyAndLogStudyActivity(ipAddress, study, user, Statics.AUDIT_STUDY_CREATE, null, null);
		
		dto.setId(study.getId());
		dto.setResult(true);
		
		return dto;
	}

	// used only in this class
	void stampStudyAndLogStudyActivity(final String ipAddress,
									   final Study study,
									   final User user,
									   final String action,
									   final String requiredFieldPreviousData,
									   final String previousData) {
		
		study.setStatusChange(new Date());
		studyDAO.updateEntity(study);
		auditService.logStudyActivity(ipAddress, study, user, action, requiredFieldPreviousData, previousData);
	}

	private void wipePreviousSFSs(final Study study) {
		
		final Set<StudyFundingSource> studyFundingSources = study.getStudyFundingSources();

		studyFundingSources.forEach(studyDAO::deleteEntity);
	}

	//@Transactional
	public StudyDTO updateStudy(final StudyDTO dto, final User user, final String ipAddress) {
		
		final Study study = studyDAO.findStudyById(dto.getId());

		if (isUniqueLocalId(dto, study)) {
			return dto;
		}

		logStudyDataChange(dto, user, ipAddress, study);

		wipePreviousSFSs(study);

		final List<StudyFundingSource> dependentSFSs = setStudyData(dto, study);
		
		setSomeOtherUsers(dto, study);

		studyDAO.updateEntity(study);

		for (final StudyFundingSource sfs : dependentSFSs) {
			sfs.setStudyId(study.getId());
			studyDAO.createEntity(sfs);
		}

		dto.setId(study.getId());
		dto.setResult(true);
		
		return dto;
	}

	private List<StudyFundingSource> setStudyData(final StudyDTO dto, final Study s) {
		
		final List<StudyFundingSource> result = new ArrayList<>();

		s.setName(dto.getName());
		s.setStudyStatus(studyDAO.findStudyStatusById(dto.getStudyStatus()));
		s.setInstitution(studyDAO.findByInstitutionId(dto.getInstitution()));
		
		final boolean isIndustryInitiated = dto.getIndustryInitiated().equalsIgnoreCase("yes");
		
		s.setIndustryInitiated(isIndustryInitiated);
		
		s.setLocalId(dto.getLocalId());
		s.setPediatric(dto.getPediatric());
		s.setTotalInpatientVisits(dto.getTotalInpatientVisits());
		s.setExpectedStartDate(dto.getExpectedStartDate());
		s.setExpectedEndDate(dto.getExpectedEndDate());
		s.setTotalOutpatientVisits(dto.getTotalOutpatientVisits());
		s.setTotalSubjects(dto.getTotalSubjects());
		s.setShortTitle(dto.getShortTitle());
		s.setIrbApprovalDate(dto.getIrbApprovalDate());
		s.setIrbExpiration(dto.getIrbExpiration());
		s.setIrbRenewalDate(dto.getIrbRenewalDate());
		s.setCrcCategory(dto.getCrcCategory());
		s.setCatalystId(dto.getCatalystId());
		s.setIrb(dto.getIrb());
		s.setSpid(dto.getSpid());
		s.setIrbInstitution(studyDAO.findIRBInstitutionById(dto.getIrbInstitution()));

		for (final FundingSourceInfo fundingInfo : dto.getFundingSourceInfoList()) {
			final FundingSource fundingSource = studyDAO.findFundingSourceById(fundingInfo.getId());
			
			final CentersAndInstitutions centersAndInstitutions;
			
			if (fundingSource.getName().equalsIgnoreCase("Federal PHS")) {
				centersAndInstitutions = studyDAO.findCentersAndInstitutionsById(fundingInfo.getCenterAndInstitutionId());
			} else {
				centersAndInstitutions = null;
			}
			
			final StudyFundingSource sfs = new StudyFundingSource(
					s,
					fundingSource,
					centersAndInstitutions,
					fundingInfo.getOneToFour(),
					fundingInfo.getComment(),
					fundingInfo.getGrant(),
					fundingInfo.getCenter(),
					fundingInfo.getDirectAward(),
					fundingInfo.getIndirectAward(),
					fundingInfo.getStart(),
					fundingInfo.getEnd());
			
			result.add(sfs);
		}

		s.setCrcFunded(dto.getCrcFunded());
		s.setStudyAbstract(dto.getStudyAbstract());

		s.setClinicalTrial(dto.getClinicalTrial());
		s.setNewDrug(dto.getNewDrug());
		s.setDeviceExemption(dto.getDeviceExemption());

		return result;
	}

	String comparePreviousAndCurrentFundingSource(final StudyFundingSource backEndStudyFundingSource,
			final FundingSourceInfo frontEndFundingSourceInfo,
			final int fsNumber) {

		final StringBuilder diffBuilder = new StringBuilder();

		String fsNameBack = NULL, fsNameFront = NULL;
		String grantIdBack = NULL, grantIdFront = NULL;
		String siteCostCenterBack = NULL, siteCostCenterFront = NULL;
		String totalDirectAwardBack = NULL, totalDirectAwardFront = NULL;
		String totalIndirectAwardBack = NULL, totalIndirectAwardFront = NULL;
		String projectStartDateBack = NULL, projectStartDateFront = NULL;
		String projectEndDateBack = NULL, projectEndDateFront = NULL;
		String caiNameBack = NULL, caiNameFront = NULL;

		if (backEndStudyFundingSource != null) {
			fsNameBack = backEndStudyFundingSource.getFundingSource().getName();
			grantIdBack = backEndStudyFundingSource.getGrantId();
			siteCostCenterBack = backEndStudyFundingSource.getSiteCostCenter();
			totalDirectAwardBack = String.valueOf(backEndStudyFundingSource.getTotalDirectAward());
			totalIndirectAwardBack = String.valueOf(backEndStudyFundingSource.getTotalIndirectAward());
			projectStartDateBack = String.valueOf(backEndStudyFundingSource.getProjectStartDate());
			projectEndDateBack = String.valueOf(backEndStudyFundingSource.getProjectEndDate());

			final CentersAndInstitutions cai = backEndStudyFundingSource.getCentersAndInstitutions();
			
			if (cai != null) {
				caiNameBack = cai.getName();
			}
		}

		if (frontEndFundingSourceInfo != null) {
			fsNameFront = frontEndFundingSourceInfo.getName();
			grantIdFront = frontEndFundingSourceInfo.getGrant();
			siteCostCenterFront = frontEndFundingSourceInfo.getCenter();
			totalDirectAwardFront = String.valueOf(frontEndFundingSourceInfo.getDirectAward());
			totalIndirectAwardFront = String.valueOf(frontEndFundingSourceInfo.getIndirectAward());
			projectStartDateFront = String.valueOf(frontEndFundingSourceInfo.getStart());
			projectEndDateFront = String.valueOf(frontEndFundingSourceInfo.getEnd());

			caiNameFront = frontEndFundingSourceInfo.getCenterAndInstitutionName();
		}

		diffBuilder.append(" Funding Source : ").append(fsNumber).append(SPACE);
		diffBuilder.append(fsNameBack).append(TO).append(fsNameFront).append(COMMA);

		diffBuilder.append(" Funding Source GrantId: ").append(fsNumber).append(SPACE);
		diffBuilder.append(grantIdBack).append(TO).append(grantIdFront).append(COMMA);

		diffBuilder.append(" Funding Source Site Cost Center: ").append(fsNumber).append(SPACE);
		diffBuilder.append(siteCostCenterBack).append(TO).append(siteCostCenterFront).append(COMMA);

		diffBuilder.append(" Funding Source Direct Award: ").append(fsNumber).append(SPACE);
		diffBuilder.append(totalDirectAwardBack).append(TO).append(totalDirectAwardFront).append(COMMA);

		diffBuilder.append(" Funding Source Indirect Award: ").append(fsNumber).append(SPACE);
		diffBuilder.append(totalIndirectAwardBack).append(TO).append(totalIndirectAwardFront).append(COMMA);

		diffBuilder.append(" Funding Source Project Start Date: ").append(fsNumber).append(SPACE);
		diffBuilder.append(projectStartDateBack).append(TO).append(projectStartDateFront).append(COMMA);

		diffBuilder.append(" Funding Source Project End Date: ").append(fsNumber).append(SPACE);
		diffBuilder.append(projectEndDateBack).append(TO).append(projectEndDateFront).append(COMMA);

		diffBuilder.append(" Funding Source Centers And Institutes: ").append(fsNumber).append(SPACE);
		diffBuilder.append(caiNameBack).append(TO).append(caiNameFront).append(COMMA);

		return diffBuilder.toString();
	}

	void addToBuilderIfStringsDiff(final String label, final StringBuilder diffBuilder, final String front, final String back) {
		if (!front.equals(back)) {
			diffBuilder.append(label).append(back).append(TO).append(front).append(COMMA);
		}
	}

	void addNameToBuilderIfStringsDiffEntityNonNull(final String label,
			final HasId entity,
			final StringBuilder diffBuilder,
			final Integer frontId,
			final String frontValue) {

		addNameToBuilderIfStringsDiffEntityNonNullHelper(label, entity, diffBuilder, frontId, frontValue, false);
	}

	void addFirstNameToBuilderIfStringsDiffEntityNonNull(final String label,
			final BaseEntity entity,
			final StringBuilder diffBuilder,
			final Integer frontId,
			final String frontValue) {

		addNameToBuilderIfStringsDiffEntityNonNullHelper(label, entity, diffBuilder, frontId, frontValue, true);
	}

	private void addNameToBuilderIfStringsDiffEntityNonNullHelper(final String label,
			final HasId entity,
			final StringBuilder diffBuilder,
			final Integer frontId,
			final String frontValue,
			final boolean first) {
		
	    if (entity == null) {
			diffBuilder.append(label).append(NULL).append(COMMA);
		} else {
			final Integer backId = entity.getId();
			
			if(!frontId.equals(backId)) {

				final String tableName = entity.getClass().getSimpleName();
				final String backValue = studyDAO.findNameById(tableName, backId, first);

				diffBuilder.append(label).append(backValue).append(TO).append(frontValue).append(COMMA);
			}
		}
	}

	void addToBuilderIfObjectsDiffNonNullBack(final String label, final StringBuilder diffBuilder, final Object front, final Object back) {
		if( back != null && ! back.equals(front)) {
			diffBuilder.append(label).append(back).append(TO).append(front).append(COMMA);
		}
	}

	String findNameByNonZeroId(final Class<?> entityClass, final int id) {
		return findNameByNonZeroId(entityClass, id, false);
	}

	String findFirstNameByNonZeroId(final Class<?> entityClass, final int id) {
		return findNameByNonZeroId(entityClass, id, true);
	}

	String findFirstNameByNonZeroNonMinusOneId(final Class<?> entityClass, final int id) {
		return (id == -1) ? NA : findFirstNameByNonZeroId(entityClass, id);
	}

	private String findNameByNonZeroId(final Class<?> entityClass, final int id, final boolean first) {

		final String tableName = entityClass.getSimpleName();

		return id != 0 ? studyDAO.findNameById(tableName, id, first) : null;

	}
	
	private static <A> Map<Integer, A> makeFundingSourceMap(final Collection<A> sources, final Function<A, Integer> getId) {
		return sources.stream().collect(Collectors.toMap(getId, v -> v));
	}
	
	private void logStudyDataChange(final StudyDTO dto, final User user, final String ipAddress, final Study s) {
		final StringBuilder diffBuilder = new StringBuilder();

		final Map<Integer, StudyFundingSource> backEndFundingSourceMap = makeFundingSourceMap(s.getStudyFundingSources(), StudyFundingSource::getOneToFour);

		final Map<Integer, FundingSourceInfo> frontEndFundingInfoMap = makeFundingSourceMap(dto.getFundingSourceInfoList(), FundingSourceInfo::getOneToFour);

		from(1).to(4).inclusive().forEach(i -> {
			final StudyFundingSource backEndStudyFundingSource = backEndFundingSourceMap.get(i);
			final FundingSourceInfo frontEndFundingSourceInfo = frontEndFundingInfoMap.get(i);

			diffBuilder.append(comparePreviousAndCurrentFundingSource(backEndStudyFundingSource, frontEndFundingSourceInfo, i));
		});

		buildStudyChangeString(dto, s, diffBuilder);

		stampStudyAndLogStudyActivity(ipAddress, s, user,
				Statics.AUDIT_STUDY_UPDATE,
				diffBuilder.toString(), "");
	}

	private void buildStudyChangeString(final StudyDTO dto, final Study s, final StringBuilder diffBuilder) {
		final String studyStatusFrontValue    = findNameByNonZeroId(StudyStatus.class, dto.getStudyStatus());
		final String institutionFrontValue    = findNameByNonZeroId(Institution.class, dto.getInstitution());
		final String irbInstitutionFrontValue = findNameByNonZeroId(IRBInstitution.class, dto.getIrbInstitution());

		final String investigatorFrontValue   = findFirstNameByNonZeroId(User.class, dto.getInvestigator());

		final String nurseFrontValue          = findFirstNameByNonZeroNonMinusOneId(User.class, dto.getProtocolNurse());
		final String nutritionistFrontValue   = findFirstNameByNonZeroNonMinusOneId(User.class, dto.getProtocolNutritionist());

		final String schedulerFrontValue                     = findFirstNameByNonZeroId(User.class, dto.getScheduler());
		final String physicianFrontValue                     = findFirstNameByNonZeroId(User.class, dto.getPhysician());
		final String physician2FrontValue                    = findFirstNameByNonZeroId(User.class, dto.getPhysician2());
		final String associateNurseFrontValue                = findFirstNameByNonZeroId(User.class, dto.getAssociateNurse());
		final String associateProtocolNutritionistFrontValue = findFirstNameByNonZeroId(User.class, dto.getAssociateProtocolNutritionist());
		final String secondarySchedulerFrontValue            = findFirstNameByNonZeroId(User.class, dto.getSecondaryScheduler());
		final String scheduler3FrontValue                    = findFirstNameByNonZeroId(User.class, dto.getScheduler3());

		addToBuilderIfStringsDiff(" Catalyst ID: ", diffBuilder, dto.getCatalystId(), s.getCatalystId());
		addToBuilderIfStringsDiff(" Local ID: ", diffBuilder, dto.getLocalId(), s.getLocalId());
		addToBuilderIfStringsDiff(" Study Name: ", diffBuilder, dto.getName(), s.getName());

		addNameToBuilderIfStringsDiffEntityNonNull(" Study Status ", s.getStudyStatus(), diffBuilder, dto.getStudyStatus(), studyStatusFrontValue);
		addNameToBuilderIfStringsDiffEntityNonNull(" Institution: ", s.getInstitution(), diffBuilder, dto.getInstitution(), institutionFrontValue);
		addNameToBuilderIfStringsDiffEntityNonNull(" IRB Institution: ", s.getIrbInstitution(), diffBuilder, dto.getIrbInstitution(), irbInstitutionFrontValue);

		addToBuilderIfStringsDiff(" CRC Cat: ", diffBuilder, dto.getCrcCategory(), s.getCrcCategory());
		addToBuilderIfStringsDiff(" Pediatric: ", diffBuilder, dto.getPediatric(), s.getPediatric());
		addToBuilderIfStringsDiff(" IRB: ", diffBuilder, dto.getIrb(), s.getIrb());

		addToBuilderIfObjectsDiffNonNullBack(" IRB Approval: ", diffBuilder, dto.getIrbApprovalDate(), s.getIrbApprovalDate());
		addToBuilderIfObjectsDiffNonNullBack(" IRB Expiration: ", diffBuilder, dto.getIrbExpiration(), s.getIrbExpiration());

		addFirstNameToBuilderIfStringsDiffEntityNonNull(" Protocol Nurse: ", s.getProtocolNurse(), diffBuilder, dto.getProtocolNurse(), nurseFrontValue);
		addFirstNameToBuilderIfStringsDiffEntityNonNull(" Protocol Nutritionist: ", s.getProtocolNutritionist(), diffBuilder, dto.getProtocolNutritionist(), nutritionistFrontValue);
		addFirstNameToBuilderIfStringsDiffEntityNonNull(" Scheduler: ", s.getScheduler(), diffBuilder, dto.getScheduler(), schedulerFrontValue);
		addFirstNameToBuilderIfStringsDiffEntityNonNull(" Investigator: ", s.getInvestigator(), diffBuilder, dto.getInvestigator(), investigatorFrontValue);
		addFirstNameToBuilderIfStringsDiffEntityNonNull(" Scheduler 3: ", s.getScheduler3(), diffBuilder, dto.getScheduler3(), scheduler3FrontValue);
		addFirstNameToBuilderIfStringsDiffEntityNonNull(" Secondary Scheduler: ", s.getSecondaryScheduler(), diffBuilder, dto.getSecondaryScheduler(), secondarySchedulerFrontValue);
		addFirstNameToBuilderIfStringsDiffEntityNonNull(" Physician: ", s.getPhysician(), diffBuilder, dto.getPhysician(), physicianFrontValue);
		addFirstNameToBuilderIfStringsDiffEntityNonNull(" Physician 2: ", s.getPhysician2(), diffBuilder, dto.getPhysician2(), physician2FrontValue);
		addFirstNameToBuilderIfStringsDiffEntityNonNull(" Associate Nurse: ", s.getAssociateNurse(), diffBuilder, dto.getAssociateNurse(), associateNurseFrontValue);
		addFirstNameToBuilderIfStringsDiffEntityNonNull(" Associate Protocol Nutritionist: ", s.getAssociateProtocolNutritionist(), diffBuilder, dto.getAssociateProtocolNutritionist(), associateProtocolNutritionistFrontValue);
	}

	//NB: WhyTF is this side-effecting??
	private boolean isUniqueLocalId(final StudyDTO dto, final Study s) {
		if(!s.getLocalId().equals(dto.getLocalId()) && !studyDAO.checkLocalId(dto.getLocalId())) {
			dto.setErrorMsg("Local Id already exists");
			dto.setResult(false);
			return true;
		}
		
		return false;
	}

	//@Transactional
	public VisitTemplate createVisit(final VisitDTO visit, final User user, final String ipAddress) {

		final Study study = studyDAO.findStudyById(visit.getStudyId());

		final VisitTemplate visitTemplate = new VisitTemplate.VisitTemplateBuilder().
				name(visit.getName()).
				shortName(visit.getShortName()).
				approved(Boolean.FALSE).
				relativeTime(visit.isRelativeTime()).
				nursing(visit.getNursing()).
				nutrition(visit.getNutrition()).
				processing(visit.getProcessing()).
				setup(visit.getSetup()).
				sublocation(studyDAO.findSubLocationById(visit.getSublocation())).
				createdDate(new Date()).
				visitType(studyDAO.findVisitTypeById(visit.getVisitType())).
				study(study).institutionNonCRC(visit.isInstitutionNonCRC()).
				nonInstitutionNonCRC(visit.isNonInstitutionNonCRC()).
				researchPharmacy(visit.isResearchPharmacy()).
				active(Boolean.TRUE).
				lastUpdateTime(new Date()).
				duration(null).
				billable(visit.getBillable()).
				create();

		studyDAO.createEntity(visitTemplate);
		
		auditService.logVisitActivity(ipAddress, visitTemplate, user, Statics.AUDIT_VISIT_CREATE, null, null);
		
		if (!visit.getComment().isEmpty()) {
			createVisitComments(visit.getComment(), user, ipAddress, visitTemplate);
		}
		return visitTemplate;
	}

	void createVisitComments(final String comment, final User user, final String ipAddress, final VisitTemplate v) {
		final Comments comments = new Comments();
		comments.setComment(comment);
		comments.setVisitTemplate(v);
		comments.setUser(user);
		comments.setDate(new Date());
		studyDAO.createEntity(comments);
		auditService.logVisitActivity(ipAddress, v, user, Statics.AUDIT_VISIT_COMMENT_CREATE, null, null);
	}

	@Transactional
	public VisitTemplate copyVisit(final VisitDTO visitDTO, final User user, final String ipAddress) {

		final VisitTemplate oldVisit = studyDAO.findVisitById(visitDTO.getId());

		Sublocation sublocation = oldVisit.getSublocation();
		if(visitDTO.getSublocation() != null && visitDTO.getSublocation() != 0)
		{
			sublocation = studyDAO.findSubLocationById(visitDTO.getSublocation());
		}

		VisitType visitType = oldVisit.getVisitType();
		if(visitDTO.getVisitType() != null && visitDTO.getVisitType() != 0)
		{
			visitType = studyDAO.findVisitTypeById(visitDTO.getVisitType());
		}

		final VisitTemplate newVisit = new VisitTemplate.VisitTemplateBuilder().
				name(visitDTO.getName()).
				shortName(visitDTO.getShortName()).
				approved(Boolean.FALSE).
				relativeTime(oldVisit.getRelativeTime()).
				nursing(visitDTO.getNursing()).
				nutrition(visitDTO.getNutrition()).
				processing(visitDTO.getProcessing()).
				setup(visitDTO.getSetup()).
				sublocation(sublocation).
				createdDate(new Date()).
				visitType(visitType).
				study(oldVisit.getStudy()).
				institutionNonCRC(oldVisit.getInstitutionNonCRC()).
				nonInstitutionNonCRC(oldVisit.getNonInstitutionNonCRC()).
				researchPharmacy(oldVisit.getResearchPharmacy()).
				active(oldVisit.getActive()).
				lastUpdateTime(new Date()).
				duration(oldVisit.getDuration()).
				billable(oldVisit.getBillable()).
				create();

		studyDAO.createEntity(newVisit);

		addVisitCommentIfPresent(newVisit, visitDTO.getComment(), user, ipAddress);
		
		final List<TemplateResourceWithTraListDTO> templateResourceWithTraListDTOList =
				templateResourceDAO.findTemplateResourcesAndAnnotationsByVisit(oldVisit.getId(), "");

		templateResourceWithTraListDTOList.forEach(trwalDto -> {
            final TemplateResource newTemplateResource = createTemplateResource(newVisit, trwalDto.getTemplateResource());

            studyDAO.createEntity(newTemplateResource);

            final List<TemplateResourceAnnotations> traList = trwalDto.getTraList();

            traList.stream().map(tra -> {
                final TemplateResourceAnnotations newTra = new TemplateResourceAnnotations();
                newTra.setTemplateResource(newTemplateResource);
                newTra.setLineLevelAnnotations(tra.getLineLevelAnnotations());
                newTra.setComment(tra.getComment());
                newTra.setQuantity(tra.getQuantity());
                return newTra;
            }).forEach(studyDAO::createEntity);

            if (newTemplateResource.getGroupId() != null) {
                final TemplateResourceGroup templateResourceGroup = new TemplateResourceGroup();
                templateResourceGroup.setGroupId(newTemplateResource.getGroupId());
                templateResourceGroup.setTemplateResource(newTemplateResource);
                templateResourceGroup.setVisit(newVisit);
                templateResourceGroup.setFlexGroup(newTemplateResource.getFlexible());
                studyDAO.createEntity(templateResourceGroup);
            }
        });

		auditService.logVisitActivity(ipAddress, newVisit, user, Statics.AUDIT_VISIT_COPY, null, null);
		return newVisit;
	}

	private TemplateResource createTemplateResource(final VisitTemplate visitTemplate, final TemplateResource oldTemplateResource) {
		final TemplateResource newTemplateResource = new TemplateResource();

		Date now = new Date();
		newTemplateResource.setCreatedDate(now);
		newTemplateResource.setLastUpdateTime(now);

		newTemplateResource.setVisitTemplate(visitTemplate);

		newTemplateResource.setAlternate(oldTemplateResource.getAlternate());
		newTemplateResource.setBillable(oldTemplateResource.getBillable());
		newTemplateResource.setResource(oldTemplateResource.getResource());
		newTemplateResource.setFlexible(oldTemplateResource.getFlexible());
		newTemplateResource.setFloatable(oldTemplateResource.getFloatable());
		newTemplateResource.setFloatStart(oldTemplateResource.getFloatStart());
		newTemplateResource.setFloatEnd(oldTemplateResource.getFloatEnd());
		newTemplateResource.setStartMinutes(oldTemplateResource.getStartMinutes());
		newTemplateResource.setEndMinutes(oldTemplateResource.getEndMinutes());
		newTemplateResource.setDuration(oldTemplateResource.getDuration());
		
		if(oldTemplateResource.getGroupId() != null) {
			newTemplateResource.setGroupId(oldTemplateResource.getGroupId() + newTemplateResource.getVisitTemplate().getId().toString());
		}
		
		return newTemplateResource;
	}

	void addVisitCommentIfPresent(final VisitTemplate visitTemplate,
								  final String comment,
								  final User user,
								  final String ipAddress) {

		if (MiscUtil.isNonNullNonEmpty(comment)) {
			createVisitComments(comment, user, ipAddress, visitTemplate);
			visitTemplate.setComment(comment);
		}
	}
	//@Transactional
	public VisitTemplate updateVisit(final VisitDTO visitDTO, final User user, final String ipAddress) {
		final VisitTemplate visitTemplate = studyDAO.findVisitById(visitDTO.getId());

		final StringBuilder diffBuilder = new StringBuilder();
		final StringBuilder diffBuilder2 = new StringBuilder();

		final VisitType visitType = studyDAO.findVisitTypeById(visitDTO.getVisitType());
		final String visitTypeName = visitType.getName();

		final String sublocation = findNameByNonZeroId(Sublocation.class, visitDTO.getSublocation());

		addToBuilderIfObjectsDiffNonNullBack(" Institution Non CRC: ", diffBuilder2, visitDTO.isInstitutionNonCRC(), visitTemplate.getInstitutionNonCRC());
		addToBuilderIfObjectsDiffNonNullBack(" Non Institution Non CRC: ", diffBuilder2, visitDTO.isNonInstitutionNonCRC(), visitTemplate.getNonInstitutionNonCRC());
		addToBuilderIfObjectsDiffNonNullBack(" Relative Time: ", diffBuilder2, visitDTO.isRelativeTime(), visitTemplate.getRelativeTime());
		addToBuilderIfObjectsDiffNonNullBack(" Research Pharmacy: ", diffBuilder2, visitDTO.isResearchPharmacy(), visitTemplate.getResearchPharmacy());

		addToBuilderIfStringsDiff(" Visit Name: ", diffBuilder, visitDTO.getName(), visitTemplate.getName());

		// these had been giving back-end 'toString()', but 'getName()' makes more sense
		addToBuilderIfStringsDiff(" Visit Type: ", diffBuilder, visitTypeName, visitTemplate.getVisitType().getName());
		addNameToBuilderIfStringsDiffEntityNonNull(" Sub-location: ", visitTemplate.getSublocation(), diffBuilder, visitDTO.getSublocation(), sublocation);

		addToBuilderIfObjectsDiffNonNullBack(" Nursing: ", diffBuilder, visitDTO.getNursing(), visitTemplate.getNursing());
		addToBuilderIfObjectsDiffNonNullBack(" Nutrition: ", diffBuilder, visitDTO.getNutrition(), visitTemplate.getNutrition());
		addToBuilderIfObjectsDiffNonNullBack(" Processing: ", diffBuilder, visitDTO.getProcessing(), visitTemplate.getProcessing());
		addToBuilderIfObjectsDiffNonNullBack(" Set-Up: ", diffBuilder, visitDTO.getSetup(), visitTemplate.getSetup());

		visitTemplate.setInstitutionNonCRC(visitDTO.isInstitutionNonCRC());
		visitTemplate.setNonInstitutionNonCRC(visitDTO.isNonInstitutionNonCRC());
		visitTemplate.setRelativeTime(visitDTO.isRelativeTime());
		visitTemplate.setResearchPharmacy(visitDTO.isResearchPharmacy());
		visitTemplate.setBillable(visitDTO.getBillable());
		visitTemplate.setName(visitDTO.getName());
		visitTemplate.setShortName(visitDTO.getShortName());
		visitTemplate.setVisitType(visitType);
		visitTemplate.setSublocation(studyDAO.findSubLocationById(visitDTO.getSublocation()));

		visitTemplate.setNursing(visitDTO.getNursing());
		visitTemplate.setNutrition(visitDTO.getNutrition());
		visitTemplate.setProcessing(visitDTO.getProcessing());
		visitTemplate.setSetup(visitDTO.getSetup());

		addVisitCommentIfPresent(visitTemplate, visitDTO.getComment(), user, ipAddress);

		visitTemplate.setLastUpdateTime(new Date());

		studyDAO.updateEntity(visitTemplate);
		auditService.logVisitActivity(ipAddress, visitTemplate, user, Statics.AUDIT_VISIT_UPDATE, diffBuilder.toString(), diffBuilder2.toString());
		return visitTemplate;
	}

	// used only in this class
	private void logViewVisit(final int visitId, final User user, final String ipAddress) {
		final VisitTemplate s = studyDAO.findVisitById(visitId);
		
		auditService.logVisitActivity(ipAddress, s, user, Statics.AUDIT_VISIT_VIEW, null, null);
	}

	//@Transactional
	public GetStudySubjectsResponse getStudySubjects(
			final Optional<String> nullableFilterString,
			final Integer ofPage,
			final Integer ofMaxResults,
			final Optional<Integer> ofStudyId,
			final String ofSortBy,
			final String ofOrderBy,
			final Boolean ofWantAll,
			final Optional<User> ofUser,
			SearchDTO searchDTO) {

		final Study study = studyDAO.findStudyById(ofStudyId.get());

		final User user = ofUser.get();
		
		if (user.isStudyStaff()) {
			final boolean isLegitStudy = studyDAO.isStudyByPersonAndStudy(user, study);

			if (!isLegitStudy) {
				return null;
			}
		}

		List<SearchDTO.SearchItem> searchItems = searchDTO != null ? searchDTO.getSearchItems() : null;

		if(searchItems != null) {
			searchItems = searchItems.stream().map(si -> {

				if (si.getKey() != null && si.getKey().equals("birthdate")) {
					Date formattedBirthDate = DateUtility.parse(DateUtility.monthDayYear(), si.getValue());
					si.setValue(DateUtility.format(DateUtility.yearMonthDay(), formattedBirthDate));
				}

				if (si.getKey()!= null  && si.getKey().equals("primaryContactNumber")) {
					MiscUtil.preparePhoneNumberSearchItems(si);
				}
				return si;

			}).collect(Collectors.toList());
			searchDTO.setSearchItems(searchItems);

			Map keyMap = new HashMap();
			keyMap.put("mrn", "sm.mrn");
			keyMap.put("lastName", "su.lastName");
			keyMap.put("firstName", "su.firstName");
			keyMap.put("birthdate", "su.birthdate");
			keyMap.put("city", "su.city");
			keyMap.put("state", "su.state.name");
			keyMap.put("primaryContactNumber", "su.primaryContactNumber");

			Map valueMapperMap = new HashMap();
			valueMapperMap.put("sm.mrn", SubjectDataEncryptor.capitalizeAndEncrypt);
			valueMapperMap.put("su.lastName", SubjectDataEncryptor.capitalizeAndEncrypt);
			valueMapperMap.put("su.firstName", SubjectDataEncryptor.capitalizeAndEncrypt);
			valueMapperMap.put("su.birthdate", null);
			valueMapperMap.put("su.city", SubjectDataEncryptor.capitalizeAndEncrypt);
			valueMapperMap.put("su.state.name", null);
			valueMapperMap.put("su.primaryContactNumber", SubjectDataEncryptor.capitalizeAndEncrypt);

			searchDTO.mapSearchItemKeysAndValues(keyMap, valueMapperMap);
		}

		final List<StudySubject> resultRows = studyDAO.getRawStudySubjectsByStudy(study, ofWantAll, searchDTO);

		return getAllStudySubjectsByStudy(resultRows, nullableFilterString, ofPage, ofMaxResults, ofSortBy, ofOrderBy);
	}

    private GetStudySubjectsResponse getAllStudySubjectsByStudy(List<StudySubject> resultRows,
														final Optional<String> nullableFilterString,
														final Integer ofPage,
														final Integer ofMaxResults,
														final String ofSortBy,
														final String ofOrderBy) {
        final String filterString = nullableFilterString.orElse("");
        final boolean filterStringIsPresent = !filterString.isEmpty();

        final List<StudySubject> finalList = RichList.enrich(resultRows).map(studySubject -> {
            decryptSubjectWithinStudySubject(studySubject);


            return studySubject;
        }).filter(studySubject -> {
            final Subject decryptedSubject = studySubject.getSubject();

            SubjectMrn subjectMrn = studySubject.getSubjectMrn();
            final String decryptedMrn = subjectMrn != null ? SubjectDataEncryptor.decrypt(subjectMrn.getMrn()) : null;
            final String decrytpedLname = decryptedSubject.getLastName();
            final boolean matches = (decryptedMrn != null && decryptedMrn.contains(filterString.toUpperCase()))
                    || (decrytpedLname != null && decrytpedLname.contains(filterString.toUpperCase()));

            return filterStringIsPresent && matches;
        }).toList();

        final List<StudySubject> resultRowsToSort = filterStringIsPresent ? finalList : resultRows;

        final int total = resultRowsToSort.size();

        sortStudySubjectData(ofSortBy, ofOrderBy, resultRowsToSort);

        final List<StudySubject> filteredRows = paginateStudySubjectData(ofPage, ofMaxResults, resultRowsToSort, total);

        return GetStudySubjectsResponse.createGetStudiesSubjectsResponse(filteredRows, (long) total);
    }

    private List<StudySubject> paginateStudySubjectData(final Integer ofPage, final Integer ofMaxResults, final List<StudySubject> resultRows, final int total) {
        final int offset = (ofPage - 1) * ofMaxResults;

        final List<StudySubject> filteredRows;
        if(ofMaxResults == -1 || offset + ofMaxResults > total) {
			filteredRows = resultRows.subList(offset, total);
		}
        else if(offset + ofMaxResults <= total) {
            filteredRows = resultRows.subList(offset, offset + ofMaxResults);
        }  else {
            filteredRows = new ArrayList<>();
        }
        return filteredRows;
    }

    private void sortStudySubjectData(final String ofSortBy, final String ofOrderBy, final List<StudySubject> resultRows) {
        if (ofOrderBy.equalsIgnoreCase("ASC")) {
            sortStudySubjectAscending(ofSortBy, resultRows);
        }
        else if (ofOrderBy.equalsIgnoreCase("DESC")) {
            sortStudySubjectDescending(ofSortBy, resultRows);
        }
    }

    private void sortStudySubjectDescending(final String ofSortBy, final List<StudySubject> resultRows) {
        if (ofSortBy.equalsIgnoreCase("lastName")) {
            Collections.sort(resultRows, GetStudySubjectsResponse.StudySubjectLastNameComparatorDesc);
        }
        else if (ofSortBy.equalsIgnoreCase("mrn")) {
            Collections.sort(resultRows, GetStudySubjectsResponse.StudySubjectMRNComparatorDesc);
        }
        else if (ofSortBy.equalsIgnoreCase("firstName")) {
            Collections.sort(resultRows, GetStudySubjectsResponse.StudySubjectFirstNameComparatorDesc);
        }
        else if (ofSortBy.equalsIgnoreCase("primaryContactNumber")) {
            Collections.sort(resultRows, GetStudySubjectsResponse.StudySubjectContactComparatorDesc);
        }
        else if (ofSortBy.equalsIgnoreCase("birthdate")) {
            Collections.sort(resultRows, GetStudySubjectsResponse.StudySubjectBirthdateComparatorDesc);
        }
        else if (ofSortBy.equalsIgnoreCase("city")) {
            Collections.sort(resultRows, GetStudySubjectsResponse.StudySubjectCityComparatorDesc);
        }
        else if (ofSortBy.equalsIgnoreCase("state")) {
            Collections.sort(resultRows, GetStudySubjectsResponse.StudySubjectStateComparatorDesc);
        }
    }

    private void sortStudySubjectAscending(final String ofSortBy, final List<StudySubject> resultRows) {
        if (ofSortBy.equalsIgnoreCase("lastName")) {
            Collections.sort(resultRows, GetStudySubjectsResponse.StudySubjectLastNameComparatorAsc);
        }
        else if (ofSortBy.equalsIgnoreCase("mrn")) {
            Collections.sort(resultRows, GetStudySubjectsResponse.StudySubjectMRNComparatorAsc);
        }
        else if (ofSortBy.equalsIgnoreCase("firstName")) {
            Collections.sort(resultRows, GetStudySubjectsResponse.StudySubjectFirstNameComparatorAsc);
        }
        else if (ofSortBy.equalsIgnoreCase("primaryContactNumber")) {
            Collections.sort(resultRows, GetStudySubjectsResponse.StudySubjectContactComparatorAsc);
        }
        else if (ofSortBy.equalsIgnoreCase("birthdate")) {
            Collections.sort(resultRows, GetStudySubjectsResponse.StudySubjectBirthdateComparatorAsc);
        }
        else if (ofSortBy.equalsIgnoreCase("city")) {
            Collections.sort(resultRows, GetStudySubjectsResponse.StudySubjectCityComparatorAsc);
        }
        else if (ofSortBy.equalsIgnoreCase("state")) {
            Collections.sort(resultRows, GetStudySubjectsResponse.StudySubjectStateComparatorAsc);
        }
    }

    //@Transactional   // read-only
	public List<StudyUser> getStudyMembers(final int studyId) {
		final Study study = studyDAO.findStudyById(studyId);
		
		return studyDAO.findStudyUserRolesByStudy(study);
	}

	//@Transactional
	public BooleanRequest deleteVisit(final int visit, final User user, final String ipAddress) {
		final BooleanRequest booleanRequest = new BooleanRequest();
		final VisitTemplate visitTemplate = studyDAO.findVisitById(visit);

		final boolean bookedVisitsHaveVisit = studyDAO.ifBookedVisitsHaveVisit(visitTemplate);

		if (bookedVisitsHaveVisit) {
			booleanRequest.setResult(false);
			return booleanRequest;
		}

		final List<ActivityLog> activityLogs = studyDAO.findActivityLogByVisit(visitTemplate);
		final List<TemplateApprovalHistory> templateApprovalHistories = studyDAO.findTemplateApprovalHistoryByVisit(visitTemplate);
		final List<Comments> comments = studyDAO.findVisitTemplateCommentsByVisit(visitTemplate);
		final String visitName = visitTemplate.getName();

		activityLogs.forEach(studyDAO::deleteEntity);
		templateApprovalHistories.forEach(studyDAO::deleteEntity);
		comments.forEach(studyDAO::deleteEntity);

		final List<TemplateResource> templateResources =
				templateResourceDAO.findTemplateResourcesByVisit(visitTemplate);

		templateResources.forEach(tr -> deleteTemplateResource(visitTemplate, tr));

		studyDAO.deleteEntity(visitTemplate);
		auditService.logDeleteVisitActivity(ipAddress, visitName, user, Statics.AUDIT_VISIT_DELETE, null, null);
		booleanRequest.setResult(true);

		return booleanRequest;
	}

	// public for testability
	public void deleteTemplateResource(final VisitTemplate visitTemplate, final TemplateResource templateResource) {
		final List<TemplateResourceAnnotations> templateResourceAnnotations = templateResourceDAO.findTemplateResourceAnnotationsByTemplateResource(templateResource);
		final List<TemplateResourceGroup> templateResourceGroups = templateResourceDAO.findTemplateResourceGroupByVisitAndTemplate(visitTemplate, templateResource);
		
		if(templateResourceGroups != null) {
			templateResourceGroups.forEach(studyDAO::deleteEntity);
		}
		
		templateResourceAnnotations.forEach(studyDAO::deleteEntity);
		
		studyDAO.deleteEntity(templateResource);
	}

	//@Transactional
	public BooleanResultDTO changeVisitTemplateStatus(final VisitTemplatesRequest visits, final boolean activate, final User user, final String ipAddress) {
		final BooleanResultDTO booleanResultDTO = new BooleanResultDTO();
		
		if (!visits.getVisitTemplatesId().isEmpty()) {
			from(0).to(visits.getVisitTemplatesId().size()).forEach(i -> {
				final VisitTemplate s = studyDAO.findVisitById(visits.getVisitTemplatesId().get(i));
				final String logStatus;
				if(activate) {
					logStatus = Statics.AUDIT_VISIT_ACTIVATE;
					s.setActive(Boolean.TRUE);
				}
				else {
					logStatus = Statics.AUDIT_VISIT_DEACTIVATE;
					s.setActive(Boolean.FALSE);
				}
				s.setLastUpdateTime(new Date());
				studyDAO.updateEntity(s);
				auditService.logVisitActivity(ipAddress, s, user, logStatus, null, null);
				booleanResultDTO.setResult(true);
			});
		}
		else {
			booleanResultDTO.setResult(false);
		}
		
		return booleanResultDTO;
	}

	// used only under transactions, whether in this class or in SubjectService
	void stampStudyAndLogStudySubjectActivity(
			final String ipAddress, 
			final Study study, 
			final Subject subject, 
			final User user,
			final String action, 
			final String requiredFieldPreviousData,
			final String previousData) {
		
		study.setStatusChange(new Date());
		studyDAO.updateEntity(study);
		auditService.logStudySubjectActivity(ipAddress, study, subject, user, action, requiredFieldPreviousData, previousData);
	}


	void stampStudyAndLogStudySubjectMrnActivity(
			final String ipAddress,
			final Study study,
			final SubjectMrn subjectMrn,
			final User user,
			final String action,
			final String requiredFieldPreviousData,
			final String previousData) {

		study.setStatusChange(new Date());
		studyDAO.updateEntity(study);
		auditService.logStudyAndSubjectMrnActivity(ipAddress, study, subjectMrn, user, action, requiredFieldPreviousData, previousData);
	}

	// used only in this class
	void stampStudyAndLogStudyMemberActivity(
			final String ipAddress, 
			final Study study, 
			final User studyUser,
			final User user, 
			final String action,
			final String requiredFieldPreviousData, 
			final String previousData) {
		
		study.setStatusChange(new Date());
		studyDAO.updateEntity(study);
		auditService.logStudyMemberActivity(ipAddress, study, studyUser, user, action, requiredFieldPreviousData, previousData);
	}

	//@Transactional
	public BooleanResultDTO assignStudyMembers(final AddStudyMembersDTO dto, final User usr, final String ipAddress, final String templatePath) {

		final BooleanResultDTO booleanResultDTO = new BooleanResultDTO();
		final List<StudyUser> sendEmails = new ArrayList<>();
		final Study study = studyDAO.findStudyById(dto.getStudyId());
		final List<User> usersAlreadyOnTheStudy = new ArrayList<>();

		for(int i = 0; i < dto.getStudyMembers().size(); i++) {
			final User user = studyDAO.findByStudyMemberId(dto.getStudyMembers().get(i));
			final StudyUser existingStudyUser = studyDAO.findStudyUserByStudyAndUser(study, user);
			if (existingStudyUser == null) {
				final StudyUser studyUser = new StudyUser();
				studyUser.setStudy(study);
				studyUser.setUser(user);
				studyUser.setActive(Boolean.TRUE);
				stampStudyAndLogStudyMemberActivity(ipAddress, study, user, usr, Statics.AUDIT_STUDY_MEMBER_CREATE, null, null);
				studyDAO.createEntity(studyUser);
				sendEmails.add(studyUser);
			}
			else {
				usersAlreadyOnTheStudy.add(user);
			}
		}

		sendStudyMemberEmails(sendEmails, usr.getInstitution().getLongName(), templatePath);
		booleanResultDTO.setResult(true);
		return booleanResultDTO;
	}

	// used only in this class
	private void sendStudyMemberEmails(final List<StudyUser> users, final String institution, final String templatePath) {
		final String title = "You have been granted access to study. ";
		
		final List<Optional<SimpleMailMessage>> messages = enrich(users).map(u -> {
			final String to = u.getUser().getPreferredNotificationEmail();
			final String studyname = u.getStudy().getName();
			final String local = u.getStudy().getLocalId();
			final String catid = u.getStudy().getCatalystId();
			final String spid = u.getStudy().getSpid();
			final String irb = u.getStudy().getIrb();

			final StringTemplateGroup group = new StringTemplateGroup("underwebinf", templatePath, DefaultTemplateLexer.class);

			final StringTemplate studyMemberEmail = group.getInstanceOf("email");

			studyMemberEmail.setAttribute("institution", institution);
			studyMemberEmail.setAttribute("studyName", studyname);
			studyMemberEmail.setAttribute("local", local);
			studyMemberEmail.setAttribute("catid", catid);
			studyMemberEmail.setAttribute("spid", spid);
			studyMemberEmail.setAttribute("irb", irb);

			return Optional.of(u.getUser().getEmail()).map(email -> new MailMessageBuilder().to(to).subject(title).text(studyMemberEmail.toString()).build());

		}).toList();
		
		flatten(messages).forEach(mailHandler::sendOptionalEmails);
	}

	//@Transactional   // read-only
	public List<Comments> getVisitTemplateComments(final int visitId) {
		final VisitTemplate visit = studyDAO.findVisitById(visitId);

		return studyDAO.findVisitTemplateCommentsByVisit(visit);
	}

	//@Transactional   // read-only
	public long getNumVisitTemplateComments(final int visitId) {
		return studyDAO.findNumVisitTemplateCommentsByVisit(visitId);
	}

	//@Transactional
	public TemplateApprovalHistory approveVisitTemplate(final TemplateApprovalHistoryDTO visit, final User user, final String ipAddress, final String templatePath) {

		final VisitTemplate visitTemplate = studyDAO.findVisitById(visit.getVisitTemplate());
		final User u = authDAO.findUserById(visit.getUser().getId());
		final TemplateApprovalHistory approvalUser = studyDAO.findTemplateApprovalHistoryByVisitAndUser(visitTemplate, user);

		final TemplateApprovalHistory history;
		
		if(approvalUser != null) {
			history = studyDAO.findTemplateApprovalHistoryByVisitAndUser(visitTemplate, visit.getUser());
			history.setVisitTemplate(visitTemplate);
			history.setComment(visit.getComment());
			history.setApproved(Boolean.TRUE);

			if(u.getRole() != null && u.getRole().getType() == RoleType.ROLE_FINAL_APPROVER) {
				visitTemplate.setApproved(Boolean.TRUE);
				studyDAO.updateEntity(visitTemplate);
			}

			history.setUser(visit.getUser());
			history.setStatusChangeTime(new Date());
			visitTemplate.setLastUpdateTime(new Date());
			studyDAO.updateEntity(visitTemplate);
			studyDAO.updateEntity(history);
		} else {
			// TODO: use Builder pattern
			history = new TemplateApprovalHistory(
					visit.getUser(),
					null, // lastName
					null, // firstName
					null, // role
					null, // title
					Boolean.TRUE,
					visit.getComment(),
					new Date(),
					visitTemplate,
					null, // visitName
					null  // visitCreated
					);


			if(u.getRole() != null && u.getRole().getType() == RoleType.ROLE_FINAL_APPROVER) {
				visitTemplate.setApproved(Boolean.TRUE);
				studyDAO.updateEntity(visitTemplate);
			}

			visitTemplate.setLastUpdateTime(new Date());
			studyDAO.updateEntity(visitTemplate);
			studyDAO.createEntity(history);
		}
		auditService.logVisitActivity(ipAddress, visitTemplate, user, Statics.AUDIT_VISIT_TEMPLATE_APPROVED, null, null);
		sendVisitApprovalEmail(visitTemplate, u, ipAddress, user.getInstitution().getLongName(), templatePath);
		return history;
	}

	// used only in this class
	private void sendVisitApprovalEmail(final VisitTemplate visitTemplate, final User user, final String ipAddress, final String institution, final String templatePath) {

		final String studyName = visitTemplate.getStudy().getName();
		final String userRole = user.getRole().getName();
		final String visitName = visitTemplate.getName();
		final String localId = visitTemplate.getStudy().getLocalId();
		final String catid = visitTemplate.getStudy().getCatalystId();
		final String spid = visitTemplate.getStudy().getSpid();
		final String irb = visitTemplate.getStudy().getIrb();

		final String title = "Visit Template is Approved. ";

		final StringTemplateGroup group = new StringTemplateGroup("underwebinf", templatePath, DefaultTemplateLexer.class);

		final StringTemplate userEmail = group.getInstanceOf("visitApprovalEmail");
		userEmail.setAttribute("institution", institution);
		userEmail.setAttribute("userRole", userRole);
		userEmail.setAttribute("visitName", visitName);
		userEmail.setAttribute("studyName", studyName);
		userEmail.setAttribute("local", localId);
		userEmail.setAttribute("catid", catid);
		userEmail.setAttribute("spid", spid);
		userEmail.setAttribute("irb", irb);

		final User protocolNutritionist = visitTemplate.getStudy().getProtocolNutritionist();
		final User protocolNurse = visitTemplate.getStudy().getProtocolNurse();
		final User scheduler = visitTemplate.getStudy().getScheduler();
		final RoleType roleType = user.getRole().getType();

		if(roleType == RoleType.ROLE_FINAL_APPROVER) {
			sentVisitApprovalEmails(visitTemplate, user, ipAddress, title, userEmail, scheduler);
			sentVisitApprovalEmails(visitTemplate, user, ipAddress, title, userEmail, protocolNutritionist);
			sentVisitApprovalEmails(visitTemplate, user, ipAddress, title, userEmail, protocolNurse);
		}
		else if(roleType == RoleType.ROLE_ADMINISTRATIVE_DIRECTOR) {
			final List<User> usersList = authDAO.findFinalApproverByRole();
			sendUserEmails(visitTemplate, user, ipAddress, title, userEmail, usersList);
		}
		else if(roleType == RoleType.ROLE_SCHEDULER) {
			final List<User> nurseList = authDAO.findNurseManagerUserByRole();
			final List<User> nutritionList = authDAO.findNutritionManagerUserByRole();
			sentVisitApprovalEmails(visitTemplate, user, ipAddress, title, userEmail, protocolNutritionist);
			sentVisitApprovalEmails(visitTemplate, user, ipAddress, title, userEmail, protocolNurse);
			sendUserEmails(visitTemplate, user, ipAddress, title, userEmail, nurseList);
			sendUserEmails(visitTemplate, user, ipAddress, title, userEmail, nutritionList);
		}
		else if(roleType == RoleType.ROLE_PROTOCOL_NURSE || roleType == RoleType.ROLE_PROTOCOL_NUTRITIONIST) {
			final List<User> usersList = authDAO.findAdminDirectorUserByRole();
			sendUserEmails(visitTemplate, user, ipAddress, title, userEmail, usersList);
		}
	}

	private void sendUserEmails(final VisitTemplate v, final User u, final String ipAddress, final String title, final StringTemplate userEmail, final List<User> users) {
		if(!users.isEmpty()) {
			for (final User user : users) {
				sentVisitApprovalEmails(v, u, ipAddress, title, userEmail, user);
			}
		}
	}

	private void sentVisitApprovalEmails(final VisitTemplate v, final User u, final String ipAddress, final String title, final StringTemplate userEmail, final User user) {
		if (user != null) {
			mailHandler.sendOptionalEmails(new MailMessageBuilder().to(user.getPreferredNotificationEmail()).subject(title).text(userEmail.toString()).build());

			final String previousData = " Email : " + user.getEmail() + " and Role : " + user.getRole().getName();
			
			auditService.logVisitApprovalActivity(ipAddress, v, u, user,
					Statics.AUDIT_VISIT_TEMPLATE_APPROVED_EMAIL_SENT, previousData, null);
		}
	}

	//@Transactional
	public TemplateApprovalHistory commentVisitTemplate(final TemplateApprovalHistoryDTO visit, final User user, final String ipAddress) {

		final TemplateApprovalHistory history;
		final VisitTemplate visits = studyDAO.findVisitById(visit.getVisitTemplate());
		final User u = authDAO.findUserById(visit.getUser().getId());
		final TemplateApprovalHistory approvalUser = studyDAO.findTemplateApprovalHistoryByVisitAndUser(visits, user);

		if(approvalUser != null) {
			history = studyDAO.findTemplateApprovalHistoryByVisitAndUser(visits, visit.getUser());
			history.setVisitTemplate(visits);
			history.setComment(visit.getComment());
			history.setApproved(Boolean.FALSE);
			history.setUser(visit.getUser());
			history.setStatusChangeTime(new Date());
			studyDAO.updateEntity(history);
		} else {
			history = new TemplateApprovalHistory(
					visit.getUser(),
					null, // lastName
					null, // firstName
					null, // role
					null, // title
					Boolean.FALSE,
					visit.getComment(),
					new Date(),
					visits,
					null, // visitName
					null  // visitCreated
					);
			studyDAO.createEntity(history);
		}
		if(u.getRole() != null && u.getRole().getType() == RoleType.ROLE_FINAL_APPROVER) {
			visits.setApproved(Boolean.FALSE);
		}
		visits.setLastUpdateTime(new Date());
		studyDAO.updateEntity(visits);
		auditService.logVisitActivity(ipAddress, visits, user, Statics.AUDIT_VISIT_TEMPLATE_COMMENT, null, null);
		return history;
	}

	//@Transactional
	public List<VisitApprovalModelResponseDTO.VisitApproval> getVisitApprovals(final int visitId, final String sortBy, final String orderBy, final int page, final int maxResults) {
		return studyDAO.getVisitApprovals(visitId, sortBy, orderBy, page, maxResults);
	}

	List<VisitTemplatesResponse> adjustVisitsPerPageAndMaxResults(final int page, final int maxResults, final List<VisitTemplatesResponse> visitsParam) {

		final List<VisitTemplatesResponse> visitsResult = new ArrayList<>(visitsParam);

		final int grandTotal = visitsResult.size();
		
		for (final VisitTemplatesResponse visit : visitsResult) {
			visit.setTotalCount(grandTotal);
		}

		final int start = (page - 1) * maxResults;

		if(start + maxResults <= grandTotal) {
			return visitsResult.subList(start, start + maxResults);
		} else if(start + maxResults > grandTotal) {
			return visitsResult.subList(start, grandTotal);
		} else {
			return visitsResult;
		}
	}

	boolean seeAllUnapprovedExceptUserApproved(final RoleType userRole, final InstitutionRoleType institutionRole) {

		final Map<RolePair, Boolean> rolePairMap = RolePair.getRolePairMap();
		
		return rolePairMap.get(new RolePair(userRole, institutionRole)) != null;
	}

	boolean seeAllUnapproved(final RoleType userRole, final InstitutionRoleType institutionRole) {
		return userRole == RoleType.ROLE_FINAL_APPROVER && institutionRole == InstitutionRoleType.ROLE_SUPER_ADMIN;
	}

	//@Transactional
	public List<VisitTemplatesResponse> getTemplatesToApprove(final String sortBy, final String orderBy, final int page, final int maxResults, final int userId, final String ipAddress) {

		final User user = authDAO.findUserById(userId);
		final RoleType userRole = user.getRole().getType();
		final InstitutionRoleType institutionRole = user.getInstitutionRole().getType();

		auditService.logViewActivity(ipAddress, user, "HOME SCREEN -Template Approval View.");

		if(seeAllUnapproved(userRole, institutionRole)) {
			return studyDAO.getNotApprovedVisits(sortBy, orderBy, page, maxResults);
		}
		else if(seeAllUnapprovedExceptUserApproved(userRole, institutionRole)){
			return removeApprovedVisitsByUser(user, studyDAO.getNotApprovedVisits(sortBy, orderBy, page, maxResults));
		}
		else if(institutionRole == InstitutionRoleType.ROLE_STUDY_STAFF) {
			final List<Study> studyStaffStudies = studyDAO.findStudyListByPerson(user);

			final List<VisitTemplatesResponse> withStudies = addStudysVisitToList(studyStaffStudies, Collections.emptyList(), user);
			
			final List<VisitTemplatesResponse> withoutApprovedVisits = removeApprovedVisitsByUser(user, withStudies);
			
			// totals in the visits may correct for other use-cases, but need to be updated for this one
			// also, there should be only one field for total, since there is only one distinct value
			return adjustVisitsPerPageAndMaxResults(page, maxResults, withoutApprovedVisits);
		}
		else {
			//Nurse Manager List

			final List<Study> schedulerStudies = studyDAO.findStudyByScheduler(user);
			final List<Study> piStudies = studyDAO.findStudyByPI(user);
			final List<Study> nurseStudies = studyDAO.findStudyByNurse(user);
			final List<Study> nutritionistStudies = studyDAO.findStudyByNutritionist(user);

			final List<VisitTemplatesResponse> withPiStudies = addStudysVisitToList(piStudies, Collections.emptyList(), user);

			final List<VisitTemplatesResponse> withNurseStudies = addStudysVisitToList(nurseStudies, withPiStudies, user);

			final List<VisitTemplatesResponse> withNutritionistStudies = addStudysVisitToList(nutritionistStudies, withNurseStudies, user);
			
			return addStudysVisitToList(schedulerStudies, withNutritionistStudies, user);
		}
	}

	private List<VisitTemplatesResponse> removeApprovedVisitsByUser(final User user, final List<VisitTemplatesResponse> visits) {
		
		final List<TemplateApprovalHistory> userTemplateHistory = studyDAO.findTemplateApprovalHistoryListByUser(user);
		final List<VisitTemplatesResponse> userApprovedTemplates = getUserApprovedTemplates(userTemplateHistory);

		final List<VisitTemplatesResponse> result = new ArrayList<>(visits);
		
		//NutritionManager List
		result.removeAll(userApprovedTemplates);
		
		return result; 
	}

	List<VisitTemplatesResponse> addStudysVisitToList(final List<Study> studyList, final List<VisitTemplatesResponse> visitTemplateListParam, final User user) {
		if (studyList == null || studyList.isEmpty()) {
			return visitTemplateListParam;
		}
		
		final List<TemplateApprovalHistory> userTemplateHistory = studyDAO.findTemplateApprovalHistoryListByUser(user);

		final List<VisitTemplatesResponse> userApprovedTemplates = getUserApprovedTemplates(userTemplateHistory);

		final List<VisitTemplatesResponse> accumulator = new ArrayList<>(visitTemplateListParam);
		
		for(final Study study : studyList) {

			final List<VisitTemplate> visitTemplateList = studyDAO.findVisitTemplateByStudy(study);
			
			long total = visitTemplateList.size();

			for (final VisitTemplate visitTemplate: visitTemplateList) {

				final List<TemplateApprovalHistory> history = studyDAO.findTemplateApprovalHistoryListByVisitAndUser(visitTemplate, user);

				if(MiscUtil.isNonNullNonEmpty(history)) {

					for(final TemplateApprovalHistory vi : history) {
						total = history.size();

						final VisitTemplatesResponse visitDetail = studyDAO.setVisitTemplateData(total, vi.getVisitTemplate());

						accumulator.add(visitDetail);
					}
					accumulator.removeAll(userApprovedTemplates);

				} else {

					final VisitTemplatesResponse visitDetail = studyDAO.setVisitTemplateData(total, visitTemplate);
					accumulator.add(visitDetail);
				}
			}
		}
		
		return accumulator;
	}

	private List<VisitTemplatesResponse> getUserApprovedTemplates(final List<TemplateApprovalHistory> userTemplateHistory) {
		return userTemplateHistory.stream().map(th -> {
			final long total = userTemplateHistory.size();
			final VisitTemplate visitTemplate = th.getVisitTemplate();
			return studyDAO.setVisitTemplateData(total, visitTemplate);
		}).collect(Collectors.toList());
	}

	User nullForZeroFindUserForNonZeroId(final int id) {
		if (id != 0) {
			return authDAO.findUserById(id);
		}
		
		return null;
	}

	void setSomeOtherUsers(final StudyDTO studyDTO, final Study study) {
		study.setInvestigator(authDAO.findUserById(studyDTO.getInvestigator()));
		study.setScheduler(authDAO.findUserById(studyDTO.getScheduler()));

		study.setPhysician(nullForZeroFindUserForNonZeroId(studyDTO.getPhysician()));
		study.setPhysician2(nullForZeroFindUserForNonZeroId(studyDTO.getPhysician2()));
		study.setAssociateNurse(nullForZeroFindUserForNonZeroId(studyDTO.getAssociateNurse()));
		study.setAssociateProtocolNutritionist(nullForZeroFindUserForNonZeroId(studyDTO.getAssociateProtocolNutritionist()));
		study.setSecondaryScheduler(nullForZeroFindUserForNonZeroId(studyDTO.getSecondaryScheduler()));
		study.setScheduler3(nullForZeroFindUserForNonZeroId(studyDTO.getScheduler3()));

		if (studyDTO.getProtocolNurse() == -1){
			study.setProtocolNurseString(NA);
			study.setProtocolNurse(null);
		}
		else {
			study.setProtocolNurse(authDAO.findUserById(studyDTO.getProtocolNurse()));
			study.setProtocolNurseString(null);
		}

		if (studyDTO.getProtocolNutritionist() == -1){
			study.setProtocolNutritionistString(NA);
			study.setProtocolNutritionist(null);
		}
		else {
			study.setProtocolNutritionist(authDAO.findUserById(studyDTO.getProtocolNutritionist()));
			study.setProtocolNutritionistString(null);
		}
	}

	//@Transactional
	public VisitTemplateDetailResponse getVisitTemplateData(final int visitId, final User user, final String ipAddress) {
		logViewVisit(visitId, user, ipAddress);

		VisitTemplateDetailResponse visitTemplateDetailResponse = studyDAO.getVisitDataById(visitId);

		if(visitTemplateDetailResponse != null)
		{
			long numVisitTemplateComments = getNumVisitTemplateComments(visitId);
			visitTemplateDetailResponse.setNumVisitTemplateComments(numVisitTemplateComments);
		}

		return visitTemplateDetailResponse;
	}

	//@Transactional   // read-only
	public List<StudyDataResponse> getStudiesList(final String sortBy, final String orderBy, final int page,
												  final int maxResults, final User user, SearchDTO searchDTO) {

		if(user.isStudyStaff()) {
			return studyDAO.getStudyListByPerson(sortBy, orderBy, page, maxResults, user, searchDTO);
		} else {
			return studyDAO.getStudyList(sortBy, orderBy, page, maxResults, searchDTO);
		}
	}

	//@Transactional   // read-only
	public StudyDetailResponse getStudyData(final int studyId, final User user, final String ipAddress) {
		final Study s = studyDAO.findStudyById(studyId);
		
		stampStudyAndLogStudyActivity(ipAddress, s, user, Statics.AUDIT_STUDY_VIEW, null, null);
		
		return studyDAO.getStudyDataById(studyId);
	}

	//@Transactional   // read-only
	public BooleanRequest changeStudySubjectStatus(final int studySubject, final User user, final String ipAddress) {
		final BooleanRequest booleanRequest = new BooleanRequest();
		final StudySubject s = subjectDAO.findStudySubjectById(studySubject);
		
		if (s.getActive()) {
			s.setActive(false);
			studyDAO.updateEntity(s);
			deactivateStudySubject(user, ipAddress, s);
		}
		else {
			s.setActive(true);
			studyDAO.updateEntity(s);
			activateStudySubject(user, ipAddress, s);
		}
		
		booleanRequest.setResult(true);
		
		return booleanRequest;
	}

	//@Transactional
	public List<UserDataResponse> getStudyMembersList(final int studyId, final String sortBy, final String orderBy, final int page, final int maxResults, SearchDTO searchDto) {
		final Study study = studyDAO.findStudyById(studyId);
		
		return studyDAO.getStudyMembers(study, sortBy, orderBy, page, maxResults, searchDto);
	}

	//@Transactional
	public BooleanRequest changeStudyMemberStatus(final int studyMember, final User user, final String ipAddress) {
		
		final StudyUser studyUser = studyDAO.findStudyUserRoleById(studyMember);
		
		if (studyUser.getActive()) {
			studyUser.setActive(false);
			studyDAO.updateEntity(studyUser);
			deactivateStudyMember(user, ipAddress, studyUser);
		} else {
			studyUser.setActive(true);
			studyDAO.updateEntity(studyUser);
			activateStudyMember(user, ipAddress, studyUser);
		}
		
		final BooleanRequest booleanRequest = new BooleanRequest();
		
		booleanRequest.setResult(true);
		
		return booleanRequest;
	}

	private void deactivateStudySubject(final User user, final String ipAddress, final StudySubject s) {
		manipulateStudySubject(user, ipAddress, s, Statics.AUDIT_STUDY_SUBJECT_DEACTIVATE);
	}

	private void activateStudySubject(final User user, final String ipAddress, final StudySubject s) {
		manipulateStudySubject(user, ipAddress, s, Statics.AUDIT_STUDY_SUBJECT_ACTIVATE);
	}
	private void deactivateStudyMember(final User user, final String ipAddress, final StudyUser s) {
		manipulateStudyMember(user, ipAddress, s, Statics.AUDIT_STUDY_SUBJECT_DEACTIVATE);
	}

	private void activateStudyMember(final User user, final String ipAddress, final StudyUser s) {
		manipulateStudyMember(user, ipAddress, s, Statics.AUDIT_STUDY_SUBJECT_ACTIVATE);
	}
	
	private void manipulateStudySubject(final User user, final String ipAddress, final StudySubject s, final String action) {
		stampStudyAndLogStudySubjectActivity(user, ipAddress, s, action, StudySubject::getStudy, StudySubject::getSubject);
	}
	
	private void manipulateStudyMember(final User user, final String ipAddress, final StudyUser s, final String action) {
		stampStudyAndLogStudyMemberActivity(user, ipAddress, s, action, StudyUser::getStudy, StudyUser::getUser);
	}
	
	private <S> void stampStudyAndLogStudySubjectActivity(
			final User user, 
			final String ipAddress, 
			final S s, 
			final String action, 
			final Function<S, Study> getStudy, 
			final Function<S, Subject> getSubject) {
	
		stampStudyAndLogStudySubjectActivity(ipAddress, getStudy.apply(s), getSubject.apply(s), user, action, null, null);
	}
	
	private <S> void stampStudyAndLogStudyMemberActivity(
			final User user, 
			final String ipAddress, 
			final S s, 
			final String action, 
			final Function<S, Study> getStudy, 
			final Function<S, User> getUser) {
	
		stampStudyAndLogStudyMemberActivity(ipAddress, getStudy.apply(s), getUser.apply(s), user, action, null, null);
	}
}
