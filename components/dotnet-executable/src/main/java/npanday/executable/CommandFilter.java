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

import npanday.PlatformUnsupportedException;
import org.codehaus.plexus.logging.Logger;

import java.util.Collection;
import java.util.List;
import java.util.ArrayList;
import java.io.File;

/**
 * Provides services for filtering command options. A <code>NetExecutable</code> or
 * <code>CompilerExecutable</code> implementation can use the services of this interface for filtering out commands that
 * are not supported by the executable. This allows the <code>AbstractMojo</code> implementation to pass all user-defined
 * values to the framework with assurances that the framework will only use commands appropriate for the given platform
 * and executable/compiler.
 *
 * @author Shane Isbell
 */
public interface CommandFilter
{
    /**
     * Filters the commands.
     *
     *
     * @param commands the commands to filter. This parameter should not be null and will throw a
     * <code>NullPointerException</code> if it is.
     * @return a list of filtered commands.
     */
    List<String> filter( Collection<String> commands ) throws PlatformUnsupportedException;

    /**
     * Provides factory methods for creating command filters.
     */
    public static class Factory
    {
        /**
         * Default constructor
         */
        private Factory()
        {
        }

        /**
         * Creates a default implementation of the command filter. If the capability is null, then the <code>filter</code>
         * method will return an empty list. In this implementation, the returned list from the
         * <code>CommandFilter.filter</code>is the intersection of <code>CommandCapability.getIncludes()<code>
         * and the specified <code>commands<code> parameter list.
         *
         * @param capability the executable/compiler options that are supported. This parameter may be null.
         * @param logger the plexus logger. This parameter may be null.
         * @return a default implemenation of the command filter
         */
        public static CommandFilter createDefaultCommandFilter( final CommandCapability capability,
                                                                final Logger logger )
        {
            return new CommandFilter()
            {
                public List<String> filter( Collection<String> commands )
                    throws PlatformUnsupportedException
                {

                    List<String> includes = ( capability != null && capability.getIncludes() != null )
                        ? capability.getIncludes() : new ArrayList<String>();
                    List<String> excludes = ( capability != null && capability.getExcludes() != null )
                        ? capability.getExcludes() : new ArrayList<String>();

                    List<String> newCommands = new ArrayList<String>();

                    // Excluded commands should fail
                    if ( !excludes.isEmpty() )
                    {
                        for( String command : commands){
                            if ( doesInclude( excludes, command )){
                                // TODO: is the exception type right here? maybe it should be a specific exception
                                throw new PlatformUnsupportedException(
                                    "NPANDAY-060-001: The command '" + command + "' is unsupported for the targeted "
                                        + "platform or executable."
                                );
                            }
                        }
                    }

                    // Included commands are weak exclusions of all others
                    if ( !includes.isEmpty() )
                    {
                        for ( String command : commands )
                        {
                            if ( doesInclude( includes, command ) )
                            {
                                newCommands.add( command );
                            }
                            else if ( logger != null )
                            {
                                logger.info( "NPANDAY-060-000: Ignoring unknown command: Command = " + command );
                            }
                        }
                    }
                    else
                    {
                        List<String> list = new ArrayList<String>();
                        list.addAll( commands );
                        return list;
                    }

                    return newCommands;
                }

                private boolean doesInclude( List<String> includes, String command )
                {
                    for ( String include : includes )
                    {
                        if ( command.startsWith( include, 1 ) ||
                            !( command.startsWith( "/" ) || command.startsWith( "-" ) ) )
                        {
                            return true;
                        }
                    }
                    //See if it is a file on *nix.
                    if ( command.startsWith( "/" ) )
                    {
                        File file = new File( command );
                        return file.exists();
                    }
                    return false;
                }
            };
        }
    }
}
