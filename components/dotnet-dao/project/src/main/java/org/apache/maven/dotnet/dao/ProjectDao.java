package org.apache.maven.dotnet.dao;

import org.apache.maven.dotnet.repository.Project;
import org.apache.maven.dotnet.registry.DataAccessObject;

import org.apache.maven.project.MavenProject;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.manager.WagonManager;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.model.Model;

import java.util.Set;
import java.util.List;
import java.io.IOException;
import java.io.File;

public interface ProjectDao
    extends DataAccessObject
{
    /**
     * Role used to register component implementations with the container.
     */
    String ROLE = ProjectDao.class.getName();

    Project getProjectFor( String groupId, String artifactId, String version, String artifactType,
                           String publicKeyTokenId )
        throws IOException;

    Project getProjectFor( MavenProject mavenProject )
        throws IOException;

    void storeProject( Project project, File localRepository, List<ArtifactRepository> artifactRepositories )
        throws IOException;

    Set<Artifact> storeProjectAndResolveDependencies( Project project, File localRepository,
                                                      List<ArtifactRepository> artifactRepositories )
        throws IOException;

    Set<Artifact> storeModelAndResolveDependencies( Model model, File pomFileDirectory, File localArtifactRepository,
                                                    List<ArtifactRepository> artifactRepositories )
        throws IOException;

    void init( ArtifactFactory artifactFactory, WagonManager wagonManager );

    boolean openConnection();

    boolean closeConnection(); 

}
