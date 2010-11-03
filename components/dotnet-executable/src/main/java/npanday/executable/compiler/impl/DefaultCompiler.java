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
package npanday.executable.compiler.impl;

import org.apache.maven.artifact.Artifact;
import org.codehaus.plexus.util.FileUtils;

import java.util.Date;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.io.File;

import npanday.executable.CommandFilter;
import npanday.executable.ExecutionException;
import npanday.vendor.Vendor;
import npanday.executable.compiler.CompilerConfig;

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
        return !compilerContext.getCompilerRequirement().getVendor().equals( Vendor.MONO );
    }

    public List<String> getCommands()
        throws ExecutionException
    {
        if ( compilerContext == null )
        {
            throw new ExecutionException( "NPANDAY-068-000: Compiler has not been initialized with a context" );
        }
        CompilerConfig config = compilerContext.getNetCompilerConfig();
        // references uses directLibraryDependencies for non transitive dependencies
        List<Artifact> references = compilerContext.getDirectLibraryDependencies();
        List<Artifact> modules = compilerContext.getDirectModuleDependencies();

        String sourceDirectory = compilerContext.getSourceDirectoryName();
        String artifactFilePath = compilerContext.getArtifact().getAbsolutePath();
        String targetArtifactType = config.getArtifactType().getTargetCompileType();

        compilerContext.getCompilerRequirement().getFrameworkVersion();

        List<String> commands = new ArrayList<String>();



        if(config.getOutputDirectory() != null)
        {
            File f = new File(config.getOutputDirectory(), compilerContext.getArtifact().getName());
            artifactFilePath = f.getAbsolutePath();
        }

        if(artifactFilePath!=null && artifactFilePath.toLowerCase().endsWith(".zip"))
        {
        	artifactFilePath = artifactFilePath.substring(0, artifactFilePath.length() - 3) + "dll";
        }
        
        commands.add( "/out:" + artifactFilePath);
    

        commands.add( "/target:" + targetArtifactType );
        if(config.getIncludeSources() == null || config.getIncludeSources().isEmpty() )
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

        if ( compilerContext.getCompilerRequirement().getVendor().equals( Vendor.MICROSOFT ) )
        {
            commands.add( "/nologo" );
        }

        if ( compilerContext.getCompilerRequirement().getVendor().equals( Vendor.MICROSOFT ) &&
            compilerContext.getCompilerRequirement().getFrameworkVersion().equals( "3.0" ) )
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

        if ( compilerContext.getCompilerRequirement().getVendor().equals( Vendor.MICROSOFT ) &&
            compilerContext.getCompilerRequirement().getFrameworkVersion().equals( "3.5" ) )
        {
            String wcfRef = "/reference:" + System.getenv( "SystemRoot" ) +
                "\\Microsoft.NET\\Framework\\v3.5\\";
            //TODO: This is a hard-coded path: Don't have a registry value either.
            commands.add( wcfRef + "Microsoft.Build.Tasks.v3.5.dll" );
            commands.add( wcfRef + "Microsoft.CompactFramework.Build.Tasks.dll" );
            commands.add( wcfRef + "Microsoft.Data.Entity.Build.Tasks.dll" );
            commands.add( wcfRef + "Microsoft.VisualC.STLCLR.dll" );
        }
 
        if ( compilerContext.getCompilerRequirement().getVendor().equals( Vendor.MICROSOFT ) &&
            compilerContext.getCompilerRequirement().getFrameworkVersion().equals( "4.0" ) )
        {
            String wcfRef = "/reference:" + System.getenv( "SystemRoot" ) +
                "\\Microsoft.NET\\Framework\\v4.0.30319\\";
            //TODO: This is a hard-coded path: Don't have a registry value either.
            commands.add( wcfRef + "Microsoft.Build.Tasks.v4.0.dll" );
            commands.add( wcfRef + "Microsoft.Data.Entity.Build.Tasks.dll" );
            commands.add( wcfRef + "Microsoft.VisualC.STLCLR.dll" );
        } 
        if ( compilerContext.getKeyInfo().getKeyFileUri() != null )
        {
            commands.add( "/keyfile:" + compilerContext.getKeyInfo().getKeyFileUri() );
        }
        else if ( compilerContext.getKeyInfo().getKeyContainerName() != null )
        {
            commands.add( "/keycontainer:" + compilerContext.getKeyInfo().getKeyContainerName() );
        }

        if ( config.getCommands() != null )
        {
            commands.addAll( config.getCommands() );
        }
        commands.add( "/warnaserror-" );
        //commands.add( "/nowarn" );
//        if ( compilerContext.getCompilerRequirement().getVendor().equals( Vendor.MONO ) )
//        {
//            commands.add( "/reference:System.Drawing" );
//            commands.add( "/reference:System.Windows.Forms" );
//            commands.add( "/reference:System.Web.Services" );
//        }
        if ( !compilerContext.getNetCompilerConfig().isTestCompile() )
        {
            commands.add(
                "/doc:" + new File( compilerContext.getTargetDirectory(), "comments.xml" ).getAbsolutePath() );
        }

        CommandFilter filter = compilerContext.getCommandFilter();
        
        List<String> filteredCommands = filter.filter( commands );
        //Include Sources code is being copied to temporary folder for the recurse option
        
        String fileExt = "";
        String frameWorkVer = ""+compilerContext.getCompilerRequirement().getFrameworkVersion(); 
        String TempDir = "";
        String targetDir = ""+compilerContext.getTargetDirectory();
        
        Date date = new Date();
        String Now =""+date.getDate()+date.getHours()+date.getMinutes()+date.getSeconds();
               
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
        
        if(config.getIncludeSources() != null && !config.getIncludeSources().isEmpty() )
        {
            int folderCtr=0;
            for(String includeSource : config.getIncludeSources())
            {
                
                String[] sourceTokens = includeSource.split(File.separator+File.separator);
                
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
        filteredCommands.add("@" + escapeCmdParams(responseFilePath) );
        
        return filteredCommands;
    }

    public void resetCommands( List<String> commands )
    {

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
