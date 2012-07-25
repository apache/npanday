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

package npanday.plugin.libraryimporter.model;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import npanday.model.library.imports.ImportVersion;
import npanday.model.library.imports.LibraryDirectories;
import npanday.model.library.imports.NugetImport;
import npanday.model.library.imports.NugetSources;
import npanday.model.library.imports.ReferenceMapping;
import npanday.nuget.NugetSemanticVersion;
import npanday.nuget.NugetVersionSpec;
import npanday.plugin.libraryimporter.AssemblyInfo;
import npanday.plugin.libraryimporter.LibImporterPathUtils;
import npanday.plugin.libraryimporter.NuspecDependency;
import npanday.plugin.libraryimporter.NuspecMetadata;
import npanday.plugin.libraryimporter.NuspecParser;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;

import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="me@lcorneliussen.de">Lars Corneliussen, Faktum Software</a>
 */
public class NugetPackage
{
    private NuspecMetadata metadata;

    private List<NugetPackage> dependencies;

    private Collection<NugetPackage> knownPackages;

    public static List<NugetPackage> Convert(
        Iterable<NugetImport> imports, NugetSources nugetSources, File packageRootDirectory )
    {
        List<NugetPackage> list = Lists.newArrayList();
        for ( NugetImport imp : imports )
        {
            for ( ImportVersion ver : imp.getVersions() )
            {
                list.add( new NugetPackage( imp, ver, nugetSources, packageRootDirectory ) );
            }
        }
        return list;
    }

    private final NugetImport importModel;

    private NugetSemanticVersion parsedVersion;

    private final ImportVersion version;

    private NugetSources nugetSources;

    private File packageRootDirectory;

    public NugetPackage(
        NugetImport importModel, ImportVersion version, NugetSources nugetSources, File packageRootDirectory )
    {
        this.importModel = importModel;
        this.version = version;
        this.nugetSources = nugetSources;
        this.packageRootDirectory = packageRootDirectory;
    }

    public String getName()
    {
        return importModel.getPackageName();
    }

    public NugetSemanticVersion getVersion()
    {
        if ( parsedVersion == null )
        {
            parsedVersion = NugetSemanticVersion.parse( version.getSource() );
        }
        return parsedVersion;
    }

    public String getDefaultLibDirectory()
    {
        LibraryDirectories libDirs = importModel.getLibraryDirectories();
        return libDirs != null ? libDirs.getDefaultDirectory() : null;
    }

    public File getDirectory()
    {
        return new File( packageRootDirectory, getName() + "." + getVersion() );
    }

    public Iterable<NugetPackageLibrary> getLibraries( Log log, File mavenProjectsCacheDirectory ) throws
        MojoExecutionException
    {
        List<File> libDirectories = getLibraryDirectories();

        List<NugetPackageLibrary> libImports = Lists.newArrayList();
        for ( File libDir : libDirectories )
        {
            for ( File libFile : LibImporterPathUtils.getLibraries( libDir ) )
            {
                NugetPackageLibrary lib = new NugetPackageLibrary( this, libFile, mavenProjectsCacheDirectory );

                ReferenceMapping referenceMapping = tryFindReferenceMappingFor( lib.getAssemblyInfo() );
                if ( referenceMapping != null && referenceMapping.getMapToPackage() != null )
                {
                    if ( log.isDebugEnabled() )
                    {
                        log.debug(
                            "NPANDAY-142-004: Will skip inclusion for " + lib
                                + " since references to it are mapped to a different package ("
                                + referenceMapping.getMapToPackage().getId() + ", v"
                                + referenceMapping.getMapToPackage().getVersion() + ")."
                        );
                    }

                    continue;
                }

                libImports.add( lib );
            }
        }
        return libImports;
    }

