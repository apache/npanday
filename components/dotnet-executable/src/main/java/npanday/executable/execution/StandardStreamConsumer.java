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
import org.codehaus.plexus.util.cli.DefaultConsumer;
import org.codehaus.plexus.util.cli.StreamConsumer;

/**
 * StreamConsumer instance that buffers the entire output
 *
 * @author Shane Isbell
 */
class StandardStreamConsumer
    implements StreamConsumer
{

    private DefaultConsumer consumer;

    private StringBuffer sb = new StringBuffer();

    private Logger logger;


    public StandardStreamConsumer( Logger logger )
    {
        Preconditions.checkArgument( logger != null, "logger must not be null" );

        this.logger = logger;
        consumer = new DefaultConsumer();
    }

    public void consumeLine( String line )
    {
        sb.append( line );
        if ( logger != null )
        {
            consumer.consumeLine( line );
        }
    }

    /**
     * Returns the stream
     *
     * @return the stream
     */
    public String toString()
    {
        return sb.toString();
    }
}
