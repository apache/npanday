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
package org.apache.maven.dotnet.executable.compiler.impl;

import org.apache.maven.dotnet.executable.NetExecutable;
import org.apache.maven.dotnet.executable.ExecutionException;
import org.apache.maven.dotnet.NMavenContext;
import org.apache.maven.dotnet.executable.CommandExecutor;
import org.apache.maven.dotnet.executable.compiler.CompilerContext;
import org.apache.maven.dotnet.executable.compiler.InvalidArtifactException;
import org.apache.maven.dotnet.executable.compiler.CompilerExecutable;

import java.util.List;
import java.io.File;

import org.codehaus.plexus.logging.Logger;

/**
 * A compiler to be used for compiling with .NET Profiles.
 *
 * @author Shane Isbell
 */
public final class CSharpCompilerForProfile
    implements CompilerExecutable
{

    private NetExecutable netCompiler;

    private CompilerContext compilerContext;

    public CSharpCompilerForProfile()
    {
        netCompiler = new DefaultCompiler();
    }

    public File getCompiledArtifact()
        throws InvalidArtifactException
    {
        File file = compilerContext.getArtifact();
        if ( !file.exists() )
        {
            throw new InvalidArtifactException(
                "NMAVEN-067-003: Artifact does not exist: Artifact = " + file.getAbsolutePath() );
        }
        return file;
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
        Logger logger = compilerContext.getLogger();
        if ( !( new File( compilerContext.getSourceDirectoryName() ).exists() ) )
        {
            logger.info( "NMAVEN-067-000: No source files to compile." );
            return;
        }
        logger.info( "NMAVEN-067-001: Compiling Artifact: Vendor = " +
            compilerContext.getCompilerRequirement().getVendor() + ", Language = " +
            compilerContext.getCompilerRequirement().getVendor() + ", Assembly Name = " +
            compilerContext.getArtifact().getAbsolutePath() );

        CommandExecutor commandExecutor = CommandExecutor.Factory.createDefaultCommmandExecutor();
        commandExecutor.setLogger( logger );
        commandExecutor.executeCommand( getExecutable(), getCommands(), getExecutionPath(), true );
    }

    public List<String> getCommands()
        throws ExecutionException
    {
        File path = new File( compilerContext.getCompilerCapability().getAssemblyPath() );
        if ( !path.exists() )
        {
            throw new ExecutionException(
                "NMAVEN-067-002: The assembly path does not exist: Path = " + path.getAbsolutePath() );
        }

        List<String> commands = netCompiler.getCommands();
        commands.add( "/nostdlib+" );
        commands.add( "/noconfig" );
        for ( String coreAssembly : compilerContext.getCoreAssemblyNames() )
        {
            commands.add( "/reference:" + path.getAbsolutePath() + File.separator + coreAssembly + ".dll" );
        }
        return commands;
    }


    public String getExecutable()
        throws ExecutionException
    {
        return netCompiler.getExecutable();
    }

    public void init( NMavenContext nmavenContext )
    {
        netCompiler.init( nmavenContext );
        this.compilerContext = (CompilerContext) nmavenContext;
    }
}
