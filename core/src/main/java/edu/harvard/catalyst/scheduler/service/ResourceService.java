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

import com.google.common.base.Strings;
import edu.harvard.catalyst.scheduler.core.SchedulerRuntimeException;
import edu.harvard.catalyst.scheduler.core.Statics;
import edu.harvard.catalyst.scheduler.dto.AssignResourceAlternativesDTO;
import edu.harvard.catalyst.scheduler.dto.BooleanResultDTO;
import edu.harvard.catalyst.scheduler.dto.SearchDTO;
import edu.harvard.catalyst.scheduler.dto.request.*;
import edu.harvard.catalyst.scheduler.dto.SublocationClosureIntervalDTO;
import edu.harvard.catalyst.scheduler.dto.response.*;
import edu.harvard.catalyst.scheduler.entity.*;
import edu.harvard.catalyst.scheduler.persistence.AppointmentDAO;
import edu.harvard.catalyst.scheduler.persistence.AuthDAO;
import edu.harvard.catalyst.scheduler.persistence.ResourceDAO;
import edu.harvard.catalyst.scheduler.persistence.TemplateResourceDAO;
import edu.harvard.catalyst.scheduler.util.MailHandler;
import edu.harvard.catalyst.scheduler.util.MailMessageBuilder;
import edu.harvard.catalyst.scheduler.util.MiscUtil;
import org.antlr.stringtemplate.StringTemplate;
import org.antlr.stringtemplate.StringTemplateGroup;
import org.antlr.stringtemplate.language.DefaultTemplateLexer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static edu.harvard.catalyst.scheduler.util.MiscUtil.appendIfDifferentStrings;
import static edu.harvard.catalyst.scheduler.util.DateUtility.dayHrMinSecFormat;
import static edu.harvard.catalyst.scheduler.util.MiscUtil.isNullOrEmpty;


@Component
public class ResourceService {

    private MailHandler mailHandler;
    private AuthDAO authDAO;
    private ResourceDAO resourceDAO;
    private AppointmentDAO appointmentDAO;
    private TemplateResourceDAO templateResourceDAO;
    private AuditService auditService;

    @Autowired
    public ResourceService(AuthDAO authDAO, ResourceDAO resourceDAO, AuditService auditService, MailHandler mailHandler, AppointmentDAO appointmentDAO, TemplateResourceDAO templateResourceDAO) {
        this.authDAO = authDAO;
        this.resourceDAO = resourceDAO;
        this.appointmentDAO = appointmentDAO;
        this.templateResourceDAO = templateResourceDAO;
        this.auditService = auditService;
        this.mailHandler = mailHandler;
    }

    //Don't use - Needed for spring security cglib proxying
    ResourceService() {
    }

    public CreateResourceResponse createResource(
            final CreateResourceRequestDTO createResourceRequestDTO,
            final User user,
            final String ipAddress) {

        ResourceType resourceType = null;
        String resourceName = createResourceRequestDTO.getName().trim();

        // create basis for response
        final CreateResourceResponse response = new CreateResourceResponse();

        // validation: resource name is non null
        if (isNullOrEmpty(resourceName)) {
            response.setResult(false);
            response.setErrorMsg("Resource name needs to be specified");
            return response;
        }

        // validation: resource name contains only the followong characters:
        // alpha numeric characters, plus "," , "-", "(", ")", and "/".
        //
        final Pattern p = Pattern.compile("[- a-zA-Z0-9,/\\(\\)&]+");
        final Matcher m = p.matcher(resourceName);
        if (!m.matches()) {
            response.setResult(false);
            response.setErrorMsg("Resource name must contain only the following characters: " +
            "a-z A-Z 0-9 , - ( ) / & and whitespace, and cannot be empty");
            return response;
        }

        // validation: resource type exists
        try {
            resourceType = ResourceType.valueOf(createResourceRequestDTO.getResourceType());
        }
        catch (IllegalArgumentException | NullPointerException e) {
            response.setResult(false);
            response.setErrorMsg("Invalid resource type specified");
            return response;
        }

        // validation: sublocation exists
        final Sublocation sublocation = resourceDAO.findSublocationById(createResourceRequestDTO.getSublocationId());
        if (sublocation == null) {
            response.setResult(false);
            response.setErrorMsg("Invalid sublocation specified");
            return response;
        }

        // validation: resource name is already taken
        resourceName = resourceName.concat(" - ").concat(sublocation.getName());
        if (resourceDAO.findResourceByName(resourceName) != null) {
            response.setResult(false);
            response.setErrorMsg("This resource name is already existing");
            return response;
        }

        // create resource
        final Resource resource = new Resource(); // Remove deprecated
        resource.setName(resourceName);
        resource.setResourceType(resourceType);
        response.setSublocationName(sublocation.getName());

        resourceDAO.createEntity(resource);
        response.setName(resourceName);
        response.setResourceType(resourceType.getName());
        final int newResourceId = resource.getId();

        auditService.logResourceActivity(ipAddress, resource, user, Statics.AUDIT_RESOURCE_CREATE, null, null);

        // set resource-sublocation relationship and set active flag
        if (resourceDAO.findResourceSublocation(newResourceId, sublocation.getId()) != null) {
            // should NEVER happen, but just to be safe
            response.setResult(false);
            response.setErrorMsg("There already is a sublocation associated with this resource");
            return response;
        }
        final ResourceSublocation resourceSublocation = new ResourceSublocation();
        resourceSublocation.setSublocation(sublocation);
        resourceSublocation.setResource(resource);
        resourceSublocation.setActive(createResourceRequestDTO.getActive());
        resourceDAO.createEntity(resourceSublocation);
        response.setSublocationId(createResourceRequestDTO.getSublocationId());
        response.setResourceId(resource.getId());

        auditService.logResourceActivity(ipAddress, resource, user, Statics.AUDIT_RESOURCE_SUBLOCATION_CREATE,
            "Active=" + resourceSublocation.isActive(), null);

        response.setResult(true);
        return response;
    }

