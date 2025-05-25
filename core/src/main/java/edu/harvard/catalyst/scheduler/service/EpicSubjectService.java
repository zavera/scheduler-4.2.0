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

import com.google.common.collect.Lists;
import edu.harvard.catalyst.scheduler.core.SchedulerRuntimeException;
import edu.harvard.catalyst.scheduler.dto.ExternalSubjectQueryBuilder;
import edu.harvard.catalyst.scheduler.dto.Epic.EmpiSubjectDto;
import edu.harvard.catalyst.scheduler.dto.response.MrnInfoDTO;
import edu.harvard.catalyst.scheduler.dto.response.SubjectDetailResponse;
import edu.harvard.catalyst.scheduler.dto.response.SubjectsResponseDTO;
import edu.harvard.catalyst.scheduler.entity.*;
import edu.harvard.catalyst.scheduler.entity.Ethnicity;
import edu.harvard.catalyst.scheduler.persistence.EpicSubjectDAO;
import edu.harvard.catalyst.scheduler.util.MiscUtil;
import edu.harvard.catalyst.scheduler.util.SubjectDataEncryptor;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.*;

import static edu.harvard.catalyst.scheduler.core.Statics.*;
import static edu.harvard.catalyst.scheduler.util.MiscUtil.*;

@Component
public class EpicSubjectService implements SubjectService {

    private static final Logger LOG = Logger.getLogger(EpicSubjectService.class);

    protected final EpicSubjectDAO epicSubjectDAO;
    private StandaloneSubjectService standaloneSubjectService;

	@Autowired
	public EpicSubjectService(final EpicSubjectDAO epicSubjectDAO,
                              final StandaloneSubjectService standaloneSubjectService) {
		this.epicSubjectDAO = epicSubjectDAO;
        this.standaloneSubjectService = standaloneSubjectService;
	}

	// Don't use - Needed for spring security cglib proxying
	EpicSubjectService() {
		this(null, null);
	}

    public EmpiSubjectDto getSubjectsAsEmpiSubjectDto(ExternalSubjectQueryBuilder externalSubjectQueryBuilder, boolean tolerateSomeTimeouts) {
        EmpiSubjectDto result = epicSubjectDAO.getSearchedSubjectsAsEmpiSubjectDto(externalSubjectQueryBuilder, tolerateSomeTimeouts);

        return result;
    }

    @Override
    public SubjectsResponseDTO getSearchedSubjects(final String subjectLastName,
                                                   final String subjectFirstName,
                                                   final String subjectMrn,
                                                   final String subjectBirthDate,
                                                   final List<String> subjectGenderIdList) {

        String subjectGenderCode = null;
        if(subjectGenderIdList != null && subjectGenderIdList.size() == 1) {
            subjectGenderCode = subjectGenderIdList.get(0);
        }

        SubjectsResponseDTO daoResult = null;

        // Exception Handling Failover (EHF)
        // case = subject search
        try {
            daoResult = epicSubjectDAO.getSearchedSubjects(
                    subjectLastName,
                    subjectFirstName,
                    subjectMrn,
                    subjectBirthDate,
                    subjectGenderCode
            );
        }
        catch (SchedulerRuntimeException sre) {
            // try again with standalone service
            daoResult = standaloneSubjectService.getSearchedSubjects(
                    subjectLastName,
                    subjectFirstName,
                    subjectMrn,
                    subjectBirthDate,
                    subjectGenderIdList);

            String failoverMessage;
            String firstPart;
            String secondPart;
            Throwable cause = sre.getCause();

            boolean sslProblem = cause != null && cause instanceof IOException;
            if (sslProblem) {
                firstPart = STR_SSL_FAILS;
            }
            else {
                firstPart = STR_EMPI_FAILS;
            }

            boolean schedProblem = daoResult.getTotalCount() == 0L;
            String code;
            if (schedProblem) {
                secondPart = STR_AND_SCHED_FAILS_TOO;
                code = sslProblem ? CODE_SSL_FAILS_AND_SCHED_FAILS : CODE_EMPI_FAILS_AND_SCHED_FAILS;
            }
            else {
                secondPart = STR_BUT_SCHED_WORKS;
                code = sslProblem ? CODE_SSL_FAILS_BUT_SCHED_WORKS : CODE_EMPI_FAILS_BUT_SCHED_WORKS;
            }
            failoverMessage = applyCodeToXX(firstPart + secondPart, code);

            String failoverMessageDetailed = "Subject Search. " +
                    failoverMessage +
                    MiscUtil.throwableInfoForLog(sre) + ". " +
                    MiscUtil.throwableToStackTraceString(sre);

            daoResult.setEmpiFailoverMessage(failoverMessage);

            SchedulerRuntimeException.logDontThrow(failoverMessageDetailed);
        }

        return daoResult;
    }

