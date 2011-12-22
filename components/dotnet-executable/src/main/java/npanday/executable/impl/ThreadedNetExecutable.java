package npanday.executable.impl;

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

import npanday.PlatformUnsupportedException;
import npanday.executable.ExecutionException;

/**
 * Provides a service for executing a command within a separate thread. This will be used for executing long-running
 * processes.
 *
 * @author Shane Isbell
 */
public class ThreadedNetExecutable
    extends DefaultNetExecutable
    implements Runnable
{
    public void run()
    {
        try
        {
            innerExecute();
        }
        catch ( ExecutionException e )
        {
            logger.error( "NPANDAY-116-000: Error occurred in long running executable", e );
        }
        catch ( PlatformUnsupportedException e )
        {
            logger.error( "NPANDAY-116-002: Error occurred in long running executable", e );
        }
    }

    public void execute()
        throws ExecutionException
    {
        throw new ExecutionException( "NPANDAY-116-001: Create a thread, then use RUN - or refactor ThreadedNetExecutable");
        // new Thread(this).start();
    }
}
