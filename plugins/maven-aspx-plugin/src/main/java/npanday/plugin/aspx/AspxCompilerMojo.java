package npanday.plugin.aspx;

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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import npanday.ArtifactType;
import npanday.PlatformUnsupportedException;
import npanday.executable.ExecutionException;
import npanday.executable.compiler.CompilerConfig;
import npanday.executable.compiler.CompilerExecutable;
import npanday.executable.compiler.CompilerRequirement;
import npanday.vendor.VendorFactory;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.FileUtils;

/**
 * Maven Mojo for precompiling ASPx files
 * 
 * @goal compile
 * @phase compile
 * @description Maven Mojo for precompiling ASPx files
 */
public class AspxCompilerMojo
    extends AbstractMojo
{
    private static final String DEFAULT_INCLUDES = "**"; //any extension can be made for request handler in ASPX

    private static final String DEFAULT_EXCLUDES = "obj/**, target/**, **/*.pdb, **/*.csproj, **/*.vbproj, **/*.suo, **/*.user,pom.xml, **/*.sln,build.log,PrecompiledApp.config,csproj.user,Properties/**,**.releaseBackup";

    /**
     * The maven project.
     * 
     * @parameter expression="${project}"
     * @required
     */
    private MavenProject project;

   /**
     * The directory for the compilated web application
     *
     * @parameter  expression = "${outputDirectory}" default-value = "${project.build.directory}"
     * @required
     */
    private File outputDirectory;

    /**
     * This over-rides the defaultAssemblyPath for the compiler plugin.
     * 
     * @parameter expression = "${profileAssemblyPath}
     */
    private File profileAssemblyPath;

    /**
     * The location of the local Maven repository.
     * 
     * @parameter expression="${settings.localRepository}"
     */
    private File localRepository;

    /**
     * .NET Language. The default value is <code>ASPX</code>. Not case or white-space sensitive.
     * 
     * @parameter expression="${language}" default-value = "ASP"
     * @required
     */
    private String language;

    /**
     * The framework version to compile under: 1.1, 2.0, 3.0
     * 
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
     * The home directory of your .NET SDK.
     * 
     * @parameter expression="${netHome}"
     */
    private File netHome;

    /**
     * Additional compiler commands
     * 
     * @parameter expression = "${parameters}"
     */
    private ArrayList<String> parameters;

    /**
     * @component
     */
    private npanday.executable.NetExecutableFactory netExecutableFactory;

    /**
     * @component
     */
    private npanday.NPandayRepositoryRegistry npandayRegistry;

    public void execute()
        throws MojoExecutionException
    {
       long startTime = System.currentTimeMillis();


        // delete the target folder first to avoid being compiled, by aspnet_compiler
        if(outputDirectory.exists())
        {
            try
            {
                FileUtils.deleteDirectory(outputDirectory);
            }
            catch (IOException e)
            {

            }
        }


        if ( profileAssemblyPath != null && !profileAssemblyPath.exists() )
        {
            throw new MojoExecutionException( "NPANDAY-900-000: Profile Assembly Path does not exist: Path = " +
                profileAssemblyPath.getAbsolutePath() );
        }




        // Config
        CompilerConfig compilerConfig = (CompilerConfig) CompilerConfig.Factory.createDefaultExecutableConfig();
        compilerConfig.setLocalRepository( localRepository );
        if ( localRepository == null )
        {
            localRepository = new File( System.getProperty( "user.home" ), ".m2/repository" );
        }

        // Requirement
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
            throw new MojoExecutionException( "NPANDAY-900-001: Unknown Vendor: Vendor = " + vendor, e );
        }

        if ( parameters == null )
        {
            parameters = new ArrayList<String>();
        }

        File tmpDir;
        try
        {
            tmpDir = getTempDirectory();
        }
        catch ( IOException e )
        {
            throw new MojoExecutionException( "Unable to create temporary directory", e );
        }

        compilerConfig.setCommands( getCommands( tmpDir ) );

        String artifactTypeName = project.getArtifact().getType();
        ArtifactType artifactType = ArtifactType.getArtifactTypeForPackagingName( artifactTypeName );
        if ( artifactType.equals( ArtifactType.NULL ) )
        {
            throw new MojoExecutionException( "NPANDAY-900-002: Unrecognized artifact type: Language = " + language +
                ", Vendor = " + vendor + ", ArtifactType = " + artifactTypeName );
        }
        compilerConfig.setArtifactType( artifactType );

        try
        {
			CompilerExecutable compilerExecutable =
                netExecutableFactory.getCompilerExecutableFor( compilerRequirement, compilerConfig, project,
                                                               profileAssemblyPath );

            long startTimeCompile = System.currentTimeMillis();
            compilerExecutable.execute();
            long endTimeCompile = System.currentTimeMillis();

            getLog().info( "NPANDAY-000-000: Compile Time = " + ( endTimeCompile - startTimeCompile ) + " ms" );
            project.getArtifact().setFile( compilerExecutable.getCompiledArtifact() );
        }
        catch ( PlatformUnsupportedException e )
        {
            throw new MojoExecutionException( "NPANDAY-900-005: Unsupported Platform: Language = " + language +
                ", Vendor = " + vendor + ", ArtifactType = " + artifactTypeName, e );
        }
        catch ( ExecutionException e )
        {
            throw new MojoExecutionException( "NPANDAY-900-006: Unable to Compile: Language = " + language +
                ", Vendor = " + vendor + ", ArtifactType = " + artifactTypeName + ", Source Directory = " +
                project.getBuild().getSourceDirectory(), e );
        }

        File webappDir = new File( outputDirectory, project.getArtifactId() );
        webappDir.mkdirs();

        try
        {
            /* delete the target folder copied by aspnet compiler */
            FileUtils.deleteDirectory( new File( tmpDir, outputDirectory.getName() ) );

            /* keep only the files needed to run the app */
            List<File> allFiles = FileUtils.getFiles( tmpDir, "**", null );
            List<File> filesToKeep = FileUtils.getFiles( tmpDir, DEFAULT_INCLUDES, DEFAULT_EXCLUDES );

            for ( File file : allFiles )
            {
                if ( !filesToKeep.contains( file ) )
                {
                    file.delete();
                    deleteDirectoryIfEmpty( file.getParentFile() );
                }
            }
        }
        catch ( IOException e )
        {
            throw new MojoExecutionException( "Unable to delete unneccessary files in temporary directory " +
                tmpDir.getAbsolutePath(), e );
        }

        try
        {
            FileUtils.copyDirectoryStructure( tmpDir, webappDir );
        }
        catch ( IOException e )
        {
            throw new MojoExecutionException( "Unable to copy directory " + tmpDir.getAbsolutePath() + " to " +
                webappDir.getAbsolutePath(), e );
        }

        // try
        // {
        // FileUtils.copyDirectory( tmpDir, webappDir, null, DEFAULT_EXCLUDES );
        // }
        // catch ( IOException e )
        // {
        // throw new MojoExecutionException( "Unable to copy compiled files to target folder", e );
        // }

        try
        {
            FileUtils.deleteDirectory( tmpDir );
        }
        catch ( IOException e )
        {
            getLog().info( "Unable to delete temporary directory " + tmpDir );
            // temporary files can't be deleted, ignore
        }

        long endTime = System.currentTimeMillis();
        getLog().info( "Mojo Execution Time = " + ( endTime - startTime ) );

        // TODO: remove hardcoded values
        String targetFile =
            project.getBuild().getDirectory() + File.separator + project.getArtifactId() + "-" + project.getVersion() +
                "-dist" + ".zip";
        getLog().info( "NPANDAY-000-000: Setting the target file: " + targetFile );
        project.getArtifact().setFile( new File( targetFile ) );

    }

    private List<String> getCommands( File tmpDir )
        throws MojoExecutionException
    {
        if ( parameters == null )
        {
            parameters = new ArrayList<String>();
        }

        parameters.add( "-v" );
        parameters.add( " /" + project.getArtifactId() );
        parameters.add( "-p" );
        parameters.add( project.getBuild().getSourceDirectory() );
        parameters.add( "-u" );
        parameters.add( "-f" );
        parameters.add( tmpDir.getAbsolutePath() );
        parameters.add( "-nologo" );
		parameters.add( "-fixednames" );

        return parameters;
    }

    private File getTempDirectory()
        throws IOException
    {
        File tempFile = File.createTempFile( "maven-aspx-plugin-", "" );
        File tmpDir = new File( tempFile.getParentFile(), tempFile.getName() );
        tempFile.delete();
        tmpDir.mkdir();
        return tmpDir;
    }

    private void deleteDirectoryIfEmpty( File directory )
    {
        if ( directory.listFiles().length == 0 )
        {
            directory.delete();
            deleteDirectoryIfEmpty( directory.getParentFile() );
        }
    }
}
