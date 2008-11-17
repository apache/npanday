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
package org.apache.maven.dotnet.plugin.compile;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.FileUtils;
import org.apache.maven.dotnet.PlatformUnsupportedException;
import org.apache.maven.dotnet.ArtifactType;
import org.apache.maven.dotnet.executable.ExecutionException;
import org.apache.maven.dotnet.vendor.VendorFactory;
import org.apache.maven.dotnet.executable.compiler.*;

import java.util.ArrayList;
import java.io.File;

/**
 * Compiles test classes.
 *
 * @author Shane Isbell
 * @goal testCompile
 * @phase test-compile
 * @description Maven Mojo for compiling test class files
 */
public final class TestCompilerMojo
    extends AbstractMojo
{

    /**
     * The location of the local Maven repository.
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
     * Additional compiler commands for test classes
     *
     * @parameter expression = "${testParameters}"
     */
    private ArrayList<String> testParameters;

    /**
     * Skips compiling of unit tests
     *
     * @parameter expression = "${skipTestCompile}" default-value = "false"
     */
    private boolean skipTestCompile;

    /**
     * The framework version to compile the test classes: 1.1, 2.0, 3.0
     *
     * @parameter expression = "${testFrameworkVersion}"
     */
    private String testFrameworkVersion;

    /**
     * The framework version to compile under: 1.1, 2.0, 3.0
     *
     * @parameter expression = "${frameworkVersion}"
     */
    private String frameworkVersion;

    /**
     * The Vendor for the Compiler. Supports MONO and MICROSOFT: the default value is <code>MICROSOFT</code>. Not
     * case or white-space sensitive.
     *
     * @parameter expression="${testVendor}"
     */
    private String testVendor;

    /**
     * .NET Language. The default value is <code>C_SHARP</code>. Not case or white-space sensitive.
     *
     * @parameter expression="${testLanguage}"
     */
    private String testLanguage;

    /**
     * .NET Language. The default value is <code>C_SHARP</code>. Not case or white-space sensitive.
     *
     * @parameter expression="${language}" default-value = "C_SHARP"
     * @required
     */
    private String language;

    /**
     * The Vendor for the Compiler. Supports MONO and MICROSOFT: the default value is <code>MICROSOFT</code>. Not
     * case or white-space sensitive.
     *
     * @parameter expression="${vendor}"
     */
    private String vendor;

    /**
     * @parameter expression = "${testVendorVersion}"
     */
    private String testVendorVersion;

    /**
     * @component
     */
    private org.apache.maven.dotnet.executable.NetExecutableFactory netExecutableFactory;

    /**
     * Include debug output.
     *
     * @parameter expression = "${isDebug}" default-value="false"
     */
    private boolean isDebug;

    /**
     * Compiles the class files.
     *
     * @throws MojoExecutionException thrown if MOJO is unable to compile the class files or if the environment is not
     *                                properly set.
     */
    public void execute()
        throws MojoExecutionException
    {
        long startTime = System.currentTimeMillis();

        String skipTests = System.getProperty( "maven.test.skip" );
        if ( ( skipTests != null && skipTests.equalsIgnoreCase( "true" ) ) || skipTestCompile )
        {
            getLog().warn( "NMAVEN-903-004: Disabled unit tests: -Dmaven.test.skip=true" );
            return;
        }
        FileUtils.mkdir( "target" );

        if ( testLanguage == null )
        {
            testLanguage = language;
        }
        if ( testVendor == null )
        {
            testVendor = vendor;
        }
        if ( testFrameworkVersion == null )
        {
            testFrameworkVersion = frameworkVersion;
        }

        if ( localRepository == null )
        {
            localRepository = new File( System.getProperty( "user.home" ), ".m2/repository" );
        }

        //Requirement
        CompilerRequirement compilerRequirement = CompilerRequirement.Factory.createDefaultCompilerRequirement();
        compilerRequirement.setLanguage( testLanguage );
        compilerRequirement.setFrameworkVersion( testFrameworkVersion );
        compilerRequirement.setProfile( "FULL" );
        compilerRequirement.setVendorVersion( testVendorVersion );
        try
        {
            if ( vendor != null )
            {
                compilerRequirement.setVendor( VendorFactory.createVendorFromName( vendor ) );
            }
        }
        catch ( PlatformUnsupportedException e )
        {
            throw new MojoExecutionException( "NMAVEN-900-000: Unknown Vendor: Vendor = " + vendor, e );
        }

        //Config
        CompilerConfig compilerConfig = (CompilerConfig) CompilerConfig.Factory.createDefaultExecutableConfig();
        if ( testParameters == null )
        {
            testParameters = new ArrayList<String>();
        }
        if ( isDebug )
        {
            testParameters.add( "/debug+" );
        }
        compilerConfig.setCommands( testParameters );

        compilerConfig.setArtifactType( ArtifactType.LIBRARY );
        compilerConfig.setTestCompile( true );
        compilerConfig.setLocalRepository( localRepository );

        try
        {
            CompilerExecutable compilerExecutable =
                netExecutableFactory.getCompilerExecutableFor( compilerRequirement, compilerConfig, project, null );
            compilerExecutable.execute();
        }
        catch ( PlatformUnsupportedException e )
        {
            throw new MojoExecutionException(
                "NMAVEN-903-003: Unsupported Platform: Language = " + language + ", Vendor = " + vendor, e );
        }
        catch ( ExecutionException e )
        {
            throw new MojoExecutionException(
                "NMAVEN-903-002: Unable to Compile: Language = " + language + ", Vendor = " + vendor, e );
        }
        long endTime = System.currentTimeMillis();
        getLog().info( "Mojo Execution Time = " + ( endTime - startTime ) );
    }
}
