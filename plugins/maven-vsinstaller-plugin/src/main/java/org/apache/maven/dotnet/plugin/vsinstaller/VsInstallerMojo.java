package org.apache.maven.dotnet.plugin.vsinstaller;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.model.Dependency;
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
import org.apache.maven.settings.Settings;
import org.apache.maven.settings.Profile;
import org.apache.maven.settings.Repository;
import org.codehaus.plexus.util.IOUtil;

import java.io.File;
import java.io.IOException;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * Installs Visual Studio 2005 addin.
 *
 * @author Shane Isbell
 * @goal install
 * @requiresProject false
 * @requiresDirectInvocation true
 */
public class VsInstallerMojo
    extends AbstractMojo
{

    /**
     * The the path to the local maven repository.
     *
     * @parameter expression="${settings.localRepository}"
     */
    private String localRepository;

    /**
     * The remote repository that contains the vsinstaller and NMaven artifacts.
     *
     * @parameter expression="${remoteRepository}"
     */
    private String remoteRepository;

    /**
     * Provides services for obtaining artifact information and dependencies
     *
     * @component
     */
    private ArtifactContext artifactContext;

    /**
     * @component
     */
    private ArtifactHandlerManager artifactHandlerManager;

    /**
     * Provides access to configuration information used by NMaven.
     *
     * @component
     */
    private org.apache.maven.dotnet.NMavenRepositoryRegistry nmavenRegistry;

    /**
     * Provides services to obtain executables.
     *
     * @component
     */
    private org.apache.maven.dotnet.executable.NetExecutableFactory netExecutableFactory;

    /**
     * @parameter expression="${settings}"
     */
    private Settings settings;

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

        //TODO: Only use active profiles
        List<Profile> profiles = settings.getProfiles();
        List<Repository> repositories = new ArrayList<Repository>();
        for ( Profile profile : profiles )
        {
            if ( profile.getRepositories() != null )
            {
                repositories.addAll( profile.getRepositories() );
            }
            if ( profile.getPluginRepositories() != null )
            {
                repositories.addAll( profile.getPluginRepositories() );
            }
        }

        for ( Repository repository : repositories )
        {
            remoteRepositories.add( new DefaultArtifactRepository( repository.getId(), repository.getUrl(),
                                                                   new DefaultRepositoryLayout() ) );
        }
        artifactContext.init( null, remoteRepositories, new File( localRepository ) );

        try
        {
            artifactContext.getArtifactInstaller().resolveAndInstallNetDependenciesForProfile( "VisualStudio2005",
                                                                                               new ArrayList<Dependency>() );
        }
        catch ( IOException e )
        {
            throw new MojoExecutionException( e.getMessage() );
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
            String pab = new File( localRepository ).getParent() + "\\pab";
            writer.write( addin.replaceAll( "\\$\\{localRepository\\}", pab.replaceAll( "\\\\", "\\\\\\\\" ) ) );

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

    private List<String> getGacInstallCommandsFor( Artifact artifact )
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
