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
    extends AbstractMojo
{
    /**
     * The maven project.
     *
     * @parameter expression="${project}"
     * @required
     */
    private MavenProject project;

    /**
     * The location of the local Maven repository.
     *
     * @parameter expression="${settings.localRepository}"
     */
    private File localRepository;

    /**
     * Additional compiler commands
     *
     * @parameter expression = "${parameters}"
     */
    private ArrayList<String> parameters;


    /**
     * Specify a strong name key file.
     *
     * @parameter expression = "${keyfile}"
     */
    private File keyfile;
    
    
    
    /**
     * The starup object class
     *
     * @parameter expression = "${main}"
     */
    private String main;
    
    
    
    /**
     * define
     *
     * @parameter expression = "${define}"
     */
    private String define;

    /**
     * Specifies a strong name key container. (not currently supported)
     *
     * @parameter expression = "${keycontainer}"
     */
    private String keycontainer;

    /**
     * Limit the platforms this code can run on. (not currently supported)
     *
     * @parameter expression = "${platform} default-value = "anycpu"
     */
    private String platform;

    /**
     * The framework version to compile under: 1.1, 2.0, 3.0
     *
     * @parameter expression = "${frameworkVersion}"
     */
    private String frameworkVersion;

    /**
     * The profile that the compiler should use to compile classes: FULL, COMPACT, (or a custom one specified in a
     * compiler-plugins.xml).
     *
     * @parameter expression = "${profile}" default-value = "FULL"
     */
    private String profile;

    /**
     * .NET Language. The default value is <code>C_SHARP</code>. Not case or white-space sensitive.
     *
     * @parameter expression="${language}" default-value = "C_SHARP"
     * @required
     */
    private String language;

   /**
     * Returns the rootnamespace of the project. Used by VB project only.
     *
     * @parameter expression="${rootNamespace}"
     * 
     */
    private String rootNamespace;
    

    /**
     * The Vendor for the Compiler. Not
     * case or white-space sensitive.
     *
     * @parameter expression="${vendor}"
     */
    private String vendor;

    /**
     * This over-rides the defaultAssemblyPath for the compiler plugin.
     *
     * @parameter expression = "${profileAssemblyPath}
     */
    private File profileAssemblyPath;

    /**
     * @parameter expression = "${vendorVersion}"
     */
    private String vendorVersion;

    /**
     * @parameter expression = "${isDebug}" default-value="false"
     */
    private boolean isDebug;

    /**
     * @component
     */
    private org.apache.maven.dotnet.executable.NetExecutableFactory netExecutableFactory;

    /**
     * @parameter expression="${project.file}"
     * @required
     * @readonly
     */
    private File pomFile;
    
    /**
     * Delay-sign the assembly using only the public portion of the strong name key
     *
     * @parameter 
     * 
     */
    private boolean delaysign;
    
    /**
     * Link the specified modules into this assembly
     *
     * @parameter expression="${addmodules}"
     */
    private ArrayList<String> addModules;
 
    /**
     * Specify a Win32 resource file (.res)
     * 
     * @parameter expression = "${win32res}"
     */    
    private String win32Res;
    
    /**
     * Remove integer checks.
     *  @parameter
     */
    private boolean removeintchecks;

    /**
     * Specifies a Win32 icon file (.ico) for the default Win32 resources.
     *
     * @parameter expression = "${win32icon}" 
     * 
     */
    private String win32Icon;  
    
    /**
     * Declare global Imports for namespaces in referenced metadata files.
     *
     * @parameter expression = "${imports}" 
     * 
     */
    private ArrayList<String> imports;
    
    /**
     * Embed the specified resource 
     *
     * @parameter expression = "${resource}" 
     * 
     */
    private String resource;
    
    /**
     * Link the specified resource to this assembly 
     *
     * @parameter expression = "${linkresource}" 
     * 
     */
    private String linkResource;    
    

    /**
     *Require explicit declaration of variables.
     * 
     *  @parameter
     */
    private boolean optionexplicit;

    /**
     *Enforce strict language semantics / Warn when strict language semantics are not respected.
     *
     * @parameter expression = "${optionstrict}" 
     * 
     */
    private String optionStrict;

    /**
     *Enable optimizations.
     * 
     *  @parameter
     */
    private boolean optimize;
    
    /**
     *Specifies binary or text style string comparisons
     *
     * @parameter expression = "${optioncompare}" 
     * 
     */    
    private String optionCompare;
    
    /**
     *Generate overflow checks
     *
     * @parameter
     */    
    private boolean checked;
    
    /**
     *Allow 'unsafe' code
     *
     */    
    private boolean unsafe;
    
    /**
     *Do not auto include CSC.RSP/VBC.RSP file
     *
     * @parameter
     */    
    private boolean noconfig;

    /**
     *Base address for the library to be built
     *
     * @parameter expression = "${baseaddress}"  
     * 
     */    
    private String baseAddress;
    
    /**
     *Create a 'Bug Report' file.
     *
     * @parameter expression = "${bugreport}"  
     * 
     */    
    private String bugReport;
    
    /**
     *Specify the codepage to use when opening source files
     *
     * @parameter expression = "${codepage}"  
     * 
     */    
    private String codePage;
    
    /**
     *Output compiler messages in UTF-8 encoding
     *
     * @parameter
     */    
    private boolean utf8output;        
    
    /**
     *Specify debug information file name (default: output file name with .pdb extension)
     *
     * @parameter expression = "${pdb}" 
     * 
     */    
    private String pdb;

    /**
     *Specify how to handle internal compiler errors: prompt, send, queue, or none. The default is queue.
     *
     * @parameter expression = "${errorreport}" 
     * 
     */        
    private String errorReport;

    /**
     *Name of the assembly which this module will be a part of
     *
     * @parameter expression = "${moduleassemblyname}" 
     * 
     */        
    private String moduleAssemblyName;
    
    /**
     * Specify additional directories to search in for references
     *
     * @parameter expression = "${libs}" 
     * 
     */
    private ArrayList<String> libs;    
    
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

        if ( profileAssemblyPath != null && !profileAssemblyPath.exists() )
        {
            throw new MojoExecutionException( "NMAVEN-900-000: Profile Assembly Path does not exist: Path = " +
                profileAssemblyPath.getAbsolutePath() );
        }

        if ( localRepository == null )
        {
            localRepository = new File( System.getProperty( "user.home" ), ".m2/repository" );
        }

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
            throw new MojoExecutionException( "NMAVEN-900-001: Unknown Vendor: Vendor = " + vendor, e );
        }

        //Config
        CompilerConfig compilerConfig = (CompilerConfig) CompilerConfig.Factory.createDefaultExecutableConfig();
        compilerConfig.setLocalRepository( localRepository );
        if ( parameters == null )
        {
            parameters = new ArrayList<String>();
        }
        if ( isDebug )
        {
            parameters.add( "/debug+" );
        }
		
        if (rootNamespace != null )
        {
            parameters.add( "/rootnamespace:" + rootNamespace );
        }
        
        if ( delaysign )
        {
        	parameters.add( "/delaysign+" );
        }
        
        if ( addModules != null && ! addModules.isEmpty() )
        {	
        	parameters.add( "/addmodule:" + listToCommaDelimitedString(addModules) ) ;        	
        }
        
        if ( win32Res != null )
        {
        	parameters.add( "/win32res:" + win32Res );
        }
        
        if ( removeintchecks )
        {
        	parameters.add( "/removeintchecks+" );
        }
        
        if ( win32Icon != null )
        {
        	parameters.add( "/win32icon:" + win32Icon );
        }
        
        if ( imports != null && ! imports.isEmpty() )
        {
        	parameters.add( "/imports:" + listToCommaDelimitedString(imports) );
        }
        
        if ( resource != null )
        {
        	parameters.add( "/resource:" + resource );
        }
        
        if ( linkResource != null )
        {
        	parameters.add( "/linkresource:" + linkResource );
        }
        
        if ( optionexplicit )
        {
            parameters.add( "/optionexplicit+" );
        }
        
        if ( optionStrict != null )
        {
            if (optionStrict.equals( "+" )||optionStrict.equals( "-" ))
            {
                parameters.add( "/optionstrict" + optionStrict);
            }
            else 
            {
                parameters.add( "/optionstrict:" + optionStrict);
            }
            
        }
        
        if ( optimize )
        {
            parameters.add( "/optimize+" );
        }
        
        if ( optionCompare != null )
        {
            parameters.add( "/optioncompare:"+optionCompare );
        }
        
        if ( checked )
        {
            parameters.add( "/checked+" );
        }
        
        if ( unsafe )
        {
            parameters.add( "/unsafe+" );
        }
        
        if ( noconfig )
        {
            parameters.add( "/noconfig" );
        }
        
        if ( baseAddress != null )
        {
            parameters.add( "/baseaddress:" + baseAddress );
        }
        
        if ( bugReport != null )
        {
            parameters.add( "/bugreport:" + bugReport );
        }
        
        if ( codePage != null )
        {
            parameters.add( "/codepage:" + codePage );
        }
        
        if ( utf8output )
        {
            parameters.add( "/utf8output" );
        }
        
        if ( pdb != null )
        {
            parameters.add( "/pdb:" + pdb );
        }
        
        if ( errorReport != null )
        {
            parameters.add( "/errorreport:" + errorReport );
        }
        
        if ( moduleAssemblyName != null )
        {
            parameters.add( "/moduleassemblyname:" + moduleAssemblyName );
        }
        
        if ( libs != null  && ! libs.isEmpty() )
        {
            parameters.add( "/lib:"+ listToCommaDelimitedString( libs ) );
        }
        

        compilerConfig.setCommands( parameters );

        String artifactTypeName = project.getArtifact().getType();
        ArtifactType artifactType = ArtifactType.getArtifactTypeForPackagingName( artifactTypeName );
        if ( artifactType.equals( ArtifactType.NULL ) )
        {
            throw new MojoExecutionException( "NMAVEN-900-002: Unrecognized artifact type: Language = " + language +
                ", Vendor = " + vendor + ", ArtifactType = " + artifactTypeName );
        }
        compilerConfig.setArtifactType( artifactType );

        if ( keyfile != null )
        {
            KeyInfo keyInfo = KeyInfo.Factory.createDefaultKeyInfo();
            keyInfo.setKeyFileUri( keyfile.getAbsolutePath() );
            compilerConfig.setKeyInfo( keyInfo );
        }
        
        if(main != null)
        {
            parameters.add( "/main:" + main );
        }
        
        if(define != null)
        {
            parameters.add( "/define:" + define);
        }
        

        try
        {
            CompilerExecutable compilerExecutable = netExecutableFactory.getCompilerExecutableFor( compilerRequirement,
                                                                                                   compilerConfig,
                                                                                                   project,
                                                                                                   profileAssemblyPath );
            Boolean sourceFilesUpToDate = (Boolean) super.getPluginContext().get( "SOURCE_FILES_UP_TO_DATE" );
            if ( ( ( sourceFilesUpToDate == null ) || sourceFilesUpToDate ) &&
                System.getProperty( "forceCompile" ) == null && compilerExecutable.getCompiledArtifact() != null &&
                compilerExecutable.getCompiledArtifact().exists() )
            {
                if ( isUpToDateWithPomAndSettingsAndDependencies( compilerExecutable.getCompiledArtifact() ) )
                {
                    getLog().info( "NMAVEN-900-003: Nothing to compile - all classes are up-to-date" );
                    project.getArtifact().setFile( compilerExecutable.getCompiledArtifact() );
                    return;
                }
            }
            long startTimeCompile = System.currentTimeMillis();
            compilerExecutable.execute();
            long endTimeCompile = System.currentTimeMillis();

            getLog().info( "NMAVEN-900-004: Compile Time = " + ( endTimeCompile - startTimeCompile ) + " ms" );
            project.getArtifact().setFile( compilerExecutable.getCompiledArtifact() );
        }
        catch ( PlatformUnsupportedException e )
        {
            throw new MojoExecutionException( "NMAVEN-900-005: Unsupported Platform: Language = " + language +
                ", Vendor = " + vendor + ", ArtifactType = " + artifactTypeName, e );
        }
        catch ( ExecutionException e )
        {
            throw new MojoExecutionException( "NMAVEN-900-006: Unable to Compile: Language = " + language +
                ", Vendor = " + vendor + ", ArtifactType = " + artifactTypeName + ", Source Directory = " +
                project.getBuild().getSourceDirectory(), e );
        }
        long endTime = System.currentTimeMillis();
        getLog().info( "Mojo Execution Time = " + ( endTime - startTime ) );
    }

    private boolean isUpToDateWithPomAndSettingsAndDependencies( File targetFile )
    {
        File settingsFile = new File( localRepository, ".m2/nmaven-settings.xml" );
        Artifact latestDependencyModification =
            this.getLatestDependencyModification( project.getDependencyArtifacts() );

        //TODO: Different parameters from the command line should also cause an update
        //TODO: Change in resource should cause an update
        if ( targetFile.lastModified() < pomFile.lastModified() )
        {
            getLog().info( "NMAVEN-900-007: Project pom has changed. Forcing a recompile." );
            return false;
        }
        else if ( settingsFile.exists() && targetFile.lastModified() < settingsFile.lastModified() )
        {
            getLog().info( "NMAVEN-900-008:Project settings has changed. Forcing a recompile." );
            return false;
        }
        else if ( latestDependencyModification != null &&
            targetFile.lastModified() < latestDependencyModification.getFile().lastModified() )
        {
            getLog().info(
                "NMAVEN-900-009: Detected change in module dependency. Forcing a recompile: Changed Artifact = " +
                    latestDependencyModification );
            return false;
        }
        return true;
    }

    private Artifact getLatestDependencyModification( Set<Artifact> artifacts )
    {
        if ( artifacts == null )
        {
            return null;
        }
        Artifact lastModArtifact = null;
        for ( Artifact artifact : artifacts )
        {
            if ( lastModArtifact == null && !artifact.getType().startsWith( "gac" ) )
            {
                lastModArtifact = artifact;
            }
            else if ( !artifact.getType().startsWith( "gac" ) &&
                artifact.getFile().lastModified() > lastModArtifact.getFile().lastModified() )
            {
                lastModArtifact = artifact;
            }
        }
        return lastModArtifact;
    }
    
    private String listToCommaDelimitedString( List<String> list )
    {
    	StringBuffer sb = new StringBuffer();
    	boolean flag = false;
    	
    	if (list==null || list.size()==0) return "";
    	
    	for (String item: list)
    	{
    		sb.append(flag==true?",":"").append( item.trim() );
    		
    		if (!flag)
    		{	
    			flag = true;
    		}
    	}
    	return sb.toString();
    
    }
}
