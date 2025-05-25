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

import edu.harvard.catalyst.scheduler.core.SchedulerRuntimeException;
import edu.harvard.catalyst.scheduler.dto.response.MrnInfoDTO;
import edu.harvard.catalyst.scheduler.dto.response.SubjectDetailResponse;
import edu.harvard.catalyst.scheduler.dto.response.SubjectsResponseDTO;
import edu.harvard.catalyst.scheduler.entity.*;
import edu.harvard.catalyst.scheduler.persistence.SubjectDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;

import java.util.List;

@Component
public class StandaloneSubjectService implements SubjectService {
    protected final SubjectDAO subjectDAO;

    @Autowired
    public StandaloneSubjectService(final SubjectDAO subjectDAO) {
        this.subjectDAO = subjectDAO;
    }

    @Override
    public SubjectDetailResponse getSubjectData(Boolean isPuid, Integer id, String remoteHost, User user) {

        return subjectDAO.getSubjectDataById(id);
    }

    @Override
    public SubjectsResponseDTO getSearchedSubjects(final String subjectLastName,
                                                   final String subjectFirstName,
                                                   final String subjectMrn,
                                                   final String subjectBirthDate,
                                                   final List<String> subjectGenderIdList) {
        return subjectDAO.getSearchSubjects(
                subjectLastName,
                subjectFirstName,
                subjectMrn,
                subjectBirthDate,
                subjectGenderIdList
        );
    }

    public boolean refreshExternalSubject(SubjectMrn subjectMrn, boolean tolerateSomeTimeouts) {
        SchedulerRuntimeException.logAndThrow("Cannot refresh external Subject in standalone mode");

        return false;
    }

    public void markArchivalStatus(Subject subject, String actionPerformed, String changesDetail, ArchivalStatus status) {
        subject.setArchivalStatus(status);
        subjectDAO.updateEntity(subject);

        final ActivityLog al = new ActivityLog();
        al.setPerformingUser(null);
        al.setActionPerformed(actionPerformed);
        al.setDate(new Date());
        al.setIpAddress(null);
        al.setAffectedUser(null);
        al.setAffectedResource(null);
        al.setAffectedSublocation(null);
        al.setAffectedSubject(subject);
        al.setAffectedStudy(null);
        al.setAffectedVisit(null);
        al.setChangesDetailRequiredField(null);
        al.setChangesDetail(changesDetail);
        al.setAppointmentOverrideReason(null);
        al.setBookedVisit(null);

        subjectDAO.createEntity(al);
    }

    @Override
    public SubjectDetailResponse findInternalSubjectByMrn(final List<MrnInfoDTO> mrnInfoDTO)
    {
        return subjectDAO.findInternalSubjectByMrn(mrnInfoDTO);
    }

    @Override
    public SubjectDetailResponse getExternalSubjectDataUsingMrn(final MrnInfoDTO mrnInfoDTO)
    {
        //return an empty response
        return new SubjectDetailResponse();
    }
}
