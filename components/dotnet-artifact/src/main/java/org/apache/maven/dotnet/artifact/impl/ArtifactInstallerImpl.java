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

import org.apache.maven.dotnet.artifact.ArtifactContext;
import org.apache.maven.dotnet.artifact.ApplicationConfig;
import org.apache.maven.dotnet.ArtifactType;
import org.apache.maven.dotnet.PathUtil;
import org.apache.maven.dotnet.artifact.NetDependencyMatchPolicy;
import org.apache.maven.dotnet.artifact.NetDependenciesRepository;
import org.apache.maven.dotnet.artifact.AssemblyResolver;
import org.apache.maven.dotnet.registry.RepositoryRegistry;
import org.apache.maven.dotnet.model.netdependency.NetDependency;
import org.apache.maven.dotnet.dao.ProjectDao;
import org.apache.maven.dotnet.dao.Project;
import org.apache.maven.dotnet.dao.ProjectFactory;
import org.apache.maven.dotnet.dao.ProjectDependency;
import org.apache.maven.project.MavenProject;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.versioning.VersionRange;
import org.apache.maven.artifact.resolver.ArtifactResolver;
import org.apache.maven.artifact.resolver.ArtifactNotFoundException;
import org.apache.maven.artifact.resolver.ArtifactResolutionException;
import org.apache.maven.artifact.metadata.ArtifactMetadataSource;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.installer.ArtifactInstallationException;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.repository.DefaultArtifactRepository;
import org.apache.maven.artifact.repository.layout.DefaultRepositoryLayout;
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
import java.util.Set;
import java.util.HashSet;

/**
 * Provides an implementation of the <code>ArtifactInstaller</code> interface.
 *
 * @author Shane Isbell
 */
