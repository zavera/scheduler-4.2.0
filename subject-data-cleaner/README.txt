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

1. put scheduler-subject-data-cleaner-4.2.0-SNAPSHOT-dist.zip file in a new folder


2. Run the following:

unzip scheduler-subject-data-cleaner-4.2.0-SNAPSHOT-dist.zip
cd scheduler-subject-data-cleaner-4.2.0-SNAPSHOT
cd conf


3. edit subject-data-cleaner.properties as needed:
    set db credentials
    set encryption key parameters
    set subjectMode=default


4. edit log4j.properties if desired (it is already set up to log information in detail)


5. go back to the run directory

cd ..


6. display the program help screen

./subjectDataCleaner.sh -help


7. Run the program: the default program settings should work and provide output files in data-cleaner-output folder. i.e. just run:

./subjectDataCleaner.sh


8. By default the output files will be:

    data-cleaner-output/duplicateSubjectsToCurate.csv
    data-cleaner-output/nonDuplicateSubjectsToCurate.csv
    subject-data-cleaner.log


9. Note that the subject-data-cleaner.log does not get overwritten when the program runs; new log messages are concatenated to the end of the log file.
This is useful for debugging. Please archive the log file when it gets too large.


10. Sometimes with test data that does not mock real data well enough, the program may run very slowly,
or it may not produce many matches. There are two test-mode switches to help with that:

    -noMatchOnFullNamesOnly
    -matchOnLastnameAndBirthdate



EOF
