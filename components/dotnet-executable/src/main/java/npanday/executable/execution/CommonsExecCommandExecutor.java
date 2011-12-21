package npanday.executable.execution;

import com.google.common.base.Joiner;
import npanday.executable.ExecutionException;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteException;
import org.apache.commons.exec.PumpStreamHandler;
import org.apache.commons.exec.util.StringUtils;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;

/**
 * New command executor based on commons-exec instead of plexus-utils.
 *
 * @author <a href="mailto:lcorneliussen@apache.org">Lars Corneliussen</a>
 */
public class CommonsExecCommandExecutor
    extends CommandExecutorSkeleton
{
    private static final Joiner JOIN_ON_SPACE = Joiner.on( " " );

    private OutputStream standardOutHandler;

    private OutputStream errorOutHandler;

    private int result;

    private boolean hasErrorOutput = false;

    private CommandLine commandLine;

    private boolean hasLoggedStartInfo;

    public CommonsExecCommandExecutor()
    {
        setupOutputHandlers();
    }

    @Override
    public void executeCommand(
        String executable, List<String> commands, File workingDirectory, boolean failsOnErrorOutput ) throws
        ExecutionException
    {
        // TODO: This is for Windows only; in context of NPANDAY-509

        /*
         * We have to use cmd /X /C "...", because only then
         * we can pass arguments like -x="a b" - which, in this case,
         * was required by MSDeploy.
         *
         * Without cmd, using commons-exc or plexus-utils' cli, -x="a b" will
         * get quoted once more.
         *
         * Consideration: We could also exchange the  DefaultExecutor.launcher; but it is private :/
         */

        commandLine = new CommandLine( "cmd" );

        commandLine.addArgument( "/X" );
        commandLine.addArgument( "/C" );

        List<String> shellArgs = newArrayList();

        shellArgs.add( StringUtils.quoteArgument( executable ) );

        for ( String arg : commands )
        {
            // NPANDAY-509: if an argument ends with " or ', we are quite sure,
            // quoting has been taken care of on the outside
            final boolean endsWithQuote = arg.endsWith( "'" ) || arg.endsWith( "\"" );
            final boolean handleQuoting = endsWithQuote ? false : true;

            if ( handleQuoting )
            {
                shellArgs.add( StringUtils.quoteArgument( arg ) );
            }
            else
            {
                shellArgs.add( arg );
            }
        }

        // cmd \X \C "<doesn't want the "quotes" to be escaped here>"
        commandLine.addArgument( "\"" + JOIN_ON_SPACE.join( shellArgs ) + "\"", false);

        DefaultExecutor executor = new DefaultExecutor();
        executor.setExitValue( 0 );

        executor.setStreamHandler( new PumpStreamHandler( standardOutHandler, errorOutHandler ) );

        if ( workingDirectory != null )
        {
            executor.setWorkingDirectory( workingDirectory );
        }

        try
        {
            result = executor.execute( commandLine );
            mayLogEndInfo();
        }
        catch ( ExecuteException e )
        {
            throw new ExecutionException(
                "NPANDAY-125-002: Error occurred when executing " + commandLine.toString(), e
            );
        }
        catch ( IOException e )
        {
            throw new ExecutionException(
                "NPANDAY-125-000: IO error occurred when executing " + commandLine.toString(), e
            );
        }

        if ( failsOnErrorOutput && hasErrorOutput )
        {
            throw new ExecutionException(
                "NPANDAY-125-001: Execution passed, but had error outputs: " + commandLine.toString()
            );
        }
    }

    private void setupOutputHandlers()
    {
        standardOutHandler = new CapturingLogOutputStream()
        {
            protected void processLine( String line )
            {
                mayLogStartInfo();
                getLogger().info( "| " + line );
            }
        };

        errorOutHandler = new CapturingLogOutputStream()
        {
            @Override
            protected void processLine( String line )
            {
                mayLogStartInfo();
                getLogger().error( "| " + line );
                hasErrorOutput = true;
            }
        };
    }

    private void mayLogStartInfo()
    {
        if (!hasLoggedStartInfo)
        {
            getLogger().info( "+--[ RUNNING: " + commandLine  + "]");
        }
        hasLoggedStartInfo = true;
    }

    private void mayLogEndInfo()
    {
        if (hasLoggedStartInfo)
        {
           getLogger().info( "+--[ DONE" + (hasErrorOutput ? " WITH ERRORS" : "") + " ]" );
        }
    }

    public int getResult()
    {
        return result;
    }

    public String getStandardOut()
    {
        return standardOutHandler.toString();
    }

    public String getStandardError()
    {
        return errorOutHandler.toString();
    }
}
