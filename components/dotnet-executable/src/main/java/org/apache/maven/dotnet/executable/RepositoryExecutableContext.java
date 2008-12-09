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

import org.apache.maven.dotnet.NPandayContext;
import org.apache.maven.dotnet.InitializationException;
import org.apache.maven.project.MavenProject;

/**
 * Provides services for obtaining the executable for a context.
 *
 * @author Shane Isbell
 */
public interface RepositoryExecutableContext
    extends NPandayContext
{
    /**
     * Returns an instance of NetExecutable for this context.
     *
     * @return an instance of the NetExecutable
     * @throws org.apache.maven.dotnet.executable.ExecutionException
     *          if there is a problem finding an appropriate executable.
     */
    NetExecutable getNetExecutable()
        throws ExecutionException;

    /**
     * Returns user-defined configuration for this context.
     *
     * @return user-defined configuration for this context
     */
    ExecutableConfig getExecutableConfig();

    /**
     * Initializes this context.
     *
     * @param executableConfig the executable config for this context
     * @throws org.apache.maven.dotnet.InitializationException if the context cannot be initialized
     */
    void init( ExecutableConfig executableConfig )
        throws InitializationException;
}
