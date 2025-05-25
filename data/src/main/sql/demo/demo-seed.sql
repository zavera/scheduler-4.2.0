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
-- Dumping data for table `appointment_override_reason`
--

LOCK TABLES `appointment_override_reason` WRITE;
/*!40000 ALTER TABLE `appointment_override_reason` DISABLE KEYS */;
set autocommit=0;
INSERT INTO `appointment_override_reason` (`id`, `name`) VALUES (1,'Change of resource'),(2,'Override (resource otherwise not available) Approved by RN Resource Manager'),(3,'Override (resource otherwise not available) Approved by RD Resource Manager'),(4,'Override (resource otherwise not available) Approved by Other Resource Manager'),(5,'Overlap (double booking) Approved by RN Resource Manager'),(6,'Overlap (double booking) Approved by RD Resource Manager'),(7,'Overlap (double booking) Approved by Other Resource Manager');
/*!40000 ALTER TABLE `appointment_override_reason` ENABLE KEYS */;
UNLOCK TABLES;
commit;

--
-- Dumping data for table `appointment_status`
--

LOCK TABLES `appointment_status` WRITE;
/*!40000 ALTER TABLE `appointment_status` DISABLE KEYS */;
set autocommit=0;
INSERT INTO `appointment_status` (`id`, `name`, `is_cancelled`, `is_checked_in`, `is_checked_out`, `is_hold`, `is_scheduled`, `is_open`, `is_active`, `is_serviced`, `is_serviceable`) VALUES (1,'Scheduled',0,0,0,0,1,1,1,0,1),(2,'Checked-In',0,1,0,0,0,1,1,1,1),(3,'Checked-Out',0,0,1,0,0,0,0,1,1),(4,'Cancellation',1,0,0,0,0,0,0,0,0),(5,'Hold',0,0,0,1,0,1,0,0,0);
/*!40000 ALTER TABLE `appointment_status` ENABLE KEYS */;
UNLOCK TABLES;
commit;

--
-- Dumping data for table `appointment_status_reason`
--

LOCK TABLES `appointment_status_reason` WRITE;
/*!40000 ALTER TABLE `appointment_status_reason` DISABLE KEYS */;
set autocommit=0;
INSERT INTO `appointment_status_reason` (`id`, `appointment_status`, `name`) VALUES (1,4,'Unforeseen Circumstances: Sick'),(2,4,'Ineligible for the study: Non Adherent'),(3,4,'Ineligible for this visit: Menses Dependent'),(4,4,'Unforeseen Circumstances: Personal'),(5,4,'Withdrew Participation'),(6,4,'Ineligible for this visit: Non Adherent'),(7,4,'Administrative Cancel: Equipment Failure'),(8,4,'Unforeseen Circumstances: Weather'),(9,4,'Ineligible for the study: Adverse event at previous visit'),(10,4,'Ineligible for the study: Failed screening visit or other visit'),(11,4,'Administrative Cancel: Data Entry Error'),(12,4,'No Show'),(13,4,'Administrative Cancel: Operational Cancel (CRC Staff Use Only)'),(14,3,'Visit Terminated Prior to Completion'),(15,3,'Visit Completed');
/*!40000 ALTER TABLE `appointment_status_reason` ENABLE KEYS */;
UNLOCK TABLES;
commit;

--
-- Dumping data for table `cancellation_status`
--

LOCK TABLES `cancellation_status` WRITE;
/*!40000 ALTER TABLE `cancellation_status` DISABLE KEYS */;
set autocommit=0;
INSERT INTO `cancellation_status` (`id`, `name`) VALUES (5,'No Show'),(6,'Late Cancellation'),(7,'Cancellation');
/*!40000 ALTER TABLE `cancellation_status` ENABLE KEYS */;
UNLOCK TABLES;
commit;

--
-- Dumping data for table `category`
--

LOCK TABLES `category` WRITE;
/*!40000 ALTER TABLE `category` DISABLE KEYS */;
set autocommit=0;
INSERT INTO `category` (`id`, `name`, `display_name`, `csv_name`) VALUES (1,'study','Study Information','Study'),(2,'user','Study Staff','Staff'),(3,'funding_source','Study Funding Source','Funding'),(4,'visit_template','Visit Information','Visit Template'),(5,'booked_visit','Scheduled Visit','Sch Visit'),(6,'subject','Subject','Subject'),(7,'resource','Resource Information','Resource'),(8,'override_booked_resource_annotations','Scheduled Resource Activities','Sch Activities'),(9,'template_resource_annotations','Template Resource Activities','Template Activities'),(10,'principal_investigator','Principal Investigator','PI'),(11,'responsible_physician','Responsible Physician','Physician'),(12,'second_responsible_physician','Second Responsible Physician','Physician2'),(13,'first_scheduling_contact','First Scheduling Contact','Scheduler'),(14,'second_scheduling_contact','Second Scheduling Contact','Scheduler2'),(15,'third_scheduling_contact','Third Scheduling Contact','Scheduler3'),(16,'protocol_nurse','Protocol Nurse','Nurse'),(17,'associate_protocol_nurse','Associate Protocol Nurse','Nurse2'),(18,'protocol_nutritionist','Protocol Nutritionist','Nutritionist'),(19,'associate_protocol_nutritionist','Associate Protocol Nutritionist','Nutritionist2'),(20,'first_funding_source','Funding Source 1','Funding1'),(21,'second_funding_source','Funding Source 2','Funding2'),(22,'third_funding_source','Funding Source 3','Funding3'),(23,'fourth_funding_source','Funding Source 4','Funding4'),(24,'template_resource_information','Template Resource Information','Template Resource');
/*!40000 ALTER TABLE `category` ENABLE KEYS */;
UNLOCK TABLES;
commit;

--
-- Dumping data for table `centers_and_institutions`
--

LOCK TABLES `centers_and_institutions` WRITE;
/*!40000 ALTER TABLE `centers_and_institutions` DISABLE KEYS */;
set autocommit=0;
INSERT INTO `centers_and_institutions` (`id`, `name`) VALUES (1,'Center for Information Technology (CIT)'),(2,'Center for Scientific Review (CSR)'),(3,'Eunice Kennedy Shriver National Institute of Child Health and Human Development (NICHD)'),(4,'John E. Fogarty International Center (FIC)'),(5,'National Center for Advancing Translational Sciences (NCATS)'),(6,'National Center for Complementary and Alternative Medicine (NCCAM)'),(7,'National Eye Institute (NEI)'),(8,'National Heart, Lung, and Blood Institute (NHLBI)'),(9,'National Human Genome Research Institute (NHGRI)'),(10,'National Institute of Allergy and Infectious Diseases (NIAID)'),(11,'National Institute of Arthritis and Musculoskeletal and Skin Diseases (NIAMS)'),(12,'National Institute of Biomedical Imaging and Bioengineering (NIBIB)'),(13,'National Institute of Dental and Craniofacial Research (NIDCR)'),(14,'National Institute of Diabetes and Digestive and Kidney Diseases (NIDDK)'),(15,'National Institute of Environmental Health Sciences (NIEHS)'),(16,'National Institute of General Medical Sciences (NIGMS)'),(17,'National Institute of Mental Health (NIMH)'),(18,'National Institute of Neurological Disorders and Stroke (NINDS)'),(19,'National Institute of Nursing Research (NINR)'),(20,'National Institute on Aging (NIA)'),(21,'National Institute on Alcohol Abuse and Alcoholism (NIAAA)'),(22,'National Institute on Deafness and Other Communication Disorders (NIDCD)'),(23,'National Institute on Drug Abuse (NIDA)'),(24,'National Institute on Minority Health and Health Disparities (NIMHD)'),(25,'National Library of Medicine (NLM) Centers'),(26,'NIH Clinical Center (CC)'),(27,'National Cancer Institute (NCI)');
/*!40000 ALTER TABLE `centers_and_institutions` ENABLE KEYS */;
UNLOCK TABLES;
commit;

--
-- Dumping data for table `country`
--

