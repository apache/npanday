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

import com.google.common.collect.Sets;
import npanday.LocalRepositoryUtil;
import npanday.registry.RepositoryRegistry;
import npanday.resolver.NPandayArtifactResolver;
import npanday.resolver.NPandayDependencyResolution;
import npanday.resolver.filter.DotnetAssemblyArtifactFilter;
import npanday.resolver.filter.DotnetSymbolsArtifactFilter;
import npanday.resolver.filter.OrArtifactFilter;
import npanday.vendor.SettingsUtil;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.metadata.ArtifactMetadataSource;
import org.apache.maven.artifact.resolver.ArtifactNotFoundException;
import org.apache.maven.artifact.resolver.ArtifactResolutionException;
import org.apache.maven.artifact.resolver.ArtifactResolutionResult;
import org.apache.maven.artifact.resolver.filter.AndArtifactFilter;
import org.apache.maven.artifact.resolver.filter.ScopeArtifactFilter;
import org.apache.maven.artifact.versioning.InvalidVersionSpecificationException;
import org.apache.maven.artifact.versioning.VersionRange;
import org.apache.maven.model.Dependency;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;

import java.io.File;
import java.util.Set;

/**
 * Resolves .NET assemblies from special locations, as for example the GAC.
 * Run this only, if you want to resolve special dependencies before other
 * NATIVE maven plugins that require the dependencies run.
 *
 * NPandays plugins should requrest the needed dependency resolution themselves.
 *
 * @author Shane Isbell
 * @author <a href="me@lcorneliussen.de">Lars Corneliussen, Faktum Software</a>
 * @goal resolve
 */
public class ResolveMojo
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
     * @parameter expression="${resolve.requiredScope}" default-value="test"
     */
    private String requiredScope;

    /**
     * @parameter expression="${resolve.pdbs}" default-value="false"
     */
    private Boolean resolvePdbs;

    /**
     * @component
     */
    private NPandayDependencyResolution dependencyResolution;

    /**
     * @parameter expression="${resolve.skip}" default-value="false"
     */
    private boolean skip;

    public void execute() throws MojoExecutionException, MojoFailureException
    {
        if ( skip )
        {
            getLog().info( "NPANDAY-149-001: Mojo for resolving dependencies was intentionally skipped" );
            return;
        }

        SettingsUtil.applyCustomSettingsIfAvailable( getLog(), repositoryRegistry, settingsPath );

        getLog().warn(
            "NPANDAY-149-002: Mojo for resolving dependencies beforehand is executed! It should only be run, "
                + "if native maven plugins require special dependencies to be resolved!"
        );

        try
        {
            AndArtifactFilter filter = new AndArtifactFilter();
            filter.add(new ScopeArtifactFilter(requiredScope));

            OrArtifactFilter types = new OrArtifactFilter();
            types.add(new DotnetAssemblyArtifactFilter());
            if (resolvePdbs){
                types.add(new DotnetSymbolsArtifactFilter());
            }

            dependencyResolution.require( project, LocalRepositoryUtil.create( localRepository ), requiredScope );
        }
        catch ( ArtifactResolutionException e )
        {
            throw new MojoExecutionException( "NPANDAY-149-003: early dependency resolution for scope " + requiredScope + " failed!", e );
        }
    }


}
