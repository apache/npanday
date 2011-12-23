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

import npanday.executable.ExecutionException;
import npanday.executable.compiler.CompilerConfig;
import npanday.vendor.Vendor;

import java.util.List;
import java.util.ArrayList;
import java.io.File;

/**
 * Compiles ruby classes.
 *
 * @author Shane Isbell
 */
//TODO: Partial implementation
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
            throw new ExecutionException( "NPANDAY-068-000: Compiler has not been initialized with a context" );
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
        return commands;
    }

    public void resetCommands( List<String> commands )
    {
        
    }
}
