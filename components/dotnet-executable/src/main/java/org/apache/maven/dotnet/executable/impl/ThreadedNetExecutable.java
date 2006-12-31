package org.apache.maven.dotnet.executable.impl;

import org.apache.maven.dotnet.executable.NetExecutable;
import org.apache.maven.dotnet.executable.ExecutableContext;
import org.apache.maven.dotnet.executable.ExecutionException;
import org.apache.maven.dotnet.executable.CommandFilter;
import org.apache.maven.dotnet.executable.CommandExecutor;
import org.apache.maven.dotnet.NMavenContext;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.logging.Logger;

import java.util.List;
import java.io.File;


public class ThreadedNetExecutable
    implements NetExecutable, Runnable
{
    private ExecutableContext executableContext;

    private MavenProject project;

    private Logger logger;

    public void run()
    {
        CommandExecutor commandExecutor = CommandExecutor.Factory.createDefaultCommmandExecutor();
        try
        {
            commandExecutor.setLogger( logger );
            commandExecutor.executeCommand( getExecutable(), getCommands(), getExecutionPath(), true );
        }
        catch ( ExecutionException e )
        {
          //  throw new ExecutionException( "NMAVEN-063-000: Command = " + commands, e );
        }
        if ( commandExecutor.getStandardOut().contains( "error" ) )
        {
         //   t/w new ExecutionException( "NMAVEN-063-001: Command = " + commands );
        }
    }

    public List<String> getCommands()
        throws ExecutionException
    {
        CommandFilter filter = executableContext.getCommandFilter();
        return filter.filter( executableContext.getExecutableConfig().getCommands() );
    }

    public File getExecutionPath()
    {
        return ( executableContext.getExecutableConfig().getExecutionPath() != null ) ? new File(
            executableContext.getExecutableConfig().getExecutionPath() ) : null;
    }

    public void execute()
        throws ExecutionException
    {
    }


    public String getExecutable()
        throws ExecutionException
    {
        if ( executableContext == null )
        {
            throw new ExecutionException( "NMAVEN-063-002: Executable has not been initialized with a context" );
        }
        return executableContext.getExecutableCapability().getExecutable();
    }


    public void init( NMavenContext nmavenContext )
    {
        this.executableContext = (ExecutableContext) nmavenContext;
        this.project = executableContext.getMavenProject();
        this.logger = executableContext.getLogger();
    }
}
