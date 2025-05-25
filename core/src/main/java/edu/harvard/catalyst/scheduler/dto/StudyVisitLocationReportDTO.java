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

public class StudyVisitLocationReportDTO implements CsvAbleDTO {

  private Integer visitTypeId;
  private Integer visitId;
  private String visitTypeName;
  private String sublocationName;
  private Date checkInTime;
  private Date checkOutTime;
  private String subjectFirstName;
  private String subjectMiddleName;
  private String subjectLastName;
  private String mrn;
  private String genderName;
  private String studyName;
  private String catalystId;
  private String localId;
  private String irb;
  private String visitName;
  private String visitType;
  

  public StudyVisitLocationReportDTO() {
  }

  public Integer getVisitTypeId() {
    return visitTypeId;
  }
  public void setVisitTypeId(Integer visitTypeId) {
    this.visitTypeId = visitTypeId;
  }
  
  public String getSublocationName() {
    return sublocationName;
  }
  public void setSublocationName(String sublocationName) {
    this.sublocationName = sublocationName;
  }
  
  public String getVisitTypeName() {
    return visitTypeName;
  }
  public void setVisitTypeName(String visitTypeName) {
    this.visitTypeName = visitTypeName;
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
  
  public String getGenderName() {
    return genderName;
  }
  public void setGenderName(String genderName) {
    this.genderName = genderName;
  }
  
  public String getStudyName() {
    return studyName;
  }
  public void setStudyName(String studyName) {
    this.studyName = studyName;
  }
  
  public String getCatalystId() {
    return catalystId;
  }
  public void setCatalystId(String catalystId) {
    this.catalystId = catalystId;
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

  public Integer getVisitId() {
    return visitId;
  }

  public void setVisitId(Integer visitId) {
    this.visitId = visitId;
  }

  public String getVisitType() {
    return visitType;
  }

  public void setVisitType(String visitType) {
    this.visitType = visitType;
  }

  @Override
  public String toCsvHeaders() {
    return "Location Name,Visit Name,Visit Type,Check In Time," +
            "Check Out Time,Visit Duration,Subject Name,MRN,Gender," +
            "Study Name,Catalyst ID,Local Id,IRB #";
  }

  @Override
  public List<String> toCsvRows(List<?> dtoList) {

      List<String> result = Lists.newArrayList();
      result.add(toCsvHeaders() + "\n");

      for (Object object: dtoList) {
          StudyVisitLocationReportDTO d = (StudyVisitLocationReportDTO) object;

          List<String> columns = Lists.newArrayList();

          columns.add(q(d.sublocationName));
          columns.add(q(d.visitTypeName));
          columns.add(q(d.visitType));
          columns.add(q(showDateTime(d.checkInTime)));
          columns.add(q(showDateTime(d.checkOutTime)));
          columns.add(q(formatEndMinusStart(
                  d.checkInTime, d.checkOutTime)));
          columns.add(q(fullName(
                  d.subjectFirstName,
                  d.subjectMiddleName,
                  d.subjectLastName)));
          columns.add(q(d.mrn));
          columns.add(q(d.genderName));
          columns.add(q(d.studyName));
          columns.add(q(d.catalystId));
          columns.add(q(d.localId));
          columns.add(q(d.irb));

          String rows = Joiner.on(",").join(columns);
          result.add(rows + "\n");
      }
      return result;
  }
}
