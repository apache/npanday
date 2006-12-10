package org.apache.maven.plugin.nunit;

import org.apache.commons.io.FileUtils;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.csharp.helper.PackagingHelper;
import org.apache.maven.plugin.logging.Log;
import org.codehaus.plexus.util.StringUtils;
import org.codehaus.plexus.util.cli.CommandLineException;
import org.codehaus.plexus.util.cli.CommandLineUtils;
import org.codehaus.plexus.util.cli.Commandline;
import org.codehaus.plexus.util.cli.StreamConsumer;
import org.codehaus.plexus.util.cli.WriterStreamConsumer;

import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Iterator;
import java.util.List;

/**
 * NUNIT-CONSOLE [inputfiles] [options]
 * <p/>
 * Runs a set of NUnit tests from the console.
 * <p/>
 * You may specify one or more assemblies or a single
 * project file of type .nunit.
 * <p/>
 * Options:
 * \/fixture=STR         Fixture to test
 * \/config=STR          Project configuration to load
 * \/xml=STR             Name of XML output file
 * \/transform=STR       Name of transform file
 * \/xmlConsole          Display XML to the console
 * \/output=STR          File to receive test output (Sho
 * \/err=STR             File to receive test error outpu
 * \/labels              Label each test in stdOut
 * \/include=STR         List of categories to include
 * \/exclude=STR         List of categories to exclude
 * \/noshadow            Disable shadow copy
 * \/thread              Run tests on a separate thread
 * \/wait                Wait for input before closing co
 * \/nologo              Do not display the logo
 * \/help                Display help (Short format: /?)
 */

public class NUnitEnvironment
{

    private Log log = null;

    private static String directoryName = ".nunit";

    private static String argumentsName = "nunit-arguments";

    private static String nunitOutputPrefix = "[NUNIT] ";

    private File directory = null;

    private File outputDir = null;

    private File mainAssembly = null;

    private File testAssembly = null;

    private File nunitConsoleFile = null;

    private File unitTestConfig = null;

    private String runtime = null;

    private boolean inheritSystemProperties;

    private Property[] systemProperties;

    private List artifacts = null;

    public NUnitEnvironment( Log log, File outputDir, List artifacts, File mainAssembly, File testAssembly,
                             File unitTestConfig, String runtime, boolean inheritSystemProperties,
                             Property[] systemProperties )
    {
        this.log = log;
        this.outputDir = outputDir;
        this.artifacts = artifacts;
        this.mainAssembly = mainAssembly;
        this.testAssembly = testAssembly;
        this.unitTestConfig = unitTestConfig;
        this.runtime = runtime;
        this.inheritSystemProperties = inheritSystemProperties;
        this.systemProperties = systemProperties;
    }

    public void create()
        throws MojoExecutionException
    {

        try
        {

            directory = new File( outputDir.getAbsolutePath() + File.separator + directoryName );

            if ( directory.exists() )
            {

                FileUtils.deleteDirectory( directory );
            }

            FileUtils.forceMkdir( directory );

            copyDependencies( directory, artifacts );

        }
        catch ( IOException ioex )
        {
            throw new MojoExecutionException( "IOException occurred while trying to create local copy of nunit [" +
                outputDir.getAbsolutePath() + File.separator + ".nunit]", ioex );
        }
    }

    public Log getLog()
    {
        return log;
    }

    public void setLog( Log log )
    {
        this.log = log;
    }


    private void copyDependencies( File directory, List artifacts )
        throws MojoExecutionException, IOException
    {

        for ( Iterator i = artifacts.iterator(); i.hasNext(); )
        {

            Artifact a = (Artifact) i.next();

            if ( !a.getFile().exists() )
            {
                throw new MojoExecutionException( "Artifact file:" + a.getFile() + " does not exist, path provided:" +
                    a.getFile().getAbsolutePath() );
            }

            //only copy non-syetem deps to directory (system deps you get for free.. :-)
            if ( !a.getFile().isDirectory() && ( !a.getScope().equals( Artifact.SCOPE_SYSTEM ) ) )
            {
                //if reference is a non csharp (could be java for an integration test, then ignore it.
                if ( PackagingHelper.isDotnetPackaging( a.getType() ) )
                {
                    FileUtils.copyFileToDirectory( a.getFile(), outputDir );
                }
            }

            if ( a.getArtifactId().equals( "nunit-console" ) )
            {
                nunitConsoleFile = new File( outputDir + File.separator + a.getFile().getName() );
            }
        }

        if ( nunitConsoleFile == null )
        {
            throw new MojoExecutionException(
                "Cannot find dependency with id:nunit.console, cannot execute NUnit Plugin without this." );
        }

        FileUtils.copyFileToDirectory( this.mainAssembly, outputDir );

        if ( unitTestConfig == null )
        {
            this.getLog().info( nunitOutputPrefix + "\t No config file specified." );
        }
        else if ( unitTestConfig.exists() && unitTestConfig.isFile() )
        {
            FileUtils.copyFile( unitTestConfig, new File( outputDir, this.testAssembly.getName() + ".config" ) );
            this.getLog().info( nunitOutputPrefix + "\t Copying config [" + unitTestConfig.getAbsolutePath() +
                "] to [" + new File( directory, this.testAssembly.getName() + ".config" ).getAbsolutePath() );
        }
        else
        {
            this.getLog().info( nunitOutputPrefix + "\t Config file doesn't exist or is not a file [" +
                unitTestConfig.getAbsolutePath() + "]" );
        }

    }

