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

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import edu.harvard.catalyst.hccrc.core.util.LazyList;
import edu.harvard.catalyst.hccrc.core.util.Pair;
import edu.harvard.catalyst.scheduler.core.BookedVisitActivityLogStatics;
import edu.harvard.catalyst.scheduler.core.SchedulerRuntimeException;
import edu.harvard.catalyst.scheduler.core.Statics;
import edu.harvard.catalyst.scheduler.dto.*;
import edu.harvard.catalyst.scheduler.dto.response.*;
import edu.harvard.catalyst.scheduler.dto.statics.CalendarFilter;
import edu.harvard.catalyst.scheduler.entity.*;
import edu.harvard.catalyst.scheduler.persistence.*;
import edu.harvard.catalyst.scheduler.util.*;
import org.antlr.stringtemplate.StringTemplate;
import org.antlr.stringtemplate.StringTemplateGroup;
import org.antlr.stringtemplate.language.DefaultTemplateLexer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import static edu.harvard.catalyst.hccrc.core.util.LazyList.lazy;
import static edu.harvard.catalyst.hccrc.core.util.ListUtils.enrich;
import static edu.harvard.catalyst.hccrc.core.util.Pair.pair;
import static edu.harvard.catalyst.scheduler.core.Statics.NA;
import static edu.harvard.catalyst.scheduler.util.DateUtility.*;
import static edu.harvard.catalyst.scheduler.util.MiscUtil.*;


@Component
public class AppointmentService {

    private static final int INCREMENT_FACTOR = 30;

    private final MailHandler mailHandler;
    private final AuthDAO authDAO;
    private final AuditService auditService;
    private final AppointmentDAO appointmentDAO;
    private final ResourceDAO resourceDAO;
    private final StudyDAO studyDAO;
    private final SubjectDAO subjectDAO;
    private final TemplateResourceDAO templateResourceDAO;

    private final SearchAlgorithmService searchAlgorithmService;
    private final ConflictChecker conflictChecker;
    private final AppointmentConfirmer appointmentConfirmer;
    private final Object confirmationLock = new Object();

    @Autowired
    public AppointmentService(
            final AppointmentDAO appointmentDAO,
            final ResourceDAO resourceDAO,
            final StudyDAO studyDAO,
            final AuthDAO authDAO,
            final AuditService auditService,
            final MailHandler mailHandler,
            final SubjectDAO subjectDAO,
            final TemplateResourceDAO templateResourceDAO,
            final SearchAlgorithmService searchAlgorithmService
    ) {
        this(
                appointmentDAO,
                resourceDAO,
                studyDAO,
                authDAO,
                auditService,
                mailHandler,
                subjectDAO,
                templateResourceDAO, Optional.empty(),
                Optional.empty(),
                searchAlgorithmService
        );
    }

    public AppointmentService(
            final AppointmentDAO appointmentDAO,
            final ResourceDAO resourceDAO,
            final StudyDAO studyDAO,
            final AuthDAO authDAO,
            final AuditService auditService,
            final MailHandler mailHandler,
            final SubjectDAO subjectDAO,
            final TemplateResourceDAO templateResourceDAO, final Optional<ConflictChecker> conflictChecker,
            final Optional<AppointmentConfirmer> appointmentConfirmer,
            final SearchAlgorithmService searchAlgorithmService
    ) {

        this.appointmentDAO = appointmentDAO;
        this.resourceDAO = resourceDAO;
        this.studyDAO = studyDAO;
        this.authDAO = authDAO;
        this.auditService = auditService;
        this.mailHandler = mailHandler;
        this.subjectDAO = subjectDAO;
        this.templateResourceDAO = templateResourceDAO;

        this.conflictChecker = nullToEmpty(conflictChecker).orElse(defaultConflictChecker);
        this.appointmentConfirmer = nullToEmpty(appointmentConfirmer).orElseGet(() -> new DefaultAppointmentConfirmer());

        this.searchAlgorithmService = searchAlgorithmService;
    }

    // Don't use - Needed for spring security cglib proxying
    AppointmentService() {
        this(null, null, null, null, null, null, null, null, null, null, null);
    }

    public GetStudyVisitsResponse getStudyVisits(final String nullableFilterString, final String ofSortBy, final
    String ofOrderBy, final Integer ofPage, final Integer ofMaxResults, final Integer ofStudyId, final Boolean
            ofApproved) {

        return studyDAO.getStudyVisits(nullableFilterString, ofSortBy, ofOrderBy, ofPage, ofMaxResults, ofStudyId,
                                       ofApproved);
    }

    public GetSearchVisitResourceResponse getVisitResources(final String ofSortBy, final String ofOrderBy, final
    Integer ofPage, final Integer ofMaxResults, final Integer ofVisitId) {

        return templateResourceDAO.findTemplateResourcesByVisit(ofVisitId, ofSortBy, ofOrderBy, ofPage, ofMaxResults);
    }

    public List<VisitCommentsResponse.VisitComment> getAppointmentComments(final int bookedVisitId) {
        final BookedVisit bookedVisit = appointmentDAO.findBookedVisitById(bookedVisitId);
        return appointmentDAO.findAppointmentCommentsByVisit(bookedVisit);
    }

    public Long getTotalAppointmentComments(final int bookedVisitId) {
        return appointmentDAO.findTotalAppointmentCommentsByVisit(bookedVisitId);
    }

    public BookedVisitDTO logViewBookedVisit(final BookedVisitDTO bookedVisitDTO, final User user, final String
            ipAddress) {
        final BookedVisit bookedVisit = appointmentDAO.findBookedVisitById(bookedVisitDTO.getId());
        auditService.logAppointmentActivity(ipAddress, bookedVisit, user, BookedVisitActivityLogStatics.VIEWED_DETAILS_FROM_APPOINTMENT_LIST);
        return bookedVisitDTO;
    }

    public BookedVisitDTO logIncompleteOverrideActions(final BookedVisitDTO bv, final User user, final String
            ipAddress, final String action) {
        final BookedResource bookedResource = appointmentDAO.findBookedResourceById(bv.getId());
        auditService.logAppointmentOverrideActivity(ipAddress, bookedResource.getBookedVisit(), bookedResource.getResource(), user, action,
                                                    null, null);
        return bv;
    }


    public List<CalendarVisitsResponse> getCalendarBookedVisits(
            final int userId, final CalendarFilter calendarFilter, final String filterString, final String sublocationName,
            final Date startMonth, final Date endMonth, final String remoteHost, final boolean homeView
    ) {

        final User user = authDAO.findUserById(userId);
        final List<CalendarVisitsResponse> visits = getCalendarBookedVisitsByFilter(filterString, calendarFilter, sublocationName, user,
                                                                                    startMonth, endMonth, homeView
        );

        String action = "Appointment Calendar Viewed";
        if (homeView) {
            action = "HOME SCREEN - Appointment Calendar View.";
        }
        auditService.logViewActivity(remoteHost, user, action);
        return visits;
    }

    void setRooms(final List<BookedVisit> visits) {
        if (visits != null) {
            for (final BookedVisit bookedVisit : visits) {
                String rooms = " ";
                final List<BookedResource> bookedResources = appointmentDAO.findRoomBookedResourcesByBookedVisit
                        (bookedVisit.getId());
                if (bookedResources != null) {
                    for (final BookedResource bookedResource : bookedResources) {
                        if (bookedResource.getResource().getResourceType().getName().equalsIgnoreCase("Room")) {
                            rooms += bookedResource.getResource().getName() + ", ";
                            bookedVisit.setRooms(rooms);
                        }
                    }
                }
            }
        }
    }

    List<CalendarVisitsResponse> getCalendarBookedVisitsByFilter(
            final String filterString, final CalendarFilter calendarFilter,
            String sublocationName, final User user, final Date startMonth, final Date endMonth, final boolean homeView
    ) {
        List<CalendarVisitsResponse> result = Lists.newArrayList();
        List<Study> userAccessibleStudies = null;

        if (user.isStudyStaff()) {
            userAccessibleStudies = studyDAO.findStudyListByPerson(user);
        }

        if (!calendarFilter.equals(CalendarFilter.NO_FILTER)) {
            result = filterCalendarVisitsResponses(filterString, calendarFilter, sublocationName, user, startMonth, endMonth,
                                                   homeView, result, userAccessibleStudies
            );
        } else {
            result = appointmentDAO.getCalendarBookedVisits(sublocationName, startMonth, endMonth, homeView,
                                                            userAccessibleStudies, user.isStudyStaff()
            );
        }
        return result;
    }

    public List<CalendarVisitsResponse> filterCalendarVisitsResponses(
            final String filterString,
            final CalendarFilter calendarFilter,
            String sublocationName,
            final User user,
            final Date startMonth,
            final Date endMonth,
            final boolean homeView,
            final List<CalendarVisitsResponse> result,
            final List<Study> userAccessibleStudies
    ) {
        List<CalendarVisitsResponse> filteredResult;

        if (calendarFilter.equals(CalendarFilter.BY_SUBJECT_LAST_NAME)) {
            filteredResult = filterCalendarVisitsResponsesBySubjectLastName(user, filterString, sublocationName, startMonth,
                                                                            endMonth, homeView, result, userAccessibleStudies

            );
        } else if (calendarFilter.equals(CalendarFilter.BY_STUDY_LOCAL_ID)) {
            filteredResult = filterCalendarVisitsResponsesByStudyLocalId(filterString, sublocationName, user, startMonth,
                                                                         endMonth, homeView, result
            );
        } else if (calendarFilter.equals(CalendarFilter.BY_RESOURCE_NAME)) {
            filteredResult = filterCalendarVisitsResponsesByResourceName(filterString, sublocationName, user, startMonth,
                                                                         endMonth, homeView, userAccessibleStudies
            );
        } else {
            filteredResult = filterBookedVisitsByAppointmentStatus(calendarFilter, sublocationName, user, startMonth, endMonth,
                                                                   homeView, userAccessibleStudies
            );
        }
        return filteredResult;
    }

    public List<CalendarVisitsResponse> filterCalendarVisitsResponsesByResourceName(
            final String filterString, final String sublocationName, final
    User user, final Date startMonth, final Date endMonth, final boolean homeView, final List<Study>
                    userAccessibleStudies
    ) {
        return appointmentDAO.findBookedVisitsByResource(filterString, user, userAccessibleStudies, startMonth,
                                                         endMonth, homeView, sublocationName
        );
    }

    public List<CalendarVisitsResponse> filterCalendarVisitsResponsesByStudyLocalId(
            final String filterString,
            final String sublocationName,
            final User user,
            final Date startMonth,
            final Date endMonth,
            final boolean homeView,
            final List<CalendarVisitsResponse> result
    ) {

        final List<Study> studyList = studyDAO.findStudyListByPersonAndLocalID(user, filterString);
        if (studyList.size() > 0) {
            return appointmentDAO.findAllBookedVisitsByStudy(studyList, startMonth, endMonth, homeView, sublocationName);
        }
        return result;
    }

    public List<CalendarVisitsResponse> filterCalendarVisitsResponsesBySubjectLastName(
            final User user,
            final String filterString,
            String sublocationName,
            final Date startMonth,
            final Date endMonth,
            final boolean homeView,
            final List<CalendarVisitsResponse> result,
            final List<Study>
                    userAccessibleStudies
    ) {
        final List<Subject> subjects = subjectDAO.filterSubjectByLastNames(filterString);

        if (subjects.size() > 0) {
                return appointmentDAO.findAllBookedVisitsBySubject(user, userAccessibleStudies, subjects, sublocationName,
                                                                   startMonth, endMonth, homeView
                );
        }
        return result;
    }

    public List<CalendarVisitsResponse> filterBookedVisitsByAppointmentStatus(
            final CalendarFilter calendarFilter, final String sublocationName, final User user,
            final Date startMonth, final Date endMonth, final boolean
                    homeView, final List<Study> userAccessibleStudies
    ) {

        return appointmentDAO.findBookedVisitsByApppointmentStatus(calendarFilter, startMonth, endMonth,
                                                                   homeView, userAccessibleStudies, user.isStudyStaff(),
                                                                   sublocationName
        );
    }

    // @Transactional
    public List<BookedVisitsResponse> getOnlyTodaysBookedVisitsHomeList(final Date startDate, final Date endDate,
                                                                        final String sortBy, final String orderBy,
                                                                        final int page, final int maxResults, final
                                                                        int userId, final String remoteHost) {
        List<BookedVisitsResponse> visits;
        final User user = authDAO.findUserById(userId);

        if (user.isStudyStaff()) {
            final List<Study> userAccessibleStudies = studyDAO.findStudyListByPerson(user);
            visits = appointmentDAO.getOnlyTodaysBookedVisitsByStudy(userAccessibleStudies, startDate, endDate, sortBy, orderBy, page, maxResults);
        } else {
            visits = appointmentDAO.getOnlyTodaysBookedVisits(startDate, endDate, sortBy, orderBy, page, maxResults);
        }
        auditService.logViewActivity(remoteHost, user, "HOME SCREEN - Appointment List View.");
        return visits;
    }

    AppointmentStatus scheduledStatus() {
        return appointmentDAO.findScheduledStatus();
    }

    AppointmentStatus holdStatus() {
        return appointmentDAO.findHoldStatus();
    }

    AppointmentStatus checkedInStatus() {
        return appointmentDAO.findCheckedInStatus();
    }

    AppointmentStatus checkedOutStatus() {
        return appointmentDAO.findCheckedOutStatus();
    }

    AppointmentStatus cancelledStatus() {
        return appointmentDAO.findCancelledStatus();
    }

    public BooleanResultDTO crudTemplateResources(
            final TemplateResourceDTO trDto,
            final String institution,
            final String templatePath,
            final String actionType,
            final User user,
            final String host
    ) {
        trDto.setResult(true);
        VisitTemplate visitTemplate = studyDAO.findVisitById(trDto.getVisitTemplate());
        TemplateResource templateResource;

        if (actionType.equalsIgnoreCase("add_template_resource") ||
            actionType.equalsIgnoreCase("copy_template_resource")) {
            final Resource resource = resourceDAO.findResourceById(trDto.getResource());
            templateResource = new TemplateResource(trDto, resource, visitTemplate);
            appointmentDAO.createEntity(templateResource);
            createTemplateAnnotations(trDto, templateResource);
            adjustDurationUnapproveAndUpdateVisitTemplate(institution, templatePath, visitTemplate);
            auditService.logTemplateResourceActivity(host, user, visitTemplate, Statics
                    .AUDIT_TEMPLATE_RESOURCE_CREATE, null, null);
        } else if (actionType.equalsIgnoreCase("edit_template_resource")) {
            editTemplateResource(trDto, institution, templatePath, user, host, false);
        } else if (actionType.equalsIgnoreCase("edit_template_resource_check_diff_only")) {
            boolean updated = editTemplateResource(trDto, institution, templatePath, user, host, true);
            trDto.setResult(updated);
        } else if (actionType.equalsIgnoreCase("delete_template_resource")) {
            deleteTemplateResource(trDto, institution, templatePath);
            if (trDto.isResult()) {
                auditService.logTemplateResourceActivity(host, user, visitTemplate, Statics
                        .AUDIT_TEMPLATE_RESOURCE_DELETE, null, null);
            }
        }
        if (trDto.isResult()) {
            visitTemplate = studyDAO.findVisitById(trDto.getVisitTemplate());
            trDto.setInfoMsg(visitTemplate.getApproved() ? "Approved" : "Not Approved");
        }
        return trDto;
    }

    public void deleteTemplateResource(TemplateResourceDTO trDto, final String institution, final String templatePath) {
        final TemplateResource selectedTemplateResource = appointmentDAO.findTemplateResourceById(trDto.getId());
        final VisitTemplate visitTemplate = selectedTemplateResource.getVisitTemplate();
        if (deleteTemplateResourceValidation(trDto, selectedTemplateResource)) {
            if (hasGroupId(selectedTemplateResource)) {
                List<TemplateResourceGroup> trgList = appointmentDAO.findTrgListByGroupId(selectedTemplateResource
                                                                                                  .getGroupId());
                if (!selectedTemplateResource.getFloatable()) {
                    final List<TemplateResource> remainingTrs = trgList.stream()
                            .map(trg -> trg.getTemplateResource())
                            .filter(tr -> !(tr.getId().equals(selectedTemplateResource.getId())))
                            .collect(Collectors.toList());

                    if (areFlexResourcesLinkable(trDto, remainingTrs)) {
                        TemplateResourceGroup templateResourceGroup = appointmentDAO.findTemplateResourceGroup
                                (selectedTemplateResource);
                        deleteTemplateResourceAnnotations(selectedTemplateResource);
                        appointmentDAO.deleteEntity(templateResourceGroup);
                        appointmentDAO.deleteEntity(selectedTemplateResource);
                        adjustDurationUnapproveAndUpdateVisitTemplate(institution, templatePath, visitTemplate);
                    }
                } else {
                    deleteTemplateResourceAnnotations(selectedTemplateResource);
                    deleteTemplateResourceGroups(selectedTemplateResource, trgList);
                    adjustDurationUnapproveAndUpdateVisitTemplate(institution, templatePath, visitTemplate);
                }
            } else {
                deleteTemplateResourceAnnotations(selectedTemplateResource);
                appointmentDAO.deleteEntity(selectedTemplateResource);
                adjustDurationUnapproveAndUpdateVisitTemplate(institution, templatePath, visitTemplate);
            }
        } else {
            trDto.setResult(false);
        }
    }

    public BooleanResultDTO checkIfResourcesLinkableOnDeleteUnlink(Integer templateResourceId) {
        BooleanResultDTO dto = new BooleanResultDTO();
        dto.setResult(true);
        final TemplateResource selectedTemplateResource = appointmentDAO.findTemplateResourceById(templateResourceId);
        List<TemplateResourceGroup> trgList = appointmentDAO.findTrgListByGroupId(selectedTemplateResource.getGroupId());

        final List<TemplateResource> remainingTrs = trgList.stream()
                .map(trg -> trg.getTemplateResource())
                .filter(tr -> !(tr.getId().equals(selectedTemplateResource.getId())))
                .collect(Collectors.toList());

        areFlexResourcesLinkable(dto, remainingTrs);
        Integer size = trgList.size();
        dto.setErrorMsg(size.toString());
        return dto;
    }

    void deleteTemplateResourceGroups(TemplateResource selectedTemplateResource, List<TemplateResourceGroup> trgList) {
        trgList.forEach(i -> {
            final TemplateResource tr = templateResourceDAO.findTemplateResourceById(i.getTemplateResource().getId());
            tr.setGroupId(null);
            appointmentDAO.updateEntity(tr);
            appointmentDAO.deleteEntity(i);
        });
        appointmentDAO.deleteEntity(selectedTemplateResource);
    }

    public boolean editTemplateResource(
            final TemplateResourceDTO newTrDto,
            final String institution,
            final String templatePath,
            final User user,
            final String host,
            final Boolean diffCheckOnly
    ) {

        final TemplateResource tr = appointmentDAO.findTemplateResourceById(newTrDto.getId());
        TemplateResourceDTO oldTrDto = new TemplateResourceDTO(tr, templateResourceDAO);

        String updateString = oldTrDto.diffFromThisToOther(newTrDto);
        if (diffCheckOnly) {
            return !updateString.equals(TemplateResourceDTO.NO_CHANGE);
        }

        boolean userUpdated = tr.possiblyUpdateMyNonResourceFields(newTrDto);

        // these two use dao, so here rather than in entity class
        userUpdated = possiblyUpdateThisTrsResourceField(newTrDto, tr) || userUpdated;
        userUpdated = possiblyUpdateThisTrsAnnotations(newTrDto, tr) || userUpdated;

        Date now = new Date();
        tr.setLastUpdateTime(now);
        appointmentDAO.updateEntity(tr);

        VisitTemplate visitTemplate = tr.getVisitTemplate();
        if (userUpdated) {
            adjustDurationUnapproveAndUpdateVisitTemplate(institution, templatePath, visitTemplate);
        }

        auditService.logTemplateResourceActivity(host, user, visitTemplate,
                                                 Statics.AUDIT_TEMPLATE_RESOURCE_UPDATE, updateString, null
        );

        return userUpdated;
    }

    boolean possiblyUpdateThisTrsResourceField(TemplateResourceDTO trDto, TemplateResource tr) {
        boolean updated = false;

        if (tr.getResource().getId() != trDto.getResource()) {
            final List<TemplateResourceAnnotations> currentTRAs = resourceDAO
                    .findTemplateAnnotationsByTemplateResource(tr);
            currentTRAs.forEach(appointmentDAO::deleteEntity);

            final Resource resource = resourceDAO.findResourceById(trDto.getResource());
            tr.setResource(resource);


            updated = true;
        }
        return updated;
    }

    public TemplateResource createTemplateResource(final TemplateResourceDTO tr, final String institution, final
    String templatePath) {
        final Resource resource = resourceDAO.findResourceById(tr.getResource());
        final VisitTemplate visitTemplate = studyDAO.findVisitById(tr.getVisitTemplate());
        final TemplateResource templateResource = new TemplateResource(tr, resource, visitTemplate);
        appointmentDAO.createEntity(templateResource);

        createTemplateAnnotations(tr, templateResource);
        adjustDurationUnapproveAndUpdateVisitTemplate(institution, templatePath, visitTemplate);

        return templateResource;
    }

