call mvn -f ./plugins/pom.xml install
call mvn org.apache.maven.dotnet.plugins:maven-install-plugin:install-file -Dfile=./thirdparty/NUnit/NUnit.Framework.dll -DgroupId=NUnit -DartifactId=NUnit.Framework -Dpackaging=dll -Dversion=2.2.8.0
call mvn -f ./assemblies/pom.xml -Dmaven.test.skip=true -Dbootstrap install %*
rem call mvn org.apache.maven.dotnet.plugins:maven-solution-plugin:solution 