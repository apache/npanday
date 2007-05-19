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

import java.util.logging.Formatter;
import java.util.logging.LogRecord;

/**
 * Provides services for formatting log records in the standard maven format: [LOGLEVEL] message.
 *
 * @author Shane Isbell
 */
public class MavenFormatter
    extends Formatter
{
    /**
     * The line separator appropriate for the target platform.
     */
    private static String lineSeparator = System.getProperty( "line.separator" );

    /**
     * Formats the specified log record as the standard maven format: [LOGLEVEL] message.
     *
     * @param logRecord the log record to format
     * @return the specified log record as the standard maven format: [LOGLEVEL] message
     */
    public synchronized String format( LogRecord logRecord )
    {
        StringBuffer sb = new StringBuffer();
        sb.append( "[" ).append( logRecord.getLevel().getLocalizedName() ).append( "] " );
        sb.append( formatMessage( logRecord ) );

        if ( logRecord.getThrown() != null )
        {
            sb.append( lineSeparator );
            sb.append( logRecord.getThrown().toString() );
        }
        return sb.append( lineSeparator ).toString();
    }
}
