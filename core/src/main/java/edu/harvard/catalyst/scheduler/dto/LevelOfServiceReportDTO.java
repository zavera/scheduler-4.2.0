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
import edu.harvard.catalyst.scheduler.entity.AppointmentStatusReason;

import java.util.Date;
import java.util.List;

import static edu.harvard.catalyst.scheduler.util.MiscUtil.q;
import static edu.harvard.catalyst.scheduler.util.MiscUtil.showDateTime;

public class LevelOfServiceReportDTO implements CsvAbleDTO {

  private Integer visitId;
  private String studyName;
  private String visitName;
  private String nursing;
  private String nutrition;
  private String processing;
  private String setup;
  private String visitType;
  private Date checkInDate;
  private Date checkOutDate;
  private AppointmentStatusReason checkOutReason;
  private String appointmentStatus;
  private Integer duration;
  private String resourceName;
  
  public LevelOfServiceReportDTO() {
  }
  
  public Integer getVisitId() {
    return visitId;
  }

  public void setVisitId(Integer visitId) {
    this.visitId = visitId;
  }

  public String getStudyName() {
    return studyName;
  }
  public void setStudyName(String studyName) {
    this.studyName = studyName;
  }
  
  public String getVisitName() {
    return visitName;
  }
  public void setVisitName(String visitName) {
    this.visitName = visitName;
  }
  
  public String getNursing() {
    return nursing;
  }
  public void setNursing(String nursing) {
    this.nursing = nursing;
  }
  
  public String getNutrition() {
    return nutrition;
  }
  public void setNutrition(String nutrition) {
    this.nutrition = nutrition;
  }
  
  public String getProcessing() {
    return processing;
  }
  public void setProcessing(String processing) {
    this.processing = processing;
  }
  
  public String getSetup() {
    return setup;
  }
  public void setSetup(String setup) {
    this.setup = setup;
  }
  
  public Date getCheckInDate() {
    return checkInDate;
  }

  public void setCheckInDate(Date checkInDate) {
    this.checkInDate = checkInDate;
  }

  public Date getCheckOutDate() {
    return checkOutDate;
  }

  public void setCheckOutDate(Date checkOutDate) {
    this.checkOutDate = checkOutDate;
  }

  public AppointmentStatusReason getCheckOutReason() {
    return checkOutReason;
  }

  public void setCheckOutReason(AppointmentStatusReason checkOutReason) {
    this.checkOutReason = checkOutReason;
  }

  public String getAppointmentStatus() {
    return appointmentStatus;
  }

  public void setAppointmentStatus(String appointmentStatus) {
    this.appointmentStatus = appointmentStatus;
  }

  public String getVisitType() {
    return visitType;
  }

  public void setVisitType(String visitType) {
    this.visitType = visitType;
  }

  public Integer getDuration() {
    return duration;
  }

  public void setDuration(Integer duration) {
    this.duration = duration;
  }

  public String getResourceName() {
    return resourceName;
  }

  public void setResourceName(String resourceName) {
    this.resourceName = resourceName;
  }

  @Override
  public String toCsvHeaders() {
    return "Study Name,Visit Name,Visit Type,Nursing,Nutrition,Processing,Setup,Appointment Status,Check In Date,Check Out Date,Check Out Reason";
  }

  @Override
  public List<String> toCsvRows(List<?> dtoList) {

    List<String> result = Lists.newArrayList();
    result.add(toCsvHeaders() + "\n");

    for (Object object: dtoList) {
      LevelOfServiceReportDTO d = (LevelOfServiceReportDTO) object;

      List<String> columns = Lists.newArrayList();

      String reason = d.checkOutReason == null
              ? "" : d.checkOutReason.getName();

      columns.add(q(d.studyName));
      columns.add(q(d.visitName));
      columns.add(q(d.visitType));
      columns.add(q(d.nursing));
      columns.add(q(d.nutrition));
      columns.add(q(d.processing));
      columns.add(q(d.setup));
      columns.add(q(d.appointmentStatus));
      columns.add(q(showDateTime(d.checkInDate)));
      columns.add(q(showDateTime(d.checkOutDate)));
      columns.add(q(reason));

      String rows = Joiner.on(",").join(columns);
      result.add(rows + "\n");
    }
    return result;
  }
}