    //@Transactional
    public List<LineLevelAnnotations> getResourceAnnotations(int resource) {
        Resource r = resourceDAO.findResourceById(resource);
        List<LineLevelAnnotations> rs = new ArrayList<>();
        List<ResourceAnnotation> ra = resourceDAO.findResourcesAnnotationsByResource(r);
        if (MiscUtil.isNonNullNonEmpty(ra)){
            for (int i = 0; i < ra.size(); i++){
                rs.add(ra.get(i).getLineLevelAnnotations());
            }
        }
        Collections.sort(rs, new LineLevelAnnotations.AnnotationsComparator());
        return rs;
    }

    //@Transactional
    public BooleanResultDTO addResourceAlternatives(AssignResourceAlternativesDTO dto, User user, String ipAddress) {
        BooleanResultDTO booleanResultDTO = new BooleanResultDTO();
        Resource resource = resourceDAO.findResourceById(dto.getResourceId());
        for(int i = 0; i < dto.getAlternatives().size(); i++) {
            Resource altResource = resourceDAO.findResourceById(dto.getAlternatives().get(i));
            ResourceAlternate ra = new ResourceAlternate();
            ra.setSourceResource(resource);
            ra.setAlternateResource(altResource);
            resourceDAO.createEntity(ra);
        }
        auditService.logResourceActivity(ipAddress, resource, user, Statics.AUDIT_RESOURCE_ALTERNATE_CREATE, null, null);
        booleanResultDTO.setResult(true);
        return booleanResultDTO;
    }


    //@Transactional
    public SublocationClosureInterval createSublocationClosureInterval(SublocationClosureIntervalDTO dto, User user, String ipAddress) {
        SublocationClosureInterval interval = new SublocationClosureInterval();
        interval.setStartTime(dto.getStartTime());
        interval.setEndTime(dto.getEndTime());
        interval.setReason(dto.getReason());
        interval.setSublocation(resourceDAO.findSublocationById(dto.getSublocationId()));
        resourceDAO.createEntity(interval);
        auditService.logResourceSublocationClosureActivity(ipAddress, interval.getSublocation(), user, Statics.AUDIT_RESOURCE_SUBLOCATION_CLOSURE_CREATE, null, null);
        return interval;
    }


    //@Transactional
    public List<SublocationClosureIntervalResponse> getSublocationClosureIntervals(String sortBy, String orderBy, int page, int maxResults) {
        return resourceDAO.getSublocationClosureInterval(sortBy, orderBy, page, maxResults);
    }


    public List<String> getAllResourceNames(User user, String ipAddress) {

        auditService.logViewActivity(ipAddress, user, "All Resource Names Viewed");

        return resourceDAO.getAllResourceNames();

    }


    //@Transactional
    public List<Resource> getResourcesActiveInSublocations(boolean annotations, String ipAddress, User user) {
        List<Resource> resourceList = resourceDAO.findResourcesActiveInSublocations();

        List<Resource> resourceCopies = new ArrayList<>();
        auditService.logViewActivity(ipAddress, user, "All Resources Viewed");

        for (Resource r: resourceList){
            Resource resourceCopy = new Resource(
                    r.getId(),
                    r.getName(),
                    r.getResourceType(),
                    r.getSharedResource(),
                    r.getSublocations(),
                    r.getAlternateResourceList(),
                    r.getSourceResourceList(),
                    r.getDefaultScheduleList(),
                    r.getOverrideScheduleList());

            resourceCopies.add(resourceCopy);
            if (annotations && resourceCopy.getResourceType().getName().equalsIgnoreCase("ROOM")){
                resourceCopy.setName("Room : "+ resourceCopy.getName());
            }
        }
        return resourceCopies;
    }

