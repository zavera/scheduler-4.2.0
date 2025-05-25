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

import edu.harvard.catalyst.scheduler.dto.response.MrnInfoDTO;
import edu.harvard.catalyst.scheduler.dto.response.SubjectDetailResponse;
import edu.harvard.catalyst.scheduler.dto.response.SubjectsResponseDTO;
import edu.harvard.catalyst.scheduler.entity.User;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: carl
 * Date: 2/18/16
 * Time: 9:28 AM
 */
public interface SubjectService {

    SubjectDetailResponse getSubjectData(final Boolean isPuid,
                                         final Integer id,
                                         final String remoteHost,
                                         final User user);

    SubjectsResponseDTO getSearchedSubjects(final String subjectLastName,
                                            final String firstName,
                                            final String subjectMrn,
                                            final String subjectBirthDate,
                                            final List<String> subjectGenderId);

    SubjectDetailResponse findInternalSubjectByMrn(final List<MrnInfoDTO> mrnInfoDTO);

    SubjectDetailResponse getExternalSubjectDataUsingMrn(final MrnInfoDTO mrnInfoDTO);

    }


