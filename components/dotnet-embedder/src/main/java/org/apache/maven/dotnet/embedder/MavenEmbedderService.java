package org.apache.maven.dotnet.embedder;

public interface MavenEmbedderService
{

    /**
     * Role used to register component implementations with the container.
     */
    String ROLE = MavenEmbedderService.class.getName();

    /**
     *
     * @param request
     */
    void execute( MavenExecutionRequest request );

    
}
