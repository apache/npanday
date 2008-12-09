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

package org.apache.maven.dotnet.plugin.test;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.artifact.Artifact;
import org.codehaus.plexus.util.FileUtils;
import org.apache.maven.plugin.logging.Log;

import java.util.ArrayList;
import java.util.Set;
import java.util.List;
import java.io.IOException;
import java.io.File;

import org.apache.maven.dotnet.executable.ExecutionException;
import org.apache.maven.dotnet.vendor.Vendor;
import org.apache.maven.dotnet.executable.CommandExecutor;
import org.apache.maven.dotnet.artifact.AssemblyResolver;
import org.codehaus.plexus.logging.AbstractLogger;
import org.codehaus.plexus.logging.Logger;

/**
 * Runs NUnit tests
 *
 * @author Shane Isbell
 * @goal test
 * @phase test
 * @description Runs NUnit tests
 */
public class TesterMojo
extends AbstractMojo
{

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
     * @component
     */
    private AssemblyResolver assemblyResolver;

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

    private String getExecutableFor( Vendor vendor, String home )
    {
        return !( nunitHome == null || nunitHome.equals( "" ) ) ? nunitHome + File.separator + "bin" + File.separator
            + "nunit-console" : "nunit-console";
    }

    private List<String> getCommandsFor( Vendor vendor )
    {
        String finalName = project.getBuild().getFinalName();
        List<String> commands = new ArrayList<String>();
        if ( testAssemblyPath.startsWith( "/" ) )// nunit-console thinks *nix file format /home/user/ is an option
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
        
        commands.add( "/xml:" + nUnitXmlFilePath.getAbsolutePath() );

        commands.add( "/output:" + nUnitResultOutputPath.getAbsolutePath() );
        commands.add( "/err:" + nUnitResultErrorOutputPath.getAbsolutePath() );

        commands.add( "/labels" );

        if ( xmlConsole )
        {
            commands.add( "/xmlConsole" );
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

        if ( localRepository == null )
        {
            localRepository = new File( System.getProperty( "user.home" ), ".m2/repository" );
        }

        try
        {
            assemblyResolver.resolveTransitivelyFor( project, project.getDependencies(),
                                                     project.getRemoteArtifactRepositories(), localRepository, true );
        }
        catch ( IOException e )
        {
            throw new MojoExecutionException( e.getMessage() );
        }

        List<Artifact> nunitLibs = new ArrayList<Artifact>();
        Set<Artifact> artifacts = project.getDependencyArtifacts();

        for ( Artifact artifact : artifacts )
        {
            if ( artifact.getType().startsWith( "gac" ) )
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
                    FileUtils.copyFileToDirectory( artifact.getFile(), new File( testAssemblyPath ) );
                }
                catch ( IOException e )
                {
                    throw new MojoExecutionException( "NPANDAY-1100-002: Artifact = " + artifact.toString(), e );
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

        List<String> commands = getCommandsFor( null );
        getLog().debug( "NPANDAY-1100-008: " + commands.toString() );

        // pretty print nunit logs
        getLog().info( System.getProperty( "line.separator" ) );

        CommandExecutor commandExecutor = CommandExecutor.Factory.createDefaultCommmandExecutor();
        try
        {
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
            commandExecutor.executeCommand( getExecutableFor( null, null ), commands );
        }
        catch ( ExecutionException e )
        {
            String line = System.getProperty( "line.separator" );
            throw new MojoFailureException( "NPANDAY-1100-007: There are test failures." + line + line + e.getMessage() );
        }
    }
}
