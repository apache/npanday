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

package npanday.plugin.libraryimporter.skeletons;

import com.google.common.collect.Lists;
import npanday.model.library.imports.LibraryImports;
import npanday.model.library.imports.NugetImport;
import npanday.model.library.imports.NugetSources;
import npanday.model.library.imports.io.xpp3.LibraryImportsXpp3Reader;
import npanday.plugin.libraryimporter.model.NugetPackage;
import org.apache.maven.plugin.MojoExecutionException;
import org.codehaus.plexus.util.DirectoryScanner;
import org.codehaus.plexus.util.IOUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

/**
 * @author <a href="me@lcorneliussen.de">Lars Corneliussen, Faktum Software</a>
 */
public class AbstractLibraryImportsProvidingMojo
    extends AbstractNPandaySettingsAwareMojo
{
    /**
     * Which library-import files to consider.
     *
     * Defaults to *
     *//*.lib.xml.
     *
     * @parameter
     */
    private String[] includes;

    /**
     * Semicolon-separated command line version of {@link #includes}
     *
     * @parameter expression="${library.includes}"
     */
    private String includesList;

    /**
     * @parameter
     */
    private String[] excludes;

    /**
     * Semicolon-separated command line version of {@link #excludes}
     *
     * @parameter expression="${library.excludes}"
     */
    private String excludesList;

    /**
     * @parameter default-value="${project.build.directory}/packages"
     */
    protected File packageCacheDirectory;

    /**
     * @parameter default-value="${project.build.directory}/generated-projects"
     */
    protected File mavenProjectsCacheDirectory;

    @Override
    protected void setupParameters()
    {
        includes = firstNonNull( includesList, includes, new String[]{ "**/*.lib.xml" } );
        excludes = firstNonNull( excludesList, excludes );
    }

    protected Iterable<NugetPackage> getNugetImports() throws MojoExecutionException
    {
        List<LibraryImports> libraryImports = getLibraryImports();

        List<NugetPackage> nugetImports = Lists.newArrayList();
        for ( LibraryImports libraryImport : libraryImports )
        {
            List<NugetImport> imports = libraryImport.getNugetImports();
            NugetSources nugetSources = libraryImport.getNugetSources();

            if ( nugetSources != null )
            {
                if ( getLog().isDebugEnabled() )
                {
                    String nugetOrg = nugetSources.isAddNugetGallery() ? " + nuget.org" : "";
                    getLog().debug(
                        "NPANDAY-137-004: found custom nuget sources" + nugetSources.getCustomSources() + nugetOrg
                    );
                }
            }

            nugetImports.addAll(
                NugetPackage.Convert( imports, nugetSources, packageCacheDirectory )
            );
        }
        return nugetImports;
    }

    protected List<LibraryImports> getLibraryImports() throws MojoExecutionException
    {
        DirectoryScanner directoryScanner = new DirectoryScanner();
        directoryScanner.setBasedir( project.getBasedir() );

        directoryScanner.setIncludes( includes );
        if ( excludes != null && excludes.length > 0 )
        {
            directoryScanner.setExcludes( excludes );
        }

        directoryScanner.scan();

        List<LibraryImports> libraryImports = Lists.newArrayList();

        if ( getLog().isDebugEnabled() )
        {
            getLog().debug(
                "NPANDAY-137-003: parsing imports from " + Lists.newArrayList(
                    directoryScanner.getIncludedFiles()
                )
            );
        }

        for ( String path : directoryScanner.getIncludedFiles() )
        {
            LibraryImports model = load( path );
            interpolate( path, model );
            libraryImports.add( model );
        }

        if ( getLog().isDebugEnabled() )
        {
            getLog().debug( "NPANDAY-137-004: parsed " + libraryImports.size() + " library import file(s)" );
        }

        return libraryImports;
    }

    private void interpolate( String path, LibraryImports model )
    {
        if ( model.getNugetSources() != null )
        {
            List<String> sources = model.getNugetSources().getCustomSources();
            if (sources != null){
                List<String> interpolatedSources = Lists.newArrayList();
                for ( String source : sources ){

                    if (!source.contains( ":" )){
                        source = new File( new File( path ).getParentFile(), source ).toURI().toString();
                    }

                    interpolatedSources.add( source );
                }
                model.getNugetSources().setCustomSources( interpolatedSources);
            }
        }
    }

    private LibraryImports load( String path ) throws MojoExecutionException
    {
        LibraryImportsXpp3Reader xpp3Reader = new LibraryImportsXpp3Reader();

        FileInputStream inputStream = null;
        try
        {
            inputStream = new FileInputStream( path );
            return xpp3Reader.read( inputStream );
        }
        catch ( IOException e )
        {
            throw new MojoExecutionException(
                "NPANDAY-137-000: An error occurred while reading " + path + " as library imports", e
            );
        }
        catch ( org.codehaus.plexus.util.xml.pull.XmlPullParserException e )
        {
            throw new MojoExecutionException(
                "NPANDAY-137-001: An xml error occurred while reading " + path + " as library imports", e
            );
        }
        finally
        {
            IOUtil.close( inputStream );
        }
    }
}
