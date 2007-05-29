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
import org.apache.maven.artifact.Artifact;
import org.apache.maven.dotnet.artifact.ArtifactContext;
import org.apache.maven.dotnet.artifact.NetDependenciesRepository;
import org.apache.maven.dotnet.artifact.NetDependencyMatchPolicy;
import org.apache.maven.dotnet.executable.NetExecutable;
import org.apache.maven.dotnet.executable.ExecutionException;
import org.apache.maven.dotnet.PlatformUnsupportedException;
import org.apache.maven.dotnet.registry.RepositoryRegistry;
import org.apache.maven.dotnet.vendor.Vendor;
import org.apache.maven.dotnet.model.netdependency.NetDependency;
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
     * @parameter expression="${remoteRepository}"
     */
    private String remoteRepository;

    /**
     * @component
     */
    private ArtifactContext artifactContext;

    /**
     * @component
     */
    private ArtifactHandlerManager artifactHandlerManager;

    /**
     * @component
     */
    private org.apache.maven.dotnet.NMavenRepositoryRegistry nmavenRegistry;

    /**
     * @component
     */
    private org.apache.maven.dotnet.executable.NetExecutableFactory netExecutableFactory;

    public void execute()
        throws MojoExecutionException, MojoFailureException
    {

        RepositoryRegistry repositoryRegistry;
        try
        {
            repositoryRegistry = nmavenRegistry.createRepositoryRegistry();
        }
        catch ( IOException e )
        {
            throw new MojoExecutionException(
                "NMAVEN-1600-000: Failed to create the repository registry for this plugin", e );
        }
        List<ArtifactRepository> remoteRepositories = new ArrayList<ArtifactRepository>();
        if ( remoteRepository != null )
        {
            ArtifactRepository remoteArtifactRepository =
                new DefaultArtifactRepository( "nmaven", remoteRepository, new DefaultRepositoryLayout() );
            remoteRepositories.add( remoteArtifactRepository );
        }

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

        //GAC Installs
        NetDependenciesRepository repository =
            (NetDependenciesRepository) repositoryRegistry.find( "net-dependencies" );

        List<NetDependencyMatchPolicy> gacInstallPolicies = new ArrayList<NetDependencyMatchPolicy>();
        gacInstallPolicies.add( new GacMatchPolicy( true ) );
        List<Dependency> gacInstallDependencies = repository.getDependenciesFor( gacInstallPolicies );
        for ( Dependency dependency : gacInstallDependencies )
        {
            List<Artifact> artifacts = artifactContext.getArtifactsFor( dependency.getGroupId(),
                                                                        dependency.getArtifactId(),
                                                                        dependency.getVersion(), dependency.getType() );
            try
            {
                NetExecutable netExecutable = netExecutableFactory.getNetExecutableFor(
                    Vendor.MICROSOFT.getVendorName(), "2.0.50727", "GACUTIL",
                    getGacInstallCommandsFor( artifacts.get( 0 ) ), null );
                netExecutable.execute();
                getLog().info( "NMAVEN-1600-004: Installed Assembly into GAC: Assembly = " +
                    artifacts.get( 0 ).getFile().getAbsolutePath() + ",  Vendor = " +
                    netExecutable.getVendor().getVendorName() );
            }
            catch ( ExecutionException e )
            {
                throw new MojoExecutionException( "NMAVEN-1600-005: Unable to execute gacutil:", e );
            }
            catch ( PlatformUnsupportedException e )
            {
                throw new MojoExecutionException( "NMAVEN-1600-006: Platform Unsupported:", e );
            }
        }

        OutputStreamWriter writer = null;
        try
        {
            String addin =
                IOUtil.toString( VsInstallerMojo.class.getResourceAsStream( "/template/NMaven.VisualStudio.AddIn" ) );
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

    public List<String> getGacInstallCommandsFor( Artifact artifact )
        throws MojoExecutionException
    {
        List<String> commands = new ArrayList<String>();
        commands.add( "/nologo" );
        commands.add( "/i" );
        commands.add( artifact.getFile().getAbsolutePath() );
        return commands;
    }

    private class GacMatchPolicy
        implements NetDependencyMatchPolicy
    {

        private boolean isGacInstall;

        public GacMatchPolicy( boolean isGacInstall )
        {
            this.isGacInstall = isGacInstall;
        }

        public boolean match( NetDependency netDependency )
        {
            return netDependency.isIsGacInstall() == isGacInstall;
        }
    }
}