public class ArtifactInstallerImpl
    implements org.apache.maven.dotnet.artifact.ArtifactInstaller, LogEnabled
{

    /**
     * An installer component for installing artifacts into a local Maven repository.
     */
    private org.apache.maven.artifact.installer.ArtifactInstaller mavenInstaller;

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
     * A logger for writing log messages
     */
    private Logger logger;

    /**
     * An artifact resolver component for locating artifacts and pulling them into the local repo if they do not
     * already exist.
     */
    private ArtifactResolver resolver;

    /**
     * Metadata component used by the <code>ArtifactResolver</code>.
     */
    private ArtifactMetadataSource metadata;

    /**
     * The artifact factory component, which is used for creating artifacts.
     */
    private ArtifactFactory artifactFactory;

    private AssemblyResolver assemblyResolver;

    private RepositoryRegistry repositoryRegistry;

    private List<ArtifactRepository> remoteArtifactRepositories;

    private org.apache.maven.dotnet.registry.DataAccessObjectRegistry daoRegistry;

    private org.apache.maven.artifact.manager.WagonManager wagonManager;


    /**
     * Constructor. This method is intended to by invoked by the plexus-container, not by the application developer.
     */
    public ArtifactInstallerImpl()
    {
    }

    protected void initTest( ArtifactFactory artifactFactory, Logger logger )
    {
        this.artifactFactory = artifactFactory;
        this.logger = logger;
    }

    /**
     * @see LogEnabled#enableLogging(org.codehaus.plexus.logging.Logger)
     */
    public void enableLogging( Logger logger )
    {
        this.logger = logger;
    }

    public void resolveAndInstallNetDependenciesForProfile( String profile, List<Dependency> netDependencies,
                                                            List<Dependency> javaDependencies )
        throws IOException
    {
        if ( netDependencies == null )
        {
            netDependencies = new ArrayList<Dependency>();
        }

        if ( javaDependencies == null )
        {
            javaDependencies = new ArrayList<Dependency>();
        }

        NetDependenciesRepository repository =
            (NetDependenciesRepository) repositoryRegistry.find( "net-dependencies" );
        List<NetDependencyMatchPolicy> matchPolicies = new ArrayList<NetDependencyMatchPolicy>();
        matchPolicies.add( new ProfileMatchPolicy( profile ) );
        netDependencies.addAll( repository.getDependenciesFor( matchPolicies ) );

        assemblyResolver.resolveTransitivelyFor( new MavenProject(), netDependencies, remoteArtifactRepositories,
                                                 localRepository, false );

        //Do Library Installs for Net Dependencies
        matchPolicies = new ArrayList<NetDependencyMatchPolicy>();
        matchPolicies.add( new ProfileMatchPolicy( profile ) );
        matchPolicies.add( new ExecutableAndNetPluginAndAddinMatchPolicy() );

        ArtifactRepository localArtifactRepo =
            new DefaultArtifactRepository( "local", "file://" + localRepository, new DefaultRepositoryLayout() );

        for ( Dependency dependency : netDependencies )
        {
            Artifact sourceArtifact = artifactFactory.createBuildArtifact( dependency.getGroupId(),
                                                                           dependency.getArtifactId(),
                                                                           dependency.getVersion(),
                                                                           dependency.getType() );
            //Resolve the JavaBinding for the .NET plugin
            if ( sourceArtifact.getType().equals( ArtifactType.NETPLUGIN.getPackagingType() ) )
            {
                Artifact javaBindingArtifact = artifactFactory.createBuildArtifact( sourceArtifact.getGroupId(),
                                                                                    sourceArtifact.getArtifactId() +
                                                                                        ".JavaBinding",
                                                                                    sourceArtifact.getVersion(),
                                                                                    "jar" );
                try
                {
                    resolver.resolve( javaBindingArtifact, remoteArtifactRepositories, localArtifactRepo );
                }
                catch ( ArtifactResolutionException e )
                {
                    throw new IOException( e.getMessage() );
                }
                catch ( ArtifactNotFoundException e )
                {
                    throw new IOException( e.getMessage() );
                }
            }
        }

        for ( Dependency dependency : javaDependencies )
        {
            Artifact javaArtifact = artifactFactory.createBuildArtifact( dependency.getGroupId(),
                                                                         dependency.getArtifactId(),
                                                                         dependency.getVersion(),
                                                                         dependency.getType() );
            try
            {
                resolver.resolve( javaArtifact, remoteArtifactRepositories, localArtifactRepo );
            }
            catch ( ArtifactResolutionException e )
            {
                throw new IOException( e.getMessage() );
            }
            catch ( ArtifactNotFoundException e )
            {
                throw new IOException( e.getMessage() );
            }
        }

        ProjectDao dao = (ProjectDao) daoRegistry.find( "dao:project" );
        dao.openConnection();
        for ( Dependency dependency : repository.getDependenciesFor( matchPolicies ) )
        {
            Project project = dao.getProjectFor( dependency.getGroupId(), dependency.getArtifactId(),
                                                 dependency.getVersion(), dependency.getType(),
                                                 dependency.getClassifier() );

            Artifact sourceArtifact = ProjectFactory.createArtifactFrom( project, artifactFactory, localRepository );

            List<Dependency> sourceArtifactDependencies = new ArrayList<Dependency>();
            for ( ProjectDependency projectDependency : project.getProjectDependencies() )
            {
                sourceArtifactDependencies.add( ProjectFactory.createDependencyFrom( projectDependency ) );
            }
            installArtifactAndDependenciesIntoPrivateApplicationBase( localRepository, sourceArtifact,
                                                                      sourceArtifactDependencies );
        }
        dao.closeConnection();
    }

    public void installArtifactAndDependenciesIntoPrivateApplicationBase( File localRepository, Artifact artifact,
                                                                          List<Dependency> dependencies )
        throws IOException
    {

        Set<Artifact> artifactDependencies = new HashSet<Artifact>();
        for ( Dependency dependency : dependencies )
        {
            String scope = ( dependency.getScope() == null ) ? Artifact.SCOPE_COMPILE : dependency.getScope();
            Artifact artifactDependency = artifactFactory.createDependencyArtifact( dependency.getGroupId(),
                                                                                    dependency.getArtifactId(),
                                                                                    VersionRange.createFromVersion(
                                                                                        dependency.getVersion() ),
                                                                                    dependency.getType(),
                                                                                    dependency.getClassifier(), scope,
                                                                                    null );

            File artifactDependencyFile = PathUtil.getUserAssemblyCacheFileFor( artifactDependency, localRepository );
            if ( artifactDependencyFile == null || !artifactDependencyFile.exists() )
            {
                artifactDependencyFile = PathUtil.getGlobalAssemblyCacheFileFor( artifactDependency, new File(
                    System.getProperty( "SystemDrive" ), "\\Windows\\assembly" ) );
            }

            if ( artifactDependencyFile == null || !artifactDependencyFile.exists() )
            {
                throw new IOException( "NMAVEN-000-000: Could not find artifact dependency: Artifact ID = " +
                    artifactDependency.getArtifactId() + ", Path = " + (
                    ( artifactDependencyFile != null && !artifactDependencyFile.exists() )
                        ? artifactDependencyFile.getAbsolutePath() : null ) );
            }
            artifactDependency.setFile( artifactDependencyFile );
            artifactDependencies.add( artifactDependency );
        }

        if ( artifact != null )
        {
            artifactDependencies.add( artifact );
        }
        File installDirectory = PathUtil.getPrivateApplicationBaseFileFor( artifact, localRepository ).getParentFile();
        for ( Artifact artifactDependency : artifactDependencies )
        {
            if ( !artifactDependency.getType().startsWith( "gac" ) )
            {
                logger.info( "NMAVEN-002-018c: Installing file into repository: File = " +
                    artifactDependency.getFile().getAbsolutePath() );
                FileUtils.copyFileToDirectory( artifactDependency.getFile(), installDirectory );
            }
        }
    }

    /**
     * @see org.apache.maven.dotnet.artifact.ArtifactInstaller#installArtifactWithPom(org.apache.maven.artifact.Artifact,java.io.File,boolean)
     */
    public void installArtifactWithPom( Artifact artifact, File pomFile, boolean modifyProjectMetadata )
        throws ArtifactInstallationException
    {
        ApplicationConfig applicationConfig = artifactContext.getApplicationConfigFor( artifact );
        File configExeFile = new File( applicationConfig.getConfigDestinationPath() );
        if ( configExeFile.exists() )
        {
            try
            {
                FileUtils.copyFileToDirectory( configExeFile, PathUtil.getUserAssemblyCacheFileFor( artifact,
                                                                                                    localRepository ).getParentFile() );
            }
            catch ( IOException e )
            {
                throw new ArtifactInstallationException( "NMAVEN-002-003b: Failed to install artifact: ID = " +
                    artifact.getId() + ", File = " +
                    ( ( artifact.getFile() != null ) ? artifact.getFile().getAbsolutePath() : "" ), e );
            }
        }

        try
        {
            if ( artifact.getFile() != null && artifact.getFile().exists() )//maybe just a test compile and no install
            {
                logger.info(
                    "NMAVEN-002-018: Installing file into repository: File = " + artifact.getFile().getAbsolutePath() );
                File artifactFile = artifact.getFile();
                try
                {
                    FileUtils.copyFile( artifactFile,
                                        PathUtil.getUserAssemblyCacheFileFor( artifact, localRepository ) );
                }
                catch ( IOException e )
                {
                    throw new ArtifactInstallationException( "NMAVEN-002-003a: Failed to install artifact: ID = " +
                        artifact.getId() + ", File = " +
                        ( ( artifact.getFile() != null ) ? artifact.getFile().getAbsolutePath() : "" ), e );
                }
            }
            else
            {
                logger.info( "NMAVEN-002-019: Artifact does not exist. Nothing to install: Artifact = " +
                    artifact.getGroupId() + ":" + artifact.getArtifactId() + ":" + artifact.getVersion() );
            }
        }
        catch ( ArtifactInstallationException e )
        {
            throw new ArtifactInstallationException( "NMAVEN-002-003: Failed to install artifact: ID = " +
                artifact.getId() + ", File = " +
                ( ( artifact.getFile() != null ) ? artifact.getFile().getAbsolutePath() : "" ), e );
        }

        if ( !artifact.getType().equals( "exe.config" ) )//TODO: Generalize for any attached artifact
        {
            MavenXpp3Reader reader = new MavenXpp3Reader();
            Model model;
            try
            {
                model = reader.read( new FileReader( pomFile ) );
                if ( configExeFile.exists() )
                {
                    Dependency dependency = new Dependency();
                    dependency.setGroupId( artifact.getGroupId() );
                    dependency.setArtifactId( artifact.getArtifactId() );
                    dependency.setVersion( artifact.getVersion() );
                    dependency.setType( ArtifactType.EXECONFIG.getPackagingType() );
                    model.addDependency( dependency );
                }
            }
            catch ( XmlPullParserException e )
            {
                throw new ArtifactInstallationException( "NMAVEN-002-013: Unable to read pom file" );
            }
            catch ( IOException e )
            {
                throw new ArtifactInstallationException( "NMAVEN-002-013: Unable to read pom file" );
            }

            ProjectDao dao = (ProjectDao) daoRegistry.find( "dao:project" );
            dao.openConnection();
            try
            {
                dao.storeModelAndResolveDependencies( model, pomFile.getParentFile(), localRepository,
                                                      new ArrayList<ArtifactRepository>() );
            }
            catch ( java.io.IOException e )
            {
                throw new ArtifactInstallationException( e.getMessage() );
            }
            finally
            {
                dao.closeConnection();
            }
        }
    }

    /**
     * @see org.apache.maven.dotnet.artifact.ArtifactInstaller#installFileWithoutPom(String, String, String, String, java.io.File)
     */
    public void installFileWithoutPom( String groupId, String artifactId, String version, String packaging,
                                       File artifactFile )
        throws ArtifactInstallationException
    {
        Artifact artifact =
            artifactFactory.createArtifactWithClassifier( groupId, artifactId, version, packaging, null );
        artifact.setFile( artifactFile );
        Model model = new Model();
        model.setGroupId( groupId );
        model.setArtifactId( artifactId );
        model.setVersion( version );
        model.setPackaging( packaging );
        File tempFile;
        FileWriter fileWriter;
        try
        {
            tempFile = File.createTempFile( "mvninstall", ".pom" );
            tempFile.deleteOnExit();
            fileWriter = new FileWriter( tempFile );
            new MavenXpp3Writer().write( fileWriter, model );
        }
        catch ( IOException e )
        {
            throw new ArtifactInstallationException( e.getMessage() );
        }
        IOUtil.close( fileWriter );
        installArtifactWithPom( artifact, tempFile, false );
    }

    public void init( ArtifactContext artifactContext, List<ArtifactRepository> remoteArtifactRepositories,
                      File localRepository )
    {
        this.remoteArtifactRepositories = remoteArtifactRepositories;
        this.localRepository = localRepository;
        this.artifactContext = artifactContext;
    }

    private class ExecutableAndNetPluginAndAddinMatchPolicy
        implements NetDependencyMatchPolicy
    {
        public boolean match( NetDependency netDependency )
        {
            return netDependency.getType().equals( ArtifactType.EXE.getPackagingType() ) ||
                netDependency.getType().equals( ArtifactType.NETPLUGIN.getPackagingType() ) ||
                netDependency.getType().equals( ArtifactType.VISUAL_STUDIO_ADDIN.getPackagingType() ) ||
                netDependency.getType().equals( ArtifactType.SHARP_DEVELOP_ADDIN.getPackagingType() );
        }
    }

    private class ProfileMatchPolicy
        implements NetDependencyMatchPolicy
    {

        private String profile;

        public ProfileMatchPolicy( String profile )
        {
            this.profile = profile;
        }

        public boolean match( NetDependency netDependency )
        {
            //If no profile is specified in net-dependencies.xml, it matches
            if ( netDependency.getProfile() == null || netDependency.getProfile().trim().equals( "" ) )
            {
                return true;
            }

            if ( profile == null )
            {
                return false;
            }

            return profile.equals( netDependency.getProfile() );
        }
    }
}