    // public for testability
    public void createTemplateAnnotations(final TemplateResourceDTO tr, final TemplateResource templateResource) {
        if (!tr.getSelectedAnnotations().isEmpty()) {
            for (int i = 0; i < tr.getSelectedAnnotations().size(); i++) {
                final TemplateResourceAnnotations tra = new TemplateResourceAnnotations();
                tra.setTemplateResource(templateResource);
                tra.setLineLevelAnnotations(resourceDAO.findLineLevelAnnotationsById(tr.getSelectedAnnotations().get
                        (i)));
                tra.setQuantity(tr.getSelectedAnnotationsQuantity().get(i));
                tra.setComment(tr.getSelectedAnnotationsComment().get(i));
                appointmentDAO.createEntity(tra);
            }
        }
    }

    void sendVisitTemplateResourceUpdatedEmail(final VisitTemplate visit, final String institution, final boolean
            newVisitResource, final String templatePath) {
        final String visitName = visit.getName();
        final Study study = visit.getStudy();
        final String studyName = study.getName();
        final String localId = study.getLocalId();
        final String catalystId = study.getCatalystId();
        final String irb = study.getIrb();

        final String prefix = newVisitResource ? "New Resource added to the visit Template : "
                                               : "Visit Template Resource has been updated : ";
        final String title = prefix + visit.getName();

        final StringTemplateGroup group = new StringTemplateGroup("underwebinf", templatePath, DefaultTemplateLexer
                .class);
        final StringTemplate newVisit = group.getInstanceOf("visitCreatedEmail");

        newVisit.setAttribute("institution", institution);
        newVisit.setAttribute("visitName", visitName);
        newVisit.setAttribute("studyName", studyName);
        newVisit.setAttribute("localId", localId);
        newVisit.setAttribute("catalystId", catalystId);
        newVisit.setAttribute("irb", irb);

        final List<User> adminDirector = authDAO.findAdminDirectorUserByRole();
        final List<User> nurseManager = authDAO.findNurseManagerUserByRole();
        final List<User> nutritionManager = authDAO.findNutritionManagerUserByRole();
        final List<User> finalApprover = authDAO.findFinalApproverByRole();

        final boolean noProtocolNutritionist =
                study.getProtocolNutritionist() == null || study.getProtocolNutritionistString() == NA;
        final boolean hasProtocolNutritionist = !noProtocolNutritionist;

        final boolean noProtocolNurse = study.getProtocolNurse() == null || study.getProtocolNurseString() == NA;
        final boolean hasProtocolNurse = !noProtocolNurse;

        final boolean noScheduler = study.getScheduler() == null;
        final boolean hasScheduler = !noScheduler;

        final String schedulerEmail = hasScheduler ? study.getScheduler().getPreferredNotificationEmail() : "";
        final String protocolNutritionistEmail =
                hasProtocolNutritionist ? study.getProtocolNutritionist().getPreferredNotificationEmail() : "";
        final String protocolNurseEmail =
                hasProtocolNurse ? study.getProtocolNurse().getPreferredNotificationEmail() : "";

        final Optional<String> none = Optional.empty();

        if (hasScheduler && noProtocolNutritionist && noProtocolNurse) {

            sendMessage(schedulerEmail, title, newVisit, none, none);

        } else if (noScheduler && hasProtocolNutritionist && noProtocolNurse) {

            sendMessage(protocolNutritionistEmail, title, newVisit, none, none);

        } else if (noScheduler && noProtocolNutritionist && hasProtocolNurse) {

            sendMessage(protocolNurseEmail, title, newVisit, none, none);

        } else if (hasScheduler && hasProtocolNutritionist && noProtocolNurse) {

            sendMessage(schedulerEmail, title, newVisit, Optional.of(protocolNutritionistEmail), none);

        } else if (hasScheduler && noProtocolNutritionist && hasProtocolNurse) {

            sendMessage(protocolNurseEmail, title, newVisit, Optional.of(schedulerEmail), none);

        } else if (noScheduler && hasProtocolNutritionist && hasProtocolNurse) {

            sendMessage(protocolNurseEmail, title, newVisit, Optional.of(protocolNutritionistEmail), none);

        } else if (hasScheduler && hasProtocolNutritionist && hasProtocolNurse) {

            //Why not getPreferredNotificationEmail for the Protocol Nutritionist?
            sendMessage(schedulerEmail, title, newVisit, Optional.of(protocolNurseEmail), Optional.of(study.getProtocolNutritionist().getEmail()));
        }

        // Send Email(s) to Nurse Manager/Nutrition Manager/Admin Director/Final Approver

        sendMessagesToHigherUps(adminDirector, title, newVisit);

        sendMessagesToHigherUps(nurseManager, title, newVisit);

        sendMessagesToHigherUps(nutritionManager, title, newVisit);

        sendMessagesToHigherUps(finalApprover, title, newVisit);
    }

    private void sendMessagesToHigherUps(final List<User> managers, final String title, final StringTemplate newVisit) {

        final Function<User, SimpleMailMessage> toMessage = u -> new MailMessageBuilder().to(u.getPreferredNotificationEmail()).subject(title).text(newVisit.toString()).build();

        enrich(managers).map(toMessage).forEach(mailHandler::sendOptionalEmails);
    }

    private void sendMessage(final String recipient, final String title, final StringTemplate newVisit, final
    Optional<String> ccRecipient, final Optional<String> bccRecipient) {
        final MailMessageBuilder builder = new MailMessageBuilder().to(recipient).subject(title).text(newVisit.toString());

        ccRecipient.ifPresent(builder::cc);

        bccRecipient.ifPresent(builder::bcc);

        mailHandler.sendOptionalEmails(builder.build());
    }

    public BooleanResultDTO updateTemplateResourcesBillable(
            final Integer visitId,
            final String templateResourcesCommaString,
            final boolean isBillable
    ) {
        BooleanResultDTO booleanResultDTO = new BooleanResultDTO();

        final VisitTemplate visitTemplate = studyDAO.findVisitTemplateById(visitId);
        final List<Integer> templateResourcesId = Lists.newArrayList(Splitter.on(",").split
                (templateResourcesCommaString)).stream()
                .map(Integer::parseInt)
                .collect(Collectors.toList());

        templateResourcesId.stream().forEach(trId -> {
            TemplateResource templateResource = appointmentDAO.findTemplateResourceById(trId);
            templateResource.setBillable(isBillable);
            Date now = new Date();
            templateResource.setLastUpdateTime(now);
            appointmentDAO.updateEntity(templateResource);
        });

        if (visitTemplate.getApproved()) {
            visitTemplate.setApproved(Boolean.FALSE);
            visitTemplate.setLastUpdateTime(new Date());
            appointmentDAO.updateEntity(visitTemplate);
        }
        booleanResultDTO.setResult(true);
        return booleanResultDTO;
    }

    public BooleanResultDTO updateTemplateResourceTime(Integer templateResourceId, Integer startMinutes, Integer endMinutes) {
        BooleanResultDTO booleanResultDTO = new BooleanResultDTO();
        final TemplateResource templateResource = appointmentDAO.findTemplateResourceById(templateResourceId);
        final VisitTemplate visitTemplate = studyDAO.findVisitTemplateById(templateResource.getVisitTemplate().getId());
        if (templateResource.getFloatable()) {templateResource.setFloatStart(startMinutes);
            templateResource.setFloatEnd(endMinutes);
        } else {
            templateResource.setStartMinutes(startMinutes);
            templateResource.setEndMinutes(endMinutes);
            computeAndSetDuration(templateResource);
        }

        templateResource.setLastUpdateTime(new Date());
        appointmentDAO.updateEntity(templateResource);

        if (visitTemplate.getApproved()) {
            visitTemplate.setApproved(Boolean.FALSE);
            visitTemplate.setLastUpdateTime(new Date());
        }

        final List<TemplateResource> templateResourcesByVisit = templateResourceDAO.findTemplateResourcesByVisit(visitTemplate);

        int totalDuration = computeTotalDuration(visitTemplate, templateResourcesByVisit);
        visitTemplate.setDuration(totalDuration);

        appointmentDAO.updateEntity(visitTemplate);
        booleanResultDTO.setResult(true);
        return booleanResultDTO;

    }

    public GanttResourceInfoDTO getGanttResourceInfo(Integer visitId, Integer trId, int dayOffset) {
        TemplateResource templateResource = appointmentDAO.findTemplateResourceById(trId);
        List<TemplateResource> allTrsForResourceAndVisit = templateResourceDAO.findTemplateResourcesByVisitAndResource(visitId,
                                                                                                            templateResource.getResource().getId());
        GanttResourceInfoDTO ganttResourceInfo = new GanttResourceInfoDTO(allTrsForResourceAndVisit, dayOffset);
        return ganttResourceInfo;
    }

    //Man, a case class would be nice here. :\
    private static final class NameAndEmail {
        public final String name;

        public final String email;

        private static final NameAndEmail BothNull = new NameAndEmail(null, null);

        private static <A extends HasFirstName & HasLastName & HasEmail> NameAndEmail of(final A entity) {
            if (entity == null) {
                return BothNull;
            }

            final String name = entity.getFirstName() + " " + entity.getLastName();

            return new NameAndEmail(name, entity.getEmail());
        }

        private NameAndEmail(final String name, final String email) {
            super();
            this.name = name;
            this.email = email;
        }
    }

    void sendOverBookedVisitEmail(final BookedVisit visit, final String institution, final String templatePath, final
    List<String> conditions) {
        final String visitName = visit.getName();
        final Study study = visit.getStudy();
        final String localId = study.getLocalId();
        final String visitTypeName = visit.getVisitType().getName();
        final String sublocation = visit.getVisitTemplate().getSublocation().getName();
        final Date startTime = visit.getScheduledStartTime();
        final Date endTime = visit.getScheduledEndTime();

        final NameAndEmail pi = NameAndEmail.of(study.getInvestigator());
        final NameAndEmail primarySchedulingContact = NameAndEmail.of(study.getScheduler());
        final NameAndEmail protoNurse = NameAndEmail.of(study.getProtocolNurse());
        final NameAndEmail protoNutritionist = NameAndEmail.of(study.getProtocolNutritionist());

        final String title =
                visitName + "," + "Local ID " + localId + "," + "was overbooked for " + visit.getScheduledStartTime();
        final StringTemplateGroup group = new StringTemplateGroup("underwebinf", templatePath, DefaultTemplateLexer
                .class);
        final StringTemplate stringTemplate = group.getInstanceOf("overbookEmail");
        stringTemplate.setAttribute("institution", institution);
        stringTemplate.setAttribute("visitName", visitName);
        stringTemplate.setAttribute("localId", localId);
        stringTemplate.setAttribute("visitType", visitTypeName);
        stringTemplate.setAttribute("sublocation", sublocation);
        stringTemplate.setAttribute("startTime", startTime);
        stringTemplate.setAttribute("endTime", endTime);
        stringTemplate.setAttribute("piName", pi.name);
        stringTemplate.setAttribute("piEmail", pi.email);
        stringTemplate.setAttribute("primarySchedulingContactName", primarySchedulingContact.name);
        stringTemplate.setAttribute("primarySchedulingContactEmail", primarySchedulingContact.email);
        stringTemplate.setAttribute("protoNurseName", protoNurse.name);
        stringTemplate.setAttribute("protoNurseEmail", protoNurse.email);
        stringTemplate.setAttribute("protoNutritionistName", protoNutritionist.name);
        stringTemplate.setAttribute("protoNutritionistEmail", protoNutritionist.email);
        stringTemplate.setAttribute("conditions", conditions);

        sendEmailNotifications(study, title, stringTemplate);
    }

    // public for testability
    public void copyTemplateAnnotations(final List<TemplateResourceAnnotations> tras, final TemplateResource
            clonedRes) {
        enrich(tras).map(tra -> {
            final TemplateResourceAnnotations clonedResAnn = new TemplateResourceAnnotations();

            clonedResAnn.setComment(tra.getComment());
            clonedResAnn.setLineLevelAnnotations(tra.getLineLevelAnnotations());
            clonedResAnn.setTemplateResource(clonedRes);
            clonedResAnn.setQuantity(tra.getQuantity());

            return clonedResAnn;
        }).forEach(studyDAO::createEntity);
    }

    int computeTotalDuration(final VisitTemplate visitTemplate, final List<TemplateResource> templateResourceList) {
        if (templateResourceList.isEmpty()) {
            return 0;
        }

        final int earliestStartTime;
        final int latestEndTime;
        if (visitTemplate.getRelativeTime()) {
            earliestStartTime = findEarliestInpatientStartTime(templateResourceList);
            latestEndTime = findLatestInpatientEndTime(templateResourceList);
        } else {
            earliestStartTime = findEarliestInpatientClockStartTime(templateResourceList);
            latestEndTime = findLatestInpatientEndTime(templateResourceList);
        }
        return latestEndTime - earliestStartTime;
    }

    /*** returns true iff there is anything that is copy-able ***/
    public boolean populateCurrentDayCopyables(
            final int visitId,
            final int currentDay,
            final List<TemplateResource> nonGroupTrsToCopy,
            final List<TemplateResource> flexGroupedTrsToCopy,
            final List<TemplateResource> floatGroupedTrsToCopy,
            final List<TemplateResource> trsCandidatesForCopy
    ) {

        final int selectedDateInMinutes = (currentDay -1) * MINS_PER_DAY;
        final int nextDateInMinutes = currentDay * MINS_PER_DAY;
        trsCandidatesForCopy.addAll(getTemplateResourcesUsedBetween(visitId, selectedDateInMinutes,
                nextDateInMinutes));

        trsCandidatesForCopy.stream()
                .forEach(tr -> {
                    if (tr.getGroupId() == null) {
                        if (tr.getFloatable()) {
                            checkIfSingleDayFloatResources(nonGroupTrsToCopy, selectedDateInMinutes,
                                                           nextDateInMinutes, tr);
                        } else {
                            nonGroupTrsToCopy.add(tr);
                        }
                    } else {
                        if (tr.getFlexible()) {
                            flexGroupedTrsToCopy.add(tr);
                        } else {
                            checkIfSingleDayFloatResources(floatGroupedTrsToCopy, selectedDateInMinutes,
                                    nextDateInMinutes, tr);
                        }
                    }
                });
        return nonGroupTrsToCopy.size() + floatGroupedTrsToCopy.size() > 0
               || dayHasCopyableFlexGroups(flexGroupedTrsToCopy);
    }

    private void checkIfSingleDayFloatResources(List<TemplateResource> floatTrsToCopy, int selectedDateInMinutes,
                                                int nextDateInMinutes, TemplateResource tr) {
        int floatStart = tr.getFloatStart();
        int floatEnd = tr.getFloatEnd();

        if (((floatStart > selectedDateInMinutes) || floatStart == selectedDateInMinutes)
                && floatEnd < nextDateInMinutes) {
            floatTrsToCopy.add(tr);
        }
    }

    public boolean isDayCopyable(final int visitId, final int currentDay) {
        return populateCurrentDayCopyables(visitId, currentDay, Lists.newArrayList(), Lists.newArrayList(), Lists
                .newArrayList(), Lists.newArrayList());
    }

    public void copyDayVisitResources(final int visitId, final int currentDay, final String targetDaysCommaString,
                                      final String institution, final String templatePath) {

        final List<TemplateResource> nonGroupTrsToCopy = Lists.newArrayList();
        final List<TemplateResource> flexGroupedTrsToCopy = Lists.newArrayList();
        final List<TemplateResource> floatGroupedTrsToCopy = Lists.newArrayList();
        final List<TemplateResource> trsCandidatesForCopy = Lists.newArrayList();

        boolean doCopy = populateCurrentDayCopyables(
                visitId,
                currentDay,
                nonGroupTrsToCopy,
                flexGroupedTrsToCopy,
                floatGroupedTrsToCopy,
                trsCandidatesForCopy
        );

        if (!doCopy) {
            return;
        }

        final List<Integer> targetDayOffsets = Lists.newArrayList(Splitter.on(",").split(targetDaysCommaString))
                .stream()
                .map(Integer::parseInt)
                .collect(Collectors.toList());

        copyNonGroupTrs(currentDay, nonGroupTrsToCopy, targetDayOffsets);
        copyFlexGroupTrs(currentDay, flexGroupedTrsToCopy, targetDayOffsets);
        copyFloatGroupTrs(currentDay, floatGroupedTrsToCopy, targetDayOffsets);

        final VisitTemplate theVisit = trsCandidatesForCopy.get(0).getVisitTemplate();
        adjustDurationUnapproveAndUpdateVisitTemplate(institution, templatePath, theVisit);
    }

    Map<String, List<TemplateResource>> groupTrsToGroupIdMapToLists(List<TemplateResource> groupedTrs) {
        Map<String, List<TemplateResource>> result = Maps.newHashMap();

        groupedTrs.stream()
                .forEach(tr -> {
                    String groupId = tr.getGroupId();
                    List<TemplateResource> trList = result.get(groupId);
                    if (trList == null) {
                        trList = Lists.newArrayList();
                        result.put(groupId, trList);
                    }
                    trList.add(tr);
                });
        return result;
    }

    void copyFloatGroupTrs(int currentDay, List<TemplateResource> floatGroupedTrsToCopy, List<Integer>
            targetDayOffsets) {
        Map<String, List<TemplateResource>> groupIdToTrList = groupTrsToGroupIdMapToLists(floatGroupedTrsToCopy);

        for (String groupId : groupIdToTrList.keySet()) {
            final List<TemplateResource> aGroupsTrList = groupIdToTrList.get(groupId);

            copyGroupResources(currentDay, targetDayOffsets, aGroupsTrList);
        }
    }

    boolean flexGroupIsCopyable(final List<TemplateResource> theGroupsTrgListFromThisDay, final String groupId) {

        final List<TemplateResourceGroup> theGroupsTrListFromTotalVisit = appointmentDAO.findTrgListByGroupId(groupId);

        return (theGroupsTrgListFromThisDay.size() == theGroupsTrListFromTotalVisit.size());
    }

    boolean dayHasCopyableFlexGroups(List<TemplateResource> flexGroupedTrsToCopy) {
        Map<String, List<TemplateResource>> groupIdToTrList = groupTrsToGroupIdMapToLists(flexGroupedTrsToCopy);

        for (String groupId : groupIdToTrList.keySet()) {
            final List<TemplateResource> theGroupsTrgListFromThisDay = groupIdToTrList.get(groupId);

            if (flexGroupIsCopyable(theGroupsTrgListFromThisDay, groupId)) {
                return true;
            }
        }
        return false;
    }

    void copyFlexGroupTrs(int currentDay, List<TemplateResource> flexGroupedTrsToCopy, List<Integer> targetDayOffsets) {
        Map<String, List<TemplateResource>> groupIdToTrList = groupTrsToGroupIdMapToLists(flexGroupedTrsToCopy);

        for (String groupId : groupIdToTrList.keySet()) {
            final List<TemplateResource> theGroupsTrgListFromThisDay = groupIdToTrList.get(groupId);

            if (flexGroupIsCopyable(theGroupsTrgListFromThisDay, groupId)) {
                copyGroupResources(currentDay, targetDayOffsets, theGroupsTrgListFromThisDay);
            }
        }
    }

    void copyGroupResources(int currentDay, List<Integer> targetDayOffsets, List<TemplateResource>
            theGroupsTrgListFromThisDay) {
        for (Integer dayOffset : targetDayOffsets) {

            final String newGroupId = UUID.randomUUID().toString();

            for (final TemplateResource trToCopy : theGroupsTrgListFromThisDay) {

                final List<TemplateResourceAnnotations> traList = templateResourceDAO
                        .findTemplateResourceAnnotationsByTemplateResource(trToCopy);

                final TemplateResource clonedTr = copyTemplateResourceMostOfTheWay(currentDay, trToCopy, dayOffset);

                clonedTr.setGroupId(newGroupId);
                copyTemplateResourceFinalSteps(traList, clonedTr);

                final TemplateResourceGroup templateResourceGroup = new TemplateResourceGroup();
                templateResourceGroup.setTemplateResource(clonedTr);
                templateResourceGroup.setGroupId(newGroupId);
                templateResourceGroup.setFlexGroup(clonedTr.getFlexible());
                templateResourceGroup.setVisit(clonedTr.getVisitTemplate());
                create(templateResourceGroup);

            }
        }
    }

    void copyNonGroupTrs(int currentDay, List<TemplateResource> nonGroupTrsToCopy, List<Integer> targetDayOffsets) {
        for (final TemplateResource trToCopy : nonGroupTrsToCopy) {

            final List<TemplateResourceAnnotations> traList = templateResourceDAO
                    .findTemplateResourceAnnotationsByTemplateResource(trToCopy);

            for (Integer dayOffset : targetDayOffsets) {

                final TemplateResource clonedTr = copyTemplateResourceMostOfTheWay(currentDay, trToCopy, dayOffset);

                copyTemplateResourceFinalSteps(traList, clonedTr);
            }
        }
    }

    void copyTemplateResourceFinalSteps(List<TemplateResourceAnnotations> traList, TemplateResource clonedTr) {
        clonedTr.setId(null);
        studyDAO.createEntity(clonedTr);

        copyTemplateAnnotations(traList, clonedTr);
    }

    TemplateResource copyTemplateResourceMostOfTheWay(int currentDay, TemplateResource trToCopy, Integer dayOffset) {
        int dayDelta = dayOffset - currentDay;
        int minutesDelta = (MINS_PER_DAY * dayDelta);

        final TemplateResource clonedTr = trToCopy.cloneTemplateResource();

        clonedTr.setStartMinutes(trToCopy.getStartMinutes() + minutesDelta);
        clonedTr.setEndMinutes(trToCopy.getEndMinutes() + minutesDelta);

        Date now = new Date();
        clonedTr.setCreatedDate(now);
        clonedTr.setLastUpdateTime(now);

        if (trToCopy.getFloatable()) {
            clonedTr.setFloatStart(trToCopy.getFloatStart() + minutesDelta);
            clonedTr.setFloatEnd(trToCopy.getFloatEnd() + minutesDelta);
        }
        return clonedTr;
    }

