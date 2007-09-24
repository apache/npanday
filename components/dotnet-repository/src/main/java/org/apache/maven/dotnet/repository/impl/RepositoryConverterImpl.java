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
package org.apache.maven.dotnet.repository.impl;

import org.apache.maven.dotnet.repository.RepositoryConverter;
import org.apache.maven.dotnet.dao.Project;
import org.apache.maven.dotnet.dao.ProjectDao;
import org.apache.maven.dotnet.dao.ProjectFactory;
import org.apache.maven.dotnet.registry.DataAccessObjectRegistry;
import org.apache.maven.dotnet.ArtifactType;
import org.apache.maven.dotnet.artifact.ApplicationConfig;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Writer;
import org.apache.maven.artifact.repository.layout.ArtifactRepositoryLayout;
import org.apache.maven.artifact.repository.layout.DefaultRepositoryLayout;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.handler.ArtifactHandler;
import org.apache.maven.artifact.handler.DefaultArtifactHandler;
import org.apache.maven.artifact.manager.WagonManager;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.openrdf.repository.Repository;
import org.codehaus.plexus.util.IOUtil;
import org.codehaus.plexus.util.FileUtils;

import java.io.File;
import java.io.IOException;
import java.io.FileWriter;
import java.util.logging.Logger;
import java.util.Set;

/**
 * Implementation of the RepositoryConverter.
 */
public class RepositoryConverterImpl
    implements RepositoryConverter
{

    /**
     * The manager used to download artifacts.
     */
    private org.apache.maven.artifact.manager.WagonManager wagonManager;

    /**
     * The artifact factory component, which is used for creating artifacts.
     */
    private org.apache.maven.artifact.factory.ArtifactFactory artifactFactory;

    /**
     * The data access object registry used for finding DAO objects
     */
    private org.apache.maven.dotnet.registry.DataAccessObjectRegistry daoRegistry;

    private static Logger logger = Logger.getAnonymousLogger();

    /**
     * Used my unit tests to initialize the objects that plexus injects
     *
     * @param daoRegistry     the data access object registry used for finding DAO objects
     * @param artifactFactory the artifact factory used to create artifacts
     * @param wagonManager    the manager used to download artifacts
     */
    protected void initTest( DataAccessObjectRegistry daoRegistry, ArtifactFactory artifactFactory,
                             WagonManager wagonManager )
    {
        this.daoRegistry = daoRegistry;
        this.artifactFactory = artifactFactory;
        this.wagonManager = wagonManager;
    }

    /**
     * @see RepositoryConverter#convertRepositoryFormat(org.openrdf.repository.Repository, java.io.File)
     */
    public void convertRepositoryFormat( Repository repository, File mavenRepository )
        throws IOException
    {
        ProjectDao dao = (ProjectDao) daoRegistry.find( "dao:project" );
        dao.init( artifactFactory, wagonManager );
        dao.setRdfRepository( repository );
        dao.openConnection();
        Set<Project> projects = dao.getAllProjects();
        for ( Project project : projects )
        {
            logger.info( "NMAVEN-190-000: Converting Project: Artifact ID = " + project.getArtifactId() +
                ", Dependency Count =" + project.getProjectDependencies().size() );
            Artifact artifact = ProjectFactory.createArtifactFrom( project, artifactFactory, mavenRepository );
            Model model = ProjectFactory.createModelFrom( project );

            ArtifactHandler handler = new DefaultArtifactHandler(
                ArtifactType.getArtifactTypeForPackagingName( artifact.getType() ).getExtension() );
            artifact.setArtifactHandler( handler );

            ArtifactRepositoryLayout layout = new DefaultRepositoryLayout();
            if ( !project.getArtifactType().equals( "pom" ) )
            {
                if ( artifact.getFile().exists() )
                {
                    FileUtils.copyFile( artifact.getFile(), new File( mavenRepository, layout.pathOf( artifact ) ) );
                }
                else
                {
                    logger.info( "NMAVEN-190-001: Could not find file: " + artifact.getFile().getAbsolutePath() );
                    continue;
                }
            }

            if ( !artifact.getType().equals( "exe.config" ) )//This is attached
            {
                ArtifactHandler pomhandler = new DefaultArtifactHandler( "pom" );
                artifact.setArtifactHandler( pomhandler );

                File pomFile = new File( mavenRepository, pathOfPom( artifact ) );
                FileWriter fileWriter = new FileWriter( pomFile );
                new MavenXpp3Writer().write( fileWriter, model );
                IOUtil.close( fileWriter );
            }
            artifact.setArtifactHandler( handler );
        }
        dao.closeConnection();
    }

    public void convertRepositoryFormatFor( Artifact artifact, ApplicationConfig applicationConfig,
                                            Repository repository, File mavenRepository )
        throws IOException
    {
        ProjectDao dao = (ProjectDao) daoRegistry.find( "dao:project" );
        dao.init( artifactFactory, wagonManager );
        dao.setRdfRepository( repository );
        dao.openConnection();
        Project project = dao.getProjectFor( artifact.getGroupId(), artifact.getArtifactId(), artifact.getVersion(),
                                             artifact.getType(), artifact.getClassifier() );

        logger.info( "NMAVEN-190-002: Converting Project: Artifact ID = " + project.getArtifactId() +
            ", Dependency Count =" + project.getProjectDependencies().size() );
        Model model = ProjectFactory.createModelFrom( project );

        ArtifactHandler handler = new DefaultArtifactHandler(
            ArtifactType.getArtifactTypeForPackagingName( artifact.getType() ).getExtension() );
        artifact.setArtifactHandler( handler );

        ArtifactRepositoryLayout layout = new DefaultRepositoryLayout();
        if ( !project.getArtifactType().equals( "pom" ) )
        {
            if ( artifact.getFile().exists() )
            {
                FileUtils.copyFile( artifact.getFile(), new File( mavenRepository, layout.pathOf( artifact ) ) );
            }
            else
            {
                logger.info( "NMAVEN-190-003: Could not find file: " + artifact.getFile().getAbsolutePath() );
                return;
            }
        }

        if ( applicationConfig != null )
        {
            File destPath = applicationConfig.getConfigBuildPath();
            if ( destPath.exists() )
            {
                FileUtils.copyFile( destPath, applicationConfig.getRepositoryPath( mavenRepository ) );
            }
        }

        if ( !artifact.getType().equals( "exe.config" ) )//This is attached
        {
            ArtifactHandler pomhandler = new DefaultArtifactHandler( "pom" );
            artifact.setArtifactHandler( pomhandler );

            File pomFile = new File( mavenRepository, pathOfPom( artifact ) );
            FileWriter fileWriter = new FileWriter( pomFile );
            new MavenXpp3Writer().write( fileWriter, model );
            IOUtil.close( fileWriter );
        }

        artifact.setArtifactHandler( handler );

        dao.closeConnection();
    }

    /**
     * Returns the path of the the specified artifact's pom file.
     *
     * @param artifact the artifact
     * @return the path of the the specified artifact's pom file
     */
    private String pathOfPom( Artifact artifact )
    {
        StringBuffer artifactPath = new StringBuffer();
        for ( String groupId : artifact.getGroupId().split( "[.]" ) )
        {
            artifactPath.append( groupId ).append( File.separator );
        }

        artifactPath.append( artifact.getArtifactId() ).append( File.separator ).append( artifact.getBaseVersion() ).
            append( File.separator ).append( artifact.getArtifactId() ).append( "-" ).append(
            artifact.getBaseVersion() ).append( "." ).append( ( artifact.getArtifactHandler() ).getExtension() );
        return artifactPath.toString();
    }
}
