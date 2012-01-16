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

package npanday.packaging;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import npanday.PlatformUnsupportedException;
import npanday.msbuild.xdt.XmlDocumentTransformException;
import npanday.msbuild.xdt.XmlDocumentTransformer;
import npanday.vendor.VendorRequirement;
import org.apache.maven.plugin.MojoFailureException;
import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.util.DirectoryScanner;
import org.codehaus.plexus.util.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Common configuration file handling for the various packaging formats/mojos.
 *
 * @author <a href="mailto:lcorneliussen@apache.org">Lars Corneliussen</a>
 * @plexus.component role="npanday.packaging.ConfigFileHandler"
 */
public class ConfigFileHandler
    extends AbstractLogEnabled
{
    public static final String[] DEFAULT_EXCLUDES = new String[]{ "pom.xml" };

    /**
     * @plexus.requirement
     */
    private XmlDocumentTransformer xdt;

    private File workingFolder;

    public void setWorkingFolder( File workingFolder )
    {
        this.workingFolder = workingFolder;
    }

    /**
     * Will either transform or copy the selected config file.
     */
    public void handleConfigFile(
        VendorRequirement vendorRequirement, File sourceConfigFile, String transformationHint, File targetConfigFile ) throws MojoFailureException
    {
        xdt.setWorkingFolder(workingFolder);

        if ( !sourceConfigFile.exists() )
        {
            getLogger().warn( "NPANDAY-133-001: The configuration file '" + sourceConfigFile + "' couldn't be found" );
            return;
        }

        File transformationFile = null;

        final String sourceConfigFileName = sourceConfigFile.getName();

        if ( transformationHint != null )
        {
            int extensionStart = sourceConfigFileName.lastIndexOf( "." );
            String transformationFileName =
                sourceConfigFileName.substring( 0, extensionStart ) + "." + transformationHint
                    + sourceConfigFileName.substring( extensionStart );

            final File probingFile = new File( sourceConfigFile.getParentFile(), transformationFileName );
            if ( probingFile.exists() )
            {
                getLogger().debug(
                    "NPANDAY-133-006: Found transformation file for " + sourceConfigFileName + ": "
                        + probingFile.getAbsolutePath()
                );

                transformationFile = probingFile;
            }
            else
            {
                getLogger().warn(
                    "NPANDAY-133-007: Could not find transformation file for " + sourceConfigFileName + "; tried: "
                        + probingFile.getAbsolutePath()
                );
            }
        }

        if ( transformationFile != null )
        {
            getLogger().info(
                "NPANDAY-133-004: Transforming " + sourceConfigFileName + " with " + transformationFile
                    + " for packaging"
            );

            try
            {
                getLogger().debug(
                    "NPANDAY-133-002: Copying config file '" + sourceConfigFile + "' to '" + targetConfigFile + "'"
                );
                xdt.transform( vendorRequirement, sourceConfigFile, transformationFile, targetConfigFile );
            }
            catch ( PlatformUnsupportedException e )
            {
                throw new MojoFailureException(
                    "NPANDAY-133-008: Unable to transform config file '" + sourceConfigFile + "'", e
                );
            }
            catch ( XmlDocumentTransformException e )
            {
                 throw new MojoFailureException(
                    "NPANDAY-133-009: Unable to transform config file '" + sourceConfigFile + "'", e
                );
            }
        }
        else
        {
            getLogger().info( "NPANDAY-133-005: Copying " + sourceConfigFileName + " for packaging" );

            try
            {
                getLogger().debug(
                    "NPANDAY-133-010: Copying config file '" + sourceConfigFile + "' to '" + targetConfigFile + "'"
                );
                FileUtils.copyFile( sourceConfigFile, targetConfigFile );
            }
            catch ( IOException e )
            {
                throw new MojoFailureException(
                    "NPANDAY-133-011: Unable to copy config file '" + sourceConfigFile + "' to '" + targetConfigFile
                        + "'"
                );
            }
        }
    }

    public void handleConfigFiles(
        VendorRequirement vendorRequirement, File baseDir, String[] includes, String[] excludes, String transformationHint, File targetFolder ) throws
        MojoFailureException
    {
        DirectoryScanner scanner = new DirectoryScanner();
        scanner.setBasedir( baseDir );
        scanner.setIncludes( includes );

        List<String> excludesList = Lists.newArrayList(excludes);
        excludesList.addAll( Lists.newArrayList( DEFAULT_EXCLUDES ) );
        scanner.setExcludes( excludesList.toArray( new String[0] ) );

        getLogger().debug(
            "NPANDAY-133-012: Config file includes: " + Lists.newArrayList( includes )
        );
        getLogger().debug(
            "NPANDAY-133-013: Config file excludes: " + excludes
        );

        scanner.scan();

        for ( String configFilePath : scanner.getIncludedFiles() )
        {
            File configFile = new File( configFilePath );


            if ( isTransformationFile( configFile ) )
            {
                getLogger().debug(
                    "NPANDAY-133-014: Skipped file, because it seems to be a transformation: "
                        + configFile.getName()
                );
                continue;
            }

            final File targetConfigFile = new File( targetFolder, relativize(configFile, baseDir) );

            handleConfigFile(
                vendorRequirement, configFile, transformationHint, targetConfigFile
            );
        }
    }

    private String relativize( File file, File base )
    {
        return base.toURI().relativize(file.toURI()).getPath();
    }

    private Splitter SPLIT_ON_DOTS = Splitter.on( '.' );
    private Joiner JOIN_WITH_DOTS = Joiner.on( '.' );


    public boolean isTransformationFile( File filePath )
    {
        String fileName = filePath.getName();
        List<String> parts = Lists.newArrayList( SPLIT_ON_DOTS.split( fileName ) );

        if (parts.size() < 3)
            return false;

        // remove the transformation hint between name and extension
        parts.remove( parts.size()-2 );

        // and check if a target file exists
        if (new File( filePath.getParent(), JOIN_WITH_DOTS.join( parts ) ).exists())
            return true;

        return false;
    }
}
