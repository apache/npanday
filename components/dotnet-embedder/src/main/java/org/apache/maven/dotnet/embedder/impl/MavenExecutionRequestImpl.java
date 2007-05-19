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
package org.apache.maven.dotnet.embedder.impl;

import org.apache.maven.dotnet.embedder.MavenExecutionRequest;

/**
 * Provides an implementation of the MavenExecutionRequest.
 *
 * @author Shane Isbell
 */
public class MavenExecutionRequestImpl
    implements MavenExecutionRequest

{
    private String pomFile;

    private String goal;

    private int loggerPort;

    /**
     * Constructor. This method is intended to by invoked by xfire, not by the application developer.
     */
    public MavenExecutionRequestImpl()
    {
    }

    public String getPomFile()
    {
        return pomFile;
    }

    public void setPomFile( String pomFile )
    {
        this.pomFile = pomFile;
    }

    public String getGoal()
    {
        return goal;
    }

    public void setGoal( String goal )
    {
        this.goal = goal;
    }

    public int getLoggerPort()
    {
        return loggerPort;
    }

    public void setLoggerPort( int port )
    {
        this.loggerPort = port;
    }


}
