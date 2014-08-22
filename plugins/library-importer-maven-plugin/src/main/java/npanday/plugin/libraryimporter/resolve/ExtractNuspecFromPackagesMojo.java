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

package npanday.plugin.libraryimporter.resolve;

import npanday.plugin.libraryimporter.model.NugetPackage;
import npanday.plugin.libraryimporter.skeletons.AbstractHandleEachImportMojo;
import npanday.plugin.libraryimporter.skeletons.AbstractLibraryImportsProvidingMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.codehaus.plexus.util.IOUtil;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Extract nuspec files from resolved Nuget packages.
 *
 * @author <a href="mailto:lcorneliussen@apache.org">Lars Corneliussen</a>
 * @goal extract-nuspec
 */
public class ExtractNuspecFromPackagesMojo
    extends AbstractHandleEachImportMojo
{
    @Override
    protected void handleNugetPackage( NugetPackage nuget ) throws MojoExecutionException, MojoFailureException
    {
        FileInputStream zipFile = null;
        try
        {
            zipFile = new FileInputStream(  nuget.getPackageFile().getAbsolutePath() );
            ZipInputStream zip = new ZipInputStream( zipFile );
            ZipEntry ze;

            String foundSpec = null;

            while ( ( ze = zip.getNextEntry() ) != null )
            {
                if ( ze.getName().endsWith( ".nuspec" ) )
                {
                    foundSpec = ze.getName();

                    byte[] buf = new byte[1024];

                    FileOutputStream fileoutputstream = null;
                    try
                    {
                        fileoutputstream = new FileOutputStream( nuget.getNuspecFile().getAbsolutePath() );

                        int n;
                        while ( ( n = zip.read( buf, 0, 1024 ) ) > -1 )
                        {
                            fileoutputstream.write( buf, 0, n );
                        }
                    }
                    finally
                    {
                        IOUtil.close( fileoutputstream );
                    }

                    break;
                }
            }

            if ( foundSpec == null )
            {
                throw new MojoExecutionException(
                    "NPANDAY-139-004: Could not find nuspec in package file " + nuget.getPackageFile()
                );
            }
            else
            {
                if ( getLog().isDebugEnabled() )
                {
                    getLog().debug(
                        "NPANDAY-139-005: found nuspec " + foundSpec + " and extracted to " + nuget.getNuspecFile()
                    );
                }
            }
        }
        catch ( FileNotFoundException e )
        {
            throw new MojoExecutionException(
                "NPANDAY-139-001: Could not find package file " + nuget.getPackageFile().getAbsolutePath()
            );
        }
        catch ( IOException e )
        {
            throw new MojoExecutionException(
                "NPANDAY-139-003: Error on reading or extracting zip contents of " + nuget.getPackageFile()
            );
        }
        finally
        {
            IOUtil.close( zipFile );
        }
    }

}
