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
import edu.harvard.catalyst.scheduler.entity.StudyUser;
import edu.harvard.catalyst.scheduler.entity.User;

import java.util.ArrayList;
import java.util.List;

public class UserDataResponse {

    private Integer id;
    private String lastName;
    private String firstName;
    private String title;
    private Long totalCount;
    private Boolean status;
    private String userId;

    public static List<UserDataResponse> getStudyUserData(List<StudyUser> userList, Long total) {
        List<UserDataResponse> result =  new ArrayList<UserDataResponse>();

        if (userList !=  null) {
           for (StudyUser user : userList) {
               UserDataResponse studyDataReportResponse =  new UserDataResponse(user, total);
               result.add(studyDataReportResponse);
           }
        }
        return result;
    }
    
    public static List<UserDataResponse> getUserData(List<User> userList, Long total) {
        List<UserDataResponse> result =  new ArrayList<UserDataResponse>();

        if (userList !=  null) {
           for (User user : userList) {
               UserDataResponse studyDataReportResponse = new UserDataResponse(user, total);
               result.add(studyDataReportResponse);
           }
        }
        return result;
    }
    
    public UserDataResponse(User user, Long total) {

        if (user == null) {
            SchedulerRuntimeException.logAndThrow("user parameter should be non-null"); // too bad
        }

        this.id = user.getId();
        this.lastName = user.getLastName();
        this.firstName = user.getFirstName();
        this.title = user.getInstitutionRole().getName();
        this.userId = user.getEcommonsId();
        this.status = user.getActive();
        this.totalCount = total;
    }

    public UserDataResponse(StudyUser user, Long total) {

        if (user == null) {
            SchedulerRuntimeException.logAndThrow("StudyUser parameter should be non-null"); // too bad
        }

        this.id = user.getId();
        this.lastName = user.getUser().getLastName();
        this.firstName = user.getUser().getFirstName();
        this.title = user.getUser().getInstitutionRole().getName();
        this.status = user.getActive();
        this.totalCount = total;
    }

    // The following getters are needed for testability

    public String getLastName() {
        return lastName;
    }

    public String getFirstName() {
        return firstName;
    }

    public long getTotalCount() { return totalCount; }

    public String getUserId() { return userId; }

    public Integer getId() { return id; }

    public String getTitle() { return title; }
}

