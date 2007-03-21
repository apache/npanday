package org.apache.maven.dotnet.embedder;

import org.codehaus.xfire.aegis.type.java5.*;

@XmlType(namespace="urn:maven-embedder")
public interface MavenExecutionRequest
{

    /**
     * Role used to register component implementations with the container.
     */
    String ROLE = MavenExecutionRequest.class.getName();

    @XmlElement(name="pomFile", namespace="urn:maven-embedder")
    String getPomFile();

    void setPomFile(java.lang.String string);

}
