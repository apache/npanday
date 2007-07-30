package org.apache.maven.dotnet.plugin.repository;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.artifact.repository.ArtifactRepositoryFactory;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.repository.DefaultArtifactRepository;
import org.apache.maven.artifact.repository.layout.ArtifactRepositoryLayout;
import org.apache.maven.artifact.repository.layout.DefaultRepositoryLayout;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.metadata.ArtifactMetadata;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.deployer.ArtifactDeployer;
import org.apache.maven.artifact.deployer.ArtifactDeploymentException;
import org.apache.maven.dotnet.artifact.AssemblyResolver;
import org.apache.maven.dotnet.artifact.ArtifactContext;
import org.apache.maven.dotnet.repository.RepositoryConverter;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.artifact.ProjectArtifactMetadata;
import org.apache.maven.model.Model;
import org.apache.maven.model.Parent;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.codehaus.plexus.archiver.tar.TarArchiver;
import org.codehaus.plexus.archiver.ArchiverException;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.repository.RepositoryException;
import org.openrdf.sail.memory.MemoryStore;

import java.util.Set;
import java.util.HashSet;
import java.util.ArrayList;
import java.util.List;
import java.io.File;
import java.io.IOException;
import java.io.FileReader;
import java.io.FileNotFoundException;

/**
 * @author Shane Isbell
 * @goal package
 */
