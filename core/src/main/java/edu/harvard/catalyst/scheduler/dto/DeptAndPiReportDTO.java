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

public class DeptAndPiReportDTO implements CsvAbleDTO {

  private Integer piId;
  private String piFirstName;
  private String piMiddleName;
  private String piLastName;
  private String piDepartment;
  private String piDivision;
  private String piFacultyRank;
  private String piCredential;
  private String piInstitution;
  private String studyName;
  private String catalystId;
  private String localId;
  private String irb;
  private Date checkInTime;
  private Date checkOutTime;
  private Date scheduledStartTime;
  private Date scheduledEndTime;
  private String visitName;
  private String subjectFirstName;
  private String subjectMiddleName;
  private String subjectLastName;
  private String mrn;
  

  public DeptAndPiReportDTO() {
  }

  public Integer getPiId() {
    return piId;
  }
  public void setPiId(Integer piId) {
    this.piId = piId;
  }
  
  public String getPiFirstName() {
    return piFirstName;
  }
  public void setPiFirstName(String piFirstName) {
    this.piFirstName = piFirstName;
  }
  
  public String getPiMiddleName() {
    return piMiddleName;
  }
  public void setPiMiddleName(String piMiddleName) {
    this.piMiddleName = piMiddleName;
  }
  
  public String getPiLastName() {
    return piLastName;
  }
  public void setPiLastName(String piLastName) {
    this.piLastName = piLastName;
  }
  
  public String getPiDepartment() {
    return piDepartment;
  }
  public void setPiDepartment(String piDepartment) {
    this.piDepartment = piDepartment;
  }
  
  public String getPiDivision() {
    return piDivision;
  }
  public void setPiDivision(String piDivision) {
    this.piDivision = piDivision;
  }
  
  public String getPiFacultyRank() {
    return piFacultyRank;
  }
  public void setPiFacultyRank(String piFacultyRank) {
    this.piFacultyRank = piFacultyRank;
  }
  
  public String getPiCredential() {
    return piCredential;
  }
  public void setPiCredential(String piCredential) {
    this.piCredential = piCredential;
  }
  
  public String getPiInstitution() {
    return piInstitution;
  }
  public void setPiInstitution(String piInstitution) {
    this.piInstitution = piInstitution;
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
  public void setScheduledEndTime(Date scheduledEndTime) {
    this.scheduledEndTime = scheduledEndTime;
  }
  
  public String getVisitName() {
    return visitName;
  }
  public void setVisitName(String visitName) {
    this.visitName = visitName;
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

  @Override
  public String toCsvHeaders() {
    return "PI,Department,Division,Faculty Rank,Credential," +
            "Institution,Study Name,Catalyst ID,Local ID,IRB #," +
            "Visit Name,Check In Time,Check Out Time," +
            "Study Subject,MRN";
  }

  @Override
  public List<String> toCsvRows(List<?> dtoList) {

    List<String> result = Lists.newArrayList();
    result.add(toCsvHeaders() + "\n");

    for (Object object: dtoList) {
      DeptAndPiReportDTO d = (DeptAndPiReportDTO) object;

      List<String> columns = Lists.newArrayList();

      columns.add(q(fullName(
              d.piFirstName,
              d.piMiddleName,
              d.piLastName)));
      columns.add(q(d.piDepartment));
      columns.add(q(d.piDivision));
      columns.add(q(d.piFacultyRank));
      columns.add(q(d.piCredential));
      columns.add(q(d.piInstitution));
      columns.add(q(d.studyName));
      columns.add(q(d.catalystId));
      columns.add(q(d.localId));
      columns.add(q(d.irb));
      columns.add(q(d.visitName));
      columns.add(q(showDateTime(d.checkInTime)));
      columns.add(q(showDateTime(d.checkOutTime)));
      columns.add(q(fullName(
              d.subjectFirstName,
              d.subjectMiddleName,
              d.subjectLastName)));
      columns.add(q(d.mrn));

      String rows = Joiner.on(",").join(columns);
      result.add(rows + "\n");
    }
    return result;
  }
}
