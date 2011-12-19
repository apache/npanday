REM Start of LICENSE
GOTO LicenseComment
Licensed to the Apache Software Foundation (ASF) under one
or more contributor license agreements.  See the NOTICE file
distributed with this work for additional information
regarding copyright ownership.  The ASF licenses this file
to you under the Apache License, Version 2.0 (the
"License"); you may not use this file except in compliance
with the License.  You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing,
software distributed under the License is distributed on an
"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
KIND, either express or implied.  See the License for the
specific language governing permissions and limitations
under the License.
:LicenseComment
REM Workaround for http://jira.codehaus.org/browse/MNG-1911
REM The first time you build a new NPanday version, the compile-plugin has
REM to be installed into the repository already.

@echo off
echo ###################################################################
echo  Bootstrapping the NPanday Compile Plugin
echo ###################################################################
@echo on

call mvn clean install -Dbootstrap --projects org.apache.npanday.plugins:maven-compile-plugin --also-make
IF %ERRORLEVEL% NEQ 0 GOTO Error

@echo off
echo ###################################################################
echo  SUCCESS! Now you should be able to build using 'mvn install'
echo ###################################################################
@echo on

GOTO End

:Error

@echo off
echo ###################################################################
echo  FAILED. Something went wrong. Consult the mailing list...
echo ###################################################################
pause
@echo on

:End
