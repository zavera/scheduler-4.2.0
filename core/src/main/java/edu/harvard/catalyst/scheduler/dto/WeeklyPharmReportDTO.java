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

public class WeeklyPharmReportDTO implements CsvAbleDTO {

  private Integer studyId;
  private String studyName;
  private String catalystId;
  private String localId;
  private String irb;
  private String firstName;
  private String middleName;
  private String lastName;
  private String genderName;
  private String mrn;
  private String visitName;
  private Date scheduledStartTime;
  private Date scheduledEndTime;
  private String appointmentStatus;

  public WeeklyPharmReportDTO() {
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
  
  public String getGenderName() {
    return genderName;
  }
  public void setGenderName(String genderName) {
    this.genderName = genderName;
  }

  public Integer getStudyId() {
    return studyId;
  }
  public void setStudyId(Integer studyId) {
    this.studyId = studyId;
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

  public String getAppointmentStatus() {
    return appointmentStatus;
  }

  public void setAppointmentStatus(String appointmentStatus) {
    this.appointmentStatus = appointmentStatus;
  }

  @Override
  public String toCsvHeaders() {
    return "Study Name,Catalyst ID,Local Id,IRB #,IRB Expiration," +
            "Subject,MRN,Gender,Visit Name,Visit Start Time," +
            "Visit End Time,Visit Length,Appointment Status";
  }

  @Override
  public List<String> toCsvRows(List<?> dtoList) {
    List<String> result = Lists.newArrayList();
    result.add(toCsvHeaders() + "\n");

    for (Object object: dtoList) {
      WeeklyPharmReportDTO d = (WeeklyPharmReportDTO) object;

      List<String> columns = Lists.newArrayList();

      columns.add(q(d.studyName));
      columns.add(q(d.catalystId));
      columns.add(q(d.localId));
      columns.add(q(d.irb));
      columns.add(q("")); // irb expiration N/A
      columns.add(q(fullName(
              d.firstName,
              d.middleName,
              d.lastName)));
      columns.add(q(d.mrn));
      columns.add(q(d.genderName));
      columns.add(q(d.visitName));
      columns.add(showDateTime(d.scheduledStartTime));
      columns.add(showDateTime(d.scheduledEndTime));
      columns.add(formatEndMinusStart(
              d.scheduledStartTime, d.scheduledEndTime));
      columns.add(q(d.appointmentStatus));

      String rows = Joiner.on(",").join(columns);
      result.add(rows + "\n");
    }
    return result;
  }
}
