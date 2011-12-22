package npanday.executable.compiler.impl;

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
import npanday.executable.CommandFilter;
import npanday.executable.ExecutionException;
import npanday.vendor.Vendor;
import org.apache.maven.artifact.Artifact;
import org.codehaus.plexus.util.FileUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

/**
 * A default compiler that can be used in most cases.
 *
 * @author Shane Isbell
 */
public final class DefaultCompiler
    extends BaseCompiler
{
    public boolean failOnErrorOutput()
    {
        //MONO writes warnings to standard error: this turns off failing builds on warnings for MONO
        // TODO: how do we detected mono compile errors then????
        return !compilerContext.getVendor().equals( Vendor.MONO );
    }

    public List<String> getCommands() throws ExecutionException, PlatformUnsupportedException
    {
        if ( compilerContext == null )
        {
            throw new ExecutionException( "NPANDAY-068-000: Compiler has not been initialized with a context" );
        }
        // references uses directLibraryDependencies for non transitive dependencies
        List<Artifact> references = compilerContext.getDirectLibraryDependencies();
        List<Artifact> modules = compilerContext.getDirectModuleDependencies();

        String sourceDirectory = compilerContext.getSourceDirectoryName();
        String artifactFilePath = compilerContext.getArtifact().getAbsolutePath();
        String targetArtifactType = compilerContext.getTargetArtifactType().getTargetCompileType();

        compilerContext.getFrameworkVersion();

        List<String> commands = new ArrayList<String>();



        if(compilerContext.getOutputDirectory() != null)
        {
            File f = new File(compilerContext.getOutputDirectory(), compilerContext.getArtifact().getName());
            artifactFilePath = f.getAbsolutePath();
        }

        if(artifactFilePath!=null && artifactFilePath.toLowerCase().endsWith(".zip"))
        {
        	artifactFilePath = artifactFilePath.substring(0, artifactFilePath.length() - 3) + "dll";
        }
        
        commands.add( "/out:" + artifactFilePath);
    

        commands.add( "/target:" + targetArtifactType );
        if(compilerContext.getIncludeSources() == null || compilerContext.getIncludeSources().isEmpty() )
        {
            commands.add( "/recurse:" + sourceDirectory + File.separator + "**");
        }
        if ( modules != null && !modules.isEmpty() )
        {
            StringBuffer sb = new StringBuffer();
            for ( Iterator i = modules.iterator(); i.hasNext(); )
            {
                Artifact artifact = (Artifact) i.next();
                String path = artifact.getFile().getAbsolutePath();
                sb.append( path );
                if ( i.hasNext() )
                {
                    sb.append( ";" );
                }
            }
            commands.add( "/addmodule:" + sb.toString() );
        }
        if ( !references.isEmpty() )
        {
         
            for ( Artifact artifact : references )
            {
                String path = artifact.getFile().getAbsolutePath();
          
                if( !path.contains( ".jar" ) )
                {
                    commands.add( "/reference:" + path );
                }
            }
        }
        for ( String arg : compilerContext.getEmbeddedResourceArgs() )
        {
            if (logger.isDebugEnabled()) 
            {
                logger.debug( "NPANDAY-168-001 add resource: " + arg );
            }
        
            commands.add( "/resource:" + arg );
        }
        for ( File file : compilerContext.getLinkedResources() )
        {
            commands.add( "/linkresource:" + file.getAbsolutePath() );
        }
        for ( File file : compilerContext.getWin32Resources() )
        {
            commands.add( "/win32res:" + file.getAbsolutePath() );
        }
        if ( compilerContext.getWin32Icon() != null )
        {
            commands.add( "/win32icon:" + compilerContext.getWin32Icon().getAbsolutePath() );
        }

        if ( compilerContext.getVendor().equals( Vendor.MICROSOFT ) )
        {
            commands.add( "/nologo" );
        }

        // TODO: this should rather be a custom compiler class for this combination of vendor and framework!!
        if ( compilerContext.getVendor().equals( Vendor.MICROSOFT ) &&
            compilerContext.getFrameworkVersion().equals( "3.0" ) )
        {
            String wcfRef = "/reference:" + System.getenv( "SystemRoot" ) +
                "\\Microsoft.NET\\Framework\\v3.0\\Windows Communication Foundation\\";
            //TODO: This is a hard-coded path: Don't have a registry value either.
            //commands.add( wcfRef + "System.ServiceModel.dll" );
            commands.add( wcfRef + "Microsoft.Transactions.Bridge.dll" );
            commands.add( wcfRef + "Microsoft.Transactions.Bridge.Dtc.dll" );
            commands.add( wcfRef + "System.ServiceModel.Install.dll" );
            commands.add( wcfRef + "System.ServiceModel.WasHosting.dll" );
            //commands.add( wcfRef + "System.Runtime.Serialization.dll" );
            commands.add( wcfRef + "SMDiagnostics.dll" );
        }

        // TODO: this should rather be a custom compiler class for this combination of vendor and framework!!
        if ( compilerContext.getVendor().equals( Vendor.MICROSOFT ) &&
            compilerContext.getFrameworkVersion().equals( "3.5" ) )
        {
            String wcfRef = "/reference:" + System.getenv( "SystemRoot" ) +
                "\\Microsoft.NET\\Framework\\v3.5\\";
            //TODO: This is a hard-coded path: Don't have a registry value either.
            commands.add( wcfRef + "Microsoft.Build.Tasks.v3.5.dll" );

            String cfBuildTasks = wcfRef + "Microsoft.CompactFramework.Build.Tasks.dll";
            if (new File( cfBuildTasks ).exists())
            {
                commands.add( cfBuildTasks );
            }

            commands.add( wcfRef + "Microsoft.Data.Entity.Build.Tasks.dll" );
            commands.add( wcfRef + "Microsoft.VisualC.STLCLR.dll" );
        }

        // TODO: this should rather be a custom compiler class for this combination of vendor and framework!!
        if ( compilerContext.getVendor().equals( Vendor.MICROSOFT ) &&
            compilerContext.getFrameworkVersion().equals( "4.0" ) )
        {
            String frameworkPath = System.getenv( "SystemRoot" ) + "\\Microsoft.NET\\Framework\\v4.0.30319\\";
            //TODO: This is a hard-coded path: Don't have a registry value either.
            List<String> libraryNames = Arrays.asList("Microsoft.Build.Tasks.v4.0.dll", 
                "Microsoft.Data.Entity.Build.Tasks.dll", "Microsoft.VisualC.STLCLR.dll");
            for (String libraryName : libraryNames) 
            {
                String libraryFullName = frameworkPath + libraryName;
                if (new File( libraryFullName ).exists())
                {
                    commands.add( "/reference:" + libraryFullName );
                }
            }
        } 
        if ( compilerContext.getKeyInfo().getKeyFileUri() != null )
        {
            commands.add( "/keyfile:" + compilerContext.getKeyInfo().getKeyFileUri() );
        }
        else if ( compilerContext.getKeyInfo().getKeyContainerName() != null )
        {
            commands.add( "/keycontainer:" + compilerContext.getKeyInfo().getKeyContainerName() );
        }

        if ( compilerContext.getCommands() != null )
        {
            commands.addAll( compilerContext.getCommands() );
        }
        commands.add( "/warnaserror-" );
        //commands.add( "/nowarn" );

        // TODO: this should rather be a custom compiler class for FULL profile
        if ( compilerContext.getVendor().equals( Vendor.MONO ) &&
                 "FULL".equals( compilerContext.getTargetProfile() ) )
        {
            commands.add( "/nostdlib" );
            commands.add( "/noconfig" );
            commands.add( "/reference:mscorlib" );
            commands.add( "/reference:System.Data" );
            commands.add( "/reference:System" );
            commands.add( "/reference:System.Drawing" );
            commands.add( "/reference:System.Messaging" );
            commands.add( "/reference:System.Web.Services" );
            commands.add( "/reference:System.Windows.Forms" );
            commands.add( "/reference:System.Xml" );
            commands.add( "/reference:System.Core" );
            commands.add( "/reference:System.Data.DataSetExtensions" );
            commands.add( "/reference:System.Xml.Linq" );
        }
        if ( !compilerContext.isTestCompile() )
        {
            commands.add(
                "/doc:" + new File( compilerContext.getTargetDirectory(), "comments.xml" ).getAbsolutePath() );
        }

        CommandFilter filter = compilerContext.getCommandFilter();
        
        List<String> filteredCommands = filter.filter( commands );
        //Include Sources code is being copied to temporary folder for the recurse option
        
        String fileExt = "";
        String frameWorkVer = ""+compilerContext.getFrameworkVersion();
        String TempDir = "";
        String targetDir = ""+compilerContext.getTargetDirectory();
        
        Date date = new Date();
        String Now =""+date.getDate()+date.getHours()+date.getMinutes()+date.getSeconds();

        // TODO: Why can't the tmp dir just be a static one?
        TempDir = targetDir+File.separator+Now;
        
        try
        {
            FileUtils.deleteDirectory( TempDir );
        }
        catch(Exception e)
        {
            //Does Precautionary delete for tempDir 
        }
        
        FileUtils.mkdir(TempDir); 

        // TODO: move includeSources expansion to the compiler context (handle excludes there)
        if(compilerContext.getIncludeSources() != null && !compilerContext.getIncludeSources().isEmpty() )
        {
            int folderCtr=0;
            for(String includeSource : compilerContext.getIncludeSources())
            {
                
                String[] sourceTokens = includeSource.replace('\\', '/').split("/");
                
                String lastToken = sourceTokens[sourceTokens.length-1];
                
                if(fileExt=="")
                {
                    
                    String[] extToken = lastToken.split( "\\." );
                    fileExt = "."+extToken[extToken.length-1];
                }
                
                try
                {
                    String fileToCheck = TempDir+File.separator+lastToken;
                    if(FileUtils.fileExists( fileToCheck ))
                    {
                        String subTempDir = TempDir+File.separator+folderCtr+File.separator; 
                        FileUtils.mkdir( subTempDir );
                        FileUtils.copyFileToDirectory( includeSource, subTempDir);
                        folderCtr++;
                        
                    }
                    else
                    {
                        FileUtils.copyFileToDirectory( includeSource, TempDir);
                    }
                }
                catch(Exception e)
                {
                    System.out.println(e.getMessage());
                }
                //part of original code.
                //filteredCommands.add(includeSource);
            }
            String recurseCmd = "/recurse:" + TempDir+File.separator + "*" + fileExt + "";
            filteredCommands.add(recurseCmd);
            
        }
        if ( logger.isDebugEnabled() )
        {
            logger.debug( "commands: " + filteredCommands );
        }
        String responseFilePath = TempDir + File.separator + "responsefile.rsp";
        try
        {
            for(String command : filteredCommands)
            {
                FileUtils.fileAppend(responseFilePath, escapeCmdParams(command) + " ");
            }
        } catch (java.io.IOException e) {
            throw new ExecutionException( "Error while creating response file for the commands.", e );
        }
        filteredCommands.clear();
        responseFilePath = "@" + responseFilePath;
        if ( responseFilePath.indexOf( " " ) > 0)
        {
            responseFilePath = "\"" + responseFilePath + "\"";
        }

        filteredCommands.add( responseFilePath );
        
        return filteredCommands;
    }

    // escaped to make use of dotnet style of command escapes .
    // Eg. /define:"CONFIG=\"Debug\",DEBUG=-1,TRACE=-1,_MyType=\"Windows\",PLATFORM=\"AnyCPU\""
    private String escapeCmdParams(String param)
    {
        if(param == null)
            return null;
        
        String str = param;
        if((param.startsWith("/") || param.startsWith("@")) && param.indexOf(":") > 0)
        {
            int delem = param.indexOf(":") + 1;
            String command = param.substring(0, delem);
            String value = param.substring(delem);
            
            if(value.indexOf(" ") > 0 || value.indexOf("\"") > 0)
            {
                value = "\"" + value.replaceAll("\"", "\\\\\"")  + "\"";
            }
            
            str = command + value;
        }
        else if(param.indexOf(" ") > 0)
        {
            str = "\"" + param  + "\"";
        }
        
        return str;
    }
}