    //@Transactional
    public List<Resource> getRoomResources(String sublocation) {
        List<Resource> rs = resourceDAO.findRoomResourcesListedInResourceSublocation(sublocation);
        List<Resource> rooms = new ArrayList<>();
        for(Resource r : rs) {
            if((!r.getName().contains("Any Private Room")) && (!r.getName().contains("All Rooms")) && (!r.getName().contains("Feldberg Room"))) {
                rooms.add(r);
            }
        }
        return rooms;
    }

    //@Transactional
    public List<Resource> getNursingResources(String ipAddress, User user, String sublocation) {
        return resourceDAO.findNursingResourcesListedInResourceSublocation(sublocation);
    }

    //@Transactional
    public List<Resource> getNutritionResources(String ipAddress, User user, String sublocation) {
        return resourceDAO.findNutritionResourcesListedInResourceSublocation(sublocation);
    }

    //@Transactional
    public List<ResourcesResponse> getAlternateResources(int resourceId, String sortBy,
                                                         String orderBy, int page, int maxResults) {
        Resource resource = resourceDAO.findResourceById(resourceId);
        // find the list of *existing* alternatives
        List<ResourceAlternate> altResource = resourceDAO.findResourceAlternates(resource);

        List<Resource> altResourceList = new ArrayList<>();
        for (ResourceAlternate resourceAlt : altResource) {
            altResourceList.add(resourceAlt.getAlternateResource());
        }

        List<ResourcesResponse> resourceList = resourceDAO.findResourcesWithSublocationAndAlternates(resource, altResourceList, sortBy, orderBy, page, maxResults);

        return resourceList;
    }

    public List<Resource> getResourcesAvailableForGenderBlockRestriction(int resourceId) {

        return resourceDAO.getResourcesAvailableForGenderBlockRestriction(resourceId);

    }

    //@Transactional
    public List<ResourceScheduleResponse> getResourceSchedules(int resourceId, boolean isOverride, String sortBy,
                                                               String orderBy, int page, int maxResults) {
        Resource resource = resourceDAO.findResourceById(resourceId);
        if (isOverride) {
            return resourceDAO.findTemporarySchedulesByResource(resource, isOverride, sortBy, orderBy, page, maxResults);
        }
        return resourceDAO.findResourceSchedulesByResource(resource, isOverride, sortBy, orderBy, page, maxResults);
    }

    //@Transactional
    public ResourceScheduleResponse getResourceDefaultSchedule(int id) {
        return resourceDAO.resourceScheduleById(id);
    }

    //@Transactional
    public BooleanRequest addDefaultAvailability(ResourceScheduleRequest dto, User user, String ipAddress) {
        BooleanRequest booleanRequest = new BooleanRequest();
        Resource resource = resourceDAO.findResourceById(dto.getResourceId());

        for(int i = 0; i < dto.getDays().size(); i++) {
            ResourceSchedule rs = new ResourceSchedule();
            rs.setResource(resource);
            rs.setDayOfWeek(dto.getDays().get(i));
            rs.setStartTime(dto.getStartDate());
            rs.setEndTime(dto.getEndDate());
            rs.setQuantity(dto.getQuantity());
            rs.setOverride(dto.isOverride());
            resourceDAO.createEntity(rs);
        }
        auditService.logResourceActivity(ipAddress, resource, user, Statics.AUDIT_RESOURCE_DEFAULT_CREATE, null, null);
        booleanRequest.setResult(true);
        return booleanRequest;
    }


    //@Transactional
    public BooleanRequest updateDefaultAvailability(ResourceScheduleRequest dto, User user, String ipAddress, final String templatePath) {
        BooleanRequest booleanRequest = new BooleanRequest();
        ResourceSchedule rs = resourceDAO.findResourceScheduleById(dto.getId());

        int editDay = rs.getDayOfWeek();
        String editStart = rs.getStartTime().toString();
        String editEnd = rs.getEndTime().toString();
        String editQuantity = rs.getQuantity().toString();
        String previousData = deltaOfResourceAvailability(dto, rs);
        auditService.logResourceActivity(ipAddress, rs.getResource(), user, Statics.AUDIT_RESOURCE_DEFAULT_UPDATE, previousData, null);
        resourceDAO.updateEntity(rs);
        booleanRequest.setResult(true);
        sendDefaultAvailabilityChangeEmail(rs, editDay, editStart, editEnd, editQuantity, user.getInstitution().getLongName(), templatePath);
        return booleanRequest;
    }

