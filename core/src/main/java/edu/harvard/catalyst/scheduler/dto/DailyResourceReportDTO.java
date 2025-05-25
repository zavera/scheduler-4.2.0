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

public class DailyResourceReportDTO implements CsvAbleDTO {

  protected Integer resourceTypeId;
  protected String resourceTypeName;
  protected String resourceName;
  protected Date scheduledStartTime;
  protected Date scheduledEndTime;
  protected String localId;
  protected String irb;
  protected String visitName;
  protected String visitTypeName;
  protected String subjectFirstName;
  protected String subjectMiddleName;
  protected String subjectLastName;
  protected String mrn; 
  protected String comment;
  protected Integer visitId;
  

  public DailyResourceReportDTO() {
  }

    public Integer getResourceTypeId() {
      return resourceTypeId;
    }
    public void setResourceTypeId(Integer resourceTypeId) {
      this.resourceTypeId = resourceTypeId;
    }
    
    public String getResourceTypeName() {
      return resourceTypeName;
    }
    public void setResourceTypeName(String resourceTypeName) {
      this.resourceTypeName = resourceTypeName;
    }
  
    public String getResourceName() {
      return resourceName;
    }
    public void setResourceName(String resourceName) {
      this.resourceName = resourceName;
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

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}
    public Integer getVisitId() {
        return visitId;
    }

    public void setVisitId(Integer visitId) {
        this.visitId = visitId;
    }

  @Override
  public String toCsvHeaders() {
    return "Resource Name,Resource Type,Subject Name,MRN,Local ID," +
            "IRB #,Visit ID,Visit Name,Resource Start Time," +
            "Resource End Time,Resource Usage Duration,Comment";
  }

  @Override
  public List<String> toCsvRows(List<?> dtoList) {

      List<String> result = Lists.newArrayList();
      result.add(toCsvHeaders() + "\n");

      for (Object object: dtoList) {
          DailyResourceReportDTO d = (DailyResourceReportDTO) object;

          List<String> columns = Lists.newArrayList();

          columns.add(q(d.resourceName));
          columns.add(q(d.resourceTypeName));
          columns.add(q(fullName(
                  d.subjectFirstName,
                  d.subjectMiddleName,
                  d.subjectLastName)));
          columns.add(q(d.mrn));
          columns.add(q(d.localId));
          columns.add(q(d.irb));
          columns.add(q(d.visitId));
          columns.add(q(d.visitName));
          columns.add(q(showDateTime(d.scheduledStartTime)));
          columns.add(q(showDateTime(d.scheduledEndTime)));
          columns.add(q(formatEndMinusStart(
                  d.scheduledStartTime, d.scheduledEndTime)));
          columns.add(q(d.comment));

          String rows = Joiner.on(",").join(columns);
          result.add(rows + "\n");
      }
      return result;
  }
}
