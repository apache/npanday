mvn install $*
mvn org.apache.maven.dotnet.plugins:maven-install-plugin:install-file -Dfile=thirdparty/NUnit/NUnit.Framework.dll -DgroupId=NUnit -DartifactId=NUnit.Framework -Dpackaging=dll -DartifactVersion=2.2.8.0
mvn -f pom-dotnet.xml -Dmaven.test.skip=true -Dbootstrap install $*
