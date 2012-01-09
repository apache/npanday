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

package npanday.plugin.application;

import com.google.common.collect.Lists;
import npanday.ArtifactType;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectHelper;
import org.codehaus.plexus.archiver.ArchiverException;
import org.codehaus.plexus.archiver.zip.ZipArchiver;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Package the prepared folders as dotnet-application, and attach the *.app.zip as
 * a project artifact.
 *
 * @author <a href="mailto:lcorneliussen@apache.org">Lars Corneliussen</a>
 * @phase package
 * @goal package
 *
 * @since 1.5.0-incubating
 */
public class PackagePreparedPackageFolders
    extends AbstractIteratingMojo<PreparedPackage>
{

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
     * The Zip archiver.
     *
     * @parameter expression="${component.org.codehaus.plexus.archiver.Archiver#zip}"
     * @required
     */
    private ZipArchiver archiver;

    /**
     * The directory for the created zip
     *
     * @parameter expression = "${outputDirectory}" default-value = "${project.build.directory}"
     */
    private File outputDirectory;


    public void execute() throws MojoExecutionException, MojoFailureException
    {
        executeItems();
    }

    @Override
    protected List<PreparedPackage> prepareIterationItems() throws MojoFailureException, MojoExecutionException
    {
        List<PreparedPackage> list = Lists.newArrayList();

        // TODO: Support multiple packages with different classifiers
        final PreparedPackage pkg = new PreparedPackage( project );

        if ( pkg.exists() )
        {
            getLog().debug( "NPANDAY-130-000: Found a prepared package: " + pkg );
            list.add( pkg );
        }
        else
        {
            getLog().debug( "NPANDAY-130-000: Did not find a prepared package!" );
        }

        return list;
    }

    @Override
    protected void executeItem( PreparedPackage iterationItem ) throws MojoFailureException, MojoExecutionException
    {
        File destinationFile = new File(
            outputDirectory, project.getArtifactId() + "." + ArtifactType.DOTNET_APPLICATION.getExtension()
        );
        archiver.setDestFile( destinationFile );

        getLog().info( "NPANDAY-130-001: Generating package from " + destinationFile.getAbsolutePath() );

        try
        {
            archiver.addDirectory( iterationItem.getPackageFolder() );
        }
        catch ( ArchiverException e )
        {
            throw new MojoExecutionException(
                "NPANDAY-130-002: Unable to package prepared package from "
                    + iterationItem.getPackageFolder().getAbsolutePath(), e
            );
        }

        // create archive
        try
        {
            archiver.createArchive();
        }
        catch ( ArchiverException e )
        {
            throw new MojoExecutionException(
                "NPANDAY-130-003: Unable to package prepared package in " + destinationFile.getAbsolutePath(), e
            );
        }
        catch ( IOException e )
        {
            throw new MojoExecutionException(
                "NPANDAY-130-004: Unable to package prepared package in " + destinationFile.getAbsolutePath(), e
            );
        }

        projectHelper.attachArtifact( project, ArtifactType.DOTNET_APPLICATION.getPackagingType(), destinationFile );
    }
}
