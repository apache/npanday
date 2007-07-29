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
package org.apache.maven.dotnet.plugin.install;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.artifact.installer.ArtifactInstaller;
import org.apache.maven.artifact.installer.ArtifactInstallationException;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.dotnet.artifact.ArtifactContext;
import org.apache.maven.dotnet.ArtifactType;
import org.apache.maven.dotnet.PathUtil;
import org.apache.maven.dotnet.artifact.ApplicationConfig;
import org.apache.maven.dotnet.executable.NetExecutable;
import org.apache.maven.dotnet.executable.ExecutionException;
import org.apache.maven.dotnet.PlatformUnsupportedException;
import org.apache.maven.dotnet.dao.Project;
import org.apache.maven.dotnet.dao.ProjectDependency;
import org.apache.maven.dotnet.dao.ProjectDao;
import org.apache.maven.model.Dependency;

import java.io.File;
import java.util.List;
import java.util.ArrayList;

/**
 * @goal install
 * @phase install
 */
public class InstallerMojo
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
     * @parameter expression="${project.file}"
     */
    private File pomFile;

    /**
     * @component
     */
    private ArtifactContext artifactContext;

    /**
     * Install into the GAC?
     *
     * @parameter expression="${isGacInstall}" default-value = "false"
     */
    private boolean isGacInstall;

    /**
     * @component
     */
    private org.apache.maven.dotnet.executable.NetExecutableFactory netExecutableFactory;

    /**
     * The Vendor for the executable.
     *
     * @parameter expression="${vendor}"
     */
    private String vendor;

    /**
     * @parameter expression = "${frameworkVersion}"
     */
    private String frameworkVersion;

    /**
     * The profile that the executable should use.
     *
     * @parameter expression = "${profile}" default-value = "GACUTIL"
     */
    private String profile;

    /**
     * @component
     */
    private org.apache.maven.dotnet.registry.DataAccessObjectRegistry daoRegistry;

    /**
     * @component
     */
    private org.apache.maven.artifact.manager.WagonManager wagonManager;

    /**
     * The artifact factory component, which is used for creating artifacts.
     *
     * @component
     */
    private ArtifactFactory artifactFactory;

    public void execute()
        throws MojoExecutionException
    {
        long startTime = System.currentTimeMillis();

        if ( localRepository == null )
        {
            localRepository = new File( System.getProperty( "user.home" ), ".m2/repository" );
        }

        ProjectDao dao = (ProjectDao) daoRegistry.find( "dao:project" );
        dao.init( artifactFactory, wagonManager );
        dao.openConnection();

        artifactContext.init( project, project.getRemoteArtifactRepositories(), localRepository );
        if ( project.getPackaging().equals( "pom" ) )
        {
            storeMavenProject( project );
        }
        else
        {
            if ( isGacInstall )
            {
                try
                {
                    NetExecutable netExecutable = netExecutableFactory.getNetExecutableFor( vendor, frameworkVersion,
                                                                                            profile, getCommands(),
                                                                                            null );
                    netExecutable.execute();
                    getLog().info( "NMAVEN-xxx-003: Installed Assembly into GAC: Assembly = " +
                        project.getArtifact().getFile() + ",  Vendor = " + netExecutable.getVendor().getVendorName() );
                }
                catch ( ExecutionException e )
                {
                    throw new MojoExecutionException( "NMAVEN-1400-000: Unable to execute gacutil: Vendor " + vendor +
                        ", frameworkVersion = " + frameworkVersion + ", Profile = " + profile, e );
                }
                catch ( PlatformUnsupportedException e )
                {
                    throw new MojoExecutionException( "NMAVEN-1400-001: Platform Unsupported: Vendor " + vendor +
                        ", frameworkVersion = " + frameworkVersion + ", Profile = " + profile, e );
                }
                storeMavenProject( project );
            }

            //UAC install
            try
            {
                artifactContext.getArtifactInstaller().installArtifactWithPom( project.getArtifact(), pomFile, true );
            }
            catch ( ArtifactInstallationException e )
            {
                throw new MojoExecutionException( "NMAVEN-1001-000: Failed to install artifacts", e );
            }
        }

        // To allow executables to be runnable from the repo
        Artifact artifact = project.getArtifact();

        if ( artifact.getType().equals( ArtifactType.EXE.getPackagingType() ) ||
            artifact.getType().equals( ArtifactType.NETPLUGIN.getPackagingType() ) ||
            artifact.getType().equals( ArtifactType.VISUAL_STUDIO_ADDIN.getPackagingType() ) ||
            artifact.getType().equals( ArtifactType.SHARP_DEVELOP_ADDIN.getPackagingType() ) )
        {
            List<Dependency> dependencies = project.getDependencies();
            if ( artifact.getType().equals( ArtifactType.EXE.getPackagingType() ) )
            {
                ApplicationConfig applicationConfig = artifactContext.getApplicationConfigFor( artifact );
              //  File configExeFile = new File( applicationConfig.getConfigDestinationPath() );
                /*
                if ( configExeFile.exists() )
                {
                    Dependency dependency = new Dependency();
                    dependency.setGroupId( artifact.getGroupId() );
                    dependency.setArtifactId( artifact.getArtifactId() );
                    dependency.setVersion( artifact.getVersion() );
                    dependency.setType( ArtifactType.EXECONFIG.getPackagingType() );
                    dependencies.add( dependency );
                }
                */
            }
            try
            {
                artifactContext.getArtifactInstaller()
                    .installArtifactAndDependenciesIntoPrivateApplicationBase( localRepository, artifact,
                                                                               dependencies );
            }
            catch ( java.io.IOException e )
            {
                throw new MojoExecutionException( e.getMessage() );
            }
        }

        long endTime = System.currentTimeMillis();
        getLog().info( "Mojo Execution Time = " + ( endTime - startTime ) );
    }

    public List<String> getCommands()
        throws MojoExecutionException
    {
        List<String> commands = new ArrayList<String>();
        commands.add( "/i" );
        commands.add( project.getArtifact().getFile().getAbsolutePath() );
        return commands;
    }

    //TODO: Handle parent poms
    private void storeMavenProject( MavenProject project )
        throws MojoExecutionException
    {
        ProjectDao dao = (ProjectDao) daoRegistry.find( "dao:project" );
        Project proj = new Project();
        proj.setGroupId( project.getGroupId() );
        proj.setArtifactId( project.getArtifactId() );
        proj.setVersion( project.getVersion() );
        proj.setPublicKeyTokenId( project.getArtifact().getClassifier() );
        proj.setArtifactType( project.getArtifact().getType() );
        proj.setResolved( true );
        for ( Dependency dependency : (List<Dependency>) project.getDependencies() )
        {
            ProjectDependency projectDependency = new ProjectDependency();
            projectDependency.setGroupId( dependency.getGroupId() );
            projectDependency.setArtifactId( dependency.getArtifactId() );
            projectDependency.setVersion( dependency.getVersion() );
            projectDependency.setPublicKeyTokenId( dependency.getClassifier() );
            projectDependency.setArtifactType( dependency.getType() );
            proj.addProjectDependency( projectDependency );
        }

        try
        {
            dao.storeProjectAndResolveDependencies( proj, localRepository, new ArrayList<ArtifactRepository>() );
        }
        catch ( java.io.IOException e )
        {
            throw new MojoExecutionException( "NMAVEN-1001-001: Failed to install artifacts", e );
        }
    }
}
