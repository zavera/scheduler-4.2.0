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

import java.util.Date;

public class CancellationsReportDTO {

  private Integer asrId;
  private String asrName;
  private String cancelStatus;
  private String subjectFirstName;
  private String subjectMiddleName;
  private String subjectLastName;
  private String mrn;
  private Date scheduledStartTime;
  private Date scheduledEndTime;
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
  private String user;
  private String visitName;
  private String visitTypeName;
  private Date cancelDate;
  private Date scheduledDate;

  public CancellationsReportDTO() {
  }

  public Integer getAsrId() {
    return asrId;
  }
  public void setAsrId(Integer asrId) {
    this.asrId = asrId;
  }
  
  public String getAsrName() {
    return asrName;
  }
  public void setAsrName(String asrName) {
    this.asrName = asrName;
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
  
  public String getVisitName() {
    return visitName;
  }
  public void setVisitName(String visitName) {
    this.visitName = visitName;
  }
  
  public String getVisitTypeName() {
    return visitTypeName;
  }
  public void setVisitTypeName(String visitTypeName) {
    this.visitTypeName = visitTypeName;
  }

  public Date getCancelDate() {
    return cancelDate;
  }

  public void setCancelDate(Date cancelDate) {
    this.cancelDate = cancelDate;
  }

  public String getCancelStatus() {
    return cancelStatus;
  }

  public void setCancelStatus(String cancelStatus) {
    this.cancelStatus = cancelStatus;
  }

  public String getUser() {
    return user;
  }

  public void setUser(String user) {
    this.user = user;
  }

  public Date getScheduledDate() {
    return scheduledDate;
  }

  public void setScheduledDate(Date scheduledDate) {
    this.scheduledDate = scheduledDate;
  }
}
