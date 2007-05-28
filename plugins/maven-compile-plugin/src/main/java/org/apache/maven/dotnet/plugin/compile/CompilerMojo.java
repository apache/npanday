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
package org.apache.maven.dotnet.plugin.compile;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.AbstractMojo;

import org.apache.maven.project.MavenProject;
import org.apache.maven.dotnet.PlatformUnsupportedException;
import org.apache.maven.dotnet.artifact.ArtifactType;
import org.apache.maven.dotnet.executable.ExecutionException;
import org.apache.maven.dotnet.vendor.VendorFactory;
import org.apache.maven.dotnet.executable.compiler.*;
import org.apache.maven.artifact.Artifact;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.DirectoryScanner;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.Date;
import java.util.Calendar;
import java.io.File;
import java.io.FilenameFilter;

/**
 * Maven Mojo for compiling Class files to the .NET Intermediate Language.
 * To use a specific vendor (MICROSOFT/MONO) or language, the compiler/language must be previously installed AND
 * configured through the plugin-compiler.xml file: otherwise the Mojo either will throw a MojoExecutionException
 * telling you that the platform is not supported (occurs if entry is not in plugin-compilers.xml, regardless of
 * whether the compiler/language is installed) or will attempt to execute the compiler and fail (occurs if entry is in
 * plugin-compilers.xml and the compiler/language is not installed).
 *
 * @author Shane Isbell
 * @goal compile
 * @phase compile
 * @description Maven Mojo for compiling Class files to the .NET Intermediate Language
 */
