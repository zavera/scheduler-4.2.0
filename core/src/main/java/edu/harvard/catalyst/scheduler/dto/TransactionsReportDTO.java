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

public class TransactionsReportDTO implements CsvAbleDTO {

  private Integer asrId;
  private String asrName;
  private Date cancelTime; // !! this seems to be used as the 'change time', not just cancel
  private String ecommonsId;
  private Date checkInTime;
  private Date checkOutTime;
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
  private String visitName;
  private String visitTypeName;
  private String subjectFirstName;
  private String subjectMiddleName;
  private String subjectLastName;
  private String mrn;
  private String appointmentStatus;
  private String cancelStatus;
  private String cancelStatusReason;
  private Date scheduledStartTime;
  private Date scheduledEndTime;

  public TransactionsReportDTO() {
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
  
  public Date getCancelTime() {
    return cancelTime;
  }
  public void setCancelTime(Date cancelTime) {
    this.cancelTime = cancelTime;
  }
  
  public String getEcommonsId() {
    return ecommonsId;
  }
  public void setEcommonsId(String ecommonsId) {
    this.ecommonsId = ecommonsId;
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

  public String getAppointmentStatus() {
    return appointmentStatus;
  }

  public void setAppointmentStatus(String appointmentStatus) {
    this.appointmentStatus = appointmentStatus;
  }

  public String getCancelStatus() {
    return cancelStatus;
  }

  public void setCancelStatus(String cancelStatus) {
    this.cancelStatus = cancelStatus;
  }

  public String getCancelStatusReason() {
    return cancelStatusReason;
  }

  public void setCancelStatusReason(String cancelStatusReason) {
    this.cancelStatusReason = cancelStatusReason;
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

  @Override
  public String toCsvHeaders() {
    return "Appointment Status Reason,Status Change Time," +
            "Cancellation Type,Cancellation Reason,User Initiating Change," +
            "Scheduled Start Time,Scheduled End Time,Visit Length,Study Name," +
            "PI,Primary Contact,Catalyst ID,Local ID,IRB #,Subject Name,MRN";
  }

  @Override
  public List<String> toCsvRows(List<?> dtoList) {
    List<String> result = Lists.newArrayList();

    int previousAsrId = -1;
    boolean firstTime = true;

    for (Object object: dtoList) {
      TransactionsReportDTO d = (TransactionsReportDTO) object;

      List<String> columns = Lists.newArrayList();

      int currentAsrId = d.asrId;
      if (currentAsrId != previousAsrId) {
        if (!firstTime) {
          result.add(" \n");
        }
        firstTime = false;

        result.add(d.visitName + " - " + d.appointmentStatus + "\n");
        result.add(toCsvHeaders() + "\n");

      }
      previousAsrId = currentAsrId;

      columns.add(q(d.asrName));
      columns.add(q(showDateTime(d.cancelTime)));
      columns.add(q(d.cancelStatus));
      columns.add(q(d.cancelStatusReason));
      columns.add(q(d.ecommonsId));
      columns.add(q(showDateTime(d.scheduledStartTime)));
      columns.add(q(showDateTime(d.scheduledEndTime)));
      columns.add(q(formatEndMinusStart(
              d.scheduledStartTime, d.scheduledEndTime)));
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