    String deltaOfResourceAvailability(ResourceScheduleRequest dto, ResourceSchedule rs) {
        String previousData = "";
        if(!rs.getStartTime().equals(dto.getStartDate())) {
            previousData = previousData + " Start Time: " + dayHrMinSecFormat(rs.getStartTime()) +
                " to " + dayHrMinSecFormat(dto.getStartDate()) + ", ";
        }

        if(!rs.getEndTime().equals(dto.getEndDate())) {
            previousData = previousData + " End Time: " + dayHrMinSecFormat(rs.getEndTime()) +
                " to " + dayHrMinSecFormat(dto.getEndDate()) + ", ";
        }

        if(!rs.getQuantity().equals(dto.getQuantity())) {
          previousData = previousData + " Quantity: " + rs.getQuantity() + " to " + dto.getQuantity() + ", ";
        }

        if(dto.getDays() != null)
        {
            Integer newDayOfWeek = dto.getDays().get(0);
            if( ! newDayOfWeek.equals(rs.getDayOfWeek())) {
            previousData = previousData + " Day Of Week: " + rs.getDayOfWeek() + " to " + newDayOfWeek + ", ";
            }
            rs.setDayOfWeek(newDayOfWeek);
        }

        rs.setStartTime(dto.getStartDate());
        rs.setEndTime(dto.getEndDate());
        rs.setQuantity(dto.getQuantity());

        return previousData;
    }

    void sendDefaultAvailabilityChangeEmail(ResourceSchedule rs, int editDay, String editStart, String editEnd, String editQuantity, String institution, final String templatePath) {
        List<User> schedulers = authDAO.findSchedulerUserByInstitutionRole();
        for(User u : schedulers) {
            String to = u.getEmail();
            String day = returnDayofWeek(editDay);

            int days = rs.getDayOfWeek();
            String newDay = returnDayofWeek(days);

            String resourceName = rs.getResource().getName();

            String start = rs.getStartTime().getHours() + " : " + rs.getStartTime().getMinutes();
            String end = rs.getEndTime().getHours() + " : " + rs.getEndTime().getMinutes();
            String quantity = rs.getQuantity().toString();

            String title = "Change in the default availability of the resource.";

            StringTemplateGroup group = new StringTemplateGroup("underwebinf", templatePath, DefaultTemplateLexer.class);

            StringTemplate schedulerEmail = group.getInstanceOf("resourceChangeEmail");
            schedulerEmail.setAttribute("institution", institution);
            schedulerEmail.setAttribute("resourceName", resourceName);
            schedulerEmail.setAttribute("editday", day);
            schedulerEmail.setAttribute("editstart", editStart);
            schedulerEmail.setAttribute("editend", editEnd);
            schedulerEmail.setAttribute("editquantity", editQuantity);

            schedulerEmail.setAttribute("newday", newDay);
            schedulerEmail.setAttribute("newstart", start);
            schedulerEmail.setAttribute("newend", end);
            schedulerEmail.setAttribute("newquantity", quantity);

            if(u.getEmail() != null) {
                mailHandler.sendOptionalEmails(new MailMessageBuilder().to(to).subject(title).text(schedulerEmail.toString()).build());
            }
        }
    }

    public String returnDayofWeek(int editDay) {
        String day = null;
        try {
            day = Statics.DAYS_OF_WEEK[editDay - 1];
        }
        catch (ArrayIndexOutOfBoundsException e) {
            // all righty, stick to the default 'null'
            SchedulerRuntimeException.logDontThrow("Bad index for days-of-week: " + (editDay - 1), e);
        }
        return day;
    }

    //@Transactional
    public BooleanRequest addTemporaryAdjustment(ResourceScheduleRequest dto, User user, String ipAddress) {
        BooleanRequest booleanRequest = new BooleanRequest();
        Resource resource = resourceDAO.findResourceById(dto.getResourceId());
        ResourceSchedule rs = new ResourceSchedule();
        rs.setResource(resource);
        rs.setStartTime(dto.getStartDate());
        rs.setEndTime(dto.getEndDate());
        rs.setQuantity(dto.getQuantity());
        rs.setOverride(dto.isOverride());
        auditService.logResourceActivity(ipAddress, resource, user, Statics.AUDIT_RESOURCE_TEMPADJ_CREATE, null, null);
        resourceDAO.createEntity(rs);
        booleanRequest.setResult(true);
        return booleanRequest;
    }

    //@Transactional
    public BooleanRequest updateTemporaryAdjustment(ResourceScheduleRequest dto, User user, String ipAddress) {
        BooleanRequest booleanRequest = new BooleanRequest();
        ResourceSchedule rs = resourceDAO.findResourceScheduleById(dto.getId());
        String previousData = deltaOfResourceAvailability(dto, rs);
        rs.setOverride(dto.isOverride());
        auditService.logResourceActivity(ipAddress, rs.getResource(), user, Statics.AUDIT_RESOURCE_TEMPADJ_UPDATE, previousData, null);
        resourceDAO.updateEntity(rs);
        booleanRequest.setResult(true);
        return booleanRequest;
    }