LOCK TABLES `country` WRITE;
/*!40000 ALTER TABLE `country` DISABLE KEYS */;
set autocommit=0;
INSERT INTO `country` (`id`, `name`) VALUES (1,'UNITED STATES'),(2,'UNITED KINGDOM'),(3,'INDIA '),(4,'CHINA'),(5,'AUSTRALIA'),(6,'BANGLADESH'),(7,'BRAZIL'),(8,'CANADA'),(9,'EGYPT '),(10,'FRANCE'),(11,'AFGHANISTAN'),(12,'ALAND ISLANDS'),(13,'ALBANIA'),(14,'ALGERIA'),(15,'AMERICAN SAMOA'),(16,'ANDORRA'),(17,'ANGOLA'),(18,'ANGUILLA'),(19,'ANTARCTICA'),(20,'ANTIGUA AND BARBUDA'),(21,'ARGENTINA'),(22,'ARMENIA'),(23,'ARUBA'),(25,'AUSTRIA'),(26,'AZERBAIJAN'),(27,'BAHAMAS'),(28,'BAHRAIN'),(29,'BARBADOS'),(30,'BELARUS'),(31,'BELGIUM'),(32,'BELIZE'),(33,'BENIN'),(34,'BERMUDA'),(35,'BHUTAN'),(36,'BOLIVIA, PLURINATIONAL STATE OF'),(37,'BONAIRE, SINT EUSTATIUS AND SABA'),(38,'BOSNIA AND HERZEGOVINA'),(39,'BOTSWANA'),(40,'BOUVET ISLAND'),(41,'BRITISH INDIAN OCEAN TERRITORY'),(42,'BRUNEI DARUSSALAM'),(43,'BULGARIA'),(44,'BURKINA FASO'),(45,'BURUNDI'),(46,'CAMBODIA'),(47,'CAMEROON'),(48,'CAPE VERDE'),(49,'CAYMAN ISLANDS'),(50,'CENTRAL AFRICAN REPUBLIC'),(51,'CHAD'),(52,'CHILE'),(53,'CHRISTMAS ISLAND'),(54,'COCOS (KEELING) ISLANDS'),(55,'COLOMBIA'),(56,'COMOROS'),(57,'CONGO'),(58,'CONGO, THE DEMOCRATIC REPUBLIC OF THE'),(59,'COOK ISLANDS'),(60,'COSTA RICA'),(61,'COTE DIVOIRE'),(62,'CROATIA'),(63,'CUBA'),(64,'CURACAO'),(65,'CYPRUS'),(66,'CZECH REPUBLIC'),(67,'DENMARK'),(68,'DJIBOUTI'),(69,'DOMINICA'),(70,'DOMINICAN REPUBLIC'),(71,'ECUADOR'),(72,'EL SALVADOR'),(73,'EQUATORIAL GUINEA'),(74,'ERITREA'),(75,'ESTONIA'),(76,'ETHIOPIA'),(77,'FALKLAND ISLANDS (MALVINAS)'),(78,'FAROE ISLANDS'),(79,'FIJI'),(80,'FINLAND'),(81,'FRENCH GUIANA'),(82,'FRENCH POLYNESIA'),(83,'FRENCH SOUTHERN TERRITORIES'),(84,'GABON'),(85,'GAMBIA'),(86,'GEORGIA'),(87,'GERMANY'),(88,'GHANA'),(89,'GIBRALTAR'),(90,'GREECE'),(91,'GREENLAND'),(92,'GRENADA'),(93,'GUADELOUPE'),(94,'GUAM'),(95,'GUATEMALA'),(96,'GUERNSEY'),(97,'GUINEA'),(98,'BISSAU'),(99,'GUYANA'),(100,'HAITI'),(101,'HEARD ISLAND AND MCDONALD ISLANDS'),(102,'HOLY SEE (VATICAN CITY STATE)'),(103,'HONDURAS'),(104,'HONG KONG'),(105,'HUNGARY'),(106,'ICELAND'),(107,'INDONESIA'),(108,'IRAN, ISLAMIC REPUBLIC OF'),(109,'IRAQ'),(110,'IRELAND'),(111,'ISLE OF MAN'),(112,'ISRAEL'),(113,'ITALY'),(114,'JAMAICA'),(115,'JAPAN'),(116,'JERSEY'),(117,'JORDAN'),(118,'KAZAKHSTAN'),(119,'KENYA'),(120,'KIRIBATI'),(121,'KOREA, DEMOCRATIC PEOPLES REPUBLIC OF'),(122,'KOREA, REPUBLIC OF'),(123,'KUWAIT'),(124,'KYRGYZSTAN'),(125,'LAO PEOPLES DEMOCRATIC REPUBLIC'),(126,'LATVIA'),(127,'LEBANON'),(128,'LESOTHO'),(129,'LIBERIA'),(130,'LIBYAN ARAB JAMAHIRIYA'),(131,'LIECHTENSTEIN'),(132,'LITHUANIA'),(133,'LUXEMBOURG'),(134,'MACAO'),(135,'MACEDONIA, THE FORMER YUGOSLAV REPUBLIC OF'),(136,'MADAGASCAR'),(137,'MALAWI'),(138,'MALAYSIA'),(139,'MALDIVES'),(140,'MALI'),(141,'MALTA'),(142,'MARSHALL ISLANDS'),(143,'MARTINIQUE'),(144,'MAURITANIA'),(145,'MAURITIUS'),(146,'MAYOTTE'),(147,'MEXICO'),(148,'MICRONESIA, FEDERATED STATES OF'),(149,'MOLDOVA, REPUBLIC OF'),(150,'MONACO'),(151,'MONGOLIA'),(152,'MONTENEGRO'),(153,'MONTSERRAT'),(154,'MOROCCO'),(155,'MOZAMBIQUE'),(156,'MYANMAR'),(157,'NAMIBIA'),(158,'NAURU'),(159,'NEPAL'),(160,'NETHERLANDS'),(161,'NEW CALEDONIA'),(162,'NEW ZEALAND'),(163,'NICARAGUA'),(164,'NIGER'),(165,'NIGERIA'),(166,'NIUE'),(167,'NORFOLK ISLAND'),(168,'NORTHERN MARIANA ISLANDS'),(169,'NORWAY'),(170,'OMAN'),(171,'PAKISTAN'),(172,'PALAU'),(173,'PALESTINIAN TERRITORY, OCCUPIED'),(174,'PANAMA'),(175,'PAPUA NEW GUINEA'),(176,'PARAGUAY'),(177,'PERU'),(178,'PHILIPPINES'),(179,'PITCAIRN'),(180,'POLAND'),(181,'PORTUGAL'),(182,'PUERTO RICO'),(183,'QATAR'),(184,'REUNION'),(185,'ROMANIA'),(186,'RUSSIAN FEDERATION'),(187,'RWANDA'),(188,'SAINT BARTHELEMY'),(189,'SAINT HELENA, ASCENSION AND TRISTAN DA CUNHA'),(190,'SAINT KITTS AND NEVIS'),(191,'SAINT LUCIA'),(192,'SAINT MARTIN (FRENCH PART)'),(193,'SAINT PIERRE AND MIQUELON'),(194,'SAINT VINCENT AND THE GRENADINES'),(195,'SAMOA'),(196,'SAN MARINO'),(197,'SAO TOME AND PRINCIPE'),(198,'SAUDI ARABIA'),(199,'SENEGAL'),(200,'SERBIA'),(201,'SEYCHELLES'),(202,'SIERRA LEONE'),(203,'SINGAPORE'),(204,'SINT MAARTEN (DUTCH PART)'),(205,'SLOVAKIA'),(206,'SLOVENIA'),(207,'SOLOMON ISLANDS'),(208,'SOMALIA'),(209,'SOUTH AFRICA'),(210,'SOUTH GEORGIA AND THE SOUTH SANDWICH ISLANDS'),(211,'SPAIN'),(212,'SRI LANKA'),(213,'SUDAN'),(214,'SURINAME'),(215,'SVALBARD AND JAN MAYEN'),(216,'SWAZILAND'),(217,'SWEDEN'),(218,'SWITZERLAND'),(219,'SYRIAN ARAB REPUBLIC'),(220,'TAIWAN, PROVINCE OF CHINA'),(221,'TAJIKISTAN'),(222,'TANZANIA, UNITED REPUBLIC OF'),(223,'THAILAND'),(224,'TIMOR'),(225,'LESTE'),(226,'TOGO'),(227,'TOKELAU'),(228,'TONGA'),(229,'TRINIDAD AND TOBAGO'),(230,'TUNISIA'),(231,'TURKEY'),(232,'TURKMENISTAN'),(233,'TURKS AND CAICOS ISLANDS'),(234,'TUVALU'),(235,'UGANDA'),(236,'UKRAINE'),(237,'UNITED ARAB EMIRATES'),(238,'UNITED STATES MINOR OUTLYING ISLANDS'),(239,'URUGUAY'),(240,'UZBEKISTAN'),(241,'VANUATU'),(242,'VATICAN CITY STATE'),(243,'VENEZUELA, BOLIVARIAN REPUBLIC OF'),(244,'VIET NAM'),(245,'VIRGIN ISLANDS, BRITISH'),(246,'VIRGIN ISLANDS, U.S.'),(247,'WALLIS AND FUTUNA'),(248,'WESTERN SAHARA'),(249,'YEMEN'),(250,'ZAMBIA'),(251,'ZIMBABWE');
/*!40000 ALTER TABLE `country` ENABLE KEYS */;
UNLOCK TABLES;
commit;

--
-- Dumping data for table `credential`
--

LOCK TABLES `credential` WRITE;
/*!40000 ALTER TABLE `credential` DISABLE KEYS */;
set autocommit=0;
INSERT INTO `credential` (`id`, `name`) VALUES (1,'MD'),(2,'PhD'),(3,'MD PhD'),(4,'RN'),(5,'RN PhD'),(6,'RN MSN'),(7,'MA/MS'),(8,'MPH'),(9,'NP'),(10,'PA'),(11,'Other');
/*!40000 ALTER TABLE `credential` ENABLE KEYS */;
UNLOCK TABLES;
commit;

--
-- Dumping data for table `department`
--

LOCK TABLES `department` WRITE;
/*!40000 ALTER TABLE `department` DISABLE KEYS */;
set autocommit=0;
INSERT INTO `department` (`id`, `name`) VALUES (1,'Anesthesia'),(2,'CRC'),(3,'Dermatology'),(4,'Medicine'),(5,'Neurology'),(6,'Nursing'),(7,'Nutrition'),(8,'Obstetrics, Gynecology & Reproductive Biology'),(9,'Ophthalmology'),(10,'Orthopedic Surgery'),(11,'Otology-Laryngology'),(12,'Pathology'),(13,'Pediatrics'),(14,'Physical Medicine & Rehabilitation'),(15,'Population Medicine'),(16,'Psychiatry'),(17,'Radiology'),(18,'Radiation Oncology'),(19,'Surgery'),(20,'Other');
/*!40000 ALTER TABLE `department` ENABLE KEYS */;
UNLOCK TABLES;
commit;

--
-- Dumping data for table `division`
--

LOCK TABLES `division` WRITE;
/*!40000 ALTER TABLE `division` DISABLE KEYS */;
set autocommit=0;
INSERT INTO `division` (`id`, `name`) VALUES (1,'Adolescent Medicine'),(2,'Allergy & Inflammation'),(3,'Cardiology'),(4,'Cardiac Surgery'),(5,'Colon & Rectal Surgery'),(6,'Endocrinology'),(7,'Nutrition'),(8,'Emergency Medicine'),(9,'Gastroenterology'),(10,'General Medicine'),(11,'Gerontology'),(12,'Gynecology'),(13,'Hematology/Oncology'),(14,'Infectious Disease'),(15,'Minimally Invasive Surgery'),(16,'Nephrology'),(17,'Neurosurgery'),(18,'Newborn Medicine'),(19,'Plastic & Reconstructive Surgery'),(20,'Podiatry'),(21,'Pulmonology'),(22,'Rheumatology'),(23,'Sleep Medicine'),(24,'Sports Medicine'),(25,'Thoracic Surgery'),(26,'Trauma Surgery & Critical Care'),(27,'Transplant'),(28,'Urologic Surgery'),(29,'Vascular & Endovascular Surgery'),(30,'Weight Loss Surgery'),(31,'Other');
/*!40000 ALTER TABLE `division` ENABLE KEYS */;
UNLOCK TABLES;
commit;

--
-- Dumping data for table `ethnicity`
--

LOCK TABLES `ethnicity` WRITE;
/*!40000 ALTER TABLE `ethnicity` DISABLE KEYS */;
set autocommit=0;
INSERT INTO `ethnicity` (`id`, `name`) VALUES (1,'Not Hispanic or Latino'),(2,'Hispanic or Latino'),(3,'Unknown or Not Reported'),(4,'DECLINED');
/*!40000 ALTER TABLE `ethnicity` ENABLE KEYS */;
UNLOCK TABLES;
commit;

--
-- Dumping data for table `ethnicity_mapping`
--

LOCK TABLES `ethnicity_mapping` WRITE;
/*!40000 ALTER TABLE `ethnicity_mapping` DISABLE KEYS */;
set autocommit=0;
/*!40000 ALTER TABLE `ethnicity_mapping` ENABLE KEYS */;
UNLOCK TABLES;
commit;

--
-- Dumping data for table `faculty_rank`
--

LOCK TABLES `faculty_rank` WRITE;
/*!40000 ALTER TABLE `faculty_rank` DISABLE KEYS */;
set autocommit=0;
INSERT INTO `faculty_rank` (`id`, `name`) VALUES (1,'Instructor'),(2,'Assistant Professor'),(3,'Associate Professor'),(4,'Professor'),(5,'Lecturer'),(6,'Fellow'),(7,'Other');
/*!40000 ALTER TABLE `faculty_rank` ENABLE KEYS */;
UNLOCK TABLES;
commit;

--
-- Dumping data for table `field`
--