public class RepositoryAssemblerMojo
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
     * @parameter expression="${settings.localRepository}"
     * @readonly
     */
    private File localRepository;

    /**
     * @parameter expression="${withGac}"
     */
    private boolean withGac = false;

    /**
     * @component
     */
    private AssemblyResolver assemblyResolver;

    /**
     * @component
     */
    private ArtifactDeployer artifactDeployer;

    /**
     * Component used to create a repository
     *
     * @component
     */
    private ArtifactRepositoryFactory repositoryFactory;

    /**
     * @component
     */
    private ArtifactFactory artifactFactory;

    /**
     * @component
     */
    private ArtifactContext artifactContext;

    /**
     * @component
     */
    private RepositoryConverter repositoryConverter;

    public void execute()
        throws MojoExecutionException, MojoFailureException
    {
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

        artifactContext.init( project, project.getRemoteArtifactRepositories(), localRepository );

        List<Dependency> netDependencies = new ArrayList<Dependency>();

        for ( Dependency dependency : (List<Dependency>) project.getDependencies() )
        {
            netDependencies.add( dependency );
        }

        assemblyRepository( netDependencies, new DefaultRepositoryLayout() );

        try
        {
            repositoryConverter.convert( rdfRepository, localRepository );
        }
        catch ( IOException e )
        {
            throw new MojoExecutionException( e.getMessage() );
        }
    }

    public void assemblyRepository( List<Dependency> dependencies, ArtifactRepositoryLayout layout )
        throws MojoExecutionException, MojoFailureException
    {
        if ( dependencies.size() == 0 )
        {
            return;
        }

        ArtifactRepository localArtifactRepository =
            new DefaultArtifactRepository( "local", "file://" + localRepository, layout );
        ArtifactRepository deploymentRepository = repositoryFactory.createDeploymentArtifactRepository( null,
                                                                                                        "file://" +
                                                                                                            project.getBuild().getDirectory() +
                                                                                                            "/archive-tmp/repository/releases",
                                                                                                        layout, true );

        try
        {
            assemblyResolver.resolveTransitivelyFor( project, dependencies, project.getRemoteArtifactRepositories(),
                                                     localRepository, true );
        }
        catch ( IOException e )
        {
            throw new MojoExecutionException( e.getMessage() );
        }

        for ( Artifact artifact : (Set<Artifact>) project.getDependencyArtifacts() )
        {
            Set<Artifact> pomParentArtifacts = getPomArtifactsFor( artifact.getGroupId(), artifact.getArtifactId(),
                                                                   artifact.getVersion(), layout, true );
            Set<Artifact> pomArtifacts = getPomArtifactsFor( artifact.getGroupId(), artifact.getArtifactId(),
                                                             artifact.getVersion(), layout, false );
            if ( pomArtifacts.size() == 1 )
            {
                ArtifactMetadata metadata =
                    new ProjectArtifactMetadata( artifact, pomArtifacts.toArray( new Artifact[1] )[0].getFile() );
                artifact.addMetadata( metadata );
            }

            try
            {
                if ( withGac || !artifact.getType().startsWith( "gac" ) )
                {
                    artifactDeployer.deploy( artifact.getFile(), artifact, deploymentRepository,
                                             localArtifactRepository );
                }
                //Deploy parent poms
                for ( Artifact pomArtifact : pomParentArtifacts )
                {
                    artifactDeployer.deploy( pomArtifact.getFile(), pomArtifact, deploymentRepository,
                                             localArtifactRepository );
                }
            }
            catch ( ArtifactDeploymentException e )
            {
                throw new MojoExecutionException( "NMAVEN-DEPLOY: Deploy Failed", e );
            }
        }

        TarArchiver tarArchiver = new TarArchiver();
        try
        {
            tarArchiver.addDirectory(
                new File( project.getBuild().getDirectory(), "/archive-tmp/repository/releases" ) );
        }
        catch ( ArchiverException e )
        {
            throw new MojoExecutionException( "", e );
        }

        TarArchiver.TarCompressionMethod tarCompressionMethod = new TarArchiver.TarCompressionMethod();
        tarArchiver.setDestFile( new File( project.getBuild().getDirectory(), project.getArtifactId() + ".tar.gz" ) );
        try
        {
            tarCompressionMethod.setValue( "gzip" );
            tarArchiver.setCompression( tarCompressionMethod );
            tarArchiver.setIncludeEmptyDirs( false );
            tarArchiver.createArchive();
        }
        catch ( ArchiverException e )
        {
            throw new MojoExecutionException( "", e );
        }
        catch ( IOException e )
        {
            throw new MojoExecutionException( "", e );
        }

    }

    private Set<Artifact> getPomArtifactsFor( String groupId, String artifactId, String version,
                                              ArtifactRepositoryLayout layout, boolean addParents )
        throws MojoExecutionException
    {
        Set<Artifact> pomArtifacts = new HashSet<Artifact>();
        Artifact pomArtifact = artifactFactory.createArtifact( groupId, artifactId, version, "pom", "pom" );
        File pomFile = new File( localRepository, layout.pathOf( pomArtifact ) );

        if ( pomFile.exists() )
        {
            File tmpFile = null;
            try
            {
                tmpFile = File.createTempFile( "pom" + artifactId, "pom" );
                FileUtils.copyFile( pomFile, tmpFile );
            }
            catch ( IOException e )
            {
                e.printStackTrace();
            }
            pomArtifact.setFile( tmpFile );
            pomArtifacts.add( pomArtifact );
            FileReader fileReader;
            try
            {
                fileReader = new FileReader( tmpFile );
            }
            catch ( FileNotFoundException e )
            {
                throw new MojoExecutionException( "NMAVEN-000-000: Unable to read pom" );
            }
            MavenXpp3Reader reader = new MavenXpp3Reader();
            Model model;
            try
            {
                model = reader.read( fileReader );
            }
            catch ( XmlPullParserException e )
            {
                throw new MojoExecutionException( "NMAVEN-000-000: Unable to read model" );

            }
            catch ( IOException e )
            {
                throw new MojoExecutionException( "NMAVEN-000-000: Unable to read model" );
            }

            Parent parent = model.getParent();

            if ( parent != null && addParents )
            {
                pomArtifacts.addAll( getPomArtifactsFor( parent.getGroupId(), parent.getArtifactId(),
                                                         parent.getVersion(), layout, true ) );
            }
        }

        return pomArtifacts;
    }
}
