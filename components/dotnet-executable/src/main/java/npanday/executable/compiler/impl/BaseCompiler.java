package npanday.executable.compiler.impl;

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

import npanday.NPandayContext;
import npanday.PathUtil;
import npanday.PlatformUnsupportedException;
import npanday.executable.CommandExecutor;
import npanday.executable.ExecutionException;
import npanday.executable.ExecutionResult;
import npanday.executable.compiler.CompilerContext;
import npanday.executable.compiler.CompilerExecutable;
import npanday.executable.compiler.InvalidArtifactException;
import npanday.vendor.Vendor;
import org.codehaus.plexus.logging.Logger;

import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.Properties;

/**
 * Provides an implementation of the compiler executable.
 *
 * @author Shane Isbell
 */
//TODO: Describe how this class should be extended
abstract class BaseCompiler
    implements CompilerExecutable
{
    /**
     * The context that the compiler implementation can use to obtain information to customize the compile.
     */
    protected CompilerContext compilerContext;

    /**
     * A logger for writing log messages
     */
    protected Logger logger;

    protected Properties configuration;

    /**
     * This method may be overridden if the developer needs to create a profile of one of the other compilers.
     */
    public void init( NPandayContext npandayContext, Properties properties )
    {
        configuration = properties;
        this.compilerContext = (CompilerContext) npandayContext;
        this.logger = npandayContext.getLogger();
    }

    /**
     * @see npanday.executable.compiler.CompilerExecutable#getTargetFramework()
     */
    public String getTargetFramework()
    {
        return compilerContext.getTargetFramework();
    }

    /**
     * @see npanday.executable.compiler.CompilerExecutable#getCompiledArtifact()
     */
    public File getAssemblyPath()
    {
        return compilerContext.getAssemblyPath();
    }

    /**
     * @see npanday.executable.compiler.CompilerExecutable#getCompiledArtifact()
     */
    public File getCompiledArtifact() throws InvalidArtifactException
    {
        return compilerContext.getArtifact();
    }

    public String getExecutable() throws ExecutionException
    {
        if ( compilerContext == null )
        {
            throw new ExecutionException( "NPANDAY-068-001: Compiler has not been initialized with a context" );
        }
        return compilerContext.getExecutableName();
    }

    /**
     * @see npanday.executable.compiler.CompilerExecutable#execute()
     */
    public ExecutionResult execute() throws ExecutionException, PlatformUnsupportedException
    {
        logger.info(
            "NPANDAY-068-003: Compiling Artifact: Vendor = " + compilerContext.getVendor() + ", Language = "
                + compilerContext.getVendor() + ", Assembly Name = " + compilerContext.getArtifact().getAbsolutePath()
        );

        String executable = getExecutable();
        Collection<String> executablePaths = compilerContext.getProbingPaths();

        CommandExecutor commandExecutor = CommandExecutor.Factory.createDefaultCommmandExecutor(
           configuration
        );
        commandExecutor.setLogger( logger );
        commandExecutor.executeCommand(
            PathUtil.getExecutable( executable, executablePaths, logger ), getCommands(), null, failOnErrorOutput()
        );

        return new ExecutionResult(
            commandExecutor.getResult(),
            commandExecutor.getStandardOut(),
            commandExecutor.getStandardError()
        );
    }

    protected abstract List<String> getCommands() throws ExecutionException, PlatformUnsupportedException;

    public Vendor getVendor()
    {
        return compilerContext.getVendor();
    }
}
