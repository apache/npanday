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

import org.apache.maven.artifact.Artifact;
import org.codehaus.plexus.logging.Logger;

import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.io.File;

import org.apache.maven.dotnet.executable.CommandFilter;
import org.apache.maven.dotnet.executable.CommandExecutor;
import org.apache.maven.dotnet.executable.ExecutionException;
import org.apache.maven.dotnet.NMavenContext;
import org.apache.maven.dotnet.vendor.Vendor;
import org.apache.maven.dotnet.executable.compiler.CompilerConfig;
import org.apache.maven.dotnet.executable.compiler.CompilerContext;
import org.apache.maven.dotnet.executable.compiler.CompilerExecutable;
import org.apache.maven.dotnet.executable.compiler.InvalidArtifactException;

/**
 * A default compiler that can be used in most cases.
 *
 * @author Shane Isbell
 */
public final class DefaultCompiler
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
                "NMAVEN-068-004: Artifact does not exist: Artifact = " + file.getAbsolutePath() );
        }
        return file;
    }

    public File getExecutionPath()
    {
        return ( compilerContext.getNetCompilerConfig().getExecutionPath() != null ) ? new File(
            compilerContext.getNetCompilerConfig().getExecutionPath() ) : null;
    }

    public List<String> getCommands()
        throws ExecutionException
    {
        if ( compilerContext == null )
        {
            throw new ExecutionException( "NMAVEN-068-000: Compiler has not been initialized with a context" );
        }
        CompilerConfig config = compilerContext.getNetCompilerConfig();
        List<Artifact> references = compilerContext.getLibraryDependencies();
        List<Artifact> modules = compilerContext.getDirectModuleDependencies();

        String sourceDirectory = compilerContext.getSourceDirectoryName();
        String artifactFilePath = compilerContext.getArtifact().getAbsolutePath();
        String targetArtifactType = config.getArtifactType().getArtifactTypeName();

        compilerContext.getCompilerRequirement().getFrameworkVersion();

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
            commands.add( "/addmodule:" + sb.toString() );
        }
        if ( !references.isEmpty() )
        {
            for ( Artifact artifact : references )
            {
                String path = artifact.getFile().getAbsolutePath();
                commands.add( "/reference:" + path );
            }
        }

        for ( File file : compilerContext.getEmbeddedResources() )
        {
            commands.add( "/resource:" + file.getAbsolutePath() );
        }
        for ( File file : compilerContext.getLinkedResources() )
        {
            commands.add( "/linkresource:" + file.getAbsolutePath() );
        }
        for ( File file : compilerContext.getWin32Resources() )
        {
            commands.add( "/win32res:" + file.getAbsolutePath() );
        }
        if ( compilerContext.getWin32Icon() != null )
        {
            commands.add( "/win32icon:" + compilerContext.getWin32Icon().getAbsolutePath() );
        }

        if ( compilerContext.getCompilerRequirement().getVendor().equals( Vendor.MICROSOFT ) )
        {
            commands.add( "/nologo" );
        }

        if ( config.getCommands() != null )
        {
            commands.addAll( config.getCommands() );
        }
        commands.add( "/warnaserror-" );
        if ( compilerContext.getCompilerRequirement().getVendor().equals( Vendor.MONO ) )
        {
            commands.add( "/reference:System.Drawing" );
            commands.add( "/reference:System.Windows.Forms" );
        }
        CommandFilter filter = compilerContext.getCommandFilter();
        return filter.filter( commands );
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
        //MONO writes warnings to standard error: this turns off failing builds on warnings for MONO
        boolean failOnErrorOutput = !compilerContext.getCompilerRequirement().getVendor().equals( Vendor.MONO );
        commandExecutor.executeCommand( getExecutable(), getCommands(), getExecutionPath(), failOnErrorOutput );
    }
}
