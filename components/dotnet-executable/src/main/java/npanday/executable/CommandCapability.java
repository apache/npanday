package npanday.executable;

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

import java.util.List;
import java.util.ArrayList;

/**
 * Provides information about what commands the compiler can and cannot accept.
 *
 * @author Shane Isbell
 */
public interface CommandCapability
{
    /**
     * Returns the command options that a compiler supports. This will be used by a <code>CompilerExecutable</code>
     * implementation to determine whether user-defined parameters are supported by the compiler.
     *
     * @return the command options that a compiler supports
     */
    List<String> getIncludes();

    /**
     * Returns the command options that that a compiler does not support. This will be used by a <code>CompilerExecutable</code>
     * implementation to determine whether user-defined parameters are supported by the compiler.
     *
     * @return the command options that that a compiler does not support
     */
    List<String> getExcludes();

    /**
     * Provides factory services for creating a default instance of the command capability.
     */
    public static class Factory
    {
        /**
         * Constructor
         */
        private Factory()
        {
        }

        /**
         * Creates a command capability.
         *
         * @param includes the command options that the compiler supports
         * @param excludes command options that the compiler does not support
         * @return the command capability for the specified includes and excludes parameters
         */
        public static CommandCapability createDefaultCommandCapability( final List<String> includes,
                                                                        final List<String> excludes )
        {
            return new CommandCapability()
            {
                public List<String> getIncludes()
                {
                    return ( includes != null ) ? includes : new ArrayList<String>();
                }

                public List<String> getExcludes()
                {
                    return ( excludes != null ) ? excludes : new ArrayList<String>();
                }
            };
        }
    }
}
