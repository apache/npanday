package org.apache.maven.dotnet.plugin.embedder;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.artifact.resolver.ArtifactResolutionException;
import org.apache.maven.artifact.resolver.ArtifactNotFoundException;
import org.apache.maven.artifact.resolver.ArtifactResolutionResult;
import org.apache.maven.artifact.resolver.ArtifactResolver;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.metadata.ArtifactMetadataSource;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.versioning.VersionRange;
import org.apache.maven.artifact.installer.ArtifactInstallationException;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.repository.DefaultArtifactRepository;
import org.apache.maven.artifact.repository.layout.DefaultRepositoryLayout;
import org.apache.maven.model.Dependency;
import org.apache.maven.dotnet.artifact.AssemblyResolver;
import org.apache.maven.dotnet.artifact.ArtifactContext;
import org.apache.maven.dotnet.vendor.VendorInfo;
import org.apache.maven.dotnet.vendor.VendorFactory;
import org.apache.maven.dotnet.vendor.VendorUnsupportedException;
import org.apache.maven.dotnet.PlatformUnsupportedException;

import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.MalformedURLException;
import java.net.URLConnection;


/**
 * @goal start
 * @requiresProject false
 * @requiresDirectInvocation true
 */
public class EmbedderStarterMojo
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
     * @component
     */
    private AssemblyResolver assemblyResolver;

    /**
     * @parameter expression="${settings.localRepository}"
     * @readonly
     */
    private File localRepository;

    /**
     * @component
     */
    private ArtifactContext artifactContext;

    /**
     * @component
     */
    private ArtifactFactory artifactFactory;

    /**
     * @component
     */
    private ArtifactResolver resolver;

    /**
     * @component
     */
    private ArtifactMetadataSource metadata;

    /**
     * The Vendor for the executable. Supports MONO and MICROSOFT: the default value is <code>MICROSOFT</code>. Not
     * case or white-space sensitive.
     *
     * @parameter expression="${vendor}"
     */
    private String vendor;

    /**
     * @parameter expression = "${vendorVersion}"
     */
    private String vendorVersion;

    /**
     * @parameter expression = "${port}" default-value="8080"
     */
    private int port;

    /**
     * @parameter expression = "${warFile}"
     * @required
     */
    private File warFile;

    /**
     * @parameter expression = "${frameworkVersion}"
     */
    private String frameworkVersion;

    /**
     * @parameter expression = "${pom.version}"
     */
    private String pomVersion;

    /**
     * File logger: needed for creating logs when the IDE starts because the console output and thrown exceptions are
     * not available
     */
    private static Logger logger = Logger.getAnonymousLogger();

    /**
     * @component
     */
    private org.apache.maven.dotnet.executable.NetExecutableFactory netExecutableFactory;

    public void execute()
        throws MojoExecutionException, MojoFailureException
    {
        try
        {
            logger.addHandler( new FileHandler( "C:\\tmp\\nmaven-embedder-jetty.log" ) );
        }
        catch ( IOException e )
        {
            e.printStackTrace();
        }

        if ( localRepository == null )
        {
            localRepository = new File( System.getProperty( "user.home" ), ".m2/repository" );
        }
        logger.info( "NMAVEN: Found local repository: Path =  " + localRepository );

        List<ArtifactRepository> remoteRepositories = new ArrayList<ArtifactRepository>();
        //   ArtifactRepository remoteArtifactRepository = new DefaultArtifactRepository( "nmaven", "http://localhost:" +
        //       port + "/repository", new DefaultRepositoryLayout() );
        // remoteRepositories.add( remoteArtifactRepository );

        ArtifactRepository localArtifactRepository =
            new DefaultArtifactRepository( "local", "file://" + localRepository, new DefaultRepositoryLayout() );

        artifactContext.init( project, remoteRepositories, localRepository );

        try
        {
            artifactContext.getArtifactInstaller().resolveAndInstallNetDependenciesForProfile( "VisualStudio2005",
                                                                                               new ArrayList<Dependency>() );
        }
        catch ( ArtifactResolutionException e )
        {
            logger.severe( "NMAVEN-1600-003: Unable to resolve net dependencies: " + e.getMessage() );
            throw new MojoExecutionException( "NMAVEN-1600-003: Unable to resolve net dependencies:", e );
        }
        catch ( ArtifactNotFoundException e )
        {
            logger.severe( "NMAVEN-1600-004: Unable to resolve net dependencies: " + e.getMessage() );
            throw new MojoExecutionException( "NMAVEN-1600-004: Unable to resolve net dependencies:", e );
        }
        catch ( ArtifactInstallationException e )
        {
            logger.severe( "NMAVEN-1600-005: Unable to resolve net dependencies: " + e.getMessage() );
            throw new MojoExecutionException( "NMAVEN-1600-005: Unable to resolve net dependencies:", e );
        }

        Set<Artifact> artifactDependencies = new HashSet<Artifact>();
        Artifact artifact = artifactFactory.createDependencyArtifact( "org.mortbay.jetty", "jetty-embedded",
                                                                      VersionRange.createFromVersion( "6.1.3" ), "jar",
                                                                      null, "runtime", null );
        logger.info( "NMAVEN-000-000: Dependency: Type  = " + artifact.getType() + ", Artifact ID = " +
            artifact.getArtifactId() );
        artifactDependencies.add( artifact );

        ArtifactResolutionResult result;
        try
        {
            result = resolver.resolveTransitively( artifactDependencies, project.getArtifact(), localArtifactRepository,
                                                   project.getRemoteArtifactRepositories(), metadata, null );
        }
        catch ( ArtifactResolutionException e )
        {
            logger.severe( "NMAVEN:" + e.getMessage() );
            throw new MojoExecutionException( "", e );
        }
        catch ( ArtifactNotFoundException e )
        {
            logger.severe( "NMAVEN:" + e.getMessage() );
            throw new MojoExecutionException( "", e );
        }

        List<String> commands = new ArrayList<String>();
        commands.add( "-Dport=" + String.valueOf( port ) );
        commands.add( "-DwarFile=" + warFile.getAbsolutePath() );
        commands.add( "-classpath" );
        commands.add( artifactsToClassPath( result.getArtifacts() ) );
        commands.add( "org.apache.maven.dotnet.jetty.JettyStarter" );
        logger.info( commands.toString() );
        VendorInfo vendorInfo = VendorInfo.Factory.createDefaultVendorInfo();
        if ( vendor != null )
        {
            try
            {
                vendorInfo.setVendor( VendorFactory.createVendorFromName( vendor ) );
            }
            catch ( VendorUnsupportedException e )
            {
                throw new MojoExecutionException( "", e );
            }
        }
        vendorInfo.setFrameworkVersion( frameworkVersion );
        vendorInfo.setVendorVersion( vendorVersion );
        try
        {

            Runnable executable =
                (Runnable) netExecutableFactory.getJavaExecutableFromRepository( vendorInfo, commands );
            Thread thread = new Thread( executable );
            thread.start();
        }
        catch ( PlatformUnsupportedException e )
        {
            logger.severe( "NMAVEN-1400-001: Platform Unsupported: Vendor " + ", frameworkVersion = " +
                frameworkVersion + ", Message =" + e );
            throw new MojoExecutionException(
                "NMAVEN-1400-001: Platform Unsupported: Vendor " + ", frameworkVersion = " + frameworkVersion, e );
        }

        URL embedderUrl = null;
        try
        {
            embedderUrl = new URL( "http://localhost:8080/dotnet-service-embedder" );
        }
        catch ( MalformedURLException e )
        {
            e.printStackTrace();
        }

        for ( int i = 0; i < 3; i++ )
        {
            try
            {
                Thread.sleep( 5000 );
            }
            catch ( InterruptedException e )
            {
                e.printStackTrace();
            }

            URLConnection connection;
            try
            {
                connection = embedderUrl.openConnection();
            }
            catch ( IOException e )
            {
                logger.severe( "Could not open a connection to: http://localhost:8080/dotnet-service-embedder:" );
            }
        }
    }

    private String artifactsToClassPath( Set<Artifact> artifacts )
    {
        StringBuffer sb = new StringBuffer();
        for ( Artifact artifact : artifacts )
        {
            sb.append( "\"" ).append( artifact.getFile().getAbsolutePath() ).append( "\"" ).append( ";" );
        }

        File starterFile = new File( localRepository, "org\\apache\\maven\\dotnet\\dotnet-jetty\\" + pomVersion +
            "\\dotnet-jetty-" + pomVersion + ".jar" );
        sb.append( "\"" ).append( starterFile.getAbsolutePath() ).append( "\"" );
        return sb.toString();
    }
}