    //@Transactional
    public BooleanRequest deleteTemporaryAdjustment(int id, User user, String ipAddress, final String templateContext) {
        BooleanRequest booleanRequest = new BooleanRequest();
        ResourceSchedule rs = resourceDAO.findResourceScheduleById(id);

        String editStart = rs.getStartTime().toString();
        String editEnd = rs.getEndTime().toString();
        auditService.logResourceActivity(ipAddress, rs.getResource(), user, Statics.AUDIT_RESOURCE_TEMPADJ_DELETE, null, null);
        resourceDAO.deleteEntity(rs);
        booleanRequest.setResult(true);
        sendRemoveOverrideScheduleMail(rs, editStart, editEnd, user.getInstitution().getLongName(), templateContext);
        return booleanRequest;
    }

    void sendRemoveOverrideScheduleMail(ResourceSchedule rs, String editStart, String editEnd, String institution, final String templatePath) {
        List<User> schedulers = authDAO.findSchedulerUserByInstitutionRole();
        for(User u : schedulers) {
            String to = u.getPreferredNotificationEmail();
            String resourceName = rs.getResource().getName();

            String start = rs.getStartTime().toString();
            String end = rs.getEndTime().toString();
            String quant = rs.getQuantity().toString();

            String title = "Change in the temporary adjustments of the resource.";

            StringTemplateGroup group = new StringTemplateGroup("underwebinf", templatePath, DefaultTemplateLexer.class);

            StringTemplate schedulerEmail = group.getInstanceOf("resourceTempChangeEmail");
            schedulerEmail.setAttribute("institution", institution);
            schedulerEmail.setAttribute("resourceName", resourceName);
            schedulerEmail.setAttribute("editstart", editStart);
            schedulerEmail.setAttribute("editend", editEnd);

            schedulerEmail.setAttribute("newstart", start);
            schedulerEmail.setAttribute("newend", end);
            schedulerEmail.setAttribute("newquantity", quant);

            if(u.getEmail() != null) {
                mailHandler.sendOptionalEmails(new MailMessageBuilder().to(to).subject(title).text(schedulerEmail.toString()).build());
            }
        }
    }

    //@Transactional
    public BooleanRequest deleteDefaultAvailability(int id, User user, String ipAddress) {
        BooleanRequest booleanRequest = new BooleanRequest();
        ResourceSchedule rs = resourceDAO.findResourceScheduleById(id);
        auditService.logResourceActivity(ipAddress, rs.getResource(), user, Statics.AUDIT_RESOURCE_DEFAULT_DELETE, null, null);
        resourceDAO.deleteEntity(rs);
        booleanRequest.setResult(true);
        return booleanRequest;
    }

    //@Transactional
    public BooleanResultDTO deleteSublocationClosureInterval(SublocationClosureIntervalDTO dto, User user, String ipAddress) {
        BooleanResultDTO booleanResultDTO = new BooleanResultDTO();
        SublocationClosureInterval interval = resourceDAO.findBySublocationClosureIntervalId(dto.getSublocationClosureIntervalId());
        auditService.logResourceSublocationClosureActivity(ipAddress, interval.getSublocation(), user, Statics.AUDIT_RESOURCE_SUBLOCATION_CLOSURE_DELETE, null, null);
        resourceDAO.deleteEntity(interval);
        booleanResultDTO.setResult(true);
        return booleanResultDTO;
    }

    //@Transactional
    public BooleanRequest deleteResourceAlternative(int resourceId, int alternateResourceId, User user, String ipAddress) {
        BooleanRequest booleanRequest = new BooleanRequest();
        Resource resource = resourceDAO.findResourceById(resourceId);
        List<ResourceAlternate> list = resourceDAO.findResourceAlternates(resource);
        for(ResourceAlternate ra : list) {
            if(ra.getId().intValue() == alternateResourceId) {
                resourceDAO.deleteEntity(ra);
            }
        }
        booleanRequest.setResult(true);
        auditService.logResourceActivity(ipAddress, resource, user, Statics.AUDIT_RESOURCE_ALTERNATE_DELETE, null, null);
        return booleanRequest;
    }

    //@Transactional
    public BooleanRequest deleteRestriction(int resourceId, User user, String ipAddress) {
        BooleanRequest booleanRequest = new BooleanRequest();
        Resource resource = resourceDAO.findResourceById(resourceId);
        Resource sharedResource = resourceDAO.findResourceById(resource.getSharedResource());
        resource.setSharedResource(null);
        resource.setSharedResourceNotes(null);
        sharedResource.setSharedResource(null);
        sharedResource.setSharedResourceNotes(null);
        resourceDAO.updateEntity(resource);
        resourceDAO.updateEntity(sharedResource);
        booleanRequest.setResult(true);
        auditService.logResourceActivity(ipAddress, resource, user, Statics.AUDIT_RESOURCE_SHARED_RESOURCE_DELETE, null, null);
        return booleanRequest;
    }

