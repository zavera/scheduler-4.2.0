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

-- MySQL dump 10.13  Distrib 8.0.16, for macos10.14 (x86_64)
--
-- Host: 127.0.0.1    Database: scheduler
-- ------------------------------------------------------
-- Server version	8.0.16

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
 SET NAMES utf8 ;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `activity_log`
--

DROP TABLE IF EXISTS `activity_log`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
 SET character_set_client = utf8mb4 ;
CREATE TABLE `activity_log` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `action_performed` varchar(255) DEFAULT NULL,
  `date` datetime DEFAULT NULL,
  `ip_address` varchar(255) DEFAULT NULL,
  `performing_user` int(11) DEFAULT NULL,
  `affected_user` int(11) DEFAULT NULL,
  `affected_subject` int(11) DEFAULT NULL,
  `booked_visit` int(11) DEFAULT NULL,
  `appointment_override_reason` int(11) DEFAULT NULL,
  `affected_study` int(11) DEFAULT NULL,
  `affected_visit` int(11) DEFAULT NULL,
  `changes_detail` longtext,
  `changes_detail_required_field` longtext,
  `affected_resource` int(11) DEFAULT NULL,
  `affected_sublocation` int(11) DEFAULT NULL,
  `affected_subject_mrn` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK611AA614EDA290BE` (`affected_subject`),
  KEY `FK611AA614DB1FAF0` (`affected_user`),
  KEY `FK611AA61429DF80A3` (`performing_user`),
  KEY `FK611AA614A8566378` (`affected_study`),
  KEY `FK611AA614955E5717` (`appointment_override_reason`),
  KEY `FK611AA61417B19716` (`affected_visit`),
  KEY `FK611AA6144267F50` (`affected_sublocation`),
  KEY `FK611AA61494C66E76` (`affected_resource`),
  KEY `FK611AA6147E838528` (`booked_visit`),
  KEY `activity_log_9__subject_mrn` (`affected_subject_mrn`),
  CONSTRAINT `activity_log_0__visit_template` FOREIGN KEY (`affected_visit`) REFERENCES `visit_template` (`id`),
  CONSTRAINT `activity_log_1__user` FOREIGN KEY (`performing_user`) REFERENCES `user` (`id`),
  CONSTRAINT `activity_log_2__sublocation` FOREIGN KEY (`affected_sublocation`) REFERENCES `sublocation` (`id`),
  CONSTRAINT `activity_log_3__booked_visit` FOREIGN KEY (`booked_visit`) REFERENCES `booked_visit` (`id`),
  CONSTRAINT `activity_log_4__resource` FOREIGN KEY (`affected_resource`) REFERENCES `resource` (`id`),
  CONSTRAINT `activity_log_5__appointment_override_reason` FOREIGN KEY (`appointment_override_reason`) REFERENCES `appointment_override_reason` (`id`),
  CONSTRAINT `activity_log_6__study` FOREIGN KEY (`affected_study`) REFERENCES `study` (`id`),
  CONSTRAINT `activity_log_7__user` FOREIGN KEY (`affected_user`) REFERENCES `user` (`id`),
  CONSTRAINT `activity_log_8__subject` FOREIGN KEY (`affected_subject`) REFERENCES `subject` (`id`),
  CONSTRAINT `activity_log_9__subject_mrn` FOREIGN KEY (`affected_subject_mrn`) REFERENCES `subject_mrn` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `appointment_override_reason`
--

DROP TABLE IF EXISTS `appointment_override_reason`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
 SET character_set_client = utf8mb4 ;
CREATE TABLE `appointment_override_reason` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `appointment_status`
--

DROP TABLE IF EXISTS `appointment_status`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
 SET character_set_client = utf8mb4 ;
CREATE TABLE `appointment_status` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) NOT NULL,
  `is_cancelled` int(1) NOT NULL DEFAULT '0',
  `is_checked_in` int(1) NOT NULL DEFAULT '0',
  `is_checked_out` int(1) NOT NULL DEFAULT '0',
  `is_hold` int(1) NOT NULL DEFAULT '0',
  `is_scheduled` int(1) NOT NULL DEFAULT '0',
  `is_open` int(1) NOT NULL DEFAULT '0',
  `is_active` int(1) NOT NULL DEFAULT '0',
  `is_serviced` int(1) NOT NULL DEFAULT '0',
  `is_serviceable` int(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `appointment_status_reason`
--

DROP TABLE IF EXISTS `appointment_status_reason`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
 SET character_set_client = utf8mb4 ;
CREATE TABLE `appointment_status_reason` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `appointment_status` int(11) NOT NULL,
  `name` varchar(255) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_appStatReason_name` (`name`),
  KEY `FK931C3691B55A8CF4` (`appointment_status`),
  CONSTRAINT `appointment_status_reason_0__appointment_status` FOREIGN KEY (`appointment_status`) REFERENCES `appointment_status` (`id`),
  CONSTRAINT `appointment_status_reason_1__appointment_status` FOREIGN KEY (`appointment_status`) REFERENCES `appointment_status` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `booked_resource`
--

DROP TABLE IF EXISTS `booked_resource`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
 SET character_set_client = utf8mb4 ;
