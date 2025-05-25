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

import static edu.harvard.catalyst.scheduler.util.DateUtility.overnightsBetweenDates;
import static edu.harvard.catalyst.scheduler.util.MiscUtil.*;

public class CensusReportDTO implements CsvAbleDTO {

  private Integer asrId;
  private Integer studyId;
  private Integer visitId;
  private String asrName;
  private String studyName;
  private String piFirstName;
  private String piMiddleName;
  private String piLastName;
  private String psFirstName;
  private String psMiddleName;
  private String psLastName;
  private String catalystId;
  private String localId;
  private String irb;
  private Date scheduledStartTime;
  private Date scheduledEndTime;
  private String visitName;
  private String subjectFirstName;
  private String subjectMiddleName;
  private String subjectLastName;
  private String mrn;
  private String visitType;
  private Boolean industryInitiated;
  private String sublocation;

  public CensusReportDTO() {
  }

  public Integer getAsrId() {
    return asrId;
  }
  public void setAsrId(Integer asrId) {
    this.asrId = asrId;
  }
  
  public Integer getStudyId() {
    return studyId;
  }

  public void setStudyId(Integer studyId) {
    this.studyId = studyId;
  }

  public String getAsrName() {
    return asrName;
  }
  public void setAsrName(String asrName) {
    this.asrName = asrName;
  }
  
  public String getStudyName() {
    return studyName;
  }
  public void setStudyName(String studyName) {
    this.studyName = studyName;
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
  
  public String getPsFirstName() {
    return psFirstName;
  }
  public void setPsFirstName(String psFirstName) {
    this.psFirstName = psFirstName;
  }
  
  public String getPsMiddleName() {
    return psMiddleName;
  }
  public void setPsMiddleName(String psMiddleName) {
    this.psMiddleName = psMiddleName;
  }
  
  public String getPsLastName() {
    return psLastName;
  }
  public void setPsLastName(String psLastName) {
    this.psLastName = psLastName;
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
  
  public String getVisitType() {
    return visitType;
  }
  public void setVisitType(String visitType) {
    this.visitType = visitType;
  }

  public Boolean getIndustryInitiated() {
    return industryInitiated;
  }

  public void setIndustryInitiated(Boolean industryInitiated) {
    this.industryInitiated = industryInitiated;
  }

    public Integer getVisitId() {
        return visitId;
    }

    public void setVisitId(Integer visitId) {
        this.visitId = visitId;
    }

    public String getSublocation() {
        return sublocation;
    }
    public void setSublocation(String sublocation) {
        this.sublocation = sublocation;
    }


  @Override
  public String toCsvHeaders() {
    return "Reason Name,Study Name,PI Name,Primary Scheduling Contact," +
            "Catalyst ID,Local ID,IRB #,Industry Initiated,Check In Date," +
            "Check Out Date,# Days Over Midnight,Visit ID,Visit Length," +
            "Visit Length total Minutes,Visit Name, Sub-Location, " +
            "Visit Type,Study Subject,MRN";
  }

  @Override
  public List<String> toCsvRows(List<?> dtoList) {

    List<String> result = Lists.newArrayList();
    result.add(toCsvHeaders() + "\n");

    for (Object object: dtoList) {
      CensusReportDTO d = (CensusReportDTO) object;

      List<String> columns = Lists.newArrayList();

      columns.add(q(d.asrName));
      columns.add(q(d.studyName));
      columns.add(q(fullName(
              d.piFirstName,
              d.piMiddleName,
              d.piLastName)));
      columns.add(q(fullName(
              d.psFirstName,
              d.psMiddleName,
              d.psLastName)));
      columns.add(q(d.catalystId));
      columns.add(q(d.localId));
      columns.add(q(d.irb));
      columns.add(q(yesNoValue(d.industryInitiated)));
      columns.add(q(showDateTime(d.scheduledStartTime)));
      columns.add(q(showDateTime(d.scheduledEndTime)));

      columns.add(q(overnightsBetweenDates(d.scheduledStartTime,
              d.scheduledEndTime)));
      columns.add(q(d.visitId));

      columns.add(q(formatEndMinusStart(d.scheduledStartTime,
              d.scheduledEndTime)));
      columns.add(q(endMinusStartInMinutes(d.scheduledStartTime,
              d.scheduledEndTime)));

      columns.add(q(d.visitName));
      columns.add(q(d.sublocation));
      columns.add(q(d.visitType));
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