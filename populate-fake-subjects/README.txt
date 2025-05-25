====
    Copyright (c) 2015-2016, President and Fellows of Harvard College

    Redistribution and use in source and binary forms, with or without
    modification, are permitted provided that the following conditions are met:

    1. Redistributions of source code must retain the above copyright
    notice, this list of conditions and the following disclaimer.

    2. Redistributions in binary form must reproduce the above copyright
    notice, this list of conditions and the following disclaimer in the
    documentation and/or other materials provided with the distribution.

    3. The name of the author may not be used to endorse or promote products
    derived from this software without specific prior written permission.

    THIS SOFTWARE IS PROVIDED BY THE AUTHOR "AS IS" AND ANY EXPRESS OR IMPLIED
    WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
    MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO
    EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
    SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED
    TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
    PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
    LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
    NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
    SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
====

0. Unzip scheduler-populate-fake-subjects<your-version>-SNAPSHOT-dist.zip. Then cd to the unzipped
   folder.

1. Adjust conf/populate-fake-subjects.properties so that SQL and encryption properties
   are the same as those in the files, scheduler.xml (or context.xml) and scheduler.properties,
   from the configuration folder of your Scheduler's tomcat. For the url, omit everything after
   Timezone=EST

   You can leave the properties file in conf, or copy to another external location. If you
   copy it to another directory, then in the scripts, fakeSubjects.sh, or setup-classpath.cmd,
   adjust PROPERTIES_DIR to refer to the directory where you put the properties file

2. If you are replacing old subjects with new fake ones, then before you delete the
   old ones, execute the following SQL in a mySql client, to determine your highest-numbered
   subject. Record the resulting number. (Note: for our 'demo data', the max-subject-id is 47)

   select max(id) from subject;

3. You can then delete the old subjects  by executing

     set foreign_key_checks = 0;
     truncate table subject;
     set foreign_key_checks = 1;

4. Then, to replace the deleted ones with properly encrypted new, fake subjects:
  a. UNIX
     chmod +x fakeSubjects.sh
     ./fakeSubjects.sh <max-subject-id>

  b. Windows
     fakeSubjects.cmd <max-subject-id>

5. Note, fakeSubjects will also replace the (encrypted) values of all subject_mrn objects.
   They will keep their foreign-keys (to the newly faked subjects), but will have fake
   MRN codes and sites.

