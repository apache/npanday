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

import org.apache.maven.dotnet.executable.compiler.CompilerContext;
import org.apache.maven.dotnet.executable.compiler.InvalidArtifactException;
import org.apache.maven.dotnet.executable.compiler.CompilerExecutable;
import org.apache.maven.dotnet.executable.ExecutionException;
import org.apache.maven.dotnet.executable.CommandExecutor;
import org.apache.maven.dotnet.NMavenContext;
import org.apache.maven.dotnet.vendor.Vendor;
import org.codehaus.plexus.logging.Logger;

import java.io.File;
import java.util.List;

/**
 * Provides an implementation of the compiler executable.
 *
 * @author Shane Isbell
 */
//TODO: Describe how this class should be extended
abstract class BaseCompiler implements CompilerExecutable
{
    /**
     * The context that the compiler implementation can use to obtain information to customize the compile.
     */
    protected CompilerContext compilerContext;

    /**
     * A logger for writing log messages
     */
    protected Logger logger;

    /**
     * This method may be overridden if the developer needs to create a profile of one of the other compilers.
     */
    public void init( NMavenContext nmavenContext )
    {
        this.compilerContext = (CompilerContext) nmavenContext;
        this.logger = nmavenContext.getLogger();
    }

    /**
     * @see org.apache.maven.dotnet.executable.compiler.CompilerExecutable#getCompiledArtifact()
     */
    public File getCompiledArtifact()
        throws InvalidArtifactException
    {
        return compilerContext.getArtifact();
    }

    /**
     * @see org.apache.maven.dotnet.executable.compiler.CompilerExecutable#getExecutable()
     */
    public String getExecutable()
        throws ExecutionException
    {
        if ( compilerContext == null )
        {
            throw new ExecutionException( "NMAVEN-068-001: Compiler has not been initialized with a context" );
        }
        return compilerContext.getCompilerCapability().getExecutable();
    }

    /**
     * @see org.apache.maven.dotnet.executable.compiler.CompilerExecutable#getExecutionPath()
     */
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

    /**
     * @see org.apache.maven.dotnet.executable.compiler.CompilerExecutable#execute()
     */
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

    public Vendor getVendor()
    {
        return compilerContext.getCompilerCapability().getVendor();
    }
}
