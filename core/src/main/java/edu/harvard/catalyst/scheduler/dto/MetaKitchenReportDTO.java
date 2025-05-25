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
import static edu.harvard.catalyst.scheduler.util.MiscUtil.fullName;
import static edu.harvard.catalyst.scheduler.util.MiscUtil.q;

public class MetaKitchenReportDTO implements CsvAbleDTO {

  private Integer subjectId;
  private String subjectFirstName;
  private String subjectMiddleName;
  private String subjectLastName;
  private String birthdate;
  private String mrn;
  private String genderName;
  private String localId;
  private String irb;
  private String visitName;
  private String resourceName;
  private String room;
  private Date checkInTime;
  private Date checkOutTime;
  private Date scheduledStartTime;
  private Date scheduledEndTime;

  

  public MetaKitchenReportDTO() {
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
  public void setScheduledEndTime(Date scheduledEndTime) {
    this.scheduledEndTime = scheduledEndTime;
  }

  public String getBirthdate() {
    return birthdate;
  }
  public void setBirthdate(String birthdate) {
    this.birthdate = birthdate;
  }

  public String getRoom() {
    return room;
  }

  public void setRoom(String room) {
    this.room = room;
  }

  @Override
  public String toCsvHeaders() {
    return "Meal Start Date/Time,Resource Name,Room,Visit Name,Visit Start Date/Time,Comment";
  }

    @Override
    public List<String> toCsvRows(List<?> dtoList, String name) {
    if (name.equals("meta_kitchen_flat")) {
        return toCsvRowsFlat(dtoList);
    }
    else if (name.equals("meta_kitchen_by_time")) {
        return toCsvRowsByTime(dtoList);
    }
      else {
          return toCsvRows(dtoList);
      }
    }

    @Override
    public List<String> toCsvRows(List<?> dtoList) {

      List<String> result = Lists.newArrayList();

      int previousSubjectId = -1;

      for (Object object: dtoList) {
          MetaKitchenReportDTO d = (MetaKitchenReportDTO) object;

          List<String> columns = Lists.newArrayList();

          if (isNullOrEmpty(d.resourceName)) {
              continue;
          }

          int currentSubjectId = d.subjectId;
          if (currentSubjectId != previousSubjectId) {

              result.add(q(fullName(  d.subjectFirstName,
                                      d.subjectMiddleName,
                                      d.subjectLastName)) +
                         ",,,,MRN," + q(d.mrn) + "\n");

              result.add(",,,,Local ID," + q(d.localId) + "\n");
              result.add(",,,,DOB," + q(d.birthdate) + "\n");
              result.add(toCsvHeaders() + "\n");

          }
          previousSubjectId = currentSubjectId;

          columns.add(q(showDateTime(d.scheduledStartTime)));
          columns.add(q(d.resourceName));
          columns.add(q(d.room));
          columns.add(q(d.visitName));
          columns.add(q(showDateTime(d.checkInTime) + " - " +
                      showDateTime(d.checkOutTime)));

          String row = Joiner.on(",").join(columns);
          result.add(" \n" + row + "\n");
      }
      return result;
  }

  List<String> toCsvRowsFlat(List<?> dtoList) {
      List<String> result = Lists.newArrayList();

      String flatHeader = "Subject Name,Meal Start Date/Time," +
              "Local ID,Visit Name,Resource Name,MRN,DOB,Room," +
              "Visit Start Date/Time,Comment";

      result.add(flatHeader + "\n");

      for (Object object: dtoList) {
          MetaKitchenReportDTO d = (MetaKitchenReportDTO) object;

          if (isNullOrEmpty(d.resourceName)) {
              continue;
          }

          List<String> columns = Lists.newArrayList();

          columns.add(q(fullName(
                  d.subjectFirstName,
                  d.subjectMiddleName,
                  d.subjectLastName)));
          columns.add(q(showDateTime(d.scheduledStartTime)));
          columns.add(q(d.localId));
          columns.add(q(d.visitName));
          columns.add(q(d.resourceName));
          columns.add(q(d.mrn));
          columns.add(q(d.birthdate));
          columns.add(q(d.room));
          columns.add(q(showDateTime(d.checkInTime) + " - " +
                  showDateTime(d.checkOutTime)));

          String rows = Joiner.on(",").join(columns);
          result.add(rows + "\n");
      }
      return result;
    }
    List<String> toCsvRowsByTime(List<?> dtoList) {
        List<String> result = Lists.newArrayList();

        String header = "Meal Start Date/Time,Subject," +
                "Local ID,Visit Name,Resource Name,MRN,DOB,Room," +
                "Visit Start/End Time,Comment";

        result.add(header + "\n");

        for (Object object: dtoList) {
            MetaKitchenReportDTO d = (MetaKitchenReportDTO) object;

            if (isNullOrEmpty(d.resourceName)) {
                continue;
            }

            List<String> columns = Lists.newArrayList();

            columns.add(q(showDateTime(d.scheduledStartTime)));
            columns.add(q(fullName(
                    d.subjectFirstName,
                    d.subjectMiddleName,
                    d.subjectLastName)));
            columns.add(q(d.localId));
            columns.add(q(d.visitName));
            columns.add(q(d.resourceName));
            columns.add(q(d.mrn));
            columns.add(q(d.birthdate));
            columns.add(q(d.room));
            columns.add(q(showDateTime(d.checkInTime) + " - " +
                    showDateTime(d.checkOutTime)));

            String rows = Joiner.on(",").join(columns);
            result.add(rows + "\n");
        }
        return result;
    }

}
