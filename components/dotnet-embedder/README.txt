Axis Plugin for Maven (Java2Wsdl) does not work, so here the manual steps to generate a WSDL:
1) Install Axis2
2) mvn install from the dotnet-embedder directory
3) java2wsdl -cn org.apache.maven.dotnet.embedder.MavenEmbedderService -cp target/dotnet-embedder-0.14.jar -o src/main/resources

wsdl2java -o . -uri src\main\resources\MavenEmbedder.wsdl -s -S src\main\java -ss -sd