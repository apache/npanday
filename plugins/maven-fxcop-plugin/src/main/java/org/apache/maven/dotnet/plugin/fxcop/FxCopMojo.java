package org.apache.maven.dotnet.plugin.fxcop;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.dotnet.executable.ExecutionException;
import org.apache.maven.dotnet.PlatformUnsupportedException;
import org.apache.maven.dotnet.artifact.AssemblyRepositoryLayout;
import org.apache.maven.dotnet.artifact.AssemblyResolver;
import org.apache.maven.dotnet.artifact.ArtifactType;
import org.apache.maven.project.MavenProject;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.repository.DefaultArtifactRepository;
import org.apache.maven.artifact.resolver.ArtifactResolutionException;
import org.apache.maven.artifact.resolver.ArtifactNotFoundException;
import org.apache.maven.artifact.Artifact;
import org.codehaus.plexus.util.FileUtils;

import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.io.File;
import java.io.IOException;

/**
 * @author Shane Isbell
 * @goal fxcop
 */
public class FxCopMojo
    extends AbstractMojo
{
    /**
     * @component
     */
    private org.apache.maven.dotnet.executable.NetExecutableFactory netExecutableFactory;

    /**
     * The maven project.
     *
     * @parameter expression="${project}"
     * @required
     */
    private MavenProject project;

    /**
     * The Vendor for the executable.
     *
     * @parameter expression="${vendor}"
     */
    private String vendor;

    /**
     * @parameter expression = "${frameworkVersion}"
     */
    private String frameworkVersion;

    /**
     * The profile that the executable should use.
     *
     * @parameter expression = "${profile}" default-value = "DEFAULT"
     */
    private String profile;

    /**
     * @component
     */
    private AssemblyResolver assemblyResolver;

    /**
     * @parameter expression="${settings.localRepository}"
     * @readonly
     */
    private String localRepository;

    /**
     * @parameter expression = "${project.build.directory}"
     */
    private File targetDirectory;

    private File rootDir;


    public void execute()
        throws MojoExecutionException
    {
        //For multi-module
        if ( project.getPackaging().equals( "pom" ) )
        {

            if ( System.getProperty( "NMAVEN.ROOT_DIR" ) == null )
            {
                System.setProperty( "NMAVEN.ROOT_DIR", project.getBasedir().getAbsolutePath() );
            }
            return;
        }

        rootDir = ( System.getProperty( "NMAVEN.ROOT_DIR" ) != null ) ? new File(
            System.getProperty( "NMAVEN.ROOT_DIR" ) ) : null;

        ArtifactRepository localArtifactRepository =
            new DefaultArtifactRepository( "local", "file://" + localRepository, new AssemblyRepositoryLayout() );
        try
        {
            assemblyResolver.resolveTransitivelyFor( project, project.getArtifact(), project.getDependencies(),
                                                     project.getRemoteArtifactRepositories(), localArtifactRepository,
                                                     true );
        }
        catch ( ArtifactResolutionException e )
        {
            throw new MojoExecutionException( "NMAVEN-901-000: Unable to resolve assemblies", e );
        }
        catch ( ArtifactNotFoundException e )
        {
            throw new MojoExecutionException( "NMAVEN-901-001: Unable to resolve assemblies", e );
        }

        Set<Artifact> artifacts = project.getDependencyArtifacts();
        for ( Artifact artifact : artifacts )
        {
            if ( artifact.getType().startsWith( "gac" ) )
            {
                continue;
            }
            else
            {
                try
                {
                    FileUtils.copyFileToDirectory( artifact.getFile(), targetDirectory );
                }
                catch ( IOException e )
                {
                    throw new MojoExecutionException( "NMAVEN-1100-002: Artifact = " + artifact.toString(), e );
                }
            }
        }

        try
        {
            netExecutableFactory.getNetExecutableFor( vendor, frameworkVersion, profile, getCommands(),
                                                      null ).execute();
        }
        catch ( ExecutionException e )
        {
            throw new MojoExecutionException( "NMAVEN-xxx-000: Unable to execute: Vendor " + vendor +
                ", frameworkVersion = " + frameworkVersion + ", Profile = " + profile, e );
        }
        catch ( PlatformUnsupportedException e )
        {
            throw new MojoExecutionException( "NMAVEN-xxx-001: Platform Unsupported: Vendor " + vendor +
                ", frameworkVersion = " + frameworkVersion + ", Profile = " + profile, e );
        }
    }

    public List<String> getCommands()
        throws MojoExecutionException
    {
        List<String> commands = new ArrayList<String>();

        String targetPath = "target" + File.separator + project.getArtifactId() + "." +
            ArtifactType.getArtifactTypeForPackagingName( project.getPackaging() ).getExtension();
        String outputPath = "target" + File.separator + "Output.xml";

        String relativePathToTargetFile =
            ( rootDir != null ) ? new File( project.getBasedir(), targetPath ).getAbsolutePath().substring(
                rootDir.getAbsolutePath().length() + 1 ) : targetPath;
        String relativePathToOutputFile =
            ( rootDir != null ) ? new File( project.getBasedir(), outputPath ).getAbsolutePath().substring(
                rootDir.getAbsolutePath().length() + 1 ) : outputPath;

        commands.add( "/f:" + relativePathToTargetFile );
        commands.add( "/o:" + relativePathToOutputFile );
        return commands;
    }
}
