mvn install -DRdf $*
if [ $? -gt 0 ]
then
  exit 1
fi

mvn org.apache.maven.dotnet.plugins:maven-install-plugin:install-file -Dfile=thirdparty/NUnit/NUnit.Framework.dll -DgroupId=NUnit -DartifactId=NUnit.Framework -Dpackaging=library -DartifactVersion=2.2.8.0
if [ $? -gt 0 ]
then
  exit 1
fi
mvn -f pom-dotnet.xml -Dmaven.test.skip=true -Dbootstrap install $*
