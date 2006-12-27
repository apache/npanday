Initial Build
* On the first build, execute the bootstrap-build script. On subsequent builds, you can just type mvn install from
the command prompt.

IntelliJ IDEA Setup
* Do the initial build. This will create the dotnet modello model source code and will download all of the maven
binary dependencies. Next, click the maven-dotnet.ipr file. Go to settings/Path variables and set localRepository
 to your ~/.m2/repository directory (specify the absolute path). Exit IntelliJ and click the maven-dotnet.ipr file.
 Now all of the binary dependencies will be mapped to the local maven repo.