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

import npanday.PathUtil;
import npanday.PlatformUnsupportedException;
import npanday.executable.CommandExecutor;
import npanday.executable.CommandFilter;
import npanday.executable.ExecutionException;

import java.util.ArrayList;
import java.util.List;

public class AspxCompiler
    extends BaseCompiler
{

    public boolean failOnErrorOutput()
    {       
        return true;
    }

    public List<String> getCommands() throws ExecutionException, PlatformUnsupportedException
    {
        if ( compilerContext == null )
        {
            throw new ExecutionException( "NPANDAY-068-000: Compiler has not been initialized with a context" );
        }

        compilerContext.getFrameworkVersion();

        List<String> commands = new ArrayList<String>();

        
        if ( compilerContext.getCommands() != null )
        {
            commands.addAll( compilerContext.getCommands() );
        }
        
        CommandFilter filter = compilerContext.getCommandFilter();
        return filter.filter( commands );
    }
    
    

    @Override
    public void execute() throws ExecutionException, PlatformUnsupportedException
    {
        logger.info( "NPANDAY-068-003: Compiling Artifact: Vendor = "
            + compilerContext.getVendor() + ", Language = "
            + compilerContext.getArtifact().getAbsolutePath() );

        CommandExecutor commandExecutor = CommandExecutor.Factory.createDefaultCommmandExecutor(
            (String)configuration.get( "switchformats" )
        );
        commandExecutor.setLogger( logger );
        String executable = PathUtil.getExecutable( getExecutable(), compilerContext.getProbingPaths(), logger );
        commandExecutor.executeCommand( executable, getCommands(), null, failOnErrorOutput() );
    }

}