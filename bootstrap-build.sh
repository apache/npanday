mvn install $*
mvn -f ./plugins/NMaven.Plugin.Addin/pom-java.xml install $*
mvn -f ./plugins/NMaven.Plugin.Devenv/pom-java.xml install $*
mvn -f ./plugins/NMaven.Plugin.Settings/pom-java.xml install $*
mvn -f ./plugins/NMaven.Plugin.Solution/pom-java.xml install $*
mvn org.apache.maven.dotnet.plugins:maven-install-plugin:install-file -Dfile=thirdparty/NUnit/NUnit.Framework.dll -DgroupId=NUnit -DartifactId=NUnit.Framework -Dpackaging=dll -Dversion=2.2.8.0
mvn -f ./assemblies/pom.xml -Dmaven.test.skip=true -Dbootstrap install $*
mvn -f ./plugins/pom-netplugins.xml -Dmaven.test.skip=true -Dbootstrap install $*
