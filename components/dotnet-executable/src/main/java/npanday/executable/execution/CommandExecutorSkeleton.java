package npanday.executable.execution;

import npanday.executable.CommandExecutor;
import npanday.executable.ExecutionException;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.logging.console.ConsoleLogger;

import java.io.File;
import java.util.List;

/**
 * @author <a href="mailto:lcorneliussen@apache.org">Lars Corneliussen</a>
 */
public abstract class CommandExecutorSkeleton
    implements CommandExecutor
{
    /**
     * Instance of a plugin logger.
     */
    private Logger logger;

    public void setLogger( Logger logger )
    {
        this.logger = logger;
    }

    public void executeCommand( String executable, List<String> commands ) throws ExecutionException
    {
        executeCommand( executable, commands, null, true );
    }

    public void executeCommand( String executable, List<String> commands, boolean failsOnErrorOutput ) throws
        ExecutionException
    {
        executeCommand( executable, commands, null, failsOnErrorOutput );
    }

    public abstract void executeCommand(
        String executable, List<String> commands, File workingDirectory, boolean failsOnErrorOutput ) throws
        ExecutionException;

    public Logger getLogger()
    {
        if (logger == null)
            logger = new ConsoleLogger( Logger.LEVEL_DEBUG, "implicit");

        return logger;
    }
}