CREATE TABLE `booked_resource` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `booked_visit` int(11) NOT NULL,
  `resource` int(11) DEFAULT NULL,
  `template_resource` int(11) DEFAULT NULL,
  `duration` int(11) DEFAULT NULL,
  `created_date` datetime DEFAULT NULL,
  `scheduled_end_time` datetime DEFAULT NULL,
  `scheduled_start_time` datetime DEFAULT NULL,
  `appointment_override_reason` int(11) DEFAULT NULL,
  `billable` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `FK1334B525F1F4F55B` (`resource`),
  KEY `FK1334B525FF7BB82A` (`template_resource`),
  KEY `FK1334B5257E838528` (`booked_visit`),
  KEY `FK1334B525955E5717` (`appointment_override_reason`),
  CONSTRAINT `booked_resource_1__template_resource` FOREIGN KEY (`template_resource`) REFERENCES `template_resource` (`id`),
  CONSTRAINT `booked_resource_2__resource` FOREIGN KEY (`resource`) REFERENCES `resource` (`id`),
  CONSTRAINT `booked_resource_3__appointment_override_reason` FOREIGN KEY (`appointment_override_reason`) REFERENCES `appointment_override_reason` (`id`),
  CONSTRAINT `booked_resource_4__template_resource` FOREIGN KEY (`template_resource`) REFERENCES `template_resource` (`id`),
  CONSTRAINT `booked_resource_5__booked_visit` FOREIGN KEY (`booked_visit`) REFERENCES `booked_visit` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `booked_visit`
--

DROP TABLE IF EXISTS `booked_visit`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
 SET character_set_client = utf8mb4 ;
