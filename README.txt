Prerequisites
* csc.exe must be available on your PATH.  Usually it can be found in
  C:\WINDOWS\Microsoft.NET\Framework\[version]
* NUnit must be installed or otherwise configured

* The latest version of NPanday requires .NET 2.0+ to build and run NPanday.
  This is due to needing the .NET 2.0 AppDomainManagers for executing Maven
  .NET plugins. You may still target your own projects with .NET 1.1 build.

Deploying NPanday Artifacts
* Due to a bug with not being able to use snapshots with an executables
  exe.config file, you will need to do this next step manually: Copy the
  NPanday.Plugin.Resx.exe.config file
  in NPanday\Plugins\NPanday.Plugin.Resx\1.1-SNAPSHOT from the local
  repository to the remote repository.

Generating CS Project Files and Solutions:
 * Go to the directory containing a pom file.
 * type: mvn npanday.plugin:maven-solution-plugin:solution

If the project was multi-module, the plugin will pick those up as well.

Setting up the Visual Studio 2005 Addin:
 After building the project:
 * To generate the Addin for VisualStudio type:
     mvn npanday.plugin:maven-vsinstaller-plugin:install
 * Start the IDE
 * Click on an NPanday project
 * Click on Tools/NPanday Addin

