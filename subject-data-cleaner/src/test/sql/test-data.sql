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

-- how to set up the test data for SubjectDataCleaner

-- run demo-structure.sql
-- run demo-seed.sql
-- DO NOT run demo-data.sql
-- DO NOT run fakeSubjects
-- run update.sql
-- run test-data.sql (this file)
-- run test-queries.sql (if desired)
-- run SubjectDataCleaner (the program under test)

-- when working with institution data snapshots:
-- run <institution>.sql
-- run <institution>-update.sql
-- run fakeSubjects
-- run update.sql
-- DO NOT run test-data.sql
-- run SubjectDataCleaner (the program under test)

/*!40014 SET FOREIGN_KEY_CHECKS=0 */;

truncate study;
truncate subject;
truncate subject_mrn;
truncate study_subject;
truncate activity_log;

--
-- Studies
--

INSERT INTO study (id, study_status, name, short_title)
values (101, 1, 'dev test study 1', 'study 1');

INSERT INTO study (id, study_status, name, short_title)
values (102, 2, 'dev test study 2', 'study 2');

INSERT INTO study (id, study_status, name, short_title)
values (103, 2, 'dev test study 3', 'study 3');

INSERT INTO study (id, study_status, name, short_title)
values (104, 2, 'dev test study 4', 'study 4');


--
-- Subjects
--

INSERT INTO subject (id, active, city, created_date, ethnicity, first_name, full_name, gender, last_name, middle_name, primary_contact_number, race, secondary_contact_number, secure, state, street_address1, street_address2, zip, birthdate, country, comment, archival_status)
VALUES
  (101, 1,'','2016-01-01', 1,'Aaaaa','Aaaaa',1,'Aaaaa','Aaaaa','555-555-1212',1,'555-555-1212',1,1,"","",'Aaaaa','1920-01-01',1,"",null);
insert into subject_mrn (id, subject, mrn)
values (101, 101, '00101');

INSERT INTO subject (id, active, city, created_date, ethnicity, first_name, full_name, gender, last_name, middle_name, primary_contact_number, race, secondary_contact_number, secure, state, street_address1, street_address2, zip, birthdate, country, comment, archival_status)
VALUES
  (102, 1,'','2016-01-02', 1,'Bbbbb','Bbbbb',1,'Bbbbb','Bbbbb','555-555-1212',1,'555-555-1212',1,1,"","",'Bbbbb','1920-01-02',1,"",null);
insert into subject_mrn (id, subject, mrn)
values (102, 102, '00102');

INSERT INTO subject (id, active, city, created_date, ethnicity, first_name, full_name, gender, last_name, middle_name, primary_contact_number, race, secondary_contact_number, secure, state, street_address1, street_address2, zip, birthdate, country, comment, archival_status)
VALUES
  (103, 1,'','2016-01-03', 1,'Ccccc','Ccccc',1,'Ccccc','Ccccc','555-555-1212',1,'555-555-1212',1,1,"","",'Ccccc','1920-01-03',1,"",null);
insert into subject_mrn (id, subject, mrn)
values (103, 103, '00103');

INSERT INTO subject (id, active, city, created_date, ethnicity, first_name, full_name, gender, last_name, middle_name, primary_contact_number, race, secondary_contact_number, secure, state, street_address1, street_address2, zip, birthdate, country, comment, archival_status)
VALUES
  (104, 1,'','2016-02-02', 1,'Ccccc','Ccccc',1,'Ccccc','Ccccc','555-555-1212',1,'555-555-1212',1,1,"","",'Ccccc','1940-05-06',1,"",null);
insert into subject_mrn (id, subject, mrn)
values (104, 104, '00104');

-- comment in to generate an exception
-- insert into subject_mrn (id, subject, mrn)
-- values (103, 102, '00103');


--
-- QUERY_SUBJECTS_NOT_ADDED_TO_ANY_STUDY_RECENTLY_AND_NEVER_SCHEDULED
--

INSERT INTO study_subject (id, active, study, subject_mrn)
values (101, 1, 101, 101);

INSERT INTO activity_log (action_performed, affected_study, affected_subject, date)
values ('ADD SUBJECT TO STUDY', 101, 101, '2016-05-01');


INSERT INTO study_subject (id, active, study, subject_mrn)
values (103, 1, 103, 103);

INSERT INTO activity_log (action_performed, affected_study, affected_subject, date)
values ('ADD SUBJECT TO STUDY', 103, 103, '2015-01-01');


INSERT INTO study_subject (id, active, study, subject_mrn)
values (104, 1, 104, 103);

INSERT INTO activity_log (action_performed, affected_study, affected_subject, date)
values ('ADD SUBJECT TO STUDY', 104, 103, '2015-06-01');

--
-- QUERY_STUDIES_ASSIGNED_TO_SUBJECT_LONG_AGO_AND_NEVER_SCHEDULED



/*!40014 SET FOREIGN_KEY_CHECKS=1 */;