    //@Transactional
    public BooleanRequest addRestriction(AddOrModifyRestrictionRequestDTO requestDto, User user, String ipAddress) {
        BooleanRequest booleanRequest = new BooleanRequest();
        Resource resource = resourceDAO.findResourceById(requestDto.getResourceId());
        Resource sharedResource = resourceDAO.findResourceById(requestDto.getSharedResourceId());
        resource.setSharedResource(requestDto.getSharedResourceId());
        resource.setSharedResourceNotes(requestDto.getNotes());
        sharedResource.setSharedResource(requestDto.getResourceId());
        sharedResource.setSharedResourceNotes(requestDto.getNotes());
        resourceDAO.updateEntity(resource);
        resourceDAO.updateEntity(sharedResource);
        booleanRequest.setResult(true);
        auditService.logResourceActivity(ipAddress, resource, user, Statics.AUDIT_RESOURCE_SHARED_RESOURCE_ADD, null, null);
        return booleanRequest;
    }

    //@Transactional
    public BooleanRequest modifyRestriction(AddOrModifyRestrictionRequestDTO requestDto, User user, String ipAddress) {
        BooleanRequest booleanRequest = new BooleanRequest();
        Resource resource = resourceDAO.findResourceById(requestDto.getResourceId());
        Resource sharedResource = resourceDAO.findResourceById(resource.getSharedResource());
        resource.setSharedResourceNotes(requestDto.getNotes());
        sharedResource.setSharedResourceNotes(requestDto.getNotes());
        resourceDAO.updateEntity(resource);
        resourceDAO.updateEntity(sharedResource);
        booleanRequest.setResult(true);
        auditService.logResourceActivity(ipAddress, resource, user, Statics.AUDIT_RESOURCE_SHARED_RESOURCE_MODIFY, null, null);
        return booleanRequest;
    }

    //@Transactional
    public List<LineLevelAnnotations> getSelectedResourceAnnotations(String annotations, String mode, int templateResourceId) {
        List<LineLevelAnnotations> la = new ArrayList<>();
        List<String> items = Arrays.asList(annotations.split("\\s*,\\s*"));
        List<Integer> intList = new ArrayList<>();
        for(String s : items) {
            intList.add(Integer.valueOf(s));
        }

        for (int i = 0; i < intList.size(); i++){
            LineLevelAnnotations rs = resourceDAO.findLineLevelAnnotationsById(intList.get(i));
            la.add(rs);
            setLineLevelAnnotations(mode, templateResourceId, la, i);
        }
        return la;
    }

    private void setLineLevelAnnotations(String mode, int templateResourceId, List<LineLevelAnnotations> la, int i) {
        if (mode != null && mode.equalsIgnoreCase("edit")){
            templateResourceAnnotations(templateResourceId, la, i);
        } else if (mode != null && mode.equalsIgnoreCase("override_edit")){
            bookedResourceAnnotations(templateResourceId, la, i);
        } else if (mode != null && (mode.equalsIgnoreCase("new") || mode.equalsIgnoreCase("override_new"))){
            la.get(i).setQuantity(1);
        }
    }

    private void bookedResourceAnnotations(int templateResourceId, List<LineLevelAnnotations> la, int i) {
        BookedResource r = appointmentDAO.findBookedResourceById(templateResourceId);
        OverrideBookedResourceAnnotations rsa = resourceDAO.findBookedResourceAnnotationsByBookedResourceAndLineLevel(r, la.get(i));
        if (rsa != null){
            la.get(i).setQuantity(rsa.getQuantity());
            la.get(i).setComment(rsa.getComment());
            la.get(i).setResourceAnnotations(rsa.getId());
        }
        else {
            la.get(i).setQuantity(1);
        }
    }

    private void templateResourceAnnotations(int templateResourceId, List<LineLevelAnnotations> la, int i) {
        TemplateResource r = templateResourceDAO.findTemplateResourceById(templateResourceId);
        TemplateResourceAnnotations rsa = resourceDAO.findTemplateAnnotationsByTemplateResourceAndLineLevel(r, la.get(i));
        if (rsa != null){
            la.get(i).setQuantity(rsa.getQuantity());
            la.get(i).setComment(rsa.getComment());
            la.get(i).setResourceAnnotations(rsa.getId());
        }
        else {
            la.get(i).setQuantity(1);
        }
    }

