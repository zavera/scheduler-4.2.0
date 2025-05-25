@REM
@REM Copyright (c) 2015-2016, President and Fellows of Harvard College
@REM
@REM Redistribution and use in source and binary forms, with or without
@REM modification, are permitted provided that the following conditions are met:
@REM
@REM 1. Redistributions of source code must retain the above copyright
@REM notice, this list of conditions and the following disclaimer.
@REM
@REM 2. Redistributions in binary form must reproduce the above copyright
@REM notice, this list of conditions and the following disclaimer in the
@REM documentation and/or other materials provided with the distribution.
@REM
@REM 3. The name of the author may not be used to endorse or promote products
@REM derived from this software without specific prior written permission.
@REM
@REM THIS SOFTWARE IS PROVIDED BY THE AUTHOR "AS IS" AND ANY EXPRESS OR IMPLIED
@REM WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
@REM MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO
@REM EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
@REM SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED
@REM TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
@REM PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
@REM LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
@REM NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
@REM SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
@REM

REM
REM http://www.microsoft.com/resources/documentation/windows/xp/all/proddocs/en-us/batch.mspx?mfr=true
 
@echo off

@REM PROPERTIES_DIR="directory where your properties file lives"
@set PROPERTIES_DIR=.\conf

@set CONFIG_CP=.;%PROPERTIES_DIR%

@set LIB_DIR=%CONFIG_CP%\lib
 
for /F %%G in ('dir /b %CONFIG_CP%\lib') do (call :append %%G)
 
@set CONFIG_DIR=.\conf
 
@set CONFIG_CP=%CONFIG_CP%;%CONFIG_DIR%
 
REM @echo %CONFIG_CP%
 
GOTO :eof
 
@REM appending must be done in procedure call otherwise it won't append.  Windows=dumb
:append
            @set CONFIG_CP=%CONFIG_CP%;%LIB_DIR%\%1
            GOTO :eof