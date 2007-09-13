package org.apache.maven.dotnet.plugin.repository;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.dotnet.dao.ProjectDao;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.project.MavenProject;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.repository.RepositoryException;
import org.openrdf.sail.memory.MemoryStore;

import java.io.File;
import java.io.IOException;

/**
 * @goal delete
 */
public class ArtifactManagerMojo
    extends AbstractMojo
{
    /**
     * The maven project.
     *
     * @parameter expression="${project}"
     * @required
     */
    private MavenProject project;

    /**
     * The local Maven repository.
     *
     * @parameter expression="${settings.localRepository}"
     * @readonly
     */
    private File localRepository;

    /**
     * @component
     */
    private org.apache.maven.dotnet.registry.DataAccessObjectRegistry daoRegistry;

    /**
     * @component
     */
    private org.apache.maven.artifact.manager.WagonManager wagonManager;

    /**
     * The artifact factory component, which is used for creating artifacts.
     *
     * @component
     */
    private ArtifactFactory artifactFactory;

    public void execute()
        throws MojoExecutionException, MojoFailureException
    {
        if ( localRepository == null )
        {
            localRepository = new File( System.getProperty( "user.home" ), ".m2/repository" );
        }

        String artifactValue = System.getProperty( "artifact" );
        String[] tokens = artifactValue.split( "[:]" );

        File dataDir = new File( localRepository.getParentFile(), "/uac/rdfRepository" );
        org.openrdf.repository.Repository rdfRepository = new SailRepository( new MemoryStore( dataDir ) );
        try
        {
            rdfRepository.initialize();
        }
        catch ( RepositoryException e )
        {
            throw new MojoExecutionException( e.getMessage() );
        }

        ProjectDao dao = (ProjectDao) daoRegistry.find( "dao:project" );
        dao.init( artifactFactory, wagonManager );
        dao.openConnection();

        try
        {
            dao.removeProjectFor( tokens[0], tokens[1], tokens[2], tokens[3] );
        }
        catch ( IOException e )
        {
            e.printStackTrace();
            throw new MojoExecutionException( e.getMessage() );
        }
        dao.closeConnection();
    }
}
