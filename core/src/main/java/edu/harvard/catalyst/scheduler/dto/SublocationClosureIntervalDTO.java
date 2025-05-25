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
import java.util.List;

import edu.harvard.catalyst.scheduler.entity.SublocationClosureInterval;


public class SublocationClosureIntervalDTO extends AuthorizedDTO {

  private List<SublocationClosureInterval> sublocationClosureIntervals;
  
  private Date startTime;
  private Date endTime;
  private String reason;
  private Integer userId;
  private Integer sublocationId;
  private Integer sublocationClosureIntervalId;


  public SublocationClosureIntervalDTO() {
  }
  
  
  public List<SublocationClosureInterval> getSublocationClosureIntervals() {
    return sublocationClosureIntervals;
  }

  public void setSublocationClosureIntervals(List<SublocationClosureInterval> sublocationClosureIntervals) {
    this.sublocationClosureIntervals = sublocationClosureIntervals;
  }


  public Date getStartTime() {
    return startTime;
  }
  public void setStartTime(Date startTime) {
    this.startTime = startTime;
  }

  public Date getEndTime() {
    return endTime;
  }
  public void setEndTime(Date endTime) {
    this.endTime = endTime;
  }

  public String getReason() {
    return reason;
  }
  public void setReason(String reason) {
    this.reason = reason;
  }

  public Integer getUserId() {
    return userId;
  }
  public void setUserId(Integer userId) {
    this.userId = userId;
  }

  public Integer getSublocationId() {
    return sublocationId;
  }
  public void setSublocationId(Integer sublocationId) {
    this.sublocationId = sublocationId;
  }

  public Integer getSublocationClosureIntervalId() {
    return sublocationClosureIntervalId;
  }
  public void setSublocationClosureIntervalId(Integer sublocationClosureIntervalId) {
    this.sublocationClosureIntervalId = sublocationClosureIntervalId;
  }
  
}
