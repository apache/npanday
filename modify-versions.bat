cd imports/maven-pom-plugin
call mvn install
cd ../..
call mvn install -f pom-modify-versions.xml -DnmavenVersion=%*