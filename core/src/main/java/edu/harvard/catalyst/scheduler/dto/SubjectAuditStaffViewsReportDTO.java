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
package edu.harvard.catalyst.scheduler.dto;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import edu.harvard.catalyst.scheduler.entity.User;

import java.util.Date;
import java.util.List;

import static edu.harvard.catalyst.scheduler.util.MiscUtil.*;
import static edu.harvard.catalyst.scheduler.util.MiscUtil.q;

public class SubjectAuditStaffViewsReportDTO implements CsvAbleDTO {

  private User userId;
  private String userFirstName;
  private String userMiddleName;
  private String userLastName;
  private String userRoleName;
  private String userDepartmentName;
  private String userInstitutionName;
  private String actionPerformed;
  private Date date;
  private String ipAddress;
  private String subjectName;
  private Integer subjectId;
  

  public SubjectAuditStaffViewsReportDTO() {
  }
  
  public User getUserId() {
    return userId;
  }
  public void setUserId(User userId) {
    this.userId = userId;
  }

  public String getUserRoleName() {
    return userRoleName;
  }
  public void setUserRoleName(String userRoleName) {
    this.userRoleName = userRoleName;
  }

  public String getUserDepartmentName() {
    return userDepartmentName;
  }
  public void setUserDepartmentName(String userDepartmentName) {
    this.userDepartmentName = userDepartmentName;
  }

  public String getUserInstitutionName() {
    return userInstitutionName;
  }
  public void setUserInstitutionName(String userInstitutionName) {
    this.userInstitutionName = userInstitutionName;
  }

  public String getActionPerformed() {
    return actionPerformed;
  }
  public void setActionPerformed(String actionPerformed) {
    this.actionPerformed = actionPerformed;
  }
  
  public Date getDate() {
    return date;
  }
  public void setDate(Date date) {
    this.date = date;
  }
  
  public String getIpAddress() {
    return ipAddress;
  }
  public void setIpAddress(String ipAddress) {
    this.ipAddress = ipAddress;
  }
  
  public String getUserFirstName() {
    return userFirstName;
  }
  public void setUserFirstName(String userFirstName) {
    this.userFirstName = userFirstName;
  }
  
  public String getUserMiddleName() {
    return userMiddleName;
  }
  public void setUserMiddleName(String userMiddleName) {
    this.userMiddleName = userMiddleName;
  }
  
  public String getUserLastName() {
    return userLastName;
  }
  public void setUserLastName(String userLastName) {
    this.userLastName = userLastName;
  }

  public String getSubjectName() {
    return subjectName;
  }

  public void setSubjectName(String subjectName) {
    this.subjectName = subjectName;
  }

  public Integer getSubjectId() {
    return subjectId;
  }

  public void setSubjectId(Integer subjectId) {
    this.subjectId = subjectId;
  }

  @Override
  public String toCsvHeaders() {
    return "Subject Name,User,Title,Department,Institution,Action Performed," +
            "Date,IP Address";
  }

  @Override
  public List<String> toCsvRows(List<?> dtoList) {

    List<String> result = Lists.newArrayList();

    int previousSubjectId = -1;

    for (Object object: dtoList) {
      SubjectAuditStaffViewsReportDTO d = (SubjectAuditStaffViewsReportDTO) object;

      int currentSubjectId = d.subjectId;
      if (currentSubjectId != previousSubjectId) {
        result.add(toCsvHeaders() + "\n");
        previousSubjectId = currentSubjectId;
      }

      List<String> columns = Lists.newArrayList();

      String roleName =
              d.userId.getRole() == null ? ""
                      : d.userId.getRole().getName();
      String deptName =
              d.userId.getDepartment() == null ? ""
                      : d.userId.getDepartment().getName();
      String instName =
              d.userId.getInstitution() == null ? ""
                      : d.userId.getInstitution().getName();

      columns.add(q(d.subjectName));
      columns.add(q(fullName(
              d.userId.getFirstName(),
              d.userId.getMiddleName(),
              d.userId.getLastName())));
      columns.add(q(roleName));
      columns.add(q(deptName));
      columns.add(q(instName));
      columns.add(q(d.actionPerformed));
      columns.add(q(showDateTime(d.date)));
      columns.add(q(d.ipAddress));

      String rows = Joiner.on(",").join(columns);
      result.add(rows + "\n");
    }
    return result;
  }
}
