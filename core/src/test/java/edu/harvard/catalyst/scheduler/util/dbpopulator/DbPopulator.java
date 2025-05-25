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
package edu.harvard.catalyst.scheduler.util.dbpopulator;

import edu.harvard.catalyst.scheduler.entity.StudyFundingSource;
import edu.harvard.catalyst.scheduler.persistence.SiteDAO;

/**
 * Created by xavier on 8/2/17.
 */
public class DbPopulator {

    SiteDAO dao;

    /**
     * Seed data populators
     */
    public AppointmentOverrideReasonPopulator appointmentOverrideReasonPopulator;
    public AppointmentStatusPopulator appointmentStatusPopulator;
    public AppointmentStatusReasonPopulator appointmentStatusReasonPopulator;
    public BookedResourcePopulator bookedResourcePopulator;
    public BookedVisitPopulator bookedVisitPopulator;
    public CancellationStatusPopulator cancellationStatusPopulator;
    public CategoryPopulator categoryPopulator;
    public CentersAndInstitutionsPopulator centersAndInstitutionsPopulator;
    public CommentsPopulator commentsPopulator;
    public CountryPopulator countryPopulator;
    public CredentialPopulator credentialPopulator;
    public DepartmentPopulator departmentPopulator;
    public DivisionPopulator divisionPopulator;
    public EthnicityPopulator ethnicityPopulator;
    public EthnicityMappingPopulator ethnicityMappingPopulator;
    public FacultyRankPopulator facultyRankPopulator;
    public FieldPopulator fieldPopulator;
    public FundingSourcePopulator fundingSourcePopulator;
    public GenderPopulator genderPopulator;
    public InstitutionPopulator institutionPopulator;
    public InstitutionRolePopulator institutionRolePopulator;
    public IRBInstitutionPopulator irbInstitutionPopulator;
    public LineLevelAnnotationsPopulator lineLevelAnnotationsPopulator;
    public RacePopulator racePopulator;
    public ReportPopulator reportPopulator;
    public ReportTemplatePopulator reportTemplatePopulator;
    public ResourcePopulator resourcePopulator;
    public ResourceAnnotationPopulator resourceAnnotationPopulator;
    public RolePopulator rolePopulator;
    public StatePopulator statePopulator;
    public StudyFundingSourcePopulator studyFundingSourcePopulator;
    public StudyPopulator studyPopulator;
    public StudyStatusPopulator studyStatusPopulator;
    public StudySubjectPopulator studySubjectPopulator;
    public StudyUserPopulator studyUserPopulator;
    public SubCategoryPopulator subCategoryPopulator;
    public SubjectPopulator subjectPopulator;
    public SubjectMrnPopulator subjectMrnPopulator;
    public SublocationPopulator sublocationPopulator;
    public TemplateCategoryPopulator templateCategoryPopulator;
    public TemplateCategoryFieldPopulator templateCategoryFieldPopulator;
    public TemplateResourcePopulator templateResourcePopulator;
    public TemplateResourceGroupPopulator templateResourceGroupPopulator;
    public TemplateResourceAnnotationsPopulator templateResourceAnnotationsPopulator;
    public UserPopulator userPopulator;
    public VisitTemplatePopulator visitTemplatePopulator;
    public VisitTypePopulator visitTypePopulator;

    public DbPopulator(SiteDAO dao) {

        this.dao = dao;

        this.appointmentOverrideReasonPopulator = new AppointmentOverrideReasonPopulator(this);
        this.appointmentStatusPopulator = new AppointmentStatusPopulator(this);
        this.appointmentStatusReasonPopulator = new AppointmentStatusReasonPopulator(this);
        this.bookedResourcePopulator = new BookedResourcePopulator(this);
        this.bookedVisitPopulator = new BookedVisitPopulator(this);
        this.cancellationStatusPopulator = new CancellationStatusPopulator(this);
        this.categoryPopulator = new CategoryPopulator(this);
        this.categoryPopulator = new CategoryPopulator(this);
        this.centersAndInstitutionsPopulator = new CentersAndInstitutionsPopulator(this);
        this.commentsPopulator = new CommentsPopulator(this);
        this.countryPopulator = new CountryPopulator(this);
        this.credentialPopulator = new CredentialPopulator(this);
        this.departmentPopulator = new DepartmentPopulator(this);
        this.divisionPopulator = new DivisionPopulator(this);
        this.ethnicityPopulator = new EthnicityPopulator(this);
        this.ethnicityMappingPopulator = new EthnicityMappingPopulator(this);
        this.facultyRankPopulator = new FacultyRankPopulator(this);
        this.fieldPopulator = new FieldPopulator(this);
        this.fundingSourcePopulator = new FundingSourcePopulator(this);
        this.genderPopulator = new GenderPopulator(this);
        this.institutionPopulator = new InstitutionPopulator(this);
        this.institutionRolePopulator = new InstitutionRolePopulator(this);
        this.irbInstitutionPopulator = new IRBInstitutionPopulator(this);
        this.lineLevelAnnotationsPopulator = new LineLevelAnnotationsPopulator(this);
        this.racePopulator = new RacePopulator(this);
        this.reportPopulator = new ReportPopulator(this);
        this.reportTemplatePopulator = new ReportTemplatePopulator(this);
        this.resourcePopulator = new ResourcePopulator(this);
        this.resourceAnnotationPopulator = new ResourceAnnotationPopulator(this);
        this.rolePopulator = new RolePopulator(this);
        this.statePopulator = new StatePopulator(this);
        this.studyFundingSourcePopulator = new StudyFundingSourcePopulator(this);
        this.studyPopulator = new StudyPopulator(this);
        this.studyStatusPopulator = new StudyStatusPopulator(this);
        this.studySubjectPopulator = new StudySubjectPopulator(this);
        this.studyUserPopulator = new StudyUserPopulator(this);
        this.subCategoryPopulator = new SubCategoryPopulator(this);
        this.subjectPopulator = new SubjectPopulator(this);
        this.subjectMrnPopulator = new SubjectMrnPopulator(this);
        this.sublocationPopulator = new SublocationPopulator(this);
        this.templateCategoryPopulator = new TemplateCategoryPopulator(this);
        this.templateCategoryFieldPopulator = new TemplateCategoryFieldPopulator(this);
        this.templateResourceAnnotationsPopulator = new TemplateResourceAnnotationsPopulator(this);
        this.templateResourceGroupPopulator = new TemplateResourceGroupPopulator(this);
        this.templateResourcePopulator = new TemplateResourcePopulator(this);
        this.userPopulator = new UserPopulator(this);
        this.visitTemplatePopulator = new VisitTemplatePopulator(this);
        this.visitTypePopulator = new VisitTypePopulator(this);

    }

}

