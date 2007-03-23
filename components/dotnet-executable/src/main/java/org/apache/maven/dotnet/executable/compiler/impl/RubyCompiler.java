package org.apache.maven.dotnet.executable.compiler.impl;

import org.apache.maven.dotnet.executable.ExecutionException;
import org.apache.maven.dotnet.executable.compiler.CompilerConfig;
import org.apache.maven.dotnet.vendor.Vendor;

import java.util.List;
import java.util.ArrayList;
import java.io.File;

public final class RubyCompiler
    extends BaseCompiler
{
    public boolean failOnErrorOutput()
    {
        //MONO writes warnings to standard error: this turns off failing builds on warnings for MONO
        return !compilerContext.getCompilerRequirement().getVendor().equals( Vendor.MONO );
    }

    public List<String> getCommands()
        throws ExecutionException
    {
        if ( compilerContext == null )
        {
            throw new ExecutionException( "NMAVEN-068-000: Compiler has not been initialized with a context" );
        }
        List<String> commands = new ArrayList<String>();

        CompilerConfig config = compilerContext.getNetCompilerConfig();
        String sourceDirectory = compilerContext.getSourceDirectoryName();
        File srcDir = new File( sourceDirectory );
        commands.add( "--" + config.getArtifactType().getExtension() );
        for ( String command : config.getCommands() )
        {
            if ( command.startsWith( "main:" ) )
            {   String className = command.split( "[:]" )[1];
                File classFile = new File("target/build-sources/" + className);
                commands.add( "'" + classFile.getAbsolutePath() + "'");
            }
            else
            {
                commands.add( command );
            }
        }
        // commands.add("-Cdirectory");
        // commands.add();
        //commands.addAll( config.getCommands() );
        return commands;
    }
}