    public List<File> getLibraryDirectories() throws MojoExecutionException
    {
        File packageDir = getDirectory();

        String defaultLibDir = getDefaultLibDirectory();
        Map<String, File> libDirectories;
        if ( defaultLibDir != null )
        {
            File libs = LibImporterPathUtils.getLibDirectory( packageDir, defaultLibDir );
            if ( !libs.exists() )
            {
                throw new MojoExecutionException(
                    "NPANDAY-142-001: Configured libs folder does not exist: " + libs
                );
            }
            libDirectories = Maps.newHashMap();
            libDirectories.put( "lib", libs );
        }
        else
        {
            libDirectories = LibImporterPathUtils.getLibDirectories( packageDir );
            if ( libDirectories.size() > 1 )
            {
                throw new MojoExecutionException(
                    "NPANDAY-142-003: " + getName()
                        + " has multiple lib folders. Please set 'libDirs/default' to the name of the one to "
                        + "choose of: " + libDirectories.keySet()
                );
            }
        }
        return Lists.newArrayList( libDirectories.values() );
    }

    public File getNuspecFile()
    {
        return new File(
            getDirectory(), getName() + "." + getVersion() + ".nuspec"
        );
    }

    public File getPackageFile()
    {
        return new File( getDirectory(), getName() + "." + getVersion() + ".nupkg" );
    }

    public NuspecMetadata getNuspec()
    {
        if ( metadata == null )
        {
            metadata = NuspecParser.parse( getNuspecFile() );
        }
        return metadata;
    }

    public String getMavenVersion()
    {
        String maven = version.getMaven();
        return !Strings.isNullOrEmpty( maven ) ? maven : version.getSource();
    }

    public boolean isDependentOn( NugetPackage other )
    {
        return Iterables.contains( getDependencies(), other );
    }

    public Collection<NugetPackage> resolveDependencies( Collection<NugetPackage> packages ) throws
        MojoExecutionException
    {
        Preconditions.checkArgument( dependencies == null, "Dependencies have already been resolved" );

        knownPackages = packages;
        dependencies = Lists.newArrayList();
        for ( Object depO : getNuspec().getDependencies() )
        {
            // NOTE: stub-generator should IMHO generate List<NuspecDependency>
            NuspecDependency dep = (NuspecDependency) depO;
            NugetPackage highest = resolveDependencyAmongAllKnown( dep );

            dependencies.add( highest );
        }

        return dependencies;
    }

    public NugetPackage resolveDependencyAmongAllKnown( NuspecDependency dep ) throws MojoExecutionException
    {
        NugetVersionSpec depVersion = dep.getVersion();
        String depId = dep.getId();

        NugetPackage highest = null;
        for ( NugetPackage pkg : getKnownPackages() )
        {
            if ( !pkg.getName().equalsIgnoreCase( depId ) )
            {
                continue;
            }

            if ( depVersion != null && !depVersion.isSatisfiedBy( pkg.getVersion() ) )
            {
                continue;
            }

            if ( highest == null || highest.getVersion().compareTo( pkg.getVersion() ) < 0 )
            {
                highest = pkg;
            }

        }

        if ( highest == null )
        {
            throw new MojoExecutionException(
                "NPANDAY-142-004: Could not resolve dependency " + dep + " among the nuget imports."
            );
        }
        return highest;
    }

    public Collection<NugetPackage> getDependencies()
    {
        Preconditions.checkNotNull( dependencies, "Dependencies have not been resolved yet!" );

        return dependencies;
    }

    @Override
    public String toString()
    {
        return "[" + getName() + ", v" + getVersion() + "]";
    }

    public ReferenceMapping tryFindReferenceMappingFor( AssemblyInfo assembly )
    {
        if ( importModel.getReferenceMappings() == null )
        {
            return null;
        }

        for ( ReferenceMapping mapping : importModel.getReferenceMappings() )
        {
            if ( mapping.getName().equalsIgnoreCase( assembly.getName() ) )
            {
                return mapping;
            }
        }

        return null;
    }

    public List<String> getSources()
    {
        if ( nugetSources != null && nugetSources.getCustomSources() != null
            && nugetSources.getCustomSources().size() > 0 )
        {
            List<String> sources = Lists.newArrayList();

            sources.addAll( nugetSources.getCustomSources() );

            if ( nugetSources.isAddNugetGallery() )
            {
                sources.add( "https://go.microsoft.com/fwlink/?LinkID=206669" );
            }

            return sources;
        }

        return null;
    }

    public Collection<NugetPackage> getKnownPackages()
    {
        Preconditions.checkNotNull(
            dependencies, "Dependencies have not been resolved yet, there fore known packages are not available either"
        );

        return knownPackages;
    }
}
