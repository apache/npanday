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

package npanday.plugin.libraryimporter.install;

import com.google.common.base.Strings;
import npanday.plugin.libraryimporter.model.NugetPackageLibrary;
import npanday.plugin.libraryimporter.skeletons.AbstractHandleEachLibraryMojo;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.installer.ArtifactInstallationException;
import org.apache.maven.artifact.installer.ArtifactInstaller;
import org.apache.maven.artifact.metadata.ArtifactMetadata;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.repository.ArtifactRepositoryFactory;
import org.apache.maven.artifact.repository.layout.ArtifactRepositoryLayout;
import org.apache.maven.artifact.repository.layout.DefaultRepositoryLayout;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.artifact.ProjectArtifactMetadata;
import org.codehaus.plexus.digest.Digester;
import org.codehaus.plexus.digest.DigesterException;
import org.codehaus.plexus.util.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;

/**
 * Providing functionality for installing artifacts to a local repository.
 *
 * @author <a href="me@lcorneliussen.de">Lars Corneliussen, Faktum Software</a>
 * @author <a href="mailto:brett@apache.org">Brett Porter</a>
 */
public abstract class AbstractInstallMojo
    extends AbstractHandleEachLibraryMojo
{

    /**
     * @component
     */
    protected ArtifactFactory artifactFactory;

    /**
     * @component
     */
    protected ArtifactInstaller installer;


    /**
     * @component
     */
    protected ArtifactRepositoryFactory artifactRepositoryFactory;

    /**
     * @parameter expression="${localRepository}"
     * @required
     * @readonly
     */
    private ArtifactRepository localArtifactRepository;

    /**
     * Specifies an override for the local repository to which the imported artifacts should be installed.
     *
     * @parameter expression="${libimport.localRepository}"
     */
    private String localRepository;

    /**
     * Flag whether to create checksums (MD5, SHA-1) or not.
     *
     * @parameter expression="${libimport.createChecksums}" default-value="false"
     */
    protected boolean createChecksums;

    /**
     * Digester for MD5.
     *
     * @component role-hint="md5"
     */
    protected Digester md5Digester;

    /**
     * Digester for SHA-1.
     *
     * @component role-hint="sha1"
     */
    protected Digester sha1Digester;

    /**
     * Gets the path of the specified artifact within the local repository. Note that the returned path need not exist
     * (yet).
     *
     * @param artifact The artifact whose local repo path should be determined, must not be <code>null</code>.
     * @return The absolute path to the artifact when installed, never <code>null</code>.
     */
    protected File getLocalRepoFile( Artifact artifact )
    {
        String path = getLocalArtifactRepository().pathOf( artifact );
        return new File( getLocalArtifactRepository().getBasedir(), path );
    }

    /**
     * Gets the path of the specified artifact metadata within the local repository. Note that the returned path need
     * not exist (yet).
     *
     * @param metadata The artifact metadata whose local repo path should be determined, must not be <code>null</code>.
     * @return The absolute path to the artifact metadata when installed, never <code>null</code>.
     */
    protected File getLocalRepoFile( ArtifactMetadata metadata )
    {
        String path = getLocalArtifactRepository().pathOfLocalRepositoryMetadata(
            metadata, getLocalArtifactRepository()
        );
        return new File( getLocalArtifactRepository().getBasedir(), path );
    }

    /**
     * Installs the checksums for the specified artifact (and its metadata files) if this has been enabled in the plugin
     * configuration. This method creates checksums for files that have already been installed to the local repo to
     * account for on-the-fly generated/updated files. For example, in Maven 2.0.4- the
     * <code>ProjectArtifactMetadata</code> did not install the original POM file (cf. MNG-2820). While the plugin
     * currently requires Maven 2.0.6, we continue to hash the installed POM for robustness with regard to future
     * changes like re-introducing some kind of POM filtering.
     *
     * @param artifact The artifact for which to create checksums, must not be <code>null</code>.
     * @throws MojoExecutionException If the checksums could not be installed.
     */
    protected void installChecksums( Artifact artifact ) throws MojoExecutionException
    {
        if ( !createChecksums )
        {
            return;
        }

        File artifactFile = getLocalRepoFile( artifact );
        installChecksums( artifactFile );

        Collection metadatas = artifact.getMetadataList();
        if ( metadatas != null )
        {
            for ( Iterator it = metadatas.iterator(); it.hasNext(); )
            {
                ArtifactMetadata metadata = (ArtifactMetadata) it.next();
                File metadataFile = getLocalRepoFile( metadata );
                installChecksums( metadataFile );
            }
        }
    }

    /**
     * Installs the checksums for the specified file (if it exists).
     *
     * @param installedFile The path to the already installed file in the local repo for which to generate checksums,
     *                      must not be <code>null</code>.
     * @throws MojoExecutionException If the checksums could not be installed.
     */
    private void installChecksums( File installedFile ) throws MojoExecutionException
    {
        boolean signatureFile = installedFile.getName().endsWith( ".asc" );
        if ( installedFile.isFile() && !signatureFile )
        {
            installChecksum( installedFile, installedFile, md5Digester, ".md5" );
            installChecksum( installedFile, installedFile, sha1Digester, ".sha1" );
        }
    }

    /**
     * Installs a checksum for the specified file.
     *
     * @param originalFile  The path to the file from which the checksum is generated, must not be <code>null</code>.
     * @param installedFile The base path from which the path to the checksum files is derived by appending the given
     *                      file extension, must not be <code>null</code>.
     * @param digester      The checksum algorithm to use, must not be <code>null</code>.
     * @param ext           The file extension (including the leading dot) to use for the checksum file, must not be
     *                      <code>null</code>.
     * @throws MojoExecutionException If the checksum could not be installed.
     */
    private void installChecksum( File originalFile, File installedFile, Digester digester, String ext ) throws
        MojoExecutionException
    {
        String checksum;
        getLog().debug( "Calculating " + digester.getAlgorithm() + " checksum for " + originalFile );
        try
        {
            checksum = digester.calc( originalFile );
        }
        catch ( DigesterException e )
        {
            throw new MojoExecutionException(
                "Failed to calculate " + digester.getAlgorithm() + " checksum for " + originalFile, e
            );
        }

        File checksumFile = new File( installedFile.getAbsolutePath() + ext );
        getLog().debug( "Installing checksum to " + checksumFile );
        try
        {
            checksumFile.getParentFile().mkdirs();
            FileUtils.fileWrite( checksumFile.getAbsolutePath(), "UTF-8", checksum );
        }
        catch ( IOException e )
        {
            throw new MojoExecutionException( "Failed to install checksum to " + checksumFile, e );
        }
    }

    protected ArtifactRepository getLocalArtifactRepository()
    {
        ArtifactRepositoryLayout layout = new DefaultRepositoryLayout();
        if ( !Strings.isNullOrEmpty( localRepository ) )
        {
            String localRepoUrl = new File( localRepository ).toURI().toString();

            getLog().info( "NPANDAY-146-002: Using alternate local repository " + localRepository );

            localArtifactRepository = artifactRepositoryFactory.createArtifactRepository(
                "library-importer-local", localRepoUrl, layout, null, null
            );
        }
        return localArtifactRepository;
    }

    protected void handleLibrary( NugetPackageLibrary lib ) throws MojoExecutionException, MojoFailureException
    {
        Artifact artifact = artifactFactory.createBuildArtifact(
            lib.getMavenGroupId(), lib.getMavenArtifactId(), lib.getMavenVersion(), lib.getMavenPackaging()
        );

        artifact.setFile( lib.getMavenArtifactFile() );

        ProjectArtifactMetadata metadata = new ProjectArtifactMetadata( artifact, lib.getMavenPomFile() );
        artifact.addMetadata( metadata );

        handleGeneratedArtifacts( lib, artifact );
    }

    protected void install( NugetPackageLibrary lib, Artifact artifact, ArtifactRepository localRepository ) throws
        ArtifactInstallationException,
        MojoExecutionException
    {

        installer.install( lib.getMavenArtifactFile(), artifact, localRepository );
    }

    protected void markDeployed( NugetPackageLibrary lib, Artifact artifact, ArtifactRepository repo ) throws
        MojoExecutionException
    {
        File markerFile = lib.getMarkerFileFor( artifact, repo );

        try
        {
            markerFile.createNewFile();
        }
        catch ( IOException e )
        {
            throw new MojoExecutionException(
                "NPANDAY-146-003: Error creating marker file for " + repo.getUrl() + ": " + markerFile.toPath()
            );
        }
    }

    protected abstract void handleGeneratedArtifacts( NugetPackageLibrary lib, Artifact artifact ) throws
        MojoExecutionException,
        MojoFailureException;
}