LOCK TABLES `field` WRITE;
/*!40000 ALTER TABLE `field` DISABLE KEYS */;
set autocommit=0;
INSERT INTO `field` (`id`, `table_name`, `name`, `display_name`, `processing`, `sortable`, `type`) VALUES (1,'study','id','Study ID','DEFAULT',1,'Number'),(2,'study','catalyst_id','Catalyst ID','DEFAULT',1,'ShortString'),(3,'study','clinical_trial','Clinical Trial','DEFAULT',1,'ShortString'),(4,'study','crc_category','CRC Category','DEFAULT',1,'ShortSetStringUI'),(5,'study','crc_funded','CRC Funded','DEFAULT',1,'Boolean'),(6,'study','device_exemption','Device Exemption','DEFAULT',1,'ShortString'),(7,'study','expected_end_date','Expected End Date','DEFAULT',1,'Date'),(8,'study','expected_start_date','Expected Start Date','DEFAULT',1,'Date'),(9,'study','industry_initiated','Industry Initiated','DEFAULT',1,'Boolean'),(10,'study','irb','IRB','DEFAULT',1,'ShortString'),(11,'study','irb_approval_date','IRB Approval Date','DEFAULT',1,'Date'),(12,'study','irb_expiration','IRB Expiration Date','DEFAULT',1,'Date'),(13,'study','irb_renewal_date','IRB Renewal Date','DEFAULT',1,'Date'),(14,'study','local_id','Local ID','DEFAULT',1,'ShortString'),(15,'study','name','Study Name','DEFAULT',1,'ShortString'),(16,'study','new_drug','New Drug','DEFAULT',1,'ShortString'),(17,'study','pediatric','Pediatric','DEFAULT',1,'ShortSetStringUI'),(18,'study','protocol_number','Protocol Number','DEFAULT',1,'ShortString'),(19,'study','short_title','Short Title','DEFAULT',1,'ShortString'),(20,'study','spid','SPID','DEFAULT',1,'ShortString'),(21,'study','status_change','Study Most Recent Update','DEFAULT',1,'Date'),(22,'study','study_abstract','Study Abstract','DEFAULT',0,'LongSetString'),(23,'study','total_inpatient_visits','Total Inpatient Visits','DEFAULT',1,'Number'),(24,'study','total_outpatient_visits','Total Outpatient Visits','DEFAULT',1,'Number'),(25,'study','total_subjects','Total Subjects','DEFAULT',1,'Number'),(26,'study','institution','Institution','InstitutionFromStudy',1,'ShortSetStringDbLongName'),(27,'study','irb_institution','IRB Institution','IRBInstitution',1,'ShortSetStringDB'),(28,'study','study_status','Study Status','StudyStatus',1,'ShortSetStringDbShortName'),(29,'study','id','First Scheduled Visit Date','earliestBookedVisit',0,NULL),(30,'study','id','Most Recent Scheduled Visit Date','latestBookedVisit',0,NULL),(42,'user','institution','User Institution','Institution',1,'ShortSetStringDbLongName'),(43,'user','first_name','First Name','DEFAULT',1,'ShortString'),(44,'user','middle_name','Middle Name','DEFAULT',1,'ShortString'),(45,'user','last_name','Last Name','DEFAULT',1,'ShortString'),(46,'user','active','Active','DEFAULT',1,'Boolean'),(47,'user','ecommons_id','Username','DEFAULT',1,'ShortString'),(48,'user','department','Department','Department',1,'ShortSetStringDB'),(49,'user','division','Division','Division',1,'ShortSetStringDB'),(50,'user','faculty_rank','Faculty Rank','FacultyRank',1,'ShortSetStringDB'),(51,'user','institution_role','User Institutional Role','InstitutionRole',1,'ShortSetStringDB'),(53,'user','role','User Role','Role',1,'ShortSetStringDB'),(54,'user','email','Email Address','DEFAULT',1,'ShortString'),(55,'user','fax','Fax Number','DEFAULT',1,'ShortString'),(56,'user','notification_email','Notification Email','DEFAULT',1,'ShortString'),(57,'user','primary_phone','Phone Number','DEFAULT',1,'ShortString'),(58,'user','secondary_phone','Second Phone Number','DEFAULT',1,'ShortString'),(59,'user','credential','Credentials','Credential',1,'ShortSetStringDB'),(71,'study_funding_source_cai','one_to_four','Funding Source ID','DEFAULT',1,'ShortSetStringUI'),(72,'study_funding_source_cai','comment','Funding Source Comment','DEFAULT',0,'LongSetString'),(73,'study_funding_source_cai','grant_id','Grant ID','DEFAULT',1,'ShortString'),(74,'study_funding_source_cai','funding_source','Funding Source','FundingSource',1,'ShortSetStringDB'),(75,'study_funding_source_cai','project_start_date','Project Start Date','DEFAULT',1,'Date'),(76,'study_funding_source_cai','project_end_date','Project End Date','DEFAULT',1,'Date'),(77,'study_funding_source_cai','site_cost_center','Cost Center','DEFAULT',1,'ShortString'),(79,'study_funding_source_cai','total_direct_award','Total Direct Award','DEFAULT',1,'Number'),(80,'study_funding_source_cai','total_indirect_award','Total Indirect Award','DEFAULT',1,'Number'),(81,'study_funding_source_cai','centers_and_institutions','Centers And Institutions','CentersAndInstitutions',1,'ShortSetStringDB'),(91,'visit_template','id','Visit Template ID','DEFAULT',1,'Number'),(92,'visit_template','active','Active','DEFAULT',1,'Boolean'),(93,'visit_template','approved','Approved','DEFAULT',1,'Boolean'),(94,'visit_template','comment','Visit Template Comment','DEFAULT',0,'LongSetString'),(95,'visit_template','created_date','Visit Template Created Date','DEFAULT',1,'Date'),(96,'visit_template','duration','Visit Template Duration','DEFAULT',1,'Number'),(97,'visit_template','institution_non_crc','Does part of the visit occur at Non-CRC?','DEFAULT',1,'Boolean'),(98,'visit_template','last_update_time','Visit Template Most Recent Update','DEFAULT',1,'Date'),(99,'visit_template','name','Visit Template Name','DEFAULT',1,'ShortString'),(100,'visit_template','nursing','Nursing Level','DEFAULT',1,'Number'),(101,'visit_template','nutrition','Nutrition Level','DEFAULT',1,'Number'),(102,'visit_template','processing','Processing Level','DEFAULT',1,'Number'),(103,'visit_template','setUp','Setup Level','DEFAULT',1,'Number'),(104,'visit_template','relative_time','In Relative Time?','DEFAULT',1,'Boolean'),(105,'visit_template','research_pharmacy','Does this involve Research Pharmacy?','DEFAULT',1,'Boolean'),(106,'visit_template','non_institution_non_crc','Does part of the visit occur Off Institution?','DEFAULT',1,'Boolean'),(108,'visit_template','sublocation','Sub-Location','Sublocation',1,'ShortSetStringDB'),(109,'visit_template','visit_type','Visit Template Visit Type','VisitType',1,'ShortSetStringDB'),(121,'booked_visit','id','Scheduled Visit ID','DEFAULT',1,'Number'),(122,'booked_visit','cancel_date','Cancel Date','DEFAULT',1,'Date'),(123,'booked_visit','check_in_date','Check In Date','DEFAULT',1,'Date'),(124,'booked_visit','check_out_date','Check Out Date','DEFAULT',1,'Date'),(125,'booked_visit','comment','Scheduled Visit Comment','DEFAULT',0,'LongSetString'),(126,'booked_visit','name','Scheduled Visit Name','DEFAULT',1,'ShortString'),(127,'booked_visit','ommitted_activities','Omitted Activities?','DEFAULT',1,'Boolean'),(128,'booked_visit','scheduled_end_time','Scheduled End Time','DEFAULT',1,NULL),(129,'booked_visit','scheduled_start_time','Scheduled Start Time','DEFAULT',1,NULL),(130,'booked_visit','scheduling_flavor','Scheduling Type','DEFAULT',1,'ShortString'),(131,'booked_visit','scheduling_time','User Scheduled Date','DEFAULT',1,'Date'),(132,'user','ecommons_id','User that Scheduled','DEFAULT',1,'ShortString'),(133,'booked_visit','vary_duration','Did this visit vary?','DEFAULT',1,'Boolean'),(134,'booked_visit','appointment_status','Appointment Status','AppointmentStatus',1,'ShortSetStringDB'),(135,'booked_visit','appointment_status_reason','Appointment Status Reason','AppointmentStatusReason',1,'ShortSetStringDB'),(136,'booked_visit','cancel_status','Cancel Status','CancellationStatus',1,'ShortSetStringDB'),(142,'booked_visit','visit_type','Visit Type','VisitType',1,'ShortSetStringDB'),(143,'booked_visit','scheduled_end_time,scheduled_start_time','Scheduled Duration','scheduleDuration',0,NULL),(144,'booked_visit','check_out_date,check_in_date','Actual Duration','actualDuration',0,NULL),(145,'booked_visit','check_out_date,check_in_date','# of Overnights','actualDurationOvernights',0,NULL),(147,'booked_visit','scheduled_end_time,scheduled_start_time,check_out_date,check_in_date','Visit Duration Variation','visitVariation',0,NULL),(163,'subject','active','Active','DEFAULT',1,'Boolean'),(164,'subject','first_name','First Name','decrypt',0,NULL),(165,'subject','middle_name','Middle Name','decrypt',0,NULL),(166,'subject','last_name','Last Name','decrypt',0,NULL),(167,'subject','full_name','Full Name','decrypt',0,NULL),(168,'booked_visit','subject_mrn','MRN','MrnFromSubjectMrn',0,NULL),(169,'subject','birthdate','DOB','DEFAULT',1,'Date'),(170,'subject','street_address1','Street Address','decrypt',0,NULL),(171,'subject','street_address2','Street Address 2','decrypt',0,NULL),(172,'subject','city','City','decrypt',0,NULL),(173,'subject','state','State','State',1,'ShortSetStringDB'),(174,'subject','zip','Zip Code','decrypt',0,NULL),(175,'subject','country','Country','Country',1,'ShortSetStringDB'),(176,'subject','primary_contact_number','Phone Number','decrypt',0,NULL),(177,'subject','secondary_contact_number','Second Phone Number','decrypt',0,NULL),(178,'subject','ethnicity','Ethnicity','Ethnicity',1,'ShortSetStringDB'),(179,'subject','gender','Gender','Gender',1,'ShortSetStringDB'),(180,'subject','race','Race','Race',1,'ShortSetStringDB'),(181,'subject','comment','Comment','DEFAULT',0,'LongSetString'),(182,'subject','created_date','Subject Created Date','DEFAULT',1,'Date'),(192,'booked_resource','id','Scheduled Resource ID','DEFAULT',1,'Number'),(193,'resource','type','Resource Type','DEFAULT',1,'Enum'),(194,'resource','name','Resource Name','Resource',1,'ShortSetStringDbIdBack'),(195,'template_resource','billable','Billable In Template','DEFAULT',1,'Boolean'),(196,'booked_resource','duration','Duration','DEFAULT',1,'Number'),(197,'booked_resource','scheduled_start_time','Resource Start Time','DEFAULT',1,'Date'),(198,'booked_resource','scheduled_end_time','Resource End Time','DEFAULT',1,'Date'),(199,'template_resource','id','Template Resource ID','DEFAULT',1,'Number'),(200,'booked_resource','billable','Billable When Scheduled','DEFAULT',1,'Boolean'),(211,'override_booked_resource_annotations','id','Scheduled Resource Activity ID','DEFAULT',1,'Number'),(212,'line_level_annotations','name','Scheduled Resource Activity','DefaultPlusAnnotations',1,'ShortSetStringDbIdBack'),(213,'override_booked_resource_annotations','quantity','Scheduled Resource Activity Quantity','DEFAULT',1,'Number'),(214,'override_booked_resource_annotations','comment','Scheduled Resource Activity Comment','DEFAULT',0,'LongSetString'),(221,'template_resource_annotations','id','Template Resource Activity ID','DEFAULT',1,'Number'),(222,'template_resource_annotations','comment','Template Resource Activity Comment','DEFAULT',0,'LongSetString'),(223,'template_resource_annotations','quantity','Template Resource Activity Quantity','DEFAULT',1,'Number'),(224,'line_level_annotations','name','Template Resource Activity','DefaultPlusAnnotations',1,'ShortSetStringDbIdBack'),(242,'template_resource','alternate','Uses Standard Alternate?','DEFAULT',1,'Boolean'),(243,'template_resource','id','Resource Sub-Location','SublocationFromTr',1,'ShortSetStringDB'),(244,'template_resource','resource','Resource Name','ResourceFromTr',1,'ShortSetStringDbIdBack'),(245,'template_resource','billable','Billable?','DEFAULT',1,'Boolean'),(246,'template_resource','created_date','Template Resource Most Recent Update','DEFAULT',1,'Date'),(247,'template_resource','duration','Resource Duration','DEFAULT',1,'Number'),(248,'template_resource','start_minutes','Start Time','DEFAULT',1,'Date'),(249,'template_resource','end_date','End Time','DEFAULT',1,'Date'),(250,'template_resource','flexible','Flexible?','DEFAULT',1,'Boolean'),(251,'template_resource','floatable','Floatable?','DEFAULT',1,'Boolean'),(252,'template_resource','float_start','Float Start Time','DEFAULT',1,'Date'),(253,'template_resource','float_end','Float End Time','DEFAULT',1,'Date'),(254,'template_resource','group_id','Group ID','DEFAULT',1,'ShortString'),(259,'template_resource','start_minutes','Resource Start Time','DEFAULT',1,'Date'),(260,'template_resource','end_date','Resource End Time','DEFAULT',1,'Date');
/*!40000 ALTER TABLE `field` ENABLE KEYS */;
UNLOCK TABLES;
commit;

