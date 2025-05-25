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

DROP TABLE IF EXISTS `decrypted_subject`;
CREATE TABLE `decrypted_subject` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `active` tinyint(1) NOT NULL DEFAULT '0',
  `city` varchar(255) DEFAULT NULL,
  `created_date` datetime DEFAULT NULL,
  `ethnicity` int(11) DEFAULT NULL,
  `first_name` varchar(255) DEFAULT NULL,
  `full_name` varchar(255) DEFAULT NULL,
  `gender` int(11) DEFAULT NULL,
  `gender_empi` varchar(75) DEFAULT NULL,
  `last_name` varchar(255) DEFAULT NULL,
  `middle_name` varchar(255) DEFAULT NULL,
  `primary_contact_number` varchar(255) DEFAULT NULL,
  `race` int(11) DEFAULT NULL,
  `secondary_contact_number` varchar(255) DEFAULT NULL,
  `secure` tinyint(1) NOT NULL DEFAULT '0',
  `state` int(11) DEFAULT NULL,
  `street_address1` varchar(255) DEFAULT NULL,
  `street_address2` varchar(255) DEFAULT NULL,
  `zip` varchar(255) DEFAULT NULL,
  `birthdate` date DEFAULT NULL,
  `country` int(11) DEFAULT NULL,
  `comment` longtext,
  `archival_status` varchar(255) DEFAULT NULL,
  `latest_ssot_refresh` datetime DEFAULT NULL,
  `puid` varchar(255) DEFAULT NULL,
  `gender_enum` varchar(255) NOT NULL DEFAULT 'UNREPORTED',
  PRIMARY KEY (`id`),
  KEY `FK90AA552C586F81EF1` (`ethnicity`),
  KEY `FK90AA552C4CCA22931` (`state`),
  KEY `FK90AA552C21EA7A311` (`gender`),
  KEY `FK90AA552CF1F48A111` (`race`),
  KEY `FK90AA552C9722B05D1` (`country`),
  CONSTRAINT `1subject_0__gender` FOREIGN KEY (`gender`) REFERENCES `gender` (`id`),
  CONSTRAINT `1subject_1__state` FOREIGN KEY (`state`) REFERENCES `state` (`id`),
  CONSTRAINT `1subject_2__ethnicity` FOREIGN KEY (`ethnicity`) REFERENCES `ethnicity` (`id`),
  CONSTRAINT `1subject_3__country` FOREIGN KEY (`country`) REFERENCES `country` (`id`),
  CONSTRAINT `1subject_4__race` FOREIGN KEY (`race`) REFERENCES `race` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `decrypted_subject_mrn`;
CREATE TABLE `decrypted_subject_mrn` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `subject` int(11) NOT NULL,
  `mrn` varchar(255) NOT NULL,
  `site` varchar(255) default NULL,
  `status` VARCHAR(15) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `1FK_subject_mrn_site_id1` (`subject`,`mrn`, `site`),
  KEY `1FK_subject_mrn_id2` (`subject`),
  CONSTRAINT `1subject_mrn_0__subject` FOREIGN KEY (`subject`) REFERENCES `subject` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
