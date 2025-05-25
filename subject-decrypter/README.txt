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

How to create a copy of the DB, decryptedDB, that has decrypted subjects in the table
'decrypted_subject':

1. Copy PROD to a decrypted DB, e.g. 'decryptedDB'. (Since subject data is sensitive, we
   recommend that this DB be on a separate server, on an encrypted drive.)

2. Run the provided SQL script, createDecryptedSubjectTable.sql on your decrypted DB.

3. Adjust conf/decrypter.properties as appropriate for your decryptedDB. Use
   properties similar to those on your tomcat, in lib/schedulers.properties and
   conf/context.xml or conf/Catalina/localhost/scheduler.xml. For the jdbcUrl, do not
   use any url parameters given on the tomcat *after* the 'serverTimezone=EST'.

4u. On Unix
     chmod +x decrypter.sh
     ./subject-decrypter.sh


4w. On Windows
     ./decrypter.cmd

5. Take a look, via an SQL client, at the 'decrypted_subject' table in the decrypted DB.
You should see decrypted data for the subjects

The decrypted_subject table is a memory-based table, so it is possible that your default
heap size will not be large enough. If you get an out-of-memory error when creating the
table, try making your memory larger, e.g., 160000000, via the mySql command

set @@max_heap_table_size=160000000;

