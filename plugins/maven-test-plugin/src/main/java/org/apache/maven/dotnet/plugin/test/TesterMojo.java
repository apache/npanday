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
import org.apache.maven.project.MavenProject;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.resolver.ArtifactResolutionException;
import org.apache.maven.artifact.resolver.ArtifactNotFoundException;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.repository.DefaultArtifactRepository;
import org.codehaus.plexus.util.FileUtils;

import java.util.ArrayList;
import java.util.Set;
import java.util.List;
import java.io.IOException;
import java.io.File;

import org.apache.maven.dotnet.executable.ExecutionException;
import org.apache.maven.dotnet.vendor.Vendor;
import org.apache.maven.dotnet.executable.CommandExecutor;
import org.apache.maven.dotnet.artifact.AssemblyRepositoryLayout;
import org.apache.maven.dotnet.artifact.AssemblyResolver;

/**
 * Runs NUnit tests
 *
 * @author Shane Isbell
 * @goal test
 * @phase test
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
     * The home of nunit. Use this if you 1) have not added nunit to your path and you only have MS installed;
     * or 2) have mono installed and want to use another version of nunit.
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
     * @parameter expression = "${reportsDirectory}" default-value = "${project.build.directory}${file.separator}nunit-reports"
     */
    private String reportsDirectory;

    /**
     * Test Assembly Location
     *
     * @parameter expression = "${testAssemblyPath}" default-value = "${project.build.directory}${file.separator}test-assemblies"
     */
    private String testAssemblyPath;

    /**
     * @component
     */
    private AssemblyResolver assemblyResolver;

    /**
     * @parameter expression="${settings.localRepository}"
     * @readonly
     */
    private String localRepository;


    private String getExecutableFor( Vendor vendor, String home )
    {
        return !( nunitHome == null || nunitHome.equals( "" ) ) ? nunitHome + File.separator + "bin" + File.separator +
            "nunit-console" : "nunit-console";
    }

    private List<String> getCommandsFor( Vendor vendor )
    {
        String finalName = project.getBuild().getFinalName();
        List<String> commands = new ArrayList<String>();
        if(testAssemblyPath.startsWith( "/"))//nunit-console thinks *nix file format /home/user/ is an option due to / and fails.
        {
            testAssemblyPath = "/" + testAssemblyPath;            
        }

        commands.add( testAssemblyPath + File.separator + project.getArtifactId() + "-test.dll" );
        commands.add( "/xml:" + reportsDirectory + File.separator + "TEST-" + finalName + ".xml" );

        commands.add( "/output:" + reportsDirectory + File.separator + "TEST-" + finalName + "-RESULTS.txt" );
        commands.add( "/err:" + reportsDirectory + File.separator + "TEST-" + finalName + "-ERROR.txt" );
        commands.add( "/labels" );

        if ( xmlConsole )
        {
            commands.add( "/xmlConsole" );
        }
        return commands;
    }

    public void execute()
        throws MojoExecutionException
    {
        String skipTests = System.getProperty( "maven.test.skip" );
        if ( ( skipTests != null && skipTests.equalsIgnoreCase( "true" ) ) || skipTest )
        {
            getLog().warn( "NMAVEN-1100-000: Unit tests have been disabled." );
            return;
        }
        String testFileName =
            project.getBuild().getDirectory() + File.separator + project.getArtifactId() + "-test.dll";

        if ( !( new File( testFileName ).exists() ) )
        {
            getLog().info( "NMAVEN-1100-001: No Unit Tests" );
            return;
        }

        if ( localRepository == null )
        {
            localRepository = new File( System.getProperty( "user.home" ), ".m2/repository" ).getAbsolutePath();
        }

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

        List<Artifact> nunitLibs = new ArrayList<Artifact>();
        Set<Artifact> artifacts = project.getDependencyArtifacts();

        for ( Artifact artifact : artifacts )
        {
            if(artifact.getType().startsWith( "gac"))
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
                    throw new MojoExecutionException( "NMAVEN-1100-002: Artifact = " + artifact.toString(), e );
                }
            }
        }

        if ( nunitLibs.size() == 0 )
        {
            throw new MojoExecutionException( "NMAVEN-1100-003: Could not find any nunit libraries." );
        }

        //Copy Main Artifact
        try
        {
            if ( project.getArtifact() != null && project.getArtifact().getFile() != null &&
                project.getArtifact().getFile().exists() )
            {
                FileUtils.copyFileToDirectory( project.getArtifact().getFile(), new File( testAssemblyPath ) );
            }
        }
        catch ( IOException e )
        {
            throw new MojoExecutionException( "NMAVEN-1100-004: Unable to copy library to target directory: ", e );
        }
        //TODO: Check timestamps
        //Copy Test Artifact
        try
        {
            FileUtils.copyFileToDirectory( new File( testFileName ), new File( testAssemblyPath ) );
        }
        catch ( IOException e )
        {
            throw new MojoExecutionException( "NMAVEN-1100-005: Unable to copy library to target directory: ", e );
        }

        //Copy  NUnit Dependencies
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
                    "NMAVEN-1100-006: Unable to copy nunit library to target directory: File = " +
                        file.getAbsolutePath(), e );
            }
        }

        FileUtils.mkdir( reportsDirectory );

        List<String> commands = getCommandsFor( null );
        getLog().debug( "NMAVEN-1100-008: " + commands.toString() );
        CommandExecutor commandExecutor = CommandExecutor.Factory.createDefaultCommmandExecutor();
        try
        {
            commandExecutor.executeCommand( getExecutableFor( null, null ), commands );
        }
        catch ( ExecutionException e )
        {
            throw new MojoExecutionException( "NMAVEN-1100-007", e );
        }
    }
}