    // @Transactional
    public ResourceTimeBoundsAndCountResponseDTO findTemplateResourceCountEarliestLatest(final int visitId) {
        return templateResourceDAO.findTemplateResourceCountEarliestLatest(visitId);
    }

    // @Transactional
    public List<TemplateResource> getTemplateResourcesUsedBetween(
            final int visitId,
            int selectedDayInMinutes,
            int nextDayInMinutes
    ) {

        final List<TemplateResource> templateResourceResultList = templateResourceDAO.getTemplateResourcesUsedOnDay(
                visitId,
                selectedDayInMinutes,
                nextDayInMinutes
        );

        return templateResourceResultList;
    }

    public List<TemplateResource> getSelectableTemplateResources(
            int visitId, boolean isBillable, String sortBy, String orderBy, int page, int maxResults
    ) {
        VisitTemplate visitTemplate = studyDAO.findVisitById(visitId);
        return templateResourceDAO.findTemplateResourcesByVisitAndBillable(
                visitTemplate, isBillable, sortBy, orderBy, page, maxResults
        );
    }

    public List<TemplateResource> getTemplateResources(final int visitId, boolean addQuantities) {
        return getTemplateResources(visitId, addQuantities, "");
    }

    // @Transactional
    public List<TemplateResource> getTemplateResources(final int visitId, boolean addQuantities, String
            sortExpression) {
        List<TemplateResourceWithTraListDTO> trWithAnnotationsDtoList =
                templateResourceDAO.findTemplateResourcesAndAnnotationsByVisit(visitId, sortExpression);

        List<TemplateResource> trsWithAnnotationsString = Lists.newArrayList();

        for (TemplateResourceWithTraListDTO trwaDto : trWithAnnotationsDtoList) {
            TemplateResource tr = trwaDto.getTemplateResource();
            tr.determineAnnotationsString(trwaDto.getTraList(), addQuantities);
            trsWithAnnotationsString.add(tr);
        }
        return trsWithAnnotationsString;
    }

    private static boolean hasGroupId(final TemplateResource r) {

        return SearchAlgorithmService.hasGroupId(r);
    }

    // @Transactional
    public TemplateResourceWithLlaListDTO getTemplateResourceDataWithAnnotations(final int templateResourceId) {
        final TemplateResource templateResource = appointmentDAO.findTemplateResourceById(templateResourceId);
        List<TemplateResourceAnnotations> traList = resourceDAO.findTemplateAnnotationsByTemplateResource
                (templateResource);
        List<LineLevelAnnotations> llaList = getResourceAnnotations(templateResource.getResource().getId());

        for (int i = 0; i < traList.size(); i++) {
            int indexOf = llaList.indexOf(traList.get(i).getLineLevelAnnotations());
            llaList.get(indexOf).setSelected(true);
            llaList.get(indexOf).setQuantity(traList.get(i).getQuantity());
            llaList.get(indexOf).setComment(traList.get(i).getComment());
            llaList.get(indexOf).setResourceAnnotations(traList.get(i).getId());
        }

        Collections.sort(llaList, new LineLevelAnnotations.AnnotationsComparator());

        TemplateResourceWithLlaListDTO templateResourceWithLlaListDTO =
                new TemplateResourceWithLlaListDTO(templateResource, llaList);

        return templateResourceWithLlaListDTO;
    }

    // @Transactional
    public List<TemplateResource> getUngroupedTemplateResourcesByType(final int visitId, final String
            templateResourceType) {
        final VisitTemplate visit = studyDAO.findVisitById(visitId);
        return templateResourceDAO.findUngroupedTemplateResourcesTypeByVisit(visit, templateResourceType);
    }

    public GanttComboResponseDTO getTemplateResourcesForGanttCombo(
            final Integer visitId,
            final int dayOffset,
            final GanttInfoSortType sortType
    ) {
        GanttComboResponseDTO result = new GanttComboResponseDTO();

        List<TemplateResource> allTrsForVisit = getTemplateResources(visitId, true, sortType.getSortClause());

        result.setInfoDayResources(getGanttInfoDay(allTrsForVisit, dayOffset, GanttResponseDTO.GanttInfoType
                .Resources));
        result.setInfoDayEvents(getGanttInfoDay(allTrsForVisit, dayOffset, GanttResponseDTO.GanttInfoType.Events));

        result.setInfoMultiResources(getGanttInfoMulti(allTrsForVisit, dayOffset, GanttResponseDTO.GanttInfoType
                .Resources));
        result.setInfoMultiEvents(getGanttInfoMulti(allTrsForVisit, dayOffset, GanttResponseDTO.GanttInfoType.Events));

        result.setInfoFloatResources(getGanttInfoFloat(allTrsForVisit, dayOffset, GanttResponseDTO.GanttInfoType
                .ResourcesGroup));
        result.setInfoFloatEvents(getGanttInfoFloat(allTrsForVisit, dayOffset, GanttResponseDTO.GanttInfoType.Events));

        result.setInfoFlexResources(getGanttInfoFlex(allTrsForVisit, dayOffset, GanttResponseDTO.GanttInfoType
                .ResourcesGroup));
        result.setInfoFlexEvents(getGanttInfoFlex(allTrsForVisit, dayOffset, GanttResponseDTO.GanttInfoType.Events));

        return result;
    }

    public GanttResponseDTO getGanttInfoDay(
            final List<TemplateResource> allTrsForTheVisit,
            final int dayOffset,
            final GanttResponseDTO.GanttInfoType ganttInfoType
    ) {

        GanttResponseDTO ganttResponseDTO = ganttInfoType.create(
                allTrsForTheVisit,
                dayOffset,
                tr -> tr.occursInOneDay()
                      && tr.startDayOffsetMatches(dayOffset)
                      && !tr.isGrouped()
        );

        return ganttResponseDTO;
    }

    public GanttResponseDTO getGanttInfoMulti(
            final List<TemplateResource> allTrsForTheVisit,
            final int dayOffset,
            final GanttResponseDTO.GanttInfoType ganttInfoType
    ) {

        GanttResponseDTO ganttResponseDTO = ganttInfoType.create(
                allTrsForTheVisit,
                dayOffset,
                tr -> !tr.occursInOneDay()
                      && tr.resourceOverlapsDayOffset(dayOffset)
                      && !tr.isGrouped()
        );

        return ganttResponseDTO;
    }

    public GanttResponseDTO getGanttInfoFloat(
            final List<TemplateResource> allTrsForTheVisit,
            final int dayOffset,
            final GanttResponseDTO.GanttInfoType ganttInfoType
    ) {

        GanttResponseDTO ganttResponseDTO = ganttInfoType.create(
                allTrsForTheVisit,
                dayOffset,
                tr -> tr.isGrouped()
                      && tr.resourceOverlapsDayOffset(dayOffset)
                      && tr.getFloatable()
        );

        return ganttResponseDTO;
    }

    public GanttResponseDTO getGanttInfoFlex(
            final List<TemplateResource> allTrsForTheVisit,
            final int dayOffset,
            final GanttResponseDTO.GanttInfoType ganttInfoType
    ) {

        GanttResponseDTO ganttResponseDTO = ganttInfoType.create(
                allTrsForTheVisit,
                dayOffset,
                tr -> tr.isGrouped()
                      && tr.resourceOverlapsDayOffset(dayOffset)
                      && tr.getFlexible()
        );

        return ganttResponseDTO;
    }

    public GanttGroupablesResponseDTO getGanttInfoCandidateGroupables(
            Integer visitId,
            Integer templateResourceId,
            GanttResponseDTO.GanttGroupingType groupingType
    ) {

        GanttGroupablesResponseDTO ganttGroupablesResponseDTO = groupingType.create(
                getTemplateResources(visitId, true),
                templateResourceId
        );

        return ganttGroupablesResponseDTO;
    }

    boolean possiblyUpdateThisTrsAnnotations(final TemplateResourceDTO trDto, final TemplateResource templateResource) {

        final List<TemplateResourceAnnotations> currentTRAs = resourceDAO.findTemplateAnnotationsByTemplateResource
                (templateResource);
        final List<TemplateResourceAnnotations> candidateTRAs = Lists.newArrayList();

        final List<Integer> selectedAnnotationIDs = trDto.getSelectedAnnotations();

        for (int i = 0; i < selectedAnnotationIDs.size(); i++) {
            final LineLevelAnnotations lla = resourceDAO.findLineLevelAnnotationsById(selectedAnnotationIDs.get(i));
            final TemplateResourceAnnotations tra = new TemplateResourceAnnotations();
            tra.setTemplateResource(templateResource);
            tra.setLineLevelAnnotations(lla);
            tra.setQuantity(trDto.getSelectedAnnotationsQuantity().get(i));
            tra.setComment(trDto.getSelectedAnnotationsComment().get(i));

            candidateTRAs.add(tra);
        }

        boolean updated = nonCoextensionalLists(candidateTRAs, currentTRAs);

        if (updated) {
            currentTRAs.forEach(appointmentDAO::deleteEntity);
            candidateTRAs.forEach(appointmentDAO::createEntity);
        }

        return updated;
    }

    <T> boolean nonCoextensionalLists(final List<T> list1, final List<T> list2) {

        Set set1 = Sets.newHashSet(list1);
        Set set2 = Sets.newHashSet(list2);

        return !set1.equals(set2);
    }

    // @Transactional
    public BooleanResultDTO unlinkOneResource(
            final Integer deserterId,
            final String institution,
            final String templatePath
    ) {

        BooleanResultDTO booleanResultDTO = new BooleanResultDTO();

        booleanResultDTO.setResult(true); // optimistic

        final TemplateResource deserterTr = appointmentDAO.findTemplateResourceById(deserterId);

        if (deserterTr == null || deserterTr.getGroupId() == null) {
            booleanResultDTO.setResult(false);
            booleanResultDTO.setErrorMsg("Selected Resource does not exist, or is not linked in a group.");

            return booleanResultDTO;
        }

        final List<TemplateResourceGroup> trgList = appointmentDAO.findTrgListByGroupId(deserterTr.getGroupId());

        if (!trgList.isEmpty()) {

            if (trgList.get(0).getTemplateResource().getFloatable()) {
                unlinkDyadicGroup(institution, templatePath, trgList);
            } else {
                unlinkOneFromNonDyadicGroup(
                        booleanResultDTO,
                        institution,
                        templatePath,
                        deserterTr,
                        trgList
                );
            }
        }

        return booleanResultDTO;
    }

    void unlinkDyadicGroup(final String institution, final String templatePath, final List<TemplateResourceGroup>
            trgList) {
        for (final TemplateResourceGroup trg : trgList) {
            lowLevelUnlinkOneTr(institution, templatePath, trg);
        }
    }

    void lowLevelUnlinkOneTr(
            String institution,
            String templatePath,
            TemplateResourceGroup theTrg
    ) {

        Date now = new Date();
        VisitTemplate visitTemplate = theTrg.getVisit();
        TemplateResource theTr = theTrg.getTemplateResource();
        theTr.setGroupId(null);
        theTr.setLastUpdateTime(now);

        if (visitTemplate.getApproved()) {
            sendVisitTemplateResourceUpdatedEmail(theTr.getVisitTemplate(), institution, false, templatePath);
        }
        visitTemplate.setApproved(false);
        visitTemplate.setLastUpdateTime(now); // ungrouping within the template counts as a change, n'est ce pa?

        update(visitTemplate, theTr);
        delete(theTrg);
    }

    BooleanResultDTO unlinkOneFromNonDyadicGroup(
            final BooleanResultDTO booleanResultDTO,
            final String institution,
            final String templatePath,
            final TemplateResource deserterTr,
            final List<TemplateResourceGroup> trgList
    ) {

        final List<TemplateResource> remainingTrs = trgList.stream()
                .map(trg -> trg.getTemplateResource())
                .filter(tr -> !(tr.getId().equals(deserterTr.getId())))
                .collect(Collectors.toList());

        final List<TemplateResourceGroup> deserterTrgsUnitList = trgList.stream()
                .filter(trg -> (trg.getTemplateResource().getId().equals(deserterTr.getId())))
                .collect(Collectors.toList());

        if (similarResourcesAreLinkable(booleanResultDTO, remainingTrs)) {
            lowLevelUnlinkOneTr(institution, templatePath, deserterTrgsUnitList.get(0));
        }
        return booleanResultDTO;
    }

    public BooleanResultDTO unlinkGroup(
            final String groupId,
            final String institution,
            final String templatePath
    ) {

        final List<TemplateResourceGroup> trgList = appointmentDAO.findTrgListByGroupId(groupId);

        if (!trgList.isEmpty()) {
            trgList.stream()
                    .forEach(trg -> lowLevelUnlinkOneTr(institution, templatePath, trg));
        }
        BooleanResultDTO booleanResultDTO = new BooleanResultDTO(true);
        return booleanResultDTO;
    }

    private void update(final BaseEntity... es) {
        doDaoOp(appointmentDAO::updateEntity, es);
    }

    private void delete(final BaseEntity... es) {
        doDaoOp(appointmentDAO::deleteEntity, es);
    }

    private void create(final BaseEntity... es) {
        doDaoOp(appointmentDAO::createEntity, es);
    }

    private void doDaoOp(final Consumer<? super BaseEntity> op, final BaseEntity... es) {
        for (final BaseEntity e : es) {
            op.accept(e);
        }
    }

    // @Transactional
    public BooleanResultDTO linkTemplateResourcesAsNewGroup(
            final GetTemplateResourceGroupDTO booleanResultDTO,
            final String institution,
            final String templatePath
    ) {

        boolean result = true; // optimistic

        final List<TemplateResource> templateResourceList = booleanResultDTO.getLinkResources().stream()
                .map(appointmentDAO::findTemplateResourceById)
                .collect(Collectors.toList());

        if (templateResourceList.isEmpty() || templateResourceList.contains(null)) {
            result = false; // someone must have concurrently deleted our resources
        } else if (!similarResourcesAreLinkable(booleanResultDTO, templateResourceList)) {
            result = false; // someone must have concurrently invalidated groupability
        } else { // sunny day case
            linkTemplateResourcesAsNewGroup(templateResourceList, institution, templatePath);
        }

        booleanResultDTO.setResult(result);
        return booleanResultDTO;
    }

    void linkTemplateResourcesAsNewGroup(
            final List<TemplateResource> templateResourceList,
            final String institution,
            final String templatePath
    ) {

        final String groupId = UUID.randomUUID().toString();

        for (final TemplateResource templateResource : templateResourceList) {
            final TemplateResourceGroup templateResourceGroup = new TemplateResourceGroup();
            final VisitTemplate visitTemplate = studyDAO.findVisitTemplateById(templateResource.getVisitTemplate()
                                                                                       .getId());
            templateResourceGroup.setTemplateResource(templateResource);
            templateResource.setGroupId(groupId);
            if (visitTemplate.getApproved()) {
                sendVisitTemplateResourceUpdatedEmail(visitTemplate, institution, false, templatePath);
            }

            Date now = new Date();
            templateResource.setLastUpdateTime(now);
            visitTemplate.setLastUpdateTime(now);
            visitTemplate.setApproved(Boolean.FALSE);

            templateResourceGroup.setGroupId(groupId);
            templateResourceGroup.setFlexGroup(templateResource.getFlexible());
            templateResourceGroup.setVisit(visitTemplate);

            update(visitTemplate, templateResource);
            create(templateResourceGroup);
        }
    }

    boolean similarResourcesAreLinkable(final BooleanResultDTO booleanResultDTO, final List<TemplateResource>
            templateResourceList) {
        boolean floatables = templateResourceList.get(0).getFloatable();

        if (floatables) {
            return areFloatResourcesLinkable(booleanResultDTO, templateResourceList);
        } else { // flexibles
            return areFlexResourcesLinkable(booleanResultDTO, templateResourceList);
        }
    }

    boolean areFlexResourcesLinkable(final BooleanResultDTO booleanResultDTO, final List<TemplateResource>
            templateResourceList) {
        final String statusMsg = TemplateResource.isValidFlexGroup(templateResourceList);
        return isLinkableResourcesMsg(booleanResultDTO, statusMsg, templateResourceList.get(0));
    }

    boolean areFloatResourcesLinkable(final BooleanResultDTO booleanResultDTO, final List<TemplateResource>
            templateResourceList) {
        final String statusMsg = TemplateResource.isValidFloatGroup(templateResourceList);
        return isLinkableResourcesMsg(booleanResultDTO, statusMsg, templateResourceList.get(0));
    }

    private boolean isLinkableResourcesMsg(
            final BooleanResultDTO booleanResultDTO,
            final String statusMsg,
            final TemplateResource templateResource
    ) {
        if (!statusMsg.equalsIgnoreCase("OK")) {
            booleanResultDTO.setResult(false);
            booleanResultDTO.setErrorMsg(statusMsg);
            booleanResultDTO.setInfoMsg(templateResource.getGroupId());

            return false;
        }
        return true;
    }

    private void deleteTemplateResourceAnnotations(final TemplateResource selectedTemplateResource) {
        final List<TemplateResourceAnnotations> templateResourceAnnotations = templateResourceDAO
                .findTemplateResourceAnnotationsByTemplateResource(selectedTemplateResource);

        templateResourceAnnotations.forEach(appointmentDAO::deleteEntity);
    }

    boolean deleteTemplateResourceValidation(final TemplateResourceDTO templateResourceDTO, final TemplateResource
            selectedTemplateResource) {
        final boolean isResourceBooked = appointmentDAO.findBookedResourcesByBookedVisit(selectedTemplateResource);

        if (isResourceBooked) {
            templateResourceDTO.setInfoMsg("resource is booked");
            templateResourceDTO.setErrorMsg("At least one visit has been scheduled from this visit template, so you " +
                                            "cannot delete a resource. Please create a new visit template with the " +
                                            "current list of resources.");
            return false;
        }

        return true;
    }

    void adjustDurationUnapproveAndUpdateVisitTemplate(final String institution, final String templatePath, final
    VisitTemplate visitTemplate) {

        final List<TemplateResource> templateResourcesByVisit =
                templateResourceDAO.findTemplateResourcesByVisit(visitTemplate);

        int totalDuration = computeTotalDuration(visitTemplate, templateResourcesByVisit);
        visitTemplate.setDuration(totalDuration);

        if (visitTemplate.getApproved()) {
            visitTemplate.setApproved(Boolean.FALSE);
            sendVisitTemplateResourceUpdatedEmail(visitTemplate, institution, true, templatePath);
        }
        visitTemplate.setLastUpdateTime(new Date());
        appointmentDAO.updateEntity(visitTemplate);
    }

    TemplateResource createTempResourceSlot(final Resource selectedResource,
                                            final TemplateResource templateResource) {

        return searchAlgorithmService.createTempResourceSlot(selectedResource, templateResource);
    }

    Map<Date, List<ResourceSchedule>> retrieveResourceOverrideSchedule(final Resource resource,
                                                                       final Date startTime,
                                                                       final Date endTime) {
        final Map<Date, List<ResourceSchedule>> dayOfWeekSchedule = new HashMap<Date, List<ResourceSchedule>>();
        final List<ResourceSchedule> overrideScheduleList = resourceDAO.findExceptionSchedule(resource, startTime,
                                                                                              endTime, true);
        if (!overrideScheduleList.isEmpty()) {
            populateOverrideScheduleByDate(overrideScheduleList, dayOfWeekSchedule);
        }
        return dayOfWeekSchedule;
    }

    void populateSublocationClosureIntervalScheduleByDate(final List<SublocationClosureInterval>
                                                                  resourceScheduleCollection, final Map<Date,
            List<SublocationClosureInterval>> dayOfWeekSchedule) {
        List<SublocationClosureInterval> schedules;
        if (isNonNullNonEmpty(resourceScheduleCollection)) {
            for (final SublocationClosureInterval rs : resourceScheduleCollection) {
                final Map<Date, String> weekDays = retrieveDaysOfWeek(rs.getStartTime(), rs.getEndTime());
                if (weekDays == null) {
                    continue;
                }
                for (final Map.Entry<Date, String> entry : weekDays.entrySet()) {
                    final Date dayOfWeek = entry.getKey();
                    if (dayOfWeekSchedule.containsKey(dayOfWeek)) {
                        schedules = dayOfWeekSchedule.get(dayOfWeek);
                    } else {
                        schedules = new ArrayList<SublocationClosureInterval>();
                    }
                    final SublocationClosureInterval es = createNewSublocationClosureIntervalByDate(rs, entry.getKey());
                    schedules.add(es);
                    dayOfWeekSchedule.put(dayOfWeek, schedules);
                }
            }
        }
    }

    void populateOverrideScheduleByDate(final List<ResourceSchedule> resourceScheduleCollection, final Map<Date,
            List<ResourceSchedule>> dayOfWeekSchedule) {
        List<ResourceSchedule> schedules;
        if (isNonNullNonEmpty(resourceScheduleCollection)) {
            for (final ResourceSchedule rs : resourceScheduleCollection) {
                final Map<Date, String> weekDays = retrieveDaysOfWeek(rs.getStartTime(), rs.getEndTime());
                if (weekDays == null) {
                    continue;
                }
                for (final Map.Entry<Date, String> entry : weekDays.entrySet()) {
                    final Date dayOfWeek = entry.getKey();
                    if (dayOfWeekSchedule.containsKey(dayOfWeek)) {
                        schedules = dayOfWeekSchedule.get(dayOfWeek);
                    } else {
                        schedules = new ArrayList<ResourceSchedule>();
                    }
                    final ResourceSchedule es = createNewExceptionScheduleByDate(rs, entry.getKey());
                    schedules.add(es);
                    dayOfWeekSchedule.put(dayOfWeek, schedules);
                }
            }
        }
    }

