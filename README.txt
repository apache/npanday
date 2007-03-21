Prerequisites
* csc.exe must be available on your PATH.  Usually it can be found in C:\WINDOWS\Microsoft.NET\Framework\[version]
* NUnit must be installed or otherwise configured, see http://incubator.apache.org/nmaven/getting-started.html

Initial Build
* On the first build, for Windows execute
    bootstrap-build.bat or
    bootstrap-build.bat -DwithIDE
 Or on *nix,
    bootstrap-build.sh

Note that the -DwithIDE option requires Microsoft 2.0 or higher. If this is a clean build (meaning that you do not have a ~./m2/nmaven-settings.xml file)
then you will also need to make sure that you have csc within your path. On subsequent builds, you can just type mvn install from
the command prompt.

IntelliJ IDEA Setup
* Do the initial build. This will create the dotnet modello model source code and will download all of the maven
binary dependencies. Next, click the maven-dotnet.ipr file. Go to settings/Path variables and set localRepository
 to your ~/.m2/repository directory (specify the absolute path). Exit IntelliJ and click the maven-dotnet.ipr file.
 Now all of the binary dependencies will be mapped to the local maven repo.