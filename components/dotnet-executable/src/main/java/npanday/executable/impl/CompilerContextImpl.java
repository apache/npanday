package npanday.executable.impl;

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

import com.google.common.base.Objects;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import npanday.ArtifactType;
import npanday.ArtifactTypeHelper;
import npanday.PlatformUnsupportedException;
import npanday.RepositoryNotFoundException;
import npanday.executable.ExecutionException;
import npanday.executable.compiler.CompilerCapability;
import npanday.executable.compiler.CompilerConfig;
import npanday.executable.compiler.CompilerContext;
import npanday.executable.compiler.CompilerExecutable;
import npanday.executable.compiler.InvalidArtifactException;
import npanday.executable.compiler.KeyInfo;
import npanday.registry.Repository;
import npanday.registry.RepositoryRegistry;
import npanday.vendor.Vendor;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.resolver.filter.ScopeArtifactFilter;
import org.apache.maven.model.Dependency;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.util.DirectoryScanner;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.StringUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Provides an implementation of the Compiler Context.
 *
 * @author Shane Isbell
 * @plexus.component role="npanday.executable.compiler.CompilerContext"
 */
public final class CompilerContextImpl
    extends ExecutableContextImpl
    implements CompilerContext, LogEnabled
{

    private String NEW_LINE = System.getProperty( "line.separator" );

    /**
     * The maven project
     */
    private MavenProject project;

    private CompilerConfig config;

    private CompilerCapability compilerCapability;


    private List<Artifact> libraries;

    private List<Artifact> directLibraries;

    private List<Artifact> modules;

    /**
     * @plexus.requirement
     */
    private RepositoryRegistry repositoryRegistry;

    /**
     * A logger for writing log messages
     */
    private Logger logger;

    private List<File> linkedResources;

    /**
     * @deprecated
     */
    private List<File> embeddedResources;

    private List<String> embeddedResourceArgs;

    private File win32icon;

    private List<File> win32resources;

    public List<File> getLinkedResources()
    {
        return linkedResources;
    }


    @Deprecated
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

    public File getAssemblyPath()
    {
        return Objects.firstNonNull( config.getAssemblyPath(), compilerCapability.getAssemblyPath() );
    }

    public String getTargetFramework()
    {
        // TODO: Target framework could be overridden through the config here...
        return compilerCapability.getTargetFramework();
    }

    public String getTargetProfile()
    {
        return compilerCapability.getProfile();
    }

    public ArtifactType getTargetArtifactType()
    {
        return config.getArtifactType();
    }

    public String getFrameworkVersion()
    {
        return compilerCapability.getVendorInfo().getFrameworkVersion();
    }

    public boolean isTestCompile()
    {
        return config.isTestCompile();
    }

    public boolean shouldCompile()
    {
        if ( isTestCompile() && getSourceFiles().size() == 0 )
        {
            logger.info( "NPANDAY-061-017: Skipping test compile; no sources found" );
            return false;
        }

        return true;
    }

    public void enableLogging( Logger logger )
    {
        this.logger = logger;
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
        List<Artifact> artifacts = Lists.newArrayList();

        if ( config.isTestCompile() && ArtifactTypeHelper.isDotnetModule( config.getArtifactType() ) )
        {
            artifacts.add( project.getArtifact() );
        }

        if ( config.isTestCompile() && ArtifactTypeHelper.isDotnetModule( project.getArtifact().getType() )
            && project.getArtifact().getFile() != null && project.getArtifact().getFile().exists() )
        {
            artifacts.add( project.getArtifact() );
        }
        return artifacts;
    }

    public KeyInfo getKeyInfo()
    {
        if ( (
            compilerCapability.getVendorInfo().getVendor().equals( Vendor.MICROSOFT )
                && compilerCapability.getVendorInfo().getFrameworkVersion().equals( "1.1.4322" )
        ) || config.getKeyInfo() == null )
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

        addProjectArtifactForTestCompile( libraries );
        return libraries;
    }

    private void addProjectArtifactForTestCompile( List<Artifact> libraries )
    {
        if ( config.isTestCompile() && (
            ArtifactTypeHelper.isDotnetLibrary( config.getArtifactType() ) || ArtifactTypeHelper.isDotnetMavenPlugin(
                config.getArtifactType()
            )
        ) && project.getArtifact().getFile() != null && project.getArtifact().getFile().exists() && !libraries.contains(
            project.getArtifact()
        ) && !ArtifactTypeHelper.isDotnetModule(
            project.getArtifact().getType()
        ) )
        {
            libraries.add( project.getArtifact() );
        }
    }

    public List<Artifact> getDirectLibraryDependencies()
    {
        for ( Iterator i = project.getDependencyArtifacts().iterator(); i.hasNext(); )
        {
            Artifact artifact = (Artifact) i.next();

            if ( !new ScopeArtifactFilter( isTestCompile() ? "test" : "compile" ).include( artifact ) )
            {
                continue;
            }

            // TODO: use isAddedToClassPath instead? May need to annotate types
            if ( !ArtifactTypeHelper.isDotnetLibrary( artifact.getType() ) && !ArtifactTypeHelper.isDotnetExecutable(
                artifact.getType()
            ) && !ArtifactTypeHelper.isDotnetAnyGac( artifact.getType() ) )
            {
                continue;
            }

            if ( !hasArtifact( artifact ) )
            {
                directLibraries.add( artifact );
            }

            boolean found = false;
            for ( Iterator j = project.getDependencies().iterator(); j.hasNext() && !found; )
            {
                Dependency dependency = (Dependency) j.next();
                if ( dependency.getGroupId().equals( artifact.getGroupId() ) && dependency.getArtifactId().equals(
                    artifact.getArtifactId()
                ) && dependency.getVersion().equals(
                    artifact.getBaseVersion()
                ) )
                {
                    found = true;
                }
            }

            if ( !found )
            {
                directLibraries.remove( artifact );
            }


        }

        addProjectArtifactForTestCompile( directLibraries );

        return directLibraries;

    }

    private boolean hasArtifact( Artifact artifact )
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

    public File getGeneratedSourcesDirectory()
    {
        return ( config.isTestCompile() )
            ? new File( project.getBuild().getDirectory(), "build-test-sources" )
            : new File( project.getBuild().getDirectory(), "build-sources" );
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
    public File getArtifact() throws InvalidArtifactException
    {
        ArtifactType artifactType = config.getArtifactType();
        if ( artifactType == null || artifactType.equals( ArtifactType.NULL ) )
        {
            throw new InvalidArtifactException( "NPANDAY-061-001: Artifact Type cannot be null" );
        }

        //TODO: The test-plugin has a dependency on this fileName/dir. If we change it here,
        // it will break the plugin. Fix this encapsulation issue.
        String fileName = ( config.isTestCompile() )
            ? project.getBuild().getDirectory() + File.separator + project.getArtifactId() + "-test.dll"
            : project.getBuild().getDirectory() + File.separator + project.getArtifactId() + "."
                + artifactType.getExtension();
        return new File( fileName );
    }

    public CompilerExecutable getCompilerExecutable() throws ExecutionException
    {
        return (CompilerExecutable) getNetExecutable();
    }

    public Repository find( String repositoryName ) throws RepositoryNotFoundException
    {
        Repository repository = repositoryRegistry.find( repositoryName );
        if ( repository == null )
        {
            throw new RepositoryNotFoundException(
                "NPANDAY-061-002: Could not find repository: Name = " + repositoryName
            );
        }
        return repository;
    }

    public void init( CompilerCapability capability, CompilerConfig config, MavenProject project ) throws
        PlatformUnsupportedException
    {

        this.project = project;
        this.config = config;
        libraries = new ArrayList<Artifact>();
        directLibraries = new ArrayList<Artifact>();
        modules = new ArrayList<Artifact>();
        compilerCapability = capability;

        // initialize base class
        super.init( compilerCapability, config );

        Set<Artifact> artifacts = project.getDependencyArtifacts();//Can add WFC deps prior
        if ( artifacts != null )
        {
            for ( Artifact artifact : artifacts )
            {
                String type = artifact.getType();
                logger.debug( "NPANDAY-061-006: Artifact Type:" + type );
                logger.debug( "NPANDAY-061-007: Artifact Type:" + ArtifactTypeHelper.isDotnetGenericGac( type ) );
                ArtifactType artifactType = ArtifactType.getArtifactTypeForPackagingName( type );
                if ( ArtifactTypeHelper.isDotnetModule( type ) )
                {
                    modules.add( artifact );
                }
                else if ( (
                    artifactType != ArtifactType.NULL && (
                        StringUtils.equals( artifactType.getTargetCompileType(), "library" )
                            || artifactType.getExtension().equals( "dll" ) || artifactType.getExtension().equals(
                            "exe"
                        )
                    )
                ) || type.equals( "jar" ) )
                {
                    libraries.add( artifact );
                }

                if ( type.equals( ArtifactType.COM_REFERENCE.getPackagingType() ) )
                {
                    moveInteropDllToBuildDirectory( artifact );
                    libraries.add( artifact );
                }
                else if ( (
                    artifactType != null && (
                        "library".equals( artifactType.getTargetCompileType() ) || "dll".equals(
                            artifactType.getExtension()
                        ) || "exe".equals( artifactType.getExtension() )
                    )
                ) || "jar".equals( type ) )
                {
                    libraries.add( artifact );
                }

            }
        }

        String basedir = project.getBuild().getDirectory() + File.separator + "assembly-resources" + File.separator;
        linkedResources = new File( basedir, "linkresource" ).exists() ? Arrays.asList(
            new File( basedir, "linkresource" ).listFiles()
        ) : new ArrayList<File>();
        getEmbeddedResources( new File( basedir, "resource" ) );
        win32resources = new File( basedir, "win32res" ).exists() ? Arrays.asList(
            new File( basedir, "win32res" ).listFiles()
        ) : new ArrayList<File>();
        File win32IconDir = new File( basedir, "win32icon" );
        if ( win32IconDir.exists() )
        {
            File[] icons = win32IconDir.listFiles();
            if ( icons.length > 1 )
            {
                throw new PlatformUnsupportedException(
                    "NPANDAY-061-008: There is more than one win32icon in resource directory: Number = " + icons.length
                );
            }
            if ( icons.length == 1 )
            {
                win32icon = icons[0];
            }
        }
    }

    Set<File> expandedSourceFiles;

    public Set<File> getSourceFiles()
    {
        // TODO: cache source files - but it seems that this instance is reused... ??
        /*if (expandedSourceFiles == null)
            expandedSourceFiles = expandSourceFiles();*/

        return expandSourceFiles();
    }

    private Set<File> expandSourceFiles()
    {
        Set<File> files = Sets.newHashSet();
        if ( config.getDeprecatedIncludeSourcesConfiguration() != null )
        {
            for ( String file : config.getDeprecatedIncludeSourcesConfiguration() )
            {
                files.add( new File( file ) );
            }
        }

        files.addAll( expandSources( getGeneratedSourcesDirectory() ) );

        String defaultSourceRoot = isTestCompile()
            ? project.getBuild().getTestSourceDirectory()
            : project.getBuild().getSourceDirectory();

        Set<String> additionalRoots = Sets.newHashSet();
        List bareRoots = isTestCompile() ? project.getTestCompileSourceRoots() : project.getCompileSourceRoots();

        if ( bareRoots != null )
        {
            for ( Object root : project.getCompileSourceRoots() )
            {
                if ( !root.equals( defaultSourceRoot ) )
                {
                    additionalRoots.add( (String) root );
                }
            }
        }

        if ( additionalRoots.size() > 0 )
        {
            getLogger().debug(
                "NPANDAY-061-009: Adding additional compile source roots: " + additionalRoots
            );

            for ( String root : additionalRoots )
            {
                files.addAll( expandSources( new File( root ) ) );
            }
        }

        if ( isTestCompile() )
        {
            files.addAll( expandTestSourceFilePatterns() );
        }
        else if ( !isSourceAndTestsTogether() )
        {
            files.addAll( expandMainSourceFilePatterns() );
        }
        else
        {
            List<File> mainSources = expandMainSourceFilePatterns();
            List<File> testSources = expandTestSourceFilePatterns();

            getLogger().debug(
                "NPANDAY-061-014: Since tests (" + testSources.size()
                    + " files) reside in same folder as main sources (" + mainSources.size() + " files),"
                    + " they will be excluded from main sources"
            );

            List<File> sources = Lists.newArrayList();
            sources.addAll( mainSources );
            sources.removeAll( testSources );
            files.addAll( sources );
        }

        logger.info(
            "NPANDAY-061-011: Found " + files.size() + " source files for " + ( isTestCompile() ? "test" : "main" )
                + " compile"
        );

        return files;
    }

    private List<File> expandMainSourceFilePatterns()
    {
        getLogger().debug(
            "NPANDAY-061-012: Expanding main sources"
        );
        List<String> includes = Lists.newArrayList();
        if ( config.getIncludes() != null )
        {
            includes.addAll( Lists.newArrayList( config.getIncludes() ) );
        }
        List<String> excludes = Lists.newArrayList();
        if ( config.getExcludes() != null )
        {
            excludes.addAll( Lists.newArrayList( config.getExcludes() ) );
        }

        if ( includes.size() == 0 )
        {
            includes.add( "**/*." + config.getLanguageFileExtension() );
        }

        //target files
        excludes.add( "**/obj/**" );
        excludes.add( "**/bin/**" );
        excludes.add( "**/target/**" );

        File root = new File( project.getBuild().getSourceDirectory() );

        return expandSources( root, includes, excludes );
    }

    private List<File> expandTestSourceFilePatterns()
    {
        getLogger().debug(
            "NPANDAY-061-013: Expanding test sources"
        );
        List<String> includes = Lists.newArrayList();
        if ( config.getTestIncludes() != null )
        {
            includes.addAll( Lists.newArrayList( config.getTestIncludes() ) );
        }
        List<String> excludes = Lists.newArrayList();
        if ( config.getTestExcludes() != null )
        {
            excludes.addAll( Lists.newArrayList( config.getTestExcludes() ) );
        }

        if ( includes.size() == 0 )
        {
            if ( !isSourceAndTestsTogether() )
            {
                includes.add( "**/*." + config.getLanguageFileExtension() );
            }
            else
            {
                getLogger().debug(
                    "NPANDAY-061-014: Since source and tests reside in same folder, "
                        + "and no default includes are stated, conventions for finding tests will be applied."
                );
                includes.add( "**/Test/**/*.*" + config.getLanguageFileExtension() );
                includes.add( "**/Tests/**/*.*" + config.getLanguageFileExtension() );
                includes.add( "**/*Tests." + config.getLanguageFileExtension() );
                includes.add( "**/*Test." + config.getLanguageFileExtension() );
            }
        }

        //target files
        excludes.add( "**/obj/**" );
        excludes.add( "**/bin/**" );
        excludes.add( "**/target/**" );

        File root = new File( project.getBuild().getTestSourceDirectory() );

        return expandSources( root, includes, excludes );
    }

    private List<File> expandSources( File directory )
    {
        return expandSources( directory, Lists.newArrayList( "**/*." + config.getLanguageFileExtension() ), null );
    }

    private List<File> expandSources( File directory, Iterable<String> includes, Iterable<String> excludes )
    {
        if ( !directory.exists() || directory.list().length == 0 )
        {
            getLogger().debug( "NPANDAY-061-015: " + directory + " is empty; no sources found" );
            return Lists.newArrayList();
        }

        DirectoryScanner scanner = createScanner( directory, includes, excludes );
        scanner.scan();

        List<File> files = Lists.newArrayList();
        for ( String fs : scanner.getIncludedFiles() )
        {

            if ( !fs.endsWith( "." + config.getLanguageFileExtension() ) )
            {
                continue;
            }

            files.add( new File( directory, fs ) );
        }

        if ( getLogger().isDebugEnabled() )
        {
            getLogger().debug(
                "NPANDAY-061-016: scanned for source files:" + NEW_LINE + " - directory: " + directory.getAbsolutePath()
                    + NEW_LINE + " - includes: " + includes + NEW_LINE + " - excludes: " + excludes + NEW_LINE
                    + " - included (*.*): " + scanner.getIncludedFiles().length + NEW_LINE + " - included sources (*."
                    + config.getLanguageFileExtension() + "): " + files.size() + NEW_LINE + " - excluded: "
                    + scanner.getExcludedFiles().length + NEW_LINE + " - ignored: "
                    + scanner.getNotIncludedFiles().length
            );
        }

        return files;
    }

    private DirectoryScanner createScanner( File root, Iterable<String> includes, Iterable<String> excludes )
    {
        DirectoryScanner scanner = new DirectoryScanner();
        scanner.setBasedir( root );

        if ( includes != null )
        {
            scanner.setIncludes( Iterables.toArray( includes, String.class ) );
        }
        if ( excludes != null )
        {
            scanner.setExcludes( Iterables.toArray( excludes, String.class ) );
        }

        // TODO: NPANDAY-210 Maven is usually case sensitive, right?
        scanner.setCaseSensitive( false );
        scanner.addDefaultExcludes();
        return scanner;
    }

    public File getOutputDirectory()
    {
        return config.getOutputDirectory();
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
                File f = new File( basedir, file );
                embeddedResources.add( f );
                if ( f.getName().endsWith( ".resources" ) )
                {
                    embeddedResourceArgs.add( f.getAbsolutePath() );
                }
                else
                {
                    String resourceName = project.getArtifactId() + "." + file.replace( File.separatorChar, '.' );
                    embeddedResourceArgs.add( f.getAbsolutePath() + "," + resourceName );
                }
            }
        }
        this.embeddedResources = embeddedResources;
        this.embeddedResourceArgs = embeddedResourceArgs;
    }

    private void moveInteropDllToBuildDirectory( Artifact artifact ) throws PlatformUnsupportedException
    {
        try
        {
            File file = artifact.getFile();
            String oldPath = file.getAbsolutePath();
            String target = project.getBuild().getDirectory();
            String newPath = target + File.separator + "Interop." + artifact.getArtifactId() + ".dll";

            if ( oldPath.contains( target ) ) //already copied to target
            {
                return;
            }

            logger.info( "NPANDAY-000-000:[COM Reference] copying file [" + oldPath + "] to [" + target + "]" );
            FileUtils.copyFileToDirectory( file, new File( target ) );

            logger.info( "NPANDAY-000-000:[COM Reference] deleting directory [" + file.getParentFile() + "]" );
            FileUtils.deleteDirectory( file.getParentFile() );

            logger.info( "NPANDAY-000-000:[COM Reference] updating artifact path to [" + newPath + "]" );

            artifact.setFile( new File( newPath ) );
        }
        catch ( Exception e )
        {
            throw new PlatformUnsupportedException( e );
        }
    }

    public boolean isSourceAndTestsTogether()
    {
        return project.getBuild().getSourceDirectory().equals( project.getBuild().getTestSourceDirectory() );
    }
}