    Map<Date, String> retrieveDaysOfWeek(final Date startDate, final Date endDate) {
        return searchAlgorithmService.retrieveDaysOfWeek(startDate, endDate);
    }

    ResourceSchedule createNewExceptionScheduleByDate(final ResourceSchedule curSchedule, final Date startDate) {
        final ResourceSchedule es = new ResourceSchedule();
        es.setDayOfWeek(null);
        es.setEndTime(startDate);
        es.setEndTime(curSchedule.getEndTime());
        es.setId(curSchedule.getId());
        es.setQuantity(curSchedule.getQuantity());
        es.setStartTime(startDate);
        es.setStartTime(curSchedule.getStartTime());
        return es;
    }

    SublocationClosureInterval createNewSublocationClosureIntervalByDate(final SublocationClosureInterval
                                                                                 curSchedule, final Date startDate) {
        final SublocationClosureInterval es = new SublocationClosureInterval();
        es.setEndTime(startDate);
        es.setEndTime(curSchedule.getEndTime());
        es.setId(curSchedule.getId());
        es.setStartTime(startDate);
        es.setStartTime(curSchedule.getStartTime());
        return es;
    }

    /**
     * Check if the resource is available during the search time range based on
     * its count set in resource Default Schedule
     */
    boolean checkAvailability(
            final TemplateResource requestResource,
            final List<BookedResource> availableBookedResourceSlots,
            final List<TemplateResource> availableResourceSlots) {

        return searchAlgorithmService.checkAvailability(
                requestResource,
                availableBookedResourceSlots,
                availableResourceSlots);
    }

    int computePeriodOfDate(final Date date) {
        return searchAlgorithmService.computePeriodOfDate(date);
    }

    int computeLastPeriod(final Date date1, final Date date2) {

        return searchAlgorithmService.computeLastPeriod(date1, date2);
    }

    void adjustMapsForBookedResources(final Map<Integer, Integer> candidatePeriodToQtyMap,
                                      final Date reservedStartDate,
                                      final Date reservedEndDate,
                                      final Date candidateStartDate) {

        searchAlgorithmService.adjustMapsForBookedResources(
                candidatePeriodToQtyMap, reservedStartDate, reservedEndDate, candidateStartDate);
    }

    void loadIntoPeriodToQuantityMap(
            final int resourceQuantity,
            final int firstPeriod,
            final int lastPeriod, // inclusive
            final Map<Integer, Integer> periodToQuantityMap
    ) {

        searchAlgorithmService.loadIntoPeriodToQuantityMap(
                resourceQuantity,
                firstPeriod,
                lastPeriod,
                periodToQuantityMap);
    }

    // todo: unit test
    float computeTotalResources(final Date scheduledStartTime,
                                final Date scheduledEndTime,
                                final List<TimeBoundedIdentity> scheduleList,
                                final Map<Integer, Integer> periodToQuantityMap) {

        float totalResources = -1;

        // separate loop to compute periodToQuantity map
        int day = 0;
        for (final TimeBoundedIdentity schedule : scheduleList) {
            final Date startTimeDate = schedule.getStartTime();
            final Date endTimeDate = schedule.getEndTime();

            // 96 periods per day 12 am - 1145 pm
            final int firstPeriod = computePeriodOfDate(startTimeDate) + 96 * day;
            final int lastPeriod = computeLastPeriod(startTimeDate, endTimeDate) + 96 * day;

            loadIntoPeriodToQuantityMap(schedule.getQuantity(), firstPeriod, lastPeriod, periodToQuantityMap);
            day++;
        }

        final int firstPeriod = computePeriodOfDate(scheduledStartTime);
        final int lastPeriod = computeLastPeriod(scheduledStartTime, scheduledEndTime);
        for (int i = firstPeriod; i <= lastPeriod; i++) {
            final Integer currentQuantity = periodToQuantityMap.get(i);
            if ((currentQuantity == null || currentQuantity < 0) && totalResources == -1) {
                return -1; // no entry found
            } else if (currentQuantity == null || currentQuantity < 0 && totalResources != -1) {
                return totalResources; // no entry found after partial check
            } else if (currentQuantity != null && totalResources == -1) {
                totalResources = currentQuantity;
            } else if (currentQuantity != null && currentQuantity < totalResources) {
                totalResources = currentQuantity;
            }
        }
        return totalResources;
    }

    float retrieveTotalResourcesFromOverrideSchedule(final List<ResourceSchedule> scheduleListParam,
                                                     final Date scheduledStartTime,
                                                     final Date scheduledEndTime,
                                                     final Map<Integer, Integer> periodToQuantityMap) {

        final List<TimeBoundedIdentity> finalScheduleList = new ArrayList<TimeBoundedIdentity>();
        final List<TimeBoundedIdentity> scheduleList = ResourceSchedule.toTimeBoundedIdentityList(scheduleListParam);
        if (scheduleList != null) {
            finalScheduleList.addAll(scheduleList);
        }
        return computeTotalResources(scheduledStartTime, scheduledEndTime, finalScheduleList, periodToQuantityMap);
    }

    Date modifyDateFieldPlusAmtSetHourMinute(final Date date,
                                             final int field,
                                             final int amount,
                                             final int hour,
                                             final int minute) {

        return searchAlgorithmService.modifyDateFieldPlusAmtSetHourMinute(
                                                    date, field, amount, hour, minute);
    }

    Date beginningOfDayPlus(final Date date, final int deltaDays) {
        final LocalDateTime ldt = dateToLocalDateTime(date);

        final LocalDateTime cleared = ldt.withHour(0).withMinute(0).withSecond(0).withNano(0);

        final LocalDateTime adjusted = cleared.plusDays(deltaDays);

        return toDate(adjusted);
    }

    Date beginningOfDay(final Date date) {
        return beginningOfDayPlus(date, 0);
    }

    int findEarliestInpatientStartTime(final List<TemplateResource> resources) {
        int earliestStartTimeInMin = 0;
        int currentStartTimeInMin = 0;
        for (final TemplateResource r : resources) {
            if (r.getStartMinutes() != null) {
                currentStartTimeInMin = r.getStartMinutes();
            }
            if (earliestStartTimeInMin == 0 || currentStartTimeInMin == 0) {
                earliestStartTimeInMin = 0;
            } else if (earliestStartTimeInMin > 0 && currentStartTimeInMin < earliestStartTimeInMin) {
                earliestStartTimeInMin = currentStartTimeInMin;
            }
        }
        return earliestStartTimeInMin;
    }

    int findEarliestInpatientClockStartTime(final List<TemplateResource> resources) {
        return searchAlgorithmService.findEarliestInpatientClockStartTime(resources);
    }

    int findLatestInpatientEndTime(final List<TemplateResource> resources) {
        return searchAlgorithmService.findLatestInpatientEndTime(resources);
    }

    // @Transactional
    // entry point from resource layer
    public List<BookedVisit> findCandidateVisits(
            final VisitSpecsDTO visitSpecsDTO,
            final UserSession userSession,
            final boolean confirmEvent,
            final boolean rejectedCheck,
            final boolean isInpatient) {

        return searchAlgorithmService.findCandidateVisits(
                visitSpecsDTO,
                userSession,
                confirmEvent,
                rejectedCheck,
                isInpatient
        );
    }

    /**
     * Loops through all the booked resources for a visit, testing whether there is a gender block for any of them
     *
     * @param subject
     * @param bookedResources
     * @return
     */
    String checkForGenderBlock(final Subject subject, List<BookedResource> bookedResources) {

        String genderBlockDetailsMessage = "";

        for (final BookedResource bookedResource : bookedResources) {
            if (isSharedRoom(bookedResource.getResource())) {
                genderBlockDetailsMessage += checkForGenderBlockInBookingsOfSharedResource(bookedResource, subject);
            }
        }

        // Here a bit of trickery: if no gender-block messages were accumulated, we return null.
        // Returning null makes the code in the caller more explicit:
        //  null means there is no block
        //  not null means there is at least one block
        return genderBlockDetailsMessage.length() == 0 ? null : genderBlockDetailsMessage;

    }


    /**
     * Checks whether the given resource booking overlaps with a booking of its shared resource
     *
     * @param newResourceBookingAttempt
     * @param subjectInNewResourceBooking
     * @return null if there are no conflicts (i.e. no overlapping booking, or no gender conflict)
     *         or an error message if there is a conflict (overlap with at least one booking of the
     *         shared resource, with incompatible gender)
     */
    String checkForGenderBlockInBookingsOfSharedResource(
            final BookedResource newResourceBookingAttempt,
            final Subject subjectInNewResourceBooking
    ) {

        // This variable will accumulate gender-block messages as they are generated while looping through
        // the existing bookings of the shared resource.
        String genderBlockDetailsMessage = "";

        // TODO-XH : look at apache utils
        final Resource sharedResource = resourceDAO.findResourceById(newResourceBookingAttempt.getResource().getSharedResource());
        final List<BookedResource> existingBookingsOfSharedResource = appointmentDAO.findBookedResources(sharedResource);

        // note: returned value of the call to appointmentDAO.findBookedResources() cannot be null
        // c.f. http://stackoverflow.com/questions/3599318/hibernate-query-list-method-is-returning-empty-list-instead-of-null-value

        // Loop through all the bookings of the shared resource
        for (final BookedResource existingBookingOfSharedResource : existingBookingsOfSharedResource) {

            // see if there is overlap between the new attempt to book the resource
            // and any existing booking for the shared resource
            if (newResourceBookingAttempt.overlapsWith(existingBookingOfSharedResource)) {
                final SubjectMrn subjectMrnInExistingBooking = existingBookingOfSharedResource.getBookedVisit().getSubjectMrn();
                // ignore hold booked-visits (i.e. no subject assigned yet), since there cannot be a gender conflict with an inexistent subject
                if (subjectMrnInExistingBooking != null) {
                    final Subject subjectInExistingBooking = subjectMrnInExistingBooking.getSubject();
                    final GenderType genderTypeOfSubjectInExistingBooking = subjectInExistingBooking.getGenderType();
                    final GenderType genderTypeOfSubjectInNewBooking = subjectInNewResourceBooking.getGenderType();
                    if (!genderTypeOfSubjectInNewBooking.equals(genderTypeOfSubjectInExistingBooking) ||
                        !subjectInExistingBooking.canShareResource()) {
                        genderBlockDetailsMessage += (
                                sharedResource.getName() +
                                " (Shared Private room) has been booked for a Subject whose Sex is " +
                                genderTypeOfSubjectInExistingBooking.getGenderName()
                        );
                    }
                }
            }
        }

        return genderBlockDetailsMessage;

    }

    boolean isSharedRoom(final Resource resource) {

        if (!resource.getResourceType().isRoom()) return false;
        Integer sharedResourceId = resource.getSharedResource();
        if (sharedResourceId == null || sharedResourceId.equals(0)) return false;
        if (resourceDAO.findResourceById(sharedResourceId) == null) return false;
        return true;

    }

    //@Transactional
    public List<LineLevelAnnotations> getResourceAnnotations(int resource) {
        Resource r = resourceDAO.findResourceById(resource);
        List<LineLevelAnnotations> rs = new ArrayList<LineLevelAnnotations>();
        List<ResourceAnnotation> ra = resourceDAO.findResourcesAnnotationsByResource(r);
        if (isNonNullNonEmpty(ra)) {
            for (int i = 0; i < ra.size(); i++) {
                rs.add(ra.get(i).getLineLevelAnnotations());
            }
        }
        Collections.sort(rs, new LineLevelAnnotations.AnnotationsComparator());
        return rs;
    }

    void computeAndSetDuration(final TemplateResource templateResource) {

        searchAlgorithmService.computeAndSetDuration(templateResource);
    }

    public BookedVisit rescheduleData(final VisitSpecsDTO visitSpecsDTO, final User user, final String ipAddress, final
    String templatePath, final Boolean followOriginalTemplate) {
        BookedVisit result;
        if (followOriginalTemplate != null && followOriginalTemplate) {
            result = rescheduleDataFromTemplate(visitSpecsDTO, user, ipAddress, templatePath);
        } else {
            result = rescheduleDataFromVisit(visitSpecsDTO, user, ipAddress);
        }
        return result;
    }

    public BookedVisit rescheduleDataFromTemplate(final VisitSpecsDTO visitSpecsDTO, final User user, final String ipAddress,
                                                  final String templatePath) {

        final BookedVisit bookedVisit = appointmentDAO.findBookedVisitById(visitSpecsDTO.getBookedvisit());
        final VisitTemplate visitTemplate = bookedVisit.getVisitTemplate();
        final Date eventDate = new Date(visitSpecsDTO.getStartDate());
        final AppointmentOverrideReason overrideReason = appointmentDAO.findAppointmentOverrideReasonById(visitSpecsDTO
                                                                                                                  .getOverrideReason());
        final TemplateResource templateResourceLowest = templateResourceDAO.findTemplateResourceLowest(visitTemplate);
        final Date lowestStartDate = templateResourceLowest.getStartDate();
        final Calendar scheduledEndTimeCal = updateDatesForBV(visitTemplate, eventDate, lowestStartDate);
        final BookedVisit clonedVisit = createClonedBookedVisit(user, bookedVisit, eventDate, scheduledEndTimeCal);
        final List<TemplateResource> templateResourceList =
                templateResourceDAO.findTemplateResourcesByVisit(visitTemplate);
        setRoomsForBVFromTemplate(clonedVisit, templateResourceList);
        createBookedVisitComments(visitSpecsDTO, user, ipAddress, clonedVisit);
        int eventDateMinutesDelta = 0;
        if (visitTemplate.getVisitType().isInpatient() && !visitTemplate.getRelativeTime()) {
            eventDate.setHours(lowestStartDate.getHours());
            eventDate.setMinutes(lowestStartDate.getMinutes());
            eventDate.setSeconds(0);
        } else {
            eventDateMinutesDelta = eventDate.getHours() * MINS_PER_HR + eventDate.getMinutes();
        }

        setupBookedResourcesForBookedVisit(user, templatePath, templateResourceList, eventDate, clonedVisit,
                                           eventDateMinutesDelta);
        auditService.logAppointmentActivity(ipAddress, clonedVisit, user, BookedVisitActivityLogStatics.RESCHEDULED, overrideReason);
        return clonedVisit;
    }

    protected void setRoomsForBVFromTemplate(final BookedVisit clonedVisit, final List<TemplateResource>
            templateResourceList) {
        // rooms field is transient, OK to mutate after createEntity()
        String rooms = " ";
        for (final TemplateResource templateResource : templateResourceList) {
            final Resource resource = templateResource.getResource();
            if (resource.getResourceType().getName().equalsIgnoreCase("Room")) {
                rooms = resource.getName() + ", ";
            }
        }
        clonedVisit.setRooms(rooms);
    }

    protected Calendar updateDatesForBV(final VisitTemplate visitTemplate, final Date eventDate, final Date
            lowestStartDate) {

        if (visitTemplate.getVisitType().isInpatient() && !visitTemplate.getRelativeTime()) {

            eventDate.setHours(lowestStartDate.getHours());
            eventDate.setMinutes(lowestStartDate.getMinutes());
            eventDate.setSeconds(0);
        }

        final Calendar scheduledEndTimeCal = Calendar.getInstance();
        scheduledEndTimeCal.clear();
        scheduledEndTimeCal.setTime(eventDate);
        scheduledEndTimeCal.add(Calendar.MINUTE, visitTemplate.getDuration());
        scheduledEndTimeCal.set(Calendar.SECOND, 0);
        scheduledEndTimeCal.set(Calendar.MILLISECOND, 0);
        return scheduledEndTimeCal;
    }

    BookedVisit tryCloneVisit(final BookedVisit bookedVisit) {
        return bookedVisit.cloneBookedVisit();
    }

    // @Transactional
    public BookedVisit rescheduleDataFromVisit(final VisitSpecsDTO visitSpecsDTO, final User user, final String ipAddress) {

        final BookedVisit bookedVisit = appointmentDAO.findBookedVisitById(visitSpecsDTO.getBookedvisit());
        final VisitTemplate visitTemplate = bookedVisit.getVisitTemplate();

        final Date eventDate = new Date(visitSpecsDTO.getStartDate());

        final AppointmentOverrideReason overrideReason = appointmentDAO.findAppointmentOverrideReasonById(visitSpecsDTO
                                                                                                                  .getOverrideReason());
        final BookedResource bookedResourceLowest = studyDAO.findBookedResourceLowest(bookedVisit);
        final List<BookedResource> bookedResourceList = appointmentDAO.findBookedResourcesByBookedVisit(bookedVisit);
        final Date earliestBookedResourceTime = bookedResourceLowest.getScheduledStartTime();
        final Date latestBookedResourceTime = appointmentDAO.findLatestBookedResourcesByBookedVisit(bookedVisit);
        final int visitDuration = (int) (
                latestBookedResourceTime.getTime() / MILLISECS_PER_MIN -
                earliestBookedResourceTime.getTime() / MILLISECS_PER_MIN
        );

        final VisitType visitType = visitTemplate.getVisitType();

        final Calendar scheduledEndTimeCal = createStartAndEndDateForBV(visitTemplate, eventDate,
                                                                        bookedResourceLowest, visitDuration, visitType);
        final Date originalVisitStartDate = bookedVisit.getScheduledStartTime();
        final BookedVisit clonedVisit = createClonedBookedVisit(user, bookedVisit, eventDate, scheduledEndTimeCal);
        setRoomsForBVFromBookedVisit(bookedResourceList, clonedVisit);
        createBookedVisitComments(visitSpecsDTO, user, ipAddress, clonedVisit);
        setupResourcesForRescheduledByVisit(bookedResourceList, clonedVisit, eventDate, originalVisitStartDate);

        auditService.logAppointmentActivity(ipAddress, clonedVisit, user, BookedVisitActivityLogStatics.RESCHEDULED, overrideReason);
        return clonedVisit;
    }

    private void setRoomsForBVFromBookedVisit(final List<BookedResource> bookedResourceList, final BookedVisit
            clonedVisit) {
        // rooms field is transient, OK to mutate after createEntity()
        String rooms = " ";
        for (final BookedResource bookedResource : bookedResourceList) {
            final Resource resource = bookedResource.getResource();
            if (resource.getResourceType().getName().equalsIgnoreCase("Room")) {
                rooms = resource.getName() + ", ";
            }
        }
        clonedVisit.setRooms(rooms);
    }

    private Calendar createStartAndEndDateForBV(final VisitTemplate visitTemplate, final Date eventDate, final
    BookedResource bookedResourceLowest, final int visitDuration, final VisitType visitType) {
        if (visitType.isInpatient() && !visitTemplate.getRelativeTime()) {
            final Date lowestStartDate = bookedResourceLowest.getScheduledStartTime();
            eventDate.setHours(lowestStartDate.getHours());
            eventDate.setMinutes(lowestStartDate.getMinutes());
            eventDate.setSeconds(0);
        }

        final Calendar scheduledEndTimeCal = Calendar.getInstance();
        scheduledEndTimeCal.clear();
        scheduledEndTimeCal.setTime(eventDate);
        scheduledEndTimeCal.add(Calendar.MINUTE, visitDuration);
        scheduledEndTimeCal.set(Calendar.SECOND, 0);
        scheduledEndTimeCal.set(Calendar.MILLISECOND, 0);
        return scheduledEndTimeCal;
    }

    protected void createBookedVisitComments(final VisitSpecsDTO visitSpecsDTO, final User user, final String ipAddress, final
    BookedVisit clonedVisit) {
        if (MiscUtil.isNonNullNonEmpty(visitSpecsDTO.getComment())) {
            final Comments comments = new Comments();
            comments.setComment(visitSpecsDTO.getComment());
            comments.setBookedVisit(clonedVisit);
            comments.setUser(user);
            comments.setDate(new Date());
            appointmentDAO.createEntity(comments);
            auditService.logAppointmentActivity(ipAddress, clonedVisit, user, BookedVisitActivityLogStatics.COMMENTED);
        }
    }

    protected BookedVisit createClonedBookedVisit(final User user, final BookedVisit bookedVisit, final Date eventDate,
                                                final Calendar scheduledEndTimeCal) {
        final BookedVisit clonedVisit = tryCloneVisit(bookedVisit);

        clonedVisit.setId(null);
        clonedVisit.setAppointmentStatusReason(null);
        clonedVisit.setCancelStatus(null);

        if (null == clonedVisit.getSubjectMrn()){
            clonedVisit.setAppointmentStatus(holdStatus());
        } else {
            clonedVisit.setAppointmentStatus(scheduledStatus());
        }

        clonedVisit.setScheduledStartTime(eventDate);
        clonedVisit.setScheduledEndTime(scheduledEndTimeCal.getTime());
        clonedVisit.setCancelDate(null);
        clonedVisit.setCancelUser(null);
        clonedVisit.setCancelStatusReason(null);
        clonedVisit.setComment("");
        clonedVisit.setSchedulingTime(new Date());
        clonedVisit.setSchedulingUser(user);
        clonedVisit.setSchedulingFlavor(BookedVisitActivityLogStatics.RESCHEDULED.getLogString());
        appointmentDAO.createEntity(clonedVisit);
        return clonedVisit;
    }

