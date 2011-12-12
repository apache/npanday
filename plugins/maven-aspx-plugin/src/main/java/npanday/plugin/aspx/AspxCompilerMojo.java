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

import npanday.ArtifactType;
import npanday.PlatformUnsupportedException;
import npanday.executable.ExecutionException;
import npanday.executable.compiler.CompilerConfig;
import npanday.executable.compiler.CompilerExecutable;
import npanday.executable.compiler.CompilerRequirement;
import npanday.registry.RepositoryRegistry;
import npanday.vendor.SettingsException;
import npanday.vendor.SettingsUtil;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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

    private static final String DEFAULT_EXCLUDES = ".svn/**,.references/**,obj/**, target/**, **/*.pdb, **/*.csproj, **/*.vbproj, **/*.suo, **/*.user,pom.xml, **/*.sln,build.log,PrecompiledApp.config,csproj.user,Properties/**,**.releaseBackup,^-?(?:\\d+|\\d{1,3}(?:,\\d{3})+)(?:\\.\\d+)?$/**";

    /**
     * @parameter expression="${npanday.settings}" default-value="${user.home}/.m2"
     */
    private String settingsPath;
    
    /**
     * The maven project.
     *
     * @parameter expression="${project}"
     * @required
     */
    private MavenProject project;

   /**
     * The directory for the compiled web application
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
    
    private File webSourceDirectory;

    /**
     * @parameter expression = "${excludes}"
     */
    private String[] excludes;

    /** 
     * @component
     */
    private RepositoryRegistry repositoryRegistry;

    public void execute()
        throws MojoExecutionException
    {
        long startTime = System.currentTimeMillis();

        try
        {
            SettingsUtil.populateSettingsRepository( repositoryRegistry, settingsPath );
        }
        catch ( SettingsException e )
        {
            throw new MojoExecutionException( "NPANDAY-109-001: Error reading settings from " + settingsPath, e );
        }

        webSourceDirectory = new File( project.getBuild().getSourceDirectory() );

        if ( profileAssemblyPath != null && !profileAssemblyPath.exists() )
        {
            throw new MojoExecutionException( "NPANDAY-900-000: Profile Assembly Path does not exist: Path = " +
                profileAssemblyPath.getAbsolutePath() );
        }

        if ( localRepository == null )
        {
            localRepository = new File( System.getProperty( "user.home" ), ".m2/repository" );
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

        // since asp_compiler doesn't have excludes, will just copy web source to temp directory
        File tmpSourceDir = new File( tmpDir, "src" );
        tmpSourceDir.mkdirs();
        try
        {
            FileUtils.copyDirectoryStructure( webSourceDirectory, tmpSourceDir );
            
            // delete target from temp web source
            FileUtils.deleteDirectory( new File( tmpSourceDir, outputDirectory.getName() ) );
        }
        catch ( IOException e )
        {
            throw new MojoExecutionException( "Unable to copy directory " + webSourceDirectory.getAbsolutePath() + " to " +
                tmpSourceDir.getAbsolutePath(), e );
        }        

        File tmpDestDir = new File( tmpDir, "dest" );
        tmpDestDir.mkdirs();

        CompilerRequirement compilerRequirement = createCompilerRequirement();

        CompilerConfig compilerConfig = createCompilerConfig( tmpSourceDir.getAbsolutePath(), tmpDestDir.getAbsolutePath() );

        try
        {
            CompilerExecutable compilerExecutable =
                netExecutableFactory.getCompilerExecutableFor( compilerRequirement, compilerConfig, project  );

            long startTimeCompile = System.currentTimeMillis();
            compilerExecutable.execute();
            long endTimeCompile = System.currentTimeMillis();

            getLog().info( "NPANDAY-000-000: Compile Time = " + ( endTimeCompile - startTimeCompile ) + " ms" );
            project.getArtifact().setFile( compilerExecutable.getCompiledArtifact() );
        }
        catch ( PlatformUnsupportedException e )
        {
            throw new MojoExecutionException( "NPANDAY-900-005: Unsupported Platform: Language = " + language +
                ", Vendor = " + vendor + ", ArtifactType = " + project.getArtifact().getType(), e );
        }
        catch ( ExecutionException e )
        {
            throw new MojoExecutionException( "NPANDAY-900-006: Unable to Compile: Language = " + language +
                ", Vendor = " + vendor + ", ArtifactType = " + project.getArtifact().getType() + ", Source Directory = " +
                project.getBuild().getSourceDirectory(), e );
        }

        File webappDir = new File( outputDirectory, project.getArtifactId() );
        webappDir.mkdirs();

        try
        {
            /* delete the target folder copied by aspnet compiler */
            /* TODO should be removed since target is deleted */
            //FileUtils.deleteDirectory( new File( tmpDir, outputDirectory.getName() ) );

            // NPANDAY-474
            String combinedExcludes = "";
            if( excludes != null )
            {
                for( int i = 0; i < excludes.length; i++ )
                {
                    combinedExcludes = combinedExcludes + excludes[i] + ",";
                }
            }
            combinedExcludes = combinedExcludes + DEFAULT_EXCLUDES;

            /* keep only the files needed to run the app */
            List<File> allFiles = FileUtils.getFiles( tmpDestDir, "**", null );
            List<File> filesToKeep = FileUtils.getFiles( tmpDestDir, DEFAULT_INCLUDES, combinedExcludes );

            for ( File file : allFiles )
            {
                if ( !filesToKeep.contains( file ) || "App_global.asax.dll".equals(file.getName()) || "App_global.asax.compiled".equals(file.getName()))
                {
                    file.delete();
                    deleteDirectoryIfEmpty( file.getParentFile() );
                }
            }
        }
        catch ( IOException e )
        {
            throw new MojoExecutionException( "Unable to delete unneccessary files in temporary directory " +
                tmpDestDir.getAbsolutePath(), e );
        }

        try
        {
            FileUtils.copyDirectoryStructure( tmpDestDir, webappDir );
        }
        catch ( IOException e )
        {
            throw new MojoExecutionException( "Unable to copy directory " + tmpDestDir.getAbsolutePath() + " to " +
                webappDir.getAbsolutePath(), e );
        }

        try
        {
            String sourceDirectory = webSourceDirectory.getAbsolutePath();
            List<File> fileList = FileUtils.getFiles(new File(sourceDirectory), "**/*.asax", null);
            getLog().debug("copy .asax to target file (temp source folder: " + sourceDirectory +  ")...");
            getLog().debug("copy .asax to target file (temp dest folder: " + tmpDestDir +  ")...");
            getLog().debug("copy .asax to target file (file count: " + fileList.size() +  ")...");

            for ( File file : fileList )
            {
                try
                {
                    String fileName = file.getAbsolutePath().substring( (int) sourceDirectory.length() );
                    FileUtils.copyFile( new File( file.getAbsolutePath() ), new File( webappDir.getAbsolutePath() + fileName ) );
                    getLog().info("Copying " + fileName.substring(1) + " to " + webappDir.getAbsolutePath() );
                }
                catch ( IOException e )
                {
                    throw new MojoExecutionException( "Unable to copy asax file " + file.getAbsolutePath() + " to " +
                            webappDir.getAbsolutePath(), e );
                }
            }
        }
        catch ( IOException e )
        {
            throw new MojoExecutionException( "Unable to retrieve asax file", e );
        }

        // cleanup
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

        //Delete Bin Folder
        String binDir = project.getBuild().getSourceDirectory()+File.separator + "Bin";
        try
        {
            FileUtils.deleteDirectory(binDir);
            getLog().info("Bin folder deleted: " + binDir);
        }
        catch(IOException e)
        {
            getLog().info("Failed to delete Bin folder: " + binDir + " : " + e.getMessage());
        }
    }
    
    private CompilerConfig createCompilerConfig(String source, String destination) throws MojoExecutionException
    {
        CompilerConfig compilerConfig = new CompilerConfig();
        compilerConfig.setLocalRepository( localRepository );
        compilerConfig.setCommands( getCommands( source, destination ) );

        String artifactTypeName = project.getArtifact().getType();
        ArtifactType artifactType = ArtifactType.getArtifactTypeForPackagingName( artifactTypeName );
        if ( artifactType.equals( ArtifactType.NULL ) )
        {
            throw new MojoExecutionException( "NPANDAY-900-002: Unrecognized artifact type: Language = " + language +
                ", Vendor = " + vendor + ", ArtifactType = " + artifactTypeName );
        }
        compilerConfig.setArtifactType( artifactType );

        if (profileAssemblyPath != null){
            compilerConfig.setAssemblyPath( profileAssemblyPath );
        }

        return compilerConfig;
    }
    
    private CompilerRequirement createCompilerRequirement() throws MojoExecutionException
    {
        return new CompilerRequirement(
            vendor, vendorVersion, frameworkVersion, profile,  language);
    }
    
    private List<String> getCommands( String sourceDir, String outputDir )
        throws MojoExecutionException
    {
        if ( parameters == null )
        {
            parameters = new ArrayList<String>();
        }

        parameters.add( "-v" );
        parameters.add( " /" + project.getArtifactId() );
        parameters.add( "-p" );
        parameters.add( sourceDir );
        parameters.add( "-u" );
        parameters.add( "-f" );
        parameters.add( outputDir );
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
