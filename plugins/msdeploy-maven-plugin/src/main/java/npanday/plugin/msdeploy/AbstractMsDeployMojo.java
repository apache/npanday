package npanday.plugin.msdeploy;

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

import com.google.common.collect.Lists;
import npanday.LocalRepositoryUtil;
import npanday.PlatformUnsupportedException;
import npanday.executable.ExecutableRequirement;
import npanday.executable.ExecutionException;
import npanday.executable.NetExecutable;
import npanday.plugin.msdeploy.sync.Destination;
import npanday.registry.RepositoryRegistry;
import npanday.resolver.NPandayArtifactResolver;
import npanday.vendor.SettingsUtil;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.resolver.ArtifactNotFoundException;
import org.apache.maven.artifact.resolver.ArtifactResolutionException;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectHelper;
import org.apache.maven.settings.Server;
import org.apache.maven.settings.Settings;
import org.codehaus.plexus.PlexusConstants;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.codehaus.plexus.context.Context;
import org.codehaus.plexus.context.ContextException;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Contextualizable;
import org.sonatype.plexus.components.sec.dispatcher.SecDispatcher;

import java.lang.reflect.Method;
import java.util.List;


/**
 * @author <a href="mailto:lcorneliussen@apache.org">Lars Corneliussen</a>
 */
