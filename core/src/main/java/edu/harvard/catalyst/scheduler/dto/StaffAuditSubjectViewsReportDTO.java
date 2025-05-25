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

import java.util.Date;
import java.util.List;

import static edu.harvard.catalyst.scheduler.util.MiscUtil.fullName;
import static edu.harvard.catalyst.scheduler.util.MiscUtil.q;
import static edu.harvard.catalyst.scheduler.util.MiscUtil.showDateTime;

public class StaffAuditSubjectViewsReportDTO implements CsvAbleDTO {

  private Integer subjectId;
  private Integer userId;
  private String subjectFirstName;
  private String subjectMiddleName;
  private String subjectLastName;
  private String mrn;
  private String actionPerformed;
  private Date date;
  private String ipAddress;
  private String userFirstName;
  private String userMiddleName;
  private String userLastName;
  private String ecommons;

  public StaffAuditSubjectViewsReportDTO() {
  }
  
  public Integer getSubjectId() {
    return subjectId;
  }
  public void setSubjectId(Integer subjectId) {
    this.subjectId = subjectId;
  }
  

  public String getSubjectFirstName() {
    return subjectFirstName;
  }
  public void setSubjectFirstName(String subjectFirstName) {
    this.subjectFirstName = subjectFirstName;
  }
  
  public String getSubjectMiddleName() {
    return subjectMiddleName;
  }
  public void setSubjectMiddleName(String subjectMiddleName) {
    this.subjectMiddleName = subjectMiddleName;
  }
  
  public String getSubjectLastName() {
    return subjectLastName;
  }
  public void setSubjectLastName(String subjectLastName) {
    this.subjectLastName = subjectLastName;
  }
  
  public String getMrn() {
    return mrn;
  }
  public void setMrn(String mrn) {
    this.mrn = mrn;
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

  public Integer getUserId() {
    return userId;
  }

  public void setUserId(Integer userId) {
    this.userId = userId;
  }

  public String getEcommons() {
    return ecommons;
  }

  public void setEcommons(String ecommons) {
    this.ecommons = ecommons;
  }

  @Override
  public String toCsvHeaders() {
    return "Subject Name,MRN,Action Performed," +
            "Date,IP Address,User,User ID";
  }

  @Override
  public List<String> toCsvRows(List<?> dtoList) {
    List<String> result = Lists.newArrayList();

    int previousSubjectId = -1;
    boolean firstHeader = true;

    for (Object object: dtoList) {
      StaffAuditSubjectViewsReportDTO d = (StaffAuditSubjectViewsReportDTO) object;

      List<String> columns = Lists.newArrayList();

      int currentSubjectId = d.subjectId;
      if (currentSubjectId != previousSubjectId) {
        if (!firstHeader) {
          result.add(" \n");
        }
        result.add(toCsvHeaders() + "\n");
        columns.add(q(fullName(
                d.subjectFirstName,
                d.subjectMiddleName,
                d.subjectLastName)));

        firstHeader = false;
      }
      else {
        columns.add("");
      }
      previousSubjectId = currentSubjectId;

      columns.add(q(d.mrn));
      columns.add(q(d.actionPerformed));
      columns.add(q(showDateTime(d.date)));
      columns.add(q(d.ipAddress));
      columns.add(q(fullName(
              d.userFirstName,
              d.userMiddleName,
              d.userLastName)));
      columns.add(q(d.ecommons));

      String rows = Joiner.on(",").join(columns);
      result.add(rows + "\n");
    }
    return result;
  }
}
