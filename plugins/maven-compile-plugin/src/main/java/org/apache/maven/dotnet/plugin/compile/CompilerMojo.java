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

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.AbstractMojo;

import org.apache.maven.project.MavenProject;
import org.apache.maven.dotnet.PlatformUnsupportedException;
import org.apache.maven.dotnet.ArtifactType;
import org.apache.maven.dotnet.executable.ExecutionException;
import org.apache.maven.dotnet.vendor.VendorFactory;
import org.apache.maven.dotnet.executable.compiler.*;
import org.apache.maven.artifact.Artifact;
import org.codehaus.plexus.util.FileUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.io.File;

/**
 * Maven Mojo for compiling Class files to the .NET Intermediate Language.
 * To use a specific vendor (MICROSOFT/MONO) or language, the compiler/language must be previously installed AND
 * configured through the plugin-compiler.xml file: otherwise the Mojo either will throw a MojoExecutionException
 * telling you that the platform is not supported (occurs if entry is not in plugin-compilers.xml, regardless of
 * whether the compiler/language is installed) or will attempt to execute the compiler and fail (occurs if entry is in
 * plugin-compilers.xml and the compiler/language is not installed).
 *
 * @author Shane Isbell
 * @goal compile
 * @phase compile
 * @description Maven Mojo for compiling class files to the .NET Intermediate Language
 */
public final class CompilerMojo
    extends AbstractCompilerMojo
{



    protected void initializeDefaults()  throws MojoExecutionException
    {


        if ( profileAssemblyPath != null && !profileAssemblyPath.exists() )
        {
            throw new MojoExecutionException( "NPANDAY-900-000: Profile Assembly Path does not exist: Path = " +
                profileAssemblyPath.getAbsolutePath() );
        }

    }








    protected CompilerRequirement getCompilerRequirement() throws MojoExecutionException
    {
         //Requirement
        CompilerRequirement compilerRequirement = CompilerRequirement.Factory.createDefaultCompilerRequirement();
        compilerRequirement.setLanguage( language );
        compilerRequirement.setFrameworkVersion( frameworkVersion );
        compilerRequirement.setProfile( profile );
        compilerRequirement.setVendorVersion( vendorVersion );
        try
        {
            if ( vendor != null )
            {
                compilerRequirement.setVendor( VendorFactory.createVendorFromName( vendor ) );
            }
        }
        catch ( PlatformUnsupportedException e )
        {
            throw new MojoExecutionException( "NPANDAY-900-001: Unknown Vendor: Vendor = " + vendor, e );
        }

        return compilerRequirement;


    }

    protected CompilerConfig getCompilerConfig()  throws MojoExecutionException
    {

          //Config
        CompilerConfig compilerConfig = (CompilerConfig) CompilerConfig.Factory.createDefaultExecutableConfig();
        compilerConfig.setLocalRepository( localRepository );


        if ( keyfile != null )
        {
            KeyInfo keyInfo = KeyInfo.Factory.createDefaultKeyInfo();
            keyInfo.setKeyFileUri( keyfile.getAbsolutePath() );
            compilerConfig.setKeyInfo( keyInfo );
        }

        if(outputDirectory != null)
        {
            if(!outputDirectory.exists())
            {
                outputDirectory.mkdirs();
            }
            compilerConfig.setOutputDirectory(outputDirectory);
        }



        if ( includeSources != null && includeSources.length != 0 )
        {
            ArrayList<String> srcs = new ArrayList<String>();
            for(File includeSource : includeSources)
            {
                if(includeSource.exists())
                {
                    srcs.add(includeSource.getAbsolutePath());
                }
            }

          	compilerConfig.setIncludeSources(srcs);
        }

        compilerConfig.setCommands( getParameters() );

        String artifactTypeName = project.getArtifact().getType();
        ArtifactType artifactType = ArtifactType.getArtifactTypeForPackagingName( artifactTypeName );
        if ( artifactType.equals( ArtifactType.NULL ) )
        {
            throw new MojoExecutionException( "NPANDAY-900-002: Unrecognized artifact type: Language = " + language +
                ", Vendor = " + vendor + ", ArtifactType = " + artifactTypeName );
        }
        compilerConfig.setArtifactType( artifactType );


        return compilerConfig;

        

    }


    protected ArrayList<String> getParameters()
    {
        ArrayList<String> params = new ArrayList<String>();


        if (parameters != null && parameters.size() > 0)
        {
            params.addAll(parameters);
        }


        if (isDebug)
        {
            params.add("/debug+");
        }

        if (rootNamespace != null)
        {
            params.add("/rootnamespace:" + rootNamespace);
        }

        if (delaysign)
        {
            params.add("/delaysign+");
        }

        if (addModules != null && addModules.length != 0)
        {
            params.add("/addmodule:" + listToCommaDelimitedString(addModules));
        }

        if (win32Res != null)
        {
            params.add("/win32res:" + win32Res);
        }

        if (removeintchecks)
        {
            params.add("/removeintchecks+");
        }

        if (win32Icon != null)
        {
            params.add("/win32icon:" + win32Icon);
        }

        if (imports != null && imports.length != 0)
        {
            params.add("/imports:" + listToCommaDelimitedString(imports));
        }

        if (resource != null)
        {
            params.add("/resource:" + resource);
        }

        if (linkResource != null)
        {
            params.add("/linkresource:" + linkResource);
        }

        if (optionexplicit)
        {
            params.add("/optionexplicit+");
        }

        if (optionStrict != null)
        {
            if (optionStrict.equals("+") || optionStrict.equals("-"))
            {
                params.add("/optionstrict" + optionStrict);
            }
            else
            {
                params.add("/optionstrict:" + optionStrict);
            }

        }

        if (optimize)
        {
            params.add("/optimize+");
        }

        if (optionCompare != null)
        {
            params.add("/optioncompare:" + optionCompare);
        }

        if (checked)
        {
            params.add("/checked+");
        }

        if (unsafe)
        {
            params.add("/unsafe+");
        }

        if (noconfig)
        {
            params.add("/noconfig");
        }

        if (baseAddress != null)
        {
            params.add("/baseaddress:" + baseAddress);
        }

        if (bugReport != null)
        {
            params.add("/bugreport:" + bugReport);
        }

        if (codePage != null)
        {
            params.add("/codepage:" + codePage);
        }

        if (utf8output)
        {
            params.add("/utf8output");
        }

        if (pdb != null)
        {
            params.add("/pdb:" + pdb);
        }

        if (errorReport != null)
        {
            params.add("/errorreport:" + errorReport);
        }

        if (moduleAssemblyName != null)
        {
            params.add("/moduleassemblyname:" + moduleAssemblyName);
        }

        if (libs != null && libs.length != 0)
        {
            params.add("/lib:" + listToCommaDelimitedString(libs));
        }

        if (main != null)
        {
            params.add("/main:" + main);
        }

        if (define != null)
        {
            params.add("/define:" + define);
        }


        return params;
    }


}