--
-- Dumping data for table `funding_source`
--

LOCK TABLES `funding_source` WRITE;
/*!40000 ALTER TABLE `funding_source` DISABLE KEYS */;
set autocommit=0;
INSERT INTO `funding_source` (`id`, `name`) VALUES (1,'Pilot Grant'),(2,'Federal PHS'),(3,'Foundation'),(4,'Industry'),(5,'Institutional/Departmental Funds'),(6,'Other Federal'),(7,'Other');
/*!40000 ALTER TABLE `funding_source` ENABLE KEYS */;
UNLOCK TABLES;
commit;

--
-- Dumping data for table `gender`
--

LOCK TABLES `gender` WRITE;
/*!40000 ALTER TABLE `gender` DISABLE KEYS */;
set autocommit=0;
INSERT INTO `gender` (`id`, `code`, `name`) VALUES (1,'M','Male'),(2,'F','Female'),(3,'FM','Female to Male'),(4,'MF','Male to Female'),(5,'UNREPORTED','Unreported'),(6,'DECLINED','Declined');
/*!40000 ALTER TABLE `gender` ENABLE KEYS */;
UNLOCK TABLES;
commit;

--
-- Dumping data for table `institution`
--

LOCK TABLES `institution` WRITE;
/*!40000 ALTER TABLE `institution` DISABLE KEYS */;
set autocommit=0;
INSERT INTO `institution` (`id`, `long_name`, `name`) VALUES (1,'Demo Institution 1','DEMO1'),(2,'Demo Institution 2','DEMO2'),(3,'Demo Institution 3','DEMO3'),(4,'Demo Institution 4','DEMO4'),(5,'Demo Institution 5','DEMO5'),(6,'Demo Institution 6','DEMO6'),(7,'Demo Institution 7','DEMO7'),(8,'Demo Institution 8','DEMO8'),(9,'Demo Institution 9','DEMO9'),(10,'Demo Institution 10','DEMO10'),(11,'Demo Institution 11','DEMO11'),(12,'Demo Institution 12','DEMO12'),(13,'Demo Institution 13','DEMO13'),(14,'Demo Institution 14','DEMO14'),(15,'Demo Institution 15','DEMO15'),(16,'Demo Institution 16','DEMO16'),(17,'Demo Institution 17','DEMO17'),(18,'Demo Institution 18','DEMO18'),(19,'Demo Institution 19','DEMO19'),(20,'Demo Institution 20','DEMO20'),(21,'Demo Institution 21','DEMO21'),(22,'Demo Institution 22','DEMO22'),(23,'Demo Institution 23','DEMO23'),(24,'Demo Institution 24','DEMO24'),(25,'Demo Institution 25','DEMO25'),(26,'Demo Institution 26','DEMO26'),(27,'Demo Institution 27','DEMO27'),(28,'Demo Institution 28','DEMO28'),(29,'Demo Institution 29','DEMO29'),(30,'Demo Institution 30','DEMO30'),(31,'Demo Institution 31','DEMO31'),(32,'Demo Institution 32','DEMO32'),(33,'Demo Institution 33','DEMO33'),(34,'Demo Institution 34','DEMO34'),(35,'Demo Institution 35','DEMO35');
/*!40000 ALTER TABLE `institution` ENABLE KEYS */;
UNLOCK TABLES;
commit;

--
-- Dumping data for table `institution_role`
--

LOCK TABLES `institution_role` WRITE;
/*!40000 ALTER TABLE `institution_role` DISABLE KEYS */;
set autocommit=0;
INSERT INTO `institution_role` (`id`, `description`, `name`, `type`) VALUES (1,'Super Admin has access to all modules and functionality in the System','Super Admin','ROLE_SUPER_ADMIN'),(2,'ND or RD Manager / AD has limited access to modules and functionality in the System','Resource Manager','ROLE_RESOURCE_MANAGER'),(3,'Schedulers have access limited to modules and functionality in the System','Schedulers','ROLE_SCHEDULER'),(4,'Study Staff has access to Resource and Appointments modules and functionality in the System','Study Staff','ROLE_STUDY_STAFF'),(5,'Front Desk has very limited access to modules and functionality in the System','Front Desk','ROLE_FRONT_DESK'),(6,'General View has very limited access to modules and functionality in the System','General View','ROLE_GENERAL_VIEW');
/*!40000 ALTER TABLE `institution_role` ENABLE KEYS */;
UNLOCK TABLES;
commit;

--
-- Dumping data for table `irb_institution`
--

LOCK TABLES `irb_institution` WRITE;
/*!40000 ALTER TABLE `irb_institution` DISABLE KEYS */;
set autocommit=0;
INSERT INTO `irb_institution` (`id`, `name`) VALUES (1,'IRB Demo Inst 1'),(2,'IRB Demo Inst 2'),(3,'IRB Demo Inst 3'),(4,'IRB Demo Inst 4'),(5,'IRB Demo Inst 5'),(6,'IRB Demo Inst 6'),(7,'IRB Demo Inst 7'),(8,'IRB Demo Inst 8'),(9,'IRB Demo Inst 9'),(10,'IRB Demo Inst 10'),(11,'IRB Demo Inst 11'),(12,'IRB Demo Inst 12'),(13,'IRB Demo Inst 13'),(14,'IRB Demo Inst 14'),(15,'IRB Demo Inst 15'),(16,'IRB Demo Inst 16'),(17,'IRB Demo Inst 17'),(18,'IRB Demo Inst 18'),(19,'IRB Demo Inst 19'),(20,'IRB Demo Inst 20'),(21,'IRB Demo Inst 21'),(22,'IRB Demo Inst 22'),(23,'IRB Demo Inst 23'),(24,'IRB Demo Inst 24'),(25,'IRB Demo Inst 25'),(26,'IRB Demo Inst 26'),(27,'IRB Demo Inst 27'),(28,'IRB Demo Inst 28'),(29,'IRB Demo Inst 29'),(30,'IRB Demo Inst 30'),(31,'IRB Demo Inst 31'),(32,'IRB Demo Inst 32'),(33,'IRB Demo Inst 34'),(34,'Other');
/*!40000 ALTER TABLE `irb_institution` ENABLE KEYS */;
UNLOCK TABLES;
commit;

--
-- Dumping data for table `line_level_annotations`
--

LOCK TABLES `line_level_annotations` WRITE;
/*!40000 ALTER TABLE `line_level_annotations` DISABLE KEYS */;
set autocommit=0;
INSERT INTO `line_level_annotations` (`id`, `name`, `quantifiable`) VALUES (1,'Verify Informed Consent',0),(2,'Assessment Nursing Intake - Adult',0),(3,'Assessment Nursing Intake - Pediatrics',0),(4,'Vitals - Simple',1),(5,'Vitals - Complex',1),(6,'EKG 12 Lead',1),(7,'IV access simple - Adult',1),(8,'IV access simple - Pediatrics',1),(9,'IV access complex adult',1),(10,'IV access complex pedi',1),(11,'Monitor and flush drawline',1),(12,'Medication Administration Simple - Adult',1),(13,'Medication Administration Complex - Adult',1),(14,'Medication Administration High Risk IV Push - Adult',1),(15,'Medication Administration Simple - Pediatrics',1),(16,'Medication Administration Complex - Pediatrics',1),(17,'Medication Administration High Risk IV Push - Pediatrics',1),(18,'Monitoring 1:1',0),(19,'Anthropometry',1),(20,'Clinical Nursing Care',0),(21,'Other Specimen Collection Simple',1),(22,'Other Specimen Collection Complex',1),(23,'Patient / family teaching Simple',0),(24,'Patient / family teaching Complex',0),(25,'Point of care testing - simple',1),(26,'Point of care testing - complex',1),(27,'Other',0),(28,'Blood Collection Simple - Pedi',1),(29,'Blood Collection Complex - Pedi',1),(30,'Blood Collection Simple - Adult',1),(31,'Blood Collection Complex - Adult',1),(32,'Travel',0),(33,'Special Protocol Procedure - Simple',1),(34,'Special Protocol Procedure - Complex',1),(35,'Specimen Processing',1),(36,'Protocol Equipment Operation',1),(37,'Clamp Adult',1),(38,'Non-Nutrition Staff: Meal/snack prep and/or delivery',1),(39,'History & Physical - Complete',0),(40,'History & Physical - Targeted',0),(41,'Interprets patient data',0),(42,'Orders labs and diagnostic tests',1),(43,'Assess for AE & SAE',0),(44,'Admitting/Discharging Inpatient',0),(45,'Anthropometry',1),(46,'Education',0),(47,'Food Challenge Preparation',1),(48,'Food Record/Food Recall',1),(49,'Metabolic Sample Collection',1),(50,'Nutrition Documentation',0),(51,'Nutrition Travel',0),(52,'Questionnaire(s)',1),(53,'Service (includes Technician)',1),(54,'Unassisted (Non-CRC personnel performs ultrasound)',1),(55,'Start',1),(56,'Monitor',1),(57,'Download',1),(58,'Body Composition Scan',1),(59,'Bone Scan',1),(60,'DXA Specialized Analysis/Report',1),(61,'Quantity',1),(62,'Comment',0),(63,'Breakfast',1),(64,'Lunch',1),(65,'Dinner',1),(66,'Confirmation of Consent',0),(67,'Standard Research Nursing Care',1),(68,'Blood Collection Complex',1),(69,'Blood Collection Simple',1),(70,'Special Protocol Procedure/Equipment Complex',1),(71,'Special Protocol Procedure/Equipment Simple',1),(72,'Hip Structural Analysis',1);
/*!40000 ALTER TABLE `line_level_annotations` ENABLE KEYS */;
UNLOCK TABLES;
commit;

--
-- Dumping data for table `race`
--

LOCK TABLES `race` WRITE;
/*!40000 ALTER TABLE `race` DISABLE KEYS */;
set autocommit=0;
INSERT INTO `race` (`id`, `name`) VALUES (1,'White'),(2,'Black or African American'),(3,'Asian'),(4,'American Indian/ Alaskan Native'),(5,'Native Hawaiian or Other Pacific Islander'),(6,'More than one race'),(7,'Unknown or Not Reported'),(8,'DECLINED');
/*!40000 ALTER TABLE `race` ENABLE KEYS */;
UNLOCK TABLES;
commit;

--
-- Dumping data for table `report`
--

