package org.apache.maven.dotnet.plugin.fxcop;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.dotnet.artifact.AssemblyResolver;
import org.apache.maven.dotnet.ArtifactType;
import org.apache.maven.dotnet.executable.ExecutionException;
import org.apache.maven.dotnet.PlatformUnsupportedException;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.model.Model;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

import java.io.File;
import java.io.IOException;
import java.io.FileReader;
import java.util.Set;
import java.util.List;
import java.util.ArrayList;

/**
 * @author Shane Isbell
 * @goal aggregate
 * @aggregator false
 */
public class FxCopAggregateMojo
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
     * @parameter expression = "${profile}" default-value = "Microsoft:FxCop:FxCopCmd"
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
    private File localRepository;

    /**
     * @parameter expression = "${project.build.directory}"
     */
    private File targetDirectory;

    /**
     * The artifact factory component, which is used for creating artifacts.
     *
     * @component
     */
    private ArtifactFactory artifactFactory;


    public void execute()
        throws MojoExecutionException
    {
        Model model = project.getModel();
        List<Dependency> aggregateDependencies = new ArrayList<Dependency>();
        aggregateDependencies.addAll( model.getDependencies() );
        try
        {
            aggregateDependencies.addAll( addDependenciesFrom( project.getFile() ) );
        }
        catch ( IOException e )
        {
            throw new MojoExecutionException("NMAVEN-000-000:");
        }

        try
        {
            assemblyResolver.resolveTransitivelyFor( project, aggregateDependencies,
                                                     project.getRemoteArtifactRepositories(), localRepository,
                                                     true );
        }
        catch ( IOException e )
        {
            throw new MojoExecutionException(e.getMessage());
        }

        for ( Artifact artifact : (Set<Artifact>) project.getDependencyArtifacts() )
        {
            if ( artifact.getFile() != null && !artifact.getGroupId().startsWith( "NUnit"))
            {
                try
                {
                    FileUtils.copyFileToDirectory( artifact.getFile(), new File( targetDirectory, "fxcop" ) );
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

        String targetPath = "target" + File.separator + "fxcop";
        String outputPath = targetPath + File.separator + "Output.xml";

        commands.add( "/f:" + targetPath );
        commands.add( "/o:" + outputPath );
        return commands;
    }

    private Model fileToPom( File pomFile )
        throws IOException
    {
        FileReader fileReader = new FileReader( pomFile );
        MavenXpp3Reader reader = new MavenXpp3Reader();
        Model model;
        try
        {
            model = reader.read( fileReader );
        }
        catch ( XmlPullParserException e )
        {
            e.printStackTrace();
            throw new IOException( "NMAVEN-002-013: Unable to read pom file" );
        }
        return model;
    }

    private List<Dependency> addDependenciesFrom( File pomFile )
        throws IOException
    {
        List<Dependency> dependencies = new ArrayList<Dependency>();

        Model model = fileToPom( pomFile );
        if ( !model.getPackaging().equals( "pom" ) )
        {
            Dependency rootDependency = new Dependency();
            rootDependency.setArtifactId( model.getArtifactId() );
            rootDependency.setGroupId( model.getGroupId() );
            rootDependency.setVersion( model.getVersion() );
            rootDependency.setType(
                ArtifactType.getArtifactTypeForPackagingName( model.getPackaging() ).getPackagingType() );
            rootDependency.setScope( Artifact.SCOPE_COMPILE );
            dependencies.add( rootDependency );
        }

        List<String> modules = ( model != null ) ? model.getModules() : new ArrayList<String>();
        for ( String module : modules )
        {
            File childPomFile = new File( pomFile.getParent() + "/" + module + "/pom.xml" );
            if ( childPomFile.exists() )
            {
                dependencies.addAll( fileToPom( childPomFile ).getDependencies() );
                dependencies.addAll( addDependenciesFrom( childPomFile ) );
            }
        }
        return dependencies;
    }
}
