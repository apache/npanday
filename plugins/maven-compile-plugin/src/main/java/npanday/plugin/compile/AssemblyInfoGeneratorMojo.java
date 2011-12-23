package npanday.plugin.compile;

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

import npanday.ArtifactTypeHelper;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.FileUtils;

import java.io.IOException;
import java.io.File;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import npanday.InitializationException;
import npanday.PlatformUnsupportedException;
import npanday.vendor.*;
import npanday.assembler.AssemblerContext;
import npanday.assembler.AssemblyInfoMarshaller;
import npanday.assembler.AssemblyInfoException;
import npanday.assembler.AssemblyInfo;

/**
 * Generates an AssemblyInfo.* class based on information within the pom file.
 *
 * @author Shane Isbell
 * @goal generate-assembly-info
 * @phase generate-sources
 * @description Generates an AssemblyInfo.* class based on information within the pom file.
 */
public class AssemblyInfoGeneratorMojo
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
     * The framework version to compile under: 1.1, 2.0, 3.0
     *
     * @parameter expression = "${frameworkVersion}"
     */
    private String frameworkVersion;

    /**
     * The vendor version to compile under. For Microsoft, this will be the same as the framework version.
     *
     * @parameter expression = "${vendorVersion}"
     */
    private String vendorVersion;

    /**
     * .NET Language. The default value is <code>C_SHARP</code>. Not case or white-space sensitive.
     *
     * @parameter expression="${language}" default-value = "C_SHARP"
     * @required
     */
    private String language;

    /**
     * The vendor for the compiler: MICROSOFT, MONO, DOTGNU. Not case or white-space sensitive.
     *
     * @parameter expression="${vendor}"
     */
    private String vendor;

    /**
     * Specify a strong name key file.
     *
     * @parameter expression = "${keyfile}"
     */
    private File keyfile;

    /**
     * Specifies a strong name key container. (not currently supported)
     *
     * @parameter expression = "${keycontainer}"
     */
    private String keycontainer;

    /**
     * Source directory containing the copied class files.
     *
     * @parameter expression = "${sourceDirectory}" default-value="${project.build.sourceDirectory}"
     * @required
     */
    private String sourceDirectory;

    /**
     * @component
     */ 
    private AssemblerContext assemblerContext;

    /**
     * @component
     */
    private npanday.vendor.StateMachineProcessor stateMachineProcessor;

    /**
     * @parameter
     */
    protected Map assemblyInfo = new HashMap();

    /**
     * If an AssemblyInfo file exists in the source directory, then this method will not generate an AssemblyInfo.
     *
     * @throws MojoExecutionException
     */
    public void execute()
        throws MojoExecutionException
    {
        long startTime = System.currentTimeMillis();

        if ( ArtifactTypeHelper.isDotnetModule( project.getArtifact().getType() ))
        {
            return;
        }

        File srcFile = new File( sourceDirectory );
        if ( srcFile.exists() )
        {
            try
            {
                List files = FileUtils.getFiles( srcFile, "**/AssemblyInfo.*", null );
                if ( files.size() != 0 )
                {
                    getLog().info( "NPANDAY-902-001: Found AssemblyInfo file(s), so will not generate one" );
                    return;
                }
            }
            catch ( IOException e )
            {
                throw new MojoExecutionException(
                    "NPANDAY-902-004: Could not determine whether an AssemblyInfo file exists", e );
            }
        }
        getLog().info( "NPANDAY-902-000: Generating Assembly Info: Language = " + language.trim() );

        //TODO: Investigate the affect of not setting isDefault and profile. In the case of executables, this is
        //managed by the framework. I intended to keep vendor info and state machine processor out of the
        // Mojos. Unable to do so for this case. Look at new API.
        VendorInfo vendorInfo = VendorInfo.Factory.createDefaultVendorInfo();
        try
        {
            vendorInfo.setFrameworkVersion( frameworkVersion );
            if ( vendor != null )
            {
                vendorInfo.setVendor( VendorFactory.createVendorFromName( vendor ) );
            }
            vendorInfo.setVendorVersion( vendorVersion );
        }
        catch ( VendorUnsupportedException e )
        {
            throw new MojoExecutionException( "NPANDAY-902-007: Vendor not supported: Vendor = " + vendor, e);
        }

        AssemblyInfo assemblyInfo = assemblerContext.getAssemblyInfo();
        assemblyInfo.setCustomStringAttributes(this.assemblyInfo);

        try
        {
            stateMachineProcessor.process( vendorInfo );
        }
        catch ( npanday.vendor.IllegalStateException e )
        {
            throw new MojoExecutionException(
                "NPANDAY-902-008: Illegal state of vendor info: Message =  " + e.getMessage(), e);
        }

        if ( vendorInfo.getVendor().equals( Vendor.MICROSOFT ) && vendorInfo.getVendorVersion().equals( "1.1.4322" ) )
        {
            assemblyInfo.setKeyFile( keyfile );
            assemblyInfo.setKeyName( keycontainer );
        }
        try
        {
            AssemblyInfoMarshaller marshaller = assemblerContext.getAssemblyInfoMarshallerFor( language.trim() );
            marshaller.marshal( assemblyInfo, project, null );
        }
        catch ( IOException e )
        {
            throw new MojoExecutionException( "NPANDAY-902-002: Problem generating assembly info class", e );
        }
        catch ( AssemblyInfoException e )
        {
            throw new MojoExecutionException( "NPANDAY-902-005: Problem generating assembly info class", e );
        }

        long endTime = System.currentTimeMillis();
        getLog().info( "Mojo Execution Time = " + (endTime - startTime));
    }
}
