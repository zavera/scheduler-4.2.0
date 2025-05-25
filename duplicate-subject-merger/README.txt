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

INSTRUCTIONS:

1. put scheduler-duplicate-subject-merger-4.2.0-SNAPSHOT-dist.zip file in a new folder


2. Run the following:

unzip scheduler-duplicate-subject-merger-4.2.0-SNAPSHOT-dist.zip
cd scheduler-duplicate-subject-merger-4.2.0-SNAPSHOT
cd conf


3. edit subject-data-merger.properties as needed:
    set db credentials
    set encryption key parameters
    set subjectMode=default


4. edit log4j.properties if desired (it is already set up to log information in detail)


5. go back to the run directory

cd ..


6. Run the program: The input is a list of comma separated pairs of subject ids with no space between the commas. The first subject id is the one to merge and the second id is the destination id.
(i.e. 234,120 -> would mark the subject with id 234 as merged)

./subjectDataMerger.sh [-removeDupStudySubjects] <input csv file>


7. By default the output files will be:

    subject-data-merger.log

8. The switch following switch will delete any duplicate study subjects that occurs when a subject is merged (this only applies in cases where a subject is in a study with two completely different MRNs i.e 1234 BWH and 5647 MGH):
	-removeDupStudySubjects

9. Note that the subject-data-merger.log does not get overwritten when the program runs; new log messages are concatenated to the end of the log file.
This is useful for debugging. Please archive the log file when it gets too large.



EOF