    //@Transactional
    public List<LineLevelAnnotations> getBookedResourceAnnotations(int bookedResourceId) {
        BookedResource bookedResourceById = appointmentDAO.findBookedResourceById(bookedResourceId);
        List<OverrideBookedResourceAnnotations> rs = resourceDAO.findOverrideBookedResourceAnnotationsByBookedResource(bookedResourceById);
        List<LineLevelAnnotations> ls = getResourceAnnotations(bookedResourceById.getResource().getId());
        for (int i = 0; i < rs.size(); i++){
            int indexOf = ls.indexOf(rs.get(i).getLineLevelAnnotations());
            ls.get(indexOf).setSelected(true);
            ls.get(indexOf).setQuantity(rs.get(i).getQuantity());
            ls.get(indexOf).setComment(rs.get(i).getComment());
            ls.get(indexOf).setResourceAnnotations(rs.get(i).getId());
        }
        Collections.sort(ls, new LineLevelAnnotations.AnnotationsComparator());
        return ls;
    }

    //@Transactional
    public List<ResourcesResponse> getResourcesWithSublocation(String sortBy,
                                                               String orderBy,
                                                               String status,
                                                               int page,
                                                               int maxResults,
                                                               User user,
                                                               String ipAddress,
                                                               SearchDTO searchDTO) {
        auditService.logViewActivity(ipAddress, user, Statics.AUDIT_RESOURCE_VIEW);

        if (searchDTO != null) {
            searchDTO.mapSearchItemKeyAndValue("resourceName", "r.name", null);
            searchDTO.mapSearchItemKeyAndValue("resourceType", "r.resourceType", null);
            searchDTO.mapSearchItemKeyAndValue("resourceSublocation", "s.name", null);
        }

        return resourceDAO.findResourceListWithSublocation(sortBy, orderBy, status, page, maxResults, searchDTO);

    }

    //@Transactional
    public ResourcesResponse getResourceDetail(int resourceId, User user,
                                               String ipAddress) {
        Resource resource = resourceDAO.findResourceById(resourceId);
        auditService.logResourceActivity(ipAddress, resource, user, Statics.AUDIT_RESOURCE_VIEW, null, null);
        return resourceDAO.findResourceDetail(resource);
    }

    //@Transactional
    public List<ResourcesResponse> getResourceAlternates(int resourceId, String sortBy,
                                                         String orderBy, int page, int maxResults) {
        Resource resource = resourceDAO.findResourceById(resourceId);
        return resourceDAO.findResourceAlternatesByResource(resource, sortBy, orderBy, page, maxResults);
    }

    //@Transactional
    public StatusAndMessageResponseDTO updateResource(final ModifyResourceRequest modifyResourceRequest,
                                                      final User user, final String ipAddress) {

        final StatusAndMessageResponseDTO responseDto = new StatusAndMessageResponseDTO();
        final int resourceId = modifyResourceRequest.getResourceId();
        final int sublocationId = modifyResourceRequest.getSublocationId();
        final boolean activate = modifyResourceRequest.getActivate();

        // retrieve the resource, sublocation, and resource-sublocation
        final Resource resource = resourceDAO.findResourceById(resourceId);
        final Sublocation sublocation = resourceDAO.findSublocationById(sublocationId);
        final ResourceSublocation resourceSublocation = resourceDAO.findUniqueResourceSublocationByResource(resource);

        if (isNullOrEmpty(modifyResourceRequest.getResourceName())) {
            responseDto.setSuccessful(false);
            responseDto.setMessage("You must specify a name for this resource.");
            return responseDto;
        }

        final String resourceName = modifyResourceRequest.getResourceName().trim().concat(" - ").concat(sublocation.getName());
        final Resource resourceSameName = resourceDAO.findResourceByName(resourceName);

        if (resourceSameName != null) {
            // If the resource's name is equal to its own name, then it's not a name clash.
            if (resourceSameName.getId() != resourceId) {
                responseDto.setSuccessful(false);
                responseDto.setMessage("There exists a resource of the same name.");
                return responseDto;
            }
        }
        final Pattern p = Pattern.compile("[- a-zA-Z0-9,/\\(\\)&]+");
        final Matcher m = p.matcher(resourceName);
        if (!m.matches()) {
            responseDto.setSuccessful(false);
            responseDto.setMessage("Resource name must contain only the following characters: " +
                    "a-z A-Z 0-9 , - ( ) / & and whitespace, and cannot be empty");
            return responseDto;
        }

        if (resourceSublocation == null) {
            // should NEVER happen, but just to be safe
            responseDto.setSuccessful(false);
            responseDto.setMessage("There is no Sub-Location associated with this resource");
            return responseDto;
        }

        String oldName = resource.getName();
        String oldSublocation = resourceSublocation.getSublocation().getName();
        String oldActive = String.valueOf(resourceSublocation.isActive());

        // don't change the active flag of the resource, but of the resource_sublocation
        resource.setName(resourceName);
        resourceSublocation.setSublocation(sublocation);
        if (activate) {
            resourceSublocation.setActive(true);
        }

        resourceDAO.updateEntity(resource);
        resourceDAO.updateEntity(resourceSublocation);

        StringBuilder deltaBuilder = new StringBuilder();
        appendIfDifferentStrings(deltaBuilder, oldName, resourceName, "name");
        appendIfDifferentStrings(deltaBuilder, oldSublocation, sublocation.getName(), "sublocation");
        appendIfDifferentStrings(deltaBuilder, oldActive, String.valueOf(resourceSublocation.isActive()), "isActive");
        String deltaString = deltaBuilder.toString();

        auditService.logResourceActivity(ipAddress, resource, user, Statics.AUDIT_RESOURCE_UPDATE, deltaString, null);
        responseDto.setSuccessful(true);
        return responseDto;
    }

