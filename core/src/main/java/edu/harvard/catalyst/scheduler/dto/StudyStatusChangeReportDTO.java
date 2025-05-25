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

public class StudyStatusChangeReportDTO implements CsvAbleDTO {

	private String studyName;
	private String localId;
	private String pi;
	private String studyStatus;
	private Date dateStatusChange;
	private String userFirstName;
	private String userMiddleName;
	private String userLastName;
	private int studyId;
	
	
    public StudyStatusChangeReportDTO() {
    }

    public int getStudyId() {
		return studyId;
	}

    public void setStudyId(int studyId) {
		this.studyId = studyId;
	}

	public String getStudyName(){
    	return studyName;
    }
    	
    public void setStudyName(String studyName){
    	this.studyName = studyName;
    }
   
   public String getLocalId() {
        return localId;
    }
   
   public void setLocalId( String localId){
	   this.localId = localId;
   }
    
    public String getPi(){
    	return pi;
    }
    
    public void setPi(String pi){
    	this.pi = pi;
    }
    
   public String getStudyStatus(){
	   return studyStatus;
   }
   
    public void setStudyStatus(String studyStatus) {
        this.studyStatus = studyStatus;
    }
    
    public Date getDateStatusChange(){
    	return dateStatusChange;
    }

    public void setDateStatusChange(Date dateStatusChange) {
        this.dateStatusChange = dateStatusChange;
    }
    
	public void setUserFirstName(String userFirstName) {
		this.userFirstName = userFirstName;
	}

		public String getUserFirstName() {
		return userFirstName;
	}
	
	public String getUserMiddleName() {
		return userMiddleName;
	}

	public void setUserMiddleName(String userMiddleName) {
		this.userMiddleName = userMiddleName;
	}

	public String getUserLastName() {
		return userLastName;
	}

	
	public void setUserLastName(String userLastName) {
		this.userLastName = userLastName;
	}

	@Override
	public String toCsvHeaders() {
		return "Local ID,PI,Study Status, Date of Status Change, User";
	}

	@Override
	public List<String> toCsvRows(List<?> dtoList) {
		List<String> result = Lists.newArrayList();
		result.add(toCsvHeaders() + "\n");

		for (Object object: dtoList) {
			StudyStatusChangeReportDTO d = (StudyStatusChangeReportDTO) object;

			List<String> columns = Lists.newArrayList();

			columns.add(q(d.localId));
			columns.add(q(d.pi));
			columns.add(q(d.studyStatus));
			columns.add(q(showDateTime(d.dateStatusChange)));
			columns.add(q(fullName(
					d.userFirstName,
					d.userMiddleName,
					d.userLastName)));

			String rows = Joiner.on(",").join(columns);
			result.add(rows + "\n");
		}
		return result;
	}
}
   
   
    