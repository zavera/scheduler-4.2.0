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

public class DailyAdmReportDTO implements CsvAbleDTO {

  private Integer subjectId;
  private Integer visitTempId;
  private String subjectFirstName;
  private String subjectMiddleName;
  private String subjectLastName;
  private String birthdate;
  private String mrn;
  private String genderName;
  private String localId;
  private String irb;
  private String visitName;
  private String visitStatus;
  private String resourceName;
  private Date checkInTime;
  private Date checkOutTime;
  private Date scheduledStartTime;
  private Date scheduledEndTime;
  private String comment;
  

  public DailyAdmReportDTO() {
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
    this.subjectFirstName =   subjectFirstName;
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
  
  public String getGenderName() {
    return genderName;
  }
  public void setGenderName(String genderName) {
    this.genderName = genderName;
  }
  
  public String getLocalId() {
    return localId;
  }
  public void setLocalId(String localId) {
    this.localId = localId;
  }
  
  public String getIrb() {
    return irb;
  }
  public void setIrb(String irb) {
    this.irb = irb;
  }
  
  public String getVisitName() {
    return visitName;
  }
  public void setVisitName(String visitName) {
    this.visitName = visitName;
  }
  
  public String getResourceName() {
    return resourceName;
  }
  public void setResourceName(String resourceName) {
    this.resourceName = resourceName;
  }
  
  public Date getCheckInTime() {
    return checkInTime;
  }
  public void setCheckInTime(Date checkInTime) {
    this.checkInTime = checkInTime;
  }
  
  public Date getCheckOutTime() {
    return checkOutTime;
  }
  public void setCheckOutTime(Date checkOutTime) {
    this.checkOutTime = checkOutTime;
  }
  
  public Date getScheduledStartTime() {
    return scheduledStartTime;
  }
  public void setScheduledStartTime(Date scheduledStartTime) {
    this.scheduledStartTime = scheduledStartTime;
  }
  
  public Date getScheduledEndTime() {
    return scheduledEndTime;
  }
  public String getComment() {
	return comment;
}

public void setComment(String comment) {
	this.comment = comment;
}

public void setScheduledEndTime(Date scheduledEndTime) {
    this.scheduledEndTime = scheduledEndTime;
  }

  public String getBirthdate() {
    return birthdate;
  }
  public void setBirthdate(String birthdate) {
    this.birthdate = birthdate;
  }

  public Integer getVisitTempId() {
    return visitTempId;
  }

  public void setVisitTempId(Integer visitTempId) {
    this.visitTempId = visitTempId;
  }

public String getVisitStatus() {
    return visitStatus;
}

public void setVisitStatus(String visitStatus) {
    this.visitStatus = visitStatus;
}


  @Override
  public String toCsvHeaders() {
    return "Subject Name,DOB,MRN,Gender,Local ID,IRB #,Visit Name," +
            "Visit Status, Check In Time,Resource Name," +
            "Resource Start Time,Resource End Time,Comment";
  }

  @Override
  public List<String> toCsvRows(List<?> dtoList) {
    List<String> result = Lists.newArrayList();
    result.add(toCsvHeaders() + "\n");

    for (Object object: dtoList) {
      DailyAdmReportDTO d = (DailyAdmReportDTO) object;

      List<String> columns = Lists.newArrayList();

      columns.add(q(fullName(
              d.subjectFirstName,
              d.subjectMiddleName,
              d.subjectLastName)));
      columns.add(q(d.birthdate));
      columns.add(q(d.mrn));
      columns.add(q(d.genderName));
      columns.add(q(d.localId));
      columns.add(q(d.irb));
      columns.add(q(d.visitName));
      columns.add(q(d.visitStatus));
      columns.add(q(showDateTime(d.checkInTime)));
      columns.add(q(d.resourceName));
      columns.add(q(showDateTime(d.scheduledStartTime)));
      columns.add(q(showDateTime(d.scheduledEndTime)));
      columns.add(q(d.comment));

      String rows = Joiner.on(",").join(columns);
      result.add(rows + "\n");
    }
    return result;
  }
}
