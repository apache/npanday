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

import org.apache.maven.dotnet.NMavenContext;
import org.apache.maven.dotnet.vendor.Vendor;

import java.util.List;
import java.io.File;

/**
 * Provides services for executing programs.
 *
 * @author Shane Isbell
 * @see org.apache.maven.dotnet.executable.compiler.CompilerExecutable
 */
public interface NetExecutable
{

    /**
     * Returns the commands that this compiler will use to compile the application. This list is unmodifiable.
     *
     * @return the commands that this compiler will use to compile the application
     * @throws ExecutionException
     */
    List<String> getCommands()
        throws ExecutionException;

    /**
     * Resets the commands to be used by the executable. This should only be used if the executable is being reused with
     * different commands from the one that it was initialized with.
     *
     * @param commands
     */
    void resetCommands(List<String> commands);

    /**
     * Compiles class files.
     *
     * @throws org.apache.maven.dotnet.executable.ExecutionException
     *          if the compiler writes to the standard error stream.
     *          artifact (module, library, exe, winexe) or the target artifact is not valid for the compiler
     */
    void execute()
        throws ExecutionException;

    /**
     * Returns the executable file name that this compiler will use to compile the application.
     *
     * @return the executable file name that this compiler will use to compile the application
     * @throws org.apache.maven.dotnet.executable.ExecutionException
     *
     */
    String getExecutable()
        throws ExecutionException;

    /**
     * Returns the parent directory of the executable.
     *
     * @return the parent directory of the executable
     */
    File getExecutionPath();

    /**
     * Initialize this compiler.
     *
     * @param nmavenContext
     */
    void init( NMavenContext nmavenContext );

    /**
     * Returns vendor framework used to run executable.
     *
     * @return vendor vendor framework used to run executable
     */
    Vendor getVendor();

}
