REM Workaround for http://jira.codehaus.org/browse/MNG-1911
REM The first time you build a new NPanday version, the compile-plugin has
REM to be installed into the repository already.

@echo off
echo ###################################################################
echo  Bootstrapping the NPanday Compile Plugin
echo ###################################################################
@echo on

call mvn install --projects npanday.plugin:maven-compile-plugin --also-make
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