package org.apache.maven.dotnet.plugin.vsinstaller;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.model.Dependency;
import org.apache.maven.artifact.resolver.ArtifactResolutionException;
import org.apache.maven.artifact.resolver.ArtifactNotFoundException;
import org.apache.maven.artifact.installer.ArtifactInstallationException;
import org.apache.maven.artifact.handler.manager.ArtifactHandlerManager;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.repository.DefaultArtifactRepository;
import org.apache.maven.artifact.repository.layout.DefaultRepositoryLayout;
import org.apache.maven.dotnet.artifact.ArtifactContext;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.IOUtil;

import java.io.File;
import java.io.IOException;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Shane Isbell
 * @goal install
 * @requiresProject false
 * @requiresDirectInvocation true
 */
public class VsInstallerMojo
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
     */
    private String localRepository;

    /**
     * @component
     */
    private ArtifactContext artifactContext;

    /**
     * @component
     */
    private ArtifactHandlerManager artifactHandlerManager;

    public void execute()
        throws MojoExecutionException, MojoFailureException
    {
        ArtifactRepository remoteArtifactRepository = new DefaultArtifactRepository( "nmaven",
                                                                                     "http://localhost:8080/repository",
                                                                                     new DefaultRepositoryLayout() );
        List<ArtifactRepository> remoteRepositories = new ArrayList<ArtifactRepository>();
        remoteRepositories.add( remoteArtifactRepository );
        artifactContext.init( project, remoteRepositories, new File( localRepository ) );

        try
        {
            artifactContext.getArtifactInstaller().resolveAndInstallNetDependenciesForProfile( "VisualStudio2005",
                                                                                               new ArrayList<Dependency>() );
        }
        catch ( ArtifactResolutionException e )
        {
            throw new MojoExecutionException( "NMAVEN-1600-003: Unable to resolve assemblies", e );
        }
        catch ( ArtifactNotFoundException e )
        {
            throw new MojoExecutionException( "NMAVEN-1600-004: Unable to resolve assemblies", e );
        }
        catch ( ArtifactInstallationException e )
        {
            throw new MojoExecutionException( "NMAVEN-1600-005: Unable to resolve assemblies", e );
        }

        OutputStreamWriter writer = null;
        try
        {
            String addin =
                IOUtil.toString( VsInstallerMojo.class.getResourceAsStream( "/template/NMaven.VisualStudio.AddIn" ));
            File outputFile = new File( System.getProperty( "user.home" ) +
                "\\My Documents\\Visual Studio 2005\\Addins\\NMaven.VisualStudio.AddIn" );

            if ( !outputFile.getParentFile().exists() )
            {
                outputFile.getParentFile().mkdir();
            }
            writer = new OutputStreamWriter( new FileOutputStream( outputFile ), "Unicode" );
            writer.write(
                addin.replaceAll( "\\$\\{localRepository\\}", localRepository.replaceAll( "\\\\", "\\\\\\\\" ) ) );

        }
        catch ( IOException e )
        {
            e.printStackTrace();
        }
        finally
        {
            try
            {
                if ( writer != null )
                {
                    writer.close();
                }
            }
            catch ( IOException e )
            {
                e.printStackTrace();
            }
        }

    }
}