    @Override
    public SubjectDetailResponse getSubjectData(Boolean isPuid, Integer id, String remoteHost, User user) {
        // Exception Handling Prototype: Failover (EHF)
        // case = + / expand
        SubjectDetailResponse subjectDetailResponse = new SubjectDetailResponse();

        String failoverMessage;
        String firstPart;

        String code;
        if (isPuid) {
            // try with empi service
            try {
                    subjectDetailResponse = epicSubjectDAO.findSubjectByUid(id);
            }
            catch (SchedulerRuntimeException sre) {

                // failover, try with internal scheduler
                Throwable cause = sre.getCause();

                boolean sslProblem = cause != null && cause instanceof IOException;
                if (sslProblem) {
                    firstPart = STR_SSL_FAILS;
                }
                else {
                    firstPart = STR_EMPI_FAILS;
                }

                String failoverMessageDetailed = "";
                try {
                    Subject subject = epicSubjectDAO.findInternalSubjectByPuid(String.valueOf(id));
                    SubjectDataEncryptor.decryptSubjectInPlace(subject);
                    subjectDetailResponse = new SubjectDetailResponse(subject);

                    code = sslProblem ? CODE_SSL_FAILS_BUT_SCHED_WORKS : CODE_EMPI_FAILS_BUT_SCHED_WORKS;
                    failoverMessage = applyCodeToXX(firstPart + STR_BUT_SCHED_WORKS, code);

                    failoverMessageDetailed = "Subject Search, Expand (+) subject info. " +
                            failoverMessage +
                            MiscUtil.throwableInfoForLog(sre) +
                            MiscUtil.throwableToStackTraceString(sre);
                }
                catch (Exception e) {
                    // couldn't getSubjectData, use default empty subjectDetailResponse
                    subjectDetailResponse.setId(null); // just to be explicit

                    code = sslProblem ? CODE_SSL_FAILS_AND_SCHED_FAILS : CODE_EMPI_FAILS_AND_SCHED_FAILS;
                    failoverMessage = applyCodeToXX(firstPart + STR_AND_SCHED_FAILS_TOO, code);

                    failoverMessageDetailed = "Subject Search, Expand (+) subject info. " +
                            failoverMessage +
                            MiscUtil.throwableInfoForLog(sre) +
                            MiscUtil.throwableInfoForLog(e) + ". " +
                            MiscUtil.throwableToStackTraceString(sre) + ". " +
                            MiscUtil.throwableToStackTraceString(e);
                }

                subjectDetailResponse.setEmpiFailoverMessage(failoverMessage);

                SchedulerRuntimeException.logDontThrow(failoverMessageDetailed);
            }
        }
        else {
            String failoverMessageDetailed = "";

            // stick with standalone service
            try {
                Subject subject = epicSubjectDAO.findBySubjectId(id);
                subjectDetailResponse = new SubjectDetailResponse(subject);

                failoverMessage = applyCodeToXX(STR_EMPI_FAILS + STR_BUT_SCHED_WORKS, CODE_EMPI_FAILS_BUT_SCHED_WORKS);
                failoverMessageDetailed = "Subject Search, Expand (+) subject info. " +failoverMessage;
            }
            catch (Exception e) {
                // couldn't getSubjectData, use default empty subjectDetailResponse
                subjectDetailResponse.setId(null); // just to be explicit

                // leave the XX for front-end to recode
                failoverMessage = STR_AND_SCHED_FAILS_TOO;

                failoverMessageDetailed = "Subject Search, Expand (+) subject info. " +
                        failoverMessage +
                        MiscUtil.throwableInfoForLog(e) + ". " +
                        MiscUtil.throwableToStackTraceString(e);

            }
            subjectDetailResponse.setEmpiFailoverMessage(failoverMessage);
            SchedulerRuntimeException.logDontThrow(failoverMessageDetailed);
        }

        return subjectDetailResponse;
    }

    @Override
    public SubjectDetailResponse findInternalSubjectByMrn(final List<MrnInfoDTO> mrnInfoDTO)
    {
        return epicSubjectDAO.findInternalSubjectByMrn(mrnInfoDTO);
    }

    @Override
    public SubjectDetailResponse getExternalSubjectDataUsingMrn(final MrnInfoDTO mrnInfoDTO)
    {
        if(mrnInfoDTO != null) {
            // Exception Handling Prototype: Failover (EHF)
            // case = view details from study subjects page
            SubjectDetailResponse subjectDetailResponse = null;

            try {
                subjectDetailResponse = epicSubjectDAO.getSubjectDetailsUsingMrnAndSite(mrnInfoDTO.getValue(), mrnInfoDTO.getInstitution());
            }
            catch (SchedulerRuntimeException sre) {
                Throwable cause = sre.getCause();
                String firstPart;
                String code;

                boolean sslProblem = cause != null && cause instanceof IOException;
                if (sslProblem) {
                    firstPart = STR_SSL_FAILS;
                }
                else {
                    firstPart = STR_EMPI_FAILS;
                }

                // try again with standalone service
                subjectDetailResponse = standaloneSubjectService.findInternalSubjectByMrn(Lists.newArrayList(mrnInfoDTO));

                String secondPart;
                if (subjectDetailResponse.getId() != null) {
                    secondPart = STR_BUT_SCHED_WORKS;
                    code = sslProblem ? CODE_SSL_FAILS_BUT_SCHED_WORKS : CODE_EMPI_FAILS_BUT_SCHED_WORKS;
                }
                else {
                    secondPart = STR_AND_SCHED_FAILS_TOO;
                    code = sslProblem ? CODE_SSL_FAILS_AND_SCHED_FAILS : CODE_EMPI_FAILS_AND_SCHED_FAILS;
                }

                String failoverMessage = applyCodeToXX(firstPart + secondPart, code);

                String failoverMessageDetailed = "Study Subject, View Details. " +
                        failoverMessage +
                        MiscUtil.throwableInfoForLog(sre) + ". " +
                        MiscUtil.throwableToStackTraceString(sre);

                subjectDetailResponse.setEmpiFailoverMessage(failoverMessage);

                SchedulerRuntimeException.logDontThrow(failoverMessageDetailed);
            }
            return subjectDetailResponse;
        }

        return new SubjectDetailResponse();
    }

    public Ethnicity lookupEmpiEthnicityString(String ethnicity) {
        return epicSubjectDAO.lookupEmpiEthnicityString(ethnicity);
    }
}
