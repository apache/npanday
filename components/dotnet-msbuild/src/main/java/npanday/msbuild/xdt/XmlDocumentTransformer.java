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

package npanday.msbuild.xdt;

import com.google.common.base.Preconditions;
import com.google.common.io.ByteStreams;
import com.google.common.io.Files;
import com.google.common.io.InputSupplier;
import com.google.common.io.OutputSupplier;
import com.google.common.io.Resources;
import npanday.PlatformUnsupportedException;
import npanday.msbuild.MsbuildException;
import npanday.msbuild.MsbuildInvocationParameters;
import npanday.msbuild.MsbuildInvoker;
import npanday.vendor.VendorRequirement;
import org.codehaus.plexus.interpolation.os.Os;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

/**
 * @author <a href="mailto:lcorneliussen@apache.org">Lars Corneliussen</a>
 * @plexus.component role="npanday.msbuild.xdt.XmlDocumentTransformer"
 */
public class XmlDocumentTransformer
{
    /**
     * @plexus.requirement
     */
    private MsbuildInvoker msbuild;

    private File workingFolder;

    /**
     * There will be some need for temporary files; these will be stored in this folder.
     *
     * @param workingFolder
     */
    public void setWorkingFolder( File workingFolder )
    {
        this.workingFolder = workingFolder;
    }

    public void transform(
        VendorRequirement vendorRequirement, File baseFile, File transformationFile, File targetFile ) throws
        PlatformUnsupportedException, XmlDocumentTransformException
    {
        Preconditions.checkArgument( baseFile != null, "Argument 'baseFile' was null!" );
        Preconditions.checkArgument( transformationFile != null, "Argument 'baseFile' was null!" );
        Preconditions.checkArgument( targetFile != null, "Argument 'baseFile' was null!" );

        Preconditions.checkNotNull( workingFolder, "tempFolder was not yet specified!" );

        final String resourceName = "/msbuild/xdt.msbuild.xml";
        final File buildFile;
        try
        {
            buildFile = extractResource( resourceName );
        }
        catch ( IOException e )
        {
            throw new XmlDocumentTransformException(
                "NPANDAY-136-001: Error when extracting build file from resources: " + resourceName, e
            );
        }

        final MsbuildInvocationParameters parameters = new MsbuildInvocationParameters(
            vendorRequirement, buildFile
        );

        parameters.setProperty( "Source", baseFile.getAbsolutePath() );
        parameters.setProperty( "Transform", transformationFile.getAbsolutePath() );
        parameters.setProperty( "Destination", targetFile.getAbsolutePath() );
        parameters.setProperty( "VisualStudioVersion", findRequiredVSVersion() );

        targetFile.getParentFile().mkdirs();

        try
        {
            msbuild.invoke( parameters );
        }
        catch ( MsbuildException e )
        {
            throw new XmlDocumentTransformException(
                "NPANDAY-136-000: Error occured while trying to transform '" + baseFile.getName() + "' with "
                    + transformationFile.getName(), e
            );
        }
    }

    private static String findRequiredVSVersion() throws XmlDocumentTransformException {
        if (Os.isArch("amd64")) {
            return findRequiredVSVersion(System.getenv("PROGRAMFILES(X86)"));
        }
        else {
            return findRequiredVSVersion(System.getenv("PROGRAMFILES"));
        }
    }

    private static String findRequiredVSVersion(String programfiles) throws XmlDocumentTransformException {
        File[] dirs = new File( programfiles, "MSBuild/Microsoft/VisualStudio" ).listFiles();
        String version = null;
        if (dirs != null) {
            for (File dir : dirs) {
                if (new File(dir, "Web/Microsoft.Web.Publishing.Tasks.dll").exists()) {
                    version = dir.getName().substring(1);
                }
            }
        }
        if (version == null) {
            throw new XmlDocumentTransformException("Unable to find required tasks file in '" + programfiles +
                    "\\MSBuild\\Microsoft\\VisualStudio'");
        }
        return version;
    }

    private File extractResource( String resourceName ) throws IOException
    {
        final URL resource = Resources.getResource( getClass(), resourceName );
        final InputSupplier<InputStream> in = Resources.newInputStreamSupplier(
            resource
        );

        final File outFile = new File( new File( workingFolder, "tmp" ), resourceName );
        outFile.getParentFile().mkdirs();
        outFile.createNewFile();
        final OutputSupplier<FileOutputStream> out = Files.newOutputStreamSupplier(
            outFile
        );

        ByteStreams.copy( in, out );

        return outFile;
    }
}
