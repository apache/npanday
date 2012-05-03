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

package npanday.plugin.msdeploy.sync;

import org.apache.maven.plugin.MojoExecutionException;

/**
 * @author <a href="mailto:me@lcorneliussen.de>Lars Corneliussen, Faktum Software</a>
 */
public class Destination
{
    private String computerName, username, password, authType, serverId;

    private boolean local;

    public String getComputerName()
    {
        return computerName;
    }

    public void setComputerName( String computerName )
    {
        this.computerName = computerName;
    }

    public String getUsername()
    {
        return username;
    }

    public void setUsername( String username ) throws MojoExecutionException
    {
        throw new MojoExecutionException( "NPANDAY-154-000: Please use settings to store credentials by server id!" );
    }

    public String getPassword()
    {
        return password;
    }

    public void setPassword( String password ) throws MojoExecutionException
    {
        throw new MojoExecutionException( "NPANDAY-154-002: Please use settings to store credentials by server id!" );
    }

    public String getAuthType()
    {
        return authType;
    }

    public void setAuthType( String authType )
    {
        this.authType = authType;
    }

    public String getServerId()
    {
        return serverId;
    }

    public void setServerId( String serverId )
    {
        this.serverId = serverId;
    }

    protected void setSettingsUsername( String username )
    {
        this.username = username;
    }

    protected void setSettingsPassword( String password )
    {
        this.password = password;
    }

    public boolean getLocal()
    {
        return local;
    }

    public void setLocal( boolean local )
    {
        this.local = local;
    }
}