    void setupResourcesForRescheduledByVisit(final List<BookedResource> bookedResourceList, final BookedVisit
            clonedVisit, final Date eventDate, final Date originalVisitStartDate) {

        final List<BookedResource> clonedBookedResourceList = Lists.newArrayList();

        for (final BookedResource bookedResource : bookedResourceList) {

            final BookedResource clonedBookedResource = bookedResource.cloneBookedResource();

            clonedBookedResource.setId(null);
            clonedBookedResource.setBookedVisit(clonedVisit);
            clonedBookedResourceList.add(clonedBookedResource);

            final Integer deltaStartTime = (int) (
                    clonedBookedResource.getScheduledStartTime().getTime() - originalVisitStartDate.getTime()
            );

            final Date newStartTime = new Date(eventDate.getTime() + deltaStartTime);
            clonedBookedResource.setScheduledStartTime(newStartTime);

            long endDuration = clonedBookedResource.getDuration();
            long endDurationMillis = endDuration * 60 * 1000;
            long newEndTimeMillis = newStartTime.getTime();
            final Date newEndTime = new Date(newEndTimeMillis + endDurationMillis);
            clonedBookedResource.setScheduledEndTime(newEndTime);

            appointmentDAO.createEntity(clonedBookedResource);

            final List<OverrideBookedResourceAnnotations> obrasToClone = resourceDAO
                    .findOverrideBookedResourceAnnotationsByBookedResource(bookedResource);
            for (final OverrideBookedResourceAnnotations obraToClone : obrasToClone) {
                final OverrideBookedResourceAnnotations clonedObra = obraToClone.cloneObra();

                clonedObra.setBookedResource(clonedBookedResource);
                appointmentDAO.createEntity(clonedObra);
            }
        }
    }

    /**
     * *********************************************************OverBook
     * Visit/Resources
     ************************************************************/

    boolean overbookTimeWithin24Hours(final Date visitStartTime, final Date overbookTime) {
        final long diff = visitStartTime.getTime() - overbookTime.getTime();

        final long diffHours = diff / (60 * 60 * 1000);

        return diffHours <= 24;
    }

    void persistBookedResources(final List<BookedResource> resources, final BookedVisit bookedVisit) {
        if (isNullOrEmpty(resources)) {
            return;
        }
        for (final BookedResource resource : resources) {
            resource.setBookedVisit(bookedVisit);
            resource.setBillable(resource.getTemplateResource().getBillable());
            appointmentDAO.createEntity(resource);
            final List<TemplateResourceAnnotations> tr = templateResourceDAO
                    .findTemplateResourceAnnotationsByTemplateResource(resource.getTemplateResource());
            if (tr.size() > 0) {
                for (final TemplateResourceAnnotations tra : tr) {
                    final OverrideBookedResourceAnnotations bookedResourceAnnotations = new
                            OverrideBookedResourceAnnotations();
                    bookedResourceAnnotations.setBookedResource(resource);
                    bookedResourceAnnotations.setLineLevelAnnotations(tra.getLineLevelAnnotations());
                    bookedResourceAnnotations.setComment(tra.getComment());
                    bookedResourceAnnotations.setQuantity(tra.getQuantity());
                    appointmentDAO.createEntity(bookedResourceAnnotations);
                }
            }
        }
    }

    void persistVisit(final BookedVisit visit, final UserSession user, final String ipAddress) {
        // persist scheduling details
        visit.setSchedulingTime(new Date());
        visit.setSchedulingUser(user.getUser());
        visit.setSchedulingFlavor(BookedVisitActivityLogStatics.SCHEDULED.getLogString());

        appointmentDAO.createEntity(visit);

        auditService.logAppointmentActivity(ipAddress, visit, user.getUser(), BookedVisitActivityLogStatics.SCHEDULED);
    }

    // @Transactional
    public BookedVisit confirmOverbookRoomData(final VisitSpecsDTO visitSpecsDTO, final User user, final String ipAddress, final
    String templatePath) {
        final Date eventDate = new Date(visitSpecsDTO.getStartDate());
        final SubjectMrn selectedSubjectMrn = subjectDAO.findSubjectMrnById(visitSpecsDTO.getSubjectMrnId());
        final VisitTemplate selectedVisit = studyDAO.findVisitTemplateById(visitSpecsDTO.getVisit());

        final TemplateResource templateResourceLowest = templateResourceDAO.findTemplateResourceLowest(selectedVisit);
        int eventDateMinutesDelta = 0;

        if (selectedVisit.getVisitType().isInpatient() && !selectedVisit.getRelativeTime()) {
            eventDate.setHours(templateResourceLowest.getStartDate().getHours());
            eventDate.setMinutes(templateResourceLowest.getStartDate().getMinutes());
            eventDate.setSeconds(0);
        } else {
            // relative time visit templates (inpatient & outpatient)
            eventDateMinutesDelta = eventDate.getHours() * MINS_PER_HR + eventDate.getMinutes();
        }

        final AppointmentOverrideReason overrideReason = appointmentDAO.findAppointmentOverrideReasonById(visitSpecsDTO.getOverrideReason());

        final BookedVisit independentVisit = new BookedVisit();
        independentVisit.setAppointmentStatus(selectedSubjectMrn == null ? holdStatus() : scheduledStatus());
        independentVisit.setAppointmentStatusReason(null);
        independentVisit.setBookedResourceList(null);
        independentVisit.setCancelDate(null);
        independentVisit.setCancelUser(null);
        independentVisit.setCancelStatus(null);
        independentVisit.setCancelStatusReason(null);
        independentVisit.setCheckInDate(null);
        independentVisit.setCheckInUser(null);
        independentVisit.setCheckOutDate(null);
        independentVisit.setCheckOutUser(null);
        independentVisit.setCheckoutStatusReason(null);
        independentVisit.setName(selectedVisit.getName());
        independentVisit.setScheduledEndTime(eventDate);
        independentVisit.setScheduledStartTime(eventDate);
        independentVisit.setStudy(selectedVisit.getStudy());
        independentVisit.setSubjectMrn(selectedSubjectMrn);
        independentVisit.setVisitTemplate(selectedVisit);
        independentVisit.setVisitType(selectedVisit.getVisitType());
        independentVisit.setComment(visitSpecsDTO.getComment());

        appointmentDAO.createEntity(independentVisit);

        createCommentsRecordIfNonemptyComment(independentVisit, user, ipAddress);

        final List<TemplateResource> templateResourcesByVisit = templateResourceDAO.findTemplateResourcesByVisit(selectedVisit);

        final List<Resource> selectedResources = setupBookedResourcesForAppointmentWithVisitTime(visitSpecsDTO, user,
                                                                                                 templatePath,
                                                                                                 eventDate,
                                                                                                 independentVisit,
                                                                                                 templateResourcesByVisit, eventDateMinutesDelta);

        if (!selectedResources.isEmpty()) {
            final BookedVisitActivityLog bval = new BookedVisitActivityLog();
            bval.setActionPerformed(BookedVisitActivityLogStatics.OVERBOOKED.getLogString());
            bval.setBookedVisit(independentVisit);
            bval.setPerformingUser(user);
            bval.setDate(new Date());
            bval.setIpAddress(ipAddress);
            bval.setAppointmentOverrideReason(overrideReason);
            appointmentDAO.createEntity(bval);

            // persist scheduling details
            // maybe this should happen unconditionally, i.e. even if
            // selectedResources is empty?
            String rooms = " ";
            for (final Resource resource : selectedResources) {
                if (resource.getResourceType().getName().equalsIgnoreCase("Room")) {
                    rooms += resource.getName() + ", ";
                    independentVisit.setRooms(rooms);
                }
            }
            independentVisit.setSchedulingTime(new Date());
            independentVisit.setSchedulingUser(user);
            independentVisit.setSchedulingFlavor(BookedVisitActivityLogStatics.OVERBOOKED.getLogString());
            appointmentDAO.updateEntity(independentVisit);

            if (overbookTimeWithin24Hours(independentVisit.getScheduledStartTime(), Calendar.getInstance().getTime())) {
                final List<TemplateResource> trs = templateResourceDAO.findTemplateResourcesByVisit(selectedVisit);

                final List<String> conditions = new ArrayList<>();

                final boolean containsNursingResources = enrich(trs).exists(res -> res.getResource().getResourceType
                        ().isNursing());
                final boolean containsNutritionResources = enrich(trs).exists(res -> res.getResource()
                        .getResourceType().isNursing());
                final boolean containsLabResources = enrich(trs).exists(res -> res.getResource().getResourceType()
                        .isNursing());

                if (containsNursingResources) {
                    conditions.add("This visit contains Nursing resources. ");
                }

                if (containsNutritionResources) {
                    conditions.add("This visit contains Nutrition resources. ");
                }

                if (containsLabResources) {
                    conditions.add("This visit contains Lab resources. ");
                }

                sendOverBookedVisitEmail(independentVisit, user.getInstitution().getLongName(), templatePath,
                                         conditions);
            }
        }

        return independentVisit;
    }

    // @Transactional
    public BookedVisit overbookRoomData(final VisitSpecsDTO visitSpecsDTO) {
        final Date visitStartTime = new Date(visitSpecsDTO.getStartDate());
        final SubjectMrn selectedStudyMrn = subjectDAO.findSubjectMrnById(visitSpecsDTO.getSubjectMrnId());
        final VisitTemplate selectedVisitTemplate = studyDAO.findVisitTemplateById(visitSpecsDTO.getVisit());

        final TemplateResource templateResourceLowest = templateResourceDAO.findTemplateResourceLowest(selectedVisitTemplate);
        if (selectedVisitTemplate.getVisitType().isInpatient() && !selectedVisitTemplate.getRelativeTime()) {
            visitStartTime.setHours(templateResourceLowest.getStartDate().getHours());
            visitStartTime.setMinutes(templateResourceLowest.getStartDate().getMinutes());
            visitStartTime.setSeconds(0);
        }

        final AppointmentStatus status = scheduledStatus();
        final int minutes = visitStartTime.getHours() * MINS_PER_HR + visitStartTime.getMinutes();
        final int endMinutes = minutes + selectedVisitTemplate.getDuration();
        final int hours = MiscUtil.divideByMinsPerHour(endMinutes);
        final int min = MiscUtil.moduloMinsPerHour(endMinutes);
        final Calendar cal = Calendar.getInstance();
        cal.clear();
        cal.setTime(visitStartTime);
        cal.set(Calendar.HOUR_OF_DAY, hours);
        cal.set(Calendar.MINUTE, min);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

        final BookedVisit bookedVisit = new BookedVisit();
        bookedVisit.setAppointmentStatus(status);
        bookedVisit.setAppointmentStatusReason(null);
        bookedVisit.setBookedResourceList(null);
        bookedVisit.setCancelDate(null);
        bookedVisit.setCancelUser(null);
        bookedVisit.setCancelStatus(null);
        bookedVisit.setCancelStatusReason(null);
        bookedVisit.setCheckInDate(null);
        bookedVisit.setCheckInUser(null);
        bookedVisit.setCheckOutDate(null);
        bookedVisit.setCheckInUser(null);
        bookedVisit.setCheckoutStatusReason(null);

        // populate only via save button!
        bookedVisit.setComment("");

        bookedVisit.setName(selectedVisitTemplate.getName());
        bookedVisit.setScheduledEndTime(cal.getTime());
        bookedVisit.setScheduledStartTime(visitStartTime);
        bookedVisit.setStudy(selectedVisitTemplate.getStudy());
        bookedVisit.setSubjectMrn(selectedStudyMrn);
        bookedVisit.setVisitTemplate(selectedVisitTemplate);
        bookedVisit.setVisitType(selectedVisitTemplate.getVisitType());

        final List<TemplateResource> templateResourcesForSelectedVisit = templateResourceDAO.findTemplateResourcesByVisit
                (selectedVisitTemplate);
        final List<Resource> roomResources = resourceDAO.getRooms();

        if (visitSpecsDTO.getRoomSelected() == 0) {
            // set the resource start and end Times
            final TemplateResource templateResource = new TemplateResource();
            setupTemplateResource(templateResource, selectedVisitTemplate, bookedVisit);

            boolean isAvailable;
            // check for standard alternate resource availability
            for (final Resource alternateResource : roomResources) {
                templateResource.setResource(alternateResource);
                final TemplateResource altResource = createTempResourceSlot(alternateResource, templateResource);
                isAvailable = checkAvailability(altResource, bookedVisit.getBookedResourceList(),
                                                templateResourcesForSelectedVisit);
                if (isAvailable) {
                    templateResource.setResource(altResource.getResource());
                    bookedVisit.setSelectedRoom(altResource.getResource());
                    break;
                }
            }
        } else if (visitSpecsDTO.getRoomSelected() != 0) {
            final Resource room = resourceDAO.findResourceById(visitSpecsDTO.getRoomSelected());
            bookedVisit.setSelectedRoom(room);
        }

        setTemplateResourceTimes(visitStartTime, templateResourcesForSelectedVisit);
        return bookedVisit;
    }

    /**
     * Confirm and Book/Reserve the Visit Time Slot for the selected Subject.
     **/

    // @Transactional
    public ConfirmationStatus confirmEvent(final VisitSpecsDTO visitSpecsDTO, final UserSession user, final String ipAddress,
                                           final String institution, final String templatePath, final boolean
                                                   isInpatient) {
        /*
         * NB: Stopgap fix for HCCRCSCHEDULING-1565:
         */
        synchronized (confirmationLock) {
            final Date startDate = new Date(visitSpecsDTO.getStartDate());
            final Date endDate = new Date(visitSpecsDTO.getEndDate());
            if (timeSlotAvailable(visitSpecsDTO, user, isInpatient)) {

                if (visitSpecsDTO.getDoubleRoomMessage() == null) {
                    confirmVisitBooking(visitSpecsDTO, user, ipAddress, institution, templatePath, startDate, endDate);
                } else {
                    confirmVisitBookingAfterDoubleRoomMessage(visitSpecsDTO, user, ipAddress, institution, templatePath,
                                                              startDate, endDate);
                }

                return ConfirmationStatus.Confirmed;
            }

            return ConfirmationStatus.NotConfirmed;
        }
    }

    /******************************************************************************************************************************************************************/

    /*************************
     * Confirm an appointment after searching for the available appointments
     **********************************/

    boolean timeSlotAvailable(final VisitSpecsDTO visitSpecsDTO, final UserSession userSession, final boolean isInpatient) {
        return conflictChecker.timeSlotAvailable(visitSpecsDTO, userSession, isInpatient);
    }

    void confirmVisitBookingAfterDoubleRoomMessage(final VisitSpecsDTO visitSpecsDTO, final UserSession userSession, final
    String ipAddress, final String institution, final String templatePath, final Date startDate, final Date endDate) {
        appointmentConfirmer.confirmVisitBookingAfterDoubleRoomMessage(this, visitSpecsDTO, userSession, ipAddress,
                                                                       institution, templatePath, startDate, endDate);
    }

    void confirmVisitBooking(final VisitSpecsDTO visitSpecsDTO, final UserSession userSession, final String ipAddress, final
    String institution, final String templatePath, final Date startDate, final Date endDate) {
        appointmentConfirmer.confirmVisitBooking(this, visitSpecsDTO, userSession, ipAddress, institution, templatePath,
                                                 startDate, endDate);
    }

    void sendWeighedMealEmail(final BookedVisit resVisit, final BookedResource br, final String institution, final
    String templatePath) {
        final String title = "Weighed Meal booked for : " + br.getScheduledStartTime();
        final String visitName = resVisit.getName();
        final String studyName = resVisit.getStudy().getName();
        final String localId = resVisit.getStudy().getLocalId();
        final String catalystId = resVisit.getStudy().getCatalystId();
        final String irb = resVisit.getStudy().getIrb();
        String investigator = null;
        if (resVisit.getStudy().getInvestigator() != null) {
            investigator = resVisit.getStudy().getInvestigator().getFirstName() + ' ' +
                           resVisit.getStudy().getInvestigator().getLastName();
        }
        final String startTime = resVisit.getScheduledStartTime().toString();
        final String endTime = resVisit.getScheduledEndTime().toString();
        final StringTemplateGroup group = new StringTemplateGroup("underwebinf", templatePath, DefaultTemplateLexer
                .class);
        final StringTemplate meal = group.getInstanceOf("weighedMealEmail");
        meal.setAttribute("institution", institution);
        meal.setAttribute("visitName", visitName);
        meal.setAttribute("studyName", studyName);
        meal.setAttribute("investigator", investigator);
        meal.setAttribute("startTime", startTime);
        meal.setAttribute("endTime", endTime);
        meal.setAttribute("localId", localId);
        meal.setAttribute("catalystId", catalystId);
        meal.setAttribute("irb", irb);
        if (resVisit.getStudy().getProtocolNutritionist() == null) {
            final List<User> user = authDAO.findNutritionManagerUserByRole();
            for (final User us : user) {
                mailHandler.sendOptionalEmails(new MailMessageBuilder().to(us.getPreferredNotificationEmail())
                                                       .subject(title).text(meal.toString()).build());
            }
        } else if (resVisit.getStudy().getProtocolNutritionist() != null) {
            mailHandler.sendOptionalEmails(new MailMessageBuilder().to(resVisit.getStudy().getProtocolNutritionist()
                                                                               .getPreferredNotificationEmail())
                                                   .subject(title).text(meal.toString()).build());
        }
    }

    BookedVisit createBookedVisit(final VisitSpecsDTO visitSpecsDTO,
                                  final UserSession userSession,
                                  final Date startDate,
                                  final Date endDate) {

        final Optional<SubjectMrn> selectedSubjectMrnOption =
                visitSpecsDTO.getSubjectMrnId() != 0
                        ? Optional.ofNullable(subjectDAO.findSubjectMrnById(visitSpecsDTO.getSubjectMrnId()))
                        : Optional.empty();

        final Optional<VisitTemplate> selectedVisitTemplateOption =
                Optional.ofNullable(studyDAO.findVisitTemplateById(visitSpecsDTO.getVisit()));

        final SubjectMrn subjectMrn = selectedSubjectMrnOption.orElse(null);
        final VisitTemplate visitTemplate = selectedVisitTemplateOption.orElse(null);
        final BookedVisit newVisit = new BookedVisit();
        final User user = userSession.getUser();
        newVisit.setVisitTemplate(visitTemplate);
        final Study study = studyDAO.findStudyById(visitSpecsDTO.getStudy());
        newVisit.setStudy(study);
        AppointmentStatus apptStatus = null;
        if (null != subjectMrn){
            apptStatus = scheduledStatus();
        } else {
            apptStatus = holdStatus();
        }
        newVisit.setAppointmentStatus(apptStatus);

        newVisit.setScheduledStartTime(startDate);
        newVisit.setScheduledEndTime(endDate);
        newVisit.setBookedResourceList(user.getBookedVisits().get(0).getBookedResourceList());

        if (newVisit.getBookedResourceList() == null) {
            newVisit.setBookedResourceList(Lists.<BookedResource>newArrayList());
        }
        newVisit.setSubjectMrn(subjectMrn);
        newVisit.setComment(visitSpecsDTO.getComment());
        newVisit.setName(visitTemplate.getName());
        newVisit.setVisitType(visitTemplate.getVisitType());
        newVisit.setAppointmentStatusReason(null);
        return newVisit;
    }

    Comments createCommentsRecordIfNonemptyComment(final BookedVisit bookedVisit, final User user, final String
            ipAddress) {

        Comments comments = null;
        final String commentString = bookedVisit.getComment();
        if (MiscUtil.isNonNullNonEmpty(commentString)) {
            comments = new Comments();
            comments.setComment(commentString);
            comments.setBookedVisit(bookedVisit);
            comments.setUser(user);
            comments.setDate(new Date());
            appointmentDAO.createEntity(comments);
            auditService.logAppointmentActivity(ipAddress, bookedVisit, user, BookedVisitActivityLogStatics.COMMENTED);
        }
        return comments;
    }

    // Calculate late cancellation
    VisitCancelStatus calculateVisitCancelStatus(final Date visitScheduledStartTime, final Date cancelStatusTime,
                                                 final int cancelTimeHr, final int cancelTimeMin) {
        final Calendar cal = Calendar.getInstance();
        if (cancelStatusTime != null) {
            cal.clear();
            cal.setTime(cancelStatusTime);
        }
        if (cancelTimeHr >= 0) {
            cal.set(Calendar.HOUR_OF_DAY, cancelTimeHr);
        }
        if (cancelTimeMin >= 0) {
            cal.set(Calendar.MINUTE, cancelTimeMin);
        }
        return calculateCancellationStatus(visitScheduledStartTime, cal.getTime());
    }

    VisitCancelStatus calculateCancellationStatus(final Date visitStartTime, final Date cancelTime) {
        VisitCancelStatus status;
        if (cancelTimeAfterScheduledTime(visitStartTime, cancelTime)) {
            status = new VisitCancelStatus(5, "No Show");
        } else {
            if (cancelTimeWithin48Hours(visitStartTime, cancelTime)) {
                status = new VisitCancelStatus(6, "Late Cancellation");
            } else {
                status = new VisitCancelStatus(7, "Cancellation");
            }
        }
        return status;
    }

    boolean cancelTimeWithin48Hours(final Date visitStartTime, final Date cancelTime) {
        final long diff = visitStartTime.getTime() - cancelTime.getTime();
        final long diffHours = diff / (60 * 60 * 1000);
        return diffHours > 0 && diffHours <= 48;
    }

    boolean cancelTimeAfterScheduledTime(final Date visitStartTime, final Date cancelTime) {
        final Calendar cal = Calendar.getInstance();
        cal.clear();
        cal.setTime(visitStartTime);
        cal.set(Calendar.MILLISECOND, 0);
        final Date visTime = cal.getTime();
        cal.clear();
        cal.setTime(cancelTime);
        cal.set(Calendar.MILLISECOND, 0);
        final Date curCancelTime = cal.getTime();
        return !curCancelTime.equals(visTime) && curCancelTime.after(visTime);
    }

