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

package npanday.plugin.msdeploy.sync;

import com.google.common.base.Strings;
import com.google.common.collect.Sets;
import npanday.ArtifactType;
import npanday.plugin.msdeploy.AbstractMsDeployMojo;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.resolver.ArtifactNotFoundException;
import org.apache.maven.artifact.resolver.ArtifactResolutionException;
import org.apache.maven.artifact.versioning.VersionRange;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.settings.Server;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.sonatype.plexus.components.sec.dispatcher.SecDispatcher;

import java.util.List;
import java.util.Set;

/**
 * Resolves all MSDeploy-Packages from project dependencies and syncs
 * them using the specified configurations.
 *
 * @author <a href="mailto:lcorneliussen@apache.org">Lars Corneliussen</a>
 * @goal sync-packages
 */
public class SyncPackagesMojo
    extends AbstractMsDeployMojo<Package>
{
   /**
     * The packages to sync.
     *
     * @parameter
     */
    List<Package> packages;

    /**
     * The default sync destination for all packages. Can be redefined per package.
     *
     * @parameter
     */
    Destination destination;

    @Override
    protected void afterCommandExecution( Package pkg ) throws MojoExecutionException
    {

    }

    @Override
    protected void beforeCommandExecution( Package pkg )
    {

    }

    @Override
    protected List<Package> prepareIterationItems() throws MojoFailureException, MojoExecutionException
    {
        Set<Artifact> artifacts = Sets.newHashSet();

        setCredentials( destination );

        for ( Package pkg : packages )
        {
            Artifact artifact = artifactFactory.createDependencyArtifact(
                pkg.getGroupId(), pkg.getArtifactId(), VersionRange.createFromVersion( pkg.getVersion() ),
                ArtifactType.MSDEPLOY_PACKAGE.getPackagingType(), pkg.getClassifier(), "compile"
            );
            artifacts.add( artifact );

            try
            {
                resolve( artifact );
            }
            catch ( ArtifactResolutionException e )
            {
                throw new MojoExecutionException(
                    "NPANDAY-153-001: unable to resolve msdeploy package " + artifact.getId(), e
                );
            }
            catch ( ArtifactNotFoundException e )
            {
                throw new MojoExecutionException(
                    "NPANDAY-153-002: unable to resolve msdeploy package " + artifact.getId(), e
                );
            }

            setCredentials( pkg.getDestination() );

            pkg.contextualize( artifact, destination );
        }

        return packages;
    }

    private void setCredentials( Destination destination ) throws MojoExecutionException
    {
        if ( destination == null )
        {
            return;
        }

        if ( !Strings.isNullOrEmpty( destination.getServerId() ) )
        {
            Server server = getServerSettings( destination.getServerId() );

            destination.setSettingsUsername( server.getUsername() );
            destination.setSettingsPassword( decrypt( server.getPassword() ) );
        }
    }

    @Override
    protected List<String> getCommands( Package pkg ) throws MojoExecutionException, MojoFailureException
    {
        List<String> commands = super.getDefaultCommands();

        commands.add( "-verb:sync" );
        commands.add( "-source:package=" + pkg.getPackageSource().getAbsolutePath() );
        commands.add( "-dest:" + pkg.getDestinationArgument() );

        commands.addAll( pkg.getAdditionalArguments() );

        return commands;
    }
}