LOCK TABLES `report` WRITE;
/*!40000 ALTER TABLE `report` DISABLE KEYS */;
set autocommit=0;
INSERT INTO `report` (`id`, `description`, `title`, `name`) VALUES (1,'List of resources needed by visit for a particular date range ','Daily Resource Usage','daily_resource'),(2,'List of study subjects whose visits are scheduled to begin for a particular date range - and the resources needed for each visit ','Daily Admission and Check-In','daily_adm'),(3,'List of Scheduled, Checked-In meals, snacks and rooms by subject for a particular date range','Metabolic Kitchen by Subject','meta_kitchen'),(4,'List of Cancellations and Reasons by study, visit and subject for a particular date range','Cancellations and Reasons','cancellations'),(5,'List of study Visits (Scheduled, Checked-In) during a particular date range for studies which indicated use of research pharmacy, listed by subject','Weekly Pharmacy','weekly_pharm'),(6,'List of studies assigned to each nurse, including IRB expiration date ','Protocols by Nurse','proto_by_nurse'),(7,'List of Visits by location, visit type, check-in information, subject, and study for a particular date range','Study Visit Location','study_visit_location'),(8,'List of checked-out visit information by PI and department for a particular date range ','Department and PI','dept_and_pi'),(9,'List of Checked-Out Visits by billable resource, with over midnight calculation for a particular date range','Billing','billing'),(10,'List of Checked-Out billable resources with PI information for a particular date range','Billable Resources','billable_resources'),(11,'List of Checked-Out Visits by billable resource sorted by PI; can run single PI reports for a particular date range','Billing by Investigator','billing_by_investigator'),(12,'List of changes to study visit statuses: time of change, user, and visit details for a particular date range','Operations Management: Transactions','transactions'),(13,'List of Checked-Out Visits by completed, terminated prior to completion, and Reasons by visit, PI and study for a particular date range','Census','census'),(14,'List of study subjects in the database who do not have a Scheduled visit, and do not have a Checked-Out status','Subject Purge','subject_purge'),(15,'List of Overrides by user, date, reason, and visit','Override','override'),(16,'List of study subject views by staff','Staff Views of Subjects by MRN','subject_audit_staff_views'),(17,'List of staff views of study subject','Staff Views of Subjects by User','staff_audit_subject_views'),(18,'ALL FIELDS for all studies','Study Data','study_data'),(19,'ALL FIELDS of User records','User Data','user_data'),(20,'Daily overview of Scheduled and Checked-In Visits in a specific date range for booked resources','Daily Overview','daily_overview'),(21,'Off Unit Activity for all the Scheduled and Checked-In Visits','Off Unit Activity','off_unit'),(22,'Level of service for all the checked-out visits','Level of Service','level_of_service'),(23,'List of Scheduled, Checked-In meals, snacks and rooms by time for a particular date range','Metabolic Kitchen by Time','meta_kitchen_by_time'),(25,'A list of visits, by subject, and their status history','Visit Status History By Subject','subject_visit_history'),(26,'CRC Availability','CRC Availability','crc_availability'),(27,'A list of subjects with booked visits and the visit status','Visit History by Subject','study_subject_visit'),(28,'Resources, duration, and workload levels for all visit templates','Workload and Resources Report','resource_level_of_service'),(29,'ALL FIELDS on all visit templates','Visit Template Data','visit_template_data'),(31,'Study Status Change Report','Study Status Change','study_status_change'),(32,'A list of visits with scheduled start time, end time, actual duration and visit type in a particular date range','Visit Duration By Visit Type','visit_duration_by_visit_type'),(33,'A count of levels of service per visit in a specific time range','Booked Visit Service Levels by Visit Type','booked_visit_service_level_by_visit_type'),(35,'A count of Ancillaries Only visits by protocol (category A, B, D) in a specific date range','Ancillaries Only Visits By Protocol','ancillary_only_by_protocol'),(36,'List of studies assigned to each nutritionist, including IRB expiration date','Protocols By Nutritionist','proto_by_nutritionist'),(37,'List Of Visits Flagged for Edit.','Visits Flagged for Edit','visits_flagged_edit');
/*!40000 ALTER TABLE `report` ENABLE KEYS */;
UNLOCK TABLES;
commit;

--
-- Dumping data for table `report_template`
--

LOCK TABLES `report_template` WRITE;
/*!40000 ALTER TABLE `report_template` DISABLE KEYS */;
set autocommit=0;
INSERT INTO `report_template` (`id`, `display_name`, `type`, `date_bound`, `graph_type`) VALUES (1,'Scheduled Visit Data','Administrative',1,'BookedVisit'),(2,'Study & Visit Template Data','Administrative',0,'StudyAndVisitTemplate');
/*!40000 ALTER TABLE `report_template` ENABLE KEYS */;
UNLOCK TABLES;
commit;

--
-- Dumping data for table `role`
--

LOCK TABLES `role` WRITE;
/*!40000 ALTER TABLE `role` DISABLE KEYS */;
set autocommit=0;
INSERT INTO `role` (`id`, `name`, `type`) VALUES (1,'Study Coordinator - Research Asst','ROLE_STUDY_COORDINATOR_RESEARCH_ASST'),(2,'PI','ROLE_PI'),(3,'Co-Investigator','ROLE_CO_INVESTIGATOR'),(4,'Other Study Staff','ROLE_OTHER_STUDY_STAFF'),(5,'Nurse','ROLE_NURSE'),(6,'Nutritionist','ROLE_NUTRITIONIST'),(7,'CRC Administration','ROLE_CRC_ADMINISTRATION'),(8,'Lab','ROLE_LAB'),(9,'Administrative Director','ROLE_ADMINISTRATIVE_DIRECTOR'),(10,'Nurse Manager Director','ROLE_NURSE_MANAGER_DIRECTOR'),(11,'Nutrition Manager','ROLE_NUTRITION_MANAGER'),(12,'Final Approver','ROLE_FINAL_APPROVER'),(13,'Physician','ROLE_PHYSICIAN'),(14,'Physician2','ROLE_PHYSICIAN2'),(15,'Scheduler','ROLE_SCHEDULER'),(16,'Scheduler2','ROLE_SCHEDULER2'),(18,'Associate Nurse','ROLE_ASSOCIATE_NURSE'),(19,'Associate RD','ROLE_ASSOCIATE_RD');
/*!40000 ALTER TABLE `role` ENABLE KEYS */;
UNLOCK TABLES;
commit;

--
-- Dumping data for table `state`
--

LOCK TABLES `state` WRITE;
/*!40000 ALTER TABLE `state` DISABLE KEYS */;
set autocommit=0;
INSERT INTO `state` (`id`, `name`) VALUES (1,'Massachusetts'),(2,'Alabama'),(3,'Alaska'),(4,'Arizona'),(5,'Arkansas'),(6,'California'),(7,'Colorado'),(8,'Connecticut'),(9,'Delaware'),(10,'Florida'),(11,'Georgia'),(12,'Hawaii'),(13,'Idaho'),(14,'Illinois'),(15,'Indiana'),(16,'Iowa'),(17,'Kansas'),(18,'Kentucky'),(19,'Louisiana'),(20,'Maine'),(21,'Maryland'),(22,'Michigan'),(23,'Minnesota'),(24,'Mississippi'),(25,'Missouri'),(26,'Montana'),(27,'Nebraska'),(28,'Nevada'),(29,'New Hampshire'),(30,'New Jersey'),(31,'New Mexico'),(32,'New York'),(33,'North Carolina'),(34,'North Dakota'),(35,'Ohio'),(36,'Oklahoma'),(37,'Oregon'),(38,'Pennsylvania'),(39,'Rhode Island'),(40,'South Carolina'),(41,'South Dakota'),(42,'Tennessee'),(43,'Texas'),(44,'Utah'),(45,'Vermont'),(46,'Virginia'),(47,'Washington'),(48,'West Virginia'),(49,'Wisconsin'),(50,'Wyoming'),(51,'American Samoa'),(52,'District of Columbia'),(53,'Federated States of Micronesia'),(54,'Guam'),(55,'Marshall Islands'),(56,'Northern Mariana Islands'),(57,'Palau'),(58,'Puerto Rico'),(59,'Virgin Islands');
/*!40000 ALTER TABLE `state` ENABLE KEYS */;
UNLOCK TABLES;
commit;

--
-- Dumping data for table `study_status`
--

LOCK TABLES `study_status` WRITE;
/*!40000 ALTER TABLE `study_status` DISABLE KEYS */;
set autocommit=0;
INSERT INTO `study_status` (`id`, `name`, `short_name`, `is_pending`, `is_open`, `is_closed`) VALUES (1,'IRB_PROCESS','PENDING',1,0,0),(2,'OPEN','OPEN',0,1,0),(3,'CLOSED','CLOSED',0,0,1);
/*!40000 ALTER TABLE `study_status` ENABLE KEYS */;
UNLOCK TABLES;
commit;

--
-- Dumping data for table `sub_category`
--

LOCK TABLES `sub_category` WRITE;
/*!40000 ALTER TABLE `sub_category` DISABLE KEYS */;
set autocommit=0;
INSERT INTO `sub_category` (`id`, `parent_id`, `child_id`, `report_template_id`, `expanded`) VALUES (259,1,2,1,0),(260,1,3,1,0),(261,4,5,1,0),(262,4,6,1,0),(263,7,8,1,0),(264,7,9,1,0),(265,2,10,1,1),(266,2,11,1,1),(267,2,12,1,1),(268,2,13,1,1),(269,2,14,1,1),(270,2,15,1,1),(271,2,16,1,1),(272,2,17,1,1),(273,2,18,1,1),(274,2,19,1,1),(275,3,20,1,1),(276,3,21,1,1),(277,3,22,1,1),(278,3,23,1,1),(279,1,2,2,0),(280,1,3,2,0),(281,24,9,2,0),(282,2,10,2,1),(283,2,11,2,1),(284,2,12,2,1),(285,2,13,2,1),(286,2,14,2,1),(287,2,15,2,1),(288,2,16,2,1),(289,2,17,2,1),(290,2,18,2,1),(291,2,19,2,1),(292,3,20,2,1),(293,3,21,2,1),(294,3,22,2,1),(295,3,23,2,1);
/*!40000 ALTER TABLE `sub_category` ENABLE KEYS */;
UNLOCK TABLES;
commit;

--
-- Dumping data for table `sublocation`
--

LOCK TABLES `sublocation` WRITE;
/*!40000 ALTER TABLE `sublocation` DISABLE KEYS */;
set autocommit=0;
INSERT INTO `sublocation` (`id`, `name`, `institution`) VALUES (1,'Sub-Location 1',1),(2,'Sub-Location 2',1),(3,'Sub-Location 3',1),(4,'Sub-Location 4',1);
/*!40000 ALTER TABLE `sublocation` ENABLE KEYS */;
UNLOCK TABLES;
commit;

--
-- Dumping data for table `template_category`
--

LOCK TABLES `template_category` WRITE;
/*!40000 ALTER TABLE `template_category` DISABLE KEYS */;
set autocommit=0;
INSERT INTO `template_category` (`id`, `report_template_id`, `category_id`, `expanded`) VALUES (259,1,1,0),(260,1,4,0),(261,1,7,0),(262,2,1,0),(263,2,4,0),(264,2,24,0);
/*!40000 ALTER TABLE `template_category` ENABLE KEYS */;
UNLOCK TABLES;
commit;

--
-- Dumping data for table `template_category_field`
--

