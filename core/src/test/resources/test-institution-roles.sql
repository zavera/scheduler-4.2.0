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

INSERT INTO institution_role VALUES (1,'Super Admin has access to all modules and functionality in the System','Super Admin','ROLE_SUPER_ADMIN');
INSERT INTO institution_role VALUES (2,'ND or RD Manager / AD has limited access to modules and functionality in the System','Resource Manager','ROLE_RESOURCE_MANAGER');
INSERT INTO institution_role VALUES (3,'Schedulers have access limited to modules and functionality in the System','Schedulers','ROLE_SCHEDULER');
INSERT INTO institution_role VALUES (4,'Study Staff has access to Resource and Appointments modules and functionality in the System','Study Staff','ROLE_STUDY_STAFF');
INSERT INTO institution_role VALUES (5,'Front Desk has very limited access to modules and functionality in the System','Front Desk','ROLE_FRONT_DESK');
INSERT INTO institution_role VALUES (6,'General View has very limited access to modules and functionality in the System','General View','ROLE_GENERAL_VIEW');