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

import static edu.harvard.catalyst.scheduler.util.MiscUtil.*;
import static edu.harvard.catalyst.scheduler.util.MiscUtil.q;

public class SubjectPurgeReportDTO implements CsvAbleDTO {

  private String firstName;
  private String middleName;
  private String lastName;
  private String mrn;
  private String userLastName;
  private String userMiddleName;
  private String userFirstName;
  private String visitName;
  private Date createTime;

  public SubjectPurgeReportDTO() {
  }

  public String getFirstName() {
    return firstName;
  }
  public void setFirstName(String firstName) {
    this.firstName = firstName;
  }
  
  public String getMiddleName() {
    return middleName;
  }
  public void setMiddleName(String middleName) {
    this.middleName = middleName;
  }
  
  public String getLastName() {
    return lastName;
  }
  public void setLastName(String lastName) {
    this.lastName = lastName;
  }
  
  public String getMrn() {
    return mrn;
  }
  public void setMrn(String mrn) {
    this.mrn = mrn;
  }
  
  public String getUserLastName() {
    return userLastName;
  }

  public void setUserLastName(String userLastName) {
    this.userLastName = userLastName;
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

  public String getVisitName() {
    return visitName;
  }
  public void setVisitName(String visitName) {
    this.visitName = visitName;
  }
  
  public Date getCreateTime() {
    return createTime;
  }
  public void setCreateTime(Date createTime) {
    this.createTime = createTime;
  }


  @Override
  public String toCsvHeaders() {
    return "Subject Name,MRN,User,Subject Creation Date";
  }

  @Override
  public List<String> toCsvRows(List<?> dtoList) {

    List<String> result = Lists.newArrayList();
    result.add(toCsvHeaders() + "\n");

    for (Object object: dtoList) {
      SubjectPurgeReportDTO d = (SubjectPurgeReportDTO) object;

      List<String> columns = Lists.newArrayList();

      columns.add(q(fullName(
              d.firstName,
              d.middleName,
              d.lastName)));
      columns.add(q(d.mrn));
      columns.add(q(fullName(
              d.userFirstName,
              d.userMiddleName,
              d.userLastName)));
      columns.add(q(showDateTime(d.createTime)));

      String rows = Joiner.on(",").join(columns);
      result.add(rows + "\n");
    }
    return result;
  }
}
