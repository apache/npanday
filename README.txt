Prerequisites
* csc.exe must be available on your PATH.  Usually it can be found in C:\WINDOWS\Microsoft.NET\Framework\[version]
* NUnit must be installed or otherwise configured

Initial Build
* The latest version of NPanday requires .NET 2.0+ to build and run NPanday. This is due to needing the .NET 2.0 AppDomainManagers for
 executing Maven .NET plugins. You may still target your own projects with .NET 1.1 build.
* On the first build, for Windows execute
    bootstrap-build.bat [ -DMicrosoft | -DVisualStudio2005 | -DMono ]
 Or on *nix,
    bootstrap-build.sh

If this is a clean build (meaning that you do not have a ~./m2/npanday-settings.xml file)
then you will also need to make sure that you have csc within your path. On subsequent builds, you can just type mvn -f pom.xml install from
the command prompt.

IntelliJ IDEA Setup
* Do the initial build. This will create the dotnet modello model source code and will download all of the maven
binary dependencies. Next, click the maven-dotnet.ipr file. Go to settings/Path variables and set localRepository
 to your ~/.m2/repository directory (specify the absolute path). Exit IntelliJ and click the maven-dotnet.ipr file.
 Now all of the binary dependencies will be mapped to the local maven repo.

Deploying NPanday Artifacts (File System Only)
* On the command line
    set phase=deploy
    set deploy.directory=${remoteRepository}
    bootstrap-build.bat
* Due to a bug with not being able to use snapshots with an executables exe.config file, you will need to do this
  next step manually: Copy ${localRepository}\NPanday\Plugins\NPanday.Plugin.Resx\1.0.1\NPanday.Plugin.Resx.exe.config to
  ${remoteRepository}\NPanday\Plugins\NPanday.Plugin.Resx\1.0.1\NPanday.Plugin.Resx.exe.config

 To set up a remote repository accessible to others, you can transfer the contents of ${remoteRepository}
 from your file system to a web server.

Generating CS Project Files and Solutions:
 * Go to the directory containing a pom file.
 * type: mvn npanday.plugin:maven-solution-plugin:solution

 If the project was multi-module, the plugin will pick those up as well.

Setting up the Visual Studio 2005 Addin:
 After building the project with bootstrap-build -DVisualStudio2005
 * To generate the Addin for VisualStudio type: mvn npanday.plugin:maven-vsinstaller-plugin:install
 * Start the IDE
 * Click on an NPanday project
 * Click on Tools/NPanday Addin

Building Against NonPublic Repo (For release versions)
 Deploy openrdf artifacts to internal repo
 Deploy Castle artifacts to internal repo
 Type: bootstrap-build.bat -DrdfProfile.none -DVisualStudio2005