    // @Transactional
    public Comments saveComment(final VisitSpecsDTO visitSpecsDTO, final User user, final String ipAddress) {
        final BookedVisit bv = appointmentDAO.findBookedVisitById(visitSpecsDTO.getId());
        bv.setComment(visitSpecsDTO.getComment());
        appointmentDAO.updateEntity(bv);
        return createCommentsRecordIfNonemptyComment(bv, user, ipAddress);
    }

    // @Transactional
    public BookedVisit checkInVisit(final VisitSpecsDTO visitSpecsDTO, final User user, final String ipAddress) {
        final BookedVisit bv = appointmentDAO.findBookedVisitById(visitSpecsDTO.getId());
        if (visitSpecsDTO.getCheckInDate() == 0) {
            bv.setCheckInDate(Calendar.getInstance().getTime());
        } else {
            bv.setCheckInDate(new Date(visitSpecsDTO.getCheckInDate()));
        }
        bv.setCheckInUser(user);
        bv.setAppointmentStatus(checkedInStatus());
        appointmentDAO.updateEntity(bv);
        auditService.logAppointmentActivity(ipAddress, bv, user, BookedVisitActivityLogStatics.CHECKED_IN);
        return bv;
    }

    // @Transactional
    public BookedVisit checkOutVisit(final VisitSpecsDTO visitSpecsDTO, final User user, final String ipAddress) {
        final BookedVisit bv = appointmentDAO.findBookedVisitById(visitSpecsDTO.getId());
        if (visitSpecsDTO.getCheckOutDate() == 0) {
            bv.setCheckOutDate(Calendar.getInstance().getTime());
        } else {
            bv.setCheckOutDate(new Date(visitSpecsDTO.getCheckOutDate()));
        }
        bv.setCheckOutUser(user);
        bv.setOmmittedActivities(visitSpecsDTO.isOmmittedActivities());
        bv.setVaryDuration(visitSpecsDTO.isVaryDuration());
        final AppointmentStatusReason reason = appointmentDAO.findAppointmentStatusReasonById(visitSpecsDTO.getAppointmentStatusReason());
        bv.setAppointmentStatusReason(reason);
        bv.setAppointmentStatus(checkedOutStatus());
        bv.setCheckoutStatusReason(reason);
        appointmentDAO.updateEntity(bv);
        final List<BookedVisit> visits = Lists.newArrayList();
        visits.add(bv);
        setRooms(visits);
        auditService.logAppointmentActivity(ipAddress, bv, user, BookedVisitActivityLogStatics.CHECKED_OUT);
        return bv;
    }

    // @Transactional
    public String batchEntryUpdate(
            final VisitSpecsDTO visitSpecsDTO,
            final User user,
            final String ipAddress,
            final String templatePath) {

        final BookedVisit bv = appointmentDAO.findBookedVisitById(visitSpecsDTO.getId());
        bv.setErrorMsg("true");

        if (visitSpecsDTO.getCheckOutDate() != 0) {
            setCheckOutDateAndUser(visitSpecsDTO, bv, user);
        }

        if (bv.getErrorMsg().equalsIgnoreCase("true")) {
            if (visitSpecsDTO.getCheckInDate() != 0) {
                setCheckInDateAndUser(visitSpecsDTO, bv, user);
            }

            if (visitSpecsDTO.getCheckoutStatusReason() != 0) {
                setCheckOutReason(visitSpecsDTO, bv);
            }
            else if (visitSpecsDTO.getCancelStatusReason() != 0) {
                setCancellationReasonAndUser(visitSpecsDTO, bv, user);
            }
            else {
                bv.setAppointmentStatus(checkedInStatus());
            }

            if (visitSpecsDTO.getCancelDate() != 0) {
                setLateCancellationCalculation(visitSpecsDTO, user, templatePath, bv);
            }

            appointmentDAO.updateEntity(bv);
            auditService.logAppointmentActivity(ipAddress, bv, user, BookedVisitActivityLogStatics.BATCH_ENTRY);
        }

        return bv.getErrorMsg();
    }

    void setLateCancellationCalculation(final VisitSpecsDTO visitSpecsDTO, final User user, final String templatePath, final
    BookedVisit bv) {
        final Date cancelStatusTime = new Date(visitSpecsDTO.getCancelDate());
        final Calendar cal = Calendar.getInstance();
        cal.clear();
        cal.setTime(cancelStatusTime);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        final VisitCancelStatus visitCancelStatus = calculateVisitCancelStatus(bv.getScheduledStartTime(),
                                                                               cancelStatusTime, 0, 0);
        final CancellationStatus cancelStatus = appointmentDAO.findCancellationStatusById(visitCancelStatus.getId());
        bv.setCancelStatus(cancelStatus);
        if (cancelStatus.getId().equals(6)) {
            sendLateCancellationEmail(bv, user.getInstitution().getLongName(), templatePath);
        }
    }

    void setCancellationReasonAndUser(final VisitSpecsDTO visitSpecsDTO, final BookedVisit bv, User user) {
        final AppointmentStatusReason reason = appointmentDAO.findAppointmentStatusReasonById(visitSpecsDTO.getCancelStatusReason
                ());
        bv.setCancelStatusReason(reason);
        bv.setAppointmentStatusReason(reason);
        if (visitSpecsDTO.getCancelDate() != 0) {
            bv.setCancelDate(new Date(visitSpecsDTO.getCancelDate()));
        } else {
            bv.setCancelDate(Calendar.getInstance().getTime());
        }
        bv.setCancelUser(user);
        bv.setAppointmentStatus(cancelledStatus());
        bv.setCheckInDate(null);
        bv.setCheckInUser(null);
        bv.setCheckoutStatusReason(null);
        bv.setCheckOutDate(null);
        bv.setCheckOutUser(null);
        bv.setCheckoutStatusReasonName(null);
    }

    void setCheckOutReason(final VisitSpecsDTO visitSpecsDTO, final BookedVisit bv) {
        final AppointmentStatusReason reason = appointmentDAO.findAppointmentStatusReasonById(visitSpecsDTO.getCheckoutStatusReason());
        bv.setCheckoutStatusReason(reason);
        bv.setAppointmentStatusReason(reason);
        bv.setAppointmentStatus(checkedOutStatus());
        bv.setCancelStatusReason(null);
        bv.setCancelDate(null);
        bv.setCancelUser(null);
    }

    void setCheckOutDateAndUser(final VisitSpecsDTO visitSpecsDTO, final BookedVisit bv, User user) {
        final Date checkout = new Date(visitSpecsDTO.getCheckOutDate());
        final Date checkInDate = new Date(visitSpecsDTO.getCheckInDate());
        final long checkIn = visitSpecsDTO.getCheckInDate();
        if (checkIn == 0 && bv.getCheckInDate().before(checkout)) {
            bv.setCheckOutDate(checkout);
            bv.setCheckOutUser(user);
        } else if (checkIn != 0 && checkInDate.before(checkout)) {
            bv.setCheckOutDate(checkout);
            bv.setCheckOutUser(user);
        } else {
            bv.setErrorMsg("Please Note: Check-Out Date needs to be greater than Check-In Date.");
        }
    }

    void setCheckInDateAndUser(final VisitSpecsDTO visitSpecsDTO, final BookedVisit bv, final User user) {
        bv.setCheckInDate(new Date(visitSpecsDTO.getCheckInDate()));
        bv.setCheckInUser(user);
        bv.setCancelDate(null);
        bv.setCancelUser(null);
        bv.setCancelStatusReason(null);
    }

    void sendLateCancellationEmail(final BookedVisit visit, final String institution, final String templatePath) {
        final String visitName = visit.getName();
        final String visitTypeName = visit.getVisitType().getName();
        final String sublocation = visit.getVisitTemplate().getSublocation().getName();
        final String localId = visit.getStudy().getLocalId();
        final String startTime = format(dateHourMin(), visit.getScheduledStartTime());
        final String cancellationTime = format(dateHourMin(), new Date());
        final Study study = visit.getStudy();
        final String title = "Urgent! Late Cancellation of " + visitName;
        final StringTemplateGroup group = new StringTemplateGroup("underwebinf", templatePath, DefaultTemplateLexer
                .class);
        final StringTemplate stringTemplate = group.getInstanceOf("lateCancellationEmail");
        stringTemplate.setAttribute("institution", institution);
        stringTemplate.setAttribute("visitName", visitName);
        stringTemplate.setAttribute("visitType", visitTypeName);
        stringTemplate.setAttribute("sublocation", sublocation);
        stringTemplate.setAttribute("localId", localId);
        stringTemplate.setAttribute("startTime", startTime);
        stringTemplate.setAttribute("cancellationTime", cancellationTime);
        sendEmailNotifications(study, title, stringTemplate);
    }

    void sendEmailNotifications(final Study study, final String title, final StringTemplate stringTemplate) {
        final List<User> studyTeam = new ArrayList<User>();
        if (study.getScheduler() != null) {
            studyTeam.add(study.getScheduler());
        }
        if (study.getInvestigator() != null) {
            studyTeam.add(study.getInvestigator());
        }
        if (study.getProtocolNurse() != null) {
            studyTeam.add(study.getProtocolNurse());
        }
        if (study.getProtocolNutritionist() != null) {
            studyTeam.add(study.getProtocolNutritionist());
        }
        if (study.getPhysician() != null) {
            studyTeam.add(study.getPhysician());
        }

        final List<User> nurseManager = authDAO.findNurseManagerUserByRole();
        final List<User> nutritionDirector = authDAO.findNutritionManagerUserByRole();
        final List<User> scheduler = authDAO.findSchedulerUserByInstitutionRole();
        final List<User> finalApprover = authDAO.findFinalApproverByRole();
        final List<User> adminDirector = authDAO.findAdminDirectorUserByRole();
        final List<User> crcAdmin = authDAO.findCRCAdminByRole();

        if (studyTeam.size() > 0 || !studyTeam.isEmpty()) {
            for (final User u : studyTeam) {
                mailHandler.sendOptionalEmails(new MailMessageBuilder().to(u.getPreferredNotificationEmail()).subject
                        (title).text(stringTemplate.toString()).build());
            }
        }
        if (nurseManager.size() > 0 || !nurseManager.isEmpty()) {
            for (final User u : nurseManager) {
                mailHandler.sendOptionalEmails(new MailMessageBuilder().to(u.getPreferredNotificationEmail()).subject
                        (title).text(stringTemplate.toString()).build());

            }
        }
        if (nutritionDirector.size() > 0 || !nutritionDirector.isEmpty()) {
            for (final User u : nutritionDirector) {
                mailHandler.sendOptionalEmails(new MailMessageBuilder().to(u.getPreferredNotificationEmail()).subject
                        (title).text(stringTemplate.toString()).build());
            }
        }
        if (scheduler.size() > 0 || !scheduler.isEmpty()) {
            for (final User u : scheduler) {
                mailHandler.sendOptionalEmails(new MailMessageBuilder().to(u.getPreferredNotificationEmail()).subject
                        (title).text(stringTemplate.toString()).build());
            }
        }
        if (finalApprover.size() > 0 || !finalApprover.isEmpty()) {
            for (final User u : finalApprover) {
                mailHandler.sendOptionalEmails(new MailMessageBuilder().to(u.getPreferredNotificationEmail()).subject
                        (title).text(stringTemplate.toString()).build());
            }
        }
        if (adminDirector.size() > 0 || !adminDirector.isEmpty()) {
            for (final User u : adminDirector) {
                mailHandler.sendOptionalEmails(new MailMessageBuilder().to(u.getPreferredNotificationEmail()).subject
                        (title).text(stringTemplate.toString()).build());
            }
        }
        if (crcAdmin.size() > 0 || !crcAdmin.isEmpty()) {
            for (final User u : adminDirector) {
                mailHandler.sendOptionalEmails(new MailMessageBuilder().to(u.getPreferredNotificationEmail()).subject
                        (title).text(stringTemplate.toString()).build());
            }
        }
    }

    // @Transactional
    public BookedVisit cancelVisit(final VisitSpecsDTO visitSpecsDTO, final User user, final String ipAddress, final String
            templatePath) {
        final BooleanResultDTO result = new BooleanResultDTO();
        result.setResult(false);
        final BookedVisit bv = appointmentDAO.findBookedVisitById(visitSpecsDTO.getId());
        final Calendar cal = Calendar.getInstance();
        final int cancelTimeHr = 0;
        final int cancelTimeMin = 0;
        if (visitSpecsDTO.getCancelDate() == 0) {
            bv.setCancelDate(Calendar.getInstance().getTime());
        } else {
            bv.setCancelDate(new Date(visitSpecsDTO.getCancelDate()));
        }
        bv.setCancelUser(user);
        final Date cancelStatusTime = bv.getCancelDate();
        cal.clear();
        cal.setTime(cancelStatusTime);
        cal.set(Calendar.HOUR_OF_DAY, cancelTimeHr);
        cal.set(Calendar.MINUTE, cancelTimeMin);

        final VisitCancelStatus visitCancelStatus = calculateVisitCancelStatus(bv.getScheduledStartTime(),
                                                                               cancelStatusTime, cancelTimeHr,
                                                                               cancelTimeMin);
        final CancellationStatus cancelStatus = appointmentDAO.findCancellationStatusById(visitCancelStatus.getId());
        bv.setCancelStatus(cancelStatus);
        if (cancelStatus.getId().equals(6)) {
            sendLateCancellationEmail(bv, user.getInstitution().getLongName(), templatePath);
        }
        final AppointmentStatusReason reason = appointmentDAO.findAppointmentStatusReasonById(visitSpecsDTO.getAppointmentStatusReason());
        bv.setAppointmentStatus(cancelledStatus());
        bv.setAppointmentStatusReason(reason);
        bv.setCancelStatusReason(reason);
        appointmentDAO.updateEntity(bv);
        final List<BookedVisit> visits = Lists.newArrayList();
        visits.add(bv);
        setRooms(visits);
        if (cancelTimeWithin48Hours(bv.getScheduledStartTime(), Calendar.getInstance().getTime())) {
            sendLateCancellationEmail(bv, user.getInstitution().getLongName(), templatePath);
        }
        auditService.logAppointmentActivity(ipAddress, bv, user, BookedVisitActivityLogStatics.CANCELLED);
        result.setResult(true);
        return bv;
    }

    // scheduled visits details
    BookedVisit getVisitDetails(final int bookedVisitId, final User user,
                                final String remoteHost, final boolean clicked) {
        final BookedVisit bookedVisit = appointmentDAO.findBookedVisitById(bookedVisitId);

        if (clicked) {
            String statusString;

            if (bookedVisit.getAppointmentStatus().getName().equalsIgnoreCase("cancellation")) {
                // For some reason 'cancellation' maps to 'cancelled'
                statusString = "cancelled";
            } else {
                // but the other statuses are good as is
                statusString = bookedVisit.getAppointmentStatus().getName();
            }
            auditService.logAppointmentActivity(remoteHost, bookedVisit, user,
                    BookedVisitActivityLogStatics.getActionForViewedResourcesListForAppointment(statusString));
        }

        bookedVisit.setScheduledata("");
        bookedVisit.setUserdata("");

        Date scheduleDate = null;
        String action = "";
        String performingUser = null;
        if (bookedVisit.getSchedulingFlavor() != null) {
            // NOTE: in the following code, we are checking if the scheduling flavor is either
            // overbooked or scheduled as stored in the database prior to and after version 3.3,
            // i.e. before and after creating BookedVisitActivityLogStatics
            if (
                    bookedVisit.getSchedulingFlavor().equalsIgnoreCase("OVERBOOK")
                    || bookedVisit.getSchedulingFlavor().equalsIgnoreCase(BookedVisitActivityLogStatics.OVERBOOKED.getLogString())
            ) {
                action = "Overbooked on: ";
            }
            else if (
                    bookedVisit.getSchedulingFlavor().equalsIgnoreCase("Scheduled Appointment")
                    || bookedVisit.getSchedulingFlavor().equalsIgnoreCase(BookedVisitActivityLogStatics.SCHEDULED.getLogString())
            ) {
                action = "Scheduled on: ";
            }
        }

        if (bookedVisit.getSchedulingTime() != null) {
            scheduleDate = bookedVisit.getSchedulingTime();
        }

        if (bookedVisit.getSchedulingUser() != null) {
            performingUser = bookedVisit.getSchedulingUser().getEcommonsId();
        }

        if (scheduleDate != null) {
            bookedVisit.setScheduleDate(scheduleDate);
            bookedVisit.setActionName(action);
            bookedVisit.setScheduledata(format(dateHourMin(), scheduleDate));
            bookedVisit.setUserdata("by user: " +
                    (
                        performingUser != null ?
                        performingUser :
                        BookedVisitActivityLogStatics.USER_DATA_NOT_AVAILABLE
                    )
            );
        }

        return bookedVisit;
    }

    // @Transactional
    public BookedVisit getVisitDetails(final int bookedVisitId, final User user, final String remoteHost) {

        final boolean permittedToGet = appointmentDAO.canUserSeeBookedVisit(user, bookedVisitId);
        if (!permittedToGet) {
            SchedulerRuntimeException.logAndThrow("User not allowed to get details for requested study");
        }

        // scheduled visits details via a click (AppointmentResource)
        return getVisitDetails(bookedVisitId, user, remoteHost, true);
    }


    public ScheduledVisitHistoryDTO getVisitHistory(final int bookedVisitId, final User user, final String remoteHost) {

        final BookedVisit bookedVisit = appointmentDAO.findBookedVisitById(bookedVisitId);

        String statusString;
        if (bookedVisit.getAppointmentStatus().getName().equalsIgnoreCase("cancellation")) {
            // For some reason 'cancellation' maps to 'cancelled'
            statusString = "cancelled";
        } else {
            // but the other statuses are good as is
            statusString = bookedVisit.getAppointmentStatus().getName();
        }
        auditService.logAppointmentActivity(remoteHost, bookedVisit, user,
                BookedVisitActivityLogStatics.getActionForViewedHistoryForAppointment(statusString));

        ScheduledVisitHistoryDTO scheduledVisitHistoryDTO = auditService.getActivityLogForBookedVisit(bookedVisitId);

        return scheduledVisitHistoryDTO;
    }


    // @Transactional
    public List<BookedResourcesResponse> getBookedResources(final int bookedVisitId, final String sortBy, final
    String orderBy, final int page, final int maxResults) {
        return appointmentDAO.getBookedResourcesListByBookedVisit(bookedVisitId, sortBy, orderBy, page, maxResults);
    }

    // @Transactional
    public List<BookedResourcesResponse> getEventResources(final String eventId, final User user) {
        List<BookedResource> resources;
        final List<BookedResourcesResponse> bookedResourcesResponses = new ArrayList<BookedResourcesResponse>();

        for (final BookedVisit bookedVisit : user.getBookedVisits()) {
            if (bookedVisit.getUniquekey().equalsIgnoreCase(eventId)) {
                resources = bookedVisit.getBookedResourceList();
                final boolean subjectAvailable = isSubjectAvailable(bookedVisit);
                if (resources != null) {
                    for (int i = 0; i < resources.size(); i++) {
                        resources.get(i).setId(i + 1000);
                    }
                    Collections.sort(resources, resourceTimeComparator);
                }

                for (final BookedResource resource : resources) {
                    final BookedResourcesResponse bookedResourcesResponse = new BookedResourcesResponse(resource.getId(), resource.getResource().getName(), "",
                            resource.getScheduledStartTime(), resource.getScheduledEndTime(), resource.getRejectedResourceMessage(), resource.getAvailable(),
                            resource.getTemplateResource().getResourceGroupType(), (long) resources.size(), subjectAvailable);
                    bookedResourcesResponses.add(bookedResourcesResponse);
                }
            }
        }
        return bookedResourcesResponses;
    }

    boolean isSubjectAvailable(BookedVisit bookedVisit) {

        return bookedVisit.getSubjectMrn() == null
                ? true
                : isSubjectAvailable(bookedVisit.getSubjectMrn().getSubject(),
                                        bookedVisit.getScheduledStartTime(),
                                        bookedVisit.getScheduledEndTime());
    }

    public boolean isSubjectAvailable(Integer subjectMrnId, Date startTime, Date endTime) {
        if (subjectMrnId == null || subjectMrnId == 0) { // hold appointment
            return true;
        }

        SubjectMrn subjectMrn = appointmentDAO.findById(SubjectMrn.class, subjectMrnId);
        Subject subject = subjectMrn.getSubject();

        return isSubjectAvailable(subject, startTime, endTime);
    }

    boolean isSubjectAvailable(Subject subject, Date startTime, Date endTime) {

        return !appointmentDAO.subjectHasBookedVisitInDateRange(subject.getId(), startTime, endTime);
    }

    // @Transactional
    public BooleanResultDTO deleteBookedResourceOverride(final VisitSpecsDTO tr, final User user, final String ipAddress) {

        final BookedResource br = appointmentDAO.findBookedResourceById(tr.getId());
        final BookedVisit bookedVisit = appointmentDAO.findBookedVisitById(br.getBookedVisit().getId());

        final BooleanResultDTO booleanResultDTO = new BooleanResultDTO();
        final long bookedResourcesCount = studyDAO.findBookedResourcesCount(bookedVisit);

        if (bookedResourcesCount <= 1) {
            booleanResultDTO.setResult(false);
            booleanResultDTO.setErrorMsg("You cannot delete this booked resource. A booked visit must always have at least one booked resource.");
        }
        else {
            final List<OverrideBookedResourceAnnotations> tempAnnotations = resourceDAO
                    .findOverrideBookedResourceAnnotationsByBookedResource(br);
            for (final OverrideBookedResourceAnnotations tempAnnotation : tempAnnotations) {
                appointmentDAO.deleteEntity(tempAnnotation);
            }

            final ActivityLog al = new ActivityLog();
            final AppointmentOverrideReason aor = appointmentDAO.findAppointmentOverrideReasonById(tr.getOverrideReason());
            al.setAppointmentOverrideReason(aor);
            al.setAffectedResource(br.getResource());
            al.setActionPerformed(aor.getName());
            al.setDate(new Date());
            al.setPerformingUser(user);
            al.setIpAddress(ipAddress);
            al.setBookedVisit(br.getBookedVisit());
            appointmentDAO.deleteEntity(br);

            final BookedResource bookedResourceLowest = studyDAO.findBookedResourceLowest(bookedVisit);
            final Date earliestBookedResourceTime = bookedResourceLowest.getScheduledStartTime();
            final Date latestBookedResourceTime = appointmentDAO.findLatestBookedResourcesByBookedVisit(bookedVisit);

            bookedVisit.setScheduledStartTime(earliestBookedResourceTime);
            bookedVisit.setScheduledEndTime(latestBookedResourceTime);
            appointmentDAO.updateEntity(bookedVisit);
            appointmentDAO.createEntity(al);

            booleanResultDTO.setResult(true);
        }

        return booleanResultDTO;

    }

