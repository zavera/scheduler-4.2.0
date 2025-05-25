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

1. Adjust conf/epic-import-subject-mrns.properties so that
    encryption properties are the same as those in the file, scheduler.xml,
    from the configuration folder of your Scheduler's tomcat

2. Then, to run the import-subject-mrns, you need a csv file with the data to
     process. Do not include a row with column headers, but just the data.
     Desired columns in this csv might be in any order, or intermixed
     with other columns, so you need to give zero-based offsets for the columns
     containing

        subject-id
        mrn
        site
        status
        puid

     A final parameter, either 'true' or 'false', indicates whether to override DB values
     of mrn-code with the value from the csv

     E.g., if the path to your csv is /tmp/myCsv.csv, and if the csv simply has the
     above colummns, in that order, and if you want to clobber DB values of MRNs with
     csv values, then you would invoke the program as below:

     <program> /tmp/myCsv.csv 0 1 2 3 4 true

     For each row, the program will look for subjects that match the ID and mrn.
     For those subjects, site, status and puid will be updated

   Choose either a or b:

     a. UNIX

     chmod +x importMrnUpdates.sh.sh
     ./importSubjectMrns.sh  <path-to-csv> <idIndex(zero-based)> <mrnIndex> <siteIndex> <statusIndex> <puidIndex>

     b. Windows

     .importSubjectMrns.cmd  <path-to-csv> <idIndex(zero-based)> <mrnIndex> <siteIndex> <statusIndex> <puidIndex>