CREATE TABLE `booked_visit` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `study` int(11) NOT NULL,
  `visit_template` int(11) NOT NULL,
  `name` varchar(255) DEFAULT NULL,
  `appointment_status` int(11) NOT NULL,
  `appointment_status_reason` int(11) DEFAULT NULL,
  `scheduled_start_time` datetime NOT NULL,
  `scheduled_end_time` datetime NOT NULL,
  `comment` varchar(255) DEFAULT NULL,
  `cancel_date` datetime DEFAULT NULL,
  `cancel_status` int(11) DEFAULT NULL,
  `check_in_date` datetime DEFAULT NULL,
  `check_out_date` datetime DEFAULT NULL,
  `cancel_status_reason` int(11) DEFAULT NULL,
  `checkout_status_reason` int(11) DEFAULT NULL,
  `ommitted_activities` tinyint(1) DEFAULT '0',
  `vary_duration` tinyint(1) DEFAULT '0',
  `scheduling_time` datetime DEFAULT NULL,
  `scheduling_flavor` varchar(50) DEFAULT NULL,
  `subject_mrn` int(11) DEFAULT NULL,
  `scheduling_user` int(11) DEFAULT NULL,
  `check_in_user` int(11) DEFAULT NULL,
  `check_out_user` int(11) DEFAULT NULL,
  `cancel_user` int(11) DEFAULT NULL,
  `visit_type` int(11) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKB42FCB944CCAB503` (`study`),
  KEY `FKB42FCB9493F01044` (`visit_template`),
  KEY `FKB42FCB94D5C7FFD7` (`appointment_status_reason`),
  KEY `FKB42FCB94B55A8CF4` (`appointment_status`),
  KEY `FKB42FCB94A9B6813E` (`checkout_status_reason`),
  KEY `FKB42FCB94B9093772` (`cancel_status_reason`),
  KEY `FKB42FCB94AE6DDB9` (`cancel_status`),
  KEY `booked_visit_5__subject_mrn` (`subject_mrn`),
  KEY `IDX_bv_scheduling_user` (`scheduling_user`),
  KEY `IDX_bv_check_in_user` (`check_in_user`),
  KEY `IDX_bv_check_out_user` (`check_out_user`),
  KEY `IDX_bv_cancel_user` (`cancel_user`),
  KEY `booked_visit_fk_visit_type` (`visit_type`),
  CONSTRAINT `FK_bv_cancel_user` FOREIGN KEY (`cancel_user`) REFERENCES `user` (`id`),
  CONSTRAINT `FK_bv_check_in_user` FOREIGN KEY (`check_in_user`) REFERENCES `user` (`id`),
  CONSTRAINT `FK_bv_check_out_user` FOREIGN KEY (`check_out_user`) REFERENCES `user` (`id`),
  CONSTRAINT `FK_bv_scheduling_user` FOREIGN KEY (`scheduling_user`) REFERENCES `user` (`id`),
  CONSTRAINT `booked_visit_0__study` FOREIGN KEY (`study`) REFERENCES `study` (`id`),
  CONSTRAINT `booked_visit_1__visit_template` FOREIGN KEY (`visit_template`) REFERENCES `visit_template` (`id`),
  CONSTRAINT `booked_visit_3__appointment_status_reason` FOREIGN KEY (`checkout_status_reason`) REFERENCES `appointment_status_reason` (`id`),
  CONSTRAINT `booked_visit_4__appointment_status` FOREIGN KEY (`cancel_status`) REFERENCES `cancellation_status` (`id`),
  CONSTRAINT `booked_visit_5__subject_mrn` FOREIGN KEY (`subject_mrn`) REFERENCES `subject_mrn` (`id`),
  CONSTRAINT `booked_visit_6__appointment_status` FOREIGN KEY (`appointment_status`) REFERENCES `appointment_status` (`id`),
  CONSTRAINT `booked_visit_7__appointment_status_reason` FOREIGN KEY (`cancel_status_reason`) REFERENCES `appointment_status_reason` (`id`),
  CONSTRAINT `booked_visit_9__appointment_status_reason` FOREIGN KEY (`appointment_status_reason`) REFERENCES `appointment_status_reason` (`id`),
  CONSTRAINT `booked_visit_fk_visit_type` FOREIGN KEY (`visit_type`) REFERENCES `visit_type` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `booked_visit_activity_log`
--

DROP TABLE IF EXISTS `booked_visit_activity_log`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
 SET character_set_client = utf8mb4 ;
CREATE TABLE `booked_visit_activity_log` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `action_performed` varchar(255) DEFAULT NULL,
  `date` datetime DEFAULT NULL,
  `ip_address` varchar(255) DEFAULT NULL,
  `performing_user` int(11) DEFAULT NULL,
  `booked_visit` int(11) DEFAULT NULL,
  `appointment_override_reason` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `bva_log_performing_user` (`performing_user`),
  KEY `bva_log_appointment_override_reason` (`appointment_override_reason`),
  KEY `bva_log_booked_visit` (`booked_visit`),
  CONSTRAINT `bva_log_appointment_override_reason` FOREIGN KEY (`appointment_override_reason`) REFERENCES `appointment_override_reason` (`id`),
  CONSTRAINT `bva_log_booked_visit` FOREIGN KEY (`booked_visit`) REFERENCES `booked_visit` (`id`),
  CONSTRAINT `bva_log_user` FOREIGN KEY (`performing_user`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `cancellation_status`
--

DROP TABLE IF EXISTS `cancellation_status`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
 SET character_set_client = utf8mb4 ;
CREATE TABLE `cancellation_status` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `category`
--

DROP TABLE IF EXISTS `category`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
 SET character_set_client = utf8mb4 ;
CREATE TABLE `category` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) NOT NULL,
  `display_name` varchar(255) NOT NULL,
  `csv_name` varchar(255) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `centers_and_institutions`
--

DROP TABLE IF EXISTS `centers_and_institutions`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
 SET character_set_client = utf8mb4 ;
CREATE TABLE `centers_and_institutions` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(100) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `comments`
--

DROP TABLE IF EXISTS `comments`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
 SET character_set_client = utf8mb4 ;
CREATE TABLE `comments` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `comment` varchar(255) NOT NULL,
  `visit_template` int(11) DEFAULT NULL,
  `booked_visit` int(11) DEFAULT NULL,
  `user` int(11) NOT NULL,
  `date` datetime NOT NULL,
  PRIMARY KEY (`id`),
  KEY `visit_template_ibfk_1` (`visit_template`),
  KEY `booked_visit_ibfk_2` (`booked_visit`),
  KEY `user_ibfk_3` (`user`),
  CONSTRAINT `comments_0__booked_visit` FOREIGN KEY (`booked_visit`) REFERENCES `booked_visit` (`id`),
  CONSTRAINT `comments_1__user` FOREIGN KEY (`user`) REFERENCES `user` (`id`),
  CONSTRAINT `comments_2__visit_template` FOREIGN KEY (`visit_template`) REFERENCES `visit_template` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `country`
--

DROP TABLE IF EXISTS `country`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
 SET character_set_client = utf8mb4 ;
CREATE TABLE `country` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `credential`
--

DROP TABLE IF EXISTS `credential`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
 SET character_set_client = utf8mb4 ;
CREATE TABLE `credential` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `db_update_log`
--

DROP TABLE IF EXISTS `db_update_log`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
 SET character_set_client = utf8mb4 ;
CREATE TABLE `db_update_log` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `pom_version` varchar(50) NOT NULL,
  `update_script` varchar(50) NOT NULL,
  `timestamp` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `comment` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `department`
--

DROP TABLE IF EXISTS `department`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
 SET character_set_client = utf8mb4 ;
CREATE TABLE `department` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `division`
--

DROP TABLE IF EXISTS `division`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
 SET character_set_client = utf8mb4 ;
CREATE TABLE `division` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `ethnicity`
--

DROP TABLE IF EXISTS `ethnicity`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
 SET character_set_client = utf8mb4 ;
CREATE TABLE `ethnicity` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `ethnicity_mapping`
--

DROP TABLE IF EXISTS `ethnicity_mapping`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
 SET character_set_client = utf8mb4 ;
CREATE TABLE `ethnicity_mapping` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `external` varchar(255) NOT NULL,
  `scheduler` int(11) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_empi_scheduler_ethnicity_scheduler` (`scheduler`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `faculty_rank`
--

DROP TABLE IF EXISTS `faculty_rank`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
 SET character_set_client = utf8mb4 ;
CREATE TABLE `faculty_rank` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `field`
--

DROP TABLE IF EXISTS `field`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
 SET character_set_client = utf8mb4 ;
CREATE TABLE `field` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `table_name` varchar(255) NOT NULL,
  `name` varchar(255) NOT NULL,
  `display_name` varchar(255) NOT NULL,
  `processing` varchar(255) DEFAULT 'DEFAULT',
  `sortable` tinyint(1) NOT NULL DEFAULT '1',
  `type` varchar(25) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `funding_source`
--

DROP TABLE IF EXISTS `funding_source`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
 SET character_set_client = utf8mb4 ;
CREATE TABLE `funding_source` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `gender`
--

DROP TABLE IF EXISTS `gender`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
 SET character_set_client = utf8mb4 ;
CREATE TABLE `gender` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `code` varchar(255) NOT NULL,
  `name` varchar(255) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `institution`
--

DROP TABLE IF EXISTS `institution`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
 SET character_set_client = utf8mb4 ;
CREATE TABLE `institution` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `long_name` varchar(255) NOT NULL,
  `name` varchar(255) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `institution_role`
--

DROP TABLE IF EXISTS `institution_role`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
 SET character_set_client = utf8mb4 ;
CREATE TABLE `institution_role` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `description` varchar(255) DEFAULT NULL,
  `name` varchar(255) NOT NULL,
  `type` varchar(21) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `type_UNIQUE` (`type`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `irb_institution`
--

DROP TABLE IF EXISTS `irb_institution`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
 SET character_set_client = utf8mb4 ;
CREATE TABLE `irb_institution` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `line_level_annotations`
--

DROP TABLE IF EXISTS `line_level_annotations`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
 SET character_set_client = utf8mb4 ;
CREATE TABLE `line_level_annotations` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) NOT NULL,
  `quantifiable` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `nightly_batch_changes`
--

DROP TABLE IF EXISTS `nightly_batch_changes`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
 SET character_set_client = utf8mb4 ;
CREATE TABLE `nightly_batch_changes` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `changes` longtext NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `override_booked_resource_annotations`
--

DROP TABLE IF EXISTS `override_booked_resource_annotations`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
 SET character_set_client = utf8mb4 ;
CREATE TABLE `override_booked_resource_annotations` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `booked_resource` int(11) NOT NULL,
  `line_level_annotations` int(11) NOT NULL,
  `quantity` int(11) DEFAULT NULL,
  `comment` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `override_booked_resource_annotations_ibfk_1` (`booked_resource`),
  KEY `override_booked_resource_annotations_ibfk_2` (`line_level_annotations`),
  CONSTRAINT `override_booked_resource_annotations_0__booked_resource` FOREIGN KEY (`booked_resource`) REFERENCES `booked_resource` (`id`),
  CONSTRAINT `override_booked_resource_annotations_1__line_level_annotations` FOREIGN KEY (`line_level_annotations`) REFERENCES `line_level_annotations` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `race`
--

DROP TABLE IF EXISTS `race`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
 SET character_set_client = utf8mb4 ;
CREATE TABLE `race` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `report`
--

DROP TABLE IF EXISTS `report`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
 SET character_set_client = utf8mb4 ;
CREATE TABLE `report` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `description` varchar(255) NOT NULL,
  `title` varchar(255) NOT NULL,
  `name` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `report_template`
--

DROP TABLE IF EXISTS `report_template`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
 SET character_set_client = utf8mb4 ;
CREATE TABLE `report_template` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `display_name` varchar(255) NOT NULL,
  `type` varchar(255) NOT NULL,
  `date_bound` tinyint(1) NOT NULL,
  `graph_type` char(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `resource`
--

DROP TABLE IF EXISTS `resource`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
 SET character_set_client = utf8mb4 ;
CREATE TABLE `resource` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(200) NOT NULL,
  `shared_resource` int(11) DEFAULT NULL,
  `type` varchar(255) DEFAULT NULL,
  `shared_resource_notes` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_resource_name` (`name`),
  KEY `FKEBABC40E6D9F53A5` (`shared_resource`),
  KEY `idx_resource_type` (`type`),
  CONSTRAINT `resource_0__resource` FOREIGN KEY (`shared_resource`) REFERENCES `resource` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `resource_alternate`
--

DROP TABLE IF EXISTS `resource_alternate`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
 SET character_set_client = utf8mb4 ;
CREATE TABLE `resource_alternate` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `source_resource` int(11) NOT NULL,
  `alternate_resource` int(11) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_resAlt_srcRes` (`source_resource`,`alternate_resource`),
  KEY `FK229CC589ABB5EAEB` (`alternate_resource`),
  KEY `FK229CC5895C44C86A` (`source_resource`),
  CONSTRAINT `resource_alternate_0__resource` FOREIGN KEY (`alternate_resource`) REFERENCES `resource` (`id`),
  CONSTRAINT `resource_alternate_1__resource` FOREIGN KEY (`source_resource`) REFERENCES `resource` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `resource_annotation`
--

DROP TABLE IF EXISTS `resource_annotation`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
 SET character_set_client = utf8mb4 ;
CREATE TABLE `resource_annotation` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `resource` int(11) NOT NULL,
  `line_level_annotations` int(11) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `resource_annotation_unique1` (`resource`,`line_level_annotations`),
  KEY `resource_annotation_ibfk_1` (`resource`),
  KEY `resource_annotation_ibfk_2` (`line_level_annotations`),
  CONSTRAINT `resource_annotation_0__resource` FOREIGN KEY (`resource`) REFERENCES `resource` (`id`),
  CONSTRAINT `resource_annotation_1__line_level_annotations` FOREIGN KEY (`line_level_annotations`) REFERENCES `line_level_annotations` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `resource_schedule`
--

DROP TABLE IF EXISTS `resource_schedule`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
 SET character_set_client = utf8mb4 ;
CREATE TABLE `resource_schedule` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `resource` int(11) NOT NULL,
  `start_time` datetime NOT NULL,
  `end_time` datetime NOT NULL,
  `day_of_week` int(11) DEFAULT NULL,
  `quantity` float DEFAULT '0',
  `override` tinyint(1) DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `FK7720AFA8701BE7FC` (`resource`),
  CONSTRAINT `resource_schedule_1__resource` FOREIGN KEY (`resource`) REFERENCES `resource` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `resource_sublocation`
--

DROP TABLE IF EXISTS `resource_sublocation`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
 SET character_set_client = utf8mb4 ;
CREATE TABLE `resource_sublocation` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `sublocation` int(11) DEFAULT NULL,
  `resource` int(11) NOT NULL,
  `active` tinyint(1) NOT NULL DEFAULT '1',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_rescSubLoc_id1` (`resource`,`sublocation`),
  KEY `FKB60B9B4450E3AD0B` (`resource`),
  KEY `FKB60B9B44135C3C9B` (`sublocation`),
  CONSTRAINT `resource_sublocation_0__resource` FOREIGN KEY (`resource`) REFERENCES `resource` (`id`),
  CONSTRAINT `resource_sublocation_1__sublocation` FOREIGN KEY (`sublocation`) REFERENCES `sublocation` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `resource_type`
--

DROP TABLE IF EXISTS `resource_type`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
 SET character_set_client = utf8mb4 ;
CREATE TABLE `resource_type` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `role`
--

DROP TABLE IF EXISTS `role`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
 SET character_set_client = utf8mb4 ;
CREATE TABLE `role` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) NOT NULL,
  `type` varchar(36) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `type_UNIQUE` (`type`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `state`
--

DROP TABLE IF EXISTS `state`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
 SET character_set_client = utf8mb4 ;
CREATE TABLE `state` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `study`
--

DROP TABLE IF EXISTS `study`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
 SET character_set_client = utf8mb4 ;
CREATE TABLE `study` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `catalyst_id` varchar(255) DEFAULT NULL,
  `crc_category` varchar(255) DEFAULT NULL,
  `spid` varchar(255) DEFAULT NULL,
  `protocol_number` varchar(255) DEFAULT NULL,
  `expected_end_date` date DEFAULT NULL,
  `expected_start_date` date DEFAULT NULL,
  `industry_initiated` tinyint(1) DEFAULT '0',
  `institution` int(11) DEFAULT NULL,
  `irb` varchar(255) DEFAULT NULL,
  `irb_approval_date` datetime DEFAULT NULL,
  `irb_expiration` datetime DEFAULT NULL,
  `irb_institution` int(11) DEFAULT NULL,
  `irb_renewal_date` datetime DEFAULT NULL,
  `local_id` varchar(255) DEFAULT NULL,
  `short_title` varchar(255) DEFAULT NULL,
  `name` text,
  `study_status` int(11) DEFAULT NULL,
  `total_inpatient_visits` int(11) DEFAULT NULL,
  `total_outpatient_visits` int(11) DEFAULT NULL,
  `total_subjects` int(11) DEFAULT NULL,
  `pediatric` varchar(255) DEFAULT NULL,
  `principal_investigator` int(11) DEFAULT NULL,
  `responsible_physician` int(11) DEFAULT NULL,
  `responsible_physician2` int(11) DEFAULT NULL,
  `primary_scheduling_contact` int(11) DEFAULT NULL,
  `secondary_scheduling_contact` int(11) DEFAULT NULL,
  `protocol_nutritionist` int(11) DEFAULT NULL,
  `protocol_nurse` int(11) DEFAULT NULL,
  `associate_nurse` int(11) DEFAULT NULL,
  `scheduler3` int(11) DEFAULT NULL,
  `protocol_nurse_string` varchar(255) DEFAULT NULL,
  `protocol_nutritionist_string` varchar(255) DEFAULT NULL,
  `crc_funded` tinyint(1) NOT NULL DEFAULT '0',
  `associate_protocol_nutritionist` int(11) DEFAULT NULL,
  `study_abstract` varchar(3000) DEFAULT NULL,
  `clinical_trial` varchar(15) DEFAULT NULL,
  `new_drug` varchar(15) DEFAULT NULL,
  `device_exemption` varchar(15) DEFAULT NULL,
  `status_change` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK68B0DC9FF65A76B` (`responsible_physician2`),
  KEY `FK68B0DC91ADD054` (`principal_investigator`),
  KEY `FK68B0DC9D3D82F0` (`protocol_nurse`),
  KEY `FK68B0DC930E28873` (`protocol_nutritionist`),
  KEY `FK68B0DC9AF7BD5B` (`responsible_physician`),
  KEY `FK68B0DC97D2606F6` (`associate_nurse`),
  KEY `FK68B0DC97C49620C` (`primary_scheduling_contact`),
  KEY `FK68B0DC9F2DA955A` (`secondary_scheduling_contact`),
  KEY `FK68B0DC96114C21` (`institution`),
  KEY `FK68B0DC99EE8853B` (`irb_institution`),
  KEY `FK68B0DC91E68EAF4` (`study_status`),
  KEY `FK68B0DC9C8CD3712` (`scheduler3`),
  KEY `associte_proto_nutritionist_fk` (`associate_protocol_nutritionist`),
  CONSTRAINT `study_0__user` FOREIGN KEY (`associate_protocol_nutritionist`) REFERENCES `user` (`id`),
  CONSTRAINT `study_10__user` FOREIGN KEY (`principal_investigator`) REFERENCES `user` (`id`),
  CONSTRAINT `study_11__user` FOREIGN KEY (`protocol_nutritionist`) REFERENCES `user` (`id`),
  CONSTRAINT `study_12__user` FOREIGN KEY (`primary_scheduling_contact`) REFERENCES `user` (`id`),
  CONSTRAINT `study_13__user` FOREIGN KEY (`responsible_physician2`) REFERENCES `user` (`id`),
  CONSTRAINT `study_14__user` FOREIGN KEY (`responsible_physician`) REFERENCES `user` (`id`),
  CONSTRAINT `study_15__user` FOREIGN KEY (`secondary_scheduling_contact`) REFERENCES `user` (`id`),
  CONSTRAINT `study_1__study_status` FOREIGN KEY (`study_status`) REFERENCES `study_status` (`id`),
  CONSTRAINT `study_6__irb_institution` FOREIGN KEY (`irb_institution`) REFERENCES `irb_institution` (`id`),
  CONSTRAINT `study_7__user` FOREIGN KEY (`scheduler3`) REFERENCES `user` (`id`),
  CONSTRAINT `study_8__institution` FOREIGN KEY (`institution`) REFERENCES `institution` (`id`),
  CONSTRAINT `study_9__user` FOREIGN KEY (`protocol_nurse`) REFERENCES `user` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `study_funding_source_cai`
--

DROP TABLE IF EXISTS `study_funding_source_cai`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
 SET character_set_client = utf8mb4 ;
CREATE TABLE `study_funding_source_cai` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `study` int(11) NOT NULL,
  `funding_source` int(11) NOT NULL,
  `centers_and_institutions` int(11) DEFAULT NULL,
  `comment` varchar(255) DEFAULT NULL,
  `one_to_four` int(11) NOT NULL,
  `grant_id` varchar(100) DEFAULT NULL,
  `site_cost_center` varchar(100) DEFAULT NULL,
  `total_direct_award` double(19,2) DEFAULT NULL,
  `total_indirect_award` double(19,2) DEFAULT NULL,
  `project_start_date` date DEFAULT NULL,
  `project_end_date` date DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `study_funding_source_cai__study` (`study`),
  KEY `study_funding_source_cai__funding_source` (`funding_source`),
  KEY `study_funding_source_cai__cai` (`centers_and_institutions`),
  CONSTRAINT `study_funding_source_cai__cai` FOREIGN KEY (`centers_and_institutions`) REFERENCES `centers_and_institutions` (`id`),
  CONSTRAINT `study_funding_source_cai__funding_source` FOREIGN KEY (`funding_source`) REFERENCES `funding_source` (`id`),
  CONSTRAINT `study_funding_source_cai__study` FOREIGN KEY (`study`) REFERENCES `study` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `study_status`
--

DROP TABLE IF EXISTS `study_status`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
 SET character_set_client = utf8mb4 ;
CREATE TABLE `study_status` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) NOT NULL,
  `short_name` varchar(255) DEFAULT NULL,
  `is_pending` int(1) NOT NULL DEFAULT '0',
  `is_open` int(1) NOT NULL DEFAULT '0',
  `is_closed` int(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `study_subject`
--

DROP TABLE IF EXISTS `study_subject`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
 SET character_set_client = utf8mb4 ;
CREATE TABLE `study_subject` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `active` tinyint(1) NOT NULL DEFAULT '0',
  `study` int(11) NOT NULL,
  `subject_mrn` int(11) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FK4C2330B64CCAB503` (`study`),
  KEY `study_subject_0__subject_mrn` (`subject_mrn`),
  CONSTRAINT `study_subject_0__subject_mrn` FOREIGN KEY (`subject_mrn`) REFERENCES `subject_mrn` (`id`),
  CONSTRAINT `study_subject_1__study` FOREIGN KEY (`study`) REFERENCES `study` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `study_user`
--

DROP TABLE IF EXISTS `study_user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
 SET character_set_client = utf8mb4 ;
CREATE TABLE `study_user` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `user` int(11) NOT NULL,
  `study` int(11) NOT NULL,
  `active` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_studyUserRole_id1` (`study`,`user`),
  KEY `FKD4EC15744CCAB503` (`study`),
  KEY `FKD4EC1574C7582006` (`user`),
  KEY `FKE3C4CC814CCAB503` (`study`),
  KEY `FKE3C4CC81F1F7CC05` (`user`),
  CONSTRAINT `study_user_0__study` FOREIGN KEY (`study`) REFERENCES `study` (`id`),
  CONSTRAINT `study_user_1__user` FOREIGN KEY (`user`) REFERENCES `user` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `sub_category`
--

DROP TABLE IF EXISTS `sub_category`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
 SET character_set_client = utf8mb4 ;
CREATE TABLE `sub_category` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `parent_id` int(11) NOT NULL,
  `child_id` int(11) NOT NULL,
  `report_template_id` int(11) NOT NULL,
  `expanded` tinyint(1) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `sub_category_0__parent_id` (`parent_id`),
  KEY `sub_category_1__child_id` (`child_id`),
  KEY `sub_category_2__template_id` (`report_template_id`),
  CONSTRAINT `sub_category_0__parent_id` FOREIGN KEY (`parent_id`) REFERENCES `category` (`id`),
  CONSTRAINT `sub_category_1__child_id` FOREIGN KEY (`child_id`) REFERENCES `category` (`id`),
  CONSTRAINT `sub_category_2__template_id` FOREIGN KEY (`report_template_id`) REFERENCES `report_template` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `subject`
--

DROP TABLE IF EXISTS `subject`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
 SET character_set_client = utf8mb4 ;
CREATE TABLE `subject` (
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
  `latest_ssot_refresh` datetime DEFAULT NULL,
  `puid` varchar(255) DEFAULT NULL,
  `archival_status` varchar(255) DEFAULT NULL,
  `gender_enum` varchar(255) NOT NULL DEFAULT 'UNREPORTED',
  PRIMARY KEY (`id`),
  KEY `FK90AA552C586F81EF` (`ethnicity`),
  KEY `FK90AA552C4CCA2293` (`state`),
  KEY `FK90AA552C21EA7A31` (`gender`),
  KEY `FK90AA552CF1F48A11` (`race`),
  KEY `FK90AA552C9722B05D` (`country`),
  CONSTRAINT `subject_0__gender` FOREIGN KEY (`gender`) REFERENCES `gender` (`id`),
  CONSTRAINT `subject_1__state` FOREIGN KEY (`state`) REFERENCES `state` (`id`),
  CONSTRAINT `subject_2__ethnicity` FOREIGN KEY (`ethnicity`) REFERENCES `ethnicity` (`id`),
  CONSTRAINT `subject_3__country` FOREIGN KEY (`country`) REFERENCES `country` (`id`),
  CONSTRAINT `subject_4__race` FOREIGN KEY (`race`) REFERENCES `race` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `subject_mrn`
--

DROP TABLE IF EXISTS `subject_mrn`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
 SET character_set_client = utf8mb4 ;
CREATE TABLE `subject_mrn` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `subject` int(11) NOT NULL,
  `mrn` varchar(255) NOT NULL,
  `site` varchar(255) DEFAULT NULL,
  `status` varchar(15) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `FK_subject_mrn_site_id1` (`subject`,`mrn`,`site`),
  KEY `FK_subject_mrn_id2` (`subject`),
  CONSTRAINT `subject_mrn_0__subject` FOREIGN KEY (`subject`) REFERENCES `subject` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `sublocation`
--

DROP TABLE IF EXISTS `sublocation`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
 SET character_set_client = utf8mb4 ;
CREATE TABLE `sublocation` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) NOT NULL,
  `institution` int(11) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKBBCF1DF56114C21` (`institution`),
  CONSTRAINT `sublocation_0__institution` FOREIGN KEY (`institution`) REFERENCES `institution` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `sublocation_closure_interval`
--

DROP TABLE IF EXISTS `sublocation_closure_interval`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
 SET character_set_client = utf8mb4 ;
CREATE TABLE `sublocation_closure_interval` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `sublocation` int(11) NOT NULL,
  `reason` varchar(400) DEFAULT NULL,
  `start_time` datetime NOT NULL,
  `end_time` datetime NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKA3BC2B53135C3C9B` (`sublocation`),
  CONSTRAINT `sublocation_closure_interval_0__sublocation` FOREIGN KEY (`sublocation`) REFERENCES `sublocation` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `template_approval_history`
--

DROP TABLE IF EXISTS `template_approval_history`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
 SET character_set_client = utf8mb4 ;
CREATE TABLE `template_approval_history` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `visit_template` int(11) NOT NULL,
  `user` int(11) NOT NULL,
  `approved` tinyint(1) DEFAULT '0',
  `comment` varchar(255) DEFAULT NULL,
  `status_change_time` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `unique_tmpApprov_visitUser` (`visit_template`,`user`),
  KEY `FK2E5DD7BD93F01044` (`visit_template`),
  KEY `FK2E5DD7BDF1F7CC05` (`user`),
  CONSTRAINT `template_approval_history_0__visit_template` FOREIGN KEY (`visit_template`) REFERENCES `visit_template` (`id`),
  CONSTRAINT `template_approval_history_1__user` FOREIGN KEY (`user`) REFERENCES `user` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `template_category`
--

DROP TABLE IF EXISTS `template_category`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
 SET character_set_client = utf8mb4 ;
CREATE TABLE `template_category` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `report_template_id` int(11) NOT NULL,
  `category_id` int(11) NOT NULL,
  `expanded` tinyint(1) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `FK_template_category_id1` (`report_template_id`,`category_id`),
  KEY `template_category_0__category_id` (`category_id`),
  KEY `template_category_1__template_id` (`report_template_id`),
  CONSTRAINT `template_category_0__category_id` FOREIGN KEY (`category_id`) REFERENCES `category` (`id`),
  CONSTRAINT `template_category_1__template_id` FOREIGN KEY (`report_template_id`) REFERENCES `report_template` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `template_category_field`
--

DROP TABLE IF EXISTS `template_category_field`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
 SET character_set_client = utf8mb4 ;
CREATE TABLE `template_category_field` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `report_template_id` int(11) NOT NULL,
  `field_id` int(11) NOT NULL,
  `category_id` int(11) NOT NULL,
  `suffix` varchar(255) NOT NULL,
  `selected` tinyint(1) NOT NULL,
  `csv_order` int(11) NOT NULL,
  `csv_order2` int(11) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `template_category_field_0__field_id` (`field_id`),
  KEY `template_category_field_1__template_id` (`report_template_id`),
  KEY `template_category_field_2__category_id` (`category_id`),
  CONSTRAINT `template_category_field_0__field_id` FOREIGN KEY (`field_id`) REFERENCES `field` (`id`),
  CONSTRAINT `template_category_field_1__template_id` FOREIGN KEY (`report_template_id`) REFERENCES `report_template` (`id`),
  CONSTRAINT `template_category_field_2__category_id` FOREIGN KEY (`category_id`) REFERENCES `category` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `template_resource`
--

DROP TABLE IF EXISTS `template_resource`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
 SET character_set_client = utf8mb4 ;
CREATE TABLE `template_resource` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `visit_template` int(11) NOT NULL,
  `resource` int(11) NOT NULL,
  `alternate` tinyint(1) NOT NULL DEFAULT '1',
  `duration` int(11) DEFAULT NULL,
  `float_start` int(11) DEFAULT NULL,
  `float_end` int(11) DEFAULT NULL,
  `created_date` datetime DEFAULT NULL,
  `floatable` tinyint(1) DEFAULT '0',
  `flexible` tinyint(1) DEFAULT '0',
  `billable` tinyint(1) DEFAULT '0',
  `group_id` varchar(255) DEFAULT NULL,
  `last_update_time` datetime DEFAULT NULL,
  `start_minutes` int(11) DEFAULT NULL,
  `end_minutes` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKF23B923393F01044` (`visit_template`),
  KEY `FKF23B923350E3AD0B` (`resource`),
  CONSTRAINT `template_resource_0__visit_template` FOREIGN KEY (`visit_template`) REFERENCES `visit_template` (`id`),
  CONSTRAINT `template_resource_1__resource` FOREIGN KEY (`resource`) REFERENCES `resource` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `template_resource_annotations`
--

DROP TABLE IF EXISTS `template_resource_annotations`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
 SET character_set_client = utf8mb4 ;
CREATE TABLE `template_resource_annotations` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `template_resource` int(11) NOT NULL,
  `line_level_annotations` int(11) NOT NULL,
  `quantity` int(11) DEFAULT NULL,
  `comment` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `template_resource_annotations_ibfk_1` (`template_resource`),
  KEY `template_resource_annotations_ibfk_2` (`line_level_annotations`),
  CONSTRAINT `template_resource_annotations_0__template_resource` FOREIGN KEY (`template_resource`) REFERENCES `template_resource` (`id`),
  CONSTRAINT `template_resource_annotations_1__line_level_annotations` FOREIGN KEY (`line_level_annotations`) REFERENCES `line_level_annotations` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `template_resource_group`
--

DROP TABLE IF EXISTS `template_resource_group`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
 SET character_set_client = utf8mb4 ;
CREATE TABLE `template_resource_group` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `group_id` varchar(255) NOT NULL,
  `template_resource` int(11) NOT NULL,
  `flex_group` tinyint(1) DEFAULT '0',
  `visit` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_tempResGrp_resc` (`template_resource`),
  KEY `FK99A13973231932CA` (`template_resource`),
  KEY `FK99A13973BC25E8A1` (`visit`),
  CONSTRAINT `template_resource_group_0__visit_template` FOREIGN KEY (`visit`) REFERENCES `visit_template` (`id`),
  CONSTRAINT `template_resource_group_1__template_resource` FOREIGN KEY (`template_resource`) REFERENCES `template_resource` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `template_user`
--

DROP TABLE IF EXISTS `template_user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
 SET character_set_client = utf8mb4 ;
CREATE TABLE `template_user` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `report_template_id` int(11) NOT NULL,
  `user_id` int(11) NOT NULL,
  `last_update_time` datetime NOT NULL,
  `name` varchar(50) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `template_user_0__user_id` (`user_id`),
  KEY `template_user_1__template_id` (`report_template_id`),
  CONSTRAINT `template_user_0__user_id` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`),
  CONSTRAINT `template_user_1__template_id` FOREIGN KEY (`report_template_id`) REFERENCES `report_template` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `template_user_filter_selection`
--

DROP TABLE IF EXISTS `template_user_filter_selection`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
 SET character_set_client = utf8mb4 ;
CREATE TABLE `template_user_filter_selection` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `template_user_selection_id` int(11) NOT NULL,
  `expression` text NOT NULL,
  PRIMARY KEY (`id`),
  KEY `template_user_filter_selection_fk1` (`template_user_selection_id`),
  CONSTRAINT `template_user_filter_selection_constraint1` FOREIGN KEY (`template_user_selection_id`) REFERENCES `template_user_selection` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `template_user_selection`
--

DROP TABLE IF EXISTS `template_user_selection`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
 SET character_set_client = utf8mb4 ;
CREATE TABLE `template_user_selection` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `template_user_id` int(11) NOT NULL,
  `tcf_id` int(11) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `template_user_selection_fk1` (`template_user_id`),
  KEY `template_user_selection_fk2` (`tcf_id`),
  CONSTRAINT `template_user_selection_constraint1` FOREIGN KEY (`template_user_id`) REFERENCES `template_user` (`id`),
  CONSTRAINT `template_user_selection_constraint2` FOREIGN KEY (`tcf_id`) REFERENCES `template_category_field` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `template_user_sort_selection`
--

DROP TABLE IF EXISTS `template_user_sort_selection`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
 SET character_set_client = utf8mb4 ;
CREATE TABLE `template_user_sort_selection` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `template_user_selection_id` int(11) NOT NULL,
  `order_by` varchar(21) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `template_user_sort_selection_fk1` (`template_user_selection_id`),
  CONSTRAINT `template_user_sort_selection_constraint1` FOREIGN KEY (`template_user_selection_id`) REFERENCES `template_user_selection` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `user`
--

DROP TABLE IF EXISTS `user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
 SET character_set_client = utf8mb4 ;
CREATE TABLE `user` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `active` tinyint(1) NOT NULL DEFAULT '0',
  `credential` int(255) DEFAULT NULL,
  `department` int(255) DEFAULT NULL,
  `division` int(255) DEFAULT NULL,
  `ecommons_id` varchar(255) DEFAULT NULL,
  `email` varchar(255) DEFAULT NULL,
  `faculty_rank` int(255) DEFAULT NULL,
  `fax` varchar(255) DEFAULT NULL,
  `first_name` varchar(255) DEFAULT NULL,
  `institution` int(11) DEFAULT NULL,
  `irb_institution` varchar(255) DEFAULT NULL,
  `last_name` varchar(255) DEFAULT NULL,
  `middle_name` varchar(255) DEFAULT NULL,
  `pager` varchar(255) DEFAULT NULL,
  `password` varchar(255) DEFAULT NULL,
  `primary_phone` varchar(255) DEFAULT NULL,
  `salt` varchar(255) DEFAULT NULL,
  `secondary_phone` varchar(255) DEFAULT NULL,
  `institution_role` int(11) DEFAULT NULL,
  `role` int(11) DEFAULT NULL,
  `last_login_time` datetime DEFAULT NULL,
  `notification_email` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `user_session`
--

DROP TABLE IF EXISTS `user_session`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
 SET character_set_client = utf8mb4 ;
CREATE TABLE `user_session` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `ip_address` varchar(200) DEFAULT NULL,
  `session_id` varchar(200) DEFAULT NULL,
  `user` int(11) NOT NULL,
  `last_access_time` datetime NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKD1401A22F1F7CC05` (`user`),
  CONSTRAINT `user_session_0__user` FOREIGN KEY (`user`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `visit_template`
--

DROP TABLE IF EXISTS `visit_template`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
 SET character_set_client = utf8mb4 ;
CREATE TABLE `visit_template` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `study` int(11) NOT NULL,
  `name` varchar(255) DEFAULT NULL,
  `short_name` varchar(255) DEFAULT NULL,
  `sublocation` int(11) DEFAULT NULL,
  `approved` tinyint(1) DEFAULT '0',
  `relative_time` tinyint(1) DEFAULT '0',
  `duration` int(11) DEFAULT NULL,
  `comment` varchar(255) DEFAULT NULL,
  `created_date` datetime DEFAULT NULL,
  `nursing` varchar(11) DEFAULT NULL,
  `nutrition` varchar(11) DEFAULT NULL,
  `processing` varchar(11) DEFAULT NULL,
  `non_institution_non_CRC` tinyint(1) DEFAULT '0',
  `institution_non_crc` tinyint(1) DEFAULT '0',
  `active` tinyint(1) DEFAULT '0',
  `setUp` varchar(11) DEFAULT NULL,
  `research_pharmacy` tinyint(1) DEFAULT '0',
  `last_update_time` datetime DEFAULT NULL,
  `visit_type` int(11) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FK6B04D4BE967D31E` (`study`),
  KEY `FKDE7A74EE135C3C9B` (`sublocation`),
  KEY `visit_template_fk_visit_type` (`visit_type`),
  CONSTRAINT `visit_template_0__sublocation` FOREIGN KEY (`sublocation`) REFERENCES `sublocation` (`id`),
  CONSTRAINT `visit_template_1__study` FOREIGN KEY (`study`) REFERENCES `study` (`id`),
  CONSTRAINT `visit_template_fk_visit_type` FOREIGN KEY (`visit_type`) REFERENCES `visit_type` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `visit_type`
--

DROP TABLE IF EXISTS `visit_type`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
 SET character_set_client = utf8mb4 ;
CREATE TABLE `visit_type` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) NOT NULL,
  `inpatient` tinyint(1) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2019-12-20 13:44:48