LOCK TABLES `template_category_field` WRITE;
/*!40000 ALTER TABLE `template_category_field` DISABLE KEYS */;
set autocommit=0;
INSERT INTO `template_category_field` (`id`, `report_template_id`, `field_id`, `category_id`, `suffix`, `selected`, `csv_order`, `csv_order2`) VALUES (1,1,1,1,'',0,1,1),(2,1,2,1,'',0,11,1),(3,1,3,1,'',0,12,1),(4,1,4,1,'',0,13,1),(5,1,5,1,'',0,14,1),(6,1,6,1,'',0,15,1),(7,1,7,1,'',0,7,1),(8,1,8,1,'',0,6,1),(9,1,9,1,'',0,16,1),(10,1,10,1,'',0,18,1),(11,1,11,1,'',0,8,1),(12,1,12,1,'',0,10,1),(13,1,13,1,'',0,9,1),(14,1,14,1,'',1,3,1),(15,1,15,1,'',0,2,1),(16,1,16,1,'',0,20,1),(17,1,17,1,'',0,21,1),(18,1,18,1,'',0,22,1),(19,1,19,1,'',0,23,1),(20,1,20,1,'',0,24,1),(21,1,21,1,'',0,5,1),(22,1,22,1,'',0,25,1),(23,1,23,1,'',0,26,1),(24,1,24,1,'',0,27,1),(25,1,25,1,'',0,28,1),(26,1,26,1,'',0,17,1),(27,1,27,1,'',0,19,1),(28,1,28,1,'',0,4,1),(29,1,42,10,'_inv',0,15,2),(30,1,43,10,'_inv',1,2,2),(31,1,44,10,'_inv',0,3,2),(32,1,45,10,'_inv',1,4,2),(33,1,46,10,'_inv',0,5,2),(34,1,47,10,'_inv',0,1,2),(35,1,48,10,'_inv',0,12,2),(36,1,49,10,'_inv',0,13,2),(37,1,50,10,'_inv',1,14,2),(38,1,51,10,'_inv',0,17,2),(39,1,52,10,'_inv',0,0,2),(40,1,53,10,'_inv',0,18,2),(41,1,54,10,'_inv',0,6,2),(42,1,55,10,'_inv',0,10,2),(43,1,56,10,'_inv',0,7,2),(44,1,57,10,'_inv',0,8,2),(45,1,58,10,'_inv',0,9,2),(46,1,59,10,'_inv',0,11,2),(47,1,42,11,'_doc',0,15,3),(48,1,43,11,'_doc',0,2,3),(49,1,44,11,'_doc',0,3,3),(50,1,45,11,'_doc',0,4,3),(51,1,46,11,'_doc',0,5,3),(52,1,47,11,'_doc',0,1,3),(53,1,48,11,'_doc',0,12,3),(54,1,49,11,'_doc',0,13,3),(55,1,50,11,'_doc',0,14,3),(56,1,51,11,'_doc',0,17,3),(57,1,52,11,'_doc',0,0,3),(58,1,53,11,'_doc',0,18,3),(59,1,54,11,'_doc',0,6,3),(60,1,55,11,'_doc',0,10,3),(61,1,56,11,'_doc',0,7,3),(62,1,57,11,'_doc',0,8,3),(63,1,58,11,'_doc',0,9,3),(64,1,59,11,'_doc',0,11,3),(65,1,42,12,'_doc2',0,15,4),(66,1,43,12,'_doc2',0,2,4),(67,1,44,12,'_doc2',0,3,4),(68,1,45,12,'_doc2',0,4,4),(69,1,46,12,'_doc2',0,5,4),(70,1,47,12,'_doc2',0,1,4),(71,1,48,12,'_doc2',0,12,4),(72,1,49,12,'_doc2',0,13,4),(73,1,50,12,'_doc2',0,14,4),(74,1,51,12,'_doc2',0,17,4),(75,1,52,12,'_doc2',0,0,4),(76,1,53,12,'_doc2',0,18,4),(77,1,54,12,'_doc2',0,6,4),(78,1,55,12,'_doc2',0,10,4),(79,1,56,12,'_doc2',0,7,4),(80,1,57,12,'_doc2',0,8,4),(81,1,58,12,'_doc2',0,9,4),(82,1,59,12,'_doc2',0,11,4),(83,1,42,13,'_sch',0,15,5),(84,1,43,13,'_sch',0,2,5),(85,1,44,13,'_sch',0,3,5),(86,1,45,13,'_sch',0,4,5),(87,1,46,13,'_sch',0,5,5),(88,1,47,13,'_sch',0,1,5),(89,1,48,13,'_sch',0,12,5),(90,1,49,13,'_sch',0,13,5),(91,1,50,13,'_sch',0,14,5),(92,1,51,13,'_sch',0,17,5),(93,1,52,13,'_sch',0,0,5),(94,1,53,13,'_sch',0,18,5),(95,1,54,13,'_sch',0,6,5),(96,1,55,13,'_sch',0,10,5),(97,1,56,13,'_sch',0,7,5),(98,1,57,13,'_sch',0,8,5),(99,1,58,13,'_sch',0,9,5),(100,1,59,13,'_sch',0,11,5),(101,1,42,14,'_sch2',0,15,6),(102,1,43,14,'_sch2',0,2,6),(103,1,44,14,'_sch2',0,3,6),(104,1,45,14,'_sch2',0,4,6),(105,1,46,14,'_sch2',0,5,6),(106,1,47,14,'_sch2',0,1,6),(107,1,48,14,'_sch2',0,12,6),(108,1,49,14,'_sch2',0,13,6),(109,1,50,14,'_sch2',0,14,6),(110,1,51,14,'_sch2',0,17,6),(111,1,52,14,'_sch2',0,0,6),(112,1,53,14,'_sch2',0,18,6),(113,1,54,14,'_sch2',0,6,6),(114,1,55,14,'_sch2',0,10,6),(115,1,56,14,'_sch2',0,7,6),(116,1,57,14,'_sch2',0,8,6),(117,1,58,14,'_sch2',0,9,6),(118,1,59,14,'_sch2',0,11,6),(119,1,42,15,'_sch3',0,15,7),(120,1,43,15,'_sch3',0,2,7),(121,1,44,15,'_sch3',0,3,7),(122,1,45,15,'_sch3',0,4,7),(123,1,46,15,'_sch3',0,5,7),(124,1,47,15,'_sch3',0,1,7),(125,1,48,15,'_sch3',0,12,7),(126,1,49,15,'_sch3',0,13,7),(127,1,50,15,'_sch3',0,14,7),(128,1,51,15,'_sch3',0,17,7),(129,1,52,15,'_sch3',0,0,7),(130,1,53,15,'_sch3',0,18,7),(131,1,54,15,'_sch3',0,6,7),(132,1,55,15,'_sch3',0,10,7),(133,1,56,15,'_sch3',0,7,7),(134,1,57,15,'_sch3',0,8,7),(135,1,58,15,'_sch3',0,9,7),(136,1,59,15,'_sch3',0,11,7),(137,1,42,16,'_nrs',0,15,8),(138,1,43,16,'_nrs',0,2,8),(139,1,44,16,'_nrs',0,3,8),(140,1,45,16,'_nrs',0,4,8),(141,1,46,16,'_nrs',0,5,8),(142,1,47,16,'_nrs',0,1,8),(143,1,48,16,'_nrs',0,12,8),(144,1,49,16,'_nrs',0,13,8),(145,1,50,16,'_nrs',0,14,8),(146,1,51,16,'_nrs',0,17,8),(147,1,52,16,'_nrs',0,0,8),(148,1,53,16,'_nrs',0,18,8),(149,1,54,16,'_nrs',0,6,8),(150,1,55,16,'_nrs',0,10,8),(151,1,56,16,'_nrs',0,7,8),(152,1,57,16,'_nrs',0,8,8),(153,1,58,16,'_nrs',0,9,8),(154,1,59,16,'_nrs',0,11,8),(155,1,42,17,'_nrs2',0,15,9),(156,1,43,17,'_nrs2',0,2,9),(157,1,44,17,'_nrs2',0,3,9),(158,1,45,17,'_nrs2',0,4,9),(159,1,46,17,'_nrs2',0,5,9),(160,1,47,17,'_nrs2',0,1,9),(161,1,48,17,'_nrs2',0,12,9),(162,1,49,17,'_nrs2',0,13,9),(163,1,50,17,'_nrs2',0,14,9),(164,1,51,17,'_nrs2',0,17,9),(165,1,52,17,'_nrs2',0,0,9),(166,1,53,17,'_nrs2',0,18,9),(167,1,54,17,'_nrs2',0,6,9),(168,1,55,17,'_nrs2',0,10,9),(169,1,56,17,'_nrs2',0,7,9),(170,1,57,17,'_nrs2',0,8,9),(171,1,58,17,'_nrs2',0,9,9),(172,1,59,17,'_nrs2',0,11,9),(173,1,42,18,'_nut',0,15,10),(174,1,43,18,'_nut',0,2,10),(175,1,44,18,'_nut',0,3,10),(176,1,45,18,'_nut',0,4,10),(177,1,46,18,'_nut',0,5,10),(178,1,47,18,'_nut',0,1,10),(179,1,48,18,'_nut',0,12,10),(180,1,49,18,'_nut',0,13,10),(181,1,50,18,'_nut',0,14,10),(182,1,51,18,'_nut',0,17,10),(183,1,52,18,'_nut',0,0,10),(184,1,53,18,'_nut',0,18,10),(185,1,54,18,'_nut',0,6,10),(186,1,55,18,'_nut',0,10,10),(187,1,56,18,'_nut',0,7,10),(188,1,57,18,'_nut',0,8,10),(189,1,58,18,'_nut',0,9,10),(190,1,59,18,'_nut',0,11,10),(191,1,42,19,'_nut2',0,15,11),(192,1,43,19,'_nut2',0,2,11),(193,1,44,19,'_nut2',0,3,11),(194,1,45,19,'_nut2',0,4,11),(195,1,46,19,'_nut2',0,5,11),(196,1,47,19,'_nut2',0,1,11),(197,1,48,19,'_nut2',0,12,11),(198,1,49,19,'_nut2',0,13,11),(199,1,50,19,'_nut2',0,14,11),(200,1,51,19,'_nut2',0,17,11),(201,1,52,19,'_nut2',0,0,11),(202,1,53,19,'_nut2',0,18,11),(203,1,54,19,'_nut2',0,6,11),(204,1,55,19,'_nut2',0,10,11),(205,1,56,19,'_nut2',0,7,11),(206,1,57,19,'_nut2',0,8,11),(207,1,58,19,'_nut2',0,9,11),(208,1,59,19,'_nut2',0,11,11),(209,1,71,20,'_f1',0,1,12),(210,1,72,20,'_f1',0,10,12),(211,1,73,20,'_f1',0,4,12),(212,1,74,20,'_f1',0,2,12),(213,1,75,20,'_f1',0,8,12),(214,1,76,20,'_f1',0,9,12),(215,1,77,20,'_f1',0,5,12),(216,1,79,20,'_f1',0,6,12),(217,1,80,20,'_f1',0,7,12),(218,1,81,20,'_f1',0,3,12),(219,1,71,21,'_f2',0,1,13),(220,1,72,21,'_f2',0,10,13),(221,1,73,21,'_f2',0,4,13),(222,1,74,21,'_f2',0,2,13),(223,1,75,21,'_f2',0,8,13),(224,1,76,21,'_f2',0,9,13),(225,1,77,21,'_f2',0,5,13),(226,1,79,21,'_f2',0,6,13),(227,1,80,21,'_f2',0,7,13),(228,1,81,21,'_f2',0,3,13),(229,1,71,22,'_f3',0,1,14),(230,1,72,22,'_f3',0,10,14),(231,1,73,22,'_f3',0,4,14),(232,1,74,22,'_f3',0,2,14),(233,1,75,22,'_f3',0,8,14),(234,1,76,22,'_f3',0,9,14),(235,1,77,22,'_f3',0,5,14),(236,1,79,22,'_f3',0,6,14),(237,1,80,22,'_f3',0,7,14),(238,1,81,22,'_f3',0,3,14),(239,1,71,23,'_f4',0,1,15),(240,1,72,23,'_f4',0,10,15),(241,1,73,23,'_f4',0,4,15),(242,1,74,23,'_f4',0,2,15),(243,1,75,23,'_f4',0,8,15),(244,1,76,23,'_f4',0,9,15),(245,1,77,23,'_f4',0,5,15),(246,1,79,23,'_f4',0,6,15),(247,1,80,23,'_f4',0,7,15),(248,1,81,23,'_f4',0,3,15),(249,1,91,4,'',0,1,16),(250,1,92,4,'',0,3,16),(251,1,93,4,'',0,4,16),(252,1,94,4,'',0,19,16),(253,1,95,4,'',0,5,16),(254,1,96,4,'',0,18,16),(255,1,97,4,'',0,7,16),(256,1,98,4,'',0,6,16),(257,1,99,4,'',0,2,16),(258,1,100,4,'',1,12,16),(259,1,101,4,'',1,13,16),(260,1,102,4,'',1,14,16),(261,1,103,4,'',1,15,16),(262,1,104,4,'',0,10,16),(263,1,105,4,'',0,9,16),(264,1,106,4,'',0,8,16),(265,1,108,4,'',1,16,16),(266,1,109,4,'',1,17,16),(267,1,121,5,'',0,1,17),(268,1,122,5,'',1,7,17),(269,1,123,5,'',1,14,17),(270,1,124,5,'',1,15,17),(271,1,125,5,'',0,22,17),(272,1,126,5,'',1,2,17),(273,1,127,5,'',0,20,17),(274,1,128,5,'',1,12,17),(275,1,129,5,'',1,11,17),(276,1,130,5,'',0,3,17),(277,1,131,5,'',0,10,17),(278,1,132,5,'',0,9,17),(279,1,133,5,'',0,19,17),(280,1,134,5,'',1,4,17),(281,1,135,5,'',1,5,17),(282,1,136,5,'',0,8,17),(283,1,142,5,'',0,21,17),(284,1,143,5,'',0,13,17),(285,1,144,5,'',0,16,17),(286,1,145,5,'',1,17,17),(287,1,147,5,'',0,18,17),(289,1,163,6,'',0,6,18),(290,1,164,6,'',0,2,18),(291,1,165,6,'',0,3,18),(292,1,166,6,'',1,4,18),(293,1,167,6,'',0,5,18),(294,1,168,6,'',1,7,18),(295,1,169,6,'',0,9,18),(296,1,170,6,'',0,12,18),(297,1,171,6,'',0,13,18),(298,1,172,6,'',0,14,18),(299,1,173,6,'',0,15,18),(300,1,174,6,'',0,16,18),(301,1,175,6,'',0,17,18),(302,1,176,6,'',0,18,18),(303,1,177,6,'',0,19,18),(304,1,178,6,'',0,10,18),(305,1,179,6,'',0,8,18),(306,1,180,6,'',0,11,18),(307,1,181,6,'',0,21,18),(308,1,182,6,'',0,20,18),(309,1,192,7,'',0,1,19),(310,1,193,7,'',1,2,19),(311,1,194,7,'',1,3,19),(312,1,195,7,'',1,7,19),(313,1,196,7,'',1,6,19),(314,1,197,7,'',1,4,19),(315,1,198,7,'',1,5,19),(316,1,199,7,'',0,9,19),(317,1,200,7,'',0,8,19),(318,1,211,8,'',0,1,20),(319,1,212,8,'_br',0,2,20),(320,1,213,8,'',0,3,20),(321,1,214,8,'',0,4,20),(322,1,221,9,'',0,1,21),(323,1,222,9,'',0,4,21),(324,1,223,9,'',1,3,21),(325,1,224,9,'_vt',1,2,21),(326,2,1,1,'',0,1,1),(327,2,15,1,'',0,2,1),(328,2,14,1,'',1,3,1),(329,2,28,1,'',0,4,1),(330,2,21,1,'',0,5,1),(331,2,8,1,'',0,6,1),(332,2,7,1,'',0,7,1),(333,2,29,1,'',0,8,1),(334,2,30,1,'',0,9,1),(335,2,11,1,'',0,10,1),(336,2,13,1,'',0,11,1),(337,2,12,1,'',0,12,1),(338,2,2,1,'',0,13,1),(339,2,3,1,'',0,14,1),(340,2,4,1,'',0,15,1),(341,2,5,1,'',0,16,1),(342,2,6,1,'',0,17,1),(343,2,9,1,'',0,18,1),(344,2,26,1,'',0,19,1),(345,2,10,1,'',0,20,1),(346,2,27,1,'',0,21,1),(347,2,16,1,'',0,22,1),(348,2,17,1,'',0,23,1),(349,2,18,1,'',0,24,1),(350,2,19,1,'',0,25,1),(351,2,20,1,'',0,26,1),(352,2,22,1,'',0,27,1),(353,2,23,1,'',0,28,1),(354,2,24,1,'',0,29,1),(355,2,25,1,'',0,30,1),(356,2,47,10,'_inv',0,1,2),(357,2,43,10,'_inv',1,2,2),(358,2,44,10,'_inv',0,3,2),(359,2,45,10,'_inv',1,4,2),(360,2,46,10,'_inv',0,5,2),(361,2,54,10,'_inv',0,6,2),(362,2,56,10,'_inv',0,7,2),(363,2,57,10,'_inv',0,8,2),(364,2,58,10,'_inv',0,9,2),(365,2,55,10,'_inv',0,10,2),(366,2,59,10,'_inv',0,11,2),(367,2,48,10,'_inv',0,12,2),(368,2,49,10,'_inv',0,13,2),(369,2,50,10,'_inv',0,14,2),(370,2,42,10,'_inv',0,15,2),(371,2,51,10,'_inv',0,16,2),(372,2,53,10,'_inv',0,17,2),(373,2,47,11,'_doc',0,1,3),(374,2,43,11,'_doc',0,2,3),(375,2,44,11,'_doc',0,3,3),(376,2,45,11,'_doc',0,4,3),(377,2,46,11,'_doc',0,5,3),(378,2,54,11,'_doc',0,6,3),(379,2,56,11,'_doc',0,7,3),(380,2,57,11,'_doc',0,8,3),(381,2,58,11,'_doc',0,9,3),(382,2,55,11,'_doc',0,10,3),(383,2,59,11,'_doc',0,11,3),(384,2,48,11,'_doc',0,12,3),(385,2,49,11,'_doc',0,13,3),(386,2,50,11,'_doc',0,14,3),(387,2,42,11,'_doc',0,15,3),(388,2,51,11,'_doc',0,16,3),(389,2,53,11,'_doc',0,17,3),(390,2,47,12,'_doc2',0,1,4),(391,2,43,12,'_doc2',0,2,4),(392,2,44,12,'_doc2',0,3,4),(393,2,45,12,'_doc2',0,4,4),(394,2,46,12,'_doc2',0,5,4),(395,2,54,12,'_doc2',0,6,4),(396,2,56,12,'_doc2',0,7,4),(397,2,57,12,'_doc2',0,8,4),(398,2,58,12,'_doc2',0,9,4),(399,2,55,12,'_doc2',0,10,4),(400,2,59,12,'_doc2',0,11,4),(401,2,48,12,'_doc2',0,12,4),(402,2,49,12,'_doc2',0,13,4),(403,2,50,12,'_doc2',0,14,4),(404,2,42,12,'_doc2',0,15,4),(405,2,51,12,'_doc2',0,16,4),(406,2,53,12,'_doc2',0,17,4),(407,2,47,13,'_sch',0,1,5),(408,2,43,13,'_sch',0,2,5),(409,2,44,13,'_sch',0,3,5),(410,2,45,13,'_sch',0,4,5),(411,2,46,13,'_sch',0,5,5),(412,2,54,13,'_sch',0,6,5),(413,2,56,13,'_sch',0,7,5),(414,2,57,13,'_sch',0,8,5),(415,2,58,13,'_sch',0,9,5),(416,2,55,13,'_sch',0,10,5),(417,2,59,13,'_sch',0,11,5),(418,2,48,13,'_sch',0,12,5),(419,2,49,13,'_sch',0,13,5),(420,2,50,13,'_sch',0,14,5),(421,2,42,13,'_sch',0,15,5),(422,2,51,13,'_sch',0,16,5),(423,2,53,13,'_sch',0,17,5),(424,2,47,14,'_sch2',0,1,6),(425,2,43,14,'_sch2',0,2,6),(426,2,44,14,'_sch2',0,3,6),(427,2,45,14,'_sch2',0,4,6),(428,2,46,14,'_sch2',0,5,6),(429,2,54,14,'_sch2',0,6,6),(430,2,56,14,'_sch2',0,7,6),(431,2,57,14,'_sch2',0,8,6),(432,2,58,14,'_sch2',0,9,6),(433,2,55,14,'_sch2',0,10,6),(434,2,59,14,'_sch2',0,11,6),(435,2,48,14,'_sch2',0,12,6),(436,2,49,14,'_sch2',0,13,6),(437,2,50,14,'_sch2',0,14,6),(438,2,42,14,'_sch2',0,15,6),(439,2,51,14,'_sch2',0,16,6),(440,2,53,14,'_sch2',0,17,6),(441,2,47,15,'_sch3',0,1,7),(442,2,43,15,'_sch3',0,2,7),(443,2,44,15,'_sch3',0,3,7),(444,2,45,15,'_sch3',0,4,7),(445,2,46,15,'_sch3',0,5,7),(446,2,54,15,'_sch3',0,6,7),(447,2,56,15,'_sch3',0,7,7),(448,2,57,15,'_sch3',0,8,7),(449,2,58,15,'_sch3',0,9,7),(450,2,55,15,'_sch3',0,10,7),(451,2,59,15,'_sch3',0,11,7),(452,2,48,15,'_sch3',0,12,7),(453,2,49,15,'_sch3',0,13,7),(454,2,50,15,'_sch3',0,14,7),(455,2,42,15,'_sch3',0,15,7),(456,2,51,15,'_sch3',0,16,7),(457,2,53,15,'_sch3',0,17,7),(458,2,47,16,'_nrs',0,1,8),(459,2,43,16,'_nrs',0,2,8),(460,2,44,16,'_nrs',0,3,8),(461,2,45,16,'_nrs',0,4,8),(462,2,46,16,'_nrs',0,5,8),(463,2,54,16,'_nrs',0,6,8),(464,2,56,16,'_nrs',0,7,8),(465,2,57,16,'_nrs',0,8,8),(466,2,58,16,'_nrs',0,9,8),(467,2,55,16,'_nrs',0,10,8),(468,2,59,16,'_nrs',0,11,8),(469,2,48,16,'_nrs',0,12,8),(470,2,49,16,'_nrs',0,13,8),(471,2,50,16,'_nrs',0,14,8),(472,2,42,16,'_nrs',0,15,8),(473,2,51,16,'_nrs',0,16,8),(474,2,53,16,'_nrs',0,17,8),(475,2,47,17,'_nrs2',0,1,9),(476,2,43,17,'_nrs2',0,2,9),(477,2,44,17,'_nrs2',0,3,9),(478,2,45,17,'_nrs2',0,4,9),(479,2,46,17,'_nrs2',0,5,9),(480,2,54,17,'_nrs2',0,6,9),(481,2,56,17,'_nrs2',0,7,9),(482,2,57,17,'_nrs2',0,8,9),(483,2,58,17,'_nrs2',0,9,9),(484,2,55,17,'_nrs2',0,10,9),(485,2,59,17,'_nrs2',0,11,9),(486,2,48,17,'_nrs2',0,12,9),(487,2,49,17,'_nrs2',0,13,9),(488,2,50,17,'_nrs2',0,14,9),(489,2,42,17,'_nrs2',0,15,9),(490,2,51,17,'_nrs2',0,16,9),(491,2,53,17,'_nrs2',0,17,9),(492,2,47,18,'_nut',0,1,10),(493,2,43,18,'_nut',0,2,10),(494,2,44,18,'_nut',0,3,10),(495,2,45,18,'_nut',0,4,10),(496,2,46,18,'_nut',0,5,10),(497,2,54,18,'_nut',0,6,10),(498,2,56,18,'_nut',0,7,10),(499,2,57,18,'_nut',0,8,10),(500,2,58,18,'_nut',0,9,10),(501,2,55,18,'_nut',0,10,10),(502,2,59,18,'_nut',0,11,10),(503,2,48,18,'_nut',0,12,10),(504,2,49,18,'_nut',0,13,10),(505,2,50,18,'_nut',0,14,10),(506,2,42,18,'_nut',0,15,10),(507,2,51,18,'_nut',0,16,10),(508,2,53,18,'_nut',0,17,10),(509,2,47,19,'_nut2',0,1,11),(510,2,43,19,'_nut2',0,2,11),(511,2,44,19,'_nut2',0,3,11),(512,2,45,19,'_nut2',0,4,11),(513,2,46,19,'_nut2',0,5,11),(514,2,54,19,'_nut2',0,6,11),(515,2,56,19,'_nut2',0,7,11),(516,2,57,19,'_nut2',0,8,11),(517,2,58,19,'_nut2',0,9,11),(518,2,55,19,'_nut2',0,10,11),(519,2,59,19,'_nut2',0,11,11),(520,2,48,19,'_nut2',0,12,11),(521,2,49,19,'_nut2',0,13,11),(522,2,50,19,'_nut2',0,14,11),(523,2,42,19,'_nut2',0,15,11),(524,2,51,19,'_nut2',0,16,11),(525,2,53,19,'_nut2',0,17,11),(526,2,71,20,'_f1',0,1,12),(527,2,74,20,'_f1',0,2,12),(528,2,81,20,'_f1',0,3,12),(529,2,73,20,'_f1',0,4,12),(530,2,77,20,'_f1',0,5,12),(531,2,79,20,'_f1',0,6,12),(532,2,80,20,'_f1',0,7,12),(533,2,75,20,'_f1',0,8,12),(534,2,76,20,'_f1',0,9,12),(535,2,72,20,'_f1',0,10,12),(536,2,71,21,'_f2',0,1,13),(537,2,74,21,'_f2',0,2,13),(538,2,81,21,'_f2',0,3,13),(539,2,73,21,'_f2',0,4,13),(540,2,77,21,'_f2',0,5,13),(541,2,79,21,'_f2',0,6,13),(542,2,80,21,'_f2',0,7,13),(543,2,75,21,'_f2',0,8,13),(544,2,76,21,'_f2',0,9,13),(545,2,72,21,'_f2',0,10,13),(546,2,71,22,'_f3',0,1,14),(547,2,74,22,'_f3',0,2,14),(548,2,81,22,'_f3',0,3,14),(549,2,73,22,'_f3',0,4,14),(550,2,77,22,'_f3',0,5,14),(551,2,79,22,'_f3',0,6,14),(552,2,80,22,'_f3',0,7,14),(553,2,75,22,'_f3',0,8,14),(554,2,76,22,'_f3',0,9,14),(555,2,72,22,'_f3',0,10,14),(556,2,71,23,'_f4',0,1,15),(557,2,74,23,'_f4',0,2,15),(558,2,81,23,'_f4',0,3,15),(559,2,73,23,'_f4',0,4,15),(560,2,77,23,'_f4',0,5,15),(561,2,79,23,'_f4',0,6,15),(562,2,80,23,'_f4',0,7,15),(563,2,75,23,'_f4',0,8,15),(564,2,76,23,'_f4',0,9,15),(565,2,72,23,'_f4',0,10,15),(566,2,91,4,'',1,1,16),(567,2,99,4,'',1,2,16),(568,2,92,4,'',1,3,16),(569,2,93,4,'',1,4,16),(570,2,95,4,'',0,5,16),(571,2,98,4,'',0,6,16),(572,2,97,4,'',0,7,16),(573,2,106,4,'',0,8,16),(574,2,105,4,'',0,9,16),(575,2,104,4,'',0,10,16),(576,2,100,4,'',1,11,16),(577,2,101,4,'',1,12,16),(578,2,102,4,'',1,13,16),(579,2,103,4,'',1,14,16),(580,2,108,4,'',1,15,16),(581,2,109,4,'',1,16,16),(582,2,96,4,'',1,17,16),(583,2,94,4,'',0,18,16),(584,2,199,24,'',0,1,17),(585,2,244,24,'',1,2,17),(586,2,245,24,'',1,3,17),(587,2,250,24,'',0,4,17),(588,2,252,24,'',0,5,17),(589,2,253,24,'',0,6,17),(590,2,251,24,'',0,7,17),(591,2,254,24,'',0,8,17),(592,2,247,24,'',1,9,17),(593,2,259,24,'',1,10,17),(594,2,260,24,'',1,11,17),(595,2,243,24,'',0,12,17),(596,2,246,24,'',0,13,17),(597,2,242,24,'',0,14,17),(598,2,221,9,'',1,1,18),(599,2,224,9,'_vt',1,2,18),(600,2,223,9,'',1,3,18),(601,2,222,9,'',1,4,18);
/*!40000 ALTER TABLE `template_category_field` ENABLE KEYS */;
UNLOCK TABLES;
commit;