    // @Transactional
    public BooleanResultDTO editBookedResourceOverride(final VisitSpecsDTO tr, final User user, final String ipAddress) {
        final BooleanResultDTO booleanResultDTO = new BooleanResultDTO();
        booleanResultDTO.setResult(false);
        final BookedResource br = appointmentDAO.findBookedResourceById(tr.getId());
        final Resource r = resourceDAO.findResourceById(tr.getResource());

        final ActivityLog al = new ActivityLog();
        final BookedVisit bookedVisit = appointmentDAO.findBookedVisitById(br.getBookedVisit().getId());
        final AppointmentOverrideReason aor = appointmentDAO.findAppointmentOverrideReasonById(tr.getOverrideReason());

        al.setAppointmentOverrideReason(aor);
        al.setActionPerformed(aor.getName());
        al.setAffectedResource(br.getResource());
        al.setDate(new Date());
        al.setPerformingUser(user);
        al.setIpAddress(ipAddress);
        al.setBookedVisit(bookedVisit);
        al.setChangesDetail(
                "Resource : " + br.getResource().getName() + " to " + r.getName() + ", " + " Start Time : " +
                br.getScheduledStartTime() + " to " + new Date(tr.getStartDate()) + ", " + " End Time : " +
                br.getScheduledEndTime() + " to " + new Date(tr.getEndDate()));

        br.setResource(r);
        br.setBillable(tr.isBillable());
        br.setScheduledStartTime(new Date(tr.getStartDate()));
        br.setScheduledEndTime(new Date(tr.getEndDate()));
        final long start = br.getScheduledStartTime().getTime() / MILLISECS_PER_MIN;
        final long end = br.getScheduledEndTime().getTime() / MILLISECS_PER_MIN;
        final int intStart = (int) start;
        final int intEnd = (int) end;
        final int duration = intEnd - intStart;
        br.setDuration(duration);
        appointmentDAO.updateEntity(br);

        final List<OverrideBookedResourceAnnotations> tempAnnotations = resourceDAO
                .findOverrideBookedResourceAnnotationsByBookedResource(br);
        for (final OverrideBookedResourceAnnotations tempAnnotation : tempAnnotations) {
            appointmentDAO.deleteEntity(tempAnnotation);
        }

        if (!tr.getSelectedAnnotations().isEmpty()) {
            for (int i = 0; i < tr.getSelectedAnnotations().size(); i++) {
                final LineLevelAnnotations lla = resourceDAO.findLineLevelAnnotationsById(tr.getSelectedAnnotations()
                                                                                                  .get(i));
                final OverrideBookedResourceAnnotations ann = new OverrideBookedResourceAnnotations();
                ann.setBookedResource(br);
                ann.setLineLevelAnnotations(lla);
                ann.setQuantity(tr.getSelectedAnnotationsQuantity().get(i));
                ann.setComment(tr.getSelectedAnnotationsComment().get(i));
                appointmentDAO.createEntity(ann);
            }
        }

        adjustAndPersistBookedVisit(bookedVisit, br);

        appointmentDAO.createEntity(al);

        booleanResultDTO.setResult(true);
        return booleanResultDTO;
    }

    // @Transactional
    public BooleanResultDTO addBookedResourceOverride(final VisitSpecsDTO visitSpecsDTO, final User user, final String ipAddress) {
        final BooleanResultDTO booleanResultDTO = new BooleanResultDTO();
        booleanResultDTO.setResult(false);
        final BookedVisit bookedVisit = appointmentDAO.findBookedVisitById(visitSpecsDTO.getId());
        final Resource r = resourceDAO.findResourceById(visitSpecsDTO.getResource());
        final BookedResource br = new BookedResource();
        br.setBookedVisit(bookedVisit);
        br.setResource(r);
        br.setBillable(visitSpecsDTO.isBillable());
        br.setScheduledStartTime(new Date(visitSpecsDTO.getStartDate()));
        br.setScheduledEndTime(new Date(visitSpecsDTO.getEndDate()));
        final long start = br.getScheduledStartTime().getTime() / MILLISECS_PER_MIN;
        final long end = br.getScheduledEndTime().getTime() / MILLISECS_PER_MIN;
        final int intStart = (int) start;
        final int intEnd = (int) end;
        final int duration = intEnd - intStart;
        br.setDuration(duration);
        appointmentDAO.createEntity(br);

        adjustAndPersistBookedVisit(bookedVisit, br);

        if (!visitSpecsDTO.getSelectedAnnotations().isEmpty()) {
            for (int i = 0; i < visitSpecsDTO.getSelectedAnnotations().size(); i++) {
                final OverrideBookedResourceAnnotations obra = new OverrideBookedResourceAnnotations();
                obra.setBookedResource(br);
                obra.setLineLevelAnnotations(resourceDAO.findLineLevelAnnotationsById(visitSpecsDTO.getSelectedAnnotations().get
                        (i)));
                obra.setQuantity(visitSpecsDTO.getSelectedAnnotationsQuantity().get(i));
                obra.setComment(visitSpecsDTO.getSelectedAnnotationsComment().get(i));
                appointmentDAO.createEntity(obra);
            }
        }

        final ActivityLog al = new ActivityLog();
        final AppointmentOverrideReason aor = appointmentDAO.findAppointmentOverrideReasonById(visitSpecsDTO.getOverrideReason());
        al.setAppointmentOverrideReason(aor);
        al.setActionPerformed(aor.getName());
        al.setDate(new Date());
        al.setPerformingUser(user);
        al.setIpAddress(ipAddress);
        al.setBookedVisit(bookedVisit);
        al.setAffectedResource(r);
        appointmentDAO.createEntity(al);

        booleanResultDTO.setResult(true);
        return booleanResultDTO;
    }

    // todo : unit test
    void adjustAndPersistBookedVisit(final BookedVisit bookedVisit, final BookedResource br) {
        final BookedResource bookedResourceHighest = appointmentDAO.findOrderedBookedResource(br.getBookedVisit(), "a.scheduledEndTime", "DESC");
        final BookedResource bookedResourceLowest = appointmentDAO.findOrderedBookedResource(br.getBookedVisit(), "a.scheduledStartTime", "ASC");
        bookedVisit.setScheduledEndTime(bookedResourceHighest.getScheduledEndTime());
        bookedVisit.setScheduledStartTime(bookedResourceLowest.getScheduledStartTime());
        appointmentDAO.updateEntity(bookedVisit);
    }

    // @Transactional
    public List<Resource> getResources() {
        return resourceDAO.findResourcesActiveInSublocations();
    }

    /*****************************
     * check for overbooked resources(resources conflict)
     ***********************************/

    // @Transactional
    public List<OverbookedResourcesResponse> selectedVisitForOverbookChecks(final int bookedVisitId,
                                                                            final String sortOn,
                                                                            final String sortBy) {

        final Map<String, List<BookedResource>> overBookedData = new HashMap<String, List<BookedResource>>();
        List<BookedResource> resources;
        float totalOverrideAvailable = 0;
        Map<Date, List<SublocationClosureInterval>> resourceSublocationSchedule = null;

        final BookedVisit visit = appointmentDAO.findBookedVisitById(bookedVisitId);
        final List<BookedResource> scheduledResources = appointmentDAO.findBookedResourcesByBookedVisit(visit);

        List<OverbookedResourcesResponse> overbookedResourcesResponseList = new ArrayList<OverbookedResourcesResponse>();

        for (final BookedResource scheduledResource : scheduledResources) {
            List<BookedResource> filteredScheduledResources = appointmentDAO
                    .findOverbookConflictResourcesByVisitStatus(scheduledResource.getResource(), scheduledResource
                            .getScheduledStartTime(), scheduledResource.getScheduledEndTime());

            if (filteredScheduledResources.isEmpty()) {
                filteredScheduledResources = Lists.newArrayList();
                filteredScheduledResources.add(scheduledResource);
            }

            // Find default Schedule of this resource
            // Retrieve Resource Default Schedule
            final Map<String, List<ResourceSchedule>> resourceDefaultSchedule = retrieveResourceDefaultSchedule
                    (scheduledResource.getResource(), scheduledResource.getScheduledStartTime(), scheduledResource
                            .getScheduledEndTime());

            resourceSublocationSchedule = null;
            // Retrieve Resource Override Schedule
            final Map<Date, List<ResourceSchedule>> resourceOverrideSchedule = retrieveResourceOverrideSchedule
                    (scheduledResource.getResource(), scheduledResource.getScheduledStartTime(), scheduledResource
                            .getScheduledEndTime());

            if (resourceDefaultSchedule == null && resourceOverrideSchedule == null) {
                if (overBookedData.containsKey(scheduledResource.getResource().getName())) {
                    resources = overBookedData.get(scheduledResource.getResource().getName());

                } else {
                    resources = new ArrayList<BookedResource>();
                }
                if (isNonNullNonEmpty(filteredScheduledResources)) {
                    resources.addAll(filteredScheduledResources);
                }

                overBookedData.put(scheduledResource.getResource().getName(), resources);
            }
            totalOverrideAvailable = totalOverrideResourceAvailable(scheduledResource, resourceOverrideSchedule);
            // Retrieve Resource Sublocation Closure Schedule

            final ResourceSublocation resourceSublocation = studyDAO.findSublocationByResource(scheduledResource.getResource());
            if (resourceSublocation != null) {
                resourceSublocationSchedule = retrieveSublocationSchedule(resourceSublocation.getSublocation(), scheduledResource
                        .getScheduledStartTime(), scheduledResource.getScheduledEndTime());
            }

            if (!resourceSublocationSchedule.isEmpty()) {
                if (overBookedData.containsKey(scheduledResource.getResource().getName())) {
                    resources = overBookedData.get(scheduledResource.getResource().getName());

                } else {
                    resources = new ArrayList<BookedResource>();
                }
                if (isNonNullNonEmpty(filteredScheduledResources)) {
                    resources.addAll(filteredScheduledResources);
                }

                overBookedData.put(scheduledResource.getResource().getName(), resources);
            } else if (totalOverrideAvailable == 0) {
                if (overBookedData.containsKey(scheduledResource.getResource().getName())) {
                    resources = overBookedData.get(scheduledResource.getResource().getName());

                } else {
                    resources = new ArrayList<BookedResource>();
                }
                if (isNonNullNonEmpty(filteredScheduledResources)) {
                    resources.addAll(filteredScheduledResources);
                }

                overBookedData.put(scheduledResource.getResource().getName(), resources);
            } else {
                final Map<Integer, Integer> candidatePeriodToQtyMap = new TreeMap<Integer, Integer>();
                boolean isAvailable = true;

                for (final BookedResource reservedTime : filteredScheduledResources) {

                    final Date reservedStartDate = reservedTime.getScheduledStartTime();
                    final Date reservedEndDate = reservedTime.getScheduledEndTime();

                    final Date candidateStartDate = scheduledResource.getScheduledStartTime();

                    adjustMapsForBookedResources(candidatePeriodToQtyMap, reservedStartDate, reservedEndDate,
                                                 candidateStartDate);
                }
                final Date candidateStartDate = scheduledResource.getScheduledStartTime();
                final Date candidateEndDate = scheduledResource.getScheduledEndTime();

                final int firstPeriod = computePeriodOfDate(candidateStartDate);
                final int lastPeriod = computeLastPeriod(candidateStartDate, candidateEndDate);

                for (int i = firstPeriod; i <= lastPeriod; i++) {
                    final Integer currentQuantity = candidatePeriodToQtyMap.get(i);
                    if (currentQuantity == null || currentQuantity < 0) {
                        isAvailable = false;
                        break;
                    }
                }

                if (!isAvailable) {
                    if (overBookedData.containsKey(scheduledResource.getResource().getName())) {
                        resources = overBookedData.get(scheduledResource.getResource().getName());

                    } else {
                        resources = new ArrayList<BookedResource>();
                    }
                    if (isNonNullNonEmpty(filteredScheduledResources)) {
                        resources.addAll(filteredScheduledResources);
                    }

                    overBookedData.put(scheduledResource.getResource().getName(), resources);
                }
            }
        }

        if (overBookedData != null) {
            for (final Map.Entry<String, List<BookedResource>> entry : overBookedData.entrySet()) {
                resources = entry.getValue();
                if (resources == null || resources.isEmpty()) {
                    continue;
                }
                for (final BookedResource r : resources) {
                    String conflictedTimePeriod = "";
                    if (!resourceSublocationSchedule.isEmpty()) {
                        r.setConflictedTime("Sublocation Closed.");
                    } else if (totalOverrideAvailable == 0) {
                        r.setConflictedTime("Resource temporarily adjusted.");
                    } else {
                        if (resources.size() > 1) {
                            r.setConflictedTime(getConflictedTimePeriod(resources));
                        } else {
                            final Date end1 = resources.get(0).getScheduledEndTime();
                            final Date start1 = resources.get(0).getScheduledStartTime();

                            conflictedTimePeriod = adjustConflictingTimePeriods(conflictedTimePeriod, start1, end1);

                            r.setConflictedTime(conflictedTimePeriod);
                        }
                    }
                    final OverbookedResourcesResponse overbookedResourcesResponse = new OverbookedResourcesResponse
                            (r, resources.size());

                    overbookedResourcesResponseList.add(overbookedResourcesResponse);
                }
            }
        }

        Collections.sort(overbookedResourcesResponseList, OverbookedResourcesResponse.getComparator(sortOn, sortBy));

        return overbookedResourcesResponseList;
    }

    Map<Date, List<SublocationClosureInterval>> retrieveSublocationSchedule(final Sublocation sublocations, final
    Date startTime, final Date endTime) {

        final Map<Date, List<SublocationClosureInterval>> dayOfWeekSchedule = new HashMap<Date,
                List<SublocationClosureInterval>>();
        final List<SublocationClosureInterval> sublocationScheduleList = appointmentDAO.findSublocationSchedule
                (sublocations, startTime, endTime);

        if (!sublocationScheduleList.isEmpty()) {
            populateSublocationClosureIntervalScheduleByDate(sublocationScheduleList, dayOfWeekSchedule);
        }
        return dayOfWeekSchedule;
    }

    Map<String, List<ResourceSchedule>> retrieveResourceDefaultSchedule(
            final Resource resource,
            final Date startDate,
            final Date endDate) {

        return searchAlgorithmService.retrieveResourceDefaultSchedule(resource, startDate, endDate);
    }

    List<Date> buildSearchDate(final Date startDate, final Date endDate) {
        final List<Date> searchDates = new ArrayList<Date>();
        final Calendar cal = Calendar.getInstance();
        Date curDate;
        final int diff = compareDateDifference(startDate, endDate);

        if (diff == 0) {
            curDate = modifyDateFieldPlusAmtSetHourMinute(startDate, Calendar.DAY_OF_YEAR, 0, 0, 0);
            cal.clear();
            cal.setTime(curDate);
            searchDates.add(curDate);
            return searchDates;
        }
        for (int count = 0; count < diff; count++) {
            curDate = modifyDateFieldPlusAmtSetHourMinute(startDate, Calendar.DAY_OF_YEAR, count, 0, 0);
            cal.clear();
            cal.setTime(curDate);
            searchDates.add(curDate);
        }

        return searchDates;
    }

    float totalOverrideResourceAvailable(final BookedResource curSelectedResource, final Map<Date,
            List<ResourceSchedule>> resourceOverrideSchedule) {

        if (curSelectedResource == null || resourceOverrideSchedule == null) {
            return 0;
        }

        float totalAvailable = -1;

        final Date requestStartDate = beginningOfDay(curSelectedResource.getScheduledStartTime());

        final Date requestEndDate = beginningOfDay(curSelectedResource.getScheduledEndTime());

        final List<Date> searchDates = buildSearchDate(requestStartDate, requestEndDate);
        List<ResourceSchedule> overrideSchedule = null;
        List<ResourceSchedule> tempOverrideSchedule;

        if (isNonNullNonEmpty(searchDates)) {
            overrideSchedule = Lists.newArrayList();

            for (final Date date : searchDates) {
                tempOverrideSchedule = resourceOverrideSchedule.get(date);
                final boolean exists = resourceOverrideSchedule.containsKey(date);
                if (exists) {
                    overrideSchedule.addAll(tempOverrideSchedule);
                }
            }
        }

        if (overrideSchedule == null) {
            return -1;
        }

        final Map<Integer, Integer> candidatePeriodToQtyMap = new TreeMap<Integer, Integer>();

        if (isNonNullNonEmpty(overrideSchedule)) {
            totalAvailable = retrieveTotalResourcesFromOverrideSchedule(overrideSchedule, curSelectedResource
                    .getScheduledStartTime(), curSelectedResource.getScheduledEndTime(), candidatePeriodToQtyMap);
        }

        if (totalAvailable == -1) {
            return -1;
        } else if (totalAvailable == 0) {
            return 0;
        }
        /*
         * Now check if count of Booked Resources booked for the same time as
         * the selected Resource is greater than the total count available from
         * Default and Override schedules
         */
        return totalAvailable;
    }

    String getConflictedTimePeriod(final List<BookedResource> resources) {
        String conflictedTimePeriod = "";
        final int i = 0;
        final Date start1 = resources.get(i).getScheduledStartTime();
        final Date end1 = resources.get(i).getScheduledEndTime();
        final Date start2 = resources.get(i + 1).getScheduledStartTime();
        final Date end2 = resources.get(i + 1).getScheduledEndTime();
        if (start1.after(start2) && end2.after(end1)) {
            conflictedTimePeriod = adjustConflictingTimePeriods(conflictedTimePeriod, start1, end1);
        } else if (start2.after(start1) && end1.after(end2)) {
            conflictedTimePeriod = adjustConflictingTimePeriods(conflictedTimePeriod, start2, end2);
        } else if (start1.before(start2) && end1.before(end2)) {
            conflictedTimePeriod = adjustConflictingTimePeriods(conflictedTimePeriod, start2, end1);
        } else if (start1.after(start2) && end1.after(end2)) {
            conflictedTimePeriod = adjustConflictingTimePeriods(conflictedTimePeriod, start1, end2);
        } else if (start1.after(start2) && end2.after(end1)) {
            conflictedTimePeriod = adjustConflictingTimePeriods(conflictedTimePeriod, start1, end2);
        } else if (start2.after(start1) && end1.after(end2)) {
            conflictedTimePeriod = adjustConflictingTimePeriods(conflictedTimePeriod, start2, end1);
        } else if (start1.equals(start2) && end2.equals(end1)) {
            conflictedTimePeriod = adjustConflictingTimePeriods(conflictedTimePeriod, start1, end1);
        } else if (start1.before(start2) && end2.equals(end1)) {
            conflictedTimePeriod = adjustConflictingTimePeriods(conflictedTimePeriod, start2, end1);
        } else if (start2.before(start1) && end2.equals(end1)) {
            conflictedTimePeriod = adjustConflictingTimePeriods(conflictedTimePeriod, start1, end1);
        } else if (start2.equals(start1) && end2.after(end1)) {
            conflictedTimePeriod = adjustConflictingTimePeriods(conflictedTimePeriod, start1, end1);
        } else if (start2.equals(start1) && end1.after(end2)) {
            conflictedTimePeriod = adjustConflictingTimePeriods(conflictedTimePeriod, start1, end2);
        }
        return conflictedTimePeriod;

    }

    // todo: unit test
    String adjustConflictingTimePeriods(final String conflictedTimePeriod, final Date start1, final Date end1) {
        final Calendar cal = Calendar.getInstance();
        cal.clear();
        cal.setTime(start1);
        final String startTimeDate = format(monthDayYear(), cal.getTime());
        String conflictedStartTimePeriod = conflictedTimePeriod + startTimeDate + " ";

        final int hr = cal.get(Calendar.HOUR_OF_DAY);
        final int min = cal.get(Calendar.MINUTE);

        conflictedStartTimePeriod += padTime(hr) + ":";
        conflictedStartTimePeriod += padTime(min) + " to ";

        String conflictedEndTimePeriod = conflictedStartTimePeriod;
        int hr1;
        int min1;
        cal.clear();
        cal.setTime(end1);
        final String endTimeDate = format(monthDayYear(), cal.getTime());

        conflictedEndTimePeriod += " " + endTimeDate + " ";

        min1 = cal.get(Calendar.MINUTE);
        hr1 = cal.get(Calendar.HOUR_OF_DAY);

        conflictedEndTimePeriod += padTime(hr1) + ":";
        conflictedEndTimePeriod += padTime(min1);

        return conflictedEndTimePeriod;
    }

    // @Transactional
    public void logViewVisits(final User user, final String ipAddress, final String action) {
        auditService.logUserActivity(ipAddress, null, user, action, null, null);
    }

