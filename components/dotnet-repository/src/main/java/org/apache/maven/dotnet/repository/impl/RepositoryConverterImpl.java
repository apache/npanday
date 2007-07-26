package org.apache.maven.dotnet.repository.impl;

import org.apache.maven.dotnet.repository.RepositoryConverter;
import org.apache.maven.dotnet.dao.Project;
import org.apache.maven.dotnet.dao.ProjectDao;
import org.apache.maven.dotnet.dao.ProjectFactory;
import org.apache.maven.dotnet.registry.DataAccessObjectRegistry;
import org.apache.maven.dotnet.ArtifactType;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Writer;
import org.apache.maven.artifact.repository.layout.ArtifactRepositoryLayout;
import org.apache.maven.artifact.repository.layout.DefaultRepositoryLayout;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.handler.ArtifactHandler;
import org.apache.maven.artifact.handler.DefaultArtifactHandler;
import org.apache.maven.artifact.manager.WagonManager;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.openrdf.repository.Repository;
import org.codehaus.plexus.util.IOUtil;
import org.codehaus.plexus.util.FileUtils;

import java.io.File;
import java.io.IOException;
import java.io.FileWriter;
import java.util.logging.Logger;
import java.util.Set;

public class RepositoryConverterImpl
    implements RepositoryConverter
{

    private org.apache.maven.artifact.manager.WagonManager wagonManager;

    /**
     * The artifact factory component, which is used for creating artifacts.
     */
    private org.apache.maven.artifact.factory.ArtifactFactory artifactFactory;

    private org.apache.maven.dotnet.registry.DataAccessObjectRegistry daoRegistry;

    private static Logger logger = Logger.getAnonymousLogger();

    protected void initTest( DataAccessObjectRegistry daoRegistry, ArtifactFactory artifactFactory,
                             WagonManager wagonManager )
    {
        this.daoRegistry = daoRegistry;
        this.artifactFactory = artifactFactory;
        this.wagonManager = wagonManager;
    }

    public void convert( Repository repository, File mavenRepository )
        throws IOException
    {
        ProjectDao dao = (ProjectDao) daoRegistry.find( "dao:project" );
        dao.init( artifactFactory, wagonManager );
        dao.setRdfRepository( repository );
        dao.openConnection();
        Set<Project> projects = dao.getAllProjects();
        for ( Project project : projects )
        {
            System.out.println(project.getArtifactId() + ":" + project.getProjectDependencies().size());
            Artifact artifact = ProjectFactory.createArtifactFrom( project, artifactFactory, mavenRepository );
            Model model = ProjectFactory.createModelFrom( project );

            ArtifactHandler handler = new DefaultArtifactHandler(
                ArtifactType.getArtifactTypeForPackagingName( artifact.getType() ).getExtension() );
            artifact.setArtifactHandler( handler );

            ArtifactRepositoryLayout layout = new DefaultRepositoryLayout();
            if ( !project.getArtifactType().equals( "pom" ) )
            {
                if ( artifact.getFile().exists() )
                {
                    FileUtils.copyFile( artifact.getFile(), new File( mavenRepository, layout.pathOf( artifact ) ) );
                }
                else
                {
                    logger.info("NMAVEN-000-000: Could not find file: " + artifact.getFile().getAbsolutePath());
                    continue;
                }
            }

            handler = new DefaultArtifactHandler( "pom" );
            artifact.setArtifactHandler( handler );

            File pomFile = new File( mavenRepository, layout.pathOf( artifact ) );
            FileWriter fileWriter = new FileWriter( pomFile );
            new MavenXpp3Writer().write( fileWriter, model );
            IOUtil.close( fileWriter );
        }
        dao.closeConnection();
    }
}
