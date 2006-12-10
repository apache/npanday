mvn -f ../pom-java-bootstrap.xml install
mvn org.apache.maven.dotnet.plugins:maven-install-plugin:install-file -Dfile=../imports/nunit-2.0/nunit.framework.dll -DgroupId=org.nunit -DartifactId=nunit.framework -Dpackaging=dll -Dversion=2.2.8.0
mvn -f ../pom-net-bootstrap.xml -Dmaven.test.skip=true -Dbootstrap install
