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
package npanday.plugin.generator;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;

import java.io.File;
import java.util.List;
import java.util.ArrayList;

import npanday.PlatformUnsupportedException;
import npanday.executable.ExecutionException;
import npanday.vendor.VendorFactory;
import npanday.vendor.VendorInfo;

/**
 * Generates Java Bindings for .NET Mojos.
 *
 * @author Shane Isbell
 * @goal generate-bindings
 * @phase deploy
 * @description Generates Java Bindings for .NET Mojos
 */
public class MojoGeneratorMojo
    extends AbstractMojo
{
    /**
     * The base directory of the project.
     *
     * @parameter expression="${basedir}"
     */
    public String basedir;

    /**
     * The local Maven repository.
     *
     * @parameter expression="${settings.localRepository}"
     */
    private File localRepository;

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
     * The vendor version to compile under. For Microsoft, this will be the same as the framework version.
     *
     * @parameter expression = "${vendorVersion}"
     */
    private String vendorVersion;

    /**
     * The framework version to compile under: 1.1, 2.0, 3.0
     *
     * @parameter expression = "${frameworkVersion}"
     */
    private String frameworkVersion;

    /**
     * @component
     */
    private npanday.executable.NetExecutableFactory netExecutableFactory;

    /** @parameter default-value="false" */
    private boolean skip;

    public void execute()
        throws MojoExecutionException
    {
        if ( skip )
        {
            return;
        }

        try
        {    
            List<String> commands = new ArrayList<String>();
            VendorInfo vendorInfo = VendorInfo.Factory.createDefaultVendorInfo();
            if ( vendor != null )
            {
                vendorInfo.setVendor( VendorFactory.createVendorFromName( vendor ) );
            }
            vendorInfo.setFrameworkVersion( frameworkVersion );
            vendorInfo.setVendorVersion( vendorVersion );
            File targetAssemblyFile =
                new File( project.getBuild().getDirectory() + "/" + project.getArtifactId() + ".dll" );
            commands.add( "targetAssemblyFile=" + targetAssemblyFile.getAbsolutePath() );
            commands.add( "outputDirectory=" + basedir );
            commands.add( "groupId=" + project.getGroupId() );
            commands.add( "artifactId=" + project.getArtifactId() );
            commands.add( "artifactVersion=" + project.getVersion());
            netExecutableFactory.getNetExecutableFromRepository( "org.apache.npanday.plugin", "NPanday.Plugin.MojoGenerator",
                                                                 vendorInfo, localRepository, commands,
                                                                 true ).execute();
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
