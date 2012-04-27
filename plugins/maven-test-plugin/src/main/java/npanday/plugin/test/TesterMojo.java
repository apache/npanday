/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package npanday.plugin.test;

import npanday.ArtifactTypeHelper;
import npanday.PathUtil;
import npanday.PlatformUnsupportedException;
import npanday.executable.CommandExecutor;
import npanday.executable.ExecutableRequirement;
import npanday.executable.ExecutionException;
import npanday.executable.NetExecutable;
import npanday.executable.NetExecutableFactory;
import npanday.registry.RepositoryRegistry;
import npanday.vendor.SettingsUtil;
import npanday.vendor.StateMachineProcessor;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.util.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static com.google.common.base.Strings.isNullOrEmpty;

/**
 * Runs NUnit tests
 *
 * @author Shane Isbell
 * @goal test
 * @phase test
 * @description Runs NUnit tests
 */
public class TesterMojo extends AbstractMojo
{
    /**
     * @parameter expression="${npanday.settings}" default-value="${user.home}/.m2"
     */
    private String settingsPath;

    /**
     * @component
     */
    private RepositoryRegistry repositoryRegistry;

    /**
     * The maven project.
     * 
     * @parameter expression="${project}"
     * @required
     * @readonly
     */
    private MavenProject project;

    /**
     * The home of nunit. Use this if you 1) have not added nunit to your path and you only have MS installed; or 2)
     * have mono installed and want to use another version of nunit.
     * 
     * @parameter
     */
    private String nunitHome;

    /**
     * Display XML to the console
     * 
     * @parameter expression = "${xmlConsole}" default-value = "false"
     */
    private boolean xmlConsole;

    /**
     * On 64bit-systems, the tests will automatically perform in .NET 64bit-version. If you need to
     * run your tests using 32bit (native calls, i.e.) set this option to true.
     * @parameter default-value = "false"
     */
    private boolean forceX86;

    /**
     * Skips unit test
     * 
     * @parameter expression = "${skipTests}" default-value = "false"
     */
    private boolean skipTest;

    /**
     * Directory where reports are written.
     * 
     * @parameter expression = "${reportsDirectory}" default-value = "${project.build.directory}/nunit-reports"
     */
    private String reportsDirectory;

    /**
     * Test Assembly Location
     * 
     * @parameter expression = "${testAssemblyPath}" default-value = "${project.build.directory}/test-assemblies"
     */
    private String testAssemblyPath;

    /**
     * nUnitXmlFilePath
     * 
     * @parameter default-value = "${project.build.directory}/nunit-reports/TEST-${project.build.finalName}.xml"
     * @required
     */
    private File nUnitXmlFilePath;

    /**
     * nUnitResultOutputPath
     * 
     * @parameter default-value = "${project.build.directory}/nunit-reports/TEST-${project.build.finalName}-RESULTS.txt"
     * @required
     */
    private File nUnitResultOutputPath;

    /**
     * nUnitResultErrorOutputPath
     * 
     * @parameter default-value = "${project.build.directory}/nunit-reports/TEST-${project.build.finalName}-ERROR.txt"
     * @required
     */
    private File nUnitResultErrorOutputPath;

    /**
     * The local Maven repository.
     * 
     * @parameter expression="${settings.localRepository}"
     * @readonly
     */
    private File localRepository;

    /**
     * The artifact acts as an Integration test project
     *
     * @parameter
     */
    protected boolean integrationTest;

    /**
     * @component
     */
    private StateMachineProcessor processor;

    /**
     * The Vendor for the Compiler. Not case or white-space sensitive.
     *
     * @parameter expression="${vendor}"
     */
    private String vendor;

    /**
     * @parameter expression = "${vendorVersion}"
     */
    private String vendorVersion;

    /**
     * Specify the name of the NUnit command to be run, from within the <i>nunitHome</i>/bin directory.
     *  
     * @parameter
     * @deprecated since 1.5.0-incubating
     */
    private String nunitCommand;

