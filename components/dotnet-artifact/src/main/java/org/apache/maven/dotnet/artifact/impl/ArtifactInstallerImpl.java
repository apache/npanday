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
import org.apache.maven.dotnet.artifact.ArtifactType;
import org.apache.maven.dotnet.artifact.NetDependencyMatchPolicy;
import org.apache.maven.dotnet.artifact.NetDependenciesRepository;
import org.apache.maven.dotnet.artifact.AssemblyResolver;
import org.apache.maven.dotnet.registry.RepositoryRegistry;
import org.apache.maven.dotnet.model.netdependency.NetDependency;
import org.apache.maven.project.MavenProject;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.versioning.VersionRange;
import org.apache.maven.artifact.resolver.ArtifactResolutionResult;
import org.apache.maven.artifact.resolver.ArtifactResolver;
import org.apache.maven.artifact.resolver.ArtifactNotFoundException;
import org.apache.maven.artifact.resolver.ArtifactResolutionException;
import org.apache.maven.artifact.resolver.filter.ArtifactFilter;
import org.apache.maven.artifact.metadata.ArtifactMetadata;
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
import java.io.FileNotFoundException;
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

    public void resolveAndInstallNetDependenciesForProfile( String profile, List<Dependency> dependencies )
        throws ArtifactResolutionException, ArtifactNotFoundException, ArtifactInstallationException
    {
        if ( dependencies == null )
        {
            dependencies = new ArrayList<Dependency>();
        }

        NetDependenciesRepository repository =
            (NetDependenciesRepository) repositoryRegistry.find( "net-dependencies" );
        List<NetDependencyMatchPolicy> matchPolicies = new ArrayList<NetDependencyMatchPolicy>();
        matchPolicies.add( new ProfileMatchPolicy( profile ) );
        dependencies.addAll( repository.getDependenciesFor( matchPolicies ) );

        ArtifactRepository localArtifactRepository = new DefaultArtifactRepository( "local", "file://" +
            localRepository.getAbsolutePath(), new AssemblyRepositoryLayout() );

        assemblyResolver.resolveTransitivelyFor( project, project.getArtifact(), dependencies,
                                                 remoteArtifactRepositories, localArtifactRepository, false );

        //Do Library Installs for Net Dependencies
        matchPolicies = new ArrayList<NetDependencyMatchPolicy>();
        matchPolicies.add( new ProfileMatchPolicy( profile ) );
        matchPolicies.add( new ExecutableAndNetPluginAndAddinMatchPolicy() );
        for ( Dependency dependency : repository.getDependenciesFor( matchPolicies ) )
        {
            resolveAndInstallLibraryDependenciesFor( dependency );
        }
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
        //TODO: Remove GAC dependencies before installing. This should be removed and replaced with solution in the core.
        artifact.getMetadataList().clear();
        try
        {
            List<Dependency> dependencies = project.getDependencies();
            List<Dependency> newDependencies = new ArrayList<Dependency>();
            for ( Dependency dependency : dependencies )
            {
                if ( !dependency.getType().startsWith( "gac" ) )
                {
                    newDependencies.add( dependency );
                }
            }
            project.setDependencies( newDependencies );
            artifact.addMetadata( createArtifactMetadataFor( artifact, pomFile, project.getDependencies() ) );
        }
        catch ( IOException e )
        {
            throw new ArtifactInstallationException( "NMAVEN-002-001: Unable to add metadata to artifact", e );
        }
        //End GAC HACK

        if ( configExeFile.exists() )
        {
            logger.info( "NMAVEN-002-000: Found config executable: File = " + configExeFile.getAbsolutePath() );
            Dependency configExeDependency = new Dependency();
            configExeDependency.setArtifactId( project.getArtifactId() );
            configExeDependency.setGroupId( project.getGroupId() );
            configExeDependency.setVersion( project.getVersion() );
            configExeDependency.setType( "exe.config" );
            configExeDependency.setScope( Artifact.SCOPE_RUNTIME );
            List<Dependency> dep = new ArrayList<Dependency>();
            dep.add( configExeDependency );
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
                artifact.getId() + ", File = " +
                ( ( artifact.getFile() != null ) ? artifact.getFile().getAbsolutePath() : "" ), e );
        }
    }

    /**
     * @see org.apache.maven.dotnet.artifact.ArtifactInstaller#installFileWithGeneratedPom(String, String, String, String, java.io.File)
     */
    public void installFileWithGeneratedPom( String groupId, String artifactId, String version, String packaging,
                                             File artifactFile )
        throws ArtifactInstallationException
    {
        Artifact artifact =
            artifactFactory.createArtifactWithClassifier( groupId, artifactId, version, packaging, null );
        artifact.setFile( artifactFile );

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

    public void installFileWithNoPom( String groupId, String artifactId, String version, File installFile )
        throws ArtifactInstallationException
    {
        StringBuffer path = new StringBuffer();
        path.append( localRepository.getAbsolutePath() ).append( File.separator );
        for ( String id : groupId.split( "[.]" ) )
        {
            path.append( id ).append( File.separator );
        }

        path.append( artifactId ).append( File.separator )
            .append( version ).append( File.separator );

        logger.info( "NMAVEN-002-014: Installing File: From = " + installFile.getAbsolutePath() + ", To = " +
            path.toString() + artifactId + ".dll" );
        try
        {
            FileUtils.copyFileToDirectory( installFile, new File( path.toString() ) );
        }
        catch ( IOException e )
        {
            throw new ArtifactInstallationException( "NMAVEN-002-015: Failed to copy artifact to local repository", e );
        }
    }

    public void installLibraryDependencies( Artifact projectArtifact, List<Dependency> dependencies )
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

        for ( Dependency dependency : dependencies )
        {
            if ( ! ( dependency.getType().equals( "library" ) || dependency.getType().equals( "netplugin" ) ) )
            {
                continue;
            }
            StringBuffer depPath = new StringBuffer();
            depPath.append( localRepository.getAbsolutePath() ).append( File.separator );
            for ( String id : dependency.getGroupId().split( "[.]" ) )
            {
                depPath.append( id ).append( File.separator );
            }

            depPath.append( dependency.getArtifactId() ).append( File.separator )
                .append( dependency.getVersion() ).append( File.separator );
            String extension = ArtifactType.getArtifactTypeForPackagingName( dependency.getType() ).getExtension();
            File file = new File( depPath.toString() + dependency.getArtifactId() + "." + extension );

            try
            {
                logger.info( "NMAVEN-002-016: Installing File: From = " + file.getAbsolutePath() + ", To = " +
                    path.toString() + dependency.getArtifactId() + "." + extension );
                FileUtils.copyFileToDirectory( file, new File( path.toString() ) );
            }
            catch ( IOException e )
            {
                throw new ArtifactInstallationException(
                    "NMAVEN-002-017: Failed to install file into repo: File Name = " + file.getAbsolutePath() +
                        ", Extension = " + extension + ", Type = " + dependency.getType(), e );
            }
        }
    }

    public void resolveAndInstallLibraryDependenciesFor( Dependency dependency )
        throws ArtifactInstallationException, ArtifactNotFoundException
    {

        Artifact sourceArtifact = artifactFactory.createBuildArtifact( dependency.getGroupId(),
                                                                       dependency.getArtifactId(),
                                                                       dependency.getVersion(), dependency.getType() );
        //Resolve the JavaBinding for the .NET plugin
        ArtifactRepository localArtifactRepository =
            new DefaultArtifactRepository( "local", "file://" + localRepository, new DefaultRepositoryLayout() );
        if ( sourceArtifact.getType().equals( ArtifactType.NETPLUGIN.getPackagingType() ) )
        {
            Artifact javaBindingArtifact = artifactFactory.createBuildArtifact( sourceArtifact.getGroupId(),
                                                                                sourceArtifact.getArtifactId() +
                                                                                    ".JavaBinding",
                                                                                sourceArtifact.getVersion(), "jar" );
            try
            {
                resolver.resolve( javaBindingArtifact, remoteArtifactRepositories, localArtifactRepository );
            }
            catch ( ArtifactResolutionException e )
            {
                throw new ArtifactNotFoundException( "", sourceArtifact );
            }
        }

        //Resolve all the specified dependencies
        Artifact pomArtifact = artifactFactory.createProjectArtifact( dependency.getGroupId(),
                                                                      dependency.getArtifactId(),
                                                                      dependency.getVersion() );
        File pomArtifactFile = new File( localRepository, new AssemblyRepositoryLayout().pathOf( pomArtifact ) );
        FileReader fileReader;
        try
        {
            fileReader = new FileReader( pomArtifactFile );
        }
        catch ( FileNotFoundException e )
        {
            throw new ArtifactNotFoundException( "NMAVEN-000-000: Unable to read pom", sourceArtifact );
        }
        MavenXpp3Reader reader = new MavenXpp3Reader();
        Model model;
        try
        {
            model = reader.read( fileReader );
        }
        catch ( XmlPullParserException e )
        {
            throw new ArtifactNotFoundException( "NMAVEN-000-000: Unable to read model", sourceArtifact );

        }
        catch ( IOException e )
        {
            throw new ArtifactNotFoundException( "NMAVEN-000-000: Unable to read model", sourceArtifact );
        }
        List<Dependency> sourceArtifactDependencies = model.getDependencies();
        localArtifactRepository =
            new DefaultArtifactRepository( "local", "file://" + localRepository, new AssemblyRepositoryLayout() );
        Set<Artifact> artifactDependencies = new HashSet<Artifact>();
        for ( Dependency d : sourceArtifactDependencies )
        {
            String scope = ( d.getScope() == null ) ? Artifact.SCOPE_COMPILE : d.getScope();
            Artifact artifact1 = artifactFactory.createDependencyArtifact( d.getGroupId(), d.getArtifactId(),
                                                                           VersionRange.createFromVersion(
                                                                               d.getVersion() ), d.getType(),
                                                                                                 d.getClassifier(),
                                                                                                 scope, null );
            artifactDependencies.add( artifact1 );

        }

        ArtifactMetadataImpl meta = new ArtifactMetadataImpl( sourceArtifact, null );
        sourceArtifact.addMetadata( meta );
        ArtifactResolutionResult result;
        try
        {
            result = resolver.resolveTransitively( artifactDependencies, sourceArtifact, localArtifactRepository,
                                                   remoteArtifactRepositories, metadata, new ArtifactFilter()
            {
                public boolean include( org.apache.maven.artifact.Artifact artifact )
                {
                    return !artifact.getType().startsWith( "gac" );
                }
            } );
        }
        catch ( ArtifactResolutionException e )
        {
            throw new ArtifactNotFoundException( "NMAVEN-000-000: ", sourceArtifact );
        }

        //Do local installing of the dependencies into exe and netplugin repo directories
        AssemblyRepositoryLayout layout = new AssemblyRepositoryLayout();
        Set<Artifact> artifacts = result.getArtifacts();

        File destDir = new File( localRepository, layout.pathOf( sourceArtifact ) ).getParentFile();
        for ( Artifact artifact : artifacts )
        {
            File destFile = new File( destDir, artifact.getFile().getName() );
            if ( !destFile.exists() || destFile.lastModified() < artifact.getFile().lastModified() )
            {
                try
                {
                    FileUtils.copyFileToDirectory( artifact.getFile(), destDir );
                }
                catch ( IOException e )
                {
                    e.printStackTrace();
                }
            }
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
                File targetDirectoryFile = new File( targetDirectory );
                if ( new File( targetDirectoryFile, artifact.getFile().getName() ).lastModified() <
                    artifact.getFile().lastModified() )
                {
                    FileUtils.copyFileToDirectory( artifact.getFile(), targetDirectoryFile );
                }
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
     * @see org.apache.maven.dotnet.artifact.ArtifactInstaller#init(org.apache.maven.dotnet.artifact.ArtifactContext,org.apache.maven.project.MavenProject,java.util.List, File
     */
    public void init( ArtifactContext artifactContext, MavenProject mavenProject,
                      List<ArtifactRepository> remoteArtifactRepositories, File localRepository )
    {
        this.project = mavenProject;
        this.remoteArtifactRepositories = remoteArtifactRepositories;
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
        List<Dependency> dest = new ArrayList<Dependency>();
        dest.addAll( model.getDependencies() );
        for ( Dependency dependency : dest )
        {
            model.removeDependency( dependency );
        }

        for ( Dependency dependency : dependencies )
        {
            //TODO: This condition is only here since transitive gac dependencies break the build. This needs to be fixed
            //within the core.
            if ( !dependency.getType().startsWith( "gac" ) )
            {
                model.addDependency( dependency );
            }
        }

        File tempFile = File.createTempFile( "mvninstall", ".pom" );
        tempFile.deleteOnExit();
        FileWriter fileWriter = new FileWriter( tempFile );
        new MavenXpp3Writer().write( fileWriter, model );
        IOUtil.close( fileWriter );
        return new ArtifactMetadataImpl( artifact, tempFile );
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
