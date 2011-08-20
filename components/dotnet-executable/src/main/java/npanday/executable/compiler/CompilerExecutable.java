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
package npanday.executable.compiler;

import npanday.executable.NetExecutable;

import java.io.File;

/**
 * Provides a service for obtaining the compiled artifact. This interface is needed because the plugins need to set the
 * compiled artifact file on the maven project: <code>MavenProject.getArtifact().setFile</code>
 *
 * @author Shane Isbell
 */
public interface CompilerExecutable
    extends NetExecutable
{

    /**
     * Returns the assembly path for this executable.
     *
     * @return the assembly path for this executable
     */
    String getAssemblyPath();

    /**
     * Returns the target framework for this compiler executable.
     * 
     * @return  the target framework for this compiler executable
     */
     String getTargetFramework();

    /**
     * Returns a file pointing to the compiled artifact for this executable.
     *
     * @return a file pointing to the compiled artifact for this executable
     * @throws InvalidArtifactException if the artifact is invalid
     */
    File getCompiledArtifact()
        throws InvalidArtifactException;

    /**
     * Returns true to fail the build if the compiler writes anything to the error stream, otherwise return false. 
     *
     * @return true to fail the build if the compiler writes anything to the error stream, otherwise return false
     */
    boolean failOnErrorOutput();

}
