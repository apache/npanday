package org.apache.maven.plugin.nunit;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.csharp.helper.PackagingHelper;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.Os;
import org.codehaus.plexus.util.StringUtils;

import java.io.File;
import java.util.List;

/**
 * @author <a href="mailto:chris.stevenson@gmail.com">Chris Stevenson</a>
 * @requiresDependencyResolution test
 * @goal test
 * @phase test
 * @description Run tests in project using NUnit
 */
public class NUnitMojo
    extends AbstractMojo
{

    /**
     * Name of the generated assembly
     *
     * @parameter alias="jarName" expression="${project.build.finalName}"
     * @required
     */
    private String finalName;

    /**
     * Set this to 'true' to bypass unit tests entirely.
     * Its use is NOT RECOMMENDED, but quite convenient on occasion.
     *
     * @parameter expression="${maven.test.skip}"
     */
    private boolean skip;

    /**
     * The configuration file for the assembly, if required.
     *
     * @parameter
     */
    private File configFile;

    /**
     * The directory containing generated test classes of the project being tested.
     *
     * @parameter expression="${project.build.testOutputDirectory}"
     * @required
     */
    private File testOutputDirectory;

    /**
     * The file name for the unit-tests library. Defaults to unit-tests.dll
     *
     * @parameter
     */
    private String testOutputFileName;

    /**
     * A list of system properties to be passed..
     *
     * @parameter
     */
    private Property[] systemProperties;

    /**
     * This is the filename to invoke the runtime environment. On windows
     * using csc & MS dotnet this should be null.
     * <p/>
     * On Windows/*nix using mono this should be mono
     * <p/>
     * Using "default" will detect the current platform and use the
     * the default value for the platform.
     *
     * @parameter expression="${nunit.runtime}" default-value="default"
     */
    private String runtime;

    /**
     * @parameter expression="${project}"
     * @required
     * @readonly
     */
    protected MavenProject project;

    /**
     * Directory containing the classes.
     *
     * @parameter expression="${project.build.outputDirectory}"
     * @required
     * @readonly
     */
    private File outputDirectory;

    /**
     * Specifies whether system environment variables should be inherited
     * from the environment in which maven was run.
     * If this value is set to true (default), then environment variables such as
     * ${java.home} will be available without having to explictly specify
     * them within the systemProperties configuration element.  As you
     * would expect any property values specified in the systemProperties
     * configuration element take precedence.
     *
     * @parameter default-value="true"
     */
    private boolean inheritSystemProperties;

    public void execute()
        throws MojoExecutionException
    {

        if ( skip )
        {
            this.getLog().warn( "Skipping Unit Tests!!" );
            return;
        }

        this.getLog().info( "Creating NUnit environment...." );

        List dependencyartifacts = project.getTestArtifacts();

        String runtimeExecutable = getRuntimeExecutable( this.runtime );

        //this is a dirty firkle that is provided by the packaging class :-)
        File mainAssembly = outputDirectory;

        File testAssembly = null;

        if ( !StringUtils.isEmpty( testOutputFileName ) )
        {
            testAssembly = getFile( testOutputDirectory, testOutputFileName );
        }
        else
        {
            testAssembly = getFile( testOutputDirectory, "unit-tests", "dotnet-library" );
        }

        this.getLog().info( "Looking for test assembly at:" + testAssembly.getAbsolutePath() );

        if ( !testAssembly.exists() )
        {
            this.getLog().warn( "SKIPPING TESTS as no test assembly found. " );
        }

        NUnitEnvironment env = new NUnitEnvironment( this.getLog(), testOutputDirectory, dependencyartifacts,
                                                     mainAssembly, testAssembly, configFile, runtimeExecutable,
                                                     this.inheritSystemProperties, this.systemProperties );

        env.create();

        //execute Nunit tests.
        env.run();

        //if everything works then delete environment (else leave around for debugging)
        env.delete();

    }

    public File getFile( File directory, String fileName, String packaging )
        throws MojoExecutionException
    {

        String fileAbsolutePath =
            directory.getAbsolutePath() + File.separator + fileName + "." + PackagingHelper.getExtension( packaging );

        this.getLog().debug( "Looking for file[" + fileAbsolutePath + "]" );

        File f = new File( fileAbsolutePath );

        if ( !f.exists() )
        {
            throw new MojoExecutionException( "Cannot find file[" + fileAbsolutePath + "]" );
        }

        return f;
    }

    public File getFile( File directory, String fileName )
        throws MojoExecutionException
    {

        String fileAbsolutePath = directory.getAbsolutePath() + File.separator + fileName;

        this.getLog().debug( "Looking for file[" + fileAbsolutePath + "]" );

        File f = new File( fileAbsolutePath );

        if ( !f.exists() )
        {
            throw new MojoExecutionException( "Cannot find file[" + fileAbsolutePath + "]" );
        }

        return f;
    }

    private String getRuntimeExecutable( String param )
    {

        String exe = null;

        if ( "default".equals( param ) )
        {
            //if running windows leave null
            if ( Os.isFamily( "windows" ) )
            {
                exe = null;
                this.getLog().info( "OS Appears to be windows, leaving runtimeExecutable as null" );
            }
            else
            {
                //pop in mono by default.
                exe = "mono";
                this.getLog().info( "OS Appears to be *nix, setting runtimeExecutable to be \"mono\"" );
            }
        }
        else
        {
            exe = param;
            this.getLog().info( "User specified runtimeExecutable as:" + param );
        }

        return exe;
	}
}