    public BooleanResultDTO changeResourceStatus(final int resourceId, final int sublocationId, final boolean activate)
    {
        ResourceSublocation resourceSublocation = resourceDAO.findResourceSublocation(resourceId, sublocationId);

        BooleanResultDTO result = new BooleanResultDTO();

        if(resourceSublocation != null)
        {
            resourceSublocation.setActive(activate);
            resourceDAO.updateEntity(resourceSublocation);
            result.setResult(true);
        }
        else
        {
            result.setResult(false);
        }
        return result;
    }

    public ResourcesBooleanResponseDTO activateResources(final ResourceIdsRequestDTO resources) {
        final List<ResourceSublocation> resourceList = new ArrayList<>();

        final ResourcesBooleanResponseDTO resourcesBooleanResponseDTO = new ResourcesBooleanResponseDTO(true);

        if (isNullOrEmpty(resources.getResourceIds())) {
            resourcesBooleanResponseDTO.setResult(false);
            resourcesBooleanResponseDTO.setErrorMsg("No resources specified to be activated.");
            return resourcesBooleanResponseDTO;
        }

        for (Integer id: resources.getResourceIds()) {
            final Resource resource = resourceDAO.findResourceById(id);

            if (resource == null || resource.getSublocations().isEmpty()) {
                resourcesBooleanResponseDTO.setResult(false);
                resourcesBooleanResponseDTO.setErrorMsg("Not all resources specified were able to be found.");
                return resourcesBooleanResponseDTO;
            }
            else {
                resourceList.addAll(resource.getSublocations().stream().map(sublocation ->
                        resourceDAO.findResourceSublocation(resource.getId(), sublocation.getId())).collect(Collectors.toList()));
            }
        }
        resourcesBooleanResponseDTO.setResourcesResponses(this.generateResourceResponseList(resourceList));

        return resourcesBooleanResponseDTO;
    }

    public BooleanResultDTO addAnnotations(final AnnotationsIdRequestDTO dto, final User user, final String remoteHost) {
        final Resource resource = resourceDAO.findResourceById(dto.getResourceId());
        final List<LineLevelAnnotations> list = new ArrayList<>();
        final BooleanResultDTO resultDTO = new BooleanResultDTO();
        LineLevelAnnotations lineLevelAnnotations;
        ResourceAnnotation resourceAnnotation;

        if (resource == null) {
            resultDTO.setResult(false);
            resultDTO.setErrorMsg("Unable to find specified resource.");
            return resultDTO;
        }
        for (Integer lineLevelId: dto.getAnnotationIds()) {
            lineLevelAnnotations = resourceDAO.findLineLevelAnnotationsById(lineLevelId);
            list.add(lineLevelAnnotations);
            if (lineLevelAnnotations == null) {
                resultDTO.setResult(false);
                resultDTO.setErrorMsg("Not all specified annotations exist. Please try again.");
                return resultDTO;
            }
        }

        for(ResourceAnnotation resourceAnnotation1: resourceDAO.findResourcesAnnotationsByResource(resource)) {
            list.remove(resourceAnnotation1.getLineLevelAnnotations());
        }

        if (list.size() > 0) {
            for (LineLevelAnnotations annotation: list) {
                resourceAnnotation = new ResourceAnnotation();
                resourceAnnotation.setResource(resource);
                resourceAnnotation.setLineLevelAnnotations(annotation);
                resourceDAO.createEntity(resourceAnnotation);
            }
            auditService.logResourceActivity(remoteHost, resource, user, Statics.AUDIT_RESOURCE_ANNOTATIONS_ADDED, null, null);
        }

        resultDTO.setResult(true);
        return resultDTO;
    }

    private List<ResourcesResponse> generateResourceResponseList(final List<ResourceSublocation> resourceList) {
        final List<ResourcesResponse> resourcesResponseList = new ArrayList<>();
        for (ResourceSublocation resourceSublocation: resourceList) {
            resourceSublocation.setActive(true);
            resourceDAO.updateEntity(resourceSublocation);
            resourcesResponseList.add(new ResourcesResponse(resourceSublocation));
        }
        return resourcesResponseList;
    }
}
