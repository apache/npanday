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
import org.apache.maven.dotnet.executable.compiler.CompilerContext;

import java.util.List;
import java.io.File;

/**
 * A compiler to be used for compiling with .NET Profiles.
 *
 * @author Shane Isbell
 */
public final class CSharpCompilerForProfile
    extends BaseCompiler
{
    private NetExecutable netCompiler;

    private CompilerContext compilerContext;

    public boolean failOnErrorOutput()
    {
        return true;
    }

    public CSharpCompilerForProfile()
    {
        netCompiler = new DefaultCompiler();
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

    public void resetCommands( List<String> commands )
    {
        
    }

    public void init( NMavenContext nmavenContext )
    {
        super.init( nmavenContext);
        netCompiler.init( nmavenContext );
        this.compilerContext = (CompilerContext) nmavenContext;
    }
}
