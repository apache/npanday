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
package npanday.executable.impl;

import npanday.PlatformUnsupportedException;
import npanday.executable.CapabilityMatcher;
import npanday.executable.CommandFilter;
import npanday.executable.ExecutableCapability;
import npanday.executable.ExecutableConfig;
import npanday.executable.ExecutableContext;
import npanday.executable.ExecutableRequirement;
import npanday.executable.ExecutionException;
import npanday.executable.NetExecutable;
import npanday.vendor.Vendor;
import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;

import java.util.Collections;
import java.util.List;

/**
 * Provides an implementation of the executable context.
 *
 * @author Shane Isbell
 * @plexus.component
 *   role="npanday.executable.ExecutableContext"
 */
public class ExecutableContextImpl
    implements ExecutableContext, LogEnabled
{
    private ExecutableCapability executableCapability;

    private ExecutableConfig executableConfig;


    private NetExecutable netExecutable;

    private CommandFilter commandFilter;

    /**
     * A logger for writing log messages
     */
    private Logger logger;

    public void enableLogging( Logger logger )
    {
        this.logger = logger;
    }

    public Logger getLogger()
    {
        return logger;
    }

    public NetExecutable getNetExecutable()
        throws ExecutionException
    {
        return netExecutable;
    }

    public Vendor getVendor()
    {
        return executableCapability.getVendorInfo().getVendor();
    }

    public CommandFilter getCommandFilter()
    {
        return commandFilter;
    }

    public void init( ExecutableRequirement executableRequirement, ExecutableConfig executableConfig,
                      CapabilityMatcher capabilityMatcher )
        throws PlatformUnsupportedException
    {
        // TODO: getting a executable capability from the requirement should rather happen outside
        ExecutableCapability capability = capabilityMatcher.matchExecutableCapabilityFor( executableRequirement );

        init( capability, executableConfig );
    }

    protected <T> T createPluginInstance()
        throws PlatformUnsupportedException
    {
        String className = executableCapability.getPluginClassName();

        try
        {
            // TODO: create Executable through plexus
            Class cc = Class.forName( className );
            return (T) cc.newInstance();
        }
        catch ( ClassNotFoundException e )
        {
            throw new PlatformUnsupportedException(
                "NPANDAY-064-001: Unable to create NetCompiler: Class Name = " + className, e );
        }
        catch ( InstantiationException e )
        {
            throw new PlatformUnsupportedException(
                "NPANDAY-064-002: Unable to create NetCompiler: Class Name = " + className, e );
        }
        catch ( IllegalAccessException e )
        {
            throw new PlatformUnsupportedException(
                "NPANDAY-064-003: Unable to create NetCompiler: Class Name = " + className, e );
        }
    }

    public String getExecutableName()
    {
        return executableCapability.getExecutableName();
    }

    public List<String> getCommands()
    {
        return executableConfig.getCommands();
    }

    public List<String> getProbingPaths()
    {
        List<String> executionPaths = executableConfig.getExecutionPaths();

        if (executionPaths == null || executionPaths.size() == 0)
        {
            // if the user-passed configuration contains probing paths, these
            // are at highest priority and OVERRIDE built-in probing paths configuration
            executionPaths = executableCapability.getProbingPaths();
        }

        return Collections.unmodifiableList( executionPaths );
    }

    public void init( ExecutableCapability capability, ExecutableConfig config )
        throws PlatformUnsupportedException
    {
        executableCapability = capability;
        executableConfig = config;

        commandFilter = CommandFilter.Factory.createDefaultCommandFilter( executableCapability.getCommandCapability(), logger );
        netExecutable = createPluginInstance();
        netExecutable.init( this );
    }
}
