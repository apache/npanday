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

package npanday.plugin.resolver;

import com.google.common.base.Strings;
import npanday.ArtifactType;
import npanday.LocalRepositoryUtil;
import npanday.PathUtil;
import npanday.registry.RepositoryRegistry;
import npanday.resolver.NPandayDependencyResolution;
import npanday.resolver.filter.DotnetExecutableArtifactFilter;
import npanday.resolver.filter.DotnetLibraryArtifactFilter;
import npanday.resolver.filter.OrArtifactFilter;
import npanday.vendor.SettingsUtil;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.metadata.ArtifactMetadata;
import org.apache.maven.artifact.resolver.ArtifactResolutionException;
import org.apache.maven.artifact.resolver.filter.AndArtifactFilter;
import org.apache.maven.artifact.resolver.filter.ArtifactFilter;
import org.apache.maven.artifact.resolver.filter.InversionArtifactFilter;
import org.apache.maven.artifact.resolver.filter.ScopeArtifactFilter;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Set;

/**
 * Resolves and copies .NET assemblies.
 *
 * @author <a href="me@lcorneliussen.de">Lars Corneliussen, Faktum Software</a>
 * @goal copy-dependencies
 */
public class CopyDependenciesMojo
    extends AbstractMojo
{
    /**
     * @parameter expression="${npanday.settings}" default-value="${user.home}/.m2"
     */
    private String settingsPath;

    /**
     * @component
     */
    private RepositoryRegistry repositoryRegistry;

    /**
     * The maven project.
     *
     * @parameter expression="${project}"
     * @required
     */
    private MavenProject project;

    /**
     * The local Maven repository.
     *
     * @parameter expression="${settings.localRepository}"
     */
    private File localRepository;

    /**
     * @parameter default-value="compile"
     */
    private String includeScope;

    /**
     * @parameter
     */
    private String excludeScope;

    /**
     * @component
     */
    private NPandayDependencyResolution dependencyResolution;

    /**
     * @parameter default-value="${project.build.directory}\.references"
     */
    private File outputDirectory;

    /**
     * @parameter default-value="false"
     */
    private boolean skip;

    /**
     * The reactor projects.
     *
     * @parameter expression="${reactorProjects}"
     */
    protected List<MavenProject> reactorProjects;

    /**
     * If specified, Artifacts that are part of the same reactor will not be copied.
     * Transitive dependencies of these artifacts will still get copied, though.
     *
     * @parameter default-value="false"
     */
    private boolean skipReactorArtifacts;

    public void execute() throws MojoExecutionException, MojoFailureException
    {
        String skipReason = "";
        if ( !skip )
        {
            ArtifactType knownType = ArtifactType.getArtifactTypeForPackagingName(
                project.getPackaging()
            );

            if ( knownType.equals( ArtifactType.NULL ))
            {
                skip = true;
                skipReason =
                    ", because the current project (type:" + project.getPackaging() + ") is not built with NPanday";
            }
        }

        if ( skip )
        {
            getLog().info( "NPANDAY-158-001: Mojo for copying dependencies was intentionally skipped" + skipReason );
            return;
        }

        SettingsUtil.applyCustomSettingsIfAvailable( getLog(), repositoryRegistry, settingsPath );

        AndArtifactFilter includeFilter = new AndArtifactFilter();

        OrArtifactFilter typeIncludes = new OrArtifactFilter();
        typeIncludes.add( new DotnetExecutableArtifactFilter() );
        typeIncludes.add( new DotnetLibraryArtifactFilter() );
        includeFilter.add( typeIncludes );

        if ( !Strings.isNullOrEmpty( includeScope ) )
        {
            includeFilter.add( new ScopeArtifactFilter( includeScope ) );
        }

        Set<Artifact> artifacts;
        try
        {
            artifacts = dependencyResolution.require(
                project, LocalRepositoryUtil.create( localRepository ), includeFilter
            );
        }
        catch ( ArtifactResolutionException e )
        {
            throw new MojoExecutionException(
                "NPANDAY-158-003: dependency resolution for scope " + includeScope + " failed!", e
            );
        }

        /**
         * Should be resolved, but then not copied
         */
        if ( !Strings.isNullOrEmpty( excludeScope ) )
        {
            includeFilter.add( new InversionArtifactFilter( new ScopeArtifactFilter( excludeScope ) ) );
        }

        if ( skipReactorArtifacts ){
            getLog().info( "NPANDAY-158-008: " + reactorProjects );

            includeFilter.add( new InversionArtifactFilter( new ArtifactFilter()
            {
                public boolean include( Artifact artifact )
                {
                    for (MavenProject project : reactorProjects){
                        if (project.getArtifact().getId().equals( artifact.getId() ))
                            return true;
                    }
                    return false;
                }
            } ));
        }

        for ( Artifact dependency : artifacts )
        {
            if ( !includeFilter.include( dependency ) )
            {
                getLog().debug( "NPANDAY-158-006: dependency " + dependency + " was excluded" );

                continue;
            }

            try
            {
                File targetFile = new File( outputDirectory, PathUtil.getPlainArtifactFileName( dependency ) );
                if ( !targetFile.exists()
                    || targetFile.lastModified() != dependency.getFile().lastModified()
                    || targetFile.length() != dependency.getFile().length() )
                {
                    getLog().info( "NPANDAY-158-004: copy " + dependency.getFile() + " to " + targetFile );
                    FileUtils.copyFile( dependency.getFile(), targetFile );
                }
                else{
                    getLog().debug( "NPANDAY-158-007: dependency " + dependency + " is yet up to date" );
                }
            }
            catch ( IOException ioe )
            {
                throw new MojoExecutionException( "NPANDAY-158-005: Error copying dependency " + dependency, ioe );
            }
        }
    }

}
