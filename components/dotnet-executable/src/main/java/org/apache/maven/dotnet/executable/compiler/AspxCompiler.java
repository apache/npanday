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

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.maven.artifact.Artifact;
import npanday.executable.CommandExecutor;
import npanday.executable.CommandFilter;
import npanday.executable.ExecutionException;
import npanday.executable.compiler.CompilerConfig;
import npanday.vendor.Vendor;

public class AspxCompiler
    extends BaseCompiler
{

    public boolean failOnErrorOutput()
    {       
        return true;
    }

    public List<String> getCommands()
        throws ExecutionException
    {
        if ( compilerContext == null )
        {
            throw new ExecutionException( "NPANDAY-068-000: Compiler has not been initialized with a context" );
        }
        CompilerConfig config = compilerContext.getNetCompilerConfig();
        
        compilerContext.getCompilerRequirement().getFrameworkVersion();

        List<String> commands = new ArrayList<String>();

        
        if ( config.getCommands() != null )
        {
            commands.addAll( config.getCommands() );
        }
        
        CommandFilter filter = compilerContext.getCommandFilter();
        return filter.filter( commands );
    }
    
    

    @Override
    public void execute()
        throws ExecutionException
    {
        logger.info( "NPANDAY-068-003: Compiling Artifact: Vendor = "
            + compilerContext.getCompilerRequirement().getVendor() + ", Language = "
            + compilerContext.getCompilerRequirement().getVendor() + ", Assembly Name = "
            + compilerContext.getArtifact().getAbsolutePath() );

        CommandExecutor commandExecutor = CommandExecutor.Factory.createDefaultCommmandExecutor();
        commandExecutor.setLogger( logger );
        commandExecutor.executeCommand( getExecutable(), getCommands(), getExecutionPath(), failOnErrorOutput() );
    }

    public void resetCommands( List<String> commands )
    {
        // TODO Auto-generated method stub
    }

}