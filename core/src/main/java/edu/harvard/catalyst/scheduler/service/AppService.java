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

import com.google.gson.Gson;
import edu.harvard.catalyst.scheduler.dto.statics.StudyStatusFilter;
import edu.harvard.catalyst.scheduler.persistence.AuthDAO;
import edu.harvard.catalyst.scheduler.persistence.ResourceDAO;
import edu.harvard.catalyst.scheduler.persistence.StudyDAO;
import edu.harvard.catalyst.scheduler.persistence.SubjectDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class AppService {

    private final StudyDAO studyDAO;
    private final ResourceDAO resourceDAO;
    private final SubjectDAO subjectDAO;
    private final AuthDAO authDAO;


    @Autowired
    public AppService(final StudyDAO studyDAO, final ResourceDAO resourceDAO, final SubjectDAO subjectDAO, final AuthDAO authDAO) {
        this.studyDAO = studyDAO;
        this.resourceDAO = resourceDAO;
        this.subjectDAO = subjectDAO;
        this.authDAO = authDAO;
    }

    //Don't use - Needed for spring security cglib proxying
    AppService() {
        this(null, null, null, null);
    }
    
    Map<String, List<?>> getStaticListsMap() {
        final Map<String, List<?>> map = new HashMap<>();

        map.put("visitTypes", studyDAO.getVisitTypes());
        map.put("sublocations", resourceDAO.getSublocations());
        map.put("roles", studyDAO.getRoles());
        map.put("resources", resourceDAO.findResourcesActiveInSublocations());
        map.put("rooms", resourceDAO.getRooms());
        map.put("nurseAnnotations", resourceDAO.getNursingLineLevelAnnotations());
        map.put("nutritionAnnotations", resourceDAO.getNutritionLineLevelAnnotations());
        map.put("equipmentAnnotations", resourceDAO.getEquipmentlLineLevelAnnotations());
        map.put("roomAnnotations", resourceDAO.getRoomLineLevelAnnotations());
        map.put("labAnnotations", resourceDAO.getLabLineLevelAnnotations());
        map.put("institutionRoles", studyDAO.getInstitutionRoles());
        map.put("studyStatuses", studyDAO.getStudyStatuses());
        map.put("resourceTypes", resourceDAO.getResourceTypes());
        map.put("resourceNames", resourceDAO.getAllResourceNames());
        map.put("overrideReasons", studyDAO.getOverrideReasons());
        map.put("institutions", resourceDAO.getInstitutions());
        map.put("irbInstitutions", studyDAO.getIRBInstitutions());
        map.put("checkOutReasons", studyDAO.getCheckOutReasons());
        map.put("cancellationReasons", studyDAO.getCancellationReasons());
        map.put("appointmentStatuses", studyDAO.getAppointmentStatuses());
        map.put("states", subjectDAO.getStates());
        map.put("races", subjectDAO.getRaces());
        map.put("ethnicities", subjectDAO.getEthnicities());
        map.put("countries", subjectDAO.getCountries());
        map.put("genders", subjectDAO.getGenders());
        map.put("visitCancelStatuses", studyDAO.getVisitCancelStatuses());
        map.put("fundingSources", studyDAO.getFundingSources());
        map.put("credentials", authDAO.getCredentials());
        map.put("divisions", authDAO.getDivisions());
        map.put("departments", authDAO.getDepartments());
        map.put("facultyRanks", authDAO.getFacultyRanks());
        map.put("fundingSources", authDAO.getFundingSources());
        map.put("centersAndInstitutions", authDAO.getCentersAndInstitutions());

        return map;
    }

    Map<String, List<?>> getStaticMapForStandardReportPage() {

        final Map<String, List<?>> map = new HashMap<>();

        map.put("studyStatusFilterValues", StudyStatusFilter.getMenuContents());

        return map;

    }

    //@Transactional
    public String getStaticLists() {

        final Gson gson = new Gson();

        return gson.toJson(getStaticListsMap());
    }

    //@Transactional
    public String getStaticDataForStandardReportPage() {

        final Gson gson = new Gson();

        return gson.toJson(getStaticMapForStandardReportPage());
    }
}
