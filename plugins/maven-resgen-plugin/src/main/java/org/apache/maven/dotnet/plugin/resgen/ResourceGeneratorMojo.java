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
package org.apache.maven.dotnet.plugin.resgen;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.FileUtils;
import org.apache.maven.dotnet.executable.ExecutionException;
import org.apache.maven.dotnet.PlatformUnsupportedException;
import org.apache.maven.dotnet.vendor.VendorInfo;
import org.apache.maven.dotnet.vendor.VendorFactory;
import org.apache.maven.dotnet.artifact.ArtifactContext;

import java.util.ArrayList;
import java.util.List;
import java.io.File;
import java.io.IOException;

/**
 * Generates resources
 *
 * @author Shane Isbell
 * @goal generate
 * @phase process-resources
 */
public class ResourceGeneratorMojo
    extends AbstractMojo
{

    /**
     * @parameter expression="${settings.localRepository}"
     */
    private File localRepository;

    /**
     * The maven project.
     *
     * @parameter expression="${project}"
     * @required
     */
    private MavenProject project;

    /**
     * The home directory of your .NET SDK.
     *
     * @parameter expression="${netHome}"
     */
    private File netHome;

    /**
     * The Vendor for the executable. Supports MONO and MICROSOFT: the default value is <code>MICROSOFT</code>. Not
     * case or white-space sensitive.
     *
     * @parameter expression="${vendor}"
     */
    private String vendor;

    /**
     * @parameter expression = "${frameworkVersion}"
     */
    private String frameworkVersion;

    /**
     * @parameter expression = "${vendorVersion}"
     */
    private String vendorVersion;

    /**
     * @component
     */
    private ArtifactContext artifactContext;

    /**
     * @component
     */
    private org.apache.maven.dotnet.executable.NetExecutableFactory netExecutableFactory;

    /**
     * @component
     */
    private org.apache.maven.dotnet.NMavenRepositoryRegistry nmavenRegistry;

    public void execute()
        throws MojoExecutionException
    {

        if ( System.getProperty( "bootstrap" ) != null )
        {
            return;
        }

        if ( localRepository == null )
        {
            localRepository = new File( System.getProperty( "user.home" ), ".m2/repository" );
        }

        artifactContext.init( project, project.getRemoteArtifactRepositories(), localRepository );
        /*
        * We should do this check at a lower level (to keep consistent behavior with the nmaven-settings.xml)
        * but we do not currently support the execution of specific plugins based on vendor info.
        */
        if ( vendor != null && vendor.equals( "DotGNU" ) )
        {
            getLog().info( "NMAVEN-1501-005: Unsupported Plugin" );
            return;
        }

        File sourceDirectory = new File( project.getBuild().getDirectory(), "/assembly-resources/resgen" );
        if ( !sourceDirectory.exists() )
        {
            return;
        }

        try
        {
            nmavenRegistry.createRepositoryRegistry();
        }
        catch ( IOException e )
        {
            throw new MojoExecutionException(
                "NMAVEN-1501-004: Failed to create the repository registry for this plugin", e );
        }

        //resx.exe
        List<String> commands = new ArrayList<String>();
        commands.add( sourceDirectory.getAbsolutePath() );
        commands.add( project.getBuild().getDirectory() + File.separator + "assembly-resources" + File.separator +
            project.getArtifactId() + ".resx" );
        try
        {
            VendorInfo vendorInfo = VendorInfo.Factory.createDefaultVendorInfo();
            if ( vendor != null )
            {
                vendorInfo.setVendor( VendorFactory.createVendorFromName( vendor ) );
            }
            vendorInfo.setFrameworkVersion( frameworkVersion );
            vendorInfo.setVendorVersion( vendorVersion );
            netExecutableFactory.getNetExecutableFromRepository( "NMaven.Plugins", "NMaven.Plugin.Resx", vendorInfo,
                                                                 localRepository, commands, false ).execute();
        }
        catch ( PlatformUnsupportedException e )
        {
            throw new MojoExecutionException( "", e );
        }
        catch ( ExecutionException e )
        {
            throw new MojoExecutionException( "", e );
        }

        //resgen.exe
        FileUtils.mkdir(
            project.getBuild().getDirectory() + File.separator + "assembly-resources" + File.separator + "resource" );

        try
        {
            netExecutableFactory.getNetExecutableFor( vendor, frameworkVersion, "RESGEN", getCommands(),
                                                      netHome ).execute();
        }
        catch ( ExecutionException e )
        {
            throw new MojoExecutionException( "NMAVEN-1501-002: Unable to execute resgen: Vendor = " + vendor +
                ", frameworkVersion = " + frameworkVersion, e );
        }
        catch ( PlatformUnsupportedException e )
        {
            throw new MojoExecutionException( "NMAVEN-1501-003: Platform Unsupported", e );
        }
    }

    public List<String> getCommands()
        throws MojoExecutionException
    {
        List<String> commands = new ArrayList<String>();
        commands.add( project.getBuild().getDirectory() + File.separator + "assembly-resources" + File.separator +
            project.getArtifactId() + ".resx" );
        commands.add( project.getBuild().getDirectory() + File.separator + "assembly-resources" + File.separator +
            "resource" + File.separator + project.getArtifactId() + ".resources" );
        return commands;
    }
}
