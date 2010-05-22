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

package npanday.plugin.compile;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.FileUtils;
import npanday.PlatformUnsupportedException;
import npanday.ArtifactType;
import npanday.executable.ExecutionException;
import npanday.vendor.VendorFactory;
import npanday.executable.compiler.*;

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
    extends AbstractCompilerMojo
{

    /**
     * Compiles the class files.
     *
     * @throws MojoExecutionException thrown if MOJO is unable to compile the class files or if the environment is not
     *                                properly set.
     */
    public void execute()
        throws MojoExecutionException
    {


        String skipTests = System.getProperty( "maven.test.skip" );
        if ( ( skipTests != null && skipTests.equalsIgnoreCase( "true" ) ) || skipTestCompile )
        {
            getLog().warn( "NPANDAY-903-004: Disabled unit tests: -Dmaven.test.skip=true" );
            return;
        }

        // execute as a test
        super.execute(true);

    }




    protected void initializeDefaults()
    {
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

        profileAssemblyPath = null;

    }




    protected CompilerRequirement getCompilerRequirement() throws MojoExecutionException
    {
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
            throw new MojoExecutionException( "NPANDAY-900-000: Unknown Vendor: Vendor = " + vendor, e );
        }


        return compilerRequirement;

    }

    protected CompilerConfig getCompilerConfig()  throws MojoExecutionException
    {

        //Config
        CompilerConfig compilerConfig = (CompilerConfig) CompilerConfig.Factory.createDefaultExecutableConfig();

        compilerConfig.setCommands( getParameters() );

        compilerConfig.setArtifactType( ArtifactType.DOTNET_LIBRARY );
        compilerConfig.setTestCompile( true );
        compilerConfig.setLocalRepository( localRepository );





        if ( testKeyfile != null )
        {
            KeyInfo keyInfo = KeyInfo.Factory.createDefaultKeyInfo();
            keyInfo.setKeyFileUri( testKeyfile.getAbsolutePath() );
            compilerConfig.setKeyInfo( keyInfo );
        }



        if ( testIncludeSources != null && testIncludeSources.length != 0 )
        {
            ArrayList<String> srcs = new ArrayList<String>();
            for(File includeSource : testIncludeSources)
            {
                if(includeSource.exists())
                {
                    srcs.add(includeSource.getAbsolutePath());
                }
            }

          	compilerConfig.setIncludeSources(srcs);
        }




        return compilerConfig;

    }



    protected ArrayList<String> getParameters()
    {
        ArrayList<String> params = new ArrayList<String>();


        if (testParameters != null && testParameters.size() > 0)
        {
            params.addAll(testParameters);
        }


        if (isDebug)
        {
            params.add("/debug+");
        }

        if (testRootNamespace != null)
        {
            params.add("/rootnamespace:" + testRootNamespace);
        }

        if (testDelaysign)
        {
            params.add("/delaysign+");
        }

        if (testAddModules != null && testAddModules.length != 0)
        {
            params.add("/addmodule:" + listToCommaDelimitedString(testAddModules));
        }

        if (testWin32Res != null)
        {
            params.add("/win32res:" + testWin32Res);
        }

        if (testRemoveintchecks)
        {
            params.add("/removeintchecks+");
        }

        if (testWin32Icon != null)
        {
            params.add("/win32icon:" + testWin32Icon);
        }

        if (testImports != null && testImports.length != 0)
        {
            params.add("/imports:" + listToCommaDelimitedString(testImports));
        }

        if (testResource != null)
        {
            params.add("/resource:" + testResource);
        }

        if (testLinkResource != null)
        {
            params.add("/linkresource:" + testLinkResource);
        }

        if (testOptionexplicit)
        {
            params.add("/optionexplicit+");
        }

        if (testOptionStrict != null)
        {
            if (testOptionStrict.trim().equals("+") || testOptionStrict.trim().equals("-"))
            {
                params.add("/optionstrict" + testOptionStrict.trim());
            }
            else
            {
                params.add("/optionstrict:" + testOptionStrict.trim());
            }

        }

        if (testOptimize)
        {
            params.add("/optimize+");
        }

        if (testOptionCompare != null)
        {
            params.add("/optioncompare:" + testOptionCompare);
        }

        if (testChecked)
        {
            params.add("/checked+");
        }

        if (testUnsafe)
        {
            params.add("/unsafe+");
        }

        if (testNoconfig)
        {
            params.add("/noconfig");
        }

        if (testBaseAddress != null)
        {
            params.add("/baseaddress:" + testBaseAddress);
        }

        if (testBugReport != null)
        {
            params.add("/bugreport:" + testBugReport);
        }

        if (testCodePage != null)
        {
            params.add("/codepage:" + testCodePage);
        }

        if (testUtf8output)
        {
            params.add("/utf8output");
        }

        if (testPdb != null)
        {
            params.add("/pdb:" + testPdb);
        }

        if (testErrorReport != null)
        {
            params.add("/errorreport:" + testErrorReport);
        }

        if (testModuleAssemblyName != null)
        {
            params.add("/moduleassemblyname:" + testModuleAssemblyName);
        }

        if (testLibs != null && testLibs.length != 0)
        {
            params.add("/lib:" + listToCommaDelimitedString(testLibs));
        }

        if (testMain != null)
        {
            params.add("/main:" + testMain);
        }

        if (testDefine != null)
        {
            params.add("/define:" + testDefine);
        }


        return params;
    }


}