public final class CompilerMojo
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
     * @parameter expression="${settings.localRepository}"
     */
    private File localRepository;

    /**
     * Additional compiler commands
     *
     * @parameter expression = "${parameters}"
     */
    private ArrayList<String> parameters;

    /**
     * Delay-sign the assembly using only the public portion of the strong name key. (not currently supported)
     */
    private boolean delaysign;

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
     * Limit the platforms this code can run on. (not currently supported)
     *
     * @parameter expression = "${platform} default-value = "anycpu"
     */
    private String platform;

    /**
     * @parameter expression = "${frameworkVersion}"
     */
    private String frameworkVersion;

    /**
     * The profile that the compiler should use to compile classes: FULL, COMPACT, (or a custom one specified in a
     * compiler-plugins.xml).
     *
     * @parameter expression = "${profile}" default-value = "FULL"
     */
    private String profile;

    /**
     * .NET Language. The default value is <code>C_SHARP</code>. Not case or white-space sensitive.
     *
     * @parameter expression="${language}" default-value = "C_SHARP"
     * @required
     */
    private String language;

    /**
     * The Vendor for the Compiler. Not
     * case or white-space sensitive.
     *
     * @parameter expression="${vendor}"
     */
    private String vendor;

    /**
     * This over-rides the defaultAssemblyPath for the compiler plugin.
     *
     * @parameter expression = "${profileAssemblyPath}
     */
    private File profileAssemblyPath;

    /**
     * @parameter expression = "${vendorVersion}"
     */
    private String vendorVersion;

    /**
     * @parameter expression = "${isDebug}" default-value="false"
     */
    private boolean isDebug;

    /**
     * @component
     */
    private org.apache.maven.dotnet.executable.NetExecutableFactory netExecutableFactory;

    /**
     * @parameter expression="${project.file}"
     * @required
     * @readonly
     */
    private File pomFile;


    /**
     * Compiles the class files.
     *
     * @throws MojoExecutionException thrown if MOJO is unable to compile the class files or if the environment is not
     *                                properly set.
     */
    public void execute()
        throws MojoExecutionException
    {
        long startTime = System.currentTimeMillis();

        if ( profileAssemblyPath != null && !profileAssemblyPath.exists() )
        {
            throw new MojoExecutionException( "NMAVEN-900-000: Profile Assembly Path does not exist: Path = " +
                profileAssemblyPath.getAbsolutePath() );
        }

        if ( localRepository == null )
        {
            localRepository = new File( System.getProperty( "user.home" ), ".m2/repository" );
        }

        //Requirement
        CompilerRequirement compilerRequirement = CompilerRequirement.Factory.createDefaultCompilerRequirement();
        compilerRequirement.setLanguage( language );
        compilerRequirement.setFrameworkVersion( frameworkVersion );
        compilerRequirement.setProfile( profile );
        compilerRequirement.setVendorVersion( vendorVersion );
        try
        {
            if ( vendor != null )
            {
                compilerRequirement.setVendor( VendorFactory.createVendorFromName( vendor ) );
            }
        }
        catch ( PlatformUnsupportedException e )
        {
            throw new MojoExecutionException( "NMAVEN-900-001: Unknown Vendor: Vendor = " + vendor, e );
        }

        //Config
        CompilerConfig compilerConfig = (CompilerConfig) CompilerConfig.Factory.createDefaultExecutableConfig();
        compilerConfig.setLocalRepository( localRepository );
        if ( parameters == null )
        {
            parameters = new ArrayList<String>();
        }
        if ( isDebug )
        {
            parameters.add( "/debug+" );
        }

        compilerConfig.setCommands( parameters );

        String artifactTypeName = project.getArtifact().getType();
        ArtifactType artifactType = ArtifactType.getArtifactTypeForPackagingName( artifactTypeName );
        if ( artifactType.equals( ArtifactType.NULL ) )
        {
            throw new MojoExecutionException( "NMAVEN-900-002: Unrecognized artifact type: Language = " + language +
                ", Vendor = " + vendor + ", ArtifactType = " + artifactTypeName );
        }
        compilerConfig.setArtifactType( artifactType );

        if ( keyfile != null )
        {
            KeyInfo keyInfo = KeyInfo.Factory.createDefaultKeyInfo();
            keyInfo.setKeyFileUri( keyfile.getAbsolutePath() );
            compilerConfig.setKeyInfo( keyInfo );
        }

        try
        {
            CompilerExecutable compilerExecutable = netExecutableFactory.getCompilerExecutableFor( compilerRequirement,
                                                                                                   compilerConfig,
                                                                                                   project,
                                                                                                   profileAssemblyPath );
            Boolean sourceFilesUpToDate = (Boolean) super.getPluginContext().get( "SOURCE_FILES_UP_TO_DATE" );
            if ( ( ( sourceFilesUpToDate == null ) || sourceFilesUpToDate ) &&
                System.getProperty( "forceCompile" ) == null && compilerExecutable.getCompiledArtifact() != null &&
                compilerExecutable.getCompiledArtifact().exists() )
            {
                if ( isUpToDateWithPomAndSettingsAndDependencies( compilerExecutable.getCompiledArtifact() ) )
                {
                    getLog().info( "NMAVEN-900-003: Nothing to compile - all classes are up-to-date" );
                    project.getArtifact().setFile( compilerExecutable.getCompiledArtifact() );
                    return;
                }
            }
            long startTimeCompile = System.currentTimeMillis();
            compilerExecutable.execute();
            long endTimeCompile = System.currentTimeMillis();

            getLog().info( "NMAVEN-900-004: Compile Time = " + ( endTimeCompile - startTimeCompile ) + " ms" );
            project.getArtifact().setFile( compilerExecutable.getCompiledArtifact() );
        }
        catch ( PlatformUnsupportedException e )
        {
            throw new MojoExecutionException( "NMAVEN-900-005: Unsupported Platform: Language = " + language +
                ", Vendor = " + vendor + ", ArtifactType = " + artifactTypeName, e );
        }
        catch ( ExecutionException e )
        {
            throw new MojoExecutionException( "NMAVEN-900-006: Unable to Compile: Language = " + language +
                ", Vendor = " + vendor + ", ArtifactType = " + artifactTypeName + ", Source Directory = " +
                project.getBuild().getSourceDirectory(), e );
        }
        long endTime = System.currentTimeMillis();
        getLog().info( "Mojo Execution Time = " + ( endTime - startTime ) );
    }

    private boolean isUpToDateWithPomAndSettingsAndDependencies( File targetFile )
    {
        File settingsFile = new File( localRepository, ".m2/nmaven-settings.xml" );
        Artifact latestDependencyModification =
            this.getLatestDependencyModification( project.getDependencyArtifacts() );

        //TODO: Different parameters from the command line should also cause an update
        //TODO: Change in resource should cause an update
        if ( targetFile.lastModified() < pomFile.lastModified() )
        {
            getLog().info( "NMAVEN-900-007: Project pom has changed. Forcing a recompile." );
            return false;
        }
        else if ( settingsFile.exists() && targetFile.lastModified() < settingsFile.lastModified() )
        {
            getLog().info( "NMAVEN-900-008:Project settings has changed. Forcing a recompile." );
            return false;
        }
        else if ( latestDependencyModification != null &&
            targetFile.lastModified() < latestDependencyModification.getFile().lastModified() )
        {
            getLog().info(
                "NMAVEN-900-009: Detected change in module dependency. Forcing a recompile: Changed Artifact = " +
                    latestDependencyModification );
            return false;
        }
        return true;
    }

    private Artifact getLatestDependencyModification( Set<Artifact> artifacts )
    {
        Artifact lastModArtifact = null;
        for ( Artifact artifact : artifacts )
        {
            if ( lastModArtifact == null && !artifact.getType().startsWith( "gac" ) )
            {
                lastModArtifact = artifact;
            }
            else if ( !artifact.getType().startsWith( "gac" ) &&
                artifact.getFile().lastModified() > lastModArtifact.getFile().lastModified() )
            {
                lastModArtifact = artifact;
            }
        }
        return lastModArtifact;
    }
}