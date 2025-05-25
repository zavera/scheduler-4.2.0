--
-- Copyright (c) 2015-2016, President and Fellows of Harvard College
--
-- Redistribution and use in source and binary forms, with or without
-- modification, are permitted provided that the following conditions are met:
--
-- 1. Redistributions of source code must retain the above copyright
-- notice, this list of conditions and the following disclaimer.
--
-- 2. Redistributions in binary form must reproduce the above copyright
-- notice, this list of conditions and the following disclaimer in the
-- documentation and/or other materials provided with the distribution.
--
-- 3. The name of the author may not be used to endorse or promote products
-- derived from this software without specific prior written permission.
--
-- THIS SOFTWARE IS PROVIDED BY THE AUTHOR "AS IS" AND ANY EXPRESS OR IMPLIED
-- WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
-- MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO
-- EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
-- SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED
-- TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
-- PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
-- LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
-- NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
-- SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
--

-- QUERY_SUBJECTS_NOT_ADDED_TO_ANY_STUDY_RECENTLY_AND_NEVER_SCHEDULED

SELECT
su.id, 
sm.mrn ,
su.created_date, 
su.first_name, 
su.last_name, 
su.birthdate, 
su.created_date, 
su.street_address1, 
su.city, 
sta.name as state, 
su.zip, 
c.name as country, 
su.primary_contact_number, 
ge.code as gender, 
  (SELECT MAX(date) FROM activity_log 
      WHERE affected_subject = su.id 
      AND action_performed = 'ADD SUBJECT TO STUDY') AS last_assigned_to_a_study 
FROM subject su 
  LEFT JOIN subject_mrn as sm ON sm.subject = su.id 
  LEFT JOIN gender ge ON ge.id = su.gender 
  LEFT JOIN country c ON c.id = su.country 
  LEFT JOIN state as sta ON sta.id = su.state 
    WHERE 
  (SELECT COUNT(*) FROM booked_visit bv 
   JOIN subject_mrn sm2 ON bv.subject_mrn = sm2.id
    WHERE sm2.subject = su.id) = 0
   AND 
  (SELECT COUNT(*) FROM activity_log 
  WHERE date > DATE_SUB(now(), INTERVAL 3 MONTH)
    AND affected_subject = su.id 
    AND action_performed = 'ADD SUBJECT TO STUDY') = 0;


-- QUERY_STUDIES_ASSIGNED_TO_SUBJECT_LONG_AGO_AND_NEVER_SCHEDULED

-- this query shows only the manually created subject. The reason is that the activity_log table is empty when the
-- demo-data.sql file is loaded, and the activity_log table needs to be populated manually (as for the manually created
-- subjects )

SELECT 
su.id as subject_id, 
sm.mrn ,
su.created_date, 
su.first_name, 
su.last_name, 
su.birthdate, 
su.created_date, 
su.street_address1, 
su.city, 
sta.name as state, 
su.zip, 
c.name as country, 
su.primary_contact_number ,
ge.code as gender, 
ss.id AS study_subject_id, 
ss.study AS study_id, 
st.name AS study_name, 
al.date AS date_added_to_study 
  FROM subject su 
    LEFT JOIN subject_mrn as sm ON sm.subject = su.id 
    JOIN study_subject ss ON ss.subject_mrn = sm.id 
    JOIN activity_log al ON (al.affected_subject = su.id AND al.affected_study = ss.study) 
    JOIN study st ON ss.study = st.id 
    LEFT JOIN gender ge ON ge.id = su.gender 
    LEFT JOIN country c ON c.id = su.country 
    LEFT JOIN state as sta ON sta.id = su.state 
  WHERE al.action_performed = 'ADD SUBJECT TO STUDY' 
  AND al.date < DATE_SUB(now(), INTERVAL 3 MONTH)
  AND (SELECT COUNT(*) FROM booked_visit bv 
    WHERE bv.subject_mrn = sm.id and bv.study = st.id) = 0;;
