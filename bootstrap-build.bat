@echo off
IF "%phase%"=="" SET phase=install
ECHO Executing Phase: %phase%

call mvn %phase% %*
IF errorlevel 1 GOTO END

ECHO Installing 3rd Party Assemblies in the Local Repo
rem call mvn install:install-file -Dfile=./thirdparty/org.apache.ws/XmlSchema-1.1.jar -DpomFile=./thirdparty/org.apache.ws/XmlSchema-1.1.pom -DgroupId=org.apache.ws.commons -DartifactId=XmlSchema -Dversion=1.1
call mvn org.apache.maven.dotnet.plugins:maven-install-plugin:install-file -Dfile=./thirdparty/NUnit/NUnit.Framework.dll -DgroupId=NUnit -DartifactId=NUnit.Framework -Dpackaging=library -DartifactVersion=2.2.8.0
call mvn org.apache.maven.dotnet.plugins:maven-install-plugin:install-file -Dfile=./thirdparty/Castle/Castle.Core.dll -DpomFile=./thirdparty/Castle/Castle.Core-2.0-rc2.pom -DgroupId=Castle -DartifactId=Castle.Core -Dpackaging=library -DartifactVersion=2.0-rc2
call mvn org.apache.maven.dotnet.plugins:maven-install-plugin:install-file -Dfile=./thirdparty/Castle/Castle.DynamicProxy.dll -DpomFile=./thirdparty/Castle/Castle.DynamicProxy-2.0-rc2.pom -DgroupId=Castle -DartifactId=Castle.DynamicProxy -Dpackaging=library -DartifactVersion=2.0-rc2
call mvn org.apache.maven.dotnet.plugins:maven-install-plugin:install-file -Dfile=./thirdparty/Castle/Castle.MicroKernel.dll -DpomFile=./thirdparty/Castle/Castle.MicroKernel-2.0-rc2.pom -DgroupId=Castle -DartifactId=Castle.MicroKernel -Dpackaging=library -DartifactVersion=2.0-rc2
call mvn org.apache.maven.dotnet.plugins:maven-install-plugin:install-file -Dfile=./thirdparty/Castle/Castle.Windsor.dll -DpomFile=./thirdparty/Castle/Castle.Windsor-2.0-rc2.pom -DgroupId=Castle -DartifactId=Castle.Windsor -Dpackaging=library -DartifactVersion=2.0-rc2
IF errorlevel 1 GOTO END

if "%1" == "-DMicrosoft" (
    ECHO Compiling Assemblies with Microsoft
    call mvn -f pom-dotnet.xml -Dmaven.test.skip=true -Dbootstrap -Dvendor=MICROSOFT %phase% %*
    IF errorlevel 1 GOTO END
 ) else  if "%1" == "-DMono" (
    ECHO Compiling Assemblies with Mono
    call mvn -f pom-dotnet.xml -Dmaven.test.skip=true -Dbootstrap -Dvendor=MONO %phase% %*
    IF errorlevel 1 GOTO END
 ) else (
    ECHO Compiling Assemblies with Unknown Vendor
    call mvn -f pom-dotnet.xml -Dmaven.test.skip=true -Dbootstrap %phase% %*
    IF errorlevel 1 GOTO END
 )

:END