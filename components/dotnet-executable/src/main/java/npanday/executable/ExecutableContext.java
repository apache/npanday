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
package npanday.executable;

import npanday.NPandayContext;
import npanday.PlatformUnsupportedException;
import npanday.vendor.Vendor;

import java.util.Collection;

/**
 * Provides services for executables.
 *
 * @author Shane Isbell
 */
public interface ExecutableContext
    extends NPandayContext
{
    /**
     * Returns an instance of the NetExecutable appropriate for given vendor/profile/frameworkVersion/OS
     *
     * @return an instance of the NetExecutable appropriate for given vendor/profile/frameworkVersion/OS
     * @throws npanday.executable.ExecutionException
     *          if there is a problem finding an appropriate executable.
     */
    NetExecutable getNetExecutable()
        throws ExecutionException;

    /**
     * The vendor the executable runs for.
     */
    Vendor getVendor();

    /**
     * Returns the exectuable name to be run.
     *
     * @return
     */
    String getExecutableName();

    /**
     * The commands that instruct the executable to be run.
     * @return
     */
    Collection<String> getCommands();

    /**
     * The paths the executable is most likely to be found on. First matching path wins.
     * @return
     */
    Collection<String> getProbingPaths();

    /**
     * Creates a command filter. If the includes parameter is null, then the filter will return all commands that are
     * not in the exlude filters. If the excludes parameter is null, then the filter will only return what is in the
     * includes list. If both parameters are null...
     *
     * @return the command filter associated with this context
     */
    CommandFilter getCommandFilter();


    /**
     * Initializes the context
     *
     * @param capability
     * @param config
     * @throws npanday.PlatformUnsupportedException
     *
     */
    void init( ExecutableCapability capability, ExecutableConfig config )
        throws PlatformUnsupportedException;
}
