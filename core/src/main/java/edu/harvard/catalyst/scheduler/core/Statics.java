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
package edu.harvard.catalyst.scheduler.core;

public final class Statics {


    private Statics(){
        
    }
    public static final int MAX_255 = 255;

    public static final String STR_SSL_FAILS = "Unable to connect to external service. ";
    public static final String STR_EMPI_FAILS = "Unable to connect to external service. ";

    public static final String STR_BUT_SCHED_WORKS = "Data may not be fully up-to-date (error XX).";
    public static final String STR_AND_SCHED_FAILS_TOO = "At this time subject details cannot be displayed or updated, " +
            "and subject cannot be added to a study. Please try again later (error XX).";

    public static final String CODE_SSL_FAILS_BUT_SCHED_WORKS = "19";
    public static final String CODE_EMPI_FAILS_BUT_SCHED_WORKS = "53";
    public static final String CODE_SSL_FAILS_AND_SCHED_FAILS = "60";
    public static final String CODE_EMPI_FAILS_AND_SCHED_FAILS = "41";

    public static final String NA = "N/A";
    public static final String NO_SUBJECT_ASSIGNED = "No Subject Assigned";
    public static final String NASubjectSuffixForEventClass = "SubjectNA";

    public static final String EMPTY_VALUE = "";
	public static final String TAB = "\t";
	
	public static final String AUTHENTICATED_USER = "authenticatedUser";
	
	public static final String AUDIT_SUBJECT_SEARCH = "SUBJECT SEARCH";
	public static final String AUDIT_SUBJECT_CREATE = "CREATE SUBJECT";
	public static final String AUDIT_SUBJECT_UPDATE = "UPDATE SUBJECT";
	public static final String AUDIT_SUBJECT_VIEW = "VIEW SUBJECT";
	public static final String AUDIT_SUBJECT_ACTIVATE = "ACTIVATED SUBJECT";
    public static final String AUDIT_SUBJECT_DEACTIVATE = "DEACTIVATED SUBJECT";
    public static final String AUDIT_SUBJECT_STATUS = "Clicked Change Status link for Subject ";
    public static final String AUDIT_SUBJECT_STUDY = "Clicked Add to Study link ";
	public static final String AUDIT_CHANGE_PASSWD = "USER PASSWORD CHANGED";

	public static final String AUDIT_USER_CREATE = "CREATE USER";
    public static final String AUDIT_USER_UPDATE = "UPDATE USER";
    public static final String AUDIT_USER_VIEW = "VIEW USER";
    public static final String AUDIT_USER_ACTIVATE = "ACTIVATE USER";
    public static final String AUDIT_USER_DEACTIVATE = "DEACTIVATE USER";
    
    public static final String AUDIT_STUDY_CREATE = "CREATE STUDY";
    public static final String AUDIT_STUDY_UPDATE = "UPDATE STUDY";
    public static final String AUDIT_STUDY_VIEW = "VIEW STUDY";

    public static final String AUDIT_STUDY_OPEN = "OPEN";
    public static final String AUDIT_STUDY_CLOSED = "CLOSED";
    public static final String AUDIT_STUDY_PENDING = "PENDING";
    public static final String AUDIT_STUDY_IRB_PROCESS = "IRB_PROCESS";

    public static final String AUDIT_VISIT_CREATE = "CREATE VISIT";
    public static final String AUDIT_VISIT_COMMENT_CREATE = "CREATE VISIT COMMENT";
    public static final String AUDIT_VISIT_DELETE = "DELETE VISIT";
    public static final String AUDIT_VISIT_UPDATE = "UPDATE VISIT";
    public static final String AUDIT_TEMPLATE_RESOURCE_UPDATE = "UPDATE TEMPLATE RESOURCE";
    public static final String AUDIT_TEMPLATE_RESOURCE_CREATE = "UPDATE TEMPLATE CREATE";
    public static final String AUDIT_TEMPLATE_RESOURCE_DELETE = "UPDATE TEMPLATE DELETE";
    public static final String AUDIT_VISIT_VIEW = "VIEW VISIT";
    public static final String AUDIT_VISIT_COPY = "COPY VISIT";
    public static final String AUDIT_VISIT_ACTIVATE = "ACTIVATE VISIT";
    public static final String AUDIT_VISIT_DEACTIVATE = "DEACTIVATE VISIT";
    public static final String AUDIT_VISIT_TEMPLATE_COMMENT = "COMMENT VISIT TEMPLATE";
    public static final String AUDIT_VISIT_TEMPLATE_APPROVED = "APPROVED VISIT TEMPLATE";
    
    public static final String AUDIT_VISIT_TEMPLATE_APPROVED_EMAIL_SENT = "APPROVED VISIT TEMPLATE EMAIL SENT";
    
