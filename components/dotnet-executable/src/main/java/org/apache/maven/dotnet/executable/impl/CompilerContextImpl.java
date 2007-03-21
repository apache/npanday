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
package org.apache.maven.dotnet.executable.impl;

import org.apache.maven.dotnet.executable.ExecutionException;
import org.apache.maven.dotnet.executable.CapabilityMatcher;
import org.apache.maven.dotnet.executable.CommandFilter;
import org.apache.maven.dotnet.PlatformUnsupportedException;
import org.apache.maven.dotnet.executable.compiler.*;
import org.apache.maven.dotnet.executable.compiler.InvalidArtifactException;
import org.apache.maven.dotnet.artifact.ArtifactContext;
import org.apache.maven.dotnet.artifact.ArtifactException;
import org.apache.maven.dotnet.artifact.ArtifactType;

import org.apache.maven.project.MavenProject;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.dotnet.registry.Repository;
import org.apache.maven.dotnet.registry.RepositoryRegistry;
import org.apache.maven.dotnet.RepositoryNotFoundException;
import org.apache.maven.dotnet.vendor.Vendor;
import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;

import java.util.*;
import java.io.File;

/**
 * @author Shane Isbell
 */
public final class CompilerContextImpl
    implements CompilerContext, LogEnabled
{

    private MavenProject project;

    private CompilerConfig config;

    private List<Artifact> libraries;

    private List<Artifact> modules;

    private CompilerExecutable netCompiler;

    private CompilerCapability compilerCapability;

    private CompilerRequirement compilerRequirement;

    private CommandFilter commandFilter;

    private ArtifactContext artifactContext;

    private RepositoryRegistry repositoryRegistry;

    private Logger logger;

    private List<File> linkedResources;

    private List<File> embeddedResources;

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
            logger.error( "NMAVEN-061-000: Improper Initialization of the Net Modules", e );
            return null;
            //TODO: How to handle this: usually implies improper init of ArtifactContext
        }
        if ( config.isTestCompile() && config.getArtifactType().equals( ArtifactType.MODULE ) )
        {
            artifacts.add( project.getArtifact() );
        }

        if ( config.isTestCompile() &&
            project.getArtifact().getType().equals( ArtifactType.MODULE.getArtifactTypeName() ) &&
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
        if ( config.isTestCompile() && config.getArtifactType().equals( ArtifactType.LIBRARY ) &&
            project.getArtifact().getFile() != null && project.getArtifact().getFile().exists() &&
            !libraries.contains( project.getArtifact() ) && !project.getArtifact().getType().equals( "module" ) )
        {
            libraries.add( project.getArtifact() );
        }

        return libraries;
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
            throw new InvalidArtifactException( "NMAVEN-061-001: Artifact Type cannot be null" );
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


    public MavenProject getMavenProject()
    {
        return project;
    }

    public Repository find( String repositoryName )
        throws RepositoryNotFoundException
    {
        Repository repository = repositoryRegistry.find( repositoryName );
        if ( repository == null )
        {
            throw new RepositoryNotFoundException(
                "NMAVEN-061-002: Could not find repository: Name = " + repositoryName );
        }
        return repository;
    }

    public void init( CompilerRequirement compilerRequirement, CompilerConfig config, MavenProject project,
                      CapabilityMatcher capabilityMatcher )
        throws PlatformUnsupportedException
    {

        this.project = project;
        this.config = config;
        this.compilerRequirement = compilerRequirement;
        libraries = new ArrayList<Artifact>();
        modules = new ArrayList<Artifact>();
        artifactContext.init( project, config.getLocalRepository() );

        Set<Artifact> artifacts = project.getDependencyArtifacts();//Can add WFC deps prior
        for ( Artifact artifact : artifacts )
        {
            String type = artifact.getType();
            if ( type.equals( "module" ) )
            {
                modules.add( artifact );
            }
            else if ( type.equals( "library" ) )
            {
                libraries.add( artifact );
            }
            //Resolving here since the GAC path is vendor and framework aware
            //TODO: Add support for 32/64 bit GACs
            else if ( type.equals( "gac" ) )
            {
                String gacRoot = null;
                if ( compilerRequirement.getVendor().equals( Vendor.MICROSOFT ) && (
                    compilerRequirement.getFrameworkVersion().equals( "2.0.50727" ) ||
                        compilerRequirement.getFrameworkVersion().equals( "3.0" ) ) )
                {
                    gacRoot = "C:\\WINDOWS\\assembly\\GAC_MSIL\\";
                }
                else if ( compilerRequirement.getVendor().equals( Vendor.MICROSOFT ) &&
                    compilerRequirement.getFrameworkVersion().equals( "1.1.4322" ) )
                {
                    gacRoot = "C:\\WINDOWS\\assembly\\GAC\\";
                }
                else if ( compilerRequirement.getVendor().equals( Vendor.MONO ) )
                {
                    //TODO: MONO Support
                }
                if ( gacRoot != null )
                {
                    File gacFile = new File( gacRoot + artifact.getArtifactId() + File.separator +
                        artifact.getVersion() + File.separator + artifact.getArtifactId() + ".dll" );
                    if ( !gacFile.exists() )
                    {
                        throw new PlatformUnsupportedException(
                            "NMAVEN-000-000: Could not find GAC dependency: File = " + gacFile.getAbsolutePath() );
                    }
                    artifact.setFile( gacFile );
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
                "NMAVEN-061-004: Unable to create NetCompiler: Class Name = " + className, e );
        }
        catch ( InstantiationException e )
        {
            throw new PlatformUnsupportedException(
                "NMAVEN-061-005: Unable to create NetCompiler: Class Name = " + className, e );
        }
        catch ( IllegalAccessException e )
        {
            throw new PlatformUnsupportedException(
                "NMAVEN-061-006: Unable to create NetCompiler: Class Name = " + className, e );
        }
        commandFilter =
            CommandFilter.Factory.createDefaultCommandFilter( compilerCapability.getCommandCapability(), logger );

        String basedir = project.getBuild().getDirectory() + File.separator + "assembly-resources" + File.separator;
        linkedResources = new File( basedir, "linkresource" ).exists() ? Arrays.asList(
            new File( basedir, "linkresource" ).listFiles() ) : new ArrayList<File>();
        embeddedResources = new File( basedir, "resource" ).exists() ? Arrays.asList(
            new File( basedir, "resource" ).listFiles() ) : new ArrayList<File>();
        win32resources = new File( basedir, "win32res" ).exists() ? Arrays.asList(
            new File( basedir, "win32res" ).listFiles() ) : new ArrayList<File>();
        File win32IconDir = new File( basedir, "win32icon" );
        if ( win32IconDir.exists() )
        {
            File[] icons = win32IconDir.listFiles();
            if ( icons.length > 1 )
            {
                throw new PlatformUnsupportedException(
                    "NMAVEN-061-007: There is more than one win32icon in resource directory: Number = " + icons
                        .length );
            }
            if ( icons.length == 1 )
            {
                win32icon = icons[0];
            }
        }
    }
}
