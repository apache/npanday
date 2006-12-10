mvn -f ../pom-java-bootstrap.xml install
mvn org.apache.maven.plugins:maven-install-plugin:install-file -Dfile=../imports/nunit-1.1/nunit.framework.dll -DgroupId=org.nunit -DartifactId=nunit.framework -Dversion=2.2.8.0 -Dpackaging=dll
mvn -f ../pom-net-bootstrap.xml -Dmaven.test.skip=true -Dbootstrap -Dversion=1.1 install
