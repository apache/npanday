package org.apache.maven.dotnet.executable.compiler.impl;

import org.apache.maven.dotnet.executable.compiler.CompilerContext;
import org.apache.maven.dotnet.executable.compiler.InvalidArtifactException;
import org.apache.maven.dotnet.executable.compiler.CompilerExecutable;
import org.apache.maven.dotnet.executable.ExecutionException;
import org.apache.maven.dotnet.executable.CommandExecutor;
import org.apache.maven.dotnet.NMavenContext;
import org.codehaus.plexus.logging.Logger;

import java.io.File;
import java.util.List;

/**
 *
 */
abstract class BaseCompiler implements CompilerExecutable
{
    protected CompilerContext compilerContext;

    protected Logger logger;

    /**
     * This method may be overridden if the developer needs to create a profile of one of the other compilers.
     */
    public void init( NMavenContext nmavenContext )
    {
        this.compilerContext = (CompilerContext) nmavenContext;
        this.logger = nmavenContext.getLogger();
    }

    public File getCompiledArtifact()
        throws InvalidArtifactException
    {
        File file = compilerContext.getArtifact();
        if ( !file.exists() )
        {
            throw new InvalidArtifactException(
                "NMAVEN-068-004: Artifact does not exist: Artifact = " + file.getAbsolutePath() );
        }
        return file;
    }

    public String getExecutable()
        throws ExecutionException
    {
        if ( compilerContext == null )
        {
            throw new ExecutionException( "NMAVEN-068-001: Compiler has not been initialized with a context" );
        }
        return compilerContext.getCompilerCapability().getExecutable();
    }

    public File getExecutionPath()
    {
        String executable;
        try
        {
            executable = getExecutable();
        }
        catch ( ExecutionException e )
        {
            return null;
        }
        List<String> executablePaths = compilerContext.getNetCompilerConfig().getExecutionPaths();
        if ( executablePaths != null )
        {
            for ( String executablePath : executablePaths )
            {
                File exe = new File( executablePath + File.separator +  executable);
                if ( exe.exists() )
                {
                    return new File(executablePath);
                }
            }
        }
        return null;
    }

    public void execute()
        throws ExecutionException
    {
        if ( !( new File( compilerContext.getSourceDirectoryName() ).exists() ) )
        {
            logger.info( "NMAVEN-068-002: No source files to compile." );
            return;
        }
        logger.info( "NMAVEN-068-003: Compiling Artifact: Vendor = " +
            compilerContext.getCompilerRequirement().getVendor() + ", Language = " +
            compilerContext.getCompilerRequirement().getVendor() + ", Assembly Name = " +
            compilerContext.getArtifact().getAbsolutePath() );

        CommandExecutor commandExecutor = CommandExecutor.Factory.createDefaultCommmandExecutor();
        commandExecutor.setLogger( logger );
        commandExecutor.executeCommand( getExecutable(), getCommands(), getExecutionPath(), failOnErrorOutput() );
    }
}
