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
package org.apache.maven.dotnet.embedder.impl;

import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;
import java.util.logging.Logger;
import java.io.File;
import java.io.IOException;

import org.apache.maven.dotnet.embedder.MavenEmbedderService;
import org.apache.maven.dotnet.embedder.MavenExecutionRequest;
import org.apache.maven.dotnet.embedder.MavenProject;
import org.apache.maven.dotnet.embedder.Artifact;
import org.apache.maven.dotnet.embedder.EmbedderException;
import org.apache.maven.dotnet.embedder.logger.SocketLoggerManager;
import org.apache.maven.dotnet.embedder.logger.SocketLogger;
import org.apache.maven.embedder.MavenEmbedder;
import org.apache.maven.embedder.MavenEmbedderException;
import org.apache.maven.embedder.Configuration;
import org.apache.maven.embedder.DefaultConfiguration;
import org.apache.maven.execution.DefaultMavenExecutionRequest;
import org.apache.maven.model.Model;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Disposable;
import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.classworlds.ClassWorld;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.codehaus.plexus.util.DirectoryScanner;

/**
 * Provides an implementation of the maven embedder service.
 *
 * @author Shane Isbell
 */
public final class MavenEmbedderServiceImpl
    extends AbstractLogEnabled
    implements MavenEmbedderService, Initializable, Disposable
{

    /**
     * The maven embedder instance
     */
    private MavenEmbedder embedder;

    private static Logger logger = Logger.getAnonymousLogger();

    /**
     * Constructor. This method is intended to by invoked by xfire, not by the application developer.
     */
    public MavenEmbedderServiceImpl()
    {
    }

    /**
     * @see MavenEmbedderService#execute(org.apache.maven.dotnet.embedder.MavenExecutionRequest)
     */
    public void execute( final MavenExecutionRequest request )
    {
        new Thread( new Runnable()
        {
            public void run()
            {
                try
                {
                    if ( request.getLoggerPort() <= 0 )
                    {
                        resetSocket( 9099 );
                    }
                    else
                    {
                        resetSocket( request.getLoggerPort() );
                    }

                    logger.info( "NPANDAY: Executing Maven Build Request: Goal = " + request.getGoal() +
                        ", Pom File = " + request.getPomFile() );
                    List<String> goals = new ArrayList<String>();
                    goals.add( request.getGoal() );
                    org.apache.maven.execution.MavenExecutionRequest executionRequest =
                        new DefaultMavenExecutionRequest();
                    executionRequest.setPomFile( request.getPomFile() );
                    executionRequest.setGoals( goals );
                    executionRequest.setRecursive( true );
                    executionRequest.setSettings( embedder.getSettings() );
                    embedder.execute( executionRequest );
                }
                catch ( EmbedderException e )
                {
                    e.printStackTrace();
                }
            }
        } ).start();
    }

    /**
     * @see org.apache.maven.dotnet.embedder.MavenEmbedderService#getRepositoryArtifacts()
     */
    public Set<Artifact> getRepositoryArtifacts()
    {
        File localRepository = new File( System.getProperty( "user.home" ), ".m2/repository" );
        return getArtifactsFrom( localRepository.getAbsolutePath(), "**/*.pom" );
    }

    /**
     * @see MavenEmbedderService#getMavenProjectsFor(String)
     */
    public Set<org.apache.maven.dotnet.embedder.MavenProject> getMavenProjectsFor( String basedir )
        throws EmbedderException
    {
        logger.info( "NPANDAY: Getting maven projects: BaseDir = " + basedir );
        Set<MavenProject> allMavenProjects = getMavenProjectsFrom( basedir, "**/*pom*.xml" );
        Set<MavenProject> mavenProjects = new HashSet<MavenProject>();
        for ( MavenProject mavenProject : getMavenProjectsFrom( basedir, "*pom*.xml" ) )
        {
            mavenProjects.addAll( attachMavenProjectsFor( new File( mavenProject.getPomPath() ), allMavenProjects ) );
        }
        Set<MavenProject> orphanedMavenProjects = new HashSet<MavenProject>( allMavenProjects );
        orphanedMavenProjects.removeAll( getFlatSetFor( mavenProjects ) );
        for ( MavenProject mavenProject : orphanedMavenProjects )
        {
            ( (MavenProjectImpl) mavenProject ).setIsOrphaned( true );
        }
        mavenProjects.addAll( orphanedMavenProjects );
        logger.info( "NPANDAY: Found projects: Orphaned  = " + orphanedMavenProjects.size() + ", Total = " +
            allMavenProjects.size() + ", Returned = " + mavenProjects.size() );
        return mavenProjects;
    }

    /**
     * @see org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable#initialize()
     */
    public void initialize()
    {
        ClassWorld classWorld = new ClassWorld();
        try
        {
            classWorld.newRealm( "plexus.core", Thread.currentThread().getContextClassLoader() );
        }
        catch ( org.codehaus.plexus.classworlds.realm.DuplicateRealmException e )
        {
            e.printStackTrace();
        }
        Configuration configuration = new DefaultConfiguration();
        configuration.setClassWorld( classWorld );
        configuration.setLocalRepository(
            new File( System.getProperty( "user.home" ) + File.separator + ".m2/repository" ) );

        File localRepository = new File( System.getProperty( "user.home" ), ".m2" );
        File userSettingsPath = new File( localRepository, "settings.xml" );
        configuration.setUserSettingsFile( userSettingsPath );
        configuration.setMavenEmbedderLogger( null );

        try
        {
            embedder = new MavenEmbedder( configuration );
        }
        catch ( MavenEmbedderException e )
        {
            e.printStackTrace();
        }
    }

    /**
     * @see org.codehaus.plexus.personality.plexus.lifecycle.phase.Disposable#dispose()
     */
    public void dispose()
    {
        try
        {
            embedder.stop();
        }
        catch ( MavenEmbedderException e )
        {
            e.printStackTrace();
        }
    }

    /**
     * Resets the port that the socket logger will use to write out its log messages.
     *
     * @param port the socket logger port
     */
    private void resetSocket( int port )
        throws EmbedderException
    {
        logger.info( "NPANDAY: Resetting logger port: " + port );
        SocketLoggerManager socketLoggerManager =
            (SocketLoggerManager) embedder.getPlexusContainer().getLoggerManager();
        SocketLogger socketLogger = (SocketLogger) socketLoggerManager.createLogger( "90" );
        try
        {
            socketLogger.setHandlerFor( port );
        }
        catch ( IOException e )
        {
            throw new EmbedderException( "NPANDAY: Failed to set socket handler port: Port = " + port, e );
        }
    }

    /**
     * Returns a set of all maven projects, including the specified maven projects and their child projects.
     *
     * @param mavenProjects the maven projects
     * @return a set of all maven projects, including child projects of the specified maven projects
     */
    private Set<MavenProject> getFlatSetFor( Set<MavenProject> mavenProjects )
    {
        Set<MavenProject> flatSet = new HashSet<MavenProject>();
        flatSet.addAll( mavenProjects );
        for ( MavenProject mavenProject : mavenProjects )
        {
            flatSet.addAll( getFlatSetFor( mavenProject.getMavenProjects() ) );
        }
        return flatSet;
    }

    /**
     * Returns a set of maven projects that follow the specified pattern under the specified root directory.
     *
     * @param rootDirectory the root directory from which to look for pom files
     * @param pattern       the pattern of the pom files
     * @return a set of maven projects that follow the specified pattern under the specified root directory
     */
    private Set<MavenProject> getMavenProjectsFrom( String rootDirectory, String pattern )
        throws EmbedderException
    {
        DirectoryScanner scanner = new DirectoryScanner();
        scanner.setBasedir( rootDirectory );
        scanner.setIncludes( new String[]{pattern} );
        scanner.scan();

        Set<MavenProject> mavenProjects = new HashSet<MavenProject>();
        for ( String pomFile : scanner.getIncludedFiles() )
        {
            MavenProjectImpl mavenProject = new MavenProjectImpl();
            Model model;
            try
            {
                model = embedder.readModel( new File( rootDirectory, pomFile ) );
            }
            catch ( XmlPullParserException e )
            {
                throw new EmbedderException( "NPANDAY: Failed to read model: Pom  File = " + pomFile );
            }
            catch ( IOException e )
            {
                throw new EmbedderException( "NPANDAY: Failed to read model: Pom  File = " + pomFile );
            }

            if ( model == null )
            {
                throw new EmbedderException( "NPANDAY: Failed to read model - value is null: Pom  File = " + pomFile );
            }

            mavenProject.setArtifactId( model.getArtifactId() );
            mavenProject.setGroupId( model.getGroupId() );
            mavenProject.setVersion( model.getVersion() );
            mavenProject.setPomPath( new File( rootDirectory, pomFile ).getAbsolutePath() );
            mavenProject.setModel( model );
            mavenProjects.add( mavenProject );

        }
        return mavenProjects;
    }

    /**
     * Returns a set of artifact that follow the specified pattern under the specified root directory.
     * For this directory, the root directory is most likely the root local maven repository.
     *
     * @param rootDirectory the root directory from which to look for artifacts
     * @param pattern       the pattern of the pom files
     * @return a set of artifacts that follow the specified pattern under the specified root directory
     */
    private Set<Artifact> getArtifactsFrom( String rootDirectory, String pattern )
    {
        DirectoryScanner scanner = new DirectoryScanner();
        scanner.setBasedir( rootDirectory );
        scanner.setIncludes( new String[]{pattern} );
        scanner.scan();

        Set<Artifact> artifacts = new HashSet<Artifact>();

        for ( String pomFile : scanner.getIncludedFiles() )
        {
            ArtifactImpl artifact = new ArtifactImpl();
            Model model = null;
            try
            {
                model = embedder.readModel( new File( rootDirectory, pomFile ) );
            }
            catch ( XmlPullParserException e )
            {
                e.printStackTrace();
            }
            catch ( IOException e )
            {
                e.printStackTrace();
            }
            if ( model != null && ( model.getPackaging().equals( "library" ) || model.getPackaging().equals( "exe" ) ||
                model.getPackaging().equals( "netmodule" ) || model.getPackaging().equals( "winexe" ) ||
                model.getPackaging().equals( "visual-studio-addin" ) ||
                model.getPackaging().equals( "sharp-develop-addin" ) || model.getPackaging().equals( "nar" ) ||
                model.getPackaging().equals( "netplugin" ) ) )
            {
                artifact.setArtifactId( model.getArtifactId() );
                artifact.setGroupId( model.getGroupId() );
                artifact.setVersion( model.getVersion() );
                artifact.setPomPath( new File( rootDirectory, pomFile ).getAbsolutePath() );
                artifact.setModel( model );
                artifacts.add( artifact );
            }
        }
        return artifacts;
    }

    private MavenProject getMavenProjectForPomFile( File pomFile, Set<MavenProject> allMavenProjects )
        throws EmbedderException
    {
        for ( MavenProject mavenProject : allMavenProjects )
        {
            if ( pomFile.getAbsolutePath().equals( mavenProject.getPomPath() ) )
            {
                return mavenProject;
            }
        }
        throw new EmbedderException( "NPANDAY: Could not find a matching maven project: Pom File = " +
            pomFile.getAbsolutePath() + " Maven Project File Count = " + allMavenProjects.size() );
    }

    private Set<MavenProject> attachMavenProjectsFor( File pomFile, Set<MavenProject> allMavenProjects )
        throws EmbedderException
    {
        Set<MavenProject> attachedMavenProjects = new HashSet<MavenProject>();
        MavenProjectImpl mavenProject = (MavenProjectImpl) getMavenProjectForPomFile( pomFile, allMavenProjects );
        attachedMavenProjects.add( mavenProject );
        Model model = mavenProject.getModel();
        List<String> modules = ( model != null ) ? model.getModules() : new ArrayList<String>();
        Set<MavenProject> childProjects = new HashSet<MavenProject>();
        for ( String module : modules )
        {
            File moduleFile = new File( pomFile.getParent() + "/" + module + "/pom.xml" );
            MavenProject childProject = getMavenProjectForPomFile( moduleFile, allMavenProjects );
            childProjects.add( childProject );
            attachMavenProjectsFor( moduleFile, allMavenProjects );
        }
        mavenProject.setMavenProjects( childProjects );
        return attachedMavenProjects;
    }

    /**
     * Prints out the maven project artifact ids for the specified maven projects, with indents for children projects
     *
     * @param mavenProjects the maven projects to print information about
     * @param indent        the number of dashes to use for each child node iteration
     */
    private void printProjects( Set<MavenProject> mavenProjects, int indent )
    {
        StringBuffer dashes = new StringBuffer();
        for ( int i = 0; i < indent; i++ )
        {
            dashes.append( "--" );
        }
        for ( MavenProject mavenProject : mavenProjects )
        {
            System.out.println( dashes.toString() + mavenProject.getArtifactId() );
            printProjects( mavenProject.getMavenProjects(), indent + 1 );
        }
    }
}
