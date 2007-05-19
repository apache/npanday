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
package org.apache.maven.dotnet.embedder.logger;

import org.codehaus.plexus.logging.BaseLoggerManager;
import org.codehaus.plexus.logging.Logger;

/**
 * Provides services for retrieving a singleton socket logger.
 *
 * @author Shane Isbell
 */
public class SocketLoggerManager
    extends BaseLoggerManager
{
    /**
     * The singleton socket logger.
     */
    private static SocketLogger logger;

    /**
     * Constructor. This method is intended to by invoked by the plexus-container, not by the application developer.
     */
    public SocketLoggerManager()
    {
        if ( logger != null )
        {
            return;
        }

        logger = new SocketLogger( getThreshold() );
    }

    /**
     * Returns the singleton socket logger
     *
     * @param key this value is ignored
     * @return the singleton socket logger
     */
    public Logger createLogger( String key )
    {
        return logger;
    }

    /**
     * Returns the singleton socket logger
     *
     * @param role this value is ignored.
     * @param roleHint this value is ignored
     * @return the singleton socket logger
     */
    public Logger getLoggerForComponent( String role, String roleHint )
    {
        return logger;
    }
}
