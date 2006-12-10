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
import org.apache.maven.artifact.Artifact;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.logging.Logger;

import java.util.List;
import java.util.ArrayList;
import java.io.File;

/**
 * Compiler for PHP (http://php4mono.sourceforge.net/)
 *
 * @author Shane Isbell
 */
public final class PhpCompiler
    implements CompilerExecutable
{

    private CompilerContext compilerContext;

    public void init( NMavenContext nmavenContext )
    {
        this.compilerContext = (CompilerContext) nmavenContext;
    }

    public File getExecutionPath()
    {
        return new File( compilerContext.getNetCompilerConfig().getExecutionPath() );
    }

    public List<String> getCommands()
        throws ExecutionException
    {
        return null;
    }

    public String getExecutable()
        throws ExecutionException
    {
        return null;
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

    public void execute()
        throws ExecutionException
    {
        Logger logger = compilerContext.getLogger();
        CompilerConfig config = compilerContext.getNetCompilerConfig();
        List<Artifact> resources = compilerContext.getLibraryDependencies();

        String sourceDirectory = compilerContext.getSourceDirectoryName();
        String artifactFilePath = compilerContext.getArtifact().getAbsolutePath();
        String targetArtifactType = config.getArtifactType().getArtifactTypeName();

        if ( !( new File( sourceDirectory ).exists() ) )
        {
            logger.info( "NMAVEN-080-000: No source files to compile." );
            return;
        }

        List<String> commands = new ArrayList<String>();
        commands.add( "/out:" + artifactFilePath );
        commands.add( "/target:" + targetArtifactType );

        if ( !resources.isEmpty() )
        {
            for ( Artifact artifact : resources )
            {
                String path = artifact.getFile().getAbsolutePath();
                commands.add( "/reference:" + path );
            }
        }
        String[] files = FileUtils.getFilesFromExtension( sourceDirectory, new String[]{"php"} );
        for ( String file : files )
        {
            commands.add( file );
        }

        logger.info( commands.toString() );
        CommandExecutor commandExecutor = CommandExecutor.Factory.createDefaultCommmandExecutor();
        commandExecutor.setLogger( logger );
        commandExecutor.executeCommand( compilerContext.getCompilerCapability().getExecutable(), commands );
    }
}
