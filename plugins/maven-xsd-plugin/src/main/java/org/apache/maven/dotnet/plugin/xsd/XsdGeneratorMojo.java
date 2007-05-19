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
package org.apache.maven.dotnet.plugin.xsd;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.FileUtils;

import java.util.List;
import java.util.ArrayList;
import java.io.File;
import java.io.IOException;

import org.apache.maven.dotnet.executable.ExecutionException;
import org.apache.maven.dotnet.PlatformUnsupportedException;

/**
 * Generates XSD class.
 *
 * @author Shane Isbell
 * @goal xsd
 * @phase process-sources
 */

public class XsdGeneratorMojo
    extends AbstractMojo
{
    /**
     * The directory to place the generated binding classes.
     *
     * @parameter expression="${outputDirectory}" default-value = "${project.build.directory}${file.separator}build-sources"
     * @required
     */
    private String outputDirectory;

    /**
     * @parameter expression="${xsdFile}"
     * @required
     */
    private String xsdFile;

    /**
     * Generate either classes or dataset.
     *
     * @parameter expression="${generate}" default-value="classes"
     */
    private String generate;

    /**
     * Specifies the element(s) in the schema to generate code for. Not supported for MONO.
     *
     * @parameter expression="${elements}"
     */
    private ArrayList<String> elements;

    /**
     * Generates the INotifyPropertyChanged interface to enable data binding. Not supported for MONO.
     *
     * @parameter expression="${enableDataBinding}" default-value="false"
     */
    private boolean enableDataBinding;

    /**
     * Generates fields instead of properties. Not supported for MONO.
     *
     * @parameter expression = "${fields}" default-value="false"
     */
    private boolean fields;

    /**
     * Programming language to use.
     *
     * @parameter expression="${language}" default-value="CS"
     */
    private String language;

    /**
     * Runtime namespace for the generated types.
     *
     * @parameter expression="${namespace}" default-value="${project.groupId}"
     */
    private String namespace;

    /**
     * Generates explicit order identifiers on all particle members. Not supported for MONO.
     *
     * @parameter expression = "${order}" default-value = "false"
     */
    private boolean order;

    /**
     * Specifies the URI for the elements in the schema to generate code for.
     */
    private String uri;

    /**
     * Tells the xsd plugin to ignore options not appropriate to the xsd vendor.
     *
     * @parameter expresion ="${ignoreUnusedOptions}" default-value="false"
     */
    private boolean ignoreUnusedOptions;

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
     * The Vendor for the executable. Supports MONO and MICROSOFT.
     *
     * @parameter expression="${vendor}"
     */
    private String vendor;

    /**
     * @parameter expression = "${frameworkVersion}"  default-value = "2.0.50727"
     */
    private String frameworkVersion;

    /**
     * The profile that the executable should use.
     *
     * @parameter expression = "${profile}" default-value = "XSD:SCHEMA"
     */
    private String profile;

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
        try
        {
            nmavenRegistry.createRepositoryRegistry();
        }
        catch ( IOException e )
        {
            throw new MojoExecutionException(
                "NMAVEN-1400-0032 Failed to create the repository registry for this plugin", e );
        }

        FileUtils.mkdir( outputDirectory );
        try
        {
            netExecutableFactory.getNetExecutableFor( vendor, frameworkVersion, profile, getCommands(),
                                                      netHome ).execute();
        }
        catch ( ExecutionException e )
        {
            throw new MojoExecutionException( "NMAVEN-1400-000: Unable to execute xsd: Vendor " + vendor +
                ", frameworkVersion = " + frameworkVersion + ", Profile = " + profile, e );
        }
        catch ( PlatformUnsupportedException e )
        {
            throw new MojoExecutionException( "NMAVEN-1400-001: Platform Unsupported: Vendor " + vendor +
                ", frameworkVersion = " + frameworkVersion + ", Profile = " + profile, e );
        }
    }

    public List<String> getCommands()
        throws MojoExecutionException
    {
        List<String> commands = new ArrayList<String>();
        commands.add( new File( xsdFile.trim() ).getAbsolutePath() );
        commands.add( "/o:" + outputDirectory );
        commands.add( "/" + generate.trim() );
        commands.add( "/language:" + language.trim() );
        commands.add( "/namespace:" + namespace.trim() );
        if ( !isEmpty( uri ) )
        {
            commands.add( "/uri:" + uri );
        }
        if ( enableDataBinding )
        {
            commands.add( "/enableDataBinding" );
        }
        if ( fields )
        {
            commands.add( "/fields" );
        }
        if ( order )
        {
            commands.add( "/order" );
        }
        if ( elements != null )
        {
            for ( String element : elements )
            {
                commands.add( "/element:" + element );
            }
        }
        // commands.add("/nologo");
        return commands;
    }

    private boolean isEmpty( String value )
    {
        return ( value == null || value.trim().equals( "" ) );
    }
}
