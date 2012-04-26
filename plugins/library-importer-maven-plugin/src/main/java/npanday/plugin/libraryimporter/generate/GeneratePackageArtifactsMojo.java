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

package npanday.plugin.libraryimporter.generate;

import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import npanday.plugin.libraryimporter.NuspecMetadata;
import npanday.plugin.libraryimporter.model.NugetPackage;
import npanday.plugin.libraryimporter.model.NugetPackageLibrary;
import npanday.plugin.libraryimporter.skeletons.AbstractLibraryImportsProvidingMojo;
import org.apache.maven.model.Build;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Developer;
import org.apache.maven.model.License;
import org.apache.maven.model.Model;
import org.apache.maven.model.Plugin;
import org.apache.maven.model.io.xpp3.MavenXpp3Writer;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.IOUtil;
import org.codehaus.plexus.util.WriterFactory;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.util.List;

/**
 * Generates the poms and copies the libs to corresponding temp folders.
 *
 * @author <a href="mailto:lcorneliussen@apache.org">Lars Corneliussen</a>
 * @goal generate-package-artifacts
 */
public class GeneratePackageArtifactsMojo
    extends AbstractLibraryImportsProvidingMojo
{
    /**
     * @parameter default-value="${project.build.directory}/generated-projects"
     */
    protected File generatedProjectsDirectory;

    @Override
    protected void innerExecute() throws MojoExecutionException, MojoFailureException
    {
        super.innerExecute();

        List<NugetPackageLibrary> imports = Lists.newArrayList();

        List<NugetPackage> nugetImports = Lists.newArrayList( getNugetImports() );

        for ( NugetPackage nuget : nugetImports )
        {
            nuget.resolveDependencies( nugetImports );
        }

        for ( NugetPackage nuget : nugetImports )
        {
            for ( NugetPackageLibrary lib : nuget.getLibraries(mavenProjectsCacheDirectory) )
            {
                imports.add( lib );
            }
        }

        for( NugetPackageLibrary lib  : imports){

            lib.resolveDependenciesFrom(imports);

            Model model = generateModel(lib.getNugetPackage().getNuspec(), lib);

            storePomFile( model, lib.getMavenPomFile() );

            File target = lib.getMavenProjectFolder();

            try
            {
                FileUtils.copyFileToDirectory( lib.getFile(), target );
            }
            catch ( IOException e )
            {
                throw new MojoExecutionException(
                    "NPANDAY-141-002: Error on copy file " + lib.getFile() + " to " + target, e
                );
            }
        }

    }

    Splitter AUTHORS_SPLITTER = Splitter.on( "," ).omitEmptyStrings().trimResults();

    private Model generateModel( NuspecMetadata nugetPackage, NugetPackageLibrary lib ) throws
        MojoExecutionException
    {
        Model model = new Model();

        model.setModelVersion( "4.0.0" );

        model.setGroupId( lib.getMavenGroupId() );
        model.setArtifactId( lib.getMavenArtifactId() );
        model.setVersion( lib.getMavenVersion() );

        model.setBuild( new Build() );
        model.getBuild().setFinalName( model.getArtifactId() );

        if ( !Strings.isNullOrEmpty( nugetPackage.getTitle() ) )
        {
            model.setName( nugetPackage.getTitle());
        }
        else
        {
            model.setName( nugetPackage.getId() );
        }

        model.setName( model.getName() + " :: " + model.getArtifactId() );

        if ( !Strings.isNullOrEmpty( nugetPackage.getProjectUrl() ) )
        {
            model.setUrl( nugetPackage.getProjectUrl() );
        }


        if ( !Strings.isNullOrEmpty( nugetPackage.getSummary() ) )
        {
            model.setDescription( nugetPackage.getSummary() );
        }
        else if ( !Strings.isNullOrEmpty( nugetPackage.getDescription() ) )
        {
            model.setDescription( nugetPackage.getDescription() );
        }

        if ( !Strings.isNullOrEmpty( nugetPackage.getLicenseUrl() ) )
        {
            License lic = new License();
            lic.setUrl( nugetPackage.getLicenseUrl() );
            model.getLicenses().add( lic );
        }

        if ( !Strings.isNullOrEmpty( nugetPackage.getAuthors() ) )
        {
            for ( String author : AUTHORS_SPLITTER.split( nugetPackage.getAuthors() ) )
            {
                Developer dev = new Developer();
                dev.setName( author );
                model.addDeveloper( dev );
            }
        }

        model.setPackaging( lib.getMavenPackaging() );

        for( NugetPackageLibrary libDep : lib.getDependencies()){

            Dependency dep = new Dependency();
            dep.setGroupId( libDep.getMavenGroupId() );
            dep.setArtifactId( libDep.getMavenArtifactId() );
            dep.setType( libDep.getMavenPackaging() );

            // TODO: should this actually be a range?
            dep.setVersion( libDep.getMavenVersion() );

            model.getDependencies().add( dep );
        }

        return model;
    }

    private void storePomFile( Model model, File pomFile ) throws MojoExecutionException
    {
        pomFile.getParentFile().mkdirs();

        Writer writer = null;
        try
        {
            writer = WriterFactory.newXmlWriter( pomFile );
            new MavenXpp3Writer().write( writer, model );
        }
        catch ( IOException e )
        {
            throw new MojoExecutionException( "NPANDAY-141-003: Error writing POM file: " + e.getMessage(), e );
        }
        finally
        {
            IOUtil.close( writer );
        }
    }
}
