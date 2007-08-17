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
package org.apache.maven.dotnet.plugin.xsp;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.dotnet.PlatformUnsupportedException;
import org.apache.maven.dotnet.vendor.Vendor;

import java.io.File;
import java.util.ArrayList;

/**
 * Starts the XSP server.
 *
 * @author Shane Isbell
 * @goal start-xsp
 * @phase pre-integration-test
 * @description Starts the XSP server.
 */
public class XspStarterMojo
    extends AbstractMojo
{
    /**
     * The home directory of your .NET SDK.
     *
     * @parameter expression="${netHome}"
     */
    private File netHome;

    /**
     * @parameter expression = "${frameworkVersion}"
     */
    private String frameworkVersion;

    /**
     * @component
     */
    private org.apache.maven.dotnet.executable.NetExecutableFactory netExecutableFactory;

    public void execute()
        throws MojoExecutionException
    {
        try
        {
            Runnable executable = (Runnable) netExecutableFactory.getNetExecutableFor( Vendor.MONO.getVendorName(),
                                                                                       frameworkVersion, "XSP:START",
                                                                                       new ArrayList<String>(),
                                                                                       netHome );
            Thread thread = new Thread( executable );
            getPluginContext().put( "xspThread", thread );
            thread.start();

        }
        catch ( PlatformUnsupportedException e )
        {
            throw new MojoExecutionException( "NMAVEN-1400-001: Platform Unsupported:", e );
        }
    }
}