--
-- Dumping data for table `user`
--

LOCK TABLES `user` WRITE;
/*!40000 ALTER TABLE `user` DISABLE KEYS */;
set autocommit=0;
INSERT INTO `user` (`id`, `active`, `credential`, `department`, `division`, `ecommons_id`, `email`, `faculty_rank`, `fax`, `first_name`, `institution`, `irb_institution`, `last_name`, `middle_name`, `pager`, `password`, `primary_phone`, `salt`, `secondary_phone`, `institution_role`, `role`, `last_login_time`, `notification_email`) VALUES (409,1,1,1,1,'fa123','fa123@e.edu',1,'','Alma',3,NULL,'Apple','','','/VUz7S3kMqULgfqCp/OnFgIMiBqsSqBXsLdPZDrCkig=','(555) 555-5555','974f23cc/54ad/4216/856d/d552a4f0905d','',1,12,'2019-12-16 09:53:01',NULL),(410,1,NULL,NULL,NULL,'SA123','sa123@fakek.edu',NULL,'(123) 456-7890','Bobby',5,NULL,'Boyle','','(123) 456-7890','/VUz7S3kMqULgfqCp/OnFgIMiBqsSqBXsLdPZDrCkig=','(123) 456-7890','974f23cc/54ad/4216/856d/d552a4f0905d','(123) 456-7890',1,12,'2019-12-16 09:57:52',NULL),(411,1,NULL,NULL,NULL,'NR123','nr123@fakek.edu',NULL,'(123) 456-7890','Carly',5,NULL,'Cart','','(123) 456-7890','/VUz7S3kMqULgfqCp/OnFgIMiBqsSqBXsLdPZDrCkig=','(123) 456-7890','974f23cc/54ad/4216/856d/d552a4f0905d','(123) 456-7890',2,10,'2019-12-16 09:49:17',NULL),(412,1,NULL,NULL,NULL,'NT123','nt123@fakek.edu',NULL,'(123) 456-7890','Donny',5,NULL,'Dove','','(123) 456-7890','/VUz7S3kMqULgfqCp/OnFgIMiBqsSqBXsLdPZDrCkig=','(123) 456-7890','974f23cc/54ad/4216/856d/d552a4f0905d','(123) 456-7890',2,11,'2019-12-16 09:49:23',NULL),(413,1,NULL,NULL,NULL,'BS123','bs123@fakek.edu',NULL,'(123) 456-7890','Edna',5,NULL,'Everage','','(123) 456-7890','/VUz7S3kMqULgfqCp/OnFgIMiBqsSqBXsLdPZDrCkig=','(123) 456-7890','974f23cc/54ad/4216/856d/d552a4f0905d','(123) 456-7890',3,15,'2019-12-16 09:49:51',NULL),(414,1,NULL,NULL,NULL,'FD123','fd123@fakek.edu',NULL,'(123) 456-7890','Fanny',5,NULL,'Farber','','(123) 456-7890','/VUz7S3kMqULgfqCp/OnFgIMiBqsSqBXsLdPZDrCkig=','(123) 456-7890','974f23cc/54ad/4216/856d/d552a4f0905d','(123) 456-7890',5,4,'2019-12-16 09:49:09',NULL),(415,1,NULL,NULL,NULL,'GV123','gv123@fakek.edu',NULL,'(123) 456-7890','Greg',5,NULL,'Gumble','','(123) 456-7890','/VUz7S3kMqULgfqCp/OnFgIMiBqsSqBXsLdPZDrCkig=','(123) 456-7890','974f23cc/54ad/4216/856d/d552a4f0905d','(123) 456-7890',6,5,'2019-12-16 09:52:18',NULL),(416,1,NULL,NULL,NULL,'PS123','ps123@fakek.edu',NULL,'(123) 456-7890','Helen',5,NULL,'Hayes','','(123) 456-7890','/VUz7S3kMqULgfqCp/OnFgIMiBqsSqBXsLdPZDrCkig=','(123) 456-7890','974f23cc/54ad/4216/856d/d552a4f0905d','(123) 456-7890',4,1,'2019-12-16 09:50:04',NULL),(417,1,NULL,NULL,NULL,'PS456','ps456@fakek.edu',NULL,'(123) 456-7890','Ingrid',5,NULL,'India','','(123) 456-7890','/VUz7S3kMqULgfqCp/OnFgIMiBqsSqBXsLdPZDrCkig=','(123) 456-7890','974f23cc/54ad/4216/856d/d552a4f0905d','(123) 456-7890',4,1,'2019-12-16 09:50:15',NULL),(418,1,NULL,NULL,NULL,'PS890','ps890@fakek.edu',NULL,'(123) 456-7890','Jacob',5,NULL,'Jones','','(123) 456-7890','/VUz7S3kMqULgfqCp/OnFgIMiBqsSqBXsLdPZDrCkig=','(123) 456-7890','974f23cc/54ad/4216/856d/d552a4f0905d','(123) 456-7890',4,1,'2019-12-16 09:50:29',NULL),(419,1,NULL,NULL,NULL,'APS123','aps123@fakek.edu',NULL,'(123) 456-7890','Loyd',5,NULL,'Lamp','','(123) 456-7890','/VUz7S3kMqULgfqCp/OnFgIMiBqsSqBXsLdPZDrCkig=','(123) 456-7890','974f23cc/54ad/4216/856d/d552a4f0905d','(123) 456-7890',4,1,'2019-12-16 09:50:45',NULL),(420,1,NULL,NULL,NULL,'APS456','aps456@fakek.edu',NULL,'(123) 456-7890','Melnea',5,NULL,'Moses','','(123) 456-7890','/VUz7S3kMqULgfqCp/OnFgIMiBqsSqBXsLdPZDrCkig=','(123) 456-7890','974f23cc/54ad/4216/856d/d552a4f0905d','(123) 456-7890',4,1,'2019-12-16 09:51:48',NULL),(421,1,NULL,NULL,NULL,'APS789','aps789@fakek.edu',NULL,'(123) 456-7890','Ned',5,NULL,'Needham','','(123) 456-7890','/VUz7S3kMqULgfqCp/OnFgIMiBqsSqBXsLdPZDrCkig=','(123) 456-7890','974f23cc/54ad/4216/856d/d552a4f0905d','(123) 456-7890',4,1,'2019-12-16 09:51:58',NULL);
/*!40000 ALTER TABLE `user` ENABLE KEYS */;
UNLOCK TABLES;
commit;

--
-- Dumping data for table `visit_type`
--

LOCK TABLES `visit_type` WRITE;
/*!40000 ALTER TABLE `visit_type` DISABLE KEYS */;
set autocommit=0;
INSERT INTO `visit_type` (`id`, `name`, `inpatient`) VALUES (1,'Outpatient CRC',0),(2,'Outpatient Non CRC',0),(3,'Outpatient Off Institution',0),(4,'Inpatient CRC',1),(5,'Inpatient Non CRC',1),(6,'Inpatient Off Institution',1);
/*!40000 ALTER TABLE `visit_type` ENABLE KEYS */;
UNLOCK TABLES;
commit;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2019-12-20 13:44:49
