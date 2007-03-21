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

import org.apache.maven.dotnet.executable.ExecutionException;
import org.apache.maven.dotnet.NMavenContext;
import org.apache.maven.dotnet.executable.CommandExecutor;
import org.apache.maven.dotnet.executable.compiler.CompilerConfig;
import org.apache.maven.dotnet.executable.compiler.CompilerContext;
import org.apache.maven.dotnet.executable.compiler.InvalidArtifactException;
import org.apache.maven.dotnet.executable.compiler.CompilerExecutable;

import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.io.File;

import org.apache.maven.artifact.Artifact;
import org.codehaus.plexus.logging.Logger;

/**
 * Compiler for DotGNU.
 *
 * @author Shane Isbell
 */
public final class DotGNUCompiler
    implements CompilerExecutable
{

    private CompilerContext compilerContext;

    private Logger logger;

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
                "NMAVEN-069-005: Artifact does not exist: Artifact = " + file.getAbsolutePath() );
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

    public List<String> getCommands()
        throws ExecutionException
    {
        if ( compilerContext == null )
        {
            throw new ExecutionException( "NMAVEN-069-000: Compiler has not been initialized with a context" );
        }
        CompilerConfig config = compilerContext.getNetCompilerConfig();
        List<Artifact> resources = compilerContext.getLibraryDependencies();
        List<Artifact> modules = compilerContext.getDirectModuleDependencies();

        String sourceDirectory = compilerContext.getSourceDirectoryName();
        String artifactFilePath = compilerContext.getArtifact().getAbsolutePath();
        String targetArtifactType = config.getArtifactType().getArtifactTypeName();

        List<String> commands = new ArrayList<String>();
        commands.add( "/out:" + artifactFilePath );
        commands.add( "/target:" + targetArtifactType );
        commands.add( "/recurse:" + sourceDirectory + File.separator + "**" );
        if ( !modules.isEmpty() )
        {
            StringBuffer sb = new StringBuffer();
            for ( Iterator i = modules.iterator(); i.hasNext(); )
            {
                Artifact artifact = (Artifact) i.next();
                String path = artifact.getFile().getAbsolutePath();
                sb.append( path );
                if ( i.hasNext() )
                {
                    sb.append( ";" );
                }
            }
        }
        if ( !resources.isEmpty() )
        {
            for ( Artifact artifact : resources )
            {
                String path = artifact.getFile().getAbsolutePath();
                commands.add( "/reference:" + path );
            }
        }

        for ( File file : compilerContext.getEmbeddedResources() )
        {
            commands.add( "/resource:" + file.getAbsolutePath() );
        }
        if ( config.getCommands() != null )
        {
            commands.addAll( config.getCommands() );
        }
        //TODO: Apply command filter
        return commands;
    }

    public String getExecutable()
        throws ExecutionException
    {
        if ( compilerContext == null )
        {
            throw new ExecutionException( "NMAVEN-069-001: Compiler has not been initialized with a context" );
        }
        return compilerContext.getCompilerCapability().getExecutable();
    }

    public void execute()
        throws ExecutionException
    {
        logger.info( "NMAVEN-069-002: Compiling" );
        if ( !( new File( compilerContext.getSourceDirectoryName() ).exists() ) )
        {
            logger.info( "NMAVEN-069-003: No source files to compile." );
            return;
        }
        CommandExecutor commandExecutor = CommandExecutor.Factory.createDefaultCommmandExecutor();
        commandExecutor.setLogger( logger );
        commandExecutor.executeCommand( getExecutable(), getCommands(), getExecutionPath(), true );
        logger.info( "NMAVEN-069-004: Compiling Artifact: Vendor = " +
            compilerContext.getCompilerRequirement().getVendor() + ", Language = " +
            compilerContext.getCompilerRequirement().getVendor() + ", Assembly Name = " +
            compilerContext.getArtifact().getAbsolutePath() );
    }

}