    // @Transactional
    public List<TemplateResource> getRoomResources(final int visitId) {
        final VisitTemplate v = studyDAO.findVisitTemplateById(visitId);
        final List<TemplateResource> trs = templateResourceDAO.findRoomTemplateResourcesByVisit(v);

        return trs.isEmpty() ? null : trs;
    }

    // @Transactional
    public List<OverbookTimelineDataResponseDTO> getOverbookTimelineData(final Date selectedStartDate, final Date
            selectedEndDate, final int resourceTypeId, final List<Integer> sublocations, final String orderBy, final
    User user) {
        final ResourceType resourceType = ResourceType.findById(resourceTypeId).get();

        return appointmentDAO.getOverbookTimelineData(selectedStartDate, selectedEndDate, resourceType, sublocations,
                                                      orderBy, user);
    }

    // todo: unit test
    void checkMealsAndPersistVisit(final UserSession userSession, final String ipAddress, final String institution,
                                   final String templatePath, final BookedVisit resVisit) {
        for (final BookedResource br : resVisit.getBookedResourceList()) {
            br.setBookedVisit(resVisit);

            if (br.getResource().getName().contains("Meal, Weighed/Controlled")) {
                sendWeighedMealEmail(resVisit, br, institution, templatePath);
            }
        }

        final List<BookedResource> resources = resVisit.getBookedResourceList();

        persistVisit(resVisit, userSession, ipAddress);
        persistBookedResources(resources, resVisit);
        createCommentsRecordIfNonemptyComment(resVisit, userSession.getUser(), ipAddress);
    }

    List<Resource> setupBookedResourcesForAppointmentWithVisitTime(final VisitSpecsDTO visitSpecsDTO, final User user, final
    String templatePath, final Date eventDate, final BookedVisit bookedVisit, final List<TemplateResource>
            templateResourceList, final int eventDateMinutesDelta) {

        final List<Resource> accumulatedResourceList = Lists.newArrayList();

        for (final TemplateResource templateResource : templateResourceList) {
            setupOneBookedResourceForAppointment(visitSpecsDTO, user, templatePath, accumulatedResourceList, bookedVisit,
                                                 templateResource, eventDateMinutesDelta, eventDate);
        }

        return accumulatedResourceList;
    }

    void setupOneBookedResourceForAppointment(final VisitSpecsDTO visitSpecsDTO, final User user, final String templatePath,
                                              final List<Resource> accumulatedResourceList, final BookedVisit
                                                      bookedVisit, final TemplateResource templateResource, final int
                                                      eventDateMinutesDelta, final Date eventStartDate) {

        final Date trStartDate = templateResource.getStartDate();
        final Date trEndDate = templateResource.getEndDate();

        final Calendar startCal = getDateAdjustedForEvent(eventStartDate, trStartDate, eventDateMinutesDelta);
        final Calendar endCal = getDateAdjustedForEvent(eventStartDate, trEndDate, eventDateMinutesDelta);

        final Resource resource = probablyAccumulateResult(visitSpecsDTO, accumulatedResourceList, templateResource);

        final Integer duration = (int) ((endCal.getTimeInMillis() - startCal.getTimeInMillis()) / MILLISECS_PER_MIN);

        final BookedResource bookedResource = new BookedResource();
        bookedResource.setBookedVisit(bookedVisit);
        bookedResource.setDuration(duration);
        bookedResource.setScheduledStartTime(startCal.getTime());
        bookedResource.setScheduledEndTime(endCal.getTime());
        bookedResource.setResource(resource);
        bookedResource.setTemplateResource(templateResource);
        bookedResource.setBillable(templateResource.getBillable());
        appointmentDAO.createEntity(bookedResource);

        if (bookedVisit.getScheduledEndTime().before(bookedResource.getScheduledEndTime())) {
            bookedVisit.setScheduledEndTime(bookedResource.getScheduledEndTime());
        }

        persistAnnotationsForBookedResource(user, templatePath, bookedVisit, templateResource, bookedResource);
    }

    void setupBookedResourcesForBookedVisit(final User user, final String templatePath, final List<TemplateResource>
            templateResourceList, final Date eventDate, final BookedVisit bookedVisit, final int
            eventDateMinutesDelta) {

        final LazyList<Pair<TemplateResource, BookedResource>> bookedResources = lazy(templateResourceList).map
                (templateResource -> {
            final Date trStartDate = templateResource.getStartDate();
            final Date trEndDate = templateResource.getEndDate();

            final Calendar startCal = getDateAdjustedForEvent(eventDate, trStartDate, eventDateMinutesDelta);

            final Calendar endCal = getDateAdjustedForEvent(eventDate, trEndDate, eventDateMinutesDelta);

            final Integer duration = (int) (
                    (endCal.getTimeInMillis() - startCal.getTimeInMillis()) / MILLISECS_PER_MIN
            );

            final BookedResource newBookedResource = new BookedResource();
            newBookedResource.setResource(templateResource.getResource());
            newBookedResource.setId(null);
            newBookedResource.setTemplateResource(templateResource);
            newBookedResource.setBookedVisit(bookedVisit);
            newBookedResource.setDuration(duration);
            newBookedResource.setBillable(templateResource.getBillable());
            newBookedResource.setScheduledStartTime(startCal.getTime());
            newBookedResource.setScheduledEndTime(endCal.getTime());

            return pair(templateResource, newBookedResource);
        });

        final Consumer<Pair<TemplateResource, BookedResource>> justCreate = pair -> appointmentDAO.createEntity(pair.second);

        final Consumer<Pair<TemplateResource, BookedResource>> persistAnnotations = pair ->
                persistAnnotationsForBookedResource(user, templatePath, bookedVisit, pair.first, pair.second);

        final Consumer<Pair<TemplateResource, BookedResource>> persist = justCreate.andThen(persistAnnotations);

        bookedResources.forEach(persist);
    }

    Resource probablyAccumulateResult(final VisitSpecsDTO visitSpecsDTO, final List<Resource> accumulatedResourceList, final
    TemplateResource templateResource) {

        final Resource templateResourcesResource = resourceDAO.findResourceById(templateResource.getResource().getId());

        Resource resultResource = templateResourcesResource;

        if (templateResourcesResource.getResourceType().getName().equalsIgnoreCase("ROOM")) {
            if (visitSpecsDTO.getRoomSelected() != 0) {
                final Resource selectedRoomResource = resourceDAO.findResourceById(visitSpecsDTO.getRoomSelected());

                accumulatedResourceList.add(selectedRoomResource);

                resultResource = selectedRoomResource;
            }
        } else {
            accumulatedResourceList.add(templateResourcesResource);
        }

        return resultResource;
    }

    // todo: unit test
    void persistAnnotationsForBookedResource(final User user, final String templatePath, final BookedVisit
            bookedVisit, final TemplateResource templateResource, final BookedResource bookedResource) {

        final List<TemplateResourceAnnotations> la;

        if (templateResource != null) {
            la = templateResourceDAO.findTemplateResourceAnnotationsByTemplateResource(templateResource);
        } else {
            la = Collections.emptyList();
        }

        enrich(la).map(templateResourceAnnotations -> {
            final OverrideBookedResourceAnnotations obra = new OverrideBookedResourceAnnotations();

            obra.setBookedResource(bookedResource);
            obra.setLineLevelAnnotations(templateResourceAnnotations.getLineLevelAnnotations());
            obra.setQuantity(templateResourceAnnotations.getQuantity());
            obra.setComment(templateResourceAnnotations.getComment());

            return obra;
        }).forEach(appointmentDAO::createEntity);

        if (bookedResource.getResource().getName().contains("Meal, Weighed/Controlled")) {
            sendWeighedMealEmail(bookedVisit, bookedResource, user.getInstitution().getLongName(), templatePath);
        }
    }

    void setTemplateResourceTimes(final Date eventStartDate, final List<TemplateResource> templateResourceList) {
        for (final TemplateResource templateResource : templateResourceList) {

            final Date trStartDate = templateResource.getStartDate();
            final Date trEndDate = templateResource.getEndDate();

            final int eventDateHoursAndMinutesDelta =
                    eventStartDate.getHours() * MINS_PER_HR + eventStartDate.getMinutes();

            final Calendar startCal = getDateAdjustedForEvent(eventStartDate, trStartDate,
                                                              eventDateHoursAndMinutesDelta);
            final Calendar endCal = getDateAdjustedForEvent(eventStartDate, trEndDate, eventDateHoursAndMinutesDelta);

            templateResource.setScheduledStartTime(startCal.getTime());
            templateResource.setScheduledEndTime(endCal.getTime());

            computeAndSetDuration(templateResource);
        }
    }

    Calendar getDateAdjustedForEvent(final Date eventStartDate, final Date templateResourceDate, final int
            resourceMinutesDelta) {

        final int resourceStartMinuteAdjusted =
                templateResourceDate.getHours() * MINS_PER_HR + templateResourceDate.getMinutes() +
                resourceMinutesDelta;

        final int resourceAdjustedStartHour = MiscUtil.divideByMinsPerHour(resourceStartMinuteAdjusted);
        final int resourceAdjustedStartMins = MiscUtil.moduloMinsPerHour(resourceStartMinuteAdjusted);

        final Calendar templateResourceTime = Calendar.getInstance();
        templateResourceTime.setTimeInMillis(templateResourceDate.getTime());
        templateResourceTime.set(Calendar.HOUR_OF_DAY, 0);
        templateResourceTime.set(Calendar.MINUTE, 0);
        templateResourceTime.set(Calendar.SECOND, 0);
        templateResourceTime.set(Calendar.MILLISECOND, 0);

        final long templateTime = templateResourceTime.getTimeInMillis();
        final long templateResourceStartTime = TEMPLATE_RESOURCE_CALENDAR_ORIGIN.getTimeInMillis();

        final long bookedResouceRelativeTime = templateTime - templateResourceStartTime;
        final long adjustedBookedResourceStartTime = eventStartDate.getTime() + bookedResouceRelativeTime;

        final long daylightSavingsCorrectedABRST = adjustedBookedResourceStartTime +
                Math.abs(TimeZone.getDefault().getOffset(eventStartDate.getTime())
                - TimeZone.getDefault().getOffset(adjustedBookedResourceStartTime));

        final Calendar bookedResourceTime = Calendar.getInstance();
        bookedResourceTime.setTimeInMillis(daylightSavingsCorrectedABRST);
        bookedResourceTime.set(Calendar.HOUR_OF_DAY, resourceAdjustedStartHour);
        bookedResourceTime.set(Calendar.MINUTE, resourceAdjustedStartMins);
        bookedResourceTime.set(Calendar.SECOND, 0);
        bookedResourceTime.set(Calendar.MILLISECOND, 0);

        return bookedResourceTime;
    }

    TemplateResource setupTemplateResource(final TemplateResource templateResource, final VisitTemplate
            visitTemplate, final BookedVisit bookedVisit) {

        templateResource.setId(123456);
        templateResource.setVisitTemplate(visitTemplate);

        templateResource.setStartMinutes(minutesSinceOrigin(bookedVisit.getScheduledStartTime()));
        templateResource.setEndMinutes(minutesSinceOrigin(bookedVisit.getScheduledEndTime()));

        // I think we are subtracting one day's worth of minutes to adjust
        // the days from 1-offset to zero-offset
        final Integer startTimeInteger = bookedVisit.getScheduledStartTime().getDay() * MINS_PER_DAY +
                                         bookedVisit.getScheduledStartTime().getHours() * MINS_PER_HR +
                                         bookedVisit.getScheduledStartTime().getMinutes() - MINS_PER_DAY;

        final Integer endTimeInteger = bookedVisit.getScheduledEndTime().getDay() * MINS_PER_DAY +
                                       bookedVisit.getScheduledEndTime().getHours() * MINS_PER_HR +
                                       bookedVisit.getScheduledEndTime().getMinutes() - MINS_PER_DAY;

        final Integer duration = endTimeInteger - startTimeInteger;
        templateResource.setDuration(duration);

        templateResource.setScheduledStartTime(bookedVisit.getScheduledStartTime());
        templateResource.setScheduledEndTime(bookedVisit.getScheduledEndTime());

        computeAndSetDuration(templateResource);

        return templateResource;
    }

    // @Transactional
    public BookedResource getBookedResourceData(final int id, final String remoteHost, final User user) {
        final BookedResource br = appointmentDAO.findBookedResourceById(id);
        auditService.logAppointmentOverrideActivity(remoteHost, br.getBookedVisit(), br.getResource(), user, "Edit - " +
                                                                                                             "Overriding an Appointment (Incomplete Action)", null, null);
        return br;
    }

    // @Transactional
    public List<VisitTemplatesResponse> getVisitsByStudy(
            final int studyId,
            final boolean active,
            final String sortBy,
            final String orderBy,
            final int page,
            final int maxResults,
            final SearchDTO searchDTO) {

        searchDTO.mapSearchItemKeyAndValue( "v.visitType", "v.visitType", SearchDTO.visitTypeValueMapper);

        return studyDAO.getStudyVisitsByStatus(studyId, active, sortBy, orderBy, page, maxResults, searchDTO);

    }

    // @Transactional
    public List<BookedVisitsResponse> getBookedVisitsList(
            final int userId,
            final SearchDTO searchDTO,
            final String sortBy,
            final String orderBy,
            final int page,
            final int maxResults,
            final String remoteHost,
            final Date fromDate,
            final Date toDate
    ) {

        List<BookedVisitsResponse> visits;
        final User user = authDAO.findUserById(userId);

        visits = getBookedVisitsAdjustedForUser(
                searchDTO, sortBy, orderBy, page, maxResults, user, fromDate, toDate);

        auditService.logViewActivity(remoteHost, user, " Appointment Calendar List View.");
        return visits;
    }

    List<BookedVisitsResponse> getBookedVisitsAdjustedForUser(
            final SearchDTO searchDTO,
            final String sortBy, final String orderBy, final
            int page, final int maxResults, final User user,
            final Date fromDate,
            final Date toDate
    ) {

        List<BookedVisitsResponse> visits = Lists.newArrayList();
        List<Study> studyList = null;

        if (user.isStudyStaff()) {
            // will never return null. at worst, empty list
            studyList = studyDAO.findStudyListByPerson(user);
        }

        // null ==> don't limit by study
        // empty ==> limit, and there are none, so don't bother to look for visits
        if (studyList == null || !studyList.isEmpty()) {
            searchDTO.mapSearchItemKeyAndValue( "s.firstName", "s.firstName", SubjectDataEncryptor.capitalizeAndEncrypt);
            searchDTO.mapSearchItemKeyAndValue( "s.lastName", "s.lastName", SubjectDataEncryptor.capitalizeAndEncrypt);
            searchDTO.mapSearchItemKeyAndValue( "sm.mrn", "sm.mrn", SubjectDataEncryptor.capitalizeAndEncrypt);
            visits = appointmentDAO.findBookedVisitsForStudyList(
                    studyList, sortBy, orderBy, page, maxResults,
                    fromDate, toDate, searchDTO);
        }

        return visits;
    }

    // @Transactional
    public BookedVisitDetailResponse getBookedVisitData(final int bookedVisitId) {
        final BookedVisit bookedVisit = appointmentDAO.findBookedVisitById(bookedVisitId);
        return new BookedVisitDetailResponse(bookedVisit);
    }

    // @Transactional
    public boolean isStudyMember(final User user, final int id) {
        final BookedVisit bookedVisit = appointmentDAO.findBookedVisitById(id);
        return studyDAO.isStudyByPersonAndStudy(user, bookedVisit.getStudy());
    }

    static final Comparator<BookedResource> resourceTimeComparator =
            SearchAlgorithmService.resourceTimeComparator;

    /**
     * @author clint
     * @date Sep 4, 2013
     */
    final ConflictChecker defaultConflictChecker = (candidateVisitSpecs, userSession, isInpatient) -> {
        // this lambda implements timeSlotAvailable()

        // TODO: TEST THIS
        final List<BookedVisit> candidateVisits = findCandidateVisits(candidateVisitSpecs, userSession, true, false,
                                                                            isInpatient);
        boolean resourcesAvailable = false;
        final BookedVisit bookedVisit = candidateVisits.get(0);
        final List<BookedResource> bookedResourceList = bookedVisit.getBookedResourceList();
        if (isNonNullNonEmpty(bookedResourceList)) {
            resourcesAvailable = true;
            for (final BookedResource br : bookedResourceList) {
                final Boolean alternateResourceUsed = br.getTemplateResource().getAlternateResourceUsed();

                if (alternateResourceUsed != null && alternateResourceUsed) {
                    candidateVisitSpecs.setAlternateResourceUsed(
                            "During the confirmation process, one or more of the resources you were trying to " +
                            "schedule became unavailable. " +
                            "Good news! A standard alternate was substituted and your appointment was confirmed. " +
                            "Please review the booked visit by clicking on the appointment block.");
                }
            }
        }
        return resourcesAvailable;
    };

    static final class DefaultAppointmentConfirmer implements AppointmentConfirmer {
        @Override
        public void confirmVisitBooking(final AppointmentService appointmentService,
                                        final VisitSpecsDTO visitSpecsDTO,
                                        final UserSession userSession,
                                        final String ipAddress,
                                        final String institution,
                                        final String templatePath,
                                        final Date startDate,
                                        final Date endDate) {

            // TODO: Should we fail fast if selectedSubjectOption or
            // selectedVisitOption is absent? Is defaulting to null ok?
            final BookedVisit bookedVisit =
                    appointmentService.createBookedVisit(visitSpecsDTO, userSession, startDate, endDate);

            /*
             * Double room check - Check if The double rooms (or semi-private
             * rooms) cannot be occupied by subjects of different genders. A
             * notification will remind the coordinator/scheduler of that
             * restriction if an automatic restriction could not be implemented.
             */

            //only check if there is a subject; isn't when it is a hold
            SubjectMrn bvSubjectMrn = bookedVisit.getSubjectMrn();
            String genderBlockMessage = null;

            if (null != bvSubjectMrn) {
                genderBlockMessage = appointmentService.checkForGenderBlock(bvSubjectMrn.getSubject(),
                                                                bookedVisit.getBookedResourceList());
            }

            if (genderBlockMessage == null) {
                appointmentService.checkMealsAndPersistVisit(userSession, ipAddress, institution,
                                                                templatePath, bookedVisit);
            } else {
                visitSpecsDTO.setDoubleRoomMessage(genderBlockMessage);
            }
        }

        @Override
        public void confirmVisitBookingAfterDoubleRoomMessage(final AppointmentService appointmentService,
                                                              final VisitSpecsDTO visitSpecsDTO,
                                                              final UserSession userSession,
                                                              final String ipAddress,
                                                              final String institution,
                                                              final String templatePath,
                                                              final Date startDate,
                                                              final Date endDate) {
            visitSpecsDTO.setDoubleRoomMessage(null);

            final BookedVisit resVisit = appointmentService.createBookedVisit(visitSpecsDTO, userSession,
                                                                                startDate, endDate);

            appointmentService.checkMealsAndPersistVisit(userSession, ipAddress, institution,
                                                            templatePath, resVisit);
        }
    }

    public SwitchSubjectResultDTO switchVisitSubject(Integer newSubjectMrnId, Integer visitId, boolean homeScreen, String className) {

        // IMPORTANT: we are not persisting any changes to the existing booked visit in the case where
        // there are warnings.
        // This is because if there is a gender block or double-booking, then we need to give the user the option
        // to not make the changes. If the user confirms they want to make the changes, then
        // the UI will make a call to a distinct REST method (confirmSwitchVisitSubject()) where changes
        // will get persisted
        BookedVisit visit = appointmentDAO.findBookedVisitById(visitId);


        if (newSubjectMrnId.equals(Integer.valueOf(0))) {
            return appointmentDAO.switchVisitSubject(newSubjectMrnId, visit, homeScreen, className);
        }
        else {
            SubjectMrn newSubjectMrn = subjectDAO.findSubjectMrnById(newSubjectMrnId);
            Subject newSubject = newSubjectMrn.getSubject();
            // check for gender block
            // before we can check for gender block we must populate the transient bookedResources in the DTO
            List<BookedResource> bookedResources = appointmentDAO.findBookedResourcesByBookedVisit(visit);
            boolean genderBlockWarning = checkForGenderBlock(newSubject, bookedResources) != null;
            boolean doubleBookingWarning = appointmentDAO.subjectHasBookedVisitInDateRange(
                            newSubject.getId(),
                            visit.getScheduledStartTime(),
                            visit.getScheduledEndTime()
                    );
            if (!genderBlockWarning && !doubleBookingWarning) {
                // if no gender block nor double-booking warnings, return a DTO that signifies that the subject switch occurred as expected
                return appointmentDAO.switchVisitSubject(newSubjectMrnId, visit, homeScreen, className);
            }
            else {
                // gender-block or double-booking warning: send back an un-successful DTO, and do not persist any changes to the booked visit
                VisitRenderSummaryDTO visitRenderSummaryDTO = new VisitRenderSummaryDTO(visit, className, homeScreen);
                return new SwitchSubjectResultDTO(
                        false,
                        newSubjectMrnId,
                        newSubject.getFullName(),
                        visitRenderSummaryDTO,
                        genderBlockWarning,
                        doubleBookingWarning
                );
            }

        }

    }

    public SwitchSubjectResultDTO confirmSwitchVisitSubject(Integer newSubjectMrnId, Integer visitId, boolean homeScreen, String className) {

        BookedVisit visit = appointmentDAO.findBookedVisitById(visitId);

        return appointmentDAO.switchVisitSubject(newSubjectMrnId, visit, homeScreen, className);

    }

    public String getRoomString(Integer visitId){
        return appointmentDAO.findRoomString(visitId);
    }

}
