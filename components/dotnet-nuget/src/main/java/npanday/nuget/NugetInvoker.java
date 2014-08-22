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

package npanday.nuget;

import com.google.common.collect.Lists;
import npanday.PlatformUnsupportedException;
import npanday.executable.ExecutableRequirement;
import npanday.executable.ExecutionException;
import npanday.executable.NetExecutable;
import npanday.executable.NetExecutableFactory;
import org.codehaus.plexus.logging.AbstractLogEnabled;

import java.util.List;

/**
 * @author <a href="mailto:lcorneliussen@apache.org">Lars Corneliussen</a>
 * @plexus.component role="npanday.nuget.NugetInvoker"
 */
public class NugetInvoker
    extends AbstractLogEnabled
{
    /**
     * @plexus.requirement
     */
    protected NetExecutableFactory netExecutableFactory;

    public void install(
        ExecutableRequirement executableRequirement, NugetInstallParameters parameters ) throws
        PlatformUnsupportedException,
        NugetException
    {
        final NetExecutable executable = netExecutableFactory.getExecutable(
            executableRequirement, buildCommands( parameters ), null
        );

        try
        {
            executable.execute();
        }
        catch ( ExecutionException e )
        {
            throw new NugetException( "NPANDAY-143-000: Exception on executing Nuget with " + parameters, e );
        }
    }

    private List<String> buildCommands( NugetInstallParameters parameters )
    {
        List<String> commands = Lists.newArrayList();

        commands.addAll( parameters.buildCommands() );

        return commands;
    }
}
