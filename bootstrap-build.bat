@echo off
IF "%phase%"=="" SET phase=install
ECHO Executing Phase: %phase%

call mvn %phase%
IF errorlevel 1 GOTO END

ECHO Installing 3rd Party Assemblies in the Local Repo
call mvn org.apache.maven.dotnet.plugins:maven-install-plugin:install-file -Dfile=./thirdparty/NUnit/NUnit.Framework.dll -DgroupId=NUnit -DartifactId=NUnit.Framework -Dpackaging=dll -Dversion=2.2.8.0
IF errorlevel 1 GOTO END

if "%1" == "-DMicrosoft" (
    ECHO Compiling Assemblies with Microsoft
    call mvn -f ./assemblies/pom.xml -Dmaven.test.skip=true -Dbootstrap -Dvendor=MICROSOFT %phase% %*
    IF errorlevel 1 GOTO END

    call mvn -f ./plugins/pom-netplugins.xml -Dmaven.test.skip=true -Dbootstrap -Dvendor=MICROSOFT %phase% %*
    IF errorlevel 1 GOTO END

 ) else  if "%1" == "-DMono" (
    ECHO Compiling Assemblies with Mono
    call mvn -f ./assemblies/pom.xml -Dmaven.test.skip=true -Dbootstrap -Dvendor=MONO %phase% %*
    IF errorlevel 1 GOTO END

    call mvn -f ./plugins/pom-netplugins.xml -Dmaven.test.skip=true -Dbootstrap -Dvendor=MONO %phase% %*
    IF errorlevel 1 GOTO END

 ) else (
    ECHO Compiling Assemblies with Unknown Vendor
    call mvn -f ./assemblies/pom.xml -Dmaven.test.skip=true -Dbootstrap %phase% %*
    IF errorlevel 1 GOTO END

    call mvn -f ./plugins/pom-netplugins.xml -Dmaven.test.skip=true -Dbootstrap %phase% %*
    IF errorlevel 1 GOTO END
 )

:END