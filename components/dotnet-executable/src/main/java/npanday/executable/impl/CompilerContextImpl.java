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
package npanday.executable.impl;

import npanday.ArtifactTypeHelper;
import npanday.executable.CommandExecutor;
import npanday.executable.ExecutionException;
import npanday.executable.CapabilityMatcher;
import npanday.executable.CommandFilter;
import npanday.PlatformUnsupportedException;
import npanday.executable.compiler.*;
import npanday.artifact.ArtifactContext;
import npanday.artifact.ArtifactException;
import npanday.ArtifactType;

import org.apache.maven.project.MavenProject;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.model.Dependency;
import npanday.registry.Repository;
import npanday.registry.RepositoryRegistry;
import npanday.RepositoryNotFoundException;
import npanday.vendor.Vendor;
import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.util.DirectoryScanner;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.StringUtils;

import java.util.*;
import java.io.File;

/**
 * Provides an implementation of the Compiler Context.
 *
 * @author Shane Isbell
 */
public final class CompilerContextImpl
    implements CompilerContext, LogEnabled
{
    /**
     * The maven project
     */
    private MavenProject project;

    private CompilerConfig config;

    private List<Artifact> libraries;
    
    private List<Artifact> directLibraries;

    private List<Artifact> modules;

    private CompilerExecutable netCompiler;

    private CompilerCapability compilerCapability;

    private CompilerRequirement compilerRequirement;

    private CommandFilter commandFilter;

    private ArtifactContext artifactContext;

    private RepositoryRegistry repositoryRegistry;

    /**
     * A logger for writing log messages
     */
    private Logger logger;

    private List<File> linkedResources;

    /** @deprecated */
    private List<File> embeddedResources;

    private List<String> embeddedResourceArgs;

    private File win32icon;

    private List<File> win32resources;

    public List<File> getLinkedResources()
    {
        return linkedResources;
    }

    public List<File> getEmbeddedResources()
    {
        return embeddedResources;
    }

    public List<String> getEmbeddedResourceArgs()
    {
        return embeddedResourceArgs;
    }

    public File getWin32Icon()
    {
        return win32icon;
    }

    public List<File> getWin32Resources()
    {
        return win32resources;
    }

    public void enableLogging( Logger logger )
    {
        this.logger = logger;
    }

    public CompilerRequirement getCompilerRequirement()
    {
        return compilerRequirement;
    }

    public List<String> getCoreAssemblyNames()
    {
        return compilerCapability.getCoreAssemblies();
    }

    public List<Artifact> getModuleDependencies()
    {
        return modules;
    }

    public List<Artifact> getDirectModuleDependencies()
    {
        List<Artifact> artifacts;
        try
        {
            artifacts = artifactContext.getNetModulesFor( project.getArtifact() );
        }
        catch ( ArtifactException e )
        {
            logger.error( "NPANDAY-061-000: Improper Initialization of the Net Modules", e );
            return new ArrayList<Artifact>();
            //TODO: How to handle this: usually implies improper init of ArtifactContext
        }
        if ( config.isTestCompile() && ArtifactTypeHelper.isDotnetModule( config.getArtifactType() ) )
        {
            artifacts.add( project.getArtifact() );
        }

        if ( config.isTestCompile() &&
            ArtifactTypeHelper.isDotnetModule( project.getArtifact().getType() ) &&
            project.getArtifact().getFile() != null && project.getArtifact().getFile().exists() )
        {
            artifacts.add( project.getArtifact() );
        }
        return artifacts;
    }

    public KeyInfo getKeyInfo()
    {
        if ( ( compilerRequirement.getVendor().equals( Vendor.MICROSOFT ) &&
            compilerRequirement.getFrameworkVersion().equals( "1.1.4322" ) ) || config.getKeyInfo() == null )
        {
            return KeyInfo.Factory.createDefaultKeyInfo();
        }
        else
        {
            return config.getKeyInfo();
        }
    }

    public List<Artifact> getLibraryDependencies()
    {   
                
        if ( config.isTestCompile() 
            && ( ArtifactTypeHelper.isDotnetLibrary( config.getArtifactType() ) 
                 || ArtifactTypeHelper.isDotnetMavenPlugin( config.getArtifactType() ))
            && project.getArtifact().getFile() != null && project.getArtifact().getFile().exists()
            && !libraries.contains( project.getArtifact() ) && !ArtifactTypeHelper.isDotnetModule( project.getArtifact().getType() )
           )
        {
            libraries.add( project.getArtifact() );
        }
        return libraries;
    }
    
    public List<Artifact> getDirectLibraryDependencies()
    {   
                
        for ( Iterator i = project.getDependencyArtifacts().iterator(); i.hasNext(); )
        {
                Artifact artifact = (Artifact) i.next();
                
                if ( !hasArtifact(artifact) )
                {
                    directLibraries.add( artifact );
                }
                
                boolean found = false;
                for ( Iterator j = project.getDependencies().iterator(); j.hasNext() && !found; )
                {
                    Dependency dependency  = (Dependency) j.next();
                    if ( dependency.getGroupId().equals( artifact.getGroupId() ) && dependency.getArtifactId().equals(
                        artifact.getArtifactId() ) && dependency.getVersion().equals( artifact.getVersion() ) )
                    {
                        found = true;
                    }
                }

                if ( !found )
                {
                    directLibraries.remove(artifact);
                }
            
                
        }
            
        return directLibraries;
  
    }
    
    private boolean hasArtifact(Artifact artifact)
    {
        for ( Artifact art : directLibraries )
        {
            if ( art.getArtifactId().equals( artifact.getArtifactId() ) )
            {
                return true;
            }
        }
        return false;
    }

    public Logger getLogger()
    {
        return logger;
    }

    public CompilerConfig getNetCompilerConfig()
    {
        return config;
    }

    public CompilerCapability getCompilerCapability()
    {
        return compilerCapability;
    }

    public String getSourceDirectoryName()
    {
        return ( config.isTestCompile() ) ? project.getBuild().getDirectory() + File.separator + "build-test-sources"
            : project.getBuild().getDirectory() + File.separator + "build-sources";
    }

    public File getTargetDirectory()
    {
        return new File( project.getBuild().getDirectory() );
    }

    /**
     * This method will return a File where File.isExist() returns false, if NetCompile.compile has not been
     * invoked.
     *
     * @return
     * @throws InvalidArtifactException
     */
    public File getArtifact()
        throws InvalidArtifactException
    {
        ArtifactType artifactType = config.getArtifactType();
        if ( artifactType == null || artifactType.equals( ArtifactType.NULL ) )
        {
            throw new InvalidArtifactException( "NPANDAY-061-001: Artifact Type cannot be null" );
        }

        //TODO: The test-plugin has a dependency on this fileName/dir. If we change it here, it will break the plugin. Fix this encapsulation issue.
        String fileName = ( config.isTestCompile() ) ? project.getBuild().getDirectory() + File.separator +
            project.getArtifactId() + "-test.dll" : project.getBuild().getDirectory() + File.separator +
            project.getArtifactId() + "." + artifactType.getExtension();
        return new File( fileName );
    }

    public CompilerExecutable getCompilerExecutable()
        throws ExecutionException
    {
        return netCompiler;
    }

    public CommandFilter getCommandFilter()
    {
        return commandFilter;
    }


    public Repository find( String repositoryName )
        throws RepositoryNotFoundException
    {
        Repository repository = repositoryRegistry.find( repositoryName );
        if ( repository == null )
        {
            throw new RepositoryNotFoundException(
                "NPANDAY-061-002: Could not find repository: Name = " + repositoryName );
        }
        return repository;
    }

    private String getGacRootForMono()
        throws PlatformUnsupportedException
    {
        String path = System.getenv( "PATH" );
        if ( path != null )
        {
            String[] tokens = path.split( System.getProperty( "path.separator" ) );
            for ( String token : tokens )
            {
                File gacRoot = new File( new File( token ).getParentFile(), "lib/mono/gac/" );
                if ( gacRoot.exists() )
                {
                    return gacRoot.getAbsolutePath();
                }
            }
        }
        //check settings file

        String monoRoot = System.getenv( "MONO_ROOT" );
        if ( monoRoot != null && !new File( monoRoot ).exists() )
        {
            logger.warn( "MONO_ROOT has been incorrectly set. Trying /usr : MONO_ROOT = " + monoRoot );
        }
        else if ( monoRoot != null )
        {
            return ( !monoRoot.endsWith( File.separator ) ) ? monoRoot + File.separator : monoRoot;
        }

        if ( new File( "/usr/lib/mono/gac/" ).exists() )
        {
            // Linux default location
            return new File( "/usr/lib/mono/gac/" ).getAbsolutePath();
        }
        else if ( new File( "/Library/Frameworks/Mono.framework/Home/lib/mono/gac/" ).exists() )
        {
            // Mac OS X default location
            return new File( "/Library/Frameworks/Mono.framework/Home/lib/mono/gac/" ).getAbsolutePath();
        }
        else
        {
            throw new PlatformUnsupportedException(
                "NPANDAY-061-008: Could not locate Global Assembly Cache for Mono. Try setting the MONO_ROOT environmental variable." );
        }
    }

    public void init( CompilerRequirement compilerRequirement, CompilerConfig config, MavenProject project,
                      CapabilityMatcher capabilityMatcher )
        throws PlatformUnsupportedException
    {

        this.project = project;
        this.config = config;
        this.compilerRequirement = compilerRequirement;
        libraries = new ArrayList<Artifact>();
        directLibraries = new ArrayList<Artifact>();
        modules = new ArrayList<Artifact>();
        artifactContext.init( project, project.getRemoteArtifactRepositories(), config.getLocalRepository() );

        Set<Artifact> artifacts = project.getDependencyArtifacts();//Can add WFC deps prior
        if ( artifacts != null )
        {
            for ( Artifact artifact : artifacts )
            {
                String type = artifact.getType();
                ArtifactType artifactType = ArtifactType.getArtifactTypeForPackagingName( type );
                if ( ArtifactTypeHelper.isDotnetModule( type ))
                {
                    modules.add( artifact );
                }
                else if ( (artifactType != null && (
                            StringUtils.equals( artifactType.getTargetCompileType(), "library" )
                            || artifactType.getExtension().equals( "dll" )
                            || artifactType.getExtension().equals( "exe" ))
                          )
                          || type.equals( "jar" ) )
                {
                    libraries.add( artifact );
                }
                //Resolving here since the GAC path is vendor and framework aware
                else if ( ArtifactTypeHelper.isDotnetGenericGac( type ) )
                {
                    // TODO: Duplicate code with VendorInfoRepositoryImpl.getGlobalAssemblyCacheDirectoryFor
                    String gacRoot = null;
                    if ( compilerRequirement.getVendor().equals( Vendor.MICROSOFT ) &&
                        compilerRequirement.getFrameworkVersion().equals( "1.1.4322" ) )
                    {
                        gacRoot = System.getenv( "SystemRoot" ) + "\\assembly\\GAC\\";
                    }
                    else if ( compilerRequirement.getVendor().equals( Vendor.MICROSOFT ) )
                    {
                        // Layout changed since 2.0
                        // http://discuss.joelonsoftware.com/default.asp?dotnet.12.383883.5
                        gacRoot = System.getenv( "SystemRoot" ) + "\\assembly\\GAC_MSIL\\";
                    }
                    else if ( compilerRequirement.getVendor().equals( Vendor.MONO ) )
                    {
                        gacRoot = getGacRootForMono();
                    }
                    if ( gacRoot != null )
                    {
                        setArtifactGacFile( gacRoot, artifact );
                        libraries.add( artifact );
                    }
                }
                else if ( type.equals( ArtifactType.GAC.getPackagingType() ) )
                {
                    String gacRoot = ( compilerRequirement.getVendor().equals( Vendor.MONO ) ) ? getGacRootForMono()
                        : System.getenv( "SystemRoot" ) + "\\assembly\\GAC\\";
                    setArtifactGacFile( gacRoot, artifact );
                    libraries.add( artifact );
                }
                else if ( type.equals( ArtifactType.GAC_32.getPackagingType() ) )
                {
                    String gacRoot = ( compilerRequirement.getVendor().equals( Vendor.MONO ) ) ? getGacRootForMono()
                        : System.getenv( "SystemRoot" ) + "\\assembly\\GAC_32\\";
                    setArtifactGacFile( gacRoot, artifact );
                    libraries.add( artifact );
                }
                else if ( type.equals( ArtifactType.GAC_MSIL.getPackagingType() ) )
                {
                    String gacRoot = ( compilerRequirement.getVendor().equals( Vendor.MONO ) ) ? getGacRootForMono()
                        : System.getenv( "SystemRoot" ) + "\\assembly\\GAC_MSIL\\";
                    setArtifactGacFile( gacRoot, artifact );
                    libraries.add( artifact );
                }
                else if ( type.equals( ArtifactType.COM_REFERENCE.getPackagingType() ) )
                {
                    moveInteropDllToBuildDirectory( artifact );
                    libraries.add( artifact );
                }
            }
        }

        compilerCapability = capabilityMatcher.matchCompilerCapabilityFor( compilerRequirement );
        String className = compilerCapability.getPluginClassName();

        try
        {
            Class cc = Class.forName( className );
            netCompiler = (CompilerExecutable) cc.newInstance();
            netCompiler.init( this );//TODO: Add ArtifactInfo?
        }
        catch ( ClassNotFoundException e )
        {
            throw new PlatformUnsupportedException(
                "NPANDAY-061-004: Unable to create NetCompiler: Class Name = " + className, e );
        }
        catch ( InstantiationException e )
        {
            throw new PlatformUnsupportedException(
                "NPANDAY-061-005: Unable to create NetCompiler: Class Name = " + className, e );
        }
        catch ( IllegalAccessException e )
        {
            throw new PlatformUnsupportedException(
                "NPANDAY-061-006: Unable to create NetCompiler: Class Name = " + className, e );
        }
        commandFilter =
            CommandFilter.Factory.createDefaultCommandFilter( compilerCapability.getCommandCapability(), logger );

        String basedir = project.getBuild().getDirectory() + File.separator + "assembly-resources" + File.separator;
        linkedResources = new File( basedir, "linkresource" ).exists() ? Arrays.asList(
            new File( basedir, "linkresource" ).listFiles() ) : new ArrayList<File>();
        getEmbeddedResources( new File( basedir, "resource" ) );
        win32resources = new File( basedir, "win32res" ).exists() ? Arrays.asList(
            new File( basedir, "win32res" ).listFiles() ) : new ArrayList<File>();
        File win32IconDir = new File( basedir, "win32icon" );
        if ( win32IconDir.exists() )
        {
            File[] icons = win32IconDir.listFiles();
            if ( icons.length > 1 )
            {
                throw new PlatformUnsupportedException(
                    "NPANDAY-061-007: There is more than one win32icon in resource directory: Number = " + icons
                        .length );
            }
            if ( icons.length == 1 )
            {
                win32icon = icons[0];
            }
        }
    }

    private void getEmbeddedResources( File basedir )
    {
        List<File> embeddedResources = new ArrayList<File>();
        List<String> embeddedResourceArgs = new ArrayList<String>();
        if ( basedir.exists() )
        {
            DirectoryScanner scanner = new DirectoryScanner();
            scanner.setBasedir( basedir );
            scanner.scan();

            for ( String file : scanner.getIncludedFiles() )
            {
                File f = new File(basedir, file);
                embeddedResources.add(f);
                if (f.getName().endsWith(".resources")) {
                    embeddedResourceArgs.add(f.getAbsolutePath());
                } else {
                    String resourceName = project.getArtifactId() + "." + file.replace(File.separatorChar, '.');
                    embeddedResourceArgs.add(f.getAbsolutePath() + "," + resourceName);
                }
            }
        }
        this.embeddedResources = embeddedResources;
        this.embeddedResourceArgs = embeddedResourceArgs;
    }

    private void moveInteropDllToBuildDirectory(Artifact artifact) throws PlatformUnsupportedException
    {
        try
        {
            File file = artifact.getFile();
            String oldPath = file.getAbsolutePath();
            String target = project.getBuild().getDirectory();
            String newPath = target + File.separator + "Interop." + artifact.getArtifactId() + ".dll";
            
            if ( oldPath.contains( target ) ) //already copied to target
                return ; 
            
            logger.info( "NPANDAY-000-000:[COM Reference] copying file ["+ oldPath+"] to [" + target +"]" );
            FileUtils.copyFileToDirectory( file, new File( target ) );
            
            logger.info( "NPANDAY-000-000:[COM Reference] deleting directory ["+ file.getParentFile() +"]" );
            FileUtils.deleteDirectory( file.getParentFile() );
            
            logger.info( "NPANDAY-000-000:[COM Reference] updating artifact path to ["+ newPath +"]" );
            
            artifact.setFile( new File( newPath ) );
        }catch(Exception e)
        {
            throw new PlatformUnsupportedException (e);
        }
        
    }
    
    /*
     * Installs the artifact to the gac so that it can be used in aspnet
     */
    private void installArtifactGacFile(Artifact artifact)
    {
        try
        {
            CommandExecutor commandExecutor = CommandExecutor.Factory.createDefaultCommmandExecutor();
            
            String executable = "gacutil";
            List<String> commands = new ArrayList<String>();
            
            //searching for the .dll to be installed.
            String sourceDir =  config.getIncludeSources().get( 0 );
            String[] sourceDirTokens = sourceDir.split( "\\\\" );
            String sDir = "";
            
            //constructing the directory for the.dll 
            for(int i=0;i<sourceDirTokens.length-3;i++)
            {
                if(sDir.equalsIgnoreCase( "" ))
                {
                    sDir = sourceDirTokens[i];
                }
                else
                {
                    sDir = sDir +"\\"+sourceDirTokens[i];
                }
                
            }
            
            String dll = artifact.getArtifactId()+".dll";
            String dllSysPath ="";
            List<File> potentialDlls= FileUtils.getFiles( new File(sDir), "**" , null );
            
            for(File cFile: potentialDlls)
            {
                String pSysPath = cFile.getAbsolutePath(); 
                String[] pathTokens = pSysPath.split( "\\\\" );
                if(pathTokens[pathTokens.length-1].equalsIgnoreCase( dll ) ) 
                {
                    dllSysPath = cFile.getAbsolutePath();
                    //break;
                }
            }
            
            commands.add( "/i "+dllSysPath );
            commandExecutor.executeCommand( executable, commands);
        }
        catch(Exception e)
        {
            System.out.println("NPANDAY-000-000: Could not install artifact to GAC artifact:" +artifact.getArtifactId());
        }
         
    }

    private void setArtifactGacFile( String gacRoot, Artifact artifact )
        throws PlatformUnsupportedException
    {
        // TODO: Refactor to PathUtil.getGlobalAssemblyCacheFileFor

        File gacFile = new File( gacRoot, artifact.getArtifactId() + File.separator + artifact.getVersion() + "__" +
            artifact.getClassifier() + File.separator + artifact.getArtifactId() + ".dll" );
        // first check if the artifact is not yet installed
        if ( !gacFile.exists() )
        {
            installArtifactGacFile(artifact);
        }
        // after installing the gac check if it is installed in the system.
        if ( !gacFile.exists() )
        {
            // TODO: this will only work on Windows
            //check for gac_msil
            gacRoot = System.getenv( "SystemRoot" ) + "\\assembly\\GAC_MSIL\\";
            gacFile = new File( gacRoot, artifact.getArtifactId() + File.separator + artifact.getVersion() + "__" +
                                artifact.getClassifier() + File.separator + artifact.getArtifactId() + ".dll" );
            if ( !gacFile.exists() )
            {
                
                throw new PlatformUnsupportedException(
                                                       "NPANDAY-000-000: Could not find GAC dependency: File = " + gacFile.getAbsolutePath() );
            }
            
            
        }
        artifact.setFile( gacFile );
    }
}
