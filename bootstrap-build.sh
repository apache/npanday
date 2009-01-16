mvn install:install-file -Dfile=./thirdparty/org.apache.ws/XmlSchema-1.1.jar -DpomFile=./thirdparty/org.apache.ws/XmlSchema-1.1.pom -DgroupId=org.apache.ws.commons -DartifactId=XmlSchema -Dversion=1.1
mvn install -DRdf $*
if [ $? -gt 0 ]
then
  exit 1
fi

mvn npanday.plugin:maven-install-plugin:install-file -Dfile=thirdparty/NUnit/NUnit.Framework.dll -DgroupId=NUnit -DartifactId=NUnit.Framework -Dpackaging=library -DartifactVersion=2.2.8.0
if [ $? -gt 0 ]
then
  exit 1
fi
mvn -f pom-dotnet.xml -Dmaven.test.skip=true -Dbootstrap install $*
