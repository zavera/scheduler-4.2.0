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
package edu.harvard.catalyst.scheduler.dto.response;

import edu.harvard.catalyst.scheduler.core.SchedulerRuntimeException;
import edu.harvard.catalyst.scheduler.entity.Study;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: carl
 * Date: 4/7/14
 * Time: 3:27 PM
 */
public class GetStudiesResponse {

    private Long totalCount;

    List<Study1> studys;

    public List<Study1> getStudies() {
        return studys;
    }

    // public for testability
    public static class Study1 {
        public Integer id;

        public String localId;
        public String name;

        Study1(Study study) {
            if (study == null) {
                SchedulerRuntimeException.logAndThrow("Study should not be null!");
            }

            this.id = study.getId();

            this.localId = study.getLocalId();
            this.name = study.getName();
        }
    }

    public static GetStudiesResponse createGetStudiesResponse(List<Study> studies, Long total) {
        List<Study1> study1s =  new ArrayList<Study1>();

        GetStudiesResponse result = new GetStudiesResponse();

        if (studies !=  null) {
            for (Study study : studies) {
                Study1 study1 =  new Study1(study);
                study1s.add(study1);
            }
        }

        result.studys = study1s;
        result.totalCount = total;

        return result;
    }
}
