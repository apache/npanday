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
package org.apache.maven.dotnet.plugin.settings;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;

import org.apache.maven.dotnet.PlatformUnsupportedException;
import org.apache.maven.dotnet.executable.ExecutionException;
import org.apache.maven.dotnet.vendor.VendorFactory;
import org.apache.maven.dotnet.vendor.VendorInfo;

/**
 * @author Shane Isbell
 * @goal generate-settings
 * @phase validate
 */
public class SettingsGeneratorMojo
    extends AbstractMojo
{

    /**
     * @parameter expression="${settings.localRepository}"
     * @required
     */
    private String localRepository;

    /**
     * The maven project.
     *
     * @parameter expression="${project}"
     * @required
     */
    private MavenProject project;

    /**
     * The Vendor for the executable. Supports MONO and MICROSOFT: the default value is <code>MICROSOFT</code>. Not
     * case or white-space sensitive.
     *
     * @parameter expression="${vendor}"
     */
    private String vendor;

    /**
     * @parameter expression = "${vendorVersion}"
     */
    private String vendorVersion;

    /**
     * @parameter expression = "${frameworkVersion}"
     */
    private String frameworkVersion;

    /**
     * @component
     */
    private org.apache.maven.dotnet.executable.NetExecutableFactory netExecutableFactory;

    /**
     * @component
     */
    private org.apache.maven.dotnet.NMavenRepositoryRegistry nmavenRegistry;

    public void execute()
        throws MojoExecutionException
    {

        if ( !System.getProperty( "os.name" ).contains( "Windows" ) )
        {
            return;
        }

        if ( System.getProperty( "bootstrap" ) != null )
        {
            return;
        }

        String nmavenSettings =
            System.getProperty( "user.home" ) + File.separator + ".m2" + File.separator + "nmaven-settings.xml";

        if ( new File( nmavenSettings ).exists() )
        {
            return;
        }

        try
        {
            nmavenRegistry.createRepositoryRegistry();
        }
        catch ( IOException e )
        {
            throw new MojoExecutionException(
                "NMAVEN-aaa-003: Failed to create the repository registry for this plugin", e );
        }

        List<String> commands = new ArrayList<String>();
        commands.add( new File( nmavenSettings ).getAbsolutePath() );
        try
        {
            VendorInfo vendorInfo = VendorInfo.Factory.createDefaultVendorInfo();
            if ( vendor != null )
            {
                vendorInfo.setVendor( VendorFactory.createVendorFromName( vendor ) );
            }
            vendorInfo.setFrameworkVersion( frameworkVersion );
            vendorInfo.setVendorVersion( vendorVersion );
            netExecutableFactory.getNetExecutableFromRepository( "NMaven.Plugin", "NMaven.Plugin.Settings", vendorInfo,
                                                                 project, localRepository, commands ).execute();
        }
        catch ( PlatformUnsupportedException e )
        {
            throw new MojoExecutionException( "", e );
        }
        catch ( ExecutionException e )
        {
            throw new MojoExecutionException( "", e );
        }
    }
}
