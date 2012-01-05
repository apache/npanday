package npanday.executable.execution;

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

import com.google.common.base.Preconditions;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.util.cli.StreamConsumer;

/**
 * Provides behavior for determining whether the command utility wrote anything to the Standard Error Stream.
 * NOTE: I am using this to decide whether to fail the NPanday build. If the compiler implementation chooses
 * to write warnings to the error stream, then the build will fail on warnings!!!
 *
 * @author Shane Isbell
 */
class ErrorStreamConsumer
    implements StreamConsumer
{

    /**
     * Is true if there was anything consumed from the stream, otherwise false
     */
    private boolean error;

    /**
     * Buffer to store the stream
     */
    private StringBuffer sbe = new StringBuffer();

    private Logger logger;

    public ErrorStreamConsumer( Logger logger )
    {
        Preconditions.checkArgument( logger != null, "logger must not be null" );
        this.logger = logger;

        error = false;
    }

    public void consumeLine( String line )
    {
        sbe.append( line );
        if ( logger != null )
        {
            logger.error( " | " + line );
        }
        error = true;
    }

    /**
     * Returns false if the command utility wrote to the Standard Error Stream, otherwise returns true.
     *
     * @return false if the command utility wrote to the Standard Error Stream, otherwise returns true.
     */
    public boolean hasError()
    {
        return error;
    }

    /**
     * Returns the error stream
     *
     * @return error stream
     */
    public String toString()
    {
        return sbe.toString();
    }
}
