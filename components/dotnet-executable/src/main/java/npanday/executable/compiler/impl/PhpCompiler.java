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
import org.apache.maven.artifact.Artifact;
import org.codehaus.plexus.util.FileUtils;

import java.util.List;
import java.util.ArrayList;

/**
 * Compiler for PHP (http://php4mono.sourceforge.net/)
 *
 * @author Shane Isbell
 */
public final class PhpCompiler
    extends BaseCompiler
{

    public boolean failOnErrorOutput()
    {
        return true;
    }

    public List<String> getCommands()
        throws ExecutionException
    {
        CompilerConfig config = compilerContext.getNetCompilerConfig();
        List<Artifact> resources = compilerContext.getLibraryDependencies();

        String sourceDirectory = compilerContext.getSourceDirectoryName();
        String artifactFilePath = compilerContext.getArtifact().getAbsolutePath();
        String targetArtifactType = config.getArtifactType().getTargetCompileType();

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
        return commands;
    }

    public void resetCommands( List<String> commands )
    {
        
    }
}
