package org.apache.maven.dotnet.embedder.impl;

import org.apache.maven.dotnet.embedder.MavenEmbedderService;
import org.apache.maven.dotnet.embedder.MavenExecutionRequest;
import org.apache.maven.embedder.MavenEmbedder;
import org.apache.maven.embedder.MavenEmbedderException;
import org.apache.maven.embedder.MavenEmbedderConsoleLogger;
import org.apache.maven.embedder.PlexusLoggerAdapter;
import org.apache.maven.embedder.MavenEmbedderLogger;
import org.apache.maven.BuildFailureException;
import org.apache.maven.cli.ConsoleDownloadMonitor;
import org.apache.maven.cli.MavenCli;
import org.apache.maven.monitor.event.EventMonitor;
import org.apache.maven.monitor.event.DefaultEventMonitor;
import org.apache.maven.lifecycle.LifecycleExecutionException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.ProjectBuildingException;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Disposable;
import org.codehaus.plexus.util.dag.CycleDetectedException;
import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.classworlds.ClassWorld;
import org.codehaus.classworlds.DuplicateRealmException;

import java.util.List;
import java.util.ArrayList;
import java.util.Properties;
import java.io.File;

public final class MavenEmbedderServiceImpl
    implements MavenEmbedderService, Initializable, Disposable, LogEnabled
{

    private MavenEmbedder embedder;

    private Logger logger;

    private MavenEmbedderConsoleLogger embedderLogger;

    public MavenEmbedderServiceImpl()
    {
    }

    public void enableLogging( Logger logger )
    {
        this.logger = logger;
    }

    /*
     *  Uses the CLI
     */
    public void execute( MavenExecutionRequest request )
    {
        String args[] = new String[3];
        args[0] = "install";
        File pomFile = new File( "..\\dotnet-artifact\\pom.xml" );
        args[1] = "-f";
        args[2] = pomFile.getAbsolutePath();
        ClassWorld classWorld = new ClassWorld();
        try
        {
            classWorld.newRealm( "plexus.core", Thread.currentThread().getContextClassLoader() );
        }
        catch ( DuplicateRealmException e )
        {
            e.printStackTrace();
        }
        MavenCli.main( args, classWorld );

    }

    /*
     * Uses the embedder
     */
    public void execute1( MavenExecutionRequest request )
    {

        MavenProject mavenProject = null;
        try
        {
            mavenProject = embedder.readProject( new File( request.getPomFile() ) );
        }
        catch ( ProjectBuildingException e )
        {
            logger.info( "", e );
        }
        List<String> goals = new ArrayList<String>();
        goals.add( "install" );
        File executionRootDirectory =
            new File(new File( request.getPomFile() ).getParent());
        EventMonitor eventMonitor =
            new DefaultEventMonitor( new PlexusLoggerAdapter( new MavenEmbedderConsoleLogger() ) );

        try
        {
            embedder.execute( mavenProject, goals, eventMonitor, new ConsoleDownloadMonitor(), new Properties(),
                              executionRootDirectory );
        }
        catch ( CycleDetectedException e )
        {
            e.printStackTrace();
        }
        catch ( LifecycleExecutionException e )
        {
            e.printStackTrace();
        }
        catch ( BuildFailureException e )
        {
            printFullTrace( e );
        }
        catch ( org.apache.maven.project.DuplicateProjectException e )
        {
            e.printStackTrace();
        }
    }

    private void printFullTrace( Throwable t )
    {
        Throwable cause = t.getCause();
        if ( cause != null )
        {
            printFullTrace( cause );
        }
    }

    public void initialize()
    {
        embedderLogger = new MavenEmbedderConsoleLogger();
        embedderLogger.setThreshold( MavenEmbedderLogger.LEVEL_DEBUG );
        embedder = new MavenEmbedder();

        embedder.setClassLoader( Thread.currentThread().getContextClassLoader() );
        embedder.setLogger( embedderLogger );
        embedder.setAlignWithUserInstallation( true );
        embedder.setLocalRepositoryDirectory( new File( System.getProperty( "user.home" ) + File.separator + ".m2" ) );

        try
        {
            embedder.start();
        }
        catch ( MavenEmbedderException e )
        {
            e.printStackTrace();
        }
    }

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
}
