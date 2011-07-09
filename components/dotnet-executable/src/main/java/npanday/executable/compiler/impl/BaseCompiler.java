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
package npanday.executable.compiler.impl;

import npanday.PathUtil;
import npanday.executable.compiler.CompilerContext;
import npanday.executable.compiler.InvalidArtifactException;
import npanday.executable.compiler.CompilerExecutable;
import npanday.executable.ExecutionException;
import npanday.executable.CommandExecutor;
import npanday.NPandayContext;
import npanday.vendor.Vendor;
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
    public void init( NPandayContext npandayContext )
    {
        this.compilerContext = (CompilerContext) npandayContext;
        this.logger = npandayContext.getLogger();
    }

    /**
     * @see npanday.executable.compiler.CompilerExecutable#getTargetFramework()
     */
    public String getTargetFramework()
    {
        return compilerContext.getCompilerCapability().getTargetFramework();
    }

    /**
     * @see npanday.executable.compiler.CompilerExecutable#getCompiledArtifact()
     */
    public File getCompiledArtifact()
        throws InvalidArtifactException
    {
        return compilerContext.getArtifact();
    }

    /**
     * @see npanday.executable.compiler.CompilerExecutable#getExecutable()
     */
    public String getExecutable()
        throws ExecutionException
    {
        if ( compilerContext == null )
        {
            throw new ExecutionException( "NPANDAY-068-001: Compiler has not been initialized with a context" );
        }
        return compilerContext.getCompilerCapability().getExecutable();
    }

    /**
     * @see npanday.executable.compiler.CompilerExecutable#getExecutionPath()
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
				if ( PathUtil.containsExecutable(executablePath, executable) )
                {
                    logger.info("NPANDAY-068-005: Found executable path for " + executable + ": " + executablePath);
                    return new File(executablePath);
                }
            }
        }
        logger.warn("NPANDAY-068-006: Did not find path for " + executable + " in " + executablePaths);
        return null;
    }

    /**
     * @see npanday.executable.compiler.CompilerExecutable#execute()
     */
    public void execute()
        throws ExecutionException
    {
        if (compilerContext.getNetCompilerConfig().getIncludeSources() ==null && !( new File( compilerContext.getSourceDirectoryName() ).exists() ) )
        {
            logger.info( "NPANDAY-068-002: No source files to compile." );
            return;
        }
        logger.info( "NPANDAY-068-003: Compiling Artifact: Vendor = " +
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
