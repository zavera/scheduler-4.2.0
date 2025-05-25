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

/** integrity violation, no field#52, would cause server 500 **/
delete from template_category_field where field_id = 52;

/** end_date, start_date removed from template_resource around 07/2017 **/
delete from template_category_field where field_id in (select id from field where table_name = "template_resource" and name = "end_date");
delete from field where table_name = "template_resource" and name = "end_date";

ALTER TABLE visit_template ADD COLUMN billable TINYINT(1) NOT NULL DEFAULT '0';
INSERT INTO `field` (`id`, `table_name`, `name`, `display_name`, `processing`, `sortable`, `type`) VALUES (110, 'visit_template', 'billable', 'Visit Template Billable?', 'DEFAULT', 1, 'Boolean');
INSERT INTO `template_category_field` (`id`, `report_template_id`, `field_id`, `category_id`, `suffix`, `selected`, `csv_order`, `csv_order2`) VALUES (602, 2, 110, 4, '', 0, 18, 18);