    /**
     * The framework version to run the tests: 1.1, 2.0, 3.5, 4.0. Note that using this setting will require you to use
     * NUnit >= 2.5.
     *
     * @parameter expression = "${executionFrameworkVersion}"
     */
    private String executionFrameworkVersion;

    /**
     * @component
     */
    private NetExecutableFactory netExecutableFactory;
    
    private File getExecutableHome() 
    {
        return (nunitHome != null) ? new File(nunitHome, "bin") : null;
    }


    private List<String> getCommandsFor( )
    {
        String finalName = project.getBuild().getFinalName();
        List<String> commands = new ArrayList<String>();
        if ( testAssemblyPath.startsWith( "/" ) ) // nunit-console thinks *nix file format /home/user/ is an option
                                                  // due to / and fails.
        {
            testAssemblyPath = "/" + testAssemblyPath;
        }



        if(integrationTest)
        {
            // use the artifact itself if its an integration
            commands.add( testAssemblyPath + File.separator + project.getArtifactId() + ".dll" );
        }
        else
        {
            // if not use the commpiled test
            commands.add( testAssemblyPath + File.separator + project.getArtifactId() + "-test.dll" );
        }

        String switchChar = "/";
        commands.add( switchChar + "xml:" + nUnitXmlFilePath.getAbsolutePath() );

        commands.add( switchChar + "output:" + nUnitResultOutputPath.getAbsolutePath() );
        commands.add( switchChar + "err:" + nUnitResultErrorOutputPath.getAbsolutePath() );

        commands.add( switchChar + "labels" );

        if ( xmlConsole )
        {
            commands.add( switchChar + "xmlConsole" );
        }
        
        // Not supported on NUnit < 2.5 - see NPANDAY-332
        if ( executionFrameworkVersion != null && executionFrameworkVersion.length() > 0 )
        {
            getLog().debug( "NPANDAY-1100-012: Framework version:" + executionFrameworkVersion );
            commands.add( switchChar + "framework:" + "v" + executionFrameworkVersion );
        }

        return commands;
    }

