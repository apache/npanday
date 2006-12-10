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
package org.apache.maven.dotnet.artifact.impl;

import org.apache.maven.dotnet.artifact.ArtifactException;
import org.apache.maven.dotnet.artifact.ArtifactContext;
import org.apache.maven.dotnet.artifact.AssemblyRepositoryLayout;
import org.apache.maven.dotnet.artifact.ApplicationConfig;
import org.apache.maven.project.MavenProject;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.metadata.ArtifactMetadata;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.installer.ArtifactInstallationException;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.repository.DefaultArtifactRepository;
import org.apache.maven.model.Model;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.io.xpp3.MavenXpp3Writer;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.IOUtil;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;

import java.io.File;
import java.io.IOException;
import java.io.FileWriter;
import java.io.FileReader;
import java.util.List;
import java.util.ArrayList;

/**
 * Provides an implementation of the <code>ArtifactInstaller</code> interface.
 *
 * @author Shane Isbell
 */
public class ArtifactInstallerImpl
    implements org.apache.maven.dotnet.artifact.ArtifactInstaller, LogEnabled
{

    /**
     * A factory component used for creating artifacts from metadata
     */
    private ArtifactFactory mavenArtifactFactory;

    /**
     * An installer component for installing artifacts into a local Maven repository.
     */
    private org.apache.maven.artifact.installer.ArtifactInstaller mavenInstaller;

    /**
     * The maven project
     */
    private MavenProject project;

    /**
     * Root path of the local Maven repository
     */
    private File localRepository;

    /**
     * The artifact context that constructed this artifact installer. The context is used by the installer to get
     * application configs and artifact dependencies that may need to be attached to the installed artifacts.
     */
    private ArtifactContext artifactContext;

    /**
     * An artifact repository that handles .NET artifacts
     */
    private ArtifactRepository artifactRepository;

    /**
     * The default repository layout for .NET artifacts. This is used in the construction of the artifact repository.
     */
    private AssemblyRepositoryLayout assemblyRepositoryLayout;

    /**
     * A logger for writing log messages
     */
    private Logger logger;

    /**
     * Constructor. This method is intended to by invoked by the plexus-container, not by the application developer.
     */
    public ArtifactInstallerImpl()
    {
    }

    /**
     * @see LogEnabled#enableLogging(org.codehaus.plexus.logging.Logger)
     */
    public void enableLogging( Logger logger )
    {
        this.logger = logger;
    }

    /**
     * @see org.apache.maven.dotnet.artifact.ArtifactInstaller#installArtifact(org.apache.maven.artifact.Artifact, java.io.File)
     */
    public void installArtifact( Artifact artifact, File pomFile )
        throws ArtifactInstallationException
    {
        installNetModules( artifact );
        ApplicationConfig applicationConfig = artifactContext.getApplicationConfigFor( artifact );
        File configExeFile = new File( applicationConfig.getConfigDestinationPath() );

        if ( configExeFile.exists() )
        {
            logger.info( "NMAVEN-002-000: Found config executable: File = " + configExeFile.getAbsolutePath() );
            Dependency dependency = new Dependency();
            dependency.setArtifactId( project.getArtifactId() );
            dependency.setGroupId( project.getGroupId() );
            dependency.setVersion( project.getVersion() );
            dependency.setType( "exe.config" );
            dependency.setScope( Artifact.SCOPE_RUNTIME );
            List<Dependency> dep = new ArrayList<Dependency>();
            dep.add( dependency );
            project.setDependencies( dep );
            artifact.getMetadataList().clear();
            try
            {
                artifact.addMetadata( createArtifactMetadataFor( artifact, pomFile, dep ) );
            }
            catch ( IOException e )
            {
                throw new ArtifactInstallationException( "NMAVEN-002-001: Unable to add metadata to artifact", e );
            }
            File targetDirectory =
                new File( localRepository, assemblyRepositoryLayout.pathOf( artifact ) ).getParentFile();
            try
            {
                FileUtils.copyFileToDirectory( configExeFile, targetDirectory );
            }
            catch ( IOException e )
            {
                throw new ArtifactInstallationException( "NMAVEN-002-002: Failed to copy config.exe file", e );
            }
        }
        else
        {
            logger.debug( "NMAVEN-002-014: No exe.config file found: File = " + configExeFile.getAbsolutePath() );
        }

        try
        {
            if ( artifact.getFile() != null && artifact.getFile().exists() )//maybe just a test compile and no install
            {
                mavenInstaller.install( artifact.getFile(), artifact, artifactRepository );
            }
        }
        catch ( ArtifactInstallationException e )
        {
            throw new ArtifactInstallationException( "NMAVEN-002-003: Failed to install artifact: ID = " +
                artifact.getId() + ", File = " + artifact.getFile().getAbsolutePath(), e );
        }
    }

    /**
     * @see org.apache.maven.dotnet.artifact.ArtifactInstaller#installFile(String, String, String, String, java.io.File)
     */
    public void installFile( String groupId, String artifactId, String version, String packaging, File pomFile )
        throws ArtifactInstallationException
    {
        Artifact artifact =
            mavenArtifactFactory.createArtifactWithClassifier( groupId, artifactId, version, packaging, null );
        artifact.setFile( pomFile );

        FileWriter fileWriter = null;
        try
        {
            File tempFile = File.createTempFile( "mvninstall", ".pom" );
            tempFile.deleteOnExit();
            fileWriter = new FileWriter( tempFile );

            Model model = new Model();
            model.setModelVersion( "4.0.0" );
            model.setGroupId( groupId );
            model.setArtifactId( artifactId );
            model.setVersion( version );
            model.setPackaging( packaging );
            new MavenXpp3Writer().write( fileWriter, model );
            artifact.addMetadata( new ArtifactMetadataImpl( artifact, tempFile ) );
        }
        catch ( IOException e )
        {
            throw new ArtifactInstallationException( "NMAVEN-002-004: Failed to create pom file: Group Id = " +
                groupId + ", artifactId = " + artifactId + ", Version = " + version + ", packaging = " + packaging, e );
        }
        finally
        {
            IOUtil.close( fileWriter );
        }

        try
        {
            mavenInstaller.install( artifact.getFile(), artifact, artifactRepository );
        }
        catch ( ArtifactInstallationException e )
        {
            throw new ArtifactInstallationException( "NMAVEN-002-005: Failed to install artifact: ID = " +
                artifact.getId() + ", File = " + artifact.getFile().getAbsolutePath(), e );
        }

    }

    /**
     * @see org.apache.maven.dotnet.artifact.ArtifactInstaller#installNetModulesToTargetDirectory(org.apache.maven.artifact.Artifact)
     */
    public void installNetModulesToTargetDirectory( Artifact projectArtifact )
        throws ArtifactInstallationException
    {
        String targetDirectory = project.getBuild().getDirectory();
        List<Artifact> dependencies;
        try
        {
            dependencies = artifactContext.getNetModulesFor( projectArtifact );
        }
        catch ( ArtifactException e )
        {
            throw new ArtifactInstallationException( "NMAVEN-002-006: Unable to obtain .NET Modules: ", e );
        }

        if ( dependencies.isEmpty() )
        {
            logger.info( "NMAVEN-002-007: No dependencies to install into the target directory." );
            return;
        }

        for ( Artifact artifact : dependencies )
        {
            logger.info( "NMAVEN-002-008: Copying .NET module dependency into project's target directory: Module = " +
                targetDirectory + File.separator + artifact.getFile().getName() );
            try
            {
                FileUtils.copyFileToDirectory( artifact.getFile(), new File( targetDirectory ) );
            }
            catch ( IOException e )
            {
                throw new ArtifactInstallationException(
                    "NMAVEN-002-009: Failed to copy artifacts to target directory: " + "Target Directory = " +
                        targetDirectory, e );
            }
        }
    }

    /**
     * @see org.apache.maven.dotnet.artifact.ArtifactInstaller#init(org.apache.maven.dotnet.artifact.ArtifactContext,
     *      org.apache.maven.project.MavenProject, java.io.File)
     */
    public void init( ArtifactContext artifactContext, MavenProject mavenProject, File localRepository )
    {
        this.project = mavenProject;
        this.localRepository = localRepository;
        this.artifactContext = artifactContext;
        this.assemblyRepositoryLayout = new AssemblyRepositoryLayout();
        artifactRepository = new DefaultArtifactRepository( "local", "file://" + localRepository.getAbsolutePath(),
                                                            assemblyRepositoryLayout );
    }

    /**
     * Installs .netmodule artifacts into the local maven repository
     *
     * @param projectArtifact the artifact to install
     * @throws ArtifactInstallationException if there is a problem installing the artifact
     */
    private void installNetModules( Artifact projectArtifact )
        throws ArtifactInstallationException
    {
        StringBuffer path = new StringBuffer();
        path.append( localRepository.getAbsolutePath() ).append( File.separator );
        for ( String id : projectArtifact.getGroupId().split( "[.]" ) )
        {
            path.append( id ).append( File.separator );
        }

        path.append( projectArtifact.getArtifactId() ).append( File.separator )
            .append( projectArtifact.getBaseVersion() ).append( File.separator );
        List<Artifact> artifacts;
        try
        {
            artifacts = artifactContext.getNetModulesFor( project.getArtifact() );
        }
        catch ( ArtifactException e )
        {
            throw new ArtifactInstallationException( "NMAVEN-002-010: Unable to obtain .NET Modules: ", e );
        }

        for ( Artifact artifact : artifacts )
        {
            logger.info( "NMAVEN-002-011: Installing .NET Module Dependency: From = " +
                artifact.getFile().getAbsolutePath() + ", To = " + path.toString() + artifact.getArtifactId() +
                ".netmodule" );
            try
            {
                FileUtils.copyFileToDirectory( artifact.getFile(), new File( path.toString() ) );
            }
            catch ( IOException e )
            {
                throw new ArtifactInstallationException( "NMAVEN-002-012: Failed to copy artifacts to local repository",
                                                         e );
            }
        }
    }

    /**
     * Creates the artifact metadata file for the specified artifact.
     *
     * @param artifact     the artifact associated with the artifact metadata
     * @param pomFile      the pom file of the specified artifact
     * @param dependencies the dependencies of the specified artifact
     * @return the artifact metadata file for the specified artifact.
     * @throws IOException if there is a problem reading the pom file
     */
    private ArtifactMetadata createArtifactMetadataFor( Artifact artifact, File pomFile, List<Dependency> dependencies )
        throws IOException
    {
        FileReader fileReader = new FileReader( pomFile );
        MavenXpp3Reader reader = new MavenXpp3Reader();
        Model model;
        try
        {
            model = reader.read( fileReader );
        }
        catch ( XmlPullParserException e )
        {
            e.printStackTrace();
            throw new IOException( "NMAVEN-002-013: Unable to read pom file" );
        }

        for ( Dependency dependency : dependencies )
        {
            model.addDependency( dependency );
        }

        File tempFile = File.createTempFile( "mvninstall", ".pom" );
        tempFile.deleteOnExit();
        FileWriter fileWriter = new FileWriter( tempFile );
        new MavenXpp3Writer().write( fileWriter, model );
        IOUtil.close( fileWriter );
        return new ArtifactMetadataImpl( artifact, tempFile );

    }
}