public abstract class AbstractMsDeployMojo<T>
    extends AbstractMojo
    implements Contextualizable
{
    /**
     * @parameter expression="${npanday.settings}" default-value="${user.home}/.m2"
     */
    private String settingsPath;


    /**
     * The vendor of the framework, the executable is provided by or compatible with.
     *
     * @parameter expression="${vendor}"
     */
    private String vendor;

    /**
     * The version of the framework vendor, the executable is provided by or compatible with.
     *
     * @parameter expression="${vendorVersion}"
     */
    private String vendorVersion;

    /**
     * The framework version, the executable is compatible with.
     *
     * @parameter expression = "${frameworkVersion}"
     */
    private String frameworkVersion;

        /**
     * The executable identifier used to locate the right configurations from executable-plugins.xml. Can't be changed.
     */
    private String executableIdentifier = "MSDEPLOY";

    /**
     * The configured executable version, from executable-plugins.xml, to be used. Should align to a installed
     * Azure SDK Version.
     *
     * @parameter expression="${msdeploy.version}" default-value="2"
     */
    private String executableVersion;

    /**
     * The configured executable profile, from executable-plugins.xml, to be used.
     *
     * @parameter expression="${msdeploy.profile}"
     */
    private String executableProfile;

    /**
     * @component
     */
    private RepositoryRegistry repositoryRegistry;

    /**
     * @component
     */
    private npanday.executable.NetExecutableFactory netExecutableFactory;

    /**
     * The maven project.
     *
     * @parameter expression="${project}"
     * @required
     */
    protected MavenProject project;

    /**
     * The maven project helper.
     *
     * @component
     */
    protected MavenProjectHelper projectHelper;

    /**
     * True if untrusted SSL connections are allowed; otherwise, false. The default is false.
     *
     * @parameter default-value="false"
     */
    boolean allowUntrusted;

    /**
     * Specifies that files will be compared by using their CRC (Cyclic Redundancy Check) checksum and ignoring their last write time. This setting is useful when you want to copy only the files whose content has changed, and ignore files that have the same content but different time stamps.
     *
     * @parameter default-value="false"
     */
    boolean useCheckSum;

    /**
     * Specifies that the command will be run without actually making any changes.
     *
     * @parameter default-value="false"
     */
    boolean whatIf;

    /**
     * Specifies that the Informational verbosity level will be included in the output of the operation. By default, the verbosity levels of Warning, Error, and Fatal are included. The Informational verbosity level will return all messages that are triggered during an operation.
     *
     * @parameter default-value="false"
     */
    boolean verbose;

    /**
     * Specifies the number of times the provider will retry after a failure. <number> specifies the number of
     * retries. The default number of retries is 5. By default, there is a delay of one second between each retry.
     *
     * @parameter default-value="1"
     */
    int retryAttempts;

    /**
     * Specifies, in milliseconds, the interval between provider retry attempts. <milliseconds> specifies the number
     * of milliseconds between retries. The default is 1000 (one second).
     *
     * @parameter default-value="1000"
     */
    int retryInterval;

    /**
     * @parameter default-value="${settings.localRepository}"
     */
    String localRepository;

    /**
     * @parameter default-value="${settings}"
     */
    Settings settings;

    /**
     * @component
     */
    protected ArtifactFactory artifactFactory;

    /**
     * @component
     */
    protected NPandayArtifactResolver artifactResolver;

    protected PlexusContainer container;

    protected Object maven2Or3SecDispatcher;

    public void execute() throws MojoExecutionException, MojoFailureException
    {
        SettingsUtil.applyCustomSettings( getLog(), repositoryRegistry, settingsPath );

        try
        {
            maven2Or3SecDispatcher = container.lookup( SecDispatcher.ROLE, "maven" );
        }
        catch ( ComponentLookupException e )
        {
            throw new MojoExecutionException( "NPANDAY-120-008: Error on resolving SecDispatcher", e );
        }

        final List<T> iterationItems = prepareIterationItems();

        getLog().info( "NPANDAY-120-003: Configured exection syncCommands " + iterationItems );

        for(T iterationItem : iterationItems ){
            getLog().info( "NPANDAY-120-005: Exectuting iteration item " + iterationItem );

            beforeCommandExecution(iterationItem);

            try
            {
                final NetExecutable executable = netExecutableFactory.getExecutable(
                    new ExecutableRequirement( vendor, vendorVersion, frameworkVersion, executableIdentifier ),
                    getCommands( iterationItem ), null
                );
                executable.execute();
            }
            catch ( ExecutionException e )
            {
                throw new MojoExecutionException(
                    "NPANDAY-120-000: Unable to execute '" + executableIdentifier + "' for vendor " + vendor + " v"
                        + vendorVersion + " and frameworkVersion = " + frameworkVersion, e
                );
            }
            catch ( PlatformUnsupportedException e )
            {
                throw new MojoExecutionException(
                    "NPANDAY-120-001: Unsupported platform configuration. Could not find '" + executableIdentifier
                        + "' for vendor " + vendor + " v" + vendorVersion + " and frameworkVersion = " +
                        frameworkVersion, e
                );
            }

            afterCommandExecution(iterationItem);
        }
    }

    protected abstract void afterCommandExecution( T iterationItem ) throws MojoExecutionException;

    protected abstract void beforeCommandExecution( T iterationItem );

    protected abstract List<T> prepareIterationItems() throws MojoFailureException, MojoExecutionException;

    protected abstract List<String> getCommands(T item) throws MojoExecutionException, MojoFailureException;

    protected String decrypt( String password ) throws MojoExecutionException
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
            throw new MojoExecutionException( "NPANDAY-120-006: Error on decrypting password", e );
        }
    }

    protected void resolve( Artifact artifact ) throws ArtifactResolutionException, ArtifactNotFoundException
    {
        artifactResolver.resolve(
            artifact, project.getRemoteArtifactRepositories(), LocalRepositoryUtil.create( localRepository )
        );
    }

    protected Server getServerSettings( String serverId) throws MojoExecutionException
    {
        Server server = settings.getServer( serverId );

        if ( server == null )
        {
            throw new MojoExecutionException(
                "NPANDAY-120-007: Could not find credentials for server " + serverId
            );
        }
        return server;
    }

    public List<String> getDefaultCommands()
    {
        List<String> commands = Lists.newArrayList();

        if ( allowUntrusted )
        {
            commands.add( "-allowUntrusted" );
        }

        if ( useCheckSum )
        {
            commands.add( "-usechecksum" );
        }

        if ( whatIf )
        {
            commands.add( "-whatIf" );
        }

        if ( verbose )
        {
            commands.add( "-verbose" );
        }

        if ( retryAttempts > 1 )
        {
            commands.add( "-retryAttempts:" + retryAttempts );

        }

        if ( retryInterval != 1000 )
        {
            commands.add( "-retryAttempts:" + retryInterval );
        }

        return commands;
    }

    public void contextualize( Context context ) throws ContextException
    {
        container = (PlexusContainer) context.get( PlexusConstants.PLEXUS_KEY );
    }
}
