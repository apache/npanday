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

import org.apache.commons.exec.LogOutputStream;

/**
 * Stream that captures and logs lines to a plexus logger.
 * Designed for anonymous implementation.
 *
 * Access the captured output through {@link #toString}.
 *
 * @author <a href="mailto:lcorneliussen@apache.org">Lars Corneliussen</a>
 */
public abstract class CapturingLogOutputStream
    extends LogOutputStream
{
    private StringBuffer contents = new StringBuffer();

    private String NEW_LINE = System.getProperty("line.separator");

    @Override
    protected final void processLine(String line, int level) {
        processLine( line );

        contents.append( line );
        contents.append( NEW_LINE );
    }

    /**
     * Override this to log to a logger.
     *
     * @param line
     */
    protected abstract void processLine( String line );

    /**
     * Returns the captured output.
     */
    @Override
    public String toString()
    {
        return contents.toString();
    }
}