    public void execute()
        throws MojoExecutionException, MojoFailureException
    {
        String skipTests = System.getProperty( "maven.test.skip" );
        if ( ( skipTests != null && skipTests.equalsIgnoreCase( "true" ) ) || skipTest )
        {
            getLog().warn( "NPANDAY-1100-000: Unit tests have been disabled." );
            return;
        }

        SettingsUtil.applyCustomSettings( getLog(), repositoryRegistry, settingsPath );
        
        String testFileName = "";

        if(integrationTest)
        {
            getLog().info("NPANDAY-1100-000.1: Artifact is an Integration Test");
            testFileName = project.getBuild().getDirectory() + File.separator + project.getArtifactId() + ".dll";
        }
        else
        {
            testFileName = project.getBuild().getDirectory() + File.separator + project.getArtifactId() + "-test.dll";
        }

        if ( !( new File( testFileName ).exists() ) )
        {
            getLog().info( "NPANDAY-1100-001: No Unit Tests" );
            return;
        }

        // TODO: resolve dependencies with scope test here
        getLog().warn( "NPANDAY-251: removed dependency resolution here!" );

        List<Artifact> nunitLibs = new ArrayList<Artifact>();
        Set<Artifact> artifacts = project.getDependencyArtifacts();

        for ( Artifact artifact : artifacts )
        {
            if ( ArtifactTypeHelper.isDotnetAnyGac( artifact.getType() ) )
            {
                continue;
            }

            if ( artifact.getGroupId().equals( "NUnit" ) )
            {
                nunitLibs.add( artifact );
            }
            else
            {
                try
                {
                    PathUtil.copyPlainArtifactFileToDirectory( artifact, new File( testAssemblyPath ) );
                }
                catch ( IOException e )
                {
                    throw new MojoExecutionException( "NPANDAY-1100-002: Error on copying artifact " + artifact.toString(), e );
                }
            }
        }

        if ( nunitLibs.size() == 0 )
        {
            throw new MojoExecutionException( "NPANDAY-1100-003: Could not find any nunit libraries." );
        }

        // Copy Main Artifact
        try
        {
            if ( project.getArtifact() != null && project.getArtifact().getFile() != null
                && project.getArtifact().getFile().exists() )
            {
                FileUtils.copyFileToDirectory( project.getArtifact().getFile(), new File( testAssemblyPath ) );
            }
        }
        catch ( IOException e )
        {
            throw new MojoExecutionException( "NPANDAY-1100-004: Unable to copy library to target directory: ", e );
        }
        // TODO: Check timestamps
        // Copy Test Artifact
        try
        {
            FileUtils.copyFileToDirectory( new File( testFileName ), new File( testAssemblyPath ) );
        }
        catch ( IOException e )
        {
            throw new MojoExecutionException( "NPANDAY-1100-005: Unable to copy library to target directory: ", e );
        }

        // Copy NUnit Dependencies
        for ( Artifact artifact : nunitLibs )
        {
            File file = new File( testAssemblyPath + File.separator + artifact.getArtifactId() + ".dll" );
            try
            {
                FileUtils.copyFile( artifact.getFile(), file );
            }
            catch ( IOException e )
            {
                throw new MojoExecutionException(
                                                  "NPANDAY-1100-006: Unable to copy nunit library to target directory: File = "
                                                      + file.getAbsolutePath(), e );
            }
        }

        FileUtils.mkdir( reportsDirectory );


        List<String> commands = getCommandsFor( );

        // pretty print nunit logs
        getLog().info( System.getProperty( "line.separator" ) );

        String profile = "NUNIT" + (forceX86 ? "-x86" : "");
        File executableHome = getExecutableHome();

        try
        {
            try
            {
                NetExecutable executable = netExecutableFactory.getExecutable(
                    new ExecutableRequirement( vendor, vendorVersion, executionFrameworkVersion, profile ), commands,
                    executableHome
                );

                executable.execute();
            }
            catch (PlatformUnsupportedException pue)
            {
                if (isNullOrEmpty(nunitCommand))
                {
                    throw new MojoExecutionException( "NPANDAY-1100-008: Unsupported Platform.", pue );
                }

                // TODO: This should rather be done through a configurable local executable-plugins.xml; then remove nunitcommand
                getLog().debug( "NPANDAY-1100-008: Platform unsupported, is your npanday-settings.xml configured correctly?", pue );        
                CommandExecutor commandExecutor = CommandExecutor.Factory.createDefaultCommmandExecutor(null);
                commandExecutor.setLogger( new org.codehaus.plexus.logging.AbstractLogger( 0, "nunit-logger" )
                {
                    Log log = getLog();

                    public void debug( String message, Throwable throwable )
                    {
                        log.debug( message, throwable );
                    }

                    public void error( String message, Throwable throwable )
                    {
                        log.error( message, throwable );
                    }

                    public void fatalError( String message, Throwable throwable )
                    {
                        log.error( message, throwable );
                    }

                    public Logger getChildLogger( String message )
                    {
                        return null;
                    }

                    public void info( String message, Throwable throwable )
                    {
                        log.info( message, throwable );
                    }

                    public void warn( String message, Throwable throwable )
                    {
                        log.warn( message, throwable );
                    }
                } );

                String executablePath = (executableHome != null) ? new File(executableHome, nunitCommand).toString() : nunitCommand;
                commandExecutor.executeCommand( executablePath, commands );
            }
        }
        catch ( ExecutionException e )
        {
            String line = System.getProperty( "line.separator" );
            throw new MojoFailureException( "NPANDAY-1100-007: There are test failures." + line + line + e.getMessage(), e);
        }
    }
}
