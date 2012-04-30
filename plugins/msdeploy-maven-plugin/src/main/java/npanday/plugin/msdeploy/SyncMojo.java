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

package npanday.plugin.msdeploy;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import npanday.ArtifactType;
import npanday.LocalRepositoryUtil;
import npanday.resolver.NPandayArtifactResolver;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.resolver.ArtifactNotFoundException;
import org.apache.maven.artifact.resolver.ArtifactResolutionException;
import org.apache.maven.artifact.versioning.VersionRange;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.settings.Server;
import org.apache.maven.settings.Settings;
import org.codehaus.plexus.PlexusConstants;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.codehaus.plexus.context.Context;
import org.codehaus.plexus.context.ContextException;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Contextualizable;
import org.sonatype.plexus.components.sec.dispatcher.SecDispatcher;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Set;

/**
 * Resolves all MSDeploy-Packages from project dependencies and syncs
 * them using the specified configurations.
 *
 * @author <a href="mailto:lcorneliussen@apache.org">Lars Corneliussen</a>
 * @goal sync
 */
public class SyncMojo
    extends AbstractMsDeployMojo<SyncCommand>
    implements Contextualizable
{
    /**
     * @parameter default-value="false"
     */
    boolean allowUntrusted;

    /**
     * @parameter default-value="${settings.localRepository}"
     */
    String localRepository;

    /**
     * @parameter
     */
    List<SyncCommand> items;

    /**
     * @parameter
     */
    SyncDestination destination;

    /**
     * @parameter default-value="${settings}"
     */
    Settings settings;

    /**
     * @component
     */
    ArtifactFactory artifactFactory;

    /**
     * @component
     */
    NPandayArtifactResolver artifactResolver;

    private PlexusContainer container;

    private Object maven2Or3SecDispatcher;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException
    {
        try
        {
            maven2Or3SecDispatcher = container.lookup( SecDispatcher.ROLE, "maven" );
        }
        catch ( ComponentLookupException e )
        {
            throw new MojoExecutionException( "NPANDAY-153-003: Error on resolving SecDispatcher", e );
        }


        super.execute();
    }

    @Override
    protected void afterCommandExecution( SyncCommand iterationItem ) throws MojoExecutionException
    {

    }

    @Override
    protected void beforeCommandExecution( SyncCommand iterationItem )
    {

    }

    @Override
    protected List<SyncCommand> prepareIterationItems() throws MojoFailureException, MojoExecutionException
    {
        Set<Artifact> artifacts = Sets.newHashSet();

        setCredentials( destination );

        for ( SyncCommand item : items )
        {
            Artifact artifact = artifactFactory.createDependencyArtifact(
                item.getGroupId(), item.getArtifactId(), VersionRange.createFromVersion( item.getVersion() ),
                ArtifactType.MSDEPLOY_PACKAGE.getPackagingType(), null, "compile"
            );
            artifacts.add( artifact );

            try
            {
                artifactResolver.resolve( artifact, project.getRemoteArtifactRepositories(), LocalRepositoryUtil
                        .create( localRepository ) );
            }
            catch ( ArtifactResolutionException e )
            {
                throw new MojoExecutionException( "NPANDAY-153-001: unable to resolve msdeploy package " + artifact.getId(), e );
            }
            catch ( ArtifactNotFoundException e )
            {
                throw new MojoExecutionException( "NPANDAY-153-002: unable to resolve msdeploy package " + artifact.getId(), e );
            }

            setCredentials( item.getDestination() );

            item.contextualize( artifact, destination );
        }


        return items;
    }

    private void setCredentials( SyncDestination destination ) throws MojoExecutionException
    {
        if (destination == null)
            return;

        if ( !Strings.isNullOrEmpty( destination.getServerId() ) )
        {
            Server server = settings.getServer( destination.getServerId() );

            if (server == null){
                throw new MojoExecutionException( "NPANDAY-153-004: Could not find credentials for server " + destination.getServerId() );
            }

            destination.setSettingsUsername( server.getUsername() );
            destination.setSettingsPassword( decrypt( server.getPassword() ) );
        }
    }

    private String decrypt( String password ) throws MojoExecutionException
    {
        try
        {
            Method decrypt = maven2Or3SecDispatcher.getClass().getMethod( "decrypt", String.class );
            return (String) decrypt.invoke(
                maven2Or3SecDispatcher, password
            );
        }
        catch ( Exception e )
        {
            throw new MojoExecutionException( "NPANDAY-153-005: Error on decrypting password", e );
        }
    }

    @Override
    protected List<String> getCommands( SyncCommand item ) throws MojoExecutionException, MojoFailureException
    {
        List<String> commands = Lists.newArrayList();

        commands.add( "-verb:sync" );
        commands.add( "-source:package=" + item.getPackageSource().getAbsolutePath() );
        commands.add( "-dest:" + item.getDestinationArgument() );

        if (item.getPreSync() != null){
            commands.add( "-preSync:" + item.getPreSync().getArgumentPart() );
        }

        if (item.getPostSync() != null){
            commands.add( "-postSync:" + item.getPostSync().getArgumentPart() );
        }

        if ( allowUntrusted )
        {
            commands.add( "-allowUntrusted" );
        }
        return commands;
    }

    public void contextualize( Context context ) throws ContextException
    {
        container = (PlexusContainer) context.get( PlexusConstants.PLEXUS_KEY );
    }
}