    public static final String AUDIT_STUDY_SUBJECT_CREATE = "ADD STUDY SUBJECT";
    public static final String AUDIT_STUDY_MEMBER_CREATE = "ADD STUDY MEMBER";
    public static final String AUDIT_STUDY_SUBJECT_DEACTIVATE = "DEACTIVATE STUDY SUBJECT";
    public static final String AUDIT_STUDY_SUBJECT_ACTIVATE = "ACTIVATE STUDY SUBJECT";
    public static final String AUDIT_STUDY_MEMBER_DEACTIVATE = "DEACTIVATE STUDY MEMBER";
    public static final String AUDIT_STUDY_MEMBER_ACTIVATE = "ACTIVATE STUDY MEMBER";

    public static final String AUDIT_RESOURCE_SUBLOCATION_CREATE = "RESOURCE SUBLOCATION CREATED";
    public static final String AUDIT_RESOURCE_CREATE = "RESOURCE CREATED";
    public static final String AUDIT_RESOURCE_SUBLOCATION_CLOSURE_CREATE = "CREATE SUBLOCATION CLOSURE";
    public static final String AUDIT_RESOURCE_SUBLOCATION_CLOSURE_DELETE = "DELETE SUBLOCATION CLOSURE";
    public static final String AUDIT_RESOURCE_ALTERNATE_CREATE = "CREATE RESOURCE ALTERNATE";
    public static final String AUDIT_RESOURCE_ALTERNATE_DELETE = "DELETE RESOURCE ALTERNATE";
    public static final String AUDIT_RESOURCE_SHARED_RESOURCE_DELETE = "DELETE SHARED RESOURCE";
    public static final String AUDIT_RESOURCE_SHARED_RESOURCE_ADD = "ADD SHARED RESOURCE";
    public static final String AUDIT_RESOURCE_SHARED_RESOURCE_MODIFY = "MODIFIED SHARED RESOURCE";
    public static final String AUDIT_RESOURCE_TEMPADJ_CREATE = "CREATE RESOURCE TEMP ADJUSTMENT";
    public static final String AUDIT_RESOURCE_TEMPADJ_UPDATE = "UPDATE RESOURCE TEMP ADJUSTMENT";
    public static final String AUDIT_RESOURCE_TEMPADJ_DELETE = "DELETE RESOURCE TEMP ADJUSTMENT";
    public static final String AUDIT_RESOURCE_DEFAULT_CREATE = "CREATE RESOURCE DEFAULT SCHEDULE";
    public static final String AUDIT_RESOURCE_DEFAULT_UPDATE = "UPDATE RESOURCE DEFAULT SCHEDULE";
    public static final String AUDIT_RESOURCE_DEFAULT_DELETE = "DELETE RESOURCE DEFAULT SCHEDULE";
    public static final String AUDIT_RESOURCE_UPDATE = "UPDATE RESOURCE";
    public static final String AUDIT_RESOURCE_VIEW = "VIEW RESOURCE";
    public static final String AUDIT_RESOURCE_ANNOTATIONS_ADDED = "RESOURCE ANNOTATIONS ADDED";

    public static final String CHECKED_IN_APPT_STATUS = "Checked-In";
    public static final String CHECKED_OUT_APPT_STATUS = "Checked-Out";
    public static final String CANCELLED_APPT_STATUS = "Cancellation";

    public static final String SUNDAY = "SUN";
    public static final String MONDAY = "MON";
    public static final String TUESDAY = "TUE";
    public static final String WEDNESDAY = "WED";
    public static final String THURSDAY = "THU";
    public static final String FRIDAY = "FRI";
    public static final String SATURDAY = "SAT";
    
    public static final String[] DAYS_OF_WEEK = {SUNDAY, MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY};

    public static final String CREATE_CONSTRAINT_VIOLATION = "createSubject() constraint violation";
    public static final String UPDATE_SUBJECT_CONSTRAINT_VIOLATION = "updateSubject() constraint violation";
    public static final String SUBJECT_ALREADY_ENROLLED = "This subject is already enrolled in this study.";

    public static final String MRN_ALREADY_EXISTS_MISMATCH_CREATE = "MRN already exists -- cancelling creation of subject";
    public static final String MRN_ALREADY_EXISTS_MISMATCH_UPDATE = "MRN already exists -- cancelling update of subject";

    public static final String SIMILAR_MRN_ALREADY_EXISTS_MISMATCH_CREATE = "Similar MRN already exists -- cancelling creation of subject";

    public static final String MRN_NOT_FOUND_FOR_SUBJECT = "Updating Subject Failed, the Subject's previous MRN was not found";

    public static final String INSUFFICIENT_PASSWORD = "Insufficient Password";

    public static final String ECOMMONS_ID_EXISTS = "Ecommons ID already in system";
    public static final String EMAIL_EXISTS = "Email already in system";

}
