#!/bin/bash
#
# Copyright (c) 2015-2016, President and Fellows of Harvard College
#
# Redistribution and use in source and binary forms, with or without
# modification, are permitted provided that the following conditions are met:
#
# 1. Redistributions of source code must retain the above copyright
# notice, this list of conditions and the following disclaimer.
#
# 2. Redistributions in binary form must reproduce the above copyright
# notice, this list of conditions and the following disclaimer in the
# documentation and/or other materials provided with the distribution.
#
# 3. The name of the author may not be used to endorse or promote products
# derived from this software without specific prior written permission.
#
# THIS SOFTWARE IS PROVIDED BY THE AUTHOR "AS IS" AND ANY EXPRESS OR IMPLIED
# WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
# MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO
# EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
# SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED
# TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
# PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
# LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
# NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
# SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
#


CONFIG_HOME=`dirname $0`

CONFIG_FILE_DIR=${CONFIG_HOME}/conf

mkdir -p ${CONFIG_FILE_DIR}

CLASSPATH=.:${CONFIG_FILE_DIR}

LIB_DIR=${CONFIG_HOME}/lib

#
#Set up our classpath with our library dependencies...
for jar in `find ${LIB_DIR}/*.jar`;
do
    CLASSPATH=${jar}:${CLASSPATH}
done


#
#Guess JAVA_HOME if it's not set
if [ ${#JAVA_HOME} == 0 ]; then
    JAVA_HOME=/usr/local/java
fi

JVM_ARGS=""

# Program arguments:
#
# For a complete list of arguments run with the -help switch:
#
#PROGRAM_ARGS="-help"
#
# To set the output file names:
#
#PROGRAM_ARGS="-dupesFilename = myDupesFilename.csv -noDupesFilename = myNoDupesFilename.csv"
#
# By default we provide no program arguments and rely on the defaults.
# NOTE: we can set program arguments on the command line when running this script
#
PROGRAM_ARGS=""

JAVA_COMMAND="java ${JVM_ARGS} -cp ${CLASSPATH} edu.harvard.catalyst.scheduler.subjectDataCleaner.SubjectDataDeleter ${PROGRAM_ARGS} $@"

${JAVA_COMMAND}

echo "Subject Data Deleter ran."