    public void run()
        throws MojoExecutionException
    {

        Commandline cli = new Commandline();
        try
        {
            cli.setWorkingDirectory( outputDir.getCanonicalPath() );
        }
        catch ( IOException e1 )
        {
            throw new MojoExecutionException( "Could not set working directory to: " + outputDir );
        }

        File executable = nunitConsoleFile;

        if ( executable == null )
        {
            throw new MojoExecutionException(
                "Cannot find dependency with id:nunit.console, cannot execute NUnit Plugin without this." );
        }

        if ( StringUtils.isEmpty( runtime ) )
        {
            cli.setExecutable( nunitConsoleFile.getName() );
        }
        else
        {
            cli.setExecutable( runtime );
            cli.createArgument().setValue( nunitConsoleFile.getName() );
        }
        cli.createArgument().setValue( testAssembly.getName() );

        if ( this.systemProperties != null )
        {
            for ( int i = 0; i < this.systemProperties.length; i++ )
            {
                Property systemProperty = this.systemProperties[i];
                getLog().info( nunitOutputPrefix + "adding systemProperty: " + systemProperty );
                cli.addEnvironment( systemProperty.getKey(), systemProperty.getValue() );
            }
        }
        //It turns out to be very important to place the call to cli.addSystemEnvironment() after
        //all of the specified system properties have been added above.
        //The choosen order ensures the specified system property values take precidence
        //over any inherited values.
        if ( this.inheritSystemProperties )
        {
            getLog().info( nunitOutputPrefix + "Inheriting system properties." );
            try
            {
                cli.addSystemEnvironment();
            }
            catch ( Exception ex )
            {
                throw new MojoExecutionException( "Encountered problems adding system environment variables to runtime",
                                                  ex );
            }
        }

        try
        {
            logEnvironment( cli );

            Writer outWriter = new OutputStreamWriter( System.out );

            Writer errWriter = new OutputStreamWriter( System.err );

            StreamConsumer out = new WriterStreamConsumer( outWriter );

            StreamConsumer err = new WriterStreamConsumer( errWriter );

            System.out.println( "-------------------------------------------------------" );
            System.out.println( "T E S T S											   " );
            System.out.println( "-------------------------------------------------------" );

            int result = CommandLineUtils.executeCommandLine( cli, out, err );

//    	  try {
//    		  //writeNUnitOutputToLog( new BufferedReader( new StringReader( stringWriter.toString() ) ) );
//    	  }catch(IOException ioex){
//    		  throw new MojoExecutionException(ioex.getMessage(), ioex);
//    	  }

            if ( result != 0 )
            {
                throw new MojoExecutionException( "Result of execution is: \'" + result + "\'. NUnit failed." );
            }
        }
        catch ( CommandLineException e )
        {
            throw new MojoExecutionException( e.getMessage(), e );
        }
    }

    public void delete()
        throws MojoExecutionException
    {
        if ( directory != null && directory.exists() )
        {
            try
            {
                FileUtils.deleteDirectory( directory );
            }
            catch ( IOException ioex )
            {
                throw new MojoExecutionException( "Could not delete temporary nunit dir:" + directory.getAbsolutePath(),
                                                  ioex );
            }
        }
    }

//	private void writeNUnitOutputToLog(BufferedReader reader) throws IOException {
//		
//        String line = reader.readLine();
//
//        while( line != null )
//        {
//        	log.info(nunitOutputPrefix + line);
//        	
//        	line = reader.readLine();
//        }
//	}

    private void logEnvironment( Commandline cli )
        throws MojoExecutionException
    {

        getLog().info( nunitOutputPrefix + "NUnit config:" );
        getLog().info( nunitOutputPrefix + "\t nunit binary [" + nunitConsoleFile.getAbsolutePath() + "]" );
        getLog().info( nunitOutputPrefix + "\t test assembly [" + testAssembly.getAbsolutePath() + "]" );
        getLog().info( nunitOutputPrefix + "\t main assembly [" + mainAssembly.getAbsolutePath() + "]" );

        getLog().info(
            nunitOutputPrefix + "\t (nb. All references copied locally [" + directory.getAbsolutePath() + "])" );

        for ( Iterator i = artifacts.iterator(); i.hasNext(); )
        {
            Artifact a = (Artifact) i.next();
            if ( PackagingHelper.isDotnetPackaging( a.getType() ) )
            {
                getLog().info( nunitOutputPrefix + "\t reference:" + a.getFile().getName() );
            }
        }

        try
        {
            File f = new File( outputDir, "nunit-arguments" );
            f.createNewFile();
            //params are: 1=file to write to,2=data to write, 3=encoding (null for platform default
            FileUtils.writeStringToFile( f, cli.toString(), null );

            getLog().info( nunitOutputPrefix + "\t nunit command line written to [" + f.getAbsolutePath() + "]" );
        }
        catch ( IOException ioex )
        {
            throw new MojoExecutionException( "Could not create nunit-argumnets file @" + outputDir.getAbsolutePath() +
                File.separator + argumentsName );
        }
    }
}
