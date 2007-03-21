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
package org.apache.maven.dotnet.executable;

import org.apache.maven.dotnet.executable.compiler.CompilerConfig;
import org.apache.maven.dotnet.artifact.ArtifactType;
import org.apache.maven.dotnet.executable.compiler.KeyInfo;

import java.util.List;
import java.io.File;

/**
 * User-defined configuration information for an executable or compiler.
 *
 * @author Shane Isbell
 * @see RepositoryExecutableContext
 */
public interface ExecutableConfig
{

    /**
     * The commands to pass to the executable plugin.
     *
     * @return the commands to pass to the executable plugin
     */
    List<String> getCommands();

    /**
     * Sets commands to pass to the executable plugin.
     *
     * @param commands the user-defined commands to pass to the executable plugin
     */
    void setCommands( List<String> commands );

    /**
     * The execution path of the executable. This can be an absolute path to the executable or just the name of the
     * executable as invoked from the command line.
     *
     * @return the execution path of the executable
     */
    List<String> getExecutionPaths();

    /**
     * Sets the executation path of the executable.
     *
     * @param executionPaths the execution paths
     */
    void setExecutionPaths( List<String> executionPaths );

    public static class Factory
    {
        /**
         * Constructor
         */
        private Factory()
        {
        }

        /**
         * Returns a default instance of the executable config.
         *
         * @return a default instance of the executable config
         */
        public static ExecutableConfig createDefaultExecutableConfig()
        {
            return new CompilerConfig()
            {
                private KeyInfo keyInfo;

                private List<String> commands;

                private List<String> executionPaths;

                private ArtifactType artifactType;

                private boolean isTestCompile = false;

                private File localRepository;

                public List<String> getCommands()
                {
                    return commands;
                }

                public void setCommands( List<String> commands )
                {
                    this.commands = commands;
                }

                public List<String> getExecutionPaths()
                {
                    return executionPaths;
                }

                public void setExecutionPaths( List<String> executionPaths )
                {
                    this.executionPaths = executionPaths;
                }

                public ArtifactType getArtifactType()
                {
                    return artifactType;
                }

                public void setArtifactType( ArtifactType artifactType )
                {
                    this.artifactType = artifactType;
                }

                public boolean isTestCompile()
                {
                    return isTestCompile;
                }

                public void setTestCompile( boolean testCompile )
                {
                    isTestCompile = testCompile;
                }

                public File getLocalRepository()
                {
                    return localRepository;
                }

                public void setLocalRepository( File localRepository )
                {
                    this.localRepository = localRepository;
                }

                public KeyInfo getKeyInfo()
                {
                    return keyInfo;
                }

                public void setKeyInfo(KeyInfo keyInfo) {
                    this.keyInfo = keyInfo;
                }
            };
        }
    }

}
