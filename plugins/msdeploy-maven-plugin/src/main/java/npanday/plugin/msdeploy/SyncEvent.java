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

package npanday.plugin.msdeploy;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;

import java.util.List;

/**
 * Configuration class for preSync and postSync commands.
 *
 * @author <a href="mailto:me@lcorneliussen.de>Lars Corneliussen, Faktum Software</a>
 */
public class SyncEvent
{
    String runCommand;
    int waitInterval;
    int waitAttempts;
    boolean dontUseCommandExe;

    public String getRunCommand()
    {
        return runCommand;
    }

    public void setRunCommand( String runCommand )
    {
        this.runCommand = runCommand;
    }

    public int getWaitInterval()
    {
        return waitInterval;
    }

    public void setWaitInterval( int waitInterval )
    {
        this.waitInterval = waitInterval;
    }

    static Joiner JOIN_ON_COMMA = Joiner.on( "," ).skipNulls();

    public String getArgumentPart(){
        List<String> parts = Lists.newArrayList();

        parts.add( "runCommand=\"" + runCommand + "\"");

        if ( waitInterval > 0 )
        {
            parts.add( "waitInterval=" + waitInterval );
        }

        if ( waitAttempts > 0 )
        {
            parts.add( "waitAttempts=" + waitAttempts );
        }

        if ( dontUseCommandExe )
        {
            parts.add( "dontUseCommandExe=true" );
        }


        return JOIN_ON_COMMA.join( parts );
    }
}
