mvn -f ./components/pom.xml install
mvn -f ./plugins/pom.xml install
mvn org.apache.maven.dotnet.plugins:maven-install-plugin:install-file -Dfile=thirdparty/NUnit/NUnit.Framework.dll -DgroupId=NUnit -DartifactId=NUnit.Framework -Dpackaging=dll -Dversion=2.2.8.0
mvn -f ./assemblies/pom.xml -Dmaven.test.skip=true -Dbootstrap install $*
